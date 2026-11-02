(ns gravity.self-hosting.p18-t06-release-binary-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap])
  (:import [java.io File]
           [java.nio.file Files]))

(defn- delete-file!
  [path]
  (when path
    (.delete ^File (io/file path))))

(defn- temporary-directory
  [prefix]
  (.toFile
   (Files/createTempDirectory
    prefix
    (make-array java.nio.file.attribute.FileAttribute 0))))

(defn- write-executable!
  [file content]
  (spit file content)
  (.setExecutable ^File file true false)
  file)

(defn- write-test-release-script!
  [root-directory]
  (let [release-directory (io/file root-directory "target/phase-18/release")
        _ (.mkdirs ^File release-directory)
        script-file (File/createTempFile "gravity-p18-t06-test-" ".sh"
                                         release-directory)]
    (write-executable! script-file (bootstrap/p18-t06-release-binary-script))))

(defn- run-release
  [_root-directory script-file & arguments]
  (bootstrap/p18-shell-run
   (into [(.getAbsolutePath ^File script-file)] arguments)))

(deftest release-wrapper-uses-real-co-canonical-source-paths
  (let [root-directory (System/getProperty "user.dir")
        script-file (write-test-release-script! root-directory)
        output-files (atom [])]
    (try
      (let [script-text (slurp script-file)]
        (testing "source commands bypass the public release launcher"
          (doseq [command ["check" "run" "compile"]]
            (is (not (str/includes? script-text
                                    (str "bin/gravity " command))))))
        (is (str/includes? script-text
                           "clojure -M:gravity check \"$source\""))
        (is (str/includes? script-text
                           "clojure -M:gravity run \"$source\""))
        (is (str/includes? script-text
                           "clojure -M:gravity compile \"$source\" \"$flag\" \"$output\"")))
      (doseq [{:keys [extension source expected-output]}
              [{:extension ".gravity"
                :source "examples/core-app.gravity"
                :expected-output "core-app\ngravity:19:2\n(:ok 19)\n"}
               {:extension ".qst"
                :source "examples/core-app.qst"
                :expected-output "core-app\ngravity:19:2\n(:ok 19)\n"}]]
        (let [output-path
              (str "target/phase-18/release/t06 test output-"
                   (System/nanoTime) extension)
              output-file (io/file root-directory output-path)
              artifact-file (io/file (str output-path ".gravity-artifact.edn"))
              check-result (run-release root-directory script-file
                                        "check" source)
              direct-check-result
              (bootstrap/p18-shell-run
               ["clojure" "-M:gravity" "check" source])
              run-result (run-release root-directory script-file
                                      "run" source)
              compile-result (run-release root-directory script-file
                                          "compile" source "--output"
                                          output-path)]
          (swap! output-files conj output-file artifact-file)
          (testing (str extension " check")
            (testing "wrapper forwards the current compiler result"
              (is (= (:exit direct-check-result) (:exit check-result)))
              (is (= (:out direct-check-result) (:out check-result)))
              (is (= (:err direct-check-result) (:err check-result))))
            (testing "current source remains honestly blocked at its C2 reader boundary"
              (is (= 1 (:exit check-result)))
              (is (str/includes? (:err check-result) "C2-HASH"))
              (is (str/includes? (:err check-result)
                                 ":exact-regular-pinned-sh03-reader-source"))))
          (testing (str extension " run")
            (is (zero? (:exit run-result)))
            (is (= expected-output (:out run-result))))
          (testing (str extension " compile")
            (is (zero? (:exit compile-result)))
            (is (str/includes? (:out compile-result)
                               ":kind :gravity/p18-t04-executable-artifact"))
            (is (.isFile ^File output-file))
            (is (.canExecute ^File output-file))
            (is (.isFile ^File artifact-file)))))
      (finally
        (doseq [file @output-files]
          (delete-file! file))
        (delete-file! script-file)))))

(deftest release-wrapper-rejects-modified-and-missing-same-basename-inputs
  (let [root-directory (System/getProperty "user.dir")
        script-file (write-test-release-script! root-directory)
        output-files (atom [])
        source-directory (temporary-directory "gravity-p18-t06-source-")
        missing-directory (io/file source-directory "missing")
        invalid-gravity (io/file source-directory "core-app.gravity")
        invalid-qst (io/file source-directory "core-app.qst")
        missing-gravity (io/file missing-directory "core-app.gravity")
        missing-qst (io/file missing-directory "core-app.qst")]
    (try
      (spit invalid-gravity "not gravity source\n")
      (spit invalid-qst "not gravity source\n")
      (doseq [[extension source]
              [[".gravity" invalid-gravity]
               [".qst" invalid-qst]]]
        (let [result (run-release root-directory script-file
                                  "run" (.getAbsolutePath ^File source))]
          (testing (str "modified " extension " source")
            (is (= 1 (:exit result)))
            (is (str/includes? (:err result) "L3-NS-MISSING")))))
      (doseq [[extension source]
              [[".gravity" missing-gravity]
               [".qst" missing-qst]]]
        (let [source-path (.getAbsolutePath ^File source)
              output-path
              (str "target/phase-18/release/t06 missing output-"
                   (System/nanoTime) extension)
              output-file (io/file root-directory output-path)
              artifact-file (io/file (str output-path ".gravity-artifact.edn"))
              check-result (run-release root-directory script-file
                                        "check" source-path)
              run-result (run-release root-directory script-file
                                      "run" source-path)
              compile-result (run-release root-directory script-file
                                          "compile" source-path "--output"
                                          output-path)]
          (swap! output-files conj output-file artifact-file)
          (doseq [[command result]
                  [["check" check-result]
                   ["run" run-result]
                   ["compile" compile-result]]]
            (testing (str "missing " extension " source " command)
              (is (= 1 (:exit result)))
              (is (str/includes? (:err result) "C2-ENCODING"))))))
      (finally
        (doseq [file @output-files]
          (delete-file! file))
        (delete-file! invalid-gravity)
        (delete-file! invalid-qst)
        (delete-file! missing-gravity)
        (delete-file! missing-qst)
        (delete-file! source-directory)
        (delete-file! script-file)))))

(deftest release-wrapper-routes-check-once
  (let [root-directory (System/getProperty "user.dir")
        script-file (write-test-release-script! root-directory)
        fake-bin (temporary-directory "gravity-p18-t06-route-bin-")
        route-log (File/createTempFile "gravity-p18-t06-route-" ".txt")
        fake-clojure
        (write-executable!
         (io/file fake-bin "clojure")
         "#!/usr/bin/env bash\nprintf '%s\\n' \"$*\" >> \"$T06_ROUTE_LOG\"\nexit 0\n")
        path-environment
        {"PATH" (str (.getAbsolutePath ^File fake-bin)
                      File/pathSeparator
                      (or (System/getenv "PATH") ""))
         "T06_ROUTE_LOG" (.getAbsolutePath route-log)}]
    (try
      (doseq [[label source operation]
              [["ordinary" "/tmp/t06-ordinary.gravity" "check"]
               ["specialized" "/tmp/compiler-c3-capture.gravity"
                "compiler-c3-syntax"]]]
        (spit route-log "")
        (let [result
              (bootstrap/p18-shell-run
               path-environment
               [(.getAbsolutePath ^File script-file) "check" source])
              calls (str/split-lines (slurp route-log))]
          (testing (str label " route")
            (is (zero? (:exit result)))
            (is (= [(str "-M:gravity " operation " " source)] calls)))))
      (finally
        (delete-file! fake-clojure)
        (delete-file! fake-bin)
        (delete-file! route-log)
        (delete-file! script-file)))))

(deftest release-wrapper-delegates-self-host-and-keeps-output-validation
  (let [root-directory (System/getProperty "user.dir")
        script-file (write-test-release-script! root-directory)
        fake-bin (temporary-directory "gravity-p18-t06-fake-bin-")
        arguments-file (File/createTempFile "gravity-p18-t06-self-host-" ".txt")
        fake-clojure
        (write-executable!
         (io/file fake-bin "clojure")
         "#!/usr/bin/env bash\nprintf '%s\\n' \"$*\" > \"$T06_SELF_HOST_ARGS\"\nexit 23\n")
        path-environment
        {"PATH" (str (.getAbsolutePath ^File fake-bin)
                      File/pathSeparator
                      (or (System/getenv "PATH") ""))
         "T06_SELF_HOST_ARGS" (.getAbsolutePath arguments-file)}]
    (try
      (let [self-host-result
            (bootstrap/p18-shell-run
             path-environment
             (into [(.getAbsolutePath ^File script-file)]
                   ["self-host" "verify"]))
            invalid-output-result
            (run-release root-directory script-file
                         "compile" "examples/core-app.gravity"
                         "--invalid-output-flag"
                         "target/phase-18/release/t06-invalid")]
        (is (= 23 (:exit self-host-result)))
        (is (= "-M:gravity self-host verify\n"
               (slurp arguments-file)))
        (is (= 1 (:exit invalid-output-result)))
        (is (str/includes? (:err invalid-output-result) "P18T04002")))
      (finally
        (delete-file! fake-clojure)
        (delete-file! fake-bin)
        (delete-file! arguments-file)
        (delete-file! script-file)))))
