(ns gravity.capability-validation.diagnostics)

(defn- row-grant [row layer]
  (get row (keyword (str (name layer) "-grant"))))
(defn- nearest-grant [row]
  (some #(row-grant row %) [:provider :package :deployment]))

(defn diagnostic-context
  ([profile-output row] (diagnostic-context profile-output row nil))
  ([profile-output row layer]
   (let [grant (when layer (row-grant row layer))
         nearby (or grant (nearest-grant row))
         provider-fact (:provider-fact row)]
     {:profile (:profile profile-output) :target (:target profile-output)
      :source-span (first (:source-spans profile-output))
      :producing-pass :profile-validation :consuming-pass :capability-validation
      :requested-capability (:capability row) :capability (:capability row)
      :selected-or-missing-provider (:provider provider-fact)
      :provider (:provider row) :nearest-provider (:provider row)
      :grant-id (:grant-id grant) :nearest-grant (:grant-id nearby)
      :actual-scope (:actual-scope grant) :scope (:actual-scope grant)
      :requested-scope (:requested-scope (or grant nearby))
      :phase (:requested-phase (or grant nearby)) :grant-phase (:phase grant)})))

(defn- row-diagnostic [profile-output row diagnostic-record]
  (let [missing-layer (:missing-grant-layer row)
        scope-layer (:scope-mismatch-layer row)
        phase-layer (:phase-mismatch-layer row)]
    (cond
      (and (:required? row) (not (:declared? row)))
      [(diagnostic-record "L15-CAPABILITY-MISSING"
                          (merge (diagnostic-context profile-output row :provider)
                                 {:grant :source-declaration :scope :module
                                  :remediation :declare-required-capability}))]
      (not (:profile-allowed? row))
      [(diagnostic-record "L15-PROFILE"
                          (merge (diagnostic-context profile-output row :provider)
                                 {:grant :profile-legality :scope :profile
                                  :remediation :select-capability-compatible-profile}))]
      missing-layer
      [(diagnostic-record "L15-CAPABILITY-MISSING"
                          (merge (diagnostic-context profile-output row missing-layer)
                                 {:grant missing-layer :remediation :add-capability-grant}))]
      (not (:provider-selected? row))
      [(diagnostic-record "L15-PROVIDER-MISSING"
                          (merge (diagnostic-context profile-output row :provider)
                                 {:grant :provider :provider-fact (:provider-fact row)
                                  :remediation :select-required-provider}))]
      scope-layer
      [(diagnostic-record "L15-SCOPE"
                          (merge (diagnostic-context profile-output row scope-layer)
                                 {:grant scope-layer
                                  :remediation :attenuate-request-or-expand-grant-scope}))]
      phase-layer
      [(diagnostic-record "L15-PHASE"
                          (merge (diagnostic-context profile-output row phase-layer)
                                 {:grant phase-layer
                                  :remediation :use-separate-matching-phase-grant}))]
      (not (:provider-trusted? row))
      [(diagnostic-record "L15-TRUST"
                          (merge (diagnostic-context profile-output row :provider)
                                 (select-keys row [:capability :provider :provider-fact])
                                 {:grant :provider
                                  :trust-level (get-in row [:provider-fact :trust-level])
                                  :remediation :select-trusted-provider}))]
      :else [])))

(defn diagnostics [profile-output table diagnostic-record]
  (vec (concat
        (when (= :rejected (:status profile-output))
          [(diagnostic-record "L15-PROFILE"
                              (merge (diagnostic-context profile-output {})
                                     {:profile-status (:status profile-output)
                                      :grant :profile-legality :scope :profile
                                      :remediation :resolve-profile-validation-diagnostics}))])
        (mapcat #(row-diagnostic profile-output % diagnostic-record) table))))
