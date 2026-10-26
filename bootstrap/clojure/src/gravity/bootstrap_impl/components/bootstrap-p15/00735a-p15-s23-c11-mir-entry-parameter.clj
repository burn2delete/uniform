(defn p15-s23-c11-mir-parameter-position? [p]
  (and (= #{:byte :char :column :column-unit :line} (set (keys p)))
       (every? integer? (map p [:byte :char :column :line]))
       (<= 0 (:byte p)) (<= 0 (:char p)) (<= 1 (:column p)) (<= 1 (:line p))
       (= :unicode-scalar (:column-unit p))))
(defn p15-s23-c11-mir-parameter-source-valid? [core node index]
  (let [source (:source node) origin (:origin-id source)
        row (get-in core [:source-origin-table origin])
        raw (get-in core [:origin-closure origin])
        input (get-in core [:core-nodes 5 :source])
        generated? (= index 2) span (:span source)
        sha? #(and (string? %) (boolean (re-matches #"sha256:[0-9a-f]{64}" %)))]
    (and (= #{:domain :source-content-hash :plan-path :c2-form-id :c2-form-kind
              :c2-span :c2-surface-span :c2-abbrev :c2-tag :c2-metadata :c2-generated-origin
              :c3-syntax-id :generated-role :input-origin-id :origin-id} (set (keys row)))
         (every? sha? [(:node-id node) origin (:c3-syntax-id row) (:file span)])
         (= #{:file :byte-start :byte-end :start :end} (set (keys span)))
         (every? p15-s23-c11-mir-parameter-position? [(:start span) (:end span)])
         (integer? (:byte-start span)) (integer? (:byte-end span))
         (<= 0 (:byte-start span) (:byte-end span))
         (= (:byte-start span) (get-in span [:start :byte]))
         (= (:byte-end span) (get-in span [:end :byte]))
         (<= (get-in span [:start :char]) (get-in span [:end :char]))
         (<= (get-in span [:start :line]) (get-in span [:end :line]))
         (or (not= (get-in span [:start :line]) (get-in span [:end :line]))
             (<= (get-in span [:start :column]) (get-in span [:end :column])))
         (= (:source-content-hash core) (:source-content-hash row))
         (= :gravity/c6-c10-source-origin-v1 (:domain row))
         (= (:path node) (:plan-path row)) (= origin (:origin-id row))
         (= (:span source) (:c2-span row))
         (keyword? (:c2-form-id row))
         (= (:c2-form-kind row) (nth [:symbol :symbol :list :boolean :boolean :list :list] index))
         (= (:generated-role row) (when generated? :truthiness-normalization))
         (= (:input-origin-id row) (when generated? (:origin-id input)))
         (nil? (:c2-abbrev row)) (nil? (:c2-tag row)) (= {} (:c2-metadata row))
         (contains? #{nil []} (:c2-generated-origin row))
         (= raw (select-keys row [:origin-id :c2-form-id :c2-form-kind :c2-span
                                  :c2-surface-span :c2-abbrev :c2-tag :c2-metadata
                                  :c2-generated-origin :c3-syntax-id :generated-role :input-origin-id]))
         (= source {:origin-id origin :span (:c2-span row)
                    :enclosing-syntax-origin-id (:c3-syntax-id row)
                    :generated? generated?
                    :generated-origin (if generated?
                                        [{:kind :compiler-generated
                                          :producer :gravity/c6-c10-checked-core-pipeline
                                          :role :truthiness-normalization
                                          :input-origin-id (:origin-id input)}] [])})
         (= (:c3-syntax-id row) (get-in core [:core-nodes 0 :source :enclosing-syntax-origin-id])))))

(defn p15-s23-c11-mir-parameter-declaration-valid? [core binder root]
  (let [declaration (:resolved-parameter core) rows (:coordinates declaration)
        binding (:parameter-binding declaration) catalog (:type-binding declaration)
        reference (:type-resolution declaration) attrs (:attributes binder)
        span (get-in root [:source :span]) name (:name attrs)
        sha? #(and (string? %) (boolean (re-matches #"sha256:[0-9a-f]{64}" %)))
        binding-keys #{:binding-id :upstream-binding-id :name :kind :namespace :package
                       :binding-class :visibility :profile-set :target-set :type-ref :effects
                       :capabilities :safety :semantic-span :scope-id :definition-syntax-id
                       :definition-artifact-id}]
    (and (= #{:kind :source-content-hash :sh06-artifact-id :sh06-semantic-projection-id
              :projection-binding :root-syntax-id :function-syntax-id :function-scope-id
              :parameter-index :coordinates :parameter-binding :type-binding :type-resolution}
            (set (keys declaration)))
         (= :gravity/c6-c10-resolved-bool-parameter-v1 (:kind declaration))
         (= (:source-content-hash core) (:source-content-hash declaration))
         (integer? (:parameter-index declaration)) (integer? (:parameter-index attrs))
         (= 0 (:parameter-index declaration) (:parameter-index attrs))
         (symbol? name) (= name (:name binding))
         (= (:scope-id binding) (:function-scope-id declaration))
         (every? sha? (map declaration [:sh06-artifact-id :sh06-semantic-projection-id
                                        :projection-binding :root-syntax-id :function-syntax-id
                                        :function-scope-id]))
         (every? #(and (= binding-keys (set (keys %)))
                       (every? sha? (map % [:binding-id :upstream-binding-id
                                           :definition-syntax-id :definition-artifact-id])))
                 [binding catalog])
         (= [:lexical :local (get-in core [:source-core-input :module]) [:hosted] [:jvm]]
            ((juxt :binding-class :kind :namespace :profile-set :target-set) binding))
         (= {:name (get-in core [:source-core-input :module]) :version "workspace"} (:package binding))
         (= [:gravity.type/local :lexical :safe [] [] {:scope-ordinal 1 :binding-ordinal 0}]
            ((juxt :type-ref :visibility :safety :effects :capabilities :semantic-span) binding))
         (= [:core :type 'gravity.core 'Bool :gravity.type/type
             {:name 'gravity/core :version "bootstrap-contract-v1"}]
            ((juxt :binding-class :kind :namespace :name :type-ref :package) catalog))
         (= [[:all] :public nil :safe [] [] {:catalog :gravity/core :ordinal 12}]
            ((juxt :target-set :visibility :scope-id :safety :effects :capabilities :semantic-span) catalog))
         (= [:ai :core :distributed :firmware :formal :gpu :hardware :hosted :kernel :meta :native]
            (:profile-set catalog))
         (vector? rows) (= 4 (count rows))
         (every? true? (map (fn [row role]
                             (and (= #{:role :c2-form-id :projected-form-id :projected-syntax-id}
                                     (set (keys row)))
                                  (= role (:role row)) (keyword? (:c2-form-id row))
                                  (sha? (:projected-form-id row)) (sha? (:projected-syntax-id row))))
                           rows [:parameter-vector :parameter-name :annotation-marker :annotation-type]))
         (= (:definition-syntax-id binding) (:projected-syntax-id (get rows 1)))
         (= (:source-form-id attrs) (:c2-form-id (get rows 1))
            (get-in core [:source-origin-table (get-in binder [:source :origin-id]) :c2-form-id]))
         (= (:resolved-binding-id attrs) (:binding-id binding))
         (= (:type-binding-id attrs) (:binding-id catalog))
         (= #{:reference-syntax-id :symbol :position :binding-id :resolution-order
              :source-span :upstream-binding-id} (set (keys reference)))
         (= :profile-allowed-core-binding (:resolution-order reference))
         (contains? '#{Bool gravity.core/Bool} (:symbol reference))
         (= :type (:position reference))
         (= (:binding-id reference) (:binding-id catalog))
         (= (:upstream-binding-id reference) (:upstream-binding-id catalog))
         (= (:reference-syntax-id reference) (:projected-syntax-id (get rows 3)))
         (= (:source-span reference)
            {:byte-start (:byte-start span) :byte-end (:byte-end span)
             :scalar-start (get-in span [:start :char]) :scalar-end (get-in span [:end :char])
             :line-start (get-in span [:start :line]) :line-end (get-in span [:end :line])
             :column-start (get-in span [:start :column]) :column-end (get-in span [:end :column])}))))

(defn p15-s23-c11-mir-entry-parameter! [source-path core]
  ;; Only checked Core supplies this descriptor. No MIR value, definition or
  ;; report can identify a parameter or establish its declaration premise.
  (when (or (contains? core :resolved-parameter)
            (some #(= :parameter (:source-operation %)) (:core-nodes core)))
    (let [nodes (:core-nodes core) ids (mapv :node-id nodes)
          [binder local truthy then else conditional root] nodes
          attrs (:attributes binder) name (:name attrs)
          purity {:intrinsic-effects #{} :intrinsic-capabilities #{}
                  :aggregate-effects #{} :aggregate-capabilities #{}}
          path [:functions 'main :body 0]
          fn-type {:params [:gravity/bool] :return :gravity/bool
                   :latent-effects #{} :capabilities #{} :throws #{}
                   :ownership-constraints #{:persistent-immutable-shareable}
                   :profile-constraints #{:hosted}}
          declaration (:resolved-parameter core)]
      (p15-s23-c11-mir-require!
       (and (vector? nodes)
            (= [:parameter :local :truthy :literal :literal :if :function]
               (mapv :source-operation nodes))
            (= 7 (count (set ids))) (= 'main (:entrypoint core))
            (= [(last ids)] (:root-node-ids core))
            (= [:gravity/bool :gravity/bool :gravity/bool :gravity/bool :gravity/bool :gravity/bool]
               (mapv :type (butlast nodes)))
            (= fn-type (:type root))
            (= [[:functions 'main :parameters 0 :binder] (conj path :test) (conj path :truthy)
                (conj path :then) (conj path :else) path [:functions 'main]] (mapv :path nodes))
            (= [[] [(ids 0)] [(ids 1)] [] [] [(ids 2) (ids 3) (ids 4)] [(ids 5)]]
               (mapv :operands nodes)))
       "C11-MODULE" source-path core :single-bool-entry-parameter-diamond)
      (p15-s23-c11-mir-require!
       (and (p15-s23-c11-mir-parameter-declaration-valid? core binder root)
            (= attrs (assoc purity :name name :parameter-index 0
                            :source-form-id (:source-form-id attrs)
                            :resolved-binding-id (get-in declaration [:parameter-binding :binding-id])
                            :type-binding-id (get-in declaration [:type-binding :binding-id])))
            (= (:attributes local) (assoc purity :name name :resolved-binding (ids 0)))
            (= (:attributes truthy) (assoc purity :false-values [nil false] :result-type :gravity/bool))
            (= (:attributes conditional) (assoc purity :truthiness :nil-and-false-only))
            (= (:attributes root) (assoc purity :name 'main :params [name] :arity 1
                                        :body-count 1 :visibility :public :entrypoint? true))
            (= [{:name name :parameter-index 0 :source-form-id (:source-form-id attrs)
                 :resolved-binding-id (:resolved-binding-id attrs) :type-binding-id (:type-binding-id attrs)
                 :binding-node-id (ids 0) :path (:path binder)}] (:lexical-binding-records core)))
       "C11-ORIGIN" source-path core :resolved-bool-entry-parameter-declaration)
      (p15-s23-c11-mir-require!
       (and (= 7 (count (set (map #(get-in % [:source :origin-id]) nodes))))
            (every? true? (map-indexed #(p15-s23-c11-mir-parameter-source-valid? core %2 %1) nodes)))
       "C11-ORIGIN" source-path core :entry-parameter-original-source-closure)
      (p15-s23-c11-mir-require!
       (p15-s23-c11-mir-parameter-facts-valid? core binder)
       "C11-SAFETY" source-path binder :entry-parameter-checked-core-semantic-facts)
      {:node binder :id (ids 0) :entry-id (str (:artifact-id core) ":mir:entry")
       :definition {:value-id (ids 0) :parameter-index 0 :block-id (str (:artifact-id core) ":mir:entry")}})))
