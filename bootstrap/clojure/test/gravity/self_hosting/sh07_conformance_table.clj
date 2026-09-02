(ns gravity.self-hosting.sh07-conformance-table
  "Static, data-driven SH-07 fixture census.

  This namespace deliberately stops at the conformance-table boundary.  It
  authenticates fixture ownership, pair bytes, source declarations, and
  expected diagnostic metadata, but it does not compile, execute, or dispatch
  a Gravity fixture.  The old semantic, mutation, concurrency, cancellation,
  and security tests therefore remain the execution oracle while this table is
  in its compatibility phase."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str])
  (:import (java.nio ByteBuffer)
           (java.nio.channels FileChannel)
           (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Path Paths StandardOpenOption)))

(def catalog-path "contracts/verification-catalog.edn")
(def table-schema :gravity/sh07-conformance-table-v1)
(def expected-suite-ids
  ["sh07-b1" "sh07-b2" "sh07-b3" "sh07-b4" "sh07-b5" "sh07-b6"
   "sh07-b7" "sh07-b8" "sh07-b9" "sh07-b10" "sh07-b11" "sh07-b12"
   "sh07-b13" "sh07-b15"])
(def expected-module-id "sh07-checked-core")
(def maximum-fixture-peer-bytes (* 256 1024))

(def ^:private table-keys
  #{:schema :version :authority :authoritative? :complete? :source-of-truth
    :suite-order :suites :coverage :preserved-bespoke :nonclaims})
(def ^:private suite-keys
  #{:id :slice :module-id :fixture-root :legacy :accepted :rejected
    :bespoke-boundaries})
(def ^:private legacy-keys
  #{:source-path :accepted-declaration :rejected-declaration
    :oracle-declaration :directory-parity? :excluded})
(def ^:private row-keys
  #{:id :basename :gravity :qst :status :profile :target :oracle})
(def ^:private oracle-keys
  #{:rule :stage :severity :reason :semantic-rule :facts :remediation
    :fail-closed?})
(def ^:private coverage-keys
  #{:suite-count :accepted-count :rejected-count :pair-count :source-of-truth})
(def ^:private statuses #{:accepted :rejected})
(def ^:private expected-slices
  (zipmap expected-suite-ids
          [:SH-07-B1 :SH-07-B2 :SH-07-B3 :SH-07-B4 :SH-07-B5 :SH-07-B6
           :SH-07-B7 :SH-07-B8 :SH-07-B9 :SH-07-B10 :SH-07-B11 :SH-07-B12
           :SH-07-B13 :SH-07-B15]))

(defn- fail!
  [id message data]
  (throw (ex-info message (merge {:id id} data))))

(defn- ensure!
  [value id message data]
  (when-not value
    (fail! id message data))
  value)

(defn- sorted-values
  [values]
  (vec (sort-by pr-str values)))

(defn- exact-keys!
  [value allowed id path]
  (ensure! (map? value) id "Expected a map in the closed conformance schema"
           {:path path :value value})
  (let [actual (set (keys value))]
    (when-not (= actual allowed)
      (fail! id "Conformance schema has missing or unknown keys"
             {:path path
              :missing (sorted-values (set/difference allowed actual))
              :unknown (sorted-values (set/difference actual allowed))})))
  value)

(defn- repository-root
  []
  (loop [candidate (.toPath (io/file (System/getProperty "user.dir")))]
    (if (Files/isRegularFile (.resolve candidate catalog-path)
                             (into-array LinkOption []))
      candidate
      (if-let [parent (.getParent candidate)]
        (recur parent)
        (fail! "SH07-CONFORMANCE-ROOT"
               "Could not locate the repository root"
               {:catalog catalog-path})))))

(defn- root-path
  []
  (repository-root))

(defn- relative-path
  [relative id]
  (ensure! (and (string? relative)
                (not (str/blank? relative))
                (not (.isAbsolute (Paths/get relative (make-array String 0))))
                (not (str/includes? relative "\\"))
                (not-any? #{".."} (str/split relative #"/")))
           id "Conformance path is not a safe repository-relative path"
           {:path relative})
  (.normalize (.resolve (root-path) relative)))

(defn- regular-file-path!
  [path id context]
  (ensure! (Files/isRegularFile path (into-array LinkOption []))
           id "Conformance path is not a regular file"
           (merge context {:path (str path)}))
  (ensure! (not (Files/isSymbolicLink path)) id
           "Symlinked conformance inputs are not accepted"
           (merge context {:path (str path)}))
  path)

(defn- read-bounded-bytes
  [path maximum id message data]
  (let [buffer (ByteBuffer/allocate (inc maximum))]
    (with-open [channel
                (FileChannel/open
                 path
                 (into-array java.nio.file.OpenOption
                             [StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS]))]
      (loop []
        (when (.hasRemaining buffer)
          (let [read-count (.read channel buffer)]
            (cond
              (= -1 read-count) nil
              (pos? read-count) (recur)
              :else
              (fail! id "Bounded input read made no progress" data))))))
    (let [count (.position buffer)]
      (ensure! (<= count maximum) id message
               (assoc data :bytes count :maximum maximum))
      (java.util.Arrays/copyOf (.array buffer) count))))

(defn- read-bounded-fixture-bytes
  [path row-id extension]
  (read-bounded-bytes path maximum-fixture-peer-bytes
                      "SH07-CONFORMANCE-FIXTURE-BOUND"
                      "Fixture peer exceeds the reviewed byte bound"
                      {:row-id row-id :extension extension}))

(defn- read-bounded-edn
  [path id]
  (let [path (regular-file-path! path id {})
        bytes (read-bounded-bytes path (* 4 1024 1024) id
                                  "Conformance input exceeds its bounded size"
                                  {:path (str path)})]
    (try
      (edn/read-string (String. bytes StandardCharsets/UTF_8))
      (catch Throwable error
        (fail! id "Conformance input is malformed EDN"
               {:path (str path) :class (.getName (class error))})))))

(defn read-catalog
  ([] (read-catalog catalog-path))
  ([relative]
   (read-bounded-edn (relative-path relative "SH07-CONFORMANCE-CATALOG-PATH")
                     "SH07-CONFORMANCE-CATALOG-READ")))

(defn- conformance-value
  [catalog-value]
  (ensure! (map? catalog-value) "SH07-CONFORMANCE-CATALOG-SHAPE"
           "Verification manifest must be a map" {})
  (or (:conformance catalog-value)
      (fail! "SH07-CONFORMANCE-MISSING"
             "Verification manifest does not contain a conformance table" {})))

(defn- safe-basename!
  [basename id path]
  (ensure! (and (string? basename)
                (boolean (re-matches #"[A-Za-z0-9][A-Za-z0-9._-]*" basename))
                (not (str/includes? basename "/"))
                (not (str/includes? basename "\\")))
           id "Fixture basename is not safe"
           {:path path :basename basename})
  basename)

(defn- fixture-root-path!
  [fixture-root suite-id]
  (ensure! (and (string? fixture-root)
                (str/starts-with?
                 fixture-root "bootstrap/clojure/fixtures/self-hosting/")
                (not (str/includes? fixture-root "\\")))
           "SH07-CONFORMANCE-FIXTURE-ROOT"
           "Fixture root is outside the reviewed fixture tree"
           {:suite-id suite-id :fixture-root fixture-root})
  (let [path (relative-path fixture-root "SH07-CONFORMANCE-FIXTURE-ROOT")
        expected-parent (.normalize (.resolve (root-path)
                                              "bootstrap/clojure/fixtures/self-hosting"))]
    (ensure! (.startsWith path expected-parent)
             "SH07-CONFORMANCE-FIXTURE-OWNERSHIP"
             "Fixture root escapes the owned fixture tree"
             {:suite-id suite-id :fixture-root fixture-root})
    ;; Do not follow symlink roots or symlink components.  A lexical
    ;; startsWith check alone is insufficient because a reviewed-looking path
    ;; can redirect outside the owned fixture tree.
    (let [symlink-component
          (loop [current expected-parent
                 remaining (seq (iterator-seq (.iterator
                                               (.relativize expected-parent path))))]
            (cond
              (Files/isSymbolicLink current) current
              (seq remaining) (recur (.resolve current (first remaining))
                                     (next remaining))
              :else nil))]
      (ensure! (nil? symlink-component)
               "SH07-CONFORMANCE-FIXTURE-SYMLINK"
               "Fixture root contains a symlinked path component"
               {:suite-id suite-id :path (str path)
                :component (str symlink-component)}))
    (ensure! (Files/isDirectory path (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
             "SH07-CONFORMANCE-FIXTURE-ROOT"
             "Fixture root is missing"
             {:suite-id suite-id :path (str path)})
    path))

(defn- source-forms
  [relative]
  (let [path (relative-path relative "SH07-CONFORMANCE-LEGACY-PATH")]
    (regular-file-path! path "SH07-CONFORMANCE-LEGACY-PATH" {})
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (io/reader (.toFile path)))]
      (binding [*read-eval* false]
        (loop [forms []]
          (let [form (read {:eof ::eof} reader)]
            (if (= ::eof form)
              forms
              (recur (conj forms form)))))))))

(defn- definition-value
  [forms name]
  (some (fn [form]
          (when (and (seq? form) (= 'def (first form))
                     (= name (str (second form))))
            (nth form 2 nil)))
        forms))

(defn- literal-name-set
  [value]
  (when (or (vector? value) (set? value) (seq? value))
    (let [values (vec value)]
      (when (every? string? values) (set values)))))

(defn- declared-names
  [forms declaration]
  (when (string? declaration)
    (literal-name-set (definition-value forms declaration))))

(defn- declared-map
  [forms declaration]
  (when (string? declaration)
    (let [value (definition-value forms declaration)]
      (when (map? value) value))))

(defn- fixture-files
  [fixture-root family suite-id]
  (let [directory (.resolve fixture-root family)]
    (ensure! (and (Files/isDirectory directory
                                   (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
                  (not (Files/isSymbolicLink directory)))
             "SH07-CONFORMANCE-FIXTURE-DIRECTORY"
             "Fixture family directory is missing"
             {:suite-id suite-id :family family})
    (let [entries (vec (.listFiles (.toFile directory)))]
      (doseq [entry entries]
        (when (Files/isSymbolicLink (.toPath entry))
          (fail! "SH07-CONFORMANCE-FIXTURE-SYMLINK"
                 "Fixture census cannot follow symlinked entries"
                 {:suite-id suite-id :family family :path (.getName entry)})))
      (let [pairs
            (reduce (fn [acc entry]
                      (let [name (.getName entry)]
                        (if-let [[_ basename extension]
                                 (re-matches #"(.+)\.(gravity|qst)" name)]
                          (update acc basename (fnil conj #{}) extension)
                          acc)))
                    {}
                    (filter #(.isFile %) entries))]
        (doseq [[basename extensions] pairs]
          (ensure! (= #{"gravity" "qst"} extensions)
                   "SH07-CONFORMANCE-FIXTURE-PAIR"
                   "Fixture census contains a one-sided or orphan peer"
                   {:suite-id suite-id :family family :basename basename
                    :extensions extensions}))
        (set (keys pairs))))))

(defn- row-map
  [suite status]
  (into {} (map (juxt :basename identity)
                (get suite status))))

(defn- row-id-set
  [suite status]
  (set (map :basename (get suite status))))

(defn- ns-clause
  [ns-form key]
  (some (fn [clause]
          (when (and (seq? clause) (= key (first clause)))
            (second clause)))
        (drop 2 ns-form)))

(defn- gravity-declaration
  [path]
  (let [forms (source-forms
               (str (.relativize (root-path) path)))
        ns-form (first forms)]
    (ensure! (and (seq? ns-form) (= 'ns (first ns-form)))
             "SH07-CONFORMANCE-FIXTURE-SOURCE"
             "Fixture does not begin with an ns declaration"
             {:path (str path)})
    {:profile (ns-clause ns-form :profile)
     :target (ns-clause ns-form :target)
     :metadata (or (ns-clause ns-form :metadata) {})}))

(defn- fixture-oracle
  [path suite-key]
  (let [metadata (:metadata (gravity-declaration path))
        raw (get metadata suite-key)]
    (when (map? raw)
      (cond-> {}
        (contains? raw :expected-rule) (assoc :rule (:expected-rule raw))
        (contains? raw :expected-stage) (assoc :stage (:expected-stage raw))
        (contains? raw :expected-severity) (assoc :severity (:expected-severity raw))
        (contains? raw :expected-reason) (assoc :reason (:expected-reason raw))
        (contains? raw :expected-semantic-rule)
        (assoc :semantic-rule (:expected-semantic-rule raw))
        (contains? raw :expected-remediation)
        (assoc :remediation (:expected-remediation raw))
        (contains? raw :expected-fail-closed)
        (assoc :fail-closed? (:expected-fail-closed raw))
        (contains? raw :expected-actual-operand-count)
        (assoc-in [:facts :actual-operand-count]
                  (:expected-actual-operand-count raw))
        (contains? raw :expected-required-operand-count)
        (assoc-in [:facts :required-operand-count]
                  (:expected-required-operand-count raw))))))

(defn- fixture-oracle-key
  [suite]
  ;; B1 predates the per-suite metadata key; all later suites use their
  ;; stable id verbatim.  Keep this mapping explicit so metadata cannot be
  ;; silently skipped when a slice label changes presentation.
  (if (= "sh07-b1" (:id suite))
    :sh07
    (keyword (:id suite))))

(defn- validate-oracle!
  [suite row path]
  (let [oracle (:oracle row)]
    (ensure! (map? oracle) "SH07-CONFORMANCE-ORACLE"
             "Rejected fixture rows require an oracle map"
             {:row-id (:id row)})
    (ensure! (set/subset? (set (keys oracle)) oracle-keys)
             "SH07-CONFORMANCE-ORACLE-KEYS"
             "Oracle contains an unknown field"
             {:row-id (:id row) :unknown (sorted-values (set/difference
                                                         (set (keys oracle))
                                                         oracle-keys))})
    (doseq [key [:rule :stage :severity :remediation]]
      (ensure! (contains? oracle key)
               "SH07-CONFORMANCE-ORACLE-FIELD"
               "Oracle is missing a required stable field"
               {:row-id (:id row) :field key}))
    (let [expected (fixture-oracle path (fixture-oracle-key suite))]
      (when (seq expected)
        (doseq [[key value] expected]
          (ensure! (= value (get oracle key))
                   "SH07-CONFORMANCE-ORACLE-MISMATCH"
                   "Table oracle differs from fixture metadata"
                   {:row-id (:id row) :field key :expected value
                    :actual (get oracle key)}))))
    oracle))

(defn- validate-row!
  [suite status index row]
  (let [suite-id (:id suite)
        row-path [:suites suite-id status index]]
    (exact-keys! row row-keys "SH07-CONFORMANCE-ROW-KEYS" row-path)
    (safe-basename! (:basename row) "SH07-CONFORMANCE-BASENAME" row-path)
    (ensure! (= (str suite-id "/" (:basename row)) (:id row))
             "SH07-CONFORMANCE-ROW-ID"
             "Fixture row id is not stable"
             {:path row-path :id (:id row)})
    (ensure! (= (str (:basename row) ".gravity") (:gravity row))
             "SH07-CONFORMANCE-GRAVITY-NAME"
             "Gravity peer name is not derived from the basename"
             {:row-id (:id row)})
    (ensure! (= (str (:basename row) ".qst") (:qst row))
             "SH07-CONFORMANCE-QST-NAME"
             "QST peer name is not derived from the basename"
             {:row-id (:id row)})
    (ensure! (= status (:status row)) "SH07-CONFORMANCE-STATUS"
             "Fixture row status disagrees with its table"
             {:row-id (:id row) :status (:status row)})
    (ensure! (keyword? (:profile row)) "SH07-CONFORMANCE-PROFILE"
             "Fixture row profile must be a keyword" {:row-id (:id row)})
    (ensure! (keyword? (:target row)) "SH07-CONFORMANCE-TARGET"
             "Fixture row target must be a keyword" {:row-id (:id row)})
    (if (= :rejected status)
      (validate-oracle! suite row
                        (.resolve (fixture-root-path! (:fixture-root suite)
                                                      suite-id)
                                  (str "rejected/" (:gravity row))))
      (ensure! (nil? (:oracle row)) "SH07-CONFORMANCE-ACCEPTED-ORACLE"
               "Accepted fixture rows cannot carry a rejection oracle"
               {:row-id (:id row)}))
    row))

(defn- validate-pair!
  [suite status row]
  (let [root-path (fixture-root-path! (:fixture-root suite) (:id suite))
        family (if (= :accepted status) "accepted" "rejected")
        family-path (.resolve root-path family)
        gravity (regular-file-path! (.resolve family-path (:gravity row))
                                    "SH07-CONFORMANCE-FIXTURE-MISSING"
                                    {:row-id (:id row) :extension ".gravity"})
        qst (regular-file-path! (.resolve family-path (:qst row))
                                "SH07-CONFORMANCE-FIXTURE-MISSING"
                                {:row-id (:id row) :extension ".qst"})
        gravity-bytes (read-bounded-fixture-bytes gravity (:id row) ".gravity")
        qst-bytes (read-bounded-fixture-bytes qst (:id row) ".qst")]
    (ensure! (java.util.Arrays/equals gravity-bytes qst-bytes)
             "SH07-CONFORMANCE-PAIR-BYTES"
             "Gravity and QST fixture peers are not byte-identical"
             {:row-id (:id row) :gravity (str gravity) :qst (str qst)})
    (let [declaration (gravity-declaration gravity)]
      (ensure! (= (:profile row) (:profile declaration))
               "SH07-CONFORMANCE-PROFILE-MISMATCH"
               "Fixture profile is not the reviewed declaration"
               {:row-id (:id row) :expected (:profile declaration)
                :actual (:profile row)})
      (ensure! (= (:target row) (:target declaration))
               "SH07-CONFORMANCE-TARGET-MISMATCH"
               "Fixture target is not the reviewed declaration"
               {:row-id (:id row) :expected (:target declaration)
                :actual (:target row)}))
    row))

(defn- validate-legacy!
  [suite]
  (let [legacy (:legacy suite)
        forms (source-forms (:source-path legacy))
        accepted (row-id-set suite :accepted)
        rejected (row-id-set suite :rejected)
        declared-accepted (declared-names forms (:accepted-declaration legacy))
        declared-rejected-value (when (:rejected-declaration legacy)
                                  (definition-value forms
                                                     (:rejected-declaration legacy)))
        declared-rejected (or (literal-name-set declared-rejected-value)
                              (when (map? declared-rejected-value)
                                (set (keys declared-rejected-value))))
        directory-root (fixture-root-path! (:fixture-root suite) (:id suite))
        directory-accepted (fixture-files directory-root "accepted" (:id suite))
        directory-rejected (fixture-files directory-root "rejected" (:id suite))
        expected-directory? (:directory-parity? legacy)]
    (when declared-accepted
      (ensure! (= declared-accepted accepted)
               "SH07-CONFORMANCE-LEGACY-ACCEPTED"
               "Table accepted rows differ from the legacy declaration"
               {:suite-id (:id suite) :declared (sorted-values declared-accepted)
                :table (sorted-values accepted)}))
    (when declared-rejected
      (ensure! (= declared-rejected rejected)
               "SH07-CONFORMANCE-LEGACY-REJECTED"
               "Table rejected rows differ from the legacy declaration"
               {:suite-id (:id suite) :declared (sorted-values declared-rejected)
                :table (sorted-values rejected)}))
    (if expected-directory?
      (do
        (ensure! (= directory-accepted accepted)
                 "SH07-CONFORMANCE-DIRECTORY-ACCEPTED"
                 "Table accepted rows differ from the canonical directory"
                 {:suite-id (:id suite)})
        (ensure! (= directory-rejected rejected)
                 "SH07-CONFORMANCE-DIRECTORY-REJECTED"
                 "Table rejected rows differ from the canonical directory"
                 {:suite-id (:id suite)}))
      (do
        (ensure! (set/subset? accepted directory-accepted)
                 "SH07-CONFORMANCE-DIRECTORY-ACCEPTED"
                 "A declared accepted fixture is missing from the directory"
                 {:suite-id (:id suite)})
        (ensure! (set/subset? rejected directory-rejected)
                 "SH07-CONFORMANCE-DIRECTORY-REJECTED"
                 "A declared rejected fixture is missing from the directory"
                 {:suite-id (:id suite)})))
    {:suite-id (:id suite)
     :accepted {:table accepted :legacy declared-accepted
                :directory directory-accepted}
     :rejected {:table rejected :legacy declared-rejected
                :directory directory-rejected}
     :exact-directory? expected-directory?}))

(defn coverage-from-rows
  [suites]
  (let [accepted (reduce + (map #(count (:accepted %)) suites))
        rejected (reduce + (map #(count (:rejected %)) suites))]
    {:suite-count (count suites)
     :accepted-count accepted
     :rejected-count rejected
     :pair-count (+ accepted rejected)
     :source-of-truth :rows}))

(defn- validate-suite!
  [suite index]
  (exact-keys! suite suite-keys "SH07-CONFORMANCE-SUITE-KEYS" [:suites index])
  (let [suite-id (:id suite)]
    (ensure! (string? suite-id) "SH07-CONFORMANCE-SUITE-ID"
             "Suite id must be a string" {:index index})
    (ensure! (= (nth expected-suite-ids index nil) suite-id)
             "SH07-CONFORMANCE-SUITE-ORDER"
             "Suite order is not the reviewed SH-07 order"
             {:index index :suite-id suite-id})
    (ensure! (= (get expected-slices suite-id) (:slice suite))
             "SH07-CONFORMANCE-SUITE-SLICE"
             "Suite slice is not stable" {:suite-id suite-id})
    (ensure! (= expected-module-id (:module-id suite))
             "SH07-CONFORMANCE-SUITE-MODULE"
             "Fixture suite is not owned by the checked-core module"
             {:suite-id suite-id :module-id (:module-id suite)})
    (fixture-root-path! (:fixture-root suite) suite-id)
    (exact-keys! (:legacy suite) legacy-keys "SH07-CONFORMANCE-LEGACY-KEYS"
                 [:suites index :legacy])
    (ensure! (boolean? (:directory-parity? (:legacy suite)))
             "SH07-CONFORMANCE-LEGACY-BOOLEAN"
             "Legacy directory parity must be an explicit boolean"
             {:suite-id suite-id})
    (ensure! (vector? (:accepted suite)) "SH07-CONFORMANCE-ACCEPTED"
             "Accepted fixture rows must be a vector" {:suite-id suite-id})
    (ensure! (vector? (:rejected suite)) "SH07-CONFORMANCE-REJECTED"
             "Rejected fixture rows must be a vector" {:suite-id suite-id})
    (doseq [[row-index row] (map-indexed vector (:accepted suite))]
      (validate-row! suite :accepted row-index row)
      (validate-pair! suite :accepted row))
    (doseq [[row-index row] (map-indexed vector (:rejected suite))]
      (validate-row! suite :rejected row-index row)
      (validate-pair! suite :rejected row))
    (let [all-basenames (concat (map :basename (:accepted suite))
                                (map :basename (:rejected suite)))]
      (ensure! (= (count all-basenames) (count (set all-basenames)))
               "SH07-CONFORMANCE-DUPLICATE-BASENAME"
               "A fixture basename appears in both table partitions"
               {:suite-id suite-id}))
    (ensure! (seq (:bespoke-boundaries suite))
             "SH07-CONFORMANCE-BESPOKE"
             "Each table suite must name preserved bespoke boundaries"
             {:suite-id suite-id})
    (validate-legacy! suite)
    suite))

(defn validate-table
  "Validate the static fixture table and return it unchanged.

  Validation is intentionally bounded and read-only.  No fixture is compiled
  or executed here, so this function cannot confer execution, proof, release,
  integration, self-hosting, safety, or performance authority."
  ([] (validate-table (conformance-value (read-catalog))))
  ([table]
   (exact-keys! table table-keys "SH07-CONFORMANCE-TABLE-KEYS" [])
   (ensure! (= table-schema (:schema table)) "SH07-CONFORMANCE-SCHEMA"
            "Conformance table schema is not recognized" {:schema (:schema table)})
   (ensure! (= 1 (:version table)) "SH07-CONFORMANCE-VERSION"
            "Conformance table version is not recognized" {:version (:version table)})
   (ensure! (= :non-authoritative (:authority table))
            "SH07-CONFORMANCE-AUTHORITY"
            "Fixture table cannot confer authority" {})
   (ensure! (false? (:authoritative? table)) "SH07-CONFORMANCE-AUTHORITY"
            "Fixture table cannot be authoritative" {})
   (ensure! (false? (:complete? table)) "SH07-CONFORMANCE-COMPLETE"
            "Compatibility fixture table cannot claim completion" {})
   (ensure! (= :reviewed-fixture-rows (:source-of-truth table))
            "SH07-CONFORMANCE-SOURCE" "Unexpected table source" {})
   (ensure! (= expected-suite-ids (:suite-order table))
            "SH07-CONFORMANCE-SUITE-ORDER"
            "Table suite order is not exact" {})
   (ensure! (= (count expected-suite-ids) (count (:suites table)))
            "SH07-CONFORMANCE-SUITE-COUNT"
            "Table does not contain all SH-07 microfeature suites" {})
   (doseq [[index suite] (map-indexed vector (:suites table))]
     (validate-suite! suite index))
   (let [actual-coverage (coverage-from-rows (:suites table))]
     (exact-keys! (:coverage table) coverage-keys "SH07-CONFORMANCE-COVERAGE-KEYS"
                  [:coverage])
     (ensure! (= actual-coverage (:coverage table))
              "SH07-CONFORMANCE-COVERAGE"
              "Manifest coverage does not equal the row-derived census"
              {:expected actual-coverage :actual (:coverage table)}))
   (ensure! (seq (:preserved-bespoke table)) "SH07-CONFORMANCE-BESPOKE"
            "Bespoke semantic boundaries must remain named" {})
   (ensure! (seq (:nonclaims table)) "SH07-CONFORMANCE-NONCLAIMS"
            "The table must carry explicit nonclaims" {})
   table))

(defn table
  []
  (validate-table))

(defn suites
  ([] (suites (table)))
  ([table-value] (:suites (validate-table table-value))))

(defn rows
  ([] (rows (table)))
  ([table-value]
   (mapcat (fn [suite]
             (concat (:accepted suite) (:rejected suite)))
           (suites table-value))))

(defn legacy-parity
  "Return the independently checked legacy fixture census for each suite.

  This is accounting evidence only.  It intentionally does not invoke any
  legacy test namespace or compiler adapter."
  ([] (legacy-parity (table)))
  ([table-value]
   (mapv validate-legacy! (suites table-value))))

(defn fixture-census
  "Return one authenticated, table-owned fixture census.

  Pair entries are exposed only after this suite has validated both peer files,
  their exact bytes, declarations, reviewed rows, and legacy directory parity.
  This is static accounting evidence and grants no execution authority."
  [suite-id]
  (let [table-value (conformance-value (read-catalog))
        index (.indexOf ^java.util.List expected-suite-ids suite-id)]
    (ensure! (not (neg? index)) "SH07-CONFORMANCE-SUITE"
             "Fixture census suite is not reviewed" {:suite-id suite-id})
    (let [suite (validate-suite! (nth (:suites table-value) index nil) index)
          parity (validate-legacy! suite)]
      {:suite-id suite-id
       :accepted (set (map :basename (:accepted suite)))
       :rejected (set (map :basename (:rejected suite)))
       :directory-accepted (get-in parity [:accepted :directory])
       :directory-rejected (get-in parity [:rejected :directory])
       :exact-directory? (:exact-directory? parity)
       :pairs (mapv #(assoc (select-keys % [:id :basename :status])
                            :authenticated? true)
                    (concat (:accepted suite) (:rejected suite)))})))

(defn run-row
  "Expose an optional non-semantic callback boundary.

  The callback receives one already-validated row and is owned by a caller's
  executor.  This helper never supplies a compiler, process, cache, or proof
  implementation and always returns a non-authoritative envelope."
  [row callback]
  (ensure! (fn? callback) "SH07-CONFORMANCE-CALLBACK"
           "Conformance callback must be a function" {})
  {:row-id (:id row)
   :authority :non-authoritative
   :authoritative? false
   :status :callback-result
   :result (callback row)})
