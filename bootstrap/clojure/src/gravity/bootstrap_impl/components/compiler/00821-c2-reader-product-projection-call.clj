

(defn- c2-reader-product-projection-call
  [operation-key operation & args]
  (if *c2-reader-product-projection-leaf-call?*
    (c2-reader-product-projection/call-entrypoint-body
     operation-key operation args)
    (binding [*c2-reader-product-projection-leaf-call?* true]
      (c2-reader-product-projection/with-operations
       (c2-reader-product-projection-ops)
       #(c2-reader-product-projection/call-entrypoint-body
         operation-key operation args)))))

(defn c2-syntax-seed-stream
  [source-path products module-context]
  (c2-reader-product-projection-call
   :c2-syntax-seed-stream
   c2-reader-product-projection/c2-syntax-seed-stream
   source-path products module-context))

(defn c2-deferred-semantic-literals
  [form-tree]
  (c2-reader-product-projection-call
   :c2-deferred-semantic-literals
   c2-reader-product-projection/c2-deferred-semantic-literals
   form-tree))

(defn c2-top-level-products
  [artifact]
  (c2-reader-product-projection-call
   :c2-top-level-products
   c2-reader-product-projection/c2-top-level-products
   artifact))