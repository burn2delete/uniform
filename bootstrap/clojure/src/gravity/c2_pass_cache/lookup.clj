;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(defn- bytes-equal?
  [left right]
  (java.util.Arrays/equals ^bytes left ^bytes right))

(defn- secure-publish-move!
  [^SecureDirectoryStream staging ^Path temporary
   ^SecureDirectoryStream destination-directory ^Path destination]
  (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class staging)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination-directory))))
    (cache-fail! "C16-POLICY"
                 "filesystem provider cannot guarantee anchored atomic rename"
                 {:source-provider (.getName (class staging))
                  :destination-provider
                  (.getName (class destination-directory))}))
  (.move staging temporary destination-directory destination))

(defn- publish-create-or-verify!
  [store directories path-key ^SecureDirectoryStream directory
   ^Path destination bytes maximum-bytes]
  (if (secure-child-exists? directory destination)
    (let [existing (secure-read-bytes!
                    store path-key directory destination maximum-bytes)]
      (when-not (bytes-equal? existing bytes)
        (cache-fail! "C16-ENTRY"
                     "immutable cache destination contains different bytes"
                     {:path-key path-key :name (str destination)}))
      :verified-identical)
    (let [^SecureDirectoryStream staging (:staging directories)
          temporary (relative-name (str ".stage-" (UUID/randomUUID) ".tmp"))]
      (try
        (secure-write-new! store :staging staging temporary bytes)
        ;; The destination is checked again after the complete staged bytes are
        ;; durable.  Cooperative writers hold the global descriptor lock, so
        ;; the descriptor-relative move cannot replace a cooperative result.
        ;; SecureDirectoryStream.move is Unix renameat and has no no-replace
        ;; option.  Cooperative API writers cannot race because they hold the
        ;; global descriptor lock.  A same-user out-of-band writer is outside
        ;; this local-development threat boundary: post-move verification can
        ;; detect corruption but cannot promise hostile no-replace containment.
        (if (secure-child-exists? directory destination)
          (let [existing (secure-read-bytes!
                          store path-key directory destination maximum-bytes)]
            (when-not (bytes-equal? existing bytes)
              (cache-fail! "C16-ENTRY"
                           "concurrent immutable cache publication conflicts"
                           {:path-key path-key :name (str destination)}))
            :converged-identical)
          (do
            (secure-publish-move! staging temporary directory destination)
            (secure-fsync-directory! staging)
            (secure-fsync-directory! directory)
            (let [published (secure-read-bytes!
                             store path-key directory destination
                             maximum-bytes)]
              (when-not (bytes-equal? published bytes)
                (cache-fail! "C16-ENTRY"
                             "atomic cache publication bytes changed"
                             {:path-key path-key :name (str destination)})))
            :published))
        (finally
          (when (secure-child-exists? staging temporary)
            (.deleteFile staging temporary)
            (secure-fsync-directory! staging)))))))

(defn- lookup-evidence
  [key status reason extra]
  (merge
   {:artifact :gravity/c2-local-pass-cache-evidence
    :schema-version cache-schema-version
    :stage cache-stage
    :semantic-key-id (:semantic-key-id key)
    :storage-key-id (:storage-key-id key)
    :status status
    :reason reason
    :local-development-only? true
    :release-authority? false
    :proof-authority? false
    :equivalence-authority? false
    :self-hosting-credit? false}
   extra))

(defn- required-validation-ops!
  [ops]
  (when-not (and (map? ops)
                 (map? (:current-binding ops))
                 (ifn? (:artifact-id-of ops))
                 (ifn? (:boundary-projection-id-of ops))
                 (ifn? (:validate-artifact! ops)))
    (cache-fail! "C16-KEY" "cache validation operations are incomplete"
                 {:required [:current-binding :artifact-id-of
                             :boundary-projection-id-of
                             :validate-artifact!]}))
  ops)

(defn- validate-entry-shape!
  [entry key current-binding]
  (when-not (and (map? entry)
                 (= :gravity/compiler-pass-cache-entry (:artifact entry))
                 (= cache-schema-version (:schema-version entry))
                 (= canonicalizer-version (:canonicalizer-version entry))
                 (= cache-stage (:stage entry))
                 (= (:semantic-key-id key) (:semantic-key-id entry))
                 (= (:storage-key-id key) (:storage-key-id entry))
                 (= key (:cache-key entry))
                 (= current-binding (:producer-binding entry))
                 (sha256-id? (:blob-id entry))
                 (sha256-id? (:artifact-id entry))
                 (sha256-id? (:boundary-projection-id entry))
                 (= :local-development (:trust entry))
                 (= :required (:revalidation entry))
                 (false? (:release-authority? entry))
                 (false? (:self-hosted? entry)))
    (cache-fail! "C16-ENTRY" "cache entry schema or binding is stale"
                 {:observed-schema (:schema-version entry)
                  :observed-stage (:stage entry)}))
  entry)

(defn- lookup-unlocked!
  [store directories key ops]
  (let [entry-file (relative-name
                    (id-filename (:storage-key-id key)))
        ^SecureDirectoryStream entry-directory (:entries directories)]
    (if-not (secure-child-exists? entry-directory entry-file)
      {:cache-evidence
       (lookup-evidence key :miss :entry-not-found
                        {:reader-executed? false
                         :artifact-reused? false})}
      (try
        (let [entry-bytes (secure-read-bytes!
                           store :entries entry-directory entry-file
                           maximum-entry-bytes)
              entry (decode-canonical-bytes entry-bytes maximum-entry-bytes)
              _ (validate-entry-shape! entry key (:current-binding ops))
              blob-id (:blob-id entry)
              blob-file (relative-name (id-filename blob-id))
              ^SecureDirectoryStream blob-directory (:blobs directories)
              _ (when-not (secure-child-exists? blob-directory blob-file)
                  (cache-fail! "C16-ENTRY" "cache entry blob is missing"
                               {:blob-id blob-id}))
              blob-bytes (secure-read-bytes!
                          store :blobs blob-directory blob-file
                          maximum-blob-bytes)
              observed-blob-id
              (str "sha256:" (digest/sha256-bytes-hex blob-bytes))
              _ (when-not (= blob-id observed-blob-id)
                  (cache-fail! "C16-ENTRY" "cache blob hash is corrupt"
                               {:expected blob-id :observed observed-blob-id}))
              artifact (decode-canonical-bytes blob-bytes maximum-blob-bytes)
              artifact-id ((:artifact-id-of ops) artifact)
              _ (when-not (= (:artifact-id entry) artifact-id)
                  (cache-fail! "C16-STALE"
                               "cached artifact identity does not match its entry"
                               {:expected (:artifact-id entry)
                                :observed artifact-id}))
              boundary-projection-id
              ((:boundary-projection-id-of ops) artifact)
              _ (ensure-sha256-id! :boundary-projection-id
                                   boundary-projection-id)
              _ (when-not (= (:boundary-projection-id entry)
                             boundary-projection-id)
                  (cache-fail!
                   "C16-STALE"
                   "cached C2 boundary projection does not match its entry"
                   {:expected (:boundary-projection-id entry)
                    :observed boundary-projection-id}))
              validated ((:validate-artifact! ops) artifact entry key)]
          {:artifact validated
           :cache-evidence
           (lookup-evidence key :hit :validated-reuse
                            {:reader-executed? false
                             :artifact-reused? true
                             :artifact-id artifact-id
                             :blob-id blob-id
                             :entry-id
                             (str "sha256:"
                                  (digest/sha256-bytes-hex entry-bytes))})})
        (catch InterruptedException interrupted
          (.interrupt (Thread/currentThread))
          (throw interrupted))
        (catch ThreadDeath fatal
          (throw fatal))
        (catch VirtualMachineError fatal
          (throw fatal))
        (catch Throwable error
          (let [diagnostic-id (:id (ex-data error))]
            {:cache-evidence
             (lookup-evidence
              key :rejected :entry-revalidation-failed
              (cond->
               {:reader-executed? false
                :artifact-reused? false
                :contained-diagnostic (or diagnostic-id "C16-ENTRY")}
                (nil? diagnostic-id)
                (assoc :contained-host-error
                       (.getName (class error)))))}))))))
