(ns gravity.self-hosting.sh07-c9-ownership-source-coverage-test
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
         "gravity/self_hosting/sh07_c9_ownership_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C9 ownership source test is not on the classpath"
        {:id "SH07-C9-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C9-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c9-relative-path
  "bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 35894)
(def ^:private expected-source-revision-id
  "sha256:b4fdf1022eb6eb25d091f1c918332c7b1393b6850acf4bb6988d8b8dbb2269e0")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:a0b95515faa899db463683ad98a37b0511def5dffa5964fd364cdc16547d3edc")
(def ^:private expected-coverage
  {:fragment-count 31
   :root-form-count 31
   :form-count 2320
   :binding-count 370
   :resolution-count 741})
(def ^:private expected-core-census
  {:core-node-count 1964
   :definition-count 31
   :call-count 288
   :reference-count 602
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 847
    :collection-literal 88
    :def 31
    :reference 602
    :call 288
    :if 76
    :let 5
    :quote 3
    :fn 24}})
(def ^:private expected-export-names
  '[c9-ownership-analysis-contract
    c9-borrow-graph-contract
    c9-lifetime-interval-contract
    c9-region-arena-contract
    c9-linear-resource-flow-contract
    c9-transfer-runtime-unsafe-contract
    c9-ownership-diagnostic-catalog
    build-c9-borrow-record
    build-c9-linear-resource-record
    verify-c9-ownership-checker
    sh10-ownership-policy
    sh10-check-ownership-request
    sh10-verify-ownership-result])
(def ^:private expected-definition-names
  '#{c9-ownership-analysis-contract
     c9-borrow-graph-contract
     c9-lifetime-interval-contract
     c9-region-arena-contract
     c9-linear-resource-flow-contract
     c9-transfer-runtime-unsafe-contract
     c9-ownership-diagnostic-catalog
     build-c9-borrow-record
     build-c9-linear-resource-record
     verify-c9-ownership-checker
     sh10-ownership-policy
     sh10-not
     sh10-and
     sh10-member?
     sh10-lowercase-hex?
     sh10-sha256-id?
     sh10-unique-event-ids?
     sh10-active-borrow?
     sh10-active-borrow-id?
     sh10-valid-request-shape?
     sh10-valid-event-shape?
     sh10-diagnostic
     sh10-rejected
     sh10-fact
     sh10-transition-accepted
     sh10-transition-rejected
     sh10-transition-initialized
     sh10-transition
     sh10-run-events
     sh10-check-ownership-request
     sh10-verify-ownership-result})
(def ^:private quoted-definition-names
  '#{build-c9-borrow-record
     build-c9-linear-resource-record
     verify-c9-ownership-checker})
(def ^:private expected-executable-sh10-names
  (set/difference
   expected-definition-names
   '#{c9-ownership-analysis-contract
      c9-borrow-graph-contract
      c9-lifetime-interval-contract
      c9-region-arena-contract
      c9-linear-resource-flow-contract
      c9-transfer-runtime-unsafe-contract
      c9-ownership-diagnostic-catalog
      build-c9-borrow-record
      build-c9-linear-resource-record
      verify-c9-ownership-checker}))
(def ^:private expected-document-ids
  ["C9" "L10" "SAFE3" "SAFE4" "SAFE5" "SAFE8"
   "C7" "C8" "C10" "C11" "C15" "P1" "P6" "P7"
   "R1" "R11" "PKG7" "BOOT1" "BOOT2" "BOOT3"
   "BOOT4" "BOOT5" "BOOT6" "BOOT7" "BOOT8" "TEST2"])
(def ^:private expected-policy
  {:artifact :gravity/sh10-ownership-policy
   :version 1
   :request-artifact :gravity/sh10-normalized-ownership-request
   :result-artifact :gravity/sh10-ownership-analysis-result
   :ownership-kind :owned-mutable
   :initialization-states #{:uninitialized :initialized}
   :availability-states #{:available :moved :consumed}
   :events #{:initialize :read :borrow-immutable :borrow-mutable
             :end-borrow :move :consume :escape-borrow}
   :escape-destinations #{:function-return}
   :diagnostics ["L10-UNINIT-READ"
                 "C9-USE-AFTER-MOVE"
                 "C9-USE-AFTER-CONSUME"
                 "C9-BORROW-ESCAPE"
                 "C9-MUT-ALIAS"
                 "C9-MOVE-WHILE-BORROWED"
                 "C9-UNSAFE"]
   :pending [:persistent-copy-semantics
             :field-and-range-splitting
             :region-lifetimes
             :arena-generations
             :linear-resources
             :task-actor-and-ffi-transfer
             :runtime-borrow-checks
             :unsafe-audit-records
             :authenticated-sh08-sh09-adapter
             :mir-preservation]})
(def ^:private expected-diagnostic-catalog
  {:diagnostics ["C9-USE-AFTER-MOVE"
                 "C9-USE-AFTER-CONSUME"
                 "C9-BORROW-ESCAPE"
                 "C9-MUT-ALIAS"
                 "C9-MOVE-WHILE-BORROWED"
                 "C9-REGION-ESCAPE"
                 "C9-ARENA-GENERATION"
                 "C9-LINEAR-LEAK"
                 "C9-LINEAR-DOUBLE"
                 "C9-TRANSFER"
                 "C9-RUNTIME-CHECK"
                 "C9-UNSAFE"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :value-id :owner-id :borrow-id
    :region-id :arena-generation :resource-id :control-path
    :source-span :generated-origin-chain :profile :target :remediation]
   :stops [:safety-analysis :mir-construction :optimizer
           :target-lowering :runtime-selection :package-release]
   :must-not-depend-on
   [:localized-rendering-text :host-runtime-undefined-behavior
    :backend-only-lifetime-state :implicit-unsafe-audit]})
(def ^:private expected-transition-reasons
  {"C9-BORROW-ESCAPE"
   #{:borrow-outlives-owner :escape-of-inactive-borrow}
   "C9-MOVE-WHILE-BORROWED" #{:move-during-active-borrow}
   "C9-MUT-ALIAS"
   #{:owner-read-during-active-mutable-borrow
     :multiple-active-mutable-borrows
     :immutable-borrow-during-active-mutable-borrow
     :mutable-borrow-with-active-immutable-aliases}
   "C9-UNSAFE"
   #{:move-without-destination-owner
     :immutable-borrow-without-identity
     :end-of-inactive-borrow
     :duplicate-active-borrow-identity
     :mutable-borrow-without-identity
     :consume-during-active-borrow
     :double-initialization
     :unsupported-ownership-event
     :malformed-ownership-event}
   "C9-USE-AFTER-CONSUME" #{:operation-after-consume}
   "C9-USE-AFTER-MOVE" #{:operation-after-move}
   "L10-UNINIT-READ" #{:operation-before-initialization}})
(def ^:private expected-contract-structures
  {'c9-ownership-analysis-contract
   {:artifact :gravity/c9-ownership-analysis-contract
    :required-fields
    [:module :owners :moves :consumes :borrows :lifetimes :regions
     :arenas :linear :transfers :runtime-checks
     :unsafe-audit-references :diagnostics]
    :owner-kinds
    [:persistent-immutable :owned-mutable :borrowed-immutable
     :borrowed-mutable :linear :region-owned :arena-owned
     :foreign-owned :provider-scoped]
    :must-compute
    [:ownership-graph :borrow-graph :lifetime-interval-map
     :move-and-consume-records :escape-analysis-report
     :region-lifetime-graph :arena-generation-graph
     :linear-resource-flow-graph :transfer-records
     :runtime-check-records :unsafe-audit-references]
    :must-preserve
    [:core-node-identity :source-spans :type-facts :effect-facts
     :capability-facts :artifact-provenance]
    :forbidden
    [:implicit-copy-of-owned-mutable-value
     :backend-only-ownership-legality
     :runtime-only-lifetime-legality :hidden-gc-region-fallback
     :linear-finalizer-only-cleanup
     :generated-code-dropping-ownership-facts]}
   'c9-borrow-graph-contract
   {:artifact :gravity/c9-borrow-graph-contract
    :node-kinds [:owner :borrow :reference :field :range :provider-scope]
    :edge-kinds
    [:immutable-borrow :mutable-borrow :field-projection
     :slice-or-range-borrow :move :consume :transfer
     :interior-mutability-provider-alias]
    :alias-rules
    [:many-immutable-or-one-mutable
     :mutable-borrow-exclusive-for-range
     :move-forbidden-during-active-borrow
     :consume-forbidden-during-active-borrow
     :field-or-range-splitting-only-when-proven
     :unknown-overlap-treated-as-aliasing]
    :required-fields
    [:borrow-id :owner-id :value-id :kind :range :lifetime
     :source-span :profile :target]
    :forbidden
    [:mutable-access-with-active-alias :move-while-borrowed
     :consume-while-borrowed :borrow-without-lifetime
     :borrow-fact-lost-before-mir]}
   'c9-lifetime-interval-contract
   {:artifact :gravity/c9-lifetime-interval-contract
    :interval-kinds
    [:lexical-scope :closure :stack-slot :owned-heap-value :region
     :arena-generation :foreign-call :callback :provider-scope
     :structured-task :device-buffer :generated-artifact]
    :required-fields
    [:lifetime-id :start :end :owner :allowed-escape-destinations
     :invalidation-conditions :source-span]
    :escape-destinations
    [:function-return :global-or-static-storage :closure-capture
     :task-or-actor-transfer :workflow-state :ffi-retention
     :callback-retention :ai-tool-model-request :generated-artifact
     :region-or-arena-storage]
    :legal-alternatives
    [:copy :serialization :ownership-transfer
     :promotion-to-longer-lived-owner :unsafe-boundary-with-audit]
    :forbidden
    [:borrow-outliving-owner :borrow-outliving-provider-scope
     :borrow-outliving-callback :detached-task-capturing-local-borrow
     :generated-artifact-capturing-short-lifetime]}
   'c9-region-arena-contract
   {:artifact :gravity/c9-region-arena-contract
    :required-region-fields
    [:region-id :scope :allocations :escapes :cleanup-policy
     :provider :source-span]
    :required-arena-fields
    [:arena-id :generation :allocation-sites :resets
     :invalidated-values :runtime-generation-checks :provider]
    :region-rules
    [:region-values-do-not-escape-scope
     :inner-region-values-not-stored-in-outer-storage
     :foreign-retention-requires-copy-or-transfer
     :structured-task-must-complete-before-region-exit
     :cleanup-runs-at-most-once]
    :arena-rules
    [:reset-creates-new-generation :prior-generation-values-invalidated
     :runtime-generation-checks-require-profile-support
     :arena-reset-rejects-outstanding-access
     :provider-declares-reset-and-threading]
    :forbidden
    [:return-region-reference :store-region-reference-globally
     :capture-region-value-in-longer-lived-closure
     :send-region-value-to-detached-task :ffi-retains-region-memory
     :use-after-arena-reset :hidden-gc-fallback]}
   'c9-linear-resource-flow-contract
   {:artifact :gravity/c9-linear-resource-flow-contract
    :operation-kinds
    [:acquire :borrow :transfer :close :commit :rollback :unlock
     :cancel :release :poison :forget]
    :path-kinds [:normal :error :panic :cancellation :early-return]
    :terminal-states
    [:closed :committed :rolled-back :unlocked :cancelled
     :released :transferred :poisoned]
    :required-fields
    [:resource-id :provider-id :acquire-span :current-owner
     :control-path :terminal-operation :profile
     :generated-origin-chain]
    :rules
    [:exactly-one-terminal-state-on-every-path
     :transfer-consumes-source-binding
     :destination-records-cleanup-obligation
     :error-panic-and-cancellation-paths-covered
     :generated-code-preserves-linear-flow]
    :forbidden
    [:missing-terminal-operation :duplicate-terminal-operation
     :use-after-terminal-state :cleanup-only-on-some-branches
     :release-through-wrong-provider :macro-duplicates-linear-value
     :best-effort-cleanup-safe-claim]}
   'c9-transfer-runtime-unsafe-contract
   {:artifact :gravity/c9-transfer-runtime-unsafe-contract
    :transfer-destinations
    [:function :task :actor :workflow :ffi :callback :tool-request
     :model-request :generated-artifact]
    :runtime-check-kinds
    [:dynamic-borrow-state :region-generation :arena-generation
     :provider-scope-validity :resource-terminal-state]
    :runtime-check-requirements
    [:profile-allows-metadata :profile-declares-failure-behavior
     :provider-supports-check :check-record-in-artifacts]
    :unsafe-kinds
    [:lifetime-extension :alias-recovery :manual-resource-flow
     :foreign-retention :raw-region-access]
    :required-unsafe-fields
    [:audit-id :reason :safe-boundary :owner :review :source-span
     :preconditions :postconditions]
    :forbidden
    [:implicit-ownership-transfer
     :runtime-check-without-profile-support
     :unsafe-lifetime-extension-without-audit
     :manual-resource-flow-without-audit
     :capability-treated-as-ownership-proof]}})
(def ^:private expected-catalog-only-diagnostics
  #{"C9-REGION-ESCAPE" "C9-ARENA-GENERATION"
    "C9-LINEAR-LEAK" "C9-LINEAR-DOUBLE"
    "C9-TRANSFER" "C9-RUNTIME-CHECK"})
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
        "Required SH-07-B30 coordinator adapter is absent"
        {:id "SH07-C9-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader (java.io.PushbackReader. (io/reader (path c9-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))

(defn- named-top-level-form
  [forms kind name]
  (first
   (filter #(and (seq? %) (= kind (first %)) (= name (second %)))
           forms)))

(defn- quoted-body
  [definition-form]
  (let [body (nth definition-form 3 nil)]
    (when (and (seq? body) (= 'quote (first body))) (second body))))

(defn- symbols-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (symbol? entry) (vswap! found conj entry))
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

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

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
        "SH-07-B30 records are not uniquely identifiable"
        {:id "SH07-C9-COVERAGE-AMBIGUOUS-INDEX"
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

(def ^:private c9-artifact
  ;; Delayed so namespace loading and static contract tests never start SH-07.
  (delay
    ((required-var 'sh07-core-file-artifact) (path c9-relative-path))))

(def ^:private c9-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c9-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c9-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path
          (.resolve temp-root "right/c9_ownership_checker_engine.qst")
          left-path (path c9-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c9-artifact
         :right ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally (delete-tree! temp-root))))))

(deftest sh07-b30-proof-contract-registers-c9-source-exactly
  (let [contract
        (edn/read-string (slurp (path proof-contract-relative-path)))
        nonclaims (set (:nonclaims contract))]
    (is (= "SH-07-B30" (:coverage-milestone contract)))
    (is (= c9-relative-path
           (get-in contract [:authoritative-modules :c9-ownership])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c9-ownership])))
    (is (= {:request-schema-version 15
            :task "SH-07-B15"
            :scope :sh07-b15-keyword-map-lookup
            :adapter :gravity/sh07-to-c6-core-products-v15
            :fresh-authoritative-process-required true
            :iteration-cache-authoritative false}
           (:boundary contract)))
    (doseq [nonclaim
            [:c9-production-ownership-checker-execution
             :c9-contract-and-diagnostic-schema-enforcement
             :sh10-authenticated-sh08-sh09-adapter
             :sh10-persistent-copy-and-field-range-splitting
             :sh10-regions-arenas-and-linear-resources
             :sh10-transfer-runtime-check-and-unsafe-audit
             :sh10-mir-preservation
             :sh10-complete
             :sh07-complete
             :seed-retirement
             :self-hosting-complete]]
      (is (contains? nonclaims nonclaim)))))

(deftest sh07-b30-c9-source-contracts-states-and-reasons-are-exact
  (let [forms (source-forms)
        namespace-form (first forms)
        namespace-clauses
        (into {} (map (fn [clause] [(first clause) (second clause)]))
              (drop 2 namespace-form))
        bootstrap-metadata (get-in namespace-clauses [:metadata :bootstrap])
        definitions
        (into {}
              (for [name expected-definition-names]
                [name (or (named-top-level-form forms 'def name)
                          (named-top-level-form forms 'defn name))]))
        executable
        (set
         (for [[name form] definitions
               :when (and (= 'defn (first form)) (nil? (quoted-body form)))]
           name))
        policy (nth (get definitions 'sh10-ownership-policy) 3)
        transition-calls
        (collect-calls
         'sh10-transition-rejected
         [(get definitions 'sh10-transition-initialized)
          (get definitions 'sh10-transition)])
        reason-map
        (into {}
              (map
               (fn [[rule calls]] [rule (set (map #(nth % 5) calls))]))
              (group-by #(nth % 4) transition-calls))
        request-rejection-calls
        (collect-calls
         'sh10-rejected
         (get definitions 'sh10-check-ownership-request))
        verification-diagnostic-calls
        (collect-calls
         'sh10-diagnostic
         (get definitions 'sh10-verify-ownership-result))
        all-if-calls
        (mapcat #(collect-calls 'if %) (vals definitions))]
    (is (= 32 (count forms)))
    (is (= 'gravity.compiler.c9-ownership-checker-engine
           (second namespace-form)))
    (is (= :meta (:profile namespace-clauses)))
    (is (= :jvm (:target namespace-clauses)))
    (is (= expected-export-names (:exports namespace-clauses)))
    (is (= #{} (:effects namespace-clauses)))
    (is (= #{} (:capabilities namespace-clauses)))
    (is (= :safe (:safety namespace-clauses)))
    (is (= expected-document-ids (:documents bootstrap-metadata)))
    (is (= {:stage :stage1
            :component :ownership-checker
            :owner :gravity-source
            :source-language :gravity
            :seed :clojure-stage0
            :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys
            bootstrap-metadata
            [:stage :component :owner :source-language :seed
             :retirement-objective :ambient-authority-denied])))
    (is (= #{:ownership-analysis :ownership-graph :borrow-graph
             :lifetime-interval-map :move-and-consume-records
             :escape-analysis-report :region-lifetime-graph
             :arena-generation-graph :linear-resource-flow-graph
             :transfer-records :runtime-check-records
             :unsafe-audit-references :ownership-diagnostic-catalog}
           (set (:implements bootstrap-metadata))))
    (is (= #{:source-spans :syntax-identity :diagnostic-codes
             :artifact-provenance :core-node-identity :type-facts
             :effect-facts :capability-facts :ownership-facts
             :borrow-graph :lifetime-intervals :move-records
             :consume-records :escape-analysis :region-lifetimes
             :arena-generations :linear-resource-flow :transfer-records
             :runtime-check-records :unsafe-audit-references
             :profile-target-metadata}
           (:preserves bootstrap-metadata)))
    (is (= {:verified-by :clojure-stage0
            :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces
            [:clojure-c9-ownership-checker-contract-subset
             :clojure-c9-diagnostic-catalog]}
           (:lineage bootstrap-metadata)))
    (is (= expected-definition-names (set (keys definitions))))
    (is (= 7 (count (filter #(= 'def (first %)) (vals definitions)))))
    (is (= 24 (count (filter #(= 'defn (first %)) (vals definitions)))))
    (is (= quoted-definition-names
           (set (for [[name form] definitions :when (quoted-body form)] name))))
    (is (= expected-executable-sh10-names executable))
    (is (= 21 (count executable)))
    (is (= expected-policy policy))
    (is (= expected-diagnostic-catalog
           (nth (get definitions 'c9-ownership-diagnostic-catalog) 2)))
    (doseq [[name contract] expected-contract-structures]
      (is (= contract (nth (get definitions name) 2))))
    (is (= expected-transition-reasons reason-map))
    (is (= 19 (count transition-calls)))
    (is (some
         #(and (= "C9-UNSAFE" (nth % 3))
               (= :malformed-normalized-ownership-request (nth % 4)))
         request-rejection-calls))
    (is (some
         #(and (= "C9-UNSAFE" (nth % 3))
               (= :ownership-result-substitution (nth % 4)))
         verification-diagnostic-calls))
    (is (contains?
         (symbols-in (get definitions 'sh10-verify-ownership-result))
         'sh10-check-ownership-request))
    (is (= 76 (count all-if-calls)))
    (is (every? #(= 4 (count %)) all-if-calls))
    (is (= 17 (count (collect-calls
                      'if (get definitions 'sh10-valid-request-shape?)))))
    (is (= 8 (count (collect-calls
                     'if (get definitions 'sh10-valid-event-shape?)))))
    (is (= :gravity/c9-borrow-record
           (:artifact (quoted-body (get definitions 'build-c9-borrow-record)))))
    (is (= :gravity/c9-linear-resource-record
           (:artifact
            (quoted-body (get definitions 'build-c9-linear-resource-record)))))
    (is (= :stage1-source-owned
           (:status (quoted-body (get definitions
                                      'verify-c9-ownership-checker)))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c9-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c9-relative-path)))))))

(deftest sh07-b30-c9-structural-limitations-remain-explicit
  (let [forms (source-forms)
        definition
        (fn [name] (or (named-top-level-form forms 'def name)
                       (named-top-level-form forms 'defn name)))
        request-shape (definition 'sh10-valid-request-shape?)
        event-shape (definition 'sh10-valid-event-shape?)
        sha-shape (definition 'sh10-sha256-id?)
        unique-events (definition 'sh10-unique-event-ids?)
        run-events (definition 'sh10-run-events)
        checker (definition 'sh10-check-ownership-request)
        verifier (definition 'sh10-verify-ownership-result)
        policy (nth (definition 'sh10-ownership-policy) 3)
        catalog (nth (definition 'c9-ownership-diagnostic-catalog) 2)]
    (testing "bounded request/event schemas remain shallow and non-exact"
      (is (not (contains? (symbols-in request-shape) 'sh10-exact-keys?)))
      (is (not (contains? (symbols-in event-shape) 'sh10-exact-keys?)))
      (is (string/includes? (pr-str request-shape) "1024"))
      (is (not (string/includes? (pr-str request-shape)
                                 ":capability-proof-id"))))
    (testing "digest and carrier validation remain seed-local"
      (is (not (contains? (symbols-in sha-shape)
                          'authenticated-envelope-verify-template)))
      (is (not (contains? (symbols-in checker) 'sh10-structural-preflight)))
      (is (not (string/includes? (pr-str checker) ":identity-cycle"))))
    (testing "event validation/execution and verification are recursive"
      (is (contains? (symbols-in unique-events) 'sh10-unique-event-ids?))
      (is (contains? (symbols-in run-events) 'sh10-run-events))
      (is (contains? (symbols-in verifier) 'sh10-check-ownership-request))
      (is (not (contains? (symbols-in verifier)
                          'independent-ownership-verifier))))
    (testing "the bounded policy does not claim catalog-only families"
      (is (= expected-catalog-only-diagnostics
             (set/difference
              (set (:diagnostics catalog))
              (set (:diagnostics policy)))))
      (is (contains? (set (:diagnostics policy)) "L10-UNINIT-READ"))
      (is (not (contains? (set (:diagnostics catalog))
                          "L10-UNINIT-READ"))))
    (is (= (:pending expected-policy) (:pending policy)))))

(deftest sh07-b30-c9-source-has-exact-authentic-coverage
  (let [artifact @c9-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B15" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup (:scope authenticated-request)))
    (is (= 'gravity.compiler.c9-ownership-checker-engine
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in authenticated-request [:module :source-revision-id])
           (get-in authenticated-request [:lineage :source-revision-id])))
    (is (= expected-sh06-semantic-projection-id
           (get-in authenticated-request
                   [:lineage :sh06-semantic-projection-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b30-c9-calls-lookups-and-quoted-boundaries-are-exact
  (let [core-artifact (core @c9-artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        get-calls
        (filterv
         #(= 'get (get-in reference-by-node-id
                          [(:operator-node-id %) :symbol]))
         (:calls core-artifact))
        literal-keyword-get-calls
        (filterv
         #(keyword? (get-in node-by-id
                            [(second (:argument-node-ids %))
                             :attributes :value]))
         get-calls)
        dynamic-get-calls
        (filterv
         #(not (keyword? (get-in node-by-id
                                 [(second (:argument-node-ids %))
                                  :attributes :value])))
         get-calls)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 134 (count get-calls)))
    (is (= 129 (count literal-keyword-get-calls)))
    (is (= 5 (count dynamic-get-calls)))
    (is (= (count get-calls)
           (+ (count literal-keyword-get-calls)
              (count dynamic-get-calls))))
    (is (= {:reference 3 :call 2}
           (frequencies
            (map #(get-in node-by-id
                          [(second (:argument-node-ids %)) :core-form])
                 dynamic-get-calls))))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero? (count (filter #(= :keyword-map-lookup (:core-form %))
                              nodes))))
    (is (= 3 (count quote-nodes)))
    (doseq [node quote-nodes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b30-c9-is-deterministic-path-neutral-and-provenanced
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

(deftest sh07-b30-c9-replay-and-high-value-alterations-are-contained
  (let [artifact @c9-artifact
        quote-node-index
        (first
         (keep-indexed
          (fn [index node]
            (when (= :quote (:core-form node)) index))
          (:nodes (core artifact))))
        report ((required-var 'sh07-core-artifact-verification) artifact)
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
               "/altered/root/c9_ownership_checker_engine.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status] :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c9-upstream-verification)
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b30-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families
          extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." extension))
            peer-extension (if (= "gravity" extension) "qst" "gravity")
            peer-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." peer-extension))
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path))
               (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))
