
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
                             (:bundle-core-id inputs)]))
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
            (certificate-profile-within? (value-profile certificate)))))
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
            raw (:raw-source-transcript-artifact output)
            m2 (:m2-transcript-artifact output)]
        (boolean
         (and (exact-keys? output accepted-output-keys)
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

(defn- valid-accepted-output? [envelope output]
  (try
    (let [certificate (:certificate output)
          raw (:raw-source-transcript-artifact output)
          m2 (:m2-transcript-artifact output)
          inputs (check-and-derive-inputs envelope)
          checker (:checker envelope)
          claim (:claim certificate)
          result (:result certificate)
          certificate-base (dissoc certificate :certificate-id)]
      (and (exact-keys? output accepted-output-keys)
           (= :accepted (:status output))
           (certificate-valid? certificate)
           (exact-keys? certificate certificate-keys)
           (= :gravity/sh07-b51-compile-census-certificate
              (:artifact certificate))
           (= :gravity/sh07-b51-independent-verifier-census-certificate-v1
              (:domain certificate))
           (= [1 :accepted :independent-verifier-census]
              [(:schema-version certificate) (:status certificate)
               (:role certificate)])
           (= (:architecture-report-sha256 envelope)
              (:architecture-report-sha256 certificate)
              (:architecture-report-sha256 inputs))
           (= checker (:checker certificate))
           (= inputs (:inputs certificate))
           (exact-keys? claim claim-keys)
           (exact-keys? (:assumptions certificate) assumptions-keys)
           (= (assumptions) (:assumptions certificate))
           (exact-keys? result result-keys)
           (= :accepted (:status result))
           (every? true? ((juxt :external-bijection-complete
                                :builtin-parity-complete
                                :provenance-complete :ordering-complete
                                :reachability-complete) result))
           (= (:normalized-census-id claim) (:normalized-census-id result))
           (exact-keys? (:physical-census claim) physical-census-keys)
           (= (:definition-count result)
              (get-in claim [:physical-census :physical-definition-count]))
           (= (:local-count result)
              (get-in claim
                      [:physical-census
                       :physical-local-runtime-callsite-count]))
           (= (:external-count result)
              (get-in claim
                      [:physical-census
                       :physical-external-runtime-callsite-count]))
           (= (:builtin-count result)
              (get-in claim
                      [:physical-census
                       :physical-referenced-builtin-occurrence-count]))
           (= (:semantic-row-count result)
              (get-in claim [:physical-census :semantic-row-count])
              (reduce + (map :row-count (:category-commitments claim))))
           (= category-schedule (:category-schedule claim))
           (= categories (mapv :category (:category-commitments claim)))
           (= (:normalized-census-id claim)
              (h {:domain :gravity/sh07-b51-normalized-census-id-v1
                  :schema-version 1
                  :physical-census (:physical-census claim)
                  :ordered-category-row-roots
                  (mapv :root-digest (:category-commitments claim))
                  :category-row-counts
                  (mapv :row-count (:category-commitments claim))}))
           (valid-full-transcript?
            raw :gravity/sh07-b51-raw-source-census-transcript-v1
            :gravity/sh07-b51-raw-source-census-record-root-v1 checker inputs
            source-record-keys)
           (valid-full-transcript?
            m2 :gravity/sh07-b51-m2-census-transcript-v1
            :gravity/sh07-b51-m2-census-record-root-v1 checker inputs
            m2-record-keys)
           (= (transcript-descriptor raw)
              (:raw-source-transcript certificate))
           (= (transcript-descriptor m2) (:m2-transcript certificate))
           (exact-keys? (:provenance certificate) certificate-provenance-keys)
           (= (:artifact-id raw)
              (get-in certificate
                      [:provenance :raw-transcript-artifact-id]))
           (= (:artifact-id m2)
              (get-in certificate [:provenance :m2-transcript-artifact-id]))
           (= (:claim-id certificate)
              (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
                  :schema-version 1
                  :architecture-report-sha256
                  (:architecture-report-sha256 certificate)
                  :inputs inputs :assumptions (:assumptions certificate)
                  :claim claim :profile-bounds profile-bounds}))
           (= (:evidence-root certificate)
              (h {:domain
                  :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
                  :schema-version 1 :checker checker :inputs inputs
                  :raw-source-transcript (transcript-descriptor raw)
                  :m2-transcript (transcript-descriptor m2) :result result
                  :reproduction-command-id
                  (get-in certificate
                          [:provenance :reproduction-command-id])
                  :profile-bounds profile-bounds}))
           (= (:certificate-id certificate) (h certificate-base))
           (= profile-bounds (:profile-bounds certificate))
           (certificate-profile-within? (value-profile certificate))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- contains-certificate-id-key? [value]
  (cond
    (map? value) (or (contains? value :certificate-id)
                     (some contains-certificate-id-key? (vals value)))
    (coll? value) (some contains-certificate-id-key? value)
    :else false))
