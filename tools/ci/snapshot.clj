(ns ci.snapshot
  "Experimental source archive; not a built compiler distribution."
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [clojure.string :as str])
  (:import [java.security MessageDigest]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])

(def directory (io/file "target/experimental"))

(defn git [& args]
  (let [{:keys [exit out err]} (apply shell/sh "git" args)]
    (when-not (zero? exit) (throw (ex-info "Git failed" {:args args :error err})))
    (str/trim out)))

(defn sha256 [file]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (with-open [input (io/input-stream file)]
      (let [buffer (byte-array 65536)]
        (loop []
          (let [n (.read input buffer)]
            (when (pos? n) (.update digest buffer 0 n) (recur))))))
    (apply str (map #(format "%02x" (bit-and 255 %)) (.digest digest)))))

(defn build! []
  (let [sha (git "rev-parse" "HEAD")
        tree (git "rev-parse" "HEAD^{tree}")
        expected (System/getenv "GITHUB_SHA")
        archive (io/file directory (str "uniform-experimental-source-" sha ".tar.gz"))
        second-archive (io/file "target" "snapshot-rebuild.tar.gz")]
    (when (and expected (not= sha expected))
      (throw (ex-info "Checkout is not the event's exact commit" {:expected expected :actual sha})))
    (when-not (str/blank? (git "status" "--porcelain" "--untracked-files=no"))
      (throw (ex-info "Tracked checkout must be clean" {})))
    (.mkdirs directory)
    (doseq [[id file] [["source-archive" archive] ["source-archive-rebuild" second-archive]]]
      (let [result (ci/execute! id 60 ["git" "archive" "--format=tar.gz"
                                       (str "--prefix=uniform-" sha "/")
                                       "--output" (str file) sha])]
        (when-not (= :passed (:status result)) (throw (ex-info "Archive failed" result)))))
    (when-not (= (sha256 archive) (sha256 second-archive))
      (throw (ex-info "Repeated source archive mismatch" {})))
    (io/delete-file second-archive)
    (spit (io/file directory "CAPABILITIES.md")
          (str "# Experimental source snapshot\n\nExact commit: `" sha "`.\n\n"
               "Source archive only; no compiler binary was built. Requires Clojure/JVM.\n"
               "Not seedless, not a complete language/runtime, not a production release.\n"
               "The genuine current-source Core/fresh-verification gate is unavailable/nonpassing.\n"
               "No compiler stage equivalence, native execution, signature, SBOM or release acceptance claim.\n"
               "Historical artifacts inside the archive retain their original scope and status.\n"
               "The identical archive rebuild checks Git packaging bytes only.\n\n"
               "## Recent changes\n\n```text\n" (git "log" "-10" "--format=%h %s") "\n```\n"))
    (spit (io/file directory "provenance.edn")
          (str (pr-str {:schema :uniform/experimental-source-snapshot-v1
                        :kind :source-archive :commit sha :tree tree
                        :archive-sha256 (sha256 archive)
                        :recipe "tools/ci/snapshot.clj: git archive twice, compare SHA-256"
                        :recipe-sha256 (sha256 (io/file "tools/ci/snapshot.clj"))
                        :deps-sha256 (sha256 (io/file "deps.edn"))
                        :clojure-version (clojure-version)
                        :java-version (System/getProperty "java.version")
                        :os (System/getProperty "os.name")
                        :run-url (str (System/getenv "GITHUB_SERVER_URL") "/"
                                      (System/getenv "GITHUB_REPOSITORY") "/actions/runs/"
                                      (System/getenv "GITHUB_RUN_ID"))
                        :run-attempt (System/getenv "GITHUB_RUN_ATTEMPT")
                        :source-archive-rebuild-equal true
                        :compiled false :seedless false :signed false :sbom false
                        :compiler-reproducibility :unavailable
                        :core-fresh-verification :unavailable-nonpassing}) "\n"))
    (spit (io/file directory "SHA256SUMS")
          (apply str (for [file (sort-by #(.getName %) (.listFiles directory))
                           :when (not= "SHA256SUMS" (.getName file))]
                       (str (sha256 file) "  " (.getName file) "\n"))))
    (println "Experimental source archive" sha)))

(build!)
(shutdown-agents)
