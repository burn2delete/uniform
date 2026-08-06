(ns gravity.self-hosting.sh07-b1-backend-interface-source-coverage-test
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
         "gravity/self_hosting/sh07_b1_backend_interface_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 B1 source test is not on the classpath"
                      {:id "SH07-B1-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B1-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private b1-relative-path
  "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 140646)
(def ^:private expected-source-revision-id
  "sha256:afd3eda966529c2193fb08c67d998286d7a289989a3ea4b91fb6c63e43fd4d6e")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:e2da5de22c35745e7143916404ddd19ddc832ddbc88bd7820a99fa7e55b95214")
(def ^:private expected-coverage
  {:fragment-count 70 :root-form-count 70 :form-count 10980
   :binding-count 530 :resolution-count 3692})
(def ^:private expected-export-names
  '[b1-backend-interface-contract b1-backend-input-contract
    b1-eligibility-contract b1-emission-contract
    b1-proof-metadata-contract b1-diagnostic-catalog
    build-b1-backend-manifest build-b1-input-packet
    build-b1-eligibility-report build-b1-emitted-artifact-record
    b1-build-bounded-llvm-authenticated-packet
    b1-build-bounded-c-authenticated-packet
    b1-build-bounded-wasm-authenticated-packet
    verify-b1-backend-interface])
(def ^:private data-definition-names
  '#{b1-backend-interface-contract b1-backend-input-contract
     b1-eligibility-contract b1-emission-contract
     b1-proof-metadata-contract b1-diagnostic-catalog})
(def ^:private packet-builder-names
  '[b1-build-bounded-llvm-authenticated-packet
    b1-build-bounded-c-authenticated-packet
    b1-build-bounded-wasm-authenticated-packet])
(def ^:private required-executable-names
  '#{b1-build-bounded-llvm-authenticated-packet
     b1-build-bounded-c-authenticated-packet
     b1-build-bounded-wasm-authenticated-packet
     b1-bounded-c14-input-valid? b1-bounded-c14-c-input-valid?
     b1-bounded-c14-wasm-input-valid? b1-c11-source-rule-valid?
     b1-c13-source-rule-valid? b1-c14-source-rule-valid?
     b1-c14-verifier-valid?})
(def ^:private expected-contract-value-hashes
  {'b1-backend-interface-contract
   "sha256:fbfa1e55088aeed8274b6001738675bc2ded9ddebb01264f7987bb7975ed2da5"
   'b1-backend-input-contract
   "sha256:8c38cc3ee37402da07b56fa55434ee0a4bc89de086684e6bbb33abf7d7254f69"
   'b1-eligibility-contract
   "sha256:25f8eccbcc1327a3a4e8d3fa5a419cf4d5487788bdd5b0fc8e48c49575d3f039"
   'b1-emission-contract
   "sha256:c0a3f46019c97d00c18c1f203f9f9a722ee48fdf9e29082fef4278a9f179f332"
   'b1-proof-metadata-contract
   "sha256:b097b05082a2030269913eef85d723b50f75575fb058afbd421b2355f7d305fb"
   'b1-diagnostic-catalog
   "sha256:b9668a9a583add75770107ed29e43160ecba362e2c7d4f192683b27dca98be88"})
(def ^:private expected-diagnostic-ids
  ["B1-INPUT" "B1-PROFILE" "B1-TARGET" "B1-ABI" "B1-RUNTIME"
   "B1-PROOF" "B1-CAPABILITY" "B1-UNSUPPORTED" "B1-METADATA"])
(def ^:private expected-core-census
  {:core-node-count 9440
   :definition-count 70
   :call-count 1819
   :reference-count 3039
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 3549 :collection-literal 380 :def 70 :reference 3039
    :call 1819 :if 475 :let 33 :quote 11 :fn 64}})
(def ^:private expected-census-measurements
  {:maximum-fragment-external-bindings 19
   :maximum-origin-chain-entries 0
   :maximum-binding-semantic-span-entries 2
   :fragments 70
   :carrier-depth 29
   :macro-origin-expected-outputs 64
   :maximum-form-children 66
   :resolutions 3692
   :carrier-exact-utf8-scalar-bytes 11727269
   :maximum-binding-target-set 1
   :predicted-maximum-core-nodes 10980
   :bindings 530
   :maximum-fragment-aliases 0
   :maximum-fragment-resolutions 189
   :carrier-nodes 721001
   :top-level-forms 70
   :macro-origin-expected-introduced-functions 64
   :maximum-alias-targets 0
   :maximum-form-depth 27
   :carrier-width 10980
   :maximum-fragment-root-forms 1
   :maximum-binding-profile-set 11
   :carrier-scalar-bytes 46892996
   :module-effects 0
   :macro-origin-expected-inputs 64
   :macro-origin-trace-entries 64
   :maximum-fragment-local-bindings 19
   :macro-origin-expanded-definitions 64
   :module-exports 14
   :module-capabilities 0
   :maximum-binding-capabilities 1
   :maximum-metadata-entries 0
   :macro-expansion-trace-entries 64
   :aliases 0
   :maximum-binding-effects 1
   :maximum-fragment-forms 451
   :predicted-maximum-digest-requests 10984
   :forms 10980
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
(def ^:private rejected-sh07-families
  {"core-shape" "C6-CORE-SHAPE" "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private expected-nonclaims
  #{:complete-b1-mir-and-domain-ir-surface
    :b1-production-backend-execution
    :b1-object-or-executable-emission
    :b1-external-target-execution
    :b1-backend-conformance-complete
    :sh07-complete :sh17-complete :seed-retirement
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
                      (io/reader (path b1-relative-path)))]
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
      (throw (ex-info "Required SH-07-B35 adapter is absent"
                      {:id "SH07-B1-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path b1-relative-path)
        source-text (slurp source-path)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b35-b1-source-coverage
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
  (str "(ns checked.b1 (:profile :hosted) (:target :jvm) "
       "(:safety :safe) (:effects #{}) (:capabilities #{}) "
       "(:exports [main]))\n"
       "(defn main [] 0)\n"))
(defn- authentic-b1-packet [backend]
  (let [source (closed-pure-source)
        source-path (str "sh07-b35-b1-" (name backend) ".gravity")]
    (binding [bootstrap/*p15-s23-c11-mir-diagnostic-context*
              {:requested-target backend}
              bootstrap/*additional-bootstrap-targets*
              bootstrap/stage2-runtime-derived-source-targets]
      (let [context
            (bootstrap/p15-s23-stage2-gravity-checked-core-context
             source-path source backend)
            checked-core
            (bootstrap/p15-s23-stage2-gravity-checked-core-source-artifact
             context)
            c11 (bootstrap/p15-s23-stage2-c11-mir-artifact
                 checked-core context)
            packet
            (case backend
              :llvm
              (bootstrap/p15-s23-stage2-c13-c14-b1-packet-from-c11!
               c11 checked-core context)
              :c
              (bootstrap/p15-s23-stage2-c13-c14-b1-c-packet-from-c11!
               c11 checked-core context)
              :wasm
              (bootstrap/p15-s23-stage2-c13-c14-b1-wasm-packet-from-c11!
               c11 checked-core context))]
        {:context context :checked-core checked-core :c11 c11
         :packet packet}))))
(def ^:private b1-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path b1-relative-path))))
(def ^:private b1-upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @b1-artifact))))

(deftest sh07-b35-b1-source-contracts-and-static-shape-are-exact
  (let [forms (source-forms) ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 71 (count forms)))
    (is (= 70 (count definitions) (count by-name)))
    (is (= 6 (count (filter #(= 'def (first %)) definitions))))
    (is (= 64 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.backend.b1-backend-interface-specification
           (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= {:stage :stage1 :component :backend-interface
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
    (doseq [name packet-builder-names]
      (is (= '[lowering backend-manifest] (nth (get by-name name) 2))))
    (is (= expected-diagnostic-ids
           (mapv :id (get-in (nth (get by-name 'b1-diagnostic-catalog) 2)
                             [:diagnostics]))))
    (is (= 475 (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= expected-source-byte-count
           (alength (source-bytes (path b1-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path b1-relative-path)))))))

(deftest sh07-b35-b1-static-lookups-and-stage-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        by-name (into {} (map (juxt second identity)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        llvm (get by-name 'b1-build-bounded-llvm-authenticated-packet)
        c (get by-name 'b1-build-bounded-c-authenticated-packet)
        wasm (get by-name 'b1-build-bounded-wasm-authenticated-packet)]
    (is (= 1077 (count gets)))
    (is (= 1069 (count (filter #(keyword? (nth % 2 nil)) gets))))
    (is (= 8 (count dynamic)))
    (is (= {:reference 4 :call 4}
           (frequencies (map #(if (symbol? (nth % 2)) :reference :call)
                             dynamic))))
    (is (contains? (symbols-in llvm) 'b1-bounded-c14-input-valid?))
    (is (contains? (symbols-in llvm) 'b1-bounded-llvm-manifest-valid?))
    (is (contains? (symbols-in c) 'b1-bounded-c14-c-input-valid?))
    (is (contains? (symbols-in c) 'b1-bounded-c-manifest-valid?))
    (is (contains? (symbols-in wasm) 'b1-bounded-c14-wasm-input-valid?))
    (is (contains? (symbols-in wasm) 'b1-bounded-wasm-manifest-valid?))))

(deftest sh07-b35-b1-source-model-and-three-packet-builders-execute
  (let [manifest (invoke 'build-b1-backend-manifest
                         [:llvm [:hosted] [:llvm-ir]])
        input (invoke 'build-b1-input-packet
                      [{:kind :gravity/mir} :hosted {:backend :llvm} {}])
        verification (invoke 'verify-b1-backend-interface [manifest])]
    (is (= :gravity/backend-manifest (:artifact manifest)))
    (is (= :llvm (:backend manifest)))
    (is (= :gravity/b1-input-packet (:artifact input)))
    (is (true? (:source-model-only? input)))
    (is (= :gravity/b1-backend-interface-verification
           (:artifact verification)))
    (is (false? (:production-backend-execution? verification)))
    (doseq [[function expected-missing-fact]
            [['b1-build-bounded-llvm-authenticated-packet
              :accepted-c14-lowering-record]
             ['b1-build-bounded-c-authenticated-packet
              :accepted-c14-c-lowering-record]
             ['b1-build-bounded-wasm-authenticated-packet
              :accepted-c14-wasm-lowering-record]]]
      (testing (name function)
        (let [result (invoke function [nil nil])]
          (is (= :rejected (:status result)))
          (is (= :gravity/b1-verified-backend-input-packet
                 (:artifact result)))
          (is (= "B1-INPUT" (:diagnostic result)))
          (is (= ["B1-INPUT"] (:diagnostics result)))
          (is (= expected-missing-fact (:missing-fact result))))))))

(deftest sh07-b35-b1-claim-boundary-remains-explicit
  (let [forms (source-forms)
        contract-form
        (first (filter #(and (seq? %)
                             (= 'def (first %))
                             (= 'b1-backend-interface-contract (second %)))
                       forms))
        contract (nth contract-form 2)]
    (is (= :not-claimed (:production-backend-status contract)))
    (is (= :source-model-bridge (:scope contract)))
    (is (contains? (set (:forbidden contract)) :source-to-backend-shortcut))
    (is (contains? (set (:forbidden contract))
                   :target-undefined-behavior-as-semantics))))

(deftest sh07-b35-authentic-llvm-c-and-wasm-packets-execute
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_PACKETS"))
    (doseq [backend [:llvm :c :wasm]]
      (testing (name backend)
        (let [{:keys [packet checked-core context]}
              (authentic-b1-packet backend)
              expected-status
              (case backend
                :llvm :accepted-for-bounded-llvm
                :c :accepted-for-bounded-c
                :wasm :accepted-for-bounded-wasm)
              verifier
              (case backend
                :llvm
                bootstrap/p15-s23-stage2-c13-c14-b1-verification-report
                :c
                bootstrap/p15-s23-stage2-c13-c14-b1-c-verification-report
                :wasm
                bootstrap/p15-s23-stage2-c13-c14-b1-wasm-verification-report)
              expected-missing-fact
              (case backend
                :llvm :recomputable-c13-c14-b1-final-identities
                :c :recomputable-c13-c14-b1-c-final-identities
                :wasm :recomputable-c13-c14-b1-wasm-identities)
              verification (verifier packet checked-core context)
              altered-result
              (diagnostic-result
               #(verifier (assoc packet :semantic-id zero-id)
                          checked-core context))
              altered-data (:exception-data altered-result)]
          (is (= expected-status (:status packet)))
          (is (= :accepted (get-in packet [:c14 :status])))
          (is (= expected-status (get-in packet [:b1 :status])))
          (is (= :gravity/b1-verified-backend-input-packet
                 (get-in packet [:b1 :artifact])))
          (is (re-matches #"sha256:[0-9a-f]{64}" (:semantic-id packet)))
          (is (empty? (:diagnostics packet)))
          (is (= :passed (:status verification)))
          (is (= [:passed :passed :passed :passed]
                 ((juxt :c11 :c13 :c14 :b1) verification)))
          (is (re-matches #"sha256:[0-9a-f]{64}"
                          (:report-id verification)))
          (is (nil? (:raw-host-error altered-result)))
          (is (= "B1-METADATA" (:id altered-data)))
          (is (= expected-missing-fact (:missing-fact altered-data))))))
    (is true "Set GRAVITY_SH07_B35_PACKETS=1 in an isolated JVM")))

(deftest sh07-b35-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_CONTRACT"))
    (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B35" (:coverage-milestone contract)))
      (is (= b1-relative-path
             (get-in contract
                     [:authoritative-modules :b1-backend-interface])))
      (is (= {:keyword-lookups 0}
             (get-in contract
                     [:required-core-product-counts
                      :b1-backend-interface])))
      (is (every? (set (:nonclaims contract)) expected-nonclaims))
      (is (= expected-b16-bounds (:bounds contract))))
    (is true "Set GRAVITY_SH07_B35_CONTRACT=1 after coordinator registration")))

(deftest sh07-b35-b1-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_CENSUS"))
    (let [result (proof-census/census (path b1-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= expected-sh06-semantic-projection-id
             (get-in result [:request :sh06-semantic-projection-id])))
      (is (= expected-census-measurements measurements))
      (is (= 70 (:fragments measurements)))
      (is (= 70 (:top-level-forms measurements)))
      (is (= (:forms measurements)
             (:predicted-maximum-core-nodes measurements)))
      (is (= (+ 4 (:predicted-maximum-core-nodes measurements))
             (:predicted-maximum-digest-requests measurements)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result))))
    (is true "Set GRAVITY_SH07_B35_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b35-b1-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_AUTHENTIC"))
    (let [artifact @b1-artifact r (request artifact) c (core artifact)
          coverage-record (:fragment-coverage c)
          fragments (:fragment-manifest r)
          definition-index
          (first (keep-indexed
                  (fn [index definition]
                    (when (= 'b1-build-bounded-llvm-authenticated-packet
                             (:name definition))
                      index))
                  (:definitions c)))
          changed
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :definitions definition-index :binding-id] zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @b1-upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))] check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 15 (:schema-version r)))
      (is (= 'gravity.backend.b1-backend-interface-specification
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
    (is true "Set GRAVITY_SH07_B35_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b35-existing-rejected-families-remain-paired-and-structured
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_AUTHENTIC"))
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

(deftest sh07-b35-b1-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B35_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-b1-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root
                               "right/b1_backend_interface_specification.qst")
          left-path (path b1-relative-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path (source-bytes left-path)
                                   (make-array java.nio.file.OpenOption 0))
        (let [left @b1-artifact
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
    (is true "Set GRAVITY_SH07_B35_PARITY=1 in an isolated 8 GiB JVM")))
