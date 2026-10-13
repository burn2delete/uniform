
(defn- transcript-within! [artifact]
  (ensure!
   (transcript-counts-within?
    {:node-count (:node-count artifact)
     :record-count (count (:records artifact))
     :call-row-count (:row-count artifact)
     :byte-count (utf8-count (pr-str artifact))})
   :offline-transcript-bound)
  artifact)

(defn- assumptions []
  {:sha256-collision-resistance :assumed
   :canonical-reader-hash-id
   :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
   :source-emitter-bytes-immutable true
   :checker-closures-independent true
   :host-pin-boundary-id
   (h {:domain :gravity/sh07-b51-host-pin-boundary-id-v1
       :schema-version 1 :component :p15-s23-sh07-b51-plan-linker
       :pin-key-names
       [:expected-m3-id :expected-certificate-a-id
        :expected-certificate-b-id :expected-semantic-root]
       :caller-supplied false})
   :runtime-token-boundary-id
   (h {:domain :gravity/sh07-b51-runtime-token-boundary-id-v1
       :schema-version 1 :component :p15-s23-sh07-b51-runtime-context
       :token-kind :private-object-identity :caller-supplied false})})

(defn- accepted-products [envelope]
  ;; Deliberately M2-first: no source form is traversed until the complete M2
  ;; instruction space has been scanned under the closed rules above.
  (let [legacy-inputs (check-and-derive-inputs envelope)
        {:keys [lowering-product semantic-witness measurement-count]}
        (materialize/build-checker-precertificate envelope)
        _ (ensure! (= 34 measurement-count) :fixed-fuel-measurement-count)
        inputs (assoc legacy-inputs
                      :lowering-product-id (:product-id lowering-product))
        m0s (:m0s envelope) m1 (:m1 envelope) m2s (:m2s envelope)
        bundle-core (:bundle-core envelope) checker (:checker envelope)
        m2-scan (scan-m2 m2s)
        source-scan (scan-source (:source-forms envelope) m0s)
        _ (ensure! (= (:owner-by-function m2-scan)
                      (:owner-by-function source-scan))
                   :source-m2-owner-map)
        _ (verify-source-m2-call-parity! source-scan m2-scan)
        _ (verify-definitions! source-scan m0s m2s)
        external (verify-external-bijection! source-scan m0s m1 m2-scan)
        builtin-commitments (verify-builtin-parity! m2-scan m0s inputs)
        provenance-commitment
        (provenance-commitment! m2-scan m0s m1 inputs)
        reachable (reachable-functions! bundle-core m2-scan)
        rows (category-rows m0s m1 m2s bundle-core m2-scan reachable)
        commitments (build-category-commitments rows)
        physical (physical-census m0s m2s m2-scan reachable rows)
        normalized-id
        (h {:domain :gravity/sh07-b51-normalized-census-id-v1
            :schema-version 1 :physical-census physical
            :ordered-category-row-roots (mapv :root-digest commitments)
            :category-row-counts (mapv :row-count commitments)})
        external-commitment
        (h {:domain
            :gravity/sh07-b51-external-call-bijection-commitment-v1
            :schema-version 1 :m1-id (:m1-id inputs)
            :physical-external-count (count (:source-rows external))
            :ordered-source-external-root
            (h {:domain :gravity/sh07-b51-ordered-source-external-root-v1
                :schema-version 1 :records (:source-rows external)})
            :ordered-m1-root
            (h {:domain :gravity/sh07-b51-ordered-m1-external-root-v1
                :schema-version 1 :records (:m1-rows external)})
            :bijection-complete true})
        claim {:domain :gravity/sh07-b51-common-census-claim-v1
               :normalized-census-id normalized-id
               :external-bijection-commitment external-commitment
               :builtin-parity-commitments builtin-commitments
               :provenance-completeness-commitment provenance-commitment
               :physical-census physical :category-schedule category-schedule
               :category-commitments commitments}
        assumptions-value (assumptions)
        raw-calls (count (filter #(contains? #{:local :external :builtin}
                                             (:call-classification %))
                                 (:records source-scan)))
        m2-calls (count (:calls m2-scan))
        raw-artifact
        (transcript-within!
         (transcript-artifact
          :gravity/sh07-b51-raw-source-census-transcript-v1
          :gravity/sh07-b51-raw-source-census-record-root-v1 checker inputs
          (:records source-scan) raw-calls))
        m2-artifact
        (transcript-within!
         (transcript-artifact
          :gravity/sh07-b51-m2-census-transcript-v1
          :gravity/sh07-b51-m2-census-record-root-v1 checker inputs
          (:records m2-scan) m2-calls))
        raw-descriptor (transcript-descriptor raw-artifact)
        m2-descriptor (transcript-descriptor m2-artifact)
        result {:status :accepted :normalized-census-id normalized-id
                :definition-count (:physical-definition-count physical)
                :local-count (:physical-local-runtime-callsite-count physical)
                :external-count
                (:physical-external-runtime-callsite-count physical)
                :builtin-count
                (:physical-referenced-builtin-occurrence-count physical)
                :semantic-row-count (:semantic-row-count physical)
                :external-bijection-complete true :builtin-parity-complete true
                :provenance-complete true :ordering-complete true
                :reachability-complete true}
        provenance
        (assoc (:provenance envelope)
               :raw-transcript-artifact-id (:artifact-id raw-artifact)
               :m2-transcript-artifact-id (:artifact-id m2-artifact))
        claim-id
        (h {:domain :gravity/sh07-b51-common-census-claim-id-v1
            :schema-version 1
            :architecture-report-sha256
            (:architecture-report-sha256 envelope)
            :inputs inputs :assumptions assumptions-value :claim claim
            :profile-bounds profile-bounds})
        evidence-root
        (h {:domain
            :gravity/sh07-b51-independent-verifier-census-evidence-root-v1
            :schema-version 1 :checker checker :inputs inputs
            :raw-source-transcript raw-descriptor
            :m2-transcript m2-descriptor :result result
            :reproduction-command-id (:reproduction-command-id provenance)
            :profile-bounds profile-bounds})
        certificate-base
        {:artifact :gravity/sh07-b51-compile-census-certificate
         :domain :gravity/sh07-b51-independent-verifier-census-certificate-v1
         :schema-version 1 :status :accepted
         :role :independent-verifier-census
         :architecture-report-sha256
         (:architecture-report-sha256 envelope)
         :claim-id claim-id :claim claim :assumptions assumptions-value
         :checker checker :inputs inputs :result result
         :semantic-witness semantic-witness
         :provenance provenance :raw-source-transcript raw-descriptor
         :m2-transcript m2-descriptor :profile-bounds profile-bounds
         :evidence-root evidence-root}
        certificate (assoc certificate-base :certificate-id
                           (h certificate-base))]
    (ensure! (and (certificate-profile-within?
                   (value-profile (dissoc certificate :semantic-witness)))
                  (semantic-witness-profile-within?
                   (value-profile semantic-witness)))
             :certificate-profile-bound)
    {:status :accepted :certificate certificate
     :lowering-product lowering-product
     :raw-source-transcript-artifact raw-artifact
     :m2-transcript-artifact m2-artifact}))

(defn- rejection [reason]
  (let [base {:artifact :gravity/sh07-b51-census-checker-rejection-evidence-v1
              :schema-version 1 :status :rejected
              :role :independent-verifier-census
              :algorithm-role :m2-first-independent-exhaustive
              :reason reason}]
    {:status :rejected
     :rejection-evidence (assoc base :rejection-id (h base))}))

(defn produce-certificate [envelope]
  (try
    (accepted-products envelope)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (rejection (or (:reason (ex-data error)) :checker-b-failure)))
    (catch Throwable _
      (rejection :checker-b-failure))))

(defn- dense-records? [records key-name]
  (= (vec (range (count records))) (mapv key-name records)))

(defn- valid-full-transcript?
  [artifact expected-kind record-domain checker inputs record-keys]
  (and (exact-keys? artifact full-transcript-keys)
       (= expected-kind (:artifact artifact))
       (= 1 (:schema-version artifact))
       (= checker (:checker artifact)) (= inputs (:inputs artifact))
       (vector? (:records artifact))
       (= (:node-count artifact) (count (:records artifact)))
       (every? #(exact-keys? % record-keys) (:records artifact))
       (dense-records? (:records artifact) :record-ordinal)
       (= (:record-root artifact)
          (h {:domain record-domain :schema-version 1
              :checker-algorithm-role (:algorithm-role checker)
              :records (:records artifact)}))
       (= (:artifact-id artifact)
          (h {:domain :gravity/sh07-b51-census-transcript-artifact-id-v1
              :schema-version 1 :artifact expected-kind :checker checker
              :inputs inputs :node-count (:node-count artifact)
              :row-count (:row-count artifact)
              :record-root (:record-root artifact)}))
       (transcript-counts-within?
        {:node-count (:node-count artifact)
         :record-count (count (:records artifact))
         :call-row-count (:row-count artifact)
         :byte-count (utf8-count (pr-str artifact))})))
