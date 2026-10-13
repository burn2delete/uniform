(ns gravity.self-hosting.sh07-contract-shard-runner-test
  (:require [clojure.test :as test :refer [deftest is]]
            [gravity.self-hosting.sh07-contract-shard-runner :as runner]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private b11-route-id
  "sh07-b11-contract")

(def ^:private legacy-selection
  {:kind :exact-vars
   :namespace "gravity.self-hosting.sh07-vector-pattern-test"
   :selectors ["sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope"
               "sh07-b11-gravity-contract-names-l7"]})

(def ^:private legacy-test-vars
  '[sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope
    sh07-b11-gravity-contract-names-l7])

(def ^:private intended-closure-paths
  #{"bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"
    "bootstrap/gravity/src/gravity/checked_core.gravity"
    "bootstrap/clojure/test/gravity/self_hosting/sh07_vector_pattern_test.clj"
    "bootstrap/clojure/fixtures/self-hosting/sh-07-b11"
    "contracts/stage0-incremental-test-dependencies.edn"
    "deps.edn"
    "bootstrap/clojure/src/gravity/digest.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh07_conformance_table.clj"
    "bootstrap/clojure/test/gravity/self_hosting/unified_execution_plan.clj"
    "contracts/verification-catalog.edn"})

(defn- exception-details
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      {:message (.getMessage error)
       :data (ex-data error)})))

(deftest manifest-selection-matches-the-reviewed-contract-route
  (let [route (execution-plan/route b11-route-id)
        selection (:selection route)
        closure-paths (set (get-in route [:dependency-closure :paths]))
        seen (atom nil)
        result
        (with-redefs [test/test-vars
                      (fn [resolved]
                        (reset! seen resolved)
                        nil)]
          (runner/run-contract-shard))]
    (is (= legacy-selection selection))
    (is (= intended-closure-paths closure-paths))
    (is (= (symbol (:namespace legacy-selection))
           (-> @seen first meta :ns ns-name)))
    (is (= legacy-test-vars (:test-vars result)))
    (is (= (mapv #(-> % meta :name) @seen)
           legacy-test-vars))
    (is (= {:test 0
            :pass 0
            :fail 0
            :error 0
            :shard :sh07-contract
            :test-vars legacy-test-vars}
           result))))

(deftest altered-manifest-selector-retains-absent-test-diagnostic
  (let [altered (assoc legacy-selection :selectors
                       (assoc (:selectors legacy-selection) 1
                              "sh07-b11-selector-does-not-exist"))
        altered-catalog (assoc-in execution-plan/catalog
                                  [:routes b11-route-id :selection]
                                  altered)
        details
        (with-redefs [execution-plan/catalog altered-catalog]
          (exception-details runner/run-contract-shard))]
    (is (= "SH-07 contract shard references an absent test"
           (:message details)))
    (is (= {:id "SH07-CONTRACT-SHARD-TEST"
            :namespace (symbol (:namespace legacy-selection))
            :test 'sh07-b11-selector-does-not-exist}
           (:data details)))))

(deftest altered-manifest-selection-fails-closed
  (let [route (execution-plan/route b11-route-id)
        altered (assoc-in route [:selection :kind]
                          :proof-contract-module-catalog)
        details
        (with-redefs [execution-plan/route (constantly altered)]
          (exception-details runner/run-contract-shard))]
    (is (= "SH-07 contract route must select exact test vars"
           (:message details)))
    (is (= {:id "SH07-CONTRACT-SHARD-SELECTION"
            :route b11-route-id
            :kind :proof-contract-module-catalog}
           (:data details)))))

(deftest legacy-usage-surface-stays-argument-free
  (let [details (exception-details #(runner/-main "--check"))]
    (is (= "SH-07 contract shard accepts no arguments"
           (:message details)))
    (is (= {:id "SH07-CONTRACT-SHARD-USAGE"
            :arguments ["--check"]}
           (:data details)))))
