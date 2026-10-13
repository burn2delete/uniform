;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(def ^:private semantic-validation-context-fields
  #{:validation-role :requested-cache-key-id :validated-cache-key-id})

(def ^:private requested-hit-binding-fields
  #{:artifact :schema-version :state :invocation-token :operation
    :exact-generic-key :exact-entry :exact-envelope :exact-artifact
    :exact-payload-bytes :requested-cache-key-id :legacy-semantic-key-id
    :legacy-storage-key-id :entry-id :producer-receipt-id
    :producer-binding-id :validation-binding-id :requested-artifact-id
    :c2-artifact-id :boundary-projection-id :carrier-id :payload-id})

(defn- invoke-c2-semantic-validator!
  [operations artifact entry legacy-key context]
  (when-not (and (= semantic-validation-context-fields (set (keys context)))
                 (contains? #{:fresh-requested-artifact
                              :direct-store-artifact
                              :requested-retained-artifact
                              :recovery-artifact}
                            (:validation-role context))
                 (sha256-id? (:requested-cache-key-id context))
                 (sha256-id? (:validated-cache-key-id context)))
    (cache-fail! "C16-STALE"
                 "C2 semantic validation context is incomplete" {}))
  (let [validated ((:validate-artifact! operations)
                   artifact entry legacy-key)]
    (when-not (identical? artifact validated)
      (cache-fail! "C16-STALE"
                   "C2 semantic validation must preserve artifact identity"
                   {:validation-role (:validation-role context)}))
    validated))

(defn- canonical-artifact-bytes!
  [artifact]
  (encode-canonical-bytes artifact {:reject-metadata? false}
                          maximum-blob-bytes))

(defn- decode-adapter-envelope!
  [envelope legacy-key operations validation-context expected-carrier-id]
  (when-not (and (map? envelope)
                 (= adapter-envelope-fields (set (keys envelope)))
                 (= :gravity/c2-pass-cache-v2-envelope (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= adapter-version (:adapter-version envelope))
                 (sha256-id? (:c2-artifact-id envelope))
                 (sha256-id? (:boundary-projection-id envelope))
                 (sha256-id? (:carrier-id envelope))
                 (= (class (byte-array 0)) (class (:payload-bytes envelope))))
    (cache-fail! "C16-ENTRY" "C2 v2 artifact envelope is malformed" {}))
  (when (and expected-carrier-id
             (not= expected-carrier-id (:carrier-id envelope)))
    (cache-fail! "C16-STALE" "C2 v2 envelope carrier binding is stale" {}))
  (let [payload ^bytes (:payload-bytes envelope)
        payload-id (str "sha256:" (digest/sha256-bytes-hex payload))
        _ (when-not (= payload-id (:payload-id envelope))
            (cache-fail! "C16-STALE"
                         "C2 v2 envelope payload identity is stale" {}))
        decoded-artifact (decode-canonical-bytes payload maximum-blob-bytes)
        artifact-id ((:artifact-id-of operations) decoded-artifact)
        boundary-id ((:boundary-projection-id-of operations) decoded-artifact)
        _ (when-not (and (= artifact-id (:c2-artifact-id envelope))
                         (= boundary-id (:boundary-projection-id envelope)))
            (cache-fail! "C16-STALE"
                         "C2 v2 envelope differs from its artifact identity"
                         {:artifact-id artifact-id
                          :boundary-projection-id boundary-id}))
        compatibility-entry
        {:artifact-id artifact-id
         :boundary-projection-id boundary-id
         :diagnostics
         (get-in decoded-artifact
                 [:incremental-reader-hashes :reader-diagnostics])}
        artifact (if validation-context
                   (invoke-c2-semantic-validator!
                    operations decoded-artifact compatibility-entry legacy-key
                    validation-context)
                   decoded-artifact)]
    {:envelope envelope
     :artifact artifact
     :artifact-id artifact-id
     :boundary-projection-id boundary-id
     :payload-id payload-id
     :payload-bytes payload}))

(defn- requested-hit-binding!
  [state operation legacy-key generic-key validation-binding-id operations
   envelope entry key requested]
  ;; A failed or superseded requested validation must never leave authority for
  ;; a later fresh path in this invocation.
  (swap! state dissoc :requested-hit-binding)
  (when-not (and (contains? #{:lookup :lookup-or-compute} operation)
                 (= #{:requested-cache-key-id :requested-artifact-id}
                    (set (keys requested)))
                 (identical? generic-key key)
                 (= (:cache-key-id generic-key)
                    (:requested-cache-key-id requested)
                    (:cache-key-id entry))
                 (= (:semantic-key-id generic-key)
                    (:semantic-key-id entry))
                 (= (:stage generic-key) (:stage entry))
                 (= (:pass-contract-id generic-key)
                    (:pass-contract-id entry))
                 (= (:contract generic-key) (:contract entry))
                 (= validation-binding-id (:validation-binding-id entry))
                 (= (:semantic-key-id legacy-key)
                    (get-in generic-key
                            [:provenance :metadata
                             :legacy-semantic-key-id]))
                 (= (:storage-key-id legacy-key)
                    (get-in generic-key
                            [:provenance :metadata
                             :legacy-storage-key-id])))
    (cache-fail! "C16-STALE"
                 "requested C2 key, entry, or validation binding is stale" {}))
  (let [context {:validation-role :requested-retained-artifact
                 :requested-cache-key-id (:requested-cache-key-id requested)
                 :validated-cache-key-id (:cache-key-id entry)}
        decoded (decode-adapter-envelope!
                 envelope legacy-key operations context nil)
        artifact (:artifact decoded)
        artifact-id ((:artifact-id-of operations) artifact)
        boundary-id ((:boundary-projection-id-of operations) artifact)
        current-bytes (canonical-artifact-bytes! artifact)]
    (when-not (and (= (:requested-artifact-id requested)
                      (:artifact-id entry)
                      (:c2-artifact-id envelope)
                      artifact-id)
                   (= (:boundary-projection-id envelope) boundary-id)
                   (sha256-id? (:carrier-id envelope))
                   (sha256-id? (:entry-id entry))
                   (sha256-id? (:producer-receipt-id entry))
                   (identical? (:payload-bytes envelope)
                               (:payload-bytes decoded))
                   (java.util.Arrays/equals
                    ^bytes (:payload-bytes envelope) ^bytes current-bytes))
      (cache-fail! "C16-STALE"
                   "requested C2 artifact changed during semantic validation"
                   {}))
    (let [binding
          {:artifact :gravity/c2-pass-cache-requested-hit-binding
           :schema-version 1
           :state :semantic-validated
           :invocation-token (:invocation-token @state)
           :operation operation
           :exact-generic-key generic-key
           :exact-entry entry
           :exact-envelope envelope
           :exact-artifact artifact
           :exact-payload-bytes (:payload-bytes envelope)
           :requested-cache-key-id (:requested-cache-key-id requested)
           :legacy-semantic-key-id (:semantic-key-id legacy-key)
           :legacy-storage-key-id (:storage-key-id legacy-key)
           :entry-id (:entry-id entry)
           :producer-receipt-id (:producer-receipt-id entry)
           :producer-binding-id (:producer-binding-id generic-key)
           :validation-binding-id validation-binding-id
           :requested-artifact-id (:requested-artifact-id requested)
           :c2-artifact-id artifact-id
           :boundary-projection-id boundary-id
           :carrier-id (:carrier-id envelope)
           :payload-id (:payload-id envelope)}]
      (swap! state assoc :requested-hit-binding binding)
      envelope)))

(defn- valid-requested-hit-binding?
  [state legacy-key operations result]
  (let [binding (:requested-hit-binding @state)
        envelope (:artifact result)
        artifact (:exact-artifact binding)
        entry (:cache-entry result)
        key (:key result)
        receipt (:producer-receipt result)
        reuse (:reuse-receipt result)
        payload (:payload-bytes envelope)
        current-artifact-id (when artifact
                              ((:artifact-id-of operations) artifact))
        current-boundary-id (when artifact
                              ((:boundary-projection-id-of operations)
                               artifact))
        current-bytes (when artifact (canonical-artifact-bytes! artifact))]
    (and (= :hit (:status result))
         (= requested-hit-binding-fields (set (keys binding)))
         (= :gravity/c2-pass-cache-requested-hit-binding
            (:artifact binding))
         (= 1 (:schema-version binding))
         (= :semantic-validated (:state binding))
         (identical? (:invocation-token @state)
                     (:invocation-token binding))
         (= (:operation @state) (:operation binding))
         (contains? #{:lookup :lookup-or-compute} (:operation binding))
         (identical? key (:exact-generic-key binding))
         (identical? entry (:exact-entry binding))
         (identical? envelope (:exact-envelope binding))
         (identical? payload (:exact-payload-bytes binding))
         (some? artifact)
         (= (:requested-cache-key-id binding)
            (:cache-key-id key)
            (:cache-key-id entry))
         (= (:legacy-semantic-key-id binding) (:semantic-key-id legacy-key))
         (= (:legacy-storage-key-id binding) (:storage-key-id legacy-key))
         (= (:entry-id binding) (:entry-id entry))
         (= (:producer-receipt-id binding)
            (:producer-receipt-id entry)
            (:receipt-id receipt)
            (:historical-producer-receipt-id reuse))
         (= (:producer-binding-id binding)
            (:producer-binding-id key))
         (= (:validation-binding-id binding)
            (:validation-binding-id entry)
            (:validation-binding-id reuse)
            (:validation-binding-id @state))
         (= (:requested-artifact-id binding)
            (:c2-artifact-id binding)
            (:artifact-id entry)
            (:output-artifact-id receipt)
            (:current-artifact-id reuse)
            (:c2-artifact-id envelope)
            current-artifact-id)
         (= (:boundary-projection-id binding)
            (:boundary-projection-id envelope)
            current-boundary-id)
         (= (:carrier-id binding) (:carrier-id envelope))
         (= (:payload-id binding) (:payload-id envelope)
            (str "sha256:" (digest/sha256-bytes-hex payload)))
         (java.util.Arrays/equals ^bytes payload ^bytes current-bytes))))
