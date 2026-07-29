(ns gravity.self-hosting.sh06-resolution-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh06_resolution_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-06 test source is not on the classpath"
                      {:id "SH06-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH06-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- private-bootstrap-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required private SH-06 test seam is absent"
        {:id "SH06-PRIVATE-TEST-SEAM"
         :symbol symbol}))))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-06")

(def ^:private accepted-fixtures
  #{"resolution-order" "module-boundaries" "foreign-explicit"
    "compiler-subset"})

(def ^:private rejected-fixtures
  #{"unresolved" "ambiguous" "private" "alias" "shadow" "cycle"
    "cross-profile" "capability" "target" "foreign"})

(def ^:private c5-rules
  #{"C5-UNRESOLVED" "C5-AMBIGUOUS" "C5-PRIVATE" "C5-ALIAS"
    "C5-SHADOW" "C5-CYCLE" "C5-CROSS-PROFILE" "C5-CAPABILITY"
    "C5-TARGET" "C5-FOREIGN"})

(def ^:private authoritative-compiler-module-count 41)

(def ^:private authoritative-compiler-path-inventory-sha256
  "sha256:8369be6c68114363b52af1922a5d0d521cee04f35bb865f5fa400090c7c02bfc")

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- sha256-text
  [value]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        (.getBytes ^String value
                                   java.nio.charset.StandardCharsets/UTF_8))]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(def ^:private c2-artifacts (atom {}))

(defn- c2-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @c2-artifacts key)
        (let [artifact
              (bootstrap/compiler-c2-reader-file-artifact
               (fixture-path family basename extension))]
          (swap! c2-artifacts assoc key artifact)
          artifact))))

(defn- ns-metadata
  [artifact]
  (let [ns-form (first (:parsed-semantic-values artifact))
        clause (some #(when (and (seq? %) (= :metadata (first %))) %)
                     (drop 2 ns-form))]
    (second clause)))

(defn- rejection-request
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:compiler :sh06-request]))

(defn- rejection-oracle
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:sh06]))

(defn- contains-expected-key?
  [value]
  (cond
    (map? value)
    (or (some #(str/starts-with? (name %) "expected-") (keys value))
        (some contains-expected-key? (vals value)))

    (coll? value)
    (boolean (some contains-expected-key? value))

    :else false))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-06 coordinator adapter is absent"
                      {:id "SH06-ADAPTER-ABSENT" :symbol symbol}))))

(defn- sh06-file-artifact
  [source-path]
  ((required-var 'sh06-resolution-file-artifact) source-path))

(defn- sh06-source-artifact
  [source-path source-text]
  ((required-var 'sh06-resolution-source-artifact) source-path source-text))

(defn- verification
  [artifact]
  ((required-var 'sh06-resolution-artifact-verification) artifact))

(defn- capability-proof
  [artifact]
  ((required-var 'sh06-resolution-capability-based-proof) artifact))

(defn- internal-verification
  [artifact construction?]
  ((private-bootstrap-var 'sh06-resolution-artifact-verification*)
   artifact construction?))

(defn- construction-proof
  [artifact]
  ((private-bootstrap-var
    'sh06-resolution-capability-based-proof-for-construction)
   artifact))

(defn- sha256-id?
  [value]
  (boolean (and (string? value)
                (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- all-values
  [value]
  (tree-seq coll? seq value))

(defn- all-maps
  [value]
  (filter map? (all-values value)))

(defn- contains-value?
  [value expected]
  (boolean (some #(= expected %) (all-values value))))

(defn- resolution-orders
  [artifact]
  (->> (all-maps artifact)
       (map (fn [product]
              (or (some (fn [[key value]]
                          (when (= :resolution-order key) value))
                        product)
                  (some (fn [[key value]]
                          (when (= :resolution-kind key) value))
                        product))))
       (remove nil?)
       set))

(defn- semantic-identities
  [artifact]
  ((required-var 'sh06-resolution-artifact-identity-input) artifact))

(defn- rejection-data-for-source
  [source-path source-text]
  (try
    (sh06-source-artifact source-path source-text)
    {:unexpected-success true}
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:raw-host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- rejection-data
  [source-path]
  (try
    (sh06-file-artifact source-path)
    {:unexpected-success true}
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:raw-host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- delete-tree!
  [tree]
  (when tree
    (doseq [file (reverse (file-seq (io/file tree)))]
      (io/delete-file file true))))

(defn- copy-source!
  [source-path destination]
  (io/make-parents destination)
  (with-open [input (io/input-stream source-path)
              output (io/output-stream destination)]
    (io/copy input output)))

(defn- authoritative-compiler-paths
  []
  (let [directory (io/file (path "bootstrap/gravity/src"))]
    (->> (file-seq directory)
         (filter #(.isFile %))
         (map #(.getCanonicalPath %))
         (filter #(.endsWith ^String % ".gravity"))
         sort
         vec)))

(defn- authoritative-compiler-relative-paths
  [source-paths]
  (mapv (fn [source-path]
          (-> (.relativize @root (.toPath (io/file source-path)))
              str
              (str/replace "\\" "/")))
        source-paths))

(defn- parse-corpus-shard
  []
  (when-let [raw (not-empty (System/getenv "SH06_CORPUS_SHARD"))]
    (let [[_ index-text count-text]
          (re-matches #"([1-9][0-9]*)/([1-9][0-9]*)" raw)]
      (when-not index-text
        (throw (ex-info "SH06_CORPUS_SHARD must use the form i/n"
                        {:id "SH06-CORPUS-SHARD" :value raw})))
      (let [index (Long/parseLong index-text)
            count (Long/parseLong count-text)]
        (when (> index count)
          (throw (ex-info "SH06_CORPUS_SHARD index exceeds shard count"
                          {:id "SH06-CORPUS-SHARD" :value raw
                           :index index :count count})))
        {:index index :count count :label raw}))))

(defn- corpus-shard-paths
  [source-paths index count]
  (->> source-paths
       (map-indexed vector)
       (keep (fn [[path-index source-path]]
               (when (= (mod path-index count) (dec index))
                 source-path)))
       vec))

(defn- corpus-partition-valid?
  [source-paths shard-count]
  (let [partitions
        (mapv #(corpus-shard-paths source-paths % shard-count)
              (range 1 (inc shard-count)))
        assigned (vec (mapcat identity partitions))]
    (and (= (count source-paths) (count assigned))
         (= (set source-paths) (set assigned))
         (= (count assigned) (count (set assigned))))))

(deftest sh06-fixture-inventory-is-co-canonical-and-behavioral
  (testing "the checked-in inventory is explicit and symmetric"
    (is (= accepted-fixtures (fixture-basenames "accepted" ".gravity")))
    (is (= accepted-fixtures (fixture-basenames "accepted" ".qst")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".gravity")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".qst"))))
  (testing "every pair is byte-identical and traverses the Gravity reader"
    (doseq [[family basenames]
            [["accepted" accepted-fixtures] ["rejected" rejected-fixtures]]
            basename (sort basenames)]
      (let [gravity-path (fixture-path family basename ".gravity")
            qst-path (fixture-path family basename ".qst")
            gravity-c2 (c2-artifact family basename ".gravity")
            qst-c2 (c2-artifact family basename ".qst")]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path))
            (str family "/" basename))
        (is (= :gravity/stage0-c2-reader-document-artifact (:kind gravity-c2)))
        (is (= :gravity/stage0-c2-reader-document-artifact (:kind qst-c2)))
        (is (= (:parsed-semantic-values gravity-c2)
               (:parsed-semantic-values qst-c2))))))
  (testing "executable requests are generic and contain no oracle fields"
    (doseq [basename (sort rejected-fixtures)]
      (let [gravity-request (rejection-request basename ".gravity")
            qst-request (rejection-request basename ".qst")
            gravity-oracle (rejection-oracle basename ".gravity")]
        (is (map? gravity-request) basename)
        (is (= 1 (:request-version gravity-request)) basename)
        (is (= gravity-request qst-request) basename)
        (is (not (contains? gravity-request :scenario)) basename)
        (is (not (contains-expected-key? gravity-request)) basename)
        (is (= gravity-oracle
               (rejection-oracle basename ".qst")) basename))))
  (is (= c5-rules
         (set (map #(get-in (rejection-oracle % ".gravity")
                            [:expected-rule])
                   rejected-fixtures)))))

(deftest sh06-coordinator-adapter-api-is-explicit
  (let [source-var (required-var 'sh06-resolution-source-artifact)
        file-var (required-var 'sh06-resolution-file-artifact)
        construction-seams
        ['sh06-resolution-artifact-verification-bounded*
         'sh06-resolution-artifact-verification*
         'sh06-resolution-artifact-verification-contained
         'sh06-resolution-capability-based-proof-for-construction]]
    (is (= '([source-path source-text]) (:arglists (meta source-var))))
    (is (= '([source-path]) (:arglists (meta file-var))))
    (doseq [symbol construction-seams]
      (is (true? (:private (meta (private-bootstrap-var symbol))))
          (str symbol " must remain coordinator-private")))))

(deftest sh06-resolution-order-is-real-stable-and-c4-bound
  (let [source-path (fixture-path "accepted" "resolution-order" ".gravity")
        first-artifact (sh06-file-artifact source-path)
        second-artifact (sh06-file-artifact source-path)
        orders (resolution-orders first-artifact)]
    (is (= first-artifact second-artifact))
    (is (= :gravity/sh06-resolution-artifact (:kind first-artifact)))
    (is (= :accepted (:status first-artifact)))
    (is (sha256-id? (:artifact-id first-artifact)))
    (is (= :passed (:status (verification first-artifact))))
    (is (= :complete (:status (capability-proof first-artifact))))
    (is (every? orders [:local-lexical-binding
                        :current-namespace-binding
                        :alias-qualified-required-binding
                        :fully-qualified-namespace-binding]))
    (is (contains? orders :profile-allowed-core-binding))
    (is (contains-value? first-artifact
                         :gravity/sh05-macro-expansion-artifact))
    (is (= source-path (get-in first-artifact [:provenance :source-path])))))

(deftest sh06-accepted-fixtures-emit-required-resolution-products
  (doseq [basename (sort accepted-fixtures)
          extension [".gravity" ".qst"]]
    (let [artifact (sh06-file-artifact
                    (fixture-path "accepted" basename extension))]
      (testing (str basename extension)
        (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (sha256-id? (:artifact-id artifact)))
        (is (contains-value? artifact
                             :gravity/sh06-namespace-analysis-artifact))
        (let [analysis (:namespace-analysis artifact)]
          (doseq [product [:alias-table :binding-table :resolution-table
                           :lexical-scope-graph :dependency-graph
                           :cross-profile-edge-report
                           :incremental-invalidation-inputs]]
            (is (contains? analysis product) (name product))))
        (is (= :passed (:status (verification artifact))))
        (is (= :complete (:status (capability-proof artifact))))))))

(deftest sh06-module-foreign-and-compiler-cases-resolve-through-generic-rules
  (let [module-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "module-boundaries" ".gravity"))
        foreign-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "foreign-explicit" ".gravity"))
        compiler-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "compiler-subset" ".gravity"))
        module-analysis (:namespace-analysis module-artifact)
        foreign-analysis (:namespace-analysis foreign-artifact)
        compiler-analysis (:namespace-analysis compiler-artifact)]
    (is (seq (:dependency-records module-analysis)))
    (is (every? :accepted
                (:cross-profile-edge-report module-analysis)))
    (is (some #(= :pure-core-api (:profile-boundary %))
              (:dependency-records module-analysis)))
    (is (contains? (resolution-orders foreign-artifact)
                   :explicit-foreign-import-binding))
    (is (some #(= :foreign (:kind %)) (:alias-table foreign-analysis)))
    (is (seq (:lexical-scope-graph compiler-analysis)))
    (is (some #(= :local-lexical-binding %)
              (resolution-orders compiler-artifact)))
    (is (empty? (:diagnostics module-analysis)))
    (is (empty? (:diagnostics foreign-analysis)))
    (is (empty? (:diagnostics compiler-analysis)))))

(deftest sh06-identities-are-extension-checkout-and-cwd-neutral
  (let [gravity-path (fixture-path "accepted" "module-boundaries" ".gravity")
        qst-path (fixture-path "accepted" "module-boundaries" ".qst")
        gravity-artifact (sh06-file-artifact gravity-path)
        qst-artifact (sh06-file-artifact qst-path)
        root-a (java.nio.file.Files/createTempDirectory
                "gravity-sh06-checkout-a-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        root-b (java.nio.file.Files/createTempDirectory
                "gravity-sh06-checkout-b-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        unrelated-cwd (java.nio.file.Files/createTempDirectory
                       "gravity-sh06-cwd-"
                       (make-array java.nio.file.attribute.FileAttribute 0))
        path-a (str (.resolve root-a "src/module-boundaries.gravity"))
        path-b (str (.resolve root-b "src/module-boundaries.qst"))
        original-user-dir (System/getProperty "user.dir")]
    (try
      (copy-source! gravity-path path-a)
      (copy-source! qst-path path-b)
      (System/setProperty "user.dir" (str unrelated-cwd))
      (let [artifact-a (sh06-file-artifact path-a)
            artifact-b (sh06-file-artifact path-b)]
        (is (= (:artifact-id gravity-artifact) (:artifact-id qst-artifact)))
        (is (= (semantic-identities gravity-artifact)
               (semantic-identities qst-artifact)))
        (is (= (:artifact-id artifact-a) (:artifact-id artifact-b)))
        (is (= (semantic-identities artifact-a)
               (semantic-identities artifact-b)))
        (is (= path-a (get-in artifact-a [:provenance :source-path])))
        (is (= path-b (get-in artifact-b [:provenance :source-path])))
        (is (not= (get-in artifact-a [:provenance :source-path])
                  (get-in artifact-b [:provenance :source-path]))))
      (finally
        (System/setProperty "user.dir" original-user-dir)
        (doseq [tree [root-a root-b unrelated-cwd]]
          (delete-tree! (.toFile tree)))))))

(deftest sh06-rejections-are-structured-pathful-and-rule-exact
  (doseq [basename (sort rejected-fixtures)
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "rejected" basename extension)
          oracle (rejection-oracle basename extension)
          data (rejection-data source-path)]
      (testing (str basename extension)
        (is (nil? (:raw-host-error data)))
        (is (nil? (:unexpected-success data)))
        (is (= (:expected-rule oracle) (:id data)))
        (is (= (:expected-stage oracle) (:stage data)))
        (is (= (:expected-severity oracle) (:severity data)))
        (is (= :c5-name-resolution (:diagnostic-family data)))
        (is (= source-path (get-in data [:source-span :source])))
        (is (contains? data :symbol))
        (is (contains? data :syntax-id))
        (is (contains? data :namespace))
        (is (contains? data :profile))
        (is (contains? data :target))
        (is (contains? data :candidate-bindings))
        (is (contains? data :dependency-edge))
        (is (seq (:remediation data)))))))

(deftest sh06-dispatch-does-not-consume-scenario-labels-or-oracles
  (doseq [basename ["unresolved" "ambiguous" "cycle"]]
    (let [source-path (fixture-path "rejected" basename ".gravity")
          source-text (slurp source-path)
          expected (:expected-rule (rejection-oracle basename ".gravity"))
          with-ignored-scenario
          (str/replace-first source-text
                             "{:request-version 1"
                             "{:request-version 1 :scenario :ignored")
          with-changed-oracle
          (str/replace source-text expected "C5-FOREIGN")
          baseline (rejection-data-for-source source-path source-text)
          scenario-result
          (rejection-data-for-source source-path with-ignored-scenario)
          oracle-result
          (rejection-data-for-source source-path with-changed-oracle)]
      (is (not= source-text with-ignored-scenario) basename)
      (is (not= source-text with-changed-oracle) basename)
      (is (= expected (:id baseline)) basename)
      (is (= expected (:id scenario-result)) basename)
      (is (= expected (:id oracle-result)) basename)
      (is (nil? (:raw-host-error scenario-result)) basename)
      (is (nil? (:raw-host-error oracle-result)) basename))))

(deftest sh06-oracle-metadata-is-excluded-from-executable-request
  (let [source-path (fixture-path "accepted" "resolution-order" ".gravity")
        source-text (slurp source-path)
        with-oracle
        (str/replace-first
         source-text
         "{:slice :SH-06 :fixture :resolution-order}"
         "{:slice :SH-06 :fixture :resolution-order :sh06 {:expected-rule \"C5-FOREIGN\"}}")
        baseline (sh06-source-artifact source-path source-text)
        oracle-bearing (sh06-source-artifact source-path with-oracle)
        request (get-in oracle-bearing
                        [:gravity-resolution-boundary
                         :authenticated-resolution-request])]
    (is (not= source-text with-oracle))
    (is (not= (:artifact-id baseline) (:artifact-id oracle-bearing)))
    (is (not= (get-in baseline [:provenance :source-revision-id])
              (get-in oracle-bearing [:provenance :source-revision-id])))
    (is (= (mapv #(select-keys % [:symbol :position :resolution-order])
                 (get-in baseline [:namespace-analysis :resolution-table]))
           (mapv #(select-keys % [:symbol :position :resolution-order])
                 (get-in oracle-bearing
                         [:namespace-analysis :resolution-table]))))
    (is (map? request))
    (is (not (contains-value? request :sh06)))
    (is (not (contains-expected-key? request)))))

(deftest sh06-semantic-input-substitution-changes-resolution-behavior
  (let [ambiguous-path (fixture-path "rejected" "ambiguous" ".gravity")
        ambiguous-source (slurp ambiguous-path)
        single-candidate
        (str/replace
         ambiguous-source
         "{:name shared :visibility :public}]"
         "]")
        cycle-path (fixture-path "rejected" "cycle" ".gravity")
        cycle-source (slurp cycle-path)
        acyclic
        (str/replace
         cycle-source
         ":from self-hosting.sh06.reject.cycle-peer\n                                                   :to self-hosting.sh06.reject.cycle}"
         ":from self-hosting.sh06.reject.cycle-peer\n                                                   :to terminal.module}")
        ambiguous-result
        (rejection-data-for-source ambiguous-path single-candidate)
        cycle-result (rejection-data-for-source cycle-path acyclic)]
    (is (not= ambiguous-source single-candidate))
    (is (not= cycle-source acyclic))
    (is (not= "C5-AMBIGUOUS" (:id ambiguous-result)))
    (is (not= "C5-CYCLE" (:id cycle-result)))
    (is (nil? (:raw-host-error ambiguous-result)))
    (is (nil? (:raw-host-error cycle-result)))))

(deftest sh06-qualified-reference-misspelling-fails-closed
  (let [source-path (fixture-path "accepted" "module-boundaries" ".gravity")
        source-text (slurp source-path)
        misspelled (str/replace source-text "(io/write" "(io/wriet")
        result (rejection-data-for-source source-path misspelled)]
    (is (not= source-text misspelled))
    (is (= "C5-UNRESOLVED" (:id result)))
    (is (= 'io/wriet (:symbol result)))
    (is (= [] (:candidate-bindings result)))
    (is (nil? (:raw-host-error result)))
    (is (nil? (:unexpected-success result)))))

(deftest sh06-same-source-qualified-private-definitions-resolve-exactly
  (let [module-name 'self-hosting.sh06.same-source-qualified
        effective-namespace 'helper.private
        private-symbol 'helper.private/private-member
        missing-member-symbol 'helper.private/missing-member
        absent-qualifier-symbol 'missing.qualifier/private-member
        unqualified-symbol 'private-member
        source-for-reference
        (fn [reference]
          (str
           "(ns self-hosting.sh06.same-source-qualified\n"
           "  (:profile :hosted) (:target :jvm)\n"
           "  (:exports [read-private])\n"
           "  (:effects #{}) (:capabilities #{}) (:safety :safe)\n"
           "  (:metadata {:slice :SH-06}))\n"
           "(def helper.private/private-member 41)\n"
           "(defn read-private []\n"
           "  " reference ")\n"))
        accepted-source
        (source-for-reference private-symbol)
        source-paths
        {".gravity" "synthetic/sh06-same-source-qualified.gravity"
         ".qst" "synthetic/sh06-same-source-qualified.qst"}
        artifacts
        (into {}
              (map (fn [[extension source-path]]
                     [extension
                      (sh06-source-artifact source-path accepted-source)]))
              source-paths)
        repeated
        (sh06-source-artifact (get source-paths ".gravity")
                              accepted-source)
        gravity-artifact (get artifacts ".gravity")
        qst-artifact (get artifacts ".qst")
        resolution
        (some #(when (= private-symbol (:symbol %)) %)
              (get-in gravity-artifact
                      [:namespace-analysis :resolution-table]))
        binding
        (some #(when (= (:binding-id resolution) (:binding-id %)) %)
              (get-in gravity-artifact
                      [:namespace-analysis :binding-table]))
        missing-member-source (source-for-reference missing-member-symbol)
        absent-qualifier-source (source-for-reference absent-qualifier-symbol)
        unqualified-source (source-for-reference unqualified-symbol)]
    (testing "a genuinely qualified private declaration resolves by its effective namespace"
      (doseq [[extension artifact] artifacts]
        (is (= :gravity/sh06-resolution-artifact (:kind artifact)) extension)
        (is (= :accepted (:status artifact)) extension)
        (is (= :passed (:status (verification artifact))) extension)
        (is (= :complete (:status (capability-proof artifact))) extension))
      (is (= :fully-qualified-namespace-binding
             (:resolution-order resolution)))
      (is (= private-symbol (:symbol resolution)))
      (is (not= module-name effective-namespace))
      (is (= effective-namespace (:namespace binding)))
      (is (= :private (:visibility binding)))
      (is (= :namespace (:binding-class binding)))
      (is (sha256-id? (:binding-id resolution)))
      (is (= (:artifact-id (:sh05-macro-artifact gravity-artifact))
             (:definition-artifact-id binding)))
      (is (= (get source-paths ".gravity")
             (get-in gravity-artifact
                     [:gravity-resolution-boundary
                      :authenticated-resolution-request
                      :provenance :actual-source-path])))
      (is (contains?
           (set (map :syntax/id
                     (get-in gravity-artifact
                             [:sh05-macro-artifact
                              :expanded-syntax-stream])))
           (:definition-syntax-id binding))))
    (testing "artifact identity is deterministic and extension-neutral"
      (is (= (:artifact-id gravity-artifact)
             (:artifact-id qst-artifact)
             (:artifact-id repeated)))
      (is (= (semantic-identities gravity-artifact)
             (semantic-identities qst-artifact)
             (semantic-identities repeated))))
    (testing "a known same-source qualifier with an absent member is unresolved"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path missing-member-source)]
          (is (= "C5-UNRESOLVED" (:id result)) extension)
          (is (= missing-member-symbol (:symbol result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))
    (testing "the qualified declaration does not leak into unqualified lookup"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path unqualified-source)]
          (is (= "C5-UNRESOLVED" (:id result)) extension)
          (is (= unqualified-symbol (:symbol result)) extension)
          (is (= [] (:candidate-bindings result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))
    (testing "an absent qualifier is an alias error"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path absent-qualifier-source)]
          (is (= "C5-ALIAS" (:id result)) extension)
          (is (= absent-qualifier-symbol (:symbol result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))))

(deftest sh06-let-initializers-use-the-sequential-outer-environment
  (let [source-path (fixture-path "accepted" "compiler-subset" ".gravity")
        source-text (slurp source-path)
        accepted-source
        (str source-text
             "\n(def prior-value 7)\n"
             "(defn sequential-let []\n"
             "  (let [prior-value prior-value\n"
             "        later-value prior-value]\n"
             "    later-value))\n")
        accepted (sh06-source-artifact source-path accepted-source)
        prior-resolutions
        (->> (get-in accepted [:namespace-analysis :resolution-table])
             (filter #(= 'prior-value (:symbol %)))
             (mapv :resolution-order))
        forward-source
        (str source-text
             "\n(defn forward-let []\n"
             "  (let [first-value missing-later\n"
             "        missing-later 1]\n"
             "    first-value))\n")
        forward-result
        (rejection-data-for-source source-path forward-source)]
    (is (= :accepted (:status accepted)))
    (is (= [:current-namespace-binding :local-lexical-binding]
           prior-resolutions))
    (is (= :passed (:status (verification accepted))))
    (is (= "C5-UNRESOLVED" (:id forward-result)))
    (is (= 'missing-later (:symbol forward-result)))
    (is (= [] (:candidate-bindings forward-result)))
    (is (nil? (:raw-host-error forward-result)))
    (is (nil? (:unexpected-success forward-result)))))

(deftest sh06-resolution-products-survive-serialization-roundtrip
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        serialized ((required-var 'sh06-resolution-serialize) artifact)
        restored ((required-var 'sh06-resolution-deserialize) serialized)]
    (is (or (string? serialized)
            (instance? (Class/forName "[B") serialized)))
    (is (= artifact restored))
    (is (= (:artifact-id artifact) (:artifact-id restored)))
    (is (= (semantic-identities artifact) (semantic-identities restored)))
    (is (= :passed (:status (verification restored))))
    (is (= :complete (:status (capability-proof restored))))))

(deftest sh06-serialization-rejects-malformed-or-altered-payloads
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        serialized ((required-var 'sh06-resolution-serialize) artifact)
        serialized-text (if (string? serialized)
                          serialized
                          (String. ^bytes serialized
                                   java.nio.charset.StandardCharsets/UTF_8))
        altered (str/replace-first serialized-text
                                   (:artifact-id artifact)
                                   "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
        malformed "{:artifact :gravity/sh06-resolution-artifact"]
    (doseq [[label payload] [[:altered altered] [:malformed malformed]]]
      (let [result
            (try
              ((required-var 'sh06-resolution-deserialize) payload)
              {:unexpected-success true}
              (catch clojure.lang.ExceptionInfo error
                (ex-data error))
              (catch Throwable error
                {:raw-host-error (.getName (class error))}))]
        (is (nil? (:unexpected-success result)) (name label))
        (is (nil? (:raw-host-error result)) (name label))
        (is (string? (:id result)) (name label))
        (is (seq (:remediation result)) (name label))))))

(deftest sh06-authoritative-products-and-lineage-alterations-fail-closed
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        changes
        [[:binding-table (assoc-in artifact [:binding-table :status]
                                   :substituted)]
         [:dependency-graph (assoc-in artifact [:dependency-graph :edges]
                                      [{:from 'missing :to 'also-missing}])]
         [:namespace-analysis
          (assoc-in artifact [:namespace-analysis :namespace]
                    'substituted.namespace)]
         [:c4-lineage
          (assoc-in artifact [:sh05-macro-artifact :artifact-id]
                    "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
         [:artifact-id
          (assoc artifact :artifact-id
                 "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")]]]
    (doseq [[label changed] changes]
      (let [report (verification changed)
            proof (capability-proof changed)]
        (is (= :failed (:status report)) (name label))
        (is (seq (:failed-checks report)) (name label))
        (is (= :failed (:status proof)) (name label))))))

(deftest sh06-authenticated-boundary-mutations-invalidate-verification
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        boundary-path [:gravity-resolution-boundary]
        changed-digest
        "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
        mutations
        [[:document-set :document-set-current?
          #(assoc % :document-set ["L3" "C6"])]
         [:governing-document :governing-document-current?
          #(assoc % :governing-document
                  "docs/substituted-governing-document.md")]
         [:provenance-extra-key :provenance-current?
          #(assoc-in % [:provenance :unexpected] true)]
         [:request-definitions :authenticated-resolution-request-current?
          #(update-in %
                      (into boundary-path
                            [:authenticated-resolution-request
                             :definitions 0 :visibility])
                      (fn [visibility]
                        (if (= :private visibility) :public :private)))]
         [:plan-binding :plan-binding-current?
          #(update-in %
                      (into boundary-path [:plan-binding
                                           :source-byte-count])
                      inc)]
         [:raw-template :raw-template-result-current?
          #(assoc-in %
                     (into boundary-path [:raw-template-result :status])
                     :substituted)]
         [:raw-analysis :raw-analysis-current?
          #(assoc-in %
                     (into boundary-path [:raw-analysis :namespace])
                     'substituted.raw)]
         [:resolved-analysis :resolved-analysis-current?
          #(assoc-in %
                     (into boundary-path [:resolved-analysis :namespace])
                     'substituted.resolved)]
         [:digest-requests :digest-requests-current?
          #(assoc-in %
                     (into boundary-path [:digest-requests 0 :purpose])
                     :substituted-digest-purpose)]
         [:resolved-digests :resolved-digests-current?
          #(assoc-in %
                     (into boundary-path [:resolved-digests 0])
                     changed-digest)]
         [:template-verification :template-verification-current?
          #(assoc-in %
                     (into boundary-path [:template-verification :status])
                     :failed)]
         [:resolved-verification :resolved-verification-current?
          #(assoc-in %
                     (into boundary-path [:resolved-verification :status])
                     :failed)]]
        serialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-serialize) candidate)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))]
    (is (= :passed (:status (verification artifact))))
    (doseq [[label expected-check alter] mutations]
      (testing (name label)
        (let [changed (alter artifact)
              report (verification changed)
              serialization (serialize-result changed)]
          (is (not= artifact changed))
          (is (= :failed (:status report)))
          (is (contains? (set (:failed-checks report)) expected-check))
          (is (true? (get-in report
                             [:checks :fresh-gravity-request-replay?])))
          (is (= "C5-UNRESOLVED" (:id serialization)))
          (is (nil? (:unexpected-success serialization)))
          (is (nil? (:raw-host-error serialization))))))))

(deftest sh06-host-resource-failures-are-structured-at-public-seams
  (let [structured-result
        (fn [operation]
          (try
            (operation)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        verification-report
        (with-redefs-fn
          {(private-bootstrap-var
            'sh06-resolution-artifact-verification*)
           (fn [_ _]
             (throw (OutOfMemoryError. "verification resource seam")))}
          #(verification {:bounded :artifact}))
        serialization-result
        (structured-result
         #(with-redefs
           [bootstrap/sh06-resolution-artifact-verification
            (fn [_] {:status :passed})
            bootstrap/sh06-resolution-bounded-pr-str
            (fn [_ _]
              (throw (OutOfMemoryError. "serialization resource seam")))]
            ((required-var 'sh06-resolution-serialize)
             {:provenance {:source-path "<sh06-resource-serialize>"}})))
        deserialization-result
        (structured-result
         #(with-redefs
           [clojure.edn/read-string
            (fn [& _]
              (throw (OutOfMemoryError. "deserialization resource seam")))]
            ((required-var 'sh06-resolution-deserialize) "{}")))]
    (testing "public verification contains host resource exhaustion"
      (is (= :failed (:status verification-report)))
      (is (= [:contained-host-resource-failure?]
             (:failed-checks verification-report)))
      (is (false?
           (get-in verification-report
                   [:checks :contained-host-resource-failure?])))
      (is (= "java.lang.OutOfMemoryError"
             (:contained-host-error verification-report))))
    (testing "serialization contains printer resource exhaustion"
      (is (= "C5-UNRESOLVED" (:id serialization-result)))
      (is (= [:contained-resolution-serialization-resource]
             (:missing-fields serialization-result)))
      (is (= "java.lang.OutOfMemoryError"
             (get-in serialization-result
                     [:observed :contained-host-error])))
      (is (nil? (:unexpected-success serialization-result)))
      (is (nil? (:raw-host-error serialization-result))))
    (testing "deserialization contains parser resource exhaustion"
      (is (= "C5-UNRESOLVED" (:id deserialization-result)))
      (is (= [:canonical-resolution-serialization]
             (:missing-fields deserialization-result)))
      (is (= "java.lang.OutOfMemoryError"
             (get-in deserialization-result
                     [:facts :contained-host-error])))
      (is (nil? (:unexpected-success deserialization-result)))
      (is (nil? (:raw-host-error deserialization-result))))))

(deftest sh06-hostile-artifact-carriers-fail-with-structured-containment
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        nested-value
        (loop [depth 96 value :bounded-leaf]
          (if (zero? depth)
            value
            (recur (dec depth) [value])))
        cases
        [[:bounded-scalar :top-level-artifact-map? :bounded-scalar]
         [:over-depth :bounded-artifact-carrier?
          (assoc artifact :diagnostics nested-value)]
         [:over-width :bounded-artifact-carrier?
          (assoc artifact :diagnostics (vec (repeat 65537 :wide)))]]
        verification-result
        (fn [candidate]
          (try
            {:report (verification candidate)}
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        serialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-serialize) candidate)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        deserialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-deserialize) (pr-str candidate))
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))]
    (doseq [[label expected-check candidate] cases]
      (testing (name label)
        (let [verified (verification-result candidate)
              report (:report verified)
              serialization (serialize-result candidate)
              deserialization (deserialize-result candidate)]
          (is (nil? (:raw-host-error verified)))
          (is (= :failed (:status report)))
          (is (contains? (set (:failed-checks report))
                         expected-check))
          (doseq [result [serialization deserialization]]
            (is (= "C5-UNRESOLVED" (:id result)))
            (is (nil? (:unexpected-success result)))
            (is (nil? (:raw-host-error result)))))))))

(deftest sh06-authenticated-resolution-is-the-c6-consumer-boundary
  (let [source-path (fixture-path "accepted" "compiler-subset" ".gravity")
        artifact (sh06-file-artifact source-path)
        source-read-attempts (atom 0)
        original-read-source-form-records bootstrap/read-source-form-records
        audited-source-reader
        (fn [requested-path requested-text]
          (if (= source-path requested-path)
            (do
              (swap! source-read-attempts inc)
              (throw (ex-info
                      "C6 reread the user module after receiving SH-06 artifact"
                      {:id "SH06-C6-SOURCE-REREAD"})))
            (original-read-source-form-records requested-path
                                               requested-text)))
        lower-with
        (fn [resolution-artifact]
          (with-redefs [bootstrap/read-source-form-records
                        audited-source-reader]
            ((required-var 'sh06-c6-lowering-from-resolution-artifact)
             resolution-artifact)))
        lowered (lower-with artifact)
        altered (assoc artifact :artifact-id
                       "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
        rejection
        (try
          (lower-with altered)
          {:unexpected-success true}
          (catch clojure.lang.ExceptionInfo error
            (ex-data error))
          (catch Throwable error
            {:raw-host-error (.getName (class error))}))]
    (is (= :gravity/stage0-c6-core-lowering-artifact (:kind lowered)))
    (is (= :gravity/sh06-resolution-artifact
           (get-in lowered [:c5-name-resolution-artifact :kind])))
    (is (= (:artifact-id artifact)
           (get-in lowered [:c5-name-resolution-artifact :artifact-id])))
    (is (= :complete (get-in lowered [:surface-to-core-map :status])))
    (is (= :passed (get-in lowered [:core-verifier-report :status])))
    (is (pos? (get-in lowered [:core-ast-module :node-count])))
    (is (= "C6-VERIFY" (:id rejection)))
    (is (nil? (:unexpected-success rejection)))
    (is (nil? (:raw-host-error rejection)))
    (is (zero? @source-read-attempts))))

(deftest sh06-transport-width-bound-is-inclusive-and-contained
  (let [bounds {:maximum-carrier-nodes 33554432
                :maximum-carrier-depth 64
                :maximum-container-width 131072}
        accepted
        (bootstrap/sh06-resolution-require-carrier!
         "<sh06-transport-width-accepted>"
         :transport-width-boundary
         (vec (range 131072)))
        rejected
        (try
          (bootstrap/sh06-resolution-require-carrier!
           "<sh06-transport-width-rejected>"
           :transport-width-boundary
           (vec (range 131073)))
          {:unexpected-success true}
          (catch clojure.lang.ExceptionInfo error
            (ex-data error))
          (catch Throwable error
            {:raw-host-error (.getName (class error))}))]
    (is (= :passed (:status accepted)))
    (is (= "C5-UNRESOLVED" (:id rejected)))
    (is (= :error (:severity rejected)))
    (is (= [:bounded-sh06-resolution-carrier]
           (:missing-fields rejected)))
    (is (= bounds (get-in rejected [:facts :transport-bounds])))
    (is (= 131072 (get-in rejected [:observed :maximum-width])))
    (is (= 2048 bootstrap/p15-s23-c6c10-max-digest-requests))
    (is (= 8192
           (bootstrap/with-sh06-resolution-transport-bounds
            bootstrap/p15-s23-c6c10-max-digest-requests)))
    (is (nil? (:unexpected-success rejected)))
    (is (nil? (:raw-host-error rejected)))))

(deftest sh06-b1-authoritative-module-uses-declared-transport-bounds
  (let [source-path
        (path
         "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity")
        artifact (sh06-file-artifact source-path)
        component-bounds {:maximum-carrier-nodes 33554432
                          :maximum-carrier-depth 64
                          :maximum-container-width 131072}
        aggregate-bounds {:maximum-carrier-nodes 67108864
                          :maximum-carrier-depth 64
                          :maximum-container-width 131072
                          :maximum-serialized-bytes 1073741824}]
    (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
    (is (= :accepted (:status artifact)))
    (is (sha256-id? (:artifact-id artifact)))
    (is (= component-bounds
           (get-in artifact
                   [:execution-boundary :component-transport-bounds])))
    (is (= aggregate-bounds
           (get-in artifact
                   [:execution-boundary :aggregate-artifact-bounds])))
    (is (= :passed (:status (verification artifact))))
    (is (= :complete (:status (capability-proof artifact))))))

(deftest sh06-component-and-aggregate-node-bounds-are-distinct
  (let [declared-component-bounds {:maximum-carrier-nodes 33554432
                                    :maximum-carrier-depth 64
                                    :maximum-container-width 131072}
        declared-aggregate-bounds {:maximum-carrier-nodes 67108864
                                   :maximum-carrier-depth 64
                                   :maximum-container-width 131072
                                   :maximum-serialized-bytes 1073741824}
        component-bounds {:maximum-carrier-nodes 2048
                          :maximum-carrier-depth 64
                          :maximum-container-width 1024}
        aggregate-bounds {:maximum-carrier-nodes 4096
                          :maximum-carrier-depth 64
                          :maximum-container-width 1024
                          :maximum-serialized-bytes 65536}
        shared-vector-carrier
        (fn [node-count]
          (let [maximum-width (:maximum-container-width component-bounds)
                full-child (vec (repeat maximum-width :bounded-node))
                full-child-nodes (inc maximum-width)
                remaining (dec node-count)
                full-count (quot remaining full-child-nodes)
                tail-count (mod remaining full-child-nodes)
                tail
                (cond
                  (zero? tail-count) []
                  (= 1 tail-count) [:bounded-node]
                  :else [(vec (repeat (dec tail-count) :bounded-node))])]
            (into (vec (repeat full-count full-child)) tail)))
        source-path
        (fixture-path "accepted" "resolution-order" ".gravity")
        artifact (sh06-file-artifact source-path)
        component-limit (:maximum-carrier-nodes component-bounds)
        aggregate-limit (:maximum-carrier-nodes aggregate-bounds)
        over-component
        (shared-vector-carrier (inc component-limit))
        under-component
        (shared-vector-carrier (dec component-limit))
        component-over-artifact
        (assoc artifact :binding-table over-component)
        aggregate-over-carrier
        {:binding-table under-component
         :alias-table under-component}
        verify-with-small-bounds
        (fn [candidate]
          (with-redefs
           [bootstrap/sh06-resolution-transport-bounds component-bounds]
            (verification candidate)))
        component-over-report
        (verify-with-small-bounds component-over-artifact)
        component-over-validation
        (get-in component-over-report
                [:component-validations :binding-table])
        aggregate-validation
        (bootstrap/sh06-resolution-carrier-validation
         aggregate-over-carrier aggregate-bounds)
        under-component-validation
        (bootstrap/sh06-resolution-carrier-validation
         under-component component-bounds)]
    (testing "an authentic reader artifact declares both exact bound classes"
      (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
      (is (= :accepted (:status artifact)))
      (is (= :complete
             (get-in artifact [:capability-based-proof :status])))
      (is (= [] (get-in artifact
                        [:capability-based-proof :failed-checks])))
      (is (= declared-component-bounds
             (get-in artifact
                     [:execution-boundary :component-transport-bounds])))
      (is (= declared-aggregate-bounds
             (get-in artifact
                     [:execution-boundary :aggregate-artifact-bounds])))
      (is (< component-limit aggregate-limit))
      (is (= (:maximum-carrier-depth component-bounds)
             (:maximum-carrier-depth aggregate-bounds)))
      (is (= (:maximum-container-width component-bounds)
             (:maximum-container-width aggregate-bounds))))
    (testing "one over-limit semantic component fails below the aggregate cap"
      (is (= :passed
              (:status
              (bootstrap/sh06-resolution-carrier-validation
               component-over-artifact declared-aggregate-bounds))))
      (is (= :failed (:status component-over-report)))
      (is (true? (get-in component-over-report
                         [:checks :bounded-artifact-carrier?])))
      (is (false? (get-in component-over-report
                          [:checks :bounded-semantic-components?])))
      (is (contains? (set (:failed-checks component-over-report))
                     :bounded-semantic-components?))
      (is (= :rejected (:status component-over-validation)))
      (is (= :maximum-carrier-nodes (:reason component-over-validation)))
      (is (= component-limit (:maximum-nodes component-over-validation)))
      (is (= (inc component-limit)
             (:observed-nodes component-over-validation))))
    (testing "two valid components can exceed a distinct aggregate cap"
      (is (= :passed (:status under-component-validation)))
      (is (= (dec component-limit)
             (:observed-nodes under-component-validation)))
      (is (= :rejected (:status aggregate-validation)))
      (is (= :maximum-carrier-nodes (:reason aggregate-validation)))
      (is (= aggregate-limit (:maximum-nodes aggregate-validation)))
      (is (= (inc aggregate-limit)
             (:observed-nodes aggregate-validation))))))

(deftest sh06-aggregate-node-rejection-reports-bounded-measurement
  (let [normal-bounds {:maximum-carrier-nodes 10
                       :maximum-carrier-depth 64
                       :maximum-container-width 64
                       :maximum-serialized-bytes 1024}
        measurement-bounds {:maximum-carrier-nodes 100
                            :maximum-carrier-depth 64
                            :maximum-container-width 64}
        candidate {:payload (vec (range 10))}
        under-small-bounds
        (fn [operation]
          (with-redefs
           [bootstrap/sh06-resolution-artifact-bounds normal-bounds
            bootstrap/sh06-resolution-diagnostic-measurement-bounds
            measurement-bounds]
            (operation)))
        public-report
        (under-small-bounds
         #(internal-verification candidate false))
        construction-report
        (under-small-bounds
         #(internal-verification candidate true))
        proof
        (under-small-bounds
         #(construction-proof candidate))
        rejection
        (under-small-bounds
         #(try
            ((private-bootstrap-var
              'sh06-resolution-finalize-candidate)
             "<sh06-measurement-propagation>" candidate)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch Throwable error
              {:raw-host-error (.getName (class error))})))
        observations (:preflight-observations construction-report)]
    (is (= :failed (:status public-report)))
    (is (= :maximum-carrier-nodes
           (get-in public-report [:carrier-validation :reason])))
    (is (nil? (:preflight-observations public-report)))
    (is (= :failed (:status construction-report)))
    (is (= 11
           (get-in construction-report
                   [:carrier-validation :observed-nodes])))
    (is (= normal-bounds (:normal-aggregate-bounds observations)))
    (is (= measurement-bounds
           (:diagnostic-measurement-bounds observations)))
    (is (= :passed (get-in observations [:aggregate :status])))
    (is (= 13 (get-in observations [:aggregate :observed-nodes])))
    (is (true? (:measurement-only? observations)))
    (is (false? (:authorizes-bound-change? observations)))
    (is (= :failed (:status proof)))
    (is (= observations (:preflight-observations proof)))
    (is (= (:carrier-validation construction-report)
           (:carrier-validation proof)))
    (is (= "C5-UNRESOLVED" (:id rejection)))
    (is (= [:final-authenticated-resolution-artifact]
           (:missing-fields rejection)))
    (is (= (:failed-checks proof)
           (get-in rejection [:observed :failed-checks])))
    (is (= observations
           (get-in rejection
                   [:observed :preflight-observations])))
    (is (= (:carrier-validation proof)
           (get-in rejection
                   [:observed :carrier-validation])))
    (is (nil? (:unexpected-success rejection)))
    (is (nil? (:raw-host-error rejection)))))

(deftest sh06-serialization-byte-bound-is-exact-and-contained
  (let [aggregate-bounds {:maximum-carrier-nodes 67108864
                          :maximum-carrier-depth 64
                          :maximum-container-width 131072
                          :maximum-serialized-bytes 1073741824}
        unicode-value "A\u03bb\u4e2d\ud83d\ude00"
        expected-unicode-bytes 10
        with-maximum
        (fn [maximum operation]
          (with-redefs
           [bootstrap/sh06-resolution-artifact-bounds
            (assoc aggregate-bounds :maximum-serialized-bytes maximum)]
            (operation)))
        structured-result
        (fn [operation]
          (try
            (operation)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        printed
        (with-maximum
         12
         #((required-var 'sh06-resolution-bounded-pr-str)
           "<sh06-unicode-byte-count>" unicode-value))
        print-over-limit
        (structured-result
         #(with-maximum
            11
            (fn []
              ((required-var 'sh06-resolution-bounded-pr-str)
               "<sh06-print-byte-limit>" unicode-value))))
        invalid-over-limit "[[[[[["
        string-over-limit
        (structured-result
         #(with-maximum
            5
            (fn []
              ((required-var 'sh06-resolution-deserialize)
               invalid-over-limit))))
        bytes-over-limit
        (structured-result
         #(with-maximum
            5
            (fn []
              ((required-var 'sh06-resolution-deserialize)
               (.getBytes
                ^String invalid-over-limit
                java.nio.charset.StandardCharsets/UTF_8)))))]
    (testing "UTF-8 counting distinguishes BMP and surrogate pairs exactly"
      (is (= expected-unicode-bytes
             ((required-var 'sh06-resolution-utf8-byte-count-up-to)
              unicode-value 100)))
      (is (= expected-unicode-bytes
             (alength
              (.getBytes
               ^String unicode-value
               java.nio.charset.StandardCharsets/UTF_8))))
      (is (= (str "\"" unicode-value "\"") printed))
      (is (= 12
             (alength
              (.getBytes
               ^String printed
               java.nio.charset.StandardCharsets/UTF_8)))))
    (testing "the bounded printer rejects the first byte over its ceiling"
      (is (= "C5-UNRESOLVED" (:id print-over-limit)))
      (is (= [:maximum-resolution-serialization-bytes]
             (:missing-fields print-over-limit)))
      (is (= 12
             (get-in print-over-limit
                     [:observed :observed-serialized-bytes])))
      (is (= 11
             (get-in print-over-limit
                     [:observed :maximum-serialized-bytes])))
      (is (nil? (:unexpected-success print-over-limit)))
      (is (nil? (:raw-host-error print-over-limit))))
    (testing "String and byte-array inputs are bounded before EDN parsing"
      (doseq [[label result] [[:string string-over-limit]
                              [:bytes bytes-over-limit]]]
        (is (= "C5-UNRESOLVED" (:id result)) (name label))
        (is (= [:bounded-resolution-serialization-bytes]
               (:missing-fields result))
            (name label))
        (is (= 6
               (get-in result [:observed :observed-serialized-bytes]))
            (name label))
        (is (= 5
               (get-in result [:observed :maximum-serialized-bytes]))
            (name label))
        (is (nil? (:unexpected-success result)) (name label))
        (is (nil? (:raw-host-error result)) (name label))))))

(deftest sh06-corpus-shards-cover-the-sorted-inventory-exactly-once
  (let [source-paths (authoritative-compiler-paths)
        relative-paths (authoritative-compiler-relative-paths source-paths)]
    (is (= source-paths (vec (sort source-paths))))
    (is (= authoritative-compiler-module-count (count source-paths)))
    (is (= authoritative-compiler-path-inventory-sha256
           (sha256-text (str/join "\n" relative-paths))))
    (doseq [shard-count [1 2 3 4 5 39 40 41]]
      (is (corpus-partition-valid? source-paths shard-count)
          (str shard-count " shards")))))

(deftest sh06-resolves-the-complete-authoritative-compiler-source-set
  (if (= "1" (System/getenv "SH06_SKIP_CORPUS"))
    (do
      (println (pr-str {:sh06-corpus :skipped}))
      (is true "SH-06 corpus test explicitly skipped"))
    (let [all-source-paths (authoritative-compiler-paths)
          shard (parse-corpus-shard)
          source-paths (if shard
                         (corpus-shard-paths all-source-paths
                                             (:index shard) (:count shard))
                         all-source-paths)
          results
          (mapv
           (fn [source-path]
             (let [artifact (sh06-file-artifact source-path)]
               {:path source-path
                :kind (:kind artifact)
                :status (:status artifact)
                :artifact-id (:artifact-id artifact)
                :proof-artifact
                (get-in artifact
                        [:capability-based-proof :artifact])
                :proof-status
                (get-in artifact
                        [:capability-based-proof :status])
                :proof-failed-checks
                (get-in artifact
                        [:capability-based-proof :failed-checks])}))
           source-paths)]
      (when-not shard
        (is (pos? (count source-paths))))
      (is (every? #(= :gravity/sh06-resolution-artifact (:kind %)) results))
      (is (every? #(= :accepted (:status %)) results))
      (is (every? #(sha256-id? (:artifact-id %)) results))
      (is (every? #(= :gravity/sh06-resolution-capability-proof
                       (:proof-artifact %))
                  results))
      (is (every? #(= :complete (:proof-status %)) results))
      (is (every? #(= [] (:proof-failed-checks %)) results))
      (when shard
        (println (pr-str {:sh06-corpus-shard (:label shard)
                          :module-count (count source-paths)}))))))
