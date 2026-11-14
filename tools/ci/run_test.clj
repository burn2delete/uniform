(ns ci.run-test
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is run-tests]]
            [ci.public-diagnostic-report :as diagnostic-report]
            [ci.public-release-subset :as public]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])
(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint])
(require '[ci.lint-context :as context])
(binding [*command-line-args* nil] (load-file "tools/ci/lint_context_test.clj"))

(def ^:private exact-selector "--exact")

(defn- fully-qualified-test-name?
  [value]
  (and (string? value)
       (= 2 (count (str/split value #"/" -1)))
       (let [[namespace-name var-name] (str/split value #"/" -1)]
         (and (re-matches #"[A-Za-z_][A-Za-z0-9_.-]*" namespace-name)
              (re-matches #"[A-Za-z_*!?+<>=.$%-][A-Za-z0-9_*!?+<>=.$%-]*" var-name)))))

(defn- exact-selection-error
  [message data]
  (throw (ex-info message (assoc data :selector exact-selector))))

(defn- parse-exact-selection
  "Validate the complete command line before resolving or running a test."
  [args]
  (let [args (vec args)]
    (when-not (= exact-selector (first args))
      (exact-selection-error "Exact CI selection must start with --exact" {:args args}))
    (let [names (subvec args 1)]
      (when (empty? names)
        (exact-selection-error "--exact needs one or more fully qualified test Vars"
                               {:args args}))
      (when-not (every? fully-qualified-test-name? names)
        (exact-selection-error "--exact accepts only fully qualified test Vars"
                               {:args args :invalid (remove fully-qualified-test-name? names)}))
      (when-not (= (count names) (count (distinct names)))
        (exact-selection-error "--exact does not accept duplicate test Vars" {:args args}))
      names)))

(defn- resolve-loaded-test-vars
  "Resolve only Vars in namespaces already loaded by this script."
  [names]
  (mapv (fn [qualified-name]
          (let [[namespace-name var-name] (str/split qualified-name #"/" -1)
                namespace-symbol (symbol namespace-name)
                var-symbol (symbol var-name)
                namespace-object (find-ns namespace-symbol)
                value (when namespace-object (get (ns-interns namespace-object) var-symbol))]
            (when-not namespace-object
              (exact-selection-error "--exact names an unloaded namespace"
                                     {:name qualified-name :namespace namespace-name}))
            (when-not (and (var? value) (fn? (:test (meta value))))
              (exact-selection-error "--exact names a missing or non-test Var"
                                     {:name qualified-name :namespace namespace-name}))
            value))
        names))

(defn- run-selected-tests!
  "Run selected Vars with the same namespace fixture and report boundaries as run-tests."
  [vars]
  (binding [test/*report-counters* (ref test/*initial-report-counters*)]
    (doseq [[namespace-object namespace-vars]
            (sort-by (comp str key)
                     (reduce (fn [groups value]
                               (update groups (-> value meta :ns) (fnil conj []) value))
                             {}
                             vars))]
      (test/do-report {:type :begin-test-ns :ns namespace-object})
      (test/test-vars namespace-vars)
      (test/do-report {:type :end-test-ns :ns namespace-object}))
    (let [summary @test/*report-counters*]
      (test/do-report (assoc summary :type :summary))
      summary)))

(defn- run-exact-selection!
  [args]
  (let [vars (try
               (resolve-loaded-test-vars (parse-exact-selection args))
               (catch clojure.lang.ExceptionInfo error
                 (binding [*out* *err*]
                   (println (str "Exact CI selection rejected: " (.getMessage error))))
                 nil))]
    (if vars
      (run-selected-tests! vars)
      {:test 0 :pass 0 :fail 1 :error 0})))

(defn- run-command!
  [args]
  (if (seq args)
    (run-exact-selection! args)
    (run-tests 'ci.run-test 'ci.lint-context-test)))

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

(deftest public-release-workflow-is-manual-read-only-pinned-and-provenanced
  (let [workflow (slurp ".github/workflows/public-release.yml")
        toolchain (slurp ".github/actions/clojure/action.yml")]
    (is (str/includes? workflow "workflow_dispatch:"))
    (doseq [event ["pull_request:" "push:" "schedule:"]]
      (is (not (str/includes? workflow event)) event))
    (is (str/includes? workflow "permissions:\n  contents: read"))
    (is (not (re-find #"(?im)^\s*(contents|packages):\s*write\b" workflow)))
    (is (not (re-find #"(?im)^\s*continue-on-error:\s*true\b" workflow)))
    (is (not (re-find #"(?i)\bpublish(?:ing|ed)?\b" workflow)))
    (is (re-find #"actions/checkout@[0-9a-f]{40}" workflow))
    (is (re-find #"actions/upload-artifact@[0-9a-f]{40}" workflow))
    (is (str/includes? workflow "uses: ./.github/actions/clojure"))
    (is (re-find #"actions/setup-java@[0-9a-f]{40}" toolchain))
    (is (re-find #"DeLaGuardo/setup-clojure@[0-9a-f]{40}" toolchain))
    (is (str/includes? workflow "fetch-depth: 0"))
    (is (str/includes? workflow "persist-credentials: false"))
    (is (str/includes? workflow "timeout-minutes: 20"))
    (is (str/includes? workflow
                       "clojure -Srepro -M tools/ci/run.clj public-release"))
    (is (re-find #"(?s)Reject tracked source mutation\s+if: always\(\)\s+run: git diff --exit-code HEAD"
                 workflow))
    (is (str/includes? workflow "git rev-parse HEAD 'HEAD^{tree}'"))
    (is (str/includes? workflow "target/ci/SHA256SUMS"))
    (is (str/includes? workflow "actions/upload-artifact@"))
    (is (str/includes? workflow "if: always()"))
    (is (str/includes? workflow "path: target/ci/"))))

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

(def expected-contract-command-ids
  ["docs" "roadmap" "roadmap-negative-controls" "dependency-controls"
   "tooling-native-validation" "ownership-native-validation" "wasm-native-validation"
   "c-native-validation" "runtime-native-validation" "effects-native-validation"
   "types-native-validation" "c7-collection-call-inference-kernels" "c7-set-equality-routing"])

(def ^:private public-adapter-events (atom []))
(def ^:private public-adapter-throw? (atom false))
(def ^:private public-adapter-failure (atom nil))
(def ^:private public-adapter-artifact-dir "original-p18-artifact-dir")

(defn- public-adapter-current-artifact-dir []
  (let [value public-adapter-artifact-dir]
    (if (instance? clojure.lang.IDeref value) @value value)))

(defn- public-adapter-record! [phase args]
  (swap! public-adapter-events conj [phase (public-adapter-current-artifact-dir) (vec args)]))

(defn- public-adapter-shell-run [& args]
  (public-adapter-record! :shell-run args)
  {:exit 0})

(defn- public-adapter-t02-package [& args]
  (public-adapter-record! :t02-package args)
  :t02-return)

(defn- public-adapter-t04-sweep [& args]
  (public-adapter-record! :t04-sweep args)
  (if @public-adapter-throw?
    (throw @public-adapter-failure)
    :t04-return))

(defn- public-adapter-accepted-record [& args]
  (public-adapter-record! :accepted-fixture args)
  :accepted-return)

(defn- public-adapter-rejected-record [& args]
  (public-adapter-record! :rejected-fixture args)
  :rejected-return)

(defn- public-adapter-capture-runner [argv]
  (swap! public-adapter-events conj [:runner (public-adapter-current-artifact-dir) argv])
  :runner-return)

(defn- public-adapter-exercising-runner [argv]
  (swap! public-adapter-events conj [:runner (public-adapter-current-artifact-dir) argv])
  (let [shell (public-adapter-shell-run ["gravity" "test"])
        t02 (public-adapter-t02-package {:fixture :package})
        t04 (public-adapter-t04-sweep {:fixture :sweep})
        accepted (public-adapter-accepted-record {:fixture :accepted :command ["ok"]})
        rejected (public-adapter-rejected-record {:fixture :rejected :command ["bad"]})]
    (swap! public-adapter-events conj [:returns shell t02 t04 accepted rejected])
    0))

(defn- public-adapter-var [name]
  (or (ns-resolve 'ci.public-release-subset (symbol name))
      (throw (ex-info "Expected public adapter Var is not loaded" {:name name}))))

(defn- public-adapter-runtime [runner]
  {:runner runner
   :artifact-dir #'public-adapter-artifact-dir
   :shell-run #'public-adapter-shell-run
   :t02-package #'public-adapter-t02-package
   :t04-sweep #'public-adapter-t04-sweep
   :accepted-record #'public-adapter-accepted-record
   :rejected-record #'public-adapter-rejected-record})

(defn- public-adapter-error-writer [flushes]
  (proxy [java.io.StringWriter] []
    (flush []
      (swap! flushes inc)
      (proxy-super flush))))

(defn- public-adapter-artifact-snapshot []
  (let [root (io/file "target/ci/p18-public-test-artifacts")]
    (if (.isDirectory root)
      (into {}
            (map (fn [file] [(.getCanonicalPath ^java.io.File file) file]))
            (filter #(.isFile ^java.io.File %)
                    (file-seq root)))
      {})))

(defn- public-adapter-new-files [before]
  (let [after (public-adapter-artifact-snapshot)]
    (->> after
         (remove (fn [[path _]] (contains? before path)))
         (into {}))))

(defn- public-adapter-new-files-between [before after]
  (into {}
        (remove (fn [[path _]] (contains? before path)))
        after))

(defn- public-adapter-file-payload [^java.io.File file]
  (let [text (slurp file)]
    {:path (.getCanonicalPath file)
     :text text
     :value (try
              (edn/read-string text)
              (catch Throwable _ ::not-edn))}))

(defn- public-adapter-nested-contains? [value expected]
  (or (= value expected)
      (cond
        (map? value) (some #(public-adapter-nested-contains? % expected) value)
        (coll? value) (some #(public-adapter-nested-contains? % expected) value)
        :else false)))

(defn- public-adapter-payloads [files]
  (mapv (comp public-adapter-file-payload val) files))

(defn- public-adapter-manifest-payload? [{:keys [value]}]
  (and (map? value)
       (= "gravity/ci-public-release-diagnostic-v1" (:schema value))
       (= :diagnostic (:authority value))
       (false? (:authoritative? value))
       (sequential? (:records value))))

(defn- public-adapter-record-payload? [{:keys [value]}]
  (and (map? value)
       (= "gravity/ci-public-release-diagnostic-v1" (:schema value))
       (= :diagnostic (:authority value))
       (false? (:authoritative? value))
       (contains? value :record)))

(defn- public-adapter-sha256-file [^java.io.File file]
  (let [digest (doto (java.security.MessageDigest/getInstance "SHA-256")
                 (.update (java.nio.file.Files/readAllBytes (.toPath file))))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn- public-adapter-file-for-path [files path]
  (let [canonical-path (.getCanonicalPath (io/file path))]
    (some (fn [[candidate file]]
            (when (= canonical-path candidate) file))
          files)))

(defn- public-adapter-payload-contains? [payloads expected]
  (some (fn [{:keys [text value]}]
          (or (str/includes? text (str expected))
              (public-adapter-nested-contains? value expected)))
        payloads))

(defn- public-adapter-diagnostic-payload? [{:keys [value]}]
  (and (map? value)
       (= "gravity/ci-public-release-diagnostic-v1" (:schema value))
       (= :diagnostic (:authority value))
       (false? (:authoritative? value))))

(defn- public-adapter-diagnostic-runner
  ([accepted rejected]
   (public-adapter-diagnostic-runner accepted rejected nil))
  ([accepted rejected snapshots]
   (fn [argv]
     (let [accepted-result (public-adapter-accepted-record accepted)
           _after-one (when snapshots
                        (swap! snapshots conj (public-adapter-artifact-snapshot)))
           rejected-result (public-adapter-rejected-record rejected)
           _after-two (when snapshots
                        (swap! snapshots conj (public-adapter-artifact-snapshot)))
           sweep-result (public-adapter-t04-sweep {:fixture "later-sweep"})]
       {:argv (vec argv)
        :accepted accepted-result
        :rejected rejected-result
        :sweep sweep-result}))))

(def ^:private report-diagnostic-schema
  "gravity/ci-public-release-diagnostic-v1")

(defn- report-fixture-root! []
  (let [parent (io/file "target/ci")]
    (.mkdirs parent)
    (.toFile
     (java.nio.file.Files/createTempDirectory
      (.toPath parent)
      "public-diagnostic-report-"
      (make-array java.nio.file.attribute.FileAttribute 0)))))

(defn- report-fixture-write! [root relative content]
  (let [file (io/file root relative)]
    (when-let [parent (.getParentFile file)]
      (.mkdirs parent))
    (java.nio.file.Files/write
     (.toPath file)
     (.getBytes (str content) java.nio.charset.StandardCharsets/UTF_8)
     (into-array java.nio.file.OpenOption
                 [java.nio.file.StandardOpenOption/CREATE
                  java.nio.file.StandardOpenOption/TRUNCATE_EXISTING
                  java.nio.file.StandardOpenOption/WRITE]))
    file))

(defn- report-fixture-write-edn! [root relative value]
  (report-fixture-write! root relative (str (pr-str value) "\n")))

(defn- report-fixture-sha256-text [text]
  (let [digest (doto (java.security.MessageDigest/getInstance "SHA-256")
                 (.update (.getBytes (str text) java.nio.charset.StandardCharsets/UTF_8)))]
    (apply str (map #(format "%02x" (bit-and (int %) 0xff))
                    (.digest digest)))))

(defn- report-fixture-output-identity [text]
  (let [bytes (.getBytes (str text) java.nio.charset.StandardCharsets/UTF_8)]
    {:utf8-bytes (alength bytes)
     :sha256 (report-fixture-sha256-text text)}))

(defn- report-fixture-checksum-path [root file]
  (str "./"
       (str/replace
        (str (.relativize (.toPath root) (.toPath file)))
        #"\\"
        "/")))

(defn- report-fixture-write-checksums! [root]
  (let [checksum-path (.getCanonicalPath (io/file root "SHA256SUMS"))
        lines (->> (file-seq root)
                   (filter #(.isFile ^java.io.File %))
                   (remove #(= checksum-path (.getCanonicalPath ^java.io.File %)))
                   (map (fn [file]
                          (str (public-adapter-sha256-file file)
                               "  "
                               (report-fixture-checksum-path root file))))
                   sort)]
    (report-fixture-write! root "SHA256SUMS" (str (str/join "\n" lines) "\n"))))

(defn- report-fixture-snapshot [root]
  (into {}
        (map (fn [file]
               [(.getCanonicalPath ^java.io.File file)
                (vec (java.nio.file.Files/readAllBytes (.toPath file)))]))
        (filter #(.isFile ^java.io.File %)
                (file-seq root))))

(defn- report-fixture-call-error [thunk]
  (try
    (thunk)
    nil
    (catch Throwable error
      error)))

(defn- report-fixture-logical-path [root file]
  (str "target/ci/"
       (str/replace
        (str (.relativize (.toPath root) (.toPath file)))
        #"\\"
        "/")))

(defn- report-fixture-record-metadata [root file phase ordinal]
  {:path (report-fixture-logical-path root file)
   :phase phase
   :ordinal ordinal
   :bytes (.length ^java.io.File file)
   :sha256 (public-adapter-sha256-file file)})

(defn- report-fixture-manifest [root directory status records]
  {:schema report-diagnostic-schema
   :authority :diagnostic
   :authoritative? false
   :status status
   :directory (report-fixture-logical-path root directory)
   :records records})

(defn- report-fixture-marker-text [marker]
  (let [tail (pr-str (dissoc marker :marker))]
    (str "{:marker \"ci-public-release-subset\" " (subs tail 1))))

(defn- report-fixture!
  ([] (report-fixture! {}))
  ([options]
   (let [root (report-fixture-root!)
         indexes? (get options :indexes? true)
         final-index? (and indexes? (get options :final-index? true))
         missing-result? (get options :missing-result? false)
         assessment-status (get options :assessment-status :failed)
         exit-code (get options :exit-code 1)
         commit (apply str (repeat 40 "1"))
         receipt-commit (get options :receipt-commit commit)
         tree (apply str (repeat 40 "2"))
         run-id (get options :run-id "424242")
         run-attempt "3"
         run-url (get options
                      :run-url
                      "https://github.com/burn2delete/uniform/actions/runs/424242")
         directory (io/file root "p18-public-test-artifacts/run-synthetic")
         accepted-relative "p18-public-test-artifacts/run-synthetic/accepted-fixture-0001-record.edn"
         rejected-relative "p18-public-test-artifacts/run-synthetic/rejected-fixture-0002-record.edn"
         partial-one-relative "p18-public-test-artifacts/run-synthetic/partial-manifest-0001.edn"
         partial-two-relative "p18-public-test-artifacts/run-synthetic/partial-manifest-0002.edn"
         final-relative "p18-public-test-artifacts/run-synthetic/manifest-0003.edn"
         accepted-out (apply str (repeat 96 "accepted stdout \u03a9\u2713\n"))
         accepted-err (apply str (repeat 96 "accepted stderr \u03bb\n"))
         run-out (str accepted-out "run-only \u00e9\n")
         run-err (str accepted-err "run stderr \u03b4\n")
         compile-out (str accepted-out "compile-only \u03bb\n")
         compile-err (str accepted-err "compile stderr \u03a9\n")
         executable-out (str accepted-out "executable-only \u03c0\n")
         executable-err (str accepted-err "executable stderr \u03b4\n")
         rejected-out (apply str (repeat 96 "rejected stdout \u2713\n"))
         rejected-err (apply str (repeat 96 "rejected stderr \u00e4\n"))
         accepted (cond-> {:fixture "examples/\u03c0.gravity"
                           :expected-stdout "accepted \u2713\n"
                           :check-result {:exit 0 :out accepted-out :err accepted-err}
                           :run-result {:exit 0 :out run-out :err run-err}
                           :compile-result {:exit 0 :out compile-out :err compile-err}
                           :executable-result {:exit 0
                                               :out executable-out
                                               :err executable-err}}
                    missing-result? (dissoc :compile-result))
         rejected {:fixture "fixtures/rejected/\u03bb.qst"
                   :command :check
                   :expected-diagnostic "P18REPORT"
                   :result {:exit 1 :out rejected-out :err rejected-err}}
         accepted-file (report-fixture-write-edn!
                        root accepted-relative
                        {:schema report-diagnostic-schema
                         :authority :diagnostic
                         :authoritative? false
                         :phase :accepted-fixture
                         :ordinal 1
                         :record accepted})
         rejected-file (report-fixture-write-edn!
                        root rejected-relative
                        {:schema report-diagnostic-schema
                         :authority :diagnostic
                         :authoritative? false
                         :phase :rejected-fixture
                         :ordinal 2
                         :record rejected})
         accepted-metadata (report-fixture-record-metadata root accepted-file
                                                           :accepted-fixture
                                                           1)
         rejected-metadata (report-fixture-record-metadata root rejected-file
                                                           :rejected-fixture
                                                           2)
         partial-one (report-fixture-manifest root directory :partial [accepted-metadata])
         partial-two (report-fixture-manifest root directory :partial
                                              [accepted-metadata rejected-metadata])
         final (report-fixture-manifest root directory :final
                                        [accepted-metadata rejected-metadata])
         partial-one-file (when indexes?
                            (report-fixture-write-edn! root partial-one-relative partial-one))
         partial-two-file (when indexes?
                            (report-fixture-write-edn! root partial-two-relative partial-two))
         final-file (when final-index?
                      (report-fixture-write-edn! root final-relative final))
         receipt {:id "public-release-subset"
                  :argv ["clojure" "-X:dev-test" "ci.public-release-subset/run!"]
                  :timeout-seconds 900
                  :started-ms 1700000000000
                  :status assessment-status
                  :commit receipt-commit
                  :run-id run-id
                  :run-attempt run-attempt
                  :stdin-path nil
                  :finished-ms 1700000004321
                  :exit-code exit-code}
         marker (fn [phase edge monotonic-ns data]
                  (merge {:marker "ci-public-release-subset"
                          :phase phase
                          :edge edge
                          :monotonic-ns monotonic-ns}
                         data))
         markers (vec
                  (concat
                   [(marker :runtime-resolution :enter 1 {})
                    (marker :runtime-resolution :return 2 {})
                    (marker :accepted-fixture :enter 3
                            {:fixture (:fixture accepted)})
                    (marker :accepted-fixture :return 4
                            {:fixture (:fixture accepted)})
                    (marker :accepted-fixture :diagnostic-captured 5
                            accepted-metadata)
                    (marker :rejected-fixture :enter 6
                            {:fixture (:fixture rejected)})
                    (marker :rejected-fixture :return 7
                            {:fixture (:fixture rejected)})
                    (marker :rejected-fixture :diagnostic-captured 8
                            rejected-metadata)]
                   (when indexes?
                     (concat
                      [(marker :diagnostic-capture :manifest-written 9
                               {:path (report-fixture-logical-path root partial-one-file)
                                :status :partial
                                :bytes (.length ^java.io.File partial-one-file)
                                :sha256 (public-adapter-sha256-file partial-one-file)})
                       (marker :diagnostic-capture :manifest-written 10
                               {:path (report-fixture-logical-path root partial-two-file)
                                :status :partial
                                :bytes (.length ^java.io.File partial-two-file)
                                :sha256 (public-adapter-sha256-file partial-two-file)})]
                      (when final-index?
                        [(marker :diagnostic-capture :manifest-written 11
                                 {:path (report-fixture-logical-path root final-file)
                                  :status :final
                                  :bytes (.length ^java.io.File final-file)
                                  :sha256 (public-adapter-sha256-file final-file)})])))))]
     (report-fixture-write! root "checkout.txt" (str commit "\n" tree "\n"))
     (report-fixture-write! root "run-url.txt" (str run-url "\n"))
     (report-fixture-write-edn! root "public-release-subset.edn" receipt)
     (report-fixture-write!
      root
      "public-release-subset.log"
      (str "CI command public-release-subset started\n"
           (str/join "\n" (map report-fixture-marker-text markers))
           "\nCI command public-release-subset finished "
           (name assessment-status) " exit " exit-code "\n"))
     (report-fixture-write-checksums! root)
     {:root root
      :accepted accepted
      :rejected rejected
      :accepted-out accepted-out
      :accepted-err accepted-err
      :run-out run-out
      :run-err run-err
      :compile-out compile-out
      :compile-err compile-err
      :executable-out executable-out
      :executable-err executable-err
      :rejected-out rejected-out
      :rejected-err rejected-err
      :accepted-relative accepted-relative
      :rejected-relative rejected-relative
      :partial-one-relative partial-one-relative
      :partial-two-relative partial-two-relative
      :final-relative final-relative
      :accepted-file accepted-file
      :rejected-file rejected-file
      :partial-one-file partial-one-file
      :partial-two-file partial-two-file
      :final-file final-file
      :commit commit
      :tree tree
      :run-id run-id
      :run-attempt run-attempt
      :run-url run-url})))

(defn- report-record-for-phase [report phase]
  (some #(when (= phase (:phase %)) %) (:records report)))

(defn- report-read-one-form [text]
  (with-open [reader (java.io.PushbackReader.
                      (java.io.StringReader. (str text)))]
    (binding [*read-eval* false]
      [(read {:eof ::report-eof} reader)
       (read {:eof ::report-eof} reader)])))

(deftest contracts-include-the-complete-native-tooling-and-ownership-namespaces
  (let [commands (ci/commands-for-selection "contracts" {:kind :full})]
    (is (= expected-contract-command-ids
           (mapv first commands)))
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

(deftest public-release-command-is-separate-from-ordinary-contracts
  (let [change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})
        selections [nil
                    {:kind :full :reason :non-pull-request :paths []}
                    {:kind :full :reason :unknown-or-cross-stage-path
                     :paths ["unknown/new-owner.clj"]}
                    {:kind :full :reason :path-discovery-failed
                     :paths [] :selection-error "discovery failed"}
                    {:kind :docs-only :reason :known-documentation-only
                     :paths ["README.md"]}
                    (ci/stage-selection
                     (mapv change ["tools/advance/README.md"
                                   "tools/advance/src/advance/main.clj"
                                   "tools/advance/test/advance/main_test.clj"]))
                    (ci/stage-selection [(change "tools/ci/lint.clj")])]
        stage-paths ["bootstrap/clojure/src/gravity/reader.clj"
                     "language/compiler/src/gravity/macro.gravity"
                     "bootstrap/clojure/src/gravity/resolution.clj"
                     "language/compiler/src/gravity/checked_core.gravity"]
        public-command (fn [selection]
                         (let [commands (vec (ci/commands-for-selection "public-release" selection))]
                           (is (= ["public-release-subset"] (mapv first commands)))
                           (is (= [["public-release-subset" 900
                                    (ci/command "2g" "-X:dev-test"
                                                "ci.public-release-subset/run!")]]
                                  commands))))]
    (is (not (contains? ci/semantic-modes "public-release")))
    (is (not-any? #(str/starts-with? % "public-release=")
                  (ci/selection-output-lines nil)))
    (is (not (str/includes? (slurp ".github/workflows/ci.yml") "public-release")))
    (doseq [selection selections]
      (is (= expected-contract-command-ids
             (mapv first (ci/commands-for-selection "contracts" selection))))
      (public-command selection))
    (doseq [path stage-paths]
      (let [selection (ci/stage-selection [(change path)])]
        (is (= :selected (:kind selection)) path)
        (is (= :affected-stage (:reason selection)) path)
        (is (= expected-contract-command-ids
               (mapv first (ci/commands-for-selection "contracts" selection))) path)
        (public-command selection)))))

(deftest contracts-omit-public-subset-for-narrow-exclusions
  (let [change (fn [path] {:old-mode "100644" :new-mode "100644"
                           :status "M" :path path})
        docs-only {:kind :docs-only :reason :known-documentation-only
                   :paths ["README.md"]}
        advance (ci/stage-selection
                 (mapv change ["tools/advance/README.md"
                               "tools/advance/src/advance/main.clj"
                               "tools/advance/test/advance/main_test.clj"]))
        lint-tooling (ci/stage-selection [(change "tools/ci/lint.clj")])]
    (doseq [[label expected-kind selection]
            [["docs-only" :docs-only docs-only]
             ["advance-status-only" :selected advance]
             ["lint-tooling-only" :selected lint-tooling]]]
      (is (= expected-kind (:kind selection)) label)
      (is (contains? #{:known-documentation-only :advance-status-only :lint-tooling-only}
                     (:reason selection)) label)
      (is (= expected-contract-command-ids
             (mapv first (ci/commands-for-selection "contracts" selection))) label)
      (is (not-any? #(= "public-release-subset" (first %))
                    (ci/commands-for-selection "contracts" selection)) label))))

(deftest public-adapter-defers-runtime-resolution-and-delegates-exact-argv-once
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        resolver (public-adapter-var "resolve-runtime-vars")
        exit-var (public-adapter-var "exit!")
        resolver-calls (atom 0)
        exits (atom [])
        runner-results (with-redefs-fn
                         {#'public-adapter-artifact-dir "original-p18-artifact-dir"
                          resolver (fn [] (swap! resolver-calls inc) {})
                          (public-adapter-var "run-with-runtime!") (fn [_] 0)
                          exit-var (fn [code] (swap! exits conj code))}
                         (fn [] [(public/run! nil) (public/run! {}) (public/run!)]))]
    (is (= [0 0 0] runner-results))
    (is (= [0 0 0] @exits))
    (is (= 3 @resolver-calls))
    (reset! public-adapter-events [])
    (let [result (run-with-runtime (public-adapter-runtime
                                    #'public-adapter-capture-runner))]
      (is (= :runner-return result))
      (is (= [[:runner "target/ci/p18-public-test-artifacts"
               ["--progress"
                "--namespace" "gravity.bootstrap-test"
                "--exact" "p18-public-test-command-runs-current-public-subset"]]]
             @public-adapter-events)))))

(deftest public-adapter-isolates-artifacts-and-restores-on-return-or-throw
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        original-vars [#'public-adapter-artifact-dir
                       #'public-adapter-shell-run
                       #'public-adapter-t02-package
                       #'public-adapter-t04-sweep
                       #'public-adapter-accepted-record
                       #'public-adapter-rejected-record]
        originals (mapv var-get original-vars)
        flushes (atom 0)
        writer (public-adapter-error-writer flushes)
        assert-monotonic-markers!
        (fn [output]
          (let [markers (->> (str/split-lines (str output))
                             (remove str/blank?)
                             (mapv edn/read-string))]
            (is (seq markers))
            (is (every? #(integer? (:monotonic-ns %)) markers))
            (when (seq markers)
              (is (apply <= (mapv :monotonic-ns markers))))))]
    (reset! public-adapter-events [])
    (reset! public-adapter-throw? false)
    (let [result (binding [*err* writer]
                   (run-with-runtime (public-adapter-runtime
                                      #'public-adapter-exercising-runner)))]
      (is (= 0 result))
      (is (some #(and (= :runner (first %))
                      (= "target/ci/p18-public-test-artifacts" (second %)))
                @public-adapter-events))
      (is (some #(= [:returns {:exit 0} :t02-return :t04-return
                     :accepted-return :rejected-return]
                    %)
                @public-adapter-events))
      (is (= "original-p18-artifact-dir" (public-adapter-current-artifact-dir)))
      (is (= originals (mapv var-get original-vars)))
      (is (pos? @flushes))
      (doseq [phase [:p18-shell-run :t02-package :t04-sweep
                     :accepted-fixture :rejected-fixture]]
        (is (str/includes? (str writer) (str ":phase " phase)) phase))
      (is (str/includes? (str writer) ":edge :return"))
      (assert-monotonic-markers! writer))
    (let [failure (ex-info "fixture wrapper failure" {:phase :t04})
          throw-flushes (atom 0)
          throw-writer (public-adapter-error-writer throw-flushes)]
      (reset! public-adapter-events [])
      (reset! public-adapter-failure failure)
      (reset! public-adapter-throw? true)
      (let [caught (try
                     (binding [*err* throw-writer]
                       (run-with-runtime (public-adapter-runtime
                                          #'public-adapter-exercising-runner)))
                     nil
                     (catch Throwable error error))]
        (is (identical? failure caught))
        (is (= "original-p18-artifact-dir" (public-adapter-current-artifact-dir)))
        (is (= originals (mapv var-get original-vars)))
        (is (pos? @throw-flushes))
        (is (str/includes? (str throw-writer) ":phase :t04-sweep"))
        (is (str/includes? (str throw-writer) ":edge :throw"))
        (assert-monotonic-markers! throw-writer)))
    (reset! public-adapter-throw? false)))

(deftest public-adapter-rejects-unknown-options-before-heavy-load-and-propagates-nonzero-exit
  (let [resolver (public-adapter-var "resolve-runtime-vars")
        run-with-runtime (public-adapter-var "run-with-runtime!")
        exit-var (public-adapter-var "exit!")
        resolver-calls (atom 0)
        exits (atom [])
        unknown-error (try
                        (with-redefs-fn
                          {resolver (fn [] (swap! resolver-calls inc) {})}
                          (fn [] (public/run! {:unexpected-option true})))
                        nil
                        (catch Throwable error error))
        nonmap-error (try
                       (with-redefs-fn
                         {resolver (fn [] (swap! resolver-calls inc) {})}
                         (fn [] (public/run! [:unexpected-option])))
                       nil
                       (catch Throwable error error))]
    (is (instance? clojure.lang.ExceptionInfo unknown-error))
    (is (instance? clojure.lang.ExceptionInfo nonmap-error))
    (is (zero? @resolver-calls))
    (let [result (with-redefs-fn
                   {resolver (fn [] (swap! resolver-calls inc) {:runtime :fake})
                    run-with-runtime (fn [_] 9)
                    exit-var (fn [code] (swap! exits conj code))}
                   (fn [] (public/run! {})))]
      (is (= 9 result))
      (is (= [9] @exits))
      (is (= 1 @resolver-calls)))
    (let [invalid-error (with-redefs-fn
                          {resolver (fn [] {:runtime :fake})
                           run-with-runtime (fn [_] :not-an-exit)
                           exit-var (fn [code] (swap! exits conj code))}
                          (fn []
                            (try
                              (public/run! {})
                              nil
                              (catch Throwable error error))))]
      (is (instance? clojure.lang.ExceptionInfo invalid-error))
      (is (= [9] @exits)))))

(deftest public-adapter-retains-fixture-results-before-later-sweep-throw
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        accepted-out (str "h\u00e9llo \u2713\ncheck \u03a9\ncompile \u03bb\n"
                          (apply str (repeat 64 "accepted stdout \u03a9\n")))
        accepted-err (str "stdout \u03a9\nstderr \u03bb\n"
                          (apply str (repeat 64 "accepted stderr \u03bb\n")))
        rejected-out (apply str (repeat 64 "rejected stdout \u2713\n"))
        rejected-err (apply str (repeat 64 "rejected stderr \u00e9\n"))
        accepted {:fixture "examples/\u03c0.gravity"
                  :expected-stdout "h\u00e9llo \u2713\n"
                  :check-result {:exit 0 :out accepted-out :err accepted-err}
                  :run-result {:exit 0 :out accepted-out :err accepted-err}
                  :compile-result {:exit 0 :out accepted-out :err accepted-err}
                  :executable-result {:exit 0 :out accepted-out :err accepted-err}}
        rejected {:fixture "bootstrap/clojure/fixtures/rejected/\u03bb.qst"
                  :command :check
                  :expected-diagnostic "P18\u0394"
                  :result {:exit 1 :out rejected-out :err rejected-err}}
        failure (ex-info "later public sweep failed" {:phase :t04-sweep})
        originals (mapv var-get [#'public-adapter-shell-run
                                 #'public-adapter-t02-package
                                 #'public-adapter-t04-sweep
                                 #'public-adapter-accepted-record
                                 #'public-adapter-rejected-record])
        before (public-adapter-artifact-snapshot)
        snapshots (atom [])
        caught (with-redefs-fn
                 {#'public-adapter-shell-run (fn [& _] {:exit 0})
                  #'public-adapter-t02-package (fn [& _] :t02-return)
                  #'public-adapter-t04-sweep (fn [& _] (throw failure))
                  #'public-adapter-accepted-record identity
                  #'public-adapter-rejected-record identity}
                 (fn []
                   (binding [*print-length* 1
                             *print-level* 1]
                     (try
                       (run-with-runtime
                        (public-adapter-runtime
                         (public-adapter-diagnostic-runner accepted rejected snapshots)))
                       nil
                       (catch Throwable error error)))))
        files (public-adapter-new-files before)
        payloads (public-adapter-payloads files)
        [after-one after-two] @snapshots
        first-files (public-adapter-new-files-between before after-one)
        second-files (public-adapter-new-files-between after-one after-two)
        first-manifests (filter public-adapter-manifest-payload?
                                (public-adapter-payloads first-files))
        second-manifests (filter public-adapter-manifest-payload?
                                 (public-adapter-payloads second-files))]
    (is (identical? failure caught))
    (is (= originals (mapv var-get [#'public-adapter-shell-run
                                    #'public-adapter-t02-package
                                    #'public-adapter-t04-sweep
                                    #'public-adapter-accepted-record
                                    #'public-adapter-rejected-record])))
    (is (= public/public-test-argv
           ["--progress" "--namespace" "gravity.bootstrap-test"
            "--exact" "p18-public-test-command-runs-current-public-subset"]))
    (is (= 2 (count @snapshots)))
    (is (= 1 (count first-manifests)))
    (is (= 1 (count second-manifests)))
    (is (= :partial (:status (:value (first first-manifests)))))
    (is (= :partial (:status (:value (first second-manifests)))))
    (is (= [:accepted-fixture]
           (mapv :phase (get-in (first first-manifests) [:value :records]))))
    (is (= [:accepted-fixture :rejected-fixture]
           (mapv :phase (get-in (first second-manifests) [:value :records]))))
    (is (seq files))
    (is (some public-adapter-diagnostic-payload? payloads))
    (is (public-adapter-payload-contains? payloads accepted))
    (is (public-adapter-payload-contains? payloads rejected))
    (is (public-adapter-payload-contains? payloads :accepted-fixture))
    (is (public-adapter-payload-contains? payloads :rejected-fixture))
    (doseq [value ["h\u00e9llo \u2713\n" accepted-out accepted-err
                   rejected-out rejected-err 0 1]]
      (is (public-adapter-payload-contains? payloads value) (pr-str value)))
    (let [record-payloads (filter public-adapter-record-payload? payloads)
          manifest-payloads (filter public-adapter-manifest-payload? payloads)]
      (is (= 2 (count record-payloads)))
      (is (some #(= accepted (get-in % [:value :record])) record-payloads))
      (is (some #(= rejected (get-in % [:value :record])) record-payloads))
      (is (seq manifest-payloads))
      (doseq [{:keys [value]} manifest-payloads
              {:keys [path bytes sha256]} (:records value)]
        (let [file (public-adapter-file-for-path files path)]
          (is (some? file) (str "manifest record path missing: " path))
          (when file
            (is (= bytes (.length ^java.io.File file)))
            (is (= sha256 (public-adapter-sha256-file file)))))))))

(deftest public-adapter-uses-fresh-diagnostic-files-for-each-invocation
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        accepted {:fixture "examples/repeat.gravity"
                  :expected-stdout "repeat \u2713\n"
                  :check-result {:exit 0 :out "check-1\n" :err ""}}
        rejected {:fixture "fixtures/rejected/repeat.qst"
                  :command :check
                  :expected-diagnostic "P18R"
                  :result {:exit 1 :out "reject-1\n" :err "reject-err \u03a9\n"}}
        invoke! (fn []
                  (with-redefs-fn
                    {#'public-adapter-shell-run (fn [& _] {:exit 0})
                     #'public-adapter-t02-package (fn [& _] :t02-return)
                     #'public-adapter-t04-sweep (fn [& _] :t04-return)
                     #'public-adapter-accepted-record identity
                     #'public-adapter-rejected-record identity}
                    (fn []
                      (run-with-runtime
                       (public-adapter-runtime
                        (public-adapter-diagnostic-runner accepted rejected))))))
        before (public-adapter-artifact-snapshot)
        first-result (invoke!)
        after-first (public-adapter-artifact-snapshot)
        first-files (public-adapter-new-files before)
        first-text (into {} (map (fn [[path file]] [path (slurp file)])) first-files)
        second-result (invoke!)
        after-second (public-adapter-artifact-snapshot)
        second-files (public-adapter-new-files after-first)
        first-payloads (public-adapter-payloads first-files)
        second-payloads (public-adapter-payloads second-files)
        first-paths (set (keys first-files))
        second-paths (set (keys second-files))]
    (is (= first-result second-result))
    (is (seq first-files))
    (is (seq second-files))
    (is (some public-adapter-diagnostic-payload? first-payloads))
    (is (some public-adapter-diagnostic-payload? second-payloads))
    (is (empty? (set/intersection first-paths second-paths)))
    (is (= first-text
           (into {} (map (fn [[path file]] [path (slurp file)]))
                 (select-keys after-second first-paths))))
    (is (public-adapter-payload-contains? first-payloads accepted))
    (is (public-adapter-payload-contains? second-payloads accepted))
    (is (public-adapter-payload-contains? first-payloads rejected))
    (is (public-adapter-payload-contains? second-payloads rejected))
    (doseq [payloads [first-payloads second-payloads]]
      (is (some #(and (str/includes? (:text %) ":sha256")
                      (re-find #"(?i)[0-9a-f]{64}" (:text %)))
                payloads)))
    (is (= (set (keys after-second))
           (set (keys (public-adapter-artifact-snapshot)))))))

(deftest public-adapter-writes-fresh-prefix-manifests-before-runner-progresses
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        accepted {:fixture "examples/prefix.gravity"
                  :expected-stdout "prefix \u2713\n"
                  :check-result {:exit 0 :out "check\n" :err ""}}
        rejected {:fixture "fixtures/rejected/prefix.qst"
                  :command :check
                  :expected-diagnostic "P18PREFIX"
                  :result {:exit 1 :out "reject\n" :err "reject-err\n"}}
        before (public-adapter-artifact-snapshot)
        snapshots (atom [])
        prefix-texts (atom [])
        runner (fn [argv]
                 (let [accepted-result (public-adapter-accepted-record accepted)
                       after-one (public-adapter-artifact-snapshot)]
                   (swap! snapshots conj after-one)
                   (swap! prefix-texts conj
                          (into {} (map (fn [[path file]] [path (slurp file)]))
                                (public-adapter-new-files-between before after-one)))
                   (let [rejected-result (public-adapter-rejected-record rejected)
                         after-two (public-adapter-artifact-snapshot)
                         sweep-result (public-adapter-t04-sweep {:fixture "after-prefix"})]
                     (swap! snapshots conj after-two)
                     (swap! prefix-texts conj
                            (into {} (map (fn [[path file]] [path (slurp file)]))
                                  (public-adapter-new-files-between before after-two)))
                     {:argv (vec argv)
                      :accepted accepted-result
                      :rejected rejected-result
                      :sweep sweep-result})))
        result (with-redefs-fn
                 {#'public-adapter-shell-run (fn [& _] {:exit 0})
                  #'public-adapter-t02-package (fn [& _] :t02-return)
                  #'public-adapter-t04-sweep (fn [& _] :t04-return)
                  #'public-adapter-accepted-record identity
                  #'public-adapter-rejected-record identity}
                 (fn []
                   (run-with-runtime (public-adapter-runtime runner))))
        [after-one after-two] @snapshots
        after-run (public-adapter-artifact-snapshot)
        first-files (public-adapter-new-files-between before after-one)
        second-files (public-adapter-new-files-between after-one after-two)
        final-files (public-adapter-new-files-between after-two after-run)
        first-payloads (public-adapter-payloads first-files)
        second-payloads (public-adapter-payloads second-files)
        final-payloads (public-adapter-payloads final-files)
        first-manifests (filter public-adapter-manifest-payload? first-payloads)
        second-manifests (filter public-adapter-manifest-payload? second-payloads)
        final-manifests (filter public-adapter-manifest-payload? final-payloads)
        first-paths (set (keys first-files))
        second-paths (set (keys second-files))
        final-paths (set (keys final-files))]
    (is (= {:argv public/public-test-argv
            :accepted accepted
            :rejected rejected
            :sweep :t04-return}
           result))
    (is (= 2 (count @snapshots)))
    (is (= 1 (count first-manifests)))
    (is (= 1 (count second-manifests)))
    (is (seq final-manifests))
    (is (= :partial (:status (:value (first first-manifests)))))
    (is (= :partial (:status (:value (first second-manifests)))))
    (is (some #(= :final (:status (:value %))) final-manifests))
    (is (= [:accepted-fixture]
           (mapv :phase (get-in (first first-manifests) [:value :records]))))
    (is (= [:accepted-fixture :rejected-fixture]
           (mapv :phase (get-in (first second-manifests) [:value :records]))))
    (is (some #(= 2 (count (:records (:value %)))) final-manifests))
    (is (empty? (set/intersection first-paths second-paths)))
    (is (empty? (set/intersection first-paths final-paths)))
    (is (empty? (set/intersection second-paths final-paths)))
    (doseq [prefix @prefix-texts]
      (is (= prefix
             (into {} (map (fn [[path file]] [path (slurp file)]))
                   (select-keys after-run (keys prefix))))))))

(deftest public-adapter-capture-io-failure-is-visible-and-preserves-semantics
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        write-bytes-var (public-adapter-var "write-bytes!")
        accepted {:fixture "examples/io-failure.gravity"
                  :expected-stdout "done \u03bb\n"
                  :check-result {:exit 0 :out "ok\n" :err ""}}
        rejected {:fixture "fixtures/rejected/io-failure.qst"
                  :command :check
                  :expected-diagnostic "P18IO"
                  :result {:exit 1 :out "out \u03a9\n" :err "err \u2713\n"}}
        calls (atom [])
        attempts (atom 0)
        original-argv public/public-test-argv
        original-values (mapv var-get [#'public-adapter-shell-run
                                       #'public-adapter-t02-package
                                       #'public-adapter-t04-sweep
                                       #'public-adapter-accepted-record
                                       #'public-adapter-rejected-record])
        writer (java.io.StringWriter.)
        successful-result
        (with-redefs-fn
          {#'public-adapter-shell-run (fn [& _] {:exit 0})
           #'public-adapter-t02-package (fn [& _] :t02-return)
           #'public-adapter-t04-sweep (fn [& _] (swap! calls conj :t04) :t04-return)
           #'public-adapter-accepted-record (fn [record] (swap! calls conj :accepted) record)
           #'public-adapter-rejected-record (fn [record] (swap! calls conj :rejected) record)
           write-bytes-var (fn [& _]
                             (swap! attempts inc)
                             (throw (ex-info "simulated diagnostic capture I/O failure"
                                             {:attempt @attempts})))}
          (fn []
            (binding [*err* writer]
              (run-with-runtime
               (public-adapter-runtime
                (public-adapter-diagnostic-runner accepted rejected))))))
        thrown (ex-info "original sweep exception" {:phase :t04-sweep})
        throw-writer (java.io.StringWriter.)
        throw-attempts (atom 0)
        caught
        (with-redefs-fn
          {#'public-adapter-shell-run (fn [& _] {:exit 0})
           #'public-adapter-t02-package (fn [& _] :t02-return)
           #'public-adapter-t04-sweep (fn [& _] (throw thrown))
           #'public-adapter-accepted-record identity
           #'public-adapter-rejected-record identity
           write-bytes-var (fn [& _]
                             (swap! throw-attempts inc)
                             (throw (ex-info "simulated diagnostic capture I/O failure"
                                             {:attempt @throw-attempts})))}
          (fn []
            (binding [*err* throw-writer]
              (try
                (run-with-runtime
                 (public-adapter-runtime
                  (public-adapter-diagnostic-runner accepted rejected)))
                nil
                (catch Throwable error error)))))]
    (is (= {:argv public/public-test-argv
            :accepted accepted
            :rejected rejected
            :sweep :t04-return}
           successful-result))
    (is (= [:accepted :rejected :t04] @calls))
    (is (= 1 @attempts))
    (is (re-find #"(?is)(capture|diagnostic).*(throw|fail|error)"
                 (str writer)))
    (is (identical? thrown caught))
    (is (= 1 @throw-attempts))
    (is (re-find #"(?is)(capture|diagnostic).*(throw|fail|error)"
                 (str throw-writer)))
    (is (= original-argv public/public-test-argv))
    (is (= original-values
           (mapv var-get [#'public-adapter-shell-run
                          #'public-adapter-t02-package
                          #'public-adapter-t04-sweep
                          #'public-adapter-accepted-record
                          #'public-adapter-rejected-record])))))

(deftest public-adapter-manifest-io-failure-stops-later-capture-without-altering-results
  (let [run-with-runtime (public-adapter-var "run-with-runtime!")
        write-bytes-var (public-adapter-var "write-bytes!")
        original-write (var-get write-bytes-var)
        accepted {:fixture "examples/manifest-io.gravity"
                  :expected-stdout "manifest \u03bb\n"
                  :check-result {:exit 0 :out "ok\n" :err ""}}
        rejected {:fixture "fixtures/rejected/manifest-io.qst"
                  :command :check
                  :expected-diagnostic "P18MANIFEST"
                  :result {:exit 1 :out "out \u03a9\n" :err "err \u2713\n"}}
        calls (atom [])
        attempts (atom 0)
        originals (mapv var-get [#'public-adapter-shell-run
                                 #'public-adapter-t02-package
                                 #'public-adapter-t04-sweep
                                 #'public-adapter-accepted-record
                                 #'public-adapter-rejected-record])
        writer (java.io.StringWriter.)
        successful-result
        (with-redefs-fn
          {#'public-adapter-shell-run (fn [& _] {:exit 0})
           #'public-adapter-t02-package (fn [& _] :t02-return)
           #'public-adapter-t04-sweep (fn [& _] (swap! calls conj :t04) :t04-return)
           #'public-adapter-accepted-record (fn [record] (swap! calls conj :accepted) record)
           #'public-adapter-rejected-record (fn [record] (swap! calls conj :rejected) record)
           write-bytes-var (fn [path bytes]
                             (if (= 2 (swap! attempts inc))
                               (throw (ex-info "simulated diagnostic manifest I/O failure"
                                               {:path (str path)}))
                               (original-write path bytes)))}
          (fn []
            (binding [*err* writer]
              (run-with-runtime
               (public-adapter-runtime
                (public-adapter-diagnostic-runner accepted rejected))))))
        thrown (ex-info "original manifest sweep exception" {:phase :t04-sweep})
        throw-attempts (atom 0)
        throw-writer (java.io.StringWriter.)
        caught
        (with-redefs-fn
          {#'public-adapter-shell-run (fn [& _] {:exit 0})
           #'public-adapter-t02-package (fn [& _] :t02-return)
           #'public-adapter-t04-sweep (fn [& _] (throw thrown))
           #'public-adapter-accepted-record identity
           #'public-adapter-rejected-record identity
           write-bytes-var (fn [path bytes]
                             (if (= 2 (swap! throw-attempts inc))
                               (throw (ex-info "simulated diagnostic manifest I/O failure"
                                               {:path (str path)}))
                               (original-write path bytes)))}
          (fn []
            (binding [*err* throw-writer]
              (try
                (run-with-runtime
                 (public-adapter-runtime
                  (public-adapter-diagnostic-runner accepted rejected)))
                nil
                (catch Throwable error error)))))]
    (is (= {:argv public/public-test-argv
            :accepted accepted
            :rejected rejected
            :sweep :t04-return}
           successful-result))
    (is (= [:accepted :rejected :t04] @calls))
    (is (= 2 @attempts))
    (is (str/includes? (str writer) ":diagnostic-capture-failed"))
    (is (str/includes? (str writer) ":diagnostic-capture-skipped"))
    (is (str/includes? (str writer) "simulated diagnostic manifest I/O failure"))
    (is (identical? thrown caught))
    (is (= 2 @throw-attempts))
    (is (str/includes? (str throw-writer) ":diagnostic-capture-failed"))
    (is (str/includes? (str throw-writer) ":diagnostic-capture-skipped"))
    (is (str/includes? (str throw-writer) "simulated diagnostic manifest I/O failure"))
    (is (= public/public-test-argv
           ["--progress" "--namespace" "gravity.bootstrap-test"
            "--exact" "p18-public-test-command-runs-current-public-subset"]))
    (is (= originals
           (mapv var-get [#'public-adapter-shell-run
                          #'public-adapter-t02-package
                          #'public-adapter-t04-sweep
                          #'public-adapter-accepted-record
                          #'public-adapter-rejected-record])))))

(deftest public-diagnostic-report-joins-valid-prefix-and-final-indexes
  (let [{:keys [root accepted rejected accepted-out accepted-err run-out run-err
                compile-out compile-err executable-out executable-err
                rejected-out rejected-err accepted-relative rejected-relative
                partial-one-relative partial-two-relative final-relative
                accepted-file rejected-file partial-one-file partial-two-file final-file
                commit tree run-id run-attempt run-url]}
        (report-fixture!)
        before (report-fixture-snapshot root)
        report (diagnostic-report/report (str root))
        after (report-fixture-snapshot root)
        accepted-report (report-record-for-phase report :accepted-fixture)
        rejected-report (report-record-for-phase report :rejected-fixture)
        manifests (:manifests report)
        observed (:observed report)]
    (is (= before after) "report must not mutate the extracted artifact")
    (is (= "gravity/ci-public-diagnostic-report-v1" (:schema report)))
    (is (= :diagnostic (:authority report)))
    (is (false? (:authoritative? report)))
    (is (= {:commit commit
            :tree tree
            :run-id run-id
            :run-url run-url
            :attempt run-attempt}
           (:recorded report)))
    (is (= {:status :failed
            :exit-code 1
            :timeout-seconds 900
            :elapsed-ms 4321}
           (:assessment report)))
    (is (= :final (:index-status report)))
    (is (= {:entered 2
            :return-markers 2
            :captured 2
            :indexed 2
            :partial-manifests 2
            :final-manifests 1}
           observed))
    (is (= 2 (count (:records report))))
    (is (= #{:accepted-fixture :rejected-fixture}
           (set (map :phase (:records report)))))
    (is (= (:fixture accepted) (:fixture accepted-report)))
    (is (= 1 (:ordinal accepted-report)))
    (is (set/subset? #{:check-result :run-result :compile-result :executable-result}
                     (set (:record-keys accepted-report))))
    (is (not (contains? accepted-report :record)))
    (is (= (:fixture rejected) (:fixture rejected-report)))
    (is (= 2 (:ordinal rejected-report)))
    (is (contains? (set (:record-keys rejected-report)) :result))
    (is (= {:path accepted-relative
            :bytes (.length ^java.io.File accepted-file)
            :sha256 (public-adapter-sha256-file accepted-file)}
           (:file accepted-report)))
    (is (= {:path rejected-relative
            :bytes (.length ^java.io.File rejected-file)
            :sha256 (public-adapter-sha256-file rejected-file)}
           (:file rejected-report)))
    (doseq [[result-key out-text err-text]
            [[:check-result accepted-out accepted-err]
             [:run-result run-out run-err]
             [:compile-result compile-out compile-err]
             [:executable-result executable-out executable-err]]]
      (let [result (get-in accepted-report [:results result-key])]
        (is (= #{:exit :out :err} (set (keys result))) result-key)
        (is (= 0 (:exit result)) result-key)
        (doseq [[stream text] [[:out out-text] [:err err-text]]]
          (let [identity (get result stream)]
            (is (= #{:record-path :field-path :utf8-bytes :sha256}
                   (set (keys identity)))
                [result-key stream])
            (is (= accepted-relative (:record-path identity))
                [result-key stream])
            (is (= [:record result-key stream] (:field-path identity))
                [result-key stream])
            (is (= (report-fixture-output-identity text)
                   (select-keys identity [:utf8-bytes :sha256]))
                [result-key stream])))))
    (let [result (get-in rejected-report [:results :result])]
      (is (= #{:exit :out :err} (set (keys result))))
      (is (= 1 (:exit result)))
      (doseq [[stream text] [[:out rejected-out] [:err rejected-err]]]
        (let [identity (get result stream)]
          (is (= #{:record-path :field-path :utf8-bytes :sha256}
                 (set (keys identity))) stream)
          (is (= rejected-relative (:record-path identity)) stream)
          (is (= [:record :result stream] (:field-path identity)) stream)
          (is (= (report-fixture-output-identity text)
                 (select-keys identity [:utf8-bytes :sha256])) stream))))
    (let [record-file-id (:file accepted-report)
          stdout-id (get-in accepted-report [:results :check-result :out])]
      (is (not= (:bytes record-file-id) (:utf8-bytes stdout-id)))
      (is (not= (:sha256 record-file-id) (:sha256 stdout-id))))
    (is (= #{:partial :final} (set (map :status manifests))))
    (is (= [1 2 2] (sort (map :record-count manifests))))
    (is (= #{partial-one-relative partial-two-relative final-relative}
           (set (map :path manifests))))
    (is (every? #(= #{:path :bytes :sha256} (set (keys (:file %)))) manifests))
    (is (= {:path partial-one-relative
            :bytes (.length ^java.io.File partial-one-file)
            :sha256 (public-adapter-sha256-file partial-one-file)}
           (:file (some #(when (= partial-one-relative (:path %)) %) manifests))))
    (is (= {:path partial-two-relative
            :bytes (.length ^java.io.File partial-two-file)
            :sha256 (public-adapter-sha256-file partial-two-file)}
           (:file (some #(when (= partial-two-relative (:path %)) %) manifests))))
    (is (= {:path final-relative
            :bytes (.length ^java.io.File final-file)
            :sha256 (public-adapter-sha256-file final-file)}
           (:file (some #(when (= final-relative (:path %)) %) manifests))))
    (is (map? (:inputs report)))
    (is (seq (get-in report [:inputs :files])))
    (is (re-matches #"[0-9a-f]{64}"
                    (get-in report [:inputs :checksums :file :sha256])))
    (is (every? #(re-matches #"[0-9a-f]{64}" (:sha256 %))
                (vals (get-in report [:inputs :files]))))
    (is (contains? (set (:nonclaims report))
                   :final-index-is-not-assessment-completion))
    (is (contains? (set (:nonclaims report))
                   :diagnostic-not-release-or-admission-authority))
    (is (= before (report-fixture-snapshot root)))))

(deftest public-diagnostic-report-retains-timeout-and-absent-index-state
  (let [{:keys [root]} (report-fixture! {:indexes? false
                                         :assessment-status :timeout
                                         :exit-code 124})
        report (diagnostic-report/report (str root))
        observed (:observed report)]
    (is (= :timeout (:status (:assessment report))))
    (is (= 124 (:exit-code (:assessment report))))
    (is (= 900 (:timeout-seconds (:assessment report))))
    (is (= :not-observed (:index-status report)))
    (is (empty? (:manifests report)))
    (is (= 2 (:entered observed)))
    (is (= 2 (:return-markers observed)))
    (is (= 2 (:captured observed)))
    (is (= :not-observed (:indexed observed)))
    (is (= 0 (:partial-manifests observed)))
    (is (= 0 (:final-manifests observed)))
    (is (= 2 (count (:records report))))
    (is (false? (:authoritative? report))))
  (let [{:keys [root]} (report-fixture! {:final-index? false})
        report (diagnostic-report/report (str root))
        observed (:observed report)]
    (is (= :partial (:index-status report)))
    (is (= 2 (:indexed observed)))
    (is (= 2 (:partial-manifests observed)))
    (is (= 0 (:final-manifests observed)))
    (is (= [:partial :partial] (mapv :status (:manifests report))))))

(deftest public-diagnostic-report-run-validates-exact-options-and-emits-one-edn
  (let [{:keys [root]} (report-fixture!)
        invalid-options [nil
                         {}
                         {:artifact-dir ""}
                         {:artifact-dir 42}
                         {:artifact-dir (str root) :unexpected true}]]
    (let [report-calls (atom 0)]
      (with-redefs [diagnostic-report/report
                    (fn [& _] (swap! report-calls inc) {})]
        (doseq [options invalid-options]
          (let [error (report-fixture-call-error
                       #(diagnostic-report/run! options))]
            (is (instance? Throwable error) (pr-str options)))))
      (is (zero? @report-calls) "invalid options must be rejected before artifact I/O"))
    (let [before (report-fixture-snapshot root)
          output (with-out-str
                   (diagnostic-report/run! {:artifact-dir (str root)}))
          [value trailing] (report-read-one-form output)]
      (is (map? value))
      (is (= ::report-eof trailing))
      (is (= (diagnostic-report/report (str root)) value))
      (is (= before (report-fixture-snapshot root))))))

(deftest public-diagnostic-report-rejects-tampered-confined-and-unsupported-inputs
  (let [{:keys [root accepted-relative]} (report-fixture!)]
    (report-fixture-write! root accepted-relative "tampered\n")
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"SHA256SUMS digest"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root]} (report-fixture!)
        checksums (slurp (io/file root "SHA256SUMS"))]
    (report-fixture-write! root "SHA256SUMS" (str checksums checksums))
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"duplicate paths"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root final-relative]} (report-fixture!)
        manifest (edn/read-string (slurp (io/file root final-relative)))]
    (report-fixture-write-edn!
     root
     final-relative
     (assoc-in manifest [:records 0 :path] "../outside.edn"))
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"traversal"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root accepted-file]} (report-fixture!)
        link (io/file root "p18-public-test-artifacts/run-synthetic/link.edn")]
    (java.nio.file.Files/createSymbolicLink
     (.toPath link)
     (.toPath accepted-file)
     (make-array java.nio.file.attribute.FileAttribute 0))
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"symbolic link"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root accepted-relative]} (report-fixture!)]
    (report-fixture-write! root accepted-relative "{:malformed\n")
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Malformed EDN"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root accepted-relative]} (report-fixture!)
        envelope (edn/read-string (slurp (io/file root accepted-relative)))]
    (report-fixture-write-edn!
     root
     accepted-relative
     (assoc envelope :schema "gravity/unsupported-diagnostic-v99"))
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Unsupported diagnostic record schema"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root]} (report-fixture! {:missing-result? true})]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"missing a required result"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root final-relative]} (report-fixture!)
        manifest (edn/read-string (slurp (io/file root final-relative)))
        old-hash (get-in manifest [:records 0 :sha256])
        wrong-hash (str (if (str/starts-with? old-hash "0") "1" "0")
                        (subs old-hash 1))]
    (report-fixture-write-edn!
     root
     final-relative
     (assoc-in manifest [:records 0 :sha256] wrong-hash))
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"manifest record digest"
                          (diagnostic-report/report (str root)))))
  (let [{:keys [root partial-two-relative]} (report-fixture!)
        manifest (edn/read-string (slurp (io/file root partial-two-relative)))]
    (report-fixture-write-edn!
     root
     partial-two-relative
     (assoc-in manifest [:records 1 :ordinal] 3))
    (report-fixture-write-checksums! root)
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"contiguous returned prefix|ordinal"
                          (diagnostic-report/report (str root))))))

(deftest public-diagnostic-report-rejects-recorded-source-and-run-inconsistency
  (let [wrong-commit (apply str (repeat 40 "f"))
        {:keys [root]} (report-fixture! {:receipt-commit wrong-commit})
        error (report-fixture-call-error
               #(diagnostic-report/report (str root)))]
    (is (instance? Throwable error))
    (is (re-find #"commit disagrees" (.getMessage error))))
  (let [{:keys [root]} (report-fixture!
                        {:run-url "https://github.com/burn2delete/uniform/actions/runs/999999"})
        error (report-fixture-call-error
               #(diagnostic-report/report (str root)))]
    (is (instance? Throwable error))
    (is (re-find #"run-url.txt" (.getMessage error)))))

(deftest contracts-include-the-complete-native-wasm-namespace
  (let [[label timeout argv] (nth (ci/commands-for-selection "contracts" {:kind :full}) 6)
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

(deftest contracts-include-the-complete-native-c-namespace
  (let [[label timeout argv] (nth (ci/commands-for-selection "contracts" {:kind :full}) 7)
        namespace "gravity.backend.c.pipeline-test"
        source-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/backend/c/pipeline_test.clj"))]
    (is (= "c-native-validation" label))
    (is (= 180 timeout))
    (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
    (is (seq source-vars))
    (is (= source-vars (macro-selected-vars argv source-vars namespace)))
    (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
    (is (empty? (macro-selected-vars
                 (assoc argv (dec (count argv)) "gravity.mir.pipeline-test") source-vars namespace)))))

(deftest contracts-include-the-complete-native-runtime-effects-and-types-namespaces
  (doseq [[index expected-label namespace path]
          [[8 "runtime-native-validation" "gravity.runtime.pipeline-test"
            "bootstrap/clojure/test/gravity/runtime/pipeline_test.clj"]
           [9 "effects-native-validation" "gravity.effects.pipeline-test"
            "bootstrap/clojure/test/gravity/effects/pipeline_test.clj"]
           [10 "types-native-validation" "gravity.types.pipeline-test"
            "bootstrap/clojure/test/gravity/types/pipeline_test.clj"]]]
    (let [[label timeout argv] (nth (ci/commands-for-selection "contracts" {:kind :full}) index)
          source-vars (binding [*read-eval* false]
                        (source-deftest-names path))]
      (is (= expected-label label))
      (is (= 180 timeout))
      (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
      (is (seq source-vars))
      (is (= source-vars (macro-selected-vars argv source-vars namespace)))
      (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
      (is (empty? (macro-selected-vars
                   (assoc argv (dec (count argv)) "gravity.mir.pipeline-test") source-vars namespace))))))

(deftest contracts-include-the-complete-c7-set-equality-routing-namespace
  (let [[label timeout argv] (last (ci/commands-for-selection "contracts" {:kind :full}))
        namespace "gravity.self-hosting.sh08-set-equality-routing-test"
        source-vars (binding [*read-eval* false]
                      (source-deftest-names "bootstrap/clojure/test/gravity/self_hosting/sh08_set_equality_routing_test.clj"))]
    (is (= "c7-set-equality-routing" label))
    (is (= 180 timeout))
    (is (= (ci/command "2g" "-M:dev-test" "--progress" "--namespace" namespace) argv))
    (is (seq source-vars))
    (is (= source-vars (macro-selected-vars argv source-vars namespace)))
    (is (empty? (macro-selected-vars (vec (drop-last 2 argv)) source-vars namespace)))
    (is (empty? (macro-selected-vars
                 (assoc argv (dec (count argv)) "gravity.types.pipeline-test") source-vars namespace)))))

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

(deftest advance-status-selection-covers-every-eligible-subset-and-retains-controls
  (let [paths ["tools/advance/README.md"
               "tools/advance/src/advance/main.clj"
               "tools/advance/test/advance/main_test.clj"]
        change (fn [path]
                 {:old-mode "100644" :new-mode "100644" :status "M" :path path})
        subsets (mapv (fn [mask]
                        (vec (keep-indexed (fn [index path]
                                             (when (bit-test mask index) path)) paths)))
                      (range 1 8))
        leaf-catalog (ci/load-leaf-catalog)
        output-map (fn [selection]
                     (into {}
                           (map (fn [line]
                                  (let [[mode value] (str/split line #"=" 2)]
                                    [mode (= "true" value)]))
                                (ci/selection-output-lines selection))))]
    (is (= 7 (count subsets)))
    (doseq [subset subsets]
      (let [changes (mapv change subset)
            selection (ci/stage-selection changes)
            output (output-map selection)
            bootstrap-commands (set (map first (ci/commands-for-selection "bootstrap" selection)))]
        (is (ci/advance-status-only? changes) (pr-str subset))
        (is (= :selected (:kind selection)) (pr-str subset))
        (is (= :advance-status-only (:reason selection)) (pr-str subset))
        (is (= subset (:paths selection)) (pr-str subset))
        (is (= [] (:stages selection)) (pr-str subset))
        (is (= #{"static" "contracts" "bootstrap"} (:modes selection)) (pr-str subset))
        (doseq [mode ["static" "contracts" "bootstrap" "leaves-portable"
                      "leaves-darwin" "darwin-unsupported"]]
          (is (true? (get output mode)) (str mode " retained for " subset)))
        (doseq [mode ["macro" "resolution" "checked-core-runtime"]]
          (is (false? (get output mode)) (str mode " skipped for " subset))
          (is (ci/skip-for-stage-selection? mode selection) (str mode " skipped for " subset)))
        (doseq [mode ["resolution-pipeline" "resolution-unmetered"
                      "resolution-invocation-selection"]]
          (is (ci/skip-for-stage-selection? mode selection) (str mode " skipped for " subset)))
        (doseq [mode ["static" "contracts" "bootstrap"]]
          (is (not (ci/skip-for-stage-selection? mode selection))
              (str mode " retained for " subset)))
        (is (true? (get output "static-tools-required")))
        (is (true? (get output "selection-valid")))
        (doseq [command ["language-boundary" "catalog-controls" "ownership-inventory"
                         "bootstrap-input-controls"]]
          (is (contains? bootstrap-commands command)
              (str command " retained for " subset)))
        (doseq [platform ["portable" "darwin"]
                :let [leaf (ci/leaf-selection-for-changes platform leaf-catalog changes)
                      full-entries (ci/platform-leaf-catalog platform leaf-catalog)]]
          (is (= :full (:kind leaf)) (str platform " leaf selection for " subset))
          (is (= (mapv :id full-entries) (mapv :id (:entries leaf)))
              (str platform " keeps the full platform catalog for " subset)))))))

(deftest advance-status-selection-falls-back-for-all-other-records-and-inputs
  (let [status-path "tools/advance/src/advance/main.clj"
        regular {:old-mode "100644" :new-mode "100644" :status "M" :path status-path}
        full-selection (fn [changes] (ci/stage-selection changes))]
    (doseq [change [{:old-mode "000000" :new-mode "100644" :status "A" :path status-path}
                    {:old-mode "100644" :new-mode "000000" :status "D" :path status-path}
                    {:old-mode "100644" :new-mode "100644" :status "R" :path status-path}
                    {:old-mode "100644" :new-mode "100644" :status "C" :path status-path}
                    {:old-mode "100755" :new-mode "100644" :status "M" :path status-path}
                    {:old-mode "100644" :new-mode "100755" :status "M" :path status-path}
                    {:old-mode "100644" :new-mode "120000" :status "M" :path status-path}
                    {:old-mode "100644" :new-mode "100644" :status "T" :path status-path}]]
      (is (= :full (:kind (full-selection [change]))) (pr-str change)))
    (doseq [outside ["README.md" "docs/status.md" "language/compiler/src/gravity/new.gravity"
                     "tools/ci/run.clj" ".github/workflows/ci.yml" "contracts/ci-policy.edn"
                     "specs/compiler.md" "unknown/new-owner.clj"]]
      (is (= :full (:kind (full-selection [regular (assoc regular :path outside)])))
          (str "mixed input must remain full: " outside)))
    (doseq [changes [[]
                     [nil]
                     [(dissoc regular :status)]
                     [(assoc regular :path nil)]
                     [regular regular]]]
      (is (= :full (:kind (full-selection changes))) (pr-str changes)))
    (is (= :no-changes (:reason (full-selection []))))
    (let [selection {:kind :full :reason :non-pull-request :paths []}]
      (is (= #{"static=true" "contracts=true" "bootstrap=true" "macro=true"
               "resolution=true" "checked-core-runtime=true" "leaves-portable=true"
               "leaves-darwin=true" "darwin-unsupported=true" "static-tools-required=true"
               "selection-valid=true"}
             (set (ci/selection-output-lines selection)))))
    (let [discovery-fallback {:kind :full :reason :path-discovery-failed
                              :paths [] :selection-error "stale or malformed discovery"}]
      (is (= :failed (:status (ci/selection-error-result "static" discovery-fallback))))
      (is (nil? (ci/selection-error-result "bootstrap" discovery-fallback))))))

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
    (let [ordinary-catalog (ci/commands-for-selection "bootstrap" ordinary-reader)
          ordinary-commands (mapv first ordinary-catalog)]
      (is (some #{"reader-regression"} ordinary-commands))
      (is (some #{"syntax-regression"} ordinary-commands))
      (is (not (has-input-controls? ordinary-reader)))
      (let [[syntax-id syntax-timeout syntax-argv]
            (some #(when (= "syntax-regression" (first %)) %) ordinary-catalog)]
        (is (= "syntax-regression" syntax-id))
        (is (= 600 syntax-timeout))
        (is (= (ci/command "2g" "-M:dev-test"
                           "--namespace" "gravity.syntax.pipeline-test"
                           "--namespace" "gravity.syntax.owner-calls-test"
                           "--namespace" "gravity.bootstrap-compatibility.c3-test"
                           "--namespace" "gravity.self-hosting.sh04-fresh-c2-construction-test"
                           "--namespace" "gravity.self-hosting.sh04-form-index-test"
                           "--namespace" "gravity.self-hosting.sh04-literal-index-test")
               syntax-argv))))
    (doseq [path boundary-paths]
      (is (ci/bootstrap-input-control-path? path) path)
      (is (has-input-controls? (selected-boundary path)) path))
    (is (has-input-controls? full))
    (is (has-input-controls? unknown))
    (is (has-input-controls? nil))
    (is (not (has-input-controls? docs-only)))))

(deftest exact-selection-validates-all-names-before-running
  (let [invoked (atom false)
        selected-test "ci.run-test/bootstrap-selection-runs-historical-input-controls-only-at-boundary"
        result (with-redefs [run-selected-tests!
                             (fn [_]
                               (reset! invoked true)
                               {:test 1 :pass 1 :fail 0 :error 0})]
                 (run-exact-selection! [exact-selector
                                        selected-test
                                        "ci.run-test/exact-selection-missing-test"]))]
    (is (= {:test 0 :pass 0 :fail 1 :error 0} result))
    (is (false? @invoked))))

(deftest exact-selection-does-not-swallow-execution-ex-info
  (let [selected-test "ci.run-test/bootstrap-selection-runs-historical-input-controls-only-at-boundary"]
    (with-redefs [run-selected-tests!
                  (fn [_] (throw (ex-info "selected test failed" {:phase :execution})))]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"selected test failed"
                            (run-exact-selection! [exact-selector selected-test]))))))

(deftest exact-selection-preserves-fixtures-and-report-counters
  (let [once-calls (atom 0)
        each-calls (atom 0)
        body-calls (atom 0)
        fixture-ns (create-ns (symbol (str "ci.run-test.fixture-" (gensym))))
        test-body (fn []
                    (is (= 1 (swap! body-calls inc))))
        test-var (intern fixture-ns 'fixture-test test-body)]
    (try
      (alter-meta! test-var assoc :test test-body)
      (alter-meta! fixture-ns assoc
                   :clojure.test/once-fixtures
                   [(fn [f] (swap! once-calls inc) (f))]
                   :clojure.test/each-fixtures
                   [(fn [f] (swap! each-calls inc) (f))])
      (let [result (run-selected-tests! [test-var])]
        (is (= {:test 1 :pass 1 :fail 0 :error 0} result))
        (is (= 1 @once-calls))
        (is (= 1 @each-calls))
        (is (= 1 @body-calls)))
      (finally
        (remove-ns (ns-name fixture-ns))))))

(deftest no-argument-dispatch-keeps-full-suite-namespaces
  (let [summary {:test 2 :pass 2 :fail 0 :error 0}
        calls (atom [])]
    (with-redefs [run-tests (fn [& namespaces]
                              (swap! calls conj namespaces)
                              summary)]
      (is (= summary (run-command! nil)))
      (is (= [['ci.run-test 'ci.lint-context-test]] @calls)))))

(let [result (if (seq *command-line-args*)
               (run-command! *command-line-args*)
               (run-command! nil))]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
