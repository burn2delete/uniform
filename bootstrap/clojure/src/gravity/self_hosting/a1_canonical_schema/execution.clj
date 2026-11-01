(ns gravity.self-hosting.a1-canonical-schema.execution
  "Total operation dispatch and host-error normalization for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.canonical :as canonical]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.schema :as schema]
            [gravity.self-hosting.a1-canonical-schema.validation :as validation]))

(defn execute [operation args construct-vector construct-map]
  (try
    (binding [canonical/*construct-vector* construct-vector
              canonical/*construct-map* construct-map]
      (case operation
        :copy
        (do (when-not (= 1 (count args))
              (config/fail! "E-TYPE" (config/path-of "arguments")))
            (let [value (nth args 0)]
              (canonical/validate-closed-value! value nil)
              (config/accepted (canonical/copy-value! value nil))))

        :registry
        (do (when-not (= 1 (count args))
              (config/fail! "E-TYPE" (config/path-of "arguments")))
            (let [registry (nth args 0)]
              (canonical/validate-closed-value! registry nil)
              (when-not (config/canonical-map? registry)
                (config/fail! "E-TYPE" nil))
              (schema/check-registry! registry)
              (config/accepted (canonical/copy-value! registry nil))))

        :validate
        (do (when-not (= 3 (count args))
              (config/fail! "E-TYPE" (config/path-of "arguments")))
            (let [[registry schema-id value] args]
              (canonical/validate-closed-value! registry nil)
              (schema/schema-id! schema-id (config/path-of "schema-id"))
              (canonical/validate-closed-value!
                schema-id (config/path-of "schema-id"))
              (canonical/validate-closed-value! value nil)
              (when-not (config/canonical-map? registry)
                (config/fail! "E-TYPE" nil))
              (schema/check-registry! registry)
              (when-not (contains? registry schema-id)
                (config/fail! "E-UNKNOWN-ID" (config/path-of "schema-id")))
              (validation/validate-value! registry schema-id value nil)
              (config/accepted (canonical/copy-value! value nil))))))
    (catch clojure.lang.ExceptionInfo failure
      (let [data (ex-data failure)]
        (if (:a1/failure data)
          (config/rejected (:diagnostic data)
                           (config/path-vector (:path data)))
          (config/rejected "E-HOST" ["internal"]))))
    (catch InterruptedException _
      (.interrupt (Thread/currentThread))
      (config/rejected "E-HOST" ["internal"]))
    (catch Exception _
      (config/rejected "E-HOST" ["internal"]))))
