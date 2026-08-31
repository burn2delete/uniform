

(defn backend-eligibility-report
  [module manifest]
  (let [eligible? (contains? supported-targets (:target module))]
    {:profile (:profile module)
     :target (:target module)
     :backend :clojure-stage0-jvm
     :eligible? eligible?
     :required-forms (:allowed-forms (:profile-contract manifest))
     :required-effects (:effective-effects manifest)
     :required-capabilities (:effective-capabilities manifest)
     :memory-regime (:memory-regime manifest)
     :runtime-assumptions (:runtime-assumptions manifest)
     :decision (if eligible? :eligible :rejected)
     :note "backend eligibility reports target support; it does not legalize profile-rejected behavior"}))