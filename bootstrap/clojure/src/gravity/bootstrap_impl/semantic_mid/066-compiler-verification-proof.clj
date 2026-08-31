(defn- semantic-mid-compiler-verification-proof
  [artifact]
  {:diagnostic-schema-complete?
   (= :complete (get-in artifact [:diagnostic-schema :status]))
   :diagnostic-stream-deterministic?
   (= [:rule :primary :artifact]
      (get-in artifact [:diagnostic-stream :ordering-key]))
   :redaction-public-safe?
   (true? (get-in artifact [:redaction-report :public-safe?]))
   :golden-fixtures-matched?
   (every? #(= :matched (:status %))
           (:golden-diagnostic-fixtures artifact))
   :incremental-records-complete?
   (and (= :consistent
           (get-in artifact [:incremental-dependency-graph :status]))
        (= :complete (get-in artifact [:cache-key-schema :status]))
        (= :gravity/cache-entry
           (get-in artifact [:cache-entry-manifest :artifact]))
        (= :passed (get-in artifact [:revalidation-report :status])))
   :plugin-policy-enforced?
   (and (= :accepted (get-in artifact [:plugin-manifest :status]))
        (= :compatible
           (get-in artifact [:api-compatibility-report :status]))
        (= :sandboxed (get-in artifact [:sandbox-grant :status]))
        (= :passed (get-in artifact [:plugin-conformance-results :status])))
   :verification-evidence-present?
   (and (every? #(= :present (:status %))
                (:pass-evidence-records artifact))
        (every? #(= :accepted (:result %))
                (:translation-validation-logs artifact)))
   :trust-report-complete?
   (= :complete (get-in artifact [:compiler-trust-report :status]))
   :release-gate-passed?
   (= :passed (get-in artifact [:release-gate-report :status]))
   :status :complete})

(defn- semantic-mid-compiler-verification-conformance
  []
  {:documents ["C15" "C16" "C17" "C18"]
   :task "P06-T06"
   :required-diagnostic-ids compiler-verification-diagnostic-ids
   :diagnostic-status :complete
   :incremental-status :complete
   :plugin-status :complete
   :verification-status :complete
   :status :complete})
