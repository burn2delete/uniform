(ns gravity.self-hosting.sh07-parallel-shard-runner
  "Process-isolated routing for heavyweight SH-07 test shards.

  Use `--list` to print shard names, `--check` to resolve every routed test
  without running it, or pass one shard name to run that shard."
  (:require [clojure.test :as test]))

(def ^:private vector-pattern-namespace
  'gravity.self-hosting.sh07-vector-pattern-test)

(def ^:private match-lowering-namespace
  'gravity.self-hosting.sh07-match-lowering-test)

(def ^:private alias-reference-namespace
  'gravity.self-hosting.sh07-alias-qualified-reference-test)

(def ^:private shards
  (sorted-map
   "accepted"
   [[vector-pattern-namespace
     '[sh07-b11-direct-and-public-routing-use-v12
       sh07-b11-preserves-b10-scalar-pattern-products
       sh07-b11-pattern-records-are-preorder-bounded-and-parent-linked
       sh07-b11-nested-vector-records-preserve-source-order-and-kinds
       sh07-b11-vector-bindings-are-unique-branch-local-and-use-linked
       sh07-b11-scrutinee-is-once-and-vector-branches-are-conditional
       sh07-b11-vector-branch-preserves-tail-recur-target
       sh07-b11-public-proof-is-bounded-and-honest]]]

   "identity-lineage"
   [[vector-pattern-namespace
     '[sh07-b11-identities-are-deterministic-path-neutral-and-provenanced
       sh07-b11-pattern-products-bind-to-the-authenticated-sh06-lineage]]]

   "rejected"
   [[vector-pattern-namespace
     '[sh07-b11-rejections-are-structured-and-oracle-bound]]]

   "verification"
   [[vector-pattern-namespace
     '[sh07-b11-pattern-product-alterations-fail-replay
       sh07-b11-pattern-resolvers-enforce-record-depth-width-and-count-bounds
       sh07-b11-pattern-resolver-rejects-invalid-parent-graphs
       sh07-b11-pattern-graph-completeness-rejects-structural-alterations]]]

   "b12-contract-accepted"
   [[alias-reference-namespace
     '[sh07-b12-fixtures-are-dynamically-discovered-paired-and-bounded
       sh07-b12-direct-and-public-routing-use-v13
       sh07-b12-declared-alias-table-is-exact-bounded-and-projected
       sh07-b12-alias-qualified-value-and-operator-references-bind-exactly
       sh07-b12-alias-targets-may-be-a-binding-target-subset
       sh07-b12-reference-and-call-record-contract-remains-b11-compatible
       sh07-b12-alias-call-evaluates-operator-before-arguments
       sh07-b12-alias-fully-qualified-and-core-controls-remain-distinct]]]

   "b12-identity-verification"
   [[alias-reference-namespace
     '[sh07-b12-identities-are-deterministic-path-neutral-and-provenanced
       sh07-b12-alias-products-retain-authenticated-sh06-lineage
       sh07-b12-alias-table-alterations-fail-closed
       sh07-b12-declared-alias-product-alterations-fail-replay
       sh07-b12-resolution-order-and-binding-alterations-fail-closed
       sh07-b12-public-replay-and-capability-proof-pass]]]

   "b12-rejected-regression"
   [[alias-reference-namespace
     '[sh07-b12-rejected-fixtures-follow-their-declared-oracles
       sh07-b12-preserves-b11-vector-pattern-products
       sh07-b12-claim-boundary-remains-honest]]]

   "regression-a"
   [[match-lowering-namespace
     '[sh07-b10-fixtures-are-paired-byte-identical-and-cover-the-foundation
       sh07-b10-direct-and-public-routing-use-v12
       sh07-b10-match-nodes-record-exact-order-and-pending-facts
       sh07-b10-pattern-families-and-binding-scopes-are-explicit
       sh07-b10-sibling-bindings-are-branch-local
       sh07-b10-nested-matches-have-distinct-groups-and-global-order
       sh07-b10-scrutinee-is-once-and-branches-are-conditional
       sh07-b10-scrutinee-and-branch-error-transfers-have-exact-regions
       sh07-b10-match-branches-preserve-enclosing-tail-recur-targets
       sh07-b10-match-scrutinee-does-not-inherit-tail-position]]]

   "regression-b"
   [[match-lowering-namespace
     '[sh07-b10-identities-are-deterministic-path-neutral-and-provenanced
       sh07-b10-records-bind-to-the-authenticated-sh06-lineage
       sh07-b10-rejections-are-structured-and-oracle-bound
       sh07-b10-fixed-vector-fixture-is-promoted-by-b11
       sh07-b10-branch-binding-escape-fails-at-name-resolution
       sh07-b10-substituted-and-stale-inputs-fail-closed
       sh07-b10-match-product-alterations-fail-replay
       sh07-b10-public-proof-is-bounded-and-honest
       sh07-b10-match-branch-resolver-rejects-over-limit-vectors
       sh07-b10-match-decision-resolver-rejects-over-limit-vectors
       sh07-b10-match-record-resolver-rejects-invalid-boundary-values]]]))

(defn shard-names
  []
  (vec (keys shards)))

(defn- resolve-shard
  [shard-name]
  (let [routes (get shards shard-name)]
    (when-not routes
      (throw
       (ex-info
        "Unknown SH-07 parallel test shard"
        {:id "SH07-PARALLEL-SHARD"
         :shard shard-name
         :available (shard-names)})))
    (mapv
     (fn [[namespace test-symbols]]
       (require namespace)
       {:namespace namespace
        :test-vars
        (mapv
         (fn [test-symbol]
           (let [test-var (ns-resolve namespace test-symbol)]
             (when-not (and (var? test-var) (:test (meta test-var)))
               (throw
                (ex-info
                 "SH-07 parallel shard references an absent test"
                 {:id "SH07-PARALLEL-SHARD-TEST"
                  :shard shard-name
                  :namespace namespace
                  :test test-symbol})))
             test-var))
         test-symbols)})
     routes)))

(defn check-shards
  []
  (into
   (sorted-map)
   (map
    (fn [shard-name]
      [shard-name
       (reduce + (map #(count (:test-vars %))
                      (resolve-shard shard-name)))])
    (shard-names))))

(defn run-shard
  [shard-name]
  (let [routes (resolve-shard shard-name)
        test-vars (vec (mapcat :test-vars routes))
        counters (ref test/*initial-report-counters*)]
    (binding [test/*report-counters* counters]
      (test/test-vars test-vars))
    (assoc @counters
           :shard shard-name
           :test-vars (count test-vars))))

(defn -main
  [& arguments]
  (cond
    (= ["--list"] (vec arguments))
    (doseq [shard-name (shard-names)]
      (println shard-name))

    (= ["--check"] (vec arguments))
    (println (pr-str (check-shards)))

    (= 1 (count arguments))
    (let [result (run-shard (first arguments))]
      (println (pr-str result))
      (when-not (and (zero? (:fail result)) (zero? (:error result)))
        (System/exit 1)))

    :else
    (throw
     (ex-info
      "Expected --list, --check, or one SH-07 shard name"
      {:id "SH07-PARALLEL-SHARD-USAGE"
       :arguments (vec arguments)
       :available (shard-names)}))))
