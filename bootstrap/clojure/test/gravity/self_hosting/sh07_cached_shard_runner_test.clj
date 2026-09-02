(ns gravity.self-hosting.sh07-cached-shard-runner-test
  (:require [clojure.test :as test :refer [deftest is]]
            [gravity.self-hosting.sh07-cached-shard-runner :as runner]
            [gravity.self-hosting.unified-execution-plan :as execution-plan]))

(def ^:private legacy-namespace
  "gravity.self-hosting.sh07-module-fragment-test")

(def ^:private legacy-shards
  (sorted-map
   "accepted"
   ["sh07-b13-direct-and-public-routing-use-v14"
    "sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive"
    "sh07-b13-fragment-coverage-is-exact-ordered-and-complete"
    "sh07-b13-module-assembly-manifest-is-exact-and-bound"
    "sh07-b13-cross-fragment-definitions-and-references-bind"
    "sh07-b13-b12-alias-value-and-call-survive-fragment-assembly"]
   "contract"
   ["sh07-b13-fixtures-are-dynamically-discovered-paired-and-bounded"
    "sh07-b13-claim-boundary-remains-honest"]
   "identity"
   ["sh07-b13-identities-are-deterministic-path-neutral-and-provenanced"
    "sh07-b13-stale-sh06-and-alias-binding-resolution-fail-closed"]
   "rejected"
   ["sh07-b13-rejected-fixtures-follow-declared-oracles"
    "sh07-b13-rejected-corpus-covers-size-and-form-boundaries"]
   "verification"
   ["sh07-b13-fragment-request-alterations-fail-closed"
    "sh07-b13-gravity-validates-fragment-plan-and-module-anchors"
    "sh07-b13-module-resolvers-use-module-aggregate-bounds"
    "sh07-b13-fragment-module-and-coverage-products-fail-replay"]))

(def ^:private b13-closure-paths
  #{"bootstrap/clojure/src/gravity/digest.clj"
    "bootstrap/clojure/test/gravity/self_hosting/unified_execution_plan.clj"
    "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity"
    "bootstrap/gravity/src/gravity/checked_core.gravity"
    "bootstrap/clojure/test/gravity/self_hosting/sh07_conformance_table.clj"
    "bootstrap/clojure/test/gravity/self_hosting/sh07_module_fragment_test.clj"
    "bootstrap/clojure/fixtures/self-hosting/sh-07-b13"
    "contracts/verification-catalog.edn"
    "contracts/stage0-incremental-test-dependencies.edn"
    "deps.edn"})

(defn- exception-details
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      {:message (.getMessage error)
       :data (ex-data error)})))

(deftest cached-routes-freeze-legacy-selector-oracles
  (is (= (vec (keys legacy-shards))
         (runner/shard-names)))
  (doseq [[shard-name selectors] legacy-shards]
    (let [route (execution-plan/route (str "sh07-cached-" shard-name))]
      (is (= {:kind :exact-vars
              :namespace legacy-namespace
              :selectors selectors}
             (:selection route)))
      (is (= b13-closure-paths
             (set (get-in route [:dependency-closure :paths]))))
      (is (= [:fast] (:lanes route)))
      (is (false? (:replaces? route)))
      (is (false? (:exhaustive? route)))
      (is (= :compatibility-projection (:migration-status route)))
      (is (= {:mode :eligible
              :key-inputs [:dependency-closure :source-bytes :fixture-bytes
                           :runtime :tool :policy]
              :authoritative? false}
             (:cache-policy route)))
      (is (= (count selectors)
             (get-in route [:bounds :max-test-vars]))))))

(deftest cached-runner-uses-routes-without-changing-raw-result
  (let [calls (atom [])
        test-vars (atom nil)
        result
        (with-redefs-fn
          {#'runner/resolve-tests
           (fn [shard-name]
             (swap! calls conj shard-name)
             (vec (repeat (count (get legacy-shards shard-name))
                          ::stub-test-var)))
           #'test/test-vars
           (fn [resolved]
             (reset! test-vars resolved)
             nil)}
          #(runner/run-shard "accepted"))]
    (is (= ["accepted"] @calls))
    (is (= [::stub-test-var ::stub-test-var ::stub-test-var
            ::stub-test-var ::stub-test-var ::stub-test-var]
           @test-vars))
    (is (= #{:test :pass :fail :error :artifact :shard :test-vars
             :cache-authoritative? :fresh-authoritative-run-required?
             :cache-context :elapsed-ms :cache}
           (set (keys result))))
    (is (= {:test 0 :pass 0 :fail 0 :error 0
            :artifact :gravity/sh07-cached-shard-result
            :shard "accepted"
            :test-vars 6
            :cache-authoritative? false
            :fresh-authoritative-run-required? true}
           (select-keys result
                        [:test :pass :fail :error :artifact :shard :test-vars
                         :cache-authoritative?
                         :fresh-authoritative-run-required?])))))

(deftest unknown-and-non-string-shards-retain-diagnostics
  (doseq [[shard-name expected-message]
          [["missing" "Unknown cached SH-07 shard"]
           [:accepted "Unknown cached SH-07 shard"]]]
    (let [details (exception-details #(runner/run-shard shard-name))]
      (is (= expected-message (:message details)))
      (is (= {:id "SH07-CACHED-SHARD"
              :shard shard-name
              :available (vec (keys legacy-shards))}
             (:data details))))))

(deftest altered-route-retains-missing-var-diagnostic
  (let [route (execution-plan/route "sh07-cached-accepted")
        altered (assoc-in route [:selection :selectors 0]
                          "sh07-b13-selector-does-not-exist")
        details
        (with-redefs-fn
          {#'execution-plan/route
           (fn [route-id]
             (is (= "sh07-cached-accepted" route-id))
             altered)}
          #(exception-details (fn [] (runner/run-shard "accepted"))))]
    (is (= "Cached SH-07 shard references an absent test"
           (:message details)))
    (is (= {:id "SH07-CACHED-SHARD-TEST"
            :shard "accepted"
            :test 'sh07-b13-selector-does-not-exist}
           (:data details)))))

(deftest list-and-check-surfaces-preserve-order-and-counts
  (is (= (str (clojure.string/join "\n" (keys legacy-shards)) "\n")
         (with-out-str (runner/-main "--list"))))
  (is (= "{\"accepted\" 6, \"contract\" 2, \"identity\" 2, \"rejected\" 2, \"verification\" 4}\n"
         (with-redefs-fn
           {#'runner/resolve-tests
            (fn [shard-name]
              (vec (repeat (count (get legacy-shards shard-name))
                           ::stub-test-var)))}
           #(with-out-str (runner/-main "--check"))))))

(deftest usage-diagnostic-stays-argument-free
  (let [details (exception-details #(runner/-main "--wat" "accepted"))]
    (is (= "Expected --list, --check, or one cached SH-07 shard name"
           (:message details)))
    (is (= {:id "SH07-CACHED-SHARD-USAGE"
            :arguments ["--wat" "accepted"]
            :available (vec (keys legacy-shards))}
           (:data details)))))
