

(defn observability-event-schema-registry
  [input-id]
  {:artifact :gravity/event-schema-registry
   :input-artifact input-id
   :schemas [{:event-kind :capability
              :schema-id "schema/runtime-capability-event-v1"
              :stable-id? true}
             {:event-kind :replay
              :schema-id "schema/runtime-replay-event-v1"
              :stable-id? true}
             {:event-kind :ffi
              :schema-id "schema/runtime-ffi-event-v1"
              :stable-id? true}
             {:event-kind :model
              :schema-id "schema/runtime-model-event-v1"
              :stable-id? true}]
   :missing-schemas []
   :status :complete})