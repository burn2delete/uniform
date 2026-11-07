

(defn b9-document-hdl-manifest
  [source-path input-id]
  (gravity.backend.hdl/native-manifest
   {:artifact-id #'c4-artifact-id :hdl-module #'b9-document-hdl-module
    :testbench #'b9-document-testbench :timing-constraints #'b9-document-timing-constraints}
   source-path input-id))
