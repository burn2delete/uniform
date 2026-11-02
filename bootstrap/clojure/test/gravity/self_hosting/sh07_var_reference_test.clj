(ns gravity.self-hosting.sh07-var-reference-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-authoritative-runner :as authoritative]))

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
  ["same-namespace-var-object" "ordered-var-objects"
   "qualified-same-namespace-var-object"])
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private var-target-remediation
  "Use var only with a same-namespace top-level var or function binding; lexical bindings are not var objects.")
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
    :remediation var-target-remediation}
   "alias-var-reference"
   {:rule "C6-LOWERING-GAP"
    :reason :qualified-var-reference-deferred
    :remediation gap-remediation}
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
          'sh07-core-run-request-for-test
          '[resolution-artifact authenticated-request]}}))))

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
          identity-records
          (:var-references (identity-input artifact))
          nodes (node-index artifact)
          bindings
          (exactly-once-index (:binding-table (request artifact))
                              :binding-id)
          resolutions
          (exactly-once-index (:resolution-table (request artifact))
                              :reference-syntax-id)]
      (testing (str basename extension)
        (is (seq records))
        (is (= 15 (:schema-version (request artifact))))
        (is (= :sh07-b15-keyword-map-lookup (:scope (request artifact))))
        (is (=
             (mapv #(dissoc
                     % :upstream-binding-id :definition-artifact-id
                     :authenticated-sh06-artifact-id)
                   records)
             (mapv #(dissoc
                     % :upstream-binding-id :definition-artifact-id
                     :authenticated-sh06-artifact-id)
                   identity-records)))
        (doseq [[record identity-record]
                (map vector records identity-records)]
          (let [semantic-projection-id
                (:sh06-semantic-projection-id record)]
            (is (= (:binding-id record)
                   (:upstream-binding-id identity-record)))
            (is (= semantic-projection-id
                   (:definition-artifact-id identity-record)
                   (:authenticated-sh06-artifact-id identity-record)))))
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
            (is (contains? #{:current-namespace-binding
                             :fully-qualified-namespace-binding}
                           (:resolution-order resolution)))
            (is (= (:resolution-order resolution)
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
          records (:var-references (core artifact))
          template-records
          (get-in artifact
                  [:gravity-core-boundary :raw-template-result
                   :core-template :var-references])]
      (is (= ['second-target 'first-target] (mapv :symbol records)))
      (is (= [0 1] (mapv :ordinal records)))
      (is (= (mapv #(dissoc % :core-node-id) records)
             (mapv #(dissoc % :core-node-id) template-records)))
      (is (every? #(re-matches #"sha256:[0-9a-f]{64}"
                               (:core-node-id %))
                  records))
      (is (every?
           #(= {:artifact :gravity/sh07-internal-digest-reference
                :authority :sh07-digest-resolver
                :schema-version 1}
               (dissoc (:core-node-id %) :ordinal))
           template-records))
      (is (= (mapv #(get-in % [:core-node-id :ordinal])
                   template-records)
             (vec (range
                   (first
                    (map #(get-in % [:core-node-id :ordinal])
                         template-records))
                   (+
                    (first
                     (map #(get-in % [:core-node-id :ordinal])
                           template-records))
                    (count template-records)))))))))

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
        (is (= :meta (:profile diagnostic)))
        (is (= :jvm (:target diagnostic)))
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
          #(assoc % :upstream-binding-id wrong-id))}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var 'sh07-core-run-request-for-test)
                 (:sh06-resolution-artifact artifact) mutation))
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

(deftest sh07-b5-stale-resolution-order-fails-authenticated-projection-replay
  (let [artifact
        (file-artifact
         "accepted" "same-namespace-var-object" ".gravity")
        record (first (:var-references (core artifact)))
        authenticated (request artifact)]
    (doseq [resolution-order
            [:fully-qualified-namespace-binding
             :alias-qualified-required-binding]]
      (let [mutation
            (update-request-resolution
             authenticated (:operand-syntax-id record)
             #(assoc % :resolution-order resolution-order))
            result
            (diagnostic-result
             #((required-var 'sh07-core-from-authenticated-request)
               (:sh06-resolution-artifact artifact)
               mutation))
            diagnostic (diagnostic-data result)]
        (is (not= authenticated mutation))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :authenticated-sh06-projection-mismatch
               (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

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

(deftest sh07-b5-qualified-current-namespace-preserves-resolution-and-mapping
  (let [artifact (file-artifact "accepted" "qualified-same-namespace-var-object" ".gravity")
        canonical (core artifact)
        records (:var-references canonical)
        nodes (node-index artifact)
        bindings (exactly-once-index (:binding-table (request artifact)) :binding-id)
        rows (get-in artifact [:gravity-core-boundary :digest-requests])
        mapping (get-in (last rows) [:preimage :records])
        plan-binding (get-in artifact [:gravity-core-boundary :plan-binding])]
    (is (= ['demo.value/answer 'demo.value/callable 'demo.value/answer]
           (mapv :symbol records)))
    (is (= [:var :function :var] (mapv :definition-kind records)))
    (is (= :development-unadmitted (:revision-status plan-binding)))
    (is (false? (:self-hosted? plan-binding)))
    (is (= :passed (:status (bootstrap/sh07-core-artifact-verification artifact))))
    (doseq [record records]
      (let [node (get nodes (:core-node-id record))
            target (get bindings (:binding-id record))
            row (first (filter #(= (:operand-form-id record) (get-in % [:source :form-id])) mapping))]
        (is (= :fully-qualified-namespace-binding (:resolution-order record)
               (get-in node [:attributes :resolution-order])))
        (is (= :namespace (:binding-class target)))
        (is (= 'demo.value (:namespace target) (:namespace record)))
        (is (= [] (:children node) (:evaluated-children record)))
        (is (= :consumed (:disposition row)))
        (is (= :var-target (:reason row)))
        (is (= [] (:emitted-node-ids row)))))))

(deftest sh07-b5-qualified-var-forged-order-spelling-and-id-fail-authentication
  (let [artifact (file-artifact "accepted" "qualified-same-namespace-var-object" ".gravity")
        authenticated (request artifact)
        record (first (:var-references (core artifact)))]
    (doseq [[field value] [[:resolution-order :current-namespace-binding]
                         [:resolution-order :alias-qualified-required-binding]
                         [:symbol 'answer]
                         [:binding-id "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"]]]
      (let [mutation (update-request-resolution authenticated (:operand-syntax-id record) #(assoc % field value))
            result (diagnostic-result #(bootstrap/sh07-core-from-authenticated-request
                                        (:sh06-resolution-artifact artifact) mutation))
            diagnostic (diagnostic-data result)]
        (is (nil? (:raw-host-error result)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :authenticated-sh06-request-membership-mismatch (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))
    (let [mutant (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact :var-references 0 :resolution-order]
                           :current-namespace-binding)
          checks (bootstrap/sh07-core-verification-checks mutant artifact {:status :passed})]
      (is (false? (:canonical-core-replays? checks)))
      (is (false? (:authoritative-products-replay? checks))))))

(deftest sh07-development-binding-retains-complete-explicit-migration-pins
  (let [pinned {:source-byte-count bootstrap/sh07-core-expected-source-byte-count
                :source-content-hash bootstrap/sh07-core-expected-source-content-hash
                :plan-semantic-hash bootstrap/sh07-core-expected-plan-semantic-hash
                :functions-semantic-hash bootstrap/sh07-core-expected-functions-semantic-hash
                :function-count bootstrap/sh07-core-expected-function-count
                :function-names-hash bootstrap/sh07-core-expected-function-names-hash
                :function-shapes-hash bootstrap/sh07-core-expected-function-shapes-hash
                :public-function-hashes bootstrap/sh07-core-public-function-hashes
                :public-function-shapes bootstrap/sh07-core-public-function-shapes}]
    ;; This is a tuple-validator fixture, not compiled-code admission evidence.
    (is (= pinned (bootstrap/sh07-core-assert-pinned-revision! pinned)))
    (doseq [field (keys pinned)]
      (let [result (diagnostic-result #(bootstrap/sh07-core-assert-pinned-revision! (assoc pinned field ::drift)))]
        (is (= :unadmitted-checked-core-revision (get-in result [:exception-data :reason])))))
    (let [current (bootstrap/sh07-core-current-binding!)]
      (is (= :development-unadmitted (:revision-status current)))
      (is (false? (:self-hosted? current)))
      (is (= :unadmitted-checked-core-revision
             (get-in (diagnostic-result #(bootstrap/sh07-core-assert-pinned-revision!)) [:exception-data :reason]))))))

(deftest sh07-explicit-proof-entrypoints-check-migration-before-construction
  (let [failure (ex-info "Unadmitted test revision" {:reason ::unadmitted})
        called (atom false)]
    (with-redefs [bootstrap/sh07-core-assert-pinned-revision! (fn [] (throw failure))]
      (is (identical? failure
                     (try (bootstrap/sh07-core-file-proof-transaction
                           "/must-not-read.gravity" (fn [_] (reset! called true)))
                          (catch clojure.lang.ExceptionInfo error error))))
      (is (identical? failure
                     (try ((ns-resolve 'gravity.self-hosting.sh07-authoritative-runner 'run-module)
                           {:authoritative-modules {:development-fixture "/must-not-read.gravity"}}
                           "development-fixture")
                          (catch clojure.lang.ExceptionInfo error error)))))
    (is (false? @called))))

(deftest sh07-current-binding-invalidates-edited-bytes-and-observes-realization
  (let [directory (java.nio.file.Files/createTempDirectory "sh07-current-source-" (make-array java.nio.file.attribute.FileAttribute 0))
        source (.resolve directory "checked_core.gravity")
        compiler (.resolve directory "compiler.gravity")
        emitter (.resolve directory "emitter.gravity")
        write! (fn [file value] (java.nio.file.Files/write file (.getBytes value "UTF-8") (make-array java.nio.file.OpenOption 0)))
        builds (atom 0)
        cached @(ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding)]
    (try
      (doseq [file [source compiler emitter]] (write! file "first"))
      (with-redefs-fn {(ns-resolve 'gravity.bootstrap 'sh07-core-development-binding) (atom nil)}
        (fn []
      (with-redefs [bootstrap/sh07-core-source-path (constantly (str source))
                    bootstrap/c-backend-resolve-p15-s23-compiler-source-path (constantly (str compiler))
                    bootstrap/p15-s23-stage2-compiler-artifact-source-path (constantly (str emitter))
                    bootstrap/sh07-core-build-binding!
                    (fn [] (swap! builds inc)
                      {:source-content-hash (str "sha256:" (bootstrap/sha256-bytes-hex (java.nio.file.Files/readAllBytes source)))})]
        (is (instance? clojure.lang.IPending cached))
        (is (false? (realized? cached)))
        (let [first-binding (bootstrap/sh07-core-current-binding!)
              modified (java.nio.file.Files/getLastModifiedTime source (make-array java.nio.file.LinkOption 0))]
          (is (true? (realized? cached)))
          (is (identical? first-binding (bootstrap/sh07-core-current-binding!)))
          (is (= 1 @builds))
          (write! source "other")
          (java.nio.file.Files/setLastModifiedTime source modified)
          (is (false? (realized? cached)))
          (is (not= first-binding (bootstrap/sh07-core-current-binding!)))
          (is (= 2 @builds))
          (write! emitter "other")
          (is (= :checked-core-toolchain-drift
                 (get-in (diagnostic-result #(bootstrap/sh07-core-current-binding!)) [:exception-data :reason])))
          (is (= 2 @builds))))))
      (finally
        (doseq [file [source compiler emitter directory]] (java.nio.file.Files/deleteIfExists file))))))

(deftest sh07-b5-qualified-var-development-kernel
  ;; Normalized unit inputs exercise Gravity code; these are not authenticated
  ;; SH-06 provenance evidence. The composition tests above cover that boundary.
  (let [plan (:plan (bootstrap/sh07-core-current-binding!))
        invoke (fn [function arguments]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-sh07-development-fixture
                   :compiler-artifact-plan? true}
                  plan function arguments))
        sha (fn [n] (str "sha256:" (format "%064x" n)))
        operand {:form-id (sha 1) :syntax-id (sha 2) :kind :symbol
                 :value 'demo.value/answer}
        form {:form-id (sha 3) :syntax-id (sha 4)
              :child-form-ids [(sha 5) (:form-id operand)]
              :metadata {} :generated-origin [] :origin-chain []}
        target {:binding-id (sha 6) :binding-class :namespace :kind :var
                :namespace 'demo.value :upstream-binding-id (sha 7)
                :definition-syntax-id (sha 8) :definition-artifact-id (sha 9)}
        resolution {:symbol (:value operand) :position :expression
                    :binding-id (:binding-id target)
                    :upstream-binding-id (:upstream-binding-id target)
                    :resolution-order :fully-qualified-namespace-binding}
        request {:module {:namespace 'demo.value :profile :meta :target :jvm
                          :effects #{} :capabilities #{} :safety :safe}
                 :lineage {:authenticated-sh06-artifact-id (sha 10)
                           :sh06-semantic-projection-id (sha 11)}
                 :internal-indexes
                 {:forms {:by-id {(:form-id operand) operand}}
                  :bindings {:by-id {(:binding-id target) target}}
                  :resolutions {:by-reference {(:syntax-id operand) resolution}}}}
        lower (fn [input]
                (invoke 'sh07-lower-var
                        [input (invoke 'sh07-empty-state []) form]))]
    (testing "development-unadmitted normalized var/function inputs"
      (doseq [kind [:var :function]]
        (let [input (assoc-in request [:internal-indexes :bindings :by-id (:binding-id target) :kind] kind)
              result (lower input)
              node (first (get-in result [:state :nodes]))
              record (first (get-in result [:state :var-references]))
              resolved (invoke 'sh07-resolve-var-reference [record [(sha 12)]])]
          (is (= :accepted (:status result)))
          (is (= kind (:definition-kind record) (get-in node [:attributes :definition-kind])))
          (is (= :fully-qualified-namespace-binding (:resolution-order record)
                 (get-in node [:attributes :resolution-order])))
          (is (= [] (:children node) (:evaluated-children record)))
          (is (= :no-evaluation (get-in node [:evaluation :kind])))
          (is (= (:binding-id target) (:binding-id record)))
          (is (true? (invoke 'sh07-resolved-b5-node-valid? [node])))
          (is (= :accepted (:status resolved)))
          (is (= (sha 12) (get-in resolved [:value :core-node-id]))))))
    (testing "development-only malformed/deferred joins fail closed"
      (doseq [[input rule reason]
              [[(assoc-in request [:internal-indexes :bindings :by-id (:binding-id target) :namespace] 'foreign.value)
                "C6-LOWERING-GAP" :qualified-var-reference-deferred]
               [(assoc-in request [:internal-indexes :resolutions :by-reference (:syntax-id operand) :resolution-order]
                          :alias-qualified-required-binding)
                "C6-LOWERING-GAP" :qualified-var-reference-deferred]
               [(assoc-in request [:internal-indexes :bindings :by-id (:binding-id target) :binding-class] :lexical)
                "C6-VERIFY" :var-top-level-namespace-binding-required]
               [(assoc-in request [:internal-indexes :resolutions :by-reference (:syntax-id operand) :symbol] 'answer)
                "C6-VERIFY" :sh06-resolution-reference-mismatch]
               [(assoc-in request [:internal-indexes :resolutions :by-reference (:syntax-id operand) :binding-id] (sha 13))
                "C6-VERIFY" :sh06-resolution-binding-mismatch]
               [(assoc-in request [:internal-indexes :resolutions :by-reference (:syntax-id operand) :upstream-binding-id] (sha 13))
                "C6-VERIFY" :var-same-namespace-binding-required]]]
        (let [result (lower input)]
          (is (= :rejected (:status result)))
          (is (= rule (:rule result)))
          (is (= reason (get-in result [:facts :reason]))))))))

(deftest sh07-development-construction-rejects-revision-races
  ;; Cheap coordinator seams: no SH-06 construction, compiled plan, or proof.
  (let [key {:source ["development-fixture" "sha256:a"] :toolchain []}
        binding {:source-content-hash "sha256:a" :plan :development-fixture}
        observed (atom key)
        executions (atom [])
        redefs {(ns-resolve 'gravity.bootstrap 'sh07-core-current-source-key) #(deref observed)
                (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding) (delay binding)
                #'bootstrap/sh07-core-request-preflight! (constantly nil)
                #'bootstrap/sh07-core-digest-requests (fn [& _] [])
                #'bootstrap/sh07-core-resolve-result (fn [& _] {})
                #'bootstrap/sh07-core-canonical-artifact identity
                #'bootstrap/sh07-core-execute!
                (fn [_ function _]
                  (swap! executions conj (var-get (ns-resolve 'gravity.bootstrap '*sh07-core-execution-binding*)))
                  (if (= function 'sh07-build-core-template)
                    {:status :accepted :core-template {} :digest-requests []}
                    {:status :passed}))}]
    (with-redefs-fn redefs
      (fn []
        (let [run (bootstrap/sh07-core-run-structural-request-for-test {})]
          (is (= #{:raw-template-result :canonical-core-artifact :digest-requests
                   :resolved-digests :template-verification :resolved-verification}
                 (set (keys run))))
          (is (= [binding binding binding] @executions)))
        (with-redefs [bootstrap/sh07-core-canonical-artifact
                      (fn [value] (reset! observed (assoc key :source ["development-fixture" "sha256:b"])) value)]
          (is (= :checked-core-operation-revision-drift
                 (get-in (diagnostic-result #(bootstrap/sh07-core-run-structural-request-for-test {}))
                         [:exception-data :reason]))))
        (reset! observed key)
        (with-redefs [bootstrap/sh07-core-run-request-for-test
                      (fn [& _] {:canonical-core-artifact {:artifact-id :development-fixture}
                                 :plan-binding (dissoc binding :plan)})
                      bootstrap/sh06-resolution-artifact-verification (constantly {:status :passed})
                      bootstrap/sh07-core-proof-from-checks identity
                      bootstrap/sh07-core-verification-checks
                      (fn [& _] (reset! observed (assoc key :source ["development-fixture" "sha256:b"])) {})]
          (is (= :checked-core-operation-revision-drift
                 (get-in (diagnostic-result #(bootstrap/sh07-core-from-authenticated-request {} {}))
                         [:exception-data :reason]))))))))

(deftest sh07-current-binding-captures-distinct-compiler-and-emitter
  (let [key-var (ns-resolve 'gravity.bootstrap 'sh07-core-current-source-key)
        key ((var-get key-var))
        dependencies (:toolchain key)]
    (is (= 2 (count dependencies)))
    (is (= 2 (count (set (map first dependencies)))))
    (is (= [(.getCanonicalPath (io/file (bootstrap/c-backend-resolve-p15-s23-compiler-source-path)))
            (.getCanonicalPath (io/file (bootstrap/p15-s23-stage2-compiler-artifact-source-path)))]
           (mapv first dependencies)))
    (doseq [index (range 2)]
      (let [observed (atom key)
            cache (atom {:key key :binding {:source-content-hash (second (:source key))}})]
        (with-redefs-fn {key-var #(deref observed)
                        (ns-resolve 'gravity.bootstrap 'sh07-core-development-binding) cache}
          #(do (is (= (:binding @cache) (bootstrap/sh07-core-current-binding!)))
               (swap! observed assoc-in [:toolchain index 1] "sha256:changed")
               (is (= :checked-core-toolchain-drift
                      (get-in (diagnostic-result (fn [] (bootstrap/sh07-core-current-binding!)))
                              [:exception-data :reason])))))))))

(deftest sh07-current-binding-rechecks-all-inputs-before-publication
  (doseq [[field changed reason]
          [[:source ["development-fixture" "sha256:b"] :checked-core-source-drift]
           [:toolchain [["compiler.gravity" "sha256:b"]] :checked-core-toolchain-drift]]]
    (let [initial {:source ["development-fixture" "sha256:a"] :toolchain []}
          observed (atom initial)
          cache (atom nil)]
      (with-redefs-fn
        {(ns-resolve 'gravity.bootstrap 'sh07-core-current-source-key) #(deref observed)
         (ns-resolve 'gravity.bootstrap 'sh07-core-development-binding) cache
         #'bootstrap/sh07-core-build-binding!
         (fn [] (reset! observed (assoc initial field changed))
           {:source-content-hash "sha256:a"})}
        (fn []
          (is (= reason (get-in (diagnostic-result #(bootstrap/sh07-core-current-binding!))
                               [:exception-data :reason])))
          (is (nil? @cache)))))))

(deftest sh07-proof-snapshot-checks-the-exact-captured-revision
  (let [captured {:source-content-hash "sha256:unadmitted-snapshot"}
        seen (atom nil)
        stopped (ex-info "snapshot pin guard" {:reason ::snapshot-pin-guard})]
    (with-redefs-fn
      {(ns-resolve 'gravity.bootstrap 'sh05-macro-cached-binding) (delay {})
       (ns-resolve 'gravity.bootstrap 'sh06-resolution-cached-binding) (delay {})
       (ns-resolve 'gravity.bootstrap 'sh07-core-cached-binding) (delay captured)
       #'bootstrap/sh05-macro-read-pinned-source! (constantly {})
       #'bootstrap/sh06-resolution-read-pinned-source! (constantly {})
       #'bootstrap/sh07-core-assert-pinned-revision!
       (fn [actual] (reset! seen actual) (throw stopped))}
      (fn []
        (is (identical? stopped
                        (try ((ns-resolve 'gravity.bootstrap 'sh07-proof-transaction-core-snapshot))
                             (catch clojure.lang.ExceptionInfo error error))))
        (is (= captured @seen))))))
