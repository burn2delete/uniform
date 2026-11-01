(ns gravity.ownership.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.ownership :as ownership]
            [gravity.self-hosting.sh10-ownership-transition-test]))

(defn- helper [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh10-ownership-transition-test name)))

(defn- node [id operation operands extra]
  {:node-id id :source-operation operation :operands operands
   :source {:origin-id (str id ":origin") :span {:source "ownership-data.gravity" :start 0 :end 1}}
   :ownership (ownership/persistent-ownership :value (assoc extra :storage :persistent-value))})

(deftest contract-states-the-exact-data-boundary
  (is (= #{'clojure.set} (set (map ns-name (vals (ns-aliases 'gravity.ownership))))))
  (is (= #{'persistent-ownership 'forwarded-value-node-ids 'propagate-managed-ownership
           'shared-owner-domain-id 'canonical-owner-id 'persistent-aliasing-valid?
           'persistent-ownership-expected-keys 'persistent-ownership-schema-valid?
           'expected-managed-node-ids}
         (set (keys (ns-publics 'gravity.ownership)))))
  (doseq [[name operation] (ns-publics 'gravity.ownership)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.ownership (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:general-c9-analysis} (:unsupported contract))))))

(deftest real-paired-gravity-ownership-programs-accept-and-reject
  ;; Current Gravity normalized-request kernels. No authenticated Core/C8
  ;; composition or native/provider grant is attempted by these fixtures.
  (doseq [extension [".gravity" ".qst"]]
    (let [compile-plan (helper 'compile-plan)
          invoke (helper 'invoke)
          plan (compile-plan (str "bootstrap/clojure/fixtures/self-hosting/sh-10/accepted/ownership-transitions" extension))
          bad-plan (compile-plan (str "bootstrap/clojure/fixtures/self-hosting/sh-10/rejected/invalid-ownership-transitions" extension))
          request (invoke (delay plan) 'sh10-many-immutable-request [])
          bad-request (invoke (delay bad-plan) 'sh10-use-after-move-request [])
          result ((helper 'check) request)
          rejection ((helper 'check) bad-request)]
      (doseq [compiled [plan bad-plan]]
        (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
        (is (= (:plan-id compiled)
               (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc compiled :plan-id))))))))
      (is (= :accepted (:status result)))
      (is (= :rejected (:status rejection)))
      (is (= "C9-USE-AFTER-MOVE" (get-in rejection [:diagnostics 0 :rule])))
      (is (= (:source-span request) (get-in result [:preserves :source-span])))
      (is (= :passed (:status ((helper 'invoke-c9) 'sh10-verify-ownership-result [request result])))))))

(deftest forwarding-propagation-and-schema-retain-existing-semantics
  (let [source (node :allocated :str [] {:provider-id :gravity.reference/jvm-managed-allocator
                                        :lifetime :managed-reachability})
        local (node :local :local [:allocated] {:binding-node-id :allocated})
        join (node :join :if [:test :local :other] {:incoming-node-ids [:local :other]})
        nodes [source local join]
        result (ownership/propagate-managed-ownership #'b/p15-s23-closed-core-forwarded-value-node-ids nodes)]
    (is (= result (b/p15-s23-closed-core-propagate-managed-ownership nodes)))
    (is (= (mapv :source nodes) (mapv :source result)))
    (is (= [:allocated] (ownership/forwarded-value-node-ids local)))
    (is (= [:local :other] (ownership/forwarded-value-node-ids join)))
    (is (= #{:allocated :local :join} (b/p15-s23-closed-core-expected-managed-node-ids nodes)))
    (is (= (b/p15-s23-closed-core-expected-managed-node-ids nodes)
           (ownership/expected-managed-node-ids #'b/p15-s23-closed-core-forwarded-value-node-ids nodes)))
    (is (= :not-required (get-in (first (b/p15-s23-closed-core-propagate-managed-ownership [local source]))
                                [:ownership :provider-requirement])))
    (doseq [candidate result]
      (is (b/p15-s23-closed-core-persistent-aliasing-valid? candidate))
      (is (b/p15-s23-closed-core-persistent-ownership-schema-valid? candidate true))
      (is (= (b/p15-s23-closed-core-persistent-ownership-expected-keys candidate true)
             (ownership/persistent-ownership-expected-keys
               b/p15-s23-closed-core-persistent-ownership-common-keys candidate true)))
      (doseq [field [:model :shareability :alias-policy :mutation]]
        (is (false? (ownership/persistent-aliasing-valid? (assoc-in candidate [:ownership field] :forged)))))
      (is (false? (b/p15-s23-closed-core-persistent-ownership-schema-valid?
                    (assoc-in candidate [:ownership :forged] true) true))))
    (doseq [[operation operands expected]
            [[:let-binding [:a] [:a]] [:do [:a :b] [:b]] [:let [:a :b] [:b]]
             [:function [:a :b] [:b]] [:literal [] []] [:unknown [:a] []]]]
      (is (= expected (ownership/forwarded-value-node-ids {:source-operation operation :operands operands}))))))

(deftest owner-identity-fallback-and-extra-precedence-are-preserved
  (let [function {:kind :function :node-id "function-source-id"}
        product {:core-nodes [function] :ownership-facts {:value {:owner-id :forged}}}
        subject {:node-id :value}
        expected (b/p15-s23-closed-core-digest
                   {:kind :gravity/persistent-shared-owner-domain :function-node-id "function-source-id"})]
    (is (= expected (b/p15-s23-closed-core-shared-owner-domain-id [function])))
    (is (= expected (b/p15-s23-closed-core-canonical-owner-id product subject)))
    (is (= expected (ownership/canonical-owner-id #'b/p15-s23-closed-core-shared-owner-domain-id product subject)))
    (is (= :fallback (b/p15-s23-closed-core-canonical-owner-id
                      {:ownership-facts {:value {:owner-id :fallback}}} subject)))
    (is (= :not-applicable (b/p15-s23-closed-core-canonical-owner-id {} subject)))
    (is (nil? (b/p15-s23-closed-core-shared-owner-domain-id [])))
    ;; Existing merge precedence constructs DATA and does not authorize it.
    (is (= :forged (:mutation (ownership/persistent-ownership :value {:mutation :forged}))))))

(deftest wrappers-preserve-late-vars-dispatch-and-host-lookup-order
  (let [original b/p15-s23-closed-core-persistent-ownership-schema-valid?
        seen (atom nil)]
    (with-redefs [b/p15-s23-closed-core-persistent-ownership-schema-valid?
                  (fn [& args] (reset! seen args) :two-argument-port)]
      (is (= :two-argument-port (original {:source-operation :str})))
      (is (= [{:source-operation :str} true] @seen))))
  (with-redefs [b/p15-s23-closed-core-forwarded-value-node-ids (constantly [])
                b/p15-s23-closed-core-shared-owner-domain-id (constantly :owner-port)
                b/p15-s23-closed-core-persistent-ownership-expected-keys (constantly #{:only})]
    (is (= #{:allocated} (b/p15-s23-closed-core-expected-managed-node-ids
                          [{:node-id :allocated :source-operation :str}
                           {:node-id :local :source-operation :local :operands [:allocated]}])))
    (is (= :owner-port (b/p15-s23-closed-core-canonical-owner-id {} {})))
    (is (b/p15-s23-closed-core-persistent-ownership-schema-valid? {:ownership {:only nil}} false)))
  (with-redefs [b/p15-s23-closed-core-digest (constantly :digest-port)]
    (is (= :digest-port (b/p15-s23-closed-core-shared-owner-domain-id [{:kind :function}]))))
  (let [lookups (atom [])
        subject (reify clojure.lang.ILookup
                  (valAt [_ key] (swap! lookups conj key) nil)
                  (valAt [_ key fallback] (swap! lookups conj key) fallback))]
    (is (false? (ownership/persistent-aliasing-valid? subject)))
    (is (= [:ownership :operands] @lookups))
    (reset! lookups [])
    (ownership/persistent-ownership-expected-keys #{} subject false)
    (is (= [:ownership :source-operation] @lookups))))
