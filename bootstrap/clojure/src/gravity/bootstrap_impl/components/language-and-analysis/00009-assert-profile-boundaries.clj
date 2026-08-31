

(defn assert-profile-boundaries!
  [source-path module dependencies]
  (module-analysis-call
   :assert-profile-boundaries! module-analysis/assert-profile-boundaries!
   source-path module dependencies))