(ns gravity.self-hosting.sh08-lexical-reservation-model-test
  "Compiler-free DATA controls for the finite lexical reservation removal."
  (:require [clojure.test :refer [deftest is]]))

(defn lexical-advancement-data-contract
  "Finite DATA contract for an already derived access result. The lookup's
  source/proof admission and general type validation are not modeled here."
  [{:keys [lookup node-id node-type lexical-bindings binding-types] :as input}]
  (let [eligible? (and (= :accepted (:status lookup))
                       (#{:gravity.type/applied-vector-nth :gravity.type/applied-vector-get} (get-in lookup [:descriptor :kind]))
                       (= :sh08-bounded-unit-step-v2 (get-in lookup [:descriptor :bounds-proof :loop-invariant :range-proof :version]))
                       (#{:gravity.type/keyword :gravity.type/bool} node-type))
        selected (when eligible? (filter #(and (= :immutable (:mutability %)) (= node-id (:initializer-node-id %))) lexical-bindings))]
    (assoc input :binding-types (reduce #(assoc %1 (:binding-id %2) node-type) binding-types selected))))

(deftest derived-v2-lexical-update-has-no-record-count-reservation-or-debit
  (doseq [[kind type] [[:gravity.type/applied-vector-get :gravity.type/keyword]
                     [:gravity.type/applied-vector-nth :gravity.type/bool]]]
    (let [base {:lookup {:status :accepted :descriptor {:kind kind :bounds-proof {:loop-invariant {:range-proof {:version :sh08-bounded-unit-step-v2}}}}}
                :node-id :read :node-type type
                :lexical-bindings [{:binding-id :stored :initializer-node-id :read :mutability :immutable}
                                   {:binding-id :unvisited :initializer-node-id :future :mutability :immutable}]
                :binding-types {:stored :gravity.type/unknown :unvisited :gravity.type/string}
                :loop-work-count 37 :diagnostics [{:reason :retained-source-failure}]
                :source-seed-conjunction [{:binding-id :stored :type :gravity.type/string}]
                :provisional-observations {:stored :gravity.type/unknown}}
          check (fn [input expected]
                  (let [r (lexical-advancement-data-contract input)]
                    (is (= expected (get-in r [:binding-types :stored])))
                    (is (= :gravity.type/string (get-in r [:binding-types :unvisited])))
                    (is (= (dissoc input :binding-types) (dissoc r :binding-types))) r))]
      (doseq [size [0 1 64 513]]
        (let [padded (update base :lexical-bindings into
                             (mapv #(hash-map :binding-id (keyword (str "unrelated-" %))
                                             :initializer-node-id :future :mutability :immutable) (range size)))]
          (is (= 37 (:loop-work-count (check padded type))))))
      (doseq [changed [(assoc-in base [:lookup :status] :rejected)
                       (assoc-in base [:lookup :status] :deferred)
                       (assoc-in base [:lookup :descriptor :kind] :opaque)
                       (assoc-in base [:lookup :descriptor :bounds-proof :loop-invariant :range-proof :version] :sh08-bounded-unit-step-v1)
                       (assoc-in base [:lookup :descriptor :bounds-proof :loop-invariant :range-proof :version] :forged)
                       (assoc base :node-type :gravity.type/unknown)
                       (assoc base :node-type :gravity.type/never)
                       (assoc-in base [:lexical-bindings 0 :mutability] :mutable)
                       (assoc-in base [:lexical-bindings 0 :initializer-node-id] :wrong)
                       (assoc base :node-id :stale-access)]]
        (check changed :gravity.type/unknown)))))
