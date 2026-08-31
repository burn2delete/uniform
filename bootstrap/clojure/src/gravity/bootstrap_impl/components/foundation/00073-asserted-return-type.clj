

(defn asserted-return-type
  [operator args spec]
  (cond
    (and (= 'typed/assert operator) (= 2 (count args)))
    (get type-keywords (:value (first args)) (:return-type spec))

    (and (= 'typed/value operator) (= 2 (count args)))
    (or (type-token-name (first args)) (:return-type spec))

    (and (= 'typed/return operator) (= 2 (count args)))
    (or (type-token-name (first args)) (:return-type spec))

    (and (= 'dynamic/cast operator) (= 2 (count args)))
    (or (type-token-name (first args)) (:return-type spec))

    (and (= 'record/new operator) (keyword? (:value (first args))))
    (str "Record[" (name (:value (first args))) "]")

    (and (= 'union/ok operator) (keyword? (:value (first args))))
    (str "Union[" (name (:value (first args))) ".Ok]")

    (and (= 'protocol/value operator) (keyword? (:value (first args))))
    (str "Protocol[" (name (:value (first args))) "]")

    (and (= 'generic/id operator) (seq args))
    (:type (first args))

    (and (= 'schema/derive operator) (keyword? (:value (first args))))
    (str "Schema[" (name (:value (first args))) "]")

    (and (= 'schema/validate operator) (keyword? (:value (first args))))
    (str "Validated[" (name (:value (first args))) "]")

    :else
    (:return-type spec)))