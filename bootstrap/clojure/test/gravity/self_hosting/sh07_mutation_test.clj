(ns gravity.self-hosting.sh07-mutation-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_mutation_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B6 test source is not on the classpath"
                      {:id "SH07-B6-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B6-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b6")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["ordered-top-level-mutations"
   "function-var-mutation"
   "conditional-mutations"
   "nested-value-mutation"])
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private gap-remediation
  "Use only the declared bounded SH-07 core subset; defer unsupported lowering families to their owning slices.")
(def ^:private recur-remediation
  "Place recur in tail position inside the nearest loop or fixed-arity function target and pass exactly the target arity.")
(def ^:private rejected-oracles
  {"set-arity"
   {:rule "C6-CORE-SHAPE" :reason :set-arity
    :remediation shape-remediation}
   "non-symbol-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :non-symbol-set-target-deferred
    :remediation gap-remediation}
   "lexical-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :lexical-set-target-deferred
    :remediation gap-remediation}
   "qualified-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :qualified-set-target-deferred
    :remediation gap-remediation}
   "recur-in-set-value"
   {:rule "C6-VERIFY"
    :reason :recur-tail-position-required
    :semantic-rule "L2-RECUR-TARGET"
    :remediation recur-remediation}})
(def ^:private mutation-record-keys
  #{:ordinal :core-node-id :form-id :syntax-id
    :target-form-id :target-syntax-id :target-symbol
    :target-binding-id :target-binding-class
    :target-definition-kind :target-location
    :target-upstream-binding-id :target-namespace
    :target-definition-syntax-id
    :target-definition-artifact-id :resolution-order
    :value-core-node-id :target-evaluated-children
    :evaluated-children :evaluation-region
    :evaluation-owner-function-syntax-id
    :ordered-steps :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :ownership-legality :safety-classification})
(def ^:private mutation-attribute-keys
  #{:target-symbol :target-binding-id
    :target-binding-class :target-definition-kind
    :target-location :target-upstream-binding-id
    :target-namespace :target-definition-syntax-id
    :target-definition-artifact-id
    :target-form-id :target-syntax-id
    :resolution-order :target-evaluation
    :value-child-index :evaluation-order
    :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :ownership-legality :safety-classification})

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

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B6 coordinator adapter is absent"
        {:id "SH07-B6-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
          'sh07-core-from-authenticated-request
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
    (get (second clause) :sh07-b6)))

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
       (ex-info "SH-07-B6 records are not uniquely identifiable"
                {:id "SH07-B6-AMBIGUOUS-INDEX"
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

(deftest sh07-b6-fixtures-are-paired-complete-and-path-neutral
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
        (is (= (:mutations (core gravity))
               (:mutations (core qst))))
        (is (= 12 (:schema-version (request gravity))
               (:schema-version (request qst))))
        (is (= :sh07-b11-meta-jvm-core
               (:scope (request gravity))
               (:scope (request qst)))))))
  (let [fixture
        (fixture-path
         "accepted" "ordered-top-level-mutations" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b6-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/mutation.gravity")
        right-path (.resolve temp-root "right/mutation.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right
            ((required-var 'sh07-core-file-artifact) (str right-path))
            left-core (core left)
            right-core (core right)
            left-request (request left)
            right-request (request right)
            left-semantic-id
            (get-in left-request
                    [:lineage :sh06-semantic-projection-id])
            right-semantic-id
            (get-in right-request
                    [:lineage :sh06-semantic-projection-id])
            left-mutations (:mutations left-core)
            right-mutations (:mutations right-core)
            left-set-nodes
            (filterv #(= :set! (:core-form %)) (:nodes left-core))
            right-set-nodes
            (filterv #(= :set! (:core-form %)) (:nodes right-core))]
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= left-semantic-id right-semantic-id))
        (is (=
             (get-in left-request
                     [:lineage :authenticated-sh06-artifact-id])
             (get-in right-request
                     [:lineage :authenticated-sh06-artifact-id])))
        (is (not= (str left-path) (str right-path)))
        (is (= (mapv :node-id (:nodes left-core))
               (mapv :node-id (:nodes right-core))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in left-core [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in right-core [:provenance :actual-source-path])))
        (is (= (count left-mutations) (count right-mutations)))
        (is (= (count left-set-nodes) (count right-set-nodes)))
        (is (every?
             #(= (get-in left-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (:authenticated-sh06-artifact-id %))
             left-mutations))
        (is (every?
             #(= (get-in right-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (:authenticated-sh06-artifact-id %))
             right-mutations))
        (is (every?
             #(= (get-in left-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (get-in % [:attributes
                            :authenticated-sh06-artifact-id]))
             left-set-nodes))
        (is (every?
             #(= (get-in right-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (get-in % [:attributes
                            :authenticated-sh06-artifact-id]))
             right-set-nodes)))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b6-mutations-bind-authenticated-target-and-evaluate-only-value
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:mutations core-artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          source-map
          (exactly-once-index (:source-map core-artifact)
                              :core-node-id)
          forms
          (exactly-once-index (:forms (request artifact)) :form-id)
          bindings
          (exactly-once-index (:binding-table (request artifact))
                              :binding-id)
          resolutions
          (exactly-once-index (:resolution-table (request artifact))
                              :reference-syntax-id)]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (doseq [record records]
          (let [node (get nodes (:core-node-id record))
                binding (get bindings (:target-binding-id record))
                resolution
                (get resolutions (:target-syntax-id record))
                target-form (get forms (:target-form-id record))
                value-node-id (:value-core-node-id record)
                source-record (get source-map (:core-node-id record))
                attributes (:attributes node)
                shared-identity-keys
                [:target-symbol :target-binding-id
                 :target-binding-class :target-definition-kind
                 :target-location :target-upstream-binding-id
                 :target-namespace :target-definition-syntax-id
                 :target-definition-artifact-id
                 :target-form-id :target-syntax-id
                 :resolution-order
                 :authenticated-sh06-artifact-id
                 :sh06-semantic-projection-id
                 :type-legality :effect-registry-legality
                 :effect-profile-capability-legality
                 :ownership-legality :safety-classification]]
            (is (= mutation-record-keys (set (keys record))))
            (is (= mutation-attribute-keys
                   (set (keys attributes))))
            (is (= (select-keys record shared-identity-keys)
                   (select-keys attributes shared-identity-keys)))
            (is (= :set! (:core-form node)))
            (is (= (:form-id record)
                   (get-in node [:source :form-id])
                   (:form-id source-record)))
            (is (= (:syntax-id record)
                   (get-in node [:source :syntax-id])
                   (:syntax-id source-record)))
            (is (= [value-node-id] (:children node)))
            (is (= [(:target-binding-id record)]
                   (:resolved-binding-ids node)))
            (is (= :value-then-write
                   (get-in node [:evaluation :kind])))
            (is (= [{:index 0 :core-node-id value-node-id}]
                   (get-in node [:evaluation :order])))
            (is (= (:evaluation-region record)
                   (get-in node [:evaluation :region])))
            (is (= (:evaluation-owner-function-syntax-id record)
                   (get-in node
                           [:evaluation
                            :owner-function-syntax-id])))
            (is (= [] (:target-evaluated-children record)))
            (is (= [value-node-id] (:evaluated-children record)))
            (is (= [:evaluate-value :write-target :yield-unit]
                   (:ordered-steps record)))
            (is (= :unit (:result-policy record)
                   (get-in node [:attributes :result-policy])))
            (is (= :state/write (:required-effect record)
                   (get-in node [:attributes :required-effect])))
            (is (= :namespace (:binding-class binding)
                   (:target-binding-class record)))
            (is (= (:target-symbol record)
                   (:name binding)
                   (:symbol resolution)
                   (:value target-form)))
            (is (= (:target-binding-id record)
                   (:binding-id binding)
                   (:binding-id resolution)))
            (is (= (:target-syntax-id record)
                   (:syntax-id target-form)
                   (:reference-syntax-id resolution)))
            (is (= (:target-form-id record)
                   (:form-id target-form)))
            (is (= (:target-namespace record)
                   (:namespace binding)
                   (get-in (request artifact)
                           [:module :namespace])))
            (is (= (:target-definition-syntax-id record)
                   (:definition-syntax-id binding)))
            (is (contains? #{:var :function}
                           (:target-definition-kind record)))
            (is (= (:kind binding)
                   (:target-definition-kind record)))
            (is (= :top-level-var (:target-location record)))
            (is (= (:upstream-binding-id binding)
                   (:upstream-binding-id resolution)
                   (:target-upstream-binding-id record)))
            (is (= :current-namespace-binding
                   (:resolution-order resolution)
                   (:resolution-order record)))
            (is (= (:definition-artifact-id binding)
                   (:target-definition-artifact-id record)))
            (is (= (get-in (request artifact)
                           [:lineage
                            :authenticated-sh06-artifact-id])
                   (:authenticated-sh06-artifact-id record)))
            (is (= (get-in (request artifact)
                           [:lineage :sh06-semantic-projection-id])
                   (:sh06-semantic-projection-id record)))
            (is (= :pending-sh08 (:type-legality record)))
            (is (= :pending-sh09
                   (:effect-registry-legality record)
                   (:effect-profile-capability-legality record)))
            (is (= :pending-sh10 (:ownership-legality record)))
            (is (= :pending-sh11
                   (:safety-classification record)))))))))

(deftest sh07-b6-target-and-rhs-resolutions-are-distinct-and-consumed-once
  (doseq [extension extensions]
    (let [artifact
          (file-artifact
           "accepted" "ordered-top-level-mutations" extension)
          records (:mutations (core artifact))
          self-write
          (first
           (filter #(= 'first-target (:target-symbol %)) records))
          rhs-use
          (first
           (filter #(= 'first-target (:symbol %))
                   (:reference-uses (core artifact))))
          target-syntax-id (:target-syntax-id self-write)
          rhs-syntax-id (:syntax-id rhs-use)
          resolutions (:resolution-table (request artifact))]
      (is self-write)
      (is rhs-use)
      (is (not= target-syntax-id rhs-syntax-id))
      (is (= 1 (count (filter #(= target-syntax-id
                                  (:reference-syntax-id %))
                             resolutions))))
      (is (= 1 (count (filter #(= rhs-syntax-id
                                  (:reference-syntax-id %))
                             resolutions))))
      (is (= (:target-binding-id self-write)
             (:binding-id rhs-use))))))

(deftest sh07-b6-conditional-regions-and-function-var-kind-are-preserved
  (doseq [extension extensions]
    (let [conditional
          (file-artifact "accepted" "conditional-mutations" extension)
          records (:mutations (core conditional))
          regions (mapv :evaluation-region records)
          function-artifact
          (file-artifact "accepted" "function-var-mutation" extension)
          function-record
          (first (:mutations (core function-artifact)))]
      (is (= [:then :else] (mapv :role regions)))
      (is (every? #(= :conditional-branch (:kind %)) regions))
      (is (every? #(= :selected-exactly-once (:execution %))
                  regions))
      (is (= :function (:target-definition-kind function-record)))
      (is (= :top-level-var (:target-location function-record))))))

(deftest sh07-b6-nested-rhs-mutation-precedes-outer-write
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "nested-value-mutation" extension)
          records (:mutations (core artifact))
          nodes (exactly-once-index (:nodes (core artifact)) :node-id)
          source-map
          (exactly-once-index (:source-map (core artifact))
                              :core-node-id)
          inner (first records)
          outer (second records)
          inner-node (get nodes (:core-node-id inner))
          outer-node (get nodes (:core-node-id outer))]
      (is (= ['inner-target 'outer-target]
             (mapv :target-symbol records)))
      (is (= [0 1] (mapv :ordinal records)))
      (is (= (:core-node-id inner)
             (:value-core-node-id outer)))
      (is (= [(:core-node-id inner)] (:children outer-node)))
      (is (= [(:core-node-id inner)]
             (:evaluated-children outer)))
      (is (= [] (:target-evaluated-children outer)))
      (is (= [:evaluate-value :write-target :yield-unit]
             (:ordered-steps inner)
             (:ordered-steps outer)))
      (is (= :value-then-write
             (get-in inner-node [:evaluation :kind])
             (get-in outer-node [:evaluation :kind])))
      (doseq [record records]
        (let [node (get nodes (:core-node-id record))
              source (get source-map (:core-node-id record))]
          (is (= (:form-id record)
                 (get-in node [:source :form-id])
                 (:form-id source)))
          (is (= (:syntax-id record)
                 (get-in node [:source :syntax-id])
                 (:syntax-id source))))))))

(deftest sh07-b6-identity-normalizes-physical-sh06-ids-without-dropping-provenance
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          actual (:mutations (core artifact))
          semantic (:mutations (identity-input artifact))
          actual-nodes
          (filterv #(= :set! (:core-form %))
                   (:nodes (core artifact)))
          semantic-nodes
          (filterv #(= :set! (:core-form %))
                   (:nodes (identity-input artifact)))]
      (is (= (count actual) (count semantic)))
      (doseq [[actual-record semantic-record]
              (map vector actual semantic)]
        (let [semantic-projection-id
              (:sh06-semantic-projection-id actual-record)]
          (is (= (:target-binding-id actual-record)
                 (:target-upstream-binding-id semantic-record)))
          (is (= semantic-projection-id
                 (:target-definition-artifact-id semantic-record)
                 (:authenticated-sh06-artifact-id semantic-record)))
          (is (= (:target-upstream-binding-id actual-record)
                 (get-in
                  artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact :mutations
                   (:ordinal actual-record)
                   :target-upstream-binding-id])))))
      (is (= (count actual-nodes) (count semantic-nodes)))
      (doseq [[actual-node semantic-node]
              (map vector actual-nodes semantic-nodes)]
        (let [actual-attributes (:attributes actual-node)
              semantic-attributes (:attributes semantic-node)
              semantic-projection-id
              (:sh06-semantic-projection-id actual-attributes)]
          (is (= (:target-binding-id actual-attributes)
                 (:target-upstream-binding-id
                  semantic-attributes)))
          (is (= semantic-projection-id
                 (:target-definition-artifact-id
                  semantic-attributes)
                 (:authenticated-sh06-artifact-id
                  semantic-attributes))))))))

(deftest sh07-b6-authenticated-target-substitution-fails-before-lowering
  (let [artifact
        (file-artifact
         "accepted" "ordered-top-level-mutations" ".gravity")
        record (first (:mutations (core artifact)))
        authenticated (request artifact)
        wrong-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        mutations
        {"binding kind"
         (update-request-binding
          authenticated (:target-binding-id record)
          #(assoc % :kind :type))
         "binding upstream identity"
         (update-request-binding
          authenticated (:target-binding-id record)
          #(assoc % :upstream-binding-id wrong-id))
         "resolution order"
         (update-request-resolution
          authenticated (:target-syntax-id record)
          #(assoc % :resolution-order
                  :fully-qualified-namespace-binding))}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var
                  'sh07-core-from-authenticated-request)
                 (:sh06-resolution-artifact artifact)
                 mutation))
              diagnostic (diagnostic-data result)]
          (is (not= authenticated mutation))
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= :authenticated-sh06-projection-mismatch
                 (get-in diagnostic [:facts :reason])))
          (is (true?
               (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b6-rejections-are-structured-and-oracle-bound
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
        (is (= (:reason oracle)
               (get-in diagnostic [:facts :reason])))
        (when (:semantic-rule oracle)
          (is (= (:semantic-rule oracle)
                 (get-in diagnostic
                         [:facts :semantic-rule]))))
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b6-mutation-product-tampering-fails-replay
  (let [artifact
        (file-artifact
         "accepted" "ordered-top-level-mutations" ".gravity")
        value-node
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :mutations 1 :value-core-node-id])
        mutations
        {"result policy"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0]
          assoc :result-policy :target-value)
         "ordinal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0 :ordinal]
          7)
         "value node"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0 :value-core-node-id]
          value-node)}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               mutation artifact {:status :passed})
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (not= artifact mutation))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :mutations-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var
                    'sh07-core-artifact-verification)
                   mutation)))))))))
