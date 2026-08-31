

(defn profile-compliance-capability-record
  [artifact]
  (let [matrix (:effect-capability-matrix artifact)
        manifest (:profile-manifest artifact)
        capability-proof (or (get-in artifact
                                     [:profile-validation-report
                                      :capability-based-proof])
                             (:capability-based-proof artifact))]
    {:profile (profile-compliance-artifact-profile artifact)
     :document (profile-compliance-active-document artifact)
     :effective-effects (or (get-in matrix [:effects :effective])
                            (:effective-effects manifest)
                            #{})
     :effective-capabilities (or (get-in matrix [:capabilities :effective])
                                 (:effective-capabilities manifest)
                                 #{})
     :effect-permission-table-present?
     (boolean (or (:effect-permission-table artifact)
                  (get-in matrix [:effects :permission-table])))
     :capability-permission-table-present?
     (boolean (or (:capability-permission-table artifact)
                  (get-in matrix [:capabilities :permission-table])))
     :capability-proof-status (:status capability-proof)
     :status :complete}))

(defn profile-compliance-capture-accepted
  [spec]
  (let [source-path (profile-compliance-fixture-path :accepted
                                                    (:fixture spec))
        artifact (profile-compliance-run-artifact (:stage spec) source-path)
        actual-profile (profile-compliance-artifact-profile artifact)
        actual-document (profile-compliance-active-document artifact)]
    (when-not (= (:profile spec) actual-profile)
      (fail! "P03-COMPLIANCE-PROFILE"
             "profile compliance fixture emitted the wrong profile"
             {:source-span {:source source-path}
              :profile actual-profile
              :expected-profile (:profile spec)
              :fixture-id (:fixture spec)
              :remediation "Use an accepted fixture whose active profile matches the suite contract."}))
    (when-not (= (:document spec) actual-document)
      (fail! "P03-COMPLIANCE-DOCUMENT"
             "profile compliance fixture emitted the wrong document proof"
             {:source-span {:source source-path}
              :document-id actual-document
              :expected-document (:document spec)
              :fixture-id (:fixture spec)
              :remediation "Route the fixture through the artifact command owned by the governing profile document."}))
    {:fixture (:fixture spec)
     :source-path source-path
     :stage (:stage spec)
     :expected-profile (:profile spec)
     :actual-profile actual-profile
     :document actual-document
     :artifact-kind (:kind artifact)
     :artifact-hash (str "sha256:" (sha256-hex (pr-str artifact)))
     :capability-record (profile-compliance-capability-record artifact)
     :status :accepted}))

(defn profile-compliance-capture-rejected
  [source-path]
  (let [fixture-name (.getName (java.io.File. source-path))
        stage (profile-compliance-rejected-stage fixture-name)]
    (try
      (let [artifact (profile-compliance-run-artifact stage source-path)]
        (fail! "P03-COMPLIANCE-UNEXPECTED-ACCEPT"
               "profile compliance rejected fixture was accepted"
               {:source-span {:source source-path}
                :fixture-id fixture-name
                :actual-outcome (:kind artifact)
                :remediation "Add or tighten the governing profile diagnostic so this fixture rejects before backend lowering."}))
      (catch clojure.lang.ExceptionInfo ex
        (let [data (ex-data ex)]
          (if (= "P03-COMPLIANCE-UNEXPECTED-ACCEPT" (:id data))
            (throw ex)
            {:fixture fixture-name
             :source-path source-path
             :stage stage
             :diagnostic-id (:id data)
	             :diagnostic-family (:diagnostic-family data)
	             :profile (or (:active-profile data) (:profile data))
	             :target (:target data)
	             :message (:message data)
	             :status :rejected-before-backend-lowering}))))))

(defn profile-compliance-conformance-results
  [accepted-results rejected-results]
  (let [covered-profiles (set (keep :actual-profile accepted-results))
        missing-profiles (set/difference (set standard-profile-order)
                                         covered-profiles)
        covered-documents (set (keep :document accepted-results))
        missing-documents (set/difference
                           (set profile-compliance-required-documents)
                           covered-documents)
        covered-diagnostic-ids (set (keep :diagnostic-id rejected-results))
        required-diagnostic-ids (set profile-compliance-required-diagnostic-ids)
        missing-diagnostic-ids (set/difference required-diagnostic-ids
                                               covered-diagnostic-ids)
        unexpected-diagnostic-ids (set/difference covered-diagnostic-ids
                                                  required-diagnostic-ids)
        complete? (and (empty? missing-profiles)
                       (empty? missing-documents)
                       (empty? missing-diagnostic-ids)
                       (empty? unexpected-diagnostic-ids))]
    {:phase "P03"
     :task "P03-T06"
     :required-profiles standard-profile-order
     :covered-profiles covered-profiles
     :missing-profiles missing-profiles
     :required-documents profile-compliance-required-documents
     :covered-documents covered-documents
     :missing-documents missing-documents
     :required-diagnostic-ids profile-compliance-required-diagnostic-ids
     :covered-diagnostic-ids covered-diagnostic-ids
     :missing-diagnostic-ids missing-diagnostic-ids
     :unexpected-diagnostic-ids unexpected-diagnostic-ids
     :accepted-fixture-count (count accepted-results)
     :rejected-fixture-count (count rejected-results)
     :diagnostics-before-backend-lowering?
     (every? #(= :rejected-before-backend-lowering (:status %))
             rejected-results)
     :status (if complete? :complete :incomplete)}))

(defn profile-compliance-capability-proof
  [accepted-results rejected-results conformance]
  {:profile-authority-covered?
   (empty? (:missing-profiles conformance))
   :document-authority-covered?
   (empty? (:missing-documents conformance))
   :diagnostic-authority-covered?
   (empty? (:missing-diagnostic-ids conformance))
   :unexpected-diagnostics-absent?
   (empty? (:unexpected-diagnostic-ids conformance))
   :accepted-capability-records (mapv :capability-record accepted-results)
   :rejected-diagnostic-records
   (mapv #(select-keys % [:fixture :stage :diagnostic-id :profile :target
                          :status])
         rejected-results)
   :accepted-fixture-count (count accepted-results)
   :rejected-fixture-count (count rejected-results)
   :pre-backend-lowering-proof
   {:profile-validation-before-backend-lowering? true
    :backend-lowering-artifacts-emitted? false
    :rejected-fixtures (count rejected-results)}
   :status (:status conformance)})

(defn profile-compliance-require-complete!
  [source-path conformance]
  (when-not (= :complete (:status conformance))
    (fail! "P03-COMPLIANCE-INCOMPLETE"
           "profile compliance fixture suite is incomplete"
           {:source-span {:source source-path}
            :missing-profiles (:missing-profiles conformance)
            :missing-documents (:missing-documents conformance)
            :missing-diagnostic-ids (:missing-diagnostic-ids conformance)
            :unexpected-diagnostic-ids (:unexpected-diagnostic-ids conformance)
            :remediation "Add accepted and rejected profile fixtures until every Phase 03 profile document is covered."})))

(defn profile-compliance-source-artifact
  [source-path source-text]
  (let [suite-source-artifact (profile-manifest-source-artifact source-path
                                                                source-text)
        accepted-results (mapv profile-compliance-capture-accepted
                               profile-compliance-accepted-fixtures)
        rejected-results (mapv profile-compliance-capture-rejected
                               (profile-compliance-rejected-fixture-paths))
        conformance (profile-compliance-conformance-results accepted-results
                                                            rejected-results)
        capability-proof (profile-compliance-capability-proof accepted-results
                                                              rejected-results
                                                              conformance)
        _ (profile-compliance-require-complete! source-path conformance)]
    {:kind :gravity/stage0-profile-compliance-suite-artifact
     :document-set profile-compliance-required-documents
     :pass {:name :profile-compliance-fixture-suite
            :input :phase-03-profile-fixtures
            :output :profile-compliance-proof
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :profile-manifest-validation
                       :profile-specific-validation
                       :profile-compatibility-validation]
            :preserves [:source-spans :generated-origin :profile :target
                        :effects :capabilities :dependency-profile-edges
                        :profile-validation-evidence]
            :emits [:accepted-profile-fixture-results
                    :rejected-profile-diagnostic-results
                    :capability-based-proof
                    :profile-compliance-conformance-results]
            :rejects (conj profile-compliance-required-diagnostic-ids
                           "P03-COMPLIANCE-INCOMPLETE"
                           "P03-COMPLIANCE-UNEXPECTED-ACCEPT")}
     :suite-source {:path source-path
                    :profile (get-in suite-source-artifact
                                     [:profile-manifest :profile])
                    :artifact-kind (:kind suite-source-artifact)
                    :artifact-hash (str "sha256:"
                                        (sha256-hex
                                         (pr-str suite-source-artifact)))}
     :profile-compliance-fixture-suite
     {:accepted-fixtures (mapv #(select-keys % [:fixture :stage
                                                :expected-profile :document])
                               profile-compliance-accepted-fixtures)
      :rejected-fixtures (mapv #(.getName (java.io.File. %))
                               (profile-compliance-rejected-fixture-paths))
      :status :complete}
     :accepted-profile-fixture-results accepted-results
     :rejected-profile-diagnostic-results rejected-results
     :capability-based-proof capability-proof
     :profile-compliance-conformance-results conformance
     :diagnostics []}))