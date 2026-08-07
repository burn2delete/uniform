(ns gravity.self-hosting.sh07-keyword-map-lookup-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_keyword_map_lookup_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B15 test source is not on the classpath"
        {:id "SH07-B15-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B15-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b15")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-fixtures
  #{"basic-keyword-map-lookup"
    "multiple-source-ordered-keyword-lookups"
    "nested-keyword-map-lookup"})
(def ^:private rejected-fixtures
  #{"keyword-lookup-default-value-deferred"
    "keyword-lookup-over-arity"
    "keyword-lookup-zero-arity"})
(def ^:private expected-lookup-counts
  {"basic-keyword-map-lookup" 1
   "multiple-source-ordered-keyword-lookups" 3
   "nested-keyword-map-lookup" 3})
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

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (if-not (.isDirectory directory)
      #{}
      (->> (.listFiles directory)
           (filter #(.isFile %))
           (map #(.getName %))
           (filter #(str/ends-with? % extension))
           (map #(subs % 0 (- (count %) (count extension))))
           set))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-text
  [source-path]
  (String. (source-bytes source-path)
           java.nio.charset.StandardCharsets/UTF_8))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B15 coordinator adapter is absent"
        {:id "SH07-B15-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]}}))))

(def ^:private artifacts (atom {}))
(def ^:private c2-artifacts (atom {}))

(defn- file-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @artifacts key)
        (let [artifact
              ((required-var 'sh07-core-file-artifact)
               (fixture-path family basename extension))]
          (swap! artifacts assoc key artifact)
          artifact))))

(defn- source-artifact
  [source-path text]
  ((required-var 'sh07-core-source-artifact) source-path text))

(defn- c2-artifact
  [basename extension]
  (let [key [basename extension]]
    (or (get @c2-artifacts key)
        (let [artifact
              (bootstrap/compiler-c2-reader-file-artifact
               (fixture-path "rejected" basename extension))]
          (swap! c2-artifacts assoc key artifact)
          artifact))))

(defn- fixture-oracle
  [basename extension]
  (let [artifact (c2-artifact basename extension)
        namespace-form (first (:parsed-semantic-values artifact))
        metadata-clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 namespace-form))]
    (get (second metadata-clause) :sh07-b15)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B15 records are not uniquely identifiable"
        {:id "SH07-B15-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error
       {:class (.getName (class throwable))
        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(defn- verification-failures
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(defn- update-core
  [artifact key update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact key]
   update-function))

(defn- update-lookup
  [artifact core-node-id update-function]
  (update-core
   artifact :keyword-lookups
   (fn [records]
     (mapv
      (fn [record]
        (if (= core-node-id (:core-node-id record))
          (update-function record)
          record))
      records))))

(defn- update-node
  [artifact node-id update-function]
  (update-core
   artifact :nodes
   (fn [nodes]
     (mapv
      (fn [node]
        (if (= node-id (:node-id node))
          (update-function node)
          node))
      nodes))))

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

(defn- assert-keyword-lookup-products
  [artifact expected-count]
  (let [core-artifact (core artifact)
        lookups (:keyword-lookups core-artifact)
        nodes (exactly-once-index (:nodes core-artifact) :node-id)
        calls (:calls core-artifact)
        reference-uses (:reference-uses core-artifact)
        lookup-node-ids
        (set
         (map :node-id
              (filter #(= :keyword-map-lookup (:core-form %))
                      (:nodes core-artifact))))
        call-node-ids (set (map :core-node-id calls))
        operator-node-ids (set (map :operator-node-id calls))
        reference-node-ids (set (map :core-node-id reference-uses))]
    (is (vector? lookups))
    (is (= expected-count (count lookups)))
    (is (= lookup-node-ids (set (map :core-node-id lookups))))
    (is (= lookups (:keyword-lookups (identity-input artifact))))
    (doseq [lookup lookups]
      (let [lookup-node (get nodes (:core-node-id lookup))
            keyword-node (get nodes (:keyword-node-id lookup))
            map-node (get nodes (:map-node-id lookup))
            expected-order [(:keyword-node-id lookup)
                            (:map-node-id lookup)]]
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
        (is (= {:keyword-child-index 0
                :map-child-index 1
                :keyword (:keyword lookup)
                :evaluation-order :keyword-then-map
                :dispatch :keyword-map-lookup
                :missing-key-policy :nil
                :result-policy :map-value-or-nil
                :target-type-legality :pending-sh08}
               (:attributes lookup-node)))
        (is (= :keyword-then-map
               (get-in lookup-node [:evaluation :kind])))
        (is (= expected-order
               (mapv :core-node-id
                     (get-in lookup-node [:evaluation :order]))))
        (is (= 2 (count (distinct expected-order))))
        (is (= (:keyword lookup)
               (get-in keyword-node [:attributes :value])))
        (is (map? map-node))
        (is (not (contains? call-node-ids (:core-node-id lookup))))
        (is (not (contains? operator-node-ids (:keyword-node-id lookup))))
        (is (not (contains? reference-node-ids (:keyword-node-id lookup))))
        (is (not (contains? (:attributes lookup-node) :binding-id)))
        (is (not (contains? (:attributes lookup-node)
                            :operator-binding-id)))
        (assert-source-origin lookup-node)
        (assert-source-origin keyword-node)
        (assert-source-origin map-node)))
    lookups))

(deftest sh07-b15-fixtures-are-complete-paired-and-byte-identical
  (doseq [extension extensions]
    (is (= accepted-fixtures
           (fixture-basenames "accepted" extension)))
    (is (= rejected-fixtures
           (fixture-basenames "rejected" extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-fixtures
                     rejected-fixtures)]
    (testing (str family "/" basename)
      (is (= (seq (source-bytes
                   (fixture-path family basename ".gravity")))
             (seq (source-bytes
                   (fixture-path family basename ".qst"))))))))

(deftest sh07-b15-accepted-lookups-are-distinct-ordered-core-products
  (doseq [basename (sort accepted-fixtures)
          extension extensions]
    (testing (str basename extension)
      (let [artifact (file-artifact "accepted" basename extension)
            authenticated-request (request artifact)]
        (is (= :accepted (:status artifact)))
        (is (= 15 (:schema-version authenticated-request)))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope authenticated-request)))
        (is (= "SH-07-B47" (:task artifact)))
        (is (= :gravity/sh07-to-c6-core-products-v16
               @(required-var 'sh07-core-adapter-contract)
               (get-in artifact
                       [:gravity-core-boundary :adapter-contract])))
        (assert-keyword-lookup-products
         artifact
         (get expected-lookup-counts basename))))))

(deftest sh07-b15-identities-are-deterministic-path-neutral-and-provenanced
  (doseq [basename (sort accepted-fixtures)]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          repeated (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= gravity repeated))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:keyword-lookups (core gravity))
               (:keyword-lookups (core qst))))
        (is (not= (get-in gravity [:provenance :source-path])
                  (get-in qst [:provenance :source-path]))))))
  (let [fixture
        (fixture-path "accepted" "nested-keyword-map-lookup" ".gravity")
        text (source-text fixture)
        left-path "/tmp/sh07-b15-left/keyword-lookup.gravity"
        right-path "/tmp/sh07-b15-right/keyword-lookup.qst"
        left (source-artifact left-path text)
        right (source-artifact right-path text)]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (:keyword-lookups (core left))
           (:keyword-lookups (core right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))))

(deftest sh07-b15-rejected-arities-use-the-normative-shape-diagnostic
  (doseq [basename (sort rejected-fixtures)
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            oracle (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (map? oracle))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= "C6-CORE-SHAPE"
               (:expected-rule oracle)
               (:rule diagnostic)))
        (is (= :core-lowering
               (:expected-stage oracle)
               (:stage diagnostic)))
        (is (= :error
               (:expected-severity oracle)
               (:severity diagnostic)))
        (is (= :keyword-map-lookup-arity
               (:expected-reason oracle)
               (get-in diagnostic [:facts :reason])))
        (is (= (:expected-actual-operand-count oracle)
               (get-in diagnostic
                       [:facts :rule-specific
                        :actual-operand-count])))
        (is (= (:expected-required-operand-count oracle)
               (get-in diagnostic
                       [:facts :rule-specific
                        :required-operand-count])))
        (is (= 1
               (get-in diagnostic
                       [:facts :rule-specific
                        :required-operand-count])))
        (is (= (:expected-remediation oracle)
               (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b15-lookup-product-alterations-fail-public-replay
  (let [artifact
        (file-artifact
         "accepted" "multiple-source-ordered-keyword-lookups" ".gravity")
        lookups (:keyword-lookups (core artifact))
        target (first lookups)
        target-id (:core-node-id target)
        keyword-id (:keyword-node-id target)
        alternate-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        alterations
        {"lookup omission"
         (update-core artifact :keyword-lookups
                      #(vec (remove
                             (fn [record]
                               (= target-id (:core-node-id record)))
                             %)))

         "lookup duplication"
         (update-core artifact :keyword-lookups #(conj % target))

         "keyword substitution"
         (update-lookup artifact target-id #(assoc % :keyword :changed))

         "lookup node id substitution"
         (update-lookup artifact target-id
                        #(assoc % :core-node-id alternate-id))

         "keyword node id substitution"
         (update-lookup artifact target-id
                        #(assoc % :keyword-node-id alternate-id))

         "evaluation order"
         (update-lookup
          artifact target-id
          #(assoc %
                  :ordered-evaluation-node-ids
                  (vec (reverse (:ordered-evaluation-node-ids %)))))

         "evaluation policy"
         (update-lookup artifact target-id
                        #(assoc % :evaluation-order :map-then-keyword))

         "missing-key policy"
         (update-lookup artifact target-id
                        #(assoc % :missing-key-policy :error))

         "result policy"
         (update-lookup artifact target-id
                        #(assoc % :result-policy :map-value))

         "node child order"
         (update-node artifact target-id
                      #(update % :children (comp vec reverse)))

         "node keyword value"
         (update-node artifact keyword-id
                      #(assoc-in % [:attributes :value] :changed))}]
    (is (< 1 (count lookups)))
    (is (map? target))
    (doseq [[label altered] alterations]
      (testing label
        (is (not= artifact altered))
        (let [failed (verification-failures altered artifact)]
          (when-not (contains? #{"node child order"
                                "node keyword value"}
                              label)
            (is (contains? failed :keyword-lookups-replay?)))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :authoritative-products-replay?)))))
    (let [report
          ((required-var 'sh07-core-artifact-verification)
           (get alterations "lookup omission"))]
      (is (= :failed (:status report)))
      (is (some #{:keyword-lookups-replay?}
                (:failed-checks report))))))
