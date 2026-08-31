

(defn p15-s23-c11-mir-span-with-source
  [source-path subject]
  (let [candidate
        (when (map? subject)
          (or (:source-span subject)
              (get-in subject [:source :span])))
        candidate (if (map? candidate)
                    candidate
                    (source-span source-path 0))
        position
        (fn [value]
          (when (map? value)
            (into {}
                  (keep (fn [key]
                          (when (contains? value key)
                            [key
                             (p15-s23-c11-mir-safe-diagnostic-scalar
                              (get value key))])))
                  [:line :column :column-unit :char :byte])))
        span
        (into {}
              (keep (fn [key]
                      (when (contains? candidate key)
                         [key
                         (if (contains? #{:start :end} key)
                           (position (get candidate key))
                           (if (contains? #{:source :file} key)
                             (p15-s23-c11-mir-safe-source-path
                              (get candidate key))
                             (p15-s23-c11-mir-safe-diagnostic-scalar
                              (get candidate key))))])))
              [:source :file :form-index :start :end
               :byte-start :byte-end])]
    (if (or (string? (:source span)) (string? (:file span)))
      span
      (assoc span :source source-path))))

(def p15-s23-c11-mir-primary-keys
  #{:span :syntax-id :core-node-id :mir-operation-id :origin-id :artifact})

(def p15-s23-c11-mir-related-keys
  #{:role :span :syntax-id :core-node-id :mir-operation-id :origin-id
    :artifact})

(def p15-s23-c11-mir-remediation-keys
  #{:kind :from-stage :required-evidence :rule :action :document})

(def p15-s23-c11-mir-redaction-keys
  #{:kind :status :policy :digest :observed-at})

(defn p15-s23-c11-mir-safe-flat-record
  [record allowed-keys]
  (when (map? record)
    (into (sorted-map)
          (keep (fn [key]
                  (when (contains? record key)
                    [key (p15-s23-c11-mir-safe-diagnostic-scalar
                          (get record key))])))
          allowed-keys)))

(defn p15-s23-c11-mir-safe-related
  [source-path related]
  (when (and (vector? related) (<= (count related) 16)
             (every? map? related))
    (mapv
     (fn [record]
       (when (map? record)
         (cond->
          (p15-s23-c11-mir-safe-flat-record
           (dissoc record :span) p15-s23-c11-mir-related-keys)
           (contains? record :span)
           (assoc :span
                  (p15-s23-c11-mir-span-with-source
                   source-path {:source-span (:span record)})))))
     related)))

(defn p15-s23-c11-mir-safe-flat-records
  [records allowed-keys]
  (when (and (vector? records) (<= (count records) 32)
             (every? map? records))
    (mapv #(p15-s23-c11-mir-safe-flat-record % allowed-keys) records)))

(defn p15-s23-c11-mir-diagnostic-record-from-components
  [id stage severity source-path subject facts profile target redactions]
  (let [source-path (p15-s23-c11-mir-safe-source-path source-path)
        subject (if (map? subject) subject {})
        primary-artifact
        (p15-s23-c11-mir-semantic-anchor subject facts)
        span (p15-s23-c11-mir-span-with-source source-path subject)
        core-node-id
        (p15-s23-c11-mir-safe-diagnostic-scalar
         (or (:core-node-id subject)
             (:op-id subject)
             (:operation-id subject)
             :not-applicable))
        mir-operation-id
        (p15-s23-c11-mir-safe-diagnostic-scalar
         (or (:operation-id subject)
             (:op-id subject)
             (:core-node-id subject)
             :not-applicable))
        semantic-origin-id
        (p15-s23-c11-mir-safe-diagnostic-scalar
         (or (get-in subject [:source :origin-id])
             (:origin-id subject)
             :not-applicable))
        origin-chain
        (p15-s23-c11-mir-safe-origin-chain
         (or (:origin-chain subject)
             (get-in subject [:source :origin-chain])
             (when (not= :not-applicable semantic-origin-id)
               [semantic-origin-id])))
        generated-origin
        (first (get-in subject [:source :generated-origin]))
        producer-operation-id
        (when (map? generated-origin)
          (:producer-operation-id generated-origin))
        context-core-id
        (:checked-core-artifact-id
         *p15-s23-c11-mir-diagnostic-context*)
        context-mir-id (:mir-id *p15-s23-c11-mir-diagnostic-context*)
        related
        (if (and (true? (get-in subject [:source :generated?]))
                 (some? producer-operation-id))
          [{:role :generated-by
            :span span
            :syntax-id
            (p15-s23-c11-mir-safe-diagnostic-scalar
             (get-in subject [:source :enclosing-syntax-origin-id]))
            :core-node-id
            (p15-s23-c11-mir-safe-diagnostic-scalar producer-operation-id)
            :mir-operation-id
            (p15-s23-c11-mir-safe-diagnostic-scalar producer-operation-id)
            :origin-id semantic-origin-id
            :artifact
            (if (and (string? context-core-id)
                     (re-matches #"sha256:[0-9a-f]{64}" context-core-id))
              context-core-id
              primary-artifact)}]
          [])
        involved-artifacts
        (vec
         (distinct
          (filter #(and (string? %)
                        (re-matches #"sha256:[0-9a-f]{64}" %))
                  [primary-artifact context-core-id context-mir-id])))
        base
        {:artifact :gravity/diagnostic
         :rule id
         :severity severity
         :stage stage
         :message-key
         (keyword "diagnostic"
                  (str/lower-case (str/replace id #"_" "-")))
         :primary
         {:span span
          :syntax-id
          (p15-s23-c11-mir-safe-diagnostic-scalar
          (or (:syntax-id subject)
              (get-in subject [:source :enclosing-syntax-origin-id])
              semantic-origin-id))
          :core-node-id core-node-id
          :mir-operation-id mir-operation-id
          :origin-id semantic-origin-id
          :artifact primary-artifact}
         :related related
         :origin-chain origin-chain
         :profile (if (keyword? profile) profile :hosted)
         :target (if (keyword? target) target :target-neutral)
         :involved-artifacts involved-artifacts
         :facts facts
         :remediation
         [{:kind :regenerate-verified-mir
           :from-stage :c10-safety-checked-core
           :required-evidence
           [:types :effects :effect-order :ownership :capabilities
            :safety :runtime-checks :origins :profile :target-request]}]
         :redactions (vec redactions)
         :lifecycle :active}
        diagnostic-id (c15-stable-diagnostic-id base)]
    (assoc base
           :diagnostic-id diagnostic-id
           :ordering-key [id stage primary-artifact diagnostic-id])))

(defn p15-s23-c11-mir-diagnostic-record
  [id source-path subject extra]
  (let [subject (if (map? subject) subject {})
        subject
        (cond-> subject
          (or (:operation-id subject) (:op-id subject))
          (assoc :operation-id (or (:operation-id subject)
                                   (:op-id subject))))
        extra
        (merge *p15-s23-c11-mir-diagnostic-context*
               (if (map? extra) extra {})
               (select-keys subject
                            [:module-id :function-id :block-id
                             :operation-id :op-id :value-id
                             :source-operation :syntax-id :origin-id]))
        stage (or (:stage (p15-s23-c11-mir-diagnostic-rule-contract id))
                  :c11-authenticated-mir)
        facts (p15-s23-c11-mir-safe-diagnostic-facts subject extra)
        target (let [candidate (or (:target-request subject)
                                   (:requested-target extra)
                                   (:target subject))]
                 (if (keyword? candidate) candidate :target-neutral))
        profile (let [candidate (or (:profile subject) (:profile extra))]
                  (if (keyword? candidate) candidate :hosted))
        host-redactions
        (vec
         (keep (fn [key]
                 (when-let [value (get extra key)]
                   {:kind key
                    :status :redacted
                    :digest
                    (if (and (string? value)
                             (re-matches #"sha256:[0-9a-f]{64}" value))
                      value
                      :not-retained)}))
               [:contained-host-error-hash]))]
    (p15-s23-c11-mir-diagnostic-record-from-components
     id stage
     (if (= :internal-error (:diagnostic-severity extra))
       :internal-error
       :error)
     source-path subject facts profile target
     (vec (concat
           [{:kind :host-exception-details
             :status :redacted
             :policy :allowlisted-semantic-facts-only}]
           host-redactions)))))