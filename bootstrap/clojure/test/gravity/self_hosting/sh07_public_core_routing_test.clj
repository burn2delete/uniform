(ns gravity.self-hosting.sh07-public-core-routing-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_public_core_routing_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B46 public-routing test source is not on the classpath"
        {:id "SH07-B46-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B46-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private accepted-relative-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b13/accepted")
(def ^:private rejected-relative-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b13/rejected")
(def ^:private process-timeout-ms (* 4 60 60 1000))
(def ^:private raw-host-error-pattern
  #"(?s)(Exception in thread|StackOverflowError|OutOfMemoryError|IllegalArgumentException|NullPointerException|NumberFormatException|ArithmeticException)")

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path
   (str (if (= family "accepted")
          accepted-relative-root
          rejected-relative-root)
        "/" basename extension)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- write-bytes!
  [target bytes]
  (java.nio.file.Files/createDirectories
   (.getParent target)
   (make-array java.nio.file.attribute.FileAttribute 0))
  (java.nio.file.Files/write
   target bytes
   (make-array java.nio.file.OpenOption 0)))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- read-edn-output
  [text]
  (let [trimmed (str/trim text)]
    (when (seq trimmed)
      (edn/read-string trimmed))))

(defn- run-public
  [working-directory bootstrap-only? source-path]
  (let [stdout-file (java.io.File/createTempFile
                     "gravity-sh07-b46-stdout-" ".edn")
        stderr-file (java.io.File/createTempFile
                     "gravity-sh07-b46-stderr-" ".edn")
        builder
        (doto
         (ProcessBuilder.
          ^java.util.List
          [(path "bin/gravity") "sh07-core" source-path])
          (.directory (io/file working-directory))
          (.redirectOutput stdout-file)
          (.redirectError stderr-file))
        environment (.environment builder)]
    (if bootstrap-only?
      (.put environment "GRAVITY_BOOTSTRAP_ONLY" "1")
      (do
        (.remove environment "GRAVITY_BOOTSTRAP_ONLY")
        (.remove environment "GRAVITY_PACKAGED_CLI_ONLY")))
    (try
      (let [process (.start builder)
            exited?
            (.waitFor process process-timeout-ms
                      java.util.concurrent.TimeUnit/MILLISECONDS)]
        (when-not exited?
          (.destroyForcibly process)
          (.waitFor process))
        (let [out (slurp stdout-file)
              err (slurp stderr-file)
              exit (if exited? (.exitValue process) 124)]
          {:exit exit
           :out out
           :err err
           :data (when (zero? exit) (read-edn-output out))
           :diagnostic (when (= 1 exit) (read-edn-output err))}))
      (finally
        (.delete stdout-file)
        (.delete stderr-file)))))

(defn- public-gate?
  []
  (= "1" (System/getenv "GRAVITY_SH07_B46_PUBLIC")))

(defn- artifact-identity-input
  [artifact]
  (bootstrap/sh07-core-artifact-identity-input artifact))

(defn- assert-no-raw-host-error
  [result]
  (is (not (re-find raw-host-error-pattern
                    (str (:out result) "\n" (:err result))))
      result))

(defn- assert-accepted-public-artifact
  [result source-path]
  (let [artifact (:data result)]
    (is (zero? (:exit result)) result)
    (is (str/blank? (:err result)) result)
    (assert-no-raw-host-error result)
    (is (= :gravity/sh07-core-artifact (:kind artifact)))
    (is (= :accepted (:status artifact)))
    (is (= source-path (get-in artifact [:provenance :source-path])))
    (is (= source-path
           (get-in artifact
                   [:gravity-core-boundary :authenticated-envelope
                    :actual-source-path])))
    (is (= bootstrap/sh07-core-expected-source-content-hash
           (get-in artifact
                   [:gravity-core-boundary :plan-binding
                    :source-content-hash])))
    (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
    (is (true? (get-in artifact
                       [:gravity-core-boundary :clojure-adapter-residual?])))
    artifact))

(deftest sh07-b46-public-command-is-registered-and-current-source-owned
  (let [bootstrap-source
        (slurp (path "bootstrap/clojure/src/gravity/bootstrap.clj"))
        launcher-source (slurp (path "bin/gravity"))]
    (is (str/includes? bootstrap-source
                       (str "\"sh07-core\" (prn "
                            "(sh07-public-core-file-artifact path))"))
        "The Clojure bootstrap CLI must expose the strict SH-07 route.")
    (is (str/includes? launcher-source
                       "gravity sh07-core <file.qst|file.gravity>")
        "Launcher help must name both co-canonical extensions.")
    (is (str/includes? (bootstrap/p18-cli-help-text)
                       "gravity sh07-core <file.qst|file.gravity>")
        "The authoritative Clojure help must name the SH-07 route.")
    (is (str/includes? launcher-source "sh07-core")
        "The launcher must select current source for the SH-07 route.")
    (is (not (str/includes? launcher-source
                            "sh07-core-complete? true"))
        "The bootstrap-hosted route must not claim SH-07 completion.")))

(deftest sh07-b46-public-command-preserves-identity-parity-and-provenance
  (if (public-gate?)
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-b46-public-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          unrelated-cwd (.resolve temp-root "unrelated-cwd")
          gravity-path (.resolve temp-root "left/module.gravity")
          qst-path (.resolve temp-root "right/module.qst")
          fixture
          (fixture-path
           "accepted" "deterministic-top-level-fragments" ".gravity")]
      (try
        (java.nio.file.Files/createDirectories
         unrelated-cwd
         (make-array java.nio.file.attribute.FileAttribute 0))
        (doseq [target [gravity-path qst-path]]
          (write-bytes! target (source-bytes fixture)))
        (let [gravity-source (str gravity-path)
              qst-source (str qst-path)
              direct (bootstrap/sh07-core-file-artifact gravity-source)
              gravity-run (run-public (str unrelated-cwd) true gravity-source)
              repeated-run (run-public (str unrelated-cwd) true gravity-source)
              qst-run (run-public (str unrelated-cwd) true qst-source)
              default-run (run-public (str unrelated-cwd) false gravity-source)
              gravity (assert-accepted-public-artifact
                       gravity-run gravity-source)
              repeated (assert-accepted-public-artifact
                        repeated-run gravity-source)
              qst (assert-accepted-public-artifact qst-run qst-source)
              default (assert-accepted-public-artifact
                       default-run gravity-source)]
          (testing "direct and public routing bind the same authentic artifact"
            (is (= direct gravity))
            (is (= (:artifact-id direct) (:artifact-id gravity)))
            (is (= (artifact-identity-input direct)
                   (artifact-identity-input gravity))))
          (testing "public repeats are deterministic"
            (is (= gravity repeated)))
          (testing "co-canonical extensions have path-neutral identity"
            (is (= (:artifact-id gravity) (:artifact-id qst)))
            (is (= (artifact-identity-input gravity)
                   (artifact-identity-input qst)))
            (is (not= (get-in gravity [:provenance :source-path])
                      (get-in qst [:provenance :source-path]))))
          (testing "the default launcher bypasses a stale packaged CLI"
            (is (= gravity default))
            (is (false? (get-in default [:execution-boundary :self-hosted?])))
            (is (true? (get-in default
                               [:gravity-core-boundary
                                :clojure-adapter-residual?])))))
        (finally
          (delete-tree! temp-root))))
    (is true
        "Set GRAVITY_SH07_B46_PUBLIC=1 in an isolated 8 GiB JVM.")))

(deftest sh07-b46-public-command-emits-structured-c6-rejection
  (if (public-gate?)
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-b46-rejected-"
           (make-array java.nio.file.attribute.FileAttribute 0))]
      (try
        (let [source-path
              (fixture-path
               "rejected" "unsupported-core-form" ".gravity")
              result (run-public (str temp-root) true source-path)
              diagnostic (:diagnostic result)]
          (is (= 1 (:exit result)) result)
          (is (str/blank? (:out result)) result)
          (assert-no-raw-host-error result)
          (is (= "C6-LOWERING-GAP" (:rule diagnostic)) diagnostic)
          (is (= :error (:severity diagnostic)) diagnostic)
          (is (= :core-lowering
                 (or (:stage diagnostic)
                     (get-in diagnostic [:source-span :stage])))
              diagnostic)
          (is (= source-path (get-in diagnostic [:source-span :source]))
              diagnostic))
        (finally
          (delete-tree! temp-root))))
    (is true
        "Set GRAVITY_SH07_B46_PUBLIC=1 for structured rejection routing.")))

(deftest sh07-b46-public-command-contains-extension-and-encoding-errors
  (if (public-gate?)
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-b46-source-boundary-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          invalid-extension (.resolve temp-root "module.txt")
          invalid-encoding (.resolve temp-root "invalid.gravity")]
      (try
        (write-bytes!
         invalid-extension
         (.getBytes "(ns invalid.extension (:profile :meta))\n"
                    java.nio.charset.StandardCharsets/UTF_8))
        (write-bytes! invalid-encoding (byte-array [-61 40]))
        (doseq [[label source-path expected-id]
                [["extension" (str invalid-extension)
                  "L1-SOURCE-EXTENSION"]
                 ["UTF-8" (str invalid-encoding)
                  "L1-SOURCE-ENCODING"]]]
          (testing label
            (let [result (run-public (str temp-root) true source-path)
                  diagnostic (:diagnostic result)]
              (is (= 1 (:exit result)) result)
              (is (str/blank? (:out result)) result)
              (assert-no-raw-host-error result)
              (is (= expected-id (:id diagnostic)) diagnostic)
              (is (= source-path
                     (get-in diagnostic [:source-span :source]))
                  diagnostic))))
        (finally
          (delete-tree! temp-root))))
    (is true
        "Set GRAVITY_SH07_B46_PUBLIC=1 for source-boundary containment.")))
