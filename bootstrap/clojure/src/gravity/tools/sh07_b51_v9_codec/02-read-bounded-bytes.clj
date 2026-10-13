
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
