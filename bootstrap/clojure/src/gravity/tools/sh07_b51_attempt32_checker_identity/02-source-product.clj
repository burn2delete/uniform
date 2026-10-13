(defn source-product
  "Expand ordered literal loads using authenticated C-object bytes only."
  ([source-authority relative] (source-product source-authority relative #{}))
  ([source-authority relative active]
   (when (contains? active relative)
     (throw (ex-info "checker source load cycle" {:path relative})))
   (let [forms (read-forms (:bytes (source-object! source-authority relative)))
         active (conj active relative)]
     (reduce
      (fn [product form]
        (if (and (seq? form) (contains? #{'load 'clojure.core/load} (first form)))
          (reduce
           (fn [product resource]
             (when-not (and (string? resource) (str/starts-with? resource "/")
                            (safe-relative-path? (subs resource 1)))
               (throw (ex-info "checker source load must name an absolute resource"
                               {:path relative :resource resource})))
             (let [path (str "bootstrap/clojure/src/" (subs resource 1) ".clj")
                   loaded (source-product source-authority path active)]
               (when (some #(and (seq? %) (= 'ns (first %))) (:forms loaded))
                 (throw (ex-info "implementation fragment changes namespace" {:path path})))
               (-> product
                   (update :forms into (:forms loaded))
                   (update :records into
                           (cons (local-source-record source-authority path)
                                 (:records loaded))))))
           product (rest form))
          (update product :forms conj form)))
      {:forms [] :records []} forms))))

(defn- dependency-manifest [source-authority {:keys [forms records]}]
  (loop [pending (source-dependencies forms)
         seen #{} local (into {} (map (juxt :relative-path identity) records)) external {}]
    (if (empty? pending)
      (let [bootstrap? (contains? seen 'gravity.bootstrap)
            component-paths
            (if bootstrap? (load-order-paths source-authority) [])
            local (reduce #(assoc %1 %2
                                  (local-source-record source-authority %2))
                          local component-paths)]
        {:local-source-records (mapv val (sort-by key local))
         :external-runtime-records (mapv val (sort-by (comp str key) external))})
          (let [namespace (first pending) remaining (subvec (vec pending) 1)]
        (if (contains? seen namespace)
          (recur remaining seen local external)
          (let [relative (namespace-relative-path namespace)
                source-record (get (:source-objects source-authority) relative)]
            (if source-record
              (let [bytes (:bytes (source-object! source-authority relative))
                    product (source-product source-authority relative)]
                (recur (into remaining (source-dependencies (:forms product)))
                       (conj seen namespace)
                       (into (assoc local relative (local-source-record source-authority relative))
                             (map (juxt :relative-path identity) (:records product)))
                       external))
              (recur remaining (conj seen namespace) local
                     (assoc external namespace
                            (resource-record source-authority namespace))))))))))

(defn source-closure-manifest
  "Bind entry sources and dependencies from authenticated C-object bytes only."
  [source-authority relative-paths]
  (when-not
   (and (map? source-authority)
        (= #{:content :source-objects :external-source-records}
           (set (keys source-authority)))
        (map? (:source-objects source-authority))
        (map? (:external-source-records source-authority)))
    (throw (ex-info "invalid C-derived checker source authority" {})))
  (let [roots
        (mapv (fn [relative]
                (let [bytes (:bytes (source-object!
                                     source-authority relative))]
                  {:record (local-source-record source-authority relative)
                   :product (source-product source-authority relative)}))
              relative-paths)
        dependencies
        (mapv #(dependency-manifest source-authority (:product %)) roots)
        local (->> (concat (map :record roots)
                           (mapcat :local-source-records dependencies))
                   (reduce (fn [records record]
                             (assoc records (:relative-path record) record)) {})
                   vals
                   (sort-by :relative-path)
                   vec)
        external (->> (mapcat :external-runtime-records dependencies)
                      (reduce (fn [records record]
                                (assoc records (:namespace record) record)) {})
                      vals
                      (sort-by (comp str :namespace))
                      vec)]
    {:local-source-records local :external-runtime-records external}))

(defn loaded-source-fragments
  "Bind implementation files to their actual enclosing loaded namespace."
  [source-authority namespace-records]
  (->> namespace-records
       (mapcat
        (fn [{:keys [namespace relative-path logical-coordinate]}]
          (let [product (source-product source-authority relative-path)]
            (when-not (= namespace (second (first (:forms product))))
              (throw (ex-info "fragment owner differs from enclosing namespace"
                              {:path relative-path :namespace namespace})))
          (for [record (concat (:records product)
                               (when (= namespace 'gravity.bootstrap)
                                 (map #(local-source-record source-authority %)
                                      (filter #(str/ends-with? % ".clj")
                                              (load-order-paths source-authority)))))
                :let [path (:relative-path record)
                      object (source-object! source-authority path)]]
            {:owner-namespace namespace :logical-coordinate logical-coordinate
             :relative-path path :blob-oid (:blob-oid object)
             :byte-count (:byte-count object) :sha256 (:raw-sha256 object)}))))
       (reduce #(assoc %1 [(:owner-namespace %2) (:relative-path %2)] %2) {})
       vals
       (sort-by (juxt (comp str :owner-namespace) :relative-path))
       vec))
