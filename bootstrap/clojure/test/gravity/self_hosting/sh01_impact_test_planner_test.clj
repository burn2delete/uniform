(ns gravity.self-hosting.sh01-impact-test-planner-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-component-test-dependencies
             :as component-dependencies]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]
            [gravity.self-hosting-test-runner :as runner])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]))

(defn- run-process!
  [directory & command]
  (let [directory (if (instance? java.nio.file.Path directory)
                    (.toFile ^java.nio.file.Path directory)
                    (io/file directory))
        process (-> (ProcessBuilder. ^java.util.List (vec command))
                    (.directory directory)
                    (.redirectErrorStream true)
                    .start)
        output (slurp (.getInputStream process))
        exit-code (.waitFor process)]
    (when-not (zero? exit-code)
      (throw (ex-info "test process failed"
                      {:command command :exit exit-code :output output})))
    (str/trim output)))

(defn- delete-tree!
  [root]
  (when (Files/exists root (make-array java.nio.file.LinkOption 0))
    (with-open [paths (Files/walk root
                                  (make-array
                                   java.nio.file.FileVisitOption 0))]
      (doseq [path (reverse (vec (iterator-seq (.iterator paths))))]
        (Files/deleteIfExists path)))))

(deftest backlog-dependency-expansion-is-transitive
  (let [dependencies (planner/backlog-dependencies)
        affected (planner/downstream-closure dependencies #{"SH-06"})]
    (is (= #{"SH-05"} (get dependencies "SH-06")))
    (is (contains? affected "SH-06"))
    (is (contains? affected "SH-07"))
    (is (contains? affected "SH-29"))
    (is (not (contains? affected "SH-05")))))

(deftest backlog-dependency-table-fails-closed
  (let [dependencies (planner/backlog-dependencies)
        exception-data
        (fn [entries]
          (try
            (planner/validate-backlog-dependency-entries entries)
            nil
            (catch clojure.lang.ExceptionInfo exception
              (ex-data exception))))]
    (is (= "SH01-IMPACT-BACKLOG"
           (:id
            (exception-data
             (vec (dissoc dependencies "SH-29"))))))
    (is (= ["SH-99"]
           (:unknown-dependencies
            (exception-data
             (vec
              (assoc dependencies "SH-00" #{"SH-99"}))))))
    (is (= ["SH-00"]
           (:duplicate-slices
            (exception-data
             (conj (vec dependencies) ["SH-00" #{}])))))))

(deftest slice-plan-selects-only-owned-dedicated-tests
  (let [plan
        (planner/build-plan
         {:direct-slices #{"SH-01"}
          :expand-dependants? false})]
    (is (= :gravity/sh01-impact-test-plan-v1 (:schema plan)))
    (is (= ["SH-01"] (:affected-slices plan)))
    (is (some
         #{'gravity.self-hosting.sh01-ownership-test}
         (:namespaces plan)))
    (is (some
         #{'gravity.self-hosting.sh01-impact-test-planner-test}
         (:namespaces plan)))
    (is (every?
         #(= "SH-01" (:slice %))
         (:shards plan)))))

(deftest changed-dedicated-test-keeps-governance-closure-but-selects-exactly
  (let [plan
        (planner/build-plan
         {:changed-paths
          ["bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_adapter_test.clj"]
          :expand-dependants? true})]
    (is (= ["SH-06"] (:direct-slices plan)))
    (is (contains? (set (:affected-slices plan)) "SH-07"))
    (is (contains? (set (:affected-slices plan)) "SH-29"))
    (is (= [] (:execution-affected-slices plan)))
    (is (= ['gravity.self-hosting.sh06-resolution-adapter-test]
           (:namespaces plan)))
    (is (= :exact-test-path
           (get-in plan [:classifications 0 :development-selection])))
    (is (= :changed-dedicated-test
           (get-in plan
                   [:classifications 0
                    :development-invalidation-reason])))))

(deftest reviewed-component-sources-select-declared-tests-without-changing-governance
  (doseq [[path expected-component expected-tests direct-count affected-count]
          [["bootstrap/clojure/src/gravity/compiler_pass_manifest.clj"
            "compiler-pass-manifest"
            ['gravity.compiler-pass-manifest-test
             'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test]
            30 30]
           ["bootstrap/clojure/src/gravity/c11_mir.clj"
            "c11-mir"
            ['gravity.bootstrap-compatibility.c11-test
             'gravity.c11-mir-test]
            2 18]]]
    (let [plan (planner/build-plan {:changed-paths [path]
                                    :expand-dependants? true})
          classification (first (:classifications plan))]
      (is (= expected-component (:component-id classification)) path)
      (is (= :reviewed-component-dependencies
             (:development-selection classification)) path)
      (is (= :reviewed-component-source
             (:development-invalidation-reason classification)) path)
      (is (= expected-tests (:namespaces plan)) path)
      (is (= direct-count (count (:direct-slices plan))) path)
      (is (= affected-count (count (:affected-slices plan))) path)
      (is (empty? (:execution-affected-slices plan)) path)
      (is (= :non-authoritative (:authority plan)) path)
      (is (false? (:authoritative? plan)) path)
      (doseq [namespace expected-tests]
        (is (= [namespace]
               (:namespaces
                (runner/select-tests
                 ["--namespace" (str namespace)])))
            [path namespace])))))

(deftest reviewed-component-test-path-selects-only-its-exact-namespace
  (let [path "bootstrap/clojure/test/gravity/compiler_pass_manifest_test.clj"
        plan (planner/build-plan {:changed-paths [path]
                                  :expand-dependants? true})]
    (is (= ['gravity.compiler-pass-manifest-test] (:namespaces plan)))
    (is (= :exact-test-path
           (get-in plan [:classifications 0 :development-selection])))
    (is (= :reviewed-component-test
           (get-in plan
                   [:classifications 0
                    :development-invalidation-reason])))
    (is (= 30 (count (:affected-slices plan))))
    (is (empty? (:execution-affected-slices plan)))))

(deftest deleted-reviewed-test-conservatively-selects-component-dependencies
  (let [exists-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'repository-path-exists?)]
    (with-redefs-fn
      {exists-var (constantly false)}
      (fn []
        (let [path
              "bootstrap/clojure/test/gravity/compiler_pass_manifest_test.clj"
              plan (planner/build-plan {:changed-paths [path]})]
          (is (= ['gravity.compiler-pass-manifest-test
                  'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test]
                 (:namespaces plan)))
          (is (= :deleted-reviewed-component-test
                 (get-in plan
                         [:classifications 0
                          :development-invalidation-reason]))))))))

(deftest component-dependency-contract-rejects-path-namespace-drift
  (let [contract (component-dependencies/read-contract)
        malformed
        (assoc-in contract [:components 0 :tests 0 :namespace]
                  'gravity.wrong-test)
        exception
        (try
          (component-dependencies/validate-contract malformed)
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-COMPONENT-DEPENDENCY-CONTRACT"
           (:id (ex-data exception))))))

(deftest stage0-c2-module-maps-to-reader-slice-and-downstream-closure
  (let [source "bootstrap/clojure/src/gravity/c2_artifact_identity.clj"
        plan
        (planner/build-plan
         {:changed-paths [source]
          :expand-dependants? true})
        classification (first (:classifications plan))]
    (is (= :module (:classification classification)))
    (is (= ["SH-03"] (:slices classification)))
    (is (= "c2-artifact-identity" (:component-id classification)))
    (is (= 'gravity.c2-artifact-identity-test
           (:test-namespace classification)))
    (is (= ["SH-03"] (:direct-slices plan)))
    (is (contains? (set (:affected-slices plan)) "SH-04"))
    (is (contains? (set (:affected-slices plan)) "SH-29"))
    (is (= ["c2-artifact-identity"]
           (mapv :component-id (:component-identities plan))))
    (is (= ['gravity.c2-artifact-identity-test]
           (:component-test-namespaces plan)))))

(deftest stage0-c7-test-maps-to-type-checker-slice
  (let [test-path "bootstrap/clojure/test/gravity/c7_type_checker_test.clj"
        plan
        (planner/build-plan
         {:changed-paths [test-path]
          :expand-dependants? false})
        classification (first (:classifications plan))]
    (is (= :module (:classification classification)))
    (is (= ["SH-08"] (:slices classification)))
    (is (= "c7-type-checker" (:component-id classification)))
    (is (= "bootstrap/clojure/src/gravity/c7_type_checker.clj"
           (:component-source-path classification)))
    (is (= 'gravity.c7-type-checker-test
           (:test-namespace classification)))
    (is (= ["SH-08"] (:direct-slices plan)))
    (is (= ['gravity.c7-type-checker-test]
           (:component-test-namespaces plan)))))

(deftest stage0-owned-test-path-preserves-owner-slice-closure
  (let [path "bootstrap/clojure/test/gravity/c2_pass_cache_test.clj"
        plan (planner/build-plan {:changed-paths [path]
                                  :expand-dependants? true})
        classification (first (:classifications plan))]
    (is (= :module (:classification classification)))
    (is (= ["SH-03"] (:slices classification)))
    (is (= "c2-pass-cache" (:component-id classification)))
    (is (= 'gravity.c2-pass-cache-test (:test-namespace classification)))
    (is (= [path] (:changed-paths plan)))
    (is (contains? (set (:affected-slices plan)) "SH-07"))))

(deftest stage0-c16-without-a-slice-owner-selects-the-full-non-authoritative-plan
  (let [path "bootstrap/clojure/src/gravity/c16_incremental.clj"
        plan (planner/build-plan {:changed-paths [path]
                                  :expand-dependants? true})
        classification (first (:classifications plan))]
    (is (= :module (:classification classification)))
    (is (= 30 (count (:slices classification))))
    (is (= :non-authoritative (:authority classification)))
    (is (false? (:authoritative? classification)))
    (is (true? (:component-cross-cutting? classification)))
    (is (= :reserved-component-owner-has-no-slice
           (:component-cross-cutting-reason classification)))
    (is (= "c16-incremental" (:component-id classification)))
    (is (= :sh-incremental (:component-owner classification)))
    (is (= 'gravity.c16-incremental-test
           (:test-namespace classification)))
    (is (= 30 (count (:direct-slices plan))))
    (is (= 30 (count (:affected-slices plan))))
    (is (= :non-authoritative (:authority plan)))
    (is (false? (:authoritative? plan)))
    (is (= [path] (:component-cross-cutting-paths plan)))))

(deftest stage0-c17-owned-test-without-a-slice-owner-is-cross-cutting
  (let [path "bootstrap/clojure/test/gravity/c17_plugin_test.clj"
        plan (planner/build-plan {:changed-paths [path]})
        classification (first (:classifications plan))]
    (is (= :module (:classification classification)))
    (is (= 30 (count (:slices classification))))
    (is (true? (:component-cross-cutting? classification)))
    (is (= "c17-plugin" (:component-id classification)))
    (is (= :sh-pass-api (:component-owner classification)))
    (is (= 'gravity.c17-plugin-test (:test-namespace classification)))
    (is (= 30 (count (:direct-slices plan))))
    (is (= :non-authoritative (:authority plan)))))

(deftest stage0-verification-components-without-slice-owners-are-cross-cutting
  (doseq [[path component-id test-namespace]
          [["bootstrap/clojure/src/gravity/c18_verification.clj"
            "c18-verification"
            'gravity.c18-verification-test]
           ["bootstrap/clojure/test/gravity/compiler_verification_shared_test.clj"
            "compiler-verification-shared"
            'gravity.compiler-verification-shared-test]]]
    (let [plan (planner/build-plan {:changed-paths [path]})
          classification (first (:classifications plan))]
      (is (= :module (:classification classification)) path)
      (is (= 30 (count (:slices classification))) path)
      (is (true? (:component-cross-cutting? classification)) path)
      (is (= :reserved-component-owner-has-no-slice
             (:component-cross-cutting-reason classification)) path)
      (is (= component-id (:component-id classification)) path)
      (is (= :sh-verification (:component-owner classification)) path)
      (is (= test-namespace (:test-namespace classification)) path)
      (is (= 30 (count (:direct-slices plan))) path)
      (is (= 30 (count (:affected-slices plan))) path)
      (is (= :non-authoritative (:authority plan)) path))))

(deftest coordinator-path-selects-the-conservative-full-plan
  (let [plan
        (planner/build-plan
         {:changed-paths ["bootstrap/clojure/src/gravity/bootstrap.clj"]
          :expand-dependants? true})]
    (is (= 30 (count (:direct-slices plan))))
    (is (= 30 (count (:affected-slices plan))))
    (is (= :coordinator
           (get-in plan [:classifications 0 :classification])))
    (is (= "bootstrap" (get-in plan [:classifications 0 :component-id])))
    (is (= 'gravity.bootstrap-test
           (get-in plan [:classifications 0 :test-namespace])))))

(deftest unrelated-paths-are-reported-but-do-not-select-tests
  (let [plan
        (planner/build-plan
         {:changed-paths ["README.md"]
          :expand-dependants? true})]
    (is (empty? (:affected-slices plan)))
    (is (empty? (:namespaces plan)))
    (is (= ["README.md"] (:ignored-paths plan)))))

(deftest unknown-stage0-top-level-source-and-test-fail-closed
  (doseq [relative
          ["bootstrap/clojure/src/gravity/not_registered.clj"
           "bootstrap/clojure/test/gravity/not_registered_test.clj"]]
    (let [exception
          (try
            (planner/build-plan
             {:changed-paths [relative]
              :expand-dependants? true})
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-IMPACT-UNOWNED" (:id (ex-data exception))) relative)
      (is (= [relative] (:paths (ex-data exception))) relative))))

(deftest changed-path-plans-are-canonical
  (let [first-path
        "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_test.clj"
        second-path
        "bootstrap/clojure/test/gravity/self_hosting/sh07_checked_core_test.clj"
        plan
        (planner/build-plan
         {:changed-paths [second-path first-path second-path]
          :expand-dependants? false})]
    (is (= [first-path second-path] (:changed-paths plan)))
    (is (= [first-path second-path]
           (mapv :path (:classifications plan))))
    (is (every? vector? (map :slices (:classifications plan))))
    (is (= [["SH-06"] ["SH-07"]]
           (mapv :slices (:classifications plan))))))

(deftest unowned-self-hosting-paths-fail-closed
  (let [exception
        (try
          (planner/build-plan
           {:changed-paths
            ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
            :expand-dependants? true})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-IMPACT-UNOWNED" (:id (ex-data exception))))
    (is (= ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
           (:paths (ex-data exception))))))

(deftest out-of-range-dedicated-paths-fail-closed
  (doseq [relative
          ["bootstrap/clojure/test/gravity/self_hosting/sh99_unknown_test.clj"
           "bootstrap/clojure/fixtures/self-hosting/sh-99/accepted/unknown.gravity"]]
    (let [exception
          (try
            (planner/build-plan
             {:changed-paths [relative]
              :expand-dependants? true})
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-IMPACT-SLICE" (:id (ex-data exception))) relative)
      (is (= ["SH-99"] (:slices (ex-data exception))) relative))))

(deftest sh07-shards-carry-a-resource-class
  (let [plan
        (planner/build-plan
         {:direct-slices #{"SH-07"}
          :expand-dependants? false})]
    (testing "the planner exposes scheduling data without running tests"
      (is (seq (:shards plan)))
      (is (every?
           #(= :memory-heavy (:resource-class %))
           (:shards plan))))))

(deftest unknown-slices-fail-closed
  (let [exception
        (try
          (planner/build-plan {:direct-slices #{"SH-30"}})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-IMPACT-SLICE" (:id (ex-data exception))))
    (is (= ["SH-30"] (:slices (ex-data exception))))))

(deftest iteration-coordinator-path-is-explicitly-non-authoritative
  (let [plan
        (planner/build-plan
         {:changed-paths ["bootstrap/clojure/src/gravity/bootstrap.clj"]
          :iteration-slices #{"SH-07"}})]
    (is (= :gravity/sh01-impact-test-plan-v1 (:schema plan)))
    (is (= :non-authoritative (:authority plan)))
    (is (false? (:authoritative? plan)))
    (is (true? (:iteration? plan)))
    (is (true? (:full-gate-deferred? plan)))
    (is (seq (:full-gate-deferred-reason plan)))
    (is (= ["SH-07"] (:iteration-slices plan)))
    (is (= ["SH-07"] (:affected-slices plan)))
    (is (= ["bootstrap/clojure/src/gravity/bootstrap.clj"]
           (:deferred-coordinator-paths plan)))
    (is (empty? (:deferred-other-affected-paths plan)))
    (is (every? #(= "SH-07" (:slice %)) (:shards plan)))))

(deftest iteration-repeatable-slices-and-leaf-catalog-selection
  (let [plan
        (planner/build-plan
         {:changed-paths
          ["bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/new.gravity"
           "bootstrap/gravity/src/gravity/checked_core.gravity"]
          :iteration-slices ["SH-07" "SH-08"]})]
    (is (= ["SH-07" "SH-08"] (:iteration-slices plan)))
    (is (= ["SH-07" "SH-08"] (:affected-slices plan)))
    (is (seq (:namespaces plan)))
    (is (every? #{"SH-07" "SH-08"} (map :slice (:shards plan))))
    (is (empty? (:deferred-other-affected-paths plan)))))

(deftest iteration-changed-test-prefers-an-exact-discovered-namespace
  (let [changed
        "bootstrap/clojure/test/gravity/self_hosting/sh08_primitive_type_test.clj"
        plan
        (planner/build-plan
         {:changed-paths [changed]
          :iteration-slices #{"SH-07"}})
        sh08-namespaces
        (filter #(= "SH-08" (:slice %)) (:shards plan))]
    (is (= ["SH-07" "SH-08"] (:affected-slices plan)))
    (is (= ['gravity.self-hosting.sh08-primitive-type-test]
           (mapv :namespace sh08-namespaces)))
    (is (empty? (:deferred-other-affected-paths plan)))))

(deftest iteration-unselected-leaf-paths-are-deferred
  (let [fixture
        "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/new.gravity"
        module
        "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
        plan
        (planner/build-plan
         {:changed-paths [module fixture]
          :iteration-slices #{"SH-07"}})]
    (is (= ["SH-07"] (:affected-slices plan)))
    (is (= [fixture module] (:deferred-other-affected-paths plan)))
    (is (= [fixture module] (:deferred-paths plan)))))

(deftest iteration-invalid-and-missing-slices-fail-closed
  (doseq [[request expected-id expected-slices]
          [[{:iteration-slices #{}} "SH01-IMPACT-ITERATION-SLICE" []]
           [{:iteration-slices #{"SH-99"}} "SH01-IMPACT-SLICE" ["SH-99"]]
           [{:iteration-slices #{"SH-07"}
             :changed-paths
             ["bootstrap/clojure/fixtures/self-hosting/sh-99/accepted/new.gravity"]}
            "SH01-IMPACT-SLICE" ["SH-99"]]]]
    (let [exception
          (try
            (planner/build-plan request)
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= expected-id (:id (ex-data exception))))
      (is (= expected-slices (:slices (ex-data exception)))))))

(deftest iteration-unowned-paths-fail-closed
  (let [exception
        (try
          (planner/build-plan
           {:changed-paths
            ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
            :iteration-slices #{"SH-07"}})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-IMPACT-UNOWNED" (:id (ex-data exception))))
    (is (= ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]
           (:paths (ex-data exception))))))

(deftest iteration-cli-requires-changed-and-repeatable-slice-values
  (let [parse (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                          'parse-arguments)]
    (with-redefs [planner/changed-paths (constantly [])]
      (let [{:keys [plan-only? request]}
            (parse ["--changed"
                    "--iteration-slice" "SH-08"
                    "--iteration-slice" "SH-07"
                    "--plan"])]
        (is plan-only?)
        (is (= #{"SH-07" "SH-08"}
               (:iteration-slices request))))
      (doseq [[arguments expected-id]
              [[["--changed" "--iteration-slice"]
                "SH01-IMPACT-ITERATION-SLICE"]
               [["--iteration-slice" "SH-07"]
                "SH01-IMPACT-USAGE"]]]
        (let [exception
              (try
                (parse arguments)
                nil
                (catch clojure.lang.ExceptionInfo exception exception))]
          (is (= expected-id (:id (ex-data exception))) arguments))))))

(deftest authoritative-changed-plan-remains-full-and-unchanged
  (let [plan
        (planner/build-plan
         {:changed-paths ["bootstrap/clojure/src/gravity/bootstrap.clj"]
          :expand-dependants? true})]
    (is (= 30 (count (:direct-slices plan))))
    (is (= 30 (count (:affected-slices plan))))
    (is (nil? (:authority plan)))
    (is (nil? (:full-gate-deferred? plan)))
    (is (nil? (:iteration? plan)))))

(deftest exact-namespace-plan-does-not-expand-siblings-or-dependants
  (let [plan
        (planner/build-namespace-plan
         ['gravity.self-hosting.sh07-b48-call-arity-test
          'gravity.self-hosting.sh08-function-call-type-test])]
    (is (= :non-authoritative (:authority plan)))
    (is (false? (:authoritative? plan)))
    (is (= :exact-namespaces (:selection-mode plan)))
    (is (= ["SH-07" "SH-08"] (:affected-slices plan)))
    (is (= ['gravity.self-hosting.sh07-b48-call-arity-test
            'gravity.self-hosting.sh08-function-call-type-test]
           (:namespaces plan)))
    (is (= [:memory-heavy :normal]
           (mapv :resource-class (:shards plan)))))
  (doseq [[requested expected-id]
          [[[] "SH01-IMPACT-NAMESPACE-EMPTY"]
           [['gravity.self-hosting.sh07-b48-call-arity-test
             'gravity.self-hosting.sh07-b48-call-arity-test]
            "SH01-IMPACT-NAMESPACE-DUPLICATE"]
           [['gravity.self-hosting.absent-test]
            "SH01-IMPACT-NAMESPACE"]]]
    (let [exception
          (try
            (planner/build-namespace-plan requested)
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= expected-id (:id (ex-data exception)))))))

(deftest exact-namespace-plan-rejects-invalid-catalog-slices
  (let [catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)]
    (doseq [[namespace slice]
            [['gravity.self-hosting.sh30-future-test "SH-30"]
             ['gravity.self-hosting.nested-test nil]]]
      (with-redefs-fn
        {catalog-var (constantly [{:namespace namespace :slice slice}])}
        (fn []
          (let [exception
                (try
                  (planner/build-namespace-plan [namespace])
                  nil
                  (catch clojure.lang.ExceptionInfo exception exception))]
            (is (= (if (= "SH-30" slice)
                     "SH01-IMPACT-NAMESPACE-SLICE"
                     "SH01-IMPACT-CATALOG")
                   (:id (ex-data exception))))
            (when (= "SH-30" slice)
              (is (= [namespace] (:namespaces (ex-data exception)))))))))))

(deftest catalog-validation-rejects-nil-and-out-of-range-entries
  (doseq [catalog [nil
                   [{:namespace 'gravity.self-hosting.sh30-future-test
                     :slice "SH-30"}]
                   [{:namespace 'gravity.self-hosting.nested-test
                     :slice nil}]]
          mode [:normal :iteration :exact]]
    (let [context (planner/planning-context {:catalog catalog})
          exception
          (try
            (case mode
              :normal (planner/build-plan {:context context
                                           :direct-slices #{"SH-01"}})
              :iteration (planner/build-plan {:context context
                                              :iteration-slices #{"SH-01"}})
              :exact (planner/build-namespace-plan
                      ['gravity.self-hosting.sh01-ownership-test]
                      context))
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-IMPACT-CATALOG" (:id (ex-data exception)))
          [mode catalog]))))

(deftest catalog-validation-rejects-duplicate-namespace-identities
  (let [catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)
        duplicate 'gravity.self-hosting.sh01-ownership-test
        duplicate-entry {:namespace duplicate :slice "SH-01"}
        catalog
        [{:namespace 'gravity.self-hosting.sh02-envelope-test
          :slice "SH-02"}
         duplicate-entry
         duplicate-entry]]
    (with-redefs-fn
      {catalog-var (constantly catalog)}
      (fn []
        (doseq [request [{:direct-slices #{"SH-01"}}
                         {:iteration-slices #{"SH-01"}}]]
          (let [exception
                (try
                  (planner/build-plan request)
                  nil
                  (catch clojure.lang.ExceptionInfo exception exception))
                data (ex-data exception)]
            (is (= "SH01-IMPACT-CATALOG" (:id data)) request)
            (is (= [duplicate] (:duplicate-namespaces data)) request)
            (is (= [{:namespace duplicate
                     :count 2
                     :entries [duplicate-entry duplicate-entry]}]
                   (:duplicate-entries data))
                request)
            (is (= :namespace-duplicate
                   (:reason (last (:entries data))))
                request)))))))

(deftest reviewed-non-slice-infrastructure-is-discoverable-but-never-a-slice
  (let [reviewed-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'reviewed-non-slice-namespaces)
        reviewed @reviewed-var
        catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)
        general (set (runner/dedicated-test-namespaces))
        catalog (@catalog-var)]
    (testing "the reviewed exclusions are explicit and discoverable"
      (is (= 22 (count reviewed)))
      (is (contains? reviewed
                     'gravity.self-hosting.a1-canonical-schema-test))
      (is (contains? reviewed
                     'gravity.self-hosting.p15-public-native-admission-test))
      (is (contains? reviewed
                     'gravity.self-hosting.w5-c16-incremental-executor-test))
      (is (every? #(contains? general %) reviewed))
      (is (= (- (count general) (count reviewed))
             (count catalog))))
    (testing "the planner catalog contains only bounded SH slices"
      (is (not-any? reviewed (map :namespace catalog)))
      (is (every? #(re-matches #"SH-\d{2}" (:slice %)) catalog))
      (is (some #{'gravity.self-hosting.sh07-checked-core-test}
                (map :namespace catalog))))))

(deftest fixed-stage-catalog-drift-removal-and-unknowns-fail-closed
  (let [fixed-stage
        ['gravity.self-hosting.stage3-fragment-size-preflight-test
         'gravity.self-hosting.stage3-verification-runner-test]
        ownership
        'gravity.self-hosting.sh01-ownership-test
        catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)
        runner-var
        (ns-resolve 'gravity.self-hosting-test-runner
                    'dedicated-test-namespaces)
        exception-data
        (fn [discovered]
          (with-redefs-fn
            {runner-var (constantly discovered)}
            (fn []
              (try
                (@catalog-var)
                nil
                (catch clojure.lang.ExceptionInfo exception
                  (ex-data exception))))))]
    (testing "removing a reviewed infrastructure namespace is visible"
      (let [data (exception-data [ownership (first fixed-stage)])]
        (is (= "SH01-IMPACT-CATALOG" (:id data)))
        (is (= ['gravity.self-hosting.stage3-verification-runner-test]
               (:missing-fixed-stage-infrastructure data)))
        (is (empty? (:unknown-non-slice-namespaces data)))))
    (testing "renaming or adding an unreviewed non-slice namespace is not accepted"
      (let [unknown 'gravity.self-hosting.stage4-verification-runner-test
            data (exception-data (conj (vec fixed-stage) ownership unknown))]
        (is (= "SH01-IMPACT-CATALOG" (:id data)))
        (is (= [unknown] (:unknown-non-slice-namespaces data)))
        (is (empty? (:missing-fixed-stage-infrastructure data)))))
    (testing "duplicate handling remains a collision, not a slice assignment"
      (let [data
            (exception-data
             (conj (vec fixed-stage) ownership ownership))]
        (is (= "SH01-TEST-NAMESPACE-COLLISION" (:id data)))))))

(deftest planner-context-discovers-catalog-once
  (let [ownership-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'ownership-record)
        dependencies-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'backlog-dependencies)
        catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)
        ownership-scans (atom 0)
        dependency-scans (atom 0)
        catalog-scans (atom 0)
        ownership-loader @ownership-var
        dependency-loader @dependencies-var]
    (with-redefs-fn
      {ownership-var
       (fn []
         (swap! ownership-scans inc)
         (ownership-loader))
       dependencies-var
       (fn []
         (swap! dependency-scans inc)
         (dependency-loader))
       catalog-var
       (fn []
         (swap! catalog-scans inc)
         [{:namespace 'gravity.self-hosting.sh01-ownership-test
           :slice "SH-01"}])}
      (fn []
        (let [context (planner/planning-context)]
          (planner/build-plan {:context context
                               :direct-slices #{"SH-01"}})
          (planner/build-plan {:context context
                               :direct-slices #{"SH-01"}})
          (is (= 1 @ownership-scans))
          (is (= 1 @dependency-scans))
          (is (= 1 @catalog-scans)))))))

(deftest invalid-and-unowned-inputs-fail-before-catalog-discovery
  (let [catalog-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'test-catalog)
        scans (atom 0)]
    (with-redefs-fn
      {catalog-var
       (fn []
         (swap! scans inc)
         [])}
      (fn []
        (doseq [[request expected-id]
                [[{:direct-slices #{"SH-30"}} "SH01-IMPACT-SLICE"]
                 [{:changed-paths
                   ["bootstrap/clojure/test/gravity/self_hosting/unowned_test.clj"]}
                  "SH01-IMPACT-UNOWNED"]
                 [{:iteration-slices #{"SH-30"}}
                  "SH01-IMPACT-SLICE"]]]
          (let [exception
                (try
                  (planner/build-plan request)
                  nil
                  (catch clojure.lang.ExceptionInfo exception exception))]
            (is (= expected-id (:id (ex-data exception))) request)))
        (is (zero? @scans))))))

(deftest unresolved-present-dedicated-test-fails-and-deleted-one-selects-slice
  (let [catalog [{:namespace 'gravity.self-hosting.sh01-ownership-test
                  :slice "SH-01"}]
        context (planner/planning-context {:catalog catalog})
        present
        "bootstrap/clojure/test/gravity/self_hosting/sh01_impact_test_planner_test.clj"
        deleted
        "bootstrap/clojure/test/gravity/self_hosting/sh01_deleted_test.clj"
        exception
        (try
          (planner/build-plan {:context context
                               :changed-paths [present]})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))
        plan (planner/build-plan {:context context
                                  :changed-paths [deleted]})]
    (is (= "SH01-IMPACT-NAMESPACE" (:id (ex-data exception))))
    (is (= [present] (:paths (ex-data exception))))
    (is (= ["SH-01"] (:direct-slices plan)))
    (is (= ["SH-01"] (:affected-slices plan)))))

(deftest base-aware-discovery-includes-clean-branch-commit-and-working-union
  (let [attributes (make-array FileAttribute 0)
        repository (Files/createTempDirectory
                    "gravity-impact-base-" attributes)
        committed-path
        "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_adapter_test.clj"]
    (try
      (run-process! repository "git" "init" "-b" "main")
      (run-process! repository "git" "config" "user.name" "Gravity Test")
      (run-process! repository "git" "config" "user.email"
                    "gravity-test@example.invalid")
      (spit (.toFile (.resolve repository "README.md")) "base\n")
      (run-process! repository "git" "add" "README.md")
      (run-process! repository "git" "commit" "-m" "base")
      (let [base-commit (run-process! repository "git" "rev-parse" "HEAD")]
        (run-process! repository "git" "switch" "-c" "topic")
        (let [file (.resolve repository committed-path)]
          (Files/createDirectories (.getParent file)
                                   (make-array FileAttribute 0))
          (spit (.toFile file) "(ns gravity.self-hosting.sh06-resolution-adapter-test)\n"))
        (run-process! repository "git" "add" committed-path)
        (run-process! repository "git" "commit" "-m" "topic")
        (is (str/blank? (run-process! repository "git" "status"
                                     "--short")))
        (let [discovery (planner/change-discovery "main" repository)]
          (is (= base-commit (:base-commit discovery)))
          (is (= base-commit (:merge-base discovery)))
          (is (= [committed-path] (:committed-paths discovery)))
          (is (empty? (:tracked-working-paths discovery)))
          (is (empty? (:untracked-paths discovery)))
          (is (= [committed-path] (:changed-paths discovery)))
          (is (= ['gravity.self-hosting.sh06-resolution-adapter-test]
                 (:namespaces
                  (planner/build-plan
                   {:changed-paths (:changed-paths discovery)
                    :expand-dependants? true})))))
        (spit (.toFile (.resolve repository "README.md")) "working\n")
        (spit (.toFile (.resolve repository "notes.txt")) "untracked\n")
        (let [discovery (planner/change-discovery "main" repository)]
          (is (= ["README.md" committed-path "notes.txt"]
                 (:changed-paths discovery)))
          (is (= ["README.md"] (:tracked-working-paths discovery)))
          (is (= ["notes.txt"] (:untracked-paths discovery)))))
      (finally
        (delete-tree! repository)))))

(deftest base-aware-discovery-rejects-missing-and-malformed-base-refs
  (doseq [base-ref ["" "\u0000" "missing-gravity-base-ref"]]
    (let [exception
          (try
            (planner/change-discovery base-ref)
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-IMPACT-BASE" (:id (ex-data exception))) base-ref))))

(deftest parse-plan-only-is-always-boolean
  (let [parse (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                          'parse-arguments)]
    (with-redefs [planner/changed-paths (constantly [])]
      (is (true? (:plan-only? (parse ["--slice" "SH-01" "--plan"]))))
      (is (false? (:plan-only? (parse ["--slice" "SH-01"])))))))
