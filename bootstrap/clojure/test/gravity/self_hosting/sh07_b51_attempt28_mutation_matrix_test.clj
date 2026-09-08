(ns gravity.self-hosting.sh07-b51-attempt28-mutation-matrix-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt28-acceptance-support
             :as support]))

(def current-carrier
  (delay (support/current-carrier)))

(def current-products
  (delay (support/current-products)))

(def current-runtime
  (delay (support/runtime)))

(def fabricated-digest
  (str "sha256:" (apply str (repeat 64 "a"))))

(def semantic-categories
  [:roots :members :functions :local-runtime-call-edges
   :external-runtime-call-edges :referenced-builtins :module-contracts])

(defn exception-data [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn reidentify-plan [plan]
  (let [base (dissoc plan :plan-id)
        identity (-> base
                     (update :source dissoc :path)
                     (update :module dissoc :source-path))]
    (assoc base :plan-id
           (bootstrap/c4-artifact-id
            (bootstrap/c-backend-canonical-value identity)))))

(defn reidentify-bundle [bundle]
  (let [base (dissoc bundle :bundle-id)]
    (assoc base :bundle-id
           (bootstrap/p15-s23-sh07-b51-canonical-id base))))

(defn rehash-semantic-root [carrier]
  (assoc carrier :semantic-root-hash
         (bootstrap/reader-canonical-hash
          {:domain :gravity/sh07-b51-v18-linked-semantic-root-v3
           :schema-version 3
           :semantic-manifest (:semantic-manifest carrier)})))

(defn coherently-rehash-products
  [carrier descriptors unsealed-manifest unsealed-plans]
  (let [old-plans (mapv :plan (:module-plan-carriers carrier))
        old-plan-ordinal (into {} (map-indexed (fn [ordinal plan]
                                                [(:plan-id plan) ordinal])
                                              old-plans))
        manifest (assoc (dissoc unsealed-manifest :manifest-id)
                        :manifest-id
                        (bootstrap/p15-s23-sh07-b51-manifest-id
                         unsealed-manifest))
        plans
        (mapv (fn [descriptor plan]
                (reidentify-plan
                 (assoc plan
                        :sh07-b51-module-source-id
                        (:module-source-id descriptor)
                        :sh07-b51-link-manifest-id
                        (:manifest-id manifest))))
              descriptors unsealed-plans)
        plan-by-source
        (into {} (map (juxt :sh07-b51-module-source-id identity)) plans)
        old-bundle (:execution-bundle carrier)
        bundle
        (reidentify-bundle
         (assoc (dissoc old-bundle :bundle-id)
                :manifest-id (:manifest-id manifest)
                :module-plans
                (mapv (fn [descriptor plan]
                        {:module-ordinal (:module-ordinal descriptor)
                         :module-source-id (:module-source-id descriptor)
                         :plan-id (:plan-id plan)})
                      descriptors plans)
                :routes
                (mapv (fn [edge]
                        {:edge-ordinal (:ordinal edge)
                         :caller-plan-id
                         (:plan-id
                          (get plan-by-source
                               (:caller-module-source-id edge)))
                         :callee-plan-id
                         (:plan-id
                          (get plan-by-source
                               (:callee-module-source-id edge)))})
                      (:edges manifest))
                :public-roots
                (mapv (fn [root]
                        (assoc root :plan-id
                               (:plan-id
                                (nth plans
                                     (get old-plan-ordinal (:plan-id root))))))
                      (:public-roots old-bundle))))
        semantic
        (assoc (:semantic-manifest carrier)
               :module-source-ids (mapv :module-source-id descriptors)
               :link-manifest-id (:manifest-id manifest)
               :module-plan-ids (mapv :plan-id plans)
               :execution-bundle-id (:bundle-id bundle))]
    (rehash-semantic-root
     (assoc carrier
            :module-source-descriptors descriptors
            :link-manifest manifest
            :module-plan-carriers
            (mapv (fn [descriptor plan]
                    {:module-ordinal (:module-ordinal descriptor)
                     :module-source-id (:module-source-id descriptor)
                     :plan-id (:plan-id plan)
                     :plan plan})
                  descriptors plans)
            :execution-bundle bundle
            :semantic-manifest semantic))))

(defn first-instruction [predicate]
  (first
   (for [plan (:module-plans @current-products)
         [_ definition] (:functions plan)
         instruction (tree-seq coll? seq (:instructions definition))
         :when (and (map? instruction) (predicate instruction))]
     {:plan plan :instruction instruction})))

(deftest attempt28-global-plus-one-and-closed-shape-matrix-rejects
  (let [carrier @current-carrier
        census (:physical-census carrier)
        semantic (:semantic-manifest carrier)
        commitments (:category-commitments semantic)
        last-edge (last (get-in carrier [:link-manifest :edges]))
        last-module (last (:module-censuses census))
        last-root (last (get-in carrier [:execution-bundle :public-roots]))
        candidates
        {"M1 external-call count 1424"
         (update-in carrier [:link-manifest :edges]
                    conj (assoc last-edge :ordinal 1423))
         "physical definition count 1452"
         (assoc-in carrier [:physical-census :physical-definition-count] 1452)
         "physical local-call count 3127"
         (assoc-in carrier
                   [:physical-census :physical-local-runtime-callsite-count]
                   3127)
         "physical builtin count 26128"
         (assoc-in carrier
                   [:physical-census
                    :physical-referenced-builtin-occurrence-count]
                   26128)
         "physical module count 8"
         (-> carrier
             (assoc-in [:physical-census :module-count] 8)
             (update-in [:physical-census :module-censuses]
                        conj (assoc last-module :module-ordinal 7)))
         "M3 public-root count 9"
         (update-in carrier [:execution-bundle :public-roots]
                    conj (assoc last-root :root-ordinal 9))
         "binding occupancy 2441"
         (-> carrier
             (assoc-in [:physical-census :maximum-binding-occupancy] 2441)
             (assoc-in [:physical-census :module-binding-occupancies 1] 2441)
             (assoc-in [:physical-census :module-censuses 1
                        :aggregate-binding-count] 2441))
         "semantic row count 65537"
         (-> carrier
             (assoc-in [:semantic-manifest :total-row-count] 65537)
             (assoc-in [:physical-census :semantic-row-count] 65537)
             rehash-semantic-root)
         "semantic category count 8"
         (update-in carrier [:semantic-manifest :category-commitments]
                    conj (assoc (last commitments) :category :attempt28-extra))
         "semantic leaf count plus one"
         (update-in carrier
                    [:semantic-manifest :category-commitments 0 :leaf-count]
                    inc)
         "semantic fold level plus one"
         (assoc-in carrier
                   [:semantic-manifest :category-commitments 5
                    :fold-level-count]
                   4)
         "schedule rows per leaf plus one"
         (assoc-in carrier [:semantic-manifest :schedule :rows-per-leaf] 17)
         "schedule fold fanout plus one"
         (assoc-in carrier [:semantic-manifest :schedule :fold-fanout] 17)
         "schedule maximum fold levels plus one"
         (assoc-in carrier
                   [:semantic-manifest :schedule :maximum-fold-levels] 4)
         "schedule category count plus one"
         (assoc-in carrier [:semantic-manifest :schedule :category-count] 8)
         "P28 carrier key is closed"
         (assoc carrier :attempt28-extra true)
         "P28 census key is closed"
         (assoc-in carrier [:physical-census :attempt28-extra] true)
         "P28 module census key is closed"
         (assoc-in carrier [:physical-census :module-censuses 0
                            :attempt28-extra] true)}]
    (is (= [1423 1451 3126 26127 7 8 2398 2440 32433]
           [(count (get-in carrier [:link-manifest :edges]))
            (:physical-definition-count census)
            (:physical-local-runtime-callsite-count census)
            (:physical-referenced-builtin-occurrence-count census)
            (:module-count census)
            (count (get-in carrier [:execution-bundle :public-roots]))
            (:maximum-binding-occupancy census)
            (:binding-ceiling census)
            (:total-row-count semantic)]))
    (is (= semantic-categories (mapv :category commitments)))
    (is (= [8 1392 1392 3003 1356 25275 7]
           (mapv :row-count commitments)))
    (doseq [[label candidate] candidates]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
            candidate))
          label))))

(deftest attempt28-category-row-and-fold-builders-reject-at-plus-one
  (let [empty-seven
        (into (array-map) (map (fn [category] [category []])
                               semantic-categories))
        eight-categories (assoc empty-seven :attempt28-extra [])
        row-overflow (assoc empty-seven :roots (vec (repeat 65537 nil)))
        oversized-leaves
        (mapv (fn [ordinal]
                {:first-row-ordinal (* 16 ordinal) :row-count 1})
              (range 4097))]
    (doseq [[label thunk]
            [["eighth semantic category"
              #(bootstrap/p15-s23-sh07-b51-build-semantic-manifest-v3
                @current-products eight-categories [])]
             ["semantic row 65537"
              #(bootstrap/p15-s23-sh07-b51-build-semantic-manifest-v3
                @current-products row-overflow [])]
             ["fourth fold level"
              #(bootstrap/p15-s23-sh07-b51-v3-fold-preimages
                :members oversized-leaves)]]]
      (let [data (exception-data thunk)]
        (is (= "L2-UNKNOWN-CORE-FORM" (:id data)) label)
        (is (= :unauthenticated-b51-linked-carrier (:reason data)) label)))))

(deftest attempt28-coherent-identity-rehash-does-not-create-authority
  (let [carrier @current-carrier
        descriptors (:module-source-descriptors carrier)
        manifest (:link-manifest carrier)
        plans (mapv :plan (:module-plan-carriers carrier))
        old-source-id (:module-source-id (first descriptors))
        changed-descriptor
        (let [changed (update (first descriptors) :source-byte-count inc)]
          (assoc changed :module-source-id
                 (bootstrap/p15-s23-sh07-b51-module-source-id changed)))
        new-source-id (:module-source-id changed-descriptor)
        m0-descriptors (assoc descriptors 0 changed-descriptor)
        m0-manifest (walk/postwalk-replace {old-source-id new-source-id}
                                           manifest)
        m0 (coherently-rehash-products carrier m0-descriptors m0-manifest plans)
        m1-manifest (update-in manifest [:edges 0 :arity] inc)
        m1 (coherently-rehash-products carrier descriptors m1-manifest plans)
        m2-plans (assoc plans 0 (assoc (first plans)
                                      :attempt28-rehashed-mutation true))
        m2 (coherently-rehash-products carrier descriptors manifest m2-plans)
        current-rehashed
        (coherently-rehash-products carrier descriptors manifest plans)
        m3-bundle
        (reidentify-bundle
         (assoc (:execution-bundle current-rehashed)
                :attempt28-rehashed-mutation true))
        m3 (-> current-rehashed
               (assoc :execution-bundle m3-bundle)
               (assoc-in [:semantic-manifest :execution-bundle-id]
                         (:bundle-id m3-bundle))
               rehash-semantic-root)
        p28 (-> carrier
                (update-in [:physical-census :physical-definition-count] inc)
                rehash-semantic-root)
        s28 (-> carrier
                (assoc-in [:semantic-manifest :category-commitments 0
                           :root-digest]
                          fabricated-digest)
                rehash-semantic-root)
        candidates {"M0 and its downstream identities" m0
                    "M1 and its downstream identities" m1
                    "M2 and its downstream identities" m2
                    "M3 and S28 root identity" m3
                    "P28 with a recomputed S28 root" p28
                    "S28 semantic root" s28}]
    (is (= carrier current-rehashed)
        "the cascade reproduces every authentic identity before mutation")
    (doseq [[label candidate] candidates]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
            candidate))
          label))))

(deftest attempt28-v2-and-source-link-authorities-are-independent
  (let [runtime @current-runtime
        v2
        (first-instruction
         #(and (= :builtin-call (:op %))
               (= 'sh07-canonical-text-compare (:function %))
               (= :gravity/sh07-compiler-internal-call-provenance-v2
                  (get-in % [:compiler-internal-provenance :artifact]))))
        external
        (first-instruction
         #(and (= :function-call (:op %))
               (map? (:external-link %))))
        v2-mutated
        (assoc-in (:instruction v2)
                  [:compiler-internal-provenance :source-sha256]
                  fabricated-digest)
        external-mutated
        (assoc-in (:instruction external) [:external-link :manifest-id]
                  fabricated-digest)]
    (is (map? v2) "an authenticated V2 occurrence must exist in M2")
    (is (map? external) "an authenticated source-link occurrence must exist")
    (is (= -1
           (bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
            runtime (:plan v2) (:instruction v2) ["a" "b"])))
    (let [authority
          (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
           runtime (:plan external) (:instruction external))]
      (is (= #{:callee-plan :edge :route} (set (keys authority))))
      (is (= (get-in (:instruction external)
                     [:external-link :edge-ordinal])
             (get-in authority [:edge :ordinal]))))
    (let [v2-data
          (exception-data
           #(bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
             runtime (:plan v2) v2-mutated ["a" "b"]))
          external-data
          (exception-data
           #(bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
             runtime (:plan external) external-mutated))]
      (is (= ["L2-BUILTIN-ERROR" :unauthenticated-compiler-provenance
              false false]
             ((juxt :id :reason :result-committed? :output-committed?)
              v2-data)))
      (is (= ["L2-UNKNOWN-CORE-FORM" :unauthenticated-b51-external-link
              false false]
             ((juxt :id :reason :result-committed? :output-committed?)
              external-data))))
    (is (= -1
           (bootstrap/p15-s23-sh07-runtime-invoke-compiler-internal-builtin
            runtime (:plan v2) (:instruction v2) ["a" "b"])))
    (is (map?
         (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
          runtime (:plan external) (:instruction external))))
    (is (empty? @(:sh07-b51-external-call-trace runtime))
        "authorization and rejected mutations commit no runtime trace")))

(deftest attempt28-nil-old-domain-and-public-sealing-bypasses-stay-closed
  (let [carrier @current-carrier
        products @current-products
        equal-but-not-identical (into (sorted-map) carrier)
        forbidden
        '[p15-s23-sh07-b51-seal-plan
          p15-s23-sh07-b51-seal-execution-bundle
          p15-s23-sh07-b51-seal-execution-bundle*
          p15-s23-sh07-b51-seal-linked-carrier-v3
          p15-s23-sh07-b51-plan-id
          p15-s23-sh07-b51-bundle-id
          p15-s23-sh07-b51-compilation-authority-token
          *p15-s23-sh07-b51-compilation-authority*
          p15-s23-sh07-b51-compilation-authorized?]]
    (doseq [candidate
            [nil {}
             {:domain :gravity/sh07-b51-v18-executable-closure-carrier-v18}
             {:domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v1}
             {:domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v2}]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
            candidate))))
    (doseq [candidate
            [nil {}
             {:artifact :gravity/sh07-b51-raw-adapter-request-v18}
             {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v1}
             {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v2}]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-raw-v3-authorized?
            candidate))))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-execution-bundle-authorized?
          nil nil nil nil)))
    (is (= carrier equal-but-not-identical))
    (is (not (identical? carrier equal-but-not-identical)))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
          equal-but-not-identical
          (:module-source-descriptors products)
          (:link-manifest products)
          (:module-plans products)
          (:execution-bundle products))))
    (testing "no recoverable public sealer can authenticate caller values"
      (doseq [symbol forbidden]
        (is (nil? (ns-resolve 'gravity.bootstrap symbol))
            (str symbol " must not be recoverable"))))))
