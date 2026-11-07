

(defn b12-document-validate!
  [source-path artifact]
  (gravity.backend.mobile/validate-native-manifest!
   {:fail! #'b12-document-fail! :app-bundle #'b12-document-app-bundle
    :permission-manifest-fixture #'b12-document-permission-manifest-fixture
    :bundle-structurally-valid? #'b12-document-bundle-structurally-valid?
    :permission-structurally-valid? #'b12-document-permission-structurally-valid?
    :diagnostic-ids #'b12-document-diagnostic-ids}
   source-path artifact))

(defn b12-document-capability-proof
  [artifact]
  (gravity.backend.mobile/native-coverage-report
   {:app-bundle #'b12-document-app-bundle
    :permission-manifest-fixture #'b12-document-permission-manifest-fixture
    :bundle-structurally-valid? #'b12-document-bundle-structurally-valid?
    :permission-structurally-valid? #'b12-document-permission-structurally-valid?
    :diagnostic-ids #'b12-document-diagnostic-ids}
   artifact))
