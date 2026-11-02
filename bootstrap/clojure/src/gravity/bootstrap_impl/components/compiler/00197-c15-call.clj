
(defn- c15-call [operation & args]
  (if *c15-leaf-call?*
    (apply operation args)
    (binding [*c15-leaf-call?* true]
      (c15/with-operations (c15-diagnostics-ops)
        #(apply operation args)))))

(defn c15-diagnostics-source-overrides [module]
  (c15-call c15/c15-diagnostics-source-overrides module))

(defn c15-stable-diagnostic-id [diagnostic]
  (c15-call c15/c15-stable-diagnostic-id diagnostic))

(defn c15-diagnostics-fail! [id source-path subject extra]
  (c15-call c15/c15-diagnostics-fail! id source-path subject extra))

(defn c15-diagnostics-validate-source-overrides! [source-path overrides]
  (c15-call c15/c15-diagnostics-validate-source-overrides!
            source-path overrides))

(defn c15-diagnostic-record
  [rule severity stage message-key source-path form-index primary-artifact
   facts remediation & {:keys [related origin-chain redactions lifecycle
                               generated?]}]
  (c15-call c15/c15-diagnostic-record
            rule severity stage message-key source-path form-index
            primary-artifact facts remediation
            :related related :origin-chain origin-chain
            :redactions redactions :lifecycle lifecycle :generated? generated?))

(defn c15-diagnostic-catalog []
  (c15-call c15/c15-diagnostic-catalog))

(defn c15-diagnostics-validate! [source-path artifact]
  (c15-call c15/c15-diagnostics-validate! source-path artifact))

(defn c15-diagnostics-capability-proof [artifact]
  (c15-call c15/c15-diagnostics-capability-proof artifact))

(defn compiler-c15-diagnostics-source-artifact [source-path source-text]
  (c15-call c15/compiler-c15-diagnostics-source-artifact
            source-path source-text))

(defn compiler-c15-diagnostics-file-artifact [path]
  (c15-call c15/compiler-c15-diagnostics-file-artifact path))

(def c16-incremental-governing-document
  c16/c16-incremental-governing-document)
(def c16-incremental-diagnostic-ids c16/c16-incremental-diagnostic-ids)
(def c16-cache-key-required-fields c16/c16-cache-key-required-fields)
(def c16-invalidation-causes c16/c16-invalidation-causes)

(declare c16-incremental-source-overrides
         c16-incremental-fail!
         c16-incremental-validate-source-overrides!
         c16-stage-cache-key
         c16-incremental-diagnostic-stream
         c16-incremental-validate!
         c16-incremental-capability-proof
         compiler-c16-incremental-source-artifact
         compiler-c16-incremental-file-artifact)

(defn- c16-incremental-ops []
  {:fail! fail!
   :source-span source-span
   :sha256-hex sha256-hex
   :c4-artifact-id c4-artifact-id
   :perf-present? perf-present?
   :read-source-form-records read-source-form-records
   :validate-ns-syntax! validate-ns-syntax!
   :parse-module parse-module
   :compiler-c15-diagnostics-source-artifact
   compiler-c15-diagnostics-source-artifact
   :compiler-verification-diagnostic-messages
   compiler-verification-diagnostic-messages
   :compiler-verification-override-diagnostics
   compiler-verification-override-diagnostics
   :c16-incremental-governing-document c16-incremental-governing-document
   :c16-incremental-diagnostic-ids c16-incremental-diagnostic-ids
   :c16-cache-key-required-fields c16-cache-key-required-fields
   :c16-invalidation-causes c16-invalidation-causes
   :c16-incremental-source-overrides c16-incremental-source-overrides
   :c16-incremental-fail! c16-incremental-fail!
   :c16-incremental-validate-source-overrides!
   c16-incremental-validate-source-overrides!
   :c16-stage-cache-key c16-stage-cache-key
   :c16-incremental-diagnostic-stream c16-incremental-diagnostic-stream
   :c16-incremental-validate! c16-incremental-validate!
   :c16-incremental-capability-proof c16-incremental-capability-proof
   :compiler-c16-incremental-source-artifact
   compiler-c16-incremental-source-artifact
   :compiler-c16-incremental-file-artifact
   compiler-c16-incremental-file-artifact})