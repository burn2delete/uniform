(ns gravity.ownership
  "Native seed persistent ownership data and predicates."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  (quote
{:authenticates-inputs? false,
 :public-api
 {persistent-ownership {:arglists ([role extra]), :admission :none},
  persistent-ownership-schema-valid?
  {:arglists ([expected-keys node managed?]), :admission :none},
  forwarded-value-node-ids {:arglists ([node]), :admission :none},
  propagate-managed-ownership
  {:arglists ([forwarded-value-node-ids nodes]), :admission :none},
  expected-managed-node-ids
  {:arglists ([forwarded-value-node-ids nodes]), :admission :none},
  persistent-aliasing-valid? {:arglists ([node]), :admission :none},
  persistent-ownership-expected-keys
  {:arglists ([common-keys node managed?]), :admission :none},
  shared-owner-domain-id
  {:arglists ([digest nodes]), :admission :none},
  canonical-owner-id
  {:arglists ([shared-owner-domain-id product node]),
   :admission :none}},
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by [gravity.bootstrap],
  :forbids
  [gravity.bootstrap
   gravity.c9-ownership-checker
   gravity.cli
   gravity.release]},
 :issues-verifier-certificates? false,
 :governing-contracts
 [D1
  D2
  D3
  D8
  D9
  L10
  SAFE2
  SAFE3
  SAFE4
  SAFE5
  C1
  C6
  C7
  C8
  C9
  C10
  C11
  C15
  BOOT8
  TEST2],
 :unsupported
 [:public-input-admission
  :callback-authentication
  :general-c9-analysis
  :mutable-borrow-lifetimes
  :regions
  :arenas
  :linear-resources
  :provider-grants
  :native-or-backend-execution
  :performance
  :migration
  :release
  :self-hosting
  :seed-retirement],
 :artifact-inputs
 [:explicit-native-core-node-data
  :persistent-ownership-extras
  :shared-domain-digest-operation
  :forwarding-and-owner-operations
  :common-key-schema],
 :namespace gravity.ownership,
 :diagnostic-ownership
 {:stage
  [:persistent-sharing-predicate :exact-ownership-key-predicate],
  :existing-callers
  [C9-MUT-ALIAS C9-TRANSFER C6-VERIFY :diagnostic-construction]},
 :artifact-outputs
 [:persistent-ownership-records
  :forwarded-node-identities
  :managed-provider-propagation
  :owner-domain-identities
  :alias-and-schema-predicates
  :expected-managed-node-identities],
 :contract-boundary :native-seed-persistent-ownership-data}
))

(defn persistent-ownership
  [role extra]
  (merge
   {:model :persistent-immutable-value
    :role role
    :shareability :shared
    :alias-policy :immutable-sharing
    :mutation :forbidden
    :managed-reachability :value-and-program-reachability
    :cleanup-policy :no-explicit-cleanup
    :provider-requirement :not-required
    :allocator-requirement :not-required
    :escape-policy :safe-persistent-value
    :derived? true}
   extra))

(defn forwarded-value-node-ids
  [node]
  (case (:source-operation node)
    :local [(first (:operands node))]
    :let-binding [(first (:operands node))]
    :if (vec (rest (:operands node)))
    :do [(last (:operands node))]
    :let [(last (:operands node))]
    :function [(last (:operands node))]
    []))

(defn propagate-managed-ownership
  [forwarded-value-node-ids nodes]
  (loop [remaining nodes
         normalized []
         node-by-id {}]
    (if-let [node (first remaining)]
      (let [source-node-ids
            (forwarded-value-node-ids node)
            managed-forwarding?
            (some
             #(= :gravity.reference/jvm-managed-allocator
                 (get-in node-by-id [% :ownership :provider-id]))
             source-node-ids)
            node
            (if managed-forwarding?
              (update node :ownership
                      assoc
                      :provider-requirement
                      :gravity.reference/jvm-managed-allocator
                      :allocator-requirement :memory/allocator
                      :provider-id :gravity.reference/jvm-managed-allocator
                      :lifetime :managed-reachability)
              node)]
        (recur (next remaining) (conj normalized node)
               (assoc node-by-id (:node-id node) node)))
      normalized)))

(defn shared-owner-domain-id
  [digest nodes]
  (when-let [function-node (first (filter #(= :function (:kind %)) nodes))]
    (digest
     {:kind :gravity/persistent-shared-owner-domain
      :function-node-id (:node-id function-node)})))

(defn canonical-owner-id
  [shared-owner-domain-id product node]
  (or (shared-owner-domain-id
       (or (:core-nodes product) (:nodes product)))
      ;; Stored fact tables are verifier inputs, not an authority.  They are a
      ;; fallback only for prospective subjects that do not carry the core
      ;; node set needed for independent owner-domain reconstruction.
      (get-in product [:ownership-facts (:node-id node) :owner-id])
      :not-applicable))

(defn persistent-aliasing-valid?
  [node]
  (let [ownership (:ownership node)
        _operands (:operands node)]
    (and (= :persistent-immutable-value (:model ownership))
         (= :shared (:shareability ownership))
         (= :immutable-sharing (:alias-policy ownership))
         (= :forbidden (:mutation ownership)))))

(defn persistent-ownership-expected-keys
  [common-keys node managed?]
  (let [_ownership (:ownership node)
        extras
        (case (:source-operation node)
          :local #{:binding-node-id}
          :let-binding #{:value-node-id}
          :if #{:incoming-node-ids}
          :do #{:result-node-id}
          :let #{:result-node-id}
          :function #{:result-node-id :result-disposition}
          :str #{:provider-id :lifetime}
          #{})
        extras
        (if managed?
          (set/union extras #{:provider-id :lifetime})
          extras)]
    (set/union common-keys
               extras)))

(defn persistent-ownership-schema-valid?
  [expected-keys node managed?]
  (= (expected-keys node managed?)
     (set (keys (:ownership node)))))

(defn expected-managed-node-ids
  [forwarded-value-node-ids nodes]
  (reduce
   (fn [managed node]
     (let [managed?
           (or (= :str (:source-operation node))
               (some managed
                     (forwarded-value-node-ids node)))]
       (cond-> managed managed? (conj (:node-id node)))))
   #{} nodes))

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
