(ns gravity.self-hosting.sh07-reader-source-coverage-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]
            [gravity.self-hosting.sh07-proof-census :as proof-census]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh07_reader_source_coverage_test.clj")]
    (when-not resource
      (throw
       (ex-info
        "SH-07 reader source coverage test is not on the classpath"
        {:id "SH07-READER-COVERAGE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info
          "Repository root could not be located"
          {:id "SH07-READER-COVERAGE-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private reader-relative-path
  "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
(def ^:private expected-reader-source-revision-id
  "sha256:bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77")
(def ^:private expected-coverage
  ;; A source change updates this census only after the complete authentic
  ;; SH-05 -> SH-06 -> SH-07 path and the resulting products are inspected.
  {:fragment-count 298
   :root-form-count 298
   :form-count 22209
   :binding-count 1744
   :resolution-count 7868})
(def ^:private expected-executable-census
  {:qualified-definition-count 20
   :qualified-call-count 30
   :fully-qualified-call-count 30
   :quote-core-node-count 24
   :quote-reference-count 0
   :quote-call-count 0})
(def ^:private expected-qualified-call-frequencies
  {'reader/build-forms 1
   'reader/execute-binary 1
   'reader/execute-compiled-program 1
   'reader/execute-compiler-driver 1
   'reader/execute-core-bootstrap-runtime 1
   'reader/execute-diverse-bootstrap-verification 1
   'reader/execute-formal-release-governance-seed-retirement 1
   'reader/execute-release-attestation-seed-retirement 1
   'reader/execute-runtime-entrypoint 1
   'reader/execute-runtime-image 1
   'reader/execute-self-hosted-runtime 1
   'reader/execute-verified-boot-chain 1
   'reader/forms-from-tokens 5
   'reader/read-with-table 1
   'reader/realize-tokens 1
   'reader/run-token-automaton 2
   'reader/scan-tokens 1
   'reader/source-characters 6
   'reader/tokens-from-characters 1
   'reader/tokens-from-classifier 1})
(def ^:private expected-resource-census
  {:nodes 1446007
   :executable-scalar-bytes 98281260
   :exact-utf8-scalar-bytes 24579750
   :maximum-depth 25
   :maximum-width 22209})
(def ^:private expected-template-resource-census
  {:status :complete
   :nodes 9172831
   :aggregate-nodes 1282698
   :component-nodes 9172830
   :scalar-bytes 522603428
   :maximum-depth 13
   :maximum-width 22209})
(def ^:private expected-digest-resource-census
  {:status :complete
   :nodes 6695903
   :aggregate-nodes 970477
   :component-nodes 6695902
   :scalar-bytes 372860236
   :maximum-depth 14
   :maximum-width 22209})
(def ^:private expected-resolved-resource-census
  {:status :complete
   :nodes 6974903
   :aggregate-nodes 1007957
   :component-nodes 6974902
   :scalar-bytes 482574544
   :maximum-depth 13
   :maximum-width 22209})
(def ^:private expected-request-bounds
  {:maximum-forms 65536
   :maximum-fragment-forms 1024
   :maximum-fragments 1024
   :maximum-bindings 2048
   :maximum-module-resolutions 65536
   :maximum-resolutions 2048
   :maximum-carrier-nodes 8388608
   :maximum-carrier-depth 256
   :maximum-carrier-width 65536
   :maximum-scalar-bytes 268435456
   :maximum-template-carrier-nodes 33554432
   :maximum-template-carrier-depth 256
   :maximum-template-carrier-width 65536
   :maximum-template-scalar-bytes 1073741824
   :maximum-resolved-core-carrier-nodes 16777216
   :maximum-resolved-core-carrier-depth 256
   :maximum-resolved-core-carrier-width 65536
   :maximum-resolved-core-scalar-bytes 1073741824
   :maximum-generated-digest-carrier-nodes 16777216
   :maximum-generated-digest-carrier-depth 256
   :maximum-generated-digest-carrier-width 65536
   :maximum-generated-digest-scalar-bytes 1073741824})
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
        "Required SH-07-B17 coordinator adapter is absent"
        {:id "SH07-READER-COVERAGE-ADAPTER-ABSENT"
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

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info
        "SH-07-B17 records are not uniquely identifiable"
        {:id "SH07-READER-COVERAGE-AMBIGUOUS-INDEX"
         :key key-name
         :record-count (count records)
         :unique-count (count index)})))
    index))

(defn- coverage
  [artifact]
  (let [authenticated-request (request artifact)
        fragments (:fragment-manifest authenticated-request)]
    {:fragment-count
     (count fragments)
     :root-form-count
     (count (:top-level-form-ids authenticated-request))
     :form-count
     (count (:forms authenticated-request))
     :binding-count
     (count (:binding-table authenticated-request))
     :resolution-count
     (count (:resolution-table authenticated-request))}))

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
        (when (and (string? (:id data))
                   (keyword? (:stage data)))
          (assoc data :rule (:id data)))
        (when (= :gravity/sh07-core-diagnostic
                 (get-in data [:diagnostic :artifact]))
          (:diagnostic data))
        (when (= :gravity/sh07-core-diagnostic (:artifact value)) value)
        (when (= :gravity/sh07-core-diagnostic
                 (get-in value [:diagnostic :artifact]))
          (:diagnostic value)))))

(def ^:private reader-artifact
  ;; This process-local delay avoids rebuilding the same authentic product for
  ;; every assertion. It is not persisted and is not authoritative evidence.
  (delay
    ((required-var 'sh07-core-file-artifact)
     (path reader-relative-path))))

(def ^:private upstream-verification
  (delay
    ((required-var 'sh06-resolution-artifact-verification)
     (:sh06-resolution-artifact @reader-artifact))))

(def ^:private reader-verification
  (delay
    ((required-var 'sh07-core-artifact-verification)
     @reader-artifact)))

(def ^:private parity-artifacts
  (delay
    (let [temp-root
          (java.nio.file.Files/createTempDirectory
           "gravity-sh07-reader-coverage-"
           (make-array java.nio.file.attribute.FileAttribute 0))
          right-path (.resolve temp-root "right/reader.qst")
          left-path (path reader-relative-path)
          bytes (source-bytes left-path)]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent right-path)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         right-path bytes (make-array java.nio.file.OpenOption 0))
        (let [file-artifact (required-var 'sh07-core-file-artifact)]
          {:left @reader-artifact
           :right (file-artifact (str right-path))
           :left-path left-path
           :right-path (str right-path)})
        (finally
          (delete-tree! temp-root))))))

(defn- executable-census
  [artifact]
  (let [authenticated-request (request artifact)
        core-artifact (core artifact)
        binding-by-id
        (exactly-once-index
         (:binding-table authenticated-request) :binding-id)
        resolution-by-syntax-id
        (exactly-once-index
         (:resolution-table authenticated-request)
         :reference-syntax-id)
        reference-by-node-id
        (exactly-once-index
         (:reference-uses core-artifact) :core-node-id)
        definitions (:definitions core-artifact)
        calls (:calls core-artifact)
        qualified-definitions
        (filterv
         #(qualified-symbol? (:name %))
         definitions)
        calls-with-resolution
        (mapv
         (fn [call]
           (let [reference
                 (get reference-by-node-id (:operator-node-id call))
                 resolution
                 (get resolution-by-syntax-id (:syntax-id reference))
                 binding (get binding-by-id (:binding-id reference))]
             {:call call
              :reference reference
              :resolution resolution
              :binding binding}))
         calls)
        qualified-calls
        (filterv
         #(qualified-symbol? (get-in % [:reference :symbol]))
         calls-with-resolution)
        fully-qualified-calls
        (filterv
         #(= :fully-qualified-namespace-binding
             (get-in % [:resolution :resolution-order]))
         calls-with-resolution)
        quote-references
        (filterv #(= 'quote (:symbol %))
                 (:reference-uses core-artifact))
        quote-call-node-ids
        (set
         (map :core-node-id
              (filter #(= 'quote (get-in % [:reference :symbol]))
                      calls-with-resolution)))]
    {:qualified-definition-count (count qualified-definitions)
     :qualified-call-count (count qualified-calls)
     :fully-qualified-call-count (count fully-qualified-calls)
     :quote-core-node-count
     (count
      (filter #(= :quote (:core-form %)) (:nodes core-artifact)))
     :quote-reference-count (count quote-references)
     :quote-call-count (count quote-call-node-ids)}))

(defn- call-lineage-valid?
  [artifact call]
  (let [authenticated-request (request artifact)
        core-artifact (core artifact)
        node-by-id (exactly-once-index (:nodes core-artifact) :node-id)
        reference-by-node-id
        (exactly-once-index
         (:reference-uses core-artifact) :core-node-id)
        binding-by-id
        (exactly-once-index
         (:binding-table authenticated-request) :binding-id)
        resolution-by-syntax-id
        (exactly-once-index
         (:resolution-table authenticated-request)
         :reference-syntax-id)
        node (get node-by-id (:core-node-id call))
        operator (get reference-by-node-id (:operator-node-id call))
        binding (get binding-by-id (:binding-id operator))
        resolution
        (get resolution-by-syntax-id (:syntax-id operator))
        expected-order
        (into [(:operator-node-id call)] (:argument-node-ids call))]
    (and
     (map? node)
     (map? operator)
     (map? binding)
     (map? resolution)
     (= :call (:core-form node))
     (= expected-order (:children node))
     (= expected-order (:ordered-evaluation-node-ids call))
     (= expected-order
        (mapv :core-node-id (get-in node [:evaluation :order])))
     (= :operator-then-arguments (:evaluation-order call))
     (= :operator-then-arguments (get-in node [:evaluation :kind]))
     (= (:binding-id operator)
        (:binding-id binding)
        (:binding-id resolution)
        (:operator-binding-id call))
     (= (:syntax-id operator) (:reference-syntax-id resolution))
     (= (:definition-syntax-id operator)
        (:definition-syntax-id binding))
     (= (:binding-class operator) (:binding-class binding)))))

(deftest sh07-b17-reader-source-revision-and-authentic-coverage-are-exact
  (let [artifact @reader-artifact
        authenticated-request (request artifact)
        core-artifact (core artifact)
        fragments (:fragment-manifest authenticated-request)
        coverage-record (:fragment-coverage core-artifact)
        binding-by-id
        (exactly-once-index
         (:binding-table authenticated-request) :binding-id)
        form-syntax-ids
        (set (map :syntax-id (:forms authenticated-request)))
        covered-local-binding-ids
        (vec (mapcat :local-binding-ids fragments))
        local-bindings
        (mapv binding-by-id covered-local-binding-ids)]
    (is (= expected-reader-source-revision-id
           (sha256-id (source-bytes (path reader-relative-path)))))
    (is (= :accepted (:status artifact)))
    (is (= :accepted
           (get-in artifact [:sh06-resolution-artifact :status])))
    (is (= :accepted
           (get-in artifact
                   [:sh06-resolution-artifact
                    :sh05-macro-artifact :status])))
    (is (= :complete
           (get-in artifact
                   [:sh06-resolution-artifact
                    :capability-based-proof :status])))
    (is (= "SH-07-B47" (:task artifact)))
    (is (= 15 (:schema-version authenticated-request)))
    (is (= :sh07-b15-keyword-map-lookup
           (:scope authenticated-request)))
    (is (= 'gravity.bootstrap.reader
           (get-in authenticated-request [:module :namespace])))
    (is (= expected-reader-source-revision-id
           (get-in authenticated-request
                   [:module :source-revision-id])
           (get-in authenticated-request
                   [:lineage :source-revision-id])))
    (is (= expected-coverage (coverage artifact)))
    (is (= (:top-level-form-ids authenticated-request)
           (:covered-root-form-ids coverage-record)
           (vec (mapcat :root-form-ids fragments))))
    (is (= (mapv :form-id (:forms authenticated-request))
           (:covered-form-ids coverage-record)
           (vec (mapcat :form-ids fragments))))
    (is (= (count covered-local-binding-ids)
           (count (set covered-local-binding-ids))))
    (is (every? map? local-bindings))
    (is (every? #(contains? form-syntax-ids
                            (:definition-syntax-id %))
                local-bindings))
    (is (= (mapv :binding-id local-bindings)
           (:covered-local-binding-ids coverage-record)
           covered-local-binding-ids))
    (is (= (mapv :reference-syntax-id
                 (:resolution-table authenticated-request))
           (:covered-resolution-reference-syntax-ids coverage-record)
           (vec
            (mapcat :resolution-reference-syntax-ids fragments))))))

(deftest sh07-b17-reader-definitions-calls-and-quote-are-distinct
  (let [artifact @reader-artifact
        census (executable-census artifact)
        authenticated-request (request artifact)
        core-artifact (core artifact)
        binding-by-id
        (exactly-once-index
         (:binding-table authenticated-request) :binding-id)
        resolution-by-syntax-id
        (exactly-once-index
         (:resolution-table authenticated-request)
         :reference-syntax-id)
        reference-by-node-id
        (exactly-once-index
         (:reference-uses core-artifact) :core-node-id)
        node-by-id
        (exactly-once-index (:nodes core-artifact) :node-id)
        qualified-definition-symbols
        (mapv
         (fn [definition]
           (let [binding (get binding-by-id (:binding-id definition))]
             (symbol (str (:namespace binding))
                     (name (:name binding)))))
         (filterv #(qualified-symbol? (:name %))
                  (:definitions core-artifact)))
        qualified-source-definitions
        (filterv #(qualified-symbol? (:name %))
                 (:definitions core-artifact))
        qualified-source-calls
        (filterv
         (fn [call]
           (qualified-symbol?
            (:symbol
             (get reference-by-node-id (:operator-node-id call)))))
         (:calls core-artifact))]
    (is (= expected-executable-census census))
    (is (= (count qualified-definition-symbols)
           (count (set qualified-definition-symbols))))
    (is (= (set (keys expected-qualified-call-frequencies))
           (set qualified-definition-symbols)))
    (is (= #{"reader"}
           (set (map (comp clojure.core/namespace :name)
                     qualified-source-definitions))))
    (is (= 20 (count qualified-source-definitions)))
    (doseq [definition qualified-source-definitions]
      (let [binding (get binding-by-id (:binding-id definition))
            node (get node-by-id (:core-node-id definition))]
        (is (= 'reader (:namespace binding)))
        (is (= (name (:name definition))
               (name (:name binding))))
        (is (= :namespace (:binding-class binding)))
        (is (= :function (:kind binding)))
        (is (= :private (:visibility binding)))
        (is (= [:meta] (:profile-set binding)))
        (is (= [:jvm] (:target-set binding)))
        (is (= :def (:core-form node)))
        (is (= [(:binding-id definition)]
               (:resolved-binding-ids node)))
        (is (= (:name definition)
               (get-in node [:attributes :name])))
        (is (= (:syntax-id definition)
               (get-in node [:source :syntax-id])))
        (is (contains? node-by-id (:value-node-id definition)))))
    (is (= 30 (count qualified-source-calls)))
    (is (= expected-qualified-call-frequencies
           (frequencies
            (map
             #(get-in reference-by-node-id
                      [(:operator-node-id %) :symbol])
             qualified-source-calls))))
    (doseq [call qualified-source-calls]
      (let [reference
            (get reference-by-node-id (:operator-node-id call))
            resolution
            (get resolution-by-syntax-id (:syntax-id reference))
            binding (get binding-by-id (:binding-id reference))
            reference-node (get node-by-id (:core-node-id reference))
            call-node (get node-by-id (:core-node-id call))]
        (is (= "reader"
               (clojure.core/namespace (:symbol reference))))
        (is (= :operator (:position reference)
               (:position resolution)))
        (is (= :fully-qualified-namespace-binding
               (:resolution-order resolution)))
        (is (= 'reader (:namespace binding)))
        (is (= (name (:symbol reference))
               (name (:name binding))))
        (is (= :namespace (:binding-class binding)))
        (is (= :function (:kind binding)))
        (is (= :reference (:core-form reference-node)))
        (is (= [(:binding-id reference)]
               (:resolved-binding-ids reference-node)))
        (is (= :call (:core-form call-node)))
        (is (= :resolved-symbol-call
               (get-in call-node [:attributes :dispatch])))
        (is (= (:binding-id reference)
               (:binding-id resolution)
               (:binding-id binding)
               (:operator-binding-id call)))))
    (is (every? #(call-lineage-valid? artifact %)
                (:calls core-artifact)))
    (is (zero? (:quote-call-count census)))
    (doseq [node
            (filter #(= :quote (:core-form %))
                    (:nodes core-artifact))]
      (is (= [] (:children node)))
      (is (= :no-evaluation (get-in node [:evaluation :kind])))
      (is (= [] (get-in node [:evaluation :order])))
      (is (= #{:quoted-form-id :quoted-syntax-id
               :quoted-kind :quoted-value}
             (set (keys (:attributes node))))))))

(deftest sh07-b17-reader-request-is-bounded-before-lowering
  (let [artifact @reader-artifact
        authenticated-request (request artifact)
        resource-census
        (proof-census/carrier-census authenticated-request)
        template-resource-census
        ((required-var 'sh07-core-template-carrier-census)
         (get-in artifact
                 [:gravity-core-boundary :raw-template-result
                  :core-template]))
        digest-resource-census
        ((required-var 'sh07-core-template-carrier-census)
         (get-in artifact
                 [:gravity-core-boundary :digest-requests]))
        resolved-resource-census
        ((required-var 'sh07-core-template-carrier-census)
         (core artifact))
        bounds
        (get-in artifact
                [:gravity-core-boundary :raw-template-result :bounds])
        fragments (:fragment-manifest authenticated-request)]
    (is (= expected-resource-census
           (select-keys resource-census
                        (keys expected-resource-census))))
    (is (= expected-template-resource-census
           template-resource-census))
    (is (= expected-digest-resource-census
           digest-resource-census))
    (is (= expected-resolved-resource-census
           resolved-resource-census))
    (is (nat-int? (:aggregate-nodes resource-census)))
    (is (<= (:aggregate-nodes resource-census)
            (:nodes resource-census)))
    (is (= expected-request-bounds
           (select-keys bounds (keys expected-request-bounds))))
    (is (<= (:form-count expected-coverage)
            (:maximum-forms bounds)))
    (is (<= (:binding-count expected-coverage)
            (:maximum-bindings bounds)))
    (is (<= (:resolution-count expected-coverage)
            (:maximum-module-resolutions bounds)))
    (is (<= (:fragment-count expected-coverage)
            (:maximum-fragments bounds)))
    (is (<= (:nodes resource-census)
            (:maximum-carrier-nodes bounds)))
    (is (<= (:maximum-depth resource-census)
            (:maximum-carrier-depth bounds)))
    (is (<= (:maximum-width resource-census)
            (:maximum-carrier-width bounds)))
    (is (<= (:executable-scalar-bytes resource-census)
            (:maximum-scalar-bytes bounds)))
    (is (<= (:nodes template-resource-census)
            (:maximum-template-carrier-nodes bounds)))
    (is (<= (:scalar-bytes template-resource-census)
            (:maximum-template-scalar-bytes bounds)))
    (is (<= (:maximum-depth template-resource-census)
            (:maximum-template-carrier-depth bounds)))
    (is (<= (:maximum-width template-resource-census)
            (:maximum-template-carrier-width bounds)))
    (is (<= (:nodes digest-resource-census)
            (:maximum-generated-digest-carrier-nodes bounds)))
    (is (<= (:scalar-bytes digest-resource-census)
            (:maximum-generated-digest-scalar-bytes bounds)))
    (is (<= (:maximum-depth digest-resource-census)
            (:maximum-generated-digest-carrier-depth bounds)))
    (is (<= (:maximum-width digest-resource-census)
            (:maximum-generated-digest-carrier-width bounds)))
    (is (<= (:nodes resolved-resource-census)
            (:maximum-resolved-core-carrier-nodes bounds)))
    (is (<= (:scalar-bytes resolved-resource-census)
            (:maximum-resolved-core-scalar-bytes bounds)))
    (is (<= (:maximum-depth resolved-resource-census)
            (:maximum-resolved-core-carrier-depth bounds)))
    (is (<= (:maximum-width resolved-resource-census)
            (:maximum-resolved-core-carrier-width bounds)))
    (is (every?
         #(<= (count (:form-ids %))
              (:maximum-fragment-forms bounds))
         fragments))
    (is (every?
         #(<= (count (:resolution-reference-syntax-ids %))
              (:maximum-resolutions bounds))
         fragments))))

(deftest sh07-b17-reader-is-deterministic-path-neutral-and-provenanced
  (let [{:keys [left right left-path right-path]} @parity-artifacts]
    (is (= :accepted (:status left) (:status right)))
    (is (= (:artifact-id left) (:artifact-id right)))
    (is (= (identity-input left) (identity-input right)))
    (is (= (coverage left) (coverage right)))
    (is (= (executable-census left) (executable-census right)))
    (is (= (:fragment-manifest (request left))
           (:fragment-manifest (request right))))
    (is (= (:fragment-coverage (core left))
           (:fragment-coverage (core right))))
    (is (= (:module-assembly-manifest (core left))
           (:module-assembly-manifest (core right))))
    (is (= left-path
           (get-in left [:provenance :source-path])
           (get-in (core left) [:provenance :actual-source-path])))
    (is (= right-path
           (get-in right [:provenance :source-path])
           (get-in (core right) [:provenance :actual-source-path])))
    (is (not= left-path right-path))))

(deftest sh07-b17-reader-canonical-replay-and-lineage-pass
  (let [artifact @reader-artifact
        report @reader-verification
        embedded (:capability-based-proof artifact)]
    (is (= :gravity/sh07-core-artifact-verification
           (:artifact report)))
    (is (= :passed (:status report)))
    (is (= [] (:failed-checks report)))
    (is (= :passed (get-in report [:upstream-verification :status])))
    (is (= :gravity/sh07-core-capability-proof (:artifact embedded)))
    (is (= :complete (:status embedded)))
    (is (= [] (:failed-checks embedded)))
    (is (= (:checks report)
           (dissoc embedded :artifact :status :failed-checks)))
    (doseq [check
            [:authenticated-request-replays?
             :canonical-core-replays?
             :reference-uses-replay?
             :calls-replay?
             :fragment-manifest-replay?
             :fragment-coverage-replay?
             :module-assembly-manifest-replay?
             :template-verification-passed?
             :resolved-verification-passed?
             :provenance-retained?]]
      (is (true? (get embedded check))))))

(deftest sh07-b17-reader-request-and-result-alterations-fail-closed
  (let [artifact @reader-artifact
        authenticated-request (request artifact)
        resolution-artifact (:sh06-resolution-artifact artifact)
        request-cases
        {"request projection"
         (assoc authenticated-request :projection-binding zero-id)
         "source revision"
         (assoc-in authenticated-request
                   [:module :source-revision-id]
                   zero-id)
         "source lineage"
         (assoc-in authenticated-request
                   [:lineage :source-revision-id]
                   zero-id)}]
    (doseq [[label altered] request-cases]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var 'sh07-core-run-request-for-test)
                 resolution-artifact altered))
              diagnostic (diagnostic-data result)]
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= true
                 (get-in diagnostic [:facts :fail-closed]))))))
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
             ["stored proof"
              (assoc-in artifact [:capability-based-proof :status]
                        :failed)
              :stored-capability-proof-current?]
             ["actual path provenance"
              (assoc-in
               artifact
               [:gravity-core-boundary :canonical-core-artifact
                :provenance :actual-source-path]
               "/altered/root/reader.gravity")
              :provenance-retained?]]]
      (testing label
        (let [upstream @upstream-verification
              checks
              ((required-var 'sh07-core-verification-checks)
               altered artifact upstream)
              failed
              (set
               (for [[check passed?] checks
                     :when (not (true? passed?))]
                 check))]
          (is (= :passed (:status upstream)))
          (is (contains? failed expected-check))
          (is (seq failed)))))))
