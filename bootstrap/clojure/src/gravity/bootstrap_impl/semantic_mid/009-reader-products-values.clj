(defn- semantic-mid-reader-literal-set-key?
  [{:keys [nodes] :as context} node]
  (or (contains? #{:nil :boolean :integer :ratio :decimal
                   :string :character :symbol :keyword
                   :tagged-literal}
                 (:kind node))
      (and (contains? #{:list :vector :map :set
                        :abbreviation :metadata-wrapper}
                      (:kind node))
           (every? #(semantic-mid-reader-literal-set-key?
                     context (@nodes %))
                   (:children node)))))

(defn- semantic-mid-reader-set-value
  [{:keys [source-path nodes] :as context} child-ids values]
  (loop [pairs (map vector child-ids values)
         seen {}]
    (if-let [[child-id value] (first pairs)]
      (let [node (@nodes child-id)]
        (if (and (semantic-mid-reader-literal-set-key? context node)
                 (contains? seen value))
          (let [first-id (seen value)
                first-node (@nodes first-id)]
            (stage1-reader-fail!
             "C2-SET" source-path value
             {:source-span (:span node)
              :token-id (:open-token node)
              :form-id child-id
              :raw (:raw node)
              :related [{:role :first-literal
                         :span (:span first-node)
                         :artifact first-id}]
              :facts {:duplicate-value (pr-str value)
                      :first-form-id first-id
                      :duplicate-form-id child-id}}))
          (recur (rest pairs)
                 (if (semantic-mid-reader-literal-set-key? context node)
                   (assoc seen value child-id)
                   seen))))
      (set values))))

(defn- semantic-mid-reader-metadata-map
  [{:keys [source-path]} value node]
  (cond
    (map? value) value
    (keyword? value) {value true}
    (or (symbol? value) (string? value)) {:tag value}
    :else
    (stage1-reader-fail!
     "C2-METADATA" source-path value
     {:source-span (:span node)
      :token-id (:open-token node)
      :form-id (:form-id node)
      :raw (:raw node)
      :facts {:metadata-kind (:kind node)}})))

(defn- semantic-mid-reader-attach-metadata
  [{:keys [source-path]} metadata target target-node]
  (if (instance? clojure.lang.IObj target)
    (with-meta target (merge (meta target) metadata))
    (stage1-reader-fail!
     "C2-METADATA" source-path target
     {:source-span (:span target-node)
      :token-id (:open-token target-node)
      :form-id (:form-id target-node)
      :raw (:raw target-node)
      :facts {:target-kind (:kind target-node)}})))

(declare semantic-mid-reader-read-form)
