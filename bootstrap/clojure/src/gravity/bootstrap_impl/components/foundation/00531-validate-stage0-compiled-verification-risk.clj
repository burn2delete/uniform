

(defn validate-stage0-compiled-verification-risk!
  [module risk-record]
  (let [required (set (:minimum-evidence risk-record))
        available (set (:available-evidence risk-record))]
    (when (and (contains? #{:high :critical} (:risk risk-record))
               (not (set/subset? required available)))
      (c18-verification-fail!
       "C18-EVIDENCE" (:source-path module)
       {:stage :stage0-compiled-compiler-gate
        :pass-id (:pass risk-record)
        :version (or (:version risk-record) "stage0-p06-s1")
        :risk-class (:risk risk-record)
        :required-evidence required
        :available-evidence available
        :affected-profiles #{(:profile module)}
        :affected-targets #{(:target module)}
        :artifact-id (:artifact-id risk-record)}
       {:missing-fields
        (vec (sort-by name (set/difference required available)))}))))

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