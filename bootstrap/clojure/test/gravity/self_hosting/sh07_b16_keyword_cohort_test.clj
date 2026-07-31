(ns gravity.self-hosting.sh07-b16-keyword-cohort-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b16_keyword_cohort_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B16 cohort test source is not on the classpath"
        {:id "SH07-B16-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B16-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private cohort
  [{:module :b5-jvm
    :path
    "bootstrap/gravity/src/gravity/backend/b5_jvm_backend_design.gravity"
    :keyword-lookup-count 9}
   {:module :b6-javascript-typescript
    :path
    "bootstrap/gravity/src/gravity/backend/b6_javascript_typescript_backend_design.gravity"
    :keyword-lookup-count 8}
   {:module :b7-mlir
    :path
    "bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity"
    :keyword-lookup-count 5}
   {:module :b8-gpu
    :path
    "bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity"
    :keyword-lookup-count 8}
   {:module :b9-hdl
    :path
    "bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity"
    :keyword-lookup-count 10}
   {:module :b10-workflow
    :path
    "bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity"
    :keyword-lookup-count 7}
   {:module :b11-query
    :path
    "bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity"
    :keyword-lookup-count 8}
   {:module :b12-mobile
    :path
    "bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity"
    :keyword-lookup-count 8}
   {:module :c11-mir
    :path
    "bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity"
    :keyword-lookup-count 0}])
(def ^:private expected-module-counts
  {:b5-jvm 9
   :b6-javascript-typescript 8
   :b7-mlir 5
   :b8-gpu 8
   :b9-hdl 10
   :b10-workflow 7
   :b11-query 8
   :b12-mobile 8
   :c11-mir 0})
(def ^:private expected-lookup-total 63)
(def ^:private lookup-record-keys
  #{:core-node-id :keyword-node-id :map-node-id :keyword
    :ordered-evaluation-node-ids :evaluation-order
    :missing-key-policy :result-policy})
(def ^:private lookup-attribute-keys
  #{:keyword-child-index :map-child-index :keyword
    :evaluation-order :dispatch :missing-key-policy
    :result-policy :target-type-legality})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B15 public adapter is absent"
        {:id "SH07-B16-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- sha256-id
  [bytes]
  (let [digest
        (.digest
         (java.security.MessageDigest/getInstance "SHA-256")
         bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- artifact-id-from-identity
  [artifact]
  ((required-var 'reader-canonical-hash)
   {:domain :gravity/sh07-declared-digest-v1
    :purpose :sh07-core-artifact-id
    :preimage (identity-input artifact)}))

(def ^:private artifacts
  ;; This delay is process-local test reuse only. It is never persisted and is
  ;; not authoritative proof; each focused JVM constructs the cohort afresh.
  (delay
    (into
     {}
     (map
      (fn [{:keys [module path]}]
        [module
         ((required-var 'sh07-core-file-artifact)
          (str (.resolve @root path)))])
      cohort))))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B16 records are not uniquely identifiable"
        {:id "SH07-B16-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- verification-failures
  [altered expected upstream-verification]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected upstream-verification)
         :when (not (true? passed?))]
     check)))

(defn- update-core
  [artifact key update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact key]
   update-function))

(defn- assert-source-origin
  [node]
  (is (= #{:syntax-id :form-id :semantic-span
           :origin-chain :generated-origin}
         (set (keys (:source node)))))
  (is (string? (get-in node [:source :syntax-id])))
  (is (string? (get-in node [:source :form-id])))
  (is (map? (get-in node [:source :semantic-span])))
  (is (vector? (get-in node [:source :origin-chain])))
  (is (vector? (get-in node [:source :generated-origin]))))

(defn- assert-lookup-products
  [artifact expected-count]
  (let [core-artifact (core artifact)
        lookups (:keyword-lookups core-artifact)
        nodes (exactly-once-index (:nodes core-artifact) :node-id)
        lookup-node-ids
        (set
         (map :node-id
              (filter #(= :keyword-map-lookup (:core-form %))
                      (:nodes core-artifact))))
        call-node-ids (set (map :core-node-id (:calls core-artifact)))
        operator-node-ids
        (set (map :operator-node-id (:calls core-artifact)))
        reference-node-ids
        (set (map :core-node-id (:reference-uses core-artifact)))
        lookup-core-node-ids (set (map :core-node-id lookups))]
    (is (vector? lookups))
    (is (= expected-count (count lookups)))
    (is (= lookup-node-ids (set (map :core-node-id lookups))))
    (is (empty? (set/intersection
                 lookup-core-node-ids
                 reference-node-ids)))
    (is (= lookups (:keyword-lookups (identity-input artifact))))
    (doseq [lookup lookups]
      (let [lookup-node (get nodes (:core-node-id lookup))
            keyword-node (get nodes (:keyword-node-id lookup))
            map-node (get nodes (:map-node-id lookup))
            expected-order
            [(:keyword-node-id lookup) (:map-node-id lookup)]]
        (is (= lookup-record-keys (set (keys lookup))))
        (is (keyword? (:keyword lookup)))
        (is (= expected-order (:ordered-evaluation-node-ids lookup)))
        (is (= :keyword-then-map (:evaluation-order lookup)))
        (is (= :nil (:missing-key-policy lookup)))
        (is (= :map-value-or-nil (:result-policy lookup)))
        (is (= :keyword-map-lookup (:core-form lookup-node)))
        (is (= expected-order (:children lookup-node)))
        (is (= [] (:resolved-binding-ids lookup-node)))
        (is (= lookup-attribute-keys
               (set (keys (:attributes lookup-node)))))
        (is (= 0 (get-in lookup-node
                         [:attributes :keyword-child-index])))
        (is (= 1 (get-in lookup-node
                         [:attributes :map-child-index])))
        (is (= (:keyword lookup)
               (get-in lookup-node [:attributes :keyword])))
        (is (= :keyword-then-map
               (get-in lookup-node [:attributes :evaluation-order])))
        (is (= :keyword-map-lookup
               (get-in lookup-node [:attributes :dispatch])))
        (is (= :nil
               (get-in lookup-node [:attributes :missing-key-policy])))
        (is (= :map-value-or-nil
               (get-in lookup-node [:attributes :result-policy])))
        (is (= :pending-sh08
               (get-in lookup-node
                       [:attributes :target-type-legality])))
        (is (= :keyword-then-map
               (get-in lookup-node [:evaluation :kind])))
        (is (= expected-order
               (mapv :core-node-id
                     (get-in lookup-node [:evaluation :order]))))
        (is (= (:keyword lookup)
               (get-in keyword-node [:attributes :value])))
        (is (map? map-node))
        (is (not (contains? call-node-ids (:core-node-id lookup))))
        (is (not (contains? operator-node-ids
                            (:keyword-node-id lookup))))
        (is (not (contains? reference-node-ids
                            (:keyword-node-id lookup))))
        (is (not (contains? (:attributes lookup-node) :binding-id)))
        (is (not (contains? (:attributes lookup-node)
                            :operator-binding-id)))
        (assert-source-origin lookup-node)
        (assert-source-origin keyword-node)
        (assert-source-origin map-node)))
    lookups))

(deftest sh07-b16-cohort-is-the-exact-nine-module-63-lookup-census
  (let [contract
        (edn/read-string
         (slurp
          (str
           (.resolve
            @root
            "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn"))))
        contract-counts
        (into {}
              (map (fn [[module counts]]
                     [module (:keyword-lookups counts)]))
              (select-keys
               (:required-core-product-counts contract)
               (keys expected-module-counts)))
        contract-paths
        (select-keys
         (:authoritative-modules contract)
         (keys expected-module-counts))]
    (is (= "SH-07-B26" (:coverage-milestone contract)))
    (is (= 9 (count cohort)))
    (is (= expected-module-counts
           (into {}
                 (map (juxt :module :keyword-lookup-count))
                 cohort)))
    (is (= expected-module-counts contract-counts))
    (is (= (into {} (map (juxt :module :path)) cohort)
           contract-paths))
    (is (= expected-lookup-total
           (reduce + (map :keyword-lookup-count cohort))))
    (is (= 9 (count (set (map :path cohort)))))
    (doseq [{:keys [module path keyword-lookup-count]} cohort]
      (testing (name module)
        (is (.isFile (io/file (str (.resolve @root path)))))
        (is (nat-int? keyword-lookup-count))))))

(deftest sh07-b16-real-modules-produce-exact-distinct-keyword-lookups
  (doseq [{:keys [module keyword-lookup-count]} cohort]
    (testing (name module)
      (let [artifact (get @artifacts module)
            authenticated-request (request artifact)
            proof (:capability-based-proof artifact)]
        (is (= :accepted (:status artifact)))
        (is (= "SH-07-B15" (:task artifact)))
        (is (= 15 (:schema-version authenticated-request)))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope authenticated-request)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:keyword-lookups-replay? proof)))
        (is (true? (:canonical-core-replays? proof)))
        (assert-lookup-products artifact keyword-lookup-count))))
  (is (= expected-lookup-total
         (reduce
          +
          (map
           #(count (:keyword-lookups (core %)))
           (vals @artifacts))))))

(deftest sh07-b16-cohort-identities-are-deterministically-source-bound
  (doseq [{:keys [module path]} cohort]
    (testing (name module)
      (let [source-path (str (.resolve @root path))
            source-revision-id (sha256-id (source-bytes source-path))
            artifact (get @artifacts module)
            authenticated-request (request artifact)
            core-artifact (core artifact)]
        (is (= (:artifact-id artifact)
               (:artifact-id core-artifact)
               (artifact-id-from-identity artifact)))
        (is (= source-revision-id
               (get-in authenticated-request
                       [:module :source-revision-id])
               (get-in authenticated-request
                       [:lineage :source-revision-id])))
        (is (= source-path
               (get-in artifact [:provenance :source-path])
               (get-in core-artifact
                       [:provenance :actual-source-path])))
        (is (= (:keyword-lookups core-artifact)
               (:keyword-lookups (identity-input artifact))))))))

(deftest sh07-b16-product-and-cross-module-substitution-fail-closed
  (let [left (get @artifacts :b5-jvm)
        right (get @artifacts :b6-javascript-typescript)
        upstream-verification
        ((required-var 'sh06-resolution-artifact-verification)
         (:sh06-resolution-artifact left))
        left-lookups (:keyword-lookups (core left))
        cases
        {"lookup omission"
         {:artifact
          (update-core left :keyword-lookups #(vec (rest %)))
          :expected
          #{:keyword-lookups-replay?
            :canonical-core-replays?
            :authoritative-products-replay?}}

         "cross-module lookup substitution"
         {:artifact
          (assoc-in
           left
           [:gravity-core-boundary :canonical-core-artifact
            :keyword-lookups]
           (:keyword-lookups (core right)))
          :expected
          #{:keyword-lookups-replay?
            :canonical-core-replays?
            :authoritative-products-replay?}}

         "semantic artifact identity substitution"
         {:artifact
          (assoc left :artifact-id (:artifact-id right))
          :expected #{:semantic-artifact-id-current?
                      :authoritative-products-replay?}}

         "actual source provenance substitution"
         {:artifact
          (assoc-in
           left
           [:gravity-core-boundary :canonical-core-artifact
            :provenance :actual-source-path]
           "/substituted/cohort/module.gravity")
          :expected
         #{:canonical-core-replays?
            :provenance-retained?
            :authoritative-products-replay?}}}]
    (is (= :passed (:status upstream-verification)))
    (is (= 9 (count left-lookups)))
    (is (= 8 (count (:keyword-lookups (core right)))))
    (doseq [[label {:keys [artifact expected]}] cases]
      (testing label
        (let [failed
              (verification-failures
               artifact left upstream-verification)]
          (is (seq failed))
          (is (every? failed expected)))))))
