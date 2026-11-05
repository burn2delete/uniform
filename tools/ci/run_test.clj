(ns ci.run-test
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])
(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint])
(require '[ci.lint-context :as context])
(binding [*command-line-args* nil] (load-file "tools/ci/lint_context_test.clj"))

(defn fixture-tool!
  ([directory argv input allowed-exits]
   (fixture-tool! directory argv input allowed-exits directory))
  ([directory argv input allowed-exits log-directory]
   (let [log (if log-directory
               (java.io.File/createTempFile "tool-" ".log" log-directory)
               (java.io.File/createTempFile "tool-" ".log"))
         process (.start (doto (ProcessBuilder. ^java.util.List argv)
                           (.directory directory) (.redirectErrorStream true) (.redirectOutput log)))]
     (try
       (with-open [stdin (.getOutputStream process)]
         (.write stdin (.getBytes input java.nio.charset.StandardCharsets/UTF_8)))
       (when-not (.waitFor process 10 java.util.concurrent.TimeUnit/SECONDS)
         (throw (ex-info "Fixture tool timed out" {:argv argv})))
       (when-not (contains? allowed-exits (.exitValue process))
         (throw (ex-info "Fixture tool failed" {:argv argv :log (slurp log)})))
       (slurp log)
       (finally
         (when (.isAlive process) (.destroyForcibly process))
         (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS))))))

(defn git-fixture! [directory args]
  (fixture-tool! directory (into ["git"] args) "" #{0} nil))

(defn initialize-git-fixture! [directory]
  (git-fixture! directory ["init" "-q"])
  (git-fixture! directory ["config" "user.email" "ci-fixture@example.invalid"])
  (git-fixture! directory ["config" "user.name" "CI fixture"]))

(defn write-fixture-file! [directory path content]
  (let [file (io/file directory path)]
    (when-let [parent (.getParentFile file)] (.mkdirs parent))
    (spit file content)))

(defn commit-fixture! [directory message]
  (git-fixture! directory ["add" "-A"])
  (git-fixture! directory ["commit" "-q" "-m" message])
  (str/trim (git-fixture! directory ["rev-parse" "HEAD"])))

(defn delete-fixture-tree! [directory]
  (doseq [file (reverse (file-seq directory))]
    (io/delete-file file true)))

(defn assert-git-selection! [initial-files change expected?]
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-selection-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (initialize-git-fixture! directory)
      (doseq [[path content] initial-files]
        (write-fixture-file! directory path content))
      (let [base (commit-fixture! directory "base")]
        (change directory)
        (let [head (commit-fixture! directory "change")
              raw (git-fixture! directory ["diff" "--raw" "--no-renames" "-z" base head])
              changes (ci/parse-raw-diff raw)]
          (is (= expected? (ci/docs-only-change? "pull_request" base changes))
              (pr-str changes))
          changes))
      (finally (delete-fixture-tree! directory)))))

(defn lint-fixture! [directory before after]
  (spit (io/file directory "before.clj") before)
  (spit (io/file directory "after.clj") after)
  (let [findings (fn [source]
                   (:findings (edn/read-string
                               (fixture-tool! directory
                                              ["clj-kondo" "--repro" "--cache" "false" "--config"
                                               "{:output {:format :edn}}" "--lint" "-" "--filename" "sample.clj"]
                                              source #{0 2 3}))))
        changes (lint/hunks (fixture-tool! directory
                                           ["git" "diff" "--no-index" "--no-ext-diff" "--no-textconv"
                                            "--text" "--unified=0" "--" "before.clj" "after.clj"] "" #{0 1}))]
    (lint/classify (findings before) (findings after)
                   {"sample.clj" {:before (str/split-lines before) :after (str/split-lines after)
                                  :changes changes}})))

(deftest actual-linter-and-git-diff-preserve-introduced-diagnostics
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-lint-" (make-array java.nio.file.attribute.FileAttribute 0)))]
    (try
      (doseq [[before after baseline blocking]
              [["(ns sample)\n(let [unused 1] :ok)\n" "(ns sample)\n\n(let [unused 1] :ok)\n" 1 0]
               ["(ns sample)\n(identity 1)\n" "(ns sample)\n(let [unused 1] :ok)\n" 0 1]
               ["(ns sample)\n(let [unused 1] :old)\n" "(ns sample)\n(let [unused 1] :new)\n" 0 1]
               ["(ns sample)\n(let [unused 1] :ok)\n" "(ns sample)\n(let [unused 1] :ok)\n(let [unused 1] :ok)\n" 1 1]
               ["(ns sample)\n(defn- helper [] 1)\n(helper)\n" "(ns sample)\n(defn- helper [] 1)\n" 0 1]
               ["(ns sample)\n" "(ns sample)\n(missing)\n" 0 1]
               ["(ns sample)\n(missing)\n" "(ns sample)\n\n(missing)\n" 0 1]]]
        (let [result (lint-fixture! directory before after)]
          (is (= baseline (count (:baseline-warnings result))) (pr-str result))
          (is (= blocking (count (:blocking result))) (pr-str result))))
      (finally (doseq [file (reverse (file-seq directory))] (io/delete-file file))))))

(deftest moved-definitions-inherit-only-unique-unchanged-warnings
  (let [base-text (str "(ns sample)\n"
                       "(defn- stable [] :ok)\n"
                       "(defn- moved []\n"
                       "  (let [unused 1]\n"
                       "    :ok))\n")
        current-text (str "(ns sample)\n"
                          "(defn- moved []\n"
                          "  (let [unused 1]\n"
                          "    :ok))\n"
                          "(defn- stable [] :ok)\n")
        baseline-warning {:filename "sample.clj" :row 4 :end-row 4
                          :col 9 :end-col 15 :level :warning
                          :type :unused-binding :message "unused binding unused"}
        current-warning (assoc baseline-warning :row 3 :end-row 3)
        sources (fn [before after]
                  {"sample.clj"
                   {:changes [{:old-start 2 :old-count 4
                               :new-start 2 :new-count 3}]
                    :before (str/split-lines before)
                    :after (str/split-lines after)}})
        classify (fn [before after current]
                   (lint/classify [baseline-warning] [current]
                                  (sources before after)))]
    (let [result (classify base-text current-text current-warning)]
      (is (= [current-warning] (:baseline-warnings result)))
      (is (empty? (:blocking result))))
    (let [renamed-base (str/replace base-text "(ns sample)" "(ns sample.old)")
          renamed-current (str/replace current-text "(ns sample)" "(ns sample.new)")
          result (classify renamed-base renamed-current current-warning)]
      (is (empty? (:baseline-warnings result)))
      (is (= [current-warning] (:blocking result))))
    (let [control-base (str "(ns sample)\n"
                            "(set! *warn-on-reflection* true)\n"
                            (subs base-text (count "(ns sample)\n")))
          control-current (str "(ns sample)\n"
                               "(set! *warn-on-reflection* false)\n"
                               (subs current-text (count "(ns sample)\n")))
          control-baseline-warning (assoc baseline-warning :row 5 :end-row 5)
          control-current-warning (assoc current-warning :row 4 :end-row 4)
          result (lint/classify [control-baseline-warning] [control-current-warning]
                                {"sample.clj"
                                 {:changes [{:old-start 2 :old-count 5
                                             :new-start 2 :new-count 4}]
                                  :before (str/split-lines control-base)
                                  :after (str/split-lines control-current)}})]
      (is (empty? (:baseline-warnings result)))
      (is (= [control-current-warning] (:blocking result))))
    (let [crossing-base (str "(ns sample)\n"
                             "(set! *warn-on-reflection* true)\n"
                             "(in-ns 'sample)\n"
                             "(defn- stable [] :ok)\n"
                             "(defn- moved []\n"
                             "  (let [unused 1]\n"
                             "    :ok))\n")
          crossing-current (str "(ns sample)\n"
                                "(defn- moved []\n"
                                "  (let [unused 1]\n"
                                "    :ok))\n"
                                "(set! *warn-on-reflection* true)\n"
                                "(in-ns 'sample)\n"
                                "(defn- stable [] :ok)\n")
          crossing-baseline-warning (assoc baseline-warning :row 6 :end-row 6)
          crossing-current-warning (assoc current-warning :row 3 :end-row 3)
          result (lint/classify [crossing-baseline-warning] [crossing-current-warning]
                                {"sample.clj"
                                 {:changes [{:old-start 2 :old-count 6
                                             :new-start 2 :new-count 6}]
                                  :before (str/split-lines crossing-base)
                                  :after (str/split-lines crossing-current)}})]
      (is (empty? (:baseline-warnings result)))
      (is (= [crossing-current-warning] (:blocking result))))
    (let [changed-body (str/replace current-text "    :ok))" "    :changed))")
          result (classify base-text changed-body current-warning)]
      (is (empty? (:baseline-warnings result)))
      (is (= [current-warning] (:blocking result))))
    (let [duplicate-baseline (str base-text
                                  "(defn- moved []\n"
                                  "  (let [unused 1]\n"
                                  "    :ok))\n")
          result (classify duplicate-baseline current-text current-warning)]
      (is (empty? (:baseline-warnings result)))
      (is (= [current-warning] (:blocking result))))
    (let [duplicate-current (str current-text
                                 "(defn- moved []\n"
                                 "  (let [unused 1]\n"
                                 "    :ok))\n")
          result (classify base-text duplicate-current current-warning)]
      (is (empty? (:baseline-warnings result)))
      (is (= [current-warning] (:blocking result))))
    (let [error (assoc current-warning :level :error)
          result (classify base-text current-text error)]
      (is (empty? (:baseline-warnings result)))
      (is (= [error] (:blocking result))))
    (let [moved-to-new-file (assoc current-warning :filename "new.clj")
          result (lint/classify [baseline-warning] [moved-to-new-file]
                                {"new.clj" {:changes []
                                            :before nil
                                            :after (str/split-lines current-text)}})]
      (is (empty? (:baseline-warnings result)))
      (is (= [moved-to-new-file] (:blocking result))))
    (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                              "uniform-ci-moved-definition-"
                              (make-array java.nio.file.attribute.FileAttribute 0)))]
      (try
        (let [result (lint-fixture! directory base-text current-text)]
          (is (= 3 (count (:baseline-warnings result))) (pr-str result))
          (is (empty? (:blocking result)) (pr-str result)))
        (finally
          (doseq [file (reverse (file-seq directory))]
            (io/delete-file file true)))))))

(deftest definition-source-reader-is-non-evaluating
  (binding [*data-readers* {'ci/side-effect (fn [_]
                                              (throw (Exception. "reader executed")))}]
    (is (empty? (lint/source-definitions
                 (str/split-lines "(defn tagged [] #ci/side-effect 1)\n")))))
  (is (empty? (lint/source-definitions
               (str/split-lines "#=(do (def executed true) nil)\n")))))

(deftest source-mapping-loads-only-current-warnings
  (let [warning {:filename "small.clj" :row 1 :col 1 :end-row 1 :end-col 2
                 :level :warning :type :unused-binding :message "unused binding x"}
        error (assoc warning :filename "large.edn" :level :error)
        info (assoc warning :filename "large.edn" :level :info)
        sources {"small.clj" {:changes [] :before ["same"] :after ["same"]}
                 "large.edn" {:changes [] :before ["huge"] :after ["huge"]}}
        calls (atom 0)]
    ;; A large source with no warning is not selected for source loading; an
    ;; error is still a blocking finding and an info is still non-blocking.
    (is (= [] (lint/source-paths-for-warnings ["large.edn"] nil false [error info])))
    (is (= ["small.clj"]
           (lint/source-paths-for-warnings ["large.edn" "small.clj"] nil false
                                           [error warning info])))
    (with-redefs [lint/source-definitions (fn [_]
                                            (swap! calls inc)
                                            [])]
      (let [result (lint/classify [error] [error info] sources)]
        (is (= [error] (:blocking result)))
        (is (zero? @calls)))
      (let [result (lint/classify [warning] [warning] sources)]
        (is (= [warning] (:baseline-warnings result)))
        (is (empty? (:blocking result)))
        (is (= 2 @calls))))))

(deftest baseline-matching-and-output-validation-fail-closed
  (let [finding {:filename "sample.clj" :row 1 :col 2 :end-row 1 :end-col 3
                 :level :warning :type :unused-binding :message "unused binding x"}
        sources {"sample.clj" {:changes [] :before ["same"] :after ["same"]}}
        error (assoc finding :level :error)
        duplicate (lint/classify [finding] [finding finding] sources)
        directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-lint-output-" (make-array java.nio.file.attribute.FileAttribute 0)))
        log (io/file directory "observation.log")
        receipt (io/file directory "observation.edn")
        result {:findings [finding] :summary {:error 0 :warning 1}}]
    (try
      (is (= 1 (count (:baseline-warnings duplicate))))
      (is (= 1 (count (:blocking duplicate))))
      (is (= [finding] (:blocking (lint/classify [finding] [finding] {}))))
      (is (= [error] (:blocking (lint/classify [error] [error] sources))))
      (doseq [unknown [(dissoc finding :col) (dissoc finding :end-col) (dissoc finding :end-row)
                       (assoc finding :row 0) (assoc finding :end-row 0)
                       (assoc finding :end-row 2) (assoc finding :end-col 1)
                       (assoc finding :col 100 :end-col 101)]]
        (is (= [unknown] (:blocking (lint/classify [unknown] [unknown] sources)))))
      (spit log (pr-str result))
      (spit receipt (pr-str {:exit-code 2}))
      (is (= [finding] (lint/findings! log #{"sample.clj"} identity)))
      (doseq [[bad-result bad-exit]
              [[result 0]
               [(assoc-in result [:summary :error] 1) 2]
               [(assoc-in result [:findings 0 :filename] "unexpected.clj") 2]
               [(assoc-in result [:findings 0 :level] :unknown) 0]
               [{:findings []} 0]]]
        (spit log (pr-str bad-result))
        (spit receipt (pr-str {:exit-code bad-exit}))
        (is (thrown? clojure.lang.ExceptionInfo (lint/findings! log #{"sample.clj"} identity))))
      (spit log (str (pr-str result) "\n{:unexpected :trailing}"))
      (spit receipt (pr-str {:exit-code 2}))
      (is (thrown? clojure.lang.ExceptionInfo (lint/findings! log #{"sample.clj"} identity)))
      (spit log (pr-str result))
      (spit receipt "{:exit-code 2}\n:trailing")
      (is (thrown? clojure.lang.ExceptionInfo (lint/findings! log #{"sample.clj"} identity)))
      (finally (doseq [file (reverse (file-seq directory))] (io/delete-file file))))))

(defn alive? [pid]
  (let [handle (java.lang.ProcessHandle/of pid)]
    (and (.isPresent handle) (.isAlive (.get handle)))))

(deftest ci-sources-pass-the-actual-clojure-reader
  (binding [*read-eval* false]
    (is (thrown? RuntimeException (read-string "#=(+ 1 1)")))
    (is (thrown? IllegalArgumentException (read-string "^String \".\"")))
    (doseq [file (file-seq (io/file "tools/ci"))
            :when (and (.isFile file)
                       (.endsWith (.getName file) ".clj"))]
      (with-open [reader (java.io.PushbackReader. (io/reader file))]
        (let [eof (Object.)]
          (is (pos? (loop [forms 0]
                      (if (identical? eof (read {:eof eof} reader))
                        forms
                        (recur (inc forms)))))
              (str file)))))))

(deftest changed-path-base-is-consistent
  (doseq [base [nil "" "main;echo bad" (apply str (repeat 40 "0"))]]
    (is (= "HEAD^" (ci/base-ref base))))
  (let [sha (apply str (repeat 40 "a"))]
    (is (= sha (ci/base-ref sha)))))

(deftest documentation-only-selection-is-conservative
  (let [base (apply str (repeat 40 "a"))]
    (let [regular-doc (fn [path]
                        {:old-mode "100644" :new-mode "100644" :status "M" :path path})]
      (is (ci/docs-only-change? "pull_request" base
                                (mapv regular-doc ["README.md" "docs/guide.md"])))
      (doseq [paths [["language/runtime/src/gravity/runtime.gravity"]
                     [".github/workflows/ci.yml"]
                     [".github/README.md"]
                     ["tools/ci/README.md"]
                     ["bin/README.md"]
                     ["contracts/policy.md"]
                     ["specs/compiler.md"]
                     ["notes.txt"]
                     ["README.md; echo unexpected"]
                     []]]
        (is (not (ci/docs-only-change? "pull_request" base (mapv regular-doc paths)))
            (pr-str paths))))
    (is (not (ci/docs-only-change? "pull_request" nil
                                   [{:old-mode "100644" :new-mode "100644"
                                     :status "M" :path "README.md"}])))
    (is (not (ci/docs-only-change? "push" base
                                   [{:old-mode "100644" :new-mode "100644"
                                     :status "M" :path "README.md"}])))
    (doseq [mode ["bootstrap" "macro" "resolution"
                  "resolution-pipeline" "resolution-unmetered"
                  "resolution-invocation-selection" "checked-core-runtime"]]
      (is (ci/skip-for-docs-only? mode {:kind :docs-only}) mode))
    (is (not (ci/skip-for-docs-only? "static" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "contracts" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "leaves-portable" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "macro" {:kind :full})))
    (is (= [["whitespace" 30 ["git" "diff" "--check" base "HEAD"]]]
           (ci/docs-only-static-commands {:base base :kind :docs-only})))))

(deftest shared-selection-preserves-full-and-docs-contract-modes
  (let [full {:kind :full :reason :non-pull-request :paths []}
        docs-only {:kind :docs-only :reason :known-documentation-only
                   :paths ["README.md"]}
        selected {:kind :selected :reason :affected-stage
                  :modes #{"static" "contracts" "resolution"}}]
    (is (= ["static=true"
            "contracts=true"
            "bootstrap=true"
            "macro=true"
            "resolution=true"
            "checked-core-runtime=true"
            "leaves-portable=true"
            "leaves-darwin=true"
            "darwin-unsupported=true"
            "static-tools-required=true"
            "selection-valid=true"]
           (ci/selection-output-lines full)))
    (is (= ["static=true"
            "contracts=true"
            "bootstrap=false"
            "macro=false"
            "resolution=false"
            "checked-core-runtime=false"
            "leaves-portable=false"
            "leaves-darwin=false"
            "darwin-unsupported=true"
            "static-tools-required=false"
            "selection-valid=true"]
           (ci/selection-output-lines docs-only)))
    (is (= "true" (last (str/split (nth (ci/selection-output-lines selected) 4) #"="))))
    (is (= "true" (last (str/split (last (ci/selection-output-lines selected)) #"="))))))

(deftest workflow-selector-fails-closed-for-missing-or-malformed-outputs
  (let [workflow (slurp ".github/workflows/ci.yml")]
    (is (str/includes? workflow "needs.select.outputs.selection-valid != 'true'"))
    (is (str/includes? workflow "needs.select.outputs[matrix.mode] != 'false'"))
    (is (str/includes? workflow "needs.select.outputs[matrix.selection-key] != 'false'"))
    (is (str/includes? workflow "needs.select.outputs[matrix.mode] == 'false'"))
    (is (str/includes? workflow "CI_PR_HEAD_SHA: ${{ github.event.pull_request.head.sha }}"))
    (is (str/includes? workflow "CI_BASE_SHA: ${{ github.event.before }}"))
    (is (str/includes? workflow "fetch-depth: 2"))
    (is (str/includes? workflow "fetch-depth: 10"))
    (is (str/includes? workflow "&& 1 || 0"))
    (is (= 5 (count (re-seq #"persist-credentials: false" workflow))))
    (is (str/includes? workflow "needs.select.outputs.resolution != 'false'"))
    (is (str/includes? workflow "target/ci/$CI_MODE-summary.edn"))
    (is (str/includes? workflow "name: ci-${{ matrix.mode }}-${{ github.sha }}-${{ github.run_attempt }}"))))

(deftest leaf-runner-selection-is-fail-closed-in-leaves-job
  (let [workflow (slurp ".github/workflows/ci.yml")
        leaves (second (re-find #"(?s)\n  leaves:\n(.*?)(?=\n  snapshot:)" workflow))]
    (is (some? leaves))
    ;; Keep the runner expression on the leaves job, with an explicit Darwin
    ;; guard and a matrix-runner fallback.  Selector outputs and their valid
    ;; true/false cases are covered by shared-selection-preserves-full-and-docs-contract-modes.
    (is (= 1 (count (re-seq #"(?m)^    runs-on: \$\{\{.*\}\}$" (or leaves "")))))
    (is (str/includes? leaves "matrix.platform == 'darwin'"))
    (is (str/includes? leaves "needs.select.result == 'success'"))
    (is (str/includes? leaves "needs.select.outputs.selection-valid == 'true'"))
    (is (str/includes? leaves "needs.select.outputs[matrix.selection-key] == 'false'"))
    (is (str/includes? leaves "'ubuntu-24.04' || matrix.runner"))
    ;; The platform probes remain fail-closed: unselected Darwin skips the
    ;; probe, while selector failure or malformed output keeps it on macOS.
    (is (str/includes? leaves "matrix.platform == 'darwin' && (needs.select.result != 'success'"))
    (is (str/includes? leaves "matrix.platform == 'unsupported' && (needs.select.result != 'success'"))))

(defn source-deftest-names
  [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [names #{}]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          names
          (recur (if (and (seq? form)
                          (= 'deftest (first form))
                          (symbol? (second form)))
                   (conj names (str (second form)))
                   names)))))))

(defn option-value
  [argv option]
  (let [index (.indexOf ^java.util.List argv option)]
    (when (neg? index)
      (throw (ex-info "Expected command option is absent"
                      {:option option :argv argv})))
    (nth argv (inc index))))

(defn- option-values [argv option]
  (mapv second (filter #(= option (first %)) (partition 2 1 argv))))

(defn- macro-selected-vars
  ([argv source-vars]
   (macro-selected-vars argv source-vars "gravity.macro.pipeline-test"))
  ([argv source-vars namespace]
   (let [pairs (partition 2 1 argv)
         exact (set (map second (filter #(= "--exact" (first %)) pairs)))
         patterns (map #(re-pattern (second %)) (filter #(= "--regex" (first %)) pairs))]
     (if (some #{namespace} (option-values argv "--namespace"))
       (set (filter (fn [name]
                      (let [qualified (str namespace "/" name)]
                        (or (and (empty? exact) (empty? patterns))
                            (contains? exact name) (contains? exact qualified)
                            (some #(or (re-find % name) (re-find % qualified)) patterns))))
                    source-vars))
       #{}))))

(deftest contracts-include-the-complete-native-tooling-and-ownership-namespaces
  (let [commands (ci/commands-for-selection "contracts" {:kind :full})]
    (is (= ["docs" "roadmap" "roadmap-negative-controls" "dependency-controls"
            "tooling-native-validation" "ownership-native-validation" "wasm-native-validation"] (mapv first commands)))
    (doseq [[command [label namespace path]]
            (map vector (drop 4 commands)
                 [["tooling-native-validation" "gravity.tooling.pipeline-test"
                   "bootstrap/clojure/test/gravity/tooling/pipeline_test.clj"]
                  ["ownership-native-validation" "gravity.ownership.pipeline-test"
                   "bootstrap/clojure/test/gravity/ownership/pipeline_test.clj"]])]
      (let [[id timeout argv] command
            source-vars (binding [*read-eval* false] (source-deftest-names path))]
        (is (= label id))
        (is (= 180 timeout))
        (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
        (is (seq source-vars))
        (is (= source-vars (macro-selected-vars argv source-vars namespace)))
        (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
        (is (empty? (macro-selected-vars
                     (assoc argv (dec (count argv)) "gravity.mir.pipeline-test") source-vars namespace)))))))

(deftest contracts-include-the-complete-native-wasm-namespace
  (let [[label timeout argv] (last (ci/commands-for-selection "contracts" {:kind :full}))
        namespace "gravity.backend.wasm.pipeline-test"
        source-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/backend/wasm/pipeline_test.clj"))]
    (is (= "wasm-native-validation" label))
    (is (= 180 timeout))
    (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
    (is (seq source-vars))
    (is (= source-vars (macro-selected-vars argv source-vars namespace)))
    (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
    (is (empty? (macro-selected-vars
                 (assoc argv (dec (count argv)) "gravity.mir.pipeline-test") source-vars namespace)))))

(deftest checked-core-cohort-includes-the-complete-native-mir-namespace
  (let [commands (ci/commands-for-selection "checked-core-runtime" {:kind :full})
        [label timeout argv] (last commands)
        source-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/mir/pipeline_test.clj"))
        namespace "gravity.mir.pipeline-test"]
    (is (= ["checked-core-runtime-regression" "checked-core-current-binding-regression"
            "checked-core-projection-kernel-regression" "mir-native-validation"]
           (mapv first commands)))
    (is (= "mir-native-validation" label))
    (is (= 180 timeout))
    (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
    (is (seq source-vars))
    (is (= source-vars (macro-selected-vars argv source-vars namespace)))
    (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
    (is (empty? (macro-selected-vars
                 (assoc argv (dec (count argv)) "gravity.checked-core.pipeline-test") source-vars namespace)))))

(deftest macro-children-preserve-complete-disjoint-source-selection
  (let [[cohort receipt compatibility :as commands] (ci/commands-for-selection "macro" {:kind :full})
        source-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/macro/pipeline_test.clj"))
        compatibility-vars (binding [*read-eval* false]
                             (source-deftest-names "bootstrap/clojure/test/gravity/bootstrap_compatibility/c4_test.clj"))
        native-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/macro/native_reports_test.clj"))
        owner-vars (binding [*read-eval* false]
                     (source-deftest-names "bootstrap/clojure/test/gravity/macro/owner_calls_test.clj"))
        cohort-argv (nth cohort 2) receipt-argv (nth receipt 2) compatibility-argv (nth compatibility 2)
        cohort-vars (macro-selected-vars cohort-argv source-vars)
        receipt-vars (macro-selected-vars receipt-argv source-vars)
        partition? (fn [left right]
                     (and (seq left) (seq right) (empty? (set/intersection left right))
                          (= source-vars (set/union left right))))]
    (is (= ["macro-pipeline-cohort" "macro-receipt-controls" "macro-bootstrap-compatibility"]
           (mapv first commands)))
    (is (= [900 900 900] (mapv second commands)))
    (is (= [["gravity.macro.pipeline-test" "gravity.macro.native-reports-test" "gravity.macro.owner-calls-test"]
            ["gravity.macro.pipeline-test"]
            ["gravity.bootstrap-compatibility.c4-test"]]
           (mapv #(option-values (nth % 2) "--namespace") commands)))
    (is (seq native-vars))
    (is (= native-vars (macro-selected-vars cohort-argv native-vars "gravity.macro.native-reports-test")))
    (let [index (.indexOf ^java.util.List cohort-argv "gravity.macro.native-reports-test")
          omitted (vec (concat (take (dec index) cohort-argv) (drop (inc index) cohort-argv)))]
      (is (empty? (macro-selected-vars omitted native-vars "gravity.macro.native-reports-test"))))
    (is (seq owner-vars))
    (is (= owner-vars (macro-selected-vars cohort-argv owner-vars "gravity.macro.owner-calls-test")))
    (let [index (.indexOf ^java.util.List cohort-argv "gravity.macro.owner-calls-test")
          omitted (vec (concat (take (dec index) cohort-argv) (drop (inc index) cohort-argv)))]
      (is (empty? (macro-selected-vars omitted owner-vars "gravity.macro.owner-calls-test"))))
    (is (every? #(some #{"--progress"} (nth % 2)) commands))
    (is (not-any? #(some #{"--fail-fast"} (nth % 2)) commands))
    (is (= 3 (count (set ci/macro-receipt-test-vars))))
    (is (every? source-vars ci/macro-receipt-test-vars))
    (is (= (set ci/macro-receipt-test-vars) receipt-vars))
    (is (partition? cohort-vars receipt-vars))
    (is (seq compatibility-vars))
    (is (not-any? #{"--regex" "--exact" "--prefix"} compatibility-argv))
    ;; A lost selector, omitted control, or overlapping cohort is a real
    ;; coverage defect even when every remaining child would pass.
    (is (not (partition? #{} receipt-vars)))
    (is (not (partition? source-vars receipt-vars)))
    (is (not (partition? cohort-vars
                         (macro-selected-vars (vec (drop-last 2 receipt-argv)) source-vars))))
    (let [pattern (re-pattern (option-value cohort-argv "--regex"))]
      (doseq [name ci/macro-receipt-test-vars]
        (is (not (re-find pattern name)))
        (is (not (re-find pattern (str "gravity.macro.pipeline-test/" name))))))))

(deftest macro-child-failure-does-not-hide-later-evidence-or-become-success
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-macro-cohort-failure-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        calls (atom [])]
    (try
      (with-redefs [ci/output (.getPath directory)
                    ci/pull-request-selection (constantly {:kind :full})
                    ci/execute!
                    (fn [id timeout argv]
                      (swap! calls conj [id timeout argv])
                      {:id id :timeout-seconds timeout :argv argv
                       :status (if (= id "macro-pipeline-cohort") :failed :passed)
                       :exit-code (if (= id "macro-pipeline-cohort") 17 0)})]
        (is (= 1 (ci/run-mode! "macro"))))
      (let [rows (edn/read-string (slurp (io/file directory "macro-summary.edn")))]
        (is (= ["macro-pipeline-cohort" "macro-receipt-controls" "macro-bootstrap-compatibility"]
               (mapv :id rows) (mapv first @calls)))
        (is (= [:failed :passed :passed] (mapv :status rows)))
        (is (= [17 0 0] (mapv :exit-code rows))))
      (finally (delete-fixture-tree! directory)))))

(deftest macro-hosted-envelope-covers-all-child-watchdogs
  (let [workflow (slurp ".github/workflows/ci.yml")
        checks (second (re-find #"(?s)\n  checks:\n(.*?)(?=\n  resolution:)" workflow))
        [_ macro-minutes other-minutes]
        (re-find #"timeout-minutes: \$\{\{ matrix.mode == 'macro' && (\d+) \|\| (\d+) \}\}" checks)
        watchdog-seconds (reduce + (map second (ci/commands-for-selection "macro" {:kind :full})))]
    (is (some? macro-minutes))
    (is (= "20" other-minutes))
    (is (> (* 60 (Long/parseLong macro-minutes)) watchdog-seconds))
    (is (str/includes? checks "mode: [static, contracts, bootstrap, macro, checked-core-runtime]"))))

(deftest resolution-commands-are-complete-and-disjoint
  (let [modes ["resolution-pipeline" "resolution-unmetered"
               "resolution-invocation-selection"]
        commands (mapv (fn [mode]
                         (first (ci/commands-for-selection mode {:kind :full})))
                       modes)
        namespace (fn [[_ _ argv]]
                    (let [index (.indexOf ^java.util.List argv "--namespace")]
                      (nth argv (inc index))))]
    (is (= modes (mapv first commands)))
    (is (= [600 600 600] (mapv second commands)))
    (is (= ["gravity.resolution.pipeline-test"
            "gravity.self-hosting.sh06-unmetered-resolution-test"
            "gravity.self-hosting.sh06-invocation-selection-test"]
           (mapv namespace commands)))
    (is (= 3 (count (set (map #(nth % 2) commands)))))
    (is (every? #(= 1 (count (filter #{"--namespace"} (nth % 2)))) commands))))

(deftest resolution-pipeline-selector-partition-covers-the-source-namespace
  (let [pipeline-commands (ci/commands-for-selection
                           "resolution-pipeline" {:kind :full})
        [cohort final] pipeline-commands
        cohort-argv (nth cohort 2)
        final-argv (nth final 2)
        source-vars (source-deftest-names
                     "bootstrap/clojure/test/gravity/resolution/pipeline_test.clj")
        namespace "gravity.resolution.pipeline-test"
        qualified (fn [name] (str namespace "/" name))
        cohort-pattern (re-pattern (option-value cohort-argv "--regex"))
        final-name (option-value final-argv "--exact")
        cohort-vars (set (filter #(or (re-find cohort-pattern %)
                                      (re-find cohort-pattern (qualified %)))
                                 source-vars))
        final-vars (set (filter #(or (= final-name %)
                                     (= final-name (qualified %)))
                                source-vars))]
    (is (= ["resolution-pipeline" "resolution-pipeline-fresh-replay"]
           (mapv first pipeline-commands)))
    (is (= [600 600] (mapv second pipeline-commands)))
    (is (= 2 (count pipeline-commands)))
    (is (= #{ci/resolution-pipeline-final-test} final-vars))
    (is (seq cohort-vars))
    (is (empty? (set/intersection cohort-vars final-vars)))
    (is (= source-vars (set/union cohort-vars final-vars)))
    (is (not (re-find cohort-pattern ci/resolution-pipeline-final-test)))
    (is (re-find (re-pattern (option-value final-argv "--exact"))
                 ci/resolution-pipeline-final-test))))

(deftest resolution-workflow-serializes-only-its-own-pool
  (let [workflow (slurp ".github/workflows/ci.yml")
        resolution (second (re-find #"(?s)\n  resolution:\n(.*?)(?=\n  leaves:)" workflow))]
    (is (some? resolution))
    (is (str/includes? resolution
                       "mode: [resolution-pipeline, resolution-unmetered, resolution-invocation-selection]"))
    (is (str/includes? resolution "max-parallel: 1"))
    (is (str/includes? resolution "needs.select.outputs.resolution == 'false'"))
    (is (str/includes? resolution "git rev-parse HEAD 'HEAD^{tree}'"))
    (is (str/includes? resolution "git status --porcelain"))
    (is (str/includes? resolution "target/ci/SHA256SUMS"))
    (is (str/includes? workflow "needs: [checks, resolution, leaves, snapshot]"))
    (is (str/includes? workflow
                       "if: github.event_name == 'workflow_dispatch' && github.ref == 'refs/heads/main'"))
    (is (not (str/includes? workflow
                            "mode: [static, contracts, bootstrap, macro, resolution, checked-core-runtime]")))))

(deftest stage-selection-follows-owned-downstream-closure
  (let [change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})
        expected-all #{"static" "contracts" "bootstrap" "macro"
                       "resolution" "checked-core-runtime"}
        expected-macro #{"static" "contracts" "macro"
                         "resolution" "checked-core-runtime"}
        expected-resolution #{"static" "contracts" "resolution"
                              "checked-core-runtime"}
        expected-checked #{"static" "contracts" "checked-core-runtime"}]
    (is (= expected-all
           (:modes (ci/stage-selection [(change "bootstrap/clojure/src/gravity/reader.clj")]))))
    (is (= expected-macro
           (:modes (ci/stage-selection [(change "language/compiler/src/gravity/compiler/c4_macro_expansion_engine.gravity")]))))
    (is (= expected-resolution
           (:modes (ci/stage-selection [(change "bootstrap/clojure/test/gravity/resolution/pipeline_test.clj")]))))
    (is (= expected-checked
           (:modes (ci/stage-selection [(change "language/compiler/src/gravity/checked_core.gravity")]))))
    (is (= expected-checked
           (:modes (ci/stage-selection
                    [(change "language/compiler/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")]))))
    (is (= expected-checked
           (:modes (ci/stage-selection
                    [(change "language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity")]))))
    (is (= expected-checked
           (:modes (ci/stage-selection
                    [(change "bootstrap/clojure/test/gravity/self_hosting/sh07_var_reference_test.clj")]))))
    (is (= expected-all
           (:modes (ci/stage-selection
                    [(change "bootstrap/clojure/src/gravity/macro.clj")
                     (change "bootstrap/gravity/src/gravity/reader.gravity")]))))
    (let [resolution-selection
          (ci/stage-selection [(change "bootstrap/clojure/src/gravity/resolution.clj")])]
      (is (ci/skip-for-stage-selection? "macro" resolution-selection))
      (is (not (ci/skip-for-stage-selection? "resolution" resolution-selection)))
      (doseq [mode ["resolution-pipeline" "resolution-unmetered"
                    "resolution-invocation-selection"]]
        (is (not (ci/skip-for-stage-selection? mode resolution-selection)) mode))
      (let [checked-selection
            (ci/stage-selection [(change "language/compiler/src/gravity/checked_core.gravity")])]
        (doseq [mode ["resolution-pipeline" "resolution-unmetered"
                      "resolution-invocation-selection"]]
          (is (ci/skip-for-stage-selection? mode checked-selection) mode)))
      (is (not (ci/skip-for-stage-selection? "contracts" resolution-selection))))))

(deftest stage-selection-falls-back-for-cross-cutting-and-unknown-inputs
  (let [change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})]
    (doseq [path ["contracts/ci-policy.edn"
                  "specs/compiler/spec.md"
                  "deps.edn"
                  ".github/workflows/ci.yml"
                  "tools/ci/run.clj"
                  "bootstrap/clojure/src/gravity/bootstrap.clj"
                  "bootstrap/clojure/src/gravity/reader.clj/unknown.clj"
                  "bootstrap/clojure/test/gravity/self_hosting/sh07_l1_c2_surface_reader_source_coverage_test.clj"
                  "bootstrap/clojure/test/gravity/self_hosting/sh07_macro_source_coverage_test.clj"
                  "bootstrap/clojure/test/gravity/self_hosting/sh07_resolution_source_coverage_test.clj"
                  "bootstrap/clojure/test/gravity/self_hosting/sh07_c8_effect_source_coverage_test.clj"
                  "src/new-owner.gravity"
                  nil]]
      (is (= :full (:kind (ci/stage-selection [(change path)]))) (pr-str path)))))

(deftest lint-tooling-selection-keeps-only-affected-ci-surfaces
  (let [change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})
        compiler-modes ["bootstrap" "macro" "resolution"
                        "resolution-pipeline" "resolution-unmetered"
                        "resolution-invocation-selection" "checked-core-runtime"]
        selection (ci/stage-selection [(change "tools/ci/lint.clj")])
        output (set (ci/selection-output-lines selection))]
    (is (= :selected (:kind selection)))
    (is (= :lint-tooling-only (:reason selection)))
    (is (= [] (:stages selection)))
    (is (= #{"static" "contracts"} (:modes selection)))
    (doseq [mode ["static" "contracts"]]
      (is (not (ci/skip-for-stage-selection? mode selection)) mode))
    (doseq [mode compiler-modes]
      (is (ci/skip-for-stage-selection? mode selection) mode))
    (is (contains? output "static=true"))
    (is (contains? output "contracts=true"))
    (is (contains? output "darwin-unsupported=true"))
    (doseq [mode ["bootstrap" "macro" "resolution" "checked-core-runtime"
                  "leaves-portable" "leaves-darwin"]]
      (is (contains? output (str mode "=false")) mode))
    (is (contains? output "static-tools-required=true"))
    (is (contains? output "selection-valid=true"))
    (let [leaf (ci/leaf-selection-for-changes
                "portable" [] [(change "tools/ci/lint_facade_test.clj")])]
      (is (= :selected (:kind leaf)))
      (is (= :lint-tooling-only (:reason leaf)))
      (is (empty? (:entries leaf))))
    (doseq [path ["tools/ci/lint_context.clj"
                  "tools/ci/lint_facade_test.clj"
                  "tools/ci/lint_macro_test.clj"
                  "tools/ci/lint_macros.clj"
                  "tools/ci/kondo/config.edn"
                  "tools/ci/kondo/hooks/c2.clj"
                  "tools/ci/kondo/hooks/module_analysis.clj"
                  ".clj-kondo/config.edn"]]
      (is (= :lint-tooling-only
             (:reason (ci/stage-selection [(change path)])))
          path))
    (is (= :full (:kind (ci/stage-selection
                         [(change "tools/ci/lint.clj")
                          (change "language/compiler/src/gravity/checked_core.gravity")]))))
    (doseq [path ["tools/ci/run.clj" "tools/ci/run_test.clj"
                  "tools/ci/progress_test.clj" "tools/ci/snapshot.clj"
                  "tools/ci/kondo/hooks/new_hook.clj" ".github/workflows/ci.yml"
                  "deps.edn" "language/runtime/src/gravity/runtime.gravity"
                  "src/new-owner.gravity"]]
      (is (= :full (:kind (ci/stage-selection [(change path)]))) path))))

(deftest stage-selection-fails-closed-for-deletes-renames-modes-and-types
  (let [regular (fn [path] {:old-mode "100644" :new-mode "100644"
                            :status "M" :path path})
        reader "bootstrap/clojure/src/gravity/reader.clj"]
    (doseq [change [{:old-mode "100644" :new-mode "000000" :status "D" :path reader}
                    {:old-mode "100644" :new-mode "100755" :status "M" :path reader}
                    {:old-mode "100644" :new-mode "120000" :status "M" :path reader}
                    {:old-mode "100755" :new-mode "100644" :status "M" :path reader}
                    {:old-mode "120000" :new-mode "100644" :status "M" :path reader}
                    {:old-mode "100644" :new-mode "100644" :status "R" :path reader}]]
      (is (= :full (:kind (ci/stage-selection [change]))) (pr-str change)))
    (is (= :selected (:kind (ci/stage-selection
                             [{:old-mode "000000" :new-mode "100644"
                               :status "A" :path reader}]))))
    (is (= :full (:kind (ci/stage-selection [(regular reader)
                                             (regular "unknown/owner.gravity")]))))))

(deftest static-selection-runs-snapshot-controls-only-for-snapshot-changes
  (let [base (apply str (repeat 40 "a"))
        command-ids (fn [selection]
                      (mapv first (ci/commands-for-selection "static" selection)))
        selected {:kind :selected
                  :base base
                  :clojure-paths ["bootstrap/clojure/src/gravity/reader.clj"]}
        snapshot-selected {:kind :full
                           :base base
                           :clojure-paths ["tools/ci/snapshot.clj"]}
        snapshot-test-selected {:kind :full
                                :base base
                                :clojure-paths ["tools/ci/snapshot_test.clj"]}]
    (is (not (some #{"snapshot-controls"} (command-ids selected))))
    (is (= 1 (count (filter #{"snapshot-controls"} (command-ids snapshot-selected)))))
    (is (= 1 (count (filter #{"snapshot-controls"} (command-ids snapshot-test-selected)))))))

(deftest static-selection-runs-only-dependent-control-suites
  (let [base (apply str (repeat 40 "a"))
        command-ids (fn [selection]
                      (mapv first (ci/commands-for-selection "static"
                                                             (assoc selection :base base))))
        checked-core {:kind :selected
                      :reason :affected-stage
                      :paths ["language/compiler/src/gravity/checked_core.gravity"]}
        macro-source {:kind :selected
                      :reason :lint-tooling-only
                      :paths ["tools/ci/lint_macro_test.clj"]}
        context-source {:kind :selected
                        :reason :lint-tooling-only
                        :paths ["tools/ci/lint_context.clj"]}
        facade-source {:kind :selected
                       :reason :lint-tooling-only
                       :paths ["tools/ci/lint_facade_test.clj"]}
        root-kondo-config {:kind :selected
                           :reason :lint-tooling-only
                           :paths [".clj-kondo/config.edn"]}
        full {:kind :full :paths []}
        failed {:kind :full :reason :path-discovery-failed :paths []}
        malformed {:kind :selected :paths [nil]}
        unknown {:kind :selected :paths ["src/new-owner.gravity"]}]
    ;; A compiler source change has no dependency on the static control
    ;; fixtures.  Whitespace remains unconditional, while changed-file lint
    ;; remains available for Clojure/EDN inputs below.
    (is (= ["whitespace"] (command-ids checked-core)))
    (is (= ["whitespace" "clojure-lint" "clojure-format"
            "macro-context-controls" "changed-clojure-lint"]
           (command-ids macro-source)))
    (is (= ["whitespace" "clojure-lint" "clojure-format"
            "harness-controls" "macro-context-controls"
            "facade-context-controls" "changed-clojure-lint"]
           (command-ids context-source)))
    (is (= ["whitespace" "clojure-lint" "clojure-format"
            "facade-context-controls" "changed-clojure-lint"]
           (command-ids facade-source)))
    ;; tools/ci/kondo/config.edn imports the root Kondo configuration, so a
    ;; root config change affects both pinned hook/context observations.
    (is (= ["whitespace" "clojure-lint" "harness-controls"
            "macro-context-controls" "facade-context-controls"
            "changed-clojure-lint"]
           (command-ids root-kondo-config)))
    ;; Non-PR/broad selections and malformed or unknown selected data retain
    ;; the complete control suite instead of guessing a narrower dependency.
    (doseq [selection [full failed malformed unknown]]
      (is (= ["whitespace" "workflow-lint" "clojure-lint" "clojure-format"
              "harness-controls" "project-scope-runner-controls"
              "progress-controls" "macro-context-controls"
              "facade-context-controls"]
             (command-ids selection))
          (pr-str selection)))))

(deftest path-discovery-failure-cannot-green-static
  (let [base (apply str (repeat 40 "a"))
        selection {:kind :full
                   :reason :path-discovery-failed
                   :base base
                   :paths []
                   :clojure-paths []
                   :selection-error "synthetic malformed raw diff"}
        executed (atom [])
        summary (io/file "target/ci/static-summary.edn")
        bootstrap-summary (io/file "target/ci/bootstrap-summary.edn")]
    (try
      (with-redefs [ci/pull-request-selection (fn [] selection)
                    ci/execute! (fn [id _seconds _argv]
                                  (swap! executed conj id)
                                  {:id id :status :passed})]
        (is (= 1 (ci/run! "static")))
        (is (some #{"clojure-lint"} @executed))
        (is (not (some #{"changed-clojure-lint"} @executed)))
        (is (= 0 (ci/run! "bootstrap")))
        (is (some #{"cold-bootstrap-load"} @executed))
        (is (= ["static-selection-discovery" :failed]
               (let [results (edn/read-string (slurp summary))
                     failure (last results)]
                 [(:id failure) (:status failure)]))))
      (finally
        (doseq [file [summary bootstrap-summary]]
          (io/delete-file file true))))))

(deftest raw-git-selection-rejects-structural-document-lookalikes
  (is (seq (assert-git-selection! [["README.md" "# base\n"]]
                                  (fn [directory]
                                    (write-fixture-file! directory "README.md" "# changed\n"))
                                  true)))
  (is (seq (assert-git-selection! [["README.md" "# base\n"]
                                   ["src/compiler.gravity" "(seed)\n"]]
                                  (fn [directory]
                                    (git-fixture! directory ["rm" "-q" "src/compiler.gravity"])
                                    (write-fixture-file! directory "README.md" "# changed\n"))
                                  false)))
  (is (seq (assert-git-selection! [["src/compiler.gravity" "(seed)\n"]]
                                  (fn [directory]
                                    (git-fixture! directory ["rm" "-q" "src/compiler.gravity"])
                                    (write-fixture-file! directory "notes.md" "# renamed\n"))
                                  false)))
  (is (seq (assert-git-selection! [["README.md" "# base\n"]]
                                  (fn [directory]
                                    (fixture-tool! directory ["chmod" "755" "README.md"] "" #{0}))
                                  false)))
  (is (seq (assert-git-selection! [["README.md" "# base\n"]]
                                  (fn [directory]
                                    (git-fixture! directory ["rm" "-q" "README.md"])
                                    (fixture-tool! directory ["ln" "-s" "docs.md" "README.md"] "" #{0}))
                                  false)))
  (is (seq (assert-git-selection! [["baseline.txt" "base\n"]]
                                  (fn [directory]
                                    (write-fixture-file! directory "script.md" "# executable\n")
                                    (fixture-tool! directory ["chmod" "755" "script.md"] "" #{0}))
                                  false))))

(deftest pr-comparison-uses-the-tested-merge-parent
  (let [stale-base (apply str (repeat 40 "a"))
        current-main (apply str (repeat 40 "b"))
        pr-head (apply str (repeat 40 "c"))]
    (is (= current-main (ci/comparison-base "pull_request" stale-base pr-head
                                            [current-main pr-head])))
    (is (= stale-base (ci/comparison-base "push" stale-base nil
                                          [current-main pr-head])))
    (doseq [parents [[] [pr-head] [current-main stale-base]
                     [current-main pr-head stale-base]]]
      (is (thrown? Exception (ci/comparison-base "pull_request" stale-base pr-head parents))))
    (is (thrown? Exception (ci/comparison-base "pull_request" stale-base nil
                                               [current-main pr-head])))))

(deftest shallow-pr-merge-retains-parents-diff-trees-and-base-blobs
  (let [root (.toFile (java.nio.file.Files/createTempDirectory
                       "uniform-ci-shallow-pr-"
                       (make-array java.nio.file.attribute.FileAttribute 0)))
        remote (io/file root "remote")
        work (io/file root "work")
        clone (io/file root "clone")]
    (.mkdirs remote)
    (.mkdirs work)
    (.mkdirs clone)
    (try
      (git-fixture! remote ["init" "--bare" "-q"])
      (initialize-git-fixture! work)
      (git-fixture! work ["remote" "add" "origin" (.getPath remote)])
      (write-fixture-file! work "seed.txt" "seed\n")
      (let [seed (commit-fixture! work "seed")]
        (write-fixture-file! work "base.txt" "base\n")
        (let [base (commit-fixture! work "base")]
          (git-fixture! work ["branch" "-M" "main"])
          (git-fixture! work ["push" "-q" "origin" "main"])
          (git-fixture! work ["switch" "-c" "feature"])
          (write-fixture-file! work "feature.txt" "feature\n")
          (let [feature (commit-fixture! work "feature")]
            (git-fixture! work ["push" "-q" "origin" "feature"])
            (git-fixture! work ["switch" "main"])
            (git-fixture! work ["merge" "--no-ff" "--no-edit" "feature"])
            (let [merge (str/trim (git-fixture! work ["rev-parse" "HEAD"]))]
              (is (not= merge feature))
              (git-fixture! work ["push" "-q" "origin" "main"])
              (git-fixture! clone ["init" "-q"])
              (git-fixture! clone ["remote" "add" "origin" (.getPath remote)])
              (git-fixture! clone ["fetch" "-q" "--no-tags" "--depth=2" "origin"
                                   "refs/heads/main:refs/remotes/origin/main"])
              (git-fixture! clone ["checkout" "-q" "--detach" "refs/remotes/origin/main"])
              (is (= merge (str/trim (git-fixture! clone ["rev-parse" "HEAD"]))))
              (is (= "true\n" (git-fixture! clone ["rev-parse" "--is-shallow-repository"])))
              (is (= [base feature]
                     (vec (str/split (str/trim (git-fixture! clone
                                                             ["show" "-s" "--format=%P" "HEAD"]))
                                     #"\s+"))))
              (let [changes (ci/parse-raw-diff
                             (git-fixture! clone ["diff" "--raw" "--no-renames" "-z"
                                                  base "HEAD"]))]
                (is (= #{"feature.txt"} (set (map :path changes)))))
              (is (= "base\n" (git-fixture! clone ["show" (str base ":base.txt")])))
              (is (= "feature\n" (git-fixture! clone ["show" (str feature ":feature.txt")])))
              ;; A pinned commit older than the direct merge parents is not
              ;; available in this shallow checkout; jobs that need such pins
              ;; must retain a full-history checkout.
              (is (thrown? Exception
                           (git-fixture! clone ["show" (str seed ":seed.txt")])))))))
      (finally
        (delete-fixture-tree! root)))))

(deftest platform-leaves-partition-the-entire-strict-catalog
  (binding [*command-line-args* nil]
    (load-file "bootstrap/clojure/test/gravity/bootstrap_free_leaf_test_runner.clj"))
  (let [catalog (var-get (requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/catalog))
        portable (ci/leaf-selection "portable" catalog)
        darwin (ci/leaf-selection "darwin" catalog)]
    (is (= 52 (count portable)))
    (is (= ["darwin-publication"] (mapv :id darwin)))
    (is (= (set catalog) (set (concat portable darwin))))
    (is (= (count catalog) (count (concat portable darwin))))
    (is (thrown? Exception (ci/leaf-selection "unknown" catalog)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest affected-leaves-follow-reviewed-source-closures
  (let [catalog (ci/load-leaf-catalog)
        entry (fn [id] (some #(when (= id (:id %)) %) catalog))
        change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})
        select (fn [platform paths]
                 (ci/leaf-selection-for-changes platform catalog (mapv change paths)))
        scalar (entry "c2-reader-scalar-spans")
        darwin (entry "darwin-publication")
        scalar-result (select "portable" [(:test-path scalar)])
        source-result (select "portable" [(:source-path scalar)])
        dependent-result
        (select "portable"
                ["bootstrap/clojure/src/gravity/c2_reader_product_projection/policy.clj"])
        docs-result (select "portable" ["README.md"])
        darwin-result (select "darwin" [(:test-path darwin)])
        portable-darwin-result (select "portable" [(:test-path darwin)])
        unknown-result (select "portable" ["bootstrap/clojure/src/gravity/not-in-catalog.clj"])
        mode-result (ci/leaf-selection-for-changes
                     "portable" catalog
                     [{:old-mode "100644" :new-mode "100755" :status "M"
                       :path (:test-path scalar)}])
        empty-result (ci/leaf-selection-for-changes "portable" catalog [])
        delete-result (ci/leaf-selection-for-changes
                       "portable" catalog
                       [{:old-mode "100644" :new-mode "000000" :status "D"
                         :path (:test-path scalar)}])
        markdown-delete-result
        (ci/leaf-selection-for-changes
         "portable" catalog
         [{:old-mode "100644" :new-mode "000000" :status "D" :path "README.md"}])
        markdown-rename-result
        (ci/leaf-selection-for-changes
         "portable" catalog
         [{:old-mode "100644" :new-mode "100644" :status "R" :path "README.md"}])
        markdown-mode-result
        (ci/leaf-selection-for-changes
         "portable" catalog
         [{:old-mode "100644" :new-mode "100755" :status "M" :path "README.md"}])
        cross-cutting-result (select "portable" ["tools/ci/run.clj"])
        ids (fn [result] (set (map :id (:entries result))))]
    (is (= :selected (:kind scalar-result)))
    (is (= #{"c2-reader-scalar-spans"} (ids scalar-result)))
    (is (= :selected (:kind source-result)))
    (is (contains? (ids source-result) "c2-reader-scalar-spans"))
    (is (= :selected (:kind dependent-result)))
    (is (contains? (ids dependent-result) "c2-reader-product-projection"))
    (is (contains? (ids dependent-result) "c2-reader-scalar-spans"))
    (is (not (contains? (ids scalar-result) "c10-safety-analysis")))
    (is (= :docs-only (:kind docs-result)))
    (is (empty? (:entries docs-result)))
    (is (= ["darwin-publication"] (mapv :id (:entries darwin-result))))
    (is (= :selected (:kind portable-darwin-result)))
    (is (empty? (:entries portable-darwin-result)))
    (is (= :full (:kind unknown-result)))
    (is (= 52 (count (:entries unknown-result))))
    (is (= :full (:kind mode-result)))
    (is (= 52 (count (:entries mode-result))))
    (doseq [result [empty-result delete-result markdown-delete-result
                    markdown-rename-result markdown-mode-result cross-cutting-result]]
      (is (= :full (:kind result)))
      (is (= 52 (count (:entries result)))))))

(deftest leaf-closure-fails-closed-for-unsupported-dependencies
  (is (= ['gravity.foo]
         (vec (ci/leaf-required-namespaces
               (read-string "(ns demo (:require [gravity.foo :as foo]))")))))
  (doseq [source ["(ns demo (:use [gravity.foo]))"
                  "(ns demo (:require (gravity.foo [bar :as bar])))"
                  "(ns demo (:require [some.dynamic (resolve x)]))"
                  "(ns demo (:require [gravity.foo (if true :as :refer)]))"
                  "(ns demo (:require [gravity.foo :as]))"
                  "(ns demo (:require [gravity.foo :refer :bad]))"]]
    (is (thrown? Exception
                 (doall (ci/leaf-required-namespaces (read-string source))))
        source))
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-leaf-closure-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        path (.getPath (io/file directory "synthetic.clj"))]
    (try
      (doseq [dependency-form [":ci.run/eof\n(require 'gravity.hidden)"
                               ":ci.run/eof\n(load \"gravity/hidden\")"]]
        (spit path (str "(ns demo)\n" dependency-form "\n"))
        (with-redefs [ci/leaf-namespace-path (constantly path)]
          (is (thrown? Exception
                       (ci/leaf-require-closure {:namespace 'demo
                                                 :source-path path
                                                 :test-path path})))))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (io/delete-file file true)))))
  (let [catalog (ci/load-leaf-catalog)
        change {:old-mode "100644" :new-mode "100644"
                :status "M" :path "bootstrap/clojure/test/gravity/c2_reader_scalar_spans_test.clj"}]
    (with-redefs [ci/leaf-require-closure
                  (fn [_] (throw (ex-info "synthetic closure failure" {})))]
      (let [result (ci/leaf-selection-for-changes "portable" catalog [change])]
        (is (= :full (:kind result)))
        (is (= :leaf-closure-failed (:reason result)))
        (is (= 52 (count (:entries result))))))))

(deftest leaf-closure-fails-closed-for-use-and-clojure-core-loader-bindings
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-loader-closure-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        provider (str (io/file directory "provider.clj"))
        hidden (str (io/file directory "hidden.clj"))
        catalog [{:id "provider" :namespace 'gravity.provider
                  :source-path provider :test-path provider}
                 {:id "hidden" :namespace 'gravity.hidden
                  :source-path hidden :test-path hidden}]
        change {:old-mode "100644" :new-mode "100644"
                :status "M" :path provider}
        hidden-sources ["(ns gravity.hidden)\n(use 'gravity.digest)\n"
                        "(ns gravity.hidden)\n(clojure.core/use 'gravity.digest)\n"
                        "(ns gravity.hidden (:require [clojure.core :as core]))\n(core/require 'gravity.digest)\n"
                        "(ns gravity.hidden (:require [clojure.core :rename {require core/require}]))\n(core/require 'gravity.digest)\n"
                        "(ns gravity.hidden (:refer-clojure :rename {require hidden-require}))\n(hidden-require 'gravity.digest)\n"]]
    (try
      (spit provider "(ns gravity.provider)\n")
      (with-redefs [ci/leaf-namespace-path
                    (fn [namespace-symbol]
                      (case namespace-symbol
                        gravity.provider provider
                        gravity.hidden hidden
                        (throw (ex-info "Unexpected fixture namespace"
                                        {:namespace namespace-symbol}))))]
        (doseq [source hidden-sources]
          (spit hidden source)
          (let [result (ci/leaf-selection-for-changes "portable" catalog [change])]
            (is (= :full (:kind result)) (pr-str source))
            (is (= :leaf-closure-failed (:reason result)) (pr-str source))
            (is (= 2 (count (:entries result))) (pr-str source)))))
      (finally
        (doseq [file (reverse (file-seq directory))]
          (io/delete-file file true))))))

(deftest leaf-selection-reuses-parsed-forms-within-one-invocation
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-leaf-cache-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        path (fn [name] (.getPath (io/file directory name)))
        a-source (path "a.clj")
        b-source (path "b.clj")
        shared-source (path "shared.clj")
        a-test (path "a_test.clj")
        b-test (path "b_test.clj")
        namespace-paths {'gravity.a a-source
                         'gravity.b b-source
                         'gravity.shared shared-source}
        catalog [{:id "leaf-a" :namespace 'gravity.a
                  :source-path a-source :test-path a-test}
                 {:id "leaf-b" :namespace 'gravity.b
                  :source-path b-source :test-path b-test}]
        change {:old-mode "100644" :new-mode "100644"
                :status "M" :path a-test}
        read-count (atom 0)
        original-source-forms ci/leaf-source-forms]
    (try
      (spit a-source "(ns gravity.a (:require [gravity.shared :as shared]))\n")
      (spit b-source "(ns gravity.b (:require [gravity.shared :as shared]))\n")
      (spit shared-source "(ns gravity.shared)\n")
      (with-redefs [ci/leaf-namespace-path
                    (fn [namespace-symbol]
                      (or (get namespace-paths namespace-symbol)
                          (throw (ex-info "Unexpected fixture namespace"
                                          {:namespace namespace-symbol}))))
                    ci/leaf-source-forms
                    (fn [source-path]
                      (swap! read-count inc)
                      (original-source-forms source-path))]
        (let [first-result (ci/leaf-selection-for-changes "portable" catalog [change])]
          (is (= :selected (:kind first-result)))
          (is (= #{"leaf-a"} (set (map :id (:entries first-result)))))
          (is (= 3 @read-count))
          ;; A second invocation gets a fresh cache and must observe the
          ;; changed dependency instead of reusing the previous parse.
          (spit shared-source "(ns gravity.shared (:use [gravity.bad]))\n")
          (let [second-result (ci/leaf-selection-for-changes
                               "portable" catalog [change])]
            (is (= :full (:kind second-result)))
            (is (= :leaf-closure-failed (:reason second-result)))
            (is (= 5 @read-count))
            (is (= 2 (count (:entries second-result)))))))
      (finally
        (delete-fixture-tree! directory)))))

(deftest success-cleans-owned-descendants
  (let [child (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)")
        expression (str "(let [p (.start (ProcessBuilder. " (pr-str child) "))] "
                        "(spit \"target/ci/orphan.pid\" (.pid p)) (Thread/sleep 2000))")
        result (ci/execute! "positive-orphan-cleanup" 20
                            (ci/command "128m" "-M" "-e" expression))
        child-pid (read-string (slurp "target/ci/orphan.pid"))]
    (is (= :passed (:status result)))
    (is (some #{child-pid} (:descendant-pids result)))
    (is (not (alive? child-pid)))
    (is (not (alive? (:pid result))))))

(deftest receipts-preserve-failure-and-launch-errors
  (is (= :failed (:status (ci/execute! "negative-exit" 15
                                       (ci/command "128m" "-M" "-e" "(System/exit 7)")))))
  (is (= 7 (:exit-code (read-string (slurp "target/ci/negative-exit.edn")))))
  (is (= :launch-error (:status (ci/execute! "negative-launch" 1
                                             ["/nonexistent/ci-command"])))))

(deftest execute-output-keeps-success-quiet-and-failure-bounded
  (let [success-output
        (with-out-str
          (let [result (ci/execute!
                        "output-success-bounded" 15
                        (ci/command "128m" "-M" "-e"
                                    "(println \"success-payload\")"))]
            (is (= :passed (:status result)))))
        failure-output
        (with-out-str
          (let [result (ci/execute!
                        "output-failure-bounded" 15
                        (ci/command "128m" "-M" "-e"
                                    "(do (println (apply str (repeat 10000 \\x))) (System/exit 7))"))]
            (is (= :failed (:status result)))))
        success-log (slurp "target/ci/output-success-bounded.log")
        failure-log (slurp "target/ci/output-failure-bounded.log")]
    (is (str/includes? success-output "CI command output-success-bounded finished :passed exit 0"))
    (is (not (str/includes? success-output "success-payload")))
    (is (< (count success-output) 1000))
    (is (str/includes? failure-output "CI command output-failure-bounded finished :failed exit 7"))
    (is (str/includes? failure-output "CI log preview:"))
    (is (str/includes? failure-output "[log preview truncated"))
    (is (< (count failure-output) 6000))
    (is (str/includes? success-log "success-payload"))
    (is (str/includes? failure-log "xxxxxxxx"))))

(deftest log-preview-counts-multibyte-characters
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-output-preview-"
                            (make-array java.nio.file.attribute.FileAttribute 0)))
        exact-file (io/file directory "exact.log")
        extra-file (io/file directory "extra.log")
        exact-content (apply str (repeat 4096 "\u00e9"))
        extra-content (str exact-content "\u00e9")
        preview-fn (ns-resolve 'ci.run 'log-preview)
        marker "[log preview truncated"]
    (try
      (spit exact-file exact-content :encoding "UTF-8")
      (spit extra-file extra-content :encoding "UTF-8")
      (let [exact-preview (preview-fn exact-file)
            extra-preview (preview-fn extra-file)]
        (is (= 8192 (.length exact-file)))
        (is (= exact-content exact-preview))
        (is (str/starts-with? extra-preview exact-content))
        (is (str/includes? extra-preview marker)))
      (finally
        (delete-fixture-tree! directory)))))

(deftest log-preview-reads-short-chunks-without-unbounded-read
  (let [preview-fn (ns-resolve 'ci.run 'read-log-preview)
        source (apply str (repeat 10000 "x"))
        source-length (count source)
        offset (atom 0)
        reader (proxy [java.io.Reader] []
                 (read [buffer start _length]
                   (let [position @offset]
                     (if (= position source-length)
                       -1
                       (do
                         (aset-char buffer start (.charAt source position))
                         (swap! offset inc)
                         1))))
                 (close [] nil))
        preview (preview-fn reader "short-read.log")]
    (is (= 4097 @offset))
    (is (str/starts-with? preview (apply str (repeat 4096 "x"))))
    (is (str/includes? preview
                       "[log preview truncated; full log: short-read.log]"))))

(deftest timeout-is-a-failure
  (let [result (ci/execute! "negative-timeout" 1
                            (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)"))]
    (is (= :timeout (:status result)))
    (is (not (alive? (:pid result)))))
  (is (= 124 (:exit-code (read-string (slurp "target/ci/negative-timeout.edn"))))))

(deftest fatal-wait-failure-cleans-the-owned-process-before-rethrowing
  (let [process (.start (doto (ProcessBuilder. ^java.util.List
                               ["/bin/sh" "-c" "sleep 30"])))
        fatal (OutOfMemoryError. "injected fatal wait failure")
        wait-var (ns-resolve 'gravity.process-supervision 'wait-for-process!)]
    (try
      (let [thrown (with-redefs-fn
                     {wait-var (fn [& _] (throw fatal))}
                     #(try
                        (ci/await! process 1)
                        nil
                        (catch Throwable error error)))]
        (is (identical? fatal thrown))
        (is (false? (.isAlive process))))
      (finally
        (when (.isAlive process)
          (try (ci/stop! process []) (catch Throwable _ nil)))
        (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS)))))

(deftest wait-error-is-failed-rather-than-timeout
  (let [process (.start (doto (ProcessBuilder. ^java.util.List
                               ["/bin/sh" "-c" "sleep 30"])))
        wait-var (ns-resolve 'gravity.process-supervision 'wait-for-process!)]
    (try
      (let [result (with-redefs-fn
                     {wait-var (fn [& _]
                                 {:completed? false
                                  :interrupted? true
                                  :error "injected wait interruption"})}
                     #(ci/await! process 1))]
        (is (= :failed (:status result)) result)
        (is (= 1 (:exit-code result)) result))
      (finally
        (when (.isAlive process) (.destroyForcibly process))
        (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS)))))

(deftest incomplete-tracked-cleanup-cannot-be-reported-as-a-pass
  (let [process (.start (ProcessBuilder. ^java.util.List
                         ["/bin/sh" "-c" "exit 0"]))
        finalize-var (ns-resolve 'gravity.process-supervision
                                 'finalize-owned-process!)]
    (try
      (let [result (with-redefs-fn
                     {finalize-var
                      (fn [_ _]
                        {:tracked-complete? false
                         :root-alive? true
                         :descendants-alive 1
                         :survivor-pids [7002]
                         :descendant-pids []})}
                     #(ci/await! process 1))]
        (is (= :failed (:status result)) result)
        (is (= 1 (:exit-code result)) result))
      (finally
        (when (.isAlive process) (.destroyForcibly process))
        (.waitFor process 5 java.util.concurrent.TimeUnit/SECONDS)))))

(deftest resource-context-preserves-real-bindings-and-diagnostic-identity
  (let [directory (.toFile (java.nio.file.Files/createTempDirectory
                            "uniform-ci-context-" (make-array java.nio.file.attribute.FileAttribute 0)))
        holder "bootstrap/clojure/src/gravity/bootstrap_impl/holder.clj"
        foreign "bootstrap/clojure/src/gravity/bootstrap_impl/foreign.clj"
        selected "bootstrap/clojure/src/gravity/bootstrap_impl/selected.clj"
        header "(ns gravity.bootstrap (:require [clojure.string :as str]) (:import [java.io File]))\n"
        body "(let [token (nth captured 0)]\n(defn selected [] [token (str/join [] [\"ok\"]) (File. \".\")]))\n"
        observe (fn [holder-text selected-text]
                  (let [view (context/assemble!
                              directory
                              {:paths [context/bootstrap holder foreign selected]
                               :inputs (into {} (map (fn [[path text]] [path (.getBytes ^String text "UTF-8")])
                                                     {context/bootstrap header holder holder-text
                                                      foreign "(ns separate.context)\n(def unrelated 1)\n"
                                                      selected selected-text}))})
                        raw (edn/read-string
                             (fixture-tool! directory
                                            ["clj-kondo" "--repro" "--cache" "false" "--config"
                                             "{:output {:format :edn}}" "--lint" "-"] (slurp (:file view)) #{0 2 3}))]
                    {:view view :findings (mapv #(context/remap! view %) (:findings raw))}))]
    (try
      (let [positive (observe "(def captured [1])\n" body)]
        (is (empty? (:findings positive)) (pr-str positive))
        (is (= 4 (count (get-in positive [:view :inputs]))))
        (let [crlf (observe "(def captured [1])\r\n" (str/replace body "\n" "\r\n"))
              bad-alias (observe "(def captured [1])\r\n"
                                 (-> body (str/replace "str/join" "missing-alias/join")
                                     (str/replace "\n" "\r\n")))
              finding (first (filter #(= :unresolved-namespace (:type %)) (:findings bad-alias)))]
          (is (empty? (:findings crlf)) (pr-str crlf))
          (is (= [selected 2 2] ((juxt :filename :row :end-row) finding))))
        (is (thrown? clojure.lang.ExceptionInfo
                     (observe "(def captured [1])\r" (str/replace body "\n" "\r"))))
        (doseq [[holder-text source expected]
                [["(def wrong-name [1])\n" body :unresolved-symbol]
                 ["(def captured [1])\n" (str/replace body "str/join" "missing-alias/join") :unresolved-namespace]
                 ["(def captured [1])\n" (str/replace body "token (nth" "unused 1 token (nth") :unused-binding]
                 ["(def captured [1])\n" (str/replace body "(File. \".\")" "(missing-function)") :unresolved-symbol]]]
          (let [findings (:findings (observe holder-text source))
                finding (first (filter #(= expected (:type %)) findings))]
            (is (= selected (:filename finding)) (pr-str findings))
            (is (<= 1 (:row finding) (:end-row finding) 2))))
        (let [view (:view positive)
              gap (dec (:start (last (:spans view))))]
          (is (thrown? clojure.lang.ExceptionInfo
                       (context/remap! view {:row gap :end-row gap :col 1 :end-col 2})))
          (is (thrown? clojure.lang.ExceptionInfo
                       (context/remap! view {:row 1 :end-row 1000 :col 1 :end-col 2})))))
      (is (thrown? clojure.lang.ExceptionInfo (context/resource-path! "gravity/bootstrap_impl/../outside.clj")))
      (is (thrown? clojure.lang.ExceptionInfo (context/plan! (constantly (.getBytes "changed loader" "UTF-8")))))
      (finally
        (doseq [file (reverse (file-seq directory))] (.delete file))))))

(deftest context-recipe-and-resource-boundaries-fail-closed
  (let [bytes #(.getBytes ^String % "UTF-8")
        entry "(ns gravity.bootstrap)\n(clojure.core/require 'gravity.bootstrap-impl.loader)\n(gravity.bootstrap-impl.loader/load-components! \"gravity/bootstrap_impl/load_order.edn\")\n"
        inputs {context/bootstrap (bytes entry)
                context/loader (context/local-bytes! context/loader)
                context/manifest (bytes "[{:resource \"gravity/bootstrap_impl/a.clj\"} {:resource \"gravity/bootstrap_impl/b.clj\"}]")
                (str context/source-root "gravity/bootstrap_impl/a.clj") (bytes "(def captured [1])\n")
                (str context/source-root "gravity/bootstrap_impl/b.clj") (bytes "(def value (first captured))\n")}
        read-bytes (fn [inputs] (fn [path] (or (get inputs path) (throw (ex-info "Missing fixture" {:path path})))))]
    (is (= 3 (count (:paths (context/plan! (read-bytes inputs))))))
    (is (true? (context/whole-context-path? context/semantic-late-manifest)))
    (is (= (:paths (context/plan! (read-bytes inputs)))
           (:paths (context/selected-plan! (read-bytes inputs) [context/semantic-late-manifest]))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (context/selected-plan! (read-bytes inputs)
                                         [(str context/source-root "gravity/bootstrap_impl/unregistered.edn")])))
    (doseq [changed [(str/replace entry "(gravity.bootstrap-impl.loader/load-components! \"gravity/bootstrap_impl/load_order.edn\")" "")
                     (str/replace entry "load_order.edn" "different.edn")
                     (str "(gravity.bootstrap-impl.loader/load-components! \"gravity/bootstrap_impl/load_order.edn\")\n" entry)]]
      (is (thrown? clojure.lang.ExceptionInfo
                   (context/plan! (read-bytes (assoc inputs context/bootstrap (bytes changed)))))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (context/plan! (read-bytes (update inputs context/manifest
                                                    #(bytes (str (String. ^bytes % "UTF-8") " []")))))))
    ;; The concatenation is balanced, but each Compiler/load input is invalid.
    (is (= 1 (count (context/source-forms! "combined" (bytes "(do (def captured [1])\n(def value (first captured)))\n")))))
    (is (thrown? Exception
                 (context/plan! (read-bytes (assoc inputs
                                                   (str context/source-root "gravity/bootstrap_impl/a.clj") (bytes "(do (def captured [1])\n")
                                                   (str context/source-root "gravity/bootstrap_impl/b.clj") (bytes "(def value (first captured)))\n"))))))
    (is (thrown? Exception (context/source-forms! "reader-eval" (bytes "#=(+ 1 2)"))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest source-reader-resolves-ns-keywords-without-evaluation
  (let [source-ns 'gravity.ci-lint-reader-regression
        bytes #(.getBytes ^String % "UTF-8")
        source (str "(ns " source-ns " (:require [clojure.test :as test]))\n"
                    "(def test-fixture-key ::test/once-fixtures)\n"
                    "(def local-key ::local)\n")
        forms (context/source-forms! "reader-aliases" (bytes source))]
    (is (= :clojure.test/once-fixtures
           (nth (second forms) 2)))
    (is (= :gravity.ci-lint-reader-regression/local
           (nth (nth forms 2) 2)))
    (is (thrown? Exception
                 (context/source-forms!
                  "unknown-reader-alias"
                  (bytes (str "(ns " source-ns ")\n"
                              "(def missing ::unknown/key)\n")))))
    (is (nil? (find-ns source-ns)))))

(deftest bootstrap-selection-runs-historical-input-controls-only-at-boundary
  (let [command-ids (fn [selection]
                      (mapv first (ci/commands-for-selection "bootstrap" selection)))
        has-input-controls? (fn [selection]
                              (some #{"bootstrap-input-controls"}
                                    (command-ids selection)))
        ordinary-reader {:kind :selected
                         :paths ["bootstrap/clojure/src/gravity/reader.clj"]}
        boundary-paths ["contracts/bootstrap-inputs.edn"
                        "deps.edn"
                        ".github/workflows/ci.yml"
                        "tools/ci/run.clj"
                        "tools/ci/run_test.clj"
                        "bootstrap/clojure/src/gravity/bootstrap/rebuild/inputs.clj"
                        "bootstrap/clojure/test/gravity/bootstrap/rebuild/inputs_test.clj"
                        "bootstrap/clojure/fixtures/accepted/profile-accepted-meta.gravity"
                        "bootstrap/clojure/fixtures/rejected/core-unknown-form.gravity"
                        "bootstrap/clojure/src/gravity/digest.clj"
                        "bootstrap/clojure/src/gravity/process_supervision.clj"
                        "bootstrap/clojure/test/gravity/development_test_runner.clj"]
        selected-boundary (fn [path] {:kind :selected :paths [path]})
        full {:kind :full :reason :path-discovery-failed :paths []}
        unknown {:kind :selected :paths ["src/new-owner.clj"]}
        docs-only {:kind :docs-only :paths ["README.md"]}]
    (let [ordinary-commands (command-ids ordinary-reader)]
      (is (some #{"reader-regression"} ordinary-commands))
      (is (some #{"syntax-regression"} ordinary-commands))
      (is (not (has-input-controls? ordinary-reader))))
    (doseq [path boundary-paths]
      (is (ci/bootstrap-input-control-path? path) path)
      (is (has-input-controls? (selected-boundary path)) path))
    (is (has-input-controls? full))
    (is (has-input-controls? unknown))
    (is (has-input-controls? nil))
    (is (not (has-input-controls? docs-only)))))

(let [result (run-tests 'ci.run-test 'ci.lint-context-test)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
