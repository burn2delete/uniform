

(defn expand-template
  [env template]
  (macro-expansion/expand-template env template (macro-expansion-ops)))

(defn parse-syntax-template
  [macro call-span]
  (macro-expansion/parse-syntax-template macro call-span
                                         (macro-expansion-ops)))

(defn builtin-defn-output
  [args call-span]
  (macro-expansion/builtin-defn-output args call-span
                                       (macro-expansion-ops)))

(defn builtin-when-output
  [args call-span]
  (macro-expansion/builtin-when-output args call-span
                                       (macro-expansion-ops)))

(defn thread-first-step
  [value step]
  (macro-expansion/thread-first-step value step))

(defn builtin-thread-first-output
  [args call-span]
  (macro-expansion/builtin-thread-first-output args call-span
                                               (macro-expansion-ops)))