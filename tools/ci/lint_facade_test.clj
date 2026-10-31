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

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
