(ns gravity.tools.sh07-b51-v9-codec
  "Closed canonical identity and bounded EDN transport seam for SH-07 B5.1 V9.

  This Clojure leaf is bootstrap TCB substrate.  It supplies no Gravity
  semantic authority and makes no release, self-hosting, seed-retirement,
  hostile-host, or aggregate SH-07 claim."
  (:require [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest])
  (:import [java.io ByteArrayOutputStream InputStream PushbackReader StringReader]
           [java.nio ByteBuffer]
           [java.nio.charset CodingErrorAction StandardCharsets]))

(def transport-limits
  "The literal V9 IPC transport limits.  Values are byte counts, not character
  counts or allocation hints."
  {:request-max-bytes 4194304
   :response-max-bytes 1073741824
   :stderr-max-bytes 1048576})

(def ^:private namespace-contract
  {:namespace 'gravity.tools.sh07-b51-v9-codec
   :contract-boundary :sh07-b51-v9-codec-tcb
   :identity-authority ['H 'W 'R]
   :transport-api 'transport-api
   :transport-limits
   {:request-max-bytes 4194304
    :response-max-bytes 1073741824
    :stderr-max-bytes 1048576}
   :dependency-direction
   {:requires ['gravity.c2-artifact-identity.canonical 'gravity.digest]
    :forbids ['gravity.bootstrap
              'gravity.c2-artifact-identity
              'gravity.tools.sh07-b51-attempt32-checker-child
              'gravity.tools.sh07-b51-attempt32-coordinator-child]}
   :authority
   {:bootstrap-tcb? true
    :gravity-semantic-authority? false
    :release? false
    :self-hosting? false
    :seed-retirement? false
    :hostile-host? false
    :aggregate-sh07? false}})

(def ^:private diagnostic-id "SH07-B51-CODEC")

(defn- fail!
  ([reason]
   (fail! reason {}))
  ([reason data]
   (throw
    (ex-info "SH-07 B5.1 V9 codec rejected input"
             (merge {:id diagnostic-id :reason reason} data)))))

(defn- byte-array!
  [value field]
  (when-not (bytes? value)
    (fail! :byte-array-required
           {:field field :actual-class (some-> value class .getName)}))
  value)

(defn- unicode-scalars!
  [^String text field]
  (loop [index 0]
    (when (< index (.length text))
      (let [unit (.charAt text index)]
        (cond
          (Character/isHighSurrogate unit)
          (if (and (< (inc index) (.length text))
                   (Character/isLowSurrogate (.charAt text (inc index))))
            (recur (+ index 2))
            (fail! :non-unicode-scalar
                   {:field field :utf16-index index}))

          (Character/isLowSurrogate unit)
          (fail! :non-unicode-scalar
                 {:field field :utf16-index index})

          :else
          (recur (inc index))))))
  text)

(declare validate-value!)

(defn- validate-metadata!
  [value]
  (when-let [metadata (meta value)]
    (validate-value! metadata [:metadata])))

(defn- scalar-value!
  [value path]
  (cond
    (string? value) (unicode-scalars! value path)
    (char? value) (when (Character/isSurrogate ^Character value)
                    (fail! :non-unicode-scalar {:field path}))
    (symbol? value) (unicode-scalars! (str value) path)
    (keyword? value) (unicode-scalars! (str value) path)
    :else nil)
  (when (or (symbol? value) (keyword? value))
    (validate-metadata! value))
  value)

(defn- reader-canonical-value
  [value]
  (letfn [(rcv [item]
            (canonical/reader-canonical-value rcv item))]
    (rcv value)))

(defn- comparator-key
  [value]
  (binding [*print-length* nil
            *print-level* nil
            *print-meta* true]
    (pr-str (reader-canonical-value value))))

(defn- reject-comparator-collisions!
  [kind values path]
  (reduce
   (fn [seen value]
     (let [encoded (comparator-key value)]
       (when (contains? seen encoded)
         (fail! :comparator-key-collision
                {:collection-kind kind :path path :encoded-key encoded}))
       (conj seen encoded)))
   #{}
   values))

(defn- validate-value!
  [value path]
  (cond
    (or (nil? value) (boolean? value) (integer? value)) value

    (or (string? value) (char? value) (keyword? value) (symbol? value))
    (scalar-value! value path)

    (record? value)
    (fail! :non-plain-edn
           {:path path :actual-class (.getName (class value))})

    (map? value)
    (do
      (doseq [[key item] value]
        (validate-value! key (conj path :key))
        (validate-value! item (conj path :value)))
      (reject-comparator-collisions! :map (keys value) path)
      (validate-metadata! value)
      value)

    (vector? value)
    (do
      (doseq [[ordinal item] (map-indexed vector value)]
        (validate-value! item (conj path ordinal)))
      (validate-metadata! value)
      value)

    (set? value)
    (do
      (doseq [item value]
        (validate-value! item (conj path :member)))
      (reject-comparator-collisions! :set value path)
      (validate-metadata! value)
      value)

    (list? value)
    (do
      (doseq [[ordinal item] (map-indexed vector value)]
        (validate-value! item (conj path ordinal)))
      (validate-metadata! value)
      value)

    :else
    (fail! :non-plain-edn
           {:path path :actual-class (some-> value class .getName)})))

(defn- ordered-value
  [value]
  (let [comparator #(compare (comparator-key %1) (comparator-key %2))]
    (cond
      (map? value)
      (into (sorted-map-by comparator)
            (map (fn [[key item]]
                   [(ordered-value key) (ordered-value item)]))
            value)

      (set? value)
      (into (sorted-set-by comparator) (map ordered-value) value)

      (vector? value) (mapv ordered-value value)
      (list? value) (apply list (map ordered-value value))
      :else value)))

(defn H
  "Return the exact domain-bearing V9 reader-canonical SHA-256 identity of
  VALUE.  VALUE must be in the closed plain-EDN domain."
  [value]
  (validate-value! value [])
  (letfn [(rcv [item]
            (canonical/reader-canonical-value rcv item))]
    (canonical/reader-canonical-hash digest/sha256-hex rcv value)))

(defn W
  "Return the exact V9 canonical wire bytes for VALUE: ordered plain EDN and
  exactly one trailing LF, encoded as UTF-8."
  [value]
  (validate-value! value [])
  (.getBytes
   (str (binding [*print-length* nil
                  *print-level* nil
                  *print-meta* true]
          (pr-str (ordered-value value)))
        "\n")
   StandardCharsets/UTF_8))

(defn R
  "Return the raw-byte V9 SHA-256 identity for BYTES.  No decoding or reader
  canonicalization is performed."
  [bytes]
  (byte-array! bytes :bytes)
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- maximum-for-role
  [role]
  (case role
    :request 4194304
    :response 1073741824
    :stderr 1048576
    (fail! :transport-role
           {:role role :allowed-roles [:request :response :stderr]})))

(defn- wire-role!
  [role]
  (when-not (or (= :request role) (= :response role))
    (fail! :wire-role
           {:role role :allowed-roles [:request :response]}))
  role)

(defn- bounded-byte-count!
  "Validate BYTE-COUNT against the exact cap for :request, :response, or
  :stderr.  Returns BYTE-COUNT unchanged."
  [role byte-count]
  (let [maximum (maximum-for-role role)]
    (when-not (and (integer? byte-count)
                   (<= 0 byte-count maximum))
      (fail! :transport-byte-count
             {:role role :byte-count byte-count :maximum maximum}))
    byte-count))

(defn- bounded-wire-bytes
  "Encode VALUE with W and enforce the exact :request or :response cap."
  [role value]
  (wire-role! role)
  (let [bytes (W value)]
    (bounded-byte-count! role (alength ^bytes bytes))
    bytes))

(defn- read-bounded-bytes
  "Drain INPUT incrementally through the exact cap for ROLE.  The caller owns
  timeout and concurrent process-drain policy; this helper owns only byte
  framing and the hard retained-byte bound."
  [role input]
  (when-not (instance? InputStream input)
    (fail! :input-stream-required
           {:role role :actual-class (some-> input class .getName)}))
  (let [maximum (maximum-for-role role)
        buffer (byte-array 8192)
        output (ByteArrayOutputStream. 8192)]
    (loop [byte-count 0]
      (let [read-count (.read ^InputStream input buffer 0 (alength buffer))]
        (cond
          (= -1 read-count)
          (.toByteArray output)

          (zero? read-count)
          (fail! :bounded-read-no-progress
                 {:role role :byte-count byte-count :maximum maximum})

          :else
          (let [next-count (+ (long byte-count) (long read-count))]
            (when (> next-count maximum)
              (fail! :transport-byte-count
                     {:role role :byte-count next-count :maximum maximum}))
            (.write output buffer 0 read-count)
            (recur next-count)))))))

(defn- decode-utf8
  [bytes]
  (let [decoder (doto (.newDecoder StandardCharsets/UTF_8)
                  (.onMalformedInput CodingErrorAction/REPORT)
                  (.onUnmappableCharacter CodingErrorAction/REPORT))]
    (try
      (.toString (.decode decoder (ByteBuffer/wrap bytes)))
      (catch java.nio.charset.CharacterCodingException _
        (fail! :malformed-utf8)))))

(defn- lexical-delimiter?
  [character]
  (or (Character/isWhitespace ^Character character)
      (= \, character)
      (= \; character)
      (= \( character)
      (= \) character)
      (= \[ character)
      (= \] character)
      (= \{ character)
      (= \} character)
      (= \" character)))

(defn- skip-separators
  [^String text start]
  (loop [index start]
    (if (< index (.length text))
      (let [character (.charAt text index)]
        (cond
          (or (Character/isWhitespace character) (= \, character))
          (recur (inc index))

          (= \; character)
          (let [next-index
                (loop [cursor (inc index)]
                  (if (and (< cursor (.length text))
                           (not= \newline (.charAt text cursor))
                           (not= \return (.charAt text cursor)))
                    (recur (inc cursor))
                    cursor))]
            (recur next-index))

          :else index))
      index)))

(declare scan-form-end!)

(defn- scan-string-end!
  [^String text start]
  (loop [index (inc start) escaped? false]
    (when (>= index (.length text))
      (fail! :lexical-string {:utf16-index start}))
    (let [character (.charAt text index)]
      (cond
        escaped? (recur (inc index) false)
        (= \\ character) (recur (inc index) true)
        (= \" character) (inc index)
        :else (recur (inc index) false)))))

(defn- scan-character-end!
  [^String text start]
  (let [first-index (inc start)]
    (when (>= first-index (.length text))
      (fail! :lexical-character {:utf16-index start}))
    (loop [index (inc first-index)]
      (if (and (< index (.length text))
               (not (lexical-delimiter? (.charAt text index))))
        (recur (inc index))
        index))))

(defn- scan-token-end!
  [^String text start]
  (loop [index start]
    (if (and (< index (.length text))
             (not (lexical-delimiter? (.charAt text index))))
      (recur (inc index))
      (if (= index start)
        (fail! :lexical-token {:utf16-index start})
        index))))

(defn- scan-delimited-end!
  [^String text start closing kind]
  (loop [index (inc start)
         seen #{}
         map-state (if (= :map kind) :key :element)]
    (let [form-start (skip-separators text index)]
      (when (>= form-start (.length text))
        (fail! :lexical-delimiter
               {:collection-kind kind :expected closing :utf16-index start}))
      (let [character (.charAt text form-start)]
        (if (= closing character)
          (do
            (when (= :value map-state)
              (fail! :map-odd-form-count {:utf16-index form-start}))
            (inc form-start))
          (let [form-end (scan-form-end! text form-start)]
            (if (= :key map-state)
              (let [lexeme (.substring text form-start form-end)]
                (when (contains? seen lexeme)
                  (fail! :duplicate-map-key
                         {:utf16-index form-start :key-source lexeme}))
                (recur form-end (conj seen lexeme) :value))
              (recur form-end seen
                     (if (= :value map-state) :key :element)))))))))

(defn- scan-form-end!
  [^String text start]
  (let [start (skip-separators text start)]
    (when (>= start (.length text))
      (fail! :empty-wire))
    (let [character (.charAt text start)]
      (case character
        \" (scan-string-end! text start)
        \\ (scan-character-end! text start)
        \( (scan-delimited-end! text start \) :list)
        \[ (scan-delimited-end! text start \] :vector)
        \{ (scan-delimited-end! text start \} :map)
        \^ (let [metadata-end (scan-form-end! text (inc start))]
             (scan-form-end! text metadata-end))
        \# (if (and (< (inc start) (.length text))
                    (= \{ (.charAt text (inc start))))
             (scan-delimited-end! text (inc start) \} :set)
             (fail! :reader-dispatch {:utf16-index start}))
        \) (fail! :lexical-delimiter {:utf16-index start :actual \)})
        \] (fail! :lexical-delimiter {:utf16-index start :actual \]})
        \} (fail! :lexical-delimiter {:utf16-index start :actual \}})
        (scan-token-end! text start)))))

(defn- lexical-preflight!
  [^String text]
  (let [start (skip-separators text 0)]
    (when (= start (.length text))
      (fail! :empty-wire))
    (let [form-end (scan-form-end! text start)
          trailing (skip-separators text form-end)]
      (when-not (= trailing (.length text))
        (fail! :multiple-forms {:utf16-index trailing}))))
  text)

(defn- read-one-edn!
  [^String text]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (let [eof (Object.)
          options {:eof eof
                   :readers {}
                   :default (fn [tag _]
                              (fail! :tagged-literal {:tag tag}))}]
      (try
        (binding [*read-eval* false]
          (let [value (clojure.edn/read options reader)
                trailing (clojure.edn/read options reader)]
            (when (identical? eof value)
              (fail! :empty-wire))
            (when-not (identical? eof trailing)
              (fail! :multiple-forms))
            value))
        (catch clojure.lang.ExceptionInfo error
          (if (= diagnostic-id (:id (ex-data error)))
            (throw error)
            (fail! :edn-read {:cause (.getName (class error))})))
        (catch Throwable error
          (fail! :edn-read {:cause (.getName (class error))}))))))

(defn- strict-read-wire
  "Decode one exact canonical W frame for :request or :response.  The byte cap
  is enforced before UTF-8 decoding; malformed/unmappable UTF-8, reader tags,
  duplicate map keys, multiple forms, non-plain EDN, and byte divergence all
  reject."
  [role bytes]
  (wire-role! role)
  (byte-array! bytes :wire-bytes)
  (bounded-byte-count! role (alength ^bytes bytes))
  (let [text (-> bytes decode-utf8 lexical-preflight!)
        value (read-one-edn! text)
        _ (validate-value! value [])
        canonical-bytes (W value)]
    (when-not (java.util.Arrays/equals ^bytes bytes ^bytes canonical-bytes)
      (fail! :wire-divergence
             {:role role
              :byte-count (alength ^bytes bytes)
              :canonical-byte-count (alength ^bytes canonical-bytes)}))
    value))

(defn- read-bounded-frame
  "Incrementally read and strictly decode one EOF-framed :request or
  :response."
  [role input]
  (wire-role! role)
  (strict-read-wire role (read-bounded-bytes role input)))

(defn- unsigned-byte-compare
  "Compare two byte arrays lexicographically with each octet interpreted as
  0..255.  A proper prefix sorts first."
  [left right]
  (byte-array! left :left)
  (byte-array! right :right)
  (let [left-count (alength ^bytes left)
        right-count (alength ^bytes right)
        shared-count (min left-count right-count)]
    (loop [index 0]
      (if (< index shared-count)
        (let [left-byte (bit-and 0xff (aget ^bytes left index))
              right-byte (bit-and 0xff (aget ^bytes right index))]
          (if (= left-byte right-byte)
            (recur (inc index))
            (compare left-byte right-byte)))
        (compare left-count right-count)))))

(defn- unsigned-wire-sort
  "Return ROWS as a vector sorted by unsigned lexicographic W bytes of
  (KEY-FN row).  Duplicate encoded keys reject before sorting."
  [key-fn rows]
  (when-not (ifn? key-fn)
    (fail! :key-function-required
           {:actual-class (some-> key-fn class .getName)}))
  (when-not (vector? rows)
    (fail! :row-vector-required
           {:actual-class (some-> rows class .getName)}))
  (let [decorated (mapv (fn [row] [(W (key-fn row)) row]) rows)
        encoded-keys (mapv (comp vec first) decorated)]
    (when-not (= (count encoded-keys) (count (set encoded-keys)))
      (fail! :duplicate-wire-sort-key {:row-count (count rows)}))
    (->> decorated
         (sort (fn [[left] [right]]
                 (unsigned-byte-compare left right)))
         (mapv second))))

(def transport-api
  "Immutable non-function API value for the codec's bounded transport and
  unsigned-ordering substrate.  H, W, and R are intentionally the only public
  function Vars in this namespace."
  {:strict-read-wire strict-read-wire
   :bounded-wire-bytes bounded-wire-bytes
   :read-bounded-bytes read-bounded-bytes
   :read-bounded-frame read-bounded-frame
   :bounded-byte-count! bounded-byte-count!
   :unsigned-byte-compare unsigned-byte-compare
   :unsigned-wire-sort unsigned-wire-sort})
