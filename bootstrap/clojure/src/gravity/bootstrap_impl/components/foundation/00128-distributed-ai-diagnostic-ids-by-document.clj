

(def distributed-ai-diagnostic-ids-by-document
  {"P9" ["P9-REPLAY" "P9-SCHEMA" "P9-MIGRATION" "P9-RETRY"
         "P9-COMPENSATION" "P9-CAPABILITY" "P9-EFFECT" "P9-RAW"
         "P9-SERVICE-ERROR" "P9-EVENT-LOG"]
   "P10" ["P10-MODEL" "P10-TOOL" "P10-PROMPT" "P10-MEMORY"
          "P10-SECRET" "P10-GENERATED" "P10-REPLAY" "P10-BUDGET"
          "P10-DESTRUCTIVE" "P10-RAW"]})

(def distributed-ai-diagnostic-ids
  (vec (mapcat distributed-ai-diagnostic-ids-by-document ["P9" "P10"])))

(def distributed-ai-diagnostic-mapping
  {[:distributed "P1-EFFECT"] "P9-EFFECT"
   [:distributed "P1-CAPABILITY"] "P9-CAPABILITY"
   [:distributed "P1-MEMORY"] "P9-RAW"
   [:distributed "P1-RUNTIME"] "P9-REPLAY"
   [:distributed "P1-CROSS-IMPORT"] "P9-SCHEMA"
   [:ai "P1-EFFECT"] "P10-MODEL"
   [:ai "P1-CAPABILITY"] "P10-TOOL"
   [:ai "P1-MEMORY"] "P10-RAW"
   [:ai "P1-RUNTIME"] "P10-REPLAY"
   [:ai "P1-CROSS-IMPORT"] "P10-TOOL"})

(def distributed-ai-required-artifacts
  {:distributed [:workflow-graph :message-schema-bundle :event-log-schema
                 :retry-timeout-compensation-table
                 :external-service-capability-manifest
                 :replay-policy-log-schema :persistence-boundary-records
                 :distributed-conformance-results]
   :ai [:agent-manifest :model-call-trace-schema :prompt-provenance-record
        :tool-capability-manifest :tool-schema-bundle :memory-policy-record
        :policy-human-review-graph :replay-log-schema
        :generated-code-safety-record :ai-conformance-results]})

(def distributed-ai-artifact-diagnostic-by-key
  {:distributed {:workflow-graph "P9-REPLAY"
                 :message-schema-bundle "P9-SCHEMA"
                 :event-log-schema "P9-EVENT-LOG"
                 :retry-timeout-compensation-table "P9-RETRY"
                 :external-service-capability-manifest "P9-CAPABILITY"
                 :replay-policy-log-schema "P9-REPLAY"
                 :persistence-boundary-records "P9-EVENT-LOG"
                 :distributed-conformance-results "P9-SERVICE-ERROR"}
   :ai {:agent-manifest "P10-MODEL"
        :model-call-trace-schema "P10-MODEL"
        :prompt-provenance-record "P10-PROMPT"
        :tool-capability-manifest "P10-TOOL"
        :tool-schema-bundle "P10-TOOL"
        :memory-policy-record "P10-MEMORY"
        :policy-human-review-graph "P10-DESTRUCTIVE"
        :replay-log-schema "P10-REPLAY"
        :generated-code-safety-record "P10-GENERATED"
        :ai-conformance-results "P10-BUDGET"}})

(defn distributed-ai-diagnostic-id
  [data]
  (let [id (:id data)]
    (cond
      (contains? (set distributed-ai-diagnostic-ids) id) id
      :else (get distributed-ai-diagnostic-mapping
                 [(or (:active-profile data) (:profile data)) id]))))

(defn throw-distributed-ai-diagnostic!
  [ex]
  (let [data (ex-data ex)]
    (if-let [id (distributed-ai-diagnostic-id data)]
      (throw (diagnostic id
                         (or (:message data)
                             (str "distributed/AI profile diagnostic " id))
                         (merge (dissoc data :id :message)
                                {:underlying-diagnostic (:id data)
                                 :underlying-message (:message data)
                                 :active-profile (or (:active-profile data)
                                                     (:profile data))
                                 :target (:target data)
                                 :legal-alternative (:remediation data)
                                 :diagnostic-family :distributed-ai-profile-validation})))
      (throw ex))))

(defn require-distributed-ai-artifacts!
  [source-path profile document profile-validation]
  (let [required (distributed-ai-required-artifacts profile)
        missing (vec (remove #(profile-artifact-present? profile-validation %)
                             required))]
    (when-let [missing-artifact (first missing)]
      (fail! (get-in distributed-ai-artifact-diagnostic-by-key
                     [profile missing-artifact])
             "distributed/AI profile validation evidence is incomplete"
             {:source-span {:source source-path}
              :profile profile
              :document-id document
              :missing-artifact missing-artifact
              :required-artifacts required
              :remediation "Record the required distributed or AI profile-validation evidence in namespace metadata before lowering."}))
    required))