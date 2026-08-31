

(defn checked-core-validate!
  [source-path artifact]
  (checked-core-validate-overrides! source-path artifact)
  (when-not (= checked-core-stage-order (:pipeline-stage-order artifact))
    (checked-core-fail! "C1-EVIDENCE-DROP" source-path artifact
                        (checked-core-stage artifact :read-source)
                        {:expected-outcome checked-core-stage-order
                         :actual-outcome (:pipeline-stage-order artifact)}))
  (when-not (perf-present? (:source-unit-record artifact))
    (checked-core-fail! "C2-HASH" source-path artifact
                        (checked-core-stage artifact :read-source)
                        {:missing-fields [:source-unit-record]}))
  (when-not (every? #(and (perf-present? (:syntax-id %))
                          (perf-present? (:span %))
                          (perf-present? (:origin %)))
                    (:syntax-object-stream artifact))
    (checked-core-fail! "C3-ORIGIN" source-path artifact
                        (checked-core-stage artifact :build-syntax)
                        {:missing-fields [:syntax-id :span :origin]}))
  (when-not (perf-present? (:macro-expansion-trace artifact))
    (checked-core-fail! "C4-TRACE" source-path artifact
                        (checked-core-stage artifact :macro-expand)
                        {:missing-fields [:macro-expansion-trace]}))
  (when-not (perf-present? (:binding-table artifact))
    (checked-core-fail! "C5-UNRESOLVED" source-path artifact
                        (checked-core-stage artifact :resolve-names)
                        {:missing-fields [:binding-table]}))
  (when-not (and (perf-present? (:expanded-core-ast artifact))
                 (perf-present? (:core-verifier-report artifact)))
    (checked-core-fail! "C6-VERIFY" source-path artifact
                        (checked-core-stage artifact :lower-to-core)
                        {:missing-fields [:expanded-core-ast
                                          :core-verifier-report]}))
  (when-not (perf-present? (:type-facts artifact))
    (checked-core-fail! "C7-VERIFY" source-path artifact
                        (checked-core-stage artifact :type-check)
                        {:missing-fields [:type-facts]}))
  (when-not (and (perf-present? (:effect-legality-report artifact))
                 (perf-present? (:capability-proof-records artifact)))
    (checked-core-fail! "C8-CAPABILITY" source-path artifact
                        (checked-core-stage artifact :effect-check)
                        {:missing-fields [:effect-legality-report
                                          :capability-proof-records]}))
  (when-not (perf-present? (:ownership-facts artifact))
    (checked-core-fail! "C9-LINEAR-LEAK" source-path artifact
                        (checked-core-stage artifact :ownership-check)
                        {:missing-fields [:ownership-facts]}))
  (when-not (perf-present? (:safety-outcome-records artifact))
    (checked-core-fail! "C10-NO-OUTCOME" source-path artifact
                        (checked-core-stage artifact :safety-analyze)
                        {:missing-fields [:safety-outcome-records]}))
  :complete)

(defn checked-core-capability-proof
  [artifact]
  {:stage-order-preserved?
   (= checked-core-stage-order (:pipeline-stage-order artifact))
   :reader-hash-present? (perf-present? (:source-unit-record artifact))
   :syntax-origins-preserved?
   (every? #(and (perf-present? (:syntax-id %))
                 (perf-present? (:span %))
                 (perf-present? (:origin %)))
           (:syntax-object-stream artifact))
   :macro-trace-replayable? (perf-present? (:macro-expansion-trace artifact))
   :name-resolution-recorded? (perf-present? (:binding-table artifact))
   :core-verifier-passed? (perf-present? (:core-verifier-report artifact))
   :type-facts-complete? (perf-present? (:type-facts artifact))
   :effect-capability-proof-present?
   (and (perf-present? (:effect-legality-report artifact))
        (perf-present? (:capability-proof-records artifact)))
   :profile-validation-before-mir?
   (= :gravity/stage0-profile-manifest-artifact
      (get-in artifact [:profile-validation-artifact :kind]))
   :ownership-facts-present? (perf-present? (:ownership-facts artifact))
   :safety-outcomes-complete?
   (perf-present? (:safety-outcome-records artifact))
   :status :complete})