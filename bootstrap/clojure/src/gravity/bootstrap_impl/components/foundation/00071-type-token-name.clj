

(defn type-token-name
  [fact]
  (cond
    (= "Keyword" (:type fact)) (get type-keywords (:value fact))
    (= "QuotedData" (:type fact)) (type-form-name (:value fact))
    (string? (:value fact)) (:value fact)
    :else nil))

(def primitive-compatible-types
  #{"Nil" "Unit" "Boolean" "I8" "I16" "I32" "I64" "U8" "U16" "U32" "U64"
    "Int" "Integer" "BigInt" "F32" "F64" "Exact" "ExactRatio" "Symbol"
    "Keyword" "String" "Text" "List" "Vector" "Map" "Set" "Tuple" "Dynamic"})

(defn compatible-value-type?
  [expected actual]
  (or (not (contains? primitive-compatible-types expected))
      (= expected actual)
      (and (#{"I8" "I16" "I32" "I64" "U8" "U16" "U32" "U64" "Int" "BigInt"} expected)
           (= "Integer" actual))
      (and (= "F32" expected) (= "F64" actual))
      (and (= "Exact" expected) (#{"Integer" "ExactRatio"} actual))
      (and (= "Text" expected) (= "String" actual))
      (and (= "Tuple" expected) (= "Vector" actual))
      (and (= "Unit" expected) (= "Nil" actual))))