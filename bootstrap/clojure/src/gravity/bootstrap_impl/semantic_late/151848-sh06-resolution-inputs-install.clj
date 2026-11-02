; Compatibility installation; lexical algorithms live in gravity.resolution.
(defn
 sh06-resolution-analysis-inputs
 [module sh05-artifact overrides]
 (gravity.resolution/analysis-inputs
  module
  sh05-artifact
  overrides
  (resolution-stage-operations)))
