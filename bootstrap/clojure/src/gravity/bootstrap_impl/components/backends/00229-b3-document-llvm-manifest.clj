

(defn b3-document-llvm-manifest
  [input-id]
  (gravity.backend.llvm/native-manifest
   {:artifact-id #'c4-artifact-id :llvm-ir #'b3-document-llvm-ir}
   input-id))

(defn b3-document-validate!
  [source-path artifact]
  (gravity.backend.llvm/validate-native-manifest!
   {:fail! #'b3-document-fail! :llvm-ir #'b3-document-llvm-ir
    :diagnostic-ids #'b3-document-diagnostic-ids}
   source-path artifact))
