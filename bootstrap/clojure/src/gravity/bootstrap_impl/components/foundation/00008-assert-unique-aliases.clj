

(defn assert-unique-aliases!
  [source-path dependencies]
  (module-analysis-call
   :assert-unique-aliases! module-analysis/assert-unique-aliases!
   source-path dependencies))

(defn assert-referred-names-unambiguous!
  [source-path dependencies]
  (module-analysis-call
   :assert-referred-names-unambiguous!
   module-analysis/assert-referred-names-unambiguous!
   source-path dependencies))

(defn assert-qualified-symbols-resolve!
  [source-path forms module dependencies]
  (module-analysis-call
   :assert-qualified-symbols-resolve!
   module-analysis/assert-qualified-symbols-resolve!
   source-path forms module dependencies))