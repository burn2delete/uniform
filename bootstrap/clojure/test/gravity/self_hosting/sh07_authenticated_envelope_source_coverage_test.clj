(ns gravity.self-hosting.sh07-authenticated-envelope-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh02-authenticated-envelope-test]
            [gravity.self-hosting.sh07-proof-census :as proof-census]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_authenticated_envelope_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07 authenticated-envelope test is not on the classpath"
                {:id "SH07-AUTHENTICATED-ENVELOPE-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-AUTHENTICATED-ENVELOPE-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private source-relative-path
  "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 59495)
(def ^:private expected-source-revision-id
  "sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71")
(def ^:private expected-plan-semantic-id
  "sha256:125e012806bddf996f23e357bd33309c9bbd40927ce0f2c841e69b39c1740922")
(def ^:private expected-functions-semantic-id
  "sha256:e88f53c2994f5d8d4577f6df9f6531a750c2808ef0a2b4b5d297f64e7b45e26e")
(def ^:private expected-builder-semantic-id
  "sha256:19b21ed1e94563631c25b502f5297fa1e33070f0a62fefc480bcba5984b02a7a")
(def ^:private expected-verifier-semantic-id
  "sha256:e52b201a81ef82f857aaabc68cb2d6a8f0f4505f853c555816023f5dad294a77")
(def ^:private expected-export-names
  '[authenticated-envelope-contract
    authenticated-envelope-bounds
    authenticated-envelope-build-template
    authenticated-envelope-verify-template])
(def ^:private expected-data-definition-names
  '#{authenticated-envelope-contract authenticated-envelope-bounds})
(def ^:private expected-required-functions
  '#{authenticated-envelope-build-template
     authenticated-envelope-verify-template
     ae-descriptor-validation ae-build-accepted-template ae-rejection
     ae-build-projection-bindings ae-build-fact-transition-bindings
     ae-build-identity-bindings ae-reference-closure-valid?
     ae-path-neutral-semantic-value? ae-add-digest-request})
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
(def ^:private sh02-test-namespace
  'gravity.self-hosting.sh02-authenticated-envelope-test)

(defn- path [relative]
  (str (.resolve @root relative)))

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
       (ex-info "Required SH-07 B42 coordinator adapter is absent"
                {:id "SH07-AUTHENTICATED-ENVELOPE-COVERAGE-ADAPTER-ABSENT"
                 :symbol symbol}))))

(defn- sh02-var [symbol]
  (or (ns-resolve sh02-test-namespace symbol)
      (throw
       (ex-info "Required genuine SH-02 fixture helper is absent"
                {:id "SH07-AUTHENTICATED-ENVELOPE-SH02-HELPER-ABSENT"
                 :symbol symbol}))))

(defn- sh02-call [symbol & arguments]
  (apply (sh02-var symbol) arguments))

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
   {:engine :gravity-sh07-b42-authenticated-envelope-source-coverage
    :compiler-artifact-plan? true}
   @engine-plan function arguments))

(defn- fixture-case [relative]
  (sh02-call 'fixture-case relative))

(defn- descriptor [fixture]
  (sh02-call 'descriptor fixture))

(defn- seal [raw]
  (sh02-call 'seal-builder-result! raw))

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

(deftest sh07-b42-authenticated-envelope-contract-and-static-shape-are-exact
  (let [forms (source-forms)
        ns-form (first forms)
        clauses (into {} (map (fn [clause] [(first clause) (second clause)]))
                      (drop 2 ns-form))
        definitions (filter #(and (seq? %) (#{'def 'defn} (first %))) forms)
        by-name (into {} (map (juxt second identity)) definitions)
        contract (nth (get by-name 'authenticated-envelope-contract) 2)
        bounds (nth (get by-name 'authenticated-envelope-bounds) 2)
        metadata (get-in clauses [:metadata :bootstrap])
        if-calls (mapcat #(collect-calls 'if %) definitions)]
    (is (= 75 (count forms)))
    (is (= 74 (count definitions) (count by-name)))
    (is (= 2 (count (filter #(= 'def (first %)) definitions))))
    (is (= 72 (count (filter #(= 'defn (first %)) definitions))))
    (is (= 'gravity.compiler.authenticated-envelope (second ns-form)))
    (is (= :meta (:profile clauses)))
    (is (= :jvm (:target clauses)))
    (is (= expected-export-names (:exports clauses)))
    (is (= #{} (:effects clauses)))
    (is (= #{} (:capabilities clauses)))
    (is (= :safe (:safety clauses)))
    (is (= :authenticated-envelope (:component metadata)))
    (is (= :gravity-source (:owner metadata)))
    (is (true? (:ambient-authority-denied metadata)))
    (is (= expected-data-definition-names
           (set (map second (filter #(= 'def (first %)) definitions)))))
    (is (set/subset? expected-required-functions (set (keys by-name))))
    (doseq [name expected-export-names]
      (is (contains? by-name name) (str "missing export " name)))
    (is (= :gravity/authenticated-envelope-contract (:artifact contract)))
    (is (= :gravity/authenticated-envelope-semantic-root-v1
           (:semantic-root-domain contract)))
    (is (= :gravity/authenticated-envelope-provenance-binding-v1
           (:provenance-root-domain contract)))
    (is (= 65536 (:maximum-carrier-nodes bounds)
           (:maximum-scalar-bytes bounds)))
    (is (= {4 278} (frequencies (map count if-calls))))
    (is (= expected-source-byte-count
           (alength (source-bytes (path source-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path source-relative-path)))))))

(deftest sh07-b42-authenticated-envelope-lookups-and-recursion-are-exact
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
    (is (= 270 (count gets)))
    (is (= 267 (count literal)))
    (is (= 3 (count dynamic)))
    (is (= #{'map-key} (set (map #(nth % 2 nil) dynamic))))
    (is (zero? (count loops)))
    (is (zero? (count recurs)))
    (is (= 31 (count self-recursive)))
    (is (zero? (count (mapcat keyword-headed-calls definitions))))))

(deftest sh07-b42-authenticated-envelope-source-model-has-pinned-identities
  (let [plan @engine-plan
        functions (:functions plan)
        malformed (invoke-engine 'authenticated-envelope-build-template [nil])]
    (is (= expected-plan-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input
             plan))))
    (is (= expected-functions-semantic-id
           (bootstrap/p15-s23-c11-mir-digest functions)))
    (is (= expected-builder-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'authenticated-envelope-build-template))))
    (is (= expected-verifier-semantic-id
           (bootstrap/p15-s23-c11-mir-digest
            (get functions 'authenticated-envelope-verify-template))))
    (is (= 72 (count functions)))
    (is (= :rejected (:status malformed)))
    (is (= :descriptor-shape (get-in malformed [:diagnostics 0 :reason])))
    (is (true? (get-in malformed
                       [:containment :downstream-artifacts-forbidden])))))

(deftest sh07-b42-sh02-paired-fixtures-replay-with-separated-path-provenance
  (let [cases
        (into {}
              (for [[extension relative]
                    [["gravity" "accepted/envelope-comparison.gravity"]
                     ["qst" "accepted/envelope-comparison.qst"]]]
                (let [fixture (fixture-case relative)
                      descriptor (descriptor fixture)
                      raw (invoke-engine
                           'authenticated-envelope-build-template [descriptor])
                      sealed (seal raw)
                      replay
                      (invoke-engine
                       'authenticated-envelope-verify-template
                       [descriptor (:artifact-template raw)
                        (:digest-requests raw)])]
                  [extension {:fixture fixture :descriptor descriptor :raw raw
                              :sealed sealed :replay replay}])))]
    (doseq [[extension {:keys [raw replay sealed]}] cases]
      (testing extension
        (is (= :accepted (:status raw)))
        (is (= :template-replay-passed (:status replay)))
        (is (= :pending-host-resolution (:identity-enforcement replay)))
        (is (false? (:eligible-for-contextual-acceptance? replay)))
        (is (= (:semantic-root sealed)
               (get-in sealed [:template :semantic-envelope-id])))
        (is (= (:provenance-root sealed)
               (get-in sealed [:template :provenance-binding-id])))))
    (let [gravity (get cases "gravity")
          qst (get cases "qst")
          semantic-request
          (get-in gravity [:raw :digest-requests
                           (get-in gravity
                                   [:raw :semantic-envelope-root :digest-ref])])
          semantic-text (pr-str (:preimage semantic-request))]
      (is (= (get-in gravity [:fixture :source-text])
             (get-in qst [:fixture :source-text])))
      (is (= (get-in gravity [:sealed :semantic-root])
             (get-in qst [:sealed :semantic-root])))
      (is (= (get-in gravity [:sealed :identity-checks])
             (get-in qst [:sealed :identity-checks])))
      (is (not= (get-in gravity [:sealed :provenance-root])
                (get-in qst [:sealed :provenance-root])))
      (is (not= (get-in gravity [:fixture :source-path])
                (get-in qst [:fixture :source-path])))
      (is (not (str/includes?
                semantic-text (get-in gravity [:fixture :source-path]))))
      (is (not (str/includes?
                semantic-text (get-in qst [:fixture :source-path])))))))

(deftest sh07-b42-changed-paired-products-and-stale-identity-fail-closed
  (let [accepted (fixture-case "accepted/envelope-comparison.gravity")
        changed-gravity (fixture-case "rejected/mismatched-projection.gravity")
        changed-qst (fixture-case "rejected/mismatched-projection.qst")
        accepted-descriptor (descriptor accepted)
        changed-gravity-descriptor (descriptor changed-gravity)
        changed-qst-descriptor (descriptor changed-qst)
        accepted-raw
        (invoke-engine 'authenticated-envelope-build-template
                       [accepted-descriptor])
        changed-gravity-replay
        (invoke-engine
         'authenticated-envelope-verify-template
         [changed-gravity-descriptor (:artifact-template accepted-raw)
          (:digest-requests accepted-raw)])
        changed-qst-replay
        (invoke-engine
         'authenticated-envelope-verify-template
         [changed-qst-descriptor (:artifact-template accepted-raw)
          (:digest-requests accepted-raw)])
        altered-replay
        (invoke-engine
         'authenticated-envelope-verify-template
         [accepted-descriptor (:artifact-template accepted-raw)
          (assoc-in (:digest-requests accepted-raw)
                    [0 :preimage :projection-contract :target]
                    :wasm)])
        stale-descriptor
        (assoc-in accepted-descriptor
                  [:identity-subjects 0 :preimage :source-content-hash]
                  (:source-content-hash changed-gravity))
        stale-raw
        (invoke-engine 'authenticated-envelope-build-template
                       [stale-descriptor])
        stale-replay
        (invoke-engine
         'authenticated-envelope-verify-template
         [stale-descriptor (:artifact-template stale-raw)
          (:digest-requests stale-raw)])
        stale-error
        (try
          (seal stale-raw)
          nil
          (catch clojure.lang.ExceptionInfo exception exception))]
    (is (= (:source-text changed-gravity) (:source-text changed-qst)))
    (is (= (:source-content-hash changed-gravity)
           (:source-content-hash changed-qst)))
    (is (= (:plan-semantic-hash changed-gravity)
           (:plan-semantic-hash changed-qst)))
    (is (= (:functions-semantic-hash changed-gravity)
           (:functions-semantic-hash changed-qst)))
    (is (not= (:source-content-hash accepted)
              (:source-content-hash changed-gravity)))
    (is (= :rejected
           (:status changed-gravity-replay)
           (:status changed-qst-replay)))
    (is (= :artifact-template-replay
           (get-in changed-gravity-replay [:diagnostics 0 :reason])
           (get-in changed-qst-replay [:diagnostics 0 :reason])))
    (is (= changed-gravity-replay changed-qst-replay))
    (is (= :rejected (:status altered-replay)))
    (is (= :digest-request-replay
           (get-in altered-replay [:diagnostics 0 :reason])))
    (is (= :accepted (:status stale-raw)))
    (is (= :template-replay-passed (:status stale-replay)))
    (is (= :pending-host-resolution (:identity-enforcement stale-replay)))
    (is (false? (:eligible-for-contextual-acceptance? stale-replay)))
    (is (= "SH02-IDENTITY-SUBJECT" (:id (ex-data stale-error))))))

(deftest sh07-b42-clojure-seed-boundary-remains-explicit
  (let [definitions (into {} (map (juxt second identity))
                          (filter #(and (seq? %)
                                        (#{'def 'defn} (first %)))
                                  (source-forms)))
        contract (nth (get definitions 'authenticated-envelope-contract) 2)
        fixture (fixture-case "accepted/envelope-comparison.gravity")
        result (invoke-engine 'authenticated-envelope-build-template
                              [(descriptor fixture)])
        template (:artifact-template result)]
    (is (= :gravity-source (:semantic-authority template)))
    (is (= :bounded-validation-hashing-and-instantiation
           (:host-role template)))
    (is (= [:bound-carrier-validation
            :compile-and-pin-gravity-module
            :canonical-encode
            :sha256
            :validate-prior-only-reachable-digest-dag
            :resolve-digest-references
            :enforce-identity-subjects
            :instantiate-template
            :fresh-replay]
           (:host-authority contract)))
    (is (false? (:self-hosted? contract)))
    (is (false? (:whole-language? contract)))
    (is (true? (:clojure-seed-boundary? contract)))
    (is (false? (:self-hosted? template)))
    (is (true? (:clojure-seed-boundary? template)))
    (is (= [:content-and-context-consistency
            :not-a-release-signature
            :not-proof-of-verifier-correctness]
           (:claim-boundary template)))
    (is (not (contains? template :release-ready)))
    (is (not (contains? template :seed-retired)))))

(deftest sh07-b42-proof-contract-registration-is-ready-for-coordinator-gate
  (if (= "1" (System/getenv "GRAVITY_SH07_B42_CONTRACT"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          product-counts (:required-core-product-counts contract)]
      (is (= "SH-07-B42" (:coverage-milestone contract)))
      (is (= 38 (count (:authoritative-modules contract))))
      (is (= 34 (count product-counts)))
      (is (= source-relative-path
             (get-in contract [:authoritative-modules
                               :authenticated-envelope])))
      (is (= {:keyword-lookups 0}
             (get-in product-counts [:authenticated-envelope])))
      (is (= expected-b16-cohort-product-counts
             (select-keys product-counts
                          (keys expected-b16-cohort-product-counts))))
      (is (= 63 (reduce + (map (comp :keyword-lookups val)
                                expected-b16-cohort-product-counts)))))
    (is true "Set GRAVITY_SH07_B42_CONTRACT=1 after coordinator registration")))

(deftest sh07-b42-authenticated-envelope-census-fits-declared-bounds
  (if (= "1" (System/getenv "GRAVITY_SH07_B42_CENSUS"))
    (let [contract (edn/read-string
                    (slurp (path proof-contract-relative-path)))
          bounds (:bounds contract)
          result (proof-census/census (path source-relative-path))
          measurements (:measurements result)]
      (is (= :gravity/sh07-proof-census (:artifact result)))
      (is (= :within-declared-bounds (:status result)))
      (is (= expected-source-revision-id
             (get-in result [:request :source-revision-id])))
      (is (= 74 (:fragments measurements) (:top-level-forms measurements)))
      (is (<= (:maximum-fragment-forms measurements)
              (:maximum-fragment-forms bounds)))
      (is (<= (:carrier-nodes measurements)
              (:maximum-module-carrier-nodes bounds)))
      (is (empty? (:violations result)))
      (is (false? (:performed-sh07-lowering? result)))
      (is (= 38 (count (:authoritative-modules contract))))
      (is (= 34 (count (:required-core-product-counts contract)))))
    (is true "Set GRAVITY_SH07_B42_CENSUS=1 in an isolated 8 GiB JVM")))

(deftest sh07-b42-authenticated-source-core-and-reduced-replay
  (if (= "1" (System/getenv "GRAVITY_SH07_B42_AUTHENTIC"))
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
      (is (= 'gravity.compiler.authenticated-envelope
             (get-in request [:module :namespace])))
      (is (= expected-source-revision-id
             (get-in request [:module :source-revision-id])
             (get-in request [:lineage :source-revision-id])))
      (is (= 74 (:fragment-count (coverage artifact))
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
    (is true "Set GRAVITY_SH07_B42_AUTHENTIC=1 in an isolated 8 GiB JVM")))

(deftest sh07-b42-authenticated-envelope-cross-root-extension-parity
  (if (= "1" (System/getenv "GRAVITY_SH07_B42_PARITY"))
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-authenticated-envelope-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/authenticated_envelope.qst")
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
    (is true "Set GRAVITY_SH07_B42_PARITY=1 in an isolated 8 GiB JVM")))
