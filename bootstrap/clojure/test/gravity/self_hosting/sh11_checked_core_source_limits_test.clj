(ns gravity.self-hosting.sh11-checked-core-source-limits-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-branch-join-mir-test]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh11_checked_core_source_limits_test.clj")]
    (when-not resource
      (throw
       (ex-info "Checked-Core source-limit test is not on the classpath"
                {:id "SH11-C6-C10-SOURCE-LIMIT-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH11-C6-C10-SOURCE-LIMIT-TEST-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")

(defn- source-path []
  (str (.resolve @root source-relative-path)))

(def ^:private source-plan
  (delay
    (let [path (source-path)
          source (slurp path)
          emitter (:emitter
                   (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                    path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter path source))))

(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :sh11-checked-core-source-limit-test
    :compiler-artifact-plan? true}
   @source-plan function (vec arguments)))

(def ^:private base-form
  {:form-id "sha256:form"
   :kind :nil
   :value nil
   :metadata []
   :tag nil
   :abbrev nil
   :generated-origin []
   :span {}
   :surface-span {}})

(def ^:private base-envelope
  {:source-content-hash "sha256:source"
   :entrypoint-syntax-id "sha256:syntax"
   :form-tree [base-form]})

(def ^:private empty-state
  {:next-ordinal 0 :requests []})

(defn- sh12-var [name]
  (var-get
   (ns-resolve
    'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn- current-emitter-binding []
  @(var-get
    (ns-resolve
     'gravity.self-hosting.sh12-zero-arity-bool-diamond-test
     'current-emitter)))

(defn- current-normalized-input []
  (let [fixture-relative
        "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/zero_arity_bool_true_source.gravity"
        path (str (.resolve @root fixture-relative))
        text (slurp path)
        reader (str (.resolve @root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))]
    (binding [bootstrap/*p15-s23-stage2-compiler-artifact-binding*
              (current-emitter-binding)]
      ((sh12-var 'parameter-source-products) path text nil reader))))

(defn- expanded-form-input [input]
  (let [front-end (:front-end-products input)
        form-tree (:form-tree front-end)
        forms (:forms front-end)
        function-form-id (second (:top-level-form-ids front-end))
        extra
        (fn [prefix index]
          {:form-id (str "sha256:checked-core-extra-" prefix "-" index)
           :kind :nil
           :value nil
           :metadata []
           :tag nil
           :abbrev nil
           :generated-origin []
           :parent-form-id function-form-id
           :span {}
           :surface-span {}})
        form-tree-extras
        (mapv #(extra "tree" %) (range (- 129 (count form-tree))))
        form-extras
        (mapv #(extra "forms" %) (range (- 129 (count forms))))]
    (as-> input expanded
      (update expanded :front-end-products
              (fn [products]
                (-> products
                    (update :form-tree into form-tree-extras)
                    (update :forms into form-extras))))
      (assoc-in expanded [:authoritative-module :forms]
                (vec (rest (get-in expanded [:front-end-products :forms])))))))

(deftest generated-nil-and-instruction-mapping-preserve-deep-source-order
  (testing "generated nil remains an ordinary provenance-bearing node"
    (doseq [[function arguments]
            [['c6c10-implicit-nil-product
              [empty-state base-envelope base-form [:body] 49 :implicit-do-nil]]
             ['c6c10-generated-nil-from-origin
              [empty-state base-envelope base-form [:body] 49
               :implicit-do-nil "sha256:input-origin"]]]]
      (let [result (invoke function arguments)]
        (is (= :accepted (:status result)) [function result])
        (is (= 49 (:maximum-depth result)) [function result])
        (is (= 49 (get-in result [:nodes 0 :plan-depth])) [function result])
        (is (= :implicit-nil
               (get-in result [:nodes 0 :source-operation]))
            [function result]))))
  (testing "a mapped literal remains accepted beyond the old depth ceiling"
    (let [result
          (invoke 'c6c10-map-instruction
                  [empty-state base-envelope {:op :literal :value nil}
                   "sha256:form" [:body] {} 49])]
      (is (= :accepted (:status result)) result)
      (is (= :literal (get-in result [:nodes 0 :source-operation])) result)
      (is (= 49 (get-in result [:nodes 0 :plan-depth])) result)))
  (testing "source correspondence remains a semantic rejection"
    (let [result
          (invoke 'c6c10-map-instruction
                  [empty-state base-envelope {:op :literal :value false}
                   "sha256:form" [:body] {} 49])]
      (is (= :rejected (:status result)) result)
      (is (= "C6-ORIGIN" (get-in result [:diagnostics 0 :rule])) result)
      (is (= :lockstep-plan-c2-form-mapping
             (get-in result [:diagnostics 0 :facts :missing-fact]))
          result))))

(deftest checked-core-builder-accepts-normalized-129-form-input
  (let [input (expanded-form-input (:envelope (current-normalized-input)))
        result (invoke 'c6-c10-build-checked-core-template [input])
        malformed
        (invoke 'c6-c10-build-checked-core-template
                [(update input :front-end-products dissoc :forms)])
        stale-source
        (invoke 'c6-c10-build-checked-core-template
                [(assoc input :source-content-hash "sha256:stale")])]
    (is (= 129 (count (get-in input [:front-end-products :form-tree]))) input)
    (is (= 129 (count (get-in input [:front-end-products :forms]))) input)
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (is (= :rejected (:status malformed)) malformed)
    (is (= :rejected (:status stale-source)) stale-source)
    (is (= "C6-ORIGIN" (get-in stale-source [:diagnostics 0 :rule]))
        stale-source)))
