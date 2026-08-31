

(defn p15-s23-reference-runtime-contract-definitions
  [source-path target forms]
  (let [definition-names
        (vec (for [form forms
                   :when (and (seq? form) (= 'def (first form)))]
               (second form)))
        duplicate
        (first (for [[name n] (frequencies definition-names)
                     :when (> n 1)]
                 name))
        observed (set definition-names)]
    (when (or duplicate
              (not= (count p15-s23-reference-runtime-contract-definition-names)
                    (count definition-names))
              (not= p15-s23-reference-runtime-contract-definition-names
                    observed))
      (p15-s23-reference-runtime-fail!
       source-path target :runtime-contract-definition-set definition-names
       {:duplicate-definition duplicate
        :expected-definitions
        p15-s23-reference-runtime-contract-definition-names
        :observed-definitions observed}))
    (let [definitions
          (into (sorted-map)
                (map (fn [name]
                       [name (p15-s23-compiler-def-value
                              source-path forms name)]))
                definition-names)
          malformed
          (first (for [[name value] definitions
                       :when (not (map? value))]
                   {:definition name :value value}))
          _ (doseq [[name value] definitions]
              (p15-s23-reference-runtime-bounded-value!
               source-path target name value))
          definition-hash (p15-s23-reference-runtime-hash definitions)]
      (when malformed
        (p15-s23-reference-runtime-fail!
         source-path target :runtime-contract-definition-shape malformed
         {:expected-type :map}))
      (when-not (= p15-s23-reference-runtime-expected-contract-definition-hash
                   definition-hash)
        (p15-s23-reference-runtime-fail!
         source-path target :runtime-contract-definition-hash definitions
         {:expected-contract-definition-hash
          p15-s23-reference-runtime-expected-contract-definition-hash
          :observed-contract-definition-hash definition-hash}))
      {:definitions definitions
       :definition-hash definition-hash
       :definition-names definition-names})))

(defn p15-s23-reference-runtime-instruction-children
  [source-path target function-name path instruction]
  (let [fail-shape
        (fn [missing value]
          (p15-s23-reference-runtime-fail!
           source-path target missing value
           {:runtime-function function-name :instruction-path path}))
        child-records
        (fn [field values]
          (when-not (vector? values)
            (fail-shape :runtime-contract-instruction-vector values))
          (mapv (fn [index value]
                  {:path (conj path field index) :instruction value})
                (range) values))
        op (:op instruction)]
    (case op
      (:literal :quote :local) []
      (:builtin-call :function-call :println)
      (child-records :args (:args instruction))
      :vector-literal
      (child-records :items (:items instruction))
      :set-literal
      (child-records :items (:items instruction))
      :map-literal
      (let [entries (:entries instruction)]
        (when-not (vector? entries)
          (fail-shape :runtime-contract-map-entry-vector entries))
        (vec
         (mapcat
          (fn [index entry]
            (when-not (and (map? entry)
                           (contains? entry :key)
                           (contains? entry :value))
              (fail-shape :runtime-contract-map-entry entry))
            [{:path (conj path :entries index :key)
              :instruction (:key entry)}
             {:path (conj path :entries index :value)
              :instruction (:value entry)}])
          (range) entries)))
      :do (child-records :body (:body instruction))
      :if
      (let [children [[:test (:test instruction)]
                      [:then (:then instruction)]
                      [:else (:else instruction)]]]
        (when-not (every? (comp map? second) children)
          (fail-shape :runtime-contract-if-children instruction))
        (mapv (fn [[field value]]
                {:path (conj path field) :instruction value})
              children))
      :let
      (let [bindings (:bindings instruction)
            body (:body instruction)]
        (when-not (and (vector? bindings)
                       (vector? body)
                       (every? #(and (map? %)
                                     (map? (:expr %)))
                               bindings))
          (fail-shape :runtime-contract-let-shape instruction))
        (vec
         (concat
          (map-indexed
           (fn [index binding]
             {:path (conj path :bindings index :expr)
              :instruction (:expr binding)})
           bindings)
          (map-indexed
           (fn [index value]
             {:path (conj path :body index) :instruction value})
           body))))
      (fail-shape :runtime-contract-instruction-operation instruction))))

(defn p15-s23-reference-runtime-function-operation-records
  [source-path target function-name definition]
  (let [instructions (:instructions definition)]
    (when-not (and (map? definition) (vector? instructions))
      (p15-s23-reference-runtime-fail!
       source-path target :runtime-contract-function-shape definition
       {:runtime-function function-name}))
    (loop [pending
           (vec
            (reverse
             (map-indexed
              (fn [index instruction]
                {:path [:instructions index]
                 :instruction instruction
                 :depth 1})
              instructions)))
           visited 0
           records []]
      (if-let [{:keys [path instruction depth]} (peek pending)]
        (let [pending (pop pending)
              visited (inc visited)]
          (when (or (> visited p15-s23-reference-runtime-max-contract-nodes)
                    (> depth
                       p15-s23-reference-runtime-max-instruction-depth))
            (p15-s23-reference-runtime-fail!
             source-path target :runtime-contract-instruction-bounds
             instruction
             {:runtime-function function-name
              :instruction-path path
              :observed-nodes visited
              :observed-depth depth
              :maximum-nodes
              p15-s23-reference-runtime-max-contract-nodes
              :maximum-depth
              p15-s23-reference-runtime-max-instruction-depth}))
          (when-not (map? instruction)
            (p15-s23-reference-runtime-fail!
             source-path target :runtime-contract-instruction-shape
             instruction
             {:runtime-function function-name :instruction-path path}))
          (let [children
                (p15-s23-reference-runtime-instruction-children
                 source-path target function-name path instruction)
                record
                (cond-> {:function function-name
                         :path path
                         :op (:op instruction)}
                  (= :function-call (:op instruction))
                  (assoc :callee (:function instruction))
                  (= :builtin-call (:op instruction))
                  (assoc :builtin (:function instruction)))]
            (recur
             (into pending
                   (map #(assoc % :depth (inc depth))
                        (reverse children)))
             visited
             (conj records record))))
        records))))

(defn p15-s23-reference-runtime-operation-records
  [source-path target plan]
  (let [functions (:functions plan)]
    (when-not (and (map? functions)
                   (= p15-s23-reference-runtime-function-set
                      (set (keys functions))))
      (p15-s23-reference-runtime-fail!
       source-path target :runtime-contract-function-set functions
       {:expected-functions p15-s23-reference-runtime-function-set
        :observed-functions (when (map? functions)
                              (set (keys functions)))}))
    (into (sorted-map)
          (map (fn [function-name]
                 [function-name
                  (p15-s23-reference-runtime-function-operation-records
                   source-path target function-name
                   (get functions function-name))]))
          (sort-by pr-str (keys functions)))))

(defn p15-s23-reference-runtime-allocation-kind
  [record]
  (case (:op record)
    :vector-literal :vector
    :map-literal :map
    :builtin-call
    (case (:builtin record)
      str :str
      conj :conj
      assoc :assoc
      rest :rest
      nil)
    nil))