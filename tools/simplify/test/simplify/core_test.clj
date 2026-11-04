(ns simplify.core-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [simplify.codex :as codex]
            [simplify.github :as gh]
            [simplify.main :as main]
            [simplify.runtime :as rt]
            [simplify.schema :as schema]
            [simplify.workflow :as flow])
  (:import [java.nio.file Files]))

(defn temp-dir [] (str (Files/createTempDirectory "uniform-simplify-test-" (make-array java.nio.file.attribute.FileAttribute 0))))
(defn no-github-network [f]
  (let [command rt/command]
    (with-redefs [rt/command (fn
                              ([ctx cwd argv]
                               (when (= "gh" (first argv)) (throw (AssertionError. (str "Unmocked GitHub command: " argv))))
                               (command ctx cwd argv))
                              ([ctx cwd argv input]
                               (when (= "gh" (first argv)) (throw (AssertionError. (str "Unmocked GitHub command: " argv))))
                               (command ctx cwd argv input)))]
      (f))))
(use-fixtures :each no-github-network)
(defn context []
  (let [dir (temp-dir)
        config {:repo-dir dir :state-dir dir}]
    (merge {:state-dir dir :state-file (str (io/file dir "state.edn")) :control-file (str (io/file dir "control.edn"))
     :log-lock (Object.) :state (atom {:schema 1 :tasks {} :jobs {} :effects {}})
     :config config}
           (rt/coordination dir config))))

(deftest exact-command-capture-does-not-split-credential-redaction
  (let [ctx (context)
        log (str (io/file (:state-dir ctx) "capture.jsonl"))
        secret (str "ghp_" (apply str (repeat 48 "A")))
        output (str (apply str (repeat 4094 "x")) secret "\r\n" "nul\u0000tail")
        error-output "stderr\rwith-cr\r\nnul\u0000tail"
        classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
        java [(str (io/file (System/getProperty "java.home") "bin/java")) "-cp" classpath "clojure.main"]
        baseline (rt/command ctx (:state-dir ctx) (into java ["-e" "nil"]))
        expected-error (str (:err baseline) error-output)
        argv (into java ["-e" (str/replace (str "(print " (pr-str output) ") (flush) "
                                                  "(binding [*out* *err*] (print " (pr-str error-output) ") (flush))")
                                               "\u0000" "\\u0000")])
        result (rt/command (assoc ctx :log-file log) (:state-dir ctx) argv)
        events (mapv #(json/read-str % :key-fn keyword) (str/split-lines (slurp log)))
        streamed (fn [stream]
                   (str/join "\n" (map :line (filter #(and (= "command-output" (:event %))
                                                         (= stream (:stream %))) events))))]
    (is (zero? (:exit result)))
    (is (zero? (:exit baseline)))
    (is (= "" (:out baseline)))
    (is (= output (:out result)))
    (is (= expected-error (:err result)))
    ;; Inspect only output events: command-start already redacts the argv and
    ;; must not make a leaking chunk logger look as though it redacted output.
    (is (= (rt/redact output) (streamed "stdout")))
    (is (= (rt/redact expected-error) (streamed "stderr")))
    (is (not (str/includes? (slurp log) secret)))
    (is (str/includes? (slurp log) "[REDACTED]"))))

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

(deftest review-thread-pagination-never-assumes-the-first-page-is-complete
  (let [ctx (assoc-in (context) [:config :repository] "fixture/repo")]
    (doseq [last-resolved? [true false]]
      (let [seen (atom [])]
        (with-redefs [gh/graphql (fn [_ _ variables]
                                  (swap! seen conj (:after variables))
                                  {:repository {:pullRequest {:reviewThreads
                                                               {:nodes [{:isResolved (if (:after variables) last-resolved? true)}]
                                                                :pageInfo {:hasNextPage (nil? (:after variables)) :endCursor "next"}}}}})]
          (is (= last-resolved? (gh/review-threads-clear? ctx 3)))
          (is (= [nil "next"] @seen)))))
    (with-redefs [gh/graphql (fn [& _] {})]
      (is (thrown-with-msg? Exception #"evidence is unavailable" (gh/review-threads-clear? ctx 3))))
    (with-redefs [gh/graphql (fn [& _] {:repository {:pullRequest {:reviewThreads
                                                                 {:nodes [] :pageInfo {:hasNextPage true :endCursor "same"}}}}})]
      (is (thrown-with-msg? Exception #"cursor did not advance" (gh/review-threads-clear? ctx 3))))))

(deftest required-workflow-completion-is-not-inferred-from-a-partial-rollup
  (let [ctx (assoc-in (context) [:config :repository] "fixture/repo")
        run {:id 1 :workflow_id 10 :event "pull_request" :head_sha "candidate"
             :path ".github/workflows/ci.yml" :status "completed" :conclusion "success"}
        harness (assoc run :id 2 :workflow_id 11 :path ".github/workflows/simplify-harness.yml")]
    (doseq [[runs expected] [[[harness] :pending]
                            [[harness (assoc run :status "queued" :conclusion nil)] :pending]
                            [[harness (assoc run :conclusion "failure")] :failed]
                            [[harness (assoc run :conclusion "skipped")] :failed]
                            [[harness (assoc run :head_sha "old")] :pending]
                            [[harness (assoc run :conclusion "cancelled") (assoc run :id 3)] :passed]]]
      (with-redefs [gh/api (fn [& _] {:total_count (count runs) :workflow_runs runs})]
        (is (= expected (:state (gh/head-workflow-state ctx "candidate"))))))
    (let [seen (atom [])]
      (with-redefs [gh/api (fn [_ endpoint]
                            (swap! seen conj endpoint)
                            {:total_count 2 :workflow_runs [(if (str/ends-with? endpoint "page=1") harness run)]})]
        (is (= :passed (:state (gh/head-workflow-state ctx "candidate"))))
        (is (= 2 (count @seen)))))
    (with-redefs [gh/api (fn [& _] {:total_count 2 :workflow_runs []})]
      (is (thrown-with-msg? Exception #"pagination did not advance" (gh/head-workflow-state ctx "candidate"))))))

(deftest existing-project-membership-is-read-before-add-without-replaying-uncertain-effects
  (let [ctx (context) seen (atom [])]
    (rt/update! ctx assoc :project {:id "project"})
    (with-redefs [gh/graphql (fn [_ query variables]
                              (is (str/starts-with? query "query"))
                              (swap! seen conj (:after variables))
                              {:node {:items {:nodes (if (:after variables) [{:id "existing-item" :content {:id "issue"}}] [])
                                              :pageInfo {:hasNextPage (nil? (:after variables)) :endCursor "next"}}}})]
      (is (= "existing-item" (gh/item! ctx {:node_id "issue"})))
      (is (= [nil "next"] @seen))
      (is (= "existing-item" (gh/item! ctx {:node_id "issue"})))
      (is (= [nil "next"] @seen)))
    (rt/update! ctx assoc-in [:effects "project-item/project/unknown"] {:status :uncertain :message "Retain original ambiguity"})
    (with-redefs [gh/graphql (fn [& _] (throw (AssertionError. "Uncertain effect must stop before even the membership read")))]
      (is (thrown-with-msg? Exception #"needs reconciliation" (gh/item! ctx {:node_id "unknown"}))))
    (with-redefs [gh/graphql (fn [& _] {})]
      (is (thrown-with-msg? Exception #"evidence is unavailable" (gh/existing-item ctx "project" "missing"))))
    (with-redefs [gh/graphql (fn [& _] {:node {:items {:nodes [] :pageInfo {:hasNextPage true :endCursor "same"}}}})]
      (is (thrown-with-msg? Exception #"cursor did not advance" (gh/existing-item ctx "project" "missing"))))))

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

(deftest legacy-applied-receipts-remain-valid
  (let [ctx (context) receipt (str (io/file (:state-dir ctx) "inspection.edn"))]
    (rt/update! ctx assoc-in [:effects "issue"] {:status :uncertain :message "Original response lost"})
    (rt/write-edn! receipt {:result {:id 7} :evidence "Inspected exact remote issue"})
    (main/reconcile! ctx "issue" receipt)
    (is (= {:id 7} (rt/effect! ctx "issue" (fn [] (throw (ex-info "Must not replay" {}))))))
    (is (= :uncertain (get-in @(:state ctx) [:effects "issue" :previous :status])))))

(deftest confirmed-unapplied-mutations-can-be-repaired-with-original-history-retained
  (let [ctx (context) calls (atom 0) receipt (str (io/file (:state-dir ctx) "inspection.edn"))]
    (is (thrown? Exception (rt/effect! ctx "issue" #(do (swap! calls inc) (throw (ex-info "HTTP 422: body rejected" {}))))))
    (is (thrown? Exception (rt/effect! ctx "issue" #(swap! calls inc))))
    (rt/write-edn! receipt {:outcome :not-applied :result nil :evidence "Inspected HTTP 422 validation response and repository issue markers; no issue was created."})
    (main/reconcile! ctx "issue" receipt)
    (is (= :not-applied (get-in @(:state ctx) [:effects "issue" :status])))
    (is (= {:id 7} (rt/effect! ctx "issue" #(do (swap! calls inc) {:id 7}))))
    (is (= {:id 7} (rt/effect! ctx "issue" #(swap! calls inc))))
    (is (= 2 @calls))
    (is (= :not-applied (get-in @(:state ctx) [:effects "issue" :previous :status])))
    (is (= :uncertain (get-in @(:state ctx) [:effects "issue" :previous :previous :status])))))

(deftest unapplied-reconciliation-cannot-reclassify-a-restriction-or-an-existing-result
  (let [ctx (context) receipt (str (io/file (:state-dir ctx) "inspection.edn"))]
    (rt/update! ctx assoc :effects {"restricted" {:status :restricted} "unknown" {:status :uncertain}})
    (rt/write-edn! receipt {:outcome :not-applied :result nil :evidence "Inspected"})
    (is (thrown? Exception (main/reconcile! ctx "restricted" receipt)))
    (is (thrown? Exception (rt/effect! ctx "restricted" (fn [] :must-not-run))))
    (rt/write-edn! receipt {:outcome :not-applied :result {:id 1} :evidence "An issue exists"})
    (is (thrown? Exception (main/reconcile! ctx "unknown" receipt)))
    (is (= :uncertain (get-in @(:state ctx) [:effects "unknown" :status])))))

(deftest confirmed-unapplied-tracking-write-does-not-block-a-new-observed-transition
  (let [ctx (context) receipt (str (io/file (:state-dir ctx) "inspection.edn")) calls (atom 0)]
    (is (thrown? Exception (rt/tracking-effect! ctx "status" (fn [] (throw (ex-info "Connection closed" {}))))))
    (is (thrown? Exception (rt/tracking-effect! ctx "status" #(swap! calls inc))))
    (let [id (get-in @(:state ctx) [:tracking-intents "status"])]
      (rt/write-edn! receipt {:outcome :not-applied :result nil :evidence "Inspected remote field and operation response; transition did not apply."})
      (main/reconcile! ctx id receipt)
      (is (= 1 (rt/tracking-effect! ctx "status" #(swap! calls inc))))
      (is (= :not-applied (get-in @(:state ctx) [:effects id :status]))))))

(deftest redacts-known-secrets
  (is (= {:apiKey "[REDACTED]" :message "Bearer [REDACTED]" :value "[REDACTED]"}
         (rt/redact {:apiKey "private" :message "Bearer abc.def.ghi" :value "ghp_somecredential"}))))

(deftest review-feedback-binds-latest-opinion-to-current-head
  (let [ctx (assoc-in (context) [:config :repository] "fixture/repo")
        reviews (atom [{:id 1 :user {:id 10} :state "CHANGES_REQUESTED" :commit_id "head"}
                       {:id 2 :user {:id 10} :state "APPROVED" :commit_id "head"}
                       {:id 3 :user {:id 20} :state "CHANGES_REQUESTED" :commit_id "head"}
                       {:id 4 :user {:id 20} :state "COMMENTED" :commit_id "head"}
                       {:id 5 :user {:id 30} :state "CHANGES_REQUESTED" :commit_id "old"}])]
    (with-redefs [gh/pages (fn [_ endpoint]
                            (if (str/ends-with? endpoint "/reviews") @reviews
                                [{:id 30 :pull_request_review_id 3 :body "Current actionable finding"}
                                 {:id 50 :pull_request_review_id 5 :body "Old finding"}]))]
      (let [feedback (gh/requested-review-feedback ctx "head" 3)]
        (is (= [3] (mapv :id (:github-reviews feedback))))
        (is (= [30] (mapv :id (:inline-comments feedback)))))
      (is (nil? (gh/requested-review-feedback ctx "new-head" 3)))
      (swap! reviews conj {:id 6 :user {:id 20} :state "DISMISSED" :commit_id "head"})
      (is (nil? (gh/requested-review-feedback ctx "head" 3))))))

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
                              (cond
                                (str/includes? query "items(first") {:node {:items {:nodes [] :pageInfo {:hasNextPage false}}}}
                                (str/includes? query "addProjectV2ItemById")
                                {:addProjectV2ItemById {:item {:id "item"}}}
                                :else
                                {:updateProjectV2ItemFieldValue {:projectV2Item {:id "item"}}}))]
      (gh/status! ctx issue "Ready")
      (gh/status! ctx issue "Ready"))
    (is (= 1 (count (filter #(str/includes? % "addProjectV2ItemById") @calls))))
    (is (= 2 (count (filter #(str/includes? % "updateProjectV2ItemFieldValue") @calls))))
    (is (= 3 (count (:effects @(:state ctx)))))
    (is (every? #(= :done (:status %)) (vals (:effects @(:state ctx)))))))

(deftest streamed-command-capture-preserves-split-utf8-bytes
  (let [java (str (io/file (System/getProperty "java.home") "bin/java"))
        classpath (str/join java.io.File/pathSeparator
                            (map #(.getAbsolutePath (io/file %))
                                 (str/split (System/getProperty "java.class.path") (re-pattern java.io.File/pathSeparator))))
        ctx (assoc-in (context) [:config :codex-command]
                      [java "-cp" classpath "clojure.main" "-m" "simplify.fake-server" "stream-utf8"])
        result (codex/command! ctx "utf8-fixture" (:state-dir ctx) ["clojure" "-M:test"])]
    (is (zero? (:exitCode result)))
    (is (= "path-\u00e9.clj\u0000" (:stdout result)))
    (is (= "path-\u00e9.clj\u0000" (:stderr result)))))

(deftest main-reconciliation-requires-observed-main-without-requiring-it-to-equal-the-merge
  (let [ctx (context) sha #(apply str (repeat 40 %))
        intent {:id "merge/fixture" :base (sha "a") :candidate (sha "b") :tree (sha "c") :method :merge}
        actual {:commit (sha "d") :tree (sha "c") :parents [(sha "a") (sha "b")]}
        native {:status :uncertain :message "Original result unknown"}]
    (rt/update! ctx assoc-in [:effects (:id intent)] native)
    (rt/try-with-main-lock ctx #(rt/begin-main-merge! ctx intent))
    (doseq [main [nil "invalid" (sha "z")]]
      (is (thrown-with-msg? Exception #"Inspected main result differs"
                           (rt/reconcile-main-merge! ctx (:id intent) {:outcome :merged :evidence "Fixture observation"
                                                                     :actual (assoc actual :main main)})))
      (is (:pending (rt/main-merge-state ctx (:id intent)))))
    (rt/reconcile-main-merge! ctx (:id intent) {:outcome :merged :evidence "Fixture observed actual merge on advanced main"
                                              :actual (assoc actual :main (sha "e"))})
    (is (nil? (:pending (rt/main-merge-state ctx (:id intent)))))
    (is (= (sha "e") (get-in (rt/main-merge-state ctx (:id intent)) [:inspection :inspection :actual :main])))
    (is (= native (get-in @(:state ctx) [:effects (:id intent)])))))

(deftest denied-project-status-cannot-be-reissued-or-reported-remotely
  (let [ctx (context) calls (atom 0) issue {:node_id "issue"}]
    (rt/update! ctx assoc :project {:id "project" :workflow {:id "status" :options [{:id "ready" :name "Ready"}]}})
    (with-redefs [gh/graphql (fn [_ query _]
                              (swap! calls inc)
                              (cond
                                (str/includes? query "items(first") {:node {:items {:nodes [] :pageInfo {:hasNextPage false}}}}
                                (str/includes? query "addProjectV2ItemById")
                                {:addProjectV2ItemById {:item {:id "item"}}}
                                :else
                                (throw (ex-info "Forbidden" {:restricted true}))))]
      (try (gh/status! ctx issue "Ready")
           (catch Exception e (flow/attention! ctx "blocked" e)))
      (is (thrown? Exception (gh/status! ctx issue "Ready")))
      (is (= 3 @calls))
      (is (some #(= :restricted (:status %)) (vals (:effects @(:state ctx)))))
      (with-redefs [rt/with-lock (fn [_ f] (f))]
        (is (thrown? Exception (main/dispatch! ctx "retry" ["blocked"])))))))
