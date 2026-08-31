(ns gravity.pass-cache.entry-schema
  "Immutable entry schema, admission, and reuse receipts."
  (:require [gravity.pass-cache.canonical-encode :refer :all]
            [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.receipt-validation :refer :all]
            [gravity.pass-cache.request-key :refer :all]
            [gravity.pass-cache.secure-io :refer :all]
            [gravity.pass-cache.store-directories :refer :all]))

(def cache-entry-fields
  #{:artifact :schema-version :cache-key-id :semantic-key-id :stage
    :pass-contract-id :artifact-id :blob-id :producer-receipt-id :contract
    :facts :evidence :provenance-id :validation-binding-id
    :diagnostic-schema-id :diagnostic-stream-id :authority :entry-id})

(defn validate-entry-nested-schema!
  [entry]
  (let [facts (:facts entry)
        evidence (:evidence entry)
        authority (:authority entry)]
    (when-not (and (map? facts)
                   (= #{:input :output :requires :preserves :invalidates
                        :regenerates}
                      (set (keys facts)))
                   (every? #(and (set? %) (every? keyword? %)) (vals facts)))
      (fail! "C16-ENTRY" "cache entry facts schema is malformed" {}))
    (when-not (and (map? evidence)
                   (= #{:verifier-ids :evidence-ids} (set (keys evidence)))
                   (vector? (:verifier-ids evidence))
                   (vector? (:evidence-ids evidence)))
      (fail! "C16-ENTRY" "cache entry evidence schema is malformed" {}))
    (doseq [field [:verifier-ids :evidence-ids]]
      (sorted-sha-vector! field (get evidence field)))
    (when-not (and (map? authority)
                   (= #{:local? :speculative? :authoritative? :release?
                        :proof? :equivalence? :self-hosting?}
                      (set (keys authority)))
                   (every? boolean? (vals authority))
                   (true? (:local? authority))
                   (true? (:speculative? authority))
                   (false? (:authoritative? authority))
                   (false? (:release? authority))
                   (false? (:proof? authority))
                   (false? (:equivalence? authority))
                   (false? (:self-hosting? authority)))
      (fail! "C16-ENTRY" "cache entry authority schema is malformed" {})))
  entry)

(defn validate-retained-entry-commit!
  [filename entry]
  (when-not (and (map? entry)
                 (= cache-entry-fields (set (keys entry))))
    (fail! "C16-ENTRY" "cache entry has unknown or missing fields" {}))
  (doseq [field [:cache-key-id :semantic-key-id :pass-contract-id
                 :artifact-id :blob-id :producer-receipt-id :provenance-id
                 :validation-binding-id :diagnostic-schema-id
                 :diagnostic-stream-id :entry-id]]
    (require-sha256! field (get entry field)))
  (when-not (and (= :gravity/compiler-pass-cache-entry (:artifact entry))
                 (= schema-version (:schema-version entry))
                 (= (:entry-id entry) (entry-id entry))
                 (= filename (str (:cache-key-id entry) ".edn")))
    (fail! "C16-STALE"
           "retained cache entry is not an intact immutable commit"
           {:filename filename}))
  (validate-entry-nested-schema! entry)
  entry)

(defn secure-admit-publication!
  [store directories key entry artifact-bytes receipt-bytes entry-bytes]
  (let [inventory (secure-store-inventory! store directories)
        blob-name (sha-file-name! :blob-id (:blob-id entry) ".edn")
        receipt-name (sha-file-name! :producer-receipt-id
                                     (:producer-receipt-id entry) ".edn")
        entry-name (sha-file-name! :cache-key-id (key-id key) ".edn")
        new-blob? (not (secure-child-exists? (:blobs directories) blob-name))
        new-receipt?
        (not (secure-child-exists? (:receipts directories) receipt-name))
        new-entry? (not (secure-child-exists? (:entries directories) entry-name))
        next-bytes (+ (reduce + 0 (map :bytes (vals inventory)))
                      (if new-blob? (alength artifact-bytes) 0)
                      (if new-receipt? (alength receipt-bytes) 0)
                      (if new-entry? (alength entry-bytes) 0))]
    (when (or (and new-blob?
                   (>= (get-in inventory [:blobs :count]) maximum-blob-count))
              (and new-receipt?
                   (>= (get-in inventory [:receipts :count])
                       maximum-receipt-count))
              (and new-entry?
                   (>= (get-in inventory [:entries :count]) maximum-entry-count))
              (> next-bytes maximum-store-bytes))
      (fail! "C16-POLICY" "cache publication exceeds store admission policy"
             {:maximum-bytes maximum-store-bytes
              :observed-bytes next-bytes}))
    {:blob-name blob-name :receipt-name receipt-name :entry-name entry-name
     :inventory inventory}))

(defn rejected-result
  [key diagnostic extra]
  {:status :rejected
   :key key
   :cache-evidence
   (merge {:artifact :gravity/pass-cache-evidence
           :schema-version schema-version
           :status :rejected
           :cache-key-id (key-id key)
           :contained-diagnostic diagnostic
           :local? true
           :speculative? true
           :authoritative? false
           :release? false
           :proof? false
           :equivalence? false
           :self-hosting? false}
          extra)})

(defn validate-entry-record!
  [key entry]
  (validate-retained-entry-commit! (str (key-id key) ".edn") entry)
  (when-not (and
                 (= (key-id key) (:cache-key-id entry))
                 (= (:semantic-key-id key) (:semantic-key-id entry)))
    (fail! "C16-STALE" "cache entry identity or schema is stale" {}))
  ;; The key is bound through its id and all receipt fields; explicit contract
  ;; and stage identities catch key substitution before blob interpretation.
  (when-not (and (= (:stage key) (:stage entry))
                 (= (:pass-contract-id key) (:pass-contract-id entry))
                 (= (:contract key) (:contract entry)))
    (fail! "C16-STALE" "cache entry contract differs from key" {}))
  entry)

(defn validate-entry-derived!
  [key entry receipt artifact-id validation-ops]
  (let [expected-facts {:input (:input-facts receipt)
                        :output (:output-facts receipt)
                        :requires (:requires receipt)
                        :preserves (:preserves receipt)
                        :invalidates (:invalidates receipt)
                        :regenerates (:regenerates receipt)}
        expected-evidence
        {:verifier-ids (vec (sort (map :verifier-id
                                       (:verifier-reports receipt))))
         :evidence-ids (vec (sort (map :evidence-id
                                       (:evidence-records receipt))))}]
    (when-not (and (= (:output-artifact-id receipt) artifact-id)
                   (= (:artifact-id entry) artifact-id)
                   (= (:producer-receipt-id entry) (:receipt-id receipt))
                   (= (:cache-key-id entry) (key-id key))
                   (= (:semantic-key-id entry) (:semantic-key-id key))
                   (= (:provenance-id entry)
                      (get-in key [:provenance :provenance-id]))
                   (= (:validation-binding-id entry)
                      (:validation-binding-id validation-ops))
                   (= (:diagnostic-schema-id entry)
                      (:diagnostic-schema-id key))
                   (= (:diagnostic-stream-id entry)
                      (:diagnostic-stream-id key))
                   (= (:facts entry) expected-facts)
                   (= (:evidence entry) expected-evidence))
      (fail! "C16-STALE"
             "cache entry derived facts, evidence, or bindings are stale" {})))
  entry)

(defn build-reuse-receipt
  [key entry artifact producer-receipt validation-ops]
  (let [current-authority (:authority key)
        current-levels (vec (vals (:input-authorities current-authority)))
        ceiling (get-in key [:contract :authority-ceiling])
        historical-effective-level (get-in producer-receipt
                                           [:authority :effective-level])
        effective-level
        (weakest-authority
         (conj current-levels ceiling (:claimed-level current-authority)
               historical-effective-level))
        current-authority-id
        (content-id :gravity/pass-cache-current-authority-v2
                    current-authority)
        validation-id
        (content-id :gravity/pass-cache-validation-v2
                    {:cache-key-id (key-id key)
                     :artifact-id (:artifact-id entry)
                     :producer-receipt-id (:producer-receipt-id entry)
                     :diagnostic-schema-id (:diagnostic-schema-id key)
                     :diagnostic-stream-id (:diagnostic-stream-id key)
                     :validation-binding-id (:validation-binding-id validation-ops)
                     :current-authority-id current-authority-id
                     :current-authority current-authority
                     :verifier-ids (get-in entry [:evidence :verifier-ids])
                     :evidence-ids (get-in entry [:evidence :evidence-ids])
                     :provenance-id (:provenance-id entry)})
        base {:artifact :gravity/pass-cache-reuse-receipt
              :schema-version schema-version
              :stage (:stage key)
              :reuse-receipt-id nil
              :cache-key-id (key-id key)
              :semantic-key-id (:semantic-key-id key)
              :pass-contract-id (:pass-contract-id key)
              :contract (:contract key)
              :historical-producer-receipt-id (:receipt-id producer-receipt)
              :current-key-id (key-id key)
              :current-artifact-id (:artifact-id entry)
              :artifact-id (:artifact-id entry)
              :blob-id (:blob-id entry)
              :current-validation-id validation-id
              :validation-binding-id (:validation-binding-id validation-ops)
              :current-authority current-authority
              :current-authority-id current-authority-id
              :validation-ids {:current-artifact-validation-id validation-id
                               :current-validation-binding-id
                               (:validation-binding-id validation-ops)
                               :current-authority-id current-authority-id
                               :diagnostic-stream-id (:diagnostic-stream-id key)
                               :diagnostic-schema-id (:diagnostic-schema-id key)
                               :revalidated-historical-verifier-ids
                               (get-in entry [:evidence :verifier-ids])
                               :revalidated-historical-evidence-ids
                               (get-in entry [:evidence :evidence-ids])}
              :facts (:facts entry)
              :evidence (:evidence entry)
              :provenance-id (:provenance-id entry)
              :execution-mode :cache-reuse
              :authority {:local? true :speculative? true
                          :effective-level effective-level
                          :current-effective-level effective-level
                          :historical-effective-level historical-effective-level
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}
              :claims {:release? false :proof? false :self-hosting? false
                       :equivalence? false :authoritative? false}}]
    (assoc base :reuse-receipt-id
           (content-id :gravity/pass-cache-reuse-receipt-v2
                       (dissoc base :reuse-receipt-id)))))
