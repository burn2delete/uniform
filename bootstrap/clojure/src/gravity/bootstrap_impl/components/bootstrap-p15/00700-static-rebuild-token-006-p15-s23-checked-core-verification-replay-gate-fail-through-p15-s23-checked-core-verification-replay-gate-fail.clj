(let [static-rebuild-token (nth __gravity_bootstrap_checked_core_authority_values 0)
      p15-s23-stage2-closed-checked-core-source-artifact-internal __gravity_bootstrap_checked_core_source_helper
      p15-s23-stage2-closed-checked-core-rebuild-internal __gravity_bootstrap_checked_core_rebuild_helper]
(defn p15-s23-checked-core-verification-replay-gate-fail!
  [diagnostic source-path authority missing-fact exception started?]
  (let [safe-source-path
        (if (p15-s23-checked-core-authority-safe-source-path? source-path)
          source-path
          "<verification-replay-gate>")
        bounded-keyword?
        (fn [value]
          (and (keyword? value)
               (<= (count (or (namespace value) "")) 128)
               (<= (count (name value)) 128)))
        allowed-diagnostic?
        (or (contains? #{"R1-FAILURE" "R4-EXCEPTION" "R11-GRANT"}
                       diagnostic)
            (contains? p15-s23-reference-runtime-preserved-diagnostic-ids
                       diagnostic))
        _ (when-not (and allowed-diagnostic?
                         (bounded-keyword? missing-fact)
                         (boolean? started?))
            (p15-s23-closed-core-fail!
             "C8-CAPABILITY" safe-source-path {}
             {:missing-fact :bounded-verification-gate-failure-input}))
        _ (p15-s23-checked-core-bounded-ingress!
           "C8-CAPABILITY" :verification-gate-failure-authority
           authority p15-s23-reference-runtime-max-contract-nodes
           p15-s23-reference-runtime-max-contract-depth)
        cause-message
        (when exception (.getMessage exception))
        bounded-cause-message
        (if (and (string? cause-message)
                 (= :valid
                    (:status
                     (p15-s23-closed-core-bounded-utf8-count
                      cause-message 4096))))
          cause-message
          "unbounded-cause-redacted")
        redaction
        (if exception
          {:cause-class-hash
           (str "sha256:" (sha256-hex (.getName (class exception))))
           :cause-message-hash
           (str "sha256:" (sha256-hex bounded-cause-message))}
          :none)
        redaction-status (if exception :applied :not-required)
        candidate-exception-data
        (when (instance? clojure.lang.ExceptionInfo exception)
          (ex-data exception))
        exception-data
        (when
         (and (map? candidate-exception-data)
              (try
                (p15-s23-reference-runtime-bounded-value!
                 "verification-replay-structured-diagnostic" :jvm
                 :structured-runtime-diagnostic candidate-exception-data 256 16)
                true
                (catch StackOverflowError _ false)
                (catch Exception _ false)))
          candidate-exception-data)
        source-span-data (:source-span exception-data)
        safe-source-span
        (when (map? source-span-data)
          (into {}
                (filter
                 (fn [[_ value]]
                   (and (integer? value)
                        (<= 0 value Integer/MAX_VALUE))))
                (select-keys source-span-data
                             [:start :end :line :column])))
        projection-keyword?
        (fn [value]
          (and (keyword? value)
               (<= (count (or (namespace value) "")) 128)
               (<= (count (name value)) 128)))
        safe-details
        (into {}
              (filter
               (fn [[key value]]
                 (case key
                   :operator (and (symbol? value)
                                  (<= (count (str value)) 256))
                   (:expected-arity :actual-arity)
                   (and (integer? value)
                        (<= 0 value Integer/MAX_VALUE))
                   (:expected-type :actual-type)
                   (projection-keyword? value)
                   false)))
              (select-keys exception-data
                           [:operator :expected-arity :actual-arity
                            :expected-type :actual-type]))
        diagnostic-message (:message exception-data)
        bounded-diagnostic-message
        (if (and (string? diagnostic-message)
                 (= :valid
                    (:status
                     (p15-s23-closed-core-bounded-utf8-count
                      diagnostic-message 4096))))
          diagnostic-message
          "unbounded-diagnostic-message-redacted")
        runtime-diagnostic-projection-candidate
        (when (and (map? exception-data)
                   (contains?
                    p15-s23-reference-runtime-preserved-diagnostic-ids
                    (:id exception-data)))
          {:id (:id exception-data)
           :stage (when (projection-keyword? (:stage exception-data))
                    (:stage exception-data))
           :diagnostic-family
           (when (projection-keyword? (:diagnostic-family exception-data))
             (:diagnostic-family exception-data))
           :source-span safe-source-span
           :remediation
           (when (projection-keyword? (:remediation exception-data))
             (:remediation exception-data))
           :missing-fact
           (when (projection-keyword? (:missing-fact exception-data))
             (:missing-fact exception-data))
           :details safe-details
           :message-hash
           (str "sha256:"
                (sha256-hex bounded-diagnostic-message))
           :artifact-edge
           {:checked-core-artifact-id
            (get-in authority [:binding :checked-core-artifact-id])
            :mapping-id (get-in authority [:binding :mapping-id])
            :provenance-binding-id
            (get-in authority [:binding :provenance-binding-id])}})
        runtime-diagnostic-projection
        (when (and runtime-diagnostic-projection-candidate
                   (try
                     (p15-s23-reference-runtime-bounded-value!
                      "verification-replay-structured-projection" :jvm
                      :structured-runtime-diagnostic-projection
                      runtime-diagnostic-projection-candidate 128 12)
                     true
                     (catch StackOverflowError _ false)
                     (catch Exception _ false)))
          runtime-diagnostic-projection-candidate)
        success-audit
        (p15-s23-checked-core-verification-replay-audit-records
         authority false)
        decision-base (dissoc (first (:decision-records success-audit))
                              :decision-id)
        decision-record
        (if started?
          (first (:decision-records success-audit))
          (-> decision-base
              (assoc :decision :deny
                     :result :deny
                     :reason missing-fact
                     :diagnostic diagnostic
                     :missing-fact missing-fact
                     :redaction redaction
                     :redaction-status redaction-status
                     :audit-status :denied)
              (#(assoc % :decision-id
                       (p15-s23-reference-runtime-hash %)))))
        action-base (dissoc (first (:action-records success-audit))
                            :record-id)
        action-record
        (-> action-base
            (assoc :action-started? started?
                   :action-status
                   (if started? :failed-before-commit
                       :rejected-before-start)
                   :result-committed? false
                   :output-committed? false
                   :diagnostic diagnostic
                   :missing-fact missing-fact
                   :reason missing-fact
                   :redaction redaction
                   :redaction-status redaction-status
                   :audit-status (if started? :failed :denied))
            (#(assoc % :record-id
                     (p15-s23-reference-runtime-hash %))))
        failure-record-base
        {:diagnostic diagnostic
         :missing-fact missing-fact
         :decision-record decision-record
         :action-record action-record
         :result-committed? false
         :output-committed? false
         :redaction redaction
         :redaction-policy :hash-host-class-and-message
         :redaction-status redaction-status
         :audit-status (if started? :failed :denied)
         :remediation :restore_pinned_verification_replay_authority
         :profile :hosted
         :target :jvm
         :runtime-family :managed
         :service-id
         :gravity.reference/checked-core-verification-runtime-service
         :effect (:effect decision-record)
         :capability (:capability decision-record)
         :provider-id (:provider-id decision-record)
         :runtime-function (:runtime-function decision-record)
         :module (:module decision-record)
         :package (:package decision-record)
         :delegated-handle-id (:delegated-handle-id decision-record)
         :host-runtime :jvm
         :host-symbol (:runtime-function decision-record)
         :host-package :gravity/bootstrap
         :gravity-type :gravity/checked-core-reference-runtime-boundary
         :adapter-id
         :gravity.reference/checked-core-verification-runtime-service
         :missing-policy
         (if (= "R4-EXCEPTION" diagnostic)
           :host-exception-translation
           :not-applicable)
         :runtime-diagnostic-projection runtime-diagnostic-projection}
        failure-record
        (assoc failure-record-base :failure-record-id
               (p15-s23-reference-runtime-hash failure-record-base))]
    (when-not
     (and (or (contains?
               #{"R1-FAILURE" "R4-EXCEPTION" "R11-GRANT"}
               diagnostic)
              (and runtime-diagnostic-projection
                   (= diagnostic (:id runtime-diagnostic-projection))))
          (= (:failure-record-fields
              p15-s23-checked-core-expected-verification-replay-audit-policy)
             (set (keys failure-record)))
          (= (:decision-record-fields
              p15-s23-checked-core-expected-verification-replay-audit-policy)
             (set (keys decision-record)))
          (= (:action-record-fields
              p15-s23-checked-core-expected-verification-replay-audit-policy)
             (set (keys action-record)))
          (= (:failure-record-id failure-record)
             (p15-s23-reference-runtime-hash
              (dissoc failure-record :failure-record-id))))
      (p15-s23-closed-core-fail!
       "C10-CHECK" safe-source-path authority
       {:missing-fact :exact-verification-replay-failure-audit-schema}))
    (fail!
     diagnostic
     "Checked-core verification replay gate rejected execution"
     (merge
      {:source-span {:source safe-source-path}
       :stage :p15-s23-checked-core-verification-replay-gate
       :diagnostic-family :p15-s23-checked-core-verification-replay}
      failure-record)))))
