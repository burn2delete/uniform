

(defn p15-s23-stage2-runtime-invoke-builtin
  [plan callee args]
  (try
    (case callee
      + (p15-s23-stage2-runtime-add args)
      - (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-subtract args))
      * (p15-s23-stage2-runtime-multiply args)
      / (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-divide args))
      = (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 1)
            (p15-s23-stage2-runtime-equal args))
      < (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 2)
            (p15-s23-stage2-runtime-less-than args))
      > (do (p15-s23-stage2-runtime-assert-min-arity!
             plan callee args 2)
            (p15-s23-stage2-runtime-greater-than args))
      <= (do (p15-s23-stage2-runtime-assert-min-arity!
              plan callee args 2)
             (p15-s23-stage2-runtime-less-than-or-equal args))
      >= (do (p15-s23-stage2-runtime-assert-min-arity!
              plan callee args 2)
             (p15-s23-stage2-runtime-greater-than-or-equal args))
      str (p15-s23-stage2-runtime-str args)
      pr-str (p15-s23-seed-readable-pr-str
              (get-in plan [:source :path]) args)
      hash-map (do (p15-s23-stage2-runtime-assert-even-arity!
                    plan callee args)
                   (apply hash-map args))
      vector (vec args)
      list (apply list args)
      conj (do (p15-s23-stage2-runtime-assert-min-arity!
                plan callee args 1)
               (apply conj args))
      assoc (do
              (p15-s23-stage2-runtime-assert-min-arity!
               plan callee args 3)
              (when (even? (count args))
                (p15-s23-stage2-runtime-fail-call-arity!
                 "L2-BUILTIN-ARITY" plan callee args
                 "a collection followed by key/value pairs"))
              (apply assoc args))
      get (do (p15-s23-stage2-runtime-assert-between-arity!
               plan callee args 2 3)
              ;; `get` dominates checked-core execution. Avoid `apply` here:
              ;; it turns the already-materialized argument vector into a seq
              ;; and performs generic bounded-arity dispatch on every lookup.
              (case (count args)
                2 (get (nth args 0) (nth args 1))
                3 (get (nth args 0) (nth args 1) (nth args 2))))
      first (do (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (first (nth args 0)))
      second (do (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (second (nth args 0)))
      rest (do (p15-s23-stage2-runtime-assert-exact-arity!
                plan callee args 1)
               (p15-s23-seed-readable-normalized-rest (nth args 0)))
      count (do (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (count (nth args 0)))
      symbol? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw (IllegalArgumentException.
                          "compiler-only predicate outside compiler artifact")))
                (symbol? (nth args 0)))
      keyword? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw (IllegalArgumentException.
                           "compiler-only predicate outside compiler artifact")))
                 (keyword? (nth args 0)))
      char? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw (IllegalArgumentException.
                        "compiler-only predicate outside compiler artifact")))
              (char? (nth args 0)))
      number? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw (IllegalArgumentException.
                          "compiler-only predicate outside compiler artifact")))
                (number? (nth args 0)))
      seq? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw (IllegalArgumentException.
                       "compiler-only predicate outside compiler artifact")))
             (seq? (nth args 0)))
      list? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw (IllegalArgumentException.
                        "compiler-only predicate outside compiler artifact")))
              (list? (nth args 0)))
      vector? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw (IllegalArgumentException.
                          "compiler-only predicate outside compiler artifact")))
                (vector? (nth args 0)))
      map? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw (IllegalArgumentException.
                       "compiler-only predicate outside compiler artifact")))
             (map? (nth args 0)))
      set? (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw (IllegalArgumentException.
                       "compiler-only predicate outside compiler artifact")))
             (set? (nth args 0)))
      string? (do
                (p15-s23-stage2-runtime-assert-exact-arity!
                 plan callee args 1)
                (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw (IllegalArgumentException.
                          "compiler-only predicate outside compiler artifact")))
                (string? (nth args 0)))
      contains? (do
                  (p15-s23-stage2-runtime-assert-exact-arity!
                   plan callee args 2)
                  (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                    (throw (IllegalArgumentException.
                            "compiler-only predicate outside compiler artifact")))
                  (contains? (nth args 0) (nth args 1)))
      even? (do
              (p15-s23-stage2-runtime-assert-exact-arity!
               plan callee args 1)
              (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw (IllegalArgumentException.
                        "compiler-only predicate outside compiler artifact")))
              (even? (nth args 0)))
      integer? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw (IllegalArgumentException.
                           "compiler-only predicate outside compiler artifact")))
                 (integer? (nth args 0)))
      boolean? (do
                 (p15-s23-stage2-runtime-assert-exact-arity!
                  plan callee args 1)
                 (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw (IllegalArgumentException.
                           "compiler-only predicate outside compiler artifact")))
                 (boolean? (nth args 0)))
      keys (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 1)
             (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw (IllegalArgumentException.
                       "compiler-only collection primitive outside compiler artifact")))
             (apply list (keys (nth args 0))))
      set (do
            (p15-s23-stage2-runtime-assert-exact-arity!
             plan callee args 1)
            (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
              (throw (IllegalArgumentException.
                      "compiler-only collection primitive outside compiler artifact")))
            (set (nth args 0)))
      sort-by-pr-str (do
                       (p15-s23-stage2-runtime-assert-exact-arity!
                        plan callee args 1)
                       (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                         (throw (IllegalArgumentException.
                                 "compiler-only ordering primitive outside compiler artifact")))
                       (apply list (sort-by pr-str (nth args 0))))
      vec (do
            (p15-s23-stage2-runtime-assert-exact-arity!
             plan callee args 1)
            (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
              (throw (IllegalArgumentException.
                      "compiler-only vectorization primitive outside compiler artifact")))
            (into [] (nth args 0)))
      quot (do
             (p15-s23-stage2-runtime-assert-exact-arity!
              plan callee args 2)
             (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw (IllegalArgumentException.
                       "compiler-only arithmetic primitive outside compiler artifact")))
             (quot (nth args 0) (nth args 1)))
      subvec (do
               (p15-s23-stage2-runtime-assert-exact-arity!
                plan callee args 3)
               (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
                 (throw (IllegalArgumentException.
                         "compiler-only slicing primitive outside compiler artifact")))
               (into []
                     (subvec (nth args 0) (nth args 1) (nth args 2)))))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (p15-s23-stage2-runtime-fail-builtin-error! plan callee ex))))

(declare p15-s23-stage2-runtime-execute-instruction)
(declare p15-s23-stage2-runtime-execute-function)
(declare p15-s23-stage2-runtime-artifact-invoke)
(declare p15-s23-stage2-runtime-artifact-function)
(declare p15-s23-stage2-runtime-artifact-concat-function)
(declare p15-s23-stage2-runtime-artifact-println-function)
(declare p15-s23-stage2-runtime-artifact-println-two-function)