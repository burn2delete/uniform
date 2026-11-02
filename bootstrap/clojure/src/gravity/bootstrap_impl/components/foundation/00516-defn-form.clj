

(defn defn-form?
  [form]
  (and (seq? form) (= 'defn (first form))))

(defn def-main-form?
  [form]
  (and (seq? form)
       (= 'def (first form))
       (= 'main (second form))
       (seq? (nth form 2 nil))
       (= 'fn (first (nth form 2)))))

(defn def-function-form?
  [form]
  (and (seq? form)
       (= 'def (first form))
       (symbol? (second form))
       (seq? (nth form 2 nil))
       (= 'fn (first (nth form 2)))))

(def stage0-special-forms
  '#{println do if let quote host-reflect})

(def stage0-builtin-functions
  '#{+ - * / = < > <= >= str pr-str hash-map vector list conj assoc get
     first second rest count})

(def stage0-core-app-rejected-fixtures
  [{:fixture "bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity"
    :diagnostic "L2-FUNCTION-ARITY"
    :rejected-behavior :wrong-user-function-arity}
   {:fixture "bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity"
    :diagnostic "L2-BUILTIN-ARITY"
    :rejected-behavior :wrong-builtin-arity}])