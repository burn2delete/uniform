

(defn stage0-instruction-builtin-operators
  [instruction]
  (let [children (case (:op instruction)
                   :println (:args instruction)
                   :do (:body instruction)
                   :if [(:test instruction) (:then instruction)
                        (:else instruction)]
                   :let (concat (map :expr (:bindings instruction))
                                (:body instruction))
                   :builtin-call (:args instruction)
                   :function-call (:args instruction)
                   :vector-literal (:items instruction)
                   :set-literal (:items instruction)
                   :map-literal (mapcat (fn [{:keys [key value]}]
                                           [key value])
                                         (:entries instruction))
                   [])]
    (concat (when (= :builtin-call (:op instruction))
              [(:function instruction)])
            (mapcat stage0-instruction-builtin-operators
                    (remove nil? children)))))

(defn stage0-compiled-plan-builtin-operator-counts
  [plan]
  (frequencies
   (mapcat (fn [[_ function]]
             (mapcat stage0-instruction-builtin-operators
                     (:instructions function)))
           (:functions plan))))