(ns gravity.self-hosting.sh07-c17-plugin-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c17_plugin_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C17 plugin source test is not on the classpath"
        {:id "SH07-C17-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C17-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c17-relative-path
  "bootstrap/gravity/src/gravity/compiler/c17_compiler_plugin_pass_api.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 25254)
(def ^:private expected-source-revision-id
  "sha256:ccf31cd25a79eb24667bc78385d5d82ee2eeba0cd5fa5918748bb888e57b2582")
(def ^:private expected-artifact-id
  "sha256:750b8085e4bb2b13ebb9edd8516414071ad79eaabf8d18bc245ef05112740d5c")
(def ^:private expected-coverage
  {:fragment-count 17
   :root-form-count 17
   :form-count 878
   :binding-count 329
   :local-binding-count 67
   :resolution-count 27})
(def ^:private expected-core-census
  {:core-node-count 603
   :definition-count 17
   :call-count 0
   :reference-count 0
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 507
    :collection-literal 69
    :def 17
    :quote 5
    :fn 5}})
(def ^:private expected-definition-names
  '#{c17-plugin-manifest-contract
     c17-api-compatibility-contract
     c17-compiler-capability-contract
     c17-pass-contract
     c17-sandbox-contract
     c17-build-effect-contract
     c17-trust-contract
     c17-domain-registration-contract
     c17-facet-registration-contract
     c17-output-verification-contract
     c17-provenance-contract
     c17-plugin-diagnostic-catalog
     build-c17-plugin-manifest
     build-c17-pass-registration
     build-c17-sandbox-grant
     build-c17-plugin-execution-trace
     verify-c17-compiler-plugin-pass-api})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE"
   "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B25 coordinator adapter is absent"
        {:id "SH07-C17-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- sha256-id
  [bytes]
  (let [digest
        (.digest
         (java.security.MessageDigest/getInstance "SHA-256")
         bytes)]
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- core
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)
        namespace (get-in authenticated-request [:module :namespace])]
    {:fragment-count (count (:fragment-manifest authenticated-request))
     :root-form-count (count (:top-level-form-ids authenticated-request))
     :form-count (count (:forms authenticated-request))
     :binding-count (count (:binding-table authenticated-request))
     :local-binding-count
     (count
      (filter #(= namespace (:namespace %))
              (:binding-table authenticated-request)))
     :resolution-count (count (:resolution-table authenticated-request))}))

(defn- core-census
  [artifact]
  (let [core-artifact (core artifact)
        nodes (:nodes core-artifact)]
    {:core-node-count (count nodes)
     :definition-count (count (:definitions core-artifact))
     :call-count (count (:calls core-artifact))
     :reference-count (count (:reference-uses core-artifact))
     :keyword-lookup-count (count (:keyword-lookups core-artifact))
     :core-form-frequencies (frequencies (map :core-form nodes))}))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B25 records are not uniquely identifiable"
        {:id "SH07-C17-COVERAGE-AMBIGUOUS-INDEX"
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
      {:raw-host-error
       {:class (.getName (class throwable))
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

(def ^:private c17-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c17-relative-path))))

(def ^:private c17-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c17-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c17-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c17_compiler_plugin_pass_api.qst")
          left-path (path c17-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c17-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b25-proof-contract-registers-c17-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path c17-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "SH-07-B25" (:coverage-milestone contract)))
    (is (= c17-relative-path
           (get-in contract [:authoritative-modules :c17-plugin])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c17-plugin])))
    (doseq [document
            ["docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
             "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"
             "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md"
             "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"
             "docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md"
             "docs/phase-01-core-language/024-l14-language-facet-system-specification.md"
             "docs/phase-01-core-language/025-l15-capability-provider-specification.md"
             "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
             "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md"
             "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md"
             "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
             "docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md"
             "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md"
             "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]]
      (is (contains? documents document)))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c17-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c17-relative-path)))))
    (doseq [source-fact
            [":stage :stage1"
             ":seed :clojure-stage0"
             ":retirement-objective :replace-clojure-seed"
             ":verified-by :clojure-stage0"
             ":compiled-by :clojure-stage0"
             ":kind :source-model"
             ":production-execution? false"
             ":central-integration :pending"
             ":status :stage1-source-owned"
             ":scope :source-model-bridge"
             ":ambient-authority-denied true"
             ":effects #{}"
             ":capabilities #{}"
             ":ambient-authority :denied"
             ":production-plugin-loading"
             ":production-pass-execution"
             ":runtime-sandbox-implementation"
             ":central-whitelist-integration"
             ":roadmap-completion"]]
      (is (string/includes? source-text source-fact)))
    (is (contains? (set (:nonclaims contract))
                   :c17-plugin-runtime-authority))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b25-c17-source-has-exact-authentic-coverage
  (let [artifact @c17-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= expected-artifact-id (:artifact-id artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.compiler.c17-compiler-plugin-pass-api
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in authenticated-request
                   [:module :source-revision-id])
           (get-in authenticated-request
                   [:lineage :source-revision-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (false?
         (get-in artifact
                 [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b25-c17-definitions-quotes-and-lookup-boundaries-are-exact
  (let [artifact @c17-artifact
        core-artifact (core artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 5 (count (filter #(= :fn (:core-form %)) nodes))))
    (is (= 5 (count quote-nodes)))
    (is (empty? (:calls core-artifact)))
    (is (empty? (:reference-uses core-artifact)))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero?
         (count
          (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (is (every?
         #(= :def (:core-form (get node-by-id (:core-node-id %))))
         (:definitions core-artifact)))
    (testing
     (str
      "quoted representation does not prove plugin loading, pass execution, "
      "sandbox or trust enforcement, whitelist or domain integration, cache "
      "integration, filesystem or network authority, or release support")
      (doseq [node quote-nodes]
        (is (= [] (:children node)))
        (is (= :no-evaluation (get-in node [:evaluation :kind])))
        (is (= [] (get-in node [:evaluation :order])))
        (is (= #{:quoted-form-id :quoted-syntax-id
                 :quoted-kind :quoted-value}
               (set (keys (:attributes node)))))))))

(deftest sh07-b25-c17-is-deterministic-path-neutral-and-provenanced
  (let [{:keys [left right left-path right-path]} @parity-artifacts]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (coverage left) (coverage right)))
    (is (= (core-census left) (core-census right)))
    (is (= (:fragment-manifest (request left))
           (:fragment-manifest (request right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))
    (is (not= left-path right-path))))

(deftest sh07-b25-c17-replay-and-alteration-containment-pass
  (let [artifact @c17-artifact
        quote-node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= :quote (:core-form node)) index))
          (:nodes (core artifact))))
        report
        ((required-var 'sh07-core-artifact-verification) artifact)
        proof (:capability-based-proof artifact)
        request-alteration
        (assoc-in (request artifact)
                  [:module :source-revision-id] zero-id)
        request-result
        (diagnostic-result
         #((required-var 'sh07-core-run-request-for-test)
           (:sh06-resolution-artifact artifact)
           request-alteration))
        request-diagnostic (diagnostic-data request-result)]
    (is (= :passed (:status report)))
    (is (= [] (:failed-checks report)))
    (is (= :complete (:status proof)))
    (is (= [] (:failed-checks proof)))
    (is (nil? (:raw-host-error request-result)))
    (is (= :gravity/sh07-core-diagnostic
           (:artifact request-diagnostic)))
    (is (= "C6-VERIFY" (:rule request-diagnostic)))
    (doseq [[label altered expected-check]
            [["definition binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :definitions 0 :binding-id]
               zero-id)
              :canonical-core-replays?]
             ["quoted body"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :nodes quote-node-index :attributes :quoted-value]
               :altered)
              :canonical-core-replays?]
             ["actual path provenance"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :provenance :actual-source-path]
               "/altered/root/c17_compiler_plugin_pass_api.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c17-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b25-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families
          extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path
             (str
              "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
              basename "." extension))
            peer-extension (if (= "gravity" extension) "qst" "gravity")
            peer-path
            (path
             (str
              "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
              basename "." peer-extension))
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path))
               (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic
               (:artifact diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path
               (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))
