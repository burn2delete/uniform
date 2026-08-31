(ns gravity.profile-validation.validation)

(defn- keyword-set? [value]
  (and (set? value) (every? keyword? value)))

(defn- valid-module? [module]
  (and (map? module)
       (keyword? (:profile module))
       (keyword? (:target module))
       (keyword-set? (:effects module))
       (keyword-set? (:capabilities module))
       (map? (:metadata module))))

(defn validate-inputs! [module typed-artifact module-artifact]
  (when-not (valid-module? module)
    (throw (ex-info "Profile validation module input is malformed"
                    {:module module})))
  (when-not (and (map? typed-artifact) (map? module-artifact))
    (throw (ex-info "Profile validation requires prebuilt artifact maps"
                    {:typed-artifact typed-artifact
                     :module-artifact module-artifact})))
  (let [inferred-effects
        (or (:inferred-effects typed-artifact)
            (get-in typed-artifact [:namespace-effect-summary :inferred])
            #{})
        required-capabilities (or (:required-capabilities typed-artifact) #{})]
    (when-not (keyword-set? inferred-effects)
      (throw (ex-info "Profile validation inferred effects are malformed"
                      {:inferred-effects inferred-effects})))
    (when-not (keyword-set? required-capabilities)
      (throw (ex-info "Profile validation required capabilities are malformed"
                      {:required-capabilities required-capabilities})))
    {:inferred-effects inferred-effects
     :required-capabilities required-capabilities}))
