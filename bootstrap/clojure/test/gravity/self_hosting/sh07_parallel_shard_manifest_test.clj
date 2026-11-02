(ns gravity.self-hosting.sh07-parallel-shard-manifest-test
  (:require [clojure.string :as str]
            [clojure.test :as test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-parallel-shard-runner :as runner]
            [gravity.self-hosting.unified-execution-cli :as cli]
            [gravity.self-hosting.unified-execution-plan :as plan]))

(def ^:private route-prefix
  "sh07-parallel-")

;; Frozen selector parity table from the pre-manifest parallel runner at the
;; candidate base.  The table is intentionally data-only: it is the reviewed
;; oracle for the compatibility projection, not another executable catalog.
;; Owning semantic test renames and newly consumed fixture paths are updated
;; explicitly here and in the catalog; historical result records are unchanged.
(def ^:private expected-shards
  {"accepted"
   {:namespace "gravity.self-hosting.sh07-vector-pattern-test"
    :selectors ["sh07-b11-direct-and-public-routing-use-v12"
                "sh07-b11-preserves-b10-scalar-pattern-products"
                "sh07-b11-pattern-records-are-preorder-bounded-and-parent-linked"
                "sh07-b11-nested-vector-records-preserve-source-order-and-kinds"
                "sh07-b11-vector-bindings-are-unique-branch-local-and-use-linked"
                "sh07-b11-scrutinee-is-once-and-vector-branches-are-conditional"
                "sh07-b11-vector-branch-preserves-tail-recur-target"
                "sh07-b11-public-proof-is-bounded-and-honest"]}
   "identity-lineage"
   {:namespace "gravity.self-hosting.sh07-vector-pattern-test"
    :selectors ["sh07-b11-identities-are-deterministic-path-neutral-and-provenanced"
                "sh07-b11-pattern-products-bind-to-the-authenticated-sh06-lineage"]}
   "rejected"
   {:namespace "gravity.self-hosting.sh07-vector-pattern-test"
    :selectors ["sh07-b11-rejections-are-structured-and-oracle-bound"]}
   "verification"
   {:namespace "gravity.self-hosting.sh07-vector-pattern-test"
    :selectors ["sh07-b11-pattern-product-alterations-fail-replay"
                "sh07-b11-pattern-resolvers-enforce-record-depth-width-and-count-bounds"
                "sh07-b11-pattern-resolver-rejects-invalid-parent-graphs"
                "sh07-b11-pattern-graph-completeness-rejects-structural-alterations"]}
   "b12-contract-accepted"
   {:namespace "gravity.self-hosting.sh07-alias-qualified-reference-test"
    :selectors ["sh07-b12-fixtures-are-dynamically-discovered-paired-and-bounded"
                "sh07-b12-source-and-file-compose-real-module-artifacts"
                "sh07-b12-declared-alias-table-is-exact-bounded-and-projected"
                "sh07-b12-alias-qualified-value-and-operator-references-bind-exactly"
                "sh07-b12-alias-targets-come-from-the-actual-module"
                "sh07-b12-reference-and-call-record-contract-remains-b11-compatible"
                "sh07-b12-alias-call-evaluates-operator-before-arguments"
                "sh07-b12-alias-fully-qualified-and-core-controls-remain-distinct"]}
   "b12-identity-verification"
   {:namespace "gravity.self-hosting.sh07-alias-qualified-reference-test"
    :selectors ["sh07-b12-identities-are-deterministic-path-neutral-and-provenanced"
                "sh07-b12-alias-products-retain-authenticated-sh06-lineage"
                "sh07-b12-alias-table-alterations-fail-closed"
                "sh07-b12-declared-alias-product-alterations-fail-replay"
                "sh07-b12-resolution-order-and-binding-alterations-fail-closed"
                "sh07-b12-public-replay-and-capability-proof-pass"]}
   "b12-rejected-regression"
   {:namespace "gravity.self-hosting.sh07-alias-qualified-reference-test"
    :selectors ["sh07-b12-rejected-fixtures-follow-their-declared-oracles"
                "sh07-b12-preserves-b11-vector-pattern-products"
                "sh07-b12-claim-boundary-remains-honest"]}
   "regression-a"
   {:namespace "gravity.self-hosting.sh07-match-lowering-test"
    :selectors ["sh07-b10-fixtures-are-paired-byte-identical-and-cover-the-foundation"
                "sh07-b10-direct-and-public-routing-use-v12"
                "sh07-b10-match-nodes-record-exact-order-and-pending-facts"
                "sh07-b10-pattern-families-and-binding-scopes-are-explicit"
                "sh07-b10-sibling-bindings-are-branch-local"
                "sh07-b10-nested-matches-have-distinct-groups-and-global-order"
                "sh07-b10-scrutinee-is-once-and-branches-are-conditional"
                "sh07-b10-scrutinee-and-branch-error-transfers-have-exact-regions"
                "sh07-b10-match-branches-preserve-enclosing-tail-recur-targets"
                "sh07-b10-match-scrutinee-does-not-inherit-tail-position"]}
   "regression-b"
   {:namespace "gravity.self-hosting.sh07-match-lowering-test"
    :selectors ["sh07-b10-identities-are-deterministic-path-neutral-and-provenanced"
                "sh07-b10-records-bind-to-the-authenticated-sh06-lineage"
                "sh07-b10-rejections-are-structured-and-oracle-bound"
                "sh07-b10-fixed-vector-fixture-is-promoted-by-b11"
                "sh07-b10-branch-binding-escape-fails-at-name-resolution"
                "sh07-b10-substituted-and-stale-inputs-fail-closed"
                "sh07-b10-match-product-alterations-fail-replay"
                "sh07-b10-public-proof-is-bounded-and-honest"
                "sh07-b10-match-branch-resolver-rejects-over-limit-vectors"
                "sh07-b10-match-decision-resolver-rejects-over-limit-vectors"
                "sh07-b10-match-record-resolver-rejects-invalid-boundary-values"]}})

;; Keep route order and non-selection metadata frozen independently of the
;; generated manifest projection.  This is deliberately a reviewed parity
;; oracle: replacing it with a projection of `plan/route-catalog` would make
;; drift self-justifying.
(def ^:private expected-shard-order
  ["accepted"
   "b12-contract-accepted"
   "b12-identity-verification"
   "b12-rejected-regression"
   "identity-lineage"
   "regression-a"
   "regression-b"
   "rejected"
   "verification"])

(def ^:private expected-route-fields
  {:lanes [:fast]
   :execution-mode :warm-bounded-process
   :nonclaims [:integration :proof :release :self-hosting :seed-retirement
               :safety :performance]
   :migration-status :compatibility-projection
   :required-evidence [:selected-tests :fixture-pairing :diagnostics :receipt]
   :authority-ceiling :non-authoritative
   :replacement-for [:parallel-shard-runner]
   :replaces? false
   :exhaustive? false
   :bounds {:timeout-ms 3600000
            :max-output-bytes 262144}
   :resource-class :memory-heavy
   :cache-policy {:mode :eligible
                  :key-inputs [:dependency-closure :source-bytes :fixture-bytes
                               :runtime :tool :policy]
                  :authoritative? false}
   :receipt-policy {:schema :gravity/unified-execution-receipt-v1
                    :required-fields [:plan-id :selection :closure-identity
                                      :status :diagnostics :cache :authority]}
   :executor {:adapter :exact-tree-warm-worker
              :bounded? true
              :fresh-capable? false
              :authority :non-authoritative}
   :module-id "sh07-checked-core"})

(def ^:private vector-pattern-closure
  {:module-ids ["sh07-checked-core"]
   :paths ["bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity"
           "bootstrap/gravity/src/gravity/checked_core.gravity"
           "bootstrap/clojure/test/gravity/self_hosting/sh07_vector_pattern_test.clj"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b10"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b11"
           "contracts/stage0-incremental-test-dependencies.edn"
           "deps.edn"]
   :fixture-suite-ids ["sh07-b10" "sh07-b11"]
   :complete? true
   :identity-inputs [:source-bytes :fixture-bytes :test-bytes :classpath
                     :runtime :policy]})

(def ^:private alias-reference-closure
  {:module-ids ["sh07-checked-core"]
   :paths ["bootstrap/gravity/src/gravity/compiler/c5_name_resolution_namespace_analyzer.gravity"
           "bootstrap/gravity/src/gravity/resolution.gravity"
           "bootstrap/clojure/fixtures/self-hosting/sh-06/modules/shared-core.gravity"
           "bootstrap/clojure/fixtures/self-hosting/sh-06/modules/shared-core.qst"
           "bootstrap/gravity/src/gravity/checked_core.gravity"
           "bootstrap/clojure/test/gravity/self_hosting/sh07_alias_qualified_reference_test.clj"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b10"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b11"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b12"
           "contracts/stage0-incremental-test-dependencies.edn"
           "deps.edn"]
   :fixture-suite-ids ["sh07-b10" "sh07-b11" "sh07-b12"]
   :complete? true
   :identity-inputs [:source-bytes :fixture-bytes :test-bytes :classpath
                     :runtime :policy]})

(def ^:private match-lowering-closure
  {:module-ids ["sh07-checked-core"]
   :paths ["bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity"
           "bootstrap/gravity/src/gravity/checked_core.gravity"
           "bootstrap/clojure/test/gravity/self_hosting/sh07_match_lowering_test.clj"
           "bootstrap/clojure/fixtures/self-hosting/sh-07-b10"
           "contracts/stage0-incremental-test-dependencies.edn"
           "deps.edn"]
   :fixture-suite-ids ["sh07-b10"]
   :complete? true
   :identity-inputs [:source-bytes :fixture-bytes :test-bytes :classpath
                     :runtime :policy]})

(def ^:private expected-closures
  (merge
   (zipmap ["accepted" "identity-lineage" "rejected" "verification"]
           (repeat vector-pattern-closure))
   (zipmap ["b12-contract-accepted" "b12-identity-verification"
            "b12-rejected-regression"]
           (repeat alias-reference-closure))
   (zipmap ["regression-a" "regression-b"]
           (repeat match-lowering-closure))))

(defn- route-id
  [shard-name]
  (str route-prefix shard-name))

(defn- error-id
  [thunk]
  (try
    (thunk)
    nil
    (catch clojure.lang.ExceptionInfo error
      (:id (ex-data error)))))

(defn- expected-route
  [shard-name]
  (merge expected-route-fields
         {:id (route-id shard-name)
          :selection (assoc (get expected-shards shard-name) :kind :exact-vars)
          :dependency-closure (get expected-closures shard-name)}))

(def ^:private required-transitive-fixture-suites
  {"gravity.self-hosting.sh07-vector-pattern-test"
   ["sh07-b10" "sh07-b11"]
   "gravity.self-hosting.sh07-alias-qualified-reference-test"
   ["sh07-b10" "sh07-b11" "sh07-b12"]
   "gravity.self-hosting.sh07-match-lowering-test"
   ["sh07-b10"]})

(defn- fixture-suite-path
  [suite-id]
  (str "bootstrap/clojure/fixtures/self-hosting/"
       (str/replace suite-id #"^sh07-" "sh-07-")))

(deftest manifest-parallel-routes-match-frozen-projection
  (let [expected-route-ids (mapv route-id expected-shard-order)
        actual-route-ids (->> (plan/route-names)
                              (filter #(str/starts-with? % route-prefix))
                              vec)]
    (is (= expected-route-ids actual-route-ids))
    (doseq [shard-name expected-shard-order]
      (testing shard-name
        ;; Compare the complete route record.  The expected record is frozen
        ;; above and is not obtained from any generated manifest projection.
        (is (= (expected-route shard-name)
               (plan/route (route-id shard-name))))))))

(deftest manifest-routes-preserve-the-nine-shard-names
  (let [expected-names (vec (sort (keys expected-shards)))
        actual-route-ids (->> (plan/route-names)
                              (filter #(str/starts-with? % route-prefix))
                              vec)]
    (is (= expected-names (runner/shard-names)))
    (is (= (set (map route-id expected-names))
           (set actual-route-ids)))
    (is (= (count expected-shards) (count actual-route-ids)))))

(deftest manifest-selectors-and-policies-match-the-legacy-table
  (doseq [[shard-name {:keys [namespace selectors]}] expected-shards]
    (testing shard-name
      (let [route (plan/route (route-id shard-name))
            selection (:selection route)
            closure (:dependency-closure route)
            test-path (str "bootstrap/clojure/test/"
                           (-> namespace
                               (str/replace "." "/")
                               (str/replace "-" "_")
                               (str ".clj")))
            fixture-suite (first (re-find #"^(sh07-b\d+)"
                                          (first selectors)))
            fixture-path (str "bootstrap/clojure/fixtures/self-hosting/"
                              (str/replace fixture-suite
                                           #"^sh07-"
                                           "sh-07-"))
            required-suites (get required-transitive-fixture-suites namespace)
            closure-paths (set (:paths closure))
            closure-suites (set (:fixture-suite-ids closure))]
        (is (= {:kind :exact-vars
                :namespace namespace
                :selectors selectors}
               selection))
        (is (= [:fast] (:lanes route)))
        (is (= :memory-heavy (:resource-class route)))
        (is (= :warm-bounded-process (:execution-mode route)))
        (is (= :eligible (get-in route [:cache-policy :mode])))
        (is (= :exact-tree-warm-worker
               (get-in route [:executor :adapter])))
        (is (false? (get-in route [:executor :fresh-capable?])))
        (is (= :non-authoritative (:authority-ceiling route)))
        (is (true? (:complete? closure)))
        (is (= ["sh07-checked-core"] (:module-ids closure)))
        (is (contains? closure-paths test-path))
        (is (contains? closure-paths fixture-path))
        (is (= (set required-suites) closure-suites))
        (doseq [suite-id required-suites]
          (is (contains? closure-paths (fixture-suite-path suite-id))))))))

(deftest check-output-preserves-selector-counts-and-unknown-diagnostic
  ;; Resolution is bounded and does not execute any selected test var.
  (let [expected (into (sorted-map)
                       {"accepted" 8
                        "b12-contract-accepted" 8
                        "b12-identity-verification" 6
                        "b12-rejected-regression" 3
                        "identity-lineage" 2
                        "rejected" 1
                        "regression-a" 10
                        "regression-b" 11
                        "verification" 4})]
    (is (= (str (pr-str expected) "\n")
           (with-out-str (runner/-main "--check")))))
  (is (= "SH07-PARALLEL-SHARD"
         (error-id #(runner/run-shard "not-a-reviewed-shard"))))
  (is (= "SH07-PARALLEL-SHARD"
         (error-id #(runner/run-shard 'accepted))))
  (is (= "SH07-PARALLEL-SHARD"
         (error-id #(runner/run-shard :accepted)))))

(deftest list-output-preserves-shard-order-and-newline
  (is (= (str (str/join "\n" (runner/shard-names)) "\n")
         (with-out-str (runner/-main "--list")))))

(deftest shared-resolution-preserves-every-shards-ordered-test-vars
  (doseq [[shard {:keys [namespace selectors]}] expected-shards]
    (let [selected (atom nil)
          result (with-redefs [test/test-vars #(reset! selected %)]
                   (runner/run-shard shard))]
      (is (= (mapv #(str (ns-name (:ns (meta %))) "/" (:name (meta %)))
                   @selected)
             (mapv #(str namespace "/" %) selectors)))
      (is (= {:test 0 :pass 0 :fail 0 :error 0
              :shard shard :test-vars (count selectors)} result)))))

(deftest shared-resolution-preserves-missing-var-diagnostic-before-execution
  (let [missing "sh07-b11-missing-retirement-test"
        altered (assoc-in plan/catalog [:routes "sh07-parallel-accepted"
                                        :selection :selectors 0] missing)
        executed (atom false)
        details (with-redefs [plan/catalog altered
                              test/test-vars #(reset! executed true)]
                  (try (runner/run-shard "accepted") nil
                       (catch clojure.lang.ExceptionInfo error
                         [(ex-message error) (ex-data error)])))]
    (is (= ["SH-07 parallel shard references an absent test"
            {:id "SH07-PARALLEL-SHARD-TEST" :shard "accepted"
             :namespace 'gravity.self-hosting.sh07-vector-pattern-test
             :test (symbol missing)}] details))
    (is (false? @executed))))

(deftest delegated-selection-retains-fixtures-counters-and-exception-cleanup
  (let [ns-name 'gravity.self-hosting.parallel-retirement-fixture
        fixture-ns (create-ns ns-name)
        events (atom [])
        fixture (fn [label]
                  (fn [f]
                    (swap! events conj [label :before])
                    (try (f) (finally (swap! events conj [label :after])))))
        vars (mapv (fn [[name body]]
                     (let [v (intern fixture-ns name (fn []))]
                       (alter-meta! v assoc :test body)
                       v))
                   [['pass #(test/is true)]
                    ['error #(throw (ex-info "fixture error" {}))]])]
    (try
      (binding [*ns* fixture-ns]
        (test/use-fixtures :once (fixture :once))
        (test/use-fixtures :each (fixture :each)))
      (let [result (binding [*out* (java.io.StringWriter.)
                            test/*test-out* (java.io.StringWriter.)]
                     (with-redefs [cli/resolve-legacy-shard
                                   (fn [mechanism shard]
                                     (is (= :parallel-shard-runner mechanism))
                                     (is (= "accepted" shard))
                                     {:test-vars vars})]
                       (runner/run-shard "accepted")))]
        (is (= {:test 2 :pass 1 :fail 0 :error 1
                :shard "accepted" :test-vars 2} result))
        (is (= [[:once :before] [:each :before] [:each :after]
                [:each :before] [:each :after] [:once :after]] @events)))
      (finally (remove-ns ns-name)))))
