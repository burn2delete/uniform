

(def ^:private stage0-instruction-lowering-operations
  {:compile-expression #'compile-stage0-expr
   :compile-collection #'compile-stage0-collection
   :compile-let #'compile-stage0-let
   :builtin-functions #'stage0-builtin-functions
   :fail! #'fail!})

(defn compile-stage0-expr
  [module locals form]
  (gravity.lowering/compile-native-expression stage0-instruction-lowering-operations module locals form))

(defn compile-stage0-function
  [module definition]
  (gravity.lowering/compile-native-function #'compile-stage0-expr module definition))

(defn stage0-instruction-summary
  [instruction]
  (gravity.lowering/native-instruction-summary #'stage0-instruction-summary instruction))

(def ^:private stage0-plan-assembly-operations
  {:function-table #'stage0-function-table
   :profile! #'validate-stage0-compiled-profile!
   :executable-profile! #'executable-profile!
   :effects! #'validate-module-effects!
   :safety! #'validate-stage0-executable-safety!
   :performance! #'validate-stage0-compiled-performance!
   :math! #'validate-stage0-compiled-math!
   :compiler! #'validate-stage0-compiled-compiler!
   :backend! #'validate-stage0-compiled-backend!
   :runtime! #'validate-stage0-compiled-runtime!
   :domain! #'validate-stage0-compiled-domain!
   :schema! #'validate-stage0-compiled-schema!
   :ai! #'validate-stage0-compiled-ai!
   :package! #'validate-stage0-compiled-package!
   :tooling! #'validate-stage0-compiled-tooling!
   :conformance! #'validate-stage0-compiled-conformance!
   :compile-function #'compile-stage0-function
   :fail! #'fail!
   :sha256-hex #'sha256-hex
   :instruction-summary #'stage0-instruction-summary})

(defn stage0-compiled-core-plan
  [source-path source-text module]
  (gravity.lowering/assemble-native-stage0-plan stage0-plan-assembly-operations source-path source-text module))

(declare execute-stage0-instructions)

(def ^:private stage0-execution-operations
  {:execute-instructions #'execute-stage0-instructions
   :execute-instruction #'execute-stage0-instruction
   :execute-function #'execute-stage0-compiled-function
   :validate-effects! #'validate-module-effects!
   :invoke-builtin #'invoke-stage0-builtin
   :fail-call-arity! #'fail-call-arity!
   :fail! #'fail!
   :join #'clojure.string/join})

(defn execute-stage0-instructions
  [plan env instructions]
  (gravity.runtime/execute-native-stage0-instructions stage0-execution-operations plan env instructions))

(defn execute-stage0-instruction
  [plan env instruction]
  (gravity.runtime/execute-native-stage0-instruction stage0-execution-operations plan env instruction))
