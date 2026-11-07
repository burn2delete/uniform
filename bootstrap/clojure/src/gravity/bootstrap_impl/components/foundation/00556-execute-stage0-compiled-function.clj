

(defn execute-stage0-compiled-function
  [plan callee args]
  (gravity.runtime/execute-native-stage0-function stage0-execution-operations plan callee args))

(defn execute-stage0-compiled-plan
  [plan]
  (gravity.runtime/execute-native-stage0-plan stage0-execution-operations plan))
