(ns gravity.self-hosting.sh01-impact-test-planner-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-component-test-dependencies
             :as component-dependencies]
            [gravity.self-hosting.sh01-impact-test-planner :as planner]
            [gravity.self-hosting.sh01-development-loop-wiring :as wiring]
            [gravity.self-hosting.sh01-development-test-cache :as cache]
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

(deftest sh07-b8-exact-route-is-owned-and-var-scoped
  (let [route (planner/exact-test-var-route "b8-regression")
        plan (planner/build-test-var-plan "b8-regression")
        route-path (:path route)
        error-data
        (fn [request]
          (try
            (planner/build-test-var-plan "b8-regression" request)
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))))]
    (is (= ["b8-regression"]
           (planner/exact-test-var-route-names)))
    (is (= "SH-07" (:slice route)))
    (is (= :sh-core (:owner route)))
    (is (= :sh-core
           (get-in (planner/planning-context)
                   [:ownership :slice-owners "SH-07" :leaf-owner])))
    (is (= :sh-core (:route-owner plan)))
    (is (= route-path (:route-path plan)))
    (is (= :exact-test-vars (:selection-mode plan)))
    (is (= 2 (:test-var-count plan)))
    (is (= (:test-vars route) (:test-vars plan)))
    (is (= ['gravity.self-hosting.sh07-try-catch-test]
           (:namespaces plan)))
    (is (= [route-path]
           (:changed-paths plan)))
    (is (= "SH01-IMPACT-ROUTE"
           (:id (try
                  (planner/build-test-var-plan "unknown")
                  nil
                  (catch clojure.lang.ExceptionInfo error
                    (ex-data error))))))
    (is (= "SH01-IMPACT-ROUTE-PATH"
           (:id (error-data {:changed-paths
                             [route-path
                              "bootstrap/clojure/test/gravity/self_hosting/sh07_throw_test.clj"]}))))
    (is (= "SH01-IMPACT-ROUTE-SELECTION"
           (:id (error-data {:test-vars (:test-vars route)}))))))

(deftest sh07-b8-route-owner-and-dependency-identities-fail-closed
  (let [route (planner/exact-test-var-route "b8-regression")
        context (planner/planning-context)
        owner-path [:ownership :slice-owners "SH-07"]
        error-data
        (fn [request]
          (try
            (planner/build-test-var-plan "b8-regression" request)
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))))
        owner-validator
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'validate-exact-test-var-route-owner!)
        route-options
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'exact-test-var-route-options)
        owner-error
        (fn [candidate-route candidate-context]
          (try
            (owner-validator candidate-route candidate-context)
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))))]
    (testing "the route owner matches the live leaf owner"
      (is (= route
             (owner-validator route context)))
      (is (= :sh-core
             (get-in (:ownership context)
                     [:slice-owners "SH-07" :leaf-owner]))))
    (testing "owner drift, missing, malformed, and extra owner records reject"
      (doseq [[label candidate expected-reason]
              [["drifted route owner" (assoc route :owner :other-owner)
                :owner-drift]
               ["missing live owner"
                route
                :slice-owner-missing]
               ["malformed live owner"
                route
                :slice-owner-malformed]
               ["extra live owner key"
                route
                :slice-owner-shape]]]
        (let [candidate-context
              (case label
                "missing live owner"
                (update-in context [:ownership :slice-owners] dissoc "SH-07")
                "malformed live owner"
                (assoc-in context (conj owner-path :leaf-owner) "sh-core")
                "extra live owner key"
                (assoc-in context (conj owner-path :extra-owner) :other-owner)
                context)
              data (owner-error candidate candidate-context)
              admission-data (when (= candidate route)
                               (error-data {:context candidate-context}))]
          (is (= "SH01-IMPACT-ROUTE-OWNER" (:id data)) label)
          (is (= expected-reason (:reason data)) label)
          (when admission-data
            (is (= "SH01-IMPACT-ROUTE-OWNER" (:id admission-data)) label)
            (is (= expected-reason (:reason admission-data)) label)))))
    (testing "route owner metadata is closed"
      (doseq [candidate [(dissoc route :owner)
                        (assoc route :owner "sh-core")
                        (assoc route :extra-owner :other-owner)]]
        (let [data
              (try
                (route-options candidate)
                nil
                (catch clojure.lang.ExceptionInfo error
                  (ex-data error)))]
          (is (= "SH01-IMPACT-ROUTE-CONTRACT" (:id data)) candidate))))
    (testing "the exact path/namespace dependency identity is required"
      (let [catalog (->> @(:catalog-delay context)
                         (mapv #(if (= (:namespace %) (:namespace route))
                                  (assoc % :namespace
                                         'gravity.self-hosting.sh07-other-test)
                                  %)))
            data (error-data {:context (assoc context
                                              :catalog-delay (delay catalog))})]
        (is (= "SH01-IMPACT-ROUTE-CONTRACT" (:id data)) data)))))

(deftest sh07-b8-route-projection-preserves-uep-selection-and-closure
  (let [manifest-route
        ((requiring-resolve
          'gravity.self-hosting.unified-execution-plan/route)
         "sh07-b8-regression")
        route (planner/exact-test-var-route "b8-regression")
        oracle
        {:slice "SH-07"
         :owner :sh-core
         :path "bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"
         :namespace 'gravity.self-hosting.sh07-try-catch-test
         :test-vars
         '[gravity.self-hosting.sh07-try-catch-test/sh07-b8-authenticated-input-substitution-fails-before-lowering
           gravity.self-hosting.sh07-try-catch-test/sh07-b8-public-proof-is-bounded-and-does-not-claim-later-facts]
         :selection-mode :exact-test-vars
         :resource-class :memory-heavy}
        namespace (get-in manifest-route [:selection :namespace])
        selectors (get-in manifest-route [:selection :selectors])
        closure (get-in manifest-route [:dependency-closure])]
    (is (= oracle route))
    (is (= (symbol namespace) (:namespace route)))
    (is (= (mapv #(symbol (str namespace "/" %)) selectors)
           (:test-vars route)))
    (is (= "bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"
           (:path route)))
    (is (= :memory-heavy (:resource-class route)))
    (is (true? (:complete? closure)))
    (is (contains? (set (:paths closure)) (:path route)))))

(deftest sh07-b8-route-owner-remains-reviewed-and-plan-rejects-drift
  (let [owner-var #'gravity.self-hosting.sh01-impact-test-planner/ownership-record
        route (with-redefs-fn
                {owner-var (fn [] {:slice-owners
                                   {"SH-07" {:leaf-owner :forged-owner
                                              :integration-owner :forged-owner}}})}
                #(planner/exact-test-var-route "b8-regression"))
        context (planner/planning-context)
        drifted-context
        (assoc-in context [:ownership :slice-owners "SH-07" :leaf-owner]
                  :forged-owner)
        drift-data
        (try
          (planner/build-test-var-plan "b8-regression"
                                       {:context drifted-context})
          nil
          (catch clojure.lang.ExceptionInfo error
            (ex-data error)))]
    (is (= :sh-core (:owner route)))
    (is (= "SH01-IMPACT-ROUTE-OWNER" (:id drift-data)))
    (is (= :owner-drift (:reason drift-data)))))

(deftest sh07-b8-route-projection-tamper-fails-closed
  (let [route-fn
        (requiring-resolve
         'gravity.self-hosting.unified-execution-plan/route)
        manifest-route (route-fn "sh07-b8-regression")
        route-symbol 'gravity.self-hosting.unified-execution-plan/route
        projection-error
        (fn [candidate]
          (try
            (with-redefs [clojure.core/requiring-resolve
                          (fn [symbol]
                            (if (= route-symbol symbol)
                              (fn [_] candidate)
                              (fn [& _] nil)))]
              (planner/exact-test-var-route "b8-regression"))
            nil
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))))]
    (doseq [[label candidate]
            [["missing canonical test path"
             (update-in manifest-route [:dependency-closure :paths]
                         #(vec (remove
                                #{"bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"}
                                %)))]
             ["non-vector closure paths"
              (assoc-in manifest-route [:dependency-closure :paths]
                        #{"bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj"})]
             ["non-string closure path"
              (update-in manifest-route [:dependency-closure :paths]
                         #(conj % :forged-path))]
             ["incomplete closure"
              (assoc-in manifest-route [:dependency-closure :complete?] false)]
             ["wrong resource class"
              (assoc manifest-route :resource-class :normal)]
             ["fresh execution mode"
              (assoc manifest-route :execution-mode :fresh-isolated-process)]
             ["authoritative ceiling"
              (assoc manifest-route :authority-ceiling :acceptance-only)]
             ["duplicate selector"
              (assoc-in manifest-route [:selection :selectors]
                        [(first (get-in manifest-route [:selection :selectors]))
                         (first (get-in manifest-route [:selection :selectors]))])]]]
      (is (= "SH01-IMPACT-ROUTE-CONTRACT"
             (:id (projection-error candidate)))
          label))))

(deftest sh07-b8-route-listing-keeps-uep-namespace-lazy
  (is (= "[\"b8-regression\"]|false"
         (run-process!
          (System/getProperty "user.dir")
          "clojure"
          "-Sdeps"
          "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}"
          "-M"
          "-e"
          "(require '[gravity.self-hosting.sh01-impact-test-planner :as planner]) (print (str (pr-str (planner/exact-test-var-route-names)) \"|\" (boolean (find-ns 'gravity.self-hosting.unified-execution-plan))))"))))

(deftest sh07-b8-route-manifest-resolution-failures-are-stable
  (let [route-symbol 'gravity.self-hosting.unified-execution-plan/route
        error-id
        (fn [thunk]
          (try
            (thunk)
            nil
            (catch clojure.lang.ExceptionInfo error
              (:id (ex-data error)))))]
    (testing "resolver failure"
      (is (= "SH01-IMPACT-ROUTE"
             (error-id
              #(with-redefs [clojure.core/requiring-resolve
                             (fn [_]
                               (throw (IllegalStateException. "resolver")))]
                 (planner/exact-test-var-route "b8-regression"))))))
    (testing "invocation failure"
      (is (= "SH01-IMPACT-ROUTE"
             (error-id
              #(with-redefs [clojure.core/requiring-resolve
                             (fn [symbol]
                               (if (= route-symbol symbol)
                                 (fn [_]
                                   (throw (IllegalStateException. "invoke")))
                                 (fn [& _] nil)))]
                 (planner/exact-test-var-route "b8-regression")))))))
    (testing "fatal resolver errors propagate"
      (is (= :propagated
             (try
               (with-redefs [clojure.core/requiring-resolve
                             (fn [_] (throw (Error. "fatal")))]
                 (planner/exact-test-var-route "b8-regression"))
               :returned
               (catch Error _
                 :propagated))))))

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
      (is (every?
           #(= {:deterministic? true
                :performance? false
                :proof? false
                :freshness-required? false}
               (:test-policy %))
           (:shards plan))
          path)
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

(deftest standalone-p15-tests-have-exact-fail-closed-incremental-plans
  (doseq [[path namespace test-id]
          [["bootstrap/clojure/test/gravity/self_hosting/p15_bounded_profile_test.clj"
            'gravity.self-hosting.p15-bounded-profile-test
            "p15-bounded-profile"]
           ["bootstrap/clojure/test/gravity/self_hosting/p15_proof_artifact_dag_test.clj"
            'gravity.self-hosting.p15-proof-artifact-dag-test
            "p15-proof-artifact-dag"]]]
    (let [path-plan (planner/build-plan {:changed-paths [path]})
          namespace-plan (planner/build-namespace-plan [namespace])
          classification (first (:classifications path-plan))]
      (is (= ["SH-01"] (:direct-slices path-plan)) path)
      (is (= [namespace] (:namespaces path-plan)) path)
      (is (= :exact-test-path
             (:development-selection classification)) path)
      (is (= :reviewed-component-test
             (:development-invalidation-reason classification)) path)
      (is (= test-id (:development-component-id classification)) path)
      (is (= [namespace] (:namespaces namespace-plan)) namespace)
      (is (= ["SH-01"] (:affected-slices namespace-plan)) namespace)
      (is (= [namespace]
             (:namespaces
              (runner/select-tests ["--namespace" (str namespace)])))
          namespace))))

(deftest semantic-standalone-catalog-entries-keep-exact-owners-and-freshness
  ;; Catalog and dispatch only: discovering/selecting an authenticated test
  ;; does not execute its provider or establish its semantic acceptance.
  (doseq [stem ["stage2_module_lexical_call" "stage2_module_literal_map_key"
                "stage2_module_loop_call" "stage2_module_map_call"
                "stage2_module_map_order" "stage2_module_transitive_call"
                "stage2_module_vector_call" "pass_execution_canonical_sort"
                "source_fragment_binding" "mir_scalar_admission"
                "mir_source_snapshot" "resolution_construction_inputs"
                "c6_parameter_reader_invocation" "canonical_keyword_scalar_path"
                "reader_staged_closure" "reader_token_spelling_reuse"]]
    (let [relative (str "bootstrap/clojure/test/gravity/self_hosting/" stem "_test.clj")
          namespace (symbol (str "gravity.self-hosting." (str/replace stem "_" "-") "-test"))
          slice (case stem
                  ("source_fragment_binding" "mir_source_snapshot") "SH-07"
                  "mir_scalar_admission" "SH-12"
                  "resolution_construction_inputs" "SH-06"
                  "c6_parameter_reader_invocation" "SH-07"
                  ("canonical_keyword_scalar_path" "reader_staged_closure"
                   "reader_token_spelling_reuse") "SH-03"
                  "SH-01")
          plan (planner/build-plan {:changed-paths [relative] :expand-dependants? true})
          namespace-plan (planner/build-namespace-plan [namespace])
          shard (first (:shards plan))]
      (is (= [namespace] (:namespaces plan)) relative)
      (is (= [namespace] (:namespaces namespace-plan)) relative)
      (is (= [slice] (:direct-slices plan)) relative)
      (is (= :exact-test-path
             (get-in plan [:classifications 0 :development-selection])) relative)
      (is (= (not= stem "pass_execution_canonical_sort")
             (get-in shard [:test-policy :freshness-required?])) relative)
      (is (false? (get-in shard [:test-policy :proof?])) relative)
      (is (nil? (:cache-closure shard)) relative)
      (is (false? (:authoritative? plan)) relative))))

(deftest reader-dispatch-standalone-registration-is-exact-and-fresh
  (let [path "bootstrap/clojure/test/gravity/reader/dispatch_test.clj"
        namespace 'gravity.reader.dispatch-test
        context (planner/planning-context)
        entry (get-in context [:standalone-test-index :by-namespace namespace])
        contract (component-dependencies/read-contract)
        missing (update contract :standalone-tests
                        #(vec (remove (fn [test] (= namespace (:namespace test))) %)))]
    (is (= {:namespace namespace :slice "SH-03" :owner :sh-reader}
           (get-in context [:ownership :standalone-test-owners path])))
    (is (= "reader-dispatch" (:test-id entry) (:jvm-group entry)))
    (is (= {:deterministic? true :performance? false :proof? false
            :freshness-required? true} (:test-policy entry)))
    (is (nil? (component-dependencies/reviewed-cache-closure namespace)))
    (is (= [namespace] (:namespaces (runner/select-tests ["--namespace" (str namespace)]))))
    (let [resources (vec (enumeration-seq (.getResources (.getContextClassLoader (Thread/currentThread))
                                                        "gravity/reader/dispatch_test.clj")))]
      (is (= 1 (count resources)))
      (is (= (.getCanonicalFile (io/file path))
             (.getCanonicalFile (io/file (.toURI (first resources)))))))
    (let [plan (planner/build-plan {:changed-paths [path]})]
      (is (= [namespace] (:namespaces plan)))
      (is (= ["SH-03"] (:direct-slices plan)))
      (let [shard (first (:shards plan))]
        (is (= "reader-dispatch" (:component-id shard)))
        (is (= :normal (:resource-class shard)))
        (is (true? (get-in shard [:test-policy :freshness-required?])))
        (is (nil? (:cache-closure shard))))
      (is (false? (:authoritative? plan))))
    (let [data (try (planner/planning-context
                    {:component-dependencies (component-dependencies/dependency-index missing)})
                   nil (catch clojure.lang.ExceptionInfo e (ex-data e)))]
      (is (= "SH01-STANDALONE-TEST-CONTRACT" (:id data)))
      (is (= [path] (:ownership-only-paths data)))
      (is (empty? (:dependency-only-paths data))))))

(deftest normal-reader-dispatch-registration-is-exact-and-fresh
  (let [path "bootstrap/clojure/test/gravity/reader/normal_dispatch_test.clj"
        namespace 'gravity.reader.normal-dispatch-test
        context (planner/planning-context)
        entry (get-in context [:standalone-test-index :by-namespace namespace])
        plan (planner/build-plan {:changed-paths [path]})]
    (is (= {:namespace namespace :slice "SH-03" :owner :sh-reader}
           (get-in context [:ownership :standalone-test-owners path])))
    (is (= "reader-normal-dispatch" (:test-id entry) (:jvm-group entry)))
    (is (= {:deterministic? true :performance? false :proof? false :freshness-required? true}
           (:test-policy entry)))
    (is (= [namespace] (:namespaces plan)))
    (is (= ["SH-03"] (:direct-slices plan)))
    (is (= :normal (get-in plan [:shards 0 :resource-class])))
    (is (nil? (component-dependencies/reviewed-cache-closure namespace)))
    (is (= [namespace] (:namespaces (runner/select-tests ["--namespace" (str namespace)]))))
    (let [resources (vec (enumeration-seq (.getResources (.getContextClassLoader (Thread/currentThread))
                                                        "gravity/reader/normal_dispatch_test.clj")))]
      (is (= 1 (count resources)))
      (is (= (.getCanonicalFile (io/file path))
             (.getCanonicalFile (io/file (.toURI (first resources)))))))))

(deftest reader-dispatch-fresh-policy-retains-full-snapshot-inputs
  ;; Pure snapshot/request construction in a temporary Git repository. No
  ;; broker, cache operation, test producer or compiler is launched.
  (let [root (.toRealPath (Files/createTempDirectory "gravity-reader-dispatch-inputs-"
                                                    (make-array FileAttribute 0))
                          (make-array java.nio.file.LinkOption 0))
        plan (planner/build-plan {:changed-paths ["bootstrap/clojure/test/gravity/reader/dispatch_test.clj"]})
        job (first (:shards plan))
        paths ["bootstrap/clojure/src/gravity/reader/dispatch.clj"
               "bootstrap/clojure/test/gravity/reader/dispatch_test.clj"
               "bootstrap/clojure/fixtures/accepted/reader-extension-input.txt"
               "contracts/stage0-incremental-test-dependencies.edn"
               "docs/self-hosting-slice-ownership.edn" "deps.edn"]
        request (fn [snapshot]
                  (wiring/cache-request
                   {:schema :gravity/sh01-development-loop-context-v1 :test-only? true
                    :working-directory (str root) :cache-directory root :timeout-ms 1000
                    :snapshot snapshot :runtime-tool-inputs [] :classpath-inputs []}
                   plan job {}))]
    (try
      (run-process! root "git" "init" "-q")
      (doseq [path paths]
        (let [target (.resolve root path)]
          (Files/createDirectories (.getParent target) (make-array FileAttribute 0))
          (Files/copy (.toPath (io/file path)) target (make-array java.nio.file.CopyOption 0))))
      (run-process! root "git" "add" ".")
      (run-process! root "git" "-c" "user.name=Gravity" "-c" "user.email=gravity@example.invalid"
                    "commit" "-qm" "actual Reader input bytes for snapshot fixture")
      (let [snapshot (wiring/repository-snapshot root)
            selected (request snapshot)]
        (is (= (:repository-identity snapshot) (:repository-identity selected)))
        (is (= ["repository/complete-production-closure-v1"]
               (mapv :path (get-in selected [:dependencies :production-inputs]))))
        (is (= ["repository/complete-transitive-closure-v1"]
               (mapv :path (get-in selected [:dependencies :transitive-production-inputs]))))
        (is (= ["repository/complete-fixture-contract-closure-v1"]
               (mapv :path (get-in selected [:dependencies :fixture-contract-inputs]))))
        (is (= :freshness-required-test
               (:reason (try (cache/cache-key selected) nil
                             (catch clojure.lang.ExceptionInfo e (ex-data e))))))
        (doseq [path paths]
          (let [file (.resolve root path) bytes (Files/readAllBytes file)
                stamp (Files/getLastModifiedTime file (make-array java.nio.file.LinkOption 0))
                changed (aclone bytes)]
            (try
              (aset-byte changed 0 (unchecked-byte (bit-xor 1 (aget bytes 0))))
              (Files/write file changed (make-array java.nio.file.OpenOption 0))
              (Files/setLastModifiedTime file stamp)
              (is (not= (:repository-identity selected)
                        (:repository-identity (request (wiring/repository-snapshot root)))) path)
              (finally (Files/write file bytes (make-array java.nio.file.OpenOption 0))
                       (Files/setLastModifiedTime file stamp)))))
        (doseq [path ["bootstrap/clojure/src/gravity/reader/dispatch.cljc"
                     "bootstrap/clojure/src/gravity/reader/dispatch__init.class"
                     "bootstrap/clojure/src/data_readers.clj"]]
          (let [file (.resolve root path)]
            (try
              (spit (str file) "snapshot-only shadow/resource fixture")
              (is (not= (:repository-identity selected)
                        (:repository-identity (request (wiring/repository-snapshot root)))) path)
              (finally (Files/delete file))))))
      (finally (delete-tree! root)))))

(deftest runtime-module-read-catalog-preserves-owner-and-requires-guarded-execution
  (doseq [[path namespace]
          [["bootstrap/clojure/test/gravity/self_hosting/runtime_module_reads_test.clj"
            'gravity.self-hosting.runtime-module-reads-test]
           ["bootstrap/clojure/test/gravity/self_hosting/module_census_test.clj"
            'gravity.self-hosting.module-census-test]]]
    (let [context (planner/planning-context)
          owner (get-in context [:ownership :standalone-test-owners path])
          entry (get-in context [:standalone-test-index :by-namespace namespace])]
      (is (= {:namespace namespace :slice "SH-19" :owner :sh-runtime} owner))
      (is (true? (get-in entry [:test-policy :freshness-required?])))
      (is (nil? (:cache-closure entry)))
      (doseq [request [#(planner/build-namespace-plan [namespace])
                       #(planner/build-plan {:changed-paths [path]})
                       #(planner/build-plan {:direct-slices ["SH-19"]})
                       #(planner/build-plan {:iteration-slices ["SH-19"]})]]
        (let [data (first (filter #(= namespace (:namespace %)) (:shards (request))))]
          (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
          (is (= :unsupported (:execution data)))
          (is (= namespace (:namespace data)))
          (is (= [:common-host-broker :host-exclusive :canonical-heavy-lock]
                 (:required-guards data))))))))

(deftest resolution-parent-mutation-is-discovered-but-needs-the-owner-wrapper
  (let [path "bootstrap/clojure/test/gravity/self_hosting/resolution_parent_mutation_test.clj"
        namespace 'gravity.self-hosting.resolution-parent-mutation-test
        context (planner/planning-context)
        entry (get-in context [:standalone-test-index :by-namespace namespace])]
    (is (= {:namespace namespace :slice "SH-06" :owner :sh-resolution}
           (get-in context [:ownership :standalone-test-owners path])))
    (is (true? (get-in entry [:test-policy :freshness-required?])))
    (is (nil? (:cache-closure entry)))
    (doseq [request [#(planner/build-namespace-plan [namespace])
                     #(planner/build-plan {:changed-paths [path]})
                     #(planner/build-plan {:direct-slices ["SH-06"]})
                     #(planner/build-plan {:iteration-slices ["SH-06"]})]]
      (let [data (first (filter #(= namespace (:namespace %)) (:shards (request))))]
        (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
        (is (= :unsupported (:execution data)))
        (is (= "SH-06" (:slice data)))
        (is (= :sh-resolution (:owner data)))
        (is (= [:common-host-broker :host-exclusive :canonical-heavy-lock]
               (:required-guards data)))))))

(deftest private-collection-chain-catalog-requires-the-owner-wrapper
  (let [path "bootstrap/clojure/test/gravity/self_hosting/private_collection_chain_test.clj"
        namespace 'gravity.self-hosting.private-collection-chain-test
        context (planner/planning-context)
        entry (get-in context [:standalone-test-index :by-namespace namespace])]
    (is (= {:namespace namespace :slice "SH-08" :owner :sh-types}
           (get-in context [:ownership :standalone-test-owners path])))
    (is (= {:deterministic? true :performance? false :proof? false
            :freshness-required? true} (:test-policy entry)))
    (is (nil? (:cache-closure entry)))
    (doseq [request [#(planner/build-namespace-plan [namespace])
                     #(planner/build-plan {:changed-paths [path]})
                     #(planner/build-plan {:direct-slices ["SH-08"]})
                     #(planner/build-plan {:iteration-slices ["SH-08"]})]]
      (let [data (first (filter #(= namespace (:namespace %)) (:shards (request))))]
        (is (= "SH01-IMPACT-EXTERNAL-WRAPPER" (:id data)))
        (is (= :unsupported (:execution data)))
        (is (= "SH-08" (:slice data)))
        (is (= :sh-types (:owner data)))
        (is (= [:common-host-broker :host-exclusive :canonical-heavy-lock]
               (:required-guards data)))))))

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

(deftest standalone-test-contract-rejects-stale-dependency-and-owner-pins
  (let [contract (component-dependencies/read-contract)
        malformed
        (assoc-in contract [:standalone-tests 0 :namespace]
                  'gravity.self-hosting.stale-test)
        dependency-exception
        (try
          (component-dependencies/validate-contract malformed)
          nil
          (catch clojure.lang.ExceptionInfo exception exception))
        ownership-var
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'ownership-record)
        ownership (@ownership-var)
        path "bootstrap/clojure/test/gravity/self_hosting/p15_bounded_profile_test.clj"
        stale-ownership (assoc-in ownership
                                  [:standalone-test-owners path :slice]
                                  "SH-02")
        ownership-exception
        (try
          (planner/planning-context {:ownership stale-ownership})
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-COMPONENT-DEPENDENCY-CONTRACT"
           (:id (ex-data dependency-exception))))
    (is (= "SH01-STANDALONE-TEST-CONTRACT"
           (:id (ex-data ownership-exception))))))

(deftest component-dependency-contract-requires-closed-test-policy
  (let [contract (component-dependencies/read-contract)
        malformed (update-in contract [:components 0 :tests 0]
                             dissoc :test-policy)
        exception
        (try
          (component-dependencies/validate-contract malformed)
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= "SH01-COMPONENT-DEPENDENCY-CONTRACT"
           (:id (ex-data exception))))))

(deftest explicit-cache-closures-are-reviewed-opt-in-and-fallback-safe
  (let [index (component-dependencies/dependency-index)
        entries (vals (:by-test-path index))
        by-namespace (into {} (map (juxt :namespace identity)) entries)]
    (is (= :gravity/stage0-incremental-test-dependencies-v2
           (:schema index)))
    (is (true? (get-in by-namespace
                       ['gravity.c2-artifact-identity-test :cache-closure-valid?])))
    (is (true? (get-in by-namespace
                       ['gravity.c11-mir-test :cache-closure-valid?])))
    (is (true? (get-in by-namespace
                       ['gravity.compiler-pass-manifest-test
                        :cache-closure-valid?])))
    (doseq [namespace
            ['gravity.bootstrap-compatibility.c11-test
             'gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test]]
      (is (nil? (get-in by-namespace [namespace :cache-closure])))
      (is (false? (get-in by-namespace [namespace :cache-closure-valid?])))
      (is (nil? (component-dependencies/reviewed-cache-closure index namespace))))
    (let [contract (component-dependencies/read-contract)
          malformed
          (assoc-in contract
                    [:components 0 :tests 0 :cache-closure :schema]
                    :gravity/unknown-closure)
          unknown-path
          (assoc-in contract
                    [:components 0 :tests 0 :cache-closure :inputs 0 :path]
                    "bootstrap/clojure/src/gravity/unowned.clj")]
      (is (false?
           (get-in (component-dependencies/dependency-index malformed)
                   [:by-test-path
                    "bootstrap/clojure/test/gravity/c11_mir_test.clj"
                    :cache-closure-valid?])))
      (is (false?
           (get-in (component-dependencies/dependency-index unknown-path)
                   [:by-test-path
                    "bootstrap/clojure/test/gravity/c11_mir_test.clj"
                    :cache-closure-valid?]))))))

(deftest cache-closure-accepts-more-than-former-input-cap
  (let [index (component-dependencies/dependency-index)
        component (get-in index [:by-component-id "c11-mir"])
        test-entry (get-in index
                           [:by-test-path
                            "bootstrap/clojure/test/gravity/c11_mir_test.clj"])
        base (component-dependencies/reviewed-cache-closure
              'gravity.c11-mir-test)
        existing (set (map :path (:inputs base)))
        fixture-inputs
        (->> (file-seq (io/file "bootstrap/clojure/fixtures"))
             (filter #(.isFile %))
             (map #(.getPath %))
             (map #(str/replace % "\\" "/"))
             sort
             (remove existing)
             (take 97)
             (mapv #(hash-map :role :fixture :path %)))
        closure (update base :inputs into fixture-inputs)
        valid? (deref (ns-resolve
                       'gravity.self-hosting.sh01-component-test-dependencies
                       'valid-cache-closure?))]
    (is (= 97 (count fixture-inputs)))
    (is (> (count (:inputs closure)) 96))
    (is (true? (valid? component test-entry closure)))))

(deftest exact-development-metadata-carries-only-validated-closure
  (let [index (component-dependencies/dependency-index)
        component (get-in index [:by-component-id "c11-mir"])
        metadata-builder
        (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                    'exact-development-job-metadata)
        metadata
        (metadata-builder
         (:ownership (planner/planning-context))
         [{:development-component-id "c11-mir"
           :development-tests (:tests component)}])]
    (is (map? (get-in metadata
                      ['gravity.c11-mir-test :cache-closure])))
    (is (true? (get-in metadata
                       ['gravity.c11-mir-test
                        :cache-closure-authorized?])))
    (is (nil? (get-in metadata
                      ['gravity.bootstrap-compatibility.c11-test
                       :cache-closure])))
    (is (not (true? (get-in metadata
                            ['gravity.bootstrap-compatibility.c11-test
                             :cache-closure-authorized?]))))))

(deftest nested-cache-inputs-retain-exact-component-ownership
  (let [owned? (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                           'closure-owned?)
        ownership (:ownership (planner/planning-context))
        input {:role :transitive
               :path "bootstrap/clojure/src/gravity/c11_mir/artifact.clj"}
        closure {:inputs [input]}]
    (is (true? (owned? ownership "c11-mir" closure)))
    (is (false? (owned? ownership "compiler-pass-manifest" closure)))
    (is (false? (owned? ownership "unknown-component" closure)))
    (is (false? (owned? (update ownership :module-owners dissoc
                                "bootstrap/clojure/src/gravity/c11_mir.clj")
                       "c11-mir" closure)))
    (is (false? (owned? ownership "c11-mir"
                       {:inputs [(assoc input :role :production)]})))
    (is (= :unowned
           (:classification
            (planner/classify-path ownership
                                   "bootstrap/clojure/src/gravity/c11_mir/unreviewed.clj"))))))

(deftest reader-identity-closure-is-exact-and-owner-bound
  (let [namespace 'gravity.c2-artifact-identity-test
        closure (component-dependencies/reviewed-cache-closure namespace)
        production (filter #(contains? #{:production :transitive} (:role %))
                           (:inputs closure))
        owned? (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner 'closure-owned?)
        ownership (:ownership (planner/planning-context))
        source "bootstrap/clojure/src/gravity/c2_artifact_identity.clj"
        plan (planner/build-plan {:changed-paths [source]})
        shard (first (:shards plan))]
    (is (= #{source "bootstrap/clojure/src/gravity/digest.clj"
             "bootstrap/clojure/src/gravity/c2_lexical_validation.clj"
             "bootstrap/clojure/src/gravity/c2_lexical_validation/operations.clj"
             "bootstrap/clojure/src/gravity/c2_lexical_validation/primitives.clj"
             "bootstrap/clojure/src/gravity/c2_lexical_validation/product.clj"
             "bootstrap/clojure/src/gravity/c2_artifact_identity/artifacts.clj"
             "bootstrap/clojure/src/gravity/c2_artifact_identity/canonical.clj"
             "bootstrap/clojure/src/gravity/c2_artifact_identity/incremental.clj"
             "bootstrap/clojure/src/gravity/c2_artifact_identity/operations.clj"
             "bootstrap/clojure/src/gravity/c2_artifact_identity/projections.clj"}
           (set (map :path production))))
    (is (= [namespace] (:namespaces plan)))
    (is (= closure (:cache-closure shard)))
    (is (true? (get-in shard [:test-policy :freshness-required?])))
    (is (false? (:cache-closure-authorized? shard)))
    (is (true? (owned? ownership "c2-artifact-identity" closure)))
    (is (false? (owned? ownership "c11-mir" closure)))
    (is (false? (owned? (update ownership :module-owners dissoc source)
                        "c2-artifact-identity" closure)))
    (is (false? (owned? (update ownership :module-owners dissoc
                                 "bootstrap/clojure/src/gravity/c2_lexical_validation.clj")
                        "c2-artifact-identity" closure)))
    (is (false? (owned? ownership "c2-artifact-identity"
                        {:inputs [{:role :transitive
                                   :path "bootstrap/clojure/src/gravity/c2_lexical_validation/unreviewed.clj"}]})))
    (is (= :unowned
           (:classification
            (planner/classify-path ownership
              "bootstrap/clojure/src/gravity/c2_artifact_identity/unreviewed.clj"))))))

(deftest reader-lexical-nested-inputs-retain-their-own-paired-owner
  ;; Inspect the owning record directly; this does not bypass catalog checks
  ;; in build-plan or change the full planner integration test above.
  (let [owned? (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner 'closure-owned?)
        ownership (edn/read-string (slurp "docs/self-hosting-slice-ownership.edn"))
        lexical "bootstrap/clojure/src/gravity/c2_lexical_validation.clj"
        identity "bootstrap/clojure/src/gravity/c2_artifact_identity.clj"]
    (doseq [name ["operations" "primitives" "product"]
            :let [input {:path (str "bootstrap/clojure/src/gravity/c2_lexical_validation/" name ".clj")
                         :role :transitive}
                  closure {:inputs [input]}]]
      (is (true? (owned? ownership "c2-artifact-identity" closure)))
      (is (false? (owned? ownership "c11-mir" closure)))
      (is (false? (owned? (update ownership :module-owners dissoc lexical)
                          "c2-artifact-identity" closure)))
      (is (false? (owned? (update ownership :module-owners dissoc identity)
                          "c2-artifact-identity" closure)))
      (is (false? (owned? ownership "c2-artifact-identity"
                          {:inputs [(assoc input :role :production)]}))))))

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

(deftest unknown-nested-production-input-is-never-treated-as-unrelated
  (doseq [relative ["bootstrap/clojure/src/gravity/pass_execution/unregistered.clj"
                    "bootstrap/clojure/src/gravity/tools/unregistered/body.clj"]]
    (let [failure (try
                    (planner/build-plan {:changed-paths [relative]})
                    nil
                    (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= "SH01-IMPACT-UNOWNED" (:id failure)) relative)
      (is (= [relative] (:paths failure)) relative)))
  (let [context (planner/planning-context
                 {:catalog [{:namespace 'gravity.self-hosting.sh01-ownership-test
                             :slice "SH-01"}]})
        plan (planner/build-plan {:context context :changed-paths ["README.md"]})]
    (is (= ["README.md"] (:ignored-paths plan)))
    (is (empty? (:namespaces plan)))))

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
      (is (= 30 (count reviewed)))
      (is (contains? reviewed
                     'gravity.self-hosting.a1-canonical-schema-test))
      (is (contains? reviewed
                     'gravity.self-hosting.p15-public-native-admission-test))
      (is (contains? reviewed
                     'gravity.self-hosting.p18-t00-orchestration-test))
      (is (contains? reviewed
                     'gravity.self-hosting.p18-t00-semantics-test))
      (is (contains? reviewed
                     'gravity.self-hosting.macro-operations-normalization-test))
      (is (contains? reviewed
                     'gravity.self-hosting.w5-c16-incremental-executor-test))
      (doseq [namespace ['gravity.self-hosting.artifact-parent-consumption-test
                         'gravity.self-hosting.authenticated-artifact-pipeline-test
                         'gravity.self-hosting.execution-lanes-test
                         'gravity.self-hosting.unified-execution-cli-test
                         'gravity.self-hosting.unified-execution-plan-test]]
        (is (contains? reviewed namespace)))
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
       (fn [_standalone-index]
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

(deftest rename-discovery-selects-removed-and-added-dependency-owners
  ;; Real Git discovery, followed by planning only. No compiler/provider test
  ;; execution is implied by the selected namespace assertions below.
  (doseq [phase [:committed :staged :working]]
    (let [attributes (make-array FileAttribute 0)
          repository (Files/createTempDirectory "gravity-impact-rename-" attributes)
          old-path "bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_adapter_test.clj"
          new-path "bootstrap/clojure/test/gravity/self_hosting/sh07_var_reference_test.clj"
          expected [old-path new-path]
          namespaces '[gravity.self-hosting.sh06-resolution-adapter-test
                       gravity.self-hosting.sh07-var-reference-test]
          context (planner/planning-context
                   {:catalog [{:namespace (first namespaces) :slice "SH-06"}
                              {:namespace (second namespaces) :slice "SH-07"}]})]
      (try
        (run-process! repository "git" "init" "-b" "main")
        (run-process! repository "git" "config" "user.name" "Gravity Test")
        (run-process! repository "git" "config" "user.email" "gravity-test@example.invalid")
        (run-process! repository "git" "config" "diff.renames" "true")
        (let [old (.resolve repository old-path)
              destination (.resolve repository new-path)]
          (Files/createDirectories (.getParent old) attributes)
          (spit (.toFile old) "; unchanged renamed dependency bytes\n")
          (run-process! repository "git" "add" old-path)
          (run-process! repository "git" "commit" "-m" "base dependency")
          (run-process! repository "git" "switch" "-c" "topic")
          (if (= :working phase)
            (Files/move old destination (make-array java.nio.file.CopyOption 0))
            (run-process! repository "git" "mv" old-path new-path))
          (when (= :committed phase)
            (run-process! repository "git" "commit" "-m" "rename dependency")))
        (let [discovery (planner/change-discovery "main" repository)
              plan (planner/build-plan {:context context
                                        :changed-paths (:changed-paths discovery)
                                        :expand-dependants? true})]
          (when-not (= :working phase)
            ;; Establish the negative control: ordinary rename detection loses
            ;; the removed dependency even though its owner needs validation.
            (is (= new-path
                   (if (= :committed phase)
                     (run-process! repository "git" "diff" "--name-only" "main" "HEAD")
                     (run-process! repository "git" "diff" "--name-only" "HEAD")))
                (str phase)))
          (is (= expected (:changed-paths discovery)) (str phase))
          (is (= namespaces (:namespaces plan)) (str phase))
          (is (= (if (= :committed phase) expected []) (:committed-paths discovery))
              (str phase))
          (is (= (case phase :committed [] :staged expected :working [old-path])
                 (:tracked-working-paths discovery)) (str phase))
          (is (= (if (= :working phase) [new-path] []) (:untracked-paths discovery))
              (str phase)))
        (finally (delete-tree! repository))))))

(deftest parse-plan-only-is-always-boolean
  (let [parse (ns-resolve 'gravity.self-hosting.sh01-impact-test-planner
                          'parse-arguments)]
    (with-redefs [planner/changed-paths (constantly [])]
      (is (true? (:plan-only? (parse ["--slice" "SH-01" "--plan"]))))
      (is (false? (:plan-only? (parse ["--slice" "SH-01"])))))))
