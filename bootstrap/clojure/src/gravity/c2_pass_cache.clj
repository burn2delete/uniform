(ns gravity.c2-pass-cache
  "A bounded, persistent local-development cache for accepted C2 reader artifacts.

  This hosted Clojure leaf owns a versioned semantic key and a local immutable
  entry/blob store.  It does not own C2 or C16 language authority, reader
  execution, artifact validation, release publication, proof, equivalence, or
  self-hosting.  Those decisions are injected by the bootstrap integration and
  are rerun before every hit is returned."
  (:require [clojure.edn :as edn]
            [gravity.digest :as digest])
  (:import [java.math BigDecimal BigInteger]
           [java.nio ByteBuffer]
           [java.nio.channels FileChannel SeekableByteChannel]
           [java.nio.charset StandardCharsets]
           [java.nio.file DirectoryStream Files LinkOption Path Paths
            SecureDirectoryStream StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes BasicFileAttributeView
            FileAttribute PosixFileAttributeView PosixFileAttributes
            PosixFilePermissions]
           [java.util Base64 Date HashSet UUID]
           [java.util.concurrent.locks ReentrantLock]))

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
   :contract-boundary :hosted-c2-local-pass-cache-v1
   :public-api
   '#{cache-contract canonical-content-id bounded-source-snapshot!
      cache-key open-local-store lookup! store! lookup-or-compute!}
   :storage-root ".cpcache/compiler-pass/v1"
   :semantic-stage :c2-reader
   :owns [:versioned-semantic-key
          :bounded-source-snapshot
          :immutable-entry-and-blob-cas
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

(defn- encode-value
  [value state depth {:keys [reject-metadata?] :as options}]
  (let [nodes (swap! state inc)]
    (when (> nodes maximum-canonical-nodes)
      (cache-fail! "C16-KEY" "canonical value exceeds the node bound"
                   {:maximum-nodes maximum-canonical-nodes}))
    (when (> depth maximum-canonical-depth)
      (cache-fail! "C16-KEY" "canonical value exceeds the depth bound"
                   {:maximum-depth maximum-canonical-depth}))
    (when (and reject-metadata? (metadata-bearing? value))
      (cache-fail! "C16-KEY"
                   "semantic cache keys cannot depend on host metadata"
                   {:value-class (.getName (class value))}))
    (if (and (not reject-metadata?) (metadata-bearing? value))
      [:meta
       (encode-value (meta value) state (inc depth) options)
       (encode-value (with-meta value nil) state (inc depth) options)]
      (cond
        (nil? value) [:nil]
        (true? value) [:boolean true]
        (false? value) [:boolean false]
        (string? value) [:string value]
        (char? value) [:character (int value)]
        (keyword? value) [:keyword (namespace value) (name value)]
        (symbol? value) [:symbol (namespace value) (name value)]

        (integral-tag value)
        [:integer (integral-tag value) (str value)]

        (ratio? value)
        [:ratio (str (numerator value)) (str (denominator value))]

        (instance? BigDecimal value)
        [:bigdecimal (.toString ^BigDecimal value)]

        (instance? Double value)
        (if (Double/isFinite ^Double value)
          [:double (str (Double/doubleToRawLongBits ^Double value))]
          (cache-fail! "C16-KEY" "nonfinite floating values are not canonical"
                       {:value-class "java.lang.Double"}))

        (instance? Float value)
        (if (Float/isFinite ^Float value)
          [:float (str (Float/floatToRawIntBits ^Float value))]
          (cache-fail! "C16-KEY" "nonfinite floating values are not canonical"
                       {:value-class "java.lang.Float"}))

        (instance? UUID value) [:uuid (str value)]

        (= Date (class value))
        [:date (.getTime ^Date value)]

        (= (class (byte-array 0)) (class value))
        [:bytes (.encodeToString (Base64/getEncoder) ^bytes value)]

        (record? value)
        (cache-fail! "C16-KEY" "records are not supported canonical values"
                     {:value-class (.getName (class value))})

        (map? value)
        (let [entries
              (mapv (fn [[key item]]
                      [(encode-value key state (inc depth) options)
                       (encode-value item state (inc depth) options)])
                    value)]
          [:map (sorted-encoded entries)])

        (set? value)
        [:set (sorted-encoded (encode-children value state depth options))]

        (vector? value)
        [:vector (encode-children value state depth options)]

        (seq? value)
        [:list (encode-children value state depth options)]

        :else
        (cache-fail! "C16-KEY" "unsupported value in canonical cache data"
                     {:value-class (.getName (class value))})))))

(defn- canonical-node
  [value options]
  (encode-value value (atom 0) 0 options))

(defn- encode-canonical-bytes
  [value options maximum-bytes]
  (let [node (canonical-node value options)
        bytes (.getBytes (canonical-node-text node) StandardCharsets/UTF_8)]
    (when (> (alength bytes) maximum-bytes)
      (cache-fail! "C16-ENTRY" "canonical cache data exceeds its byte bound"
                   {:observed-bytes (alength bytes)
                    :maximum-bytes maximum-bytes}))
    bytes))

(defn- parse-integer
  [tag value]
  (case tag
    "byte" (Byte/valueOf value)
    "short" (Short/valueOf value)
    "int" (Integer/valueOf value)
    "long" (Long/valueOf value)
    "bigint" (clojure.lang.BigInt/fromBigInteger (BigInteger. value))
    "biginteger" (BigInteger. value)
    (cache-fail! "C16-ENTRY" "unknown canonical integer tag"
                 {:integer-tag tag})))

(declare decode-node)

(defn- expect-node!
  [node tag arity]
  (when-not (and (vector? node)
                 (= arity (count node))
                 (= tag (first node)))
    (cache-fail! "C16-ENTRY" "malformed canonical cache node"
                 {:expected-tag tag :expected-arity arity})))

(defn- expect-node-sequence!
  [node tag]
  (expect-node! node tag 2)
  (when-not (vector? (second node))
    (cache-fail! "C16-ENTRY" "canonical collection payload must be a vector"
                 {:node-tag tag})))

(defn- strictly-sorted-node-text?
  [nodes]
  (let [texts (mapv canonical-node-text nodes)]
    (or (empty? texts)
        (every? (fn [[left right]] (neg? (compare left right)))
                (partition 2 1 texts)))))

(defn- decode-node
  [node state depth]
  (let [nodes (swap! state inc)]
    (when (> nodes maximum-canonical-nodes)
      (cache-fail! "C16-ENTRY" "decoded cache data exceeds the node bound"
                   {:maximum-nodes maximum-canonical-nodes}))
    (when (> depth maximum-canonical-depth)
      (cache-fail! "C16-ENTRY" "decoded cache data exceeds the depth bound"
                   {:maximum-depth maximum-canonical-depth}))
    (when-not (and (vector? node) (keyword? (first node)))
      (cache-fail! "C16-ENTRY" "cache data is not a canonical tagged node" {}))
    (case (first node)
      :nil (do (expect-node! node :nil 1) nil)
      :boolean (do (expect-node! node :boolean 2)
                   (when-not (boolean? (second node))
                     (cache-fail! "C16-ENTRY" "malformed boolean node" {}))
                   (second node))
      :string (do (expect-node! node :string 2)
                  (when-not (string? (second node))
                    (cache-fail! "C16-ENTRY" "malformed string node" {}))
                  (second node))
      :character (do (expect-node! node :character 2)
                     (char (long (second node))))
      :keyword (do (expect-node! node :keyword 3)
                   (if-let [ns-name (second node)]
                     (keyword ns-name (nth node 2))
                     (keyword (nth node 2))))
      :symbol (do (expect-node! node :symbol 3)
                  (if-let [ns-name (second node)]
                    (symbol ns-name (nth node 2))
                    (symbol (nth node 2))))
      :integer (do (expect-node! node :integer 3)
                   (parse-integer (second node) (nth node 2)))
      :ratio (do (expect-node! node :ratio 3)
                 (clojure.lang.Ratio. (BigInteger. (second node))
                                      (BigInteger. (nth node 2))))
      :bigdecimal (do (expect-node! node :bigdecimal 2)
                      (BigDecimal. ^String (second node)))
      :double (do (expect-node! node :double 2)
                  (let [value (Double/longBitsToDouble
                               (Long/parseLong (second node)))]
                    (when-not (Double/isFinite value)
                      (cache-fail! "C16-ENTRY" "decoded double is nonfinite" {}))
                    value))
      :float (do (expect-node! node :float 2)
                 (let [value (Float/intBitsToFloat
                              (Integer/parseInt (second node)))]
                   (when-not (Float/isFinite value)
                     (cache-fail! "C16-ENTRY" "decoded float is nonfinite" {}))
                   value))
      :uuid (do (expect-node! node :uuid 2)
                (UUID/fromString (second node)))
      :date (do (expect-node! node :date 2)
                (Date. (long (second node))))
      :bytes (do (expect-node! node :bytes 2)
                 (.decode (Base64/getDecoder) ^String (second node)))
      :vector (do (expect-node-sequence! node :vector)
                  (mapv #(decode-node % state (inc depth)) (second node)))
      :list (do (expect-node-sequence! node :list)
                (apply list
                       (map #(decode-node % state (inc depth))
                            (second node))))
      :set (do (expect-node-sequence! node :set)
               (when-not (strictly-sorted-node-text? (second node))
                 (cache-fail! "C16-ENTRY"
                              "canonical set nodes are not sorted and unique"
                              {}))
               (set (map #(decode-node % state (inc depth)) (second node))))
      :map (do (expect-node-sequence! node :map)
               (when-not (strictly-sorted-node-text? (second node))
                 (cache-fail! "C16-ENTRY"
                              "canonical map entries are not sorted and unique"
                              {}))
               (reduce
                (fn [result entry]
                  (when-not (and (vector? entry) (= 2 (count entry)))
                    (cache-fail! "C16-ENTRY" "malformed canonical map entry" {}))
                  (let [key (decode-node (first entry) state (inc depth))
                        value (decode-node (second entry) state (inc depth))]
                    (when (contains? result key)
                      (cache-fail! "C16-ENTRY" "duplicate canonical map key" {}))
                    (assoc result key value)))
                {} (second node)))
      :meta (do (expect-node! node :meta 3)
                (let [metadata (decode-node (second node) state (inc depth))
                      value (decode-node (nth node 2) state (inc depth))]
                  (when-not (and (map? metadata)
                                 (instance? clojure.lang.IObj value))
                    (cache-fail! "C16-ENTRY" "malformed canonical metadata" {}))
                  (with-meta value metadata)))
      (cache-fail! "C16-ENTRY" "unknown canonical cache node"
                   {:node-tag (first node)}))))

(defn- decode-canonical-bytes
  [bytes maximum-bytes]
  (when (> (alength bytes) maximum-bytes)
    (cache-fail! "C16-ENTRY" "cache file exceeds its read bound"
                 {:observed-bytes (alength bytes)
                  :maximum-bytes maximum-bytes}))
  (let [text (String. ^bytes bytes StandardCharsets/UTF_8)
        node
        (try
          (edn/read-string
           {:readers {}
            :default (fn [tag _]
                       (cache-fail! "C16-ENTRY" "tagged EDN is forbidden"
                                    {:tag tag}))}
           text)
          (catch StackOverflowError _
            (cache-fail! "C16-ENTRY" "cache EDN exceeds the host stack bound" {}))
          (catch ThreadDeath fatal (throw fatal))
          (catch VirtualMachineError fatal (throw fatal))
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch clojure.lang.ExceptionInfo error
            (throw error))
          (catch Throwable error
            (cache-fail! "C16-ENTRY" "cache EDN is malformed"
                         {:contained-host-error (.getName (class error))})))
        value
        (try
          ;; Bound and validate the parsed tree before any recursive printer is
          ;; allowed to visit it.  Deep-but-valid hostile EDN therefore becomes
          ;; a structured cache rejection, not host stack growth.
          (decode-node node (atom 0) 0)
          (catch StackOverflowError _
            (cache-fail! "C16-ENTRY"
                         "decoded cache data exceeds the host stack bound" {})))
        canonical-bytes
        (try
          (encode-canonical-bytes value {:reject-metadata? false}
                                  maximum-bytes)
          (catch StackOverflowError _
            (cache-fail! "C16-ENTRY"
                         "canonical cache rendering exceeds the host stack bound"
                         {})))]
    (when-not (java.util.Arrays/equals bytes canonical-bytes)
      (cache-fail! "C16-ENTRY" "cache EDN is not in canonical form" {}))
    value))

(defn canonical-content-id
  "Return a domain-separated SHA-256 id for a metadata-free canonical value.

  This is the only identity helper exported by the leaf.  It rejects values
  that cannot be represented by the bounded type-sensitive canonicalizer."
  [value]
  (let [bytes (encode-canonical-bytes
               {:domain :gravity/c2-pass-cache-content-v1
                :canonicalizer-version canonicalizer-version
                :value value}
               {:reject-metadata? true}
               maximum-encoded-bytes)]
    (str "sha256:" (digest/sha256-bytes-hex bytes))))

(defn- raw-path-has-parent-segment?
  [^Path path]
  (boolean (some #(= ".." (str %)) (iterator-seq (.iterator path)))))

(defn- normalized-absolute-path!
  [path]
  (let [raw (Paths/get (str path) (make-array String 0))]
    (when (raw-path-has-parent-segment? raw)
      (cache-fail! "C16-POLICY" "cache paths cannot contain parent traversal"
                   {:path (str path)}))
    (.normalize (.toAbsolutePath raw))))

(defn- basic-attributes
  [^Path path]
  (Files/readAttributes path BasicFileAttributes nofollow-links))

(defn- unix-link-count
  [^Path path]
  (long (Files/getAttribute path "unix:nlink" nofollow-links)))

(defn- current-owner-name
  []
  (System/getProperty "user.name"))

(defn- path-owner-name
  [^Path path]
  (.getName (Files/getOwner path nofollow-links)))

(defn- current-user-owned?
  [^Path path]
  (= (current-owner-name) (path-owner-name path)))

(defn- safe-shared-directory-permissions?
  [permissions]
  (and (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_READ)
       (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_WRITE)
       (contains? permissions
                  java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE)
       (not (contains? permissions
                       java.nio.file.attribute.PosixFilePermission/GROUP_WRITE))
       (not (contains? permissions
                       java.nio.file.attribute.PosixFilePermission/OTHERS_WRITE))))

(defn- verify-directory!
  [^Path path owned?]
  (let [attributes (basic-attributes path)
        permissions (Files/getPosixFilePermissions path nofollow-links)]
    (when-not (.isDirectory attributes)
      (cache-fail! "C16-POLICY" "cache path is not a no-follow directory"
                   {:path (str path)}))
    (when-not (current-user-owned? path)
      (cache-fail! "C16-POLICY" "cache directory is not owned by the current user"
                   {:path (str path)
                    :owner (path-owner-name path)
                    :required-owner (current-owner-name)}))
    (when-not (if owned?
                (= owned-directory-permissions permissions)
                (safe-shared-directory-permissions? permissions))
      (cache-fail! "C16-POLICY"
                   (if owned?
                     "cache-owned directory permissions changed"
                     "shared cache parent has an unsafe permission policy")
                   {:path (str path)
                    :observed (PosixFilePermissions/toString permissions)
                    :required (if owned? "0700"
                                  "owner-rwx-and-no-group-or-other-write")})))
  path)

(defn- directory-identity
  [^Path path owned?]
  (verify-directory! path owned?)
  (let [attributes (basic-attributes path)]
    {:path path
     :owned? owned?
     :file-key (.fileKey attributes)
     :device (Files/getAttribute path "unix:dev" nofollow-links)
     :inode (Files/getAttribute path "unix:ino" nofollow-links)
     :owner (path-owner-name path)
     :permissions (Files/getPosixFilePermissions path nofollow-links)}))

(defn- verify-directory-identity!
  [{:keys [^Path path owned? file-key device inode owner permissions]}]
  (verify-directory! path owned?)
  (let [attributes (basic-attributes path)
        observed {:file-key (.fileKey attributes)
                  :device (Files/getAttribute path "unix:dev" nofollow-links)
                  :inode (Files/getAttribute path "unix:ino" nofollow-links)
                  :owner (path-owner-name path)
                  :permissions (Files/getPosixFilePermissions path nofollow-links)}]
    (when-not (= {:file-key file-key
                  :device device
                  :inode inode
                  :owner owner
                  :permissions permissions}
                 observed)
      (cache-fail! "C16-POLICY"
                   "cache directory identity changed after store open"
                   {:path (str path)})))
  path)

(defn- verify-store-identity!
  [store]
  (doseq [identity (:directory-identities store)]
    (verify-directory-identity! identity))
  store)

(defn- ensure-base-directory!
  [^Path base]
  (when-not (Files/exists base nofollow-links)
    (cache-fail! "C16-POLICY"
                 "explicit cache base must already exist"
                 {:path (str base)}))
  (verify-directory! base false))

(defn- require-secure-directory-stream!
  [stream operation]
  (when-not (instance? SecureDirectoryStream stream)
    (cache-fail! "C16-POLICY"
                 "filesystem provider lacks descriptor-relative cache access"
                 {:operation operation
                  :provider (.getName (class stream))}))
  ^SecureDirectoryStream stream)

(defn- secure-self-attributes
  [^SecureDirectoryStream directory]
  (let [^BasicFileAttributeView basic-view
        (.getFileAttributeView directory BasicFileAttributeView)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory PosixFileAttributeView)]
    (when-not (and basic-view posix-view)
      (cache-fail! "C16-POLICY"
                   "filesystem provider lacks required secure POSIX views"
                   {}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn- secure-child-attributes
  [^SecureDirectoryStream directory ^Path relative]
  (let [^BasicFileAttributeView basic-view
        (.getFileAttributeView directory relative BasicFileAttributeView
                               nofollow-links)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory relative PosixFileAttributeView
                               nofollow-links)]
    (when-not (and basic-view posix-view)
      (cache-fail! "C16-POLICY"
                   "filesystem provider lacks required secure child views"
                   {:name (str relative)}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn- same-basic-file?
  [^BasicFileAttributes left ^BasicFileAttributes right]
  (and (= (.fileKey left) (.fileKey right))
       (= (.size left) (.size right))
       (= (.lastModifiedTime left) (.lastModifiedTime right))))

(defn- verify-secure-directory-handle!
  [^SecureDirectoryStream directory identity]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-self-attributes directory)]
    (when-not (and (.isDirectory basic)
                   (= (:file-key identity) (.fileKey basic))
                   (= (:owner identity) (.getName (.owner posix)))
                   (= (:permissions identity) (.permissions posix)))
      (cache-fail! "C16-POLICY"
                   "secure directory handle does not match pinned store identity"
                   {:path (str (:path identity))})))
  directory)

(defn- correlated-unix-link-count!
  [^Path absolute ^BasicFileAttributes secure-basic diagnostic-id]
  ;; SecureDirectoryStream deliberately exposes only basic/POSIX views, not
  ;; unix:nlink.  This read-only path query is accepted only when path attrs on
  ;; both sides match the already held descriptor-relative file identity.  No
  ;; child is ever opened or mutated through this path.
  (let [before (basic-attributes absolute)]
    (when-not (same-basic-file? secure-basic before)
      (cache-fail! diagnostic-id
                   "path link-count probe diverged from secure file identity"
                   {:path (str absolute)}))
    (let [links (unix-link-count absolute)
          after (basic-attributes absolute)]
      (when-not (and (same-basic-file? secure-basic after)
                     (= (.fileKey before) (.fileKey after)))
        (cache-fail! diagnostic-id
                     "path changed during link-count integrity probe"
                     {:path (str absolute)}))
      links)))

(defn- require-file-channel!
  [channel diagnostic-id operation]
  (when-not (instance? FileChannel channel)
    (.close ^SeekableByteChannel channel)
    (cache-fail! diagnostic-id
                 "filesystem provider did not supply a durable file channel"
                 {:operation operation
                  :provider (.getName (class channel))}))
  ^FileChannel channel)

(defn- read-channel-exact!
  [^FileChannel channel size diagnostic-id description]
  (let [buffer (ByteBuffer/allocate (int size))]
    (loop []
      (when (.hasRemaining buffer)
        (let [count (.read channel buffer)]
          (when (= -1 count)
            (cache-fail! diagnostic-id
                         (str description " ended before its declared size")
                         {:expected-bytes size}))
          (recur))))
    (when-not (= -1 (.read channel (ByteBuffer/allocate 1)))
      (cache-fail! diagnostic-id (str description " grew during bounded read")
                   {:expected-bytes size}))
    (.array buffer)))

(defn bounded-source-snapshot!
  "Read a no-follow regular source file into one bounded immutable snapshot.

  The returned byte array is an execution input, not part of the key record;
  only its byte count and SHA-256 identity enter the semantic preimage."
  ([path]
   (bounded-source-snapshot! path maximum-source-bytes))
  ([path byte-limit]
   (when-not (and (integer? byte-limit)
                  (pos? byte-limit)
                  (<= byte-limit maximum-source-bytes))
     (cache-fail! "C16-KEY" "source snapshot bound is invalid"
                  {:maximum-source-bytes maximum-source-bytes
                   :requested byte-limit}))
   (with-contained-host-errors
    "C16-KEY" :bounded-source-snapshot
    (fn []
      (let [source-path (normalized-absolute-path! path)
            parent (.getParent source-path)
            relative (.getFileName source-path)
            parent-identity (directory-identity parent false)]
        (with-open [raw-parent (Files/newDirectoryStream parent)]
          (let [parent-stream
                (require-secure-directory-stream!
                 raw-parent :bounded-source-snapshot)
                _ (verify-secure-directory-handle! parent-stream
                                                   parent-identity)
                {:keys [^BasicFileAttributes basic
                        ^PosixFileAttributes posix]}
                (secure-child-attributes parent-stream relative)
                links (correlated-unix-link-count! source-path basic
                                                   "C16-KEY")]
            (when-not (and (.isRegularFile basic)
                           (not (.isSymbolicLink basic))
                           (= 1 links)
                           (= (current-owner-name) (.getName (.owner posix)))
                           (<= 0 (.size basic) byte-limit))
              (cache-fail!
               "C16-KEY" "source snapshot is not a bounded secure file"
               {:path (str source-path)
                :regular-file? (.isRegularFile basic)
                :symbolic-link? (.isSymbolicLink basic)
                :link-count links
                :owner (.getName (.owner posix))
                :required-owner (current-owner-name)
                :observed-bytes (.size basic)
                :maximum-bytes byte-limit}))
            (let [expected-size (long (.size basic))
                  raw-channel
                  (.newByteChannel ^SecureDirectoryStream parent-stream relative
                                   (HashSet. [StandardOpenOption/READ
                                              LinkOption/NOFOLLOW_LINKS])
                                   (make-array FileAttribute 0))
                  channel
                  (require-file-channel! raw-channel "C16-KEY"
                                         :bounded-source-snapshot)]
              (with-open [channel channel]
                (let [first-bytes
                      (read-channel-exact! channel expected-size "C16-KEY"
                                           "source snapshot")
                      _ (.position channel 0)
                      second-bytes
                      (read-channel-exact! channel expected-size "C16-KEY"
                                           "source snapshot recheck")
                      after-attributes
                      (secure-child-attributes parent-stream relative)
                      ^BasicFileAttributes after (:basic after-attributes)
                      ^PosixFileAttributes after-posix
                      (:posix after-attributes)
                      after-links
                      (correlated-unix-link-count! source-path after "C16-KEY")]
                  (when-not (and (= expected-size (.size channel))
                                 (same-basic-file? basic after)
                                 (= 1 after-links)
                                 (= (.getName (.owner posix))
                                    (.getName (.owner after-posix)))
                                 (= (.permissions posix)
                                    (.permissions after-posix))
                                 (java.util.Arrays/equals
                                  ^bytes first-bytes ^bytes second-bytes)
                                 (= (digest/sha256-bytes-hex first-bytes)
                                    (digest/sha256-bytes-hex second-bytes)))
                    (cache-fail! "C16-KEY"
                                 "source changed during stable double snapshot"
                                 {:path (str source-path)}))
                  (verify-secure-directory-handle! parent-stream
                                                   parent-identity)
                  (verify-directory-identity! parent-identity)
                  {:artifact :gravity/bounded-source-snapshot
                   :schema-version 1
                   :canonical-path (str source-path)
                   :byte-count (alength first-bytes)
                   :bytes-hash
                   (str "sha256:"
                        (digest/sha256-bytes-hex first-bytes))
                   :bytes first-bytes
                   :maximum-source-bytes byte-limit}))))))))))

(defn- validate-key-request!
  [request]
  (when-not (and (map? request) (= key-request-fields (set (keys request))))
    (cache-fail! "C16-KEY" "C2 cache key request fields are incomplete"
                 {:required-fields (vec (sort key-request-fields))
                  :observed-fields (when (map? request)
                                     (vec (sort (keys request))))}))
  (let [{:keys [source-unit source-snapshot reader-policy project-binding
                compiler-binding pass-binding dependency-binding
                build-effect-binding capability-binding facet-binding
                profile-binding target-binding boundary-binding
                path-provenance]} request]
    (doseq [[field value]
            [[:source-unit source-unit]
             [:source-snapshot source-snapshot]
             [:reader-policy reader-policy]
             [:project-binding project-binding]
             [:compiler-binding compiler-binding]
             [:pass-binding pass-binding]
             [:dependency-binding dependency-binding]
             [:build-effect-binding build-effect-binding]
             [:capability-binding capability-binding]
             [:facet-binding facet-binding]
             [:profile-binding profile-binding]
             [:target-binding target-binding]
             [:boundary-binding boundary-binding]
             [:path-provenance path-provenance]]]
      (when-not (map? value)
        (cache-fail! "C16-KEY" "C2 cache key binding must be a map"
                     {:field field})))
    (ensure-sha256-id! :source-id (:source-id source-unit))
    (ensure-sha256-id! :source-unit-bytes-hash (:bytes-hash source-unit))
    (ensure-sha256-id! :snapshot-bytes-hash (:bytes-hash source-snapshot))
    (ensure-sha256-id! :extension-policy (:extension-policy reader-policy))
    (ensure-sha256-id! :project-root-id (:project-root-id project-binding))
    (ensure-sha256-id! :compiler-id (:compiler-id compiler-binding))
    (ensure-sha256-id! :sh03-binding-id (:sh03-binding-id compiler-binding))
    (ensure-sha256-id! :pass-contract-id (:pass-contract-id pass-binding))
    (doseq [[field binding]
            [[:dependency-binding dependency-binding]
             [:build-effect-binding build-effect-binding]
             [:capability-binding capability-binding]
             [:facet-binding facet-binding]
             [:boundary-binding boundary-binding]]]
      (ensure-sha256-id! field (:identity binding)))
    (when-not (and (= (:bytes-hash source-unit)
                      (:bytes-hash source-snapshot))
                   (= (:bytes-hash source-unit)
                      (get-in source-unit [:identity-inputs :bytes-hash]))
                   (= (:reader-options source-unit)
                      (:reader-options reader-policy))
                   (= (:extension-policy reader-policy)
                      (get-in source-unit
                              [:identity-inputs :extension-policy]))
                   (= (:project-root-id project-binding)
                      (get-in source-unit
                              [:identity-inputs :project-root-id]))
                   (= (:project-relative-path project-binding)
                      (get-in source-unit
                              [:identity-inputs :project-relative-path]))
                   (integer? (:byte-count source-snapshot))
                   (<= 0 (:byte-count source-snapshot) maximum-source-bytes))
      (cache-fail! "C16-KEY" "source snapshot and C2 source-unit bindings disagree"
                   {}))
    (when-not (= {:applicability :not-applicable-at-c2} profile-binding)
      (cache-fail! "C16-KEY" "C2 profile binding must be explicitly inapplicable"
                   {:observed profile-binding}))
    (when-not (= {:applicability :not-applicable-at-c2} target-binding)
      (cache-fail! "C16-KEY" "C2 target binding must be explicitly inapplicable"
                   {:observed target-binding}))
    (when-not (and (= :SH-03 (:slice boundary-binding))
                   (= :gravity-source (:owner boundary-binding))
                   (= :gravity/sh03-to-c2-reader-products-v2
                      (:adapter-contract boundary-binding))
                   (sha256-id? (:plan-binding-id boundary-binding))
                   (sha256-id?
                    (:semantic-value-table-contract-id boundary-binding))
                   (sha256-id?
                    (:authenticated-envelope-contract-id boundary-binding))
                   (false? (:target-source-reread? boundary-binding))
                   (map? (:uncredited-source-models boundary-binding))
                   (true? (:clojure-adapter-residual? boundary-binding))
                   (false? (:self-hosted? boundary-binding))
                   (= (:identity boundary-binding)
                      (canonical-content-id
                       {:domain :gravity/c2-pass-cache-boundary-binding-v1
                        :binding (dissoc boundary-binding :identity)})))
      (cache-fail! "C16-KEY"
                   "C2 cache boundary binding is incomplete or unsafe"
                   {:observed boundary-binding}))
    (let [canonical-path (:canonical-path path-provenance)
          normalized (when (string? canonical-path)
                       (normalized-absolute-path! canonical-path))]
      (when-not (and normalized (= canonical-path (str normalized)))
        (cache-fail! "C16-KEY" "path provenance must be normalized and absolute"
                     {:observed canonical-path}))))
  request)

(defn cache-key
  "Build the canonical semantic and path-scoped storage ids for one C2 input.

  The absolute supplied path is deliberately excluded from `:semantic-key-id`
  and included in `:storage-key-id`.  This preserves the C2 semantic identity
  rule while conservatively preventing an artifact carrying one checkout's path
  provenance from being returned for another checkout."
  [request]
  (let [validated (validate-key-request! request)
        path-provenance (:path-provenance validated)
        semantic-preimage
        {:artifact :gravity/compiler-pass-cache-key-preimage
         :schema-version cache-schema-version
         :canonicalizer-version canonicalizer-version
         :stage cache-stage
         :source-unit (:source-unit validated)
         :source-snapshot (:source-snapshot validated)
         :reader-policy (:reader-policy validated)
         :project-binding (:project-binding validated)
         :compiler-binding (:compiler-binding validated)
         :pass-binding (:pass-binding validated)
         :dependency-binding (:dependency-binding validated)
         :build-effect-binding (:build-effect-binding validated)
         :capability-binding (:capability-binding validated)
         :facet-binding (:facet-binding validated)
         :profile-binding (:profile-binding validated)
         :target-binding (:target-binding validated)
         :boundary-binding (:boundary-binding validated)}
        semantic-key-id (canonical-content-id semantic-preimage)
        storage-key-id
        (canonical-content-id
         {:domain :gravity/c2-pass-cache-storage-key-v1
          :semantic-key-id semantic-key-id
          :path-provenance path-provenance})]
    {:artifact :gravity/compiler-pass-cache-key
     :schema-version cache-schema-version
     :canonicalizer-version canonicalizer-version
     :stage cache-stage
     :semantic-preimage semantic-preimage
     :semantic-key-id semantic-key-id
     :path-provenance path-provenance
     :storage-key-id storage-key-id}))

(defn- relative-name
  [filename]
  (Paths/get filename (make-array String 0)))

(declare secure-write-new! secure-fsync-directory! secure-child-exists?
         secure-file-attributes-relative! require-file-channel!)

(defn- verify-secure-child-directory!
  [^SecureDirectoryStream parent ^Path relative owned?]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-child-attributes parent relative)
        permissions (.permissions posix)]
    (when-not (and (.isDirectory basic)
                   (not (.isSymbolicLink basic))
                   (= (current-owner-name) (.getName (.owner posix)))
                   (if owned?
                     (= owned-directory-permissions permissions)
                     (safe-shared-directory-permissions? permissions)))
      (cache-fail! "C16-POLICY"
                   "cache directory failed descriptor-relative policy"
                   {:name (str relative)
                    :owned? owned?
                    :owner (.getName (.owner posix))
                    :permissions
                    (PosixFilePermissions/toString permissions)})))
  relative)

(defn- secure-directory-move!
  [^SecureDirectoryStream source ^Path source-name
   ^SecureDirectoryStream destination ^Path destination-name]
  (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class source)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination))))
    (cache-fail! "C16-POLICY"
                 "filesystem provider lacks anchored directory publication"
                 {:source-provider (.getName (class source))
                  :destination-provider (.getName (class destination))}))
  (.move source source-name destination destination-name))

(defn- secure-ensure-child-directory!
  [^SecureDirectoryStream parent child-name owned?]
  (let [relative (relative-name child-name)]
    (when-not (secure-child-exists? parent relative)
      (let [temporary
            (Files/createTempDirectory
             "gravity-c2-cache-mkdir-"
             (into-array FileAttribute [directory-attribute]))
            source-parent (.getParent temporary)
            source-relative (.getFileName temporary)
            source-parent-identity
            (directory-identity source-parent false)]
        (with-open [raw-source (Files/newDirectoryStream source-parent)]
          (let [source
                (require-secure-directory-stream!
                 raw-source :cache-directory-bootstrap)]
            (try
              (verify-secure-directory-handle!
               source source-parent-identity)
              (verify-secure-child-directory! source source-relative true)
              (if (secure-child-exists? parent relative)
                nil
                (secure-directory-move!
                 source source-relative parent relative))
              (secure-fsync-directory! source)
              (secure-fsync-directory! parent)
              (finally
                ;; Cleanup is anchored too.  A successful move makes the
                ;; detached name absent; a pre-move failure removes it through
                ;; the still-held source parent descriptor.
                (when (secure-child-exists? source source-relative)
                  (.deleteDirectory source source-relative)
                  (secure-fsync-directory! source))))))))
    (verify-secure-child-directory! parent relative owned?)
    (let [child (.newDirectoryStream parent relative nofollow-links)
          secure-child
          (require-secure-directory-stream!
           child :cache-directory-bootstrap-child)]
      (try
        (let [{:keys [^BasicFileAttributes basic
                      ^PosixFileAttributes posix]}
              (secure-self-attributes secure-child)]
          (when-not (and (.isDirectory basic)
                         (= (current-owner-name)
                            (.getName (.owner posix))))
            (cache-fail! "C16-POLICY"
                         "opened cache directory failed identity policy"
                         {:name child-name})))
        secure-child
        (catch Throwable error
          (.close ^DirectoryStream child)
          (throw error))))))

(defn- with-cache-bootstrap-lock
  [^Path base ^SecureDirectoryStream base-directory operation]
  (let [lock-name (relative-name ".cpcache-bootstrap.lock")
        partial-store {:base base}
        [local-key local-lock]
        (acquire-in-process-key-lock! (str base ":bootstrap"))]
    (try
      (when-not (secure-child-exists? base-directory lock-name)
        (try
          (secure-write-new! partial-store :base base-directory lock-name
                             (byte-array 0))
          (catch java.nio.file.FileAlreadyExistsException _ nil)))
      (secure-file-attributes-relative!
       partial-store :base base-directory lock-name 0)
      (let [raw-channel
            (.newByteChannel base-directory lock-name
                             (HashSet. [StandardOpenOption/READ
                                        StandardOpenOption/WRITE
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
            channel (require-file-channel! raw-channel "C16-POLICY"
                                           :cache-bootstrap-lock)]
        (with-open [channel channel
                    lock (.lock channel)]
          (operation)))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))

(defn open-local-store
  "Open or create the isolated local cache below `base-path`.

  The returned store has no release or proof authority.  Cache-owned
  directories are required to remain mode 0700."
  [base-path]
  (with-contained-host-errors
   "C16-POLICY" :open-local-store
   (fn []
     (let [base (normalized-absolute-path! base-path)
           _ (ensure-base-directory! base)
           base-identity (directory-identity base false)
           _
           (with-open [raw-base (Files/newDirectoryStream base)]
             (let [base-directory
                   (require-secure-directory-stream!
                    raw-base :cache-directory-bootstrap-base)]
               (verify-secure-directory-handle!
                base-directory base-identity)
               (with-cache-bootstrap-lock
                base base-directory
                (fn []
                  ;; Clojure CLI may already own `.cpcache`; an existing safe
                  ;; parent is reused.  Every absent final namespace is a
                  ;; descriptor-relative move of a private detached directory.
                  (with-open [cpcache-directory
                              (secure-ensure-child-directory!
                               base-directory ".cpcache" false)]
                    (with-open [compiler-pass-directory
                                (secure-ensure-child-directory!
                                 cpcache-directory "compiler-pass" true)]
                      (with-open [root-directory
                                  (secure-ensure-child-directory!
                                   compiler-pass-directory "v1" true)]
                        (doseq [child ["blobs" "entries" "locks" "staging"]]
                          (with-open [owned-directory
                                      (secure-ensure-child-directory!
                                       root-directory child true)]
                            (verify-secure-child-directory!
                             root-directory (relative-name child) true))))))))))
           cpcache (.resolve base ".cpcache")
           compiler-pass (.resolve cpcache "compiler-pass")
           root (.resolve compiler-pass "v1")
           blobs (.resolve root "blobs")
           entries (.resolve root "entries")
           locks (.resolve root "locks")
           staging (.resolve root "staging")
           store
           {:artifact :gravity/local-compiler-pass-cache-store
            :schema-version cache-schema-version
            :base base
            :cpcache cpcache
            :compiler-pass compiler-pass
            :root root
            :blobs blobs
            :entries entries
            :locks locks
            :staging staging
            :directory-identities
            [base-identity
             (directory-identity cpcache false)
             (directory-identity compiler-pass true)
             (directory-identity root true)
             (directory-identity blobs true)
             (directory-identity entries true)
             (directory-identity locks true)
             (directory-identity staging true)]
            :store-policy
            {:maximum-entry-count maximum-entry-count
             :maximum-blob-count maximum-blob-count
             :maximum-lock-count maximum-lock-count
             :maximum-staging-count maximum-staging-count
             :maximum-aggregate-bytes maximum-store-bytes}
            :local-development-only? true
            :release-authority? false}]
       (verify-store-identity! store)))))

(defn- id-filename
  [identity]
  (ensure-sha256-id! :storage-path-id identity)
  (str (subs identity (count "sha256:")) ".edn"))

(defn- identity-for-path
  [store path-key]
  (let [path (get store path-key)]
    (or (some #(when (= path (:path %)) %) (:directory-identities store))
        (cache-fail! "C16-POLICY" "store directory identity is missing"
                     {:path-key path-key}))))

(defn- open-secure-child!
  [^SecureDirectoryStream parent child-name identity]
  (let [raw-child (.newDirectoryStream parent (relative-name child-name)
                                       nofollow-links)
        child (require-secure-directory-stream!
               raw-child :open-secure-store-child)]
    (try
      (verify-secure-directory-handle! child identity)
      child
      (catch Throwable error
        (.close ^DirectoryStream raw-child)
        (throw error)))))

(defn- with-secure-store-directories
  [store operation]
  (with-contained-host-errors
   "C16-POLICY" :secure-store-traversal
   (fn []
     (verify-store-identity! store)
     (with-open [raw-base (Files/newDirectoryStream ^Path (:base store))]
       (let [base (require-secure-directory-stream!
                   raw-base :open-secure-store-base)]
         (verify-secure-directory-handle! base (identity-for-path store :base))
         (with-open [cpcache
                     (open-secure-child!
                      base ".cpcache" (identity-for-path store :cpcache))]
           (with-open [compiler-pass
                       (open-secure-child!
                        cpcache "compiler-pass"
                        (identity-for-path store :compiler-pass))]
             (with-open [root
                         (open-secure-child!
                          compiler-pass "v1"
                          (identity-for-path store :root))]
               (with-open [blobs
                           (open-secure-child!
                            root "blobs" (identity-for-path store :blobs))
                           entries
                           (open-secure-child!
                            root "entries" (identity-for-path store :entries))
                           locks
                           (open-secure-child!
                            root "locks" (identity-for-path store :locks))
                           staging
                           (open-secure-child!
                            root "staging"
                            (identity-for-path store :staging))]
                 (let [directories {:blobs blobs
                                    :entries entries
                                    :locks locks
                                    :staging staging}
                       result (operation directories)]
                   (doseq [[path-key directory] directories]
                     (verify-secure-directory-handle!
                      directory (identity-for-path store path-key)))
                   result))))))))))

(defn- secure-child-exists?
  [^SecureDirectoryStream directory ^Path relative]
  (try
    (secure-child-attributes directory relative)
    true
    (catch java.nio.file.NoSuchFileException _ false)))

(defn- secure-file-attributes-relative!
  [store path-key ^SecureDirectoryStream directory ^Path relative
   maximum-bytes]
  (let [{:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]
         :as attributes}
        (secure-child-attributes directory relative)
        absolute (.resolve ^Path (get store path-key) relative)
        link-count (correlated-unix-link-count! absolute basic "C16-ENTRY")]
    (when-not (and (.isRegularFile basic)
                   (not (.isSymbolicLink basic))
                   (= 1 link-count)
                   (<= 0 (.size basic) maximum-bytes)
                   (= (current-owner-name) (.getName (.owner posix)))
                   (= owned-file-permissions (.permissions posix)))
      (cache-fail! "C16-ENTRY"
                   "cache file failed descriptor-relative integrity checks"
                   {:path-key path-key
                    :name (str relative)
                    :regular-file? (.isRegularFile basic)
                    :symbolic-link? (.isSymbolicLink basic)
                    :link-count link-count
                    :observed-bytes (.size basic)
                    :maximum-bytes maximum-bytes
                    :owner (.getName (.owner posix))
                    :required-owner (current-owner-name)
                    :required-permissions "0600"}))
    attributes))

(defn- secure-read-bytes!
  [store path-key ^SecureDirectoryStream directory ^Path relative
   maximum-bytes]
  (let [before (secure-file-attributes-relative!
                store path-key directory relative maximum-bytes)
        ^BasicFileAttributes before-basic (:basic before)
        expected-size (long (.size before-basic))
        raw-channel (.newByteChannel
                     directory relative
                     (HashSet. [StandardOpenOption/READ
                                LinkOption/NOFOLLOW_LINKS])
                     (make-array FileAttribute 0))
        channel (require-file-channel! raw-channel "C16-ENTRY"
                                       :secure-cache-read)]
    (with-open [channel channel]
      (let [bytes (read-channel-exact! channel expected-size "C16-ENTRY"
                                       "cache file")
            after (secure-file-attributes-relative!
                   store path-key directory relative maximum-bytes)]
        (when-not (and (= expected-size (.size channel))
                       (same-basic-file? before-basic (:basic after)))
          (cache-fail! "C16-ENTRY" "cache file changed during secure read"
                       {:path-key path-key :name (str relative)}))
        bytes))))

(defn- secure-fsync-directory!
  [^SecureDirectoryStream directory]
  (let [raw-channel (.newByteChannel
                     directory (relative-name ".")
                     (HashSet. [StandardOpenOption/READ
                                LinkOption/NOFOLLOW_LINKS])
                     (make-array FileAttribute 0))
        channel (require-file-channel! raw-channel "C16-POLICY"
                                       :secure-directory-fsync)]
    (with-open [channel channel]
      (.force channel true))))

(defn- secure-write-new!
  [store path-key ^SecureDirectoryStream directory ^Path relative bytes]
  (let [raw-channel (.newByteChannel
                     directory relative create-new-write-options
                     (into-array FileAttribute [file-attribute]))
        channel (require-file-channel! raw-channel "C16-ENTRY"
                                       :secure-cache-publication)]
    (with-open [channel channel]
      (let [buffer (ByteBuffer/wrap bytes)]
        (while (.hasRemaining buffer)
          (.write channel buffer)))
      (.force channel true))
    (secure-file-attributes-relative!
     store path-key directory relative (alength bytes))
    (secure-fsync-directory! directory)))

(defn- lock-name
  [key]
  (relative-name
   (str (subs (:storage-key-id key) (count "sha256:")) ".lock")))

(defn- secure-ensure-lock-file!
  [store directories relative]
  (let [^SecureDirectoryStream directory (:locks directories)]
    (when-not (secure-child-exists? directory relative)
      (try
        (secure-write-new! store :locks directory relative (byte-array 0))
        (catch java.nio.file.FileAlreadyExistsException _ nil)))
    (secure-file-attributes-relative! store :locks directory relative 0)
    relative))

(defn- secure-directory-inventory!
  [store path-key ^SecureDirectoryStream directory name-pattern
   maximum-count maximum-file-bytes]
  (loop [items (iterator-seq (.iterator directory))
         count 0
         bytes 0]
    (if-let [item (first items)]
      (let [name (str (.getFileName ^Path item))
            next-count (inc count)]
        (when (or (> next-count maximum-count)
                  (not (boolean (re-matches name-pattern name))))
          (cache-fail! "C16-POLICY" "cache store inventory violates policy"
                       {:path-key path-key
                        :observed-name name
                        :maximum-count maximum-count}))
        (let [relative (relative-name name)
              attributes (secure-file-attributes-relative!
                          store path-key directory relative maximum-file-bytes)
              next-bytes (+ bytes (.size ^BasicFileAttributes
                                         (:basic attributes)))]
          (when (> next-bytes maximum-store-bytes)
            (cache-fail! "C16-POLICY"
                         "cache directory exceeds aggregate byte policy"
                         {:path-key path-key
                          :maximum-aggregate-bytes maximum-store-bytes}))
          (recur (next items) next-count next-bytes)))
      {:count count :bytes bytes})))

(defn- secure-store-inventory!
  [store directories]
  (let [blobs (secure-directory-inventory!
               store :blobs (:blobs directories) #"[0-9a-f]{64}\.edn"
               maximum-blob-count maximum-blob-bytes)
        entries (secure-directory-inventory!
                 store :entries (:entries directories) #"[0-9a-f]{64}\.edn"
                 maximum-entry-count maximum-entry-bytes)
        locks (secure-directory-inventory!
               store :locks (:locks directories)
               #"(?:store|[0-9a-f]{64})\.lock"
               maximum-lock-count 0)
        staging (secure-directory-inventory!
                 store :staging (:staging directories)
                 #"\.stage-[0-9a-f-]{36}\.tmp"
                 maximum-staging-count maximum-blob-bytes)
        aggregate (+ (:bytes blobs) (:bytes entries) (:bytes locks)
                     (:bytes staging))]
    (when (> aggregate maximum-store-bytes)
      (cache-fail! "C16-POLICY" "cache store exceeds aggregate byte policy"
                   {:observed-aggregate-bytes aggregate
                    :maximum-aggregate-bytes maximum-store-bytes}))
    {:blobs blobs :entries entries :locks locks :staging staging
     :aggregate-bytes aggregate}))

(defn- fresh-store-inventory!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (secure-store-inventory! store directories))))

(defn- recover-staging-residue!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [^SecureDirectoryStream staging (:staging directories)
           items (vec (iterator-seq (.iterator staging)))]
       (when (> (count items) maximum-staging-count)
         (cache-fail! "C16-POLICY"
                      "cache staging residue exceeds recovery policy"
                      {:maximum-staging-count maximum-staging-count}))
       (doseq [item items]
         (let [name (str (.getFileName ^Path item))
               relative (relative-name name)]
           (when-not (re-matches #"\.stage-[0-9a-f-]{36}\.tmp" name)
             (cache-fail! "C16-POLICY"
                          "cache staging residue name is invalid"
                          {:observed-name name}))
           (secure-file-attributes-relative!
            store :staging staging relative maximum-blob-bytes)
           (.deleteFile staging relative)))
       (when (seq items)
         (secure-fsync-directory! staging))))))

(defn- with-global-store-lock
  [store operation]
  (let [[local-key local-lock]
        (acquire-in-process-key-lock! (str (:root store) ":global"))]
    (try
      (with-secure-store-directories
       store
       (fn [directories]
         (let [lock-name (secure-ensure-lock-file!
                          store directories (relative-name "store.lock"))
               ^SecureDirectoryStream lock-directory (:locks directories)
               raw-channel
               (.newByteChannel lock-directory lock-name
                                (HashSet. [StandardOpenOption/READ
                                           StandardOpenOption/WRITE
                                           LinkOption/NOFOLLOW_LINKS])
                                (make-array FileAttribute 0))
               channel (require-file-channel! raw-channel "C16-POLICY"
                                              :secure-store-lock)]
           (with-open [channel channel
                       lock (.lock channel)]
             (secure-file-attributes-relative!
              store :locks lock-directory lock-name 0)
             (recover-staging-residue! store)
             ;; Recovery can invalidate directory-entry snapshots held by an
             ;; already-open provider stream.  Keep the global lock channel,
             ;; but perform admission/publication through a fresh anchored
             ;; traversal after recovery.
             (with-secure-store-directories store operation)))))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))

(defn- ensure-key-lock-admitted!
  [store key]
  (let [relative (lock-name key)
        existing?
        (with-secure-store-directories
         store
         (fn [directories]
           (let [^SecureDirectoryStream lock-directory (:locks directories)]
             (when (secure-child-exists? lock-directory relative)
               (secure-file-attributes-relative!
                store :locks lock-directory relative 0)
               true))))]
    (when-not existing?
      (with-global-store-lock
       store
       (fn [directories]
         (let [inventory (secure-store-inventory! store directories)
               ^SecureDirectoryStream lock-directory (:locks directories)]
           (when-not (secure-child-exists? lock-directory relative)
             (when (>= (get-in inventory [:locks :count]) maximum-lock-count)
               (cache-fail! "C16-POLICY"
                            "cache per-key lock admission exceeds store policy"
                            {:maximum-lock-count maximum-lock-count}))
             (secure-write-new! store :locks lock-directory relative
                                (byte-array 0)))
           (secure-file-attributes-relative!
            store :locks lock-directory relative 0)
           (let [post (fresh-store-inventory! store)]
             (when-not (<= (get-in post [:locks :count]) maximum-lock-count)
               (cache-fail!
                "C16-POLICY"
                "cache lock inventory exceeds store policy after admission"
                {:maximum-lock-count maximum-lock-count})))))))
    relative))

(defn- with-key-lock
  [store key operation]
  (ensure-key-lock-admitted! store key)
  (let [relative (lock-name key)
        local-path (str (:root store) ":" (:storage-key-id key))
        [local-key local-lock] (acquire-in-process-key-lock! local-path)]
    (try
      (with-secure-store-directories
       store
       (fn [directories]
         (let [^SecureDirectoryStream lock-directory (:locks directories)
               _ (secure-file-attributes-relative!
                  store :locks lock-directory relative 0)
               raw-channel
               (.newByteChannel lock-directory relative
                                (HashSet. [StandardOpenOption/READ
                                           StandardOpenOption/WRITE
                                           LinkOption/NOFOLLOW_LINKS])
                                (make-array FileAttribute 0))
               channel (require-file-channel! raw-channel "C16-POLICY"
                                              :secure-key-lock)]
           (with-open [channel channel
                       lock (.lock channel)]
             (secure-file-attributes-relative!
              store :locks lock-directory relative 0)
             (operation directories)))))
      (finally
        (release-in-process-key-lock! local-key local-lock)))))

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

(defn lookup!
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

(defn store!
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

(defn lookup-or-compute!
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
