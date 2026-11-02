(ns gravity.self-hosting.sh11-checked-core-source-limits-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

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

(deftest source-no-longer-carries-size-or-depth-ceilings
  (let [source (slurp (source-path))]
    (is (re-find #":maximum-functions 1" source))
    (doseq [retired [":maximum-forms"
                     ":maximum-plan-nodes"
                     ":maximum-digest-requests"
                     ":maximum-graph-depth"
                     ":maximum-container-width"
                     ":maximum-scalar-bytes"
                     ":maximum-integer-bits"]]
      (is (not (re-find (re-pattern retired) source)) retired))
    (is (not (re-find #"> depth 48" source)))
    (is (not (re-find #"<= \\(count forms\\) 128" source)))))

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

(deftest front-end-shape-does-not-impose-form-count-ceiling
  (let [form-count 129
        form-ids (mapv #(str "sha256:form-" %) (range form-count))
        front-end
        {:artifact :gravity/p15-s23-stage2-c2-c3-front-end-products
         :status :complete
         :source-unit-record
         {:artifact :gravity/stage0-source-unit
          :encoding :utf-8
          :reader-options {}
          :bytes-hash "sha256:source"
          :extension-policy {}
          :enabled-features #{}}
         :source-unit-id "sha256:source-unit"
         :token-stream []
         :form-tree (mapv #(assoc base-form :form-id %) form-ids)
         :top-level-form-ids form-ids
         :syntax-seed-stream []
         :reader-source-map []
         :literal-decoding-records []
         :semantic-error-deferment-record {}
         :reader-extension-invocation-records []
         :reader-diagnostics []
         :incremental-reader-hashes {}
         :reader-product-integrity {}
         :c3-artifact-id "sha256:c3"
         :c3-syntax-object-stream (mapv (fn [form-id] {:form-id form-id}) form-ids)
         :c3-capability-proof {}
         :records []
         :forms (mapv #(assoc base-form :form-id %) form-ids)}]
    (is (true?
         (invoke 'c6c10-private-front-end-shape? [front-end])))
    (is (false?
         (invoke 'c6c10-private-front-end-shape?
                 [(dissoc front-end :forms)])))))
