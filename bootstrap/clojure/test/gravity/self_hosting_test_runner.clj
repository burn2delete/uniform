(ns gravity.self-hosting-test-runner
  "Coordinator-owned test routing for the bootstrap and self-hosting suites.

  Dedicated self-hosting tests are discovered below gravity/self_hosting on
  the test classpath. Adding a leaf test therefore does not require an edit to
  gravity.bootstrap-test or to this runner."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test]))

(def ^:private base-test-namespaces
  '[gravity.diagnostics-test
    gravity.cli-test
    gravity.bootstrap-test
    gravity.p15-public-native-run-test
    gravity.p15-public-native-run-wrapper-test
    gravity.p15-native-launcher-test
    gravity.p15-native-runtime-driver-test])

(def ^:private explicitly-selectable-test-namespaces
  '#{gravity.darwin-publication-test})

(def ^:private dedicated-test-resource
  "gravity/self_hosting")

(defn- file-resource-directories
  []
  (let [class-loader (.getContextClassLoader (Thread/currentThread))]
    (->> (enumeration-seq
          (.getResources class-loader dedicated-test-resource))
         (mapv
          (fn [resource]
            (when-not (= "file" (.getProtocol resource))
              (throw
               (ex-info
                "Dedicated self-hosting tests require a filesystem test classpath"
                {:id "SH01-TEST-RESOURCE"
                 :resource dedicated-test-resource
                 :protocol (.getProtocol resource)})))
            (io/file (.toURI resource)))))))

(defn- clojure-test-file?
  [file]
  (and (.isFile file)
       (str/ends-with? (.getName file) "_test.clj")))

(defn- test-file->namespace
  [root file]
  (let [relative
        (str (.relativize (.toPath root) (.toPath file)))
        normalized
        (-> relative
            (str/replace java.io.File/separator "/")
            (str/replace #"\.clj$" "")
            (str/replace "_" "-")
            (str/replace "/" "."))]
    (symbol (str "gravity.self-hosting." normalized))))

(defn- dedicated-test-entry
  [root file]
  (let [relative
        (-> (str (.relativize (.toPath root) (.toPath file)))
            (str/replace java.io.File/separator "/"))]
    {:namespace (test-file->namespace root file)
     :relative-path relative}))

(defn dedicated-test-namespaces
  "Returns dedicated self-hosting test namespaces in deterministic order."
  []
  (let [entries
        (->> (file-resource-directories)
             (mapcat
              (fn [root]
                (->> (file-seq root)
                     (filter clojure-test-file?)
                     (map #(dedicated-test-entry root %)))))
             vec)
        collisions
        (->> entries
             (group-by :namespace)
             (keep
              (fn [[namespace matches]]
                (when (< 1 (count matches))
                  [namespace (mapv :relative-path matches)])))
             (into (sorted-map)))]
    (when (seq collisions)
      (throw
       (ex-info
        "Dedicated self-hosting test namespaces must map to one file"
        {:id "SH01-TEST-NAMESPACE-COLLISION"
         :collisions collisions})))
    (->> entries (map :namespace) sort vec)))

(defn test-namespaces
  "Returns the established bootstrap suite followed by dedicated leaf suites."
  []
  (into (vec base-test-namespaces) (dedicated-test-namespaces)))

(defn select-tests
  "Selects a deterministic test run or namespace listing from CLI arguments."
  [arguments]
  (let [arguments (vec arguments)
        dedicated (dedicated-test-namespaces)
        defaults (into (vec base-test-namespaces) dedicated)
        selectable
        (into (set defaults) explicitly-selectable-test-namespaces)]
    (cond
      (empty? arguments)
      {:mode :run :namespaces defaults}

      (= ["--dedicated"] arguments)
      {:mode :run :namespaces dedicated}

      (= ["--list"] arguments)
      {:mode :list :namespaces (vec (sort selectable))}

      (and (= 2 (count arguments))
           (= "--namespace" (first arguments)))
      (let [namespace (symbol (second arguments))]
        (when-not (contains? selectable namespace)
          (throw
           (ex-info
            "Requested test namespace is not owned by this runner"
            {:id "SH01-TEST-NAMESPACE"
             :namespace namespace
             :selectable (vec (sort selectable))})))
        {:mode :run :namespaces [namespace]})

      :else
      (throw
       (ex-info
        "Unsupported self-hosting test runner arguments"
        {:id "SH01-TEST-USAGE"
         :arguments arguments
         :supported
         [[] ["--dedicated"] ["--list"]
          ["--namespace" "<owned-test-namespace>"]]})))))

(defn -main
  [& arguments]
  (let [{:keys [mode namespaces]} (select-tests arguments)]
    (case mode
      :list
      (doseq [namespace namespaces]
        (println namespace))

      :run
      (do
        (doseq [namespace namespaces]
          (require namespace))
        (let [result (apply test/run-tests namespaces)]
          (if (and (zero? (:fail result)) (zero? (:error result)))
            (println
             (str "Clojure validation passed: "
                  (:test result) " tests, "
                  (:pass result) " assertions, "
                  (count namespaces) " namespaces"))
            (System/exit 1)))))))
