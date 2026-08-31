
(defn- c16-call [operation & args]
  (if *c16-leaf-call?*
    (apply operation args)
    (binding [*c16-leaf-call?* true]
      (c16/with-operations (c16-incremental-ops)
        #(apply operation args)))))

(defn c16-incremental-source-overrides [module]
  (c16-call c16/c16-incremental-source-overrides module))

(defn c16-incremental-fail! [id source-path subject extra]
  (c16-call c16/c16-incremental-fail! id source-path subject extra))

(defn c16-incremental-validate-source-overrides! [source-path overrides]
  (c16-call c16/c16-incremental-validate-source-overrides!
            source-path overrides))

(defn c16-stage-cache-key [stage source-hash dependency-hash]
  (c16-call c16/c16-stage-cache-key stage source-hash dependency-hash))

(defn c16-incremental-diagnostic-stream [source-path input-id]
  (c16-call c16/c16-incremental-diagnostic-stream source-path input-id))

(defn c16-incremental-validate! [source-path artifact]
  (c16-call c16/c16-incremental-validate! source-path artifact))

(defn c16-incremental-capability-proof [artifact]
  (c16-call c16/c16-incremental-capability-proof artifact))

(defn compiler-c16-incremental-source-artifact [source-path source-text]
  (c16-call c16/compiler-c16-incremental-source-artifact
            source-path source-text))

(defn compiler-c16-incremental-file-artifact [path]
  (c16-call c16/compiler-c16-incremental-file-artifact path))

(def c17-plugin-governing-document c17/c17-plugin-governing-document)
(def c17-plugin-diagnostic-ids c17/c17-plugin-diagnostic-ids)
(def c17-plugin-manifest-required-fields
  c17/c17-plugin-manifest-required-fields)
(def c17-plugin-pass-contract-required-fields
  c17/c17-plugin-pass-contract-required-fields)
(def c17-plugin-cache-key-required-fields
  c17/c17-plugin-cache-key-required-fields)

(declare c17-plugin-source-overrides
         c17-plugin-fail!
         c17-plugin-validate-source-overrides!
         c17-plugin-diagnostic-stream
         c17-plugin-validate!
         c17-plugin-capability-proof
         compiler-c17-plugin-source-artifact
         compiler-c17-plugin-file-artifact)

(defn- c17-plugin-ops []
  {:fail! fail!
   :source-span source-span
   :sha256-hex sha256-hex
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c16-incremental-source-artifact
   compiler-c16-incremental-source-artifact
   :compiler-verification-diagnostic-messages
   compiler-verification-diagnostic-messages
   :compiler-verification-override-diagnostics
   compiler-verification-override-diagnostics
   :c17-plugin-governing-document c17-plugin-governing-document
   :c17-plugin-diagnostic-ids c17-plugin-diagnostic-ids
   :c17-plugin-manifest-required-fields
   c17-plugin-manifest-required-fields
   :c17-plugin-pass-contract-required-fields
   c17-plugin-pass-contract-required-fields
   :c17-plugin-cache-key-required-fields
   c17-plugin-cache-key-required-fields
   :c17-plugin-source-overrides c17-plugin-source-overrides
   :c17-plugin-fail! c17-plugin-fail!
   :c17-plugin-validate-source-overrides!
   c17-plugin-validate-source-overrides!
   :c17-plugin-diagnostic-stream c17-plugin-diagnostic-stream
   :c17-plugin-validate! c17-plugin-validate!
   :c17-plugin-capability-proof c17-plugin-capability-proof
   :compiler-c17-plugin-source-artifact
   compiler-c17-plugin-source-artifact
   :compiler-c17-plugin-file-artifact compiler-c17-plugin-file-artifact})