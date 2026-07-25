(ns gravity.self-hosting.sh07-loop-recur-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_loop_recur_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B4 test source is not on the classpath"
                      {:id "SH07-B4-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B4-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b4")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private public-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :sh06-resolution-artifact :gravity-core-boundary
    :provenance :pass :execution-boundary :capability-based-proof
    :diagnostics})
(def ^:private diagnostic-keys
  #{:artifact :rule :severity :stage :syntax-id :form-id
    :core-node-id :source-span :generated-origin-chain
    :namespace :profile :target :lowering-rule :facts
    :remediation :diagnostic-id-request})
(def ^:private accepted-basenames
  ["loop-no-recur"
   "zero-arity-loop-recur"
   "sequential-loop-bindings"
   "multi-argument-recur-order"
   "nearest-nested-loop-target"
   "fixed-arity-function-recur"
   "loop-overrides-function-target"
   "tail-context-recur"])
(def ^:private target-remediation
  "Place recur in tail position inside the nearest loop or fixed-arity function target and pass exactly the target arity.")
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private gap-remediation
  "Use only the declared SH-07-B4 fixed-arity fn, simple-symbol sequential loop, tail recur, and previously supported core subset.")
(def ^:private rejected-oracles
  {"loop-binding-vector-required"
   {:rule "C6-CORE-SHAPE"
    :reason :loop-binding-vector-required
    :remediation shape-remediation}
   "odd-loop-bindings"
   {:rule "C6-CORE-SHAPE"
    :reason :loop-bindings-even-required
    :remediation shape-remediation}
   "non-symbol-loop-binding"
   {:rule "C6-CORE-SHAPE"
    :reason :loop-binding-symbol-required
    :remediation shape-remediation}
   "empty-loop-body"
   {:rule "C6-CORE-SHAPE"
    :reason :loop-body-required
    :remediation shape-remediation}
   "loop-destructuring"
   {:rule "C6-LOWERING-GAP"
    :reason :loop-destructuring-deferred
    :remediation gap-remediation}
   "recur-without-target"
   {:rule "C6-VERIFY"
    :reason :recur-target-required
    :semantic-rule "L2-RECUR-TARGET"
    :remediation target-remediation}
   "loop-recur-arity-mismatch"
   {:rule "C6-VERIFY"
    :reason :recur-arity-mismatch
    :semantic-rule "L2-RECUR-TARGET"
    :facts {:target-kind :loop :expected-arity 1 :actual-arity 2}
    :remediation target-remediation}
   "function-recur-arity-mismatch"
   {:rule "C6-VERIFY"
    :reason :recur-arity-mismatch
    :semantic-rule "L2-RECUR-TARGET"
    :facts {:target-kind :function :expected-arity 1 :actual-arity 2}
    :remediation target-remediation}
   "recur-not-tail"
   {:rule "C6-VERIFY"
    :reason :recur-tail-position-required
    :semantic-rule "L2-RECUR-TARGET"
    :remediation target-remediation}
   "outer-loop-hidden-by-function"
   {:rule "C6-VERIFY"
    :reason :recur-arity-mismatch
    :semantic-rule "L2-RECUR-TARGET"
    :facts {:target-kind :function :expected-arity 0 :actual-arity 1}
    :remediation target-remediation}
   "variadic-function-recur"
   {:rule "C6-LOWERING-GAP"
    :reason :variadic-function-recur-deferred
    :remediation gap-remediation}})
(def ^:private diagnostic-parity-basenames
  #{"loop-binding-vector-required"
    "loop-destructuring"
    "recur-not-tail"})

(def ^:private maximum-loop-binding-records 1024)
(def ^:private maximum-recur-target-records 1024)
(def ^:private maximum-recur-transfer-records 1024)
(def ^:private maximum-recur-target-depth 256)
(def ^:private loop-binding-keys
  #{:loop-core-node-id :ordinal :name :binding-id
    :definition-form-id :definition-syntax-id :binding-scope-id
    :initializer-form-id :initializer-syntax-id
    :initializer-scope-id :initializer-node-id
    :visible-prior-binding-ids :mutability})
(def ^:private recur-target-keys
  #{:target-id :target-kind :owner-core-node-id
    :owner-form-id :owner-syntax-id :arity
    :binding-ids :body-scope-id :parent-target-id
    :type-compatibility})
(def ^:private recur-transfer-keys
  #{:recur-core-node-id :recur-form-id :recur-syntax-id
    :target-id :target-kind :arity :argument-node-ids
    :tail-position :evaluation-order :transfer-policy
    :type-compatibility})

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
        "Required SH-07-B4 coordinator adapter is absent"
        {:id "SH07-B4-ADAPTER-ABSENT"
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
    (get (second clause) :sh07-b4)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- nodes
  [artifact]
  (:nodes (core artifact)))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info "SH-07-B4 records are not uniquely identifiable"
                {:id "SH07-B4-AMBIGUOUS-INDEX"
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

(defn- target-index
  [artifact]
  (exactly-once-index (:recur-targets (core artifact)) :target-id))

(defn- sha256-id?
  [value]
  (boolean
   (and (string? value)
        (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- target-depth
  [targets target]
  (loop [current target observed #{} depth 1]
    (let [parent-id (:parent-target-id current)]
      (cond
        (nil? parent-id) depth
        (contains? observed parent-id)
        (throw
         (ex-info "SH-07-B4 recur target parent cycle"
                  {:id "SH07-B4-TARGET-CYCLE"
                   :target-id (:target-id target)
                   :parent-target-id parent-id}))
        :else
        (recur (get targets parent-id)
               (conj observed parent-id)
               (inc depth))))))

(defn- form-operator
  [forms form]
  (when (= :list (:kind form))
    (:value (get forms (first (:child-form-ids form))))))

(defn- nearest-recur-owner-form-id
  [artifact recur-form-id]
  (let [forms (form-index artifact)]
    (loop [form (get forms recur-form-id)]
      (let [parent (get forms (:parent-form-id form))
            operator (form-operator forms parent)]
        (cond
          (nil? parent) nil
          (contains? '#{loop fn} operator) (:form-id parent)
          :else (recur parent))))))

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

(defn- normalized-diagnostic
  [diagnostic]
  (assoc-in diagnostic [:source-span :source] "<co-canonical-source>"))

(defn- update-core-records
  [artifact table update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact table]
   update-function))

(defn- update-core-record
  [artifact table key-name key-value update-function]
  (update-core-records
   artifact table
   (fn [records]
     (mapv (fn [record]
             (if (= key-value (get record key-name))
               (update-function record)
               record))
           records))))

(defn- table-template
  [artifact table]
  (get-in artifact
          [:gravity-core-boundary :raw-template-result
           :core-template table]))

(defn- assert-origin-link
  [node form]
  (is (= (:form-id form) (get-in node [:source :form-id])))
  (is (= (:syntax-id form) (get-in node [:source :syntax-id])))
  (is (= (:source-span form) (get-in node [:source :semantic-span])))
  (is (= (:origin-chain form) (get-in node [:source :origin-chain])))
  (is (= (:generated-origin form)
         (get-in node [:source :generated-origin]))))

(defn- assert-b4-tables
  [artifact]
  (let [core-artifact (core artifact)
        bindings (:loop-bindings core-artifact)
        targets (:recur-targets core-artifact)
        transfers (:recur-transfers core-artifact)
        nodes-by-id (node-index artifact)
        forms-by-id (form-index artifact)
        bindings-by-id (binding-index artifact)
        targets-by-id (target-index artifact)
        bounds
        (get-in artifact
                [:gravity-core-boundary :raw-template-result :bounds])]
    (is (= 5 (:schema-version (request artifact))))
    (is (= :sh07-b4-meta-jvm-core (:scope (request artifact))))
    (is (= maximum-loop-binding-records
           (:maximum-loop-binding-records bounds)))
    (is (= maximum-recur-target-records
           (:maximum-recur-target-records bounds)))
    (is (= maximum-recur-transfer-records
           (:maximum-recur-transfer-records bounds)))
    (is (= maximum-recur-target-depth
           (:maximum-recur-target-depth bounds)))
    (is (<= (count bindings) maximum-loop-binding-records))
    (is (<= (count targets) maximum-recur-target-records))
    (is (<= (count transfers) maximum-recur-transfer-records))
    (is (<= (+ (count (:lexical-bindings core-artifact))
               (count bindings))
            maximum-loop-binding-records))
    (doseq [table [:loop-bindings :recur-targets :recur-transfers]]
      (is (vector? (get core-artifact table)))
      (is (= (get core-artifact table)
             (get (identity-input artifact) table)))
      (is (= (mapv #(dissoc % :loop-core-node-id
                            :owner-core-node-id :recur-core-node-id
                            :initializer-node-id :argument-node-ids)
                   (get core-artifact table))
             (mapv #(dissoc % :loop-core-node-id
                            :owner-core-node-id :recur-core-node-id
                            :initializer-node-id :argument-node-ids)
                   (table-template artifact table)))))
    (doseq [record bindings]
      (let [loop-node (get nodes-by-id (:loop-core-node-id record))
            initializer
            (get nodes-by-id (:initializer-node-id record))
            definition-form
            (get forms-by-id (:definition-form-id record))
            initializer-form
            (get forms-by-id (:initializer-form-id record))
            binding (get bindings-by-id (:binding-id record))]
        (is (= loop-binding-keys (set (keys record))))
        (is (= :loop (:core-form loop-node)))
        (is (map? initializer))
        (is (= :symbol (:kind definition-form)))
        (is (= (:name record) (:value definition-form)))
        (is (= (:definition-syntax-id record)
               (:syntax-id definition-form)))
        (is (= (:initializer-syntax-id record)
               (:syntax-id initializer-form)))
        (is (= (:binding-scope-id record) (:scope-id binding)))
        (is (= :immutable (:mutability record)))
        (assert-origin-link initializer initializer-form)))
    (doseq [target targets]
      (let [owner (get nodes-by-id (:owner-core-node-id target))
            owner-form (get forms-by-id (:owner-form-id target))
            parent (:parent-target-id target)]
        (is (= recur-target-keys (set (keys target))))
        (is (sha256-id? (:target-id target)))
        (is (contains? #{:loop :function} (:target-kind target)))
        (is (= (:owner-form-id target)
               (get-in owner [:source :form-id])))
        (is (= (:owner-syntax-id target)
               (get-in owner [:source :syntax-id])))
        (is (= (:target-kind target)
               (case (:core-form owner)
                 :loop :loop
                 :fn :function
                 nil)))
        (is (= (:arity target) (count (:binding-ids target))))
        (is (every? #(contains? bindings-by-id %)
                    (:binding-ids target)))
        (is (sha256-id? (:body-scope-id target)))
        (is (= :pending-sh08 (:type-compatibility target)))
        (is (or (nil? parent) (contains? targets-by-id parent)))
        (is (<= (target-depth targets-by-id target)
                maximum-recur-target-depth))
        (assert-origin-link owner owner-form)))
    (doseq [transfer transfers]
      (let [recur-node
            (get nodes-by-id (:recur-core-node-id transfer))
            recur-form (get forms-by-id (:recur-form-id transfer))
            target (get targets-by-id (:target-id transfer))]
        (is (= recur-transfer-keys (set (keys transfer))))
        (is (= :recur (:core-form recur-node)))
        (is (= (:target-kind target) (:target-kind transfer)))
        (is (= (:arity target) (:arity transfer)))
        (is (= (:arity transfer)
               (count (:argument-node-ids transfer))))
        (is (= (:argument-node-ids transfer) (:children recur-node)))
        (is (= true (:tail-position transfer)))
        (is (= :arguments-left-to-right (:evaluation-order transfer)))
        (is (= :nearest-lexical-recur-target
               (:transfer-policy transfer)))
        (is (= :pending-sh08 (:type-compatibility transfer)))
        (is (= (:owner-form-id target)
               (nearest-recur-owner-form-id
                artifact (:recur-form-id transfer))))
        (is (= (:argument-node-ids transfer)
               (mapv :core-node-id
                     (get-in recur-node [:evaluation :order]))))
        (assert-origin-link recur-node recur-form)))
    {:loop-bindings bindings
     :recur-targets targets
     :recur-transfers transfers}))

(deftest sh07-b4-fixtures-are-complete-paired-and-path-neutral
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
  (let [basename "nearest-nested-loop-target"
        gravity (file-artifact "accepted" basename ".gravity")
        qst (file-artifact "accepted" basename ".qst")
        gravity-path (fixture-path "accepted" basename ".gravity")
        qst-path (fixture-path "accepted" basename ".qst")]
    (is (= :accepted (:status gravity) (:status qst)))
    (is (= (:artifact-id gravity) (:artifact-id qst)))
    (is (= (identity-input gravity) (identity-input qst)))
    (is (= (select-keys (core gravity)
                        [:loop-bindings :recur-targets
                         :recur-transfers])
           (select-keys (core qst)
                        [:loop-bindings :recur-targets
                         :recur-transfers])))
    (is (= gravity-path (get-in gravity [:provenance :source-path])))
    (is (= qst-path (get-in qst [:provenance :source-path])))
    (is (= gravity-path
           (get-in gravity
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))
    (is (= qst-path
           (get-in qst
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))))

(deftest sh07-b4-products-bind-targets-transfers-and-order
  (doseq [basename accepted-basenames]
    (testing (str basename ".gravity")
      (let [artifact (file-artifact "accepted" basename ".gravity")
            tables (assert-b4-tables artifact)]
        (is (seq (:recur-targets tables)))
        (when (= basename "loop-no-recur")
          (is (empty? (:recur-transfers tables))))
        (when (= basename "zero-arity-loop-recur")
          (is (= [0] (mapv :arity (:recur-targets tables))))
          (is (= [0] (mapv :arity (:recur-transfers tables)))))))))

(deftest sh07-b4-nearest-target-function-boundary-and-tail-are-explicit
  (let [nested
        (file-artifact "accepted" "nearest-nested-loop-target" ".gravity")
        nested-targets (:recur-targets (core nested))
        nested-transfers (:recur-transfers (core nested))
        nested-target-index (target-index nested)
        function
        (file-artifact "accepted" "fixed-arity-function-recur" ".gravity")
        function-targets (:recur-targets (core function))
        overrides
        (file-artifact "accepted" "loop-overrides-function-target"
                       ".gravity")
        override-targets (:recur-targets (core overrides))
        override-transfers (:recur-transfers (core overrides))
        override-index (target-index overrides)
        function-target
        (first (filter #(= :function (:target-kind %))
                       override-targets))
        loop-target
        (first (filter #(= :loop (:target-kind %))
                       override-targets))
        tail
        (file-artifact "accepted" "tail-context-recur" ".gravity")]
    (is (= 2 (count nested-targets)))
    (is (= 2 (count nested-transfers)))
    (is (= #{:loop} (set (map :target-kind nested-targets))))
    (is (= 1 (count (filter :parent-target-id nested-targets))))
    (is (= (set (map :target-id nested-targets))
           (set (map :target-id nested-transfers))))
    (is (every? #(contains? nested-target-index (:target-id %))
                nested-transfers))
    (is (= [:function] (mapv :target-kind function-targets)))
    (is (= 2 (count override-targets)))
    (is (= #{:function :loop}
           (set (map :target-kind override-targets))))
    (is (= (:target-id function-target)
           (:parent-target-id loop-target)))
    (is (= [:loop]
           (mapv #(get-in override-index [(:target-id %) :target-kind])
                 override-transfers)))
    (is (every? true? (map :tail-position
                           (:recur-transfers (core tail)))))))

(deftest sh07-b4-public-replay-and-capability-proof-pass
  (doseq [basename accepted-basenames]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          embedded-proof (:capability-based-proof artifact)]
      (testing basename
        (is (= public-artifact-keys (set (keys artifact))))
        (is (= :gravity/sh07-core-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (= "SH-07-B4" (:task artifact)))
        (is (= ["L2" "C6"] (:document-set artifact)))
        (is (= :c6-gravity-core-lowering-b4
               (get-in artifact [:pass :name])))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact embedded-proof)))
        (is (= :complete (:status embedded-proof)))
        (is (= [] (:failed-checks embedded-proof)))
        (is (every? true?
                    (vals (dissoc embedded-proof
                                  :artifact :status :failed-checks))))
        (is (= :passed
               (get-in artifact
                       [:gravity-core-boundary
                        :template-verification :status])))
        (is (= :passed
               (get-in artifact
                       [:gravity-core-boundary
                        :resolved-verification :status])))
        (is (= (:artifact-id artifact)
               (get-in artifact
                       [:gravity-core-boundary
                        :authenticated-envelope-descriptor
                        :semantic-artifact-id])
               (get-in artifact
                       [:gravity-core-boundary
                        :authenticated-envelope
                        :semantic-artifact-id])))
        (is (= (get-in artifact [:provenance :source-path])
               (get-in artifact
                       [:gravity-core-boundary
                        :authenticated-envelope
                        :actual-source-path])))
        (is (= false
               (get-in artifact
                       [:gravity-core-boundary
                        :target-source-reread?])))
        (is (= false
               (get-in artifact
                       [:gravity-core-boundary :self-hosted?])))
        (is (= false
               (get-in artifact
                       [:execution-boundary :sh07-complete?]))))))
  (testing "one rich nested-loop artifact passes the complete public proof"
    (let [artifact
          (file-artifact "accepted" "nearest-nested-loop-target" ".gravity")
          public-proof
          ((required-var 'sh07-core-capability-based-proof) artifact)]
      (is (= (:capability-based-proof artifact) public-proof))
      (is (= :gravity/sh07-core-capability-proof
             (:artifact public-proof)))
      (is (= :complete (:status public-proof)))
      (is (= [] (:failed-checks public-proof)))
      (is (every? true?
                  (vals (dissoc public-proof
                                :artifact :status :failed-checks)))))))

(deftest sh07-b4-rejections-are-structured-and-oracle-bound
  (doseq [[basename oracle] rejected-oracles]
    (let [checked-extensions
          (if (contains? diagnostic-parity-basenames basename)
            extensions
            [".gravity"])
          diagnostics
          (mapv
           (fn [extension]
             (testing (str basename extension)
               (let [source-path
                     (fixture-path "rejected" basename extension)
                     declared (fixture-oracle basename extension)
                     result
                     (diagnostic-result
                      #((required-var 'sh07-core-file-artifact)
                        source-path))
                     diagnostic (diagnostic-data result)
                     expected-declaration
                     (cond-> {:expected-rule (:rule oracle)
                              :expected-stage :core-lowering
                              :expected-severity :error
                              :expected-reason (:reason oracle)
                              :expected-remediation
                              (:remediation oracle)}
                       (:semantic-rule oracle)
                       (assoc :expected-semantic-rule
                              (:semantic-rule oracle)))]
                 (is (= expected-declaration declared))
                 (is (nil? (:raw-host-error result)))
                 (is (map? diagnostic))
                 (is (= diagnostic-keys (set (keys diagnostic))))
                 (is (= :gravity/sh07-core-diagnostic
                        (:artifact diagnostic)))
                 (is (= (:rule oracle) (:rule diagnostic)))
                 (is (= :core-lowering (:stage diagnostic)))
                 (is (= :error (:severity diagnostic)))
                 (is (= :sh07-b4-core-lowering
                        (:lowering-rule diagnostic)))
                 (is (= (:reason oracle)
                        (get-in diagnostic [:facts :reason])))
                 (is (= (:semantic-rule oracle)
                        (get-in diagnostic [:facts :semantic-rule])))
                 (when (:semantic-rule oracle)
                   (is (= (:semantic-rule oracle)
                          (get-in diagnostic
                                  [:facts :rule-specific
                                   :semantic-rule]))))
                 (when-let [facts (:facts oracle)]
                   (is (= facts
                          (select-keys
                           (get-in diagnostic [:facts :rule-specific])
                           (keys facts)))))
                 (is (= (:remediation oracle)
                        (:remediation diagnostic)))
                 (is (= source-path
                        (get-in diagnostic [:source-span :source])))
                 (is (= true
                        (get-in diagnostic [:facts :fail-closed])))
                 (is (sha256-id? (:diagnostic-id-request diagnostic)))
                 diagnostic)))
           checked-extensions)]
      (when (= checked-extensions extensions)
        (testing (str basename " co-canonical diagnostic parity")
        (is (= (normalized-diagnostic (first diagnostics))
               (normalized-diagnostic (second diagnostics)))))))))

(deftest sh07-b4-target-transfer-and-order-mutations-fail-replay
  (let [artifact
        (file-artifact "accepted" "multi-argument-recur-order" ".gravity")
        bindings (:loop-bindings (core artifact))
        targets (:recur-targets (core artifact))
        transfers (:recur-transfers (core artifact))
        binding (first bindings)
        target (first targets)
        transfer (first transfers)
        alternate-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        mutations
        {"loop binding omission"
         {:artifact
          (update-core-records artifact :loop-bindings
                               #(vec (rest %)))
          :checks #{:loop-bindings-replay?}}
         "loop binding duplication"
         {:artifact
          (update-core-records artifact :loop-bindings
                               #(conj % binding))
          :checks #{:loop-bindings-replay?}}
         "target omission"
         {:artifact
          (update-core-records artifact :recur-targets
                               #(vec (rest %)))
          :checks #{:recur-targets-replay?}}
         "target duplication"
         {:artifact
          (update-core-records artifact :recur-targets
                               #(conj % target))
          :checks #{:recur-targets-replay?}}
         "transfer omission"
         {:artifact
          (update-core-records artifact :recur-transfers
                               #(vec (rest %)))
          :checks #{:recur-transfers-replay?}}
         "transfer duplication"
         {:artifact
          (update-core-records artifact :recur-transfers
                               #(conj % transfer))
          :checks #{:recur-transfers-replay?}}
         "target substitution"
         {:artifact
          (update-core-record
           artifact :recur-transfers :recur-core-node-id
           (:recur-core-node-id transfer)
           #(assoc % :target-id alternate-id))
          :checks #{:recur-transfers-replay?}}
         "target arity substitution"
         {:artifact
          (update-core-record
           artifact :recur-targets :target-id (:target-id target)
           #(update % :arity inc))
          :checks #{:recur-targets-replay?}}
         "argument order substitution"
         {:artifact
          (update-core-record
           artifact :recur-transfers :recur-core-node-id
           (:recur-core-node-id transfer)
           #(update % :argument-node-ids
                    (fn [values] (vec (reverse values)))))
          :checks #{:recur-transfers-replay?}}
         "owner substitution"
         {:artifact
          (update-core-record
           artifact :recur-targets :target-id (:target-id target)
           #(assoc % :owner-core-node-id alternate-id))
          :checks #{:recur-targets-replay?}}
         "tail substitution"
         {:artifact
          (update-core-record
           artifact :recur-transfers :recur-core-node-id
           (:recur-core-node-id transfer)
           #(assoc % :tail-position false))
          :checks #{:recur-transfers-replay?}}
         "authenticated envelope descriptor substitution"
         {:artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :authenticated-envelope-descriptor
            :semantic-artifact-id]
           alternate-id)
          :checks #{}}
         "authenticated envelope provenance substitution"
         {:artifact
          (assoc-in
           artifact
           [:gravity-core-boundary :authenticated-envelope
            :actual-source-path]
           "/tmp/substituted-sh07-b4.gravity")
          :checks #{}}}]
    (is (map? binding))
    (is (map? target))
    (is (map? transfer))
    (doseq [[label {:keys [checks] mutation :artifact}] mutations]
      (testing label
        (is (not= artifact mutation))
        (let [results
              ((required-var 'sh07-core-verification-checks)
               mutation artifact {:status :passed})
              failed
              (set (for [[check passed?] results
                         :when (not (true? passed?))]
                     check))]
          (is (contains? failed :authoritative-products-replay?))
          (is (every? failed checks))
          (when (seq checks)
            (is (contains? failed :canonical-core-replays?))))))
    (let [report
          ((required-var 'sh07-core-artifact-verification)
           (get-in mutations ["target substitution" :artifact]))]
      (is (= :failed (:status report)))
      (is (some #{:canonical-core-replays?}
                (:failed-checks report)))
      (is (some #{:recur-transfers-replay?}
                (:failed-checks report))))))
