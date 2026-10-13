
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
