(ns gravity.bootstrap-impl.loader
  "Loads the ordered compatibility components into `gravity.bootstrap`.")

(def ^:private original-source-name "gravity/bootstrap.clj")
(def ^:private data-state (atom {}))

(defn- resource-text
  [resource]
  (let [url (clojure.java.io/resource resource)]
    (when-not url
      (throw (ex-info "bootstrap component resource is missing"
                      {:resource resource})))
    (slurp url)))

(defn begin-data!
  [data-id kind]
  (swap! data-state assoc data-id {:kind kind :items []}))

(defn append-data!
  [data-id resource]
  (let [items (clojure.edn/read-string {:readers {}} (resource-text resource))]
    (when-not (vector? items)
      (throw (ex-info "bootstrap data shard must contain a vector"
                      {:data-id data-id :resource resource})))
    (swap! data-state update-in [data-id :items] into items)))

(defn finish-data!
  [data-id]
  (let [{:keys [kind items] :as state} (get @data-state data-id)]
    (when-not state
      (throw (ex-info "bootstrap data assembly is missing"
                      {:data-id data-id})))
    (swap! data-state dissoc data-id)
    (case kind
      :hash-map (into {} items)
      :vector (vec items)
      (throw (ex-info "unknown bootstrap data collection kind"
                      {:data-id data-id :kind kind})))))

(defn- read-manifest
  [resource]
  (clojure.edn/read-string {:readers {}} (resource-text resource)))

(defn- load-component!
  [{:keys [resource reader-line metadata-symbol metadata-line
           metadata-column]}]
  (let [reader (clojure.lang.LineNumberingPushbackReader.
                (java.io.StringReader. (resource-text resource)))]
    (.setLineNumber reader reader-line)
    (binding [*ns* (the-ns 'gravity.bootstrap)]
      (clojure.lang.Compiler/load
       reader original-source-name original-source-name))
    (when metadata-symbol
      (when-let [owner-var (ns-resolve 'gravity.bootstrap metadata-symbol)]
        (alter-meta! owner-var assoc
                     :line metadata-line :column metadata-column)))))

(defn load-components!
  [manifest-resource]
  (doseq [component (read-manifest manifest-resource)]
    (load-component! component))
  (when (seq @data-state)
    (throw (ex-info "bootstrap data assembly was not completed"
                    {:remaining-data-count (count @data-state)}))))
