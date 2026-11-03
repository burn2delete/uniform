

(def b12-document-app-bundle
  gravity.backend.mobile/app-bundle)

(def b12-document-permission-manifest-fixture
  gravity.backend.mobile/permission-manifest-fixture)

(defn b12-document-bundle-structurally-valid?
  [text]
  (gravity.backend.mobile/bundle-structurally-valid? text))

(defn b12-document-permission-structurally-valid?
  [text]
  (gravity.backend.mobile/permission-structurally-valid? text))

(defn b12-document-diagnostic-stream
  [source-path input-id]
  (gravity.backend.mobile/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b12-document-diagnostic-ids)
    :domain-anchor (fn [id] (b12-document-domain-anchor id))
    :missing-policy (fn [id] (b12-document-missing-policy id))}))
