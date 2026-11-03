(ns gravity.self-hosting.canonical-core-node-source-execution-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.c6-core-lowering :as c6]
            [gravity.c6-core-lowering.lowering :as lowering]
            [gravity.bootstrap :as bootstrap]))

;; This is a development execution check for the current Uniform C6 source.
;; It does not establish canonical admission, verifier authority, or production
;; lowering adoption.
(def ^:private source-relative-path
  "language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity")

(def ^:private root
  (delay
    (let [resource
          (io/resource
           "gravity/self_hosting/canonical_core_node_source_execution_test.clj")]
      (when-not resource
        (throw
         (ex-info
          "Canonical core-node source execution test is not on the classpath"
          {:id "CORE-NODE-SOURCE-EXECUTION-TEST-SOURCE"})))
      (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
        (cond
          (nil? candidate)
          (throw
           (ex-info
            "Repository root could not be located"
            {:id "CORE-NODE-SOURCE-EXECUTION-REPOSITORY-ROOT"}))

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

(defn- invoke-source
  [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :canonical-core-node-source-execution
    :compiler-artifact-plan? true}
   @plan function arguments))

(def ^:private syntax
  {:syntax-id "syntax-1"
   :form '(if condition then else)
   :span {:source "sample.gravity" :form-index 0}
   :origin :source
   :generated-origin [{:kind :macro-expansion
                       :from-syntax-id "macro-1"}]
   :metadata {:source :fixture}})

(def ^:private module
  {:profile :native
   :target :jvm
   :capabilities #{:compile/core}})

(def ^:private data
  {:surface-form '(unsafe (if condition then else))
   :children {:condition "condition" :then "then" :else "else"}
   :evaluation-order [:condition :then-or-else]
   :lowering-rule :if
   :custom-field :preserved})

(def ^:private effects
  #{:error/throw})

(def ^:private artifact-module
  {:module 'gravity.canonical-test
   :source-path "sample.gravity"
   :profile :hosted
   :target :jvm
   :effects #{}
   :capabilities #{}
   :safety :safe
   :metadata {}})

(def ^:private c5-artifact
  {:kind :gravity/stage0-c5-name-resolution-artifact
   :artifact-id "sha256:c5"
   :namespace-analysis {:namespace 'gravity.canonical-test}
   :binding-table {:bindings [{:binding-id "binding-0"}]}
   :alias-table {}
   :dependency-graph {:nodes [] :edges []}})

(defn- artifact-syntax
  [id form]
  {:syntax-id id
   :form form
   :span {:source "sample.gravity" :form-index id}
   :origin :source
   :generated-origin []
   :metadata {}})

(defn- expected-host-node
  []
  (lowering/c6-core-node "node-1" :if syntax module
                         (assoc data :effects effects)))

(deftest source-canonical-node-matches-hosted-construction
  (let [source-node
        (invoke-source
         'construct-canonical-core-node
         ["node-1" :if syntax module data effects])
        host-node (expected-host-node)]
    (testing "Stage2 executes the Uniform-authored canonical constructor"
      (is (= :gravity/core-node (:artifact source-node)))
      (is (= host-node source-node))
      (is (= {:syntax-id "syntax-1"
              :span {:source "sample.gravity" :form-index 0}
              :origin-chain [{:kind :source}
                             {:kind :macro-expansion
                              :from-syntax-id "macro-1"}]}
             (:source source-node)))
      (is (= {:source :fixture} (:metadata source-node)))
      (is (= #{:compile/core} (:capabilities source-node)))
      (is (= {:unsafe-island :declared
              :safety-outcome :pending-safe6}
             (:unsafe-metadata source-node)))
      (is (true? (:generated? source-node))))))

(deftest source-canonical-node-preserves-sequential-generated-origin
  (let [list-origin-syntax
        (assoc syntax
               :generated-origin
               (list {:kind :macro-expansion
                      :from-syntax-id "macro-list-1"}
                     {:kind :macro-expansion
                      :from-syntax-id "macro-list-2"}))
        source-node
        (invoke-source
         'construct-canonical-core-node
         ["node-list-origin" :if list-origin-syntax module data effects])
        host-node
        (lowering/c6-core-node "node-list-origin" :if list-origin-syntax module
                               (assoc data :effects effects))]
    (testing "Stage2 retains list/seq generated origins in hosted order"
      (is (= host-node source-node))
      (is (= [{:kind :source}
              {:kind :macro-expansion
               :from-syntax-id "macro-list-1"}
              {:kind :macro-expansion
               :from-syntax-id "macro-list-2"}]
             (get-in source-node [:source :origin-chain])))
      (is (true? (:generated? source-node))))))

(deftest source-canonical-node-preserves-data-merge-and-arity-refusal
  (let [overridden
        (invoke-source
         'construct-canonical-core-node
         ["node-2" :if syntax module
          (assoc data
                 :artifact :gravity/custom-node
                 :profile :hosted
                 :effects #{:io/read})
          effects])]
    (is (= :gravity/custom-node (:artifact overridden)))
    (is (= :hosted (:profile overridden)))
    (is (= #{:io/read} (:effects overridden)))
    (is (= :preserved (:custom-field overridden)))
    (is (thrown? clojure.lang.ExceptionInfo
                 (invoke-source 'construct-canonical-core-node
                                ["node-2"])))))

(deftest source-node-can-feed-existing-c6-validation-boundary
  (let [source-backed-node
        (fn [node-id form node-syntax node-module node-data]
          (invoke-source
           'construct-canonical-core-node
           [node-id form node-syntax node-module node-data
            (get node-module :effects)]))
        stream [(artifact-syntax "ns" '(ns gravity.canonical-test))
                (artifact-syntax "value" '(def answer 42))]
        artifact
        (c6/c6-lowering-artifact
         "sample.gravity" artifact-module c5-artifact stream)
        source-backed-artifact
        (c6/with-operations
          {:c6-core-node source-backed-node}
          #(c6/c6-lowering-artifact
            "sample.gravity" artifact-module c5-artifact stream))
        malformed (assoc-in source-backed-artifact
                             [:surface-to-core-map :status]
                             :forged)
        rejection (try
                    (c6/c6-lowering-validate! "sample.gravity" malformed)
                    nil
                    (catch clojure.lang.ExceptionInfo error
                      (ex-data error)))]
    (testing "the complete existing artifact can consume source-created nodes"
      (is (= :complete (get-in source-backed-artifact
                                [:c6-lowering-results :status])))
      (is (= :passed (get-in source-backed-artifact
                              [:core-verifier-report :status])))
      (is (= (get-in artifact [:core-node-table])
             (get-in source-backed-artifact [:core-node-table])))
      (is (= (get-in source-backed-artifact
                     [:core-node-table 0 :artifact])
             :gravity/core-node)))
    (testing "the existing C6 validation boundary rejects malformed output"
      (is (= "C6-CORE-SHAPE" (:id rejection)))
      (is (= :source-to-core-map-present?
             (get-in rejection [:missing-fields 0]))))))
