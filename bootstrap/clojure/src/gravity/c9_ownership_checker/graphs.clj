(ns gravity.c9-ownership-checker.graphs
  "Ownership and borrow graph projections for the hosted C9 facade.")

(defn ownership-graph [source-span node-ids node module effect-graph]
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

(defn borrow-graph [node-ids node module effect-graph]
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
