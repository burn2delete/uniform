

(defn profile-validation-facts
  [module typed-artifact module-artifact]
  (profile-validation-call
   :profile-validation-facts profile-validation/profile-validation-facts
   module typed-artifact module-artifact))