
(defn- c17-call [operation & args]
  (if *c17-leaf-call?*
    (apply operation args)
    (binding [*c17-leaf-call?* true]
      (c17/with-operations (c17-plugin-ops)
        #(apply operation args)))))

(defn c17-plugin-source-overrides [module]
  (c17-call c17/c17-plugin-source-overrides module))

(defn c17-plugin-fail! [id source-path subject extra]
  (c17-call c17/c17-plugin-fail! id source-path subject extra))

(defn c17-plugin-validate-source-overrides! [source-path overrides]
  (c17-call c17/c17-plugin-validate-source-overrides!
            source-path overrides))

(defn c17-plugin-diagnostic-stream [source-path plugin-manifest input-id]
  (c17-call c17/c17-plugin-diagnostic-stream
            source-path plugin-manifest input-id))

(defn c17-plugin-validate! [source-path artifact]
  (c17-call c17/c17-plugin-validate! source-path artifact))

(defn c17-plugin-capability-proof [artifact]
  (c17-call c17/c17-plugin-capability-proof artifact))

(defn compiler-c17-plugin-source-artifact [source-path source-text]
  (c17-call c17/compiler-c17-plugin-source-artifact
            source-path source-text))

(defn compiler-c17-plugin-file-artifact [path]
  (c17-call c17/compiler-c17-plugin-file-artifact path))

(def c18-verification-governing-document
  c18/c18-verification-governing-document)
(def c18-verification-diagnostic-ids c18/c18-verification-diagnostic-ids)
(def c18-pass-risk-required-fields c18/c18-pass-risk-required-fields)
(def c18-trust-report-required-fields c18/c18-trust-report-required-fields)

(declare c18-verification-source-overrides
         c18-verification-fail!
         c18-verification-validate-source-overrides!
         c18-verification-diagnostic-stream
         c18-pass-risk-records
         c18-verification-validate!
         c18-verification-capability-proof
         compiler-c18-verification-source-artifact
         compiler-c18-verification-file-artifact)

(defn- c18-verification-ops []
  {:fail! fail!
   :source-span source-span
   :c4-artifact-id c4-artifact-id
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c17-plugin-source-artifact compiler-c17-plugin-source-artifact
   :compiler-verification-diagnostic-messages
   compiler-verification-diagnostic-messages
   :compiler-verification-override-diagnostics
   compiler-verification-override-diagnostics
   :c18-verification-governing-document
   c18-verification-governing-document
   :c18-verification-diagnostic-ids c18-verification-diagnostic-ids
   :c18-pass-risk-required-fields c18-pass-risk-required-fields
   :c18-trust-report-required-fields c18-trust-report-required-fields
   :c18-verification-source-overrides c18-verification-source-overrides
   :c18-verification-fail! c18-verification-fail!
   :c18-verification-validate-source-overrides!
   c18-verification-validate-source-overrides!
   :c18-verification-diagnostic-stream c18-verification-diagnostic-stream
   :c18-pass-risk-records c18-pass-risk-records
   :c18-verification-validate! c18-verification-validate!
   :c18-verification-capability-proof c18-verification-capability-proof
   :compiler-c18-verification-source-artifact
   compiler-c18-verification-source-artifact
   :compiler-c18-verification-file-artifact
   compiler-c18-verification-file-artifact})