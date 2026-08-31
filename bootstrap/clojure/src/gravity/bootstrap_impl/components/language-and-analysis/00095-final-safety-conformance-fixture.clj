

(defn final-safety-conformance-fixture
  [checker-state]
  (let [safe12 (safe12-conformance-fixture checker-state)
        safe13 (safe13-conformance-fixture checker-state)
        safe15 (safe15-conformance-fixture checker-state)
        safe16 (safe16-conformance-fixture checker-state)
        statuses (map :status [safe12 safe13 safe15 safe16])]
    {:documents [:SAFE12 :SAFE13 :SAFE15 :SAFE16]
     :document-statuses {:SAFE12 (:status safe12)
                         :SAFE13 (:status safe13)
                         :SAFE15 (:status safe15)
                         :SAFE16 (:status safe16)}
     :required-families (vec (concat safe12-required-families
                                     safe13-required-families
                                     safe15-required-families
                                     safe16-required-families))
     :covered-families (vec (sort-by name
                                     (set (concat (:covered-families safe12)
                                                  (:covered-families safe13)
                                                  (:covered-families safe15)
                                                  (:covered-families safe16)))))
     :missing-families (vec (concat (:missing-families safe12)
                                    (:missing-families safe13)
                                    (:missing-families safe15)
                                    (:missing-families safe16)))
     :status (if (every? #{:complete} statuses) :complete :incomplete)}))