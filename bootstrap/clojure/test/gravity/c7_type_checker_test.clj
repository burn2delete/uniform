(ns gravity.c7-type-checker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c7-type-checker :as c7]
            [gravity.c7-type-checker.inference :as compatibility]
            [gravity.types :as types]))

(def module
  {:module 'gravity.c7-test
   :source-path "c7-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(defn node
  ([id form]
   (node id form {}))
  ([id form children]
   {:artifact :gravity/core-node
    :node-id (str "c6-core-" id)
    :form form
    :children children
    :source {:syntax-id (str "syntax-" id)
             :span {:source "c7-test.gravity" :form-index id}
             :origin-chain [{:kind :source}]}
    :profile :hosted
    :target :jvm
    :effects #{}
    :capabilities #{}
    :metadata {}
    :unsafe-metadata nil}))

(def nodes
  [(node 0 :literal)
   (node 1 'fn {:params ['x]})
   (node 2 :call {:operator 'dynamic/value})
   (node 3 :call {:operator 'dynamic/cast})
   (node 4 :call {:operator 'generic/id})
   (node 5 :call {:operator 'protocol/value})])

(def c6-artifact
  {:kind :gravity/stage0-c6-core-lowering-artifact
   :artifact-id "sha256:c6"
   :core-ast-module {:artifact :gravity/core-ast-module
                     :module 'gravity.c7-test
                     :roots (mapv :node-id nodes)
                     :status :complete}
   :core-node-table nodes
   :surface-to-core-map {:status :complete}
   :evaluation-order-records {:status :complete}
   :domain-boundary-records
   [{:domain :schema-ir
     :source {:syntax-id "schema-syntax"}
     :semantic-anchor {:source-syntax "schema-syntax"}
     :profile :hosted
     :target :jvm}]})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c7-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c6-lowering-source-artifact (fn [_ _] c6-artifact)}))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c7/c7-engine-contract)
        publics (ns-publics 'gravity.c7-type-checker)]
    (is (= :hosted-stage0-c7-type-checker (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c7-authority? contract)))
    (is (some #{:effect-legality} (:does-not-own contract)))
    (is (some #{:release} (:does-not-own contract)))
    (is (= #{'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= #{'gravity.bootstrap 'gravity.diagnostics}
           (set (get-in contract [:dependency-direction :forbids]))))
    (is (= (set (keys publics)) (set (keys c7/public-api))))
    (doseq [[name spec] c7/public-api
            :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (not (contains? (set (keys (ns-aliases
                                    'gravity.c7-type-checker)))
                        'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operations [nil
                      {:unknown identity}
                      {:c7-node-type :keyword-is-invokable}
                      {:c7-type-diagnostic-ids [:not-a-string]}
                      {:c7-type-governing-document ""}
                      {:c7-type-rejected-designs [:not-a-map]}
                      {:c7-type-override-diagnostics {:verify :not-a-string}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c7/with-operations operations (constantly :unreachable)))))
  (let [calls (atom [])]
    (is (= "CheckedCast[String]"
           (c7/with-operations
             {:c7-node-operator (fn [value]
                                  (swap! calls conj value)
                                  'dynamic/cast)}
             #(c7/c7-node-type (node 10 :call
                                     {:operator 'dynamic/value})))))
    ;; The original operator would resolve Dynamic, proving the interposed
    ;; helper was reached even though the enclosing type function was not.
    (is (= 1 (count @calls)))))

(deftest type-engine-emits-complete-products
  (is (= ["Nil" "Boolean" "I64" "F64" "String" "Keyword"
          "Symbol" "Vector[Dynamic]" "Map[Keyword, Dynamic]"
          "Set[Dynamic]" "List[Dynamic]" "Dynamic"]
         (mapv c7/c7-literal-type
               [nil true 1 (float 1.0) "x" :x 'x [] {} #{} '(x)
                (Object.)])))
  (let [artifact (c7/with-operations
                   (operations)
                   #(c7/compiler-c7-type-source-artifact
                     "c7-test.gravity" "ignored"))]
    (is (= :gravity/stage0-c7-type-checker-artifact (:kind artifact)))
    (is (= :gravity/typed-core
           (get-in artifact [:typed-core-module :artifact])))
    (is (= (count nodes) (count (:type-facts artifact))))
    (is (= :solved (get-in artifact [:constraint-ledger :status])))
    (is (= 1 (count (get-in artifact
                            [:dynamic-boundary-records :records]))))
    (is (= 1 (count (get-in artifact
                            [:cast-conversion-records :records]))))
    (is (= 1 (count (get-in artifact
                            [:generic-instantiation-table :records]))))
    (is (= 1 (count (get-in artifact
                            [:protocol-dispatch-type-table :records]))))
    (is (= 1 (count (get-in artifact [:schema-type-links :records]))))
    (is (= :passed (get-in artifact
                           [:typed-core-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c7/c7-type-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact [:type-diagnostics :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c7-identities
  (doseq [[failure expected-id]
          [[:type-mismatch "C7-TYPE-MISMATCH"]
           [:annotation "C7-ANNOTATION"]
           [:dynamic "C7-DYNAMIC"]
           [:cast "C7-CAST"]
           [:nullability "C7-NULLABILITY"]
           [:generic "C7-GENERIC"]
           [:protocol "C7-PROTOCOL"]
           [:layout "C7-LAYOUT"]
           [:schema "C7-SCHEMA"]
           [:verify "C7-VERIFY"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler :c7-type-check :fail]
                                    failure)
            error (try
                    (c7/with-operations
                      (operations failed-module)
                      #(c7/compiler-c7-type-source-artifact
                        "c7-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C7" (:document-id (ex-data error))))))))

(deftest types-stage-contract-states-the-seed-data-boundary
  (let [contract (:gravity/namespace-contract (meta #'types/type-fact))
        publics (ns-publics 'gravity.types)]
    (is (= :seed-type-products-and-current-gravity-runtime (:contract-boundary contract)))
    (is (= 11 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= contract (:gravity/namespace-contract (meta (publics name)))))
      (is (= :none (:admission api)))
      (is (or (true? (:seed-compatibility-data-only? api))
              (true? (:current-gravity-runtime-data-only? api))))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-canonical-types? api)))
      (is (false? (:grants-authority? api))))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.c7-type-checker.inference gravity.bootstrap]
           (get-in contract [:dependency-direction :consumed-by])))
    (is (false? (:legacy-status-labels-authoritative? contract)))
    (is (not (contains? (ns-aliases 'gravity.types) 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest types-stage-products-retain-source-facts-and-constraint-order
  (let [source {:syntax-id :syntax :span {:source "native.gravity" :form-index 2}
                :origin-chain [:source] :generated-origin {:from :macro}}
        node {:node-id :native-function :form 'fn :source source :children {:params ['x 'y]}
              :profile :hosted :target :jvm :effects #{:io/write} :capabilities #{:io/stdout}}
        fact (types/type-fact (constantly "ExplicitCallerClassification") node)
        other (assoc fact :core-node :local :type "BindingRef")
        environment (types/type-environment [fact other])
        constraints (types/constraint-ledger [fact other])
        function (first (:functions (types/function-table [node])))]
    (is (= "ExplicitCallerClassification" (:type fact)))
    (is (identical? source (:source fact)))
    (is (= (select-keys node [:profile :target :effects :capabilities])
           (select-keys fact [:profile :target :effects :capabilities])))
    (is (= {:local "BindingRef" :native-function "ExplicitCallerClassification"} (:types environment)))
    (is (= {:local {:type "BindingRef" :mutability :immutable :ownership :borrowed}} (:locals environment)))
    (is (= [:native-function :local] (mapv :source-node (:constraints constraints))))
    (is (= ["c7-constraint-0" "c7-constraint-1"] (mapv :constraint-id (:constraints constraints))))
    (is (= [[:native-function] [:local]] (mapv :dependencies (:constraints constraints))))
    (is (= ["Dynamic" "Dynamic"] (:params function)))
    (is (= (:effects node) (:latent-effects function)))
    (is (= (:capabilities node) (:capabilities function)))
    (is (identical? source (:source function)))
    (is (= [] (:functions (types/function-table [(assoc node :form :literal)]))))))

(deftest types-stage-callbacks-and-compatibility-entry-points-remain-interposable
  (is (= "I64" (compatibility/literal-type 1) (types/literal-type 1)))
  (let [calls (atom []) node {:form :literal :value :payload}]
    (is (= "CallerType"
           (types/node-type (fn [value] (swap! calls conj [:literal value]) "CallerType")
                            (fn [value] (swap! calls conj [:operator value]) nil) node)))
    (is (= [[:operator node] [:literal :payload]] @calls)))
  (doseq [[name arguments] [['literal-type [1]] ['node-operator [{:children {:operator :call}}]]
                            ['node-type [identity identity {:form :literal :value :x}]]
                            ['type-fact [identity {:node-id :node}]] ['type-environment [[]]]
                            ['constraint-ledger [[]]] ['function-table [[]]]]]
    (let [stage-var (ns-resolve 'gravity.types name)
          wrapper (ns-resolve 'gravity.c7-type-checker.inference name)
          calls (atom []) marker (Object.)]
      (with-redefs-fn {stage-var (fn [& values] (swap! calls conj values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= [arguments] @calls))))
  (is (= "StageInterposition"
         (with-redefs [types/literal-type (constantly "StageInterposition")]
           (c7/c7-node-type {:form :literal :value 1})))))
