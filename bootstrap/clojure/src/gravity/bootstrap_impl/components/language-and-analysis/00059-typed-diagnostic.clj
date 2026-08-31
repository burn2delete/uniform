

(defn typed-diagnostic!
  ([id message node remediation]
   (typed-diagnostic! id message node remediation {}))
  ([id message node remediation data]
   (fail! id message
          (merge {:source-span (:source-span node)
                  :analyzer-stage :typed-effected-core
                  :remediation remediation}
                 data))))