(defn- semantic-mid-validate-formal-governance-links!
  [{:keys [reader-source-path formal-governance diagnostics
           missing-diagnostics]}]
  (when-not (set/subset?
             #{:stage1-reader-formal-release-governance-seed-retirement
               :stage1-reader-release-attestation-seed-retirement
               :stage1-reader-diverse-bootstrap-verification
               :stage1-reader-verified-boot-chain :stage1-reader-runtime-image
               :stage1-reader-runtime-entrypoint :stage1-reader-compiler-driver
               :stage1-reader-core-bootstrap-runtime
               :stage1-reader-self-hosted-runtime
               :stage1-reader-source-runtime}
             (set (:uses-runtimes formal-governance)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:uses-runtimes]}))
  (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                         (set (:uses-builtins formal-governance)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:uses-builtins]}))
  (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                           :stage1-reader-form-builder-executor}
                         (set (:uses-executors formal-governance)))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:uses-executors]}))
  (when-not (= :gravity-source
               (get-in formal-governance [:provenance :owner]))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:provenance :owner]}))
  (when-not (= :reader-formal-release-governance-assumption-replacement
               (get-in formal-governance [:provenance :purpose]))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:provenance :purpose]}))
  (when-not (= :replace-human-release-governance-and-deployment-custody-with-formal-release-governance-while-keeping-full-compiler-self-hosting-explicit
               (get-in formal-governance
                       [:provenance :retirement-objective]))
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path formal-governance
     {:missing-fields [:provenance :retirement-objective]}))
  (when (seq missing-diagnostics)
    (stage1-reader-formal-release-governance-seed-retirement-fail!
     "STAGE1GOV010" reader-source-path diagnostics
     {:missing-fields (vec missing-diagnostics)})))
