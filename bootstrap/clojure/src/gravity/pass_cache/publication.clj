(ns gravity.pass-cache.publication
  "Bounded immutable artifact, receipt, and entry publication."
  (:require [gravity.pass-cache.canonical-decode :refer :all]
            [gravity.pass-cache.canonical-encode :refer :all]
            [gravity.pass-cache.entry-schema :refer :all]
            [gravity.pass-cache.locking :refer :all]
            [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.receipt-validation :refer :all]
            [gravity.pass-cache.recovery :refer :all]
            [gravity.pass-cache.request-key :refer :all]
            [gravity.pass-cache.secure-io :refer :all]
            [gravity.pass-cache.store-directories :refer :all]
            [gravity.pass-execution :as pass-execution]))

(defn store-unlocked!
  [store key artifact producer-receipt validation-ops skip-artifact-validation?]
  (let [contract (:contract key)
        _ (receipt-output-compatible! key producer-receipt)
        _ (pass-execution/validate-execution-receipt!
           producer-receipt contract (receipt-validation-ops validation-ops))
        artifact (if skip-artifact-validation?
                   artifact
                   (validate-artifact! validation-ops artifact
                                       {:entry nil :key key}))
        artifact-id (artifact-id-of validation-ops artifact)
        artifact-bytes (encoded-value artifact maximum-blob-bytes)
        _ (when-not (= artifact-id (:output-artifact-id producer-receipt))
            (fail! "C16-STALE" "artifact identity differs from producer receipt"
                   {:artifact-id artifact-id
                    :receipt-artifact-id (:output-artifact-id producer-receipt)}))
        entry (cache-entry key artifact-id artifact-bytes producer-receipt
                           validation-ops)
        ;; Content-addressed blobs and receipts are immutable.  Admission and
        ;; all publication occur under the short global CAS gate; the producer
        ;; itself ran only under its per-key lock.
        publication
        (with-global-store-lock
          store
          (fn []
            ;; Recovery consumes one bounded iterator per newly opened secure
            ;; directory.  Close that set before publication admission opens a
            ;; fresh set, while retaining the global store lock across phases.
            (recover-orphans! store validation-ops)
            (let [statuses
                  (with-secure-store-directories
                   store
                   (fn [directories]
                     (let [receipt-bytes
                           (encoded-value producer-receipt maximum-entry-bytes)
                           entry-bytes
                           (encoded-value entry maximum-entry-bytes)
                           {:keys [blob-name receipt-name entry-name]}
                           (secure-admit-publication!
                            store directories key entry artifact-bytes
                            receipt-bytes entry-bytes)
                           blob-publication
                           (publish-create-or-verify!
                            store directories :blobs (:blobs directories)
                            blob-name artifact-bytes maximum-blob-bytes)
                           receipt-publication
                           (publish-create-or-verify!
                            store directories :receipts (:receipts directories)
                            receipt-name receipt-bytes maximum-entry-bytes)
                           entry-publication
                           (publish-create-or-verify!
                            store directories :entries (:entries directories)
                            entry-name entry-bytes maximum-entry-bytes)]
                       {:blob-publication blob-publication
                        :receipt-publication receipt-publication
                        :entry-publication entry-publication})))
                  ;; Verify the post-publication store under the same global
                  ;; lock, but with a newly opened secure-directory set.
                  post-publication-inventory (inventory! store)]
              (assoc statuses
                     :post-publication-inventory
                     post-publication-inventory))))]
        {:status :stored
         :key key
         :artifact artifact
         :producer-receipt producer-receipt
         :cache-entry entry
         :cache-evidence {:artifact :gravity/pass-cache-evidence
                          :schema-version schema-version
                          :status :stored
                          :cache-key-id (key-id key)
                          :artifact-id artifact-id
                          :producer-receipt-id (:receipt-id producer-receipt)
                          :reader-executed? true
                          :producer-executed? true
                          :artifact-reused? false
                          :cache-publication :published
                          :blob-publication (:blob-publication publication)
                          :receipt-publication
                          (:receipt-publication publication)
                          :entry-publication (:entry-publication publication)
                          :post-publication-inventory
                          (:post-publication-inventory publication)
                          :local? true :speculative? true
                          :authoritative? false :release? false :proof? false
                          :equivalence? false :self-hosting? false}}))

(defn store!
  "Validate and immutably publish an accepted artifact and producer receipt."
  [store key artifact producer-receipt validation-ops]
  (validate-store! store)
  (validate-key! key)
  (validate-cache-operations! validation-ops)
  (if *key-lock-held*
    (store-unlocked! store key artifact producer-receipt validation-ops false)
    (with-key-lock store (key-id key)
      #(store-unlocked! store key artifact producer-receipt validation-ops false))))

(defn execution-operations
  [operations]
  (let [required [:produce! :validate-output! :artifact-id-of
                  :verifier-reports :evidence-records]
        selected (select-keys operations required)]
    (when-not (every? #(fn? (get selected %)) required)
      (fail! "C16-ENTRY" "pass execution operations are incomplete"
             {:missing (vec (remove #(fn? (get selected %)) required))}))
    selected))
