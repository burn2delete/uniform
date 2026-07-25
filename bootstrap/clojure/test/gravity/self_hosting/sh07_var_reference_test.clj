(ns gravity.self-hosting.sh07-var-reference-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_var_reference_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B5 test source is not on the classpath"
                      {:id "SH07-B5-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B5-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b5")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["same-namespace-var-object" "ordered-var-objects"])
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private verify-remediation
  "Replay the Gravity template and bind every digest ordinal exactly once.")
(def ^:private gap-remediation
  "Use only the declared bounded SH-07 core subset; defer unsupported lowering families to their owning slices.")
(def ^:private rejected-oracles
  {"missing-var-operand"
   {:rule "C6-CORE-SHAPE" :reason :var-arity
    :remediation shape-remediation}
   "extra-var-operand"
   {:rule "C6-CORE-SHAPE" :reason :var-arity
    :remediation shape-remediation}
   "non-symbol-var-operand"
   {:rule "C6-CORE-SHAPE" :reason :var-symbol-required
    :remediation shape-remediation}
   "lexical-var-target"
   {:rule "C6-VERIFY"
    :reason :var-top-level-namespace-binding-required
    :remediation verify-remediation}
   "qualified-var-reference"
   {:rule "C6-LOWERING-GAP"
    :reason :qualified-var-reference-deferred
    :remediation gap-remediation}})
(def ^:private var-reference-keys
  #{:ordinal :core-node-id :form-id :syntax-id
    :operand-form-id :operand-syntax-id :symbol
    :binding-id :binding-class :definition-kind :object-kind
    :upstream-binding-id :namespace :definition-syntax-id
    :definition-artifact-id :object-policy :resolution-order
    :evaluated-children :evaluation-order
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id :profile-legality})
(def ^:private var-attribute-keys
  #{:symbol :binding-id :binding-class :definition-kind :object-kind
    :upstream-binding-id :namespace :definition-syntax-id
    :definition-artifact-id :operand-form-id :operand-syntax-id
    :object-policy :resolution-order
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id :profile-legality})

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
        "Required SH-07-B5 coordinator adapter is absent"
        {:id "SH07-B5-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
          'sh07-core-from-authenticated-request
          '[resolution-artifact authenticated-request]
          'sh07-core-run-request-for-test '[authenticated-request]}}))))

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
    (get (second clause) :sh07-b5)))

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
       (ex-info "SH-07-B5 records are not uniquely identifiable"
                {:id "SH07-B5-AMBIGUOUS-INDEX"
                 :key key-name
                 :record-count (count records)
                 :unique-count (count index)})))
    index))

(defn- node-index
  [artifact]
  (exactly-once-index (:nodes (core artifact)) :node-id))

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

(defn- update-request-binding
  [request binding-id update-function]
  (update request :binding-table
          (fn [bindings]
            (mapv (fn [binding]
                    (if (= binding-id (:binding-id binding))
                      (update-function binding)
                      binding))
                  bindings))))

(defn- update-request-resolution
  [request syntax-id update-function]
  (update request :resolution-table
          (fn [resolutions]
            (mapv (fn [resolution]
                    (if (= syntax-id (:reference-syntax-id resolution))
                      (update-function resolution)
                      resolution))
                  resolutions))))

(deftest sh07-b5-fixtures-are-complete-paired-and-path-neutral
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-oracles)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames
                     (keys rejected-oracles))]
    (is (= (seq (source-bytes
                 (fixture-path family basename ".gravity")))
           (seq (source-bytes
                 (fixture-path family basename ".qst"))))))
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:var-references (core gravity))
               (:var-references (core qst))))
        (is (not= (get-in gravity [:provenance :source-path])
                  (get-in qst [:provenance :source-path]))))))
  (let [fixture
        (fixture-path "accepted" "same-namespace-var-object" ".gravity")
        text (source-text fixture)
        left (source-artifact "/tmp/sh07-b5-left/var.gravity" text)
        right (source-artifact "/tmp/sh07-b5-right/var.qst" text)]
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= "/tmp/sh07-b5-left/var.gravity"
           (get-in left [:provenance :source-path])))
    (is (= "/tmp/sh07-b5-right/var.qst"
           (get-in right [:provenance :source-path])))
    (is (= "/tmp/sh07-b5-left/var.gravity"
           (get-in left
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))
    (is (= "/tmp/sh07-b5-right/var.qst"
           (get-in right
                   [:gravity-core-boundary :canonical-core-artifact
                    :provenance :actual-source-path])))))

(deftest sh07-b5-var-objects-use-authenticated-sh06-bindings-without-evaluation
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:var-references core-artifact)
          nodes (node-index artifact)
          bindings
          (exactly-once-index (:binding-table (request artifact))
                              :binding-id)
          resolutions
          (exactly-once-index (:resolution-table (request artifact))
                              :reference-syntax-id)]
      (testing (str basename extension)
        (is (seq records))
        (is (= 6 (:schema-version (request artifact))))
        (is (= :sh07-b5-meta-jvm-core (:scope (request artifact))))
        (is (= records (:var-references (identity-input artifact))))
        (is (= (vec (range (count records))) (mapv :ordinal records)))
        (doseq [record records]
          (let [node (get nodes (:core-node-id record))
                binding (get bindings (:binding-id record))
                resolution
                (get resolutions (:operand-syntax-id record))]
            (is (= var-reference-keys (set (keys record))))
            (is (= :var (:core-form node)))
            (is (= [] (:children node)))
            (is (= :no-evaluation (get-in node [:evaluation :kind])))
            (is (= [] (get-in node [:evaluation :order])))
            (is (= var-attribute-keys
                   (set (keys (:attributes node)))))
            (is (= :namespace (:binding-class record)
                   (:binding-class binding)))
            (is (contains? #{:var :function}
                           (:definition-kind record)))
            (is (= (:kind binding) (:definition-kind record)))
            (is (= :top-level-var (:object-kind record)
                   (get-in node [:attributes :object-kind])))
            (is (= (get-in (request artifact) [:module :namespace])
                   (:namespace record)))
            (is (= (:upstream-binding-id binding)
                   (:upstream-binding-id resolution)
                   (:upstream-binding-id record)))
            (is (= :current-namespace-binding
                   (:resolution-order resolution)
                   (:resolution-order record)
                   (get-in node [:attributes :resolution-order])))
            (is (= (:definition-artifact-id binding)
                   (:definition-artifact-id record)
                   (get-in node
                           [:attributes :definition-artifact-id])))
            (is (= [(:binding-id record)]
                   (:resolved-binding-ids node)))
            (is (= (get-in (request artifact)
                           [:lineage :authenticated-sh06-artifact-id])
                   (:authenticated-sh06-artifact-id record)
                   (get-in node
                           [:attributes
                            :authenticated-sh06-artifact-id])))
            (is (= (get-in (request artifact)
                           [:lineage :sh06-semantic-projection-id])
                   (:sh06-semantic-projection-id record)
                   (get-in node
                           [:attributes
                            :sh06-semantic-projection-id])))
            (is (= :reference-without-dereference-or-execution
                   (:object-policy record)))
            (is (= [] (:evaluated-children record)
                   (:evaluation-order record)))
            (is (= :pending-sh09 (:profile-legality record)
                   (get-in node [:attributes :profile-legality])))))))))

(deftest sh07-b5-function-definition-still-produces-a-top-level-var-object
  (doseq [extension extensions]
    (let [artifact
          (file-artifact
           "accepted" "same-namespace-var-object" extension)
          record
          (first
           (filter #(= 'callable (:symbol %))
                   (:var-references (core artifact))))
          node (get (node-index artifact) (:core-node-id record))]
      (is (= :function (:definition-kind record)))
      (is (= :top-level-var (:object-kind record)))
      (is (= :top-level-var
             (get-in node [:attributes :object-kind])))
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b5-var-reference-order-is-deterministic
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "ordered-var-objects" extension)
          records (:var-references (core artifact))]
      (is (= ['second-target 'first-target] (mapv :symbol records)))
      (is (= [0 1] (mapv :ordinal records)))
      (is (= (:var-references (core artifact))
             (get-in artifact
                     [:gravity-core-boundary :raw-template-result
                      :core-template :var-references]))))))

(deftest sh07-b5-rejections-are-structured-and-oracle-bound
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

(deftest sh07-b5-definition-kind-identity-and-resolution-order-substitution-fail-closed
  (let [artifact
        (file-artifact
         "accepted" "same-namespace-var-object" ".gravity")
        record (first (:var-references (core artifact)))
        authenticated (request artifact)
        wrong-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        mutations
        {"kind substitution"
         (update-request-binding
          authenticated (:binding-id record)
          #(assoc % :kind :type))
         "binding identity substitution"
         (update-request-binding
          authenticated (:binding-id record)
          #(assoc % :upstream-binding-id wrong-id))
         "resolution identity substitution"
         (update-request-resolution
          authenticated (:operand-syntax-id record)
          #(assoc % :upstream-binding-id wrong-id))
         "qualified resolution substitution"
         (update-request-resolution
          authenticated (:operand-syntax-id record)
          #(assoc % :resolution-order
                  :fully-qualified-namespace-binding))
         "alias resolution substitution"
         (update-request-resolution
          authenticated (:operand-syntax-id record)
          #(assoc % :resolution-order
                  :alias-qualified-required-binding))}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var 'sh07-core-run-request-for-test) mutation))
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= :core-lowering (:stage diagnostic)))
          (is (= true (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b5-stale-legal-definition-kind-fails-authenticated-projection-replay
  (let [artifact
        (file-artifact
         "accepted" "same-namespace-var-object" ".gravity")
        record (first (:var-references (core artifact)))
        authenticated (request artifact)
        stale-kind
        (if (= :var (:definition-kind record)) :function :var)
        mutation
        (update-request-binding
         authenticated (:binding-id record)
         #(assoc % :kind stale-kind))
        result
        (diagnostic-result
         #((required-var 'sh07-core-from-authenticated-request)
           (:sh06-resolution-artifact artifact)
           mutation))
        diagnostic (diagnostic-data result)]
    (is (contains? #{:var :function} stale-kind))
    (is (not= authenticated mutation))
    (is (nil? (:raw-host-error result)))
    (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :core-lowering (:stage diagnostic)))
    (is (= :authenticated-sh06-projection-mismatch
           (get-in diagnostic [:facts :reason])))
    (is (= true (get-in diagnostic [:facts :fail-closed])))))

(deftest sh07-b5-var-evidence-mutation-fails-replay
  (let [artifact
        (file-artifact "accepted" "ordered-var-objects" ".gravity")
        mutation
        (update-in
         artifact
         [:gravity-core-boundary :canonical-core-artifact
          :var-references]
         #(assoc-in % [0 :profile-legality] :accepted-before-sh09))
        checks
        ((required-var 'sh07-core-verification-checks)
         mutation artifact {:status :passed})
        failed
        (set (for [[check passed?] checks
                   :when (not (true? passed?))]
               check))]
    (is (not= artifact mutation))
    (is (contains? failed :canonical-core-replays?))
    (is (contains? failed :authoritative-products-replay?))
    (is (= :failed
           (:status
            ((required-var 'sh07-core-artifact-verification)
             mutation))))))
