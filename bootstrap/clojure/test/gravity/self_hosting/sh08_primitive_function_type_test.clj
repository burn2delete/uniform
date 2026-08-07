(ns gravity.self-hosting.sh08-primitive-function-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (loop [candidate (java.io.File. ".")]
    (if (.isFile (io/file candidate "deps.edn"))
      (.toPath candidate)
      (let [parent (.getParentFile candidate)]
        (if (nil? parent)
          (throw (ex-info "repository root is unavailable"
                          {:id "STD08-PRIMITIVE-FUNCTION-ROOT"}))
          (recur parent))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private c7-plan
  (delay
    (let [source-path (str (.resolve @root c7-relative-path))
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(defn- invoke-c7 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-primitive-function-type
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- node
  [node-id core-form children attributes owner]
  {:node-id node-id
   :core-form core-form
   :children children
   :attributes attributes
   :evaluation {:owner-function-syntax-id owner}
   :source {:syntax-id node-id
            :semantic-span {:line 1 :column 1 :length 1}
            :origin-chain []
            :generated-origin nil}})

(defn- primitive-ho-carrier
  ([literal-kind]
   (let [identity-syntax :primitive-identity-syntax
         apply-syntax :primitive-apply-syntax
         identity-definition-syntax :primitive-identity-definition-syntax
         apply-definition-syntax :primitive-apply-definition-syntax
         identity-function-node :primitive-identity-function-node
         identity-body-node :primitive-identity-body-node
         identity-parameter-node :primitive-identity-parameter-node
         apply-function-node :primitive-apply-function-node
         apply-body-node :primitive-apply-body-node
         inner-operator-node :primitive-inner-operator-node
         inner-value-node :primitive-inner-value-node
         inner-call-node apply-body-node
         outer-operator-node :primitive-outer-operator-node
         outer-identity-node :primitive-outer-identity-node
         outer-value-node :primitive-outer-value-node
         outer-call-node :primitive-outer-call-node
         identity-definition-binding :primitive-identity-definition-binding
         identity-parameter-binding :primitive-identity-parameter-binding
         apply-definition-binding :primitive-apply-definition-binding
         apply-callable-binding :primitive-apply-callable-binding
         apply-value-binding :primitive-apply-value-binding
         nodes
         [(node identity-function-node :fn [identity-body-node] {}
                identity-syntax)
          (node identity-body-node :reference [identity-parameter-node]
                {:binding-id identity-parameter-binding} identity-syntax)
          (node identity-parameter-node :reference []
                {:binding-id identity-parameter-binding} identity-syntax)
          (node apply-function-node :fn [apply-body-node] {}
                apply-syntax)
          (node apply-body-node :call
                [inner-operator-node inner-value-node] {} apply-syntax)
          (node inner-operator-node :reference []
                {:binding-id apply-callable-binding} apply-syntax)
          (node inner-value-node :reference []
                {:binding-id apply-value-binding} apply-syntax)
          (node outer-operator-node :reference []
                {:binding-id apply-definition-binding} nil)
          (node outer-identity-node :reference []
                {:binding-id identity-definition-binding} nil)
          (node outer-value-node :literal []
                {:literal-kind literal-kind} nil)
          (node outer-call-node :call
                [outer-operator-node outer-identity-node outer-value-node]
                {} nil)]
         definitions
         [{:binding-id identity-definition-binding
           :syntax-id identity-definition-syntax
           :value-node-id identity-function-node}
          {:binding-id apply-definition-binding
           :syntax-id apply-definition-syntax
           :value-node-id apply-function-node}]
         function-records
         [{:ordinal 0
           :function-core-node-id identity-function-node
           :function-form-id :primitive-identity-form
           :function-syntax-id identity-syntax
           :body-core-node-id identity-body-node
           :parameter-binding-ids [identity-parameter-binding]
           :fixed-arity 1
           :definition-kind :named-top-level
           :definition-binding-id identity-definition-binding
           :definition-core-node-id :primitive-identity-definition-node
           :definition-name 'primitive-identity}
          {:ordinal 1
           :function-core-node-id apply-function-node
           :function-form-id :primitive-apply-form
           :function-syntax-id apply-syntax
           :body-core-node-id apply-body-node
           :parameter-binding-ids
           [apply-callable-binding apply-value-binding]
           :fixed-arity 2
           :definition-kind :named-top-level
           :definition-binding-id apply-definition-binding
           :definition-core-node-id :primitive-apply-definition-node
           :definition-name 'primitive-apply}]
         calls
         [{:core-node-id inner-call-node
           :operator-binding-id apply-callable-binding
           :argument-node-ids [inner-value-node]
           :ordered-evaluation-node-ids
           [inner-operator-node inner-value-node]
           :evaluation-order :operator-then-arguments}
          {:core-node-id outer-call-node
           :operator-binding-id apply-definition-binding
           :argument-node-ids [outer-identity-node outer-value-node]
           :ordered-evaluation-node-ids
           [outer-operator-node outer-identity-node outer-value-node]
           :evaluation-order :operator-then-arguments}]
         edges
         [{:ordinal 0
           :call-core-node-id inner-call-node
           :caller-function-syntax-id apply-syntax
           :caller-function-core-node-id apply-function-node
           :callee-binding-id apply-callable-binding
           :callee-definition-syntax-id apply-definition-syntax
           :callee-function-syntax-id nil
           :callee-function-core-node-id nil
           :argument-core-node-ids [inner-value-node]
           :ordered-evaluation-node-ids
           [inner-operator-node inner-value-node]
           :evaluation-order :operator-then-arguments
           :classification :nonlocal-or-nonfunction}
          {:ordinal 1
           :call-core-node-id outer-call-node
           :caller-function-syntax-id nil
           :caller-function-core-node-id nil
           :callee-binding-id apply-definition-binding
           :callee-definition-syntax-id apply-definition-syntax
           :callee-function-syntax-id apply-syntax
           :callee-function-core-node-id apply-function-node
           :argument-core-node-ids [outer-identity-node outer-value-node]
           :ordered-evaluation-node-ids
           [outer-operator-node outer-identity-node outer-value-node]
           :evaluation-order :operator-then-arguments
           :classification :local-function}]
         bindings
         [{:binding-id identity-definition-binding
           :definition-syntax-id identity-definition-syntax}
          {:binding-id identity-parameter-binding
           :definition-syntax-id identity-definition-syntax}
          {:binding-id apply-definition-binding
           :definition-syntax-id apply-definition-syntax}
          {:binding-id apply-callable-binding
           :definition-syntax-id apply-definition-syntax}
          {:binding-id apply-value-binding
           :definition-syntax-id apply-definition-syntax}]
         module {:effects #{}
                 :capabilities #{}
                 :profile :meta
                 :target :jvm}]
     {:nodes nodes
      :definitions definitions
      :function-records function-records
      :calls calls
      :edges edges
      :call-edges edges
      :recursion-components []
      :lexical-bindings []
      :bindings bindings
      :module module
      :artifact-id :primitive-ho-artifact
      :provenance {:source-path "synthetic"}})))

(defn- complete-ho [carrier proof]
  (let [records (:function-records carrier)]
    (into {}
          (map
           (fn [record]
             (let [skeleton
                   (invoke-c7 'sh08-ft-function-type-skeleton
                              [record (:module carrier)])
                   proof-type
                   (if (= (:function-syntax-id record)
                          (:identity-function-syntax-id proof))
                     (:identity-function-type proof)
                     (:apply-function-type proof))]
               [(:function-syntax-id record)
                (invoke-c7 'sh08-ft-higher-order-complete-function
                           [skeleton proof-type])]))
           records))))

(deftest sh08-primitive-family-structure-and-fixture-parity
  (let [artifact-function
        (get-in @c7-plan [:functions 'sh08-ft-function-type-core-artifact])
        instructions (:instructions artifact-function)
        top-level (first instructions)
        invalid-lets
        (filter #(and (map? %)
                      (= :let (:op %))
                      (not= 1 (count (:body %))))
                (tree-seq coll? seq artifact-function))
        source
        (slurp (str (.resolve @root c7-relative-path)))
        function-calls
        (filter #(and (map? %)
                      (= :function-call (:op %)))
                (tree-seq coll? seq artifact-function))]
    (is (= 1 (count instructions)))
    (is (= :if (:op top-level)))
    (is (empty? invalid-lets))
    (is (some #(= 'sh08-ft-higher-order-proof (:function %)) function-calls))
    (is (some #(= 'sh08-ft-infer-acyclic-with-context (:function %))
              function-calls))
    (is (.contains source "sh08-ft-authoritative-primitive-type-for-node"))
    (is (.contains source ":unsupported-higher-order-primitive-type")))
  (doseq [fixture ["function-value-typed-bool"
                   "function-self-recursive-string-type"]]
    (let [gravity
          (java.nio.file.Files/readAllBytes
           (.resolve @root
                     (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/"
                          fixture ".gravity")))
          qst
          (java.nio.file.Files/readAllBytes
           (.resolve @root
                     (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/"
                          fixture ".qst")))]
      (is (pos? (alength gravity)) fixture)
      (is (java.util.Arrays/equals gravity qst) fixture))))

(deftest sh08-primitive-family-ho-diagonal
  (doseq [[literal-kind expected-type]
          [[:integer :gravity.type/integer]
           [:boolean :gravity.type/bool]
           [:string :gravity.type/string]]]
    (let [carrier (primitive-ho-carrier literal-kind)
          proof
          (invoke-c7 'sh08-ft-higher-order-proof
                     [(:nodes carrier) (:definitions carrier)
                      (:bindings carrier) (:function-records carrier)
                      (:calls carrier) (:edges carrier) (:module carrier)])
          complete (complete-ho carrier proof)
          node-table (into {} (map (fn [n] [(:node-id n) n])
                                   (:nodes carrier)))
          inference
          (invoke-c7 'sh08-ft-infer-acyclic-with-context
                     [(:nodes carrier) node-table [] (:definitions carrier)
                      (:calls carrier) (:edges carrier)
                      complete (:initial-binding-types proof) 64 proof])
          call-facts
          (invoke-c7 'sh08-ft-call-facts-with-context
                     [(:calls carrier) (:edges carrier) node-table
                      (:type-table inference) complete (:module carrier)
                      proof])
          identity-type (:identity-function-type proof)
          apply-type (:apply-function-type proof)
          input (invoke-c7 'sh08-ft-higher-order-proof-input [proof])]
      (is (= :accepted (:status proof)) (str literal-kind))
      (is (= expected-type (:value-type proof)) (str literal-kind))
      (is (= expected-type (get-in identity-type [:parameters 0 :type])))
      (is (= expected-type (:return identity-type)))
      (is (= identity-type
             (get-in apply-type [:parameters 0 :type])))
      (is (= expected-type (get-in apply-type [:parameters 1 :type])))
      (is (= expected-type (:return apply-type)))
      (is (= identity-type
             (get-in (:initial-binding-types proof)
                     [:primitive-identity-definition-binding])))
      (is (= expected-type
             (get-in complete
                     [:primitive-identity-syntax :parameters 0 :type])))
      (is (= expected-type
             (get-in complete
                     [:primitive-apply-syntax :parameters 1 :type])))
      (is (= :converged (:convergence-status inference)))
      (is (= [] (:diagnostics inference)))
      (is (= expected-type
             (get (:type-table inference) :primitive-apply-body-node)))
      (is (= expected-type
             (get (:type-table inference) :primitive-outer-call-node)))
      (is (= expected-type
             (get-in call-facts [0 :result-type])))
      (is (= expected-type
             (get-in call-facts [1 :result-type])))
      (is (= expected-type (:identity-parameter-type input)))
      (is (= expected-type (:apply-value-parameter-type input)))
      (is (= expected-type (:inner-result-type input)))
      (is (= expected-type (:outer-result-type input)))
      (is (not (contains? input :value-type)))
      (is (= literal-kind (:outer-value-literal-kind input))))))

(deftest sh08-primitive-family-ho-unsupported-is-explicit
  (let [carrier (primitive-ho-carrier :decimal)
        proof
        (invoke-c7 'sh08-ft-higher-order-proof
                   [(:nodes carrier) (:definitions carrier)
                    (:bindings carrier) (:function-records carrier)
                    (:calls carrier) (:edges carrier) (:module carrier)])]
    (is (= :rejected (:status proof)))
    (is (= :unsupported-higher-order-primitive-type (:reason proof)))
    (is (= :primitive-outer-value-node (:node-id (:node proof))))
    (is (= :decimal (:actual proof)))))

(deftest sh08-primitive-family-ho-mutation-is-not-silent
  (doseq [[label overrides expected]
          [["operator-order"
            [[[:calls 1 :evaluation-order] :arguments-then-operator]]
            :no-complete-identity-apply-shape]
           ["mixed-literal-kind"
            [[[:nodes 9 :attributes :literal-kind] :keyword]]
            :unsupported-higher-order-primitive-type]]]
    (let [carrier (reduce (fn [value [path replacement]]
                            (assoc-in value path replacement))
                          (primitive-ho-carrier :integer)
                          overrides)
          proof
          (invoke-c7 'sh08-ft-higher-order-proof
                     [(:nodes carrier) (:definitions carrier)
                      (:bindings carrier) (:function-records carrier)
                      (:calls carrier) (:edges carrier) (:module carrier)])]
      (is (= expected (:reason proof)) label))))

(deftest sh08-primitive-family-authenticated-bool-gravity-boundary
  ;; This is an independently selectable authenticated boundary.  It is
  ;; intentionally not part of the cheap pure batch and never builds .qst.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact (deref (ns-resolve namespace 'fixture-artifact))
        function-request (deref (ns-resolve namespace 'function-request))
        artifact (fixture-artifact "accepted" "function-value-typed-bool"
                                   ".gravity")
        request (function-request artifact)
        result (invoke-c7 'sh08-function-type-core-artifact [request])
        proof (:higher-order-proof result)
        identity-id (:identity-function-syntax-id proof)
        apply-id (:apply-function-syntax-id proof)
        identity-entry
        (some #(when (= identity-id (:function-id %)) %)
              (:function-type-table result))
        apply-entry
        (some #(when (= apply-id (:function-id %)) %)
              (:function-type-table result))
        inner-id (:inner-call-core-node-id proof)
        outer-id (:outer-call-core-node-id proof)
        inner-fact
        (some #(when (= inner-id (:call-core-node-id %)) %)
              (:call-type-facts result))
        outer-fact
        (some #(when (= outer-id (:call-core-node-id %)) %)
              (:call-type-facts result))
        source-edges (get-in request [:canonical-core-artifact :call-edges])
        source-inner-edge
        (some #(when (= inner-id (:call-core-node-id %)) %) source-edges)
        source-outer-edge
        (some #(when (= outer-id (:call-core-node-id %)) %) source-edges)
        pending
        [:recursive-type-annotations
         :captures :lifetimes :aliases :multi-hop :polymorphism :overloads
         :multi-arity :variadic-dispatch :protocol-dispatch :dynamic-dispatch
         :reflection :records :unions :protocols :generics :casts
         :dynamic-boundaries]
        verification
        (invoke-c7 'sh08-verify-function-type-result [request result])
        altered-candidates
        [(assoc-in result [:function-type-table 0 :return]
                   :gravity.type/string)
         (assoc-in result [:higher-order-proof :outer-edge :classification]
                   :nonlocal-or-nonfunction)
         (assoc-in result
                   [:identity-input :higher-order-proof :outer-edge
                    :classification]
                   :nonlocal-or-nonfunction)
         (assoc-in result [:constraint-ledger 0 :status] :rejected)]]
    (is (= :accepted (:status result)))
    (is (= :capture-free-higher-order-fixed-arity-one-hop (:scope result)))
    (is (= :gravity/sh08-authoritative-higher-order-proof-v1
           (:domain proof)))
    (is (= :boolean (:outer-value-literal-kind proof)))
    (is (= :gravity.type/bool
           (get-in (:identity-function-type proof) [:parameters 0 :type])))
    (is (= :gravity.type/bool (:return (:identity-function-type proof))))
    (is (= identity-entry
           (:identity-function-type proof)))
    (is (= apply-entry (:apply-function-type proof)))
    (is (= :gravity.type/bool
           (get-in apply-entry [:parameters 1 :type])))
    (is (= :gravity.type/bool (:return apply-entry)))
    (is (= (:identity-parameter-binding-id proof)
           (get-in identity-entry [:parameters 0 :binding-id])))
    (is (= (:apply-callable-binding-id proof)
           (get-in apply-entry [:parameters 0 :binding-id])))
    (is (= (:apply-value-binding-id proof)
           (get-in apply-entry [:parameters 1 :binding-id])))
    (is (= :gravity.type/bool (:identity-parameter-type proof)))
    (is (= :gravity.type/bool (:apply-value-parameter-type proof)))
    (is (= :gravity.type/bool (:inner-result-type proof)))
    (is (= :gravity.type/bool (:outer-result-type proof)))
    (is (= identity-entry
           (get-in apply-entry [:parameters 0 :type])))
    (is (= :gravity.type/bool (get (:type-table result) inner-id)))
    (is (= :gravity.type/bool (get (:type-table result) outer-id)))
    (is (= :gravity.type/bool (:result-type inner-fact)))
    (is (= :gravity.type/bool (:result-type outer-fact)))
    (is (= :inner-indirect-call
           (get-in result [:higher-order-call-facts 0 :role])))
    (is (= :outer-direct-call
           (get-in result [:higher-order-call-facts 1 :role])))
    (is (= :indirect-function-value
           (get-in result [:higher-order-call-facts 0 :dispatch])))
    (is (= :direct-local-function
           (get-in result [:higher-order-call-facts 1 :dispatch])))
    (is (= (:b47-edge inner-fact)
           (get-in result [:higher-order-call-facts 0 :b47-edge])))
    (is (= (:b47-edge outer-fact)
           (get-in result [:higher-order-call-facts 1 :b47-edge])))
    (is (= (select-keys source-inner-edge
                        [:ordinal :call-core-node-id
                         :caller-function-syntax-id
                         :caller-function-core-node-id :callee-binding-id
                         :callee-definition-syntax-id
                         :callee-function-syntax-id
                         :callee-function-core-node-id
                         :argument-core-node-ids
                         :ordered-evaluation-node-ids :evaluation-order
                         :classification])
           (get-in proof [:inner-edge])))
    (is (= (select-keys source-outer-edge
                        [:ordinal :call-core-node-id
                         :caller-function-syntax-id
                         :caller-function-core-node-id :callee-binding-id
                         :callee-definition-syntax-id
                         :callee-function-syntax-id
                         :callee-function-core-node-id
                         :argument-core-node-ids
                         :ordered-evaluation-node-ids :evaluation-order
                         :classification])
           (get-in proof [:outer-edge])))
    (is (= pending (:pending result)))
    (is (= (:constraint-ledger result)
           (get-in result [:typed-core :constraints])
           (get-in result [:identity-input :constraint-ledger])))
    (is (= proof (get-in result [:typed-core :higher-order-proof])))
    (is (= proof (get-in result [:identity-input :higher-order-proof])))
    (is (= (get-in request [:canonical-core-artifact :provenance :source-path])
           (get-in result [:provenance :source-path])))
    (is (contains? (:provenance result) :actual-source-path))
    (is (= :passed (:status verification)))
    (doseq [candidate altered-candidates]
      (is (= :rejected
             (:status
              (invoke-c7 'sh08-verify-function-type-result
                         [request candidate])))))))
