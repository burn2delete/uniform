(ns ci.run-test
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is run-tests]]))

(binding [*command-line-args* nil] (load-file "tools/ci/run.clj"))
(require '[ci.run :as ci])
(binding [*command-line-args* nil] (load-file "tools/ci/lint.clj"))
(require '[ci.lint :as lint])
(require '[ci.lint-context :as context])

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
            :when (.endsWith (.getName file) ".clj")]
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
      (doseq [paths [["bootstrap/gravity/src/gravity/runtime.gravity"]
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
    (doseq [mode ["bootstrap" "macro" "resolution" "checked-core-runtime"]]
      (is (ci/skip-for-docs-only? mode {:kind :docs-only}) mode))
    (is (not (ci/skip-for-docs-only? "static" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "contracts" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "leaves-portable" {:kind :docs-only})))
    (is (not (ci/skip-for-docs-only? "macro" {:kind :full})))
    (is (= [["whitespace" 30 ["git" "diff" "--check" base "HEAD"]]]
           (ci/docs-only-static-commands {:base base :kind :docs-only})))))

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
           (:modes (ci/stage-selection [(change "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity")]))))
    (is (= expected-resolution
           (:modes (ci/stage-selection [(change "bootstrap/clojure/test/gravity/resolution/pipeline_test.clj")]))))
    (is (= expected-checked
           (:modes (ci/stage-selection [(change "bootstrap/gravity/src/gravity/checked_core.gravity")]))))
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
    (doseq [result [empty-result delete-result cross-cutting-result]]
      (is (= :full (:kind result)))
      (is (= 52 (count (:entries result)))))))

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

(deftest timeout-is-a-failure
  (let [result (ci/execute! "negative-timeout" 1
                            (ci/command "128m" "-M" "-e" "(Thread/sleep 30000)"))]
    (is (= :timeout (:status result)))
    (is (not (alive? (:pid result)))))
  (is (= 124 (:exit-code (read-string (slurp "target/ci/negative-timeout.edn"))))))

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

(let [result (run-tests)]
  (System/exit (if (zero? (+ (:fail result) (:error result))) 0 1)))
