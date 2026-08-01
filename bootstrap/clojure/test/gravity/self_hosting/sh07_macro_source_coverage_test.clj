(ns gravity.self-hosting.sh07-macro-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_macro_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 macro source coverage test is not on the classpath"
        {:id "SH07-MACRO-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-MACRO-COVERAGE-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private macro-relative-path
  "bootstrap/gravity/src/gravity/macro.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-macro-source-revision-id
  "sha256:19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549")
(def ^:private expected-coverage
  ;; These values intentionally freeze full-source B15 coverage. A source
  ;; change must update this census only after the new lowering is inspected.
  {:fragment-count 49
   :root-form-count 49
   :form-count 4743
   :binding-count 426
   :local-binding-count 164
   :resolution-count 1552})
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B15 coordinator adapter is absent"
        {:id "SH07-MACRO-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)
        namespace (get-in authenticated-request [:module :namespace])]
    {:fragment-count
     (count (:fragment-manifest authenticated-request))
     :root-form-count
     (count (:top-level-form-ids authenticated-request))
     :form-count
     (count (:forms authenticated-request))
     :binding-count
     (count (:binding-table authenticated-request))
     :local-binding-count
     (count
      (filter #(= namespace (:namespace %))
              (:binding-table authenticated-request)))
     :resolution-count
     (count (:resolution-table authenticated-request))}))

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error
       {:class (.getName (class throwable))
        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (and (string? (:id data))
                   (keyword? (:stage data)))
          (assoc data :rule (:id data)))
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(def ^:private macro-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path macro-relative-path))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-macro-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/macro.qst")
          left-path (path macro-relative-path)
          bytes (source-bytes (path macro-relative-path))]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        (let [file-artifact (required-var 'sh07-core-file-artifact)]
          {:left @macro-artifact
           :right (file-artifact (str right-path))
           :left-path left-path
           :right-path (str right-path)})
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b14-proof-contract-registers-macro-source-exactly
  (let [contract
        (edn/read-string (slurp (path proof-contract-relative-path)))
        modules (:authoritative-modules contract)
        nonclaims (set (:nonclaims contract))]
    (is (= "SH-07-B31" (:coverage-milestone contract)))
    (is (= {:request-schema-version 15
            :task "SH-07-B15"
            :scope :sh07-b15-keyword-map-lookup
            :adapter :gravity/sh07-to-c6-core-products-v15
            :fresh-authoritative-process-required true
            :iteration-cache-authoritative false}
           (:boundary contract)))
    (is (= {:diagnostics
            "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity"
            :b5-jvm
            "bootstrap/gravity/src/gravity/backend/b5_jvm_backend_design.gravity"
            :b6-javascript-typescript
            "bootstrap/gravity/src/gravity/backend/b6_javascript_typescript_backend_design.gravity"
            :b7-mlir
            "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity"
            :b8-gpu
            "bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity"
            :b9-hdl
            "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"
            :b10-workflow
            "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"
            :b11-query
            "bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity"
            :b12-mobile
            "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity"
            :c11-mir
            "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
            :c15-diagnostics
            "bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity"
            :c16-incremental
            "bootstrap/gravity/src/gravity/compiler/c16_incremental_compilation_design.gravity"
            :c17-plugin
            "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity"
            :sh21-meta
            "bootstrap/gravity/src/gravity/self_hosting/meta_compiler_legality.gravity"
            :c18-verification
            "bootstrap/gravity/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity"
            :c7-types
            "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
            :c8-effects
            "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity"
            :c9-ownership
            "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity"
            :c10-safety
            "bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity"
            :c3-compat
            "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"
            :c6-core
            "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity"
            :c4-compat
            "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity"
            :c5-compat
            "bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"
            :l2-core
            "bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity"
            :macro macro-relative-path
            :reader
            "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
            :syntax
            "bootstrap/gravity/src/gravity/bootstrap/syntax.gravity"}
           modules))
    (is (= expected-macro-source-revision-id
           (sha256-id (source-bytes (path (:macro modules))))))
    (doseq [nonclaim
            [:macro-expander-runtime-authority
             :c4-adapter-retirement
             :c5-adapter-retirement
             :c15-diagnostic-runtime-authority
             :c16-incremental-runtime-authority
             :c17-plugin-runtime-authority
             :sh21-whole-compiler-meta-execution
             :c18-production-verifier-execution
             :c18-contract-and-artifact-schema-enforcement
             :c18-proof-certificate-and-evidence-checker-execution
             :c18-translation-validation-and-replay-execution
             :c18-release-gate-decision-authority
             :c18-plugin-and-backend-conformance-execution
             :c7-production-type-checker-execution
             :c7-contract-and-diagnostic-schema-enforcement
             :sh08-authenticated-coordinator-adapter
             :sh08-resolved-typed-artifact-digest
             :sh08-inference-constraints-generics-and-profile-legality
             :sh08-dynamic-layout-schema-ownership-and-diagnostic-execution
             :sh08-complete
             :c8-production-effect-checker-execution
             :c8-contract-and-diagnostic-schema-enforcement
             :sh09-authenticated-sh08-adapter
             :sh09-effect-inference-and-transitive-call-effects
             :sh09-handled-effects-and-module-summaries
             :sh09-runtime-profile-policy
             :sh09-mir-preservation
             :sh09-complete
             :c9-production-ownership-checker-execution
             :c9-contract-and-diagnostic-schema-enforcement
             :sh10-authenticated-sh08-sh09-adapter
             :sh10-persistent-copy-and-field-range-splitting
             :sh10-regions-arenas-and-linear-resources
             :sh10-transfer-runtime-check-and-unsafe-audit
             :sh10-mir-preservation
             :sh10-complete
             :c10-production-safety-analysis-execution
             :c10-contract-and-diagnostic-schema-enforcement
             :sh11-memory-lifetime-region-and-linear-safety
             :sh11-ffi-concurrency-taint-and-generated-code-safety
             :sh11-floating-point-and-elementary-function-safety
             :sh11-optimization-invalidation-and-mir-preservation
             :sh11-authenticated-sh09-sh10-convergence
             :sh11-complete
             :sh07-complete]]
      (is (contains? nonclaims nonclaim)))))

(deftest sh07-b14-macro-source-has-exact-b13-coverage-and-real-products
  (let [artifact @macro-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        fragments (:fragment-manifest authenticated-request)
        coverage-record (:fragment-coverage core-artifact)
        core-forms (frequencies (map :core-form (:nodes core-artifact)))
        embedded-proof (:capability-based-proof artifact)
        local-bindings
        (filterv
         #(= (get-in authenticated-request [:module :namespace])
             (:namespace %))
         (:binding-table authenticated-request))]
    (is (= :accepted (:status artifact)))
    (is (= "SH-07-B15" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= expected-coverage (coverage artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (= (mapv :binding-id local-bindings)
           (:covered-local-binding-ids coverage-record)
           (vec (mapcat :local-binding-ids fragments))))
    (is (= (mapv :reference-syntax-id
                 (:resolution-table authenticated-request))
           (:covered-resolution-reference-syntax-ids coverage-record)
           (vec
            (mapcat :resolution-reference-syntax-ids fragments))))
    (is (seq (:definitions core-artifact)))
    (is (pos? (get core-forms :fn 0)))
    (is (seq (:calls core-artifact)))
    (is (seq (:loop-bindings core-artifact)))
    (is (seq (:recur-targets core-artifact)))
    (is (seq (:recur-transfers core-artifact)))
    (is (= :gravity/sh07-core-capability-proof
           (:artifact embedded-proof)))
    (is (= :complete (:status embedded-proof)))
    (is (= [] (:failed-checks embedded-proof)))
    (is (every? true?
                (vals
                 (dissoc embedded-proof
                         :artifact :status :failed-checks))))
    (doseq [required-check
            [:template-verification-passed?
             :resolved-verification-passed?
             :canonical-core-replays?
             :fragment-manifest-replay?
             :fragment-coverage-replay?
             :module-assembly-manifest-replay?]]
      (is (true? (get embedded-proof required-check))))))

(deftest sh07-b14-macro-source-is-deterministic-path-neutral-and-provenanced
  (let [{:keys [left right left-path right-path]}
        @parity-artifacts]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (coverage left) (coverage right)))
    (is (= (:fragment-manifest (request left))
           (:fragment-manifest (request right))))
    (is (= (:fragment-coverage (core left))
           (:fragment-coverage (core right))))
    (is (= (:module-assembly-manifest (core left))
           (:module-assembly-manifest (core right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))
    (is (not= left-path right-path))))

(deftest sh07-b14-macro-request-and-source-lineage-alterations-fail-closed
  (let [artifact @macro-artifact
        authenticated-request (request artifact)
        resolution-artifact (:sh06-resolution-artifact artifact)
        cases
        (cond->
         {"request projection"
          (assoc authenticated-request :projection-binding zero-id)
          "source revision"
          (assoc-in authenticated-request
                    [:module :source-revision-id]
                    zero-id)
          "source lineage"
          (assoc-in authenticated-request
                    [:lineage :source-revision-id]
                    zero-id)}
          (seq (:macro-origin-traces authenticated-request))
          (assoc
           "macro origin source lineage"
           (assoc-in authenticated-request
                     [:macro-origin-traces 0 :source-revision-id]
                     zero-id)))]
    (is (seq (:macro-origin-traces authenticated-request)))
    (doseq [[label altered] cases]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var 'sh07-core-run-request-for-test)
                 resolution-artifact altered))
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= true
                 (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b14-macro-structural-and-resolved-alterations-fail-verification
  (let [artifact @macro-artifact
        upstream-verification
        ((required-var 'sh06-resolution-artifact-verification)
         (:sh06-resolution-artifact artifact))
        cases
        {"fragment structure"
         {:expected-checks
          #{:fragment-manifest-replay? :canonical-core-replays?}
          :artifact
          (update-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :fragment-manifest]
           pop)}
         "resolved definition"
         {:expected-checks #{:canonical-core-replays?}
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :definitions 0 :binding-id]
           zero-id)}
         "resolved node"
         {:expected-checks #{:canonical-core-replays?}
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :nodes 0 :node-id]
           zero-id)}
         "resolved call"
         {:expected-checks #{:calls-replay? :canonical-core-replays?}
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :calls 0 :binding-id]
           zero-id)}
         "digest request"
         {:expected-checks #{:digest-sequence-replays?}
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :digest-requests 0
            :unexpected-alteration]
           true)}
         "stored capability status"
         {:expected-checks #{:stored-capability-proof-current?}
          :artifact
          (assoc-in artifact [:capability-based-proof :status]
                    :failed)}
         "stored capability check"
         {:expected-checks #{:stored-capability-proof-current?}
          :artifact
          (assoc-in artifact
                    [:capability-based-proof
                     :fragment-manifest-replay?]
                    false)}
         "actual path provenance"
         {:expected-checks #{:provenance-retained?}
          :artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact
            :provenance :actual-source-path]
           "/substituted/root/macro.gravity")}}]
    (is (= :passed (:status upstream-verification)))
    (doseq [[label {:keys [expected-checks artifact]}] cases]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var 'sh07-core-verification-checks)
                 artifact @macro-artifact upstream-verification))
              checks (:value result)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (nil? (:raw-host-error result)))
          (is (map? checks))
          (is (every? failed expected-checks))
          (is (seq failed)))))))
