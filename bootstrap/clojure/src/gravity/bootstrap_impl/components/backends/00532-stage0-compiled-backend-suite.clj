

(defn stage0-compiled-backend-suite
  [module]
  (get-in module [:metadata :backend :compiled-gate] {}))

(defn stage0-compiled-backend-suite-present?
  [module]
  (contains? (get-in module [:metadata :backend] {}) :compiled-gate))

(defn validate-stage0-compiled-backend-input!
  [module input-record]
  (when-not (contains? #{:verified-mir :verified-domain-ir}
                       (:input-kind input-record))
    (backend-interface-fail!
     "B1-INPUT" (:source-path module)
     {:stage :stage0-compiled-backend-gate
      :backend-id :gravity.backend/stage0-jvm-instruction-runner
      :input-artifact-id (:input-artifact-id input-record)
      :profile (:profile module)
      :target (:target module)
      :missing-evidence [:verified-mir-or-domain-ir]
      :fallback-status :development-only}
     {:missing-fields [:verified-mir-or-domain-ir]
      :remediation
      "A release or real backend claim must consume verified MIR or verified domain IR; the stage0 instruction plan is only a development backend artifact boundary."})))