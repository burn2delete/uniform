(ns gravity.pass-cache
  "Generic local compiler-pass cache with receipt-first revalidation.

  The cache is deliberately a hosted Stage0 leaf.  It stores immutable,
  content-addressed artifacts and pass execution receipts for local or
  speculative reuse; it never grants release, proof, equivalence, or
  self-hosting authority."
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.pass-execution :as pass-execution])
  (:import [java.math BigDecimal BigInteger]
           [java.nio ByteBuffer]
           [java.nio.channels FileChannel FileLock OverlappingFileLockException
            SeekableByteChannel]
           [java.nio.charset CharacterCodingException CodingErrorAction
            StandardCharsets]
           [java.nio.file DirectoryStream Files LinkOption OpenOption Path Paths
            SecureDirectoryStream StandardCopyOption StandardOpenOption]
           [java.nio.file.attribute BasicFileAttributes BasicFileAttributeView
            FileAttribute PosixFileAttributeView PosixFileAttributes
            PosixFilePermission PosixFilePermissions]
           [java.util Base64 Date HashSet UUID]
           [java.util.concurrent.locks ReentrantLock]))

(def ^:private schema-version 2)
(def ^:private canonicalizer-version 2)
(def ^:private cache-root-relative [".cpcache" "compiler-pass" "v2"])
(def ^:private maximum-depth 96)
(def ^:private maximum-nodes 32768)
(def ^:private maximum-file-bytes (* 48 1024 1024))
(def ^:private maximum-canonical-bytes maximum-file-bytes)
(def ^:private maximum-entry-bytes (* 4 1024 1024))
(def ^:private maximum-blob-bytes maximum-file-bytes)
(def ^:private maximum-entry-count 8192)
(def ^:private maximum-blob-count 8192)
(def ^:private maximum-receipt-count 8192)
(def ^:private maximum-lock-count 8193)
(def ^:private maximum-staging-count 8192)
(def ^:private maximum-store-bytes (* 512 1024 1024))
(def ^:private store-policy
  {:maximum-entry-count maximum-entry-count
   :maximum-blob-count maximum-blob-count
   :maximum-receipt-count maximum-receipt-count
   :maximum-lock-count maximum-lock-count
   :maximum-staging-count maximum-staging-count
   :maximum-aggregate-bytes maximum-store-bytes
   :maximum-entry-bytes maximum-entry-bytes
   :maximum-blob-bytes maximum-blob-bytes})
(def ^:private sha256-pattern #"sha256:[0-9a-f]{64}")
(def ^:private authority-rank
  {:none 0 :non-authoritative 1 :reviewed 2 :authoritative 3})
(def ^:private nofollow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
(def ^:private private-directory-permissions
  (PosixFilePermissions/fromString "rwx------"))
(def ^:private private-file-permissions
  (PosixFilePermissions/fromString "rw-------"))
(def ^:private private-directory-attribute
  (PosixFilePermissions/asFileAttribute private-directory-permissions))
(def ^:private private-file-attribute
  (PosixFilePermissions/asFileAttribute private-file-permissions))
(def ^:private create-new-write-options
  (HashSet. [StandardOpenOption/CREATE_NEW
             StandardOpenOption/WRITE
             LinkOption/NOFOLLOW_LINKS]))
(def ^:private in-process-key-locks (atom {}))
(def ^:dynamic ^:private *key-lock-held* false)
(def ^:private publication-lock (ReentrantLock.))
(def ^:private store-bootstrap-lock (ReentrantLock.))
(def ^:private active-staging (atom #{}))
(def ^:dynamic ^:private *store-lock-held* false)
(def ^:dynamic ^:private *publication-hook* nil)

(def ^:private execution-request-fields
  #{:stage :contract :producer-binding-id :input-artifact-ids :input-facts
    :external-root-inputs :semantic-bindings :dependency-graph-id
    :build-effect-replay-id :profile-id :target-id :policy-ids :provenance
    :diagnostic-stream-id :execution-mode :authority})

(def ^:private local-store-fields
  #{:base :cpcache :compiler-pass :root :blobs :entries :receipts :locks
    :staging :schema-version :store-policy :directory-identities})

(def ^:private pass-cache-public-api
  {'pass-cache-contract {:arglists '([])}
   'stage-cache-key {:arglists '([request])}
   'open-local-store {:arglists '([base-path])}
   'lookup! {:arglists '([store key validation-ops])}
   'store! {:arglists '([store key artifact producer-receipt validation-ops])}
   'lookup-or-compute! {:arglists '([store key execution-request operations])}})

(def ^:private pass-cache-contract-record
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

(defn- fail!
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

(defn- fatal?
  [error]
  (or (instance? ThreadDeath error)
      (instance? VirtualMachineError error)
      (instance? InterruptedException error)))

(defn- sha256-id?
  [value]
  (and (string? value) (boolean (re-matches sha256-pattern value))))

(defn- require-sha256!
  [field value]
  (when-not (sha256-id? value)
    (fail! "C16-KEY" "semantic identity must be lowercase SHA-256"
           {:field field :observed value}))
  value)

(defn- require-keyword-set!
  [field value]
  (when-not (and (set? value) (every? keyword? value))
    (fail! "C16-KEY" "semantic fact fields must be keyword sets"
           {:field field :observed value}))
  value)

(defn- sorted-sha-vector!
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

(defn- weakest-authority
  [levels]
  (first (sort-by authority-rank levels)))

(defn- validate-authority!
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

(defn- integral-tag
  [value]
  (cond
    (instance? Byte value) :byte
    (instance? Short value) :short
    (instance? Integer value) :int
    (instance? Long value) :long
    (instance? clojure.lang.BigInt value) :bigint
    (instance? BigInteger value) :biginteger
    :else nil))

(defn- bounded-string-bytes
  [text]
  ;; Count exact UTF-8 bytes without first allocating an encoded copy.  The
  ;; former `4 * UTF-16-length` shortcut was safe but rejected large ASCII and
  ;; Base64 payloads far below the declared artifact bound.
  (loop [index 0 total 0]
    (if (= index (.length ^String text))
      total
      (let [code-point (.codePointAt ^String text index)
            width (Character/charCount code-point)
            encoded-width (cond
                            (<= code-point 0x7f) 1
                            (<= code-point 0x7ff) 2
                            (<= code-point 0xffff) 3
                            :else 4)
            next-total (+ total encoded-width)]
        (when (> next-total maximum-canonical-bytes)
          (fail! "C16-KEY" "canonical scalar exceeds its byte bound"
                 {:maximum-bytes maximum-canonical-bytes}))
        (recur (+ index width) next-total)))))

(defn- byte-array?
  [value]
  (and value (= (class value) (Class/forName "[B"))))

(defn- object-metadata!
  [value]
  (when (and (instance? clojure.lang.IMeta value) (seq (meta value)))
    (fail! "C16-KEY" "semantic cache values may not carry host metadata"
           {:value-class (.getName (class value))}))
  value)

(defn- finite-double!
  [value]
  (when-not (Double/isFinite (double value))
    (fail! "C16-KEY" "nonfinite floating point values are forbidden"
           {:observed value}))
  value)

(defn- integer-bit-length
  [value]
  (cond
    (instance? BigInteger value) (.bitLength ^BigInteger value)
    (instance? clojure.lang.BigInt value)
    (.bitLength ^BigInteger (.toBigInteger ^clojure.lang.BigInt value))
    :else (.bitLength (BigInteger/valueOf (long value)))))

(defn- require-integer-bound!
  [field value]
  (when (> (integer-bit-length value) (* 8 maximum-canonical-bytes))
    (fail! "C16-KEY" "integer exceeds canonical bit bound"
           {:field field :maximum-bits (* 8 maximum-canonical-bytes)}))
  value)

(defn- negative-integral?
  [value]
  (cond
    (instance? BigInteger value) (neg? (.signum ^BigInteger value))
    (instance? clojure.lang.BigInt value)
    (neg? (.signum ^BigInteger
                   (.toBigInteger ^clojure.lang.BigInt value)))
    :else (neg? (long value))))

(defn- conservative-integer-printed-bytes
  [value]
  ;; ceil(bit-length * log10(2)), plus sign and surrounding string quotes.
  (let [bits (max 1 (integer-bit-length value))
        digits (quot (+ (* (long bits) 30103) 99999) 100000)]
    (+ digits (if (negative-integral? value) 1 0) 2)))

(declare ^{:private true} account-bytes!)

(defn- account-integral-text!
  [state field value]
  (require-integer-bound! field value)
  (account-bytes! state (conservative-integer-printed-bytes value)))

(defn- account-bigdecimal-text!
  [state ^BigDecimal value]
  ;; BigDecimal.toString may choose plain or exponent form. precision+|scale|
  ;; plus sign, decimal point, exponent marker/sign/digits, and quotes is a
  ;; conservative allocation-free upper bound.
  (let [precision (long (.precision value))
        scale (long (.scale value))
        scale-magnitude (Math/abs scale)
        exponent-magnitude (+ precision scale-magnitude 1)
        exponent-digits
        (max 1 (quot (+ (* (max 1 (.bitLength
                                   (BigInteger/valueOf exponent-magnitude)))
                             30103)
                          99999)
                       100000))
        upper-bound (+ precision scale-magnitude exponent-digits 8)]
    (account-bytes! state upper-bound)))

(defn- account-node!
  [state]
  (let [{:keys [nodes]}
        (swap! state update :nodes inc)]
    (when (> nodes maximum-nodes)
      (fail! "C16-KEY" "canonical value exceeds its node bound"
             {:maximum-nodes maximum-nodes}))))

(defn- account-bytes!
  [state estimate]
  (let [{:keys [bytes limit]}
        (swap! state update :bytes + estimate)]
    (when (> bytes maximum-canonical-bytes)
      (fail! "C16-KEY" "canonical value exceeds its byte bound"
             {:maximum-bytes maximum-canonical-bytes}))
    (when (> bytes limit)
      (fail! "C16-KEY" "canonical value exceeds its requested byte bound"
             {:maximum-bytes limit}))))

(defn- account-text!
  [state text]
  (let [text (str text)
        _ (bounded-string-bytes text)
        printed-bytes
        (loop [index 0 total 2]
          (if (< index (.length ^String text))
            (let [code-point (.codePointAt ^String text index)
                  width (Character/charCount code-point)
                  escaped-bytes
                  (cond
                    (or (= code-point (int \")) (= code-point (int \\))) 2
                    (#{(int \newline) (int \return) (int \tab)
                       (int \backspace) (int \formfeed)} code-point) 2
                    (Character/isISOControl code-point) 6
                    (<= code-point 0x7f) 1
                    (<= code-point 0x7ff) 2
                    (<= code-point 0xffff) 3
                    :else 4)]
              (recur (+ index width) (+ total escaped-bytes)))
            total))]
    (account-bytes! state printed-bytes)))

(declare canonical-node)

(defn- canonical-sort
  [nodes]
  (let [with-text (mapv (fn [node] [node (pr-str node)]) nodes)]
    (mapv first (sort-by second with-text))))

(defn- canonical-node
  [value state depth]
  (when (> depth maximum-depth)
    (fail! "C16-KEY" "canonical value exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  (object-metadata! value)
  (account-node! state)
  ;; Account the tagged-node/container syntax independently of node count.
  ;; 64 bytes conservatively covers every fixed tag and bracket/space
  ;; scaffold in this closed tagged-node grammar (including :biginteger).
  (account-bytes! state 64)
  (cond
    (nil? value) (do (account-bytes! state 3) [:nil])
    (true? value) (do (account-bytes! state 4) [:boolean true])
    (false? value) (do (account-bytes! state 5) [:boolean false])
    (string? value) (do (account-text! state value)
                        [:string value])
    (char? value) (do (account-text! state (int value))
                      [:character (int value)])
    (keyword? value) (do (account-text! state (name value))
                         (when (namespace value)
                           (account-text! state (namespace value)))
                         [:keyword (namespace value) (name value)])
    (symbol? value) (do (account-text! state (name value))
                         (when (namespace value)
                           (account-text! state (namespace value)))
                         [:symbol (namespace value) (name value)])
    (integral-tag value) (do (account-integral-text! state :integer value)
                             [:integer (integral-tag value) (str value)])
    (ratio? value) (do (account-integral-text! state :ratio-numerator
                                               (numerator value))
                       (account-integral-text! state :ratio-denominator
                                               (denominator value))
                       [:ratio (str (numerator value)) (str (denominator value))])
    (instance? BigDecimal value) (do (account-bigdecimal-text! state value)
                                     [:bigdecimal (str value)])
    (instance? Double value) (do (finite-double! value)
                                 (let [encoded (Long/toString
                                                (Double/doubleToRawLongBits value))]
                                   (account-text! state encoded)
                                   [:double encoded]))
    (instance? Float value) (do (finite-double! value)
                                (let [encoded (Integer/toString
                                               (Float/floatToRawIntBits value))]
                                  (account-text! state encoded)
                                  [:float encoded]))
    (instance? UUID value) (do (account-text! state value) [:uuid (str value)])
    (instance? Date value) (do (account-text! state (.getTime ^Date value))
                               [:date (.getTime ^Date value)])
    (byte-array? value) (do
                          (when (> (alength ^bytes value)
                                   (quot (* maximum-canonical-bytes 3) 4))
                            (fail! "C16-KEY"
                                   "byte-array exceeds canonical preflight bound"
                                   {:maximum-bytes maximum-canonical-bytes}))
                          (let [encoded (.encodeToString (Base64/getEncoder)
                                                          ^bytes value)]
                            (account-text! state encoded)
                            [:bytes encoded]))
    (map? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical map exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      (let [entries (mapv (fn [[key item]]
                            [(canonical-node key state (inc depth))
                             (canonical-node item state (inc depth))])
                          value)]
        [:map (canonical-sort entries)]))
    (set? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical set exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      [:set (canonical-sort
             (mapv #(canonical-node % state (inc depth)) value))])
    (vector? value)
    (do
      (when (> (count value) maximum-nodes)
        (fail! "C16-KEY" "canonical vector exceeds its cardinality bound"
               {:maximum-cardinality maximum-nodes}))
      (account-bytes! state (+ 8 (count value)))
      [:vector (mapv #(canonical-node % state (inc depth)) value)])
    (seq? value)
    (let [items (loop [remaining (seq value) result []]
                  (when (> (count result) maximum-nodes)
                    (fail! "C16-KEY" "canonical list exceeds its cardinality bound"
                           {:maximum-cardinality maximum-nodes}))
                  (if remaining
                    (if (= (count result) maximum-nodes)
                      (fail! "C16-KEY"
                             "canonical list exceeds its cardinality bound"
                             {:maximum-cardinality maximum-nodes})
                      (recur (next remaining)
                             (conj result
                                   (canonical-node (first remaining)
                                                   state (inc depth)))))
                    result))]
      (account-bytes! state (+ 8 (count items)))
      [:list items])
    :else
    (fail! "C16-KEY" "unsupported value in semantic cache identity"
           {:value-class (.getName (class value))})))

(defn- canonical-bytes
  ([value] (canonical-bytes value maximum-canonical-bytes))
  ([value byte-limit]
   (let [node (canonical-node value (atom {:nodes 0 :bytes 0
                                           :limit byte-limit}) 0)
         text (pr-str node)
         bytes (.getBytes text StandardCharsets/UTF_8)]
     (when (> (alength bytes) byte-limit)
       (fail! "C16-KEY" "canonical value exceeds its byte bound"
              {:maximum-bytes byte-limit :observed-bytes (alength bytes)}))
     bytes)))

(defn- content-id
  [domain value]
  (str "sha256:" (digest/sha256-bytes-hex
                   (canonical-bytes
                    {:domain domain
                     :canonicalizer-version canonicalizer-version
                     :value value}))))

(defn- canonical-text
  [node]
  (pr-str node))

(defn- expect-node!
  [node tag arity]
  (when-not (and (vector? node) (= arity (count node)) (= tag (first node)))
    (fail! "C16-ENTRY" "malformed canonical cache node"
           {:expected-tag tag :expected-arity arity})))

(defn- sorted-node-vector!
  [field value]
  (when-not (vector? value)
    (fail! "C16-ENTRY" "canonical collection payload must be a vector"
           {:field field}))
  (let [texts (mapv canonical-text value)]
    (when-not (every? (fn [[a b]] (neg? (compare a b)))
                      (partition 2 1 texts))
      (fail! "C16-ENTRY" "canonical collection nodes are not sorted"
             {:field field})))
  value)

(defn- parse-integer
  [tag text]
  (try
    (case tag
      :byte (Byte/valueOf text)
      :short (Short/valueOf text)
      :int (Integer/valueOf text)
      :long (Long/valueOf text)
      :bigint (clojure.lang.BigInt/fromBigInteger (BigInteger. text))
      :biginteger (BigInteger. text)
      (fail! "C16-ENTRY" "unknown canonical integer tag" {:tag tag}))
    (catch NumberFormatException error
      (fail! "C16-ENTRY" "canonical integer is malformed"
             {:tag tag :message (.getMessage error)}))))

(declare decode-node)

(defn- decode-node
  [node state depth]
  (when (> depth maximum-depth)
    (fail! "C16-ENTRY" "decoded cache data exceeds its depth bound"
           {:maximum-depth maximum-depth}))
  (let [nodes (swap! state inc)]
    (when (> nodes maximum-nodes)
      (fail! "C16-ENTRY" "decoded cache data exceeds its node bound"
             {:maximum-nodes maximum-nodes})))
  (when-not (and (vector? node) (keyword? (first node)))
    (fail! "C16-ENTRY" "cache data is not a canonical tagged node" {}))
  (case (first node)
    :nil (do (expect-node! node :nil 1) nil)
    :boolean (do (expect-node! node :boolean 2)
                 (when-not (boolean? (second node))
                   (fail! "C16-ENTRY" "malformed boolean node" {}))
                 (second node))
    :string (do (expect-node! node :string 2)
                (when-not (string? (second node))
                  (fail! "C16-ENTRY" "malformed string node" {}))
                (second node))
    :character (do (expect-node! node :character 2)
                   (try (char (int (second node)))
                        (catch Throwable _
                          (fail! "C16-ENTRY" "malformed character node" {}))))
    :keyword (do (expect-node! node :keyword 3)
                 (when-not (or (nil? (second node)) (string? (second node)))
                   (fail! "C16-ENTRY" "malformed keyword namespace" {}))
                 (when-not (string? (nth node 2))
                   (fail! "C16-ENTRY" "malformed keyword name" {}))
                 (if-let [ns-name (second node)]
                   (keyword ns-name (nth node 2))
                   (keyword (nth node 2))))
    :symbol (do (expect-node! node :symbol 3)
                (when-not (or (nil? (second node)) (string? (second node)))
                  (fail! "C16-ENTRY" "malformed symbol namespace" {}))
                (when-not (string? (nth node 2))
                  (fail! "C16-ENTRY" "malformed symbol name" {}))
                (if-let [ns-name (second node)]
                  (symbol ns-name (nth node 2))
                  (symbol (nth node 2))))
    :integer (do (expect-node! node :integer 3)
                 (parse-integer (second node) (nth node 2)))
    :ratio (do (expect-node! node :ratio 3)
               (try (clojure.lang.Ratio. (BigInteger. (second node))
                                          (BigInteger. (nth node 2)))
                    (catch Throwable _
                      (fail! "C16-ENTRY" "malformed ratio node" {}))))
    :bigdecimal (do (expect-node! node :bigdecimal 2)
                    (try (BigDecimal. ^String (second node))
                         (catch Throwable _
                           (fail! "C16-ENTRY" "malformed decimal node" {}))))
    :double (do (expect-node! node :double 2)
                (try (let [value (Double/longBitsToDouble
                                  (Long/parseLong (second node)))]
                      (when-not (Double/isFinite value)
                        (fail! "C16-ENTRY" "decoded double is nonfinite" {}))
                      value)
                     (catch NumberFormatException _
                       (fail! "C16-ENTRY" "malformed double node" {}))))
    :float (do (expect-node! node :float 2)
               (try (let [value (Float/intBitsToFloat
                                 (Integer/parseInt (second node)))]
                     (when-not (Float/isFinite value)
                       (fail! "C16-ENTRY" "decoded float is nonfinite" {}))
                     value)
                    (catch NumberFormatException _
                      (fail! "C16-ENTRY" "malformed float node" {}))))
    :uuid (do (expect-node! node :uuid 2)
              (try (UUID/fromString (second node))
                   (catch Throwable _
                     (fail! "C16-ENTRY" "malformed UUID node" {}))))
    :date (do (expect-node! node :date 2)
              (when-not (integer? (second node))
                (fail! "C16-ENTRY" "malformed date node" {}))
              (Date. (long (second node))))
    :bytes (do (expect-node! node :bytes 2)
               (when-not (string? (second node))
                 (fail! "C16-ENTRY" "malformed byte-array node" {}))
               (try (.decode (Base64/getDecoder) ^String (second node))
                    (catch Throwable _
                      (fail! "C16-ENTRY" "malformed byte-array node" {}))))
    :vector (do (expect-node! node :vector 2)
                (when-not (vector? (second node))
                  (fail! "C16-ENTRY" "canonical vector payload must be a vector" {}))
                (mapv #(decode-node % state (inc depth)) (second node)))
    :list (do (expect-node! node :list 2)
              (when-not (vector? (second node))
                (fail! "C16-ENTRY" "canonical list payload must be a vector" {}))
              (apply list (map #(decode-node % state (inc depth)) (second node))))
    :set (do (expect-node! node :set 2)
             (sorted-node-vector! :set (second node))
             (set (map #(decode-node % state (inc depth)) (second node))))
    :map (do (expect-node! node :map 2)
             (sorted-node-vector! :map (second node))
             (reduce (fn [result entry]
                       (when-not (and (vector? entry) (= 2 (count entry)))
                         (fail! "C16-ENTRY" "malformed canonical map entry" {}))
                       (let [key (decode-node (first entry) state (inc depth))
                             value (decode-node (second entry) state (inc depth))]
                         (when (contains? result key)
                           (fail! "C16-ENTRY" "duplicate canonical map key" {}))
                         (assoc result key value)))
                     {} (second node)))
    (fail! "C16-ENTRY" "unknown canonical cache node"
           {:node-tag (first node)})))

(defn- utf8-string
  [^bytes bytes diagnostic]
  (try
    (let [decoder (.newDecoder StandardCharsets/UTF_8)]
      (.onMalformedInput decoder CodingErrorAction/REPORT)
      (.onUnmappableCharacter decoder CodingErrorAction/REPORT)
      (str (.toString (.decode decoder (ByteBuffer/wrap bytes)))))
    (catch CharacterCodingException _
      (fail! diagnostic "cache file is not valid UTF-8" {}))))

(defn- preflight-edn-text!
  [^String text]
  ;; Count containers and scalar tokens before invoking the EDN reader.  A
  ;; delimiter-only scan does not bound a huge flat vector of atoms.
  (letfn [(account-node! [nodes]
            (let [next-nodes (inc nodes)]
              (when (> next-nodes maximum-nodes)
                (fail! "C16-ENTRY" "cache EDN exceeds its node bound"
                       {:maximum-nodes maximum-nodes}))
              next-nodes))]
   (loop [index 0 depth 0 nodes 0 quoted? false escaped? false
          comment? false token? false]
    (if (= index (.length text))
      (do
        (when token? (account-node! nodes))
        true)
      (let [ch (.charAt text index)]
        (cond
          comment?
          (recur (inc index) depth nodes false false
                 (not (or (= ch \newline) (= ch \return))) false)
          escaped? (recur (inc index) depth nodes true false false false)
          (and quoted? (= ch \\))
          (recur (inc index) depth nodes true true false false)
          (and quoted? (= ch \"))
          (recur (inc index) depth (account-node! nodes) false false false false)
          quoted? (recur (inc index) depth nodes true false false false)
          (= ch \;)
          (recur (inc index) depth (if token? (account-node! nodes) nodes)
                 false false true false)
          (= ch \" )
          (recur (inc index) depth (if token? (account-node! nodes) nodes)
                 true false false false)
          (#{\[ \{ \(} ch)
          (let [next-depth (inc depth)
                next-nodes (account-node!
                            (if token? (account-node! nodes) nodes))]
            (when (> next-depth maximum-depth)
              (fail! "C16-ENTRY" "cache EDN exceeds its depth bound"
                     {:maximum-depth maximum-depth}))
            (recur (inc index) next-depth next-nodes false false false false))
          (#{\] \} \)} ch)
          (recur (inc index) (max 0 (dec depth))
                 (if token? (account-node! nodes) nodes)
                 false false false false)
          (or (Character/isWhitespace ch) (= ch \,))
          (recur (inc index) depth
                 (if token? (account-node! nodes) nodes)
                 false false false false)
          :else (recur (inc index) depth nodes false false false true)))))))

(defn- decode-canonical-bytes
  [^bytes bytes byte-limit]
  (when (> (alength bytes) byte-limit)
    (fail! "C16-ENTRY" "cache file exceeds its read bound"
           {:maximum-bytes byte-limit :observed-bytes (alength bytes)}))
  (let [text (utf8-string bytes "C16-ENTRY")
        _ (preflight-edn-text! text)
        node (try
               (edn/read-string
                {:readers {}
                 :default (fn [tag _]
                            (fail! "C16-ENTRY" "tagged EDN is forbidden"
                                   {:tag tag}))}
                text)
               (catch clojure.lang.ExceptionInfo error (throw error))
               (catch StackOverflowError _
                 (fail! "C16-ENTRY" "cache EDN exceeds host stack bound" {}))
               (catch Throwable error
                 (if (fatal? error)
                   (throw error)
                   (fail! "C16-ENTRY" "cache EDN is malformed"
                          {:contained-host-error (.getName (class error))}))))
        value (try (decode-node node (atom 0) 0)
                   (catch StackOverflowError _
                     (fail! "C16-ENTRY" "decoded cache data exceeds host stack bound" {})))
        canonical (canonical-bytes value byte-limit)]
    (when-not (java.util.Arrays/equals bytes canonical)
      (fail! "C16-ENTRY" "cache EDN is not in canonical form" {}))
    value))

(defn- encoded-value
  [value byte-limit]
  (canonical-bytes value byte-limit))

(defn- path-parent-segment?
  [^Path path]
  (boolean (some #(= ".." (str %)) (iterator-seq (.iterator path)))))

(defn- absolute-base-path!
  [base-path]
  (when (nil? base-path)
    (fail! "C16-POLICY" "cache base path must be explicit" {}))
  (let [raw (Paths/get (str base-path) (make-array String 0))]
    (when (path-parent-segment? raw)
      (fail! "C16-POLICY" "cache base path cannot contain parent traversal"
             {:path (str base-path)}))
    (.normalize (.toAbsolutePath raw))))

(defn- relative-name!
  "Return one descriptor-relative name, rejecting paths and dot segments."
  [value]
  (let [^Path relative (if (instance? Path value)
                         value
                         (Paths/get (str value) (make-array String 0)))]
    (when (or (.isAbsolute relative)
              (not= 1 (.getNameCount relative))
              (#{"." ".."} (str relative))
              (not= (str relative) (str (.getFileName relative))))
      (fail! "C16-POLICY" "cache descriptor-relative name is invalid"
             {:name (str value)}))
    relative))

(defn- sha-file-name!
  [field id suffix]
  (require-sha256! field id)
  (relative-name! (str id suffix)))

(defn- attrs
  [^Path path]
  (try
    (Files/readAttributes path BasicFileAttributes nofollow-links)
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        (fail! "C16-POLICY" "cache path attributes are unavailable"
               {:path (str path) :contained-host-error (.getName (class error))})))))

(defn- owner-name
  [^Path path]
  (try
    (.getName (Files/getOwner path nofollow-links))
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        nil))))

(defn- current-owner-name [] (System/getProperty "user.name"))

(defn- unix-link-count
  [^Path path]
  (try
    (long (Files/getAttribute path "unix:nlink" nofollow-links))
    (catch UnsupportedOperationException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))
    (catch IllegalArgumentException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))
    (catch java.io.IOException error
      (fail! "C16-POLICY" "filesystem provider cannot verify link count"
             {:path (str path) :contained-host-error (.getName (class error))}))))

(defn- safe-shared-permissions?
  [permissions]
  (and (contains? permissions PosixFilePermission/OWNER_READ)
       (contains? permissions PosixFilePermission/OWNER_WRITE)
       (contains? permissions PosixFilePermission/OWNER_EXECUTE)
       (not (contains? permissions PosixFilePermission/GROUP_WRITE))
       (not (contains? permissions PosixFilePermission/OTHERS_WRITE))))

(defn- verify-directory!
  [^Path path owned?]
  (let [attributes (attrs path)
        permissions (Files/getPosixFilePermissions path nofollow-links)]
    (when (or (.isSymbolicLink attributes) (.isOther attributes)
              (not (.isDirectory attributes)))
      (fail! "C16-POLICY" "cache path is not a no-follow directory"
             {:path (str path)}))
    (when-not (= (current-owner-name) (owner-name path))
      (fail! "C16-POLICY" "cache directory is not owned by current user"
             {:path (str path) :owner (owner-name path)}))
    (when-not (if owned?
                (= private-directory-permissions permissions)
                (safe-shared-permissions? permissions))
      (fail! "C16-POLICY" "cache directory permissions are unsafe"
             {:path (str path)
              :observed (PosixFilePermissions/toString permissions)
              :owned? owned?})))
  path)

(defn- identity-of
  [^Path path owned?]
  (verify-directory! path owned?)
  (let [a (attrs path)]
    {:path path :owned? owned? :file-key (.fileKey a)
     :owner (owner-name path)
     :permissions (Files/getPosixFilePermissions path nofollow-links)}))

(defn- verify-identity!
  [{:keys [^Path path owned? file-key owner permissions]}]
  (verify-directory! path owned?)
  (let [a (attrs path)
        observed {:file-key (.fileKey a)
                  :owner (owner-name path)
                  :permissions (Files/getPosixFilePermissions path nofollow-links)}]
    (when-not (= {:file-key file-key :owner owner :permissions permissions}
                 observed)
      (fail! "C16-POLICY" "cache directory identity changed"
             {:path (str path)})))
  path)

(defn- verify-store-identities!
  [store]
  (doseq [identity (:directory-identities store)]
    (verify-identity! identity))
  store)

(declare ^{:private true} child-path)

(defn- validate-store!
  [store]
  (when-not (and (map? store)
                 (= local-store-fields (set (keys store)))
                 (= schema-version (:schema-version store))
                 (= store-policy (:store-policy store))
                 (vector? (:directory-identities store)))
    (fail! "C16-POLICY" "local cache store schema is unknown or incomplete" {}))
  (doseq [path-key [:base :cpcache :compiler-pass :root :blobs :entries
                    :receipts :locks :staging]]
    (when-not (instance? Path (get store path-key))
      (fail! "C16-POLICY" "local cache store path projection is malformed"
             {:path-key path-key})))
  (let [base (:base store)
        expected {:cpcache (child-path base ".cpcache")
                  :compiler-pass (child-path (:cpcache store) "compiler-pass")
                  :root (child-path (:compiler-pass store) "v2")
                  :blobs (child-path (:root store) "blobs")
                  :entries (child-path (:root store) "entries")
                  :receipts (child-path (:root store) "receipts")
                  :locks (child-path (:root store) "locks")
                  :staging (child-path (:root store) "staging")}
        expected-identities
        [[base false]
         [(:cpcache store) false]
         [(:compiler-pass store) false]
         [(:root store) true]
         [(:blobs store) true]
         [(:entries store) true]
         [(:receipts store) true]
         [(:locks store) true]
         [(:staging store) true]]
        identities (:directory-identities store)]
    (when-not (= expected-identities
                 (mapv (fn [identity]
                         (when-not (and (map? identity)
                                        (= #{:path :owned? :file-key :owner
                                             :permissions}
                                           (set (keys identity))))
                           (fail! "C16-POLICY"
                                  "local cache directory identity is malformed"
                                  {}))
                         [(:path identity) (:owned? identity)])
                       identities))
      (fail! "C16-POLICY"
             "local cache directory identities are not exact and ordered" {}))
    (doseq [[path-key expected-path] expected]
      (when-not (= expected-path (get store path-key))
        (fail! "C16-POLICY" "local cache store path projection was substituted"
               {:path-key path-key}))))
  (verify-store-identities! store))

;; SecureDirectoryStream is the descriptor-relative filesystem boundary.  A
;; provider that cannot expose secure POSIX/basic views is rejected instead of
;; falling back to path-precheck/open races for cache-owned data.
(defn- require-secure-directory-stream!
  [stream operation]
  (when-not (instance? SecureDirectoryStream stream)
    (fail! "C16-POLICY" "filesystem provider lacks descriptor-relative cache access"
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
      (fail! "C16-POLICY" "filesystem provider lacks required secure POSIX views" {}))
    {:basic (.readAttributes basic-view)
     :posix (.readAttributes posix-view)}))

(defn- secure-child-attributes
  [^SecureDirectoryStream directory ^Path relative]
  (let [relative (relative-name! relative)
        ^BasicFileAttributeView basic-view
        (.getFileAttributeView directory relative BasicFileAttributeView
                               nofollow-links)
        ^PosixFileAttributeView posix-view
        (.getFileAttributeView directory relative PosixFileAttributeView
                               nofollow-links)]
    (when-not (and basic-view posix-view)
      (fail! "C16-POLICY" "filesystem provider lacks required secure child views"
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
      (fail! "C16-POLICY" "secure directory handle does not match pinned identity"
             {:path (str (:path identity))})))
  directory)

(defn- secure-child-exists?
  [^SecureDirectoryStream directory ^Path relative]
  (let [relative (relative-name! relative)]
   (try
    (secure-child-attributes directory relative)
    true
    (catch java.nio.file.NoSuchFileException _ false))))

(defn- secure-file-attributes-relative!
  [store path-key ^SecureDirectoryStream directory ^Path relative maximum-bytes]
  (let [relative (relative-name! relative)
        {:keys [^BasicFileAttributes basic ^PosixFileAttributes posix] :as child-attrs}
        (secure-child-attributes directory relative)
        absolute (.resolve ^Path (get store path-key) relative)
        path-before (attrs absolute)
        _ (when-not (same-basic-file? basic path-before)
            (fail! "C16-POLICY"
                   "path link-count probe diverged from secure file identity"
                   {:path (str absolute)}))
        links (unix-link-count absolute)
        path-after (attrs absolute)
        _ (when-not (same-basic-file? basic path-after)
            (fail! "C16-POLICY"
                   "cache path changed during link-count integrity probe"
                   {:path (str absolute)}))]
    (when-not (and (.isRegularFile basic) (not (.isSymbolicLink basic))
                   (= 1 links) (<= 0 (.size basic) maximum-bytes)
                   (= (current-owner-name) (.getName (.owner posix)))
                   (= private-file-permissions (.permissions posix)))
      (fail! "C16-POLICY" "cache file failed descriptor-relative integrity checks"
             {:path-key path-key :name (str relative)
              :regular-file? (.isRegularFile basic)
              :symbolic-link? (.isSymbolicLink basic)
              :link-count links :observed-bytes (.size basic)
              :maximum-bytes maximum-bytes}))
    child-attrs))

(defn- require-file-channel!
  [channel diagnostic-id operation]
  (if (instance? FileChannel channel)
    ^FileChannel channel
    (do
      (.close ^SeekableByteChannel channel)
      (fail! diagnostic-id "filesystem provider did not supply durable file channel"
             {:operation operation :provider (.getName (class channel))}))))

(defn- read-channel-exact!
  [^FileChannel channel size diagnostic-id description]
  (let [buffer (ByteBuffer/allocate (int size))]
    (loop []
      (when (.hasRemaining buffer)
        (let [count (.read channel buffer)]
          (when (= -1 count)
            (fail! diagnostic-id (str description " ended before its declared size")
                   {:expected-bytes size}))
          (recur))))
    (when-not (= -1 (.read channel (ByteBuffer/allocate 1)))
      (fail! diagnostic-id (str description " grew during bounded read")
             {:expected-bytes size}))
    (.array buffer)))

(defn- secure-fsync-directory!
  [^SecureDirectoryStream directory]
  (let [raw (.newByteChannel directory (Paths/get "." (make-array String 0))
                             (HashSet. [StandardOpenOption/READ
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
        channel (require-file-channel! raw "C16-POLICY"
                                       :secure-directory-fsync)]
    (with-open [channel channel]
      (.force channel true))))

(defn- secure-read-bytes!
  [store path-key ^SecureDirectoryStream directory ^Path relative maximum-bytes]
  (let [relative (relative-name! relative)
        before (secure-file-attributes-relative!
                store path-key directory relative maximum-bytes)
        expected-size (long (.size ^BasicFileAttributes (:basic before)))
        raw (.newByteChannel directory relative
                             (HashSet. [StandardOpenOption/READ
                                        LinkOption/NOFOLLOW_LINKS])
                             (make-array FileAttribute 0))
        channel (require-file-channel! raw "C16-ENTRY" :secure-cache-read)]
    (with-open [channel channel]
      (let [bytes (read-channel-exact! channel expected-size "C16-ENTRY"
                                        "cache file")
            after (secure-file-attributes-relative!
                   store path-key directory relative maximum-bytes)]
        (when-not (same-basic-file? (:basic before) (:basic after))
          (fail! "C16-ENTRY" "cache file changed during secure read"
                 {:path-key path-key :name (str relative)}))
        bytes))))

(defn- secure-write-new!
  [store path-key ^SecureDirectoryStream directory ^Path relative ^bytes bytes]
  (let [relative (relative-name! relative)
        raw (.newByteChannel directory relative create-new-write-options
                             (into-array FileAttribute [private-file-attribute]))
        channel (require-file-channel! raw "C16-ENTRY"
                                       :secure-cache-publication)]
    (with-open [channel channel]
      (let [buffer (ByteBuffer/wrap bytes)]
        (while (.hasRemaining buffer)
          (.write channel buffer)))
      (.force channel true))
    (secure-file-attributes-relative! store path-key directory relative
                                       (alength bytes))
    (secure-fsync-directory! directory)
    relative))

(defn- secure-publish-move!
  [^SecureDirectoryStream staging ^Path temporary
   ^SecureDirectoryStream destination-directory ^Path destination]
  (let [temporary (relative-name! temporary)
        destination (relative-name! destination)]
   (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class staging)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination-directory))))
    (fail! "C16-POLICY" "filesystem provider lacks anchored atomic rename"
           {:source-provider (.getName (class staging))
            :destination-provider (.getName (class destination-directory))}))
   (.move staging temporary destination-directory destination)))

(defn- bytes-equal?
  [^bytes left ^bytes right]
  (java.util.Arrays/equals left right))

(defn- publish-create-or-verify!
  [store directories path-key ^SecureDirectoryStream destination-directory
   ^Path destination ^bytes bytes maximum-bytes]
  (let [destination (relative-name! destination)]
   (if (secure-child-exists? destination-directory destination)
    (let [existing (secure-read-bytes! store path-key destination-directory
                                       destination maximum-bytes)]
      (when-not (bytes-equal? existing bytes)
        (fail! "C16-ENTRY" "immutable cache destination contains divergent bytes"
               {:path-key path-key :name (str destination)}))
      :verified-identical)
    (let [^SecureDirectoryStream staging (:staging directories)
          temporary (Paths/get (str ".stage-" (UUID/randomUUID) ".tmp")
                               (make-array String 0))
          active-path (str (.resolve ^Path (:staging store) temporary))]
      (swap! active-staging conj active-path)
      (try
        (secure-write-new! store :staging staging temporary bytes)
        ;; Test-safe private hook for crash-recovery validation.  Production
        ;; callers leave it nil; a killed writer leaves a durable stage for the
        ;; next descriptor-locked bootstrap to recover.
        (when *publication-hook*
          (*publication-hook*))
        (if (secure-child-exists? destination-directory destination)
          (let [existing (secure-read-bytes!
                          store path-key destination-directory destination
                          maximum-bytes)]
            (when-not (bytes-equal? existing bytes)
              (fail! "C16-ENTRY"
                     "concurrent immutable cache publication conflicts"
                     {:path-key path-key :name (str destination)}))
            :converged-identical)
          (do
            (secure-publish-move! staging temporary destination-directory
                                  destination)
            (secure-fsync-directory! staging)
            (secure-fsync-directory! destination-directory)
            (let [published (secure-read-bytes!
                             store path-key destination-directory destination
                             maximum-bytes)]
              (when-not (bytes-equal? published bytes)
                (fail! "C16-ENTRY" "published cache bytes changed"
                       {:path-key path-key :name (str destination)})))
            :published))
        (finally
          (when (secure-child-exists? staging temporary)
            (.deleteFile staging temporary)
            (secure-fsync-directory! staging))
          (swap! active-staging disj active-path)))))))

(defn- child-path
  [^Path parent name]
  (let [name (str (relative-name! name))
        child (.resolve parent name)]
    (when-not (= parent (.getParent child))
      (fail! "C16-POLICY" "cache child escaped its parent"
             {:parent (str parent) :name name}))
    child))

(defn- create-private-tree!
  [base]
  (let [cpcache (child-path base ".cpcache")
        compiler-pass (child-path cpcache "compiler-pass")
        root (child-path compiler-pass "v2")
        blobs (child-path root "blobs")
        entries (child-path root "entries")
        receipts (child-path root "receipts")
        locks (child-path root "locks")
        staging (child-path root "staging")]
    ;; The SecureDirectoryStream bootstrap above creates and verifies every
    ;; component.  This retained map is only a path projection; no operation
    ;; opens or mutates cache data through it.
    {:base base :cpcache cpcache :compiler-pass compiler-pass :root root
     :blobs blobs :entries entries :receipts receipts :locks locks
     :staging staging}))

(defn- open-secure-child!
  [^SecureDirectoryStream parent child-name identity]
  (let [relative (relative-name! child-name)
        raw (.newDirectoryStream parent
                                 relative
                                 nofollow-links)
        child (require-secure-directory-stream!
               raw :open-secure-store-child)]
    (try
      (verify-secure-directory-handle! child identity)
      child
      (catch Throwable error
        (.close ^DirectoryStream raw)
        (throw error)))))

(defn- identity-for-path
  [store path-key]
  (let [path (get store path-key)]
    (or (some #(when (= path (:path %)) %) (:directory-identities store))
        (fail! "C16-POLICY" "store directory identity is missing"
               {:path-key path-key}))))

(defn- with-secure-store-directories
  [store operation]
  (verify-store-identities! store)
  (with-open [raw-base (Files/newDirectoryStream ^Path (:base store))]
    (let [base (require-secure-directory-stream!
                raw-base :open-secure-store-base)]
      (verify-secure-directory-handle! base (identity-for-path store :base))
      (with-open [cpcache (open-secure-child!
                           base ".cpcache" (identity-for-path store :cpcache))
                  compiler-pass (open-secure-child!
                                 cpcache "compiler-pass"
                                 (identity-for-path store :compiler-pass))
                  root (open-secure-child!
                        compiler-pass "v2" (identity-for-path store :root))
                  blobs (open-secure-child!
                         root "blobs" (identity-for-path store :blobs))
                  entries (open-secure-child!
                           root "entries" (identity-for-path store :entries))
                  receipts (open-secure-child!
                            root "receipts" (identity-for-path store :receipts))
                  locks (open-secure-child!
                         root "locks" (identity-for-path store :locks))
                  staging (open-secure-child!
                           root "staging" (identity-for-path store :staging))]
        (let [directories {:base base :cpcache cpcache
                           :compiler-pass compiler-pass :root root
                           :blobs blobs :entries entries :receipts receipts
                           :locks locks :staging staging}]
          (operation directories))))))

(defn- verify-secure-child-directory!
  [^SecureDirectoryStream parent ^Path relative owned?]
  (let [relative (relative-name! relative)
        {:keys [^BasicFileAttributes basic ^PosixFileAttributes posix]}
        (secure-child-attributes parent relative)
        permissions (.permissions posix)]
    (when-not (and (.isDirectory basic)
                   (not (.isSymbolicLink basic))
                   (= (current-owner-name) (.getName (.owner posix)))
                   (if owned?
                     (= private-directory-permissions permissions)
                     (safe-shared-permissions? permissions)))
      (fail! "C16-POLICY" "cache directory failed descriptor-relative policy"
             {:name (str relative) :owned? owned?})))
  relative)

(defn- secure-directory-move!
  [^SecureDirectoryStream source ^Path source-name
   ^SecureDirectoryStream destination ^Path destination-name]
  (let [source-name (relative-name! source-name)
        destination-name (relative-name! destination-name)]
   (when-not (and (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class source)))
                 (= "sun.nio.fs.UnixSecureDirectoryStream"
                    (.getName (class destination))))
    (fail! "C16-POLICY" "filesystem provider lacks anchored directory publication"
           {:source-provider (.getName (class source))
            :destination-provider (.getName (class destination))}))
   (.move source source-name destination destination-name)))

(defn- secure-ensure-child-directory!
  [^SecureDirectoryStream parent child-name owned?]
  (let [relative (relative-name! child-name)]
    (when-not (secure-child-exists? parent relative)
      (let [temporary (Files/createTempDirectory
                       "gravity-pass-cache-mkdir-"
                       (into-array FileAttribute [private-directory-attribute]))
            source-parent (.getParent temporary)
            source-relative (.getFileName temporary)
            source-parent-identity (identity-of source-parent false)]
        (with-open [raw-source (Files/newDirectoryStream source-parent)]
          (let [source (require-secure-directory-stream!
                        raw-source :cache-directory-bootstrap)]
            (try
              (verify-secure-directory-handle! source source-parent-identity)
              (verify-secure-child-directory! source source-relative true)
              (when-not (secure-child-exists? parent relative)
                (secure-directory-move! source source-relative parent relative))
              (secure-fsync-directory! source)
              (secure-fsync-directory! parent)
              (finally
                (when (secure-child-exists? source source-relative)
                  (.deleteDirectory source source-relative)
                  (secure-fsync-directory! source))))))))
    (verify-secure-child-directory! parent relative owned?)
    (let [raw-child (.newDirectoryStream parent relative nofollow-links)
          child (require-secure-directory-stream!
                 raw-child :cache-directory-bootstrap-child)]
      (try
        child
        (catch Throwable error
          (.close ^DirectoryStream raw-child)
          (throw error))))))

(defn- secure-directory-inventory!
  [store path-key ^SecureDirectoryStream directory pattern maximum maximum-bytes]
  (loop [iterator (.iterator directory) count 0 bytes 0 names []]
    (if (.hasNext iterator)
      (let [item (.next iterator)
            name (str (.getFileName ^Path item))
            next-count (inc count)]
        (when (or (> next-count maximum) (not (re-matches pattern name)))
          (fail! "C16-POLICY" "cache directory inventory violates policy"
                 {:directory path-key :name name :maximum maximum}))
        (let [relative (relative-name! name)
              attrs (secure-file-attributes-relative!
                     store path-key directory relative maximum-bytes)
              next-bytes (+ bytes (.size ^BasicFileAttributes (:basic attrs)))]
          (when (> next-bytes maximum-store-bytes)
            (fail! "C16-POLICY" "cache directory exceeds aggregate byte policy"
                   {:directory path-key :maximum-bytes maximum-store-bytes}))
          (recur iterator next-count next-bytes (conj names relative))))
      {:count count :bytes bytes :names names})))

(defn- secure-store-inventory!
  [store directories]
  (let [counts {:entries (secure-directory-inventory!
                          store :entries (:entries directories)
                          #"sha256:[0-9a-f]{64}\.edn"
                          maximum-entry-count maximum-entry-bytes)
                :blobs (secure-directory-inventory!
                        store :blobs (:blobs directories)
                        #"sha256:[0-9a-f]{64}\.edn"
                        maximum-blob-count maximum-blob-bytes)
                :receipts (secure-directory-inventory!
                           store :receipts (:receipts directories)
                           #"sha256:[0-9a-f]{64}\.edn"
                           maximum-receipt-count maximum-entry-bytes)
                :locks (secure-directory-inventory!
                        store :locks (:locks directories)
                        #"(?:sha256:[0-9a-f]{64}\.lock|\.store\.lock)"
                        maximum-lock-count 4096)
                :staging (secure-directory-inventory!
                          store :staging (:staging directories)
                          #"\.stage-[0-9a-f-]{36}\.tmp"
                          maximum-staging-count maximum-file-bytes)}
        total (reduce + 0 (map :bytes (vals counts)))]
    (when (> total maximum-store-bytes)
      (fail! "C16-POLICY" "cache store exceeds aggregate byte bound"
             {:maximum-bytes maximum-store-bytes :observed-bytes total}))
    counts))

(defn- inventory!
  [store]
  (with-secure-store-directories
   store
   #(secure-store-inventory! store %)))

(declare ^{:private true} validate-retained-entry-commit!
         ^{:private true} validate-retained-entry-references!
         ^{:private true} receipt-validation-ops
         ^{:private true} artifact-id-of
         ^{:private true} validate-artifact!)

(defn- retained-entry-references!
  [store directories entry-names validation-ops]
  (let [^SecureDirectoryStream entries (:entries directories)]
    (loop [remaining (seq entry-names) count 0
           blob-names #{} receipt-names #{}]
      (if-let [relative (first remaining)]
        (let [next-count (inc count)
              _ (when (> next-count maximum-entry-count)
                  (fail! "C16-POLICY"
                         "cache recovery entry traversal exceeds its bound"
                         {:maximum-count maximum-entry-count}))
              name (str relative)]
          (when-not (re-matches #"sha256:[0-9a-f]{64}\.edn" name)
            (fail! "C16-POLICY" "cache entry residue name is invalid"
                   {:name name}))
          (let [reference
                (try
                  (let [entry (decode-canonical-bytes
                               (secure-read-bytes!
                                store :entries entries relative
                                maximum-entry-bytes)
                               maximum-entry-bytes)
                        entry (validate-retained-entry-commit! name entry)
                        entry (validate-retained-entry-references!
                               store directories entry validation-ops)
                        blob-id (:blob-id entry)
                        receipt-id (:producer-receipt-id entry)]
                    (when (and (sha256-id? blob-id)
                               (sha256-id? receipt-id))
                      {:blob-name (str blob-id ".edn")
                       :receipt-name (str receipt-id ".edn")}))
                  (catch Throwable error
                    (if (fatal? error) (throw error) nil)))]
            (if reference
              (recur (next remaining) next-count
                     (conj blob-names (:blob-name reference))
                     (conj receipt-names (:receipt-name reference)))
              ;; An undecipherable retained entry could still be forensic
              ;; evidence.  Keep every CAS object rather than guessing.
              {:complete? false})))
        {:complete? true :blob-names blob-names
         :receipt-names receipt-names}))))

(defn- verified-pass-execution-receipt-id!
  [receipt]
  (let [calculator-var
        (ns-resolve 'gravity.pass-execution 'calculated-receipt-id)]
    (when-not calculator-var
      (fail! "C16-POLICY"
             "pass execution receipt identity verifier is unavailable" {}))
    (let [observed (require-sha256! :receipt-id (:receipt-id receipt))
          calculated ((var-get calculator-var) receipt)]
      (when-not (= observed calculated)
        (fail! "C16-ENTRY" "orphan receipt content identity is stale"
               {:observed observed}))
      observed)))

(defn- validate-retained-entry-references!
  [store directories entry validation-ops]
  (let [blob-name (sha-file-name! :blob-id (:blob-id entry) ".edn")
        receipt-name (sha-file-name! :producer-receipt-id
                                     (:producer-receipt-id entry) ".edn")
        ^SecureDirectoryStream blobs (:blobs directories)
        ^SecureDirectoryStream receipts (:receipts directories)]
    (when-not (and (secure-child-exists? blobs blob-name)
                   (secure-child-exists? receipts receipt-name))
      (fail! "C16-STALE" "retained commit references missing CAS objects" {}))
    (let [receipt (decode-canonical-bytes
                   (secure-read-bytes! store :receipts receipts receipt-name
                                       maximum-entry-bytes)
                   maximum-entry-bytes)
          _ (pass-execution/validate-execution-receipt!
             receipt (:contract entry)
             (receipt-validation-ops validation-ops))
          receipt-id (verified-pass-execution-receipt-id! receipt)
          request (-> (select-keys receipt execution-request-fields)
                      (assoc :contract (:contract entry)
                             :execution-mode :executed
                             :authority
                             (select-keys (:authority receipt)
                                          [:input-authorities :claimed-level
                                           :scope])))
          stage-key-var (ns-resolve 'gravity.pass-cache 'stage-cache-key)
          _ (when-not stage-key-var
                     (fail! "C16-POLICY"
                            "semantic stage key validator is unavailable" {}))
          reconstructed-key ((var-get stage-key-var) request)
          blob-bytes (secure-read-bytes! store :blobs blobs blob-name
                                         maximum-blob-bytes)
          _ (when-not (= (:blob-id entry)
                         (str "sha256:"
                              (digest/sha256-bytes-hex blob-bytes)))
              (fail! "C16-STALE"
                     "retained artifact blob content identity is stale" {}))
          expected-facts {:input (:input-facts receipt)
                          :output (:output-facts receipt)
                          :requires (:requires receipt)
                          :preserves (:preserves receipt)
                          :invalidates (:invalidates receipt)
                          :regenerates (:regenerates receipt)}
          expected-evidence
          {:verifier-ids (vec (sort (map :verifier-id
                                         (:verifier-reports receipt))))
           :evidence-ids (vec (sort (map :evidence-id
                                         (:evidence-records receipt))))}
          _ (when-not
              (and (= (:producer-receipt-id entry) receipt-id)
                   (= (:cache-key-id entry)
                      (:semantic-key-id reconstructed-key))
                   (= (:semantic-key-id entry)
                      (:semantic-key-id reconstructed-key))
                   (= (:stage entry) (:stage receipt)
                      (:stage reconstructed-key))
                   (= (:pass-contract-id entry)
                      (:pass-contract-id receipt)
                      (:pass-contract-id reconstructed-key))
                   (= (:contract entry) (:contract reconstructed-key))
                   (= (:pass-contract-id entry)
                      (pass-execution/pass-contract-id (:contract entry)))
                   (= (:artifact-id entry) (:output-artifact-id receipt))
                   (= (:facts entry) expected-facts)
                   (= (:evidence entry) expected-evidence)
                   (= (:provenance-id entry)
                      (get-in receipt [:provenance :provenance-id])
                      (get-in reconstructed-key [:provenance :provenance-id]))
                   (= (:diagnostic-schema-id entry)
                      (get-in receipt
                              [:semantic-bindings :diagnostic-schema-id])
                      (:diagnostic-schema-id reconstructed-key))
                   (= (:diagnostic-stream-id entry)
                      (:diagnostic-stream-id receipt)
                      (:diagnostic-stream-id reconstructed-key)))
              (fail! "C16-STALE"
                     "retained commit and referenced CAS objects are inconsistent"
                     {}))
          artifact (decode-canonical-bytes blob-bytes maximum-blob-bytes)
          artifact (validate-artifact!
                    validation-ops artifact
                    {:entry entry :key reconstructed-key})
          artifact-id (artifact-id-of validation-ops artifact)]
      (when-not (= artifact-id (:artifact-id entry))
        (fail! "C16-STALE"
               "retained artifact identity differs from its bound entry" {})))
    entry))

(defn- plan-unreferenced-files!
  [store path-key ^SecureDirectoryStream directory relative-names retained
   name-pattern maximum-bytes]
  (let [maximum-count (case path-key
                        :blobs maximum-blob-count
                        :receipts maximum-receipt-count)]
    (loop [remaining (seq relative-names) count 0 plan []]
     (if-let [relative (first remaining)]
      (let [next-count (inc count)
            _ (when (> next-count maximum-count)
                (fail! "C16-POLICY"
                       "cache recovery CAS traversal exceeds its bound"
                       {:path-key path-key :maximum-count maximum-count}))
            name (str relative)]
        (when-not (re-matches name-pattern name)
          (fail! "C16-POLICY" "cache CAS residue name is invalid"
                 {:path-key path-key :name name}))
        (when-not (contains? retained name)
          (let [bytes (secure-read-bytes!
                       store path-key directory relative maximum-bytes)
                expected-name
                (case path-key
                  :blobs (str "sha256:"
                              (digest/sha256-bytes-hex bytes) ".edn")
                  :receipts
                  (let [receipt (decode-canonical-bytes bytes maximum-entry-bytes)
                        id (verified-pass-execution-receipt-id! receipt)]
                    (str id ".edn"))
                  (fail! "C16-POLICY" "orphan recovery path is unsupported"
                         {:path-key path-key}))]
            (when-not (= name expected-name)
              (fail! "C16-ENTRY"
                     "corrupt unreferenced CAS object is retained"
                     {:path-key path-key :name name}))))
        (recur (next remaining) next-count
               (if (contains? retained name) plan (conj plan relative))))
      plan))))

(defn- delete-planned-files!
  [store path-key ^SecureDirectoryStream directory plan maximum-bytes]
  (doseq [relative plan]
    (secure-file-attributes-relative!
     store path-key directory relative maximum-bytes)
    (.deleteFile directory relative))
  (when (seq plan)
    (secure-fsync-directory! directory))
  (count plan))

(defn- potential-entry-reference-sets
  [store directories entry-names]
  (let [^SecureDirectoryStream entries (:entries directories)]
    (loop [remaining (seq entry-names) blobs #{} receipts #{}]
      (if-let [relative (first remaining)]
        (let [name (str relative)
              reference
              (try
                (let [entry (validate-retained-entry-commit!
                             name
                             (decode-canonical-bytes
                              (secure-read-bytes!
                               store :entries entries relative
                               maximum-entry-bytes)
                              maximum-entry-bytes))]
                  [(str (:blob-id entry) ".edn")
                   (str (:producer-receipt-id entry) ".edn")])
                (catch Throwable error
                  (if (fatal? error) (throw error) nil)))]
          (if reference
            (recur (next remaining) (conj blobs (first reference))
                   (conj receipts (second reference)))
            nil))
        {:blobs blobs :receipts receipts}))))

(defn- recover-orphans-in-directories!
  [store directories validation-ops]
  ;; Complete the bounded, policy-valid inventory before deleting anything.
  (let [inventory (secure-store-inventory! store directories)
        entry-names (get-in inventory [:entries :names])
        blob-names (get-in inventory [:blobs :names])
        receipt-names (get-in inventory [:receipts :names])
        potential (potential-entry-reference-sets
                   store directories entry-names)
        recovery-needed?
        (or (nil? potential)
            (seq (set/difference
                  (into #{} (map str) blob-names)
                  (:blobs potential)))
            (seq (set/difference
                  (into #{} (map str) receipt-names)
                  (:receipts potential))))]
   ;; A bounded structural prepass avoids duplicate semantic validator calls
   ;; on ordinary hits while still detecting shared-blob orphan configurations.
   (if-not recovery-needed?
    {:complete? true :skipped? true}
    (let [{:keys [complete? blob-names receipt-names]}
          (retained-entry-references!
           store directories entry-names validation-ops)]
    (when complete?
      ;; Entries are the commit records.  Objects not named by any retained
      ;; entry can only be incomplete publication residue and are reclaimed
      ;; under the global cross-process store lock.
      (let [blob-plan (plan-unreferenced-files!
                       store :blobs (:blobs directories)
                       (get-in inventory [:blobs :names]) blob-names
                       #"sha256:[0-9a-f]{64}\.edn" maximum-blob-bytes)
            receipt-plan (plan-unreferenced-files!
                          store :receipts (:receipts directories)
                          (get-in inventory [:receipts :names]) receipt-names
                          #"sha256:[0-9a-f]{64}\.edn" maximum-entry-bytes)]
        ;; Both plans are fully content/name/canonical validated before the
        ;; first mutation, so a late invalid candidate preserves all residue.
        (delete-planned-files! store :blobs (:blobs directories)
                               blob-plan maximum-blob-bytes)
        (delete-planned-files! store :receipts (:receipts directories)
                               receipt-plan maximum-entry-bytes)))
    {:complete? complete? :skipped? false}))))

(defn- recover-orphans!
  [store validation-ops]
  (with-secure-store-directories
   store #(recover-orphans-in-directories! store % validation-ops)))

(defn- clean-staging!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [^SecureDirectoryStream staging (:staging directories)
           ;; First pass proves the complete directory is within policy before
           ;; cleanup mutates any residue.  SecureDirectoryStream permits only
           ;; one iterator, so retain the validated relative names for the
           ;; descriptor-relative mutation phase.
           residue-plan
           (loop [iterator (.iterator staging) count 0 plan []]
             (if (.hasNext iterator)
               (let [next-count (inc count)
                     item (.next iterator)
                     name (str (.getFileName ^Path item))]
                 (when (> next-count maximum-staging-count)
                   (fail! "C16-POLICY"
                          "cache staging traversal exceeds its bound"
                          {:maximum-count maximum-staging-count}))
                 (when-not (re-matches #"\.stage-[0-9a-f-]{36}\.tmp" name)
                   (fail! "C16-POLICY" "cache staging residue name is invalid"
                          {:name name}))
                 (recur iterator next-count
                        (conj plan (relative-name! name))))
               plan))
           touched? (volatile! false)]
       (doseq [relative residue-plan]
         (when-not (contains? @active-staging
                              (str (.resolve ^Path (:staging store) relative)))
           (secure-file-attributes-relative!
            store :staging staging relative maximum-file-bytes)
           (.deleteFile staging relative)
           (vreset! touched? true)))
       (when @touched?
         (secure-fsync-directory! staging)))))
  store)

;; The store bootstrap path takes the filesystem lock while cleaning staging
;; and inventorying the v2 tree.  Declare it before open-local-store so the
;; hosted Clojure compiler never relies on an unresolved forward reference.
(declare ^{:private true} with-global-store-lock)

(defn- with-cache-bootstrap-lock
  [^Path base ^SecureDirectoryStream base-directory operation]
  (let [lock-name (relative-name! ".cpcache-bootstrap.lock")
        partial-store {:base base}]
    (.lock store-bootstrap-lock)
    (try
      (when-not (secure-child-exists? base-directory lock-name)
        (try
          (secure-write-new! partial-store :base base-directory lock-name
                             (byte-array 0))
          (catch java.nio.file.FileAlreadyExistsException _ nil)))
      (secure-file-attributes-relative!
       partial-store :base base-directory lock-name 0)
      (let [raw (.newByteChannel base-directory lock-name
                                 (HashSet. [StandardOpenOption/READ
                                            StandardOpenOption/WRITE
                                            LinkOption/NOFOLLOW_LINKS])
                                 (make-array FileAttribute 0))
            channel (require-file-channel! raw "C16-POLICY"
                                           :cache-bootstrap-lock)]
        (with-open [channel channel]
          (let [file-lock (.lock channel)]
            (try (operation)
                 (finally (.release ^FileLock file-lock))))))
      (finally (.unlock store-bootstrap-lock)))))

(defn open-local-store
  "Open the explicit local cache below exactly `.cpcache/compiler-pass/v2`."
  [base-path]
  (let [base (absolute-base-path! base-path)]
    (when-not (Files/exists base nofollow-links)
      (fail! "C16-POLICY" "explicit cache base must already exist"
             {:path (str base)}))
    (verify-directory! base false)
    ;; Bootstrap the namespace through a held descriptor.  Path-based checks
    ;; below only materialize retained projections; creation itself is
    ;; anchored and fails closed when SecureDirectoryStream is unavailable.
    (let [base-identity (identity-of base false)]
      (with-open [raw-base (Files/newDirectoryStream base)]
        (let [base-directory (require-secure-directory-stream!
                              raw-base :cache-directory-bootstrap-base)]
          (verify-secure-directory-handle! base-directory base-identity)
          (with-cache-bootstrap-lock
           base base-directory
           (fn []
             (with-open [cpcache (secure-ensure-child-directory!
                                  base-directory ".cpcache" false)
                         compiler-pass (secure-ensure-child-directory!
                                        cpcache "compiler-pass" false)
                         root (secure-ensure-child-directory!
                               compiler-pass "v2" true)]
               (doseq [child ["blobs" "entries" "receipts" "locks" "staging"]]
                 (with-open [owned (secure-ensure-child-directory!
                                    root child true)]
                   (verify-secure-child-directory!
                    root (relative-name! child) true)))))))))
    (let [private-store (create-private-tree! base)
          store (assoc private-store
                       :schema-version schema-version
                       :store-policy store-policy
                       :directory-identities
                       (mapv (fn [[path owned?]] (identity-of path owned?))
                             [[(:base private-store) false]
                              [(:cpcache private-store) false]
                              [(:compiler-pass private-store) false]
                              [(:root private-store) true]
                              [(:blobs private-store) true]
                              [(:entries private-store) true]
                              [(:receipts private-store) true]
                              [(:locks private-store) true]
                              [(:staging private-store) true]]))]
      (with-global-store-lock store
        #(do (clean-staging! store)
             (inventory! store)))
      store)))

(defn- pass-request-fields
  [request]
  (when-not (map? request)
    (fail! "C16-KEY" "pass cache request must be a map" {}))
  request)

(defn- validate-stage-request!
  [request]
  (let [request (pass-request-fields request)
        contract (:contract request)]
    (when-not (= execution-request-fields (set (keys request)))
      (fail! "C16-KEY" "pass execution request has unknown or missing fields"
             {:expected execution-request-fields
              :observed (set (keys request))}))
    (pass-execution/validate-pass-contract! contract)
    (when-not (= (:stage request) (:pass contract))
      (fail! "D1-PIPELINE-ORDER" "request stage differs from pass contract"
             {:stage (:stage request) :pass (:pass contract)}))
    (require-sha256! :producer-binding-id (:producer-binding-id request))
    (sorted-sha-vector! :input-artifact-ids (:input-artifact-ids request))
    (when (empty? (:input-artifact-ids request))
      (fail! "D1-ARTIFACT-GAP" "pass cache requires input artifact ids" {}))
    (require-keyword-set! :input-facts (:input-facts request))
    (when-not (set/subset? (:requires contract) (:input-facts request))
      (fail! "C1-PASS-CONTRACT" "pass input lacks required facts"
             {:missing (vec (sort (set/difference (:requires contract)
                                                  (:input-facts request))))}))
    (when-not (set/subset? (:preserves contract) (:input-facts request))
      (fail! "C1-EVIDENCE-DROP" "pass preserves absent input facts"
             {:missing (vec (sort (set/difference (:preserves contract)
                                                  (:input-facts request))))}))
    (when-not (map? (:external-root-inputs request))
      (fail! "C16-KEY" "external-root inputs must be a map" {}))
    (doseq [[artifact-id descriptor] (:external-root-inputs request)]
      (require-sha256! :external-root-artifact-id artifact-id)
      (when-not (and (map? descriptor)
                     (= #{:kind :facts} (set (keys descriptor)))
                     (= (:input contract) (:kind descriptor))
                     (set? (:facts descriptor)))
        (fail! "C16-KEY" "external-root input descriptor is malformed"
               {:artifact-id artifact-id}))
      (require-keyword-set! :external-root-facts (:facts descriptor))
      (when-not (set/subset? (:facts descriptor) (:input-facts request))
        (fail! "C1-EVIDENCE-DROP" "external-root facts are absent from input facts"
               {:artifact-id artifact-id})))
    (when-not (set/subset? (set (keys (:external-root-inputs request)))
                           (set (:input-artifact-ids request)))
      (fail! "C16-KEY" "external roots must be declared input ids" {}))
    (let [bindings (:semantic-bindings request)]
      (when-not (and (map? bindings)
                     (= #{:compiler-id :capability-policy-id :facet-set-id
                          :provider-manifest-id :package-lock-id
                          :diagnostic-schema-id}
                        (set (keys bindings))))
        (fail! "C16-KEY" "semantic bindings are incomplete" {}))
      (doseq [[field value] bindings]
        (require-sha256! field value)))
    (doseq [field [:dependency-graph-id :build-effect-replay-id :profile-id
                   :target-id :diagnostic-stream-id]]
      (require-sha256! field (get request field)))
    (sorted-sha-vector! :policy-ids (:policy-ids request))
    (let [provenance (:provenance request)]
      (when-not (and (map? provenance)
                     (or (= #{:provenance-id :source-path :metadata}
                            (set (keys provenance)))
                         (= #{:provenance-id} (set (keys provenance))))
                     (sha256-id? (:provenance-id provenance))
                     (or (= #{:provenance-id} (set (keys provenance)))
                         (and (or (nil? (:source-path provenance))
                                  (string? (:source-path provenance)))
                              (map? (:metadata provenance)))))
        (fail! "C16-KEY" "provenance binding is malformed" {})))
    (when-not (= :executed (:execution-mode request))
      (fail! "C16-KEY" "pass cache keys require executed requests" {}))
    (validate-authority! (:authority request) (:input-artifact-ids request)
                         (:authority-ceiling contract))
    request))

(defn- key-preimage
  [request]
  (let [request (validate-stage-request! request)
        contract (:contract request)
        contract-id (pass-execution/pass-contract-id contract)]
    {:artifact :gravity/compiler-pass-cache-key
     :schema-version schema-version
     :canonicalizer-version canonicalizer-version
     :stage (:stage request)
     :pass-contract-id contract-id
     :contract contract
     :producer-binding-id (:producer-binding-id request)
     :input-artifact-ids (:input-artifact-ids request)
     :input-facts (:input-facts request)
     :external-root-inputs (:external-root-inputs request)
     :semantic-bindings (:semantic-bindings request)
     :dependency-graph-id (:dependency-graph-id request)
     :build-effect-replay-id (:build-effect-replay-id request)
     :profile-id (:profile-id request)
     :target-id (:target-id request)
     :policy-ids (:policy-ids request)
     :diagnostic-schema-id
     (get-in request [:semantic-bindings :diagnostic-schema-id])
     :diagnostic-stream-id (:diagnostic-stream-id request)
     :provenance (:provenance request)
     :authority (:authority request)}))

(defn- request-from-key
  [key]
  ;; Execution mode is fixed by this cache contract and intentionally omitted
  ;; from the semantic key projection.  Reinsert it before recomputation.
  (assoc (select-keys key execution-request-fields)
         :execution-mode :executed))

(defn stage-cache-key
  "Build a bounded semantic key over the full pass execution contract."
  [request]
  (let [preimage (key-preimage request)
        semantic-preimage
        (-> preimage
            (dissoc :authority :provenance)
            ;; The producer observes its authority scope, so cross-scope reuse
            ;; would not be semantically sound.  Authority levels remain
            ;; nonsemantic and are monotonically capped on every reuse.
            (assoc :authority-scope (get-in request [:authority :scope]))
            (assoc :provenance-id
                   (get-in request [:provenance :provenance-id])))
        key-id (content-id :gravity/compiler-pass-cache-key-v2
                           semantic-preimage)]
    (assoc preimage
           :semantic-key-id key-id
           :cache-key-id key-id
           :storage-key-id key-id)))

(defn- key-id
  [key]
  (let [id (or (:semantic-key-id key) (:cache-key-id key) (:storage-key-id key))]
    (require-sha256! :semantic-key-id id)
    id))

(defn- validate-key!
  [key]
  (when-not (map? key)
    (fail! "C16-KEY" "cache key must be a map" {}))
  (let [expected (stage-cache-key (request-from-key key))]
    (when-not (= (:semantic-key-id key) (:semantic-key-id expected))
      (fail! "C16-STALE" "cache key identity does not recompute"
             {:observed (:semantic-key-id key)
              :expected (:semantic-key-id expected)}))
    (when-not (= key expected)
      (fail! "C16-STALE" "cache key contains stale derived identity" {})))
  key)

(defn- lock-state
  [path]
  (let [id (str path)]
    (get (swap! in-process-key-locks
                (fn [locks]
                  (if-let [entry (get locks id)]
                    (assoc locks id (update entry :references inc))
                    (assoc locks id {:lock (ReentrantLock.) :references 1}))))
         id)))

(defn- release-lock-state!
  [path lock]
  (let [id (str path)]
    (swap! in-process-key-locks
           (fn [locks]
             (if-let [entry (get locks id)]
               (if-not (identical? lock (:lock entry))
                 locks
                 (if (= 1 (:references entry))
                   (dissoc locks id)
                   (assoc locks id (update entry :references dec))))
               locks)))))

(defn- with-global-store-lock
  [store thunk]
  (.lock store-bootstrap-lock)
  (try
    (with-secure-store-directories
     store
     (fn [directories]
       (let [^SecureDirectoryStream lock-directory (:locks directories)
             lock-name (relative-name! ".store.lock")]
         (when-not (secure-child-exists? lock-directory lock-name)
           (try
             (secure-write-new! store :locks lock-directory lock-name
                                (byte-array 0))
             (catch java.nio.file.FileAlreadyExistsException _ nil)))
         (secure-file-attributes-relative! store :locks lock-directory lock-name
                                            4096)
         (let [raw (.newByteChannel lock-directory lock-name
                                     (HashSet. [StandardOpenOption/READ
                                                StandardOpenOption/WRITE
                                                LinkOption/NOFOLLOW_LINKS])
                                     (make-array FileAttribute 0))
               channel (require-file-channel! raw "C16-POLICY"
                                              :secure-store-lock)]
           (with-open [channel channel]
             (let [file-lock (.lock channel)]
               (try
                 (binding [*store-lock-held* true]
                   (thunk))
                 (finally (.release ^FileLock file-lock)))))))))
  (finally (.unlock store-bootstrap-lock))))

(defn- with-key-lock-held
  [store id thunk]
  (let [relative (sha-file-name! :cache-key-id id ".lock")
        lock-id (str (:locks store) ":" id)
        {:keys [^ReentrantLock lock]} (lock-state lock-id)]
      (.lock lock)
      (try
        (with-secure-store-directories
         store
         (fn [directories]
           (let [^SecureDirectoryStream lock-directory (:locks directories)
                 _ (when-not (secure-child-exists? lock-directory relative)
                     (fail! "C16-POLICY"
                            "cache key lock disappeared before acquisition" {}))
                 _ (secure-file-attributes-relative!
                    store :locks lock-directory relative 4096)
                 raw (.newByteChannel lock-directory relative
                                      (HashSet. [StandardOpenOption/READ
                                                 StandardOpenOption/WRITE
                                                 LinkOption/NOFOLLOW_LINKS])
                                      (make-array FileAttribute 0))
                 channel (require-file-channel! raw "C16-POLICY"
                                                :secure-key-lock)]
             (with-open [channel channel]
               (let [file-lock (.lock channel)]
                 (try
                   (binding [*key-lock-held* true]
                     (thunk))
                   (finally (.release ^FileLock file-lock))))))))
        (finally
          (.unlock lock)
          (release-lock-state! lock-id lock)))))

(defn- ensure-key-lock-file!
  [store id]
  (let [relative (sha-file-name! :cache-key-id id ".lock")
        present?
        (with-secure-store-directories
         store
         (fn [directories]
           (let [lock-directory (:locks directories)]
             (when (secure-child-exists? lock-directory relative)
               (secure-file-attributes-relative!
                store :locks lock-directory relative 4096)
               true))))]
    (when-not present?
      (with-global-store-lock
       store
       (fn []
         (with-secure-store-directories
          store
          (fn [directories]
            (let [lock-directory (:locks directories)
                  existing? (secure-child-exists? lock-directory relative)]
              (when (and (not existing?)
                         (>= (get-in (secure-store-inventory!
                                     store directories) [:locks :count])
                             maximum-lock-count))
                (fail! "C16-POLICY" "cache lock admission exceeds its count bound"
                       {:maximum-lock-count maximum-lock-count}))
              (when-not existing?
                (try
                  (secure-write-new! store :locks lock-directory relative
                                     (byte-array 0))
                  (catch java.nio.file.FileAlreadyExistsException _ nil)))
              (secure-file-attributes-relative!
               store :locks lock-directory relative 4096)))))))
    relative))

(defn- with-key-lock
  [store id thunk]
  (if *store-lock-held*
    (with-key-lock-held store id thunk)
    (do
      ;; Global admission is released before the per-key lock is acquired, so
      ;; a producer never serializes unrelated keys and no lock inversion is
      ;; possible with the publication gate.
      (ensure-key-lock-file! store id)
      (with-key-lock-held store id thunk))))

(def ^:private cache-operation-fields
  #{:artifact-id-of :validate-artifact! :validate-output!
    :validation-binding-id :validate-diagnostic-stream!
    :validate-verifier-report! :validate-evidence-record!
    :produce! :verifier-reports :evidence-records})

(defn- validate-cache-operations!
  [operations]
  (when-not (map? operations)
    (fail! "C16-ENTRY" "cache validation operations must be a map" {}))
  (let [unknown (set/difference (set (keys operations))
                                cache-operation-fields)]
    (when (seq unknown)
      (fail! "C16-ENTRY" "cache validation operations contain unknown fields"
             {:unknown-fields (vec (sort unknown))})))
  (require-sha256! :validation-binding-id (:validation-binding-id operations))
  (when-not (fn? (:artifact-id-of operations))
    (fail! "C16-ENTRY" "cache artifact identity operation is required" {}))
  (let [artifact-validator? (fn? (:validate-artifact! operations))
        output-validator? (fn? (:validate-output! operations))]
    (when (= artifact-validator? output-validator?)
      (fail! "C16-ENTRY"
             "exactly one cache artifact validation operation is required"
             {:validate-artifact!? artifact-validator?
              :validate-output!? output-validator?})))
  (doseq [field [:validate-diagnostic-stream! :validate-verifier-report!
                 :validate-evidence-record!]]
    (when-not (fn? (get operations field))
      (fail! "C16-ENTRY" "cache receipt validator operation is required"
             {:field field})))
  operations)

(defn- receipt-validation-ops
  [operations]
  {:validate-diagnostic-stream!
   (:validate-diagnostic-stream! operations)
   :validate-verifier-report!
   (:validate-verifier-report! operations)
   :validate-evidence-record!
   (:validate-evidence-record! operations)})

(defn- artifact-id-of
  [operations artifact]
  (let [candidate (:artifact-id-of operations)
        id (cond
             (fn? candidate) (candidate artifact)
             (keyword? candidate) (get artifact candidate)
             (sha256-id? (:artifact-id artifact)) (:artifact-id artifact)
             :else (content-id :gravity/pass-cache-artifact-v2 artifact))]
    (require-sha256! :artifact-id id)))

(defn- validate-artifact!
  [operations artifact context]
  (cond
    (fn? (:validate-artifact! operations))
    ((:validate-artifact! operations) artifact (:entry context) (:key context))

    (fn? (:validate-output! operations))
    ((:validate-output! operations) artifact (:key context)
     (get-in context [:key :contract]))

    :else artifact))

(defn- receipt-output-compatible!
  [key receipt]
  (let [same-fields
        [[:stage :stage]
         [:pass-contract-id :pass-contract-id]
         [:producer-binding-id :producer-binding-id]
         [:input-artifact-ids :input-artifact-ids]
         [:input-facts :input-facts]
         [:external-root-inputs :external-root-inputs]
         [:semantic-bindings :semantic-bindings]
         [:dependency-graph-id :dependency-graph-id]
         [:build-effect-replay-id :build-effect-replay-id]
         [:profile-id :profile-id]
         [:target-id :target-id]
         [:policy-ids :policy-ids]
         [:diagnostic-stream-id :diagnostic-stream-id]]]
    (doseq [[key-field receipt-field] same-fields]
      (when-not (= (get key key-field) (get receipt receipt-field))
        (fail! "C16-STALE" "producer receipt does not bind the cache key"
               {:field key-field})))
    (when-not (= (get-in key [:provenance :provenance-id])
                 (get-in receipt [:provenance :provenance-id]))
      (fail! "C16-STALE" "producer receipt provenance differs from cache key" {}))
    (when-not (= (get-in key [:authority :scope])
                 (get-in receipt [:authority :scope]))
      (fail! "C16-STALE" "producer receipt authority scope differs from cache key"
             {}))
    receipt))

(defn- validate-receipt!
  "Validate the key-bound historical producer receipt before touching its blob."
  [key receipt validation-ops]
  ;; A cache key is the semantic projection of an executed request and does
  ;; not carry the execution-mode field itself.  Reconstruct that fixed mode
  ;; before applying the exact pass-execution request validator so hits cannot
  ;; bypass its full request invariants.
  (validate-stage-request!
   (assoc (select-keys key execution-request-fields)
          :execution-mode :executed))
  (receipt-output-compatible! key receipt)
  (pass-execution/validate-execution-receipt!
   receipt (:contract key) (receipt-validation-ops validation-ops))
  receipt)

(defn- validate-artifact-against-receipt!
  [key artifact receipt validation-ops context]
  (let [artifact (validate-artifact! validation-ops artifact context)
        id (artifact-id-of validation-ops artifact)]
    (when-not (= id (:output-artifact-id receipt))
      (fail! "C16-STALE" "artifact identity differs from producer receipt"
             {:observed id :expected (:output-artifact-id receipt)}))
    artifact))

(defn- entry-id
  [entry]
  (content-id :gravity/pass-cache-entry-v2 (dissoc entry :entry-id)))

(defn- blob-id
  [bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- receipt-id
  [receipt]
  (:receipt-id receipt))

(defn- cache-entry
  [key artifact-id artifact-bytes producer-receipt validation-ops]
  (let [base {:artifact :gravity/compiler-pass-cache-entry
              :schema-version schema-version
              :cache-key-id (key-id key)
              :semantic-key-id (:semantic-key-id key)
              :stage (:stage key)
              :pass-contract-id (:pass-contract-id key)
              :artifact-id artifact-id
              :blob-id (blob-id artifact-bytes)
              :producer-receipt-id (receipt-id producer-receipt)
              :contract (:contract key)
              :facts {:input (:input-facts producer-receipt)
                      :output (:output-facts producer-receipt)
                      :requires (:requires producer-receipt)
                      :preserves (:preserves producer-receipt)
                      :invalidates (:invalidates producer-receipt)
                      :regenerates (:regenerates producer-receipt)}
              :evidence {:verifier-ids (vec (sort (map :verifier-id
                                                        (:verifier-reports
                                                         producer-receipt))))
                         :evidence-ids (vec (sort (map :evidence-id
                                                        (:evidence-records
                                                         producer-receipt))))}
              :provenance-id (get-in key [:provenance :provenance-id])
              :validation-binding-id (:validation-binding-id validation-ops)
              :diagnostic-schema-id (:diagnostic-schema-id key)
              :diagnostic-stream-id (:diagnostic-stream-id key)
              :authority {:local? true :speculative? true
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}}]
    (assoc base :entry-id (entry-id base))))

(def ^:private cache-entry-fields
  #{:artifact :schema-version :cache-key-id :semantic-key-id :stage
    :pass-contract-id :artifact-id :blob-id :producer-receipt-id :contract
    :facts :evidence :provenance-id :validation-binding-id
    :diagnostic-schema-id :diagnostic-stream-id :authority :entry-id})

(defn- validate-entry-nested-schema!
  [entry]
  (let [facts (:facts entry)
        evidence (:evidence entry)
        authority (:authority entry)]
    (when-not (and (map? facts)
                   (= #{:input :output :requires :preserves :invalidates
                        :regenerates}
                      (set (keys facts)))
                   (every? #(and (set? %) (every? keyword? %)) (vals facts)))
      (fail! "C16-ENTRY" "cache entry facts schema is malformed" {}))
    (when-not (and (map? evidence)
                   (= #{:verifier-ids :evidence-ids} (set (keys evidence)))
                   (vector? (:verifier-ids evidence))
                   (vector? (:evidence-ids evidence)))
      (fail! "C16-ENTRY" "cache entry evidence schema is malformed" {}))
    (doseq [field [:verifier-ids :evidence-ids]]
      (sorted-sha-vector! field (get evidence field)))
    (when-not (and (map? authority)
                   (= #{:local? :speculative? :authoritative? :release?
                        :proof? :equivalence? :self-hosting?}
                      (set (keys authority)))
                   (every? boolean? (vals authority))
                   (true? (:local? authority))
                   (true? (:speculative? authority))
                   (false? (:authoritative? authority))
                   (false? (:release? authority))
                   (false? (:proof? authority))
                   (false? (:equivalence? authority))
                   (false? (:self-hosting? authority)))
      (fail! "C16-ENTRY" "cache entry authority schema is malformed" {})))
  entry)

(defn- validate-retained-entry-commit!
  [filename entry]
  (when-not (and (map? entry)
                 (= cache-entry-fields (set (keys entry))))
    (fail! "C16-ENTRY" "cache entry has unknown or missing fields" {}))
  (doseq [field [:cache-key-id :semantic-key-id :pass-contract-id
                 :artifact-id :blob-id :producer-receipt-id :provenance-id
                 :validation-binding-id :diagnostic-schema-id
                 :diagnostic-stream-id :entry-id]]
    (require-sha256! field (get entry field)))
  (when-not (and (= :gravity/compiler-pass-cache-entry (:artifact entry))
                 (= schema-version (:schema-version entry))
                 (= (:entry-id entry) (entry-id entry))
                 (= filename (str (:cache-key-id entry) ".edn")))
    (fail! "C16-STALE"
           "retained cache entry is not an intact immutable commit"
           {:filename filename}))
  (validate-entry-nested-schema! entry)
  entry)

(defn- secure-admit-publication!
  [store directories key entry artifact-bytes receipt-bytes entry-bytes]
  (let [inventory (secure-store-inventory! store directories)
        blob-name (sha-file-name! :blob-id (:blob-id entry) ".edn")
        receipt-name (sha-file-name! :producer-receipt-id
                                     (:producer-receipt-id entry) ".edn")
        entry-name (sha-file-name! :cache-key-id (key-id key) ".edn")
        new-blob? (not (secure-child-exists? (:blobs directories) blob-name))
        new-receipt?
        (not (secure-child-exists? (:receipts directories) receipt-name))
        new-entry? (not (secure-child-exists? (:entries directories) entry-name))
        next-bytes (+ (reduce + 0 (map :bytes (vals inventory)))
                      (if new-blob? (alength artifact-bytes) 0)
                      (if new-receipt? (alength receipt-bytes) 0)
                      (if new-entry? (alength entry-bytes) 0))]
    (when (or (and new-blob?
                   (>= (get-in inventory [:blobs :count]) maximum-blob-count))
              (and new-receipt?
                   (>= (get-in inventory [:receipts :count])
                       maximum-receipt-count))
              (and new-entry?
                   (>= (get-in inventory [:entries :count]) maximum-entry-count))
              (> next-bytes maximum-store-bytes))
      (fail! "C16-POLICY" "cache publication exceeds store admission policy"
             {:maximum-bytes maximum-store-bytes
              :observed-bytes next-bytes}))
    {:blob-name blob-name :receipt-name receipt-name :entry-name entry-name
     :inventory inventory}))

(defn- rejected-result
  [key diagnostic extra]
  {:status :rejected
   :key key
   :cache-evidence
   (merge {:artifact :gravity/pass-cache-evidence
           :schema-version schema-version
           :status :rejected
           :cache-key-id (key-id key)
           :contained-diagnostic diagnostic
           :local? true
           :speculative? true
           :authoritative? false
           :release? false
           :proof? false
           :equivalence? false
           :self-hosting? false}
          extra)})

(defn- validate-entry-record!
  [key entry]
  (validate-retained-entry-commit! (str (key-id key) ".edn") entry)
  (when-not (and
                 (= (key-id key) (:cache-key-id entry))
                 (= (:semantic-key-id key) (:semantic-key-id entry)))
    (fail! "C16-STALE" "cache entry identity or schema is stale" {}))
  ;; The key is bound through its id and all receipt fields; explicit contract
  ;; and stage identities catch key substitution before blob interpretation.
  (when-not (and (= (:stage key) (:stage entry))
                 (= (:pass-contract-id key) (:pass-contract-id entry))
                 (= (:contract key) (:contract entry)))
    (fail! "C16-STALE" "cache entry contract differs from key" {}))
  entry)

(defn- validate-entry-derived!
  [key entry receipt artifact-id validation-ops]
  (let [expected-facts {:input (:input-facts receipt)
                        :output (:output-facts receipt)
                        :requires (:requires receipt)
                        :preserves (:preserves receipt)
                        :invalidates (:invalidates receipt)
                        :regenerates (:regenerates receipt)}
        expected-evidence
        {:verifier-ids (vec (sort (map :verifier-id
                                       (:verifier-reports receipt))))
         :evidence-ids (vec (sort (map :evidence-id
                                       (:evidence-records receipt))))}]
    (when-not (and (= (:output-artifact-id receipt) artifact-id)
                   (= (:artifact-id entry) artifact-id)
                   (= (:producer-receipt-id entry) (:receipt-id receipt))
                   (= (:cache-key-id entry) (key-id key))
                   (= (:semantic-key-id entry) (:semantic-key-id key))
                   (= (:provenance-id entry)
                      (get-in key [:provenance :provenance-id]))
                   (= (:validation-binding-id entry)
                      (:validation-binding-id validation-ops))
                   (= (:diagnostic-schema-id entry)
                      (:diagnostic-schema-id key))
                   (= (:diagnostic-stream-id entry)
                      (:diagnostic-stream-id key))
                   (= (:facts entry) expected-facts)
                   (= (:evidence entry) expected-evidence))
      (fail! "C16-STALE"
             "cache entry derived facts, evidence, or bindings are stale" {})))
  entry)

(defn- build-reuse-receipt
  [key entry artifact producer-receipt validation-ops]
  (let [current-authority (:authority key)
        current-levels (vec (vals (:input-authorities current-authority)))
        ceiling (get-in key [:contract :authority-ceiling])
        historical-effective-level (get-in producer-receipt
                                           [:authority :effective-level])
        effective-level
        (weakest-authority
         (conj current-levels ceiling (:claimed-level current-authority)
               historical-effective-level))
        current-authority-id
        (content-id :gravity/pass-cache-current-authority-v2
                    current-authority)
        validation-id
        (content-id :gravity/pass-cache-validation-v2
                    {:cache-key-id (key-id key)
                     :artifact-id (:artifact-id entry)
                     :producer-receipt-id (:producer-receipt-id entry)
                     :diagnostic-schema-id (:diagnostic-schema-id key)
                     :diagnostic-stream-id (:diagnostic-stream-id key)
                     :validation-binding-id (:validation-binding-id validation-ops)
                     :current-authority-id current-authority-id
                     :current-authority current-authority
                     :verifier-ids (get-in entry [:evidence :verifier-ids])
                     :evidence-ids (get-in entry [:evidence :evidence-ids])
                     :provenance-id (:provenance-id entry)})
        base {:artifact :gravity/pass-cache-reuse-receipt
              :schema-version schema-version
              :stage (:stage key)
              :reuse-receipt-id nil
              :cache-key-id (key-id key)
              :semantic-key-id (:semantic-key-id key)
              :pass-contract-id (:pass-contract-id key)
              :contract (:contract key)
              :historical-producer-receipt-id (:receipt-id producer-receipt)
              :current-key-id (key-id key)
              :current-artifact-id (:artifact-id entry)
              :artifact-id (:artifact-id entry)
              :blob-id (:blob-id entry)
              :current-validation-id validation-id
              :validation-binding-id (:validation-binding-id validation-ops)
              :current-authority current-authority
              :current-authority-id current-authority-id
              :validation-ids {:current-artifact-validation-id validation-id
                               :current-validation-binding-id
                               (:validation-binding-id validation-ops)
                               :current-authority-id current-authority-id
                               :diagnostic-stream-id (:diagnostic-stream-id key)
                               :diagnostic-schema-id (:diagnostic-schema-id key)
                               :revalidated-historical-verifier-ids
                               (get-in entry [:evidence :verifier-ids])
                               :revalidated-historical-evidence-ids
                               (get-in entry [:evidence :evidence-ids])}
              :facts (:facts entry)
              :evidence (:evidence entry)
              :provenance-id (:provenance-id entry)
              :execution-mode :cache-reuse
              :authority {:local? true :speculative? true
                          :effective-level effective-level
                          :current-effective-level effective-level
                          :historical-effective-level historical-effective-level
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}
              :claims {:release? false :proof? false :self-hosting? false
                       :equivalence? false :authoritative? false}}]
    (assoc base :reuse-receipt-id
           (content-id :gravity/pass-cache-reuse-receipt-v2
                       (dissoc base :reuse-receipt-id)))))

(defn- lookup-unlocked!
  [store key validation-ops]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [entry-name (sha-file-name! :cache-key-id (key-id key) ".edn")
           ^SecureDirectoryStream entry-directory (:entries directories)]
       (if-not (secure-child-exists? entry-directory entry-name)
         {:status :miss
          :key key
          :cache-evidence {:artifact :gravity/pass-cache-evidence
                           :schema-version schema-version
                           :status :miss
                           :cache-key-id (key-id key)
                           :local? true :speculative? true
                           :authoritative? false :release? false :proof? false
                           :equivalence? false :self-hosting? false}}
         (try
           (let [entry-bytes (secure-read-bytes!
                              store :entries entry-directory entry-name
                              maximum-entry-bytes)
                 entry (validate-entry-record!
                        key (decode-canonical-bytes entry-bytes
                                                    maximum-entry-bytes))
                 receipt-name (sha-file-name!
                               :producer-receipt-id
                               (:producer-receipt-id entry) ".edn")
                 ^SecureDirectoryStream receipt-directory (:receipts directories)
                 _ (when-not (secure-child-exists?
                              receipt-directory receipt-name)
                     (fail! "C16-STALE" "producer receipt is missing" {}))
                 receipt-bytes (secure-read-bytes!
                                store :receipts receipt-directory receipt-name
                                maximum-entry-bytes)
                 receipt (decode-canonical-bytes receipt-bytes
                                                  maximum-entry-bytes)
                 _ (when-not (= (:producer-receipt-id entry)
                                (:receipt-id receipt))
                     (fail! "C16-STALE" "producer receipt identity is stale" {}))
                 ;; Receipt-first ordering prevents an untrusted blob from
                 ;; being interpreted before its producer attestation binds to
                 ;; this key and survives all current validators.
                 _ (validate-receipt! key receipt validation-ops)
                 blob-name (sha-file-name! :blob-id (:blob-id entry) ".edn")
                 ^SecureDirectoryStream blob-directory (:blobs directories)
                 _ (when-not (secure-child-exists? blob-directory blob-name)
                     (fail! "C16-STALE" "artifact blob is missing" {}))
                 artifact-bytes (secure-read-bytes!
                                store :blobs blob-directory blob-name
                                maximum-blob-bytes)
                 _ (when-not (= (:blob-id entry) (blob-id artifact-bytes))
                     (fail! "C16-STALE" "artifact blob identity is stale" {}))
                 artifact (decode-canonical-bytes artifact-bytes
                                                   maximum-blob-bytes)
                 artifact (validate-artifact-against-receipt!
                           key artifact receipt validation-ops
                           {:entry entry :key key})
                 artifact-id (artifact-id-of validation-ops artifact)
                 _ (validate-entry-derived!
                    key entry receipt artifact-id validation-ops)
                 reuse (build-reuse-receipt key entry artifact receipt
                                            validation-ops)]
             {:status :hit :key key :artifact artifact
              :producer-receipt receipt :cache-entry entry
              :reuse-receipt reuse
              :cache-evidence {:artifact :gravity/pass-cache-evidence
                               :schema-version schema-version :status :hit
                               :cache-key-id (key-id key)
                               :artifact-id (:artifact-id entry)
                               :producer-receipt-id (:producer-receipt-id entry)
                               :artifact-reused? true
                               :producer-executed? false
                               :reader-executed? false
                               :reuse-receipt-id (:reuse-receipt-id reuse)
                               :local? true :speculative? true
                               :authoritative? false :release? false
                               :proof? false :equivalence? false
                               :self-hosting? false}})
           (catch Throwable error
             (if (fatal? error)
               (throw error)
               (let [data (if (instance? clojure.lang.ExceptionInfo error)
                            (ex-data error) {})]
                 (rejected-result key (or (:id data) "C16-ENTRY")
                                  {:artifact-id (:artifact-id
                                                 (when (map? data) data))}))))))))))

(defn lookup!
  "Read and fully revalidate one immutable cache entry, if present."
  [store key validation-ops]
  (validate-store! store)
  (validate-key! key)
  (validate-cache-operations! validation-ops)
  (try
    (with-global-store-lock
     store #(recover-orphans! store validation-ops))
    (if *key-lock-held*
      (lookup-unlocked! store key validation-ops)
      (with-key-lock store (key-id key)
        #(lookup-unlocked! store key validation-ops)))
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        (let [data (if (instance? clojure.lang.ExceptionInfo error)
                     (ex-data error) {})]
          (rejected-result key (or (:id data) "C16-ENTRY")
                           {:artifact-id (:artifact-id
                                          (when (map? data) data))}))))))

(defn- store-unlocked!
  [store key artifact producer-receipt validation-ops skip-artifact-validation?]
  (let [contract (:contract key)
        _ (receipt-output-compatible! key producer-receipt)
        _ (pass-execution/validate-execution-receipt!
           producer-receipt contract (receipt-validation-ops validation-ops))
        artifact (if skip-artifact-validation?
                   artifact
                   (validate-artifact! validation-ops artifact
                                       {:entry nil :key key}))
        artifact-id (artifact-id-of validation-ops artifact)
        artifact-bytes (encoded-value artifact maximum-blob-bytes)
        _ (when-not (= artifact-id (:output-artifact-id producer-receipt))
            (fail! "C16-STALE" "artifact identity differs from producer receipt"
                   {:artifact-id artifact-id
                    :receipt-artifact-id (:output-artifact-id producer-receipt)}))
        entry (cache-entry key artifact-id artifact-bytes producer-receipt
                           validation-ops)
        ;; Content-addressed blobs and receipts are immutable.  Admission and
        ;; all publication occur under the short global CAS gate; the producer
        ;; itself ran only under its per-key lock.
        publication
        (with-global-store-lock
          store
          (fn []
            ;; Recovery consumes one bounded iterator per newly opened secure
            ;; directory.  Close that set before publication admission opens a
            ;; fresh set, while retaining the global store lock across phases.
            (recover-orphans! store validation-ops)
            (let [statuses
                  (with-secure-store-directories
                   store
                   (fn [directories]
                     (let [receipt-bytes
                           (encoded-value producer-receipt maximum-entry-bytes)
                           entry-bytes
                           (encoded-value entry maximum-entry-bytes)
                           {:keys [blob-name receipt-name entry-name]}
                           (secure-admit-publication!
                            store directories key entry artifact-bytes
                            receipt-bytes entry-bytes)
                           blob-publication
                           (publish-create-or-verify!
                            store directories :blobs (:blobs directories)
                            blob-name artifact-bytes maximum-blob-bytes)
                           receipt-publication
                           (publish-create-or-verify!
                            store directories :receipts (:receipts directories)
                            receipt-name receipt-bytes maximum-entry-bytes)
                           entry-publication
                           (publish-create-or-verify!
                            store directories :entries (:entries directories)
                            entry-name entry-bytes maximum-entry-bytes)]
                       {:blob-publication blob-publication
                        :receipt-publication receipt-publication
                        :entry-publication entry-publication})))
                  ;; Verify the post-publication store under the same global
                  ;; lock, but with a newly opened secure-directory set.
                  post-publication-inventory (inventory! store)]
              (assoc statuses
                     :post-publication-inventory
                     post-publication-inventory))))]
        {:status :stored
         :key key
         :artifact artifact
         :producer-receipt producer-receipt
         :cache-entry entry
         :cache-evidence {:artifact :gravity/pass-cache-evidence
                          :schema-version schema-version
                          :status :stored
                          :cache-key-id (key-id key)
                          :artifact-id artifact-id
                          :producer-receipt-id (:receipt-id producer-receipt)
                          :reader-executed? true
                          :producer-executed? true
                          :artifact-reused? false
                          :cache-publication :published
                          :blob-publication (:blob-publication publication)
                          :receipt-publication
                          (:receipt-publication publication)
                          :entry-publication (:entry-publication publication)
                          :post-publication-inventory
                          (:post-publication-inventory publication)
                          :local? true :speculative? true
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}}))

(defn store!
  "Validate and immutably publish an accepted artifact and producer receipt."
  [store key artifact producer-receipt validation-ops]
  (validate-store! store)
  (validate-key! key)
  (validate-cache-operations! validation-ops)
  (if *key-lock-held*
    (store-unlocked! store key artifact producer-receipt validation-ops false)
    (with-key-lock store (key-id key)
      #(store-unlocked! store key artifact producer-receipt validation-ops false))))

(defn- execution-operations
  [operations]
  (let [required [:produce! :validate-output! :artifact-id-of
                  :verifier-reports :evidence-records]
        selected (select-keys operations required)]
    (when-not (every? #(fn? (get selected %)) required)
      (fail! "C16-ENTRY" "pass execution operations are incomplete"
             {:missing (vec (remove #(fn? (get selected %)) required))}))
    selected))

(defn lookup-or-compute!
  "Reuse a fully revalidated hit, or execute one producer pass and publish it."
  [store key execution-request operations]
  (validate-store! store)
  (validate-key! key)
  ;; The semantic id deliberately excludes monotone authority levels and
  ;; nonsemantic provenance detail.  Execution must nevertheless use the exact
  ;; request from which the supplied key was projected, or a weaker/currently
  ;; different request could borrow authority or provenance carried by `key`.
  (when-not (= key (stage-cache-key execution-request))
    (fail! "C16-KEY" "execution request does not bind supplied cache key" {}))
  (with-key-lock
    store (key-id key)
    (fn []
      ;; The lock is held across lookup and producer execution.  Therefore two
      ;; cooperative writers execute at most one producer for one semantic key.
      (let [looked-up (lookup! store key operations)]
        (if (= :hit (:status looked-up))
          looked-up
          (let [execution (pass-execution/execute-pass!
                           execution-request (execution-operations operations))
                rejected? (= :rejected
                             (get-in looked-up [:cache-evidence :status]))]
            (if rejected?
              {:status :miss
               :key key
               :artifact (:artifact execution)
               :producer-receipt (:receipt execution)
               :cache-evidence {:artifact :gravity/pass-cache-evidence
                                :schema-version schema-version
                                :status :miss
                                :cache-key-id (key-id key)
                                :cache-publication :withheld
                                :rejected-entry-evidence
                                (:cache-evidence looked-up)
                                :producer-executed? true
                                :reader-executed? true
                                :artifact-reused? false
                                :local? true :speculative? true
                                :authoritative? false :release? false
                                :proof? false :equivalence? false
                                :self-hosting? false}}
              (let [stored (store-unlocked! store key (:artifact execution)
                                             (:receipt execution)
                                             operations true)]
                (assoc stored :cache-evidence
                       (assoc (:cache-evidence stored)
                              :producer-executed? true
                              :reader-executed? true))))))))))
