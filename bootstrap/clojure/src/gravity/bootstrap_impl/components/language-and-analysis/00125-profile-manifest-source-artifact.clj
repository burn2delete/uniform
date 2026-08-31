

(defn profile-manifest-source-artifact
  [source-path source-text]
  (try
    (let [typed-artifact (typed-source-artifact source-path source-text)
          module-artifact (module-source-artifact source-path source-text)
          module (:module typed-artifact)
          inferred-effects (or (get-in typed-artifact [:namespace-effect-summary :inferred])
                               (get-in module-artifact [:namespace-effect-summary :inferred])
                               #{})
          required-capabilities (set (map :capability
                                          (:provider-selection-records typed-artifact)))
          effect-authority (profile-effective-effects module inferred-effects)
          capability-authority (profile-effective-capabilities module
                                                               required-capabilities)
          effect-table (effect-permission-table module inferred-effects
                                                effect-authority)
          capability-table (capability-permission-table module
                                                        required-capabilities
                                                        capability-authority)
          contract (profile-contract (:profile module))
          memory-regime (merge (:memory contract)
                               (get-in module [:metadata :memory] {}))
          runtime-assumptions (merge (:runtime contract)
                                     (get-in module [:metadata :runtime] {}))
          dependency-graph (:module-dependency-graph module-artifact)
          provider-selections (distinct-records
                               (:provider-selection-records typed-artifact))
          manifest {:module (:module module)
                    :source-path source-path
                    :profile (:profile module)
                    :target (:target module)
                    :source-effects (:source effect-authority)
                    :inferred-effects inferred-effects
                    :effective-effects (:effective effect-authority)
                    :source-capabilities (:source capability-authority)
                    :required-capabilities required-capabilities
                    :effective-capabilities (:effective capability-authority)
                    :memory-regime memory-regime
                    :runtime-assumptions runtime-assumptions
                    :unsafe-policy (:unsafe-policy contract)
                    :dependencies dependency-graph
                    :provider-selections provider-selections
                    :safety (:safety module)
                    :metadata (:metadata module)
                    :policy-layers {:effects (select-keys effect-authority
                                                          [:source :profile
                                                           :package :provider
                                                           :deployment])
                                    :capabilities (select-keys capability-authority
                                                               [:source :profile
                                                                :package :provider
                                                                :deployment])}}
          manifest-with-contract (assoc manifest :profile-contract contract)
          backend-report (backend-eligibility-report module manifest-with-contract)
          conformance (profile-conformance-fixture manifest effect-table
                                                   capability-table
                                                   dependency-graph
                                                   backend-report)]
      {:kind :gravity/stage0-profile-manifest-artifact
       :document "P1"
       :pass {:name :profile-manifest-validation
              :input :typed-effected-core
              :output :profile-manifest
              :requires [:reader :namespace-analyzer :macro-expansion
                         :core-lowering :type-effect-capability-check]
              :preserves [:source-spans :generated-origin :profile :target
                          :effects :capabilities :metadata]
              :emits [:profile-manifest :effect-permission-table
                      :capability-permission-table :memory-regime-record
                      :runtime-assumption-record :cross-profile-dependency-graph
                      :backend-eligibility-report :profile-diagnostics
                      :profile-conformance-fixture]
              :rejects p1-diagnostic-ids}
       :typed-core-artifact-hash (str "sha256:"
                                      (sha256-hex (pr-str typed-artifact)))
       :profile-contract-schema {:portable-minimum-fields
                                 [:profile :allowed-forms :allowed-effects
                                  :checked-effects :forbidden-effects
                                  :capabilities :memory :runtime
                                  :nondeterminism :unsafe-policy
                                  :artifact-boundaries]
                                 :policy-layers [:source :profile :package
                                                 :provider :deployment]
                                 :status :complete}
       :profile-contract contract
       :profile-manifest manifest
       :effect-permission-table effect-table
       :capability-permission-table capability-table
       :memory-regime-record {:profile (:profile module)
                              :memory memory-regime}
       :runtime-assumption-record {:profile (:profile module)
                                   :runtime runtime-assumptions}
       :cross-profile-dependency-graph dependency-graph
       :profile-boundary-records (:profile-boundary-records module-artifact)
       :backend-eligibility-report backend-report
       :profile-diagnostics []
       :profile-conformance-fixture conformance
       :diagnostics []})
    (catch clojure.lang.ExceptionInfo ex
      (throw-p1-diagnostic! ex))))

(def first-profile-set-order
  [:core :meta :hosted :native])

(def profile-documents-by-profile
  {:core "P2"
   :meta "P3"
   :hosted "P4"
   :native "P5"})

(def profile-set-diagnostic-ids-by-document
  {"P2" ["P2-EFFECT" "P2-CAPABILITY" "P2-RUNTIME" "P2-MEMORY"
         "P2-UNSAFE" "P2-NONDETERMINISM" "P2-MACRO" "P2-IMPORT"
         "P2-BACKEND"]
   "P3" ["P3-BUILD-EFFECT" "P3-HERMETIC" "P3-COMPILER-CAPABILITY"
         "P3-PASS-CONTRACT" "P3-FACT-INVALIDATION"
         "P3-GENERATED-PROFILE" "P3-GENERATED-SAFETY" "P3-PHASE"
         "P3-SOURCE-MAP"]
   "P4" ["P4-HOST-EFFECT" "P4-HOST-CAPABILITY" "P4-REFLECTION"
         "P4-DYNAMIC" "P4-HOST-OBJECT" "P4-EXCEPTION" "P4-RESOURCE"
         "P4-RAW-MEMORY" "P4-CROSS-IMPORT" "P4-SOURCEMAP"]
   "P5" ["P5-ALLOC" "P5-MEMORY-PROVIDER" "P5-FFI" "P5-RAW-MEMORY"
         "P5-THREAD" "P5-ATOMIC" "P5-SIMD" "P5-NUMERIC"
         "P5-OPTIMIZATION" "P5-RUNTIME"]})

(def profile-set-diagnostic-ids
  (vec (mapcat profile-set-diagnostic-ids-by-document ["P2" "P3" "P4" "P5"])))

(def profile-set-diagnostic-mapping
  {[:core "P1-EFFECT"] "P2-EFFECT"
   [:core "P1-CAPABILITY"] "P2-CAPABILITY"
   [:core "P1-RUNTIME"] "P2-RUNTIME"
   [:core "P1-MEMORY"] "P2-MEMORY"
   [:core "P1-CROSS-IMPORT"] "P2-IMPORT"
   [:core "P1-MACRO"] "P2-MACRO"
   [:core "P1-BACKEND"] "P2-BACKEND"
   [:hosted "P1-EFFECT"] "P4-HOST-EFFECT"
   [:hosted "P1-CAPABILITY"] "P4-HOST-CAPABILITY"
   [:hosted "P1-MEMORY"] "P4-RAW-MEMORY"
   [:hosted "P1-CROSS-IMPORT"] "P4-CROSS-IMPORT"
   [:native "P1-MEMORY"] "P5-RAW-MEMORY"
   [:native "P1-RUNTIME"] "P5-RUNTIME"
   [:native "P1-CAPABILITY"] "P5-MEMORY-PROVIDER"
   [:native "P1-BACKEND"] "P5-SIMD"})

(defn profile-set-diagnostic-id
  [data]
  (let [id (:id data)]
    (cond
      (contains? (set profile-set-diagnostic-ids) id) id
      :else (get profile-set-diagnostic-mapping
                 [(or (:active-profile data) (:profile data)) id]))))

(defn throw-profile-set-diagnostic!
  [ex]
  (let [data (ex-data ex)]
    (if-let [id (profile-set-diagnostic-id data)]
      (throw (diagnostic id
                         (or (:message data)
                             (str "profile-set diagnostic " id))
                         (merge (dissoc data :id :message)
                                {:underlying-diagnostic (:id data)
                                 :underlying-message (:message data)
                                 :active-profile (or (:active-profile data)
                                                     (:profile data))
                                 :target (:target data)
                                 :legal-alternative (:remediation data)
                                 :diagnostic-family :profile-set-validation})))
      (throw ex))))

(defn profile-set-conformance-fixture
  [profile document effect-capability-matrix]
  {:documents ["P2" "P3" "P4" "P5"]
   :active-document document
   :active-profile profile
   :required-profiles first-profile-set-order
   :diagnostic-ids (get profile-set-diagnostic-ids-by-document document)
   :effective-effects (get-in effect-capability-matrix [:effects :effective])
   :effective-capabilities (get-in effect-capability-matrix
                                   [:capabilities :effective])
   :matrix-status :complete
   :status :complete})