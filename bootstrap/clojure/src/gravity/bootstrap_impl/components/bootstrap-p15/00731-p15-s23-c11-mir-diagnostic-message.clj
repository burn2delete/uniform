

(defn p15-s23-c11-mir-diagnostic-message
  [rule]
  (if (contains? (set c11-mir-diagnostic-ids) rule)
    (c11-mir-message rule)
    "authenticated checked-core validation failed"))

(defn p15-s23-c11-mir-throw-record!
  [record]
  (let [rule (:rule record)
        message (p15-s23-c11-mir-diagnostic-message rule)]
    (throw
     (ex-info
      message
      (merge record
             {:id rule
              :message message
              :bootstrap-stage :stage0
              :source-span (get-in record [:primary :span])
              :missing-fact (get-in record [:facts :missing-fact])
              :fallback-status :rejected})))))

(defn p15-s23-c11-mir-fail!
  [id source-path subject extra]
  (let [id (if (contains? p15-s23-c11-mir-upstream-diagnostic-rules id)
             id "C11-VERIFY")
        record (p15-s23-c11-mir-diagnostic-record
                id source-path subject extra)
        _ (p15-s23-c11-mir-throw-record! record)]))

(defn p15-s23-c11-mir-require-trusted-carrier!
  [source-path carrier value sorted-policy]
  (let [validation
        (p15-s23-trusted-carrier-validation
         value sorted-policy
         p15-s23-c11-mir-max-final-artifact-carrier-nodes
         p15-s23-c11-mir-max-carrier-depth
         p15-s23-c11-mir-max-final-artifact-carrier-nodes)]
    (when-not (= :passed (:status validation))
      (p15-s23-c11-mir-fail!
       (if (contains? #{:maximum-carrier-nodes
                        :maximum-carrier-depth
                        :maximum-carrier-width}
                      (:reason validation))
         "C11-VERIFY"
         "C11-MODULE")
       source-path {}
       (assoc
        (select-keys validation
                     [:reason :observed-nodes :observed-depth
                      :maximum-nodes :maximum-depth :maximum-width])
        :missing-fact :trusted-comparator-free-c11-carrier
        :carrier carrier)))
    validation))

(defn- p15-s23-c11-mir-sanitized-complete-diagnostic
  [data]
  (when (map? data)
    (let [rule (:rule data)
          contract (p15-s23-c11-mir-diagnostic-rule-contract rule)
          primary (:primary data)
          source-path
          (or (get-in primary [:span :source])
              (get-in primary [:span :file])
              "<c11-mir>")
          safe-primary
          (when (map? primary)
            (assoc
             (p15-s23-c11-mir-safe-flat-record
              (dissoc primary :span) p15-s23-c11-mir-primary-keys)
             :span
             (p15-s23-c11-mir-span-with-source
              source-path {:source-span (:span primary)})))
          safe-related
          (p15-s23-c11-mir-safe-related source-path (:related data))
          safe-origin-chain
          (p15-s23-c11-mir-safe-origin-chain (:origin-chain data))
          safe-involved
          (p15-s23-c11-mir-safe-diagnostic-scalar
           (:involved-artifacts data))
          safe-facts
          (when (map? (:facts data))
            (p15-s23-c11-mir-safe-diagnostic-facts {} (:facts data)))
          safe-remediation
          (p15-s23-c11-mir-safe-flat-records
           (:remediation data) p15-s23-c11-mir-remediation-keys)
          safe-redactions
          (p15-s23-c11-mir-safe-flat-records
           (:redactions data) p15-s23-c11-mir-redaction-keys)
          base
          {:artifact :gravity/diagnostic
           :diagnostic-id (:diagnostic-id data)
           :rule rule
           :severity (:severity data)
           :stage (:stage data)
           :message-key (:message-key data)
           :primary safe-primary
           :related safe-related
           :origin-chain safe-origin-chain
           :profile (:profile data)
           :target (:target data)
           :involved-artifacts safe-involved
           :facts safe-facts
           :remediation safe-remediation
           :redactions safe-redactions
           :lifecycle :active
           :ordering-key (:ordering-key data)}
          required (set c15-diagnostic-required-fields)
          primary-artifact (get-in safe-primary [:artifact])]
      (when
       (and contract
            (every? (set (keys data)) required)
            (= :gravity/diagnostic (:artifact data))
            (contains? #{:error :internal-error} (:severity data))
            (= :active (:lifecycle data))
            (= (:stage contract) (:stage data))
            (= rule (:id data))
            (keyword? (:message-key data))
            (keyword? (:profile data))
            (keyword? (:target data))
            (= p15-s23-c11-mir-primary-keys (set (keys primary)))
            (= primary safe-primary)
            (and (string? primary-artifact)
                 (re-matches #"sha256:[0-9a-f]{64}" primary-artifact))
            (= (:related data) safe-related)
            (= (:origin-chain data) safe-origin-chain)
            (= (:involved-artifacts data) safe-involved)
            (vector? safe-involved)
            (seq safe-involved)
            (every? #(and (string? %)
                          (re-matches #"sha256:[0-9a-f]{64}" %))
                    safe-involved)
            (= (:facts data) safe-facts)
            (map? safe-facts)
            (seq safe-facts)
            (= (:remediation data) safe-remediation)
            (vector? safe-remediation)
            (seq safe-remediation)
            (every? map? safe-remediation)
            (= (:redactions data) safe-redactions)
            (vector? safe-redactions)
            (seq safe-redactions)
            (every? map? safe-redactions)
            (= (:diagnostic-id data) (c15-stable-diagnostic-id base))
            (= [rule (:stage data) primary-artifact
                (:diagnostic-id data)]
               (:ordering-key data)))
        base))))

(defn p15-s23-c11-mir-containment-observation
  []
  {:kind :diagnostic-boundary-observation
   :status :recorded
   :observed-at :c11-authenticated-mir})

(defn p15-s23-trusted-diagnostic-data
  [data maximum-nodes maximum-depth]
  (when (and (map? data)
             (contains? p15-s23-trusted-carrier-map-classes
                        (.getName (class data)))
             (nil? (meta data))
             (<= (count data) maximum-nodes))
    (let [projection
          (reduce-kv
           (fn [state key value]
             (if (identical? key ::c11-upstream-diagnostic-owner)
               (-> state
                   (update :owner-count inc)
                   (update :owner-valid?
                           #(and %
                                 (some?
                                  *p15-s23-c11-upstream-diagnostic-owner*)
                                 (identical?
                                  *p15-s23-c11-upstream-diagnostic-owner*
                                  value)))
                   (update :entries conj [key nil]))
               (update state :entries conj [key value])))
           {:entries [] :owner-count 0 :owner-valid? true}
           data)
          owner-valid?
          (or (zero? (:owner-count projection))
              (and (= 1 (:owner-count projection))
                   (:owner-valid? projection)))
          validation
          (when owner-valid?
            (p15-s23-trusted-carrier-validation
             (:entries projection) :default-only
             maximum-nodes maximum-depth maximum-nodes))]
      (when (= :passed (:status validation)) data))))

(defn p15-s23-backend-trusted-exception-data
  [exception maximum-nodes maximum-depth]
  (when (instance? clojure.lang.ExceptionInfo exception)
    (p15-s23-trusted-diagnostic-data
     (ex-data exception) maximum-nodes maximum-depth)))

(defn p15-s23-c11-mir-contain-exception!
  [source-path boundary exception]
  (let [data
        (p15-s23-backend-trusted-exception-data
         exception p15-s23-c11-mir-max-final-artifact-carrier-nodes
         p15-s23-c11-mir-max-carrier-depth)
        complete (p15-s23-c11-mir-sanitized-complete-diagnostic data)]
    (if complete
      (let [observation (p15-s23-c11-mir-containment-observation)
            record
            (update complete :redactions
                    #(if (some #{observation} %)
                       %
                       (conj % observation)))]
        (p15-s23-c11-mir-throw-record! record))
      (p15-s23-c11-mir-fail!
       "C11-VERIFY" source-path {}
       {:missing-fact boundary
        :diagnostic-severity :internal-error
        :contained-host-error-hash
        (str "sha256:" (sha256-hex (.getName (class exception))))}))))