(ns gravity.self-hosting.sh07-checked-core-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_checked_core_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07 test source is not on the classpath"
                      {:id "SH07-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07")

(def ^:private accepted-fixtures
  #{"macro-def-fn-literals" "quoted-carrier-payloads"
    "latent-function-order" "control-flow-order"
    "control-flow-truthiness"})

(def ^:private rejected-fixtures
  #{"lowering-gap" "core-shape" "missing-origin"
    "unauthenticated-projection" "empty-do"
    "if-extra-branch" "if-missing-branch" "nested-def"})

(def ^:private diagnostic-rules
  #{"C6-LOWERING-GAP" "C6-CORE-SHAPE" "C6-ORIGIN" "C6-VERIFY"})

(def ^:private diagnostic-remediation
  {"C6-LOWERING-GAP"
   "Use only the declared SH-07-B2 def, fn, quote, if, do, reference, symbol-headed call, and literal subset."
   "C6-CORE-SHAPE"
   "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape."
   "C6-ORIGIN"
   "Preserve exact syntax ids, semantic spans, and generated-origin chains."
   "C6-VERIFY"
   "Replay the Gravity template and bind every digest ordinal exactly once."})

(def ^:private public-artifact-keys
  #{:kind :status :slice :task :document-set :governing-document
    :artifact-id :sh06-resolution-artifact :gravity-core-boundary
    :provenance :pass :execution-boundary :capability-based-proof
    :diagnostics})

(def ^:private core-boundary-keys
  #{:slice :owner :adapter-contract :plan-binding
    :authenticated-sh06-resolution-artifact
    :authenticated-core-request :raw-template-result
    :canonical-core-artifact :digest-requests :resolved-digests
    :template-verification :resolved-verification
    :authenticated-envelope-descriptor :authenticated-envelope
    :target-source-reread? :clojure-adapter-residual? :self-hosted?})

(def ^:private canonical-core-keys
  #{:artifact :schema-version :artifact-id :provenance-binding-id
    :module :lineage :projection-binding :root-core-node-ids
    :definitions :nodes :evaluation-order :control-flow
    :reference-uses :calls :source-map
    :preserved-resolution :macro-expansion-trace
    :macro-origin-traces :macro-origin-expectation
    :pending-fact-families :identity-preimage
    :provenance-binding-preimage :provenance :diagnostics})

(def ^:private authenticated-request-keys
  #{:artifact :schema-version :lineage :module :forms
    :top-level-form-ids :binding-table :resolution-table
    :macro-expansion-trace :macro-origin-traces
    :macro-origin-expectation :projection-binding
    :provenance :scope})

(def ^:private diagnostic-keys
  #{:artifact :rule :severity :stage :syntax-id :form-id
    :core-node-id :source-span :generated-origin-chain
    :namespace :profile :target :lowering-rule :facts
    :remediation :diagnostic-id-request})

(def ^:private request-run-keys
  #{:raw-template-result :canonical-core-artifact
    :digest-requests :resolved-digests
    :template-verification :resolved-verification})

(def ^:private verification-keys
  #{:artifact :status :checks :failed-checks :source-path
    :template-verification :resolved-verification
    :upstream-verification})

(def ^:private public-api-signatures
  {'sh07-core-from-resolution-artifact
   '[resolution-artifact]
   'sh07-core-source-artifact
   '[source-path source-text]
   'sh07-core-file-artifact
   '[source-path]
   'sh07-core-artifact-verification
   '[artifact]
   'sh07-core-capability-based-proof
   '[artifact]
   'sh07-core-artifact-identity-input
   '[artifact]})

(def ^:private private-test-signatures
  {'sh07-core-authenticated-request
   '[resolution-artifact]
   'sh07-core-run-request-for-test
   '[authenticated-request]
   'sh07-core-verification-checks
   '[artifact expected upstream-verification]
   'sh07-core-from-authenticated-request
   '[resolution-artifact authenticated-request]})

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-text
  [source-path]
  (String. (source-bytes source-path)
           java.nio.charset.StandardCharsets/UTF_8))

(defn- contains-expected-key?
  [value]
  (cond
    (map? value)
    (or (some #(contains? #{:expected-rule :expected-stage
                            :expected-severity :expected-remediation}
                          %)
              (keys value))
        (some contains-expected-key? (vals value)))

    (coll? value)
    (boolean (some contains-expected-key? value))

    :else false))

(defn- sha256-id?
  [value]
  (boolean
   (and (string? value)
        (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07 coordinator adapter is absent"
        {:id "SH07-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         (merge public-api-signatures private-test-signatures)}))))

(def ^:private sh06-fixture-artifacts (atom {}))
(def ^:private sh07-fixture-artifacts (atom {}))
(def ^:private sh07-fixture-requests (atom {}))
(def ^:private sh07-verification-reports
  (java.util.IdentityHashMap.))

(defn- cached-artifact
  [cache key build]
  (if-let [artifact (get @cache key)]
    artifact
    (let [artifact (build)]
      (swap! cache assoc key artifact)
      artifact)))

(defn- identity-cached-artifact
  [^java.util.IdentityHashMap cache key build]
  (locking cache
    (if (.containsKey cache key)
      (.get cache key)
      (let [artifact (build)]
        (.put cache key artifact)
        artifact))))

(defn- fresh-sh06-file-artifact
  [source-path]
  ((required-var 'sh06-resolution-file-artifact) source-path))

(defn- sh06-file-artifact
  [source-path]
  (cached-artifact sh06-fixture-artifacts source-path
                   #(fresh-sh06-file-artifact source-path)))

(defn- sh07-from-resolution
  [resolution-artifact]
  ((required-var 'sh07-core-from-resolution-artifact)
   resolution-artifact))

(defn- sh07-source-artifact
  [source-path text]
  ((required-var 'sh07-core-source-artifact) source-path text))

(defn- sh07-file-artifact
  [source-path]
  (cached-artifact
   sh07-fixture-artifacts source-path
   #((required-var 'sh07-core-file-artifact) source-path)))

(defn- fresh-sh07-file-artifact
  [source-path]
  ((required-var 'sh07-core-file-artifact) source-path))

(defn- sh07-verification
  [artifact]
  (identity-cached-artifact
   sh07-verification-reports artifact
   #((required-var 'sh07-core-artifact-verification) artifact)))

(defn- fresh-sh07-verification
  [artifact]
  ((required-var 'sh07-core-artifact-verification) artifact))

(defn- sh07-capability-proof
  [artifact]
  ((required-var 'sh07-core-capability-based-proof) artifact))

(defn- sh07-fast-verification-checks
  [artifact expected upstream-verification]
  ((required-var 'sh07-core-verification-checks)
   artifact expected upstream-verification))

(defn- sh07-identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- sh07-request
  [resolution-artifact]
  (let [cache-key
        [(:artifact-id resolution-artifact)
         (get-in resolution-artifact [:provenance :source-path])]]
    (cached-artifact
     sh07-fixture-requests cache-key
     #((required-var 'sh07-core-authenticated-request)
       resolution-artifact))))

(defn- sh07-from-request
  [resolution-artifact request]
  ((required-var 'sh07-core-from-authenticated-request)
   resolution-artifact request))

(defn- sh07-run-request
  [request]
  ((required-var 'sh07-core-run-request-for-test) request))

(def ^:private c2-artifacts (atom {}))

(defn- c2-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @c2-artifacts key)
        (let [artifact
              (bootstrap/compiler-c2-reader-file-artifact
               (fixture-path family basename extension))]
          (swap! c2-artifacts assoc key artifact)
          artifact))))

(defn- ns-metadata
  [artifact]
  (let [ns-form (first (:parsed-semantic-values artifact))
        clause (some #(when (and (seq? %) (= :metadata (first %))) %)
                     (drop 2 ns-form))]
    (second clause)))

(defn- rejection-oracle
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:sh07]))

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
  (let [exception-data (:exception-data result)
        value (:value result)
        candidates
        (remove nil?
                [(when (= :gravity/sh07-core-diagnostic
                          (:artifact exception-data))
                   exception-data)
                 (when (= :gravity/sh07-core-diagnostic
                          (get-in exception-data [:diagnostic :artifact]))
                   (:diagnostic exception-data))
                 (when (= :gravity/sh07-core-diagnostic (:artifact value))
                   value)
                 (when (= :gravity/sh07-core-diagnostic
                          (get-in value [:diagnostic :artifact]))
                   (:diagnostic value))
                 (when (= :gravity/sh07-core-diagnostic
                          (get-in value [:diagnostics 0 :artifact]))
                   (get-in value [:diagnostics 0]))])]
    (when (= 1 (count candidates))
      (first candidates))))

(defn- assert-diagnostic
  [result expected-rule expected-path]
  (let [diagnostic (diagnostic-data result)]
    (is (nil? (:raw-host-error result))
        (str expected-rule " must not escape as a host error: "
             (:raw-host-error result)))
    (is (map? diagnostic)
        (str expected-rule " must be a structured diagnostic: " result))
    (is (= diagnostic-keys (set (keys diagnostic))))
    (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
    (is (= expected-rule (:rule diagnostic)))
    (is (= :core-lowering (:stage diagnostic)))
    (is (= :error (:severity diagnostic)))
    (is (= (diagnostic-remediation expected-rule)
           (:remediation diagnostic)))
    (is (map? (:source-span diagnostic)))
    (is (= expected-path (get-in diagnostic [:source-span :source])))
    (is (= :sh07-b2-core-lowering (:lowering-rule diagnostic)))
    (is (nil? (:core-node-id diagnostic)))
    (is (map? (:facts diagnostic)))
    diagnostic))

(defn- core-nodes
  [artifact]
  (get-in artifact [:gravity-core-boundary
                    :canonical-core-artifact :nodes]))

(defn- evaluation-records
  [artifact]
  (get-in artifact [:gravity-core-boundary
                    :canonical-core-artifact :evaluation-order]))

(defn- artifact-id
  [artifact]
  (:artifact-id artifact))

(defn- provenance-path
  [artifact]
  (get-in artifact [:provenance :source-path]))

(defn- core-artifact
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- core-identity-input
  [artifact]
  (get-in artifact [:gravity-core-boundary
                    :canonical-core-artifact :identity-preimage]))

(defn- core-provenance
  [artifact]
  (get-in artifact [:gravity-core-boundary
                    :canonical-core-artifact :provenance]))

(defn- digest-requests
  [artifact]
  (get-in artifact [:gravity-core-boundary :digest-requests]))

(defn- template-verification
  [artifact]
  (get-in artifact [:gravity-core-boundary :template-verification]))

(defn- resolved-verification
  [artifact]
  (get-in artifact [:gravity-core-boundary :resolved-verification]))

(defn- index-exactly-once
  [records key-name]
  (let [ids (mapv key-name records)
        index (zipmap ids records)]
    (when-not (and (vector? records)
                   (every? some? ids)
                   (= (count records) (count index)))
      (throw (ex-info "SH07 records must be uniquely indexed"
                      {:id "SH07-TEST-AMBIGUOUS-INDEX"
                       :key key-name
                       :record-count (count records)
                       :unique-count (count index)})))
    index))

(defn- exact-record
  [records key-name expected]
  (let [matches (filterv #(= expected (get % key-name)) records)]
    (when-not (= 1 (count matches))
      (throw (ex-info "SH07 exact record selection failed"
                      {:id "SH07-TEST-EXACT-RECORD"
                       :key key-name :expected expected
                       :match-count (count matches)})))
    (first matches)))

(defn- transitive-node-closure
  [node-index root-node-ids]
  (loop [frontier (vec root-node-ids)
         visited #{}]
    (if (empty? frontier)
      visited
      (let [node-id (peek frontier)
            remaining (pop frontier)]
        (if (contains? visited node-id)
          (recur remaining visited)
          (let [node (get node-index node-id)]
            (when-not node
              (throw (ex-info "SH07 structural child is dangling"
                              {:id "SH07-TEST-DANGLING-CORE-NODE"
                               :node-id node-id})))
            (recur (into remaining (:children node))
                   (conj visited node-id))))))))

(defn- assert-public-artifact-schema
  [artifact]
  (is (= public-artifact-keys (set (keys artifact))))
  (is (= :gravity/sh07-core-artifact (:kind artifact)))
  (is (= :accepted (:status artifact)))
  (is (= :SH-07 (:slice artifact)))
  (is (sha256-id? (:artifact-id artifact)))
  (is (= core-boundary-keys
         (set (keys (:gravity-core-boundary artifact)))))
  (is (= canonical-core-keys
         (set (keys (core-artifact artifact)))))
  (is (= :gravity/sh07-canonical-core-artifact
         (:artifact (core-artifact artifact))))
  (is (= :passed (:status (template-verification artifact))))
  (is (= :passed (:status (resolved-verification artifact))))
  (is (vector? (digest-requests artifact))))

(defn- assert-authenticated-request-schema
  [request]
  (is (= authenticated-request-keys (set (keys request))))
  (is (= :gravity/sh07-authenticated-sh06-core-request
         (:artifact request)))
  (is (= 3 (:schema-version request)))
  (is (= :sh07-b2-meta-jvm-core (:scope request)))
  (is (sha256-id? (:projection-binding request)))
  (is (= #{:actual-source-path}
         (set (keys (:provenance request))))))

(defn- run-core
  [run]
  (:canonical-core-artifact run))

(defn- assert-request-run-schema
  [run]
  (is (= request-run-keys (set (keys run))))
  (is (= :passed (get-in run [:template-verification :status])))
  (is (= :passed (get-in run [:resolved-verification :status])))
  (is (= :gravity/sh07-canonical-core-artifact
         (get-in run [:canonical-core-artifact :artifact])))
  (is (= canonical-core-keys
         (set (keys (:canonical-core-artifact run))))))

(defn- replace-record
  [records key-name id transform]
  (let [matches (keep-indexed
                 (fn [index record]
                   (when (= id (get record key-name)) index))
                 records)]
    (when-not (= 1 (count matches))
      (throw (ex-info "SH07 exact record replacement failed"
                      {:id "SH07-TEST-EXACT-REPLACEMENT"
                       :key key-name :expected id
                       :match-count (count matches)})))
    (update records (first matches) transform)))

(defn- form-source
  [form]
  {:syntax-id (:syntax-id form)
   :form-id (:form-id form)
   :semantic-span (:source-span form)
   :origin-chain (:origin-chain form)
   :generated-origin (:generated-origin form)})

(defn- mutate-macro-origin
  [request syntax-id transform]
  (update
   request :forms replace-record :syntax-id syntax-id
   (fn [form]
     (let [origins (:generated-origin form)
           matches
           (keep-indexed
            (fn [index origin]
              (when (= :macro-expansion (:kind origin)) index))
            origins)]
       (when-not (= 1 (count matches))
         (throw (ex-info "one exact macro origin is required"
                         {:id "SH07-TEST-EXACT-MACRO-ORIGIN"
                          :syntax-id syntax-id
                          :match-count (count matches)})))
       (update form :generated-origin
               #(update % (first matches) transform))))))

(defn- request-form-operator
  [form-index form]
  (when (and (= :list (:kind form))
             (seq (:child-form-ids form)))
    (:value (get form-index (first (:child-form-ids form))))))

(defn- carrier
  [ordinal]
  {:artifact :gravity/sh07-internal-digest-reference
   :schema-version 1
   :ordinal ordinal
   :authority :sh07-digest-resolver})

(defn- alternate-sha
  [digit]
  (str "sha256:" (apply str (repeat 64 digit))))

(defn- ordinal-sha
  [ordinal]
  (str "sha256:" (format "%064x" (biginteger ordinal))))

(defn- literal-boundary-request
  [request form-count]
  (let [template
        (let [matches (filterv #(and (= :integer (:kind %))
                                     (= 42 (:value %)))
                               (:forms request))]
          (when-not (= 1 (count matches))
            (throw (ex-info "one integer form template is required"
                            {:id "SH07-TEST-LITERAL-TEMPLATE"
                             :match-count (count matches)})))
          (first matches))
        forms
        (mapv
         (fn [ordinal]
           (assoc template
                  :form-id (ordinal-sha (+ 100000 ordinal))
                  :syntax-id (ordinal-sha (+ 200000 ordinal))
                  :child-form-ids []
                  :origin-chain []
                  :generated-origin []
                  :metadata {}
                  :value ordinal))
         (range form-count))]
    (-> request
        (assoc :forms forms
               :top-level-form-ids (mapv :form-id forms)
               :binding-table []
               :resolution-table []
               :macro-expansion-trace []
               :macro-origin-traces [])
        (assoc-in [:module :exports] [])
        (assoc :macro-origin-expectation
               (assoc (:macro-origin-expectation request)
                      :expanded-defn-count 0
                      :expected-input-syntax-ids []
                      :expected-output-def-syntax-ids []
                      :expected-introduced-fn-syntax-ids [])))))

(defn- delete-tree!
  [root-path]
  (when (java.nio.file.Files/exists root-path
                                    (make-array java.nio.file.LinkOption 0))
    (with-open [stream
                (java.nio.file.Files/walk
                 root-path
                 (make-array java.nio.file.FileVisitOption 0))]
      (doseq [entry (reverse (iterator-seq (.iterator stream)))]
        (java.nio.file.Files/deleteIfExists entry)))))

(deftest sh07-fixture-inventory-and-oracle-isolation
  (testing "the co-canonical fixture inventory is exact and byte-identical"
    (doseq [family ["accepted" "rejected"]]
      (let [expected (if (= family "accepted")
                       accepted-fixtures
                       rejected-fixtures)]
        (is (= expected (fixture-basenames family ".gravity")))
        (is (= expected (fixture-basenames family ".qst")))
        (doseq [basename expected]
          (let [gravity-path (fixture-path family basename ".gravity")
                qst-path (fixture-path family basename ".qst")]
            (is (java.util.Arrays/equals (source-bytes gravity-path)
                                        (source-bytes qst-path)))
            (is (every? #(<= 0 (bit-and 0xff %) 127)
                        (source-bytes gravity-path))))))))
  (testing "only rejected fixture metadata contains the assertion oracle"
    (doseq [basename accepted-fixtures
            extension [".gravity" ".qst"]]
      (is (nil? (get-in (ns-metadata
                         (c2-artifact "accepted" basename extension))
                        [:sh07]))))
    (doseq [basename rejected-fixtures
            extension [".gravity" ".qst"]]
      (let [oracle (rejection-oracle basename extension)]
        (is (contains? diagnostic-rules (:expected-rule oracle)))
        (is (= :core-lowering (:expected-stage oracle)))
        (is (= :error (:expected-severity oracle)))
        (is (= (diagnostic-remediation (:expected-rule oracle))
               (:expected-remediation oracle))))))
  (testing "oracle fields never enter the authenticated executable request"
    (doseq [basename (concat accepted-fixtures rejected-fixtures)]
      (let [family (if (accepted-fixtures basename)
                     "accepted" "rejected")
            resolution
            (sh06-file-artifact
             (fixture-path family basename ".gravity"))
            request (sh07-request resolution)]
        (assert-authenticated-request-schema request)
        (is (not (contains-expected-key? request)) basename)
        (is (nil? (:sh07 request)) basename)))))

(deftest sh07-coordinator-api-signatures-are-exact
  (doseq [[symbol expected] (merge public-api-signatures
                                   private-test-signatures)]
    (let [resolved (required-var symbol)
          arglists (:arglists (meta resolved))]
      (is (= 1 (count arglists)) symbol)
      (is (= expected (first arglists)) symbol)
      (is (fn? @resolved) symbol))))

(deftest sh07-genuine-upstream-boundary-and-supported-core
  (doseq [extension [".gravity" ".qst"]]
    (let [source-path
          (fixture-path "accepted" "macro-def-fn-literals" extension)
          resolution (sh06-file-artifact source-path)
          artifact (sh07-from-resolution resolution)
          nodes (vec (core-nodes artifact))
          boundary (:gravity-core-boundary artifact)
          stored-resolution (:sh06-resolution-artifact artifact)
          stored-sh05 (:sh05-macro-artifact stored-resolution)
          stored-sh04
          (get-in stored-sh05
                  [:gravity-macro-boundary
                   :authenticated-sh04-artifact])
          stored-reader-view (:c2-reader-artifact stored-sh04)
          core-forms (set (map :core-form nodes))
          literal-kinds
          (set (keep #(get-in % [:attributes :literal-kind]) nodes))
          decimal-nodes
          (filterv #(= :decimal
                       (get-in % [:attributes :literal-kind]))
                   nodes)
          decimal-value
          (when (= 1 (count decimal-nodes))
            (get-in (first decimal-nodes)
                    [:attributes :value]))]
      (testing (str "genuine SH03 -> SH05 -> SH06 products " extension)
        (is (= :gravity/sh06-resolution-artifact (:kind resolution)))
        (assert-public-artifact-schema artifact)
        (is (= resolution stored-resolution))
        (is (= resolution
               (:authenticated-sh06-resolution-artifact boundary)))
        (is (= :gravity/sh05-macro-expansion-artifact
               (:kind stored-sh05)))
        (is (= :gravity/stage0-c3-syntax-object-artifact
               (:kind stored-sh04)))
        (is (= :gravity/stage0-c2-reader-document-artifact
               (:kind stored-reader-view)))
        (is (= #{:reader-result-id :semantic-envelope-id
                 :provenance-binding-id}
               (set (keys (:sh03-reader-authentication
                           stored-reader-view)))))
        (is (sha256-id?
             (get-in stored-reader-view
                     [:sh03-reader-authentication
                      :reader-result-id])))
        (is (false? (:target-source-reread? boundary))))
      (testing (str "def, fn, quote, and declared literal subset " extension)
        (is (every? core-forms [:def :fn :quote :literal]))
        (is (every? literal-kinds
                    [:nil :boolean :integer :decimal :ratio
                     :character :string :keyword]))
        (is (= 1 (count decimal-nodes))
            "the accepted fixture has one authoritative decimal literal")
        (is (instance? java.math.BigDecimal decimal-value))
        (when (instance? java.math.BigDecimal decimal-value)
          (is (= "1.25"
                 (.toPlainString ^java.math.BigDecimal decimal-value)))
          (is (= 2 (.scale ^java.math.BigDecimal decimal-value))
              "lowering preserves the source decimal scale"))
        (is (every? #(= [:types :effects :ownership :safety]
                       (:pending-fact-families %))
                    nodes))
        (is (= [:types :effects :ownership :safety]
               (get-in artifact
                       [:gravity-core-boundary
                        :canonical-core-artifact
                        :pending-fact-families])))
        (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
               (get-in artifact
                       [:execution-boundary
                        :downstream-fact-statuses])))
        (is (false? (get-in artifact
                            [:execution-boundary :sh07-complete?])))))))

(deftest sh07-source-and-file-entrypoints-have-the-same-core
  ;; One feature-complete source proves entrypoint equivalence. Both physical
  ;; extensions are exercised by the genuine-boundary, identity, and focused
  ;; carrier/evaluation tests, while the remaining accepted fixtures use the
  ;; cached file entrypoint.
  (doseq [basename ["macro-def-fn-literals"]
          extension [".gravity"]]
    (let [source-path (fixture-path "accepted" basename extension)
          direct (sh07-from-resolution (sh06-file-artifact source-path))
          source (sh07-source-artifact source-path (source-text source-path))
          file (fresh-sh07-file-artifact source-path)]
      (assert-public-artifact-schema direct)
      (assert-public-artifact-schema source)
      (assert-public-artifact-schema file)
      (is (= (sh07-identity-input direct)
             (sh07-identity-input source)
             (sh07-identity-input file))
          [basename extension])
      (is (= (artifact-id direct)
             (artifact-id source)
             (artifact-id file))
          [basename extension]))))

(deftest sh07-carrier-shaped-language-data-remains-ordinary-data
  (doseq [extension [".gravity" ".qst"]]
    (let [source-path
          (fixture-path "accepted" "quoted-carrier-payloads" extension)
          artifact (sh07-file-artifact source-path)
          nodes (core-nodes artifact)
          collections
          (mapv #(get-in % [:attributes :source-value])
                (filter #(= :collection-literal (:core-form %)) nodes))
          quotes
          (mapv #(get-in % [:attributes :quoted-value])
                (filter #(= :quote (:core-form %)) nodes))]
      (assert-public-artifact-schema artifact)
      (is (= 1 (count (filter #(= (carrier 73) (:payload %))
                             collections))))
      (is (= 1 (count (filter #(= :carrier-shaped-key
                                  (get % (carrier 74)))
                             collections))))
      (is (= 1 (count (filter
                       #(and (sequential? %)
                             (= (carrier 75)
                                (get-in (vec %) [0 :value]))
                             (= :quoted-carrier-shaped-key
                                (get-in (vec %)
                                        [1 (carrier 76)])))
                       quotes))))))
  (testing "parameter/module/quote carriers survive resolved verification"
    (let [source-path
          (fixture-path "accepted" "latent-function-order" ".gravity")
          resolution (sh06-file-artifact source-path)
          request (sh07-request resolution)
          parameter-carrier (carrier 0)
          export-carrier (carrier 1)
          key-carrier (carrier 1000000000)
          quote-carrier (carrier 2)
          forms (:forms request)
          form-by-id (index-exactly-once forms :form-id)
          trace
          (let [matches
                (filterv
                 (fn [candidate]
                   (let [def-form
                         (exact-record
                          forms :syntax-id
                          (:output-def-syntax-id candidate))
                         name-form
                         (get form-by-id
                              (nth (:child-form-ids def-form) 1))]
                     (= 'latent-order (:value name-form))))
                 (:macro-origin-traces request))]
            (when-not (= 1 (count matches))
              (throw
               (ex-info
                "latent-order macro trace must be unique"
                {:id "SH07-TEST-LATENT-TRACE"
                 :match-count (count matches)})))
            (first matches))
          fn-syntax-id (:introduced-fn-syntax-id trace)
          fn-form (exact-record forms :syntax-id fn-syntax-id)
          parameter-form-id (nth (:child-form-ids fn-form) 1)
          parameter-form (get form-by-id parameter-form-id)
          body-form-id (nth (:child-form-ids fn-form) 2)
          body-form (get form-by-id body-form-id)
          quote-form
          (let [matches
                (->> (:child-form-ids body-form)
                     (mapv form-by-id)
                     (filterv #(= 'quote
                                  (request-form-operator
                                   form-by-id %))))]
            (when-not (= 1 (count matches))
              (throw
               (ex-info
                "latent-order body must have one structural quote"
                {:id "SH07-TEST-LATENT-QUOTE"
                 :body-form-id body-form-id
                 :match-count (count matches)})))
            (first matches))
          quoted-value-form
          (get form-by-id (nth (:child-form-ids quote-form) 1))
          mutated
          (-> request
              (update :forms replace-record :form-id parameter-form-id
                      #(assoc % :value
                              [parameter-carrier
                               {key-carrier :carrier-map-key}]))
              (update :forms replace-record :form-id
                      (:form-id quoted-value-form)
                      #(assoc % :value
                              [quote-carrier
                               {key-carrier :quote-map-key}]))
              (assoc-in [:module :exports]
                        [export-carrier {key-carrier :module-map-key}])
              (update :macro-expansion-trace
                      (fn [trace]
                        (conj (vec trace)
                              {:opaque-semantic-payload
                               [parameter-carrier export-carrier]}))))
          run (sh07-run-request mutated)
          core (run-core run)
          nodes (:nodes core)
          fn-node (exact-record nodes :source (form-source fn-form))
          quote-node
          (let [matches
                (filterv #(and (= :quote (:core-form %))
                               (= [quote-carrier
                                   {key-carrier :quote-map-key}]
                                  (get-in %
                                          [:attributes :quoted-value])))
                         nodes)]
            (when-not (= 1 (count matches))
              (throw (ex-info "mutated quote node must be unique"
                              {:id "SH07-TEST-QUOTE-NODE"
                               :match-count (count matches)})))
            (first matches))]
      (assert-authenticated-request-schema request)
      (is (= :vector (:kind parameter-form)))
      (is (= :vector (:kind body-form)))
      (is (= '(left right) (:value quoted-value-form)))
      (is (= (:form-id quoted-value-form)
             (nth (:child-form-ids quote-form) 1)))
      (assert-request-run-schema run)
      (is (<= 3 (count (:digest-requests run)))
          "ordinals 0, 1, and 2 collide with genuine digest requests")
      (is (= [parameter-carrier {key-carrier :carrier-map-key}]
             (get-in fn-node [:attributes :parameters])))
      (is (= [export-carrier {key-carrier :module-map-key}]
             (get-in core [:module :exports])))
      (is (= [quote-carrier {key-carrier :quote-map-key}]
             (get-in quote-node [:attributes :quoted-value])))
      (is (= (:macro-expansion-trace mutated)
             (:macro-expansion-trace core))))))

(deftest sh07-function-bodies-are-latent-and-ordered
  (let [artifact
        (sh07-file-artifact
         (fixture-path "accepted" "latent-function-order" ".gravity"))
        replayed
        (fresh-sh07-file-artifact
         (fixture-path "accepted" "latent-function-order" ".gravity"))
        nodes (vec (core-nodes artifact))
        evaluations (vec (evaluation-records artifact))
        node-by-id (index-exactly-once nodes :node-id)
        evaluation-by-id
        (index-exactly-once evaluations :core-node-id)
        fn-nodes (filterv #(= :fn (:core-form %)) nodes)
        body-records (filterv #(= :function-body (:region %))
                              evaluations)
        body-record-ids (set (map :core-node-id body-records))
        fn-closures
        (mapv
         (fn [fn-node]
           {:syntax-id (get-in fn-node [:source :syntax-id])
            :node-ids
            (transitive-node-closure node-by-id (:children fn-node))})
         fn-nodes)
        ordered-latent-values
        (->> evaluations
             (map #(get node-by-id (:core-node-id %)))
             (keep
              (fn [node]
                (cond
                  (and (= :literal (:core-form node))
                       (contains? #{1 2 3 :middle}
                                  (get-in node [:attributes :value])))
                  (get-in node [:attributes :value])

                  (and (= :quote (:core-form node))
                       (= '(left right)
                          (get-in node [:attributes :quoted-value])))
                  '(left right)

                  :else nil)))
             vec)]
    (assert-public-artifact-schema artifact)
    (is (= 2 (count fn-nodes)))
    (doseq [[fn-node closure]
            (map vector fn-nodes fn-closures)]
      (let [syntax-id (get-in fn-node [:source :syntax-id])
            creation (get evaluation-by-id (:node-id fn-node))
            owned (filterv #(= syntax-id
                                (:owner-function-syntax-id %))
                           body-records)]
        (is (= syntax-id (:syntax-id closure)))
        (is (sha256-id? syntax-id))
        (is (= :function-creation (:kind creation)))
        (is (= :module-initialization (:region creation)))
        (is (nil? (:owner-function-syntax-id creation)))
        (is (= [] (:children creation))
            "function creation evaluates no body children")
        (is (= 1 (count (:children fn-node)))
            "the structural function node retains its body child")
        (is (seq (:node-ids closure)))
        (doseq [node-id (:node-ids closure)]
          (let [record (get evaluation-by-id node-id)]
            (is (map? record))
            (is (= :function-body (:region record)))
            (is (= syntax-id
                   (:owner-function-syntax-id record)))))
        (is (= (:node-ids closure)
               (set (map :core-node-id owned)))
            "the structural closure and owned evaluation records agree")))
    (is (seq body-records))
    (doseq [left-index (range (count fn-closures))
            right-index (range (inc left-index) (count fn-closures))]
      (is (empty?
           (set/intersection
            (:node-ids (nth fn-closures left-index))
            (:node-ids (nth fn-closures right-index))))
          "function-body closures are disjoint"))
    (is (= body-record-ids
           (apply set/union #{} (map :node-ids fn-closures)))
        "function closures exactly partition every function-body record")
    (is (every? #(contains?
                  (set (map (comp :syntax-id :source) fn-nodes))
                  (:owner-function-syntax-id %))
                body-records)
        "every latent record owner names one exact fn source syntax id")
    (doseq [record body-records
            :when (contains? #{:left-to-right :value} (:kind record))]
      (let [node (get node-by-id (:core-node-id record))]
        (when (= :left-to-right (:kind record))
          (is (= (:children node) (:children record))
              "left-to-right records preserve structural child order"))))
    (is (= [1 :middle 2 3 '(left right)] ordered-latent-values))
    (is (vector? evaluations))
    (is (= evaluations (evaluation-records replayed))
        "evaluation order must be deterministic")
    (is (= (count evaluations)
           (count (set (map :core-node-id
                            evaluations))))
        "each lowered node has one stable evaluation-order entry")))

(deftest sh07-public-lowering-gap-and-core-shape-diagnostics
  (doseq [[basename expected-rule]
          [["lowering-gap" "C6-LOWERING-GAP"]
           ["core-shape" "C6-CORE-SHAPE"]]
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "rejected" basename extension)
          result (diagnostic-result #(sh07-file-artifact source-path))]
      (assert-diagnostic result expected-rule source-path))))

(deftest sh07-origin-expectation-is-exact-and-bijective
  (doseq [extension [".gravity" ".qst"]]
    (let [source-path
          (fixture-path "accepted" "latent-function-order" extension)
          resolution (sh06-file-artifact source-path)
          request (sh07-request resolution)
          traces (:macro-origin-traces request)
          expectation (:macro-origin-expectation request)
          forms (:forms request)
          artifact (sh07-from-request resolution request)]
      (assert-public-artifact-schema artifact)
      (is (= (:expanded-defn-count expectation) (count traces)))
      (is (= (set (:expected-input-syntax-ids expectation))
             (set (map :input-syntax-id traces))))
      (is (= (set (:expected-output-def-syntax-ids expectation))
             (set (map :output-def-syntax-id traces))))
      (is (= (set (:expected-introduced-fn-syntax-ids expectation))
             (set (map :introduced-fn-syntax-id traces))))
      (is (= (count traces)
             (count (set (map :input-syntax-id traces)))))
      (is (= (count traces)
             (count (set (map :def-generated-origin-id traces)))))
      (is (= (count traces)
             (count (set (map :fn-generated-origin-id traces)))))
      (doseq [trace traces]
        (let [def-form
              (exact-record forms :syntax-id
                            (:output-def-syntax-id trace))
              fn-form
              (exact-record forms :syntax-id
                            (:introduced-fn-syntax-id trace))
              def-origin
              (exact-record (:generated-origin def-form)
                            :kind :macro-expansion)
              fn-origin
              (exact-record (:generated-origin fn-form)
                            :kind :macro-expansion)]
          (is (= {:origin-id (:def-generated-origin-id trace)
                  :kind :macro-expansion
                  :from-syntax-id (:input-syntax-id trace)
                  :role :introduced-def}
                 def-origin))
          (is (= {:origin-id (:fn-generated-origin-id trace)
                  :kind :macro-expansion
                  :from-syntax-id (:input-syntax-id trace)
                  :role :introduced-fn}
                 fn-origin))
          (is (= (:form-id fn-form)
                 (nth (:child-form-ids def-form) 2))))))))

(deftest sh07-origin-mutations-fail-closed
  (let [source-path
        (fixture-path "accepted" "latent-function-order" ".gravity")
        resolution (sh06-file-artifact source-path)
        request (sh07-request resolution)
        expectation (:macro-origin-expectation request)
        traces (:macro-origin-traces request)
        first-trace (nth traces 0)
        second-trace (nth traces 1)
        def-id (:output-def-syntax-id first-trace)
        fn-id (:introduced-fn-syntax-id first-trace)
        second-def-id (:output-def-syntax-id second-trace)
        second-fn-id (:introduced-fn-syntax-id second-trace)
        form-index (index-exactly-once (:forms request) :form-id)
        traced-syntax-ids
        (set (concat (map :output-def-syntax-id traces)
                     (map :introduced-fn-syntax-id traces)))
        untraced-def
        (let [matches
              (filterv
               #(and (= 'def (request-form-operator form-index %))
                     (not (contains? traced-syntax-ids
                                     (:syntax-id %))))
               (:forms request))]
          (when-not (= 1 (count matches))
            (throw (ex-info "one untraced handwritten def is required"
                            {:id "SH07-TEST-UNTRACED-DEF"
                             :match-count (count matches)})))
          (first matches))
        traced-fn
        (exact-record (:forms request) :syntax-id fn-id)
        extra-def-origin
        {:origin-id (alternate-sha "1")
         :kind :macro-expansion
         :from-syntax-id (:input-syntax-id first-trace)
         :role :introduced-def}
        extra-fn
        (assoc traced-fn
               :form-id (alternate-sha "2")
               :syntax-id (alternate-sha "3")
               :generated-origin
               [{:origin-id (alternate-sha "4")
                 :kind :macro-expansion
                 :from-syntax-id (:input-syntax-id first-trace)
                 :role :introduced-fn}])
        stripped
        (-> request
            (assoc :macro-origin-traces [])
            (update :forms
                    #(mapv (fn [form]
                             (assoc form :generated-origin []))
                           %)))
        mutations
        [["expectation missing"
          :missing-macro-origin-expectation
          (dissoc request :macro-origin-expectation)]
         ["expectation malformed"
          :malformed-or-substituted-macro-origin-expectation
          (assoc request :macro-origin-expectation :not-a-map)]
         ["expectation source revision substituted"
          :malformed-or-substituted-macro-origin-expectation
          (assoc-in request
                    [:macro-origin-expectation :source-revision-id]
                    (alternate-sha "5"))]
         ["expectation SH05 artifact substituted"
          :malformed-or-substituted-macro-origin-expectation
          (assoc-in request
                    [:macro-origin-expectation :sh05-artifact-id]
                    (alternate-sha "6"))]
         ["expectation macro trace substituted"
          :malformed-or-substituted-macro-origin-expectation
          (assoc-in request
                    [:macro-origin-expectation
                     :macro-expansion-trace-id]
                    (alternate-sha "7"))]
         ["expectation count mismatch"
          :malformed-or-substituted-macro-origin-expectation
          (update-in request
                     [:macro-origin-expectation :expanded-defn-count]
                     inc)]
         ["expectation input set mismatch"
          :macro-origin-input-expectation-mismatch
          (assoc-in request
                    [:macro-origin-expectation
                     :expected-input-syntax-ids 0]
                    (alternate-sha "8"))]
         ["expectation output def set mismatch"
          :macro-origin-def-expectation-mismatch
          (assoc-in request
                    [:macro-origin-expectation
                     :expected-output-def-syntax-ids 0]
                    (alternate-sha "9"))]
         ["expectation introduced fn set mismatch"
          :macro-origin-fn-expectation-mismatch
          (assoc-in request
                    [:macro-origin-expectation
                     :expected-introduced-fn-syntax-ids 0]
                    (alternate-sha "a"))]
         ["traces missing"
          :missing-macro-origin-traces
          (dissoc request :macro-origin-traces)]
         ["traces scalar"
          :malformed-or-over-bound-macro-origin-traces
          (assoc request :macro-origin-traces :bad)]
         ["traces over bound"
          :malformed-or-over-bound-macro-origin-traces
          (assoc request :macro-origin-traces
                 (vec (repeat 1025 first-trace)))]
         ["scalar trace element"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request [:macro-origin-traces 0] 42)]
         ["malformed trace element"
          :malformed-or-substituted-macro-origin-trace
          (update-in request [:macro-origin-traces 0]
                     dissoc :macro-expansion-trace-id)]
         ["trace macro substituted"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request [:macro-origin-traces 0 :macro]
                    'not-defn)]
         ["trace source revision substituted"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request
                    [:macro-origin-traces 0 :source-revision-id]
                    (alternate-sha "b"))]
         ["trace SH05 artifact substituted"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request
                    [:macro-origin-traces 0 :sh05-artifact-id]
                    (alternate-sha "c"))]
         ["trace macro expansion id substituted"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request
                    [:macro-origin-traces 0
                     :macro-expansion-trace-id]
                    (alternate-sha "d"))]
         ["trace def origin id malformed"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request
                    [:macro-origin-traces 0
                     :def-generated-origin-id]
                    :malformed)]
         ["trace fn origin id malformed"
          :malformed-or-substituted-macro-origin-trace
          (assoc-in request
                    [:macro-origin-traces 0
                     :fn-generated-origin-id]
                    :malformed)]
         ["traces and origins stripped under nonzero expectation"
          :expanded-defn-count-does-not-match-origin-traces
          stripped]
         ["dangling def syntax"
          :output-def-syntax-dangling
          (assoc-in request [:macro-origin-traces 0
                             :output-def-syntax-id]
                    (alternate-sha "e"))]
         ["dangling fn syntax"
          :introduced-fn-syntax-dangling
          (assoc-in request [:macro-origin-traces 0
                             :introduced-fn-syntax-id]
                    (alternate-sha "f"))]
         ["swapped structural roles"
          :output-def-operator-mismatch
          (-> request
              (assoc-in [:macro-origin-traces 0
                         :output-def-syntax-id] fn-id)
              (assoc-in [:macro-origin-traces 0
                         :introduced-fn-syntax-id] def-id))]
         ["altered def origin role"
          :def-generated-origin-not-exact
          (mutate-macro-origin request def-id
                               #(assoc % :role :introduced-fn))]
         ["altered def origin from"
          :def-generated-origin-not-exact
          (mutate-macro-origin request def-id
                               #(assoc % :from-syntax-id
                                       (alternate-sha "0")))]
         ["altered def origin id"
          :def-generated-origin-not-exact
          (mutate-macro-origin request def-id
                               #(assoc % :origin-id
                                       (alternate-sha "1")))]
         ["malformed def origin id"
          :def-generated-origin-not-exact
          (mutate-macro-origin request def-id
                               #(assoc % :origin-id :malformed))]
         ["altered fn origin role"
          :fn-generated-origin-not-exact
          (mutate-macro-origin request fn-id
                               #(assoc % :role :introduced-def))]
         ["altered fn origin from"
          :fn-generated-origin-not-exact
          (mutate-macro-origin request fn-id
                               #(assoc % :from-syntax-id
                                       (alternate-sha "2")))]
         ["altered fn origin id"
          :fn-generated-origin-not-exact
          (mutate-macro-origin request fn-id
                               #(assoc % :origin-id
                                       (alternate-sha "3")))]
         ["malformed fn origin id"
          :fn-generated-origin-not-exact
          (mutate-macro-origin request fn-id
                               #(assoc % :origin-id :malformed))]
         ["second macro origin on traced def"
          :def-macro-origin-not-bijective
          (update
           request :forms replace-record :syntax-id def-id
           (fn [form]
             (update
              form :generated-origin conj
              (assoc
               (exact-record (:generated-origin form)
                             :kind :macro-expansion)
               :origin-id (alternate-sha "5")))))]
         ["second macro origin on traced fn"
          :fn-macro-origin-not-bijective
          (update
           request :forms replace-record :syntax-id fn-id
           (fn [form]
             (update
              form :generated-origin conj
              (assoc
               (exact-record (:generated-origin form)
                             :kind :macro-expansion)
               :origin-id (alternate-sha "6")))))]
         ["traced def value points at a different genuine fn"
          :introduced-def-value-is-not-traced-fn
          (update
           request :forms replace-record :syntax-id def-id
           (fn [form]
             (assoc-in
              form [:child-form-ids 2]
              (:form-id
               (exact-record (:forms request)
                             :syntax-id second-fn-id)))))]
         ["introduced fn syntax points at genuine non-fn form"
          :introduced-fn-operator-mismatch
          (assoc-in request
                    [:macro-origin-traces 0
                     :introduced-fn-syntax-id]
                    (:syntax-id untraced-def))]
         ["equal def and fn origin ids within one trace"
          :malformed-or-substituted-macro-origin-trace
          (-> request
              (assoc-in
               [:macro-origin-traces 0 :fn-generated-origin-id]
               (:def-generated-origin-id first-trace))
              (mutate-macro-origin
               fn-id
               #(assoc % :origin-id
                       (:def-generated-origin-id first-trace))))]
         ["duplicate causing input"
          :causing-input-owned-by-multiple-traces
          (assoc-in request [:macro-origin-traces 1
                             :input-syntax-id]
                    (:input-syntax-id first-trace))]
         ["duplicate output def"
          :introduced-def-owned-by-multiple-traces
          (assoc-in request [:macro-origin-traces 1
                             :output-def-syntax-id]
                    def-id)]
         ["duplicate introduced fn"
          :introduced-fn-owned-by-multiple-traces
          (assoc-in request [:macro-origin-traces 1
                             :introduced-fn-syntax-id]
                    fn-id)]
         ["def-to-fn cross-role overlap"
          :def-fn-trace-role-overlap
          (assoc-in request [:macro-origin-traces 1
                             :output-def-syntax-id]
                    fn-id)]
         ["fn-to-def cross-role overlap"
          :def-fn-trace-role-overlap
          (assoc-in request [:macro-origin-traces 1
                             :introduced-fn-syntax-id]
                    def-id)]
         ["def-def global origin reuse"
          :generated-origin-id-reused-across-traces
          (assoc-in request [:macro-origin-traces 1
                             :def-generated-origin-id]
                    (:def-generated-origin-id first-trace))]
         ["fn-fn global origin reuse"
          :generated-origin-id-reused-across-traces
          (assoc-in request [:macro-origin-traces 1
                             :fn-generated-origin-id]
                    (:fn-generated-origin-id first-trace))]
         ["def-fn global origin reuse"
          :generated-origin-id-reused-across-traces
          (assoc-in request [:macro-origin-traces 1
                             :def-generated-origin-id]
                    (:fn-generated-origin-id first-trace))]
         ["fn-def global origin reuse"
          :generated-origin-id-reused-across-traces
          (assoc-in request [:macro-origin-traces 1
                             :fn-generated-origin-id]
                    (:def-generated-origin-id first-trace))]
         ["extra generated def origin without trace"
          :generated-core-form-has-no-origin-trace
          (update request :forms replace-record :form-id
                  (:form-id untraced-def)
                  #(assoc % :generated-origin [extra-def-origin]))]
         ["extra generated fn origin without trace"
          :generated-core-form-has-no-origin-trace
          (update request :forms conj extra-fn)]]]
    (is (= 2 (count traces)))
    (is (= 2 (:expanded-defn-count expectation)))
    (is (not= first-trace second-trace))
    (is (not= def-id second-def-id))
    (is (not= fn-id second-fn-id))
    (doseq [[label expected-reason mutation] mutations]
      (testing label
        (let [diagnostic
              (assert-diagnostic
               (diagnostic-result #(sh07-run-request mutation))
               "C6-ORIGIN" source-path)]
          (is (= expected-reason
                 (get-in diagnostic [:facts :reason])))
          (is (= expected-reason
                 (get-in diagnostic
                         [:facts :rule-specific :reason]))))))))

(deftest sh07-zero-origin-handwritten-input-and-opaque-trace-preservation
  (let [handwritten-path
        (fixture-path "accepted" "quoted-carrier-payloads" ".gravity")
        handwritten-request
        (sh07-request (sh06-file-artifact handwritten-path))
        handwritten-run (sh07-run-request handwritten-request)
        macro-path
        (fixture-path "accepted" "latent-function-order" ".gravity")
        macro-request (sh07-request (sh06-file-artifact macro-path))
        macro-run (sh07-run-request macro-request)]
    (is (= 0 (get-in handwritten-request
                     [:macro-origin-expectation :expanded-defn-count])))
    (is (= [] (:macro-origin-traces handwritten-request)))
    (assert-request-run-schema handwritten-run)
    (assert-request-run-schema macro-run)
    (is (= (:macro-expansion-trace handwritten-request)
           (get-in handwritten-run
                   [:canonical-core-artifact
                    :macro-expansion-trace])))
    (is (= (:macro-expansion-trace macro-request)
           (get-in macro-run
                   [:canonical-core-artifact
                    :macro-expansion-trace])))
    (is (= (:macro-origin-traces macro-request)
           (get-in macro-run
                   [:canonical-core-artifact
                    :macro-origin-traces])))))

(deftest sh07-projection-authentication-mutations-fail-closed
  (doseq [extension [".gravity" ".qst"]]
    (let [source-path
          (fixture-path "rejected" "unauthenticated-projection" extension)
          resolution (sh06-file-artifact source-path)
          request (sh07-request resolution)
          mutations
          [(dissoc request :projection-binding)
           (assoc request :projection-binding (alternate-sha "0"))
           (assoc request :artifact
                  :gravity/not-an-authenticated-sh07-request)]]
      (doseq [mutation mutations]
        (assert-diagnostic
         (diagnostic-result #(sh07-from-request resolution mutation))
         "C6-VERIFY" source-path)))))

(deftest sh07-bounds-and-malformed-projections-fail-closed
  (let [source-path
        (fixture-path "accepted" "macro-def-fn-literals" ".gravity")
        resolution (sh06-file-artifact source-path)
        request (sh07-request resolution)
        too-wide
        (assoc request :macro-expansion-trace
               (vec (repeat 1025 {:event :opaque})))
        too-deep
        (assoc request :macro-expansion-trace
               [{:opaque-depth
                 (reduce (fn [value _] [value])
                         :leaf (range 257))}])
        width-diagnostic
        (assert-diagnostic
         (diagnostic-result #(sh07-run-request too-wide))
         "C6-VERIFY" source-path)
        depth-diagnostic
        (assert-diagnostic
         (diagnostic-result #(sh07-run-request too-deep))
         "C6-VERIFY" source-path)]
    (is (= :maximum-forms
           (get-in width-diagnostic
                   [:facts :rule-specific :bound])))
    (is (= 1024 (get-in width-diagnostic
                        [:facts :rule-specific :maximum])))
    (is (= 1025 (get-in width-diagnostic
                        [:facts :rule-specific :observed])))
    (is (= :maximum-carrier-depth
           (get-in depth-diagnostic
                   [:facts :rule-specific :bound])))
    (is (= 256 (get-in depth-diagnostic
                       [:facts :rule-specific :maximum])))
    (is (= 257 (get-in depth-diagnostic
                       [:facts :rule-specific :observed])))))

(deftest sh07-deterministic-path-neutral-identity-retains-provenance
  (doseq [basename accepted-fixtures]
    (let [gravity-path (fixture-path "accepted" basename ".gravity")
          qst-path (fixture-path "accepted" basename ".qst")
          gravity-first (sh07-file-artifact gravity-path)
          gravity-second (fresh-sh07-file-artifact gravity-path)
          qst (sh07-file-artifact qst-path)]
      (assert-public-artifact-schema gravity-first)
      (assert-public-artifact-schema gravity-second)
      (assert-public-artifact-schema qst)
      (is (= (artifact-id gravity-first)
             (artifact-id gravity-second)
             (artifact-id qst))
          basename)
      (is (= (sh07-identity-input gravity-first)
             (sh07-identity-input gravity-second)
             (sh07-identity-input qst))
          basename)
      (is (= (sh07-identity-input gravity-first)
             (core-identity-input gravity-first)))
      (is (= (sh07-identity-input qst)
             (core-identity-input qst)))
      (is (= gravity-path (provenance-path gravity-first)))
      (is (= qst-path (provenance-path qst)))
      (is (= gravity-path
             (:actual-source-path (core-provenance gravity-first))))
      (is (= qst-path
             (:actual-source-path (core-provenance qst))))
      (is (not= (provenance-path gravity-first)
                (provenance-path qst)))))
  (testing "an unrelated checkout root and current working directory are neutral"
    (let [source-path
          (fixture-path "accepted" "macro-def-fn-literals" ".gravity")
          temp-root (java.nio.file.Files/createTempDirectory
                     "gravity-sh07-identity-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
          nested (.resolve temp-root "elsewhere/nested/input.qst")
          original-user-dir (System/getProperty "user.dir")]
      (try
        (java.nio.file.Files/createDirectories
         (.getParent nested)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         nested
         (source-bytes source-path)
         (make-array java.nio.file.OpenOption 0))
        (let [original (sh07-file-artifact source-path)
              copied
              (do
                (System/setProperty "user.dir" (str temp-root))
                (fresh-sh07-file-artifact (str nested)))]
          (is (= (artifact-id original) (artifact-id copied)))
          (is (= (sh07-identity-input original)
                 (sh07-identity-input copied)))
          (is (= (str nested) (provenance-path copied)))
          (is (= (str nested)
                 (:actual-source-path (core-provenance copied))))
          (is (not= (provenance-path original)
                    (provenance-path copied))))
        (finally
          (System/setProperty "user.dir" original-user-dir)
          (delete-tree! temp-root))))))

(deftest sh07-template-replay-and-resolved-artifact-verification
  (doseq [basename accepted-fixtures
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "accepted" basename extension)
          artifact (sh07-file-artifact source-path)
          public-replay?
          (= basename "macro-def-fn-literals")
          verification
          (when public-replay?
            (sh07-verification artifact))
          embedded-proof (:capability-based-proof artifact)
          public-proof
          (when (and (= basename "macro-def-fn-literals")
                     (= extension ".gravity"))
            (sh07-capability-proof artifact))]
      (assert-public-artifact-schema artifact)
      (is (= :gravity/sh07-core-capability-proof
             (:artifact embedded-proof)))
      (is (= :complete (:status embedded-proof)))
      (is (= [] (:failed-checks embedded-proof)))
      (when verification
        (is (= verification-keys (set (keys verification))))
        (is (= :gravity/sh07-core-artifact-verification
               (:artifact verification)))
        (is (= :passed (:status verification)))
        (is (= [] (:failed-checks verification)))
        (is (= :gravity/sh07-core-template-verification
               (get-in verification
                       [:template-verification :artifact])))
        (is (= :passed
               (get-in verification
                       [:template-verification :status])))
        (is (= :gravity/sh07-core-resolved-verification
               (get-in verification
                       [:resolved-verification :artifact])))
        (is (= :passed
               (get-in verification
                       [:resolved-verification :status])))
        (is (= (template-verification artifact)
               (:template-verification verification)))
        (is (= (resolved-verification artifact)
               (:resolved-verification verification)))
        (is (= :passed
               (get-in verification
                       [:upstream-verification :status])))
        (is (= (:checks verification)
               (dissoc embedded-proof
                       :artifact :status :failed-checks))))
      (when public-proof
        (is (= embedded-proof public-proof)
            "one representative public capability replay covers the thin proof wrapper")))))

(deftest sh07-stale-substituted-and-malformed-artifacts-do-not-verify
  (let [source-path
        (fixture-path "accepted" "macro-def-fn-literals" ".gravity")
        artifact (sh07-file-artifact source-path)
        upstream-verification
        {:status
         (if (true?
              (get-in artifact
                      [:capability-based-proof
                       :upstream-verification-passed?]))
           :passed
           :failed)}
        mutations
        [["stale wrapper artifact id"
          (assoc artifact :artifact-id (alternate-sha "1"))]
         ["substituted SH06 lineage"
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :lineage :sh06-artifact-id]
                    (alternate-sha "2"))]
         ["malformed canonical provenance"
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :provenance :actual-source-path]
                    42)]
         ["substituted digest request"
          (assoc-in artifact
                    [:gravity-core-boundary :digest-requests 0
                     :preimage :domain]
                    :gravity/substituted-domain)]
         ["malformed resolved node"
          (assoc-in artifact
                    [:gravity-core-boundary :canonical-core-artifact
                     :nodes 0 :source :semantic-span]
                    :not-a-span)]
         ["decimal scale substitution"
          (update-in
           artifact
           [:gravity-core-boundary :canonical-core-artifact :nodes]
           (fn [nodes]
             (mapv
              (fn [node]
                (if (= :decimal
                       (get-in node [:attributes :literal-kind]))
                  (assoc-in node [:attributes :value]
                            (java.math.BigDecimal. "1.250"))
                  node))
              nodes)))]
         ["wrong wrapper kind"
          (assoc artifact :kind :gravity/not-sh07-core-artifact)]
         ["missing wrapper artifact id"
          (dissoc artifact :artifact-id)]
         ["missing embedded capability proof"
          (assoc artifact :capability-based-proof nil)]]]
    (is (= :passed (:status upstream-verification)))
    (doseq [[label mutation] mutations]
      (let [checks
            (sh07-fast-verification-checks
             mutation artifact upstream-verification)
            failed-checks
            (vec (keep (fn [[check passed?]]
                         (when-not (true? passed?) check))
                       checks))
            public-verification
            (cond
              (= label "decimal scale substitution")
              (fresh-sh07-verification mutation)

              (= label "missing embedded capability proof")
              (sh07-verification mutation)

              :else nil)]
        (testing label
          (is (seq failed-checks))
          (when public-verification
            (is (= verification-keys
                   (set (keys public-verification))))
            (is (= :gravity/sh07-core-artifact-verification
                   (:artifact public-verification)))
            (is (= :failed (:status public-verification)))
            (is (seq (:failed-checks public-verification)))
            (when (= label "decimal scale substitution")
              (is (some #{:canonical-core-replays?}
                        (:failed-checks public-verification))))
            (when (= label "missing embedded capability proof")
              (is (some #{:stored-capability-proof-current?}
                        (:failed-checks public-verification))))))))))

(deftest sh07-form-node-and-digest-boundaries-are-explicitly-opt-in
  (if (= "1" (System/getenv "SH07_BOUNDARY_STRESS"))
    (let [source-path
          (fixture-path "accepted" "macro-def-fn-literals" ".gravity")
          base
          (sh07-request (sh06-file-artifact source-path))
          maximum-request (literal-boundary-request base 1024)
          over-request (literal-boundary-request base 1025)
          maximum-run (sh07-run-request maximum-request)
          over-diagnostic
          (assert-diagnostic
           (diagnostic-result #(sh07-run-request over-request))
           "C6-VERIFY" source-path)]
      (assert-request-run-schema maximum-run)
      (is (= 1024 (count (:forms maximum-request))))
      (is (= 1024
             (count (get-in maximum-run
                            [:canonical-core-artifact :nodes]))))
      (is (= 1026 (count (:digest-requests maximum-run)))
          "1024 core nodes plus artifact and provenance digests")
      (is (= :maximum-forms
             (get-in over-diagnostic
                     [:facts :rule-specific :bound])))
      (is (= 1024 (get-in over-diagnostic
                          [:facts :rule-specific :maximum])))
      (is (= 1025 (get-in over-diagnostic
                          [:facts :rule-specific :observed])))
      (is (= 1025
             (get-in over-diagnostic
                     [:facts :rule-specific
                      :projected-core-node-count])))
      (is (= 1027
             (get-in over-diagnostic
                     [:facts :rule-specific
                      :projected-digest-request-count]))))
    (is true
        "set SH07_BOUNDARY_STRESS=1 for 1024/1025 node and 1026/1027 digest gates")))

(deftest sh07-corpus-is-an-explicit-separate-gate
  (if (= "1" (System/getenv "SH07_COMPILER_CORPUS"))
    (let [compiler-root
          (io/file (path "bootstrap/gravity/src/gravity"))
          sources
          (->> (file-seq compiler-root)
               (filter #(.isFile %))
               (filter #(str/ends-with? (.getName %) ".gravity"))
               (sort-by #(.getPath %)))
          results
          (mapv
           (fn [source]
             (diagnostic-result #(sh07-file-artifact (.getPath source))))
           sources)]
      (is (seq sources))
      (is (every? #(nil? (:raw-host-error %)) results)
          "the opt-in corpus gate may expose structured SH07-A gaps, never host errors"))
    (is true
        "set SH07_COMPILER_CORPUS=1 to run the separate authoritative corpus gate")))
