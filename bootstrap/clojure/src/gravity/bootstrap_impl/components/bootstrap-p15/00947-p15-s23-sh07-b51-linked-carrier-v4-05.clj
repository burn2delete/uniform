
(defn- p15-s23-sh07-b51-v4-products-closed-joins? [products]
  (let [descriptors (:module-source-descriptors products)
        manifest (:link-manifest products)
        plans (:module-plans products)
        core (p15-s23-sh07-b51-v4-bundle-core-preimage products)]
    (and (vector? descriptors) (= 7 (count descriptors))
         (= (vec (range 7)) (mapv :module-ordinal descriptors))
         (every? p15-s23-sh07-b51-digest-id?
                 (map :module-source-id descriptors))
         (vector? plans) (= 7 (count plans))
         (= (mapv :module-source-id descriptors)
            (mapv :sh07-b51-module-source-id plans))
         (= (mapv :plan-id plans) (mapv :plan-id (:module-plans core)))
         (= #{:artifact :schema-version :manifest-id :module-source-ids :edges}
            (set (keys manifest)))
         (= :gravity/sh07-b51-link-manifest-v1 (:artifact manifest))
         (= 1 (:schema-version manifest))
         (= (mapv :module-source-id descriptors)
            (:module-source-ids manifest))
         (= (:manifest-id manifest)
            (p15-s23-sh07-b51-manifest-id manifest))
         (vector? (:edges manifest))
         (= (vec (range (count (:edges manifest))))
            (mapv :ordinal (:edges manifest)))
         (every? #(p15-s23-sh07-b51-v4-declared-edge-valid?
                   descriptors %)
                 (:edges manifest))
         (= 8 (count (:public-roots core)))
         (= (:execution-bundle-core products) core)
         (= (:execution-bundle-core-id products)
            (p15-s23-sh07-b51-canonical-id core))
         (p15-s23-sh07-b51-execution-bundle-core-valid-structure?
          descriptors manifest plans core
          (:execution-bundle-core-id products)))))

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

(declare p15-s23-sh07-b51-build-linked-carrier-v4-candidate)

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

(def ^:private p15-s23-sh07-b51-v4-linked-product-keys
  #{:artifact :module-source-descriptors :link-manifest :module-plans
    :emitter-source-id :emitter-semantic-id :execution-bundle-core
    :execution-bundle-core-id})

(def ^:private p15-s23-sh07-b51-v9-receipted-product-keys
  #{:m0s :m1 :m2s :bundle-core :execution-bundle-core-id :source-forms
    :module-transactions :sh06-resolution-plan-authority
    :sh06-authority-carrier :source-semantic-carrier :source-read-receipt})

(def ^:private p15-s23-sh07-b51-v9-source-receipt-keys
  #{:schema :role :commit :tree :source-descriptors
    :source-descriptors-root :events :event-root :counters :finalized})
