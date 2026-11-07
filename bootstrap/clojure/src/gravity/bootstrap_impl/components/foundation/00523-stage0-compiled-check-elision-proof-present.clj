

(defn stage0-compiled-check-elision-proof-present?
  [record]
  (gravity.optimization/native-stage0-check-elision-proof-present? #'perf-present? record))

(defn validate-stage0-compiled-check-elision!
  [module record]
  (gravity.optimization/validate-native-stage0-check-elision!
    #'fail! #'stage0-compiled-check-elision-proof-present? module record))
