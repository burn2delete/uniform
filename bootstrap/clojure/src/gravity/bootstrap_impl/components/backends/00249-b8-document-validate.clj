

(defn b8-document-validate!
  [source-path artifact]
  (gravity.backend.gpu/validate-native-manifest!
   {:fail! #'b8-document-fail! :kernel-text #'b8-document-kernel-module
    :host-stub-text #'b8-document-host-stub
    :kernel-structurally-valid? #'b8-document-kernel-structurally-valid?
    :host-stub-structurally-valid? #'b8-document-host-stub-structurally-valid?
    :diagnostic-ids #'b8-document-diagnostic-ids}
   source-path artifact))

(defn b8-document-capability-proof
  [artifact]
  (gravity.backend.gpu/native-coverage-report
   {:kernel-text #'b8-document-kernel-module :host-stub-text #'b8-document-host-stub
    :kernel-structurally-valid? #'b8-document-kernel-structurally-valid?
    :host-stub-structurally-valid? #'b8-document-host-stub-structurally-valid?
    :diagnostic-ids #'b8-document-diagnostic-ids}
   artifact))
