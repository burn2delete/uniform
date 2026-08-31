;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- decode-adapter-envelope!
  [envelope legacy-key operations validate?]
  (when-not (and (map? envelope)
                 (= adapter-envelope-fields (set (keys envelope)))
                 (= :gravity/c2-pass-cache-v2-envelope (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= adapter-version (:adapter-version envelope))
                 (sha256-id? (:c2-artifact-id envelope))
                 (sha256-id? (:boundary-projection-id envelope))
                 (= (class (byte-array 0)) (class (:payload-bytes envelope))))
    (cache-fail! "C16-ENTRY" "C2 v2 artifact envelope is malformed" {}))
  (let [payload ^bytes (:payload-bytes envelope)
        payload-id (str "sha256:" (digest/sha256-bytes-hex payload))
        _ (when-not (= payload-id (:payload-id envelope))
            (cache-fail! "C16-STALE"
                         "C2 v2 envelope payload identity is stale" {}))
        artifact (decode-canonical-bytes payload maximum-blob-bytes)
        artifact-id ((:artifact-id-of operations) artifact)
        boundary-id ((:boundary-projection-id-of operations) artifact)
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
         (get-in artifact [:incremental-reader-hashes :reader-diagnostics])}
        artifact (if validate?
                   ((:validate-artifact! operations)
                    artifact compatibility-entry legacy-key)
                   artifact)]
    {:envelope envelope :artifact artifact}))

(defn- adapter-operations!
  [legacy-key operations produce]
  (let [operations (required-validation-ops! operations)
        request (adapter-stage-request! legacy-key operations)
        validation-binding-id
        (adapter-projection-id
         :gravity/c2-pass-cache-v2-validation-binding
         {:current-binding (:current-binding operations)
          :legacy-semantic-key-id (:semantic-key-id legacy-key)
          :legacy-storage-key-id (:storage-key-id legacy-key)})]
    {:request request
     :key (pass-cache/stage-cache-key request)
     :operations
     {:produce! (fn [_]
                  (when-not (fn? produce)
                    (cache-fail! "C16-KEY"
                                 "C2 cache compute operation is missing" {}))
                  ;; The generic execution validator owns the one validation
                  ;; call.  Envelope construction only snapshots the produced
                  ;; C2 value and its injected identities.
                  (let [artifact (produce)]
                    (adapter-envelope! artifact legacy-key operations nil)))
      :validate-output!
      (fn [envelope _ _]
        ;; Validate the opaque payload here so producer receipts never attest
        ;; bytes that differ from the currently accepted C2 value.
        (decode-adapter-envelope! envelope legacy-key operations true)
        envelope)
      :artifact-id-of
      (fn [envelope]
        (:c2-artifact-id
         (:envelope
          (decode-adapter-envelope! envelope legacy-key operations false))))
      :validation-binding-id validation-binding-id
      :verifier-reports (fn [& _] [])
      :evidence-records (fn [& _] [])
      :validate-diagnostic-stream!
      (fn [stream-id receipt]
        (when-not (and (= stream-id (:diagnostic-stream-id request))
                       (= stream-id (:diagnostic-stream-id receipt)))
          (cache-fail! "C16-STALE"
                       "C2 adapter diagnostic stream binding is stale" {})))
      :validate-verifier-report!
      (fn [& _]
        (cache-fail! "C18-EVIDENCE"
                     "C2 adapter contract does not admit verifier reports" {}))
      :validate-evidence-record!
      (fn [& _]
        (cache-fail! "C18-EVIDENCE"
                     "C2 adapter contract does not admit evidence records" {}))}}))

(defn- legacy-inventory-projection
  [inventory]
  (when (map? inventory)
    (let [selected (select-keys inventory [:entries :blobs :locks :staging])]
      (assoc selected :aggregate-bytes
             (reduce + 0 (map #(long (or (:bytes %) 0)) (vals selected)))))))

(defn- legacy-evidence
  [legacy-key result derived]
  (let [status (or (:status result)
                   (get-in result [:cache-evidence :status]))
        generic-evidence (:cache-evidence result)
        reason (case status
                 :hit :validated-reuse
                 :stored :validated-success
                 :miss (if (:rejected-entry-evidence generic-evidence)
                         :existing-entry-rejected
                         :entry-not-found)
                 :rejected :entry-revalidation-failed
                 :entry-revalidation-failed)]
    (lookup-evidence
     legacy-key status reason
     (cond->
      {:reader-executed? (boolean (:reader-executed? generic-evidence))
       :artifact-reused? (boolean (:artifact-reused? generic-evidence))}
       (:artifact-id derived)
       (assoc :artifact-id (:artifact-id derived))

       (:blob-id derived)
       (assoc :blob-id (:blob-id derived))

       (:entry-id derived)
       (assoc :entry-id (:entry-id derived))

       (:blob-publication generic-evidence)
       (assoc :blob-publication (:blob-publication generic-evidence))

       (:entry-publication generic-evidence)
       (assoc :entry-publication (:entry-publication generic-evidence))

       (:post-publication-inventory generic-evidence)
       (assoc :post-publication-inventory
              (legacy-inventory-projection
               (:post-publication-inventory generic-evidence)))

       (:contained-diagnostic generic-evidence)
       (assoc :contained-diagnostic
              (:contained-diagnostic generic-evidence))

       (:rejected-entry-evidence generic-evidence)
       (assoc :cache-publication :withheld
              :rejected-entry-evidence
              (:rejected-entry-evidence generic-evidence))))))

(defn- project-adapter-result
  [legacy-key operations result]
  (let [envelope (:artifact result)
        artifact (when envelope
                   (:artifact
                    (decode-adapter-envelope! envelope legacy-key operations
                                              false)))
        derived
        (when artifact
          (let [artifact-id ((:artifact-id-of operations) artifact)
                boundary-id ((:boundary-projection-id-of operations) artifact)
                blob-bytes (encode-canonical-bytes
                            artifact {:reject-metadata? false}
                            maximum-blob-bytes)
                blob-id (str "sha256:" (digest/sha256-bytes-hex blob-bytes))
                entry (entry-record legacy-key artifact artifact-id boundary-id
                                    blob-id operations)
                entry-bytes (encode-canonical-bytes
                             entry {:reject-metadata? false}
                             maximum-entry-bytes)]
            {:artifact-id artifact-id
             :blob-id blob-id
             :entry-id
             (str "sha256:" (digest/sha256-bytes-hex entry-bytes))}))]
    (cond-> {:cache-evidence (legacy-evidence legacy-key result derived)}
      artifact (assoc :artifact artifact))))

;; These definitions intentionally replace only the four physical-storage
;; entrypoints above.  The legacy canonical key and source-snapshot APIs remain
;; byte-for-byte v1 compatible, while every new read/write is isolated under
;; generic `.cpcache/compiler-pass/v2`.  Existing v1 bytes are never opened.
(defn open-local-store
  "Open the generic v2 local store used by the C2 compatibility adapter."
  [base-path]
  (let [base (normalized-absolute-path! base-path)]
    (ensure-base-directory! base)
    (pass-cache/open-local-store base)))

(defn lookup!
  "Look up one C2 artifact through receipt-first generic v2 revalidation."
  [store legacy-key validation-ops]
  (let [{:keys [key operations]}
        (adapter-operations! legacy-key validation-ops nil)]
    (project-adapter-result
     legacy-key validation-ops (pass-cache/lookup! store key operations))))

(defn store!
  "Publish one supplied C2 artifact through a fresh generic v2 receipt."
  [store legacy-key artifact validation-ops]
  (let [{:keys [request key operations]}
        (adapter-operations! legacy-key validation-ops (constantly artifact))
        execution (pass-execution/execute-pass!
                   request (select-keys operations
                                        [:produce! :validate-output!
                                         :artifact-id-of :verifier-reports
                                         :evidence-records]))
        result (pass-cache/store! store key (:artifact execution)
                                  (:receipt execution) operations)]
    (project-adapter-result legacy-key validation-ops result)))

(defn lookup-or-compute!
  "Reuse or produce one C2 artifact through the generic v2 cache."
  [store legacy-key {:keys [compute!] :as validation-ops}]
  (let [{:keys [request key operations]}
        (adapter-operations! legacy-key validation-ops compute!)
        result (pass-cache/lookup-or-compute! store key request operations)]
    (project-adapter-result legacy-key validation-ops result)))
