

(def b9-document-hdl-module
  gravity.backend.hdl/hdl-module)

(def b9-document-testbench
  gravity.backend.hdl/testbench)

(def b9-document-timing-constraints
  gravity.backend.hdl/timing-constraints)

(defn b9-document-hdl-structurally-valid?
  [text]
  (gravity.backend.hdl/hdl-structurally-valid? text))

(defn b9-document-testbench-structurally-valid?
  [text]
  (gravity.backend.hdl/testbench-structurally-valid? text))

(defn b9-document-timing-structurally-valid?
  [text]
  (gravity.backend.hdl/timing-structurally-valid? text))

(defn b9-document-diagnostic-stream
  [source-path input-id]
  (gravity.backend.hdl/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b9-document-diagnostic-ids)
    :signal-id (fn [id] (b9-document-signal-id id))
    :missing-fact (fn [id] (b9-document-missing-fact id))}))
