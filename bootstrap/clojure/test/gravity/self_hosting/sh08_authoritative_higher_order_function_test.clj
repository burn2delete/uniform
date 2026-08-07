(ns gravity.self-hosting.sh08-authoritative-higher-order-function-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_authoritative_higher_order_function_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 authoritative higher-order test source is absent"
                {:id "STD08-C7-HO-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "STD08-C7-HO-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private ho2-relative-root
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
   {:engine :gravity-sh08-authoritative-higher-order-function
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- source-text []
  (slurp (str (.resolve @root c7-relative-path))))

(defn- source-reachable? [name]
  (.contains (source-text) name))

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

(defn- pure-carrier
  ([] (pure-carrier {}))
  ([overrides]
   (let [identity-fn-syntax :identity-function-syntax
         apply-fn-syntax :apply-function-syntax
         identity-def-syntax :identity-definition-syntax
         apply-def-syntax :apply-definition-syntax
         identity-def-binding :identity-definition-binding
         apply-def-binding :apply-definition-binding
         identity-param-binding :identity-parameter-binding
         callable-binding :callable-parameter-binding
         value-binding :value-parameter-binding
         identity-fn-node
         (node :identity-fn :fn [:identity-body] {}
               identity-fn-syntax)
         identity-body
         (node :identity-body :reference []
               {:binding-id identity-param-binding} identity-fn-syntax)
         apply-fn-node
         (node :apply-fn :fn [:inner-call] {}
               apply-fn-syntax)
         inner-operator
         (node :inner-operator :reference []
               {:binding-id callable-binding} apply-fn-syntax)
         inner-value
         (node :inner-value :reference []
               {:binding-id value-binding} apply-fn-syntax)
         inner-call-node
         (assoc
          (node :inner-call :call [:inner-operator :inner-value] {}
                apply-fn-syntax)
          :resolved-binding-ids [callable-binding])
         outer-operator
         (node :outer-operator :reference []
               {:binding-id apply-def-binding} nil)
         outer-function-value
         (node :outer-function-value :reference []
               {:binding-id identity-def-binding} nil)
         outer-literal
         (node :outer-literal :literal []
               {:literal-kind :integer} nil)
         outer-call-node
         (node :outer-call :call
               [:outer-operator :outer-function-value :outer-literal]
               {} nil)
         nodes [identity-fn-node identity-body apply-fn-node inner-operator
                inner-value inner-call-node outer-operator outer-function-value
                outer-literal outer-call-node]
         definitions
         [{:binding-id identity-def-binding
           :syntax-id identity-def-syntax
           :value-node-id :identity-fn}
          {:binding-id apply-def-binding
           :syntax-id apply-def-syntax
           :value-node-id :apply-fn}]
         function-records
         [{:ordinal 0
           :function-core-node-id :identity-fn
           :function-form-id :identity-form
           :function-syntax-id identity-fn-syntax
           :body-core-node-id :identity-body
           :parameter-binding-ids [identity-param-binding]
           :fixed-arity 1
           :definition-kind :named-top-level
           :definition-binding-id identity-def-binding
           :definition-core-node-id :identity-def-node
           :definition-name 'identity}
          {:ordinal 1
           :function-core-node-id :apply-fn
           :function-form-id :apply-form
           :function-syntax-id apply-fn-syntax
           :body-core-node-id :inner-call
           :parameter-binding-ids [callable-binding value-binding]
           :fixed-arity 2
           :definition-kind :named-top-level
           :definition-binding-id apply-def-binding
           :definition-core-node-id :apply-def-node
           :definition-name 'apply-one}]
         inner-call
         {:core-node-id :inner-call
          :operator-binding-id callable-binding
          :argument-node-ids [:inner-value]
          :ordered-evaluation-node-ids [:inner-operator :inner-value]
          :evaluation-order :operator-then-arguments}
         outer-call
         {:core-node-id :outer-call
          :operator-binding-id apply-def-binding
          :argument-node-ids [:outer-function-value :outer-literal]
          :ordered-evaluation-node-ids
          [:outer-operator :outer-function-value :outer-literal]
          :evaluation-order :operator-then-arguments}
         callable-definition-syntax :callable-parameter-definition-syntax
         edges
         [{:ordinal 0
           :call-core-node-id :inner-call
           :caller-function-syntax-id apply-fn-syntax
           :caller-function-core-node-id :apply-fn
           :callee-binding-id callable-binding
           :callee-definition-syntax-id callable-definition-syntax
           :callee-function-syntax-id nil
           :callee-function-core-node-id nil
           :argument-core-node-ids [:inner-value]
           :ordered-evaluation-node-ids [:inner-operator :inner-value]
           :evaluation-order :operator-then-arguments
           :classification :nonlocal-or-nonfunction}
          {:ordinal 1
           :call-core-node-id :outer-call
           :caller-function-syntax-id nil
           :caller-function-core-node-id nil
           :callee-binding-id apply-def-binding
           :callee-definition-syntax-id apply-def-syntax
           :callee-function-syntax-id apply-fn-syntax
           :callee-function-core-node-id :apply-fn
           :argument-core-node-ids
           [:outer-function-value :outer-literal]
           :ordered-evaluation-node-ids
           [:outer-operator :outer-function-value :outer-literal]
           :evaluation-order :operator-then-arguments
           :classification :local-function}]
         bindings
         [{:binding-id callable-binding
           :definition-syntax-id callable-definition-syntax}
          {:binding-id apply-def-binding
           :definition-syntax-id apply-def-syntax}]
         module {:effects #{}
                 :capabilities #{}
                 :profile :meta
                 :target :jvm}
         base {:nodes nodes
               :definitions definitions
               :function-records function-records
               :calls [inner-call outer-call]
               :edges edges
               :bindings bindings
               :module module}]
     (loop [remaining overrides result base]
       (if (= (count remaining) 0)
         result
         (recur (rest remaining)
                (assoc-in result (first (first remaining))
                          (second (first remaining)))))))))

(defn- pure-proof [carrier]
  (invoke-c7
   'sh08-ft-higher-order-proof
   [(:nodes carrier) (:definitions carrier) (:bindings carrier)
    (:function-records carrier) (:calls carrier) (:edges carrier)
    (:module carrier)]))

(defn- pure-contextual-inference [carrier context]
  (let [node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
        function-types
        (into {}
              (map (fn [record]
                     (let [function
                           (invoke-c7 'sh08-ft-function-type-skeleton
                                      [record (:module carrier)])]
                       [(:function-id function) function]))
                   (:function-records carrier)))]
    (invoke-c7
     'sh08-ft-infer-acyclic-with-context
     [(:nodes carrier) node-table [] (:definitions carrier)
      (:calls carrier) (:edges carrier) function-types {} 64 context])))

(deftest sh08-authoritative-c7-reachability-and-identity
  (is (map? (get-in @c7-plan [:functions
                              'sh08-function-type-core-artifact])))
  (is (map? (get-in @c7-plan [:functions
                              'sh08-verify-function-type-result])))
  (let [artifact-function
        (get-in @c7-plan [:functions 'sh08-ft-function-type-core-artifact])
        top-level (first (:instructions artifact-function))
        invalid-lets
        (filter #(and (map? %)
                      (= :let (:op %))
                      (not= 1 (count (:body %))))
                (tree-seq coll? seq artifact-function))]
    (is (= 1 (count (:instructions artifact-function))))
    (is (= :if (:op top-level)))
    (is (= :map-literal (get-in top-level [:else :op])))
    (is (empty? invalid-lets)))
  (doseq [name ["sh08-ft-higher-order-proof"
                "sh08-ft-infer-acyclic-with-context"
                "sh08-ft-call-facts-with-context"
                "sh08-ft-higher-order-outer-edge-valid?"]]
    (is (source-reachable? name) name)))

(deftest sh08-authoritative-pure-proof-and-context-matrix
  (let [carrier (pure-carrier)
        proof (pure-proof carrier)
        identity-type (:identity-function-type proof)
        apply-type (:apply-function-type proof)]
    (is (= :accepted (:status proof)))
    (is (not= :identity-definition-syntax
              (:callable-definition-syntax-id proof)))
    (is (= :pending-sh10 (:ownership-constraints identity-type)))
    (is (= [:pending-sh09] (:thrown-error-effects identity-type)))
    (is (= identity-type
           (get-in apply-type [:parameters 0 :type])))
    (is (= identity-type
           (invoke-c7
            'sh08-ft-higher-order-complete-function
            [(invoke-c7
              'sh08-ft-function-type-skeleton
              [(first (:function-records carrier)) (:module carrier)])
             identity-type])))
    (is (= apply-type
           (invoke-c7
            'sh08-ft-higher-order-complete-function
             [(invoke-c7
              'sh08-ft-function-type-skeleton
              [(second (:function-records carrier)) (:module carrier)])
             apply-type])))
    (is (= (:source-span identity-type)
           (get-in apply-type [:parameters 0 :type :source-span])))
    (is (= (:syntax-id identity-type)
           (get-in apply-type [:parameters 0 :type :syntax-id])))
    (is (= (:origin-chain identity-type)
           (get-in apply-type [:parameters 0 :type :origin-chain])))
    (is (= (:generated-origin identity-type)
           (get-in apply-type [:parameters 0 :type :generated-origin])))
    (is (= [:inner-indirect-call :outer-direct-call]
           (mapv :role (invoke-c7
                        'sh08-ft-higher-order-call-proof-facts
                        [proof]))))
    (let [node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
          function-types
          (into {}
                [[(:function-id identity-type) identity-type]
                 [(:function-id apply-type) apply-type]])
          inference
          (invoke-c7
           'sh08-ft-infer-acyclic-with-context
           [(:nodes carrier) node-table [] (:definitions carrier)
            (:calls carrier) (:edges carrier) function-types
            (:initial-binding-types proof) 64 proof])
          call-facts
          (invoke-c7
           'sh08-ft-call-facts-with-context
           [(:calls carrier) (:edges carrier) node-table
            (:type-table inference) (:function-types inference)
            (:module carrier) proof])]
      (is (= :converged (:convergence-status inference)))
      (is (= [] (:diagnostics inference)))
      (is (= :gravity.type/integer
             (get (:type-table inference) :inner-call)))
      (is (= :gravity.type/integer
             (get (:type-table inference) :outer-call)))
      (is (= [:inner-indirect-call :outer-direct-call]
             (mapv :role (invoke-c7
                          'sh08-ft-higher-order-call-proof-facts
                          [proof]))))
      (is (= [:indirect-function-value :direct-local-function]
             (mapv :dispatch call-facts)))
      (is (= identity-type
             (get-in (:binding-types inference)
                     [:callable-parameter-binding])))
      (is (= :gravity.type/integer
             (get-in (:binding-types inference)
                     [:identity-parameter-binding]))))))

(deftest sh08-authoritative-first-order-record-shape-is-additive
  (let [carrier (pure-carrier)
        proof (pure-proof carrier)
        identity-type (:identity-function-type proof)
        apply-type (:apply-function-type proof)
        node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
        type-table {:outer-function-value identity-type
                    :outer-literal :gravity.type/integer}
        function-types {(:function-id apply-type) apply-type}
        call-facts
        (invoke-c7
         'sh08-ft-call-facts
         [[(second (:calls carrier))]
          [(second (:edges carrier))]
          node-table type-table function-types (:module carrier)])
        constraints
        (invoke-c7
         'sh08-ft-call-constraints
         [[(second (:calls carrier))]
          [(second (:edges carrier))]
          node-table type-table {} function-types])
        fact (first call-facts)
        constraint (first (:constraints constraints))]
    (is (= #{:artifact :fact-id-request :call-core-node-id
             :caller-function-syntax-id :callee-function-syntax-id
             :argument-types :result-type :evaluation-order :source-span
             :syntax-id :origin-chain :generated-origin :profile :target
             :effects :capabilities :status}
           (set (keys fact))))
    (is (= #{:domain :call-core-node-id :callee-function-syntax-id
             :argument-types :result-type}
           (set (keys (:fact-id-request fact)))))
    (is (not (contains? fact :higher-order-call?)))
    (is (not (contains? fact :dispatch)))
    (is (= #{:constraint-id-request :kind :lhs :rhs :origin :profile
             :target :status :diagnostics}
           (set (keys constraint))))
    (is (= #{:domain :call-core-node-id :callee-function-syntax-id}
           (set (keys (:constraint-id-request constraint)))))
    (is (not (contains? constraint :higher-order-call?)))))

(deftest sh08-authoritative-pure-proof-rejects-substitution
  (let [carrier (pure-carrier)]
    (doseq [[label overrides]
            [["callable-definition-lineage"
              [[[:bindings 0 :definition-syntax-id]
                :identity-definition-syntax]]]
             ["inner-edge-binding"
              [[[:edges 0 :callee-binding-id]
                :wrong-callable-binding]]]
             ["inner-order"
              [[[:calls 0 :ordered-evaluation-node-ids]
                [:inner-value :inner-operator]]]]
             ["outer-edge-classification"
              [[[:edges 1 :classification] :nonlocal-or-nonfunction]]]
             ["outer-argument-order"
              [[[:calls 1 :argument-node-ids]
                [:outer-literal :outer-function-value]]]]
             ["outer-literal-kind"
              [[[:nodes 8 :attributes :literal-kind] :string]]]]]
      (is (not= :accepted
                (:status (pure-proof (pure-carrier overrides))))
          label))))

(deftest sh08-authoritative-pure-proof-rejects-nonfunction-capture-and-arity
  (doseq [[label overrides]
          [["identity-not-function"
            [[[:nodes 0 :core-form] :literal]]]
           ["identity-capture"
            [[[:nodes 1 :evaluation :owner-function-syntax-id]
              :captured-owner]]]
           ["inner-wrong-arity"
            [[[:calls 0 :argument-node-ids]
              [:inner-value :inner-value-2]]]]
           ["inner-missing-order"
            [[[:calls 0 :evaluation-order] :arguments-then-operator]]]]]
    (is (not= :accepted
              (:status (pure-proof (pure-carrier overrides))))
        label)))

(deftest sh08-authoritative-rejected-proof-uses-first-order-public-fallback
  (doseq [[label overrides]
          [["identity-not-function"
            [[[:nodes 0 :core-form] :literal]]]
           ["identity-capture"
            [[[:nodes 1 :evaluation :owner-function-syntax-id]
              :captured-owner]]]
           ["inner-wrong-arity"
            [[[:calls 0 :argument-node-ids]
              [:inner-value :inner-value-2]]]]
           ["inner-wrong-order"
            [[[:calls 0 :evaluation-order] :arguments-then-operator]]]]]
    (let [carrier (pure-carrier overrides)
          proof (pure-proof carrier)
          inference (pure-contextual-inference carrier proof)
          diagnostic (first (:diagnostics inference))
          node-table
          (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
          rendered
          (invoke-c7 'sh08-ft-render-inference-diagnostics
                     [(:diagnostics inference) node-table (:module carrier)])
          diagnostic-envelope (first rendered)
          fallback-base {:status :rejected :diagnostics rendered}
          fallback-result
          (invoke-c7 'sh08-ft-higher-order-result-fields
                     [fallback-base proof])
          call-facts
          (invoke-c7
           'sh08-ft-call-facts-with-context
           [(:calls carrier) (:edges carrier) node-table
            (:type-table inference) (:function-types inference)
            (:module carrier) proof])]
      (is (not= :accepted (:status proof)) label)
      (is (= {:rule "C7-ANNOTATION"
              :reason :unsupported-nonlocal-call
              :expected :supported-local-first-order-call
              :actual :nonlocal-or-nonfunction}
             (select-keys diagnostic [:rule :reason :expected :actual]))
          label)
      (is (= {:artifact :gravity/c7-function-type-diagnostic
              :diagnostic-id
              "SH08-C7:C7-ANNOTATION::inner-call::unsupported-nonlocal-call"
              :diagnostic-id-request
              {:domain :gravity/sh08-function-type-diagnostic-v1
               :rule "C7-ANNOTATION"
               :core-node-id :inner-call
               :syntax-id :inner-call
               :source-span {:line 1 :column 1 :length 1}
               :binding-id :callable-parameter-binding
               :expected-type :supported-local-first-order-call
               :actual-type :nonlocal-or-nonfunction
               :profile :meta
               :target :jvm
               :origin-chain []
               :generated-origin nil
               :parameter-binding-id nil
               :constraint-id nil
               :reason :unsupported-nonlocal-call}
              :rule "C7-ANNOTATION"
              :severity :error
              :stage :type-checking
              :core-node-id :inner-call
              :syntax-id :inner-call
              :source-span {:line 1 :column 1 :length 1}
              :binding-id :callable-parameter-binding
              :parameter-binding-id nil
              :type-id :nonlocal-or-nonfunction
              :constraint-id nil
              :expected-type :supported-local-first-order-call
              :actual-type :nonlocal-or-nonfunction
              :profile :meta
              :target :jvm
              :origin-chain []
              :generated-origin nil
              :reason :unsupported-nonlocal-call
              :remediation
              "Provide coherent B47 function, call, recursion, definition, binding, and lexical products with bounded first-order primitive types."}
             diagnostic-envelope)
          label)
      (is (= fallback-base fallback-result) label)
      (is (not (contains? fallback-result :higher-order-proof)) label)
      (is (not (contains? fallback-result :higher-order-call-facts)) label)
      (is (not-any? #(contains? % :higher-order-call?)
                    (:constraints inference))
          label)
      (is (= 2 (count call-facts)) label)
      (is (not-any? #(or (contains? % :higher-order-call?)
                         (contains? % :b47-edge)
                         (contains? % :dispatch))
                    call-facts)
          label)
      (is (not-any? #(contains? (:fact-id-request %)
                                :higher-order-call?)
                    call-facts)
          label))))

(deftest sh08-authoritative-higher-order-pending-is-an-exact-replacement
  (is (= [:recursive-type-annotations
          :captures :lifetimes :aliases :multi-hop :polymorphism :overloads
          :multi-arity :variadic-dispatch :protocol-dispatch :dynamic-dispatch
          :reflection
          :records :unions :protocols :generics :casts :dynamic-boundaries]
         (invoke-c7
          'sh08-ft-higher-order-pending
          [[:recursive-type-annotations :higher-order-functions
            :records :unions :protocols :generics :casts
            :dynamic-boundaries]]))))

(deftest sh08-authoritative-ho2-fixtures-are-co-canonical
  (doseq [basename ["function-value-typed-call"]]
    (let [gravity (str (.resolve @root
                                 (str ho2-relative-root
                                      "/accepted/" basename ".gravity")))
          qst (str (.resolve @root
                             (str ho2-relative-root "/accepted/" basename
                                  ".qst")))]
      (is (= (slurp gravity) (slurp qst)))
      (is (.contains (slurp gravity) ":scope")))))

(deftest sh08-authoritative-ho2-authenticated-fixture-boundary
  ;; This is intentionally the last var: it is the only var that asks the
  ;; coordinator's SH-07 harness for a full authenticated B47 carrier.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact
        (deref (ns-resolve namespace 'fixture-artifact))
        function-request
        (deref (ns-resolve namespace 'function-request))]
    (doseq [extension [".gravity"]]
      (let [artifact (fixture-artifact "accepted"
                                      "function-value-typed-call"
                                      extension)
            request (function-request artifact)
            core (:canonical-core-artifact request)
            authenticated-core
            (invoke-c7 'sh08-ft-authenticated-core [request])
            resolved-core (:core authenticated-core)
            binding-table (:binding-table authenticated-core)
            core-shape (invoke-c7 'sh08-validate-core-shape [resolved-core])
            products
            (invoke-c7 'sh08-ft-products-valid?
                       [resolved-core core-shape binding-table])
            node-index
            (invoke-c7 'sh08-ft-node-index [(:nodes resolved-core)])
            identity-candidates
            (invoke-c7
             'sh08-ft-higher-order-identity-candidates
             [(:function-records resolved-core) node-index
              (:definitions resolved-core)])
            apply-candidates
            (if (= 1 (count identity-candidates))
              (invoke-c7
               'sh08-ft-higher-order-apply-candidates
               [(:function-records resolved-core) node-index
                (:definitions resolved-core) binding-table
                (:calls resolved-core) (:call-edges resolved-core)
                (first identity-candidates)])
              [])
            preflight-proof
            (invoke-c7
             'sh08-ft-higher-order-proof
             [(:nodes resolved-core) (:definitions resolved-core) binding-table
              (:function-records resolved-core) (:calls resolved-core)
              (:call-edges resolved-core) (:module resolved-core)])
            private-result
            (invoke-c7 'sh08-ft-function-type-core-artifact
                       [resolved-core binding-table])
            result (invoke-c7 'sh08-function-type-core-artifact [request])
            authenticated-evidence
            (pr-str
             {:status (:status authenticated-core)
              :map? (map? authenticated-core)
              :core-id (:artifact-id resolved-core)
              :request-core-id (:artifact-id core)
              :binding-count (count binding-table)})
            candidate-evidence
            (pr-str
             {:identity-count (count identity-candidates)
              :identity-function-ids
              (mapv #(get-in % [:record :function-syntax-id])
                    identity-candidates)
              :apply-count (count apply-candidates)
              :apply-function-ids
              (mapv #(get-in % [:record :function-syntax-id])
                    apply-candidates)})
            product-evidence
            (pr-str
             {:shape-status (:status core-shape)
              :product-status (:status products)
              :product-reason (:reason products)})
            preflight-evidence
            (pr-str
             (select-keys
              preflight-proof
              [:status :reason :identity-function-syntax-id
               :apply-function-syntax-id :inner-call-core-node-id
               :outer-call-core-node-id]))
            private-evidence
            (let [diagnostic (first (:diagnostics private-result))]
              (pr-str
               {:status (:status private-result)
                :scope (:scope private-result)
                :diagnostic
                {:rule (:rule diagnostic)
                 :reason (:reason diagnostic)
                 :expected (:expected-type diagnostic)
                 :actual (:actual-type diagnostic)}}))
            result-evidence
            (let [diagnostic (first (:diagnostics result))]
              (pr-str
               {:status (:status result)
                :scope (:scope result)
                :diagnostic
                {:rule (:rule diagnostic)
                 :reason (:reason diagnostic)
                 :expected (:expected-type diagnostic)
                 :actual (:actual-type diagnostic)}}))
            comparison-evidence
            (pr-str
             {:equal? (= private-result result)
              :private-status (:status private-result)
              :public-status (:status result)})]
        (is (= :accepted (:status authenticated-core)) authenticated-evidence)
        (is (= (:artifact-id core) (:artifact-id resolved-core))
            authenticated-evidence)
        (is (= :accepted (:status core-shape)) product-evidence)
        (is (= :accepted (:status products)) product-evidence)
        (is (= 1 (count identity-candidates)) candidate-evidence)
        (is (= 1 (count apply-candidates)) candidate-evidence)
        (is (= :accepted (:status preflight-proof)) preflight-evidence)
        (is (= :accepted (:status private-result)) private-evidence)
        (is (= private-result result) comparison-evidence)
        (is (= :accepted (:status result)) result-evidence)
        (when (and (= :accepted (:status preflight-proof))
                   (= :accepted (:status result)))
          (let [
            verification (invoke-c7
                          'sh08-verify-function-type-result
                          [request result])
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
            outer-value-id
            (first (rest (get-in request
                                 [:canonical-core-artifact :calls 1
                                  :argument-node-ids])))
            inner-value-id
            (first (get-in request
                           [:canonical-core-artifact :calls 0
                            :argument-node-ids]))
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
            identity-input (:identity-input result)
            altered-candidates
            [(assoc-in result [:higher-order-proof :inner-edge :classification]
                       :local-function)
             (assoc-in result [:higher-order-call-facts 0 :b47-edge :classification]
                        :local-function)
             (assoc-in result [:function-type-table 0 :return]
                        :gravity.type/string)
             (assoc-in result [:constraint-ledger 0 :status] :rejected)
             (assoc-in result [:call-type-facts 0 :result-type]
                        :gravity.type/string)
             (assoc-in result [:pending 0] :altered-pending)
             (assoc-in result [:identity-input :higher-order-proof :inner-edge
                               :classification]
                       :local-function)
             (assoc result :diagnostics [{:rule "C7-ALTERED"}])
             (assoc result :scope :altered-scope)]]
        (is (= :capture-free-higher-order-fixed-arity-one-hop
               (:scope result)) extension)
        (is (= :passed (:status verification)) extension)
        (is (= :gravity.type/integer
               (get (:type-table result) outer-value-id)) extension)
        (is (= :gravity.type/integer
               (get (:type-table result) inner-value-id)) extension)
        (is (= :gravity.type/integer (get (:type-table result) outer-id)) extension)
        (is (= :gravity.type/integer (get (:type-table result) inner-id)) extension)
        (is (= :gravity.type/integer
               (get (:type-table result)
                    (:identity-body-core-node-id proof))) extension)
        (is (= identity-entry
               (get-in apply-entry [:parameters 0 :type])) extension)
        (is (= identity-entry (:identity-function-type proof)) extension)
        (is (= apply-entry (:apply-function-type proof)) extension)
        (is (= (set [(:function-id identity-entry)
                     (:function-id apply-entry)])
               (set (map :function-id (:function-type-table result)))) extension)
        (is (= (get-in request [:canonical-core-artifact :function-records])
               (get-in result [:function-products :function-records])) extension)
        (is (= (get-in request [:canonical-core-artifact :call-edges])
               (get-in result [:function-products :call-edges])) extension)
        (is (= (get-in request [:canonical-core-artifact :lexical-bindings])
               (get-in result [:function-products :lexical-bindings])) extension)
        (is (= (get-in request [:canonical-core-artifact :recursion-components])
               (get-in result [:function-products :recursion-components])) extension)
        (is (= (mapv :core-node-id
                     (get-in request [:canonical-core-artifact :calls]))
               (mapv :call-core-node-id (:call-type-facts result))) extension)
        (is (= source-inner-edge
               (first (filter #(= inner-id (:call-core-node-id %))
                              (get-in result [:function-products :call-edges]))))
            extension)
        (is (= source-outer-edge
               (first (filter #(= outer-id (:call-core-node-id %))
                              (get-in result [:function-products :call-edges]))))
            extension)
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
               (get-in result [:higher-order-proof :inner-edge])) extension)
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
               (get-in result [:higher-order-proof :outer-edge])) extension)
        (is (= (get inner-fact :b47-edge)
               (get-in result [:higher-order-call-facts 0 :b47-edge])) extension)
        (is (= (get outer-fact :b47-edge)
               (get-in result [:higher-order-call-facts 1 :b47-edge])) extension)
        (is (= (:callee-function-syntax-id source-inner-edge)
               (:callee-function-syntax-id inner-fact)) extension)
        (is (= identity-id
               (:selected-identity-function-syntax-id inner-fact)) extension)
        (is (= apply-id
               (:selected-apply-function-syntax-id outer-fact)) extension)
        (is (= (get-in request [:canonical-core-artifact :module :effects])
               (:latent-effects identity-entry)) extension)
        (is (= (get-in request [:canonical-core-artifact :module :capabilities])
               (:capabilities identity-entry)) extension)
        (is (= (:latent-effects identity-entry)
               (:latent-effects apply-entry)) extension)
        (is (= (:capabilities identity-entry)
               (:capabilities apply-entry)) extension)
        (is (= :pending-sh10 (:ownership-constraints identity-entry)) extension)
        (is (= [:pending-sh09] (:thrown-error-effects identity-entry)) extension)
        (is (= (:ownership-constraints identity-entry)
               (:ownership-constraints apply-entry)) extension)
        (is (= (:thrown-error-effects identity-entry)
               (:thrown-error-effects apply-entry)) extension)
        (is (= (get-in request [:canonical-core-artifact :module :profile])
               (first (:profile-constraints identity-entry))) extension)
        (is (= (get-in request [:canonical-core-artifact :module :target])
               (:target identity-entry)) extension)
        (is (= [:recursive-type-annotations
                :captures :lifetimes :aliases :multi-hop :polymorphism
                :overloads :multi-arity :variadic-dispatch
                :protocol-dispatch :dynamic-dispatch :reflection
                :records :unions :protocols :generics :casts
                :dynamic-boundaries]
               (:pending result)) extension)
        (is (not (contains? identity-input :source-path)) extension)
        (is (contains? (:provenance result) :actual-source-path) extension)
        (doseq [candidate altered-candidates]
          (is (= :rejected
                 (:status (invoke-c7
                           'sh08-verify-function-type-result
                           [request candidate])))
              extension))
        (is (= :indirect-function-value
               (get-in result [:higher-order-call-facts 0 :dispatch]))
            extension)
        (is (= :direct-local-function
               (get-in result [:higher-order-call-facts 1 :dispatch]))
            extension)
        (is (not-any? #{:gravity.type/unknown :unknown :Dynamic :dynamic}
                      (tree-seq coll? seq
                                (select-keys result
                                             [:function-type-table
                                              :higher-order-proof
                                              :higher-order-call-facts]))))))))))
