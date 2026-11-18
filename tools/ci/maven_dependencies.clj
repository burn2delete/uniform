(ns ci.maven-dependencies
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str]))

(def ^:private max-attempts 3)
(def ^:private retry-delays-ms [2000 5000])
(def ^:private transient-http-statuses #{408 429 500 502 503 504})
(def ^:private alias-pattern #"[A-Za-z0-9][A-Za-z0-9_.-]*")
(def ^:private transfer-pattern
  #"(?i)(could not transfer|failed to transfer|transfer artifact|failed to read artifact descriptor|could not resolve dependencies)")
(def ^:private status-pattern
  #"(?i)\b(?:status code|response code|HTTP response code|HTTP(?:/\d(?:\.\d)?)?)\D{0,16}(\d{3})\b")
(def ^:private transient-transport-pattern
  #"(?i)(SocketTimeoutException|ConnectException|UnknownHostException|SocketException|Read timed out|Connection reset|Connection refused|connect timed out|Temporary failure in name resolution|Remote host closed connection)")

(defn transient-dependency-fetch-failure?
  "Return true only for a dependency transfer with a known transient response or transport failure."
  [output]
  (let [output (str output)
        statuses (map (comp parse-long second) (re-seq status-pattern output))]
    (and (re-find transfer-pattern output)
         (if (seq statuses)
           (every? transient-http-statuses statuses)
           (boolean (re-find transient-transport-pattern output))))))

(defn prefetch-command
  "Build the Clojure CLI command that resolves a dependency basis without running tests."
  [alias]
  (when-not (or (str/blank? alias) (re-matches alias-pattern alias))
    (throw (ex-info "Invalid Clojure dependency alias" {:alias alias})))
  ["clojure" "-Srepro" "-P" (if (str/blank? alias) "-M" (str "-M:" alias))])

(defn- result-output
  [{:keys [out err]}]
  (str (or out "") (when (and (seq out) (seq err)) "\n") (or err "")))

(defn- emit-process-result!
  [{:keys [out err]}]
  (when (seq out)
    (print out)
    (flush))
  (when (seq err)
    (binding [*out* *err*]
      (print err)
      (flush))))

(defn retry-prefetch-with!
  "Run only dependency prefetch, retrying bounded transient transport failures."
  [command directory run-command sleep! emit!]
  (loop [attempt 1]
    (let [result (run-command command directory)
          output (result-output result)]
      (emit! {:type :process-result :result result})
      (if (zero? (:exit result))
        {:attempts attempt :retried? (> attempt 1)}
        (if (and (< attempt max-attempts)
                 (transient-dependency-fetch-failure? output))
          (let [delay-ms (nth retry-delays-ms (dec attempt))]
            (emit! {:type :retry
                    :attempt attempt
                    :next-attempt (inc attempt)
                    :delay-ms delay-ms})
            (sleep! delay-ms)
            (recur (inc attempt)))
          (throw (ex-info "Maven dependency prefetch failed"
                          {:attempts attempt
                           :exit (:exit result)
                           :transient? (transient-dependency-fetch-failure? output)
                           :output output})))))))

(defn retry-prefetch!
  [command directory]
  (retry-prefetch-with!
   command directory
   (fn [argv working-directory]
     (apply shell/sh (concat argv [:dir working-directory])))
   (fn [delay-ms] (Thread/sleep (long delay-ms)))
   (fn [{:keys [type result attempt delay-ms]}]
     (case type
       :process-result (emit-process-result! result)
       :retry (binding [*out* *err*]
                (println (str "Transient Maven fetch failure; retrying dependency prefetch in "
                              (/ delay-ms 1000) "s (attempt " attempt "/" max-attempts ").")))))))

(defn- parse-options
  [args]
  (loop [remaining (seq args)
         options {:directory "." :alias "" :seen #{}}]
    (if-not remaining
      (dissoc options :seen)
      (let [[option value & more] remaining]
        (when-not (contains? #{"--directory" "--alias"} option)
          (throw (ex-info "Unknown Maven dependency prefetch option" {:option option})))
        (when-not value
          (throw (ex-info "Maven dependency prefetch option needs a value"
                          {:option option})))
        (when (contains? (:seen options) option)
          (throw (ex-info "Duplicate Maven dependency prefetch option" {:option option})))
        (recur more
               (-> options
                   (assoc (if (= option "--directory") :directory :alias) value)
                   (update :seen conj option)))))))

(defn -main
  [& args]
  (let [exit-code
        (try
          (let [{:keys [directory alias]} (parse-options args)
                dependency-directory (io/file directory)]
            (when-not (.isDirectory dependency-directory)
              (throw (ex-info "Maven dependency root is not a directory"
                              {:directory directory})))
            (when-not (or (str/blank? alias) (re-matches alias-pattern alias))
              (throw (ex-info "Invalid Clojure dependency alias" {:alias alias})))
            (let [{:keys [attempts retried?]}
                  (retry-prefetch! (prefetch-command alias) directory)]
              (println (str "Maven dependencies prepared (attempts: " attempts
                            ", retried: " retried? ").")))
            0)
          (catch clojure.lang.ExceptionInfo error
            (binding [*out* *err*]
              (println (.getMessage error)))
            1))]
    (shutdown-agents)
    (when-not (zero? exit-code)
      (System/exit exit-code))))
