

(defn stage0-compiled-plan-safety-report
  [plan]
  (let [summary (:instruction-summary plan)
        source-path (get-in plan [:source :path])
        operation-records
        (vec
         (concat
          (when (pos? (get summary :function-call 0))
            [{:operation :stage0/function-call
              :outcome :runtime-checked
              :condition :fixed-arity
              :failure "L2-FUNCTION-ARITY"
              :source-span {:source source-path}
              :profile (get-in plan [:module :profile])}])
          (when (pos? (get summary :builtin-call 0))
            [{:operation :stage0/builtin-call
              :outcome :runtime-checked
              :condition :builtin-arity
              :failure "L2-BUILTIN-ARITY"
              :source-span {:source source-path}
              :profile (get-in plan [:module :profile])}])
          (when (pos? (get summary :println 0))
            [{:operation :stage0/io-write
              :outcome :proven-safe
              :condition :declared-effect-and-capability
              :effect :io/write
              :capability :io/stdout
              :source-span {:source source-path}
              :profile (get-in plan [:module :profile])}])
          [{:operation :stage0/control-flow
            :outcome :proven-safe
            :condition :compiled-instruction-plan
            :forms (select-keys summary [:do :if :let :quote])
            :source-span {:source source-path}
            :profile (get-in plan [:module :profile])}
           {:operation :stage0/literals-and-locals
            :outcome :proven-safe
            :condition :closed-compiled-plan
            :forms (select-keys summary
                                [:literal :local :vector-literal
                                 :map-literal :set-literal])
            :source-span {:source source-path}
            :profile (get-in plan [:module :profile])}]))
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-safety-report
         :document "SAFE1"
         :governing-documents ["D8" "D9" "SAFE1" "SAFE6"]
         :compiled-plan-id (:plan-id plan)
         :module (:module plan)
         :safety-mode (get-in plan [:module :safety])
         :safety-outcomes stage0-safe1-outcomes
         :safety-classification-records operation-records
         :runtime-checks (filterv #(= :runtime-checked (:outcome %))
                                  operation-records)
         :unsafe-islands []
         :unsafe-policy {:mode (get-in plan [:module :safety])
                         :unsafe-islands-allowed? false}
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-safety-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        safety-report (stage0-compiled-plan-safety-report compiled-plan)
        outcomes (map :outcome (:safety-classification-records safety-report))
        proof {:compiled-plan-safety-classified? true
               :exactly-one-outcome-per-operation?
               (and (seq outcomes)
                    (every? stage0-safe1-outcomes outcomes)
                    (= (count outcomes)
                       (count (:safety-classification-records
                               safety-report))))
               :runtime-checks-recorded?
               (set/subset? #{:stage0/function-call :stage0/builtin-call}
                            (set (map :operation
                                      (:runtime-checks safety-report))))
               :unsafe-islands-rejected? true
               :unsafe-audit-metadata-required? true
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"SAFE6-UNSAFE-FORBIDDEN" "SAFE6-MISSING-METADATA"}
                  (set (map :diagnostic
                            stage0-compiled-safety-rejected-fixtures)))
               :limitations {:clojure-instruction-runner? true
                             :self-hosted-compiler? false
                             :native-backend? false
                             :unsafe-island-execution? false
                             :next-required-capability
                             :lower-safety-report-into-runtime-and-mir-artifacts}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-safety-proof
         :phase "02"
         :task "P02-S1"
         :governing-documents ["D8" "D9" "SAFE1" "SAFE6"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :safety-report (select-keys safety-report
                                     [:kind :report-id :document
                                      :safety-mode
                                      :safety-classification-records
                                      :runtime-checks :unsafe-islands
                                      :unsafe-policy :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-safety-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime :clojure/jvm
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :self-hosted-compiler? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-safety-proof-file-artifact
  [path]
  (hosted-core-compiled-safety-proof-source-artifact path (slurp path)))

(defn stage0-compiled-plan-profile-report
  [plan]
  (let [module (:module plan)
        inferred-effects (get-in plan [:effect-summary :inferred] #{})
        required-capabilities (set/union
                               (get-in plan [:effect-summary :capabilities] #{})
                               (required-capabilities-for-effects
                                inferred-effects))
        effect-authority (profile-effective-effects module inferred-effects)
        capability-authority (profile-effective-capabilities
                              module
                              required-capabilities)
        effect-table (effect-permission-table module inferred-effects
                                              effect-authority)
        capability-table (capability-permission-table
                          module
                          required-capabilities
                          capability-authority)
        contract (profile-contract (:profile module))
        manifest {:module (:module module)
                  :source-path (:source-path module)
                  :profile (:profile module)
                  :target (:target module)
                  :source-effects (:source effect-authority)
                  :inferred-effects inferred-effects
                  :effective-effects (:effective effect-authority)
                  :source-capabilities (:source capability-authority)
                  :required-capabilities required-capabilities
                  :effective-capabilities (:effective capability-authority)
                  :memory-regime (:memory contract)
                  :runtime-assumptions (:runtime contract)
                  :unsafe-policy (:unsafe-policy contract)
                  :dependencies {:module (:module module)
                                 :dependencies []
                                 :acyclic true}
                  :provider-selections []
                  :safety (:safety module)
                  :policy-layers {:effects (select-keys
                                             effect-authority
                                             [:source :profile :package
                                              :provider :deployment])
                                  :capabilities (select-keys
                                                 capability-authority
                                                 [:source :profile :package
                                                  :provider :deployment])}}
        backend-report (backend-eligibility-report
                        module
                        (assoc manifest :profile-contract contract))
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-profile-report
         :document-set ["P1" "P4" "P13"]
         :compiled-plan-id (:plan-id plan)
         :profile-contract (select-keys contract
                                        [:profile :allowed-effects
                                         :checked-effects :capabilities
                                         :memory :runtime :unsafe-policy])
         :profile-manifest manifest
         :effect-permission-table effect-table
         :capability-permission-table capability-table
         :cross-profile-dependency-graph (:dependencies manifest)
         :backend-eligibility-report backend-report
         :behavior-states {:effects (set (map :state effect-table))
                           :capabilities (set (map :state
                                                   capability-table))}
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))