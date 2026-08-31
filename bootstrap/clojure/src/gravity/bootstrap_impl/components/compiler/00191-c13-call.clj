
(defn- c13-call [operation & args]
  (if *c13-leaf-call?*
    (apply operation args)
    (binding [*c13-leaf-call?* true]
      (c13/with-operations (c13-optimization-ops)
        #(apply operation args)))))

(defn c13-optimization-source-overrides
  [module]
  (c13-call c13/c13-optimization-source-overrides module))

(defn c13-optimization-validate-source-overrides!
  [source-path overrides]
  (c13-call c13/c13-optimization-validate-source-overrides! source-path overrides))

(defn c13-optimization-diagnostic-catalog
  [source-path]
  (c13-call c13/c13-optimization-diagnostic-catalog source-path))

(defn c13-optimization-validate!
  [source-path artifact]
  (c13-call c13/c13-optimization-validate! source-path artifact))

(defn c13-optimization-capability-proof
  [artifact]
  (c13-call c13/c13-optimization-capability-proof artifact))

(defn compiler-c13-optimization-source-artifact
  [source-path source-text]
  (c13-call c13/compiler-c13-optimization-source-artifact source-path source-text))

(defn compiler-c13-optimization-file-artifact
  [path]
  (c13-call c13/compiler-c13-optimization-file-artifact path))

(def c14-lowering-governing-document c14/c14-lowering-governing-document)

(declare c14-lowering-source-overrides
         c14-lowering-validate-source-overrides!
         c14-lowering-diagnostic-catalog
         c14-lowering-validate!
         c14-lowering-capability-proof
         compiler-c14-lowering-source-artifact
         compiler-c14-lowering-file-artifact)

(defn- c14-lowering-ops []
  {:source-span source-span
   :c4-artifact-id c4-artifact-id
   :sha256-hex sha256-hex
   :perf-present? perf-present?
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c13-optimization-source-artifact
   compiler-c13-optimization-source-artifact
   :optimization-lowering-validate-overrides!
   optimization-lowering-validate-overrides!
   :optimization-lowering-fail! optimization-lowering-fail!
   :c14-lowering-governing-document c14-lowering-governing-document
   :c14-lowering-diagnostic-ids c14-lowering-diagnostic-ids
   :optimization-lowering-diagnostic-messages
   optimization-lowering-diagnostic-messages
   :c14-lowering-source-overrides c14-lowering-source-overrides
   :c14-lowering-validate-source-overrides!
   c14-lowering-validate-source-overrides!
   :c14-lowering-diagnostic-catalog c14-lowering-diagnostic-catalog
   :c14-lowering-validate! c14-lowering-validate!
   :c14-lowering-capability-proof c14-lowering-capability-proof
   :compiler-c14-lowering-source-artifact
   compiler-c14-lowering-source-artifact
   :compiler-c14-lowering-file-artifact compiler-c14-lowering-file-artifact})