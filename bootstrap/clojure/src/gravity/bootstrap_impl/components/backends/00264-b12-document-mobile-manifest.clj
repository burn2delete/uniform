

(defn b12-document-mobile-manifest
  [source-path input-id]
  (gravity.backend.mobile/native-manifest
   {:artifact-id #'c4-artifact-id :app-bundle #'b12-document-app-bundle
    :permission-manifest-fixture #'b12-document-permission-manifest-fixture}
   source-path input-id))
