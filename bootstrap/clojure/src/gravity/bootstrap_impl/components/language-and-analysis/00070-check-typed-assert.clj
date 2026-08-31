

(defn check-typed-assert!
  [node args]
  (when-not (and (= 2 (count args))
                 (= "Keyword" (:type (first args))))
    (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                       "typed/assert requires a type keyword and value"
                       node
                       "Use (typed/assert :Type value)."))
  (let [expected (get type-keywords (:value (first args)))
        actual (:type (second args))]
    (when-not expected
      (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                         "typed/assert uses an unknown type keyword"
                         node
                         "Use a registered type keyword."))
    (when (not= expected actual)
      (typed-diagnostic! "L5-TYPE-MISMATCH"
                         "expression type does not match declared assertion"
                         node
                         "Change the expression type or the declared assertion."
                         {:expected-type expected
                          :actual-type actual}))))