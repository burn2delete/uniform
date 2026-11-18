(defn c-backend-stage2-runtime-source-rule!
  "Load the Gravity-authored P15-S23 runtime executor and kernel rules.\n\n  Runtime-derived lowering is only authoritative for this slice when the\n  emitted stage2 plan is executed through the source-defined runtime rule\n  record.  Keep the source rule hashes path-neutral; the resolved source path\n  remains a provenance field on the returned binding for auditability."
  [source-path target]
  (let [state (hash-map 'source-path source-path 'target target)
        state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_bindings_01
                state)
        state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_expressions_02
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_bindings_03
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_expressions_04
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_bindings_05
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_expressions_06
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_07
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_08
                state)
        state (__gravity_bootstrap_runtime_source_rule_runtime_artifact_contract_bindings_09
                state)]
    (__gravity_bootstrap_runtime_source_rule_final_runtime_rule_record state)))

(def ^:private c-backend-current-runtime-development-exports
  #{'p15-s23-runtime-execute-closed-plan
    'p15-s23-runtime-evaluate-closed-instruction
    'p15-s23-runtime-evaluate-in-context
    'p15-s23-runtime-activate-function})

(defn c-backend-current-runtime-development-packet?
  [packet]
  (and (map? packet)
       (= :gravity/current-source-runtime-development-packet (:kind packet))))

(defn c-backend-current-runtime-development-source-record
  [runtime-rule]
  (let [binding (:current-runtime-development-binding runtime-rule)
        facade (:current-runtime-compatibility-link runtime-rule)]
    {:kind :gravity/current-runtime-development-source
     :runtime-owner-content-hash (:runtime-source-content-hash binding)
     :runtime-plan-id (:runtime-plan-id binding)
     :runtime-function-hashes (:runtime-function-hashes binding)
     :evaluator-function (:runtime-artifact-closed-plan-function runtime-rule)
     :evaluator-function-hash
     (:runtime-artifact-closed-plan-function-hash runtime-rule)
     :compatibility-facade-content-hash (:source-content-hash facade)
     :compatibility-import (:import facade)
     :compatibility-forwarders (:forwarders facade)
     :compatibility-only-helpers (:compatibility-only-helpers facade)
     :compatibility-helpers-invoked-by-evaluator? false
     :runtime-rule-hash (:runtime-rule-hash runtime-rule)
     :runtime-artifact-hash (:runtime-artifact-hash runtime-rule)
     :revision-status :development-unadmitted
     :effects :not-checked
     :clojure-seed-boundary? true
     :self-hosted? false}))

(declare sh03-reader-strict-source-text!)

(defn c-backend-current-runtime-development-source-paths
  "Resolve the current evaluator and its compatibility facade from the checkout
  that owns the P15 emitter. The historical adapter is never a fallback."
  []
  (let [compiler-source (c-backend-resolve-p15-s23-compiler-source-path)
        compiler-file (.getCanonicalFile (java.io.File. compiler-source))
        start (.getParentFile compiler-file)
        project-root (or (loop [candidate start]
                           (when candidate
                             (if (.isFile (java.io.File. candidate "deps.edn"))
                               candidate
                               (recur (.getParentFile candidate)))))
                         start)
        resolve-path #(-> (java.io.File. project-root %)
                          .getCanonicalFile
                          .getPath)]
    {:compiler-source compiler-source
     :runtime-source (resolve-path "language/runtime/src/gravity/runtime.gravity")
     :compatibility-source
     (resolve-path "bootstrap/gravity/p15_s23/runtime.gravity")}))

(defn- c-backend-current-runtime-development-read-source!
  [path requested-source target missing-fact]
  (let [file (java.io.File. path)]
    (when-not (.isFile file)
      (p15-s23-stage2-runtime-executor-fail!
       "P15S23X001" path nil
       {:requested-source requested-source
        :target target
        :missing-fact missing-fact
        :missing-fields [missing-fact]
        :result-committed? false
        :output-committed? false}))
    (let [snapshot (try
                     (c2-pass-cache/source-snapshot! path)
                     (catch Exception ex
                       (p15-s23-stage2-runtime-executor-fail!
                        "P15S23X001" path nil
                        {:requested-source requested-source
                         :target target
                         :missing-fact missing-fact
                         :cause-message (.getMessage ex)})))
          bytes (:bytes snapshot)
          canonical-path (:canonical-path snapshot)
          text (try
                 (sh03-reader-strict-source-text!
                  requested-source canonical-path bytes)
                 (catch Exception ex
                   (p15-s23-stage2-runtime-executor-fail!
                    "P15S23X001" canonical-path nil
                    {:requested-source requested-source
                     :target target
                     :missing-fact :current-runtime-source-valid-utf8
                     :cause-message (.getMessage ex)})))]
      {:path canonical-path
       :bytes bytes
       :text text
       :byte-count (:byte-count snapshot)
       :sha256 (:bytes-hash snapshot)})))

(defn- c-backend-current-runtime-development-compatibility-link!
  [requested-source target compatibility-source]
  (let [{:keys [path text byte-count sha256]}
        (c-backend-current-runtime-development-read-source!
         compatibility-source requested-source target
         :runtime-compatibility-source)
        forms (:forms (p15-s23-compiler-source-form-record-from-text path text))
        ns-form (first forms)
        requires-clause (some #(when (and (seq? %) (= :requires (first %))) %)
                              (drop 2 ns-form))
        forwarders
        {'p15-s23-runtime-execute-closed-plan ['plan]
         'p15-s23-runtime-evaluate-closed-instruction
         ['instruction 'environment 'output]
         'p15-s23-runtime-evaluate-in-context
         ['instruction 'environment 'output 'functions 'path]
         'p15-s23-runtime-activate-function
         ['function 'values 'output 'functions 'path]}
        expected-forwarders
        (into {}
              (map (fn [[function-name params]]
                     [function-name
                      (list 'defn function-name (vec params)
                            (apply list
                                   (symbol "runtime" (clojure.core/name function-name))
                                   params))]))
              forwarders)
        helpers
        {'p15-s23-runtime-format-value
         '(defn p15-s23-runtime-format-value [value] (str value))
         'p15-s23-runtime-concat
         '(defn p15-s23-runtime-concat [left right] (str left right))
         'p15-s23-runtime-println-value
         '(defn p15-s23-runtime-println-value [value] (println (str value)))
         'p15-s23-runtime-println-two
         '(defn p15-s23-runtime-println-two [left right]
            (println (str left " " right)))}
        definitions (into {} (map (fn [form] [(second form) form]))
                          (filter #(and (seq? %) (= 'defn (first %))) forms))
        forwards-valid? (every? (fn [[function-name expected]]
                                  (= expected (get definitions function-name)))
                                expected-forwarders)
        helpers-valid? (every? (fn [[function-name expected]]
                                 (= expected (get definitions function-name)))
                               helpers)
        valid? (and (= 'gravity.bootstrap.p15-s23.runtime (second ns-form))
                    (= '(:requires [gravity.runtime :as runtime]) requires-clause)
                    forwards-valid?
                    helpers-valid?)]
    (when-not valid?
      (p15-s23-stage2-runtime-executor-fail!
       "P15S23X002" path
       {:namespace (second ns-form)
        :requires requires-clause
        :forwarders (select-keys definitions (keys expected-forwarders))
        :compatibility-helpers (select-keys definitions (keys helpers))}
       {:requested-source requested-source
        :target target
        :missing-fact :current-runtime-compatibility-link
        :compatibility-helpers-valid? helpers-valid?
        :forwarders-valid? forwards-valid?}))
    {:schema :gravity/current-runtime-development-compatibility-link-v1
     :source-path path
     :source-byte-count byte-count
     :source-content-hash sha256
     :namespace 'gravity.bootstrap.p15-s23.runtime
     :import {:namespace 'gravity.runtime :alias 'runtime}
     :forwarders (into (sorted-map)
                       (map (fn [[function-name params]]
                              [function-name {:arity (count params)
                                              :semantic-hash
                                              (str "sha256:" (sha256-hex
                                                               (pr-str
                                                                (c-backend-canonical-value
                                                                 (get definitions function-name)))))}]))
                       forwarders)
     :compatibility-only-helpers
     (into (sorted-map)
           (map (fn [[function-name expected]]
                  [function-name
                   {:arity (count (nth expected 2))
                    :semantic-hash
                    (str "sha256:" (sha256-hex
                                     (pr-str (c-backend-canonical-value
                                              (get definitions function-name)))))}]))
           helpers)
     :status :complete
     :clojure-seed-boundary? true
     :self-hosted? false}))

(defn c-backend-current-runtime-development-source-rule!
  "Bind the current evaluator leaf plus its structurally checked P15 facade.
  This development record does not use the historical P15 runtime pins or
  make reference-runtime, deployment-runtime, or module-resolution claims."
  [source-path target emitter]
  (let [{:keys [compiler-source runtime-source compatibility-source]}
        (c-backend-current-runtime-development-source-paths)
        engine-state (hash-map 'source-path source-path 'target target)
        engine-state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_bindings_01
                      engine-state)
        engine-state (__gravity_bootstrap_runtime_source_rule_compiler_source_pinning_expressions_02
                      engine-state)
        engine-state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_bindings_03
                      engine-state)
        engine-state (__gravity_bootstrap_runtime_source_rule_runtime_kernel_definitions_expressions_04
                      engine-state)
        engine-state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_bindings_05
                      engine-state)
        engine-state (__gravity_bootstrap_runtime_source_rule_runtime_rule_linkage_expressions_06
                      engine-state)
        {:syms [runtime kernel source-data runtime-rule-record kernel-rule-record]}
        engine-state
        compiler-data source-data
        compiler-content-hash
        (str "sha256:" (sha256-hex (:source-text compiler-data)))
        runtime-source-data
        (c-backend-current-runtime-development-read-source!
         runtime-source source-path target :current-runtime-source)
        compatibility-link
        (c-backend-current-runtime-development-compatibility-link!
         source-path target compatibility-source)
        runtime-forms
        (:forms (p15-s23-compiler-source-form-record-from-text
                 (:path runtime-source-data) (:text runtime-source-data)))
        runtime-ns (first runtime-forms)
        exports-clause (some #(when (and (seq? %) (= :exports (first %))) %) (drop 2 runtime-ns))
        requires-clause (some #(when (and (seq? %) (= :requires (first %))) %) (drop 2 runtime-ns))
        ns-exports (second exports-clause)
        actual-arities
        (into {}
              (map (fn [form] [(second form) (count (nth form 2))]))
              (filter #(and (seq? %) (= 'defn (first %))) runtime-forms))
        runtime-plan
        (try
          (p15-s23-stage2-compiler-artifact-plan
           emitter (:path runtime-source-data) (:text runtime-source-data))
          (catch clojure.lang.ExceptionInfo ex (throw ex))
          (catch Exception ex
            (p15-s23-stage2-runtime-executor-fail!
             "P15S23X002" (:path runtime-source-data) nil
             {:requested-source source-path :target target
              :missing-fact :current-runtime-development-plan
              :cause-message (.getMessage ex)})))
        plan-arities (into {} (map (fn [[name f]] [name (:arity f)]))
                           (:functions runtime-plan))
        exported-arities (select-keys actual-arities
                                     c-backend-current-runtime-development-exports)
        expected-export-arities
        {'p15-s23-runtime-execute-closed-plan 1
         'p15-s23-runtime-evaluate-closed-instruction 3
         'p15-s23-runtime-evaluate-in-context 5
         'p15-s23-runtime-activate-function 5}
        source-valid? (and
                       (= 'gravity.runtime (second runtime-ns))
                       (nil? requires-clause)
                       (= c-backend-current-runtime-development-exports
                          (set ns-exports))
                       (= expected-export-arities exported-arities)
                       (= actual-arities plan-arities)
                       (= :gravity/stage2-compiler-artifact-plan
                          (:kind runtime-plan))
                       (= 'gravity.runtime (get-in runtime-plan [:module :module]))
                       (= :hosted (get-in runtime-plan [:module :profile]))
                       (= :jvm (get-in runtime-plan [:module :target]))
                       (= (:sha256 runtime-source-data)
                          (get-in runtime-plan [:source :sha256])))
        _ (when-not source-valid?
            (p15-s23-stage2-runtime-executor-fail!
             "P15S23X002" (:path runtime-source-data) runtime-plan
             {:requested-source source-path :target target
              :missing-fact :current-runtime-development-source-contract
              :namespace (second runtime-ns)
              :exports ns-exports
              :imports requires-clause
              :exported-function-arities exported-arities
              :compiled-function-arities plan-arities}))
        function-hashes
        (into (sorted-map)
              (map (fn [[name definition]]
                     [name (p15-s23-stage2-runtime-artifact-function-semantic-hash
                            definition)]))
              (:functions runtime-plan))
        closed-functions
        (select-keys (:functions runtime-plan) c-backend-current-runtime-development-exports)
        closed-function-hashes (select-keys function-hashes
                                            c-backend-current-runtime-development-exports)
        runtime-source-hash (:sha256 runtime-source-data)
        runtime-rule-input
        {:schema :gravity/current-runtime-development-binding-v1
         :runtime-source-content-hash runtime-source-hash
         :compatibility-source-content-hash (:source-content-hash compatibility-link)
         :compatibility-link (dissoc compatibility-link :source-path)
         :runtime-plan-id (:plan-id runtime-plan)
         :runtime-function-hashes function-hashes
         :runtime-exports (vec (sort ns-exports))
         :runtime-engine-hash
         (str "sha256:" (sha256-hex
                          (pr-str (c-backend-canonical-value runtime))))}
        runtime-rule-hash
        (str "sha256:" (sha256-hex
                         (pr-str (c-backend-canonical-value runtime-rule-input))))
        kernel-rule-hash
        (str "sha256:" (sha256-hex
                         (pr-str (c-backend-canonical-value kernel))))
        provider-binding
        (assoc runtime-rule-input
               :artifact :gravity/current-runtime-development-source-binding
               :runtime-source-path (:path runtime-source-data)
               :runtime-source-byte-count (:byte-count runtime-source-data)
               :compatibility-link compatibility-link
               :compiler-source-path compiler-source
               :compiler-source-content-hash compiler-content-hash
               :runtime-function-arities actual-arities
               :revision-status :development-unadmitted
               :effects :not-checked
               :clojure-seed-boundary? true
               :self-hosted? false)
        runtime-contract-validation
        {:artifact :gravity/current-runtime-development-contract-validation
         :status :complete
         :module 'gravity.runtime
         :function-count (count function-hashes)
         :export-count (count ns-exports)
         :compatibility-link-status (:status compatibility-link)
         :revision-status :development-unadmitted
         :effects :not-checked}
        runtime-artifact-hash runtime-rule-hash]
    {:runtime runtime
     :kernel kernel
     :runtime-engine (:engine runtime)
     :runtime-kernel-engine (:engine kernel)
     :runtime-rule-record runtime-rule-record
     :kernel-rule-record kernel-rule-record
     :runtime-rule-hash runtime-rule-hash
     :kernel-rule-hash kernel-rule-hash
     :runtime-kernel-rule-hash kernel-rule-hash
     :runtime-rule-source
     {:kind :gravity-source
      :sha256 runtime-source-hash
      :runtime-source {:path (:path runtime-source-data)
                       :sha256 runtime-source-hash}
      :compatibility-source
      {:path (:source-path compatibility-link)
       :sha256 (:source-content-hash compatibility-link)}
      :development-unadmitted? true
      :effects :not-checked
      :runtime-rule-hash runtime-rule-hash
      :runtime-kernel-rule-hash kernel-rule-hash}
     :runtime-artifact-source-path (:path runtime-source-data)
     :runtime-source-path (:path runtime-source-data)
     :runtime-source-content-hash runtime-source-hash
     :runtime-artifact-source-content-hash runtime-source-hash
     :runtime-artifact-source-byte-count (:byte-count runtime-source-data)
     :runtime-artifact-plan runtime-plan
     :runtime-artifact-function 'p15-s23-runtime-format-value
     :runtime-artifact-concat-function 'p15-s23-runtime-concat
     :runtime-artifact-println-function 'p15-s23-runtime-println-value
     :runtime-artifact-println-two-function 'p15-s23-runtime-println-two
     :runtime-artifact-println-over-two-boundary
     :current-runtime-closed-plan-variadic
     :runtime-artifact-closed-plan-function
     'p15-s23-runtime-execute-closed-plan
     :runtime-artifact-closed-plan-function-hash
     (get closed-function-hashes 'p15-s23-runtime-execute-closed-plan)
     :runtime-artifact-closed-plan-helper-functions
     (set (keys closed-functions))
     :runtime-artifact-closed-function-hashes closed-function-hashes
     :runtime-artifact-functions (:functions runtime-plan)
     :runtime-artifact-function-hashes function-hashes
     :runtime-contract-validation-record runtime-contract-validation
     :runtime-contract-definition-hash runtime-rule-hash
     :runtime-contract-derived-facts-hash runtime-rule-hash
     :runtime-contract-definitions {}
     :runtime-artifact-authoritative-module (:module runtime-plan)
     :runtime-artifact-providers {}
     :runtime-artifact-effects (get-in runtime-plan [:module :effects])
     :runtime-artifact-capabilities (get-in runtime-plan [:module :capabilities])
     :runtime-artifact-generic-bridge-residual? true
     :runtime-artifact-hash runtime-artifact-hash
     :runtime-artifact-hash-input runtime-rule-input
     :current-runtime-development-binding provider-binding
     :current-runtime-compatibility-link compatibility-link
     :stage2-compiler-source-path compiler-source
     :stage2-compiler-source-content-hash compiler-content-hash
     :runtime-rule-source-content-hash runtime-source-hash
     :clojure-seed-boundary? true
     :self-hosted? false
     :revision-status :development-unadmitted
     :effects :not-checked}))
