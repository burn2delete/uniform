(ns simplify.core-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [simplify.codex :as codex]
            [simplify.github :as gh]
            [simplify.main :as main]
            [simplify.runtime :as rt]
            [simplify.schema :as schema]
            [simplify.workflow :as flow])
  (:import [java.nio.file Files]))

(defn temp-dir [] (str (Files/createTempDirectory "uniform-simplify-test-" (make-array java.nio.file.attribute.FileAttribute 0))))
(defn context []
  (let [dir (temp-dir)
        config {:repo-dir dir :state-dir dir}]
    (merge {:state-dir dir :state-file (str (io/file dir "state.edn")) :control-file (str (io/file dir "control.edn"))
     :log-lock (Object.) :state (atom {:schema 1 :tasks {} :jobs {} :effects {}})
     :config config}
           (rt/coordination dir config))))

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

(deftest recovery-keeps-verified-merges-out-of-authoring
  (let [ctx (context)
        cases [[{:merge-verification {:actual {:commit "merged"}} :merged "merged"} :finalizing]
               [{:merged "merged"} :ci]
               [{:resume-status :ci} :ci]
               [{:resume-status :merge} :ci]
               [{:resume-status :implementing} :repair]]]
    (doseq [[extra phase] cases]
      (rt/update! ctx assoc-in [:tasks "recovery"] (merge {:key "recovery" :status :attention} extra))
      (main/dispatch! ctx "retry" ["recovery"])
      (is (= phase (:status (flow/task ctx "recovery")))))
    (doseq [status [:pending :uncertain :restricted]]
      (rt/update! ctx assoc-in [:tasks "recovery"] {:key "recovery" :status :attention :merged "merged"
                                                    :error {:details {:effect "close/recovery"}}})
      (rt/update! ctx assoc-in [:effects "close/recovery"] {:status status})
      (is (thrown-with-msg? Exception #"reconciliation before retry" (main/dispatch! ctx "retry" ["recovery"])))
      (is (= :attention (:status (flow/task ctx "recovery")))))))

(deftest retained-uncertain-effects-do-not-trigger-attention-writes
  (let [ctx (context) calls (atom 0)]
    (doseq [status [:pending :uncertain :restricted]]
      (rt/update! ctx assoc-in [:effects "close/finding"] {:status status})
      (rt/update! ctx assoc-in [:tasks "finding"] {:key "finding" :status :finalizing})
      (with-redefs [gh/status! (fn [& _] (swap! calls inc))
                    gh/comment! (fn [& _] (swap! calls inc))]
        (try (rt/effect! ctx "close/finding" #(swap! calls inc))
             (catch Exception e (flow/attention! ctx "finding" e))))
      (is (= :attention (:status (flow/task ctx "finding"))))
      (is (= :finalizing (:resume-status (flow/task ctx "finding")))))
    (is (zero? @calls))))

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

(deftest denied-github-actions-are-persistent-and-not-retried
  (let [ctx (context) calls (atom 0)]
    (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 1 :out "" :err "Forbidden (HTTP 403)"})]
      (is (thrown? Exception (rt/effect! ctx "denied-status" #(gh/api ctx "POST" "graphql" {:query "write"}))))
      (is (= :restricted (get-in @(:state ctx) [:effects "denied-status" :status])))
      (is (thrown? Exception (rt/effect! ctx "denied-status" #(swap! calls inc))))
      ;; A new body/intent cannot route around the recorded endpoint denial.
      (is (thrown? Exception (gh/api ctx "POST" "graphql" {:query "different write"})))
      (is (= 1 @calls)))
    (with-redefs [gh/status! (fn [& _] (swap! calls inc)) gh/comment! (fn [& _] (swap! calls inc))]
      (flow/attention! ctx "blocked" (ex-info "Denied" {:restricted true}))
      (is (= :attention (get-in @(:state ctx) [:tasks "blocked" :status])))
      (is (= 1 @calls)))
    (with-redefs [rt/with-lock (fn [_ f] (f))]
      (is (thrown? Exception (main/dispatch! ctx "retry" ["blocked"]))))))

(deftest graphql-authorization-errors-cannot-use-http-success-to-escape-denial
  (let [ctx (context) calls (atom 0)]
    (with-redefs [rt/command (fn [& _]
                              (swap! calls inc)
                              {:exit 0 :err "" :out "{\"errors\":[{\"type\":\"FORBIDDEN\",\"message\":\"Resource not accessible by integration\"}]}"})]
      (is (thrown? Exception (gh/graphql ctx "mutation first" {})))
      (is (seq (:restricted-actions (rt/read-edn (:state-file ctx)))))
      (is (thrown? Exception (gh/graphql ctx "mutation changed" {})))
      (is (= 1 @calls)))))

(deftest controller-push-and-merge-denials-block-successor-intents
  (doseq [[first-command successor error]
          [[["git" "push" "origin" "first:refs/heads/codex/task"]
            ["git" "push" "other-alias" "successor:refs/heads/codex/other-task"]
            "remote: Permission to fixture/repo denied to fixture-user."]
           [["gh" "pr" "merge" "1" "--match-head-commit" "first"]
            ["gh" "pr" "merge" "2" "--match-head-commit" "successor"]
            "GraphQL: Resource not accessible by integration (HTTP 403)"]]]
    (let [ctx (assoc-in (context) [:config :repository] "fixture/repo") calls (atom 0)]
      (with-redefs [rt/command (fn [& _] (swap! calls inc) {:exit 1 :out "" :err error})]
        (try (rt/effect! ctx "first-intent" #(rt/run! ctx (:state-dir ctx) first-command))
             (catch Exception e (flow/attention! ctx "blocked" e)))
        (is (= :restricted (get-in @(:state ctx) [:effects "first-intent" :status])))
        (is (thrown? Exception (rt/effect! ctx "new-candidate-intent" #(rt/run! ctx (:state-dir ctx) successor))))
        (is (= :restricted (get-in @(:state ctx) [:effects "new-candidate-intent" :status])))
        (is (seq (:restricted-commands (rt/read-edn (:state-file ctx)))))
        (is (= 1 @calls))
        (with-redefs [rt/with-lock (fn [_ f] (f))]
          (is (thrown? Exception (main/dispatch! ctx "retry" ["blocked"]))))))))

(deftest ordinary-source-and-stale-head-failures-are-not-authorization-denials
  (doseq [error ["! [rejected] codex/task -> codex/task (non-fast-forward)"
                 "Pull request is not mergeable: the head commit changed"]]
    (let [ctx (context)]
      (with-redefs [rt/command (fn [& _] {:exit 1 :out "" :err error})]
        (try (rt/run! ctx (:state-dir ctx) ["git" "push" "origin" "candidate:refs/heads/codex/task"])
             (is false "Expected the ordinary command failure")
             (catch Exception e (is (false? (:restricted (ex-data e)))))))
      (is (empty? (:restricted-commands @(:state ctx)))))))

(deftest tracking-writes-have-durable-intents-and-stop-on-uncertainty
  (let [ctx (context) calls (atom 0)]
    (is (= 1 (rt/tracking-effect! ctx "status/item" #(swap! calls inc))))
    (is (= 2 (rt/tracking-effect! ctx "status/item" #(swap! calls inc))))
    (is (= 2 (:tracking-sequence @(:state ctx))))
    (is (thrown? Exception (rt/tracking-effect! ctx "status/item" #(throw (ex-info "Connection lost" {})))))
    (is (thrown? Exception (rt/tracking-effect! ctx "status/item" #(swap! calls inc))))
    (is (= 2 @calls))
    (let [id (get-in @(:state ctx) [:tracking-intents "status/item"])]
      (is (= :uncertain (get-in (rt/read-edn (:state-file ctx)) [:effects id :status])))
      (rt/update! ctx assoc-in [:effects id] {:status :done :result "Inspected external result"})
      (is (= 3 (rt/tracking-effect! ctx "status/item" #(swap! calls inc)))))))

(deftest same-title-project-needs-recorded-or-repository-identity
  (let [ctx (assoc-in (context) [:config :repository] "fixture/repo")
        project {:id "project" :title "Uniform Simplify"}]
    (with-redefs [gh/owner-info (fn [_] {:id "owner"})
                  gh/projects (fn [& _] [project])
                  gh/fields (fn [& _] (throw (AssertionError. "Unverified Project must not reach field mutations")))
                  gh/graphql (fn [_ query _]
                               (is (str/starts-with? query "query"))
                               {:node {:id "project" :readme "Unrelated project"
                                       :repositories {:nodes [{:nameWithOwner "fixture/repo"}]}}})]
      (is (thrown? Exception (gh/verify-project! ctx project)))
      (is (thrown? Exception (gh/setup! (assoc-in ctx [:config :project-title] "Uniform Simplify")))))
    (with-redefs [gh/graphql (fn [& _] {:node {:id "project" :readme (gh/project-marker ctx)
                                               :repositories {:nodes [{:nameWithOwner "other/repo"}]}}})]
      (is (thrown? Exception (gh/verify-project! ctx project))))
    (with-redefs [gh/graphql (fn [& _] {:node {:id "project" :readme (gh/project-marker ctx)
                                               :repositories {:nodes [{:nameWithOwner "fixture/repo"}]}}})]
      (is (nil? (gh/verify-project! ctx project))))
    (rt/update! ctx assoc :project project)
    (with-redefs [gh/graphql (fn [& _] (throw (ex-info "Recorded Project must not be replaced or probed for adoption" {})))]
      (is (nil? (gh/verify-project! ctx project))))))

(deftest project-status-and-item-writes-are-recorded
  (let [ctx (context)
        issue {:node_id "issue"}
        calls (atom [])]
    (rt/update! ctx assoc :project {:id "project" :workflow {:id "status" :options [{:id "ready" :name "Ready"}]}})
    (with-redefs [gh/graphql (fn [_ query _]
                              (swap! calls conj query)
                              (if (str/includes? query "addProjectV2ItemById")
                                {:addProjectV2ItemById {:item {:id "item"}}}
                                {:updateProjectV2ItemFieldValue {:projectV2Item {:id "item"}}}))]
      (gh/status! ctx issue "Ready")
      (gh/status! ctx issue "Ready"))
    (is (= 1 (count (filter #(str/includes? % "addProjectV2ItemById") @calls))))
    (is (= 2 (count (filter #(str/includes? % "updateProjectV2ItemFieldValue") @calls))))
    (is (= 3 (count (:effects @(:state ctx)))))
    (is (every? #(= :done (:status %)) (vals (:effects @(:state ctx)))))))

(deftest denied-project-status-cannot-be-reissued-or-reported-remotely
  (let [ctx (context) calls (atom 0) issue {:node_id "issue"}]
    (rt/update! ctx assoc :project {:id "project" :workflow {:id "status" :options [{:id "ready" :name "Ready"}]}})
    (with-redefs [gh/graphql (fn [_ query _]
                              (swap! calls inc)
                              (if (str/includes? query "addProjectV2ItemById")
                                {:addProjectV2ItemById {:item {:id "item"}}}
                                (throw (ex-info "Forbidden" {:restricted true}))))]
      (try (gh/status! ctx issue "Ready")
           (catch Exception e (flow/attention! ctx "blocked" e)))
      (is (thrown? Exception (gh/status! ctx issue "Ready")))
      (is (= 2 @calls))
      (is (some #(= :restricted (:status %)) (vals (:effects @(:state ctx)))))
      (with-redefs [rt/with-lock (fn [_ f] (f))]
        (is (thrown? Exception (main/dispatch! ctx "retry" ["blocked"])))))))
