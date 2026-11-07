

(def perf2-required-claim-fields gravity.optimization/native-zero-cost-required-claim-fields)

(def perf2-required-nonempty-claim-fields gravity.optimization/native-zero-cost-required-nonempty-claim-fields)

(def perf2-required-artifacts gravity.optimization/native-zero-cost-required-artifacts)

(def perf2-abstraction-families gravity.optimization/native-zero-cost-abstraction-families)

(def perf2-cost-diagnostic gravity.optimization/native-zero-cost-cost-diagnostic)

(defn perf2-normalize-claim [claim]
  (gravity.optimization/native-zero-cost-normalize-claim claim))

(defn perf2-missing-claim-fields [claim]
  (gravity.optimization/native-zero-cost-missing-claim-fields
    #'perf-present? #'perf2-required-claim-fields #'perf2-required-nonempty-claim-fields claim))

(defn perf2-fail! [id source-path manifest performance-claim claim extra]
  (gravity.optimization/native-zero-cost-failure! #'fail! id source-path manifest performance-claim claim extra))

(defn perf2-ir-artifact-id [claim key]
  (gravity.optimization/native-zero-cost-ir-artifact-id claim key))

(defn perf2-costs-in-ir [claim ir-key]
  (gravity.optimization/native-zero-cost-costs-in-ir claim ir-key))

(defn perf2-hidden-costs [claim]
  (gravity.optimization/native-zero-cost-hidden-costs #'perf2-costs-in-ir claim))

(defn perf2-missing-evidence [claim]
  (gravity.optimization/native-zero-cost-missing-evidence #'perf-present? #'perf2-ir-artifact-id #'perf2-required-artifacts claim))

(defn perf2-validate-claim! [source-path manifest performance-claim claim]
  (gravity.optimization/validate-native-zero-cost-claim!
    #'perf2-missing-claim-fields #'perf2-hidden-costs #'perf2-missing-evidence #'perf2-fail!
    #'perf2-abstraction-families #'perf2-cost-diagnostic source-path manifest performance-claim claim))

(defn zero-cost-capability-proof [manifest performance-claim claims]
  (gravity.optimization/native-zero-cost-capability-proof #'perf-present? #'perf2-ir-artifact-id manifest performance-claim claims))
