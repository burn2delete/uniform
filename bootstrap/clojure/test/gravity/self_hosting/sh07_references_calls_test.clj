(ns gravity.self-hosting.sh07-references-calls-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_references_calls_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B2 test source is not on the classpath"
                      {:id "SH07-B2-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B2-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b2")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["parameter-and-namespace-references"
   "direct-symbol-call"
   "qualified-symbol-call"
   "higher-order-lexical-call"
   "call-evaluation-order"
   "operator-argument-same-symbol"
   "keyword-headed-call"
   "required-primitive-call-arities"])
(def ^:private rejected-oracles
  {"empty-call"
   {:rule "C6-CORE-SHAPE"
    :reason :call-operator-required
    :remediation
    "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape."}})
(def ^:private maximum-reference-use-records 1024)
(def ^:private maximum-call-records 1024)
(def ^:private reference-use-keys
  #{:core-node-id :form-id :syntax-id :symbol :position
    :binding-id :binding-class :definition-syntax-id})
(def ^:private reference-attribute-keys
  #{:symbol :position :binding-id :binding-class
    :definition-syntax-id})
(def ^:private call-record-keys
  #{:core-node-id :operator-node-id :operator-binding-id
    :argument-node-ids :ordered-evaluation-node-ids
    :evaluation-order :result-policy})
(def ^:private call-attribute-keys
  #{:operator-child-index :argument-count :argument-child-indexes
    :evaluation-order :dispatch})

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
        "Required SH-07-B2 coordinator adapter is absent"
        {:id "SH07-B2-ADAPTER-ABSENT"
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
        metadata-clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 ns-form))]
    (get (second metadata-clause) :sh07-b2)))

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
       (ex-info "SH-07-B2 records are not uniquely identifiable"
                {:id "SH07-B2-AMBIGUOUS-INDEX"
                 :key key-name
                 :record-count (count records)
                 :unique-count (count index)})))
    index))

(defn- node-index
  [artifact]
  (exactly-once-index (nodes artifact) :node-id))

(defn- reference-use-index
  [artifact]
  (exactly-once-index (:reference-uses (core artifact)) :core-node-id))

(defn- call-index
  [artifact]
  (exactly-once-index (:calls (core artifact)) :core-node-id))

(defn- definition
  [artifact name]
  (let [matches
        (filterv #(= name (str (:name %))) (:definitions (core artifact)))]
    (when-not (= 1 (count matches))
      (throw
       (ex-info "SH-07-B2 definition is not uniquely identifiable"
                {:id "SH07-B2-AMBIGUOUS-DEFINITION"
                 :name name
                 :matches (count matches)})))
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

(defn- update-reference-use
  [artifact node-id update-function]
  (update-core-records
   artifact :reference-uses
   (fn [records]
     (mapv (fn [record]
             (if (= node-id (:core-node-id record))
               (update-function record)
               record))
           records))))

(defn- update-call
  [artifact node-id update-function]
  (update-core-records
   artifact :calls
   (fn [records]
     (mapv (fn [record]
             (if (= node-id (:core-node-id record))
               (update-function record)
               record))
           records))))

(defn- assert-reference-and-call-tables
  [artifact]
  (let [core-artifact (core artifact)
        reference-uses (:reference-uses core-artifact)
        calls (:calls core-artifact)
        index (node-index artifact)
        node-ids (set (keys index))
        bindings
        (exactly-once-index (:binding-table (request artifact))
                            :binding-id)
        resolutions (:resolution-table (request artifact))
        resolution-keys
        (set (map (juxt :reference-syntax-id
                        :symbol :position :binding-id)
                  resolutions))
        reference-node-ids
        (set (map :node-id
                  (filter #(= :reference (:core-form %))
                          (nodes artifact))))
        call-node-ids
        (set (map :node-id
                  (filter #(= :call (:core-form %))
                          (nodes artifact))))
        declared-reference-maximum
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :bounds :maximum-reference-use-records])
        declared-call-maximum
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :bounds :maximum-call-records])]
    (is (= 15 (:schema-version (request artifact))))
    (is (= :sh07-b15-keyword-map-lookup (:scope (request artifact))))
    (is (vector? reference-uses))
    (is (vector? calls))
    (is (= maximum-reference-use-records declared-reference-maximum))
    (is (= maximum-call-records declared-call-maximum))
    (is (<= 1 (count reference-uses) declared-reference-maximum))
    (is (<= (count calls) declared-call-maximum))
    (is (= reference-node-ids
           (set (map :core-node-id reference-uses))))
    (is (= call-node-ids
           (set (map :core-node-id calls))))
    (is (= reference-uses (:reference-uses (identity-input artifact))))
    (is (= calls (:calls (identity-input artifact))))
    (is (= (mapv :symbol reference-uses)
           (mapv :symbol
                 (get-in artifact
                         [:gravity-core-boundary
                          :raw-template-result :core-template
                          :reference-uses]))))
    (is (= (mapv :evaluation-order calls)
           (mapv :evaluation-order
                 (get-in artifact
                         [:gravity-core-boundary
                          :raw-template-result :core-template :calls]))))
    (doseq [use reference-uses]
      (let [node (get index (:core-node-id use))
            binding (get bindings (:binding-id use))]
        (is (= reference-use-keys (set (keys use))))
        (is (= :reference (:core-form node)))
        (is (= [] (:children node)))
        (is (= reference-attribute-keys
               (set (keys (:attributes node)))))
        (is (= (select-keys use reference-attribute-keys)
               (:attributes node)))
        (is (= [(:binding-id use)] (:resolved-binding-ids node)))
        (is (= :value (get-in node [:evaluation :kind])))
        (is (map? binding))
        (is (= (:binding-class binding) (:binding-class use)))
        (is (= (:definition-syntax-id binding)
               (:definition-syntax-id use)))
        (is (contains?
             resolution-keys
             [(:syntax-id use) (:symbol use)
              (:position use) (:binding-id use)]))
        (assert-source-origin node)))
    (doseq [call calls]
      (let [node (get index (:core-node-id call))
            operator-id (:operator-node-id call)
            arguments (:argument-node-ids call)
            expected-order (into [operator-id] arguments)]
        (is (= call-record-keys (set (keys call))))
        (is (= :call (:core-form node)))
        (is (= expected-order (:children node)))
        (is (= call-attribute-keys
               (set (keys (:attributes node)))))
        (is (= {:operator-child-index 0
                :argument-count (count arguments)
                :argument-child-indexes
                (vec (range 1 (inc (count arguments))))
                :evaluation-order :operator-then-arguments
                :dispatch :resolved-symbol-call}
               (:attributes node)))
        (is (= expected-order (:ordered-evaluation-node-ids call)))
        (is (= :operator-then-arguments (:evaluation-order call)))
        (is (= :call-result (:result-policy call)))
        (is (= (:operator-binding-id call)
               (get-in index [operator-id :attributes :binding-id])))
        (is (= [(:operator-binding-id call)]
               (:resolved-binding-ids node)))
        (is (= :operator-then-arguments
               (get-in node [:evaluation :kind])))
        (is (= expected-order
               (mapv :core-node-id
                     (get-in node [:evaluation :order]))))
        (is (every? node-ids expected-order))
        (assert-source-origin node)))
    {:reference-uses reference-uses
     :calls calls}))

(deftest sh07-b2-fixtures-are-paired-and-path-neutral
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-oracles)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames (keys rejected-oracles))]
    (testing (str family "/" basename)
      (is (= (seq (source-bytes (fixture-path family basename ".gravity")))
             (seq (source-bytes (fixture-path family basename ".qst")))))))
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:reference-uses (core gravity))
               (:reference-uses (core qst))))
        (is (= (:calls (core gravity)) (:calls (core qst))))
        (is (not=
             (get-in gravity [:provenance :source-path])
             (get-in qst [:provenance :source-path]))))))
  (let [fixture (fixture-path "accepted" "direct-symbol-call" ".gravity")
        text (source-text fixture)
        left (source-artifact "/tmp/sh07-b2-left/direct.gravity" text)
        right (source-artifact "/tmp/sh07-b2-right/direct.qst" text)]
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= "/tmp/sh07-b2-left/direct.gravity"
           (get-in left [:provenance :source-path])))
    (is (= "/tmp/sh07-b2-right/direct.qst"
           (get-in right [:provenance :source-path])))
    (is (= "/tmp/sh07-b2-left/direct.gravity"
           (get-in left
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))
    (is (= "/tmp/sh07-b2-right/direct.qst"
           (get-in right
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))))

(deftest sh07-b2-authentically-consumes-sh06-references
  (doseq [basename accepted-basenames
          extension extensions]
    (testing (str basename extension)
      (let [artifact (file-artifact "accepted" basename extension)]
        (if (= basename "keyword-headed-call")
          (do
            (is (empty? (:reference-uses (core artifact))))
            (is (empty? (:calls (core artifact)))))
          (let [tables (assert-reference-and-call-tables artifact)]
            (is (seq (:reference-uses tables)))
            (when (not= basename "parameter-and-namespace-references")
              (is (seq (:calls tables))))))))))

(deftest sh07-b2-reference-classes-and-call-order-are-explicit
  (let [references
        (file-artifact
         "accepted" "parameter-and-namespace-references" ".gravity")
        uses (:reference-uses (core references))
        parameter-use
        (first (filter #(= 'value (:symbol %)) uses))
        namespace-use
        (first (filter #(= 'global-value (:symbol %)) uses))
        order
        (file-artifact "accepted" "call-evaluation-order" ".gravity")
        index (node-index order)
        call-node (definition-value-node order "ordered-call")
        [operator first-argument second-argument third-argument]
        (mapv index (:children call-node))]
    (is (= :lexical (:binding-class parameter-use)))
    (is (= :namespace (:binding-class namespace-use)))
    (is (= [:reference :do :if :do]
           (mapv :core-form
                 [operator first-argument second-argument third-argument])))
    (is (= (:children call-node)
           (mapv :core-node-id
                 (get-in call-node [:evaluation :order]))))
    (is (= [2 4 6]
           [(get-in (get index (last (:children first-argument)))
                    [:attributes :value])
            (get-in (get index (nth (:children second-argument) 2))
                    [:attributes :value])
            (get-in (get index (last (:children third-argument)))
                    [:attributes :value])]))))

(deftest sh07-b2-equal-symbol-operator-and-argument-remain-distinct
  (doseq [extension extensions]
    (let [artifact
          (file-artifact
           "accepted" "operator-argument-same-symbol" extension)
          matching
          (filterv #(= 'f (:symbol %))
                   (:reference-uses (core artifact)))
          by-position (group-by :position matching)
          operator (first (get by-position :operator))
          expression (first (get by-position :expression))
          call (first (:calls (core artifact)))]
      (testing extension
        (is (= 2 (count matching)))
        (is (= 1 (count (get by-position :operator))))
        (is (= 1 (count (get by-position :expression))))
        (is (not= (:core-node-id operator)
                  (:core-node-id expression)))
        (is (not= (:syntax-id operator) (:syntax-id expression)))
        (is (= (:binding-id operator) (:binding-id expression)))
        (is (= (:core-node-id operator) (:operator-node-id call)))
        (is (= [(:core-node-id expression)]
               (:argument-node-ids call)))))))

(deftest sh07-b2-qualified-namespace-reference-retains-binding-identity
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "qualified-symbol-call" extension)
          qualified-symbol
          'self-hosting.sh07-b2.qualified-symbol-call/qualified-identity
          use (first (filter #(= qualified-symbol (:symbol %))
                             (:reference-uses (core artifact))))
          request (get-in artifact
                          [:gravity-core-boundary
                           :authenticated-core-request])
          resolution
          (first
           (filter #(= (:syntax-id use) (:reference-syntax-id %))
                   (:resolution-table request)))
          resolution-index
          (first
           (keep-indexed
            (fn [index candidate]
              (when (= (:syntax-id use)
                       (:reference-syntax-id candidate))
                index))
            (:resolution-table request)))
          binding-index
          (first
           (keep-indexed
            (fn [index binding]
              (when (= (:binding-id use) (:binding-id binding))
                index))
            (:binding-table request)))
          binding (get (:binding-table request) binding-index)
          changed-namespace
          (assoc-in
           artifact
           [:gravity-core-boundary :authenticated-core-request
            :binding-table binding-index :namespace]
           'self-hosting.sh07-b2.changed-namespace)
          changed-report
          ((required-var 'sh07-core-artifact-verification)
           changed-namespace)
          changed-request
          (assoc-in
           request [:binding-table binding-index :namespace]
           'self-hosting.sh07-b2.changed-namespace)
          changed-request-result
          (diagnostic-result
           #((required-var 'sh07-core-run-request-for-test)
             (:sh06-resolution-artifact artifact)
             changed-request))
          changed-request-diagnostic
          (diagnostic-data changed-request-result)
          alias-request
          (assoc-in
           request [:resolution-table resolution-index :resolution-order]
           :alias-qualified-required-binding)
          alias-result
          (diagnostic-result
           #((required-var 'sh07-core-run-request-for-test)
             (:sh06-resolution-artifact artifact)
             alias-request))
          alias-diagnostic (diagnostic-data alias-result)]
      (testing extension
        (is (= :accepted (:status artifact)))
        (is (= qualified-symbol (:symbol use)))
        (is (= :fully-qualified-namespace-binding
               (:resolution-order resolution)))
        (is (= 'qualified-identity (:name binding)))
        (is (= 'self-hosting.sh07-b2.qualified-symbol-call
               (:namespace binding)))
        (is (= (:binding-id binding) (:binding-id resolution)
               (:binding-id use)))
        (is (= :failed (:status changed-report)))
        (is (some #{:authoritative-products-replay?}
                  (:failed-checks changed-report)))
        (is (nil? (:raw-host-error changed-request-result)))
        (is (= "C6-VERIFY" (:rule changed-request-diagnostic)))
        (is (= :authenticated-sh06-request-membership-mismatch
               (get-in changed-request-diagnostic [:facts :reason])))
        (is (nil? (:raw-host-error alias-result)))
        (is (= "C6-VERIFY" (:rule alias-diagnostic)))
        (is (= :authenticated-sh06-request-membership-mismatch
               (get-in alias-diagnostic [:facts :reason])))))))

(deftest sh07-b2-public-replay-and-capability-proof-pass
  (doseq [basename accepted-basenames]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          report
          ((required-var 'sh07-core-artifact-verification) artifact)
          proof
          ((required-var 'sh07-core-capability-based-proof) artifact)]
      (testing basename
        (is (= :gravity/sh07-core-artifact-verification
               (:artifact report)))
        (is (= :passed (:status report)))
        (is (= [] (:failed-checks report)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))))))

(deftest sh07-b2-c6-rejections-are-structured
  (doseq [[basename oracle] rejected-oracles
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared-oracle (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= {:expected-rule (:rule oracle)
                :expected-stage :core-lowering
                :expected-severity :error
                :expected-reason (:reason oracle)
                :expected-remediation (:remediation oracle)}
               declared-oracle))
        (is (nil? (:raw-host-error result)))
        (is (map? diagnostic))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= (:rule oracle) (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= (:reason oracle) (get-in diagnostic [:facts :reason])))
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b2-reference-call-and-evaluation-mutations-fail-replay
  (let [artifact
        (file-artifact "accepted" "higher-order-lexical-call" ".gravity")
        reference-uses (:reference-uses (core artifact))
        calls (:calls (core artifact))
        target
        (or (first (filter #(and (= 'identity (:symbol %))
                                (= :expression (:position %)))
                          reference-uses))
            (first reference-uses))
        wrong
        (first (filter #(not= (:binding-id target) (:binding-id %))
                       reference-uses))
        call (first (filter #(< 1 (count (:argument-node-ids %))) calls))
        call (or call (first calls))
        alternate-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        coherent-wrong-binding
        (-> artifact
            (update-reference-use
             (:core-node-id target)
             #(assoc %
                     :binding-id (:binding-id wrong)
                     :binding-class (:binding-class wrong)
                     :definition-syntax-id
                     (:definition-syntax-id wrong)))
            (update-node
             (:core-node-id target)
             #(-> %
                  (assoc-in [:attributes :binding-id]
                            (:binding-id wrong))
                  (assoc-in [:attributes :binding-class]
                            (:binding-class wrong))
                  (assoc-in [:attributes :definition-syntax-id]
                            (:definition-syntax-id wrong))
                  (assoc :resolved-binding-ids
                         [(:binding-id wrong)]))))
        reordered-arguments (vec (reverse (:argument-node-ids call)))
        reordered-call
        (-> artifact
            (update-call
             (:core-node-id call)
             #(assoc %
                     :argument-node-ids reordered-arguments
                     :ordered-evaluation-node-ids
                     (into [(:operator-node-id %)]
                           reordered-arguments)))
            (update-node
             (:core-node-id call)
             #(let [children
                    (into [(first (:children %))]
                          (reverse (rest (:children %))))]
                (-> %
                    (assoc :children children)
                    (assoc-in
                     [:evaluation :order]
                     (mapv (fn [index node-id]
                             {:index index :core-node-id node-id})
                           (range) children))))))
        mutations
        {"reference omission"
         (update-core-records
          artifact :reference-uses
          #(vec (remove
                 (fn [use]
                   (= (:core-node-id target) (:core-node-id use)))
                 %)))

         "reference duplication"
         (update-core-records
          artifact :reference-uses #(conj % target))

         "reference substitution"
         (update-reference-use
          artifact (:core-node-id target)
          #(assoc % :binding-id alternate-id))

         "valid wrong binding"
         coherent-wrong-binding

         "call argument reorder"
         reordered-call

         "evaluation mutation"
         (update-node
          artifact (:core-node-id call)
          #(assoc-in % [:evaluation :kind]
                     :arguments-before-operator))}]
    (is (map? target))
    (is (map? wrong))
    (is (map? call))
    (is (not= (:binding-id target) (:binding-id wrong)))
    (when (< 1 (count (:argument-node-ids call)))
      (is (not= (:argument-node-ids call) reordered-arguments)))
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
           coherent-wrong-binding)]
      (is (= :failed (:status report)))
      (is (some #{:canonical-core-replays?}
                (:failed-checks report))))))

;; L2/C5/C6: these are concrete assoc/21 and get/3 uses from the selected
;; compiler sources, not a primitive implementation or a new arity policy.
(defn- required-primitive-stage [label operation]
  (println (pr-str {:required-primitive-stage label :event :begin}))
  (let [start (System/nanoTime)
        value (operation)]
    (println (pr-str {:required-primitive-stage label :event :complete
                     :elapsed-ms (quot (- (System/nanoTime) start) 1000000)}))
    value))

(defn- required-primitive-current-artifact []
  (let [source-path (fixture-path "accepted" "required-primitive-call-arities" ".gravity")
        resolved
        (required-primitive-stage
         :current-reader-c5
         #(bootstrap/compiler-c5-resolution-source-artifact
           source-path (source-text source-path)
           {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
           (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")))]
    ;; Preserve the ordinary helper's historical pins and cache. Only this
    ;; explicit development composition selects the actual current reader.
    (required-primitive-stage
     :gravity-core #(bootstrap/sh07-core-from-resolution-artifact resolved))))

(defn- rewrite-required-call-arguments [artifact call arguments]
  (let [call-id (:core-node-id call)
        ordered (into [(:operator-node-id call)] arguments)
        candidate
        (-> artifact
            (update-call call-id #(assoc % :argument-node-ids arguments
                                        :ordered-evaluation-node-ids ordered))
            (update-node call-id
                         #(-> %
                              (assoc :children ordered)
                              (assoc-in [:attributes :argument-count] (count arguments))
                              (assoc-in [:attributes :argument-child-indexes]
                                        (vec (range 1 (inc (count arguments)))))
                              (assoc-in [:evaluation :order]
                                        (mapv (fn [index id] {:index index :core-node-id id})
                                              (range) ordered))))
            (update-core-records
             :evaluation-order
             #(mapv (fn [row] (if (= call-id (:core-node-id row))
                               (assoc row :children ordered) row)) %))
            (update-core-records
             :call-edges
             #(mapv (fn [row] (if (= call-id (:call-core-node-id row))
                               (assoc row :argument-core-node-ids arguments
                                          :ordered-evaluation-node-ids ordered) row)) %)))]
    ;; Keep the obvious product copies coherent: rejection must still compare
    ;; against the original source/producer, not just a stale identity preimage.
    (update-in candidate [:gravity-core-boundary :canonical-core-artifact :identity-preimage]
               merge (select-keys (core candidate) [:nodes :calls :call-edges :evaluation-order]))))

(deftest sh07-b2-required-primitive-arities-current-gravity-composition
  (let [artifact (required-primitive-current-artifact)
        product (core artifact)
        input (request artifact)
        index (node-index artifact)
        forms (exactly-once-index (:forms input) :form-id)
        bindings (exactly-once-index (:binding-table input) :binding-id)
        source-maps (exactly-once-index (:source-map product) :core-node-id)
        calls (exactly-once-index (:calls product)
                                  #(get-in index [(:operator-node-id %) :attributes :symbol]))
        assoc-call (get calls 'assoc)
        get-call (get calls 'get)
        default-id (last (:argument-node-ids get-call))
        default-node (get index default-id)
        report (required-primitive-stage
                :fresh-public-verification #(bootstrap/sh07-core-artifact-verification artifact))]
    (is (= :accepted (:status artifact)))
    (is (= :passed (:status report)) (pr-str (:failed-checks report)))
    (is (= '#{assoc get} (set (keys calls))))
    (is (= [21 3] (mapv #(count (:argument-node-ids %)) [assoc-call get-call])))
    (is (= (fixture-path "accepted" "required-primitive-call-arities" ".gravity")
           (:source-path report) (get-in product [:provenance :actual-source-path])))
    (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
           (get-in artifact [:execution-boundary :downstream-fact-statuses])))
    (doseq [[operator call] calls]
      (let [operator-node (get index (:operator-node-id call))
            binding (get bindings (:operator-binding-id call))
            ids (into [(:operator-node-id call)] (:argument-node-ids call))
            node (get index (:core-node-id call))
            form (get forms (get-in node [:source :form-id]))]
        (is (= [:core 'gravity.core operator]
               ((juxt :binding-class :namespace :name) binding)))
        (is (= [:reference :operator operator (:operator-binding-id call)]
               ((juxt :core-form #(get-in % [:attributes :position])
                      #(get-in % [:attributes :symbol]) #(get-in % [:attributes :binding-id]))
                operator-node)))
        (is (= ids (:children node) (:ordered-evaluation-node-ids call)
               (mapv :core-node-id (get-in node [:evaluation :order]))))
        (is (= (count ids) (count (set ids))))
        (is (= (:child-form-ids form)
               (mapv #(get-in index [% :source :form-id]) ids)))
        (is (= :operator-then-arguments (:evaluation-order call)))
        (is (= (count (:argument-node-ids call)) (get-in node [:attributes :argument-count])))))
    (is (= (vec (mapcat (fn [n] [(keyword (str "k" n)) n]) (range 1 11)))
           (mapv #(get-in index [% :attributes :value]) (rest (:argument-node-ids assoc-call)))))
    (is (= [:collection-literal :map]
           (let [receiver (get index (first (:argument-node-ids assoc-call)))]
             [(:core-form receiver) (get-in receiver [:attributes :literal-kind])])))
    (is (= :if (:core-form default-node)))
    (is (= [true :fallback :unused]
           (mapv #(get-in index [% :attributes :value]) (:children default-node))))
    (is (= 1 (count (filter #{default-id} (:ordered-evaluation-node-ids get-call)))))
    (is (= 1 (count (:function-records product)) (count (:macro-origin-traces input))))
    (doseq [node (:nodes product)]
      (let [source (:source node)
            form (get forms (:form-id source))
            mapping (get source-maps (:node-id node))]
        (is (map? form))
        (is (map? mapping))
        (is (= (select-keys form [:syntax-id :form-id :origin-chain :generated-origin])
               (select-keys source [:syntax-id :form-id :origin-chain :generated-origin])
               (select-keys mapping [:syntax-id :form-id :origin-chain :generated-origin])))
        (is (= (:source-span form) (:source-span mapping)))
        (when (#{:def :fn} (:core-form node))
          (is (seq (:generated-origin source))))))
    (is (= (:source-map product) (:source-map (identity-input artifact))))
    (let [session (required-primitive-stage
                   :original-verification-session
                   #(bootstrap/sh07-core-verification-session (:sh06-resolution-artifact artifact)))
          verify #(bootstrap/sh07-core-artifact-verification-against-expected % session)
          assoc-arguments (:argument-node-ids assoc-call)
          get-arguments (:argument-node-ids get-call)
          reversed-pairs (into [(first assoc-arguments)]
                               (mapcat identity (reverse (partition 2 (rest assoc-arguments)))))
          substituted (first (:children default-node))
          candidates
          [["assoc final pair omitted" (rewrite-required-call-arguments artifact assoc-call (vec (drop-last 2 assoc-arguments)))]
           ["assoc pairs reordered" (rewrite-required-call-arguments artifact assoc-call reversed-pairs)]
           ["get default omitted" (rewrite-required-call-arguments artifact get-call (pop get-arguments))]
           ["get default replaced by existing condition occurrence"
            (rewrite-required-call-arguments artifact get-call (assoc get-arguments 2 substituted))]]]
      (is (= :ready (:status session)))
      (is (= :passed (:status (verify artifact))))
      (doseq [[label candidate] candidates]
        (testing label
          (is (not= artifact candidate))
          (let [rejected (verify candidate)]
            (is (= :failed (:status rejected)))
            (is (= (:source-path report) (:source-path rejected)))
            (is (every? (set (:failed-checks rejected))
                        [:canonical-core-replays? :authoritative-products-replay?]))
            (println (pr-str {:required-primitive-mutation label
                              :status (:status rejected)
                              :failed-checks (:failed-checks rejected)}))))))))

(deftest sh07-b2-required-primitive-arities-paired-bytes-and-seed-parser
  ;; This is explicitly not a second current-reader/C5/Gravity-Core execution.
  (let [paths (mapv #(fixture-path "accepted" "required-primitive-call-arities" %) extensions)
        texts (mapv source-text paths)]
    (is (= (seq (source-bytes (first paths))) (seq (source-bytes (second paths)))))
    (is (= (bootstrap/read-forms (first paths) (first texts))
           (bootstrap/read-forms (second paths) (second texts)))))
  (doseq [extension extensions]
    (is (= (set accepted-basenames) (fixture-basenames "accepted" extension)))))
