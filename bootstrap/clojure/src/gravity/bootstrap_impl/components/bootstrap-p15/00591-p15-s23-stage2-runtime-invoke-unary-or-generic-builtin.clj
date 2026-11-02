

(defn- p15-s23-stage2-runtime-invoke-unary-or-generic-builtin
  [runtime plan function value]
  ;; Argument evaluation deliberately happens at the call site, before this
  ;; exception boundary and before compiler-context validation. This retains
  ;; the generic path's evaluation order while avoiding its one-element vector.
  (try
    (case function
      first (first value)
      second (second value)
      rest (p15-s23-seed-readable-normalized-rest value)

      symbol? (do
                (symbol? value))
      keyword? (do
                 (keyword? value))
      char? (do
              (char? value))
      number? (do
                (number? value))
      seq? (do
             (seq? value))
      list? (do
              (list? value))
      vector? (do
                (vector? value))
      map? (do
             (map? value))
      set? (do
             (set? value))
      string? (do
                (string? value))
      even? (do
              (even? value))
      integer? (do
                 (integer? value))
      boolean? (do
                 (boolean? value))

      keys (do
             (apply list (keys value)))
      set (do
            (set value))
      sort-by-pr-str
      (do
        (apply list (sort-by pr-str value)))
      vec (do
            (into [] value))

      ;; Variadic builtins, pr-str, and malformed callees retain generic
      ;; dispatch after the sole argument has been evaluated exactly once.
      (p15-s23-stage2-runtime-invoke-builtin runtime plan function [value]))
    (catch clojure.lang.ExceptionInfo ex
      (throw ex))
    (catch Exception ex
      (p15-s23-stage2-runtime-fail-builtin-error! plan function ex))))
