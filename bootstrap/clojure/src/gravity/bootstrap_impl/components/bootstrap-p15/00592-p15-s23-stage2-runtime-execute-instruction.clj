(declare p15-s23-sh07-b51-runtime-authorize-external-link
         p15-s23-sh07-b51-runtime-execute-external-machine
         p15-s23-stage2-module-execute-call p15-s23-stage2-runtime-execute-call-scoped
         ^:private p15-s23-stage2-runtime-execute-function-scoped)
(def ^:private p15-s23-runtime-instruction-operations
  {:fail! #'fail!
   :execute-values-scoped #'p15-s23-stage2-runtime-execute-values-scoped
   :execute-map-entries-scoped #'p15-s23-stage2-runtime-execute-map-entries-scoped
   :validate-module-effects! #'validate-module-effects!
   :artifact-invoke #'p15-s23-stage2-runtime-artifact-invoke
   :artifact-println-function #'p15-s23-stage2-runtime-artifact-println-function
   :artifact-println-two-function #'p15-s23-stage2-runtime-artifact-println-two-function
   :join #'clojure.string/join
   :artifact-function #'p15-s23-stage2-runtime-artifact-function
   :execute-instructions-scoped #'p15-s23-stage2-runtime-execute-instructions-scoped
   :nontail-value! #'p15-s23-stage2-runtime-nontail-value!
   :execute-instruction-scoped #'p15-s23-stage2-runtime-execute-instruction-scoped
   :recur-signal? #'p15-s23-stage2-runtime-recur-signal?
   :recur-values #'p15-s23-stage2-runtime-recur-values
   :recur-fail! #'p15-s23-stage2-runtime-recur-fail!
   :bind-values #'p15-s23-stage2-runtime-bind-values
   :recur-signal #'p15-s23-stage2-runtime-recur-signal
   :source-throw! #'p15-s23-stage2-runtime-source-throw!
   :execute-value-scoped #'p15-s23-stage2-runtime-execute-value-scoped
   :invoke-get #'p15-s23-stage2-runtime-invoke-get
   :fail-builtin-error! #'p15-s23-stage2-runtime-fail-builtin-error!
   :invoke-compiler-internal-builtin #'p15-s23-sh07-runtime-invoke-compiler-internal-builtin
   :invoke-unary-or-generic-builtin #'p15-s23-stage2-runtime-invoke-unary-or-generic-builtin
   :execute-generic-builtin-call-scoped #'p15-s23-stage2-runtime-execute-generic-builtin-call-scoped
   :execute-call-scoped #'p15-s23-stage2-runtime-execute-call-scoped
   :module-execute-call #'p15-s23-stage2-module-execute-call})

(defn- p15-s23-stage2-runtime-execute-instruction-scoped
  [runtime plan env instruction public-invocation-scope]
  (gravity.runtime/execute-instruction-scoped runtime plan env instruction public-invocation-scope p15-s23-runtime-instruction-operations))
(defn p15-s23-stage2-runtime-execute-instruction
  [runtime plan env instruction]
  (p15-s23-stage2-runtime-execute-instruction-scoped
   runtime plan env instruction nil))
