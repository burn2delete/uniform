

(defn parse-defmacro-form
  [module syntax]
  (macro-expansion/parse-defmacro-form module syntax
                                        (macro-expansion-ops)))

(defn macro-registry
  [module syntax]
  (macro-expansion/macro-registry module syntax (macro-expansion-ops)))

(defn macro-namespace-entry
  [macro]
  (macro-expansion/macro-namespace-entry macro))

(defn macro-build-effect-record
  [macro]
  (macro-expansion/macro-build-effect-record macro))

(defn macro-build-grants
  [module]
  (macro-expansion/macro-build-grants module))