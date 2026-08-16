(ns gravity.self-hosting.sh07-sh22-standard-library-source-coverage-test
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
         "gravity/self_hosting/sh07_sh22_standard_library_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 SH-22 standard-library test is not on the classpath"
                {:id "SH07-SH22-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-SH22-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-22")
(def ^:private expected-source-byte-count 45717)
(def ^:private expected-source-revision-id
  "sha256:6198d87e19ee86f72d51aa1253c2d438da6af8dea495f2dd5eb0e64ca3eff1e4")
(def ^:private expected-plan-semantic-id
  "sha256:647df86e6dfb3a485ad98e7771b9662017271ac4bf9b5fd8af5b930d46e25c29")
(def ^:private expected-functions-semantic-id
  "sha256:0bd7665685931457a35b6b7eb5b2b86d6850f31958e1059ca09b2533fd05e136")
(def ^:private expected-handler-semantic-id
  "sha256:db9ed8fcd28d203910aad7d01258e0f1b0a2f1656b30b712a736369aac645ded")
(def ^:private expected-verifier-semantic-id
  "sha256:ce1e2e66bfaa477dbda6574112933f2b37ad3f51a1c09ef3be097050adec125e")
(def ^:private expected-export-names
  '[sh22-library-policy sh22-module-manifest
    sh22-handle-request sh22-verify-result])
(def ^:private expected-diagnostic-ids
  #{"STD1001" "STD1002" "STD1003" "STD2004" "STD3001" "STD3002"
    "STD4001" "STD5001" "STD8001" "STD8003"
    "STD10003" "STD15001" "STD20001"})
(def ^:private expected-pending-capabilities
  #{:authenticated-sh13-control-flow
    :authenticated-sh14-layout-and-allocation
    :authenticated-sh19-runtime-services
    :complete-unicode-and-normalization
    :complete-standard-library
    :seedless-execution})
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
(def ^:private expected-self-recursive-functions
  '#{sh22-canonical-vector sh22-path-neutral-sequence?
     sh22-path-neutral-map? sh22-vector-contains?
     sh22-canonical-entries sh22-canonical-set-values
     sh22-vector-unique? sh22-lowercase-hex? sh22-entry-index})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- fixture-relative-path [family basename extension]
  (str fixture-root "/" family "/" basename extension))

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
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- keyword-headed-calls [value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (keyword? (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info "Required SH-07 B41 coordinator adapter is absent"
                {:id "SH07-SH22-COVERAGE-ADAPTER-ABSENT"
                 :symbol symbol}))))

(defn- compile-plan [relative-path]
  (let [source-path (path relative-path)
        bytes (source-bytes source-path)
        source-text (String. bytes java.nio.charset.StandardCharsets/UTF_8)
        emitter (:emitter
                 (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                  source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private engine-plan (delay (compile-plan source-relative-path)))
(def ^:private accepted-gravity-plan
  (delay (compile-plan
          (fixture-relative-path "accepted" "library-requests" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (fixture-relative-path "accepted" "library-requests" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture-relative-path "rejected" "invalid-library-requests"
                                 ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture-relative-path "rejected" "invalid-library-requests"
                                 ".qst"))))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b41-sh22-standard-library-source-coverage
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))

(defn- fixture-request [plan function]
  (invoke plan function []))

(defn- core-request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- coverage [artifact]
  (let [request (core-request artifact)]
    {:fragment-count (count (:fragment-manifest request))
     :root-form-count (count (:top-level-form-ids request))
     :form-count (count (:forms request))
     :binding-count (count (:binding-table request))
     :resolution-count (count (:resolution-table request))}))

(defn- core-census [artifact]
  (let [canonical-core (core artifact)]
    {:node-count (count (:nodes canonical-core))
     :definition-count (count (:definitions canonical-core))
     :call-count (count (:calls canonical-core))
     :reference-count (count (:reference-uses canonical-core))
     :keyword-lookup-count (count (:keyword-lookups canonical-core))}))

(defn- delete-tree! [root-path]
  (when (java.nio.file.Files/exists
         root-path (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- write-bytes! [target bytes]
  (java.nio.file.Files/createDirectories
   (.getParent target) (make-array java.nio.file.attribute.FileAttribute 0))
  (java.nio.file.Files/write
   target bytes (make-array java.nio.file.OpenOption 0)))

(def ^:private source-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path source-relative-path))))
(def ^:private upstream-verification
  (delay ((required-var 'sh06-resolution-artifact-verification)
          (:sh06-resolution-artifact @source-artifact))))

(deftest sh07-b41-sh22-source-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 65 (count forms)))
    (is (= 64 (count definitions) (count by-name)))
    (is (zero? (count (filter #(= 'def (first %)) definitions))))
    (is (= 64 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.stdlib.self-hosting-core (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= {4 263} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b41-sh22-lookups-recursion-and-call-heads-are-exact
  (let [definitions (filter #(and (seq? %) (= 'defn (first %)))
                            (source-forms))
        gets (mapcat #(collect-calls 'get %) definitions)
        literal (filter #(keyword? (nth % 2 nil)) gets)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        loops (mapcat #(collect-calls 'loop %) definitions)
        recurs (mapcat #(collect-calls 'recur %) definitions)
        self-recursive
        (set (for [definition definitions
                   :let [name (second definition)]
                   :when (seq (collect-calls name definition))]
               name))]
    (is (= 186 (count gets)))
    (is (= 136 (count literal)))
    (is (= 50 (count dynamic)))
    (is (= 7 (count loops)))
    (is (= 8 (count recurs)))
    (is (= expected-self-recursive-functions self-recursive))
    (is (zero? (count (mapcat keyword-headed-calls definitions))))))

(deftest sh07-b41-sh22-source-model-executes-with-pinned-identities
  (let [plan @engine-plan
        functions (:functions plan)
        policy (invoke-engine 'sh22-library-policy [])]
    (is (= expected-plan-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= expected-functions-semantic-id
           (bootstrap/p15-s23-c11-mir-digest functions)))
    (is (= expected-handler-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh22-handle-request))))
    (is (= expected-verifier-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh22-verify-result))))
    (is (= 64 (count functions)))
    (is (= :gravity/sh22-bootstrap-library-policy (:artifact policy)))
    (is (= expected-diagnostic-ids (:diagnostics policy)))
    (is (= expected-pending-capabilities (set (:pending policy))))))

(deftest sh07-b41-sh22-accepted-fixtures-execute-and-verify-fresh
  (let [results
        (into {}
              (for [[extension plan]
                    [["gravity" accepted-gravity-plan]
                     ["qst" accepted-qst-plan]]]
                (let [request
                      (fixture-request plan 'sh22-vector-assoc-request)
                      result (invoke-engine 'sh22-handle-request [request])
                      verification
                      (invoke-engine 'sh22-verify-result [request result])]
                  [extension {:request request :result result
                              :verification verification}])))]
    (doseq [[extension {:keys [result verification]}] results]
      (testing extension
        (is (= :accepted (:status result)))
        (is (= [10 99 30] (:value result)))
        (is (empty? (:diagnostics result)))
        (is (= :passed (:status verification)))))
    (is (= (get-in results ["gravity" :request])
           (get-in results ["qst" :request])))
    (is (= (get-in results ["gravity" :result])
           (get-in results ["qst" :result])))))

(deftest sh07-b41-sh22-rejected-fixtures-and-altered-result-fail-closed
  (let [results
        (for [[extension accepted-plan rejected-plan]
              [["gravity" accepted-gravity-plan rejected-gravity-plan]
               ["qst" accepted-qst-plan rejected-qst-plan]]]
          (let [base
                (fixture-request accepted-plan 'sh22-vector-assoc-request)
                request
                (invoke rejected-plan 'sh22-out-of-bounds-request [base])
                result (invoke-engine 'sh22-handle-request [request])]
            {:extension extension :request request :result result}))]
    (doseq [{:keys [extension request result]} results]
      (testing extension
        (is (= :rejected (:status result)))
        (is (= "STD3002" (get-in result [:diagnostics 0 :rule])))
        (is (= :index-out-of-bounds
               (get-in result [:diagnostics 0 :reason])))
        (is (= :passed
               (:status
                (invoke-engine 'sh22-verify-result [request result]))))))
    (is (= (:request (first results)) (:request (second results))))
    (is (= (:result (first results)) (:result (second results))))
    (let [request
          (fixture-request accepted-gravity-plan 'sh22-vector-assoc-request)
          result (invoke-engine 'sh22-handle-request [request])
          changed (assoc result :value [10 98 30])
          verification (invoke-engine 'sh22-verify-result [request changed])]
      (is (= :rejected (:status verification)))
      (is (= "STD10003" (get-in verification [:diagnostics 0 :rule])))
      (is (= :library-result-substitution
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh07-b41-sh22-claim-boundary-remains-explicit
  (let [policy (invoke-engine 'sh22-library-policy [])]
    (is (= :experimental (:stability policy)))
    (is (= expected-pending-capabilities (set (:pending policy))))
    (is (some #{:authenticated-sh19-runtime-services} (:pending policy)))
    (is (some #{:complete-standard-library} (:pending policy)))
    (is (some #{:seedless-execution} (:pending policy)))
    (is (not (contains? policy :self-hosting-complete)))
    (is (not (contains? policy :release-ready)))
    (is (not (contains? policy :target-support-complete)))))

(deftest sh07-b41-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B41_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          product-counts (:required-core-product-counts contract)]
      (is (= "SH-07-B41" (:coverage-milestone contract)))
      (is (= 37 (count (:authoritative-modules contract))))
      (is (= 33 (count product-counts)))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules
                               :sh22-standard-library])))
      (is (= {:keyword-lookups 0}
             (get-in product-counts [:sh22-standard-library])))
      (is (= expected-b16-cohort-product-counts
             (select-keys product-counts
                          (keys expected-b16-cohort-product-counts))))
      (is (= 63 (reduce + (map (comp :keyword-lookups val)
                                expected-b16-cohort-product-counts)))))
    (is true "Set GRAVITY_SH07_B41_CONTRACT=1 after coordinator registration")))

(deftest sh07-b41-sh22-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B41_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 64 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 37 (count (:authoritative-modules contract))))
      (is (= 33 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B41_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b41-sh22-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B41_AUTHENTIC"))
    (let [artifact @source-artifact
          request (core-request artifact)
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
      (is (= 'gravity.stdlib.self-hosting-core
             (get-in request [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in request [:module :source-revision-id])
             (get-in request [:lineage :source-revision-id])))
      (is (= 64 (:fragment-count (coverage artifact))
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
    (is true "Set GRAVITY_SH07_B41_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b41-sh22-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B41_PARITY"))
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-sh22-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/self_hosting_core.qst")
          left-path (path source-relative-path)]
      (try
        (write-bytes! right-path (source-bytes left-path))
        (let [left @source-artifact
              right ((required-var 'sh07-core-file-artifact) (str right-path))
              identity-input
              (required-var 'sh07-core-artifact-identity-input)]
          (is (= :accepted (:status left) (:status right)))
          (is (= (:artifact-id left) (:artifact-id right)))
          (is (= (identity-input left) (identity-input right)))
          (is (= (coverage left) (coverage right)))
          (is (= (core-census left) (core-census right)))
          (is (= left-path (get-in left [:provenance :source-path])))
          (is (= (str right-path) (get-in right [:provenance :source-path])))
          (is (not= left-path (str right-path))))
        (finally (delete-tree! temp-root))))
    (is true "Set GRAVITY_SH07_B41_PARITY=1 in an isolated 8 GiB JVM")))
