(ns gravity.self-hosting.sh01-workstream-governance-test
  (:require [clojure.test :refer [deftest is testing]]))

(import '(java.nio.file Files LinkOption Paths))

(System/setProperty "gravity.workstream-governance.library" "true")
(load-file "tools/validate_workstream_governance.clj")
(binding [*command-line-args* nil]
  (load-file "tools/verify_integration_candidate.clj"))
(alias 'governance 'gravity.workstream-governance)
(alias 'fresh 'gravity.integration-candidate-verification)

(def ^:private contract-path "contracts/workstream-governance.json")
(def ^:private ledger-path "contracts/workstream-ledger.json")
(def ^:private fixture-root "tools/fixtures/workstream_governance")
(def ^:private authority
  {"integration_only" true
   "release" false
   "seed_retirement" false
   "self_hosting" false})

(def ^:private oid-a (apply str (repeat 40 "a")))
(def ^:private oid-b (apply str (repeat 40 "b")))
(def ^:private oid-c (apply str (repeat 40 "c")))

(defn- errors-have? [errors diagnostic]
  (boolean (some #(clojure.string/starts-with? % (str diagnostic " ")) errors)))

(defn- transitions [& states]
  (mapv (fn [index state]
          {"state" state
           "at" (format "2026-08-23T%02d:00:00Z" index)
           "actor" "test-author"
           "reason" (str "enter " state)})
        (range)
        states))

(defn- candidate
  ([] (candidate "candidate" "test/family" "draft"))
  ([id family state]
   {"id" id
    "title" "Bounded test candidate"
    "invariant_family" family
    "owner" "test-owner"
    "author" "test-author"
    "state" state
    "dependencies" []
    "architecture_decision" ""
    "base_commit" nil
    "candidate_commit" nil
    "clean_worktree" false
    "owned_paths" []
    "governing_contracts" ["D0"]
    "evidence" {"accepted_fixtures" []
                "rejected_fixtures" []
                "stable_diagnostics" []
                "validation_commands" []}
    "reviews" []
    "residual_host_boundaries" []
    "no_overclaim_authority" authority
    "history" (transitions "draft")
    "disposition" "Test candidate has no integration authority."}))

(defn- ledger [& candidates]
  {"schema_version" 1
   "contract_id" "gravity/workstream-ledger-v1"
   "governance_contract" "contracts/workstream-governance.json"
   "workstreams" (vec candidates)})

(defn- with-state [item state history]
  (assoc item "state" state "history" history))

(defn- eligible []
  (-> (candidate)
      (with-state "integration-eligible"
                  (transitions "draft" "frozen" "review-pending" "accepted"
                               "integration-eligible"))
      (assoc "base_commit" (apply str (repeat 40 "a"))
             "candidate_commit" (apply str (repeat 40 "b"))
             "clean_worktree" true
             "owned_paths" ["bootstrap/clojure"]
             "governing_contracts" ["D0" "D9"]
             "evidence" {"accepted_fixtures" ["fixtures/accepted/example"]
                         "rejected_fixtures" ["fixtures/rejected/example"]
                         "stable_diagnostics" ["WG-TEST"]
                         "validation_commands"
                         [{"command" "clojure -M:test"
                           "exit_code" 0
                           "result" "passed"}]}
             "reviews" [{"reviewer" "independent-reviewer"
                          "kind" "independent"
                          "result" "accepted"}])))

(deftest canonical-contract-ledger-and-positive-fixture-pass
  (is (empty? (governance/validate-documents contract-path ledger-path)))
  (is (nil? (governance/validate-current)))
  (is (empty? (governance/validate-documents
               contract-path
               (str fixture-root "/accepted/minimal-ledger.json")))))

(deftest strict-json-rejects-duplicate-members
  (let [errors (governance/validate-documents
                contract-path
                (str fixture-root "/rejected/duplicate-key-ledger.json"))]
    (is (errors-have? errors "WG001"))
    (is (some #(clojure.string/includes? % "repeats a key") errors))))

(deftest governance-contract-policy-and-language-cannot-be-weakened
  (let [contract (governance/load-json contract-path)
        mutations [(assoc-in contract
                             ["admission_policy" "self_audit_confers_eligibility"]
                             true)
                   (assoc-in contract
                             ["admission_policy" "architecture_decision_after_failures"]
                             3)
                   (assoc-in contract ["lifecycle" "transitions" "rejected"]
                             ["draft"])
                   (assoc-in contract ["diagnostics" "WG012"]
                             "product authority may be inferred")
                   (assoc contract "nonclaims"
                          ["Integrated work establishes release authority."])]]
    (doseq [mutation mutations]
      (is (errors-have? (governance/validate-contract mutation) "WG002")))))

(deftest records-and-nested-records-are-closed
  (doseq [document [(assoc (ledger (candidate)) "unexpected" true)
                    (ledger (assoc (candidate) "unexpected" true))
                    (ledger (assoc-in (candidate) ["evidence" "unexpected"] true))
                    (ledger (assoc-in (eligible)
                                      ["reviews" 0 "unexpected"] true))
                    (ledger (update (candidate) "no_overclaim_authority"
                                    dissoc "release"))]]
    (is (some #(or (clojure.string/starts-with? % "WG001 ")
                   (clojure.string/starts-with? % "WG003 "))
              (governance/validate-ledger document)))))

(deftest lifecycle-active-family-and-dependency-rules-fail-closed
  (testing "illegal lifecycle transition"
    (let [item (with-state (candidate) "accepted"
                           (transitions "draft" "accepted"))]
      (is (errors-have? (governance/validate-ledger (ledger item)) "WG004"))))
  (testing "one active candidate per invariant family"
    (let [first (candidate "first" "same/family" "draft")
          second (with-state (candidate "second" "same/family" "frozen")
                             "frozen" (transitions "draft" "frozen"))]
      (is (errors-have? (governance/validate-ledger (ledger first second))
                        "WG006"))))
  (testing "missing and under-floor dependencies"
    (let [missing (assoc (candidate) "dependencies" ["absent"])
          upstream (with-state (candidate "upstream" "upstream" "frozen")
                               "frozen" (transitions "draft" "frozen"))
          downstream (-> (candidate "downstream" "downstream" "review-pending")
                         (with-state "review-pending"
                                     (transitions "draft" "frozen"
                                                  "review-pending"))
                         (assoc "dependencies" ["upstream"]))]
      (is (errors-have? (governance/validate-ledger (ledger missing)) "WG007"))
      (is (errors-have? (governance/validate-ledger
                         (ledger upstream downstream)) "WG007"))))
  (testing "dependency cycle"
    (let [first (assoc (candidate "first" "one" "draft")
                       "dependencies" ["second"])
          second (assoc (candidate "second" "two" "draft")
                        "dependencies" ["first"])]
      (is (errors-have? (governance/validate-ledger (ledger first second))
                        "WG007")))))

(deftest two-rejections-require-an-architecture-decision
  (let [failed-one (with-state (candidate "failed-one" "same/family" "rejected")
                               "rejected" (transitions "draft" "rejected"))
        failed-two (with-state (candidate "failed-two" "same/family" "rejected")
                               "rejected" (transitions "draft" "rejected"))
        fresh (candidate "fresh" "same/family" "draft")]
    (is (errors-have? (governance/validate-ledger
                       (ledger failed-one failed-two fresh)) "WG008"))
    (is (empty? (governance/validate-ledger
                 (ledger failed-one failed-two
                         (assoc fresh "architecture_decision"
                                "ADR-004 bounds expansion before execution.")))))))

(deftest integration-admission-requires-exact-evidence-and-independent-review
  (is (empty? (governance/validate-ledger (ledger (eligible)))))
  (doseq [item [(assoc (eligible) "base_commit" "short")
                (assoc (eligible) "clean_worktree" false)
                (assoc (eligible) "owned_paths" [])
                (assoc-in (eligible) ["evidence" "rejected_fixtures"] [])]]
    (is (errors-have? (governance/validate-ledger (ledger item)) "WG009")))
  (let [self-only (assoc (eligible) "reviews"
                         [{"reviewer" "test-author"
                           "kind" "self-audit"
                           "result" "accepted"}])]
    (is (errors-have? (governance/validate-ledger (ledger self-only)) "WG011")))
  (let [overclaim (assoc-in (eligible)
                            ["no_overclaim_authority" "release"] true)]
    (is (errors-have? (governance/validate-ledger (ledger overclaim)) "WG012"))))

(deftest fresh-integration-plan-preserves-the-full-suite-and-authority-ceiling
  (let [plan (fresh/verification-plan
              {:base oid-a :commit oid-b :tree oid-c})]
    (is (= fresh/full-suite-command (first (:commands plan))))
    (is (some #{["clojure" "-Srepro" "-Sforce" "-M:test"
                 "--namespace"
                 "gravity.self-hosting.sh01-language-boundary-test"]}
              (:commands plan)))
    (is (= {:mode :fresh :new-export true :resume false
            :repository-cache false}
           (:evidence plan)))
    (is (= {:stage3 :not-substituted :sh07 :not-substituted}
           (:external-proof-lanes plan)))
    (is (= [:clojure-jvm :git :dependency-cache-tool-resolution]
           (:residual-host-boundaries plan)))
    (is (= {:integration-evidence :candidate-only
            :release false :self-hosting false :seed-retirement false
            :safety false :performance false :stage3 false :sh07 false
            :reproducible-environment false}
           (:authority plan)))
    (is (= (str "target/validation/integration-fresh-verification/"
                oid-b "/receipt.edn")
           (fresh/default-receipt oid-b)))))

(deftest fresh-integration-evidence-rejects-cache-and-speculation
  (doseq [evidence [{:mode :speculative :new-export true
                     :resume false :repository-cache false}
                    {:mode :fresh :new-export false
                     :resume true :repository-cache true}
                    {:mode :fresh :new-export true :resume false
                     :repository-cache false :reuse :speculative}]]
    (testing (pr-str evidence)
      (let [error (try
                    (fresh/validate-publishable-evidence! evidence)
                    nil
                    (catch clojure.lang.ExceptionInfo value value))]
        (is (= "C16-SPECULATIVE" (-> error ex-data :code)))))))

(deftest fresh-integration-plan-requires-exact-identities
  (let [error (try
                (fresh/verification-plan
                 {:base oid-a :commit "local-head" :tree oid-c})
                nil
                (catch clojure.lang.ExceptionInfo value value))]
    (is (= "INTEGRATION-FRESH-IDENTITY" (-> error ex-data :code)))))

(deftest fresh-observability-progress-is-bounded-and-non-authoritative
  (let [path (Paths/get "target/validation/fresh-observability-progress.edn"
                        (make-array String 0))
        state (atom {:phase-history []
                     :sample-count 0
                     :rss-high-water-bytes nil
                     :last-rss-bytes nil
                     :last-progress nil
                     :last-phase nil
                     :last-elapsed-ms 0})
        progress {:schema :gravity/fresh-verification-progress-v1
                  :sequence 7
                  :timestamp-ms 123
                  :event :test-var-start
                  :namespace "gravity.bootstrap-test"
                  :test-var "gravity.bootstrap-test/p15-s23-proof"
                  :phase "gravity.bootstrap-test/p15-s23-proof"
                  :active? true}]
    (try
      (Files/deleteIfExists path)
      (spit (str path) (pr-str progress))
      (is (= progress (#'fresh/progress-record path)))
      (let [output (java.io.StringWriter.)
            summary (binding [*out* output]
                      (#'fresh/telemetry-summary
                       state fresh/full-suite-command (System/nanoTime) path))]
        (is (= :gravity/fresh-verification-observability-v1
               (:schema summary)))
        (is (false? (:authoritative? summary)))
        (is (= :fresh-child-progress-file (:progress-source summary)))
        (is (= progress (:last-progress summary)))
        (is (= "gravity.bootstrap-test/p15-s23-proof"
               (:phase (first (:phase-history summary)))))
        (is (re-find #"fresh verification heartbeat: phase=gravity\.bootstrap-test/p15-s23-proof"
                     (str output))))
      (spit (str path) (apply str (repeat 33000 "x")))
      (is (nil? (#'fresh/progress-record path)))
      (finally
        (Files/deleteIfExists path)))))

(deftest fresh-observability-process-sample-never-changes-exit-semantics
  (let [root (Paths/get "." (make-array String 0))
        output (java.io.StringWriter.)
        result (binding [*out* output]
                 (#'fresh/run-process root ["sh" "-c" "exit 0"]))]
    (is (= 0 (:exit-code result)))
    (is (= :gravity/fresh-verification-observability-v1
           (get-in result [:telemetry :schema])))
    (is (false? (get-in result [:telemetry :authoritative?])))
    (is (pos? (get-in result [:telemetry :sample-count])))
    (is (re-find #"fresh verification heartbeat: phase=process"
                 (str output)))))

(deftest fresh-observability-hung-rss-sampler-is-bounded
  (let [started (System/nanoTime)
        observed
        (binding [fresh/*telemetry-sampler-timeout-ms* 50
                  fresh/*rss-process-command*
                  (fn [_] ["sh" "-c" "sleep 5"])]
          (#'fresh/rss-bytes [1]))
        elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))]
    (is (nil? observed))
    (is (< elapsed-ms 1000) elapsed-ms)))
