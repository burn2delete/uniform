(ns gravity.self-hosting.sh07-b4-wasm-backend-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-proof-census :as proof-census]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_b4_wasm_backend_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 B4 source test is not on the classpath"
                      {:id "SH07-B4-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B4-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private b4-relative-path
  "bootstrap/gravity/src/gravity/backend/b4_wasm_backend_design.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 118572)
(def ^:private expected-source-revision-id
  "sha256:b0e2f2c1e8ff98541f4af07770eb54488234124b8ed03975ff8a5ebed2412d85")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:eec6df18b4b20898f79da0b11d13c688fef58a0fcbe0f4a524ec65203345b812")
(def ^:private expected-sh07-artifact-id
  "sha256:334fe22fbd4367dca63e53b768ff0ef1ed26ef8d8937f8b605471d3fe60da739")
(def ^:private expected-request-routing
  {:task "SH-07-B47"
   :scope :sh07-b15-keyword-map-lookup
   :adapter :gravity/sh07-to-c6-core-products-v16})
(def ^:private expected-request-lineage
  {:authenticated-sh06-artifact-id
   "sha256:9549673401779907d2db548c44cce9edb757a96bc4525fd0115726587eb18f11"
   :sh06-semantic-projection-id
   "sha256:eec6df18b4b20898f79da0b11d13c688fef58a0fcbe0f4a524ec65203345b812"
   :sh06-analysis-artifact-id
   "sha256:8b35614a98f513bc133bc26517bcbd6cadff71292057c012bc8de2f94e77806d"
   :source-revision-id
   "sha256:9efc6cc91b77c73f2adcb8db89e1a7e74494a8512dda52fd4b226bb10591d87a"
   :sh05-artifact-id
   "sha256:dbc9b362aebd29d5d6a6e7dab6eb17e6514603c84b480f07a041605477b08d6e"
   :expanded-syntax-stream-id
   "sha256:27bc7e98865737357cc6e87327f66b0d4c98165b1390aec82229095bb044add2"
   :macro-expansion-trace-id
   "sha256:6abc38acb17f19c6a7c5b30a4bb026cf6bcfbf516517e1e0ae24014a9e14615f"})
(def ^:private expected-coverage
  {:fragment-count 72
   :root-form-count 72
   :form-count 8189
   :binding-count 629
   :resolution-count 2900})
(def ^:private expected-core-census
  {:core-node-count 6830
   :definition-count 72
   :call-count 1280
   :reference-count 2347
   :keyword-lookup-count 0
   :core-form-frequencies
   {:let 37 :fn 61 :call 1280 :if 349 :reference 2347 :quote 34
    :collection-literal 327 :literal 2323 :def 72}})
(def ^:private expected-plan-semantic-id
  "sha256:67f28ecef53335b9e0a65bc8671c194bf3ea84620497ef10ac89f6d3973e7dac")
(def ^:private expected-functions-semantic-id
  "sha256:617ee850d511270ae86619261666ad418608d50e422e522f25a23f4635ea79d0")
(def ^:private expected-builder-semantic-id
  "sha256:f3e7c33fc18167ea5c3ae5da1aa8005cf7939feaaf60e9be5c488266cf1c16a9")
(def ^:private expected-export-names
  '[b4-wasm-backend-contract b4-target-feature-contract
    b4-component-contract b4-canonical-abi-contract
    b4-import-export-capability-contract b4-linear-memory-contract
    b4-async-component-contract b4-numeric-simd-atomic-contract
    b4-runtime-binding-contract b4-artifact-manifest-contract
    b4-diagnostic-catalog build-b4-wasm-backend-manifest
    build-b4-target-feature-record build-b4-component-contract-manifest
    build-b4-canonical-abi-manifest build-b4-import-capability-manifest
    build-b4-linear-memory-plan build-b4-async-abi-manifest
    build-b4-wasm-artifact-record verify-b4-wasm-backend-design
    b4-build-bounded-wasm32-core])
(def ^:private data-definition-names
  '#{b4-wasm-backend-contract b4-target-feature-contract
     b4-component-contract b4-canonical-abi-contract
     b4-import-export-capability-contract b4-linear-memory-contract
     b4-async-component-contract b4-numeric-simd-atomic-contract
     b4-runtime-binding-contract b4-artifact-manifest-contract
     b4-diagnostic-catalog})
(def ^:private required-executable-names
  '#{b4-build-bounded-wasm32-core b4-b1-packet-valid?
     b4-cfg-reason b4-operation-reason b4-first-operation-rejection
     b4-function-instructions b4-operation-bytes b4-evaluate-operations})
(def ^:private expected-contract-value-hashes
  {'b4-wasm-backend-contract
   "sha256:dd3af395dac7c8a2f34922f6efd70ee5fe0c3637d94ce8445c72b0a62b35ca22"
   'b4-target-feature-contract
   "sha256:f3d8e3ed1feea396c98f5490988e0ff15a9556b73b5411da2aece9f280311a25"
   'b4-component-contract
   "sha256:3be327f1094bac76c0feff9ad8c97fd493442e7ed897a78b8c492523037c90dc"
   'b4-canonical-abi-contract
   "sha256:cd14d74ee59a5194f0d7e62b7558b0a18b3ace136bb0fa473a1a8eb51282a40a"
   'b4-import-export-capability-contract
   "sha256:d2e975698940b141a7b45d665251814385b2a952c9ac9b7e070d0f85c9345734"
   'b4-linear-memory-contract
   "sha256:5bbd0ef3313adb0dd868437e57f04f07b551ea9c1088c484f9d6fc0335483b74"
   'b4-async-component-contract
   "sha256:b89c7805ae360752e5fce895408284f76bbf7e14c7cb1cd221ec270a4a1088ae"
   'b4-numeric-simd-atomic-contract
   "sha256:6941ef834b4dc65910e79abea638ba8c700a8626a9bc10e8b76c6ac5302baa3b"
   'b4-runtime-binding-contract
   "sha256:d46dcc3c8165a4079fa4997a600ee70964d52d42b638a4ddd476995e5ebb5c16"
   'b4-artifact-manifest-contract
   "sha256:852af638b4fd361829738f43b9a384e6492ea122ddb210f64511d6e1e69887bb"
   'b4-diagnostic-catalog
   "sha256:0c1444a54f58a3fc02886375d3f73f1924a603335d091db0a0057e512cf0e4eb"})
(def ^:private expected-diagnostic-ids
  ["B4-TARGET" "B4-COMPONENT" "B4-CANONICAL-ABI" "B4-IMPORT"
   "B4-EXPORT" "B4-MEMORY" "B4-BOUNDS" "B4-NONDETERMINISM"
   "B4-ASYNC" "B4-WASI-ASYNC" "B4-SIMD" "B4-ATOMIC"
   "B4-HOST-SCHEMA" "B4-MANIFEST"])
(def ^:private expected-census-measurements
  {:maximum-fragment-external-bindings 27
   :maximum-origin-chain-entries 0
   :maximum-binding-semantic-span-entries 2
   :fragments 72
   :carrier-depth 44
   :macro-origin-expected-outputs 61
   :maximum-form-children 54
   :resolutions 2900
   :carrier-exact-utf8-scalar-bytes 9056474
   :maximum-binding-target-set 1
   :predicted-maximum-core-nodes 8189
   :bindings 629
   :maximum-fragment-aliases 0
   :maximum-fragment-resolutions 318
   :carrier-nodes 561455
   :top-level-forms 72
   :macro-origin-expected-introduced-functions 61
   :maximum-alias-targets 0
   :maximum-form-depth 42
   :carrier-width 8189
   :maximum-fragment-root-forms 1
   :maximum-binding-profile-set 11
   :carrier-scalar-bytes 36208772
   :module-effects 0
   :macro-origin-expected-inputs 61
   :macro-origin-trace-entries 61
   :maximum-fragment-local-bindings 26
   :macro-origin-expanded-definitions 61
   :module-exports 21
   :module-capabilities 0
   :maximum-binding-capabilities 1
   :maximum-metadata-entries 0
   :macro-expansion-trace-entries 61
   :aliases 0
   :maximum-binding-effects 1
   :maximum-fragment-forms 894
   :predicted-maximum-digest-requests 8193
   :forms 8189
   :maximum-generated-origin-entries 1})
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
   :maximum-template-carrier-nodes 33554432
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
(def ^:private expected-b37-nonclaims
  #{:b4-complete-mir-and-domain-ir-surface
    :b4-production-backend-execution
    :b4-complete-component-model
    :b4-complete-canonical-abi
    :b4-complete-import-export-capability-surface
    :b4-complete-linear-memory-and-table-surface
    :b4-complete-wasi-async-surface
    :b4-complete-simd-atomic-feature-surface
    :b4-complete-runtime-binding-surface
    :b4-complete-artifact-emission-surface
    :b4-complete-external-target-matrix
    :b4-backend-conformance-complete
    :b4-public-gravity-routing
    :b4-complete
    :b4-release})
(def ^:private expected-global-nonclaims
  #{:sh07-complete :sh17-complete :seed-retirement
    :self-hosting-complete})
(def ^:private rejected-sh07-families
  {"core-shape" "C6-CORE-SHAPE" "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))
(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))
(defn- sha256-id [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))
(defn- value-sha256-id [value]
  (sha256-id (.getBytes (pr-str value) "UTF-8")))
(defn- source-forms []
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (io/reader (path b4-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))
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
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07-B37 adapter is absent"
                      {:id "SH07-B4-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path b4-relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :wasm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b37-b4-source-coverage
    :compiler-artifact-plan? true}
   @engine-plan function arguments))
(defn- request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))
(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))
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
                 (get-in value [:diagnostic :artifact])) (:diagnostic value))
        data)))
(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))
(defn- constant-wasm-source []
  (str "(ns checked.b4 (:profile :hosted) (:target :jvm) "
       "(:safety :safe) (:effects #{}) (:capabilities #{}) "
       "(:exports [main]))\n"
       "(defn main [] 42)\n"))
(defn- authentic-wasm []
  (let [source (constant-wasm-source)
        source-path "sh07-b37-b4-wasm.gravity"]
    (binding [bootstrap/*p15-s23-c11-mir-diagnostic-context*
              {:requested-target :wasm}
              bootstrap/*additional-bootstrap-targets*
              bootstrap/stage2-runtime-derived-source-targets]
      (let [context
            (bootstrap/p15-s23-stage2-gravity-checked-core-context
             source-path source :wasm)
            checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context)
            c11 (bootstrap/p15-s23-stage2-c11-mir-artifact
                 checked-core context)
            packet
            (bootstrap/p15-s23-stage2-c13-c14-b1-wasm-packet-from-c11!
             c11 checked-core context)
            gravity-lowering
            (invoke 'b4-build-bounded-wasm32-core [(:b1 packet)])
            artifact
            (bootstrap/p15-s23-stage2-b4-wasm-artifact-from-c11!
             c11 checked-core context)
            verification
            (bootstrap/p15-s23-stage2-b4-wasm-verification-report
             artifact checked-core context)]
        {:context context :checked-core checked-core :c11 c11
         :packet packet :gravity-lowering gravity-lowering
         :artifact artifact :verification verification}))))
(def ^:private b4-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path b4-relative-path))))
(def ^:private b4-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @b4-artifact))))

(deftest sh07-b37-b4-source-contracts-and-static-shape-are-exact
  (let [forms (source-forms) ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 73 (count forms)))
    (is (= 72 (count definitions) (count by-name)))
    (is (= 11 (count (filter #(= 'def (first %)) definitions))))
    (is (= 61 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.backend.b4-wasm-backend-design (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :wasm-backend (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? required-executable-names (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash (value-sha256-id (nth (get by-name name) 2)))))
    (is (= '[b1-packet]
           (nth (get by-name 'b4-build-bounded-wasm32-core) 2)))
    (is (= expected-diagnostic-ids
           (mapv :id (get-in (nth (get by-name 'b4-diagnostic-catalog) 2)
                             [:diagnostics]))))
    (is (= 349 (count if-calls)))
    (is (= {4 349} (frequencies (map count if-calls))))
    (is (empty?
         (filter #(not= 4 (count %))
                 (collect-calls
                  'if (get by-name 'b4-b1-static-contract-valid?)))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path b4-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path b4-relative-path)))))
    (is (= expected-source-byte-count
           bootstrap/p15-s23-b4-wasm-source-byte-count))
    (is (= expected-source-revision-id
           bootstrap/p15-s23-b4-wasm-expected-source-content-hash))
    (is (= expected-plan-semantic-id
           bootstrap/p15-s23-b4-wasm-expected-plan-semantic-hash))
    (is (= expected-functions-semantic-id
           bootstrap/p15-s23-b4-wasm-expected-functions-semantic-hash))
    (is (= expected-builder-semantic-id
           bootstrap/p15-s23-b4-wasm-expected-builder-semantic-hash))))

(deftest sh07-b37-b4-static-lookups-and-lowering-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        dynamic-kinds
        (frequencies (map #(if (symbol? (nth % 2 nil)) :reference :computed)
                          dynamic))
        builder (get by-name 'b4-build-bounded-wasm32-core)]
    (is (= 520 (count gets)))
    (is (= 471 (count (filter #(keyword? (nth % 2 nil)) gets))))
    (is (= 49 (count dynamic)))
    (is (= {:reference 11 :computed 38} dynamic-kinds))
    (is (contains? (symbols-in builder) 'b4-b1-packet-valid?))
    (is (contains? (symbols-in builder) 'b4-cfg-reason))
    (is (contains? (symbols-in builder) 'b4-function-instructions))
    (is (every? #(not (keyword? (first %)))
                (mapcat #(filter seq? (tree-seq coll? seq %))
                        (rest (source-forms)))))))

(deftest sh07-b37-b4-source-model-and-bounded-builder-execute
  (let [plan @engine-plan
        functions (:functions plan)
        required bootstrap/p15-s23-b4-wasm-required-functions
        observed-required
        (into (sorted-map)
              (map (fn [[name _]]
                     [name (select-keys (get functions name)
                                        [:arity :params])]))
              required)
        target (invoke 'build-b4-target-feature-record
                       [:core-module :wasm32 #{} :standalone])
        manifest (invoke 'build-b4-wasm-backend-manifest
                         [target target [:wasm-core-module]
                          [:verified-mir]])
        verification (invoke 'verify-b4-wasm-backend-design [manifest])
        rejected (invoke 'b4-build-bounded-wasm32-core [nil])]
    (is (= 61 (count functions)))
    (is (= required observed-required))
    (is (= :gravity/b4-wasm-target-feature-record (:artifact target)))
    (is (true? (:source-model-only? target)))
    (is (= :gravity/wasm-backend-manifest (:artifact manifest)))
    (is (true? (:source-model-only? manifest)))
    (is (= :gravity/b4-wasm-backend-verification
           (:artifact verification)))
    (is (true? (:source-model-only? verification)))
    (is (false? (:production-backend-execution? verification)))
    (is (= :rejected (:status rejected)))
    (is (= "B1-INPUT" (:diagnostic rejected)))
    (is (= :verified-bounded-wasm-b1-packet (:missing-fact rejected)))
    (is (nil? (:operation-id rejected)))))

(deftest sh07-b37-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_CONTRACT"))
    (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B37" (:coverage-milestone contract)))
      (is (= b4-relative-path (get-in contract [:authoritative-modules :b4-wasm])))
      (is (= {:keyword-lookups 0}
             (get-in contract [:required-core-product-counts :b4-wasm])))
      (is (every? (set (:nonclaims contract)) expected-b37-nonclaims))
      (is (every? (set (:nonclaims contract)) expected-global-nonclaims))
      (is (= expected-b16-bounds (:bounds contract))))
    (is true "Set GRAVITY_SH07_B37_CONTRACT=1 after coordinator registration")))

(deftest sh07-b37-b4-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_CENSUS"))
    (let [result (proof-census/census (path b4-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= expected-sh06-semantic-projection-id
             (get-in result [:request :sh06-semantic-projection-id])))
      (is (= expected-census-measurements measurements))
      (is (= (:forms measurements)
             (:predicted-maximum-core-nodes measurements)))
      (is (= (+ 4 (:predicted-maximum-core-nodes measurements))
             (:predicted-maximum-digest-requests measurements)))
      (is (<= (:forms measurements) (:maximum-module-forms expected-b16-bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes expected-b16-bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result))))
    (is true "Set GRAVITY_SH07_B37_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b37-b4-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_AUTHENTIC"))
    (let [artifact @b4-artifact r (request artifact) c (core artifact)
          coverage-record (:fragment-coverage c)
          fragments (:fragment-manifest r)
          census (core-census artifact)
          definition-index
          (first (keep-indexed
                  (fn [index definition]
                    (when (= 'b4-build-bounded-wasm32-core (:name definition))
                      index))
                  (:definitions c)))
          changed
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :definitions definition-index :binding-id] zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @b4-upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))] check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= expected-sh07-artifact-id (:artifact-id artifact)))
      (is (= 15 (:schema-version r)))
      (is (= expected-request-routing
             {:task (:task artifact)
              :scope (:scope r)
              :adapter (get-in artifact
                               [:gravity-core-boundary :adapter-contract])}))
      (is (= 'gravity.backend.b4-wasm-backend-design
             (get-in r [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in r [:module :source-revision-id])
             (get-in r [:lineage :source-revision-id])))
      (is (= expected-sh06-semantic-projection-id
             (get-in r [:lineage :sh06-semantic-projection-id])))
      (is (= expected-request-lineage
             (select-keys (:lineage r) (keys expected-request-lineage))))
      (is (= expected-coverage (coverage artifact)))
      (is (= expected-core-census census))
      (is (= (:core-node-count census)
             (reduce + (vals (:core-form-frequencies census)))))
      (is (= (:top-level-form-ids r)
             (:covered-root-form-ids coverage-record)
             (vec (mapcat :root-form-ids fragments))))
      (is (= (mapv :form-id (:forms r))
             (:covered-form-ids coverage-record)
             (vec (mapcat :form-ids fragments))))
      (is (false? (get-in artifact
                          [:gravity-core-boundary :target-source-reread?])))
      (is (= :complete (get-in artifact [:capability-based-proof :status])))
      (is (= [] (get-in artifact [:capability-based-proof :failed-checks])))
      (is (contains? failed :canonical-core-replays?)))
    (is true "Set GRAVITY_SH07_B37_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b37-existing-rejected-families-remain-paired-and-structured
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_AUTHENTIC"))
    (doseq [[basename expected-rule] rejected-sh07-families
            extension ["gravity" "qst"]]
      (testing (str basename "." extension)
        (let [source-path
              (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                         basename "." extension))
              peer-path
              (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                         basename "."
                         (if (= extension "gravity") "qst" "gravity")))
              result (diagnostic-result
                      #((required-var 'sh07-core-file-artifact) source-path))
              diagnostic (diagnostic-data result)]
          (is (= (vec (source-bytes source-path))
                 (vec (source-bytes peer-path))))
          (is (nil? (:raw-host-error result)))
          (is (= expected-rule (:rule diagnostic)))
          (is (= source-path (get-in diagnostic [:source-span :source])))
          (is (= true (get-in diagnostic [:facts :fail-closed]))))))
    (is true "Rejected fixture replay shares the isolated authentic gate")))

(deftest sh07-b37-b4-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b4-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/b4_wasm_backend_design.qst")
          left-path (path b4-relative-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path (source-bytes left-path)
                                   (make-array java.nio.file.OpenOption 0))
        (let [left @b4-artifact
              right ((required-var 'sh07-core-file-artifact) (str right-path))
              identity-input (required-var 'sh07-core-artifact-identity-input)]
          (is (= :accepted (:status left) (:status right)))
          (is (= expected-sh07-artifact-id
                 (:artifact-id left) (:artifact-id right)))
          (is (= expected-sh06-semantic-projection-id
                 (get-in (request left)
                         [:lineage :sh06-semantic-projection-id])
                 (get-in (request right)
                         [:lineage :sh06-semantic-projection-id])))
          (is (= (identity-input left) (identity-input right)))
          (is (= expected-coverage (coverage left) (coverage right)))
          (is (= expected-core-census
                 (core-census left) (core-census right)))
          (is (= left-path (get-in left [:provenance :source-path])))
          (is (= (str right-path) (get-in right [:provenance :source-path])))
          (is (not= left-path (str right-path))))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B37_PARITY=1 in an isolated 8 GiB JVM")))

(deftest sh07-b37-contextual-b4-verifier-and-tamper-boundary
  (if (= "1" (System/getenv "GRAVITY_SH07_B37_CONTEXTUAL"))
    (let [{:keys [artifact checked-core context verification packet
                  gravity-lowering]}
          (authentic-wasm)
          candidates
          [[:optimized-mir
            (update-in artifact
                       [:c13-c14-b1-packet :optimized-mir :module-id]
                       str "-altered")]
           [:lowering
            (update-in artifact [:lowering :expected-result] inc)]
           [:raw-wasm
            (update-in artifact [:raw-wasm :bytes 0]
                       #(bit-xor 1 %))]]
          expected-diagnostics
          {:optimized-mir
           ["B1-METADATA" :recomputable-c13-c14-b1-wasm-identities]
           :lowering
           ["B4-MANIFEST" :frozen-b4-envelope-self-consistency]
           :raw-wasm
           ["B13-HASH" :raw-wasm-content-hash]}
          before
          (bootstrap/p15-s23-b4-wasm-node-execution-snapshot)
          results
          (mapv
           (fn [[label candidate]]
             [label
              (diagnostic-result
               #(bootstrap/p15-s23-stage2-b4-wasm-verification-report
                 candidate checked-core context))])
           candidates)
          after
          (bootstrap/p15-s23-b4-wasm-node-execution-snapshot)]
      (is (= :gravity/p15-s23-b4-authenticated-wasm-artifact
             (:kind artifact)))
      (is (= :accepted-for-bounded-wasm (:status packet)))
      (is (= packet (:c13-c14-b1-packet artifact)))
      (is (= :gravity/b4-bounded-wasm32-core-lowering
             (:artifact gravity-lowering)))
      (is (= :constructed-unverified (:status gravity-lowering)))
      (is (= (dissoc gravity-lowering :wasm-bytes)
             (:lowering artifact)))
      (is (= (:wasm-bytes gravity-lowering)
             (get-in artifact [:raw-wasm :bytes])
             (get-in artifact [:independent-reconstruction :wasm-bytes])))
      (is (= :partial-bounded-executable-slice
             (get-in artifact [:b4-record :status])))
      (is (= :bounded-experimental-slice
             (get-in artifact [:b14-record :status])))
      (is (= :bounded-experimental-slice
             (get-in artifact [:c18-record :status])))
      (is (= :passed (get-in artifact [:independent-parser :status])))
      (is (= :passed (get-in artifact [:node-conformance :status])))
      (is (= :passed (get-in artifact [:c11-verification :status])))
      (is (= :passed (:status verification)))
      (is (= :passed (:gravity-b4-replay verification)))
      (is (= :passed (:independent-reconstruction verification)))
      (is (= :passed (:raw-module-verification verification)))
      (is (= :passed (:pinned-node-replay verification)))
      (doseq [[label result] results]
        (testing (name label)
          (let [diagnostic (diagnostic-data result)
                [expected-id expected-missing-fact]
                (get expected-diagnostics label)]
            (is (nil? (:raw-host-error result)))
            (is (= expected-id (:id diagnostic)))
            (is (= expected-missing-fact
                   (:missing-fact diagnostic))))))
      (is (= before after))
      (is (false? (get-in artifact [:scope :whole-b4?])))
      (is (false? (get-in artifact [:scope :public?])))
      (is (false? (get-in artifact [:scope :release?])))
      (is (false? (get-in artifact [:scope :component-model?])))
      (is (false? (get-in artifact [:scope :wit?])))
      (is (false? (get-in artifact [:scope :wasi?])))
      (is (false? (get-in artifact [:scope :self-hosted?])))
      (is (false? (get-in artifact [:scope :compile-to-any-target?]))))
    (is true "Set GRAVITY_SH07_B37_CONTEXTUAL=1 in an isolated JVM")))
