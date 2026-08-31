(ns gravity.self-hosting.a1-canonical-schema.execution
  "Total operation dispatch and terminal audit publication for A1."
  (:require [gravity.self-hosting.a1-canonical-schema.budget :as budget]
            [gravity.self-hosting.a1-canonical-schema.canonical :as canonical]
            [gravity.self-hosting.a1-canonical-schema.config :as config]
            [gravity.self-hosting.a1-canonical-schema.schema :as schema]
            [gravity.self-hosting.a1-canonical-schema.validation :as validation]))

(defn execute [operation args audit-sink construct-vector construct-map]
  (let [state (budget/budget)]
    (try
      (let [result
            (try
              (binding [canonical/*construct-vector* construct-vector
                        canonical/*construct-map* construct-map]
                (case operation
                  :copy
                  (do (when-not (= 1 (count args))
                        (config/fail! "E-TYPE" (config/path-of "arguments")))
                      (let [value (nth args 0)]
                        (budget/work! state 1 (config/path-of "arguments"))
                        (canonical/meter-value! state value nil 0 :input)
                        (config/accepted (canonical/finish-copy! state value))))

                  :registry
                  (do (when-not (= 1 (count args))
                        (config/fail! "E-TYPE" (config/path-of "arguments")))
                      (let [registry (nth args 0)]
                        (budget/work! state 1 (config/path-of "arguments"))
                        (canonical/meter-value! state registry nil 0 :input)
                        (when-not (config/canonical-map? registry)
                          (config/fail! "E-TYPE" nil))
                        (schema/check-registry! state registry)
                        (config/accepted (canonical/finish-copy! state registry))))

                  :validate
                  (do (when-not (= 3 (count args))
                        (config/fail! "E-TYPE" (config/path-of "arguments")))
                      (let [[registry schema-id value] args]
                        (budget/work! state 1 (config/path-of "arguments"))
                        (canonical/meter-value! state registry nil 0 :input)
                        (budget/work! state 1 (config/path-of "arguments"))
                        (schema/schema-id! schema-id (config/path-of "schema-id"))
                        (canonical/meter-value! state schema-id
                                                (config/path-of "schema-id")
                                                0 :input)
                        (budget/work! state 1 (config/path-of "arguments"))
                        (canonical/meter-value! state value nil 0 :input)
                        (when-not (config/canonical-map? registry)
                          (config/fail! "E-TYPE" nil))
                        (schema/check-registry! state registry)
                        (when-not (contains? registry schema-id)
                          (config/fail! "E-UNKNOWN-ID"
                                        (config/path-of "schema-id")))
                        (swap! state assoc :phase2-work
                               (get-in @state [:work :committed]))
                        (validation/validate-value! state registry schema-id
                                                    value nil 0)
                        (config/accepted
                          (canonical/finish-copy! state value))))))
              (catch clojure.lang.ExceptionInfo failure
                (let [data (ex-data failure)]
                  (if (:a1/failure data)
                    (budget/emit-rejection! state (:diagnostic data) (:path data))
                    (budget/emit-rejection! state "E-HOST"
                                            (config/path-of "internal")))))
              (catch InterruptedException _
                (.interrupt (Thread/currentThread))
                (budget/emit-rejection! state "E-HOST"
                                        (config/path-of "internal")))
              (catch Exception _
                (budget/emit-rejection! state "E-HOST"
                                        (config/path-of "internal"))))]
        (budget/finalize-terminal! state)
        result)
      (catch InterruptedException _
        (.interrupt (Thread/currentThread))
        (budget/emit-rejection! state "E-HOST" (config/path-of "internal")))
      (catch Exception _
        (budget/emit-rejection! state "E-HOST" (config/path-of "internal")))
      (finally
        (when (instance? clojure.lang.IAtom audit-sink)
          (reset! audit-sink @state))))))
