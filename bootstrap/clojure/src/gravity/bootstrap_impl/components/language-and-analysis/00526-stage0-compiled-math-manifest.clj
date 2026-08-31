

(defn stage0-compiled-math-manifest
  [module]
  {:profile (:profile module)
   :target (:target module)
   :source-effects (:effects module)
   :source-capabilities (:capabilities module)
   :metadata (:metadata module)})

(defn stage0-compiled-math-suite-present?
  [module]
  (contains? (get-in module [:metadata :math] {}) :numeric))