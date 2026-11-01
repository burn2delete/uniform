

(declare p15-s23-stage2-runtime-artifact-expected-source-content-hash)

(defn p15-s23-stage2-construct-emitted-core-plan
  "Construct the stage2 plan envelope from an already parsed module and its
  authoritative function table.  Policy/admission validation deliberately
  remains in the public emitted-core wrapper below; authenticated private
  compiler stages use this construction seam so Gravity owns later policy."
  [emitter source-path source-text module function-table]
  (let [runtime-artifact-source?
        (and (= 'gravity.bootstrap.p15-s23.runtime (:module module))
             (= p15-s23-stage2-runtime-artifact-expected-source-content-hash
                (str "sha256:" (sha256-hex source-text))))
        ;; Attempt24's bounded cross-plan machine is part of the exact pinned
        ;; runtime artifact and uses the same closed collection/predicate
        ;; surface as compiler artifacts.  Do not expose that augmentation to
        ;; another runtime spelling or an ordinary hosted module.
        emitter (if runtime-artifact-source?
                  (p15-s23-stage2-compiler-artifact-augmented-emitter emitter)
                  emitter)
        module (assoc module :function-table function-table)
        source-authority
        (or (:p15-s23-sh07-source-authority module)
            (when (:p15-s23-sh07-module-source-descriptor module)
              (p15-s23-sh07-compiler-source-authority
               source-path source-text module function-table)))
        module (cond-> module
                 source-authority
                 (assoc :p15-s23-sh07-source-authority source-authority))
        compiler-artifact-binding
        (or (:p15-s23-sh07-compiler-artifact-binding module)
            *p15-s23-stage2-compiler-artifact-binding*
            (p15-s23-stage2-compiler-artifact-binding!
             emitter source-path (:target module)))
        ;; A source map is not an ordering contract.  Give the Gravity
        ;; assembler an explicitly stable function sequence so binding-table
        ;; order and recursive summary traversal are root/runtime neutral.
        ordered-definitions (mapv (fn [[name definition]]
                                    [name definition])
                                  (sort-by key function-table))
        assembled-products
        (binding [*p15-s23-stage2-compiler-artifact-binding*
                  compiler-artifact-binding]
          (p15-s23-stage2-assemble-plan-products
           emitter module ordered-definitions))
        functions (:functions assembled-products)
        ;; The Gravity artifact owns the authoritative plan summary.  Stage0
        ;; independently recomputes the same additive tree facts below as
        ;; migration evidence; it is not the product source.
        instruction-summary (:instruction-summary assembled-products)
        plan-base {:kind (get-in emitter
                                 [:plan-shape :kind]
                                 :gravity/stage2-hosted-core-compiled-plan)
                   :compatibility-kind
                   (get-in emitter [:plan-shape :compatibility-kind])
                   :entrypoint (get-in emitter [:plan-shape :entrypoint] 'main)
                   :source {:path source-path
                            :sha256 (str "sha256:" (sha256-hex source-text))}
                   :compiler (merge
                              (get-in emitter [:plan-shape :compiler])
                              {:rule-engine (:engine emitter)
                               :rule-source :p15-s23-stage2-plan-emitter
                               :expression-lowering-owner :gravity-source
                               :expression-lowering-artifact-hash
                               (:artifact-hash compiler-artifact-binding)
                               :expression-lowering-source-content-hash
                               (:source-content-hash compiler-artifact-binding)
                               :expression-lowering-semantic-hash
                               (:semantic-hash compiler-artifact-binding)
                               :expression-lowering-invoked? true
                               :expression-lowering-generic-bridge-residual?
                               true
                               :plan-assembly-owner :gravity-source
                               :plan-assembly-function
                               p15-s23-stage2-compiler-artifact-plan-assembly-function
                               :plan-assembly-artifact-hash
                               (:artifact-hash compiler-artifact-binding)
                               :plan-assembly-source-content-hash
                               (:source-content-hash compiler-artifact-binding)
                               :plan-assembly-semantic-hash
                               (:semantic-hash compiler-artifact-binding)
                               :plan-assembly-invoked? true
                               :plan-assembly-generic-bridge-residual? true
                               :retirement-objective
                               :retire-clojure-seed}
                              (when (and (identical? Long (class (:source-contract-version compiler-artifact-binding)))
                                         (== 2 (.longValue ^Long (:source-contract-version compiler-artifact-binding))))
                                {:expression-lowering-source-contract-version 2
                                 :expression-lowering-functions-semantic-hash
                                 (:functions-semantic-hash compiler-artifact-binding)
                                 :expression-lowering-function-count
                                 (:function-count compiler-artifact-binding)}))
                   :module (select-keys module
                                        [:module :source-path :profile :target
                                         :effects :capabilities :providers
                                         :exports :safety])
                   :binding-table (:binding-table assembled-products)
                   :functions functions
                   :instruction-summary instruction-summary
                   :effect-summary (:effect-summary assembled-products)
                   :diagnostics []}
        identity-base
        (-> plan-base
            (update :source dissoc :path)
            (update :module dissoc :source-path))]
    (assoc plan-base
           :plan-id
           (c4-artifact-id (c-backend-canonical-value identity-base)))))

(defn p15-s23-stage2-emitted-core-plan
  [emitter source-path source-text module]
  (let [function-table (stage0-function-table module)
        module (assoc module :function-table function-table)
        _ (validate-stage0-compiled-profile! module)
        _ (executable-profile! source-path module (:forms module))
        _ (validate-module-effects! module)
        _ (validate-stage0-executable-safety! module)
        _ (validate-stage0-compiled-performance! module)
        _ (validate-stage0-compiled-math! module)
        _ (validate-stage0-compiled-compiler! module)
        _ (validate-stage0-compiled-backend! module)
        _ (validate-stage0-compiled-runtime! module)
        _ (validate-stage0-compiled-domain! module)
        _ (validate-stage0-compiled-schema! module)
        _ (validate-stage0-compiled-ai! module)
        _ (validate-stage0-compiled-package! module)
        _ (validate-stage0-compiled-tooling! module)
        _ (validate-stage0-compiled-conformance! module)]
    (p15-s23-stage2-construct-emitted-core-plan
     emitter source-path source-text module function-table)))

(declare p15-s23-stage2-c2-c3-front-end-products)
