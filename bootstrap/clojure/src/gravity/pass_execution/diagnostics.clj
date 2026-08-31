(ns gravity.pass-execution.diagnostics
  "Stable pass-execution diagnostics and dynamically scoped context.")

(def ^:dynamic *diagnostic-context* {})

(defn default-fail!
  [id message data]
  (throw (ex-info message
                  (merge {:id id
                          :stage :pass-execution
                          :pass nil
                          :artifact-id nil
                          :profile-id nil
                          :target-id nil
                          :remediation
                          "validate inputs against D1/C1/C16/C18 contracts"}
                         *diagnostic-context*
                         data))))

(def ^:dynamic *fail!* default-fail!)

(defn fail!
  [id message data]
  (*fail!* id message data))
