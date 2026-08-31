

(defn- c2-lexical-validation-call
  [operation & args]
  (if *c2-lexical-validation-leaf-call?*
    (apply operation args)
    (binding [*c2-lexical-validation-leaf-call?* true]
      (c2-lexical-validation/with-operations
       (c2-lexical-validation-ops)
       #(apply operation args)))))

(defn c2-utf8-slice
  [source-bytes byte-start byte-end]
  (c2-lexical-validation-call
   c2-lexical-validation/c2-utf8-slice
   source-bytes byte-start byte-end))

(defn c2-span-encloses?
  [parent child]
  (c2-lexical-validation-call
   c2-lexical-validation/c2-span-encloses? parent child))

(defn c2-spans-source-ordered?
  [spans]
  (c2-lexical-validation-call
   c2-lexical-validation/c2-spans-source-ordered? spans))

(defn c2-form-graph-metrics
  [form-tree]
  (c2-lexical-validation-call
   c2-lexical-validation/c2-form-graph-metrics form-tree))

(defn c2-lexical-product-validation
  [source-text token-stream form-tree root-form-ids]
  (c2-lexical-validation-call
   c2-lexical-validation/c2-lexical-product-validation
   source-text token-stream form-tree root-form-ids))