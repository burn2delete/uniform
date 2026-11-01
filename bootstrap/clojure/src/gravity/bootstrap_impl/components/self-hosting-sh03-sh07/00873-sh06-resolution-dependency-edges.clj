

(defn
 sh06-resolution-dependency-edges
 [source-path module imports overrides]
 (gravity.resolution/dependency-edges
  source-path
  module
  imports
  overrides
  (resolution-stage-operations)))

(declare sh06-resolution-boundary-fail!)

(def ^:private sh06-resolution-b51-module-specs
  [{:module-ordinal 0 :module 'gravity.sh07-b51.producer-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
    :owner :r1-producer :exports []}
   {:module-ordinal 1 :module 'gravity.sh07-b51.producer-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
    :owner :r1-producer :exports []}
   {:module-ordinal 2 :module 'gravity.sh07-b51.producer-c
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
    :owner :r1-producer :exports '[sh07-b51-resolve-digest-preimage]}
   {:module-ordinal 3 :module 'gravity.sh07-b51.producer-d
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-build-template sh07-b51-verify-template
               sh07-b51-resolve-template sh07-b51-verify-resolved
               sh07-b51-build-final-artifact]}
   {:module-ordinal 4 :module 'gravity.sh07-b51.root6-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
    :owner :r6-independent-verifier :exports []}
   {:module-ordinal 5 :module 'gravity.sh07-b51.root6-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
    :owner :r6-independent-verifier
    :exports '[sh07-b51-build-independent-verifier-binding]}
   {:module-ordinal 6 :module 'gravity.sh07-b51.root8
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
    :owner :r8-finalizer :exports '[sh07-b51-finalize-rejection]}])

(def ^:private sh06-resolution-b51-effective-builtins
  '#{* + - / < <= = > >= assoc conj count first get hash-map list pr-str
     rest second str vector symbol? keyword? char? number? list? seq? vector?
     map? set? string? contains? even? set sort-by-pr-str vec quot subvec
     integer? boolean? keys sh07-canonical-text-compare
     sh07-declared-digest-hash sh07-observe-utf8-byte-count
     sh07-b51-admitted-executable-carrier-validation?})

(def ^:private sh06-resolution-b51-declared-builtin-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def ^:private sh06-resolution-b51-m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def ^:private sh06-resolution-b51-m1-keys
  #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(defn- sh06-resolution-c5-fail!
  [source-path reason observed]
  (sh06-resolution-boundary-fail!
   "C5-UNRESOLVED" source-path :authenticated-b51-external-binding-authority
   (assoc observed :reason reason)
   {:required [:complete-git-object-proof :complete-m0-m1-rederivation
               :authorized-cross-fragment-edge :lexical-authority]}))

(defn- sh06-resolution-exact-keys?
  [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn- sh06-resolution-sha256-id?
  [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- sh06-resolution-git-oid?
  [value]
  (and (string? value)
       (boolean (re-matches #"[0-9a-f]{40}" value))))

(defn- sh06-resolution-byte-vector?
  [value]
  (and (vector? value)
       (every? #(and (integer? %) (<= 0 % 255)) value)))

(defn- sh06-resolution-byte-array
  [value]
  (byte-array (map #(unchecked-byte (int %)) value)))

(defn- sh06-resolution-hex
  [bytes]
  (apply str (map #(format "%02x" (bit-and 0xff %)) bytes)))

(defn- sh06-resolution-git-object-id
  [kind payload]
  (let [digest (java.security.MessageDigest/getInstance "SHA-1")
        header (.getBytes (str kind " " (alength ^bytes payload) "\u0000")
                          java.nio.charset.StandardCharsets/US_ASCII)]
    (.update digest header)
    (.update digest ^bytes payload)
    (sh06-resolution-hex (.digest digest))))

(defn- sh06-resolution-strict-utf8
  [source-path value]
  (try
    (let [decoder (doto (.newDecoder java.nio.charset.StandardCharsets/UTF_8)
                    (.onMalformedInput
                     java.nio.charset.CodingErrorAction/REPORT)
                    (.onUnmappableCharacter
                     java.nio.charset.CodingErrorAction/REPORT))]
      (str (.decode decoder
                    (java.nio.ByteBuffer/wrap
                     (sh06-resolution-byte-array value)))))
    (catch Throwable error
      (sh06-resolution-c5-fail!
       source-path :malformed-utf8
       {:contained-host-error (.getName (class error))}))))

(defn- sh06-resolution-content-proof-tree!
  [source-path content proof]
  (when-not
   (and (sh06-resolution-exact-keys? content #{:commit :tree})
        (sh06-resolution-git-oid? (:commit content))
        (sh06-resolution-git-oid? (:tree content))
        (sh06-resolution-exact-keys? proof #{:oid :raw-bytes})
        (= (:commit content) (:oid proof))
        (sh06-resolution-byte-vector? (:raw-bytes proof)))
    (sh06-resolution-c5-fail!
     source-path :content-object-proof-shape {:content content}))
  (let [raw (sh06-resolution-byte-array (:raw-bytes proof))
        text (String. raw java.nio.charset.StandardCharsets/ISO_8859_1)
        split (.indexOf text "\n\n")
        header-text (if (neg? split) text (.substring text 0 split))
        headers (vec (.split header-text "\n" -1))
        tree-headers (filterv #(.startsWith ^String % "tree ") headers)
        leading (first headers)]
    (when-not
     (and (= (:commit content)
             (sh06-resolution-git-object-id "commit" raw))
          (= 1 (count tree-headers))
          (= (str "tree " (:tree content)) leading)
          (= leading (first tree-headers)))
      (sh06-resolution-c5-fail!
       source-path :commit-object-proof
       {:expected-commit (:commit content)
        :expected-tree (:tree content)})))
  true)

(defn- sh06-resolution-compare-byte-vectors
  [left right]
  (loop [index 0]
    (cond
      (= index (min (count left) (count right)))
      (compare (count left) (count right))

      (= (nth left index) (nth right index))
      (recur (inc index))

      :else (compare (nth left index) (nth right index)))))

(defn- sh06-resolution-tree-sort-key
  [{:keys [name-bytes mode]}]
  (cond-> name-bytes (= "40000" mode) (conj 47)))

(defn- sh06-resolution-parse-tree!
  [source-path tree-bytes]
  (let [size (count tree-bytes)]
    (loop [offset 0 rows []]
      (if (= offset size)
        (let [keys (mapv sh06-resolution-tree-sort-key rows)]
          (when-not
           (and (= (count rows) (count (set (map :name rows))))
                (every? neg?
                        (map sh06-resolution-compare-byte-vectors
                             keys (subvec keys 1))))
            (sh06-resolution-c5-fail!
             source-path :noncanonical-git-tree-order {}))
          rows)
        (let [space (first (filter #(= 32 (nth tree-bytes %))
                                   (range offset size)))
              nul (when space
                    (first (filter #(= 0 (nth tree-bytes %))
                                   (range (inc space) size))))
              oid-end (when nul (+ nul 21))]
          (when-not (and space nul (<= oid-end size) (< offset space)
                         (< (inc space) nul))
            (sh06-resolution-c5-fail!
             source-path :malformed-git-tree {:offset offset}))
          (let [mode (apply str (map char (subvec tree-bytes offset space)))
                name-bytes (subvec tree-bytes (inc space) nul)
                name (sh06-resolution-strict-utf8 source-path name-bytes)
                oid (sh06-resolution-hex
                     (sh06-resolution-byte-array
                      (subvec tree-bytes (inc nul) oid-end)))]
            (when-not
             (and (contains? #{"40000" "100644" "100755"} mode)
                  (not (or (empty? name) (= "." name) (= ".." name)
                           (.contains ^String name "/"))))
              (sh06-resolution-c5-fail!
               source-path :unsafe-git-tree-entry {:mode mode :name name}))
            (recur oid-end
                   (conj rows {:mode mode :name name
                               :name-bytes name-bytes :oid oid}))))))))
