

(defn distributed-capability-enforcement-table
  [input-id]
  {:artifact :gravity/distributed-capability-enforcement-table
   :input-artifact input-id
   :deny-by-default? true
   :decisions [{:action-id "action/http"
                :principal :workflow/order
                :effect :network/http
                :capability :http/client
                :provider :http-provider
                :decision :grant
                :audit :recorded
                :redaction :headers-redacted}
               {:action-id "action/db-write"
                :principal :workflow/order
                :effect :database/write
                :capability :db/write
                :provider :db-provider
                :decision :grant
                :audit :recorded
                :redaction :row-values-redacted}
               {:action-id "action/secret"
                :principal :workflow/order
                :effect :secrets/read
                :capability :secret/read
                :provider :secret-store
                :decision :deny
                :audit :recorded
                :redaction :secret-name-only}]
   :ambient-authority-denied? true
   :status :complete})

(defn distributed-migration-policy
  [input-id]
  {:artifact :gravity/schema-event-log-migration-policy
   :input-artifact input-id
   :schema-upgrades [{:from "schema/order-state-v1"
                      :to "schema/order-state-v2"
                      :compatibility :backward-compatible
                      :replay-policy :compatible}]
   :event-log-upgrades [{:from "event-log/orders-v1"
                         :to "event-log/orders-v2"
                         :migration :append-only-compatible
                         :replay-policy :compatible}]
   :unsafe-upgrades []
   :status :complete})