

(defn p1-diagnostic-id
  [data]
  (let [id (:id data)]
    (cond
      (and (= "L3-NS-MISSING" id)
           (not= :profile (:missing data))) nil
      :else (get p1-underlying-diagnostic-map id))))

(defn throw-p1-diagnostic!
  [ex]
  (let [data (ex-data ex)]
    (if-let [id (p1-diagnostic-id data)]
      (throw (diagnostic id
                         (get p1-diagnostic-messages id (:message data))
                         (merge (dissoc data :id :message)
                                {:underlying-diagnostic (:id data)
                                 :underlying-message (:message data)
                                 :active-profile (or (:active-profile data)
                                                     (:profile data))
                                 :target (:target data)
                                 :legal-alternative (:remediation data)
                                 :diagnostic-family :profile-validation})))
      (throw ex))))