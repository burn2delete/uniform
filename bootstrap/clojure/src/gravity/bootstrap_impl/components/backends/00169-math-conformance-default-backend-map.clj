

(def math-conformance-default-backend-map
  [{:candidate-id :candidate/fused-poly-7
    :target :jvm
    :feature-guards #{:portable}
    :backend-preserves? true
    :residual-checks [:domain :target-feature]
    :lowering :mir-domain-ir}])