;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
(def ^:private cache-schema-version 1)
(def ^:private canonicalizer-version 1)
(def ^:private cache-stage :c2-reader)
(def ^:private maximum-source-bytes 1048576)
(def ^:private maximum-canonical-depth 640)
(def ^:private maximum-canonical-nodes 524288)
(def ^:private maximum-encoded-bytes (* 32 1024 1024))
(def ^:private maximum-entry-bytes (* 4 1024 1024))
(def ^:private maximum-blob-bytes maximum-encoded-bytes)
(def ^:private maximum-entry-count 4096)
(def ^:private maximum-blob-count 4096)
(def ^:private maximum-lock-count 4097)
(def ^:private maximum-staging-count 1)
(def ^:private maximum-store-bytes (* 256 1024 1024))
(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")
(def ^:private owned-directory-permissions
  (PosixFilePermissions/fromString "rwx------"))
(def ^:private owned-file-permissions
  (PosixFilePermissions/fromString "rw-------"))
(def ^:private nofollow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private create-new-write-options
  (java.util.HashSet. [StandardOpenOption/CREATE_NEW
                       StandardOpenOption/WRITE
                       LinkOption/NOFOLLOW_LINKS]))
(def ^:private file-attribute
  (PosixFilePermissions/asFileAttribute owned-file-permissions))
(def ^:private directory-attribute
  (PosixFilePermissions/asFileAttribute owned-directory-permissions))

(def ^:private key-request-fields
  #{:source-unit :source-snapshot :reader-policy :project-binding
    :compiler-binding :pass-binding :dependency-binding
    :build-effect-binding :capability-binding :facet-binding
    :profile-binding :target-binding :boundary-binding :path-provenance})

;; JVM file locks reject overlapping acquisition from sibling threads instead
;; of blocking.  This local guard makes the subsequent OS lock usable by both
;; threads and independent processes without retry or lock bypass.
(def ^:private in-process-key-locks (atom {}))

(defn- acquire-in-process-key-lock!
  [path]
  (let [key (str path)
        entry
        (get
         (swap! in-process-key-locks
                (fn [locks]
                  (if-let [existing (get locks key)]
                    (assoc locks key (update existing :references inc))
                    (assoc locks key {:lock (ReentrantLock.)
                                      :references 1}))))
         key)]
    (.lock ^ReentrantLock (:lock entry))
    [key (:lock entry)]))

(defn- release-in-process-key-lock!
  [key ^ReentrantLock lock]
  (.unlock lock)
  (swap! in-process-key-locks
         (fn [locks]
           (if-let [entry (get locks key)]
             (if-not (identical? lock (:lock entry))
               locks
               (if (= 1 (:references entry))
                 (dissoc locks key)
                 (assoc locks key (update entry :references dec))))
             locks))))

(def ^:private cache-contract-record
  {:namespace 'gravity.c2-pass-cache
   :contract-boundary :hosted-c2-local-pass-cache-v2-adapter
   :public-api
   '#{cache-contract canonical-content-id bounded-source-snapshot!
      cache-key open-local-store lookup! store! lookup-or-compute!}
   :storage-root ".cpcache/compiler-pass/v2"
   :legacy-storage-root ".cpcache/compiler-pass/v1"
   :legacy-storage-policy :forensic-only-never-read-or-reinterpreted
   :adapter-artifact-policy
   {:encoding :opaque-c2-canonical-envelope
    :metadata-preserving? true
    :maximum-c2-canonical-bytes maximum-encoded-bytes
    :maximum-v2-envelope-bytes (* 48 1024 1024)}
   :semantic-stage :c2-reader
   :owns [:versioned-semantic-key
          :bounded-source-snapshot
          :c2-to-generic-pass-request-adaptation
          :opaque-metadata-preserving-artifact-envelope
          :generic-v2-producer-receipt-projection
          :local-atomic-locked-publication
          :hit-and-miss-evidence]
   :does-not-own [:c2-reader-authority
                  :c16-language-conformance
                  :reader-execution
                  :artifact-validation
                  :release-publication
                  :proof
                  :equivalence
                  :self-hosting
                  :same-user-out-of-band-mutation-safety]
   :authority {:local-development-only? true
               :release-authority? false
               :proof-authority? false
               :self-hosting-credit? false
               :clojure-seed-boundary? true}
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn 'gravity.digest
               'gravity.pass-cache 'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c16-incremental]}
   :threat-boundary
   {:cooperative-processes :descriptor-lock-and-immutable-cas
    :same-user-out-of-band-mutation-safety
    {:protected? false
     :classification :excluded-local-corruption
     :hostile-containment? false
     :no-replace-guarantee? false
     :detection :read-and-post-publication-integrity-checks}}})

(defn cache-contract
  "Return the static ownership, threat boundary, and nonclaims for this leaf.

  Descriptor locks make cooperative API writers immutable.  A hostile or
  out-of-band writer running as the same OS user is explicitly excluded: its
  corruption may be detected by integrity rechecks, but this local cache does
  not provide no-replace containment against it."
  []
  cache-contract-record)

(defn- cache-fail!
  [id message data]
  (throw (ex-info message (merge {:id id
                                  :stage cache-stage
                                  :cache-schema-version cache-schema-version}
                                 data))))

(defn- with-contained-host-errors
  [diagnostic-id operation thunk]
  (try
    (thunk)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch java.nio.channels.ClosedByInterruptException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      ;; Injected producer/validator diagnostics retain their owning stage and
      ;; cache diagnostics retain their stable C16 id.  Only raw host failures
      ;; are translated here.
      (throw error))
    (catch Exception error
      (cache-fail! diagnostic-id "cache host operation failed closed"
                   {:operation operation
                    :contained-host-error (.getName (class error))
                    :contained-host-message (.getMessage error)}))))

(defn- sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn- ensure-sha256-id!
  [field value]
  (when-not (sha256-id? value)
    (cache-fail! "C16-KEY" "cache identity must be lowercase SHA-256"
                 {:field field :observed value}))
  value)

(defn- metadata-bearing?
  [value]
  (and (instance? clojure.lang.IMeta value)
       (seq (meta value))))

(defn- integral-tag
  [value]
  (cond
    (instance? Byte value) "byte"
    (instance? Short value) "short"
    (instance? Integer value) "int"
    (instance? Long value) "long"
    (instance? clojure.lang.BigInt value) "bigint"
    (instance? BigInteger value) "biginteger"
    :else nil))

(declare encode-value)

(defn- encode-children
  [values state depth options]
  (mapv #(encode-value % state (inc depth) options) values))

(defn- canonical-node-text
  [node]
  (binding [*print-length* nil
            *print-level* nil
            *print-meta* false]
    (pr-str node)))

(defn- sorted-encoded
  [nodes]
  (->> nodes
       (map (fn [node] [(canonical-node-text node) node]))
       (sort-by first)
       (mapv second)))
