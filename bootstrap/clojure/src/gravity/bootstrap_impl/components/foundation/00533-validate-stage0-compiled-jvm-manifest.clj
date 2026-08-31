

(defn validate-stage0-compiled-jvm-manifest!
  [module manifest]
  (let [missing-fields (compiler-pass-missing-fields
                        manifest
                        stage0-compiled-jvm-manifest-required-fields)]
    (when (seq missing-fields)
      (hosted-lowering-fail!
       "B5-MANIFEST" (:source-path module)
       {:stage :stage0-compiled-jvm-backend-gate
        :backend :gravity.backend/jvm
        :target :jvm
        :artifact-id (:artifact-id manifest)
        :host-symbol :stage0-compiled-core-app
        :missing-evidence missing-fields}
       {:missing-fields missing-fields
        :remediation
        "A JVM backend artifact claim must record classfile/runtime/package/module, host boundary, source map, and manifest metadata."})))
  (doseq [flow (:null-flow manifest)]
    (when (and (not (true? (:checked? flow)))
               (not (contains? #{:option :result :opaque-checked}
                               (:wrapper flow))))
      (hosted-lowering-fail!
       "B5-NULL" (:source-path module)
       {:stage :stage0-compiled-jvm-backend-gate
        :backend :gravity.backend/jvm
        :target :jvm
        :artifact-id (:artifact-id manifest)
        :host-symbol (:host-symbol flow)
        :missing-evidence [:nullability-wrapper]
        :target-construct (:target-construct flow)}
       {:missing-fields [:nullability-wrapper]
        :remediation
        "Java null must cross into safe Gravity only through Option, Result, checked wrappers, or opaque checked host values."}))))

(defn stage0-compiled-artifact-provenance-complete?
  [artifact]
  (let [provenance (:provenance artifact)]
    (and (:source provenance)
         (:compiler provenance)
         (seq (:passes provenance))
         (:dependencies provenance))))