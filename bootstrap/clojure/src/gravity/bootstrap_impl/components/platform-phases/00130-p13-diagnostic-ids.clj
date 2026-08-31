

(def p13-diagnostic-ids
  ["P13-DIRECT" "P13-FACADE" "P13-ARTIFACT" "P13-EVIDENCE"
   "P13-RUNTIME" "P13-MEMORY" "P13-EFFECT" "P13-CAPABILITY"
   "P13-GENERATED" "P13-MATRIX"])

(def p13-artifact-boundary-kinds
  #{:schema :schema-file :proof-object :proof-artifact :hdl :hdl-module
    :firmware-image :kernel-module :workflow-graph :agent-manifest
    :native-object :generated-header :verified-lookup-table})

(def p13-default-facade-evidence
  #{:typed-api :safety-proof})

(defn p13-required-facade-evidence
  [consumer-profile producer-profile]
  (cond
    (and (contains? #{:firmware :kernel} consumer-profile)
         (= :native producer-profile))
    #{:typed-api :safety-proof :no-gc :no-hidden-allocation :no-throw}

    (and (= :distributed consumer-profile)
         (contains? #{:hosted :native} producer-profile))
    #{:schema :durable-step :capability-proof}

    (= :ai consumer-profile)
    #{:tool-schema :capability-proof :human-review-policy}

    (and (= :gpu consumer-profile)
         (= :native producer-profile))
    #{:transfer-boundary :capability-proof :device-memory-proof}

    (= :formal consumer-profile)
    #{:proof-certificate :safety-proof}

    :else
    p13-default-facade-evidence))

(defn p13-source-context
  [source-path source-text]
  (let [records (read-source-form-records source-path source-text)
        forms (mapv :form records)
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        dependencies (vec (concat (:requires module) (:imports module)))]
    {:records records
     :forms forms
     :module module
     :dependencies dependencies
     :metadata (:metadata module)}))

(defn p13-fail!
  [id source-path module dependency extra]
  (fail! id
         (case id
           "P13-DIRECT" "illegal direct cross-profile source import"
           "P13-FACADE" "cross-profile facade metadata is missing or invalid"
           "P13-ARTIFACT" "artifact-only edge is being imported as source"
           "P13-EVIDENCE" "cross-profile edge is missing required evidence"
           "P13-RUNTIME" "producer runtime assumptions are unsupported"
           "P13-MEMORY" "producer memory assumptions are incompatible"
           "P13-EFFECT" "producer effects exceed consumer profile policy"
           "P13-CAPABILITY" "producer capabilities exceed consumer grants"
           "P13-GENERATED" "macro-generated cross-profile edge is illegal"
           "P13-MATRIX" "package-declared edge contradicts the profile matrix"
           "profile compatibility diagnostic")
         (merge {:source-span {:source source-path}
                 :document-id "P13"
                 :consumer-namespace (:module module)
                 :consumer-profile (:profile module)
                 :producer-namespace (:module dependency)
                 :producer-profile (:profile dependency)
                 :edge-kind (or (:edge dependency) :direct)
                 :boundary (:boundary dependency)
                 :generated-origin-chain (when (:generated? dependency)
                                           [:macro-generated-edge])
                 :suggested-boundary
                 (or (:suggested-boundary extra)
                     :profile-safe-facade-or-artifact-boundary)
                 :remediation
                 "Declare a legal direct edge, complete facade evidence, or an artifact-only boundary before lowering."}
                extra)))

(defn p13-unsupported-runtime
  [consumer-profile runtime]
  (let [providers (set (:providers runtime))
        consumer-providers (get-in (profile-contract consumer-profile)
                                   [:runtime :providers])]
    (when (:required runtime)
      (seq (set/difference providers consumer-providers)))))

(defn p13-memory-compatible?
  [consumer-profile memory]
  (let [consumer-hidden (get-in (profile-contract consumer-profile)
                                [:memory :hidden-allocation])
        producer-hidden (:hidden-allocation memory)]
    (or (nil? producer-hidden)
        (not= :forbidden consumer-hidden)
        (= :forbidden producer-hidden))))

(defn p13-effect-denials
  [consumer-profile effects]
  (seq (set/difference (set effects)
                       (profile-allowed-effects consumer-profile))))

(defn p13-capability-denials
  [consumer-profile capabilities]
  (seq (set/difference (set capabilities)
                       (profile-capabilities consumer-profile))))

(defn p13-facade-record
  [module dependency]
  (let [consumer (:profile module)
        producer (:profile dependency)]
    {:consumer {:namespace (:module module)
                :profile consumer}
     :producer {:namespace (:module dependency)
                :profile producer}
     :edge :facade-required
     :boundary (:boundary dependency)
     :facade (:facade dependency)
     :evidence (:evidence dependency)
     :effects (:producer-effects dependency)
     :capabilities (:producer-capabilities dependency)
     :memory (:memory dependency)
     :runtime (:runtime dependency)
     :provider (:provider dependency)
     :status :accepted}))

(defn p13-direct-record
  [module dependency]
  {:consumer {:namespace (:module module)
              :profile (:profile module)}
   :producer {:namespace (:module dependency)
              :profile (:profile dependency)}
   :edge :direct
   :boundary (:boundary dependency)
   :facade nil
   :evidence #{}
   :status :accepted})

(defn p13-validate-facade!
  [source-path module dependency]
  (let [consumer (:profile module)
        producer (:profile dependency)
        evidence (set (:evidence dependency))
        required-evidence (p13-required-facade-evidence consumer producer)
        missing-evidence (set/difference required-evidence evidence)
        unsupported-runtime (p13-unsupported-runtime consumer
                                                     (:runtime dependency))
        effect-denials (p13-effect-denials consumer
                                           (:producer-effects dependency))
        capability-denials (p13-capability-denials
                            consumer
                            (:producer-capabilities dependency))]
    (when-not (and (:facade dependency)
                   (= :facade (:boundary dependency)))
      (p13-fail! "P13-FACADE" source-path module dependency
                 {:missing-fact :facade
                  :suggested-boundary :facade-required}))
    (when (seq missing-evidence)
      (p13-fail! "P13-EVIDENCE" source-path module dependency
                 {:missing-evidence missing-evidence
                  :required-evidence required-evidence
                  :suggested-boundary :facade-required}))
    (when (seq unsupported-runtime)
      (p13-fail! "P13-RUNTIME" source-path module dependency
                 {:unsupported-runtime-providers unsupported-runtime
                  :runtime (:runtime dependency)
                  :suggested-boundary :runtime-isolating-facade}))
    (when-not (p13-memory-compatible? consumer (:memory dependency))
      (p13-fail! "P13-MEMORY" source-path module dependency
                 {:memory (:memory dependency)
                  :consumer-memory (get-in (profile-contract consumer)
                                           [:memory])
                  :suggested-boundary :memory-safe-facade}))
    (when (seq effect-denials)
      (p13-fail! "P13-EFFECT" source-path module dependency
                 {:denied-effects effect-denials
                  :suggested-boundary :effect-narrowing-facade}))
    (when (seq capability-denials)
      (p13-fail! "P13-CAPABILITY" source-path module dependency
                 {:denied-capabilities capability-denials
                  :suggested-boundary :capability-narrowing-facade}))
    (p13-facade-record module dependency)))