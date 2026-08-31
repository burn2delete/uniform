

(def l8-required-dispatch-families
  [:protocol :implementation :method-signature :direct :dictionary :vtable
   :hosted-dynamic :multimethod :host-interop :tool :interface-lowering
   :method-effects])

(defn dispatch-conformance-fixture
  [protocol-table implementation-table method-signatures dispatch-records
   multimethods interface-artifacts host-records]
  (let [modes (set (map :dispatch-mode dispatch-records))
        covered (cond-> #{}
                  (seq protocol-table) (conj :protocol)
                  (seq implementation-table) (conj :implementation)
                  (seq method-signatures) (conj :method-signature)
                  (contains? modes :direct) (conj :direct)
                  (contains? modes :dictionary) (conj :dictionary)
                  (contains? modes :vtable) (conj :vtable)
                  (contains? modes :hosted-dynamic) (conj :hosted-dynamic)
                  (seq multimethods) (conj :multimethod)
                  (seq host-records) (conj :host-interop)
                  (contains? modes :artifact-boundary) (conj :tool)
                  (seq interface-artifacts) (conj :interface-lowering)
                  (some seq (map :effects dispatch-records)) (conj :method-effects))
        missing (vec (remove covered l8-required-dispatch-families))]
    {:required-families l8-required-dispatch-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l9-required-error-families
  [:option :result :try :throw :panic :safety-check :host-error :ffi-error
   :workflow-error :ai-error])

(defn error-conformance-fixture
  [checker-state type-facts]
  (let [covered (cond-> #{}
                  (some #(= :option (:family %)) (:error-type-declarations checker-state)) (conj :option)
                  (some #(= :result (:family %)) (:error-type-declarations checker-state)) (conj :result)
                  (some #(= :try (:source-kind %)) type-facts) (conj :try)
                  (seq (:thrown-error-effect-records checker-state)) (conj :throw)
                  (seq (:panic-lowering-records checker-state)) (conj :panic)
                  (seq (:safety-check-failure-records checker-state)) (conj :safety-check)
                  (seq (:host-error-normalization-records checker-state)) (conj :host-error)
                  (seq (:ffi-error-mapping-artifacts checker-state)) (conj :ffi-error)
                  (seq (:workflow-failure-records checker-state)) (conj :workflow-error)
                  (seq (:ai-tool-error-records checker-state)) (conj :ai-error))
        missing (vec (remove covered l9-required-error-families))]
    {:required-families l9-required-error-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l10-required-memory-families
  [:gc :ownership :borrow :region :arena :stack :static :linear :raw :mmio
   :gpu-device :host-managed :initialization :allocation-effect
   :allocator-runtime :bounds-check])

(defn memory-conformance-fixture
  [checker-state]
  (let [memory-families (set (keep :family (:memory-facts checker-state)))
        covered (cond-> memory-families
                  (seq (:linear-resource-table checker-state)) (conj :linear)
                  (seq (:allocation-effect-records checker-state)) (conj :allocation-effect)
                  (seq (:allocator-runtime-manifests checker-state)) (conj :allocator-runtime)
                  (some #(= :bounds (:kind %)) (:runtime-check-records checker-state)) (conj :bounds-check)
                  (seq (:unsafe-raw-memory-audit-records checker-state)) (conj :raw)
                  (seq (:mmio-capability-records checker-state)) (conj :mmio)
                  (seq (:initialization-facts checker-state)) (conj :initialization)
                  (seq (:lifetime-region-facts checker-state)) (into [:region :arena])
                  (seq (:ownership-borrow-facts checker-state)) (conj :ownership))
        missing (vec (remove covered l10-required-memory-families))]
    {:required-families l10-required-memory-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l11-required-concurrency-families
  [:task-scope :structured-task :async-await :atomic :lock :channel :actor
   :ownership-transfer :immutable-sharing :workflow-replay :scheduler-runtime
   :hardware-state :gpu-kernel :race-analysis])

(defn concurrency-conformance-fixture
  [checker-state]
  (let [fact-families (set (keep :conformance-family (:concurrency-facts checker-state)))
        covered (cond-> fact-families
                  (seq (:task-scope-graphs checker-state)) (conj :task-scope)
                  (seq (:scheduler-runtime-manifests checker-state)) (conj :scheduler-runtime)
                  (seq (:workflow-replay-records checker-state)) (conj :workflow-replay)
                  (seq (:race-analysis-reports checker-state)) (conj :race-analysis)
                  (some #(= :atomic (:primitive %)) (:synchronization-facts checker-state)) (conj :atomic)
                  (some #(= :lock (:primitive %)) (:synchronization-facts checker-state)) (conj :lock)
                  (some #(= :hardware-state (:primitive %)) (:synchronization-facts checker-state)) (conj :hardware-state)
                  (some #(= :gpu-barrier (:primitive %)) (:synchronization-facts checker-state)) (conj :gpu-kernel)
                  (some #(= :channel (:kind %)) (:actor-channel-schemas checker-state)) (conj :channel)
                  (some #(= :actor (:kind %)) (:actor-channel-schemas checker-state)) (conj :actor)
                  (seq (:concurrency-ownership-transfer-records checker-state)) (conj :ownership-transfer))
        missing (vec (remove covered l11-required-concurrency-families))]
    {:required-families l11-required-concurrency-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l12-required-compile-time-families
  [:pure-constant :defconst :schema-loading :write-artifact :target-probe
   :generated-code :model-codegen :nondeterministic-replay :cache-decision
   :build-effect-log :grant-record :hermetic-replay :cache-key])

(defn compile-time-conformance-fixture
  [checker-state build-log]
  (let [trace-kinds (set (keep :compile-time-kind
                               (:compile-time-evaluation-trace checker-state)))
        covered (cond-> trace-kinds
                  (seq (:constant-value-table checker-state)) (conj :defconst)
                  (seq (:generated-form-provenance-records checker-state)) (conj :generated-code)
                  (seq build-log) (conj :build-effect-log)
                  (seq (:compile-time-capability-proof-records checker-state)) (conj :grant-record)
                  (seq (:hermetic-replay-records checker-state)) (conj :hermetic-replay)
                  (seq (:cache-key-records checker-state)) (conj :cache-key))
        missing (vec (remove covered l12-required-compile-time-families))]
    {:required-families l12-required-compile-time-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l13-required-standard-library-families
  [:namespace-contract :api-contract :profile-availability :example
   :unsafe-wrapper :compatibility :numeric-mode :resource-api
   :effect-record :capability-record :allocation-record
   :blocking-record :panic-record])

(defn standard-library-conformance-fixture
  [checker-state]
  (let [api-records (:standard-library-api-contracts checker-state)
        covered (cond-> #{}
                  (seq (:standard-library-namespace-contracts checker-state))
                  (conj :namespace-contract)

                  (seq api-records)
                  (conj :api-contract)

                  (seq (:standard-library-profile-availability-reports checker-state))
                  (conj :profile-availability)

                  (seq (:standard-library-documentation-examples checker-state))
                  (conj :example)

                  (seq (:standard-library-unsafe-wrapper-audits checker-state))
                  (conj :unsafe-wrapper)

                  (seq (:standard-library-compatibility-records checker-state))
                  (conj :compatibility)

                  (seq (:standard-library-numeric-mode-records checker-state))
                  (conj :numeric-mode)

                  (seq (:standard-library-resource-records checker-state))
                  (conj :resource-api)

                  (some seq (map :declared-effects api-records))
                  (conj :effect-record)

                  (some seq (map :declared-capabilities api-records))
                  (conj :capability-record)

                  (some :allocation api-records)
                  (conj :allocation-record)

                  (some :blocking api-records)
                  (conj :blocking-record)

                  (some :panic api-records)
                  (conj :panic-record))
        missing (vec (remove covered l13-required-standard-library-families))]
    {:required-families l13-required-standard-library-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l14-required-facet-families
  [:manifest :activation :generated-code :domain-ir :composition
   :privacy-boundary :compatibility :build-effect :capability
   :source-map :schema-version])

(defn facet-conformance-fixture
  [checker-state]
  (let [domain-ir (:facet-domain-ir-records checker-state)
        generated (:facet-generated-gravity-records checker-state)
        manifests (:facet-manifests checker-state)
        covered (cond-> #{}
                  (seq manifests) (conj :manifest)
                  (seq (:facet-activation-records checker-state)) (conj :activation)
                  (seq generated) (conj :generated-code)
                  (seq domain-ir) (conj :domain-ir)
                  (seq (:facet-composition-records checker-state)) (conj :composition)
                  (seq (:facet-privacy-boundary-records checker-state)) (conj :privacy-boundary)
                  (seq (:facet-compatibility-records checker-state)) (conj :compatibility)
                  (some seq (map :build-effects manifests)) (conj :build-effect)
                  (some seq (map :capabilities-declared manifests)) (conj :capability)
                  (or (some #(= :preserved (:source-map %)) domain-ir)
                      (some #(= :preserved (:source-map %)) generated)) (conj :source-map)
                  (or (some :schema-version manifests)
                      (some :artifact-schema-version domain-ir)) (conj :schema-version))
        missing (vec (remove covered l14-required-facet-families))]
    {:required-families l14-required-facet-families
     :covered-families (vec (sort-by name covered))
     :missing-families missing
     :status (if (empty? missing) :complete :incomplete)}))

(def l15-required-selection-sources
  #{:source-annotation :manifest :workspace-policy :profile-default
    :compiler-default})

(def l15-required-scope-categories
  #{:filesystem :network :environment :model :tool :memory :compiler})

(def l15-required-provider-families
  [:declaration :grant :explicit-capability-value :selection-source
   :scope-audit :build-runtime-separation :compile-time-replay
   :runtime-manifest :conformance :replacement :attenuation :revocation
   :artifact-record])