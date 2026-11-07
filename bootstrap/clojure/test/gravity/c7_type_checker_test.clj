(ns gravity.c7-type-checker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c7-type-checker :as c7]
            [gravity.c7-type-checker.inference :as compatibility]
            [gravity.c7-type-checker.evidence :as record-compatibility]
            [gravity.c7-type-checker.artifact :as artifact-compatibility]
            [gravity.c7-type-checker.verification :as verification]
            [gravity.c7-type-checker.diagnostics :as diagnostic-compatibility]
            [gravity.c7-type-checker.catalog :as catalog-compatibility]
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
    (is (= :seed-type-products-current-gravity-runtime-and-native-annotation-checks (:contract-boundary contract)))
    (is (= 32 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= contract (:gravity/namespace-contract (meta (publics name)))))
      (is (= :none (:admission api)))
      (is (or (true? (:seed-compatibility-data-only? api))
              (true? (:current-gravity-runtime-data-only? api))
              (true? (:seed-compatibility-checks-only? api))))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-canonical-types? api)))
      (is (false? (:grants-authority? api))))
    (is (= [] (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.c7-type-checker.inference gravity.c7-type-checker.verification gravity.c7-type-checker.evidence gravity.c7-type-checker.artifact gravity.c7-type-checker.diagnostics gravity.c7-type-checker.catalog gravity.bootstrap]
           (get-in contract [:dependency-direction :consumed-by])))
    (is (false? (:legacy-status-labels-authoritative? contract)))
    (is (not (contains? (ns-aliases 'gravity.types) 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest native-diagnostic-catalog-and-wrappers-retain-interposition
  (doseq [[stage-name compatibility-name]
          [['native-diagnostic-ids 'diagnostic-ids]
           ['native-governing-document 'governing-document]
           ['native-rejected-designs 'rejected-designs]
           ['native-override-diagnostics 'override-diagnostics]]]
    (let [stage-var (ns-resolve 'gravity.types stage-name)
          wrapper-var (ns-resolve 'gravity.c7-type-checker.catalog compatibility-name)]
      (is (identical? @stage-var @wrapper-var))
      (is (= :data (get-in (meta stage-var)
                           [:gravity/namespace-contract :public-api stage-name :api-kind])))))
  (is (= 10 (count catalog-compatibility/diagnostic-ids)))
  (is (= "Type checking failed" (catalog-compatibility/type-message :unknown)))
  (is (= "typed-core verifier rejected the artifact"
         (catalog-compatibility/type-message "C7-VERIFY")))
  (is (= :live-message
         (with-redefs [types/native-type-message (constantly :live-message)]
           (catalog-compatibility/type-message "C7-VERIFY"))))
  (doseq [[stage-name wrapper-name arity]
          [['native-type-fail! 'type-fail! 8]
           ['validate-native-overrides! 'validate-overrides! 6]
           ['native-type-diagnostics 'type-diagnostics 5]]]
    (let [arguments (vec (repeatedly arity #(Object.)))
          marker (Object.) calls (atom [])
          stage-var (ns-resolve 'gravity.types stage-name)
          wrapper-var (ns-resolve 'gravity.c7-type-checker.diagnostics wrapper-name)]
      (with-redefs-fn {stage-var (fn [& args] (reset! calls args) marker)}
        #(is (identical? marker (apply wrapper-var arguments))))
      (is (every? true? (map identical? arguments @calls)))))
  (let [marker (Object.)]
    (is (identical? marker
                    (with-redefs [types/native-type-fail! (constantly marker)]
                      (diagnostic-compatibility/type-fail! nil nil nil nil nil nil nil nil))))))

(deftest native-type-failure-preserves-payload-precedence-and-callback-order
  (let [events (atom []) marker (Object.)
        span {:source "owner.gravity" :form-index 4}
        origin [{:kind :macro :syntax-id :original}]
        subject {:node-id :core :source {:syntax-id :syntax :span span :origin-chain origin}
                 :binding-ref :binding :profile :hosted :target :jvm}
        captured (atom nil)
        fail (fn [id message payload]
               (swap! events conj :failure)
               (reset! captured [id message payload]) marker)
        source-span (fn [path index] (swap! events conj [:span path index]) :fallback)
        message (fn [id] (swap! events conj [:message id]) :message)]
    (is (identical? marker
                    (types/native-type-fail! fail source-span message :document
                                             "C7-VERIFY" "owner.gravity" subject {})))
    (is (= [[:message "C7-VERIFY"] :failure] @events))
    (let [[id text payload] @captured]
      (is (= ["C7-VERIFY" :message] [id text]))
      (is (identical? span (:source-span payload)))
      (is (identical? origin (:generated-origin-chain payload)))
      (is (= {:diagnostic-family :c7-type-checker :stage :type-check :document-id "C7"
              :expected-document :document :core-node-id :core :syntax-id :syntax
              :expected-type "Typed" :actual-type "Dynamic" :active-profile :hosted
              :target :jvm :relevant-binding-id :binding}
             (dissoc payload :source-span :generated-origin-chain :remediation))))
    (doseq [[subject expected]
            [[(assoc subject :source-span :direct :span :third) :direct]
             [(assoc subject :span :third) span]
             [{:span :third} :third]
             [{} :fallback]]]
      (types/native-type-fail! fail source-span message :document :id "owner.gravity" subject {})
      (is (= expected (get-in @captured [2 :source-span]))))
    (reset! events [])
    (types/native-type-fail! fail source-span message :document :id "owner.gravity" {}
                             {:source-span :extra :document-id :extra :active-profile :extra})
    ;; The owner span callback still runs before the extra payload overwrites it.
    (is (= [[:message :id] [:span "owner.gravity" 0] :failure] @events))
    (is (= :extra (get-in @captured [2 :source-span])))
    (is (= :extra (get-in @captured [2 :document-id])))
    (is (= :extra (get-in @captured [2 :active-profile])))
    (is (nil? (get-in @captured [2 :target]))))
  (doseq [failure-point [:message :span :failure]]
    (let [events (atom []) error (ex-info "owner failure" {:at failure-point})
          callback (fn [point result]
                     (fn [& _]
                       (swap! events conj point)
                       (if (= point failure-point) (throw error) result)))
          caught (try
                   (types/native-type-fail! (callback :failure nil) (callback :span :span)
                                            (callback :message :text) :document :id "x.gravity" {} {})
                   nil (catch clojure.lang.ExceptionInfo e e))]
      (is (identical? error caught))
      (is (= (case failure-point :message [:message] :span [:message :span]
                   :failure [:message :span :failure]) @events)))))

(deftest native-registry-and-override-retain-source-data-without-admission
  (let [calls (atom []) span {:source "registry.gravity" :form-index 3}
        origin [{:kind :source}]
        ids ["C7-VERIFY"] designs [{:diagnostic "C7-VERIFY" :fixture "fixture.gravity"}]
        source-span (fn [path index] (swap! calls conj [path index]) :fallback)
        registry (types/native-type-diagnostics source-span ids designs "registry.gravity"
                                                [{:node-id :core :source {:syntax-id :syntax
                                                                                       :span span :origin-chain origin}
                                                  :profile :hosted :target :jvm}])
        diagnostic (first (:diagnostics registry))]
    (is (identical? ids (:required-diagnostic-ids registry)))
    (is (= [["registry.gravity" 0]] @calls))
    (is (identical? span (:source-span diagnostic)))
    (is (identical? origin (:generated-origin-chain diagnostic)))
    (is (= {:diagnostic "C7-VERIFY" :fixture "fixture.gravity" :core-node-id :core
            :syntax-id :syntax :expected-type "Expected" :actual-type "Actual"
            :active-profile :hosted :target :jvm :relevant-binding-id :core}
           (dissoc diagnostic :source-span :generated-origin-chain :remediation)))
    (is (= :complete (:status registry)))
    (let [captured (atom nil) error (ex-info "override fixture" {})
          caught (try
                   (types/validate-native-overrides!
                    source-span (fn [& args] (reset! captured args) (throw error))
                    types/native-override-diagnostics "registry.gravity" module {:fail :verify})
                   nil (catch clojure.lang.ExceptionInfo e e))]
      (is (identical? error caught))
      (is (= ["C7-VERIFY" "registry.gravity"] (vec (take 2 @captured))))
      (is (= {:source-span :fallback :syntax-id "fixture-override" :core-node-id "fixture-override"
              :expected-type "Expected" :actual-type "Actual" :profile :hosted :target :jvm
              :generated-origin []} (nth @captured 2)))
      (is (= {:missing-fields [:verify]} (nth @captured 3))))
    (is (nil? (types/validate-native-overrides! source-span (fn [& _] (throw (Exception.)))
                                               types/native-override-diagnostics "x.gravity" module
                                               {:fail :unknown})))))

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

(deftest types-stage-native-verification-retains-products-and-refusals
  (let [artifact (c7/with-operations (operations)
                   #(c7/compiler-c7-type-source-artifact "c7-test.gravity" "ignored"))
        arguments [nodes (:type-facts artifact) (:constraint-ledger artifact)
                   (:function-type-table artifact) (:dynamic-boundary-records artifact)
                   (:cast-conversion-records artifact) (:generic-instantiation-table artifact)
                   (:protocol-dispatch-type-table artifact) (:schema-type-links artifact)
                   (:layout-facts artifact)]
        report (apply types/typed-core-verifier-report arguments)
        pending (assoc-in arguments [2 :constraints 0 :status] :pending)
        rejected (apply types/typed-core-verifier-report pending)
        failed-artifact (assoc artifact :typed-core-verifier-report rejected)
        error (try (c7/c7-type-validate! "c7-test.gravity" failed-artifact)
                   nil
                   (catch clojure.lang.ExceptionInfo error error))]
    ;; These supplied hosted seed fixtures are not canonical type evidence.
    (is (= :passed (:status report)))
    (is (= (:typed-core-verifier-report artifact) report))
    (is (= (:capability-based-proof artifact)
           (types/type-capability-proof c7/c7-type-diagnostic-ids artifact)))
    (is (= :failed (:status rejected)))
    (is (false? (:constraints-solved? rejected)))
    (is (= "C7-VERIFY" (:id (ex-data error))))
    (is (= [:constraints-solved?] (:missing-fields (ex-data error))))
    (is (= "C7" (:document-id (ex-data error))))
    (let [layout-report (apply types/typed-core-verifier-report
                               (assoc-in arguments [9 :records 0 :status] :pending))
          layout-error (try (c7/c7-type-validate!
                             "c7-test.gravity"
                             (assoc artifact :typed-core-verifier-report layout-report))
                            nil
                            (catch clojure.lang.ExceptionInfo error error))]
      (is (false? (:layout-facts-recorded? layout-report)))
      (is (= "C7-LAYOUT" (:id (ex-data layout-error))))
      (is (= [:layout-facts-recorded?] (:missing-fields (ex-data layout-error)))))
    (let [missing-origin (update-in arguments [0 0 :source] dissoc :syntax-id)
          report (apply types/typed-core-verifier-report missing-origin)]
      (is (= :failed (:status report)))
      (is (false? (:origins-preserved? report))))
    (is (= :failed (:status (apply types/typed-core-verifier-report
                                  (assoc arguments 1 [])))))))

(deftest types-stage-validation-preserves-callback-order-and-exception-identity
  (let [events (atom []) artifact (Object.)
        proof (fn [actual]
                (is (identical? artifact actual))
                (swap! events conj :proof)
                {})
        fail! (fn [id source subject extra]
                (swap! events conj [id source subject extra]))]
    (is (= :complete (types/validate! proof fail! "native.gravity" artifact)))
    (is (= [:proof
            ["C7-TYPE-MISMATCH" :every-core-node-has-type-or-diagnostic?]
            ["C7-VERIFY" :constraints-solved?]
            ["C7-VERIFY" :function-types-include-latent-effects?]
            ["C7-DYNAMIC" :dynamic-boundaries-profile-gated?]
            ["C7-CAST" :casts-classified?]
            ["C7-GENERIC" :generic-and-protocol-evidence?]
            ["C7-SCHEMA" :schema-identity-preserved?]
            ["C7-LAYOUT" :layout-facts-recorded?]
            ["C7-VERIFY" :diagnostics-covered?]
            ["C7-VERIFY" :verifier-passed?]]
           (into [:proof] (map (fn [[id source subject extra]]
                                (is (= "native.gravity" source))
                                (is (= {:stage :type-check} subject))
                                [id (first (:missing-fields extra))])
                              (rest @events)))))
    (let [failure (ex-info "original failure" {:marker (Object.)})
          calls (atom [])
          actual (try (types/validate! (constantly {})
                                       (fn [& args] (swap! calls conj args) (throw failure))
                                       "native.gravity" artifact)
                      nil
                      (catch clojure.lang.ExceptionInfo error error))]
      (is (identical? failure actual))
      (is (= 1 (count @calls))))))

(deftest types-stage-verification-entry-points-preserve-interposition
  (doseq [[name arguments]
          [['typed-core-verifier-report (vec (repeatedly 10 #(Object.)))]
           ['type-capability-proof [(Object.) (Object.)]]
           ['validate! [(Object.) (Object.) (Object.) (Object.)]]]]
    (let [stage-var (ns-resolve 'gravity.types name)
          wrapper (ns-resolve 'gravity.c7-type-checker.verification name)
          observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage-var (fn [& values] (reset! observed values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (every? true? (map identical? arguments @observed)))))
  (let [marker (Object.) artifact (Object.)]
    (is (identical? marker
          (with-redefs [types/type-capability-proof
                        (fn [catalog actual]
                          (is (= c7/c7-type-diagnostic-ids catalog))
                          (is (identical? artifact actual)) marker)]
            (c7/c7-type-capability-proof artifact)))))
  (is (= :complete
         (verification/validate! (constantly {}) (fn [& _]) nil nil))))

(deftest types-stage-records-retain-source-unsafe-and-callback-data
  (let [source {:syntax-id :syntax :span {:source "native.gravity" :form-index 3}
                :origin-chain [:source] :generated-origin {:from :macro}}
        unsafe {:unsafe-island :island :ai/human-review :required}
        value (assoc (node 12 :call {:operator 'dynamic/cast})
                     :source source :unsafe-metadata unsafe)
        events (atom [])
        operator (fn [actual] (swap! events conj actual)
                   (get-in actual [:children :operator]))
        cast (first (:records (types/cast-records operator [value])))
        boundary (first (:records (types/dynamic-boundary-records
                                    (constantly 'dynamic/value) [value] {:profile :native})))
        schema (first (:records (types/schema-links
                                 [{:source source :semantic-anchor {:source-syntax :schema}
                                   :domain :schema-ir :profile :hosted :target :jvm}])))
        layout (first (:records (types/layout-facts (constantly :caller-type)
                                                   [(assoc value :profile :unknown)])))]
    (is (= [value] @events))
    (is (identical? value (first @events)))
    (is (identical? source (:source cast)))
    (is (identical? unsafe (:unsafe-metadata cast)))
    (is (= (:node-id value) (:source-node cast) (:source boundary) (:core-node layout)))
    (is (false? (:accepted? boundary)))
    (is (= :schema (:source-schema schema)))
    (is (= :schema-ir (:domain schema)))
    ;; These legacy labels record caller data; they prove no native layout.
    (is (= :abstract (:layout layout)))
    (is (= :recorded (:status layout)))
    (is (= :caller-type (:type layout)))
    (is (= [] (:records (types/cast-records (constantly :unknown) [value]))))
    (is (= [] (:records (types/schema-links [{:domain :other}])))))
  (is (= 1 (count (:records
                    (c7/with-operations {:c7-node-operator (constantly 'dynamic/cast)}
                      #(c7/c7-cast-records [(node 3 :call {:operator 'dynamic/value})])))))))

(deftest types-stage-record-entry-points-remain-interposable
  (doseq [[name arity] [['dynamic-boundary-records 3] ['cast-records 2]
                       ['generic-instantiations 2] ['protocol-dispatch-table 2]
                       ['schema-links 1] ['layout-facts 2]]]
    (let [arguments (vec (repeatedly arity #(Object.)))
          stage-var (ns-resolve 'gravity.types name)
          wrapper (ns-resolve 'gravity.c7-type-checker.evidence name)
          observed (atom nil) marker (Object.)]
      (with-redefs-fn {stage-var (fn [& values] (reset! observed values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= arity (count @observed)))
      (is (every? true? (map identical? arguments @observed)))))
  (is (= [] (:records (record-compatibility/schema-links [])))))

(deftest types-stage-assembly-compatibility-preserves-port-identity
  (let [ports (Object.) source (Object.) text (Object.) result (Object.)
        observed (atom nil)]
    (with-redefs [types/assemble-source-artifact
                  (fn [& values] (reset! observed values) result)]
      (is (identical? result (artifact-compatibility/source-artifact ports source text))))
    (is (= 3 (count @observed)))
    (is (every? true? (map identical? [ports source text] @observed)))))

(deftest types-stage-assembly-preserves-order-and-refusal-barriers
  (doseq [stop [nil :overrides :validate]]
    (let [events (atom []) failure (ex-info "owner refusal" {:marker (Object.)})
          supplied (into {} (for [[key operation] (operations)]
                              [key (fn [& args]
                                     (swap! events conj key)
                                     (apply operation args))]))
          supplied (assoc supplied
                          :c7-type-validate-overrides!
                          (fn [& args]
                            (swap! events conj :overrides)
                            (when (= :overrides stop) (throw failure))
                            (apply c7/c7-type-validate-overrides! args))
                          :c7-type-validate!
                          (fn [& args]
                            (swap! events conj :validate)
                            (when (= :validate stop) (throw failure))
                            (apply c7/c7-type-validate! args))
                          :c7-type-capability-proof
                          (fn [& args]
                            (swap! events conj :proof)
                            (apply c7/c7-type-capability-proof args)))
          result (try (c7/with-operations supplied
                        #(c7/compiler-c7-type-source-artifact "c7-test.gravity" "ignored"))
                      (catch clojure.lang.ExceptionInfo error error))]
      (is (= (case stop
               :overrides [:read-source-form-records :validate-ns-syntax! :parse-module :overrides]
               :validate [:read-source-form-records :validate-ns-syntax! :parse-module :overrides
                          :compiler-c6-lowering-source-artifact :validate]
               [:read-source-form-records :validate-ns-syntax! :parse-module :overrides
                :compiler-c6-lowering-source-artifact :validate :proof :proof])
             @events))
      (if stop
        (is (identical? failure result))
        (do (is (= :passed (get-in result [:typed-core-verifier-report :status])))
            (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id result))))))))
