(ns advance.coordination-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [simplify.coordination :as coordination]
            [simplify.codex :as codex]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.main :as main]
            [advance.workflow :as workflow]
            [simplify.runtime :as rt]
            [simplify.workflow :as flow]))

(defn pair []
  (let [a (fixture/context) b (fixture/context)
        directory (str (io/file (:state-dir a) "shared"))]
    [(assoc a :coordination-dir directory :lock-file (str (io/file (:state-dir a) "simplify-harness.lock")))
     (assoc b :coordination-dir directory :lock-file (str (io/file (:state-dir a) "advance-harness.lock")))]))

(deftest actual-path-conflicts-are-shared-and-survive-a-controller-exit
  (let [[a b] (pair)]
    (is (coordination/claim! a "simplify" ["src/reader"]))
    (is (not (coordination/claim! b "reader" ["src/reader/parser.clj"])))
    (is (not (coordination/claim! b "all-source" ["src"])))
    (is (coordination/claim! b "effects" ["src/effects"]))
    (coordination/unregister! a)
    (is (not (coordination/claim! b "reader" ["src/reader"])))
    (is (= 2 (count (coordination/claims b))))
    (coordination/release! a "simplify")
    (is (coordination/claim! b "reader" ["src/reader"]))
    (is (coordination/claim! b "reader" ["src/reader"]))))

(deftest main-writes-serialize-across-coordinators
  (let [[a b] (pair) effects (atom [])]
    (coordination/with-merge a
      #(do (swap! effects conj :first)
           (coordination/with-merge b (fn [] (swap! effects conj :conflicting)))))
    (is (= [:first] @effects))
    (coordination/with-merge b #(swap! effects conj :second))
    (is (= [:first :second] @effects))))

(deftest denials-cannot-transfer-to-another-harness-or-campaign
  (let [[a b] (pair) calls (atom 0)]
    (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 1 :err "Forbidden (HTTP 403)" :out ""})]
      (is (thrown? Exception (gh/api a "POST" "graphql" {:query "mutation first"})))
      (is (thrown? Exception (gh/api b "POST" "graphql" {:query "mutation different"})))
      (is (= 1 @calls)))
    (let [a (assoc-in a [:config :repository] "same/repo") b (assoc-in b [:config :repository] "same/repo")]
      (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 1 :err "Permission denied" :out ""})]
        (is (thrown? Exception (rt/run! a (:state-dir a) ["git" "push" "origin" "first"])))
        (is (thrown? Exception (rt/run! b (:state-dir b) ["git" "push" "origin" "second"])))
        (is (= 2 @calls))))))

(deftest legacy-simplify-process-requires-a-coordination-upgrade
  (let [[a b] (pair)
        legacy (str (io/file (:coordination-dir a) ".." "simplify-harness.lock"))]
    (coordination/with-file-lock legacy
      #(is (thrown-with-msg? Exception #"Restart" (coordination/register! b))))))

(deftest utility-commands-and-rejected-coordinators-cannot-overwrite-live-state
  (let [a (assoc (fixture/context) :coordinator-owner (atom false) :processes (atom #{}) :command "run")
        a (assoc a :lock-file (str (io/file (:state-dir a) "coordinator.lock")))
        b (assoc a :state (atom @(:state a)) :coordinator-owner (atom false))]
    (rt/with-lock a
      #(do
         (rt/update! a assoc :status :running :effects {"in-flight" {:status :pending}})
         (let [expected (rt/read-edn (:state-file a))]
           (with-out-str (main/dispatch! b "status" []))
           (with-out-str (main/dispatch! b "pause" []))
           (main/shutdown! b)
           (is (thrown? Exception (rt/with-lock b (fn [] (rt/update! b assoc :status :running)))))
           (main/shutdown! b)
           (is (= expected (rt/read-edn (:state-file a))))
           (is (thrown? Exception (rt/update! b assoc :status :attention))))))
    (rt/with-lock b #(rt/update! b assoc-in [:effects "next"] {:status :done}))
    (is (= {"in-flight" {:status :pending} "next" {:status :done}}
           (:effects (rt/read-edn (:state-file a)))))
    (is (false? @(:coordinator-owner a)))
    (is (false? @(:coordinator-owner b)))))

(deftest permission-denied-checks-cannot-repair-or-transfer
  (let [[a b] (pair) argv ["clojure" "-M:denied-check"] opened (atom 0)]
    (with-redefs [flow/prepare! (fn [& _]
                                            (codex/validate-command-result! a argv {:exitCode 1 :stderr "Permission denied" :stdout ""}))
                  gh/status! (fn [& _] nil) gh/comment! (fn [& _] nil)]
      (workflow/run-worker! a "denied")
      (is (= :attention (get-in @(:state a) [:tasks "denied" :status])))
      (is (true? (get-in @(:state a) [:tasks "denied" :error :details :restricted])))
      (is (thrown-with-msg? Exception #"Restricted" (main/dispatch! a "retry" ["denied"]))))
    (with-redefs [codex/open! (fn [& _] (swap! opened inc))]
      (is (thrown? Exception (codex/command! b "transfer" (:state-dir b) argv)))
      (is (zero? @opened)))))
