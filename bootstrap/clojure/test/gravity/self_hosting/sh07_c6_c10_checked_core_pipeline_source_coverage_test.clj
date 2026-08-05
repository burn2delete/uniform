(ns gravity.self-hosting.sh07-c6-c10-checked-core-pipeline-source-coverage-test
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
         (str "gravity/self_hosting/"
              "sh07_c6_c10_checked_core_pipeline_source_coverage_test.clj"))]
    (when-not resource
      (throw
       (ex-info "SH-07 C6-C10 source test is not on the classpath"
                {:id "SH07-C6-C10-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-C6-C10-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 141562)
(def ^:private expected-source-revision-id
  "sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5")
(def ^:private expected-plan-semantic-id
  "sha256:c0abe759e32feb1810cbb477bba2e1db6bf41735b5e06beed8f0308babd24339")
(def ^:private expected-functions-semantic-id
  "sha256:79984c3b2cfd56f64ee161cb6d9611be55c2a8d614120c9ebd2477d5835a4aff")
(def ^:private expected-builder-semantic-id
  "sha256:1e14438321405f6f0126fab7ddebd6e4f1450e6074f5a832a56ec53ed64252f7")
(def ^:private expected-verifier-semantic-id
  "sha256:5695bc710e197df7b43efb9288480db43ae1e56a1da4d54a12180fd9e3b6906a")
(def ^:private expected-export-names
  '[c6-c10-checked-core-pass-contract
    c6-c10-checked-core-scope-contract
    c6-c10-build-checked-core-template
    c6-c10-verify-checked-core-template])
(def ^:private expected-data-definition-names
  '#{c6-c10-checked-core-pass-contract
     c6-c10-checked-core-scope-contract
     c6c10-core-node-lowering-rule-version
     c6-c10-emitted-diagnostic-subset
     c6-c10-normative-diagnostic-catalogs})
(def ^:private expected-required-functions
  '#{c6c10-envelope-shape-valid? c6c10-build-accepted-template
     c6c10-upstream-diagnostic-rejection
     c6c10-template-structure-equal? c6c10-template-origin-equal?
     c6c10-template-type-equal? c6c10-template-effect-equal?
     c6c10-template-ownership-profile-equal?
     c6c10-template-safety-equal?
     c6-c10-build-checked-core-template
     c6-c10-verify-checked-core-template})
(def ^:private expected-emitted-diagnostic-subset
  {:L2 ["L2-BUILTIN-ARITY"]
   :C6 ["C6-LOWERING-GAP" "C6-CORE-SHAPE" "C6-ORIGIN" "C6-VERIFY"]
   :C7 ["C7-TYPE-MISMATCH" "C7-VERIFY"]
   :C8 ["C8-VERIFY"]
   :C9 []
   :C10 ["C10-PROOF"]})
(def ^:private expected-b16-cohort-product-counts
  {:b5-jvm {:keyword-lookups 9}
   :b6-javascript-typescript {:keyword-lookups 8}
   :b7-mlir {:keyword-lookups 5}
   :b8-gpu {:keyword-lookups 8}
   :b9-hdl {:keyword-lookups 10}
   :b10-workflow {:keyword-lookups 7}
   :b11-query {:keyword-lookups 8}
   :b12-mobile {:keyword-lookups 8}
   :c11-mir {:keyword-lookups 0}})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))
(def ^:private accepted-source
  (str "(ns gravity.sh07.c6c10.probe "
       "(:profile :hosted) (:target :jvm) "
       "(:effects #{}) (:capabilities #{}) (:safety :safe) "
       "(:exports [main]))\n"
       "(defn main [] nil)\n"))

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
(defn- keyword-headed-calls [value]
  (let [found (volatile! [])]
    (walk/postwalk (fn [entry]
                     (when (and (seq? entry) (keyword? (first entry)))
                       (vswap! found conj entry))
                     entry)
                   value)
    @found))
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07 B44 coordinator adapter is absent"
                      {:id "SH07-C6-C10-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- required-fn [symbol] (deref (required-var symbol)))
(defn- invoke-pinned [source-binding function arguments boundary]
  (bootstrap/p15-s23-c6c10-invoke-pinned-source-function!
   "/virtual/sh07-c6-c10-coverage.gravity"
   source-binding function arguments boundary))
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

(def ^:private accepted-context
  (delay
    (bootstrap/p15-s23-stage2-gravity-checked-core-context
     "/virtual/sh07-c6-c10-coverage.gravity" accepted-source :jvm)))
(def ^:private accepted-construction
  (delay ((required-fn 'p15-s23-c6c10-fresh-construction)
          @accepted-context)))
(def ^:private source-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path source-relative-path))))
(def ^:private upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @source-artifact))))

(deftest sh07-b44-c6-c10-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        pass-contract (nth (get by-name 'c6-c10-checked-core-pass-contract) 2)
        scope-contract
        (nth (get by-name 'c6-c10-checked-core-scope-contract) 2)
        emitted (nth (get by-name 'c6-c10-emitted-diagnostic-subset) 2)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 145 (count forms)))
    (is (= 144 (count definitions) (count by-name)))
    (is (= 5 (count (filter #(= 'def (first %)) definitions))))
    (is (= 139 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.compiler.c6-c10-checked-core-pipeline (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :c6-c10-checked-core (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= expected-data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= :gravity/c6-c10-checked-core-pass-contract
           (:artifact pass-contract)))
    (is (= :gravity/private-fresh-c2-c3-stage2-plan-envelope
           (:input pass-contract)))
    (is (= :gravity/c6-c10-gravity-checked-core-template
           (:output pass-contract)))
    (is (= 128 (get-in scope-contract [:limits :maximum-forms])))
    (is (= expected-emitted-diagnostic-subset emitted))
    (is (= {4 524} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b44-c6-c10-lookups-and-recursion-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (#{'def 'defn} (first %)))
                            (source-forms))
        functions (filter #(= 'defn (first %)) definitions)
        gets (mapcat #(collect-calls 'get %) definitions)
        literal (filter #(keyword? (nth % 2 nil)) gets)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        loops (mapcat #(collect-calls 'loop %) definitions)
        recurs (mapcat #(collect-calls 'recur %) definitions)
        self-recursive
        (set (for [definition functions
                   :let [name (second definition)]
                   :when (seq (collect-calls name definition))]
               name))
        self-call-count
        (reduce + (map #(count (collect-calls (second %) %)) functions))]
    (is (= 667 (count gets)))
    (is (= 651 (count literal)))
    (is (= 16 (count dynamic)))
    (is (= {'map-key 5 '(get instruction :name) 1 'name 1
            'binding-id 1 'return-node-id 1 ''main 3 'field 4}
           (frequencies (map #(nth % 2 nil) dynamic))))
    (is (empty? loops))
    (is (empty? recurs))
    (is (= 42 (count self-recursive)))
    (is (= 49 self-call-count))
    (is (zero? (count (mapcat keyword-headed-calls definitions))))))

(deftest sh07-b44-c6-c10-pinned-source-binding-is-exact
  (let [fresh @accepted-construction
        binding (:private-source-binding fresh)]
    (is (= expected-source-byte-count (:source-byte-count binding)))
    (is (= expected-source-revision-id (:source-content-hash binding)))
    (is (= expected-plan-semantic-id (:plan-semantic-hash binding)))
    (is (= expected-functions-semantic-id
           (:functions-semantic-hash binding)))
    (is (= expected-builder-semantic-id (:builder-semantic-hash binding)))
    (is (= expected-verifier-semantic-id (:verifier-semantic-hash binding)))
    (is (= 139 (count (get-in binding [:plan :functions]))))
    (is (= {'c6-c10-build-checked-core-template
            {:arity 1 :params ['input]}
            'c6-c10-verify-checked-core-template
            {:arity 3 :params ['input 'template 'requests]}}
           (:function-shapes binding)))
    (is (= (path source-relative-path) (:source-path binding)))
    (is (= :complete (:status binding)))))

(deftest sh07-b44-c6-c10-genuine-builder-and-verifier-replay
  (let [fresh @accepted-construction
        binding (:private-source-binding fresh)
        envelope (:private-envelope fresh)
        raw (:raw-result fresh)
        replay (invoke-pinned binding 'c6-c10-build-checked-core-template
                              [envelope] :b44-builder-replay)
        verification
        (invoke-pinned binding 'c6-c10-verify-checked-core-template
                       [envelope (:artifact-template raw)
                        (:digest-requests raw)]
                       :b44-verifier-replay)]
    (is (= :accepted (:status raw) (:status replay)))
    (is (= raw replay))
    (is (= :passed (:status verification)))
    (is (= :gravity-source (:semantic-authority verification)))
    (is (= [:fresh-template-replay :exact-digest-request-replay
            :bounded-pure-c6-c10-scope]
           (:checks verification)))
    (is (= (count (:digest-requests raw))
           (:request-count verification)))
    (is (empty? (:diagnostics verification)))
    (is (= :passed (get-in fresh [:gravity-verification :status])))
    (is (re-matches #"sha256:[0-9a-f]{64}"
                    (get-in fresh [:artifact :artifact-id])))))

(deftest sh07-b44-c6-c10-malformed-and-altered-products-fail-closed
  (let [fresh @accepted-construction
        binding (:private-source-binding fresh)
        envelope (:private-envelope fresh)
        raw (:raw-result fresh)
        malformed
        (invoke-pinned binding 'c6-c10-build-checked-core-template
                       [nil] :b44-malformed-builder)
        altered-template (assoc (:artifact-template raw) :artifact-id zero-id)
        altered-check
        (invoke-pinned binding 'c6-c10-verify-checked-core-template
                       [envelope altered-template (:digest-requests raw)]
                       :b44-altered-template)
        altered-requests
        (invoke-pinned binding 'c6-c10-verify-checked-core-template
                       [envelope (:artifact-template raw)
                        (conj (:digest-requests raw)
                              {:ordinal 999999 :preimage :altered})]
                       :b44-altered-requests)]
    (is (= :rejected (:status malformed)))
    (is (= "C6-CORE-SHAPE" (get-in malformed [:diagnostics 0 :rule])))
    (is (= :exact-private-c2-c3-stage2-plan-envelope
           (get-in malformed [:diagnostics 0 :facts :missing-fact])))
    (is (= :rejected (:status altered-check)))
    (is (= "C6-VERIFY" (get-in altered-check [:diagnostics 0 :rule])))
    (is (= :exact-gravity-artifact-template-replay
           (get-in altered-check [:diagnostics 0 :facts :missing-fact])))
    (is (= :rejected (:status altered-requests)))
    (is (= "C6-VERIFY" (get-in altered-requests [:diagnostics 0 :rule])))
    (is (= :exact-digest-request-replay
           (get-in altered-requests [:diagnostics 0 :facts :missing-fact])))))

(deftest sh07-b44-c6-c10-claim-boundary-remains-explicit
  (let [definitions (into {} (map (juxt second identity))
                          (filter #(and (seq? %)
                                        (#{'def 'defn} (first %)))
                                  (source-forms)))
        contract (nth (get definitions 'c6-c10-checked-core-pass-contract) 2)
        scope (nth (get definitions 'c6-c10-checked-core-scope-contract) 2)]
    (is (some #{:canonical-encode} (:host-authority contract)))
    (is (some #{:sha256} (:host-authority contract)))
    (is (some #{:fresh-replay} (:host-authority contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:whole-language? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (= [:hosted] (:profiles scope)))
    (is (= [:jvm] (:source-targets scope)))
    (is (some #{:call} (:forbidden scope)))
    (is (some #{:runtime-check} (:forbidden scope)))
    (is (some #{:unsafe-island} (:forbidden scope)))
    (is (not (contains? contract :self-hosting-complete)))
    (is (not (contains? contract :release-ready)))))

(deftest sh07-b44-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B44_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          product-counts (:required-core-product-counts contract)]
      (is (= "SH-07-B44" (:coverage-milestone contract)))
      (is (= 40 (count (:authoritative-modules contract))))
      (is (= 36 (count product-counts)))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules :c6-c10-pipeline])))
      (is (= {:keyword-lookups 0}
             (get-in product-counts [:c6-c10-pipeline])))
      (is (= expected-b16-cohort-product-counts
             (select-keys product-counts
                          (keys expected-b16-cohort-product-counts))))
      (is (= 63 (reduce + (map (comp :keyword-lookups val)
                                expected-b16-cohort-product-counts)))))
    (is true "Set GRAVITY_SH07_B44_CONTRACT=1 after coordinator registration")))

(deftest sh07-b44-c6-c10-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B44_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 144 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 40 (count (:authoritative-modules contract))))
      (is (= 36 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B44_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b44-c6-c10-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B44_AUTHENTIC"))
    (let [artifact @source-artifact
          request (request artifact)
          canonical-core (core artifact)
          coverage-record (:fragment-coverage canonical-core)
          fragments (:fragment-manifest request)
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
      (is (= 'gravity.compiler.c6-c10-checked-core-pipeline
             (get-in request [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in request [:module :source-revision-id])
             (get-in request [:lineage :source-revision-id])))
      (is (= 144 (:fragment-count (coverage artifact))
             (:root-form-count (coverage artifact))
             (:definition-count census)))
      (is (= 0 (:keyword-lookup-count census)))
      (is (= (:top-level-form-ids request)
             (:covered-root-form-ids coverage-record)
             (vec (mapcat :root-form-ids fragments))))
      (is (= (mapv :form-id (:forms request))
             (:covered-form-ids coverage-record)
             (vec (mapcat :form-ids fragments))))
      (is (false? (get-in artifact
                          [:gravity-core-boundary :target-source-reread?])))
      (is (= :complete (get-in artifact [:capability-based-proof :status])))
      (is (contains? failed :canonical-core-replays?)))
    (is true "Set GRAVITY_SH07_B44_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b44-c6-c10-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B44_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-c6-c10-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c6_c10_pipeline.qst")
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
    (is true "Set GRAVITY_SH07_B44_PARITY=1 in an isolated 8 GiB JVM")))
