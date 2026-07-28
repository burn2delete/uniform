(ns gravity.self-hosting.sh07-vector-pattern-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_vector_pattern_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07-B11 test source is not on the classpath"
                {:id "SH07-B11-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-B11-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b11")
(def ^:private b10-fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b10")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-fixtures
  #{"nested-vector-patterns"
    "vector-branch-local-binding"
    "vector-leaf-patterns"
    "vector-scrutinee-once"
    "vector-tail-recur"})
(def ^:private rejected-fixtures
  #{"guard-pattern-deferred"
    "list-pattern-deferred"
    "map-pattern-deferred"
    "nested-map-in-vector"
    "set-pattern-deferred"
    "vector-pattern-over-width"
    "vector-rest-extra-tail"
    "vector-rest-missing-tail"
    "vector-rest-tail"})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- b10-fixture-path
  [family basename extension]
  (path (str b10-fixture-root "/" family "/" basename extension)))

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
  (slurp source-path :encoding "UTF-8"))

(defn- source-forms
  [relative]
  (with-open [reader
              (java.io.PushbackReader.
               (io/reader (path relative) :encoding "UTF-8"))]
    (let [eof (Object.)]
      (loop [forms []]
        (let [form (read {:eof eof} reader)]
          (if (identical? eof form)
            forms
            (recur (conj forms form))))))))

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
        "Required SH-07-B11 coordinator adapter is absent"
        {:id "SH07-B11-ADAPTER-ABSENT"
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

(defn- direct-artifact
  [family basename extension]
  (let [source-path (fixture-path family basename extension)]
    ((required-var 'sh07-core-source-artifact)
     source-path
     (source-text source-path))))

(defn- b10-file-artifact
  [basename extension]
  ((required-var 'sh07-core-file-artifact)
   (b10-fixture-path "accepted" basename extension)))

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
    (get (second clause) :sh07-b11)))

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
       (ex-info "SH-07-B11 records are not uniquely identifiable"
                {:id "SH07-B11-AMBIGUOUS-INDEX"
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

(defn- verification-failures
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(defn- execute-core-function
  [basename function arguments]
  (bootstrap/sh07-core-execute!
   (fixture-path "accepted" basename ".gravity")
   function
   arguments))

(defn- match-groups
  [artifact]
  (group-by :core-node-id (:match-branch-records (core artifact))))

(defn- pattern-groups
  [artifact]
  (group-by :core-node-id (:match-pattern-records (core artifact))))

(defn- pattern-clause-groups
  [artifact]
  (group-by
   (juxt :core-node-id :clause-ordinal)
   (:match-pattern-records (core artifact))))

(def ^:private match-pattern-record-keys
  #{:ordinal :clause-ordinal :local-ordinal
    :parent-local-ordinal :root-local-ordinal
    :parent-ordinal :root-ordinal :core-node-id
    :depth :path :pattern-kind
    :pattern-form-id :pattern-syntax-id
    :pattern-value :vector-width
    :pattern-binding-id :pattern-binding-scope-id
    :pattern-binding-use-syntax-ids
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id})

(deftest sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope
  (doseq [family ["accepted" "rejected"]]
    (let [expected
          (if (= family "accepted")
            accepted-fixtures
            rejected-fixtures)]
      (is (= expected
             (fixture-basenames family ".gravity")
             (fixture-basenames family ".qst")))
      (doseq [basename expected]
        (is (= (seq (source-bytes
                     (fixture-path family basename ".gravity")))
               (seq (source-bytes
                     (fixture-path family basename ".qst"))))))))
  (doseq [required-name
          ["leaf" "nested" "branch-local" "scrutinee-once" "tail-recur"]]
    (is (some #(str/includes? % required-name) accepted-fixtures)))
  (doseq [required-name ["list" "map" "set" "rest"]]
    (is (some #(str/includes? % required-name) rejected-fixtures)))
  (is (not-any? #(str/includes? % "duplicate") rejected-fixtures))
  (is (not-any? #(str/includes? % "variable-width") accepted-fixtures)))

(deftest sh07-b11-gravity-contract-names-l7
  (let [forms
        (source-forms "bootstrap/gravity/src/gravity/checked_core.gravity")
        contract-form
        (first
         (filter
          #(and (seq? %)
                (= 'def (first %))
                (= 'sh07-core-contract (second %)))
          forms))
        contract-documents
        (set (:governing-contracts (nth contract-form 2)))]
    (is (seq contract-form))
    (is (contains? contract-documents "L7"))))

(deftest sh07-b11-direct-and-public-routing-use-v12
  (doseq [extension extensions]
    (let [basename "vector-leaf-patterns"
          direct (direct-artifact "accepted" basename extension)
          public (file-artifact "accepted" basename extension)]
      (testing (str basename extension)
        (is (= :accepted (:status direct) (:status public)))
        (is (= (:artifact-id direct) (:artifact-id public)))
        (is (= (identity-input direct) (identity-input public)))
        (is (= (:match-pattern-records (core direct))
               (:match-pattern-records (core public))))
        (is (= 15 (:schema-version (request direct))
               (:schema-version (request public))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request direct))
               (:scope (request public))))
        (is (= "SH-07-B15" (:task direct) (:task public)))
        (is (= :c6-gravity-core-lowering-b15
               (get-in direct [:pass :name])
               (get-in public [:pass :name])))
        (is (= :gravity/sh07-to-c6-core-products-v15
               (get-in direct
                       [:gravity-core-boundary :adapter-contract])
               (get-in public
                       [:gravity-core-boundary :adapter-contract])))))))

(deftest sh07-b11-preserves-b10-scalar-pattern-products
  (doseq [extension extensions]
    (let [artifact
          (b10-file-artifact "literal-clauses-wildcard" extension)
          core-artifact (core artifact)]
      (testing extension
        (is (= :accepted (:status artifact)))
        (is (= 15 (:schema-version (request artifact))))
        (is (seq (:match-branch-records core-artifact)))
        (is (seq (:match-decision-skeletons core-artifact)))
        (is (= (count (:match-branch-records core-artifact))
               (count (:match-pattern-records core-artifact))))
        (is (= #{:literal :wildcard}
               (set (map :pattern-kind
                         (:match-branch-records core-artifact)))
               (set (map :pattern-kind
                         (:match-pattern-records core-artifact)))))
        (is (every? #(and (= 0 (:depth %))
                          (= [] (:path %))
                          (nil? (:parent-ordinal %)))
                    (:match-pattern-records core-artifact)))))))

(deftest sh07-b11-pattern-records-are-preorder-bounded-and-parent-linked
  (doseq [basename accepted-fixtures
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:match-pattern-records core-artifact)
          record-by-ordinal
          (exactly-once-index records :ordinal)]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (is (= (set (keys (match-groups artifact)))
               (set (keys (pattern-groups artifact)))))
        (doseq [[[match-id clause-ordinal] clause-records]
                (pattern-clause-groups artifact)]
          (let [ordered (vec (sort-by :ordinal clause-records))
                root-record (first ordered)
                root-ordinal (:ordinal root-record)]
            (is (= (vec (range (count ordered)))
                   (mapv :local-ordinal ordered)))
            (is (= #{match-id} (set (map :core-node-id ordered))))
            (is (= #{clause-ordinal}
                   (set (map :clause-ordinal ordered))))
            (is (= root-ordinal (:root-ordinal root-record)))
            (is (= 0 (:root-local-ordinal root-record)
                   (:local-ordinal root-record)
                   (:depth root-record)))
            (is (= [] (:path root-record)))
            (is (nil? (:parent-ordinal root-record)))
            (is (nil? (:parent-local-ordinal root-record)))
            (doseq [record ordered]
              (is (= match-pattern-record-keys
                     (set (keys record))))
              (is (= root-ordinal (:root-ordinal record)))
              (is (= (:local-ordinal record)
                     (- (:ordinal record) root-ordinal)))
              (is (= (:depth record) (count (:path record))))
              (is (<= 0 (:depth record) 64))
              (is (every? #(and (integer? %) (not (neg? %)))
                          (:path record)))
              (if-some [parent-ordinal (:parent-ordinal record)]
                (let [parent (get record-by-ordinal parent-ordinal)]
                  (is (= :vector (:pattern-kind parent)))
                  (is (= (:local-ordinal parent)
                         (:parent-local-ordinal record)))
                  (is (= (inc (:depth parent)) (:depth record)))
                  (is (= (:path parent) (pop (:path record))))
                  (is (< (peek (:path record))
                         (:vector-width parent))))
                (is (= record root-record)))
              (if (= :vector (:pattern-kind record))
                (let [children
                      (filterv
                       #(= (:ordinal record) (:parent-ordinal %))
                       ordered)]
                  (is (nil? (:pattern-value record)))
                  (is (<= 0 (:vector-width record) 256))
                  (is (= (:vector-width record) (count children))))
                (is (nil? (:vector-width record)))))))))))

(deftest sh07-b11-nested-vector-records-preserve-source-order-and-kinds
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "nested-vector-patterns" extension)
          records (:match-pattern-records (core artifact))
          vector-records (filterv #(= :vector (:pattern-kind %)) records)
          nested-records (filterv #(pos? (:depth %)) records)]
      (testing extension
        (is (< 1 (count vector-records)))
        (is (seq nested-records))
        (is (some #(= 2 (:depth %)) records))
        (is (= #{:binding :literal :vector :wildcard}
               (set (map :pattern-kind records))))
        (doseq [[[_ clause-ordinal] clause-records]
                (pattern-clause-groups artifact)]
          (let [ordered (vec (sort-by :ordinal clause-records))]
            (is (= (if (< clause-ordinal 2)
                     [[] [0] [0 0] [0 1] [1] [1 0] [1 1]]
                     [[]])
                   (mapv :path ordered)))))))))

(deftest sh07-b11-vector-bindings-are-unique-branch-local-and-use-linked
  (doseq [basename ["nested-vector-patterns"
                    "vector-branch-local-binding"]
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          references (:reference-uses core-artifact)
          binding-records
          (filterv #(= :binding (:pattern-kind %))
                   (:match-pattern-records core-artifact))]
      (testing (str basename extension)
        (is (seq binding-records))
        (doseq [[_ clause-records] (pattern-clause-groups artifact)]
          (let [bindings
                (filterv #(= :binding (:pattern-kind %))
                         clause-records)]
            (is (= (count bindings)
                   (count (set (map :pattern-value bindings)))))
            (is (= (count bindings)
                   (count (set (map :pattern-binding-id bindings)))))))
        (doseq [record binding-records]
          (is (symbol? (:pattern-value record)))
          (is (not= '_ (:pattern-value record)))
          (is (string? (:pattern-binding-id record)))
          (is (string? (:pattern-binding-scope-id record)))
          (doseq [use-id (:pattern-binding-use-syntax-ids record)]
            (is (= 1
                   (count
                    (filter
                     #(and (= use-id (:syntax-id %))
                           (= (:pattern-binding-id record)
                              (:binding-id %)))
                     references))))))))))

(deftest sh07-b11-scrutinee-is-once-and-vector-branches-are-conditional
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "vector-scrutinee-once" extension)
          core-artifact (core artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          transfers (:error-transfers core-artifact)]
      (testing extension
        (is (= 2 (count transfers)))
        (doseq [[match-id records] (match-groups artifact)]
          (let [ordered (vec (sort-by :clause-ordinal records))
                match-node (get nodes match-id)
                scrutinee-id (first (:children match-node))
                branch-transfers
                (filterv #(= :match-branch
                             (get-in % [:evaluation-region :kind]))
                         transfers)]
            (is (= 1 (count (filter #{scrutinee-id}
                                    (:children match-node)))))
            (is (= #{scrutinee-id}
                   (set (map :scrutinee-core-node-id ordered))))
            (is (= #{:evaluate-scrutinee-once}
                   (set (map :scrutinee-evaluation ordered))))
            (is (= #{:conditionally-evaluate-selected-branch}
                   (set (map :branch-evaluation ordered))))
            (is (= 1 (count branch-transfers)))
            (is (= #{(:conditional-region (first ordered))}
                   (set (map :evaluation-region
                             branch-transfers))))))))))

(deftest sh07-b11-vector-branch-preserves-tail-recur-target
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "vector-tail-recur" extension)
          core-artifact (core artifact)
          targets (exactly-once-index (:recur-targets core-artifact) :target-id)
          transfers (:recur-transfers core-artifact)]
      (testing extension
        (is (= 1 (count transfers)))
        (let [transfer (first transfers)
              target (get targets (:target-id transfer))]
          (is (= :loop (:target-kind transfer)
                 (:target-kind target)))
          (is (true? (:tail-position transfer)))
          (is (= :nearest-lexical-recur-target
                 (:transfer-policy transfer)))
          (is (= :pending-sh08 (:type-compatibility transfer))))))))

(deftest sh07-b11-identities-are-deterministic-path-neutral-and-provenanced
  (let [fixture
        (fixture-path "accepted" "nested-vector-patterns" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b11-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/vector.gravity")
        right-path (.resolve temp-root "right/vector.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left ((required-var 'sh07-core-file-artifact) (str left-path))
            repeated
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right ((required-var 'sh07-core-file-artifact) (str right-path))]
        (is (= :accepted (:status left) (:status right)))
        (is (= left repeated))
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= (:match-pattern-records (core left))
               (:match-pattern-records (core right))))
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

(deftest sh07-b11-pattern-products-bind-to-the-authenticated-sh06-lineage
  (doseq [basename accepted-fixtures
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          lineage (:lineage (request artifact))]
      (doseq [record (:match-pattern-records (core artifact))]
        (is (= (:authenticated-sh06-artifact-id lineage)
               (:authenticated-sh06-artifact-id record)))
        (is (= (:sh06-semantic-projection-id lineage)
               (:sh06-semantic-projection-id record)))))))

(deftest sh07-b11-pattern-product-alterations-fail-replay
  (let [artifact
        (file-artifact "accepted" "nested-vector-patterns" ".gravity")
        records (:match-pattern-records (core artifact))
        child-index
        (first
         (keep-indexed
          (fn [index record]
            (when (:parent-ordinal record) index))
          records))
        alterations
        {"record removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records]
          pop)
         "record order"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records]
          (vec (reverse records)))
         "path"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records child-index :path]
          [255])
         "parent"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records child-index :parent-ordinal]
          nil)
         "width"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records 0 :vector-width]
          257)}]
    (is (integer? child-index))
    (doseq [[label altered] alterations]
      (testing label
        (let [failed (verification-failures altered artifact)]
          (is (not= artifact altered))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :match-pattern-records-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b11-pattern-resolvers-enforce-record-depth-width-and-count-bounds
  (let [basename "nested-vector-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        vector-record (first (filter #(= :vector (:pattern-kind %)) records))
        cases
        {"unexpected field"
         [(assoc vector-record :unexpected true)
          :match-pattern-record-shape]
         "negative ordinal"
         [(assoc vector-record :ordinal -1)
          :match-pattern-ordinal]
         "depth above maximum"
         [(assoc vector-record
                 :depth 65
                 :path (vec (repeat 65 0)))
          :match-pattern-ordinal-or-depth]
         "width above maximum"
         [(assoc vector-record :vector-width 257)
          :match-pattern-value]}]
    (is (map? vector-record))
    (doseq [[label [record reason]] cases]
      (testing label
        (is (= {:status :rejected :reason reason}
               (execute-core-function
                basename
                'sh07-resolve-match-pattern-record
                [record resolved-digests])))))
    (is (= {:status :rejected
            :reason :match-pattern-vector-required}
           (execute-core-function
            basename
            'sh07-resolve-match-pattern-vector
            [(vec (repeat 1025 {})) resolved-digests])))))

(deftest sh07-b11-pattern-resolver-rejects-invalid-parent-graphs
  (let [basename "nested-vector-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        child-index
        (first
         (keep-indexed
          (fn [index record]
            (when (:parent-ordinal record) index))
          records))
        parentless
        (assoc-in records [child-index :parent-ordinal] nil)
        outside-width
        (assoc-in records [child-index :path]
                  [(get-in records
                           [(:parent-ordinal (get records child-index))
                            :vector-width])])]
    (is (integer? child-index))
    (is (= :accepted
           (:status
            (execute-core-function
             basename
             'sh07-resolve-match-pattern-vector
             [records resolved-digests]))))
    (doseq [altered [parentless outside-width]]
      (is (= {:status :rejected
              :reason :match-pattern-parent-graph}
             (execute-core-function
              basename
              'sh07-resolve-match-pattern-vector
              [altered resolved-digests]))))))

(deftest sh07-b11-pattern-graph-completeness-rejects-structural-alterations
  (let [basename "vector-leaf-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        raw-records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        vector-root
        (first
         (filter
          #(and (= :vector (:pattern-kind %))
                (= 2 (:vector-width %))
                (nil? (:parent-ordinal %)))
          raw-records))
        direct-children
        (filterv #(= (:ordinal vector-root) (:parent-ordinal %))
                 raw-records)
        first-child (first direct-children)
        second-child (second direct-children)
        inflated-width
        (assoc-in raw-records
                  [(:ordinal vector-root) :vector-width]
                  3)
        missing-child-path
        [(assoc vector-root
                :ordinal 0
                :local-ordinal 0
                :root-ordinal 0)
         (assoc first-child
                :ordinal 1
                :local-ordinal 1
                :parent-ordinal 0
                :root-ordinal 0)]
        duplicate-child-path
        (assoc-in raw-records
                  [(:ordinal second-child) :path]
                  (:path first-child))
        duplicate-root
        (assoc raw-records
               (:ordinal second-child)
               (assoc second-child
                      :local-ordinal 0
                      :parent-local-ordinal nil
                      :parent-ordinal nil
                      :root-ordinal (:ordinal second-child)
                      :depth 0
                      :path []))
        resolver-cases
        {"inflated vector width" inflated-width
         "missing child path" missing-child-path
         "duplicate child path" duplicate-child-path
         "duplicate root in one match clause" duplicate-root}
        canonical (core artifact)
        canonical-records (:match-pattern-records canonical)
        canonical-root
        (first
         (filter
          #(and (= :vector (:pattern-kind %))
                (= 2 (:vector-width %))
                (nil? (:parent-ordinal %)))
          canonical-records))
        canonical-children
        (filterv
         #(= (:ordinal canonical-root) (:parent-ordinal %))
         canonical-records)
        canonical-first-child (first canonical-children)
        canonical-second-child (second canonical-children)
        coherence-cases
        {"inflated vector width"
         (assoc-in canonical-records
                   [(:ordinal canonical-root) :vector-width]
                   3)
         "missing child path"
         (vec (remove #(= (:ordinal canonical-second-child)
                          (:ordinal %))
                      canonical-records))
         "duplicate child path"
         (assoc-in canonical-records
                   [(:ordinal canonical-second-child) :path]
                   (:path canonical-first-child))
         "duplicate root in one match clause"
         (assoc canonical-records
                (:ordinal canonical-second-child)
                (assoc canonical-second-child
                       :local-ordinal 0
                       :parent-local-ordinal nil
                       :parent-ordinal nil
                       :root-ordinal (:ordinal canonical-second-child)
                       :depth 0
                       :path []))}]
    (is (map? vector-root))
    (is (= 2 (count direct-children)))
    (doseq [[label altered] resolver-cases]
      (testing (str "Gravity resolver: " label)
        (is (= {:status :rejected
                :reason :match-pattern-incomplete-graph}
               (execute-core-function
                basename
                'sh07-resolve-match-pattern-vector
                [altered resolved-digests])))))
    (doseq [[label altered-records] coherence-cases]
      (testing (str "Clojure coherence: " label)
        (is (false?
             ((required-var 'sh07-core-match-products-coherent?)
              (assoc canonical
                     :match-pattern-records altered-records))))))))

(deftest sh07-b11-public-proof-is-bounded-and-honest
  (doseq [basename accepted-fixtures]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          proof (:capability-based-proof artifact)
          boundary (:gravity-core-boundary artifact)
          canonical (:canonical-core-artifact boundary)
          bounds (get-in boundary [:raw-template-result :bounds])
          pending
          (set (get-in artifact
                       [:execution-boundary
                        :pending-lowering-families]))]
      (testing basename
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (contains? (set (:document-set artifact)) "L7"))
        (is (= 1024 (:maximum-match-pattern-records bounds)))
        (is (= 64 (:maximum-match-pattern-depth bounds)))
        (is (= 256 (:maximum-match-pattern-width bounds)))
        (is (<= (count (:match-pattern-records canonical)) 1024))
        (is (= :gravity/sh07-to-c6-core-products-v15
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:match-pattern-records-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (not (contains? pending :fixed-width-vector-patterns)))
        (is (contains? pending :variable-width-vector-patterns))
        (is (contains? pending :map-list-set-record-constructor-patterns))
        (is (contains? pending :duplicate-pattern-binding-policy))
        (is (contains? pending :guard-patterns))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))

(deftest sh07-b11-rejections-are-structured-and-oracle-bound
  (let [observed-reasons (atom #{})]
    (doseq [basename rejected-fixtures
            extension extensions]
      (testing (str basename extension)
        (let [source-path (fixture-path "rejected" basename extension)
              declared (fixture-oracle basename extension)
              result
              (diagnostic-result
               #((required-var 'sh07-core-file-artifact) source-path))
              diagnostic (diagnostic-data result)]
          (swap! observed-reasons conj (:expected-reason declared))
          (is (map? declared))
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
          (is (= (:expected-rule declared) (:rule diagnostic)))
          (is (= (:expected-stage declared) (:stage diagnostic)))
          (is (= (:expected-severity declared) (:severity diagnostic)))
          (is (= (:expected-reason declared)
                 (get-in diagnostic [:facts :reason])))
          (is (= (:expected-remediation declared)
                 (:remediation diagnostic)))
          (is (= source-path (get-in diagnostic [:source-span :source])))
          (is (true? (get-in diagnostic [:facts :fail-closed]))))))
    (is (= #{:composite-pattern-deferred
             :guard-pattern-deferred
             :unsupported-vector-pattern-leaf
             :variable-width-vector-pattern-deferred
             :vector-pattern-width-bound}
           @observed-reasons))))
