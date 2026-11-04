

(defn c2-reader-capability-proof
  [artifact]
  (c2-reader-product-projection-call
   :c2-reader-capability-proof
   c2-reader-product-projection/c2-reader-capability-proof
   artifact))

(defn c2-reader-validate!
  [source-path artifact]
  (gravity.reader/validate-native-reader-report!
   #'c2-reader-capability-proof #'c2-reader-fail! #'source-span source-path artifact))

(defn c2-reader-overrides-from-forms
  [forms]
  (c2-reader-product-projection-call
   :c2-reader-overrides-from-forms
   c2-reader-product-projection/c2-reader-overrides-from-forms
   forms))

(defn c2-reader-extension-invocations
  [form-tree]
  (c2-reader-product-projection-call
   :c2-reader-extension-invocations
   c2-reader-product-projection/c2-reader-extension-invocations
   form-tree))
