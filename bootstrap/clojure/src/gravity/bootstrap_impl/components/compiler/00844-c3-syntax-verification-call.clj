

(defn- c3-syntax-verification-call
  [operation & args]
  (if *c3-syntax-verification-leaf-call?*
    (apply operation args)
    (binding [*c3-syntax-verification-leaf-call?* true]
      (c3-syntax-verification/with-operations
       (c3-syntax-verification-ops)
       #(apply operation args)))))

(defn c3-syntax-verification-report
  ([syntax-stream serialization]
   (c3-syntax-verification-call
    c3-syntax-verification/c3-syntax-verification-report
    syntax-stream serialization))
  ([syntax-stream serialization c2-artifact]
   (c3-syntax-verification-call
    c3-syntax-verification/c3-syntax-verification-report
    syntax-stream serialization c2-artifact))
  ([syntax-stream serialization c2-artifact gravity-boundary]
   (c3-syntax-verification-call
    c3-syntax-verification/c3-syntax-verification-report
    syntax-stream serialization c2-artifact gravity-boundary)))

(defn c3-syntax-capability-proof
  [artifact]
  (c3-syntax-verification-call
   c3-syntax-verification/c3-syntax-capability-proof artifact))

(defn c3-syntax-validate!
  [source-path artifact]
  (c3-syntax-verification-call
   c3-syntax-verification/c3-syntax-validate! source-path artifact))