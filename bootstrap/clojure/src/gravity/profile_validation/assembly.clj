(ns gravity.profile-validation.assembly)

(def ^:private pass-template
  {:name :profile-validation
   :input :effected-core
   :output :profile-valid-core
   :requires [:type-facts :effect-facts :profile-declaration
              :module-facts :module-dependency-graph
              :profile-effect-policy :profile-capability-policy]
   :preserves [:source-spans :types :effects :profile-context
               :capability-requirements]
   :invalidates [:unchecked-profile-assumptions]
   ;; The report is a new output, not a replacement for an invalidated fact.
   :regenerates []
   :emits [:profile-facts :effect-permission-table
           :profile-validation-report :profile-diagnostics
           :input-provenance]})

(defn- diagnostic-context [module typed-artifact]
  {:profile (:profile module)
   :target (:target module)
   :source-span (first (:source-spans typed-artifact))
   :producing-pass :effect-checking
   :consuming-pass :profile-validation})

(defn- profile-diagnostics
  [module typed-artifact supported-profile? target-eligible? rejected-effects
   invoke]
  (let [context (diagnostic-context module typed-artifact)]
    (vec
     (concat
      (when-not supported-profile?
        [(invoke :diagnostic-record "P1-MISSING-PROFILE"
                 (assoc context :remediation :declare-a-standard-profile))])
      (map #(invoke :diagnostic-record "P1-EFFECT"
                    (merge context
                           (select-keys % [:effect :state :policy-layer])
                           {:remediation
                            :remove-effect-or-select-compatible-profile}))
           rejected-effects)
      (when-not target-eligible?
        [(invoke :diagnostic-record "P1-BACKEND"
                 (assoc context :remediation :select-a-supported-target))])))))

(defn- profile-report
  [module contract permission-table target-eligible? checked-effects
   rejected-effects diagnostics accepted?]
  {:artifact :gravity/profile-validation-report
   :profile (:profile module)
   :target (:target module)
   :profile-contract contract
   :effect-permission-table permission-table
   :memory-regime (:memory contract)
   :runtime-assumptions (:runtime contract)
   :backend-eligibility
   {:profile (:profile module)
    :target (:target module)
    :eligible? target-eligible?
    :decision (if target-eligible? :eligible :rejected)
    :authority? false}
   :checked-effects checked-effects
   :rejected-effects rejected-effects
   :diagnostics diagnostics
   :status (if accepted? :accepted :rejected)})

(defn profile-validation-facts
  [module typed-artifact module-artifact inferred-effects
   required-capabilities effective-effects-fn permission-table-fn contract-fn
   invoke operation-value]
  (let [authority (effective-effects-fn module inferred-effects)
        permission-table (permission-table-fn module inferred-effects authority)
        contract (contract-fn (:profile module))
        supported-profile?
        (contains? (set (operation-value :standard-profile-order))
                   (:profile module))
        target-eligible?
        (contains? (operation-value :supported-targets) (:target module))
        rejected-effects (filterv #(= :rejected (:state %)) permission-table)
        checked-effects (filterv #(= :checked (:state %)) permission-table)
        diagnostics (profile-diagnostics module typed-artifact supported-profile?
                                         target-eligible? rejected-effects invoke)
        accepted? (and supported-profile? target-eligible?
                       (empty? rejected-effects))
        pass (assoc pass-template
                    :rejects (operation-value :profile-diagnostic-ids))
        report (profile-report module contract permission-table target-eligible?
                               checked-effects rejected-effects diagnostics
                               accepted?)]
    {:kind :gravity/stage0-profile-validation-facts
     :pass pass
     :profile-valid-core
     {:artifact :gravity/profile-valid-core
      :effected-core (or (:effected-core typed-artifact)
                         (:typed-core-module typed-artifact))
      :profile (:profile module)
      :target (:target module)
      :effects (:effective authority)
      :declared-capabilities (:capabilities module)
      :required-capabilities required-capabilities
      :source-spans (:source-spans typed-artifact)
      :status (if accepted? :accepted :rejected)}
     :profile-validation-report report
     :profile-diagnostics diagnostics
     :module-dependency-graph (:module-dependency-graph module-artifact)
     :input-provenance
     {:typed-artifact-kind (:kind typed-artifact)
      :module-artifact-kind (:kind module-artifact)
      :module (:module module)}
     :status (if accepted? :accepted :rejected)}))
