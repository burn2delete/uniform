(ns gravity.self-hosting.sh07-multiple-catch-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_multiple_catch_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B9 test source is not on the classpath"
                      {:id "SH07-B9-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B9-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b9")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["two-catches-source-order"
   "three-catches-source-order"
   "sibling-binding-isolation"
   "shared-protected-candidates"
   "nested-handler-chains"
   "duplicate-types-remain-pending"])
(def ^:private rejected-reasons
  {"missing-catch" ["C6-CORE-SHAPE" :try-catch-required]
   "catch-before-protected"
   ["C6-CORE-SHAPE" :try-protected-expression-required]
   "protected-sequencing-deferred"
   ["C6-LOWERING-GAP" :try-protected-sequencing-deferred]
   "later-catch-empty-handler"
   ["C6-CORE-SHAPE" :catch-handler-expression-required]
   "later-catch-non-symbol-type"
   ["C6-CORE-SHAPE" :catch-type-symbol-required]
   "later-catch-non-symbol-binding"
   ["C6-CORE-SHAPE" :catch-binding-symbol-required]
   "later-catch-handler-sequencing-deferred"
   ["C6-LOWERING-GAP" :catch-handler-sequencing-deferred]
   "expression-between-catches"
   ["C6-CORE-SHAPE" :try-handler-chain-contiguity-required]
   "expression-after-catches"
   ["C6-CORE-SHAPE" :try-handler-chain-contiguity-required]
   "finally-remains-deferred"
   ["C6-LOWERING-GAP" :finally-clause-deferred]})
(def ^:private descriptor-keys
  #{:clause-ordinal :handler-child-index
    :catch-clause-form-id :catch-clause-syntax-id
    :error-type-form-id :error-type-syntax-id :error-type-binding-id
    :catch-binding-form-id :catch-binding-syntax-id
    :catch-binding-id :catch-binding-scope-id})
(def ^:private error-handler-record-keys
  #{:ordinal :clause-ordinal :clause-count :handler-child-index
    :core-node-id :form-id :syntax-id
    :protected-core-node-id :handler-core-node-id
    :catch-clause-form-id :catch-clause-syntax-id
    :error-type-form-id :error-type-syntax-id :error-type-binding-id
    :catch-binding-form-id :catch-binding-syntax-id
    :catch-binding-id :catch-binding-scope-id
    :catch-binding-use-syntax-ids
    :candidate-error-transfers
    :evaluation-region :evaluation-owner-function-syntax-id
    :ordered-steps :construction-order :runtime-evaluation
    :runtime-reachability :selection-policy :result-policy
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-coverage-legality :result-type-join-legality
    :effect-registry-legality
    :effect-profile-capability-legality
    :profile-error-lowering-legality
    :ownership-legality :safety-classification})
(def ^:private error-handler-attribute-keys
  #{:protected-child-index :handler-count :handler-child-indexes
    :handler-clauses :evaluation-order :runtime-reachability
    :selection-policy :result-policy
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-coverage-legality :result-type-join-legality
    :effect-registry-legality
    :effect-profile-capability-legality
    :profile-error-lowering-legality
    :ownership-legality :safety-classification})
(def ^:private public-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :sh06-resolution-artifact :gravity-core-boundary
    :provenance :pass :execution-boundary :capability-based-proof
    :diagnostics})

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
        "Required SH-07-B9 coordinator adapter is absent"
        {:id "SH07-B9-ADAPTER-ABSENT"
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
    (get (second clause) :sh07-b9)))

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
       (ex-info "SH-07-B9 records are not uniquely identifiable"
                {:id "SH07-B9-AMBIGUOUS-INDEX"
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

(defn- node-descendants
  [nodes root-id]
  (loop [pending [root-id] seen #{}]
    (if (empty? pending)
      seen
      (let [node-id (peek pending)
            remaining (pop pending)]
        (if (contains? seen node-id)
          (recur remaining seen)
          (recur (into remaining (:children (get nodes node-id)))
                 (conj seen node-id)))))))

(defn- handler-groups
  [artifact]
  (group-by :core-node-id (:error-handlers (core artifact))))

(defn- sorted-group
  [records]
  (sort-by :clause-ordinal records))

(defn- handler-descriptor
  [record]
  (select-keys record descriptor-keys))

(defn- verification-failures
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(deftest sh07-b9-fixtures-are-paired-complete-and-byte-identical
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-reasons)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames
                     (keys rejected-reasons))]
    (is (= (seq (source-bytes
                 (fixture-path family basename ".gravity")))
           (seq (source-bytes
                 (fixture-path family basename ".qst")))))))

(deftest sh07-b9-accepted-pairs-use-v11-and-path-neutral-products
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:error-handlers (core gravity))
               (:error-handlers (core qst))))
        (is (= 15 (:schema-version (request gravity))
               (:schema-version (request qst))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request gravity))
               (:scope (request qst))))
        (is (= "SH-07-B15" (:task gravity) (:task qst)))
        (is (= :c6-gravity-core-lowering-b15
               (get-in gravity [:pass :name])
               (get-in qst [:pass :name])))
        (is (= :gravity/sh07-to-c6-core-products-v15
               (get-in gravity
                       [:gravity-core-boundary :adapter-contract])
               (get-in qst
                       [:gravity-core-boundary :adapter-contract])))))))

(deftest sh07-b9-error-handler-records-nodes-and-descriptors-are-exact
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          forms (exactly-once-index (:forms (request artifact)) :form-id)
          bindings
          (exactly-once-index (:binding-table (request artifact)) :binding-id)
          resolutions (:resolution-table (request artifact))
          lineage (:lineage (request artifact))]
      (testing (str basename extension)
        (is (seq (handler-groups artifact)))
        (doseq [[try-id unsorted-records] (handler-groups artifact)]
          (let [records (vec (sorted-group unsorted-records))
                node (get nodes try-id)
                attributes (:attributes node)
                protected-id (first (:children node))
                handler-ids (vec (rest (:children node)))
                descriptors (:handler-clauses attributes)
                flattened-bindings
                (vec
                 (mapcat
                  (juxt :error-type-binding-id :catch-binding-id)
                  records))]
            (is (= :try (:core-form node)))
            (is (= error-handler-attribute-keys
                   (set (keys attributes))))
            (is (= (inc (count records)) (count (:children node))))
            (is (= protected-id (:protected-core-node-id (first records))))
            (is (= handler-ids (mapv :handler-core-node-id records)))
            (is (= flattened-bindings (:resolved-binding-ids node)))
            (is (= [{:index 0 :core-node-id protected-id}]
                   (get-in node [:evaluation :order])))
            (is (= :protected-then-ordered-typed-handler-candidates
                   (get-in node [:evaluation :kind])
                   (:evaluation-order attributes)))
            (is (= 0 (:protected-child-index attributes)))
            (is (= (count records) (:handler-count attributes)))
            (is (= (vec (range 1 (inc (count records))))
                   (:handler-child-indexes attributes)))
            (is (= (mapv handler-descriptor records) descriptors))
            (is (= :not-asserted-by-sh07-b9
                   (:runtime-reachability attributes)))
            (is (= :source-ordered-typed-handler-candidates
                   (:selection-policy attributes)))
            (is (= :protected-or-selected-handler-last
                   (:result-policy attributes)))
            (doseq [[record descriptor]
                    (map vector records descriptors)]
              (is (= error-handler-record-keys
                     (set (keys record))))
              (is (= descriptor-keys (set (keys descriptor))))
              (is (= (handler-descriptor record) descriptor))
              (is (= (count records) (:clause-count record)))
              (is (= (inc (:clause-ordinal record))
                     (:handler-child-index record)))
              (is (= [:evaluate-protected
                      :conditionally-select-source-ordered-typed-handler]
                     (:ordered-steps record)))
              (is (= :protected-then-handlers-source-order-then-try
                     (:construction-order record)))
              (is (= :protected-then-conditional-handler-chain
                     (:runtime-evaluation record)))
              (is (= :not-asserted-by-sh07-b9
                     (:runtime-reachability record)))
              (is (= :source-ordered-typed-handler-candidates
                     (:selection-policy record)))
              (is (= :protected-or-selected-handler-last
                     (:result-policy record)))
              (is (= (:authenticated-sh06-artifact-id lineage)
                     (:authenticated-sh06-artifact-id record)))
              (is (= (:sh06-semantic-projection-id lineage)
                     (:sh06-semantic-projection-id record)))
              (is (= :pending-sh08
                     (:type-coverage-legality record)
                     (:result-type-join-legality record)))
              (is (= :pending-sh09
                     (:effect-registry-legality record)
                     (:effect-profile-capability-legality record)
                     (:profile-error-lowering-legality record)))
              (is (= :pending-sh10 (:ownership-legality record)))
              (is (= :pending-sh11 (:safety-classification record)))
              (is (= :list
                     (:kind
                      (get forms (:catch-clause-form-id record)))))
              (is (= :symbol
                     (:kind (get forms (:error-type-form-id record)))))
              (is (= :symbol
                     (:kind
                      (get forms (:catch-binding-form-id record)))))
              (is (= :type
                     (:kind
                      (get bindings (:error-type-binding-id record)))))
              (is (= :local
                     (:kind (get bindings (:catch-binding-id record)))))
              (doseq [use-id (:catch-binding-use-syntax-ids record)]
                (is (some
                     #(and (= use-id (:reference-syntax-id %))
                           (= :expression (:position %))
                           (= (:catch-binding-id record)
                              (:binding-id %)))
                     resolutions))))))))))

(deftest sh07-b9-two-and-three-clause-order-is-source-stable
  (doseq [extension extensions
          [basename expected-count]
          [["two-catches-source-order" 2]
           ["three-catches-source-order" 3]]]
    (let [artifact (file-artifact "accepted" basename extension)
          groups (vals (handler-groups artifact))]
      (is (= 1 (count groups)))
      (let [records (vec (sorted-group (first groups)))
            node
            (get (exactly-once-index (:nodes (core artifact)) :node-id)
                 (:core-node-id (first records)))]
        (is (= expected-count (count records)))
        (is (= (vec (range expected-count))
               (mapv :clause-ordinal records)))
        (is (= (vec (range 1 (inc expected-count)))
               (mapv :handler-child-index records)))
        (is (= (vec (range expected-count))
               (mapv :ordinal records)))
        (is (= (vec (rest (:children node)))
               (mapv :handler-core-node-id records)))
        (is (= (mapv handler-descriptor records)
               (get-in node [:attributes :handler-clauses])))))))
(deftest sh07-b9-duplicate-handler-types-remain-pending
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted"
                         "duplicate-types-remain-pending"
                         extension)
          records
          (vec (sorted-group (first (vals (handler-groups artifact)))))]
      (is (= 2 (count records)))
      (is (= 1 (count (set (map :error-type-binding-id records)))))
      (is (= #{:pending-sh08}
             (set (map :type-coverage-legality records))))
      (is (= :not-asserted-by-sh07-b9
             (get-in
              (first
               (filter
                #(= (:core-node-id (first records)) (:node-id %))
                (:nodes (core artifact))))
              [:attributes :runtime-reachability]))))))

(deftest sh07-b9-sibling-catch-bindings-have-isolated-scopes
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "sibling-binding-isolation" extension)
          records (vec (sorted-group (first (vals (handler-groups artifact)))))
          resolutions (:resolution-table (request artifact))]
      (is (= 2 (count records)))
      (is (= 2 (count (set (map :catch-binding-id records)))))
      (is (= 2 (count (set (map :catch-binding-scope-id records)))))
      (doseq [record records
              use-id (:catch-binding-use-syntax-ids record)]
        (let [matches
              (filter #(= use-id (:reference-syntax-id %)) resolutions)]
          (is (= 1 (count matches)))
          (is (= (:catch-binding-id record)
                 (:binding-id (first matches))))
          (is (not-any?
               #(and (not= (:catch-binding-id record)
                           (:catch-binding-id %))
                     (some #{use-id}
                           (:catch-binding-use-syntax-ids %)))
               records)))))))

(deftest sh07-b9-protected-candidates-are-shared-and-exclude-handler-bodies
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "shared-protected-candidates" extension)
          records (vec (sorted-group (first (vals (handler-groups artifact)))))
          nodes (exactly-once-index (:nodes (core artifact)) :node-id)
          transfer-index
          (exactly-once-index (:error-transfers (core artifact)) :ordinal)
          protected-tree
          (node-descendants nodes (:protected-core-node-id (first records)))
          handler-trees
          (mapv #(node-descendants nodes (:handler-core-node-id %))
                records)
          handler-node-ids (apply set/union #{} handler-trees)
          candidates (:candidate-error-transfers (first records))]
      (is (= 2 (count records)))
      (is (seq candidates))
      (is (apply = (map :candidate-error-transfers records)))
      (doseq [candidate candidates]
        (let [transfer (get transfer-index (:ordinal candidate))]
          (is (= #{:ordinal :core-node-id} (set (keys candidate))))
          (is (= (:core-node-id candidate) (:core-node-id transfer)))
          (is (contains? protected-tree (:core-node-id candidate)))
          (is (not (contains? handler-node-ids
                              (:core-node-id candidate)))))))))

(deftest sh07-b9-nested-groups-preserve-per-try-and-global-construction-order
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "nested-handler-chains" extension)
          records (:error-handlers (core artifact))
          groups (handler-groups artifact)]
      (is (< 1 (count groups)))
      (is (= (vec (range (count records))) (mapv :ordinal records)))
      (doseq [[try-id group] groups]
        (let [ordered (vec (sorted-group group))
              clause-count (count ordered)]
          (is (= (vec (range clause-count))
                 (mapv :clause-ordinal ordered)))
          (is (= #{clause-count} (set (map :clause-count ordered))))
          (is (= #{try-id} (set (map :core-node-id ordered))))
          (is (= 1 (count (set (map :protected-core-node-id ordered)))))
          (is (= 1
                 (count
                  (set (map :candidate-error-transfers ordered))))))))))

(deftest sh07-b9-fresh-cross-root-identity-retains-actual-path
  (let [fixture
        (fixture-path "accepted" "nested-handler-chains" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b9-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/handlers.gravity")
        right-path (.resolve temp-root "right/handlers.qst")]
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
            ((required-var 'sh07-core-file-artifact) (str right-path))]
        (is (= :accepted (:status left) (:status right)))
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= (:error-handlers (core left))
               (:error-handlers (core right))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in (core left)
                       [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in (core right)
                       [:provenance :actual-source-path])))
        (is (not= (get-in left [:provenance :source-path])
                  (get-in right [:provenance :source-path]))))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b9-rejections-are-structured-oracle-bound-and-precedence-stable
  (doseq [[basename [rule reason]] rejected-reasons
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= rule (:expected-rule declared)))
        (is (= :core-lowering (:expected-stage declared)))
        (is (= :error (:expected-severity declared)))
        (is (= reason (:expected-reason declared)))
        (is (string? (:expected-remediation declared)))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= rule (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= reason (get-in diagnostic [:facts :reason])))
        (is (= (:expected-remediation declared)
               (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b9-authenticated-input-substitution-fails-before-lowering
  (let [artifact
        (file-artifact "accepted" "two-catches-source-order" ".gravity")
        authenticated (request artifact)
        substituted
        (assoc-in
         authenticated
         [:lineage :sh06-semantic-projection-id]
         "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        result
        (diagnostic-result
         #((required-var 'sh07-core-from-authenticated-request)
           (:sh06-resolution-artifact artifact)
           substituted))
        diagnostic (diagnostic-data result)]
    (is (not= authenticated substituted))
    (is (nil? (:raw-host-error result)))
    (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :authenticated-sh06-projection-mismatch
           (get-in diagnostic [:facts :reason])))
    (is (true? (get-in diagnostic [:facts :fail-closed])))))

(deftest sh07-b9-handler-chain-alterations-fail-replay
  (let [artifact
        (file-artifact "accepted" "three-catches-source-order" ".gravity")
        handlers
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :error-handlers])
        first-handler (first handlers)
        node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= (:core-node-id first-handler) (:node-id node))
              index))
          (get-in artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact :nodes])))
        second-handler-id (:handler-core-node-id (second handlers))
        alterations
        {"record order"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers]
          (vec (concat [(second handlers) (first handlers)]
                       (drop 2 handlers))))
         "record removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers]
          pop)
         "clause ordinal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers 0 :clause-ordinal]
          2)
         "candidate removal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :error-handlers 0 :candidate-error-transfers]
          [])
         "descriptor removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :attributes :handler-clauses]
          pop)
         "child cross-wire"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :children 1]
          second-handler-id)
         "selection policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :attributes :selection-policy]
          :always)}]
    (doseq [[label altered] alterations]
      (testing label
        (let [failed (verification-failures altered artifact)]
          (is (not= artifact altered))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :error-handlers-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b9-public-proof-is-bounded-and-does-not-claim-later-facts
  (doseq [basename accepted-basenames]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          proof (:capability-based-proof artifact)
          boundary (:gravity-core-boundary artifact)
          canonical (:canonical-core-artifact boundary)
          pending
          (get-in artifact
                  [:execution-boundary :pending-lowering-families])
          maximum
          (get-in boundary
                  [:raw-template-result
                   :bounds :maximum-error-handler-records])]
      (testing basename
        (is (= public-artifact-keys (set (keys artifact))))
        (is (= :gravity/sh07-core-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (= ["L2" "L3" "L6" "L7" "L9" "C5" "C6"]
               (:document-set artifact)))
        (is (= 1024 maximum))
        (is (<= (count (:error-handlers canonical)) maximum))
        (is (= :gravity/sh07-to-c6-core-products-v15
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:error-handlers-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (not-any? #{:multiple-try-handlers} pending))
        (is (every? (set pending)
                    [:try-finally
                     :try-protected-sequencing
                     :try-handler-sequencing
                     :patterns]))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))

(deftest sh07-b9-error-handler-resolver-rejects-over-limit-vectors
  (is (= {:status :rejected
          :reason :error-handler-vector-required}
         (bootstrap/sh07-core-execute!
          (fixture-path
           "accepted" "two-catches-source-order" ".gravity")
          'sh07-resolve-error-handler-vector
          [(vec (repeat 1025 {})) []]))))
