

(defn validate-stage0-compiled-target-lowering!
  [module lowering]
  (let [source-path (:source-path module)
        backend (or (:backend lowering) (:target module))
        artifact {:lowering-request {:profile (:profile module)
                                     :target {:backend backend}}
                  :input (:input-artifact-id lowering)}
        subject (assoc lowering
                       :pass-id :stage0-compiled-target-lowering-gate
                       :source-span {:source source-path})]
    (when-not (and (contains? #{:verified-mir :verified-domain-ir}
                              (:input-kind lowering))
                   (true? (:verified? lowering)))
      (optimization-lowering-fail!
       "C14-INPUT" source-path artifact subject
       {:missing-fields [:verified-mir-or-domain-ir]
        :remediation
        "Target lowering must consume verified MIR or verified domain IR before backend artifacts are claimed."}))
    (when (and (seq (:target-metadata lowering))
               (not (stage0-compiled-target-proof-present? lowering)))
      (optimization-lowering-fail!
       "C14-PROOF-METADATA" source-path artifact subject
       {:missing-fields [:proof-id]
        :remediation
        "Attach proof or certificate metadata before preserving target-specific lowering facts."}))))