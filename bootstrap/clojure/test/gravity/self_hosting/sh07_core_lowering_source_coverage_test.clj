(ns gravity.self-hosting.sh07-core-lowering-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_core_lowering_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C6 source coverage test is not on the classpath"
        {:id "SH07-C6-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C6-COVERAGE-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c6-relative-path
  "bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 294386)
(def ^:private expected-source-revision-id
  "sha256:4e8fef01eb10d15e5896301bb06d6b6153187a78e321712277bcbabd5e9f872c")
(def ^:private expected-coverage
  {:fragment-count 256
   :root-form-count 256
   :form-count 28773
   :binding-count 1853
   :local-binding-count 1591
   :resolution-count 11606})
(def ^:private expected-core-census
  {:core-node-count 24230
   :definition-count 256
   :call-count 4825
   :reference-count 9377
   :keyword-lookup-count 0
   :core-form-frequencies
   {:call 4825
    :collection-literal 867
    :def 256
    :fn 251
    :if 1143
    :let 196
    :literal 6932
    :loop 154
    :quote 40
    :recur 189
    :reference 9377}})
(def ^:private expected-definition-names
  '[c6-core-node-contract
    c6-lowering-rule-contract
    c6-domain-boundary-contract
    c6-core-lowering-diagnostic-catalog
    c6-set-mutation-execution-contract
    c6-set-execution-contract-value
    c6-set-execution-verification-checks
    c6-set-execution-verification-carrier
    c6-set-execution-exact-keys?
    c6-set-execution-id?
    c6-set-execution-find
    c6-set-execution-remediation
    c6-set-execution-diagnostic
    c6-set-execution-request-shape?
    c6-set-execution-store-entry?
    c6-set-execution-store-valid?
    c6-set-execution-node-index-valid?
    c6-set-execution-mutation-shape?
    c6-set-execution-mutation-index-valid?
    c6-set-execution-positive-integer?
    c6-set-execution-semantic-span-valid?
    c6-set-execution-generated-origin-valid?
    c6-set-execution-literal-value-kind-valid?
    c6-set-execution-source-valid?
    c6-set-execution-core-node-shape?
    c6-set-execution-mutation-pending-legality?
    c6-set-execution-set-node-ids
    c6-set-execution-mutation-node-ids
    c6-set-execution-first-mismatched-set-node
    c6-set-execution-first-unseen-node
    c6-set-execution-graph-check
    c6-set-execution-literal-check
    c6-set-execution-set-check
    c6-set-execution-node-check
    c6-set-execution-all-nodes-check
    verify-c6-set-mutation-execution-request
    c6-sh07-exception-contract-value
    c6-sh07-exception-exact-keys?
    c6-sh07-exception-find
    c6-sh07-exception-sha256-id?
    c6-sh07-exception-all-unique?
    c6-sh07-exception-bounded-vector?
    c6-sh07-exception-aggregate?
    c6-sh07-exception-components
    c6-sh07-exception-carrier-preflight
    c6-sh07-exception-diagnostic
    c6-sh07-exception-rule-for-reason
    c6-sh07-exception-request-shape?
    c6-sh07-exception-snapshot-shape?
    c6-sh07-exception-byte-vector-valid?
    c6-sh07-exception-utf8-continuation?
    c6-sh07-exception-utf8-bytes-valid?
    c6-sh07-exception-snapshot-strict-utf8?
    c6-sh07-exception-carrier-shape?
    c6-sh07-exception-source-unit
    c6-sh07-exception-core-request
    c6-sh07-exception-core
    c6-sh07-exception-index-shaped-request
    c6-sh07-exception-index-request
    c6-sh07-exception-form
    c6-sh07-exception-form-by-syntax
    c6-sh07-exception-binding
    c6-sh07-exception-definition-binding
    c6-sh07-exception-resolution
    c6-sh07-exception-form-operator
    c6-sh07-exception-form-root-id
    c6-sh07-exception-project-binding
    c6-sh07-exception-upstream-binding
    c6-sh07-exception-upstream-request-binding
    c6-sh07-exception-core-external-authorized?
    c6-sh07-exception-upstream-import-dependency
    c6-sh07-exception-alias-record-valid?
    c6-sh07-exception-alias-table-valid?
    c6-sh07-exception-qualified-alias-record
    c6-sh07-exception-import-external-authorized?
    c6-sh07-exception-edge-classification
    c6-sh07-exception-all-checks-true?
    c6-sh07-exception-exact-checks-true?
    c6-sh07-exception-sh06-report-checks
    c6-sh07-exception-b47-report-checks
    c6-sh07-exception-sh05-report-checks
    c6-sh07-exception-sh05-run-verification-valid?
    c6-sh07-exception-sh05-gravity-verifiers-valid?
    c6-sh07-exception-sh05-report-valid?
    c6-sh07-exception-verification-reports-valid?
    c6-sh07-exception-authorized-edges
    c6-sh07-exception-path-segments-valid?
    c6-sh07-exception-absolute-normalized-path?
    c6-sh07-exception-relative-normalized-path?
    c6-sh07-exception-path-has-extension?
    c6-sh07-exception-path-membership-valid?
    c6-sh07-exception-membership-valid?
    c6-sh07-exception-vector-sha?
    c6-sh07-exception-span-valid?
    c6-sh07-exception-origin-entry-valid?
    c6-sh07-exception-origin-vector-valid?
    c6-sh07-exception-form-record-valid?
    c6-sh07-exception-binding-record-valid?
    c6-sh07-exception-resolution-record-valid?
    c6-sh07-exception-trace-record-valid?
    c6-sh07-exception-lineage-valid?
    c6-sh07-exception-module-valid?
    c6-sh07-exception-preserved-macro-step-valid?
    c6-sh07-exception-origin-expectation-valid?
    c6-sh07-exception-trace-pair-valid?
    c6-sh07-exception-origin-entries
    c6-sh07-exception-origin-follow-acyclic?
    c6-sh07-exception-retained-origins-unique?
    c6-sh07-exception-origin-closure-valid?
    c6-sh07-exception-flatten-fragment-field
    c6-sh07-exception-form-ids
    c6-sh07-exception-local-binding-ids
    c6-sh07-exception-resolution-reference-ids
    c6-sh07-exception-fragment-record-valid?
    c6-sh07-exception-form-graph-valid?
    c6-sh07-exception-fragment-products-valid?
    c6-sh07-exception-request-collections-shaped?
    c6-sh07-exception-request-bounds-valid?
    c6-sh07-exception-carrier-admission
    c6-sh07-exception-child
    c6-sh07-exception-operator-resolution-valid?
    c6-sh07-exception-origin-valid?
    c6-sh07-exception-fragment-forms
    c6-sh07-exception-operator-count
    c6-sh07-exception-selected-fragment
    c6-sh07-exception-definition-binding-valid?
    c6-sh07-exception-catch-valid
    c6-sh07-exception-raw-candidate
    c6-sh07-exception-raw-selection
    c6-sh07-exception-value-edge
    c6-sh07-exception-value-node-authorized?
    c6-sh07-exception-canonical-cross-check
    c6-sh07-exception-shape
    c6-sh07-exception-path-neutral-record
    c6-sh07-exception-path-neutral-binding
    c6-sh07-exception-path-neutral-resolution
    c6-sh07-exception-semantic-edges
    c6-sh07-exception-semantic-bindings
    c6-sh07-exception-semantic-node
    c6-sh07-exception-semantic-nodes
    c6-sh07-exception-node-sources
    c6-sh07-exception-source-map
    c6-sh07-exception-snapshot-membership-view
    c6-sh07-exception-build-template
    c6-sh07-independent-template-check
    c6-sh07-exception-digest-entry-valid?
    c6-sh07-exception-digest-transcript-valid?
    c6-sh07-exception-digest-at
    c6-sh07-exception-snapshot-digests-valid?
    c6-sh07-exception-resolved-template
    c6-sh07-exception-circular-verifier-retired
    c6-sh07-independent-exception-aggregate?
    c6-sh07-independent-exception-components
    c6-sh07-independent-exception-carrier-preflight
    c6-sh07-independent-exception-utf8-continuation?
    c6-sh07-independent-exception-strict-utf8?
    c6-sh07-independent-exception-snapshot-valid?
    c6-sh07-independent-exception-path-segments-valid?
    c6-sh07-independent-exception-paths-valid?
    c6-sh07-independent-exception-all-checks-true?
    c6-sh07-independent-exception-sh06-check-set
    c6-sh07-independent-exception-b47-check-set
    c6-sh07-independent-exception-sh05-check-set
    c6-sh07-independent-exception-sh05-run-valid?
    c6-sh07-independent-exception-sh05-verifiers-valid?
    c6-sh07-independent-exception-sh05-report-valid?
    c6-sh07-independent-exception-reports-valid?
    c6-sh07-independent-exception-membership-valid?
    c6-sh07-independent-exception-index-request
    c6-sh07-independent-exception-vector-sha?
    c6-sh07-independent-exception-span-valid?
    c6-sh07-independent-exception-origin-vector-shaped?
    c6-sh07-independent-exception-form-shaped?
    c6-sh07-independent-exception-binding-shaped?
    c6-sh07-independent-exception-resolution-shaped?
    c6-sh07-independent-exception-alias-shaped?
    c6-sh07-independent-exception-trace-shaped?
    c6-sh07-independent-exception-fragment-shaped?
    c6-sh07-independent-exception-lineage-shaped?
    c6-sh07-independent-exception-module-shaped?
    c6-sh07-independent-exception-macro-step-shaped?
    c6-sh07-independent-exception-records-shaped?
    c6-sh07-independent-exception-origin-expectation-valid?
    c6-sh07-independent-exception-macro-products-valid?
    c6-sh07-independent-exception-tree-id-set
    c6-sh07-independent-exception-ordered-form-ids
    c6-sh07-independent-exception-ordered-local-binding-ids
    c6-sh07-independent-exception-ordered-resolution-ids
    c6-sh07-independent-exception-ordered-external-binding-ids
    c6-sh07-independent-exception-resolution-alias
    c6-sh07-independent-exception-fragment-aliases
    c6-sh07-independent-exception-fragment-derived-valid?
    c6-sh07-independent-exception-append-values
    c6-sh07-independent-exception-flatten-field
    c6-sh07-independent-exception-disjoint?
    c6-sh07-independent-exception-union
    c6-sh07-independent-exception-form-graph-valid?
    c6-sh07-independent-exception-fragment-products-valid?
    c6-sh07-independent-exception-edge-occurrences
    c6-sh07-independent-exception-request-collections-valid?
    c6-sh07-independent-exception-request-domain-valid?
    c6-sh07-independent-exception-origin-entries
    c6-sh07-independent-exception-origin-entry-valid?
    c6-sh07-independent-exception-retained-unique?
    c6-sh07-independent-exception-origin-closure-valid?
    c6-sh07-independent-exception-carrier-admission
    c6-sh07-independent-exception-upstream-binding
    c6-sh07-independent-exception-upstream-request-binding
    c6-sh07-independent-exception-core-external-authorized?
    c6-sh07-independent-exception-upstream-import-dependency
    c6-sh07-independent-exception-alias-record-valid?
    c6-sh07-independent-exception-qualified-alias-record
    c6-sh07-independent-exception-import-external-authorized?
    c6-sh07-independent-exception-edge-classification
    c6-sh07-independent-exception-authorized-edges
    c6-sh07-independent-exception-operator-resolution-valid?
    c6-sh07-independent-exception-origin-valid?
    c6-sh07-independent-exception-fragment-forms
    c6-sh07-independent-exception-operator-count
    c6-sh07-independent-exception-selected-fragment
    c6-sh07-independent-exception-definition-binding-valid?
    c6-sh07-independent-exception-catch-valid
    c6-sh07-independent-exception-raw-candidate
    c6-sh07-independent-exception-raw-selection
    c6-sh07-independent-exception-value-edge
    c6-sh07-independent-exception-value-node-authorized?
    c6-sh07-independent-exception-canonical-cross-check
    c6-sh07-independent-exception-resolve-eligible?
    c6-sh07-independent-exception-path-neutral-record
    c6-sh07-independent-exception-semantic-node
    c6-sh07-independent-exception-semantic-nodes
    c6-sh07-independent-exception-semantic-binding
    c6-sh07-independent-exception-semantic-bindings
    c6-sh07-independent-exception-semantic-edges
    c6-sh07-independent-exception-snapshot-view
    c6-sh07-independent-exception-source-map
    c6-sh07-independent-exception-node-sources
    c6-sh07-independent-exception-derived-context
    c6-sh07-independent-exception-template-from-context
    c6-sh07-exception-not-equal?
    c6-sh07-independent-exception-resolved-template-check
    c6-sh07-independent-exception-resolved-core-check
    c6-sh07-independent-exception-snapshot-digests-valid?
    c6-sh07-independent-exception-verifier-from-resolved
    c6-sh07-independent-exception-verifier
    c6-sh07-exception-admission-diagnostic
    c6-sh07-exception-phase-payload-valid?
    c6-sh07-exception-admit-phase
    c6-sh07-exception-verify-template-phase
    c6-sh07-exception-template-literal-value?
    c6-sh07-exception-resolve-phase
    c6-sh07-exception-verify-resolved-phase
    c6-sh07-authenticated-exception-entrypoint
    build-c6-core-node
    build-c6-desugaring-trace
    verify-c6-core-lowering])
(def ^:private expected-definition-names-hash
  "sha256:dac69746e106d1a1f36a829df8581c302f0d5e68a5cdac6ab5d24abda9c308d5")
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE"
   "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B18 coordinator adapter is absent"
        {:id "SH07-C6-COVERAGE-ADAPTER-ABSENT"
         :symbol symbol}))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- sha256-id
  [bytes]
  (let [digest
        (.digest
         (java.security.MessageDigest/getInstance "SHA-256")
         bytes)]
    (str
     "sha256:"
     (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path
         (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(defn- core
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact
          [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)
        namespace (get-in authenticated-request [:module :namespace])]
    {:fragment-count
     (count (:fragment-manifest authenticated-request))
     :root-form-count
     (count (:top-level-form-ids authenticated-request))
     :form-count
     (count (:forms authenticated-request))
     :binding-count
     (count (:binding-table authenticated-request))
     :local-binding-count
     (count
      (filter #(= namespace (:namespace %))
              (:binding-table authenticated-request)))
     :resolution-count
     (count (:resolution-table authenticated-request))}))

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

(defn- diagnostic-result
  [operation]
  (try
    {:value (operation)}
    (catch clojure.lang.ExceptionInfo exception
      {:exception-data (ex-data exception)})
    (catch Throwable throwable
      {:raw-host-error
       {:class (.getName (class throwable))
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

(def ^:private c6-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c6-relative-path))))

(def ^:private c6-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c6-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c6-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c6_core_lowering_engine.qst")
          left-path (path c6-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c6-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b18-proof-contract-registers-c6-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))]
    (is (= "SH-07-B47" (:coverage-milestone contract)))
    (is (= c6-relative-path
           (get-in contract [:authoritative-modules :c6-core])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c6-core])))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c6-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c6-relative-path)))))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b18-c6-source-has-exact-authentic-coverage
  (let [artifact @c6-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.compiler.c6-core-lowering-engine
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-source-revision-id
           (get-in authenticated-request
                   [:module :source-revision-id])
           (get-in authenticated-request
                   [:lineage :source-revision-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= expected-core-census (core-census artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (= (mapv :reference-syntax-id
                 (:resolution-table authenticated-request))
           (:covered-resolution-reference-syntax-ids coverage-record)))
    (is (false?
         (get-in artifact
                 [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b18-c6-definitions-functions-and-quotes-remain-distinct
  (let [core-artifact (core @c6-artifact)
        nodes (:nodes core-artifact)
        node-by-id (into {} (map (juxt :node-id identity)) nodes)
        definitions (:definitions core-artifact)
        quote-nodes (filterv #(= :quote (:core-form %)) nodes)
        fn-nodes (filterv #(= :fn (:core-form %)) nodes)]
    (is (= expected-definition-names
           (mapv :name definitions)))
    (is (= expected-definition-names-hash
           (gravity.bootstrap/p15-s23-c11-mir-digest
            expected-definition-names)))
    (is (= 256 (count definitions)))
    (is (= 40 (count quote-nodes)))
    (is (= 251 (count fn-nodes)))
    (is (every? #(= :def
                    (:core-form (get node-by-id (:core-node-id %))))
                definitions))
    (doseq [node quote-nodes]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order])))
      (is (= #{:quoted-form-id :quoted-syntax-id
               :quoted-kind :quoted-value}
             (set (keys (:attributes node))))))))

(deftest sh07-b18-c6-is-deterministic-path-neutral-and-provenanced
  (let [{:keys [left right left-path right-path]} @parity-artifacts]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (coverage left) (coverage right)))
    (is (= (core-census left) (core-census right)))
    (is (= (:fragment-manifest (request left))
           (:fragment-manifest (request right))))
    (is (= (:fragment-coverage (core left))
           (:fragment-coverage (core right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))
    (is (not= left-path right-path))))

(deftest sh07-b18-c6-replay-and-alteration-containment-pass
  (let [artifact @c6-artifact
        report
        ((required-var 'sh07-core-artifact-verification) artifact)
        proof (:capability-based-proof artifact)
        authenticated-request (request artifact)
        request-alteration
        (assoc-in authenticated-request
                  [:module :source-revision-id] zero-id)
        request-result
        (diagnostic-result
         #((required-var 'sh07-core-run-request-for-test)
           (:sh06-resolution-artifact artifact)
           request-alteration))
        request-diagnostic (diagnostic-data request-result)]
    (is (= :passed (:status report)))
    (is (= [] (:failed-checks report)))
    (is (= :complete (:status proof)))
    (is (= [] (:failed-checks proof)))
    (is (nil? (:raw-host-error request-result)))
    (is (= :gravity/sh07-core-diagnostic
           (:artifact request-diagnostic)))
    (is (= "C6-VERIFY" (:rule request-diagnostic)))
    (is (= true
           (get-in request-diagnostic [:facts :fail-closed])))
    (doseq [[label altered expected-check]
            [["definition binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :definitions 0 :binding-id]
               zero-id)
              :canonical-core-replays?]
             ["actual path provenance"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :provenance :actual-source-path]
               "/altered/root/c6_core_lowering_engine.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c6-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b18-existing-rejected-families-remain-paired-and-structured
  (doseq [[basename expected-rule] rejected-families
          extension ["gravity" "qst"]]
    (testing (str basename "." extension)
      (let [source-path
            (path
             (str
              "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
              basename "." extension))
            peer-extension (if (= "gravity" extension) "qst" "gravity")
            peer-path
            (path
             (str
              "bootstrap/clojure/fixtures/self-hosting/sh-07/rejected/"
              basename "." peer-extension))
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= (vec (source-bytes source-path))
               (vec (source-bytes peer-path))))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic
               (:artifact diagnostic)))
        (is (= expected-rule (:rule diagnostic)))
        (is (= source-path
               (get-in diagnostic [:source-span :source])))
        (is (= true (get-in diagnostic [:facts :fail-closed])))))))
