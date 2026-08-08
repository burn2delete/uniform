(ns gravity.p15-public-native-run-wrapper-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]))

(def ^:private process-timeout-ms 10000)
(def ^:private system-path "/usr/bin:/bin:/usr/sbin:/sbin")
(def ^:private bounded-run-argv
  ["run"
   "inputs/demo module.gravity"
   "--target" "c"
   "--lowering" "runtime-derived"])

(defn- repository-root
  []
  (let [resource (io/resource "gravity/p15_public_native_run_wrapper_test.clj")]
    (when-not resource
      (throw (ex-info "P15 public native-run wrapper test source is not on the classpath"
                      {:id "P15-S23-WRAPPER-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "P15-S23-WRAPPER-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- write-text!
  [target content]
  (let [target (java.nio.file.Paths/get (str target) (make-array String 0))]
    (java.nio.file.Files/createDirectories
     (.getParent target)
     (make-array java.nio.file.attribute.FileAttribute 0))
    (java.nio.file.Files/write
     target
     (.getBytes content java.nio.charset.StandardCharsets/UTF_8)
     (make-array java.nio.file.OpenOption 0))))

(defn- executable-text!
  [target content]
  (write-text! target content)
  (.setExecutable (.toFile (java.nio.file.Paths/get (str target)
                                                   (make-array String 0)))
                  true)
  target)

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                        root-path
                        (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- read-file
  [file]
  (if (.isFile (io/file (str file)))
    (slurp (str file))
    ""))

(defn- run-wrapper
  [temp-root env-vars argv]
  (let [stdout-file (java.io.File/createTempFile "gravity-p15-wrapper-out-" ".txt")
        stderr-file (java.io.File/createTempFile "gravity-p15-wrapper-err-" ".txt")
        builder (doto (ProcessBuilder.
                       ^java.util.List
                       (into [(path "bin/gravity")] argv))
                  (.directory (.toFile temp-root))
                  (.redirectOutput stdout-file)
                  (.redirectError stderr-file))
        environment (.environment builder)]
    (doseq [[key value] env-vars]
      (.put environment key value))
    (try
      (let [process (.start builder)
            exited? (.waitFor process process-timeout-ms
                              java.util.concurrent.TimeUnit/MILLISECONDS)]
        (when-not exited?
          (.destroyForcibly process)
          (.waitFor process))
        {:exit (if exited? (.exitValue process) 124)
         :out (read-file stdout-file)
         :err (read-file stderr-file)})
      (finally
        (.delete stdout-file)
        (.delete stderr-file)))))

(defn- with-fake-wrapper-root
  [f]
  (let [temp-root (java.nio.file.Files/createTempDirectory
                   "gravity-p15-public-native-run-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        fake-bin (.resolve temp-root "fake-bin")
        wrapper (.resolve temp-root "bin/gravity")
        jar (.resolve temp-root "target/phase-18/jvm-cli/gravity-jvm-cli.jar")
        clojure-capture (.resolve temp-root "clojure-argv.txt")
        java-capture (.resolve temp-root "java-argv.txt")]
    (try
      (java.nio.file.Files/createDirectories
       (.getParent wrapper)
       (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/copy
       (java.nio.file.Paths/get (path "bin/gravity") (make-array String 0))
       wrapper
       (into-array java.nio.file.CopyOption
                   [java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
      (.setExecutable (.toFile wrapper) true)
      (write-text! jar "not-a-real-jar")
      (executable-text!
       (.resolve fake-bin "clojure")
       (str "#!/bin/sh\n"
            "if [ \"${1:-}\" = \"-Spath\" ]; then\n"
            "  printf '%s\\n' '/fake/runtime:'\n"
            "  exit 0\n"
            "fi\n"
            "printf '%s\\n' \"$@\" > \"${GRAVITY_CLOJURE_CAPTURE}\"\n"
            "exit 0\n"))
      (executable-text!
       (.resolve fake-bin "java")
       (str "#!/bin/sh\n"
            "if [ \"${1:-}\" = \"-XshowSettings:properties\" ]; then\n"
            "  echo \"    java.class.version = ${GRAVITY_FAKE_JAVA_CLASS_MAJOR:-63}.0\" >&2\n"
            "  exit 0\n"
            "fi\n"
            "printf '%s\\n' \"$@\" > \"${GRAVITY_JAVA_CAPTURE}\"\n"
            "exit 0\n"))
      (executable-text!
       (.resolve fake-bin "unzip")
       (str "#!/bin/sh\n"
            "[ \"${1:-}\" = \"-p\" ] || exit 1\n"
            "printf '\\312\\376\\272\\276\\000\\000\\000\\077'\n"))
      (f {:temp-root temp-root
          :fake-bin fake-bin
          :clojure-capture clojure-capture
          :java-capture java-capture})
      (finally
        (delete-tree! temp-root)))))

(defn- wrapper-env
  [{:keys [fake-bin clojure-capture java-capture]} overrides]
  (merge {"PATH" (str fake-bin ":" system-path)
          "GRAVITY_CLOJURE_CAPTURE" (str clojure-capture)
          "GRAVITY_JAVA_CAPTURE" (str java-capture)
          "GRAVITY_BOOTSTRAP_ONLY" "1"
          "GRAVITY_PACKAGED_CLI_ONLY" "0"}
         overrides))

(defn- capture-argv
  [capture]
  (vec (str/split-lines (read-file capture))))

(deftest bounded-run-argv-is-forwarded-through-bootstrap-only-wrapper
  (with-fake-wrapper-root
    (fn [{:keys [temp-root clojure-capture java-capture] :as context}]
      (let [result (run-wrapper temp-root
                                (wrapper-env context {})
                                bounded-run-argv)]
        (is (zero? (:exit result)) result)
        (is (= (into ["-M:gravity"] bounded-run-argv)
               (capture-argv clojure-capture)))
        (is (not (.exists (io/file (str java-capture)))))
        (is (str/blank? (:err result)))))))

(deftest bounded-run-argv-bypasses-stale-packaged-wrapper
  (with-fake-wrapper-root
    (fn [{:keys [temp-root clojure-capture java-capture] :as context}]
      (let [result (run-wrapper temp-root
                                (wrapper-env context
                                             {"GRAVITY_BOOTSTRAP_ONLY" "0"})
                                bounded-run-argv)
            clojure-argv (capture-argv clojure-capture)]
        (is (zero? (:exit result)) result)
        (is (= (into ["-M:gravity"] bounded-run-argv)
               clojure-argv))
        (is (not (.exists (io/file (str java-capture)))))
        (is (str/blank? (:err result)))))))

(deftest malformed-targeted-run-also-bypasses-stale-packaged-wrapper
  (with-fake-wrapper-root
    (fn [{:keys [temp-root clojure-capture java-capture] :as context}]
      (let [argv ["run" "inputs/demo.gravity" "--target" "c"]
            result (run-wrapper temp-root
                                (wrapper-env context
                                             {"GRAVITY_BOOTSTRAP_ONLY" "0"})
                                argv)]
        (is (zero? (:exit result)) result)
        (is (= (into ["-M:gravity"] argv)
               (capture-argv clojure-capture)))
        (is (not (.exists (io/file (str java-capture)))))
        (is (str/blank? (:err result)))))))

(deftest packaged-wrapper-rejects-incompatible-java-before-forwarding
  (with-fake-wrapper-root
    (fn [{:keys [temp-root java-capture] :as context}]
      (let [result (run-wrapper temp-root
                                (wrapper-env context
                                             {"GRAVITY_BOOTSTRAP_ONLY" "0"
                                              "GRAVITY_FAKE_JAVA_CLASS_MAJOR" "62"})
                                ["run" "examples/hello.gravity"])]
        (is (= 1 (:exit result)) result)
        (is (str/includes? (:err result) "P18T02008"))
        (is (str/includes? (:err result) ":jar-class-major 63"))
        (is (str/includes? (:err result) ":runtime-class-major 62"))
        (is (not (.exists (io/file (str java-capture)))))))))

(deftest legacy-help-version-and-run-behavior-remain-bootstrap-hosted
  (with-fake-wrapper-root
    (fn [{:keys [temp-root clojure-capture] :as context}]
      (let [env (wrapper-env context {})
            help (run-wrapper temp-root env ["help"])
            version (run-wrapper temp-root env ["--version"])
            legacy-run (run-wrapper temp-root env ["run" "examples/hello.gravity"])]
        (testing "help retains legacy route and labels bounded route honestly"
          (is (zero? (:exit help)) help)
          (is (str/includes? (:out help)
                             "gravity run <file.qst|file.gravity>"))
          (is (str/includes? (:out help)
                             "gravity run <file.gravity|file.qst> --target c --lowering runtime-derived"))
          (is (str/includes? (:out help)
                             "bootstrap-hosted and non-seedless"))
          (is (str/includes? (:out help) ":seedless-release? false")))
        (testing "version remains bootstrap-hosted and non-seedless"
          (is (zero? (:exit version)) version)
          (is (str/includes? (:out version) ":phase \"P18-T01\""))
          (is (str/includes? (:out version) ":bootstrap-hosted? true"))
          (is (str/includes? (:out version) ":seedless-release? false")))
        (testing "legacy run still delegates unchanged"
          (is (zero? (:exit legacy-run)) legacy-run)
          (is (= ["-M:gravity" "run" "examples/hello.gravity"]
                 (capture-argv clojure-capture))))))))
