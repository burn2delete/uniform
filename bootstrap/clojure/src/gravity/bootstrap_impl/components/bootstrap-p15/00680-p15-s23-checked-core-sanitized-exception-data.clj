

(defn p15-s23-checked-core-sanitized-exception-data
  [exception]
  (let [data (if (map? (ex-data exception)) (ex-data exception) {})
        id (:id data)
        allowed-ids
        (set
         (concat c6-lowering-diagnostic-ids
                 c7-type-diagnostic-ids
                 c8-effect-diagnostic-ids
                 c15-diagnostics-diagnostic-ids
                 stage1-reader-execution-diagnostic-ids
                 ["C2-NUMERIC"]
                 p15-s23-reference-runtime-preserved-diagnostic-ids))
        bounded-keyword?
        (fn [value]
          (and (keyword? value)
               (<= (count (or (namespace value) "")) 128)
               (<= (count (name value)) 128)))
        source-span (:source-span data)
        safe-source
        (when (and (p15-s23-checked-core-authority-small-map?
                    source-span 8)
                   (string? (:source source-span))
                   (<= (count (:source source-span)) 4096)
                   (= :valid
                      (:status
                       (p15-s23-closed-core-bounded-utf8-count
                        (:source source-span) 16384))))
          (:source source-span))
        safe-span
        (when (p15-s23-checked-core-authority-small-map? source-span 8)
          (let [numeric
                (into {}
                      (filter
                       (fn [[_ value]]
                         (and (integer? value)
                              (<= 0 value Integer/MAX_VALUE))))
                      (select-keys source-span [:start :end :line :column]))]
            (cond-> numeric safe-source (assoc :source safe-source))))
        projection-base
        (cond->
         {:message "Checked-core authority issuance rejected"
          :redaction
          {:cause-class-hash
           (str "sha256:" (sha256-hex (.getName (class exception))))
           :cause-message-hash
           (str "sha256:"
                (sha256-hex (or (.getMessage exception) "")))}}
          (contains? allowed-ids id)
          (assoc :id id)
          (bounded-keyword? (:bootstrap-stage data))
          (assoc :bootstrap-stage (:bootstrap-stage data))
          (bounded-keyword? (:stage data))
          (assoc :stage (:stage data))
          (bounded-keyword? (:diagnostic-family data))
          (assoc :diagnostic-family (:diagnostic-family data))
          (bounded-keyword? (:missing-fact data))
          (assoc :missing-fact (:missing-fact data))
          (bounded-keyword? (:remediation data))
          (assoc :remediation (:remediation data))
          (seq safe-span) (assoc :source-span safe-span))
        projection-valid?
        (try
          (p15-s23-reference-runtime-bounded-value!
           "p15-s23-checked-core-authority" :jvm
           :authority-issuer-sanitized-exception-projection
           projection-base 64 8)
          true
          (catch StackOverflowError _ false)
          (catch Exception _ false))]
    (if projection-valid?
      projection-base
      (select-keys projection-base [:message :redaction]))))

(defn p15-s23-stage2-closed-checked-core-authority-binding
  "Compile and authenticate one effectful checked-core source unit, then issue
  its exact single-reference-execution authority without executing the plan."
  [source-path source-text requested-target policy-selector]
  (try
    (when-not (p15-s23-checked-core-authority-safe-source-path? source-path)
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" "<checked-core-authority>"
       {:requested-target (when (keyword? requested-target)
                            requested-target)}
       {:missing-fact :bounded-checked-core-authority-source-path}))
    (p15-s23-closed-core-source-request-bounds!
     source-path source-text requested-target)
    (when-not (p15-s23-checked-core-authority-small-map? policy-selector 8)
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path {:requested-target requested-target}
       {:missing-fact :bounded-checked-core-reference-policy-selector}))
    (try
      (p15-s23-reference-runtime-bounded-value!
       source-path :jvm :checked-core-reference-policy-selector
       policy-selector 32 8)
      (catch Exception _
        (p15-s23-closed-core-fail!
         "C8-CAPABILITY" source-path {:requested-target requested-target}
         {:missing-fact
          :bounded-checked-core-reference-policy-selector})))
    (when-not (= p15-s23-checked-core-reference-policy-selector-keys
                 (set (keys policy-selector)))
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path {:requested-target requested-target}
       {:missing-fact :exact-checked-core-reference-policy-selector}))
    (when-not (= p15-s23-checked-core-reference-policy-selector
                 policy-selector)
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path {:requested-target requested-target}
       {:missing-fact :pinned-checked-core-reference-policy-selector}))
    (let [source-content-hash (str "sha256:" (sha256-hex source-text))
          bounded-front-end
          (p15-s23-stage2-c2-c3-front-end-products source-path source-text)
          executable-form-records
          (p15-s23-closed-core-executable-form-records
           (:form-tree bounded-front-end)
           (:top-level-form-ids bounded-front-end))
          source-surface-validation
          (p15-s23-closed-core-source-surface-validation
           (:forms bounded-front-end) executable-form-records)
          _ (when-not (= :passed (:status source-surface-validation))
              (p15-s23-closed-core-fail!
               (if (= :over-limit (:status source-surface-validation))
                 "C6-VERIFY" "C6-LOWERING-GAP")
               source-path (or (first executable-form-records) {})
               (assoc source-surface-validation
                      :missing-fact
                      (if (= :over-limit
                             (:status source-surface-validation))
                        :bounded-authority-source-before-plan-hash
                        (:missing-fact source-surface-validation)))))
          emitter-rule
          (c-backend-stage2-plan-emitter-source-rule!
           source-path requested-target)
          plan
          (binding [*additional-bootstrap-targets*
                    stage2-runtime-derived-source-targets]
            (p15-s23-stage2-plan-emitter-compile-source
             (:emitter emitter-rule) source-path source-text))
          _ (p15-s23-closed-runtime-plan-validation!
             source-path requested-target plan)
          module (:module plan)
          requirements
          (p15-s23-closed-core-preflight-effect-requirements plan)
          structural-operation-set
          (p15-s23-checked-core-authority-structural-operations requirements)
          _ (when-not (seq structural-operation-set)
              (p15-s23-closed-core-fail!
               "C8-CAPABILITY" source-path module
               {:missing-fact :effectful-checked-core-authority-request
                :required-effects (:required-effects requirements)
                :required-capabilities (:required-capabilities requirements)}))
          _ (when-not (and (= :hosted (:profile module))
                           (= :safe (:safety module))
                           (= :jvm (:target module))
                           (= :jvm requested-target)
                           (= (:effects module)
                              (:required-effects requirements))
                           (= (:capabilities module)
                              (:required-capabilities requirements)))
              (p15-s23-closed-core-fail!
               "C8-CAPABILITY" source-path module
               {:missing-fact
                :exact-effectful-checked-core-module-authority-contract
                :required-effects (:required-effects requirements)
                :required-capabilities (:required-capabilities requirements)}))
          runtime-rule
          (c-backend-stage2-runtime-source-rule!
           source-path requested-target)
          _ (when-not (p15-s23-reference-runtime-rule-authentic? runtime-rule)
              (p15-s23-closed-core-fail!
               "C8-CAPABILITY" source-path runtime-rule
               {:missing-fact :authenticated-pinned-reference-runtime-policy}))
          policy
          (get (:runtime-contract-definitions runtime-rule)
               'p15-s23-checked-core-program-authority-policy)
          _ (when-not (= p15-s23-checked-core-expected-program-authority-policy
                         policy)
              (p15-s23-closed-core-fail!
               "C8-CAPABILITY" source-path policy
               {:missing-fact :authenticated-program-authority-policy}))
          record
          (p15-s23-checked-core-authority-record
           source-content-hash plan module requirements runtime-rule policy)]
      (when-not (p15-s23-checked-core-authority-record-valid? record)
        (p15-s23-closed-core-fail!
         "C8-CAPABILITY" source-path record
         {:missing-fact :independently-valid-checked-core-authority-record}))
      record)
    (catch StackOverflowError error
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY"
       (if (p15-s23-checked-core-authority-safe-source-path? source-path)
         source-path
         "<checked-core-authority>")
       {:requested-target (when (keyword? requested-target)
                            requested-target)}
       {:missing-fact :contained-authority-issuer-host-stack-failure
        :redaction
        {:cause-class-hash
         (str "sha256:" (sha256-hex (.getName (class error))))}}))
    (catch clojure.lang.ExceptionInfo ex
      (throw
       (ex-info "Checked-core authority issuance rejected"
                (p15-s23-checked-core-sanitized-exception-data ex))))
    (catch Exception error
      (p15-s23-closed-core-fail!
       "C8-CAPABILITY" source-path {:requested-target requested-target}
       {:missing-fact :contained-authority-issuer-host-failure
        :redaction
        {:cause-class-hash
         (str "sha256:" (sha256-hex (.getName (class error))))
         :cause-message-hash
         (str "sha256:" (sha256-hex (or (.getMessage error) "")))}}))))