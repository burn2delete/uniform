
;; This marker is denial-only: it grants no source or product authority.  The
;; V9 object-mode wrapper binds it before opening its private C/T session so a
;; legacy compatibility branch can never fall through to a checked-out path,
;; even if a later refactor accidentally reaches that branch.  The session's
;; unforgeable token remains the positive authority for every governed read.
(def ^:dynamic ^:private
  *p15-s23-sh07-b51-v9-receipted-bundle-construction* false)

(defn- p15-s23-sh07-b51-governed-read-fail!
  [reason details]
  (throw (ex-info "SH-07 B51 governed source read rejected"
                  {:id "SH07-B51-00945-GOVERNED-READ"
                   :reason reason
                   :details details})))

(def ^:private p15-s23-sh07-b51-git-limits
  {:stdout-max-bytes 1073741824
   :stderr-max-bytes 1048576
   :deadline-ms 3600000})

(def ^:private p15-s23-sh07-b51-checker-entrypoint
  "gravity.tools.sh07-b51-attempt32-checker-child")

(def ^:private p15-s23-sh07-b51-coordinator-entrypoint
  "gravity.tools.sh07-b51-attempt32-coordinator-child")

(def ^:private p15-s23-sh07-b51-coordinator-option-names
  ["--snapshot-commit"
   "--snapshot-tree"
   "--completed-at"
   "--evidence-relative-root"
   "--staging-root"
   "--eligibility-review-commit"
   "--eligibility-review-tree"
   "--eligibility-receipt-path"
   "--eligibility-receipt-blob"
   "--eligibility-receipt-byte-count"
   "--eligibility-receipt-raw-sha256"
   "--eligibility-receipt-id"])

(defn- p15-s23-sh07-b51-read-stream-bytes
  [^java.io.InputStream stream maximum coordinate]
  (let [output (java.io.ByteArrayOutputStream.)
        buffer (byte-array 8192)]
    (loop [byte-count 0]
      (let [count (.read stream buffer)]
        (if (neg? count)
          (.toByteArray output)
          (let [next-count (+ byte-count count)]
            (when (> next-count maximum)
              (p15-s23-sh07-b51-governed-read-fail!
               :git-stream-overflow
               {:coordinate coordinate
                :maximum maximum
                :observed-at-least next-count}))
            (.write output buffer 0 count)
            (recur next-count)))))))

(defn- p15-s23-sh07-b51-strict-utf8
  [bytes coordinate]
  (try
    (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                    (.onMalformedInput
                     java.nio.charset.CodingErrorAction/REPORT)
                    (.onUnmappableCharacter
                     java.nio.charset.CodingErrorAction/REPORT))]
      (str (.decode decoder (java.nio.ByteBuffer/wrap bytes))))
    (catch java.nio.charset.CharacterCodingException _
      (p15-s23-sh07-b51-governed-read-fail!
       :malformed-utf8 {:coordinate coordinate}))))

(defn- p15-s23-sh07-b51-raw-sha256
  [bytes]
  (str "sha256:" (sha256-bytes-hex bytes)))

(def ^:private p15-s23-sh07-b51-byte-array-class
  (Class/forName "[B"))

(defn- p15-s23-sh07-b51-byte-array?
  [value]
  (instance? p15-s23-sh07-b51-byte-array-class value))

(defn- p15-s23-sh07-b51-unsigned-byte-vector
  [bytes]
  (when-not (p15-s23-sh07-b51-byte-array? bytes)
    (p15-s23-sh07-b51-governed-read-fail!
     :object-proof-bytes {:observed-type (some-> bytes class .getName)}))
  (mapv #(bit-and 0xff (int %)) ^bytes bytes))

(defn- p15-s23-sh07-b51-unsigned-byte-array
  [value coordinate]
  (when-not (and (vector? value)
                 (every? #(and (integer? %) (<= 0 % 255)) value))
    (p15-s23-sh07-b51-governed-read-fail!
     :object-proof-bytes {:coordinate coordinate}))
  (byte-array (map unchecked-byte value)))

(defn- p15-s23-sh07-b51-byte-array-equal?
  [left right]
  (and (p15-s23-sh07-b51-byte-array? left)
       (p15-s23-sh07-b51-byte-array? right)
       (java.util.Arrays/equals ^bytes left ^bytes right)))

(defn- p15-s23-sh07-b51-hex-bytes
  [bytes]
  (let [digits "0123456789abcdef"]
    (apply str
           (mapcat (fn [value]
                     (let [unsigned (bit-and 0xff (int value))]
                       [(.charAt digits (unsigned-bit-shift-right unsigned 4))
                        (.charAt digits (bit-and unsigned 0x0f))]))
                   ^bytes bytes))))

(defn- p15-s23-sh07-b51-git-object-id
  [kind bytes]
  (when-not (and (contains? #{"blob" "tree" "commit"} kind)
                 (p15-s23-sh07-b51-byte-array? bytes))
    (p15-s23-sh07-b51-governed-read-fail!
     :git-object-id-input {:kind kind}))
  (let [digest (java.security.MessageDigest/getInstance "SHA-1")
        header (.getBytes (str kind " " (alength ^bytes bytes) "\u0000")
                          java.nio.charset.StandardCharsets/US_ASCII)]
    (.update digest ^bytes header)
    (.update digest ^bytes bytes)
    (p15-s23-sh07-b51-hex-bytes (.digest digest))))

(defn- p15-s23-sh07-b51-safe-proof-path-segments
  [path]
  (let [segments (when (string? path) (str/split path #"/" -1))]
    (when-not (and (vector? segments)
                   (not (str/blank? path))
                   (not (str/starts-with? path "/"))
                   (every? #(and (not (str/blank? %))
                                 (not (contains? #{"." ".."} %))
                                 (not (str/includes? % "\\"))
                                 (not (str/includes? % "\u0000")))
                           segments))
      (p15-s23-sh07-b51-governed-read-fail!
       :object-proof-path {:path path}))
    segments))

(defn- p15-s23-sh07-b51-parse-commit-tree!
  [raw-bytes coordinate]
  (let [text (p15-s23-sh07-b51-strict-utf8 raw-bytes coordinate)
        lines (str/split text #"\n" -1)
        first-line (first lines)
        tree (when first-line
               (second (re-matches #"tree ([0-9a-f]{40})" first-line)))
        later-tree-lines (filter #(str/starts-with? % "tree ") (rest lines))]
    (when-not (and tree (empty? later-tree-lines))
      (p15-s23-sh07-b51-governed-read-fail!
       :commit-object-tree-header
       {:coordinate coordinate :first-line first-line
        :later-tree-header-count (count later-tree-lines)}))
    tree))

(defn- p15-s23-sh07-b51-unsigned-bytes-compare
  [left right]
  (loop [index 0]
    (cond
      (= index (count left) (count right)) 0
      (= index (count left)) -1
      (= index (count right)) 1
      :else
      (let [comparison (compare (nth left index) (nth right index))]
        (if (zero? comparison)
          (recur (inc index))
          comparison)))))

(defn- p15-s23-sh07-b51-tree-sort-key
  [{:keys [name mode]}]
  (cond-> (p15-s23-sh07-b51-unsigned-byte-vector
           (.getBytes ^String name java.nio.charset.StandardCharsets/UTF_8))
    (= "40000" mode) (conj (int \/))))

(defn- p15-s23-sh07-b51-parse-tree-object!
  [raw-bytes coordinate]
  (let [length (alength ^bytes raw-bytes)
        entries
        (loop [index 0 result []]
          (if (= index length)
            result
            (let [space
                  (loop [cursor index]
                    (cond
                      (>= cursor length) nil
                      (= 32 (bit-and 0xff (aget ^bytes raw-bytes cursor))) cursor
                      :else (recur (inc cursor))))
                  nul
                  (when space
                    (loop [cursor (inc space)]
                      (cond
                        (>= cursor length) nil
                        (zero? (bit-and 0xff (aget ^bytes raw-bytes cursor))) cursor
                        :else (recur (inc cursor)))))
                  oid-start (when nul (inc nul))
                  oid-end (when oid-start (+ oid-start 20))]
              (when-not (and space (< index space) nul (< space nul)
                             (<= oid-end length))
                (p15-s23-sh07-b51-governed-read-fail!
                 :tree-object-grammar {:coordinate coordinate :offset index}))
              (let [mode-bytes (java.util.Arrays/copyOfRange
                                ^bytes raw-bytes index space)
                    name-bytes (java.util.Arrays/copyOfRange
                                ^bytes raw-bytes (inc space) nul)
                    oid-bytes (java.util.Arrays/copyOfRange
                               ^bytes raw-bytes oid-start oid-end)
                    mode (p15-s23-sh07-b51-strict-utf8
                          mode-bytes [coordinate :mode])
                    name (p15-s23-sh07-b51-strict-utf8
                          name-bytes [coordinate :name])]
                (when-not (and (contains? #{"40000" "100644"} mode)
                               (not (str/blank? name))
                               (not (contains? #{"." ".."} name))
                               (not (str/includes? name "/"))
                               (not (str/includes? name "\u0000")))
                  (p15-s23-sh07-b51-governed-read-fail!
                   :tree-object-entry
                   {:coordinate coordinate :mode mode :name name}))
                (recur oid-end
                       (conj result
                             {:mode mode :name name
                              :oid (p15-s23-sh07-b51-hex-bytes oid-bytes)}))))))]
    (when-not (and (= (count entries) (count (set (map :name entries))))
                   (every?
                    neg?
                    (map (fn [[left right]]
                           (p15-s23-sh07-b51-unsigned-bytes-compare
                            (p15-s23-sh07-b51-tree-sort-key left)
                            (p15-s23-sh07-b51-tree-sort-key right)))
                         (partition 2 1 entries))))
      (p15-s23-sh07-b51-governed-read-fail!
       :tree-object-order {:coordinate coordinate}))
    entries))
