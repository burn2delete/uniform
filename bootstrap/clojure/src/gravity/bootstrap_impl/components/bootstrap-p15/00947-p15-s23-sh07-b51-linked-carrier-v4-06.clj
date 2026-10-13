
(defn- p15-s23-sh07-b51-v4-receipted-products->linked-products
  [products]
  (let [receipt (:source-read-receipt products)
        source-descriptors (:source-descriptors receipt)
        events (:events receipt)
        plans (:m2s products)
        source-ids (mapv #(get-in % [:compiler :source-content-hash]) plans)
        semantic-ids (mapv #(get-in % [:compiler :semantic-hash]) plans)
        emitter-source-id (first source-ids)
        emitter-semantic-id (first semantic-ids)
        plan-authority (:sh06-resolution-plan-authority products)
        source-semantic-carrier (:source-semantic-carrier products)
        content (select-keys receipt [:commit :tree])
        linked-products
        {:artifact :gravity/sh07-b51-linked-plan-products-v2
         :module-source-descriptors (:m0s products)
         :link-manifest (:m1 products)
         :module-plans plans
         :emitter-source-id emitter-source-id
         :emitter-semantic-id emitter-semantic-id
         :execution-bundle-core (:bundle-core products)
         :execution-bundle-core-id (:execution-bundle-core-id products)}]
    (when-not
     (and
      (p15-s23-sh07-b51-exact-keys?
       products p15-s23-sh07-b51-v9-receipted-product-keys)
      (p15-s23-sh07-b51-exact-keys?
       receipt p15-s23-sh07-b51-v9-source-receipt-keys)
      (= :gravity/sh07-b51-source-read-receipt-v3 (:schema receipt))
      (contains? #{:producer-census :independent-verifier-census :m}
                 (:role receipt))
      (vector? source-descriptors) (= 13 (count source-descriptors))
      (= (:source-descriptors-root receipt)
         (p15-s23-sh07-b51-canonical-id
          [:gravity/sh07-b51-source-descriptors-root-v1 source-descriptors]))
      (vector? events) (= 40 (count events))
      (= (:event-root receipt)
         (p15-s23-sh07-b51-canonical-id
          [:gravity/sh07-b51-source-event-root-v1 events]))
      (= {:acquire-count 13 :decode-count 13
          :event-count 40 :finalize-count 1}
         (:counters receipt))
      (true? (:finalized receipt))
      (vector? (:source-forms products))
      (= 7 (count (:source-forms products)))
      (vector? (:module-transactions products))
      (= 7 (count (:module-transactions products)))
      (vector? plans) (= 7 (count plans))
      (p15-s23-sh07-b51-digest-id? emitter-source-id)
      (p15-s23-sh07-b51-digest-id? emitter-semantic-id)
      (= (vec (repeat 7 emitter-source-id)) source-ids)
      (= (vec (repeat 7 emitter-semantic-id)) semantic-ids)
      (p15-s23-sh07-b51-resolution-plan-authority-valid? plan-authority)
      (= content (:content plan-authority))
      (= (nth source-descriptors 12 nil)
         (:source-descriptor plan-authority))
      (p15-s23-sh07-b51-source-semantic-carrier-valid?
       content receipt (:module-transactions products)
       source-semantic-carrier)
      (p15-s23-sh07-b51-replay-returned-source-authority-events!
       (:m0s products) (:m1 products) plan-authority receipt)
      (p15-s23-sh07-b51-exact-keys?
       linked-products p15-s23-sh07-b51-v4-linked-product-keys))
      (throw (ex-info "invalid Attempt32 receipted product boundary"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-linked-carrier})))
    linked-products))

(defn- p15-s23-sh07-b51-v4-normalize-products [products]
  (cond
    (p15-s23-sh07-b51-exact-keys?
     products p15-s23-sh07-b51-v4-linked-product-keys)
    products

    (p15-s23-sh07-b51-exact-keys?
     products p15-s23-sh07-b51-v9-receipted-product-keys)
    (p15-s23-sh07-b51-v4-receipted-products->linked-products products)

    :else
    (throw (ex-info "invalid Attempt32 linked product shape"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :unauthenticated-b51-linked-carrier}))))

(defn- p15-s23-sh07-b51-v9-certificate-shell? [certificate]
  (and (contains? certificate :semantic-witness)
       (contains? (:inputs certificate) :lowering-product-id)
       (= :gravity/sh07-b51-authentic-lowering-witness-v1
          (get-in certificate [:semantic-witness :schema]))
       (p15-s23-sh07-b51-digest-id?
        (get-in certificate [:semantic-witness :witness-id]))
       (p15-s23-sh07-b51-digest-id?
        (get-in certificate [:inputs :lowering-product-id]))))

(defn p15-s23-sh07-b51-build-linked-carrier-v4-candidate
  [products certificates]
  (let [receipted-e3-products?
        (p15-s23-sh07-b51-exact-keys?
         products p15-s23-sh07-b51-v9-receipted-product-keys)
        products (p15-s23-sh07-b51-v4-normalize-products products)]
    (when-not
     (and (p15-s23-sh07-b51-v4-products-closed-joins? products)
          (or (not receipted-e3-products?)
              (every? p15-s23-sh07-b51-v9-certificate-shell? certificates))
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
       :predecessor-executable-descriptor-id
       p15-s23-sh07-b51-v4-predecessor-id
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
       :compile-census-certificates certificates))))

(defn- p15-s23-sh07-b51-v4-host-pin-map []
  {:expected-m3-id p15-s23-sh07-b51-expected-m3-id
   :expected-certificate-a-id p15-s23-sh07-b51-expected-certificate-a-id
   :expected-certificate-b-id p15-s23-sh07-b51-expected-certificate-b-id
   :expected-semantic-root p15-s23-sh07-b51-expected-semantic-root})

(defn- p15-s23-sh07-b51-load-local-authority-v4 []
  (let [pin-map (p15-s23-sh07-b51-v4-host-pin-map)
        carrier p15-s23-sh07-b51-authoritative-linked-carrier-value-v4]
    (when (and (every? p15-s23-sh07-b51-digest-id? (vals pin-map))
               (map? carrier)
               (every? p15-s23-sh07-b51-v9-certificate-shell?
                       (:compile-census-certificates carrier)))
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

(defn p15-s23-sh07-b51-current-authoritative-linked-products-v4 []
  (if-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
    (:products authority)
    (throw (ex-info "Attempt32 authority is not frozen"
                    {:id "L2-UNKNOWN-CORE-FORM"
                     :reason :provisional-no-authority
                     :result-committed? false
                     :output-committed? false}))))

(defn- p15-s23-sh07-b51-v4-bounded-carrier-equal? [supplied current]
  (= supplied current))

(defn p15-s23-sh07-b51-linked-carrier-v4-authorized? [carrier]
  (try
    (boolean
     (and (p15-s23-sh07-b51-v4-carrier-preflight? carrier)
          (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
            (and (p15-s23-sh07-b51-v4-bounded-carrier-equal?
                  carrier (:carrier authority))
                 (p15-s23-sh07-b51-linked-carrier-v4-valid-after-preflight?
                  carrier)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (try
    (when-let [authority (p15-s23-sh07-b51-load-local-authority-v4)]
      (let [current (:carrier authority)]
        (and (identical? carrier current)
             (= descriptors (:module-source-descriptors current))
             (= manifest (:link-manifest current))
             (= bundle (:execution-bundle current))
             (= plans (mapv :plan (:module-plan-carriers current))))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

;; Compatibility for the unchanged Stage2 internal-builtin component.  The
;; value accepted here is carrier-v4; carrier-v3 domains always reject.
(defn p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
  [carrier descriptors manifest plans bundle]
  (p15-s23-sh07-b51-linked-carrier-v4-runtime-products-match?
   carrier descriptors manifest plans bundle))

(def ^:private p15-s23-sh07-b51-v4-preflight-placeholder
  :gravity/bounded-carrier-preflight-placeholder)
