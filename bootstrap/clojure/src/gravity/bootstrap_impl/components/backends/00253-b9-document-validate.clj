

(defn b9-document-validate!
  [source-path artifact]
  (gravity.backend.hdl/validate-native-manifest!
   {:fail! #'b9-document-fail! :hdl-module #'b9-document-hdl-module
    :testbench #'b9-document-testbench :timing-constraints #'b9-document-timing-constraints
    :hdl-structurally-valid? #'b9-document-hdl-structurally-valid?
    :testbench-structurally-valid? #'b9-document-testbench-structurally-valid?
    :timing-structurally-valid? #'b9-document-timing-structurally-valid?
    :diagnostic-ids #'b9-document-diagnostic-ids}
   source-path artifact))

(defn b9-document-capability-proof
  [artifact]
  (gravity.backend.hdl/native-coverage-report
   {:hdl-module #'b9-document-hdl-module :testbench #'b9-document-testbench
    :timing-constraints #'b9-document-timing-constraints
    :hdl-structurally-valid? #'b9-document-hdl-structurally-valid?
    :testbench-structurally-valid? #'b9-document-testbench-structurally-valid?
    :timing-structurally-valid? #'b9-document-timing-structurally-valid?
    :diagnostic-ids #'b9-document-diagnostic-ids}
   artifact))
