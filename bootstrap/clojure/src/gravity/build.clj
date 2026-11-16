(ns gravity.build
  "Transport for two Uniform Stage0 metadata predicates; no build execution or admission.")

(def ^:private namespace-contract
  {:namespace 'gravity.build
   :contract-boundary :stage0-build-metadata-checks
   :governing-contracts '[D1 D2 D3 D8 D9 PKG1 PKG2 PKG3 PKG6 PKG7 PKG10 PKG11 C1 C15 C16 BOOT8 TEST2]
   :public-api {'check-graph-effects! {:arglists '([execute-policy! fail-var module build-graph]) :admission :none}
                'check-offline-recipe! {:arglists '([execute-policy! fail-var module reproducible-build-recipe]) :admission :none}}
   :artifact-inputs [:trusted-native-module :supplied-build-graph :supplied-recipe :diagnostic-var
                     :current-source-uniform-execution]
   :artifact-outputs [:nil-on-accepted-metadata :unchanged-pkg2001-or-pkg7003-failure]
   :diagnostic-ownership {:decisions ["PKG2001" "PKG7003"]
                          :decision-owner :uniform-build-metadata-predicates
                          :construction :explicit-existing-owner-var
                          :unchanged [:message :source-span :subject :missing-fields :stage :severity]}
   :dependency-direction {:requires [] :consumed-by ['gravity.bootstrap]
                          :forbids ['gravity.bootstrap 'gravity.cli 'gravity.release
                                    'gravity.build.invocation 'gravity.build.declared-inputs]}
   :supported [:declared-node-effect-subset :disabled-recipe-network]
   :unsupported [:general-build-validation :input-authentication :capability-grants :build-execution
                 :dependency-resolution :cache-reuse :reproducibility-proof :signing :sbom
                 :release :migration :self-hosting :seed-retirement]
   :authenticates-inputs? false :performs-io? false :issues-certificates? false
   :test-owner 'gravity.build.pipeline-test :bootstrap-hosted? true
   :clojure-seed-boundary? true :self-hosted? false})

(defn- policy-result! [execute-policy! predicate arguments]
  (when-not (ifn? execute-policy!)
    (throw (ex-info "Uniform build metadata execution is unavailable"
                    {:id "BUILD-METADATA-POLICY" :reason :execution-unavailable})))
  (let [result (execute-policy! predicate arguments)]
    (when-not (boolean? result)
      (throw (ex-info "Uniform build metadata predicate must return a boolean"
                      {:id "BUILD-METADATA-POLICY" :reason :nonboolean-result
                       :predicate predicate :result result})))
    result))

(defn check-graph-effects!
  [execute-policy! fail-var module build-graph]
  (when-not (policy-result! execute-policy! 'build-graph-effects-declared?
                            [(:declared-effects build-graph)
                             (mapv :effects (:nodes build-graph))])
    ((deref fail-var)
     "PKG2001" module build-graph
     {:missing-fields [:declared-effects]})))

(defn check-offline-recipe!
  [execute-policy! fail-var module reproducible-build-recipe]
  (when-not (policy-result! execute-policy! 'offline-build-recipe?
                            [(get-in reproducible-build-recipe [:environment :network])])
    ((deref fail-var)
     "PKG7003" module reproducible-build-recipe
     {:missing-fields [:controlled-network]})))

(doseq [operation [#'check-graph-effects! #'check-offline-recipe!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
