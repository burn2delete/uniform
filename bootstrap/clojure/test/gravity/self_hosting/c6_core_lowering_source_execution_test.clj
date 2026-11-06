(ns gravity.self-hosting.c6-core-lowering-source-execution-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

;; This is a development execution check for the current Gravity C6 source.
;; It does not renew the historical SH-07 source census or migration pins.
(def ^:private source-relative-path
  "language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity")

(def ^:private root
  (delay
    (let [resource
          (io/resource
           "gravity/self_hosting/c6_core_lowering_source_execution_test.clj")]
      (when-not resource
        (throw
         (ex-info
          "C6 source execution test is not on the classpath"
          {:id "C6-SOURCE-EXECUTION-TEST-SOURCE"})))
      (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
        (cond
          (nil? candidate)
          (throw
           (ex-info
            "Repository root could not be located"
            {:id "C6-SOURCE-EXECUTION-REPOSITORY-ROOT"}))

          (.isFile (.toFile (.resolve candidate "deps.edn")))
          candidate

          :else
          (recur (.getParent candidate)))))))

(defn- path
  []
  (str (.resolve @root source-relative-path)))

(def ^:private plan
  (delay
    (let [source-path (path)
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path (slurp source-path :encoding "UTF-8")))))

(defn- invoke
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-c6-core-lowering-source-execution
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- sample-node
  []
  (invoke
   'build-c6-core-node
   ["node-1"
    :if
    {:condition "condition" :then "then" :else "else"}
    "syntax-1"
    {:line 1 :column 1}
    []
    :scope-1
    :meta
    :jvm
    {:source :fixture}
    {:resolved-bindings :fixture}
    false]))

(deftest current-c6-source-builders-preserve-runtime-inputs
  (let [node (sample-node)
        trace
        (invoke
         'build-c6-desugaring-trace
         ["syntax-1"
          :when
          "node-1"
          [:if :do :quote]
          [:source-spans :metadata :profile]
          [{:core-node "node-1"
            :reason :surface-desugar
            :from "syntax-1"}]
          [:condition :body]])]
    (testing "the internal source builder evaluates its arguments"
      (is (= :gravity/c6-core-node (:artifact node)))
      (is (= "node-1" (:node-id node)))
      (is (= :if (:form node)))
      (is (= {:condition "condition" :then "then" :else "else"}
             (:children node)))
      (is (= {:syntax-id "syntax-1"
              :span {:line 1 :column 1}
              :origin-chain []}
             (:source node)))
      (is (= :scope-1 (:binding-context node)))
      (is (= :meta (:profile node)))
      (is (= :jvm (:target node)))
      (is (= {:source :fixture} (:metadata node)))
      (is (= {:resolved-bindings :fixture} (:facts node)))
      (is (false? (:generated? node))))
    (testing "the internal source trace builder evaluates its arguments"
      (is (= :gravity/c6-desugaring-trace (:artifact trace)))
      (is (= "syntax-1" (:surface-syntax trace)))
      (is (= :when (:surface-kind trace)))
      (is (= "node-1" (:core-root trace)))
      (is (= [:if :do :quote] (:introduced-forms trace)))
      (is (= [:source-spans :metadata :profile] (:preserved trace)))
      (is (= [{:core-node "node-1"
               :reason :surface-desugar
               :from "syntax-1"}]
             (:introduced-origin trace)))
      (is (= [:condition :body] (:evaluation-order trace)))
      (is (= [] (:diagnostics trace))))))

(deftest current-c6-source-builders-reject-wrong-arity
  (is (thrown? clojure.lang.ExceptionInfo
               (invoke 'build-c6-core-node ["node-1"]))))
