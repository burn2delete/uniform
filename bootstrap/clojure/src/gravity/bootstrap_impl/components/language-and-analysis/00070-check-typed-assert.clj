

(defn check-typed-assert!
  [node args]
  (gravity.types/check-native-assert!
   {:diagnostic! #'typed-diagnostic! :type-keywords #'type-keywords}
   node args))
