

(defn p10-database-mapping
  [source-schema]
  {:artifact :gravity/database-migration
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :dialect :postgresql
   :table-mapping :ticket-classifications
   :column-types {:priority "ticket_priority_enum not null"
                  :category "text not null"
                  :confidence "double precision check 0.0 <= value <= 1.0"}
   :constraints [:confidence-range :priority-known]
   :indexes [:ticket-classifications-priority-idx]
   :migration-plan {:from "TicketClassification/v1"
                    :to "TicketClassification/v2"
                    :policy :additive
                    :transaction-mode :single-transaction
                    :deployment-ordering :before-api-compatibility-bump}
   :data-loss-report {:destructive false
                      :policy :no-data-loss}
   :rollback-or-forward-policy :rollback-supported
   :required-review-record :not-required-for-additive-migration
   :effects #{:database/write}
   :capabilities #{:db/migrate :db/query}
   :row-adapter {:name :TicketClassificationRowAdapter/v2
                 :weakens-source-schema? false}
   :fixture-validation :migration-plan-validation-passed
   :status :complete})

(defn p10-binary-abi-schema
  [source-schema]
  {:artifact :gravity/abi-schema
   :schema-id (:schema-id source-schema)
   :schema-version (:schema-version source-schema)
   :schema-hash (:schema-hash source-schema)
   :target-abi "x86_64-unknown-linux-gnu"
   :calling-convention :c
   :endian :little
   :alignment 8
   :field-order [:priority :category-offset :category-length :confidence]
   :widths {:priority :u8
            :category-offset :u32
            :category-length :u32
            :confidence :f64}
   :padding :zeroed-explicit-padding
   :pointer-policy :no-raw-pointers-in-stable-record
   :ownership-lifetime-map {:category-bytes :borrowed-for-decode-owned-after-validation}
   :variant-discriminants {:low 0 :medium 1 :high 2}
   :reference-vectors [:ticket-classification-abi-x86-64-v2
                       :ticket-classification-abi-wasm32-v2]
   :ffi-binding-input {:name :TicketClassificationAbi/v2
                       :schema-hash (:schema-hash source-schema)}
   :status :complete})