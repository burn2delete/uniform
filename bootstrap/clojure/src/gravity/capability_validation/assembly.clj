(ns gravity.capability-validation.assembly
  (:require [gravity.capability-validation.diagnostics :as diagnostics]))

(def ^:private pass-template
  {:name :capability-validation :input :profile-valid-core :output :capability-valid-core
   :requires [:capability-requirements :profile-validation-report
              :package-capability-grants :provider-capability-grants
              :deployment-capability-grants :provider-registry :provider-trust-facts]
   :preserves [:source-spans :types :effects :profile-context :target
               :capability-requirements]
   :invalidates [:unscoped-provider-cache] :regenerates []
   :emits [:capability-facts :capability-permission-table
           :capability-validation-report :capability-diagnostics]})

(defn capability-validation-facts
  [profile-output profile-report grant-facts provider-facts profile-effective
   permission-table invoke operation-value]
  (let [authority (profile-effective profile-output grant-facts)
        table (permission-table profile-output authority provider-facts)
        diagnostic-record (fn [id facts] (invoke :diagnostic-record id facts))
        diagnostics (diagnostics/diagnostics profile-output table diagnostic-record)
        accepted? (and (= :accepted (:status profile-output)) (empty? diagnostics))
        pass (assoc pass-template :rejects (operation-value :capability-diagnostic-ids))
        grant-effective-capabilities (:effective authority)
        effective-capabilities
        (invoke :stable-set
                (for [row table
                      :when (and (:effective? row) (:provider-selected? row)
                                 (:scope-valid? row) (:phase-valid? row)
                                 (:provider-trusted? row))]
                  (:capability row)))
        report {:artifact :gravity/capability-validation-report
                :profile (:profile profile-output) :target (:target profile-output)
                :capability-permission-table table
                :grant-effective-capabilities grant-effective-capabilities
                :effective-capabilities effective-capabilities
                :grant-facts grant-facts :provider-facts provider-facts
                :profile-validation-report profile-report :diagnostics diagnostics
                :status (if accepted? :accepted :rejected)}]
    {:kind :gravity/stage0-capability-validation-facts :pass pass
     :capability-valid-core
     {:artifact :gravity/capability-valid-core :profile-valid-core profile-output
      :profile (:profile profile-output) :target (:target profile-output)
      :grant-effective-capabilities grant-effective-capabilities
      :effective-capabilities effective-capabilities
      :source-spans (:source-spans profile-output)
      :status (if accepted? :accepted :rejected)}
     :capability-validation-report report :capability-diagnostics diagnostics
     :input-provenance {:profile-validation-report profile-report
                        :grant-facts grant-facts :provider-facts provider-facts}
     :status (if accepted? :accepted :rejected)}))
