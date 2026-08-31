

(defn p15-s23-closed-core-form-indexes
  [source-path form-tree root-form-ids]
  (when-not (and (vector? form-tree)
                 (vector? root-form-ids)
                 (<= (count form-tree) 2048))
    (p15-s23-closed-core-fail!
     "C6-VERIFY" source-path {:missing-fact :bounded-c2-form-tree}
     {:observed-form-count (when (vector? form-tree) (count form-tree))
      :maximum-form-count 2048}))
  (let [form-by-id (into {} (map (juxt :form-id identity)) form-tree)
        ordinal-by-id (into {} (map-indexed (fn [idx form]
                                             [(:form-id form) idx]))
                            form-tree)]
    (loop [pending (vec (map-indexed (fn [idx form-id]
                                      {:form-id form-id
                                       :structural-path [idx]})
                                    root-form-ids))
           structural-path-by-id {}
           parent-by-id {}
           visited #{}]
      (if-let [{:keys [form-id structural-path parent-form-id]}
               (peek pending)]
        (let [pending (pop pending)
              form (get form-by-id form-id)]
          (when (or (nil? form) (contains? visited form-id))
            (p15-s23-closed-core-fail!
             "C6-ORIGIN" source-path form
             {:missing-fact :c2-structural-form-closure
              :form-id form-id}))
          (let [children (:children form)
                frames (map-indexed
                        (fn [idx child-id]
                          {:form-id child-id
                           :parent-form-id form-id
                           :structural-path (conj structural-path idx)})
                        children)]
            (recur (into pending (reverse frames))
                   (assoc structural-path-by-id form-id structural-path)
                   (cond-> parent-by-id
                     parent-form-id (assoc form-id parent-form-id))
                   (conj visited form-id))))
        {:form-by-id form-by-id
         :ordinal-by-id ordinal-by-id
         :structural-path-by-id structural-path-by-id
         :parent-by-id parent-by-id}))))

(defn p15-s23-closed-core-top-level-form-id
  [form-id parent-by-id]
  (loop [current form-id
         seen #{}]
    (let [parent (get parent-by-id current)]
      (cond
        (nil? parent) current
        (contains? seen current) nil
        :else (recur parent (conj seen current))))))

(defn p15-s23-closed-core-function-form?
  [entrypoint value]
  (and (seq? value)
       (= 'defn (first value))
       (= entrypoint (second value))))

(defn p15-s23-closed-core-function-source-shape
  [source-path entrypoint form-by-id root-record]
  (let [value (:value root-record)
        children (:children root-record)]
    (cond
      (and (seq? value) (= 'defn (first value)))
      (let [params-form (get form-by-id (nth children 2 nil))]
        (when-not (and (= entrypoint (second value))
                       (vector? (:value params-form))
                       (empty? (:value params-form)))
          (p15-s23-closed-core-fail!
           "C6-CORE-SHAPE" source-path root-record
           {:missing-fact :closed-entrypoint-source-shape}))
        {:root-form-id (:form-id root-record)
         :body-form-ids (vec (drop 3 children))
         :params-form-id (:form-id params-form)})

      :else
      (p15-s23-closed-core-fail!
       "C6-CORE-SHAPE" source-path root-record
       {:missing-fact :closed-entrypoint-defn-source-form
        :excluded-source-forms [:def-fn :def :defmacro]}))))

(defn p15-s23-closed-core-syntax-semantic-input
  [syntax]
  {:form-kind (get-in syntax [:form :kind])
   :form-hash
   (p15-s23-closed-core-digest (get-in syntax [:form :value]))
   :span (p15-s23-closed-core-path-neutral-span
          (get-in syntax [:span :primary]))
   :namespace (:namespace syntax)
   :phase (:phase syntax)
   :profile (:profile syntax)
   :metadata (:metadata syntax)
   :hygiene (:hygiene syntax)
   :origin (mapv p15-s23-closed-core-path-neutral-generated-origin
                 (:origin syntax))
   :version (:version syntax)})

(defn p15-s23-closed-core-c2-semantic-input
  [front-end]
  {:source-content-hash (:source-id front-end)
   :tokens
   (mapv (fn [token]
           {:token-id (:token-id token)
            :kind (:kind token)
            :raw (:raw token)
            :span (p15-s23-closed-core-path-neutral-span (:span token))
            :trivia? (:trivia? token)})
         (:token-stream front-end))
   :forms
   (mapv (fn [form]
           {:form-id (:form-id form)
            :kind (:kind form)
            :collection-kind (:collection-kind form)
            :children (:children form)
            :parent-form-id (:parent-form-id form)
            :abbrev (:abbrev form)
            :tag (:tag form)
            :raw-hash (str "sha256:" (sha256-hex (:raw form)))
            :value (:value form)
            :metadata (:metadata form)
            :span (p15-s23-closed-core-path-neutral-span (:span form))
            :surface-span
            (p15-s23-closed-core-path-neutral-span (:surface-span form))})
         (:form-tree front-end))
   :top-level-form-ids (:top-level-form-ids front-end)
   :syntax-seeds
   (mapv (fn [seed]
           {:syntax-id (:syntax-id seed)
            :form-id (:form-id seed)
            :span (p15-s23-closed-core-path-neutral-span (:span seed))
            :metadata (:metadata seed)
            :reader-origin (:reader-origin seed)
            :generated-origin
            (mapv p15-s23-closed-core-path-neutral-generated-origin
                  (:generated-origin seed))})
         (:syntax-seed-stream front-end))})

(defn p15-s23-closed-core-c3-semantic-input
  [front-end]
  (mapv p15-s23-closed-core-syntax-semantic-input
        (:c3-syntax-object-stream front-end)))