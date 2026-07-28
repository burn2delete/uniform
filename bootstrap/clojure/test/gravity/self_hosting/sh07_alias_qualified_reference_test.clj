(ns gravity.self-hosting.sh07-alias-qualified-reference-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_alias_qualified_reference_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07-B13 test source is not on the classpath"
        {:id "SH07-B12-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-B12-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b12")
(def ^:private b11-fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b11")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private maximum-alias-records 256)
(def ^:private alias-record-keys
  #{:alias :dependency-artifact-id :kind :namespace :profile :targets})
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

(defn- b11-fixture-path
  [basename extension]
  (path
   (str b11-fixture-root "/accepted/" basename extension)))

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

(defn- accepted-fixtures
  []
  (fixture-basenames "accepted" ".gravity"))

(defn- rejected-fixtures
  []
  (fixture-basenames "rejected" ".gravity"))

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
        "Required SH-07-B13 coordinator adapter is absent"
        {:id "SH07-B12-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-capability-based-proof '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
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

(defn- direct-artifact
  [family basename extension]
  (let [source-path (fixture-path family basename extension)]
    ((required-var 'sh07-core-source-artifact)
     source-path
     (source-text source-path))))

(defn- b11-file-artifact
  [extension]
  ((required-var 'sh07-core-file-artifact)
   (b11-fixture-path "nested-vector-patterns" extension)))

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
    (get (second metadata-clause) :sh07-b12)))

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
        "SH-07-B13 records are not uniquely identifiable"
        {:id "SH07-B12-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- alias-resolutions
  [artifact]
  (filterv
   #(= :alias-qualified-required-binding (:resolution-order %))
   (:resolution-table (request artifact))))

(defn- alias-resolution-fixture
  [position]
  (first
   (for [basename (sort (accepted-fixtures))
         extension extensions
         :let [artifact (file-artifact "accepted" basename extension)]
         resolution (alias-resolutions artifact)
         :when (= position (:position resolution))]
     {:basename basename
      :extension extension
      :artifact artifact
      :resolution resolution})))

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
        (when (and (string? (:id data))
                   (keyword? (:stage data)))
          (assoc data :rule (:id data)))
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

(defn- run-request
  [resolution-artifact authenticated-request]
  (diagnostic-result
   #((required-var 'sh07-core-run-request-for-test)
     resolution-artifact authenticated-request)))

(defn- rebind-request
  [authenticated-request]
  (assoc
   authenticated-request
   :projection-binding
   ((required-var 'reader-canonical-hash)
    ((required-var 'sh07-core-projection-binding-input)
     authenticated-request))))

(defn- rebind-alias-lineage-and-request
  [authenticated-request]
  (rebind-request
   (assoc-in
    authenticated-request
    [:lineage :alias-table-id]
    ((required-var 'reader-canonical-hash)
     {:domain :gravity/sh07-sh06-alias-table-v1
      :aliases (:alias-table authenticated-request)}))))

(defn- alias-for-resolution
  [authenticated-request resolution]
  (let [symbol-value (:symbol resolution)
        alias-name
        (when (qualified-symbol? symbol-value)
          (symbol (namespace symbol-value)))]
    (first
     (filter #(= alias-name (:alias %))
             (:alias-table authenticated-request)))))

(defn- binding-for-resolution
  [authenticated-request resolution]
  (first
   (filter #(= (:binding-id resolution) (:binding-id %))
           (:binding-table authenticated-request))))

(deftest sh07-b12-fixtures-are-dynamically-discovered-paired-and-bounded
  (let [accepted (accepted-fixtures)
        rejected (rejected-fixtures)]
    (is (seq accepted))
    (is (seq rejected))
    (is (= accepted
           (fixture-basenames "accepted" ".qst")))
    (is (= rejected
           (fixture-basenames "rejected" ".qst")))
    (doseq [family ["accepted" "rejected"]
            basename (sort
                      (if (= family "accepted")
                        accepted
                        rejected))]
      (testing (str family "/" basename)
        (is (= (seq (source-bytes
                     (fixture-path family basename ".gravity")))
               (seq (source-bytes
                     (fixture-path family basename ".qst")))))))
    (is (some
         #(str/includes?
           (source-text (fixture-path "accepted" % ".gravity"))
           "shared/")
         accepted))
    (is (some
         #(re-find
           #"\(shared/[A-Za-z0-9*+!_?<>.=/-]+\s"
           (source-text (fixture-path "accepted" % ".gravity")))
         accepted))))

(deftest sh07-b12-direct-and-public-routing-use-v13
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [direct (direct-artifact "accepted" basename extension)
          public (file-artifact "accepted" basename extension)]
      (testing (str basename extension)
        (is (= :accepted (:status direct) (:status public)))
        (is (= (:artifact-id direct) (:artifact-id public)))
        (is (= (identity-input direct) (identity-input public)))
        (is (= 14 (:schema-version (request direct))
               (:schema-version (request public))))
        (is (= :sh07-b13-fragmented-meta-jvm-core
               (:scope (request direct))
               (:scope (request public))))
        (is (= "SH-07-B13" (:task direct) (:task public)))
        (is (= :c6-gravity-core-lowering-b13
               (get-in direct [:pass :name])
               (get-in public [:pass :name])))
        (is (= :gravity/sh07-to-c6-core-products-v14
               (get-in direct
                       [:gravity-core-boundary :adapter-contract])
               (get-in public
                       [:gravity-core-boundary :adapter-contract])))))))

(deftest sh07-b12-declared-alias-table-is-exact-bounded-and-projected
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          authenticated-request (request artifact)
          aliases (:alias-table authenticated-request)
          declared-aliases (:declared-alias-table (core artifact))
          alias-index (exactly-once-index aliases :alias)
          upstream
          (get-in artifact
                  [:sh06-resolution-artifact
                   :namespace-analysis :alias-table])]
      (testing (str basename extension)
        (is (vector? aliases))
        (is (<= 1 (count aliases) maximum-alias-records))
        (is (= aliases declared-aliases upstream))
        (is (= declared-aliases
               (:declared-alias-table (identity-input artifact))))
        (is (= declared-aliases
               (get-in artifact
                       [:gravity-core-boundary
                        :raw-template-result :core-template
                        :declared-alias-table])))
        (is (= (count aliases) (count alias-index)))
        (doseq [alias-record aliases]
          (is (= alias-record-keys (set (keys alias-record))))
          (is (simple-symbol? (:alias alias-record)))
          (is (symbol? (:namespace alias-record)))
          (is (= :namespace (:kind alias-record)))
          (is (keyword? (:profile alias-record)))
          (is (vector? (:targets alias-record)))
          (is (every? keyword? (:targets alias-record)))
          (is (re-matches #"sha256:[0-9a-f]{64}"
                          (:dependency-artifact-id alias-record))))))))

(deftest sh07-b12-alias-qualified-value-and-operator-references-bind-exactly
  (doseq [position [:expression :operator]]
    (let [{:keys [basename extension artifact resolution]}
          (alias-resolution-fixture position)
          authenticated-request (request artifact)
          alias-record (alias-for-resolution authenticated-request resolution)
          binding (binding-for-resolution authenticated-request resolution)
          use
          (first
           (filter #(= (:reference-syntax-id resolution) (:syntax-id %))
                   (:reference-uses (core artifact))))]
      (testing (name position)
        (is (string? basename))
        (is (contains? (set extensions) extension))
        (is (map? resolution))
        (is (map? alias-record))
        (is (map? binding))
        (is (map? use))
        (is (= :alias-qualified-required-binding
               (:resolution-order resolution)))
        (is (= position (:position resolution) (:position use)))
        (is (= (:binding-id resolution)
               (:binding-id binding)
               (:binding-id use)))
        (is (= (:namespace alias-record) (:namespace binding)))
        (is (= (:dependency-artifact-id alias-record)
               (:definition-artifact-id binding)))
        (is (some #{(:profile alias-record)}
                  (:profile-set binding)))
        (is (every? (set (:target-set binding))
                    (:targets alias-record)))
        (is (= (name (:alias alias-record))
               (namespace (:symbol resolution))))
        (is (= (name (:name binding))
               (name (:symbol resolution))))
        (is (= reference-use-keys (set (keys use))))
        (is (= (:symbol resolution) (:symbol use)))
        (is (= (:reference-syntax-id resolution) (:syntax-id use)))))))

(deftest sh07-b12-alias-targets-may-be-a-binding-target-subset
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "alias-multitarget-binding" extension)
          authenticated-request (request artifact)
          resolution (first (alias-resolutions artifact))
          alias-record
          (alias-for-resolution authenticated-request resolution)
          binding
          (binding-for-resolution authenticated-request resolution)]
      (testing extension
        (is (= :accepted (:status artifact)))
        (is (= [:jvm] (:targets alias-record)))
        (is (= [:jvm :wasm] (:target-set binding)))
        (is (every? (set (:target-set binding))
                    (:targets alias-record)))))))

(deftest sh07-b12-reference-and-call-record-contract-remains-b11-compatible
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          node-index (exactly-once-index (:nodes core-artifact) :node-id)]
      (testing (str basename extension)
        (doseq [use (:reference-uses core-artifact)]
          (let [node (get node-index (:core-node-id use))]
            (is (= reference-use-keys (set (keys use))))
            (is (= :reference (:core-form node)))
            (is (= reference-attribute-keys
                   (set (keys (:attributes node)))))
            (is (= (select-keys use reference-attribute-keys)
                   (:attributes node)))
            (is (= [(:binding-id use)] (:resolved-binding-ids node)))))
        (doseq [call (:calls core-artifact)]
          (let [node (get node-index (:core-node-id call))
                expected-order
                (into [(:operator-node-id call)]
                      (:argument-node-ids call))]
            (is (= call-record-keys (set (keys call))))
            (is (= :call (:core-form node)))
            (is (= call-attribute-keys
                   (set (keys (:attributes node)))))
            (is (= expected-order (:children node)))
            (is (= expected-order
                   (:ordered-evaluation-node-ids call)))
            (is (= :operator-then-arguments
                   (:evaluation-order call)
                   (get-in node [:evaluation :kind])))
            (is (= :resolved-symbol-call
                   (get-in node [:attributes :dispatch])))))))))

(deftest sh07-b12-alias-call-evaluates-operator-before-arguments
  (let [{:keys [artifact resolution]}
        (alias-resolution-fixture :operator)
        core-artifact (core artifact)
        call
        (first
         (filter
          #(= (:reference-syntax-id resolution)
              (get-in
               (exactly-once-index
                (:reference-uses core-artifact) :core-node-id)
               [(:operator-node-id %) :syntax-id]))
          (:calls core-artifact)))
        expected-order
        (into [(:operator-node-id call)]
              (:argument-node-ids call))]
    (is (map? call))
    (is (= expected-order (:ordered-evaluation-node-ids call)))
    (is (= :operator-then-arguments (:evaluation-order call)))
    (is (= expected-order
           (mapv
            :core-node-id
            (get-in
             (exactly-once-index (:nodes core-artifact) :node-id)
             [(:core-node-id call) :evaluation :order]))))))

(deftest sh07-b12-alias-fully-qualified-and-core-controls-remain-distinct
  (let [artifact
        (first
         (keep
          (fn [basename]
            (let [candidate
                  (file-artifact "accepted" basename ".gravity")
                  orders
                  (set (map :resolution-order
                            (:resolution-table (request candidate))))]
              (when (and
                     (contains? orders :alias-qualified-required-binding)
                     (contains? orders :profile-allowed-core-binding)
                     (contains? orders :fully-qualified-namespace-binding))
                candidate)))
          (sort (accepted-fixtures))))
        resolutions (:resolution-table (request artifact))
        uses (:reference-uses (core artifact))]
    (is (map? artifact))
    (doseq [order [:alias-qualified-required-binding
                   :profile-allowed-core-binding
                   :fully-qualified-namespace-binding]]
      (let [resolution
            (first
             (filter
              (fn [candidate]
                (and
                 (= order (:resolution-order candidate))
                 (some
                  #(= (:reference-syntax-id candidate) (:syntax-id %))
                  uses)))
              resolutions))
            use
            (first
             (filter #(= (:reference-syntax-id resolution) (:syntax-id %))
                     uses))]
        (testing (name order)
          (is (map? resolution))
          (is (map? use))
          (is (= (:binding-id resolution) (:binding-id use)))
          (is (= (:symbol resolution) (:symbol use))))))))

(deftest sh07-b12-identities-are-deterministic-path-neutral-and-provenanced
  (let [basename (first (sort (accepted-fixtures)))
        fixture (fixture-path "accepted" basename ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b12-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/alias.gravity")
        right-path (.resolve temp-root "right/alias.qst")]
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
        (is (= (:alias-table (request left))
               (:alias-table (request right))))
        (is (= (:declared-alias-table (core left))
               (:declared-alias-table (core right))))
        (is (= (:reference-uses (core left))
               (:reference-uses (core right))))
        (is (= (:calls (core left)) (:calls (core right))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in (core left) [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in (core right) [:provenance :actual-source-path])))
        (is (not= (get-in left [:provenance :source-path])
                  (get-in right [:provenance :source-path]))))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b12-alias-products-retain-authenticated-sh06-lineage
  (doseq [basename (sort (accepted-fixtures))
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          authenticated-request (request artifact)
          lineage (:lineage authenticated-request)
          sh06 (:sh06-resolution-artifact artifact)]
      (testing (str basename extension)
        (is (= (:alias-table authenticated-request)
               (get-in sh06 [:namespace-analysis :alias-table])))
        (is (= (:authenticated-sh06-artifact-id lineage)
               (:artifact-id sh06)))
        (is (= (:authenticated-sh06-artifact-id lineage)
               (get-in artifact
                       [:gravity-core-boundary
                        :authenticated-envelope
                        :authenticated-sh06-artifact-id])))
        (is (= (:sh06-semantic-projection-id lineage)
               (get-in (core artifact)
                       [:lineage :sh06-semantic-projection-id])))))))

(deftest sh07-b12-alias-table-alterations-fail-closed
  (let [{:keys [artifact resolution]}
        (alias-resolution-fixture :expression)
        authenticated-request (request artifact)
        alias-record (alias-for-resolution authenticated-request resolution)
        alias-index
        (first
         (keep-indexed
          (fn [index candidate]
            (when (= (:alias alias-record) (:alias candidate)) index))
          (:alias-table authenticated-request)))
        alternate-namespace 'self-hosting.sh07-b12.changed
        cases
        {"removal"
         (update authenticated-request :alias-table
                 #(vec (concat (subvec % 0 alias-index)
                               (subvec % (inc alias-index)))))
         "namespace substitution"
         (assoc-in authenticated-request
                   [:alias-table alias-index :namespace]
                   alternate-namespace)
         "profile substitution"
         (assoc-in authenticated-request
                   [:alias-table alias-index :profile]
                   :application)
         "target substitution"
         (assoc-in authenticated-request
                   [:alias-table alias-index :targets]
                   [:wasm32])
         "dependency substitution"
         (assoc-in authenticated-request
                   [:alias-table alias-index :dependency-artifact-id]
                   (str "sha256:" (apply str (repeat 64 "0"))))
         "duplicate"
         (update authenticated-request :alias-table conj alias-record)
         "over bound"
         (assoc authenticated-request :alias-table
                (vec (repeat (inc maximum-alias-records) alias-record)))
         "malformed record"
         (assoc-in authenticated-request
                   [:alias-table alias-index]
                   {:alias (:alias alias-record)})
         "coherent profile substitution with stale lineage"
         (rebind-request
          (-> authenticated-request
              (assoc-in [:alias-table alias-index :profile] :application)
              (assoc-in [:binding-table
                         (first
                          (keep-indexed
                           (fn [index binding]
                             (when (= (:binding-id resolution)
                                      (:binding-id binding))
                               index))
                           (:binding-table authenticated-request)))
                         :profile-set]
                        [:application])))
         "coherent profile substitution with rebound public identities"
         (rebind-alias-lineage-and-request
          (-> authenticated-request
              (assoc-in [:alias-table alias-index :profile] :application)
              (assoc-in [:binding-table
                         (first
                          (keep-indexed
                           (fn [index binding]
                             (when (= (:binding-id resolution)
                                      (:binding-id binding))
                               index))
                           (:binding-table authenticated-request)))
                         :profile-set]
                        [:application])))
         "coherent duplicate targets with stale lineage"
         (rebind-request
          (-> authenticated-request
              (assoc-in [:alias-table alias-index :targets] [:jvm :jvm])
              (assoc-in [:binding-table
                         (first
                          (keep-indexed
                           (fn [index binding]
                             (when (= (:binding-id resolution)
                                      (:binding-id binding))
                               index))
                           (:binding-table authenticated-request)))
                         :target-set]
                        [:jvm :jvm])))
         "unused alias appended with stale lineage"
         (rebind-request
          (update authenticated-request :alias-table conj
                  (assoc alias-record :alias 'unused)))}]
    (is (integer? alias-index))
    (doseq [[label altered-request] cases]
      (testing label
        (let [result (run-request
                      (:sh06-resolution-artifact artifact)
                      altered-request)
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (map? diagnostic))
          (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= :core-lowering (:stage diagnostic)))
          (is (= true (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b12-declared-alias-product-alterations-fail-replay
  (let [{:keys [artifact]}
        (alias-resolution-fixture :expression)
        records (:declared-alias-table (core artifact))
        altered
        (assoc-in
         artifact
         [:gravity-core-boundary :canonical-core-artifact
          :declared-alias-table 0 :namespace]
         'self-hosting.sh07-b12.changed)
        removed
        (update-in
         artifact
         [:gravity-core-boundary :canonical-core-artifact
          :declared-alias-table]
         pop)]
    (is (seq records))
    (doseq [[label candidate]
            {"namespace substitution" altered "removal" removed}]
      (testing label
        (let [failed (verification-failures candidate artifact)]
          (is (not= artifact candidate))
          (is (contains? failed :declared-alias-table-replay?))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   candidate)))))))))

(deftest sh07-b12-resolution-order-and-binding-alterations-fail-closed
  (let [{:keys [artifact resolution]}
        (alias-resolution-fixture :expression)
        authenticated-request (request artifact)
        resolution-index
        (first
         (keep-indexed
          (fn [index candidate]
            (when (= (:reference-syntax-id resolution)
                     (:reference-syntax-id candidate))
              index))
          (:resolution-table authenticated-request)))
        binding-index
        (first
         (keep-indexed
          (fn [index candidate]
            (when (= (:binding-id resolution) (:binding-id candidate))
              index))
          (:binding-table authenticated-request)))
        alternate-binding
        (first
         (filter #(not= (:binding-id resolution) (:binding-id %))
                 (:binding-table authenticated-request)))
        cases
        (cond->
         {"resolution order"
          (assoc-in authenticated-request
                    [:resolution-table resolution-index :resolution-order]
                    :fully-qualified-namespace-binding)
          "binding namespace"
          (assoc-in authenticated-request
                    [:binding-table binding-index :namespace]
                    'self-hosting.sh07-b12.changed)}
          alternate-binding
          (assoc
           "binding substitution"
           (assoc-in authenticated-request
                     [:resolution-table resolution-index :binding-id]
                     (:binding-id alternate-binding))))]
    (is (integer? resolution-index))
    (is (integer? binding-index))
    (doseq [[label altered-request] cases]
      (testing label
        (let [result (run-request
                      (:sh06-resolution-artifact artifact)
                      altered-request)
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (map? diagnostic))
          (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
          (is (contains? #{"C6-VERIFY" "C6-LOWERING-GAP"}
                         (:rule diagnostic)))
          (is (= true (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b12-public-replay-and-capability-proof-pass
  (doseq [basename (sort (accepted-fixtures))]
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
        (is (= [] (:failed-checks proof))))))
  (let [artifact
        (:artifact
         (alias-resolution-fixture :expression))
        altered
        (update-in
         artifact
         [:gravity-core-boundary :authenticated-core-request :alias-table]
         pop)
        failed (verification-failures altered artifact)]
    (is (not= altered artifact))
    (is (contains? failed :authoritative-products-replay?))
    (is (= :failed
           (:status
            ((required-var 'sh07-core-artifact-verification) altered))))))

(deftest sh07-b12-rejected-fixtures-follow-their-declared-oracles
  (doseq [basename (sort (rejected-fixtures))
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
        (is (= :c5-name-resolution (:diagnostic-family diagnostic)))
        (is (= (:expected-rule oracle) (:rule diagnostic)))
        (is (= (:expected-stage oracle) (:stage diagnostic)))
        (is (= (:expected-severity oracle) (:severity diagnostic)))
        (is (= (:expected-reason oracle)
               (get-in diagnostic [:facts :reason])))
        (is (= (:expected-remediation oracle)
               (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b12-preserves-b11-vector-pattern-products
  (doseq [extension extensions]
    (let [artifact (b11-file-artifact extension)
          core-artifact (core artifact)]
      (testing extension
        (is (= :accepted (:status artifact)))
        (is (= 14 (:schema-version (request artifact))))
        (is (seq (:match-pattern-records core-artifact)))
        (is (some #(= :vector (:pattern-kind %))
                  (:match-pattern-records core-artifact)))
        (is (some #(= :binding (:pattern-kind %))
                  (:match-pattern-records core-artifact)))
        (is (= :passed
               (:status
                ((required-var 'sh07-core-artifact-verification)
                 artifact))))))))

(deftest sh07-b12-claim-boundary-remains-honest
  (let [basename (first (sort (accepted-fixtures)))
        artifact (file-artifact "accepted" basename ".gravity")
        pending
        (set
         (get-in artifact
                 [:execution-boundary :pending-lowering-families]))]
    (is (false? (get-in artifact
                        [:execution-boundary :sh07-complete?])))
    (is (false? (get-in artifact
                        [:execution-boundary :self-hosted?])))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :self-hosted?])))
    (is (true? (get-in artifact
                       [:gravity-core-boundary :clojure-adapter-residual?])))
    (doseq [family
            [:alias-qualified-type-references
             :alias-qualified-var-references
             :alias-qualified-set-mutations
             :keyword-headed-calls
             :var-profile-legality-sh09
             :destructuring-bindings
             :variadic-function-recur
             :recur-type-compatibility
             :general-recursion
             :try-finally
             :map-list-set-record-constructor-patterns
             :variable-width-vector-patterns
             :guard-patterns
             :match-exhaustiveness
             :match-result-type-join]]
      (is (contains? pending family)))))
