
(defn p15-s23-sh07-b51-compile-census-certificate-v1-valid?
  [certificate]
  (try
    (let [role (:role certificate)
          role-domain
          ({:producer-census
            :gravity/sh07-b51-producer-census-certificate-v1
            :independent-verifier-census
            :gravity/sh07-b51-independent-verifier-census-certificate-v1}
           role)
          algorithm-role
          ({:producer-census :source-first-exhaustive
            :independent-verifier-census
            :m2-first-independent-exhaustive}
           role)
          evidence-domain
          ({:producer-census
            :gravity/sh07-b51-producer-census-evidence-root-v1
            :independent-verifier-census
            :gravity/sh07-b51-independent-verifier-census-evidence-root-v1}
           role)
          claim (:claim certificate)
          assumptions (:assumptions certificate)
          checker (:checker certificate)
          inputs (:inputs certificate)
          result (:result certificate)
          provenance (:provenance certificate)
          raw (:raw-source-transcript certificate)
          m2 (:m2-transcript certificate)
          commitments (:category-commitments claim)
          normalized-preimage
          {:domain :gravity/sh07-b51-normalized-census-id-v1
           :schema-version 1
           :physical-census (:physical-census claim)
           :ordered-category-row-roots (mapv :root-digest commitments)
           :category-row-counts (mapv :row-count commitments)}
          claim-preimage
          {:domain :gravity/sh07-b51-common-census-claim-id-v1
           :schema-version 1
           :architecture-report-sha256
           (:architecture-report-sha256 certificate)
           :inputs inputs :assumptions assumptions :claim claim
           :profile-bounds (:profile-bounds certificate)}
          evidence-preimage
          {:domain evidence-domain :schema-version 1 :checker checker
           :inputs inputs :raw-source-transcript raw :m2-transcript m2
           :result result
           :reproduction-command-id (:reproduction-command-id provenance)
           :profile-bounds (:profile-bounds certificate)}]
      (and (p15-s23-sh07-b51-v4-certificate-preflight? certificate)
           (p15-s23-sh07-b51-exact-keys?
            certificate p15-s23-sh07-b51-v4-certificate-keys)
           (= :gravity/sh07-b51-compile-census-certificate
              (:artifact certificate))
           (= role-domain (:domain certificate))
           (= 1 (:schema-version certificate))
           (= :accepted (:status certificate))
           (= p15-s23-sh07-b51-attempt32-architecture-report-sha256
              (:architecture-report-sha256 certificate)
              (:architecture-report-sha256 inputs))
           (p15-s23-sh07-b51-exact-keys?
            claim p15-s23-sh07-b51-v4-claim-keys)
           (= :gravity/sh07-b51-common-census-claim-v1 (:domain claim))
           (p15-s23-sh07-b51-digest-id?
            (:external-bijection-commitment claim))
           (p15-s23-sh07-b51-v4-digest-vector?
            (:builtin-parity-commitments claim) 7)
           (p15-s23-sh07-b51-digest-id?
            (:provenance-completeness-commitment claim))
           (= p15-s23-sh07-b51-v4-schedule (:category-schedule claim))
           (p15-s23-sh07-b51-v4-category-commitments-valid? commitments)
           (p15-s23-sh07-b51-v4-physical-census-valid?
            (:physical-census claim))
           (p15-s23-sh07-b51-v4-category-census-joins?
            commitments (:physical-census claim))
           (= (:normalized-census-id claim)
              (p15-s23-sh07-b51-canonical-id normalized-preimage))
           (p15-s23-sh07-b51-exact-keys?
            assumptions p15-s23-sh07-b51-v4-assumption-keys)
           (= {:sha256-collision-resistance :assumed
               :canonical-reader-hash-id
               :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
               :source-emitter-bytes-immutable true
               :checker-closures-independent true
               :host-pin-boundary-id p15-s23-sh07-b51-host-pin-boundary-id
               :runtime-token-boundary-id
               p15-s23-sh07-b51-runtime-token-boundary-id}
              assumptions)
           (p15-s23-sh07-b51-v4-checker-valid? checker algorithm-role)
           (p15-s23-sh07-b51-v4-inputs-valid? inputs)
           (p15-s23-sh07-b51-v4-result-valid? result claim)
           (p15-s23-sh07-b51-exact-keys?
            provenance p15-s23-sh07-b51-v4-provenance-keys)
           (every? p15-s23-sh07-b51-digest-id?
                   ((juxt :tool-closure-id :raw-transcript-artifact-id
                          :m2-transcript-artifact-id
                          :reproduction-command-id) provenance))
           (every? #(and (string? %) (not (empty? %)))
                   ((juxt :invocation-id :evidence-relative-path
                          :completed-at) provenance))
           (p15-s23-sh07-b51-v4-transcript-valid?
            raw :gravity/sh07-b51-raw-source-census-transcript-v1
            checker inputs)
           (p15-s23-sh07-b51-v4-transcript-valid?
            m2 :gravity/sh07-b51-m2-census-transcript-v1
            checker inputs)
           (= (:artifact-id raw) (:raw-transcript-artifact-id provenance))
           (= (:artifact-id m2) (:m2-transcript-artifact-id provenance))
           (= p15-s23-sh07-b51-attempt32-profile-bounds
              (:profile-bounds certificate))
           (= (:claim-id certificate)
              (p15-s23-sh07-b51-canonical-id claim-preimage))
           (= (:evidence-root certificate)
              (p15-s23-sh07-b51-canonical-id evidence-preimage))
           (= (:certificate-id certificate)
              (p15-s23-sh07-b51-canonical-id
               (dissoc certificate :certificate-id)))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-compile-census-certificate-pair-v1-valid?
  [certificates]
  (try
    (let [[certificate-a certificate-b] certificates
          checker-a (:checker certificate-a)
          checker-b (:checker certificate-b)
          checker-identity-keys
          [:source-sha256 :semantic-id :executable-id :dependency-closure-id]
          common-keys
          [:architecture-report-sha256 :claim-id :claim :assumptions
           :inputs :profile-bounds]]
      (and (vector? certificates) (= 2 (count certificates))
           (= [:producer-census :independent-verifier-census]
              (mapv :role certificates))
           (every? p15-s23-sh07-b51-compile-census-certificate-v1-valid?
                   certificates)
           (= (select-keys certificate-a common-keys)
              (select-keys certificate-b common-keys))
           (not= (:certificate-id certificate-a)
                 (:certificate-id certificate-b))
           (every? #(not= (get checker-a %) (get checker-b %))
                   checker-identity-keys)))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn p15-s23-sh07-b51-v4-bundle-core-preimage [products]
  {:domain :gravity/sh07-b51-execution-bundle-core-id-v1
   :schema-version 1
   :manifest-id (get-in products [:link-manifest :manifest-id])
   :module-plans
   (mapv (fn [descriptor plan]
           {:module-ordinal (:module-ordinal descriptor)
            :module-source-id (:module-source-id descriptor)
            :plan-id (:plan-id plan)})
         (:module-source-descriptors products) (:module-plans products))
   :routes
   (let [plan-by-source
         (into {} (map (juxt :sh07-b51-module-source-id identity)
                       (:module-plans products)))]
     (mapv (fn [edge]
             {:edge-ordinal (:ordinal edge)
              :caller-plan-id
              (:plan-id (get plan-by-source (:caller-module-source-id edge)))
              :callee-plan-id
              (:plan-id (get plan-by-source (:callee-module-source-id edge)))})
           (get-in products [:link-manifest :edges])))
   :public-roots
   (mapv (fn [root]
           {:root-ordinal (:root-ordinal root)
            :function (:function root) :arity (:arity root)
            :plan-id (:plan-id
                      (nth (:module-plans products) (:module-ordinal root)))})
         p15-s23-sh07-b51-public-root-specs)})

(defn- p15-s23-sh07-b51-v4-declared-edge-valid?
  [descriptors edge]
  (let [by-id (into {} (map (juxt :module-source-id identity)) descriptors)
        caller (get by-id (:caller-module-source-id edge))
        callee (get by-id (:callee-module-source-id edge))
        external
        (first (filter #(= (:callee-function edge) (:symbol %))
                       (:external-bindings caller)))
        caller-definition
        (first (filter #(= (:caller-function edge) (:name %))
                       (:definitions caller)))
        callee-definition
        (first (filter #(= (:callee-function edge) (:name %))
                       (:definitions callee)))]
    (and caller callee external caller-definition callee-definition
         (p15-s23-sh07-b51-edge-row-valid? edge)
         (= (p15-s23-sh07-b51-owner-edge-kind
             (:owner caller) (:owner callee))
            (:owner-edge edge) (:owner-edge external))
         (= (:callee-module-ordinal external) (:module-ordinal callee))
         (= (:callee-module external) (:module callee))
         (= (:callee-source-sha256 external) (:source-sha256 callee))
         (= (:caller-function-source-hash edge)
            (:source-hash caller-definition))
         (= (:callee-function-source-hash edge)
            (:source-hash callee-definition))
         (= (:callee-definition-source-hash external)
            (:source-hash callee-definition))
         (= (:arity external) (:arity edge) (:arity callee-definition)))))

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
