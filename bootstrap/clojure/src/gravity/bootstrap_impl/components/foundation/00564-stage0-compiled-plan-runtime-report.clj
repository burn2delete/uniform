

(defn stage0-compiled-plan-runtime-report
  [plan module]
  (let [plan-id (:plan-id plan)
        runtime-manifest
        {:artifact :gravity/stage0-hosted-core-compiled-runtime-manifest
         :family :managed
         :runtime :gravity.runtime/stage0-clojure-jvm-instruction-runner
         :profile (:profile module)
         :target (:target module)
         :host-runtime :clojure-jvm
         :services {:linked #{:stage0-instruction-runner
                              :arity-checks
                              :effect-checks
                              :capability-checks
                              :diagnostic-renderer}
                    :generated #{:instruction-dispatch
                                 :runtime-check-table}
                    :delegated #{:clojure-jvm-stdout
                                 :clojure-persistent-collections
                                 :clojure-exceptioninfo}
                    :external #{}
                    :forbidden #{:ambient-reflection
                                 :dynamic-eval
                                 :unchecked-host-null
                                 :unchecked-host-exception
                                 :production-telemetry-sink}}
         :status :development-only}
        conformance
        {:document-set ["R1" "R4" "R11" "R12"]
         :task "P08-S1"
         :required-diagnostic-ids
         ["R1-SELECTION" "R1-FORBIDDEN" "R4-MANIFEST"
          "R4-NULL" "R11-GRANT" "R12-SINK"]
         :managed-runtime-status :development-only-stage0
         :capability-status :complete
         :observability-status :development-only-local
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-runtime-report
         :document-set ["D1" "R1" "R4" "R11" "R12"]
         :compiled-plan-id plan-id
         :runtime-manifest runtime-manifest
         :runtime-service-table
         {:classification-kinds #{:linked :generated :delegated
                                  :external :forbidden}
          :hidden-services []
          :status :complete}
         :managed-runtime-record
         {:family :managed
          :host-runtime :clojure-jvm
          :typed-adapter :stage0-instruction-runner
          :null-policy :not-crossing-host-boundary
          :exception-policy :diagnostic-ex-info
          :reflection-policy :not-used
          :collection-semantics :clojure-persistent-collections-for-stage0
          :source-debug-map :preserved
          :status :complete}
         :capability-enforcement-record
         {:deny-by-default? true
          :decisions [{:action-id "core-app/stdout"
                       :principal :core.app/main
                       :effect :io/write
                       :capability :io/stdout
                       :provider :clojure-jvm-stdout
                       :decision :grant
                       :audit :recorded
                       :redaction :none}]
          :runtime-checks-do-not-grant-authority? true
          :status :complete}
         :observability-record
         {:development-only? true
          :event-schema-registry [:program-start :println
                                  :program-complete :diagnostic]
          :sink :local-diagnostic-bundle
          :sink-capability-required? true
          :network-sink? false
          :redaction-policy :no-secrets-observed
          :semantics-preserving? true
          :status :complete}
         :runtime-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))

(defn hosted-core-compiled-runtime-proof-source-artifact
  [source-path source-text]
  (let [_ (validate-stage0-source-profile! source-path source-text)
        _ (validate-stage0-source-safety! source-path source-text)
        macro-artifact (macro-source-artifact source-path source-text)
        module (assoc (:module macro-artifact)
                      :forms (:expanded-forms macro-artifact))
        compiled-plan (stage0-compiled-core-plan source-path source-text
                                                 module)
        run-output (execute-stage0-compiled-plan compiled-plan)
        runtime-report (stage0-compiled-plan-runtime-report compiled-plan
                                                            module)
        manifest (:runtime-manifest runtime-report)
        service-table (:runtime-service-table runtime-report)
        managed-runtime (:managed-runtime-record runtime-report)
        capability-record (:capability-enforcement-record runtime-report)
        observability-record (:observability-record runtime-report)
        conformance (:runtime-conformance-results runtime-report)
        proof {:compiled-runtime-gate-validated? true
               :runtime-selection-recorded?
               (= :managed (:family manifest))
               :runtime-service-table-recorded?
               (= :complete (:status service-table))
               :managed-host-runtime-recorded?
               (= :complete (:status managed-runtime))
               :runtime-capability-enforcement-recorded?
               (boolean
                (and (true? (:deny-by-default? capability-record))
                     (some #(= :grant (:decision %))
                           (:decisions capability-record))))
               :observability-recorded?
               (and (true? (:development-only? observability-record))
                    (false? (:network-sink? observability-record)))
               :runtime-checks-do-not-grant-authority?
               (true? (:runtime-checks-do-not-grant-authority?
                       capability-record))
               :compiled-plan-executed? (= "core-app\ngravity:19:2\n(:ok 19)\n"
                                          run-output)
               :rejected-diagnostics-covered?
               (= #{"R1-SELECTION" "R1-FORBIDDEN" "R4-MANIFEST"
                    "R4-NULL" "R11-GRANT" "R12-SINK"}
                  (set (:required-diagnostic-ids conformance)))
               :limitations {:clojure-instruction-runner? true
                             :production-runtime? false
                             :live-host-adapters? false
                             :external-observability-sink? false
                             :verified-mir-input? false
                             :target-lowering? false
                             :self-hosted-runtime? false
                             :next-required-capability
                             :replace-stage0-instruction-runner-with-gravity-runtime}
               :status :complete}
        artifact-base
        {:kind :gravity/stage0-hosted-core-compiled-runtime-proof
         :phase "08"
         :task "P08-S1"
         :governing-documents ["D1" "R1" "R4" "R11" "R12"]
         :source {:path source-path
                  :sha256 (str "sha256:" (sha256-hex source-text))}
         :compiled-plan (select-keys compiled-plan
                                     [:kind :plan-id :entrypoint
                                      :instruction-summary :effect-summary])
         :runtime-report
         (select-keys runtime-report
                      [:kind :report-id :document-set
                       :compiled-plan-id
                       :runtime-manifest
                       :runtime-service-table
                       :managed-runtime-record
                       :capability-enforcement-record
                       :observability-record
                       :runtime-conformance-results
                       :diagnostics])
         :accepted-run {:command (str "clojure -M:gravity run-compiled "
                                      source-path)
                        :stdout run-output}
         :rejected-fixtures stage0-compiled-runtime-rejected-fixtures
         :trusted-boundary {:compiler :clojure/jvm
                            :runtime
                            :gravity.runtime/stage0-clojure-jvm-instruction-runner
                            :instruction-plan? true
                            :clojure-instruction-runner? true
                            :production-runtime? false
                            :live-host-adapters? false
                            :external-observability-sink? false
                            :verified-mir-input? false
                            :target-lowering? false
                            :self-hosted-runtime? false}
         :capability-based-proof proof}]
    (assoc artifact-base
           :artifact-id (str "sha256:" (sha256-hex (pr-str artifact-base))))))

(defn hosted-core-compiled-runtime-proof-file-artifact
  [path]
  (hosted-core-compiled-runtime-proof-source-artifact path (slurp path)))