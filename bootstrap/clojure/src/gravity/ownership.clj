(ns gravity.ownership
  "Native seed persistent ownership data and predicates."
  (:require [clojure.set :as set]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {persistent-ownership {:arglists ([role extra]), :admission :none},
  native-borrow-graph
  {:arglists ([node-ids node module effect-graph]),
   :admission :none,
   :native-compatibility-only? true},
  persistent-ownership-schema-valid?
  {:arglists ([expected-keys node managed?]), :admission :none},
  forwarded-value-node-ids {:arglists ([node]), :admission :none},
  native-ownership-graph
  {:arglists ([source-span node-ids node module effect-graph]),
   :admission :none,
   :native-compatibility-only? true},
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
 :native-graph-boundary :c9-native-graph-projections,
 :native-graph-status-is-data? true,
 :dependency-direction
 {:requires [clojure.set],
  :consumed-by [gravity.bootstrap gravity.c9-ownership-checker.graphs],
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
  TEST2
  D6],
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
  :common-key-schema
  :explicit-native-module-and-effect-node-view
  :source-span-and-node-callbacks],
 :preserves-native-graphs
 [:callback-lookup-and-invocation-order
  :exception-identity
  :node-order-and-owner-numbering
  :sorted-owner-map
  :fallback-values
  :source-origin-records
  :profile-and-target-data],
 :namespace gravity.ownership,
 :diagnostic-ownership
 {:stage
  [:persistent-sharing-predicate :exact-ownership-key-predicate],
  :existing-callers
  [C9-MUT-ALIAS C9-TRANSFER C6-VERIFY :diagnostic-construction],
  :native-graph-projections :original-callback-and-host-errors},
 :artifact-outputs
 [:persistent-ownership-records
  :forwarded-node-identities
  :managed-provider-propagation
  :owner-domain-identities
  :alias-and-schema-predicates
  :expected-managed-node-identities
  :native-ownership-graph
  :native-borrow-graph],
 :contract-boundary :native-seed-persistent-ownership-data}
)

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

(defn native-ownership-graph [source-span node-ids node module effect-graph]
  (let [node-ids (node-ids effect-graph)]
    {:artifact :gravity/c9-ownership-graph
     :module (:module module)
     :owners
     (into (sorted-map)
           (map-indexed (fn [idx node-id]
                          [node-id
                           {:value-id node-id
                            :owner-id (str "owner-" (inc idx))
                            :kind (case (mod idx 6)
                                    0 :persistent-immutable
                                    1 :owned-mutable
                                    2 :borrowed-immutable
                                    3 :borrowed-mutable
                                    4 :region-owned
                                    :linear-resource)
                            :copyable? (zero? (mod idx 6))
                            :profile (:profile module)
                            :target (:target module)
                            :source (get-in effect-graph [:nodes node-id :source])}])
                        node-ids))
     :moves [{:move-id "move-owned-buffer"
              :from "buffer" :to "worker-buffer"
              :value (node node-ids 1 "core-node-owned")
              :owner-before "owner-2" :owner-after "owner-task"
              :span (source-span (:source-path module) 0)
              :status :accepted}]
     :consumes [{:consume-id "consume-linear-file"
                 :resource-id "resource-file" :operation :close
                 :owner-before "owner-resource" :terminal-state :closed
                 :span (source-span (:source-path module) 0)
                 :status :accepted}]
     :status :complete}))

(defn native-borrow-graph [node-ids node module effect-graph]
  (let [node-ids (node-ids effect-graph)]
    {:artifact :gravity/c9-borrow-graph
     :module (:module module)
     :nodes {:owners ["owner-2" "owner-region" "owner-resource"]
             :borrows ["borrow-immutable-a" "borrow-immutable-b"
                       "borrow-mutable-exclusive"]
             :ranges [:whole :header :payload]
             :provider-scopes [:provider/scope-stdout :provider/scope-memory]}
     :edges [{:edge :immutable-borrow :owner "owner-2"
              :borrow-id "borrow-immutable-a"
              :value (node node-ids 1 "core-node-owned") :range :whole
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :immutable-borrow :owner "owner-2"
              :borrow-id "borrow-immutable-b"
              :value (node node-ids 1 "core-node-owned") :range :whole
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :mutable-borrow :owner "owner-2"
              :borrow-id "borrow-mutable-exclusive"
              :value (node node-ids 1 "core-node-owned") :range :payload
              :lifetime "lt-borrow-write" :status :accepted}
             {:edge :field-projection :owner "owner-2"
              :borrow-id "borrow-field-header"
              :value (node node-ids 2 "core-node-field") :range :header
              :lifetime "lt-borrow-read" :status :accepted}
             {:edge :transfer :owner "owner-2" :destination "owner-task"
              :value (node node-ids 3 "core-node-transfer")
              :lifetime "lt-structured-task" :status :accepted}]
     :conflict-analysis {:many-immutable-borrows :accepted
                         :one-mutable-borrow :accepted
                         :move-while-borrowed :rejected-by-diagnostic
                         :mutable-alias :rejected-by-diagnostic
                         :status :complete}
     :status :complete}))

(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
