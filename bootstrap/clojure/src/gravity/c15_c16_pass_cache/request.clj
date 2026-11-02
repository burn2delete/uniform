(ns gravity.c15-c16-pass-cache.request
  "Pure C15/C16 generic-pass request construction for validated context.

  Callers own context validation; this helper only projects the request and
  rechecks its input artifact identity before deriving the cache key.")

(defn build
  [{:keys [require-sha256! stage-cache-key]}
   context stage-key contract input-id input-facts external-root?]
  (require-sha256! :input-artifact-id input-id)
  (let [request
        {:stage (:pass contract)
         :contract contract
         :producer-binding-id (get-in context [:producer-binding-ids stage-key])
         :input-artifact-ids [input-id]
         :input-facts input-facts
         :external-root-inputs
         (if external-root?
           {input-id {:kind (:input contract) :facts input-facts}}
           {})
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
