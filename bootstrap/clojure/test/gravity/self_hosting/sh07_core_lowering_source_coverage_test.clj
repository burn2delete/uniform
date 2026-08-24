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
(def ^:private expected-source-byte-count 35852)
(def ^:private expected-source-revision-id
  "sha256:695920b23f2920f310657f0dc010edf2fbe43dd4570a00150f40666494114e44")
(def ^:private expected-coverage
  {:fragment-count 39
   :root-form-count 39
   :form-count 2801
   :binding-count 410
   :local-binding-count 148
   :resolution-count 880})
(def ^:private expected-core-census
  {:core-node-count 2264
   :definition-count 39
   :call-count 349
   :reference-count 637
   :keyword-lookup-count 0
   :core-form-frequencies
   {:let 17
    :fn 34
    :call 349
    :if 129
    :recur 11
    :loop 10
    :reference 637
    :quote 3
    :collection-literal 127
    :literal 908
    :def 39}})
(def ^:private expected-definition-names
  '#{c6-core-node-contract
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
     build-c6-core-node
     build-c6-desugaring-trace
     verify-c6-core-lowering})
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
           (set (map :name definitions))))
    (is (= 39 (count definitions)))
    (is (= 3 (count quote-nodes)))
    (is (= 34 (count fn-nodes)))
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
