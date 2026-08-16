(ns gravity.self-hosting.sh05-macro-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh05_macro_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-05 test source is not on the classpath"
                      {:id "SH05-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH05-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-05")

(def ^:private accepted-fixtures
  #{"defn-basic"
    "defn-multiple"
    "legacy-external-macro"
    "legacy-local-defmacro"})

(def ^:private rejected-fixtures
  #{"not-macro"
    "phase-mismatch"
    "return-non-syntax"
    "depth-limit"
    "size-limit"
    "build-effect-ungranted"
    "hygiene-hidden-binding"
    "capture-authority"
    "generated-unsafe-missing-audit"
    "profile-illegal-output"
    "trace-replay-substitution"
    "override-scalar"
    "override-vector"
    "override-nil"})

(def ^:private malformed-override-fixtures
  #{"override-scalar" "override-vector" "override-nil"})

(def ^:private c4-rules
  #{"C4-NOT-MACRO"
    "C4-RETURN"
    "C4-DEPTH"
    "C4-SIZE"
    "C4-BUILD-EFFECT"
    "C4-HYGIENE"
    "C4-CAPTURE"
    "C4-GENERATED-UNSAFE"
    "C4-PROFILE"
    "C4-TRACE"})

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(.endsWith ^String % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- c2-artifact
  [family basename extension]
  (bootstrap/compiler-c2-reader-file-artifact
   (fixture-path family basename extension)))

(defn- ns-metadata
  [artifact]
  (let [ns-form (first (:parsed-semantic-values artifact))
        clause (some #(when (and (seq? %) (= :metadata (first %))) %)
                     (drop 2 ns-form))]
    (second clause)))

(defn- rejection-oracle
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:sh05]))

(defn- rejection-request
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:compiler :sh05-request]))

(defn- contains-expected-key?
  [value]
  (cond
    (map? value)
    (or (some #(str/starts-with? (name %) "expected-") (keys value))
        (some contains-expected-key? (vals value)))

    (coll? value)
    (boolean (some contains-expected-key? value))

    :else false))

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-05 coordinator adapter is absent"
                      {:id "SH05-ADAPTER-ABSENT"
                       :symbol symbol}))))

(defn- sh05-file-artifact
  [source-path]
  ((required-var 'sh05-macro-file-artifact) source-path))

(defn- sha256-id?
  [value]
  (boolean (and (string? value)
                (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- expanded-forms
  [artifact]
  (or (:expanded-forms artifact)
      (mapv :form (:expanded-syntax-stream artifact))))

(defn- authoritative-compiler-paths
  []
  (let [directory (io/file (path "bootstrap/gravity/src"))]
    (->> (file-seq directory)
         (filter #(.isFile %))
         (map #(.getCanonicalPath %))
         (filter #(.endsWith ^String % ".gravity"))
         sort
         vec)))

(defn- parse-corpus-shard
  []
  (when-let [raw (not-empty (System/getenv "SH05_CORPUS_SHARD"))]
    (let [[_ index-text count-text]
          (re-matches #"([1-9][0-9]*)/([1-9][0-9]*)" raw)]
      (when-not index-text
        (throw (ex-info "SH05_CORPUS_SHARD must use the form i/n"
                        {:id "SH05-CORPUS-SHARD"
                         :value raw})))
      (let [index (Long/parseLong index-text)
            count (Long/parseLong count-text)]
        (when (> index count)
          (throw (ex-info "SH05_CORPUS_SHARD index exceeds shard count"
                          {:id "SH05-CORPUS-SHARD"
                           :value raw
                           :index index
                           :count count})))
        {:index index :count count :label raw}))))

(defn- corpus-shard-paths
  [source-paths index count]
  (->> source-paths
       (map-indexed vector)
       (keep (fn [[path-index source-path]]
               (when (= (mod path-index count) (dec index))
                 source-path)))
       vec))

(defn- corpus-partition-valid?
  [source-paths shard-count]
  (let [partitions
        (mapv #(corpus-shard-paths source-paths % shard-count)
              (range 1 (inc shard-count)))
        assigned (vec (mapcat identity partitions))]
    (and (= (count source-paths) (count assigned))
         (= (set source-paths) (set assigned))
         (= (count assigned) (count (set assigned))))))

(defn- normalize-expanded-forms
  [forms]
  (mapv #(if (and (seq? %) (= 'ns (first %))) ::namespace %) forms))

(defn- verification
  [artifact]
  ((required-var 'sh05-macro-artifact-verification) artifact))

(defn- capability-proof
  [artifact]
  ((required-var 'sh05-macro-capability-based-proof) artifact))

(defn- alter-gravity-expansions
  [artifact relative-path value]
  (-> artifact
      (assoc-in (into [:gravity-macro-boundary :expansion-runs 0
                       :raw-template-result :expansion-template]
                      relative-path)
                value)
      (assoc-in (into [:gravity-macro-boundary :expansion-runs 0
                       :resolved-expansion]
                      relative-path)
                value)
      (assoc-in (into [:gravity-macro-boundary :raw-template-result
                       :expansion-template]
                      relative-path)
                value)
      (assoc-in (into [:gravity-macro-boundary :resolved-expansion]
                      relative-path)
                value)))

(defn- rejection-data
  [source-path]
  (try
    (sh05-file-artifact source-path)
    {:unexpected-success true}
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:raw-host-error (.getName (class error))
       :message (.getMessage error)})))

(deftest sh05-fixture-inventory-is-co-canonical-and-behavioral
  (testing "the checked-in inventory is explicit and symmetric"
    (is (= accepted-fixtures (fixture-basenames "accepted" ".gravity")))
    (is (= accepted-fixtures (fixture-basenames "accepted" ".qst")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".gravity")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".qst"))))
  (testing "every source pair is byte-identical and traverses the Gravity reader"
    (doseq [[family basenames]
            [["accepted" accepted-fixtures]
             ["rejected" rejected-fixtures]]
            basename (sort basenames)]
      (let [gravity-path (fixture-path family basename ".gravity")
            qst-path (fixture-path family basename ".qst")
            gravity-c2 (c2-artifact family basename ".gravity")
            qst-c2 (c2-artifact family basename ".qst")]
        (is (java.util.Arrays/equals (source-bytes gravity-path)
                                    (source-bytes qst-path))
            (str family "/" basename))
        (is (= :gravity/stage0-c2-reader-document-artifact (:kind gravity-c2)))
        (is (= :gravity/stage0-c2-reader-document-artifact (:kind qst-c2)))
        (is (= (:parsed-semantic-values gravity-c2)
               (:parsed-semantic-values qst-c2))))))
  (testing "oracle data is separate from the executable incompatible request"
    (doseq [basename (sort rejected-fixtures)]
      (let [gravity-request (rejection-request basename ".gravity")
            qst-request (rejection-request basename ".qst")
            gravity-oracle (rejection-oracle basename ".gravity")
            qst-oracle (rejection-oracle basename ".qst")]
        (if (contains? malformed-override-fixtures basename)
          (is (not (map? gravity-request)) basename)
          (is (map? gravity-request) basename))
        (is (= gravity-request qst-request) basename)
        (is (not (contains-expected-key? gravity-request)) basename)
        (is (= gravity-oracle qst-oracle) basename))))
  (testing "all C4 families plus the explicit phase mapping are represented"
    (let [oracles (map #(rejection-oracle % ".gravity") rejected-fixtures)]
      (is (= c4-rules (set (map :expected-rule oracles))))
      (is (= :phase-mismatch
             (:scenario (rejection-oracle "phase-mismatch" ".gravity"))))
      (is (= ["L12-PHASE-CAPTURE" "SAFE12-PHASE"]
             (get-in (rejection-oracle "phase-mismatch" ".gravity")
                     [:expected-facts :catalog-mapping])))
      (is (= {:reason :missing-build-authority
              :missing-declaration :build/read-file
              :missing-capability :fs/read
              :missing-grant :build/read-file
              :catalog-mapping ["L15" "SAFE10"]}
             (:expected-facts
              (rejection-oracle "build-effect-ungranted" ".gravity")))))))

(deftest sh05-coordinator-adapter-api-is-explicit
  (let [source-var (required-var 'sh05-macro-source-artifact)
        file-var (required-var 'sh05-macro-file-artifact)]
    (is (= '([source-path source-text]) (:arglists (meta source-var))))
    (is (= '([source-path]) (:arglists (meta file-var))))))

(deftest sh05-basic-defn-expansion-is-stable-co-canonical-and-origin-linked
  (let [gravity-path (fixture-path "accepted" "defn-basic" ".gravity")
        qst-path (fixture-path "accepted" "defn-basic" ".qst")
        gravity-first (sh05-file-artifact gravity-path)
        gravity-second (sh05-file-artifact gravity-path)
        qst-artifact (sh05-file-artifact qst-path)
        gravity-stage0 (bootstrap/macro-file-artifact gravity-path)
        qst-stage0 (bootstrap/macro-file-artifact qst-path)
        trace (:macro-expansion-trace gravity-first)
        expansion '(def add-one (fn [x] (+ x 1)))]
    (is (= gravity-first gravity-second))
    (is (= :gravity/sh05-macro-expansion-artifact (:kind gravity-first)))
    (is (= :accepted (:status gravity-first)))
    (is (sha256-id? (:artifact-id gravity-first)))
    (is (sha256-id? (:expanded-syntax-stream-id gravity-first)))
    (is (= (:artifact-id gravity-first) (:artifact-id qst-artifact)))
    (is (= (:expanded-syntax-stream-id gravity-first)
           (:expanded-syntax-stream-id qst-artifact)))
    (is (some #{expansion} (expanded-forms gravity-first)))
    (is (some #{expansion} (expanded-forms qst-artifact)))
    (doseq [[label artifact stage0]
            [[:gravity gravity-first gravity-stage0]
             [:qst qst-artifact qst-stage0]]]
      (let [stream (:expanded-syntax-stream artifact)
            first-form (:form (first stream))]
        (is (seq stream) (name label))
        (is (and (seq? first-form) (= 'ns (first first-form)))
            (name label))
        (is (= (:expanded-forms stage0) (:expanded-forms artifact))
            (name label))
        (is (= (count stream) (inc (count (:expanded-forms artifact))))
            (name label))))
    (is (= 1 (count trace)))
    (is (= 'defn (:macro (first trace))))
    (is (sha256-id? (:macro-version (first trace))))
    (is (sha256-id? (:input-syntax-id (first trace))))
    (is (sha256-id? (:output-syntax-id (first trace))))
    (is (map? (:definition-span (first trace))))
    (is (map? (:call-site-span (first trace))))
    (is (seq (:generated-origin (first trace))))
    (is (= :meta (:profile (first trace))))
    (is (= :jvm (:target (first trace))))
    (is (= [] (:build-effects (first trace))))
    (is (= [] (:capabilities (first trace))))
    (is (= :gravity/sh04-syntax-object-artifact
           (get-in gravity-first [:sh04-syntax-artifact :artifact])))
    (is (= :complete (get-in gravity-first [:capability-based-proof :status])))
    (is (= gravity-path (get-in gravity-first [:provenance :source-path])))
    (is (= qst-path (get-in qst-artifact [:provenance :source-path])))
    (is (not= (get-in gravity-first [:provenance :source-path])
              (get-in qst-artifact [:provenance :source-path])))))

(deftest sh05-expansion-preserves-an-ordinary-digest-reference-shaped-map
  (let [source-text
        (str "(ns sh05.digest-reference-map"
             " (:profile :meta) (:target :jvm))\n"
             "(defn preserve-digest-reference-map [] {:digest-ref 0})\n")
        source-body
        '(defn preserve-digest-reference-map [] {:digest-ref 0})
        expanded-body
        '(def preserve-digest-reference-map
           (fn [] {:digest-ref 0}))
        gravity-path "/sh05/exact-map/source.gravity"
        qst-path "/sh05/exact-map/source.qst"
        gravity-first
        ((required-var 'sh05-macro-source-artifact)
         gravity-path source-text)
        gravity-second
        ((required-var 'sh05-macro-source-artifact)
         gravity-path source-text)
        qst-artifact
        ((required-var 'sh05-macro-source-artifact) qst-path source-text)]
    (doseq [[extension artifact]
            [[".gravity" gravity-first] [".qst" qst-artifact]]]
      (is (= :gravity/sh05-macro-expansion-artifact (:kind artifact))
          extension)
      (is (= :accepted (:status artifact)) extension)
      (is (= source-body
             (get-in artifact
                     [:gravity-macro-boundary
                      :authenticated-sh04-artifact
                      :syntax-object-stream 1 :form :value]))
          extension)
      (is (= [expanded-body] (:expanded-forms artifact)) extension)
      (is (= expanded-body
             (get-in artifact [:expanded-syntax-stream 1 :form]))
          extension)
      (is (= {:digest-ref 0}
             (last (last (get-in artifact
                                 [:expanded-syntax-stream 1 :form]))))
          extension)
      (is (= :passed (:status (verification artifact))) extension)
      (is (= :complete (:status (capability-proof artifact))) extension)
      (is (sha256-id? (:artifact-id artifact)) extension)
      (is (sha256-id? (:expanded-syntax-stream-id artifact)) extension)
      (is (sha256-id? (:macro-expansion-trace-id artifact)) extension))
    (is (= gravity-first gravity-second))
    (is (= (:artifact-id gravity-first) (:artifact-id qst-artifact)))
    (is (= (:expanded-syntax-stream-id gravity-first)
           (:expanded-syntax-stream-id qst-artifact)))
    (is (= (:macro-expansion-trace-id gravity-first)
           (:macro-expansion-trace-id qst-artifact)))
    (is (= gravity-path
           (get-in gravity-first [:provenance :source-path])))
    (is (= qst-path
           (get-in qst-artifact [:provenance :source-path])))))

(deftest sh05-rejections-are-structured-pathful-and-rule-exact
  (doseq [basename (sort rejected-fixtures)
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "rejected" basename extension)
          oracle (rejection-oracle basename extension)
          data (rejection-data source-path)]
      (testing (str basename extension)
        (is (nil? (:raw-host-error data)))
        (is (nil? (:unexpected-success data)))
        (is (= (:expected-rule oracle) (:id data)))
        (is (= (:expected-stage oracle) (:stage data)))
        (is (= (:expected-severity oracle) (:severity data)))
        (is (= :c4-macro-expansion (:diagnostic-family data)))
        (is (= source-path (get-in data [:source-span :source])))
        (is (map? (:facts data)))
        (is (seq (:remediation data)))
        (when-let [expected-facts (:expected-facts oracle)]
          (is (= expected-facts
                 (select-keys (:facts data) (keys expected-facts)))))))))

(deftest sh05-corpus-shards-cover-the-sorted-inventory-exactly-once
  (let [source-paths (authoritative-compiler-paths)]
    (is (= source-paths (vec (sort source-paths))))
    (doseq [shard-count [1 2 3 4 5 36 37]]
      (is (corpus-partition-valid? source-paths shard-count)
          (str shard-count " shards")))))

(deftest sh05-v2-stream-and-trace-identities-are-bounded-and-order-sensitive
  (let [ids
        (mapv #(bootstrap/p15-s23-c6c10-canonical-digest
                "<sh05-v2-identity-test>"
                {:domain :sh05-synthetic-identity :ordinal %})
              (range 256))
        expanded-stream
        (mapv (fn [syntax-id] {:expanded-syntax-id syntax-id}) ids)
        trace
        (mapv (fn [ordinal syntax-id]
                {:input-syntax-id syntax-id
                 :output-syntax-id (nth ids (mod (inc ordinal) 256))
                 :macro 'defn
                 :macro-version (first ids)
                 :trace-replay-id syntax-id})
              (range 256) ids)
        mutations
        (fn [values substitute]
          {:reordered (vec (reverse values))
           :deleted (pop values)
           :duplicate (assoc values 128 (first values))
           :first-id (substitute values 0 :first)
           :middle-id (substitute values 128 :middle)
           :last-id (substitute values 255 :last)})
        substituted-id
        (fn [family position]
          (bootstrap/p15-s23-c6c10-canonical-digest
           "<sh05-v2-identity-test>"
           {:domain :sh05-synthetic-substitution
            :family family :position position}))
        cases
        [{:label :expanded-stream
          :values expanded-stream
          :identity-input bootstrap/sh05-expanded-syntax-stream-identity-input
          :identity-id #(bootstrap/sh05-expanded-syntax-stream-id
                         "<sh05-v2-expanded-stream>" %)
          :domain :gravity/sh05-expanded-syntax-stream-v2
          :mutations
          (mutations
           expanded-stream
           (fn [values index position]
             (assoc-in values [index :expanded-syntax-id]
                       (substituted-id :expanded-stream position))))}
         {:label :macro-trace
          :values trace
          :identity-input bootstrap/sh05-macro-trace-identity-input
          :identity-id #(bootstrap/sh05-macro-trace-id
                         "<sh05-v2-macro-trace>" %)
          :domain :gravity/sh05-macro-expansion-trace-v2
          :mutations
          (mutations
           trace
           (fn [values index position]
             (assoc-in values [index :output-syntax-id]
                       (substituted-id :macro-trace position))))}]]
    (doseq [{:keys [label values identity-input identity-id domain mutations]}
            cases]
      (let [input (identity-input values)
            chunks (:item-chunks input)
            record
            (bootstrap/p15-s23-c6c10-canonical-record
             "<sh05-v2-identity-test>" input)
            stable-id (identity-id values)]
        (is (= domain (:domain input)) (name label))
        (is (= 256 (:item-count input)) (name label))
        (is (= 16 (count chunks)) (name label))
        (is (= (vec (range 16)) (mapv :chunk-index chunks))
            (name label))
        (is (= (mapv #(* 16 %) (range 16))
               (mapv :start-ordinal chunks))
            (name label))
        (is (every? #(= 16 (:item-count %)) chunks) (name label))
        (is (= (vec (range 256))
               (mapv :ordinal (mapcat :items chunks)))
            (name label))
        (is (every? #(< % 65536)
                    (vals (select-keys (:stats record)
                                       [:nodes :maximum-depth
                                        :maximum-width])))
            (name label))
        (is (sha256-id? stable-id) (name label))
        (is (= stable-id (identity-id values)) (name label))
        (doseq [[mutation changed] mutations]
          (is (not= stable-id (identity-id changed))
              (str (name label) " " (name mutation))))))))

(deftest sh05-expands-the-complete-authoritative-compiler-defn-subset
  (if (= "1" (System/getenv "SH05_SKIP_CORPUS"))
    (do
      (println (pr-str {:sh05-corpus :skipped}))
      (is true "SH05 corpus test explicitly skipped"))
    (let [all-source-paths (authoritative-compiler-paths)
          shard (parse-corpus-shard)
          source-paths
          (if shard
            (corpus-shard-paths all-source-paths
                                (:index shard) (:count shard))
            all-source-paths)
        results
        (mapv
         (fn [source-path]
           (let [artifact (sh05-file-artifact source-path)
                 stage0 (bootstrap/macro-file-artifact source-path)
                 authenticated-input-forms
                 (get-in artifact
                         [:gravity-macro-boundary
                          :authenticated-sh04-artifact
                          :c2-reader-artifact
                          :parsed-semantic-values])]
             {:path source-path
              :artifact artifact
              :input-defn-count
              (count (filter #(and (seq? %) (= 'defn (first %)))
                             authenticated-input-forms))
              :stage0-forms (:expanded-forms stage0)}))
           source-paths)
          input-defn-count (reduce + (map :input-defn-count results))]
      ;; The current authoritative compiler inventory measures 36 modules and
      ;; 1,276 top-level defn forms. The default run remains exhaustive and
      ;; compiles that measured inventory, including its own SH-05 sources.
      (when-not shard
        (is (= 36 (count source-paths)))
        (is (= 1276 input-defn-count))
        (is (= input-defn-count
               (reduce +
                       (map #(get-in % [:artifact :expanded-defn-count])
                            results)))))
      (is (every?
           (fn [{:keys [artifact]}]
             (and (= :gravity/sh05-macro-expansion-artifact (:kind artifact))
                  (= :accepted (:status artifact))))
           results))
      (is (every?
           (fn [{:keys [artifact stage0-forms]}]
             (= (normalize-expanded-forms stage0-forms)
                (normalize-expanded-forms (expanded-forms artifact))))
           results))
      (is (every?
           (fn [{:keys [artifact input-defn-count]}]
             (= input-defn-count (:expanded-defn-count artifact)))
           results))
      (is (every?
           (fn [{:keys [artifact]}]
             (= :complete
                (get-in artifact [:capability-based-proof :status])))
           results))
      (when shard
        (println
         (pr-str {:sh05-corpus-shard (:label shard)
                  :module-count (count source-paths)
                  :input-defn-count input-defn-count}))))))

(deftest sh05-identities-are-cwd-and-checkout-path-neutral
  (let [original-user-dir (System/getProperty "user.dir")
        root-a (java.nio.file.Files/createTempDirectory
                "gravity-sh05-checkout-a-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        root-b (java.nio.file.Files/createTempDirectory
                "gravity-sh05-checkout-b-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        cwd (java.nio.file.Files/createTempDirectory
             "gravity-sh05-unrelated-cwd-"
             (make-array java.nio.file.attribute.FileAttribute 0))
        source-bytes (source-bytes
                      (fixture-path "accepted" "defn-basic" ".gravity"))
        path-a (str (.resolve root-a "src/defn-basic.gravity"))
        path-b (str (.resolve root-b "src/defn-basic.gravity"))]
    (try
      (io/make-parents path-a)
      (io/make-parents path-b)
      (with-open [out (io/output-stream path-a)] (.write out source-bytes))
      (with-open [out (io/output-stream path-b)] (.write out source-bytes))
      (System/setProperty "user.dir" (str cwd))
      (let [artifact-a (sh05-file-artifact path-a)
            artifact-b (sh05-file-artifact path-b)]
        (is (= (:artifact-id artifact-a) (:artifact-id artifact-b)))
        (is (= (:expanded-syntax-stream-id artifact-a)
               (:expanded-syntax-stream-id artifact-b)))
        (is (= (:macro-expansion-trace-id artifact-a)
               (:macro-expansion-trace-id artifact-b)))
        (is (= path-a (get-in artifact-a [:provenance :source-path])))
        (is (= path-b (get-in artifact-b [:provenance :source-path])))
        (is (not= (get-in artifact-a [:provenance :source-path])
                  (get-in artifact-b [:provenance :source-path]))))
      (finally
        (System/setProperty "user.dir" original-user-dir)
        (doseq [tree [root-a root-b cwd]
                file (reverse (file-seq (.toFile tree)))]
          (io/delete-file file true))))))

(deftest sh05-lineage-revalidates-sh03-sh04-and-the-sh02-envelope
  (let [artifact
        (sh05-file-artifact
         (fixture-path "accepted" "defn-basic" ".gravity"))
        report (verification artifact)
        proof (capability-proof artifact)]
    (is (= :passed (:status report)))
    (is (= :passed
           (get-in report [:gravity-verifiers :template :status])))
    (is (= :passed
           (get-in report [:gravity-verifiers :resolved :status])))
    (is (true? (get-in report [:checks :fresh-sh02-envelope-verified?])))
    (is (true? (get-in report [:checks :sh03-reader-lineage-current?])))
    (is (true? (get-in report [:checks :sh04-syntax-lineage-current?])))
    (is (true? (get-in report [:checks :macro-output-current?])))
    (is (true? (get-in report [:checks :trace-replay-current?])))
    (is (= :complete (:status proof)))
    (is (true? (:fresh-sh02-envelope-verified? proof)))
    (is (true? (:sh03-reader-lineage-current? proof)))
    (is (true? (:sh04-syntax-lineage-current? proof)))))

(deftest sh05-output-policy-replay-lineage-and-graph-alterations-fail-closed
  (let [artifact
        (sh05-file-artifact
         (fixture-path "accepted" "defn-basic" ".gravity"))
        changes
        [{:check :macro-output-current
          :gravity-failure? true
          :value (alter-gravity-expansions
                  artifact [:expanded-syntax-recipe]
                  {:kind :substituted-output})}
         {:check :build-grants-current
          :gravity-failure? true
          :value (alter-gravity-expansions
                  artifact [:build-effect-log :requested]
                  [:build/read-file])}
         {:check :macro-version-current
          :gravity-failure? true
          :value (alter-gravity-expansions
                  artifact [:macro-version]
                  "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")}
         {:check :trace-replay-current
          :gravity-failure? true
          :value (alter-gravity-expansions
                  artifact [:macro-expansion-trace 0 :trace-replay-id]
                  "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")}
         {:check :input-lineage-current
          :gravity-failure? false
          :value (assoc-in artifact [:sh04-syntax-artifact :artifact-id]
                           "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")}
         {:check :expanded-graph-valid
          :gravity-failure? false
          :value (assoc-in artifact [:expanded-syntax-graph :edges]
                           [{:from "missing" :to "also-missing"}])}
         {:check :exact-artifact-shape
          :gravity-failure? true
          :value (alter-gravity-expansions
                  artifact [:unexpected-field] :rejected)}]]
    (doseq [{:keys [check gravity-failure? value]} changes]
      (let [report (verification value)
            proof (capability-proof value)
            gravity-statuses
            (set [(get-in report [:gravity-verifiers :template :status])
                  (get-in report [:gravity-verifiers :resolved :status])])]
        (is (= :failed (:status report)) (name check))
        (is (some #{check} (:failed-checks report)) (name check))
        (if gravity-failure?
          (is (contains? gravity-statuses :failed) (name check))
          (is (not (contains? gravity-statuses :failed)) (name check)))
        (is (= :failed (:status proof)) (name check))))))

(deftest sh05-compatibility-c4-routing-retains-the-gravity-result
  (let [source-path
        (path "bootstrap/gravity/src/gravity/compiler/c4_macro_expansion_engine.gravity")
        sh05 (sh05-file-artifact source-path)
        c4 (bootstrap/compiler-c4-macro-file-artifact source-path)]
    (is (= :gravity/stage0-c4-macro-expansion-artifact (:kind c4)))
    (is (= (:artifact-id sh05)
           (get-in c4 [:sh05-macro-expansion-artifact :artifact-id])))
    (is (= :gravity (get-in c4 [:execution-boundary :macro-authority])))
    (is (= :compatibility-only
           (get-in c4 [:execution-boundary :c4-stage0-adapter])))))

(defn- run-identities
  [artifact]
  (mapv
   (fn [run]
     (select-keys (:resolved-expansion run)
                  [:artifact-id :input-syntax-id :output-syntax-id
                   :macro-version]))
   (get-in artifact [:gravity-macro-boundary :expansion-runs])))

(defn- copy-source!
  [source-path destination]
  (io/make-parents destination)
  (with-open [input (io/input-stream source-path)
              output (io/output-stream destination)]
    (io/copy input output)))

(deftest sh05-multiple-defn-runs-are-ordered-independent-and-path-neutral
  (let [gravity-path (fixture-path "accepted" "defn-multiple" ".gravity")
        qst-path (fixture-path "accepted" "defn-multiple" ".qst")
        gravity-artifact (sh05-file-artifact gravity-path)
        qst-artifact (sh05-file-artifact qst-path)
        gravity-c4 (bootstrap/sh05-c4-compatibility-artifact
                    gravity-path gravity-artifact)
        qst-c4 (bootstrap/sh05-c4-compatibility-artifact
                qst-path qst-artifact)
        runs (get-in gravity-artifact
                     [:gravity-macro-boundary :expansion-runs])]
    (is (= 2 (:expanded-defn-count gravity-artifact)))
    (is (= 2 (count runs)))
    (is (= (run-identities gravity-artifact)
           (run-identities qst-artifact)))
    (is (= 2 (count (set (map #(get-in % [:resolved-expansion :artifact-id])
                              runs)))))
    (is (= 2 (count (set (map #(get-in % [:resolved-expansion :output-syntax-id])
                              runs)))))
    (is (= (:artifact-id gravity-artifact) (:artifact-id qst-artifact)))
    (is (= (:artifact-id gravity-c4) (:artifact-id qst-c4)))
    (is (= gravity-path (get-in gravity-artifact [:provenance :source-path])))
    (is (= qst-path (get-in qst-artifact [:provenance :source-path])))
    (let [root-a (java.nio.file.Files/createTempDirectory
                  "gravity-sh05-multi-a-"
                  (make-array java.nio.file.attribute.FileAttribute 0))
          root-b (java.nio.file.Files/createTempDirectory
                  "gravity-sh05-multi-b-"
                  (make-array java.nio.file.attribute.FileAttribute 0))
          path-a (str (.resolve root-a "src/multiple.gravity"))
          path-b (str (.resolve root-b "src/multiple.qst"))]
      (try
        (copy-source! gravity-path path-a)
        (copy-source! qst-path path-b)
        (let [artifact-a (sh05-file-artifact path-a)
              artifact-b (sh05-file-artifact path-b)
              c4-a (bootstrap/sh05-c4-compatibility-artifact path-a artifact-a)
              c4-b (bootstrap/sh05-c4-compatibility-artifact path-b artifact-b)]
          (is (= (run-identities artifact-a) (run-identities artifact-b)))
          (is (= (:artifact-id artifact-a) (:artifact-id artifact-b)))
          (is (= (:artifact-id c4-a) (:artifact-id c4-b)))
          (is (= path-a (get-in artifact-a [:provenance :source-path])))
          (is (= path-b (get-in artifact-b [:provenance :source-path])))
          (is (not= (get-in artifact-a [:provenance :source-path])
                    (get-in artifact-b [:provenance :source-path]))))
        (finally
          (doseq [tree [root-a root-b]
                  file (reverse (file-seq (.toFile tree)))]
            (io/delete-file file true)))))))

(defn- verification-failed?
  [artifact]
  (and (= :failed (:status (verification artifact)))
       (= :failed (:status (capability-proof artifact)))))

(defn- alter-second-run-both
  [artifact relative-path value]
  (-> artifact
      (assoc-in (into [:gravity-macro-boundary :expansion-runs 1
                       :raw-template-result :expansion-template]
                      relative-path)
                value)
      (assoc-in (into [:gravity-macro-boundary :expansion-runs 1
                       :resolved-expansion]
                      relative-path)
                value)))

(defn- alter-second-run-provenance
  [artifact field value]
  (reduce (fn [changed provenance-path]
            (assoc-in changed (conj provenance-path field) value))
          artifact
          [[:gravity-macro-boundary :expansion-runs 1
            :raw-template-result :expansion-template :provenance]
           [:gravity-macro-boundary :expansion-runs 1
            :resolved-expansion :provenance]
           [:gravity-macro-boundary :expansion-runs 1
            :digest-requests 1 :preimage :provenance]]))

(deftest sh05-second-expansion-run-reorder-delete-and-substitutions-fail-closed
  (let [artifact
        (sh05-file-artifact
         (fixture-path "accepted" "defn-multiple" ".gravity"))
        runs (get-in artifact [:gravity-macro-boundary :expansion-runs])
        second-run (second runs)
        changed-digest
        "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        cases
        {:reordered
         (assoc-in artifact [:gravity-macro-boundary :expansion-runs]
                   (vec (reverse runs)))
         :deleted
         (assoc-in artifact [:gravity-macro-boundary :expansion-runs]
                   [(first runs)])
         :template
         (assoc-in artifact
                   [:gravity-macro-boundary :expansion-runs 1
                    :raw-template-result :expansion-template
                    :unexpected-field]
                   :substituted)
         :raw-wrapper-schema
         (assoc-in artifact
                   [:gravity-macro-boundary :expansion-runs 1
                    :raw-template-result :unexpected-wrapper-field]
                   :substituted)
         :resolved
         (assoc-in artifact
                   [:gravity-macro-boundary :expansion-runs 1
                    :resolved-expansion :unexpected-field]
                   :substituted)
         :digests
         (assoc-in artifact
                   [:gravity-macro-boundary :expansion-runs 1
                    :resolved-digests 0]
                   changed-digest)
         :version
         (alter-second-run-both
          artifact [:macro-version] changed-digest)
         :replay
         (alter-second-run-both
          artifact [:macro-expansion-trace 0 :trace-replay-id]
          changed-digest)
         :provenance-source-path
         (alter-second-run-provenance
          artifact :actual-source-path "/substituted/root/source.gravity")
         :provenance-call-site-span
         (alter-second-run-provenance
          artifact :call-site-span
          {:source "/substituted/root/source.gravity"
           :byte-start 0 :byte-end 1})
         :provenance-origin-chain
         (alter-second-run-provenance
          artifact :input-origin-chain [:substituted-origin])
         :provenance-definition-span
         (alter-second-run-provenance
          artifact :definition-span
          {:source "bootstrap/gravity/src/gravity/macro.gravity"
           :byte-start 0 :byte-end 1})
         :provenance-reader-binding
         (alter-second-run-provenance
          artifact :reader-binding {:semantic-binding-id changed-digest})
         :provenance-reader-revision
         (alter-second-run-provenance
          artifact :reader-source-revision {:revision-id changed-digest})
         :stored-verifier-schema
         (assoc-in artifact
                   [:gravity-macro-boundary :expansion-runs 1
                    :template-verification :unexpected-verifier-field]
                   :substituted)}]
    (is (= 2 (count runs)))
    (is (= :passed (:status (:template-verification second-run))))
    (is (= :passed (:status (:resolved-verification second-run))))
    (doseq [[label changed] cases]
      (let [report (verification changed)
            proof (capability-proof changed)]
        (is (= :failed (:status report)) (name label))
        (is (= :failed (:status proof)) (name label))
        (when (contains? #{:raw-wrapper-schema :stored-verifier-schema}
                         label)
          (is (false? (get-in report
                              [:checks :macro-run-storage-exact?]))
              (name label))
          (is (some #{:macro-run-storage-exact}
                    (:failed-checks report))
              (name label)))
        (when (contains? #{:provenance-source-path
                           :provenance-call-site-span
                           :provenance-origin-chain
                           :provenance-definition-span
                           :provenance-reader-binding
                           :provenance-reader-revision}
                         label)
          (is (false? (get-in report
                              [:checks :macro-run-provenance-current?]))
              (name label))
          (is (some #{:macro-run-provenance-current}
                    (:failed-checks report))
              (name label)))))))

(deftest sh05-outer-semantic-authority-environment-graph-origin-and-plan-mutations-fail
  (let [artifact
        (sh05-file-artifact
         (fixture-path "accepted" "defn-multiple" ".gravity"))
        graph (:expanded-syntax-graph artifact)
        alternate-id
        "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
        alternate-graph
        (-> graph
            (update :nodes conj {:id alternate-id
                                 :kind :expanded-syntax
                                 :form :alternate-valid-node})
            (update :node-ids conj alternate-id))
        changed-hash
        "sha256:eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
        cases
        {:macro-authority
         (assoc-in artifact [:execution-boundary :macro-authority] :clojure)
         :macro-environment
         (assoc-in artifact [:macro-environment :macros 0 :version]
                   changed-hash)
         :valid-alternate-graph
         (assoc artifact :expanded-syntax-graph alternate-graph)
         :generated-origin
         (assoc-in artifact [:generated-origin-source-map 1
                             :generated-origin 0 :reason]
                   :substituted-origin)
         :status
         (assoc artifact :status :rejected)
         :pass
         (assoc-in artifact [:pass :name] :substituted-pass)
         :proof-nil
         (assoc artifact :capability-based-proof nil)
         :proof-altered
         (assoc-in artifact [:capability-based-proof :status] :failed)
         :plan-public-function-identity
         (assoc-in artifact
                   [:gravity-macro-boundary :plan-binding
                    :public-function-hashes 'sh05-expand-macro-template]
                   changed-hash)}]
    (is (true? (get-in (verification
                        (assoc artifact :expanded-syntax-graph alternate-graph))
                       [:checks :expanded-graph-valid?])))
    (doseq [[label changed] cases]
      (is (verification-failed? changed) (name label)))))

(defn- substitute-generated-origin-source
  [origin substituted-source]
  (if (= :generated (:kind origin))
    (assoc-in origin [:call-site-span :source] substituted-source)
    origin))

(defn- substitute-outer-trace-physical-source
  [artifact substituted-source]
  (-> artifact
      (update :macro-expansion-trace
              (fn [trace]
                (mapv (fn [step]
                        (-> step
                            (assoc-in [:call-site-span :source]
                                      substituted-source)
                            (update :generated-origin
                                    #(mapv (fn [origin]
                                             (substitute-generated-origin-source
                                              origin substituted-source))
                                           %))))
                      trace)))
      (update :generated-origin-source-map
              (fn [source-map]
                (mapv (fn [entry]
                        (update entry :generated-origin
                                #(mapv (fn [origin]
                                         (substitute-generated-origin-source
                                          origin substituted-source))
                                       %)))
                      source-map)))
      (update :expanded-syntax-stream
              (fn [stream]
                (mapv (fn [syntax]
                        (update syntax :origin
                                #(mapv (fn [origin]
                                         (substitute-generated-origin-source
                                          origin substituted-source))
                                       %)))
                      stream)))))

(deftest sh05-expanded-syntax-and-coherent-outer-trace-substitutions-reject
  (let [artifact
        (sh05-file-artifact
         (fixture-path "accepted" "defn-multiple" ".gravity"))
        cases
        {:expanded-metadata
         {:changed (assoc-in artifact [:expanded-syntax-stream 0 :metadata]
                            {:substituted true})
          :check :macro-output-current}
         :expanded-hygiene
         {:changed (assoc-in artifact [:expanded-syntax-stream 0 :hygiene]
                            {:marks [:substituted]
                             :lexical-scopes []})
          :check :macro-output-current}
         :expanded-span
         {:changed (update-in artifact
                              [:expanded-syntax-stream 0 :span :primary
                               :byte-end]
                              inc)
          :check :macro-output-current}
         :expanded-origin
         {:changed (update-in artifact [:expanded-syntax-stream 0 :origin]
                              conj {:kind :substituted-origin})
          :check :macro-output-current}
         :coherent-outer-trace-physical-source
         {:changed
          (substitute-outer-trace-physical-source
           artifact "/substituted/root/source.gravity")
          :check :trace-replay-current}}]
    (doseq [[label {:keys [changed check]}] cases]
      (let [report (verification changed)]
        (is (= :failed (:status report)) (name label))
        (is (false? (get-in report [:checks (keyword (str (name check) "?"))]))
            (name label))
        (is (some #{check} (:failed-checks report)) (name label))))))

(deftest sh05-legacy-external-and-local-macros-route-without-sh05-credit
  (doseq [extension [".gravity" ".qst"]]
    (let [external-path
          (fixture-path "accepted" "legacy-external-macro" extension)
          local-path
          (fixture-path "accepted" "legacy-local-defmacro" extension)
          capture
          (fn [source-path]
            (try
              {:unexpected-success
               (bootstrap/compiler-c4-macro-file-artifact source-path)}
              (catch clojure.lang.ExceptionInfo error
                (ex-data error))
              (catch Throwable error
                {:raw-host-error (.getName (class error))})))
          external (capture external-path)
          local (capture local-path)
          external-forms
          (:parsed-semantic-values
           (bootstrap/compiler-c2-reader-file-artifact external-path))
          local-forms
          (:parsed-semantic-values
           (bootstrap/compiler-c2-reader-file-artifact local-path))
          external-module (bootstrap/parse-module external-path external-forms)
          local-module (bootstrap/parse-module local-path local-forms)
          structured-legacy-outcome?
          (fn [outcome]
            (or (= :gravity/stage0-c4-macro-expansion-artifact
                   (get-in outcome [:unexpected-success :kind]))
                (and (string? (:id outcome))
                     (str/starts-with? (:id outcome) "C4-"))))]
      (is (false? (bootstrap/sh05-bounded-authoritative-source?
                   external-path external-module external-forms)))
      (is (false? (bootstrap/sh05-bounded-authoritative-source?
                   local-path local-module local-forms)))
      (is (some bootstrap/sh05-form-contains-legacy-macro-position?
                local-forms))
      (is (nil? (:raw-host-error external)))
      (is (nil? (:raw-host-error local)))
      (is (structured-legacy-outcome? external))
      (is (structured-legacy-outcome? local))
      (is (nil? (get-in external
                        [:unexpected-success
                         :sh05-macro-expansion-artifact])))
      (is (nil? (get-in local
                        [:unexpected-success
                         :sh05-macro-expansion-artifact])))
      (is (nil? (get-in external [:facts :sh05-boundary])))
      (is (nil? (get-in local [:facts :sh05-boundary])))
      (when (:id external)
        (is (= external-path (get-in external [:source-span :source]))))
      (when (:id local)
        (is (= local-path (get-in local [:source-span :source])))))))

(deftest sh05-malformed-request-overrides-reject-without-host-errors
  (doseq [basename (sort malformed-override-fixtures)
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "rejected" basename extension)
          data (rejection-data source-path)]
      (is (nil? (:raw-host-error data)) (str basename extension))
      (is (= "C4-RETURN" (:id data)) (str basename extension))
      (is (= :macro-expansion (:stage data)) (str basename extension))
      (is (= :error (:severity data)) (str basename extension))
      (is (= source-path (get-in data [:source-span :source]))
          (str basename extension))
      (is (map? (:facts data)) (str basename extension)))))
