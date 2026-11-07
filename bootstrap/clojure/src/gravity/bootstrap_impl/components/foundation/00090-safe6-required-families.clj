

(def safe6-required-families
  [:unsafe-island :safe-wrapper :operation-inventory :review-status
   :invariant-proof :generated-provenance :policy-decision
   :dependency-summary :release-audit])

(defn safe6-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe6-unsafe-island-records checker-state))
                  (conj :unsafe-island)
                  (seq (:safe6-safe-wrapper-records checker-state))
                  (conj :safe-wrapper)
                  (seq (:safe6-operation-inventories checker-state))
                  (conj :operation-inventory)
                  (seq (:safe6-review-status-records checker-state))
                  (conj :review-status)
                  (seq (:safe6-invariant-proof-links checker-state))
                  (conj :invariant-proof)
                  (seq (:safe6-generated-unsafe-provenance checker-state))
                  (conj :generated-provenance)
                  (seq (:safe6-policy-decision-records checker-state))
                  (conj :policy-decision)
                  (seq (:safe6-dependency-unsafe-summaries checker-state))
                  (conj :dependency-summary)
                  (seq (:safe6-release-audit-reports checker-state))
                  (conj :release-audit))
        missing (vec (remove covered safe6-required-families))]
    {:required-families safe6-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE6
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def ^:private safe6-source-audit-operations
  {:arg-value #'dispatch-arg-value :record! #'record-checker!})

(defn record-safe6-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (gravity.safety/record-native-unsafe-audit-call!
   checker ctx node operator args effects capabilities return-type spec safe6-source-audit-operations))

(def safe7-required-families
  [:safe7-foreign-declaration :safe7-abi-record :safe7-type-mapping
   :safe7-ownership-lifetime :safe7-safe-wrapper :safe7-error-translation
   :safe7-callback-safety :safe7-host-bridge :safe7-generated-binding])

(def safe8-required-families
  [:safe8-concurrency-graph :safe8-task-capture :safe8-ownership-transfer
   :safe8-shared-state-access :safe8-synchronization-proof
   :safe8-atomic-order :safe8-blocking-cancellation
   :safe8-backend-preservation :safe8-race-analysis])

(def safe9-required-families
  [:safe9-numeric-mode :safe9-runtime-check :safe9-range-proof
   :safe9-floating-mode :safe9-elementary-approximation
   :safe9-relaxed-approval :safe9-optimization-proof
   :safe9-backend-lowering])

(def safe11-required-families
  [:safe11-taint-source :safe11-taint-flow :safe11-validator-contract
   :safe11-residual-constraint :safe11-sink-authorization
   :safe11-parameterization :safe11-deserialization
   :safe11-secret-redaction :safe11-prompt-tool-policy
   :safe11-generated-taint :safe11-unsafe-clear-audit])

(defn safe7-conformance-fixture
  [checker-state]
  (let [covered (cond-> #{}
                  (seq (:safe7-foreign-declaration-records checker-state))
                  (conj :safe7-foreign-declaration)
                  (seq (:safe7-abi-protocol-records checker-state))
                  (conj :safe7-abi-record)
                  (seq (:safe7-type-mapping-records checker-state))
                  (conj :safe7-type-mapping)
                  (seq (:safe7-ownership-lifetime-maps checker-state))
                  (conj :safe7-ownership-lifetime)
                  (seq (:safe7-safe-wrapper-audits checker-state))
                  (conj :safe7-safe-wrapper)
                  (seq (:safe7-error-translation-maps checker-state))
                  (conj :safe7-error-translation)
                  (seq (:safe7-callback-safety-records checker-state))
                  (conj :safe7-callback-safety)
                  (seq (:safe7-host-bridge-records checker-state))
                  (conj :safe7-host-bridge)
                  (seq (:safe7-generated-binding-provenance checker-state))
                  (conj :safe7-generated-binding))
        missing (vec (remove covered safe7-required-families))]
    {:required-families safe7-required-families
     :covered-families (vec (sort-by name covered))
     :document :SAFE7
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))