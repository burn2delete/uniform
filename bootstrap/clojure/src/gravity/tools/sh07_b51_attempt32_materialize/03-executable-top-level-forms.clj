
(defn executable-top-level-forms
  "Return every authenticated executable top-level defn in exact source order."
  [q]
  (let [tops (mapv #(q-form q %) (:top-level-form-ids q))
        traces (:macro-origin-traces q)]
    (when-not (= (count tops) (count traces))
      (fail! :owner-top-level-trace-count))
    (doseq [top tops]
      (unique-where!
       #(and (= 'defn (:macro %))
             (= (:syntax-id top) (:output-def-syntax-id %)))
       traces :owner-top-level-trace))
    (doseq [trace traces]
      (when-not (and (= 'defn (:macro trace))
                     (= 1 (count (filter
                                  #(= (:output-def-syntax-id trace)
                                      (:syntax-id %))
                                  tops))))
        (fail! :owner-trace-top-level)))
    tops))

(defn build-owner-registry
  "Construct the authentic Q/RC top-level defn registry in module/source order."
  [module-transactions]
  (let [registry
        (reduce
         (fn [rows [module-index transaction]]
           (when-not (= module-index (:module-ordinal transaction))
             (fail! :owner-module-order))
           (let [q (:authenticated-core-request transaction)
                 rc (:resolved-core-carrier transaction)
                 tops (executable-top-level-forms q)]
             (reduce
              (fn [inner [top-index top]]
                (let [trace
                      (unique-where!
                       #(and (= 'defn (:macro %))
                             (= (:syntax-id top)
                                (:output-def-syntax-id %)))
                       (:macro-origin-traces q) :owner-defn-trace)
                      function-form
                      (unique-where!
                       #(= (:introduced-fn-syntax-id trace) (:syntax-id %))
                       (:forms q) :owner-function-form)
                      function-record
                      (unique-where!
                       #(and (= :named-top-level (:definition-kind %))
                             (= (:form-id function-form)
                                (:function-form-id %))
                             (= (:syntax-id function-form)
                                (:function-syntax-id %)))
                       (:function-records rc) :owner-function-record)
                      definition-record
                      (unique-where!
                       #(and (= (:definition-binding-id function-record)
                                (:binding-id %))
                             (= (:definition-core-node-id function-record)
                                (:core-node-id %)))
                       (:definitions rc) :owner-definition-record)
                      function-node
                      (unique-where!
                       #(= (:function-core-node-id function-record)
                           (:node-id %))
                       (:nodes rc) :owner-function-node)
                      base
                      {:ordinal (count inner)
                       :source-id
                       (get-in q [:lineage :sh06-semantic-projection-id])
                       :module-ordinal module-index
                       :top-level-form-ordinal top-index
                       :declared-name (:definition-name function-record)
                       :arity (:fixed-arity function-record)
                       :defn-id (:definition-core-node-id function-record)
                       :sh05-expanded-def-id (:output-def-syntax-id trace)
                       :sh05-expanded-fn-id (:introduced-fn-syntax-id trace)
                       :declared-body-root-id
                       (:body-core-node-id function-record)}]
                  (when-not
                   (and (= (:definition-core-node-id function-record)
                           (:core-node-id definition-record))
                        (= (:definition-binding-id function-record)
                           (:binding-id definition-record))
                        (= [(:body-core-node-id function-record)]
                           (:children function-node)))
                    (fail! :owner-definition-join))
                  (conj inner
                        (assoc base :registry-id
                               (semantic-witness-member-id
                                :l2-owner-registry base)))))
              rows (map-indexed vector tops))))
         [] (map-indexed vector module-transactions))]
    ;; Every Q form, including nested forms, must reach one authentic registry
    ;; row through its unique owning top-level defn.
    (doseq [[module-index transaction]
            (map-indexed vector module-transactions)
            form (:forms (:authenticated-core-request transaction))]
      (let [top-index
            (top-level-form-index (:authenticated-core-request transaction)
                                  form)]
        (unique-where!
         #(and (= module-index (:module-ordinal %))
               (= top-index (:top-level-form-ordinal %)))
         registry :owner-form-registry-join)))
    registry))

(defn build-owner-selection
  "Select the eight public roots from the complete authentic owner registry."
  [root-specs bundle-core owner-registry]
  (doseq [root-spec root-specs]
    (require-keys! root-spec
                   [:root-ordinal :function :arity :module-ordinal]
                   :owner-root-spec-shape))
  (let [bundle-root-specs
        (mapv
         (fn [root]
           (require-keys! root [:root-ordinal :function :arity :plan-id]
                          :owner-bundle-root-shape)
           (let [plan
                 (unique-where! #(= (:plan-id root) (:plan-id %))
                                (:module-plans bundle-core)
                                :owner-bundle-root-plan)]
             {:root-ordinal (:root-ordinal root)
              :function (:function root)
              :arity (:arity root)
              :module-ordinal (:module-ordinal plan)}))
         (:public-roots bundle-core))]
    (when-not (= root-specs bundle-root-specs)
      (fail! :owner-root-spec-projection))
    (let [candidate-vectors
          (mapv
           (fn [spec]
             (mapv :registry-id
                   (filterv #(and (= (:module-ordinal spec)
                                     (:module-ordinal %))
                                  (= (:function spec) (:declared-name %))
                                  (= (:arity spec) (:arity %)))
                            owner-registry)))
           root-specs)
          selections
          (mapv
           (fn [spec candidates]
             (when-not (= 1 (count candidates))
               (fail! :owner-root-candidate-count
                      {:root-ordinal (:root-ordinal spec)
                       :candidate-count (count candidates)}))
             (let [row
                   (unique-where! #(= (first candidates) (:registry-id %))
                                  owner-registry :owner-root-candidate)]
               {:root-spec spec
                :registry-id (:registry-id row)
                :defn-id (:defn-id row)
                :declared-body-root-id (:declared-body-root-id row)}))
           root-specs candidate-vectors)
          rejected []
          root-specs-source-id
          (codec/H
           [:gravity/sh07-b51-public-root-specs-id-v1 root-specs])
          bundle-root-specs-id
          (codec/H
           [:gravity/sh07-b51-bundle-core-public-roots-id-v1
            bundle-root-specs])]
      {:root-specs root-specs
       :root-specs-source-id root-specs-source-id
       :bundle-core-public-roots-projection bundle-root-specs
       :bundle-core-public-roots-id bundle-root-specs-id
       :registry owner-registry
       :candidates candidate-vectors
       :selections selections
       :rejected rejected
       :registry-root
       (semantic-witness-member-root
        :l2-owner-registry
        [root-specs owner-registry candidate-vectors selections rejected])})))

;; E3 pre-certificate construction -------------------------------------------------

(def checker-semantic-input-keys-e3
  #{:architecture-report-sha256 :snapshot-commit :snapshot-tree
    :predecessor-executable-descriptor-id :module-source-identities
    :module-source-byte-counts :emitter-source-id :emitter-semantic-id
    :m0s :m1 :m2s :bundle-core :source-forms :module-transactions
    :sh06-resolution-plan-authority :sh06-authority-carrier :source-semantic-carrier
    :semantic-algorithm-source-descriptors
    :semantic-algorithm-source-root :checker :provenance})

(def semantic-algorithm-source-paths-e3
  ["bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_child.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_checker_identity.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_materialize.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt32_tier0_materializer.clj"
   "bootstrap/clojure/src/gravity/tools/sh07_b51_v9_codec.clj"])

(def ^:private semantic-algorithm-descriptor-keys
  #{:ordinal :path :mode :blob-oid :byte-count :raw-sha256})

(def ^:private source-semantic-carrier-keys
  #{:source-descriptors :source-events :l1-document :l1-source-units
    :l1-grammar-change :c2-snapshot :c2-units :c2-source-map
    :c2-diagnostics :c2-literal-records :c2-reader-extensions})

(def fixed-fuel-e3
  {:maximum-module-forms 65536
   :maximum-module-core-nodes 65536
   :maximum-fragments 1024
   :maximum-top-level-forms 1024
   :maximum-fragment-forms 1024
   :maximum-form-children 1024
   :maximum-form-depth 256
   :maximum-module-bindings 2440
   :maximum-bindings 2048
   :maximum-alias-records 256
   :maximum-fragment-resolutions 2048
   :maximum-module-resolutions 65536
   :maximum-origin-entries 256
   :maximum-metadata-entries 256
   :maximum-effects 128
   :maximum-capabilities 128
   :maximum-exports 1024
   :maximum-module-carrier-nodes 8388608
   :maximum-module-carrier-depth 256
   :maximum-module-carrier-width 65536
   :maximum-module-scalar-bytes 268435456
   :maximum-template-carrier-nodes 33554432
   :maximum-template-carrier-depth 256
   :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 1073741824
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 1073741824
   :maximum-generated-digest-carrier-nodes 16777216
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 1073741824
   :maximum-module-digest-requests 65540})
