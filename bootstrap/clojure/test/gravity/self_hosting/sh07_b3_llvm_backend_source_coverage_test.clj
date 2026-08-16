(ns gravity.self-hosting.sh07-b3-llvm-backend-source-coverage-test
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
         "gravity/self_hosting/sh07_b3_llvm_backend_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 B3 source test is not on the classpath"
                      {:id "SH07-B3-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B3-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private b3-relative-path
  "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 91077)
(def ^:private expected-source-revision-id
  "sha256:3faf6a748485547abd6bd8da917a2214a7a8ba1cc9b3038bbbd3cdadd0562300")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:2190dea530d73d9abdff5b64df3eada3992c03583ab6498fc0c783b3f971a732")
(def ^:private expected-coverage
  {:fragment-count 83
   :root-form-count 83
   :form-count 7072
   :binding-count 692
   :resolution-count 2762})
(def ^:private expected-census-measurements
  {:maximum-fragment-external-bindings 23
   :maximum-origin-chain-entries 0
   :maximum-binding-semantic-span-entries 2
   :fragments 83
   :carrier-depth 27
   :macro-origin-expected-outputs 72
   :maximum-form-children 92
   :resolutions 2762
   :carrier-exact-utf8-scalar-bytes 8068604
   :maximum-binding-target-set 1
   :predicted-maximum-core-nodes 7072
   :bindings 692
   :maximum-fragment-aliases 0
   :maximum-fragment-resolutions 225
   :carrier-nodes 484843
   :top-level-forms 83
   :macro-origin-expected-introduced-functions 72
   :maximum-alias-targets 0
   :maximum-form-depth 25
   :carrier-width 7072
   :maximum-fragment-root-forms 1
   :maximum-binding-profile-set 11
   :carrier-scalar-bytes 32257096
   :module-effects 0
   :macro-origin-expected-inputs 72
   :macro-origin-trace-entries 72
   :maximum-fragment-local-bindings 21
   :macro-origin-expanded-definitions 72
   :module-exports 19
   :module-capabilities 0
   :maximum-binding-capabilities 1
   :maximum-metadata-entries 0
   :macro-expansion-trace-entries 72
   :aliases 0
   :maximum-binding-effects 1
   :maximum-fragment-forms 486
   :predicted-maximum-digest-requests 7076
   :forms 7072
   :maximum-generated-origin-entries 1})
(def ^:private expected-export-names
  '[b3-llvm-backend-contract b3-target-data-layout-contract
    b3-metadata-attribute-contract b3-safe-llvm-subset-contract
    b3-pointer-ownership-contract b3-numeric-floating-contract
    b3-atomic-volatile-contract b3-runtime-abi-contract
    b3-pass-pipeline-contract b3-artifact-manifest-contract
    b3-diagnostic-catalog build-b3-llvm-backend-manifest
    build-b3-target-record build-b3-metadata-map
    build-b3-runtime-abi-helper-map build-b3-pass-pipeline-record
    build-b3-llvm-artifact-record verify-b3-llvm-backend-design
    b3-bounded-llvm-x86_64-linux-policy-record
    b3-build-bounded-llvm-x86_64-linux
    b3-build-bounded-arm64-macos-llvm])
(def ^:private data-definition-names
  '#{b3-llvm-backend-contract b3-target-data-layout-contract
     b3-metadata-attribute-contract b3-safe-llvm-subset-contract
     b3-pointer-ownership-contract b3-numeric-floating-contract
     b3-atomic-volatile-contract b3-runtime-abi-contract
     b3-pass-pipeline-contract b3-artifact-manifest-contract
     b3-diagnostic-catalog})
(def ^:private required-executable-names
  '#{b3-bounded-arm64-macos-policy-record
     b3-bounded-llvm-x86_64-linux-policy-record
     b3-linux-target-fields-reason b3-linux-target-reason
     b3-target-rejected b3-module-envelope-reason
     b3-function-envelope-reason b3-operation-envelope-reason
     b3-cfg-structure-reason b3-data-flow-value-closure-reason
     b3-operation-unsupported-reason b3-build-emitted-lowering
     b3-build-validated-operations b3-build-validated-cfg
     b3-build-bounded-llvm-x86_64-linux
     b3-build-bounded-arm64-macos-llvm})
(def ^:private expected-contract-value-hashes
  {'b3-llvm-backend-contract
   "sha256:0dc8acd13be38d054205c7ce5e09a54f3a60a0ed99939a6f004d756192d40477"
   'b3-target-data-layout-contract
   "sha256:7303910a7043687987884e1d4dbeaf72215f04f03c2799c130849cf8c038b15b"
   'b3-metadata-attribute-contract
   "sha256:76e646f07943417617ded5f2128cbce20f15ddf7012ab0f68de091bf4cc1bfaf"
   'b3-safe-llvm-subset-contract
   "sha256:643ba9a135ae65ca3bb10626b9fc2d72f31bfbab2d370bb8aa9f3e9f937de72a"
   'b3-pointer-ownership-contract
   "sha256:046d4d026d10e94d17029e77c83cf438ef2c5082a677a053b026b3b4f7f786db"
   'b3-numeric-floating-contract
   "sha256:c1c170b694cf59dd3ab56d33393eab061e8815061f5466aa3d05cb0cba0f3591"
   'b3-atomic-volatile-contract
   "sha256:84af120bf85ab6aa37f0f6c1d2b70b2bba12e5076f96d1cf7cbabf796c2c46f5"
   'b3-runtime-abi-contract
   "sha256:471a23e82fb5a8788f1cc6f67cf5b39af6dd9c993d078ef61773b1175db9751a"
   'b3-pass-pipeline-contract
   "sha256:de749aed069291cc85ccff04f4e80178c729c9bebd4b0faa08a7b3c7396205d7"
   'b3-artifact-manifest-contract
   "sha256:39f71d576b65bd6b3d9f4149baa6a4b34076e02ac4f379afcd812c8699fb1e66"
   'b3-diagnostic-catalog
   "sha256:d398c62d4d2b4747a284f9e94c1d81d585dc775a792dfae1cd2fda3cef6a8c4f"})
(def ^:private expected-diagnostic-ids
  ["B3-TARGET" "B3-METADATA" "B3-UB" "B3-POINTER" "B3-NUMERIC"
   "B3-ATOMIC" "B3-RUNTIME" "B3-ABI" "B3-PASS" "B3-MANIFEST"])
(def ^:private expected-core-census
  {:core-node-count 5940
   :definition-count 83
   :call-count 1135
   :reference-count 2191
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 1839 :collection-literal 204 :def 83 :reference 2191
    :call 1135 :if 357 :let 56 :quote 3 :fn 72}})
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
(def ^:private expected-b36-nonclaims
  #{:b3-complete-mir-and-domain-ir-surface
    :b3-production-backend-execution
    :b3-object-or-executable-emission
    :b3-external-target-execution
    :b3-backend-conformance-complete
    :b3-public-gravity-routing
    :b3-release
    :b3-complete
    :sh07-complete
    :sh17-complete
    :seed-retirement
    :self-hosting-complete})
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
                      (io/reader (path b3-relative-path)))]
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
      (throw (ex-info "Required SH-07-B36 adapter is absent"
                      {:id "SH07-B3-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path b3-relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :llvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b36-b3-source-coverage
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
                 (get-in value [:diagnostic :artifact])) (:diagnostic value)))))
(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))
(defn- closed-pure-source []
  (str "(ns checked.b3 (:profile :hosted) (:target :jvm) "
       "(:safety :safe) (:effects #{}) (:capabilities #{}) "
       "(:exports [main]))\n"
       "(defn main [] (= 20 22))\n"))
(defn- authentic-llvm-lowering []
  (let [source (closed-pure-source)
        source-path "sh07-b36-b3-llvm.gravity"]
    (binding [bootstrap/*p15-s23-c11-mir-diagnostic-context*
              {:requested-target :llvm-x86_64-linux}
              bootstrap/*additional-bootstrap-targets*
              bootstrap/stage2-runtime-derived-source-targets]
      (let [context
            (bootstrap/p15-s23-stage2-gravity-checked-core-context
             source-path source :llvm-x86_64-linux)
            checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context)
            c11 (bootstrap/p15-s23-stage2-c11-mir-artifact
                 checked-core context)
            packet
            (bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!
             c11 checked-core context)
            mir (:optimized-mir packet)
            lowering (invoke 'b3-build-bounded-llvm-x86_64-linux [mir])
            artifact
            (bootstrap/p15-s23-stage2-b3-llvm-artifact-from-c11!
             c11 checked-core context)
            verification
            (bootstrap/p15-s23-stage2-b3-llvm-verification-report
             artifact checked-core context)]
        {:context context :checked-core checked-core :c11 c11
         :packet packet :mir mir :lowering lowering
         :artifact artifact :verification verification}))))
(def ^:private b3-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path b3-relative-path))))
(def ^:private b3-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @b3-artifact))))

(deftest sh07-b36-b3-source-contracts-and-static-shape-are-exact
  (let [forms (source-forms) ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 89 (count forms)))
    (is (= 88 (count definitions) (count by-name)))
    (is (= 11 (count (filter #(= 'def (first %)) definitions))))
    (is (= 77 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.backend.b3-llvm-backend-design (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= {:stage :stage1 :component :llvm-backend
            :owner :gravity-source :source-language :gravity
            :seed :clojure-stage0 :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys metadata
                        [:stage :component :owner :source-language :seed
                         :retirement-objective :ambient-authority-denied])))
    (is (= data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? required-executable-names (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash (value-sha256-id (nth (get by-name name) 2)))))
    (is (= '[mir]
           (nth (get by-name 'b3-build-bounded-llvm-x86_64-linux) 2)))
    (is (= '[mir]
           (nth (get by-name 'b3-build-bounded-arm64-macos-llvm) 2)))
    (is (= expected-diagnostic-ids
           (mapv :id (get-in (nth (get by-name 'b3-diagnostic-catalog) 2)
                             [:diagnostics]))))
    (is (= 363 (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= expected-source-byte-count
           (alength (source-bytes (path b3-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path b3-relative-path)))))))

(deftest sh07-b36-b3-static-lookups-and-lowering-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        builder (get by-name 'b3-build-bounded-llvm-x86_64-linux)
        emitted (get by-name 'b3-build-emitted-lowering)]
    (is (= 377 (count gets)))
    (is (= 310 (count (filter #(keyword? (nth % 2 nil)) gets))))
    (is (= 67 (count dynamic)))
    (is (= {:reference 23 :call 44}
           (frequencies (map #(if (symbol? (nth % 2)) :reference :call)
                             dynamic))))
    (is (contains? (symbols-in builder) 'b3-module-envelope-reason))
    (is (contains? (symbols-in builder) 'b3-function-envelope-reason))
    (is (contains? (symbols-in builder) 'b3-build-validated-cfg))
    (is (contains? (symbols-in emitted) 'b3-build-operation-records))
    (is (contains? (symbols-in emitted) 'b3-build-block-records))
    (is (contains? (symbols-in emitted)
                   'b3-bounded-llvm-x86_64-linux-policy-record))
    (is (contains? (symbols-in builder) 'b3-linux-target-reason))
    (is (contains? (symbols-in builder) 'b3-target-rejected))))

(deftest sh07-b36-b3-source-model-and-bounded-builder-execute
  (let [forms (source-forms)
        by-name (into {} (map (juxt second identity)
                              (filter #(and (seq? %)
                                            (#{'def 'defn} (first %)))
                                      forms)))
        policy (nth (get by-name
                         'b3-bounded-llvm-x86_64-linux-policy-record)
                    2)
        policy-text (pr-str policy)
        builder (get by-name 'b3-build-bounded-llvm-x86_64-linux)
        emitted (get by-name 'b3-build-emitted-lowering)]
    (is (re-find #":canonical-target :llvm-x86_64-linux" policy-text))
    (is (re-find #":target-triple \"x86_64-unknown-linux-gnu\""
                 policy-text))
    (is (re-find #":object-format :elf" policy-text))
    (is (re-find #":architecture :x86_64" policy-text))
    (is (re-find #":abi :sysv-amd64" policy-text))
    (is (re-find #":calling-convention :sysv-amd64" policy-text))
    (is (re-find #":unwind-strategy :dwarf-cfi" policy-text))
    (is (re-find #":linux/process-startup" policy-text))
    (is (re-find #":linux/elf-loader" policy-text))
    (is (re-find #":linux/glibc-2.36" policy-text))
    (is (re-find #":elf-x86_64-object" policy-text))
    (is (re-find #":elf-x86_64-executable" policy-text))
    (is (re-find #":clojure-seed-boundary\? true" policy-text))
    (is (re-find #":public\? false" policy-text))
    (is (re-find #":release\? false" policy-text))
    (is (re-find #":self-hosted\? false" policy-text))
    (is (re-find #":whole-b3\? false" policy-text))
    (is (contains? (symbols-in builder) 'b3-linux-target-reason))
    (is (contains? (symbols-in builder) 'b3-target-rejected))
    (is (re-find #":gravity/b3-llvm-x86_64-linux-elf-emission"
                 (pr-str emitted)))
    (is (re-find #":gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact"
                 (pr-str emitted)))))

(deftest sh07-b36-b3-claim-boundary-remains-explicit
  (let [forms (source-forms)
        by-name (into {} (map (juxt second identity) (rest forms)))
        contract (nth (get by-name 'b3-llvm-backend-contract) 2)
        policy (nth (get by-name
                         'b3-bounded-llvm-x86_64-linux-policy-record)
                    2)
        legacy (nth (get by-name
                         'b3-bounded-arm64-macos-policy-record)
                    2)
        builder (get by-name 'b3-build-bounded-llvm-x86_64-linux)]
    (is (= :not-claimed (:production-backend-status contract)))
    (is (= :source-model-bridge (:scope contract)))
    (is (contains? (set (:forbidden contract)) :source-to-llvm-shortcut))
    (is (contains? (set (:forbidden contract))
                   :llvm-undefined-behavior-as-optimization))
    (is (re-find #":clojure-seed-boundary\? true" (pr-str policy)))
    (is (re-find #":public\? false" (pr-str policy)))
    (is (re-find #":release\? false" (pr-str policy)))
    (is (re-find #":self-hosted\? false" (pr-str policy)))
    (is (re-find #":whole-b3\? false" (pr-str policy)))
    (is (re-find #":status :rejected" (pr-str legacy)))
    (is (re-find #":diagnostic \"B3-TARGET\"" (pr-str legacy)))
    (is (contains? (symbols-in builder) 'b3-linux-target-reason))
    (is (contains? (symbols-in builder) 'b3-target-rejected))))

(deftest sh07-b36-b3-linux-target-selection-is-source-owned-and-fail-closed
  (let [forms (source-forms)
        definitions (filter #(and (seq? %)
                                   (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity) definitions))
        target-reason (get by-name 'b3-linux-target-reason)
        target-rejection (get by-name 'b3-target-rejected)
        builder (get by-name 'b3-build-bounded-llvm-x86_64-linux)
        emitted (get by-name 'b3-build-emitted-lowering)
        legacy-builder (get by-name 'b3-build-bounded-arm64-macos-llvm)
        source-text (slurp (path b3-relative-path))]
    (testing "the source owns one canonical Linux target"
      (is (contains? (symbols-in target-reason) 'b3-linux-target-fields-reason))
      (is (re-find #":llvm-x86_64-linux" (pr-str target-reason)))
      (is (re-find #":requested-target" (pr-str target-reason)))
      (is (re-find #":source-target" (pr-str target-reason)))
      (is (re-find #":identity-bearing\?" (pr-str target-reason)))
      (is (re-find #":downstream-lowering-required\?" (pr-str target-reason)))
      (is (re-find #":x86_64-unknown-linux-gnu|x86_64" source-text))
      (is (re-find #":gravity/b3-bounded-llvm-x86_64-linux-lowering"
                   (pr-str emitted)))
      (is (re-find #":gravity/b3-llvm-x86_64-linux-elf-emission"
                   (pr-str emitted)))
      (is (re-find #":gravity/p15-s23-b3-authenticated-llvm-x86_64-linux-artifact"
                   (pr-str emitted))))
    (testing "hostile target spellings reject before emission"
      (doseq [hostile [":llvm" ":llvm-x86_64-darwin" ":llvm-arm64-darwin"
                       ":llvm-aarch64-linux" ":darwin" ":mach-o" ":arm64"
                       ":aarch64"]]
        (is (re-find (re-pattern (java.util.regex.Pattern/quote hostile))
                     source-text)
            (str "missing fail-closed hostile target literal " hostile)))
      (is (re-find #"B3-TARGET" (pr-str target-rejection)))
      (is (re-find #":emission-attempted\? false" (pr-str target-rejection)))
      (is (re-find #":fallback :reject-no-jvm-or-clojure-fallback"
                   (pr-str target-rejection)))
      (is (re-find #":canonical-linux-target-required" (pr-str target-reason)))
      (is (re-find #":exact-target-request-metadata-required"
                   (pr-str target-reason)))
      (is (contains? (symbols-in builder) 'b3-linux-target-reason))
      (is (contains? (symbols-in builder) 'b3-target-rejected))
      (is (contains? (symbols-in legacy-builder) 'b3-target-rejected))
      (is (not (contains? (symbols-in legacy-builder)
                          'b3-build-validated-cfg))))))

(deftest sh07-b36-authentic-bounded-llvm-lowering-executes
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_EXECUTION"))
    (let [{:keys [context checked-core packet mir lowering artifact
                  verification]}
          (authentic-llvm-lowering)
          altered-mir
          (update-in artifact
                     [:c13-c14-b1-packet :optimized-mir :module-id]
                     str "-altered")
          altered-lowering
          (update-in artifact [:lowering :llvm-ir] str "\n; altered\n")
          altered-mir-result
          (diagnostic-result
           #(bootstrap/p15-s23-stage2-b3-llvm-verification-report
             altered-mir checked-core context))
          altered-lowering-result
          (diagnostic-result
           #(bootstrap/p15-s23-stage2-b3-llvm-verification-report
             altered-lowering checked-core context))]
      (is (= :accepted-for-bounded-llvm (:status packet)))
      (is (= (:optimized-mir packet) mir))
      (is (= :constructed-unverified (:status lowering)))
      (is (= :gravity/b3-bounded-llvm-x86_64-linux-lowering
             (:artifact lowering)))
      (is (string? (:llvm-ir lowering)))
      (is (<= (count (:llvm-ir lowering)) 65536))
      (is (= [] (:diagnostics lowering)))
      (is (false? (:self-hosted? lowering)))
      (is (= :validated-candidate-for-bounded-internal-slice
             (:status artifact)))
      (is (= lowering (:lowering artifact)))
      (is (= :passed (:status verification)))
      (is (= :passed (:gravity-b3-replay verification)))
      (is (= :passed (:independent-lowering-reconstruction verification)))
      (doseq [[label candidate result]
              [[:altered-mir altered-mir altered-mir-result]
               [:altered-lowering altered-lowering altered-lowering-result]]]
        (testing (name label)
          (is (nil? (:raw-host-error result)))
          (is (= "B3-MANIFEST" (get-in result [:exception-data :id])))
          (is (= :content-bound-final-b3-artifact
                 (get-in result [:exception-data :missing-fact])))
          (is (false?
               (bootstrap/p15-s23-stage2-b3-llvm-authentic?
                candidate checked-core context))))))
    (is true "Set GRAVITY_SH07_B36_EXECUTION=1 in an isolated JVM")))

(deftest sh07-b36-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_CONTRACT"))
    (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B36" (:coverage-milestone contract)))
      (is (= b3-relative-path (get-in contract [:authoritative-modules :b3-llvm])))
      (is (= {:keyword-lookups 0}
             (get-in contract [:required-core-product-counts :b3-llvm])))
      (is (every? (set (:nonclaims contract)) expected-b36-nonclaims))
      (is (= expected-b16-bounds (:bounds contract))))
    (is true "Set GRAVITY_SH07_B36_CONTRACT=1 after coordinator registration")))

(deftest sh07-b36-b3-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_CENSUS"))
    (let [result (proof-census/census (path b3-relative-path))
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
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result))))
    (is true "Set GRAVITY_SH07_B36_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b36-b3-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_AUTHENTIC"))
    (let [artifact @b3-artifact r (request artifact) c (core artifact)
          coverage-record (:fragment-coverage c)
          fragments (:fragment-manifest r)
          definition-index
          (first (keep-indexed
                  (fn [index definition]
                    (when (= 'b3-build-bounded-llvm-x86_64-linux
                             (:name definition))
                      index))
                  (:definitions c)))
          changed
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :definitions definition-index :binding-id] zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @b3-upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))] check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 15 (:schema-version r)))
      (is (= 'gravity.backend.b3-llvm-backend-design
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
    (is true "Set GRAVITY_SH07_B36_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b36-existing-rejected-families-remain-paired-and-structured
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_AUTHENTIC"))
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

(deftest sh07-b36-b3-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B36_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b3-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/b3_llvm_backend_design.qst")
          left-path (path b3-relative-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path (source-bytes left-path)
                                   (make-array java.nio.file.OpenOption 0))
        (let [left @b3-artifact
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
    (is true "Set GRAVITY_SH07_B36_PARITY=1 in an isolated 8 GiB JVM")))
