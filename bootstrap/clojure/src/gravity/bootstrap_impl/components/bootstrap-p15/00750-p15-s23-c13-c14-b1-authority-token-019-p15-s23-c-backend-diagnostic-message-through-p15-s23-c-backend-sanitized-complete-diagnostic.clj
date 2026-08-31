(let [p15-s23-c13-c14-b1-authority-token (nth __gravity_bootstrap_lexical_values_126914 0)]
(defn p15-s23-c-backend-diagnostic-message
  [id]
  (get
   {"C13-CONTRACT" "C13 optimization pass contract is invalid"
    "C13-PRESERVE" "C13 optimization preservation evidence is incomplete"
    "C13-INVALIDATE" "C13 invalidation ledger is incomplete"
    "C13-PROOF" "C13 optimization proof closure is incomplete"
    "C13-CHECK-ELISION" "C13 runtime check accounting is incomplete"
    "C13-EFFECT" "C13 effect ordering preservation failed"
    "C13-SAFETY" "C13 safety preservation failed"
    "C13-DOMAIN" "C13 domain anchor preservation failed"
    "C13-NONDETERMINISM" "C13 deterministic replay failed"
    "C13-VERIFY" "C13 optimized MIR verification failed"
    "C14-INPUT" "C14 lowering input is unverified or stale"
    "C14-PROFILE" "C14 profile is ineligible for the C backend"
    "C14-TARGET" "C14 hosted-C17 target contract is incomplete"
    "C14-ABI" "C14 ABI or layout contract is incomplete"
    "C14-RUNTIME" "C14 runtime contract is incomplete"
    "C14-PROVIDER" "C14 provider contract is incomplete"
    "C14-PROOF-METADATA" "C14 target metadata lacks proof authority"
    "C14-CAPABILITY" "C14 effect or capability authority is incomplete"
    "C14-UNSUPPORTED" "C14 input is outside the bounded C lowering surface"
    "C14-MANIFEST" "C14 C lowering manifest is incomplete"
    "B1-INPUT" "C backend input is unverified or incomplete"
    "B1-PROFILE" "B1 profile contract is ineligible"
    "B1-TARGET" "B1 C target or backend manifest is incomplete"
    "B1-ABI" "B1 ABI contract is incomplete"
    "B1-RUNTIME" "B1 runtime contract is incomplete"
    "B1-PROOF" "B1 proof closure is incomplete"
    "B1-CAPABILITY" "B1 authority closure is incomplete"
    "B1-UNSUPPORTED" "verified MIR is outside the bounded C slice"
    "B1-METADATA" "B1 metadata or provenance is incomplete"
    "B2-DIALECT" "B2 C dialect selection is invalid"
    "B2-UB" "B2 C lowering would introduce undefined behavior"
    "B2-ABI" "B2 ABI or layout contract is incomplete"
    "B2-POINTER" "B2 pointer provenance is incomplete"
    "B2-NUMERIC" "B2 numeric lowering is unsafe or unsupported"
    "B2-RUNTIME" "B2 runtime helper contract is incomplete"
    "B2-FFI" "B2 FFI lowering is unsupported"
    "B2-MMIO" "B2 MMIO lowering is unsupported"
    "B2-MANIFEST" "B2 C artifact manifest is incomplete"
    "B13-SCHEMA" "C artifact manifest schema is incomplete"
    "B13-HASH" "emitted C artifact hash did not verify"
    "B13-PROVENANCE" "C artifact provenance is incomplete"
    "B13-SOURCEMAP" "C artifact source map is incomplete"
    "B13-EVIDENCE" "C artifact safety or proof evidence is incomplete"
    "B13-TARGET" "C artifact target or ABI evidence is incomplete"
    "B13-CONFORMANCE" "C artifact conformance evidence is incomplete"
    "B13-REPRODUCIBILITY" "C artifact reproducibility evidence is incomplete"
    "B13-RELEASE" "bounded C artifact is not release eligible"
    "B13-GRAPH" "C artifact graph is incomplete"
    "B14-COVERAGE" "C backend fixture coverage is incomplete"
    "B14-TARGET" "pinned C target is unavailable"
    "B14-POSITIVE" "valid C fixture failed lowering or execution"
    "B14-NEGATIVE" "invalid C fixture produced the wrong result"
    "B14-DIFFERENTIAL" "C process result differs from reference"
    "B14-METADATA" "C backend metadata preservation failed"
    "B14-ARTIFACT" "C backend artifact manifest validation failed"
    "B14-NONDETERMINISM" "C backend nondeterminism is unrecorded"
    "B14-SKIP" "C target skip is unsupported"
    "B14-EVIDENCE" "C backend conformance evidence is incomplete"}
   id "bounded C backend failure"))

(def p15-s23-c-backend-safe-fact-keys
  (conj (disj p15-s23-b3-llvm-safe-fact-keys
              :b3-source-content-hash)
        :dialect :helper :c-source-content-hash :b1-source-content-hash
        :b2-source-content-hash :semantic-result :source-target
        :requested-target-argument :provider-return-code
        :expected-byte-count :expected-mode
        :cleanup-complete? :residue-possible?
        :primary-failure-rule :primary-diagnostic-id))

(defn p15-s23-c-backend-safe-facts
  [facts]
  (let [safe
        (into
         (sorted-map)
         (keep
          (fn [key]
            (when (contains? facts key)
              [key
               (cond
                 (= :logical-path key)
                 (if (contains?
                      #{"program.c" "program.h" "program.o" "program"
                        "manifest.edn" "provenance.edn"
                        "conformance.edn"}
                      (get facts key))
                   (get facts key)
                   :redacted)

                 (contains? p15-s23-b3-llvm-safe-fact-keys key)
                 (p15-s23-b3-llvm-safe-fact-value key (get facts key))
                 :else
                 (p15-s23-c11-mir-safe-diagnostic-scalar
                  (get facts key)))])))
         p15-s23-c-backend-safe-fact-keys)]
    (if (seq safe) safe {:missing-fact :bounded-c-failure})))

(defn p15-s23-c-backend-diagnostic-record
  [id source-path subject facts]
  (let [id (if (contains? p15-s23-c-backend-diagnostic-rules id)
             id "B2-MANIFEST")
        source-path (p15-s23-c11-mir-safe-source-path source-path)
        subject (if (map? subject) subject {})
        safe-facts
        (p15-s23-c-backend-safe-facts
         (merge {:dialect :hosted-c17 :helper :none} facts))
        artifact-anchor
        (or (when (and (string? (:artifact-id subject))
                       (re-matches #"sha256:[0-9a-f]{64}"
                                   (:artifact-id subject)))
              (:artifact-id subject))
            (p15-s23-c11-mir-digest
             {:kind :gravity/bounded-c-diagnostic
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
         :rule id :severity :error
         :stage (p15-s23-c-backend-diagnostic-stage id)
         :message-key (keyword "diagnostic" (str/lower-case id))
         :primary primary :related []
         :origin-chain (if (= :not-applicable (:origin-id primary))
                         [] [(:origin-id primary)])
         :profile :hosted :target :c
         :dialect :hosted-c17 :helper :none
         :involved-artifacts [artifact-anchor]
         :facts safe-facts
         :remediation
         [{:kind :repair-bounded-c-input
           :from-stage :verified-c11-mir
           :required-evidence
           [:authenticated-c11-replay
            :bounded-hosted-c17-reconstruction
            :content-hash-and-provenance]}]
         :redactions
         [{:kind :host-and-tool-details :status :redacted
           :policy :hashes-and-bounded-counts-only}]
         :lifecycle :active}
        diagnostic-id (c15-stable-diagnostic-id base)]
    (assoc base :diagnostic-id diagnostic-id
           :ordering-key [id (:stage base) artifact-anchor diagnostic-id])))

(defn p15-s23-c-backend-throw-record!
  [record]
  (let [id (:rule record)
        message (p15-s23-c-backend-diagnostic-message id)]
    (throw
     (ex-info
      message
      (merge record
             {:id id :message message :bootstrap-stage :stage0
              :source-span (get-in record [:primary :span])
              :missing-fact (get-in record [:facts :missing-fact])
              :fallback-status :rejected})))))

(defn p15-s23-c-backend-fail!
  [id source-path subject facts]
  (p15-s23-c-backend-throw-record!
   (p15-s23-c-backend-diagnostic-record id source-path subject facts)))

(defn- p15-s23-c-backend-sanitized-complete-diagnostic
  [data]
  (when (and (map? data)
             (contains? p15-s23-c-backend-diagnostic-rules (:id data))
             (= (:id data) (:rule data))
             (map? (:primary data)) (map? (:facts data)))
    (let [primary (:primary data)
          source-path (or (get-in primary [:span :source]) "<b2-c>")
          subject {:artifact-id (:artifact primary)
                   :syntax-id (:syntax-id primary)
                   :op-id (:mir-operation-id primary)
                   :source-span (:span primary)
                   :source {:origin-id (:origin-id primary)}}
          rebuilt (p15-s23-c-backend-diagnostic-record
                   (:id data) source-path subject (:facts data))
          required (set c15-diagnostic-required-fields)]
      (when (and (every? (set (keys data)) required)
                 (= rebuilt (select-keys data (keys rebuilt))))
        rebuilt)))))
