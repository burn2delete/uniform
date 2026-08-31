

(defn capability-permission-table
  [module required-capabilities effective]
  (let [source-capabilities (:source effective)
        row-capabilities (set/union source-capabilities required-capabilities)]
    (mapv (fn [capability]
            (let [profile-allowed? (contains? (:profile effective) capability)
                  package-allowed? (contains? (:package effective) capability)
                  provider-granted? (contains? (:provider effective) capability)
                  deployment-granted? (contains? (:deployment effective) capability)
                  effective? (contains? (:effective effective) capability)]
              {:capability capability
               :provider (provider-name capability)
               :declared? (contains? source-capabilities capability)
               :required? (contains? required-capabilities capability)
               :profile-allowed? profile-allowed?
               :package-allowed? package-allowed?
               :provider-granted? provider-granted?
               :deployment-granted? deployment-granted?
               :effective? effective?
               :state (cond
                        effective? :allowed
                        (not profile-allowed?) :rejected
                        (or (not package-allowed?)
                            (not provider-granted?)
                            (not deployment-granted?)) :checked
                        :else :rejected)
               :policy-layer (cond
                               (not profile-allowed?) :profile
                               (not package-allowed?) :package
                               (not provider-granted?) :provider
                               (not deployment-granted?) :deployment
                               :else :effective)}))
          (stable-vec row-capabilities))))

(defn capability-validation-facts
  "Project explicit capability grants through the hosted compatibility leaf.

  The legacy capability-permission-table above intentionally remains the
  bootstrap-owned grant row.  This entrypoint is the newer explicit pass
  boundary: the leaf narrows the final authority with provider trust while
  retaining the legacy grant intersection in its report."
  [profile-output profile-report grant-facts provider-facts]
  (capability-validation-call
   :capability-validation-facts
   capability-validation/capability-validation-facts
   profile-output profile-report grant-facts provider-facts))