(ns gravity.self-hosting.sh08-recursive-function-type-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_recursive_function_type_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 recursive function type test source is absent"
                {:id "STD08-C7-RECURSIVE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "STD08-C7-RECURSIVE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
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
   {:engine :gravity-sh08-recursive-function-type
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

(defn- recursive-carrier
  ([] (recursive-carrier {}))
  ([overrides]
   (let [function-syntax :recursive-function-syntax
         definition-syntax :recursive-definition-syntax
         function-node :recursive-function-node
         body-node :recursive-body-node
         condition-node :recursive-condition-node
         call-node :recursive-call-node
         operator-node :recursive-operator-node
         parameter-node :recursive-parameter-node
         base-node :recursive-base-node
         external-call-node :recursive-external-call-node
         external-operator-node :recursive-external-operator-node
         external-literal-node :recursive-external-literal-node
         definition-binding :recursive-definition-binding
         parameter-binding :recursive-parameter-binding
         nodes
         [(node function-node :fn [body-node] {} function-syntax)
          (node body-node :if [condition-node call-node base-node] {}
                function-syntax)
          (node condition-node :literal [] {:literal-kind :boolean}
                function-syntax)
          (node operator-node :reference []
                {:binding-id definition-binding} function-syntax)
          (node parameter-node :reference []
                {:binding-id parameter-binding} function-syntax)
          (node call-node :call [operator-node parameter-node] {}
                function-syntax)
          (node base-node :literal [] {:literal-kind :integer}
                function-syntax)
          (node external-operator-node :reference []
                {:binding-id definition-binding} nil)
          (node external-literal-node :literal [] {:literal-kind :integer} nil)
          (node external-call-node :call
                [external-operator-node external-literal-node] {} nil)]
         definitions
         [{:binding-id definition-binding
           :syntax-id definition-syntax
           :value-node-id function-node}]
         function-records
         [{:ordinal 0
           :function-core-node-id function-node
           :function-form-id :recursive-function-form
           :function-syntax-id function-syntax
           :body-core-node-id body-node
           :parameter-binding-ids [parameter-binding]
           :fixed-arity 1
           :definition-kind :named-top-level
           :definition-binding-id definition-binding
           :definition-core-node-id :recursive-definition-node
           :definition-name 'self-recursive-type}]
         calls
         [{:core-node-id call-node
           :operator-binding-id definition-binding
           :argument-node-ids [parameter-node]
           :ordered-evaluation-node-ids [operator-node parameter-node]
           :evaluation-order :operator-then-arguments}
          {:core-node-id external-call-node
           :operator-binding-id definition-binding
           :argument-node-ids [external-literal-node]
           :ordered-evaluation-node-ids
           [external-operator-node external-literal-node]
           :evaluation-order :operator-then-arguments}]
         edges
         [{:ordinal 0
           :call-core-node-id call-node
           :caller-function-syntax-id function-syntax
           :caller-function-core-node-id function-node
           :callee-binding-id definition-binding
           :callee-definition-syntax-id definition-syntax
           :callee-function-syntax-id function-syntax
           :callee-function-core-node-id function-node
           :argument-core-node-ids [parameter-node]
           :ordered-evaluation-node-ids [operator-node parameter-node]
           :evaluation-order :operator-then-arguments
           :classification :local-function}
          {:ordinal 1
           :call-core-node-id external-call-node
           :caller-function-syntax-id nil
           :caller-function-core-node-id nil
           :callee-binding-id definition-binding
           :callee-definition-syntax-id definition-syntax
           :callee-function-syntax-id function-syntax
           :callee-function-core-node-id function-node
           :argument-core-node-ids [external-literal-node]
           :ordered-evaluation-node-ids
           [external-operator-node external-literal-node]
           :evaluation-order :operator-then-arguments
           :classification :local-function}]
         bindings
         [{:binding-id definition-binding
           :definition-syntax-id definition-syntax}
          {:binding-id parameter-binding
           :definition-syntax-id definition-syntax}]
         component
         {:ordinal 0
          :kind :self-recursive
          :function-syntax-ids [function-syntax]
          :function-core-node-ids [function-node]
          :internal-call-edge-ordinals [0]}
         module {:effects #{}
                 :capabilities #{}
                 :profile :meta
                 :target :jvm}
         base {:nodes nodes
               :definitions definitions
               :function-records function-records
               :calls calls
               :edges edges
               :call-edges edges
               :recursion-components [component]
               :lexical-bindings []
               :bindings bindings
               :module module
               :artifact-id :recursive-artifact
               :provenance {:source-path "synthetic"}}]
     (loop [remaining overrides result base]
       (if (= (count remaining) 0)
         result
         (recur (rest remaining)
                (assoc-in result (first (first remaining))
                          (second (first remaining)))))))))

(defn- recursive-proof [carrier]
  (invoke-c7
   'sh08-ft-recursive-proof
   [(:nodes carrier) (:definitions carrier) (:bindings carrier)
    (:function-records carrier) (:calls carrier) (:edges carrier)
    (:recursion-components carrier) (:module carrier)]))

(deftest sh08-recursive-source-reachability-and-structure
  (let [artifact-function
        (get-in @c7-plan [:functions 'sh08-ft-function-type-core-artifact])
        recursive-function
        (get-in @c7-plan [:functions 'sh08-ft-recursive-proof])
        calls
        (filter
         #(and (map? %)
               (= :function-call (:op %)))
         (tree-seq coll? seq artifact-function))
        inference-call
        (first
         (filter
          #(= 'sh08-ft-infer-acyclic-with-context (:function %))
          calls))
        instructions (:instructions artifact-function)
        top-level (first instructions)
        invalid-lets
        (filter #(and (map? %)
                      (= :let (:op %))
                      (not= 1 (count (:body %))))
                (tree-seq coll? seq artifact-function))]
    (is (= 1 (count instructions)))
    (is (= :if (:op top-level)))
    (is (empty? invalid-lets))
    (is (map? recursive-function))
    (is (map? (get-in @c7-plan [:functions
                                'sh08-ft-recursive-complete-function])))
    (is (map? (get-in @c7-plan [:functions
                                'sh08-ft-recursive-call-proof-facts])))
    (is (map? inference-call))
    (is (= :if (get-in inference-call [:args 9 :op])))
    (is (= :local (get-in inference-call [:args 9 :then :op])))
    (is (= 'recursive-proof
           (get-in inference-call [:args 9 :then :name]))))
  (doseq [name ["sh08-ft-recursive-proof"
                "sh08-ft-recursive-pending"
                "sh08-ft-recursive-constraint-ledger"
                "sh08-ft-recursive-result-fields"]]
    (is (.contains
         (slurp (str (.resolve @root c7-relative-path))) name)
        name)))

(deftest sh08-recursive-fixture-pair-is-byte-identical
  (let [gravity
        (slurp (str (.resolve @root
                              (str fixture-root
                                   "/accepted/function-self-recursive-type.gravity"))))
        qst
        (slurp (str (.resolve @root
                              (str fixture-root
                                   "/accepted/function-self-recursive-type.qst"))))]
    (is (pos? (count gravity)))
    (is (= gravity qst))))

(deftest sh08-recursive-pure-positive-and-monotone-fixed-point
  (let [carrier (recursive-carrier)
        proof (recursive-proof carrier)
        skeleton
        (invoke-c7 'sh08-ft-function-type-skeleton
                   [(first (:function-records carrier)) (:module carrier)])
        complete
        (invoke-c7 'sh08-ft-recursive-complete-function
                   [skeleton proof])
        node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
        inference
        (invoke-c7
         'sh08-ft-infer-acyclic-with-context
         [(:nodes carrier) node-table [] (:definitions carrier)
          (:calls carrier) (:edges carrier)
          {(:function-id complete) complete}
          {(get-in proof [:definition :binding-id]) complete}
          64 proof])
        permuted (assoc carrier :nodes (vec (reverse (:nodes carrier))))
        permuted-proof (recursive-proof permuted)
        permuted-skeleton
        (invoke-c7 'sh08-ft-function-type-skeleton
                   [(first (:function-records permuted)) (:module permuted)])
        permuted-complete
        (invoke-c7 'sh08-ft-recursive-complete-function
                   [permuted-skeleton permuted-proof])
        permuted-node-table
        (into {} (map (fn [n] [(:node-id n) n]) (:nodes permuted)))
        permuted-inference
        (invoke-c7
         'sh08-ft-infer-acyclic-with-context
         [(:nodes permuted) permuted-node-table [] (:definitions permuted)
          (:calls permuted) (:edges permuted)
          {(:function-id permuted-complete) permuted-complete}
          {(get-in permuted-proof [:definition :binding-id])
           permuted-complete}
          64 permuted-proof])
        canonical-call-facts
        (invoke-c7
         'sh08-ft-call-facts
         [(:calls carrier) (:edges carrier) node-table
          (:type-table inference)
          {(:function-id complete) complete}
          (:module carrier)])
        ledger
        (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])
        recursive-fields
        (invoke-c7 'sh08-ft-recursive-result-fields
                   [{:status :accepted} proof canonical-call-facts])]
    (is (= :accepted (:status proof)))
    (is (= :gravity.type/integer (:base-type proof)))
    (is (= [:gravity.type/integer] (:parameter-types proof)))
    (is (= :gravity.type/integer (get-in complete [:parameters 0 :type])))
    (is (= :gravity.type/integer (:return complete)))
    (is (= :pending-sh10 (:ownership-constraints complete)))
    (is (= [:pending-sh09] (:thrown-error-effects complete)))
    (is (= 1 (count ledger)))
    (is (= :recursive-fixed-point (get-in ledger [0 :kind])))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1
           (get-in recursive-fields [:recursive-proof :domain])))
    (is (= :direct-named-self-recursion
           (get-in recursive-fields [:recursive-call-facts 0 :dispatch])))
    (is (= :recursive-call-node
           (get-in recursive-fields [:recursive-call-facts 0 :syntax-id])))
    (is (= :meta
           (get-in recursive-fields [:recursive-call-facts 0 :profile])))
    (is (= :external-parameter-evidence
           (get-in recursive-fields [:recursive-call-facts 1 :role])))
    (is (= :recursive-external-call-node
           (get-in recursive-fields [:recursive-call-facts 1 :syntax-id])))
    (let [proof-input (invoke-c7 'sh08-ft-recursive-proof-input [proof])]
      (is (= (get (:edges carrier) 0)
             (:recursive-edge proof-input)))
      (is (= (get (:edges carrier) 1)
             (get-in proof-input [:parameter-evidence-call :edge])))
      (is (not= (:recursive-edge proof-input)
                (get-in proof-input [:parameter-evidence-call :edge])))
      (is (= :recursive-definition-binding
             (:recursive-definition-binding-id proof-input))))
    (is (not (contains?
              (set (invoke-c7 'sh08-ft-recursive-pending
                              [[:recursive-type-annotations :records]]))
              :recursive-type-annotations)))
    (is (= :gravity.type/unknown
           (invoke-c7 'sh08-ft-if-join-with-context
                      [[:unrelated-condition :unrelated-then
                        :unrelated-else]
                       {} proof])))
    (is (= :converged (:convergence-status inference)))
    (is (= [] (:diagnostics inference)))
    (is (= :gravity.type/integer
           (get (:type-table inference) :recursive-call-node)))
    (is (= :gravity.type/integer
           (get (:type-table inference) :recursive-body-node)))
    (is (= :gravity.type/integer
           (get-in (:binding-types inference)
                   [:recursive-parameter-binding])))
    (is (= :accepted (:status permuted-proof)))
    (is (= :converged (:convergence-status permuted-inference)))
    (is (= [] (:diagnostics permuted-inference)))
    (is (= :gravity.type/integer
           (get (:type-table permuted-inference) :recursive-body-node)))
    (is (= (:type-table inference) (:type-table permuted-inference)))))

(deftest sh08-recursive-pure-hostile-matrix
  (doseq [[label overrides expected]
          [["mutual-scc"
            [[[:recursion-components 0 :kind] :mutually-recursive]]
            :mutual-recursion-unsupported]
           ["multiple-components"
            [[[:recursion-components]
              [{:kind :self-recursive
                :function-syntax-ids [:recursive-function-syntax]
                :internal-call-edge-ordinals [0]}
               {:kind :self-recursive
                :function-syntax-ids [:other-function-syntax]
                :internal-call-edge-ordinals [0]}]]]
            :multiple-recursive-components]
           ["zero-arity"
            [[[:function-records 0 :fixed-arity] 0]]
            :recursive-fixed-arity-required]
           ["no-base"
            [[[:nodes 6 :core-form] :reference]
             [[:nodes 6 :attributes :binding-id] :recursive-parameter-binding]]
            :recursive-base-evidence-required]
           ["edge-lineage"
            [[[:edges 0 :callee-definition-syntax-id] :wrong-definition-syntax]]
            :recursive-call-edge-lineage]
           ["order-tamper"
            [[[:calls 0 :evaluation-order] :arguments-then-operator]]
            :recursive-call-edge-lineage]
           ["argument-count"
            [[[:calls 0 :argument-node-ids]
              [:recursive-parameter-node :recursive-parameter-node]]
             [[:calls 0 :ordered-evaluation-node-ids]
              [:recursive-operator-node :recursive-parameter-node
               :recursive-parameter-node]]
             [[:edges 0 :argument-core-node-ids]
              [:recursive-parameter-node :recursive-parameter-node]]
             [[:edges 0 :ordered-evaluation-node-ids]
              [:recursive-operator-node :recursive-parameter-node
               :recursive-parameter-node]]]
            :recursive-argument-shape]
           ["transformed-argument"
            [[[:nodes 4 :core-form] :literal]
             [[:nodes 4 :attributes :literal-kind] :integer]]
            :recursive-argument-shape]
           ["capture"
            [[[:nodes 2 :core-form] :reference]
             [[:nodes 2 :attributes :binding-id] :captured-binding]]
            :recursive-capture-unsupported]
           ["argument-binding"
            [[[:nodes 4 :attributes :binding-id] :captured-binding]]
            :recursive-argument-shape]
           ["result-conflict"
            [[[:nodes 6 :attributes :literal-kind] :string]]
            :recursive-result-type-conflict]]]
    (let [proof (recursive-proof (recursive-carrier overrides))]
      (is (= :rejected (:status proof)) label)
      (is (= expected (:reason proof)) label))))

(deftest sh08-recursive-unsupported-external-primitive-keeps-evidence
  (let [proof
        (recursive-proof
         (recursive-carrier
          [[[:nodes 8 :attributes :literal-kind] :decimal]]))
        offending-node (:node proof)]
    (is (= :rejected (:status proof)))
    (is (= :unsupported-recursive-primitive-type (:reason proof)))
    (is (= :recursive-external-literal-node (:node-id offending-node)))
    (is (= :recursive-external-literal-node
           (get-in offending-node [:source :syntax-id])))
    (is (= {:line 1 :column 1 :length 1}
           (get-in offending-node [:source :semantic-span])))
    (is (= [] (get-in offending-node [:source :origin-chain])))
    (is (nil? (get-in offending-node [:source :generated-origin])))))

(deftest sh08-recursive-primitive-family-diagonal-and-conflicts
  (let [primitive-kinds
        [[:integer :gravity.type/integer]
         [:boolean :gravity.type/bool]
         [:string :gravity.type/string]]]
    (doseq [[literal-kind expected-type] primitive-kinds]
      (let [carrier
            (recursive-carrier
             [[[:nodes 6 :attributes :literal-kind] literal-kind]
              [[:nodes 8 :attributes :literal-kind] literal-kind]])
            proof (recursive-proof carrier)
            skeleton
            (invoke-c7 'sh08-ft-function-type-skeleton
                       [(first (:function-records carrier)) (:module carrier)])
            complete
            (invoke-c7 'sh08-ft-recursive-complete-function
                       [skeleton proof])
            node-table
            (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
            inference
            (invoke-c7
             'sh08-ft-infer-acyclic-with-context
             [(:nodes carrier) node-table [] (:definitions carrier)
              (:calls carrier) (:edges carrier)
              {(:function-id complete) complete}
              {(get-in proof [:definition :binding-id]) complete}
              64 proof])
            call-facts
            (invoke-c7
             'sh08-ft-call-facts
             [(:calls carrier) (:edges carrier) node-table
              (:type-table inference)
              {(:function-id complete) complete}
              (:module carrier)])
            proof-input (invoke-c7 'sh08-ft-recursive-proof-input [proof])
            ledger (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])]
        (is (= :accepted (:status proof)) (str literal-kind))
        (is (= expected-type (:base-type proof)) (str literal-kind))
        (is (= [expected-type] (:parameter-types proof)) (str literal-kind))
        (is (= expected-type (get-in complete [:parameters 0 :type])))
        (is (= expected-type (:return complete)))
        (is (= :pending-sh10 (:ownership-constraints complete)))
        (is (= [:pending-sh09] (:thrown-error-effects complete)))
        (is (= :converged (:convergence-status inference)))
        (is (= [] (:diagnostics inference)))
        (is (= expected-type (get (:type-table inference)
                                  :recursive-call-node)))
        (is (= expected-type (get (:type-table inference)
                                  :recursive-body-node)))
        (is (= expected-type (get-in (:binding-types inference)
                                     [:recursive-parameter-binding])))
        (is (= expected-type (get-in call-facts [0 :result-type])))
        (is (= expected-type (get-in call-facts [1 :result-type])))
        (is (= (get (:edges carrier) 0)
               (:recursive-edge proof-input)))
        (is (= (get (:edges carrier) 1)
               (get-in proof-input [:parameter-evidence-call :edge])))
        (is (= :recursive-fixed-point (get-in ledger [0 :kind])))
        (is (= :meta (:recursive-call-profile proof-input)))
        (is (= :meta (get-in proof-input
                              [:parameter-evidence-call :profile])))))
    (doseq [[base-kind base-type]
            primitive-kinds
            [evidence-kind evidence-type]
            primitive-kinds
            :when (not= base-kind evidence-kind)]
      (let [proof
            (recursive-proof
             (recursive-carrier
              [[[:nodes 6 :attributes :literal-kind] base-kind]
               [[:nodes 8 :attributes :literal-kind] evidence-kind]]))]
        (is (= :rejected (:status proof)))
        (is (= :recursive-result-type-conflict (:reason proof)))
        (is (= base-type (:expected proof)))
        (is (= evidence-type (:actual proof)))))))

(deftest sh08-recursive-nonconvergence-is-precise
  (let [carrier (recursive-carrier)
        proof (recursive-proof carrier)
        skeleton
        (invoke-c7 'sh08-ft-function-type-skeleton
                   [(first (:function-records carrier)) (:module carrier)])
        complete
        (invoke-c7 'sh08-ft-recursive-complete-function
                   [skeleton proof])
        node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
        inference
        (invoke-c7
         'sh08-ft-infer-acyclic-with-context
         [(:nodes carrier) node-table [] (:definitions carrier)
          (:calls carrier) (:edges carrier)
          {(:function-id complete) complete}
          {(get-in proof [:definition :binding-id]) complete}
          0 proof])]
    (is (= :nonconverged (:convergence-status inference)))
    (is (= :bounded-function-type-inference-nonconvergence
           (get-in inference [:diagnostics 0 :reason])))))

(deftest sh08-recursive-authenticated-gravity-boundary
  ;; One .gravity carrier is the only expensive check in this namespace.  The
  ;; paired .qst file is covered by a separate release/parity lane.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact (deref (ns-resolve namespace 'fixture-artifact))
        function-request (deref (ns-resolve namespace 'function-request))
        artifact (fixture-artifact "accepted" "function-self-recursive-type"
                                   ".gravity")
        request (function-request artifact)
        result
        (invoke-c7 'sh08-function-type-core-artifact
                   [request])
        proof (:recursive-proof result)
        function-id (:function-syntax-id proof)
        function-entry
        (some #(when (= function-id (:function-id %)) %)
              (:function-type-table result))
        recursive-call-id (:recursive-call-core-node-id proof)
        evidence-call-id (:parameter-evidence-call-core-node-id proof)
        source-edges (get-in request [:canonical-core-artifact :call-edges])
        source-recursive-edge
        (some #(when (= recursive-call-id (:call-core-node-id %)) %) source-edges)
        source-evidence-edge
        (some #(when (= evidence-call-id (:call-core-node-id %)) %) source-edges)
        recursive-facts (:recursive-call-facts result)
        recursive-constraint-index
        (first
         (keep-indexed
          (fn [index constraint]
            (when (= :recursive-fixed-point (:kind constraint)) index))
          (:constraint-ledger result)))
        expected-pending
        [:mutual-recursion :unbounded-recursion :recursive-captures
         :recursive-higher-order :recursive-polymorphism
         :higher-order-functions :records :unions :protocols :generics
         :casts :dynamic-boundaries]
        altered-candidates
        [(assoc-in result [:recursive-proof :proof-bound] :altered-bound)
         (assoc-in result [:recursive-call-facts 0 :dispatch]
                   :altered-dispatch)
         (assoc-in result
                   [:constraint-ledger recursive-constraint-index :status]
                   :rejected)
         (assoc-in result [:pending 0] :altered-pending)
         (assoc result :scope :first-order-fixed-arity-functions-locals-calls)
         (assoc-in result [:typed-core :recursive-proof :base-type]
                   :gravity.type/string)
         (assoc-in result [:identity-input :recursive-proof :base-type]
                   :gravity.type/string)]]
    (is (= :accepted (:status result)))
    (is (= :bounded-named-self-recursive-positive-fixed-arity-positional-literal-base
           (:scope result)))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1 (:domain proof)))
    (is (= :gravity.type/integer (:base-type proof)))
    (is (= :gravity.type/integer (:return function-entry)))
    (is (= :gravity.type/integer
           (get-in function-entry [:parameters 0 :type])))
    (is (= :pending-sh10 (:ownership-constraints function-entry)))
    (is (= [:pending-sh09] (:thrown-error-effects function-entry)))
    (is (= :direct-named-self-recursion
           (get-in result [:recursive-call-facts 0 :dispatch])))
    (is (= [:self-recursive-call :external-parameter-evidence]
           (mapv :role recursive-facts)))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-recursive-edge])
           (:recursive-edge proof)
           (get-in recursive-facts [0 :b47-edge])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-evidence-edge])
           (get-in proof [:parameter-evidence-call :edge])
           (get-in recursive-facts [1 :external-edge])))
    (is (= (:recursive-call-source proof)
           (select-keys (first recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (select-keys (:parameter-evidence-call proof)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])
           (select-keys (second recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (not= (:recursive-edge proof)
              (get-in proof [:parameter-evidence-call :edge])))
    (is (= :operator-then-arguments
           (get-in proof [:recursive-edge :evaluation-order])))
    (is (= (:definition-binding-id proof)
           (:recursive-definition-binding-id proof)
           (get-in recursive-facts [0 :recursive-definition-binding-id])))
    (is (= :converged (get-in result [:convergence :status])))
    (is (= expected-pending (:pending result)
           (get-in result [:identity-input :pending])))
    (is (= (:constraint-ledger result)
           (get-in result [:typed-core :constraints])
           (get-in result [:identity-input :constraint-ledger])))
    (is (some #(= :recursive-fixed-point (:kind %))
              (:constraint-ledger result)))
    (is (integer? recursive-constraint-index))
    (is (= proof
           (get-in result [:typed-core :recursive-proof])
           (get-in result [:identity-input :recursive-proof])))
    (is (= recursive-facts
           (get-in result [:typed-core :recursive-call-facts])))
    (is (= :passed
           (:status
            (invoke-c7 'sh08-verify-function-type-result
                       [request result]))))
    (doseq [candidate altered-candidates]
      (is (= :rejected
             (:status
              (invoke-c7 'sh08-verify-function-type-result
                         [request candidate])))))))

(deftest sh08-recursive-authenticated-string-gravity-boundary
  ;; Independently selectable authenticated boundary for the String diagonal.
  ;; The paired .qst is byte-parity only and is not built here.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact (deref (ns-resolve namespace 'fixture-artifact))
        function-request (deref (ns-resolve namespace 'function-request))
        artifact (fixture-artifact "accepted"
                                   "function-self-recursive-string-type"
                                   ".gravity")
        request (function-request artifact)
        result (invoke-c7 'sh08-function-type-core-artifact [request])
        proof (:recursive-proof result)
        function-id (:function-syntax-id proof)
        function-entry
        (some #(when (= function-id (:function-id %)) %)
              (:function-type-table result))
        recursive-call-id (:recursive-call-core-node-id proof)
        evidence-call-id (:parameter-evidence-call-core-node-id proof)
        source-edges (get-in request [:canonical-core-artifact :call-edges])
        source-recursive-edge
        (some #(when (= recursive-call-id (:call-core-node-id %)) %)
              source-edges)
        source-evidence-edge
        (some #(when (= evidence-call-id (:call-core-node-id %)) %)
              source-edges)
        recursive-facts (:recursive-call-facts result)
        recursive-constraint-index
        (first
         (keep-indexed
          (fn [index constraint]
            (when (= :recursive-fixed-point (:kind constraint)) index))
          (:constraint-ledger result)))
        pending
        [:mutual-recursion :unbounded-recursion :recursive-captures
         :recursive-higher-order :recursive-polymorphism
         :higher-order-functions :records :unions :protocols :generics
         :casts :dynamic-boundaries]
        verification
        (invoke-c7 'sh08-verify-function-type-result [request result])
        altered-candidates
        [(assoc-in result [:function-type-table 0 :return]
                   :gravity.type/integer)
         (assoc-in result [:recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:identity-input :recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:typed-core :recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:recursive-call-facts 0 :dispatch]
                   :altered-dispatch)
         (assoc-in result [:constraint-ledger recursive-constraint-index :status]
                   :rejected)]]
    (is (= :accepted (:status result)))
    (is (= :bounded-named-self-recursive-positive-fixed-arity-positional-literal-base
           (:scope result)))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1 (:domain proof)))
    (is (= :gravity.type/string (:base-type proof)))
    (is (= :gravity.type/string (:base-type
                                 (get-in result [:identity-input
                                                 :recursive-proof]))))
    (is (= :gravity.type/string (:return function-entry)))
    (is (= :gravity.type/string
           (get-in function-entry [:parameters 0 :type])))
    (is (= :pending-sh10 (:ownership-constraints function-entry)))
    (is (= [:pending-sh09] (:thrown-error-effects function-entry)))
    (is (= :gravity.type/string
           (get (:type-table result) recursive-call-id)))
    (is (= :gravity.type/string
           (get (:type-table result) evidence-call-id)))
    (is (= [:self-recursive-call :external-parameter-evidence]
           (mapv :role recursive-facts)))
    (is (= :direct-named-self-recursion
           (get-in recursive-facts [0 :dispatch])))
    (is (= :external-parameter-evidence
           (get-in recursive-facts [1 :role])))
    (is (= :gravity.type/string
           (get-in recursive-facts [0 :result-type])))
    (is (= :gravity.type/string
           (get-in recursive-facts [1 :result-type])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-recursive-edge])
           (:recursive-edge proof)
           (get-in recursive-facts [0 :b47-edge])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-evidence-edge])
           (get-in proof [:parameter-evidence-call :edge])
           (get-in recursive-facts [1 :external-edge])))
    (is (= (:recursive-call-source proof)
           (select-keys (first recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (select-keys (:parameter-evidence-call proof)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])
           (select-keys (second recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (:definition-binding-id proof)
           (:recursive-definition-binding-id proof)
           (get-in recursive-facts [0 :recursive-definition-binding-id])))
    (is (= :operator-then-arguments
           (get-in proof [:recursive-edge :evaluation-order])))
    (is (= (:constraint-ledger result)
           (get-in result [:typed-core :constraints])
           (get-in result [:identity-input :constraint-ledger])))
    (is (= pending (:pending result)
           (get-in result [:identity-input :pending])))
    (is (= proof
           (get-in result [:typed-core :recursive-proof])
           (get-in result [:identity-input :recursive-proof])))
    (is (= recursive-facts
           (get-in result [:typed-core :recursive-call-facts])))
    (is (= :converged (get-in result [:convergence :status])))
    (is (= (get-in request [:canonical-core-artifact :provenance :source-path])
           (get-in result [:provenance :source-path])))
    (is (contains? (:provenance result) :actual-source-path))
    (is (= :passed (:status verification)))
    (doseq [candidate altered-candidates]
      (is (= :rejected
             (:status
              (invoke-c7 'sh08-verify-function-type-result
                         [request candidate])))))))
