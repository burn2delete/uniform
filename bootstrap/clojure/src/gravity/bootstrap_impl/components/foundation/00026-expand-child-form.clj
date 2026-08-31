

(defn expand-child-form
  [registry module syntax form trace depth]
  (macro-expansion/expand-child-form registry module syntax form trace depth
                                      (macro-expansion-ops)))

(defn expand-form-children
  [registry module syntax form trace depth]
  (macro-expansion/expand-form-children registry module syntax form trace depth
                                         (macro-expansion-ops)))

(defn expansion-trace-record
  [module macro syntax input output generated-origin depth]
  (macro-expansion/expansion-trace-record module macro syntax input output
                                           generated-origin depth
                                           (macro-expansion-ops)))

(defn distinct-by-pr-str
  [values]
  (macro-expansion/distinct-by-pr-str values))

(defn expand-syntax-object
  [registry module syntax trace depth]
  (macro-expansion/expand-syntax-object registry module syntax trace depth
                                        (macro-expansion-ops)))