(ns gravity.core-ast-lowering-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [gravity.core-ast-lowering :as l2]))

(def module
  {:module 'demo.core
   :source-path "demo.gravity"
   :profile :hosted
   :target :jvm
   :effects #{:io/write :error/throw}
   :capabilities #{:io/stdout}
   :safety :safe
   :metadata {}})

(defn syntax
  ([form] (syntax 0 form))
  ([index form]
   {:syntax-id (str "syntax-" index)
    :form form
    :span {:source "demo.gravity" :form-index index}
    :profile :hosted
    :namespace 'demo.core
    :generated-origin [{:kind :source :from (str "source-" index)}]
    :metadata {:fixture true}}))

(defn failure-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (:id (ex-data ex)))))

(defn failure-data
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo ex
      (ex-data ex))))

(defn list-form
  [operator & args]
  (apply list operator args))

(deftest contract-public-surface-dependency-and-nonauthority
  (let [contract-var (get (ns-interns 'gravity.core-ast-lowering)
                          'namespace-contract)
        contract (var-get contract-var)
        api (:public-api (l2/core-ast-lowering-engine-contract))
        publics (set (keys (ns-publics 'gravity.core-ast-lowering)))]
    (is (true? (:private (meta contract-var))))
    (is (= 'gravity.core-ast-lowering (:namespace contract)))
    (is (true? (:compatibility-only? contract)))
    (is (true? (:bootstrap-hosted? contract)))
    (is (false? (:canonical-sh07-authority? contract)))
    (is (false? (:canonical-c6-authority? contract)))
    (is (false? (:source-reading? contract)))
    (is (false? (:target-lowering? contract)))
    (is (= ['clojure.core 'clojure.string]
           (get-in contract [:dependency-direction :requires])))
    (is (= ['gravity.bootstrap 'gravity.diagnostics]
           (get-in contract [:dependency-direction :forbids])))
    (is (= publics (set (keys api))))
    (doseq [name '[fail! macro-source-artifact uses-println?]
            :let [dependency-var
                  (get (ns-interns 'gravity.core-ast-lowering) name)]]
      (is (some? dependency-var))
      (is (true? (:private (meta dependency-var))))
      (is (not (contains? api name)))
      (is (not (contains? publics name))))
    (doseq [[name spec] api
            :when (:arglists spec)]
      (is (= (:arglists spec)
             (:arglists (meta (get (ns-publics 'gravity.core-ast-lowering)
                                   name))))))
    (is (not (contains? (set (keys (ns-aliases 'gravity.core-ast-lowering)))
                        'gravity.bootstrap)))
    (is (not (contains? (set (keys (ns-aliases 'gravity.core-ast-lowering)))
                        'gravity.diagnostics))))

(deftest constants-and-operation-map-are-strict
  (is (= '#{quote if do let fn loop recur def var set! try throw match}
         l2/core-forms))
  (is (contains? l2/lowering-gap-forms 'defn))
  (is (= '#{core-unknown} l2/unknown-reserved-core-forms))
  (doseq [operations [nil []
                      {:unknown identity}
                      {:fail! :keyword-is-not-a-function}
                      {:core-forms [:if]}
                      {:core-forms #{}}
                      {:lowering-gap-forms #{:when}}
                      {:unknown-reserved-core-forms #{}}]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (l2/with-operations operations (fn [] :unused)))
        (str "rejected operation map " operations)))
  (is (thrown? clojure.lang.ExceptionInfo
               (l2/with-operations {} :not-a-function)))
  (is (= :ok
         (l2/with-operations {:core-forms '#{quote}
                              :lowering-gap-forms '#{defn}
                              :unknown-reserved-core-forms '#{core-unknown}}
           (fn [] :ok))))
  (is (= #{:io/write}
         (l2/with-operations {:uses-println? (constantly true)}
           #(l2/form-effect '(not-println 1))))))

(deftest nested-partial-operation-bindings-merge
  (let [seen (atom [])
        fail-op (fn [id _ data]
                  (swap! seen conj [id data])
                  ::failed)]
    (is (= ::failed
           (l2/with-operations
             {:core-forms '#{outer-form}
              :fail! fail-op}
             (fn []
               (l2/with-operations
                 {:lowering-gap-forms '#{inner-gap}}
                 (fn []
                    (l2/assert-core-operator!
                    module (syntax '(inner-gap))
                    (list-form 'inner-gap))))))))
    (is (= "L2-LOWERING-GAP" (ffirst @seen)))
    ;; A nested partial override keeps the outer scalar and function
    ;; bindings.  The outer reserved-form set therefore still drives the
    ;; assertion after the inner lowering-gap-only override.
    (is (= ::failed
           (l2/with-operations
             {:unknown-reserved-core-forms '#{outer-reserved}
              :fail! fail-op}
             (fn []
               (l2/with-operations
                 {:lowering-gap-forms '#{inner-gap}}
                 (fn []
                    (l2/assert-core-operator!
                    module (syntax '(outer-reserved))
                    (list-form 'outer-reserved))))))))
    (is (= ["L2-LOWERING-GAP" "L2-UNKNOWN-CORE-FORM"]
           (mapv first @seen)))))

(deftest captured-original-one-shot-and-recursive-interposition
  (let [calls (atom [])
        captured l2/lower-core-expr
        form (list-form 'do 1 2)
        result
        (l2/with-operations
          {:lower-core-expr
           (fn [& args]
             (swap! calls conj (nth args 3))
             (l2/call-entrypoint-body :lower-core-expr captured args))}
          (fn []
            (captured (atom 0) module (syntax form) form {})))]
    (is (= :do (:kind result)))
    (is (= [form 1 2] @calls)))
  (let [calls (atom [])
        captured l2/flatten-core
        node {:node-id "root"
              :children [{:node-id "child" :kind :literal :children []}]}
        result
        (l2/with-operations
          {:flatten-core
           (fn [value]
             (swap! calls conj (:node-id value))
             (l2/call-entrypoint-body :flatten-core captured [value]))}
          (fn [] (captured node)))]
    (is (= ["root" "child"] @calls))
    (is (= ["root" "child"] (mapv :node-id result))))
  (doseq [[key operation args]
          [[:unknown identity []]
           [:lower-core-expr :not-a-function []]
           [:lower-core-expr identity :not-sequential]]]
    (is (thrown? clojure.lang.ExceptionInfo
                 (l2/call-entrypoint-body key operation args)))))

(deftest effects-and-node-metadata-preserve-legacy-shape
  (is (= #{:io/write} (l2/form-effect (list-form 'do (list-form 'println 1)))))
  (is (= #{:error/throw} (l2/form-effect (list-form 'throw 'error))))
  (is (= #{:state/write} (l2/form-effect (list-form 'set! 'mutable-x 1))))
  (is (= #{:io/write :state/write}
         (l2/combine-effects #{:io/write} #{:state/write})))
  (let [node (l2/core-node "7" :literal (syntax 3 42) 42 {:value 42})]
    (is (= "stage0-core-7" (:node-id node)))
    (is (= :literal (:kind node)))
    (is (= 42 (:value node)))
    (is (= (:span (syntax 3 42)) (:source-span node)))
    (is (= (:generated-origin (syntax 3 42)) (:generated-origin node)))
    (is (= :hosted (:profile node)))
    (is (= 'demo.core (:namespace node)))
    (is (= #{} (:capabilities node)))))

(deftest default-failure-carrier-retains-stage0-diagnostic-shape
  (let [form '(recur value)
        data (failure-data
              #(l2/lower-core-expr (atom 0) module (syntax form) form {}))]
    (is (= {:id "L2-RECUR-TARGET"
            :message "recur has no compatible loop or function target"
            :bootstrap-stage :stage0}
           (select-keys data [:id :message :bootstrap-stage])))))

(deftest all-l2-forms-and-literal-symbol-call-kinds
  (let [forms [[(list-form 'quote 'data) :quote]
               [(list-form 'if 'test 'then 'else) :if]
               [(list-form 'do 'first 'second) :do]
               [(list-form 'let ['x 1] 'x) :let]
               [(list-form 'fn ['x] 'x) :fn]
               [(list-form 'loop ['x 1] (list-form 'recur 'x)) :loop]
               [(list-form 'recur 'x) :recur]
               [(list-form 'def 'answer 42) :def]
               [(list-form 'var 'answer) :var]
               [(list-form 'set! 'mutable-answer 43) :set!]
               [(list-form 'try 'body (list-form 'catch 'Error 'e 'e)) :try]
               [(list-form 'throw 'error) :throw]
               [(list-form 'match 'value 'pattern 'result) :match]]]
    (doseq [[[form expected] index] (map vector forms (range))]
      (testing (str form)
        (let [context (if (= expected :recur) {:recur-arity 1} {})
              node (l2/lower-core-expr (atom 0) module
                                       (syntax index form) form context)]
          (is (= expected (:kind node)))
          (is (contains? node :evaluation-order))))))
  (is (= :symbol
         (:kind (l2/lower-core-expr (atom 0) module (syntax 'x) 'x {}))))
  (is (= :literal
         (:kind (l2/lower-core-expr (atom 0) module (syntax 0 17) 17 {}))))
  (is (= :call
         (:kind (l2/lower-core-expr (atom 0) module
                                    (syntax 0 (list-form 'f 1))
                                    (list-form 'f 1) {}))))
  (let [node (l2/lower-core-expr (atom 0) module
                                 (syntax 0 (list-form 'defconst 'x 1))
                                 (list-form 'defconst 'x 1) {})]
    (is (= :def (:kind node)))
    (is (true? (:compile-time-binding? node)))))

(deftest negative-diagnostics-retain-l2-identities-and-id-consumption
  (doseq [[form context expected]
          [[(list-form 'recur 'x) {} "L2-RECUR-TARGET"]
           [(list-form 'set! 'answer 1) {} "L2-SET-ILLEGAL"]
           [(list-form 'throw 'error) {} "L2-THROW-ILLEGAL"]
           [(list-form 'core-unknown) {} "L2-UNKNOWN-CORE-FORM"]
           [(list-form 'defn 'answer []) {} "L2-LOWERING-GAP"]
           [(list-form 'reorder-effects) {} "L2-EVAL-ORDER"]
           [(list-form 'host-exception) {} "L2-HOST-SEMANTICS"]]]
    (testing expected
      (is (= expected
             (failure-id
              #(l2/lower-core-expr (atom 0)
                                   (assoc module :effects #{})
                                   (syntax form) form context))))))
  (let [counter (atom 0)]
    (is (= "L2-RECUR-TARGET"
           (failure-id #(l2/lower-core-expr counter
                                             (assoc module :effects #{})
                                             (syntax '(recur x))
                                             '(recur x) {}))))
    ;; The legacy allocator takes the node id before assertions.
    (is (= 1 @counter))))
  (is (= "L7-PATTERN-TYPE"
         (failure-id #(l2/lower-match-clauses
                       (atom 0) module (syntax '(match x p))
                       ['p] {})))))

(deftest flatten-and-pure-core-artifact-projection
  (let [macro-artifact
        {:module module
         :macro-expansion-trace [:trace]
         :expanded-syntax-object-stream
         [(syntax 0 (list-form 'ns 'demo.core))
          (syntax 1 (list-form 'def 'answer 42))
          (syntax 2 (list-form 'if 'answer 1 0))]}
        calls (atom [])
        artifact
        (l2/with-operations
          {:macro-source-artifact
           (fn [source-path source-text]
             (swap! calls conj [source-path source-text])
             macro-artifact)}
          (fn [] (l2/core-source-artifact "demo.gravity" "synthetic")))]
    (is (= [["demo.gravity" "synthetic"]] @calls))
    (is (= :gravity/stage0-core-artifact (:kind artifact)))
    (is (= module (:module artifact)))
    (is (= [:trace] (:macro-expansion-trace artifact)))
    (is (= 2 (count (:expanded-core-ast artifact))))
    (is (= 6 (count (:core-node-source-map artifact))))
    (is (= 6 (count (:core-form-kind-records artifact))))
    (is (= 6 (count (:evaluation-order-metadata artifact))))
    (is (= [] (:latent-function-effect-records artifact)))
    (is (= [] (:call-records artifact)))
    (is (= [] (:diagnostics artifact)))
    (is (= :def (get-in artifact [:expanded-core-ast 0 :kind])))
    (is (= :if (get-in artifact [:expanded-core-ast 1 :kind])))
    (is (= #{:io/write}
           (:effects (l2/lower-core-expr
                      (atom 0) module
                      (syntax 3 (list-form 'println 1))
                      (list-form 'println 1) {}))))))

(deftest macro-source-acquisition-is-injectable-and-no-default-read
  (let [error (try
                (l2/core-source-artifact "not-read.gravity" "ignored")
                nil
                (catch clojure.lang.ExceptionInfo ex ex))]
    (is (some? error))
    (is (= :macro-source-artifact (:operation (ex-data error))))))
