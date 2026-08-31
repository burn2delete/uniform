

(defn boundary-safety-conformance-fixture
  [checker-state]
  (let [safe7 (safe7-conformance-fixture checker-state)
        safe8 (safe8-conformance-fixture checker-state)
        safe9 (safe9-conformance-fixture checker-state)
        safe11 (safe11-conformance-fixture checker-state)
        statuses (map :status [safe7 safe8 safe9 safe11])]
    {:documents [:SAFE7 :SAFE8 :SAFE9 :SAFE11]
     :document-statuses {:SAFE7 (:status safe7)
                         :SAFE8 (:status safe8)
                         :SAFE9 (:status safe9)
                         :SAFE11 (:status safe11)}
     :required-families (vec (concat safe7-required-families
                                     safe8-required-families
                                     safe9-required-families
                                     safe11-required-families))
     :covered-families (vec (sort-by name
                                     (set (concat (:covered-families safe7)
                                                  (:covered-families safe8)
                                                  (:covered-families safe9)
                                                  (:covered-families safe11)))))
     :missing-families (vec (concat (:missing-families safe7)
                                    (:missing-families safe8)
                                    (:missing-families safe9)
                                    (:missing-families safe11)))
     :status (if (every? #{:complete} statuses) :complete :incomplete)}))