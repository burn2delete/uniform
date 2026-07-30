(ns gravity.self-hosting.sh07-c3-compat-source-coverage-test
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_c3_compat_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 C3 compatibility source test is not on the classpath"
        {:id "SH07-C3-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-C3-COVERAGE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn"))) candidate
        :else (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c3-relative-path
  "bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity")
(def ^:private proof-contract-relative-path
  "bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn")
(def ^:private expected-source-byte-count 4149)
(def ^:private expected-source-revision-id
  "sha256:8f33d57ab88a8afd206b4a824dfaa38187afd74d9e2309626e10dbaa2b2a2d55")
(def ^:private expected-coverage
  {:fragment-count 4
   :root-form-count 4
   :form-count 238
   :binding-count 270
   :local-binding-count 8
   :resolution-count 70})
(def ^:private expected-core-census
  {:core-node-count 207
   :definition-count 4
   :call-count 31
   :reference-count 50
   :keyword-lookup-count 0
   :core-form-frequencies
   {:literal 99
    :collection-literal 7
    :def 4
    :reference 50
    :call 31
    :if 13
    :fn 3}})
(def ^:private expected-definition-names
  '#{c3-syntax-object-compatibility-contract
     c3-exact-keys?
     c3-compatible-syntax-result?
     c3-package-syntax-result})
(def ^:private rejected-families
  {"core-shape" "C6-CORE-SHAPE"
   "lowering-gap" "C6-LOWERING-GAP"})
(def ^:private zero-id
  (str "sha256:" (apply str (repeat 64 "0"))))

(defn- path [relative] (str (.resolve @root relative)))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B21 coordinator adapter is absent"
        {:id "SH07-C3-COVERAGE-ADAPTER-ABSENT"
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
        "SH-07-B21 records are not uniquely identifiable"
        {:id "SH07-C3-COVERAGE-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

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

(def ^:private c3-artifact
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path c3-relative-path))))

(def ^:private c3-upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @c3-artifact))))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-c3-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/c3_syntax_object_model.qst")
          left-path (path c3-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        {:left @c3-artifact
         :right
         ((required-var 'sh07-core-file-artifact) (str right-path))
         :left-path left-path
         :right-path (str right-path)}
        (finally
          (delete-tree! temp-root))))))

(deftest sh07-b21-proof-contract-registers-c3-source-exactly
  (let [contract
        (edn/read-string
         (slurp (path proof-contract-relative-path)))
        documents (set (:governing-documents contract))
        source-text
        (String. (source-bytes (path c3-relative-path))
                 java.nio.charset.StandardCharsets/UTF_8)]
    (is (= "SH-07-B21" (:coverage-milestone contract)))
    (is (= c3-relative-path
           (get-in contract [:authoritative-modules :c3-compat])))
    (is (= {:keyword-lookups 0}
           (get-in contract
                   [:required-core-product-counts :c3-compat])))
    (doseq [document
            ["docs/phase-01-core-language/011-l1-surface-syntax-specification.md"
             "docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md"
             "docs/phase-01-core-language/014-l4-macro-system-specification.md"
             "docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md"
             "docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md"]]
      (is (contains? documents document)))
    (is (= expected-source-byte-count
           (alength (source-bytes (path c3-relative-path)))))
    (is (= expected-source-revision-id
           (sha256-id (source-bytes (path c3-relative-path)))))
    (is (string/includes? source-text
                          ":authentication-credit? false"))
    (is (string/includes? source-text
                          ":authoritative-syntax-result? false"))
    (is (string/includes? source-text
                          ":authoritative-route :gravity.bootstrap.syntax"))
    (is (contains? (set (:nonclaims contract))
                   :c3-adapter-retirement))
    (is (contains? (set (:nonclaims contract)) :sh07-complete))))

(deftest sh07-b21-c3-source-has-exact-authentic-coverage
  (let [artifact @c3-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        coverage-record (:fragment-coverage core-artifact)
        fragments (:fragment-manifest authenticated-request)]
    (is (= :accepted (:status artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= "SH-07-B15" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.compiler.c3-syntax-object-model
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

(deftest sh07-b21-c3-compatibility-and-call-boundaries-are-exact
  (let [artifact @c3-artifact
        core-artifact (core artifact)
        nodes (:nodes core-artifact)
        node-by-id (exactly-once-index nodes :node-id)
        reference-by-node-id
        (exactly-once-index (:reference-uses core-artifact) :core-node-id)
        calls (:calls core-artifact)
        operator-symbols
        (mapv
         #(get-in reference-by-node-id
                  [(:operator-node-id %) :symbol])
         calls)]
    (is (= expected-definition-names
           (set (map :name (:definitions core-artifact)))))
    (is (= 3 (count (filter #(= :fn (:core-form %)) nodes))))
    (is (= 13 (count (filter #(= :if (:core-form %)) nodes))))
    (is (= 11 (get (frequencies operator-symbols) 'get)))
    (is (empty? (:keyword-lookups core-artifact)))
    (is (zero?
         (count
          (filter #(= :keyword-map-lookup (:core-form %)) nodes))))
    (doseq [call calls]
      (let [call-node (get node-by-id (:core-node-id call))]
        (is (= :call (:core-form call-node)))
        (is (= :operator-then-arguments
               (:evaluation-order call)
               (get-in call-node [:evaluation :kind])))))))

(deftest sh07-b21-c3-is-deterministic-path-neutral-and-provenanced
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

(deftest sh07-b21-c3-replay-and-alteration-containment-pass
  (let [artifact @c3-artifact
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
            [["call binding"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :calls 0 :operator-binding-id]
               zero-id)
              :calls-replay?]
             ["actual path provenance"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :provenance :actual-source-path]
               "/altered/root/c3_syntax_object_model.gravity")
              :provenance-retained?]
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]]]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact @c3-upstream-verification)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (contains? failed expected-check))
          (is (seq failed)))))))

(deftest sh07-b21-existing-rejected-families-remain-paired-and-structured
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
