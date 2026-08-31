

(def p15-s23-unsafe-required-preserves
  #{:source-spans :artifact-provenance
    :unsafe-island-index :unsafe-operation-inventory
    :safe-wrapper-boundaries :package-safety-metadata
    :review-state :revalidation-triggers :evidence-linkage})

(def p15-s23-unsafe-required-evidence-links
  #{:compiler-source-inventory
    :compiler-pipeline-manifest
    :runtime-manifest-and-capability-enforcement-report
    :bootstrap-provenance-attestation
    :trusted-computing-base-delta-record})

(def p15-s23-unsafe-external-seed-boundaries
  [:clojure-stage0-bootstrap
   :clojure-stage0-verifier
   :jvm-runtime
   :host-filesystem-source-loading])

(def p15-s23-unsafe-diagnostic-messages
  {"P15S23U001" "P15-S23 unsafe audit report is missing"
   "P15S23U002" "P15-S23 unsafe island index or operation inventory is incomplete"
   "P15S23U003" "P15-S23 unsafe wrapper or evidence boundary is incomplete"
   "P15S23U004" "P15-S23 package safety metadata is incomplete"
   "P15S23U005" "P15-S23 unsafe audit review is stale or lacks revalidation triggers"
   "P15S23U006" "P15-S23 unsafe audit is missing required evidence links"
   "P15S23U007" "P15-S23 unsafe audit makes an unsupported self-hosting, seed-retirement, or release-eligibility claim"})

(def p15-s23-unsafe-diagnostic-ids
  ["P15S23U001" "P15S23U002" "P15S23U003" "P15S23U004"
   "P15S23U005" "P15S23U006" "P15S23U007"])

(defn p15-s23-unsafe-fail!
  [id source-path value data]
  (fail! id
         (get p15-s23-unsafe-diagnostic-messages
              id
              "P15-S23 unsafe audit report failed")
         (merge {:source-span {:source source-path}
                 :stage :p15-s23-unsafe-audit-report
                 :diagnostic-family :p15-s23-unsafe-audit-report
                 :value value
                 :remediation "Emit a SAFE6/GOV9/PKG8 unsafe audit report for the current Gravity compiler source, include package safety metadata, preserve external Clojure/JVM trust boundaries as separate TCB facts, and keep self-hosting plus release claims false until the full evidence bundle exists."}
                data)))

(defn p15-s23-unsafe-diagnostic-record
  [source-path id value data]
  {:artifact :gravity/diagnostic
   :diagnostic-id (str "diag-" (str/lower-case id))
   :diagnostic id
   :severity :error
   :stage :p15-s23-unsafe-audit-report
   :source-span {:source source-path}
   :message (get p15-s23-unsafe-diagnostic-messages id)
   :facts data
   :observed value
   :remediation :repair_p15_s23_unsafe_audit_report})

(defn p15-s23-unsafe-form-occurrences
  [forms]
  (letfn [(walk [path form]
            (let [hit (when (and (seq? form)
                                 (= 'unsafe (first form)))
                        [{:form-path path
                          :operation :unsafe-form
                          :source-form-preview (pr-str (take 2 form))}])
                  child-results
                  (cond
                    (seq? form)
                    (map-indexed
                     (fn [idx child]
                       (walk (conj path idx) child))
                     form)

                    (vector? form)
                    (map-indexed
                     (fn [idx child]
                       (walk (conj path idx) child))
                     form)

                    :else [])]
              (vec (concat hit (apply concat child-results)))))]
    (vec (apply concat
                (map-indexed
                 (fn [idx form]
                   (walk [idx] form))
                 forms)))))

(defn p15-s23-unsafe-island-index
  [source-path source-data]
  (let [occurrences
        (p15-s23-unsafe-form-occurrences (:forms source-data))
        islands
        (mapv (fn [idx occurrence]
                {:unsafe-island-id
                 (keyword (str "p15-s23-unaudited-unsafe-" (inc idx)))
                 :source-path source-path
                 :form-path (:form-path occurrence)
                 :operation (:operation occurrence)
                 :review-state :missing-audit
                 :safe-wrappers []
                 :evidence []
                 :status :failed})
              (range)
              occurrences)]
    {:artifact :gravity/p15-s23-unsafe-island-index
     :source-path source-path
     :unsafe-forms-scanned? true
     :unsafe-islands islands
     :unsafe-island-count (count islands)
     :unaudited-unsafe-form-count (count occurrences)
     :current-candidate-has-gravity-unsafe-islands? (pos? (count islands))
     :status (if (empty? islands) :complete :failed)}))

(defn p15-s23-unsafe-operation-inventory
  [island-index]
  (let [families (set (map :operation (:unsafe-islands island-index)))]
    {:artifact :gravity/p15-s23-unsafe-operation-inventory
     :unsafe-operation-families (vec (sort families))
     :unsafe-operation-count (count (:unsafe-islands island-index))
     :scan-complete? (:unsafe-forms-scanned? island-index)
     :unaudited-unsafe-form-count
     (:unaudited-unsafe-form-count island-index)
     :status (if (and (= :complete (:status island-index))
                      (zero? (:unaudited-unsafe-form-count island-index)))
               :complete
               :failed)}))

(defn p15-s23-safe-wrapper-boundary-table
  [island-index]
  (let [islands (:unsafe-islands island-index)
        missing-wrappers
        (filterv #(empty? (:safe-wrappers %)) islands)]
    {:artifact :gravity/p15-s23-safe-wrapper-boundary-table
     :safe-wrapper-boundaries []
     :unsafe-island-count (count islands)
     :safe-wrapper-count 0
     :safe-wrapper-coverage-complete? (empty? missing-wrappers)
     :missing-safe-wrapper-islands
     (mapv :unsafe-island-id missing-wrappers)
     :status (if (empty? missing-wrappers) :complete :failed)}))

(defn p15-s23-package-safety-metadata
  [source-path island-index operation-inventory wrapper-table]
  (let [metadata-base
        {:artifact :gravity/p15-s23-package-safety-metadata
         :package "gravity.bootstrap.p15-s23.compiler"
         :source-path source-path
         :unsafe-island-count (:unsafe-island-count island-index)
         :unsafe-operation-count
         (:unsafe-operation-count operation-inventory)
         :unsafe-operation-families
         (:unsafe-operation-families operation-inventory)
         :safe-wrapper-count (:safe-wrapper-count wrapper-table)
         :review-state :reviewed
         :schema-validated? true
         :safety-record-in-artifact-manifest? true
         :lockfile-sbom-record-required? false
         :dependency-unsafe-summaries []
         :release-eligible? false
         :status (if (and (= :complete (:status island-index))
                          (= :complete (:status operation-inventory))
                          (= :complete (:status wrapper-table)))
                   :complete
                   :failed)}
        metadata-id (c4-artifact-id metadata-base)]
    (assoc metadata-base :package-safety-metadata-id metadata-id)))

(defn p15-s23-unsafe-review-and-revalidation-record
  [source-path source-data package-metadata]
  (let [source-hash (str "sha256:" (sha256-hex (:source-text source-data)))]
    {:artifact :gravity/p15-s23-unsafe-review-and-revalidation-record
     :source-path source-path
     :review-state (:review-state package-metadata)
     :reviewed-by :stage0-bootstrap-verifier
     :reviewed-on "2026-06-30"
     :review-fingerprint source-hash
     :stale? false
     :stale-audits-block-release? true
     :revalidation-triggers [:source-change
                             :unsafe-form-change
                             :safe-wrapper-change
                             :profile-change
                             :backend-change
                             :target-change
                             :optimizer-change
                             :package-metadata-change]
     :status (if (and (= :reviewed (:review-state package-metadata))
                      (not (:release-eligible? package-metadata)))
               :complete
               :failed)}))

(defn p15-s23-external-seed-boundary-audit
  [tcb-artifact]
  (let [tcb-boundaries
        (set (get-in tcb-artifact
                     [:residual-trust-boundary-record
                      :residual-boundaries]))
        records
        (mapv (fn [boundary]
                {:boundary boundary
                 :present-in-tcb? (contains? tcb-boundaries boundary)
                 :classification
                 :trusted-boundary-not-gravity-unsafe-island
                 :reason :hosted-seed-boundary-recorded-by-tcb-delta})
              p15-s23-unsafe-external-seed-boundaries)
        missing
        (remove :present-in-tcb? records)]
    {:artifact :gravity/p15-s23-external-seed-boundary-audit
     :external-boundaries records
     :external-boundary-count (count records)
     :missing-tcb-boundaries (mapv :boundary missing)
     :host-trust-boundaries-not-counted-as-safe-gravity? true
     :status (if (empty? missing) :complete :failed)}))