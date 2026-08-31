

(defn p10-task-id
  [document]
  (str "P10-D" (+ 144 (p10-document-number document))))

(defn p10-schema-source-overrides
  [module]
  (get-in module [:metadata :schema :interop] {}))

(defn p10-schema-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id (first %))
                      (vals (p10-schema-contracts document)))
            document))
        p10-schema-documents))

(defn p10-schema-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p10-schema-diagnostic-document id))]
    (fail! id
           "P10 schema interop validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase10-schema-interop
                   :stage :schema-interop
                   :document-id document
                   :task (when document (p10-task-id document))
                   :schema-id (or (:schema-id subject)
                                  (:schema-id p10-source-schema-contract))
                   :schema-version (or (:schema-version subject)
                                       (:schema-version p10-source-schema-contract))
                   :schema-hash (or (:schema-hash subject) p10-schema-hash)
                   :artifact-id (:artifact-id subject)
                   :boundary (:boundary subject)
                   :derivation-target (:derivation-target subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 10 requires one authoritative source schema to drive validators, serialization, canonical bytes, GraphQL, OpenAPI, database migrations, binary ABI, typed configuration, artifact schemas, and AI structured outputs without weakening schema identity, taint, effects, capabilities, source spans, or provenance."}
                  extra))))

(defn p10-schema-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p10-schema-override-diagnostics fail-kind)]
      (p10-schema-fail!
       id source-path
       {:artifact-id (str "p10-schema-" (name fail-kind))
        :document-id (p10-schema-diagnostic-document id)
        :missing-fact fail-kind
        :boundary :fixture}
       {:missing-fields [fail-kind]})
      (p10-schema-fail!
       "P10-MANIFEST" source-path
       {:artifact-id "p10-schema-unknown-override"
        :missing-fact fail-kind
        :boundary :fixture}
       {:missing-fields [:known-override-diagnostic]}))))

(defn p10-contract-diagnostics
  [document]
  (mapv (comp first val)
        (sort-by (comp name key) (p10-schema-contracts document))))

(defn p10-contract-evidence
  [document]
  (into {}
        (map (fn [[fact [diagnostic missing-fact]]]
               [fact {:diagnostic diagnostic
                      :missing-fact missing-fact
                      :source :governing-document
                      :status :present}])
             (sort-by (comp name key) (p10-schema-contracts document)))))

(defn p10-rejected-fixture-name
  [document]
  (let [diagnostic (p10-schema-rejected-diagnostics document)
        local-id (subs diagnostic (inc (count document)))]
    (str "schema-" (str/lower-case document) "-"
         (str/lower-case local-id) ".gravity")))

(defn p10-source-schema-ir
  [source-path input-id]
  (merge p10-source-schema-contract
         {:artifact :gravity/source-schema-ir
          :input-artifact input-id
          :schema-hash p10-schema-hash
          :source-spans [{:source source-path
                          :form-index 0
                          :schema-id (:schema-id p10-source-schema-contract)}]
          :static-type-projection {:type-name "TicketClassification"
                                   :schema-hash p10-schema-hash
                                   :preserves [:nullability :refinements
                                               :taint :validation-boundaries]}
          :semantic-diff {:from "TicketClassification/v1"
                          :to "TicketClassification/v2"
                          :policy :additive
                          :changes [:add-category-field
                                    :preserve-priority-enum
                                    :preserve-confidence-refinement]}
          :migration-requirements {:database :backfill-category
                                   :api :no-breaking-operation-change
                                   :binary :layout-compatible-with-policy}
          :manifest-status :complete}))

(defn p10-validator-artifact
  [source-schema]
  {:artifact :gravity/schema-validator
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :boundaries (:boundaries source-schema)
   :validation-states [:untrusted :validated-for-schema :validated-for-sink]
   :clears-taint-for #{:api-input :database-row :model-output
                       :configuration :artifact-manifest}
   :retains-taint-for #{:logs :prompts :unvalidated-sinks}
   :refinement-checks [{:field :confidence
                        :mode :runtime
                        :predicate :between-inclusive
                        :min 0.0
                        :max 1.0}]
   :diagnostics ["S1-TAINT" "S2-TAINT" "S5-TAINT" "S8-VALIDATION"]
   :status :complete})

(defn p10-serialization-fixture
  [source-schema]
  {:artifact :gravity/serializer
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :format :json
   :unknown-fields :reject
   :field-policy :required-fields-checked
   :numeric-policy :exact-or-diagnostic
   :string-policy :utf8-nfc
   :variant-policy :finite-enum
   :trust-boundary :external-data
   :decoded-trust :untrusted
   :canonical false
   :round-trip-vectors
   [{:name :ticket-classification-v2-json
     :value {:priority :high :category "billing" :confidence 0.98}
     :decoded-taint :untrusted
     :validation-result :validated-for-schema}]
   :status :complete})

(defn p10-canonical-format
  [source-schema]
  {:artifact :gravity/canonical-format
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :format-version 1
   :schema-hash-included true
   :map-order :lexicographic-encoded-key
   :set-order :lexicographic-encoded-value
   :numeric-policy :declared-or-reject
   :string-normalization :utf8-nfc
   :metadata-policy :schema-declared-only
   :reference-vectors [:ticket-classification-canonical-v2
                       :ticket-classification-replay-v2]
   :hash-input-record {:context :artifact-hash
                       :bytes :canonical-gravity-data
                       :schema-hash (:schema-hash source-schema)}
   :signing-input-record {:context :release-signature
                          :bytes :canonical-manifest-bytes}
   :status :complete})

(defn p10-graphql-generation
  [source-schema]
  {:artifact :gravity/graphql-generation
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :source-authority :gravity-source-schema
   :sdl ["type TicketClassification {"
         "  priority: TicketPriority!"
         "  category: String!"
         "  confidence: Float!"
         "}"]
   :operations {:classifyTicket {:effects #{:database/read}
                                 :capabilities #{:db/query}
                                 :auth :support-agent
                                 :error-model :gravity-result}}
   :nullability :gravity-option-result
   :resolver-adapters {:classifyTicket {:capability-check :runtime-enforced
                                        :source-map :schema-span}}
   :typed-client {:schema-hash (:schema-hash source-schema)
                  :operation-validation :required}
   :schema-diff {:compatibility :additive
                 :breaking-changes []}
   :status :complete})

(defn p10-openapi-generation
  [source-schema]
  {:artifact :gravity/openapi-generation
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :source-authority :gravity-route-and-schema-source
   :service :TicketService
   :routes {:create-ticket-classification
            {:method :post
             :path "/tickets/{ticketId}/classification"
             :request-schema "TicketClassification"
             :response-schema "TicketClassification"
             :error-schema "ErrorEnvelope"
             :taint-boundary :http-input
             :effects #{:database/write}
             :capabilities #{:db/query}
             :idempotency :required
             :source-span :route-source}}
   :request-validator :generated
   :response-validator :generated
   :typed-client {:operation-hash (c4-artifact-id [:openapi
                                                   (:schema-hash source-schema)])
                  :schema-hash (:schema-hash source-schema)}
   :contract-tests [:request-response-schema-match
                    :handler-effect-grant-match]
   :status :complete})