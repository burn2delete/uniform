(ns gravity.pass-cache.recovery
  "Descriptor-locked orphan recovery and staging cleanup."
  (:require [clojure.set :as set]
            [gravity.pass-cache.canonical-decode :refer :all]
            [gravity.pass-cache.entry-schema :refer :all]
            [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.retention-scan :refer :all]
            [gravity.pass-cache.secure-io :refer :all]
            [gravity.pass-cache.store-directories :refer :all])
  (:import [java.nio.file Path SecureDirectoryStream]))

(defn delete-planned-files!
  [store path-key ^SecureDirectoryStream directory plan maximum-bytes]
  (doseq [relative plan]
    (secure-file-attributes-relative!
     store path-key directory relative maximum-bytes)
    (.deleteFile directory relative))
  (when (seq plan)
    (secure-fsync-directory! directory))
  (count plan))

(defn potential-entry-reference-sets
  [store directories entry-names]
  (let [^SecureDirectoryStream entries (:entries directories)]
    (loop [remaining (seq entry-names) blobs #{} receipts #{}]
      (if-let [relative (first remaining)]
        (let [name (str relative)
              reference
              (try
                (let [entry (validate-retained-entry-commit!
                             name
                             (decode-canonical-bytes
                              (secure-read-bytes!
                               store :entries entries relative
                               maximum-entry-bytes)
                              maximum-entry-bytes))]
                  [(str (:blob-id entry) ".edn")
                   (str (:producer-receipt-id entry) ".edn")])
                (catch Throwable error
                  (if (fatal? error) (throw error) nil)))]
          (if reference
            (recur (next remaining) (conj blobs (first reference))
                   (conj receipts (second reference)))
            nil))
        {:blobs blobs :receipts receipts}))))

(defn recover-orphans-in-directories!
  [store directories validation-ops]
  ;; Complete the bounded, policy-valid inventory before deleting anything.
  (let [inventory (secure-store-inventory! store directories)
        entry-names (get-in inventory [:entries :names])
        blob-names (get-in inventory [:blobs :names])
        receipt-names (get-in inventory [:receipts :names])
        potential (potential-entry-reference-sets
                   store directories entry-names)
        recovery-needed?
        (or (nil? potential)
            (seq (set/difference
                  (into #{} (map str) blob-names)
                  (:blobs potential)))
            (seq (set/difference
                  (into #{} (map str) receipt-names)
                  (:receipts potential))))]
   ;; A bounded structural prepass avoids duplicate semantic validator calls
   ;; on ordinary hits while still detecting shared-blob orphan configurations.
   (if-not recovery-needed?
    {:complete? true :skipped? true}
    (let [{:keys [complete? blob-names receipt-names]}
          (retained-entry-references!
           store directories entry-names validation-ops)]
    (when complete?
      ;; Entries are the commit records.  Objects not named by any retained
      ;; entry can only be incomplete publication residue and are reclaimed
      ;; under the global cross-process store lock.
      (let [blob-plan (plan-unreferenced-files!
                       store :blobs (:blobs directories)
                       (get-in inventory [:blobs :names]) blob-names
                       #"sha256:[0-9a-f]{64}\.edn" maximum-blob-bytes)
            receipt-plan (plan-unreferenced-files!
                          store :receipts (:receipts directories)
                          (get-in inventory [:receipts :names]) receipt-names
                          #"sha256:[0-9a-f]{64}\.edn" maximum-entry-bytes)]
        ;; Both plans are fully content/name/canonical validated before the
        ;; first mutation, so a late invalid candidate preserves all residue.
        (delete-planned-files! store :blobs (:blobs directories)
                               blob-plan maximum-blob-bytes)
        (delete-planned-files! store :receipts (:receipts directories)
                               receipt-plan maximum-entry-bytes)))
    {:complete? complete? :skipped? false}))))

(defn recover-orphans!
  [store validation-ops]
  (with-secure-store-directories
   store #(recover-orphans-in-directories! store % validation-ops)))

(defn clean-staging!
  [store]
  (with-secure-store-directories
   store
   (fn [directories]
     (let [^SecureDirectoryStream staging (:staging directories)
           ;; First pass proves the complete directory is within policy before
           ;; cleanup mutates any residue.  SecureDirectoryStream permits only
           ;; one iterator, so retain the validated relative names for the
           ;; descriptor-relative mutation phase.
           residue-plan
           (loop [iterator (.iterator staging) count 0 plan []]
             (if (.hasNext iterator)
               (let [next-count (inc count)
                     item (.next iterator)
                     name (str (.getFileName ^Path item))]
                 (when (> next-count maximum-staging-count)
                   (fail! "C16-POLICY"
                          "cache staging traversal exceeds its bound"
                          {:maximum-count maximum-staging-count}))
                 (when-not (re-matches #"\.stage-[0-9a-f-]{36}\.tmp" name)
                   (fail! "C16-POLICY" "cache staging residue name is invalid"
                          {:name name}))
                 (recur iterator next-count
                        (conj plan (relative-name! name))))
               plan))
           touched? (volatile! false)]
       (doseq [relative residue-plan]
         (when-not (contains? @active-staging
                              (str (.resolve ^Path (:staging store) relative)))
           (secure-file-attributes-relative!
            store :staging staging relative maximum-file-bytes)
           (.deleteFile staging relative)
           (vreset! touched? true)))
       (when @touched?
         (secure-fsync-directory! staging)))))
  store)
