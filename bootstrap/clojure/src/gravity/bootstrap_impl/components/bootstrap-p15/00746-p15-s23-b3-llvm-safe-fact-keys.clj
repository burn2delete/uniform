

(def p15-s23-b3-llvm-safe-fact-keys
  #{:missing-fact :operation-id :opcode :source-operation :observed-type
    :expected-type :requested-target :target-triple :tool-step
    :exit-code :expected-exit-code :stdout-byte-count :stderr-byte-count
    :stdout-hash :stderr-hash :timed-out? :output-collision?
    :expected-hash :observed-hash :observed-format :observed-architecture
    :c11-mir-id :b3-source-content-hash :bounded-reason
    :native-access-enabled? :rename-return-code :captured-errno
    :expected-file-count :observed-file-count :logical-path
    :maximum-byte-count :observed-byte-count :observed-mode
    :carrier :reason :observed-nodes :observed-depth :maximum-nodes
    :maximum-depth :maximum-width :source-content-hash
    :expected-source-content-hash :observed-source-content-hash
    :expected-source-bytes :observed-source-bytes})

(defn p15-s23-b3-llvm-safe-fact-value
  [key value]
  (let [sha256?
        (fn [candidate]
          (and (string? candidate)
               (re-matches #"sha256:[0-9a-f]{64}" candidate)))
        bounded-count?
        (fn [candidate]
          (and (integer? candidate) (<= 0 candidate 2147483647)))]
    (cond
      (contains? #{:missing-fact :opcode :source-operation
                   :observed-type :expected-type :requested-target
                   :bounded-reason :tool-step :observed-format
                   :observed-architecture}
                 key)
      (if (keyword? value) value :redacted)

      (contains? #{:stdout-hash :stderr-hash :expected-hash
                   :observed-hash :c11-mir-id
                   :b3-source-content-hash}
                 key)
      (if (or (sha256? value) (= :unavailable value)) value :redacted)

      (= :operation-id key)
      (if (sha256? value) value :redacted)

      (= :target-triple key)
      (if (= value (:target-triple p15-s23-b3-llvm-policy))
        value :redacted)

      (= :logical-path key)
      (if (contains? #{"program.ll" "program.o" "program"
                       "manifest.edn" "provenance.edn"
                       "conformance.edn"}
                     value)
        value :redacted)

      (= :observed-mode key)
      (if (contains? #{"0644" "0755"} value) value :redacted)

      (contains? #{:timed-out? :output-collision?
                   :native-access-enabled?}
                 key)
      (if (boolean? value) value :redacted)

      (contains? #{:exit-code :expected-exit-code :rename-return-code}
                 key)
      (if (and (integer? value) (<= -2147483648 value 2147483647))
        value :redacted)

      (= :captured-errno key)
      (if (and (integer? value) (<= 0 value 4096)) value :redacted)

      (contains? #{:stdout-byte-count :stderr-byte-count
                   :expected-file-count :observed-file-count
                   :maximum-byte-count :observed-byte-count}
                 key)
      (if (bounded-count? value) value :redacted)

      :else :redacted)))

(defn p15-s23-b3-llvm-safe-facts
  [facts]
  (let [safe
        (into (sorted-map)
              (keep (fn [key]
                      (when (contains? facts key)
                        [key (p15-s23-b3-llvm-safe-fact-value
                              key (get facts key))])))
              p15-s23-b3-llvm-safe-fact-keys)]
    (if (seq safe) safe {:missing-fact :bounded-llvm-failure})))

(defn p15-s23-b3-llvm-diagnostic-record
  [id source-path subject facts]
  (let [id (if (contains? p15-s23-b3-llvm-diagnostic-rules id)
             id "B3-MANIFEST")
        source-path (p15-s23-c11-mir-safe-source-path source-path)
        safe-facts (p15-s23-b3-llvm-safe-facts facts)
        artifact-anchor
        (or (when (and (map? subject)
                       (string? (:artifact-id subject))
                       (re-matches #"sha256:[0-9a-f]{64}"
                                   (:artifact-id subject)))
              (:artifact-id subject))
            (p15-s23-c11-mir-digest
             {:kind :gravity/bounded-llvm-diagnostic
              :rule id :facts safe-facts}))
        span (p15-s23-c11-mir-span-with-source source-path subject)
        primary
        {:span span
         :syntax-id (p15-s23-c11-mir-safe-diagnostic-scalar
                     (or (:syntax-id subject) :not-applicable))
         :core-node-id (p15-s23-c11-mir-safe-diagnostic-scalar
                        (or (:op-id subject)
                            (:operation-id facts) :not-applicable))
         :mir-operation-id (p15-s23-c11-mir-safe-diagnostic-scalar
                            (or (:op-id subject)
                                (:operation-id facts) :not-applicable))
         :origin-id (p15-s23-c11-mir-safe-diagnostic-scalar
                     (or (get-in subject [:source :origin-id])
                         :not-applicable))
         :artifact artifact-anchor}
        base
        {:artifact :gravity/diagnostic
         :rule id
         :severity :error
         :stage (p15-s23-b3-llvm-diagnostic-stage id)
         :message-key
         (keyword "diagnostic" (str/lower-case id))
         :primary primary
         :related []
         :origin-chain
         (if (= :not-applicable (:origin-id primary))
           [] [(:origin-id primary)])
         :profile :hosted
         :target :llvm-x86_64-linux
         :involved-artifacts [artifact-anchor]
         :facts safe-facts
         :remediation
         [{:kind :repair-bounded-llvm-input
           :from-stage :verified-c11-mir
           :required-evidence
           [:authenticated-c11-replay :bounded-b3-lowering
            :toolchain-verification :differential-execution]}]
         :redactions
         [{:kind :host-and-tool-details
           :status :redacted
           :policy :hashes-and-bounded-counts-only}]
         :lifecycle :active}
        diagnostic-id (c15-stable-diagnostic-id base)]
    (assoc base
           :diagnostic-id diagnostic-id
           :ordering-key
           [id (p15-s23-b3-llvm-diagnostic-stage id)
            artifact-anchor diagnostic-id])))

(defn p15-s23-b3-llvm-throw-record!
  [record]
  (let [id (:rule record)
        message (p15-s23-b3-llvm-diagnostic-message id)]
    (throw (ex-info message
                    (merge record
                           {:id id :message message
                            :bootstrap-stage :stage0
                            :source-span (get-in record [:primary :span])
                            :missing-fact
                            (get-in record [:facts :missing-fact])
                            :fallback-status :rejected})))))

(defn p15-s23-b3-llvm-fail!
  [id source-path subject facts]
  (p15-s23-b3-llvm-throw-record!
   (p15-s23-b3-llvm-diagnostic-record
    id source-path subject facts)))

(declare p15-s23-stage2-c13-c14-b1-packet-from-c11!
         p15-s23-stage2-c13-c14-b1-verification-report
         p15-s23-stage2-c13-c14-b1-verify!
         p15-s23-stage2-c13-c14-b1-authentic?
         p15-s23-c13-c14-b1-sidecar-evidence!)