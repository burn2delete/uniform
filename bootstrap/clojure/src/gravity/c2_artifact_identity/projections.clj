(ns gravity.c2-artifact-identity.projections)

(defn semantic-form-hash-input [form-tree]
  (mapv #(select-keys % [:form-id :kind :collection-kind :children :parent-form-id
                         :abbrev :tag :value :metadata]) form-tree))

(defn path-neutral-span [span]
  (if (map? span) (dissoc span :source) span))

(defn token-hash-input [path-neutral-span token-stream]
  (mapv #(-> % (dissoc :source-path) (update :span path-neutral-span)) token-stream))

(defn form-hash-input [path-neutral-span form-tree]
  (mapv (fn [form]
          (-> form
              (dissoc :source-path)
              (update :span path-neutral-span)
              (update :surface-span path-neutral-span)
              (update :origin #(when % (dissoc % :source-path)))
              (update :generated-origin
                      #(mapv (fn [origin]
                               (update origin :from path-neutral-span))
                             (or % [])))))
        form-tree))

(defn syntax-seed-hash-input [path-neutral-span syntax-seeds]
  (mapv (fn [seed]
          (cond-> (update seed :span path-neutral-span)
            (contains? seed :generated-origin)
            (update :generated-origin
                    #(mapv (fn [origin]
                             (cond-> origin
                               (contains? origin :from)
                               (update :from path-neutral-span)))
                           %))))
        syntax-seeds))

(defn extension-hash-input [extension-invocations]
  (let [semantic-span #(if (map? %) (dissoc % :source :file) %)]
    (mapv (fn [invocation]
            (cond-> (dissoc invocation :source-path)
              (contains? invocation :span) (update :span semantic-span)
              (contains? invocation :invocations)
              (update :invocations
                      #(mapv (fn [record]
                               (cond-> record
                                 (contains? record :span)
                                 (update :span semantic-span)))
                             %))))
          extension-invocations)))

(defn diagnostic-hash-input [path-neutral-span diagnostics]
  (mapv (fn [diagnostic]
          (cond-> diagnostic
            (contains? diagnostic :source-span)
            (update :source-span path-neutral-span)
            (get-in diagnostic [:primary :span])
            (update-in [:primary :span] path-neutral-span)
            (contains? diagnostic :related)
            (update :related
                    #(mapv (fn [related]
                             (cond-> related
                               (contains? related :span)
                               (update :span path-neutral-span)))
                           %))
            (contains? diagnostic :origin-chain)
            (update :origin-chain
                    #(mapv (fn [origin]
                             (cond-> (dissoc origin :path)
                               (contains? origin :span)
                               (update :span path-neutral-span)))
                           %))))
        diagnostics))
