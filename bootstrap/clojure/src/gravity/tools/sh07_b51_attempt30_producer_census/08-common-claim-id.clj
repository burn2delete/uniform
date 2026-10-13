
(defn- common-claim-id [architecture-report-sha256 inputs assumptions claim]
  (canonical-hash
   {:domain :gravity/sh07-b51-common-census-claim-id-v1
    :schema-version 1
    :architecture-report-sha256 architecture-report-sha256
    :inputs inputs
    :assumptions assumptions
    :claim claim
    :profile-bounds profile-bounds}))

(defn- evidence-root [checker inputs raw-descriptor m2-descriptor result
                      reproduction-command-id]
  (canonical-hash
   {:domain :gravity/sh07-b51-producer-census-evidence-root-v1
    :schema-version 1
    :checker checker
    :inputs inputs
    :raw-source-transcript raw-descriptor
    :m2-transcript m2-descriptor
    :result result
    :reproduction-command-id reproduction-command-id
    :profile-bounds profile-bounds}))

(defn- rejection [reason context]
  {:artifact :gravity/sh07-b51-census-checker-rejection-evidence-v1
   :schema-version 1
   :status :rejected
   :role :producer-census
   :checker-algorithm-role :source-first-exhaustive
   :reason (if (keyword? reason) reason :checker-failure)
   :context (if (map? context)
              (select-keys context
                           [:module-ordinal :caller :callee :ordinal
                            :edge-ordinal :root-ordinal :artifact :metrics])
              {})})

(defn- certificate-inputs-valid? [inputs]
  (and (exact-keys? inputs certificate-input-keys)
       (sha256-id? (:architecture-report-sha256 inputs))
       (git-id? (:snapshot-commit inputs))
       (git-id? (:snapshot-tree inputs))
       (sha256-id? (:predecessor-executable-descriptor-id inputs))
       (digest-vector? (:module-source-identities inputs) 7)
       (vector? (:module-source-byte-counts inputs))
       (= 7 (count (:module-source-byte-counts inputs)))
       (every? nonnegative-integer? (:module-source-byte-counts inputs))
       (sha256-id? (:emitter-source-id inputs))
       (sha256-id? (:emitter-semantic-id inputs))
       (digest-vector? (:m0-ids inputs) 7)
       (sha256-id? (:m1-id inputs))
       (digest-vector? (:m2-ids inputs) 7)
       (sha256-id? (:bundle-core-id inputs))))

(defn- checker-valid? [checker]
  (and (exact-keys? checker checker-keys)
       (= :source-first-exhaustive (:algorithm-role checker))
       (every? sha256-id?
               ((juxt :artifact-id :source-sha256 :semantic-id :executable-id
                      :dependency-closure-id) checker))))

(defn- transcript-descriptor-valid? [descriptor artifact]
  (and (exact-keys? descriptor transcript-descriptor-keys)
       (= artifact (:artifact descriptor))
       (= 1 (:schema-version descriptor))
       (nonnegative-integer? (:node-count descriptor))
       (nonnegative-integer? (:row-count descriptor))
       (<= (:row-count descriptor) (:node-count descriptor))
       (sha256-id? (:artifact-id descriptor))
       (sha256-id? (:record-root descriptor))))

(defn- category-commitments-valid? [commitments]
  (and (vector? commitments) (= 7 (count commitments))
       (= categories (mapv :category commitments))
       (every?
        #(and (exact-keys? %
                           #{:category :row-count :leaf-count
                             :fold-level-count :root-digest})
              (nonnegative-integer? (:row-count %))
              (pos-int? (:leaf-count %))
              (and (pos-int? (:fold-level-count %))
                   (<= (:fold-level-count %) 3))
              (sha256-id? (:root-digest %)))
        commitments)))

(defn- physical-census-valid? [physical]
  (and (exact-keys? physical physical-census-keys)
       (= 7 (:module-count physical))
       (vector? (:module-censuses physical))
       (= 7 (count (:module-censuses physical)))
       (= (vec (range 7))
          (mapv :module-ordinal (:module-censuses physical)))
       (every? #(exact-keys? % module-census-keys)
               (:module-censuses physical))
       (every? nonnegative-integer?
               ((juxt :physical-definition-count :reachable-definition-count
                      :physical-local-runtime-callsite-count
                      :reachable-local-runtime-callsite-count
                      :physical-external-runtime-callsite-count
                      :reachable-external-runtime-callsite-count
                      :physical-referenced-builtin-occurrence-count
                      :reachable-referenced-builtin-occurrence-count
                      :semantic-row-count :maximum-binding-occupancy
                      :binding-ceiling) physical))
       (= 2440 (:binding-ceiling physical))
       (<= (:maximum-binding-occupancy physical) 2440)
       (vector? (:module-binding-occupancies physical))
       (= 7 (count (:module-binding-occupancies physical)))
       (every? nonnegative-integer? (:module-binding-occupancies physical))))

(defn certificate-valid?
  "Validate the closed compact checker-A certificate without importing or
  invoking the offline scanners.  Host integration must additionally compare
  the certificate and its id with immutable expected literals."
  [certificate]
  (try
    (let [claim (:claim certificate)
          assumptions (:assumptions certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          physical (:physical-census claim)
          commitments (:category-commitments claim)
          normalized
          (when (and (physical-census-valid? physical)
                     (category-commitments-valid? commitments))
            (normalized-census-id physical commitments))
          expected-claim-id
          (when (and (certificate-inputs-valid? inputs)
                     (exact-keys? assumptions assumptions-keys)
                     (exact-keys? claim claim-keys))
            (common-claim-id (:architecture-report-sha256 certificate)
                             inputs assumptions claim))
          expected-evidence
          (when (and (checker-valid? (:checker certificate))
                     (transcript-descriptor-valid?
                      raw :gravity/sh07-b51-raw-source-census-transcript-v1)
                     (transcript-descriptor-valid?
                      m2 :gravity/sh07-b51-m2-census-transcript-v1)
                     (exact-keys? result result-keys)
                     (exact-keys? provenance certificate-provenance-keys))
            (evidence-root (:checker certificate) inputs raw m2 result
                           (:reproduction-command-id provenance)))]
      (boolean
       (and (exact-keys? certificate certificate-keys)
            (= :gravity/sh07-b51-compile-census-certificate
               (:artifact certificate))
            (= :gravity/sh07-b51-producer-census-certificate-v1
               (:domain certificate))
            (= 1 (:schema-version certificate))
            (= :accepted (:status certificate) (:status result))
            (= :producer-census (:role certificate))
            (sha256-id? (:architecture-report-sha256 certificate))
            (= (:architecture-report-sha256 certificate)
               (:architecture-report-sha256 inputs))
            (= :source-first-exhaustive
               (get-in certificate [:checker :algorithm-role]))
            (certificate-inputs-valid? inputs)
            (checker-valid? (:checker certificate))
            (physical-census-valid? physical)
            (category-commitments-valid? commitments)
            (= profile-bounds (:profile-bounds certificate))
            (= category-schedule (:category-schedule claim))
            (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
            (= normalized (:normalized-census-id claim)
               (:normalized-census-id result))
            (= expected-claim-id (:claim-id certificate))
            (= expected-evidence (:evidence-root certificate))
            (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
            (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
            (transcript-descriptor-valid?
             raw :gravity/sh07-b51-raw-source-census-transcript-v1)
            (transcript-descriptor-valid?
             m2 :gravity/sh07-b51-m2-census-transcript-v1)
            (sha256-id? (:external-bijection-commitment claim))
            (digest-vector? (:builtin-parity-commitments claim) 7)
            (sha256-id? (:provenance-completeness-commitment claim))
            (= (:definition-count result)
               (:physical-definition-count physical))
            (= (:local-count result)
               (:physical-local-runtime-callsite-count physical))
            (= (:external-count result)
               (:physical-external-runtime-callsite-count physical))
            (= (:builtin-count result)
               (:physical-referenced-builtin-occurrence-count physical))
            (= (:semantic-row-count result) (:semantic-row-count physical)
               (reduce + (map :row-count commitments)))
            (every? true? ((juxt :external-bijection-complete
                                 :builtin-parity-complete
                                 :provenance-complete :ordering-complete
                                 :reachability-complete) result))
            (= (accepted-assumptions) assumptions)
            (certificate-profile-within-bounds? (value-profile certificate))
            (= (:certificate-id certificate)
               (canonical-hash (dissoc certificate :certificate-id))))))
    (catch Throwable _ false)))
