;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- legacy-v1-lookup!
  "Look up and fully revalidate one entry without executing its producer.

  Corrupt, stale, unknown, or malformed data returns explicit `:rejected`
  evidence and never an artifact."
  [store key validation-ops]
  (let [ops (required-validation-ops! validation-ops)]
    (with-key-lock
     store key
     (fn [directories]
       (lookup-unlocked! store directories key ops)))))

(defn- entry-record
  [key artifact artifact-id boundary-projection-id blob-id ops]
  {:artifact :gravity/compiler-pass-cache-entry
   :schema-version cache-schema-version
   :canonicalizer-version canonicalizer-version
   :stage cache-stage
   :semantic-key-id (:semantic-key-id key)
   :storage-key-id (:storage-key-id key)
   :cache-key key
   :blob-id blob-id
   :artifact-id artifact-id
   :boundary-projection-id boundary-projection-id
   :producer-binding (:current-binding ops)
   :inputs [(get-in key [:semantic-preimage :source-unit :source-id])
            (get-in key [:semantic-preimage :source-snapshot :bytes-hash])]
   :preserved-facts #{:source-spans :raw-literal-facts :reader-origin
                      :trivia :diagnostics :source-unit-identity}
   :invalidated-by #{:source-change :reader-policy-change
                     :project-root-change :dependency-change
                     :compiler-change :pass-contract-change
                     :sh03-binding-change :build-effect-change
                     :capability-policy-change :facet-change}
   :diagnostics
   (get-in artifact [:incremental-reader-hashes :reader-diagnostics])
   :trust :local-development
   :revalidation :required
   :release-authority? false
   :proof-authority? false
   :self-hosted? false})

(defn- ensure-publication-admitted!
  [directories inventory blob-name blob-bytes entry-name entry-bytes]
  (let [new-blob? (not (secure-child-exists?
                        (:blobs directories) blob-name))
        new-entry? (not (secure-child-exists?
                         (:entries directories) entry-name))
        next-blob-count (+ (get-in inventory [:blobs :count])
                           (if new-blob? 1 0))
        next-entry-count (+ (get-in inventory [:entries :count])
                            (if new-entry? 1 0))
        next-bytes (+ (:aggregate-bytes inventory)
                      (if new-blob? (alength ^bytes blob-bytes) 0)
                      (if new-entry? (alength ^bytes entry-bytes) 0))]
    (when-not (and (<= next-blob-count maximum-blob-count)
                   (<= next-entry-count maximum-entry-count)
                   (<= next-bytes maximum-store-bytes))
      (cache-fail! "C16-POLICY" "cache publication exceeds store policy"
                   {:next-blob-count next-blob-count
                    :maximum-blob-count maximum-blob-count
                    :next-entry-count next-entry-count
                    :maximum-entry-count maximum-entry-count
                    :next-aggregate-bytes next-bytes
                    :maximum-aggregate-bytes maximum-store-bytes})))
  :admitted)

(defn- store-unlocked!
  [store directories inventory key artifact ops]
  (let [validated ((:validate-artifact! ops) artifact nil key)
        artifact-id ((:artifact-id-of ops) validated)
        _ (ensure-sha256-id! :artifact-id artifact-id)
        boundary-projection-id ((:boundary-projection-id-of ops) validated)
        _ (ensure-sha256-id! :boundary-projection-id boundary-projection-id)
        blob-bytes (encode-canonical-bytes validated
                                           {:reject-metadata? false}
                                           maximum-blob-bytes)
        blob-id (str "sha256:" (digest/sha256-bytes-hex blob-bytes))
        entry (entry-record key validated artifact-id boundary-projection-id
                            blob-id ops)
        entry-bytes (encode-canonical-bytes entry
                                            {:reject-metadata? false}
                                            maximum-entry-bytes)
        blob-name (relative-name (id-filename blob-id))
        entry-name (relative-name (id-filename (:storage-key-id key)))
        _ (ensure-publication-admitted!
           directories inventory blob-name blob-bytes entry-name entry-bytes)
        blob-status (publish-create-or-verify!
                     store directories :blobs (:blobs directories) blob-name
                     blob-bytes maximum-blob-bytes)
        entry-status (publish-create-or-verify!
                      store directories :entries (:entries directories)
                      entry-name entry-bytes maximum-entry-bytes)
        post-publication-inventory (fresh-store-inventory! store)]
    {:artifact validated
     :cache-evidence
     (lookup-evidence key :stored :validated-success
                      {:reader-executed? true
                       :artifact-reused? false
                       :artifact-id artifact-id
                       :blob-id blob-id
                       :blob-publication blob-status
                       :entry-publication entry-status
                       :post-publication-inventory
                       post-publication-inventory})}))

(defn- legacy-v1-store!
  "Validate and immutably publish one accepted artifact, blob first.

  Existing identical bytes from cooperative API writers converge, and their
  same-address conflicts fail closed under descriptor locks.  Same-user
  out-of-band mutation safety is an explicit nonclaim.  Producer failures
  cannot reach this function through `lookup-or-compute!`."
  [store key artifact validation-ops]
  (let [ops (required-validation-ops! validation-ops)]
    (with-key-lock
     store key
     (fn [_directories]
       (with-global-store-lock
        store
        (fn [directories]
          (store-unlocked!
           store directories (secure-store-inventory! store directories)
           key artifact ops)))))))

(defn- legacy-v1-lookup-or-compute!
  "Return a validated hit or compute and store a successful miss.

  `:compute!` is invoked only for a miss/rejected entry.  A rejected existing
  entry is never replaced: the fresh result is validated and returned with
  publication withheld, preserving immutable corruption evidence."
  [store key {:keys [compute!] :as operations}]
  (when-not (fn? compute!)
    (cache-fail! "C16-KEY" "cache compute operation is missing" {}))
  (let [ops (required-validation-ops! operations)]
    (with-key-lock
     store key
     (fn [directories]
       (let [looked-up (lookup-unlocked! store directories key ops)
             status (get-in looked-up [:cache-evidence :status])]
         (if (= :hit status)
           looked-up
           (let [artifact (compute!)
                 validated ((:validate-artifact! ops) artifact nil key)]
             (if (= :miss status)
               (with-global-store-lock
                store
                (fn [publication-directories]
                  (let [publication-inventory
                        (secure-store-inventory!
                         store publication-directories)
                        rechecked
                        (lookup-unlocked! store publication-directories key ops)
                        rechecked-status
                        (get-in rechecked [:cache-evidence :status])]
                    (cond
                      (= :miss rechecked-status)
                      (store-unlocked!
                       store publication-directories publication-inventory
                       key validated ops)

                      (= :hit rechecked-status)
                      (let [computed-id ((:artifact-id-of ops) validated)
                            reused-id ((:artifact-id-of ops)
                                       (:artifact rechecked))]
                        (when-not (= computed-id reused-id)
                          (cache-fail!
                           "C16-ENTRY"
                           "same semantic key produced conflicting artifacts"
                           {:computed-artifact-id computed-id
                            :reused-artifact-id reused-id}))
                        (update rechecked :cache-evidence
                                assoc
                                :reason :populated-before-publication
                                :reader-executed? true))

                      :else
                      {:artifact validated
                       :cache-evidence
                       (lookup-evidence
                        key :miss :publication-recheck-rejected
                        {:reader-executed? true
                         :artifact-reused? false
                         :cache-publication :withheld
                         :rejected-entry-evidence
                         (:cache-evidence rechecked)
                         :artifact-id
                         ((:artifact-id-of ops) validated)})}))))
               {:artifact validated
                :cache-evidence
                (lookup-evidence
                 key :miss :existing-entry-rejected
                 {:reader-executed? true
                  :artifact-reused? false
                  :cache-publication :withheld
                  :rejected-entry-evidence (:cache-evidence looked-up)
                  :artifact-id ((:artifact-id-of ops) validated)})}))))))))

;; ---------------------------------------------------------------------------
;; Generic v2 storage adapter
;; ---------------------------------------------------------------------------
