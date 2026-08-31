

(defn assert-generated-unsafe!
  [module macro output call-span]
  (macro-expansion/assert-generated-unsafe! module macro output call-span
                                            (macro-expansion-ops)))