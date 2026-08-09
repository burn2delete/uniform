(ns gravity.reader-namespace-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.reader-namespace :as reader-namespace]))

(defn- diagnostic-data
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))))

(deftest every-allowed-namespace-clause-shape-is-accepted
  (let [form
        '(ns accepted.module
           (:profile :hosted)
           (:profiles #{:hosted :core})
           (:target :jvm)
           (:targets #{:jvm :native})
           (:requires [shared.core :as shared] [other.core :as other])
           (:imports [java.time Instant] [java.util UUID])
           (:exports [main start])
           (:effects #{:io/write})
           (:capabilities #{:io/stdout})
           (:safety :safe)
           (:providers [clock console])
           (:doc "accepted")
           (:metadata {:owner :reader}))]
    (is (= reader-namespace/allowed-ns-clauses
           (set (map first (drop 2 form)))))
    (is (nil? (reader-namespace/validate-ns-syntax! "accepted.gravity"
                                                   [form])))))

(deftest malformed-namespace-shapes-have-exact-l1-payloads
  (let [path "bad.gravity"
        cases
        [{:name :name
          :form '(ns :not-a-symbol (:profile :hosted))
          :clause '(ns :not-a-symbol (:profile :hosted))
          :remediation "Use a symbolic namespace name."}
         {:name :clause
          :form '(ns bad.module [:profile :hosted])
          :clause [:profile :hosted]
          :remediation "Use list clauses such as (:profile :hosted)."}
         {:name :unknown
          :form '(ns bad.module (:unknown :value))
          :clause '(:unknown :value)
          :remediation "Use one of the L1 allowed namespace clause keys."}
         {:name :single-cardinality
          :form '(ns bad.module (:profile :hosted :core))
          :clause '(:profile :hosted :core)
          :remediation "Use exactly one value in this namespace clause."}
         {:name :set-type
          :form '(ns bad.module (:effects [:io/write]))
          :clause '(:effects [:io/write])
          :remediation "Use exactly one set value in this namespace clause."}
         {:name :vector-type
          :form '(ns bad.module (:exports #{main}))
          :clause '(:exports #{main})
          :remediation "Use exactly one vector value in this namespace clause."}
         {:name :dependency-cardinality
          :form '(ns bad.module (:requires))
          :clause '(:requires)
          :remediation "Use one or more dependency vector values in this namespace clause."}
         {:name :dependency-type
          :form '(ns bad.module (:imports java.time.Instant))
          :clause '(:imports java.time.Instant)
          :remediation "Use one or more dependency vector values in this namespace clause."}
         {:name :doc-type
          :form '(ns bad.module (:doc :not-a-string))
          :clause '(:doc :not-a-string)
          :remediation "Use exactly one string value in the doc clause."}
         {:name :metadata-type
          :form '(ns bad.module (:metadata [:not-a-map]))
          :clause '(:metadata [:not-a-map])
          :remediation "Use exactly one map value in the metadata clause."}]]
    (doseq [{:keys [name form clause remediation]} cases]
      (let [data (diagnostic-data
                  #(reader-namespace/validate-ns-syntax! path [form]))]
        (is (= "L1-NS-SHAPE" (:id data)) (str name))
        (is (= "namespace clause has invalid reader syntax shape"
               (:message data))
            (str name))
        (is (= :stage0 (:bootstrap-stage data)) (str name))
        (is (= {:source path :form-index 0} (:source-span data)) (str name))
        (is (= clause (:clause data)) (str name))
        (is (= remediation (:remediation data)) (str name))))))

(deftest nil-and-non-namespace-inputs-are-no-ops
  (doseq [forms [nil [] [nil] ['(def value 1)] [["not" "a" "list"]]]]
    (is (nil? (reader-namespace/validate-ns-syntax! "input.gravity" forms))
        (pr-str forms))
    (is (nil? (reader-namespace/reader-module-context forms))
        (pr-str forms)))
  (doseq [[form expected] [[nil false]
                           [[] false]
                           ['(def value 1) false]
                           ['(ns sample) true]]]
    (is (= expected (reader-namespace/ns-form? form)) (pr-str form))))

(deftest context-defaults-duplicates-and-syntax-order-are-preserved
  (let [minimal (reader-namespace/reader-module-context ['(ns minimal)])
        duplicate-form
        '(ns duplicate.module
           (:profile :core)
           (:effects #{:state/read})
           (:profile :hosted)
           (:target :native)
           (:effects #{:io/write})
           (:capabilities #{:io/stdout})
           (:safety :safe))
        duplicate (reader-namespace/reader-module-context [duplicate-form])]
    (is (= {:module 'minimal
            :profile nil
            :target :jvm
            :effects #{}
            :capabilities #{}
            :safety nil
            :namespace-clause-syntax []}
           minimal))
    (testing "L1 permits duplicates while context projection remains last-wins"
      (is (nil? (reader-namespace/validate-ns-syntax!
                 "duplicate.gravity" [duplicate-form])))
      (is (= :hosted (:profile duplicate)))
      (is (= :native (:target duplicate)))
      (is (= #{:io/write} (:effects duplicate)))
      (is (= #{:io/stdout} (:capabilities duplicate)))
      (is (= :safe (:safety duplicate))))
    (is (= [:profile :effects :profile :target :effects :capabilities :safety]
           (mapv :clause (:namespace-clause-syntax duplicate))))
    (is (= [:list :list :list :list :list :list :list]
           (mapv :raw-form-kind (:namespace-clause-syntax duplicate))))
    (is (= [[:keyword] [:set] [:keyword] [:keyword] [:set] [:set] [:keyword]]
           (mapv :value-kinds (:namespace-clause-syntax duplicate))))))

(deftest optional-operations-interpose-without-policy-injection
  (let [calls (atom [])
        failure
        (reader-namespace/fail-ns-shape!
         "interposed.gravity" :bad "repair"
         {:source-span (fn [path index]
                         (swap! calls conj [:source-span path index])
                         {:custom-span true})
          :fail! (fn [id message data]
                   (swap! calls conj [:fail id])
                   {:id id :message message :data data})})]
    (is (= [[:source-span "interposed.gravity" 0] [:fail "L1-NS-SHAPE"]]
           @calls))
    (is (= {:id "L1-NS-SHAPE"
            :message "namespace clause has invalid reader syntax shape"
            :data {:source-span {:custom-span true}
                   :clause :bad
                   :remediation "repair"}}
           failure)))
  (let [calls (atom [])
        marker (ex-info "interposed" {:marker true})]
    (is (= marker
           (try
             (reader-namespace/validate-ns-syntax!
              "interposed.gravity" ['(not-ns :invalid-name)]
              {:ns-form? (fn [form]
                           (swap! calls conj [:ns-form? form])
                           true)
               :allowed-ns-clause? (constantly true)
               :fail-ns-shape! (fn [path clause remediation]
                                 (swap! calls conj
                                        [:fail path clause remediation])
                                 (throw marker))})
             (catch clojure.lang.ExceptionInfo ex ex))))
    (is (= :ns-form? (ffirst @calls)))
    (is (= :fail (first (second @calls)))))
  (let [rejection (ex-info "interposed allowed-key rejection" {:marker true})]
    (is (= rejection
           (try
             (reader-namespace/validate-ns-syntax!
              "custom.gravity" ['(ns custom.module (:profile :hosted))]
              {:ns-form? reader-namespace/ns-form?
               :allowed-ns-clause? (constantly false)
               :fail-ns-shape! (fn [& _] (throw rejection))})
             (catch clojure.lang.ExceptionInfo ex ex)))
        "the explicit allowed-key operation remains interposable"))
  (let [calls (atom [])
        context
        (reader-namespace/reader-module-context
         ['(anything projected (:custom value))]
         {:ns-form? (fn [form]
                      (swap! calls conj [:ns-form? form])
                      true)
          :form-kind (fn [form]
                       (swap! calls conj [:form-kind form])
                       [:kind form])})]
    (is (= 'projected (:module context)))
    (is (= [{:clause :custom
             :raw-form-kind [:kind '(:custom value)]
             :value-kinds [[:kind 'value]]}]
           (:namespace-clause-syntax context)))
    (is (= 3 (count @calls)))))

(deftest operation-maps-are-exact-and-function-valued
  (doseq [[label invoke expected-keys]
          [[:failure
            #(reader-namespace/fail-ns-shape! "x" :bad "fix" %)
            #{:source-span :fail!}]
           [:validation
            #(reader-namespace/validate-ns-syntax! "x" [] %)
            #{:ns-form? :allowed-ns-clause? :fail-ns-shape!}]
           [:context
            #(reader-namespace/reader-module-context [] %)
            #{:ns-form? :form-kind}]]]
    (doseq [operations [nil
                        {}
                        (assoc (zipmap expected-keys (repeat identity))
                               :extra identity)
                        (assoc (zipmap expected-keys (repeat identity))
                               (first expected-keys) :not-a-function)]]
      (is (thrown? clojure.lang.ExceptionInfo (invoke operations))
          (str label " " (pr-str operations))))))

(deftest reader-namespace-api-dependencies-and-nonclaims-are-explicit
  (is (nil? (find-ns 'gravity.bootstrap))
      "the direct leaf test must not load the bootstrap monolith")
  (let [contract-var
        (get (ns-interns 'gravity.reader-namespace) 'namespace-contract)
        contract (var-get contract-var)]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.reader-namespace (:namespace contract)))
    (is (= :stage0-reader-namespace-shape-and-context
           (:contract-boundary contract)))
    (is (= #{'allowed-ns-clauses 'ns-form? 'fail-ns-shape!
             'validate-ns-syntax! 'reader-module-context}
           (set (keys (:public-api contract)))))
    (is (= #{'allowed-ns-clauses 'ns-form? 'fail-ns-shape!
             'validate-ns-syntax! 'reader-module-context}
           (set (keys (ns-publics 'gravity.reader-namespace)))))
    (is (= #{'diagnostics 'reader-primitives 'source-span}
           (set (keys (ns-aliases 'gravity.reader-namespace)))))
    (is (= ['gravity.diagnostics 'gravity.reader-primitives 'gravity.source-span]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap]
           (get-in contract [:dependency-direction :forbids])))
    (doseq [nonclaim [:canonical-c2-reader-authority
                      :l3-namespace-semantic-analysis
                      :l3-namespace-policy
                      :bootstrap-orchestration
                      :self-hosted-authority]]
      (is (some #{nonclaim} (get-in contract [:ownership :does-not-own]))))
    (is (true? (:bootstrap-hosted? contract)))
    (is (false? (:canonical-c2-reader? contract)))
    (is (false? (:l3-semantic-analyzer? contract)))
    (is (false? (:self-hosted? contract)))
    (is (= '([form])
           (:arglists (meta #'reader-namespace/ns-form?))))
    (is (= '([source-path clause remediation]
             [source-path clause remediation operations])
           (:arglists (meta #'reader-namespace/fail-ns-shape!))))
    (is (= '([source-path forms] [source-path forms operations])
           (:arglists (meta #'reader-namespace/validate-ns-syntax!))))
    (is (= '([forms] [forms operations])
           (:arglists (meta #'reader-namespace/reader-module-context))))))
