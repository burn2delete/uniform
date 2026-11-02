(ns gravity.pass-cache.lookup
  "Receipt-first immutable cache lookup and current revalidation."
  (:require [gravity.pass-cache.canonical-decode
             :refer [decode-canonical-bytes]]
            [gravity.pass-cache.entry-schema
             :refer [build-reuse-receipt rejected-result
                     validate-entry-derived! validate-entry-record!]]
            [gravity.pass-cache.locking
             :refer [with-global-store-lock with-key-lock]]
            [gravity.pass-cache.path-policy
             :refer [sha-file-name! validate-store!]]
            [gravity.pass-cache.policy
             :refer [*key-lock-held* fail! fatal? maximum-blob-bytes
                     maximum-entry-bytes schema-version]]
            [gravity.pass-cache.receipt-validation
             :as receipt-validation
             :refer [artifact-id-of blob-id validate-artifact-against-receipt!
                     validate-cache-operations! validate-receipt!]]
            [gravity.pass-cache.recovery :refer [recover-orphans!]]
            [gravity.pass-cache.request-key :refer [key-id validate-key!]]
            [gravity.pass-cache.secure-io
             :refer [secure-child-exists? secure-read-bytes!]]
            [gravity.pass-cache.store-directories
             :refer [with-secure-store-directories]])
  (:import [java.nio.file SecureDirectoryStream]))

(def ^:private default-envelope-artifact-id
  ;; Capture the known pure operation once. A later Var redefinition or
  ;; wrapper must remain on the ordinary callback path.
  receipt-validation/envelope-artifact-id)

(defn lookup-unlocked!
  [store key validation-ops]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [entry-name (sha-file-name! :cache-key-id (key-id key) ".edn")
           ^SecureDirectoryStream entry-directory (:entries directories)]
       (if-not (secure-child-exists? entry-directory entry-name)
         {:status :miss
          :key key
          :cache-evidence {:artifact :gravity/pass-cache-evidence
                           :schema-version schema-version
                           :status :miss
                           :cache-key-id (key-id key)
                           :local? true :speculative? true
                           :authoritative? false :release? false :proof? false
                           :equivalence? false :self-hosting? false}}
         (try
           (let [entry-bytes (secure-read-bytes!
                              store :entries entry-directory entry-name
                              maximum-entry-bytes)
                 entry (validate-entry-record!
                        key (decode-canonical-bytes entry-bytes
                                                    maximum-entry-bytes))
                 receipt-name (sha-file-name!
                               :producer-receipt-id
                               (:producer-receipt-id entry) ".edn")
                 ^SecureDirectoryStream receipt-directory (:receipts directories)
                 _ (when-not (secure-child-exists?
                              receipt-directory receipt-name)
                     (fail! "C16-STALE" "producer receipt is missing" {}))
                 receipt-bytes (secure-read-bytes!
                                store :receipts receipt-directory receipt-name
                                maximum-entry-bytes)
                 receipt (decode-canonical-bytes receipt-bytes
                                                  maximum-entry-bytes)
                 _ (when-not (= (:producer-receipt-id entry)
                                (:receipt-id receipt))
                     (fail! "C16-STALE" "producer receipt identity is stale" {}))
                 ;; Receipt-first ordering prevents an untrusted blob from
                 ;; being interpreted before its producer attestation binds to
                 ;; this key and survives all current validators.
                 _ (validate-receipt! key receipt validation-ops)
                 blob-name (sha-file-name! :blob-id (:blob-id entry) ".edn")
                 ^SecureDirectoryStream blob-directory (:blobs directories)
                 _ (when-not (secure-child-exists? blob-directory blob-name)
                     (fail! "C16-STALE" "artifact blob is missing" {}))
                 artifact-bytes (secure-read-bytes!
                                store :blobs blob-directory blob-name
                                maximum-blob-bytes)
                 _ (when-not (= (:blob-id entry) (blob-id artifact-bytes))
                     (fail! "C16-STALE" "artifact blob identity is stale" {}))
                 artifact (decode-canonical-bytes artifact-bytes
                                                   maximum-blob-bytes)
                 {:keys [artifact artifact-id]}
                 (validate-artifact-against-receipt!
                  key artifact receipt validation-ops
                  {:entry entry :key key :retain-artifact-id? true})
                 artifact-id (if (identical? (:artifact-id-of validation-ops)
                                             default-envelope-artifact-id)
                               artifact-id
                               (artifact-id-of validation-ops artifact))
                 _ (validate-entry-derived!
                    key entry receipt artifact-id validation-ops)
                 reuse (build-reuse-receipt key entry artifact receipt
                                            validation-ops)]
             {:status :hit :key key :artifact artifact
              :producer-receipt receipt :cache-entry entry
              :reuse-receipt reuse
              :cache-evidence {:artifact :gravity/pass-cache-evidence
                               :schema-version schema-version :status :hit
                               :cache-key-id (key-id key)
                               :artifact-id (:artifact-id entry)
                               :producer-receipt-id (:producer-receipt-id entry)
                               :artifact-reused? true
                               :producer-executed? false
                               :reader-executed? false
                               :reuse-receipt-id (:reuse-receipt-id reuse)
                               :local? true :speculative? true
                               :authoritative? false :release? false
                               :proof? false :equivalence? false
                               :self-hosting? false}})
           (catch Throwable error
             (if (fatal? error)
               (throw error)
               (let [data (if (instance? clojure.lang.ExceptionInfo error)
                            (ex-data error) {})]
                 (rejected-result key (or (:id data) "C16-ENTRY")
                                  {:artifact-id (:artifact-id
                                                 (when (map? data) data))}))))))))))

(defn lookup!
  "Read and fully revalidate one immutable cache entry, if present."
  [store key validation-ops]
  (validate-store! store)
  (validate-key! key)
  (validate-cache-operations! validation-ops)
  (try
    (with-global-store-lock
     store #(recover-orphans! store validation-ops))
    (if *key-lock-held*
      (lookup-unlocked! store key validation-ops)
      (with-key-lock store (key-id key)
        #(lookup-unlocked! store key validation-ops)))
    (catch Throwable error
      (if (fatal? error)
        (throw error)
        (let [data (if (instance? clojure.lang.ExceptionInfo error)
                     (ex-data error) {})]
          (rejected-result key (or (:id data) "C16-ENTRY")
                           {:artifact-id (:artifact-id
                                          (when (map? data) data))}))))))
