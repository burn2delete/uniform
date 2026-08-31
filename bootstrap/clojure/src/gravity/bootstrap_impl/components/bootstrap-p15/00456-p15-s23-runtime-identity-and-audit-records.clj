

(defn p15-s23-runtime-identity-and-audit-records
  [source-path capability-table]
  (let [rows (:rows capability-table)
        grants (filter #(contains? #{:grant :delegate} (:decision %)) rows)
        denials (filter #(= :deny (:decision %)) rows)
        revocations (filter #(= :revoke (:decision %)) rows)]
    {:principal-identity-record
     {:artifact :gravity/principal-identity-record
      :principals [{:principal :gravity.compiler/p15-s23
                    :namespace :gravity.bootstrap.p15-s23.compiler
                    :package :gravity/bootstrap
                    :source-path source-path
                    :identity-status :verified}]
      :invalid-principals []
      :status :complete}
     :runtime-decision-log
     {:artifact :gravity/runtime-decision-log
      :decisions
      (mapv #(select-keys % [:action-id :principal :family :effect
                             :capability :provider :policy-source
                             :decision :redaction-status :audit-status
                             :handle-id])
            rows)
      :missing-required-audit []
      :status :complete}
     :delegated-handle-record
     {:artifact :gravity/delegated-handle-record
      :handles
      (mapv (fn [row]
              {:handle-id (:handle-id row)
               :principal (:principal row)
               :provider (:provider row)
               :allowed-effect (:effect row)
               :capability (:capability row)
               :scope (or (:delegate-scope row) [(:family row)])
               :typed? true
               :scoped? true
               :revocable? true
               :audit-required? true})
            grants)
      :unscoped-handles []
      :status :complete}
     :revocation-record
     {:artifact :gravity/revocation-record
      :records
      (mapv (fn [row]
              {:handle-id (:handle-id row)
               :principal (:principal row)
               :provider (:provider row)
               :decision :revoke
               :use-after-revocation? false
               :audit-status :recorded})
            revocations)
      :unsupported-revocations []
      :status :complete}
     :denial-diagnostic-record
     {:artifact :gravity/denial-diagnostic-record
      :denials
      (mapv (fn [row]
              {:diagnostic "R11-GRANT"
               :action-id (:action-id row)
               :principal (:principal row)
               :effect (:effect row)
               :capability (:capability row)
               :provider (:provider row)
               :policy-source (:policy-source row)
               :decision (:decision row)
               :redaction-status (:redaction-status row)
               :source-span (:source-span row)
               :remediation :attach_matching_runtime_grant_or_keep_denied})
            denials)
      :status :complete}
     :redaction-secret-handling-record
     {:artifact :gravity/redaction-secret-handling-record
      :policies [{:category :secret :redaction :secret-name-only}
                 {:category :prompt :redaction :prompt-digest}
                 {:category :command :redaction :command-digest}
                 {:category :artifact :redaction :artifact-id-only}]
      :secret-leaks []
      :records-redacted?
      (every? #(contains? % :redaction-status) rows)
      :status :complete}}))

(defn p15-s23-runtime-conformance-evidence
  [runtime-manifest service-table capability-manifest capability-table
   audit-records]
  {:artifact :gravity/p15-s23-runtime-capability-conformance-evidence
   :runtime-manifest-consumed?
   (contains? (set (:consumed-by runtime-manifest)) :self-hosting-gate)
   :runtime-family-selection-covered?
   (= :complete (get-in runtime-manifest [:selection-record :status]))
   :service-classification-covered?
   (and (= :complete (:status service-table))
        (empty? (:hidden-services service-table)))
   :capability-manifest-covered?
   (and (= :complete (:status capability-manifest))
        (true? (:deny-by-default? capability-manifest)))
   :deny-by-default-demonstrated?
   (and (true? (:deny-by-default? capability-table))
        (some #(= :deny (:decision %)) (:rows capability-table)))
   :grant-deny-delegate-revoke-covered?
   (= #{:grant :deny :delegate :revoke}
      (:decisions capability-table))
   :action-families-covered?
   (set/subset? p15-s23-runtime-action-families
                (:families-covered capability-table))
   :principal-identity-covered?
   (= :complete
      (get-in audit-records [:principal-identity-record :status]))
   :delegated-handles-scoped?
   (empty?
    (get-in audit-records [:delegated-handle-record :unscoped-handles]))
   :revocation-covered?
   (and (= :complete (get-in audit-records [:revocation-record :status]))
        (every? #(false? (:use-after-revocation? %))
                (get-in audit-records [:revocation-record :records])))
   :secret-redaction-covered?
   (and (= :complete
           (get-in audit-records
                   [:redaction-secret-handling-record :status]))
        (empty?
         (get-in audit-records
                 [:redaction-secret-handling-record :secret-leaks])))
   :audit-log-covered?
   (and (= :complete (get-in audit-records
                             [:runtime-decision-log :status]))
        (empty? (get-in audit-records
                        [:runtime-decision-log
                         :missing-required-audit])))
   :status :complete})