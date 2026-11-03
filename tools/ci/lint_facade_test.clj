(ns ci.lint-facade-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint] '[ci.lint-context :as context] '[ci.lint-macros :as macros])

(def fragment (str context/facade-prefix "04-envelope-valid.clj"))
(def actual (context/facade-plan! context/local-bytes!))
(def c2-fragment (str context/c2-pass-cache-prefix "source_snapshot.clj"))
(def c2-actual (context/c2-pass-cache-plan! context/local-bytes!))
(def git-evidence-fragment
  (str context/git-evidence-prefix "04-predicate-spec-vector.clj"))
(def git-evidence-actual (context/git-evidence-plan! context/local-bytes!))
(defn utf8 [text] (.getBytes ^String text "UTF-8"))
(defn c2-entry [resources]
  (utf8 (str "(ns gravity.c2-pass-cache)\n"
             (apply str (map #(str "(load \"/gravity/c2_pass_cache/" % "\")\n")
                             resources)))))
(def historical-c2-inputs
  (let [current (:inputs c2-actual)]
    (into (assoc current context/c2-pass-cache
                 (c2-entry context/c2-pass-cache-historical-resources))
          (for [resource context/c2-pass-cache-historical-resources
                :let [path (str context/c2-pass-cache-prefix resource ".clj")]]
            [path (or (get current path) (utf8 "(def historical-c2-resource true)\n"))]))))

(defn observe! [plan]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-facade-lint-" (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (let [view (context/assemble! directory plan)
            result (sh/sh "clj-kondo" "--repro" "--cache" "false"
                          "--config-dir" "tools/ci/kondo" "--config" "{:output {:format :edn}}"
                          "--lint" "-" :in (slurp (:file view)))
            raw (edn/read-string (:out result))
            findings (mapv #(context/remap! view %) (:findings raw))]
        (is (#{0 2 3} (:exit result)) (pr-str result))
        (is (= (:exit result) (cond (pos? (get-in raw [:summary :error])) 3
                                    (pos? (get-in raw [:summary :warning])) 2 :else 0)))
        findings)
      (finally (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest actual-facade-resolves-the-selected-fragment
  (is (= 6 (count (:paths actual))))
  (is (= (:paths actual) (:paths (context/selected-plan! context/local-bytes! [fragment]))))
  (let [findings (observe! actual)]
    (is (not-any? #(= :error (:level %)) findings) (pr-str findings))
    (is (empty? (filter #(and (= fragment (:filename %))
                              (#{:warning :error} (:level %))) findings)) (pr-str findings)))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.authenticated-artifact-pipeline))))

(deftest actual-c2-facade-resolves-the-selected-fragment
  (is (= 11 (count (:paths c2-actual))))
  (is (= (:paths c2-actual) (:paths (context/selected-plan! context/local-bytes! [c2-fragment]))))
  (let [findings (observe! c2-actual)]
    (is (not-any? #(= :error (:level %)) findings) (pr-str findings))
    (is (empty? (filter #(and (= c2-fragment (:filename %))
                              (#{:warning :error} (:level %))) findings)) (pr-str findings)))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.c2-pass-cache))))

(deftest actual-git-evidence-facade-resolves-the-selected-fragment
  (is (= 25 (count (:paths git-evidence-actual))))
  (is (= context/git-evidence (first (:paths git-evidence-actual))))
  (is (= (str context/git-evidence-prefix "23-cleanup-unpublished.clj")
         (last (:paths git-evidence-actual))))
  (is (= (:paths git-evidence-actual)
         (:paths (context/selected-plan! context/local-bytes!
                                         [git-evidence-fragment]))))
  (let [findings (observe! git-evidence-actual)]
    (is (not-any? #(= :error (:level %)) findings) (pr-str findings))
    (is (empty? (filter #(and (= git-evidence-fragment (:filename %))
                              (#{:warning :error} (:level %))) findings))
        (pr-str findings)))
  (is (nil? (find-ns 'gravity.bootstrap)))
  (is (nil? (find-ns 'gravity.tools.sh07-b51-git-evidence))))

(deftest historical-c2-facade-recipe-remains-readable
  (let [read-inputs (fn [path] (or (get historical-c2-inputs path)
                                   (throw (ex-info "Missing historical C2 fixture" {:path path}))))
        plan (context/c2-pass-cache-plan! read-inputs)
        selected (context/selected-plan! read-inputs [context/c2-pass-cache])
        entry (String. ^bytes (get historical-c2-inputs context/c2-pass-cache) "UTF-8")]
    (is (= 17 (count (:paths plan))))
    (is (= (:paths plan) (:paths selected)))
    (is (some #(str/ends-with? % "/directories.clj") (:paths plan)))
    (is (some #(str/ends-with? % "/publication.clj") (:paths plan)))
    (is (thrown? Exception
                 (context/c2-pass-cache-plan!
                  (fn [path]
                    (if (= path context/c2-pass-cache)
                      (utf8 (str/replace entry "/gravity/c2_pass_cache/lookup\""
                                         "/gravity/c2_pass_cache/unreviewed\""))
                      (read-inputs path))))))
    (is (thrown? Exception
                 (context/c2-pass-cache-plan!
                  (fn [path]
                    (if (= path context/c2-pass-cache)
                      (utf8 (str entry "(load \"/gravity/c2_pass_cache/unreviewed\")\n"))
                      (read-inputs path))))))))

(deftest introduced-symbol-and-changed-context-still-block
  (doseq [plan [(update-in actual [:inputs fragment]
                           #(utf8 (str (String. ^bytes % "UTF-8") "\n(def bad (unknown-facade-function))\n")))
                (update-in actual [:inputs (str context/facade-prefix "03-revalidate-payload.clj")]
                           #(utf8 (str/replace (String. ^bytes % "UTF-8")
                                               "(defn- parent-ids" "(defn- renamed-parent-ids")))]]
    (let [findings (filterv #(and (= fragment (:filename %)) (= :error (:level %))) (observe! plan))]
      (is (some #(= :unresolved-symbol (:type %)) findings) (pr-str findings))
      ;; Even an identical baseline error cannot be inherited.
      (is (= findings (:blocking (lint/classify findings findings {})))))))

(deftest unsupported-or-malformed-load-context-fails-closed
  (let [entry (String. ^bytes (get-in actual [:inputs context/facade]) "UTF-8")
        inputs (:inputs actual)
        read-inputs (fn [data] (fn [path] (or (get data path) (throw (ex-info "Missing resource" {:path path})))))]
    (doseq [changed [(str/replace entry "gravity.authenticated-artifact-pipeline" "gravity.wrong-facade")
                     (str/replace entry "04-envelope-valid" "04-different-resource")
                     (str entry "\n(load \"/unreviewed\")")]]
      (is (thrown? Exception (context/facade-plan! (read-inputs (assoc inputs context/facade (utf8 changed)))))))
    (is (thrown? Exception (context/facade-plan! (read-inputs (dissoc inputs fragment)))))
    (doseq [body ["(def broken [" "(in-ns 'gravity.other)" "(load \"/other\")"]]
      (is (thrown? Exception (context/facade-plan! (read-inputs (assoc inputs fragment (utf8 body)))))))
    (is (thrown? Exception (context/selected-plan! (read-inputs inputs)
                                                   [(str context/facade-prefix "unregistered.clj")])))))

(deftest unsupported-or-malformed-c2-load-context-fails-closed
  (let [entry (String. ^bytes (get-in c2-actual [:inputs context/c2-pass-cache]) "UTF-8")
        inputs (:inputs c2-actual)
        read-inputs (fn [data] (fn [path] (or (get data path) (throw (ex-info "Missing resource" {:path path})))))]
    (doseq [changed [(str/replace entry "gravity.c2-pass-cache" "gravity.wrong-c2")
                     (str/replace entry "source_snapshot" "different-resource")
                     (str entry "\n(load \"/gravity/c2_pass_cache/unreviewed\")")]]
      (is (thrown? Exception (context/c2-pass-cache-plan!
                              (read-inputs (assoc inputs context/c2-pass-cache (utf8 changed)))))))
    (is (thrown? Exception (context/c2-pass-cache-plan!
                            (read-inputs (dissoc inputs c2-fragment)))))
    (doseq [body ["(def broken [" "(in-ns 'gravity.other)"
                  "(load \"/gravity/c2_pass_cache/unreviewed\")"
                  "(clojure.core/load \"/gravity/c2_pass_cache/unreviewed\")"
                  "(load-file \"/gravity/c2_pass_cache/unreviewed\")"
                  "(clojure.core/load-file \"/gravity/c2_pass_cache/unreviewed\")"]]
      (is (thrown? Exception (context/c2-pass-cache-plan!
                              (read-inputs (assoc inputs c2-fragment (utf8 body)))))))
    (let [adapter-request (str context/c2-pass-cache-prefix "adapter_request.clj")
          body (String. ^bytes (get inputs adapter-request) "UTF-8")]
      (is (thrown? Exception
                   (context/c2-pass-cache-plan!
                    (read-inputs (assoc inputs adapter-request
                                        (utf8 (str/replace body
                                                           "/gravity/c2_pass_cache/capacity_handoff"
                                                           "/gravity/c2_pass_cache/unreviewed"))))))))
    (is (thrown? Exception (context/selected-plan! (read-inputs inputs)
                                                   [(str context/c2-pass-cache-prefix
                                                         "unregistered.clj")])))))

(deftest unsupported-or-malformed-git-evidence-load-context-fails-closed
  (let [entry (String. ^bytes (get-in git-evidence-actual [:inputs context/git-evidence]) "UTF-8")
        inputs (:inputs git-evidence-actual)
        read-inputs (fn [data]
                      (fn [path]
                        (or (get data path)
                            (throw (ex-info "Missing resource" {:path path})))))]
    (doseq [changed [(str/replace entry "gravity.tools.sh07-b51-git-evidence"
                                  "gravity.wrong-git-evidence")
                     (str/replace entry "04-predicate-spec-vector"
                                  "04-different-resource")
                     (str entry "\n(load \"/gravity/tools/sh07_b51_git_evidence/unreviewed\")")]]
      (is (thrown? Exception
                   (context/git-evidence-plan!
                    (read-inputs (assoc inputs context/git-evidence (utf8 changed)))))))
    (is (thrown? Exception
                 (context/git-evidence-plan!
                  (read-inputs (dissoc inputs git-evidence-fragment)))))
    (doseq [body ["(def broken ["
                  "(in-ns 'gravity.other)"
                  "(load \"/gravity/tools/sh07_b51_git_evidence/unreviewed\")"]]
      (is (thrown? Exception
                   (context/git-evidence-plan!
                    (read-inputs (assoc inputs git-evidence-fragment (utf8 body)))))))
    (is (thrown? Exception
                 (context/selected-plan! (read-inputs inputs)
                                         [(str context/git-evidence-prefix
                                               "unregistered.clj")])))))

(deftest archived-context-rejects-unknown-loader-hash
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-archived-loader-" (make-array java.nio.file.attribute.FileAttribute 0)))
        archive (io/file directory "context.zip")
        inventory (io/file directory "inventory.txt")
        loader-bytes (utf8 "(def changed-loader true)\n")]
    (try
      (with-open [output (java.util.zip.ZipOutputStream. (io/output-stream archive))]
        (.putNextEntry output (java.util.zip.ZipEntry. context/loader))
        (.write output loader-bytes 0 (alength ^bytes loader-bytes))
        (.closeEntry output))
      (spit inventory (str "100644\t" context/loader "\u0000"))
      (let [error (try
                    (context/archived-plan! archive inventory [context/loader])
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (instance? clojure.lang.ExceptionInfo error))
        (is (= "Bootstrap loader changed; lint context requires independent review"
               (.getMessage error)))
        (is (= (context/sha256 loader-bytes) (:loader-sha (ex-data error)))))
      (finally
        (doseq [file (reverse (file-seq directory))] (.delete file))))))

(defn fixture-git! [directory & arguments]
  (let [result (apply sh/sh (concat ["git"] arguments [:dir (str directory)]))]
    (when-not (zero? (:exit result))
      (throw (ex-info "Lint fixture Git operation failed" result)))
    (str/trim (:out result))))

(defn compare-added-fragment! [mode]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-added-fragment-" (make-array java.nio.file.attribute.FileAttribute 0)))
        output (io/file directory "observations")
        existing (str context/source-root "gravity/bootstrap_impl/components/lint-fixture/existing.clj")
        added (str context/source-root "gravity/bootstrap_impl/components/lint-fixture/added.clj")
        marker (io/file directory "source-must-not-execute")
        read-local context/local-bytes!
        entry #(hash-map :resource (subs % (count context/source-root)))
        original "(defn fixture-existing [value] (let [unused 1] value))\n"
        baseline {context/bootstrap (read-local context/bootstrap)
                  context/loader (read-local context/loader)
                  context/manifest (utf8 (pr-str [(entry existing)]))
                  existing (utf8 original)
                  macros/source (read-local macros/source)}
        current (cond-> (assoc baseline
                               context/manifest (utf8 (pr-str (cond-> [(entry existing)]
                                                                (not= mode :unregistered) (conj (entry added)))))
                               existing (utf8 (str "\n" original))
                               added (utf8 (str (if (= mode :invalid)
                                                  "(def fixture-added (missing-current-symbol))\n"
                                                  "(def fixture-added (fixture-existing 1))\n")
                                                "(spit " (pr-str (str marker)) " \"must-not-run\")\n")))
                  (= mode :missing) (dissoc added))
        write-inputs! (fn [inputs]
                        (doseq [[path bytes] inputs]
                          (let [target (io/file directory path)]
                            (io/make-parents target)
                            (java.nio.file.Files/write (.toPath target) ^bytes bytes
                                                       (make-array java.nio.file.OpenOption 0)))))
        commands (atom [])
        execute! (fn [id _ argv & [stdin-path]]
                   (swap! commands conj [id argv])
                   (let [options (concat (when (= "git" (first argv)) [:dir (str directory)])
                                         (when stdin-path [:in (slurp stdin-path)]))
                         result (apply sh/sh (concat argv options))
                         receipt {:status (if (zero? (:exit result)) :passed :failed)
                                  :exit-code (:exit result)}]
                     (spit (io/file output (str id ".log")) (:out result))
                     (spit (io/file output (str id ".edn")) (pr-str receipt))
                     receipt))]
    (try
      (write-inputs! baseline)
      (fixture-git! directory "init" "--quiet")
      (fixture-git! directory "add" ".")
      (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                    "commit" "--quiet" "-m" "Baseline fragment")
      (let [base (fixture-git! directory "rev-parse" "HEAD")]
        (write-inputs! current)
        (fixture-git! directory "add" ".")
        (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                      "commit" "--quiet" "-m" "Current fragment")
        (.mkdir output)
        (with-redefs [context/local-bytes! (fn [path]
                                             (or (get current path)
                                                 (when-not (= path added) (read-local path))
                                                 (throw (ex-info "Missing current fixture source" {:path path}))))]
          (if (#{:missing :unregistered} mode)
            (do
              (is (thrown? Exception (lint/run-comparison! execute! output base [existing added])))
              (is (not-any? #(= "clj-kondo" (first (second %))) @commands)))
            (let [exit (lint/run-comparison! execute! output base [existing added])
                  report (lint/read-one! (io/file output "changed-lint-report.edn"))]
              (is (= (if (= mode :invalid) 1 0) exit))
              (is (= [existing] (get-in report [:context-selection :base])))
              (is (= #{existing added} (set (get-in report [:context-selection :current]))))
              (is (some #(and (= existing (:filename %)) (= :unused-binding (:type %)))
                        (:baseline-warnings report)))
              (if (= mode :invalid)
                (is (some #(and (= added (:filename %)) (= :error (:level %))
                                (= :unresolved-symbol (:type %))) (:blocking report)))
                (is (empty? (:blocking report))))))))
      (is (not (.exists marker)))
      (is (nil? (find-ns 'gravity.bootstrap)))
      (finally (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest new-fragments-use-complete-current-context-and-only-existing-baseline-paths
  (doseq [mode [:valid :invalid :missing :unregistered]]
    (compare-added-fragment! mode)))

(defn run-batched-files-fixture!
  ([unknown-filename?] (run-batched-files-fixture! unknown-filename? false))
  ([unknown-filename? same-namespace?]
   (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                             "uniform-batched-lint-"
                             (make-array java.nio.file.attribute.FileAttribute 0)))
         workspace-root (str (io/file "target" (str "batched-lint-" (System/nanoTime))))
         output (io/file directory "observations")
         first-file (str (io/file workspace-root "fixture/first.clj"))
         second-file (str (io/file workspace-root "fixture/second.clj"))
         paths [first-file second-file]
         baseline (if same-namespace?
                    {first-file (utf8 "(ns fixture.shared)\n(defn call-first [value] (missing-only-in-second value))\n")
                     second-file (utf8 "(ns fixture.shared)\n(defn missing-only-in-second [value] value)\n")
                     macros/source (context/local-bytes! macros/source)}
                    {first-file (utf8 "(ns fixture.first)\n(let [unused 1] 1)\n")
                     second-file (utf8 "(ns fixture.second)\n(+ 1 2)\n")
                     macros/source (context/local-bytes! macros/source)})
         current (cond
                   same-namespace? (assoc baseline first-file
                                          (utf8 "\n(ns fixture.shared)\n(defn call-first [value] (missing-only-in-second value))\n"))
                   unknown-filename? (assoc baseline first-file
                                            (utf8 "\n(ns fixture.first)\n(let [unused 1] 1)\n"))
                   :else {first-file (utf8 "\n(ns fixture.first)\n(let [unused 1] 1)\n")
                          second-file (utf8 "(ns fixture.second)\n(let [new-unused 1] 1)\n(missing-current-symbol)\n")
                          macros/source (context/local-bytes! macros/source)})
         write-inputs! (fn [inputs]
                         (doseq [[path bytes] inputs]
                           (let [file (io/file path)
                                 target (if (.isAbsolute file) file (io/file directory path))]
                             (io/make-parents target)
                             (java.nio.file.Files/write (.toPath target) ^bytes bytes
                                                        (make-array java.nio.file.OpenOption 0))
                             (when (some #{path} paths)
                               (io/make-parents file)
                               (java.nio.file.Files/write (.toPath file) ^bytes bytes
                                                          (make-array java.nio.file.OpenOption 0))))))
         commands (atom [])
         execute! (fn [id _ argv & [stdin-path]]
                    (swap! commands conj [id argv])
                    (let [options (concat [:dir (str directory)]
                                          (when stdin-path [:in (slurp stdin-path)]))
                          result (apply sh/sh (concat argv options))
                          output-text (:out result)
                          raw (when (and unknown-filename? (= id "lint-current"))
                                (edn/read-string output-text))
                          log (if raw
                                (pr-str (-> raw
                                            (update :findings conj
                                                    {:filename "unknown.clj"
                                                     :level :warning
                                                     :row 1 :col 1
                                                     :message "Unknown fixture finding"
                                                     :type :unused-binding})
                                            (update-in [:summary :warning] inc)))
                                output-text)
                          receipt {:status (if (or (not (zero? (:exit result))) raw)
                                             :failed :passed)
                                   :exit-code (if raw 2 (:exit result))}]
                      (spit (io/file output (str id ".log")) log)
                      (spit (io/file output (str id ".edn")) (pr-str receipt))
                      receipt))]
     (try
       (doseq [file (file-seq (io/file "tools/ci/kondo")) :when (.isFile file)]
         (let [target (io/file directory (.getPath file))]
           (io/make-parents target)
           (io/copy file target)))
       (write-inputs! baseline)
       (fixture-git! directory "init" "--quiet")
       (fixture-git! directory "add" ".")
       (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                     "commit" "--quiet" "-m" "Batched lint baseline")
       (let [base (fixture-git! directory "rev-parse" "HEAD")]
         (write-inputs! current)
         (fixture-git! directory "add" ".")
         (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                       "commit" "--quiet" "-m" "Batched lint current")
         (.mkdir output)
         (let [slurp* clojure.core/slurp
               read-current (fn [path]
                              (if-let [bytes (get current path)]
                                (String. ^bytes bytes "UTF-8")
                                (slurp* path)))]
           (with-redefs [clojure.core/slurp read-current]
             (if unknown-filename?
               (is (thrown? clojure.lang.ExceptionInfo
                            (lint/run-comparison! execute! output base paths)))
               (let [exit (lint/run-comparison! execute! output base paths)
                     report (lint/read-one! (io/file output "changed-lint-report.edn"))
                     kondo-commands (filter #(= "clj-kondo" (first (second %))) @commands)]
                 (is (= 1 exit))
                 (if same-namespace?
                   (do
                     (is (= 4 (count kondo-commands)) (pr-str @commands))
                     (is (= ["lint-baseline" "lint-baseline-1"
                             "lint-current" "lint-current-1"]
                            (mapv first kondo-commands)))
                     (is (every? #(= 1 (count (rest (drop-while (fn [argument]
                                                                  (not= "--lint" argument))
                                                                (second %)))))
                                 kondo-commands))
                     (is (some #(and (= :unresolved-symbol (:type %))
                                     (= :error (:level %))) (:blocking report))))
                   (do
                     (is (= 4 (count kondo-commands)) (pr-str @commands))
                     (is (= ["lint-baseline" "lint-baseline-1"
                             "lint-current" "lint-current-1"]
                            (mapv first kondo-commands)))
                     (is (every? #(= 1 (count (rest (drop-while (fn [argument]
                                                                  (not= "--lint" argument))
                                                                (second %)))))
                                 kondo-commands))
                     (is (= 1 (count (:baseline-warnings report))) (pr-str report))
                     (is (some #(and (= :unused-binding (:type %))
                                     (= :warning (:level %))) (:blocking report)))
                     (is (some #(and (= :unresolved-symbol (:type %))
                                     (= :error (:level %))) (:blocking report)))))))
             (when unknown-filename?
               (is (= 3 (count (filter #(= "clj-kondo" (first (second %))) @commands)))
                   (pr-str @commands))))))
       (finally
         (doseq [file (reverse (file-seq directory))]
           (.delete file))
         (when (.exists (io/file workspace-root))
           (doseq [file (reverse (file-seq (io/file workspace-root)))]
             (.delete file))))))))

(deftest ordinary-clojure-files-run-in-isolation
  (run-batched-files-fixture! false))

(deftest ordinary-files-reject-unknown-kondo-filenames
  (run-batched-files-fixture! true))

(deftest same-namespace-ordinary-files-stay-isolated
  (run-batched-files-fixture! false true))

(deftest ordinary-lint-grouping-batches-only-data-files
  (let [group! (var-get (ns-resolve 'ci.lint 'ordinary-lint-groups))]
    (is (= [] (group! [])))
    (is (= [["a.clj"] ["b.clj"]]
           (group! ["a.clj" "b.clj"])))
    (is (= [["a.clj"] ["b.edn" "c.edn"] ["d.clj"]]
           (group! ["a.clj" "b.edn" "c.edn" "d.clj"])))
    (is (= [["a.edn" "b.edn"] ["c.clj"]]
           (group! ["a.edn" "b.edn" "c.clj"])))
    (is (= [["a.clj"] ["b.edn"]]
           (group! ["a.clj" "b.edn"])))))

(deftest macro-admission-is-required-for-context-and-source-inputs
  (let [needed? (var-get (ns-resolve 'ci.lint 'macro-admission-needed?))]
    (is (needed? [] false))
    (is (not (needed? ["deps.edn"] false)))
    (is (needed? ["source.gravity"] false))
    (is (needed? ["data.edn" "source.gravity"] false))
    (is (needed? [nil] false))
    (is (needed? [".clj-kondo"] false))
    (is (needed? [context/manifest] true))
    (is (needed? [macros/source] false))
    (is (needed? ["tools/ci/kondo/config.edn"] false))))

(deftest contextual-edn-retains-macro-admission-guard
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-context-edn-lint-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        output (io/file directory "observations")
        commands (atom [])
        local-reads (atom [])
        read-local context/local-bytes!
        execute! (fn [id _ argv & [_stdin-path]]
                   (swap! commands conj [id argv])
                   (if (= id "lint-macro-base-source")
                     (java.nio.file.Files/write
                      (.toPath (io/file output (str id ".log")))
                      ^bytes (read-local macros/source)
                      (make-array java.nio.file.OpenOption 0))
                     (spit (io/file output (str id ".log")) ""))
                   (spit (io/file output (str id ".edn"))
                         (pr-str {:status :passed :exit-code 0}))
                   {:status :passed :exit-code 0})]
    (try
      (.mkdir output)
      ;; Context-plan validation may run before this guard; that phase order is
      ;; not a contract. Stub it here and assert only guard reachability before Kondo.
      (let [error (try
                    (with-redefs [context/selected-plan! (fn [& _] nil)
                                  context/local-bytes!
                                  (fn [path]
                                    (swap! local-reads conj path)
                                    (if (= path macros/source)
                                      (utf8 "(ns gravity.c2-artifact-identity)\n")
                                      (read-local path)))]
                      (lint/run-comparison! execute! output "fixture-base" [context/manifest]))
                    nil
                    (catch Exception exception exception))]
        (is (instance? Exception error))
        (is (some #{macros/source} @local-reads))
        (is (some #(= "lint-macro-base-source" (first %)) @commands))
        (is (not-any? #(= "clj-kondo" (first (second %))) @commands)))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (.delete file))))))

(deftest ordinary-edn-lint-skips-macro-admission-reads
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-data-only-lint-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        output (io/file directory "observations")
        data-path "deps.edn"
        data-bytes (java.nio.file.Files/readAllBytes (.toPath (io/file data-path)))
        commands (atom [])
        local-reads (atom [])
        result-edn (pr-str {:findings [] :summary {:error 0 :warning 0}})
        execute! (fn [id _ argv & [_stdin-path]]
                   (swap! commands conj [id argv])
                   (let [log (io/file output (str id ".log"))
                         receipt (io/file output (str id ".edn"))]
                     (cond
                       (= id "lint-base-inventory") (spit log (str data-path "\u0000"))
                       (= id "lint-base-source-0")
                       (java.nio.file.Files/write (.toPath log) ^bytes data-bytes
                                                  (make-array java.nio.file.OpenOption 0))
                       (#{"lint-baseline" "lint-current"} id) (spit log result-edn)
                       (= id "lint-config-diff") (spit log "")
                       :else (spit log ""))
                     (spit receipt (pr-str {:status :passed :exit-code 0}))
                     {:status :passed :exit-code 0}))]
    (try
      (.mkdir output)
      (with-redefs [context/local-bytes!
                    (fn [path]
                      (swap! local-reads conj path)
                      (throw (ex-info "ordinary data lint must not read macro inputs"
                                      {:path path})))]
        (is (= 0 (lint/run-comparison! execute! output "fixture-base" [data-path])))
        (let [report (lint/read-one! (io/file output "changed-lint-report.edn"))]
          (is (= :not-applicable (get-in report [:macro-contract :status])))
          (is (= :ordinary-data-only (get-in report [:macro-contract :reason])))))
      (is (empty? @local-reads))
      (is (not-any? #(= "lint-macro-base-source" (first %)) @commands))
      (is (not (.exists (io/file output "lint-macro-current-source.clj"))))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (.delete file))))))

(deftest pinned-kondo-data-files-have-equivalent-batched-findings
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-data-lint-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        first-file (str (io/file directory "first.edn"))
        second-file (str (io/file directory "second.edn"))
        argv (fn [files]
               (into ["clj-kondo" "--repro" "--cache" "false"
                      "--config-dir" "tools/ci/kondo"
                      "--config" "{:output {:format :edn}}" "--lint"] files))
        run! (fn [files] (apply sh/sh (argv files)))
        read-result (fn [result] (edn/read-string (:out result)))
        finding-view (fn [finding]
                       (select-keys finding [:filename :type :row :col :message :level :langs]))]
    (try
      (spit first-file "{:fixture :valid}\n")
      (spit second-file "{:fixture [}\n")
      (let [batch (run! [first-file second-file])
            first-result (run! [first-file])
            second-result (run! [second-file])
            batch-data (read-result batch)
            first-data (read-result first-result)
            second-data (read-result second-result)
            isolated-findings (mapv finding-view
                                    (concat (:findings first-data) (:findings second-data)))]
        (is (= 3 (:exit batch)) (pr-str batch))
        (is (= 0 (:exit first-result)) (pr-str first-result))
        (is (= 3 (:exit second-result)) (pr-str second-result))
        (is (= isolated-findings (mapv finding-view (:findings batch-data)))
            (pr-str {:batch batch-data :isolated [first-data second-data]}))
        (is (= 2 (get-in batch-data [:summary :files])) (pr-str batch-data))
        (is (= 1 (get-in first-data [:summary :files])) (pr-str first-data))
        (is (= 1 (get-in second-data [:summary :files])) (pr-str second-data))
        (is (= (select-keys (:summary second-data) [:error :warning :info :type])
               (select-keys (:summary batch-data) [:error :warning :info :type]))
            (pr-str {:batch batch-data :second second-data})))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (.delete file))))))

(defn private-call-plan [call & [required]]
  {:paths ["fixture-owner.clj" "fixture-caller.clj"]
   :entrypoints #{"fixture-owner.clj" "fixture-caller.clj"}
   :namespace-by-path {"fixture-owner.clj" 'fixture.private-owner "fixture-caller.clj" 'fixture.private-caller}
   :inputs {"fixture-owner.clj" (utf8 (str "(ns fixture.private-owner)\n"
                                           "(defn- held [value] value)\n"
                                           "(defn- dead [value] value)\n"))
            "fixture-caller.clj" (utf8 (str "(ns fixture.private-caller (:require ["
                                            (or required "fixture.private-owner") " :as local]))\n" call "\n"))}})

(deftest real-linter-private-var-calls-have-source-backed-usage-provenance
  (doseq [reference ["local/held" "fixture.private-owner/held"]]
    (let [plan (private-call-plan (str "(defn call-helper [] (#'" reference " 1))"))
          raw (observe! plan)
          result (context/resolve-private-usages raw (context/private-var-usages [plan]))
          edge (first (:resolved result))]
      (is (= #{"Unused private var fixture.private-owner/held"
               "Unused private var fixture.private-owner/dead"} (set (map :message raw))))
      (is (= 'held (get-in edge [:definition :name])))
      (is (= "fixture-owner.clj" (get-in edge [:definition :path])))
      (is (= (context/sha256 (get-in plan [:inputs "fixture-owner.clj"]))
             (get-in edge [:definition :sha256])))
      (is (= 'fixture.private-caller (get-in edge [:callers 0 :caller-namespace])))
      (is (= (symbol reference) (get-in edge [:callers 0 :reference])))
      (is (= (context/sha256 (get-in plan [:inputs "fixture-caller.clj"]))
             (get-in edge [:callers 0 :sha256])))
      (is (= 'fixture.private-owner (get-in edge [:callers 0 :require :namespace])))
      (is (= "fixture-caller.clj" (get-in edge [:callers 0 :require :path])))
      (is (= (context/sha256 (get-in plan [:inputs "fixture-caller.clj"]))
             (get-in edge [:callers 0 :require :sha256])))
      (is (= ["Unused private var fixture.private-owner/dead"] (mapv :message (:findings result))))
      (is (= (:findings result) (:blocking (lint/classify [] (:findings result) {})))))))

(deftest data-wrong-bindings-and-lost-calls-cannot-count-as-private-invocations
  (doseq [[call required]
          [["(defn call-helper [] (#'local/held 1))" "fixture.other-owner"]
           ["(defn call-helper [] (#'wrong/held 1))" nil]
           ["(defn call-helper [] (#'local/other 1))" nil]
           ["(def data '(#'local/held 1))" nil]
           ["(def data `(#'local/held 1))" nil]
           ["(comment (#'local/held 1))" nil]
           ["(def data #'local/held)" nil]
           ["(defn call-helper [] 1)" nil]]]
    (let [plan (private-call-plan call required)
          raw (observe! plan)
          result (context/resolve-private-usages raw (context/private-var-usages [plan]))]
      (is (empty? (:resolved result)))
      (is (= raw (:findings result)))
      (is (some #(= "Unused private var fixture.private-owner/held" (:message %))
                (:blocking (lint/classify [] (:findings result) {}))))))
  (let [plan (private-call-plan "(defn call-helper [] (#'local/held 1) (new-unresolved-symbol))")
        raw (observe! plan)
        result (context/resolve-private-usages raw (context/private-var-usages [plan]))]
    (is (= 1 (count (:resolved result))))
    (is (some #(and (= :unresolved-symbol (:type %)) (= :error (:level %)))
              (:blocking (lint/classify [] (:findings result) {})))))
  (let [live (private-call-plan "(defn call-helper [] (#'local/held 1))")
        dead (private-call-plan "(defn call-helper [] 1)")
        before (context/resolve-private-usages (observe! live) (context/private-var-usages [live]))
        after (context/resolve-private-usages (observe! dead) (context/private-var-usages [dead]))
        source "fixture-owner.clj"
        text (String. ^bytes (get-in live [:inputs source]) "UTF-8")
        mapped {source {:changes [] :before (str/split-lines text) :after (str/split-lines text)}}
        blocking (:blocking (lint/classify (:findings before) (:findings after) mapped))]
    (is (= ["Unused private var fixture.private-owner/held"]
           (mapv :message (filter #(= :unused-private-var (:type %)) blocking))))
    (is (some #(= :unused-namespace (:type %)) blocking))))

(deftest executable-case-positions-and-lexical-comment-bindings
  (doseq [call ["(defn f [x] (case (#'local/held x) :hit :yes :no))"
                "(defn f [x] (case x :hit (#'local/held x) :no))"
                "(defn f [x] (case x :hit :yes (#'local/held x)))"
                "(defn f [] (let [comment identity] (comment (#'local/held 1))))"
                "(defn f [comment] (comment (#'local/held 1)))"
                "(defn f [] ((fn [comment] (comment (#'local/held 1))) identity))"
                "(defn f [] (let [{:keys [comment]} {:comment identity}] (comment (#'local/held 1))))"
                "(defn f [] (let [[comment] [identity]] (comment (#'local/held 1))))"
                "(defn f [] (letfn [(comment [x] x)] (comment (#'local/held 1))))"
                "(defn f [] (let [case identity] (case (#'local/held 1))))"]]
    (let [plan (private-call-plan call)
          result (context/resolve-private-usages (observe! plan) (context/private-var-usages [plan]))]
      (is (= ['held] (mapv #(get-in % [:definition :name]) (:resolved result))) call)))
  (doseq [call ["(defn f [x] (case x (#'local/held) :hit :miss))"
                "(defn f [] (comment (#'local/held 1)))"
                "(defn f [] (let [comment identity] (clojure.core/comment (#'local/held 1))))"
                "(defn f [] (let [comment identity] (comment 1)) (comment (#'local/held 1)))"
                "(defn f [] (let [comment (comment (#'local/held 1))] comment))"
                "(defn f [] (if-let [comment identity] (comment 1) (comment (#'local/held 1))))"]]
    (let [plan (private-call-plan call)
          result (context/resolve-private-usages (observe! plan) (context/private-var-usages [plan]))]
      (is (empty? (:resolved result)) call))))

(deftest destructuring-defaults-use-executable-initializers-and-their-binding-scopes
  (doseq [call ["(defn f [] (let [{:keys [value] :or {value (#'local/held 1)}} {}] value))"
                "(defn f [] (let [[{:keys [value] :or {value (#'local/held 1)}}] [{}]] value))"
                "(defn f [] (let [comment identity {:keys [value] :or {value (comment (#'local/held 1))}} {}] value))"
                "(defn f [] (let [case identity {:keys [value] :or {value (case (#'local/held 1))}} {}] value))"
                "(defn f [{:keys [value] :or {value (#'local/held 1)}}] value)"
                "(defn f [[{:keys [value] :or {value (#'local/held 1)}}]] value)"
                "(defn f [{:keys [value] :or {value (comment (#'local/held 1))}} comment] value)"
                "(defn f [] (if-let [{:keys [value] :or {value (#'local/held 1)}} {}] value :miss))"
                "(defn f [] (when-let [{:keys [value] :or {value (#'local/held 1)}} {}] value))"]]
    (let [plan (private-call-plan call)
          result (context/resolve-private-usages (observe! plan) (context/private-var-usages [plan]))]
      (is (= ['held] (mapv #(get-in % [:definition :name]) (:resolved result))) call)))
  (doseq [call ["(defn f [] (let [{:keys [value] :or {value '(#'local/held 1)}} {}] value))"
                "(defn f [] (let [{:keys [value] :or {unused (#'local/held 1)}} {}] value))"
                "(defn f [] (let [{value '(#'local/held 1)} {}] value))"
                "(defn f [] (let [{:keys [value] :or {value (#'wrong/held 1)}} {}] value))"
                "(defn f [] (let [{:keys [value] :or {value (#'local/other 1)}} {}] value))"
                "(defn f [] (let [{:keys [comment] :or {comment (comment (#'local/held 1))}} {}] comment))"
                "(defn f [{:keys [comment] :or {comment (comment (#'local/held 1))}}] comment)"
                "(defn f [] (let [{:keys [comment] :or {comment identity}} {}] (comment 1)) (comment (#'local/held 1)))"
                "(defn f [] (let [{:keys [comment] :or {comment identity}} (comment (#'local/held 1))] comment))"
                "(defn f [x] (let [{:keys [value] :or {value (case x (#'local/held) :hit :miss)}} {}] value))"]]
    (let [plan (private-call-plan call)
          result (context/resolve-private-usages (observe! plan) (context/private-var-usages [plan]))]
      (is (empty? (:resolved result)) call)
      (is (some #(= "Unused private var fixture.private-owner/held" (:message %))
                (:blocking (lint/classify [] (:findings result) {}))) call))))

(defn compare-private-programs! [private-before? body expected & [expected-types]]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-private-scope-" (make-array java.nio.file.attribute.FileAttribute 0)))
        owner-path (io/file directory "fixture/owner.clj")
        caller-path (io/file directory "fixture/caller.clj")
        owner #(str "(ns fixture.owner)\n(defn" (when % "-") " hidden [x] x)\n(def public-value :present)\n")
        caller #(str "(ns fixture.caller (:require [fixture.owner :as h]))\n"
                     "(def public h/public-value)\n(defn use-hidden [x] " % ")\n")]
    (try
      (doseq [file (file-seq (io/file "tools/ci")) :when (.isFile file)]
        (let [target (io/file directory (.getPath file))]
          (io/make-parents target)
          (io/copy file target)))
      (let [target (io/file directory macros/source)]
        (io/make-parents target)
        (io/copy (io/file macros/source) target))
      (let [target (io/file directory "src/gravity/process_supervision.clj")]
        (io/make-parents target)
        (io/copy (io/file "bootstrap/clojure/src/gravity/process_supervision.clj") target))
      (io/make-parents owner-path)
      (spit owner-path (owner private-before?))
      (spit caller-path (caller "(#'h/hidden x)"))
      (fixture-git! directory "init" "--quiet")
      (fixture-git! directory "add" ".")
      (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                    "commit" "--quiet" "-m" "Live baseline")
      (let [base (fixture-git! directory "rev-parse" "HEAD")]
        (spit owner-path (owner true))
        (spit caller-path (caller body))
        (fixture-git! directory "add" ".")
        (fixture-git! directory "-c" "user.name=Lint fixture" "-c" "user.email=lint@example.invalid"
                      "commit" "--quiet" "-m" "Current executable positions")
        (let [result (sh/sh "clojure" "-J-Xmx256m" "-Srepro" "-Sdeps"
                            "{:deps {org.clojure/clojure {:mvn/version \"1.12.5\"}}}"
                            "-M" "tools/ci/lint.clj" base "fixture/owner.clj" "fixture/caller.clj"
                            :dir (str directory))
              report-path (io/file directory "target/ci/changed-lint-report.edn")]
          (is (= expected (:exit result)) (pr-str result))
          (is (.isFile report-path))
          (when (.isFile report-path)
            (let [report (lint/read-one! report-path)]
              (is (= (if (zero? expected) 1 0) (count (get-in report [:resolved-private-usages :current]))))
              (is (= (set (or expected-types (if (zero? expected) [] [:unused-private-var])))
                     (set (mapv :type (:blocking report)))))))))
      (finally (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest case-data-and-shadowed-comment-run-through-the-complete-lint-pipeline
  ;; The live baseline has no unused allowance after source-backed resolution.
  (compare-private-programs! true "(case x (#'h/hidden) :hit :miss)" 1)
  (compare-private-programs! true "(comment (#'h/hidden x))" 1)
  ;; The previously public owner supplies no baseline unused-warning allowance.
  (compare-private-programs! false "(let [comment identity] (comment (#'h/hidden x)))" 0)
  (compare-private-programs! false "(case x :hit (#'h/hidden x) :miss)" 0))

(deftest destructuring-defaults-run-through-the-complete-lint-pipeline
  (compare-private-programs! true "(let [{:keys [value] :or {value (#'h/hidden x)}} {}] value)" 0)
  (compare-private-programs! true "(let [{:keys [value] :or {value '(#'h/hidden x)}} {}] value)" 1
                             [:unused-private-var :unused-binding])
  (compare-private-programs! true "(let [{:keys [value] :or {unused (#'h/hidden x)}} {}] value)" 1
                             [:unused-private-var :unbound-destructuring-default]))

(deftest source-reader-resolves-declared-aliases-without-loading-namespaces
  (let [before (ns-aliases *ns*)
        source (str "(ns fixture.reader-unloaded (:require [fixture.never-load :as native]\n"
                    " [fixture.also-never-load :as-alias only]))\n"
                    "(def values [::own ::native/value #::only{:key 1}])\n"
                    "(require '[fixture.later-never-load :as later])\n"
                    "(def later-value ::later/key)")
        forms (context/source-forms! "fixture.clj" (utf8 source))]
    (is (= [:fixture.reader-unloaded/own :fixture.never-load/value
            {:fixture.also-never-load/key 1}]
           (nth (nth forms 1) 2)))
    (is (= :fixture.later-never-load/key (nth (nth forms 3) 2)))
    (is (= before (ns-aliases *ns*)))
    (doseq [namespace '[fixture.reader-unloaded fixture.never-load fixture.also-never-load fixture.later-never-load]]
      (is (nil? (find-ns namespace)))))
  (doseq [source ["(ns fixture.unknown) ::absent/key"
                  "(ns fixture.unknown) ::context/key"
                  "(ns fixture.first (:require [fixture.owner :as local])) (ns fixture.second) ::local/key"
                  "'(ns fixture.quoted (:require [fixture.owner :as local])) ::local/key"]]
    (is (thrown? Exception (context/source-forms! "unknown.clj" (utf8 source))))))

(deftest source-reader-refuses-tags-and-reader-evaluation-before-side-effects
  (let [effects (atom [])]
    (binding [*data-readers* {'fixture/touch #(do (swap! effects conj %) %)}]
      (doseq [source ["#fixture/touch 1" "#inst \"2026-10-31\""
                      "#uuid \"00000000-0000-0000-0000-000000000000\""
                      "#=(spit \"source-reader-must-not-write\" \"forbidden\")"]]
        (is (thrown? Exception (context/source-forms! "refused.clj" (utf8 source))))))
    (is (empty? @effects))
    (is (not (.exists (io/file "source-reader-must-not-write"))))))

(deftest baseline-current-alias-scopes-retain-private-call-provenance
  (let [program
        (fn [target alias]
          {:paths ["owner.clj" "caller.clj"] :entrypoints #{"owner.clj" "caller.clj"}
           :namespace-by-path {"owner.clj" target "caller.clj" 'fixture.alias-caller}
           :inputs {"owner.clj" (utf8 (str "(ns " target ")\n(defn- held [x] x)\n(defn- dead [] nil)\n"))
                    "caller.clj" (utf8 (str "(ns fixture.alias-caller (:require [" target " :as " alias "]))\n"
                                            "(def fixture-key ::" alias "/once-fixtures)\n"
                                            "(defn call [x] (#'" alias "/held x))\n"))}})
        baseline (program 'fixture.baseline 'old)
        current (program 'fixture.current 'native)]
    (doseq [[plan target alias] [[baseline 'fixture.baseline 'old] [current 'fixture.current 'native]]]
      (let [source (context/private-var-usages [plan])
            resolved (context/resolve-private-usages (observe! plan) source)
            calls (vec (mapcat :callers source))]
        (is (= 1 (count calls)))
        (is (= target (:namespace (first calls))))
        (is (= alias (:alias (first calls))))
        (is (= (context/sha256 (get-in plan [:inputs "caller.clj"])) (:sha256 (first calls))))
        (is (= 1 (count (:resolved resolved))))
        (is (some #(= :unused-private-var (:type %)) (:findings resolved)))))
    (is (thrown? Exception
                 (context/source-forms! "current-with-stale-alias.clj"
                                        (utf8 "(ns fixture.current (:require [fixture.current :as native])) ::old/key"))))))

(deftest syntax-quote-retains-core-classes-and-source-declaration-scope
  (let [read-source #(context/source-forms! "syntax.clj" (utf8 %))
        symbols #(set (filter symbol? (tree-seq coll? seq %)))
        basic (symbols (read-source "(ns never.syntax) (def v `(map String clojure.core/inc java.lang/String))"))]
    (is (contains? basic 'clojure.core/map))
    (is (contains? basic 'java.lang.String))
    (is (contains? basic 'clojure.core/inc))
    (is (contains? basic 'java.lang/String))
    (is (not (contains? basic 'never.syntax/map)))
    (is (not (contains? basic 'never.syntax/String)))
    (let [scoped (symbols
                  (read-source (str "(ns never.scoped (:refer-clojure :exclude [map] :rename {inc increment})"
                                    " (:require [never.library :refer [foreign] :rename {foreign renamed} :as n])"
                                    " (:import (java.util Date)))\n"
                                    "(def local 1) (clojure.core/defn str [] nil)\n"
                                    "(def v `(map increment renamed n/qualified Date Date. local str))")))]
      (doseq [name '[never.scoped/map clojure.core/inc never.library/foreign never.library/qualified
                     java.util.Date java.util.Date. never.scoped/local never.scoped/str]]
        (is (contains? scoped name) (str name))))
    (let [only (symbols (read-source "(ns never.only (:refer-clojure :only [map])) (def v `(map inc))"))]
      (is (contains? only 'clojure.core/map))
      (is (contains? only 'never.only/inc)))
    (is (nil? (find-ns 'never.syntax)))
    (is (nil? (find-ns 'never.scoped)))
    (is (nil? (find-ns 'never.library)))
    (let [imported (symbols (read-source "(ns never.import (:import never.init.MustNotLoad)) (def v `MustNotLoad)"))]
      (is (contains? imported 'never.init.MustNotLoad)))
    (doseq [source ["(ns never.all (:require [never.library :refer :all]))"
                    "(ns never.use (:use never.library))"
                    "(ns never.refer) (refer 'never.library)"
                    "(ns never.switch) (in-ns 'never.other)"
                    "(ns never.shadow) (def String nil)"
                    "(ns never.shadow (:require [never.lib :refer [String]]))"]]
      (is (thrown? Exception (read-source source))))))

(deftest prefix-libspec-aliases-are-expanded-only-in-reader-scope
  (doseq [source ["(ns never.prefix (:require (clojure [set :as s]))) (def k ::s/value)"
                  "(ns never.prefix) (require '(clojure [set :as s])) (def k ::s/value)"
                  "(ns never.prefix (:require (never (nested [library :as s])))) (def k ::s/value)"
                  "(ns never.prefix) (require '(never (nested [library :as s]))) (def k ::s/value)"]]
    (let [before (set (map ns-name (all-ns)))
          forms (context/source-forms! "prefix.clj" (utf8 source))
          target (if (str/includes? source "clojure") :clojure.set/value :never.nested.library/value)]
      (is (= target (nth (last forms) 2)))
      (is (= before (set (map ns-name (all-ns)))))
      (is (= 'never.prefix (second (first forms)))))))

(deftest source-reader-refuses-ambiguous-bindings-and-keeps-private-prefix-provenance
  (let [parse #(context/source-forms! "ambiguous.clj" (utf8 %))]
    (doseq [source ["(ns never.ambiguous (:require [never.x :as a] [never.y :as a])) ::a/x"
                    "(ns never.ambiguous (:require [never.x :refer [value]] [never.y :refer [value]])) value"
                    "(ns never.ambiguous (:import (java.lang String) (java.util String))) String"]]
      (is (thrown? Exception (parse source)) source))
    (let [forms (parse (str "(ns never.same (:require [never.x :as a] [never.x :as a]"
                            " [never.x :refer [value]] [never.x :refer [value]]))"
                            " ::a/x value"))]
      (is (= :never.x/x (nth forms 1)))
      (is (= 'value (nth forms 2))))
    (is (nil? (find-ns 'never.ambiguous)))
    (is (nil? (find-ns 'never.same))))
  (let [plan {:paths ["prefix-owner.clj" "prefix-caller.clj"]
              :entrypoints #{"prefix-owner.clj" "prefix-caller.clj"}
              :namespace-by-path {"prefix-owner.clj" 'never.prefix-owner
                                  "prefix-caller.clj" 'never.prefix-caller}
              :inputs {"prefix-owner.clj" (utf8 "(ns never.prefix-owner)\n(defn- held [x] x)\n")
                       "prefix-caller.clj" (utf8 (str "(ns never.prefix-caller "
                                                      "(:require (never [prefix-owner :as o])))\n"
                                                      "(defn call [x] (#'o/held x))\n"))}}
        usages (context/private-var-usages [plan])
        edge (first (mapcat :callers usages))]
    (is (= 1 (count usages)))
    (is (= 'never.prefix-owner (:namespace edge)))
    (is (= 'o (:alias edge)))
    (is (= 'o/held (:reference edge)))
    (is (= 'never.prefix-owner (get-in edge [:require :namespace])))
    (is (= (context/sha256 (get-in plan [:inputs "prefix-caller.clj"]))
           (:sha256 edge)
           (get-in edge [:require :sha256])))
    (is (nil? (find-ns 'never.prefix-owner)))
    (is (nil? (find-ns 'never.prefix-caller)))))

(deftest declaration-heads-respect-prior-source-bindings
  (let [parse #(context/source-forms! "head-scope.clj" (utf8 %))
        shadow-require (str "(ns never.shadow (:refer-clojure :exclude [require]))\n"
                            "(defn require [& xs] nil)\n"
                            "(require '[foreign.library :refer [map]])\n(def v `map)")
        shadow-import (str "(ns never.shadow (:refer-clojure :exclude [import]))\n"
                           "(defn import [& xs] nil)\n"
                           "(import 'foreign.init.MustNotLoad)\n(def v `MustNotLoad)")
        before (set (map ns-name (all-ns)))]
    (is (= '(def v (quote clojure.core/map)) (last (parse shadow-require))))
    (is (= '(def v (quote never.shadow/MustNotLoad)) (last (parse shadow-import))))
    (is (= '(def v (quote clojure.core/map))
           (last (parse (str "(ns never.bound (:refer-clojure :exclude [require])) (def require identity)\n"
                             "(require '[foreign.library :refer [map]]) (def v `map)")))))
    (is (= '(def v (quote never.bound/MustNotLoad))
           (last (parse (str "(ns never.bound (:refer-clojure :exclude [import])) (def import identity)\n"
                             "(import 'foreign.init.MustNotLoad) (def v `MustNotLoad)")))))
    (is (= '(def v (quote clojure.core/map))
           (last (parse (str "(ns never.referred (:refer-clojure :exclude [require])"
                             " (:require [foreign.api :refer [require]]))\n"
                             "(require '[foreign.library :refer [map]]) (def v `map)")))))
    (is (= '(def v (quote never.referred/MustNotLoad))
           (last (parse (str "(ns never.referred (:refer-clojure :exclude [import])"
                             " (:require [foreign.api :refer [import]]))\n"
                             "(import 'foreign.init.MustNotLoad) (def v `MustNotLoad)")))))
    (is (= :clojure.set/value
           (last (parse (str shadow-require "\n(clojure.core/require '(clojure [set :as s])) ::s/value")))))
    (is (= '(def q (quote foreign.init.MustNotLoad))
           (last (parse (str shadow-import "\n(clojure.core/import 'foreign.init.MustNotLoad) (def q `MustNotLoad)")))))
    (is (= :clojure.set/value
           (last (parse (str "(ns never.renamed (:refer-clojure :rename {require need}))\n"
                             "(need '(clojure [set :as s])) ::s/value")))))
    (is (= :clojure.set/value
           (last (parse (str "(ns never.core-alias (:require [clojure.core :as c]))\n"
                             "(c/require '(clojure [set :as s])) ::s/value")))))
    (is (thrown? Exception
                 (parse (str shadow-require "\n(require '[foreign.library :as invented]) ::invented/value"))))
    (is (= before (set (map ns-name (all-ns)))))))

(deftest literal-core-alias-declarations-follow-read-order-and-resolved-heads
  (let [parse #(context/source-forms! "literal-alias.clj" (utf8 %))
        before (set (map ns-name (all-ns))) ambient (ns-aliases *ns*)]
    (doseq [declaration ["(alias 'late 'never.target)"
                         "(clojure.core/alias 'late 'never.target)"
                         "(link 'late 'never.target)"
                         "(c/alias 'late 'never.target)"]]
      (let [source (str "(ns never.literal (:refer-clojure :rename {alias link}) (:require [clojure.core :as c])) "
                        declaration " (def key ::late/value) (def q `late/held) (def v #'late/held)")
            source (if (str/starts-with? declaration "(alias ")
                     (str/replace source " :rename {alias link}" "") source)
            forms (parse source)]
        (is (= '(def key :never.target/value) (nth forms 2)))
        (is (= '(def q (quote never.target/held)) (nth forms 3)))
        (is (= '(def v (var late/held)) (nth forms 4)))))
    (is (= :never.target/value
           (last (parse "(ns never.same) (alias 'late 'never.target) (alias 'late 'never.target) ::late/value"))))
    (is (= :never.target/value
           (last (parse "(ns never.explicit) (def alias nil) (clojure.core/alias 'late 'never.target) ::late/value"))))
    (doseq [source ["(ns never.before) ::late/value (alias 'late 'never.target)"
                    "(ns never.reset) (alias 'late 'never.target) (ns never.reset2) ::late/value"
                    "(ns never.local) (defn alias [& xs] nil) (alias 'late 'never.target) ::late/value"
                    "(ns never.local) (def alias nil) (alias 'late 'never.target) ::late/value"
                    "(ns never.foreign (:require [never.foreign-lib :refer [alias]])) (alias 'late 'never.target) ::late/value"
                    "(ns never.excluded (:refer-clojure :exclude [alias])) (alias 'late 'never.target) ::late/value"
                    "(ns never.renamed (:refer-clojure :rename {alias link})) (alias 'late 'never.target) ::late/value"
                    "(ns never.local-link (:refer-clojure :rename {alias link})) (def link nil) (link 'late 'never.target) ::late/value"
                    "(ns never.foreign-head (:require [never.foreign-lib :as c])) (c/alias 'late 'never.target) ::late/value"
                    "(ns never.nested) (do (alias 'late 'never.target)) ::late/value"
                    "(ns never.nested) (defn f [] (alias 'late 'never.target)) ::late/value"
                    "(ns never.nested) (comment (alias 'late 'never.target)) ::late/value"
                    "(ns never.conflict) (alias 'late 'never.one) (alias 'late 'never.two)"
                    "(ns never.conflict (:require [never.one :as late])) (alias 'late 'never.two)"
                    "(ns never.class) (alias 'String 'never.target)"
                    "(ns never.unknown) ::unknown/value"]]
      (is (thrown? Exception (parse source)) source))
    (doseq [declaration ["(alias)" "(alias 'late)" "(alias 'late 'never.target 'extra)"
                         "(alias late 'never.target)" "(alias 'late target)"
                         "(alias (symbol \"late\") 'never.target)" "(alias 'late (identity 'never.target))"
                         "(alias :late 'never.target)" "(alias '[late] 'never.target)"
                         "(alias 'late/x 'never.target)" "(alias 'late 'never/target)"
                         "(alias '^:private late 'never.target)"]]
      (is (thrown? Exception (parse (str "(ns never.malformed) " declaration))) declaration))
    (is (= before (set (map ns-name (all-ns)))))
    (is (= ambient (ns-aliases *ns*)))
    (is (nil? (find-ns 'never.target)))))

(deftest top-level-alias-private-calls-retain-source-and-require-provenance
  (let [make-plan (fn [target alias]
                    {:paths ["owner.clj" "caller.clj"]
                     :namespace-by-path {"owner.clj" target "caller.clj" 'never.alias-caller}
                     :inputs {"owner.clj" (utf8 (str "(ns " target ") (defn- held [x] x) (defn- dead [x] x)"))
                              "caller.clj" (utf8 (str "(ns never.alias-caller (:require [" target " :as original]))\n"
                                                      "(alias '" alias " '" target ")\n"
                                                      "(defn call [x] (#'" alias "/held x))\n"))}})
        baseline (make-plan 'never.old-owner 'old)
        current (make-plan 'never.current-owner 'late)]
    (doseq [[plan target alias] [[baseline 'never.old-owner 'old] [current 'never.current-owner 'late]]]
      (let [[{:keys [definition callers]} :as usages] (context/private-var-usages [plan])
            call (first callers) findings (observe! plan)
            resolved (context/resolve-private-usages findings usages)]
        (is (= 1 (count usages)))
        (is (= target (:namespace definition) (:namespace call)))
        (is (= alias (:alias call)))
        (is (= (symbol (str alias) "held") (:reference call)))
        (is (= 'original (get-in call [:require :alias])))
        (is (= (context/sha256 (get-in plan [:inputs "caller.clj"])) (:sha256 call) (get-in call [:require :sha256])))
        (is (not-any? #(= :error (:level %)) findings))
        (is (some #(= (str "Unused private var " target "/dead") (:message %)) (:findings resolved)))
        (is (not-any? #(= (str "Unused private var " target "/held") (:message %)) (:findings resolved)))))
    (let [unrequired (update-in current [:inputs "caller.clj"]
                                (constantly (utf8 "(ns never.alias-caller) (alias 'late 'never.current-owner) (defn call [x] (#'late/held x))")))]
      (is (empty? (context/private-var-usages [unrequired]))))
    (is (nil? (find-ns 'never.alias-caller)))
    (is (nil? (find-ns 'never.current-owner)))))

(deftest literal-alias-private-edges-are-not-retroactive-or-foreign-declarations
  (let [owner 'never.ordered-owner
        header "(ns never.ordered-caller (:require [never.ordered-owner :as original] [clojure.core :as c]))\n"
        usages (fn [source]
                 (context/private-var-usages
                  [{:paths ["owner.clj" "caller.clj"]
                    :inputs {"owner.clj" (utf8 "(ns never.ordered-owner) (defn- held [x] x)")
                             "caller.clj" (utf8 source)}}]))
        call "(defn call [x] (#'late/held x))\n"]
    (doseq [declaration ["(def alias nil) (alias 'late 'never.ordered-owner)"
                         "(defn alias [& xs] nil) (alias 'late 'never.ordered-owner)"
                         "(do (alias 'late 'never.ordered-owner))"
                         "(comment (alias 'late 'never.ordered-owner))"]]
      (is (empty? (usages (str header declaration "\n" call)))))
    (let [[{:keys [callers definition]}] (usages (str header call "(alias 'late 'never.ordered-owner)\n" call))]
      (is (= owner (:namespace definition)))
      (is (= 1 (count callers)))
      (is (= 4 (:row (first callers)))))
    (is (= owner (get-in (usages (str header "(c/alias 'late 'never.ordered-owner)\n" call)) [0 :definition :namespace])))
    (is (empty? (usages (str header "(alias 'late 'never.ordered-owner)\n(ns never.reset-caller)\n" call))))))

(deftest malformed-foreign-quote-carriers-are-refused-without-callbacks
  (let [calls (atom 0)
        callback (fn [] (swap! calls inc) (throw (AssertionError. "foreign seq")))
        foreign (reify clojure.lang.ISeq
                  (seq [_] (callback)) (first [_] (callback)) (next [_] (callback))
                  (more [_] (callback)) (cons [_ _] (callback)) (count [_] (callback))
                  (empty [_] (callback)) (equiv [_ _] (callback)))
        bad-quote (clojure.lang.Cons. 'quote foreign)
        check #'context/source-alias-pair!]
    (doseq [form [foreign (list 'alias foreign '(quote never.target))
                  (list 'alias bad-quote '(quote never.target))]]
      (is (thrown? Exception (check form))))
    (is (zero? @calls))))

(deftest alias-declaration-head-and-drop-mutations-are-detected
  (let [parse #(context/source-forms! "alias-mutant.clj" (utf8 %))
        resolve-head @#'context/source-declaration-operation
        local "(ns never.mutant) (def alias nil) (alias 'late 'never.target) ::late/value"
        actual "(ns never.mutant) (alias 'late 'never.target) ::late/value"]
    (is (thrown? Exception (parse local)))
    (is (= :never.target/value (last (parse actual))))
    (is (= :never.target/value
           (with-redefs-fn {#'context/source-declaration-operation
                            (fn [scope form] (if (= 'alias (when (seq? form) (first form)))
                                               'alias (resolve-head scope form)))}
             #(last (parse local)))))
    (is (thrown? Exception
                 (with-redefs-fn {#'context/source-declaration-operation
                                  (fn [scope form] (let [operation (resolve-head scope form)]
                                                     (when-not (= 'alias operation) operation)))}
                   #(parse actual))))))

(deftest actual-C13-compatibility-source-is-inert-and-keeps-late-Vars
  (let [path (str context/source-root "gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-comparison-fold-reconstruction.clj")
        base (sh/sh "git" "show" (str "41ab1c357e4d96a510fae4cf5a46e903fe340510^:" path))
        pinned (sh/sh "git" "show" (str "41ab1c357e4d96a510fae4cf5a46e903fe340510:" path))
        current (context/local-bytes! path) before (set (map ns-name (all-ns)))
        forms (context/source-forms! path current)
        output (first (filter #(and (seq? %) (= 'output-id (second %))) forms))]
    (is (zero? (:exit base)))
    (is (zero? (:exit pinned)))
    (is (= (:out pinned) (String. ^bytes current "UTF-8")))
    (is (seq (context/source-forms! path (utf8 (:out base)))))
    (is (= '(alias (quote bootstrap) (quote gravity.bootstrap)) (second forms)))
    (is (= '(optimization/output-id (var bootstrap/p15-s23-c11-mir-digest)
                                    (var bootstrap/p15-s23-c11-mir-path-neutral-value) output)
           (last output)))
    (is (= :gravity.bootstrap/value
           (last (context/source-forms! path (utf8 (str (String. ^bytes current "UTF-8") "\n::bootstrap/value"))))))
    (is (some #{path} (:paths (context/plan! context/local-bytes!))))
    (is (= before (set (map ns-name (all-ns)))))
    (is (nil? (find-ns 'gravity.bootstrap-c13-comparison)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.optimization)))))

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
