(ns gravity.self-hosting.sh07-b51-attempt30-successor-public-roots-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-b51-attempt30-integration-support
             :as support]))

(defn exact-entrypoint-envelope? [value status tag]
  (and (= #{:artifact :domain :schema-version :status :tag :value}
          (set (keys value)))
       (= :gravity/sh07-b51-entrypoint-result-v18 (:artifact value))
       (= :gravity/sh07-b51-entrypoint-abi-v18 (:domain value))
       (= 18 (:schema-version value))
       (= status (:status value))
       (= tag (:tag value))))

(defn- root1-trace [execution]
  (filterv #(= 'sh07-b51-build-template (:function %))
           (:trace execution)))

(deftest attempt30-successor-exposes-the-eight-authentic-m3-v2-roots
  (let [carrier (support/carrier)
        products (support/products)
        roots (get-in carrier [:execution-bundle :public-roots])]
    (is (= true
           (boolean
            (bootstrap/p15-s23-sh07-b51-linked-carrier-v4-valid? carrier))))
    (is (= :gravity/sh07-b51-execution-bundle-v2
           (get-in carrier [:execution-bundle :artifact])))
    (is (= 2 (get-in carrier [:execution-bundle :schema-version])))
    (is (= (vec (range 1 9)) (mapv :root-ordinal roots)))
    (is (= support/public-root-symbols (mapv :function roots)))
    (is (= [1 3 3 3 4 6 7 1] (mapv :arity roots)))
    (is (= (mapv :certificate-id (support/certificates))
           (get-in carrier
                   [:execution-bundle :compile-census-certificate-ids])))
    (doseq [ordinal (range 1 9)]
      (let [{:keys [root plan]} (support/root-plan ordinal)]
        (is (= (:plan-id root) (:plan-id plan)) ordinal)
        (is (= (:arity root)
               (get-in plan [:functions (:function root) :arity]))
            ordinal)))
    (is (= (:execution-bundle carrier) (:execution-bundle products)))))

(deftest attempt30-successor-runs-all-eight-roots-through-v4-admission
  (support/with-provisional-authority
   (fn []
     (let [raw (support/linked-raw-carrier
                "accepted" "nested-empty-wildcard-let")
           pipeline (support/execute-success-pipeline raw)
           root8 (support/execute-root-with-trace raw 8 [raw])
           {:keys [root1 root2 root3 root4 root5 root6 root7]} pipeline]
       (is (bootstrap/p15-s23-sh07-b51-linked-raw-v4-authorized? raw))
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
            (:result root8)
            :boundary-rejected :rejection-finalizer-boundary-rejected))
       (is (= :not-rejected (get-in root8 [:result :value :reason])))
       (is (= 1 (count (root1-trace root8))))))))

(deftest attempt30-root8-makes-zero-or-one-byte-identical-root1-call
  (support/with-provisional-authority
   (fn []
     (let [external-var
           (support/private-var
            'gravity.bootstrap
            'p15-s23-sh07-b51-runtime-execute-external-machine)
           original @external-var
           invoke
           (fn [raw]
             (let [root1-arguments (atom [])
                   execution
                   (with-redefs-fn
                     {external-var
                      (fn [runtime caller-plan instruction authority args scope]
                        (when (= 'sh07-b51-build-template
                                 (:function instruction))
                          (swap! root1-arguments conj args))
                        (original runtime caller-plan instruction
                                  authority args scope))}
                     #(support/execute-root-with-trace raw 8 [raw]))]
               {:execution execution :root1-arguments @root1-arguments}))
           success (support/linked-raw-carrier
                    "accepted" "nested-empty-wildcard-let")
           pending (support/linked-raw-carrier
                    "rejected" "unsupported-vector-rest")
           malformed (support/malformed-raw)]
       (doseq [raw [success pending]]
         (let [{:keys [execution root1-arguments]} (invoke raw)
               transferred (ffirst root1-arguments)]
           (is (= 1 (count root1-arguments)))
           (is (= [raw] (first root1-arguments)))
           (is (= (bootstrap/reader-canonical-hash raw)
                  (bootstrap/reader-canonical-hash transferred)))
           (is (= 1 (count (root1-trace execution))))))
       (let [{:keys [execution root1-arguments]} (invoke malformed)]
         (is (empty? root1-arguments))
         (is (empty? (root1-trace execution)))
         (is (exact-entrypoint-envelope?
              (:result execution)
              :boundary-rejected :rejection-finalizer-boundary-rejected))
         (is (= :raw-carrier-shape
                (get-in execution [:result :value :reason]))))))))

(deftest attempt30-declared-external-binding-accepts-and-unauthorized-rejects
  (support/with-provisional-authority
   (fn []
     (testing "a source-declared authenticated external binding is accepted"
       (let [raw (support/linked-raw-carrier
                  "accepted" "declared-import-external")
             execution (support/execute-root-with-trace raw 1 [raw])]
         (is (exact-entrypoint-envelope?
              (:result execution) :accepted :template-built))))
     (testing "a cross-fragment source edge without declaration is rejected"
       (let [raw (support/linked-raw-carrier
                  "rejected" "unauthorized-cross-fragment")
             execution (support/execute-root-with-trace raw 1 [raw])]
         (is (exact-entrypoint-envelope?
              (:result execution)
              :boundary-rejected :template-boundary-rejected))
         (is (= :source-integrity-mismatch
                (get-in execution [:result :value :reason])))))
     (testing "a mutated authenticated M2 external link commits no trace"
       (let [raw (support/linked-raw-carrier
                  "accepted" "nested-empty-wildcard-let")
             products (support/products)
             carrier (support/carrier)
             route (support/root-plan 1)
             register
             @(support/private-var
               'gravity.bootstrap
               'p15-s23-sh07-b51-register-admitted-runtime-context)
             unregister
             @(support/private-var
               'gravity.bootstrap
               'p15-s23-sh07-b51-unregister-admitted-runtime-context!)
             lease (register products carrier raw route [raw]
                             {:engine :attempt30-external-link-test})
             runtime (:runtime lease)
             {:keys [plan instruction]} (support/first-external-instruction)
             authority
             (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
              runtime plan instruction)
             mutation (assoc-in instruction [:external-link :manifest-id]
                                (bootstrap/reader-canonical-hash
                                 :unauthorized-cross-fragment))]
         (try
           (let [data
                 (try
                   (bootstrap/p15-s23-sh07-b51-runtime-authorize-external-link
                    runtime plan mutation)
                   nil
                   (catch clojure.lang.ExceptionInfo failure
                     (ex-data failure)))]
             (is (= #{:callee-plan :edge :route} (set (keys authority))))
             (is (= ["L2-UNKNOWN-CORE-FORM"
                     :unauthenticated-b51-external-link false false]
                    ((juxt :id :reason :result-committed? :output-committed?)
                     data)))
             (is (empty? @(:sh07-b51-external-call-trace runtime))))
           (finally
             (unregister runtime)
             (java.lang.ref.Reference/reachabilityFence
              (:context-lease lease)))))))))
