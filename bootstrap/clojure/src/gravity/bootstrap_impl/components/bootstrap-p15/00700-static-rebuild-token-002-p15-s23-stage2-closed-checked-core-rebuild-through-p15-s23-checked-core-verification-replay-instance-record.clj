(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-stage2-closed-checked-core-rebuild
  [context]
  (p15-s23-checked-core-bounded-context! context)
  (when (and (map? context) (contains? context :authority-record))
    (p15-s23-closed-core-fail!
     "C8-CAPABILITY" (or (:source-path context) "<closed-core-context>")
     context
     {:missing-fact
      :effectful-static-rebuild-is-private-to-authenticating-verifier}))
  (p15-s23-stage2-closed-checked-core-rebuild-internal context nil))

(defn p15-s23-stage2-closed-checked-core-context-mode
  [context]
  (try
    (when-not (and (map? context)
                   (contains?
                    p15-s23-reference-runtime-supported-collection-class-names
                    (some-> context class .getName))
                   (<= (count context) 5))
      (throw (ex-info "unsupported context" {})))
    (p15-s23-reference-runtime-bounded-value!
     "p15-s23-closed-core-context" :jvm :checked-core-context-mode
     context p15-s23-reference-runtime-max-contract-nodes
     p15-s23-reference-runtime-max-contract-depth)
    (let [pure-keys #{:source-path :source-text :source-content-hash
                      :requested-target}
          effectful-keys (conj pure-keys :authority-record)
          keys (set (keys context))]
      (cond
        (= pure-keys keys) :pure
        (= effectful-keys keys) :effectful-reference
        :else :invalid))
    (catch StackOverflowError _ :invalid)
    (catch Exception _ :invalid)))

(def p15-s23-checked-core-verification-replay-authority-keys
  #{:kind :schema-version :policy-id :policy-hash :audit-policy-id
    :audit-policy-hash :binding :verifier-principal :runtime-principal
    :handler-principal :invocation-contract :provider-selection-records
    :grant-records :required-effects :required-capabilities :phase :lifetime
    :reference-invocation :package :deployment :deny-by-default?
    :host-service-boundary :authoritative-invocation?
    :excluded-from-authoritative-invocation-count?
    :program-authority-consumed? :program-grants-consumed?
    :live-external-io? :delegation :authority-widening?
    :authority-record-id})

(defn p15-s23-checked-core-verification-replay-instance-record
  [kind role binding policy audit-policy attributes]
  (let [provider-roles
        #{:verifier-managed-allocation :verifier-transcript-fixture
          :managed-allocation :transcript-capture}
        grant-roles (conj provider-roles :fixture)
        reserved-keys
        #{:kind :role :phase :lifetime :policy-id :policy-hash
          :audit-policy-id :audit-policy-hash :profile :target
          :runtime-family :service-id :delegated-handle-id :package
          :deployment :source-declaration-is-grant? :live-external-io?
          :delegation :authority-widening? :binding
          :provider-selection-record-id :grant-record-id}]
  (when-not
   (and (contains? #{:provider-selection :grant} kind)
        (contains? (if (= :provider-selection kind)
                     provider-roles grant-roles)
                   role)
        (map? attributes)
        (empty? (set/intersection reserved-keys (set (keys attributes)))))
    (p15-s23-closed-core-fail!
     "C8-CAPABILITY" "<verification-instance>" {}
     {:missing-fact
      :exact-verification-provider-grant-instance-role-and-fields})))
  (doseq [[definition value]
          [[:verification-instance-binding binding]
           [:verification-instance-policy policy]
           [:verification-instance-audit-policy audit-policy]
           [:verification-instance-attributes attributes]]]
    (p15-s23-checked-core-bounded-ingress!
     "C8-CAPABILITY" definition value
     p15-s23-reference-runtime-max-contract-nodes
     p15-s23-reference-runtime-max-contract-depth))
  (let [id-key (if (= :provider-selection kind)
                 :provider-selection-record-id
                 :grant-record-id)
        record
        (merge
         {:kind
          (if (= :provider-selection kind)
            :gravity/p15-s23-verification-provider-selection-record
            :gravity/p15-s23-verification-grant-record)
          :role role
          :phase :verification
          :lifetime :single-verification-replay
          :policy-id (:policy-id policy)
          :policy-hash (p15-s23-reference-runtime-hash policy)
          :audit-policy-id (:policy-id audit-policy)
          :audit-policy-hash
          (p15-s23-reference-runtime-hash audit-policy)
          :profile :hosted
          :target :jvm
          :runtime-family :managed
          :service-id
          :gravity.reference/checked-core-verification-runtime-service
          :delegated-handle-id
          :gravity.reference/checked-core-verification-runtime-handle
          :package :gravity/bootstrap
          :deployment :verification-harness-only
          :source-declaration-is-grant? false
          :live-external-io? false
          :delegation :none
          :authority-widening? false
          :binding binding}
         attributes)]
    (assoc record id-key
           (p15-s23-reference-runtime-hash record)))))
