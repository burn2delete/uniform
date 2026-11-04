(ns gravity.self-hosting.c6-core-lowering-source-classifier-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-core-lowering :as c6]
            [gravity.c6-core-lowering.lowering :as lowering]
            [gravity.c6-core-lowering.source-adapter :as source-adapter]))

;; This is a current-source development execution check.  It does not renew
;; migration evidence, source pins, or a self-hosting claim.
(def ^:private repository-root
  (delay
    (let [resource
          (io/resource
           "gravity/self_hosting/c6_core_lowering_source_classifier_test.clj")]
      (when-not resource
        (throw
         (ex-info
          "C6 source classifier test is not on the classpath"
          {:id "C6-SOURCE-CLASSIFIER-TEST-SOURCE"})))
      (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
        (cond
          (nil? candidate)
          (throw
           (ex-info
            "Repository root could not be located"
            {:id "C6-SOURCE-CLASSIFIER-REPOSITORY-ROOT"}))

          (.isFile (.toFile (.resolve candidate "deps.edn")))
          candidate

          :else
          (recur (.getParent candidate)))))))

(def ^:private compiler-api
  {:c-backend-stage2-plan-emitter-source-rule!
   bootstrap/c-backend-stage2-plan-emitter-source-rule!
   :p15-s23-stage2-compiler-artifact-plan
   bootstrap/p15-s23-stage2-compiler-artifact-plan})

(def ^:private source-plan
  (delay
    (source-adapter/current-source-plan @repository-root compiler-api)))

(def ^:private source-operation
  (delay
    (source-adapter/c6-form->core-form-operation
     bootstrap/p15-s23-stage2-runtime-execute-function
     @source-plan)))

(def ^:private source-evaluation-order-operation
  (delay
    (source-adapter/c6-eval-order-operation
     bootstrap/p15-s23-stage2-runtime-execute-function
     @source-plan)))

(def ^:private module
  {:module 'gravity.c6-source-classifier-test
   :source-path "c6-source-classifier-test.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def ^:private c5-artifact
  {:kind :gravity/stage0-c5-name-resolution-artifact
   :artifact-id "sha256:c6-source-classifier-test"
   :namespace-analysis {:namespace 'gravity.c6-source-classifier-test}
   :binding-table {:bindings [{:binding-id "c6-source-classifier-binding"}]}
   :alias-table {}
   :dependency-graph {:nodes [] :edges []}})

(defn- syntax [id form]
  {:syntax-id id
   :form form
   :span {:source "c6-source-classifier-test.gravity" :form-index id}
   :origin :source
   :generated-origin []
   :metadata {}})

(deftest current-source-classifier-preserves-hosted-leaf-parity
  (let [classify @source-operation]
    (doseq [core-form '[quote if do let fn loop recur def var set! try throw match]]
      (is (= core-form (classify (list core-form :argument)))
          (str "current source core-form classification for " core-form)))
    (doseq [[form expected]
            [['answer :symbol]
             [42 :literal]
             ['(if true 1 2) 'if]
             ['(unsafe value) :declared-primitive]
             ['(unknown value) :call]]]
      (is (= expected (classify form))
          (str "current source classification for " (pr-str form))))))

(deftest current-source-evaluation-order-preserves-hosted-leaf-parity
  (let [order @source-evaluation-order-operation
        hosted-order lowering/c6-eval-order]
    (doseq [[form child-count expected]
            [[(quote quote) 9 []]
             [(quote if) 3 [:condition :then-or-else]]
             [(quote do) 0 []]
             [(quote do) -1 []]
             [(quote do) 3 [[:expr 0] [:expr 1] [:expr 2]]]
             [(quote let) 2 [:bindings-left-to-right :body-left-to-right]]
             [(quote fn) 2 [:call-arguments-left-to-right]]
             [(quote loop) 2 [:loop-bindings-left-to-right :body-left-to-right]]
             [(quote recur) 2 [:arguments-left-to-right]]
             [(quote def) 2 [:initializer]]
             [(quote var) 1 []]
             [(quote set!) 2 [:value]]
             [(quote try) 2 [:body :matching-handler]]
             [(quote throw) 1 [:value]]
             [(quote match) 2 [:scrutinee :selected-clause]]
             [:call 3 [:operator :arguments-left-to-right]]
             [:declared-primitive 3 [:arguments-left-to-right]]
             [:unknown 3 []]]]
      (let [hosted-result (hosted-order form child-count)
            source-result (order form child-count)]
        (is (= expected hosted-result)
            (str "hosted evaluation order contract for " (pr-str form)))
        (is (= hosted-result source-result)
            (str "current source evaluation order parity for " (pr-str form)))))))

(deftest current-source-evaluation-order-stage2-rejects-wrong-arity
  (let [failure
        (try
          (bootstrap/p15-s23-stage2-runtime-execute-function
           {:engine :gravity-c6-core-lowering-source-evaluation-order
            :compiler-artifact-plan? true}
           @source-plan
           source-adapter/evaluation-order-source-function
           [(quote if)])
          nil
          (catch clojure.lang.ExceptionInfo error
            (ex-data error)))]
    (is (= "L2-FUNCTION-ARITY" (:id failure)))
    (is (= 2 (:expected-arity failure)))
    (is (= 1 (:actual-arity failure)))))

(deftest nested-do-caller-preserves-source-owned-evaluation-order
  (let [form '(do (do) (do 1 2) (if true (do 3) (do 4 5 6)))
        classification-calls (atom [])
        evaluation-calls (atom [])
        lowered
        (with-redefs [lowering/c6-form->core-form
                      (fn [_]
                        (throw (ex-info "hosted classifier was called"
                                        {:id "C6-SOURCE-CLASSIFIER-HOST-FALLBACK"})))
                      lowering/c6-eval-order
                      (fn [& _]
                        (throw (ex-info "hosted evaluation order was called"
                                        {:id "C6-SOURCE-EVAL-ORDER-HOST-FALLBACK"})))]
          (c6/with-operations
            {:c6-form->core-form
             (fn [value]
               (swap! classification-calls conj value)
               (@source-operation value))
             :c6-eval-order
             (fn [core-form child-count]
               (swap! evaluation-calls conj [core-form child-count])
               (@source-evaluation-order-operation core-form child-count))}
            #(c6/c6-lowering-artifact
              (:source-path module) module c5-artifact
              [(syntax "nested-do" form)])))
        nodes (:core-node-table lowered)
        do-nodes (filterv #(= 'do (:form %)) nodes)]
    (is (= form (:surface-form (first nodes))))
    (is (= [form '(do) '(do 1 2)
            '(if true (do 3) (do 4 5 6)) '(do 3) '(do 4 5 6)]
           (filterv seq? @classification-calls)))
    (is (= [['do 0] ['do 2] ['do 1] ['do 3] ['if 3] ['do 3]]
           @evaluation-calls))
    (is (= [[[:expr 0] [:expr 1] [:expr 2]] []
            [[:expr 0] [:expr 1]] [[:expr 0]]
            [[:expr 0] [:expr 1] [:expr 2]]]
           (mapv :evaluation-order do-nodes)))
    (is (= [1 2 true 3 4 5 6]
           (mapv :value (filter #(= :literal (:form %)) nodes))))))

(deftest current-source-leaves-are-used-by-c6-lowering-caller
  (let [classification-calls (atom [])
        evaluation-calls (atom [])
        classify (fn [form]
                   (swap! classification-calls conj form)
                   (@source-operation form))
        evaluate (fn [form child-count]
                   (swap! evaluation-calls conj [form child-count])
                   (@source-evaluation-order-operation form child-count))
        lowered
        (with-redefs [lowering/c6-form->core-form
                      (fn [_]
                        (throw
                         (ex-info
                          "hosted Clojure classifier was called"
                          {:id "C6-SOURCE-CLASSIFIER-HOST-FALLBACK"})))
                      lowering/c6-eval-order
                      (fn [& _]
                        (throw
                         (ex-info
                          "hosted Clojure evaluation-order operation was called"
                          {:id "C6-SOURCE-EVAL-ORDER-HOST-FALLBACK"})))]
          (c6/with-operations
            {:c6-form->core-form classify
             :c6-eval-order evaluate}
            #(c6/c6-lowering-artifact
              (:source-path module)
              module
              c5-artifact
              [(syntax "if" '(if true 1 2))])))
        root (first (:core-node-table lowered))]
    (testing "the existing C6 caller consumes the source-owned operation"
      (is (= 'if (:form root)))
      (is (= 3 (count (:children root))))
      (is (= [:condition :then-or-else] (:evaluation-order root)))
      (is (= 4 (count @classification-calls)))
      (is (= [[(quote if) 3]] @evaluation-calls)))
    (testing "the source operation retains the real lowering-gap rejection"
      (let [failure
            (try
              (with-redefs [lowering/c6-form->core-form
                            (fn [_]
                              (throw
                               (ex-info
                                "hosted Clojure classifier was called"
                                {:id "C6-SOURCE-CLASSIFIER-HOST-FALLBACK"})))
                            lowering/c6-eval-order
                            (fn [& _]
                              (throw
                               (ex-info
                                "hosted Clojure evaluation-order operation was called"
                                {:id "C6-SOURCE-EVAL-ORDER-HOST-FALLBACK"})))]
                (c6/with-operations
                  {:c6-form->core-form @source-operation
                   :c6-eval-order @source-evaluation-order-operation}
                  #(c6/c6-lowering-artifact
                    (:source-path module)
                    module
                    c5-artifact
                    [(syntax "defn" '(defn answer [] 1))])))
              nil
              (catch clojure.lang.ExceptionInfo error
                (ex-data error)))]
        (is (= "C6-LOWERING-GAP" (:id failure)))
        (is (= 'defn (:lowering-rule failure)))))))
