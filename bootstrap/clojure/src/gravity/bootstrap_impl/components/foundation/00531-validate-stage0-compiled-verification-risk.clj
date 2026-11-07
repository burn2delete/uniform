

(defn validate-stage0-compiled-verification-risk! [module risk-record]
  (gravity.compiler-pass-manifest/validate-native-stage0-verification-risk!
    {:subset? #'set/subset?,
     :difference #'set/difference,
     :fail! #'c18-verification-fail!}
    module
    risk-record))

(defn validate-stage0-compiled-compiler!
  [module]
  (when (stage0-compiled-compiler-suite-present? module)
    (let [manifest (stage0-compiled-compiler-manifest module)
          suite (stage0-compiled-compiler-suite module)]
      (doseq [contract (:pass-contracts suite)]
        (validate-stage0-compiled-pass-contract! module manifest contract))
      (doseq [mir-operation (:mir-operations suite)]
        (validate-stage0-compiled-mir! module manifest mir-operation))
      (doseq [lowering (:target-lowering suite)]
        (validate-stage0-compiled-target-lowering! module lowering))
      (doseq [risk-record (:verification-risk suite)]
        (validate-stage0-compiled-verification-risk! module risk-record)))))

(def stage0-compiled-jvm-manifest-required-fields
  [:backend :target :classfile :runtime :packaging :module-system
   :classloader-policy :reflection-policy :exception-policy
   :nullability-policy :runtime-provider :artifact-kind
   :source-debug-map])