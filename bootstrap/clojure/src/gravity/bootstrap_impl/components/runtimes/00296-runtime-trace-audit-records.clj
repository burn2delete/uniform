

(defn runtime-trace-audit-records
  [source-path input-id]
  {:artifact :gravity/runtime-trace-audit-records
   :input-artifact input-id
   :records [{:event-id "trace/task-start"
              :runtime-family :concurrency
              :artifact-id input-id
              :source-span (source-span source-path 0)
              :effect :time/schedule
              :capability nil
              :redaction :none}
             {:event-id "trace/replay-http"
              :runtime-family :distributed
              :artifact-id input-id
              :source-span (source-span source-path 1)
              :effect :network/http
              :capability :http/client
              :redaction :headers-redacted}
             {:event-id "trace/capability-deny"
              :runtime-family :distributed
              :artifact-id input-id
              :source-span (source-span source-path 2)
              :effect :secrets/read
              :capability :secret/read
              :decision :deny
              :redaction :secret-name-only}]
   :required-audit-events-preserved? true
   :secret-leaks []
   :status :complete})