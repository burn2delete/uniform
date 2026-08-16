(ns gravity.self-hosting.sh07-l1-c2-surface-reader-source-coverage-test
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
         "gravity/self_hosting/sh07_l1_c2_surface_reader_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 L1/C2 surface reader test is not on the classpath"
                      {:id "SH07-L1-C2-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-L1-C2-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private sh03-fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-03")
(def ^:private expected-source-byte-count 36920)
(def ^:private expected-source-revision-id
  "sha256:cb416baa7330fd7db5507fcd5fc1d78d5c9e848feb9020552d4c21b9e1c17fe0")
(def ^:private expected-plan-semantic-id
  "sha256:1012a51721cd2c29e260a6f7bc3bc0b2ced5aefe1222cbbd95a155fc44f8977f")
(def ^:private expected-functions-semantic-id
  "sha256:1ac79857179b3729eb47c3ce364f990333799ffec49f5047e700be7e7a827123")
(def ^:private expected-builder-semantic-id
  "sha256:e29e04b088323661b1949b6be255011a1196fbc65b4c20ff7bdfcd899baae8c5")
(def ^:private expected-export-names
  '[l1-c2-source-unit-contract l1-c2-reader-diagnostic-catalog
    read-l1-c2-source-unit l1-c2-reader-result-compatible?
    l1-c2-package-reader-result])
(def ^:private expected-data-definition-names
  '#{l1-c2-source-unit-contract l1-c2-reader-diagnostic-catalog})
(def ^:private expected-required-functions
  '#{read-l1-c2-source-unit l1-c2-source-unit-valid?
     l1-c2-reader-policy-valid? l1-c2-reader-result-products-valid?
     l1-c2-reader-result-shape-compatible?
     l1-c2-verification-report-compatible?
     l1-c2-reader-result-compatible? l1-c2-package-reader-result})
(def ^:private expected-l1-diagnostics
  ["L1-DELIMITER" "L1-STRING" "L1-MAP-ARITY" "L1-SET"
   "L1-METADATA" "L1-ABBREV" "L1-NUMERIC" "L1-IDENTIFIER"
   "L1-NS-SHAPE" "L1-READER-EXTENSION" "L1-SOURCE-ENCODING"
   "L1-SOURCE-EXTENSION"])
(def ^:private expected-c2-diagnostics
  ["C2-ENCODING" "C2-DELIMITER" "C2-STRING" "C2-MAP" "C2-SET"
   "C2-METADATA" "C2-ABBREV" "C2-EXTENSION" "C2-NUMERIC"
   "C2-IDENTIFIER" "C2-NS-SHAPE" "C2-HASH"])
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))
(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))
(defn- sha256-id [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))
(defn- source-forms []
  (with-open [reader (clojure.lang.LineNumberingPushbackReader.
                      (io/reader (path source-relative-path)))]
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
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07 B39 coordinator adapter is absent"
                      {:id "SH07-L1-C2-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- compile-plan []
  (let [source-path (path source-relative-path)
        bytes (source-bytes source-path)
        source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))
(def ^:private engine-plan (delay (compile-plan)))
(defn- invoke [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b39-l1-c2-source-coverage
    :compiler-artifact-plan? true}
   @engine-plan function arguments))
(defn- sh03-facade-inputs [source-path]
  (let [bytes (source-bytes source-path)
        project-context
        (bootstrap/reader-project-context-for-source source-path)
        source-unit
        (bootstrap/sh03-reader-input-source-unit
         source-path bytes project-context)
        reader-policy
        (bootstrap/sh03-reader-input-policy bootstrap/standard-reader-options)
        resolved
        (bootstrap/sh03-reader-resolved-result!
         source-path bytes project-context bootstrap/standard-reader-options)]
    [source-unit reader-policy (:raw-result resolved)
     (:verification-report resolved)]))
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
  (let [c (core artifact)]
    {:node-count (count (:nodes c))
     :definition-count (count (:definitions c))
     :call-count (count (:calls c))
     :reference-count (count (:reference-uses c))
     :keyword-lookup-count (count (:keyword-lookups c))}))
(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))
(defn- write-bytes! [target bytes]
  (java.nio.file.Files/createDirectories
   (.getParent target) (make-array java.nio.file.attribute.FileAttribute 0))
  (java.nio.file.Files/write target bytes
                             (make-array java.nio.file.OpenOption 0)))

(def ^:private source-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path source-relative-path))))
(def ^:private upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @source-artifact))))

(deftest sh07-b39-l1-c2-source-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        metadata (get-in clauses [:metadata :bootstrap])
        diagnostic-catalog (nth (get by-name 'l1-c2-reader-diagnostic-catalog) 2)
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 33 (count forms)))
    (is (= 32 (count definitions) (count by-name)))
    (is (= 2 (count (filter #(= 'def (first %)) definitions))))
    (is (= 30 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.compiler.l1-c2-surface-syntax-reader (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :source-frontend (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= expected-data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= expected-l1-diagnostics (:l1 diagnostic-catalog)))
    (is (= expected-c2-diagnostics (:c2 diagnostic-catalog)))
    (is (= 12 (count (:l1-to-c2 diagnostic-catalog))))
    (is (= 180 (count if-calls)))
    (is (= {4 180} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b39-l1-c2-static-lookups-and-recursion-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        gets (mapcat #(collect-calls 'get %) definitions)
        literal (filter #(keyword? (nth % 2 nil)) gets)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        loops (mapcat #(collect-calls 'loop %) definitions)
        recurs (mapcat #(collect-calls 'recur %) definitions)]
    (is (= 149 (count gets)))
    (is (= 148 (count literal)))
    (is (= 1 (count dynamic)))
    (is (= 'ordinal (nth (first dynamic) 2)))
    (is (= 2 (count loops)))
    (is (= 2 (count recurs)))))

(deftest sh07-b39-l1-c2-source-model-executes-with-pinned-identities
  (let [plan @engine-plan
        functions (:functions plan)
        source-owned (invoke 'read-l1-c2-source-unit
                             ["/physical/input.gravity" :gravity "(def x 1)"])
        rejected-package
        (invoke 'l1-c2-package-reader-result [nil nil nil nil])]
    (is (= expected-plan-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= expected-functions-semantic-id
           (bootstrap/p15-s23-c11-mir-digest functions)))
    (is (= expected-builder-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'l1-c2-package-reader-result))))
    (is (= 30 (count functions)))
    (is (= :stage1-source-owned (:status source-owned)))
    (is (= 'source-kind (:source-kind source-owned)))
    (is (= 'source-path (:source-path source-owned)))
    (is (= :gravity/l1-c2-reader-compatibility-package
           (:artifact rejected-package)))
    (is (= :rejected (:compatibility-status rejected-package)))
    (is (false? (:authentication-credit? rejected-package)))
    (is (false? (:authoritative-reader-result? rejected-package)))))

(deftest sh07-b39-l1-c2-facade-consumes-genuine-sh03-products
  (let [results
        (into {}
              (for [[family basename expected-status]
                    [["accepted" "complete-reader-surface" :accepted]
                     ["rejected" "malformed-numeric" :rejected]]
                    extension ["gravity" "qst"]]
                (let [source-path
                      (path (str sh03-fixture-root "/" family "/" basename
                                 "." extension))
                      inputs (sh03-facade-inputs source-path)
                      compatible?
                      (invoke 'l1-c2-reader-result-compatible? inputs)
                      package
                      (invoke 'l1-c2-package-reader-result inputs)]
                  [[family extension]
                   {:source-path source-path
                    :inputs inputs
                    :expected-status expected-status
                    :compatible? compatible?
                    :package package}])))]
    (doseq [[[family extension]
             {:keys [inputs expected-status compatible? package]}]
            results]
      (let [[_ _ reader-result verification-report] inputs
            preserved-products
            {:source-unit-record :source-unit
             :actual-path-provenance :actual-path-provenance
             :token-stream :token-stream
             :form-tree :form-tree
             :semantic-value-table :semantic-value-table
             :top-level-form-ids :top-level-form-ids
             :top-level-parsed-records :top-level-parsed-records
             :parsed-semantic-values :parsed-semantic-values
             :literal-decoding-records :literal-decoding-records
             :semantic-error-deferment-record
             :semantic-error-deferment-record
             :reader-extension-invocation-records
             :reader-extension-invocation-records
             :reader-source-map :reader-source-map
             :incremental-reader-hashes :incremental-reader-hashes
             :semantic-reader-template :semantic-reader-template
             :digest-requests :digest-requests
             :reader-diagnostics :diagnostics
             :execution-boundary :execution-boundary}]
        (testing (str family "." extension)
          (is (true? compatible?))
          (is (= :gravity/l1-c2-reader-compatibility-package
                 (:artifact package)))
          (is (= :compatible (:compatibility-status package)))
          (is (= expected-status (:reader-result-status package)))
          (is (true? (:compatibility-only? package)))
          (is (false? (:authentication-credit? package)))
          (is (false? (:authoritative-reader-result? package)))
          (is (= :sh03-fresh-verifier-and-digest-resolution
                 (:authoritative-route package)))
          (is (false? (:fresh-verifier-executed-here? package)))
          (is (false? (:digest-resolution-executed-here? package)))
          (doseq [[package-key reader-result-key] preserved-products]
            (is (= (get reader-result reader-result-key)
                   (get package package-key))
                (str "mismatched preserved product " package-key)))
          (is (= verification-report
                 (:reader-verification-report package))))))
    (doseq [family ["accepted" "rejected"]]
      (let [gravity (get results [family "gravity"])
            qst (get results [family "qst"])]
        (is (= (vec (source-bytes (:source-path gravity)))
               (vec (source-bytes (:source-path qst)))))
        (is (= (:reader-result-status (:package gravity))
               (:reader-result-status (:package qst))))))
    (let [[source-unit reader-policy reader-result verification-report]
          (:inputs (get results ["accepted" "gravity"]))
          altered-reader-result (assoc reader-result :unexpected true)
          altered-inputs [source-unit reader-policy altered-reader-result
                          verification-report]
          altered-package
          (invoke 'l1-c2-package-reader-result altered-inputs)
          substituted-reader-result (assoc reader-result :token-stream [])
          substituted-inputs
          [source-unit reader-policy substituted-reader-result
           verification-report]
          substituted-package
          (invoke 'l1-c2-package-reader-result substituted-inputs)]
      (is (false?
           (invoke 'l1-c2-reader-result-compatible? altered-inputs)))
      (is (= :rejected (:compatibility-status altered-package)))
      (is (false? (:authentication-credit? altered-package)))
      (is (false? (:authoritative-reader-result? altered-package)))
      (is (not= (:token-stream reader-result)
                (:token-stream substituted-reader-result)))
      (is (= (set (keys reader-result))
             (set (keys substituted-reader-result))))
      (is (false?
           (invoke 'l1-c2-reader-result-compatible? substituted-inputs)))
      (is (= :rejected (:compatibility-status substituted-package)))
      (is (false? (:authentication-credit? substituted-package)))
      (is (false? (:authoritative-reader-result?
                   substituted-package))))))

(deftest sh07-b39-l1-c2-claim-boundary-remains-explicit
  (let [definitions (into {} (map (juxt second identity))
                          (filter #(and (seq? %) (= 'def (first %)))
                                  (source-forms)))
        contract (nth (get definitions 'l1-c2-source-unit-contract) 2)
        facade (:facade-package-boundary contract)
        execution-boundary (invoke 'l1-c2-reader-execution-boundary-record [])]
    (is (true? (:compatibility-only? facade)))
    (is (false? (:authentication-credit? facade)))
    (is (false? (:authoritative-reader-result? facade)))
    (is (= :sh03-fresh-verifier-and-digest-resolution
           (:authoritative-route facade)))
    (is (= :gravity-source (:owner execution-boundary)))
    (is (false? (:self-hosted? execution-boundary)))
    (is (false? (:whole-language? execution-boundary)))
    (is (true? (:clojure-seed-boundary? execution-boundary)))
    (is (some #{:sha256-resolution}
              (:clojure-seed-boundary execution-boundary)))))

(deftest sh07-b39-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B39_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B39" (:coverage-milestone contract)))
      (is (= 35 (count (:authoritative-modules contract))))
      (is (= 31 (count (:required-core-product-counts contract))))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules
                               :l1-c2-surface-reader])))
      (is (= {:keyword-lookups 0}
             (get-in contract [:required-core-product-counts
                               :l1-c2-surface-reader]))))
    (is true "Set GRAVITY_SH07_B39_CONTRACT=1 after coordinator registration")))

(deftest sh07-b39-l1-c2-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B39_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 32 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 35 (count (:authoritative-modules contract))))
      (is (= 31 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B39_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b39-l1-c2-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B39_AUTHENTIC"))
    (let [artifact @source-artifact
          r (request artifact)
          c (core artifact)
          coverage-record (:fragment-coverage c)
          fragments (:fragment-manifest r)
          census (core-census artifact)
          changed (assoc-in artifact
                            [:gravity-core-boundary :canonical-core-artifact
                             :definitions 0 :binding-id]
                            zero-id)
          checks ((required-var 'sh07-core-verification-checks)
                  changed artifact @upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))]
                        check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 'gravity.compiler.l1-c2-surface-syntax-reader
             (get-in r [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in r [:module :source-revision-id])
             (get-in r [:lineage :source-revision-id])))
      (is (= 32 (:fragment-count (coverage artifact))
             (:root-form-count (coverage artifact))
             (:definition-count census)))
      (is (= 0 (:keyword-lookup-count census)))
      (is (= (:top-level-form-ids r)
             (:covered-root-form-ids coverage-record)
             (vec (mapcat :root-form-ids fragments))))
      (is (= (mapv :form-id (:forms r))
             (:covered-form-ids coverage-record)
             (vec (mapcat :form-ids fragments))))
      (is (false? (get-in artifact
                          [:gravity-core-boundary :target-source-reread?])))
      (is (= :complete (get-in artifact [:capability-based-proof :status])))
      (is (contains? failed :canonical-core-replays?)))
    (is true "Set GRAVITY_SH07_B39_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b39-l1-c2-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B39_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-l1-c2-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root
                               "right/l1_c2_surface_syntax_reader.qst")
          left-path (path source-relative-path)]
      (try
        (write-bytes! right-path (source-bytes left-path))
        (let [left @source-artifact
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
    (is true "Set GRAVITY_SH07_B39_PARITY=1 in an isolated 8 GiB JVM")))
