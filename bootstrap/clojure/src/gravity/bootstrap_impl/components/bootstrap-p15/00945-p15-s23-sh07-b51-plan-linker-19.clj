
(defn- p15-s23-sh07-b51-execution-bundle-core-valid*
  [descriptors manifest plans bundle-core bundle-core-id]
  (let [plans-by-id (into {} (map (juxt :plan-id identity)) plans)
        edge-by-ordinal (into {} (map (juxt :ordinal identity))
                              (:edges manifest))
        module-plans (:module-plans bundle-core)
        routes (:routes bundle-core)
        roots (:public-roots bundle-core)]
    (and (p15-s23-sh07-b51-link-manifest-valid? descriptors manifest)
         (= 7 (count plans))
         (every? (fn [descriptor]
                   (let [plan (nth plans (:module-ordinal descriptor))]
                     (and (p15-s23-sh07-b51-plan-valid-structure?
                           descriptor manifest plan)
                          (p15-s23-sh07-b51-plan-links-valid-structure?
                           descriptor manifest plan))))
                 descriptors)
         (p15-s23-sh07-b51-exact-keys?
          bundle-core p15-s23-sh07-b51-bundle-core-keys)
         (= :gravity/sh07-b51-execution-bundle-core-id-v1
            (:domain bundle-core))
         (= 1 (:schema-version bundle-core))
         (= (:manifest-id manifest) (:manifest-id bundle-core))
         (= (vec (range 7)) (mapv :module-ordinal module-plans))
         (= (mapv (fn [descriptor plan]
                    {:module-ordinal (:module-ordinal descriptor)
                     :module-source-id (:module-source-id descriptor)
                     :plan-id (:plan-id plan)})
                  descriptors plans)
            module-plans)
         (= (vec (range (count routes))) (mapv :edge-ordinal routes))
         (= (count routes) (count (:edges manifest)))
         (every?
          (fn [route]
            (and (= #{:edge-ordinal :caller-plan-id :callee-plan-id}
                    (set (keys route)))
                 (let [edge (get edge-by-ordinal (:edge-ordinal route))
                       caller-plan (get plans-by-id (:caller-plan-id route))
                       callee-plan (get plans-by-id (:callee-plan-id route))]
                   (and edge caller-plan callee-plan
                        (= (:caller-module-source-id edge)
                           (:sh07-b51-module-source-id caller-plan))
                        (= (:callee-module-source-id edge)
                           (:sh07-b51-module-source-id callee-plan))))))
          routes)
         (= 8 (count roots))
         (= (vec (range 1 9)) (mapv :root-ordinal roots))
         (every?
          (fn [expected]
            (let [root (nth roots (dec (:root-ordinal expected)))
                  descriptor (nth descriptors (:module-ordinal expected))
                  plan (nth plans (:module-ordinal expected))
                  definition (get-in plan [:functions (:function expected)])]
              (and (= #{:root-ordinal :function :arity :plan-id}
                      (set (keys root)))
                   (= (select-keys expected [:root-ordinal :function :arity])
                      (select-keys root [:root-ordinal :function :arity]))
                   (= (:plan-id plan) (:plan-id root))
                   (= (:module-source-id descriptor)
                      (:sh07-b51-module-source-id plan))
                   (= (:arity expected) (:arity definition)))))
          p15-s23-sh07-b51-public-root-specs)
         (= bundle-core-id
            (p15-s23-sh07-b51-canonical-id bundle-core)))))

(defn- p15-s23-sh07-b51-execution-bundle-valid*
  [descriptors manifest plans bundle]
  (let [bundle-core
        (select-keys bundle
                     [:manifest-id :module-plans :routes :public-roots])
        bundle-core
        (assoc bundle-core
               :domain :gravity/sh07-b51-execution-bundle-core-id-v1
               :schema-version 1)
        certificate-ids (:compile-census-certificate-ids bundle)]
    (and (p15-s23-sh07-b51-exact-keys? bundle p15-s23-sh07-b51-m3-keys)
         (= :gravity/sh07-b51-execution-bundle-v2 (:artifact bundle))
         (= 2 (:schema-version bundle))
         (vector? certificate-ids)
         (= 2 (count certificate-ids))
         (every? p15-s23-sh07-b51-digest-id? certificate-ids)
         (= 2 (count (set certificate-ids)))
         (p15-s23-sh07-b51-execution-bundle-core-valid*
          descriptors manifest plans bundle-core (:bundle-core-id bundle))
         (= (:bundle-id bundle)
            (p15-s23-sh07-b51-canonical-id
             {:domain :gravity/sh07-b51-execution-bundle-id-v2
              :schema-version 1
              :bundle (dissoc bundle :bundle-id)})))))

(defn- p15-s23-sh07-b51-execution-bundle-core-valid-structure?
  [descriptors manifest plans bundle-core bundle-core-id]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-core-valid*
              descriptors manifest plans bundle-core bundle-core-id))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-execution-bundle-valid-structure?
  [descriptors manifest plans bundle]
  (try
    (boolean (p15-s23-sh07-b51-execution-bundle-valid*
              descriptors manifest plans bundle))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-build-execution-bundle-v2
  [products certificate-ids]
  (let [bundle-core (:execution-bundle-core products)
        base
        {:artifact :gravity/sh07-b51-execution-bundle-v2
         :schema-version 2
         :manifest-id (:manifest-id bundle-core)
         :module-plans (:module-plans bundle-core)
         :routes (:routes bundle-core)
         :public-roots (:public-roots bundle-core)
         :bundle-core-id (:execution-bundle-core-id products)
         :compile-census-certificate-ids certificate-ids}
        bundle
        (assoc base :bundle-id
               (p15-s23-sh07-b51-canonical-id
                {:domain :gravity/sh07-b51-execution-bundle-id-v2
                 :schema-version 1
                 :bundle base}))]
    (when-not (p15-s23-sh07-b51-execution-bundle-valid-structure?
               (:module-source-descriptors products)
               (:link-manifest products) (:module-plans products) bundle)
      (throw (ex-info "invalid SH-07 B51 M3-v2 construction"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link})))
    bundle))

(defn p15-s23-sh07-b51-current-bundle-core-products []
  (p15-s23-sh07-b51-build-execution-bundle))

(defn p15-s23-sh07-b51-plan-valid? [descriptor manifest plan]
  (try
    (let [products (p15-s23-sh07-b51-current-bundle-core-products)
          ordinal (:module-ordinal descriptor)]
      (and (integer? ordinal) (<= 0 ordinal 6)
           (= descriptor
              (nth (:module-source-descriptors products) ordinal nil))
           (= manifest (:link-manifest products))
           (= plan (nth (:module-plans products) ordinal nil))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-plan-links-valid? [descriptor manifest plan]
  (and (p15-s23-sh07-b51-plan-valid? descriptor manifest plan)
       (p15-s23-sh07-b51-plan-links-valid-structure?
        descriptor manifest plan)))

(defn p15-s23-sh07-b51-execution-bundle-valid?
  [descriptors manifest plans bundle]
  (try
    (let [products (p15-s23-sh07-b51-current-authoritative-linked-products-v4)]
      (and (= descriptors (:module-source-descriptors products))
           (= manifest (:link-manifest products))
           (= plans (:module-plans products))
           (= bundle (:execution-bundle products))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-execution-bundle-authorized?
  [descriptors manifest plans bundle]
  (and (p15-s23-sh07-b51-digest-id? p15-s23-sh07-b51-expected-m3-id)
       (= p15-s23-sh07-b51-expected-m3-id
          (:bundle-id bundle))
       (p15-s23-sh07-b51-execution-bundle-valid?
        descriptors manifest plans bundle)))

(defn p15-s23-sh07-b51-external-instruction
  [module caller callee arity callsite-path]
  (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
        manifest (:p15-s23-sh07-link-manifest module)
        rows (filter #(and (= (:module-source-id descriptor)
                             (:caller-module-source-id %))
                           (= caller (:caller-function %))
                           (= callee (:callee-function %))
                           (= arity (:arity %))
                           (= callsite-path (:callsite-path %)))
                     (:edges manifest))]
    (when-not (and (p15-s23-sh07-b51-module-source-valid? descriptor)
                   (= (:manifest-id manifest)
                      (p15-s23-sh07-b51-manifest-id manifest))
                   (= 1 (count rows)))
      (throw (ex-info "unauthenticated SH-07 B51 external call"
                      {:id "L2-UNKNOWN-CORE-FORM"
                       :reason :unauthenticated-b51-external-link
                       :caller caller :callee callee
                       :callsite-path callsite-path})))
    {:artifact :gravity/sh07-b51-external-call-v1
     :manifest-id (:manifest-id manifest)
     :edge-ordinal (:ordinal (first rows))}))

(defn- p15-s23-sh07-b51-runtime-link-fail! [plan instruction]
  (fail! "L2-UNKNOWN-CORE-FORM"
         "stage2 runtime rejected an unauthenticated B51 external link"
         {:source-span {:source (get-in plan [:source :path])}
          :operator (:function instruction)
          :reason :unauthenticated-b51-external-link
          :result-committed? false
          :output-committed? false
          :remediation
          "Execute only the independently pinned seven-module M3 bundle."}))
