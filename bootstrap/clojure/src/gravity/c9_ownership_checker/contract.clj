(ns gravity.c9-ownership-checker.contract
  "Hosted C9 leaf contract projection.")

(defn engine-contract [operation-keys public-api]
  {:contract-boundary :hosted-stage0-c9-ownership-checker
   :artifact-inputs [:c8-effect-checker-artifact :module-context]
   :artifact-outputs [:ownership-graph :borrow-graph :lifetime-interval-map
                      :move-consume-records :escape-analysis-report
                      :region-lifetime-graph :arena-generation-graph
                      :linear-resource-flow-graph :transfer-records
                      :runtime-check-records :unsafe-audit-references
                      :ownership-diagnostics]
   :owns [:hosted-stage0-c9-ownership-analysis :hosted-stage0-c9-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c9-authority :source-authentication
                  :effect-checking-authority :ownership-safety-authority
                  :region-provider-authority :arena-provider-authority
                  :linear-resource-provider-authority :runtime-check-authority
                  :safety-analysis :mir-construction :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true :override-driven-diagnostics? true
   :ownership-model-complete? false :canonical-c9-authority? false
   :operation-interposition {:accepted-keys operation-keys
                             :unknown-keys-rejected? true :partial-overrides? true
                             :single-binding-per-top-level-call? true}
   :public-api public-api})
