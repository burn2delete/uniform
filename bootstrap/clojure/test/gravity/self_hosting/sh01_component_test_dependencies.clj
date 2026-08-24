(ns gravity.self-hosting.sh01-component-test-dependencies
  "Reviewed component-to-test dependencies for incremental development checks."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def contract-schema
  :gravity/stage0-incremental-test-dependencies-v1)

(def ^:private contract-relative-path
  "contracts/stage0-incremental-test-dependencies.edn")

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh01_component_test_dependencies.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-01 component dependency source is not on the classpath"
        {:id "SH01-COMPONENT-DEPENDENCY-CONTRACT"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH01-COMPONENT-DEPENDENCY-CONTRACT"}))

        (.isFile (.toFile (.resolve path contract-relative-path)))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- contract-error
  [message details]
  (throw
   (ex-info
    message
    (merge {:id "SH01-COMPONENT-DEPENDENCY-CONTRACT"} details))))

(defn- expected-component-id
  [source-path]
  (some-> (re-matches
           #"bootstrap/clojure/src/gravity/([^/]+)\.clj"
           source-path)
          second
          (str/replace "_" "-")))

(defn- expected-test-namespace
  [test-path]
  (some-> (re-matches
           #"bootstrap/clojure/test/(gravity/.+_test)\.clj"
           test-path)
          second
          (str/replace "_" "-")
          (str/replace "/" ".")
          symbol))

(defn validate-contract
  "Validates and returns the closed incremental component/test contract."
  [contract]
  (let [top-level-keys
        #{:schema :authority :authoritative? :components :nonclaims}
        component-keys #{:component-id :source-path :tests}
        test-keys #{:path :namespace :jvm-group}
        components (:components contract)]
    (when-not (= top-level-keys (set (keys contract)))
      (contract-error "Incremental dependency contract keys changed"
                      {:keys (vec (sort (keys contract)))}))
    (when-not (and (= contract-schema (:schema contract))
                   (= :non-authoritative (:authority contract))
                   (false? (:authoritative? contract))
                   (vector? components)
                   (seq components)
                   (vector? (:nonclaims contract))
                   (seq (:nonclaims contract))
                   (every? #(and (string? %) (not (empty? %)))
                           (:nonclaims contract)))
      (contract-error "Incremental dependency contract metadata is invalid"
                      {:schema (:schema contract)
                       :authority (:authority contract)}))
    (doseq [component components]
      (when-not (= component-keys (set (keys component)))
        (contract-error "Incremental component entry keys changed"
                        {:component component}))
      (when-not (and (string? (:component-id component))
                     (not (empty? (:component-id component)))
                     (string? (:source-path component))
                     (not (empty? (:source-path component)))
                     (vector? (:tests component))
                     (seq (:tests component)))
        (contract-error "Incremental component entry is malformed"
                        {:component component}))
      (when-not (= (:component-id component)
                   (expected-component-id (:source-path component)))
        (contract-error "Incremental component source identity is not canonical"
                        {:component-id (:component-id component)
                         :source-path (:source-path component)}))
      (doseq [test-entry (:tests component)]
        (when-not (= test-keys (set (keys test-entry)))
          (contract-error "Incremental component test entry keys changed"
                          {:component-id (:component-id component)
                           :test test-entry}))
        (when-not (and (string? (:path test-entry))
                       (not (empty? (:path test-entry)))
                       (symbol? (:namespace test-entry))
                       (string? (:jvm-group test-entry))
                       (not (empty? (:jvm-group test-entry))))
          (contract-error "Incremental component test entry is malformed"
                          {:component-id (:component-id component)
                           :test test-entry}))
        (when-not (= (:namespace test-entry)
                     (expected-test-namespace (:path test-entry)))
          (contract-error "Incremental test path and namespace disagree"
                          {:component-id (:component-id component)
                           :test test-entry}))))
    (doseq [[kind values]
            [[:component-id (map :component-id components)]
             [:source-path (map :source-path components)]
             [:test-path (mapcat #(map :path (:tests %)) components)]
             [:test-namespace
              (mapcat #(map :namespace (:tests %)) components)]]]
      (when-not (= (count values) (count (distinct values)))
        (contract-error "Incremental dependency identities must be unique"
                        {:kind kind :values (vec values)})))
    contract))

(defn read-contract
  "Reads the reviewed repository contract and fails closed on malformed EDN."
  []
  (try
    (validate-contract
     (edn/read-string
      {:readers *data-readers*}
      (slurp (.toFile (.resolve @root contract-relative-path)))))
    (catch clojure.lang.ExceptionInfo exception
      (throw exception))
    (catch Throwable throwable
      (contract-error "Incremental dependency contract could not be read"
                      {:cause (str throwable)}))))

(defn dependency-index
  "Returns deterministic source, test-path, and selectable-namespace indexes."
  ([] (dependency-index (read-contract)))
  ([contract]
   (let [components (:components (validate-contract contract))
         by-component-id
         (into
          (sorted-map)
          (map (juxt :component-id identity))
          components)
         by-source-path
         (into
          (sorted-map)
          (map (juxt :source-path identity))
          components)
         by-test-path
         (into
          (sorted-map)
          (for [component components
                test-entry (:tests component)]
            [(:path test-entry)
             (assoc test-entry :component-id (:component-id component))]))]
     {:schema contract-schema
      :authority :non-authoritative
      :authoritative? false
      :by-component-id by-component-id
      :by-source-path by-source-path
      :by-test-path by-test-path
      :selectable-namespaces
      (->> components
           (mapcat :tests)
           (map :namespace)
           distinct
           sort
           vec)})))
