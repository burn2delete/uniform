(ns gravity.self-hosting.sh07-b2-c-backend-source-coverage-test
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
         "gravity/self_hosting/sh07_b2_c_backend_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 B2 source test is not on the classpath"
                      {:id "SH07-B2-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B2-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private b2-relative-path
  "bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 122488)
(def ^:private expected-source-revision-id
  "sha256:8830a033bde79c4d728cb17d45ee2fead15ca00d8d86b16d7751b21820f47291")
(def ^:private expected-plan-semantic-id
  "sha256:07f6b7d32f8ef8620c08e629a65e6abf1ed1e0e0f62bfbfd4ff3d2e21e3108bd")
(def ^:private expected-functions-semantic-id
  "sha256:13775553d1144c6620351a18ead8a021083dd4d9e42e14f5218564dc1ad9f8c8")
(def ^:private expected-builder-semantic-id
  "sha256:1eb13380d0364d7cbf49c442e4b3ed571153b22fd25ac726d9eb30bccca032e8")
(def ^:private expected-export-names
  '[b2-c-backend-contract b2-dialect-contract b2-safe-c-subset-contract
    b2-abi-layout-contract b2-pointer-memory-contract
    b2-numeric-lowering-contract b2-runtime-helper-contract
    b2-artifact-manifest-contract b2-diagnostic-catalog
    build-b2-c-backend-manifest build-b2-dialect-selection
    build-b2-abi-layout-manifest build-b2-pointer-provenance-record
    build-b2-runtime-helper-manifest build-b2-c-artifact-record
    b2-build-bounded-hosted-c17 verify-b2-c-backend-design])
(def ^:private data-definition-names
  '#{b2-c-backend-contract b2-dialect-contract b2-safe-c-subset-contract
     b2-abi-layout-contract b2-pointer-memory-contract
     b2-numeric-lowering-contract b2-runtime-helper-contract
     b2-artifact-manifest-contract b2-diagnostic-catalog})
(def ^:private required-executable-names
  '#{b2-b1-c-packet-structurally-valid? b2-mir-validation
     b2-verifier-mir-cross-links-valid? b2-operation-reason
     b2-join-return-chain-valid? b2-c-build-manifest-record
     b2-abi-layout-record b2-source-debug-map-record
     b2-proof-to-c-assumption-record b2-build-accepted-hosted-c17
     b2-build-bounded-hosted-c17})
(def ^:private expected-contract-value-hashes
  {'b2-c-backend-contract
   "sha256:35e6263e91b47d58cccae0bd094111ab14bd58c0d799601ea2f04795b4cdc8e0"
   'b2-dialect-contract
   "sha256:570ce6798bfbdc0bd80338fbeafe5016e6fb54c056b059d6d8ac399cc0e98c9e"
   'b2-safe-c-subset-contract
   "sha256:e0b3e4ce9b6cc9209f28e658450c91c0c2a2fbc2c8334746df18f13f19d11d91"
   'b2-abi-layout-contract
   "sha256:4e8f342760f9fa2a11cac23c4671fa920ba9d1f0e50c83f7b2bfd1cd89026810"
   'b2-pointer-memory-contract
   "sha256:1df2c56879861183cc15b5bcfc26df8890eb25389ee466ded8394bca404e9e74"
   'b2-numeric-lowering-contract
   "sha256:51f38ab7af7ea71ff683cef57c8189df6185e97971cdc8306710ce2608c67a7f"
   'b2-runtime-helper-contract
   "sha256:a232c341f261d14db25b7fe40f139be038d87d1f6ffb7e18e9f254654a1f32dd"
   'b2-artifact-manifest-contract
   "sha256:287aba3278b164d74f0a4aff629fb299fdfd8d62d22130fb761f929063946c9d"
   'b2-diagnostic-catalog
   "sha256:0e472af4d19bb8b947c921019506f310599d5f1bc381364953f54ba36ec5f126"})
(def ^:private expected-diagnostic-ids
  ["B2-DIALECT" "B2-UB" "B2-ABI" "B2-POINTER" "B2-NUMERIC"
   "B2-RUNTIME" "B2-FFI" "B2-MMIO" "B2-MANIFEST"])
(def ^:private expected-b38-nonclaims
  #{:b2-complete-mir-and-domain-ir-surface
    :b2-complete-operation-and-profile-surface
    :b2-complete-c-dialect-target-and-abi-surface
    :b2-production-backend-authority
    :b2-complete-runtime-helper-and-ffi-surface
    :b2-complete-pointer-memory-and-mmio-surface
    :b2-complete-artifact-emission-surface
    :b2-complete-external-target-matrix
    :b2-backend-conformance-complete
    :b2-public-gravity-routing
    :b2-release
    :b2-complete})
(def ^:private expected-global-nonclaims
  #{:sh07-complete :sh17-complete :seed-retirement
    :self-hosting-complete})
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
(def ^:private rejected-sh07-families
  {"core-shape" "C6-CORE-SHAPE" "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))
(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))
(defn- sha256-id [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))
(defn- value-sha256-id [value]
  (sha256-id (.getBytes (pr-str value) "UTF-8")))
(defn- source-forms []
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (io/reader (path b2-relative-path)))]
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
      (throw (ex-info "Required SH-07-B38 adapter is absent"
                      {:id "SH07-B2-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path b2-relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :c))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b38-b2-source-coverage
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
(defn- write-bytes! [target bytes]
  (java.nio.file.Files/createDirectories
   (.getParent target)
   (make-array java.nio.file.attribute.FileAttribute 0))
  (java.nio.file.Files/write target bytes
                             (make-array java.nio.file.OpenOption 0))
  target)
(defn- closed-c-source []
  (str "(ns checked.b2 (:profile :hosted) (:target :jvm) "
       "(:safety :safe) (:effects #{}) (:capabilities #{}) "
       "(:exports [main]))\n"
       "(defn main [] (let [x 7] (if 0 (do x) 9)))\n"))
(defn- upstream [source-path source-text]
  (binding [bootstrap/*additional-bootstrap-targets*
            bootstrap/stage2-runtime-derived-source-targets]
    (let [context
          (bootstrap/p15-s23-stage2-gravity-checked-core-context
           source-path source-text :c)
          checked-core
          (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
           context)
          c11 (bootstrap/p15-s23-stage2-c11-mir-artifact
               checked-core context)]
      {:context context :checked-core checked-core :c11 c11})))
(def ^:private b2-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path b2-relative-path))))
(def ^:private b2-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @b2-artifact))))

(deftest sh07-b38-b2-source-contracts-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 130 (count forms)))
    (is (= 129 (count definitions) (count by-name)))
    (is (= 9 (count (filter #(= 'def (first %)) definitions))))
    (is (= 120 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.backend.b2-c-backend-design (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :c-backend (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? required-executable-names (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash (value-sha256-id (nth (get by-name name) 2)))))
    (is (= expected-diagnostic-ids
           (mapv :id (get-in (nth (get by-name 'b2-diagnostic-catalog) 2)
                             [:diagnostics]))))
    (is (= 441 (count if-calls)))
    (is (= {4 441} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path b2-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path b2-relative-path)))))
    (is (= expected-source-byte-count
           bootstrap/p15-s23-b2-c17-source-byte-count))
    (is (= expected-source-revision-id
           bootstrap/p15-s23-b2-c17-expected-source-content-hash))
    (is (= expected-plan-semantic-id
           bootstrap/p15-s23-b2-c17-expected-plan-semantic-hash))
    (is (= expected-functions-semantic-id
           bootstrap/p15-s23-b2-c17-expected-functions-semantic-hash))
    (is (= expected-builder-semantic-id
           bootstrap/p15-s23-b2-c17-expected-builder-semantic-hash))))

(deftest sh07-b38-b2-static-lookups-and-lowering-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        dynamic-kinds
        (frequencies (map #(if (symbol? (nth % 2 nil)) :reference :computed)
                          dynamic))
        builder (get by-name 'b2-build-bounded-hosted-c17)]
    (is (= 842 (count gets)))
    (is (= 792 (count (filter #(keyword? (nth % 2 nil)) gets))))
    (is (= 50 (count dynamic)))
    (is (= {:reference 21 :computed 29} dynamic-kinds))
    (doseq [symbol '[b2-b1-c-packet-structurally-valid?
                     b2-mir-validation
                     b2-verifier-mir-cross-links-valid?
                     b2-first-operation-rejection
                     b2-evaluate-operations
                     b2-build-accepted-hosted-c17]]
      (is (contains? (symbols-in builder) symbol) symbol))
    (is (every? #(not (keyword? (first %)))
                (mapcat #(filter seq? (tree-seq coll? seq %))
                        (rest (source-forms)))))))

(deftest sh07-b38-b2-source-model-and-bounded-builder-execute
  (let [plan @engine-plan
        functions (:functions plan)
        required bootstrap/p15-s23-b2-c17-required-functions
        observed-required
        (into (sorted-map)
              (map (fn [[name _]]
                     [name (select-keys (get functions name)
                                        [:arity :params])]))
              required)
        dialect (invoke 'build-b2-dialect-selection
                        [:hosted-c17 :apple-clang ["-std=c17"]
                         [:signed-i64-carrier]])
        manifest (invoke 'build-b2-c-backend-manifest
                         [[dialect] [:c-source :c-header]
                          [:verified-mir :source-map]])
        abi (invoke 'build-b2-abi-layout-manifest
                    ["arm64-apple-macosx14.0.0" :lp64 []])
        pointer (invoke 'build-b2-pointer-provenance-record
                        ["object-1" [0 8] :scope-1 :exclusive])
        runtime (invoke 'build-b2-runtime-helper-manifest
                        [:hosted []])
        artifact (invoke 'build-b2-c-artifact-record
                         [:c-source "program.c" expected-source-revision-id
                          "manifest-1" {:source "input.gravity"}])
        verification (invoke 'verify-b2-c-backend-design [manifest])
        rejected (invoke 'b2-build-bounded-hosted-c17 [nil])]
    (is (= 120 (count functions)))
    (is (= required observed-required))
    (is (= :gravity/b2-c-dialect-selection-record (:artifact dialect)))
    (is (= :gravity/c-backend-manifest (:artifact manifest)))
    (is (= :gravity/b2-abi-layout-manifest (:artifact abi)))
    (is (= :gravity/b2-pointer-provenance-record (:artifact pointer)))
    (is (= :gravity/b2-runtime-helper-manifest (:artifact runtime)))
    (is (= :gravity/b2-c-artifact-record (:artifact artifact)))
    (is (every? :source-model-only?
                [dialect manifest abi pointer runtime artifact verification]))
    (is (= :gravity/b2-c-backend-verification (:artifact verification)))
    (is (false? (:production-backend-execution? verification)))
    (is (= :rejected (:status rejected)))
    (is (= "B1-INPUT" (:diagnostic rejected)))
    (is (= :exact-sealed-b1-c-packet (:missing-fact rejected)))
    (is (nil? (:operation-id rejected)))))

(deftest sh07-b38-b2-claim-boundary-remains-explicit
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        contract (nth (get by-name 'b2-c-backend-contract) 2)
        policy (invoke 'b2-bounded-hosted-c17-policy-record [])]
    (is (= :not-claimed (:production-backend-status contract)))
    (is (= :source-model-bridge (:scope contract)))
    (is (= :partial-bounded-source-emission-slice
           (:backend-status policy)))
    (is (= :experimental (:tier policy)))
    (is (= :internal (:exposure policy)))
    (doseq [key [:whole-b2? :public? :release? :self-hosted?]]
      (is (false? (get policy key)) key))))

(deftest sh07-b38-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_CONTRACT"))
    (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B38" (:coverage-milestone contract)))
      (is (= 34 (count (:authoritative-modules contract))))
      (is (= 30 (count (:required-core-product-counts contract))))
      (is (set/subset? (set (keys (:required-core-product-counts contract)))
                       (set (keys (:authoritative-modules contract)))))
      (is (= b2-relative-path (get-in contract [:authoritative-modules :b2-c])))
      (is (= {:keyword-lookups 0}
             (get-in contract [:required-core-product-counts :b2-c])))
      (is (every? (set (:nonclaims contract)) expected-b38-nonclaims))
      (is (every? (set (:nonclaims contract)) expected-global-nonclaims))
      (is (= expected-b16-bounds (:bounds contract))))
    (is true "Set GRAVITY_SH07_B38_CONTRACT=1 after coordinator registration")))

(deftest sh07-b38-b2-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_CENSUS"))
    (let [result (proof-census/census (path b2-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 129 (:fragments measurements)
             (:top-level-forms measurements)))
      (is (= (:forms measurements)
             (:predicted-maximum-core-nodes measurements)))
      (is (= (+ 4 (:predicted-maximum-core-nodes measurements))
             (:predicted-maximum-digest-requests measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms expected-b16-bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes expected-b16-bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result))))
    (is true "Set GRAVITY_SH07_B38_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b38-b2-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_AUTHENTIC"))
    (let [artifact @b2-artifact
          r (request artifact)
          c (core artifact)
          coverage-record (:fragment-coverage c)
          fragments (:fragment-manifest r)
          census (core-census artifact)
          definition-index
          (first (keep-indexed
                  (fn [index definition]
                    (when (= 'b2-build-bounded-hosted-c17 (:name definition))
                      index))
                  (:definitions c)))
          changed
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :definitions definition-index :binding-id] zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @b2-upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))] check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 15 (:schema-version r)))
      (is (= 'gravity.backend.b2-c-backend-design
             (get-in r [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in r [:module :source-revision-id])
             (get-in r [:lineage :source-revision-id])))
      (is (= 129 (:fragment-count (coverage artifact))
             (:root-form-count (coverage artifact))
             (:definition-count census)))
      (is (pos-int? (:form-count (coverage artifact))))
      (is (pos-int? (:binding-count (coverage artifact))))
      (is (pos-int? (:resolution-count (coverage artifact))))
      (is (= 0 (:keyword-lookup-count census)))
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
    (is true "Set GRAVITY_SH07_B38_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b38-existing-rejected-families-remain-paired-and-structured
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_AUTHENTIC"))
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

(deftest sh07-b38-b2-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b2-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/b2_c_backend_design.qst")
          left-path (path b2-relative-path)]
      (try
        (write-bytes! right-path (source-bytes left-path))
        (let [left @b2-artifact
              right ((required-var 'sh07-core-file-artifact) (str right-path))
              identity-input (required-var 'sh07-core-artifact-identity-input)]
          (is (= :accepted (:status left) (:status right)))
          (is (= (:artifact-id left) (:artifact-id right)))
          (is (= (identity-input left) (identity-input right)))
          (is (= (coverage left) (coverage right)))
          (is (= (core-census left) (core-census right)))
          (is (= left-path (get-in left [:provenance :source-path])))
          (is (= (str right-path) (get-in right [:provenance :source-path])))
          (is (not= left-path (str right-path))))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B38_PARITY=1 in an isolated 8 GiB JVM")))

(deftest sh07-b38-b2-gate-a-is-contextual-path-neutral-and-tool-free
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_GATE_A"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b2-gate-a-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          source-text (closed-c-source)
          bytes (.getBytes source-text java.nio.charset.StandardCharsets/UTF_8)
          left-path (.resolve temp-root "left/program.gravity")
          right-path (.resolve temp-root "right/program.qst")]
      (try
        (write-bytes! left-path bytes)
        (write-bytes! right-path bytes)
        (let [left-path (str (.toRealPath left-path
                                         (make-array java.nio.file.LinkOption 0)))
              right-path (str (.toRealPath right-path
                                          (make-array java.nio.file.LinkOption 0)))
              left-upstream (upstream left-path source-text)
              before (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
              left (bootstrap/p15-s23-stage2-b2-c17-artifact-from-c11!
                    (:c11 left-upstream) (:checked-core left-upstream)
                    (:context left-upstream))
              report (bootstrap/p15-s23-stage2-b2-c17-verification-report
                      left (:checked-core left-upstream)
                      (:context left-upstream))
              right (bootstrap/p15-s23-stage2-b2-c17-source-artifact!
                     right-path source-text)
              after (bootstrap/p15-s23-b3-llvm-tool-execution-snapshot)
              altered (assoc-in left [:source-file :content]
                                "int main(void) { return 99; }\n")
              altered-result
              (diagnostic-result
               #(bootstrap/p15-s23-stage2-b2-c17-verification-report
                 altered (:checked-core left-upstream)
                 (:context left-upstream)))
              altered-data (diagnostic-data altered-result)]
          (is (= before after))
          (is (= :gravity/b2-bounded-hosted-c17 (:artifact left)))
          (is (= :constructed-unverified (:status left) (:status right)))
          (is (= :passed (:status report)))
          (is (= [:passed :passed :passed :passed :passed :passed]
                 ((juxt :fresh-c11 :fresh-c13 :fresh-c14 :fresh-b1
                        :gravity-b2-source-replay
                        :independent-c-reconstruction) report)))
          (is (= (:semantic-id left) (:semantic-id right)))
          (is (= (:artifact-id left) (:artifact-id right)))
          (is (not= (:actual-path-binding-id left)
                    (:actual-path-binding-id right)))
          (is (true? (bootstrap/p15-s23-stage2-b2-c17-authentic?
                      left (:checked-core left-upstream)
                      (:context left-upstream))))
          (is (false? (bootstrap/p15-s23-stage2-b2-c17-authentic? left)))
          (is (nil? (:raw-host-error altered-result)))
          (is (= "B13-HASH" (:id altered-data)))
          (is (= :recomputable-b2-c17-final-identities
                 (:missing-fact altered-data)))
          (doseq [key [:whole-b2? :public? :release? :self-hosted?]]
            (is (false? (get left key)) key)))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B38_GATE_A=1 in an isolated JVM")))

(deftest sh07-b38-b2-gate-b-emits-executes-and-replays
  (if (= "1" (System/getenv "GRAVITY_SH07_B38_GATE_B"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b2-gate-b-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          source-text (closed-c-source)
          source-file (.resolve temp-root "program.gravity")]
      (try
        (write-bytes! source-file
                      (.getBytes source-text
                                 java.nio.charset.StandardCharsets/UTF_8))
        (let [source-path
              (str (.toRealPath source-file
                                (make-array java.nio.file.LinkOption 0)))
              u (upstream source-path source-text)
              before
              (bootstrap/p15-s23-b2-c17-gate-b-tool-execution-snapshot)
              artifact
              (bootstrap/p15-s23-stage2-b2-c17-gate-b-artifact-from-c11!
               (:c11 u) (:checked-core u) (:context u))
              after-build
              (bootstrap/p15-s23-b2-c17-gate-b-tool-execution-snapshot)
              report
              (bootstrap/p15-s23-stage2-b2-c17-gate-b-verification-report
               artifact (:checked-core u) (:context u))
              after-replay
              (bootstrap/p15-s23-b2-c17-gate-b-tool-execution-snapshot)
              altered (assoc-in artifact
                                [:gate-a-artifact :source-file :content]
                                "int main(void) { return 99; }\n")
              before-altered
              (bootstrap/p15-s23-b2-c17-gate-b-tool-execution-snapshot)
              altered-result
              (diagnostic-result
               #(bootstrap/p15-s23-stage2-b2-c17-gate-b-verification-report
                 altered (:checked-core u) (:context u)))
              altered-data (diagnostic-data altered-result)
              after-altered
              (bootstrap/p15-s23-b2-c17-gate-b-tool-execution-snapshot)]
          (is (= 20 (- (:total after-build) (:total before))))
          (is (= 20 (- (:total after-replay) (:total after-build))))
          (is (= before-altered after-altered))
          (is (= :gravity/b2-hosted-c17-gate-b (:artifact artifact)))
          (is (= :validated-bounded-internal-c17-candidate
                 (:status artifact)))
          (is (= :passed (:status report)))
          (is (= :passed (:pinned-toolchain-replay report)))
          (is (= 20 (count (get-in artifact
                                   [:toolchain-evidence :tool-records]))))
          (doseq [record (get-in artifact [:toolchain-evidence :tool-records])]
            (is (= :gravity/b2-c17-bounded-tool-step (:artifact record)))
            (is (true? (:finished? record)))
            (is (false? (:timed-out? record)))
            (is (= {:kill-requested? false
                    :captured-process-set-reaped? :not-applicable
                    :whole-process-tree-reaping-proved? false
                    :root-alive-after-kill? false
                    :descendants-alive-after-kill 0}
                   (:termination record)))
            (is (false? (:stdout-truncated? record)))
            (is (false? (:stderr-truncated? record))))
          (is (= {:expected-exit-code 7 :observed-exit-code 7
                  :stdout-byte-count 0 :stderr-byte-count 0 :matched? true}
                 (get-in artifact
                         [:toolchain-evidence :process-evidence])))
          (is (= :passed-for-bounded-positive-slice
                 (get-in artifact [:b14-record :status])))
          (is (= :passed-for-experimental-bounded-slice
                 (get-in artifact [:c18-record :status])))
          (is (nil? (:raw-host-error altered-result)))
          (is (= "B13-HASH" (:id altered-data)))
          (is (= :recomputable-b2-c17-final-identities
                 (:missing-fact altered-data)))
          (doseq [key [:whole-b2? :public? :release? :self-hosted?]]
            (is (false? (get artifact key)) key)))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B38_GATE_B=1 in an isolated toolchain JVM")))
