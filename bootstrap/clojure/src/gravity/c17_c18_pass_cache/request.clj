(ns gravity.c17-c18-pass-cache.request
  "Pure C17/C18 generic-pass request construction over injected validation.")

(defn build
  [{:keys [validate-context! require-sha256! stage-cache-key]}
   context stage-key contract input-id input-facts]
  (validate-context! context)
  (require-sha256! :input-artifact-id input-id)
  (let [request {:stage (:pass contract)
                 :contract contract
                 :producer-binding-id (get-in context [:producer-binding-ids stage-key])
                 :input-artifact-ids [input-id]
                 :input-facts input-facts
                 :external-root-inputs {}
                 :semantic-bindings (:semantic-bindings context)
                 :dependency-graph-id (:dependency-graph-id context)
                 :build-effect-replay-id (:build-effect-replay-id context)
                 :profile-id (:profile-id context)
                 :target-id (:target-id context)
                 :policy-ids (:policy-ids context)
                 :provenance (:provenance context)
                 :diagnostic-stream-id (get-in context [:diagnostic-stream-ids stage-key])
                 :execution-mode :executed
                 :authority {:input-authorities {input-id :none}
                             :claimed-level :none
                             :scope (:authority-scope context)}}]
    (stage-cache-key request)
    request))
