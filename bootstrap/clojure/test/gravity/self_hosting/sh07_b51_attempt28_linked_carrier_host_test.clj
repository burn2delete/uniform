(ns gravity.self-hosting.sh07-b51-attempt28-linked-carrier-host-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def exact-schedule-keys
  #{:rows-per-leaf :fold-fanout :maximum-fold-levels :category-count
    :maximum-total-rows :maximum-row-nodes :maximum-row-depth
    :maximum-row-width :maximum-row-scalar-utf8 :maximum-row-c11-bytes
    :maximum-row-canonical-reader-bytes :maximum-leaf-nodes
    :maximum-leaf-depth :maximum-leaf-width :maximum-leaf-scalar-utf8
    :maximum-leaf-c11-bytes :maximum-leaf-canonical-reader-bytes
    :maximum-fold-nodes :maximum-fold-depth :maximum-fold-width
    :maximum-fold-scalar-utf8 :maximum-fold-c11-bytes
    :maximum-fold-canonical-reader-bytes :maximum-commitment-nodes
    :maximum-commitment-depth :maximum-commitment-width
    :maximum-commitment-scalar-utf8 :maximum-commitment-c11-bytes
    :maximum-commitment-canonical-reader-bytes :maximum-root-nodes
    :maximum-root-depth :maximum-root-width :maximum-root-scalar-utf8
    :maximum-root-c11-bytes :maximum-root-canonical-reader-bytes})

(def current-carrier
  (delay (bootstrap/p15-s23-sh07-b51-current-linked-carrier-v3)))

(def accepted-attempt28-execution-bundle-id
  "sha256:6f4e7824372c66c2b315c95e5383e468159b6697846cccc55c7a6c6c0142c58f")

(def superseded-execution-bundle-id
  "sha256:7d94fd4826fe13f024d45867270a72b26865501950635a6091300e88d0b45d1f")

(def fabricated-execution-bundle-id
  (str "sha256:" (apply str (repeat 64 \0))))

(def exact-carrier-keys
  #{:domain :schema-version :predecessor-executable-descriptor-id
    :semantic-manifest :semantic-root-hash :module-source-descriptors
    :link-manifest :module-plan-carriers :execution-bundle :physical-census})

(def exact-physical-census-keys
  #{:module-count :module-censuses :physical-definition-count
    :reachable-definition-count :physical-local-runtime-callsite-count
    :reachable-local-runtime-callsite-count
    :physical-external-runtime-callsite-count
    :reachable-external-runtime-callsite-count
    :physical-referenced-builtin-occurrence-count
    :reachable-referenced-builtin-occurrence-count :semantic-row-count
    :module-binding-occupancies :maximum-binding-occupancy :binding-ceiling})

(deftest attempt28-host-schedule-and-folds-are-closed-and-bounded
  (let [schedule bootstrap/p15-s23-sh07-b51-v3-schedule
        rows (mapv (fn [ordinal] {:member-ordinal ordinal}) (range 17))
        leaves (bootstrap/p15-s23-sh07-b51-v3-leaf-preimages
                :members rows)
        folded (bootstrap/p15-s23-sh07-b51-v3-fold-preimages
                :members leaves)
        two-level
        (bootstrap/p15-s23-sh07-b51-v3-fold-preimages
         :members
         (bootstrap/p15-s23-sh07-b51-v3-leaf-preimages
          :members (mapv (fn [ordinal] {:member-ordinal ordinal})
                         (range 257))))
        empty-leaves (bootstrap/p15-s23-sh07-b51-v3-leaf-preimages
                      :roots [])]
    (is (= exact-schedule-keys (set (keys schedule))))
    (is (= 35 (count schedule)))
    (is (= [16 1] (mapv :row-count leaves)))
    (is (= [0 16] (mapv :first-row-ordinal leaves)))
    (is (= 1 (:fold-level-count folded)))
    (is (= 17 (:row-count (last (:preimages folded)))))
    (is (= 2 (:fold-level-count two-level)))
    (is (= 257 (:row-count (last (:preimages two-level)))))
    (is (= 1 (count empty-leaves)))
    (is (zero? (:row-count (first empty-leaves))))
    (is (string? (:root-digest folded)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:root-digest folded)))))

(deftest attempt28-host-prospective-value-bounds-fail-at-plus-one
  (let [base {:nodes 48 :depth 6 :width 8 :scalar-utf8 4096
              :c11-utf8 8192 :canonical-reader-utf8 9216}]
    (is (bootstrap/p15-s23-sh07-b51-v3-value-profile-within? base "row"))
    (doseq [field [:nodes :depth :width :scalar-utf8
                   :c11-utf8 :canonical-reader-utf8]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-v3-value-profile-within?
            (update base field inc) "row"))
          (str field " must reject at its first exceeding value")))))

(deftest attempt28-host-local-order-is-authenticated-m2-preorder
  (let [products
        {:module-plans
         [{:functions
           {'z {:instructions
                {:op :if
                 :test {:op :builtin-call :function '= :args [1 1]}
                 :then {:op :function-call :function 'local-b :args []}
                 :else {:op :builtin-call :function 'get :args [{} :k]}}}
            'a {:instructions
                {:op :do
                 :body [{:op :function-call :function 'local-a :args []}
                        {:op :builtin-call :function 'count :args [[]]}]}}}}]}
        calls (:calls
               (bootstrap/p15-s23-sh07-b51-v3-plan-call-census products))]
    (is (= [['a :function 'local-a 0]
            ['a :builtin 'count 1]
            ['z :builtin '= 2]
            ['z :function 'local-b 0]
            ['z :builtin 'get 2]]
           (mapv (juxt :caller :kind :callee :arity) calls)))))

(deftest attempt28-host-raw-v3-rejects-old-and-fabricated-carriers
  (testing "old carrier domains and incomplete raw values do not acquire authority"
    (doseq [artifact [:gravity/sh07-b51-raw-adapter-request-v18
                      :gravity/sh07-b51-raw-adapter-request-linked-v1
                      :gravity/sh07-b51-raw-adapter-request-linked-v2]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-raw-v3-authorized?
            {:artifact artifact}))))
    (doseq [domain [:gravity/sh07-b51-v18-executable-closure-carrier-v18
                    :gravity/sh07-b51-v18-linked-executable-closure-carrier-v1
                    :gravity/sh07-b51-v18-linked-executable-closure-carrier-v2]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
            {:domain domain}))))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-linked-raw-v3-authorized?
          {:artifact :gravity/sh07-b51-raw-adapter-request-linked-v3
           :schema-version 3
           :scope :sh07-b51-vector-destructuring})))))

(deftest attempt28-current-source-carrier-is-closed-and-self-consistent
  (let [carrier @current-carrier
        semantic (:semantic-manifest carrier)
        census (:physical-census carrier)
        modules (:module-censuses census)
        external (get-in semantic [:category-commitments 4])]
    (is (= exact-carrier-keys (set (keys carrier))))
    (is (= exact-physical-census-keys (set (keys census))))
    (is (= 7 (:module-count census) (count modules)))
    (is (= (vec (range 7)) (mapv :module-ordinal modules)))
    (is (= (:semantic-row-count census) (:total-row-count semantic)))
    (is (= (:reachable-external-runtime-callsite-count census)
           (:row-count external)))
    (is (= (:module-binding-occupancies census)
           (mapv :aggregate-binding-count modules)))
    (is (= (:maximum-binding-occupancy census)
           (apply max (:module-binding-occupancies census))))
    (is (every? #(<= % (:binding-ceiling census))
                (:module-binding-occupancies census)))
    (is (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized? carrier))
    (doseq [path [[:semantic-root-hash]
                  [:semantic-manifest :link-manifest-id]
                  [:module-source-descriptors 0 :module-source-id]
                  [:link-manifest :manifest-id]
                  [:module-plan-carriers 0 :plan-id]
                  [:execution-bundle :bundle-id]
                  [:physical-census :semantic-row-count]]]
      (is (false?
           (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
            (update-in carrier path (fn [value]
                                      (if (integer? value)
                                        (inc value)
                                        (str value "-mutated"))))))
          (str "mutation must reject at " path)))))

(deftest attempt28-execution-bundle-pin-authorizes-only-the-frozen-current-m3
  (let [carrier @current-carrier
        products {:module-source-descriptors (:module-source-descriptors carrier)
                  :link-manifest (:link-manifest carrier)
                  :module-plans (mapv :plan (:module-plan-carriers carrier))
                  :execution-bundle (:execution-bundle carrier)}
        runtime (bootstrap/p15-s23-sh07-b51-runtime-context products {})
        current-bundle (:execution-bundle products)]
    (is (= accepted-attempt28-execution-bundle-id
           bootstrap/p15-s23-sh07-b51-expected-execution-bundle-id
           (:bundle-id current-bundle)))
    (is (bootstrap/p15-s23-sh07-b51-execution-bundle-authorized?
         (:module-source-descriptors products)
         (:link-manifest products)
         (:module-plans products)
         current-bundle))
    (is (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
         runtime (first (:module-plans products))))
    (doseq [rejected-id [superseded-execution-bundle-id
                         fabricated-execution-bundle-id]]
      (let [rejected-bundle (assoc current-bundle :bundle-id rejected-id)
            rejected-runtime
            (assoc runtime :sh07-b51-execution-bundle rejected-bundle)]
        (is (false?
             (bootstrap/p15-s23-sh07-b51-execution-bundle-authorized?
              (:module-source-descriptors products)
              (:link-manifest products)
              (:module-plans products)
              rejected-bundle))
            rejected-id)
        (is (false?
             (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized?
              (assoc-in carrier [:execution-bundle :bundle-id] rejected-id)))
            rejected-id)
        (is (false?
             (bootstrap/p15-s23-sh07-b51-runtime-augmented-plan-authorized?
              rejected-runtime (first (:module-plans products))))
            rejected-id)))))

(deftest attempt28-runtime-context-binds-current-carrier-before-dispatch
  (let [carrier @current-carrier
        products {:module-source-descriptors (:module-source-descriptors carrier)
                  :link-manifest (:link-manifest carrier)
                  :module-plans (mapv :plan (:module-plan-carriers carrier))
                  :execution-bundle (:execution-bundle carrier)}
        runtime (bootstrap/p15-s23-sh07-b51-runtime-context
                 products {:sh07-b51-linked-carrier-v3 :caller-forged})]
    (is (identical? carrier (:sh07-b51-linked-carrier-v3 runtime)))
    (is (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
         (:sh07-b51-linked-carrier-v3 runtime)
         (:module-source-descriptors products) (:link-manifest products)
         (:module-plans products) (:execution-bundle products)))
    (is (false?
         (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-runtime-products-match?
          (assoc carrier :semantic-root-hash "sha256:caller-forged")
          (:module-source-descriptors products) (:link-manifest products)
          (:module-plans products) (:execution-bundle products))))))
