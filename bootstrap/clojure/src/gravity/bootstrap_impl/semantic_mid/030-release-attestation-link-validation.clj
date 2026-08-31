(defn- semantic-mid-validate-release-attestation-links!
  [{:keys [reader-source-path release-attestation diagnostics
           missing-diagnostics]}]
  (when-not (set/subset?
             #{:stage1-reader-release-attestation-seed-retirement
               :stage1-reader-diverse-bootstrap-verification
               :stage1-reader-verified-boot-chain :stage1-reader-runtime-image
               :stage1-reader-runtime-entrypoint :stage1-reader-compiler-driver
               :stage1-reader-core-bootstrap-runtime
               :stage1-reader-self-hosted-runtime
               :stage1-reader-source-runtime}
             (set (:uses-runtimes release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:uses-runtimes]}))
  (when-not (set/subset? #{:stage1-reader-core-bootstrap-builtins}
                         (set (:uses-builtins release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:uses-builtins]}))
  (when-not (set/subset? #{:stage1-reader-token-automaton-executor
                           :stage1-reader-form-builder-executor}
                         (set (:uses-executors release-attestation)))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:uses-executors]}))
  (when-not (= :gravity-source
               (get-in release-attestation [:provenance :owner]))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:provenance :owner]}))
  (when-not (= :reader-release-attestation-seed-retirement-assumption-replacement
               (get-in release-attestation [:provenance :purpose]))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:provenance :purpose]}))
  (when-not (= :replace-physical-supply-chain-and-independent-diversity-assumptions-with-release-attestation-and-seed-retirement
               (get-in release-attestation
                       [:provenance :retirement-objective]))
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path release-attestation
     {:missing-fields [:provenance :retirement-objective]}))
  (when (seq missing-diagnostics)
    (stage1-reader-release-attestation-seed-retirement-fail!
     "STAGE1REL010" reader-source-path diagnostics
     {:missing-fields (vec missing-diagnostics)})))
