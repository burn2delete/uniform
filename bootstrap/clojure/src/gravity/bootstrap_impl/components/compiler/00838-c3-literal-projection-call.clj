

(defn- c3-literal-projection-call
  [operation & args]
  (if *c3-literal-projection-leaf-call?*
    (apply operation args)
    (binding [*c3-literal-projection-leaf-call?* true]
      (c3-literal-projection/with-operations
       (c3-literal-projection-ops)
       #(apply operation args)))))

(defn c3-deferred-ratio-descriptor-from-raw
  [raw]
  (c3-literal-projection-call
   c3-literal-projection/c3-deferred-ratio-descriptor-from-raw raw))

(defn c3-ratio-descriptor-from-raw
  [raw]
  (c3-literal-projection-call
   c3-literal-projection/c3-ratio-descriptor-from-raw raw))

(defn c3-lossless-literal-descriptor
  [seed form-record c2-artifact integrity-report]
  (c3-literal-projection-call
   c3-literal-projection/c3-lossless-literal-descriptor
   seed form-record c2-artifact integrity-report))

(defn c3-tagged-literal-descriptor
  [seed form-record c2-artifact integrity-report]
  (c3-literal-projection-call
   c3-literal-projection/c3-tagged-literal-descriptor
   seed form-record c2-artifact integrity-report))

(defn c3-source-form-kind
  [seed form-record c2-artifact integrity-report]
  (c3-literal-projection-call
   c3-literal-projection/c3-source-form-kind
   seed form-record c2-artifact integrity-report))

(defn c3-source-facts
  [seed form-record c2-artifact integrity-report]
  (c3-literal-projection-call
   c3-literal-projection/c3-source-facts
   seed form-record c2-artifact integrity-report))

(declare c3-path-neutral-origin
         c3-identity-input
         c3-stable-syntax-id
         c3-syntax-object
         c3-generated-syntax-object)

(defn- c3-syntax-construction-ops
  []
  {:c2-path-neutral-span c2-path-neutral-span
   :sha256-hex sha256-hex
   :c3-origin-chain c3-origin-chain
   :c3-source-form-kind c3-source-form-kind
   :c3-source-facts c3-source-facts
   :c3-path-neutral-origin c3-path-neutral-origin
   :c3-identity-input c3-identity-input
   :c3-stable-syntax-id c3-stable-syntax-id
   :c3-syntax-object c3-syntax-object
   :c3-generated-syntax-object c3-generated-syntax-object})