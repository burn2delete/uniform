

(def ^:private p15-s23-runtime-unary-operations
  {:normalized-rest #'p15-s23-seed-readable-normalized-rest
   :invoke-builtin #'p15-s23-stage2-runtime-invoke-builtin
   :fail-builtin-error! #'p15-s23-stage2-runtime-fail-builtin-error!})

(defn- p15-s23-stage2-runtime-invoke-unary-or-generic-builtin
  [runtime plan function value]
  (gravity.runtime/hosted-unary-builtin runtime plan function value p15-s23-runtime-unary-operations))
