

(def perf1-required-claim-fields gravity.optimization/native-performance-required-claim-fields)

(defn perf-present? [value]
  (gravity.optimization/native-performance-present? value))

(defn perf1-normalize-claim [claim]
  (gravity.optimization/native-performance-normalize-claim claim))

(defn perf1-missing-claim-fields [claim]
  (gravity.optimization/native-performance-missing-claim-fields #'perf-present? #'perf1-required-claim-fields claim))

(defn perf1-fail! [id source-path manifest claim extra]
  (gravity.optimization/native-performance-failure! #'fail! id source-path manifest claim extra))

(defn perf1-target-fingerprint [claim]
  (gravity.optimization/native-performance-target-fingerprint claim))

(defn perf1-validate-claim! [source-path manifest performance claim]
  (gravity.optimization/validate-native-performance-claim!
    #'perf1-missing-claim-fields #'perf1-target-fingerprint #'perf-present? #'perf1-fail!
    source-path manifest performance claim))
