

(defn validate-stage0-compiled-target-lowering! [module lowering]
  (gravity.lowering/validate-native-stage0-target-lowering!
    {:fail! #'optimization-lowering-fail!,
     :proof-present? #'stage0-compiled-target-proof-present?}
    module
    lowering))
