

(defn b7-document-validate!
  [source-path artifact]
  (gravity.backend.mlir/validate-native-manifest!
   {:fail! #'b7-document-fail! :module-text #'b7-document-mlir-module
    :structurally-valid? #'b7-document-mlir-structurally-valid?
    :diagnostic-ids #'b7-document-diagnostic-ids}
   source-path artifact))

(defn b7-document-capability-proof
  [artifact]
  (gravity.backend.mlir/native-coverage-report
   {:module-text #'b7-document-mlir-module
    :structurally-valid? #'b7-document-mlir-structurally-valid?
    :diagnostic-ids #'b7-document-diagnostic-ids}
   artifact))
