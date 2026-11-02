(ns gravity.c8-effect-checker-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.c8-effect-checker :as c8]
            [gravity.c8-effect-checker.facts :as compatibility]
            [gravity.c8-effect-checker.verification :as report-compatibility]
            [gravity.c8-effect-checker.evidence :as record-compatibility]
            [gravity.c8-effect-checker.artifact :as artifact-compatibility]
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
    (is (= :hosted-seed-effect-data-and-native-reports (:contract-boundary contract)))
    (is (= #{'fact-direct-effects 'effectful-facts 'effect-graph
             'native-verifier-report 'native-capability-proof 'validate-native-report!
             'native-source-overrides 'native-diagnostic-fail! 'validate-native-overrides! 'native-diagnostics
             'assemble-source-artifact 'legality-records 'capability-proof-records 'build-effect-log
             'replay-requirements 'ordering-constraints 'residual-effect-report} (set (keys publics))))
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
    (is (= '[gravity.c8-effect-checker.facts gravity.c8-effect-checker.verification gravity.c8-effect-checker.evidence gravity.c8-effect-checker.artifact gravity.c8-effect-checker.diagnostics]
           (get-in contract [:dependency-direction :consumed-by])))
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

(deftest effects-stage-record-wrappers-preserve-argument-identity
  (doseq [[operation arity] [['legality-records 3] ['capability-proof-records 3]
                             ['build-effect-log 2] ['replay-requirements 1]
                             ['ordering-constraints 1] ['residual-effect-report 1]]]
    (let [arguments (vec (repeatedly arity #(Object.)))
          marker (Object.) observed (atom nil)
          stage (ns-resolve 'gravity.effects operation)
          wrapper (ns-resolve 'gravity.c8-effect-checker.evidence operation)]
      (with-redefs-fn {stage (fn [& values] (reset! observed values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= arity (count @observed)))
      (is (every? true? (map identical? arguments @observed))))))

(deftest effects-stage-assembly-wrapper-preserves-argument-identity
  (let [ops (Object.) source (Object.) text (Object.) marker (Object.) observed (atom nil)]
    (with-redefs [effects/assemble-source-artifact
                  (fn [& values] (reset! observed values) marker)]
      (is (identical? marker (artifact-compatibility/source-artifact ops source text))))
    (is (= 3 (count @observed)))
    (is (every? true? (map identical? [ops source text] @observed)))))

(deftest effects-stage-diagnostic-wrappers-preserve-port-identity
  (doseq [[old stage-name arity]
          [['source-overrides 'native-source-overrides 1]
           ['fail! 'native-diagnostic-fail! 8]
           ['validate-overrides! 'validate-native-overrides! 7]
           ['effect-diagnostics 'native-diagnostics 6]]]
    (let [arguments (vec (repeatedly arity #(Object.)))
          observed (atom nil) marker (Object.)
          stage (ns-resolve 'gravity.effects stage-name)
          wrapper (ns-resolve 'gravity.c8-effect-checker.diagnostics old)]
      (with-redefs-fn {stage (fn [& values] (reset! observed values) marker)}
        #(is (identical? marker (apply wrapper arguments))))
      (is (= arity (count @observed)))
      (is (every? true? (map identical? arguments @observed))))))

(deftest effects-stage-native-failure-preserves-owner-order-and-exceptions
  (doseq [stop [nil :message :span :failure]]
    (let [events (atom []) marker (Object.)
          error (ex-info "owner failure" {:marker marker})
          observe (fn [event result]
                    (fn [& _]
                      (swap! events conj event)
                      (when (= stop event) (throw error)) result))
          result (try (effects/native-diagnostic-fail!
                        (observe :failure marker) (observe :message "owner text")
                        (observe :span {:source "native.gravity"}) "C8 contract"
                        "C8-CAPABILITY" "native.gravity" {} {})
                      (catch clojure.lang.ExceptionInfo failure failure))]
      (is (= (case stop :message [:message] :span [:message :span]
                   [:message :span :failure]) @events))
      (is (identical? (if stop error marker) result)))))

(deftest effects-stage-diagnostics-preserve-source-fields-and-eager-fallback
  (let [span (Object.) origin (Object.) extra {:effect :owner/extra}
        payload (atom nil) span-calls (atom [])
        source-span (fn [& values] (swap! span-calls conj values) :fallback)
        result (effects/native-diagnostic-fail!
                 (fn [id message data] (reset! payload [id message data]) :owner-return)
                 (constantly "owner text") source-span "C8 contract"
                 "C8-CAPABILITY" "native.gravity"
                 {:source {:span span :origin-chain origin} :core-node "node"}
                 extra)]
    (is (= :owner-return result))
    (is (empty? @span-calls))
    (is (= ["C8-CAPABILITY" "owner text"] (subvec @payload 0 2)))
    (is (identical? span (get-in @payload [2 :source-span])))
    (is (identical? origin (get-in @payload [2 :generated-origin-chain])))
    (is (= :owner/extra (get-in @payload [2 :effect])))
    (is (= "node" (get-in @payload [2 :core-node-id])))
    (is (contains? (nth @payload 2) :profile))
    (is (nil? (get-in @payload [2 :profile])))
    (is (nil? (get-in @payload [2 :target])))
    (let [ids ["C8-CAPABILITY"]
          registry (effects/native-diagnostics source-span ids
                     [{:diagnostic "C8-CAPABILITY" :fixture :negative}]
                     {} "native.gravity" [{:core-node "node" :source {:span span :origin-chain origin}}])]
      ;; The existing get-in default is evaluated even when the fact has a span.
      (is (= [["native.gravity" 0]] @span-calls))
      (is (identical? ids (:required-diagnostic-ids registry)))
      (is (identical? span (get-in registry [:diagnostics 0 :source-span])))
      (is (identical? origin (get-in registry [:diagnostics 0 :generated-origin-chain]))))))

(deftest effects-stage-native-overrides-retain-data-and-owner-refusal
  (is (= {} (effects/native-source-overrides {})))
  (doseq [value [nil false (Object.)]]
    (is (identical? value (effects/native-source-overrides
                           {:metadata {:compiler {:c8-effect-check value}}}))))
  (let [failure (ex-info "owner refusal" {}) calls (atom [])
        fail (fn [& values] (swap! calls conj values) (throw failure))
        result (try (effects/validate-native-overrides!
                      fail (fn [path index] {:source path :form-index index})
                      {:capability "C8-CAPABILITY"} {:capability :io/stdout}
                      "native.gravity" module {:fail :capability})
                    (catch clojure.lang.ExceptionInfo error error))]
    (is (identical? failure result))
    (is (= 1 (count @calls)))
    (is (= ["C8-CAPABILITY" "native.gravity"] (vec (take 2 (first @calls)))))
    (is (= {:missing-fields [:capability]} (last (first @calls))))))

(deftest effects-stage-assembly-preserves-sequencing-and-refusal-barriers
  (doseq [stop [nil :overrides :validate]]
    (let [events (atom []) failure (ex-info "owner refusal" {:marker (Object.)})
          supplied (into {} (for [[key operation] (operations)]
                              [key (fn [& values]
                                     (swap! events conj key)
                                     (apply operation values))]))
          supplied (assoc supplied
                          :c8-effect-validate-overrides!
                          (fn [& values]
                            (swap! events conj :overrides)
                            (when (= :overrides stop) (throw failure))
                            (apply c8/c8-effect-validate-overrides! values))
                          :c8-effect-validate!
                          (fn [& values]
                            (swap! events conj :validate)
                            (when (= :validate stop) (throw failure))
                            (apply c8/c8-effect-validate! values))
                          :c8-effect-capability-proof
                          (fn [& values]
                            (swap! events conj :proof)
                            (apply c8/c8-effect-capability-proof values)))
          result (try (c8/with-operations supplied
                        #(c8/compiler-c8-effect-source-artifact "c8-test.gravity" "ignored"))
                      (catch clojure.lang.ExceptionInfo error error))]
      (is (= (case stop
               :overrides [:read-source-form-records :validate-ns-syntax! :parse-module :overrides]
               :validate [:read-source-form-records :validate-ns-syntax! :parse-module :overrides
                          :compiler-c7-type-source-artifact :validate]
               [:read-source-form-records :validate-ns-syntax! :parse-module :overrides
                :compiler-c7-type-source-artifact :validate :proof :proof])
             @events))
      (if stop
        (is (identical? failure result))
        (do (is (= :passed (get-in result [:effect-verifier-report :status])))
            (is (re-matches #"sha256:[0-9a-f]{64}" (:artifact-id result))))))))

(deftest effects-stage-records-preserve-labels-order-and-legacy-nonclaims
  (let [effect-capability {:io/write :io/stdout :memory/raw :memory/raw}
        module {:module 'records.test :profile :hosted :target :jvm
                :effects #{:io/write} :capabilities #{:io/stdout}
                :metadata {:build-grants #{:build/network :build/env}}}
        graph {:namespace {:inferred #{:memory/raw :io/write :error/throw}}
               :replay-required #{:time/read :random/read}
               :nodes (array-map "z" {:direct #{:io/write} :ordering :sequence}
                                 "pure" {:direct #{} :ordering :pure}
                                 "a" {:direct #{:memory/raw} :ordering :sequence})}
        legality (effects/legality-records effect-capability module graph)
        proofs (effects/capability-proof-records effect-capability module graph)
        build (effects/build-effect-log effect-capability module)]
    (is (= [:error/throw :io/write :memory/raw] (mapv :effect (:records legality))))
    (is (= [#{} #{:io/stdout} #{}] (mapv :granted-capabilities (:records legality))))
    ;; These legacy accepted labels do not establish the missing declaration or grant.
    (is (false? (get-in legality [:records 2 :allowed-by :namespace])))
    (is (= [:accepted :accepted :accepted] (mapv :result (:records legality))))
    (is (= [:accepted :accepted :rejected] (mapv :status (:records proofs))))
    (is (= :stage0/raw (get-in proofs [:records 2 :grant :grant/id])))
    (is (= [:build/env :build/network] (mapv :effect (:records build))))
    (is (every? :granted? (:records build)))
    (is (= ["c8-replay-read" "c8-replay-read"]
           (mapv :record-id (:records (effects/replay-requirements graph)))))
    (is (= [:random/read :time/read]
           (mapv :effect (:records (effects/replay-requirements graph)))))
    (is (= ["z" "a"] (mapv :core-node (:records (effects/ordering-constraints graph)))))
    (is (= [:error/throw :memory/raw]
           (mapv :effect (:records (effects/residual-effect-report graph)))))
    (is (= legality (record-compatibility/legality-records effect-capability module graph)))))

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

(defn- native-report-arguments []
 (let [graph (c8/c8-effect-graph module type-facts (:function-type-table c7-artifact))]
  [c8/c8-known-effects c8/c8-effect-diagnostic-ids module graph
   (c8/c8-legality-records module graph) (c8/c8-capability-proof-records module graph)
   (c8/c8-build-effect-log module) (c8/c8-replay-requirements graph)
   (c8/c8-ordering-constraints graph) (c8/c8-residual-effect-report graph)
   (c8/c8-effect-diagnostics "original.qst" type-facts)]))

(deftest native-reports-preserve-success-and-every-existing-verifier-predicate
 (let [arguments (native-report-arguments)
       report (apply effects/native-verifier-report arguments)]
  (is (= :passed (:status report)))
  (is (= report (apply report-compatibility/verifier-report arguments)))
  (is (= :complete (effects/validate-native-report! effects/native-capability-proof
                    (fn [& _] (throw (ex-info "unexpected refusal" {})))
                    "original.qst" {:effect-verifier-report report})))
  (doseq [[key mutate]
          [[:known-effects? #(assoc % 0 #{})]
           [:declarations-cover-inferred-effects? #(assoc-in % [2 :effects] #{})]
           [:legality-intersections-accepted? #(assoc-in % [4 :records] [{:result :rejected}])]
           [:capability-proofs-accepted? #(assoc-in % [5 :records] [{:status :rejected}])]
           [:build-effects-authorized? #(assoc-in % [6 :records] [{:status :rejected}])]
           [:replay-obligations-recorded? #(-> % (assoc-in [3 :replay-required] #{:time/read}) (assoc-in [7 :records] []))]
           [:ordering-constraints-recorded? #(assoc-in % [8 :records] [])]
           [:residual-effects-recorded? #(assoc-in % [9 :status] :failed)]
           [:diagnostics-covered? #(assoc-in % [10 :diagnostics] [])]]]
   (let [failed (apply effects/native-verifier-report (mutate arguments))]
    (is (false? (get failed key)) (str key))
    (is (= :failed (:status failed)) (str key))))
  (let [empty-nodes (apply effects/native-verifier-report (assoc-in arguments [3 :nodes] {}))]
   ;; The legacy report's status omits this bit; validation still checks it.
   (is (false? (:every-effectful-node-recorded? empty-nodes)))
   (is (= :passed (:status empty-nodes)))
   (let [seen (atom [])]
    (is (= :complete (effects/validate-native-report! effects/native-capability-proof
                      (fn [& args] (swap! seen conj args)) "original.qst" {:effect-verifier-report empty-nodes})))
    (is (= [["C8-VERIFY" "original.qst" {:stage :effect-check}
             {:missing-fields [:effect-graph-complete?]}]] @seen))))))

(def ^:private native-proof-fields
 [[:effect-graph-complete? "C8-VERIFY"]
  [:declared-effect-allowance-checked? "C8-UNDECLARED"]
  [:legality-intersection-recorded? "C8-PROFILE"]
  [:capability-proofs-accepted? "C8-CAPABILITY"]
  [:build-effects-separated-and-authorized? "C8-BUILD"]
  [:replay-obligations-recorded? "C8-REPLAY"]
  [:ordering-constraints-recorded? "C8-ORDER"]
  [:residual-effects-recorded? "C8-RUNTIME"]
  [:diagnostics-covered? "C8-VERIFY"]
  [:verifier-passed? "C8-VERIFY"]])

(deftest native-report-validation-preserves-order-callback-source-and-truthiness
 (let [artifact (Object.) source "original.gravity"
       complete (zipmap (map first native-proof-fields) (repeat true))]
  (doseq [[field id] native-proof-fields absent [false nil]]
   (let [seen (atom []) proof (assoc complete field absent)]
    (is (= :complete
         (effects/validate-native-report!
          (fn [received] (is (identical? artifact received)) proof)
          (fn [& args] (swap! seen conj args)) source artifact)))
    (is (= [[id source {:stage :effect-check} {:missing-fields [field]}]] @seen))))
  (let [seen (atom [])]
   (effects/validate-native-report! (constantly {}) (fn [& args] (swap! seen conj args)) source artifact)
   (is (= (mapv (fn [[field id]] [id source {:stage :effect-check} {:missing-fields [field]}]) native-proof-fields) @seen)))
  (is (= :complete (effects/validate-native-report! (constantly (zipmap (map first native-proof-fields) (repeat :legacy-truthy)))
                    (fn [& _] (throw (ex-info "unexpected callback" {}))) source artifact)))))

(deftest native-projection-is-legacy-data-and-retains-field-mapping
 (let [report {:every-effectful-node-recorded? :nodes :declarations-cover-inferred-effects? :declared
               :legality-intersections-accepted? :legality :capability-proofs-accepted? :capabilities
               :build-effects-authorized? :build :replay-obligations-recorded? :replay
               :ordering-constraints-recorded? :order :residual-effects-recorded? :residual
               :diagnostics-covered? :diagnostics :status :passed}
       proof (effects/native-capability-proof {:effect-verifier-report report})]
  (is (= {:effect-graph-complete? :nodes :declared-effect-allowance-checked? :declared
          :legality-intersection-recorded? :legality :capability-proofs-accepted? :capabilities
          :build-effects-separated-and-authorized? :build :replay-obligations-recorded? :replay
          :ordering-constraints-recorded? :order :residual-effects-recorded? :residual
          :diagnostics-covered? :diagnostics :verifier-passed? true :status :complete} proof))
  (is (false? (:verifier-passed? (effects/native-capability-proof {:effect-verifier-report (assoc report :status :complete)}))))
  (is (= :complete (:status (effects/native-capability-proof {}))))))

(deftest native-report-exceptions-propagate-without-translation
 (doseq [error [(ex-info "original" {}) (IllegalStateException. "original")]]
  (let [observed (try (effects/validate-native-report! (fn [_] (throw error))
                       (fn [& _] (throw (ex-info "unreachable" {}))) "original.qst" {})
                     nil (catch Throwable actual actual))]
   (is (identical? error observed)))
  (let [seen (atom []) observed (try (effects/validate-native-report! (constantly {})
                                     (fn [& args] (swap! seen conj args) (throw error)) "original.qst" {})
                                   nil (catch Throwable actual actual))]
   (is (identical? error observed))
   (is (= [["C8-VERIFY" "original.qst" {:stage :effect-check} {:missing-fields [:effect-graph-complete?]}]] @seen)))))

(deftest native-report-compatibility-wrappers-remain-interposable
 (doseq [[stage-name wrapper-name arguments]
         [['native-verifier-report 'verifier-report (native-report-arguments)]
          ['native-capability-proof 'capability-proof [{}]]
          ['validate-native-report! 'validate! [identity identity "original.qst" {}]]]]
  (let [stage-var (ns-resolve 'gravity.effects stage-name)
        wrapper (ns-resolve 'gravity.c8-effect-checker.verification wrapper-name)
        seen (atom nil) marker (Object.)]
   (with-redefs-fn {stage-var (fn [& args] (reset! seen args) marker)}
    #(is (identical? marker (apply wrapper arguments))))
   (is (= arguments @seen))
   (is (= (:arglists (meta stage-var)) (:arglists (meta wrapper)))))))
