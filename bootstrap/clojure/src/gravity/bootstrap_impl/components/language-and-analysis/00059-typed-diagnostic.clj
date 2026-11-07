

(defn typed-diagnostic!
  ([id message node remediation]
   (typed-diagnostic! id message node remediation {}))
  ([id message node remediation data]
   (gravity.diagnostics/fail-at-node!
    #'fail! :typed-effected-core id message node remediation data)))
