
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
          witness (:semantic-witness certificate)
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
               (canonical-hash
                [:gravity/sh07-b51-authentic-lowering-witness-v1
                 (dissoc witness :witness-id)]))
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
            (certificate-profile-within-bounds?
             (value-profile (dissoc certificate :semantic-witness)))
            (semantic-witness-profile-within-bounds? (value-profile witness))
            (= (:certificate-id certificate)
               (canonical-hash (dissoc certificate :certificate-id))))))
    (catch Throwable _ false)))

(defn checker-output-valid?
  "Validate an accepted checker output and both governed full transcripts.
  This predicate recomputes transcript record roots and compact certificate
  identities but never rescans source or M2."
  [output]
  (try
    (let [certificate (:certificate output)
          lowering-product (:lowering-product output)
          raw-evidence (:raw-source-transcript-evidence output)
          m2-evidence (:m2-transcript-evidence output)
          valid-transcript?
          (fn [evidence raw?]
            (let [records (:records evidence)
                  descriptor (:descriptor evidence)
                  root-domain (if raw?
                                :gravity/sh07-b51-raw-source-census-record-root-v1
                                :gravity/sh07-b51-m2-census-record-root-v1)
                  expected-root
                  (canonical-hash
                   {:domain root-domain :schema-version 1
                    :checker-algorithm-role :source-first-exhaustive
                    :records records})
                  expected-artifact-id
                  (canonical-hash
                   {:domain
                    :gravity/sh07-b51-census-transcript-artifact-id-v1
                    :schema-version 1
                    :artifact (:artifact descriptor)
                    :checker (:checker certificate)
                    :inputs (:inputs certificate)
                    :node-count (count records)
                    :row-count (:row-count descriptor)
                    :record-root expected-root})
                  record-keys (if raw? raw-record-keys m2-record-keys)]
              (and (exact-keys? evidence
                                #{:artifact :schema-version
                                  :checker-algorithm-role :descriptor :records})
                   (vector? records)
                   (= 1 (:schema-version evidence))
                   (= :source-first-exhaustive
                      (:checker-algorithm-role evidence))
                   (every? #(exact-keys? % record-keys) records)
                   (= (vec (range (count records)))
                      (mapv :node-ordinal records))
                   (= expected-root (:record-root descriptor))
                   (= expected-artifact-id (:artifact-id descriptor))
                   (= (count records) (:node-count descriptor))
                   (= (count (filter :semantic-row records))
                      (:row-count descriptor))
                   (= descriptor
                      (if raw?
                        (:raw-source-transcript certificate)
                        (:m2-transcript certificate))))))]
      (boolean
       (and (exact-keys? output
                         #{:artifact :schema-version :status :certificate
                           :lowering-product
                           :raw-source-transcript-evidence
                           :m2-transcript-evidence :commitment-evidence})
            (= :gravity/sh07-b51-producer-census-checker-output-v1
               (:artifact output))
            (= 1 (:schema-version output))
            (= :accepted (:status output))
            (= (get-in certificate [:inputs :lowering-product-id])
               (:product-id lowering-product))
            (certificate-valid? certificate)
            (valid-transcript? raw-evidence true)
            (valid-transcript? m2-evidence false))))
    (catch Throwable _ false)))
