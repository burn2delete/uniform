(ns gravity.self-hosting.sh07-sh21-meta-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_sh21_meta_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 SH21 meta source test is not on the classpath"
        {:id "SH07-SH21-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-SH21-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private sh21-relative-path
  "bootstrap/gravity/src/gravity/self_hosting/meta_compiler_legality.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 28374)
(def ^:private expected-source-revision-id
  "sha256:57184c6d8931d0beeadc5b4be56c47c09d1e0fce379c0d398dcd6ae15929aef0")
(def ^:private expected-artifact-id
  "sha256:771dbb9bf65b3e7160c149f16b0d9a12230b0ec96083a0430a0b8c2c3b179b88")
(def ^:private expected-coverage
  {:fragment-count 42
   :root-form-count 42
   :form-count 2778
   :binding-count 419
   :local-binding-count 157
   :resolution-count 1109})
(def ^:private expected-core-census
  {:core-node-count 2292
   :definition-count 42
   :call-count 459
   :reference-count 848
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 616
    :collection-literal 108
    :def 42
    :reference 848
    :call 459
    :if 151
    :let 26
    :fn 42}})
(def ^:private expected-definition-names
  '#{sh21-meta-policy
     sh21-member?
     sh21-present?
     sh21-source-span-valid?
     sh21-origin-chain-valid?
     sh21-set-subset?
     sh21-sets-disjoint?
     sh21-unique-by-key?
     sh21-unique-values?
     sh21-string-values?
     sh21-find-module
     sh21-effect-capability
     sh21-required-capabilities
     sh21-module-authority-error
     sh21-input-kind-effect
     sh21-build-input-valid?
     sh21-build-inputs-valid?
     sh21-effects-requiring-inputs
     sh21-input-effects
     sh21-module-input-error
     sh21-pass-contract-error
     sh21-generated-record-error
     sh21-generated-records-error
     sh21-module-generated-error
     sh21-module-common-error
     sh21-module-error
     sh21-dependencies-exist?
     sh21-dependency-count
     sh21-module-container-error
     sh21-cycle-from-dependencies?
     sh21-cycle-from?
     sh21-any-cycle?
     sh21-modules-error
     sh21-module-ids
     sh21-dependency-order-valid?
     sh21-program-error
     sh21-diagnostic
     sh21-module-identity
     sh21-module-identities
     sh21-module-provenance
     sh21-check-program
     sh21-verify-result})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE"
   "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private expected-pending
  [:authenticated-sh15-diagnostics
   :authenticated-sh17-lowering-interface
   :authenticated-sh19-runtime-services
   :all-authoritative-compiler-modules
   :compiler-executable-under-meta
   :seedless-execution])
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B26 coordinator adapter is absent"
        {:id "SH07-SH21-COVERAGE-ADAPTER-ABSENT"
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
    (str "sha256:"
         (apply str
                (map #(format "%02x" (bit-and 0xff %)) digest)))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists
         root-path (make-array java.nio.file.LinkOption 0))
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
    {:fragment-count (count (:fragment-manifest authenticated-request))
     :root-form-count (count (:top-level-form-ids authenticated-request))
     :form-count (count (:forms authenticated-request))
     :binding-count (count (:binding-table authenticated-request))
     :local-binding-count
     (count
      (filter #(= namespace (:namespace %))
              (:binding-table authenticated-request)))
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
        "SH-07-B26 records are not uniquely identifiable"
        {:id "SH07-SH21-COVERAGE-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- assert-source-origin
  [node]
  (is (= #{:syntax-id :form-id :semantic-span
           :origin-chain :generated-origin}
         (set (keys (:source node)))))
  (is (string? (get-in node [:source :syntax-id])))
  (is (string? (get-in node [:source :form-id])))
  (is (map? (get-in node [:source :semantic-span])))
  (is (vector? (get-in node [:source :origin-chain])))
  (is (vector? (get-in node [:source :generated-origin]))))

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

(def ^:private sh21-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path sh21-relative-path))))

(def ^:private sh21-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @sh21-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-sh21-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/meta_compiler_legality.qst")
          left-path (path sh21-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @sh21-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b26-proof-contract-registers-sh21-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path sh21-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)
        pending-source
        (second
         (re-find #"(?s):pending\s*(\[[^\]]*\])" source-text))]
    (is (= "SH-07-B26" (:coverage-milestone contract)))
    (is (= sh21-relative-path
           (get-in contract [:authoritative-modules :sh21-meta])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :sh21-meta])))
    (doseq [document
            ["docs/self-hosting-slice-backlog.md"
             "docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md"
             "docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md"
             "docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md"
             "docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md"
             "docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md"
             "docs/phase-01-core-language/024-l14-language-facet-system-specification.md"
             "docs/phase-01-core-language/025-l15-capability-provider-specification.md"
             "docs/phase-03-profile-system/048-p3-meta-profile-specification.md"
             "docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md"
             "docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md"
             "docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md"
             "docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md"
             "docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md"
             "docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md"]]
      (is (contains? documents document)))
    (is (= expected-source-byte-count
           (alength (source-bytes (path sh21-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path sh21-relative-path)))))
    (doseq [boundary
            ["coordinator-owned integration work"
             "Applying this policy to every"
             "authoritative compiler module and executing the resulting compiler"]]
      (is (string/includes? source-text boundary)))
    (is (= expected-pending (edn/read-string pending-source)))
    (is (contains? (set (:nonclaims contract))
                   :sh21-whole-compiler-meta-execution))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b26-sh21-source-has-exact-authentic-coverage
  (let [artifact @sh21-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= expected-artifact-id (:artifact-id artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.self-hosting.meta-compiler-legality
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
    (is (false?
         (get-in artifact
                 [:gravity-core-boundary :target-source-reread?])))))

(deftest sh07-b26-sh21-core-calls-lookups-and-recursion-are-exact
  (let [artifact @sh21-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        binding-by-id
        (exactly-once-index (:binding-table authenticated-request) :binding-id)
        calls (:calls core-artifact)
        definition-by-name
        (into {} (map (juxt :name identity)) (:definitions core-artifact))
        operator-symbols
        (mapv
         #(get-in reference-by-node-id
                  [(:operator-node-id %) :symbol])
         calls)
        get-calls
        (filterv
         #(= 'get
             (get-in reference-by-node-id
                     [(:operator-node-id %) :symbol]))
         calls)
        literal-keyword-get-calls
        (filterv
         (fn [call]
           (keyword?
            (get-in node-by-id
                    [(second (:argument-node-ids call))
                     :attributes :value])))
         get-calls)
        other-get-calls
        (filterv
         (fn [call]
           (not
            (keyword?
             (get-in node-by-id
                     [(second (:argument-node-ids call))
                      :attributes :value]))))
         get-calls)
        recursive-symbols
        '#{sh21-cycle-from? sh21-cycle-from-dependencies?}
        recursive-calls
        (filterv
         #(contains?
           recursive-symbols
           (get-in reference-by-node-id
                   [(:operator-node-id %) :symbol]))
         calls)
        definition-order (mapv :name (:definitions core-artifact))]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 42 (count (filter #(= :fn (:core-form %)) nodes))))
    (is (= 151 (count (filter #(= :if (:core-form %)) nodes))))
    (is (= 26 (count (filter #(= :let (:core-form %)) nodes))))
    (is (= 161 (get (frequencies operator-symbols) 'get)))
    (is (= 161 (count get-calls)))
    (is (= 139 (count literal-keyword-get-calls)))
    (is (= 22 (count other-get-calls)))
    (is (= {:reference 20 :literal 2}
           (frequencies
            (map
             #(get-in node-by-id
                      [(second (:argument-node-ids %)) :core-form])
             other-get-calls))))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero?
         (count
          (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (doseq [call calls]
      (let [call-node (get node-by-id (:core-node-id call))
            operator-node (get node-by-id (:operator-node-id call))
            operator-reference
            (get reference-by-node-id (:operator-node-id call))
            expected-order
            (into [(:operator-node-id call)] (:argument-node-ids call))]
        (is (= :call (:core-form call-node)))
        (is (= expected-order (:children call-node)))
        (is (= expected-order (:ordered-evaluation-node-ids call)))
        (is (= :operator-then-arguments
               (:evaluation-order call)
               (get-in call-node [:evaluation :kind])))
        (is (= (:operator-binding-id call)
               (:binding-id operator-reference)
               (get-in operator-node [:attributes :binding-id])))
        (is (map? (get binding-by-id (:operator-binding-id call))))
        (assert-source-origin call-node)
        (assert-source-origin operator-node)
        (doseq [argument-node-id (:argument-node-ids call)]
          (assert-source-origin (get node-by-id argument-node-id)))))
    (is (< (.indexOf definition-order 'sh21-cycle-from-dependencies?)
           (.indexOf definition-order 'sh21-cycle-from?)))
    (is (= {'sh21-cycle-from? 2
            'sh21-cycle-from-dependencies? 2}
           (frequencies
            (map
             #(get-in reference-by-node-id
                      [(:operator-node-id %) :symbol])
             recursive-calls))))
    (doseq [call recursive-calls]
      (let [operator-reference
            (get reference-by-node-id (:operator-node-id call))
            target
            (get definition-by-name (:symbol operator-reference))]
        (is (= (:binding-id target)
               (:binding-id operator-reference)
               (:operator-binding-id call)))))))

(deftest sh07-b26-sh21-is-deterministic-path-neutral-and-provenanced
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
    (is (not= left-path right-path))))

(deftest sh07-b26-sh21-replay-and-alteration-containment-pass
  (let [artifact @sh21-artifact
        report
        ((required-var 'sh07-core-artifact-verification) artifact)
        proof (:capability-based-proof artifact)
        request-alteration
        (assoc-in (request artifact)
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
    (doseq [[label altered expected-check]
            [["definition binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :definitions 0 :binding-id]
               zero-id)
              :canonical-core-replays?]
             ["call binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :calls 0 :operator-binding-id]
               zero-id)
              :calls-replay?]
             ["keyword lookup collection"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :keyword-lookups]
               [{:core-node-id zero-id}])
              :keyword-lookups-replay?]
             ["actual path provenance"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :provenance :actual-source-path]
               "/altered/root/meta_compiler_legality.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @sh21-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b26-existing-rejected-families-remain-paired-and-structured
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
