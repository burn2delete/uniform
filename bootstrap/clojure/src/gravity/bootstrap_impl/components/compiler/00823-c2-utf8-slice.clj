

(declare c2-utf8-slice
         c2-span-encloses?
         c2-spans-source-ordered?
         c2-form-graph-metrics
         c2-lexical-product-validation)

(defn- c2-lexical-validation-ops
  []
  {:c2-utf8-slice c2-utf8-slice
   :c2-span-encloses? c2-span-encloses?
   :c2-spans-source-ordered? c2-spans-source-ordered?
   :c2-form-graph-metrics c2-form-graph-metrics
   :c2-lexical-product-validation c2-lexical-product-validation})