(ns gravity.pass-cache.policy
  "Shared cache bounds, ownership contract, authority, and diagnostics."
  (:import [java.nio.file LinkOption StandardOpenOption]
           [java.nio.file.attribute PosixFilePermissions]
           [java.util HashSet]
           [java.util.concurrent.locks ReentrantLock]))

(def schema-version 2)
(def canonicalizer-version 2)
(def cache-root-relative [".cpcache" "compiler-pass" "v2"])
(def ^:dynamic maximum-depth 96)
(def ^:dynamic maximum-nodes 32768)
(def ^:dynamic maximum-file-bytes (* 48 1024 1024))
(def ^:dynamic maximum-canonical-bytes maximum-file-bytes)
(def ^:dynamic maximum-entry-bytes (* 4 1024 1024))
(def ^:dynamic maximum-blob-bytes maximum-file-bytes)
(def ^:dynamic maximum-entry-count 8192)
(def ^:dynamic maximum-blob-count 8192)
(def ^:dynamic maximum-receipt-count 8192)
(def ^:dynamic maximum-lock-count 8193)
(def ^:dynamic maximum-staging-count 8192)
(def ^:dynamic maximum-store-bytes (* 512 1024 1024))
(def store-policy
  {:maximum-entry-count maximum-entry-count
   :maximum-blob-count maximum-blob-count
   :maximum-receipt-count maximum-receipt-count
   :maximum-lock-count maximum-lock-count
   :maximum-staging-count maximum-staging-count
   :maximum-aggregate-bytes maximum-store-bytes
   :maximum-entry-bytes maximum-entry-bytes
   :maximum-blob-bytes maximum-blob-bytes})
(def sha256-pattern #"sha256:[0-9a-f]{64}")
(def authority-rank
  {:none 0 :non-authoritative 1 :reviewed 2 :authoritative 3})
(def nofollow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def private-directory-permissions
  (PosixFilePermissions/fromString "rwx------"))
(def private-file-permissions
  (PosixFilePermissions/fromString "rw-------"))
(def private-directory-attribute
  (PosixFilePermissions/asFileAttribute private-directory-permissions))
(def private-file-attribute
  (PosixFilePermissions/asFileAttribute private-file-permissions))
(def create-new-write-options
  (HashSet. [StandardOpenOption/CREATE_NEW
             StandardOpenOption/WRITE
             LinkOption/NOFOLLOW_LINKS]))
(def in-process-key-locks (atom {}))
(def ^:dynamic *key-lock-held* false)
(def publication-lock (ReentrantLock.))
(def store-bootstrap-lock (ReentrantLock.))
(def active-staging (atom #{}))
(def ^:dynamic *store-lock-held* false)
(def ^:dynamic *publication-hook* nil)

(def execution-request-fields
  #{:stage :contract :producer-binding-id :input-artifact-ids :input-facts
    :external-root-inputs :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-id :execution-mode :authority})

(def local-store-fields
  #{:base :cpcache :compiler-pass :root :blobs :entries :receipts :locks
    :staging :schema-version :store-policy :directory-identities})

(def pass-cache-public-api
  {'pass-cache-contract {:arglists '([])}
   'stage-cache-key {:arglists '([request])}
   'open-local-store {:arglists '([base-path])}
   'lookup! {:arglists '([store key validation-ops])}
   'store! {:arglists '([store key artifact producer-receipt validation-ops])}
   'lookup-or-compute! {:arglists '([store key execution-request operations])}})

(def pass-cache-contract-record
  {:namespace 'gravity.pass-cache
   :contract-boundary :hosted-generic-local-pass-cache-v2
   :public-api pass-cache-public-api
   :storage-root ".cpcache/compiler-pass/v2"
   :semantic-stage :generic-compiler-pass
   :authoritative? false
   :cache-storage? true
   :pass-implementation? false
   :proof-authority? false
   :release-authority? false
   :self-hosting-authority? false
   :equivalence-authority? false
   :owns [:bounded-semantic-stage-key
          :immutable-content-addressed-artifact-blobs
          :immutable-cache-entries
          :immutable-producer-receipts
          :receipt-first-revalidation
          :local-concurrent-publication]
   :does-not-own [:pass-implementation
                  :compiler-authority
                  :profile-authority
                  :artifact-semantics
                  :proof-authority
                  :release-publication
                  :equivalence-authority
                  :self-hosting-authority
                  :same-user-out-of-band-mutation-safety]
   :dependency-direction
   {:requires ['clojure.core 'clojure.edn 'clojure.set 'gravity.digest
               'gravity.pass-execution]
    :forbids ['gravity.bootstrap 'gravity.c2-pass-cache
              'gravity.c16-incremental]}
   :authority {:local-development-only? true
               :speculative-only? true
               :authoritative? false
               :release-authority? false
               :proof-authority? false
               :equivalence-authority? false
               :self-hosting-authority? false
               :release? false
               :proof? false
               :equivalence? false
               :self-hosting? false
               :clojure-seed-boundary? true}
   :threat-boundary
   {:cooperative-processes :per-key-lock-and-immutable-cas
    :out-of-band-same-user-mutation :detect-and-reject
    :v1-isolation :never-read-or-reinterpret
    :filesystem-provider-boundary
    {:secure-directory-stream-required? true
     :secure-posix-basic-views-required? true
     :anchored-atomic-rename-provider "sun.nio.fs.UnixSecureDirectoryStream"
     :durable-file-and-directory-fsync-required? true}}}
  )

(defn pass-cache-contract
  "Return the non-authoritative ownership and dependency contract."
  []
  pass-cache-contract-record)

(defn fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :stage :pass-cache
                          :remediation
                          "validate the pass request, receipt, and cache entry"
                          :release-authority? false
                          :proof-authority? false
                          :equivalence-authority? false
                          :self-hosting-authority? false}
                         data))))

(defn fatal?
  [error]
  (or (instance? ThreadDeath error)
      (instance? VirtualMachineError error)
      (instance? InterruptedException error)))

(defn sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "semantic identity must be lowercase SHA-256"
           {:field field :observed value}))
  value)

(defn require-keyword-set!
  [field value]
  (when-not (and (set? value) (every? keyword? value))
    (fail! "C16-KEY" "semantic fact fields must be keyword sets"
           {:field field :observed value}))
  value)

(defn sorted-sha-vector!
  [field value]
  (when-not (vector? value)
    (fail! "C16-KEY" "semantic identity list must be a vector"
           {:field field :observed value}))
  (when (or (> (count value) maximum-nodes)
            (not (every? sha256-id? value))
            (not= (count value) (count (distinct value)))
            (not= value (vec (sort value))))
    (fail! "C16-KEY" "semantic identity list is malformed"
           {:field field :observed value}))
  value)

(defn weakest-authority
  [levels]
  (first (sort-by authority-rank levels)))

(defn validate-authority!
  [authority input-artifact-ids ceiling]
  (when-not (and (map? authority)
                 (= #{:input-authorities :claimed-level :scope}
                    (set (keys authority))))
    (fail! "C16-POLICY" "pass authority binding is incomplete" {}))
  (let [bindings (:input-authorities authority)]
    (when-not (and (map? bindings)
                   (= (set input-artifact-ids) (set (keys bindings))))
      (fail! "C16-POLICY" "pass authority must bind exact input ids" {}))
    (doseq [[artifact-id level] bindings]
      (require-sha256! :input-authority-artifact-id artifact-id)
      (when-not (contains? authority-rank level)
        (fail! "C16-POLICY" "unknown authority level" {:level level})))
    (when-not (contains? authority-rank (:claimed-level authority))
      (fail! "C16-POLICY" "unknown claimed authority level" {}))
    (when (> (authority-rank (:claimed-level authority))
             (authority-rank (weakest-authority
                              (conj (vec (vals bindings)) ceiling))))
      (fail! "C16-POLICY" "pass authority widens its input or contract ceiling"
             {:claimed (:claimed-level authority) :ceiling ceiling}))
    (let [scope (:scope authority)]
      (when-not (or (and (keyword? scope) (seq (name scope)))
                    (and (string? scope)
                         (some #(not (Character/isWhitespace ^char %)) scope)))
        (fail! "C16-POLICY" "authority scope must be explicit and nonblank" {}))))
  authority)
