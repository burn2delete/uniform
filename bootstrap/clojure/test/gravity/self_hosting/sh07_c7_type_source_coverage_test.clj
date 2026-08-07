(ns gravity.self-hosting.sh07-c7-type-source-coverage-test
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
         "gravity/self_hosting/sh07_c7_type_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C7 type source test is not on the classpath"
        {:id "SH07-C7-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C7-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 205845)
(def ^:private expected-source-revision-id
  "sha256:2b6f5dfa13c9de10514d7faa3cad3c422fda52a158284557bdf824ff38e5191a")
(def ^:private expected-plan-semantic-id
  "sha256:b43c1016f9610810cb9e04d2bf67bf2583f36cfec97f358015f9d2a9e578a3ee")
(def ^:private expected-functions-semantic-id
  "sha256:26370d9e66576ad54120367cd29524b0f0e2775a76a28521f376bee6507e7b7f")
(def ^:private expected-function-names-id
  "sha256:c1b894e6d8636585be0ff7be206213bb5a736691ee164071b3cb2c062f10d333")
(def ^:private expected-function-shapes-id
  "sha256:86bb1e551c08f3ed4afa20da3c6cdcbfd4423630738b00c02292f83d1d0f388b")
(def ^:private expected-public-function-hashes
  {'sh08-type-core-artifact
   "sha256:9a6ce8c438e9126c44c8610e740909f1aa31381bded4e375cc8c48e6ea0cffdb"
   'sh08-verify-type-result
   "sha256:0494d7cafc0ff8651d1f5467f356286952033e9f406d4177fc180c0c96adf602"
   'sh08-function-type-boundary-policy
   "sha256:d887f147cda91b8bd6bb613808aa7d5fd574297cc801a1ea17496988962d605c"
   'sh08-function-type-core-artifact
   "sha256:79ac1e36aa8119f1f838d360c6fc25d45aa27c947ecf05cf25faac34989b091b"
   'sh08-verify-function-type-result
   "sha256:8dc884ece47f4f039d81371aaf15bb65f33e689718a2545ce161f3caf69abe7f"})
(def ^:private expected-coverage
  {:fragment-count 142
   :root-form-count 142
   :form-count 12759
   :binding-count 1114
   :local-binding-count 852
   :resolution-count 5221})
(def ^:private expected-core-census
  {:core-node-count 10405
   :definition-count 142
   :call-count 2069
   :function-record-count 137
   :call-edge-count 2069
   :recursion-component-count 10
   :reference-count 4117
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 2852
    :collection-literal 263
    :def 142
    :reference 4117
    :call 2069
    :if 566
    :let 98
    :loop 73
    :recur 85
    :quote 3
    :fn 137}})
(def ^:private expected-export-names
  '[c7-type-fact-contract
    c7-type-environment-contract
    c7-constraint-ledger-contract
    c7-dynamic-boundary-contract
    c7-type-diagnostic-catalog
    build-c7-type-fact
    build-c7-function-type
    sh08-primitive-type-from-literal-kind
    sh08-primitive-type-from-collection-kind
    sh08-type-core-artifact
    sh08-verify-type-result
    sh08-function-type-boundary-policy
    sh08-function-type-core-artifact
    sh08-verify-function-type-result
    verify-c7-type-checker])
(def ^:private expected-definition-names
  '#{c7-type-fact-contract
     c7-type-environment-contract
     c7-constraint-ledger-contract
     c7-dynamic-boundary-contract
     c7-type-diagnostic-catalog
     build-c7-type-fact
     build-c7-function-type
     sh08-local-bounds-value
     sh08-vector-contains?
     sh08-all-unique?
     sh08-exact-keys?
     sh08-lowercase-hex?
     sh08-sha256-id?
     sh08-bounded-vector?
     sh08-keyword-vector?
     sh08-sha-vector?
     sh08-aggregate?
     sh08-aggregate-width
     sh08-aggregate-components
     sh08-max
     sh08-carrier-preflight
     sh08-origin-shape?
     sh08-origin-vector?
     sh08-source-shape?
     sh08-module-shape?
     sh08-node-module-matches?
     sh08-preserved-declarations-match?
     sh08-node-shape?
     sh08-children-seen?
     sh08-validate-node-sequence
     sh08-roots-valid?
     sh08-reachable-node-count
     sh08-validate-core-shape
     sh08-primitive-type-from-literal-kind
     sh08-primitive-type-from-collection-kind
     sh08-third
     sh08-type-diagnostic
     sh08-type-fact
     sh08-accepted-node
     sh08-rejected-node
     sh08-child-types
     sh08-even-count?
     sh08-type-node
     sh08-type-nodes
     sh08-type-core-artifact
     sh08-verify-type-result
     verify-c7-type-checker})
(def ^:private expected-executable-sh08-names
  (disj expected-definition-names
        'c7-type-fact-contract
        'c7-type-environment-contract
        'c7-constraint-ledger-contract
        'c7-dynamic-boundary-contract
        'c7-type-diagnostic-catalog
        'build-c7-type-fact
        'build-c7-function-type
        'verify-c7-type-checker))
(def ^:private expected-definition-names-hash
  "sha256:f2d506a370731f999a0c855ead8facbd0f6b87c5c1222046fa5be8984485c5b3")
(def ^:private expected-executable-names-hash
  "sha256:684e0690bcc706a56f838cdde63aea3b3df4da8cce412d86c1f0b244335d42b9")
(def ^:private expected-document-ids
  ["C7" "L5" "L7" "L8" "L9" "L10"
   "C5" "C6" "C8" "C9" "C10" "C11"
   "C12" "C14" "C15" "P1" "SAFE1"
   "SAFE2" "SAFE3" "SAFE4" "SAFE5"
   "SAFE6" "SAFE9" "BOOT1" "BOOT2"
   "BOOT3" "BOOT4" "BOOT5" "BOOT6"
   "BOOT7" "BOOT8" "TEST2"])
(def ^:private expected-bounds
  {:maximum-core-nodes 65536
   :maximum-root-nodes 1024
   :maximum-node-children 1024
   :maximum-origin-entries 256
   :maximum-metadata-entries 256
   :maximum-effects 128
   :maximum-capabilities 128
   :maximum-exports 1024
   :maximum-carrier-nodes 1048576
   :maximum-carrier-depth 128
   :maximum-carrier-width 65536
   :maximum-scalar-units 16777216})
(def ^:private expected-pending
  [:authenticated-coordinator-adapter
   :resolved-typed-artifact-digest
   :functions :calls :locals :records :unions
   :protocols :generics :casts :dynamic-boundaries
   :ownership-types :layout-types :schema-types
   :list-lowering])
(def ^:private expected-nonclaims
  #{:c7-production-type-checker-execution
    :c7-contract-and-diagnostic-schema-enforcement
    :sh08-authenticated-coordinator-adapter
    :sh08-resolved-typed-artifact-digest
    :sh08-inference-constraints-generics-and-profile-legality
    :sh08-dynamic-layout-schema-ownership-and-diagnostic-execution
    :sh08-complete})
(def ^:private expected-diagnostic-catalog
  {:diagnostics
   ["C7-TYPE-MISMATCH" "C7-ANNOTATION" "C7-DYNAMIC" "C7-CAST"
    "C7-NULLABILITY" "C7-GENERIC" "C7-PROTOCOL" "C7-LAYOUT"
    "C7-SCHEMA" "C7-VERIFY"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :core-node-id :type-id
    :source-span :binding-id :constraint-id :profile :target
    :remediation]
   :stops
   [:effect-checking :ownership-checking :safety-analysis
    :mir-construction :target-lowering]
   :must-not-depend-on
   [:host-reflection :backend-only-legality
    :runtime-only-type-resolution :localized-rendering-text]})
(def ^:private expected-contract-structures
  {'c7-type-fact-contract
   {:artifact :gravity/c7-type-fact-contract
    :required-fields
    [:core-node-id :type-id :type-form
     :source-span :binding-id :profile :target
     :effects :capabilities :ownership
     :layout :nullability :dynamic-boundary]
    :fact-kinds
    [:literal :local :binding :function :generic
     :protocol-dispatch :schema-derived :layout
     :resource :dynamic :cast :foreign]
    :must-preserve
    [:core-node-identity :source-spans
     :binding-identity :profile-target-metadata
     :effect-capability-metadata :artifact-provenance]
    :forbidden
    [:untyped-core-node :backend-only-type-legality
     :implicit-dynamic-boundary :unchecked-nullability
     :erased-ownership-resource-fact]}
   'c7-type-environment-contract
   {:artifact :gravity/c7-type-environment-contract
    :environment-fields
    [:namespace :imports :locals :bindings
     :type-aliases :protocols :schema-types
     :profile :target :dynamic-boundaries]
    :records
    [:type-fact-table :function-type-table
     :generic-instantiation-table :protocol-dispatch-table
     :layout-fact-table :ownership-resource-fact-table
     :schema-type-link-table]
    :feeds
    [:effect-checking :ownership-checking
     :safety-analysis :mir-construction
     :domain-ir-lowering :diagnostic-rendering]
    :invalidates-on
    [:core-node-change :binding-table-change
     :profile-target-change :schema-version-change
     :generic-bound-change :protocol-method-change
     :layout-policy-change]}
   'c7-constraint-ledger-contract
   {:artifact :gravity/c7-constraint-ledger-contract
    :constraint-fields
    [:constraint-id :kind :lhs :rhs :origin
     :profile :target :status :diagnostics]
    :constraint-kinds
    [:equality :subtype :numeric-mode :generic-bound
     :protocol-conformance :layout-availability
     :ownership-region :resource-lifetime :nullability
     :effect-capability-annotation]
    :statuses
    [:solved :deferred-to-profile-check
     :deferred-to-ownership-check :rejected]
    :determinism
    [:stable-ordering :local-inference-only
     :no-runtime-profile-discovery
     :no-backend-dependent-resolution]}
   'c7-dynamic-boundary-contract
   {:artifact :gravity/c7-dynamic-boundary-contract
    :boundary-fields
    [:boundary-id :core-node-id :source-span
     :input-type :output-type :check-strategy
     :profile :target :diagnostic]
    :check-strategies
    [:compile-time-proof :runtime-check
     :unsafe-island-required :rejected]
    :required-links
    [:typed-core-anchor :source-span
     :constraint-ledger-entry :diagnostic-origin
     :artifact-provenance]
    :forbidden
    [:implicit-any :silent-truncation
     :unchecked-foreign-boundary
     :dynamic-boundary-without-diagnostic-path]}})
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
        "Required SH-07-B47 coordinator adapter is absent"
        {:id "SH07-C7-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader
    (java.io.PushbackReader.
     (io/reader (path c7-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form)
          forms
          (recur (conj forms form)))))))

(def ^:private c7-plan
  (delay
    (let [source-path (path c7-relative-path)
          source-text (slurp source-path)
          emitter
          (:emitter
           (gravity.bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (gravity.bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(defn- plan-semantic-id
  [plan]
  (gravity.bootstrap/p15-s23-c11-mir-digest
   (gravity.bootstrap/p15-s23-stage2-compiler-artifact-semantic-input plan)))

(defn- function-shapes
  [plan]
  (into
   (sorted-map)
   (map
    (fn [[name function]]
      [name (select-keys function [:arity :params])]))
   (:functions plan)))

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
  (let [body (nth definition-form 3 nil)]
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
     :function-record-count (count (:function-records core-artifact))
     :call-edge-count (count (:call-edges core-artifact))
     :recursion-component-count
     (count (:recursion-components core-artifact))
     :reference-count (count (:reference-uses core-artifact))
     :keyword-lookup-count (count (:keyword-lookups core-artifact))
     :core-form-frequencies (frequencies (map :core-form nodes))}))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B47 records are not uniquely identifiable"
        {:id "SH07-C7-COVERAGE-AMBIGUOUS-INDEX"
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

(def ^:private c7-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c7-relative-path))))

(def ^:private c7-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c7-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c7-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path
          (.resolve temp-root "right/c7_type_checker_engine.qst")
          left-path (path c7-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c7-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b28-proof-contract-registers-c7-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path c7-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "SH-07-B47" (:coverage-milestone contract)))
    (is (= c7-relative-path
           (get-in contract [:authoritative-modules :c7-types])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c7-types])))
    (doseq [document
            ["docs/phase-01-core-language/015-l5-type-system-specification.md"
             "docs/phase-01-core-language/017-l7-pattern-matching-specification.md"
             "docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md"
             "docs/phase-01-core-language/019-l9-error-handling-specification.md"
             "docs/phase-01-core-language/020-l10-memory-model-specification.md"
             "docs/phase-02-safety/030-safe1-safe-gravity-semantics.md"
             "docs/phase-02-safety/031-safe2-memory-safety-model.md"
             "docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md"
             "docs/phase-02-safety/033-safe4-region-and-arena-safety.md"
             "docs/phase-02-safety/034-safe5-linear-resource-safety.md"
             "docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md"
             "docs/phase-02-safety/038-safe9-numeric-safety.md"
             "docs/phase-03-profile-system/046-p1-profile-system-specification.md"
             "docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md"
             "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"
             "docs/phase-06-compiler-architecture/086-c7-type-checker-design.md"
             "docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md"
             "docs/phase-06-compiler-architecture/088-c9-ownership-lifetime-and-region-checker-design.md"
             "docs/phase-06-compiler-architecture/089-c10-safety-analysis-pipeline-design.md"
             "docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md"
             "docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md"
             "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"
             "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
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
           (alength (source-bytes (path c7-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c7-relative-path)))))
    (doseq [source-fact
            [":stage :stage1"
             ":seed :clojure-stage0"
             ":retirement-objective :replace-clojure-seed"
             ":verified-by :clojure-stage0"
             ":compiled-by :clojure-stage0"
             ":authentication-status :coordinator-adapter-required"
             ":identity-resolution :coordinator-digest-required"
             ":effects #{}"
             ":capabilities #{}"
             ":core-form-outside-bounded-slice"]]
      (is (string/includes? source-text source-fact)))
    (is (every? (set (:nonclaims contract)) expected-nonclaims))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b47-c7-stage2-plan-identity-is-exact
  (let [plan @c7-plan
        functions (:functions plan)
        digest gravity.bootstrap/p15-s23-c11-mir-digest]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan)))
    (is (= expected-plan-semantic-id (plan-semantic-id plan)))
    (is (= expected-functions-semantic-id (digest functions)))
    (is (= 176 (count functions)))
    (is (= expected-function-names-id
           (digest (vec (sort (keys functions))))))
    (is (= expected-function-shapes-id
           (digest (function-shapes plan))))
    (doseq [[name expected-hash] expected-public-function-hashes]
      (is (contains? functions name))
      (is (= expected-hash (digest (get functions name)))))))

(deftest sh07-b28-c7-source-contracts-bounds-pending-and-limitations-are-exact
  (let [forms (source-forms)
        namespace-form (first forms)
        namespace-clauses
        (into {}
              (map (fn [clause] [(first clause) (second clause)]))
              (drop 2 namespace-form))
        bootstrap-metadata (get-in namespace-clauses [:metadata :bootstrap])
        definition-forms
        (into {}
              (for [form forms
                    :when (and (seq? form)
                               (#{'def 'defn} (first form)))]
                [(second form) form]))
        executable-names
        (set
         (for [[name form] definition-forms
               :when (and (= 'defn (first form))
                          (nil? (quoted-body form)))]
           name))
        type-core-form (get definition-forms 'sh08-type-core-artifact)
        pending
        (first
         (filter
          #(and (vector? %)
                (= :authenticated-coordinator-adapter (first %)))
          (tree-seq coll? seq type-core-form)))
        bounds-form (get definition-forms 'sh08-local-bounds-value)
        diagnostic-form (get definition-forms 'c7-type-diagnostic-catalog)
        keyword-vector-form (get definition-forms 'sh08-keyword-vector?)
        sha-form (get definition-forms 'sh08-sha256-id?)
        source-shape-form (get definition-forms 'sh08-source-shape?)
        node-shape-form (get definition-forms 'sh08-node-shape?)
        core-shape-form (get definition-forms 'sh08-validate-core-shape)
        type-node-form (get definition-forms 'sh08-type-node)
        verifier-form (get definition-forms 'verify-c7-type-checker)]
    (is (= 182 (count forms)))
    (is (= 'gravity.compiler.c7-type-checker-engine
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
            :component :type-checker
            :owner :gravity-source
            :source-language :gravity
            :seed :clojure-stage0
            :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys
            bootstrap-metadata
            [:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied])))
    (is (= #{:typed-core-module :type-environment :constraint-ledger
             :function-type-table :generic-instantiation-table
             :protocol-dispatch-type-table :dynamic-boundary-record
             :cast-conversion-record :layout-fact-record
             :schema-type-link :typed-core-verifier-report
             :type-diagnostic-catalog}
           (set (:implements bootstrap-metadata))))
    (is (= #{:source-spans :syntax-identity :diagnostic-codes
             :artifact-provenance :origin-chains :generated-origin
             :binding-identity :namespace-context
             :profile-target-metadata :effect-capability-metadata
             :core-node-identity :evaluation-order :dynamic-boundaries
             :schema-identity :layout-facts
             :ownership-resource-type-facts :latent-effects
             :thrown-error-effects}
           (:preserves bootstrap-metadata)))
    (is (= {:verified-by :clojure-stage0
            :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces
            [:clojure-c7-type-checker-contract-subset
             :clojure-c7-diagnostic-catalog]}
           (:lineage bootstrap-metadata)))
    (is (every? (set (keys definition-forms)) expected-definition-names))
    (is (= 181 (count definition-forms)))
    (is (= expected-definition-names-hash
           (gravity.bootstrap/p15-s23-c11-mir-digest
            (vec (sort (keys definition-forms))))))
    (is (= 5 (count (filter #(= 'def (first %))
                            (vals definition-forms)))))
    (is (= 176 (count (filter #(= 'defn (first %))
                              (vals definition-forms)))))
    (is (every? executable-names expected-executable-sh08-names))
    (is (= 173 (count executable-names)))
    (is (= expected-executable-names-hash
           (gravity.bootstrap/p15-s23-c11-mir-digest
            (vec (sort executable-names)))))
    (is (= 3 (count (filter quoted-body (vals definition-forms)))))
    (is (= expected-bounds (nth bounds-form 3)))
    (is (= expected-pending pending))
    (is (= expected-diagnostic-catalog (nth diagnostic-form 2)))
    (doseq [[name expected] expected-contract-structures]
      (is (= expected (nth (get definition-forms name) 2))))
    (is (= :gravity/c7-type-fact
           (:artifact
            (quoted-body (get definition-forms 'build-c7-type-fact)))))
    (is (= :gravity/c7-function-type
           (:artifact
            (quoted-body (get definition-forms 'build-c7-function-type)))))
    (is (= :stage1-source-owned
           (:status (quoted-body verifier-form))))
    (is (= 'c7-type-diagnostic-catalog
           (:diagnostics (quoted-body verifier-form))))
    (testing "documented structural limitations remain explicit"
      (is (not (contains? (symbols-in keyword-vector-form)
                          'sh08-all-unique?)))
      (is (contains? (symbols-in sha-form) 'sh08-lowercase-hex?))
      (is (not (contains? (symbols-in sha-form)
                          'authenticated-envelope-verify-template)))
      (is (not (contains? (symbols-in core-shape-form) 'sh08-exact-keys?)))
      (is (string/includes? (pr-str source-shape-form)
                            "(map? (get source :semantic-span))"))
      (is (string/includes? (pr-str node-shape-form)
                            "(map? (get node :attributes))"))
      (is (string/includes? (pr-str node-shape-form)
                            "(map? (get node :metadata))"))
      (is (not (string/includes? (pr-str type-node-form)
                                 ":gravity.type/bool")))
      (is (string/includes? (pr-str type-node-form)
                            ":core-form-outside-bounded-slice"))
      (is (string/includes? (pr-str type-core-form)
                            ":unauthenticated-sh07-shaped-artifact-id"))
      (is (string/includes? (pr-str type-core-form)
                            ":coordinator-digest-required")))))

(deftest sh07-b28-c7-source-has-exact-authentic-coverage
  (let [artifact @c7-artifact
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
    (is (= 'gravity.compiler.c7-type-checker-engine
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

(deftest sh07-b28-c7-calls-lookups-and-quoted-boundaries-are-exact
  (let [core-artifact (core @c7-artifact)
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
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= (set
            (map second
                 (filter #(and (seq? %)
                               (#{'def 'defn} (first %)))
                         (source-forms))))
           (set (map :name (:definitions core-artifact)))))
    (is (= 833 (count get-calls)))
    (is (= 774 (count literal-keyword-get-calls)))
    (is (= 59 (count dynamic-get-calls)))
    (is (= {:call 42 :reference 17}
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
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b28-c7-is-deterministic-path-neutral-and-provenanced
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

(deftest sh07-b28-c7-replay-and-alteration-containment-pass
  (let [artifact @c7-artifact
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
               "/altered/root/c7_type_checker_engine.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c7-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b28-existing-rejected-families-remain-paired-and-structured
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
