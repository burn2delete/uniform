(ns gravity.cli.packaged-launcher-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.cli :as cli]
            [gravity.cli.packaged-launcher :as packaged-launcher]))

(def ^:private repository-root
  (System/getProperty "user.dir"))

(defn- read-process-stream
  [stream]
  (with-open [input stream]
    (let [buffer (byte-array 8192)
          output (java.io.ByteArrayOutputStream.)]
      (loop [count (.read input buffer)]
        (if (neg? count)
          (.toString output "UTF-8")
          (do
            (when (pos? count)
              (.write output buffer 0 count))
            (recur (.read input buffer))))))))

(defn- stream-reader-thread
  [stream result]
  (Thread.
   (reify Runnable
     (run [_]
       (try
         (deliver result (read-process-stream stream))
         (catch Throwable error
           (deliver result error)))))))

(defn- without-known-java-tool-options-banner
  [stderr]
  ;; CI may announce the inherited JAVA_TOOL_OPTIONS value once per JVM.
  ;; Remove only that exact line; unexpected diagnostics remain visible.
  (let [options (System/getenv "JAVA_TOOL_OPTIONS")
        banner (when (seq options)
                 (str "Picked up JAVA_TOOL_OPTIONS: " options "\n"))]
    (loop [remaining stderr]
      (if (and banner (str/starts-with? remaining banner))
        (recur (subs remaining (count banner)))
        remaining))))

(defn- process-result
  [& arguments]
  (let [builder (doto (ProcessBuilder. ^java.util.List (mapv str arguments))
                  (.directory (java.io.File. repository-root)))
        process (.start builder)
        stdout (promise)
        stderr (promise)
        stdout-thread (stream-reader-thread (.getInputStream process) stdout)
        stderr-thread (stream-reader-thread (.getErrorStream process) stderr)]
    (.start stdout-thread)
    (.start stderr-thread)
    (let [exit (.waitFor process)]
      (.join stdout-thread)
      (.join stderr-thread)
      (let [out @stdout
            err @stderr]
        (when (instance? Throwable out) (throw ^Throwable out))
        (when (instance? Throwable err) (throw ^Throwable err))
        {:exit exit :out out :err err}))))

(defn- build-current-package!
  []
  (let [result
        (process-result
         "clojure" "-Srepro" "-M" "-e"
         "(require 'gravity.bootstrap) (println (gravity.bootstrap/p18-t02-build-packaged-jvm-cli!))")]
    (is (= 0 (:exit result))
        (pr-str (select-keys result [:exit :out :err])))
    (is (= packaged-launcher/packaged-jar-path (str/trim (:out result)))
        (pr-str (select-keys result [:out :err])))
    result))

(defn- packaged-expression-command
  [expression]
  (let [command (packaged-launcher/packaged-command)]
    (into (subvec (vec command) 0 5) ["-e" expression])))

(defn- packaged-observation
  [argument]
  (let [expression
        (str "(require 'gravity.cli.launcher)"
             " (gravity.cli.launcher/-main " (pr-str argument) ")"
             " (prn {:bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))"
             " :cli-loaded (boolean (find-ns 'gravity.cli))"
             " :packaged-jvm-cli? (= \"true\""
             " (System/getProperty \"gravity.packaged.jvm.cli\"))})")
        result (apply process-result (packaged-expression-command expression))
        lines (str/split-lines (:out result))]
    {:exit (:exit result)
     :err (:err result)
     :presentation (str/join "\n" (butlast lines))
     :marker (edn/read-string (last lines))}))

(deftest packaged-command-uses-current-lightweight-launcher
  (let [java-var (ns-resolve 'gravity.cli.packaged-launcher 'java-executable)
        classpath-var (ns-resolve 'gravity.cli.packaged-launcher 'runtime-classpath)
        command
        (with-redefs-fn
          {java-var (constantly "/jdk/bin/java")
           classpath-var (constantly "/deps/clojure.jar")}
          #(packaged-launcher/packaged-command "--help"))]
    (is (= ["/jdk/bin/java"
            "-Dgravity.packaged.jvm.cli=true"
            "-cp"
            (str packaged-launcher/packaged-jar-path
                 java.io.File/pathSeparator
                 "/deps/clojure.jar")
            "clojure.main" "-m" "gravity.cli.launcher" "--help"]
           command))))

(deftest packaged-help-version-are-fresh-and-preserve-parity
  (build-current-package!)
  (doseq [[argument expected]
          [["help" (str/trimr (cli/p18-cli-help-text true))]
           ["version" (pr-str (cli/p18-cli-version-record true))]]]
    (testing argument
      (let [{:keys [exit err presentation marker]}
            (packaged-observation argument)]
        (is (= 0 exit) (pr-str {:err err :marker marker}))
        (is (= "" (without-known-java-tool-options-banner err)))
        (is (= expected presentation))
        (is (= {:bootstrap-loaded false
                :cli-loaded true
                :packaged-jvm-cli? true}
               marker)))))
  (doseq [[argument expected]
          [["--help" (str/trimr (cli/p18-cli-help-text true))]
           ["--version" (pr-str (cli/p18-cli-version-record true))]]]
    (testing (str "explicit alias " argument)
      (let [result (process-result "clojure" "-Srepro" "-M:packaged-gravity" argument)]
        (is (= 0 (:exit result))
            (pr-str (select-keys result [:exit :out :err])))
        (is (= expected (str/trimr (:out result))))
        (is (= "" (without-known-java-tool-options-banner (:err result))))))))
