(ns gravity.self-hosting.sh07-lexical-binding-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_lexical_binding_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B3 test source is not on the classpath"
                      {:id "SH07-B3-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B3-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b3")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["zero-binding-let" "one-binding-let"
   "sequential-binding-visibility" "shadowed-binding"
   "ordered-multi-form-body" "let-in-call-control"])
(def ^:private rejected-oracles
  {"odd-let-bindings"
   {:rule "C6-CORE-SHAPE"
    :reason :let-bindings-even-required
    :remediation
    "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape."}
   "non-symbol-let-binding"
   {:rule "C6-CORE-SHAPE"
    :reason :let-binding-symbol-required
    :remediation
    "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape."}
   "let-destructuring"
   {:rule "C6-LOWERING-GAP"
    :reason :let-destructuring-deferred
    :remediation
    "Use only the declared bounded SH-07 core subset; defer unsupported lowering families to their owning slices."}
   "empty-let-body"
   {:rule "C6-CORE-SHAPE"
    :reason :let-body-required
    :remediation
    "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape."}})
(def ^:private maximum-lexical-binding-records 1024)
(def ^:private lexical-binding-keys
  #{:let-core-node-id :ordinal :name :binding-id
    :definition-form-id :definition-syntax-id :binding-scope-id
    :initializer-form-id :initializer-syntax-id
    :initializer-scope-id :initializer-node-id
    :visible-prior-binding-ids :mutability})
(def ^:private let-attribute-keys
  #{:binding-vector-form-id :binding-vector-syntax-id
    :outer-scope-id :body-scope-id :binding-count :body-count
    :initializer-child-indexes :body-child-indexes
    :evaluation-order :result-policy})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(str/ends-with? % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

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
        "Required SH-07-B3 coordinator adapter is absent"
        {:id "SH07-B3-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-capability-based-proof '[artifact]
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
        ns-form (first (:parsed-semantic-values artifact))
        clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 ns-form))]
    (get (second clause) :sh07-b3)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- nodes
  [artifact]
  (:nodes (core artifact)))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info "SH-07-B3 records are not uniquely identifiable"
                {:id "SH07-B3-AMBIGUOUS-INDEX"
                 :key key-name
                 :record-count (count records)
                 :unique-count (count index)})))
    index))

(defn- node-index
  [artifact]
  (exactly-once-index (nodes artifact) :node-id))

(defn- form-index
  [artifact]
  (exactly-once-index (:forms (request artifact)) :form-id))

(defn- binding-index
  [artifact]
  (exactly-once-index (:binding-table (request artifact)) :binding-id))

(defn- definition
  [artifact name]
  (let [matches
        (filterv #(= name (str (:name %))) (:definitions (core artifact)))]
    (when-not (= 1 (count matches))
      (throw
       (ex-info "SH-07-B3 definition is not uniquely identifiable"
                {:id "SH07-B3-AMBIGUOUS-DEFINITION"
                 :name name :matches (count matches)})))
    (first matches)))

(defn- definition-value-node
  [artifact name]
  (get (node-index artifact)
       (:value-node-id (definition artifact name))))

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

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error {:class (.getName (class throwable))
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

(defn- update-core-records
  [artifact table update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact table]
   update-function))

(defn- update-node
  [artifact node-id update-function]
  (update-core-records
   artifact :nodes
   (fn [records]
     (mapv (fn [node]
             (if (= node-id (:node-id node))
               (update-function node)
               node))
           records))))

(defn- update-lexical-record
  [artifact binding-id update-function]
  (update-core-records
   artifact :lexical-bindings
   (fn [records]
     (mapv (fn [record]
             (if (= binding-id (:binding-id record))
               (update-function record)
               record))
           records))))

(defn- let-records
  [artifact let-node-id]
  (->> (:lexical-bindings (core artifact))
       (filter #(= let-node-id (:let-core-node-id %)))
       (sort-by :ordinal)
       vec))

(defn- let-body-node-ids
  [let-node]
  (let [children (:children let-node)]
    (mapv #(nth children %)
          (get-in let-node [:attributes :body-child-indexes]))))

(defn- assert-let-and-lexical-tables
  [artifact]
  (let [core-artifact (core artifact)
        lexical (:lexical-bindings core-artifact)
        index (node-index artifact)
        forms (form-index artifact)
        bindings (binding-index artifact)
        let-nodes (filterv #(= :let (:core-form %)) (nodes artifact))
        declared-maximum
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :bounds :maximum-lexical-binding-records])]
    (is (= 7 (:schema-version (request artifact))))
    (is (= :sh07-b6-meta-jvm-core (:scope (request artifact))))
    (is (vector? lexical))
    (is (= maximum-lexical-binding-records declared-maximum))
    (is (<= (count lexical) declared-maximum))
    (is (= lexical (:lexical-bindings (identity-input artifact))))
    (is (= (mapv #(select-keys % [:ordinal :name :mutability]) lexical)
           (mapv #(select-keys % [:ordinal :name :mutability])
                 (get-in artifact
                         [:gravity-core-boundary :raw-template-result
                          :core-template :lexical-bindings]))))
    (doseq [record lexical]
      (let [let-node (get index (:let-core-node-id record))
            initializer (get index (:initializer-node-id record))
            definition-form (get forms (:definition-form-id record))
            initializer-form (get forms (:initializer-form-id record))
            binding (get bindings (:binding-id record))]
        (is (= lexical-binding-keys (set (keys record))))
        (is (= :let (:core-form let-node)))
        (is (map? initializer))
        (is (= :symbol (:kind definition-form)))
        (is (= (:name record) (:value definition-form)))
        (is (= (:definition-syntax-id record)
               (:syntax-id definition-form)))
        (is (= (:initializer-syntax-id record)
               (:syntax-id initializer-form)))
        (is (= (:initializer-form-id record)
               (get-in initializer [:source :form-id])))
        (is (= (:initializer-syntax-id record)
               (get-in initializer [:source :syntax-id])))
        (is (= :immutable (:mutability record)))
        (is (map? binding))
        (is (= (:name binding) (:name record)))
        (is (= (:definition-syntax-id binding)
               (:definition-syntax-id record)))
        (is (= (:scope-id binding) (:binding-scope-id record)))
        (assert-source-origin initializer)))
    (doseq [let-node let-nodes]
      (let [attributes (:attributes let-node)
            records (let-records artifact (:node-id let-node))
            binding-ids (mapv :binding-id records)
            initializer-ids (mapv :initializer-node-id records)
            binding-count (count records)
            body-ids (let-body-node-ids let-node)
            body-count (count body-ids)
            expected-children (into initializer-ids body-ids)]
        (is (= let-attribute-keys (set (keys attributes))))
        (is (= binding-count (:binding-count attributes)))
        (is (= body-count (:body-count attributes)))
        (is (= (vec (range binding-count))
               (:initializer-child-indexes attributes)))
        (is (= (vec (range binding-count
                           (+ binding-count body-count)))
               (:body-child-indexes attributes)))
        (is (= :initializers-then-body-left-to-right
               (:evaluation-order attributes)))
        (is (= {:kind :last-body
                :child-index (dec (+ binding-count body-count))}
               (:result-policy attributes)))
        (is (= expected-children (:children let-node)))
        (is (= binding-ids (:resolved-binding-ids let-node)))
        (is (= :initializers-then-body-left-to-right
               (get-in let-node [:evaluation :kind])))
        (is (= expected-children
               (mapv :core-node-id
                     (get-in let-node [:evaluation :order]))))
        (is (pos? body-count))
        (if (empty? records)
          (is (= (:outer-scope-id attributes)
                 (:body-scope-id attributes)))
          (is (= (:binding-scope-id (last records))
                 (:body-scope-id attributes))))
        (doseq [ordinal (range binding-count)]
          (let [record (nth records ordinal)]
            (is (= ordinal (:ordinal record)))
            (is (= (subvec binding-ids 0 ordinal)
                   (:visible-prior-binding-ids record)))
            (is (= (if (zero? ordinal)
                     (:outer-scope-id attributes)
                     (:binding-scope-id (nth records (dec ordinal))))
                   (:initializer-scope-id record)))))
        (assert-source-origin let-node)))
    {:lexical-bindings lexical :let-nodes let-nodes}))

(deftest sh07-b3-fixtures-are-complete-paired-and-path-neutral
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-oracles)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames (keys rejected-oracles))]
    (is (= (seq (source-bytes (fixture-path family basename ".gravity")))
           (seq (source-bytes (fixture-path family basename ".qst"))))))
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:lexical-bindings (core gravity))
               (:lexical-bindings (core qst))))
        (is (not= (get-in gravity [:provenance :source-path])
                  (get-in qst [:provenance :source-path]))))))
  (let [fixture (fixture-path "accepted" "one-binding-let" ".gravity")
        text (source-text fixture)
        left-path "/tmp/sh07-b3-left/let.gravity"
        right-path "/tmp/sh07-b3-right/let.qst"
        left (source-artifact left-path text)
        right (source-artifact right-path text)]
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= left-path (get-in left [:provenance :source-path])))
    (is (= right-path (get-in right [:provenance :source-path])))
    (is (= left-path
           (get-in left
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))
    (is (= right-path
           (get-in right
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))))

(deftest sh07-b3-let-products-preserve-sequential-scope
  (doseq [basename accepted-basenames
          extension extensions]
    (testing (str basename extension)
      (let [artifact (file-artifact "accepted" basename extension)
            tables (assert-let-and-lexical-tables artifact)]
        (is (seq (:let-nodes tables)))
        (when (= basename "zero-binding-let")
          (is (empty? (:lexical-bindings tables))))))))

(deftest sh07-b3-sequential-shadow-body-and-nesting-are-explicit
  (let [sequential
        (file-artifact
         "accepted" "sequential-binding-visibility" ".gravity")
        sequential-let
        (definition-value-node sequential "sequential-values")
        records (let-records sequential (:node-id sequential-let))
        index (node-index sequential)
        second-initializer (get index (:initializer-node-id (nth records 1)))
        third-initializer (get index (:initializer-node-id (nth records 2)))
        shadow
        (file-artifact "accepted" "shadowed-binding" ".gravity")
        outer-let (definition-value-node shadow "shadowed-value")
        outer-record (first (let-records shadow (:node-id outer-let)))
        shadow-index (node-index shadow)
        inner-let (get shadow-index (first (let-body-node-ids outer-let)))
        inner-record (first (let-records shadow (:node-id inner-let)))
        inner-initializer
        (get shadow-index (:initializer-node-id inner-record))
        inner-body
        (get shadow-index
             (first (let-body-node-ids inner-let)))
        multi
        (file-artifact "accepted" "ordered-multi-form-body" ".gravity")
        multi-let (definition-value-node multi "ordered-body")
        multi-index (node-index multi)
        body-nodes
        (mapv multi-index (let-body-node-ids multi-let))
        nested
        (file-artifact "accepted" "let-in-call-control" ".gravity")
        call-node (definition-value-node nested "nested-let")
        nested-index (node-index nested)
        if-node (get nested-index (second (:children call-node)))
        branch-nodes (mapv nested-index (subvec (:children if-node) 1))]
    (is (= 3 (count records)))
    (is (= (:binding-id (first records))
           (get-in second-initializer [:attributes :binding-id])))
    (is (= (:binding-id (second records))
           (get-in third-initializer [:attributes :binding-id])))
    (is (= (:name outer-record) (:name inner-record) 'value))
    (is (not= (:binding-id outer-record) (:binding-id inner-record)))
    (is (not= (:binding-scope-id outer-record)
              (:binding-scope-id inner-record)))
    (is (= (:binding-id outer-record)
           (get-in inner-initializer [:attributes :binding-id])))
    (is (= (:binding-id inner-record)
           (get-in inner-body [:attributes :binding-id])))
    (is (= [:reference :literal :reference]
           (mapv :core-form body-nodes)))
    (is (= (:node-id (last body-nodes))
           (last (:children multi-let))))
    (is (= :call (:core-form call-node)))
    (is (= :if (:core-form if-node)))
    (is (= [:let :let] (mapv :core-form branch-nodes)))))

(deftest sh07-b3-public-replay-and-capability-proof-pass
  (doseq [basename accepted-basenames]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          report
          ((required-var 'sh07-core-artifact-verification) artifact)
          proof
          ((required-var 'sh07-core-capability-based-proof) artifact)]
      (testing basename
        (is (= :passed (:status report)))
        (is (= [] (:failed-checks report)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))))))

(deftest sh07-b3-rejections-are-structured-and-oracle-bound
  (doseq [[basename oracle] rejected-oracles
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= {:expected-rule (:rule oracle)
                :expected-stage :core-lowering
                :expected-severity :error
                :expected-reason (:reason oracle)
                :expected-remediation (:remediation oracle)}
               declared))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= (:rule oracle) (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= (:reason oracle) (get-in diagnostic [:facts :reason])))
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b3-lexical-scope-and-order-mutations-fail-replay
  (let [artifact
        (file-artifact
         "accepted" "sequential-binding-visibility" ".gravity")
        lexical (:lexical-bindings (core artifact))
        target (second lexical)
        let-node (get (node-index artifact) (:let-core-node-id target))
        alternate-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        reversed (vec (reverse lexical))
        mutations
        {"lexical omission"
         (update-core-records
          artifact :lexical-bindings
          #(vec (remove
                 (fn [record]
                   (= (:binding-id target) (:binding-id record)))
                 %)))
         "lexical duplication"
         (update-core-records artifact :lexical-bindings #(conj % target))
         "lexical reorder"
         (update-core-records artifact :lexical-bindings
                              (constantly reversed))
         "initializer substitution"
         (update-lexical-record
          artifact (:binding-id target)
          #(assoc % :initializer-node-id alternate-id))
         "binding scope substitution"
         (update-lexical-record
          artifact (:binding-id target)
          #(assoc % :binding-scope-id alternate-id))
         "visible-prior substitution"
         (update-lexical-record
          artifact (:binding-id target)
          #(assoc % :visible-prior-binding-ids []))
         "let evaluation reorder"
         (update-node
          artifact (:node-id let-node)
          #(-> %
               (update :children (fn [values] (vec (reverse values))))
               (assoc-in [:evaluation :kind] :body-before-bindings)))
         "result policy substitution"
         (update-node
          artifact (:node-id let-node)
          #(assoc-in % [:attributes :result-policy :child-index] 0))}]
    (is (map? target))
    (is (map? let-node))
    (is (not= lexical reversed))
    (doseq [[label mutation] mutations]
      (testing label
        (is (not= artifact mutation))
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               mutation artifact {:status :passed})
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :authoritative-products-replay?)))))
    (let [report
          ((required-var 'sh07-core-artifact-verification)
           (get mutations "binding scope substitution"))]
      (is (= :failed (:status report)))
      (is (some #{:canonical-core-replays?}
                (:failed-checks report))))))
