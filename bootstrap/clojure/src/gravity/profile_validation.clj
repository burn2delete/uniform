(ns gravity.profile-validation
  "Pure hosted Stage0 profile-validation compatibility projection.

  The leaf consumes an already-produced effected-core/module projection and
  externally supplied policy tables. It owns profile legality facts and a
  deterministic validation report, while capability grants remain the next
  pass's responsibility."
  (:require [gravity.profile-validation.assembly :as assembly]
            [gravity.profile-validation.contract :as contract]
            [gravity.profile-validation.interposition :as interposition]
            [gravity.profile-validation.policy :as policy]
            [gravity.profile-validation.validation :as validation]))

(interposition/definterposable all-registered-effects
  :all-registered-effects
  []
  (set (keys (interposition/operation-value :effect-registry))))

(interposition/definterposable effect-registry-entry
  :effect-registry-entry
  [effect]
  (get (interposition/operation-value :effect-registry) effect))

(interposition/definterposable profile-allowed-effects
  :profile-allowed-effects
  [profile]
  (policy/profile-allowed-effects
   (interposition/operation-value :effect-registry)
   profile
   interposition/invoke))

(interposition/definterposable profile-capabilities
  :profile-capabilities
  [profile]
  (policy/profile-capabilities
   (interposition/operation-value :provider-specs)
   profile
   interposition/invoke))

(interposition/definterposable profile-contract :profile-contract
  [profile]
  (policy/profile-contract
   profile
   profile-allowed-effects
   all-registered-effects
   profile-capabilities
   interposition/operation-value
   interposition/invoke))

(interposition/definterposable profile-policy-layer :profile-policy-layer
  [module metadata-key source-key default-value]
  (policy/profile-policy-layer module metadata-key source-key default-value))

(interposition/definterposable profile-effective-effects
  :profile-effective-effects
  [module inferred-effects]
  (policy/profile-effective-effects
   module inferred-effects profile-allowed-effects profile-policy-layer))

(interposition/definterposable effect-permission-table
  :effect-permission-table
  [module inferred-effects effective]
  (policy/effect-permission-table
   inferred-effects effective effect-registry-entry interposition/invoke))

(interposition/definterposable profile-validation-facts
  :profile-validation-facts
  [module typed-artifact module-artifact]
  (let [{:keys [inferred-effects required-capabilities]}
        (validation/validate-inputs! module typed-artifact module-artifact)]
    (assembly/profile-validation-facts
     module typed-artifact module-artifact inferred-effects
     required-capabilities profile-effective-effects effect-permission-table
     profile-contract interposition/invoke interposition/operation-value)))

(defn with-operations [operations thunk]
  (interposition/with-operations operations thunk))

(defn call-entrypoint-body [operation-key operation args]
  (interposition/call-entrypoint-body operation-key operation args))

(def public-api
  {'public-api {:kind :contract}
   'profile-validation-contract {:arglists '([])}
   'with-operations {:arglists '([operations thunk])}
   'call-entrypoint-body {:arglists '([operation-key operation args])}
   'all-registered-effects {:arglists '([])}
   'effect-registry-entry {:arglists '([effect])}
   'profile-allowed-effects {:arglists '([profile])}
   'profile-capabilities {:arglists '([profile])}
   'profile-contract {:arglists '([profile])}
   'profile-policy-layer
   {:arglists '([module metadata-key source-key default-value])}
   'profile-effective-effects {:arglists '([module inferred-effects])}
   'effect-permission-table
   {:arglists '([module inferred-effects effective])}
   'profile-validation-facts
   {:arglists '([module typed-artifact module-artifact])}})

(defn profile-validation-contract []
  (assoc (contract/namespace-contract interposition/operation-keys)
         :public-api public-api))
