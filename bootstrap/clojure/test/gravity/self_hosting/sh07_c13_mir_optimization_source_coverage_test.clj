(ns gravity.self-hosting.sh07-c13-mir-optimization-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c13_mir_optimization_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 C13 source test is not on the classpath"
                      {:id "SH07-C13-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-C13-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c13-relative-path
  "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private accepted-fixture-relative-path
  "bootstrap/clojure/fixtures/self-hosting/sh-16/accepted/mir-optimizations.gravity")
(def ^:private expected-source-byte-count 98836)
(def ^:private expected-source-revision-id
  "sha256:071511017d4f3f3816716cfa899ae0189e882e6be9d67dc34e794729ae105681")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:3b42495073aac7f58957bafeaa8720e803df2203b69e499628e10755d93fdfb5")
(def ^:private expected-coverage
  {:fragment-count 114 :root-form-count 114 :form-count 8513
   :binding-count 740 :resolution-count 3140})
(def ^:private expected-core-census
  {:core-node-count 7036
   :definition-count 114
   :call-count 1285
   :reference-count 2466
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 2365 :collection-literal 246 :def 114 :reference 2466
    :call 1285 :if 396 :let 41 :loop 6 :recur 7 :quote 5 :fn 105}})
(def ^:private expected-census-measurements
  {:fragments 114 :top-level-forms 114 :forms 8513 :bindings 740
   :resolutions 3140 :maximum-fragment-forms 373
   :maximum-fragment-resolutions 135 :maximum-fragment-local-bindings 28
   :maximum-fragment-external-bindings 24 :maximum-fragment-root-forms 1
   :maximum-form-children 44 :maximum-form-depth 29
   :carrier-nodes 562128 :carrier-depth 31 :carrier-width 8513
   :carrier-exact-utf8-scalar-bytes 9490546
   :carrier-scalar-bytes 37945980
   :predicted-maximum-core-nodes 8513
   :predicted-maximum-digest-requests 8517})
(def ^:private expected-export-names
  '[c13-mir-optimization-contract c13-optimization-pass-contract
    c13-invalidation-ledger-contract c13-preservation-contract
    c13-proof-certificate-contract c13-safety-preservation-contract
    c13-nondeterminism-contract c13-domain-optimization-contract
    c13-optimization-diagnostic-catalog build-c13-pass-contract
    build-c13-decision-record build-c13-invalidation-entry
    build-c13-check-elision-gate c13-build-bounded-identity-optimized-mir
    verify-c13-mir-optimization-passes sh16-optimization-policy
    sh16-optimize sh16-verify])
(def ^:private data-definition-names
  '#{c13-mir-optimization-contract c13-optimization-pass-contract
     c13-invalidation-ledger-contract c13-preservation-contract
     c13-proof-certificate-contract c13-safety-preservation-contract
     c13-nondeterminism-contract c13-domain-optimization-contract
     c13-optimization-diagnostic-catalog})
(def ^:private quoted-definition-names
  '#{build-c13-pass-contract build-c13-decision-record
     build-c13-invalidation-entry build-c13-check-elision-gate
     verify-c13-mir-optimization-passes})
(def ^:private executable-definition-names
  '#{c13-lowercase-hex? c13-sha256-id? c13-content-binding-valid?
     c13-fact-bindings-valid? c13-fact-bindings-match-mir?
     c13-operation-ids c13-operation-order-from-blocks
     c13-bounded-operation-count? c13-bounded-operation-order
     c13-unique-operation-ids? c13-bounded-mir-operation-shape-valid?
     c13-bounded-identity-input-valid?
     c13-build-bounded-identity-optimized-mir sh16-optimization-policy
     sh16-member? sh16-keys-allowed-from? sh16-keys-allowed?
     sh16-exact-keys? sh16-aggregate? sh16-aggregate-components
     sh16-structural-scalar-valid? sh16-structural-preflight
     sh16-identifier-character? sh16-ascii-identifier-string?
     sh16-canonical-identifier? sh16-i64? sh16-bounded-scalar?
     sh16-bounded-operands-from? sh16-bounded-operands?
     sh16-keyword-vector-from? sh16-bounded-keyword-set?
     sh16-unique-operation-ids? sh16-unique-runtime-check-ids?
     sh16-source-span-valid? sh16-origin-entry-expected-keys
     sh16-origin-entry-valid? sh16-origin-chain-valid-from?
     sh16-origin-chain-valid? sh16-operation-expected-keys
     sh16-common-operation-valid? sh16-proof-claim
     sh16-bounds-condition-proven? sh16-proof-condition-valid?
     sh16-proof-valid? sh16-runtime-check-error sh16-result-id-used-from?
     sh16-dead-proof-valid? sh16-effect-safety-error sh16-operation-error
     sh16-operation-error-valid-shape sh16-operations-error
     sh16-request-error sh16-rule sh16-diagnostic sh16-foldable-add?
     sh16-simplifiable-branch? sh16-elidable-runtime-check?
     sh16-output-base-operation sh16-transform-operation
     sh16-retained-runtime-check sh16-family-changed?
     sh16-residual-checks sh16-normalized-span sh16-identity-origin-entry
     sh16-identity-origin-chain sh16-identity-proof
     sh16-identity-operation sh16-identity-operations sh16-identity-input
     sh16-append-unique-from sh16-append-unique
     sh16-proof-invalidated-by-state? sh16-operation-changed-in-state?
     sh16-state-changed-operation-ids sh16-operation-id-selected?
     sh16-apply-pass-from sh16-apply-pass sh16-proof-ids-for-selected
     sh16-all-proof-ids sh16-pass-analysis-invalidated
     sh16-pass-facts-invalidated sh16-pass-facts-regenerated
     sh16-pass-decision sh16-invalidation-entry sh16-pass-verifier-report
     sh16-run-pass sh16-sequential-pipeline
     sh16-post-pass-operation-valid? sh16-pass-output-operation-valid?
     sh16-pass-output-valid-from? sh16-post-pass-valid-from?
     sh16-pass-reports-passed-from? sh16-pipeline-valid?
     sh16-check-ids-for-selected sh16-elision-records-for-selected
     sh16-finalize-pipeline sh16-accepted-artifact
     sh16-structural-rejection sh16-optimize sh16-verify})
(def ^:private expected-definition-names
  (set/union data-definition-names quoted-definition-names
             executable-definition-names))
(def ^:private expected-contract-value-hashes
  {'c13-mir-optimization-contract
   "sha256:c6b11f050ebc7aed7599c33c16d1f5d74787ce57280cf12c33ab2f439d5ed9dd"
   'c13-optimization-pass-contract
   "sha256:52d92d9d45b0e383b31add01938fbe12c2e265ea3f40711f79bccfc6035eb589"
   'c13-invalidation-ledger-contract
   "sha256:ab843c6da384f0dab0485b49afe8f4e798b31a027c4db61712553c91b08f45d5"
   'c13-preservation-contract
   "sha256:c6cbc016d398dd139b0c1c41e43402fecd500953a5d040f47c0d14b8f4fb66a8"
   'c13-proof-certificate-contract
   "sha256:4e60f405a5da17b9910036a5e2381ba42dfb74480136670f14cd4d572b7f0fe7"
   'c13-safety-preservation-contract
   "sha256:2fdc0d7500fff81948c4adebc1c2f6c8a0ead5c2cad736239ee3d66fe9c201ca"
   'c13-nondeterminism-contract
   "sha256:9d6e85297f7312f3aab0c2da11250822b109c8c77533e62f947ae4f5b0471bcb"
   'c13-domain-optimization-contract
   "sha256:ee1eca7ebc439ebac412cf3f455b10d6f36bec94ebcd04bbced6acabde4cfb08"
   'c13-optimization-diagnostic-catalog
   "sha256:597f815e3528b85cc687c338dcb2071e9b9b206b052c855b75f27061328af35b"})
(def ^:private expected-policy
  {:artifact :gravity/sh16-optimization-policy
   :version 1
   :pass-order [:constant-fold :branch-simplify :dead-code-eliminate
                :check-retention-or-elision]
   :maximum-operations 128
   :integer-width-bits 64
   :minimum-i64 -9223372036854775808
   :maximum-i64 9223372036854775807
   :maximum-identifier-units 256
   :structural-bounds
   {:maximum-nodes 8192 :maximum-depth 96 :maximum-width 1024
    :maximum-collections 2048 :maximum-scalars 6144
    :maximum-scalar-units 65536}
   :supported-opcodes #{:const :add :branch-if :branch :compute
                        :runtime-check :return}
   :elidable-check-classes #{:bounds}
   :policy-check-classes
   #{:capability-effect :unsafe-audit :workflow-replay :ai-human-review}
   :safety-outcomes #{:proven-safe :runtime-checked :rejected :unsafe-island}
   :diagnostics #{"C13-CONTRACT" "C13-PRESERVE" "C13-PROOF"
                  "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY"
                  "C13-NONDETERMINISM" "C13-VERIFY"}
   :pending [:authenticated-sh15-input :complete-c11-mir-adapter
             :remaining-check-class-proof-replay
             :whole-function-translation-validation
             :target-lowering-proof-preservation
             :self-hosted-certificate-checker
             :guarded-operation-dominance-authentication
             :whole-c11-dead-code-analysis
             :complete-structured-pass-diagnostics]})
(def ^:private expected-diagnostic-catalog
  {:diagnostics ["C13-CONTRACT" "C13-PRESERVE" "C13-INVALIDATE"
                 "C13-PROOF" "C13-CHECK-ELISION" "C13-EFFECT" "C13-SAFETY"
                 "C13-DOMAIN" "C13-NONDETERMINISM" "C13-VERIFY"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :stage :pass-id :decision-id :input-mir-id
    :output-mir-id :function-id :block-id :operation-id :changed-operations
    :source-span :origin-chain :missing-fact :invalidated-fact :proof-id
    :certificate-id :runtime-check-id :domain :domain-anchor :profile
    :target :remediation]
   :rule-to-message-key
   {:C13-CONTRACT :optimization.invalid-pass-contract
    :C13-PRESERVE :optimization.preservation-gap
    :C13-INVALIDATE :optimization.invalidation-gap
    :C13-PROOF :optimization.proof-gap
    :C13-CHECK-ELISION :optimization.check-elision-gap
    :C13-EFFECT :optimization.effect-reorder
    :C13-SAFETY :optimization.stale-safety
    :C13-DOMAIN :optimization.domain-anchor-gap
    :C13-NONDETERMINISM :optimization.unreplayable-choice
    :C13-VERIFY :optimization.post-pass-verify}
   :stops [:target-lowering :runtime-selection :artifact-emission
           :package-release :self-hosting-comparison]
   :must-not-depend-on [:localized-rendering-text :backend-undefined-behavior
                        :host-runtime-accident :unordered-map-iteration
                        :unrecorded-benchmark-result]})
(def ^:private expected-b16-bounds
  {:maximum-module-forms 65536 :maximum-module-core-nodes 65536
   :maximum-fragments 1024 :maximum-top-level-forms 1024
   :maximum-fragment-forms 1024 :maximum-form-children 1024
   :maximum-form-depth 256 :maximum-bindings 2048
   :maximum-alias-records 256 :maximum-fragment-resolutions 2048
   :maximum-module-resolutions 65536 :maximum-origin-entries 256
   :maximum-metadata-entries 256 :maximum-effects 128
   :maximum-capabilities 128 :maximum-exports 1024
   :maximum-module-carrier-nodes 8388608
   :maximum-module-carrier-depth 256 :maximum-module-carrier-width 65536
   :maximum-module-scalar-bytes 268435456
   :maximum-template-carrier-nodes 16777216
   :maximum-template-carrier-depth 256 :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 1073741824
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 1073741824
   :maximum-generated-digest-carrier-nodes 16777216
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 1073741824
   :maximum-module-digest-requests 65540})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE" "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07-B33 adapter is absent"
                      {:id "SH07-C13-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))
(defn- source-forms []
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (io/reader (path c13-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))
(defn- named-top-level-form [forms kind name]
  (first (filter #(and (seq? %) (= kind (first %)) (= name (second %))) forms)))
(defn- quoted-body [definition-form]
  (let [body (nth definition-form 3 nil)]
    (when (and (seq? body) (= 'quote (first body))) (second body))))
(defn- collect-calls [operator value]
  (let [found (volatile! [])]
    (walk/postwalk (fn [entry]
                     (when (and (seq? entry) (= operator (first entry)))
                       (vswap! found conj entry))
                     entry)
                   value)
    @found))
(defn- symbols-in [value]
  (let [found (volatile! #{})]
    (walk/postwalk (fn [entry]
                     (when (symbol? entry) (vswap! found conj entry))
                     entry)
                   value)
    @found))
(defn- sha256-id [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))
(defn- value-sha256-id [value]
  (sha256-id (.getBytes (pr-str value) "UTF-8")))
(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))
(defn- request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))
(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))
(defn- identity-input [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))
(defn- coverage [artifact]
  (let [r (request artifact)]
    {:fragment-count (count (:fragment-manifest r))
     :root-form-count (count (:top-level-form-ids r))
     :form-count (count (:forms r))
     :binding-count (count (:binding-table r))
     :resolution-count (count (:resolution-table r))}))
(defn- core-census [artifact]
  (let [c (core artifact) nodes (:nodes c)]
    {:core-node-count (count nodes)
     :definition-count (count (:definitions c))
     :call-count (count (:calls c))
     :reference-count (count (:reference-uses c))
     :keyword-lookup-count (count (:keyword-lookups c))
     :core-form-frequencies (frequencies (map :core-form nodes))}))
(defn- exactly-once-index [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw (ex-info "B33 records are not uniquely identifiable"
                      {:id "SH07-C13-COVERAGE-AMBIGUOUS-INDEX"})))
    index))
(defn- diagnostic-result [operation]
  (try {:value (operation)}
       (catch clojure.lang.ExceptionInfo exception
         {:exception-data (ex-data exception)})
       (catch Throwable throwable
         {:raw-host-error {:class (.getName (class throwable))
                           :message (.getMessage throwable)}})))
(defn- diagnostic-data [result]
  (let [data (:exception-data result) value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact])) (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact])) (:diagnostic value)))))

(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b33-c13-source-coverage
    :compiler-artifact-plan? true}
   @plan function arguments))

(def ^:private engine-plan (delay (compile-plan c13-relative-path)))
(def ^:private accepted-plan (delay (compile-plan accepted-fixture-relative-path)))
(def ^:private c13-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path c13-relative-path))))
(def ^:private c13-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @c13-artifact))))
(def ^:private parity-artifacts
  (delay
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-c13-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c13_mir_optimization_passes.qst")
          left-path (path c13-relative-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path (source-bytes left-path)
                                   (make-array java.nio.file.OpenOption 0))
        {:left @c13-artifact
         :right ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path :right-path (str right-path)}
        (finally (delete-tree! temp-root))))))

(deftest sh07-b33-proof-contract-registers-c13-source-exactly
  (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))
        nonclaims (set (:nonclaims contract))]
    (is (= "SH-07-B33" (:coverage-milestone contract)))
    (is (= c13-relative-path
           (get-in contract [:authoritative-modules :c13-mir-optimization])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c13-mir-optimization])))
    (is (= expected-b16-bounds (:bounds contract)))
    (doseq [nonclaim
            [:c13-production-mir-optimization-execution
             :c13-complete-mir-pass-pipeline
             :sh16-authenticated-sh15-input :sh16-complete-c11-mir-adapter
             :sh16-whole-function-translation-validation
             :sh16-target-lowering-proof-preservation
             :sh16-self-hosted-certificate-checker
             :sh16-complete :sh07-complete :seed-retirement
             :self-hosting-complete]]
      (is (contains? nonclaims nonclaim)))))

(deftest sh07-b33-c13-source-contracts-policy-and-static-shape-are-exact
  (let [forms (source-forms) ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        metadata (get-in clauses [:metadata :bootstrap])
        definitions
        (into {} (for [name expected-definition-names]
                   [name (or (named-top-level-form forms 'def name)
                             (named-top-level-form forms 'defn name))]))
        executable (set (for [[name form] definitions
                              :when (and (= 'defn (first form))
                                         (nil? (quoted-body form)))] name))
        if-calls (mapcat #(collect-calls 'if %) (vals definitions))]
    (is (= 115 (count forms)))
    (is (= 'gravity.compiler.c13-mir-optimization-passes (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= {:stage :stage1 :component :mir-optimization
            :owner :gravity-source :source-language :gravity
            :seed :clojure-stage0 :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys metadata
                        [:stage :component :owner :source-language :seed
                         :retirement-objective :ambient-authority-denied])))
    (is (= expected-definition-names (set (keys definitions))))
    (is (= 9 (count (filter #(= 'def (first %)) (vals definitions)))))
    (is (= 105 (count (filter #(= 'defn (first %)) (vals definitions)))))
    (is (= quoted-definition-names
           (set (for [[name form] definitions :when (quoted-body form)] name))))
    (is (= executable-definition-names executable))
    (is (= 100 (count executable)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash (value-sha256-id (nth (get definitions name) 2)))))
    (is (= expected-diagnostic-catalog
           (nth (get definitions 'c13-optimization-diagnostic-catalog) 2)))
    (is (= expected-policy
           (nth (get definitions 'sh16-optimization-policy) 3)))
    (is (= 396 (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c13-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c13-relative-path)))))))

(deftest sh07-b33-c13-static-lookups-and-residual-boundaries-are-exact
  (let [forms (source-forms)
        definitions
        (into {} (for [name expected-definition-names]
                   [name (or (named-top-level-form forms 'def name)
                             (named-top-level-form forms 'defn name))]))
        gets (mapcat #(collect-calls 'get %) (vals definitions))
        literal (filter #(keyword? (nth % 2 nil)) gets)
        dynamic (sort-by #(-> % meta :line)
                         (remove #(keyword? (nth % 2 nil)) gets))
        preflight (get definitions 'sh16-structural-preflight)
        verifier (get definitions 'sh16-verify)
        identity (get definitions 'sh16-identity-input)
        sha-shape (get definitions 'c13-sha256-id?)]
    (is (= 493 (count gets)))
    (is (= 445 (count literal)))
    (is (= 48 (count dynamic)))
    (is (= {:reference 36 :call 4 :literal 8}
           (frequencies
            (map #(let [key-form (nth % 2)]
                    (cond (symbol? key-form) :reference
                          (seq? key-form) :call
                          :else :literal)) dynamic))))
    (is (= 667 (-> dynamic first meta :line)))
    (is (= 2287 (-> dynamic last meta :line)))
    (is (contains? (symbols-in preflight) 'sh16-aggregate-components))
    (is (contains? (symbols-in preflight) 'sh16-structural-scalar-valid?))
    (is (contains? (symbols-in verifier) 'sh16-structural-preflight))
    (is (contains? (symbols-in verifier) 'sh16-optimize))
    (is (contains? (symbols-in identity) 'sh16-normalized-span))
    (is (not (contains? (symbols-in sha-shape)
                        'authenticated-envelope-verify-template)))
    (is (= #{"C13-INVALIDATE" "C13-DOMAIN"}
           (set/difference
            (set (:diagnostics expected-diagnostic-catalog))
            (:diagnostics expected-policy))))))

(deftest sh07-b33-exported-sh16-policy-optimization-and-verification-execute
  (let [policy (invoke engine-plan 'sh16-optimization-policy [])
        request (invoke accepted-plan 'sh16-mixed-optimization-request [])
        result (invoke engine-plan 'sh16-optimize [request])
        verification (invoke engine-plan 'sh16-verify [request result])
        changed (assoc-in result [:optimized-operations 0 :operands] [999])
        changed-verification (invoke engine-plan 'sh16-verify [request changed])]
    (is (= expected-policy policy))
    (is (= :accepted (:status result)))
    (is (= [:op/add :op/branch :op/bounds-check :op/return]
           (mapv :op-id (:optimized-operations result))))
    (is (= [:constant-fold :branch-simplify :dead-code-eliminate
            :check-retention-or-elision]
           (mapv :pass-id (:decision-log result))))
    (is (= :passed (:status verification)))
    (is (= :rejected (:status changed-verification)))
    (is (= :optimization-result-substitution
           (get-in changed-verification [:diagnostics 0 :reason])))))

(deftest sh07-b33-c13-source-has-exact-authentic-coverage
  (let [artifact @c13-artifact r (request artifact) c (core artifact)
        coverage-record (:fragment-coverage c)
        fragments (:fragment-manifest r)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= 15 (:schema-version r)))
    (is (= 'gravity.compiler.c13-mir-optimization-passes
           (get-in r [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in r [:module :source-revision-id])
           (get-in r [:lineage :source-revision-id])))
    (is (= expected-sh06-semantic-projection-id
           (get-in r [:lineage :sh06-semantic-projection-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids r)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms r))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b33-c13-core-lookups-and-quotes-are-exact
  (let [c (core @c13-artifact) nodes (:nodes c)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id (exactly-once-index (:reference-uses c)
                                                 :core-node-id)
        gets (filterv #(= 'get (get-in reference-by-node-id
                                       [(:operator-node-id %) :symbol]))
                      (:calls c))
        literal (filterv #(keyword? (get-in node-by-id
                                            [(second (:argument-node-ids %))
                                             :attributes :value])) gets)
        dynamic (filterv #(not (keyword? (get-in node-by-id
                                                  [(second (:argument-node-ids %))
                                                   :attributes :value]))) gets)
        quotes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names (set (map :name (:definitions c)))))
    (is (= 493 (count gets)))
    (is (= 445 (count literal)))
    (is (= 48 (count dynamic)))
    (is (= {:reference 36 :call 4 :literal 8}
           (frequencies
            (map #(get-in node-by-id
                          [(second (:argument-node-ids %)) :core-form]) dynamic))))
    (is (empty? (:keyword-lookups c)))
    (is (= 5 (count quotes)))
    (doseq [node quotes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b33-c13-is-deterministic-path-neutral-and-provenanced
  (if (= "1" (System/getenv "GRAVITY_SH07_B33_PARITY"))
    (let [{:keys [left right left-path right-path]} @parity-artifacts]
      (is (= :accepted (:status left) (:status right)))
      (is (= (:artifact-id left) (:artifact-id right)))
      (is (= (identity-input left) (identity-input right)))
      (is (= (coverage left) (coverage right)))
      (is (= (core-census left) (core-census right)))
      (is (= (:fragment-manifest (request left))
             (:fragment-manifest (request right))))
      (is (= left-path (get-in left [:provenance :source-path])
             (get-in (core left) [:provenance :actual-source-path])))
      (is (= right-path (get-in right [:provenance :source-path])
             (get-in (core right) [:provenance :actual-source-path])))
      (is (not= left-path right-path)))
    (is true "Set GRAVITY_SH07_B33_PARITY=1 for isolated parity")))

(deftest sh07-b33-c13-replay-and-high-value-alterations-are-contained
  (let [artifact @c13-artifact c (core artifact)
        definition-index
        (first (keep-indexed
                (fn [index definition]
                  (when (= 'sh16-optimize (:name definition)) index))
                (:definitions c)))
        proof (:capability-based-proof artifact)
        changed-proof (assoc proof :status :failed)
        changed
        (assoc-in artifact
                  [:gravity-core-boundary :canonical-core-artifact
                   :definitions definition-index :binding-id] zero-id)
        checks ((required-var 'sh07-core-verification-checks)
                changed artifact @c13-upstream-verification)
        failed (set (for [[check passed?] checks
                          :when (not (true? passed?))] check))]
    (is (= :complete (:status proof)))
    (is (= [] (:failed-checks proof)))
    (is (= :failed (:status changed-proof)))
    (is (not= proof changed-proof))
    (is (contains? failed :canonical-core-replays?))))

(deftest sh07-b33-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                       basename "." extension))
            peer-path
            (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                       basename "." (if (= extension "gravity") "qst" "gravity")))
            result (diagnostic-result
                    #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path)) (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b33-c13-measured-carrier-fits-unchanged-b16-bounds
  (let [m expected-census-measurements b expected-b16-bounds]
    (is (< (:forms m) (:maximum-module-forms b)))
    (is (< (:predicted-maximum-core-nodes m) (:maximum-module-core-nodes b)))
    (is (< (:fragments m) (:maximum-fragments b)))
    (is (< (:bindings m) (:maximum-bindings b)))
    (is (< (:resolutions m) (:maximum-module-resolutions b)))
    (is (< (:carrier-nodes m) (:maximum-module-carrier-nodes b)))
    (is (< (:carrier-depth m) (:maximum-module-carrier-depth b)))
    (is (< (:carrier-width m) (:maximum-module-carrier-width b)))
    (is (< (:carrier-scalar-bytes m) (:maximum-module-scalar-bytes b)))
    (is (< (:predicted-maximum-digest-requests m)
           (:maximum-module-digest-requests b)))))
