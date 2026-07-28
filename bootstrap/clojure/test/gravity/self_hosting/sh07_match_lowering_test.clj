(ns gravity.self-hosting.sh07-match-lowering-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_match_lowering_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B10 test source is not on the classpath"
                      {:id "SH07-B10-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B10-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b10")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-fixtures
  #{"branch-local-binding"
    "function-tail-recur"
    "literal-clauses-wildcard"
    "loop-tail-recur"
    "macro-origin-match"
    "nested-match-scope-isolation"
    "repeated-literal-occurrences"
    "scrutinee-branch-effects"
    "sibling-binding-isolation"})
(def ^:private rejected-fixtures
  #{"composite-pattern-deferred"
    "guard-pattern-deferred"
    "missing-scrutinee"
    "no-clauses"
    "odd-pattern-body-tail"
    "outside-branch-binding-use"
    "sibling-branch-binding-use"})
(def ^:private upstream-resolution-fixtures
  #{"outside-branch-binding-use" "sibling-branch-binding-use"})
(def ^:private promoted-b11-fixtures
  #{"composite-pattern-deferred"})
(def ^:private core-lowering-fixtures
  (set/difference
   rejected-fixtures upstream-resolution-fixtures promoted-b11-fixtures))

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

(defn- source-text
  [source-path]
  (slurp source-path :encoding "UTF-8"))

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
        "Required SH-07-B10 coordinator adapter is absent"
        {:id "SH07-B10-ADAPTER-ABSENT"
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
    (get (second clause) :sh07-b10)))

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
       (ex-info "SH-07-B10 records are not uniquely identifiable"
                {:id "SH07-B10-AMBIGUOUS-INDEX"
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

(defn- upstream-diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (and (map? data) (string? (:id data))) data)
        (when (and (map? value) (string? (:id value))) value))))

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

(defn- sorted-group
  [records]
  (sort-by :clause-ordinal records))

(defn- accepted-basenames
  []
  accepted-fixtures)

(defn- rejected-basenames
  []
  rejected-fixtures)

(defn- representative-basename
  [pattern]
  (or (some #(when (str/includes? % pattern) %)
            (sort (accepted-basenames)))
      (throw
       (ex-info "Required SH-07-B10 accepted fixture family is absent"
                {:id "SH07-B10-FIXTURE-FAMILY-ABSENT"
                 :pattern pattern
                 :available (sort (accepted-basenames))}))))

(deftest sh07-b10-fixtures-are-paired-byte-identical-and-cover-the-foundation
  (let [accepted (accepted-basenames)
        rejected (rejected-basenames)]
    (is (seq accepted))
    (is (seq rejected))
    (doseq [family ["accepted" "rejected"]]
      (is (= (if (= family "accepted")
               accepted-fixtures
               rejected-fixtures)
             (fixture-basenames family ".gravity")
             (fixture-basenames family ".qst"))))
    (doseq [family ["accepted" "rejected"]
            basename (fixture-basenames family ".gravity")]
      (is (= (seq (source-bytes
                   (fixture-path family basename ".gravity")))
             (seq (source-bytes
                   (fixture-path family basename ".qst"))))))
    (doseq [required-name ["literal" "wildcard" "binding" "nested"]]
      (is (some #(str/includes? % required-name) accepted)))
    (doseq [required-name ["guard" "composite" "missing"
                           "no-clauses" "odd-pattern-body-tail"]]
      (is (some #(str/includes? % required-name) rejected)))))

(deftest sh07-b10-direct-and-public-routing-use-v12
  (doseq [extension extensions]
    (let [basename "literal-clauses-wildcard"
          direct (direct-artifact "accepted" basename extension)
          public (file-artifact "accepted" basename extension)]
      (testing (str basename extension)
        (is (= :accepted (:status direct) (:status public)))
        (is (= (:artifact-id direct) (:artifact-id public)))
        (is (= (identity-input direct) (identity-input public)))
        (is (= (:match-branch-records (core direct))
               (:match-branch-records (core public))))
        (is (= (:match-decision-skeletons (core direct))
               (:match-decision-skeletons (core public))))
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

(deftest sh07-b10-match-nodes-record-exact-order-and-pending-facts
  (doseq [basename (accepted-basenames)
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          groups (match-groups artifact)
          skeletons
          (exactly-once-index
           (:match-decision-skeletons core-artifact)
           :core-node-id)]
      (testing (str basename extension)
        (is (seq groups))
        (is (= (set (keys groups)) (set (keys skeletons))))
        (doseq [[match-id unsorted-records] groups]
          (let [records (vec (sorted-group unsorted-records))
                node (get nodes match-id)
                attributes (:attributes node)
                scrutinee-id (first (:children node))
                branch-ids (vec (rest (:children node)))
                skeleton (get skeletons match-id)]
            (is (= :match (:core-form node)))
            (is (= (inc (count records)) (count (:children node))))
            (is (= scrutinee-id (:scrutinee-core-node-id (first records))))
            (is (= branch-ids (mapv :branch-core-node-id records)))
            (is (= 0 (:scrutinee-child-index attributes)))
            (is (= (count records) (:branch-count attributes)))
            (is (= (vec (range 1 (inc (count records))))
                   (:branch-child-indexes attributes)))
            (is (= :scrutinee-then-source-ordered-pattern-candidates
                   (:evaluation-order attributes)))
            (is (= :not-asserted-by-sh07-b11
                   (:runtime-reachability attributes)))
            (is (= :source-ordered-pattern-candidates
                   (:selection-policy attributes)))
            (is (= :pending-sh08
                   (:result-type-join attributes)
                   (:exhaustiveness attributes)))
            (is (= (vec (range (count records)))
                   (mapv :clause-ordinal records)))
            (is (= (vec (range 1 (inc (count records))))
                   (mapv :branch-child-index records)))
            (is (= #{(count records)}
                   (set (map :clause-count records))))
            (is (= match-id (:core-node-id skeleton)))
            (is (= scrutinee-id (:scrutinee-core-node-id skeleton)))
            (is (= branch-ids (:branch-core-node-ids skeleton)))
            (is (= :source-ordered-pattern-candidates
                   (:selection-policy skeleton)))
            (is (= :not-asserted-by-sh07-b11
                   (:runtime-reachability skeleton)))
            (is (= :pending-sh08
                   (:result-type-join skeleton)
                   (:exhaustiveness skeleton)))))))))

(deftest sh07-b10-pattern-families-and-binding-scopes-are-explicit
  (doseq [basename (accepted-basenames)
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          records (:match-branch-records (core artifact))
          bindings
          (exactly-once-index (:binding-table (request artifact)) :binding-id)
          resolutions (:resolution-table (request artifact))]
      (doseq [record records]
        (is (contains? #{:literal :wildcard :binding}
                       (:pattern-kind record)))
        (if (= :binding (:pattern-kind record))
          (do
            (is (string? (:pattern-binding-id record)))
            (is (string? (:pattern-binding-scope-id record)))
            (is (= :local
                   (:kind
                    (get bindings (:pattern-binding-id record)))))
            (doseq [use-id (:pattern-binding-use-syntax-ids record)]
              (is (= 1
                     (count
                      (filter
                       #(and (= use-id (:reference-syntax-id %))
                             (= (:pattern-binding-id record)
                                (:binding-id %)))
                       resolutions))))))
          (do
            (is (nil? (:pattern-binding-id record)))
            (is (nil? (:pattern-binding-scope-id record)))
            (is (= [] (:pattern-binding-use-syntax-ids record)))))))))

(deftest sh07-b10-sibling-bindings-are-branch-local
  (doseq [extension extensions]
    (let [basename (representative-basename "sibling-binding")
          artifact (file-artifact "accepted" basename extension)
          binding-records
          (filterv #(= :binding (:pattern-kind %))
                   (:match-branch-records (core artifact)))]
      (is (seq binding-records))
      (is (= (count binding-records)
             (count (set (map :pattern-binding-scope-id
                              binding-records)))))
      (doseq [record binding-records
              sibling binding-records
              :when (not= (:clause-ordinal record)
                          (:clause-ordinal sibling))]
        (is (not=
             (:pattern-binding-scope-id record)
             (:pattern-binding-scope-id sibling)))
        (is (empty?
             (set/intersection
              (set (:pattern-binding-use-syntax-ids record))
              (set (:pattern-binding-use-syntax-ids sibling)))))))))

(deftest sh07-b10-nested-matches-have-distinct-groups-and-global-order
  (doseq [extension extensions]
    (let [basename (representative-basename "nested")
          artifact (file-artifact "accepted" basename extension)
          records (:match-branch-records (core artifact))
          groups (match-groups artifact)]
      (is (< 1 (count groups)))
      (is (= (vec (range (count records))) (mapv :ordinal records)))
      (doseq [[match-id group] groups]
        (let [ordered (vec (sorted-group group))]
          (is (= (vec (range (count ordered)))
                 (mapv :clause-ordinal ordered)))
          (is (= #{match-id} (set (map :core-node-id ordered))))
          (is (= 1
                 (count
                  (set (map :scrutinee-core-node-id ordered))))))))))

(deftest sh07-b10-scrutinee-is-once-and-branches-are-conditional
  (doseq [basename (accepted-basenames)
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          nodes (exactly-once-index (:nodes (core artifact)) :node-id)]
      (doseq [[match-id records] (match-groups artifact)]
        (let [ordered (vec (sorted-group records))
              node (get nodes match-id)
              scrutinee-id (first (:children node))]
          (is (= 1 (count (filter #{scrutinee-id} (:children node)))))
          (is (= #{scrutinee-id}
                 (set (map :scrutinee-core-node-id ordered))))
          (is (= :evaluate-scrutinee-once
                 (:scrutinee-evaluation (first ordered))))
          (is (= #{:conditionally-evaluate-selected-branch}
                 (set (map :branch-evaluation ordered)))))))))

(deftest sh07-b10-scrutinee-and-branch-error-transfers-have-exact-regions
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "scrutinee-branch-effects" extension)
          core-artifact (core artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          records
          (vec (sorted-group
                (first (vals (match-groups artifact)))))
          match-node (get nodes (:core-node-id (first records)))
          scrutinee-id (:scrutinee-core-node-id (first records))
          transfers (:error-transfers core-artifact)
          branch-transfers
          (filterv #(= :match-branch
                       (get-in % [:evaluation-region :kind]))
                   transfers)
          scrutinee-transfers
          (filterv #(not= :match-branch
                         (get-in % [:evaluation-region :kind]))
                   transfers)
          expected-branch-regions
          (set (map :conditional-region (take 2 records)))]
      (is (= 3 (count transfers)))
      (is (= 1 (count scrutinee-transfers)))
      (is (= 2 (count branch-transfers)))
      (is (= expected-branch-regions
             (set (map :evaluation-region branch-transfers))))
      (is (= 2 (count (set (map :evaluation-region
                                branch-transfers)))))
      (doseq [record (take 2 records)]
        (is (= {:kind :match-branch
                :match-syntax-id
                (get-in record [:conditional-region :match-syntax-id])
                :clause-ordinal (:clause-ordinal record)}
               (:conditional-region record))))
      (is (not= :match-branch
                (get-in (first scrutinee-transfers)
                        [:evaluation-region :kind])))
      (is (= [scrutinee-id]
             (mapv :core-node-id
                   (get-in match-node [:evaluation :order]))))
      (is (= [{:index 0 :core-node-id scrutinee-id}]
             (get-in match-node [:evaluation :order]))))))

(deftest sh07-b10-match-branches-preserve-enclosing-tail-recur-targets
  (doseq [extension extensions
          [basename expected-kind]
          [["loop-tail-recur" :loop]
           ["function-tail-recur" :function]]]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          targets
          (exactly-once-index (:recur-targets core-artifact) :target-id)
          transfers (:recur-transfers core-artifact)]
      (testing (str basename extension)
        (is (= 1 (count transfers)))
        (let [transfer (first transfers)
              target (get targets (:target-id transfer))]
          (is (map? target))
          (is (= expected-kind
                 (:target-kind target)
                 (:target-kind transfer)))
          (is (true? (:tail-position transfer)))
          (is (= :nearest-lexical-recur-target
                 (:transfer-policy transfer)))
          (is (= :pending-sh08 (:type-compatibility transfer))))))))

(deftest sh07-b10-match-scrutinee-does-not-inherit-tail-position
  (let [synthetic-path
        (path
         "bootstrap/clojure/fixtures/self-hosting/sh-07-b10/synthetic-non-tail-scrutinee.gravity")
        source
        (str "(ns self-hosting.sh07-b10.synthetic-non-tail-scrutinee\n"
             "  (:profile :meta)\n"
             "  (:target :jvm)\n"
             "  (:effects #{})\n"
             "  (:capabilities #{})\n"
             "  (:safety :safe))\n"
             "(defn invalid [value]\n"
             "  (match (recur value)\n"
             "    _ value))\n")
        result
        (diagnostic-result
         #((required-var 'sh07-core-source-artifact)
           synthetic-path source))
        diagnostic (diagnostic-data result)]
    (is (nil? (:raw-host-error result)))
    (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
    (is (= "C6-VERIFY" (:rule diagnostic)))
    (is (= :recur-tail-position-required
           (get-in diagnostic [:facts :reason])))
    (is (true? (get-in diagnostic [:facts :fail-closed])))))

(deftest sh07-b10-identities-are-deterministic-path-neutral-and-provenanced
  (let [basename (first (sort (accepted-basenames)))
        fixture (fixture-path "accepted" basename ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b10-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/match.gravity")
        right-path (.resolve temp-root "right/match.qst")]
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
        (is (= (:match-branch-records (core left))
               (:match-branch-records (core right))))
        (is (= (:match-decision-skeletons (core left))
               (:match-decision-skeletons (core right))))
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

(deftest sh07-b10-records-bind-to-the-authenticated-sh06-lineage
  (doseq [basename (accepted-basenames)
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          lineage (:lineage (request artifact))]
      (doseq [record (:match-branch-records (core artifact))]
        (is (= (:authenticated-sh06-artifact-id lineage)
               (:authenticated-sh06-artifact-id record)))
        (is (= (:sh06-semantic-projection-id lineage)
               (:sh06-semantic-projection-id record))))
      (doseq [skeleton (:match-decision-skeletons (core artifact))]
        (is (= (:authenticated-sh06-artifact-id lineage)
               (:authenticated-sh06-artifact-id skeleton)))
        (is (= (:sh06-semantic-projection-id lineage)
               (:sh06-semantic-projection-id skeleton)))))))

(deftest sh07-b10-rejections-are-structured-and-oracle-bound
  (let [observed-reasons (atom #{})]
    (doseq [basename core-lowering-fixtures
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
    (is (some #(str/includes? (name %) "guard") @observed-reasons))
    (is (some #(or (str/includes? (name %) "composite")
                   (str/includes? (name %) "pattern"))
              @observed-reasons))
    (is (< 2 (count @observed-reasons)))))

(deftest sh07-b10-fixed-vector-fixture-is-promoted-by-b11
  (doseq [basename promoted-b11-fixtures
          extension extensions]
    (let [artifact
          ((required-var 'sh07-core-file-artifact)
           (fixture-path "rejected" basename extension))]
      (testing (str basename extension)
        (is (= :accepted (:status artifact)))
        (is (= #{:vector :binding :wildcard}
               (set
                (map :pattern-kind
                     (:match-pattern-records (core artifact))))))))))

(deftest sh07-b10-branch-binding-escape-fails-at-name-resolution
  (doseq [basename upstream-resolution-fixtures
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (upstream-diagnostic-data result)]
        (is (= "C5-UNRESOLVED" (:expected-rule declared)))
        (is (= :name-resolution (:expected-stage declared)))
        (is (= :error (:expected-severity declared)))
        (is (= :declare-or-import-binding
               (:expected-remediation declared)))
        (is (not (contains? declared :expected-reason)))
        (is (nil? (:raw-host-error result)))
        (is (map? diagnostic))
        (is (= (:expected-rule declared)
               (or (:rule diagnostic) (:id diagnostic))))
        (is (= (:expected-stage declared) (:stage diagnostic)))
        (is (string? (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))))))

(deftest sh07-b10-substituted-and-stale-inputs-fail-closed
  (let [basename (first (sort (accepted-basenames)))
        artifact (file-artifact "accepted" basename ".gravity")
        authenticated (request artifact)
        substitutions
        [(assoc-in
          authenticated
          [:lineage :sh06-semantic-projection-id]
          "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
         (assoc authenticated :schema-version 10)]]
    (doseq [substituted substitutions]
      (let [result
            (diagnostic-result
             #((required-var 'sh07-core-from-authenticated-request)
               (:sh06-resolution-artifact artifact)
               substituted))
            diagnostic (diagnostic-data result)]
        (is (not= authenticated substituted))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b10-match-product-alterations-fail-replay
  (let [basename (first (sort (accepted-basenames)))
        artifact (file-artifact "accepted" basename ".gravity")
        records (:match-branch-records (core artifact))
        skeletons (:match-decision-skeletons (core artifact))
        first-record (first records)
        node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= (:core-node-id first-record) (:node-id node))
              index))
          (:nodes (core artifact))))
        alterations
        {"branch removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-branch-records]
          pop)
         "branch order"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-branch-records]
          (vec (reverse records)))
         "clause ordinal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-branch-records 0 :clause-ordinal]
          1024)
         "pattern kind"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-branch-records 0 :pattern-kind]
          :composite)
         "skeleton removal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-decision-skeletons]
          (vec (rest skeletons)))
         "selection policy"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :nodes node-index :attributes :selection-policy]
          :unordered)}]
    (doseq [[label altered] alterations]
      (testing label
        (let [failed (verification-failures altered artifact)]
          (is (not= artifact altered))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :match-branch-records-replay?))
          (is (contains? failed :match-decision-skeletons-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b10-public-proof-is-bounded-and-honest
  (doseq [basename (accepted-basenames)]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          proof (:capability-based-proof artifact)
          boundary (:gravity-core-boundary artifact)
          canonical (:canonical-core-artifact boundary)
          maximum
          (get-in boundary
                  [:raw-template-result
                   :bounds :maximum-match-branch-records])]
      (testing basename
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (= 1024 maximum))
        (is (<= (count (:match-branch-records canonical)) maximum))
        (is (= :gravity/sh07-to-c6-core-products-v14
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:match-branch-records-replay? proof)))
        (is (true? (:match-decision-skeletons-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (not-any? #{:patterns}
                      (get-in artifact
                              [:execution-boundary
                               :pending-lowering-families])))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))

(deftest sh07-b10-match-branch-resolver-rejects-over-limit-vectors
  (let [basename (first (sort (accepted-basenames)))]
    (is (= {:status :rejected
            :reason :match-branch-vector-required}
           (execute-core-function
            basename
            'sh07-resolve-match-branch-vector
            [(vec (repeat 1025 {})) []])))))

(deftest sh07-b10-match-decision-resolver-rejects-over-limit-vectors
  (let [basename (first (sort (accepted-basenames)))]
    (is (= {:status :rejected
            :reason :match-decision-skeleton-vector-required}
           (execute-core-function
            basename
            'sh07-resolve-match-decision-skeleton-vector
            [(vec (repeat 1025 {})) []])))))

(deftest sh07-b10-match-record-resolver-rejects-invalid-boundary-values
  (let [basename "branch-local-binding"
        artifact (file-artifact "accepted" basename ".gravity")
        records (:match-branch-records (core artifact))
        literal (first (filter #(= :literal (:pattern-kind %)) records))
        binding (first (filter #(= :binding (:pattern-kind %)) records))
        valid-use-id (:pattern-syntax-id binding)
        cases
        {"negative clause ordinal"
         [(assoc literal :clause-ordinal -1)
          :match-branch-ordinal]
         "wildcard must carry underscore"
         [(assoc literal
                 :pattern-kind :wildcard
                 :pattern-value 'not-wildcard)
          :match-pattern-value]
         "binding must not carry underscore"
         [(assoc binding :pattern-value '_)
          :match-pattern-value]
         "literal must remain scalar"
         [(assoc literal :pattern-value [:composite])
          :match-pattern-value]
         "binding uses remain bounded"
         [(assoc binding
                 :pattern-binding-use-syntax-ids
                 (vec (repeat 1025 valid-use-id)))
          :match-pattern-binding-fields]}]
    (is (map? literal))
    (is (map? binding))
    (is (string? valid-use-id))
    (doseq [[label [record reason]] cases]
      (testing label
        (is (= {:status :rejected :reason reason}
               (execute-core-function
                basename
                'sh07-resolve-match-branch-record
                [record []])))))))
