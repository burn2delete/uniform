
(def checker-specs
  {:producer-census
   {:algorithm-role :source-first-exhaustive
    :namespace 'gravity.tools.sh07-b51-attempt30-producer-census
    :entrypoint 'gravity.tools.sh07-b51-attempt30-producer-census/produce-certificate
    :source-relative-path "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_producer_census.clj"
    :domains
    {:artifact :gravity/sh07-b51-producer-census-checker-artifact-id-v1
     :semantic :gravity/sh07-b51-producer-census-checker-semantic-id-v1
     :dependency :gravity/sh07-b51-producer-census-checker-dependency-closure-id-v1
     :executable :gravity/sh07-b51-producer-census-checker-executable-id-v1
     :manifest :gravity/sh07-b51-producer-census-checker-identity-manifest-v1}}
   :independent-verifier-census
   {:algorithm-role :m2-first-independent-exhaustive
    :namespace 'gravity.tools.sh07-b51-attempt30-independent-census
    :entrypoint 'gravity.tools.sh07-b51-attempt30-independent-census/produce-certificate
    :source-relative-path "bootstrap/clojure/src/gravity/tools/sh07_b51_attempt30_independent_census.clj"
    :domains
    {:artifact
     :gravity/sh07-b51-independent-census-checker-artifact-id-v1
     :semantic
     :gravity/sh07-b51-independent-census-checker-semantic-id-v1
     :dependency
     :gravity/sh07-b51-independent-census-checker-dependency-closure-id-v1
     :executable
     :gravity/sh07-b51-independent-census-checker-executable-id-v1
     :manifest
     :gravity/sh07-b51-independent-census-checker-identity-manifest-v1}}})

(defn canonical-value [value]
  (canonical/reader-canonical-value canonical-value value))

(defn canonical-id [value]
  (canonical/reader-canonical-hash digest/sha256-hex canonical-value value))

(defn- sha256-bytes [bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn- read-bytes [^Path path]
  (Files/readAllBytes path))

(defn- safe-relative-path? [value]
  (and (string? value) (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not-any? #{"" "." ".."} (str/split value #"/"))))

(defn symlink-free-path?
  "Return true only when the absolute path and every ancestor are not links."
  [^Path path]
  (loop [current (.normalize (.toAbsolutePath path))]
    (cond
      (nil? current) true
      (Files/isSymbolicLink current) false
      :else (recur (.getParent current)))))

(defn- resolve-file [^Path root relative]
  (when-not (safe-relative-path? relative)
    (throw (ex-info "unsafe checker source path" {:path relative})))
  (let [path (.normalize (.resolve root relative))]
    (when-not (and (.startsWith path root)
                   (Files/isRegularFile
                    path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                   (symlink-free-path? path))
      (throw (ex-info "checker dependency source is unavailable"
                      {:path relative})))
    path))

(defn- read-forms [bytes]
  (with-open [reader
              (LineNumberingPushbackReader.
               (InputStreamReader. (ByteArrayInputStream. bytes)
                                   StandardCharsets/UTF_8))]
    (binding [*read-eval* false]
      (let [eof (Object.)]
        (loop [forms []]
          (let [form (read {:eof eof :read-cond :allow :features #{:clj}}
                           reader)]
            (if (identical? eof form) forms (recur (conj forms form)))))))))

(defn- reader-gensym-parts [value]
  (when (symbol? value)
    (let [value-name (name value)]
      (or (when-let [[_ prefix id] (re-matches
                                    #"(.+)__([0-9]+)__auto__" value-name)]
            [prefix (Long/parseLong id) :auto])
          (when-let [[_ prefix id] (re-matches
                                    #"(p[0-9]+|rest)__([0-9]+)#" value-name)]
            [prefix (Long/parseLong id) :fn-argument])))))

(defn- semantic-forms [forms]
  (let [ranks (->> (tree-seq coll? seq forms) (keep reader-gensym-parts)
                   (map second) distinct sort
                   (map-indexed (fn [rank id] [id rank])) (into {}))]
    (walk/postwalk
     (fn [value]
       (cond
         (instance? java.util.regex.Pattern value)
         [:gravity/clojure-regex (.pattern ^java.util.regex.Pattern value)
          (.flags ^java.util.regex.Pattern value)]
         (reader-gensym-parts value)
         (let [[prefix id kind] (reader-gensym-parts value)
               stable-name (str prefix "__reader_" (get ranks id) "_"
                                (name kind))]
           (with-meta (symbol (namespace value) stable-name) (meta value)))
         :else value))
     forms)))

(defn- require-spec-names [spec]
  (cond
    (symbol? spec) [spec]
    (vector? spec) (when (symbol? (first spec)) [(first spec)])
    (and (seq? spec) (symbol? (first spec)))
    (mapcat (fn [child]
              (when (symbol? child)
                [(symbol (str (first spec) "." child))]))
            (rest spec))
    :else []))

(defn- source-dependencies [forms]
  (let [ns-form (first (filter #(and (seq? %) (= 'ns (first %))) forms))
        clauses (drop 2 ns-form)
        declared
        (mapcat #(when (and (seq? %) (= :require (first %)))
                   (mapcat require-spec-names (rest %)))
                clauses)
        dynamic
        (keep (fn [form]
                (when (and (seq? form)
                           (contains? #{'require 'clojure.core/require}
                                      (first form)))
                  (let [argument (second form)]
                    (when (and (seq? argument) (= 'quote (first argument)))
                      (second argument)))))
              forms)]
    (vec (distinct (concat declared dynamic)))))

(defn- namespace-relative-path [namespace]
  (str "bootstrap/clojure/src/"
       (-> (str namespace) (str/replace "." "/") (str/replace "-" "_"))
       ".clj"))

(defn- local-source-record [root relative]
  (let [bytes (read-bytes (resolve-file root relative))
        classpath-prefix "bootstrap/clojure/src/"
        classpath-resource
        (when (str/starts-with? relative classpath-prefix)
          (subs relative (count classpath-prefix)))]
    (when classpath-resource
      (let [selected (io/resource classpath-resource)]
        (when-not selected
          (throw (ex-info "checker dependency classpath resource is unavailable"
                          {:path relative :resource classpath-resource})))
        (with-open [input (io/input-stream selected)]
          (when-not (java.util.Arrays/equals bytes (.readAllBytes input))
            (throw
             (ex-info "checker dependency classpath resource differs from checkout"
                      {:path relative :resource classpath-resource}))))))
    {:relative-path relative :byte-count (alength bytes)
     :source-sha256 (sha256-bytes bytes)}))

(defn- resource-record [namespace]
  (let [relative (-> (str namespace) (str/replace "." "/")
                     (str/replace "-" "_"))
        candidates [(str relative ".clj") (str relative ".cljc")]
        selected (first (keep #(when (io/resource %) %) candidates))]
    (if selected
      (with-open [input (io/input-stream (io/resource selected))]
        (let [bytes (.readAllBytes input)]
          {:namespace namespace :resource selected :byte-count (alength bytes)
           :source-sha256 (sha256-bytes bytes)}))
      {:namespace namespace :resource :host-provided})))

(defn- load-order-paths [root]
  (let [relative "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
        entries (edn/read-string {:readers {}}
                                 (String. (read-bytes (resolve-file root relative))
                                          StandardCharsets/UTF_8))
        component-paths
        (mapv #(str "bootstrap/clojure/src/" (:resource %)) entries)
        data-resources
        (->> component-paths
             (mapcat
              (fn [path]
                (let [forms (read-forms (read-bytes (resolve-file root path)))]
                  (->> (tree-seq coll? seq forms)
                       (keep
                        (fn [form]
                          (when (and (seq? form)
                                     (= 'gravity.bootstrap-impl.loader/append-data!
                                        (first form))
                                     (string? (nth form 2 nil)))
                            (nth form 2))))))))
             distinct
             sort
             vec)]
    (when-not (= 118 (count data-resources))
      (throw (ex-info "bootstrap loader data-resource closure is incomplete"
                      {:expected 118 :actual (count data-resources)})))
    (vec
     (concat ["deps.edn" relative]
             component-paths
             (map #(str "bootstrap/clojure/src/" %) data-resources)))))
