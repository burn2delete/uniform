

(defn effect-permission-table
  [module inferred-effects effective]
  (profile-validation-call
   :effect-permission-table profile-validation/effect-permission-table
   module inferred-effects effective))