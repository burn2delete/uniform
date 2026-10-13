
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
