(declare c-backend-canonical-value c4-artifact-id reader-canonical-value
         p15-s23-compiler-source-form-record-from-text
         p15-s23-compiler-def-value
         p15-s23-stage2-compiler-artifact-augmented-emitter
         sh06-resolution-definition-records sh06-resolution-core-records
         sh06-resolution-analysis-inputs sh06-resolution-package
         sh06-resolution-request
         sh06-resolution-artifact-from-sh05-artifact
         sh06-resolution-artifact-verification
         sh06-resolution-plan-identities
         sh06-resolution-source-relative-path
         sh06-resolution-expected-source-byte-count
         sh06-resolution-expected-source-content-hash
         sh06-resolution-expected-plan-semantic-hash
         sh06-resolution-expected-plan-id
         sh06-resolution-expected-functions-semantic-hash
         sh06-resolution-expected-function-count
         sh06-resolution-expected-function-names-hash
         sh06-resolution-expected-function-shapes-hash
         sh06-resolution-public-function-hashes
         sh06-resolution-public-function-shapes
         sh07-core-authenticated-request
         p15-s23-sh07-b51-derive-callsite-census
         p15-s23-stage2-construct-emitted-core-plan
         p15-s23-sh07-b51-resolution-plan-authority-valid?
         p15-s23-sh07-b51-plan-valid-structure?
         p15-s23-sh07-b51-plan-links-valid-structure?
         p15-s23-sh07-b51-execution-bundle-core-valid-structure?
         p15-s23-sh07-b51-execution-bundle-valid-structure?
         p15-s23-sh07-b51-execution-bundle-authorized?
         p15-s23-sh07-b51-current-authoritative-linked-products-v4
         ^:private p15-s23-sh07-b51-issue-top-level-validation-receipt!
         ^:private p15-s23-sh07-b51-retire-admitted-runtime-context!
         p15-s23-stage2-runtime-execute-function
         ^:private p15-s23-stage2-runtime-execute-function-scoped)

(def p15-s23-sh07-b51-module-specs
  [{:module-ordinal 0
    :module 'gravity.sh07-b51.producer-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 1
    :module 'gravity.sh07-b51.producer-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity"
    :owner :r1-producer
    :exports []}
   {:module-ordinal 2
    :module 'gravity.sh07-b51.producer-c
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-resolve-digest-preimage]}
   {:module-ordinal 3
    :module 'gravity.sh07-b51.producer-d
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity"
    :owner :r1-producer
    :exports '[sh07-b51-build-template
               sh07-b51-verify-template
               sh07-b51-resolve-template
               sh07-b51-verify-resolved
               sh07-b51-build-final-artifact]}
   {:module-ordinal 4
    :module 'gravity.sh07-b51.root6-a
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity"
    :owner :r6-independent-verifier
    :exports []}
   {:module-ordinal 5
    :module 'gravity.sh07-b51.root6-b
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity"
    :owner :r6-independent-verifier
    :exports '[sh07-b51-build-independent-verifier-binding]}
   {:module-ordinal 6
    :module 'gravity.sh07-b51.root8
    :logical-source-path
    "bootstrap/gravity/src/gravity/sh07_b51/root8.gravity"
    :owner :r8-finalizer
    :exports '[sh07-b51-finalize-rejection]}])

(def p15-s23-sh07-b51-public-root-specs
  [{:root-ordinal 1 :function 'sh07-b51-build-template :arity 1
    :module-ordinal 3}
   {:root-ordinal 2 :function 'sh07-b51-verify-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 3 :function 'sh07-b51-resolve-digest-preimage :arity 3
    :module-ordinal 2}
   {:root-ordinal 4 :function 'sh07-b51-resolve-template :arity 3
    :module-ordinal 3}
   {:root-ordinal 5 :function 'sh07-b51-verify-resolved :arity 4
    :module-ordinal 3}
   {:root-ordinal 6
    :function 'sh07-b51-build-independent-verifier-binding :arity 6
    :module-ordinal 5}
   {:root-ordinal 7 :function 'sh07-b51-build-final-artifact :arity 7
    :module-ordinal 3}
   {:root-ordinal 8 :function 'sh07-b51-finalize-rejection :arity 1
    :module-ordinal 6}])

(def ^:private p15-s23-sh07-b51-m0-keys
  #{:artifact :schema-version :module-source-id :module-ordinal :module
    :logical-source-path :owner :source-byte-count :source-sha256 :profile
    :target :effects :capabilities :safety :definitions :exports
    :core-binding-count :lexical-binding-count :external-bindings
    :aggregate-binding-count :builtin-calls})

(def ^:private p15-s23-sh07-b51-definition-keys
  #{:name :arity :visibility :source-hash})

(def ^:private p15-s23-sh07-b51-external-binding-keys
  #{:symbol :callee-module-ordinal :callee-module :callee-source-sha256
    :callee-definition-source-hash :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-builtin-call-keys
  #{:caller :caller-function-source-hash :builtin :arity
    :compiler-internal?})

(def ^:private p15-s23-sh07-b51-edge-keys
  #{:ordinal :caller-module-source-id :caller-function
    :caller-function-source-hash :callsite-path :callee-module-source-id
    :callee-function :callee-function-source-hash :arity :owner-edge
    :authorization})

(def ^:private p15-s23-sh07-b51-census-edge-keys
  #{:ordinal :caller-module-ordinal :caller-module :caller-function
    :callsite-path :callee-module-ordinal :callee-module :callee-function
    :arity :owner-edge})

(def ^:private p15-s23-sh07-b51-m1-keys
  #{:artifact :schema-version :manifest-id :module-source-ids :edges})

(def ^:private p15-s23-sh07-b51-m3-keys
  #{:artifact :schema-version :manifest-id :module-plans :routes
    :public-roots :bundle-core-id :compile-census-certificate-ids
    :bundle-id})

(def ^:private p15-s23-sh07-b51-bundle-core-keys
  #{:domain :schema-version :manifest-id :module-plans :routes
    :public-roots})

(def ^:private p15-s23-sh07-b51-root8-external-callees
  '[sh07-b51-build-template
    sh07-b51-entrypoint-envelope
    sh07-b51-exact-keys?
    sh07-b51-kind-remediation
    sh07-b51-kind-rule
    sh07-b51-nonnegative-integer?
    sh07-b51-pending-rejected-envelope-shape?
    sh07-b51-pending-rejection
    sh07-b51-raw-carrier-shape?
    sh07-b51-semantic-coordinate
    sh07-b51-sha256-id?
    sh07-b51-template-shape-for-plan?])

;; Attempt 30 cannot acquire implementation authority until both independent
;; census certificates and the resulting M3/root snapshot have been frozen.
;; These deliberately non-digest values fail every authority check and must be
;; replaced only by a separately reviewed immutable implementation snapshot.
(def ^:private p15-s23-sh07-b51-expected-m3-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-a-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-certificate-b-id
  :gravity/provisional-no-authority)
(def ^:private p15-s23-sh07-b51-expected-semantic-root
  :gravity/provisional-no-authority)

(def ^:private p15-s23-sh07-b51-source-specs
  (vec
   (concat
    [{:ordinal 0 :kind :l1-contract :module-ordinal nil
      :path "docs/phase-01-core-language/011-l1-surface-syntax-specification.md"}
     {:ordinal 1 :kind :l2-contract :module-ordinal nil
      :path "docs/phase-01-core-language/012-l2-core-language-semantics.md"}
     {:ordinal 2 :kind :checked-core :module-ordinal nil
      :path "bootstrap/gravity/src/gravity/checked_core.gravity"}]
    (mapv (fn [{:keys [module-ordinal logical-source-path]}]
            {:ordinal (+ 3 module-ordinal)
             :kind :module
             :module-ordinal module-ordinal
             :path logical-source-path})
          p15-s23-sh07-b51-module-specs)
    [{:ordinal 10 :kind :compiler :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/compiler.gravity"}
     {:ordinal 11 :kind :emitter :module-ordinal nil
      :path "bootstrap/gravity/p15_s23/emitter.gravity"}
     {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
      :path "bootstrap/gravity/src/gravity/resolution.gravity"}])))

(def ^:private p15-s23-sh07-b51-source-event-plan
  (vec
   (concat
    [{:ordinal 0 :domain :l1-contract :source-ordinal 0
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract}
     {:ordinal 1 :domain :l2-contract :source-ordinal 1
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract}
     {:ordinal 2 :domain :checked-core-revision :source-ordinal 2
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 3 module-ordinal)
       :domain :module-validation
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 10 module-ordinal)
       :domain :callsite-census
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer 'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 17 module-ordinal)
       :domain :link-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census})
    [{:ordinal 24 :domain :compiler-emitter :source-ordinal 10
      :module-ordinal nil
      :consumer 'gravity.bootstrap/p15-s23-sh07-b51-current-emitter}]
    (for [module-ordinal (range 7)]
      {:ordinal (+ 25 module-ordinal)
       :domain :source-authority
       :source-ordinal (+ 3 module-ordinal)
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-sh07-b51-current-module-source-authority})
    (for [module-ordinal (range 7)]
      {:ordinal (+ 32 module-ordinal)
       :domain :compiler-artifact-binding
       :source-ordinal 11
       :module-ordinal module-ordinal
       :consumer
       'gravity.bootstrap/p15-s23-stage2-compiler-artifact-binding})
    [{:ordinal 39 :domain :sh06-resolution-binding :source-ordinal 12
      :module-ordinal nil
      :consumer
      'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding}])))

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

(defn- p15-s23-sh07-b51-validate-content-object-proof!
  [content proof]
  (when-not (and (= #{:commit :tree} (set (keys content)))
                 (string? (:commit content))
                 (boolean (re-matches #"[0-9a-f]{40}" (:commit content)))
                 (string? (:tree content))
                 (boolean (re-matches #"[0-9a-f]{40}" (:tree content)))
                 (= #{:oid :raw-bytes} (set (keys proof))))
    (p15-s23-sh07-b51-governed-read-fail!
     :content-object-proof-shape {:content content}))
  (let [bytes (p15-s23-sh07-b51-unsigned-byte-array
               (:raw-bytes proof) :commit-object-proof)
        computed (p15-s23-sh07-b51-git-object-id "commit" bytes)
        tree (p15-s23-sh07-b51-parse-commit-tree!
              bytes :commit-object-proof)]
    (when-not (and (= (:commit content) (:oid proof) computed)
                   (= (:tree content) tree))
      (p15-s23-sh07-b51-governed-read-fail!
       :content-object-proof
       {:content content :proof-oid (:oid proof)
        :computed-oid computed :computed-tree tree}))
    true))

(defn- p15-s23-sh07-b51-validate-source-object-proof!
  [content descriptor proof]
  (when-not
   (and (= #{:schema :path :root-tree :tree-chain :blob-bytes}
           (set (keys proof)))
        (= :gravity/sh07-b51-git-tree-inclusion-proof-v1 (:schema proof))
        (= (:path descriptor) (:path proof))
        (= (:tree content) (:root-tree proof))
        (vector? (:tree-chain proof)))
    (p15-s23-sh07-b51-governed-read-fail!
     :source-object-proof-shape {:path (:path descriptor)}))
  (let [segments (p15-s23-sh07-b51-safe-proof-path-segments (:path proof))
        chain (:tree-chain proof)
        blob-bytes (p15-s23-sh07-b51-unsigned-byte-array
                    (:blob-bytes proof) [(:path proof) :blob])]
    (when-not (= (count segments) (count chain))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-object-proof-depth {:path (:path proof)}))
    (doseq [index (range (count chain))]
      (let [row (nth chain index)
            component (nth segments index)
            final? (= index (dec (count chain)))
            tree-bytes (when (map? row)
                         (p15-s23-sh07-b51-unsigned-byte-array
                          (:tree-bytes row) [(:path proof) index :tree]))
            entries (when tree-bytes
                      (p15-s23-sh07-b51-parse-tree-object!
                       tree-bytes [(:path proof) index]))
            matches (filterv #(= component (:name %)) entries)
            selected (first matches)
            expected-tree-oid
            (if (zero? index) (:tree content)
                (:selected-oid (nth chain (dec index))))]
        (when-not
         (and (= #{:tree-oid :tree-bytes :component
                   :selected-mode :selected-oid}
                 (set (keys row)))
              (= expected-tree-oid (:tree-oid row))
              (= (:tree-oid row)
                 (p15-s23-sh07-b51-git-object-id "tree" tree-bytes))
              (= component (:component row))
              (= 1 (count matches))
              (= (:mode selected) (:selected-mode row))
              (= (:oid selected) (:selected-oid row))
              (= (if final? "100644" "40000") (:selected-mode row)))
          (p15-s23-sh07-b51-governed-read-fail!
           :source-object-proof-chain
           {:path (:path proof) :index index}))))
    (let [leaf (peek chain)
          blob-oid (p15-s23-sh07-b51-git-object-id "blob" blob-bytes)]
      (when-not
       (and (= "100644" (:mode descriptor) (:selected-mode leaf))
            (= (:blob-oid descriptor) (:selected-oid leaf) blob-oid)
            (= (:byte-count descriptor) (alength ^bytes blob-bytes))
            (= (:raw-sha256 descriptor)
               (p15-s23-sh07-b51-raw-sha256 blob-bytes)))
        (p15-s23-sh07-b51-governed-read-fail!
         :source-object-proof-leaf {:path (:path proof)})))
    blob-bytes))

(defn- p15-s23-sh07-b51-git-arguments-valid?
  [arguments]
  (let [git-id? #(and (string? %)
                      (boolean (re-matches #"[0-9a-f]{40}" %)))
        source-paths (set (map :path p15-s23-sh07-b51-source-specs))]
    (and
     (vector? arguments)
     (or
      (and (= 3 (count arguments))
           (= ["rev-parse" "--verify"] (subvec arguments 0 2))
           (boolean
            (re-matches #"[0-9a-f]{40}\^\{(?:commit|tree)\}"
                        (nth arguments 2))))
      (and (= 5 (count arguments))
           (= ["ls-tree" "-z"] (subvec arguments 0 2))
           (git-id? (nth arguments 2))
           (= "--" (nth arguments 3))
           (contains? source-paths (nth arguments 4)))
      (and (= 3 (count arguments))
           (= "cat-file" (first arguments))
           (contains? #{"blob" "tree" "commit"} (second arguments))
           (git-id? (nth arguments 2)))))))

(defn- p15-s23-sh07-b51-remaining-millis
  [deadline-nanos]
  (max 0
       (long
        (Math/ceil
         (/ (double (- deadline-nanos (System/nanoTime))) 1000000.0)))))

(defn- p15-s23-sh07-b51-close-quietly!
  [value]
  (when value
    (try
      (.close ^java.io.Closeable value)
      (catch Throwable _ nil))))

(defn- p15-s23-sh07-b51-stop-git!
  [^Process process futures executor]
  (when (.isAlive process)
    (.destroy process)
    (when-not (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)
      (.destroyForcibly process)
      (.waitFor process 250 java.util.concurrent.TimeUnit/MILLISECONDS)))
  (doseq [stream [(.getOutputStream process)
                  (.getInputStream process)
                  (.getErrorStream process)]]
    (p15-s23-sh07-b51-close-quietly! stream))
  (doseq [future futures]
    (.cancel ^java.util.concurrent.Future future true))
  (.shutdownNow ^java.util.concurrent.ExecutorService executor))

(defn- p15-s23-sh07-b51-completed-git-task!
  [completion deadline-nanos]
  (let [remaining (p15-s23-sh07-b51-remaining-millis deadline-nanos)
        future (.poll ^java.util.concurrent.ExecutorCompletionService
                      completion remaining
                      java.util.concurrent.TimeUnit/MILLISECONDS)]
    (when-not future
      (p15-s23-sh07-b51-governed-read-fail!
       :git-timeout {:deadline-ms (:deadline-ms p15-s23-sh07-b51-git-limits)}))
    (try
      (.get ^java.util.concurrent.Future future)
      (catch java.util.concurrent.ExecutionException error
        (throw (.getCause error))))))

(defn- p15-s23-sh07-b51-git-bytes
  [arguments]
  (when-not (and (vector? arguments)
                 (every? string? arguments)
                 (p15-s23-sh07-b51-git-arguments-valid? arguments))
    (p15-s23-sh07-b51-governed-read-fail!
     :git-argv {:arguments arguments}))
  (let [argv (into ["/usr/bin/git" "-C" (System/getProperty "user.dir")]
                   arguments)
        process (-> (ProcessBuilder. ^java.util.List argv) .start)
        executor (java.util.concurrent.Executors/newFixedThreadPool 3)
        completion (java.util.concurrent.ExecutorCompletionService. executor)
        stdout-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stdout
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getInputStream process)
                       (:stdout-max-bytes p15-s23-sh07-b51-git-limits)
                       :stdout)})))
        stderr-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_]
                     {:kind :stderr
                      :bytes
                      (p15-s23-sh07-b51-read-stream-bytes
                       (.getErrorStream process)
                       (:stderr-max-bytes p15-s23-sh07-b51-git-limits)
                       :stderr)})))
        exit-future
        (.submit completion
                 ^java.util.concurrent.Callable
                 (reify java.util.concurrent.Callable
                   (call [_] {:kind :exit :value (.waitFor process)})))
        futures [stdout-future stderr-future exit-future]
        deadline-nanos
        (+ (System/nanoTime)
           (* 1000000 (:deadline-ms p15-s23-sh07-b51-git-limits)))
        completed? (atom false)]
    (.close (.getOutputStream process))
    (try
      (let [results
            (loop [remaining 3 values {}]
              (if (zero? remaining)
                values
                (let [{:keys [kind] :as result}
                      (p15-s23-sh07-b51-completed-git-task!
                       completion deadline-nanos)]
                  (when (contains? values kind)
                    (p15-s23-sh07-b51-governed-read-fail!
                     :git-task-duplicate {:kind kind}))
                  (recur (dec remaining) (assoc values kind result)))))
            stdout (get-in results [:stdout :bytes])
            stderr (get-in results [:stderr :bytes])
            exit (get-in results [:exit :value])]
        (when-not (and (zero? exit) (zero? (alength ^bytes stderr)))
          (p15-s23-sh07-b51-governed-read-fail!
           :git-object-read
           {:arguments arguments
            :exit exit
            :stdout-byte-count (alength ^bytes stdout)
            :stderr-byte-count (alength ^bytes stderr)
            :stderr (p15-s23-sh07-b51-strict-utf8 stderr :git-stderr)}))
        (reset! completed? true)
        stdout)
      (finally
        (if @completed?
          (do
            (doseq [stream [(.getInputStream process) (.getErrorStream process)]]
              (p15-s23-sh07-b51-close-quietly! stream))
            (.shutdownNow executor))
          (p15-s23-sh07-b51-stop-git! process futures executor))))))

(defn- p15-s23-sh07-b51-single-lf-line
  [bytes coordinate]
  (let [text (p15-s23-sh07-b51-strict-utf8 bytes coordinate)]
    (when-not (boolean (re-matches #"[^\r\n]+\n" text))
      (p15-s23-sh07-b51-governed-read-fail!
       :git-line-grammar {:coordinate coordinate}))
    (subs text 0 (dec (count text)))))

(defn- p15-s23-sh07-b51-assert-content-coordinate!
  [commit tree phase ordinal]
  (let [actual-commit
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{commit}")])
         [phase ordinal :commit])
        actual-tree
        (p15-s23-sh07-b51-single-lf-line
         (p15-s23-sh07-b51-git-bytes
          ["rev-parse" "--verify" (str commit "^{tree}")])
         [phase ordinal :tree])]
    (when-not (and (= commit actual-commit) (= tree actual-tree))
      (p15-s23-sh07-b51-governed-read-fail!
       :content-coordinate
       {:phase phase :ordinal ordinal
        :expected {:commit commit :tree tree}
        :actual {:commit actual-commit :tree actual-tree}}))))

(defn- p15-s23-sh07-b51-ls-tree-row
  [commit path]
  (let [bytes (p15-s23-sh07-b51-git-bytes
               ["ls-tree" "-z" commit "--" path])
        text (p15-s23-sh07-b51-strict-utf8 bytes [:ls-tree path])]
    (when-not (and (pos? (count text))
                   (= \u0000 (last text))
                   (= 1 (count (filter #(= \u0000 %) text))))
      (p15-s23-sh07-b51-governed-read-fail!
       :ls-tree-record {:path path}))
    (let [record (subs text 0 (dec (count text)))
          tab (.indexOf ^String record "\t")
          header (when (pos? tab) (subs record 0 tab))
          observed-path (when (pos? tab) (subs record (inc tab)))
          fields (when header (str/split header #" " -1))]
      (when-not (and (= 3 (count fields))
                     (= "100644" (nth fields 0 nil))
                     (= "blob" (nth fields 1 nil))
                     (boolean (re-matches #"[0-9a-f]{40}"
                                          (nth fields 2 "")))
                     (= path observed-path))
        (p15-s23-sh07-b51-governed-read-fail!
         :ls-tree-projection {:path path :record record}))
      {:mode (nth fields 0) :type (nth fields 1)
       :blob-oid (nth fields 2)})))

(defn- p15-s23-sh07-b51-acquire-object-proofs
  [commit tree path]
  (let [commit-bytes
        (p15-s23-sh07-b51-git-bytes ["cat-file" "commit" commit])
        content {:commit commit :tree tree}
        content-proof
        {:oid commit
         :raw-bytes
         (p15-s23-sh07-b51-unsigned-byte-vector commit-bytes)}
        _ (p15-s23-sh07-b51-validate-content-object-proof!
           content content-proof)
        segments (p15-s23-sh07-b51-safe-proof-path-segments path)
        {:keys [tree-chain leaf-oid leaf-bytes]}
        (loop [index 0 tree-oid tree result []]
          (let [component (nth segments index)
                tree-bytes
                (p15-s23-sh07-b51-git-bytes ["cat-file" "tree" tree-oid])
                entries
                (p15-s23-sh07-b51-parse-tree-object!
                 tree-bytes [path index :acquisition])
                matches (filterv #(= component (:name %)) entries)
                selected (first matches)
                final? (= index (dec (count segments)))]
            (when-not
             (and (= tree-oid
                     (p15-s23-sh07-b51-git-object-id "tree" tree-bytes))
                  (= 1 (count matches))
                  (= (if final? "100644" "40000") (:mode selected)))
              (p15-s23-sh07-b51-governed-read-fail!
               :source-object-proof-acquisition
               {:path path :index index :component component}))
            (let [row {:tree-oid tree-oid
                       :tree-bytes
                       (p15-s23-sh07-b51-unsigned-byte-vector tree-bytes)
                       :component component
                       :selected-mode (:mode selected)
                       :selected-oid (:oid selected)}
                  next-result (conj result row)]
              (if final?
                (let [blob-bytes
                      (p15-s23-sh07-b51-git-bytes
                       ["cat-file" "blob" (:oid selected)])]
                  {:tree-chain next-result
                   :leaf-oid (:oid selected)
                   :leaf-bytes blob-bytes})
                (recur (inc index) (:oid selected) next-result)))))
        source-proof
        {:schema :gravity/sh07-b51-git-tree-inclusion-proof-v1
         :path path
         :root-tree tree
         :tree-chain tree-chain
         :blob-bytes
         (p15-s23-sh07-b51-unsigned-byte-vector leaf-bytes)}]
    {:content-object-proof content-proof
     :source-object-proof source-proof
     :leaf-oid leaf-oid
     :leaf-bytes leaf-bytes}))

(defn- p15-s23-sh07-b51-acquire-source-entry
  [commit tree spec]
  (let [{:keys [ordinal path]} spec]
    (let [{:keys [content-object-proof source-object-proof
                  leaf-oid leaf-bytes]}
          (p15-s23-sh07-b51-acquire-object-proofs commit tree path)]
    (p15-s23-sh07-b51-assert-content-coordinate!
     commit tree :pre-acquisition ordinal)
    (let [tree-row (p15-s23-sh07-b51-ls-tree-row commit path)
          bytes (p15-s23-sh07-b51-git-bytes
                 ["cat-file" "blob" (:blob-oid tree-row)])
          text (p15-s23-sh07-b51-strict-utf8 bytes [:source ordinal path])
          descriptor
          {:ordinal ordinal
           :kind (:kind spec)
           :module-ordinal (:module-ordinal spec)
           :path path
           :mode (:mode tree-row)
           :blob-oid (:blob-oid tree-row)
           :byte-count (alength bytes)
           :raw-sha256 (p15-s23-sh07-b51-raw-sha256 bytes)}]
      (when-not (and (= (:blob-oid tree-row) leaf-oid)
                     (p15-s23-sh07-b51-byte-array-equal?
                      bytes leaf-bytes))
        (p15-s23-sh07-b51-governed-read-fail!
         :object-and-ls-tree-route-mismatch {:path path :ordinal ordinal}))
      (p15-s23-sh07-b51-validate-source-object-proof!
       {:commit commit :tree tree} descriptor source-object-proof)
      (p15-s23-sh07-b51-assert-content-coordinate!
       commit tree :post-acquisition ordinal)
      {:descriptor descriptor :bytes bytes :text text
       :content-object-proof content-object-proof
       :source-object-proof source-object-proof}))))

(defn- p15-s23-sh07-b51-process-role-fail!
  [reason details]
  (throw (ex-info "SH-07 B51 process role rejected"
                  {:id "SH07-B51-TRUSTED-TCB"
                   :reason reason
                   :details details})))

(defn- p15-s23-sh07-b51-process-role-for-observation
  [{:keys [arguments loaded-project-namespaces]}]
  (let [arguments (when (vector? arguments) arguments)
        loaded (set loaded-project-namespaces)
        checker-child
        'gravity.tools.sh07-b51-attempt32-checker-child
        coordinator
        'gravity.tools.sh07-b51-attempt32-coordinator-child
        producer
        'gravity.tools.sh07-b51-attempt30-producer-census
        independent
        'gravity.tools.sh07-b51-attempt30-independent-census
        bootstrap 'gravity.bootstrap
        checker-tail (when (and arguments (<= 5 (count arguments)))
                       (subvec arguments (- (count arguments) 5)))
        coordinator-tail-count
        (+ 3 (* 2 (count p15-s23-sh07-b51-coordinator-option-names)))
        coordinator-tail
        (when (and arguments
                   (<= coordinator-tail-count (count arguments)))
          (subvec arguments (- (count arguments) coordinator-tail-count)))
        coordinator-option-names
        (when coordinator-tail
          (mapv first (partition 2 (subvec coordinator-tail 3))))
        producer-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "producer-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child producer])
             (not-any? loaded [coordinator independent]))
        independent-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-checker-entrypoint
                 "--role" "independent-verifier-census"]
                checker-tail)
             (every? loaded [bootstrap checker-child independent])
             (not-any? loaded [coordinator producer]))
        coordinator-process?
        (and (= ["clojure.main" "-m" p15-s23-sh07-b51-coordinator-entrypoint]
                (when coordinator-tail (subvec coordinator-tail 0 3)))
             (= p15-s23-sh07-b51-coordinator-option-names
                coordinator-option-names)
             (every? loaded [bootstrap coordinator])
             (not-any? loaded [checker-child producer independent]))]
    (cond
      producer-process? :producer-census
      independent-process? :independent-verifier-census
      coordinator-process? :m
      :else
      (p15-s23-sh07-b51-process-role-fail!
       :unauthenticated-process-role
       {:argument-count (count arguments)
        :loaded-project-namespaces (vec (sort-by str loaded))}))))

(defn- p15-s23-sh07-b51-process-role
  []
  (let [info (.info (java.lang.ProcessHandle/current))
        argument-value (.arguments info)
        arguments (when (.isPresent argument-value)
                    (vec (.get argument-value)))
        loaded-project-namespaces
        (->> (loaded-libs)
             (filter #(str/starts-with? (str %) "gravity."))
             set)]
    (p15-s23-sh07-b51-process-role-for-observation
     {:arguments arguments
      :loaded-project-namespaces loaded-project-namespaces})))

(defn- p15-s23-sh07-b51-open-source-session
  [commit tree]
  (when-not (and (string? commit)
                 (boolean (re-matches #"[0-9a-f]{40}" commit))
                 (string? tree)
                 (boolean (re-matches #"[0-9a-f]{40}" tree)))
    (p15-s23-sh07-b51-governed-read-fail!
     :content-coordinate-shape {:commit commit :tree tree}))
  (let [entries (mapv #(p15-s23-sh07-b51-acquire-source-entry
                        commit tree %)
                      p15-s23-sh07-b51-source-specs)
        descriptors (mapv :descriptor entries)
        token (Object.)
        state {:token token
               :thread (Thread/currentThread)
               :role (p15-s23-sh07-b51-process-role)
               :commit commit :tree tree
               :entries entries
               :descriptors descriptors
               :descriptors-root
               (reader-canonical-hash
                [:gravity/sh07-b51-source-descriptors-root-v1 descriptors])
               :cursor (atom 0)
               :events (atom [])
               :checked-products (atom {})
               :finalized (atom false)}]
    {:token token :state state}))

(defn- p15-s23-sh07-b51-active-source-session
  []
  (let [{:keys [token state] :as binding-value}
        *p15-s23-sh07-b51-governed-source-session*]
    (when-not (and (= #{:token :state} (set (keys binding-value)))
                   (map? state)
                   (identical? token (:token state))
                   (identical? (Thread/currentThread) (:thread state))
                   (false? @(:finalized state)))
      (p15-s23-sh07-b51-governed-read-fail!
       :inactive-or-forged-session {}))
    state))

(defn- p15-s23-sh07-b51-governed-source-entry
  [source-ordinal]
  (let [state (p15-s23-sh07-b51-active-source-session)]
    (when-not (and (integer? source-ordinal)
                   (<= 0 source-ordinal)
                   (< source-ordinal (count (:entries state))))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-ordinal {:source-ordinal source-ordinal}))
    (nth (:entries state) source-ordinal)))

(defn- p15-s23-sh07-b51-source-event-product-id
  [event product]
  (reader-canonical-hash
   [:gravity/sh07-b51-source-event-product-id-v1
    {:ordinal (:ordinal event)
     :domain (:domain event)
     :consumer (:consumer event)
     :source-raw-sha256 (:source-raw-sha256 event)
     :product product}]))

(defn- p15-s23-sh07-b51-governed-source-event!
  [domain source-ordinal module-ordinal consumer product-fn]
  (let [state (p15-s23-sh07-b51-active-source-session)
        ordinal @(:cursor state)
        expected (nth p15-s23-sh07-b51-source-event-plan ordinal nil)
        coordinate {:ordinal ordinal :domain domain
                    :source-ordinal source-ordinal
                    :module-ordinal module-ordinal
                    :consumer consumer}]
    (when-not (= expected coordinate)
      (p15-s23-sh07-b51-governed-read-fail!
       :event-order {:expected expected :actual coordinate}))
    (let [entry (nth (:entries state) source-ordinal)
          product (product-fn entry)
          raw-sha (get-in entry [:descriptor :raw-sha256])
          event-base (assoc coordinate :source-raw-sha256 raw-sha)
          product-id
          (p15-s23-sh07-b51-source-event-product-id event-base product)
          event (assoc event-base :product-id product-id)]
      (swap! (:events state) conj event)
      (swap! (:cursor state) inc)
      product)))

(defn- p15-s23-sh07-b51-session-checked-product
  [key]
  (get @(:checked-products (p15-s23-sh07-b51-active-source-session)) key))

(defn- p15-s23-sh07-b51-cache-session-product!
  [key value]
  (let [cache (:checked-products (p15-s23-sh07-b51-active-source-session))]
    (when (contains? @cache key)
      (p15-s23-sh07-b51-governed-read-fail!
       :duplicate-checked-product {:key key}))
    (swap! cache assoc key value)
    value))

(defn- p15-s23-sh07-b51-finalize-source-session!
  []
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptors (:descriptors state)
        events @(:events state)
        counters {:acquire-count (count (:entries state))
                  :decode-count (count (:entries state))
                  :event-count (count events)
                  :finalize-count 1}]
    (when-not (and (= 13 (count descriptors))
                   (= 40 @(:cursor state) (count events))
                   (= (vec (range 40)) (mapv :ordinal events))
                   (= {:acquire-count 13 :decode-count 13
                       :event-count 40 :finalize-count 1}
                      counters)
                   (compare-and-set! (:finalized state) false true))
      (p15-s23-sh07-b51-governed-read-fail!
       :session-finalize
       {:descriptor-count (count descriptors)
        :event-count (count events)
        :cursor @(:cursor state)}))
    {:schema :gravity/sh07-b51-source-read-receipt-v3
     :role (:role state)
     :commit (:commit state)
     :tree (:tree state)
     :source-descriptors descriptors
     :source-descriptors-root (:descriptors-root state)
     :events events
     :event-root
     (reader-canonical-hash
      [:gravity/sh07-b51-source-event-root-v1 events])
     :counters counters
     :finalized true}))

(defn- p15-s23-sh07-b51-active-module-ordinal
  [source-path]
  (some (fn [{:keys [module-ordinal logical-source-path]}]
          (when (= logical-source-path source-path) module-ordinal))
        p15-s23-sh07-b51-module-specs))

(defn- p15-s23-sh07-b51-compatibility-source-record
  [source-path]
  (when *p15-s23-sh07-b51-v9-receipted-bundle-construction*
    (p15-s23-sh07-b51-governed-read-fail!
     :legacy-live-source-read-in-v9
     {:source-path source-path}))
  (let [direct (when (string? source-path) (java.io.File. source-path))
        file (if (and direct (.isFile direct))
               (.getCanonicalFile direct)
               (p15-s23-sh07-governed-source-file source-path))]
    (when-not (and file (.isFile ^java.io.File file))
      (p15-s23-sh07-b51-governed-read-fail!
       :compatibility-source-missing {:source-path source-path}))
    (let [bytes (with-open [stream (java.io.FileInputStream. ^java.io.File file)]
                  (p15-s23-sh07-b51-read-stream-bytes
                   stream 268435456 :compatibility-source))]
      {:path (.getCanonicalPath ^java.io.File file)
       :bytes bytes
       :text (p15-s23-sh07-b51-strict-utf8 bytes source-path)})))

;; Historical public name retained only so older host code fails closed rather
;; than resolving a removed Var.  It is not an accepted M3-v1 pin.
(def p15-s23-sh07-b51-expected-execution-bundle-id
  p15-s23-sh07-b51-expected-m3-id)

(def p15-s23-sh07-b51-host-pin-boundary-id
  "sha256:ecae8acf77cf8c64588aef4cad380c0a7873baab1b282398b03f532ae94b0a6e")

(def p15-s23-sh07-b51-runtime-token-boundary-id
  "sha256:844b3ea1b9bd75f416bfb51ff2c40e9a8aed1803813d6a11b3e54e44e75809ad")

(def ^:private p15-s23-sh07-b51-admitted-runtime-contexts
  (java.util.HashMap.))
(def ^:private p15-s23-sh07-b51-admitted-runtime-context-queue
  (java.lang.ref.ReferenceQueue.))
(def ^:private p15-s23-sh07-b51-registry-prune-maximum 16)
(def ^:private p15-s23-sh07-b51-maximum-external-calls 65536)

(def ^:private p15-s23-sh07-b51-runtime-authority-keys
  #{:sh07-b51-runtime-context-token
    :sh07-b51-runtime-token-boundary-id
    :sh07-b51-linked-carrier-v4
    :sh07-b51-linked-carrier-v3
    :sh07-b51-module-source-descriptors
    :sh07-b51-link-manifest
    :sh07-b51-module-plans
    :sh07-b51-execution-bundle
    :sh07-b51-external-call-trace})

(deftype ^:private P15S23Sh07B51IdentityTokenQuery
  [token ^int identity-hash]
  Object
  (hashCode [_] identity-hash)
  (equals [_ other]
    (and (instance? java.lang.ref.WeakReference other)
         (identical? token
                     (.get ^java.lang.ref.WeakReference other)))))

(alter-meta! #'->P15S23Sh07B51IdentityTokenQuery assoc :private true)

(defn- p15-s23-sh07-b51-weak-identity-token-key [token]
  (let [identity-hash (System/identityHashCode token)]
    (proxy [java.lang.ref.WeakReference]
      [token p15-s23-sh07-b51-admitted-runtime-context-queue]
      (hashCode [] identity-hash)
      (equals [other]
        (let [registered-token (proxy-super get)]
          (and registered-token
               (instance? java.lang.ref.WeakReference other)
               (identical?
                registered-token
                (.get ^java.lang.ref.WeakReference other))))))))

(defn- p15-s23-sh07-b51-token-query [token]
  (P15S23Sh07B51IdentityTokenQuery.
   token (System/identityHashCode token)))

(defn- p15-s23-sh07-b51-prune-admitted-runtime-contexts! []
  (loop [remaining p15-s23-sh07-b51-registry-prune-maximum]
    (when (pos? remaining)
      (when-let [key (.poll ^java.lang.ref.ReferenceQueue
                            p15-s23-sh07-b51-admitted-runtime-context-queue)]
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts key)
        (recur (dec remaining))))))

(defn- p15-s23-sh07-b51-register-runtime-context! [context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [token (.get ^java.lang.ref.WeakReference (:token-ref context))]
      (.put ^java.util.HashMap p15-s23-sh07-b51-admitted-runtime-contexts
            (p15-s23-sh07-b51-weak-identity-token-key token)
            (java.lang.ref.WeakReference. context))))
  context)

(defn- p15-s23-sh07-b51-runtime-context-by-token [token]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (p15-s23-sh07-b51-prune-admitted-runtime-contexts!)
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          context (when context-ref
                    (.get ^java.lang.ref.WeakReference context-ref))]
      (when (and context-ref (nil? context))
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query))
      context)))

(defn- p15-s23-sh07-b51-remove-runtime-context!
  [token context]
  (locking p15-s23-sh07-b51-admitted-runtime-contexts
    (let [query (p15-s23-sh07-b51-token-query token)
          context-ref
          (.get ^java.util.HashMap
                p15-s23-sh07-b51-admitted-runtime-contexts query)
          registered-context
          (when context-ref (.get ^java.lang.ref.WeakReference context-ref))]
      (when (identical? context registered-context)
        (.remove ^java.util.HashMap
                 p15-s23-sh07-b51-admitted-runtime-contexts query)
        true))))

(defn- p15-s23-sh07-b51-unadmitted-runtime-context [runtime]
  (assoc (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         :sh07-b51-external-call-trace (atom [])))

(defn- p15-s23-sh07-b51-register-admitted-runtime-context
  [linked-products carrier raw route args runtime]
  (let [token (Object.)
        trace (atom [])
        top-level-invocation (Object.)
        top-level-owner
        (java.util.concurrent.atomic.AtomicReference. {:state :entering})
        receipt-slot
        (java.util.concurrent.atomic.AtomicReference. {:state :available})
        admitted-runtime
        (merge
         (apply dissoc (if (map? runtime) runtime {})
                p15-s23-sh07-b51-runtime-authority-keys)
         {:sh07-b51-runtime-context-token token
          :sh07-b51-runtime-token-boundary-id
          p15-s23-sh07-b51-runtime-token-boundary-id
          :sh07-b51-linked-carrier-v4 carrier
          ;; The Stage2 V2 builtin bridge still reads this historical key.
          ;; It holds the same carrier-v4 object; no carrier-v3 is admitted.
          :sh07-b51-linked-carrier-v3 carrier
          :sh07-b51-module-source-descriptors
          (:module-source-descriptors linked-products)
          :sh07-b51-link-manifest (:link-manifest linked-products)
          :sh07-b51-module-plans (:module-plans linked-products)
         :sh07-b51-execution-bundle (:execution-bundle linked-products)
         :sh07-b51-external-call-trace trace})
        context
        {:token-ref (java.lang.ref.WeakReference. token)
         :runtime-ref (java.lang.ref.WeakReference. admitted-runtime)
         :carrier carrier
         :module-source-descriptors
         (:module-source-descriptors linked-products)
         :link-manifest (:link-manifest linked-products)
         :module-plans (:module-plans linked-products)
         :execution-bundle (:execution-bundle linked-products)
         :external-call-trace trace
         :authority-ref {:carrier carrier :products linked-products}
         :raw-ref raw
         :selected-root-ref (:root route)
         :selected-plan-ref (:plan route)
         :arguments-ref args
         :top-level-invocation-ref top-level-invocation
         :top-level-owner-ref top-level-owner
         :receipt-slot-ref receipt-slot
         :thread-ref (Thread/currentThread)}]
    (when (contains? #{1 6 8} (get-in route [:root :root-ordinal]))
      (when-not (and (vector? args)
                     (pos? (count args))
                     (identical? raw (nth args 0))
                     (identical? carrier
                                 (:v18-executable-descriptor raw)))
        (throw (ex-info "selected SH-07 root arguments are not admission-bound"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-invocation-identity}))))
    (try
      (p15-s23-sh07-b51-issue-top-level-validation-receipt! context)
      (p15-s23-sh07-b51-register-runtime-context! context)
      {:runtime admitted-runtime :context-lease context}
      (catch Throwable failure
        (p15-s23-sh07-b51-retire-admitted-runtime-context! context)
        (p15-s23-sh07-b51-remove-runtime-context! token context)
        (throw failure)))))

(defn- p15-s23-sh07-b51-admitted-runtime-context [runtime]
  (when (map? runtime)
    (let [token (:sh07-b51-runtime-context-token runtime)
          context
          (when token
            (p15-s23-sh07-b51-runtime-context-by-token token))]
      (when
       (and context
            (identical? token
                        (.get ^java.lang.ref.WeakReference
                              (:token-ref context)))
            (identical? runtime
                        (.get ^java.lang.ref.WeakReference
                              (:runtime-ref context)))
            (identical? (Thread/currentThread) (:thread-ref context))
            (= p15-s23-sh07-b51-runtime-token-boundary-id
               (:sh07-b51-runtime-token-boundary-id runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v4 runtime))
            (identical? (:carrier context)
                        (:sh07-b51-linked-carrier-v3 runtime))
            (identical? (:module-source-descriptors context)
                        (:sh07-b51-module-source-descriptors runtime))
            (identical? (:link-manifest context)
                        (:sh07-b51-link-manifest runtime))
            (identical? (:module-plans context)
                        (:sh07-b51-module-plans runtime))
            (identical? (:execution-bundle context)
                        (:sh07-b51-execution-bundle runtime))
            (identical? (:external-call-trace context)
                        (:sh07-b51-external-call-trace runtime)))
        context))))

(defn p15-s23-sh07-b51-runtime-admitted-products-match?
  [runtime carrier descriptors manifest plans bundle]
  (boolean
   (when-let [context
              (p15-s23-sh07-b51-admitted-runtime-context runtime)]
     (and (identical? carrier (:carrier context))
          (identical? descriptors (:module-source-descriptors context))
          (identical? manifest (:link-manifest context))
          (identical? plans (:module-plans context))
          (identical? bundle (:execution-bundle context))))))

(def ^:private p15-s23-sh07-b51-effective-builtins
  '#{* + - / < <= = > >= assoc conj count first get hash-map list pr-str
     rest second str vector symbol? keyword? char? number? list? seq? vector?
     map? set? string? contains? even? set sort-by-pr-str vec quot subvec
     integer? boolean? keys sh07-canonical-text-compare
     sh07-declared-digest-hash sh07-observe-utf8-byte-count
     sh07-b51-admitted-executable-carrier-validation?})

(defn p15-s23-sh07-b51-exact-keys? [value expected-keys]
  (and (map? value) (= expected-keys (set (keys value)))))

(defn p15-s23-sh07-b51-digest-id? [value]
  (and (string? value)
       (boolean (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn p15-s23-sh07-b51-canonical-id [value]
  (reader-canonical-hash value))

(defn p15-s23-sh07-b51-owner-edge-kind
  [caller-owner callee-owner]
  (cond
    (= caller-owner callee-owner)
    (case caller-owner
      :r1-producer :producer-internal
      :r6-independent-verifier :root6-internal
      nil)

    (and (= :r8-finalizer caller-owner)
         (= :r1-producer callee-owner))
    :root8-to-producer-interface

    :else nil))

(defn p15-s23-sh07-b51-binding-census [source-path module module-ordinal]
  (let [syntax-stream
        (mapv (fn [ordinal form]
                {:form form
                 :syntax/id
                 (p15-s23-sh07-b51-canonical-id
                  {:domain :gravity/sh07-b51-attempt24-syntax-v1
                   :module-ordinal module-ordinal :form-ordinal ordinal})
                 :span {:source source-path :form-index ordinal}})
              (range) (:forms module))
        sh05
        {:artifact-id
         (p15-s23-sh07-b51-canonical-id
          {:domain :gravity/sh07-b51-attempt24-sh05-census-v1
           :module-ordinal module-ordinal})
         :expanded-syntax-stream syntax-stream}
        definitions (sh06-resolution-definition-records module sh05)
        core (sh06-resolution-core-records)
        analysis (sh06-resolution-analysis-inputs module sh05 {})
        lexical (vec (mapcat :bindings (:lexical-scopes analysis)))]
    {:definition-count (count definitions)
     :core-binding-count (count core)
     :lexical-binding-count (count lexical)}))

(defn p15-s23-sh07-b51-builtin-call-census [function-table]
  (vec
   (mapcat
    (fn [caller]
      (let [definition (get function-table caller)
            caller-hash (p15-s23-sh07-source-definition-hash definition)]
        (mapcat
         (fn [body-form]
           (letfn [(walk [value]
                     (cond
                       (seq? value)
                       (if (= 'quote (first value))
                         []
                         (let [callee (first value)
                               row
                               (when (contains? p15-s23-sh07-b51-effective-builtins
                                                callee)
                                 [{:caller caller
                                   :caller-function-source-hash caller-hash
                                   :builtin callee
                                   :arity (count (rest value))
                                   :compiler-internal?
                                   (contains?
                                    p15-s23-sh07-declared-builtin-exact-arities
                                    callee)}])]
                           (into (or row []) (mapcat walk value))))
                       (map? value)
                       (mapcat walk
                               (mapcat identity
                                       (sort-by #(pr-str
                                                  (reader-canonical-value %))
                                                value)))
                       (coll? value) (mapcat walk value)
                       :else []))]
             (walk body-form)))
         (:body definition))))
    (sort-by str (keys function-table)))))

(defn p15-s23-sh07-b51-module-spec [ordinal]
  (when (and (integer? ordinal) (<= 0 ordinal 6))
    (nth p15-s23-sh07-b51-module-specs ordinal)))

(defn p15-s23-sh07-b51-definition-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-definition-keys)
       (symbol? (:name row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:public :private} (:visibility row))
       (p15-s23-sh07-b51-digest-id? (:source-hash row))))

(defn p15-s23-sh07-b51-external-binding-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-external-binding-keys)
       (symbol? (:symbol row))
       (integer? (:callee-module-ordinal row))
       (<= 0 (:callee-module-ordinal row) 6)
       (symbol? (:callee-module row))
       (p15-s23-sh07-b51-digest-id? (:callee-source-sha256 row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-definition-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))))

(defn p15-s23-sh07-b51-builtin-call-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys?
        row p15-s23-sh07-b51-builtin-call-keys)
       (symbol? (:caller row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (contains? p15-s23-sh07-b51-effective-builtins (:builtin row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (= (contains? p15-s23-sh07-declared-builtin-exact-arities
                     (:builtin row))
          (:compiler-internal? row))
       (or (not (:compiler-internal? row))
           (= (get p15-s23-sh07-declared-builtin-exact-arities
                   (:builtin row))
              (:arity row)))))

(defn p15-s23-sh07-b51-module-source-id [descriptor]
  (p15-s23-sh07-b51-canonical-id
   (dissoc descriptor :module-source-id)))

(defn- p15-s23-sh07-b51-module-input-record
  [spec {:keys [descriptor bytes text]}]
  (let [path (:logical-source-path spec)
        module (parse-module path (read-forms path text))
        table (stage0-function-table module)]
    (when-not
     (and (= path (:path descriptor))
          (= (:module spec) (:module module))
          (= :meta (:profile module))
          (= :jvm (:target module))
          (= #{} (:effects module))
          (= #{} (:capabilities module))
          (= :safe (:safety module)))
      (p15-s23-sh07-b51-governed-read-fail!
       :module-source-contract
       {:module-ordinal (:module-ordinal spec) :path path}))
    {:spec spec :path path :text text :bytes bytes
     :source-descriptor descriptor :module module :table table
     :source-sha256 (p15-s23-sh07-b51-raw-sha256 bytes)}))

(defn- p15-s23-sh07-b51-validate-module-input!
  [module-ordinal]
  (let [spec (p15-s23-sh07-b51-module-spec module-ordinal)
        validated?
        (p15-s23-sh07-b51-governed-source-event!
         :module-validation (+ 3 module-ordinal) module-ordinal
         'gravity.bootstrap/p15-s23-sh07-b51-module-source-valid*
         (fn [entry]
           (p15-s23-sh07-b51-cache-session-product!
            [:module-input module-ordinal]
            (p15-s23-sh07-b51-module-input-record spec entry))
           true))]
    (when-not (true? validated?)
      (p15-s23-sh07-b51-governed-read-fail!
       :module-validation {:module-ordinal module-ordinal}))
    (p15-s23-sh07-b51-session-checked-product
     [:module-input module-ordinal])))

(defn- p15-s23-sh07-b51-compatibility-module-input
  [spec]
  (let [{:keys [path bytes text]}
        (p15-s23-sh07-b51-compatibility-source-record
         (:logical-source-path spec))
        descriptor {:path (:logical-source-path spec)}]
    (assoc (p15-s23-sh07-b51-module-input-record
            spec {:descriptor descriptor :bytes bytes :text text})
           :compatibility-path path)))

(defn- p15-s23-sh07-b51-module-source-valid-with-input?
  [descriptor source]
  (let [spec (:spec source)
        definitions (:definitions descriptor)
        external-bindings (:external-bindings descriptor)
        builtin-calls (:builtin-calls descriptor)
        lexical-count (:lexical-binding-count descriptor)
        aggregate (when (and (vector? definitions)
                             (integer? (:core-binding-count descriptor))
                             (integer? lexical-count)
                             (vector? external-bindings))
                    (+ (:core-binding-count descriptor)
                       (count definitions) lexical-count
                       (count external-bindings)))
        source-bytes (:bytes source)
        source-module (:module source)
        source-table (:table source)
        source-path (:path source)
        expected-binding-census
        (when source-module
          (p15-s23-sh07-b51-binding-census
           source-path source-module
           (:module-ordinal spec)))
        expected-builtin-calls
        (when source-table
          (p15-s23-sh07-b51-builtin-call-census source-table))
        expected-definitions
        (when source-table
          (mapv (fn [form]
                  (let [definition
                        (function-definition-form source-path form)
                        definition (assoc definition :arity
                                          (count (:params definition)))]
                    {:name (:name definition) :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)}))
                (filter #(function-definition-form source-path %)
                        (:forms source-module))))]
    (and (p15-s23-sh07-b51-exact-keys? descriptor
                                              p15-s23-sh07-b51-m0-keys)
         (= :gravity/sh07-b51-module-source-v1 (:artifact descriptor))
         (= 1 (:schema-version descriptor))
         (= (select-keys spec [:module-ordinal :module :logical-source-path
                               :owner :exports])
            (select-keys descriptor
                         [:module-ordinal :module :logical-source-path
                          :owner :exports]))
         (integer? (:source-byte-count descriptor))
         (<= 0 (:source-byte-count descriptor))
         (p15-s23-sh07-b51-digest-id? (:source-sha256 descriptor))
         (= (:source-byte-count descriptor) (alength source-bytes))
         (= (:source-sha256 descriptor)
            (str "sha256:" (sha256-bytes-hex source-bytes)))
         (= :meta (:profile descriptor))
         (= :jvm (:target descriptor))
         (= #{} (:effects descriptor))
         (= #{} (:capabilities descriptor))
         (= :safe (:safety descriptor))
         (vector? definitions)
         (every? p15-s23-sh07-b51-definition-row-valid? definitions)
         (= expected-definitions definitions)
         (= (count definitions) (count (set (map :name definitions))))
         (= (:exports descriptor)
            (vec (filter (set (:exports descriptor))
                         (map :name definitions))))
         (every? #(= (contains? (set (:exports descriptor)) (:name %))
                      (= :public (:visibility %)))
                 definitions)
         (= (:core-binding-count expected-binding-census)
            (:core-binding-count descriptor))
         (integer? (:lexical-binding-count descriptor))
         (<= 0 (:lexical-binding-count descriptor))
         (= (:lexical-binding-count expected-binding-census)
            (:lexical-binding-count descriptor))
         (= (:definition-count expected-binding-census)
            (count definitions))
         (vector? external-bindings)
         (every? p15-s23-sh07-b51-external-binding-row-valid?
                 external-bindings)
         (= (count external-bindings)
            (count (set (map :symbol external-bindings))))
         (vector? builtin-calls)
         (every? p15-s23-sh07-b51-builtin-call-row-valid? builtin-calls)
         (= expected-builtin-calls builtin-calls)
         (= aggregate (:aggregate-binding-count descriptor))
         (<= aggregate 2440)
         (= (:module-source-id descriptor)
            (p15-s23-sh07-b51-module-source-id descriptor)))))

(defn- p15-s23-sh07-b51-module-source-valid* [descriptor]
  (when-let [spec (p15-s23-sh07-b51-module-spec
                   (:module-ordinal descriptor))]
    (let [source
          (if *p15-s23-sh07-b51-governed-source-session*
            (p15-s23-sh07-b51-session-checked-product
             [:module-input (:module-ordinal descriptor)])
            (p15-s23-sh07-b51-compatibility-module-input spec))]
      (and source
           (p15-s23-sh07-b51-module-source-valid-with-input?
            descriptor source)))))

(defn p15-s23-sh07-b51-module-source-valid? [descriptor]
  (try
    (boolean (p15-s23-sh07-b51-module-source-valid* descriptor))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-module-source [descriptor]
  (let [sealed (assoc descriptor :module-source-id
                      (p15-s23-sh07-b51-module-source-id descriptor))]
    (when-not (p15-s23-sh07-b51-module-source-valid? sealed)
      (throw (ex-info "invalid SH-07 B51 M0 descriptor"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    sealed))

(defn p15-s23-sh07-b51-build-module-source
  [source-path source-text module binding-census external-bindings
   builtin-calls]
  (let [spec (first (filter #(= (:module module) (:module %))
                            p15-s23-sh07-b51-module-specs))
        source-bytes (.getBytes ^String source-text
                                java.nio.charset.StandardCharsets/UTF_8)
        function-table (stage0-function-table module)
        definitions
        (mapv (fn [form]
                (when-let [definition (function-definition-form source-path form)]
                  (let [definition (assoc definition
                                          :arity (count (:params definition)))]
                    {:name (:name definition)
                     :arity (:arity definition)
                     :visibility
                     (if (contains? (set (:exports spec)) (:name definition))
                       :public :private)
                     :source-hash
                     (p15-s23-sh07-source-definition-hash definition)})))
              (filter #(function-definition-form source-path %)
                      (:forms module)))
        base
        {:artifact :gravity/sh07-b51-module-source-v1
         :schema-version 1
         :module-source-id nil
         :module-ordinal (:module-ordinal spec)
         :module (:module spec)
         :logical-source-path (:logical-source-path spec)
         :owner (:owner spec)
         :source-byte-count (alength source-bytes)
         :source-sha256 (str "sha256:" (sha256-bytes-hex source-bytes))
         :profile (:profile module)
         :target (:target module)
         :effects (:effects module)
         :capabilities (:capabilities module)
         :safety (:safety module)
         :definitions definitions
         :exports (:exports spec)
         :core-binding-count (:core-binding-count binding-census)
         :lexical-binding-count (:lexical-binding-count binding-census)
         :external-bindings external-bindings
         :aggregate-binding-count
         (+ (:core-binding-count binding-census)
            (:definition-count binding-census)
            (:lexical-binding-count binding-census)
            (count external-bindings))
         :builtin-calls builtin-calls}]
    (when-not (and spec
                   (= source-path (:logical-source-path spec))
                   (= (set (keys function-table))
                      (set (map :name definitions)))
                   (= (count definitions)
                      (:definition-count binding-census)))
      (throw (ex-info "invalid SH-07 B51 module source input"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (p15-s23-sh07-b51-seal-module-source base)))

(defn p15-s23-sh07-b51-edge-row-valid? [row]
  (and (p15-s23-sh07-b51-exact-keys? row p15-s23-sh07-b51-edge-keys)
       (integer? (:ordinal row)) (<= 0 (:ordinal row))
       (p15-s23-sh07-b51-digest-id? (:caller-module-source-id row))
       (symbol? (:caller-function row))
       (p15-s23-sh07-b51-digest-id?
        (:caller-function-source-hash row))
       (vector? (:callsite-path row))
       (every? #(and (integer? %) (<= 0 %)) (:callsite-path row))
       (p15-s23-sh07-b51-digest-id? (:callee-module-source-id row))
       (symbol? (:callee-function row))
       (p15-s23-sh07-b51-digest-id?
        (:callee-function-source-hash row))
       (integer? (:arity row)) (<= 0 (:arity row))
       (contains? #{:producer-internal :root6-internal
                    :root8-to-producer-interface}
                  (:owner-edge row))
       (= :authenticated-b51-namespace-external (:authorization row))))

(defn p15-s23-sh07-b51-manifest-id [manifest]
  (p15-s23-sh07-b51-canonical-id (dissoc manifest :manifest-id)))

(defn- p15-s23-sh07-b51-definition-by-name [descriptor function]
  (first (filter #(= function (:name %)) (:definitions descriptor))))

(defn- p15-s23-sh07-b51-link-manifest-valid*
  [descriptors manifest]
  (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
        ordinal-by-id (into {} (map (juxt :module-source-id :module-ordinal))
                            descriptors)
        edges (:edges manifest)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        root8 (when (and (vector? descriptors) (= 7 (count descriptors)))
                (nth descriptors 6))
        root8-edges
        (filter #(= (:module-source-id root8)
                    (:caller-module-source-id %)) edges)
        root8-root1-edges
        (filter #(and (= 'sh07-b51-finalize-rejection
                         (:caller-function %))
                      (= 'sh07-b51-build-template
                         (:callee-function %))) root8-edges)
        root6-source-ids
        (if (and (vector? descriptors) (= 7 (count descriptors)))
          (set (map :module-source-id (subvec descriptors 4 6)))
          #{})
        root6-cross-edges
        (filter #(and (contains? root6-source-ids
                                 (:caller-module-source-id %))
                      (not (contains? root6-source-ids
                                      (:callee-module-source-id %))))
                edges)
        source-census (p15-s23-sh07-b51-derive-callsite-census)
        projected-census
        (mapv (fn [edge]
                (let [caller (get by-id (:caller-module-source-id edge))
                      callee (get by-id (:callee-module-source-id edge))]
                  {:ordinal (:ordinal edge)
                   :caller-module-ordinal (:module-ordinal caller)
                   :caller-module (:module caller)
                   :caller-function (:caller-function edge)
                   :callsite-path (:callsite-path edge)
                   :callee-module-ordinal (:module-ordinal callee)
                   :callee-module (:module callee)
                   :callee-function (:callee-function edge)
                   :arity (:arity edge)
                   :owner-edge (:owner-edge edge)}))
              edges)]
    (and (= 7 (count descriptors))
         (= (range 7) (map :module-ordinal descriptors))
         (every? p15-s23-sh07-b51-module-source-valid? descriptors)
         (p15-s23-sh07-b51-exact-keys? manifest p15-s23-sh07-b51-m1-keys)
         (= :gravity/sh07-b51-link-manifest-v1 (:artifact manifest))
         (= 1 (:schema-version manifest))
         (= (mapv :module-source-id descriptors)
            (:module-source-ids manifest))
         (vector? edges)
         (= ordered edges)
         (= (vec (range (count edges))) (mapv :ordinal edges))
         (= source-census projected-census)
         (= (count edges)
            (count (set (map (juxt :caller-module-source-id
                                   :caller-function :callsite-path)
                             edges))))
         (= p15-s23-sh07-b51-root8-external-callees
            (mapv :symbol (:external-bindings root8)))
         (= 1 (count root8-root1-edges))
         (empty? root6-cross-edges)
         (every? (fn [descriptor]
                   (= (set (map :symbol (:external-bindings descriptor)))
                      (set (map :callee-function
                                (filter #(= (:module-source-id descriptor)
                                            (:caller-module-source-id %))
                                        edges)))))
                 descriptors)
         (every?
          (fn [row]
            (let [caller (get by-id (:caller-module-source-id row))
                  callee (get by-id (:callee-module-source-id row))
                  caller-definition
                  (p15-s23-sh07-b51-definition-by-name
                   caller (:caller-function row))
                  callee-definition
                  (p15-s23-sh07-b51-definition-by-name
                   callee (:callee-function row))
                  external (first (filter
                                   #(= (:callee-function row) (:symbol %))
                                   (:external-bindings caller)))
                  expected-owner-edge
                  (p15-s23-sh07-b51-owner-edge-kind
                   (:owner caller) (:owner callee))]
              (and (p15-s23-sh07-b51-edge-row-valid? row)
                   caller callee caller-definition callee-definition external
                   (= (:caller-function-source-hash row)
                      (:source-hash caller-definition))
                   (= (:callee-function-source-hash row)
                      (:source-hash callee-definition))
                   (= (:arity row) (:arity callee-definition) (:arity external))
                   (= (:module-ordinal callee)
                      (:callee-module-ordinal external))
                   (= (:module callee) (:callee-module external))
                   (= (:source-sha256 callee)
                      (:callee-source-sha256 external))
                   (= (:source-hash callee-definition)
                      (:callee-definition-source-hash external))
                   (= expected-owner-edge
                      (:owner-edge row) (:owner-edge external)))))
          edges)
         (= (:manifest-id manifest)
            (p15-s23-sh07-b51-manifest-id manifest)))))

(defn p15-s23-sh07-b51-link-manifest-valid? [descriptors manifest]
  (try
    (boolean (p15-s23-sh07-b51-link-manifest-valid* descriptors manifest))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-seal-link-manifest [descriptors edges]
  (let [ordinal-by-id
        (into {} (map (juxt :module-source-id :module-ordinal)) descriptors)
        ordered
        (vec (sort-by (fn [row]
                        [(get ordinal-by-id (:caller-module-source-id row))
                         (str (:caller-function row))
                         (count (:callsite-path row))
                         (:callsite-path row)])
                      edges))
        base {:artifact :gravity/sh07-b51-link-manifest-v1
              :schema-version 1
              :module-source-ids (mapv :module-source-id descriptors)
              :edges (mapv #(assoc %1 :ordinal %2) ordered (range))}
        manifest (assoc base :manifest-id
                        (p15-s23-sh07-b51-manifest-id base))]
    (when-not (p15-s23-sh07-b51-link-manifest-valid?
               descriptors manifest)
      (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
            projected
            (mapv (fn [edge]
                    (let [caller (get by-id (:caller-module-source-id edge))
                          callee (get by-id (:callee-module-source-id edge))]
                      {:ordinal (:ordinal edge)
                       :caller-module-ordinal (:module-ordinal caller)
                       :caller-module (:module caller)
                       :caller-function (:caller-function edge)
                       :callsite-path (:callsite-path edge)
                       :callee-module-ordinal (:module-ordinal callee)
                       :callee-module (:module callee)
                       :callee-function (:callee-function edge)
                       :arity (:arity edge)
                       :owner-edge (:owner-edge edge)}))
                  (:edges manifest))
            source-census (p15-s23-sh07-b51-derive-callsite-census)]
        (throw (ex-info "invalid SH-07 B51 M1 manifest"
                        {:id "L2-UNKNOWN-CORE-FORM"
                         :reason :unauthenticated-b51-external-link
                         :descriptor-valid?
                         (mapv p15-s23-sh07-b51-module-source-valid?
                               descriptors)
                         :source-census-match? (= source-census projected)
                         :source-census-count (count source-census)
                         :projected-count (count projected)
                         :root8-external-symbols
                         (mapv :symbol
                               (:external-bindings (nth descriptors 6)))}))))
    manifest))

(defn p15-s23-sh07-b51-enrich-census-edges
  [descriptors census-edges]
  (let [by-ordinal (into {} (map (juxt :module-ordinal identity)) descriptors)]
    (when-not (and (vector? census-edges)
                   (= (vec (range (count census-edges)))
                      (mapv :ordinal census-edges))
                   (every? #(p15-s23-sh07-b51-exact-keys?
                             % p15-s23-sh07-b51-census-edge-keys)
                           census-edges))
      (throw (ex-info "invalid SH-07 B51 callsite census"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    (mapv
     (fn [row]
       (let [caller (get by-ordinal (:caller-module-ordinal row))
             callee (get by-ordinal (:callee-module-ordinal row))
             caller-definition
             (p15-s23-sh07-b51-definition-by-name caller (:caller-function row))
             callee-definition
             (p15-s23-sh07-b51-definition-by-name callee (:callee-function row))]
         (when-not (and caller callee caller-definition callee-definition
                        (= (:caller-module row) (:module caller))
                        (= (:callee-module row) (:module callee)))
           (throw (ex-info "callsite census does not join module sources"
                           {:id "L2-UNKNOWN-CORE-FORM"
                            :reason :unauthenticated-b51-external-link
                            :ordinal (:ordinal row)})))
         {:ordinal (:ordinal row)
          :caller-module-source-id (:module-source-id caller)
          :caller-function (:caller-function row)
          :caller-function-source-hash (:source-hash caller-definition)
          :callsite-path (:callsite-path row)
          :callee-module-source-id (:module-source-id callee)
          :callee-function (:callee-function row)
          :callee-function-source-hash (:source-hash callee-definition)
          :arity (:arity row)
          :owner-edge (:owner-edge row)
          :authorization :authenticated-b51-namespace-external}))
     census-edges)))

(defn- p15-s23-sh07-b51-derive-callsite-census-from-modules
  [modules]
  (let [all-function-names
        (vec (mapcat (comp keys :table) modules))
        _ (when-not (= (count all-function-names)
                       (count (set all-function-names)))
            (throw (ex-info "SH-07 B51 module definitions are duplicated"
                            {:id "L2-UNKNOWN-CORE-FORM"
                             :reason :unauthenticated-b51-external-link})))
        owner-by-function
        (into {} (for [{:keys [spec table]} modules, name (keys table)]
                   [name {:module-ordinal (:module-ordinal spec)
                          :module (:module spec) :owner (:owner spec)}]))
        rows
        (vec
         (mapcat
          (fn [{:keys [spec table]}]
            (let [locals (set (keys table))]
              (mapcat
               (fn [caller]
                 (let [definition (get table caller)]
                   (mapcat
                    (fn [body-index body-form]
                      (letfn [(walk [value path]
                                (cond
                                  (seq? value)
                                  (if (= 'quote (first value))
                                    []
                                    (let [callee (first value)
                                          callee-owner (get owner-by-function callee)
                                          row
                                          (when (and callee-owner
                                                     (not (contains? locals callee)))
                                            [{:caller-module-ordinal
                                              (:module-ordinal spec)
                                              :caller-module (:module spec)
                                              :caller-function caller
                                              :callsite-path path
                                              :callee-module-ordinal
                                              (:module-ordinal callee-owner)
                                              :callee-module (:module callee-owner)
                                              :callee-function callee
                                              :arity (count (rest value))
                                              :owner-edge
                                              (cond
                                                (= (:owner spec)
                                                   (:owner callee-owner))
                                                (if (= :r1-producer (:owner spec))
                                                  :producer-internal
                                                  :root6-internal)
                                                (and (= :r8-finalizer (:owner spec))
                                                     (= :r1-producer
                                                        (:owner callee-owner)))
                                                :root8-to-producer-interface
                                                :else :forbidden-owner-edge)}])]
                                      (into (or row [])
                                            (mapcat (fn [index child]
                                                      (walk child
                                                            (conj path index)))
                                                    (range) value))))

                                  (vector? value)
                                  (mapcat (fn [index child]
                                            (walk child (conj path index)))
                                          (range) value)

                                  (map? value)
                                  (let [entries
                                        (sort-by
                                         #(pr-str (reader-canonical-value %))
                                         value)]
                                    (mapcat
                                     (fn [entry-index [key item]]
                                       (concat
                                        (walk key (conj path entry-index 0))
                                        (walk item (conj path entry-index 1))))
                                     (range) entries))

                                  (set? value)
                                  (mapcat (fn [index item]
                                            (walk item (conj path index)))
                                          (range)
                                          (sort-by
                                           #(pr-str (reader-canonical-value %))
                                           value))
                                  :else []))]
                        (walk body-form [body-index])))
                    (range) (:body definition))))
               (sort-by str (keys table)))))
          modules))
        ordered
        (vec (sort-by (juxt :caller-module-ordinal
                            (comp str :caller-function)
                            (comp count :callsite-path)
                            :callsite-path)
                      rows))]
    (when-not (and (= 1480 (count ordered))
                   (not-any? #(= :forbidden-owner-edge (:owner-edge %))
                             ordered)
                   (= (count ordered)
                      (count (set (map (juxt :caller-module-ordinal
                                             :caller-function :callsite-path)
                                       ordered)))))
      (throw (ex-info "SH-07 B51 source callsite census is not governed"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :observed-count (count ordered)})))
    (mapv #(assoc %1 :ordinal %2) ordered (range))))

(defn p15-s23-sh07-b51-derive-callsite-census []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :callsite-census)
        (let [modules
              (mapv
               (fn [spec]
                 (let [module-ordinal (:module-ordinal spec)
                       source
                       (p15-s23-sh07-b51-session-checked-product
                        [:module-input module-ordinal])]
                   (when-not source
                     (p15-s23-sh07-b51-governed-read-fail!
                      :module-validation-not-consumed
                      {:module-ordinal module-ordinal}))
                   (p15-s23-sh07-b51-governed-source-event!
                    :callsite-census (+ 3 module-ordinal) module-ordinal
                    'gravity.bootstrap/p15-s23-sh07-b51-derive-callsite-census
                    (fn [entry]
                      (when-not (and (= (:source-descriptor source)
                                         (:descriptor entry))
                                     (= (:text source) (:text entry)))
                        (p15-s23-sh07-b51-governed-read-fail!
                         :cached-source-mismatch
                         {:module-ordinal module-ordinal
                          :domain :callsite-census}))
                      true))
                   source))
               p15-s23-sh07-b51-module-specs)
              census
              (p15-s23-sh07-b51-derive-callsite-census-from-modules modules)]
          (p15-s23-sh07-b51-cache-session-product!
           :callsite-census census)))
    (p15-s23-sh07-b51-derive-callsite-census-from-modules
     (mapv p15-s23-sh07-b51-compatibility-module-input
           p15-s23-sh07-b51-module-specs))))

(defn- p15-s23-sh07-b51-build-link-authority-from-census
  [census-edges]
  (let [sources
        (if *p15-s23-sh07-b51-governed-source-session*
          (mapv
           (fn [spec]
             (let [module-ordinal (:module-ordinal spec)
                   source
                   (p15-s23-sh07-b51-session-checked-product
                    [:module-input module-ordinal])]
               (when-not source
                 (p15-s23-sh07-b51-governed-read-fail!
                  :module-validation-not-consumed
                  {:module-ordinal module-ordinal}))
               (p15-s23-sh07-b51-governed-source-event!
                :link-authority (+ 3 module-ordinal) module-ordinal
                'gravity.bootstrap/p15-s23-sh07-b51-build-link-authority-from-census
                (fn [entry]
                  (when-not (and (= (:source-descriptor source)
                                     (:descriptor entry))
                                 (= (:text source) (:text entry)))
                    (p15-s23-sh07-b51-governed-read-fail!
                     :cached-source-mismatch
                     {:module-ordinal module-ordinal
                      :domain :link-authority}))
                  true))
               source))
           p15-s23-sh07-b51-module-specs)
          (mapv p15-s23-sh07-b51-compatibility-module-input
                p15-s23-sh07-b51-module-specs))
        by-ordinal (into {} (map (fn [source]
                                  [(get-in source [:spec :module-ordinal])
                                   source])
                                sources))
        external-symbols
        (into {}
              (map (fn [ordinal]
                     [ordinal
                      (vec (sort-by str
                                    (set (map :callee-function
                                              (filter #(= ordinal
                                                          (:caller-module-ordinal %))
                                                      census-edges)))))]))
              (range 7))
        descriptors
        (mapv
         (fn [{:keys [spec path text module table]}]
           (let [ordinal (:module-ordinal spec)
                 externals
                 (mapv
                  (fn [symbol]
                    (let [sample (first (filter
                                         #(and (= ordinal
                                                  (:caller-module-ordinal %))
                                               (= symbol (:callee-function %)))
                                         census-edges))
                          callee-source (get by-ordinal
                                             (:callee-module-ordinal sample))
                          callee-definition (get (:table callee-source) symbol)]
                      {:symbol symbol
                       :callee-module-ordinal (:callee-module-ordinal sample)
                       :callee-module (get-in callee-source [:spec :module])
                       :callee-source-sha256 (:source-sha256 callee-source)
                       :callee-definition-source-hash
                       (p15-s23-sh07-source-definition-hash callee-definition)
                       :arity (:arity sample)
                       :owner-edge (:owner-edge sample)}))
                  (get external-symbols ordinal))
                 builtins (p15-s23-sh07-b51-builtin-call-census table)]
             (p15-s23-sh07-b51-build-module-source
              path text module
              (p15-s23-sh07-b51-binding-census
               path module (:module-ordinal spec))
              externals builtins)))
         sources)
        edges (p15-s23-sh07-b51-enrich-census-edges descriptors census-edges)
        manifest (p15-s23-sh07-b51-seal-link-manifest descriptors edges)]
    {:module-source-descriptors descriptors
     :link-manifest manifest
     :sources sources}))

(defn p15-s23-sh07-b51-build-link-authority []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :link-authority)
        (p15-s23-sh07-b51-cache-session-product!
         :link-authority
         (p15-s23-sh07-b51-build-link-authority-from-census
          (p15-s23-sh07-b51-derive-callsite-census))))
    (p15-s23-sh07-b51-build-link-authority-from-census
     (p15-s23-sh07-b51-derive-callsite-census))))

(def ^:private p15-s23-sh07-b51-compiler-source-path
  "bootstrap/gravity/p15_s23/compiler.gravity")

(defn- p15-s23-sh07-b51-current-emitter []
  (if *p15-s23-sh07-b51-governed-source-session*
    (or (p15-s23-sh07-b51-session-checked-product :current-emitter)
        (let [emitter
              (p15-s23-sh07-b51-governed-source-event!
               :compiler-emitter 10 nil
               'gravity.bootstrap/p15-s23-sh07-b51-current-emitter
               (fn [{:keys [descriptor text]}]
                 (let [source-path (:path descriptor)
                       forms
                       (:forms
                        (p15-s23-compiler-source-form-record-from-text
                         source-path text))]
                   (p15-s23-compiler-def-value
                    source-path forms 'p15-s23-stage2-plan-emitter))))]
          (p15-s23-sh07-b51-cache-session-product!
           :current-emitter emitter)))
    (let [{:keys [path text]}
          (p15-s23-sh07-b51-compatibility-source-record
           p15-s23-sh07-b51-compiler-source-path)
          forms (:forms
                 (p15-s23-compiler-source-form-record-from-text path text))]
      (p15-s23-compiler-def-value
       path forms 'p15-s23-stage2-plan-emitter))))

(defn p15-s23-sh07-b51-build-execution-bundle
  ([]
   (p15-s23-sh07-b51-build-execution-bundle
    (p15-s23-sh07-b51-current-emitter)))
  ([emitter]
  (when-not (= emitter (p15-s23-sh07-b51-current-emitter))
    (throw (ex-info "unauthenticated SH-07 B51 compiler identity"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-external-link})))
  (let [{:keys [module-source-descriptors link-manifest sources]}
        (p15-s23-sh07-b51-build-link-authority)
        sources
        (or (when *p15-s23-sh07-b51-governed-source-session*
              (p15-s23-sh07-b51-session-checked-product :plan-sources))
            sources)
        compilation-emitter
        (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
        raw-plans
        (mapv
         (fn [descriptor source]
           (let [module
                 (cond->
                  (assoc (:module source)
                         :p15-s23-sh07-module-source-descriptor descriptor
                         :p15-s23-sh07-link-manifest link-manifest)
                   (:source-authority source)
                   (assoc :p15-s23-sh07-source-authority
                          (:source-authority source))
                   (:compiler-artifact-binding source)
                   (assoc :p15-s23-sh07-compiler-artifact-binding
                          (:compiler-artifact-binding source)))]
             (p15-s23-stage2-construct-emitted-core-plan
              compilation-emitter (:path source) (:text source)
              module (:table source))))
         module-source-descriptors sources)
        ;; M2 identity is constructed lexically from the seven plans emitted
        ;; immediately above.  There is deliberately no Var that accepts an
        ;; arbitrary plan and returns an M2 product or id.
        plans
        (mapv
         (fn [descriptor raw-plan]
           (let [emitter-source-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-source-content-hash])
                 emitter-semantic-id
                 (get-in raw-plan
                         [:compiler :expression-lowering-semantic-hash])
                 base
                 (-> (dissoc raw-plan :plan-id)
                     (assoc :sh07-b51-module-source-id
                            (:module-source-id descriptor)
                            :sh07-b51-link-manifest-id
                            (:manifest-id link-manifest))
                     ;; Attempt30 makes the already authenticated compiler
                     ;; binding coordinates explicit in every M2.  The two
                     ;; census implementations join these aliases without
                     ;; depending on a lower-level compiler-field spelling.
                     (assoc-in [:compiler :source-content-hash]
                               emitter-source-id)
                     (assoc-in [:compiler :semantic-hash]
                               emitter-semantic-id))
                 identity
                 (-> base
                     (update :source dissoc :path)
                     (update :module dissoc :source-path))]
             (assoc base :plan-id
                    (c4-artifact-id
                     (c-backend-canonical-value identity)))))
         module-source-descriptors raw-plans)
        plan-by-source
        (into {} (map (juxt :sh07-b51-module-source-id identity)) plans)
        bundle-core
        {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
         :schema-version 1
         :manifest-id (:manifest-id link-manifest)
         :module-plans
         (mapv (fn [descriptor plan]
                 {:module-ordinal (:module-ordinal descriptor)
                  :module-source-id (:module-source-id descriptor)
                  :plan-id (:plan-id plan)})
               module-source-descriptors plans)
         :routes
         (mapv (fn [edge]
                 {:edge-ordinal (:ordinal edge)
                  :caller-plan-id
                  (:plan-id
                   (get plan-by-source (:caller-module-source-id edge)))
                  :callee-plan-id
                  (:plan-id
                   (get plan-by-source (:callee-module-source-id edge)))})
               (:edges link-manifest))
         :public-roots
         (mapv (fn [root]
                 {:root-ordinal (:root-ordinal root)
                  :function (:function root)
                  :arity (:arity root)
                  :plan-id (:plan-id
                            (nth plans (:module-ordinal root)))})
               p15-s23-sh07-b51-public-root-specs)}
        bundle-core-id (p15-s23-sh07-b51-canonical-id bundle-core)
        emitter-source-id (get-in (first plans)
                                  [:compiler :source-content-hash])
        emitter-semantic-id (get-in (first plans)
                                    [:compiler :semantic-hash])
        products
        {:artifact :gravity/sh07-b51-linked-plan-products-v2
         :module-source-descriptors module-source-descriptors
         :link-manifest link-manifest
         :module-plans plans
         :emitter-source-id emitter-source-id
         :emitter-semantic-id emitter-semantic-id
         :execution-bundle-core bundle-core
         :execution-bundle-core-id bundle-core-id}]
    (when-not (and
               (p15-s23-sh07-b51-digest-id? emitter-source-id)
               (p15-s23-sh07-b51-digest-id? emitter-semantic-id)
               (every? #(and (= emitter-source-id
                                 (get-in % [:compiler :source-content-hash]))
                              (= emitter-semantic-id
                                 (get-in % [:compiler :semantic-hash])))
                       plans)
               (p15-s23-sh07-b51-execution-bundle-core-valid-structure?
                module-source-descriptors link-manifest plans bundle-core
                bundle-core-id))
      (throw (ex-info "invalid fresh-source SH-07 B51 bundle-core products"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :emitter-source-id emitter-source-id
                       :emitter-semantic-id emitter-semantic-id
                       :manifest-valid?
                       (p15-s23-sh07-b51-link-manifest-valid?
                        module-source-descriptors link-manifest)
                       :plan-valid?
                       (mapv #(p15-s23-sh07-b51-plan-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :plan-links-valid?
                       (mapv #(p15-s23-sh07-b51-plan-links-valid-structure?
                               %1 link-manifest %2)
                             module-source-descriptors plans)
                       :bundle-core-id bundle-core-id
                       :computed-bundle-core-id
                       (p15-s23-sh07-b51-canonical-id bundle-core)})))
    products)))

(defn- p15-s23-sh07-b51-project-context
  [commit tree source-path]
  (let [project-root-id
        (reader-canonical-hash
         [:gravity/sh07-b51-c2-project-root-id-v1
          {:content {:commit commit :tree tree}
           :scope :sh07-b51-c2-explicit-project}])]
    {:project-root-id project-root-id
     :project-root-path nil
     :project-relative-path source-path}))

(def ^:private p15-s23-sh07-b51-resolved-pass
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :passed :rule nil})

(def ^:private p15-s23-sh07-b51-resolved-reject
  {:artifact :gravity/sh07-core-resolved-verification
   :schema-version 16 :status :rejected :rule "C6-VERIFY"})

;; SH-06 is intentionally unaware of B51's seven-fragment manifest.  The
;; pure constructors below close the exact M0/M1-to-SH06 import projection.
;; Installing that projection requires an explicit lexical SH06 constructor
;; and verifier seam; ambient Var state is deliberately not used as authority.

(defn- p15-s23-sh07-b51-c5-authority-fail!
  [cause details]
  (p15-s23-sh07-b51-governed-read-fail!
   :unauthenticated-c5-external-binding
   (assoc details :cause cause)))

(def ^:private p15-s23-sh07-b51-c5-preimage-keys
  #{:schema :content :content-object-proof :source-descriptors
    :module-source-inputs :module-source-descriptors :link-manifest})

(def ^:private p15-s23-sh07-b51-c5-authority-keys
  #{:schema :schema-version :content :module-ordinal :source-path
    :sh05-artifact-id :base-request-id :imports :import-bindings
    :dependency-edges :preimage :self-id-omits :authority-id})

(def ^:private p15-s23-sh07-b51-c5-import-keys
  #{:namespace :alias :kind :profile :targets :visibility :refer :effects
    :capabilities :safety :boundary :semantic-span :source-span
    :dependency-artifact-id :foreign-record-complete?})

(def ^:private p15-s23-sh07-b51-c5-import-binding-keys
  #{:name :kind :namespace :package :binding-class :visibility :profile-set
    :target-set :type-ref :effects :capabilities :safety :semantic-span
    :source-span :definition-syntax-id :definition-artifact-id})

(def ^:private p15-s23-sh07-b51-c5-dependency-edge-keys
  #{:from :to :kind :boundary :semantic-span :source-span})

(def ^:private p15-s23-sh07-b51-resolution-plan-authority-keys
  #{:schema :content :content-object-proof :source-descriptor
    :source-object-proof :plan-binding :self-id-omits :authority-id})

(def ^:private p15-s23-sh07-b51-resolution-plan-binding-keys
  #{:source-path :source-byte-count :source-content-hash
    :plan-semantic-hash :functions-semantic-hash :function-count
    :function-names-hash :function-shapes-hash
    :public-function-hashes :public-function-shapes
    :artifact :status :semantic-authority :compiled-by :executed-by
    :generic-bridge-residual? :self-hosted? :plan})

(def ^:private p15-s23-sh07-b51-sh06-context-keys
  #{:external-binding-authority :resolution-plan-authority})

(defn- p15-s23-sh07-b51-c5-authority-rows*
  [module-ordinal source descriptors manifest]
  (when-not (and (integer? module-ordinal) (<= 0 module-ordinal 6)
                 (vector? descriptors) (= 7 (count descriptors))
                 (= (vec (range 7)) (mapv :module-ordinal descriptors))
                 (= 7 (count (set (map :module-source-id descriptors))))
                 (map? manifest) (vector? (:edges manifest)))
    (p15-s23-sh07-b51-c5-authority-fail!
     :authority-shape
     {:module-ordinal module-ordinal
      :descriptor-count (when (vector? descriptors) (count descriptors))}))
  (let [caller (nth descriptors module-ordinal)
        source-descriptor (:source-descriptor source)
        externals (:external-bindings caller)
        caller-edges
        (filterv #(= (:module-source-id caller)
                     (:caller-module-source-id %))
                 (:edges manifest))]
    (when-not
     (and (= caller (nth descriptors (:module-ordinal caller) nil))
          (= (:module caller) (get-in source [:module :module]))
          (= (:logical-source-path caller) (:path source)
             (:path source-descriptor))
          (= (:source-sha256 caller) (:source-sha256 source)
             (:raw-sha256 source-descriptor))
          (= (:source-byte-count caller) (alength ^bytes (:bytes source)))
          (vector? externals))
      (p15-s23-sh07-b51-c5-authority-fail!
       :caller-descriptor
       {:module-ordinal module-ordinal
        :logical-source-path (:logical-source-path caller)}))
    (when-not (= (count externals)
                 (count (set (map :symbol externals))))
      (p15-s23-sh07-b51-c5-authority-fail!
       :ambiguous-external-binding
       {:module-ordinal module-ordinal
        :symbols (mapv :symbol externals)}))
    (let [rows
          (mapv
           (fn [external]
             (let [symbol (:symbol external)
                   callee-ordinal (:callee-module-ordinal external)
                   callee (when (and (integer? callee-ordinal)
                                     (<= 0 callee-ordinal 6))
                            (nth descriptors callee-ordinal nil))
                   definitions
                   (filterv #(= symbol (:name %)) (:definitions callee))
                   definition (first definitions)
                   edges (filterv #(= symbol (:callee-function %))
                                  caller-edges)
                   expected-owner-edge
                   (when callee
                     (p15-s23-sh07-b51-owner-edge-kind
                      (:owner caller) (:owner callee)))]
               (when-not (= 1 (count definitions))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :ambiguous-external-binding
                  {:module-ordinal module-ordinal :symbol symbol
                   :callee-module-ordinal callee-ordinal
                   :definition-count (count definitions)}))
               (when (empty? edges)
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :missing-authorized-edge
                  {:module-ordinal module-ordinal :symbol symbol}))
               (when-not
                (and expected-owner-edge
                     (= expected-owner-edge (:owner-edge external))
                     (every?
                      #(and (= :authenticated-b51-namespace-external
                               (:authorization %))
                            (= expected-owner-edge (:owner-edge %)))
                      edges))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :unauthorized-owner-edge
                  {:module-ordinal module-ordinal :symbol symbol
                   :expected-owner-edge expected-owner-edge
                   :external-owner-edge (:owner-edge external)
                   :edge-owner-edges (mapv :owner-edge edges)
                   :authorizations (mapv :authorization edges)}))
               (when-not
                (and callee
                     (= callee-ordinal (:module-ordinal callee))
                     (= (:callee-module external) (:module callee))
                     (= (:callee-source-sha256 external)
                        (:source-sha256 callee))
                     (= (:callee-definition-source-hash external)
                        (:source-hash definition))
                     (= (:arity external) (:arity definition))
                     (every?
                      (fn [edge]
                        (let [caller-definition
                              (p15-s23-sh07-b51-definition-by-name
                               caller (:caller-function edge))]
                          (and caller-definition
                               (= (:caller-function-source-hash edge)
                                  (:source-hash caller-definition))
                               (= (:callee-module-source-id edge)
                                  (:module-source-id callee))
                               (= (:callee-function-source-hash edge)
                                  (:source-hash definition))
                               (= (:arity edge) (:arity external)))))
                      edges))
                 (p15-s23-sh07-b51-c5-authority-fail!
                  :stale-external-binding
                  {:module-ordinal module-ordinal :symbol symbol
                   :callee-module-ordinal callee-ordinal}))
               {:external external
                :callee callee
                :definition definition
                :edges edges}))
           externals)
          covered-edge-ordinals
          (mapcat #(map :ordinal (:edges %)) rows)]
      (when-not
       (and (= (count caller-edges) (count covered-edge-ordinals))
            (= (set (map :ordinal caller-edges))
               (set covered-edge-ordinals))
            (= (count covered-edge-ordinals)
               (count (set covered-edge-ordinals))))
        (p15-s23-sh07-b51-c5-authority-fail!
         :extra-or-ambiguous-authorized-edge
         {:module-ordinal module-ordinal
          :caller-edge-ordinals (mapv :ordinal caller-edges)
          :covered-edge-ordinals (vec covered-edge-ordinals)}))
      rows)))

(defn- p15-s23-sh07-b51-c5-request-carriers
  [caller manifest authority-rows]
  (let [groups
        (->> authority-rows
             (group-by #(get-in % [:callee :module-ordinal]))
             (sort-by key)
             (mapv second))
        imports
        (mapv
         (fn [rows]
           (let [callee (:callee (first rows))
                 edge-ordinals (vec (mapcat #(map :ordinal (:edges %)) rows))
                 dependency-id
                 (p15-s23-sh07-b51-canonical-id
                  [:gravity/sh07-b51-authenticated-sh06-import-v1
                   {:manifest-id (:manifest-id manifest)
                    :caller-module-source-id (:module-source-id caller)
                    :callee-module-source-id (:module-source-id callee)
                    :bindings
                    (mapv (fn [{:keys [external edges]}]
                            {:external-binding external
                             :edge-ordinals (mapv :ordinal edges)})
                          rows)}])]
             {:namespace (:module callee)
              :alias nil
              :kind :namespace
              :profile (:profile callee)
              :targets [(:target callee)]
              :visibility :public
              :refer (mapv #(get-in % [:external :symbol]) rows)
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :boundary (get-in rows [0 :external :owner-edge])
              :semantic-span
              {:manifest-id (:manifest-id manifest)
               :caller-module-source-id (:module-source-id caller)
               :callee-module-source-id (:module-source-id callee)
               :authorized-edge-ordinals edge-ordinals}
              :source-span {:source (:logical-source-path caller)}
              :dependency-artifact-id dependency-id
              :foreign-record-complete? true}))
         groups)
        import-bindings
        (mapv
         (fn [{:keys [external callee definition edges]}]
           (let [edge-ordinals (mapv :ordinal edges)
                 coordinate
                 {:manifest-id (:manifest-id manifest)
                  :caller-module-source-id (:module-source-id caller)
                  :callee-module-source-id (:module-source-id callee)
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash definition)
                  :symbol (:symbol external)
                  :arity (:arity external)
                  :owner-edge (:owner-edge external)
                  :authorized-edge-ordinals edge-ordinals}]
             {:name (:symbol external)
              :kind :function
              :namespace (:module callee)
              :package (sh06-resolution-package (:module callee))
              :binding-class :import
              ;; M1 authorization is the visibility grant.  The originating
              ;; M0 definition may remain module-private.
              :visibility :public
              :profile-set [(:profile callee)]
              :target-set [(:target callee)]
              :type-ref :gravity.type/function
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :semantic-span coordinate
              :source-span {:source :external-binding}
              :definition-syntax-id
              (p15-s23-sh07-b51-canonical-id
               [:gravity/sh07-b51-authenticated-sh06-binding-v1 coordinate])
              :definition-artifact-id (:module-source-id callee)}))
         authority-rows)
        dependency-edges
        (mapv
         (fn [dependency]
           {:from (:module caller)
            :to (:namespace dependency)
            :kind :namespace
            :boundary (:boundary dependency)
            :semantic-span (:semantic-span dependency)
            :source-span (:source-span dependency)})
         imports)]
    (when-not
     (and (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-import-keys)
                  imports)
          (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-import-binding-keys)
                  import-bindings)
          (every? #(p15-s23-sh07-b51-exact-keys?
                    % p15-s23-sh07-b51-c5-dependency-edge-keys)
                  dependency-edges))
      (p15-s23-sh07-b51-c5-authority-fail!
       :projection-schema {:module-ordinal (:module-ordinal caller)}))
    {:imports imports
     :import-bindings import-bindings
     :dependency-edges dependency-edges}))

(defn- p15-s23-sh07-b51-validate-c5-authority-preimage!
  [preimage]
  (when-not
   (and (p15-s23-sh07-b51-exact-keys?
         preimage p15-s23-sh07-b51-c5-preimage-keys)
        (= :gravity/sh07-b51-c5-authority-preimage-v1
           (:schema preimage))
        (= #{:commit :tree} (set (keys (:content preimage))))
        (vector? (:source-descriptors preimage))
        (= 7 (count (:source-descriptors preimage)))
        (vector? (:module-source-inputs preimage))
        (= 7 (count (:module-source-inputs preimage)))
        (vector? (:module-source-descriptors preimage))
        (= 7 (count (:module-source-descriptors preimage))))
    (p15-s23-sh07-b51-c5-authority-fail!
     :preimage-shape {}))
  (p15-s23-sh07-b51-validate-content-object-proof!
   (:content preimage) (:content-object-proof preimage))
  (let [content (:content preimage)
        source-descriptors (:source-descriptors preimage)
        inputs (:module-source-inputs preimage)
        descriptors (:module-source-descriptors preimage)
        manifest (:link-manifest preimage)
        sources
        (mapv
         (fn [module-ordinal input]
           (when-not
            (and (p15-s23-sh07-b51-exact-keys?
                  input #{:descriptor :source-object-proof})
                 (= (nth source-descriptors module-ordinal nil)
                    (:descriptor input)))
             (p15-s23-sh07-b51-c5-authority-fail!
              :module-source-input-shape
              {:module-ordinal module-ordinal}))
           (let [descriptor (:descriptor input)
                 expected-spec (nth p15-s23-sh07-b51-source-specs
                                    (+ 3 module-ordinal))
                 bytes
                 (p15-s23-sh07-b51-validate-source-object-proof!
                  content descriptor (:source-object-proof input))
                 text
                 (p15-s23-sh07-b51-strict-utf8
                  bytes [:c5-authority module-ordinal :source])]
             (when-not
              (= (select-keys descriptor
                              [:ordinal :kind :module-ordinal :path])
                 expected-spec)
              (p15-s23-sh07-b51-c5-authority-fail!
               :module-source-descriptor
               {:module-ordinal module-ordinal}))
             (p15-s23-sh07-b51-module-input-record
              (p15-s23-sh07-b51-module-spec module-ordinal)
              {:descriptor descriptor :bytes bytes :text text})))
         (range 7) inputs)
        _
        (doseq [module-ordinal (range 7)]
          (when-not
           (p15-s23-sh07-b51-module-source-valid-with-input?
            (nth descriptors module-ordinal)
            (nth sources module-ordinal))
            (p15-s23-sh07-b51-c5-authority-fail!
             :module-source-rederivation
             {:module-ordinal module-ordinal})))
        census
        (p15-s23-sh07-b51-derive-callsite-census-from-modules sources)
        expected-edges
        (p15-s23-sh07-b51-enrich-census-edges descriptors census)
        expected-manifest-base
        {:artifact :gravity/sh07-b51-link-manifest-v1
         :schema-version 1
         :module-source-ids (mapv :module-source-id descriptors)
         :edges expected-edges}
        expected-manifest
        (assoc expected-manifest-base :manifest-id
               (p15-s23-sh07-b51-manifest-id expected-manifest-base))
        root8 (nth descriptors 6)
        root6-source-ids
        (set (map :module-source-id (subvec descriptors 4 6)))
        root6-cross-edges
        (filter #(and (contains? root6-source-ids
                                 (:caller-module-source-id %))
                      (not (contains? root6-source-ids
                                      (:callee-module-source-id %))))
                expected-edges)]
    (when-not
     (and (= expected-manifest manifest)
          (= 1480 (count expected-edges))
          (= p15-s23-sh07-b51-root8-external-callees
             (mapv :symbol (:external-bindings root8)))
          (empty? root6-cross-edges))
      (p15-s23-sh07-b51-c5-authority-fail!
       :m0-m1-rederivation
       {:expected-manifest-id (:manifest-id expected-manifest)
        :actual-manifest-id (:manifest-id manifest)
        :edge-count (count expected-edges)}))
    ;; Every caller is checked, including modules with no external symbols.
    ;; This closes visibility, source/definition/arity/owner joins and proves
    ;; complete edge coverage rather than trusting a self-consistent M0/M1.
    (doseq [module-ordinal (range 7)]
      (p15-s23-sh07-b51-c5-authority-rows*
       module-ordinal (nth sources module-ordinal) descriptors manifest))
    {:sources sources :descriptors descriptors :manifest manifest}))

(defn p15-s23-sh07-b51-c5-external-binding-authority
  [module-ordinal source sh05-artifact base-request preimage]
  (let [{:keys [sources descriptors manifest]}
        (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
        expected-source (nth sources module-ordinal nil)
        caller (nth descriptors module-ordinal nil)
        source-path (:path expected-source)
        recomputed-base
        (when (and caller expected-source)
          (sh06-resolution-request source-path sh05-artifact))]
    (when-not
     (and caller expected-source
          (= source-path (:path source))
          (= (:source-descriptor expected-source)
             (:source-descriptor source))
          (= (:source-sha256 expected-source) (:source-sha256 source))
          (p15-s23-sh07-b51-byte-array-equal?
           (:bytes expected-source) (:bytes source))
          (= recomputed-base base-request)
          (= (:module caller) (get-in base-request [:module :namespace]))
          (= (:artifact-id sh05-artifact)
             (get-in base-request [:module :c4-artifact-id])))
      (p15-s23-sh07-b51-c5-authority-fail!
       :caller-sh05-base-join {:module-ordinal module-ordinal}))
    (let [rows
          (p15-s23-sh07-b51-c5-authority-rows*
           module-ordinal expected-source descriptors manifest)
          carriers
          (p15-s23-sh07-b51-c5-request-carriers caller manifest rows)
          external-symbols
          (set (map #(get-in % [:external :symbol]) rows))
          base-binding-symbols
          (set (concat (map :name (:definitions base-request))
                       (map :name (:import-bindings base-request))))]
      (when-not (empty? (clojure.set/intersection external-symbols
                                                   base-binding-symbols))
        (p15-s23-sh07-b51-c5-authority-fail!
         :base-binding-collision
         {:module-ordinal module-ordinal
          :symbols (vec (sort-by str external-symbols))}))
      (let [base
            (merge
             {:schema :gravity/sh07-b51-c5-external-binding-authority-v1
              :schema-version 1
              :content (:content preimage)
              :module-ordinal module-ordinal
              :source-path source-path
              :sh05-artifact-id (:artifact-id sh05-artifact)
              :base-request-id
              (p15-s23-sh07-b51-canonical-id
               [:gravity/sh07-b51-sh06-base-request-v1 base-request])
              :preimage preimage
              :self-id-omits [[:authority-id]]}
             carriers)
            authority
            (assoc base :authority-id
                   (p15-s23-sh07-b51-canonical-id
                    [:gravity/sh07-b51-c5-external-binding-authority-v1
                     base]))]
        (when-not
         (and (p15-s23-sh07-b51-exact-keys?
               authority p15-s23-sh07-b51-c5-authority-keys)
              (p15-s23-sh07-b51-digest-id? (:authority-id authority)))
          (p15-s23-sh07-b51-c5-authority-fail!
           :authority-schema {:module-ordinal module-ordinal}))
        authority))))

(defn p15-s23-sh07-b51-c5-external-binding-authority-valid?
  [authority]
  (try
    (let [{:keys [sources descriptors manifest]}
          (p15-s23-sh07-b51-validate-c5-authority-preimage!
           (:preimage authority))
          module-ordinal (:module-ordinal authority)
          source (nth sources module-ordinal nil)
          caller (nth descriptors module-ordinal nil)
          rows
          (when (and source caller)
            (p15-s23-sh07-b51-c5-authority-rows*
             module-ordinal source descriptors manifest))
          carriers
          (when rows
            (p15-s23-sh07-b51-c5-request-carriers
             caller manifest rows))
          expected-id
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-c5-external-binding-authority-v1
            (dissoc authority :authority-id)])]
      (boolean
       (and (p15-s23-sh07-b51-exact-keys?
             authority p15-s23-sh07-b51-c5-authority-keys)
            (= :gravity/sh07-b51-c5-external-binding-authority-v1
               (:schema authority))
            (= 1 (:schema-version authority))
            (= (:content (:preimage authority)) (:content authority))
            (= (:path source) (:source-path authority))
            (p15-s23-sh07-b51-digest-id? (:sh05-artifact-id authority))
            (p15-s23-sh07-b51-digest-id? (:base-request-id authority))
            (= carriers
               (select-keys authority
                            [:imports :import-bindings :dependency-edges]))
            (= [[:authority-id]] (:self-id-omits authority))
            (= expected-id (:authority-id authority)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-sh06-context
  [c5-authority resolution-plan-authority]
  (let [context {:external-binding-authority c5-authority
                 :resolution-plan-authority resolution-plan-authority}]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           context p15-s23-sh07-b51-sh06-context-keys)
          (p15-s23-sh07-b51-exact-keys?
           c5-authority p15-s23-sh07-b51-c5-authority-keys)
          (p15-s23-sh07-b51-exact-keys?
           resolution-plan-authority
           p15-s23-sh07-b51-resolution-plan-authority-keys)
          (p15-s23-sh07-b51-c5-external-binding-authority-valid?
           c5-authority)
          (p15-s23-sh07-b51-resolution-plan-authority-valid?
           resolution-plan-authority)
          (= (:content c5-authority)
             (:content resolution-plan-authority)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :context-join {}))
    context))

(defn- p15-s23-sh07-b51-authenticated-c5-request-adapter
  [module-ordinal source descriptors manifest sh05-artifact
   base-request-function]
  (when-not (p15-s23-sh07-b51-link-manifest-valid? descriptors manifest)
    (p15-s23-sh07-b51-c5-authority-fail!
     :unverified-m0-m1
     {:module-ordinal module-ordinal}))
  (let [authority-rows
        (p15-s23-sh07-b51-c5-authority-rows*
         module-ordinal source descriptors manifest)
        caller (nth descriptors module-ordinal)
        base-request
        (base-request-function (:path source) sh05-artifact)
        external-symbols (set (map #(get-in % [:external :symbol])
                                   authority-rows))
        base-binding-symbols
        (set (concat (map :name (:definitions base-request))
                     (map :name (:import-bindings base-request))))]
    (when-not
     (and (= (:module caller) (get-in base-request [:module :namespace]))
          (= (:artifact-id sh05-artifact)
             (get-in base-request [:module :c4-artifact-id]))
          (empty? (clojure.set/intersection external-symbols
                                            base-binding-symbols)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :unverified-m0-m1-or-caller-request
       {:module-ordinal module-ordinal
        :external-symbols (vec (sort-by str external-symbols))}))
    (merge
     {:source-path (:path source)
      :sh05-artifact-id (:artifact-id sh05-artifact)
      :base-request-id
      (p15-s23-sh07-b51-canonical-id
       [:gravity/sh07-b51-sh06-base-request-v1 base-request])
      :caller-module-source-id (:module-source-id caller)
      :manifest-id (:manifest-id manifest)}
     (p15-s23-sh07-b51-c5-request-carriers
      caller manifest authority-rows))))

(defn- p15-s23-sh07-b51-apply-c5-request-adapter
  [adapter source-path sh05-artifact base-request]
  (let [imports (:imports adapter)
        import-bindings (:import-bindings adapter)
        dependency-edges (:dependency-edges adapter)
        manifest-id (:manifest-id adapter)
        caller-id (:caller-module-source-id adapter)
        referred (vec (mapcat :refer imports))
        binding-names (mapv :name import-bindings)
        expected-dependency-edges
        (mapv
         (fn [dependency]
           {:from (get-in base-request [:module :namespace])
            :to (:namespace dependency)
            :kind :namespace
            :boundary (:boundary dependency)
            :semantic-span (:semantic-span dependency)
            :source-span (:source-span dependency)})
         imports)]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           adapter
           #{:source-path :sh05-artifact-id :base-request-id
             :caller-module-source-id :manifest-id :imports
             :import-bindings :dependency-edges})
          (= source-path (:source-path adapter))
          (= (:artifact-id sh05-artifact) (:sh05-artifact-id adapter))
          (= (:base-request-id adapter)
             (p15-s23-sh07-b51-canonical-id
              [:gravity/sh07-b51-sh06-base-request-v1 base-request]))
          (p15-s23-sh07-b51-digest-id? caller-id)
          (p15-s23-sh07-b51-digest-id? manifest-id)
          (vector? imports) (vector? import-bindings)
          (vector? dependency-edges)
          (= dependency-edges expected-dependency-edges)
          (= (set referred) (set binding-names))
          (= (count referred) (count (set referred)))
          (= (count binding-names) (count (set binding-names)))
          (every?
           #(and (= manifest-id (get-in % [:semantic-span :manifest-id]))
                 (= caller-id
                    (get-in % [:semantic-span
                               :caller-module-source-id])))
           imports)
          (every?
           #(and (= manifest-id (get-in % [:semantic-span :manifest-id]))
                 (= caller-id
                    (get-in % [:semantic-span
                               :caller-module-source-id])))
           import-bindings))
      (p15-s23-sh07-b51-c5-authority-fail!
       :request-replay-mismatch
       {:source-path source-path
        :expected-source-path (:source-path adapter)}))
    (-> base-request
        (update :imports into imports)
        (update :import-bindings into import-bindings)
        (update :dependency-edges into dependency-edges))))

(defn- p15-s23-sh07-b51-c5-scoped-request-function
  [adapter base-request-function]
  (fn [source-path candidate-sh05-artifact]
    (let [base-request
          (base-request-function source-path candidate-sh05-artifact)]
      (if (and (= source-path (:source-path adapter))
               (= (:artifact-id candidate-sh05-artifact)
                  (:sh05-artifact-id adapter))
               (= (:base-request-id adapter)
                  (p15-s23-sh07-b51-canonical-id
                   [:gravity/sh07-b51-sh06-base-request-v1 base-request])))
        (p15-s23-sh07-b51-apply-c5-request-adapter
         adapter source-path candidate-sh05-artifact base-request)
        base-request))))

(defn- p15-s23-sh07-b51-session-checked-core-binding
  [emitter]
  (let [{:keys [descriptor bytes text]}
        (p15-s23-sh07-b51-session-checked-product :checked-core-entry)
        source-path (:path descriptor)
        source-hash (p15-s23-sh07-b51-raw-sha256 bytes)
        plan (p15-s23-stage2-compiler-artifact-plan
              emitter source-path text)
        identities (sh07-core-plan-identities plan)]
    (when-not
     (and (= sh07-core-expected-source-byte-count (alength bytes))
          (= sh07-core-expected-source-content-hash source-hash)
          (= sh07-core-expected-plan-semantic-hash
             (:plan-semantic-hash identities))
          (= sh07-core-expected-functions-semantic-hash
             (:functions-semantic-hash identities))
          (= sh07-core-expected-function-count (:function-count identities))
          (= sh07-core-expected-function-names-hash
             (:function-names-hash identities))
          (= sh07-core-expected-function-shapes-hash
             (:function-shapes-hash identities))
          (= sh07-core-public-function-hashes
             (:public-function-hashes identities))
          (= sh07-core-public-function-shapes
             (:public-function-shapes identities)))
      (p15-s23-sh07-b51-governed-read-fail!
       :checked-core-binding
       {:source-path source-path :source-hash source-hash}))
    (merge
     {:artifact :gravity/sh07-pinned-core-plan-binding
      :source-path source-path
      :source-byte-count (alength bytes)
      :source-content-hash source-hash
      :plan plan
      :semantic-authority :gravity-source
      :compiled-by :clojure-stage0-seed
      :executed-by :clojure-stage2-generic-rule-runner
      :generic-bridge-residual? true
      :self-hosted? false}
     identities)))

(defn- p15-s23-sh07-b51-resolution-plan-binding-valid?
  [descriptor source-object-proof binding]
  (try
    (let [bytes
          (p15-s23-sh07-b51-unsigned-byte-array
           (:blob-bytes source-object-proof) :resolution-plan-source)
          identities (sh06-resolution-plan-identities (:plan binding))
          plan-base (dissoc (:plan binding) :plan-id)
          expected-plan-id
          (c4-artifact-id (c-backend-canonical-value plan-base))]
      (boolean
       (and
        (p15-s23-sh07-b51-exact-keys?
         binding p15-s23-sh07-b51-resolution-plan-binding-keys)
        (= {:ordinal 12 :kind :sh06-resolution :module-ordinal nil
            :path sh06-resolution-source-relative-path
            :mode "100644"
            :blob-oid "16ef2b638298a8e934fcd0b39131072305fbcfd5"
            :byte-count sh06-resolution-expected-source-byte-count
            :raw-sha256 sh06-resolution-expected-source-content-hash}
           descriptor)
        (= (:source-path binding) (:path descriptor))
        (= (:source-byte-count binding) (:byte-count descriptor)
           (alength ^bytes bytes))
        (= (:source-content-hash binding) (:raw-sha256 descriptor)
           (p15-s23-sh07-b51-raw-sha256 bytes))
        (= :gravity/sh06-pinned-resolution-plan-binding (:artifact binding))
        (= :complete (:status binding))
        (= :gravity-source (:semantic-authority binding))
        (= :clojure-stage0-seed (:compiled-by binding))
        (= :clojure-stage2-generic-rule-runner (:executed-by binding))
        (true? (:generic-bridge-residual? binding))
        (false? (:self-hosted? binding))
        (= :gravity/stage2-compiler-artifact-plan
           (get-in binding [:plan :kind]))
        (true? (get-in binding [:plan :compiler-artifact-plan?]))
        (= 'gravity.resolution (get-in binding [:plan :module :module]))
        (= :meta (get-in binding [:plan :module :profile]))
        (= :jvm (get-in binding [:plan :module :target]))
        (= #{} (get-in binding [:plan :module :effects]))
        (= #{} (get-in binding [:plan :module :capabilities]))
        (= :safe (get-in binding [:plan :module :safety]))
        (= (:byte-count descriptor) (get-in binding [:plan :source :byte-count]))
        (= expected-plan-id (get-in binding [:plan :plan-id])
           sh06-resolution-expected-plan-id)
        (= (select-keys binding
                        [:plan-semantic-hash :functions-semantic-hash
                         :function-count :function-names-hash
                         :function-shapes-hash :public-function-hashes
                         :public-function-shapes])
           identities)
        (= sh06-resolution-expected-plan-semantic-hash
           (:plan-semantic-hash identities))
        (= sh06-resolution-expected-functions-semantic-hash
           (:functions-semantic-hash identities))
        (= sh06-resolution-expected-function-count
           (:function-count identities))
        (= sh06-resolution-expected-function-names-hash
           (:function-names-hash identities))
        (= sh06-resolution-expected-function-shapes-hash
           (:function-shapes-hash identities))
        (= sh06-resolution-public-function-hashes
           (:public-function-hashes identities))
        (= sh06-resolution-public-function-shapes
           (:public-function-shapes identities)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-resolution-plan-authority-valid?
  [authority]
  (try
    (let [content (:content authority)
          descriptor (:source-descriptor authority)
          proof (:source-object-proof authority)
          expected-id
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
            (dissoc authority :authority-id)])]
      (p15-s23-sh07-b51-validate-content-object-proof!
       content (:content-object-proof authority))
      (p15-s23-sh07-b51-validate-source-object-proof!
       content descriptor proof)
      (boolean
       (and (p15-s23-sh07-b51-exact-keys?
             authority p15-s23-sh07-b51-resolution-plan-authority-keys)
            (= :gravity/sh07-b51-sh06-resolution-plan-authority-v1
               (:schema authority))
            (= [[:authority-id]] (:self-id-omits authority))
            (= expected-id (:authority-id authority))
            (p15-s23-sh07-b51-resolution-plan-binding-valid?
             descriptor proof (:plan-binding authority)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-current-sh06-resolution-binding
  [entry emitter]
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptor (:descriptor entry)
        bytes (:bytes entry)
        text (:text entry)
        content {:commit (:commit state) :tree (:tree state)}]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys?
           entry #{:descriptor :bytes :text
                   :content-object-proof :source-object-proof})
          (= descriptor (nth (:descriptors state) 12 nil))
          (= entry (nth (:entries state) 12 nil))
          (identical? emitter
                      (p15-s23-sh07-b51-session-checked-product
                       :current-emitter))
          (p15-s23-sh07-b51-byte-array? bytes)
          (= text (p15-s23-sh07-b51-strict-utf8
                   bytes :resolution-plan-source-text))
          (= sh06-resolution-expected-source-byte-count
             (:byte-count descriptor) (alength ^bytes bytes))
          (= sh06-resolution-expected-source-content-hash
             (:raw-sha256 descriptor)
             (p15-s23-sh07-b51-raw-sha256 bytes)))
      (p15-s23-sh07-b51-governed-read-fail!
       :sh06-resolution-plan-source {:descriptor descriptor}))
    (p15-s23-sh07-b51-validate-content-object-proof!
     content (:content-object-proof entry))
    (let [proven-bytes
          (p15-s23-sh07-b51-validate-source-object-proof!
           content descriptor (:source-object-proof entry))]
      (when-not (p15-s23-sh07-b51-byte-array-equal? bytes proven-bytes)
        (p15-s23-sh07-b51-governed-read-fail!
         :sh06-resolution-plan-source-proof {})))
    (let [raw-plan
          (p15-s23-stage2-compiler-artifact-plan
           emitter (:path descriptor) text)
          identities (sh06-resolution-plan-identities raw-plan)
          authenticated-plan-base
          (assoc-in (dissoc raw-plan :plan-id)
                    [:source :byte-count] (:byte-count descriptor))
          plan
          (assoc authenticated-plan-base :plan-id
                 (c4-artifact-id
                  (c-backend-canonical-value authenticated-plan-base)))
          binding
          (merge
           {:source-path (:path descriptor)
            :source-byte-count (:byte-count descriptor)
            :source-content-hash (:raw-sha256 descriptor)
            :artifact :gravity/sh06-pinned-resolution-plan-binding
            :status :complete
            :semantic-authority :gravity-source
            :compiled-by :clojure-stage0-seed
            :executed-by :clojure-stage2-generic-rule-runner
            :generic-bridge-residual? true
            :self-hosted? false
            :plan plan}
           identities)
          authority-base
          {:schema :gravity/sh07-b51-sh06-resolution-plan-authority-v1
           :content content
           :content-object-proof (:content-object-proof entry)
           :source-descriptor descriptor
           :source-object-proof (:source-object-proof entry)
           :plan-binding binding
           :self-id-omits [[:authority-id]]}
          authority
          (assoc authority-base :authority-id
                 (p15-s23-sh07-b51-canonical-id
                  [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
                   authority-base]))]
      (when-not (p15-s23-sh07-b51-resolution-plan-authority-valid?
                 authority)
        (p15-s23-sh07-b51-governed-read-fail!
         :sh06-resolution-plan-authority {}))
      authority)))

(defn p15-s23-sh07-b51-build-module-transaction
  [commit tree module-ordinal source c5-preimage resolution-plan-authority]
  (let [descriptor (:source-descriptor source)
        source-path (:path descriptor)
        source-text (:text source)
        project-context
        (p15-s23-sh07-b51-project-context commit tree source-path)
        c2-artifact
        (compiler-c2-reader-source-artifact
         source-path source-text project-context)
        c3-artifact
        (compiler-c3-syntax-source-artifact
         source-path source-text project-context c2-artifact
         sh03-reader-internal-product-authority)
        sh05-artifact
        (sh05-macro-artifact-from-c3-artifact*
         source-path c3-artifact true)
        base-request (sh06-resolution-request source-path sh05-artifact)
        c5-authority
        (p15-s23-sh07-b51-c5-external-binding-authority
         module-ordinal source sh05-artifact base-request c5-preimage)
        sh06-context
        (p15-s23-sh07-b51-sh06-context
         c5-authority resolution-plan-authority)
        resolution-artifact
        (sh06-resolution-artifact-from-sh05-artifact
         source-path sh05-artifact sh06-context)
        resolution-verification
        (sh06-resolution-artifact-verification
         resolution-artifact sh06-context)
        request
        (sh07-core-authenticated-request resolution-artifact sh06-context)
        raw (sh07-core-execute!
             source-path 'sh07-build-core-template [request])
        template (:core-template raw)
        digest-requests (:digest-requests raw)
        template-verification
        (sh07-core-execute!
         source-path 'sh07-verify-core-template
         [request template digest-requests])
        resolved-digests
        (sh07-core-digest-requests source-path digest-requests)
        resolution
        (sh07-core-execute!
         source-path 'sh07-resolve-core-template
         [template resolved-digests])
        resolved (:value resolution)
        bad-resolved (assoc-in resolved [:module :namespace] 'gravity.altered)
        pass-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request resolved digest-requests resolved-digests])
        reject-actual
        (sh07-core-execute!
         source-path 'sh07-verify-core-resolved
         [request bad-resolved digest-requests resolved-digests])
        form-ids (mapv :form-id (:forms request))
        node-form-ids (mapv :form-id (:nodes resolved))]
    (when-not
     (and (= module-ordinal (:module-ordinal descriptor))
          (= source-path (:path descriptor))
          (= (:raw-sha256 descriptor)
             (get-in c2-artifact [:source-unit-record :bytes-hash]))
          (= (:project-root-id project-context)
             (get-in c2-artifact [:source-unit-record :project-root]))
          (= {:path nil :project-root-id (:project-root-id project-context)}
             (get-in c2-artifact [:source-unit-record :project-root-record]))
          (= :passed (:status resolution-verification))
          (= base-request
             (sh06-resolution-request source-path sh05-artifact))
          (= (:authority-id c5-authority)
             (get-in sh06-context
                     [:external-binding-authority :authority-id]))
          (= (:authority-id resolution-plan-authority)
             (get-in sh06-context
                     [:resolution-plan-authority :authority-id]))
          (= source-path (get-in request [:provenance :actual-source-path]))
          (= :accepted (:status raw))
          (= :passed (:status template-verification))
          (= :accepted (:status resolution))
          (= p15-s23-sh07-b51-resolved-pass pass-actual)
          (= p15-s23-sh07-b51-resolved-reject reject-actual)
          (not= 'gravity.altered (get-in resolved [:module :namespace]))
          (empty? (:diagnostics resolved))
          (= (count (:nodes resolved)) (count (:forms request)))
          (= (count form-ids) (count (set form-ids)))
          (= (set form-ids) (set node-form-ids))
          (= (count node-form-ids) (count (set node-form-ids)))
          (<= (count digest-requests) (+ (count (:forms request)) 4)))
      (p15-s23-sh07-b51-governed-read-fail!
       :module-transaction
       {:module-ordinal module-ordinal
        :resolution-status (:status resolution-verification)
        :template-status (:status raw)
        :template-verification (:status template-verification)
        :resolution (:status resolution)}))
    {:module-ordinal module-ordinal
     :source-descriptor descriptor
     :resolution-artifact resolution-artifact
     :c2-reader-artifact c2-artifact
     :authenticated-core-request request
     :resolved-digests resolved-digests
     :module-core (:nodes resolved)
     :template-carrier template
     :generated-digest-carrier digest-requests
     :resolved-core-carrier resolved
     :module-digest-requests digest-requests}))

(defn- p15-s23-sh07-b51-source-authority-product-from-m0-m1
  [descriptor link-manifest]
  {:artifact :gravity/sh07-compiler-source-authority-v2
   :manifest-id (:manifest-id link-manifest)
   :module-source-id (:module-source-id descriptor)
   :logical-source-path (:logical-source-path descriptor)
   :source-sha256 (:source-sha256 descriptor)
   :module (:module descriptor)
   :function-source-hashes
   (into (sorted-map)
         (map (juxt :name :source-hash))
         (:definitions descriptor))})

(defn- p15-s23-sh07-b51-c5-authority-preimage-from-session
  [commit tree descriptors manifest]
  (let [entries (mapv p15-s23-sh07-b51-governed-source-entry
                      (range 3 10))
        content {:commit commit :tree tree}
        content-proof (:content-object-proof (first entries))
        source-descriptors (mapv :descriptor entries)
        module-source-inputs
        (mapv #(select-keys % [:descriptor :source-object-proof]) entries)
        preimage
        {:schema :gravity/sh07-b51-c5-authority-preimage-v1
         :content content
         :content-object-proof content-proof
         :source-descriptors source-descriptors
         :module-source-inputs module-source-inputs
         :module-source-descriptors descriptors
         :link-manifest manifest}]
    (when-not
     (and (= 7 (count entries))
          (every? #(= content-proof (:content-object-proof %)) entries)
          (= source-descriptors
             (subvec (:descriptors
                      (p15-s23-sh07-b51-active-source-session)) 3 10)))
      (p15-s23-sh07-b51-c5-authority-fail!
       :session-preimage-join {}))
    (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
    preimage))

(defn- p15-s23-sh07-b51-replay-returned-source-authority-events!
  [m0s link-manifest resolution-plan-authority source-read-receipt]
  (let [events (:events source-read-receipt)
        source-descriptors (:source-descriptors source-read-receipt)]
    (when-not (and (= 7 (count m0s))
                   (= 13 (count source-descriptors))
                   (= 40 (count events)))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-authority-event-replay-shape
       {:m0-count (count m0s)
        :source-descriptor-count (count source-descriptors)
        :event-count (count events)}))
    (doseq [module-ordinal (range 7)]
      (let [event-ordinal (+ 25 module-ordinal)
            event (nth events event-ordinal)
            expected-coordinate
            (nth p15-s23-sh07-b51-source-event-plan event-ordinal)
            m0 (nth m0s module-ordinal)
            source-descriptor
            (nth source-descriptors (+ 3 module-ordinal))
            product
            (p15-s23-sh07-b51-source-authority-product-from-m0-m1
             m0 link-manifest)
            replayed-id
            (p15-s23-sh07-b51-source-event-product-id event product)]
        (when-not
         (and (= expected-coordinate
                 (select-keys event
                              [:ordinal :domain :source-ordinal
                               :module-ordinal :consumer]))
              (= (:module-ordinal m0) module-ordinal)
              (= (:logical-source-path m0) (:path source-descriptor))
              (= (:source-sha256 m0) (:raw-sha256 source-descriptor)
                 (:source-raw-sha256 event))
              (= replayed-id (:product-id event)))
          (p15-s23-sh07-b51-governed-read-fail!
           :source-authority-event-replay
           {:module-ordinal module-ordinal
            :event-ordinal event-ordinal
            :expected-product-id replayed-id
            :actual-product-id (:product-id event)}))))
    (let [event (nth events 39)
          expected-coordinate (nth p15-s23-sh07-b51-source-event-plan 39)
          descriptor (nth source-descriptors 12)
          replayed-id
          (p15-s23-sh07-b51-source-event-product-id
           event resolution-plan-authority)]
      (when-not
       (and (p15-s23-sh07-b51-resolution-plan-authority-valid?
             resolution-plan-authority)
            (= expected-coordinate
               (select-keys event
                            [:ordinal :domain :source-ordinal
                             :module-ordinal :consumer]))
            (= (:raw-sha256 descriptor) (:source-raw-sha256 event))
            (= replayed-id (:product-id event)))
        (p15-s23-sh07-b51-governed-read-fail!
         :resolution-plan-authority-event-replay
         {:expected-product-id replayed-id
          :actual-product-id (:product-id event)})))
    true))

(def ^:private p15-s23-sh07-b51-source-semantic-carrier-keys
  #{:source-descriptors :source-events :l1-document :l1-source-units
    :l1-grammar-change :c2-snapshot :c2-units :c2-source-map
    :c2-diagnostics :c2-literal-records :c2-reader-extensions})

(def ^:private p15-s23-sh07-b51-v9-builder-receipted-product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id
    :source-forms :module-transactions :source-read-receipt
    :sh06-resolution-plan-authority :source-semantic-carrier})

(def ^:private p15-s23-sh07-b51-c2-unit-keys
  #{:ordinal :path :extension :project-relative-path :encoding :source-id
    :bytes-hash :project-root :reader-options :features :source-kind
    :tokens :forms :source-map :diagnostics :literal-records
    :reader-extensions})

(def ^:private p15-s23-sh07-b51-l1-unit-keys
  #{:ordinal :descriptor :source-order :syntax})

(defn- p15-s23-sh07-b51-semantic-member-root
  [kind entries]
  (p15-s23-sh07-b51-canonical-id
   [:gravity/sh07-b51-semantic-witness-member-root-v1
    {:kind kind :entries entries}]))

(defn- p15-s23-sh07-b51-path-neutral-span [span]
  (if (map? span) (dissoc span :source) span))

(defn- p15-s23-sh07-b51-path-neutral-tokens [tokens]
  (mapv #(-> %
             (dissoc :source-path)
             (update :span p15-s23-sh07-b51-path-neutral-span))
        tokens))

(defn- p15-s23-sh07-b51-path-neutral-forms [forms]
  (mapv
   (fn [form]
     (-> form
         (dissoc :source-path)
         (update :span p15-s23-sh07-b51-path-neutral-span)
         (update :surface-span p15-s23-sh07-b51-path-neutral-span)
         (update :origin #(when % (dissoc % :source-path)))
         (update :generated-origin
                 #(mapv (fn [origin]
                          (update origin :from
                                  p15-s23-sh07-b51-path-neutral-span))
                        (or % [])))))
   forms))

(defn- p15-s23-sh07-b51-path-neutral-seeds [seeds]
  (mapv
   (fn [seed]
     (cond-> (update seed :span p15-s23-sh07-b51-path-neutral-span)
       (contains? seed :generated-origin)
       (update :generated-origin
               #(mapv (fn [origin]
                        (cond-> origin
                          (contains? origin :from)
                          (update :from
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))))
   seeds))

(defn- p15-s23-sh07-b51-path-neutral-extensions [extensions]
  (let [semantic-span #(if (map? %) (dissoc % :source :file) %)]
    (mapv
     (fn [invocation]
       (cond-> (dissoc invocation :source-path)
         (contains? invocation :span) (update :span semantic-span)
         (contains? invocation :invocations)
         (update :invocations
                 #(mapv (fn [record]
                          (cond-> record
                            (contains? record :span)
                            (update :span semantic-span)))
                        %))))
     extensions)))

(defn- p15-s23-sh07-b51-path-neutral-diagnostics [diagnostics]
  (mapv
   (fn [diagnostic]
     (cond-> diagnostic
       (contains? diagnostic :source-span)
       (update :source-span p15-s23-sh07-b51-path-neutral-span)
       (get-in diagnostic [:primary :span])
       (update-in [:primary :span] p15-s23-sh07-b51-path-neutral-span)
       (contains? diagnostic :related)
       (update :related
               #(mapv (fn [related]
                        (cond-> related
                          (contains? related :span)
                          (update :span
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))
       (contains? diagnostic :origin-chain)
       (update :origin-chain
               #(mapv (fn [origin]
                        (cond-> (dissoc origin :path)
                          (contains? origin :span)
                          (update :span
                                  p15-s23-sh07-b51-path-neutral-span)))
                      %))))
   diagnostics))

(defn- p15-s23-sh07-b51-path-neutral-literal
  [literal]
  (if (contains? literal :span)
    (update literal :span p15-s23-sh07-b51-path-neutral-span)
    literal))

(defn- p15-s23-sh07-b51-unit-descriptor
  [descriptor artifact]
  (let [source-unit (:source-unit-record artifact)
        identity-inputs (:identity-inputs source-unit)
        projected
        {:path (:path descriptor)
         :extension (:extension source-unit)
         :project-relative-path (:project-relative-path source-unit)
         :encoding (:encoding identity-inputs)
         :source-id (:source-id source-unit)
         :bytes-hash (:bytes-hash source-unit)
         :project-root (:project-root source-unit)
         :reader-options (:reader-options source-unit)
         :features (:enabled-features source-unit)
         :source-kind (:source-kind source-unit)}]
    (when-not
     (and (= (:path descriptor) (:project-relative-path source-unit)
             (:project-relative-path identity-inputs))
          (= (:raw-sha256 descriptor) (:bytes-hash source-unit))
          (= (:project-root source-unit) (:project-root-id identity-inputs))
          (= {:path nil :project-root-id (:project-root source-unit)}
             (:project-root-record source-unit)))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-semantic-carrier-unit-descriptor
       {:ordinal (:ordinal descriptor) :path (:path descriptor)}))
    projected))

(defn- p15-s23-sh07-b51-c2-unit
  [descriptor artifact]
  (let [ordinal (:ordinal descriptor)
        token-entries
        (p15-s23-sh07-b51-path-neutral-tokens (:token-stream artifact))
        form-entries
        (p15-s23-sh07-b51-path-neutral-forms (:form-tree artifact))
        source-map-entries
        (mapv #(update % :span p15-s23-sh07-b51-path-neutral-span)
              (:reader-source-map artifact))
        diagnostic-entries
        (p15-s23-sh07-b51-path-neutral-diagnostics
         (:reader-diagnostics artifact))
        literal-records
        (mapv p15-s23-sh07-b51-path-neutral-literal
              (:literal-decoding-records artifact))
        reader-extensions
        (p15-s23-sh07-b51-path-neutral-extensions
         (:reader-extension-invocation-records artifact))]
    (merge
     {:ordinal ordinal}
     (p15-s23-sh07-b51-unit-descriptor descriptor artifact)
     {:tokens {:entries token-entries
               :root (p15-s23-sh07-b51-semantic-member-root
                      :c2-tokens token-entries)}
      :forms {:entries form-entries
              :root (p15-s23-sh07-b51-semantic-member-root
                     :c2-forms form-entries)}
      :source-map {:entries source-map-entries
                   :root (p15-s23-sh07-b51-semantic-member-root
                          :c2-source-map source-map-entries)}
      :diagnostics {:entries diagnostic-entries
                    :root (p15-s23-sh07-b51-semantic-member-root
                           :c2-diagnostics diagnostic-entries)}
      :literal-records literal-records
      :reader-extensions reader-extensions})))

(defn- p15-s23-sh07-b51-l1-unit
  [descriptor artifact c2-unit]
  (let [entries (p15-s23-sh07-b51-path-neutral-seeds
                 (:syntax-seed-stream artifact))]
    {:ordinal (:ordinal descriptor)
     :descriptor (select-keys
                  c2-unit
                  [:path :extension :project-relative-path :encoding
                   :source-id :bytes-hash :project-root :reader-options
                   :features :source-kind])
     :source-order (mapv :form-id (:syntax-seed-stream artifact))
     :syntax {:entries entries
              :root (p15-s23-sh07-b51-semantic-member-root
                     :l1-syntax entries)}}))

(defn- p15-s23-sh07-b51-source-semantic-carrier-from-session!
  [commit tree module-transactions]
  (let [state (p15-s23-sh07-b51-active-source-session)
        descriptors (:descriptors state)
        events @(:events state)
        artifacts
        (mapv
         (fn [ordinal]
           (if (<= 3 ordinal 9)
             (:c2-reader-artifact
              (nth module-transactions (- ordinal 3)))
             (let [entry (nth (:entries state) ordinal)
                   descriptor (:descriptor entry)]
               (compiler-c2-reader-source-artifact
                (:path descriptor) (:text entry)
                (p15-s23-sh07-b51-project-context
                 commit tree (:path descriptor))))))
         (range 2 12))
        c2-units (mapv p15-s23-sh07-b51-c2-unit
                       (subvec descriptors 2 12) artifacts)
        l1-units (mapv p15-s23-sh07-b51-l1-unit
                       (subvec descriptors 2 12) artifacts c2-units)
        source-map-entries (vec (mapcat #(get-in % [:source-map :entries])
                                        c2-units))
        diagnostic-entries
        (vec (mapcat #(get-in % [:diagnostics :entries]) c2-units))
        carrier
        {:source-descriptors descriptors
         :source-events events
         :l1-document (first descriptors)
         :l1-source-units l1-units
         :l1-grammar-change :none
         :c2-snapshot
         {:commit commit :tree tree
          :source-descriptors-root (:descriptors-root state)
          :event-root
          (p15-s23-sh07-b51-canonical-id
           [:gravity/sh07-b51-source-event-root-v1 events])}
         :c2-units c2-units
         :c2-source-map
         {:entries source-map-entries
          :root (p15-s23-sh07-b51-semantic-member-root
                 :c2-source-map source-map-entries)}
         :c2-diagnostics
         {:entries diagnostic-entries
          :root (p15-s23-sh07-b51-semantic-member-root
                 :c2-diagnostics diagnostic-entries)}
         :c2-literal-records
         (vec (mapcat :literal-records c2-units))
         :c2-reader-extensions
         (vec (mapcat :reader-extensions c2-units))}]
    (when-not (= 40 @(:cursor state) (count events))
      (p15-s23-sh07-b51-governed-read-fail!
       :source-semantic-carrier-event-census
       {:cursor @(:cursor state) :event-count (count events)}))
    carrier))

(defn p15-s23-sh07-b51-source-semantic-carrier-valid?
  "Validate the complete receipt-free E3 source carrier against one finalized
  checker-local source receipt and its seven authenticated transactions."
  [content source-read-receipt module-transactions carrier]
  (try
    (let [descriptors (:source-descriptors source-read-receipt)
          events (:events source-read-receipt)
          c2-units (:c2-units carrier)
          l1-units (:l1-source-units carrier)
          source-map-entries
          (vec (mapcat #(get-in % [:source-map :entries]) c2-units))
          diagnostic-entries
          (vec (mapcat #(get-in % [:diagnostics :entries]) c2-units))]
      (boolean
       (and
        (p15-s23-sh07-b51-exact-keys?
         carrier p15-s23-sh07-b51-source-semantic-carrier-keys)
        (= 13 (count descriptors))
        (= 40 (count events))
        (= descriptors (:source-descriptors carrier))
        (= events (:source-events carrier))
        (= (first descriptors) (:l1-document carrier))
        (= :none (:l1-grammar-change carrier))
        (= 10 (count c2-units) (count l1-units))
        (= (vec (range 2 12)) (mapv :ordinal c2-units)
           (mapv :ordinal l1-units))
        (every? #(p15-s23-sh07-b51-exact-keys?
                  % p15-s23-sh07-b51-c2-unit-keys)
                c2-units)
        (every? #(p15-s23-sh07-b51-exact-keys?
                  % p15-s23-sh07-b51-l1-unit-keys)
                l1-units)
        (= {:commit (:commit content) :tree (:tree content)
            :source-descriptors-root
            (:source-descriptors-root source-read-receipt)
            :event-root (:event-root source-read-receipt)}
           (:c2-snapshot carrier))
        (= (:source-descriptors-root source-read-receipt)
           (p15-s23-sh07-b51-canonical-id
            [:gravity/sh07-b51-source-descriptors-root-v1 descriptors]))
        (= (:event-root source-read-receipt)
           (p15-s23-sh07-b51-canonical-id
            [:gravity/sh07-b51-source-event-root-v1 events]))
        (= (vec (repeat 10 true))
           (mapv
          (fn [descriptor c2-unit l1-unit]
            (let [unit-descriptor
                  (select-keys
                   c2-unit
                   [:path :extension :project-relative-path :encoding
                    :source-id :bytes-hash :project-root :reader-options
                    :features :source-kind])]
              (and (= (:path descriptor) (:path c2-unit)
                      (:project-relative-path c2-unit))
                   (= (:raw-sha256 descriptor) (:bytes-hash c2-unit))
                   (= (:project-root-id
                       (p15-s23-sh07-b51-project-context
                        (:commit content) (:tree content) (:path descriptor)))
                      (:project-root c2-unit))
                   (= unit-descriptor (:descriptor l1-unit))
                   (= (:source-order l1-unit)
                      (mapv :form-id (get-in l1-unit [:syntax :entries])))
                   (= (get-in l1-unit [:syntax :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :l1-syntax (get-in l1-unit [:syntax :entries])))
                   (= (get-in c2-unit [:tokens :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-tokens (get-in c2-unit [:tokens :entries])))
                   (= (get-in c2-unit [:forms :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-forms (get-in c2-unit [:forms :entries])))
                   (= (get-in c2-unit [:source-map :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-source-map
                       (get-in c2-unit [:source-map :entries])))
                   (= (get-in c2-unit [:diagnostics :root])
                      (p15-s23-sh07-b51-semantic-member-root
                       :c2-diagnostics
                       (get-in c2-unit [:diagnostics :entries]))))))
            (subvec descriptors 2 12) c2-units l1-units))
        (= source-map-entries
           (get-in carrier [:c2-source-map :entries]))
        (= (p15-s23-sh07-b51-semantic-member-root
            :c2-source-map source-map-entries)
           (get-in carrier [:c2-source-map :root]))
        (= diagnostic-entries
           (get-in carrier [:c2-diagnostics :entries]))
        (= (p15-s23-sh07-b51-semantic-member-root
            :c2-diagnostics diagnostic-entries)
           (get-in carrier [:c2-diagnostics :root]))
        (= (vec (mapcat :literal-records c2-units))
           (:c2-literal-records carrier))
        (= (vec (mapcat :reader-extensions c2-units))
           (:c2-reader-extensions carrier))
        (= 7 (count module-transactions))
        (= (vec (repeat 7 true))
           (mapv
          (fn [module-ordinal transaction]
            (= (nth c2-units (inc module-ordinal))
               (p15-s23-sh07-b51-c2-unit
                (nth descriptors (+ 3 module-ordinal))
                (:c2-reader-artifact transaction))))
            (range 7) module-transactions)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-build-receipted-bundle-core-products
  [commit tree]
  (binding [*p15-s23-sh07-b51-v9-receipted-bundle-construction* true]
    (let [{:keys [token state] :as session}
          (p15-s23-sh07-b51-open-source-session commit tree)]
      (binding [*p15-s23-sh07-b51-governed-source-session*
                {:token token :state state}]
      ;; These three products are semantic event values.  Their bytes remain
      ;; solely in the private session entries and never cross the wrapper.
      (p15-s23-sh07-b51-governed-source-event!
       :l1-contract 0 nil 'gravity.bootstrap/p15-s23-sh07-b51-l1-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :l2-contract 1 nil 'gravity.bootstrap/p15-s23-sh07-b51-l2-contract
       (fn [{:keys [text]}] text))
      (p15-s23-sh07-b51-governed-source-event!
       :checked-core-revision 2 nil
       'gravity.bootstrap/p15-s23-sh07-b51-checked-core-contract
       (fn [{:keys [descriptor text] :as entry}]
         (p15-s23-sh07-b51-cache-session-product!
          :checked-core-entry entry)
         (vec (read-forms (:path descriptor) text))))
      (doseq [module-ordinal (range 7)]
        (p15-s23-sh07-b51-validate-module-input! module-ordinal))
      (let [census (p15-s23-sh07-b51-derive-callsite-census)
            authority
            (p15-s23-sh07-b51-build-link-authority-from-census census)
            authority
            (p15-s23-sh07-b51-cache-session-product!
             :link-authority authority)
            emitter (p15-s23-sh07-b51-current-emitter)
            compilation-emitter
            (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
            sources (:sources authority)
            descriptors (:module-source-descriptors authority)
            manifest (:link-manifest authority)
            source-authorities
            (mapv
             (fn [descriptor source]
               (let [module
                     (assoc (:module source)
                            :p15-s23-sh07-module-source-descriptor descriptor
                            :p15-s23-sh07-link-manifest manifest)]
                 (p15-s23-sh07-compiler-source-authority
                  (:path source) (:text source) module (:table source))))
             descriptors sources)
            compiler-bindings
            (mapv
             (fn [source]
               (p15-s23-stage2-compiler-artifact-binding!
                compilation-emitter (:path source)
                (:target (:module source))))
             sources)
            resolution-plan-authority
            (p15-s23-sh07-b51-governed-source-event!
             :sh06-resolution-binding 12 nil
             'gravity.bootstrap/p15-s23-sh07-b51-current-sh06-resolution-binding
             (fn [entry]
               (p15-s23-sh07-b51-current-sh06-resolution-binding
                entry emitter)))
            c5-preimage
            (p15-s23-sh07-b51-c5-authority-preimage-from-session
             commit tree descriptors manifest)
            plan-sources
            (mapv #(assoc %1
                          :source-authority %2
                          :compiler-artifact-binding %3)
                  sources source-authorities compiler-bindings)
            _ (p15-s23-sh07-b51-cache-session-product!
               :plan-sources plan-sources)
            products
            (p15-s23-sh07-b51-build-execution-bundle emitter)
            checked-core-binding
            (p15-s23-sh07-b51-session-checked-core-binding emitter)
            checked-core-binding-var
            (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)
            module-transactions
            (with-redefs-fn
              {checked-core-binding-var (delay checked-core-binding)}
              (fn []
                (mapv
                 (fn [module-ordinal]
                   (p15-s23-sh07-b51-build-module-transaction
                    commit tree module-ordinal
                    (p15-s23-sh07-b51-session-checked-product
                     [:module-input module-ordinal])
                    c5-preimage resolution-plan-authority))
                 (range 7))))
            source-forms (mapv (comp vec :forms :module) sources)
            source-semantic-carrier
            (p15-s23-sh07-b51-source-semantic-carrier-from-session!
             commit tree module-transactions)
            receipt (p15-s23-sh07-b51-finalize-source-session!)
            returned-products
            {:m0s (:module-source-descriptors products)
             :m1 (:link-manifest products)
             :m2s (:module-plans products)
             :bundle-core (:execution-bundle-core products)
             :execution-bundle-core-id (:execution-bundle-core-id products)
             :source-forms source-forms
             :module-transactions module-transactions
             :sh06-resolution-plan-authority resolution-plan-authority
             :source-semantic-carrier source-semantic-carrier
             :source-read-receipt receipt}]
        (when-not
         (p15-s23-sh07-b51-exact-keys?
          returned-products
          p15-s23-sh07-b51-v9-builder-receipted-product-keys)
          (p15-s23-sh07-b51-governed-read-fail!
           :receipted-product-shape {}))
        (p15-s23-sh07-b51-replay-returned-source-authority-events!
         (:m0s returned-products) (:m1 returned-products)
         resolution-plan-authority receipt)
        (when-not
         (p15-s23-sh07-b51-source-semantic-carrier-valid?
          {:commit commit :tree tree} receipt module-transactions
          source-semantic-carrier)
          (p15-s23-sh07-b51-governed-read-fail!
           :source-semantic-carrier {}))
        returned-products)))))

(def ^:private p15-s23-sh07-b51-sh06-authority-carrier-base-keys
  #{:content :source-descriptors :bundle-core-id
    :resolution-artifact-paths :resolution-artifacts
    :resolution-verifications :semantic-projection-paths
    :semantic-projection-ids :module-source-descriptors :link-manifest
    :link-authority-event-product-ids :link-authority-id
    :expected-edge-census :resolution-plan-authority
    :external-binding-authorities})

(defn p15-s23-sh07-b51-build-sh06-authority-carrier
  [commit tree products]
  (let [expected-product-keys p15-s23-sh07-b51-v9-builder-receipted-product-keys
        receipt (:source-read-receipt products)
        source-descriptors (:source-descriptors receipt)
        module-descriptors (:m0s products)
        manifest (:m1 products)
        transactions (:module-transactions products)
        resolution-plan-authority (:sh06-resolution-plan-authority products)
        source-semantic-carrier (:source-semantic-carrier products)
        content {:commit commit :tree tree}
        entries
        (mapv
         (fn [module-ordinal]
           (p15-s23-sh07-b51-acquire-source-entry
            commit tree (nth p15-s23-sh07-b51-source-specs
                             (+ 3 module-ordinal))))
         (range 7))
        sources
        (mapv
         (fn [module-ordinal entry]
           (p15-s23-sh07-b51-module-input-record
            (p15-s23-sh07-b51-module-spec module-ordinal) entry))
         (range 7) entries)
        preimage
        {:schema :gravity/sh07-b51-c5-authority-preimage-v1
         :content content
         :content-object-proof (:content-object-proof (first entries))
         :source-descriptors (mapv :descriptor entries)
         :module-source-inputs
         (mapv #(select-keys % [:descriptor :source-object-proof]) entries)
         :module-source-descriptors module-descriptors
         :link-manifest manifest}
        _ (p15-s23-sh07-b51-validate-c5-authority-preimage! preimage)
        c5-authorities
        (mapv
         (fn [module-ordinal source transaction]
           (let [resolution (:resolution-artifact transaction)
                 sh05 (:sh05-macro-artifact resolution)
                 base (sh06-resolution-request (:path source) sh05)]
             (p15-s23-sh07-b51-c5-external-binding-authority
              module-ordinal source sh05 base preimage)))
         (range 7) sources transactions)
        contexts
        (mapv #(p15-s23-sh07-b51-sh06-context
                % resolution-plan-authority)
              c5-authorities)
        resolution-artifacts (mapv :resolution-artifact transactions)
        resolution-artifact-ids (mapv :artifact-id resolution-artifacts)
        resolution-verifications
        (mapv sh06-resolution-artifact-verification
              resolution-artifacts contexts)
        replayed-requests
        (mapv sh07-core-authenticated-request
              resolution-artifacts contexts)
        semantic-projection-ids
        (mapv #(get-in % [:authenticated-core-request :lineage
                          :sh06-semantic-projection-id])
              transactions)
        resolution-artifact-paths
        (mapv #(vector :resolution-artifacts %) (range 7))
        semantic-projection-paths
        (mapv #(vector :semantic-projection-ids %) (range 7))
        link-authority-event-product-ids
        (mapv :product-id (subvec (:events receipt) 17 24))
        link-authority-id
        (p15-s23-sh07-b51-canonical-id
         [:gravity/sh07-b51-sh06-link-authority-id-e2-v1
          {:resolution-artifact-ids resolution-artifact-ids
           :semantic-projection-ids semantic-projection-ids
           :event-product-ids link-authority-event-product-ids
           :manifest-id (:manifest-id manifest)
           :resolution-plan-authority resolution-plan-authority
           :external-binding-authorities c5-authorities}])
        base
        {:content content
         :source-descriptors (subvec source-descriptors 3 10)
         :bundle-core-id (:execution-bundle-core-id products)
         :resolution-artifact-paths resolution-artifact-paths
         :resolution-artifacts resolution-artifacts
         :resolution-verifications resolution-verifications
         :semantic-projection-paths semantic-projection-paths
         :semantic-projection-ids semantic-projection-ids
         :module-source-descriptors module-descriptors
         :link-manifest manifest
         :link-authority-event-product-ids link-authority-event-product-ids
         :link-authority-id link-authority-id
         :expected-edge-census (:edges manifest)
         :resolution-plan-authority resolution-plan-authority
         :external-binding-authorities c5-authorities}
        carrier
        (assoc base :carrier-id
               (p15-s23-sh07-b51-canonical-id
                [:gravity/sh07-b51-sh06-authority-carrier-id-e2-v1 base]))]
    (when-not
     (and (p15-s23-sh07-b51-exact-keys? products expected-product-keys)
          (= content (select-keys receipt [:commit :tree]))
          (= 13 (count source-descriptors))
          (= {:acquire-count 13 :decode-count 13
              :event-count 40 :finalize-count 1}
             (:counters receipt))
          (= (:source-descriptors preimage) (subvec source-descriptors 3 10))
          (= content (:content resolution-plan-authority))
          (p15-s23-sh07-b51-resolution-plan-authority-valid?
           resolution-plan-authority)
          (p15-s23-sh07-b51-source-semantic-carrier-valid?
           content receipt transactions source-semantic-carrier)
          (= 7 (count module-descriptors) (count transactions)
             (count c5-authorities) (count resolution-artifacts)
             (count resolution-verifications)
             (count semantic-projection-ids))
          (= (:module-source-ids manifest)
             (mapv :module-source-id module-descriptors))
          (= (:manifest-id manifest) (:manifest-id (:bundle-core products)))
          (= (:execution-bundle-core-id products)
             (p15-s23-sh07-b51-canonical-id (:bundle-core products)))
          (= replayed-requests
             (mapv :authenticated-core-request transactions))
          (every? #(= :passed (:status %)) resolution-verifications)
          (every? p15-s23-sh07-b51-digest-id? semantic-projection-ids)
          (p15-s23-sh07-b51-exact-keys?
           base p15-s23-sh07-b51-sh06-authority-carrier-base-keys)
          (= (conj p15-s23-sh07-b51-sh06-authority-carrier-base-keys
                   :carrier-id)
             (set (keys carrier))))
      (p15-s23-sh07-b51-c5-authority-fail!
       :sh06-authority-carrier-join
       {:transaction-count (count transactions)
        :source-descriptor-count (count source-descriptors)}))
    carrier))

(defn p15-s23-sh07-b51-sh06-authority-carrier-valid?
  [commit tree products carrier]
  (try
    (= carrier
       (p15-s23-sh07-b51-build-sh06-authority-carrier
        commit tree products))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-runtime-context [linked-products runtime]
  ;; Historical host compatibility surface.  It deliberately cannot attach a
  ;; runtime token: Attempt32 admits invocation only through bounded carrier-v4
  ;; preflight, local authority, equality/joins, and root/route/arity checks.
  (let [_ linked-products]
    (p15-s23-sh07-b51-unadmitted-runtime-context runtime)))

(defn p15-s23-sh07-b51-runtime-augmented-plan-authorized?
  [runtime plan]
  (try
    (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
          plans (:module-plans context)]
      (boolean
       (and (map? runtime) (map? plan)
            context
            (some #(identical? plan %) plans))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execute-public-root
  [root-ordinal args runtime]
  (let [_ [root-ordinal args runtime]]
    (throw
     (ex-info "Attempt32 public roots require linked carrier-v4 admission"
              {:id "L2-UNKNOWN-CORE-FORM"
               :reason :unauthenticated-b51-linked-carrier
               :result-committed? false
               :output-committed? false}))))

(defn- p15-s23-sh07-b51-plan-valid-structure?
  [descriptor manifest plan]
  (let [plan-functions (:functions plan)
        descriptor-functions (:definitions descriptor)
        exports (set (:exports descriptor))]
  (and (p15-s23-sh07-b51-module-source-valid? descriptor)
       (map? plan-functions)
       (= (set (map :name descriptor-functions))
          (set (keys plan-functions)))
       (every? (fn [row]
                 (let [definition (get plan-functions (:name row))]
                   (and (= (:arity row) (:arity definition))
                        (= (:source-hash row)
                           (p15-s23-sh07-source-definition-hash definition))
                        (= (case (:visibility row)
                             :public :public
                             :private (if (empty? exports)
                                        :stage2-local
                                        :private)
                             ::invalid)
                           (get-in definition [:binding :visibility])))))
               descriptor-functions)
       (= (:module-source-id descriptor)
          (:sh07-b51-module-source-id plan))
       (= (:manifest-id manifest) (:sh07-b51-link-manifest-id plan))
       (= (:module descriptor) (get-in plan [:module :module]))
       (= (:source-sha256 descriptor) (get-in plan [:source :sha256]))
       (= (:plan-id plan)
          (c4-artifact-id
           (c-backend-canonical-value
            (-> plan
                (dissoc :plan-id)
                (update :source dissoc :path)
                (update :module dissoc :source-path))))))))

(defn- p15-s23-sh07-b51-plan-external-links [plan]
  (vec
   (for [[caller definition] (:functions plan)
         instruction (tree-seq coll? seq (:instructions definition))
         :when (and (map? instruction) (= :function-call (:op instruction))
                    (contains? instruction :external-link))]
     (let [ordinal (get-in instruction [:external-link :edge-ordinal])]
       {:caller caller
        :callee (:function instruction)
        :arity (count (:args instruction))
        :edge-ordinal ordinal
        :external-link (:external-link instruction)}))))

(defn- p15-s23-sh07-b51-plan-links-valid-structure?
  [descriptor manifest plan]
  (let [expected
        (filter #(= (:module-source-id descriptor)
                    (:caller-module-source-id %)) (:edges manifest))
        edge-by-ordinal (into {} (map (juxt :ordinal identity)) expected)
        actual
        (mapv (fn [row]
                (assoc row :callsite-path
                       (:callsite-path
                        (get edge-by-ordinal (:edge-ordinal row)))))
              (p15-s23-sh07-b51-plan-external-links plan))]
    (and (= (count expected) (count actual))
         (= (set (map (fn [row]
                        [(:caller-function row) (:callee-function row)
                         (:arity row) (:ordinal row)
                         (:callsite-path row)]) expected))
            (set (map (fn [row]
                        [(:caller row) (:callee row) (:arity row)
                         (:edge-ordinal row) (:callsite-path row)]) actual)))
         (every? #(= {:artifact :gravity/sh07-b51-external-call-v1
                      :manifest-id (:manifest-id manifest)
                      :edge-ordinal
                   (get-in % [:external-link :edge-ordinal])}
                     (:external-link %)) actual))))

(defn- p15-s23-sh07-b51-execution-bundle-core-valid*
  [descriptors manifest plans bundle-core bundle-core-id]
  (let [plans-by-id (into {} (map (juxt :plan-id identity)) plans)
        edge-by-ordinal (into {} (map (juxt :ordinal identity))
                              (:edges manifest))
        module-plans (:module-plans bundle-core)
        routes (:routes bundle-core)
        roots (:public-roots bundle-core)]
    (and (p15-s23-sh07-b51-link-manifest-valid? descriptors manifest)
         (= 7 (count plans))
         (every? (fn [descriptor]
                   (let [plan (nth plans (:module-ordinal descriptor))]
                     (and (p15-s23-sh07-b51-plan-valid-structure?
                           descriptor manifest plan)
                          (p15-s23-sh07-b51-plan-links-valid-structure?
                           descriptor manifest plan))))
                 descriptors)
         (p15-s23-sh07-b51-exact-keys?
          bundle-core p15-s23-sh07-b51-bundle-core-keys)
         (= :gravity/sh07-b51-execution-bundle-core-id-v1
            (:domain bundle-core))
         (= 1 (:schema-version bundle-core))
         (= (:manifest-id manifest) (:manifest-id bundle-core))
         (= (vec (range 7)) (mapv :module-ordinal module-plans))
         (= (mapv (fn [descriptor plan]
                    {:module-ordinal (:module-ordinal descriptor)
                     :module-source-id (:module-source-id descriptor)
                     :plan-id (:plan-id plan)})
                  descriptors plans)
            module-plans)
         (= (vec (range (count routes))) (mapv :edge-ordinal routes))
         (= (count routes) (count (:edges manifest)))
         (every?
          (fn [route]
            (and (= #{:edge-ordinal :caller-plan-id :callee-plan-id}
                    (set (keys route)))
                 (let [edge (get edge-by-ordinal (:edge-ordinal route))
                       caller-plan (get plans-by-id (:caller-plan-id route))
                       callee-plan (get plans-by-id (:callee-plan-id route))]
                   (and edge caller-plan callee-plan
                        (= (:caller-module-source-id edge)
                           (:sh07-b51-module-source-id caller-plan))
                        (= (:callee-module-source-id edge)
                           (:sh07-b51-module-source-id callee-plan))))))
          routes)
         (= 8 (count roots))
         (= (vec (range 1 9)) (mapv :root-ordinal roots))
         (every?
          (fn [expected]
            (let [root (nth roots (dec (:root-ordinal expected)))
                  descriptor (nth descriptors (:module-ordinal expected))
                  plan (nth plans (:module-ordinal expected))
                  definition (get-in plan [:functions (:function expected)])]
              (and (= #{:root-ordinal :function :arity :plan-id}
                      (set (keys root)))
                   (= (select-keys expected [:root-ordinal :function :arity])
                      (select-keys root [:root-ordinal :function :arity]))
                   (= (:plan-id plan) (:plan-id root))
                   (= (:module-source-id descriptor)
                      (:sh07-b51-module-source-id plan))
                   (= (:arity expected) (:arity definition)))))
          p15-s23-sh07-b51-public-root-specs)
         (= bundle-core-id
            (p15-s23-sh07-b51-canonical-id bundle-core)))))

(defn- p15-s23-sh07-b51-execution-bundle-valid*
  [descriptors manifest plans bundle]
  (let [bundle-core
        (select-keys bundle
                     [:manifest-id :module-plans :routes :public-roots])
        bundle-core
        (assoc bundle-core
               :domain :gravity/sh07-b51-execution-bundle-core-id-v1
               :schema-version 1)
        certificate-ids (:compile-census-certificate-ids bundle)]
    (and (p15-s23-sh07-b51-exact-keys? bundle p15-s23-sh07-b51-m3-keys)
         (= :gravity/sh07-b51-execution-bundle-v2 (:artifact bundle))
         (= 2 (:schema-version bundle))
         (vector? certificate-ids)
         (= 2 (count certificate-ids))
         (every? p15-s23-sh07-b51-digest-id? certificate-ids)
         (= 2 (count (set certificate-ids)))
         (p15-s23-sh07-b51-execution-bundle-core-valid*
          descriptors manifest plans bundle-core (:bundle-core-id bundle))
         (= (:bundle-id bundle)
            (p15-s23-sh07-b51-canonical-id
             {:domain :gravity/sh07-b51-execution-bundle-id-v2
              :schema-version 1
              :bundle (dissoc bundle :bundle-id)})))))

(defn- p15-s23-sh07-b51-execution-bundle-core-valid-structure?
  [descriptors manifest plans bundle-core bundle-core-id]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-core-valid*
              descriptors manifest plans bundle-core bundle-core-id))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-execution-bundle-valid-structure?
  [descriptors manifest plans bundle]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-valid*
              descriptors manifest plans bundle))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-build-execution-bundle-v2
  [products certificate-ids]
  (let [bundle-core (:execution-bundle-core products)
        base
        {:artifact :gravity/sh07-b51-execution-bundle-v2
         :schema-version 2
         :manifest-id (:manifest-id bundle-core)
         :module-plans (:module-plans bundle-core)
         :routes (:routes bundle-core)
         :public-roots (:public-roots bundle-core)
         :bundle-core-id (:execution-bundle-core-id products)
         :compile-census-certificate-ids certificate-ids}
        bundle
        (assoc base :bundle-id
               (p15-s23-sh07-b51-canonical-id
                {:domain :gravity/sh07-b51-execution-bundle-id-v2
                 :schema-version 1
                 :bundle base}))]
    (when-not (p15-s23-sh07-b51-execution-bundle-valid-structure?
               (:module-source-descriptors products)
               (:link-manifest products) (:module-plans products) bundle)
      (throw (ex-info "invalid SH-07 B51 M3-v2 construction"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    bundle))

(defn p15-s23-sh07-b51-current-bundle-core-products []
  (p15-s23-sh07-b51-build-execution-bundle))

(defn p15-s23-sh07-b51-plan-valid? [descriptor manifest plan]
  (try
    (let [products (p15-s23-sh07-b51-current-bundle-core-products)
          ordinal (:module-ordinal descriptor)]
      (and (integer? ordinal) (<= 0 ordinal 6)
           (= descriptor
              (nth (:module-source-descriptors products) ordinal nil))
           (= manifest (:link-manifest products))
           (= plan (nth (:module-plans products) ordinal nil))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-plan-links-valid? [descriptor manifest plan]
  (and (p15-s23-sh07-b51-plan-valid? descriptor manifest plan)
       (p15-s23-sh07-b51-plan-links-valid-structure?
        descriptor manifest plan)))

(defn p15-s23-sh07-b51-execution-bundle-valid?
  [descriptors manifest plans bundle]
  (try
    (let [products (p15-s23-sh07-b51-current-authoritative-linked-products-v4)]
      (and (= descriptors (:module-source-descriptors products))
           (= manifest (:link-manifest products))
           (= plans (:module-plans products))
           (= bundle (:execution-bundle products))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execution-bundle-authorized?
  [descriptors manifest plans bundle]
  (and (p15-s23-sh07-b51-digest-id? p15-s23-sh07-b51-expected-m3-id)
       (= p15-s23-sh07-b51-expected-m3-id
          (:bundle-id bundle))
       (p15-s23-sh07-b51-execution-bundle-valid?
        descriptors manifest plans bundle)))

(defn p15-s23-sh07-b51-external-instruction
  [module caller callee arity callsite-path]
  (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
        manifest (:p15-s23-sh07-link-manifest module)
        rows (filter #(and (= (:module-source-id descriptor)
                             (:caller-module-source-id %))
                           (= caller (:caller-function %))
                           (= callee (:callee-function %))
                           (= arity (:arity %))
                           (= callsite-path (:callsite-path %)))
                     (:edges manifest))]
    (when-not (and (p15-s23-sh07-b51-module-source-valid? descriptor)
                   (= (:manifest-id manifest)
                      (p15-s23-sh07-b51-manifest-id manifest))
                   (= 1 (count rows)))
      (throw (ex-info "unauthenticated SH-07 B51 external call"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :caller caller :callee callee
                       :callsite-path callsite-path})))
    {:artifact :gravity/sh07-b51-external-call-v1
     :manifest-id (:manifest-id manifest)
     :edge-ordinal (:ordinal (first rows))}))

(defn- p15-s23-sh07-b51-runtime-link-fail! [plan instruction]
  (fail! "L2-UNKNOWN-CORE-FORM"
         "stage2 runtime rejected an unauthenticated B51 external link"
         {:source-span {:source (get-in plan [:source :path])}
          :operator (:function instruction)
          :reason :unauthenticated-b51-external-link
          :result-committed? false
          :output-committed? false
          :remediation
          "Execute only the independently pinned seven-module M3 bundle."}))

(defn- p15-s23-sh07-b51-runtime-authorize-external-link*
  [runtime plan instruction]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        descriptors (:module-source-descriptors context)
        manifest (:link-manifest context)
        plans (:module-plans context)
        bundle (:execution-bundle context)
        link (:external-link instruction)
        edge (when (and (map? link) (integer? (:edge-ordinal link))
                        (<= 0 (:edge-ordinal link))
                        (vector? (:edges manifest))
                        (< (:edge-ordinal link) (count (:edges manifest))))
               (nth (:edges manifest) (:edge-ordinal link) nil))
        route (when (and edge (vector? (:routes bundle))
                         (< (:ordinal edge) (count (:routes bundle))))
                (nth (:routes bundle) (:ordinal edge) nil))
        caller-plan (when route
                      (first (filter #(= (:caller-plan-id route) (:plan-id %))
                                     plans)))
        callee-plan (when route
                      (first (filter #(= (:callee-plan-id route) (:plan-id %))
                                     plans)))
        caller-definition
        (when edge (get-in plan [:functions (:caller-function edge)]))
        occurrences
        (when caller-definition
          (filter #(= instruction %)
                  (tree-seq coll? seq (:instructions caller-definition))))]
    (when-not
     (and context
          (= #{:op :function :args :external-link} (set (keys instruction)))
          (= :function-call (:op instruction))
          (= #{:artifact :manifest-id :edge-ordinal} (set (keys link)))
          (= :gravity/sh07-b51-external-call-v1 (:artifact link))
          (= (:manifest-id manifest) (:manifest-id link))
          edge route
          (= plan caller-plan)
          (= (:plan-id plan) (:caller-plan-id route))
          (= (:sh07-b51-module-source-id plan)
             (:caller-module-source-id edge))
          (= (:function instruction) (:callee-function edge))
          (= (count (:args instruction)) (:arity edge))
          (= 1 (count occurrences))
          (= (:plan-id callee-plan) (:callee-plan-id route))
          (= (:sh07-b51-module-source-id callee-plan)
             (:callee-module-source-id edge))
          (= (:callee-function edge) (:name (get-in callee-plan
                                                     [:functions
                                                      (:callee-function edge)])))
          (= (:arity edge) (:arity (get-in callee-plan
                                           [:functions
                                            (:callee-function edge)]))))
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))
    {:callee-plan callee-plan :edge edge :route route}))

(defn p15-s23-sh07-b51-runtime-authorize-external-link
  [runtime plan instruction]
  (try
    (p15-s23-sh07-b51-runtime-authorize-external-link*
     runtime plan instruction)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (if (= :unauthenticated-b51-external-link (:reason (ex-data error)))
        (throw error)
        (p15-s23-sh07-b51-runtime-link-fail! plan instruction)))
    (catch Throwable _
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))))

(defn- p15-s23-sh07-b51-runtime-record-external-call!
  [runtime caller-plan instruction authority]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        trace (:sh07-b51-external-call-trace runtime)
        callee-plan (:callee-plan authority)]
    (when-not (and context
                   (identical? trace (:external-call-trace context))
                   (instance? clojure.lang.IAtom trace)
                   (< (count @trace) p15-s23-sh07-b51-maximum-external-calls))
      (p15-s23-sh07-b51-runtime-link-fail! caller-plan instruction))
    (swap! trace conj
           {:bundle-id (get-in runtime
                               [:sh07-b51-execution-bundle :bundle-id])
            :edge-ordinal (get-in instruction [:external-link :edge-ordinal])
            :caller-plan-id (:plan-id caller-plan)
            :callee-plan-id (:plan-id callee-plan)
            :function (:function instruction)})))
