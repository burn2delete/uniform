

(defn b8-document-gpu-manifest
  [source-path input-id]
  (gravity.backend.gpu/native-manifest
   {:artifact-id #'c4-artifact-id :kernel-text #'b8-document-kernel-module
    :host-stub-text #'b8-document-host-stub}
   source-path input-id))
