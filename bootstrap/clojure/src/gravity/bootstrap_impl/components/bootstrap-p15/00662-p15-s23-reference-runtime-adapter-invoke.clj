

(defn p15-s23-reference-runtime-adapter-invoke
  [runtime-rule function args authority]
  (let [safe-function
        (when (symbol? function) function)
        reject-input!
        (fn [diagnostic plan-id source-id missing-fact remediation extra]
          (p15-s23-reference-runtime-adapter-noncapability-reject!
           diagnostic runtime-rule safe-function plan-id source-id missing-fact
           remediation extra))
        bound-input!
        (fn [definition-name value maximum-nodes maximum-depth]
          (try
            (p15-s23-reference-runtime-bounded-value!
             "p15-s23-reference-runtime-adapter" :jvm definition-name value
             maximum-nodes maximum-depth)
            (catch Exception exception
              (let [data (ex-data exception)
                    failure-cause
                    {:cause-class (.getName (class exception))
                     :cause-message-hash
                     (str "sha256:"
                          (sha256-hex (or (.getMessage exception) "")))}]
                (reject-input!
                 "P15S23X002" nil nil
                 (or (:missing-fact data) :runtime-adapter-input-bounds)
                 :supply_bounded_runtime_adapter_input
                 (merge
                  (select-keys
                   data
                   [:runtime-contract-definition :observed-nodes
                    :observed-depth :maximum-nodes :maximum-depth
                    :observed-total-scalar-bytes
                    :maximum-total-scalar-bytes])
                  {:redaction failure-cause
                   :failure-cause failure-cause
                   :cause-diagnostic (:id data)}))))))]
    ;; Reject hostile collection/scalar domains and cumulative byte budgets
    ;; before reading any candidate plan identity or hashing a failure record.
    (bound-input! :runtime-adapter-function function 1
                  p15-s23-reference-runtime-max-contract-depth)
    (when-not (= p15-s23-stage2-runtime-artifact-closed-plan-function
                 function)
      (reject-input!
       "P15S23X002" nil nil :runtime-contract-function-scope
       :use_closed_plan_runtime_entrypoint {}))
    (bound-input! :runtime-adapter-arguments args
                  p15-s23-reference-runtime-max-contract-nodes
                  p15-s23-reference-runtime-max-closed-plan-carrier-depth)
    (bound-input! :runtime-adapter-authority authority
                  p15-s23-reference-runtime-max-contract-nodes
                  p15-s23-reference-runtime-max-contract-depth)
    (when-not (and (= "clojure.lang.PersistentVector"
                      (some-> args class .getName))
                   (= 1 (count args))
                   (contains?
                    p15-s23-reference-runtime-supported-collection-class-names
                    (some-> (first args) class .getName))
                   (map? (first args)))
      (reject-input!
       "P15S23X002" nil nil :runtime-adapter-argument-schema
       :supply_one_bounded_closed_plan {}))
    (let [target-plan (first args)]
      (bound-input! :runtime-adapter-target-plan target-plan
                    p15-s23-reference-runtime-max-contract-nodes
                    p15-s23-reference-runtime-max-closed-plan-carrier-depth)
      (let [candidate-plan-id (:plan-id target-plan)
            source-record (:source target-plan)
            module-record (:module target-plan)
            candidate-source-id
            (when (map? source-record) (:sha256 source-record))
            digest?
            #(and (string? %)
                  (boolean (re-matches #"sha256:[0-9a-f]{64}" %)))
            plan-id (when (digest? candidate-plan-id) candidate-plan-id)
            source-id (when (digest? candidate-source-id)
                        candidate-source-id)]
        (when-not (and (map? source-record)
                       (map? module-record)
                       plan-id source-id
                       (string? (:path source-record))
                       (string? (:source-path module-record)))
          (reject-input!
           "P15S23X002" plan-id source-id
           :runtime-adapter-plan-envelope
           :restore_closed_plan_identity {}))
        (let [normalized-plan
              (-> target-plan
                  (dissoc :plan-id)
                  (update :source dissoc :path)
                  (update :module dissoc :source-path))
              derived-plan-id
              (c4-artifact-id (c-backend-canonical-value normalized-plan))]
        (when-not (and (= derived-plan-id plan-id)
                       source-id)
          (reject-input!
           "P15S23X002" plan-id source-id
           :runtime-adapter-plan-identity
           :restore_closed_plan_identity {}))
        (when-not (p15-s23-reference-runtime-rule-authentic? runtime-rule)
          (reject-input!
           "P15S23X002" plan-id source-id
           :runtime-contract-authenticity
           :restore_pinned_runtime_contract {}))
        (let [closed-plan-validation
              (try
                (p15-s23-closed-runtime-plan-validation!
                 (get-in target-plan [:source :path]) :jvm target-plan)
                (catch Exception exception
                  (let [data (ex-data exception)
                        diagnostic
                        (if (contains?
                             p15-s23-reference-runtime-preserved-diagnostic-ids
                             (:id data))
                          (:id data)
                          "P15S23X002")
                        failure-cause
                        {:cause-class (.getName (class exception))
                         :cause-message-hash
                         (str "sha256:"
                              (sha256-hex
                               (or (.getMessage exception) "")))}]
                    (reject-input!
                     diagnostic plan-id source-id
                     (or (:missing-fact data)
                         :runtime-adapter-plan-validation)
                     :restore_bounded_closed_plan
                     {:redaction failure-cause
                      :failure-cause failure-cause
                      :cause-diagnostic (:id data)}))))]
          (p15-s23-reference-runtime-adapter-validated-invoke
           runtime-rule function args authority target-plan plan-id source-id
           closed-plan-validation)))))))