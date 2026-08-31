

(defn- p15-s23-stage2-runtime-invoke-unary-or-generic-builtin
  [plan function value]
  ;; Argument evaluation deliberately happens at the call site, before this
  ;; exception boundary and before compiler-context validation. This retains
  ;; the generic path's evaluation order while avoiding its one-element vector.
  (try
    (case function
      first (first value)
      second (second value)
      rest (p15-s23-seed-readable-normalized-rest value)

      symbol? (do
                (when-not
                 (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw
                   (IllegalArgumentException.
                    "compiler-only predicate outside compiler artifact")))
                (symbol? value))
      keyword? (do
                 (when-not
                  (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw
                    (IllegalArgumentException.
                     "compiler-only predicate outside compiler artifact")))
                 (keyword? value))
      char? (do
              (when-not
               (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw
                 (IllegalArgumentException.
                  "compiler-only predicate outside compiler artifact")))
              (char? value))
      number? (do
                (when-not
                 (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw
                   (IllegalArgumentException.
                    "compiler-only predicate outside compiler artifact")))
                (number? value))
      seq? (do
             (when-not
              (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw
                (IllegalArgumentException.
                 "compiler-only predicate outside compiler artifact")))
             (seq? value))
      list? (do
              (when-not
               (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw
                 (IllegalArgumentException.
                  "compiler-only predicate outside compiler artifact")))
              (list? value))
      vector? (do
                (when-not
                 (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw
                   (IllegalArgumentException.
                    "compiler-only predicate outside compiler artifact")))
                (vector? value))
      map? (do
             (when-not
              (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw
                (IllegalArgumentException.
                 "compiler-only predicate outside compiler artifact")))
             (map? value))
      set? (do
             (when-not
              (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw
                (IllegalArgumentException.
                 "compiler-only predicate outside compiler artifact")))
             (set? value))
      string? (do
                (when-not
                 (p15-s23-stage2-compiler-artifact-plan-context? plan)
                  (throw
                   (IllegalArgumentException.
                    "compiler-only predicate outside compiler artifact")))
                (string? value))
      even? (do
              (when-not
               (p15-s23-stage2-compiler-artifact-plan-context? plan)
                (throw
                 (IllegalArgumentException.
                  "compiler-only predicate outside compiler artifact")))
              (even? value))
      integer? (do
                 (when-not
                  (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw
                    (IllegalArgumentException.
                     "compiler-only predicate outside compiler artifact")))
                 (integer? value))
      boolean? (do
                 (when-not
                  (p15-s23-stage2-compiler-artifact-plan-context? plan)
                   (throw
                    (IllegalArgumentException.
                     "compiler-only predicate outside compiler artifact")))
                 (boolean? value))

      keys (do
             (when-not
              (p15-s23-stage2-compiler-artifact-plan-context? plan)
               (throw
                (IllegalArgumentException.
                 "compiler-only collection primitive outside compiler artifact")))
             (apply list (keys value)))
      set (do
            (when-not
             (p15-s23-stage2-compiler-artifact-plan-context? plan)
              (throw
               (IllegalArgumentException.
                "compiler-only collection primitive outside compiler artifact")))
            (set value))
      sort-by-pr-str
      (do
        (when-not (p15-s23-stage2-compiler-artifact-plan-context? plan)
          (throw
           (IllegalArgumentException.
            "compiler-only ordering primitive outside compiler artifact")))
        (apply list (sort-by pr-str value)))
      vec (do
            (when-not
             (p15-s23-stage2-compiler-artifact-plan-context? plan)
              (throw
               (IllegalArgumentException.
                "compiler-only vectorization primitive outside compiler artifact")))
            (into [] value))

      ;; Variadic builtins, pr-str, and malformed callees retain generic
      ;; dispatch after the sole argument has been evaluated exactly once.
      (p15-s23-stage2-runtime-invoke-builtin plan function [value]))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (p15-s23-stage2-runtime-fail-builtin-error! plan function ex))))