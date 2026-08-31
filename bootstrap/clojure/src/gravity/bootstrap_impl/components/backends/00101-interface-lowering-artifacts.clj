

(defn interface-lowering-artifacts
  [implementation-table dispatch-mode-records]
  (vec (concat
        (map (fn [implementation]
               {:protocol (:protocol implementation)
                :type (:type implementation)
                :method (:method implementation)
                :lowering (:dispatch-mode implementation)
                :implementation (:implementation implementation)})
             implementation-table)
        (map (fn [record]
               {:protocol (:protocol record)
                :type (:receiver record)
                :method (:method record)
                :lowering (:dispatch-mode record)
                :implementation (:implementation record)})
             (filter #(contains? #{:dictionary :vtable :host-interface}
                                  (:dispatch-mode %))
                     dispatch-mode-records)))))