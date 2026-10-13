
(defn- cat-file-operation!
  [repository oid include-value?]
  (git-operation!
   repository :cat-file-blob ["cat-file" "blob" oid] #{0}
   (fn [bytes _]
     (cond-> {:oid oid
              :byte-count (alength ^bytes bytes)
              :raw-sha256 (codec/R bytes)}
       include-value? (assoc :value
                             ((:strict-read-wire transport-api)
                              :response bytes))))))

(defn- cat-file-bytes-operation!
  [repository oid]
  (let [result
        (run-bounded-process!
         {:argv (git-argv repository ["cat-file" "blob" oid])
          :directory repository
          :allowed-exit-codes #{0}
          :stdout-role :response
          :deadline-ms (:deadline-ms transport-limits)
          :id "SH07-B51-GIT-SNAPSHOT"})
        bytes (:stdout-bytes result)
        stderr (:stderr-bytes result)]
    (when-not (zero? (alength ^bytes stderr))
      (fail! "SH07-B51-GIT-SNAPSHOT" :git-stderr
             {:operation :cat-file-blob
              :stderr-byte-count (alength ^bytes stderr)
              :stderr-raw-sha256 (codec/R stderr)}))
    {:bytes bytes
     :observation
     {:operation :cat-file-blob
      :argv (:argv result)
      :stdin-byte-count 0
      :stdin-raw-sha256 empty-raw-sha256
      :exit-code 0
      :stdout-byte-count (alength ^bytes bytes)
      :stdout-raw-sha256 (codec/R bytes)
      :stderr-byte-count 0
      :stderr-raw-sha256 empty-raw-sha256
      :parsed-result {:oid oid
                      :byte-count (alength ^bytes bytes)
                      :raw-sha256 (codec/R bytes)}}}))

(def ^:private git-object-proof-id "SH07-B51-GIT-OBJECT-PROOF")

(def ^:private content-object-proof-keys #{:oid :raw-bytes})

(def ^:private source-object-proof-keys
  #{:schema :path :root-tree :tree-chain :blob-bytes})

(def ^:private tree-chain-row-keys
  #{:tree-oid :tree-bytes :component :selected-mode :selected-oid})

(defn- unsigned-byte-vector?
  [value]
  (and (vector? value)
       (<= (count value) (:response-max-bytes transport-limits))
       (every? #(and (integer? %) (<= 0 % 255)) value)))

(defn- bytes->unsigned-vector
  [bytes]
  (mapv #(bit-and (int %) 0xff) bytes))

(defn- unsigned-vector->bytes!
  [value reason]
  (when-not (unsigned-byte-vector? value)
    (fail! git-object-proof-id reason))
  (byte-array (map unchecked-byte value)))

(defn- bytes->lowercase-hex
  [bytes]
  (apply str (map #(format "%02x" (bit-and (int %) 0xff)) bytes)))

(defn- git-object-id
  [kind bytes]
  (when-not (contains? #{"blob" "commit" "tree"} kind)
    (fail! git-object-proof-id :object-kind {:kind kind}))
  (let [digest (java.security.MessageDigest/getInstance "SHA-1")
        header (.getBytes (str kind " " (alength ^bytes bytes) "\u0000")
                          StandardCharsets/US_ASCII)]
    (.update digest header)
    (.update digest ^bytes bytes)
    (bytes->lowercase-hex (.digest digest))))

(defn- byte-line-end
  [bytes start]
  (loop [index start]
    (cond
      (= index (alength ^bytes bytes)) nil
      (= 10 (bit-and (int (aget ^bytes bytes index)) 0xff)) index
      :else (recur (inc index)))))

(defn- bytes-prefix-at?
  [bytes index prefix]
  (let [prefix-bytes (.getBytes ^String prefix StandardCharsets/US_ASCII)]
    (and (<= (+ index (alength prefix-bytes)) (alength ^bytes bytes))
         (loop [offset 0]
           (or (= offset (alength prefix-bytes))
               (and (= (aget ^bytes bytes (+ index offset))
                       (aget prefix-bytes offset))
                    (recur (inc offset))))))))

(defn- parse-commit-tree!
  [bytes]
  (let [first-end (byte-line-end bytes 0)]
    (when-not first-end
      (fail! git-object-proof-id :commit-tree-header))
    (let [first-line (String. ^bytes bytes 0 first-end
                              StandardCharsets/US_ASCII)
          match (re-matches #"tree ([0-9a-f]{40})" first-line)
          tree-header-count
          (loop [index 0 count 0]
            (if (>= index (alength ^bytes bytes))
              count
              (let [line-end (or (byte-line-end bytes index)
                                 (alength ^bytes bytes))
                    next-count (if (bytes-prefix-at? bytes index "tree ")
                                 (inc count) count)]
                (recur (if (= line-end (alength ^bytes bytes))
                         line-end (inc line-end))
                       next-count))))]
      (when-not (and match (= 1 tree-header-count))
        (fail! git-object-proof-id :commit-tree-header
               {:tree-header-count tree-header-count}))
      (second match))))

(defn- index-of-byte
  [bytes start target]
  (loop [index start]
    (cond
      (= index (alength ^bytes bytes)) nil
      (= target (bit-and (int (aget ^bytes bytes index)) 0xff)) index
      :else (recur (inc index)))))

(defn- slice-bytes
  [bytes start end]
  (java.util.Arrays/copyOfRange ^bytes bytes (int start) (int end)))

(defn- git-tree-name-compare
  [left right]
  (let [left-name ^bytes (:name-bytes left)
        right-name ^bytes (:name-bytes right)
        left-count (alength left-name)
        right-count (alength right-name)]
    (loop [index 0]
      (let [left-value (if (< index left-count)
                         (bit-and (int (aget left-name index)) 0xff)
                         (if (= "40000" (:mode left)) 47 0))
            right-value (if (< index right-count)
                          (bit-and (int (aget right-name index)) 0xff)
                          (if (= "40000" (:mode right)) 47 0))]
        (cond
          (not= left-value right-value) (compare left-value right-value)
          (and (>= index left-count) (>= index right-count)) 0
          :else (recur (inc index)))))))

(defn- parse-git-tree-bytes!
  [tree-bytes]
  (let [bytes (unsigned-vector->bytes! tree-bytes :tree-byte-vector)
        byte-count (alength ^bytes bytes)
        entries
        (loop [index 0 rows []]
          (if (= index byte-count)
            rows
            (let [space (index-of-byte bytes index 32)
                  _ (when-not (and space (< index space)
                                   (<= (- space index) 6))
                      (fail! git-object-proof-id :tree-mode-grammar))
                  mode-bytes (slice-bytes bytes index space)
                  mode (String. mode-bytes StandardCharsets/US_ASCII)
                  _ (when-not (contains? #{"40000" "100644" "100755"}
                                         mode)
                      (fail! git-object-proof-id
                             (if (contains? #{"120000" "160000"} mode)
                               :forbidden-tree-mode :tree-mode-grammar)
                             {:mode mode}))
                  nul (index-of-byte bytes (inc space) 0)
                  _ (when-not (and nul (< (inc space) nul)
                                   (<= (+ nul 21) byte-count))
                      (fail! git-object-proof-id :tree-entry-grammar))
                  name-bytes (slice-bytes bytes (inc space) nul)
                  name (decode-utf8! name-bytes git-object-proof-id
                                     :tree-name-utf8)
                  _ (when-not (and (not (contains? #{"." ".."} name))
                                   (not (str/includes? name "/")))
                      (fail! git-object-proof-id :tree-name {:name name}))
                  oid-bytes (slice-bytes bytes (inc nul) (+ nul 21))]
              (recur (+ nul 21)
                     (conj rows {:mode mode :name name
                                 :name-bytes name-bytes
                                 :oid (bytes->lowercase-hex oid-bytes)})))))]
    (when-not (and (= (count entries) (count (distinct (map :name entries))))
                   (every? neg?
                           (map git-tree-name-compare entries (rest entries))))
      (fail! git-object-proof-id :tree-entry-order))
    entries))

(defn validate-content-object-proof!
  "Purely validate a complete raw Git commit payload against immutable C/T."
  [content proof]
  (exact-keys! content #{:commit :tree} git-object-proof-id :content)
  (exact-keys! proof content-object-proof-keys
               git-object-proof-id :content-object-proof-keys)
  (require-git-id! (:commit content) :commit git-object-proof-id)
  (require-git-id! (:tree content) :tree git-object-proof-id)
  (require-git-id! (:oid proof) :oid git-object-proof-id)
  (let [bytes (unsigned-vector->bytes! (:raw-bytes proof)
                                       :content-object-byte-vector)
        recomputed (git-object-id "commit" bytes)
        parsed-tree (parse-commit-tree! bytes)]
    (when-not (and (= (:commit content) (:oid proof) recomputed)
                   (= (:tree content) parsed-tree))
      (fail! git-object-proof-id :content-object-coordinate
             {:expected content :proof-oid (:oid proof)
              :recomputed-oid recomputed :parsed-tree parsed-tree})))
  proof)
