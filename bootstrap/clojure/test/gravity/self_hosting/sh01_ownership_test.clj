(ns gravity.self-hosting.sh01-ownership-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :as test :refer [deftest is testing]]
            [gravity.self-hosting-test-runner :as runner]))

(import '(java.nio.file Files LinkOption Paths)
        '(java.nio.file.attribute FileAttribute))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh01_ownership_test.clj")]
    (when-not resource
      (throw (ex-info "SH-01 test source is not on the classpath"
                      {:id "SH01-TEST-SOURCE"})))
    (loop [path (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? path)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH01-REPOSITORY-ROOT"}))

        (and (.isFile (.toFile (.resolve path "deps.edn")))
             (.isFile
              (.toFile
               (.resolve path "docs/self-hosting-slice-ownership.edn"))))
        path

        :else
        (recur (.getParent path))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (.toFile (.resolve @root relative)))

(defn- ownership-record
  []
  (edn/read-string
   {:readers *data-readers*}
   (slurp (path "docs/self-hosting-slice-ownership.edn"))))

(defn- gravity-module-paths
  []
  (let [source-root (path "bootstrap/gravity/src")
        root-path @root]
    (->> (file-seq source-root)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".gravity"))
         (map #(str (.relativize root-path (.toPath %))))
         (map #(str/replace % java.io.File/separator "/"))
         set)))

(defn- top-level-paths
  "Returns exact, direct-child paths for a Stage0 root.

  The Stage0 component contract intentionally excludes nested runner and
  self-hosting infrastructure.  Keep this helper exact so an unrelated file
  cannot silently enter the owner projection.
  "
  [relative-root suffix]
  (let [root-file (path relative-root)
        root-path @root]
    (->> (.listFiles root-file)
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) suffix))
         (map #(str (.relativize root-path (.toPath %))))
         (map #(str/replace % java.io.File/separator "/"))
         set)))

(defn- stage0-source-paths
  []
  (top-level-paths "bootstrap/clojure/src/gravity" ".clj"))

(defn- stage0-test-paths
  []
  (top-level-paths "bootstrap/clojure/test/gravity" "_test.clj"))

(defn- all-direct-stage0-test-paths
  []
  (top-level-paths "bootstrap/clojure/test/gravity" ".clj"))

(defn- stage0-component-id
  "Derives the reviewed Stage0 id from one exact source/test stem."
  [relative]
  (some-> (re-find
           #"^bootstrap/clojure/(?:src|test)/gravity/([^/]+?)(?:_test)?\.clj$"
           relative)
          second
          (str/replace "_test" "")
          (str/replace "_" "-")))

(defn- paired-stage0-path
  [relative]
  (when-let [id (stage0-component-id relative)]
    (let [stem (str/replace id "-" "_")]
      (cond
        (str/starts-with? relative "bootstrap/clojure/src/")
        (str "bootstrap/clojure/test/gravity/" stem "_test.clj")

        (str/starts-with? relative "bootstrap/clojure/test/")
        (str "bootstrap/clojure/src/gravity/" stem ".clj")))))

(defn- coordinator-support-paths
  "Return top-level Clojure coordinator reservations outside paired Stage0.

  Coordinator runners and P15 integration surfaces are intentionally not
  Stage0 components and must not be admitted by the leaf projection merely
  because they are direct children of a Stage0 source or test root.
  "
  [record]
  (->> (concat (get-in record [:coordinator-owned :central-routing])
               (get-in record
                       [:coordinator-owned
                        :p15-coordinator-integration-reservations])
               (get-in record
                       [:coordinator-owned
                        :coordinator-runner-integration-reservations]))
       (filter #(re-find
                 #"^bootstrap/clojure/(?:src|test)/gravity/[^/]+\.clj$"
                 %))
       set
       (#(disj %
               "bootstrap/clojure/src/gravity/bootstrap.clj"
               "bootstrap/clojure/test/gravity/bootstrap_test.clj"))))

(deftest ownership-record-is-total-and-unambiguous
  (let [record (ownership-record)
        module-owners (:module-owners record)
        actual-gravity-modules (gravity-module-paths)
        all-direct-stage0-sources (stage0-source-paths)
        all-direct-stage0-tests (all-direct-stage0-test-paths)
        auxiliary (:auxiliary-test-associations record)
        auxiliary-paths (into (set (keys auxiliary)) (map :source-path (vals auxiliary)))
        helper-paths (set (keys (:production-helper-associations record)))
        direct-helper-paths (set (filter #(re-matches #"bootstrap/clojure/src/gravity/[^/]+\.clj" %)
                                        helper-paths))
        support-paths (coordinator-support-paths record)
        support-test-paths
        (set (filter #(str/starts-with?
                      % "bootstrap/clojure/test/gravity/")
                     support-paths))
        actual-stage0-sources
        (set/difference all-direct-stage0-sources support-paths auxiliary-paths)
        actual-stage0-tests
        (set/difference all-direct-stage0-tests support-paths auxiliary-paths)
        actual-stage0-paths (set/union actual-stage0-sources actual-stage0-tests)
        declared-modules (set (keys module-owners))
        slice-ids (set (keys (:slice-owners record)))
        expected-slice-ids
        (set (map #(format "SH-%02d" %) (range 30)))
        declared-gravity-modules
        (set (filter #(str/starts-with? % "bootstrap/gravity/src/")
                     declared-modules))
        declared-stage0-paths
        (set/difference
         (set (filter #(re-find
                        #"^bootstrap/clojure/(?:src|test)/gravity/[^/]+$"
                        %)
                       declared-modules))
         support-paths)]
    (testing "the countable plan and all current Gravity modules have one owner"
      (is (= :gravity/self-hosting-slice-ownership-v1 (:schema record)))
      (is (= "SH-01" (:slice record)))
      (is (= :complete (:status record)))
      (is (= :master-coordinator (:integration-owner record)))
      (is (= expected-slice-ids slice-ids))
      (is (= actual-gravity-modules declared-gravity-modules))
      (is (= 70 (count actual-gravity-modules)))
      (is (= (set/difference (stage0-test-paths) support-paths auxiliary-paths)
             actual-stage0-tests))
      (is (= all-direct-stage0-tests
             (set/union actual-stage0-tests support-test-paths (set (keys auxiliary)))))
      (is (empty? (set/intersection actual-stage0-tests support-paths)))
      (is (= (+ 110 (count direct-helper-paths)) (count actual-stage0-paths)))
      (is (= actual-stage0-paths declared-stage0-paths))
      (is (= (+ 205 (count helper-paths)) (count module-owners)))
      (is (every? keyword? (vals module-owners))))
    (testing "coordinator support paths stay reserved outside paired Stage0"
      (is (= 16 (count support-paths)))
      (doseq [relative ["bootstrap/clojure/test/gravity/native_syntax_invocation_test.clj"
                        "bootstrap/clojure/test/gravity/reader_invocation_test.clj"]]
        (is (contains? support-paths relative))
        (is (some #{relative} (get-in record [:coordinator-owned :central-routing]))))
      (is (contains? support-paths
                     "bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj"))
      (is (every? #(or (not (contains? module-owners %))
                       (= :master-coordinator (get module-owners %)))
                  support-paths))
      (is (empty? (set/intersection actual-stage0-paths support-paths))))
    (testing "all exact module-owner paths exist and source/test owners are paired"
      (doseq [relative declared-modules]
        (is (.isFile (path relative)) relative))
      (doseq [source (set/difference actual-stage0-sources helper-paths)
              :let [test (paired-stage0-path source)]]
        (is (some? test) source)
        (is (= (get module-owners source)
               (get module-owners test))
            [source test]))
      (doseq [test actual-stage0-tests
              :let [source (paired-stage0-path test)]]
        (is (some? source) test)
        (is (= (get module-owners test)
               (get module-owners source))
            [test source])))
    (testing "coordinator-only integration modules are not assigned to a leaf"
      (doseq [module
              (get-in record [:coordinator-owned :integration-surfaces])
              :when (str/starts-with? module "bootstrap/gravity/src/")]
        (is (= :master-coordinator (get module-owners module)) module)))
    (testing "reserved Stage0 ids cover exactly the 55 source components"
      (let [reserved (:reserved-leaf-modules record)
            primary-sources (set/difference actual-stage0-sources helper-paths)
            expected-ids (set (map stage0-component-id primary-sources))]
        (is (= 55 (count reserved)))
        (is (= expected-ids (set (keys reserved))))
        (doseq [source primary-sources
                :let [id (stage0-component-id source)
                      owner (get module-owners source)]]
          (is (some? id) source)
          (is (= owner (get reserved id)) [id source]))))))

(def ^:private macro-helper-path "bootstrap/clojure/src/gravity/macro_run_storage.clj")
(def ^:private macro-helper-consumer
  "bootstrap/clojure/src/gravity/macro.clj")
(def ^:private macro-helper-test
  "bootstrap/clojure/test/gravity/self_hosting/macro_run_storage_test.clj")
(def ^:private reader-helper-path "bootstrap/clojure/src/gravity/reader.clj")
(def ^:private reader-helper-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00004-max-reader-form-depth.clj")
(def ^:private reader-helper-test
  "bootstrap/clojure/test/gravity/reader/artifact_test.clj")
(def ^:private syntax-helper-path "bootstrap/clojure/src/gravity/syntax.clj")
(def ^:private syntax-helper-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00856-compiler-c3-syntax-source-artifact.clj")
(def ^:private syntax-helper-test "bootstrap/clojure/test/gravity/syntax/pipeline_test.clj")
(def ^:private macro-stage-path "bootstrap/clojure/src/gravity/macro.clj")
(def ^:private macro-stage-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00857-sh05-macro-source-relative-path.clj")
(def ^:private macro-stage-test "bootstrap/clojure/test/gravity/macro/pipeline_test.clj")

(def ^:private resolution-stage-path "bootstrap/clojure/src/gravity/resolution.clj")
(def ^:private resolution-stage-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00873-sh06-resolution-dependency-edges-05.clj")
(def ^:private resolution-stage-test "bootstrap/clojure/test/gravity/resolution/pipeline_test.clj")

(def ^:private core-stage-path "bootstrap/clojure/src/gravity/checked_core.clj")
(def ^:private core-stage-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj")
(def ^:private core-stage-test "bootstrap/clojure/test/gravity/checked_core/pipeline_test.clj")
(def ^:private types-stage-path "bootstrap/clojure/src/gravity/types.clj")
(def ^:private types-stage-consumer "bootstrap/clojure/src/gravity/c7_type_checker/inference.clj")
(def ^:private types-stage-test "bootstrap/clojure/test/gravity/c7_type_checker_test.clj")
(def ^:private mir-stage-path "bootstrap/clojure/src/gravity/mir.clj")
(def ^:private mir-stage-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00739-p15-s23-c11-mir-validate-terminator.clj")
(def ^:private mir-stage-test "bootstrap/clojure/test/gravity/mir/pipeline_test.clj")
(def ^:private runtime-stage-path "bootstrap/clojure/src/gravity/runtime.clj")
(def ^:private runtime-stage-consumer
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00593-p15-s23-stage2-runtime-execute-function.clj")
(def ^:private runtime-stage-test "bootstrap/clojure/test/gravity/runtime/pipeline_test.clj")

(def ^:private optimization-stage-path "bootstrap/clojure/src/gravity/optimization.clj")
(def ^:private optimization-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-comparison-fold-reconstruction.clj")
(def ^:private optimization-stage-test "bootstrap/clojure/test/gravity/optimization/pipeline_test.clj")

(def ^:private llvm-data-stage-path "bootstrap/clojure/src/gravity/backend/llvm.clj")
(def ^:private llvm-data-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/005-lowering-reconstruction-and-builder.clj")
(def ^:private llvm-data-stage-test "bootstrap/clojure/test/gravity/backend/llvm/pipeline_test.clj")

(def ^:private c-emission-stage-path "bootstrap/clojure/src/gravity/backend/c.clj")
(def ^:private c-emission-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-023-p15-s23-b2-c17-operation-expression-through-p15-s23-b2-c17-discard-cast-records.clj")
(def ^:private c-emission-stage-test "bootstrap/clojure/test/gravity/backend/c/pipeline_test.clj")

(def ^:private wasm-codec-stage-path "bootstrap/clojure/src/gravity/backend/wasm.clj")
(def ^:private wasm-codec-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00756-p15-s23-b4-wasm-authority-token-002-p15-s23-b4-wasm-operation-bytes-through-p15-s23-b4-wasm-canonical-bounded-octet.clj")
(def ^:private wasm-codec-stage-test "bootstrap/clojure/test/gravity/backend/wasm/pipeline_test.clj")

(def ^:private jvm-source-stage-path "bootstrap/clojure/src/gravity/backend/jvm.clj")
(def ^:private jvm-source-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00773-jvm-backend-value-source.clj")
(def ^:private jvm-source-stage-test "bootstrap/clojure/test/gravity/backend/jvm/pipeline_test.clj")

(def ^:private js-source-stage-path "bootstrap/clojure/src/gravity/backend/js.clj")
(def ^:private js-source-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00767-c-backend-file-artifact.clj")
(def ^:private js-source-stage-test "bootstrap/clojure/test/gravity/backend/js/pipeline_test.clj")

(def ^:private lowering-stage-path "bootstrap/clojure/src/gravity/lowering.clj")
(def ^:private lowering-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-012-p15-s23-c14-policy-base-through-p15-s23-b2-c17-expected-source-content-hash.clj")
(def ^:private lowering-stage-test "bootstrap/clojure/test/gravity/lowering/pipeline_test.clj")

(def ^:private effects-stage-path "bootstrap/clojure/src/gravity/effects.clj")
(def ^:private effects-stage-consumer "bootstrap/clojure/src/gravity/c8_effect_checker/facts.clj")
(def ^:private effects-stage-test "bootstrap/clojure/test/gravity/c8_effect_checker_test.clj")

(def ^:private package-stage-path "bootstrap/clojure/src/gravity/package.clj")
(def ^:private package-stage-consumer "bootstrap/clojure/src/gravity/build/package_selection.clj")
(def ^:private package-policy-consumer "bootstrap/clojure/src/gravity/build/package_policy.clj")
(def ^:private package-stage-test "bootstrap/clojure/test/gravity/package/pipeline_test.clj")

(def ^:private ownership-stage-path "bootstrap/clojure/src/gravity/ownership.clj")
(def ^:private ownership-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00694-p15-s23-closed-core-persistent-aliasing-valid.clj")
(def ^:private ownership-stage-test "bootstrap/clojure/test/gravity/ownership/pipeline_test.clj")

(def ^:private tooling-stage-path "bootstrap/clojure/src/gravity/tooling.clj")
(def ^:private tooling-stage-consumer "bootstrap/clojure/src/gravity/bootstrap_impl/components/platform-phases/00349-p13-tooling-proof.clj")
(def ^:private tooling-stage-test "bootstrap/clojure/test/gravity/tooling/pipeline_test.clj")

(defn- production-helper-registration-valid? [record contract]
  (let [expected {:namespace 'gravity.macro-run-storage :owner :sh-macro :slice "SH-05"
                  :consumer-path macro-helper-consumer :test-path macro-helper-test}]
    (and (= {tooling-stage-path
             {:namespace 'gravity.tooling :owner :master-coordinator :slice "SH-24"
              :consumer-path tooling-stage-consumer :test-path tooling-stage-test}
             ownership-stage-path
             {:namespace 'gravity.ownership :owner :sh-ownership :slice "SH-10"
              :consumer-path ownership-stage-consumer :test-path ownership-stage-test}
             package-stage-path
             {:namespace 'gravity.package :owner :sh-package-build :slice "SH-23"
              :consumer-path package-stage-consumer :test-path package-stage-test}
             effects-stage-path
             {:namespace 'gravity.effects :owner :sh-effects :slice "SH-09"
              :consumer-path effects-stage-consumer :test-path effects-stage-test}
             lowering-stage-path
             {:namespace 'gravity.lowering :owner :master-coordinator :slice "SH-17"
              :consumer-path lowering-stage-consumer :test-path lowering-stage-test}
             js-source-stage-path
             {:namespace 'gravity.backend.js :owner :sh-target-js :slice "SH-00"
              :consumer-path js-source-stage-consumer :test-path js-source-stage-test}
             jvm-source-stage-path
             {:namespace 'gravity.backend.jvm :owner :sh-target-jvm :slice "SH-00"
              :consumer-path jvm-source-stage-consumer :test-path jvm-source-stage-test}
             wasm-codec-stage-path
             {:namespace 'gravity.backend.wasm :owner :sh-target-wasm :slice "SH-00"
              :consumer-path wasm-codec-stage-consumer :test-path wasm-codec-stage-test}
             c-emission-stage-path
             {:namespace 'gravity.backend.c :owner :sh-target-c :slice "SH-18"
              :consumer-path c-emission-stage-consumer :test-path c-emission-stage-test}
             llvm-data-stage-path
             {:namespace 'gravity.backend.llvm :owner :sh-target-llvm :slice "SH-18"
              :consumer-path llvm-data-stage-consumer :test-path llvm-data-stage-test}
             optimization-stage-path
             {:namespace 'gravity.optimization :owner :sh-optimization :slice "SH-16"
              :consumer-path optimization-stage-consumer :test-path optimization-stage-test}
             runtime-stage-path
             {:namespace 'gravity.runtime :owner :sh-runtime :slice "SH-19"
              :consumer-path runtime-stage-consumer :test-path runtime-stage-test}
             mir-stage-path
             {:namespace 'gravity.mir :owner :sh-mir :slice "SH-12"
              :consumer-path mir-stage-consumer :test-path mir-stage-test}
             types-stage-path
             {:namespace 'gravity.types :owner :sh-types :slice "SH-08"
              :consumer-path types-stage-consumer :test-path types-stage-test}
             core-stage-path
             {:namespace 'gravity.checked-core :owner :sh-core :slice "SH-07"
              :consumer-path core-stage-consumer :test-path core-stage-test}
             resolution-stage-path
             {:namespace 'gravity.resolution :owner :sh-resolution :slice "SH-06"
              :consumer-path resolution-stage-consumer :test-path resolution-stage-test}
             macro-helper-path expected
             reader-helper-path
             {:namespace 'gravity.reader :owner :sh-reader :slice "SH-03"
              :consumer-path reader-helper-consumer :test-path reader-helper-test}
             syntax-helper-path
             {:namespace 'gravity.syntax :owner :sh-syntax :slice "SH-04"
              :consumer-path syntax-helper-consumer :test-path syntax-helper-test}
             macro-stage-path
             {:namespace 'gravity.macro :owner :sh-macro :slice "SH-05"
              :consumer-path macro-stage-consumer :test-path macro-stage-test}}
            (:production-helper-associations record))
         (= :sh-macro (get-in record [:module-owners macro-helper-path]))
         (= {:namespace 'gravity.self-hosting.macro-run-storage-test
             :slice "SH-05" :owner :sh-macro}
            (get-in record [:standalone-test-owners macro-helper-test]))
         (= :sh-reader (get-in record [:module-owners reader-helper-path]))
         (= {:namespace 'gravity.reader.artifact-test :slice "SH-03" :owner :sh-reader}
            (get-in record [:standalone-test-owners reader-helper-test]))
         (= :sh-syntax (get-in record [:module-owners syntax-helper-path]))
         (= {:namespace 'gravity.syntax.pipeline-test :slice "SH-04" :owner :sh-syntax}
            (get-in record [:standalone-test-owners syntax-helper-test]))
         (= :sh-macro (get-in record [:module-owners macro-stage-path]))
         (= {:namespace 'gravity.macro.pipeline-test :slice "SH-05" :owner :sh-macro}
            (get-in record [:standalone-test-owners macro-stage-test]))
         (= :sh-resolution (get-in record [:module-owners resolution-stage-path]))
         (= {:namespace 'gravity.resolution.pipeline-test :slice "SH-06" :owner :sh-resolution}
            (get-in record [:standalone-test-owners resolution-stage-test]))
         (= :sh-core (get-in record [:module-owners core-stage-path]))
         (= :sh-effects (get-in record [:module-owners effects-stage-path]))
         (= :sh-effects (get-in record [:module-owners effects-stage-test]))
         (= :sh-types (get-in record [:module-owners types-stage-path]))
         (= :sh-types (get-in record [:module-owners types-stage-test]))
         (= :master-coordinator (get-in record [:module-owners lowering-stage-path]))
         (= {:namespace 'gravity.lowering.pipeline-test :slice "SH-17" :owner :master-coordinator}
            (get-in record [:standalone-test-owners lowering-stage-test]))
         (= :sh-package-build (get-in record [:module-owners package-stage-path]))
         (= {:namespace 'gravity.package.pipeline-test :slice "SH-23" :owner :sh-package-build}
            (get-in record [:standalone-test-owners package-stage-test]))
         (= :sh-ownership (get-in record [:module-owners ownership-stage-path]))
         (= {:namespace 'gravity.ownership.pipeline-test :slice "SH-10" :owner :sh-ownership}
            (get-in record [:standalone-test-owners ownership-stage-test]))
         (= :master-coordinator (get-in record [:module-owners tooling-stage-path]))
         (= {:namespace 'gravity.tooling.pipeline-test :slice "SH-24" :owner :master-coordinator}
            (get-in record [:standalone-test-owners tooling-stage-test]))
         (= :sh-target-jvm (get-in record [:module-owners jvm-source-stage-path]))
         (= {:namespace 'gravity.backend.jvm.pipeline-test :slice "SH-00" :owner :sh-target-jvm}
            (get-in record [:standalone-test-owners jvm-source-stage-test]))
         (= :sh-target-llvm (get-in record [:module-owners llvm-data-stage-path]))
         (= {:namespace 'gravity.backend.llvm.pipeline-test :slice "SH-18" :owner :sh-target-llvm}
            (get-in record [:standalone-test-owners llvm-data-stage-test]))
         (= :sh-target-js (get-in record [:module-owners js-source-stage-path]))
         (= {:namespace 'gravity.backend.js.pipeline-test :slice "SH-00" :owner :sh-target-js}
            (get-in record [:standalone-test-owners js-source-stage-test]))
         (= :sh-target-c (get-in record [:module-owners c-emission-stage-path]))
         (= {:namespace 'gravity.backend.c.pipeline-test :slice "SH-18" :owner :sh-target-c}
            (get-in record [:standalone-test-owners c-emission-stage-test]))
         (= :sh-target-wasm (get-in record [:module-owners wasm-codec-stage-path]))
         (= {:namespace 'gravity.backend.wasm.pipeline-test :slice "SH-00" :owner :sh-target-wasm}
            (get-in record [:standalone-test-owners wasm-codec-stage-test]))
         (= :sh-optimization (get-in record [:module-owners optimization-stage-path]))
         (= {:namespace 'gravity.optimization.pipeline-test :slice "SH-16" :owner :sh-optimization}
            (get-in record [:standalone-test-owners optimization-stage-test]))
         (= :sh-mir (get-in record [:module-owners mir-stage-path]))
         (= :sh-runtime (get-in record [:module-owners runtime-stage-path]))
         (= {:namespace 'gravity.runtime.pipeline-test :slice "SH-19" :owner :sh-runtime}
            (get-in record [:standalone-test-owners runtime-stage-test]))
         (= {:namespace 'gravity.mir.pipeline-test :slice "SH-12" :owner :sh-mir}
            (get-in record [:standalone-test-owners mir-stage-test]))
         (= {:namespace 'gravity.checked-core.pipeline-test :slice "SH-07" :owner :sh-core}
            (get-in record [:standalone-test-owners core-stage-test]))
         (= [{"id" "macro-run-storage" "kind" "production-verification-helper" "owner" "sh-macro"
              "source" {"path" macro-helper-path "namespace" "gravity.macro-run-storage"}
              "consumer" {"path" macro-helper-consumer "namespace" "gravity.macro" "owner" "sh-macro"}
              "test" {"path" macro-helper-test "namespace" "gravity.self-hosting.macro-run-storage-test"
                      "registration" "standalone"}}
             {"id" "reader-artifact-assembly" "kind" "production-stage-helper" "owner" "sh-reader"
              "source" {"path" reader-helper-path "namespace" "gravity.reader"}
              "consumer" {"path" reader-helper-consumer "namespace" "gravity.bootstrap" "owner" "sh-reader"}
              "test" {"path" reader-helper-test "namespace" "gravity.reader.artifact-test"
                      "registration" "standalone"}}
             {"id" "syntax-pipeline" "kind" "production-stage-helper" "owner" "sh-syntax"
              "source" {"path" syntax-helper-path "namespace" "gravity.syntax"}
              "consumer" {"path" syntax-helper-consumer "namespace" "gravity.bootstrap" "owner" "sh-syntax"}
              "test" {"path" syntax-helper-test "namespace" "gravity.syntax.pipeline-test"
                      "registration" "standalone"}}
             {"id" "resolution-plan-runtime" "kind" "production-stage-helper" "owner" "sh-resolution"
              "source" {"path" resolution-stage-path "namespace" "gravity.resolution"}
              "consumer" {"path" resolution-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-resolution"}
              "test" {"path" resolution-stage-test "namespace" "gravity.resolution.pipeline-test"
                      "registration" "standalone"}}
             {"id" "macro-plan-runtime" "kind" "production-stage-helper" "owner" "sh-macro"
              "source" {"path" macro-stage-path "namespace" "gravity.macro"}
              "consumer" {"path" macro-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-macro"}
              "test" {"path" macro-stage-test "namespace" "gravity.macro.pipeline-test"
                      "registration" "standalone"}}
             {"id" "checked-core-plan-runtime" "kind" "production-stage-helper" "owner" "sh-core"
              "source" {"path" core-stage-path "namespace" "gravity.checked-core"}
              "consumer" {"path" core-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-core"}
              "test" {"path" core-stage-test "namespace" "gravity.checked-core.pipeline-test"
                      "registration" "standalone"}}
             {"id" "types-products" "kind" "production-stage-helper" "owner" "sh-types"
              "source" {"path" types-stage-path "namespace" "gravity.types"}
              "consumer" {"path" types-stage-consumer "namespace" "gravity.c7-type-checker.inference" "owner" "sh-types"}
              "test" {"path" types-stage-test "namespace" "gravity.c7-type-checker-test"
                      "registration" "bootstrap-free"}}
             {"id" "mir-control-flow" "kind" "production-stage-helper" "owner" "sh-mir"
              "source" {"path" mir-stage-path "namespace" "gravity.mir"}
              "consumer" {"path" mir-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-mir"}
              "test" {"path" mir-stage-test "namespace" "gravity.mir.pipeline-test"
                      "registration" "standalone"}}
             {"id" "runtime-invocation" "kind" "production-stage-helper" "owner" "sh-runtime"
              "source" {"path" runtime-stage-path "namespace" "gravity.runtime"}
              "consumer" {"path" runtime-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-runtime"}
              "test" {"path" runtime-stage-test "namespace" "gravity.runtime.pipeline-test"
                      "registration" "standalone"}}
             {"id" "optimization-reconstruction" "kind" "production-stage-helper" "owner" "sh-optimization"
              "source" {"path" optimization-stage-path "namespace" "gravity.optimization"}
              "consumer" {"path" optimization-stage-consumer "namespace" "gravity.bootstrap-c13-comparison" "owner" "sh-optimization"}
              "test" {"path" optimization-stage-test "namespace" "gravity.optimization.pipeline-test"
                      "registration" "standalone"}}
             {"id" "llvm-data" "kind" "production-stage-helper" "owner" "sh-target-llvm"
              "source" {"path" llvm-data-stage-path "namespace" "gravity.backend.llvm"}
              "consumer" {"path" llvm-data-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-target-llvm"}
              "test" {"path" llvm-data-stage-test "namespace" "gravity.backend.llvm.pipeline-test"
                      "registration" "standalone"}}
             {"id" "c-emission" "kind" "production-stage-helper" "owner" "sh-target-c"
              "source" {"path" c-emission-stage-path "namespace" "gravity.backend.c"}
              "consumer" {"path" c-emission-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-target-c"}
              "test" {"path" c-emission-stage-test "namespace" "gravity.backend.c.pipeline-test"
                      "registration" "standalone"}}
             {"id" "wasm-codec" "kind" "production-stage-helper" "owner" "sh-target-wasm"
              "source" {"path" wasm-codec-stage-path "namespace" "gravity.backend.wasm"}
              "consumer" {"path" wasm-codec-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-target-wasm"}
              "test" {"path" wasm-codec-stage-test "namespace" "gravity.backend.wasm.pipeline-test"
                      "registration" "standalone"}}
             {"id" "jvm-source" "kind" "production-stage-helper" "owner" "sh-target-jvm"
              "source" {"path" jvm-source-stage-path "namespace" "gravity.backend.jvm"}
              "consumer" {"path" jvm-source-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-target-jvm"}
              "test" {"path" jvm-source-stage-test "namespace" "gravity.backend.jvm.pipeline-test"
                      "registration" "standalone"}}
             {"id" "js-source" "kind" "production-stage-helper" "owner" "sh-target-js"
              "source" {"path" js-source-stage-path "namespace" "gravity.backend.js"}
              "consumer" {"path" js-source-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-target-js"}
              "test" {"path" js-source-stage-test "namespace" "gravity.backend.js.pipeline-test"
                      "registration" "standalone"}}
             {"id" "lowering-reconstruction" "kind" "production-stage-helper" "owner" "master-coordinator"
              "source" {"path" lowering-stage-path "namespace" "gravity.lowering"}
              "consumer" {"path" lowering-stage-consumer "namespace" "gravity.bootstrap" "owner" "master-coordinator"}
              "test" {"path" lowering-stage-test "namespace" "gravity.lowering.pipeline-test"
                      "registration" "standalone"}}
             {"id" "effects-graph-data" "kind" "production-stage-helper" "owner" "sh-effects"
              "source" {"path" effects-stage-path "namespace" "gravity.effects"}
              "consumer" {"path" effects-stage-consumer "namespace" "gravity.c8-effect-checker.facts" "owner" "sh-effects"}
              "test" {"path" effects-stage-test "namespace" "gravity.c8-effect-checker-test"
                      "registration" "bootstrap-free"}}
             {"id" "package-selection" "kind" "production-stage-helper" "owner" "sh-package-build"
              "source" {"path" package-stage-path "namespace" "gravity.package"}
              "consumer" {"path" package-stage-consumer "namespace" "gravity.build.package-selection" "owner" "sh-package-build"}
              "test" {"path" package-stage-test "namespace" "gravity.package.pipeline-test"
                      "registration" "standalone"}}
             {"id" "ownership-data" "kind" "production-stage-helper" "owner" "sh-ownership"
              "source" {"path" ownership-stage-path "namespace" "gravity.ownership"}
              "consumer" {"path" ownership-stage-consumer "namespace" "gravity.bootstrap" "owner" "sh-ownership"}
              "test" {"path" ownership-stage-test "namespace" "gravity.ownership.pipeline-test"
                      "registration" "standalone"}}
             {"id" "tooling-evidence" "kind" "production-stage-helper" "owner" "master-coordinator"
              "source" {"path" tooling-stage-path "namespace" "gravity.tooling"}
              "consumer" {"path" tooling-stage-consumer "namespace" "gravity.bootstrap" "owner" "master-coordinator"}
              "test" {"path" tooling-stage-test "namespace" "gravity.tooling.pipeline-test"
                      "registration" "standalone"}}]
            (get contract "production_helpers")))))

(deftest runtime-invocation-helper-has-an-explicit-consumer-and-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (first (keep-indexed #(when (= "runtime-invocation" (get %2 "id")) %1)
                                         (get contract "production_helpers")))]
    (is (production-helper-registration-valid? record contract))
    (is (integer? helper-index))
    (doseq [relative [runtime-stage-path runtime-stage-consumer runtime-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path runtime-stage-path)))]
        (is (= 'gravity.runtime (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc runtime-stage-path)
                 (assoc-in record [:module-owners runtime-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners runtime-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations runtime-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations runtime-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations runtime-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest mir-control-flow-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [mir-stage-path mir-stage-consumer mir-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path mir-stage-path)))]
        (is (= 'gravity.mir (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc mir-stage-path)
                 (assoc-in record [:module-owners mir-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners mir-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations mir-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations mir-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations mir-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 7 "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" 7 "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" 7 "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" 7 "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest types-products-helper-joins-existing-bootstrap-free-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [types-stage-path types-stage-consumer types-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path types-stage-path)))]
        (is (= 'gravity.types (second (read reader))))))
    (doseq [operation ["types/literal-type" "types/node-type" "types/type-fact"
                      "types/type-environment" "types/constraint-ledger" "types/function-table"]]
      (is (str/includes? (slurp (path types-stage-consumer)) operation)))
    (doseq [bad [(update record :module-owners dissoc types-stage-path)
                 (assoc-in record [:module-owners types-stage-path] :master-coordinator)
                 (assoc-in record [:module-owners types-stage-test] :sh-core)
                 (assoc-in record [:production-helper-associations types-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations types-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations types-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 6 "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" 6 "test" "registration"] "standalone")
                 (assoc-in contract ["production_helpers" 6 "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" 6 "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest checked-core-runtime-helper-joins-real-source-consumer-and-standalone-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [core-stage-path core-stage-consumer core-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path core-stage-path)))]
        (is (= 'gravity.checked-core (second (read reader))))))
    (doseq [operation ["gravity.checked-core/build-plan-binding!" "gravity.checked-core/execute!"]]
      (is (str/includes? (slurp (path core-stage-consumer)) operation)))
    (doseq [bad [(assoc-in record [:module-owners core-stage-path] :master-coordinator)
                 (assoc-in record [:production-helper-associations core-stage-path :namespace]
                           'gravity.forged)
                 (assoc-in record [:production-helper-associations core-stage-path :consumer-path]
                           "forged.clj")
                 (update record :standalone-test-owners dissoc core-stage-test)]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 5 "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" 5 "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" 5 "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" 5 "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest macro-runtime-helper-joins-real-stage-consumer-and-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [macro-stage-path macro-stage-consumer macro-stage-test]]
      (is (.isFile (path relative))))
    (is (str/includes? (slurp (path macro-stage-consumer)) "(clojure.core/require 'gravity.macro)"))
    (is (str/includes? (slurp (path macro-stage-consumer)) "gravity.macro/build-plan-binding!"))
    (doseq [bad [(assoc-in record [:module-owners macro-stage-path] :master-coordinator)
                 (assoc-in record [:production-helper-associations macro-stage-path :consumer-path] "forged.clj")
                 (update record :standalone-test-owners dissoc macro-stage-test)]]
      (is (not (production-helper-registration-valid? bad contract))))))

(deftest production-helper-joins-real-source-consumer-and-existing-standalone-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (is (.isFile (path macro-helper-path)))
    (is (.isFile (path macro-helper-test)))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path macro-helper-path)))]
        (is (= 'gravity.macro-run-storage (second (read reader)))))
      (with-open [reader (java.io.PushbackReader. (io/reader (path macro-helper-consumer)))]
        (let [consumer (read reader)]
          (is (= 'gravity.macro (second consumer)))
          (is (some #{'[gravity.macro-run-storage :as macro-run-storage]}
                    (rest (last consumer)))))))
    (is (str/includes? (slurp (path macro-helper-consumer)) "(macro-run-storage/exact? runs)"))
    (is (not (.exists (path "bootstrap/clojure/test/gravity/macro_run_storage_test.clj"))))
    (doseq [bad [(assoc-in record [:module-owners macro-helper-path] :master-coordinator)
                 (assoc-in record [:production-helper-associations macro-helper-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations macro-helper-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations macro-helper-path :test-path]
                           "bootstrap/clojure/test/gravity/macro_run_storage_test.clj")
                 (update record :standalone-test-owners dissoc macro-helper-test)
                 (assoc-in record [:production-helper-associations "unreviewed.clj"]
                           (get-in record [:production-helper-associations macro-helper-path]))]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 0 "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" 0 "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" 0 "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest reader-assembly-helper-joins-the-owned-stage-and-bootstrap-consumer
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [reader-helper-path reader-helper-consumer reader-helper-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path reader-helper-path)))]
        (is (= 'gravity.reader (second (read reader))))))
    (is (str/includes? (slurp (path reader-helper-consumer))
                       "(gravity.reader/read-source-artifact"))
    (doseq [bad [(assoc-in record [:module-owners reader-helper-path] :master-coordinator)
                 (assoc-in record [:production-helper-associations reader-helper-path :namespace]
                           'gravity.forged)
                 (assoc-in record [:production-helper-associations reader-helper-path :consumer-path]
                           "forged.clj")
                 (update record :standalone-test-owners dissoc reader-helper-test)]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 1 "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" 1 "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" 1 "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest syntax-stage-helper-joins-real-source-consumer-and-standalone-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))]
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [syntax-helper-path syntax-helper-consumer syntax-helper-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path syntax-helper-path)))]
        (is (= 'gravity.syntax (second (read reader))))))
    (is (str/includes? (slurp (path syntax-helper-consumer)) "(gravity.syntax/build"))
    (doseq [bad [(assoc-in record [:module-owners syntax-helper-path] :sh-reader)
                 (update record :standalone-test-owners dissoc syntax-helper-test)
                 (assoc-in record [:production-helper-associations syntax-helper-path :consumer-path]
                           "forged.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" 2 "owner"] "sh-reader")
                 (assoc-in contract ["production_helpers" 2 "consumer" "path"] "forged.clj")
                 (assoc-in contract ["production_helpers" 2 "test" "registration"] "bootstrap-free")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest auxiliary-helper-associations-are-explicit-and-owned
  (let [associations (:auxiliary-test-associations (ownership-record))
        expected {"c2_reader_scalar_spans" [:sh-reader "c2_reader_product_projection/scalar_spans.clj"]
                  "c3_literal_indexes" [:sh-syntax "c3_literal_projection/indexes.clj"]
                  "c3_reader_expansions" [:sh-syntax "c3_reader_expansions.clj"]
                  "c3_reader_lossless_span" [:sh-syntax "c3_literal_projection/spans.clj"]}]
    (is (= (set (map #(str "bootstrap/clojure/test/gravity/" % "_test.clj") (keys expected)))
           (set (keys associations))))
    (doseq [[stem [owner source]] expected
            :let [test-path (str "bootstrap/clojure/test/gravity/" stem "_test.clj")
                  record (get associations test-path)]]
      (is (= #{:owner :source-path :source-namespace} (set (keys record))))
      (is (= owner (:owner record)))
      (is (= (str "bootstrap/clojure/src/gravity/" source) (:source-path record)))
      (is (.isFile (path test-path)))
      (is (.isFile (path (:source-path record))))
      (is (= (:source-namespace record)
             (binding [*read-eval* false]
               (with-open [reader (java.io.PushbackReader. (io/reader (path (:source-path record))))]
                 (second (read reader)))))))))

(deftest coordinator-reservations-and-leaf-conventions-are-explicit
  (let [record (ownership-record)
        coordinator (:coordinator-owned record)
        conventions (:future-path-conventions record)
        reserved-paths
        (concat (:central-routing coordinator)
                (:host-boundary-modules coordinator)
                (:host-boundary-tests coordinator)
                (:legacy-fixture-prefixes coordinator)
                (:integration-surfaces coordinator)
                (:coordination-state coordinator)
                (:coverage-generator-paths coordinator))]
    (testing "all exact coordinator reservations exist"
      (doseq [reserved reserved-paths]
        (is (.exists (path reserved)) reserved)))
    (testing "generated evidence, coverage, and completion state stay central"
      (is (seq (:generated-evidence-prefixes coordinator)))
      (is (seq (:coverage-prefixes coordinator)))
      (is (seq (:completion-state-paths coordinator)))
      (is (every? #(= :master-coordinator (:integration-owner %))
                  (vals (:slice-owners record)))))
    (testing "leaf writers receive only disjoint module, fixture, and test shapes"
      (is (= [:reserved-leaf-module :new-fixture :dedicated-test]
             (:worker-may-create conventions)))
      (is (= "gravity/self_hosting" (:discovery-resource conventions)))
      (is (= 'gravity.self-hosting-test-runner
             (:test-runner conventions)))
      (is (some #{:another-leaf-owner}
                (:worker-must-not-edit conventions))))))

(deftest stage0-compatibility-tests-are-five-exact-owned-paths
  (let [record (ownership-record)
        compatibility (:bootstrap-compatibility-tests record)
        module-owners (:module-owners record)
        stage0-tests (stage0-test-paths)]
    (is (= 5 (count compatibility)))
    (is (= 5 (count (set compatibility))))
    (doseq [test-path compatibility
            :let [source-path (paired-stage0-path test-path)
                  owner (get module-owners test-path)]]
      (is (.isFile (path test-path)) test-path)
      (is (contains? stage0-tests test-path) test-path)
      (is (keyword? owner) test-path)
      (is (= owner (get module-owners source-path)) [test-path source-path]))))

(deftest bootstrap-free-stage0-catalog-is-exact-when-runner-is-available
  (testing "the optional bootstrap-free runner owns exactly the reviewed leaf tests"
    (if-not (io/resource "gravity/bootstrap_free_leaf_test_runner.clj")
      (is true "bootstrap-free runner is not present in this checkout")
      (do
        (require 'gravity.bootstrap-free-leaf-test-runner)
        (let [runner-ns (find-ns 'gravity.bootstrap-free-leaf-test-runner)
              catalog (var-get (ns-resolve runner-ns 'catalog))
              validate! (var-get (ns-resolve runner-ns 'validate-catalog!))
              record (ownership-record)
              stage0-tests (stage0-test-paths)
              expected
              (set/difference
               stage0-tests
               (set (:bootstrap-compatibility-tests record))
               #{"bootstrap/clojure/test/gravity/bootstrap_test.clj"}
               (set (map #(str "bootstrap/clojure/test/gravity/" %)
                         (var-get
                          (ns-resolve runner-ns
                                      'excluded-top-level-test-files)))))]
          (is (= 53 (count catalog)))
          (is (= expected (set (map :test-path catalog))))
          (is (true? (validate!)))
          (is (every? symbol? (map :namespace catalog)))
          (is (= (set (map #(symbol (str "gravity." (str/replace % "_" "-") "-test"))
                           (map #(str/replace % #"^bootstrap/clojure/test/gravity/|_test\.clj$" "")
                                expected)))
                 (set (map :namespace catalog)))))))))

(deftest canonical-sort-regressions-use-the-existing-owned-leaf-module
  ;; Verify ownership, registration and retained test declarations without
  ;; loading or executing a provider namespace.
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        catalog (var-get (ns-resolve 'gravity.bootstrap-free-leaf-test-runner 'catalog))
        relative "bootstrap/clojure/test/gravity/c2_artifact_identity_test.clj"
        entry (some #(when (= relative (:test-path %)) %) catalog)
        source (slurp (path relative))]
    (is (= :sh-reader (get-in record [:module-owners relative])))
    (is (= 'gravity.c2-artifact-identity-test (:namespace entry)))
    (is (= "bootstrap/clojure/src/gravity/c2_artifact_identity.clj" (:source-path entry)))
    (is (= :c2-c3 (:group entry)))
    (is (not (.exists (path "bootstrap/clojure/test/gravity/c2_artifact_identity_sort_decoration_test.clj"))))
    (doseq [name ["decorations-never-change-normalized-carriers-or-full-encoded-preimages"
                  "sort-printer-call-order-and-thrown-object-are-conserved"
                  "full-entry-order-and-stable-ties-survive-canonical-key-collisions"
                  "final-string-printer-failure-precedes-the-injected-hash-callback"]]
      (is (str/includes? source (str "(deftest " name)) name))))

(deftest dedicated-test-routing-does-not-use-the-monolithic-test-file
  (let [deps (edn/read-string (slurp (path "deps.edn")))
        main-options (get-in deps [:aliases :test :main-opts])
        bootstrap-test-source
        (slurp (path "bootstrap/clojure/test/gravity/bootstrap_test.clj"))
        dedicated (runner/dedicated-test-namespaces)]
    (is (= ["-m" "gravity.self-hosting-test-runner"] main-options))
    (is (some #{'gravity.self-hosting.sh01-ownership-test} dedicated))
    (is (= dedicated (vec (sort dedicated))))
    (is (= dedicated (vec (distinct dedicated))))
    (is (= '[gravity.diagnostics-test
             gravity.cli-test
             gravity.bootstrap-test]
           (subvec (runner/test-namespaces) 0 3)))
    (is (not
         (str/includes?
          bootstrap-test-source
          "gravity.self-hosting.sh01-ownership-test")))
    (is (= {:mode :run :namespaces dedicated}
           (select-keys (runner/select-tests ["--dedicated"])
                        [:mode :namespaces])))
    (is (= {:mode :run
            :namespaces ['gravity.self-hosting.sh01-ownership-test]}
           (select-keys
            (runner/select-tests
             ["--namespace"
              "gravity.self-hosting.sh01-ownership-test"])
            [:mode :namespaces])))
    (is (some
         #{'gravity.darwin-publication-test}
         (:namespaces (runner/select-tests ["--list"]))))
    (let [exception
          (try
            (runner/select-tests
             ["--namespace" "gravity.unowned-test"])
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= "SH01-TEST-NAMESPACE" (:id (ex-data exception)))))))

(deftest repeatable-namespace-selection-is-strict-and-deterministic
  (let [selected
        (runner/select-tests
         ["--namespace" "gravity.self-hosting.sh01-ownership-test"
          "--namespace" "gravity.self-hosting.sh01-parallel-test-runner-test"
          "--fail-fast"])]
    (is (= [:run true]
           [(:mode selected) (:fail-fast? selected)]))
    (is (= ['gravity.self-hosting.sh01-ownership-test
            'gravity.self-hosting.sh01-parallel-test-runner-test]
           (:namespaces selected))))
  (doseq [[arguments expected-id]
          [[["--namespace" "gravity.self-hosting.sh01-ownership-test"
             "--namespace" "gravity.self-hosting.sh01-ownership-test"]
            "SH01-TEST-NAMESPACE-DUPLICATE"]
           [["--namespace" "gravity.self-hosting.sh01-does-not-exist"]
            "SH01-TEST-NAMESPACE"]]]
    (let [exception
          (try
            (runner/select-tests arguments)
            nil
            (catch clojure.lang.ExceptionInfo exception exception))]
      (is (= expected-id (:id (ex-data exception)))))))

(deftest run-namespaces-preserves-order-output-and-fail-fast-tail
  (let [events (atom [])
        summary (fn [namespace]
                  (swap! events conj [:run namespace])
                  (binding [test/*test-out* *out*]
                    (println (str "report:" namespace)))
                  {:test 1 :pass 1 :fail 0 :error 0})
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test
                       'gravity.self-hosting.sh01-parallel-test-runner-test]
          :fail-fast? true
          :require-fn #(swap! events conj [:require %])
          :run-tests-fn summary})]
    (is (= [[:require 'gravity.self-hosting.sh01-ownership-test]
            [:run 'gravity.self-hosting.sh01-ownership-test]
            [:require 'gravity.self-hosting.sh01-parallel-test-runner-test]
            [:run 'gravity.self-hosting.sh01-parallel-test-runner-test]]
           @events))
    (is (= :passed (:status report)))
    (is (empty? (:skipped-namespaces report)))
    (is (str/includes? (get-in report [:namespace-results 0 :stdout :text])
                       "report:gravity.self-hosting.sh01-ownership-test"))))

(deftest fresh-progress-telemetry-is-bounded-and-path-contained
  (let [relative "target/validation/sh01-fresh-progress-test.edn"
        path (Paths/get relative (make-array String 0))
        outside "/tmp/gravity-fresh-progress-outside.edn"
        link (Paths/get "target/validation/sh01-progress-linkdir"
                        (make-array String 0))
        linked-progress (.resolve link "progress.edn")]
    (try
      (Files/deleteIfExists path)
      (Files/deleteIfExists link)
      (let [emit (#'runner/progress-emitter relative)]
        (is (fn? emit))
        (emit {:event :test-var-start
               :namespace "gravity.bootstrap-test"
               :test-var "gravity.bootstrap-test/p15-s23-whole-language-compiler-artifact-records-current-stage-proof"
               :phase "gravity.bootstrap-test/p15-s23-whole-language-compiler-artifact-records-current-stage-proof"
               :active? true})
        (is (Files/exists path (make-array LinkOption 0)))
        (let [record (edn/read-string (slurp relative))]
          (is (= :gravity/fresh-verification-progress-v1 (:schema record)))
          (is (= :test-var-start (:event record)))
          (is (= 1 (:sequence record)))
          (is (integer? (:timestamp-ms record)))
          (is (true? (:active? record)))))
      (is (nil? (#'runner/progress-emitter outside)))
      (Files/createDirectories (.getParent link)
                               (make-array java.nio.file.attribute.FileAttribute 0))
      (Files/createSymbolicLink link (Paths/get "/tmp" (make-array String 0))
                               (make-array FileAttribute 0))
      (is (nil? (#'runner/progress-emitter (str linked-progress))))
      (finally
        (Files/deleteIfExists path)
        (Files/deleteIfExists link)))))

(deftest fresh-progress-telemetry-identifies-test-var-phase
  (let [events (atom [])
        emit #(swap! events conj %)]
    (#'runner/report-progress-event
     emit
     'gravity.bootstrap-test
     {:type :begin-test-var
      :var #'run-namespaces-preserves-order-output-and-fail-fast-tail})
    (is (= [{:event :test-var-start
             :namespace "gravity.bootstrap-test"
             :test-var "gravity.self-hosting.sh01-ownership-test/run-namespaces-preserves-order-output-and-fail-fast-tail"
             :phase "gravity.self-hosting.sh01-ownership-test/run-namespaces-preserves-order-output-and-fail-fast-tail"
             :active? true}]
           @events))))

(deftest fresh-progress-telemetry-rechecks-parent-before-publication
  (let [directory (Paths/get "target/validation/sh01-progress-race-dir"
                            (make-array String 0))
        progress (.resolve directory "progress.edn")
        outside (Files/createTempDirectory
                 "gravity-fresh-progress-race-outside-"
                 (make-array FileAttribute 0))]
    (try
      (Files/deleteIfExists directory)
      (Files/createDirectories directory
                               (make-array FileAttribute 0))
      (let [emit (#'runner/progress-emitter (str progress))]
        (is (fn? emit))
        ;; Replace the already-validated parent after setup. The per-write
        ;; recheck must disable publication rather than follow the new link.
        (Files/deleteIfExists directory)
        (Files/createSymbolicLink directory outside
                                   (make-array FileAttribute 0))
        (emit {:event :test-var-start
               :namespace "gravity.bootstrap-test"
               :test-var "gravity.bootstrap-test/p15-s23-proof"
               :phase "gravity.bootstrap-test/p15-s23-proof"
               :active? true})
        (is (not (Files/exists (.resolve outside "progress.edn")
                               (make-array LinkOption 0)))))
      (finally
        (Files/deleteIfExists directory)
        (Files/deleteIfExists outside)))))

(deftest run-namespaces-fail-fast-does-not-load-tail-and-reports-error
  (let [events (atom [])
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test
                       'gravity.self-hosting.sh01-parallel-test-runner-test]
          :fail-fast? true
          :require-fn #(swap! events conj [:require %])
          :run-tests-fn
          (fn [namespace]
            (swap! events conj [:run namespace])
            (if (= namespace 'gravity.self-hosting.sh01-ownership-test)
              {:test 1 :pass 0 :fail 1 :error 0}
              {:test 1 :pass 1 :fail 0 :error 0}))})]
    (is (= [[:require 'gravity.self-hosting.sh01-ownership-test]
            [:run 'gravity.self-hosting.sh01-ownership-test]]
           @events))
    (is (= :failed (:status report)))
    (is (= 1 (:exit-code report)))
    (is (= ['gravity.self-hosting.sh01-parallel-test-runner-test]
           (:skipped-namespaces report)))
    (is (= :failed (get-in report [:namespace-results 0 :status])))))

(deftest namespace-output-cap-never-emits-a-split-utf8-codepoint
  (let [ascii (apply str (repeat 8191 "a"))
        report
        (runner/run-namespaces
         {:namespaces ['gravity.self-hosting.sh01-ownership-test]
          :require-fn (fn [_] nil)
          :run-tests-fn
          (fn [_]
            (print (str ascii "€"))
            {:test 0 :pass 0 :fail 0 :error 0})})
        output (get-in report [:namespace-results 0 :stdout])]
    (is (= 8191 (:bytes output)))
    (is (= 8194 (:observed-bytes output)))
    (is (not (str/includes? (:text output) "�")))
    (is (= 8191 (count (:text output))))))

(deftest duplicate-dedicated-namespace-mappings-are-rejected
  (let [resource (io/resource "gravity/self_hosting/sh01_ownership_test.clj")
        test-root (.getParentFile (io/file (.toURI resource)))
        directories-var
        (ns-resolve
         'gravity.self-hosting-test-runner
         'file-resource-directories)
        exception
        (with-redefs-fn
          {directories-var (fn [] [test-root test-root])}
          (fn []
            (try
              (runner/dedicated-test-namespaces)
              nil
              (catch clojure.lang.ExceptionInfo exception exception))))]
    (is (= "SH01-TEST-NAMESPACE-COLLISION"
           (:id (ex-data exception))))
    (is (contains?
         (:collisions (ex-data exception))
         'gravity.self-hosting.sh01-ownership-test))))

(deftest optimization-reconstruction-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "optimization-reconstruction")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [optimization-stage-path optimization-stage-consumer optimization-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path optimization-stage-path)))]
        (is (= 'gravity.optimization (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc optimization-stage-path)
                 (assoc-in record [:module-owners optimization-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners optimization-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations optimization-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations optimization-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations optimization-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest llvm-data-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "llvm-data")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [llvm-data-stage-path llvm-data-stage-consumer llvm-data-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path llvm-data-stage-path)))]
        (is (= 'gravity.backend.llvm (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc llvm-data-stage-path)
                 (assoc-in record [:module-owners llvm-data-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners llvm-data-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations llvm-data-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations llvm-data-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations llvm-data-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest c-emission-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "c-emission")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [c-emission-stage-path c-emission-stage-consumer c-emission-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path c-emission-stage-path)))]
        (is (= 'gravity.backend.c (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc c-emission-stage-path)
                 (assoc-in record [:module-owners c-emission-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners c-emission-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations c-emission-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations c-emission-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations c-emission-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest wasm-codec-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "wasm-codec")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [wasm-codec-stage-path wasm-codec-stage-consumer wasm-codec-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path wasm-codec-stage-path)))]
        (is (= 'gravity.backend.wasm (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc wasm-codec-stage-path)
                 (assoc-in record [:module-owners wasm-codec-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners wasm-codec-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations wasm-codec-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations wasm-codec-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations wasm-codec-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest jvm-source-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "jvm-source")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [jvm-source-stage-path jvm-source-stage-consumer jvm-source-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path jvm-source-stage-path)))]
        (is (= 'gravity.backend.jvm (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc jvm-source-stage-path)
                 (assoc-in record [:module-owners jvm-source-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners jvm-source-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations jvm-source-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations jvm-source-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations jvm-source-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest js-source-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "js-source")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [js-source-stage-path js-source-stage-consumer js-source-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path js-source-stage-path)))]
        (is (= 'gravity.backend.js (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc js-source-stage-path)
                 (assoc-in record [:module-owners js-source-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners js-source-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations js-source-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations js-source-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations js-source-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest diagnostic-stage-dependencies-match-the-declared-api-and-real-test-imports
  (let [contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        components (get contract "components")
        by-id (into {} (map (juxt #(get % "id") identity)) components)
        component (get by-id "c15-diagnostics")
        stage-contract ((requiring-resolve 'gravity.c15-diagnostics/c15-engine-contract))
        source-namespaces (set (filter #(str/starts-with? (str %) "gravity.")
                                      (get-in stage-contract [:dependency-direction :requires])))
        test-ns (with-open [reader (java.io.PushbackReader.
                                    (io/reader (path (get-in component ["test" "path"]))))]
                  (edn/read reader))
        test-namespaces (->> (drop 2 test-ns)
                             (filter #(and (seq? %) (= :require (first %))))
                             (mapcat rest) (map first)
                             (filter #(str/starts-with? (str %) "gravity.")) set)
        resolve-dependencies (fn [field]
                               (set (map #(some-> (get-in by-id [% "source" "namespace"]) symbol)
                                         (get component field))))]
    (is (= source-namespaces (resolve-dependencies "direct_source_dependencies")))
    (is (= test-namespaces (resolve-dependencies "direct_test_dependencies")))
    (is (contains? source-namespaces 'gravity.diagnostics))))

(deftest lowering-helper-preserves-the-existing-coordinator-owned-c14-boundary
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "lowering-reconstruction")]
    (is (not (neg? index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [lowering-stage-path lowering-stage-consumer lowering-stage-test]]
      (is (.isFile (path relative))))
    (is (str/includes? (slurp (path lowering-stage-consumer)) "gravity.lowering/llvm-record"))
    (is (str/includes? (slurp (path lowering-stage-consumer)) "gravity.lowering/assert-llvm-reconstruction!"))
    (doseq [bad [(assoc-in record [:module-owners lowering-stage-path] :sh-effects)
                 (update record :standalone-test-owners dissoc lowering-stage-test)
                 (assoc-in record [:production-helper-associations lowering-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations lowering-stage-path :consumer-path] "forged.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" index "owner"] "sh-effects")
                 (assoc-in contract ["production_helpers" index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" index "consumer" "path"] "forged.clj")
                 (assoc-in contract ["production_helpers" index "test" "registration"] "bootstrap-free")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest effects-graph-helper-joins-existing-bootstrap-free-test
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "effects-graph-data")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [effects-stage-path effects-stage-consumer effects-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path effects-stage-path)))]
        (is (= 'gravity.effects (second (read reader))))))
    (doseq [operation ["effects/fact-direct-effects" "effects/effectful-facts" "effects/effect-graph"]]
      (is (str/includes? (slurp (path effects-stage-consumer)) operation)))
    (doseq [bad [(update record :module-owners dissoc effects-stage-path)
                 (assoc-in record [:module-owners effects-stage-path] :master-coordinator)
                 (assoc-in record [:module-owners effects-stage-test] :sh-core)
                 (assoc-in record [:production-helper-associations effects-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations effects-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations effects-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "standalone")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest package-helper-keeps-both-build-consumers-and-the-package-owner
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "package-selection")]
    (is (not (neg? index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [package-stage-path package-stage-consumer package-policy-consumer package-stage-test]]
      (is (.isFile (path relative))))
    (is (str/includes? (slurp (path package-stage-consumer)) "package/select-exact #'policy/decide"))
    (is (str/includes? (slurp (path package-policy-consumer)) "package/decide-policy request"))
    (doseq [bad [(assoc-in record [:module-owners package-stage-path] :sh-core)
                 (assoc-in record [:production-helper-associations package-stage-path :test-path] "forged_test.clj")
                 (update record :standalone-test-owners dissoc package-stage-test)
                 (assoc-in record [:production-helper-associations package-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations package-stage-path :consumer-path] "forged.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" index "owner"] "sh-core")
                 (assoc-in contract ["production_helpers" index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" index "consumer" "path"] "forged.clj")
                 (assoc-in contract ["production_helpers" index "test" "registration"] "bootstrap-free")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest ownership-data-helper-has-a-real-consumer-and-standalone-test
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        helper-index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "ownership-data")]
    (is (not (neg? helper-index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [ownership-stage-path ownership-stage-consumer ownership-stage-test]]
      (is (.isFile (path relative))))
    (binding [*read-eval* false]
      (with-open [reader (java.io.PushbackReader. (io/reader (path ownership-stage-path)))]
        (is (= 'gravity.ownership (second (read reader))))))
    (doseq [bad [(update record :module-owners dissoc ownership-stage-path)
                 (assoc-in record [:module-owners ownership-stage-path] :master-coordinator)
                 (assoc-in record [:standalone-test-owners ownership-stage-test :owner] :sh-core)
                 (assoc-in record [:production-helper-associations ownership-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations ownership-stage-path :consumer-path] "forged.clj")
                 (assoc-in record [:production-helper-associations ownership-stage-path :test-path] "forged_test.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" helper-index "owner"] "master-coordinator")
                 (assoc-in contract ["production_helpers" helper-index "test" "registration"] "bootstrap-free")
                 (assoc-in contract ["production_helpers" helper-index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" helper-index "consumer" "path"] "forged.clj")]]
      (is (not (production-helper-registration-valid? record bad))))))

(deftest tooling-helper-keeps-static-evidence-and-native-coverage-boundaries
  (require 'gravity.bootstrap-free-leaf-test-runner)
  (let [record (ownership-record)
        contract ((requiring-resolve 'gravity.bootstrap-free-leaf-test-runner/read-strict-component-contract))
        index (.indexOf (mapv #(get % "id") (get contract "production_helpers")) "tooling-evidence")]
    (is (not (neg? index)))
    (is (production-helper-registration-valid? record contract))
    (doseq [relative [tooling-stage-path tooling-stage-consumer tooling-stage-test]]
      (is (.isFile (path relative))))
    (is (str/includes? (slurp (path tooling-stage-consumer)) "gravity.tooling/coverage-record"))
    (is (= :native-coverage-data-and-static-source-evidence
           (:contract-boundary (:gravity/namespace-contract
             (meta (requiring-resolve 'gravity.tooling/coverage-record))))))
    (doseq [bad [(assoc-in record [:module-owners tooling-stage-path] :sh-core)
                 (update record :standalone-test-owners dissoc tooling-stage-test)
                 (assoc-in record [:production-helper-associations tooling-stage-path :namespace] 'gravity.forged)
                 (assoc-in record [:production-helper-associations tooling-stage-path :consumer-path] "forged.clj")]]
      (is (not (production-helper-registration-valid? bad contract))))
    (doseq [bad [(assoc-in contract ["production_helpers" index "owner"] "sh-core")
                 (assoc-in contract ["production_helpers" index "source" "namespace"] "gravity.forged")
                 (assoc-in contract ["production_helpers" index "consumer" "path"] "forged.clj")
                 (assoc-in contract ["production_helpers" index "test" "registration"] "bootstrap-free")]]
      (is (not (production-helper-registration-valid? record bad))))))
