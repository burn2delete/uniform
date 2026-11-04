(ns advance.coordination-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [simplify.coordination :as coordination]
            [simplify.core-test :as fixture]
            [simplify.github :as gh]
            [simplify.runtime :as rt]))

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
