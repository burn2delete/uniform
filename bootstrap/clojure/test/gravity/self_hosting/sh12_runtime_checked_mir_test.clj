(ns gravity.self-hosting.sh12-runtime-checked-mir-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh11-runtime-checked-verifier-test]
            [gravity.self-hosting.sh12-authenticated-mir-integration-test]))

(defn- resolved-value [namespace name]
  (or
   (some-> (ns-resolve namespace name) var-get)
   (throw
    (ex-info
     "Required authenticated integration helper is unavailable"
     {:id "SH12-RUNTIME-HELPER"
      :namespace namespace
      :name name}))))

(def ^:private sh11-namespace
  'gravity.self-hosting.sh11-runtime-checked-verifier-test)

(def ^:private sh12-namespace
  'gravity.self-hosting.sh12-authenticated-mir-integration-test)

(defn- invoke-sh12 [plan-name function arguments]
  ((resolved-value sh12-namespace 'invoke)
   (resolved-value sh12-namespace plan-name)
   function arguments))

(defn- run-runtime-checked [extension actual-path]
  (let [upstream
        ((resolved-value sh11-namespace 'runtime-product)
         extension)
        products
        {:safe-core (get-in upstream [:run :result])
         :verification (:verification upstream)}]
    ((resolved-value sh12-namespace 'run-sh12)
     products extension :runtime-checked actual-path)))

(def ^:private runtime-runs
  (into
   {}
   (for [extension [".gravity" ".qst"]]
     [extension
      (delay
       (run-runtime-checked
        extension
        (str "/checkout-a/sh12-runtime" extension)))])))

(defn- runtime-run [extension]
  @(get runtime-runs extension))

(defn- runtime-parts [run]
  (let [checked-core (:checked-core run)
        mir (:mir run)
        node (first (:core-nodes checked-core))
        node-id (:node-id node)
        check (get-in checked-core
                      [:safety-facts node-id :check])
        check-id (:check-id check)
        check-operation-id
        (str check-id ":mir:runtime-check")
        token-id (str check-id ":mir:token")
        entrypoint (:entrypoint checked-core)
        function (get-in mir [:functions entrypoint])
        entry-id (:entry function)
        block (get-in function [:blocks entry-id])]
    {:checked-core checked-core
     :mir mir
     :node node
     :node-id node-id
     :check check
     :check-id check-id
     :check-operation-id check-operation-id
     :token-id token-id
     :function function
     :entry-id entry-id
     :block block}))

(deftest sh12-runtime-checked-products-build-and-verify-guarded-mir
  (doseq [extension [".gravity" ".qst"]]
    (testing extension
      (let [run (runtime-run extension)
            {:keys
             [checked-core mir node-id check check-id
              check-operation-id token-id function
              entry-id block]}
            (runtime-parts run)
            instructions (:instructions block)
            check-operation
            (first
             (filter
              #(= check-operation-id (:op-id %))
              instructions))
            guarded-operation
            (first
             (filter #(= node-id (:op-id %))
                     instructions))
            guard-edge
            {:from token-id
             :consumer-kind :operation
             :consumer-id node-id
             :consumer-block entry-id
             :operand-index 0
             :edge-kind :runtime-check-guard}]
        (is (= :accepted (:status checked-core)))
        (is (= :runtime-checked
               (get-in checked-core
                       [:identity-input :safe-outcome])))
        (is (= #{:error/raise}
               (get-in checked-core
                       [:source-core-input :declared-effects])))
        (is (= #{:error/raise}
               (get-in checked-core
                       [:effect-facts node-id :direct])))
        (is (= #{:error/raise}
               (get-in checked-core
                       [:effect-facts node-id :residual])))
        (is (= :error/numeric (:failure check)))
        (is (= :before-operation
               (:emitted-location check)))
        (is (= :gravity/mir-module (:artifact mir)))
        (is (= :stage1-source-owned
               (get-in run [:mir-verification :status])))
        (is (= :accepted
               (get-in run [:result :status])))
        (is (= :passed
               (get-in run [:verification :status])))
        (is (= []
               (get-in run [:result :diagnostics])))
        (is (= 1 (count (:runtime-check-table mir))))
        (is (= node-id
               (get-in mir
                       [:runtime-check-table check-id
                        :guarded-operation-id])))
        (is (= check-operation-id
               (get-in mir
                       [:runtime-check-table check-id
                        :check-operation-id])))
        (is (= token-id
               (get-in mir
                       [:runtime-check-table check-id
                        :token-value-id])))
        (is (= :runtime-check
               (:opcode check-operation)))
        (is (= token-id (:result check-operation)))
        (is (= node-id
               (get-in check-operation
                       [:facts :guarded-operation-id])))
        (is (= token-id
               (first (:operands guarded-operation))))
        (is (< (.indexOf instructions check-operation)
               (.indexOf instructions guarded-operation)))
        (is (some #{guard-edge}
                  (get-in mir [:data-flow-graph :edges])))
        (is (= check-operation-id
               (get-in mir
                       [:data-flow-graph :definitions
                        token-id :operation-id])))
        (is (= :runtime-check-token
               (get-in mir
                       [:data-flow-graph :values
                        token-id :kind])))
        (is (= :runtime-check-generated
               (get-in mir
                       [:source-map check-operation-id
                        :generated-origin 0 :role])))
        (is (= node-id
               (get-in mir
                       [:source-map check-operation-id
                        :generated-origin 0
                        :producer-operation-id])))
        (is (= check-id
               (get-in mir
                       [:source-map check-operation-id
                        :generated-origin 0
                        :runtime-check-id])))
        (is (= {:value-id token-id
                :operation-id check-operation-id
                :block-id entry-id}
               (get-in mir
                       [:data-flow-graph :definitions
                        token-id])))
        (is (= #{:error/raise}
               (get-in mir
                       [:effect-table
                        (get-in checked-core
                                [:effect-facts node-id
                                 :fact-id])
                        :direct])))
        (is (= #{:error/raise}
               (:latent-effects function)))
        (is (true?
             (invoke-sh12
              'bridge-plan
              'sh12-mir-envelope-valid?
              [checked-core mir])))
        (is (true?
             (invoke-sh12
              'bridge-plan
              'sh12-runtime-check-mir-valid?
              [checked-core mir])))
        (is (= #{:status :mir-module :checks
                 :diagnostics :preserves}
               (set
                (keys (:mir-verification run)))))
        (is (= :stage1-source-owned
               (get-in run
                       [:mir-verification :status])))
        (is (= mir
               (get-in run
                       [:mir-verification :mir-module])))
        (is (true?
             (invoke-sh12
              'bridge-plan
              'sh12-exact-mir-module-equal?
              [mir
               (get-in run
                       [:mir-verification
                        :mir-module])])))
        (is (true?
             (invoke-sh12
              'bridge-plan
              'sh12-mir-verification-valid?
              [mir (:mir-verification run)])))
        (is (= (str "/checkout-a/sh12-runtime"
                    extension)
               (get-in checked-core
                       [:provenance
                        :actual-source-path])))))))

(deftest sh12-runtime-checked-identities-are-co-canonical
  (let [gravity (runtime-run ".gravity")
        qst (runtime-run ".qst")]
    (is (= (get-in gravity
                   [:checked-core :identity-input])
           (get-in qst
                   [:checked-core :identity-input])))
    (is (= (:mir-id gravity) (:mir-id qst)))
    (is (= (dissoc (:mir gravity) :provenance)
           (dissoc (:mir qst) :provenance)))
    (is (not= (get-in gravity [:mir :provenance])
              (get-in qst [:mir :provenance])))
    (is (= :accepted
           (get-in gravity [:result :status])))
    (is (= :accepted
           (get-in qst [:result :status])))
    (is (= :passed
           (get-in gravity [:verification :status])))
    (is (= :passed
           (get-in qst [:verification :status])))))

(deftest sh12-runtime-checked-rejects-self-consistent-nil-check
  (let [upstream
        ((resolved-value sh11-namespace 'runtime-product)
         ".gravity")
        safe-core (get-in upstream [:run :result])
        check
        (get-in safe-core
                [:safety-result :runtime-checks 0])
        nil-check
        (-> safe-core
            (assoc-in
             [:safety-result :runtime-checks] [nil])
            (assoc-in
             [:safety-result :outcomes 0
              :runtime-check] nil)
            (assoc-in
             [:identity-input :runtime-checks] [nil])
            (assoc-in
             [:identity-input :outcomes 0
              :runtime-check] nil))
        altered-condition
        (let [altered
              (assoc check :condition :always-true)]
          (-> safe-core
              (assoc-in
               [:safety-result :runtime-checks 0]
               altered)
              (assoc-in
               [:safety-result :outcomes 0
                :runtime-check]
               altered)
              (assoc-in
               [:identity-input :runtime-checks 0]
               altered)
              (assoc-in
               [:identity-input :outcomes 0
                :runtime-check]
               altered)))
        coordinated-invalid-condition
        (let [altered
              (-> check
                  (assoc :condition :always-true)
                  (assoc-in
                   [:guard-proof :condition]
                   :always-true))
              altered-request
              (-> (get-in
                   safe-core
                   [:safety-result :identity-input
                    :request])
                  (assoc-in
                   [:runtime-check :condition]
                   :always-true)
                  (assoc-in
                   [:runtime-check-support :conditions]
                   #{:always-true}))]
          (-> safe-core
              (assoc-in
               [:safety-result :identity-input
                :request]
               altered-request)
              (assoc-in
               [:safety-result :runtime-checks 0]
               altered)
              (assoc-in
               [:safety-result :outcomes 0
                :runtime-check]
               altered)
              (assoc-in
               [:identity-input :runtime-checks 0]
               altered)
              (assoc-in
               [:identity-input :outcomes 0
                :runtime-check]
               altered)))
        coordinated-statically-safe-division
        (let [request
              (get-in
               safe-core
               [:safety-result :identity-input
                :request])
              altered-facts
              (assoc (:facts request) :divisor 1)
              altered-predicate
              (assoc-in
               (get-in request
                       [:runtime-check :predicate])
               [:operands :divisor] 1)
              altered-request
              (-> request
                  (assoc :facts altered-facts)
                  (assoc-in
                   [:runtime-check :predicate]
                   altered-predicate))
              altered-check
              (-> check
                  (assoc-in
                   [:guard-proof :predicate]
                   altered-predicate))
              altered-outcome
              (-> (get-in
                   safe-core
                   [:safety-result :outcomes 0])
                  (assoc :facts altered-facts)
                  (assoc :runtime-check
                         altered-check))]
          (-> safe-core
              (assoc-in
               [:safety-result :identity-input
                :request]
               altered-request)
              (assoc-in
               [:safety-result :runtime-checks 0]
               altered-check)
              (assoc-in
               [:safety-result :outcomes 0]
               altered-outcome)
              (assoc-in
               [:identity-input :runtime-checks 0]
               altered-check)
              (assoc-in
               [:identity-input :outcomes 0]
               altered-outcome)))
        list-check
        (-> safe-core
            (assoc-in
             [:safety-result :runtime-checks]
             (list check))
            (assoc-in
             [:identity-input :runtime-checks]
             (list check)))]
    (doseq [[label altered]
            [[:nil-check nil-check]
             [:altered-condition altered-condition]
             [:coordinated-invalid-condition
              coordinated-invalid-condition]
             [:coordinated-statically-safe-division
              coordinated-statically-safe-division]
             [:list-check list-check]]]
      (testing (name label)
        (let [products
              {:safe-core altered
               :verification
               (-> (:verification upstream)
                   (assoc :expected altered)
                   (assoc :candidate altered))}
              request
              ((resolved-value sh12-namespace
                               'sh12-request)
               products ".gravity" :runtime-checked
               (str "/checkout-a/sh12-runtime-"
                    (name label) ".gravity"))
              checked-core
              (invoke-sh12
               'bridge-plan
               'sh12-build-checked-core
               [request])]
          (is (= :rejected (:status checked-core)))
          (is (= "STD12-BRIDGE-LINEAGE"
                 (get-in checked-core
                         [:diagnostics 0 :rule]))))))))

(deftest sh12-runtime-checked-guard-alterations-fail-closed
  (let [run (runtime-run ".gravity")
        {:keys
         [mir check-id token-id function entry-id block]}
        (runtime-parts run)
        instructions (:instructions block)
        entrypoint (:name function)
        reordered
        (assoc-in
         mir
         [:functions entrypoint :blocks entry-id
          :instructions]
         (vec
          (concat
           (rest instructions)
           [(first instructions)])))
        missing-edge
        (assoc-in mir [:data-flow-graph :edges] [])
        missing-check
        (assoc mir :runtime-check-table {})
        altered-effect
        (assoc-in
         mir
         [:functions entrypoint :latent-effects]
         #{})
        altered-token
        (assoc-in
         mir
         [:runtime-check-table check-id
          :token-value-id]
         (str token-id ":altered"))
        bind
        (fn [candidate]
          (invoke-sh12
           'bridge-plan
           'sh12-bind-authenticated-mir
           [(:request run)
            (:checked-core run)
            candidate
            (assoc (:mir-verification run)
                   :mir-module candidate)
            (:mir-id run)]))]
    (doseq [[label candidate]
            [[:operation-order reordered]
             [:guard-edge missing-edge]
             [:runtime-check missing-check]
             [:effect-lineage altered-effect]
             [:token-lineage altered-token]]]
      (testing (name label)
        (let [result (bind candidate)]
          (is (= :rejected (:status result)))
          (is (= "STD12-BRIDGE-MIR"
                 (get-in result
                         [:diagnostics 0 :rule])))
          (is (= :invalid-c11-result
                 (get-in result
                         [:diagnostics 0 :facts
                          :reason]))))))))

(deftest sh12-runtime-checked-policy-exposes-the-remaining-verifier-boundary
  (let [policy
        (invoke-sh12
         'bridge-plan
         'sh12-authenticated-mir-bridge-policy
         [])]
    (is (= [:proven-safe :runtime-checked
            :unsafe-island]
           (:accepted-outcomes policy)))
    (is (some
         #{:independent-c11-semantic-verifier}
         (:pending policy)))
    (is (some #{:sh12-completion}
              (:pending policy)))))
