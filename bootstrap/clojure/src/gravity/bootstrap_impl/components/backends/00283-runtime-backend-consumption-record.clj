

(defn runtime-backend-consumption-record
  [input-id]
  {:artifact :gravity/runtime-backend-consumption-record
   :input-artifact input-id
   :consumers
   [{:consumer :backend-artifact-emission
     :requires [:runtime-manifest :service-table
                :forbidden-service-report]
     :status :complete}
    {:consumer :package-manifest
     :requires [:capability-summary :runtime-handles
                :denied-authority]
     :status :complete}
    {:consumer :conformance-suite
     :requires [:accepted-no-runtime-fixture
                :rejected-runtime-diagnostics]
     :status :complete}
    {:consumer :self-hosting-check
     :requires [:runtime-family-selection-record
                :upstream-artifact-identity]
     :status :complete}]
   :status :complete})