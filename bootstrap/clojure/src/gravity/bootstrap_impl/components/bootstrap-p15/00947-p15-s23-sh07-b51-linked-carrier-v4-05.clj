
(defn- p15-s23-sh07-b51-v4-certificate-inputs-match-products?
  [certificate products]
  (let [inputs (:inputs certificate)
        descriptors (:module-source-descriptors products)
        plans (:module-plans products)]
    (= {:predecessor-executable-descriptor-id
        p15-s23-sh07-b51-v4-predecessor-id
        :module-source-identities (mapv :source-sha256 descriptors)
        :module-source-byte-counts (mapv :source-byte-count descriptors)
        :emitter-source-id (:emitter-source-id products)
        :emitter-semantic-id (:emitter-semantic-id products)
        :m0-ids (mapv :module-source-id descriptors)
        :m1-id (get-in products [:link-manifest :manifest-id])
        :m2-ids (mapv :plan-id plans)
        :bundle-core-id (:execution-bundle-core-id products)}
       (select-keys
        inputs
        [:predecessor-executable-descriptor-id :module-source-identities
         :module-source-byte-counts :emitter-source-id :emitter-semantic-id
         :m0-ids :m1-id :m2-ids :bundle-core-id]))))

(defn- p15-s23-sh07-b51-v4-physical-census-match-products?
  [certificate products]
  (let [rows (get-in certificate [:claim :physical-census :module-censuses])
        descriptors (:module-source-descriptors products)
        plans (:module-plans products)]
    (and (= 7 (count rows) (count descriptors) (count plans))
         (every?
          true?
          (map
           (fn [row descriptor plan]
             (= {:module-ordinal (:module-ordinal descriptor)
                 :module (:module descriptor)
                 :logical-source-path (:logical-source-path descriptor)
                 :owner (:owner descriptor)
                 :source-byte-count (:source-byte-count descriptor)
                 :source-sha256 (:source-sha256 descriptor)
                 :module-source-id (:module-source-id descriptor)
                 :plan-id (:plan-id plan)
                 :core-binding-count (:core-binding-count descriptor)
                 :lexical-binding-count (:lexical-binding-count descriptor)
                 :external-binding-count (count (:external-bindings descriptor))
                 :aggregate-binding-count (:aggregate-binding-count descriptor)}
                (select-keys
                 row
                 [:module-ordinal :module :logical-source-path :owner
                  :source-byte-count :source-sha256 :module-source-id :plan-id
                  :core-binding-count :lexical-binding-count
                  :external-binding-count :aggregate-binding-count])))
           rows descriptors plans)))))

(defn p15-s23-sh07-b51-build-semantic-manifest-v4
  [products execution-bundle certificates]
  (let [claim (:claim (first certificates))
        commitments (:category-commitments claim)]
    {:domain :gravity/sh07-b51-v18-linked-semantic-manifest-v4
     :schema-version 4
     :predecessor-executable-descriptor-id p15-s23-sh07-b51-v4-predecessor-id
     :module-source-ids
     (mapv :module-source-id (:module-source-descriptors products))
     :link-manifest-id (get-in products [:link-manifest :manifest-id])
     :module-plan-ids (mapv :plan-id (:module-plans products))
     :execution-bundle-id (:bundle-id execution-bundle)
     :compile-census-certificate-ids (mapv :certificate-id certificates)
     :schedule (:category-schedule claim)
     :category-commitments commitments
     :logical-public-surface p15-s23-sh07-b51-v4-logical-public-surface
     :inherited-contract-value-hashes
     p15-s23-sh07-b51-v4-inherited-contract-hashes
     :total-row-count (reduce + 0 (map :row-count commitments))}))

(defn- p15-s23-sh07-b51-v4-build-execution-bundle
  [products certificates]
  (p15-s23-sh07-b51-build-execution-bundle-v2
   products (mapv :certificate-id certificates)))

(defn p15-s23-sh07-b51-build-linked-carrier-v4-candidate
  [products certificates]
  (when-not (and (p15-s23-sh07-b51-v4-products-closed-joins? products)
                 (p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
                  certificates)
                 (every? #(p15-s23-sh07-b51-v4-certificate-inputs-match-products?
                            % products)
                         certificates)
                 (every? #(p15-s23-sh07-b51-v4-physical-census-match-products?
                            % products)
                         certificates))
    (throw (ex-info "invalid Attempt32 carrier candidate inputs"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-linked-carrier})))
  (let [execution-bundle
        (p15-s23-sh07-b51-v4-build-execution-bundle products certificates)
        semantic
        (p15-s23-sh07-b51-build-semantic-manifest-v4
         products execution-bundle certificates)
        root-preimage
        {:domain :gravity/sh07-b51-v18-linked-semantic-root-v4
         :schema-version 4 :semantic-manifest semantic}
        physical (get-in certificates [0 :claim :physical-census])]
    (array-map
     :domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v4
     :schema-version 4
     :predecessor-executable-descriptor-id p15-s23-sh07-b51-v4-predecessor-id
     :semantic-manifest semantic
     :semantic-root-hash (p15-s23-sh07-b51-canonical-id root-preimage)
     :module-source-descriptors (:module-source-descriptors products)
     :link-manifest (:link-manifest products)
     :module-plan-carriers
     (mapv (fn [descriptor plan]
             {:module-ordinal (:module-ordinal descriptor)
              :module-source-id (:module-source-id descriptor)
              :plan-id (:plan-id plan) :plan plan})
           (:module-source-descriptors products) (:module-plans products))
     :execution-bundle execution-bundle
     :physical-census physical
     :compile-census-certificates certificates)))

(defn- p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
  [carrier]
  (let [certificates (:compile-census-certificates carrier)
          certificate-a (first certificates)
          execution-bundle (:execution-bundle carrier)
          plans (mapv :plan (:module-plan-carriers carrier))
          products
          {:artifact :gravity/sh07-b51-linked-plan-products-v2
           :module-source-descriptors (:module-source-descriptors carrier)
           :link-manifest (:link-manifest carrier)
           :module-plans plans
           :emitter-source-id (get-in certificate-a [:inputs :emitter-source-id])
           :emitter-semantic-id
           (get-in certificate-a [:inputs :emitter-semantic-id])
           :execution-bundle-core
           (assoc (select-keys execution-bundle
                               [:manifest-id :module-plans :routes
                                :public-roots])
                  :domain :gravity/sh07-b51-execution-bundle-core-id-v1
                  :schema-version 1)
           :execution-bundle-core-id (:bundle-core-id execution-bundle)}
          expected-bundle
          (p15-s23-sh07-b51-v4-build-execution-bundle products certificates)
          expected-semantic
          (p15-s23-sh07-b51-build-semantic-manifest-v4
           products expected-bundle certificates)
          expected-root
          (p15-s23-sh07-b51-canonical-id
           {:domain :gravity/sh07-b51-v18-linked-semantic-root-v4
            :schema-version 4 :semantic-manifest expected-semantic})]
        (and (p15-s23-sh07-b51-exact-keys?
            carrier p15-s23-sh07-b51-v4-carrier-keys)
           (= :gravity/sh07-b51-v18-linked-executable-closure-carrier-v4
              (:domain carrier))
           (= 4 (:schema-version carrier))
           (= p15-s23-sh07-b51-v4-predecessor-id
              (:predecessor-executable-descriptor-id carrier))
           (p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
            certificates)
           (p15-s23-sh07-b51-v4-products-closed-joins? products)
           (every? #(p15-s23-sh07-b51-v4-certificate-inputs-match-products?
                      % products)
                   certificates)
           (every? #(p15-s23-sh07-b51-v4-physical-census-match-products?
                      % products)
                   certificates)
           (= (:physical-census carrier)
              (get-in certificate-a [:claim :physical-census]))
           (= expected-bundle execution-bundle)
           (= expected-semantic (:semantic-manifest carrier))
           (= p15-s23-sh07-b51-v4-semantic-keys
              (set (keys (:semantic-manifest carrier))))
           (= expected-root (:semantic-root-hash carrier))
           (= (mapv (fn [descriptor plan]
                      {:module-ordinal (:module-ordinal descriptor)
                       :module-source-id (:module-source-id descriptor)
                       :plan-id (:plan-id plan) :plan plan})
                    (:module-source-descriptors products) plans)
              (:module-plan-carriers carrier)))))

(defn p15-s23-sh07-b51-linked-carrier-v4-valid? [carrier]
  (try
    (and (p15-s23-sh07-b51-v4-carrier-preflight? carrier)
         (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
          carrier))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-v4-candidate-pin-map [carrier]
  {:expected-m3-id (get-in carrier [:execution-bundle :bundle-id])
   :expected-certificate-a-id
   (get-in carrier [:compile-census-certificates 0 :certificate-id])
   :expected-certificate-b-id
   (get-in carrier [:compile-census-certificates 1 :certificate-id])
   :expected-semantic-root (:semantic-root-hash carrier)})

(defn p15-s23-sh07-b51-v4-candidate-matches-pin-map?
  [carrier pin-map]
  (and (= #{:expected-m3-id :expected-certificate-a-id
            :expected-certificate-b-id :expected-semantic-root}
          (set (keys pin-map)))
       (every? p15-s23-sh07-b51-digest-id? (vals pin-map))
       (= pin-map (p15-s23-sh07-b51-v4-candidate-pin-map carrier))))

(defn p15-s23-sh07-b51-attempt32-authority-status []
  {:status :provisional-no-authority
   :host-pin-boundary-id p15-s23-sh07-b51-host-pin-boundary-id
   :runtime-token-boundary-id p15-s23-sh07-b51-runtime-token-boundary-id
   :pins-frozen? false
   :certificates-frozen? false})

(defn p15-s23-sh07-b51-attempt30-authority-status []
  (p15-s23-sh07-b51-attempt32-authority-status))

(def ^:private p15-s23-sh07-b51-authoritative-linked-carrier-value-v4
  :gravity/provisional-no-authority)

(defn- p15-s23-sh07-b51-v4-host-pin-map []
  {:expected-m3-id p15-s23-sh07-b51-expected-m3-id
   :expected-certificate-a-id p15-s23-sh07-b51-expected-certificate-a-id
   :expected-certificate-b-id p15-s23-sh07-b51-expected-certificate-b-id
   :expected-semantic-root p15-s23-sh07-b51-expected-semantic-root})

(defn- p15-s23-sh07-b51-load-local-authority-v4 []
  (let [pin-map (p15-s23-sh07-b51-v4-host-pin-map)
        carrier p15-s23-sh07-b51-authoritative-linked-carrier-value-v4]
    (when (and (every? p15-s23-sh07-b51-digest-id? (vals pin-map))
               (map? carrier))
      (let [base-products (p15-s23-sh07-b51-current-bundle-core-products)
            reconstructed-carrier
            (p15-s23-sh07-b51-build-linked-carrier-v4-candidate
             base-products
             (:compile-census-certificates carrier))
            products
            (assoc base-products :execution-bundle (:execution-bundle carrier))]
        (when (and (= carrier reconstructed-carrier)
                   (p15-s23-sh07-b51-linked-carrier-v4-valid? carrier)
                   (p15-s23-sh07-b51-v4-candidate-matches-pin-map?
                    carrier pin-map))
          {:carrier carrier :products products})))))

(defn p15-s23-sh07-b51-current-linked-carrier-v4 []
  (if-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
    (:carrier authority)
    (throw (ex-info "Attempt32 authority is not frozen"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :provisional-no-authority
                     :result-committed? false
                     :output-committed? false}))))
