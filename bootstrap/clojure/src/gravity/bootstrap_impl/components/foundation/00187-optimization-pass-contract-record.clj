

(defn optimization-pass-contract-record
  [record]
  (optimization-lowering-call :optimization-pass-contract-record
                              optimization-lowering/optimization-pass-contract-record
                              record))

(defn optimization-decision-record
  [domain-ir-artifact input-id index contract]
  (optimization-lowering-call :optimization-decision-record
                              optimization-lowering/optimization-decision-record
                              domain-ir-artifact input-id index contract))