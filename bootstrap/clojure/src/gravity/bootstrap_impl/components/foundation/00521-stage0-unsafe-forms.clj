

(defn stage0-unsafe-forms
  [form]
  (gravity.safety/native-unsafe-forms #'stage0-unsafe-forms form))

(defn validate-stage0-unsafe-island!
  [module form]
  (gravity.safety/validate-native-unsafe-island!
   #'fail! #'clojure.set/difference #'stage0-unsafe-required-metadata module form))
