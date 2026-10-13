
(defn checker-output-valid?
  "Validate an accepted checker output and both governed full transcripts.
  This predicate recomputes transcript record roots and compact certificate
  identities but never rescans source or M2."
  [output]
  (try
    (let [certificate (:certificate output)
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
                           :raw-source-transcript-evidence
                           :m2-transcript-evidence :commitment-evidence})
            (= :gravity/sh07-b51-producer-census-checker-output-v1
               (:artifact output))
            (= 1 (:schema-version output))
            (= :accepted (:status output))
            (certificate-valid? certificate)
            (valid-transcript? raw-evidence true)
            (valid-transcript? m2-evidence false))))
    (catch Throwable _ false)))

(defn- produce-accepted [input]
  (let [{:keys [indexes certificate-inputs]} (validate-envelope! input)
        raw (scan-raw-source (:source-forms input) (:m0s input))
        _ (require! (= indexes (:indexes raw)) :source-index-stability)
        m2 (scan-m2 (:m2s input) indexes)
        call-evidence (validate-call-parity! raw m2 (:m0s input) (:m1 input))
        builtin-occurrences (validate-builtin-parity! raw m2 (:m0s input))
        provenance-occurrences
        (validate-provenance! m2 (:m0s input) (:m1 input))
        reachable (reachable-functions (:bundle-core input) (:records m2))
        all-functions (set (keys (:owner-by-function indexes)))
        _ (require! (set/subset? reachable all-functions)
                    :reachability-unknown-function)
        rows (semantic-category-rows (:m0s input) (:m1 input) (:m2s input)
                                     (:bundle-core input) (:records m2)
                                     reachable)
        products (category-products rows)
        commitments (mapv :commitment products)
        physical (physical-census (:m0s input) (:m1 input) (:m2s input)
                                  raw m2 reachable rows)
        normalized-id (normalized-census-id physical commitments)
        external (external-commitment (:manifest-id (:m1 input))
                                      (:source-external call-evidence)
                                      (:m1-external call-evidence))
        builtin (builtin-commitments (:m0s input) (:m2s input)
                                     builtin-occurrences)
        provenance-commitment-value
        (provenance-commitment (:m0s input) (:m1 input) (:m2s input)
                               provenance-occurrences)
        claim {:domain :gravity/sh07-b51-common-census-claim-v1
               :normalized-census-id normalized-id
               :external-bijection-commitment (:id external)
               :builtin-parity-commitments (mapv :id builtin)
               :provenance-completeness-commitment
               (:id provenance-commitment-value)
               :physical-census physical
               :category-schedule category-schedule
               :category-commitments commitments}
        assumptions (accepted-assumptions)
        result (result-map physical normalized-id)
        raw-evidence
        (transcript-evidence
         :gravity/sh07-b51-raw-source-census-transcript-v1
         (:records raw) (:row-count raw) (:checker input) certificate-inputs)
        m2-evidence
        (transcript-evidence
         :gravity/sh07-b51-m2-census-transcript-v1
         (:records m2) (:row-count m2) (:checker input) certificate-inputs)
        provenance
        (assoc (:provenance input)
               :raw-transcript-artifact-id
               (get-in raw-evidence [:descriptor :artifact-id])
               :m2-transcript-artifact-id
               (get-in m2-evidence [:descriptor :artifact-id]))
        claim-id (common-claim-id (:architecture-report-sha256 input)
                                  certificate-inputs assumptions claim)
        evidence-id
        (evidence-root (:checker input) certificate-inputs
                       (:descriptor raw-evidence) (:descriptor m2-evidence)
                       result (:reproduction-command-id provenance))
        certificate-base
        {:artifact :gravity/sh07-b51-compile-census-certificate
         :domain :gravity/sh07-b51-producer-census-certificate-v1
         :schema-version 1
         :status :accepted
         :role :producer-census
         :architecture-report-sha256 (:architecture-report-sha256 input)
         :claim-id claim-id
         :claim claim
         :assumptions assumptions
         :checker (:checker input)
         :inputs certificate-inputs
         :result result
         :provenance provenance
         :raw-source-transcript (:descriptor raw-evidence)
         :m2-transcript (:descriptor m2-evidence)
         :profile-bounds profile-bounds
         :evidence-root evidence-id}
        certificate (assoc certificate-base :certificate-id
                           (canonical-hash certificate-base))
        output
        {:artifact :gravity/sh07-b51-producer-census-checker-output-v1
         :schema-version 1
         :status :accepted
         :certificate certificate
         :raw-source-transcript-evidence raw-evidence
         :m2-transcript-evidence m2-evidence
         :commitment-evidence
         {:external external
          :builtin builtin
          :provenance provenance-commitment-value
          :category-products products}}]
    (require! (certificate-valid? certificate) :constructed-certificate-invalid)
    (require! (checker-output-valid? output) :constructed-output-invalid)
    output))

(defn produce-certificate
  "Produce checker A's certificate and full transcript evidence from one
  immutable closed snapshot envelope.  Failure returns only closed rejection
  evidence and never exposes a partial accepted identity."
  [input]
  (try
    (produce-accepted input)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (let [{:keys [reason context]} (ex-data error)]
        (rejection reason context)))
    (catch Throwable _
      (rejection :checker-failure {}))))
