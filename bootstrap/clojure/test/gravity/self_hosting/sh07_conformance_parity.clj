(ns gravity.self-hosting.sh07-conformance-parity
  "Bounded semantic parity for the reviewed SH-07 conformance table.

  The table remains a compatibility projection until this harness has been
  independently reviewed.  A row is executed through the same
  `sh07-core-file-artifact` adapter used by the legacy source-coverage tests;
  the result is then compared with the reviewed row metadata.  This namespace
  owns no proof or release authority and deliberately limits one invocation to
  one suite and a bounded list of rows."
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-conformance-table :as table])
  (:import (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption Paths)
           (java.security MessageDigest)))

(def parity-schema :gravity/sh07-conformance-parity-v1)
(def result-schema :gravity/sh07-conformance-row-result-v1)
(def maximum-batch-size 16)
(def default-batch-size 8)
(def supported-extensions #{".gravity" ".qst"})
(def legacy-oracle-id :legacy-sh07-core-file-artifact)
(def simulated-oracle-id :injected-test-oracle)
(def ^:dynamic *resource-lease* nil)
(def ^:dynamic *deadline-ms* nil)

(def ^:private expected-statuses #{:accepted :rejected})

(defn- fail!
  [id message data]
  (throw (ex-info message (merge {:id id} data))))

(defn- ensure!
  [value id message data]
  (when-not value
    (fail! id message data))
  value)

(defn- sha256-id
  [^bytes bytes]
  (let [digest (.digest (MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and (int %) 0xff)) digest)))))

(defn- repository-root
  []
  (loop [candidate (.toPath (io/file (System/getProperty "user.dir")))]
    (if (Files/isRegularFile (.resolve candidate "contracts/verification-catalog.edn")
                             (into-array LinkOption []))
      candidate
      (if-let [parent (.getParent candidate)]
        (recur parent)
        (fail! "SH07-PARITY-ROOT"
               "Could not locate the verification catalog"
               {:catalog "contracts/verification-catalog.edn"})))))

(defn- safe-relative-path
  [relative id]
  (ensure! (and (string? relative)
                (not (str/blank? relative))
                (not (.isAbsolute (Paths/get relative (make-array String 0))))
                (not (str/includes? relative "\\"))
                (not-any? #{".."} (str/split relative #"/")))
           id "Conformance path is not repository-relative" {:path relative})
  (.normalize (.resolve (repository-root) relative)))

(defn- suite-by-id
  [catalog suite-id]
  (let [catalog (table/validate-table catalog)]
    (or (some #(when (= suite-id (:id %)) %) (:suites catalog))
      (fail! "SH07-PARITY-SUITE"
             "Unknown SH-07 conformance suite"
             {:suite-id suite-id
              :available (mapv :id (:suites catalog))}))))

(defn- row-by-id
  [suite row-id]
  (or (some #(when (= row-id (:id %)) %)
            (concat (:accepted suite) (:rejected suite)))
      (fail! "SH07-PARITY-ROW"
             "Row is not present in the selected suite"
             {:suite-id (:id suite) :row-id row-id})))

(defn reviewed-row
  "Return one validated table row by its stable `suite/fixture` id."
  ([row-id]
   (reviewed-row (table/table) row-id))
  ([catalog row-id]
   (let [[suite-id] (str/split (str row-id) #"/" 2)]
     (ensure! (and suite-id (not (str/blank? suite-id)))
              "SH07-PARITY-ROW" "Row id must include a suite id" {:row-id row-id})
     (row-by-id (suite-by-id catalog suite-id) row-id))))

(defn- fixture-path
  [suite row extension]
  (ensure! (contains? supported-extensions extension)
           "SH07-PARITY-EXTENSION"
           "Only reviewed .gravity and .qst peers may execute"
           {:extension extension :row-id (:id row)})
  (let [root (safe-relative-path (:fixture-root suite)
                                 "SH07-PARITY-FIXTURE-ROOT")
        family (if (= :accepted (:status row)) "accepted" "rejected")
        family-root (.resolve root family)
        path (.normalize (.resolve family-root
                                   (str (:basename row) extension)))]
    (ensure! (.startsWith path family-root)
             "SH07-PARITY-FIXTURE-PATH"
             "Fixture path escapes its reviewed suite root"
             {:row-id (:id row) :path (str path)})
    (ensure! (Files/isRegularFile path
                                  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))
             "SH07-PARITY-FIXTURE-MISSING"
             "Fixture peer is missing or not a regular file"
             {:row-id (:id row) :path (str path)})
    (ensure! (not (Files/isSymbolicLink path))
             "SH07-PARITY-FIXTURE-SYMLINK"
             "Symlinked fixture peers are not accepted"
             {:row-id (:id row) :path (str path)})
    path))

(defn- fixture-identity
  [suite row]
  (let [gravity (fixture-path suite row ".gravity")
        qst (fixture-path suite row ".qst")
        gravity-size (Files/size gravity)
        qst-size (Files/size qst)
        max-bytes (* 4 1024 1024)
        _ (ensure! (and (<= gravity-size max-bytes)
                        (<= qst-size max-bytes))
                   "SH07-PARITY-FIXTURE-BOUND"
                   "Fixture exceeds the bounded parity input size"
                   {:row-id (:id row) :gravity-bytes gravity-size :qst-bytes qst-size})
        gravity-bytes (Files/readAllBytes gravity)
        qst-bytes (Files/readAllBytes qst)
        content-id (sha256-id gravity-bytes)]
    (ensure! (java.util.Arrays/equals gravity-bytes qst-bytes)
             "SH07-PARITY-PAIR-BYTES"
             "Co-canonical fixture peers differ in bytes"
             {:row-id (:id row)})
    {:fixture-id content-id
     :pair-id content-id
     :source-content-hash content-id
     :byte-count (alength gravity-bytes)
     :paths {:gravity (str (.relativize (repository-root) gravity))
             :qst (str (.relativize (repository-root) qst))}}))

(defn- nofollow-snapshot
  "Capture bounded no-follow attributes for both fixture peers and components."
  [suite row]
  (let [paths [(fixture-path suite row ".gravity")
               (fixture-path suite row ".qst")]
        root (repository-root)]
    (mapv (fn [path]
            (let [relative (.relativize root path)]
              (loop [current root
                     names (seq (iterator-seq (.iterator relative)))]
                (when (Files/isSymbolicLink current)
                  (fail! "SH07-PARITY-FIXTURE-SYMLINK"
                         "Fixture path component is symbolic"
                         {:row-id (:id row) :path (str current)}))
                (if-let [name (first names)]
                  (recur (.resolve current name) (next names))
                  (let [attrs (Files/readAttributes
                               current java.nio.file.attribute.BasicFileAttributes
                               (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))]
                    {:path (str relative)
                     :size (.size attrs)
                     :modified (.lastModifiedTime attrs)
                     :file-key (str (.fileKey attrs))})))))
          paths)))

(defn- source-span
  [value]
  (if (map? value)
    ;; Actual source paths are provenance, not semantic identity.  Existing
    ;; legacy tests use this same co-canonical placeholder for pair parity.
    (assoc value :source "<co-canonical-source>")
    value))

(defn normalize-diagnostic
  "Normalize one legacy diagnostic without dropping its stable facts.

  The vector order in the enclosing result is semantic.  Source paths are
  replaced with the existing co-canonical marker so `.gravity` and `.qst`
  peers compare equal while their actual paths remain in `:fixture`."
  [diagnostic]
  (ensure! (map? diagnostic)
           "SH07-PARITY-DIAGNOSTIC"
           "Legacy oracle did not return a diagnostic map"
           {:value diagnostic})
  (let [fields [:artifact :rule :id :diagnostic-family :stage :severity :lowering-rule
                :syntax-id :form-id :core-node-id :source-span
                :generated-origin-chain :facts :remediation
                :diagnostic-id-request :namespace :profile :active-profile :target]
        normalized
        (reduce (fn [result key]
                  (if (contains? diagnostic key)
                    (assoc result key
                           (if (= :source-span key)
                             (source-span (get diagnostic key))
                             (get diagnostic key)))
                    result))
                (array-map)
                fields)
        normalized (if (and (not (contains? normalized :rule))
                           (string? (:id diagnostic)))
                    (assoc normalized :rule (:id diagnostic))
                    normalized)
        normalized (if (and (not (contains? normalized :profile))
                           (contains? diagnostic :active-profile))
                    (assoc normalized :profile (:active-profile diagnostic))
                    normalized)
        reason (or (:reason diagnostic)
                   (get-in diagnostic [:facts :reason])
                   (get-in diagnostic [:facts :rule-specific :reason]))
        semantic-rule (or (:semantic-rule diagnostic)
                          (get-in diagnostic [:facts :semantic-rule])
                          (get-in diagnostic [:facts :rule-specific :semantic-rule]))
        fail-closed (or (:fail-closed? diagnostic)
                        (get-in diagnostic [:facts :fail-closed]))]
    (cond-> normalized
      (some? reason) (assoc :reason reason)
      (some? semantic-rule) (assoc :semantic-rule semantic-rule)
      (some? fail-closed) (assoc :fail-closed? fail-closed))))

(defn- extract-diagnostic
  [value]
  (let [data (cond
               (instance? clojure.lang.ExceptionInfo value) (ex-data value)
               (and (map? value) (map? (:exception-data value)))
               (:exception-data value)
               :else value)]
    (let [diagnostic? (fn [candidate]
                        (and (map? candidate)
                             (or (= :gravity/sh07-core-diagnostic (:artifact candidate))
                                 (string? (:rule candidate))
                                 (string? (:id candidate)))
                             (or (:stage candidate)
                                 (:diagnostic-family candidate)
                                 (:severity candidate))))]
      (some #(when (diagnostic? %) %)
            [data (:diagnostic data) value (:diagnostic value)]))))

(defn legacy-semantic-oracle
  "Invoke the exact SH-07 file adapter used by legacy source-coverage tests.

  This is a compatibility oracle only.  It performs no cache lookup and its
  result cannot confer SH-07 authority.  Ordinary exceptions become a failed
  row result; interruption and fatal VM/linkage errors are propagated so the
  caller cannot accidentally turn cancellation or resource failure into a
  semantic rejection."
  [path _row]
  (try
    {:value (bootstrap/sh07-core-file-artifact (str path))}
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch VirtualMachineError fatal
      (throw fatal))
    (catch ThreadDeath fatal
      (throw fatal))
    (catch LinkageError fatal
      (throw fatal))
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Exception exception
      {:raw-host-error {:class (.getName (class exception))
                        :message (.getMessage exception)}})))

(defn- artifact-id
  [value diagnostic]
  (or (:artifact-id value)
      (get-in value [:gravity-core-boundary :canonical-core-artifact :artifact-id])
      (:diagnostic-id-request diagnostic)
      ;; A rejected fixture has no canonical core artifact.  The diagnostic
      ;; identity is nevertheless a stable semantic artifact identity for the
      ;; observed outcome and is explicitly tagged by `:artifact-kind`.
      (when diagnostic
        (sha256-id (.getBytes (pr-str (normalize-diagnostic diagnostic))
                             StandardCharsets/UTF_8)))))

(defn- observed-status
  [value diagnostic raw]
  (cond
    (and (map? value) (= :accepted (:status value))) :accepted
    (map? diagnostic) :rejected
    (map? raw) :error
    :else :error))

(defn normalize-outcome
  "Convert one legacy oracle return into a closed, path-neutral result."
  [row raw]
  (let [value (if (and (map? raw) (contains? raw :value))
                (:value raw)
                raw)
        diagnostic (extract-diagnostic raw)
        status (observed-status value diagnostic raw)
        diagnostics (cond
                      diagnostic [(normalize-diagnostic diagnostic)]
                      (and (map? value) (vector? (:diagnostics value)))
                      (mapv normalize-diagnostic (:diagnostics value))
                      :else [])
        id (artifact-id value diagnostic)
        artifact-kind (cond
                        (= :accepted status) :gravity/sh07-core-artifact
                        (= :rejected status) :gravity/sh07-core-diagnostic
                        :else nil)]
    {:schema result-schema
     :status status
     :artifact-id id
     :artifact-kind artifact-kind
     :diagnostics diagnostics
     :raw-host-error (:raw-host-error raw)}))

(defn- expected-diagnostic-match?
  [oracle diagnostic]
  (let [actual-facts (:facts diagnostic)
        rule-specific (or (:rule-specific actual-facts) {})
        actual-reason (or (:reason diagnostic)
                          (:reason actual-facts)
                          (:reason rule-specific))
        actual-semantic-rule (or (:semantic-rule diagnostic)
                                 (:semantic-rule actual-facts)
                                 (:semantic-rule rule-specific))
        actual-fail-closed (or (:fail-closed? diagnostic)
                               (:fail-closed actual-facts))]
    (and (= (:rule oracle) (:rule diagnostic))
         (= (:stage oracle) (:stage diagnostic))
         (= (:severity oracle) (:severity diagnostic))
         (= (:remediation oracle) (:remediation diagnostic))
         (or (not (contains? oracle :reason))
             (= (:reason oracle) actual-reason))
         (or (not (contains? oracle :semantic-rule))
             (= (:semantic-rule oracle) actual-semantic-rule))
         (or (not (contains? oracle :fail-closed?))
             (= (:fail-closed? oracle) actual-fail-closed))
         (or (not (contains? oracle :facts))
             (= (:facts oracle)
                (select-keys rule-specific (keys (:facts oracle))))))))

(def ^:private complete-diagnostic-fields
  #{:artifact :rule :stage :severity :source-span :generated-origin-chain
    :facts :remediation :namespace :profile :target :lowering-rule})

(defn- diagnostic-contract-complete?
  [oracle diagnostic]
  (let [ids (filter #(contains? diagnostic %) [:diagnostic-id-request :id])
        id (some #(get diagnostic %) ids)]
    (and (map? oracle)
         (map? diagnostic)
         (= 1 (count ids))
         (string? id)
         (boolean (re-matches #"sha256:[0-9a-f]{64}" id))
         (set/subset? complete-diagnostic-fields (set (keys diagnostic)))
         (expected-diagnostic-match? oracle diagnostic))))

(defn- table-parity
  [row outcome]
  (let [expected-status (:status row)
        expected-oracle (:oracle row)
        actual-status (:status outcome)
        diagnostics (:diagnostics outcome)
        status? (= expected-status actual-status)
        diagnostics?
        (if (= :accepted expected-status)
          (empty? diagnostics)
          (and (= 1 (count diagnostics))
               (expected-diagnostic-match? expected-oracle (first diagnostics))))]
    {:status? status?
     :diagnostics? diagnostics?
     :declared-contract-match? (and status? diagnostics?)
     :diagnostic-contract-complete? (and (= :rejected expected-status)
                                         (= 1 (count diagnostics))
                                         (diagnostic-contract-complete? expected-oracle (first diagnostics)))
     :expected-status expected-status
     :actual-status actual-status
     :expected-oracle expected-oracle
     :actual-diagnostics diagnostics
     :exact? (and status? diagnostics?
                  (or (= :accepted expected-status)
                      (and (= 1 (count diagnostics))
                           (diagnostic-contract-complete? expected-oracle (first diagnostics)))))}))

(defn- declaration-value
  [suite]
  (let [relative (get-in suite [:legacy :source-path])
        path (safe-relative-path relative "SH07-PARITY-LEGACY-SOURCE")]
    (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                        (io/reader (.toFile path)))]
      (binding [*read-eval* false]
        (loop [forms []]
          (let [form (read {:eof ::eof} reader)]
            (if (= ::eof form)
              forms
              (recur (conj forms form)))))))))

(defn- declaration-definition
  [forms name]
  (some (fn [form]
          (when (and (seq? form) (= 'def (first form))
                     (= name (str (second form))))
            (nth form 2 nil)))
        forms))

(defn- normalize-declared-oracle
  [value]
  (when (map? value)
    (cond-> {}
      (or (contains? value :rule) (contains? value :expected-rule))
      (assoc :rule (or (:rule value) (:expected-rule value)))
      (or (contains? value :stage) (contains? value :expected-stage))
      (assoc :stage (or (:stage value) (:expected-stage value)))
      (or (contains? value :severity) (contains? value :expected-severity))
      (assoc :severity (or (:severity value) (:expected-severity value)))
      (or (contains? value :reason) (contains? value :expected-reason))
      (assoc :reason (or (:reason value) (:expected-reason value)))
      (or (contains? value :remediation) (contains? value :expected-remediation))
      (assoc :remediation (or (:remediation value) (:expected-remediation value)))
      (or (contains? value :semantic-rule) (contains? value :expected-semantic-rule))
      (assoc :semantic-rule (or (:semantic-rule value) (:expected-semantic-rule value)))
      (or (contains? value :fail-closed?) (contains? value :expected-fail-closed))
      (assoc :fail-closed? (or (:fail-closed? value) (:expected-fail-closed value)))
      (contains? value :facts) (assoc :facts (:facts value)))))

(def ^:private allowed-oracle-declaration-keys
  #{:rule :expected-rule :stage :expected-stage :severity :expected-severity
    :reason :expected-reason :remediation :expected-remediation
    :semantic-rule :expected-semantic-rule :fail-closed? :expected-fail-closed
    :facts})

(defn validate-oracle-declarations
  "Validate table rejection oracles against the legacy namespace declarations.

  Suites whose legacy tests derive oracle data from fixture metadata rather than
  a declaration map are intentionally skipped; `table/validate-table` already
  authenticates those metadata projections."
  ([suite-id]
   (validate-oracle-declarations (table/table) suite-id))
  ([catalog suite-id]
   (let [suite (suite-by-id catalog suite-id)
         legacy (:legacy suite)
         declaration-name (:oracle-declaration legacy)]
     (if-not declaration-name
       {:suite-id suite-id :checked 0 :status :not-declared}
       (let [forms (declaration-value suite)
             declared (declaration-definition forms declaration-name)
             rows (filter #(= :rejected (:status %)) (:rejected suite))]
         (ensure! (map? declared)
                  "SH07-PARITY-ORACLE-DECLARATION"
                  "Legacy oracle declaration is not a map"
                  {:suite-id suite-id :declaration declaration-name})
         (let [row-names (set (map :basename rows))
               declared-names (set (keys declared))]
           (ensure! (= row-names declared-names)
                    "SH07-PARITY-ORACLE-DECLARATION"
                    "Legacy oracle declaration keys differ from rejected fixtures"
                    {:suite-id suite-id :missing (sort (set/difference row-names declared-names))
                     :extra (sort (set/difference declared-names row-names))}))
         (doseq [row rows]
           (let [value (get declared (:basename row))]
             (cond
               (map? value)
               (do (ensure! (set/subset? (set (keys value)) allowed-oracle-declaration-keys)
                            "SH07-PARITY-ORACLE-DECLARATION"
                            "Legacy declaration contains unknown keys"
                            {:suite-id suite-id :row-id (:id row)
                             :unknown (set/difference (set (keys value)) allowed-oracle-declaration-keys)})
                   (let [expected (normalize-declared-oracle value)
                     actual (select-keys (:oracle row) (keys expected))]
                     (ensure! (= expected actual)
                          "SH07-PARITY-ORACLE-DECLARATION"
                          "Table oracle differs from legacy declaration"
                          {:suite-id suite-id :row-id (:id row)
                           :expected expected :actual actual})))
               (keyword? value)
               (ensure! (= value (get-in row [:oracle :reason]))
                        "SH07-PARITY-ORACLE-DECLARATION"
                        "Keyword declaration reason differs from table oracle"
                        {:suite-id suite-id :row-id (:id row)
                         :expected value :actual (get-in row [:oracle :reason])})
               :else
               (fail! "SH07-PARITY-ORACLE-DECLARATION"
                      "Legacy oracle declaration entry has unsupported shape"
                      {:suite-id suite-id :row-id (:id row) :value value}))))
         {:suite-id suite-id :checked (count rows) :status :validated})))))

(defn- same-semantic-outcome?
  [left right]
  (and (= (:status left) (:status right))
       (= (:artifact-id left) (:artifact-id right))
       (= (:artifact-kind left) (:artifact-kind right))
       (= (:diagnostics left) (:diagnostics right))))

(defn run-row
  "Execute one reviewed row through the legacy semantic oracle.

  Options: `:extension` (default `.gravity`) and `:oracle`, a two-argument
  function of path and row.  The callback exists for bounded tests and does
  not change the default legacy adapter or confer authority."
  ([suite row] (run-row suite row {}))
  ([suite row {:keys [extension oracle oracle-id simulated?]
               :or {extension ".gravity" oracle legacy-semantic-oracle
                    oracle-id legacy-oracle-id simulated? false}}]
   (let [catalog (table/table)
         owned-suite (suite-by-id catalog (:id suite))
         owned-row (row-by-id owned-suite (:id row))]
     (ensure! (= row owned-row)
              "SH07-PARITY-ROW-OWNERSHIP"
              "Parity row is not the reviewed manifest row"
              {:suite-id (:id suite) :row-id (:id row)}))
   (ensure! (contains? expected-statuses (:status row))
            "SH07-PARITY-ROW-STATUS"
            "Row status is not accepted or rejected"
            {:row-id (:id row) :status (:status row)})
   (ensure! (fn? oracle) "SH07-PARITY-ORACLE"
            "Semantic oracle must be callable" {:row-id (:id row)})
   (ensure! (keyword? oracle-id) "SH07-PARITY-ORACLE-ID"
            "Semantic oracle identity must be explicit"
            {:row-id (:id row) :oracle-id oracle-id})
   (let [trusted-legacy? (identical? oracle legacy-semantic-oracle)
         simulated? (or simulated? (not trusted-legacy?))
         oracle-id (if trusted-legacy? legacy-oracle-id
                       (do (ensure! (not= oracle-id legacy-oracle-id)
                                    "SH07-PARITY-ORACLE-ID"
                                    "Injected callbacks cannot claim legacy identity"
                                    {:row-id (:id row)})
                           oracle-id))]
   (ensure! (or trusted-legacy? simulated?)
            "SH07-PARITY-ORACLE-ID"
            "Only the reviewed legacy oracle or an explicitly simulated test oracle is allowed"
            {:row-id (:id row) :oracle-id oracle-id})
   (let [path (fixture-path suite row extension)
         before-snapshot (nofollow-snapshot suite row)
         before-identity (fixture-identity suite row)
         bounded? (or simulated?
                     (and trusted-legacy?
                           *resource-lease*
                           (integer? *deadline-ms*)
                           (pos? (long *deadline-ms*))))
         raw (when bounded? (oracle path row))
         outcome (if bounded?
                   (normalize-outcome row raw)
                   {:schema result-schema
                    :status :blocked
                    :artifact-id nil
                    :artifact-kind nil
                    :diagnostics [{:artifact :gravity/sh07-conformance-diagnostic
                                   :rule "SH07-PARITY-RESOURCE"
                                   :stage :resource-acquisition
                                   :severity :error
                                   :reason :resource-lease-or-deadline-required}]
                    :raw-host-error nil})
         after-snapshot (nofollow-snapshot suite row)
         after-identity (fixture-identity suite row)
         _ (ensure! (and (= before-snapshot after-snapshot)
                         (= (:source-content-hash before-identity)
                            (:source-content-hash after-identity)))
                    "SH07-PARITY-MUTATION"
                    "Fixture changed during semantic oracle execution"
                    {:row-id (:id row)})
         identity before-identity]
     {:schema parity-schema
      :suite-id (:id suite)
      :row-id (:id row)
      :extension extension
      :fixture identity
      :outcome outcome
      :authority :non-authoritative
      :authoritative? false
      :oracle oracle-id
      :simulated? (boolean simulated?)
      :blocked? (not bounded?)}))))

(defn- pair-row
  [suite row options]
  (let [gravity (run-row suite row (assoc options :extension ".gravity"))
        qst (run-row suite row (assoc options :extension ".qst"))
        left (:outcome gravity)
        right (:outcome qst)]
    {:row-id (:id row)
     :fixture (:fixture gravity)
     :legacy gravity
     :co-canonical qst
     :table-parity (table-parity row left)
     :table-contract-match? (boolean (get-in (table-parity row left) [:exact?]))
     :co-canonical? (same-semantic-outcome? left right)
     :co-canonical-full-outcome? (same-semantic-outcome? left right)
     :simulated-parity? (and (get-in gravity [:outcome :status])
                             (get-in gravity [:outcome :artifact-id])
                             (get-in gravity [:outcome :artifact-kind])
                             (get-in gravity [:fixture :fixture-id])
                             (get-in gravity [:fixture :pair-id])
                             (get-in (table-parity row left) [:declared-contract-match?])
                             (same-semantic-outcome? left right))
     :exact? (and (not (:simulated? options))
                  (get-in gravity [:outcome :status])
                  (get-in gravity [:outcome :artifact-id])
                  (get-in gravity [:outcome :artifact-kind])
                  (get-in gravity [:fixture :fixture-id])
                  (get-in gravity [:fixture :pair-id])
                  (get-in (table-parity row left) [:exact?])
                  (same-semantic-outcome? left right))}))

(defn- selected-rows
  [suite row-ids]
  (let [all (concat (:accepted suite) (:rejected suite))]
    (if (nil? row-ids)
      (vec all)
      (do
        (ensure! (vector? row-ids)
                 "SH07-PARITY-SELECTION"
                 "Exact parity selection must be a vector of row ids"
                 {:row-ids row-ids})
        (ensure! (= (count row-ids) (count (set row-ids)))
                 "SH07-PARITY-SELECTION"
                 "Exact parity selection contains duplicate row ids"
                 {:row-ids row-ids})
        (mapv #(row-by-id suite %) row-ids)))))

(defn exact-batch-row-ids
  "Return deterministic bounded row-id batches for one suite.

  This helper is pure selection data.  It never executes a fixture and rejects
  batch sizes above the reviewed maximum so callers cannot accidentally launch
  the full 147-row table as one ordinary iteration."
  ([suite-id] (exact-batch-row-ids suite-id default-batch-size))
  ([suite-id batch-size]
   (let [suite (suite-by-id (table/table) suite-id)
         batch-size (long batch-size)]
     (ensure! (and (pos? batch-size) (<= batch-size maximum-batch-size))
              "SH07-PARITY-BATCH-BOUND"
              "Parity batch size exceeds the reviewed bound"
              {:suite-id suite-id :batch-size batch-size
               :maximum maximum-batch-size})
     (->> (concat (:accepted suite) (:rejected suite))
          (map :id)
          vec
          (partition-all batch-size)
          (mapv vec)))))

(defn prove-exact-batch
  "Execute one exact suite batch and produce non-authoritative parity evidence.

  `row-ids` must belong to exactly one suite and contain at most
  `maximum-batch-size` rows.  `:paired?` defaults true, executing both
  co-canonical extensions so artifact and ordered-diagnostic identities are
  compared.  The default legacy oracle is intentionally expensive; ordinary
  iteration should pass a tiny row vector and the existing resource broker
  should own process/timeout coordination."
  ([suite-id row-ids] (prove-exact-batch suite-id row-ids {}))
  ([suite-id row-ids {:keys [oracle oracle-id paired? simulated?]
                      :or {oracle legacy-semantic-oracle
                           oracle-id legacy-oracle-id
                           paired? true simulated? false}}]
   (let [catalog (table/table)
         suite (suite-by-id catalog suite-id)
         declaration-parity (validate-oracle-declarations catalog suite-id)
         rows (selected-rows suite row-ids)]
     (ensure! (or simulated?
                   (and (= oracle-id legacy-oracle-id)
                        paired?
                        *resource-lease*
                        (integer? *deadline-ms*)
                        (pos? (long *deadline-ms*))))
              "SH07-PARITY-RESOURCE"
              "Exact legacy parity requires paired execution under a resource lease and deadline"
              {:suite-id suite-id :oracle-id oracle-id :paired? paired?})
     (ensure! (<= 1 (count rows) maximum-batch-size)
              "SH07-PARITY-BATCH-BOUND"
              "Parity execution requires a non-empty bounded batch"
              {:suite-id suite-id :count (count rows)
               :maximum maximum-batch-size})
     (let [records
           (mapv (fn [row]
                   (if paired?
                     (pair-row suite row {:oracle oracle :oracle-id oracle-id
                                          :simulated? simulated?})
                     (let [legacy (run-row suite row {:oracle oracle
                                                      :oracle-id oracle-id
                                                      :simulated? simulated?})]
                       {:row-id (:id row)
                        :fixture (:fixture legacy)
                        :legacy legacy
                        :co-canonical nil
                        :table-parity (table-parity row (:outcome legacy))
                        :co-canonical? nil
                        :exact? (get-in (table-parity row (:outcome legacy))
                                        [:exact?])})))
                 rows)
           parity-key (if simulated? :simulated-parity? :exact?)
           passed? (every? true? (map parity-key records))
           identity (sha256-id
                     (.getBytes
                      (pr-str {:suite-id suite-id
                               :row-ids (mapv :row-id records)
                               :fixtures (mapv #(get-in % [:fixture :fixture-id])
                                               records)
                               :oracle-id oracle-id
                               :oracle legacy-oracle-id
                               :outcomes (mapv #(get-in % [:legacy :outcome]) records)})
                      StandardCharsets/UTF_8))]
       {:schema parity-schema
        :parity-id identity
        :suite-id suite-id
        :row-ids (mapv :row-id records)
        :oracle-declaration declaration-parity
        :records records
        :counts {:selected (count records)
                 :passed (count (filter parity-key records))
                 :failed (count (remove parity-key records))}
        :status (cond
                  simulated? :simulated
                  passed? :passed
                  :else :failed)
        :simulated? (boolean simulated?)
        :authority :non-authoritative
        :authoritative? false
        :required-fresh-replay? true
        :nonclaims [:authority :integration :proof :release :self-hosting
                    :seed-retirement :safety :performance]}))))

(defn run-suite-batches
  "Run one suite as bounded exact batches; never combines multiple suites."
  ([suite-id] (run-suite-batches suite-id {}))
  ([suite-id {:keys [batch-size] :or {batch-size default-batch-size} :as options}]
   (let [batches (exact-batch-row-ids suite-id batch-size)]
     (mapv #(prove-exact-batch suite-id % options) batches))))
