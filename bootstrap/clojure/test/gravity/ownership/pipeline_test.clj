(ns gravity.ownership.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.c9-ownership-checker :as c9]
            [gravity.c9-ownership-checker.graphs :as graphs]
            [gravity.ownership :as ownership]
            [gravity.self-hosting.sh10-ownership-transition-fixture-support
             :as fixture-support]))

(defn- node [id operation operands extra]
  {:node-id id :source-operation operation :operands operands
   :source {:origin-id (str id ":origin") :span {:source "ownership-data.gravity" :start 0 :end 1}}
   :ownership (ownership/persistent-ownership :value (assoc extra :storage :persistent-value))})

(deftest contract-states-the-exact-data-boundary
  (is (= #{'clojure.set} (set (map ns-name (vals (ns-aliases 'gravity.ownership))))))
  (is (= #{'persistent-ownership 'forwarded-value-node-ids 'propagate-managed-ownership
           'shared-owner-domain-id 'canonical-owner-id 'persistent-aliasing-valid?
           'persistent-ownership-expected-keys 'persistent-ownership-schema-valid?
           'expected-managed-node-ids 'native-ownership-graph 'native-borrow-graph}
         (set (keys (ns-publics 'gravity.ownership)))))
  (doseq [[name operation] (ns-publics 'gravity.ownership)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.ownership (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-verifier-certificates? contract)))
      (is (some #{:general-c9-analysis} (:unsupported contract))))))

(def ^:private graph-projectors
  [ownership/native-ownership-graph graphs/ownership-graph
   (fn [span ids node module graph]
     (c9/with-operations {:source-span span :c9-node-ids ids :c9-node node}
       #(c9/c9-ownership-graph module graph)))])

(deftest native-graph-projections-retain-source-owner-order-and-callback-order
  (doseq [project graph-projectors]
    (let [calls (atom []) source-z {:span :original-z :origin-chain [:generated :source]}
          source-a {:span :original-a :origin-chain [:source]}
          module {:module 'native.graph :source-path "native-graph-data" :profile :hosted :target :jvm}
          input {:nodes {"z" {:source source-z} "a" {:source source-a}}}
          span (fn [path index] (swap! calls conj [:span path index]) {:source path :form-index index})
          ids (fn [graph] (is (identical? input graph)) (swap! calls conj :ids) ["z" "a"])
          node (fn [values index fallback]
                 (swap! calls conj [:node values index fallback]) :selected-node)
          result (project span ids node module input)]
      (is (= :gravity/c9-ownership-graph (:artifact result)))
      (is (= ["a" "z"] (vec (keys (:owners result)))))
      (is (= "owner-2" (get-in result [:owners "a" :owner-id])))
      (is (= "owner-1" (get-in result [:owners "z" :owner-id])))
      (is (= :persistent-immutable (get-in result [:owners "z" :kind])))
      (is (= :owned-mutable (get-in result [:owners "a" :kind])))
      (is (identical? source-z (get-in result [:owners "z" :source])))
      (is (identical? source-a (get-in result [:owners "a" :source])))
      (is (= [:hosted :jvm] ((juxt :profile :target) (get-in result [:owners "a"]))))
      (is (= :selected-node (get-in result [:moves 0 :value])))
      (is (= [:ids [:node ["z" "a"] 1 "core-node-owned"]
              [:span "native-graph-data" 0] [:span "native-graph-data" 0]] @calls)))))

(deftest native-borrow-projection-retains-each-callback-and-fallback
  (doseq [project [ownership/native-borrow-graph graphs/borrow-graph
                   (fn [ids node module graph]
                     (c9/with-operations {:c9-node-ids ids :c9-node node}
                       #(c9/c9-borrow-graph module graph)))]]
    (let [calls (atom [])
          ids (fn [_] (swap! calls conj :ids) ["zero" "one"])
          node (fn [values index fallback]
                 (swap! calls conj [index fallback]) (get values index fallback))
          result (project ids node {:module 'native.graph} {})]
      (is (= :gravity/c9-borrow-graph (:artifact result)))
      (is (= ["one" "one" "one" "core-node-field" "core-node-transfer"]
             (mapv :value (:edges result))))
      (is (= [:ids [1 "core-node-owned"] [1 "core-node-owned"] [1 "core-node-owned"]
              [2 "core-node-field"] [3 "core-node-transfer"]] @calls)))))

(deftest native-graph-refusals-preserve-exception-identity-and-stop-later-callbacks
  (doseq [project graph-projectors
          failure [:ids :node :span :owner-key]]
    (let [calls (atom []) error (ex-info "original graph callback" {:callback failure})
          ids (fn [_]
                (swap! calls conj :ids)
                (when (= :ids failure) (throw error))
                (if (= :owner-key failure) ["string-id" :keyword-id] []))
          node (fn [& _] (swap! calls conj :node) (when (= :node failure) (throw error)) :node)
          span (fn [& _] (swap! calls conj :span) (throw error))
          observed (try (project span ids node {} {}) (catch Throwable value value))]
      (if (= :owner-key failure)
        (is (instance? ClassCastException observed))
        (is (identical? error observed)))
      (is (= (case failure :ids [:ids] :node [:ids :node]
                    :span [:ids :node :span] :owner-key [:ids]) @calls)))))

(deftest real-paired-gravity-ownership-programs-accept-and-reject
  ;; Current Gravity normalized-request kernels. No authenticated Core/C8
  ;; composition or native/provider grant is attempted by these fixtures.
  (doseq [extension [".gravity" ".qst"]]
    (let [plan (fixture-support/compile-plan
                (fixture-support/fixture-relative-path
                 "accepted" "ownership-transitions" extension))
          bad-plan (fixture-support/compile-plan
                    (fixture-support/fixture-relative-path
                     "rejected" "invalid-ownership-transitions" extension))
          request (fixture-support/invoke (delay plan)
                                          'sh10-many-immutable-request [])
          bad-request (fixture-support/invoke (delay bad-plan)
                                              'sh10-use-after-move-request [])
          result (fixture-support/check request)
          rejection (fixture-support/check bad-request)]
      (doseq [compiled [plan bad-plan]]
        (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
        (is (= (:plan-id compiled)
               (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc compiled :plan-id))))))))
      (is (= :accepted (:status result)))
      (is (= :rejected (:status rejection)))
      (is (= "C9-USE-AFTER-MOVE" (get-in rejection [:diagnostics 0 :rule])))
      (is (= (:source-span request) (get-in result [:preserves :source-span])))
      (is (= :passed
             (:status
              (fixture-support/invoke-c9
               'sh10-verify-ownership-result [request result])))))))

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
