

(defn sh06-resolution-dependency-edges
  [source-path module imports overrides]
  (let [base
        (mapv
         (fn [ordinal dependency]
           {:from (:module module)
            :to (:namespace dependency)
            :kind (:kind dependency)
            :boundary (:boundary dependency)
            :semantic-span {:dependency-edge ordinal}
            :source-span (:source-span dependency)})
         (range) imports)
        explicit (get-in overrides [:module-graph :edges])]
    (vec
     (distinct
      (concat
       base
       (map-indexed
        (fn [ordinal edge]
          (let [[from to] (when (vector? edge) edge)]
            {:from (or (:from edge) from)
             :to (or (:to edge) to)
             :kind (or (:kind edge) :namespace)
             :boundary (:boundary edge)
             :semantic-span {:explicit-edge ordinal}
             :source-span (source-span source-path 0)}))
        explicit))))))

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
  [value keys]
  (and (map? value) (= keys (set (keys value)))))

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

(defn- sh06-resolution-source-proof-bytes!
  [source-path content descriptor proof]
  (when-not
   (and (sh06-resolution-exact-keys?
         proof #{:schema :path :root-tree :tree-chain :blob-bytes})
        (= :gravity/sh07-b51-git-tree-inclusion-proof-v1 (:schema proof))
        (= source-path (:path proof) (:path descriptor))
        (= (:tree content) (:root-tree proof))
        (vector? (:tree-chain proof))
        (seq (:tree-chain proof))
        (sh06-resolution-byte-vector? (:blob-bytes proof)))
    (sh06-resolution-c5-fail!
     source-path :source-object-proof-shape {}))
  (let [components (vec (.split ^String source-path "/" -1))
        chain (:tree-chain proof)]
    (when-not (= (count components) (count chain))
      (sh06-resolution-c5-fail!
       source-path :tree-chain-length
       {:component-count (count components) :row-count (count chain)}))
    (doseq [index (range (count chain))]
      (let [row (nth chain index)
            component (nth components index)
            final? (= index (dec (count chain)))]
        (when-not
         (and (sh06-resolution-exact-keys?
               row #{:tree-oid :tree-bytes :component
                     :selected-mode :selected-oid})
              (sh06-resolution-git-oid? (:tree-oid row))
              (sh06-resolution-byte-vector? (:tree-bytes row))
              (= component (:component row))
              (= (:tree-oid row)
                 (if (zero? index) (:tree content)
                     (:selected-oid (nth chain (dec index)))))
              (= (:tree-oid row)
                 (sh06-resolution-git-object-id
                  "tree" (sh06-resolution-byte-array (:tree-bytes row)))))
         (sh06-resolution-c5-fail!
          source-path :tree-chain-row {:row-index index}))
        (let [entries (sh06-resolution-parse-tree!
                       source-path (:tree-bytes row))
              selected (filterv #(= component (:name %)) entries)]
          (when-not
           (and (= 1 (count selected))
                (= (:selected-mode row) (:mode (first selected)))
                (= (:selected-oid row) (:oid (first selected)))
                (= (if final? "100644" "40000") (:selected-mode row)))
            (sh06-resolution-c5-fail!
             source-path :tree-selected-entry {:row-index index})))))
    (let [blob (:blob-bytes proof)
          blob-array (sh06-resolution-byte-array blob)
          leaf (peek chain)]
      (when-not
       (and (= "100644" (:mode descriptor))
            (= (:blob-oid descriptor) (:selected-oid leaf))
            (= (:blob-oid descriptor)
               (sh06-resolution-git-object-id "blob" blob-array))
            (= (:byte-count descriptor) (count blob))
            (= (:raw-sha256 descriptor)
               (str "sha256:" (sha256-bytes-hex blob-array))))
        (sh06-resolution-c5-fail!
         source-path :blob-object-proof {:descriptor descriptor}))
      blob)))

(defn- sh06-resolution-b51-owner-edge
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

(defn- sh06-resolution-b51-definition-hash
  [definition]
  (reader-canonical-hash
   (select-keys definition [:name :arity :params :body])))

(defn- sh06-resolution-b51-binding-census
  [source-path module module-ordinal]
  (let [syntax-stream
        (mapv (fn [ordinal form]
                {:form form
                 :syntax/id
                 (reader-canonical-hash
                  {:domain :gravity/sh07-b51-attempt24-syntax-v1
                   :module-ordinal module-ordinal :form-ordinal ordinal})
                 :span {:source source-path :form-index ordinal}})
              (range) (:forms module))
        sh05 {:artifact-id
              (reader-canonical-hash
               {:domain :gravity/sh07-b51-attempt24-sh05-census-v1
                :module-ordinal module-ordinal})
              :expanded-syntax-stream syntax-stream}
        definitions (sh06-resolution-definition-records module sh05)
        analysis (sh06-resolution-analysis-inputs module sh05 {})]
    {:definition-count (count definitions)
     :core-binding-count (count (sh06-resolution-core-records))
     :lexical-binding-count (count (mapcat :bindings (:lexical-scopes analysis)))}))

(defn- sh06-resolution-b51-builtin-census
  [table]
  (vec
   (mapcat
    (fn [caller]
      (let [definition (get table caller)
            caller-hash (sh06-resolution-b51-definition-hash definition)]
        (mapcat
         (fn [body-form]
           (letfn [(walk [value]
                     (cond
                       (seq? value)
                       (if (= 'quote (first value)) []
                           (let [callee (first value)
                                 row (when (contains?
                                            sh06-resolution-b51-effective-builtins
                                            callee)
                                       [{:caller caller
                                         :caller-function-source-hash caller-hash
                                         :builtin callee
                                         :arity (count (rest value))
                                         :compiler-internal?
                                         (contains?
                                          sh06-resolution-b51-declared-builtin-arities
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
    (sort-by str (keys table)))))

(defn- sh06-resolution-b51-module-inputs!
  [source-path content descriptors inputs]
  (when-not
   (and (vector? descriptors) (= 7 (count descriptors))
        (vector? inputs) (= 7 (count inputs)))
    (sh06-resolution-c5-fail!
     source-path :module-source-input-count {}))
  (mapv
   (fn [ordinal spec descriptor input]
     (when-not
      (and (sh06-resolution-exact-keys?
            descriptor #{:ordinal :kind :module-ordinal :path :mode
                         :blob-oid :byte-count :raw-sha256})
           (= (+ 3 ordinal) (:ordinal descriptor))
           (= :module (:kind descriptor))
           (= ordinal (:module-ordinal descriptor))
           (= (:logical-source-path spec) (:path descriptor))
           (sh06-resolution-exact-keys?
            input #{:descriptor :source-object-proof})
           (= descriptor (:descriptor input)))
       (sh06-resolution-c5-fail!
        source-path :module-source-descriptor {:module-ordinal ordinal}))
     (let [blob (sh06-resolution-source-proof-bytes!
                 (:path descriptor) content descriptor
                 (:source-object-proof input))
           text (sh06-resolution-strict-utf8 (:path descriptor) blob)
           module (parse-module (:path descriptor)
                                (read-forms (:path descriptor) text))
           table (stage0-function-table module)]
       (when-not
        (and (= (:module spec) (:module module))
             (= :meta (:profile module)) (= :jvm (:target module))
             (= #{} (:effects module)) (= #{} (:capabilities module))
             (= :safe (:safety module)))
         (sh06-resolution-c5-fail!
          source-path :module-source-contract {:module-ordinal ordinal}))
       {:spec spec :descriptor descriptor :bytes blob :text text
        :module module :table table
        :source-sha256 (:raw-sha256 descriptor)}))
   (range 7) sh06-resolution-b51-module-specs descriptors inputs))

(defn- sh06-resolution-b51-callsite-census!
  [source-path modules]
  (let [names (vec (mapcat (comp keys :table) modules))]
    (when-not (= (count names) (count (set names)))
      (sh06-resolution-c5-fail!
       source-path :duplicate-provider-definition {}))
    (let [owner-by-name
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
                                    (if (= 'quote (first value)) []
                                        (let [callee (first value)
                                              callee-owner
                                              (get owner-by-name callee)
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
                                                  (sh06-resolution-b51-owner-edge
                                                   (:owner spec)
                                                   (:owner callee-owner))}])]
                                          (into (or row [])
                                                (mapcat
                                                 (fn [index child]
                                                   (walk child (conj path index)))
                                                 (range) value))))
                                    (vector? value)
                                    (mapcat (fn [index child]
                                              (walk child (conj path index)))
                                            (range) value)
                                    (map? value)
                                    (let [entries
                                          (sort-by #(pr-str
                                                     (reader-canonical-value %))
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
                                            (sort-by #(pr-str
                                                       (reader-canonical-value %))
                                                     value))
                                    :else []))]
                          (walk body-form [body-index])))
                      (range) (:body definition))))
                 (sort-by str (keys table)))))
            modules))
          ordered (vec (sort-by (juxt :caller-module-ordinal
                                      (comp str :caller-function)
                                      (comp count :callsite-path)
                                      :callsite-path) rows))]
      (when-not
       (and (= 1480 (count ordered))
            (every? :owner-edge ordered)
            (= (count ordered)
               (count (set (map (juxt :caller-module-ordinal
                                      :caller-function :callsite-path)
                                ordered)))))
        (sh06-resolution-c5-fail!
         source-path :unauthorized-cross-fragment-edge
         {:edge-count (count ordered)}))
      (mapv #(assoc %1 :ordinal %2) ordered (range)))))

(defn- sh06-resolution-b51-derive-m0-m1!
  [source-path modules]
  (let [census (sh06-resolution-b51-callsite-census! source-path modules)
        by-ordinal (into {} (map (fn [m]
                                  [(get-in m [:spec :module-ordinal]) m])
                                modules))
        definitions
        (mapv
         (fn [{:keys [spec module]}]
           (mapv
            (fn [form]
              (let [definition (function-definition-form
                                (:logical-source-path spec) form)]
                {:name (:name definition)
                 :arity (count (:params definition))
                 :visibility (if (contains? (set (:exports spec))
                                            (:name definition))
                               :public :private)
                 :source-hash
                 (sh06-resolution-b51-definition-hash
                  (assoc definition :arity (count (:params definition))))}))
            (filter #(function-definition-form
                      (:logical-source-path spec) %)
                    (:forms module))))
         modules)
        external-symbols
        (mapv
         (fn [ordinal]
           (vec (sort-by str
                         (set (map :callee-function
                                   (filter #(= ordinal
                                               (:caller-module-ordinal %))
                                           census))))))
         (range 7))
        m0-bases
        (mapv
         (fn [ordinal {:keys [spec descriptor bytes module table] :as input}]
           (let [binding-census
                 (sh06-resolution-b51-binding-census
                  (:logical-source-path spec) module ordinal)
                 externals
                 (mapv
                  (fn [symbol]
                    (let [sample
                          (first (filter #(and (= ordinal
                                                  (:caller-module-ordinal %))
                                               (= symbol (:callee-function %)))
                                         census))
                          callee (get by-ordinal
                                      (:callee-module-ordinal sample))
                          definition
                          (first (filter #(= symbol (:name %))
                                         (nth definitions
                                              (:callee-module-ordinal sample))))]
                      {:symbol symbol
                       :callee-module-ordinal (:callee-module-ordinal sample)
                       :callee-module (get-in callee [:spec :module])
                       :callee-source-sha256 (:source-sha256 callee)
                       :callee-definition-source-hash (:source-hash definition)
                       :arity (:arity sample)
                       :owner-edge (:owner-edge sample)}))
                  (nth external-symbols ordinal))
                 census-counts binding-census]
             {:artifact :gravity/sh07-b51-module-source-v1
              :schema-version 1 :module-source-id nil
              :module-ordinal ordinal :module (:module spec)
              :logical-source-path (:logical-source-path spec)
              :owner (:owner spec) :source-byte-count (count bytes)
              :source-sha256 (:raw-sha256 descriptor)
              :profile (:profile module) :target (:target module)
              :effects (:effects module) :capabilities (:capabilities module)
              :safety (:safety module) :definitions (nth definitions ordinal)
              :exports (:exports spec)
              :core-binding-count (:core-binding-count census-counts)
              :lexical-binding-count (:lexical-binding-count census-counts)
              :external-bindings externals
              :aggregate-binding-count
              (+ (:core-binding-count census-counts)
                 (:definition-count census-counts)
                 (:lexical-binding-count census-counts)
                 (count externals))
              :builtin-calls (sh06-resolution-b51-builtin-census table)}))
         (range 7) modules)
        m0s (mapv #(assoc % :module-source-id
                           (reader-canonical-hash
                            (dissoc % :module-source-id)))
                   m0-bases)
        edges
        (mapv
         (fn [row]
           (let [caller (nth m0s (:caller-module-ordinal row))
                 callee (nth m0s (:callee-module-ordinal row))
                 caller-definition
                 (first (filter #(= (:caller-function row) (:name %))
                                (:definitions caller)))
                 callee-definition
                 (first (filter #(= (:callee-function row) (:name %))
                                (:definitions callee)))]
             {:ordinal (:ordinal row)
              :caller-module-source-id (:module-source-id caller)
              :caller-function (:caller-function row)
              :caller-function-source-hash (:source-hash caller-definition)
              :callsite-path (:callsite-path row)
              :callee-module-source-id (:module-source-id callee)
              :callee-function (:callee-function row)
              :callee-function-source-hash (:source-hash callee-definition)
              :arity (:arity row) :owner-edge (:owner-edge row)
              :authorization :authenticated-b51-namespace-external}))
         census)
        m1-base {:artifact :gravity/sh07-b51-link-manifest-v1
                 :schema-version 1
                 :manifest-id nil
                 :module-source-ids (mapv :module-source-id m0s)
                 :edges edges}
        m1 (assoc m1-base :manifest-id
                  (reader-canonical-hash (dissoc m1-base :manifest-id)))]
    (when-not (and (every? #(= sh06-resolution-b51-m0-keys
                                (set (keys %))) m0s)
                   (= sh06-resolution-b51-m1-keys (set (keys m1))))
      (sh06-resolution-c5-fail! source-path :derived-m0-m1-shape {}))
    {:module-source-descriptors m0s :link-manifest m1}))

(defn- sh06-resolution-b51-request-carriers
  [source-path caller manifest]
  (let [descriptors (:module-source-descriptors caller)
        caller-row (:caller caller)
        authority-rows (:authority-rows caller)
        groups (->> authority-rows
                    (group-by #(get-in % [:callee :module-ordinal]))
                    (sort-by key) (mapv second))
        imports
        (mapv
         (fn [rows]
           (let [callee (:callee (first rows))
                 edge-ordinals (vec (mapcat #(map :ordinal (:edges %)) rows))
                 join {:manifest-id (:manifest-id manifest)
                       :caller-module-source-id (:module-source-id caller-row)
                       :callee-module-source-id (:module-source-id callee)
                       :bindings
                       (mapv (fn [{:keys [external edges]}]
                               {:external-binding external
                                :edge-ordinals (mapv :ordinal edges)}) rows)}]
             {:namespace (:module callee) :alias nil :kind :namespace
              :profile (:profile callee) :targets [(:target callee)]
              :visibility :public
              :refer (mapv #(get-in % [:external :symbol]) rows)
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee)
              :boundary (get-in rows [0 :external :owner-edge])
              :semantic-span
              {:manifest-id (:manifest-id manifest)
               :caller-module-source-id (:module-source-id caller-row)
               :callee-module-source-id (:module-source-id callee)
               :authorized-edge-ordinals edge-ordinals}
              :source-span {:source (:logical-source-path caller-row)}
              :dependency-artifact-id
              (reader-canonical-hash
               [:gravity/sh07-b51-authenticated-sh06-import-v1 join])
              :foreign-record-complete? true})) groups)
        bindings
        (mapv
         (fn [{:keys [external callee definition edges]}]
           (let [coordinate
                 {:manifest-id (:manifest-id manifest)
                  :caller-module-source-id (:module-source-id caller-row)
                  :callee-module-source-id (:module-source-id callee)
                  :callee-module (:module callee)
                  :callee-source-sha256 (:source-sha256 callee)
                  :callee-definition-source-hash (:source-hash definition)
                  :symbol (:symbol external) :arity (:arity external)
                  :owner-edge (:owner-edge external)
                  :authorized-edge-ordinals (mapv :ordinal edges)}]
             {:name (:symbol external) :kind :function
              :namespace (:module callee)
              :package (sh06-resolution-package (:module callee))
              :binding-class :import :visibility :public
              :profile-set [(:profile callee)] :target-set [(:target callee)]
              :type-ref :gravity.type/function
              :effects (vec (sort (:effects callee)))
              :capabilities (vec (sort (:capabilities callee)))
              :safety (:safety callee) :semantic-span coordinate
              :source-span {:source :external-binding}
              :definition-syntax-id
              (reader-canonical-hash
               [:gravity/sh07-b51-authenticated-sh06-binding-v1 coordinate])
              :definition-artifact-id (:module-source-id callee)}))
         authority-rows)
        edges (mapv (fn [dependency]
                      {:from (:module caller-row)
                       :to (:namespace dependency) :kind :namespace
                       :boundary (:boundary dependency)
                       :semantic-span (:semantic-span dependency)
                       :source-span (:source-span dependency)}) imports)]
    {:imports imports :import-bindings bindings :dependency-edges edges}))

(defn- sh06-resolution-c5-expected-projection!
  [source-path module-ordinal m0s manifest]
  (let [caller (nth m0s module-ordinal)
        caller-edges (filterv #(= (:module-source-id caller)
                                  (:caller-module-source-id %))
                              (:edges manifest))
        rows
        (mapv
         (fn [external]
           (let [symbol (:symbol external)
                 callee-ordinal (:callee-module-ordinal external)
                 callee (when (and (integer? callee-ordinal)
                                   (<= 0 callee-ordinal 6))
                          (nth m0s callee-ordinal nil))
                 definitions (filterv #(= symbol (:name %))
                                      (:definitions callee))
                 definition (first definitions)
                 edges (filterv #(= symbol (:callee-function %)) caller-edges)
                 expected-owner (when callee
                                  (sh06-resolution-b51-owner-edge
                                   (:owner caller) (:owner callee)))]
             (when-not
              (and (= 1 (count definitions)) (seq edges) expected-owner
                   (= expected-owner (:owner-edge external))
                   (= (:module callee) (:callee-module external))
                   (= (:source-sha256 callee)
                      (:callee-source-sha256 external))
                   (= (:source-hash definition)
                      (:callee-definition-source-hash external))
                   (= (:arity definition) (:arity external))
                   (every? #(and (= :authenticated-b51-namespace-external
                                     (:authorization %))
                                  (= expected-owner (:owner-edge %))
                                  (= (:module-source-id callee)
                                     (:callee-module-source-id %))
                                  (= (:source-hash definition)
                                     (:callee-function-source-hash %))
                                  (= (:arity external) (:arity %))) edges))
               (sh06-resolution-c5-fail!
                source-path :unauthorized-or-stale-edge {:symbol symbol}))
             {:external external :callee callee
              :definition definition :edges edges}))
         (:external-bindings caller))
        covered (vec (mapcat #(map :ordinal (:edges %)) rows))]
    (when-not (and (= (set covered) (set (map :ordinal caller-edges)))
                   (= (count covered) (count (set covered))))
      (sh06-resolution-c5-fail!
       source-path :incomplete-edge-coverage {:covered covered}))
    (sh06-resolution-b51-request-carriers
     source-path {:module-source-descriptors m0s :caller caller
                  :authority-rows rows} manifest)))

(defn- sh06-resolution-validate-c5-authority!
  [source-path sh05-artifact base context]
  (when-not
   (and (sh06-resolution-exact-keys?
         context #{:external-binding-authority :resolution-plan-authority})
        (map? (:external-binding-authority context)))
    (sh06-resolution-c5-fail! source-path :missing-or-malformed-context {}))
  (let [authority (:external-binding-authority context)
        preimage (:preimage authority)
        content (:content authority)]
    (when-not
     (and (sh06-resolution-exact-keys?
           authority
           #{:schema :schema-version :content :module-ordinal :source-path
             :sh05-artifact-id :base-request-id :imports :import-bindings
             :dependency-edges :preimage :self-id-omits :authority-id})
          (= :gravity/sh07-b51-c5-external-binding-authority-v1
             (:schema authority))
          (= 1 (:schema-version authority))
          (= [[:authority-id]] (:self-id-omits authority))
          (sh06-resolution-sha256-id? (:authority-id authority))
          (= (:authority-id authority)
             (reader-canonical-hash
              [:gravity/sh07-b51-c5-external-binding-authority-v1
               (dissoc authority :authority-id)]))
          (sh06-resolution-exact-keys?
           preimage #{:schema :content :content-object-proof
                      :source-descriptors :module-source-inputs
                      :module-source-descriptors :link-manifest})
          (= :gravity/sh07-b51-c5-authority-preimage-v1 (:schema preimage))
          (= content (:content preimage))
          (= source-path (:source-path authority))
          (= (:artifact-id sh05-artifact) (:sh05-artifact-id authority))
          (= (reader-canonical-hash
              [:gravity/sh07-b51-sh06-base-request-v1 base])
             (:base-request-id authority)))
      (sh06-resolution-c5-fail! source-path :authority-shape-or-id {}))
    (sh06-resolution-content-proof-tree!
     source-path content (:content-object-proof preimage))
    (let [modules
          (sh06-resolution-b51-module-inputs!
           source-path content (:source-descriptors preimage)
           (:module-source-inputs preimage))
          derived (sh06-resolution-b51-derive-m0-m1! source-path modules)
          m0s (:module-source-descriptors derived)
          manifest (:link-manifest derived)
          ordinal (:module-ordinal authority)
          plan (:resolution-plan-authority context)]
      (when-not
       (and (= m0s (:module-source-descriptors preimage))
            (= manifest (:link-manifest preimage))
            (integer? ordinal) (<= 0 ordinal 6)
            (= source-path (:logical-source-path (nth m0s ordinal)))
            (= content (:content plan)))
        (sh06-resolution-c5-fail!
         source-path :m0-m1-or-context-content-mismatch {}))
      (let [projection
            (sh06-resolution-c5-expected-projection!
             source-path ordinal m0s manifest)]
        (when-not
         (= projection
            (select-keys authority
                         [:imports :import-bindings :dependency-edges]))
          (sh06-resolution-c5-fail!
           source-path :fabricated-external-binding-projection {}))
        authority))))

(defn sh06-resolution-c5-authority-request!
  [source-path sh05-artifact base context]
  (let [authority
        (sh06-resolution-validate-c5-authority!
         source-path sh05-artifact base context)
        external-symbols (set (map :name (:import-bindings authority)))
        base-symbols (set (concat (map :name (:definitions base))
                                  (map :name (:import-bindings base))))]
    (when-not (empty? (clojure.set/intersection external-symbols base-symbols))
      (sh06-resolution-c5-fail!
       source-path :external-binding-intersects-base
       {:symbols (vec (sort-by str
                              (clojure.set/intersection
                               external-symbols base-symbols)))}))
    (-> base
        (update :imports into (:imports authority))
        (update :import-bindings into (:import-bindings authority))
        (update :dependency-edges into (:dependency-edges authority)))))

(defn sh06-resolution-request
  ([source-path sh05-artifact]
   (let [verification (sh05-macro-artifact-verification sh05-artifact)
        _ (when-not (= :passed (:status verification))
            (c5-resolution-fail!
             "C5-UNRESOLVED" source-path
             {:stage :name-resolution}
             {:missing-fields [:verified-sh05-macro-artifact]}))
        forms (mapv :form (:expanded-syntax-stream sh05-artifact))
        module (parse-module source-path forms)
        overrides (sh06-resolution-request-overrides module)
        imports (sh06-resolution-import-records
                 source-path module overrides)
        explicit-definitions
        (sh06-resolution-explicit-candidate-records
         module sh05-artifact overrides)
        analysis-inputs
        (sh06-resolution-analysis-inputs
         module sh05-artifact overrides)
        source-revision-id
        (sh06-resolution-source-revision-id sh05-artifact)]
    {:artifact :gravity/sh06-authenticated-c4-resolution-request
     :schema-version 1
     :module
     {:namespace (:module module)
      :package (sh06-resolution-package (:module module))
      :profile (:profile module)
      :target (:target module)
      :safety (:safety module)
      :effects (vec (sort (:effects module)))
      :capabilities (vec (sort (:capabilities module)))
      :exports (vec (:exports module))
      :source-revision-id source-revision-id
      :c4-artifact-id (:artifact-id sh05-artifact)}
     :definitions
     (vec (concat
           (sh06-resolution-definition-records module sh05-artifact)
           explicit-definitions))
     :imports imports
     :import-bindings
     (sh06-resolution-import-binding-records module imports overrides)
     :lexical-scopes (:lexical-scopes analysis-inputs)
     :references (:references analysis-inputs)
     :core-bindings (sh06-resolution-core-records)
     :dependency-edges
     (sh06-resolution-dependency-edges source-path module imports overrides)
     :macro-expansion-binding
     {:artifact :gravity/sh05-macro-expansion-binding
      :schema-version 1
      :c4-artifact-id (:artifact-id sh05-artifact)
      :source-revision-id source-revision-id
      :macro-result-id (:expanded-syntax-stream-id sh05-artifact)
      :authenticated-envelope-id
      (sh06-resolution-envelope-id sh05-artifact)}
     :provenance {:actual-source-path source-path}}))
  ([source-path sh05-artifact context]
   (when (nil? context)
     (sh06-resolution-c5-fail! source-path :missing-context {}))
   (let [base (sh06-resolution-request source-path sh05-artifact)]
     (sh06-resolution-c5-authority-request!
      source-path sh05-artifact base context))))

(def sh06-resolution-source-relative-path
  "bootstrap/gravity/src/gravity/resolution.gravity")
(def sh06-resolution-facade-relative-path
  "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity")
(def sh06-resolution-adapter-contract
  :gravity/sh06-to-c5-resolution-products-v1)
(def sh06-resolution-envelope-stage :c5-resolution)
(def sh06-resolution-sealed-artifact-kind
  :gravity/sh06-resolution-products)

;; Final source/plan pins are filled only after the leaf's semantic matrix is
;; stable.  A nil pin is rejected, so an in-progress leaf cannot accidentally
;; receive integration credit.
(def sh06-resolution-expected-source-byte-count 77209)
(def sh06-resolution-expected-source-content-hash
  "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56")
(def sh06-resolution-expected-plan-semantic-hash
  "sha256:ba8c292ffe3c703bbc220af0a7121496c134793d1e80a012f6fb0a0d9dc6b6fa")
(def sh06-resolution-expected-plan-id
  "sha256:a85dd6c53929b8a9f9b4fdf9d7b8b3b5c92fd06bacccf282ea036f9738cc13ee")
(def sh06-resolution-expected-functions-semantic-hash
  "sha256:a1445872cda26a9aa0fb8959165eada593619de7a03f527a8ea8a8c216237b91")
(def sh06-resolution-expected-function-count 94)
(def sh06-resolution-expected-function-names-hash
  "sha256:1a12e29aea416d4df7c2904d366975ce240f5ecaee0b2b9c5aa95b1aa83d1ba4")
(def sh06-resolution-expected-function-shapes-hash
  "sha256:62aec401ab663019a10b63c1b91487f70b9756503d59e43879283afae8c02f57")
(def sh06-resolution-public-function-hashes
  {'sh06-build-resolution-template
   "sha256:154d52c95bc0488e5ec302ce9219b57d72995f5f54f137816ca980d97bf71f38"
   'sh06-verify-resolution-template
   "sha256:de579ce14c12644979fe636d5f7b32d02c505753b85463b8a1994eb9de7a03ec"
   'sh06-verify-resolution-resolved
   "sha256:80e3c1e5313ffb38805eba60110dc80edccb50218eaabd5edba98b2025e99342"})
(def sh06-resolution-public-function-shapes
  {'sh06-build-resolution-template {:arity 1 :params ['request]}
   'sh06-verify-resolution-template
   {:arity 2 :params ['analysis 'digest-requests]}
   'sh06-verify-resolution-resolved
   {:arity 3
    :params ['analysis 'digest-requests 'resolved-digests]}})

(defn sh06-resolution-boundary-fail!
  [rule source-path missing-field observed facts]
  (c5-resolution-fail!
   rule source-path
   {:source-span (source-span source-path 0)
    :symbol (:symbol facts)
    :syntax-id (:syntax-id facts)
    :namespace (:namespace facts)
    :profile (:profile facts)
    :target (:target facts)
    :candidate-bindings (:candidate-bindings facts)
    :dependency-edge (:dependency-edge facts)}
   {:severity :error
    :profile (:profile facts)
    :missing-fields [missing-field]
    :facts (merge {:sh06-boundary :gravity-resolution-plan} facts)
    :observed observed}))

(defn sh06-resolution-resolve-source-path
  []
  (let [anchor (java.io.File.
                (p15-s23-stage2-compiler-artifact-source-path))
        start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
    (or
     (loop [directory start]
       (when directory
         (let [candidate
               (java.io.File. directory sh06-resolution-source-relative-path)]
           (if (.isFile candidate)
             (.getPath candidate)
             (recur (.getParentFile directory))))))
     sh06-resolution-source-relative-path)))

(defn sh06-resolution-read-pinned-source!
  [request-source]
  (let [source-path (sh06-resolution-resolve-source-path)
        nio-path (.toPath (java.io.File. source-path))
        nofollow (into-array java.nio.file.LinkOption
                             [java.nio.file.LinkOption/NOFOLLOW_LINKS])
        attributes
        (try
          (java.nio.file.Files/readAttributes
           nio-path java.nio.file.attribute.BasicFileAttributes nofollow)
          (catch Exception error
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" request-source :pinned-resolution-source-readable
             source-path {:cause-message (.getMessage error)})))
        bytes
        (try
          (java.nio.file.Files/readAllBytes nio-path)
          (catch Exception error
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" request-source :pinned-resolution-source-bytes
             source-path {:cause-message (.getMessage error)})))
        content-hash (str "sha256:" (sha256-bytes-hex bytes))]
    (when-not
     (and (number? sh06-resolution-expected-source-byte-count)
          (string? sh06-resolution-expected-source-content-hash)
          attributes (.isRegularFile attributes)
          (= (long sh06-resolution-expected-source-byte-count)
             (.size attributes))
          (= sh06-resolution-expected-source-byte-count (alength bytes))
          (= sh06-resolution-expected-source-content-hash content-hash))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" request-source :exact-pinned-resolution-source
       {:source-path source-path
        :source-byte-count (alength bytes)
        :source-content-hash content-hash}
       {:expected-byte-count sh06-resolution-expected-source-byte-count
        :expected-content-hash
        sh06-resolution-expected-source-content-hash}))
    {:source-path source-path
     :source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
     :source-byte-count (alength bytes)
     :source-content-hash content-hash}))

(defn sh06-resolution-plan-identities
  [plan]
  (let [functions (:functions plan)
        shapes
        (into (sorted-map)
              (map (fn [[name function]]
                     [name (select-keys function [:arity :params])]))
              functions)]
    {:plan-semantic-hash
     (p15-s23-c11-mir-digest
      (p15-s23-stage2-compiler-artifact-semantic-input plan))
     :functions-semantic-hash (p15-s23-c11-mir-digest functions)
     :function-count (count functions)
     :function-names-hash
     (p15-s23-c11-mir-digest (vec (keys functions)))
     :function-shapes-hash (p15-s23-c11-mir-digest shapes)
     :public-function-hashes
     (into (sorted-map)
           (map (fn [name]
                  [name (p15-s23-c11-mir-digest (get functions name))]))
           (keys sh06-resolution-public-function-shapes))
     :public-function-shapes
     (select-keys shapes (keys sh06-resolution-public-function-shapes))}))
