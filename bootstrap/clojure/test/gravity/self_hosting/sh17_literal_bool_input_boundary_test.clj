(ns gravity.self-hosting.sh17-literal-bool-input-boundary-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]
            [gravity.self-hosting.sh16-literal-bool-translation-validation-test]))

(defn- existing [namespace name] (var-get (ns-resolve namespace name)))
(defn- path [relative]
  (str (.resolve @(existing 'gravity.self-hosting.sh12-branch-join-mir-test 'root) relative)))
(def ^:private c14-path
  "bootstrap/gravity/src/gravity/compiler/c14_literal_bool_contextual_lowering.gravity")
(def ^:private dependencies
  ["bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
   "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity"])
(def ^:private bundle
  (delay
    ;; Ordinary C5 module artifacts and source plans own dependency resolution;
    ;; no host function or supplied verification callback enters this bundle.
    (let [source (path c14-path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
          files (mapv path dependencies)
          reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
          empty-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
          resolve-source (fn [file module-inputs]
                           (let [start (System/nanoTime)]
                             (prn {:development-phase :c5-start :source file})
                             (let [artifact (bootstrap/compiler-c5-resolution-source-artifact
                                             file (slurp file) module-inputs reader)]
                               (prn {:development-phase :c5-complete :source file
                                     :elapsed-nanoseconds (- (System/nanoTime) start)})
                               artifact)))
          artifacts (mapv #(resolve-source % empty-inputs) files)
          inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts artifacts}
          caller (resolve-source source inputs)
          plans (mapv #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter % (slurp %)) files)]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)
        {:caller-resolution-artifact caller :dependency-plans plans}))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-module-execute-function
   {:engine :sh17-literal-bool-input-boundary-development :compiler-artifact-plan? true}
   @bundle function arguments))
(defn- lower [context] (invoke 'c14-build-literal-bool-contextual-lowering [context]))
(def ^:private required-diagnostic-fields
  [:input-artifact-id :mir-operation-or-domain-anchor :source-span :origin-chain
   :profile :target :backend :missing-feature :proof-expected :provider-expected
   :fallback-status :remediation])

(deftest contextual-diagnostic-helper-preserves-canonical-fields
  ;; Actual source-plan helper execution on supplied DATA only. This does not
  ;; authenticate MIR or execute the contextual C5/dependency consumer.
  (let [source (path c14-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))
        forms (mapv :form (bootstrap/read-source-form-records source (slurp source)))
        helper (first (filter #(and (= 'defn (first %))
                                     (= 'c14-literal-bool-boundary-result (second %))) forms))
        header (apply list (map #(if (and (seq? %) (= :exports (first %)))
                                  '(:exports [c14-literal-bool-boundary-result]) %)
                                (remove #(and (seq? %) (= :requires (first %))) (first forms))))
        ;; The body is unchanged; only this leaf's unused imports and exports
        ;; are narrowed. The full contextual bundle above remains mandatory.
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan
              emitter (str source "#diagnostic-leaf") (str (pr-str header) "\n" (pr-str helper)))
        origin [{:kind :source :source-id :fixture}]
        span {:source-id :fixture :start 0 :end 1}
        before {:source-core "fixture-core" :module-id 'fixture :profile :hosted :target-request :jvm
                :functions {'main {:source {:span span :origin-id :fixture :generated-origin origin}}}}
        run (fn [code reason mir]
              (bootstrap/p15-s23-stage2-runtime-execute-function
               {:engine :sh17-contextual-diagnostic-development :compiler-artifact-plan? true}
               plan 'c14-literal-bool-boundary-result [code reason :rejected mir]))]
    (doseq [code ["C14-INPUT" "C14-UNSUPPORTED"]]
      (let [diagnostic (first (:diagnostics (run code :fixture-reason before)))]
        (is (every? #(contains? diagnostic %) required-diagnostic-fields))
        (is (= ["fixture-core" :original-zero-arity-literal-bool-diamond 'fixture :hosted :jvm
                :gravity.backend/jvm span origin :fixture-reason]
               ((juxt :input-artifact-id :mir-operation-or-domain-anchor :mir-module-id :profile :target
                      :backend :source-span :origin-chain :missing-feature) diagnostic)))
        (is (= (if (= code "C14-INPUT") :not-applicable :jvm-b5-target-lowering-contract)
               (:provider-expected diagnostic)))))
    (let [diagnostic (first (:diagnostics (run "C14-INPUT" :invalid-context nil)))]
      (is (every? #(contains? diagnostic %) required-diagnostic-fields))
      (is (= [nil :not-applicable nil nil nil []]
             ((juxt :input-artifact-id :mir-operation-or-domain-anchor :profile :target :source-span
                    :origin-chain) diagnostic))))))

(defn- products [condition extension]
  (let [start (System/nanoTime)
        {:keys [core mir] :as pair}
        ((existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'original-pair) condition extension)
        source-complete (System/nanoTime)
        verify (existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'invoke)
        report (verify 'sh12-verify-branch-join-mir core mir)
        after (:candidate-mir
               ((existing 'gravity.self-hosting.sh16-literal-bool-branch-rewrite-test 'rewrite) mir report))
        post (verify 'sh12-verify-literal-bool-branch-specialization core mir after)
        result ((existing 'gravity.self-hosting.sh16-literal-bool-translation-validation-test 'consume)
                mir report after post)]
    (assoc pair :report report :post post
           :development-timing {:source-products-nanoseconds (- source-complete start)
                                :c11-c13-products-nanoseconds (- (System/nanoTime) source-complete)}
           :context {:checked-core core :before-mir mir :c13-result result :target-request :jvm})))
(def ^:private cases
  (into {} (for [condition ["true" "false"] extension ["gravity" "qst"]]
             [[condition extension] (delay (products condition extension))])))

(deftest original-context-passes-carrier-validation
  ;; This is a producer/transport check, not execution of the C14 consumer.
  (doseq [[condition extension] [["true" "gravity"]]]
    (let [{:keys [raw upstream report post context]} @(get cases [condition extension])
          preflight ((existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'invoke)
                     'sh12-authenticated-c10-mir-carrier-preflight context)]
      (is (= :accepted (:status raw)))
      (is (= :passed (:status upstream) (:status report) (:status post)))
      (is (= :accepted (:status preflight)) (pr-str preflight))
      (let [realized? (atom false)
            sequence (lazy-seq (reset! realized? true) (list :untrusted))]
        (is (= {:status :rejected :reason :c10-mir-carrier-seq-unsupported}
               ((existing 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'invoke)
                'sh12-authenticated-c10-mir-carrier-preflight
                (assoc context :sequence sequence))))
        (is (false? @realized?)))
      (prn {:development-context [condition extension] :carrier-preflight preflight}))))

(defn- check-valid [condition extension]
  (let [{:keys [raw upstream report post context development-timing]} @(get cases [condition extension])]
    (is (= :accepted (:status raw)))
    (is (= :passed (:status upstream) (:status report) (:status post)))
    (prn {:development-context [condition extension] :development-timing development-timing})
    (let [result (lower context)]
    (is (= [:rejected :scoped-valid] ((juxt :status :input-status) result)) (pr-str result))
    (is (= ["C14-UNSUPPORTED"] (mapv :diagnostic-id (:diagnostics result))))
    (is (= [:jvm-b5-target-lowering-contract] (mapv :reason (:diagnostics result))))
    (is (= (:artifact-id (:checked-core context))
           (get-in result [:diagnostics 0 :input-artifact-id])))
    (is (every? #(contains? (first (:diagnostics result)) %) required-diagnostic-fields))
    (is (= [:jvm :gravity.backend/jvm]
           ((juxt :target :backend) (first (:diagnostics result)))))
    (is (= [true false] ((juxt :clojure-seed-boundary? :self-hosted?) result)))
    (is (every? (set (:nonclaims result)) [:backend-admission :artifact-emission :optimized-execution]))
    (is (not-any? #(contains? result %) [:emitted-artifact :target-program :authority :artifact-id])))))

(deftest sh17-fresh-false-gravity-input-stops-at-jvm-contract (check-valid "false" "gravity"))
(deftest sh17-fresh-true-qst-input-stops-at-jvm-contract (check-valid "true" "qst"))
(deftest sh17-fresh-false-qst-input-stops-at-jvm-contract (check-valid "false" "qst"))

(defn- rejected-input
  ([context] (rejected-input context nil))
  ([context reason]
   (let [result (lower context)]
     (is (= [:rejected :rejected] ((juxt :status :input-status) result)))
     (is (= ["C14-INPUT"] (mapv :diagnostic-id (:diagnostics result))))
     (when reason
       (is (= [reason] (mapv :reason (:diagnostics result)))))
     (is (every? #(contains? (first (:diagnostics result)) %) required-diagnostic-fields))
     (is (not-any? #(contains? result %) [:emitted-artifact :target-program :authority :artifact-id]))
     result)))

(deftest sh17-contextual-module-validates-original-and-rejects-substitution
  ;; Minimal affected-integration control: one real source producer, followed
  ;; by actual fresh module execution for the positive and independent
  ;; invalidation, target and source-binding mutations.
  (check-valid "true" "gravity")
  (let [context (:context @(get cases ["true" "gravity"]))]
    (let [result (rejected-input (assoc-in context [:c13-result :invalidation-ledger :proofs-invalidated]
                                         [:substituted-proof]) :stale-c13-result)]
      (is (= (get-in context [:before-mir :source-core]) (get-in result [:diagnostics 0 :input-artifact-id])))
      (is (= (get-in context [:before-mir :functions 'main :source :span])
             (get-in result [:diagnostics 0 :source-span]))))
    ;; Keep the authentic Core present: a malformed Core would mask this edge.
    (let [result (rejected-input (assoc context :target-request :llvm) :substituted-target-request)]
      (is (= (get-in context [:before-mir :source-core]) (get-in result [:diagnostics 0 :input-artifact-id])))
      (is (= :jvm (get-in result [:diagnostics 0 :target]))))
    (let [substituted-hash "sha256:0000000000000000000000000000000000000000000000000000000000000000"]
      (is (not= substituted-hash (get-in context [:checked-core :source-content-hash])))
      (let [result (rejected-input (assoc-in context [:checked-core :source-content-hash] substituted-hash)
                                   :invalid-specialization-relation)]
        (is (= [nil nil []] ((juxt :input-artifact-id :source-span :origin-chain)
                             (first (:diagnostics result)))))))))

(deftest sh17-input-boundary-rejects-malformed-and-unbound-contexts
  (let [context (:context @(get cases ["true" "gravity"]))]
    (doseq [bad (concat [nil [] {} 0 false "context"]
                       (map #(dissoc context %) (keys context))
                       [(assoc context :extra true)
                        (assoc context :target-request :llvm)
                        (assoc context :target-request {:backend :jvm})
                        (assoc context :checked-core (vec (repeat 1025 nil)))
                        (assoc context :checked-core (nth (iterate vector nil) 257))
                        (assoc context :c13-result {:translation-validation-status :passed})])]
      (rejected-input bad))))

(deftest sh17-input-boundary-rejects-stale-source-and-target-tuples
  (let [context (:context @(get cases ["true" "gravity"]))
        other (:context @(get cases ["false" "gravity"]))]
    (doseq [field [:checked-core :before-mir :c13-result]]
      (testing (name field) (rejected-input (assoc context field (get other field)))))
    ;; A coherent target substitution is still outside the actual JVM source
    ;; family. Eligibility never converts a malformed tuple into UNSUPPORTED.
    (let [retarget #(assoc % :source-target :llvm :target-request :llvm)
          substituted (-> context
                          (assoc :target-request :llvm)
                          (update :checked-core assoc :source-target :llvm)
                          (assoc-in [:checked-core :target-request-metadata :requested-target] :llvm)
                          (update :before-mir retarget)
                          (update-in [:c13-result :candidate-mir] retarget))]
      (rejected-input substituted))
    (doseq [field [[:checked-core :target-request-metadata :requested-target]
                  [:before-mir :target-request] [:c13-result :candidate-mir :target-request]]]
      (rejected-input (assoc-in context field :llvm)))))

(deftest sh17-input-boundary-rederives-complete-c13-record
  (let [context (:context @(get cases ["true" "gravity"]))
        result (:c13-result context)]
    (doseq [bad (concat (map #(dissoc result %) (keys result))
                       [(assoc result :extra true)
                        (assoc result :status :accepted)
                        (assoc result :admission-status :admitted)
                        (assoc result :translation-validation-status :rejected)
                        (assoc result :self-hosted? true)
                        (assoc-in result [:post-pass-report :status] :passed-but-stale)
                        (assoc-in result [:post-pass-report :checks] [])
                        (assoc-in result [:post-pass-report :input-bindings :checked-core-artifact-id] :stale)
                        (assoc-in result [:decision-record :verifier-result] :rejected)
                        (assoc-in result [:decision-record :changed-terminators] [])
                        (assoc-in result [:decision-record :proofs-used] [:substituted-proof])
                        (assoc-in result [:invalidation-ledger :proofs-invalidated] [:substituted-proof])
                        (assoc-in result [:invalidation-ledger :certificates-invalidated] [:substituted-certificate])
                        (assoc-in result [:invalidation-ledger :analysis-invalidated] [])
                        (assoc-in result [:pass-contract :version] 2)
                        (assoc result :nonclaims [])])]
      (rejected-input (assoc context :c13-result bad)))))

(deftest sh17-input-boundary-rejects-coherent-cfg-origin-and-type-mutations
  (let [context (:context @(get cases ["true" "gravity"]))
        before (:before-mir context) after (get-in context [:c13-result :candidate-mir])
        entry (get-in after [:functions 'main :entry])
        selected (get-in after [:functions 'main :blocks entry :terminator :successors 0])
        term [:functions 'main :blocks entry :terminator]
        arm [:functions 'main :blocks selected :instructions 0]
        operation (get-in after arm) id (:op-id operation) value (:result operation)
        altered-source (assoc (:source operation) :origin-id :substituted-origin)
        changed-origin (-> after (assoc-in (conj arm :source) altered-source)
                           (assoc-in [:source-map id] altered-source)
                           (assoc-in [:data-flow-graph :values value :source] altered-source))]
    (doseq [bad [(assoc after :control-flow-graph (:control-flow-graph before))
                 (assoc after :data-flow-graph (:data-flow-graph before))
                 (assoc-in after (conj term :successors) [])
                 (assoc-in after [:control-flow-graph :dominance selected] [selected])
                 (assoc-in after [:control-flow-graph :join :incoming] [])
                 changed-origin
                 (assoc-in after [:functions 'main :returns] :gravity/i64)
                 (assoc after :runtime-check-table {:substituted {:status :elided}})]]
      (rejected-input (assoc-in context [:c13-result :candidate-mir] bad)))
    (rejected-input (-> context
                        (assoc-in [:before-mir :functions 'main :returns] :gravity/i64)
                        (assoc-in [:c13-result :candidate-mir :functions 'main :returns] :gravity/i64)
                        (assoc-in [:checked-core :core-nodes 5 :type :return] :gravity/i64)))))

(deftest sh17-current-reader-binding-and-source-construction-control
  (let [source (path "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/zero_arity_bool_true_source.gravity")
        text (slurp source) reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        reader-text (slurp reader) calls (atom 0) original bootstrap/read-source-form-records
        artifact (with-redefs [bootstrap/read-source-form-records
                               (fn [file contents]
                                 (when (= source file) (swap! calls inc))
                                 (original file contents))]
                   (bootstrap/compiler-c2-reader-source-artifact
                    source text (bootstrap/reader-project-context-for-source source) nil nil reader))
        boundary (:gravity-reader-boundary artifact)
        binding (:plan-binding boundary)
        revision (get-in boundary [:authenticated-envelope-descriptor :source-revision])]
    (is (= :gravity/stage0-c2-reader-document-artifact (:kind artifact)))
    (is (= :accepted (get-in boundary [:resolved-reader-result :status])))
    (is (= (str "sha256:" (bootstrap/sha256-hex reader-text))
           (:source-content-hash binding) (:source-content-hash revision)))
    (is (= (alength (.getBytes reader-text java.nio.charset.StandardCharsets/UTF_8))
           (:source-byte-count binding) (:source-byte-count revision)))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in artifact [:source-unit-record :bytes-hash])))
    (is (= :accepted (get-in boundary [:authenticated-envelope :status])))
    (is (zero? @calls))))
