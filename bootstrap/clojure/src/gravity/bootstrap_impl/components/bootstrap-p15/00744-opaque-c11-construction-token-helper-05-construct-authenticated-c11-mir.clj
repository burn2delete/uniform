(def ^:private __gravity_bootstrap_lexical_119416_construct-authenticated-c11-mir
  (let [opaque-c11-construction-token (nth __gravity_bootstrap_lexical_values_119416 0)
        classify-checked-core-ingress-pair __gravity_bootstrap_lexical_119416_classify-checked-core-ingress-pair
        invoke-pinned-c11-builder! __gravity_bootstrap_lexical_119416_invoke-pinned-c11-builder]
    (fn construct-authenticated-c11-mir!
      [checked-core context ingress token]
      (when-not (identical? opaque-c11-construction-token token)
        (p15-s23-c11-mir-fail!
         "C11-VERIFY" (:source-path context) {}
         {:missing-fact :opaque-c11-constructor-entry}))
      (let [source-path (:source-path context)
            binding (p15-s23-c11-mir-source-binding!
                     source-path (:requested-target context))
            constructed
            (invoke-pinned-c11-builder!
             checked-core context binding :authoritative-build token)
            verifier
            (p15-s23-c11-mir-validate-constructed!
             source-path checked-core constructed)
            artifact
            (p15-s23-c11-mir-final-artifact-base
             checked-core context ingress binding constructed verifier)]
        (p15-s23-c11-mir-validate-final-artifact!
         artifact checked-core context)
        artifact))))
