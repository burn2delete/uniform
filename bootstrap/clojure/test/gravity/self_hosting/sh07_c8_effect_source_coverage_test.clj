(ns gravity.self-hosting.sh07-c8-effect-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c8_effect_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C8 effect source test is not on the classpath"
        {:id "SH07-C8-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C8-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c8-relative-path
  "bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 80761)
(def ^:private expected-source-revision-id
  "sha256:ff072574ed4bd6feaa8714e2f221b64d633fe2cd601d55de2b0df1eff4983a70")
(def ^:private expected-coverage
  {:fragment-count 40
   :root-form-count 40
   :form-count 3301
   :binding-count 410
   :resolution-count 1078})
(def ^:private expected-core-census
  {:core-node-count 2788
   :definition-count 40
   :call-count 443
   :reference-count 841
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 1131
    :collection-literal 136
    :def 40
    :reference 841
    :call 443
    :if 139
    :let 14
    :loop 3
    :recur 4
    :quote 3
    :fn 34}})
(def ^:private expected-export-names
  '[c8-effect-graph-contract
    c8-effect-legality-contract
    c8-capability-proof-contract
    c8-build-effect-contract
    c8-replay-ordering-contract
    c8-effect-diagnostic-catalog
    build-c8-effect-node
    build-c8-capability-proof
    verify-c8-effect-checker
    sh09-effect-policy
    sh09-check-effect-request
    sh09-verify-effect-result
    sh09-authenticated-sh08-adapter-policy
    sh09-build-authenticated-pure-effect-result
    sh09-verify-authenticated-pure-effect-result
    sh09-authenticated-effect-identity-requests
    sh09-bind-authenticated-effect-identities
    sh09-verify-authenticated-effect-identities])
(def ^:private expected-definition-names
  '#{c8-effect-graph-contract
     c8-effect-legality-contract
     c8-capability-proof-contract
     c8-build-effect-contract
     c8-replay-ordering-contract
     c8-effect-diagnostic-catalog
     build-c8-effect-node
     build-c8-capability-proof
     verify-c8-effect-checker
     sh09-effect-registry
     sh09-effect-policy
     sh09-diagnostic
     sh09-rejected
     sh09-bounds-rejected
     sh09-not
     sh09-and
     sh09-not-equal
     sh09-member?
     sh09-phase-valid?
     sh09-authority-mode-valid?
     sh09-boolean?
     sh09-exact-keys?
     sh09-lowercase-hex?
     sh09-sha256-id?
     sh09-aggregate?
     sh09-aggregate-width
     sh09-aggregate-components
     sh09-max
     sh09-structural-preflight
     sh09-authority-collections-valid?
     sh09-hermetic-build-policy?
     sh09-valid-request-shape?
     sh09-authority-allows?
     sh09-capability-authorities-allow?
     sh09-provider-allows?
     sh09-grant-allows?
     sh09-replay-record-valid?
     sh09-accepted
     sh09-check-effect-request
     sh09-authenticated-sh08-adapter-policy
     sh09-adapter-type-fact-valid?
     sh09-adapter-type-table-valid?
     sh09-adapter-type-facts-valid?
     sh09-adapter-module-valid?
     sh09-adapter-upstream-identity-valid?
     sh09-adapter-upstream-valid?
     sh09-adapter-pure-request
     sh09-adapter-pure-products
     sh09-adapter-diagnostic
     sh09-adapter-function-fact-valid?
     sh09-adapter-function-facts-valid?
     sh09-adapter-call-fact-valid?
     sh09-adapter-call-facts-valid?
     sh09-adapter-function-upstream-valid?
     sh09-adapter-function-request
     sh09-adapter-function-products
     sh09-adapter-function-effect-table
     sh09-build-authenticated-function-effect-result
     sh09-build-authenticated-pure-effect-result
     sh09-verify-authenticated-pure-effect-result
     sh09-effect-identity-request
     sh09-effect-identity-requests-loop
     sh09-authenticated-effect-identity-requests
     sh09-resolved-effect-identities-valid?
     sh09-bound-effect-identities
     sh09-bind-authenticated-effect-identities
     sh09-verify-authenticated-effect-identities
     sh09-verify-effect-result})
(def ^:private quoted-definition-names
  '#{build-c8-effect-node
     build-c8-capability-proof
     verify-c8-effect-checker})
(def ^:private expected-executable-sh09-names
  (set/difference
   expected-definition-names
   '#{c8-effect-graph-contract
      c8-effect-legality-contract
      c8-capability-proof-contract
      c8-build-effect-contract
      c8-replay-ordering-contract
      c8-effect-diagnostic-catalog
      build-c8-effect-node
      build-c8-capability-proof
      verify-c8-effect-checker}))
(def ^:private expected-document-ids
  ["C8" "L6" "L9" "L12" "L15"
   "C7" "C9" "C10" "C11" "C12"
   "C15" "P1" "P3" "P6" "P7" "P8"
   "R1" "R11" "PKG1" "PKG7"
   "SAFE1" "SAFE6" "BOOT1" "BOOT2"
   "BOOT3" "BOOT4" "BOOT5" "BOOT6"
   "BOOT7" "BOOT8" "TEST2"])
(def ^:private expected-policy
  {:artifact :gravity/sh09-effect-policy
   :version 2
   :request-artifact :gravity/sh09-normalized-effect-request
   :result-artifact :gravity/sh09-effect-legality-result
   :profiles #{:meta}
   :targets #{:jvm}
   :phases #{:build}
   :effects
   #{:error/raise
     :compiler/read-ir :compiler/write-ir
     :build/read-file :build/write-artifact
     :build/env :build/network :build/exec
     :build/time :build/random
     :build/model-call :build/tool-call}
   :structural-bounds
   {:maximum-nodes 8192
    :maximum-depth 32
    :maximum-width 256
    :maximum-scalars 7000
    :maximum-scalar-units 32768
    :maximum-collections 2048}
   :authorities
   [:source-declaration :profile :package :deployment
    :build-policy :provider :grant :phase :safety]
   :diagnostics
   ["C8-UNDECLARED" "C8-PROFILE" "C8-CAPABILITY" "C8-BUILD"
    "C8-REPLAY" "C8-ORDER" "C8-RUNTIME" "C8-UNKNOWN" "C8-VERIFY"]
   :pending
   [:effect-inference
    :function-latent-effects
    :transitive-call-effects
    :handled-effects
    :namespace-and-module-summaries
    :runtime-profile-policy
    :authenticated-sh08-function-and-effectful-adapters
    :mir-preservation]})
(def ^:private expected-registry
  {:error/raise
   {:capability nil :profiles #{:meta} :phases #{:build}
    :replay-sensitive false :ordering :sequence}
   :compiler/read-ir
   {:capability :compiler/ir-read :profiles #{:meta} :phases #{:build}
    :replay-sensitive false :ordering :sequence}
   :compiler/write-ir
   {:capability :compiler/ir-transform :profiles #{:meta} :phases #{:build}
    :replay-sensitive false :ordering :sequence}
   :build/read-file
   {:capability :build/file-read :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/write-artifact
   {:capability :build/artifact-write :profiles #{:meta} :phases #{:build}
    :replay-sensitive false :ordering :sequence}
   :build/env
   {:capability :build/environment-read :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/network
   {:capability :build/network :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/exec
   {:capability :build/process :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/time
   {:capability :build/clock :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/random
   {:capability :build/random :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/model-call
   {:capability :build/model-call :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :build/tool-call
   {:capability :build/tool-call :profiles #{:meta} :phases #{:build}
    :replay-sensitive true :ordering :sequence}
   :network/http
   {:capability :http/client
    :profiles #{:hosted :native :distributed :ai}
    :phases #{:runtime} :replay-sensitive true :ordering :sequence}
   :filesystem/read
   {:capability :fs/read :profiles #{:hosted :native}
    :phases #{:runtime} :replay-sensitive true :ordering :sequence}
   :shell/exec
   {:capability :process/exec :profiles #{:hosted :native}
    :phases #{:runtime} :replay-sensitive true :ordering :sequence}
   :time/read
   {:capability :time/read
    :profiles #{:hosted :native :distributed :ai}
    :phases #{:runtime} :replay-sensitive true :ordering :sequence}})
(def ^:private expected-diagnostic-catalog
  {:diagnostics
   ["C8-UNDECLARED" "C8-PROFILE" "C8-CAPABILITY" "C8-BUILD"
    "C8-REPLAY" "C8-ORDER" "C8-RUNTIME" "C8-UNKNOWN" "C8-VERIFY"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :core-node-id :source-span
    :generated-origin-chain :function :namespace :effect :capability
    :profile :target :provider :grant :remediation]
   :stops
   [:mir-construction :optimizer :target-lowering
    :runtime-selection :package-release]
   :must-not-depend-on
   [:localized-rendering-text :host-runtime-provider-discovery
    :backend-only-effect-legality :implicit-capability-grant]})
(def ^:private expected-contract-structures
  {'c8-effect-graph-contract
   {:artifact :gravity/c8-effect-graph-contract
    :required-fields
    [:module :nodes :functions :namespace
     :build-effects :replay-required
     :ordering-constraints :residual-effects :diagnostics]
    :node-fields
    [:core-node-id :direct :latent :transitive
     :ordering :source-span :type-fact-id]
    :function-fields
    [:function-id :declared :inferred :latent
     :throws :capabilities :source-span]
    :summaries
    [:function-latent-effect-table
     :namespace-effect-summary
     :module-effect-summary]
    :must-preserve
    [:core-node-identity :source-spans :type-facts
     :latent-effects :thrown-error-effects :artifact-provenance]
    :forbidden
    [:backend-only-effect-inference
     :runtime-only-effect-legality
     :hidden-macro-runtime-effect
     :missing-effect-diagnostic]}
   'c8-effect-legality-contract
   {:artifact :gravity/c8-effect-legality-contract
    :authorities
    [:function :namespace :profile :package
     :deployment :runtime :safety]
    :result-values [:accepted :rejected]
    :required-fields
    [:effect :source :allowed-by
     :required-capabilities :granted-capabilities
     :profile :target :provider :grant :result]
    :intersection-rules
    [:source-declaration-required
     :profile-must-allow
     :package-grant-must-cover
     :deployment-grant-must-cover
     :runtime-provider-must-support
     :safe-mode-must-permit]
    :forbidden
    [:backend-authority-widening
     :runtime-authority-widening
     :capability-from-effect-name
     :implicit-unsafe-effect-acceptance]}
   'c8-capability-proof-contract
   {:artifact :gravity/c8-capability-proof-contract
    :required-fields
    [:effect :source :capability :grant
     :provider :phase :status]
    :grant-fields
    [:scope :principal :phase :source-span :package :deployment]
    :statuses
    [:accepted :rejected :requires-runtime-check
     :requires-human-review]
    :consumers
    [:profile-validation :safety-analysis
     :runtime-selection :audit-tooling :package-verification]
    :forbidden
    [:ambient-authority :provider-without-grant
     :grant-without-source :capability-proof-as-safety-proof]}
   'c8-build-effect-contract
   {:artifact :gravity/c8-build-effect-contract
    :effect-domains [:build :runtime :package]
    :build-sources
    [:reader-extension :macro :compile-time-evaluation
     :compiler-plugin :target-probe :code-generator
     :dependency-resolution :artifact-generation]
    :hermeticity-requirements
    [:declared-build-effect :package-grant
     :replay-record :artifact-provenance :diagnostic-on-gap]
    :must-separate
    [:macro-build-effects :macro-emitted-runtime-effects
     :package-build-effects :program-runtime-effects]
    :forbidden
    [:runtime-effect-hidden-as-build-effect
     :build-effect-hidden-as-runtime-effect
     :unguarded-target-probe
     :unrecorded-dependency-effect]}
   'c8-replay-ordering-contract
   {:artifact :gravity/c8-replay-ordering-contract
    :replay-sensitive-effects
    [:time :randomness :external-io
     :database-read :workflow-event
     :model-call :tool-call :ai-human-review
     :scheduler-sensitive-operation]
    :ordering-modes
    [:sequence :may-commute :must-not-duplicate
     :must-not-eliminate :bracket-resource
     :preserve-volatile-order :preserve-mmio-order
     :preserve-atomic-order :preserve-replay-order]
    :required-fields
    [:effect :core-node-id :source-span
     :replay-record :ordering :consumer]
    :downstream-consumers
    [:workflow-runtime :ai-runtime :test-replay :optimizer :audit]
    :forbidden
    [:replay-gap :optimizer-reorder-without-proof
     :duplicate-noncommutative-effect
     :eliminate-observable-effect]}})
(def ^:private expected-check-rejection-reasons
  #{:ambient-authority-denied
    :build-effect-not-granted
    :capability-not-allowed-by-all-authorities
    :capability-phase-mismatch
    :capability-policy-mismatch
    :declared-authority-required
    :effect-forbidden-by-profile
    :effect-not-allowed-by-deployment
    :effect-not-allowed-by-package
    :effect-not-declared
    :effect-ordering-mismatch
    :effect-rejected-by-safety-policy
    :explicit-authority-required
    :grant-does-not-authorize-request
    :language-effect-resource-subject-forbidden
    :malformed-normalized-effect-request
    :provider-does-not-support-request
    :pure-operation-carries-authority
    :replay-sensitive-effect-without-record
    :resource-subject-required
    :slice-phase-mismatch
    :slice-profile-mismatch
    :slice-target-mismatch
    :unexpected-replay-record
    :unregistered-effect})
(def ^:private expected-bound-reasons
  #{:request-node-bound :request-depth-bound :request-width-bound
    :request-collection-bound :request-scalar-count-bound
    :request-scalar-bound})
(def ^:private expected-nonclaims
  #{:c8-production-effect-checker-execution
    :c8-contract-and-diagnostic-schema-enforcement
    :sh09-authenticated-sh08-function-and-effectful-adapters
    :sh09-effect-inference-and-transitive-call-effects
    :sh09-handled-effects-and-module-summaries
    :sh09-runtime-profile-policy
    :sh09-mir-preservation
    :sh09-complete})
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
        "Required SH-07-B29 coordinator adapter is absent"
        {:id "SH07-C8-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader
    (java.io.PushbackReader. (io/reader (path c8-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(defn- named-top-level-form
  [forms kind name]
  (first
   (filter
    #(and (seq? %) (= kind (first %)) (= name (second %)))
    forms)))

(defn- quoted-body
  [definition-form]
  (let [body (nth definition-form 3 nil)]
    (when (and (seq? body) (= 'quote (first body)))
      (second body))))

(defn- symbols-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (symbol? entry) (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- keyword-literals-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (keyword? entry) (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- collect-calls
  [operator value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- invalid-source-if-forms
  [forms]
  (vec
   (filter #(not= 4 (count %))
           (mapcat #(collect-calls 'if %) forms))))

(defn- parse-registry
  [form]
  (loop [branch (nth form 3)
         entries {}]
    (if (= nil branch)
      entries
      (let [[operator condition result alternative] branch]
        (when-not
         (and (= 'if operator)
              (seq? condition)
              (= '= (first condition))
              (= 'effect (second condition))
              (keyword? (nth condition 2))
              (map? result))
          (throw
           (ex-info
            "C8 registry is not the inspected bounded conditional chain"
            {:id "SH07-C8-REGISTRY-SHAPE"
             :branch branch})))
        (recur alternative
               (assoc entries (nth condition 2) result))))))

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
  (let [authenticated-request (request artifact)]
    {:fragment-count (count (:fragment-manifest authenticated-request))
     :root-form-count (count (:top-level-form-ids authenticated-request))
     :form-count (count (:forms authenticated-request))
     :binding-count (count (:binding-table authenticated-request))
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
        "SH-07-B29 records are not uniquely identifiable"
        {:id "SH07-C8-COVERAGE-AMBIGUOUS-INDEX"
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

(def ^:private c8-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c8-relative-path))))

(def ^:private c8-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c8-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c8-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path
          (.resolve temp-root "right/c8_effect_checker_engine.qst")
          left-path (path c8-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c8-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b29-proof-contract-registers-c8-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path c8-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "SH-07-B47" (:coverage-milestone contract)))
    (is (= c8-relative-path
           (get-in contract [:authoritative-modules :c8-effects])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c8-effects])))
    (doseq [document
            ["docs/phase-01-core-language/016-l6-effect-system-specification.md"
             "docs/phase-01-core-language/019-l9-error-handling-specification.md"
             "docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md"
             "docs/phase-01-core-language/025-l15-capability-provider-specification.md"
             "docs/phase-03-profile-system/046-p1-profile-system-specification.md"
             "docs/phase-03-profile-system/048-p3-meta-profile-specification.md"
             "docs/phase-03-profile-system/051-p6-firmware-profile-specification.md"
             "docs/phase-03-profile-system/052-p7-kernel-profile-specification.md"
             "docs/phase-03-profile-system/053-p8-hardware-profile-specification.md"
             "docs/phase-06-compiler-architecture/086-c7-type-checker-design.md"
             "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md"
             "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md"
             "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md"
             "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"
             "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md"
             "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
             "docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md"
             "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
             "docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md"
             "docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md"
             "docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md"
             "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md"
             "docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md"
             "docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]]
      (is (contains? documents document)))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c8-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c8-relative-path)))))
    (doseq [source-fact
            [":stage :stage1"
             ":seed :clojure-stage0"
             ":retirement-objective :replace-clojure-seed"
             ":verified-by :clojure-stage0"
             ":compiled-by :clojure-stage0"
             ":effects #{}"
             ":capabilities #{}"]]
      (is (string/includes? source-text source-fact)))
    (is (every? (set (:nonclaims contract)) expected-nonclaims))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b29-c8-source-contracts-policy-and-boundaries-are-exact
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
        executable-names
        (set
         (for [[name form] definition-forms
               :when (and (= 'defn (first form))
                          (nil? (quoted-body form)))]
           name))
        policy-form (get definition-forms 'sh09-effect-policy)
        registry-form (get definition-forms 'sh09-effect-registry)
        check-form (get definition-forms 'sh09-check-effect-request)
        request-shape-form
        (get definition-forms 'sh09-valid-request-shape?)
        preflight-form (get definition-forms 'sh09-structural-preflight)
        verifier-form (get definition-forms 'sh09-verify-effect-result)
        legacy-verifier-form
        (get definition-forms 'verify-c8-effect-checker)
        rejection-calls (collect-calls 'sh09-rejected check-form)
        emitted-reasons
        (set
         (mapcat
          #(filter keyword?
                   (tree-seq coll? seq (nth % 3)))
          rejection-calls))]
    (is (= 69 (count forms)))
    (is (= 'gravity.compiler.c8-effect-checker-engine
           (second namespace-form)))
    (is (= :meta (:profile namespace-clauses)))
    (is (= :jvm (:target namespace-clauses)))
    (is (= expected-export-names (:exports namespace-clauses)))
    (is (= #{} (:effects namespace-clauses)))
    (is (= #{} (:capabilities namespace-clauses)))
    (is (= :safe (:safety namespace-clauses)))
    (is (= expected-document-ids (:documents bootstrap-metadata)))
    (is (= #{:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied
             :documents :implements :preserves :lineage :conformance}
           (set (keys bootstrap-metadata))))
    (is (= {:stage :stage1
            :component :effect-checker
            :owner :gravity-source
            :source-language :gravity
            :seed :clojure-stage0
            :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys
            bootstrap-metadata
            [:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied])))
    (is (= #{:effect-graph :effect-node-table
             :function-latent-effect-table
             :namespace-effect-summary :module-effect-summary
             :capability-proof-record :build-effect-log
             :replay-effect-requirements
             :effect-ordering-constraints :residual-effect-report
             :effect-legality-report :effect-diagnostic-catalog}
           (set (:implements bootstrap-metadata))))
    (is (= #{:source-spans :syntax-identity :diagnostic-codes
             :artifact-provenance :core-node-identity :type-facts
             :function-type-identity :latent-effects
             :thrown-error-effects :profile-target-metadata
             :effect-capability-metadata :package-grant-identity
             :runtime-provider-identity :replay-obligations
             :ordering-constraints :build-runtime-effect-separation
             :residual-effects}
           (:preserves bootstrap-metadata)))
    (is (= {:verified-by :clojure-stage0
            :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces
            [:clojure-c8-effect-checker-contract-subset
             :clojure-c8-diagnostic-catalog]}
           (:lineage bootstrap-metadata)))
    (is (= expected-definition-names (set (keys definition-forms))))
    (is (= 6 (count (filter #(= 'def (first %))
                            (vals definition-forms)))))
    (is (= 62 (count (filter #(= 'defn (first %))
                             (vals definition-forms)))))
    (is (= quoted-definition-names
           (set
            (for [[name form] definition-forms
                  :when (quoted-body form)]
              name))))
    (is (= expected-executable-sh09-names executable-names))
    (is (= 59 (count executable-names)))
    (is (= expected-policy (nth policy-form 3)))
    (is (= expected-registry (parse-registry registry-form)))
    (is (= expected-diagnostic-catalog
           (nth (get definition-forms
                     'c8-effect-diagnostic-catalog)
                2)))
    (doseq [[name expected] expected-contract-structures]
      (is (= expected (nth (get definition-forms name) 2))))
    (is (= 30 (count rejection-calls)))
    (is (= 15 (count (collect-calls 'if request-shape-form))))
    (is (every? #(= 4 (count %))
                (collect-calls 'if request-shape-form)))
    (is (= expected-check-rejection-reasons emitted-reasons))
    (is (= expected-bound-reasons
           (set
            (filter
             #(string/starts-with? (name %) "request-")
             (keyword-literals-in preflight-form)))))
    (is (= :gravity/c8-effect-node
           (:artifact
            (quoted-body
             (get definition-forms 'build-c8-effect-node)))))
    (is (= :gravity/c8-capability-proof
           (:artifact
            (quoted-body
             (get definition-forms 'build-c8-capability-proof)))))
    (is (= :stage1-source-owned
           (:status (quoted-body legacy-verifier-form))))
    (is (= 'c8-effect-diagnostic-catalog
           (:diagnostics (quoted-body legacy-verifier-form))))
    (is (contains? (symbols-in verifier-form)
                   'sh09-check-effect-request))
    (is (string/includes? (pr-str verifier-form)
                          ":effect-result-substitution"))
    (is (string/includes? (pr-str verifier-form)
                          ":effect-result-structural-bound"))))

(deftest sh07-b29-c8-source-control-form-arities-are-bounded
  (let [forms (source-forms)
        form-node-counts
        (mapv #(count (tree-seq coll? seq %)) forms)]
    (is (empty? (invalid-source-if-forms forms)))
    (is (= '[(if true)]
           (invalid-source-if-forms '[(if true)])))
    (is (= '[(if true :then :else :extra)]
           (invalid-source-if-forms
            '[(if true :then :else :extra)])))
    (is (<= (apply max form-node-counts) 1024)
        "Conservative reader-tree admission is not the authoritative C6 form-id census")))

(deftest sh07-b29-c8-structural-limitations-remain-explicit
  (let [forms (source-forms)
        definition
        (fn [name] (or (named-top-level-form forms 'def name)
                       (named-top-level-form forms 'defn name)))
        policy (nth (definition 'sh09-effect-policy) 3)
        registry (parse-registry (definition 'sh09-effect-registry))
        request-shape-form (definition 'sh09-valid-request-shape?)
        provider-form (definition 'sh09-provider-allows?)
        grant-form (definition 'sh09-grant-allows?)
        aggregate-form (definition 'sh09-aggregate-components)
        preflight-form (definition 'sh09-structural-preflight)
        checker-form (definition 'sh09-check-effect-request)
        verifier-form (definition 'sh09-verify-effect-result)
        sha-form (definition 'sh09-sha256-id?)
        runtime-effects
        (set
         (for [[effect record] registry
               :when (contains? (:phases record) :runtime)]
           effect))]
    (testing "schemas are bounded but remain shallow and non-exact"
      (is (not (contains? (symbols-in request-shape-form)
                          'sh09-exact-keys?)))
      (is (not (contains? (symbols-in provider-form)
                          'sh09-exact-keys?)))
      (is (not (contains? (symbols-in grant-form)
                          'sh09-exact-keys?))))
    (testing "the pure branch is wider than a complete policy contract"
      (is (string/includes? (pr-str checker-form)
                            "(= nil (get request :effect))"))
      (is (not (string/includes?
                (pr-str checker-form)
                ":pure-operation-profile-policy"))))
    (testing "runtime registry entries are not supported by this policy"
      (is (= #{:network/http :filesystem/read :shell/exec :time/read}
             runtime-effects))
      (is (= #{:build} (:phases policy)))
      (is (= #{:meta} (:profiles policy)))
      (is (= #{:jvm} (:targets policy)))
      (is (empty? (set/intersection runtime-effects (:effects policy)))))
    (testing "digest, traversal, and verification remain seed-local"
      (is (not (contains? (symbols-in sha-form)
                          'authenticated-envelope-verify-template)))
      (is (contains? (symbols-in aggregate-form) 'keys))
      (is (not (contains? (symbols-in aggregate-form) 'sort)))
      (is (not (string/includes? (pr-str preflight-form)
                                 ":identity-cycle")))
      (is (contains? (symbols-in verifier-form)
                     'sh09-check-effect-request))
      (is (not (contains? (symbols-in verifier-form)
                          'independent-effect-verifier))))
    (is (= (:pending expected-policy) (:pending policy)))))

(deftest sh07-b29-c8-source-has-exact-authentic-coverage
  (let [artifact @c8-artifact
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
    (is (= 'gravity.compiler.c8-effect-checker-engine
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

(deftest sh07-b29-c8-calls-lookups-and-error-effect-are-exact
  (let [core-artifact (core @c8-artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        calls (:calls core-artifact)
        get-calls
        (filterv
         #(= 'get
             (get-in reference-by-node-id
                     [(:operator-node-id %) :symbol]))
         calls)
        literal-keyword-get-calls
        (filterv
         #(keyword?
           (get-in node-by-id
                   [(second (:argument-node-ids %))
                    :attributes :value]))
         get-calls)
        dynamic-get-calls
        (filterv
         #(not
           (keyword?
            (get-in node-by-id
                    [(second (:argument-node-ids %))
                     :attributes :value])))
         get-calls)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)
        forms (source-forms)
        registry
        (parse-registry
         (named-top-level-form forms 'defn 'sh09-effect-registry))]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 178 (count get-calls)))
    (is (= 169 (count literal-keyword-get-calls)))
    (is (= 9 (count dynamic-get-calls)))
    (is (= {:reference 5 :call 4}
           (frequencies
            (map
             #(get-in node-by-id
                      [(second (:argument-node-ids %)) :core-form])
             dynamic-get-calls))))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero?
         (count
          (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (is (= 3 (count quote-nodes)))
    (doseq [node quote-nodes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))
    (is (= {:capability nil
            :profiles #{:meta}
            :phases #{:build}
            :replay-sensitive false
            :ordering :sequence}
           (get registry :error/raise)))
    (is (contains? (:effects expected-policy) :error/raise))
    (is (not (contains? (:effects expected-policy) :network/http)))))

(deftest sh07-b29-c8-is-deterministic-path-neutral-and-provenanced
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

(deftest sh07-b29-c8-replay-and-alteration-containment-pass
  (let [artifact @c8-artifact
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
             ["call binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :calls 0 :operator-binding-id]
               zero-id)
              :calls-replay?]
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
               "/altered/root/c8_effect_checker_engine.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c8-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b29-existing-rejected-families-remain-paired-and-structured
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
