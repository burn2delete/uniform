

(defn assert-generated-profile!
  [module macro output call-span]
  (macro-expansion/assert-generated-profile! module macro output call-span
                                             (macro-expansion-ops)))