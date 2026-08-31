

(defn validate-stage0-compiled-floating-literals!
  [module manifest]
  (let [floating-literals (vec (stage0-floating-literals (:forms module)))
        floating-manifests (get-in module
                                   [:metadata :math :numeric
                                    :floating-manifests])]
    (when (and (seq floating-literals) (empty? floating-manifests))
      (numeric-fail! "MATH8-MANIFEST"
                     (:source-path module)
                     manifest
                     {:operation :stage0/floating-literal
                      :manifest-id :stage0/floating-manifest
                      :profile (:profile module)
                      :target (:target module)
                      :source-span {:source (:source-path module)}}
                     {:floating-literals floating-literals
                      :missing-fields [:floating-manifests]
                      :remediation "Attach a complete MATH8 floating manifest before compiling floating arithmetic through the stage0 executable path."}))))