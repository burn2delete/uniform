(ns gravity.backend.query.native-manifest-test
  "Cold query document data; no source admission or database execution."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.query :as query]
            [gravity.digest :as digest]))

(def ^:private diagnostic-values
  ["B11-DIALECT" "B11-SCHEMA" "B11-TAINT" "B11-PARAMETER" "B11-CAPABILITY"
   "B11-TRANSACTION" "B11-NULL" "B11-MIGRATION" "B11-RESULT" "B11-PLAN" "B11-MANIFEST"])
(defn- hash-text [text] (digest/sha256-hex text))
(defn- publish-refusal [id path subject extra]
  (throw (ex-info "Caller-owned unverified query document refusal"
                  (merge {:id id :source-path path :subject subject} extra))))
(defn- ports []
  {:artifact-id #'hash-text :sql-statement #'query/sql-statement :migration-sql #'query/migration-sql
   :result-adapter-fixture #'query/result-adapter-fixture
   :sql-structurally-valid? #'query/sql-structurally-valid?
   :result-adapter-structurally-valid? #'query/result-adapter-structurally-valid?
   :diagnostic-ids #'diagnostic-values :fail! #'publish-refusal})
(defn- unverified-data []
  {:fixture :forged-unverified-document-data
   :specialized-lowering-artifact {:kind :gravity/stage0-specialized-lowering-artifact
                                   :capability-based-proof {:status :complete}}
   :query-backend-manifest (query/native-manifest (ports) "unverified-source.gravity" {:fixture :unverified-input})
   :b11-diagnostic-stream {:diagnostics (mapv #(hash-map :diagnostic %) diagnostic-values)}})
(defn- refusal [data]
  (try (query/validate-native-manifest! (ports) "query-fixture.gravity" data)
       nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest emits-three-fixed-texts-with-deterministic-hashes-and-borrowed-input
  (let [input {:fixture :unverified-input} path "unverified-query.gravity"
        manifest (query/native-manifest (ports) path input)]
    (is (= manifest (query/native-manifest (ports) path input)))
    (is (identical? input (:input-artifact manifest)))
    (is (identical? input (get-in manifest [:relational-ir-handoff-record :source-artifact])))
    (is (identical? input (get-in manifest [:source-debug-map :source])))
    (is (= (mapv #(str path %) [":query" ":bindings" ":migration" ":adapter"])
           (get-in manifest [:source-debug-map :locations])))
    (is (= (str path ":query") (get-in manifest [:prepared-binding-manifest :parameters 0 :source-span])))
    (is (= (str path ":query:select-gravity-value")
           (get-in manifest [:source-debug-map :query-source-map :select-gravity-value])))
    (is (= (str path ":query:parameter:1")
           (get-in manifest [:source-debug-map :parameter-source-map :gravity-value])))
    (is (= (str path ":query:result:gravity_value")
           (get-in manifest [:source-debug-map :adapter-source-map :gravity_value])))
    (is (= [:mir :c11-mir :c12-query-domain-ir :c14-target-lowering :b1-interface :b11-query-relational-backend]
           (get-in manifest [:source-debug-map :generated-origin-chain])))
    (is (= :not-available-in-current-environment (get-in manifest [:external-database-validation-record :status])))
    (doseq [[field content-key text filename sha]
            [[[:sql-statement-artifacts 0] :sql query/sql-statement "gravity_stage0_query.sql"
              "488f53e5a89c58015718421c74b313b367ccf31c2fb9c44819b9e63f1559213f"]
             [[:migration-artifact] :content query/migration-sql "gravity_stage0_migration.sql"
              "1c8df61ae19adbcb9f36f2ec2754fc9f3f9806f738c866ad940259932ff76daa"]
             [[:typed-result-adapter] :content query/result-adapter-fixture "gravity_stage0_result_adapter.edn"
              "2cab27ad7bbc56c6d3d0204f5cfc7e90456a98bbd8f94f353a076415fd509334"]]]
      (let [record (get-in manifest field) file (io/file "target/validation/query-native-manifest" filename)]
        (is (= text (get record content-key)))
        (is (= sha (digest/sha256-hex text) (:hash record)))
        (io/make-parents file) (spit file text)
        (is (= text (slurp file)))))))

(deftest rejects-all-existing-record-conditions
  (let [data (unverified-data) m :query-backend-manifest]
    (is (= :complete (query/validate-native-manifest! (ports) "unverified.gravity" data)))
    (doseq [[bad id missing]
            [[(dissoc data :specialized-lowering-artifact) "B11-MANIFEST" :specialized-lowering-artifact]
             [(assoc-in data [:specialized-lowering-artifact :capability-based-proof :status] :forged) "B11-MANIFEST" :specialized-lowering-proof]
             [(assoc-in data [m :target :dialect] :forged) "B11-DIALECT" :target-dialect]
             [(assoc-in data [m :relational-ir-handoff-record :status] :missing) "B11-MANIFEST" :relational-ir-handoff-record]
             [(assoc-in data [m :dialect-schema-mapping :status] :missing) "B11-DIALECT" :dialect-schema-mapping]
             [(assoc-in data [m :schema-mapping-record :status] :missing) "B11-SCHEMA" :schema-mapping-record]
             [(assoc-in data [m :sql-statement-artifacts 0 :status] :missing) "B11-MANIFEST" :sql-statement-artifacts]
             [(assoc-in data [m :prepared-binding-manifest :status] :missing) "B11-PARAMETER" :prepared-binding-manifest]
             [(assoc-in data [m :prepared-binding-manifest :dynamic-string-construction] :forged) "B11-TAINT" :dynamic-string-rejection]
             [(assoc-in data [m :query-plan-metadata :status] :missing) "B11-PLAN" :query-plan-metadata]
             [(assoc-in data [m :query-plan-metadata :provider-specific-movement] [:unproven]) "B11-PLAN" :provider-plan-equivalence]
             [(assoc-in data [m :typed-result-adapter :status] :missing) "B11-RESULT" :typed-result-adapter]
             [(assoc-in data [m :transaction-isolation-manifest :status] :missing) "B11-TRANSACTION" :transaction-isolation-manifest]
             [(assoc-in data [m :migration-artifact :status] :missing) "B11-MIGRATION" :migration-artifact]
             [(assoc-in data [m :migration-artifact :data-loss] :forged) "B11-MIGRATION" :data-loss-policy]
             [(assoc-in data [m :schema-diff-compatibility-report :status] :missing) "B11-SCHEMA" :schema-diff-compatibility]
             [(assoc-in data [m :capability-taint-report :status] :missing) "B11-CAPABILITY" :capability-taint-report]
             [(assoc-in data [m :capability-taint-report :tainted-string-query] :forged) "B11-TAINT" :tainted-string-query-rejection]
             [(assoc-in data [m :null-collation-timezone-numeric-json-enum-behavior :status] :missing) "B11-NULL" :null-type-behavior-map]
             [(assoc-in data [m :distributed-workflow-integration-record :status] :missing) "B11-TRANSACTION" :workflow-integration-record]
             [(assoc-in data [m :source-debug-map :status] :missing) "B11-MANIFEST" :source-debug-map]
             [(update data m dissoc :external-database-validation-record) "B11-MANIFEST" :query-artifact-manifest]
             [(assoc-in data [:b11-diagnostic-stream :diagnostics] []) "B11-MANIFEST" :b11-diagnostics]]]
      (let [result (refusal bad)]
        (is (= id (:id result)))
        (is (= [missing] (:missing-fields result)))
        (is (= "query-fixture.gravity" (:source-path result)))))))

(deftest missing-reference-tokens-and-forbidden-sql-fragments-reject
  (let [data (unverified-data)]
    (doseq [[operation text tokens id missing]
            [[#'query/sql-statement query/sql-statement ["$1::bigint" "gravity_value"] "B11-TAINT" :parameterized-sql]
             [#'query/result-adapter-fixture query/result-adapter-fixture
              [":gravity-type :I64" ":database-type :bigint" ":nullability :nonnull" ":validation :checked" ":taint-policy :validated"]
              "B11-RESULT" :result-adapter-validation]]]
      (doseq [token tokens]
        (with-redefs-fn {operation (str/replace text token "removed")}
          (fn [] (let [result (refusal data)]
                   (is (= id (:id result)))
                   (is (= [missing] (:missing-fields result))))))))
    (doseq [suffix ["||" "${" "concat("]]
      (with-redefs [query/sql-statement (str query/sql-statement suffix)]
        (is (= "B11-TAINT" (:id (refusal data))))
        (is (= [:parameterized-sql] (:missing-fields (refusal data))))))))

(deftest preserves-all-twenty-five-refusal-steps-and-subjects
  (let [calls (atom []) manifest {:query-plan-metadata {:provider-specific-movement [:unproven]}}
        supplied (assoc (ports) :sql-statement "invalid" :result-adapter-fixture "invalid"
                        :fail! (fn [& args] (swap! calls conj args) false))]
    (is (= :complete (query/validate-native-manifest! supplied "ordered.gravity" {:query-backend-manifest manifest})))
    (is (= ["B11-MANIFEST" "B11-MANIFEST" "B11-DIALECT" "B11-MANIFEST" "B11-DIALECT" "B11-SCHEMA"
            "B11-MANIFEST" "B11-TAINT" "B11-PARAMETER" "B11-TAINT" "B11-PLAN" "B11-PLAN"
            "B11-RESULT" "B11-RESULT" "B11-TRANSACTION" "B11-MIGRATION" "B11-MIGRATION" "B11-SCHEMA"
            "B11-CAPABILITY" "B11-TAINT" "B11-NULL" "B11-TRANSACTION" "B11-MANIFEST" "B11-MANIFEST" "B11-MANIFEST"]
           (mapv first @calls)))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof] [:target-dialect]
            [:relational-ir-handoff-record] [:dialect-schema-mapping] [:schema-mapping-record]
            [:sql-statement-artifacts] [:parameterized-sql] [:prepared-binding-manifest] [:dynamic-string-rejection]
            [:query-plan-metadata] [:provider-plan-equivalence] [:typed-result-adapter] [:result-adapter-validation]
            [:transaction-isolation-manifest] [:migration-artifact] [:data-loss-policy] [:schema-diff-compatibility]
            [:capability-taint-report] [:tainted-string-query-rejection] [:null-type-behavior-map]
            [:workflow-integration-record] [:source-debug-map] [:query-artifact-manifest] [:b11-diagnostics]]
           (mapv #(:missing-fields (nth % 3)) @calls)))
    (is (every? #(= "ordered.gravity" (second %)) @calls))
    (is (every? nil? (map #(nth % 2) (take 2 @calls))))
    (is (every? #(identical? manifest (nth % 2)) (take 22 (drop 2 @calls))))
    (is (nil? (nth (last @calls) 2)))))

(deftest first-failure-preserves-the-caller-exception-object
  (let [error (Error. "Caller-owned query failure") calls (atom [])]
    (is (identical? error
                    (try (query/validate-native-manifest!
                          (assoc (ports) :fail! (fn [& args] (swap! calls conj args) (throw error))) "identity.gravity" {})
                         nil (catch Throwable actual actual))))
    (is (= [["B11-MANIFEST" "identity.gravity" nil {:missing-fields [:specialized-lowering-artifact]}]] @calls))))

(deftest failure-var-is-resolved-at-every-original-use
  (let [error (Error. "Late caller failure") calls (atom [])]
    (with-redefs [publish-refusal (fn [& args]
                                  (swap! calls conj args)
                                  (alter-var-root #'publish-refusal
                                                  (constantly (fn [& later]
                                                                (swap! calls conj later) (throw error)))))]
      (is (identical? error
                      (try (query/validate-native-manifest! (ports) "late.gravity" {})
                           nil (catch Throwable actual actual)))))
    (is (= [[:specialized-lowering-artifact] [:specialized-lowering-proof]]
           (mapv #(:missing-fields (nth % 3)) @calls)))))

(deftest three-hash-calls-preserve-late-function-and-value-reads
  (let [originals [query/sql-statement query/migration-sql query/result-adapter-fixture] calls (atom [])
        changed (fn [number] (mapv #(str % "\n-- change " number "\n") originals))
        set-texts! (fn [texts]
                     (doseq [[operation text] (map vector [#'query/sql-statement #'query/migration-sql #'query/result-adapter-fixture] texts)]
                       (alter-var-root operation (constantly text))))]
    (with-redefs [query/sql-statement query/sql-statement query/migration-sql query/migration-sql
                  query/result-adapter-fixture query/result-adapter-fixture
                  hash-text (fn [text]
                              (swap! calls conj [:sql text]) (set-texts! (changed 1))
                              (alter-var-root #'hash-text
                                              (constantly (fn [second-text]
                                                            (swap! calls conj [:migration second-text]) (set-texts! (changed 2))
                                                            (alter-var-root #'hash-text
                                                                            (constantly (fn [third-text]
                                                                                          (swap! calls conj [:adapter third-text])
                                                                                          (set-texts! (changed 3))
                                                                                          (digest/sha256-hex third-text))))
                                                            (digest/sha256-hex second-text))))
                              (digest/sha256-hex text))]
      (let [manifest (query/native-manifest (ports) "late.gravity" :unverified-id)]
        (is (= [[:sql (nth originals 0)] [:migration (nth (changed 1) 1)] [:adapter (nth (changed 2) 2)]] @calls))
        (doseq [[field content-key text hashed]
                [[[:sql-statement-artifacts 0] :sql (nth (changed 3) 0) (nth originals 0)]
                 [[:migration-artifact] :content (nth (changed 3) 1) (nth (changed 1) 1)]
                 [[:typed-result-adapter] :content (nth (changed 3) 2) (nth (changed 2) 2)]]]
          (let [record (get-in manifest field)]
            (is (= text (get record content-key)))
            (is (= (digest/sha256-hex hashed) (:hash record)))
            (is (not= (digest/sha256-hex text) (:hash record)))))))))

(deftest predicate-order-and-late-lookup-remain-observable
  (let [data (unverified-data) calls (atom [])
        supplied (assoc (ports)
                        :sql-structurally-valid? (fn [_] (swap! calls conj :sql) true)
                        :result-adapter-structurally-valid? (fn [_] (swap! calls conj :adapter) true))]
    (is (= :complete (query/validate-native-manifest! supplied "order.gravity" data)))
    (is (= [:sql :adapter] @calls))
    (reset! calls [])
    (is (= :complete (:status (query/native-coverage-report supplied data))))
    (is (= [:sql :adapter] @calls))
    (reset! calls [])
    (query/native-coverage-report supplied {})
    (is (= [:sql] @calls))
    (with-redefs [query/result-adapter-structurally-valid? query/result-adapter-structurally-valid?
                  query/sql-structurally-valid?
                  (fn [_] (alter-var-root #'query/result-adapter-structurally-valid? (constantly (fn [_] false))) true)]
      (is (= [:result-adapter-validation] (:missing-fields (refusal data)))))))

(deftest failure-operation-is-selected-before-final-subject-evaluation
  (let [events (atom []) armed (atom false) error (ex-info "Original caller" {:identity :original})
        comparator (fn [left right]
                     (when (and @armed (= :b11-diagnostic-stream left))
                       (reset! armed false) (swap! events conj :argument)
                       (alter-var-root #'publish-refusal (constantly (fn [& _] (throw (Error. "Late caller"))))))
                     (compare left right))
        data (into (sorted-map-by comparator) (unverified-data))]
    (with-redefs [diagnostic-values (lazy-seq (reset! armed true) nil)
                  publish-refusal (fn [& _] (swap! events conj :original) (throw error))]
      (is (identical? error (try (query/validate-native-manifest! (ports) "selection.gravity" data)
                                 nil (catch Throwable actual actual)))))
    (is (= [:argument :original] @events))))

(deftest forged-content-and-empty-bindings-do-not-authenticate-query-semantics
  (let [data (-> (unverified-data)
                 (assoc-in [:query-backend-manifest :sql-statement-artifacts 0 :sql] "not parameterized SQL")
                 (assoc-in [:query-backend-manifest :typed-result-adapter :content] "not an adapter")
                 (assoc-in [:query-backend-manifest :migration-artifact :content] "unverified migration text")
                 (assoc-in [:query-backend-manifest :prepared-binding-manifest :parameters] [])
                 (assoc-in [:query-backend-manifest :capability-taint-report :granted-capabilities] #{}))
        report (query/native-coverage-report (ports) {})]
    (is (= :complete (query/validate-native-manifest! (ports) "forged.gravity" data)))
    (is (= :complete (:status report)))
    (is (false? (:manifest-complete? report)))
    (is (false? (:backend-interface-input-verified? report)))
    (is (= :not-available-in-current-environment
           (:external-database-validation? (query/native-coverage-report (ports) data))))))

(deftest new-apis-have-explicit-non-admitting-contracts
  (is (= 14 (count (ns-publics 'gravity.backend.query))))
  (doseq [operation [#'query/native-manifest #'query/validate-native-manifest! #'query/native-coverage-report]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= 'gravity.backend.query (:namespace contract)))
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? entry)))
      (is (false? (:grants-authority? entry)))
      (is (false? (:issues-certificates? entry)))
      (is (seq (:required-operations entry)))
      (is (= '[gravity.bootstrap gravity.cli gravity.release]
             (get-in contract [:dependency-direction :must-not-require])))
      (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :consumed-by]))))))
