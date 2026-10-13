
(defn certificate-valid?
  "Validate checker B's compact certificate without invoking either offline
  scanner.  Immutable host pins remain a separate admission obligation."
  [certificate]
  (try
    (let [claim (:claim certificate)
          assumptions-value (:assumptions certificate)
          checker (:checker certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          witness (:semantic-witness certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          physical (:physical-census claim)
          commitments (:category-commitments claim)]
      (boolean
       (and (exact-keys? certificate certificate-keys)
            (= :gravity/sh07-b51-compile-census-certificate
               (:artifact certificate))
            (= :gravity/sh07-b51-independent-verifier-census-certificate-v1
               (:domain certificate))
            (= [1 :accepted :independent-verifier-census]
               [(:schema-version certificate) (:status certificate)
                (:role certificate)])
            (exact-keys?
             witness
             #{:schema :content :operation-manifest :operation-schedule-id
               :fuel-receipts :l1 :l2 :c2 :c6 :boot2 :boot3 :boot4
               :witness-id})
            (= :gravity/sh07-b51-authentic-lowering-witness-v1
               (:schema witness))
            (= {:commit (:snapshot-commit inputs)
                :tree (:snapshot-tree inputs)}
               (:content witness))
            (= (:witness-id witness)
               (h [:gravity/sh07-b51-authentic-lowering-witness-v1
                   (dissoc witness :witness-id)]))
            (digest-id? (:architecture-report-sha256 certificate))
            (exact-keys? checker checker-keys)
            (= :m2-first-independent-exhaustive (:algorithm-role checker))
            (exact-keys? inputs input-identity-keys)
            (= (:architecture-report-sha256 certificate)
               (:architecture-report-sha256 inputs))
            (= 7 (count (:module-source-identities inputs))
               (count (:module-source-byte-counts inputs))
               (count (:m0-ids inputs)) (count (:m2-ids inputs)))
            (every? digest-id?
                    (concat (:module-source-identities inputs)
                            (:m0-ids inputs) (:m2-ids inputs)
                            [(:architecture-report-sha256 inputs)
                             (:predecessor-executable-descriptor-id inputs)
                             (:emitter-source-id inputs)
                             (:emitter-semantic-id inputs) (:m1-id inputs)
                             (:bundle-core-id inputs)
                             (:lowering-product-id inputs)]))
            (exact-keys? claim claim-keys)
            (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
            (exact-keys? assumptions-value assumptions-keys)
            (= (assumptions) assumptions-value)
            (exact-keys? result result-keys)
            (= :accepted (:status result))
            (every? true?
                    ((juxt :external-bijection-complete
                           :builtin-parity-complete :provenance-complete
                           :ordering-complete :reachability-complete) result))
            (exact-keys? physical physical-census-keys)
            (vector? commitments) (= 7 (count commitments))
            (= categories (mapv :category commitments))
            (= category-schedule (:category-schedule claim))
            (= (:normalized-census-id claim) (:normalized-census-id result)
               (h {:domain :gravity/sh07-b51-normalized-census-id-v1
                   :schema-version 1 :physical-census physical
                   :ordered-category-row-roots (mapv :root-digest commitments)
                   :category-row-counts (mapv :row-count commitments)}))
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
            (exact-keys? provenance certificate-provenance-keys)
            (exact-keys? raw transcript-descriptor-keys)
            (exact-keys? m2 transcript-descriptor-keys)
            (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
            (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
            (= (:claim-id certificate)
               (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
                   :schema-version 1
                   :architecture-report-sha256
                   (:architecture-report-sha256 certificate)
                   :inputs inputs :assumptions assumptions-value :claim claim
                   :profile-bounds profile-bounds}))
            (= (:evidence-root certificate)
               (h {:domain
                   :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
                   :schema-version 1 :checker checker :inputs inputs
                   :raw-source-transcript raw :m2-transcript m2 :result result
                   :reproduction-command-id
                   (:reproduction-command-id provenance)
                   :profile-bounds profile-bounds}))
            (= profile-bounds (:profile-bounds certificate))
            (= (:certificate-id certificate)
               (h (dissoc certificate :certificate-id)))
            (certificate-profile-within?
             (value-profile (dissoc certificate :semantic-witness)))
            (semantic-witness-profile-within? (value-profile witness)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(declare valid-rejection-output?)

(defn checker-output-valid?
  "Validate checker B's closed accepted output and both governed transcripts,
  or its closed rejection evidence, without rescanning source or M2."
  [output]
  (try
    (case (:status output)
      :accepted
      (let [certificate (:certificate output)
            checker (:checker certificate)
            inputs (:inputs certificate)
            lowering-product (:lowering-product output)
            raw (:raw-source-transcript-artifact output)
            m2 (:m2-transcript-artifact output)]
        (boolean
         (and (exact-keys? output accepted-output-keys)
              (= (get-in certificate [:inputs :lowering-product-id])
                 (:product-id lowering-product))
              (certificate-valid? certificate)
              (valid-full-transcript?
               raw :gravity/sh07-b51-raw-source-census-transcript-v1
               :gravity/sh07-b51-raw-source-census-record-root-v1
               checker inputs source-record-keys)
              (valid-full-transcript?
               m2 :gravity/sh07-b51-m2-census-transcript-v1
               :gravity/sh07-b51-m2-census-record-root-v1
               checker inputs m2-record-keys)
              (= (transcript-descriptor raw)
                 (:raw-source-transcript certificate))
              (= (transcript-descriptor m2) (:m2-transcript certificate)))))
      :rejected (boolean (valid-rejection-output? output))
      false)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))
