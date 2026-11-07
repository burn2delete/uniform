

(defn invoke-stage0-builtin
  [module callee args]
  (gravity.runtime/invoke-native-stage0-builtin
    {:assert-min-arity! (var assert-min-arity!), :assert-exact-arity! (var assert-exact-arity!), :assert-between-arity! (var assert-between-arity!), :assert-even-arity! (var assert-even-arity!), :fail-call-arity! (var fail-call-arity!), :readable-pr-str (var p15-s23-seed-readable-pr-str), :normalized-rest (var p15-s23-seed-readable-normalized-rest), :fail! (var fail!)}
    module callee args))

(defn invoke-stage0-function
  [module callee args]
  (gravity.runtime/invoke-native-stage0-function #'fail-call-arity! #'eval-do module callee args))

(defn eval-collection
  [module env form]
  (gravity.runtime/eval-native-stage0-collection #'eval-expr module env form))

(defn eval-expr
  [module env form]
  (gravity.runtime/eval-native-stage0-expr {:fail! #'fail! :validate-module-effects! #'validate-module-effects! :eval-expr #'eval-expr :eval-do #'eval-do :bind-let #'bind-let :stage0-builtin-functions #'stage0-builtin-functions :invoke-stage0-builtin #'invoke-stage0-builtin :invoke-stage0-function #'invoke-stage0-function :eval-collection #'eval-collection :join #'clojure.string/join} module env form))

(defn run-main
  [module]
  (gravity.runtime/run-native-stage0-main #'stage0-function-table #'fail! #'eval-do module))

(defn compile-stage0-collection
  [module locals form]
  (gravity.lowering/compile-native-collection #'compile-stage0-expr module locals form))

(defn compile-stage0-let
  [module locals bindings body]
  (gravity.lowering/compile-native-let #'fail! #'compile-stage0-expr module locals bindings body))
