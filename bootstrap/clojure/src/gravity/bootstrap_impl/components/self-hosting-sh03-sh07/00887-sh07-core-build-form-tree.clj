

(defn sh07-core-build-form-tree
  [source-path source-revision-id decimal-evidence syntax trace]
  (let [root-id (:sh07/root-syntax-id syntax)
        source-span (sh07-core-semantic-span (:span syntax))
        trace? (and trace (= root-id (:output-def-syntax-id trace)))
        records (atom [])]
    (letfn [(walk [value path parent-form-id]
              (let [kind (sh07-core-value-kind value)
                    syntax-id
                    (sh07-core-projected-syntax-id
                     source-revision-id root-id path kind
                     (when trace? trace))
                    form-id
                    (sh07-core-projected-form-id
                     source-revision-id root-id path kind)
                    children (sh07-core-children value)
                    child-records
                    (mapv
                     (fn [index child]
                       (walk child (conj path index) form-id))
                     (range) children)
                    generated-origin
                    (cond
                      (and trace? (empty? path))
                      [{:origin-id (:def-generated-origin-id trace)
                        :kind :macro-expansion
                        :from-syntax-id (:input-syntax-id trace)
                        :role :introduced-def}]
                      (and trace? (= path [2]))
                      [{:origin-id (:fn-generated-origin-id trace)
                        :kind :macro-expansion
                        :from-syntax-id (:input-syntax-id trace)
                        :role :introduced-fn}]
                      :else [])
                    record
                    {:form-id form-id
                     :syntax-id syntax-id
                     :kind kind
                     :value
                     (sh07-core-neutral-value
                      source-path source-revision-id root-id path
                      decimal-evidence value)
                     :parent-form-id parent-form-id
                     :child-form-ids (mapv :form-id child-records)
                     :source-span source-span
                     :origin-chain []
                     :generated-origin generated-origin
                     :metadata
                     (sh05-path-neutral-semantic-value
                      (or (meta value) {}))
                     :scope-id nil}]
                (swap! records conj record)
                record))]
      (let [root (walk (:form syntax) [] nil)]
        {:root root :records @records}))))

(defn sh07-core-parameter-binding-paths
  [parameters parameter-path]
  (loop [remaining (seq (if (sequential? parameters) parameters []))
         index 0
         result []]
    (if (empty? remaining)
      result
      (let [item (first remaining)]
        (cond
          (= item ':-)
          (recur (nnext remaining) (+ index 2) result)

          (= item '&)
          (recur (next remaining) (inc index) result)

          (symbol? item)
          (recur
           (next remaining)
           (inc index)
           (conj result
                 {:name item
                  :path (conj parameter-path index)}))

          :else
          (recur (next remaining) (inc index) result))))))