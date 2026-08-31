

(def numeric-default-efir-annotations
  [{:graph-id :efir/sin-fast
    :typed-core-anchor :typed/math-sin-fast
    :numeric-family :float
    :numeric-mode :certified-approx
    :precision {:type :F32 :absolute-error-max 1.0e-5}
    :branch-policy :real-only
    :source-span "math/numeric.gravity:13:1"}])