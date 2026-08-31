

(defn consume-linear-resource!
  [checker ctx node args]
  (when (or (empty? args) (not= :symbol (:source-kind (first args))))
    (typed-diagnostic! "L10-LINEAR-RESOURCE"
                       "resource close requires a linear resource symbol"
                       node
                       "Close the named linear resource exactly once."))
  (let [name (:name (first args))
        state (get-in @ctx [:linear-resources name])]
    (when (or (nil? state) (:consumed? state))
      (typed-diagnostic! "L10-LINEAR-RESOURCE"
                         "linear resource is unavailable or already consumed"
                         node
                         "Close each linear resource exactly once."
                         {:resource name}))
    (swap! ctx assoc-in [:linear-resources name] (assoc state :consumed? true))
    (record-checker! checker :linear-resource-table
                     {:name name
                      :resource-type (:resource-type state)
                      :opened-at (:node-id state)
                      :closed-at (:node-id node)
                      :status :consumed})))