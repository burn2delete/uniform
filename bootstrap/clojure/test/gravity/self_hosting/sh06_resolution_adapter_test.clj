(ns gravity.self-hosting.sh06-resolution-adapter-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.c6-core-lowering :as hosted-c6]
            [gravity.c6-core-lowering.context :as c6-context]
            [gravity.c6-core-lowering.lowering :as c6-lowering]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh06_resolution_adapter_test.clj")]
    (when-not resource
      (throw (ex-info "SH-06 test source is not on the classpath"
                      {:id "SH06-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH06-REPOSITORY-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- private-bootstrap-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required private SH-06 test seam is absent"
        {:id "SH06-PRIVATE-TEST-SEAM"
         :symbol symbol}))))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-06")

(def ^:private accepted-fixtures
  #{"resolution-order" "module-boundaries" "foreign-explicit"
    "compiler-subset"})

(def ^:private rejected-fixtures
  #{"unresolved" "ambiguous" "private" "alias" "shadow" "cycle"
    "cross-profile" "capability" "target" "foreign"})

(def ^:private c5-rules
  #{"C5-UNRESOLVED" "C5-AMBIGUOUS" "C5-PRIVATE" "C5-ALIAS"
    "C5-SHADOW" "C5-CYCLE" "C5-CROSS-PROFILE" "C5-CAPABILITY"
    "C5-TARGET" "C5-FOREIGN"})

(def ^:private authoritative-compiler-module-count 41)

(def ^:private authoritative-compiler-path-inventory-sha256
  "sha256:8369be6c68114363b52af1922a5d0d521cee04f35bb865f5fa400090c7c02bfc")

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

(defn- sha256-text
  [value]
  (let [digest (.digest (java.security.MessageDigest/getInstance "SHA-256")
                        (.getBytes ^String value
                                   java.nio.charset.StandardCharsets/UTF_8))]
    (str "sha256:"
         (apply str (map #(format "%02x" (bit-and 0xff %)) digest)))))

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

(defn- rejection-request
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:compiler :sh06-request]))

(defn- rejection-oracle
  [basename extension]
  (get-in (ns-metadata (c2-artifact "rejected" basename extension))
          [:sh06]))

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
      (throw (ex-info "Required SH-06 coordinator adapter is absent"
                      {:id "SH06-ADAPTER-ABSENT" :symbol symbol}))))

(defn- fixture-module-inputs [source-path source-text]
  ;; Explicit test libraries; production constructors never discover modules.
  (let [forms (mapv :form (bootstrap/read-source-form-records source-path source-text))
        module (bootstrap/parse-module source-path forms)
        extension (if (str/ends-with? source-path ".qst") ".qst" ".gravity")
        catalog {'shared.core "shared-core" 'gravity.io "io"}]
    {:artifact :gravity/sh06-module-inputs :schema-version 1
     :module-artifacts (mapv #(bootstrap/sh06-resolution-file-artifact (fixture-path "modules" % extension))
                            (distinct (keep #(get catalog (:module %)) (:requires module))))}))

(defn- sh06-file-artifact
  [source-path]
  ((required-var 'sh06-resolution-file-artifact) source-path (fixture-module-inputs source-path (slurp source-path))))

(defn- sh06-source-artifact
  [source-path source-text]
  ((required-var 'sh06-resolution-source-artifact) source-path source-text (fixture-module-inputs source-path source-text)))

(deftest sh06-current-reader-module-source-and-parent-composition
  ;; New ordinary transport only, not B51 or authoritative reader admission.
  (let [reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        library-path (fixture-path "modules" "values" ".gravity")
        caller-path (fixture-path "modules" "consumer" ".gravity")
        empty-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        stage (fn [label operation]
                (println (pr-str {:current-module-stage label :event :begin}))
                (let [start (System/nanoTime) value (operation)]
                  (println (pr-str {:current-module-stage label :event :complete
                                    :elapsed-ms (quot (- (System/nanoTime) start) 1000000)})) value))
        observed (try {:value (stage :library #(bootstrap/compiler-c5-resolution-source-artifact
                                                library-path (slurp library-path) empty-inputs reader))}
                      (catch clojure.lang.ArityException error {:arity-error (.getMessage error)}))
        library (:value observed)]
    (is (= :accepted (:status library)) (pr-str (dissoc observed :value)))
    (when (= :accepted (:status library))
      (let [inputs (assoc empty-inputs :module-artifacts [library])
            text (slurp caller-path)
            syntax (stage :caller-c3 #(bootstrap/compiler-c3-syntax-source-artifact
                                      caller-path text (bootstrap/reader-project-context-for-source caller-path)
                                      nil nil reader))
            parent (stage :caller-sh05 #(bootstrap/sh05-macro-artifact-from-c3-artifact caller-path syntax))
            caller (stage :caller-from-parent #(bootstrap/sh06-resolution-artifact-from-sh05-artifact
                                                caller-path parent inputs reader))
            replay (stage :caller-from-source #(bootstrap/compiler-c5-resolution-source-artifact
                                                caller-path text inputs reader))
            report (stage :fresh-c5 #(bootstrap/sh06-resolution-artifact-verification caller))
            exported (first (filter #(and (= :namespace (:binding-class %)) (= 'identity-i64 (:name %)))
                                    (get-in library [:binding-table :bindings])))
            imported (first (filter #(and (= :import (:binding-class %)) (= 'identity-i64 (:name %)))
                                    (get-in caller [:binding-table :bindings])))]
        (is (= :accepted (:status caller) (:status replay)))
        (is (= caller replay))
        (is (= :passed (:status report)))
        (is (= :gravity/sh06-ordinary-module-resolution-request
               (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :artifact])))
        (is (= 2 (get-in caller [:gravity-resolution-boundary :authenticated-resolution-request :schema-version])))
        (is (= inputs (get-in caller [:gravity-resolution-boundary :module-inputs])))
        (is (= (:artifact-id library) (:definition-artifact-id imported)))
        (is (= (:definition-syntax-id exported) (:definition-syntax-id imported)))
        (is (= (:binding-id exported) (get-in imported [:semantic-span :origin-binding-id])))
        (is (= [(:artifact-id library)] (get-in caller [:incremental-invalidation-keys :inputs :module-artifact-ids])))
        (is (= caller-path (get-in caller [:provenance :source-path])))
        (let [reject (fn [supplied-parent supplied-inputs]
                       (try
                         (bootstrap/sh06-resolution-artifact-from-sh05-artifact
                          caller-path supplied-parent supplied-inputs reader)
                         {:unexpected-success true}
                         (catch clojure.lang.ExceptionInfo error (ex-data error))))
              retained-path [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact
                             :sh03-reader-authentication :reader-source-revision]]
          (doseq [[supplied-parent supplied-inputs]
                  [[parent nil]
                   [parent empty-inputs]
                   [parent (assoc inputs :module-artifacts [library library])]
                   [parent (assoc inputs :module-artifacts [(assoc library :artifact-id (sha256-text "stale"))])]
                   [(update-in parent (pop retained-path) dissoc (peek retained-path)) inputs]
                   [(assoc-in parent (conj retained-path :source-content-hash) (sha256-text "changed")) inputs]]]
            (let [failure (reject supplied-parent supplied-inputs)]
              (is (contains? c5-rules (:id failure)) (pr-str failure))
              (is (nil? (:unexpected-success failure))))))
        (doseq [name ["values" "consumer"]]
          (is (= (slurp (fixture-path "modules" name ".gravity"))
                 (slurp (fixture-path "modules" name ".qst")))))))))

(deftest sh06-current-reader-module-physical-path-and-arity-fences
  (let [source (fixture-path "modules" "consumer" ".gravity")
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
        failure (fn [operation]
                  (try (operation) {:unexpected-success true}
                       (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= [2 3 4 5] (mapv count (:arglists (meta #'bootstrap/compiler-c5-resolution-source-artifact)))))
    (is (= [1 2 3 4 5] (mapv count (:arglists (meta #'bootstrap/sh06-resolution-artifact-from-sh05-artifact)))))
    ;; This current cohort differs from the historical default reader pin.
    (is (= "C2-HASH" (:id (failure #(bootstrap/compiler-c5-resolution-source-artifact
                                    source (slurp source))))))
    (doseq [reader [nil 17 (str "bad" \u0000 "reader") source]]
      (is (= "C2-HASH" (:id (failure #(bootstrap/compiler-c5-resolution-source-artifact source "" inputs reader)))))
      (is (= "C2-HASH" (:id (failure #(bootstrap/sh06-resolution-artifact-from-sh05-artifact source nil inputs reader))))))
    (let [b51 (failure #(bootstrap/sh06-resolution-artifact-from-sh05-artifact source nil nil))]
      (is (= "C5-UNRESOLVED" (:id b51)))
      (is (= :missing-context (get-in b51 [:observed :reason]))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) 7)
                   (equals [_] (swap! callbacks inc) false)
                   (toString [] (swap! callbacks inc) "opaque-reader"))]
      (.hashCode opaque) (.equals opaque :probe) (.toString opaque)
      (is (= 3 @callbacks))
      (reset! callbacks 0)
      (is (= "C2-HASH" (:id (failure #(bootstrap/compiler-c5-resolution-source-artifact source "" inputs opaque)))))
      (is (= "C2-HASH" (:id (failure #(bootstrap/sh06-resolution-artifact-from-sh05-artifact source nil inputs opaque)))))
      (is (= 0 @callbacks)))))

(defn- verification
  [artifact]
  ((required-var 'sh06-resolution-artifact-verification) artifact))

(defn- capability-proof
  [artifact]
  ((required-var 'sh06-resolution-capability-based-proof) artifact))

(defn- internal-verification
  [artifact construction?]
  ((private-bootstrap-var 'sh06-resolution-artifact-verification*)
   artifact construction?))

(defn- construction-proof
  [artifact]
  ((private-bootstrap-var
    'sh06-resolution-capability-based-proof-for-construction)
   artifact))

(defn- sha256-id?
  [value]
  (boolean (and (string? value)
                (re-matches #"sha256:[0-9a-f]{64}" value))))

(defn- all-values
  [value]
  (tree-seq coll? seq value))

(defn- all-maps
  [value]
  (filter map? (all-values value)))

(defn- contains-value?
  [value expected]
  (boolean (some #(= expected %) (all-values value))))

(defn- resolution-orders
  [artifact]
  (->> (all-maps artifact)
       (map (fn [product]
              (or (some (fn [[key value]]
                          (when (= :resolution-order key) value))
                        product)
                  (some (fn [[key value]]
                          (when (= :resolution-kind key) value))
                        product))))
       (remove nil?)
       set))

(defn- semantic-identities
  [artifact]
  ((required-var 'sh06-resolution-artifact-identity-input) artifact))

(deftest sh06-product-identity-chunks-preserve-old-equations
  ;; These bounded values test the former equations, not proof authority.
  (let [hash-value (required-var 'reader-canonical-hash)
        old-chunks
        (fn [values]
          (mapv (fn [index entries]
                  {:chunk-index index :entry-count (count entries)
                   :chunk-id (hash-value
                              {:domain :gravity/sh06-resolution-identity-chunk-v1
                               :chunk-index index :entries (vec entries)})})
                (range) (partition-all 16 values)))
        old-pair
        (fn [artifact]
          {:binding-id-chunks
           (old-chunks (mapv :binding-id (get-in artifact [:binding-table :bindings])))
           :resolution-chunks
           (old-chunks (mapv #(dissoc % :source-span) (:resolution-table artifact)))})
        old-identity
        (fn [a]
          (merge
           {:domain :gravity/sh06-resolution-artifact-v1
            :kind (:kind a) :slice (:slice a) :task (:task a)
            :adapter-contract (get-in a [:gravity-resolution-boundary :adapter-contract])
            :analysis-artifact-id (get-in a [:gravity-resolution-boundary :resolved-analysis :artifact-id])
            :source-revision-id (get-in a [:gravity-resolution-boundary :authenticated-resolution-request :module :source-revision-id])
            :upstream-artifact-id (get-in a [:sh05-macro-artifact :artifact-id])
            :upstream-syntax-stream-id (get-in a [:sh05-macro-artifact :expanded-syntax-stream-id])
            :upstream-trace-id (get-in a [:sh05-macro-artifact :macro-expansion-trace-id])
            :dependency-graph-id
            (hash-value {:domain :gravity/sh06-dependency-graph-v1
                         :nodes (get-in a [:dependency-graph :nodes])
                         :edges (mapv #(dissoc % :source-span) (get-in a [:dependency-graph :edges]))})
            :invalidation-id (get-in a [:incremental-invalidation-keys :invalidation-id])}
           (old-pair a)))
        old-summary
        (fn [a]
          (merge
           {:slice :SH-06 :artifact-id (:artifact-id a)
            :analysis-artifact-id (get-in a [:gravity-resolution-boundary :resolved-analysis :artifact-id])
            :upstream-artifact-id (get-in a [:sh05-macro-artifact :artifact-id])
            :source-revision-id (get-in a [:gravity-resolution-boundary :authenticated-resolution-request :module :source-revision-id])
            :binding-count (count (get-in a [:binding-table :bindings]))
            :reference-count (count (:resolution-table a))
            :dependency-edge-count (count (get-in a [:dependency-graph :edges]))}
           (old-pair a)))
        pair-builder (private-bootstrap-var 'sh06-resolution-product-identity-chunks)
        identity-builder (private-bootstrap-var 'sh06-resolution-artifact-identity-input-with-chunks)
        summary-builder (private-bootstrap-var 'sh06-resolution-envelope-summary-with-chunks)
        identity-input (required-var 'sh06-resolution-artifact-identity-input)
        artifact-id (required-var 'sh06-resolution-artifact-id)
        summary (required-var 'sh06-resolution-envelope-summary)]
    (doseq [builder [pair-builder identity-builder summary-builder]]
      (is (true? (:private (meta builder)))))
    (doseq [public [identity-input artifact-id summary]]
      (is (= '([artifact]) (:arglists (meta public))))
      (is (thrown? clojure.lang.ArityException (public {} {}))))
    (doseq [size [0 1 15 16 17 31 32 33]]
      (let [bindings (mapv #(hash-map :binding-id (sha256-text (str (mod % 3)))) (range size))
            rows (mapv #(hash-map :symbol (symbol (str "name" (mod % 3)))
                                  :binding-id (:binding-id (get bindings %))
                                  :source-span {:form-index %}
                                  :origin {:source-span {:form-index (mod % 3)}})
                       (range size))
            a0 {:kind :gravity/sh06-resolution-artifact :slice :SH-06 :task "SH-06"
                :artifact-id nil :binding-table {:bindings bindings} :resolution-table rows
                :gravity-resolution-boundary
                {:adapter-contract :equation-kernel
                 :resolved-analysis {:artifact-id (sha256-text "analysis")}
                 :authenticated-resolution-request {:module {:source-revision-id (sha256-text "source")}}}
                :sh05-macro-artifact {:artifact-id (sha256-text "upstream")
                                     :expanded-syntax-stream-id (sha256-text "syntax")
                                     :macro-expansion-trace-id (sha256-text "trace")}
                :dependency-graph {:nodes ['one 'two] :edges [{:from 'one :to 'two :source-span {:form-index 0}}]}
                :incremental-invalidation-keys {:invalidation-id (sha256-text "invalidation")}}
            pair (pair-builder a0)
            a1 (assoc a0 :artifact-id (hash-value (old-identity a0)))
            changed-span (update a1 :resolution-table
                                 #(mapv (fn [row] (assoc row :source-span {:form-index -1})) %))]
        (testing (str "chunk boundary " size)
          (is (= (old-pair a0) pair (pair-builder a1)))
          (is (= (old-identity a0) (identity-input a0) (identity-builder a0 pair)))
          (is (= (:artifact-id a1) (artifact-id a0) (artifact-id a1)))
          (is (= (old-summary a1) (summary a1) (summary-builder a1 pair)))
          (is (= (artifact-id a1) (artifact-id changed-span)))
          (is (= (summary a1) (summary changed-span)))
          (is (= (mapv :entry-count (:binding-id-chunks pair))
                 (mapv count (partition-all 16 bindings))))
          (when (> size 1)
            (is (not= (artifact-id a1) (artifact-id (update a1 :resolution-table #(vec (reverse %))))))))))))

(deftest sh06-invocation-local-chunks-preserve-fresh-verification
  (let [artifact (sh06-file-artifact (fixture-path "accepted" "compiler-subset" ".gravity"))
        pair-var (private-bootstrap-var 'sh06-resolution-product-identity-chunks)
        original-pair @pair-var
        seen (atom [])
        verify (fn [candidate]
                 (reset! seen [])
                 (let [report (with-redefs-fn
                                {pair-var (fn [current]
                                            (swap! seen conj current)
                                            (original-pair current))}
                                #(internal-verification candidate false))]
                   (is (= 1 (count @seen)))
                   (is (identical? candidate (first @seen)))
                   report))
        identity (required-var 'sh06-resolution-artifact-id)
        mutations
        [[:binding-id :binding-table-current?
          #(assoc-in % [:binding-table :bindings 0 :binding-id] (sha256-text "changed-binding"))]
         [:row-semantic :resolution-table-current?
          #(assoc-in % [:resolution-table 0 :symbol] 'substituted)]
         [:row-order :resolution-table-current?
          #(update % :resolution-table (comp vec reverse))]
         [:source-span :resolution-table-current?
          #(assoc-in % [:resolution-table 0 :source-span :form-index] -1)]
         [:stale-envelope :fresh-sh02-envelope-verified?
          #(assoc-in % [:gravity-resolution-boundary :authenticated-envelope-descriptor
                        :semantic-projections 0 :value :binding-count] -1)]]]
    (is (= :passed (:status (verify artifact))))
    (doseq [[label check mutate] mutations]
      (let [candidate (mutate artifact)
            report (verify candidate)]
        (is (= :failed (:status report)) (name label))
        (is (false? (get-in report [:checks check])) (name label))
        (is (true? (get-in report [:checks :fresh-gravity-request-replay?])) (name label))
        (is (= (contains? #{:source-span :stale-envelope} label)
               (= (identity artifact) (identity candidate))) (name label))
        (when (contains? #{:binding-id :row-semantic :row-order} label)
          (is (false? (get-in report [:checks :artifact-identity-current?])) (name label))
          (is (false? (get-in report [:checks :fresh-sh02-envelope-verified?])) (name label)))))
    (testing "malformed values reject before chunk construction"
      (reset! seen [])
      (with-redefs-fn
        {pair-var (fn [candidate] (swap! seen conj candidate) (throw (ex-info "preflight bypass" {})))}
        #(is (= :failed (:status (internal-verification nil false)))))
      (is (empty? @seen)))))

(defn- rejection-data-for-source
  [source-path source-text]
  (try
    (sh06-source-artifact source-path source-text)
    {:unexpected-success true}
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:raw-host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- normalized-policy-rejection [source-path source-text]
  ;; Historical metadata-driven matrices exercise v1 DATA policy only. They
  ;; never create or supply an ordinary module artifact.
  (try
    (let [parent (bootstrap/sh05-macro-source-artifact source-path source-text)
          request (bootstrap/sh06-resolution-base-request source-path parent)
          binding (bootstrap/sh06-resolution-current-binding! source-path)]
      (bootstrap/sh06-resolution-run-request! source-path binding request)
      {:unexpected-success true})
    (catch clojure.lang.ExceptionInfo e (ex-data e))))

(defn- rejection-data
  [source-path]
  (try
    (sh06-file-artifact source-path)
    {:unexpected-success true}
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))
    (catch Throwable error
      {:raw-host-error (.getName (class error))
       :message (.getMessage error)})))

(defn- delete-tree!
  [tree]
  (when tree
    (doseq [file (reverse (file-seq (io/file tree)))]
      (io/delete-file file true))))

(defn- copy-source!
  [source-path destination]
  (io/make-parents destination)
  (with-open [input (io/input-stream source-path)
              output (io/output-stream destination)]
    (io/copy input output)))

(defn- authoritative-compiler-paths
  []
  (let [directory (io/file (path "bootstrap/gravity/src"))]
    (->> (file-seq directory)
         (filter #(.isFile %))
         (map #(.getCanonicalPath %))
         (filter #(.endsWith ^String % ".gravity"))
         sort
         vec)))

(defn- authoritative-compiler-relative-paths
  [source-paths]
  (mapv (fn [source-path]
          (-> (.relativize @root (.toPath (io/file source-path)))
              str
              (str/replace "\\" "/")))
        source-paths))

(defn- parse-corpus-shard
  []
  (when-let [raw (not-empty (System/getenv "SH06_CORPUS_SHARD"))]
    (let [[_ index-text count-text]
          (re-matches #"([1-9][0-9]*)/([1-9][0-9]*)" raw)]
      (when-not index-text
        (throw (ex-info "SH06_CORPUS_SHARD must use the form i/n"
                        {:id "SH06-CORPUS-SHARD" :value raw})))
      (let [index (Long/parseLong index-text)
            count (Long/parseLong count-text)]
        (when (> index count)
          (throw (ex-info "SH06_CORPUS_SHARD index exceeds shard count"
                          {:id "SH06-CORPUS-SHARD" :value raw
                           :index index :count count})))
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

(defn- module-policy-input [namespace exports definitions imports references]
  ;; Normalized DATA for the Gravity policy kernel, never an ordinary artifact.
  (let [sha #(str "sha256:" (format "%064x" %))
        module {:namespace namespace :package {:name namespace :version "workspace"}
                :profile :meta :target :jvm :safety :safe :effects [] :capabilities [] :exports exports
                :source-revision-id (sha 1) :c4-artifact-id (sha 2)}
        definitions (mapv (fn [index name]
                            {:name name :kind :function :namespace namespace :package (:package module)
                             :binding-class :namespace :visibility (if (contains? (set exports) name) :public :private)
                             :profile-set [:meta] :target-set [:jvm] :type-ref :gravity.type/function :effects [] :capabilities [] :safety :safe
                             :semantic-span {:ordinal index} :source-span {:source "<kernel>" :form-index index}
                             :definition-syntax-id (sha (+ 20 index)) :definition-artifact-id (sha 2)}) (range) definitions)]
    {:artifact :gravity/sh06-ordinary-module-resolution-request :schema-version 2
     :module module :definitions definitions :imports imports :import-bindings [] :lexical-scopes [] :references references
     :core-bindings [] :dependency-edges [] :module-premises []
     :namespace-source {:semantic-span {:namespace-syntax-id (sha 5)} :source-span {:source "<kernel>" :form-index 0}}
     :macro-expansion-binding {:artifact :gravity/sh05-macro-expansion-binding :schema-version 1
                              :c4-artifact-id (sha 2) :source-revision-id (sha 1) :macro-result-id (sha 3) :authenticated-envelope-id (sha 4)}
     :provenance {:actual-source-path "<kernel>"}}))

(deftest sh06-ordinary-module-policy-development-kernel
  (let [binding (bootstrap/sh06-resolution-current-binding! "<module-policy>")
        invoke #(bootstrap/sh06-resolution-execute! "<module-policy>" binding %1 %2)
        run #(bootstrap/sh06-resolution-run-request! "<module-policy>" binding %)
        library (module-policy-input 'example.values '[identity-i64] '[identity-i64 hidden] [] [])
        premise-for (fn [request]
                      (let [result (run request)]
                        {:artifact-id (sha256-text (pr-str request)) :request request
                         :definitions (:definitions request) :analysis (:resolved-analysis result)
                         :raw-analysis (:raw-analysis result) :digest-requests (:digest-requests result)
                         :resolved-digests (:resolved-digests result)}))
        premise (premise-for library)
        dependency {:namespace 'example.values :alias 'values :kind :namespace :profile :meta :targets [:jvm]
                    :visibility :public :refer [] :effects [] :capabilities [] :safety :safe :boundary nil
                    :semantic-span {:dependency-ordinal 0} :source-span {:source "<kernel>" :form-index 0}
                    :dependency-artifact-id (sha256-text "ignored-header") :foreign-record-complete? true}
        reference {:syntax-id (sha256-text "use") :symbol 'values/identity-i64 :qualifier 'values :name 'identity-i64
                   :position :expression :scope-chain [] :semantic-span {:ordinal 0} :source-span {:source "<kernel>"}}
        caller (assoc (module-policy-input 'example.consumer [] [] [dependency] [reference]) :module-premises [premise])
        prepared (:request (invoke 'sh06-module-prepare [caller]))
        result (invoke 'sh06-build-resolution-template [caller])
        imported (first (:import-bindings prepared))
        original (first (filter #(= 'identity-i64 (:name %)) (get-in premise [:analysis :binding-table])))
        reason #(get-in (invoke 'sh06-build-resolution-template [%]) [:diagnostics 0 :facts :reason])
        rule #(get-in (invoke 'sh06-build-resolution-template [%]) [:diagnostics 0 :rule])]
    (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
    (is (= (:artifact-id premise) (get-in prepared [:imports 0 :dependency-artifact-id]) (:definition-artifact-id imported)))
    (is (= (:definition-syntax-id original) (:definition-syntax-id imported)))
    (is (= (:binding-id original) (get-in imported [:semantic-span :origin-binding-id])))
    (is (not (contains? (get-in imported [:semantic-span :origin-definition]) :source-span)))
    (is (= :missing-module-artifact (reason (assoc caller :module-premises [] :references []))))
    (is (= "C5-PRIVATE" (rule (assoc-in caller [:references 0 :name] 'hidden))))
    (is (= "C5-PRIVATE" (rule (-> caller (assoc :references []) (assoc-in [:imports 0 :refer] '[hidden])))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:references 0 :name] 'missing))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:references 0 :qualifier] nil))))
    (is (= :accepted (:status (invoke 'sh06-build-resolution-template [(-> caller
               (assoc-in [:references 0 :qualifier] nil) (assoc-in [:imports 0 :refer] '[identity-i64]))]))))
    (is (= "C5-AMBIGUOUS" (rule (assoc caller :module-premises [premise premise]))))
    (is (= "C5-ALIAS" (rule (update caller :imports conj dependency))))
    (let [repeated (-> caller (update :imports conj (assoc dependency :alias 'also-values))
                       (update :imports conj (assoc dependency :alias nil :refer '[identity-i64])))
          normalized (:request (invoke 'sh06-module-prepare [repeated]))]
      (is (= :accepted (:status (invoke 'sh06-build-resolution-template [repeated]))))
      (is (= :accepted (:status (invoke 'sh06-build-resolution-template
                                        [(assoc-in repeated [:references 0 :qualifier] 'example.values)]))))
      (is (= :accepted (:status (invoke 'sh06-build-resolution-template
                                        [(-> repeated (assoc-in [:references 0 :qualifier] nil)
                                             (update :imports conj (assoc dependency :alias nil :refer '[identity-i64])))]))))
      (is (= 2 (count (:import-bindings normalized))))
      (is (= 3 (count (:imports normalized)))))
    (doseq [[expected altered] [["C5-TARGET" (assoc-in caller [:module :target] :wasm)]
                               ["C5-CROSS-PROFILE" (assoc-in caller [:module :profile] :hosted)]]]
      (is (= expected (rule altered))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:module-premises 0 :definitions 0 :definition-syntax-id] (sha256-text "forged")))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:module-premises 0 :analysis :exports] '[fabricated]))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:module-premises 0 :analysis :dependency-graph :edges]
                                          [{:from 'example.values :to 'fabricated}]))))
    (is (= :accepted (:status (invoke 'sh06-build-resolution-template
                              [(assoc-in caller [:references 0 :qualifier] 'example.values)]))))
    (is (= "C5-UNRESOLVED" (rule (assoc-in caller [:module :exports] '[fabricated]))))
    (is (= [(:artifact-id premise)] (get-in (run caller) [:resolved-analysis :incremental-invalidation-inputs :module-artifact-ids])))
    (let [a0 (module-policy-input 'example.consumer [] [] [] [])
          b (-> library
                (assoc :imports [(assoc dependency :namespace 'example.consumer :alias 'consumer)]
                       :module-premises [(premise-for a0)]))
          cyclic (assoc caller :module-premises [(premise-for b)])]
      (is (= "C5-CYCLE" (rule cyclic))))
    (let [other (module-policy-input 'example.other '[identity-i64] '[identity-i64] [] [])
          ambiguous (-> caller
                        (assoc :module-premises [premise (premise-for other)])
                        (assoc-in [:imports 0 :refer] '[identity-i64])
                        (update :imports conj (assoc dependency :namespace 'example.other :alias 'other :refer '[identity-i64]))
                        (assoc-in [:references 0 :qualifier] nil))]
      (is (= "C5-AMBIGUOUS" (rule ambiguous))))
    (let [impure-core (-> library
                          (assoc-in [:module :profile] :core)
                          (assoc-in [:module :effects] [:io/write])
                          (update :definitions #(mapv (fn [d] (assoc d :profile-set [:core] :effects [:io/write])) %)))
          same-profile (-> caller (assoc-in [:module :profile] :core)
                           (assoc :module-premises [(premise-for impure-core)]))
          prepared (:request (invoke 'sh06-module-prepare [same-profile]))]
      (is (= :accepted (:status (invoke 'sh06-build-resolution-template [same-profile]))))
      (is (nil? (get-in prepared [:imports 0 :boundary]))))
    (doseq [[expected target effects capabilities grants]
            [[nil :jvm [] [] []] ["C5-TARGET" :wasm [] [] []]
             ["C5-CAPABILITY" :jvm [] [:fs/read] []]
             ["C5-CROSS-PROFILE" :jvm [:filesystem/read] [:fs/read] [:fs/read]]]]
      (let [request (-> library (assoc-in [:module :profile] :core) (assoc-in [:module :target] target)
                        (assoc-in [:module :effects] effects) (assoc-in [:module :capabilities] capabilities)
                        (update :definitions #(mapv (fn [d] (assoc d :profile-set [:core] :target-set [target]
                                                                   :effects effects :capabilities capabilities)) %)))
            changed (-> caller (assoc :module-premises [(premise-for request)]) (assoc-in [:module :capabilities] grants))
            result (invoke 'sh06-build-resolution-template [changed])]
        (is (= expected (get-in result [:diagnostics 0 :rule])) (pr-str (:diagnostics result)))
        (when-not expected (is (= :accepted (:status result))))))
    (is (= (get-in caller [:imports 0 :source-span]) (get-in (invoke 'sh06-build-resolution-template
                       [(assoc caller :module-premises [])]) [:diagnostics 0 :source-span])))))

(deftest sh06-ordinary-and-strict-request-routing-remain-distinct
  (let [source-path "<module-route>.gravity"
        parent (bootstrap/sh05-macro-source-artifact source-path
                 "(ns example.route (:profile :meta) (:target :jvm))")
        base (bootstrap/sh06-resolution-base-request source-path parent)
        ordinary (bootstrap/sh06-resolution-request source-path parent)
        context {:strict-source-context :sentinel}
        calls (atom [])]
    (is (= :gravity/sh06-authenticated-c4-resolution-request (:artifact base)))
    (is (= 1 (:schema-version base)))
    (is (= :gravity/sh06-ordinary-module-resolution-request (:artifact ordinary)))
    (is (= 2 (:schema-version ordinary)))
    (with-redefs [bootstrap/sh06-resolution-base-request
                  (fn [& args] (swap! calls conj args) :strict-dispatch)]
      (is (= :strict-dispatch (bootstrap/sh06-resolution-request source-path parent context))))
    (is (= [[source-path parent context]] @calls))
    ;; These existing SOURCE authority paths hash the v1 base. This is a
    ;; structural routing regression check, not SOURCE admission evidence.
    (doseq [[suffix expected-count] [["" 0] ["-12" 1] ["-14" 2] ["-18" 1]]]
      (let [text (slurp (path (str "bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker" suffix ".clj")))]
        (is (not (re-find #"\bsh06-resolution-request\b" text)))
        (if (zero? expected-count)
          (is (str/includes? text "sh06-resolution-base-request"))
          (is (= expected-count (count (re-seq #"\(sh06-resolution-base-request\b" text)))))))))

(deftest sh06-ordinary-module-authenticated-composition
  (let [results
        (mapv
         (fn [extension]
           (let [library (bootstrap/sh06-resolution-file-artifact (fixture-path "modules" "values" extension))
                 inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact (fixture-path "modules" "consumer" extension) inputs)
                 lib-binding (first (filter #(and (= :namespace (:binding-class %)) (= 'identity-i64 (:name %)))
                                            (get-in library [:binding-table :bindings])))
                 imported (first (filter #(= 'identity-i64 (:name %)) (filter #(= :import (:binding-class %))
                                                                                    (get-in caller [:binding-table :bindings]))))
                 alias (first (get-in caller [:alias-table :aliases]))
                 core-artifact (bootstrap/sh07-core-from-resolution-artifact caller)
                 core (get-in core-artifact [:gravity-core-boundary :canonical-core-artifact])
                 call (first (:calls core))
                 operator (first (filter #(= (:node-id %) (:operator-node-id call)) (:nodes core)))
                 binding (first (filter #(= (:binding-id %) (get-in operator [:attributes :binding-id]))
                                        (get-in core-artifact [:gravity-core-boundary :authenticated-core-request :binding-table])))]
             (is (= :accepted (:status library) (:status caller) (:status core-artifact)))
             (is (= :passed (:status (verification library)) (:status (verification caller))))
             (is (= (:artifact-id library) (:dependency-artifact-id alias) (:definition-artifact-id imported)
                    (:definition-artifact-id binding)))
             (is (= (:binding-id imported) (:upstream-binding-id binding)))
             (is (= (:definition-syntax-id lib-binding) (:definition-syntax-id imported)
                    (:definition-syntax-id binding) (get-in operator [:attributes :definition-syntax-id])))
             (is (= (:binding-id lib-binding) (get-in imported [:semantic-span :origin-binding-id])))
             (is (= [(:artifact-id library)] (get-in caller [:incremental-invalidation-keys :inputs :module-artifact-ids])))
             (is (= 1 (count (:calls core))))
             (is (= :nonlocal-or-nonfunction (get-in core [:call-edges 0 :classification])))
             (is (= :passed (:status (bootstrap/sh07-core-artifact-verification core-artifact))))
             (require 'gravity.self-hosting.sh08-function-call-type-test)
             (let [typed ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'function-result) core-artifact)]
               (is (= :rejected (:status typed)))
               (is (= :missing-imported-function-typing (get-in typed [:diagnostics 0 :reason]))))
             {:library library :caller caller :core core-artifact}))
         [".gravity" ".qst"])]
    (is (= (get-in results [0 :library :artifact-id]) (get-in results [1 :library :artifact-id])))
    (is (= (get-in results [0 :caller :artifact-id]) (get-in results [1 :caller :artifact-id])))
    (is (= (get-in results [0 :core :artifact-id]) (get-in results [1 :core :artifact-id])))))

(deftest sh06-ordinary-module-origin-negative-and-replay-composition
  (with-redefs-fn {(private-bootstrap-var 'sh06-resolution-cached-binding) (atom nil)}
    (fn []
      (let [library-path (fixture-path "modules" "values" ".gravity")
            caller-path (fixture-path "modules" "consumer" ".gravity")
            text (slurp caller-path)
            library (bootstrap/sh06-resolution-file-artifact library-path)
            inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
            caller (bootstrap/sh06-resolution-source-artifact caller-path text inputs)
            core-request (bootstrap/sh07-core-authenticated-request caller)
            imported (first (filter #(and (= :import (:binding-class %)) (= 'identity-i64 (:name %)))
                                    (get-in caller [:binding-table :bindings])))
            rows (filter #(= (:binding-id imported) (:upstream-binding-id %)) (:binding-table core-request))
            row (first rows)
            reject (fn [source supplied]
                     (try (bootstrap/compiler-c5-resolution-source-artifact caller-path source supplied)
                          {:unexpected-success true}
                          (catch clojure.lang.ExceptionInfo e (ex-data e))))]
        (is (= :accepted (:status library) (:status caller)))
        (is (= 1 (count rows)))
        (is (= (:artifact-id library) (:definition-artifact-id imported) (:definition-artifact-id row)))
        (is (= (:binding-id imported) (:upstream-binding-id row)))
        (is (= (:definition-syntax-id imported) (:definition-syntax-id row)))
        (is (some #(= (:binding-id row) (:binding-id %)) (:resolution-table core-request)))
        (let [missing (try
                        (bootstrap/compiler-c5-resolution-source-artifact caller-path
                          (str/replace text "(values/identity-i64 x)" "x"))
                        {:unexpected-success true}
                        (catch clojure.lang.ExceptionInfo e (ex-data e)))]
          (is (= "C5-UNRESOLVED" (:id missing)))
          (is (= :missing-module-artifact (get-in missing [:facts :reason])) (pr-str missing))
          (is (= 'example.values (get-in missing [:dependency-edge :namespace])))
          (is (= 'values (get-in missing [:dependency-edge :alias])))
          (is (= caller-path (get-in missing [:source-span :source]))))
        (doseq [[symbol expected] [["private-i64" "C5-PRIVATE"] ["missing-i64" "C5-UNRESOLVED"]]]
          (let [failure (reject (str/replace text "values/identity-i64" (str "values/" symbol)) inputs)]
            (is (= expected (:id failure)) (pr-str failure))
            (is (= :name-resolution (:stage failure)))
            (is (= caller-path (get-in failure [:source-span :source])))))
        (doseq [forged [(assoc-in library [:import-export-table :exports] '[fabricated])
                        (assoc-in library [:binding-table :bindings 0 :definition-syntax-id] (sha256-text "forged"))
                        (assoc-in library [:sh05-macro-artifact :artifact-id] (sha256-text "stale-source"))]]
          (let [failure (try
                          (bootstrap/sh06-resolution-module-input-views! caller-path (assoc inputs :module-artifacts [forged]))
                          {:unexpected-success true}
                          (catch clojure.lang.ExceptionInfo e (ex-data e)))]
            (is (= "C5-UNRESOLVED" (:id failure)))
            (is (= :invalid-module-artifact (get-in failure [:facts :reason])))))
        (let [serialized (bootstrap/sh06-resolution-serialize caller)
              restored (bootstrap/sh06-resolution-deserialize serialized)]
          (is (= caller restored))
          (is (= :passed (:status (verification restored))))
          (is (= (:artifact-id library)
                 (get-in restored [:gravity-resolution-boundary :module-inputs :module-artifacts 0 :artifact-id]))))
        (let [changed-source (str/replace (slurp library-path) "(:exports [identity-i64])" "(:exports [identity-i64 private-i64])")
              changed-library (bootstrap/sh06-resolution-source-artifact library-path changed-source)
              changed (bootstrap/sh06-resolution-source-artifact caller-path text
                       (assoc inputs :module-artifacts [changed-library]))]
          (is (not= (:artifact-id library) (:artifact-id changed-library)))
          (is (not= (:artifact-id caller) (:artifact-id changed)))
          (is (not= (get-in caller [:incremental-invalidation-keys :invalidation-id])
                    (get-in changed [:incremental-invalidation-keys :invalidation-id]))))))))

(deftest sh06-generated-defn-shadowing-and-diagnostic-composition
  ;; C4 -> real C5 -> hosted Stage0 C6. This does not execute Gravity C6,
  ;; typecheck the function, admit a module revision or renew corpus evidence.
  (doseq [name ["generated-shadow" "generated-unresolved"]]
    (is (= (slurp (fixture-path "composition" name ".gravity"))
           (slurp (fixture-path "composition" name ".qst")))))
  (let [results
        (mapv
         (fn [extension]
           (let [source (fixture-path "composition" "generated-shadow" extension)
                 expanded (bootstrap/sh05-macro-file-artifact source)
                 reference (bootstrap/macro-file-artifact source)
                 resolved (bootstrap/sh06-resolution-artifact-from-sh05-artifact source expanded)
                 trace (first (:macro-expansion-trace expanded))
                 syntax (second (:expanded-syntax-stream expanded))
                 bindings (get-in resolved [:binding-table :bindings])
                 rows (:resolution-table resolved)
                 lexical (filter #(= :local-lexical-binding (:resolution-order %)) rows)
                 original-reader bootstrap/read-source-form-records
                 reads (atom 0)
                 core (with-redefs [bootstrap/read-source-form-records
                                    (fn [p text]
                                      (when (= p source)
                                        (swap! reads inc)
                                        (throw (ex-info "Hosted C6 reread generated source" {})))
                                      (original-reader p text))]
                        (bootstrap/sh06-c6-lowering-from-resolution-artifact resolved))]
             (is (= :accepted (:status expanded) (:status resolved)))
             (is (= (:expanded-forms reference) (:expanded-forms expanded)))
             (is (= '(def shadow (fn [shadow] (let [next shadow] (let [shadow next] shadow))))
                    (:form syntax)))
             (is (= 'defn (:macro trace)))
             (is (seq (:generated-origin trace)))
             (is (seq (:origin syntax)))
             (is (= (:span syntax) (:call-site-span trace)))
             (is (= 3 (count lexical)))
             (is (= 3 (count (set (map :binding-id lexical)))))
             (is (every? (set (map :binding-id bindings)) (map :binding-id lexical)))
             (is (= :passed (:status (verification resolved))))
             (is (= :gravity/stage0-c6-core-lowering-artifact (:kind core)))
             (is (= :passed (get-in core [:core-verifier-report :status])))
             (is (= (:artifact-id resolved) (get-in core [:c5-name-resolution-artifact :artifact-id])))
             (is (some :generated? (get-in core [:surface-to-core-map :entries])))
             (is (zero? @reads))
             {:expanded (:artifact-id expanded) :resolved (:artifact-id resolved)}))
         [".gravity" ".qst"])]
    (is (= (first results) (second results)))))

(deftest sh06-generated-diagnostic-origin-transport-development-kernel
  ;; Normalized transport checks only; parent authentication is a caller premise.
  ;; No source product, resolver admission or migration evidence is constructed.
  (let [source "<generated-origin-kernel>.gravity"
        canonical (private-bootstrap-var 'reader-canonical-hash)
        span {:byte-start 10 :byte-end 40 :line-start 2 :line-end 2}
        owner-id (sha256-text "expanded-owner")
        artifact-id (sha256-text "parent")
        revision-id (sha256-text "source")
        stream-id (sha256-text "expanded-stream")
        origin [{:kind :source :span span}
                {:kind :generated :producer {:kind :macro :name 'gravity.core/defn}
                 :inputs [(sha256-text "call")]}]
        owner {:syntax/id owner-id :span span :origin origin}
        reference {:syntax-id (canonical {:domain :gravity/sh06-reference-syntax-v1
                                         :owner-syntax-id owner-id :ordinal 1
                                         :symbol 'missing :position :expression})
                   :symbol 'missing :qualifier nil :name 'missing :position :expression
                   :scope-chain [] :source-span span
                   :semantic-span (assoc span :ordinal 1)}
        expanded {:artifact-id artifact-id :expanded-syntax-stream-id stream-id
                  :expanded-syntax-stream [(assoc owner :syntax/id (sha256-text "unrelated")
                                                       :origin [{:kind :generated :producer :unrelated}]) owner]
                  :gravity-macro-boundary {:authenticated-sh04-artifact
                                           {:gravity-syntax-boundary
                                            {:reader-source-revision {:revision-id revision-id}}}}}
        request {:module {:namespace 'example.generated :profile :meta :target :jvm
                          :c4-artifact-id artifact-id :source-revision-id revision-id}
                 :references [reference] :provenance {:actual-source-path source}
                 :macro-expansion-binding {:c4-artifact-id artifact-id :source-revision-id revision-id
                                           :macro-result-id stream-id}}
        details {:reason :no-legal-binding :qualifier nil :name 'missing
                 :position :expression :scope-chain [] :candidate-count 0}
        error (try
                (bootstrap/c5-resolution-fail!
                 "C5-UNRESOLVED" source
                 {:source-span (assoc span :source source) :symbol 'missing :syntax-id (:syntax-id reference)
                  :namespace 'example.generated :profile :meta :target :jvm :candidate-bindings []}
                 {:diagnostic-id "original-diagnostic-id" :severity :error :remediation "original-remediation"
                  :facts {:reason :no-legal-binding :rule-specific details :semantic-span (:semantic-span reference)
                          :source-revision-id revision-id :c4-artifact-id artifact-id :fail-closed true}})
                (catch clojure.lang.ExceptionInfo e e))
        diagnostic (ex-data error)
        recover (private-bootstrap-var 'sh06-resolution-diagnostic-origin)
        candidate (private-bootstrap-var 'sh06-resolution-source-artifact-candidate)
        transport (fn [parent input problem]
                    (with-redefs-fn
                      {(private-bootstrap-var 'sh06-authenticated-sh05-parent!) (fn [& _] :authenticated-test-premise)
                       (private-bootstrap-var 'sh06-authenticated-source-snapshot!) (fn [& _] :snapshot)
                       (private-bootstrap-var 'sh06-resolution-module-request) (fn [& _] input)
                       (private-bootstrap-var 'sh06-resolution-current-binding!) (fn [& _] {})
                       (private-bootstrap-var 'sh06-resolution-run-request!) (fn [& _] (throw problem))}
                      #(try (candidate source parent true)
                            (catch clojure.lang.ExceptionInfo e e))))
        carried (transport expanded request error)]
    (is (= origin (recover source expanded request diagnostic)))
    (is (= origin (:origin-chain (ex-data carried))))
    (is (= diagnostic (dissoc (ex-data carried) :origin-chain)))
    (is (= (.getMessage error) (.getMessage carried)))
    (is (identical? error (.getCause carried)))
    ;; The existing reference checker also reports UNRESOLVED for a single
    ;; malformed core candidate. Its reason and candidate count remain intact.
    (let [malformed (-> diagnostic
                        (assoc :candidate-bindings [{:name 'missing :binding-class :core}])
                        (assoc-in [:facts :reason] :malformed-core-binding-availability)
                        (assoc-in [:facts :rule-specific :reason] :malformed-core-binding-availability)
                        (assoc-in [:facts :rule-specific :candidate-count] 1))
          problem (ex-info "malformed candidate" malformed)
          result (transport expanded request problem)]
      (is (= origin (recover source expanded request malformed)))
      (is (= origin (:origin-chain (ex-data result))))
      (is (= malformed (dissoc (ex-data result) :origin-chain)))
      (doseq [changed [(assoc-in malformed [:facts :rule-specific :candidate-count] 0)
                       (assoc malformed :candidate-bindings [])
                       (assoc-in malformed [:facts :reason] :no-legal-binding)
                       (assoc-in malformed [:facts :rule-specific :reason] :no-legal-binding)]]
        (is (nil? (recover source expanded request changed)))))
    (doseq [ordinal [nil 0 -1 2 1.5 "1" 999999999999999999999999N]]
      (is (nil? (recover source expanded request (assoc-in diagnostic [:facts :semantic-span :ordinal] ordinal)))))
    (doseq [changed [(dissoc diagnostic :syntax-id)
                     (assoc diagnostic :syntax-id (sha256-text "changed"))
                     (assoc diagnostic :symbol 'other)
                     (assoc-in diagnostic [:source-span :source] "<other>.gravity")
                     (assoc-in diagnostic [:facts :semantic-span :byte-end] 41)
                     (assoc-in diagnostic [:facts :rule-specific :position] :operator)
                     (assoc-in diagnostic [:facts :rule-specific :scope-chain] [:unrelated])
                     (update-in diagnostic [:facts :rule-specific] dissoc :qualifier)
                     (assoc-in diagnostic [:facts :rule-specific :candidate-count] -1)
                     (assoc-in diagnostic [:facts :rule-specific :candidate-count] 1)
                     (assoc diagnostic :id "C5-CYCLE")
                     (assoc diagnostic :candidate-bindings [:unexpected])
                     (assoc diagnostic :namespace 'other)
                     (assoc diagnostic :active-profile :core)
                     (assoc diagnostic :target :wasm)
                     (assoc-in diagnostic [:facts :source-revision-id] (sha256-text "changed"))
                     (assoc-in diagnostic [:facts :c4-artifact-id] (sha256-text "changed"))
                     (assoc-in diagnostic [:facts :rule-specific] 7)
                     (assoc diagnostic :origin-chain [:already-present])]]
      (is (nil? (recover source expanded request changed)))
      (let [problem (ex-info "preserved" changed)]
        (is (identical? problem (transport expanded request problem)))))
    (doseq [changed [(assoc request :references nil)
                     (assoc request :references {})
                     (assoc-in request [:references 0 :syntax-id] (sha256-text "changed"))
                     (assoc-in request [:references 0 :symbol] 'other)
                     (assoc-in request [:references 0 :source-span :byte-end] 41)
                     (assoc-in request [:provenance :actual-source-path] "<other>.gravity")
                     (assoc-in request [:macro-expansion-binding :c4-artifact-id] (sha256-text "changed"))
                     (assoc-in request [:macro-expansion-binding :source-revision-id] (sha256-text "changed"))
                     (assoc-in request [:macro-expansion-binding :macro-result-id] (sha256-text "changed"))]]
      (is (nil? (recover source expanded changed diagnostic))))
    (doseq [changed [(assoc expanded :expanded-syntax-stream [])
                     (assoc expanded :expanded-syntax-stream [owner owner])
                     (assoc expanded :expanded-syntax-stream [(assoc owner :syntax/id (sha256-text "changed"))])
                     (assoc expanded :expanded-syntax-stream [(assoc-in owner [:span :byte-end] 41)])
                     (assoc expanded :expanded-syntax-stream [(assoc owner :origin [])])
                     (assoc expanded :expanded-syntax-stream [(assoc owner :origin [{:kind :source :span span}])])]]
      (is (nil? (recover source changed request diagnostic)))
      (is (identical? error (transport changed request error))))
    ;; Even a FIRST generated reference's exact display coordinates are not
    ;; causal evidence for a dependency-cycle/import/binding rejection.
    (doseq [[id reason] [["C5-CYCLE" :namespace-dependency-cycle]
                         ["C5-UNRESOLVED" :malformed-authoritative-record]
                         ["C5-UNRESOLVED" :missing-module-artifact]
                         ["C5-CAPABILITY" :missing-import-capability]]]
      (let [ordinary (-> diagnostic (assoc :id id)
                         (assoc-in [:facts :reason] reason)
                         (assoc-in [:facts :rule-specific] {:reason reason :dependency-edges []}))
            problem (ex-info "ordinary" ordinary)]
        (is (nil? (recover source expanded request ordinary)))
        (is (identical? problem (transport expanded request problem)))
        (is (= ordinary (ex-data (transport expanded request problem))))))))

(deftest sh06-generated-unresolved-origin-composition
  ;; Select only the changed rejection path after a diagnostic transport fix;
  ;; unchanged positive C5/C6 products need not be reconstructed for this check.
  (let [source (fixture-path "composition" "generated-unresolved" ".gravity")
        expanded (bootstrap/sh05-macro-file-artifact source)
        failure (try
                  (bootstrap/sh06-resolution-artifact-from-sh05-artifact source expanded)
                  {:unexpected-success true}
                  (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= :accepted (:status expanded)))
    (is (= "C5-UNRESOLVED" (:id failure)) (pr-str failure))
    (is (= 'missing-generated-value (:symbol failure)))
    (is (= source (get-in failure [:source-span :source])))
    ;; L12 requires generation provenance when generated code fails checking.
    (is (seq (:origin-chain failure)) (pr-str failure))
    (is (= (:origin (second (:expanded-syntax-stream expanded)))
           (:origin-chain failure)))
    (is (nil? (:unexpected-success failure)))))

(deftest sh06-source-module-cycle-composition
  ;; A0 is an earlier immutable dependency of B, not an accepted A1 artifact.
  ;; This checks a supplied authenticated logical namespace graph; it does not
  ;; construct mutually accepted cyclic artifacts or select package revisions.
  (doseq [name ["cycle-a0" "cycle-b" "cycle-a1"]]
    (is (= (slurp (fixture-path "composition" name ".gravity"))
           (slurp (fixture-path "composition" name ".qst")))))
  (let [a0-path (fixture-path "composition" "cycle-a0" ".gravity")
        b-path (fixture-path "composition" "cycle-b" ".gravity")
        a1-path (fixture-path "composition" "cycle-a1" ".gravity")
        inputs (fn [artifact] {:artifact :gravity/sh06-module-inputs
                               :schema-version 1 :module-artifacts [artifact]})
        a0 (bootstrap/compiler-c5-resolution-file-artifact a0-path)
        b (bootstrap/compiler-c5-resolution-file-artifact b-path (inputs a0))
        retained (get-in b [:gravity-resolution-boundary :module-inputs :module-artifacts 0])
        alias (first (get-in b [:alias-table :aliases]))
        imported (first (filter #(and (= :import (:binding-class %)) (= 'identity (:name %)))
                                (get-in b [:binding-table :bindings])))
        a0-definition (first (filter #(and (= :namespace (:binding-class %)) (= 'identity (:name %)))
                                     (get-in a0 [:binding-table :bindings])))
        failure (try
                  (bootstrap/compiler-c5-resolution-file-artifact a1-path (inputs b))
                  {:unexpected-success true}
                  (catch clojure.lang.ExceptionInfo e (ex-data e)))]
    (is (= :accepted (:status a0) (:status b)))
    (is (= :passed (:status (verification a0)) (:status (verification b))))
    (is (= a0 retained))
    (is (= a0-path (get-in retained [:provenance :source-path])))
    (is (not= a0-path a1-path))
    (is (= (:artifact-id a0) (:dependency-artifact-id alias) (:definition-artifact-id imported)))
    (is (= (:definition-syntax-id a0-definition) (:definition-syntax-id imported)))
    (is (= (:binding-id a0-definition) (get-in imported [:semantic-span :origin-binding-id])))
    (is (some #(and (= 'example.cycle.b (:from %)) (= 'example.cycle.a (:to %)))
              (get-in b [:dependency-graph :edges])))
    (is (= "C5-CYCLE" (:id failure)) (pr-str failure))
    (is (= :namespace-dependency-cycle (get-in failure [:facts :reason])) (pr-str failure))
    (is (= 'example.cycle.a (:namespace failure)))
    (is (= a1-path (get-in failure [:source-span :source])))
    (is (= #{['example.cycle.a 'example.cycle.b] ['example.cycle.b 'example.cycle.a]}
           (set (map (juxt :from :to) (get-in failure [:facts :rule-specific :dependency-edges])))))
    ;; The rejection is graph-level, even if the producer used its first
    ;; generated reference as a diagnostic location. No causal macro is known.
    (is (empty? (:origin-chain failure)))
    (is (nil? (:unexpected-success failure)))))

(deftest sh06-b-source-referred-ambiguity-composition
  ;; C5 must reject the actual two exported origins, not an invented premise
  ;; or an earlier alias/privacy failure. This does not construct downstream IR.
  (doseq [name ["b-referred-left" "b-referred-right" "b-referred-ambiguous"]]
    (is (= (slurp (fixture-path "composition" name ".gravity"))
           (slurp (fixture-path "composition" name ".qst")))))
  (let [libraries
        (mapv #(bootstrap/compiler-c5-resolution-file-artifact
                (fixture-path "composition" % ".gravity"))
              ["b-referred-left" "b-referred-right"])
        definitions
        (mapv (fn [artifact]
                (first (filter #(and (= :namespace (:binding-class %))
                                     (= 'b-shared-value (:name %)))
                               (get-in artifact [:binding-table :bindings]))))
              libraries)
        source (fixture-path "composition" "b-referred-ambiguous" ".gravity")
        failure (try
                  (bootstrap/compiler-c5-resolution-file-artifact
                   source {:artifact :gravity/sh06-module-inputs
                           :schema-version 1 :module-artifacts libraries})
                  {:unexpected-success true}
                  (catch clojure.lang.ExceptionInfo error (ex-data error)))
        candidates (:candidate-bindings failure)]
    (doseq [artifact libraries]
      (is (= :accepted (:status artifact)))
      (is (= :passed (:status (verification artifact)))))
    (is (every? #(and (= :public (:visibility %))
                     (= 'b-shared-value (:name %))) definitions))
    (is (= "C5-AMBIGUOUS" (:id failure)))
    (is (= :name-resolution (:stage failure)))
    (is (= :multiple-legal-bindings (get-in failure [:facts :reason])))
    (is (= 2 (get-in failure [:facts :rule-specific :candidate-count])
           (count candidates)))
    (is (= 'b-shared-value (:symbol failure)))
    (is (= source (get-in failure [:source-span :source])))
    (is (sha256-id? (:syntax-id failure)))
    (is (= #{'example.b-referred-left 'example.b-referred-right}
           (set (map :namespace candidates))))
    (is (every? #(and (= :import (:binding-class %))
                     (= :public (:visibility %))
                     (not= 'gravity.core (:namespace %))) candidates))
    (doseq [[artifact definition] (map vector libraries definitions)]
      (let [matches (filter #(= (:artifact-id artifact)
                                (:definition-artifact-id %)) candidates)
            candidate (first matches)]
        (is (= 1 (count matches)))
        (is (= (:binding-id definition)
               (get-in candidate [:semantic-span :origin-binding-id])))
        (is (= (:definition-syntax-id definition)
               (:definition-syntax-id candidate)
               (get-in candidate [:semantic-span :origin-definition-syntax-id])))
        (is (= (:definition-artifact-id definition)
               (get-in candidate [:semantic-span :origin-definition-artifact-id])))
        (is (= (:artifact-id artifact)
               (get-in candidate [:semantic-span :ordinary-module-artifact-id])))))
    (is (nil? (:unexpected-success failure)))
    (is (not (contains? failure :core-ast-module)))
    (is (not (contains? failure :c5-name-resolution-artifact)))))

(deftest sh06-ordinary-seed-build-does-not-call-public-frontdoors
  (let [frontdoors (mapv required-var '[compiler-c4-macro-source-artifact compiler-c5-resolution-source-artifact compiler-c6-lowering-source-artifact])
        forbidden (fn [& _] (throw (ex-info "Seed compilation reached a public frontdoor" {:id "SH06-SEED-FRONTDOOR"})))
        binding (with-redefs-fn (zipmap frontdoors (repeat forbidden))
                  #(bootstrap/sh06-resolution-build-binding! "<seed-frontdoor-kernel>"))]
    (is (= :clojure-stage0-seed (:compiled-by binding)))
    (is (= :development-unadmitted (:revision-status binding)))
    (is (false? (:self-hosted? binding)))
    (is (= (:source-content-hash binding) (sha256-text (:source-text binding))))))

(deftest sh06-atomic-signature-type-namespace-development-kernel
  (let [binding (bootstrap/sh06-resolution-current-binding! "<type-namespace-kernel>")
        plan (:plan binding)
        invoke #(bootstrap/sh06-resolution-execute! "<type-namespace-kernel>" binding %1 %2)
        core (bootstrap/sh06-resolution-core-record 'I64 :type 0)
        variable (assoc core :kind :var :namespace 'demo.types :binding-class :namespace
                            :definition-syntax-id "var-I64")
        var-only (assoc variable :name 'OnlyVar)
        local {:name 'I64 :kind :local}
        local-binding {:name 'I64 :kind :local :binding-class :lexical :scope-id :scope :binding-id :local}
        bindings [(assoc core :binding-id :core) (assoc variable :binding-id :var)
                  (assoc var-only :binding-id :var-only) local-binding]
        request {:module {:namespace 'demo.types} :definitions [variable var-only]
                 :core-bindings [core] :imports [] :import-bindings []
                 :lexical-scopes [{:scope-id :scope :bindings [local]}]}
        reference {:position :type :qualifier nil :symbol 'I64 :name 'I64 :scope-chain [:scope]}]
    (is (= :development-unadmitted (:revision-status binding)))
    (is (false? (:self-hosted? binding)))
    (is (identical? binding (bootstrap/sh06-resolution-current-binding! "<same-input>")))
    (is (= (:source-content-hash binding) (get-in plan [:source :sha256])))
    (is (= [:core] (mapv :binding-id (invoke 'sh06-reference-candidates [request bindings reference]))))
    (is (= [:local] (mapv :binding-id (invoke 'sh06-reference-candidates
                                            [request bindings (assoc reference :position :expression)]))))
    (is (= [] (invoke 'sh06-reference-candidates [request bindings (assoc reference :symbol 'OnlyVar :name 'OnlyVar)])))
    (is (= [:core] (mapv :binding-id (invoke 'sh06-reference-candidates
                                           [request bindings (assoc reference :qualifier 'gravity.core :symbol 'gravity.core/I64)]))))
    (is (thrown? clojure.lang.ExceptionInfo (bootstrap/sh06-resolution-read-pinned-source! "<historical-boundary>")))
    (let [key-var (private-bootstrap-var 'sh06-resolution-current-source-key)
          captured-var (private-bootstrap-var '*sh06-resolution-operation-binding*)
          original-key (var-get key-var)]
      (with-redefs-fn {key-var #(assoc (original-key) :source [:changed :changed])}
        #(with-bindings {captured-var binding}
           (is (thrown? clojure.lang.ExceptionInfo
                        (bootstrap/sh06-resolution-current-binding! "<operation-drift>"))))))))

(deftest sh06-qualified-core-var-development-kernel
  (let [binding (bootstrap/sh06-resolution-current-binding! "<qualified-core-kernel>")
        invoke #(bootstrap/sh06-resolution-execute! "<qualified-core-kernel>" binding %1 %2)
        core (bootstrap/sh06-resolution-core-record 'count :var 0)
        reference {:syntax-id (sha256-text "qualified-count") :symbol 'gravity.core/count
                   :qualifier 'gravity.core :name 'count :position :operator :scope-chain []
                   :semantic-span {:ordinal 0} :source-span {:source "<qualified-core-kernel>"}}
        request (assoc (module-policy-input 'demo.qualified [] [] [] [reference]) :core-bindings [core])
        candidates (fn [request reference]
                     (let [records (vec (concat (:definitions request) (:core-bindings request) (:import-bindings request)))
                           bindings (mapv #(assoc %1 :binding-id %2) records (range))]
                       (invoke 'sh06-reference-candidates [request bindings reference])))
        check (fn [request reference]
                (let [records (vec (concat (:definitions request) (:core-bindings request) (:import-bindings request)))
                      bindings (mapv #(assoc %1 :binding-id %2) records (range))
                      result (invoke 'sh06-reference-check [request bindings reference])]
                  (is (= (:rule result) (invoke 'sh06-reference-rule [request bindings reference])))
                  result))
        reject (fn [request reference rule reason]
                 (let [result (check request reference)]
                   (is (= rule (:rule result)))
                   (is (= reason (get-in result [:facts :reason])))
                   result))]
    (doseq [position [:operator :expression]]
      (let [reference (assoc reference :position position)
            unqualified (assoc reference :symbol 'count :qualifier nil)]
        (is (= [0] (mapv :binding-id (candidates request reference))))
        (is (= (candidates request reference) (candidates request unqualified)))
        (is (nil? (check request reference)))
        (is (nil? (check request unqualified)))))
    (let [local {:name 'count :kind :local}
          request (assoc request :lexical-scopes [{:scope-id :scope :bindings [local]}])
          bindings [(assoc core :binding-id :core)
                    (assoc local :scope-id :scope :binding-class :lexical :binding-id :local)]]
      (is (= [:local] (mapv :binding-id (invoke 'sh06-reference-candidates
                                       [request bindings (assoc reference :qualifier nil :scope-chain [:scope])]))))
      (is (= [:core] (mapv :binding-id (invoke 'sh06-reference-candidates
                                      [request bindings (assoc reference :scope-chain [:scope])])))))
    (reject request (assoc reference :name 'missing :symbol 'gravity.core/missing)
            "C5-UNRESOLVED" :no-legal-binding)
    (reject request (assoc reference :qualifier 'other.core)
            "C5-ALIAS" :unknown-namespace-alias)
    (doseq [position [:macro :declaration nil]]
      (reject request (assoc reference :position position) "C5-UNRESOLVED" :no-legal-binding))
    (doseq [[kind position] [[:special-form :operator] [:macro :operator] [:type :expression]]]
      (let [changed (assoc request :core-bindings [(assoc core :kind kind)])]
        (reject changed (assoc reference :position position) "C5-UNRESOLVED" :no-legal-binding)))
    (doseq [changed-core [(assoc core :namespace 'other.core) (assoc core :binding-class :import)]]
      (reject (assoc request :core-bindings [changed-core]) reference "C5-UNRESOLVED" :no-legal-binding))
    (doseq [owner [{:imports [{:alias 'gravity.core :namespace 'owned.library}]}
                  {:imports [{:alias 'owned :namespace 'gravity.core}]}
                  {:definitions [(assoc core :name 'another :kind :function :binding-class :namespace)]}
                  {:module (assoc (:module request) :namespace 'gravity.core)}]]
      (reject (merge request owner) reference "C5-UNRESOLVED" :no-legal-binding))
    (let [private-import (assoc core :binding-class :import :namespace 'owned.library :visibility :private)
          owned (assoc request :imports [{:alias 'gravity.core :namespace 'owned.library}]
                               :import-bindings [private-import])]
      (is (= :import (:binding-class (first (candidates owned reference)))))
      (reject owned reference "C5-PRIVATE" :private-binding-access))
    (let [private-core (assoc core :visibility :private :profile-set nil :target-set nil)]
      (reject (assoc request :core-bindings [private-core]) reference "C5-PRIVATE" :private-binding-access)
      (reject (assoc request :core-bindings [core private-core]) reference "C5-AMBIGUOUS" :multiple-legal-bindings))
    (doseq [kind [:var :type :special-form]
            qualified? [false true]
            :when (or (= kind :var) (= kind :type) (not qualified?))]
      (let [reference (assoc reference :qualifier (when qualified? 'gravity.core)
                                       :position (if (= kind :type) :type :operator))
            request (assoc request :core-bindings [(assoc core :kind kind)])]
        (is (nil? (check request reference)))
        (is (nil? (check (assoc-in request [:core-bindings 0 :target-set] [:all]) reference)))
        (is (nil? (check (assoc-in request [:core-bindings 0 :target-set] [:jvm]) reference)))
        (doseq [[key value] [[:profile-set nil] [:profile-set :meta] [:profile-set '(:meta)]
                            [:profile-set [[:meta]]] [:profile-set [nil]] [:profile-set ["meta"]]
                            [:target-set nil] [:target-set :all] [:target-set #{:all}]
                            [:target-set [[:jvm]]] [:target-set ["jvm"]]]]
          (reject (assoc-in request [:core-bindings 0 key] value) reference
                  "C5-UNRESOLVED" :malformed-core-binding-availability))
        (doseq [targets [[] [:llvm]]]
          (reject (assoc-in request [:core-bindings 0 :target-set] targets) reference
                  "C5-TARGET" :core-binding-target-excluded))
        (doseq [profiles [[] [:core] [:native]]]
          (reject (assoc-in request [:core-bindings 0 :profile-set] profiles) reference
                  "C5-CROSS-PROFILE" :core-binding-profile-excluded))
        (reject (-> request (assoc-in [:core-bindings 0 :profile-set] [:core])
                            (assoc-in [:core-bindings 0 :target-set] [:llvm])) reference
                "C5-TARGET" :core-binding-target-excluded)))
    (let [second-reference (assoc reference :qualifier nil :symbol 'count :syntax-id (sha256-text "plain-count"))
          request (assoc request :references [reference second-reference])
          result (bootstrap/sh06-resolution-run-request! "<qualified-core-kernel>" binding request)
          analysis (:resolved-analysis result)
          rows (:resolution-table analysis)
          selected (first (filter #(= 'count (:name %)) (:binding-table analysis)))]
      (is (= 2 (count rows)))
      (is (apply = (map :binding-id rows)))
      (is (= 2 (count (set (map :syntax-id rows)))))
      (is (= [:fully-qualified-namespace-binding :profile-allowed-core-binding] (mapv :resolution-order rows)))
      (is (= (dissoc core :source-span) (dissoc selected :source-span :binding-id :artifact)))
      (is (= :passed (:status (invoke 'sh06-verify-resolution-resolved
                                    [analysis (:digest-requests result) (:resolved-digests result)]))))
      (doseq [[key value] [[:profile-set [:core]] [:target-set [:llvm]]
                          [:effects [:io/write]] [:capabilities [:io/stdout]]
                          [:binding-class :import] [:visibility :private]]]
        (is (= :failed (:status (invoke 'sh06-verify-resolution-resolved
                                       [(assoc-in analysis [:binding-table 0 key] value)
                                        (:digest-requests result) (:resolved-digests result)]))))))))

(deftest sh06-list-parameter-type-projection-development-kernel
  (let [references (atom [])
        project (var-get (private-bootstrap-var 'sh06-resolution-analysis-inputs))
        walk (fn [form]
               (let [result (project {} {:expanded-syntax-stream
                                         [{:form form :syntax/id :function
                                           :span {:source "list-parameter-projection.gravity"}}]} {})]
                 (reset! references (mapv (juxt :symbol :position) (:references result)))
                 result))]
    (let [result (walk '(fn [parts :- (gravity.core/List String) text :- String] :- String parts))]
      (is (= '[parts text] (mapv :name (:bindings (first (:lexical-scopes result)))))))
    (is (= '[[fn :operator] [gravity.core/List :type] [String :type] [String :type]
             [String :type] [parts :expression]] @references))
    (reset! references [])
    (walk '(fn [parts :- (UnknownConstructor MissingElement)] parts))
    (is (= '[[fn :operator] [UnknownConstructor :type] [MissingElement :type] [parts :expression]] @references))
    (reset! references [])
    (walk '(fn [parts] :- (List String) parts))
    (is (= '[[fn :operator] [List :type] [String :type] [parts :expression]] @references))
    (doseq [form ['(fn named [parts :- (List String)] :- (gravity.core/List gravity.core/String) parts)
                  '(fn [parts] :- (UnknownConstructor MissingElement) parts)]]
      (let [result (walk form)]
        (is (= (if (= 'named (second form))
                 '[[fn :operator] [List :type] [String :type] [gravity.core/List :type] [gravity.core/String :type] [parts :expression]]
                 '[[fn :operator] [UnknownConstructor :type] [MissingElement :type] [parts :expression]]) @references))
        (is (every? #(= (:scope-id (first (:lexical-scopes result))) (first (:scope-chain %)))
                    (filter #(= :type (:position %)) (:references result))))))
    (reset! references [])
    (walk '(fn [parts :- (List (List String))] parts))
    (is (= '[[fn :operator] [parts :expression]] @references))
    (doseq [form ['(fn [parts] :- (List) parts) '(fn [parts] :- (List String Bool) parts)
                  '(fn [parts] :- (List (List String)) parts)]]
      (walk form)
      (is (= '[[fn :operator] [parts :expression]] @references)))))

(deftest sh06-atomic-signature-source-type-rejections
  (doseq [[basename symbol] [["function-annotation-unknown" 'MissingType]
                             ["function-annotation-var-only" 'OnlyVar]]]
    (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" basename)
          source-path (path (str relative ".gravity"))
          data (rejection-data source-path)]
      (is (nil? (:raw-host-error data)) (pr-str data))
      (is (= "C5-UNRESOLVED" (:id data)))
      (is (= :name-resolution (:stage data)))
      (is (= symbol (:symbol data)))
      (is (= source-path (get-in data [:source-span :source])))
      (is (string? (:syntax-id data)))
      (is (seq (:remediation data)))
      (is (= (slurp source-path) (slurp (path (str relative ".qst"))))))))

(deftest sh06-fixture-inventory-is-co-canonical-and-behavioral
  (testing "the checked-in inventory is explicit and symmetric"
    (is (= accepted-fixtures (fixture-basenames "accepted" ".gravity")))
    (is (= accepted-fixtures (fixture-basenames "accepted" ".qst")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".gravity")))
    (is (= rejected-fixtures (fixture-basenames "rejected" ".qst"))))
  (testing "every pair is byte-identical and traverses the Gravity reader"
    (doseq [[family basenames]
            [["accepted" accepted-fixtures] ["rejected" rejected-fixtures]]
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
  (testing "executable requests are generic and contain no oracle fields"
    (doseq [basename (sort rejected-fixtures)]
      (let [gravity-request (rejection-request basename ".gravity")
            qst-request (rejection-request basename ".qst")
            gravity-oracle (rejection-oracle basename ".gravity")]
        (is (map? gravity-request) basename)
        (is (= 1 (:request-version gravity-request)) basename)
        (is (= gravity-request qst-request) basename)
        (is (not (contains? gravity-request :scenario)) basename)
        (is (not (contains-expected-key? gravity-request)) basename)
        (is (= gravity-oracle
               (rejection-oracle basename ".qst")) basename))))
  (is (= c5-rules
         (set (map #(get-in (rejection-oracle % ".gravity")
                            [:expected-rule])
                   rejected-fixtures)))))

(deftest sh06-coordinator-adapter-api-is-explicit
  (let [source-var (required-var 'sh06-resolution-source-artifact)
        file-var (required-var 'sh06-resolution-file-artifact)
        construction-seams
        ['sh06-resolution-artifact-verification-bounded*
         'sh06-resolution-artifact-verification*
         'sh06-resolution-artifact-verification-contained
         'sh06-resolution-capability-based-proof-for-construction]]
    (is (= '([source-path source-text] [source-path source-text module-inputs]) (:arglists (meta source-var))))
    (is (= '([source-path] [source-path module-inputs]) (:arglists (meta file-var))))
    (doseq [symbol construction-seams]
      (is (true? (:private (meta (private-bootstrap-var symbol))))
          (str symbol " must remain coordinator-private")))))

(deftest sh06-resolution-order-is-real-stable-and-c4-bound
  (let [source-path (fixture-path "accepted" "resolution-order" ".gravity")
        first-artifact (sh06-file-artifact source-path)
        second-artifact (sh06-file-artifact source-path)
        orders (resolution-orders first-artifact)]
    (is (= first-artifact second-artifact))
    (is (= :gravity/sh06-resolution-artifact (:kind first-artifact)))
    (is (= :accepted (:status first-artifact)))
    (is (sha256-id? (:artifact-id first-artifact)))
    (is (= :passed (:status (verification first-artifact))))
    (is (= :complete (:status (capability-proof first-artifact))))
    (is (every? orders [:local-lexical-binding
                        :current-namespace-binding
                        :alias-qualified-required-binding
                        :fully-qualified-namespace-binding]))
    (is (contains? orders :profile-allowed-core-binding))
    (is (contains-value? first-artifact
                         :gravity/sh05-macro-expansion-artifact))
    (is (= source-path (get-in first-artifact [:provenance :source-path])))))

(deftest sh06-accepted-fixtures-emit-required-resolution-products
  (doseq [basename (sort accepted-fixtures)
          extension [".gravity" ".qst"]]
    (let [artifact (sh06-file-artifact
                    (fixture-path "accepted" basename extension))]
      (testing (str basename extension)
        (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
        (is (= :accepted (:status artifact)))
        (is (sha256-id? (:artifact-id artifact)))
        (is (contains-value? artifact
                             :gravity/sh06-namespace-analysis-artifact))
        (let [analysis (:namespace-analysis artifact)]
          (doseq [product [:alias-table :binding-table :resolution-table
                           :lexical-scope-graph :dependency-graph
                           :cross-profile-edge-report
                           :incremental-invalidation-inputs]]
            (is (contains? analysis product) (name product))))
        (is (= :passed (:status (verification artifact))))
        (is (= :complete (:status (capability-proof artifact))))))))

(deftest sh06-module-foreign-and-compiler-cases-resolve-through-generic-rules
  (let [module-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "module-boundaries" ".gravity"))
        foreign-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "foreign-explicit" ".gravity"))
        compiler-artifact
        (sh06-file-artifact
         (fixture-path "accepted" "compiler-subset" ".gravity"))
        module-analysis (:namespace-analysis module-artifact)
        foreign-analysis (:namespace-analysis foreign-artifact)
        compiler-analysis (:namespace-analysis compiler-artifact)]
    (is (seq (:dependency-records module-analysis)))
    (is (every? :accepted
                (:cross-profile-edge-report module-analysis)))
    (is (some #(= :pure-core-api (:profile-boundary %))
              (:dependency-records module-analysis)))
    (is (contains? (resolution-orders foreign-artifact)
                   :explicit-foreign-import-binding))
    (is (some #(= :foreign (:kind %)) (:alias-table foreign-analysis)))
    (is (seq (:lexical-scope-graph compiler-analysis)))
    (is (some #(= :local-lexical-binding %)
              (resolution-orders compiler-artifact)))
    (is (empty? (:diagnostics module-analysis)))
    (is (empty? (:diagnostics foreign-analysis)))
    (is (empty? (:diagnostics compiler-analysis)))))

(deftest sh06-identities-are-extension-checkout-and-cwd-neutral
  (let [gravity-path (fixture-path "accepted" "module-boundaries" ".gravity")
        qst-path (fixture-path "accepted" "module-boundaries" ".qst")
        gravity-artifact (sh06-file-artifact gravity-path)
        qst-artifact (sh06-file-artifact qst-path)
        root-a (java.nio.file.Files/createTempDirectory
                "gravity-sh06-checkout-a-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        root-b (java.nio.file.Files/createTempDirectory
                "gravity-sh06-checkout-b-"
                (make-array java.nio.file.attribute.FileAttribute 0))
        unrelated-cwd (java.nio.file.Files/createTempDirectory
                       "gravity-sh06-cwd-"
                       (make-array java.nio.file.attribute.FileAttribute 0))
        path-a (str (.resolve root-a "src/module-boundaries.gravity"))
        path-b (str (.resolve root-b "src/module-boundaries.qst"))
        original-user-dir (System/getProperty "user.dir")]
    (try
      (copy-source! gravity-path path-a)
      (copy-source! qst-path path-b)
      (System/setProperty "user.dir" (str unrelated-cwd))
      (let [artifact-a (sh06-file-artifact path-a)
            artifact-b (sh06-file-artifact path-b)]
        (is (= (:artifact-id gravity-artifact) (:artifact-id qst-artifact)))
        (is (= (semantic-identities gravity-artifact)
               (semantic-identities qst-artifact)))
        (is (= (:artifact-id artifact-a) (:artifact-id artifact-b)))
        (is (= (semantic-identities artifact-a)
               (semantic-identities artifact-b)))
        (is (= path-a (get-in artifact-a [:provenance :source-path])))
        (is (= path-b (get-in artifact-b [:provenance :source-path])))
        (is (not= (get-in artifact-a [:provenance :source-path])
                  (get-in artifact-b [:provenance :source-path]))))
      (finally
        (System/setProperty "user.dir" original-user-dir)
        (doseq [tree [root-a root-b unrelated-cwd]]
          (delete-tree! (.toFile tree)))))))

(deftest sh06-normalized-policy-rejections-are-structured-pathful-and-rule-exact
  (doseq [basename (sort rejected-fixtures)
          extension [".gravity" ".qst"]]
    (let [source-path (fixture-path "rejected" basename extension)
          oracle (rejection-oracle basename extension)
          data (normalized-policy-rejection source-path (slurp source-path))]
      (testing (str basename extension)
        (is (nil? (:raw-host-error data)))
        (is (nil? (:unexpected-success data)))
        (is (= (:expected-rule oracle) (:id data)))
        (is (= (:expected-stage oracle) (:stage data)))
        (is (= (:expected-severity oracle) (:severity data)))
        (is (= :c5-name-resolution (:diagnostic-family data)))
        (is (= source-path (get-in data [:source-span :source])))
        (is (contains? data :symbol))
        (is (contains? data :syntax-id))
        (is (contains? data :namespace))
        (is (contains? data :profile))
        (is (contains? data :target))
        (is (contains? data :candidate-bindings))
        (is (contains? data :dependency-edge))
        (is (seq (:remediation data)))))))

(deftest sh06-normalized-policy-does-not-consume-scenario-labels-or-oracles
  (doseq [basename ["unresolved" "ambiguous" "cycle"]]
    (let [source-path (fixture-path "rejected" basename ".gravity")
          source-text (slurp source-path)
          expected (:expected-rule (rejection-oracle basename ".gravity"))
          with-ignored-scenario
          (str/replace-first source-text
                             "{:request-version 1"
                             "{:request-version 1 :scenario :ignored")
          with-changed-oracle
          (str/replace source-text expected "C5-FOREIGN")
          baseline (normalized-policy-rejection source-path source-text)
          scenario-result
          (normalized-policy-rejection source-path with-ignored-scenario)
          oracle-result
          (normalized-policy-rejection source-path with-changed-oracle)]
      (is (not= source-text with-ignored-scenario) basename)
      (is (not= source-text with-changed-oracle) basename)
      (is (= expected (:id baseline)) basename)
      (is (= expected (:id scenario-result)) basename)
      (is (= expected (:id oracle-result)) basename)
      (is (nil? (:raw-host-error scenario-result)) basename)
      (is (nil? (:raw-host-error oracle-result)) basename))))

(deftest sh06-oracle-metadata-is-excluded-from-executable-request
  (let [source-path (fixture-path "accepted" "resolution-order" ".gravity")
        source-text (slurp source-path)
        with-oracle
        (str/replace-first
         source-text
         "{:slice :SH-06 :fixture :resolution-order}"
         "{:slice :SH-06 :fixture :resolution-order :sh06 {:expected-rule \"C5-FOREIGN\"}}")
        baseline (sh06-source-artifact source-path source-text)
        oracle-bearing (sh06-source-artifact source-path with-oracle)
        request (get-in oracle-bearing
                        [:gravity-resolution-boundary
                         :authenticated-resolution-request])]
    (is (not= source-text with-oracle))
    (is (not= (:artifact-id baseline) (:artifact-id oracle-bearing)))
    (is (not= (get-in baseline [:provenance :source-revision-id])
              (get-in oracle-bearing [:provenance :source-revision-id])))
    (is (= (mapv #(select-keys % [:symbol :position :resolution-order])
                 (get-in baseline [:namespace-analysis :resolution-table]))
           (mapv #(select-keys % [:symbol :position :resolution-order])
                 (get-in oracle-bearing
                         [:namespace-analysis :resolution-table]))))
    (is (map? request))
    (is (not (contains-value? request :sh06)))
    (is (not (contains-expected-key? request)))))

(deftest sh06-normalized-policy-input-substitution-changes-resolution-behavior
  (let [ambiguous-path (fixture-path "rejected" "ambiguous" ".gravity")
        ambiguous-source (slurp ambiguous-path)
        single-candidate
        (str/replace
         ambiguous-source
         "{:name shared :visibility :public}]"
         "]")
        cycle-path (fixture-path "rejected" "cycle" ".gravity")
        cycle-source (slurp cycle-path)
        acyclic
        (str/replace
         cycle-source
         ":from self-hosting.sh06.reject.cycle-peer\n                                                   :to self-hosting.sh06.reject.cycle}"
         ":from self-hosting.sh06.reject.cycle-peer\n                                                   :to terminal.module}")
        ambiguous-result
        (normalized-policy-rejection ambiguous-path single-candidate)
        cycle-result (normalized-policy-rejection cycle-path acyclic)]
    (is (not= ambiguous-source single-candidate))
    (is (not= cycle-source acyclic))
    (is (not= "C5-AMBIGUOUS" (:id ambiguous-result)))
    (is (not= "C5-CYCLE" (:id cycle-result)))
    (is (nil? (:raw-host-error ambiguous-result)))
    (is (nil? (:raw-host-error cycle-result)))))

(deftest sh06-qualified-reference-misspelling-fails-closed
  (let [source-path (fixture-path "accepted" "module-boundaries" ".gravity")
        source-text (slurp source-path)
        misspelled (str/replace source-text "(io/write" "(io/wriet")
        result (rejection-data-for-source source-path misspelled)]
    (is (not= source-text misspelled))
    (is (= "C5-UNRESOLVED" (:id result)))
    (is (= 'io/wriet (:symbol result)))
    (is (= [] (:candidate-bindings result)))
    (is (nil? (:raw-host-error result)))
    (is (nil? (:unexpected-success result)))))

(deftest sh06-same-source-qualified-private-definitions-resolve-exactly
  (let [module-name 'self-hosting.sh06.same-source-qualified
        effective-namespace 'helper.private
        private-symbol 'helper.private/private-member
        missing-member-symbol 'helper.private/missing-member
        absent-qualifier-symbol 'missing.qualifier/private-member
        unqualified-symbol 'private-member
        source-for-reference
        (fn [reference]
          (str
           "(ns self-hosting.sh06.same-source-qualified\n"
           "  (:profile :hosted) (:target :jvm)\n"
           "  (:exports [read-private])\n"
           "  (:effects #{}) (:capabilities #{}) (:safety :safe)\n"
           "  (:metadata {:slice :SH-06}))\n"
           "(def helper.private/private-member 41)\n"
           "(defn read-private []\n"
           "  " reference ")\n"))
        accepted-source
        (source-for-reference private-symbol)
        source-paths
        {".gravity" "synthetic/sh06-same-source-qualified.gravity"
         ".qst" "synthetic/sh06-same-source-qualified.qst"}
        artifacts
        (into {}
              (map (fn [[extension source-path]]
                     [extension
                      (sh06-source-artifact source-path accepted-source)]))
              source-paths)
        repeated
        (sh06-source-artifact (get source-paths ".gravity")
                              accepted-source)
        gravity-artifact (get artifacts ".gravity")
        qst-artifact (get artifacts ".qst")
        resolution
        (some #(when (= private-symbol (:symbol %)) %)
              (get-in gravity-artifact
                      [:namespace-analysis :resolution-table]))
        binding
        (some #(when (= (:binding-id resolution) (:binding-id %)) %)
              (get-in gravity-artifact
                      [:namespace-analysis :binding-table]))
        missing-member-source (source-for-reference missing-member-symbol)
        absent-qualifier-source (source-for-reference absent-qualifier-symbol)
        unqualified-source (source-for-reference unqualified-symbol)]
    (testing "a genuinely qualified private declaration resolves by its effective namespace"
      (doseq [[extension artifact] artifacts]
        (is (= :gravity/sh06-resolution-artifact (:kind artifact)) extension)
        (is (= :accepted (:status artifact)) extension)
        (is (= :passed (:status (verification artifact))) extension)
        (is (= :complete (:status (capability-proof artifact))) extension))
      (is (= :fully-qualified-namespace-binding
             (:resolution-order resolution)))
      (is (= private-symbol (:symbol resolution)))
      (is (not= module-name effective-namespace))
      (is (= effective-namespace (:namespace binding)))
      (is (= :private (:visibility binding)))
      (is (= :namespace (:binding-class binding)))
      (is (sha256-id? (:binding-id resolution)))
      (is (= (:artifact-id (:sh05-macro-artifact gravity-artifact))
             (:definition-artifact-id binding)))
      (is (= (get source-paths ".gravity")
             (get-in gravity-artifact
                     [:gravity-resolution-boundary
                      :authenticated-resolution-request
                      :provenance :actual-source-path])))
      (is (contains?
           (set (map :syntax/id
                     (get-in gravity-artifact
                             [:sh05-macro-artifact
                              :expanded-syntax-stream])))
           (:definition-syntax-id binding))))
    (testing "artifact identity is deterministic and extension-neutral"
      (is (= (:artifact-id gravity-artifact)
             (:artifact-id qst-artifact)
             (:artifact-id repeated)))
      (is (= (semantic-identities gravity-artifact)
             (semantic-identities qst-artifact)
             (semantic-identities repeated))))
    (testing "a known same-source qualifier with an absent member is unresolved"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path missing-member-source)]
          (is (= "C5-UNRESOLVED" (:id result)) extension)
          (is (= missing-member-symbol (:symbol result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))
    (testing "the qualified declaration does not leak into unqualified lookup"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path unqualified-source)]
          (is (= "C5-UNRESOLVED" (:id result)) extension)
          (is (= unqualified-symbol (:symbol result)) extension)
          (is (= [] (:candidate-bindings result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))
    (testing "an absent qualifier is an alias error"
      (doseq [[extension source-path] source-paths]
        (let [result
              (rejection-data-for-source source-path absent-qualifier-source)]
          (is (= "C5-ALIAS" (:id result)) extension)
          (is (= absent-qualifier-symbol (:symbol result)) extension)
          (is (= source-path (get-in result [:source-span :source])) extension)
          (is (nil? (:raw-host-error result)) extension)
          (is (nil? (:unexpected-success result)) extension))))))

(deftest sh06-let-initializers-use-the-sequential-outer-environment
  (let [source-path (fixture-path "accepted" "compiler-subset" ".gravity")
        source-text (slurp source-path)
        accepted-source
        (str source-text
             "\n(def prior-value 7)\n"
             "(defn sequential-let []\n"
             "  (let [prior-value prior-value\n"
             "        later-value prior-value]\n"
             "    later-value))\n")
        accepted (sh06-source-artifact source-path accepted-source)
        prior-resolutions
        (->> (get-in accepted [:namespace-analysis :resolution-table])
             (filter #(= 'prior-value (:symbol %)))
             (mapv :resolution-order))
        forward-source
        (str source-text
             "\n(defn forward-let []\n"
             "  (let [first-value missing-later\n"
             "        missing-later 1]\n"
             "    first-value))\n")
        forward-result
        (rejection-data-for-source source-path forward-source)]
    (is (= :accepted (:status accepted)))
    (is (= [:current-namespace-binding :local-lexical-binding]
           prior-resolutions))
    (is (= :passed (:status (verification accepted))))
    (is (= "C5-UNRESOLVED" (:id forward-result)))
    (is (= 'missing-later (:symbol forward-result)))
    (is (= [] (:candidate-bindings forward-result)))
    (is (nil? (:raw-host-error forward-result)))
    (is (nil? (:unexpected-success forward-result)))))

(deftest sh06-resolution-products-survive-serialization-roundtrip
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        serialized ((required-var 'sh06-resolution-serialize) artifact)
        restored ((required-var 'sh06-resolution-deserialize) serialized)]
    (is (or (string? serialized)
            (instance? (Class/forName "[B") serialized)))
    (is (= artifact restored))
    (is (= (:artifact-id artifact) (:artifact-id restored)))
    (is (= (semantic-identities artifact) (semantic-identities restored)))
    (is (= :passed (:status (verification restored))))
    (is (= :complete (:status (capability-proof restored))))))

(deftest sh06-serialization-rejects-malformed-or-altered-payloads
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        serialized ((required-var 'sh06-resolution-serialize) artifact)
        serialized-text (if (string? serialized)
                          serialized
                          (String. ^bytes serialized
                                   java.nio.charset.StandardCharsets/UTF_8))
        altered (str/replace-first serialized-text
                                   (:artifact-id artifact)
                                   "sha256:cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc")
        malformed "{:artifact :gravity/sh06-resolution-artifact"]
    (doseq [[label payload] [[:altered altered] [:malformed malformed]]]
      (let [result
            (try
              ((required-var 'sh06-resolution-deserialize) payload)
              {:unexpected-success true}
              (catch clojure.lang.ExceptionInfo error
                (ex-data error))
              (catch Throwable error
                {:raw-host-error (.getName (class error))}))]
        (is (nil? (:unexpected-success result)) (name label))
        (is (nil? (:raw-host-error result)) (name label))
        (is (string? (:id result)) (name label))
        (is (seq (:remediation result)) (name label))))))

(deftest sh06-authoritative-products-and-lineage-alterations-fail-closed
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        changes
        [[:binding-table (assoc-in artifact [:binding-table :status]
                                   :substituted)]
         [:dependency-graph (assoc-in artifact [:dependency-graph :edges]
                                      [{:from 'missing :to 'also-missing}])]
         [:namespace-analysis
          (assoc-in artifact [:namespace-analysis :namespace]
                    'substituted.namespace)]
         [:c4-lineage
          (assoc-in artifact [:sh05-macro-artifact :artifact-id]
                    "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")]
         [:artifact-id
          (assoc artifact :artifact-id
                 "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")]]]
    (doseq [[label changed] changes]
      (let [report (verification changed)
            proof (capability-proof changed)]
        (is (= :failed (:status report)) (name label))
        (is (seq (:failed-checks report)) (name label))
        (is (= :failed (:status proof)) (name label))))))

(deftest sh06-authenticated-boundary-mutations-invalidate-verification
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        boundary-path [:gravity-resolution-boundary]
        changed-digest
        "sha256:dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd"
        mutations
        [[:document-set :document-set-current?
          #(assoc % :document-set ["L3" "C6"])]
         [:governing-document :governing-document-current?
          #(assoc % :governing-document
                  "docs/substituted-governing-document.md")]
         [:provenance-extra-key :provenance-current?
          #(assoc-in % [:provenance :unexpected] true)]
         [:request-definitions :authenticated-resolution-request-current?
          #(update-in %
                      (into boundary-path
                            [:authenticated-resolution-request
                             :definitions 0 :visibility])
                      (fn [visibility]
                        (if (= :private visibility) :public :private)))]
         [:plan-binding :plan-binding-current?
          #(update-in %
                      (into boundary-path [:plan-binding
                                           :source-byte-count])
                      inc)]
         [:raw-template :raw-template-result-current?
          #(assoc-in %
                     (into boundary-path [:raw-template-result :status])
                     :substituted)]
         [:raw-analysis :raw-analysis-current?
          #(assoc-in %
                     (into boundary-path [:raw-analysis :namespace])
                     'substituted.raw)]
         [:resolved-analysis :resolved-analysis-current?
          #(assoc-in %
                     (into boundary-path [:resolved-analysis :namespace])
                     'substituted.resolved)]
         [:digest-requests :digest-requests-current?
          #(assoc-in %
                     (into boundary-path [:digest-requests 0 :purpose])
                     :substituted-digest-purpose)]
         [:resolved-digests :resolved-digests-current?
          #(assoc-in %
                     (into boundary-path [:resolved-digests 0])
                     changed-digest)]
         [:template-verification :template-verification-current?
          #(assoc-in %
                     (into boundary-path [:template-verification :status])
                     :failed)]
         [:resolved-verification :resolved-verification-current?
          #(assoc-in %
                     (into boundary-path [:resolved-verification :status])
                     :failed)]]
        serialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-serialize) candidate)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))]
    (is (= :passed (:status (verification artifact))))
    (doseq [[label expected-check alter] mutations]
      (testing (name label)
        (let [changed (alter artifact)
              report (verification changed)
              serialization (serialize-result changed)]
          (is (not= artifact changed))
          (is (= :failed (:status report)))
          (is (contains? (set (:failed-checks report)) expected-check))
          (is (true? (get-in report
                             [:checks :fresh-gravity-request-replay?])))
          (is (= "C5-UNRESOLVED" (:id serialization)))
          (is (nil? (:unexpected-success serialization)))
          (is (nil? (:raw-host-error serialization))))))))

(deftest sh06-host-resource-failures-are-structured-at-public-seams
  (let [structured-result
        (fn [operation]
          (try
            (operation)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        verification-report
        (with-redefs-fn
          {(private-bootstrap-var
            'sh06-resolution-artifact-verification*)
           (fn [_ _]
             (throw (OutOfMemoryError. "verification resource seam")))}
          #(verification {:bounded :artifact}))
        serialization-result
        (structured-result
         #(with-redefs
           [bootstrap/sh06-resolution-artifact-verification
            (fn [_] {:status :passed})
            bootstrap/sh06-resolution-pr-str
            (fn [_ _]
              (throw (OutOfMemoryError. "serialization resource seam")))]
            ((required-var 'sh06-resolution-serialize)
             {:provenance {:source-path "<sh06-resource-serialize>"}})))
        deserialization-result
        (structured-result
         #(with-redefs
           [clojure.edn/read-string
            (fn [& _]
              (throw (OutOfMemoryError. "deserialization resource seam")))]
            ((required-var 'sh06-resolution-deserialize) "{}")))]
    (testing "public verification contains host resource exhaustion"
      (is (= :failed (:status verification-report)))
      (is (= [:contained-host-resource-failure?]
             (:failed-checks verification-report)))
      (is (false?
           (get-in verification-report
                   [:checks :contained-host-resource-failure?])))
      (is (= "java.lang.OutOfMemoryError"
             (:contained-host-error verification-report))))
    (testing "serialization contains printer resource exhaustion"
      (is (= "C5-UNRESOLVED" (:id serialization-result)))
      (is (= [:contained-resolution-serialization-resource]
             (:missing-fields serialization-result)))
      (is (= "java.lang.OutOfMemoryError"
             (get-in serialization-result
                     [:observed :contained-host-error])))
      (is (nil? (:unexpected-success serialization-result)))
      (is (nil? (:raw-host-error serialization-result))))
    (testing "deserialization contains parser resource exhaustion"
      (is (= "C5-UNRESOLVED" (:id deserialization-result)))
      (is (= [:canonical-resolution-serialization]
             (:missing-fields deserialization-result)))
      (is (= "java.lang.OutOfMemoryError"
             (get-in deserialization-result
                     [:facts :contained-host-error])))
      (is (nil? (:unexpected-success deserialization-result)))
      (is (nil? (:raw-host-error deserialization-result))))))

(deftest sh06-hostile-artifact-carriers-fail-with-structured-containment
  (let [artifact
        (sh06-file-artifact
         (fixture-path "accepted" "resolution-order" ".gravity"))
        nested-value
        (loop [depth 96 value :bounded-leaf]
          (if (zero? depth)
            value
            (recur (dec depth) [value])))
        cases
        [[:bounded-scalar :top-level-artifact-map? :bounded-scalar]
         [:nested-diagnostics :accepted-diagnostics-empty?
          (assoc artifact :diagnostics nested-value)]
         [:wide-diagnostics :accepted-diagnostics-empty?
          (assoc artifact :diagnostics (vec (repeat 65537 :wide)))]]
        verification-result
        (fn [candidate]
          (try
            {:report (verification candidate)}
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        serialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-serialize) candidate)
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))
        deserialize-result
        (fn [candidate]
          (try
            ((required-var 'sh06-resolution-deserialize) (pr-str candidate))
            {:unexpected-success true}
            (catch clojure.lang.ExceptionInfo error
              (ex-data error))
            (catch StackOverflowError error
              {:raw-host-error (.getName (class error))})
            (catch Throwable error
              {:raw-host-error (.getName (class error))
               :message (.getMessage error)})))]
    (doseq [[label expected-check candidate] cases]
      (testing (name label)
        (let [verified (verification-result candidate)
              report (:report verified)
              serialization (serialize-result candidate)
              deserialization (deserialize-result candidate)]
          (is (nil? (:raw-host-error verified)))
          (is (= :failed (:status report)))
          (is (contains? (set (:failed-checks report))
                         expected-check))
          (doseq [result [serialization deserialization]]
            (is (= "C5-UNRESOLVED" (:id result)))
            (is (nil? (:unexpected-success result)))
            (is (nil? (:raw-host-error result)))))))))

(defn- bootstrap-c6-recursive-flatten-oracle [node]
  ;; Frozen recursive operation semantics, not a constructor/verifier substitute.
  (vec (cons node
             (mapcat #(c6-context/invoke-op :c6-flatten-core
                                           bootstrap-c6-recursive-flatten-oracle %)
                     (c6-context/invoke-op :c6-core-child-nodes
                                          c6-lowering/c6-core-child-nodes
                                          (:children node))))))

(deftest sh06-bootstrap-c6-default-flatten-preserves-normalized-artifact
  ;; Normalized C6 DATA exercises the actual bootstrap c6-call operation map.
  ;; The separate authenticated consumer below owns real fresh SH06 gates.
  (let [module {:module 'c6.bootstrap-normalized :source-path "normalized.gravity"
                :profile :hosted :target :jvm :effects #{} :capabilities #{}
                :safety :safe :metadata {}}
        c5 {:kind :gravity/stage0-c5-name-resolution-artifact :artifact-id "sha256:normalized-c5"
            :namespace-analysis {:namespace 'c6.bootstrap-normalized}
            :binding-table {:bindings [{:binding-id "normalized-binding"}]}
            :alias-table {} :dependency-graph {:nodes [] :edges []}}
        syntax (fn [id form] {:syntax-id id :form form :origin :source
                              :span {:source "normalized.gravity" :form-index id}
                              :generated-origin [] :metadata {}})
        stream [(syntax "ns" '(ns c6.bootstrap-normalized))
                (assoc (syntax "generated" '(let [x (if true 1 2)] (do x 3)))
                       :generated-origin [{:macro 'example :syntax-id "macro-0"}])
                (syntax "plain" '(def answer 42))]
        via (private-bootstrap-var 'c6-call)
        build (fn [candidate]
                (via hosted-c6/c6-lowering-artifact "normalized.gravity" candidate c5 stream))
        old-build (fn [candidate]
                    (via #(hosted-c6/with-operations
                            {:c6-flatten-core bootstrap-c6-recursive-flatten-oracle}
                            (fn [] (hosted-c6/c6-lowering-artifact
                                    "normalized.gravity" candidate c5 stream)))))
        expected (old-build module)
        original-invoke c6-context/invoke-op
        original-proof bootstrap/c6-lowering-capability-proof
        calls (atom []) proof-calls (atom 0)
        actual (with-redefs [c6-context/invoke-op
                            (fn [key fallback & args]
                              (when (= :c6-flatten-core key)
                                (swap! calls conj (:node-id (first args))))
                              (apply original-invoke key fallback args))
                            bootstrap/c6-lowering-capability-proof
                            (fn [artifact]
                              (swap! proof-calls inc)
                              (original-proof artifact))]
                 (build module))
        roots (get-in actual [:core-ast-module :roots])]
    (is (= expected actual))
    (is (= (:artifact-id expected) (:artifact-id actual)))
    (is (= :passed (get-in actual [:core-verifier-report :status])))
    (is (= 2 (count roots)))
    (is (= (concat roots roots) @calls))
    (is (= 1 @proof-calls)
        "the invocation-local capability proof is validated and then published")
    (is (= c5 (:c5-name-resolution-artifact actual)))
    (doseq [failure [:origin :verify]]
      (let [candidate (assoc-in module [:metadata :compiler :c6-lowering :fail] failure)
            rejected (fn [thunk]
                       (try (thunk) {:unexpected-success true}
                            (catch clojure.lang.ExceptionInfo error (ex-data error))))
            old-error (rejected #(old-build candidate))
            error (rejected #(build candidate))]
        (is (= old-error error))
        (is (= (if (= :origin failure) "C6-ORIGIN" "C6-VERIFY") (:id error)))
        (is (= "C6" (:document-id error)))
        (is (nil? (:unexpected-success error)))))))

(deftest sh06-bootstrap-c6-custom-flatten-retains-original-operations
  (let [a {:artifact :gravity/core-node :node-id "a" :form :literal :children {}}
        b {:artifact :gravity/core-node :node-id "b" :form :literal :children {}}
        root {:artifact :gravity/core-node :node-id "root" :form 'do
              :children (array-map :first a :nested [b a])}
        replacement (assoc b :node-id "replacement")
        original-bootstrap bootstrap/c6-flatten-core
        original-children c6-lowering/c6-core-child-nodes
        failure (ex-info "original bootstrap C6 callback failure" {:id "C6-ORIGIN" :sentinel 29})]
    (doseq [layer [:bootstrap :facade :leaf]
            operation [:c6-flatten-core :c6-core-child-nodes]
            throw? [false true]]
      (let [flatten? (= :c6-flatten-core operation)
            var (case [layer operation]
                  [:bootstrap :c6-flatten-core] #'bootstrap/c6-flatten-core
                  [:bootstrap :c6-core-child-nodes] #'bootstrap/c6-core-child-nodes
                  [:facade :c6-flatten-core] #'hosted-c6/c6-flatten-core
                  [:facade :c6-core-child-nodes] #'hosted-c6/c6-core-child-nodes
                  [:leaf :c6-flatten-core] #'c6-lowering/c6-flatten-core
                  [:leaf :c6-core-child-nodes] #'c6-lowering/c6-core-child-nodes)
            original @var
            run (fn [oracle?]
                  (let [events (atom [])
                        callback (if flatten?
                                   (fn [node]
                                     (swap! events conj (:node-id node))
                                     (if (= "b" (:node-id node))
                                       (if throw? (throw failure) [replacement])
                                       ((if oracle? bootstrap-c6-recursive-flatten-oracle original) node)))
                                   (fn [value]
                                     (swap! events conj value)
                                     (if throw? (throw failure)
                                         (reverse (original-children value)))))
                        result (try
                                 (if oracle?
                                   (hosted-c6/with-operations {operation callback}
                                     #(if (and flatten? (not= :bootstrap layer))
                                        (callback root)
                                        (bootstrap-c6-recursive-flatten-oracle root)))
                                   (with-redefs-fn {var callback} #(original-bootstrap root)))
                                 (catch clojure.lang.ExceptionInfo error error))]
                    [result @events]))
            [expected expected-events] (run true)
            [actual actual-events] (run false)]
        (is (= expected-events actual-events))
        (if throw?
          (do (is (and (identical? failure expected) (identical? failure actual)))
              (is (= (ex-data expected) (ex-data actual))))
          (do (is (= expected actual))
              (is (every? true? (map identical? expected actual)))))))))

(deftest sh06-authenticated-resolution-is-the-c6-consumer-boundary
  (let [source-path (fixture-path "accepted" "compiler-subset" ".gravity")
        artifact (bootstrap/compiler-c5-resolution-source-artifact
                  source-path (slurp source-path)
                  {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                  (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
        source-read-attempts (atom 0)
        original-read-source-form-records bootstrap/read-source-form-records
        audited-source-reader
        (fn [requested-path requested-text]
          (if (= source-path requested-path)
            (do
              (swap! source-read-attempts inc)
              (throw (ex-info
                      "C6 reread the user module after receiving SH-06 artifact"
                      {:id "SH06-C6-SOURCE-REREAD"})))
            (original-read-source-form-records requested-path
                                               requested-text)))
        lower-with
        (fn [resolution-artifact]
          (with-redefs [bootstrap/read-source-form-records
                        audited-source-reader]
            ((required-var 'sh06-c6-lowering-from-resolution-artifact)
             resolution-artifact)))
        lowered (lower-with artifact)
        ;; Fixed compiler-subset only: observe original hosted C6 work, not SH07
        ;; execution or the interrupted checked_core source subject. No product
        ;; printing/serialization is added. Timings include observer overhead;
        ;; construction contains trace/hash, and trace contains flatten work.
        measurements (atom {}) gates (atom []) root-visits (atom [])
        flatten-phase (atom :assembly) flatten-depth (atom 0)
        require-observation! (fn [condition phase]
                         (when-not condition
                           (throw (ex-info "hosted C6 observation mismatch"
                                           {:phase phase}))))
        timed (fn [label original]
                (fn [& args]
                  (let [start (System/nanoTime)]
                    (try (apply original args)
                         (finally
                           (swap! measurements update label
                                  (fn [prior]
                                    {:calls (inc (get prior :calls 0))
                                     :inclusive-ns (+ (get prior :inclusive-ns 0)
                                                      (- (System/nanoTime) start))})))))))
        original-verify bootstrap/sh06-resolution-artifact-verification
        original-construct hosted-c6/c6-lowering-artifact
        original-flatten bootstrap/c6-flatten-core
        original-trace bootstrap/c6-desugaring-trace
        original-id bootstrap/c4-artifact-id
        observed
        (with-redefs
          [bootstrap/sh06-resolution-artifact-verification
           (fn [candidate]
             (let [start (System/nanoTime) report (original-verify candidate)]
               (swap! gates conj {:same-input? (identical? artifact candidate)
                                  :status (:status report)
                                  :inclusive-ns (- (System/nanoTime) start)})
               report))
           hosted-c6/c6-lowering-artifact (timed :construction original-construct)
           bootstrap/c6-desugaring-trace
           (fn [roots]
             (reset! flatten-phase :desugaring)
             (try ((timed :desugaring original-trace) roots)
                  (finally (reset! flatten-phase :assembly))))
           bootstrap/c6-flatten-core
           (fn [node]
             (let [phase @flatten-phase depth @flatten-depth]
               (swap! measurements update-in [:flatten phase :calls] (fnil inc 0))
               (swap! flatten-depth inc)
               (let [start (System/nanoTime)]
                 (try
                   (let [flat (original-flatten node)]
                     (require-observation! (vector? flat) :returned-flat)
                     ;; Sum every actual recursive return's vector length. This
                     ;; counts row occurrences, not unique allocations/bytes.
                     (swap! measurements update-in [:flatten phase :returned-rows]
                            (fnil + 0) (count flat))
                     (when (zero? depth)
                       (require-observation! (string? (:node-id node)) :root-observation)
                       (swap! root-visits conj {:phase phase :node-id (:node-id node)
                                               :rows (count flat)
                                               :inclusive-ns (- (System/nanoTime) start)}))
                     flat)
                   (finally (swap! flatten-depth dec))))))
           bootstrap/c4-artifact-id
           (fn [value]
             (if (= :gravity/stage0-c6-core-lowering-artifact (:kind value))
               ((timed :final-artifact-id original-id) value)
               (original-id value)))]
          (lower-with artifact))
        altered (assoc artifact :artifact-id
                       "sha256:bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
        rejection
        (try
          (lower-with altered)
          {:unexpected-success true}
          (catch clojure.lang.ExceptionInfo error
            (ex-data error))
          (catch Throwable error
            {:raw-host-error (.getName (class error))}))]
    (is (true? (= lowered observed)) "observed/unobserved complete hosted C6 artifact parity")
    (is (= (:artifact-id lowered) (:artifact-id observed)))
    (is (= 2 (count @gates)))
    (is (every? #(and (:same-input? %) (= :passed (:status %))) @gates))
    (is (= 1 (get-in @measurements [:construction :calls])
           (get-in @measurements [:desugaring :calls])
           (get-in @measurements [:final-artifact-id :calls])))
    (let [roots (get-in observed [:core-ast-module :roots])
          nodes (get-in observed [:core-ast-module :node-count])
          visits #(filter (fn [visit] (= % (:phase visit))) @root-visits)
          assembly (get-in @measurements [:flatten :assembly])
          desugaring (get-in @measurements [:flatten :desugaring])]
      (is (= 3 (count roots)))
      (is (pos? nodes))
      (is (= roots (mapv :node-id (visits :assembly))))
      (is (= roots (mapv :node-id (visits :desugaring))))
      (is (= nodes (:calls assembly) (reduce + (map :rows (visits :assembly)))))
      (is (= nodes (:calls desugaring) (reduce + (map :rows (visits :desugaring)))))
      (is (= (:returned-rows assembly) (:returned-rows desugaring)))
      (is (> (:returned-rows assembly) nodes))
      (require-observation!
       (and (= lowered observed) (= 2 (count @gates))
            (every? #(and (:same-input? %) (= :passed (:status %))) @gates)
            (zero? @source-read-attempts) (= 3 (count roots)) (pos? nodes)
            (= 1 (get-in @measurements [:construction :calls])
               (get-in @measurements [:desugaring :calls])
               (get-in @measurements [:final-artifact-id :calls]))
            (= roots (mapv :node-id (visits :assembly)))
            (= roots (mapv :node-id (visits :desugaring)))
            (= nodes (:calls assembly) (reduce + (map :rows (visits :assembly))))
            (= nodes (:calls desugaring) (reduce + (map :rows (visits :desugaring))))
            (= (:returned-rows assembly) (:returned-rows desugaring))
            (> (:returned-rows assembly) nodes)) :observation-parity)
      (println (pr-str {:hosted-c6-phase-observation :compiler-subset-only
                       :authority :non-authoritative :fresh-c5-gates @gates
                       :measurements @measurements :root-visits @root-visits
                       :flat-nodes nodes :timing :inclusive-not-additive-observer-included
                       :row-unit :sum-of-each-original-recursive-return-vector-length
                       :not-measured [:exclusive-cost :heap :bytes :speedup :large-subject-cause
                                      :gravity-sh07-execution :migration]})))
    (is (= :gravity/stage0-c6-core-lowering-artifact (:kind lowered)))
    (is (= :gravity/sh06-resolution-artifact
           (get-in lowered [:c5-name-resolution-artifact :kind])))
    (is (= (:artifact-id artifact)
           (get-in lowered [:c5-name-resolution-artifact :artifact-id])))
    (is (= :complete (get-in lowered [:surface-to-core-map :status])))
    (is (= :passed (get-in lowered [:core-verifier-report :status])))
    (is (pos? (get-in lowered [:core-ast-module :node-count])))
    (is (= "C6-VERIFY" (:id rejection)))
    (is (nil? (:unexpected-success rejection)))
    (is (nil? (:raw-host-error rejection)))
    ;; Recompute the ordinary ID after a coherent OUTER selection change.
    ;; The independently retained request/run/proof/envelope stay untouched.
    (let [rows (:resolution-table artifact)
          targets (filter #(and (= 'name (:symbol %)) (= :expression (:position %))
                                (= :local-lexical-binding (:resolution-order %))) rows)
          target (first targets)
          bindings (get-in artifact [:binding-table :bindings])
          original (first (filter #(= (:binding-id target) (:binding-id %)) bindings))
          alternatives (filter #(and (= 'table (:name %)) (= :lexical (:binding-class %))
                                     (= (:scope-id original) (:scope-id %))) bindings)
          alternative (first alternatives)
          paths [[:namespace-analysis :resolution-table]
                 [:binding-table :resolution-table] [:resolution-table]]
          matches? #(and (= (:syntax-id target) (:syntax-id %))
                         (= (:symbol target) (:symbol %))
                         (= (:position target) (:position %)))
          rewrite (fn [view] (mapv #(if (matches? %) (assoc % :binding-id (:binding-id alternative)) %) view))
          candidate (reduce #(update-in %1 %2 rewrite) artifact paths)
          identity (required-var 'sh06-resolution-artifact-id)
          candidate (assoc candidate :artifact-id (identity candidate))
          report (verification candidate)
          failed (try (lower-with candidate) {:unexpected-success true}
                      (catch clojure.lang.ExceptionInfo error (ex-data error))
                      (catch Throwable error {:raw-host-error (.getName (class error))}))]
      (is (= 1 (count targets) (count alternatives)))
      (is (sha256-id? (:binding-id target)))
      (is (sha256-id? (:binding-id alternative)))
      (is (not= (:binding-id target) (:binding-id alternative)))
      (is (some? (:scope-id original)))
      (is (= (:scope-id original) (:scope-id alternative)))
      (doseq [p paths]
        (is (= 1 (count (filter matches? (get-in artifact p)))))
        (is (= (:binding-id alternative) (:binding-id (first (filter matches? (get-in candidate p)))))))
      (is (= (:gravity-resolution-boundary artifact) (:gravity-resolution-boundary candidate)))
      (is (= (:sh05-macro-artifact artifact) (:sh05-macro-artifact candidate)))
      (is (sha256-id? (:artifact-id candidate)))
      (is (not= (:artifact-id artifact) (:artifact-id candidate)))
      (is (= (:artifact-id candidate) (identity candidate)))
      (is (= :failed (:status report)))
      (is (true? (get-in report [:checks :artifact-identity-current?])))
      (is (true? (get-in report [:checks :fresh-gravity-request-replay?])))
      (doseq [check [:namespace-analysis-current? :binding-table-current? :resolution-table-current?]]
        (is (false? (get-in report [:checks check])))
        (is (some #{check} (:failed-checks report))))
      (is (= "C6-VERIFY" (:id failed)) (pr-str failed))
      (is (= :core-lowering (:stage failed)))
      (is (= "C6" (:document-id failed)))
      (is (= :c6-ast-core-lowering (:diagnostic-family failed)))
      (is (= [:fresh-authenticated-sh06-resolution] (:missing-fields failed)))
      (is (= {:source source-path :form-index 0} (:source-span failed)))
      (is (seq (:remediation failed)))
      (is (nil? (:unexpected-success failed)))
      (is (nil? (:raw-host-error failed)))
      (is (not (contains? failed :core-ast-module)))
      (println (pr-str {:b-coherent-binding-ingress-diagnostic failed
                       :baseline-artifact-id (:artifact-id artifact)
                       :candidate-artifact-id (:artifact-id candidate)
                       :owner-checks (:checks report) :owner-failed-checks (:failed-checks report)})))
    (is (zero? @source-read-attempts))))


(deftest sh06-b1-authoritative-module-retains-fresh-verification
  (let [source-path
        (path
         "bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity")
        artifact (sh06-file-artifact source-path)]
    (is (= :gravity/sh06-resolution-artifact (:kind artifact)))
    (is (= :accepted (:status artifact)))
    (is (sha256-id? (:artifact-id artifact)))
    (is (= :passed (:status (verification artifact))))
    (is (= :complete (:status (capability-proof artifact))))))




(defn- b-frontend-source-syntax-joins?
  [source c2 input-syntax]
  ;; This cohort contains top-level ns/def/defn LISTS, not arbitrary standalone
  ;; literal forms (whose owning C3 descriptor can differ from its seed value).
  ;; The retained C2 view already projects registered literals when marked;
  ;; fresh C5 verification authenticates that view. Never equate the complete
  ;; C3 {:kind :value :raw} descriptor with a raw C2 form.
  (let [seeds (:syntax-seed-stream c2)
        products (bootstrap/c2-top-level-products c2)
        source-id (bootstrap/sh04-syntax-semantic-source-id source (:source-unit-record c2))
        registry (if (:registered-literal-projection c2)
                   {:lookup {}}
                   (bootstrap/sh04-syntax-registered-literal-registry source c2))
        values (bootstrap/sh04-syntax-project-registered-literal-values
                source registry (mapv :form seeds))]
    (and (= (count (:parsed-semantic-values c2)) (count seeds) (count products) (count input-syntax))
         (= (:parsed-semantic-values c2) (mapv :form seeds))
         (every?
          (fn [[seed product syntax value]]
            (let [record (:form-record product) form (:form seed)]
              (and (seq? form) (contains? '#{ns def defn} (first form))
                   (= (:form-id seed) (:form-id record))
                   (= {:kind :list :value value
                       :raw (or (get-in seed [:reader-origin :raw-excerpt]) "")}
                      (:form syntax))
                   (= {:source-id source-id :form-id (:form-id seed)
                       :token-range [(:open-token record) (:close-token record)]}
                      (:source syntax))
                   (= (let [span (bootstrap/sh04-syntax-semantic-span (:span seed) source-id)]
                        {:primary span :all [span]})
                      (:span syntax)))))
          (map vector seeds products input-syntax values)))))

(deftest sh06-b-frontend-source-schema-development-kernel
  ;; Normalized schema/codec checks only: these inputs confer no authentication.
  (let [source "normalized/b-frontend-schema.gravity"
        descriptor {:artifact :gravity/registered-literal-value :kind :tagged-literal :tag 'inst
                    :canonical-value {:kind :instant :epoch-milliseconds "0"}
                    :semantic-validation :accepted}
        registry {:lookup {['inst "0"] descriptor}}
        scalar {:artifact :gravity/unicode-scalar-character :schema-version 1
                :codepoint 128578 :text (String. (Character/toChars 128578))}
        form (list 'def 'sample [false nil [\a scalar] {:inert '(call payload)} (java.util.Date. 0)])
        span {:source source :byte-start 0 :byte-end 12 :scalar-start 0 :scalar-end 12
              :line-start 1 :line-end 1 :column-start 1 :column-end 13}
        original {:source-unit-record {:path source :bytes-hash (sha256-text "normalized")}
                  :parsed-semantic-values [form]
                  :syntax-seed-stream [{:form form :form-id :root :span span
                                        :reader-origin {:raw-excerpt "(def sample ...)"}}]
                  :form-tree [{:form-id :root :kind :list :open-token :open :close-token :close}]
                  :top-level-form-ids [:root] :token-stream []}
        c2 (assoc (bootstrap/sh04-syntax-project-registered-literal-values source registry original)
                  :registered-literal-projection {:bindings []})
        source-id (bootstrap/sh04-syntax-semantic-source-id source (:source-unit-record c2))
        syntax [{:form {:kind :list :value (first (:parsed-semantic-values c2)) :raw "(def sample ...)"}
                 :source {:source-id source-id :form-id :root :token-range [:open :close]}
                 :span (let [projected (bootstrap/sh04-syntax-semantic-span span source-id)]
                         {:primary projected :all [projected]})}]
        value (nth (get-in syntax [0 :form :value]) 2)]
    (is (not= (:parsed-semantic-values c2) (mapv :form syntax)))
    (is (not= (:parsed-semantic-values original) (:parsed-semantic-values c2)))
    (is (= false (nth value 0)))
    (is (nil? (nth value 1)))
    (is (= [\a scalar] (nth value 2)))
    (is (= {:inert '(call payload)} (nth value 3)))
    (is (= descriptor (nth value 4)))
    (is (true? (b-frontend-source-syntax-joins? source c2 syntax)))
    (doseq [changed [(assoc-in syntax [0 :form] form)
                     (assoc-in syntax [0 :form :value] form)
                     (assoc-in syntax [0 :form :value] '(def sample false))
                     (assoc-in syntax [0 :source :form-id] :wrong)
                     (assoc-in syntax [0 :source :source-id] (sha256-text "wrong"))
                     (assoc-in syntax [0 :source :token-range] [:open :wrong])
                     (update-in syntax [0 :span :primary :byte-end] inc)]]
      (is (false? (b-frontend-source-syntax-joins? source c2 changed))))))

(defn- b-with-subject-read-audit
  [subject counts operation]
  ;; A subject can also be a compiler owner. Observe the real plan-construction
  ;; boundary, not a filename exemption or a substituted verification result.
  (let [original-reader bootstrap/read-source-form-records
        original-plan bootstrap/p15-s23-stage2-compiler-artifact-plan
        original-verifier bootstrap/sh06-resolution-artifact-verification
        owner-source (ThreadLocal.)]
    (with-redefs
      [bootstrap/p15-s23-stage2-compiler-artifact-plan
       (fn [emitter source text & inputs]
         (let [prior (.get owner-source)]
           (.set owner-source [source text])
           (try
             (apply original-plan emitter source text inputs)
             (finally
               (if prior (.set owner-source prior) (.remove owner-source))))))
       bootstrap/read-source-form-records
       (fn [source text]
         (when (= source subject)
           (if (= [source text] (.get owner-source))
             (swap! counts update :owner-reads inc)
             (do
               (swap! counts update :subject-read-attempts inc)
               (throw (ex-info "Hosted C6 reread selected frontend subject"
                               {:id "SH06-B-SUBJECT-REREAD"})))))
         (original-reader source text))
       bootstrap/sh06-resolution-artifact-verification
       (fn [artifact]
         (let [report (original-verifier artifact)]
           (when-not (= :passed (:status report))
             (println (pr-str {:b-frontend-fresh-verification (:status report)
                              :contained-diagnostic-id (get-in report [:contained-diagnostic :id])
                              :failed-checks (vec (take 16 (:failed-checks report)))})))
           report))]
      (operation))))

(deftest sh06-b-subject-read-audit-distinguishes-owner-construction
  (let [subject "<selected-owner>" text "(ns selected.owner)"
        counts (atom {:owner-reads 0 :subject-read-attempts 0})
        delegated (atom [])
        report {:status :failed :failed-checks [:specific] :opaque {:unmodified true}}
        rejection (fn [operation]
                    (try (operation) nil
                         (catch clojure.lang.ExceptionInfo error (:id (ex-data error)))))]
    (with-redefs
      [bootstrap/read-source-form-records
       (fn [source value] (swap! delegated conj [source value]) value)
       bootstrap/p15-s23-stage2-compiler-artifact-plan
       (fn [mode source value & inputs]
         (case mode
           :arguments inputs
           :nested (do (bootstrap/p15-s23-stage2-compiler-artifact-plan :normal "<other-owner>" "nested")
                       (bootstrap/read-source-form-records source value))
           :mismatched (bootstrap/read-source-form-records source (str value " "))
           :throws (do (bootstrap/read-source-form-records source value)
                       (throw (ex-info "owner construction failure" {:id "OWNER-FAIL"})))
           (bootstrap/read-source-form-records source value)))
       bootstrap/sh06-resolution-artifact-verification (fn [_] report)]
      (b-with-subject-read-audit
       subject counts
       (fn []
         (is (= text (bootstrap/p15-s23-stage2-compiler-artifact-plan :normal subject text)))
         (is (= '(:inputs) (bootstrap/p15-s23-stage2-compiler-artifact-plan :arguments subject text :inputs)))
         (is (= text (bootstrap/p15-s23-stage2-compiler-artifact-plan :nested subject text)))
         (is (= "SH06-B-SUBJECT-REREAD"
                (rejection #(bootstrap/read-source-form-records subject text))))
         (is (= "SH06-B-SUBJECT-REREAD"
                (rejection #(bootstrap/p15-s23-stage2-compiler-artifact-plan :mismatched subject text))))
         (is (= "OWNER-FAIL"
                (rejection #(bootstrap/p15-s23-stage2-compiler-artifact-plan :throws subject text))))
         (is (= "SH06-B-SUBJECT-REREAD"
                (rejection #(bootstrap/read-source-form-records subject text))))
         (is (= "other" (bootstrap/read-source-form-records "<other>" "other")))
         (let [printed (with-out-str
                         (is (identical? report (bootstrap/sh06-resolution-artifact-verification :candidate))))]
           (is (str/includes? printed ":specific"))
           (is (not (str/includes? printed "unmodified")))))))
    (is (= {:owner-reads 3 :subject-read-attempts 3} @counts))
    (is (= [[subject text] ["<other-owner>" "nested"] [subject text]
            [subject text] ["<other>" "other"]] @delegated))))

(defn- b-run-frontend-subject
  [[relative expected-defns]]
  ;; L4/C4/C5/TEST2: this explicit current frontend cohort is not the historical
  ;; 36/37/41 corpus, an authoritative migration, or execution of every body.
  ;; Reader is a whole-source subject (275 defns), although its SH03 execution
  ;; plan selects only 231 sh03-* functions. P15 compiler rules are consumed as
  ;; DATA by the existing constructors, not credited as an executed compiler.
          (let [source (path (str "bootstrap/gravity/src/gravity/" relative))
                text (slurp source :encoding "UTF-8")
                check (fn [condition label]
                        ;; Keep failure output bounded: never print a complete
                        ;; nested compiler product as the assertion operand.
                        (is (boolean condition) (str relative " " label)))
                _ (println (pr-str {:b-frontend-subject relative :phase :c5-before}))
                resolved (bootstrap/compiler-c5-resolution-file-artifact source)
                expanded (:sh05-macro-artifact resolved)
                c3 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact])
                c2 (:c2-reader-artifact c3)
                forms (:parsed-semantic-values c2)
                seeds (:syntax-seed-stream c2)
                input-syntax (bootstrap/sh05-source-syntax-stream c3)
                output-syntax (:expanded-syntax-stream expanded)
                defn-indexes (keep-indexed #(when (and (seq? %2) (= 'defn (first %2))) %1) forms)
                trace (:macro-expansion-trace expanded)
                boundary (:gravity-resolution-boundary resolved)
                request (:authenticated-resolution-request boundary)
                references (:references request)
                rows (:resolution-table resolved)
                bindings (get-in resolved [:binding-table :bindings])
                binding-ids (set (map :binding-id bindings))
                reference-fields [:syntax-id :symbol :position :semantic-span :source-span]]
            (println (pr-str {:b-frontend-subject relative :phase :c5-after
                              :definitions (count (:definitions request))
                              :references (count references)}))
            (when
             (and
              (check (= :accepted (:status resolved) (:status expanded)) :accepted-products)
              (check (= :development-unadmitted (get-in boundary [:plan-binding :revision-status]))
                     :ordinary-development-status)
              (check (= source (get-in resolved [:provenance :source-path])
                        (get-in c2 [:source-unit-record :path])) :source-path)
              (check (= (sha256-text text) (get-in c2 [:source-unit-record :bytes-hash])) :source-bytes)
              (check (= (count forms) (count seeds) (count input-syntax) (count output-syntax))
                     :complete-source-streams)
              (check (b-frontend-source-syntax-joins? source c2 input-syntax) :original-c3-source-projection)
              (check (= expected-defns (count defn-indexes) (:expanded-defn-count expanded) (count trace))
                     :source-derived-expansion-count)
              (check
               (every? (fn [[index step]]
                         (let [input (nth input-syntax index) output (nth output-syntax index)]
                           (and (= 'defn (:macro step))
                                (= (:syntax/id input) (:input-syntax-id step))
                                (= (:syntax/id output) (:output-syntax-id step))
                                (= (:span input) (:call-site-span step) (:span output))
                                (seq (:generated-origin step))
                                (= (last (:origin output)) (first (:generated-origin step)))
                                (seq (get-in step [:hygiene :introduced-identifiers])))))
                       (map vector defn-indexes trace))
               :introduced-and-reused-source-origins)
              (check (= (:artifact-id expanded) (get-in request [:module :c4-artifact-id])
                        (get-in request [:macro-expansion-binding :c4-artifact-id])) :c4-parent-binding)
              (check (and (seq references)
                          (= (mapv #(select-keys % reference-fields) references)
                             (mapv #(select-keys % reference-fields) rows))) :source-reference-bijection)
              (check (every? #(and (sha256-id? (:binding-id %)) (contains? binding-ids (:binding-id %))) rows)
                     :selected-binding-identities))
             ;; The reference macro algorithm receives the pre-expansion
             ;; authenticated C2 view, never C3 descriptors or expanded forms. It does not
             ;; reconstruct another complete reader/C5 product for comparison.
             (let [records (mapv (fn [form seed] (assoc seed :form form)) forms seeds)
                   reference (bootstrap/macro-source-artifact-from-records source text records)]
               (when (check (= (:expanded-forms reference) (:expanded-forms expanded)) :stage0-defn-form-parity)
                 (println (pr-str {:b-frontend-subject relative :phase :hosted-c6-before}))
                 ;; Public C6 freshly verifies this exact C5 input. Do not add
                 ;; another test-only verification of the unchanged artifact.
                 (let [reads (atom {:owner-reads 0 :subject-read-attempts 0})
                       core (b-with-subject-read-audit
                             source reads
                             #(bootstrap/sh06-c6-lowering-from-resolution-artifact resolved))]
                   (println (pr-str {:b-frontend-subject relative :read-audit @reads}))
                   (when
                    (and
                     (check (= :gravity/stage0-c6-core-lowering-artifact (:kind core)) :hosted-c6-boundary)
                     (check (= (:artifact-id resolved) (get-in core [:c5-name-resolution-artifact :artifact-id]))
                            :c6-c5-identity)
                     (check (= :passed (get-in core [:core-verifier-report :status])) :core-verifier)
                     (check (= (mapv :syntax/id (rest output-syntax))
                               (mapv :surface-syntax (get-in core [:surface-to-core-map :entries])))
                            :complete-core-source-map)
                     (check (zero? (:subject-read-attempts @reads)) :no-subject-reread)
                     (check (= text (slurp source :encoding "UTF-8")) :source-stable-after-consumption))
                    ;; Return scalars only. No subject product survives in the
                    ;; subject invocation, and no proof/cache status is reused.
                    {:subject relative :expanded-defns (count defn-indexes)
                     :references (count references) :artifact-id (:artifact-id resolved)
                     :core-node-count (get-in core [:core-ast-module :node-count])})))))))

(deftest sh06-b-frontend-macro-development-coverage
  (println (pr-str {:b-frontend-summary (b-run-frontend-subject ["macro.gravity" 46])})))

(deftest sh06-b-frontend-envelope-development-coverage
  (println (pr-str {:b-frontend-summary (b-run-frontend-subject ["compiler/authenticated_envelope.gravity" 72])})))

(deftest sh06-b-frontend-syntax-development-coverage
  (println (pr-str {:b-frontend-summary (b-run-frontend-subject ["bootstrap/syntax.gravity" 102])})))

(deftest sh06-b-frontend-resolver-development-coverage
  (println (pr-str {:b-frontend-summary (b-run-frontend-subject ["resolution.gravity" 111])})))

(deftest sh06-b-frontend-reader-development-coverage
  (println (pr-str {:b-frontend-summary (b-run-frontend-subject ["bootstrap/reader.gravity" 275])})))

(deftest sh06-b-source-duplicate-alias-composition
  ;; L3/C5: genuine imports, not caller-invented binding candidates. Only G
  ;; subjects are constructed; QST receives explicitly separate parser parity.
  (doseq [name ["b-alias-distinct" "b-alias-duplicate"]]
    (let [g (fixture-path "composition" name ".gravity")
          q (fixture-path "composition" name ".qst")
          text (slurp g)]
      (is (= text (slurp q)))
      (is (= (bootstrap/read-forms g text) (bootstrap/read-forms q (slurp q))))))
  (let [libraries (mapv #(bootstrap/compiler-c5-resolution-file-artifact
                         (fixture-path "composition" % ".gravity"))
                       ["b-referred-left" "b-referred-right"])
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1
                :module-artifacts libraries}
        positive-path (fixture-path "composition" "b-alias-distinct" ".gravity")
        negative-path (fixture-path "composition" "b-alias-duplicate" ".gravity")
        caller (bootstrap/compiler-c5-resolution-file-artifact positive-path inputs)
        aliases (get-in caller [:alias-table :aliases])
        imported (filter #(and (= :import (:binding-class %))
                               (= 'b-shared-value (:name %)))
                         (get-in caller [:binding-table :bindings]))
        references (filter #(contains? #{'left/b-shared-value 'right/b-shared-value}
                                       (:symbol %)) (:resolution-table caller))
        read-counts (atom {:owner-reads 0 :subject-read-attempts 0})
        core (b-with-subject-read-audit
              positive-path read-counts
              #(bootstrap/sh06-c6-lowering-from-resolution-artifact caller))
        failure (try
                  (bootstrap/compiler-c5-resolution-file-artifact negative-path inputs)
                  {:unexpected-success true}
                  (catch clojure.lang.ExceptionInfo error (ex-data error)))
        candidates (:candidate-bindings failure)]
    (doseq [library libraries]
      (is (= :accepted (:status library)))
      (is (= :passed (:status (verification library)))))
    (is (= :accepted (:status caller)))
    (is (= :development-unadmitted
           (get-in caller [:gravity-resolution-boundary :plan-binding :revision-status])))
    (is (= :passed (:status (verification caller))))
    (is (= #{'left 'right} (set (map :alias aliases))))
    (is (= 2 (count aliases) (count imported) (count references)))
    (is (= (set (map :binding-id imported)) (set (map :binding-id references))))
    (doseq [[library alias-name] (map vector libraries ['left 'right])]
      (let [definition (first (filter #(and (= :namespace (:binding-class %))
                                           (= 'b-shared-value (:name %)))
                                     (get-in library [:binding-table :bindings])))
            alias (first (filter #(= alias-name (:alias %)) aliases))
            binding (first (filter #(= (:artifact-id library) (:definition-artifact-id %)) imported))]
        (is (= (:artifact-id library) (:dependency-artifact-id alias) (:definition-artifact-id binding)))
        (is (= (:namespace definition) (:namespace alias) (:namespace binding)))
        (is (= (:definition-syntax-id definition) (:definition-syntax-id binding)))
        (is (= (:binding-id definition) (get-in binding [:semantic-span :origin-binding-id])))))
    (is (= :gravity/stage0-c6-core-lowering-artifact (:kind core)))
    (is (= :passed (get-in core [:core-verifier-report :status])))
    (is (= (:artifact-id caller) (get-in core [:c5-name-resolution-artifact :artifact-id])))
    (is (= (mapv :syntax/id (rest (get-in caller [:sh05-macro-artifact :expanded-syntax-stream])))
           (mapv :surface-syntax (get-in core [:surface-to-core-map :entries]))))
    (is (zero? (:subject-read-attempts @read-counts)))
    (is (= "C5-ALIAS" (:id failure)) (pr-str failure))
    (is (= :name-resolution (:stage failure)))
    (is (= :duplicate-namespace-alias (get-in failure [:facts :reason])))
    (is (= 'duplicate (:symbol failure) (get-in failure [:facts :rule-specific :alias])))
    (is (= negative-path (get-in failure [:source-span :source])))
    (is (= {:source negative-path :byte-start 0 :line-start 1
            :byte-end (.indexOf (slurp negative-path) "\n(defn")}
           (select-keys (get-in failure [:source-span :primary])
                        [:source :byte-start :line-start :byte-end])))
    (is (and (seq (get-in failure [:source-span :all]))
             (some #{(get-in failure [:source-span :primary])}
                   (get-in failure [:source-span :all]))))
    (is (= 2 (count candidates)))
    (is (= #{'example.b-referred-left 'example.b-referred-right}
           (set (map :namespace candidates))))
    (is (every? #(and (= 'duplicate (:alias %)) (= :namespace (:kind %))) candidates))
    (doseq [library libraries]
      (let [matching (filter #(= (:artifact-id library) (:dependency-artifact-id %)) candidates)]
        (is (= 1 (count matching)))
        (is (= (select-keys (:source-span failure) [:all :primary])
               (:source-span (first matching))))))
    ;; A header error has no causal generated body reference. Do not fabricate
    ;; its syntax id or macro-origin chain from the first function-body call.
    (is (nil? (:syntax-id failure)))
    (is (empty? (:origin-chain failure)))
    (is (nil? (:unexpected-success failure)))
    (is (not (contains? failure :c5-name-resolution-artifact)))
    (is (not (contains? failure :core-ast-module)))
    (println (pr-str {:b-duplicate-alias-diagnostic failure
                     :positive-read-audit @read-counts}))))

(deftest sh06-b-source-profile-incompatible-composition
  ;; L3/C5: same-target modules still need a legal cross-profile boundary.
  ;; Compile G only; Q gets byte/parser parity, not a second source run.
  (let [source (fixture-path "composition" "b-profile-incompatible" ".gravity")
        qst (fixture-path "composition" "b-profile-incompatible" ".qst")
        text (slurp source)
        library (bootstrap/compiler-c5-resolution-file-artifact
                 (fixture-path "composition" "b-referred-left" ".gravity"))
        module (get-in library [:gravity-resolution-boundary :authenticated-resolution-request :module])
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
        failure (try
                  (bootstrap/compiler-c5-resolution-file-artifact source inputs)
                  {:unexpected-success true}
                  (catch clojure.lang.ExceptionInfo error (ex-data error)))
        edge (:dependency-edge failure)]
    (is (= text (slurp qst)))
    (is (= (bootstrap/read-forms source text) (bootstrap/read-forms qst (slurp qst))))
    (is (= :accepted (:status library)))
    (is (= :passed (:status (verification library))))
    (is (= {:profile :meta :target :jvm :capabilities []}
           (select-keys module [:profile :target :capabilities])))
    (is (= "C5-CROSS-PROFILE" (:id failure)) (pr-str failure))
    (is (= :name-resolution (:stage failure)))
    (is (= :unsupported-or-impure-module-boundary (get-in failure [:facts :reason])))
    (is (= 'example.b-referred-left (:symbol failure) (:namespace edge)))
    (is (= :namespace (:kind edge)))
    (is (= 'left (:alias edge)))
    (is (= 'example.b-profile-incompatible (:namespace failure)))
    (is (= :hosted (:active-profile failure)))
    (is (= :jvm (:target failure)))
    (is (= source (get-in failure [:source-span :source])))
    (is (= {:source source :byte-start 0 :line-start 1
            :byte-end (.indexOf text "\n(defn")}
           (select-keys (get-in failure [:source-span :primary])
                        [:source :byte-start :line-start :byte-end])))
    (is (= (select-keys (:source-span failure) [:all :primary]) (:source-span edge)))
    (is (and (seq (get-in failure [:source-span :all]))
             (some #{(get-in failure [:source-span :primary])}
                   (get-in failure [:source-span :all]))))
    (is (string? (:remediation failure)))
    (is (seq (:remediation failure)))
    (is (= [] (:candidate-bindings failure)))
    ;; Compatibility reports the original import before enrichment. The real
    ;; callee contract above comes from the verified artifact, not this edge.
    (is (sha256-id? (:syntax-id failure)))
    (is (= (:syntax-id failure) (get-in edge [:semantic-span :namespace-syntax-id])))
    (is (empty? (:origin-chain failure)))
    (is (nil? (:unexpected-success failure)))
    (is (not (contains? failure :c5-name-resolution-artifact)))
    (is (not (contains? failure :core-ast-module)))
    (println (pr-str {:b-profile-incompatible-diagnostic failure
                     :supplied-library-artifact-id (:artifact-id library)}))))

(deftest sh06-b-source-capability-boundary-composition
  ;; L3/C5: effect declarations do not satisfy an imported capability need.
  ;; This is metadata/binding coverage, never IO execution or provider grants.
  (doseq [[directory name] [["modules" "io"]
                            ["composition" "b-capability-missing"]
                            ["composition" "b-capability-declared"]]]
    (let [g (fixture-path directory name ".gravity") q (fixture-path directory name ".qst")
          text (slurp g)]
      (is (= text (slurp q)))
      (is (= (bootstrap/read-forms g text) (bootstrap/read-forms q (slurp q))))))
  (let [library (bootstrap/compiler-c5-resolution-file-artifact (fixture-path "modules" "io" ".gravity"))
        module (get-in library [:gravity-resolution-boundary :authenticated-resolution-request :module])
        definition (first (filter #(and (= :namespace (:binding-class %)) (= 'write (:name %)))
                                  (get-in library [:binding-table :bindings])))
        inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
        positive (bootstrap/compiler-c5-resolution-file-artifact
                  (fixture-path "composition" "b-capability-declared" ".gravity") inputs)
        imported (first (filter #(and (= :import (:binding-class %)) (= 'write (:name %)))
                                (get-in positive [:binding-table :bindings])))
        reference (first (filter #(= 'io/write (:symbol %)) (:resolution-table positive)))
        dependency (first (get-in positive [:import-export-table :imports]))
        source (fixture-path "composition" "b-capability-missing" ".gravity") text (slurp source)
        failure (try (bootstrap/compiler-c5-resolution-file-artifact source inputs)
                     {:unexpected-success true}
                     (catch clojure.lang.ExceptionInfo error (ex-data error)))
        edge (:dependency-edge failure)]
    (is (= :accepted (:status library) (:status positive)))
    (is (= :passed (:status (verification library)) (:status (verification positive))))
    (is (= {:namespace 'gravity.io :profile :hosted :target :jvm :exports '[write]
            :effects [:io/write] :capabilities [:io/stdout]}
           (select-keys module [:namespace :profile :target :exports :effects :capabilities])))
    (is (= :public (:visibility definition)))
    (is (= [:io/write] (:effects definition) (:effects imported)))
    (is (= [:io/stdout] (:capabilities definition) (:capabilities imported)))
    (is (= (:artifact-id library) (:definition-artifact-id imported) (:dependency-artifact-id dependency)))
    (is (= (:definition-syntax-id definition) (:definition-syntax-id imported)))
    (is (= (:binding-id definition) (get-in imported [:semantic-span :origin-binding-id])))
    (is (= (:binding-id imported) (:binding-id reference)))
    (is (= {:to 'gravity.io :kind :namespace :profile-boundary :compatible
            :effects [:io/write] :capabilities [:io/stdout] :targets [:jvm]}
           (select-keys dependency [:to :kind :profile-boundary :effects :capabilities :targets])))
    (is (= [:io/stdout]
           (get-in positive [:gravity-resolution-boundary :authenticated-resolution-request :module :capabilities])))
    (is (= "C5-CAPABILITY" (:id failure)) (pr-str failure))
    (is (= :name-resolution (:stage failure)))
    (is (= :missing-import-capability (get-in failure [:facts :reason])))
    (is (= 'gravity.io (:symbol failure) (:namespace edge)))
    (is (= :namespace (:kind edge)))
    (is (= 'io (:alias edge)))
    (is (= 'example.b-capability-missing (:namespace failure)))
    (is (= :hosted (:active-profile failure)))
    (is (= :jvm (:target failure)))
    (is (= source (get-in failure [:source-span :source])))
    (is (= {:source source :byte-start 0 :line-start 1
            :byte-end (.indexOf text "\n(defn")}
           (select-keys (get-in failure [:source-span :primary])
                        [:source :byte-start :line-start :byte-end])))
    (is (= (select-keys (:source-span failure) [:all :primary]) (:source-span edge)))
    (is (and (seq (get-in failure [:source-span :all]))
             (some #{(get-in failure [:source-span :primary])}
                   (get-in failure [:source-span :all]))))
    (is (sha256-id? (:syntax-id failure)))
    (is (= (:syntax-id failure) (get-in edge [:semantic-span :namespace-syntax-id])))
    (is (empty? (:origin-chain failure)))
    (is (= [] (:candidate-bindings failure)))
    (is (string? (:remediation failure)))
    (is (seq (:remediation failure)))
    (is (nil? (:unexpected-success failure)))
    (is (not (contains? failure :c5-name-resolution-artifact)))
    (is (not (contains? failure :core-ast-module)))
    ;; The rejected edge is pre-enrichment, not the callee's capability record.
    ;; The positive namespace declaration is not a package/provider grant.
    (println (pr-str {:b-missing-capability-diagnostic failure
                     :library-artifact-id (:artifact-id library)
                     :positive-artifact-id (:artifact-id positive)
                     :positive-dependency dependency}))))

(deftest sh06-corpus-shards-cover-the-sorted-inventory-exactly-once
  (let [source-paths (authoritative-compiler-paths)
        relative-paths (authoritative-compiler-relative-paths source-paths)]
    (is (= source-paths (vec (sort source-paths))))
    (is (= authoritative-compiler-module-count (count source-paths)))
    (is (= authoritative-compiler-path-inventory-sha256
           (sha256-text (str/join "\n" relative-paths))))
    (doseq [shard-count [1 2 3 4 5 39 40 41]]
      (is (corpus-partition-valid? source-paths shard-count)
          (str shard-count " shards")))))

(deftest sh06-resolves-the-complete-authoritative-compiler-source-set
  (if (= "1" (System/getenv "SH06_SKIP_CORPUS"))
    (do
      (println (pr-str {:sh06-corpus :skipped}))
      (is true "SH-06 corpus test explicitly skipped"))
    (let [all-source-paths (authoritative-compiler-paths)
          shard (parse-corpus-shard)
          source-paths (if shard
                         (corpus-shard-paths all-source-paths
                                             (:index shard) (:count shard))
                         all-source-paths)
          results
          (mapv
           (fn [source-path]
             (let [artifact (sh06-file-artifact source-path)]
               {:path source-path
                :kind (:kind artifact)
                :status (:status artifact)
                :artifact-id (:artifact-id artifact)
                :proof-artifact
                (get-in artifact
                        [:capability-based-proof :artifact])
                :proof-status
                (get-in artifact
                        [:capability-based-proof :status])
                :proof-failed-checks
                (get-in artifact
                        [:capability-based-proof :failed-checks])}))
           source-paths)]
      (when-not shard
        (is (pos? (count source-paths))))
      (is (every? #(= :gravity/sh06-resolution-artifact (:kind %)) results))
      (is (every? #(= :accepted (:status %)) results))
      (is (every? #(sha256-id? (:artifact-id %)) results))
      (is (every? #(= :gravity/sh06-resolution-capability-proof
                       (:proof-artifact %))
                  results))
      (is (every? #(= :complete (:proof-status %)) results))
      (is (every? #(= [] (:proof-failed-checks %)) results))
      (when shard
        (println (pr-str {:sh06-corpus-shard (:label shard)
                          :module-count (count source-paths)}))))))
