

(defn p15-s23-closed-core-fail!
  [id source-path subject extra]
  (let [subject-map (if (map? subject) subject {})
        facts
        (merge {:closed-slice :pure-literal-quote-local-do-if-let
                :whole-language? false
                :pre-mir? true
                ;; A C2 form id is not a C3 syntax identity.  Callers that
                ;; have a genuine C3 object supply its id explicitly and keep
                ;; the C2 edge in :c2-form-id.
                :syntax-id (or (:syntax-id subject-map) :not-applicable)
                :c2-form-id (or (:c2-form-id subject-map)
                                (:form-id subject-map)
                                :not-applicable)
                :core-node-id (or (:core-node-id subject-map)
                                  (:node-id subject-map)
                                  :not-applicable)
                :generated-origin-chain
                (let [origin (or (:generated-origin subject-map)
                                 (get-in subject-map
                                         [:source :generated-origin]))]
                  (if (vector? origin) origin []))
                :lowering-rule (or (:lowering-rule subject-map)
                                   :p15-s23-pure-closed-core)
                :active-profile (or (:profile subject-map) :hosted)
                :profile (or (:profile subject-map) :hosted)
                :source-target (or (:source-target subject-map) :jvm)
                :requested-target
                (or (:requested-target subject-map)
                    (:target subject-map)
                    :jvm)
                :target (or (:target subject-map)
                            (:requested-target subject-map)
                            :jvm)
                :target-request (or (:requested-target subject-map)
                                    :target-neutral-bootstrap)
                :namespace (or (:namespace subject-map)
                               (:module subject-map)
                               :not-applicable)
                :function (or (:function subject-map) 'main)
                :value-id (or (:value-id subject-map)
                              (:node-id subject-map)
                              :not-applicable)
                :owner-id (or (:owner-id subject-map)
                              (:node-id subject-map)
                              :not-applicable)
                :borrow-id (or (:borrow-id subject-map) :not-applicable)
                :region-id (or (:region-id subject-map) :not-applicable)
                :arena-generation (or (:arena-generation subject-map)
                                      :not-applicable)
                :resource-id (or (:resource-id subject-map)
                                 :not-applicable)
                :control-path (or (:control-path subject-map)
                                  (:path subject-map)
                                  :not-applicable)
                :operation-id (or (:operation-id subject-map)
                                  (:node-id subject-map)
                                  (:form-id subject-map)
                                  :not-applicable)
                :relevant-binding-id
                (or (:relevant-binding-id subject-map)
                    (:binding-ref subject-map)
                    (:binding-id subject-map)
                    :not-applicable)
                :expected-type (or (:expected-type subject-map)
                                   :typed-pure-closed-core)
                :actual-type (or (:actual-type subject-map)
                                 (:type subject-map)
                                 :not-applicable)
                :provider (or (:provider subject-map) :not-applicable)
                :grant (or (:grant subject-map) :not-applicable)
                :specialized-safe-rule
                (or (:specialized-safe-rule subject-map)
                    :persistent-immutable-pure-closed-operation)
                :safety-mode (or (:safety-mode subject-map) :safe)
                :proof-id (or (:proof-id subject-map) :not-applicable)
                :runtime-check (or (:runtime-check subject-map)
                                   :not-applicable)
                :unsafe-audit (or (:unsafe-audit subject-map)
                                  :not-applicable)
                :boundary-identity-reason
                (if (some #(and (string? %) (not (str/blank? %)))
                          [(:syntax-id subject-map)
                           (:node-id subject-map)
                           (:core-node-id subject-map)])
                  :genuine-source-or-core-identity
                  :identity-unavailable-at-raw-verifier-boundary)}
               extra)]
    (try
      (cond
        (contains? (set c6-lowering-diagnostic-ids) id)
        (c6-lowering-fail! id source-path subject facts)

        (contains? (set c7-type-diagnostic-ids) id)
        (c7-type-fail! id source-path subject facts)

        (contains? (set c8-effect-diagnostic-ids) id)
        (c8-effect-fail! id source-path subject facts)

        (contains? (set c9-ownership-diagnostic-ids) id)
        (c9-ownership-fail! id source-path subject facts)

        (contains? (set c10-safety-diagnostic-ids) id)
        (c10-safety-fail! id source-path subject facts)

        :else
        (c6-lowering-fail!
         "C6-VERIFY" source-path subject
         (assoc facts :unrecognized-pre-mir-diagnostic id)))
      (catch clojure.lang.ExceptionInfo exception
        (if (some? *p15-s23-c11-upstream-diagnostic-owner*)
          (throw
           (ex-info
            (.getMessage exception)
            (assoc (ex-data exception)
                   ::c11-upstream-diagnostic-owner
                   *p15-s23-c11-upstream-diagnostic-owner*)
            exception))
          (throw exception))))))

(defn p15-s23-closed-core-source-request-bounds!
  [source-path source-text requested-target]
  (when-not (and (string? source-path)
                 (string? source-text)
                 (keyword? requested-target))
    (p15-s23-closed-core-fail!
     "C6-CORE-SHAPE" (if (string? source-path)
                        source-path
                        "<closed-core>")
     {:source-path (when (string? source-path) source-path)
      :requested-target (when (keyword? requested-target)
                          requested-target)}
     {:missing-fact :typed-closed-core-source-request}))
  (let [observation
        (p15-s23-closed-core-bounded-utf8-count
         source-text p15-s23-closed-core-max-source-bytes)]
    (when-not (= :valid (:status observation))
      (p15-s23-closed-core-fail!
       "C6-VERIFY" source-path (source-span source-path 0)
       {:missing-fact :bounded-closed-core-source-bytes
        :observed-source-bytes (:bytes observation)
        :encoding-status (:status observation)
        :maximum-source-bytes p15-s23-closed-core-max-source-bytes}))
    (:bytes observation)))

(defn p15-s23-closed-core-path-neutral-span
  [span]
  (when (map? span)
    (dissoc span :source :file)))

(defn p15-s23-closed-core-path-neutral-generated-origin
  [origin]
  (cond-> (dissoc origin :source-id :source-path :path :inputs)
    (contains? origin :span)
    (update :span p15-s23-closed-core-path-neutral-span)
    (contains? origin :source-span)
    (update :source-span p15-s23-closed-core-path-neutral-span)
    (contains? origin :from)
    (update :from p15-s23-closed-core-path-neutral-span)
    (contains? origin :generated-span)
    (update :generated-span
            #(if (map? %)
               (p15-s23-closed-core-path-neutral-span %)
               %))))

(defn p15-s23-closed-core-digest
  [value]
  (reader-canonical-hash (c-backend-canonical-value value)))

(def p15-s23-checked-core-authority-record-keys
  #{:kind :schema-version :source-content-hash :plan-id :module :profile
    :source-target :runtime-source-content-hash
    :runtime-contract-definition-hash
    :runtime-contract-derived-facts-hash :runtime-artifact-hash
    :runtime-function :runtime-function-hash
    :program-authority-policy-id :program-authority-policy-hash
    :structural-operation-set :required-effects :required-capabilities
    :program-principal :runtime-principal :handler-principal
    :program-provider-records :program-grant-records
    :runtime-provider-ids :runtime-grant-ids
    :handler-provider-ids :handler-grant-ids
    :scope :phase :lifetime :single-invocation?
    :reference-interpreter? :deployment-runtime? :live-external-io?
    :adapter-authority :authority-record-id})

(def p15-s23-checked-core-program-provider-record-keys
  #{:artifact :schema-version :provider-selection-id :principal-id
    :effect :capability :provider-id :profile :target :phase :scope
    :source-binding :lifetime :policy-id :policy-hash :selection-source
    :provider-provenance
    :source-declaration-is-grant? :deployment :live-external-authority?
    :status})

(def p15-s23-checked-core-program-grant-record-keys
  #{:artifact :schema-version :grant-id :principal-id :effect :capability
    :provider-selection-id :provider-id :scope :source-binding :profile
    :target :phase :lifetime :policy-id :policy-hash :audit-policy-id
    :program-grant-template-id :reference-invocation :package :deployment
    :authority-source :provider-provenance :source-declaration-is-grant?
    :live-external-authority? :status})

(defn p15-s23-checked-core-authority-structural-operations
  [requirements]
  (cond-> #{}
    (or (contains? (:required-effects requirements) :memory/allocate)
        (contains? (:required-capabilities requirements) :memory/allocator))
    (conj :str)

    (or (contains? (:required-effects requirements) :io/write)
        (contains? (:required-capabilities requirements) :io/stdout))
    (conj :println)))

(defn p15-s23-checked-core-authority-source-binding
  [source-content-hash plan-id module structural-operation-set]
  {:source-content-hash source-content-hash
   :plan-id plan-id
   :module module
   :structural-operation-set structural-operation-set})