

(defn stage0-compiled-compiler-manifest [module]
  (gravity.compiler-pass-manifest/native-stage0-compiler-manifest
    module))

(defn stage0-compiled-compiler-suite [module]
  (gravity.compiler-pass-manifest/native-stage0-compiler-suite module))

(defn stage0-compiled-compiler-suite-present? [module]
  (gravity.compiler-pass-manifest/native-stage0-compiler-suite-present?
    module))

(defn validate-stage0-compiled-pass-contract! [module
                                               manifest
                                               contract]
  (gravity.compiler-pass-manifest/validate-native-stage0-pass-contract!
    {:missing-fields #'compiler-pass-missing-fields,
     :required-fields #'compiler-pass-contract-required-fields,
     :durable-facts #'compiler-pass-durable-facts,
     :intersection #'set/intersection,
     :difference #'set/difference,
     :fail! #'compiler-pass-fail!}
    module
    manifest
    contract))

(defn validate-stage0-compiled-mir! [module manifest mir-operation]
  (gravity.mir/validate-native-stage0-mir-operation!
    #'mir-fail!
    module
    manifest
    mir-operation))

(defn stage0-compiled-target-proof-present? [record]
  (gravity.lowering/native-stage0-target-proof-present?
    #'perf-present?
    record))
