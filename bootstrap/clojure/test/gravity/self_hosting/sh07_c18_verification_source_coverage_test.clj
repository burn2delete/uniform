(ns gravity.self-hosting.sh07-c18-verification-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c18_verification_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C18 verification source test is not on the classpath"
        {:id "SH07-C18-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C18-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c18-relative-path
  "bootstrap/gravity/src/gravity/compiler/c18_compiler_verification_pass_correctness.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 30982)
(def ^:private expected-source-revision-id
  "sha256:52529a1a77290252567b1a7e0c7c87aede36d0e94e7ef8f2939151f1e183f4c0")
(def ^:private expected-coverage
  {:fragment-count 22
   :root-form-count 22
   :form-count 1148
   :binding-count 394
   :local-binding-count 132
   :resolution-count 42})
(def ^:private expected-core-census
  {:core-node-count 640
   :definition-count 22
   :call-count 0
   :reference-count 0
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 522
    :collection-literal 76
    :def 22
    :quote 10
    :fn 10}})
(def ^:private expected-definition-names
  '#{c18-compiler-verification-contract
     c18-pass-risk-contract
     c18-pass-proof-obligation-contract
     c18-translation-validation-contract
     c18-replay-contract
     c18-evidence-record-contract
     c18-counterexample-contract
     c18-trust-report-contract
     c18-release-gate-contract
     c18-plugin-verification-contract
     c18-backend-conformance-contract
     c18-diagnostic-catalog
     build-c18-pass-risk
     build-c18-proof-obligation
     build-c18-translation-validation
     build-c18-evidence-record
     build-c18-counterexample
     build-c18-trust-report
     build-c18-release-gate
     build-c18-plugin-report
     build-c18-backend-conformance-report
     verify-c18-compiler-verification})
(def ^:private expected-export-names
  '[c18-compiler-verification-contract
    c18-pass-risk-contract
    c18-pass-proof-obligation-contract
    c18-translation-validation-contract
    c18-replay-contract
    c18-evidence-record-contract
    c18-counterexample-contract
    c18-trust-report-contract
    c18-release-gate-contract
    c18-plugin-verification-contract
    c18-backend-conformance-contract
    c18-diagnostic-catalog
    build-c18-pass-risk
    build-c18-proof-obligation
    build-c18-translation-validation
    build-c18-evidence-record
    build-c18-counterexample
    build-c18-trust-report
    build-c18-release-gate
    build-c18-plugin-report
    build-c18-backend-conformance-report
    verify-c18-compiler-verification])
(def ^:private expected-document-ids
  ["C18" "C1" "C15" "C17" "C13" "C14" "B14" "D1"
   "D3" "D6" "D8" "D9" "P3" "SAFE15" "PERF10"
   "BOOT1" "BOOT3" "BOOT8" "TEST2" "TEST10" "TEST11" "TEST13"])
(def ^:private expected-nonclaims
  #{:c18-production-verifier-execution
    :c18-contract-and-artifact-schema-enforcement
    :c18-proof-certificate-and-evidence-checker-execution
    :c18-translation-validation-and-replay-execution
    :c18-release-gate-decision-authority
    :c18-plugin-and-backend-conformance-execution})
(def ^:private expected-verifier-checks
  [:pass-risk-classification-present
   :pass-proof-obligations-structured
   :evidence-kind-matches-risk
   :translation-validation-records-linked
   :verification-replay-records-content-addressed
   :stale-proofs-and-certificates-rejected
   :counterexamples-captured-as-regression-fixtures
   :trust-report-covers-passes-profiles-targets-and-artifacts
   :release-gate-fails-closed-for-missing-required-evidence
   :plugin-passes-listed-in-trust-report
   :backend-conformance-linked-to-target-lowering
   :diagnostics-use-stable-c18-catalog
   :source-spans-and-origin-chains-preserved
   :no-ambient-authority-declared
   :bootstrap-lineage-and-provenance-linked])
(def ^:private expected-verifier-preserves
  [:source-spans :syntax-identity
   :diagnostic-codes :artifact-provenance
   :origin-chains :generated-origin
   :compiler-pipeline-manifest
   :pass-contracts :pass-versions
   :pass-risk-classifications
   :type-facts :effect-facts
   :capability-facts :ownership-facts
   :profile-target-metadata
   :safety-outcomes :runtime-check-records
   :proof-obligations :proof-references
   :certificate-references
   :translation-validation-logs
   :counterexample-artifacts
   :trust-report-entries
   :release-gate-decisions
   :bootstrap-provenance])
(def ^:private expected-diagnostic-catalog
  {:diagnostics
   ["C18-RISK" "C18-EVIDENCE" "C18-VALIDATION" "C18-PROOF"
    "C18-TRUST-REPORT" "C18-RELEASE-GATE" "C18-COUNTEREXAMPLE"
    "C18-PLUGIN" "C18-BACKEND"]
   :required-fields
   [:pass-id :pass-version :risk
    :required-evidence :available-evidence
    :affected-profiles :affected-targets
    :source-or-artifact-id :stage
    :profile :target :remediation]
   :rule-to-message-key
   {:C18-RISK :compiler-verification.risk-gap
    :C18-EVIDENCE :compiler-verification.evidence-gap
    :C18-VALIDATION :compiler-verification.validation-failure
    :C18-PROOF :compiler-verification.proof-rejected
    :C18-TRUST-REPORT :compiler-verification.trust-report-gap
    :C18-RELEASE-GATE :compiler-verification.release-blocked
    :C18-COUNTEREXAMPLE :compiler-verification.counterexample
    :C18-PLUGIN :compiler-verification.plugin-policy
    :C18-BACKEND :compiler-verification.backend-conformance}
   :stable-id-rule :c18-rule-plus-pass-artifact-and-semantic-facts
   :must-not-depend-on
   [:localized-rendering-text :backend-wording
    :host-exception-text :manual-review-prose]})
(def ^:private expected-builder-contracts
  '{build-c18-pass-risk
    [:gravity/pass-risk c18-pass-risk-contract]
    build-c18-proof-obligation
    [:gravity/pass-proof-obligation c18-pass-proof-obligation-contract]
    build-c18-translation-validation
    [:gravity/translation-validation c18-translation-validation-contract]
    build-c18-evidence-record
    [:gravity/pass-evidence c18-evidence-record-contract]
    build-c18-counterexample
    [:gravity/compiler-counterexample c18-counterexample-contract]
    build-c18-trust-report
    [:gravity/compiler-trust-report c18-trust-report-contract]
    build-c18-release-gate
    [:gravity/compiler-release-gate c18-release-gate-contract]
    build-c18-plugin-report
    [:gravity/plugin-verification-report c18-plugin-verification-contract]
    build-c18-backend-conformance-report
    [:gravity/backend-conformance-report c18-backend-conformance-contract]})
(def ^:private expected-builder-missing-required-fields
  '{build-c18-pass-risk
    #{:pass-id :input-ir :output-ir :changed-fact-families
      :stronger-evidence :diagnostic-policy}
    build-c18-proof-obligation
    #{:stage :changed-functions :available-evidence
      :source-spans :origin-chain}
    build-c18-translation-validation
    #{:pass-id}
    build-c18-evidence-record
    #{}
    build-c18-counterexample
    #{:provenance}
    build-c18-trust-report
    #{:artifact-kinds}
    build-c18-release-gate
    #{:stale-proofs :stale-certificates :backend-conformance
      :plugin-policy :diagnostic-goldens :self-hosting-comparison}
    build-c18-plugin-report
    #{:trust-report-entry}
    build-c18-backend-conformance-report
    #{}})
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
        "Required SH-07-B27 coordinator adapter is absent"
        {:id "SH07-C18-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader
    (java.io.PushbackReader.
     (io/reader (path c18-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn- named-top-level-form
  [forms kind name]
  (first
   (filter
    #(and (seq? %)
          (= kind (first %))
          (= name (second %)))
    forms)))

(defn- quoted-body
  [definition-form]
  (let [body (nth definition-form 3)]
    (when (and (seq? body) (= 'quote (first body)))
      (second body))))

(defn- symbols-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (symbol? entry)
         (vswap! found conj entry))
       entry)
     value)
    @found))

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
        "SH-07-B27 records are not uniquely identifiable"
        {:id "SH07-C18-COVERAGE-AMBIGUOUS-INDEX"
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

(def ^:private c18-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c18-relative-path))))

(def ^:private c18-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c18-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c18-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path
          (.resolve
           temp-root
           "right/c18_compiler_verification_pass_correctness.qst")
          left-path (path c18-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c18-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b27-proof-contract-registers-c18-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path c18-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)
        declared-document-source
        (second
         (re-find
          #"(?s):governing-document-ids\s*(\[[^\]]*\])"
          source-text))]
    (is (= "SH-07-B27" (:coverage-milestone contract)))
    (is (= c18-relative-path
           (get-in contract [:authoritative-modules :c18-verification])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c18-verification])))
    (doseq [document
            ["docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
             "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"
             "docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md"
             "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md"
             "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"
             "docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md"
             "docs/phase-03-profile-system/048-p3-meta-profile-specification.md"
             "docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md"
             "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
             "docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md"
             "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"
             "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
             "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md"
             "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md"
             "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
             "docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md"
             "docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md"
             "docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md"
             "docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]]
      (is (contains? documents document)))
    (is (= expected-document-ids
           (edn/read-string declared-document-source)))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c18-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c18-relative-path)))))
    (doseq [source-fact
            [":stage :stage1"
             ":seed :clojure-stage0"
             ":retirement-objective :replace-clojure-seed"
             ":verified-by :clojure-stage0"
             ":compiled-by :clojure-stage0"
             ":production-verifier-status :not-claimed"
             ":scope :source-model-bridge"
             ":no-ambient-authority-declared"
             ":release-behavior :fail-closed"
             ":effects #{}"
             ":capabilities #{}"]]
      (is (string/includes? source-text source-fact)))
    (is (every? (set (:nonclaims contract)) expected-nonclaims))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b27-c18-source-contracts-and-quoted-builders-are-exact
  (let [forms (source-forms)
        namespace-form (first forms)
        namespace-clauses
        (into {}
              (map (fn [clause] [(first clause) (second clause)]))
              (drop 2 namespace-form))
        bootstrap-metadata (get-in namespace-clauses [:metadata :bootstrap])
        definition-forms
        (into {}
              (for [name expected-definition-names]
                [name
                 (or (named-top-level-form forms 'def name)
                     (named-top-level-form forms 'defn name))]))
        diagnostic-form
        (get definition-forms 'c18-diagnostic-catalog)
        replay-form
        (get definition-forms 'c18-replay-contract)
        verifier-form
        (get definition-forms 'verify-c18-compiler-verification)
        verifier-body (quoted-body verifier-form)]
    (is (= 23 (count forms)))
    (is (= 'ns (first namespace-form)))
    (is (= 'gravity.compiler.c18-compiler-verification-pass-correctness
           (second namespace-form)))
    (is (= :meta (:profile namespace-clauses)))
    (is (= :jvm (:target namespace-clauses)))
    (is (= expected-export-names (:exports namespace-clauses)))
    (is (= #{} (:effects namespace-clauses)))
    (is (= #{} (:capabilities namespace-clauses)))
    (is (= :safe (:safety namespace-clauses)))
    (is (= #{:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied
             :accepted-source-extensions :governing-document-ids
             :documents :implements :preserves :authority
             :lineage :conformance}
           (set (keys bootstrap-metadata))))
    (is (= {:stage :stage1
            :component :compiler-verification
            :owner :gravity-source
            :source-language :gravity
            :seed :clojure-stage0
            :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true
            :accepted-source-extensions [".gravity" ".qst"]}
           (select-keys
            bootstrap-metadata
            [:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied
             :accepted-source-extensions])))
    (is (= expected-document-ids
           (:governing-document-ids bootstrap-metadata)
           (:documents bootstrap-metadata)))
    (is (= {:verified-by :clojure-stage0
            :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces
            [:clojure-c18-verification-contract-subset
             :clojure-c18-diagnostic-catalog]}
           (:lineage bootstrap-metadata)))
    (is (= #{:ambient :declared-effects :declared-capabilities :denies}
           (set (keys (:authority bootstrap-metadata)))))
    (is (= #{} (get-in bootstrap-metadata [:authority :ambient])))
    (is (= #{} (get-in bootstrap-metadata
                       [:authority :declared-effects])))
    (is (= #{} (get-in bootstrap-metadata
                       [:authority :declared-capabilities])))
    (is (= expected-definition-names (set (keys definition-forms))))
    (is (= expected-diagnostic-catalog (nth diagnostic-form 2)))
    (is (= 'def (first replay-form)))
    (is (nil? (get definition-forms 'build-c18-replay)))
    (doseq [[builder [expected-artifact expected-contract]]
            expected-builder-contracts]
      (testing (name builder)
        (let [builder-form (get definition-forms builder)
              parameters (nth builder-form 2)
              body (quoted-body builder-form)
              contract-form
              (get definition-forms expected-contract)
              required-fields (get (nth contract-form 2) :required-fields)]
          (is (= 'defn (first builder-form)))
          (is (= expected-artifact (:artifact body)))
          (is (= expected-contract (:contract body)))
          (is (every? (symbols-in body) parameters))
          (is (= (get expected-builder-missing-required-fields builder)
                 (set (remove (set (keys body)) required-fields)))))))
    (is (= 'defn (first verifier-form)))
    (is (= '[verification-record] (nth verifier-form 2)))
    (is (= :stage1-source-owned (:status verifier-body)))
    (is (= :source-model-bridge (:scope verifier-body)))
    (is (= :not-claimed (:production-verifier-status verifier-body)))
    (is (= 'verification-record (:verification-record verifier-body)))
    (is (= expected-verifier-checks (:checks verifier-body)))
    (is (= 'c18-diagnostic-catalog (:diagnostics verifier-body)))
    (is (= :fail-closed (:release-behavior verifier-body)))
    (is (= expected-verifier-preserves (:preserves verifier-body)))))

(deftest sh07-b27-c18-source-has-exact-authentic-coverage
  (let [artifact @c18-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.compiler.c18-compiler-verification-pass-correctness
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

(deftest sh07-b27-c18-definitions-quotes-and-lookup-boundaries-are-exact
  (let [core-artifact (core @c18-artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 10 (count (filter #(= :fn (:core-form %)) nodes))))
    (is (= 10 (count quote-nodes)))
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
     "quoted source models do not execute production verification or release gates"
      (doseq [node quote-nodes]
        (is (= [] (:children node)))
        (is (= :no-evaluation (get-in node [:evaluation :kind])))
        (is (= [] (get-in node [:evaluation :order])))
        (is (= #{:quoted-form-id :quoted-syntax-id
                 :quoted-kind :quoted-value}
               (set (keys (:attributes node)))))))))

(deftest sh07-b27-c18-is-deterministic-path-neutral-and-provenanced
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

(deftest sh07-b27-c18-replay-and-alteration-containment-pass
  (let [artifact @c18-artifact
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
               "/altered/root/c18_compiler_verification_pass_correctness.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c18-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b27-existing-rejected-families-remain-paired-and-structured
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
