

(defn- c3-reader-integrity-call
  [operation & args]
  (if *c3-reader-integrity-leaf-call?*
    (apply operation args)
    (binding [*c3-reader-integrity-leaf-call?* true]
      (c3-reader-integrity/with-operations
       (c3-reader-integrity-ops)
       #(apply operation args)))))

(defn c3-c2-reader-integrity-report
  [c2-artifact]
  (c3-reader-integrity-call
   c3-reader-integrity/c3-c2-reader-integrity-report c2-artifact))

(defn c3-validate-c2-reader-artifact!
  [source-path c2-artifact]
  (c3-reader-integrity-call
   c3-reader-integrity/c3-validate-c2-reader-artifact!
   source-path c2-artifact))

(declare c3-deferred-ratio-descriptor-from-raw
         c3-ratio-descriptor-from-raw
         c3-lossless-literal-descriptor
         c3-tagged-literal-descriptor
         c3-source-form-kind
         c3-source-facts)

(defn- c3-literal-projection-ops
  []
  {:c3-c2-reader-integrity-report c3-c2-reader-integrity-report
   :form-kind form-kind
   :c3-deferred-ratio-descriptor-from-raw
   c3-deferred-ratio-descriptor-from-raw
   :c3-ratio-descriptor-from-raw c3-ratio-descriptor-from-raw
   :c3-lossless-literal-descriptor c3-lossless-literal-descriptor
   :c3-tagged-literal-descriptor c3-tagged-literal-descriptor
   :c3-source-form-kind c3-source-form-kind
   :c3-source-facts c3-source-facts})