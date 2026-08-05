(ns gravity.self-hosting.sh07-resolution-source-coverage-test
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
         "gravity/self_hosting/sh07_resolution_source_coverage_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 resolution source test is not on the classpath"
                      {:id "SH07-RESOLUTION-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-RESOLUTION-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/resolution.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-06")
(def ^:private expected-source-byte-count 77209)
(def ^:private expected-source-revision-id
  "sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56")
(def ^:private expected-plan-semantic-id
  "sha256:ba8c292ffe3c703bbc220af0a7121496c134793d1e80a012f6fb0a0d9dc6b6fa")
(def ^:private expected-functions-semantic-id
  "sha256:a1445872cda26a9aa0fb8959165eada593619de7a03f527a8ea8a8c216237b91")
(def ^:private expected-builder-semantic-id
  "sha256:154d52c95bc0488e5ec302ce9219b57d72995f5f54f137816ca980d97bf71f38")
(def ^:private expected-template-verifier-semantic-id
  "sha256:de579ce14c12644979fe636d5f7b32d02c505753b85463b8a1994eb9de7a03ec")
(def ^:private expected-resolved-verifier-semantic-id
  "sha256:80e3c1e5313ffb38805eba60110dc80edccb50218eaabd5edba98b2025e99342")
(def ^:private expected-export-names
  '[sh06-resolution-contract
    sh06-resolution-bounds
    sh06-resolution-diagnostic-catalog
    sh06-build-resolution-template
    sh06-verify-resolution-template
    sh06-verify-resolution-resolved])
(def ^:private expected-data-definition-names
  '#{sh06-resolution-contract
     sh06-resolution-bounds
     sh06-resolution-diagnostic-catalog})
(def ^:private expected-required-functions
  '#{sh06-request-shape? sh06-all-record-shapes?
     sh06-reference-candidates sh06-first-rejection-check
     sh06-rejected-result sh06-accepted-result
     sh06-build-resolution-template sh06-analysis-shape?
     sh06-digest-request-sequence? sh06-analysis-cross-fields?
     sh06-verify-resolution-template sh06-verify-resolution-resolved})
(def ^:private expected-diagnostic-ids
  #{"C5-UNRESOLVED" "C5-AMBIGUOUS" "C5-PRIVATE" "C5-ALIAS"
    "C5-SHADOW" "C5-CYCLE" "C5-CROSS-PROFILE" "C5-CAPABILITY"
    "C5-TARGET" "C5-FOREIGN"})
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
(defn- fixture-path [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))
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
      (throw (ex-info "Required SH-07 B43 coordinator adapter is absent"
                      {:id "SH07-RESOLUTION-COVERAGE-ADAPTER-ABSENT"
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
(defn- invoke-engine [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh07-b43-resolution-source-coverage
    :compiler-artifact-plan? true}
   @engine-plan function arguments))
(defn- fixture-request [family basename extension]
  (let [source-path (fixture-path family basename extension)
        macro-artifact ((required-var 'sh05-macro-file-artifact) source-path)]
    ((required-var 'sh06-resolution-request) source-path macro-artifact)))
(defn- resolve-template [source-path template]
  (let [raw-analysis (:namespace-analysis-template template)
        digest-requests (:digest-requests template)
        resolved-digests
        ((required-var 'sh06-resolution-resolve-digest-requests!)
         source-path raw-analysis digest-requests)
        resolved-analysis
        ((required-var 'sh06-resolution-resolve-analysis!)
         source-path raw-analysis resolved-digests)]
    {:raw-analysis raw-analysis
     :digest-requests digest-requests
     :resolved-digests resolved-digests
     :resolved-analysis resolved-analysis}))
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

(deftest sh07-b43-resolution-source-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        contract (nth (get by-name 'sh06-resolution-contract) 2)
        bounds (nth (get by-name 'sh06-resolution-bounds) 2)
        catalog (nth (get by-name 'sh06-resolution-diagnostic-catalog) 2)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 98 (count forms)))
    (is (= 97 (count definitions) (count by-name)))
    (is (= 3 (count (filter #(= 'def (first %)) definitions))))
    (is (= 94 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.resolution (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :name-resolution (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= expected-data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= :gravity/sh06-resolution-contract (:artifact contract)))
    (is (= :gravity/sh06-authenticated-c4-resolution-request
           (:input contract)))
    (is (= :gravity/sh06-namespace-analysis-artifact
           (:resolved-output contract)))
    (is (= :gravity/sh06-to-c5-resolution-products-v1
           (:coordinator-adapter contract)))
    (is (= 65536 (:maximum-references bounds)))
    (is (= 8192 (:maximum-digest-requests bounds)))
    (is (= expected-diagnostic-ids (set (:diagnostics catalog))))
    (is (= {4 341} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b43-resolution-lookups-and-recursion-boundaries-are-exact
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
    (is (= 487 (count gets)))
    (is (= 482 (count literal)))
    (is (= 5 (count dynamic)))
    (is (= {'(first remaining) 1 'ordinal 3 'artifact-ordinal 1}
           (frequencies (map #(nth % 2 nil) dynamic))))
    (is (= 69 (count loops)))
    (is (= 96 (count recurs)))
    (is (empty? self-recursive))
    (is (zero? (count (mapcat keyword-headed-calls definitions))))))

(deftest sh07-b43-resolution-source-model-executes-with-pinned-identities
  (let [plan @engine-plan
        functions (:functions plan)
        malformed (invoke-engine 'sh06-build-resolution-template [nil])]
    (is (= expected-plan-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= expected-functions-semantic-id
           (bootstrap/p15-s23-c11-mir-digest functions)))
    (is (= expected-builder-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh06-build-resolution-template))))
    (is (= expected-template-verifier-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh06-verify-resolution-template))))
    (is (= expected-resolved-verifier-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'sh06-verify-resolution-resolved))))
    (is (= 94 (count functions)))
    (is (= :rejected (:status malformed)))
    (is (= "C5-UNRESOLVED" (get-in malformed [:diagnostics 0 :rule])))
    (is (= :malformed-authoritative-request
           (get-in malformed [:diagnostics 0 :facts :reason])))
    (is (true? (get-in malformed
                       [:containment :downstream-artifacts-forbidden])))))

(deftest sh07-b43-sh06-accepted-pair-runs-fresh-builder-and-verifiers
  (let [cases
        (into {}
              (for [extension [".gravity" ".qst"]]
                (let [source-path
                      (fixture-path "accepted" "resolution-order" extension)
                      request
                      (fixture-request "accepted" "resolution-order" extension)
                      template
                      (invoke-engine 'sh06-build-resolution-template [request])
                      resolved (resolve-template source-path template)
                      template-verification
                      (invoke-engine
                       'sh06-verify-resolution-template
                       [(:raw-analysis resolved) (:digest-requests resolved)])
                      resolved-verification
                      (invoke-engine
                       'sh06-verify-resolution-resolved
                       [(:resolved-analysis resolved)
                        (:digest-requests resolved)
                        (:resolved-digests resolved)])]
                  [extension {:request request :template template
                              :resolved resolved
                              :template-verification template-verification
                              :resolved-verification resolved-verification}])))]
    (doseq [[extension {:keys [template resolved template-verification
                               resolved-verification]}] cases]
      (testing extension
        (is (= :accepted (:status template)))
        (is (empty? (:diagnostics template)))
        (is (= :passed (:status template-verification)))
        (is (= :passed (:status resolved-verification)))
        (is (seq (get-in resolved [:resolved-analysis :binding-table])))
        (is (seq (get-in resolved [:resolved-analysis :resolution-table])))
        (is (every? #(re-matches #"sha256:[0-9a-f]{64}" %)
                    (:resolved-digests resolved)))))
    (let [gravity (get cases ".gravity")
          qst (get cases ".qst")]
      (is (= (get-in gravity [:resolved :resolved-analysis
                              :identity-preimage])
             (get-in qst [:resolved :resolved-analysis
                          :identity-preimage])))
      (is (= (get-in gravity [:resolved :resolved-analysis :artifact-id])
             (get-in qst [:resolved :resolved-analysis :artifact-id])))
      (is (not= (get-in gravity [:request :provenance :actual-source-path])
                (get-in qst [:request :provenance :actual-source-path]))))))

(deftest sh07-b43-sh06-rejected-pair-and-altered-products-fail-closed
  (let [rejections
        (into {}
              (for [extension [".gravity" ".qst"]]
                (let [request
                      (fixture-request "rejected" "unresolved" extension)
                      result
                      (invoke-engine 'sh06-build-resolution-template [request])]
                  [extension {:request request :result result}])))]
    (doseq [[extension {:keys [result]}] rejections]
      (testing extension
        (is (= :rejected (:status result)))
        (is (= "C5-UNRESOLVED" (get-in result [:diagnostics 0 :rule])))
        (is (= :no-legal-binding
               (get-in result [:diagnostics 0 :facts :reason])))
        (is (= 'missing-binding (get-in result [:diagnostics 0 :symbol])))
        (is (true? (get-in result
                           [:containment :downstream-artifacts-forbidden])))))
    (is (= (get-in rejections [".gravity" :result :digest-requests 0
                               :preimage])
           (get-in rejections [".qst" :result :digest-requests 0
                               :preimage])))
    (let [source-path
          (fixture-path "accepted" "resolution-order" ".gravity")
          request (fixture-request "accepted" "resolution-order" ".gravity")
          template (invoke-engine 'sh06-build-resolution-template [request])
          resolved (resolve-template source-path template)
          altered-raw
          (assoc-in (:raw-analysis resolved) [:binding-table 0 :name]
                    'changed-binding)
          altered-resolved
          (assoc-in (:resolved-analysis resolved) [:artifact-id] zero-id)
          raw-check
          (invoke-engine 'sh06-verify-resolution-template
                         [altered-raw (:digest-requests resolved)])
          resolved-check
          (invoke-engine 'sh06-verify-resolution-resolved
                         [altered-resolved (:digest-requests resolved)
                          (:resolved-digests resolved)])]
      (is (= :failed (:status raw-check)))
      (is (= "C5-UNRESOLVED" (:rule raw-check)))
      (is (= :failed (:status resolved-check)))
      (is (= "C5-UNRESOLVED" (:rule resolved-check))))))

(deftest sh07-b43-resolution-claim-boundary-remains-explicit
  (let [definitions (into {} (map (juxt second identity))
                          (filter #(and (seq? %)
                                        (#{'def 'defn} (first %)))
                                  (source-forms)))
        contract (nth (get definitions 'sh06-resolution-contract) 2)
        ns-form (first (source-forms))
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        claim-boundary (set (get-in clauses
                                    [:metadata :bootstrap :claim-boundary]))]
    (is (= [:authenticate-c4-request
            :resolve-digest-requests
            :bind-c5-artifact]
           (:host-authority contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:whole-language? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (contains? claim-boundary :normalized-c4-input-required))
    (is (contains? claim-boundary :not-package-discovery))
    (is (contains? claim-boundary :not-type-or-effect-checking))
    (is (contains? claim-boundary :not-seed-retirement))
    (is (not (contains? contract :self-hosting-complete)))
    (is (not (contains? contract :release-ready)))))

(deftest sh07-b43-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B43_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          product-counts (:required-core-product-counts contract)]
      (is (= "SH-07-B43" (:coverage-milestone contract)))
      (is (= 39 (count (:authoritative-modules contract))))
      (is (= 35 (count product-counts)))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules :resolution])))
      (is (= {:keyword-lookups 0}
             (get-in product-counts [:resolution])))
      (is (= expected-b16-cohort-product-counts
             (select-keys product-counts
                          (keys expected-b16-cohort-product-counts))))
      (is (= 63 (reduce + (map (comp :keyword-lookups val)
                                expected-b16-cohort-product-counts)))))
    (is true "Set GRAVITY_SH07_B43_CONTRACT=1 after coordinator registration")))

(deftest sh07-b43-resolution-measured-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B43_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 97 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 39 (count (:authoritative-modules contract))))
      (is (= 35 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B43_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b43-resolution-authentic-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B43_AUTHENTIC"))
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
      (is (= 'gravity.resolution (get-in request [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in request [:module :source-revision-id])
             (get-in request [:lineage :source-revision-id])))
      (is (= 97 (:fragment-count (coverage artifact))
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
    (is true "Set GRAVITY_SH07_B43_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b43-resolution-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B43_PARITY"))
    (let [temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-resolution-coverage-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/resolution.qst")
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
    (is true "Set GRAVITY_SH07_B43_PARITY=1 in an isolated 8 GiB JVM")))
