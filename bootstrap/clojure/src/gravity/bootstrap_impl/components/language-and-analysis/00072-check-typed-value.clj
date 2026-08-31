

(defn check-typed-value!
  [node args]
  (when-not (and (= 2 (count args)) (= "Keyword" (:type (first args))))
    (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                       "typed/value requires a type keyword and value"
                       node
                       "Use (typed/value :Type value)."))
  (let [expected (type-token-name (first args))
        value-fact (second args)
        actual (if (= "QuotedData" (:type value-fact))
                 (literal-type (:value value-fact))
                 (:type value-fact))]
    (when-not expected
      (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                         "typed/value uses an unknown type keyword"
                         node
                         "Use a registered L5 type keyword."))
    (when-not (compatible-value-type? expected actual)
      (typed-diagnostic! "L5-TYPE-MISMATCH"
                         "typed/value expression does not satisfy the declared type"
                         node
                         "Change the fixture value or the declared type."
                         {:expected-type expected
                          :actual-type actual}))
    expected))

(defn check-typed-return!
  [node args]
  (when-not (= 2 (count args))
    (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                       "typed/return requires a return type and value"
                       node
                       "Use (typed/return (quote Type) value)."))
  (let [expected (type-token-name (first args))
        actual (:type (second args))]
    (when-not expected
      (typed-diagnostic! "L5-ANNOTATION-REQUIRED"
                         "typed/return uses an unknown return type"
                         node
                         "Use a registered return type."))
    (when-not (compatible-value-type? expected actual)
      (typed-diagnostic! "L5-TYPE-MISMATCH"
                         "function return expression does not match its declared type"
                         node
                         "Change the function body or declared return type."
                         {:expected-type expected
                          :actual-type actual}))
    expected))