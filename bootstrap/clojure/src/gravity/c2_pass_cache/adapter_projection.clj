;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- decode-adapter-envelope!
  [envelope legacy-key operations validate? expected-carrier-id]
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
  [legacy-key operations produce operation]
  (let [operations (required-validation-ops! operations)
        request (adapter-stage-request! legacy-key operations)
        validation-binding-id
        (adapter-projection-id
         :gravity/c2-pass-cache-v2-validation-binding
         {:current-binding (:current-binding operations)
          :legacy-semantic-key-id (:semantic-key-id legacy-key)
          :legacy-storage-key-id (:storage-key-id legacy-key)})
        generic-key (pass-cache/stage-cache-key request)
        token (Object.)
        state (atom {:invocation-token token :operation operation})]
    {:request request
     :key generic-key
     :invocation-state state
     :operations
     {:produce! (fn [_]
                  (when-not (fn? produce)
                    (cache-fail! "C16-KEY"
                                 "C2 cache compute operation is missing" {}))
                  (let [artifact (produce)
                        validated ((:validate-artifact! operations)
                                   artifact nil legacy-key)]
                    (when-not (identical? artifact validated)
                      (cache-fail!
                       "C16-STALE"
                       "C2 semantic validation must preserve artifact identity"
                       {}))
                    (let [carrier (prevalidated-carrier!
                                   artifact legacy-key request
                                   validation-binding-id operations)]
                      (swap! state assoc :carrier carrier)
                      (adapter-envelope! artifact carrier legacy-key operation
                                         state operations))))
      ;; Existing recovery retains its current semantic-validator ownership.
      :validate-artifact!
      (fn [envelope _ _]
        (:envelope
         (decode-adapter-envelope! envelope legacy-key operations true nil)))
      :validate-requested-retained-artifact!
      (fn [envelope _ _ requested]
        (when-not (and (= (:cache-key-id generic-key)
                          (:requested-cache-key-id requested))
                       (= (:c2-artifact-id envelope)
                          (:requested-artifact-id requested)))
          (cache-fail! "C16-STALE"
                       "requested C2 validation context is stale" {}))
        (:envelope
         (decode-adapter-envelope! envelope legacy-key operations true nil)))
      :validate-output!
      (fn [envelope _ _]
        ;; Fresh/direct semantic validation already happened before encoding.
        ;; This role validates only the closed envelope and carrier binding.
        (decode-adapter-envelope! envelope legacy-key operations false
                                  (get-in @state [:carrier :carrier-id]))
        envelope)
      :artifact-id-of
      (fn [envelope]
        (:c2-artifact-id
         (:envelope
          (decode-adapter-envelope! envelope legacy-key operations false
                                    nil))))
      :capacity-failure-handoff!
      (fn [error looked-up]
        (capacity-failure-handoff! state legacy-key generic-key operation
                                   error looked-up))
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
                       "C2 adapter contract does not admit evidence records" {}))}
     :operation operation}))

(defn- project-adapter-result
  [legacy-key operations state result]
  (let [envelope (:artifact result)
        carrier (:carrier @state)
        fresh? (and carrier (not= :hit (:status result)))
        _ (when (and fresh?
                     (not= (:carrier-id carrier) (:carrier-id envelope)))
            (cache-fail! "C16-STALE"
                         "fresh C2 result differs from its carrier" {}))
        artifact (cond
                   fresh? (:exact-artifact carrier)
                   envelope (:artifact
                             (decode-adapter-envelope!
                              envelope legacy-key operations false nil)))
        derived
        (when artifact
          (cond-> {:artifact-id (:c2-artifact-id envelope)
                   :blob-id (:payload-id envelope)}
            (get-in result [:cache-entry :entry-id])
            (assoc :entry-id (get-in result [:cache-entry :entry-id]))))]
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
  (let [{:keys [key operations invocation-state]}
        (adapter-operations! legacy-key validation-ops nil :lookup)]
    (project-adapter-result
     legacy-key validation-ops invocation-state
     (pass-cache/lookup! store key operations))))

(defn store!
  "Publish one supplied C2 artifact through a fresh generic v2 receipt."
  [store legacy-key artifact validation-ops]
  (let [{:keys [request key operations invocation-state]}
        (adapter-operations! legacy-key validation-ops (constantly artifact)
                             :store)]
    (try
      (let [execution (pass-execution/execute-pass!
                       request (select-keys operations
                                            [:produce! :validate-output!
                                             :artifact-id-of :verifier-reports
                                             :evidence-records]))
            result (pass-cache/store! store key (:artifact execution)
                                      (:receipt execution) operations)]
        (project-adapter-result legacy-key validation-ops invocation-state
                                result))
      (catch java.nio.channels.ClosedByInterruptException error
        (.interrupt (Thread/currentThread))
        (throw error))
      (catch InterruptedException error
        (.interrupt (Thread/currentThread))
        (throw error)))))

(defn lookup-or-compute!
  "Reuse or produce one C2 artifact through the generic v2 cache."
  [store legacy-key {:keys [compute!] :as validation-ops}]
  (let [{:keys [request key operations invocation-state]}
        (adapter-operations! legacy-key validation-ops compute!
                             :lookup-or-compute)]
    (try
      (let [result (pass-cache/lookup-or-compute!
                    store key request operations)]
        (project-adapter-result legacy-key validation-ops invocation-state
                                result))
      (catch java.nio.channels.ClosedByInterruptException error
        (.interrupt (Thread/currentThread))
        (throw error))
      (catch InterruptedException error
        (.interrupt (Thread/currentThread))
        (throw error))
      (catch clojure.lang.ExceptionInfo wrapper
        (capacity-withholding-result! legacy-key key invocation-state
                                      wrapper)))))
