
(defn- valid-accepted-output? [envelope output]
  (try
    (let [certificate (:certificate output)
          lowering-product (:lowering-product output)
          raw (:raw-source-transcript-artifact output)
          m2 (:m2-transcript-artifact output)
          inputs (assoc (check-and-derive-inputs envelope)
                        :lowering-product-id (:product-id lowering-product))
          checker (:checker envelope)
          claim (:claim certificate)
          result (:result certificate)
          certificate-base (dissoc certificate :certificate-id)]
      (and (exact-keys? output accepted-output-keys)
           (= (get-in certificate [:inputs :lowering-product-id])
              (:product-id lowering-product))
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
           (certificate-profile-within?
            (value-profile (dissoc certificate :semantic-witness)))
           (semantic-witness-profile-within?
            (value-profile (:semantic-witness certificate)))))
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

(defn- valid-rejection-output? [output]
  (let [evidence (:rejection-evidence output)]
    (and (exact-keys? output rejection-output-keys)
         (= :rejected (:status output))
         (exact-keys? evidence rejection-keys)
         (= :gravity/sh07-b51-census-checker-rejection-evidence-v1
            (:artifact evidence))
         (= [1 :rejected :independent-verifier-census
             :m2-first-independent-exhaustive]
            [(:schema-version evidence) (:status evidence) (:role evidence)
             (:algorithm-role evidence)])
         (keyword? (:reason evidence))
         (= (:rejection-id evidence) (h (dissoc evidence :rejection-id)))
         (not (contains-certificate-id-key? output)))))

(defn valid-output? [envelope output]
  (case (:status output)
    :accepted (boolean (valid-accepted-output? envelope output))
    :rejected (boolean (valid-rejection-output? output))
    false))
