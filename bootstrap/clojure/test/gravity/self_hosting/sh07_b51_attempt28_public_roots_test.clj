(ns gravity.self-hosting.sh07-b51-attempt28-public-roots-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt28-acceptance-support
             :as support]))

(defn exact-entrypoint-envelope? [value status tag]
  (and (= #{:artifact :domain :schema-version :status :tag :value}
          (set (keys value)))
       (= :gravity/sh07-b51-entrypoint-result-v18 (:artifact value))
       (= :gravity/sh07-b51-entrypoint-abi-v18 (:domain value))
       (= 18 (:schema-version value))
       (= status (:status value))
       (= tag (:tag value))))

(defn root1-trace [execution]
  (filterv #(= 'sh07-b51-build-template (:function %))
           (:trace execution)))

(deftest attempt28-current-products-expose-the-exact-eight-m3-public-roots
  (let [carrier (support/current-carrier)
        products (support/current-products)
        roots (get-in products [:execution-bundle :public-roots])]
    (is (bootstrap/p15-s23-sh07-b51-linked-carrier-v3-authorized? carrier))
    (is (bootstrap/p15-s23-sh07-b51-execution-bundle-authorized?
         (:module-source-descriptors products)
         (:link-manifest products)
         (:module-plans products)
         (:execution-bundle products)))
    (is (= (vec (range 1 9)) (mapv :root-ordinal roots)))
    (is (= support/public-root-symbols (mapv :function roots)))
    (is (= [1 3 3 3 4 6 7 1] (mapv :arity roots)))
    (doseq [ordinal (range 1 9)]
      (let [{:keys [root plan]} (support/root-plan ordinal)]
        (is (= (:plan-id root) (:plan-id plan)) ordinal)
        (is (= (:arity root)
               (get-in plan [:functions (:function root) :arity]))
            ordinal)))))

(deftest attempt28-all-eight-m3-public-roots-execute-authentically
  (let [{:keys [success-raw success-pipeline success-root8]}
        @support/acceptance-fixture
        {:keys [root1 root2 root3 root4 root5 root6 root7]}
        success-pipeline
        root-results
        [(:result root1) (:result root2)
         (mapv (comp :result identity) (:executions root3))
         (:result root4) (:result root5) (:result root6)
         (:result root7) (:result success-root8)]]
    (is (exact-entrypoint-envelope?
         (:result root1) :accepted :template-built))
    (is (exact-entrypoint-envelope?
         (:result root2) :accepted :template-verified))
    (is (seq (:executions root3)))
    (is (every? #(exact-entrypoint-envelope?
                  (:result %) :accepted :digest-preimage-resolved)
                (:executions root3)))
    (is (exact-entrypoint-envelope?
         (:result root4) :accepted :template-resolved))
    (is (exact-entrypoint-envelope?
         (:result root5) :accepted :resolved-verified))
    (is (exact-entrypoint-envelope?
         (:result root6) :accepted :independent-verifier-bound))
    (is (exact-entrypoint-envelope?
         (:result root7) :accepted :final-artifact-built))
    (is (exact-entrypoint-envelope?
         (:result success-root8)
         :boundary-rejected :rejection-finalizer-boundary-rejected))
    (is (= :not-rejected
           (get-in success-root8 [:result :value :reason])))
    (is (= success-raw
           (get-in root4
                   [:result :value :predecessor-observation
                    :preimage :raw-adapter-request])))
    (is (= (bootstrap/reader-canonical-hash success-raw)
           (bootstrap/reader-canonical-hash
            (get-in root4
                    [:result :value :predecessor-observation
                     :preimage :raw-adapter-request]))))
    (is (= 8 (count root-results)))))

(deftest attempt28-root8-observes-zero-or-one-byte-identical-root1-transfer
  (let [{:keys [success-raw success-root8 pending-raw pending-root8
                malformed-root8]}
        @support/acceptance-fixture
        success-hash (bootstrap/reader-canonical-hash success-raw)
        pending-hash (bootstrap/reader-canonical-hash pending-raw)]
    (testing "valid linked-v3 carriers cross Root8 to Root1 exactly once"
      (is (= 1 (count (root1-trace success-root8))))
      (is (= 1 (count (root1-trace pending-root8))))
      (is (= success-hash (bootstrap/reader-canonical-hash success-raw)))
      (is (= pending-hash (bootstrap/reader-canonical-hash pending-raw)))
      (doseq [execution [success-root8 pending-root8]]
        (let [row (first (root1-trace execution))]
          (is (= (get-in (support/current-products)
                         [:execution-bundle :bundle-id])
                 (:bundle-id row)))
          (is (= (:plan-id (:plan (support/root-plan 1)))
                 (:callee-plan-id row))))))
    (testing "a valid-arity malformed Root8 request returns before Root1"
      (is (empty? (root1-trace malformed-root8)))
      (is (exact-entrypoint-envelope?
           (:result malformed-root8)
           :boundary-rejected :rejection-finalizer-boundary-rejected))
      (is (= :raw-carrier-shape
             (get-in malformed-root8 [:result :value :reason]))))))

(deftest attempt28-root8-pending-success-and-malformed-laws-are-closed
  (let [{:keys [success-root8 pending-root8 malformed-root8]}
        @support/acceptance-fixture
        pending-result (:result pending-root8)
        finalized (get-in pending-result [:value :envelope :result])]
    (is (exact-entrypoint-envelope?
         pending-result :accepted :rejection-finalized))
    (is (= :accepted (get-in pending-result [:value :status])))
    (is (= :rejected (:status finalized)))
    (is (= (get-in pending-result [:value :digest-id])
           (get-in finalized [:diagnostic :diagnostic-id])))
    (is (exact-entrypoint-envelope?
         (:result success-root8)
         :boundary-rejected :rejection-finalizer-boundary-rejected))
    (is (= :not-rejected
           (get-in success-root8 [:result :value :reason])))
    (is (exact-entrypoint-envelope?
         (:result malformed-root8)
         :boundary-rejected :rejection-finalizer-boundary-rejected))
    (is (= :raw-carrier-shape
           (get-in malformed-root8 [:result :value :reason])))))
