

(def b10-document-workflow-graph
  gravity.backend.workflow/workflow-graph)

(def b10-document-replay-fixture
  gravity.backend.workflow/replay-fixture)

(defn b10-document-workflow-graph-structurally-valid?
  [text]
  (gravity.backend.workflow/workflow-graph-structurally-valid? text))

(defn b10-document-replay-fixture-structurally-valid?
  [text]
  (gravity.backend.workflow/replay-fixture-structurally-valid? text))

(defn b10-document-diagnostic-stream
  [source-path input-id]
  (gravity.backend.workflow/diagnostic-stream
   source-path input-id
   {:source-span (fn [path index] (source-span path index))
    :diagnostic-ids (fn [] b10-document-diagnostic-ids)
    :step-id (fn [id] (b10-document-step-id id))
    :missing-policy (fn [id] (b10-document-missing-policy id))}))
