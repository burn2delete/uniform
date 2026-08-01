(ns gravity.self-hosting.sh07-c12-domain-ir-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c12_domain_ir_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C12 domain IR source test is not on the classpath"
        {:id "SH07-C12-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C12-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c12-relative-path
  "bootstrap/gravity/src/gravity/compiler/c12_domain_ir_architecture.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 61946)
(def ^:private expected-source-revision-id
  "sha256:45447d313f8cd6bf0c082e04c49f87a98cff33c9c5bcf582b968336eb27ff0e0")
(def ^:private expected-sh06-semantic-projection-id
  "sha256:8a57d56f28dfad5055ef0b1b3bb695dc4beed8473e0d4b212ba7a7bacc0d97ee")
(def ^:private expected-coverage
  {:fragment-count 85
   :root-form-count 85
   :form-count 5711
   :binding-count 569
   :resolution-count 2073})
(def ^:private expected-core-census
  {:core-node-count 4591
   :definition-count 85
   :call-count 807
   :reference-count 1517
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 1589
    :collection-literal 122
    :def 85
    :reference 1517
    :call 807
    :if 325
    :let 38
    :loop 10
    :recur 18
    :quote 4
    :fn 76}})
(def ^:private expected-census-measurements
  {:fragments 85
   :top-level-forms 85
   :forms 5711
   :bindings 569
   :resolutions 2073
   :maximum-fragment-forms 302
   :maximum-fragment-resolutions 135
   :maximum-fragment-local-bindings 28
   :maximum-fragment-external-bindings 23
   :maximum-fragment-root-forms 1
   :maximum-form-children 50
   :maximum-form-depth 21
   :carrier-nodes 383280
   :carrier-depth 23
   :carrier-width 5711
   :carrier-exact-utf8-scalar-bytes 6372295
   :carrier-scalar-bytes 25472240
   :predicted-maximum-core-nodes 5711
   :predicted-maximum-digest-requests 5715})
(def ^:private expected-export-names
  '[c12-domain-ir-contract
    c12-domain-ir-registry-contract
    c12-domain-ir-artifact-schema-contract
    c12-semantic-anchor-contract
    c12-domain-lowering-contract
    c12-domain-fallback-contract
    c12-domain-proof-contract
    c12-plugin-boundary-contract
    c12-domain-ir-diagnostic-catalog
    build-c12-domain-registration
    build-c12-semantic-anchor
    build-c12-domain-artifact
    verify-c12-domain-ir-architecture
    sh14-layout-policy
    sh14-build-layout
    sh14-verify-layout])
(def ^:private data-definition-names
  '#{c12-domain-ir-contract
     c12-domain-ir-registry-contract
     c12-domain-ir-artifact-schema-contract
     c12-semantic-anchor-contract
     c12-domain-lowering-contract
     c12-domain-fallback-contract
     c12-domain-proof-contract
     c12-plugin-boundary-contract
     c12-domain-ir-diagnostic-catalog})
(def ^:private quoted-definition-names
  '#{build-c12-domain-registration build-c12-semantic-anchor
     build-c12-domain-artifact verify-c12-domain-ir-architecture})
(def ^:private executable-definition-names
  '#{sh14-layout-policy sh14-member? sh14-unique-from? sh14-unique?
     sh14-common-valid? sh14-shape-valid? sh14-bounds-valid?
     sh14-allocation-valid? sh14-size-valid? sh14-v2-range? sh14-v2-byte?
     sh14-v2-byte-vector-from? sh14-v2-byte-vector? sh14-v2-continuation?
     sh14-v2-utf8-from? sh14-v2-utf8? sh14-v2-character-member-from?
     sh14-v2-hex-from? sh14-v2-sha256-id? sh14-v2-point-valid?
     sh14-v2-source-span-valid? sh14-v2-generated-origin-valid?
     sh14-v2-origin-entry-valid? sh14-v2-origin-from-valid?
     sh14-v2-origin-chain-valid? sh14-v3-exact-keys? sh14-v3-not
     sh14-v3-request-keys sh14-v3-aggregate? sh14-v3-aggregate-components
     sh14-v3-structural-preflight sh14-v3-identifier-character?
     sh14-v3-ascii-identifier? sh14-v3-canonical-identifier?
     sh14-v3-canonical-scalar? sh14-v3-canonical-vector?
     sh14-v3-keyword-vector? sh14-v3-canonical-key-vector?
     sh14-v3-byte-vector? sh14-v3-utf8? sh14-v3-source-id?
     sh14-v3-point-valid? sh14-v3-source-span-valid?
     sh14-v3-generated-origin-valid-at? sh14-v3-origin-entry-valid?
     sh14-v3-origin-chain-valid? sh14-v3-profile-target-valid?
     sh14-v2-common-error sh14-v2-variant-schema-from? sh14-v2-variant-valid?
     sh14-v2-shape-error sh14-v2-bounds-error sh14-v2-aligned-size
     sh14-v3-kind-element-size-valid? sh14-v2-size-error sh14-v2-error
     sh14-error sh14-rule sh14-family sh14-missing-fact sh14-diagnostic
     sh14-identity-point sh14-identity-source-span
     sh14-identity-generated-origin sh14-identity-origin-entry
     sh14-identity-origins-from sh14-identity-origins sh14-identity-input
     sh14-layout sh14-v3-structural-diagnostic sh14-build-layout
     sh14-verify-layout})
(def ^:private expected-definition-names
  (set/union data-definition-names quoted-definition-names
             executable-definition-names))
(def ^:private expected-document-ids
  ["C12" "C11" "C13" "C14" "C15" "C17" "C18" "MATH3" "MATH4"
   "SAFE1" "SAFE15" "D1" "D3" "D6" "D8" "D9" "P1" "P11" "B1"
   "B11" "L19" "S1" "PKG1" "PKG3" "BOOT1" "BOOT2" "BOOT3"
   "BOOT4" "BOOT5" "BOOT6" "BOOT7" "BOOT8" "TEST2"])
(def ^:private expected-contract-value-hashes
  {'c12-domain-ir-contract
   "sha256:b623a3a8af864c57e14918db024ca8f5bf658cba4f9f42a2ec14af1505b86e7d"
   'c12-domain-ir-registry-contract
   "sha256:498cdc442645eec40acafda5c28ed77c5749fc0c704bc2aa1f156adc5d9ea294"
   'c12-domain-ir-artifact-schema-contract
   "sha256:8fb3ecfe32c85b7a55f1b86dd6da38f698049747fd5661971b6bac83ef0307db"
   'c12-semantic-anchor-contract
   "sha256:4560cb43325ace95073af9dcd9138e9065f6b953c175c5d4ba167c8df0b32d30"
   'c12-domain-lowering-contract
   "sha256:fc53cfbac087fe5b4d74af2e3e576c7c690155729d09908879e019d31c9f04c6"
   'c12-domain-fallback-contract
   "sha256:7f678b811d4b8073d4f35ed3688cb15b3f64bd63c4e461f804bbb411ca078c2a"
   'c12-domain-proof-contract
   "sha256:22667cfba505a3b24c72d57917dc534777980dfd974bcc204d7967be168408ae"
   'c12-plugin-boundary-contract
   "sha256:e951fbdb7e4ba06f375ba673ae11c16cb12058b3d89dd640b9381fd39ca98ae8"
   'c12-domain-ir-diagnostic-catalog
   "sha256:eda2412724894f415fe6c0f4ed0f61591abff0639d6517d1b03e9886a7964556"})
(def ^:private expected-diagnostic-catalog
  {:diagnostics ["C12-REGISTRATION" "C12-ANCHOR" "C12-SCHEMA" "C12-FACTS"
                 "C12-VERIFY" "C12-PROOF" "C12-LOWERING" "C12-FALLBACK"
                 "C12-PLUGIN"]
   :stable-diagnostic-fields
   [:diagnostic-id :severity :domain :artifact-id :source-span
    :semantic-anchor :owner-doc :profile :target :verifier :missing-fact
    :proof-id :fallback-status :plugin-id :remediation]
   :stops [:optimization :target-lowering :runtime-selection :package-release
           :self-hosting-stage-claim]
   :must-not-depend-on [:localized-rendering-text :backend-private-state
                        :provider-claim :plugin-host-runtime
                        :opaque-domain-payload]})
(def ^:private expected-policy
  {:artifact :gravity/sh14-layout-policy
   :version 1
   :kinds #{:variant :symbol :tuple :string :vector :keyword :record
            :mutable-buffer :bytes :set :map}
   :allocation-regimes #{:bounded :persistent :static :region :stack}
   :alignments #{1 2 4 8 16 32 64}
   :integer-width-bits 64
   :maximum-integer 9223372036854775807
   :maximum-capacity 4096
   :maximum-element-size 64
   :maximum-size-bytes 131072
   :maximum-variant-tags 64
   :maximum-origin-count 64
   :maximum-generated-origin-depth 64
   :maximum-identifier-units 256
   :supported-profile-target-pairs #{[:safe :portable] [:meta :portable-mir]}
   :size-policy :capacity-times-element-size-rounded-up-to-alignment
   :structural-bounds
   {:maximum-nodes 32768 :maximum-depth 96 :maximum-width 8192
    :maximum-collections 8192 :maximum-scalars 24576
    :maximum-scalar-units 262144}
   :diagnostics #{"C12-SCHEMA" "SAFE2-BOUNDS" "L10-HIDDEN-ALLOC"
                  "S7-LAYOUT" "C12-FACTS" "C12-VERIFY"}
   :pending [:authenticated-sh12-mir-input :target-specific-layout
             :actual-allocation :field-offset-calculation
             :pointer-and-lifetime-layouts]})
(def ^:private expected-b16-bounds
  {:maximum-module-forms 65536
   :maximum-module-core-nodes 65536
   :maximum-fragments 1024
   :maximum-top-level-forms 1024
   :maximum-fragment-forms 1024
   :maximum-form-children 1024
   :maximum-form-depth 256
   :maximum-bindings 2048
   :maximum-alias-records 256
   :maximum-fragment-resolutions 2048
   :maximum-module-resolutions 65536
   :maximum-origin-entries 256
   :maximum-metadata-entries 256
   :maximum-effects 128
   :maximum-capabilities 128
   :maximum-exports 1024
   :maximum-module-carrier-nodes 8388608
   :maximum-module-carrier-depth 256
   :maximum-module-carrier-width 65536
   :maximum-module-scalar-bytes 268435456
   :maximum-template-carrier-nodes 16777216
   :maximum-template-carrier-depth 256
   :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 536870912
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 536870912
   :maximum-generated-digest-carrier-nodes 8388608
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 536870912
   :maximum-module-digest-requests 65540})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE" "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B32 coordinator adapter is absent"
        {:id "SH07-C12-COVERAGE-ADAPTER-ABSENT" :symbol symbol}))))

(defn- source-bytes [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-forms
  []
  (with-open
   [reader (clojure.lang.LineNumberingPushbackReader.
            (io/reader (path c12-relative-path)))]
    (loop [forms []]
      (let [form (read {:eof ::eof} reader)]
        (if (= ::eof form) forms (recur (conj forms form)))))))

(defn- named-top-level-form [forms kind name]
  (first (filter #(and (seq? %) (= kind (first %)) (= name (second %))) forms)))

(defn- quoted-body [definition-form]
  (let [body (nth definition-form 3 nil)]
    (when (and (seq? body) (= 'quote (first body))) (second body))))

(defn- collect-calls
  [operator value]
  (let [found (volatile! [])]
    (walk/postwalk
     (fn [entry]
       (when (and (seq? entry) (= operator (first entry)))
         (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- symbols-in
  [value]
  (let [found (volatile! #{})]
    (walk/postwalk
     (fn [entry]
       (when (symbol? entry) (vswap! found conj entry))
       entry)
     value)
    @found))

(defn- sha256-id
  [bytes]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256") bytes)]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- value-sha256-id [value]
  (sha256-id (.getBytes (pr-str value) "UTF-8")))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream (java.nio.file.Files/walk
                       root-path (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- request [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))
(defn- core [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))
(defn- identity-input [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)]
    {:fragment-count (count (:fragment-manifest authenticated-request))
     :root-form-count (count (:top-level-form-ids authenticated-request))
     :form-count (count (:forms authenticated-request))
     :binding-count (count (:binding-table authenticated-request))
     :resolution-count (count (:resolution-table authenticated-request))}))

(defn- core-census
  [artifact]
  (let [core-artifact (core artifact)
        nodes (:nodes core-artifact)]
    {:core-node-count (count nodes)
     :definition-count (count (:definitions core-artifact))
     :call-count (count (:calls core-artifact))
     :reference-count (count (:reference-uses core-artifact))
     :keyword-lookup-count (count (:keyword-lookups core-artifact))
     :core-form-frequencies (frequencies (map :core-form nodes))}))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B32 records are not uniquely identifiable"
        {:id "SH07-C12-COVERAGE-AMBIGUOUS-INDEX" :key key-name})))
    index))

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error {:class (.getName (class throwable))
                        :message (.getMessage throwable)}})))

(defn- diagnostic-data
  [result]
  (let [data (:exception-data result)
        value (:value result)]
    (or (when (= :gravity/sh07-core-diagnostic (:artifact data)) data)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(def ^:private c12-artifact
  (delay ((required-var 'sh07-core-file-artifact) (path c12-relative-path))))

(def ^:private c12-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c12-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c12-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c12_domain_ir_architecture.qst")
          left-path (path c12-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write right-path bytes
                                   (make-array java.nio.file.OpenOption 0))
        {:left @c12-artifact
         :right ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally (delete-tree! temp-root))))))

(deftest sh07-b32-proof-contract-registers-c12-source-exactly
  (let [contract (edn/read-string (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        nonclaims (set (:nonclaims contract))]
    (is (= "SH-07-B32" (:coverage-milestone contract)))
    (is (= c12-relative-path
           (get-in contract [:authoritative-modules :c12-domain-ir])))
    (is (= {:keyword-lookups 0}
           (get-in contract [:required-core-product-counts :c12-domain-ir])))
    (is (= expected-b16-bounds (:bounds contract)))
    (is (= 15 (get-in contract [:boundary :request-schema-version])))
    (doseq [document
            ["docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md"
             "docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md"
             "docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md"
             "docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md"
             "docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md"
             "docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md"
             "docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md"
             "docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md"]]
      (is (contains? documents document)))
    (doseq [nonclaim
            [:c12-production-domain-ir-execution
             :c12-contract-and-diagnostic-schema-enforcement
             :sh14-authenticated-sh12-mir-input
             :sh14-target-specific-layout
             :sh14-actual-allocation
             :sh14-field-offset-calculation
             :sh14-pointer-and-lifetime-layouts
             :sh14-complete :sh07-complete :seed-retirement
             :self-hosting-complete]]
      (is (contains? nonclaims nonclaim)))))

(deftest sh07-b32-c12-source-metadata-contracts-catalog-and-policy-are-exact
  (let [forms (source-forms)
        namespace-form (first forms)
        namespace-clauses
        (into {} (map (fn [clause] [(first clause) (second clause)]))
              (drop 2 namespace-form))
        metadata (get-in namespace-clauses [:metadata :bootstrap])
        definitions
        (into {} (for [name expected-definition-names]
                   [name (or (named-top-level-form forms 'def name)
                             (named-top-level-form forms 'defn name))]))
        executable
        (set (for [[name form] definitions
                   :when (and (= 'defn (first form)) (nil? (quoted-body form)))]
               name))
        policy (nth (get definitions 'sh14-layout-policy) 3)
        if-calls (mapcat #(collect-calls 'if %) (vals definitions))]
    (is (= 86 (count forms)))
    (is (= 'gravity.compiler.c12-domain-ir-architecture
           (second namespace-form)))
    (is (= :meta (:profile namespace-clauses)))
    (is (= :jvm (:target namespace-clauses)))
    (is (= expected-export-names (:exports namespace-clauses)))
    (is (= #{} (:effects namespace-clauses)))
    (is (= #{} (:capabilities namespace-clauses)))
    (is (= :safe (:safety namespace-clauses)))
    (is (= expected-document-ids (:documents metadata)))
    (is (= {:stage :stage1 :component :domain-ir-architecture
            :owner :gravity-source :source-language :gravity
            :seed :clojure-stage0 :retirement-objective :replace-clojure-seed
            :ambient-authority-denied true}
           (select-keys metadata
                        [:stage :component :owner :source-language :seed
                         :retirement-objective :ambient-authority-denied])))
    (is (= #{:domain-ir-registry :domain-ir-artifact-schema
             :semantic-anchor-map :mir-input-anchor-contract
             :entry-pass-records :exit-pass-records :domain-verifier-report
             :proof-and-certificate-references :lowering-eligibility-matrix
             :fallback-records :plugin-registration-policy
             :domain-ir-diagnostic-catalog}
           (set (:implements metadata))))
    (is (= #{:source-spans :syntax-identity :diagnostic-codes
             :artifact-provenance :origin-chains :generated-origin
             :core-node-identity :typed-core-anchors :mir-operation-anchors
             :mir-domain-anchors :type-facts :effect-facts :ownership-facts
             :capability-facts :profile-target-metadata :safety-outcomes
             :proof-obligations :certificate-references :source-map-fields
             :fallback-legality :plugin-policy-records
             :self-hosting-source-metadata}
           (:preserves metadata)))
    (is (= {:verified-by :clojure-stage0 :compiled-by :clojure-stage0
            :next-stage :stage1
            :replaces [:clojure-c12-domain-ir-contract-subset
                       :clojure-c12-diagnostic-catalog]}
           (:lineage metadata)))
    (is (= expected-definition-names (set (keys definitions))))
    (is (= 9 (count (filter #(= 'def (first %)) (vals definitions)))))
    (is (= 76 (count (filter #(= 'defn (first %)) (vals definitions)))))
    (is (= quoted-definition-names
           (set (for [[name form] definitions :when (quoted-body form)] name))))
    (is (= executable-definition-names executable))
    (is (= 72 (count executable)))
    (doseq [[name expected-hash] expected-contract-value-hashes]
      (is (= expected-hash
             (value-sha256-id (nth (get definitions name) 2)))))
    (is (= expected-diagnostic-catalog
           (nth (get definitions 'c12-domain-ir-diagnostic-catalog) 2)))
    (is (= expected-policy policy))
    (is (= 325 (count if-calls)))
    (is (every? #(= 4 (count %)) if-calls))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c12-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c12-relative-path)))))))

(deftest sh07-b32-c12-correction-residuals-and-static-lookups-are-exact
  (let [forms (source-forms)
        definitions
        (into {} (for [name expected-definition-names]
                   [name (or (named-top-level-form forms 'def name)
                             (named-top-level-form forms 'defn name))]))
        get-calls (mapcat #(collect-calls 'get %) (vals definitions))
        literal-gets (filter #(keyword? (nth % 2 nil)) get-calls)
        dynamic-gets (sort-by #(-> % meta :line)
                              (remove #(keyword? (nth % 2 nil)) get-calls))
        request-keys (get definitions 'sh14-v3-request-keys)
        request-key-sets
        (set (filter #(and (set? %)
                           (contains? % :artifact)
                           (contains? % :value-id))
                     (tree-seq coll? seq request-keys)))
        common-valid (get definitions 'sh14-v2-common-error)
        preflight (get definitions 'sh14-v3-structural-preflight)
        verifier (get definitions 'sh14-verify-layout)
        identity-input-form (get definitions 'sh14-identity-input)
        sha-shape (get definitions 'sh14-v2-sha256-id?)]
    (is (= 254 (count get-calls)))
    (is (= 209 (count literal-gets)))
    (is (= 45 (count dynamic-gets)))
    (is (= [418 425 567 582 588 594 595 612 613 614 641 651
            658 658 659 659 660 660 661 661 662 662 663 663 664 664
            743 810 833 896 929 939 957 967 973 979 980 997 998 999
            1108 1169 1170 1199 1457]
           (mapv #(-> % meta :line) dynamic-gets)))
    (is (= '(get values value) (first dynamic-gets)))
    (is (= '(get origins index) (last dynamic-gets)))
    (is (= 5 (count request-key-sets)))
    (is (every? #(contains? % :safety-fact-id) request-key-sets))
    (is (contains? (symbols-in common-valid) 'sh14-v3-exact-keys?))
    (is (contains? (symbols-in preflight) 'sh14-v3-aggregate-components))
    (is (contains? (symbols-in preflight) 'sh14-v3-structural-preflight))
    (is (contains? (symbols-in verifier) 'sh14-build-layout))
    (is (not (contains? (symbols-in verifier) 'independent-layout-verifier)))
    (is (contains? (symbols-in identity-input-form) 'sh14-identity-origins))
    (is (not (contains? (symbols-in sha-shape)
                        'authenticated-envelope-verify-template)))
    (is (= {:maximum-nodes 32768 :maximum-depth 96 :maximum-width 8192
            :maximum-collections 8192 :maximum-scalars 24576
            :maximum-scalar-units 262144}
           (:structural-bounds expected-policy)))
    (is (= #{[:safe :portable] [:meta :portable-mir]}
           (:supported-profile-target-pairs expected-policy)))
    (is (= 64 (:maximum-origin-count expected-policy)))
    (is (= 64 (:maximum-generated-origin-depth expected-policy)))
    (is (= 256 (:maximum-identifier-units expected-policy)))
    (is (= [:authenticated-sh12-mir-input :target-specific-layout
            :actual-allocation :field-offset-calculation
            :pointer-and-lifetime-layouts]
           (:pending expected-policy)))))

(deftest sh07-b32-c12-source-has-exact-authentic-coverage
  (let [artifact @c12-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= 'gravity.compiler.c12-domain-ir-architecture
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in authenticated-request [:module :source-revision-id])
           (get-in authenticated-request [:lineage :source-revision-id])))
    (is (= expected-sh06-semantic-projection-id
           (get-in authenticated-request [:lineage :sh06-semantic-projection-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (false? (get-in artifact
                        [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b32-c12-core-lookups-and-quotes-are-exact
  (let [core-artifact (core @c12-artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        get-calls
        (filterv #(= 'get (get-in reference-by-node-id
                                  [(:operator-node-id %) :symbol]))
                 (:calls core-artifact))
        literal-gets
        (filterv #(keyword? (get-in node-by-id
                                    [(second (:argument-node-ids %))
                                     :attributes :value]))
                 get-calls)
        dynamic-gets
        (filterv #(not (keyword? (get-in node-by-id
                                         [(second (:argument-node-ids %))
                                          :attributes :value])))
                 get-calls)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 254 (count get-calls)))
    (is (= 209 (count literal-gets)))
    (is (= 45 (count dynamic-gets)))
    (is (= {:reference 19 :call 12 :literal 14}
           (frequencies
            (map #(get-in node-by-id
                          [(second (:argument-node-ids %)) :core-form])
                 dynamic-gets))))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero? (count (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (is (= 4 (count quote-nodes)))
    (doseq [node quote-nodes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order]))))))

(deftest sh07-b32-c12-is-deterministic-path-neutral-and-provenanced
  (if (= "1" (System/getenv "GRAVITY_SH07_B32_PARITY"))
    (let [{:keys [left right left-path right-path]} @parity-artifacts]
      (is (= :accepted (:status left) (:status right)))
      (is (= (:artifact-id left) (:artifact-id right)))
      (is (= (identity-input left) (identity-input right)))
      (is (= (coverage left) (coverage right)))
      (is (= (core-census left) (core-census right)))
      (is (= (:fragment-manifest (request left))
             (:fragment-manifest (request right))))
      (is (= left-path
             (get-in left [:provenance :source-path])
             (get-in (core left) [:provenance :actual-source-path])))
      (is (= right-path
             (get-in right [:provenance :source-path])
             (get-in (core right) [:provenance :actual-source-path])))
      (is (not= left-path right-path)))
    (is true "Set GRAVITY_SH07_B32_PARITY=1 for the isolated parity gate")))

(deftest sh07-b32-c12-replay-and-high-value-alterations-are-contained
  (let [artifact @c12-artifact
        core-artifact (core artifact)
        quote-index
        (first (keep-indexed (fn [index node]
                               (when (= :quote (:core-form node)) index))
                             (:nodes core-artifact)))
        report ((required-var 'sh07-core-artifact-verification) artifact)
        proof (:capability-based-proof artifact)
        changed-request
        (assoc-in (request artifact) [:module :source-revision-id] zero-id)
        request-result
        (diagnostic-result
         #((required-var 'sh07-core-run-request-for-test)
           (:sh06-resolution-artifact artifact) changed-request))
        request-diagnostic (diagnostic-data request-result)]
    (is (= :passed (:status report)))
    (is (= [] (:failed-checks report)))
    (is (= :complete (:status proof)))
    (is (= [] (:failed-checks proof)))
    (is (nil? (:raw-host-error request-result)))
    (is (= :gravity/sh07-core-diagnostic (:artifact request-diagnostic)))
    (is (= "C6-VERIFY" (:rule request-diagnostic)))
    (doseq [[label changed expected-check]
            [["definition binding"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :definitions 0 :binding-id] zero-id)
              :canonical-core-replays?]
             ["call binding"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :calls 0 :operator-binding-id] zero-id)
              :calls-replay?]
             ["quoted body"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :nodes quote-index :attributes :quoted-value]
                        :changed)
              :canonical-core-replays?]
             ["actual path provenance"
              (assoc-in artifact
                        [:gravity-core-boundary :canonical-core-artifact
                         :provenance :actual-source-path]
                        "/changed/root/c12_domain_ir_architecture.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status] :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               changed artifact @c12-upstream-verification)
              failed (set (for [[check passed?] checks
                                :when (not (true? passed?))]
                            check))]
          (is (contains? failed expected-check)))))))

(deftest sh07-b32-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families
          extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." extension))
            peer-extension (if (= "gravity" extension) "qst" "gravity")
            peer-path
            (path
             (str "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
                  basename "." peer-extension))
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path))
               (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b32-c12-measured-carrier-fits-unchanged-b16-bounds
  (let [m expected-census-measurements
        b expected-b16-bounds]
    (is (< (:forms m) (:maximum-module-forms b)))
    (is (< (:predicted-maximum-core-nodes m)
           (:maximum-module-core-nodes b)))
    (is (< (:fragments m) (:maximum-fragments b)))
    (is (< (:top-level-forms m) (:maximum-top-level-forms b)))
    (is (< (:maximum-fragment-forms m) (:maximum-fragment-forms b)))
    (is (< (:maximum-form-children m) (:maximum-form-children b)))
    (is (< (:maximum-form-depth m) (:maximum-form-depth b)))
    (is (< (:bindings m) (:maximum-bindings b)))
    (is (< (:maximum-fragment-resolutions m)
           (:maximum-fragment-resolutions b)))
    (is (< (:resolutions m) (:maximum-module-resolutions b)))
    (is (< (:carrier-nodes m) (:maximum-module-carrier-nodes b)))
    (is (< (:carrier-depth m) (:maximum-module-carrier-depth b)))
    (is (< (:carrier-width m) (:maximum-module-carrier-width b)))
    (is (< (:carrier-scalar-bytes m) (:maximum-module-scalar-bytes b)))
    (is (< (:predicted-maximum-digest-requests m)
           (:maximum-module-digest-requests b)))))
