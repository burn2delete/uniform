(ns gravity.pass-cache.receipt-validation
  "Cache operation, artifact, and historical receipt validation."
  (:require [clojure.set :as set]
            [gravity.digest :as digest]
            [gravity.pass-cache.canonical-encode :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.request-key :refer :all]
            [gravity.pass-execution :as pass-execution]))

(def cache-operation-fields
  #{:artifact-id-of :validate-artifact! :validate-output!
    :validation-binding-id :validate-diagnostic-stream!
    :validate-verifier-report! :validate-evidence-record!
    :produce! :verifier-reports :evidence-records})

(defn validate-cache-operations!
  [operations]
  (when-not (map? operations)
    (fail! "C16-ENTRY" "cache validation operations must be a map" {}))
  (let [unknown (set/difference (set (keys operations))
                                cache-operation-fields)]
    (when (seq unknown)
      (fail! "C16-ENTRY" "cache validation operations contain unknown fields"
             {:unknown-fields (vec (sort unknown))})))
  (require-sha256! :validation-binding-id (:validation-binding-id operations))
  (when-not (fn? (:artifact-id-of operations))
    (fail! "C16-ENTRY" "cache artifact identity operation is required" {}))
  (let [artifact-validator? (fn? (:validate-artifact! operations))
        output-validator? (fn? (:validate-output! operations))]
    (when (= artifact-validator? output-validator?)
      (fail! "C16-ENTRY"
             "exactly one cache artifact validation operation is required"
             {:validate-artifact!? artifact-validator?
              :validate-output!? output-validator?})))
  (doseq [field [:validate-diagnostic-stream! :validate-verifier-report!
                 :validate-evidence-record!]]
    (when-not (fn? (get operations field))
      (fail! "C16-ENTRY" "cache receipt validator operation is required"
             {:field field})))
  operations)

(defn receipt-validation-ops
  [operations]
  {:validate-diagnostic-stream!
   (:validate-diagnostic-stream! operations)
   :validate-verifier-report!
   (:validate-verifier-report! operations)
   :validate-evidence-record!
   (:validate-evidence-record! operations)})

(defn artifact-id-of
  [operations artifact]
  (let [candidate (:artifact-id-of operations)
        id (cond
             (fn? candidate) (candidate artifact)
             (keyword? candidate) (get artifact candidate)
             (sha256-id? (:artifact-id artifact)) (:artifact-id artifact)
             :else (content-id :gravity/pass-cache-artifact-v2 artifact))]
    (require-sha256! :artifact-id id)))

(defn validate-artifact!
  [operations artifact context]
  (cond
    (fn? (:validate-artifact! operations))
    ((:validate-artifact! operations) artifact (:entry context) (:key context))

    (fn? (:validate-output! operations))
    ((:validate-output! operations) artifact (:key context)
     (get-in context [:key :contract]))

    :else artifact))

(defn receipt-output-compatible!
  [key receipt]
  (let [same-fields
        [[:stage :stage]
         [:pass-contract-id :pass-contract-id]
         [:producer-binding-id :producer-binding-id]
         [:input-artifact-ids :input-artifact-ids]
         [:input-facts :input-facts]
         [:external-root-inputs :external-root-inputs]
         [:semantic-bindings :semantic-bindings]
         [:dependency-graph-id :dependency-graph-id]
         [:build-effect-replay-id :build-effect-replay-id]
         [:profile-id :profile-id]
         [:target-id :target-id]
         [:policy-ids :policy-ids]
         [:diagnostic-stream-id :diagnostic-stream-id]]]
    (doseq [[key-field receipt-field] same-fields]
      (when-not (= (get key key-field) (get receipt receipt-field))
        (fail! "C16-STALE" "producer receipt does not bind the cache key"
               {:field key-field})))
    (when-not (= (get-in key [:provenance :provenance-id])
                 (get-in receipt [:provenance :provenance-id]))
      (fail! "C16-STALE" "producer receipt provenance differs from cache key" {}))
    (when-not (= (get-in key [:authority :scope])
                 (get-in receipt [:authority :scope]))
      (fail! "C16-STALE" "producer receipt authority scope differs from cache key"
             {}))
    receipt))

(defn validate-receipt!
  "Validate the key-bound historical producer receipt before touching its blob."
  [key receipt validation-ops]
  ;; A cache key is the semantic projection of an executed request and does
  ;; not carry the execution-mode field itself.  Reconstruct that fixed mode
  ;; before applying the exact pass-execution request validator so hits cannot
  ;; bypass its full request invariants.
  (validate-stage-request!
   (assoc (select-keys key execution-request-fields)
          :execution-mode :executed))
  (receipt-output-compatible! key receipt)
  (pass-execution/validate-execution-receipt!
   receipt (:contract key) (receipt-validation-ops validation-ops))
  receipt)

(defn validate-artifact-against-receipt!
  [key artifact receipt validation-ops context]
  (let [artifact (validate-artifact! validation-ops artifact context)
        id (artifact-id-of validation-ops artifact)]
    (when-not (= id (:output-artifact-id receipt))
      (fail! "C16-STALE" "artifact identity differs from producer receipt"
             {:observed id :expected (:output-artifact-id receipt)}))
    artifact))

(defn entry-id
  [entry]
  (content-id :gravity/pass-cache-entry-v2 (dissoc entry :entry-id)))

(defn blob-id
  [bytes]
  (str "sha256:" (digest/sha256-bytes-hex bytes)))

(defn receipt-id
  [receipt]
  (:receipt-id receipt))

(defn cache-entry
  [key artifact-id artifact-bytes producer-receipt validation-ops]
  (let [base {:artifact :gravity/compiler-pass-cache-entry
              :schema-version schema-version
              :cache-key-id (key-id key)
              :semantic-key-id (:semantic-key-id key)
              :stage (:stage key)
              :pass-contract-id (:pass-contract-id key)
              :artifact-id artifact-id
              :blob-id (blob-id artifact-bytes)
              :producer-receipt-id (receipt-id producer-receipt)
              :contract (:contract key)
              :facts {:input (:input-facts producer-receipt)
                      :output (:output-facts producer-receipt)
                      :requires (:requires producer-receipt)
                      :preserves (:preserves producer-receipt)
                      :invalidates (:invalidates producer-receipt)
                      :regenerates (:regenerates producer-receipt)}
              :evidence {:verifier-ids (vec (sort (map :verifier-id
                                                        (:verifier-reports
                                                         producer-receipt))))
                         :evidence-ids (vec (sort (map :evidence-id
                                                        (:evidence-records
                                                         producer-receipt))))}
              :provenance-id (get-in key [:provenance :provenance-id])
              :validation-binding-id (:validation-binding-id validation-ops)
              :diagnostic-schema-id (:diagnostic-schema-id key)
              :diagnostic-stream-id (:diagnostic-stream-id key)
              :authority {:local? true :speculative? true
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}}]
    (assoc base :entry-id (entry-id base))))
