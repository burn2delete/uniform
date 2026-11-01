
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

(defn- safe-relative-path? [value]
  (and (string? value) (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (not-any? #{"" "." ".."} (str/split value #"/"))))

(defn- source-object!
  [source-authority relative]
  (when-not (safe-relative-path? relative)
    (throw (ex-info "unsafe checker source path" {:path relative})))
  (let [record (get (:source-objects source-authority) relative)
        bytes (:bytes record)
        expected-keys #{:path :mode :blob-oid :byte-count :raw-sha256 :bytes}]
    (when-not
     (and (= expected-keys (set (keys record)))
          (= relative (:path record))
          (= "100644" (:mode record))
          (boolean (re-matches #"[0-9a-f]{40}" (or (:blob-oid record) "")))
          (bytes? bytes)
          (= (:byte-count record) (alength ^bytes bytes))
          (= (:raw-sha256 record) (sha256-bytes bytes)))
      (throw (ex-info "C-derived checker source object is unavailable"
                      {:path relative})))
    record))

#_{:clj-kondo/ignore [:unused-private-var]}
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

(defn- local-source-record [source-authority relative]
  (let [record (source-object! source-authority relative)]
    {:relative-path relative
     :byte-count (:byte-count record)
     :source-sha256 (:raw-sha256 record)}))

(defn- resource-record [source-authority namespace]
  (or (get (:external-source-records source-authority) namespace)
      {:namespace namespace :resource :host-provided}))

(defn- load-order-paths [source-authority]
  (let [relative "bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn"
        entries (edn/read-string {:readers {}}
                                 (String. (:bytes (source-object!
                                                  source-authority relative))
                                          StandardCharsets/UTF_8))
        component-paths
        (->> entries
             (remove :data-resource)
             (mapv #(str "bootstrap/clojure/src/" (:resource %))))
        data-resources
        (->> entries
             (keep (fn [{:keys [data-resource resource]}]
                     (when data-resource resource)))
             distinct
             sort
             vec)]
    (when-not (= 118 (count data-resources))
      (throw (ex-info "bootstrap loader data-resource closure is incomplete"
                      {:expected 118 :actual (count data-resources)})))
    (vec
     (concat [relative]
             component-paths
             (map #(str "bootstrap/clojure/src/" %) data-resources)))))
