(ns gravity.pass-cache.retention-scan
  "Validate retained commits and plan bounded CAS reclamation."
  (:require [gravity.digest :as digest]
            [gravity.pass-cache.canonical-decode :as canonical-decode]
            [gravity.pass-cache.entry-schema :as entry-schema]
            [gravity.pass-cache.path-policy :as path-policy]
            [gravity.pass-cache.policy :as policy]
            [gravity.pass-cache.receipt-validation :as receipt-validation]
            [gravity.pass-cache.secure-io :as secure-io]
            [gravity.pass-execution :as pass-execution]
            [gravity.pass-execution.receipt :as pass-receipt])
  (:import [java.nio.file SecureDirectoryStream]))

(declare validate-retained-entry-references!)

(defn retained-entry-references!
  [store directories entry-names validation-ops]
  (let [^SecureDirectoryStream entries (:entries directories)]
    (loop [remaining (seq entry-names) count 0
           blob-names #{} receipt-names #{}]
      (if-let [relative (first remaining)]
        (let [next-count (inc count)
              _ (when (> next-count policy/maximum-entry-count)
                  (policy/fail! "C16-POLICY"
                                "cache recovery entry traversal exceeds its bound"
                                {:maximum-count policy/maximum-entry-count}))
              name (str relative)]
          (when-not (re-matches #"sha256:[0-9a-f]{64}\.edn" name)
            (policy/fail! "C16-POLICY" "cache entry residue name is invalid"
                          {:name name}))
          (let [reference
                (try
                  (let [entry (canonical-decode/decode-canonical-bytes
                               (secure-io/secure-read-bytes!
                                store :entries entries relative
                                policy/maximum-entry-bytes)
                               policy/maximum-entry-bytes)
                        entry (entry-schema/validate-retained-entry-commit!
                               name entry)
                        entry (validate-retained-entry-references!
                               store directories entry validation-ops)
                        blob-id (:blob-id entry)
                        receipt-id (:producer-receipt-id entry)]
                    (when (and (policy/sha256-id? blob-id)
                               (policy/sha256-id? receipt-id))
                      {:blob-name (str blob-id ".edn")
                       :receipt-name (str receipt-id ".edn")}))
                  (catch Throwable error
                    (if (policy/fatal? error) (throw error) nil)))]
            (if reference
              (recur (next remaining) next-count
                     (conj blob-names (:blob-name reference))
                     (conj receipt-names (:receipt-name reference)))
              ;; An undecipherable retained entry could still be forensic
              ;; evidence.  Keep every CAS object rather than guessing.
              {:complete? false})))
        {:complete? true :blob-names blob-names
         :receipt-names receipt-names}))))

(defn verified-pass-execution-receipt-id!
  [receipt]
  (let [observed (policy/require-sha256! :receipt-id (:receipt-id receipt))
        calculated (pass-receipt/calculated-receipt-id receipt)]
    (when-not (= observed calculated)
      (policy/fail! "C16-ENTRY" "orphan receipt content identity is stale"
                    {:observed observed}))
    observed))

(defn validate-retained-entry-references!
  [store directories entry validation-ops]
  (let [blob-name (path-policy/sha-file-name! :blob-id (:blob-id entry) ".edn")
        receipt-name (path-policy/sha-file-name! :producer-receipt-id
                                                 (:producer-receipt-id entry)
                                                 ".edn")
        ^SecureDirectoryStream blobs (:blobs directories)
        ^SecureDirectoryStream receipts (:receipts directories)]
    (when-not (and (secure-io/secure-child-exists? blobs blob-name)
                   (secure-io/secure-child-exists? receipts receipt-name))
      (policy/fail! "C16-STALE"
                    "retained commit references missing CAS objects" {}))
    (let [receipt (canonical-decode/decode-canonical-bytes
                   (secure-io/secure-read-bytes!
                    store :receipts receipts receipt-name
                    policy/maximum-entry-bytes)
                   policy/maximum-entry-bytes)
          _ (pass-execution/validate-execution-receipt!
             receipt (:contract entry)
             (receipt-validation/receipt-validation-ops validation-ops))
          receipt-id (verified-pass-execution-receipt-id! receipt)
          request (-> (select-keys receipt policy/execution-request-fields)
                      (assoc :contract (:contract entry)
                             :execution-mode :executed
                             :authority
                             (select-keys (:authority receipt)
                                          [:input-authorities :claimed-level
                                           :scope])))
          stage-key-var (ns-resolve 'gravity.pass-cache 'stage-cache-key)
          _ (when-not stage-key-var
                     (policy/fail! "C16-POLICY"
                                   "semantic stage key validator is unavailable" {}))
          reconstructed-key ((var-get stage-key-var) request)
          blob-bytes (secure-io/secure-read-bytes!
                      store :blobs blobs blob-name policy/maximum-blob-bytes)
          _ (when-not (= (:blob-id entry)
                         (str "sha256:"
                              (digest/sha256-bytes-hex blob-bytes)))
              (policy/fail! "C16-STALE"
                            "retained artifact blob content identity is stale" {}))
          expected-facts {:input (:input-facts receipt)
                          :output (:output-facts receipt)
                          :requires (:requires receipt)
                          :preserves (:preserves receipt)
                          :invalidates (:invalidates receipt)
                          :regenerates (:regenerates receipt)}
          expected-evidence
          {:verifier-ids (vec (sort (map :verifier-id
                                         (:verifier-reports receipt))))
           :evidence-ids (vec (sort (map :evidence-id
                                         (:evidence-records receipt))))}
          _ (when-not
              (and (= (:producer-receipt-id entry) receipt-id)
                   (= (:cache-key-id entry)
                      (:semantic-key-id reconstructed-key))
                   (= (:semantic-key-id entry)
                      (:semantic-key-id reconstructed-key))
                   (= (:stage entry) (:stage receipt)
                      (:stage reconstructed-key))
                   (= (:pass-contract-id entry)
                      (:pass-contract-id receipt)
                      (:pass-contract-id reconstructed-key))
                   (= (:contract entry) (:contract reconstructed-key))
                   (= (:pass-contract-id entry)
                      (pass-execution/pass-contract-id (:contract entry)))
                   (= (:artifact-id entry) (:output-artifact-id receipt))
                   (= (:facts entry) expected-facts)
                   (= (:evidence entry) expected-evidence)
                   (= (:provenance-id entry)
                      (get-in receipt [:provenance :provenance-id])
                      (get-in reconstructed-key [:provenance :provenance-id]))
                   (= (:diagnostic-schema-id entry)
                      (get-in receipt
                              [:semantic-bindings :diagnostic-schema-id])
                      (:diagnostic-schema-id reconstructed-key))
                   (= (:diagnostic-stream-id entry)
                      (:diagnostic-stream-id receipt)
                      (:diagnostic-stream-id reconstructed-key)))
              (policy/fail!
               "C16-STALE"
               "retained commit and referenced CAS objects are inconsistent"
               {}))
          artifact (canonical-decode/decode-canonical-bytes
                    blob-bytes policy/maximum-blob-bytes)
          artifact (receipt-validation/validate-artifact!
                    validation-ops artifact
                    {:entry entry :key reconstructed-key})
          artifact-id (receipt-validation/artifact-id-of validation-ops artifact)]
      (when-not (= artifact-id (:artifact-id entry))
        (policy/fail!
         "C16-STALE"
         "retained artifact identity differs from its bound entry" {})))
    entry))

(defn plan-unreferenced-files!
  [store path-key ^SecureDirectoryStream directory relative-names retained
   name-pattern maximum-bytes]
  (let [maximum-count (case path-key
                        :blobs policy/maximum-blob-count
                        :receipts policy/maximum-receipt-count)]
    (loop [remaining (seq relative-names) count 0 plan []]
     (if-let [relative (first remaining)]
      (let [next-count (inc count)
            _ (when (> next-count maximum-count)
                (policy/fail! "C16-POLICY"
                              "cache recovery CAS traversal exceeds its bound"
                              {:path-key path-key :maximum-count maximum-count}))
            name (str relative)]
        (when-not (re-matches name-pattern name)
          (policy/fail! "C16-POLICY" "cache CAS residue name is invalid"
                        {:path-key path-key :name name}))
        (when-not (contains? retained name)
          (let [bytes (secure-io/secure-read-bytes!
                       store path-key directory relative maximum-bytes)
                expected-name
                (case path-key
                  :blobs (str "sha256:"
                              (digest/sha256-bytes-hex bytes) ".edn")
                  :receipts
                  (let [receipt (canonical-decode/decode-canonical-bytes
                                 bytes policy/maximum-entry-bytes)
                        id (verified-pass-execution-receipt-id! receipt)]
                    (str id ".edn"))
                  (policy/fail! "C16-POLICY" "orphan recovery path is unsupported"
                                {:path-key path-key}))]
            (when-not (= name expected-name)
              (policy/fail! "C16-ENTRY"
                            "corrupt unreferenced CAS object is retained"
                            {:path-key path-key :name name}))))
        (recur (next remaining) next-count
               (if (contains? retained name) plan (conj plan relative))))
      plan))))
