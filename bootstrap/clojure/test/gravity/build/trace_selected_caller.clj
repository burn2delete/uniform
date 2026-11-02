(ns gravity.build.trace-selected-caller
  "Concrete cooperative RV caller. Loading this namespace starts no operation."
  (:refer-clojure :exclude [run!])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [gravity.build.trace-semantic-delivery :as delivery]
            [gravity.build.trace-input-syntax-receipt :as receipt]
            [gravity.development-test-runner :as development])
  (:import [java.io PushbackReader StringReader]
           [java.nio.file Files Path]
           [java.nio.charset StandardCharsets]
           [java.security MessageDigest]))

(def operation
  {:id :RV1-trace-input-syntax-second-id :selector receipt/selector
   :reuse-held-operation false :genuine-execution false})
(def inherited-inputs
  ["JAVA_HOME" "CLASSPATH" "JAVA_TOOL_OPTIONS" "JDK_JAVA_OPTIONS" "_JAVA_OPTIONS"
   "DYLD_INSERT_LIBRARIES" "DYLD_LIBRARY_PATH" "LD_PRELOAD" "LD_LIBRARY_PATH"])
(def short-selector (name receipt/selector))

(defn- require! [condition field]
  (when-not condition
    (throw (ex-info "Selected caller binding refused" {:id "RV-TRACE-CALLER-BINDING" :field field}))))
(defn- digest [bytes]
  (format "%064x" (java.math.BigInteger. 1 (.digest (MessageDigest/getInstance "SHA-256") bytes))))
(defn- data-digest [data]
  (let [canonical (walk/postwalk #(if (map? %) (into (sorted-map-by (fn [a b] (compare (pr-str a) (pr-str b)))) %) %) data)]
    (digest (.getBytes (pr-str canonical) StandardCharsets/UTF_8))))
(defn- absolute? [s] (and (string? s) (.isAbsolute (io/file s))))
(defn- pin! [{:keys [path sha256]}]
  (require! (and (absolute? path) (string? sha256)
                 (= sha256 (digest (Files/readAllBytes (.toPath (io/file path)))))) :material-pin))
(defn- inherited-binding []
  ;; No raw inherited value is retained or published. Absence differs from empty.
  (mapv (fn [key] [key (when-let [value (System/getenv key)] (data-digest value))]) inherited-inputs))
(defn- configuration! [config]
  (require! (= #{:launcher :jvm-options :classpath :source-files :working-directory
                 :output-root :parent-report :broker :limits :source :test-source-sha256
                 :evidence-kind :inherited} (set (keys config))) :configuration-shape)
  (require! (and (absolute? (get-in config [:launcher :path]))
                 (vector? (:jvm-options config)) (every? string? (:jvm-options config))
                 (vector? (:classpath config)) (seq (:classpath config))
                 (every? absolute? (:classpath config))
                 (vector? (:source-files config)) (seq (:source-files config))
                 (every? absolute? [(:working-directory config) (:output-root config)
                                    (get-in config [:parent-report :file])])
                 (= (:inherited config) (inherited-binding))) :configuration)
  ;; Source tuple is an externally authenticated composition input. These reads
  ;; check actual material bytes, not Git ancestry, origin or all JVM behavior.
  (pin! (:launcher config))
  (doseq [file (:source-files config)] (pin! file))
  (let [test-path (str (.resolve (.toPath (io/file (:working-directory config)))
                                 "bootstrap/clojure/test/gravity/self_hosting/resolution_parent_mutation_test.clj"))]
    (require! (some #(and (= test-path (:path %)) (= (:test-source-sha256 config) (:sha256 %)))
                    (:source-files config)) :selected-test-pin))
  (doseq [path (:classpath config) :when (.isFile (io/file path))]
    (require! (some #(= path (:path %)) (:source-files config)) :classpath-file-pin))
  (require! (and (= #{:source-commit :source-tree} (set (keys (:source config))))
                 (every? #(and (string? %) (re-matches #"[0-9a-f]{40}" %)) (vals (:source config)))) :source)
  (require! (and (= #{:timeout-ms :max-capture-bytes :max-capture-chars :capture-wait-ms
                      :completion-bytes :semantic-bytes} (set (keys (:limits config))))
                 (every? #(and (integer? %) (pos? %) (<= % Long/MAX_VALUE)) (vals (:limits config)))
                 (every? #(< (get-in config [:limits %]) Integer/MAX_VALUE)
                         [:completion-bytes :semantic-bytes]))
            :existing-capture-and-wait-values)
  config)

(defn bind-request
  "Read/pin selected material and inherited override digests; never launches.
  Caller supplies existing capture/watchdog values and authenticated source tuple.
  Classpath directory contents must be covered by source-files; origin/coverage
  authentication remains an explicit owner obligation, not inferred from hashes."
  [configuration]
  (let [config (configuration! (assoc configuration :inherited (inherited-binding)))
        hash (data-digest config)
        expected {:source (assoc (:source config) :configuration-sha256 hash)
                  :test-source-sha256 (:test-source-sha256 config) :evidence-kind (:evidence-kind config)}]
    {:configuration config :configuration-sha256 hash :expected expected}))

(defn- bound! [{:keys [configuration configuration-sha256 expected] :as request}]
  (configuration! configuration)
  (require! (= configuration-sha256 (data-digest configuration)) :configuration-digest)
  (require! (= expected {:source (assoc (:source configuration) :configuration-sha256 configuration-sha256)
                         :test-source-sha256 (:test-source-sha256 configuration)
                         :evidence-kind (:evidence-kind configuration)}) :expected-binding)
  request)

(defn command-prefix [request]
  (bound! request)
  (let [config (:configuration request)]
    (vec (concat [(get-in config [:launcher :path])] (:jvm-options config)
                 ["-cp" (str/join java.io.File/pathSeparator (:classpath config))
                  "clojure.main" "-m" "gravity.build.trace-selected-caller"
                  "--rv-binding" (pr-str request)]))))

(defn owner-options [request parent-root audit]
  (bound! request)
  (let [config (:configuration request) limits (:limits config)]
    (merge (select-keys limits [:timeout-ms :max-capture-bytes :max-capture-chars :capture-wait-ms])
           {:resource-broker (:broker config) :command (command-prefix request)
            :working-directory (:working-directory config) :exact-test-vars [short-selector]
            :child-fail-fast? false
            :development-result (assoc (get-in request [:expected :source]) :limit-bytes (:completion-bytes limits))
            :inside-lease-report! (delivery/inside-lease-callback
                                   parent-root (:expected request) (:semantic-bytes limits) audit)
            :parent-report (:parent-report config)})))

(defn correlate-parent
  "Check the actual v2 report against the actual returned result and callback.
  The outer report ID is deliberately independent of the child request UUID.
  This adds correlation only; it does not revalidate or grant resource custody."
  [request result audit]
  (let [report (get-in result [:parent-report :report])
        original (dissoc result :parent-report)
        child-request (:development-result-request original)]
    (require! (= :gravity/development-parent-report-v2 (:schema report)) :joint-report-schema)
    (require! (and (map? (get-in report [:resource :owned-resource]))
                   (= (:source child-request) (get-in request [:expected :source]))
                   (= (:source child-request) (get-in report [:request :source]))
                   (= (namespace receipt/selector) (get-in report [:request :namespace]))
                   (= original (get-in report [:operation :result]))
                   (true? (get-in report [:operation :returned?]))
                   (= :correlated (:state audit))
                   (= child-request (get-in audit [:observation :delivery-request]))
                   (= original (get-in audit [:observation :result]))) :joint-report-correlation)
    {:result result :semantic audit :request-binding request
     :actual-command (:command original) :method-status :constructed-unadmitted
     :genuine-execution-established? false :cache-admission? false}))

(defn run!
  "Actual operation entry; requires separately admitted cooperative execution.
  Keep audit outside this call to retain original callback/error facts on throw.
  Common owner alone acquires/disposes C/L and writes the final joint report."
  [request parent-root audit]
  (let [options (owner-options request parent-root audit)
        route (requiring-resolve 'gravity.self-hosting.sh01-parallel-test-runner/run-resolution-parent-mutation-process)
        result (route operation options)]
    (correlate-parent request result @audit)))

(defn- one-edn [text]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (let [eof (Object.) opts {:eof eof :readers {} :default (fn [& _] (require! false :tagged-data))}
          value (edn/read opts reader)]
      (require! (and (not (identical? eof value)) (identical? eof (edn/read opts reader))) :one-data-frame)
      value)))

(defn child-main!
  "Consume only this prefix plus the common-appended selection/delivery DATA.
  The actual target namespace loads normally only after binding validation.
  No handles/functions cross JVMs; byte authority is constructed in this child."
  [arguments]
  (require! (and (= 8 (count arguments))
                 (= ["--rv-binding" "--namespace" "--exact" "--machine-result"]
                    (mapv #(nth arguments %) [0 2 4 6]))
                 (= (namespace receipt/selector) (nth arguments 3))
                 (= short-selector (nth arguments 5))) :child-arguments)
  (let [request (bound! (one-edn (nth arguments 1)))
        actual-delivery (one-edn (nth arguments 7))
        expected (:expected request)
        _ (require! (= (:source expected) (get-in actual-delivery [:request :source])) :child-source)
        _ (require! (and (= {:namespaces [(namespace receipt/selector)]
                             :test-vars [(str receipt/selector)] :fail-fast? false}
                            (get-in actual-delivery [:request :selection]))
                         (= (get-in request [:configuration :limits :completion-bytes])
                            (:limit-bytes actual-delivery))) :child-delivery-binding)
        completion-sink (development/result-delivery-sink actual-delivery nil)
        observations-var (requiring-resolve 'gravity.self-hosting.resolution-parent-mutation-test/observations)
        observations (var-get observations-var)
        limit (get-in request [:configuration :limits :semantic-bytes])
        result (delivery/invoke-child!
                (Path/of (get-in request [:configuration :output-root]) (make-array String 0))
                (:request actual-delivery) expected observations completion-sink
                (fn [bytes] (require! (<= bytes limit) :semantic-capture)))]
    (when-let [error (or (:collection-error result) (:publication-error result))] (throw error))
    (require! (and (= :consistent-supplied-observation (:binding-status result))
                   (= 0 (:run-return result))) :child-nonpassing)
    (:run-return result)))

(defn -main [& arguments]
  (System/exit (int (child-main! (vec arguments)))))
