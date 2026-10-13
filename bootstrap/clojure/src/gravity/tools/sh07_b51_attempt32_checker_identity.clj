(ns gravity.tools.sh07-b51-attempt32-checker-identity
  "Deterministic checker identities for the governed SH-07 Attempt-32 snapshot."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [gravity.c2-artifact-identity.canonical :as canonical]
            [gravity.digest :as digest])
  (:import [clojure.lang LineNumberingPushbackReader]
           [java.io ByteArrayInputStream InputStreamReader]
           [java.nio.charset StandardCharsets]))

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
        (mapv #(str "bootstrap/clojure/src/" (:resource %)) entries)
        data-resources
        (->> component-paths
             (mapcat
              (fn [path]
                (let [forms (read-forms
                             (:bytes (source-object! source-authority path)))]
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
     (concat [relative]
             component-paths
             (map #(str "bootstrap/clojure/src/" %) data-resources)))))

(defn- dependency-manifest [source-authority checker-forms]
  (loop [pending (source-dependencies checker-forms)
         seen #{} local {} external {}]
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
                    forms (read-forms bytes)]
                (recur (into remaining (source-dependencies forms))
                       (conj seen namespace)
                       (assoc local relative
                              (local-source-record source-authority relative))
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
                   :forms (read-forms bytes)}))
              relative-paths)
        dependencies
        (mapv #(dependency-manifest source-authority (:forms %)) roots)
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

(defn checker-identity-manifest
  "Derive one role identity from bytes already authenticated against C/T.

  The caller must pass the internal source-authority returned by the child
  classpath preflight.  A checkout path, classpath resource lookup, or missing
  C object is deliberately not accepted as a fallback."
  [source-authority role]
  (let [{:keys [algorithm-role namespace entrypoint source-relative-path domains]}
        (or (get checker-specs role)
            (throw (ex-info "unknown SH-07 checker role" {:role role})))
        _ (when-not
           (and (map? source-authority)
                (= #{:commit :tree} (set (keys (:content source-authority))))
                (map? (:source-objects source-authority))
                (map? (:external-source-records source-authority)))
            (throw (ex-info "missing authenticated C-derived source input"
                            {:role role})))
        bytes (:bytes (source-object! source-authority source-relative-path))
        forms (read-forms bytes)
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
        dependencies (dependency-manifest source-authority forms)
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
