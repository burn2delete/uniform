(ns gravity.bootstrap-free-leaf-test-runner
  "The small, bootstrap-free Stage 0 leaf test runner.

  The catalog in this namespace is deliberately static.  Discovering tests at
  runtime made the old coordinator runner convenient, but it also made it too
  easy for a new test to cross the bootstrap boundary without being reviewed.
  This runner therefore validates a checked-in catalog before it requires any
  test namespace.  Test namespaces are then run in catalog order, one fresh
  JVM per namespace, using clojure.test's ordinary namespace and per-test
  fixtures."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :as test])
  (:import [java.io ByteArrayOutputStream Closeable InputStream PushbackReader
            StringReader]
           [java.lang Process ProcessHandle]
           [java.nio.charset StandardCharsets]
           [java.nio.file Files LinkOption Path Paths]
           [java.util.concurrent TimeUnit]))

(def ^:const catalog-schema
  :gravity/bootstrap-free-leaf-test-catalog-v1)

(def ^:const summary-schema
  :gravity/bootstrap-free-leaf-test-summary-v1)

(def ^:const expected-catalog-count 39)

(def excluded-top-level-test-files
  "The six top-level tests that intentionally remain outside the leaf runner."
  #{"bootstrap_test.clj"
    "cli_test.clj"
    "diagnostics_test.clj"
    "reader_primitives_test.clj"
    "source_span_test.clj"
    "c2_pass_cache_test.clj"})

(def groups
  "The only groups accepted by --group."
  #{:foundation-reader :c2-c3 :compiler})

(def ^:private catalog-stems
  ;; Keep this list literal and sorted.  It is the reviewed Stage 0 surface,
  ;; not a filesystem discovery result.
  ["c10_safety_analysis"
   "c11_mir"
   "c12_domain_ir"
   "c13_optimization"
   "c14_lowering"
   "c15_diagnostics"
   "c16_incremental"
   "c17_plugin"
   "c18_verification"
   "c2_artifact_identity"
   "c2_lexical_validation"
   "c2_reader_diagnostics"
   "c2_reader_product_projection"
   "c2_source_identity"
   "c3_artifact_identity"
   "c3_literal_projection"
   "c3_reader_integrity"
   "c3_syntax_construction"
   "c3_syntax_diagnostics"
   "c3_syntax_evidence"
   "c3_syntax_verification"
   "c4_macro_evidence"
   "c5_name_resolution"
   "c6_core_lowering"
   "c7_type_checker"
   "c8_effect_checker"
   "c9_ownership_checker"
   "compiler_verification_shared"
   "darwin_publication"
   "digest"
   "macro_expansion"
   "optimization_lowering"
   "reader_cursor"
   "reader_diagnostic_policy"
   "reader_host_oracle"
   "reader_namespace"
   "source_unit"
   "syntax_object_stream"
   "syntax_origin"])

(defn- hyphenate
  [value]
  (str/replace value "_" "-"))

(defn- group-for-stem
  [stem]
  (cond
    (or (str/starts-with? stem "c2_")
        (str/starts-with? stem "c3_"))
    :c2-c3

    (or (str/starts-with? stem "reader_")
        (contains? #{"digest" "source_unit" "syntax_object_stream"
                     "syntax_origin"}
                   stem))
    :foundation-reader

    :else
    :compiler))

(defn- entry-for-stem
  [stem]
  (let [name (hyphenate stem)]
    {:id name
     :namespace (symbol (str "gravity." name "-test"))
     :source-path (str "bootstrap/clojure/src/gravity/" stem ".clj")
     :test-path (str "bootstrap/clojure/test/gravity/" stem "_test.clj")
     :group (group-for-stem stem)
     :jvm-options (if (= stem "darwin_publication")
                    ["-J--enable-native-access=ALL-UNNAMED"]
                    [])}))

(defn- canonical-entry
  [entry]
  (sorted-map
   :id (:id entry)
   :namespace (:namespace entry)
   :source-path (:source-path entry)
   :test-path (:test-path entry)
   :group (:group entry)
   :jvm-options (vec (:jvm-options entry))))

(def catalog
  "The reviewed, deterministic catalog of bootstrap-free top-level tests."
  (->> catalog-stems
       (map entry-for-stem)
       (map canonical-entry)
       (sort-by (comp str :namespace))
       vec))

(defn- sha256-hex
  [value]
  (let [digest (java.security.MessageDigest/getInstance "SHA-256")
        bytes (if (instance? (Class/forName "[B") value)
                value
                (.getBytes (str value) StandardCharsets/UTF_8))]
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and (int %) 0xff))
                     (.digest digest bytes))))))

(defn canonical-catalog
  "Returns the exact value whose EDN representation is hashed.

  The projection intentionally excludes vars, filesystem objects, and any
  runtime state so the hash is stable across checkouts and JVMs."
  ([entries]
   (mapv canonical-entry entries))
  ([]
   (canonical-catalog catalog)))

(defn catalog-hash-of
  [entries]
  (sha256-hex (pr-str (canonical-catalog entries))))

(def catalog-hash
  "The SHA-256 identity of the canonical catalog."
  (catalog-hash-of catalog))

(def catalog-hash-value catalog-hash)

(def ^:private no-link-options
  (make-array LinkOption 0))

(def ^:private no-follow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- find-repository-root
  [^Path start]
  (loop [path start]
    (cond
      (nil? path)
      nil

      (and (Files/isRegularFile (.resolve path "deps.edn") no-link-options)
           (.isDirectory
            (.toFile (.resolve path "bootstrap/clojure/test/gravity"))))
      path

      :else
      (recur (.getParent path)))))

(defn repository-root
  "Locates the checkout containing the test classpath.

  This is public so the SH01 contract test can audit the same paths as the
  runner without duplicating a second root-discovery policy."
  []
  (let [resource-root
        (when-let [resource (io/resource
                             "gravity/bootstrap_free_leaf_test_runner.clj")]
          (when (= "file" (.getProtocol resource))
            (find-repository-root (.toPath (io/file (.toURI resource))))))
        cwd-root (find-repository-root
                  (.toPath (io/file (System/getProperty "user.dir"))))]
    (if-let [root (or resource-root cwd-root)]
      (.toRealPath root no-link-options)
      (throw (ex-info "Could not locate the Gravity repository root"
                      {:id "S0LEAF-REPOSITORY-ROOT"})))))

(defn- safe-relative-path
  [relative-path]
  (when-not (and (string? relative-path)
                 (not (str/blank? relative-path))
                 (not (str/includes? relative-path "\\")))
    (throw (ex-info "Repository path must be a non-empty portable relative path"
                    {:id "S0LEAF-REPOSITORY-PATH"
                     :path relative-path})))
  (let [path (Paths/get relative-path (make-array String 0))]
    (when (or (.isAbsolute path)
              (some #{".."} (map str (iterator-seq (.iterator path)))))
      (throw (ex-info "Repository path escapes the checkout"
                      {:id "S0LEAF-REPOSITORY-PATH"
                       :path relative-path})))
    path))

(defn- reject-symbolic-link-components!
  [^Path root ^Path relative]
  (loop [current root
         names (seq (iterator-seq (.iterator relative)))]
    (when-let [name (first names)]
      (let [candidate (.resolve current ^Path name)]
        (when (and (Files/exists candidate no-follow-links)
                   (Files/isSymbolicLink candidate))
          (throw (ex-info "Repository path contains a symbolic link"
                          {:id "S0LEAF-REPOSITORY-SYMLINK"
                           :path (str relative)
                           :component (str (.relativize root candidate))})))
        (recur candidate (next names))))))

(defn repository-path
  [relative-path]
  (let [root (repository-root)
        relative (safe-relative-path relative-path)
        resolved (.normalize (.resolve root relative))]
    (when-not (.startsWith resolved root)
      (throw (ex-info "Repository path escapes the checkout"
                      {:id "S0LEAF-REPOSITORY-PATH"
                       :path relative-path})))
    (reject-symbolic-link-components! root relative)
    (.toFile resolved)))

(defn owned-top-level-test-paths
  "Returns all currently present top-level *_test.clj paths, minus exclusions."
  []
  (let [root (repository-path "bootstrap/clojure/test/gravity")]
    (->> (.listFiles root)
         (filter #(and (.isFile %)
                       (str/ends-with? (.getName %) "_test.clj")
                       (not (contains? excluded-top-level-test-files
                                       (.getName %)))))
         (map #(.relativize (repository-root) (.toPath %)))
         (map str)
         sort
         vec)))

(def top-level-test-paths owned-top-level-test-paths)

(def ^:private component-contract-relative-path
  "contracts/stage0-clojure-components.json")

(def ^:private component-contract-schema
  "gravity/stage0-clojure-components-v1")

(defn- json-error
  [message details]
  (throw (ex-info message
                  (merge {:id "S0LEAF-COMPONENT-CONTRACT-JSON"}
                         details))))

(defn- json-whitespace?
  [character]
  (contains? #{\space \tab \newline \return} character))

(defn- skip-json-whitespace!
  [^PushbackReader reader]
  (loop [value (.read reader)]
    (if (and (not= -1 value)
             (json-whitespace? (char value)))
      (recur (.read reader))
      value)))

(defn- unread-json!
  [^PushbackReader reader value]
  (when-not (= -1 value)
    (.unread reader value)))

(defn- read-json-string!
  [^PushbackReader reader]
  (let [output (StringBuilder.)]
    (loop []
      (let [value (.read reader)]
        (cond
          (= -1 value)
          (json-error "Unterminated JSON string" {})

          (= \" (char value))
          (str output)

          (= \\ (char value))
          (let [escaped (.read reader)]
            (when (= -1 escaped)
              (json-error "Unterminated JSON escape" {}))
            (case (char escaped)
              \" (.append output \" )
              \\ (.append output \\)
              \/ (.append output \/)
              \b (.append output \backspace)
              \f (.append output \formfeed)
              \n (.append output \newline)
              \r (.append output \return)
              \t (.append output \tab)
              \u (let [values (repeatedly 4 #(.read reader))]
                   (when (some #(= -1 %) values)
                     (json-error "Unterminated JSON unicode escape" {}))
                   (let [digits (apply str (map char values))]
                     (when-not (re-matches #"[0-9A-Fa-f]{4}" digits)
                       (json-error "Malformed JSON unicode escape"
                                   {:digits digits}))
                     (.append output
                              (char (Integer/parseInt digits 16)))))
              (json-error "Unsupported JSON escape"
                          {:escape (str (char escaped))}))
            (recur))

          (< value 0x20)
          (json-error "JSON string contains an unescaped control character"
                      {:codepoint value})

          :else
          (do (.append output (char value))
              (recur)))))))

(declare read-json-value!)

(defn- read-json-literal!
  [^PushbackReader reader first-character suffix value]
  (doseq [expected suffix]
    (let [actual (.read reader)]
      (when (or (= -1 actual)
                (not= expected (char actual)))
        (json-error "Malformed JSON literal"
                    {:literal (str first-character suffix)}))))
  value)

(defn- json-number-character?
  [character]
  (boolean (re-matches #"[0-9eE+\-.]" (str character))))

(defn- read-json-number!
  [^PushbackReader reader first-character]
  (let [token
        (loop [output (StringBuilder. (str first-character))]
          (let [value (.read reader)]
            (if (and (not= -1 value)
                     (json-number-character? (char value)))
              (recur (.append output (char value)))
              (do
                (unread-json! reader value)
                (str output)))))]
    (when-not (re-matches #"-?(?:0|[1-9][0-9]*)(?:\.[0-9]+)?(?:[eE][+-]?[0-9]+)?"
                          token)
      (json-error "Malformed JSON number" {:token token}))
    (try
      (if (re-find #"[.eE]" token)
        (bigdec token)
        (bigint token))
      (catch NumberFormatException exception
        (json-error "JSON number is out of range"
                    {:token token
                     :cause (.getMessage exception)})))))

(defn- read-json-array!
  [^PushbackReader reader]
  (let [first-value (skip-json-whitespace! reader)]
    (if (= (int \]) first-value)
      []
      (do
        (unread-json! reader first-value)
        (loop [values []]
          (let [value (read-json-value! reader)
                delimiter (skip-json-whitespace! reader)]
            (cond
              (= (int \,) delimiter)
              (recur (conj values value))

              (= (int \]) delimiter)
              (conj values value)

              :else
              (json-error "JSON array requires comma or closing bracket"
                          {:delimiter delimiter}))))))))

(defn- read-json-object!
  [^PushbackReader reader]
  (let [first-key (skip-json-whitespace! reader)]
    (if (= (int \}) first-key)
      {}
      (do
        (unread-json! reader first-key)
        (loop [result {}]
          (let [quote-value (skip-json-whitespace! reader)]
            (when-not (= (int \") quote-value)
              (json-error "JSON object key must be a string"
                          {:value quote-value}))
            (let [key (read-json-string! reader)
                  colon (skip-json-whitespace! reader)]
              (when (contains? result key)
                (json-error "JSON object repeats a key" {:key key}))
              (when-not (= (int \:) colon)
                (json-error "JSON object key requires a colon" {:key key}))
              (let [value (read-json-value! reader)
                    delimiter (skip-json-whitespace! reader)
                    updated (assoc result key value)]
                (cond
                  (= (int \,) delimiter)
                  (recur updated)

                  (= (int \}) delimiter)
                  updated

                  :else
                  (json-error "JSON object requires comma or closing brace"
                              {:key key :delimiter delimiter}))))))))))

(defn- read-json-value!
  [^PushbackReader reader]
  (let [value (skip-json-whitespace! reader)]
    (when (= -1 value)
      (json-error "JSON value is missing" {}))
    (let [character (char value)]
      (case character
        \{ (read-json-object! reader)
        \[ (read-json-array! reader)
        \" (read-json-string! reader)
        \t (read-json-literal! reader character "rue" true)
        \f (read-json-literal! reader character "alse" false)
        \n (read-json-literal! reader character "ull" nil)
        (if (or (= character \-)
                (Character/isDigit character))
          (read-json-number! reader character)
          (json-error "Unsupported JSON value"
                      {:value (str character)}))))))

(defn read-strict-component-contract
  "Reads one strict JSON value for the normative Stage0 component contract."
  ([]
   (read-strict-component-contract
    (slurp (repository-path component-contract-relative-path))))
  ([text]
   (with-open [reader (PushbackReader. (StringReader. (str text)))]
     (let [value (read-json-value! reader)
           trailing (skip-json-whitespace! reader)]
       (when-not (= -1 trailing)
         (json-error "Trailing data follows the JSON contract"
                     {:value trailing}))
       value))))

(defn- contract-leaf-tuple
  [component]
  (let [test-record (get component "test")
        tuple {:id (get component "id")
               :source-path (get-in component ["source" "path"])
               :test-path (get test-record "path")
               :namespace (get test-record "namespace")
               :group (get component "leaf_execution_group")}]
    (when-not (every? string? (vals tuple))
      (throw (ex-info "Bootstrap-free component tuple fields must be strings"
                      {:id "S0LEAF-COMPONENT-CONTRACT"
                       :tuple tuple})))
    (when-not (contains? groups (keyword (:group tuple)))
      (throw (ex-info "Bootstrap-free component has an unknown execution group"
                      {:id "S0LEAF-COMPONENT-CONTRACT"
                       :tuple tuple})))
    (-> tuple
        (update :namespace symbol)
        (update :group keyword))))

(defn component-contract-leaf-tuples
  "Returns the exact reviewed bootstrap-free catalog tuples from the contract."
  ([]
   (component-contract-leaf-tuples (read-strict-component-contract)))
  ([contract]
   (when-not (= component-contract-schema (get contract "schema"))
     (throw (ex-info "Unexpected Stage0 component contract schema"
                     {:id "S0LEAF-COMPONENT-CONTRACT"
                      :schema (get contract "schema")})))
   (let [components (get contract "components")]
     (when-not (vector? components)
       (throw (ex-info "Stage0 component contract components must be a vector"
                       {:id "S0LEAF-COMPONENT-CONTRACT"})))
     (->> components
          (filter #(= "bootstrap-free" (get-in % ["test" "lane"])))
          (map contract-leaf-tuple)
          (sort-by (comp str :namespace))
          vec))))

(defn- catalog-leaf-tuples
  [entries]
  (mapv #(select-keys % [:id :source-path :test-path :namespace :group])
        entries))

(defn- ownership-bootstrap-free-test-paths
  []
  (let [record (edn/read-string
                (slurp (repository-path
                        "docs/self-hosting-slice-ownership.edn")))
        module-paths (->> (:module-owners record)
                          keys
                          (filter #(and (string? %)
                                        (str/starts-with?
                                         % "bootstrap/clojure/test/gravity/")
                                        (not (str/includes?
                                              (subs % (count "bootstrap/clojure/test/gravity/"))
                                              "/"))
                                        (str/ends-with? % "_test.clj")))
                          set)]
    (-> (reduce disj module-paths (:bootstrap-compatibility-tests record))
        (disj "bootstrap/clojure/test/gravity/bootstrap_test.clj"))))

(defn- relative-path?
  [value]
  (try
    (safe-relative-path value)
    true
    (catch clojure.lang.ExceptionInfo _
      false)))

(defn- first-ns-symbol
  [file]
  (some-> (re-find #"(?m)^\s*\(ns\s+([^\s\)]+)"
                   (slurp file))
          second
          symbol))

(defn- validation-error
  [kind details]
  {:kind kind :details details})

(defn validate-catalog!
  "Validates the static catalog and the current owned test-file surface.

  Validation is intentionally fail-closed and occurs before any catalog test
  namespace is required.  The successful return value is true; callers that
  need detail can use catalog-validation-report."
  ([]
   (validate-catalog! catalog))
  ([entries]
   (let [entries (vec entries)
         sorted-entries (vec (sort-by (comp str :namespace) entries))
         ids (mapv :id entries)
         namespaces (mapv :namespace entries)
         source-paths (mapv :source-path entries)
         test-paths (mapv :test-path entries)
         shape-errors
         (vec
          (concat
           (when (not= expected-catalog-count (count entries))
             [(validation-error :count (count entries))])
           (when (not= entries sorted-entries)
             [(validation-error :order :namespace)])
           (when (not= (count ids) (count (distinct ids)))
             [(validation-error :duplicate-ids ids)])
           (when (not= (count namespaces) (count (distinct namespaces)))
             [(validation-error :duplicate-namespaces namespaces)])
           (when (not= (count source-paths) (count (distinct source-paths)))
             [(validation-error :duplicate-source-paths source-paths)])
           (when (not= (count test-paths) (count (distinct test-paths)))
             [(validation-error :duplicate-test-paths test-paths)])
           (mapcat
            (fn [entry]
              (let [{:keys [id namespace source-path test-path group jvm-options]} entry
                    name (some-> test-path
                                 (str/split #"/")
                                 last
                                 (str/replace #"_test\.clj$" ""))
                    expected-jvm-options
                    (if (= name "darwin_publication")
                      ["-J--enable-native-access=ALL-UNNAMED"]
                      [])
                    source-file (repository-path source-path)
                    test-file (repository-path test-path)]
                (concat
                 (when-not (and (string? id) (not (str/blank? id)))
                   [(validation-error :id entry)])
                 (when-not (and (symbol? namespace)
                                (re-matches #"gravity\..+-test"
                                            (str namespace)))
                   [(validation-error :namespace entry)])
                 (when-not (contains? groups group)
                   [(validation-error :group entry)])
                 (when-not (and (vector? jvm-options)
                                (every? #(and (string? %)
                                              (str/starts-with? % "-J"))
                                        jvm-options))
                   [(validation-error :jvm-options entry)])
                 (when (and name (not= expected-jvm-options jvm-options))
                   [(validation-error :jvm-options-profile
                                      [name expected-jvm-options jvm-options])])
                 (when-not (relative-path? source-path)
                   [(validation-error :source-path entry)])
                 (when-not (relative-path? test-path)
                   [(validation-error :test-path entry)])
                 (when-not (.isFile source-file)
                   [(validation-error :missing-source-path source-path)])
                 (when-not (.isFile test-file)
                   [(validation-error :missing-test-path test-path)])
                 (when (and (.isFile test-file)
                            (not= namespace (first-ns-symbol test-file)))
                   [(validation-error :test-namespace
                                      [test-path namespace
                                       (first-ns-symbol test-file)])])
                 (when (and (.isFile source-file)
                            (not= (symbol (str "gravity." (hyphenate name)))
                                  (first-ns-symbol source-file)))
                   [(validation-error :source-namespace
                                      [source-path
                                       (symbol (str "gravity." (hyphenate name)))
                                       (first-ns-symbol source-file)])])
                 (when (and name
                            (not= group (group-for-stem name)))
                   [(validation-error :group-semantics [name group])]))))
            entries)))
         expected-paths (owned-top-level-test-paths)
         ownership-paths (vec (sort (ownership-bootstrap-free-test-paths)))
         contract-tuples (component-contract-leaf-tuples)
         actual-tuples (catalog-leaf-tuples entries)
         actual-paths (vec (sort test-paths))]
     (when (not= expected-paths actual-paths)
       (throw (ex-info "Bootstrap-free leaf catalog does not match top-level tests"
                       {:id "S0LEAF-CATALOG-SURFACE"
                        :expected expected-paths
                        :actual actual-paths
                        :errors shape-errors})))
     (when (not= ownership-paths actual-paths)
       (throw (ex-info "Bootstrap-free leaf catalog does not match SH01 ownership"
                       {:id "S0LEAF-CATALOG-OWNERSHIP"
                        :expected ownership-paths
                        :actual actual-paths})))
     (when (not= contract-tuples actual-tuples)
       (throw (ex-info "Bootstrap-free leaf catalog does not match the component contract"
                       {:id "S0LEAF-CATALOG-COMPONENT-CONTRACT"
                        :expected contract-tuples
                        :actual actual-tuples})))
     (when (seq shape-errors)
       (throw (ex-info "Bootstrap-free leaf test catalog is malformed"
                       {:id "S0LEAF-CATALOG"
                        :errors shape-errors})))
     true)))

(defn catalog-validation-report
  "Returns a compact validation report, or a fail-closed error record."
  ([] (catalog-validation-report catalog))
  ([entries]
   (try
     (validate-catalog! entries)
     {:schema catalog-schema
      :valid? true
      :count (count entries)
      :catalog-hash (catalog-hash-of entries)}
     (catch clojure.lang.ExceptionInfo exception
       {:schema catalog-schema
        :valid? false
        :error (ex-data exception)}))))

(defn bootstrap-loaded?
  []
  (boolean (find-ns 'gravity.bootstrap)))

(defn assert-bootstrap-absent!
  [phase]
  (when (bootstrap-loaded?)
    (throw (ex-info "gravity.bootstrap crossed the bootstrap-free leaf boundary"
                    {:id "S0LEAF-BOOTSTRAP-LOADED"
                     :phase phase}))))

(def ^:private usage-text
  (str
   "Usage: clojure -M:leaf-test [options]\n"
   "\n"
   "Runs each reviewed bootstrap-free top-level Stage 0 test in a fresh JVM.\n"
   "With no selector, all 39 catalog entries run in canonical order.\n"
   "\n"
   "Options (repeatable):\n"
   "  --namespace NAME  select one namespace exactly\n"
   "  --exact VALUE     select an id, namespace, source path, or test path exactly\n"
   "  --group GROUP     select foundation-reader, c2-c3, or compiler\n"
   "  --list            print selected namespaces and do not run tests\n"
   "  --fail-fast       stop after the first test failure or error\n"
   "  --help            print this help\n"
   "\n"
   "Internal child mode (used by the aggregate runner):\n"
   "  --run-one NAME    run one exact catalog namespace in a fresh JVM\n"))

(defn- usage-error
  [message data]
  (throw (ex-info message
                 (merge {:id "S0LEAF-CLI-USAGE"
                         :type ::usage-error}
                        data))))

(defn- duplicate-value
  [values]
  (->> values frequencies (keep (fn [[value n]] (when (< 1 n) value))) sort vec))

(defn parse-args
  "Parses CLI arguments without requiring a test namespace."
  [arguments]
  (loop [remaining (seq arguments)
         options {:namespace []
                  :exact []
                  :group []
                  :list? false
                  :fail-fast? false
                  :help? false}]
    (if-not remaining
      (let [duplicates
            (vec (concat
                  (map #(vector :namespace %) (duplicate-value (:namespace options)))
                  (map #(vector :exact %) (duplicate-value (:exact options)))
                  (map #(vector :group %) (duplicate-value (:group options)))))]
        (when (seq duplicates)
          (usage-error "duplicate leaf-test selector"
                       {:duplicates duplicates}))
        (when (and (:list? options) (:fail-fast? options))
          (usage-error "--list and --fail-fast cannot be combined"
                       {:arguments (vec arguments)}))
        options)
      (let [argument (first remaining)
            tail (next remaining)]
        (cond
          (= argument "--help")
          (recur tail (assoc options :help? true))

          (= argument "--list")
          (if (:list? options)
            (usage-error "duplicate --list" {:option argument})
            (recur tail (assoc options :list? true)))

          (= argument "--fail-fast")
          (if (:fail-fast? options)
            (usage-error "duplicate --fail-fast" {:option argument})
            (recur tail (assoc options :fail-fast? true)))

          (contains? #{"--namespace" "--exact" "--group"} argument)
          (if-not (seq tail)
            (usage-error (str argument " requires a non-empty value")
                         {:option argument})
            (let [value (str/trim (str (first tail)))]
              (when (str/blank? value)
                (usage-error (str argument " requires a non-empty value")
                             {:option argument}))
              (recur (next tail)
                     (update options
                             (case argument
                               "--namespace" :namespace
                               "--exact" :exact
                               "--group" :group)
                             conj value))))

          :else
          (usage-error (str "unsupported option or positional argument: " argument)
                       {:option argument
                        :arguments (vec arguments)}))))))

(def parse-cli-args parse-args)

(defn- find-exact-entry
  [value]
  (let [matches
        (filter
         (fn [entry]
           (some #(= value %)
                 [(:id entry)
                  (str (:namespace entry))
                  (:source-path entry)
                  (:test-path entry)]))
         catalog)
        matches (vec matches)]
    (cond
      (empty? matches)
      (usage-error (str "unknown exact leaf-test selector: " value)
                   {:kind :exact :selector value})

      (< 1 (count matches))
      (usage-error (str "ambiguous exact leaf-test selector: " value)
                   {:kind :exact :selector value
                    :matches (mapv :namespace matches)})

      :else
      (first matches))))

(defn- find-namespace-entry
  [value]
  (let [matches (vec (filter #(= value (str (:namespace %))) catalog))]
    (cond
      (empty? matches)
      (usage-error (str "unknown leaf-test namespace: " value)
                   {:kind :namespace :selector value})

      (< 1 (count matches))
      (usage-error (str "ambiguous leaf-test namespace: " value)
                   {:kind :namespace :selector value
                    :matches (mapv :namespace matches)})

      :else
      (first matches))))

(defn- normalize-group
  [value]
  (let [value (str/replace (str/trim value) #"^:" "")
        group (keyword value)]
    (when-not (contains? groups group)
      (usage-error (str "unknown leaf-test group: " value)
                   {:kind :group :selector value
                    :groups (vec (sort groups))}))
    group))

(defn select-entries
  "Validates options and returns selected entries in catalog order."
  [options]
  (validate-catalog!)
  (assert-bootstrap-absent! :before-require)
  (let [namespace-entries (mapv find-namespace-entry (:namespace options))
        exact-entries (mapv find-exact-entry (:exact options))
        group-values (mapv normalize-group (:group options))
        direct-entries (vec (concat namespace-entries exact-entries))
        direct-ids (mapv :id direct-entries)
        duplicate-direct (duplicate-value direct-ids)
        selected-ids (set direct-ids)
        selected
        (if (or (seq direct-entries) (seq group-values))
          (->> catalog
               (filter #(or (contains? selected-ids (:id %))
                            (contains? (set group-values) (:group %))))
               vec)
          catalog)]
    (when (seq duplicate-direct)
      (usage-error "duplicate leaf-test selection"
                   {:duplicates duplicate-direct}))
    (when (empty? selected)
      (usage-error "leaf-test selectors matched no catalog entries"
                   {:options options}))
    selected))

(defn select-catalog
  [options]
  (select-entries options))

(defn selected-namespaces
  [options]
  (mapv :namespace (select-entries options)))

(defn- test-var-records
  [namespace-symbol]
  (->> (ns-interns namespace-symbol)
       (keep (fn [[name test-var]]
               (when (:test (meta test-var))
                 test-var)))
       vec))


(defn- failures-reported?
  []
  (or (pos? (get @test/*report-counters* :fail 0))
      (pos? (get @test/*report-counters* :error 0))))

(defn- run-namespace-tests!
  [namespace-symbol fail-fast?]
  (let [namespace-object (the-ns namespace-symbol)
        original-test-var (var-get #'test/test-var)
        guarded-test-var (fn [test-var]
                           (when (or (not fail-fast?)
                                     (not (failures-reported?)))
                             (original-test-var test-var)))
        test-hook (find-var (symbol (str (ns-name namespace-object))
                                    "test-ns-hook"))]
    (test/do-report {:type :begin-test-ns :ns namespace-object})
    (try
      (binding [test/test-var guarded-test-var]
        (if test-hook
          ((var-get test-hook))
          (test/test-vars (test-var-records namespace-symbol))))
      (catch Throwable exception
        (test/do-report {:type :error
                         :message "namespace or fixture failure"
                         :actual exception
                         :ns namespace-object}))
      (finally
        (test/do-report {:type :end-test-ns :ns namespace-object})))))

(defn- test-counters
  []
  (into (sorted-map)
        (for [key [:test :pass :fail :error]]
          [key (long (get @test/*report-counters* key 0))])))

(def ^:dynamic *child-timeout-ms* 300000)
(def ^:dynamic *child-output-limit-bytes* (* 1024 1024))
(def ^:dynamic *child-stream-drain-timeout-ms* 1000)
(def ^:dynamic *descendant-observer*
  "Optional test hook called once for each descendant first seen by the sampler."
  nil)

(def ^:private child-summary-prefix "GRAVITY_LEAF_SUMMARY ")

(def ^:dynamic *child-executor*
  "Optional injectable child executor used by contract tests.

  A production run uses run-child-process!.  Tests can bind this var to a
  pure function that returns a child result without starting another JVM."
  nil)

(defn clojure-executable
  []
  (or (not-empty (System/getenv "CLOJURE_EXECUTABLE"))
      "clojure"))

(defn child-command
  "Returns the argv vector used for one fresh leaf-test JVM."
  ([entry]
   (child-command entry {}))
  ([entry {:keys [fail-fast?] :or {fail-fast? false}}]
   (vec (concat
         [(clojure-executable) "-J-Xmx256m"]
         (:jvm-options entry)
         ["-M:leaf-test" "--run-one" (str (:namespace entry))]
         (when fail-fast? ["--fail-fast"])))))

(defn- read-bounded-stream
  [^InputStream stream]
  (with-open [stream stream]
    (let [buffer (byte-array 16384)
          output (ByteArrayOutputStream.)]
      (loop [total 0
             truncated? false]
        (let [read (.read stream buffer)]
          (if (= -1 read)
            {:text (String. (.toByteArray output) StandardCharsets/UTF_8)
             :truncated? truncated?}
            (let [remaining (- *child-output-limit-bytes* total)
                  write-count (max 0 (min read remaining))]
              (when (pos? write-count)
                (.write output buffer 0 write-count))
              (recur (+ total read)
                     (or truncated? (< write-count read))))))))))

(defn- exact-counter-map?
  [counts]
  (and (map? counts)
       (= #{:test :pass :fail :error} (set (keys counts)))
       (every? #(and (integer? %) (not (neg? %))) (vals counts))))

(defn- child-summary-errors
  [value entry exit-code]
  (let [counts (:counts value)
        expected-namespace (:namespace entry)
        expected-exit (when (exact-counter-map? counts)
                        (if (and (zero? (:fail counts))
                                 (zero? (:error counts)))
                          0
                          1))]
    (vec
     (concat
      (when-not (map? value) [:not-a-map])
      (when-not (= summary-schema (:schema value)) [:schema])
      (when-not (= :non-authoritative (:authority value)) [:authority])
      (when-not (false? (:authoritative? value)) [:authoritative])
      (when-not (= (count catalog) (:catalog-count value)) [:catalog-count])
      (when-not (= catalog-hash (:catalog-hash value)) [:catalog-hash])
      (when-not (= 1 (:selected-count value)) [:selected-count])
      (when-not (= [expected-namespace] (:selected value)) [:selected])
      (when-not (= (catalog-hash-of [entry]) (:selected-hash value))
        [:selected-hash])
      (when-not (boolean? (:fail-fast? value)) [:fail-fast])
      (when-not (exact-counter-map? counts) [:counts])
      (when (exact-counter-map? counts)
        (for [key [:test :pass :fail :error]
              :when (not= (get counts key) (get value key))]
          [:top-level-counter key]))
      (when-not (and (integer? (:elapsed-ms value))
                     (not (neg? (:elapsed-ms value))))
        [:elapsed-ms])
      (when (and expected-exit (not= expected-exit exit-code))
        [:exit-consistency])))))

(defn- read-single-edn
  [text]
  (with-open [reader (PushbackReader. (StringReader. text))]
    (let [eof (Object.)
          value (edn/read {:eof eof} reader)
          trailing (edn/read {:eof eof} reader)]
      (when (or (identical? eof value) (not (identical? eof trailing)))
        (throw (ex-info "Child summary line must contain exactly one EDN value"
                        {:id "S0LEAF-CHILD-SUMMARY-EDN"})))
      value)))

(defn- parse-child-summary-result
  [stdout entry exit-code]
  (if-let [line (->> (str/split-lines (str stdout))
                     reverse
                     (some #(when (str/starts-with? % child-summary-prefix) %)))]
    (try
      (let [value (read-single-edn (subs line (count child-summary-prefix)))]
        (if-let [errors (seq (child-summary-errors value entry exit-code))]
          {:summary nil
           :parse-diagnostic :invalid-summary
           :parse-errors (vec errors)}
          {:summary value :parse-diagnostic nil}))
      (catch Throwable exception
        {:summary nil
         :parse-diagnostic :invalid-edn
         :parse-message (.getMessage exception)}))
    {:summary nil :parse-diagnostic :missing-summary-line}))

(defn parse-child-summary
  "Parses and validates the final prefixed child summary, or returns nil."
  ([stdout entry exit-code]
   (:summary (parse-child-summary-result stdout entry exit-code)))
  ([stdout]
   (when-let [line (->> (str/split-lines (str stdout))
                        reverse
                        (some #(when (str/starts-with? % child-summary-prefix) %)))]
     (try
       (let [value (read-single-edn (subs line (count child-summary-prefix)))
             namespace-symbol (when (and (vector? (:selected value))
                                         (= 1 (count (:selected value))))
                                (first (:selected value)))
             entry (some #(when (= namespace-symbol (:namespace %)) %) catalog)
             exit-code (when (exact-counter-map? (:counts value))
                         (if (and (zero? (get-in value [:counts :fail]))
                                  (zero? (get-in value [:counts :error])))
                           0
                           1))]
         (when (and entry (some? exit-code))
           (:summary (parse-child-summary-result stdout entry exit-code))))
       (catch Throwable _
         nil)))))

(defn- close-quietly!
  [value]
  (when (instance? Closeable value)
    (try
      (.close ^Closeable value)
      (catch Throwable _))))

(defn- await-handle-exit!
  [^ProcessHandle handle timeout-ms]
  (try
    (.get (.onExit handle) timeout-ms TimeUnit/MILLISECONDS)
    (catch Throwable _
      nil)))

(defn- current-descendants
  [^ProcessHandle root]
  (try
    (with-open [stream (.descendants root)]
      (vec (iterator-seq (.iterator stream))))
    (catch Throwable _
      [])))

(defn- track-descendants!
  [^ProcessHandle root observed stop?]
  (loop []
    (let [descendants (current-descendants root)
          previously-observed @observed
          newly-observed (remove previously-observed descendants)]
      (swap! observed into descendants)
      (when *descendant-observer*
        (doseq [^ProcessHandle handle newly-observed]
          (*descendant-observer* handle))))
    (when-not @stop?
      (try
        (Thread/sleep 1)
        (catch InterruptedException _))
      (recur))))

(defn- stop-process-handles!
  [handles]
  (let [handles (->> handles
                     (remove nil?)
                     (reduce (fn [by-pid ^ProcessHandle handle]
                               (assoc by-pid (.pid handle) handle)) {})
                     vals
                     vec)]
    (when (seq handles)
      (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
        (.destroy handle))
      (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
        (await-handle-exit! handle 250))
      (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
        (.destroyForcibly handle))
      (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
        (await-handle-exit! handle 1000))
      (doseq [^ProcessHandle handle handles :when (.isAlive handle)]
        (.destroyForcibly handle)
        (await-handle-exit! handle 1000))
      (when-let [survivors (seq (filter #(.isAlive ^ProcessHandle %) handles))]
        (throw (ex-info "Child process tree survived forced termination"
                        {:id "S0LEAF-PROCESS-SURVIVOR"
                         :pids (mapv #(.pid ^ProcessHandle %) survivors)}))))))

(defn- stop-observed-process-tree!
  [^Process process observed]
  (when process
    (close-quietly! (.getOutputStream process))
    (let [root (.toHandle process)]
      (swap! observed into (current-descendants root))
      (stop-process-handles! (concat @observed [root])))))

(defn- cancel-and-drain-future!
  [reader]
  (when reader
    (when-not (realized? reader)
      (future-cancel reader))
    (try
      (deref reader 250 nil)
      (catch Throwable _
        nil))))

(defn run-child-process!
  "Executes one leaf namespace in a fresh JVM with a shell-free argv vector."
  ([entry]
   (run-child-process! entry {}))
  ([entry options]
  (let [command (child-command entry options)
        started (System/nanoTime)
        process-state (atom nil)
        stdout-state (atom nil)
        stderr-state (atom nil)
        descendant-monitor-state (atom nil)
        observed-descendants (atom #{})
        stop-monitor? (atom false)
        interrupted-state (atom false)
        process-builder (doto (ProcessBuilder. ^java.util.List command)
                          (.directory (.toFile (repository-root)))
                          (.redirectErrorStream false))]
    (try
      (let [process (.start process-builder)
            _ (reset! process-state process)
            root-handle (.toHandle process)
            _ (swap! observed-descendants conj root-handle)
            descendant-monitor (future
                                 (track-descendants! root-handle
                                                     observed-descendants
                                                     stop-monitor?))
            _ (reset! descendant-monitor-state descendant-monitor)
            stdout-future (future (read-bounded-stream (.getInputStream process)))
            stderr-future (future (read-bounded-stream (.getErrorStream process)))
            _ (reset! stdout-state stdout-future)
            _ (reset! stderr-state stderr-future)
            finished? (.waitFor process *child-timeout-ms* TimeUnit/MILLISECONDS)
            timed-out? (not finished?)]
        (reset! stop-monitor? true)
        (cancel-and-drain-future! descendant-monitor)
        ;; Descendants are never allowed to outlive the child invocation, even
        ;; when the direct process exits successfully before inherited pipes
        ;; reach EOF.
        (stop-observed-process-tree! process observed-descendants)
        (let [stdout (deref stdout-future *child-stream-drain-timeout-ms*
                            {:text "" :truncated? true})
              stderr (deref stderr-future *child-stream-drain-timeout-ms*
                            {:text "" :truncated? true})
              exit-code (if timed-out? 124 (.exitValue process))
              parsed (parse-child-summary-result (:text stdout) entry exit-code)
              child-summary (:summary parsed)
              elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
              status (cond
                       timed-out? :timeout
                       (and (zero? exit-code)
                            child-summary
                            (zero? (:fail child-summary 0))
                            (zero? (:error child-summary 0)))
                       :passed
                       :else :failed)]
          {:entry entry
           :namespace (:namespace entry)
           :command command
           :status status
           :exit-code exit-code
           :summary child-summary
           :summary-parse-diagnostic (:parse-diagnostic parsed)
           :summary-parse-message (:parse-message parsed)
           :summary-parse-errors (:parse-errors parsed)
           :stdout (:text stdout)
           :stderr (:text stderr)
           :stdout-truncated? (:truncated? stdout)
           :stderr-truncated? (:truncated? stderr)
           :elapsed-ms elapsed-ms}))
      (catch InterruptedException exception
        (reset! interrupted-state true)
        {:entry entry
         :namespace (:namespace entry)
         :command command
         :status :error
         :exit-code 130
         :summary nil
         :stdout ""
         :stderr (str (.getClass exception) ": " (.getMessage exception))
         :stdout-truncated? false
         :stderr-truncated? false
         :summary-parse-diagnostic :process-interrupted
         :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))})
      (catch Throwable exception
        {:entry entry
         :namespace (:namespace entry)
         :command command
         :status :error
         :exit-code 2
         :summary nil
         :stdout ""
         :stderr (str (.getClass exception) ": " (.getMessage exception))
         :stdout-truncated? false
         :stderr-truncated? false
         :summary-parse-diagnostic :process-exception
         :elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))})
      (finally
        (reset! stop-monitor? true)
        (cancel-and-drain-future! @descendant-monitor-state)
        (when-let [process @process-state]
          (stop-observed-process-tree! process observed-descendants)
          (close-quietly! (.getInputStream ^Process process))
          (close-quietly! (.getErrorStream ^Process process))
          (close-quietly! (.getOutputStream ^Process process)))
        (doseq [reader [@stdout-state @stderr-state]]
          (cancel-and-drain-future! reader))
        (when @interrupted-state
          (.interrupt (Thread/currentThread))))))))

(def run-child! run-child-process!)

(defn- run-one-tests!
  [entry {:keys [fail-fast?] :or {fail-fast? false}}]
  (validate-catalog!)
  (when-not (some #(= entry %) catalog)
    (usage-error "--run-one namespace is not a catalog entry"
                 {:namespace (:namespace entry)}))
  (assert-bootstrap-absent! :child-before-require)
  (let [namespace-symbol (:namespace entry)
        started (System/nanoTime)]
    (require namespace-symbol)
    (assert-bootstrap-absent! :child-after-require)
    (let [counters
          (binding [test/*report-counters* (ref test/*initial-report-counters*)]
            (run-namespace-tests! namespace-symbol fail-fast?)
            (test-counters))
          elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
          summary
          (into
           (sorted-map)
           [[:schema summary-schema]
            [:authority :non-authoritative]
            [:authoritative? false]
            [:catalog-hash catalog-hash]
            [:selected-hash (catalog-hash-of [entry])]
            [:catalog-count (count catalog)]
            [:selected-count 1]
            [:selected [namespace-symbol]]
            [:fail-fast? (boolean fail-fast?)]
            [:counts counters]
            [:test (:test counters)]
            [:pass (:pass counters)]
            [:fail (:fail counters)]
            [:error (:error counters)]
            [:elapsed-ms elapsed-ms]])]
      (assert-bootstrap-absent! :child-after-run)
      summary)))

(defn- aggregate-child-results
  [entries results fail-fast? elapsed-ms]
  (let [entries (vec entries)
        results (vec results)
        result-counts (fn [result]
                        (or (get-in result [:summary :counts])
                            (sorted-map :test 0 :pass 0 :fail 0 :error 1)))
        counts
        (into (sorted-map)
              (for [key [:test :pass :fail :error]]
                [key (reduce + 0 (map #(get (result-counts %) key 0)
                                      results))]))
        failed? (or (some #(not= :passed (:status %)) results)
                    (pos? (:fail counts))
                    (pos? (:error counts)))
        executed-count (count results)]
    (into
     (sorted-map)
     [[:schema summary-schema]
      [:mode :aggregate]
      [:authority :non-authoritative]
      [:authoritative? false]
      [:catalog-hash catalog-hash]
      [:selected-hash (catalog-hash-of entries)]
      [:catalog-count (count catalog)]
      [:selected-count (count entries)]
      [:executed-count executed-count]
      [:skipped-count (- (count entries) executed-count)]
      [:selected (mapv :namespace entries)]
      [:fail-fast? (boolean fail-fast?)]
      [:counts counts]
      [:test (:test counts)]
      [:pass (:pass counts)]
      [:fail (:fail counts)]
      [:error (:error counts)]
      [:elapsed-ms (long elapsed-ms)]
      [:results
       (mapv (fn [{:keys [entry namespace status exit-code summary elapsed-ms
                          stdout stderr stdout-truncated? stderr-truncated?
                          summary-parse-diagnostic summary-parse-message
                          summary-parse-errors]}]
               (into (sorted-map)
                     [[:id (:id entry)]
                      [:namespace namespace]
                      [:status status]
                      [:exit-code exit-code]
                      [:counts (result-counts
                                {:summary summary :status status})]
                      [:stdout (or stdout "")]
                      [:stderr (or stderr "")]
                      [:stdout-truncated? (boolean stdout-truncated?)]
                      [:stderr-truncated? (boolean stderr-truncated?)]
                      [:summary-parse-diagnostic summary-parse-diagnostic]
                      [:summary-parse-message summary-parse-message]
                      [:summary-parse-errors summary-parse-errors]
                      [:elapsed-ms elapsed-ms]]))
             results)]
      [:exit-code (if failed? 1 0)]
      [:status (if failed? :failed :passed)]])))

(def aggregate-results aggregate-child-results)

(defn run-selected-tests!
  "Runs each selected namespace in canonical order and aggregates child EDN."
  ([entries]
   (run-selected-tests! entries {}))
  ([entries {:keys [fail-fast?] :or {fail-fast? false}}]
   (validate-catalog!)
   (assert-bootstrap-absent! :before-child-runs)
   (let [entries (vec entries)
         executor (or *child-executor* run-child!)
         started (System/nanoTime)
         results
         (loop [remaining entries
                results []]
           (if (empty? remaining)
             results
             (let [result (executor (first remaining)
                                    {:fail-fast? fail-fast?})
                   result (if (map? result)
                            result
                            {:entry (first remaining)
                             :namespace (:namespace (first remaining))
                             :status :error
                             :exit-code 2
                             :summary nil
                             :stdout ""
                             :stderr "child executor returned a non-map"
                             :elapsed-ms 0})
                   results (conj results result)
                   stop? (and fail-fast?
                              (not= :passed (:status result)))]
               (if stop?
                 results
                 (recur (next remaining) results)))))
         elapsed-ms (long (/ (- (System/nanoTime) started) 1000000))
         summary (aggregate-child-results entries results fail-fast? elapsed-ms)]
     (assert-bootstrap-absent! :after-child-runs)
     summary)))

(def run-selected! run-selected-tests!)

(defn- print-list!
  [entries]
  (doseq [entry entries]
    (println (:namespace entry)))
  (flush))

(defn run-one-cli!
  "Internal child mode: run exactly one catalog namespace and emit EDN."
  [arguments]
  (assert-bootstrap-absent! :child-before-parse)
  (when-not (or (= 1 (count arguments))
                (and (= 2 (count arguments))
                     (= "--fail-fast" (second arguments))))
    (usage-error "--run-one requires one namespace and optional --fail-fast"
                 {:arguments (vec arguments)}))
  (validate-catalog!)
  (let [entry (find-namespace-entry (first arguments))
        fail-fast? (= "--fail-fast" (second arguments))
        summary (run-one-tests! entry {:fail-fast? fail-fast?})]
    (println (str child-summary-prefix (pr-str summary)))
    (flush)
    (if (and (zero? (:fail summary))
             (zero? (:error summary)))
      0
      1)))

(defn run-cli!
  "Runs the CLI behavior and returns 0, 1, or 2 without System/exit."
  [arguments]
  (assert-bootstrap-absent! :before-parse)
  (let [options (parse-args arguments)]
    (if (:help? options)
      (do (print usage-text) 0)
      (let [entries (select-entries options)]
        (if (:list? options)
          (do (print-list! entries) 0)
          (let [summary (run-selected-tests!
                         entries {:fail-fast? (:fail-fast? options)})]
            (prn summary)
            (flush)
            (long (get summary :exit-code
                       (if (and (zero? (:fail summary))
                                (zero? (:error summary)))
                         0
                         1)))))))))

(defn- report-cli-error
  [^Throwable exception]
  (binding [*out* *err*]
    (println (str "bootstrap-free-leaf-test-runner: "
                  (.getMessage exception)))
    (flush)))

(defn -main
  [& arguments]
  (try
    (System/exit (if (= "--run-one" (first arguments))
                   (run-one-cli! (next arguments))
                   (run-cli! arguments)))
    (catch Throwable exception
      (report-cli-error exception)
      (System/exit 2))))
