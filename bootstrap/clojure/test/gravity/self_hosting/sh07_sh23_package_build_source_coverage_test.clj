(ns gravity.self-hosting.sh07-sh23-package-build-source-coverage-test
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
         "gravity/self_hosting/sh07_sh23_package_build_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 SH-23 package/build test is not on the classpath"
                      {:id "SH07-SH23-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-SH23-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-23")
(def ^:private expected-source-byte-count 39872)
(def ^:private expected-source-revision-id
  "sha256:70649e49d7130cf33e1d11ac226452ea908fee6731e0e49ca8e003e14cbabe1e")
(def ^:private expected-plan-semantic-id
  "sha256:8c401426b7acceb54c0d9141ba14fc77e52e538dfd0e1de86e52c251d371d67f")
(def ^:private expected-functions-semantic-id
  "sha256:db17a83164c8cd76c2ca749d9a5fe90933a2f4258807d67d3ba0866c45b8ec08")
(def ^:private expected-builder-semantic-id
  "sha256:255e8189d2f62efb37b696185dbf7416aa2437493c5d632dc50cef1c7529e85c")
(def ^:private expected-verifier-semantic-id
  "sha256:8def8ca24b2cf7d3df346f3fcef5af862c1130681e931d45df39a93052b19fb0")
(def ^:private expected-export-names
  '[sh23-build-policy sh23-build sh23-verify-result])
(def ^:private expected-required-functions
  '#{sh23-build-policy sh23-request-error sh23-authority-error
     sh23-package-policy-error sh23-lockfile-error sh23-environment-error
     sh23-target-error sh23-legality-error sh23-library-error
     sh23-components-error sh23-component-records-error
     sh23-artifact-request sh23-actions sh23-artifact-requests
     sh23-component-provenance sh23-diagnostic sh23-build
     sh23-verify-result})
(def ^:private expected-diagnostic-ids
  #{"PKG1001" "PKG1002" "PKG1004" "PKG1005" "PKG1006"
    "PKG2001" "PKG2004" "PKG2007"
    "PKG3003" "PKG5002" "PKG5003" "PKG5004"
    "PKG6004" "PKG7002" "PKG7003" "PKG7004" "PKG7005"
    "PKG11001" "PKG11002" "BOOT3002" "BOOT8002" "SH23-VERIFY"})
(def ^:private expected-pending-capabilities
  #{:general-semver-resolution :network-registry :arbitrary-build-plugins
    :shell-build-actions :build-action-execution
    :self-hosted-compiler-driver :seedless-bootstrap})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))
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
    (walk/postwalk (fn [entry]
                     (when (and (seq? entry) (= operator (first entry)))
                       (vswap! found conj entry))
                     entry)
                   value)
    @found))
(defn- required-var [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-07 B40 coordinator adapter is absent"
                      {:id "SH07-SH23-COVERAGE-ADAPTER-ABSENT"
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
          (fixture-relative-path "accepted" "hermetic-builds" ".gravity"))))
(def ^:private accepted-qst-plan
  (delay (compile-plan
          (fixture-relative-path "accepted" "hermetic-builds" ".qst"))))
(def ^:private rejected-gravity-plan
  (delay (compile-plan
          (fixture-relative-path "rejected" "invalid-hermetic-builds"
                                 ".gravity"))))
(def ^:private rejected-qst-plan
  (delay (compile-plan
          (fixture-relative-path "rejected" "invalid-hermetic-builds"
                                 ".qst"))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b40-sh23-package-build-source-coverage
    :compiler-artifact-plan? true}
   @plan function arguments))
(defn- invoke-engine [function arguments]
  (invoke engine-plan function arguments))
(defn- request [plan function]
  (invoke plan function []))
(defn- core-request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))
(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))
(defn- coverage [artifact]
  (let [r (core-request artifact)]
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

(deftest sh07-b40-sh23-source-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 51 (count forms)))
    (is (= 50 (count definitions) (count by-name)))
    (is (zero? (count (filter #(= 'def (first %)) definitions))))
    (is (= 50 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.self-hosting.hermetic-package-build (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= 185 (count if-calls)))
    (is (= {4 185} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b40-sh23-static-lookups-and-recursion-boundaries-are-exact
  (let [definitions (filter #(and (seq? %) (= 'defn (first %)))
                            (source-forms))
        gets (mapcat #(collect-calls 'get %) definitions)
        literal (filter #(keyword? (nth % 2 nil)) gets)
        dynamic (remove #(keyword? (nth % 2 nil)) gets)
        loops (mapcat #(collect-calls 'loop %) definitions)
        recurs (mapcat #(collect-calls 'recur %) definitions)]
    (is (= 241 (count gets)))
    (is (= 207 (count literal)))
    (is (= 34 (count dynamic)))
    (is (zero? (count loops)))
    (is (zero? (count recurs)))))

(deftest sh07-b40-sh23-source-model-executes-with-pinned-identities
  (let [plan @engine-plan
        functions (:functions plan)
        policy (invoke-engine 'sh23-build-policy [])]
    (is (= expected-plan-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= expected-functions-semantic-id
           (bootstrap/p15-s23-c11-mir-digest functions)))
    (is (= expected-builder-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh23-build))))
    (is (= expected-verifier-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh23-verify-result))))
    (is (= 50 (count functions)))
    (is (= :gravity/sh23-hermetic-build-policy (:artifact policy)))
    (is (= expected-diagnostic-ids (:diagnostics policy)))
    (is (= expected-pending-capabilities (set (:pending policy))))
    (is (contains? (:forbidden-effects policy) :build/network))
    (is (contains? (:forbidden-effects policy) :shell/exec))))

(deftest sh07-b40-sh23-accepted-fixtures-execute-and-preserve-identity
  (let [results
        (into {}
              (for [[extension plan]
                    [["gravity" accepted-gravity-plan]
                     ["qst" accepted-qst-plan]]]
                (let [build-request (request plan 'sh23-hermetic-build-request)
                      result (invoke-engine 'sh23-build [build-request])
                      verification
                      (invoke-engine 'sh23-verify-result
                                     [build-request result])
                      alternate-request
                      (request plan
                               'sh23-hermetic-build-alternate-path-request)
                      alternate-result
                      (invoke-engine 'sh23-build [alternate-request])
                      alternate-verification
                      (invoke-engine 'sh23-verify-result
                                     [alternate-request alternate-result])]
                  [extension {:request build-request
                              :result result
                              :verification verification
                              :alternate-request alternate-request
                              :alternate-result alternate-result
                              :alternate-verification
                              alternate-verification}])))]
    (doseq [[extension
             {:keys [result verification alternate-result
                     alternate-verification]}]
            results]
      (testing extension
        (is (= :accepted (:status result)))
        (is (= :accepted (:status alternate-result)))
        (is (empty? (:diagnostics result)))
        (is (empty? (:diagnostics alternate-result)))
        (is (= :passed (:status verification)))
        (is (= :passed (:status alternate-verification)))
        (is (= 3 (count (:actions result))))
        (is (= 3 (count (:artifact-requests result))))
        (is (= [:component/reader :component/analyzer :component/emitter]
               (get-in result [:build-graph :component-order])))
        (is (= (:identity-input result)
               (:identity-input alternate-result)))
        (is (not= result alternate-result))
        (is (= #{"/checkout-a/bootstrap/gravity/src/compiler.gravity"}
               (set (map :actual-source-path
                         (get-in result [:provenance :components])))))
        (is (= #{"/checkout-b/bootstrap/gravity/src/compiler.gravity"}
               (set (map :actual-source-path
                         (get-in alternate-result
                                 [:provenance :components])))))
        (is (= #{"/checkout-a/bootstrap/gravity/src/compiler.gravity"}
               (set (map :actual-source-path
                         (:artifact-requests result)))))
        (is (= #{"/checkout-b/bootstrap/gravity/src/compiler.gravity"}
               (set (map :actual-source-path
                         (:artifact-requests alternate-result)))))
        (is (= #{"/checkout-a/bootstrap/gravity/src/compiler.gravity"}
               (set (map #(get-in % [:source-map 0 :source-span :source])
                         (:artifact-requests result)))))
        (is (= #{"/checkout-b/bootstrap/gravity/src/compiler.gravity"}
               (set (map #(get-in % [:source-map 0 :source-span :source])
                         (:artifact-requests alternate-result)))))))
    (is (= (get-in results ["gravity" :request])
           (get-in results ["qst" :request])))
    (is (= (get-in results ["gravity" :result])
           (get-in results ["qst" :result])))
    (is (= (get-in results ["gravity" :alternate-request])
           (get-in results ["qst" :alternate-request])))
    (is (= (get-in results ["gravity" :alternate-result])
           (get-in results ["qst" :alternate-result])))))

(deftest sh07-b40-sh23-rejected-fixtures-and-verifier-fail-closed
  (let [results
        (for [[extension plan]
              [["gravity" rejected-gravity-plan]
               ["qst" rejected-qst-plan]]]
          (let [build-request (request plan 'sh23-online-request)
                result (invoke-engine 'sh23-build [build-request])]
            {:extension extension :request build-request :result result}))]
    (doseq [{:keys [extension result request]} results]
      (testing extension
        (is (= :rejected (:status result)))
        (is (= "PKG7003" (get-in result [:diagnostics 0 :rule])))
        (is (= :offline-lockfile-required
               (get-in result [:diagnostics 0 :reason])))
        (is (= :passed
               (:status
                (invoke-engine 'sh23-verify-result [request result]))))))
    (is (= (:request (first results)) (:request (second results))))
    (is (= (:result (first results)) (:result (second results))))
    (let [build-request
          (request accepted-gravity-plan 'sh23-hermetic-build-request)
          result (invoke-engine 'sh23-build [build-request])
          changed (assoc-in result [:actions 0 :output-id]
                            :artifact/substituted)
          verification
          (invoke-engine 'sh23-verify-result [build-request changed])]
      (is (= :rejected (:status verification)))
      (is (= "SH23-VERIFY" (get-in verification [:diagnostics 0 :rule])))
      (is (= :build-result-substitution
             (get-in verification [:diagnostics 0 :reason]))))))

(deftest sh07-b40-sh23-claim-boundary-remains-explicit
  (let [policy (invoke-engine 'sh23-build-policy [])]
    (is (= expected-pending-capabilities (set (:pending policy))))
    (is (some #{:build-action-execution} (:pending policy)))
    (is (some #{:self-hosted-compiler-driver} (:pending policy)))
    (is (some #{:seedless-bootstrap} (:pending policy)))
    (is (not (contains? policy :self-hosting-complete)))
    (is (not (contains? policy :release-ready)))
    (is (not (contains? policy :target-support-complete)))))

(deftest sh07-b40-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B40_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))]
      (is (= "SH-07-B40" (:coverage-milestone contract)))
      (is (= 36 (count (:authoritative-modules contract))))
      (is (= 32 (count (:required-core-product-counts contract))))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules
                               :sh23-package-build])))
      (is (= {:keyword-lookups 0}
             (get-in contract [:required-core-product-counts
                               :sh23-package-build]))))
    (is true "Set GRAVITY_SH07_B40_CONTRACT=1 after coordinator registration")))

(deftest sh07-b40-sh23-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B40_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 50 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 36 (count (:authoritative-modules contract))))
      (is (= 32 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B40_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b40-sh23-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B40_AUTHENTIC"))
    (let [artifact @source-artifact
          r (core-request artifact)
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
      (is (= 'gravity.self-hosting.hermetic-package-build
             (get-in r [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in r [:module :source-revision-id])
             (get-in r [:lineage :source-revision-id])))
      (is (= 50 (:fragment-count (coverage artifact))
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
    (is true "Set GRAVITY_SH07_B40_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b40-sh23-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B40_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-sh23-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/hermetic_package_build.qst")
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
    (is true "Set GRAVITY_SH07_B40_PARITY=1 in an isolated 8 GiB JVM")))
