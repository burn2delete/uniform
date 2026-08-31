

(def stage0-numeric-builtin-functions
  '#{+ - * / = < > <= >=})

(defn stage0-floating-literal?
  [form]
  (or (instance? Float form)
      (instance? Double form)
      (instance? java.math.BigDecimal form)))

(defn stage0-floating-literals
  [form]
  (cond
    (stage0-floating-literal? form)
    [form]

    (and (seq? form) (= 'quote (first form)))
    []

    (seq? form)
    (mapcat stage0-floating-literals form)

    (coll? form)
    (mapcat stage0-floating-literals form)

    :else
    []))