(ns gravity.self-hosting.sh01-component-test-dependencies
  "Reviewed component-to-test dependencies for incremental development checks."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def contract-schema
  :gravity/stage0-incremental-test-dependencies-v2)

(def cache-closure-schema
  :gravity/stage0-explicit-test-cache-closure-v1)

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

(def ^:private cache-closure-roles
  #{:production :transitive :test :fixture :runner :contract :classpath})

(def ^:private reviewed-runner-paths
  #{"bootstrap/clojure/test/gravity/self_hosting/sh01_component_test_dependencies.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_development_loop_wiring.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_development_test_cache.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_host_resource_broker.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_incremental_check.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj"
    "bootstrap/clojure/test/gravity/self_hosting_test_runner.clj"})

(def ^:private reviewed-contract-paths
  #{contract-relative-path
    "docs/self-hosting-slice-backlog.md"
    "docs/self-hosting-slice-ownership.edn"})

(def ^:private reviewed-inventories
  [{:kind :relative-test-path-set-v1
    :root "bootstrap/clojure/test/gravity/self_hosting"
    :suffix "_test.clj"}
   {:kind :internal-class-path-set-v1
    :root "bootstrap/clojure/src"
    :suffix ".class"}
   {:kind :internal-class-path-set-v1
    :root "bootstrap/clojure/test"
    :suffix ".class"}])

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

(defn- normalized-relative-path?
  [value]
  (and (string? value)
       (not (str/blank? value))
       (not (str/starts-with? value "/"))
       (not (str/includes? value "\\"))
       (let [segments (str/split value #"/" -1)]
         (and (every? #(and (not (str/blank? %))
                            (not= "." %)
                            (not= ".." %))
                      segments)
              (= value (str/join "/" segments))))))

(defn- role-path-valid?
  [test-entry {:keys [path role]}]
  (case role
    :production
    (boolean (re-matches #"bootstrap/clojure/src/gravity/[^/]+\.clj"
                         path))

    :transitive
    (boolean (re-matches #"bootstrap/clojure/src/gravity/[^/]+\.clj"
                         path))

    :test (= path (:path test-entry))
    :fixture (str/starts-with? path "bootstrap/clojure/fixtures/")
    :runner (contains? reviewed-runner-paths path)
    :contract (contains? reviewed-contract-paths path)
    :classpath (= "deps.edn" path)
    false))

(defn valid-cache-closure?
  "True only for one bounded, explicit, locally present test closure."
  [component test-entry closure]
  (let [inputs (:inputs closure)
        inventories (:inventories closure)
        identities (mapv (juxt :role :path) inputs)
        roles (set (map :role inputs))]
    (and (map? closure)
         (nil? (meta closure))
         (= #{:schema :inputs :inventories} (set (keys closure)))
         (= cache-closure-schema (:schema closure))
         (vector? inputs)
         (<= 1 (count inputs) 64)
         (= reviewed-inventories inventories)
         (every? #(and (map? %)
                       (nil? (meta %))
                       (= #{:path :role} (set (keys %)))
                       (normalized-relative-path? (:path %))
                       (contains? cache-closure-roles (:role %))
                       (role-path-valid? test-entry %)
                       (.isFile (.toFile (.resolve @root (:path %)))))
                 inputs)
         (= (count identities) (count (distinct identities)))
         (= (count inputs) (count (distinct (map :path inputs))))
         (every? roles [:production :test :runner :contract :classpath])
         (some #(and (= :production (:role %))
                     (= (:source-path component) (:path %)))
               inputs)
         (some #(and (= :test (:role %))
                     (= (:path test-entry) (:path %)))
               inputs))))

(defn validate-contract
  "Validates and returns the closed incremental component/test contract."
  [contract]
  (let [top-level-keys
        #{:schema :authority :authoritative? :components :standalone-tests
          :nonclaims}
        component-keys #{:component-id :source-path :tests}
        required-test-keys #{:path :namespace :jvm-group :test-policy}
        allowed-test-keys (conj required-test-keys :cache-closure)
        standalone-test-keys
        #{:test-id :path :namespace :jvm-group :test-policy}
        test-policy-keys
        #{:deterministic? :performance? :proof? :freshness-required?}
        components (:components contract)
        standalone-tests (:standalone-tests contract)]
    (when-not (= top-level-keys (set (keys contract)))
      (contract-error "Incremental dependency contract keys changed"
                      {:keys (vec (sort (keys contract)))}))
    (when-not (and (= contract-schema (:schema contract))
                   (= :non-authoritative (:authority contract))
                   (false? (:authoritative? contract))
                   (vector? components)
                   (seq components)
                   (vector? standalone-tests)
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
        (when-not (and (= required-test-keys
                          (set (filter required-test-keys
                                       (keys test-entry))))
                       (every? allowed-test-keys (keys test-entry)))
          (contract-error "Incremental component test entry keys changed"
                          {:component-id (:component-id component)
                           :test test-entry}))
        (when-not (and (string? (:path test-entry))
                       (not (empty? (:path test-entry)))
                       (symbol? (:namespace test-entry))
                       (string? (:jvm-group test-entry))
                       (not (empty? (:jvm-group test-entry)))
                       (= test-policy-keys
                          (set (keys (:test-policy test-entry))))
                       (every? boolean? (vals (:test-policy test-entry))))
          (contract-error "Incremental component test entry is malformed"
                          {:component-id (:component-id component)
                           :test test-entry}))
        (when-not (= (:namespace test-entry)
                     (expected-test-namespace (:path test-entry)))
          (contract-error "Incremental test path and namespace disagree"
                         {:component-id (:component-id component)
                           :test test-entry}))))
    (doseq [test-entry standalone-tests]
      (when-not (= standalone-test-keys (set (keys test-entry)))
        (contract-error "Incremental standalone test entry keys changed"
                        {:test test-entry}))
      (when-not (and (string? (:test-id test-entry))
                     (not (empty? (:test-id test-entry)))
                     (string? (:path test-entry))
                     (not (empty? (:path test-entry)))
                     (symbol? (:namespace test-entry))
                     (string? (:jvm-group test-entry))
                     (not (empty? (:jvm-group test-entry)))
                     (= test-policy-keys
                        (set (keys (:test-policy test-entry))))
                     (every? boolean? (vals (:test-policy test-entry))))
        (contract-error "Incremental standalone test entry is malformed"
                        {:test test-entry}))
      (when-not (= (:namespace test-entry)
                   (expected-test-namespace (:path test-entry)))
        (contract-error "Incremental standalone test path and namespace disagree"
                        {:test test-entry})))
    (doseq [[kind values]
            [[:component-id (map :component-id components)]
             [:source-path (map :source-path components)]
             [:test-id (map :test-id standalone-tests)]
             [:test-path (concat
                          (mapcat #(map :path (:tests %)) components)
                          (map :path standalone-tests))]
             [:test-namespace
              (concat
               (mapcat #(map :namespace (:tests %)) components)
               (map :namespace standalone-tests))]]]
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
   (let [contract (validate-contract contract)
         components
         (mapv
          (fn [component]
            (update component :tests
                    (fn [tests]
                      (mapv
                       #(assoc % :cache-closure-valid?
                               (valid-cache-closure?
                                component % (:cache-closure %)))
                       tests))))
          (:components contract))
         standalone-tests (:standalone-tests contract)
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
          (concat
           (for [component components
                 test-entry (:tests component)]
             [(:path test-entry)
              (assoc test-entry :component-id (:component-id component))])
           (map (juxt :path identity) standalone-tests)))]
     {:schema contract-schema
      :authority :non-authoritative
      :authoritative? false
      :by-component-id by-component-id
      :by-source-path by-source-path
      :by-test-path by-test-path
      :standalone-tests standalone-tests
      :selectable-namespaces
      (->> (concat (mapcat :tests components) standalone-tests)
           (map :namespace)
           distinct
           sort
           vec)})))

(defn reviewed-cache-closure
  "Return the exact reviewed closure for a namespace, otherwise nil."
  ([namespace]
   (reviewed-cache-closure (dependency-index) namespace))
  ([index namespace]
   (some
    (fn [[_ component]]
      (some (fn [test-entry]
              (when (and (= namespace (:namespace test-entry))
                         (:cache-closure-valid? test-entry))
                (:cache-closure test-entry)))
            (:tests component)))
    (:by-component-id index))))
