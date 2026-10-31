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
(defn utf8 [text] (.getBytes ^String text "UTF-8"))

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

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
