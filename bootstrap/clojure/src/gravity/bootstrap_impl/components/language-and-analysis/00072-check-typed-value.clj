

(defn check-typed-value!
  [node args]
  (gravity.types/check-native-value!
   {:diagnostic! #'typed-diagnostic! :type-token-name #'type-token-name
    :literal-type #'literal-type :compatible-value-type? #'compatible-value-type?}
   node args))

(defn check-typed-return!
  [node args]
  (gravity.types/check-native-return!
   {:diagnostic! #'typed-diagnostic! :type-token-name #'type-token-name
    :compatible-value-type? #'compatible-value-type?}
   node args))
