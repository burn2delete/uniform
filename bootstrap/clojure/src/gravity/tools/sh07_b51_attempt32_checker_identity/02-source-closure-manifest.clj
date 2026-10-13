(defn- source-product
  "Read ordered implementation files and bind every loaded file's bytes."
  ([root relative] (source-product root relative #{}))
  ([root relative active]
   (when (contains? active relative)
     (throw (ex-info "checker source load cycle" {:path relative})))
   (let [bytes (read-bytes (resolve-file root relative))
         forms (read-forms bytes)
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
                   loaded (source-product root path active)]
               (-> product
                   (update :forms into (:forms loaded))
                   (update :records into (cons (local-source-record root path)
                                               (:records loaded))))))
           product (rest form))
          (update product :forms conj form)))
      {:forms [] :records []} forms))))

(defn- dependency-manifest [root {:keys [forms records]}]
  (loop [pending (source-dependencies forms)
         seen #{} local (into {} (map (juxt :relative-path identity) records))
         external {}]
    (if (empty? pending)
      (let [bootstrap? (contains? seen 'gravity.bootstrap)
            component-paths (if bootstrap? (load-order-paths root) [])
            local (reduce #(assoc %1 %2 (local-source-record root %2))
                          local component-paths)]
        {:local-source-records (mapv val (sort-by key local))
         :external-runtime-records (mapv val (sort-by (comp str key) external))})
      (let [namespace (first pending) remaining (subvec (vec pending) 1)]
        (if (contains? seen namespace)
          (recur remaining seen local external)
          (let [relative (namespace-relative-path namespace)
                path (.resolve root relative)]
            (if (Files/isRegularFile path (make-array LinkOption 0))
              (let [product (source-product root relative)]
                (recur (into remaining (source-dependencies (:forms product)))
                       (conj seen namespace)
                       (into (assoc local relative (local-source-record root relative))
                             (map (juxt :relative-path identity) (:records product)))
                       external))
              (recur remaining (conj seen namespace) local
                     (assoc external namespace (resource-record namespace))))))))))

(defn source-closure-manifest
  "Bind entry sources and their complete transitive namespace dependencies."
  [root relative-paths]
  (let [root (.normalize (.toAbsolutePath ^Path root))
        roots
        (mapv (fn [relative]
                {:record (local-source-record root relative)
                 :product (source-product root relative)})
              relative-paths)
        dependencies (mapv #(dependency-manifest root (:product %)) roots)
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

(defn- runtime-fingerprint []
  {:clojure-version (clojure-version)
   :java-version (System/getProperty "java.version")
   :java-vendor (System/getProperty "java.vendor")
   :java-vm-name (System/getProperty "java.vm.name")
   :java-vm-version (System/getProperty "java.vm.version")
   :os-name (System/getProperty "os.name")
   :os-arch (System/getProperty "os.arch")})

(defn checker-identity-manifest [root role]
  (let [{:keys [algorithm-role namespace entrypoint source-relative-path domains]}
        (or (get checker-specs role)
            (throw (ex-info "unknown SH-07 checker role" {:role role})))
        root (.normalize (.toAbsolutePath ^Path root))
        bytes (read-bytes (resolve-file root source-relative-path))
        product (source-product root source-relative-path)
        forms (:forms product)
        source-sha (sha256-bytes bytes)
        semantic-id (canonical-id {:domain (:semantic domains) :schema-version 1
                                   :role role :namespace namespace
                                   :entrypoint entrypoint
                                   :reader-normalization
                                   :ranked-gensym-and-regex-v1
                                   :forms (semantic-forms forms)})
        artifact-id (canonical-id {:domain (:artifact domains) :schema-version 1
                                   :role role :algorithm-role algorithm-role
                                   :source-relative-path source-relative-path
                                   :source-byte-count (alength bytes)
                                   :source-sha256 source-sha})
        dependencies (dependency-manifest root product)
        runtime (runtime-fingerprint)
        closure-id (canonical-id {:domain (:dependency domains) :schema-version 1
                                  :role role :dependencies dependencies
                                  :runtime runtime})
        executable-id
        (canonical-id {:domain (:executable domains) :schema-version 1 :role role
                       :entrypoint entrypoint :artifact-id artifact-id
                       :semantic-id semantic-id :dependency-closure-id closure-id
                       :runtime runtime})
        checker {:artifact-id artifact-id :source-sha256 source-sha
                 :semantic-id semantic-id :executable-id executable-id
                 :dependency-closure-id closure-id
                 :algorithm-role algorithm-role}
        base {:artifact (:manifest domains) :schema-version 1 :role role
              :source-relative-path source-relative-path
              :source-byte-count (alength bytes) :checker checker
              :dependencies dependencies :runtime runtime}]
    (when-not (= 5 (count (set ((juxt :artifact-id :source-sha256 :semantic-id
                                           :executable-id :dependency-closure-id)
                                checker))))
      (throw (ex-info "checker identity coordinates are not distinct"
                      {:role role})))
    (assoc base :identity-manifest-id (canonical-id base))))
