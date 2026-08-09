(ns gravity.c9-ownership-checker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c9-ownership-checker :as c9]))

(def module
  {:module 'gravity.c9-test
   :source-path "c9-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def effect-nodes
  (sorted-map
   "core-node-0" {:source {:span {:source "c9-test.gravity"
                                   :form-index 0}}}
   "core-node-1" {:source {:span {:source "c9-test.gravity"
                                   :form-index 1}}}
   "core-node-2" {:source {:span {:source "c9-test.gravity"
                                   :form-index 2}}}
   "core-node-3" {:source {:span {:source "c9-test.gravity"
                                   :form-index 3}}}))

(def c8-artifact
  {:kind :gravity/stage0-c8-effect-checker-artifact
   :artifact-id "sha256:c8"
   :effect-graph {:artifact :gravity/c8-effect-graph
                  :nodes effect-nodes
                  :status :complete}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c9-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c8-effect-source-artifact (fn [_ _] c8-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c9/c9-engine-contract)
        publics (ns-publics 'gravity.c9-ownership-checker)]
    (is (= :hosted-stage0-c9-ownership-checker
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c9-authority? contract)))
    (is (false? (:ownership-model-complete? contract)))
    (is (some #{:ownership-safety-authority} (:does-not-own contract)))
    (is (some #{:linear-resource-provider-authority}
              (:does-not-own contract)))
    (is (true? (get-in contract
                       [:operation-interposition
                        :unknown-keys-rejected?])))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c9/public-api))))
    (doseq [[name spec] c9/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operation-map
          [nil
           {:unknown identity}
           {:c9-node :keyword-is-invokable}
           {:c9-ownership-diagnostic-ids [:not-a-string]}
           {:c9-ownership-governing-document ""}
           {:c9-ownership-rejected-designs [:not-a-map]}
           {:c9-ownership-override-diagnostics {:unsafe :not-a-string}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c9/with-operations operation-map
                   (constantly :unreachable)))))
  (let [calls (atom [])
        graph (c9/with-operations
                {:c9-node (fn [node-ids index fallback]
                            (swap! calls conj [node-ids index fallback])
                            "interposed-node")}
                #(c9/c9-ownership-graph module
                                        {:nodes effect-nodes}))]
    (is (= "interposed-node" (get-in graph [:moves 0 :value])))
    (is (= 1 (count @calls)))))

(deftest ownership-engine-emits-complete-products
  (let [artifact (c9/with-operations
                   (operations)
                   #(c9/compiler-c9-ownership-source-artifact
                     "c9-test.gravity" "ignored"))]
    (is (= :gravity/stage0-c9-ownership-checker-artifact (:kind artifact)))
    (is (= 4 (count (get-in artifact [:ownership-graph :owners]))))
    (is (= 5 (count (get-in artifact [:borrow-graph :edges]))))
    (is (= 9 (count (get-in artifact [:lifetime-interval-map :intervals]))))
    (is (= 2 (count (get-in artifact [:region-lifetime-graph :regions]))))
    (is (= 1 (count (get-in artifact [:arena-generation-graph :arenas]))))
    (is (= 2 (count (get-in artifact
                            [:linear-resource-flow-graph :resources]))))
    (is (= 4 (count (get-in artifact [:transfer-records :records]))))
    (is (= 4 (count (get-in artifact [:runtime-check-records :records]))))
    (is (= 2 (count (get-in artifact
                            [:unsafe-audit-references :records]))))
    (is (= :passed (get-in artifact [:ownership-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c9/c9-ownership-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact
                             [:ownership-diagnostics :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c9-identities
  (doseq [[failure expected-id]
          [[:use-after-move "C9-USE-AFTER-MOVE"]
           [:use-after-consume "C9-USE-AFTER-CONSUME"]
           [:borrow-escape "C9-BORROW-ESCAPE"]
           [:mut-alias "C9-MUT-ALIAS"]
           [:move-while-borrowed "C9-MOVE-WHILE-BORROWED"]
           [:region-escape "C9-REGION-ESCAPE"]
           [:arena-generation "C9-ARENA-GENERATION"]
           [:linear-leak "C9-LINEAR-LEAK"]
           [:linear-double "C9-LINEAR-DOUBLE"]
           [:transfer "C9-TRANSFER"]
           [:runtime-check "C9-RUNTIME-CHECK"]
           [:unsafe "C9-UNSAFE"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler
                                     :c9-ownership-check :fail]
                                    failure)
            error (try
                    (c9/with-operations
                      (operations failed-module)
                      #(c9/compiler-c9-ownership-source-artifact
                        "c9-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C9" (:document-id (ex-data error))))))))
