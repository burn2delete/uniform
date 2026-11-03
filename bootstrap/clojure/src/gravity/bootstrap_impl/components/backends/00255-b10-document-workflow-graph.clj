

(def b10-document-workflow-graph
  (str
   "{:workflow :gravity-stage0-workflow\n"
   " :runtime :durable-workflow\n"
   " :nodes [{:id :start :kind :deterministic-computation}\n"
   "         {:id :call-model :kind :model-call :provider :stage0-model-provider}\n"
   "         {:id :call-tool :kind :tool-call :provider :stage0-tool-provider}\n"
   "         {:id :approve-output :kind :human-review-gate :capability :ai/human-review}\n"
   "         {:id :write-ticket :kind :external-service-call :effect :network/request}\n"
   "         {:id :compensate-ticket :kind :compensation-handler}\n"
   "         {:id :done :kind :deterministic-computation}]\n"
   " :edges [{:from :start :to :call-model :kind :data}\n"
   "         {:from :call-model :to :call-tool :kind :tool-input}\n"
   "         {:from :call-tool :to :approve-output :kind :taint-validation}\n"
   "         {:from :approve-output :to :write-ticket :kind :human-reviewed-write}\n"
   "         {:from :write-ticket :to :done :kind :success}\n"
   "         {:from :write-ticket :to :compensate-ticket :kind :compensation}]\n"
   " :replay-barriers [:call-model :call-tool :write-ticket]\n"
   " :status :complete}\n"))

(def b10-document-replay-fixture
  (str
   "{:workflow-input-digest \"sha256:workflow-input-stage0\"\n"
   " :events [{:event :started :step :start :cycle 0}\n"
   "          {:event :model-output-recorded :step :call-model :digest \"sha256:model-output-stage0\"}\n"
   "          {:event :tool-output-recorded :step :call-tool :digest \"sha256:tool-output-stage0\"}\n"
   "          {:event :human-reviewed :step :approve-output :decision :approved}\n"
   "          {:event :external-write-idempotent :step :write-ticket :idempotency-key \"workflow-input-hash\"}]\n"
   " :replay-mode :event-log\n"
   " :side-effects-reissued false\n"
   " :status :complete}\n"))

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
