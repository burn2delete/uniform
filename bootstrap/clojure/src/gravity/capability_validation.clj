(ns gravity.capability-validation
  "Pure hosted Stage0 capability-validation compatibility projection."
  (:require [gravity.capability-validation.assembly :as assembly]
            [gravity.capability-validation.authority :as authority]
            [gravity.capability-validation.contract :as contract]
            [gravity.capability-validation.interposition :as interposition]
            [gravity.capability-validation.validation :as validation]))

(interposition/definterposable provider-name :provider-name
  [capability]
  (get-in (interposition/operation-value :provider-specs) [capability :provider]))

(interposition/definterposable profile-capabilities :profile-capabilities
  [profile]
  (authority/profile-capabilities
   (interposition/operation-value :provider-specs) profile interposition/invoke))

(interposition/definterposable profile-effective-capabilities
  :profile-effective-capabilities
  [profile-output grant-facts]
  (authority/profile-effective-capabilities profile-output grant-facts
                                            profile-capabilities))

(interposition/definterposable capability-permission-table
  :capability-permission-table
  [profile-output effective provider-facts]
  (authority/capability-permission-table profile-output effective provider-facts
                                         provider-name interposition/invoke))

(interposition/definterposable capability-validation-facts
  :capability-validation-facts
  [profile-output profile-report grant-facts provider-facts]
  (validation/validate-inputs! profile-output profile-report grant-facts provider-facts)
  (assembly/capability-validation-facts
   profile-output profile-report grant-facts provider-facts
   profile-effective-capabilities capability-permission-table
   interposition/invoke interposition/operation-value))

(defn with-operations [operations thunk]
  (interposition/with-operations operations thunk))

(defn call-entrypoint-body [operation-key operation args]
  (interposition/call-entrypoint-body operation-key operation args))

(def public-api
  {'public-api {:kind :contract}
   'capability-validation-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'provider-name {:arglists '([capability])}
   'profile-capabilities {:arglists '([profile])}
   'profile-effective-capabilities {:arglists '([profile-output grant-facts])}
   'capability-permission-table {:arglists '([profile-output effective provider-facts])}
   'capability-validation-facts
   {:arglists '([profile-output profile-report grant-facts provider-facts])}})

(defn capability-validation-contract []
  (assoc (contract/namespace-contract interposition/operation-keys)
         :public-api public-api))
