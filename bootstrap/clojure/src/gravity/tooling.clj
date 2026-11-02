(ns gravity.tooling
  "Native coverage data and static source evidence; no tooling admission."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {coverage-record
  {:arglists
   ([document-ids-var
     diagnostic-ids-var
     present-var
     task-statuses-var
     artifact]),
   :admission :none},
  check-evidence {:arglists ([invoke request]), :admission :none},
  execute-evidence
  {:arglists ([invoke original-request]), :admission :none},
  verify-evidence
  {:arglists ([invoke original-request candidate]), :admission :none}},
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by [gravity.bootstrap],
  :forbids
  [gravity.bootstrap gravity.cli gravity.build gravity.release]},
 :legacy-complete-and-proof-labels :supplied-compatibility-data,
 :performs-tool-services? false,
 :source-scope :static-non-authority-meta-jvm,
 :governing-contracts
 [D1
  D2
  D3
  D8
  D9
  L2
  L3
  T1
  T2
  T3
  T4
  T5
  T6
  T7
  T8
  T9
  T10
  T11
  T12
  T13
  C15
  BOOT8
  TEST2],
 :preserves
 {:native-coverage [:var-read-position :argument-order :legacy-record-and-task-data]
  :source-evidence [:original-request :source-provenance :deterministic-identity
                    :negative-status :incomplete-status :blocked-verifier-gate]},
 :unsupported
 [:formatter
  :linter
  :repl
  :lsp
  :debugger
  :dev-server
  :registry-ui
  :profiler
  :audit-explorer
  :ai-execution
  :provider-grants
  :public-cli
  :source-core-admission
  :callback-authentication
  :native-backend-execution
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:caller-supplied-native-report-data
  :document-and-diagnostic-vars
  :trusted-presence-and-task-status-vars
  :static-w5-original-request
  :static-w5-candidate
  :trusted-source-invocation-operation],
 :namespace gravity.tooling,
 :diagnostic-ownership
 {:native-builder :none,
  :source-rules :gravity.self-hosting.w5-tooling-conformance-verifier,
  :compiler-and-runtime-failures :propagated-unchanged,
  :bootstrap-admission-and-publication :existing-owner},
 :artifact-outputs
 [:legacy-coverage-data
  :source-owned-static-evidence-result
  :fresh-replay-verification
  :unchanged-source-rejection],
 :issues-certificates? false,
 :contract-boundary :native-coverage-data-and-static-source-evidence}
)

(defn
 coverage-record
 [document-ids-var
  diagnostic-ids-var
  present-var
  task-statuses-var
  artifact]
 (let
  [documents
   (:document-contracts artifact)
   accepted-docs
   (set (map :document (:accepted-tooling-fixtures artifact)))
   rejected-docs
   (set (map :document (:rejected-tooling-fixtures artifact)))
   conformance-docs
   (set (map :document (:tooling-conformance-evidence artifact)))
   diagnostics
   (set
    (map
     :diagnostic
     (get-in artifact [:tooling-diagnostic-stream :diagnostics])))]
  {:accepted-fixtures-covered?
   (= (set (deref document-ids-var)) accepted-docs),
   :formatter-linter-docs-covered?
   (and
    (true? (get-in artifact [:formatter-fixture :reader-round-trip]))
    (set/subset?
     #{:capabilities :types :effects :profile :artifacts}
     (set
      (get-in artifact [:linter-diagnostic-report :compiler-facts])))
    (true?
     (get-in artifact [:documentation-artifact :structured-docs]))),
   :lsp-and-debugger-covered?
   (and
    (true?
     (get-in artifact [:lsp-capability-matrix :diagnostics-match-cli]))
    (=
     :passed
     (get-in
      artifact
      [:debugger-trace :source-map-validation :status]))),
   :task-statuses ((deref task-statuses-var)),
   :document-coverage-complete?
   (= (set (deref document-ids-var)) (set (keys documents))),
   :repl-and-incremental-dev-covered?
   (and
    (= :complete (:status (:repl-session-artifact artifact)))
    (true?
     (get-in artifact [:repl-session-artifact :transcript-redacted]))
    (= :complete (:status (:dev-server-session artifact)))),
   :cli-and-project-workflow-covered?
   (and
    (= :complete (:status (:cli-command-set artifact)))
    (true? (get-in artifact [:cli-command-set :json-output]))
    (contains?
     (set
      (get-in
       artifact
       [:cli-command-set :capability-prompts :shown-denials]))
     :shell/exec)),
   :rejected-fixtures-covered?
   (= (set (deref document-ids-var)) rejected-docs),
   :status :complete,
   :ai-assisted-tooling-covered?
   (and
    (true?
     (get-in artifact [:ai-tooling-record :patch-artifact :validated]))
    (=
     :approved
     (get-in
      artifact
      [:ai-tooling-record :human-review-record :decision]))
    (false?
     (get-in
      artifact
      [:ai-tooling-record :replay-trace :hidden-tool-use-detected]))),
   :diagnostics-covered?
   (set/subset? (set (deref diagnostic-ids-var)) diagnostics),
   :conformance-evidence-covered?
   (= (set (deref document-ids-var)) conformance-docs),
   :dev-registry-and-inspectors-covered?
   (boolean
    (and
     (true?
      (get-in
       artifact
       [:registry-ux-record :update-diff :capability-diff-visible]))
     ((deref present-var)
      (get-in artifact [:ir-inspector-bundle :source-span-maps]))
     ((deref present-var)
      (get-in
       artifact
       [:profiler-report :check-elision-report :evidence]))
     ((deref present-var)
      (get-in artifact [:safety-audit-report :proof-index]))))}))

(defn check-evidence
  [invoke request]
  (invoke 'w5-tooling-conformance-verify [request]))

(defn verify-evidence
  [invoke original-request candidate]
  (invoke 'w5-tooling-conformance-verify-result [original-request candidate]))

(defn execute-evidence
  [invoke original-request]
  (let [candidate (check-evidence invoke original-request)
        verification (verify-evidence invoke original-request candidate)]
    (if (= :passed (:status verification))
      {:result candidate :verification verification}
      verification)))

(doseq [operation [#'coverage-record #'check-evidence #'execute-evidence #'verify-evidence]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
