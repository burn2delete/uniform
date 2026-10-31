(ns gravity.c8-effect-checker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c8-effect-checker :as c8]
            [gravity.c8-effect-checker.facts :as compatibility]
            [gravity.effects :as effects]))

(def module
  {:module 'gravity.c8-test
   :source-path "c8-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write :runtime/dynamic-dispatch}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(def type-facts
  [{:core-node "c7-node-0"
    :source {:syntax-id "syntax-0"
             :span {:source "c8-test.gravity" :form-index 0}
             :origin-chain [{:kind :source}]}
    :type "CheckedCast[String]"
    :profile :hosted
    :target :jvm
    :effects #{:io/write}
    :capabilities #{:io/stdout}}])

(def c7-artifact
  {:kind :gravity/stage0-c7-type-checker-artifact
   :artifact-id "sha256:c7"
   :typed-core-module {:artifact :gravity/typed-core :status :complete}
   :type-environment {:status :complete}
   :type-facts type-facts
   :function-type-table
   {:functions [{:fn-id "fn-0" :latent-effects #{:io/write}
                 :throws #{"String"}}]}
   :capability-based-proof {:status :complete}})

(defn operations
  ([] (operations module))
  ([module-value]
   {:read-source-form-records
    (fn [_ _]
      [{:form '(ns gravity.c8-test (:profile :hosted) (:target :jvm))}])
    :validate-ns-syntax! (fn [_ _] nil)
    :parse-module (fn [_ _] module-value)
    :compiler-c7-type-source-artifact (fn [_ _] c7-artifact)}))

(deftest effects-stage-contract-preserves-the-native-data-boundary
  (let [contract (:gravity/namespace-contract (meta #'effects/effect-graph))
        publics (ns-publics 'gravity.effects)]
    (is (= :hosted-seed-effect-graph-data (:contract-boundary contract)))
    (is (= #{'fact-direct-effects 'effectful-facts 'effect-graph} (set (keys publics))))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= contract (:gravity/namespace-contract (meta (publics name)))))
      (is (= :none (:admission api)))
      (is (true? (:seed-compatibility-data-only? api)))
      (is (false? (:authenticates-inputs? api)))
      (is (false? (:proves-effect-legality? api)))
      (is (false? (:grants-authority? api))))
    (is (= '[clojure.set] (get-in contract [:dependency-direction :requires])))
    (is (= '[gravity.c8-effect-checker.facts] (get-in contract [:dependency-direction :consumed-by])))
    (is (false? (:legacy-status-labels-authoritative? contract)))
    (is (false? (:legacy-build-grants-prove-performed-effects? contract)))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest effects-stage-preserves-lazy-classification-and-source-data
  (doseq [[type expected] [["CheckedCast[String]" :runtime/dynamic-dispatch]
                          ["ProtocolValue" :runtime/dynamic-dispatch]
                          ["SchemaDerived" :runtime/dynamic-dispatch]
                          ["UnsafeIsland[Dynamic]" :memory/raw]
                          ["Never" :error/throw]]]
    (is (= #{:io/write expected} (effects/fact-direct-effects {:type type :effects #{:io/write}}))))
  (is (= #{} (effects/fact-direct-effects {:type "CallerClassification" :effects []})))
  (let [source {:syntax-id :syntax :origin-chain [:source] :generated-origin [:macro]}
        facts [{:core-node :pure :effects #{} :source source}
               {:core-node :effectful :effects #{:time/read} :source source}]
        calls (atom []) direct (fn [fact] (swap! calls conj fact) (:effects fact))
        pending (effects/effectful-facts direct facts)]
    (is (empty? @calls))
    (is (= [(second facts)] (vec pending)))
    (is (= facts @calls))
    (let [graph (effects/effect-graph #(effects/effectful-facts direct %) direct #{:time/read}
                                     {:module 'test.effects :effects #{:time/read}
                                      :metadata {:build-grants #{:build/env}}}
                                     facts {:functions [{:fn-id :function :latent-effects #{:error/throw}
                                                         :throws #{'CallerError}}]})]
      (is (= #{:time/read} (get-in graph [:namespace :inferred]) (:replay-required graph)))
      (is (= :pure (get-in graph [:nodes :pure :ordering])))
      (is (= :sequence (get-in graph [:nodes :effectful :ordering])))
      (is (identical? source (get-in graph [:nodes :effectful :source])))
      (is (= #{:error/throw} (get-in graph [:functions :function :latent])))
      (is (= #{'CallerError} (get-in graph [:functions :function :throws])))
      (is (= [:build/env] (:build-effects graph)))
      (is (= :complete (:status graph))))))

(deftest effects-stage-compatibility-entry-points-remain-interposable
  (doseq [[name arguments] [['fact-direct-effects [{:effects #{:io/write}}]]
                            ['effectful-facts [identity []]]
                            ['effect-graph [identity identity #{} {} [] {}]]]]
    (let [stage-var (ns-resolve 'gravity.effects name)
          wrapper (ns-resolve 'gravity.c8-effect-checker.facts name)
          calls (atom []) marker (Object.)]
      (with-redefs-fn {stage-var (fn [& values] (swap! calls conj values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= [arguments] @calls))))
  (is (= #{:stage-classification}
         (with-redefs [effects/fact-direct-effects (constantly #{:stage-classification})]
           (c8/c8-fact-direct-effects {:effects #{}}))))
  (is (= #{:io/write} (compatibility/fact-direct-effects {:effects #{:io/write}}))))

(deftest contract-is-hosted-compatible-and-nonauthoritative
  (let [contract (c8/c8-engine-contract)
        publics (ns-publics 'gravity.c8-effect-checker)]
    (is (= :hosted-stage0-c8-effect-checker
           (:contract-boundary contract)))
    (is (true? (:compatibility-only? contract)))
    (is (false? (:canonical-c8-authority? contract)))
    (is (false? (:legality-model-complete? contract)))
    (is (some #{:package-grant-authority} (:does-not-own contract)))
    (is (some #{:safety-legality} (:does-not-own contract)))
    (is (true? (get-in contract
                       [:operation-interposition
                        :unknown-keys-rejected?])))
    (is (= #{'clojure.set 'gravity.digest}
           (set (get-in contract [:dependency-direction :requires]))))
    (is (= (set (keys publics)) (set (keys c8/public-api))))
    (doseq [[name spec] c8/public-api :when (:arglists spec)]
      (is (= (:arglists spec) (:arglists (meta (get publics name))))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest operation-map-is-validated-and-preserves-nested-interposition
  (doseq [operations [nil
                      {:unknown identity}
                      {:c8-effect-graph :keyword-is-invokable}
                      {:c8-effect-diagnostic-ids [:not-a-string]}
                      {:c8-effect-governing-document ""}
                      {:c8-effect-rejected-designs [:not-a-map]}
                      {:c8-effect-override-diagnostics {:verify :not-a-string}}
                      {:c8-known-effects #{"not-a-keyword"}}
                      {:c8-effect-capability {:io/write "not-a-keyword"}}
                      {:c8-replay-sensitive-effects #{}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (c8/with-operations operations (constantly :unreachable)))))
  (let [calls (atom [])]
    (let [lazy-facts
          (c8/with-operations
            {:c8-fact-direct-effects
             (fn [fact]
               (swap! calls conj fact)
               #{:interposed/effect})}
            #(c8/c8-effectful-facts type-facts))]
      (is (= type-facts (vec lazy-facts))))
    (is (= 1 (count @calls)))))

(deftest effect-engine-emits-complete-products
  (let [artifact (c8/with-operations
                   (operations)
                   #(c8/compiler-c8-effect-source-artifact
                     "c8-test.gravity" "ignored"))]
    (is (= :gravity/stage0-c8-effect-checker-artifact (:kind artifact)))
    (is (= #{:io/write :runtime/dynamic-dispatch}
           (get-in artifact [:effect-graph :namespace :inferred])))
    (is (= :accepted (get-in artifact [:effect-legality-report :status])))
    (is (every? #(= :accepted (:status %))
                (get-in artifact [:capability-proof-records :records])))
    (is (= :complete (get-in artifact [:build-effect-log :status])))
    (is (seq (get-in artifact [:replay-effect-requirements :records])))
    (is (seq (get-in artifact [:effect-ordering-constraints :records])))
    (is (seq (get-in artifact [:residual-effect-report :records])))
    (is (= :passed (get-in artifact [:effect-verifier-report :status])))
    (is (= :complete (get-in artifact [:capability-based-proof :status])))
    (is (= (set c8/c8-effect-diagnostic-ids)
           (set (map :diagnostic
                     (get-in artifact [:effect-diagnostics :diagnostics])))))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id artifact)))))

(deftest diagnostic-overrides-retain-c8-identities
  (doseq [[failure expected-id]
          [[:undeclared "C8-UNDECLARED"]
           [:profile "C8-PROFILE"]
           [:capability "C8-CAPABILITY"]
           [:build "C8-BUILD"]
           [:replay "C8-REPLAY"]
           [:order "C8-ORDER"]
           [:runtime "C8-RUNTIME"]
           [:unknown "C8-UNKNOWN"]
           [:verify "C8-VERIFY"]]]
    (testing expected-id
      (let [failed-module (assoc-in module
                                    [:metadata :compiler :c8-effect-check :fail]
                                    failure)
            error (try
                    (c8/with-operations
                      (operations failed-module)
                      #(c8/compiler-c8-effect-source-artifact
                        "c8-test.gravity" "ignored"))
                    nil
                    (catch clojure.lang.ExceptionInfo exception exception))]
        (is (some? error))
        (is (= expected-id (:id (ex-data error))))
        (is (= "C8" (:document-id (ex-data error))))))))
