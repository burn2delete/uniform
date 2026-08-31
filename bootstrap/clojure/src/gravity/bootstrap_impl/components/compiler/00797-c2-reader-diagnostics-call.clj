

(defn- c2-reader-diagnostics-call
  [operation-key operation & args]
  (if *c2-reader-diagnostics-leaf-call?*
    (c2-reader-diagnostics/call-entrypoint-body
     operation-key operation args)
    (binding [*c2-reader-diagnostics-leaf-call?* true]
      (c2-reader-diagnostics/with-operations
       (c2-reader-diagnostics-ops)
       #(c2-reader-diagnostics/call-entrypoint-body
         operation-key operation args)))))

(defn c2-reader-source-overrides
  [module]
  (c2-reader-diagnostics-call
   :c2-reader-source-overrides
   c2-reader-diagnostics/c2-reader-source-overrides module))

(defn c2-reader-message
  [id]
  (c2-reader-diagnostics-call
   :c2-reader-message c2-reader-diagnostics/c2-reader-message id))

(defn c2-reader-fail!
  [id source-path subject extra]
  (c2-reader-diagnostics-call
   :c2-reader-fail! c2-reader-diagnostics/c2-reader-fail!
   id source-path subject extra))

(defn c2-reader-remap-exception!
  [source-path ex]
  (c2-reader-diagnostics-call
   :c2-reader-remap-exception!
   c2-reader-diagnostics/c2-reader-remap-exception! source-path ex))

(defn c2-reader-validate-overrides!
  [source-path overrides source-unit token-stream]
  (c2-reader-diagnostics-call
   :c2-reader-validate-overrides!
   c2-reader-diagnostics/c2-reader-validate-overrides!
   source-path overrides source-unit token-stream))