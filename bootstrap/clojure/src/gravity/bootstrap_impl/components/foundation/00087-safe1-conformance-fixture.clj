

(defn safe1-conformance-fixture
  [checker-state]
  (let [classifications (:safe1-safety-classification-records checker-state)
        outcomes (set (map :outcome classifications))
        covered (cond-> #{}
                  (contains? outcomes :proven-safe) (conj :proven-safe)
                  (contains? outcomes :runtime-checked) (conj :runtime-checked)
                  (contains? outcomes :rejected) (conj :rejected)
                  (contains? outcomes :unsafe-island) (conj :unsafe-island)
                  (seq (:safe1-runtime-check-records checker-state))
                  (conj :runtime-check-record)
                  (seq (:safe1-unsafe-island-audit-records checker-state))
                  (conj :unsafe-island-audit)
                  (seq (:safe1-generated-code-safety-provenance checker-state))
                  (conj :generated-provenance)
                  (seq (:safe1-optimization-check-erasure-justifications checker-state))
                  (conj :optimization-proof)
                  (seq (:safe1-dependency-safety-mode-records checker-state))
                  (conj :dependency-mode))
        missing (vec (remove covered safe1-required-families))]
    {:required-families safe1-required-families
     :covered-families (vec (sort-by name covered))
     :legal-outcomes (vec (sort-by name safe1-legal-outcomes))
     :observed-outcomes (vec (sort-by name outcomes))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def ^:private safe1-source-outcome-operations
  {:arg-value #'dispatch-arg-value :check-outcome! #'check-safe1-outcome!
   :diagnostic! #'safe1-diagnostic! :record! #'record-checker!
   :legal-outcomes #'safe1-legal-outcomes})

(defn record-safe1-call!
  [checker ctx node operator args effects capabilities return-type spec]
  (gravity.safety/record-native-outcome-call!
   checker ctx node operator args effects capabilities return-type spec safe1-source-outcome-operations))

(def safe-memory-required-families
  [:safe2-memory-operation :safe2-runtime-check :safe2-allocation-release
   :safe2-escape-analysis :safe2-optimization-proof
   :safe2-backend-preservation :safe2-unsafe-audit
   :safe3-ownership-graph :safe3-borrow-graph :safe3-lifetime-map
   :safe3-transfer :safe3-runtime-borrow-check
   :safe4-region-lifetime :safe4-arena-generation
   :safe4-reset-invalidation :safe4-provider :safe4-cleanup
   :safe5-linear-flow :safe5-terminal-operation
   :safe5-exceptional-cleanup :safe5-structured-lowering
   :safe5-generated-flow])