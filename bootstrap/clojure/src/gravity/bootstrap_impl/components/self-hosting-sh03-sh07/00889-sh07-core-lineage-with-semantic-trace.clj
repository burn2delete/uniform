

(defn- sh07-core-lineage-with-semantic-trace
  [resolution-artifact semantic-trace]
  (let [boundary (:gravity-resolution-boundary resolution-artifact)
        analysis (:resolved-analysis boundary)
        sh05 (:sh05-macro-artifact resolution-artifact)
        neutral sh05-path-neutral-semantic-value
        source-unit
        (get-in sh05
                [:gravity-macro-boundary
                 :authenticated-sh04-artifact
                 :c2-reader-artifact :source-unit-record])
        source-revision-id (:bytes-hash source-unit)
        expanded-forms (mapv :form (:expanded-syntax-stream sh05))
        neutral-expanded-forms (neutral expanded-forms)
        semantic-module
        (select-keys (:module-contract analysis)
                     [:namespace :profile :target :safety
                      :effects :capabilities :exports])
        binding-semantics
        (mapv #(select-keys
                %
                [:name :kind :namespace :package
                 :binding-class :visibility
                 :profile-set :target-set :type-ref :effects
                 :capabilities :safety :semantic-span])
              (:binding-table analysis))
        resolution-semantics
        (mapv #(select-keys
                %
                [:reference-syntax-id :symbol :position
                 :resolution-order :semantic-span :resolution-kind])
              (:resolution-table analysis))
        alias-semantics (vec (:alias-table analysis))
        sh05-artifact-id
        (reader-canonical-hash
         {:domain :gravity/sh07-semantic-sh05-artifact-v1
          :source-revision-id source-revision-id
          :expanded-forms neutral-expanded-forms
          :macro-trace semantic-trace})]
    (let [semantic-projection-id
          (reader-canonical-hash
           {:domain :gravity/sh07-semantic-sh06-artifact-v1
            :source-revision-id source-revision-id
            :module semantic-module
            :expanded-forms neutral-expanded-forms
            :bindings binding-semantics
            :aliases alias-semantics
            :resolutions resolution-semantics})]
      {:authenticated-sh06-artifact-id (:artifact-id resolution-artifact)
       :sh06-semantic-projection-id semantic-projection-id
       ;; Retained as a compatibility alias while consumers migrate to the
       ;; unambiguous semantic-projection name above.
       :sh06-artifact-id semantic-projection-id
     :sh06-analysis-artifact-id
     (reader-canonical-hash
      {:domain :gravity/sh07-semantic-sh06-analysis-v1
       :source-revision-id source-revision-id
       :module semantic-module
       :bindings binding-semantics
       :aliases alias-semantics
       :resolutions resolution-semantics})
     :source-revision-id
     source-revision-id
     :sh05-artifact-id
     sh05-artifact-id
     :expanded-syntax-stream-id
     (reader-canonical-hash
      {:domain :gravity/sh07-semantic-expanded-stream-v1
       :source-revision-id source-revision-id
       :expanded-forms neutral-expanded-forms})
     :macro-expansion-trace-id
     (reader-canonical-hash
      {:domain :gravity/sh07-semantic-macro-trace-v1
       :source-revision-id source-revision-id
       :trace semantic-trace})
     :binding-table-id
     (reader-canonical-hash
      {:domain :gravity/sh07-sh06-binding-table-v1
       :bindings binding-semantics})
     :resolution-table-id
     (reader-canonical-hash
      {:domain :gravity/sh07-sh06-resolution-table-v1
       :resolutions resolution-semantics})
     :alias-table-id
     (reader-canonical-hash
      {:domain :gravity/sh07-sh06-alias-table-v1
       :aliases alias-semantics})
     :lexical-scope-graph-id
     (reader-canonical-hash
      {:domain :gravity/sh07-sh06-lexical-scope-graph-v1
       :source-revision-id source-revision-id
       :graph
       (neutral
        (select-keys (:lexical-scope-graph analysis)
                     [:scope-count :edge-count :status]))})
     :authenticated-envelope-id
     (reader-canonical-hash
      {:domain :gravity/sh07-semantic-upstream-envelope-v1
       :source-revision-id source-revision-id
       :sh05-artifact-id sh05-artifact-id})})))

(defn sh07-core-lineage
  [resolution-artifact]
  (let [sh05 (:sh05-macro-artifact resolution-artifact)]
    (sh07-core-lineage-with-semantic-trace
     resolution-artifact
     (sh07-core-semantic-macro-trace
      (:macro-expansion-trace sh05)))))

(defn sh07-core-projection-binding-input
  [request]
  {:domain :gravity/sh07-authenticated-sh06-core-projection-v15
   :request
   (-> request
       (dissoc :projection-binding :provenance)
       sh05-path-neutral-semantic-value)})

(defn sh07-b13-alias-name
  [resolution]
  (when (= :alias-qualified-required-binding
           (:resolution-order resolution))
    (some-> (:symbol resolution)
            namespace
            symbol)))

(defn sh07-b13-fragment-record
  [lineage module ordinal tree bindings resolutions aliases]
  (let [forms (:records tree)
        form-ids (mapv :form-id forms)
        syntax-ids (set (map :syntax-id forms))
        local-bindings
        (filterv #(contains? syntax-ids (:definition-syntax-id %))
                 bindings)
        local-binding-ids (mapv :binding-id local-bindings)
        local-binding-id-set (set local-binding-ids)
        fragment-resolutions
        (filterv #(contains? syntax-ids (:reference-syntax-id %))
                 resolutions)
        external-binding-ids
        (->> fragment-resolutions
             (map :binding-id)
             (remove local-binding-id-set)
             distinct
             vec)
        alias-names
        (->> fragment-resolutions
             (keep sh07-b13-alias-name)
             distinct
             vec)
        available-aliases (set (map :alias aliases))
        root-form-id (get-in tree [:root :form-id])
        root-node-id root-form-id
        content
        {:ordinal ordinal
         :root-form-ids [root-form-id]
         :form-ids form-ids
         :local-binding-ids local-binding-ids
         :external-binding-ids external-binding-ids
         :resolution-reference-syntax-ids
         (mapv :reference-syntax-id fragment-resolutions)
         :alias-names alias-names
         :root-node-ids [root-node-id]}
        content-id root-form-id
        fragment-id root-form-id]
    (when-not (every? available-aliases alias-names)
      (throw
       (ex-info "SH-07-B13 fragment referenced an undeclared alias"
                {:id "C6-VERIFY"
                 :stage :core-lowering
                 :reason :fragment-undeclared-alias
                 :ordinal ordinal
                 :alias-names alias-names})))
    (assoc content
           :fragment-id fragment-id
           :content-id content-id)))

(defn sh07-b13-fragment-manifest
  [lineage module trees bindings resolutions aliases]
  (mapv
   #(sh07-b13-fragment-record
     lineage module %1 %2 bindings resolutions aliases)
   (range)
   trees))

(defn sh07-b13-binding-order
  [module trees bindings]
  (let [fragment-by-syntax
        (into {}
              (mapcat
               (fn [ordinal tree]
                 (map
                  (fn [form] [(:syntax-id form) ordinal])
                  (:records tree)))
               (range)
               trees))
        indexed (map-indexed vector bindings)
        local?
        #(contains? fragment-by-syntax (:definition-syntax-id %))]
    (->> indexed
         (sort-by
          (fn [[index binding]]
            [(if (local? binding)
               (get fragment-by-syntax
                    (:definition-syntax-id binding)
                    Integer/MAX_VALUE)
               Integer/MAX_VALUE)
             index]))
         (mapv second))))

(defn sh07-b13-fragment-coverage
  [module forms bindings resolutions fragment-manifest]
  (let [local-binding-ids
        (vec (mapcat :local-binding-ids fragment-manifest))]
    {:root-form-count
     (count (mapcat :root-form-ids fragment-manifest))
     :form-count (count forms)
     :local-binding-count (count local-binding-ids)
     :resolution-count (count resolutions)
     :fragment-count (count fragment-manifest)
     :covered-root-form-ids
     (vec (mapcat :root-form-ids fragment-manifest))
     :covered-form-ids
     (vec (mapcat :form-ids fragment-manifest))
     :covered-local-binding-ids
     local-binding-ids
     :covered-resolution-reference-syntax-ids
     (vec
      (mapcat
       :resolution-reference-syntax-ids fragment-manifest))}))