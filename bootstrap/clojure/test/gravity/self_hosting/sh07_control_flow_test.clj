(ns gravity.self-hosting.sh07-control-flow-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_control_flow_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B1 test source is not on the classpath"
                      {:id "SH07-B1-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B1-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["control-flow-order" "control-flow-truthiness"])
(def ^:private rejected-basenames
  ["if-missing-branch" "if-extra-branch" "empty-do" "nested-def"])
(def ^:private rejected-reasons
  {"if-missing-branch" :if-arity
   "if-extra-branch" :if-arity
   "empty-do" :do-empty-body
   "nested-def" :def-top-level-only})
(def ^:private core-shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private maximum-control-flow-records 1024)
(def ^:private if-control-flow-keys
  #{:core-node-id :kind :condition-node-id :branches
    :branch-exclusivity :truthiness :result-policy})
(def ^:private do-control-flow-keys
  #{:core-node-id :kind :ordered-child-node-ids
    :evaluation-order :result-policy})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B1 coordinator adapter is absent"
        {:id "SH07-B1-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]}}))))

(def ^:private artifacts (atom {}))

(defn- file-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @artifacts key)
        (let [artifact
              ((required-var 'sh07-core-file-artifact)
               (fixture-path family basename extension))]
          (swap! artifacts assoc key artifact)
          artifact))))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- nodes
  [artifact]
  (:nodes (core artifact)))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info "SH-07-B1 records are not uniquely identifiable"
                {:id "SH07-B1-AMBIGUOUS-INDEX"
                 :key key-name
                 :record-count (count records)
                 :unique-count (count index)})))
    index))

(defn- node-index
  [artifact]
  (exactly-once-index (nodes artifact) :node-id))

(defn- evaluation-index
  [artifact]
  (exactly-once-index (:evaluation-order (core artifact)) :core-node-id))

(defn- control-flow-records
  [artifact]
  (:control-flow (core artifact)))

(defn- control-flow-index
  [artifact]
  (exactly-once-index (control-flow-records artifact) :core-node-id))

(defn- definition
  [artifact name]
  (let [matches
        (filterv #(= name (str (:name %))) (:definitions (core artifact)))]
    (when-not (= 1 (count matches))
      (throw
       (ex-info "SH-07-B1 definition is not uniquely identifiable"
                {:id "SH07-B1-AMBIGUOUS-DEFINITION"
                 :name name
                 :matches (count matches)})))
    (first matches)))

(defn- definition-value-node
  [artifact name]
  (get (node-index artifact)
       (:value-node-id (definition artifact name))))

(defn- child-nodes
  [index node]
  (mapv index (:children node)))

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

(defn- update-node
  [artifact node-id update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact :nodes]
   (fn [records]
     (mapv (fn [node]
             (if (= node-id (:node-id node))
               (update-function node)
               node))
           records))))

(defn- update-control-flow-record
  [artifact node-id update-function]
  (update-in
   artifact
   [:gravity-core-boundary :canonical-core-artifact :control-flow]
   (fn [records]
     (mapv (fn [record]
             (if (= node-id (:core-node-id record))
               (update-function record)
               record))
           records))))

(defn- assert-control-flow-reference-integrity
  [artifact]
  (let [records (control-flow-records artifact)
        index (node-index artifact)
        node-ids (set (keys index))
        expected-control-node-ids
        (set (map :node-id
                  (filter #(contains? #{:if :do} (:core-form %))
                          (nodes artifact))))
        table-node-ids (set (map :core-node-id records))
        declared-maximum
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :bounds :maximum-control-flow-records])]
    (is (vector? records))
    (is (= maximum-control-flow-records declared-maximum))
    (is (<= 1 (count records) declared-maximum))
    (is (= (count records) (count table-node-ids)))
    (is (= expected-control-node-ids table-node-ids))
    (is (= records (:control-flow (identity-input artifact))))
    (let [template-control-flow
          (get-in artifact
                  [:gravity-core-boundary :raw-template-result
                   :core-template :control-flow])]
      (is (vector? template-control-flow))
      (is (= (count records) (count template-control-flow)))
      (is (= (mapv :kind records)
             (mapv :kind template-control-flow))))
    (doseq [record records]
      (let [node (get index (:core-node-id record))]
        (is (contains? node-ids (:core-node-id record)))
        (case (:kind record)
          :if
          (do
            (is (= if-control-flow-keys (set (keys record))))
            (is (= :if (:core-form node)))
            (is (= (first (:children node))
                   (:condition-node-id record)))
            (is (contains? node-ids (:condition-node-id record)))
            (is (= [{:role :then
                     :predicate :truthy
                     :core-node-id (second (:children node))}
                    {:role :else
                     :predicate :falsey
                     :core-node-id (nth (:children node) 2)}]
                   (:branches record)))
            (is (every? #(contains? node-ids (:core-node-id %))
                        (:branches record)))
            (is (= :exactly-one (:branch-exclusivity record)))
            (is (= {:false-values [:nil :false] :other-values :true}
                   (:truthiness record)))
            (is (= :selected-branch (:result-policy record))))

          :do
          (do
            (is (= do-control-flow-keys (set (keys record))))
            (is (= :do (:core-form node)))
            (is (= (:children node)
                   (:ordered-child-node-ids record)))
            (is (every? node-ids (:ordered-child-node-ids record)))
            (is (= :left-to-right (:evaluation-order record)))
            (is (= {:kind :last-child
                    :core-node-id (last (:children node))}
                   (:result-policy record))))

          (is false (str "Unexpected control-flow record kind: "
                         (:kind record))))))
    records))

(deftest sh07-b1-fixtures-are-paired-and-path-neutral
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames rejected-basenames)]
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
        (is (= (:artifact-id (core gravity)) (:artifact-id (core qst))))
        (is (= (control-flow-records gravity)
               (control-flow-records qst)))
        (is (not=
             (get-in gravity [:provenance :source-path])
             (get-in qst [:provenance :source-path])))
        (is (=
             (get-in gravity [:provenance :source-path])
             (get-in gravity
                     [:gravity-core-boundary :canonical-core-artifact
                      :provenance :actual-source-path])))))))

(deftest sh07-b1-if-and-do-preserve-order-and-exclusive-branches
  (doseq [extension extensions]
    (let [artifact (file-artifact "accepted" "control-flow-order" extension)
          index (node-index artifact)
          evaluations (evaluation-index artifact)
          outer-do (definition-value-node artifact "nested-order")
          [one-node if-node six-node] (child-nodes index outer-do)
          [condition-node then-do else-do] (child-nodes index if-node)
          [two-node nested-if] (child-nodes index then-do)
          [nested-condition nested-then nested-else]
          (child-nodes index nested-if)
          else-nodes (child-nodes index else-do)]
      (testing extension
        (assert-control-flow-reference-integrity artifact)
        (is (= :do (:core-form outer-do)))
        (is (= {:body-count 3
                :evaluation-order :left-to-right
                :result :last-child
                :result-child-index 2}
               (:attributes outer-do)))
        (is (= [1 6]
               (mapv #(get-in % [:attributes :value])
                     [one-node six-node])))
        (is (= :if (:core-form if-node)))
        (is (= {:condition-child-index 0
                :then-child-index 1
                :else-child-index 2
                :condition-evaluation :always-first
                :branch-evaluation :exactly-one
                :truthiness {:false-values [:nil :false]
                             :other-values :true}}
               (:attributes if-node)))
        (is (= true (get-in condition-node [:attributes :value])))
        (is (= [:do :do] (mapv :core-form [then-do else-do])))
        (is (= 2 (get-in two-node [:attributes :value])))
        (is (= :if (:core-form nested-if)))
        (is (= false (get-in nested-condition [:attributes :value])))
        (is (= [30 3]
               (mapv #(get-in % [:attributes :value])
                     [nested-then nested-else])))
        (is (= [4 5]
               (mapv #(get-in % [:attributes :value]) else-nodes)))
        (is (= (:children outer-do)
               (:children (get evaluations (:node-id outer-do)))))
        (is (= [(:node-id condition-node)]
               (:children (get evaluations (:node-id if-node)))))
        (is (= :condition-then-exclusive-branch
               (get-in if-node [:evaluation :kind])))
        (is (= :then
               (get-in then-do [:evaluation :region :role])))
        (is (= :else
               (get-in else-do [:evaluation :region :role])))
        (is (= :selected-exactly-once
               (get-in then-do [:evaluation :region :execution])))
        (is (= :selected-exactly-once
               (get-in else-do [:evaluation :region :execution])))
        (is (= :then
               (get-in nested-if [:evaluation :region :role])))
        (is (= (get-in nested-if [:source :syntax-id])
               (get-in nested-then
                       [:evaluation :region :if-syntax-id])))
        (is (= (get-in nested-if [:source :syntax-id])
               (get-in nested-else
                       [:evaluation :region :if-syntax-id])))
        (is (= :then
               (get-in nested-then
                       [:evaluation :region :parent-branch-role])))
        (let [control-index (control-flow-index artifact)
              outer-if-record (get control-index (:node-id if-node))
              nested-if-record (get control-index (:node-id nested-if))
              outer-do-record (get control-index (:node-id outer-do))
              then-do-record (get control-index (:node-id then-do))]
          (is (= (:node-id then-do)
                 (get-in outer-if-record [:branches 0 :core-node-id])))
          (is (= (:node-id else-do)
                 (get-in outer-if-record [:branches 1 :core-node-id])))
          (is (= (:node-id nested-if)
                 (last (:ordered-child-node-ids then-do-record))))
          (is (= (:node-id six-node)
                 (get-in outer-do-record
                         [:result-policy :core-node-id])))
          (is (= [(:node-id nested-then) (:node-id nested-else)]
                 (mapv :core-node-id (:branches nested-if-record)))))
        (doseq [node [outer-do one-node if-node condition-node
                      then-do two-node nested-if nested-condition
                      nested-then nested-else else-do six-node]
                :when node]
          (assert-source-origin node))))))

(deftest sh07-b1-truthiness-shapes-retain-condition-values
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "control-flow-truthiness" extension)
          index (node-index artifact)
          expectations
          {"nil-is-false" [:nil nil]
           "false-is-false" [:boolean false]
           "zero-is-truthy" [:integer 0]
           "empty-vector-is-truthy" [:vector []]}]
      (assert-control-flow-reference-integrity artifact)
      (doseq [[name [literal-kind value]] expectations]
        (testing (str extension " " name)
          (let [if-node (definition-value-node artifact name)
                condition-node (get index (first (:children if-node)))]
            (is (= :if (:core-form if-node)))
            (is (= {:false-values [:nil :false] :other-values :true}
                   (get-in if-node [:attributes :truthiness])))
            (if (= literal-kind :vector)
              (do
                (is (= :collection-literal (:core-form condition-node)))
                (is (= literal-kind
                       (get-in condition-node
                               [:attributes :literal-kind])))
                (is (= value
                       (get-in condition-node
                               [:attributes :source-value]))))
              (do
                (is (= :literal (:core-form condition-node)))
                (is (= literal-kind
                       (get-in condition-node
                               [:attributes :literal-kind])))
                (is (= value
                       (get-in condition-node [:attributes :value])))))))))))

(deftest sh07-b1-malformed-control-flow-fails-closed
  (doseq [basename rejected-basenames
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (nil? (:raw-host-error result)))
        (is (map? diagnostic))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= "C6-CORE-SHAPE" (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= core-shape-remediation (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))
        (is (= (get rejected-reasons basename)
               (get-in diagnostic [:facts :reason])))
        (when (= basename "nested-def")
          (is (= :nested-expression
                 (get-in diagnostic
                         [:facts :rule-specific
                          :definition-context])))
          (is (= :top-level
                 (get-in diagnostic
                         [:facts :rule-specific
                          :required-definition-context]))))))))

(deftest sh07-b1-evaluation-child-and-origin-mutations-fail-replay
  (let [artifact
        (file-artifact "accepted" "control-flow-order" ".gravity")
        do-node
        (definition-value-node artifact "nested-order")
        index
        (node-index artifact)
        if-node
        (get index (second (:children do-node)))
        nested-if-node
        (let [then-do (get index (second (:children if-node)))]
          (get index (last (:children then-do))))
        alternate-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        mutations
        {"evaluation metadata"
         (update-node
          artifact (:node-id if-node)
          #(assoc-in % [:evaluation :kind] :evaluate-all-branches))

         "child reference"
         (update-node
          artifact (:node-id if-node)
          #(assoc % :children
                  (assoc (:children %) 0 alternate-id)))

         "source origin"
         (update-node
          artifact (:node-id if-node)
          #(assoc-in % [:source :origin-chain]
                     [{:artifact :gravity/substituted-origin}]))

         "control-flow substitution"
         (update-control-flow-record
          artifact (:node-id if-node)
          #(assoc % :condition-node-id alternate-id))

         "control-flow branch reorder"
         (update-control-flow-record
          artifact (:node-id nested-if-node)
          #(update % :branches
                   (fn [branches] (vec (reverse branches)))))

         "do evaluation reorder"
         (update-control-flow-record
          artifact (:node-id do-node)
          #(update % :ordered-child-node-ids
                   (fn [children] (vec (reverse children)))))

         "branch exclusivity metadata"
         (update-control-flow-record
          artifact (:node-id if-node)
          #(assoc % :branch-exclusivity :both))}]
    (is (map? if-node))
    (is (map? nested-if-node))
    (is (map? do-node))
    (doseq [[label mutation] mutations]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               mutation artifact {:status :passed})
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :authoritative-products-replay?)))))
    (let [public-report
          ((required-var 'sh07-core-artifact-verification)
           (get mutations "evaluation metadata"))]
      (is (= :gravity/sh07-core-artifact-verification
             (:artifact public-report)))
      (is (= :failed (:status public-report)))
      (is (some #{:canonical-core-replays?}
                (:failed-checks public-report))))))
