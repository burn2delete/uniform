;; Semantic C2 pass-cache component loaded by gravity.c2-pass-cache.
;; It deliberately evaluates in the facade namespace to retain private test seams.
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
