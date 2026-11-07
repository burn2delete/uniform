

(defn b7-document-mlir-manifest
  [input-id]
  (gravity.backend.mlir/native-manifest
   {:artifact-id #'c4-artifact-id :module-text #'b7-document-mlir-module}
   input-id))
