(ns gravity.pass-cache.request-key
  "Exact pass request validation and semantic cache-key projection."
  (:require [clojure.set :as set]
            [gravity.pass-cache.canonical-encode :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-execution :as pass-execution]))

(defn pass-request-fields
  [request]
  (when-not (map? request)
    (fail! "C16-KEY" "pass cache request must be a map" {}))
  request)

(defn validate-stage-request!
  [request]
  (let [request (pass-request-fields request)
        contract (:contract request)]
    (when-not (= execution-request-fields (set (keys request)))
      (fail! "C16-KEY" "pass execution request has unknown or missing fields"
             {:expected execution-request-fields
              :observed (set (keys request))}))
    (pass-execution/validate-pass-contract! contract)
    (when-not (= (:stage request) (:pass contract))
      (fail! "D1-PIPELINE-ORDER" "request stage differs from pass contract"
             {:stage (:stage request) :pass (:pass contract)}))
    (require-sha256! :producer-binding-id (:producer-binding-id request))
    (sorted-sha-vector! :input-artifact-ids (:input-artifact-ids request))
    (when (empty? (:input-artifact-ids request))
      (fail! "D1-ARTIFACT-GAP" "pass cache requires input artifact ids" {}))
    (require-keyword-set! :input-facts (:input-facts request))
    (when-not (set/subset? (:requires contract) (:input-facts request))
      (fail! "C1-PASS-CONTRACT" "pass input lacks required facts"
             {:missing (vec (sort (set/difference (:requires contract)
                                                  (:input-facts request))))}))
    (when-not (set/subset? (:preserves contract) (:input-facts request))
      (fail! "C1-EVIDENCE-DROP" "pass preserves absent input facts"
             {:missing (vec (sort (set/difference (:preserves contract)
                                                  (:input-facts request))))}))
    (when-not (map? (:external-root-inputs request))
      (fail! "C16-KEY" "external-root inputs must be a map" {}))
    (doseq [[artifact-id descriptor] (:external-root-inputs request)]
      (require-sha256! :external-root-artifact-id artifact-id)
      (when-not (and (map? descriptor)
                     (= #{:kind :facts} (set (keys descriptor)))
                     (= (:input contract) (:kind descriptor))
                     (set? (:facts descriptor)))
        (fail! "C16-KEY" "external-root input descriptor is malformed"
               {:artifact-id artifact-id}))
      (require-keyword-set! :external-root-facts (:facts descriptor))
      (when-not (set/subset? (:facts descriptor) (:input-facts request))
        (fail! "C1-EVIDENCE-DROP" "external-root facts are absent from input facts"
               {:artifact-id artifact-id})))
    (when-not (set/subset? (set (keys (:external-root-inputs request)))
                           (set (:input-artifact-ids request)))
      (fail! "C16-KEY" "external roots must be declared input ids" {}))
    (let [bindings (:semantic-bindings request)]
      (when-not (and (map? bindings)
                     (= #{:compiler-id :capability-policy-id :facet-set-id
                          :provider-manifest-id :package-lock-id
                          :diagnostic-schema-id}
                        (set (keys bindings))))
        (fail! "C16-KEY" "semantic bindings are incomplete" {}))
      (doseq [[field value] bindings]
        (require-sha256! field value)))
    (doseq [field [:dependency-graph-id :build-effect-replay-id :profile-id
                   :target-id :diagnostic-stream-id]]
      (require-sha256! field (get request field)))
    (sorted-sha-vector! :policy-ids (:policy-ids request))
    (let [provenance (:provenance request)]
      (when-not (and (map? provenance)
                     (or (= #{:provenance-id :source-path :metadata}
                            (set (keys provenance)))
                         (= #{:provenance-id} (set (keys provenance))))
                     (sha256-id? (:provenance-id provenance))
                     (or (= #{:provenance-id} (set (keys provenance)))
                         (and (or (nil? (:source-path provenance))
                                  (string? (:source-path provenance)))
                              (map? (:metadata provenance)))))
        (fail! "C16-KEY" "provenance binding is malformed" {})))
    (when-not (= :executed (:execution-mode request))
      (fail! "C16-KEY" "pass cache keys require executed requests" {}))
    (validate-authority! (:authority request) (:input-artifact-ids request)
                         (:authority-ceiling contract))
    request))

(defn key-preimage
  [request]
  (let [request (validate-stage-request! request)
        contract (:contract request)
        contract-id (pass-execution/pass-contract-id contract)]
    {:artifact :gravity/compiler-pass-cache-key
     :schema-version schema-version
     :canonicalizer-version canonicalizer-version
     :stage (:stage request)
     :pass-contract-id contract-id
     :contract contract
     :producer-binding-id (:producer-binding-id request)
     :input-artifact-ids (:input-artifact-ids request)
     :input-facts (:input-facts request)
     :external-root-inputs (:external-root-inputs request)
     :semantic-bindings (:semantic-bindings request)
     :dependency-graph-id (:dependency-graph-id request)
     :build-effect-replay-id (:build-effect-replay-id request)
     :profile-id (:profile-id request)
     :target-id (:target-id request)
     :policy-ids (:policy-ids request)
     :diagnostic-schema-id
     (get-in request [:semantic-bindings :diagnostic-schema-id])
     :diagnostic-stream-id (:diagnostic-stream-id request)
     :provenance (:provenance request)
     :authority (:authority request)}))

(defn request-from-key
  [key]
  ;; Execution mode is fixed by this cache contract and intentionally omitted
  ;; from the semantic key projection.  Reinsert it before recomputation.
  (assoc (select-keys key execution-request-fields)
         :execution-mode :executed))

(defn stage-cache-key
  "Build a bounded semantic key over the full pass execution contract."
  [request]
  (let [preimage (key-preimage request)
        semantic-preimage
        (-> preimage
            (dissoc :authority :provenance)
            ;; The producer observes its authority scope, so cross-scope reuse
            ;; would not be semantically sound.  Authority levels remain
            ;; nonsemantic and are monotonically capped on every reuse.
            (assoc :authority-scope (get-in request [:authority :scope]))
            (assoc :provenance-id
                   (get-in request [:provenance :provenance-id])))
        key-id (content-id :gravity/compiler-pass-cache-key-v2
                           semantic-preimage)]
    (assoc preimage
           :semantic-key-id key-id
           :cache-key-id key-id
           :storage-key-id key-id)))

(defn key-id
  [key]
  (let [id (or (:semantic-key-id key) (:cache-key-id key) (:storage-key-id key))]
    (require-sha256! :semantic-key-id id)
    id))

(defn validate-key!
  [key]
  (when-not (map? key)
    (fail! "C16-KEY" "cache key must be a map" {}))
  (let [expected (stage-cache-key (request-from-key key))]
    (when-not (= (:semantic-key-id key) (:semantic-key-id expected))
      (fail! "C16-STALE" "cache key identity does not recompute"
             {:observed (:semantic-key-id key)
              :expected (:semantic-key-id expected)}))
    (when-not (= key expected)
      (fail! "C16-STALE" "cache key contains stale derived identity" {})))
  key)
