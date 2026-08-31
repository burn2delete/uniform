

(defn stage0-compiled-compiler-manifest
  [module]
  {:profile (:profile module)
   :target (:target module)
   :source-effects (:effects module)
   :source-capabilities (:capabilities module)
   :metadata (:metadata module)})

(defn stage0-compiled-compiler-suite
  [module]
  (get-in module [:metadata :compiler :compiled-gate] {}))

(defn stage0-compiled-compiler-suite-present?
  [module]
  (contains? (get-in module [:metadata :compiler] {}) :compiled-gate))

(defn validate-stage0-compiled-pass-contract!
  [module manifest contract]
  (let [source-path (:source-path module)
        missing-fields (compiler-pass-missing-fields
                        contract
                        compiler-pass-contract-required-fields)]
    (if (seq missing-fields)
      (compiler-pass-fail!
       "C1-PASS-CONTRACT" source-path manifest contract
       {:missing-fields missing-fields
        :remediation
        "Expose the compiled executable pass contract before claiming compiler architecture coverage."})
      (let [durable-drops (set/intersection compiler-pass-durable-facts
                                            (set (:invalidates contract)))
            replacements (set (concat (:regenerates contract)
                                      (:replacement-evidence contract)
                                      (:emits contract)))
            missing-replacements (set/difference durable-drops
                                                 replacements)]
        (when (seq missing-replacements)
          (compiler-pass-fail!
           "C1-EVIDENCE-DROP" source-path manifest contract
           {:missing-fields (vec missing-replacements)
            :remediation
            "Regenerate durable facts, emit replacement proof, keep runtime checks, or reject the transformation."}))))))

(defn validate-stage0-compiled-mir!
  [module manifest mir-operation]
  (when (= :target-specific (:family mir-operation))
    (mir-fail!
     "C11-TARGET-LEAK"
     (:source-path module)
     {:mir-module {:profile (:profile module)
                   :target-request (:target module)
                   :module :stage0-compiled-core-app}}
     mir-operation
     {:missing-fields [:target-independent-opcode]
      :remediation
      "Keep generic MIR target-independent; move target opcodes behind verified target lowering."})))

(defn stage0-compiled-target-proof-present?
  [record]
  (or (perf-present? (:proof-id record))
      (perf-present? (:proof record))
      (perf-present? (:certificate record))
      (perf-present? (:certificate-id record))
      (perf-present? (:translation-validation record))))