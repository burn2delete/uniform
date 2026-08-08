(ns gravity.self-hosting.sh07-c14-target-lowering-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c14_target_lowering_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 C14 source test is not on the classpath"
                      {:id "SH07-C14-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-C14-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c14-relative-path
  "bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 168685)
(def ^:private expected-source-revision-id
  "sha256:931928313d0218aca740a906634ea1fa3a21058b546ce3f0ddc7329a059b566d")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:aee0b3c997a661ae9a08391a0468483b2459a2d814fe05e4f09a43ef90a1b70e")
(def ^:private expected-coverage
  {:fragment-count 143 :root-form-count 143 :form-count 15213
   :binding-count 936 :resolution-count 5693})
(def ^:private expected-core-census
  {:core-node-count 12683
   :definition-count 143
   :call-count 2531
   :reference-count 4488
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 4079 :collection-literal 380 :def 143 :reference 4488
    :call 2531 :if 776 :let 74 :loop 31 :recur 32 :quote 14 :fn 135}})
(def ^:private expected-census-measurements
  {:fragments 143 :top-level-forms 143 :forms 15213 :bindings 936
   :resolutions 5693 :maximum-fragment-forms 536
   :maximum-fragment-resolutions 157 :maximum-fragment-local-bindings 27
   :maximum-fragment-external-bindings 21 :maximum-fragment-root-forms 1
   :maximum-form-children 74 :maximum-form-depth 30
   :carrier-nodes 1015002 :carrier-depth 32 :carrier-width 15213
   :carrier-exact-utf8-scalar-bytes 16757402
   :carrier-scalar-bytes 67007600
   :predicted-maximum-core-nodes 15213
   :predicted-maximum-digest-requests 15217})
(def ^:private expected-export-names
  '[c14-target-lowering-contract c14-lowering-request-contract
    c14-target-eligibility-contract c14-abi-layout-contract
    c14-runtime-provider-contract c14-proof-metadata-contract
    c14-emitted-artifact-contract c14-lowering-diagnostic-catalog
    build-c14-lowering-request build-c14-target-artifact
    c14-build-bounded-llvm-lowering-record
    c14-build-bounded-c-lowering-record
    c14-build-bounded-wasm-lowering-record sh17-target-lowering-policy
    sh17-current-c11-revision sh17-structural-preflight
    sh17-build-target-lowering sh17-verify-target-lowering
    verify-c14-target-lowering])
(def ^:private data-definition-names
  '#{c14-target-lowering-contract c14-lowering-request-contract
     c14-target-eligibility-contract c14-abi-layout-contract
     c14-runtime-provider-contract c14-proof-metadata-contract
     c14-emitted-artifact-contract c14-lowering-diagnostic-catalog})
(def ^:private quoted-definition-names
  '#{build-c14-lowering-request build-c14-target-artifact
     verify-c14-target-lowering c14-current-c11-function-shapes})
(def ^:private target-builder-names
  '[c14-build-bounded-llvm-lowering-record
    c14-build-bounded-c-lowering-record
    c14-build-bounded-wasm-lowering-record])
(def ^:private required-executable-names
  '#{c14-build-bounded-llvm-lowering-record
     c14-build-bounded-c-lowering-record
     c14-build-bounded-wasm-lowering-record sh17-target-lowering-policy
     sh17-current-c11-revision sh17-structural-preflight
     sh17-build-target-lowering sh17-verify-target-lowering})
(def ^:private expected-contract-value-hashes
  {'c14-target-lowering-contract
   "sha256:42f15447da1fa68a6609d1586cdfdeb5dee0be251e9c74b251c716c9bb2962ed"
   'c14-lowering-request-contract
   "sha256:ecdf044a1783722574535e04bb749b4556265700818a141d6bc646e44b7f6e8c"
   'c14-target-eligibility-contract
   "sha256:9f4740fe510034c962dbea4a78642e7c59565c826fdc9228f584bd589a1b9f14"
   'c14-abi-layout-contract
   "sha256:fb7cee8db30cb88c244799b533ec14bd8b0f109a1fbd0ee39525c9ceb6edfbf6"
   'c14-runtime-provider-contract
   "sha256:a167061ccb2ba0235eec98d8b894ddeff0cd4247912aeb4e6bb7300e3e75ee86"
   'c14-proof-metadata-contract
   "sha256:f08971bc7a939a0b6628042af668e07f90132ef689d9189801ebf7937b0f21fc"
   'c14-emitted-artifact-contract
   "sha256:74f36eef3c1ffe02441c66ef304539fe6b7bee59994bd1ccd6e6d3f980dc6004"
   'c14-lowering-diagnostic-catalog
   "sha256:890d79857481171987a9b377099fa2b9fead18206bade47fb3461ef0be30d285"})
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
(def ^:private rejected-families
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
                      (io/reader (path c14-relative-path)))]
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
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07-B34 adapter is absent"
                      {:id "SH07-C14-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path c14-relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b34-c14-source-coverage
    :compiler-artifact-plan? true}
   @engine-plan function arguments))
(defn- digest [character]
  (str "sha256:" (apply str (repeat 64 character))))
(defn- target [backend]
  (case backend
    :llvm {:backend :llvm :triple "arm64-apple-macosx14.0.0"
           :architecture :arm64 :object-format :mach-o}
    :c {:backend :c :triple "arm64-apple-macosx14.0.0"
        :architecture :arm64 :object-format :c17-source}
    :wasm {:backend :wasm :triple "wasm32-unknown-unknown"
           :architecture :wasm32 :object-format :wasm-module}))
(defn- abi [backend]
  {:calling-convention (if (= backend :wasm) :wasm-core :c)
   :pointer-width (if (= backend :wasm) 32 64)
   :endianness :little :layout-id (digest "a")})
(defn- operation [operation-id opcode operands result-id source-id]
  {:operation-id operation-id :opcode opcode :operands operands
   :result-id result-id :type :gravity/integer :effects []
   :capabilities [] :source-id source-id})
(defn- source-entry [operation-id source-id checkout]
  {:operation-id operation-id :source-id source-id
   :span {:source-id source-id :start-byte 0 :end-byte 8
          :actual-source-path (str checkout "/program.gravity")}
   :origin-chain [{:generator-id (digest "b") :anchor-id (digest "c")
                   :actual-source-path (str checkout "/generated.gravity")}]})
(defn- sh17-request [backend checkout]
  (let [constant-source (digest "d") return-source (digest "e")
        operations
        [(operation :op/constant :constant [] :value/constant constant-source)
         (assoc (operation :op/return :return [:value/constant]
                           :value/return return-source)
                :effects [:io/write] :capabilities [:stdio/write])]]
    {:artifact :gravity/sh17-target-lowering-request :schema-version 1
     :backend backend :profile :hosted :target (target backend)
     :abi (abi backend) :runtime {:family :minimal-native :services []}
     :providers [{:effect :io/write :capability :stdio/write
                  :provider-id (digest "9")}]
     :effects [:io/write] :capabilities [:stdio/write]
     :c11-revision (invoke 'sh17-current-c11-revision [])
     :mir {:artifact :gravity/verified-target-independent-mir
           :schema-version 1 :verified? true :mir-id (digest "f")
           :entrypoint :function/main
           :functions [{:function-id :function/main :entry-block :block/entry
                        :return-type :gravity/integer
                        :blocks [{:block-id :block/entry
                                  :operations operations
                                  :terminator {:kind :return
                                               :value-id :value/constant}}]}]}
     :proofs [{:proof-id (digest "1") :operation-id :op/constant
               :claim :integer-constant-representable :status :verified}]
     :source-map [(source-entry :op/constant constant-source checkout)
                  (source-entry :op/return return-source checkout)]
     :actual-path-provenance
     [{:kind :actual-source-path :path (str checkout "/program.gravity")}] }))

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
                 (get-in value [:diagnostic :artifact])) (:diagnostic value)))))
(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))
(def ^:private c14-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path c14-relative-path))))
(def ^:private c14-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @c14-artifact))))

(deftest sh07-b34-c14-source-contracts-and-static-shape-are-exact
  (let [forms (source-forms) ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %)
                                  (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 144 (count forms)))
    (is (= 143 (count definitions) (count by-name)))
    (is (= 8 (count (filter #(= 'def (first %)) definitions))))
    (is (= 135 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.compiler.c14-target-lowering-architecture (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (= quoted-definition-names
           (set (for [form definitions :when (quoted-body form)] (second form)))))
    (is (set/subset? required-executable-names (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash (value-sha256-id (nth (get by-name name) 2)))))
    (doseq [name target-builder-names]
      (is (= 2 (count (nth (get by-name name) 2))) (str name)))
    (is (= 776 (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c14-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c14-relative-path)))))))

(deftest sh07-b34-c14-static-lookups-and-hardening-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        build (get by-name 'sh17-build-target-lowering)
        verify (get by-name 'sh17-verify-target-lowering)
        identity (get by-name 'sh17-identity-input)]
    (is (= 1131 (count gets)))
    (is (= 1032 (count (filter #(keyword? (nth % 2 nil)) gets))))
    (is (= 99 (count dynamic)))
    (is (= {:reference 45 :call 54}
           (frequencies (map #(if (symbol? (nth % 2)) :reference :call)
                             dynamic))))
    (is (contains? (symbols-in build) 'sh17-structural-preflight))
    (is (contains? (symbols-in build) 'sh17-request-valid?))
    (is (contains? (symbols-in build) 'sh17-references-valid?))
    (is (contains? (symbols-in verify) 'sh17-build-target-lowering))
    (is (contains? (symbols-in identity) 'sh17-neutral-source-map))))

(deftest sh07-b34-all-target-builders-and-sh17-routing-execute
  (let [policy (invoke 'sh17-target-lowering-policy [])]
    (is (= [:llvm :c :wasm] (:backends policy)))
    (is (= [:hosted] (:profiles policy)))
    (is (= 8192 (get-in policy [:structural-bounds :maximum-nodes])))
    (doseq [[function expected-artifact]
            [['c14-build-bounded-llvm-lowering-record
              :gravity/c14-bounded-llvm-lowering-record]
             ['c14-build-bounded-c-lowering-record
              :gravity/c14-bounded-c-lowering-record]
             ['c14-build-bounded-wasm-lowering-record
              :gravity/c14-bounded-wasm-lowering-record]]]
      (let [result (invoke function [nil nil])]
        (is (= :rejected (:status result)))
        (is (= expected-artifact (:artifact result)))
        (is (= "C14-INPUT" (:diagnostic result)))
        (is (= :verified-optimized-mir (:missing-fact result)))))
    (doseq [backend [:llvm :c :wasm]]
      (testing (name backend)
        (let [input (sh17-request backend "/checkout-a")
              result (invoke 'sh17-build-target-lowering [input])
              verification
              (invoke 'sh17-verify-target-lowering [input result])]
          (is (= :accepted (:status result)))
          (is (= backend (:backend result)))
          (is (= backend
                 (first (get-in result
                                [:target-program 0 :operations 0
                                 :target-opcode]))))
          (is (= :passed (:status verification)))
          (is (empty? (:diagnostics verification))))))))

(deftest sh07-b34-sh17-rejected-families-are-structured
  (let [base (sh17-request :llvm "/checkout-a")
        cases [[(assoc base :unexpected true) :invalid-normalized-request]
               [(assoc base :backend :unknown) :invalid-normalized-request]
               [(assoc-in base [:c11-revision :function-count] 138)
                :invalid-normalized-request]
               [(assoc-in base [:mir :verified?] false)
                :invalid-normalized-request]
               [(assoc-in base [:proofs 0 :status] :rejected)
                :invalid-normalized-request]]]
    (doseq [[candidate expected-reason] cases]
      (let [result (invoke 'sh17-build-target-lowering [candidate])]
        (is (= :rejected (:status result)))
        (is (= "C14-INPUT" (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (= expected-reason (get-in result [:diagnostics 0 :reason])))))
    (let [result (invoke 'sh17-build-target-lowering [base])
          changed (assoc result :backend :wasm)
          verification (invoke 'sh17-verify-target-lowering [base changed])]
      (is (= :rejected (:status verification)))
      (is (= :result-substitution
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh07-b34-c14-measured-carrier-fits-unchanged-b16-bounds
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

(deftest sh07-b34-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B34_CONTRACT"))
    (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B34" (:coverage-milestone contract)))
      (is (= c14-relative-path
             (get-in contract [:authoritative-modules :c14-target-lowering])))
      (is (= {:keyword-lookups 0}
             (get-in contract
                     [:required-core-product-counts :c14-target-lowering])))
      (is (= expected-b16-bounds (:bounds contract))))
    (is true "Set GRAVITY_SH07_B34_CONTRACT=1 after coordinator registration")))

(deftest sh07-b34-c14-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B34_AUTHENTIC"))
    (let [artifact @c14-artifact r (request artifact) c (core artifact)
          fragments (:fragment-manifest r)
          coverage-record (:fragment-coverage c)
          definition-index
          (first (keep-indexed
                  (fn [index definition]
                    (when (= 'sh17-build-target-lowering (:name definition))
                      index))
                  (:definitions c)))
          changed
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :definitions definition-index :binding-id] zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @c14-upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))] check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 15 (:schema-version r)))
      (is (= 'gravity.compiler.c14-target-lowering-architecture
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
                          [:gravity-core-boundary :target-source-reread?])))
      (is (= :complete (get-in artifact [:capability-based-proof :status])))
      (is (= [] (get-in artifact [:capability-based-proof :failed-checks])))
      (is (contains? failed :canonical-core-replays?)))
    (is true "Set GRAVITY_SH07_B34_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b34-existing-rejected-families-remain-paired-and-structured
  (if (= "1" (System/getenv "GRAVITY_SH07_B34_AUTHENTIC"))
    (doseq [[basename expected-rule] rejected-families
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
    (is true "Rejected SH-07 fixture replay shares the isolated authentic gate")))

(deftest sh07-b34-c14-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B34_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-c14-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root
                               "right/c14_target_lowering_architecture.qst")
          left-path (path c14-relative-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path (source-bytes left-path)
                                   (make-array java.nio.file.OpenOption 0))
        (let [left @c14-artifact
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
    (is true "Set GRAVITY_SH07_B34_PARITY=1 in an isolated 8 GiB JVM")))
