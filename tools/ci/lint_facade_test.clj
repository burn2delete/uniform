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
              (is (= (or expected-types (if (zero? expected) [] [:unused-private-var]))
                     (mapv :type (:blocking report))))))))
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

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
