

(defn optimization-lowering-validate-overrides!
  [source-path artifact]
  (optimization-lowering-call :optimization-lowering-validate-overrides!
                              optimization-lowering/optimization-lowering-validate-overrides!
                              source-path artifact))

(defn optimization-lowering-validate!
  [source-path artifact]
  (optimization-lowering-call :optimization-lowering-validate!
                              optimization-lowering/optimization-lowering-validate!
                              source-path artifact))

(defn optimization-lowering-capability-proof
  [artifact]
  (optimization-lowering-call :optimization-lowering-capability-proof
                              optimization-lowering/optimization-lowering-capability-proof
                              artifact))

(defn optimization-lowering-source-artifact
  [source-path source-text]
  (optimization-lowering-call :optimization-lowering-source-artifact
                              optimization-lowering/optimization-lowering-source-artifact
                              source-path source-text))