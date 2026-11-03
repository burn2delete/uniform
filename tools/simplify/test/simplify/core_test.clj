(ns simplify.core-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [simplify.codex :as codex]
            [simplify.main :as main]
            [simplify.runtime :as rt]
            [simplify.schema :as schema]
            [simplify.workflow :as flow])
  (:import [java.nio.file Files]))

(defn temp-dir [] (str (Files/createTempDirectory "uniform-simplify-test-" (make-array java.nio.file.attribute.FileAttribute 0))))
(defn context []
  (let [dir (temp-dir)]
    {:state-dir dir :state-file (str (io/file dir "state.edn")) :control-file (str (io/file dir "control.edn"))
     :log-lock (Object.) :state (atom {:schema 1 :tasks {} :jobs {} :effects {}})
     :config {:repo-dir dir}}))

(deftest schema-rejects-unusable-results
  (is (= {:coverage ["tools"] :summary "No candidates" :findings []}
         (schema/validate! schema/audit {:coverage ["tools"] :summary "No candidates" :findings []})))
  (doseq [bad [{:coverage [] :summary "No candidates" :findings []}
               {:coverage ["tools"] :summary " " :findings []}
               {:coverage ["tools"] :summary "Done" :findings [] :unexpected true}
               {:coverage ["tools"] :summary "Done" :findings {}}]]
    (is (thrown? Exception (schema/validate! schema/audit bad)))))

(deftest ownership-and-dependency-scheduling
  (is (flow/overlap? ["tools/ci"] ["tools/ci/run.clj"]))
  (is (not (flow/overlap? ["tools/ci"] ["tools/compiler.clj"])))
  (doseq [path ["../escape" "/outside" ".git/config" "x/../../outside" ""]]
    (is (thrown? Exception (flow/safe-path! path))))
  (is (thrown? Exception (flow/validate-findings! [{:key "a" :paths ["tools"] :dependencies ["b"]}
                                                  {:key "b" :paths ["src"] :dependencies ["a"]}] {})))
  (is (thrown? Exception (flow/validate-findings! [{:key "a" :paths ["tools"] :dependencies ["missing"]}] {})))
  (let [ctx (context)]
    (rt/update! ctx assoc :tasks {"a" {:key "a" :status :ready :paths ["tools"] :dependencies ["b"]}
                                 "b" {:key "b" :status :ci :paths ["src"]}})
    (is (not (flow/eligible? ctx (flow/task ctx "a") [])))
    (flow/task! ctx "b" :status :done)
    (is (flow/eligible? ctx (flow/task ctx "a") []))
    (is (not (flow/eligible? ctx (flow/task ctx "a") [{:paths ["tools/a.clj"]}])))))

(deftest uncertain-mutations-are-not-replayed
  (let [ctx (context) calls (atom 0)]
    (is (= {:id 1} (rt/effect! ctx "one" #(do (swap! calls inc) {:id 1}))))
    (is (= {:id 1} (rt/effect! ctx "one" #(swap! calls inc))))
    (is (= 1 @calls))
    (is (thrown? Exception (rt/effect! ctx "ambiguous" #(throw (ex-info "connection lost" {})))))
    (is (thrown? Exception (rt/effect! ctx "ambiguous" #(swap! calls inc))))
    (is (= 1 @calls))
    (is (= :uncertain (get-in (rt/read-edn (:state-file ctx)) [:effects "ambiguous" :status])))))

(deftest redacts-known-secrets
  (is (= {:apiKey "[REDACTED]" :message "Bearer [REDACTED]" :value "[REDACTED]"}
         (rt/redact {:apiKey "private" :message "Bearer abc.def.ghi" :value "ghp_somecredential"}))))

(deftest checks-are-observed-not-inferred
  (is (= :missing (flow/check-state [])))
  (is (= :pending (flow/check-state [{:status "IN_PROGRESS"}])))
  (is (= :failed (flow/check-state [{:status "COMPLETED" :conclusion "CANCELLED"}])))
  (is (= :failed (flow/check-state [{:state "ERROR"}])))
  (is (= :missing (flow/check-state [{:status "COMPLETED" :conclusion "SKIPPED"}
                                    {:status "COMPLETED" :conclusion "NEUTRAL"}])))
  (is (= :missing (flow/check-state [{:status "COMPLETED" :conclusion "SKIPPED"}])))
  (is (= :passed (flow/check-state [{:status "COMPLETED" :conclusion "SUCCESS"}
                                   {:status "COMPLETED" :conclusion "SKIPPED"} {:state "SUCCESS"}]))))

(deftest protocol-fixture-completion-and-denials
  (let [java (str (io/file (System/getProperty "java.home") "bin/java"))
        classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
        command [java "-cp" classpath "clojure.main" "-m" "simplify.fake-server"]]
    (doseq [mode ["ok" "malformed" "failed" "interrupted" "approval"]]
      (let [ctx (assoc-in (context) [:config :codex-command] (conj command mode))]
        (testing mode
          (if (= "ok" mode)
            (do
              (is (= "approved" (:verdict (codex/job! ctx "fixture" (:state-dir ctx) :read "fixture" "fixture" schema/review))))
              (is (= :completed (get-in @(:state ctx) [:jobs "fixture" :status]))))
            (do
              (is (thrown? Exception (codex/job! ctx "fixture" (:state-dir ctx) :read "fixture" "fixture" schema/review)))
              (is (= :attention (get-in @(:state ctx) [:jobs "fixture" :status]))))))))))

(deftest restricted-findings-cannot-be-retried
  (let [ctx (context)]
    (rt/update! ctx assoc-in [:tasks "blocked"] {:status :attention :error {:details {:restricted true}}})
    (with-redefs [rt/with-lock (fn [_ f] (f))]
      (is (thrown? Exception (main/dispatch! ctx "retry" ["blocked"]))))))

(deftest checks-use-agentless-sandbox-protocol
  (let [classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
        command [(str (io/file (System/getProperty "java.home") "bin/java")) "-cp" classpath "clojure.main" "-m" "simplify.fake-server"]
        ctx (assoc-in (assoc (context) :processes (atom #{})) [:config :codex-command] (conj command "ok"))]
    (is (= "fixture command" (:stdout (codex/command! ctx "check-fixture" (:state-dir ctx) ["clojure" "-M:test"]))))
    (is (empty? (:jobs @(:state ctx))))
    (is (empty? @(:processes ctx)))
    (is (thrown? Exception (codex/command! (assoc-in ctx [:config :codex-command] (conj command "failed"))
                                          "check-failure" (:state-dir ctx) ["clojure" "-M:test"])))))
