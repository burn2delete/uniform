(ns gravity.self-hosting.sh07-checked-core-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-proof-census :as proof-census]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_checked_core_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 checked-core source test is not on the classpath"
                {:id "SH07-CHECKED-CORE-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-CHECKED-CORE-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private accepted-fixture-relative-path
  (str "bootstrap/clojure/fixtures/self-hosting/sh-07/accepted/"
       "macro-def-fn-literals.gravity"))
(def ^:private expected-source-byte-count 444325)
(def ^:private expected-source-revision-id
  "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092")
(def ^:private expected-plan-semantic-id
  "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd")
(def ^:private expected-functions-semantic-id
  "sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db")
(def ^:private expected-function-names-id
  "sha256:4e7bbfcd94db26a468920a87917005eee97a85f8f0448ba32cd689cafc9d02e5")
(def ^:private expected-function-shapes-id
  "sha256:61d6a743d65973ec4cb357c7285fae622c25f42b04a7de6aa8bf0fd0f1c02ee4")
(def ^:private expected-public-function-hashes
  {'sh07-build-core-template
   "sha256:3c986f70123a51afb4e788199f559b1d571afd825c2ba72c0a53675eb5c34948"
   'sh07-verify-core-template
   "sha256:4bc863464168971648f1c3e7ee17df32155e6c3d77b6c3d69d138566cf3b1791"
   'sh07-verify-core-resolved
   "sha256:d0aa83b35de51eb7fdbbdef6133aa5b20ec825340bf45d8c3833fb6801ffa8ba"})
(def ^:private expected-public-function-shapes
  {'sh07-build-core-template {:arity 1 :params '[request]}
   'sh07-verify-core-template
   {:arity 3 :params '[request template digest-requests]}
   'sh07-verify-core-resolved
   {:arity 4
    :params '[request resolved-core digest-requests resolved-digests]}})
(def ^:private expected-export-names
  '[sh07-core-contract
    sh07-core-bounds
    sh07-core-diagnostic-catalog
    sh07-build-core-template
    sh07-verify-core-template
    sh07-verify-core-resolved])
(def ^:private expected-data-definition-names
  '#{sh07-core-contract sh07-core-bounds sh07-core-diagnostic-catalog})
(def ^:private expected-required-functions
  '#{sh07-build-core-template sh07-verify-core-template
     sh07-verify-core-resolved sh07-request-shape?
     sh07-b13-fragment-products-shape? sh07-accepted-result
     sh07-graph-references-valid? sh07-lineage-shape?
     sh07-resolved-core-success sh07-resolved-core-carrier-preflight})
(def ^:private expected-diagnostic-ids
  ["C6-CORE-SHAPE" "C6-LOWERING-GAP" "C6-EVAL-ORDER" "C6-ORIGIN"
   "C6-EFFECT-DROP" "C6-UNSAFE-DROP" "C6-DOMAIN-BOUNDARY" "C6-VERIFY"])
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
      (throw (ex-info "Required SH-07 B45 coordinator adapter is absent"
                      {:id "SH07-CHECKED-CORE-COVERAGE-ADAPTER-ABSENT"
                       :symbol symbol}))))
(defn- required-fn [symbol] (var-get (required-var symbol)))
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
     :function-record-count (count (:function-records c))
     :call-edge-count (count (:call-edges c))
     :recursion-component-count (count (:recursion-components c))
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

(def ^:private checked-core-binding
  (delay (bootstrap/sh07-core-build-binding!)))
(def ^:private accepted-resolution
  (delay ((required-fn 'sh06-resolution-file-artifact)
          (path accepted-fixture-relative-path))))
(def ^:private accepted-request
  (delay ((required-fn 'sh07-core-authenticated-request)
          @accepted-resolution)))
(def ^:private raw-template-result
  (delay (bootstrap/sh07-core-execute!
          (path accepted-fixture-relative-path)
          'sh07-build-core-template [@accepted-request])))
(def ^:private resolved-digests
  (delay ((required-fn 'sh07-core-digest-requests)
          (path accepted-fixture-relative-path)
          (:digest-requests @raw-template-result))))
(def ^:private resolved-core
  (delay ((required-fn 'sh07-core-resolve-result)
          (path accepted-fixture-relative-path)
          (:core-template @raw-template-result)
          (:digest-requests @raw-template-result)
          @resolved-digests)))
(def ^:private source-artifact
  (delay ((required-fn 'sh07-core-file-artifact)
          (path source-relative-path))))
(def ^:private upstream-verification
  (delay ((required-fn 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @source-artifact))))

(deftest sh07-b45-checked-core-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        contract (nth (get by-name 'sh07-core-contract) 2)
        bounds (nth (get by-name 'sh07-core-bounds) 2)
        catalog (nth (get by-name 'sh07-core-diagnostic-catalog) 2)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 309 (count forms)))
    (is (= 308 (count definitions) (count by-name)))
    (is (= 3 (count (filter #(= 'def (first %)) definitions))))
    (is (= 305 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.checked-core (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :stage1 (:stage metadata)))
    (is (= :core-form-lowering (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (= expected-data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= :gravity/sh07-core-contract (:artifact contract)))
    (is (= :gravity/sh07-authenticated-sh06-core-request (:input contract)))
    (is (= :gravity/sh07-canonical-core-artifact (:resolved-output contract)))
    (is (= 65536 (:maximum-forms bounds)))
    (is (= 2440 (:maximum-module-bindings bounds)))
    (is (= 2048 (:maximum-bindings bounds)))
    (is (= 1024 (:maximum-keyword-lookup-records bounds)))
    (is (= expected-diagnostic-ids (:diagnostics catalog)))
    (is (= {4 1663}
           (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b47-checked-core-lookups-and-recursion-boundaries-are-exact
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
               name))]
    (is (= 2598 (count gets)))
    (is (= 2565 (count literal)))
    (is (= 33 (count dynamic)))
    (is (= 150 (count loops)))
    (is (= 176 (count recurs)))
    (is (= 3 (count self-recursive)))
    (is (= 5 (reduce +
                     (map #(count (collect-calls (second %) %)) functions))))
    (is (zero? (count (mapcat keyword-headed-calls definitions))))))

(deftest sh07-b45-checked-core-pinned-source-binding-is-exact
  (let [binding @checked-core-binding]
    (is (= expected-source-byte-count (:source-byte-count binding)
           bootstrap/sh07-core-expected-source-byte-count))
    (is (= expected-source-revision-id (:source-content-hash binding)
           bootstrap/sh07-core-expected-source-content-hash))
    (is (= expected-plan-semantic-id (:plan-semantic-hash binding)
           bootstrap/sh07-core-expected-plan-semantic-hash))
    (is (= expected-functions-semantic-id
           (:functions-semantic-hash binding)
           bootstrap/sh07-core-expected-functions-semantic-hash))
    (is (= 305 (:function-count binding)
           bootstrap/sh07-core-expected-function-count))
    (is (= expected-function-names-id (:function-names-hash binding)
           bootstrap/sh07-core-expected-function-names-hash))
    (is (= expected-function-shapes-id (:function-shapes-hash binding)
           bootstrap/sh07-core-expected-function-shapes-hash))
    (is (= expected-public-function-hashes
           (:public-function-hashes binding)
           bootstrap/sh07-core-public-function-hashes))
    (is (= expected-public-function-shapes
           (:public-function-shapes binding)
           bootstrap/sh07-core-public-function-shapes))
    (is (= :gravity-source (:semantic-authority binding)))
    (is (= :clojure-stage0-seed (:compiled-by binding)))
    (is (= :clojure-stage2-generic-rule-runner (:executed-by binding)))
    (is (true? (:generic-bridge-residual? binding)))
    (is (false? (:self-hosted? binding)))))

(deftest sh07-b45-checked-core-genuine-builder-and-verifier-replay
  (let [request @accepted-request
        raw @raw-template-result
        template-verification
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-template
         [request (:core-template raw) (:digest-requests raw)])
        resolved-verification
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-resolved
         [request @resolved-core (:digest-requests raw) @resolved-digests])]
    (is (= :accepted (:status @accepted-resolution)))
    (is (= :gravity/sh07-authenticated-sh06-core-request
           (:artifact request)))
    (is (= :accepted (:status raw)))
    (is (= :gravity/sh07-core-template-result (:artifact raw)))
    (is (= :gravity/sh07-canonical-core-template
           (get-in raw [:core-template :artifact])))
    (is (pos? (count (:digest-requests raw))))
    (is (= :passed (:status template-verification)))
    (is (= :gravity/sh07-core-template-verification
           (:artifact template-verification)))
    (is (nil? (:reason template-verification)))
    (is (nil? (:rule template-verification)))
    (is (= (count (:digest-requests raw)) (count @resolved-digests)))
    (is (every? #(and (string? %)
                      (re-matches #"sha256:[0-9a-f]{64}" %))
                @resolved-digests))
    (is (= :gravity/sh07-canonical-core-template (:artifact @resolved-core)))
    (is (= :passed (:status resolved-verification)))
    (is (= :gravity/sh07-core-resolved-verification
           (:artifact resolved-verification)))
    (is (nil? (:rule resolved-verification)))))

(deftest sh07-b45-checked-core-malformed-and-altered-products-fail-closed
  (let [request @accepted-request
        raw @raw-template-result
        malformed
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-build-core-template [nil])
        altered-template (assoc (:core-template raw) :artifact-id zero-id)
        altered-check
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-template
         [request altered-template (:digest-requests raw)])
        altered-requests
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-template
         [request (:core-template raw)
          (conj (:digest-requests raw)
                {:ordinal 999999 :preimage :altered})])
        altered-resolved-core
        (assoc-in @resolved-core [:module :namespace] 'gravity.altered)
        altered-resolved-check
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-resolved
         [request altered-resolved-core (:digest-requests raw)
          @resolved-digests])
        altered-resolved-digests (assoc @resolved-digests 0 zero-id)
        altered-digest-check
        (bootstrap/sh07-core-execute!
         (path accepted-fixture-relative-path)
         'sh07-verify-core-resolved
         [request @resolved-core (:digest-requests raw)
          altered-resolved-digests])]
    (is (= :rejected (:status malformed)))
    (is (= "C6-VERIFY" (get-in malformed [:diagnostics 0 :rule])))
    (is (= :request-shape
           (get-in malformed [:diagnostics 0 :facts :reason])))
    (is (true? (get-in malformed
                       [:containment :downstream-artifacts-forbidden])))
    (is (= :rejected (:status altered-check)))
    (is (= "C6-VERIFY" (:rule altered-check)))
    (is (= :template-replay (:reason altered-check)))
    (is (= :rejected (:status altered-requests)))
    (is (= "C6-VERIFY" (:rule altered-requests)))
    (is (= :digest-request-sequence (:reason altered-requests)))
    (is (= (count @resolved-digests) (count altered-resolved-digests)))
    (is (every? #(and (string? %)
                      (re-matches #"sha256:[0-9a-f]{64}" %))
                altered-resolved-digests))
    (is (not= @resolved-digests altered-resolved-digests))
    (is (= :rejected (:status altered-resolved-check)))
    (is (= :gravity/sh07-core-resolved-verification
           (:artifact altered-resolved-check)))
    (is (= "C6-VERIFY" (:rule altered-resolved-check)))
    (is (= :rejected (:status altered-digest-check)))
    (is (= :gravity/sh07-core-resolved-verification
           (:artifact altered-digest-check)))
    (is (= "C6-VERIFY" (:rule altered-digest-check)))))

(deftest sh07-b45-checked-core-claim-boundary-remains-explicit
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (into {} (map (juxt second identity))
                          (filter #(and (seq? %)
                                        (#{'def 'defn} (first %)))
                                  forms))
        contract (nth (get definitions 'sh07-core-contract) 2)
        metadata (get-in clauses [:metadata :bootstrap])]
    (is (some #{:authenticate-sh06-projection} (:host-authority contract)))
    (is (some #{:resolve-digest-requests} (:host-authority contract)))
    (is (some #{:bind-core-artifact} (:host-authority contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:whole-language? contract)))
    (is (false? (:target-support? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (some #{:not-complete-c6} (:claim-boundary metadata)))
    (is (some #{:not-checked-core} (:claim-boundary metadata)))
    (is (some #{:not-self-hosting} (:claim-boundary metadata)))
    (is (some #{:not-target-support} (:claim-boundary metadata)))
    (is (not (contains? contract :self-hosting-complete)))
    (is (not (contains? contract :release-ready)))))

(deftest sh07-b45-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B45_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          product-counts (:required-core-product-counts contract)]
      (is (= "SH-07-B47" (:coverage-milestone contract)))
      (is (= 42 (count (:authoritative-modules contract))))
      (is (= 38 (count product-counts)))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules :checked-core])))
      (is (= {:keyword-lookups 0
              :function-records 305
              :call-edges 6547
              :recursion-components 6}
             (get-in product-counts [:checked-core])))
      (is (= expected-b16-cohort-product-counts
             (select-keys product-counts
                          (keys expected-b16-cohort-product-counts))))
      (is (= 63 (reduce + (map (comp :keyword-lookups val)
                                expected-b16-cohort-product-counts)))))
    (is true "Set GRAVITY_SH07_B45_CONTRACT=1 after coordinator registration")))

(deftest sh07-b45-checked-core-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B45_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 308 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 42 (count (:authoritative-modules contract))))
      (is (= 38 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B45_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b45-checked-core-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B45_AUTHENTIC"))
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
          checks ((required-fn 'sh07-core-verification-checks)
                  changed artifact @upstream-verification)
          failed (set (for [[check passed?] checks
                            :when (not (true? passed?))]
                        check))]
      (is (= :accepted (:status artifact)))
      (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
      (is (= 'gravity.checked-core (get-in request [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in request [:module :source-revision-id])
             (get-in request [:lineage :source-revision-id])))
      (is (= 308 (:fragment-count (coverage artifact))
             (:root-form-count (coverage artifact))
             (:definition-count census)))
      (is (= 0 (:keyword-lookup-count census)))
      (is (= 305 (:function-record-count census)))
      (is (= 6547 (:call-edge-count census)))
      (is (= 6 (:recursion-component-count census)))
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
    (is true "Set GRAVITY_SH07_B45_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b45-checked-core-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B45_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-checked-core-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/checked_core.qst")
          left-path (path source-relative-path)]
      (try
        (write-bytes! right-path (source-bytes left-path))
        (let [left @source-artifact
              right ((required-fn 'sh07-core-file-artifact) (str right-path))
              identity-input (required-fn 'sh07-core-artifact-identity-input)]
          (is (= :accepted (:status left) (:status right)))
          (is (= (:artifact-id left) (:artifact-id right)))
          (is (= (identity-input left) (identity-input right)))
          (is (= (coverage left) (coverage right)))
          (is (= (core-census left) (core-census right)))
          (is (= left-path (get-in left [:provenance :source-path])))
          (is (= (str right-path) (get-in right [:provenance :source-path])))
          (is (not= left-path (str right-path))))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B45_PARITY=1 in an isolated 8 GiB JVM")))

(deftest sh07-b45-resolved-carrier-failure-preserves-telemetry
  (let [resolution
        {:status :rejected
         :reason :resolved-core-output-carrier-bound
         :carrier {:status :rejected :reason :carrier-scalar-byte-bound}
         :measurement {:status :accepted
                       :nodes 1
                       :scalar-bytes 2}}
        failure
        (try
          (with-redefs [bootstrap/sh07-core-execute!
                        (fn [& _] resolution)]
            (bootstrap/sh07-core-resolve-result
             "checked_core.gravity" {} [] []))
          nil
          (catch clojure.lang.ExceptionInfo exception
            (ex-data exception)))]
    (is (= "C6-VERIFY" (:id failure)))
    (is (= :resolved-core-output-carrier-bound (:reason failure)))
    (is (= resolution (:resolution failure)))))
