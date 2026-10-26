(ns gravity.self-hosting.sh07-vector-pattern-test
  (:require [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_vector_pattern_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-07-B11 test source is not on the classpath"
                {:id "SH07-B11-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH07-B11-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b11")
(def ^:private b10-fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b10")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-fixtures
  #{"nested-vector-patterns"
    "vector-branch-local-binding"
    "vector-leaf-patterns"
    "vector-scrutinee-once"
    "vector-tail-recur" "keyword-map-patterns" "nested-map-in-vector"
    "recursive-map-patterns" "map-tail-recur"})
(def ^:private rejected-fixtures
  #{"guard-pattern-deferred"
    "list-pattern-deferred"
    "map-non-keyword-key" "duplicate-map-bindings" "map-unsupported-leaf"
    "map-branch-binding-escape" "map-sibling-binding-escape"
    "set-pattern-deferred"
    "vector-pattern-over-width"
    "vector-rest-extra-tail"
    "vector-rest-missing-tail"
    "vector-rest-tail"})

(def ^:private map-upstream-rejections #{"map-branch-binding-escape" "map-sibling-binding-escape"})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- b10-fixture-path
  [family basename extension]
  (path (str b10-fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (if-not (.isDirectory directory)
      #{}
      (->> (.listFiles directory)
           (filter #(.isFile %))
           (map #(.getName %))
           (filter #(str/ends-with? % extension))
           (map #(subs % 0 (- (count %) (count extension))))
           set))))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-text
  [source-path]
  (slurp source-path :encoding "UTF-8"))

(defn- source-forms
  [relative]
  (with-open [reader
              (java.io.PushbackReader.
               (io/reader (path relative) :encoding "UTF-8"))]
    (let [eof (Object.)]
      (loop [forms []]
        (let [form (read {:eof eof} reader)]
          (if (identical? eof form)
            forms
            (recur (conj forms form))))))))

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

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info
        "Required SH-07-B11 coordinator adapter is absent"
        {:id "SH07-B11-ADAPTER-ABSENT"
         :symbol symbol
         :required-signatures
         {'sh07-core-source-artifact '[source-path source-text]
          'sh07-core-file-artifact '[source-path]
          'sh07-core-artifact-verification '[artifact]
          'sh07-core-artifact-identity-input '[artifact]
          'sh07-core-verification-checks
          '[artifact expected upstream-verification]
          'sh07-core-from-authenticated-request
          '[resolution-artifact authenticated-request]}}))))

(def ^:private artifacts (atom {}))
(def ^:private c2-artifacts (atom {}))

(defn- file-artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @artifacts key)
        (let [artifact
              ((required-var 'sh07-core-file-artifact)
               (fixture-path family basename extension))]
          (swap! artifacts assoc key artifact)
          artifact))))

(defn- direct-artifact
  [family basename extension]
  (let [source-path (fixture-path family basename extension)]
    ((required-var 'sh07-core-source-artifact)
     source-path
     (source-text source-path))))

(defn- b10-file-artifact
  [basename extension]
  ((required-var 'sh07-core-file-artifact)
   (b10-fixture-path "accepted" basename extension)))

(defn- c2-artifact
  [basename extension]
  (let [key [basename extension]]
    (or (get @c2-artifacts key)
        (let [artifact
              (bootstrap/compiler-c2-reader-file-artifact
               (fixture-path "rejected" basename extension))]
          (swap! c2-artifacts assoc key artifact)
          artifact))))

(defn- fixture-oracle
  [basename extension]
  (let [artifact (c2-artifact basename extension)
        ns-form (first (:parsed-semantic-values artifact))
        clause
        (some #(when (and (seq? %) (= :metadata (first %))) %)
              (drop 2 ns-form))]
    (get (second clause) :sh07-b11)))

(defn- core
  [artifact]
  (get-in artifact [:gravity-core-boundary :canonical-core-artifact]))

(defn- request
  [artifact]
  (get-in artifact [:gravity-core-boundary :authenticated-core-request]))

(defn- identity-input
  [artifact]
  ((required-var 'sh07-core-artifact-identity-input) artifact))

(defn- exactly-once-index
  [records key-name]
  (let [index (into {} (map (juxt key-name identity)) records)]
    (when-not (= (count records) (count index))
      (throw
       (ex-info "SH-07-B11 records are not uniquely identifiable"
                {:id "SH07-B11-AMBIGUOUS-INDEX"
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

(defn- verification-failures
  [altered expected]
  (set
   (for [[check passed?]
         ((required-var 'sh07-core-verification-checks)
          altered expected {:status :passed})
         :when (not (true? passed?))]
     check)))

(defn- execute-core-function
  [basename function arguments]
  (bootstrap/sh07-core-execute!
   (fixture-path "accepted" basename ".gravity")
   function
   arguments))

(defn- match-groups
  [artifact]
  (group-by :core-node-id (:match-branch-records (core artifact))))

(defn- pattern-groups
  [artifact]
  (group-by :core-node-id (:match-pattern-records (core artifact))))

(defn- pattern-clause-groups
  [artifact]
  (group-by
   (juxt :core-node-id :clause-ordinal)
   (:match-pattern-records (core artifact))))

(def ^:private match-pattern-record-keys
  #{:ordinal :clause-ordinal :local-ordinal
    :parent-local-ordinal :root-local-ordinal
    :parent-ordinal :root-ordinal :core-node-id
    :depth :path :pattern-kind
    :pattern-form-id :pattern-syntax-id
    :pattern-value :vector-width
    :pattern-binding-id :pattern-binding-scope-id
    :pattern-binding-use-syntax-ids
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id})

(deftest sh07-b11-keyword-map-development-kernel
  ;; Normalized unit graphs execute the current Gravity pass, not provenance
  ;; evidence. Source paths below use the existing flattened semantic map order.
  (let [plan (:plan (bootstrap/sh07-core-current-binding!))
        invoke (fn [function arguments]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-sh07-development-fixture :compiler-artifact-plan? true}
                  plan function arguments))
        sha #(str "sha256:" (format "%064x" %))
        ordinal (atom 0)
        forms (atom {})
        upstream-cases ['{:id x :name y} '[{:inner {:tags [(quote ready) _ z]}}] {}
                        (into {} (map (fn [n] [(keyword (str "k" n)) n]) (range 12)))]]
    (letfn [(make! [value]
              (let [kind (cond (map? value) :map (vector? value) :vector (seq? value) :list
                               (symbol? value) :symbol (keyword? value) :keyword
                               (integer? value) :integer (nil? value) :nil (set? value) :set
                               :else :boolean)
                    children (mapv make! (cond (map? value) (vec (mapcat identity value))
                                               (coll? value) (vec value) :else []))
                    n (swap! ordinal inc)
                    form {:form-id (sha n) :syntax-id (sha (+ 1000 n)) :kind kind :value value
                          :child-form-ids (mapv :form-id children) :scope-id (sha 5000)
                          :metadata {} :origin-chain [] :generated-origin []}]
                (swap! forms assoc (:form-id form) form)
                (doseq [child children]
                  (swap! forms assoc-in [(:form-id child) :parent-form-id] (:form-id form)))
                form))
            (request []
              (let [bindings (for [form (vals @forms) :when (contains? '#{x y z} (:value form))]
                               {:binding-id (sha (+ 2000 (Long/parseLong (subs (:form-id form) 7) 16)))
                                :binding-class :lexical :kind :local :name (:value form)
                                :scope-id (sha 5000) :definition-syntax-id (:syntax-id form)})]
                {:module {:namespace 'demo.map :profile :meta :target :jvm
                          :effects #{} :capabilities #{} :safety :safe}
                 :lineage {:authenticated-sh06-artifact-id (sha 6000) :sh06-semantic-projection-id (sha 6001)}
                 :internal-indexes {:forms {:by-id @forms}
                                    :bindings {:by-definition (into {} (map (juxt :definition-syntax-id identity) bindings))
                                               :by-id (into {} (map (juxt :binding-id identity) bindings))}}}))]
      (doseq [value (concat upstream-cases
                           [(vec (repeat 257 '_))
                            (into {} (map (fn [n] [(keyword (str "wide" n)) '_]) (range 257)))
                            (nth (iterate vector '_) 65)])]
        (let [match-form (make! (list 'match 0 value 1 '_ 2))
              input (request)
              pattern (get @forms (nth (:child-form-ids match-form) 2))
              lowered (invoke 'sh07-lower-match [input (invoke 'sh07-empty-state []) match-form])
              state (:state lowered)
              records (:match-pattern-records state)
              digests (mapv sha (range 7000 7004))
              resolved (invoke 'sh07-resolve-match-pattern-vector [records digests])
              node (last (:nodes state))
              resolve-id #(get digests (:ordinal %))
              nodes (mapv #(-> % (update :node-id resolve-id) (update :children (fn [ids] (mapv resolve-id ids)))
                               (update-in [:evaluation :order]
                                          (fn [entries] (mapv (fn [entry] (update entry :core-node-id resolve-id)) entries))))
                          (:nodes state))
              resolved-node (last nodes)
              branches (:value (invoke 'sh07-resolve-match-branch-vector [(:match-branch-records state) digests]))
              skeleton (:value (invoke 'sh07-resolve-match-decision-skeleton [(first (:match-decision-skeletons state)) digests]))
              indexes {:patterns (into {} (map (fn [record] [[(:node-id resolved-node) (:pattern-form-id record)] record]) (:value resolved)))
                       :branches (into {} (map (fn [record] [[(:node-id resolved-node) (:clause-ordinal record)] record]) branches))}]
          (is (= :accepted (:status lowered)))
          (is (= [:literal :literal :literal :match] (mapv :core-form (:nodes state))))
          (is (= :accepted (:status resolved)))
          (is (true? (invoke 'sh07-resolved-b11-node-valid? [resolved-node])))
          (is (bootstrap/sh07-b11-match-group-coherent? (into {} (map (juxt :node-id identity) nodes))
                                                       [] resolved-node branches skeleton (:value resolved)))
          (when (= value '{:id x :name y})
            (doseq [bad-value [0 nil []]]
              (let [bad-records (assoc-in (:value resolved) [0 :pattern-value] bad-value)
                    node-by-id (into {} (map (juxt :node-id identity) nodes))
                    by-ordinal (into {} (map (juxt :ordinal identity) bad-records))]
                (is (false? (invoke 'sh07-match-pattern-graph-complete? [bad-records])))
                (doseq [operation [#(bootstrap/sh07-b11-match-pattern-graph-complete? bad-records)
                                   #(bootstrap/sh07-b11-match-group-coherent?
                                     node-by-id [] resolved-node branches skeleton bad-records)
                                   #(bootstrap/sh07-b11-match-pattern-record-coherent?
                                     node-by-id [] (into {} (map (juxt :clause-ordinal identity) branches))
                                     (second bad-records) by-ordinal)]]
                  (let [result (diagnostic-result operation)]
                    (is (nil? (:raw-host-error result)))
                    (is (false? (:value result))))))))
          (when-let [binding (first (filter #(= :binding (:pattern-kind %)) (:value resolved)))]
            (is (false? (invoke 'sh07-source-core-map-pattern-join?
                                [input (get @forms (:pattern-form-id binding)) resolved-node
                                 (assoc-in indexes [:patterns [(:node-id resolved-node) (:pattern-form-id binding)] :pattern-binding-id]
                                           (sha 9999))]))))
          (doseq [record records :when (= :map (:pattern-kind record))]
            (let [form (get @forms (:pattern-form-id record))
                  child-records (filter #(= (:ordinal record) (:parent-ordinal %)) records)]
              (is (= (:value form) (:pattern-value record)))
              (is (nil? (:vector-width record)))
              (is (= (count (:value form)) (count child-records)))
              (is (= (set (range 1 (count (:child-form-ids form)) 2))
                     (set (map #(peek (:path %)) child-records))))
              (doseq [child child-records]
                (let [coordinate (peek (:path child))
                      key-form (get @forms (nth (:child-form-ids form) (dec coordinate)))
                      value-form (get @forms (nth (:child-form-ids form) coordinate))]
                  (is (= :keyword (:kind key-form)))
                  (is (= (get (:value form) (:value key-form)) (:value value-form)))
                  (is (= (:form-id value-form) (:pattern-form-id child)))))
              (doseq [coordinate (range 0 (count (:child-form-ids form)) 2)]
                (let [key-form (get @forms (nth (:child-form-ids form) coordinate))]
                  (is (= :match-pattern (get-in state [:form-dispositions (:form-id key-form) :reason])))
                  (is (= [] (get-in state [:form-dispositions (:form-id key-form) :emitted-node-ids])))
                  (is (true? (invoke 'sh07-source-core-map-pattern-join? [input key-form resolved-node indexes])))))))
          (let [map-record (first (filter #(and (= :map (:pattern-kind %)) (seq (:pattern-value %))) (:value resolved)))]
            (when map-record
              (let [form (get @forms (:pattern-form-id map-record))
                    key-id (first (:child-form-ids form))
                    key-form (get @forms key-id)
                    bad (assoc-in input [:internal-indexes :forms :by-id key-id :value] :foreign)]
                (is (= :map-pattern-source-shape
                       (get-in (invoke 'sh07-match-pattern-descriptor [bad form 0]) [:facts :reason])))
                (is (false? (invoke 'sh07-source-core-map-pattern-join? [bad (assoc key-form :value :foreign) resolved-node indexes])))
                (when-let [other (first (filter #(and (= :map (:pattern-kind %))
                                                     (not= (:pattern-form-id map-record) (:pattern-form-id %))) (:value resolved)))]
                  (let [foreign-key (assoc key-form :parent-form-id (:pattern-form-id other))
                        foreign-input (assoc-in input [:internal-indexes :forms :by-id key-id] foreign-key)]
                    (is (false? (invoke 'sh07-source-core-map-pattern-join?
                                        [foreign-input foreign-key resolved-node indexes]))))))))
          (let [map-record (first (filter #(= :map (:pattern-kind %)) records))
                map-form (get @forms (:pattern-form-id map-record))
                children (filterv #(= (:ordinal map-record) (:parent-ordinal %)) records)]
            (when (and map-record (seq children))
              (let [child (first children)
                    mutant (assoc-in records [(:ordinal child) :path (dec (count (:path child)))]
                                     (dec (peek (:path child))))]
                (is (= :rejected (:status (invoke 'sh07-resolve-match-pattern-vector [mutant digests]))))
                (is (false? (invoke 'sh07-match-pattern-graph-complete?
                                    [(vec (remove #(= (:ordinal child) (:ordinal %)) records))]))))
                (is (= :map-pattern-source-shape
                       (get-in (invoke 'sh07-match-pattern-descriptor
                                       [(update-in input [:internal-indexes :forms :by-id (:form-id map-form) :child-form-ids] pop)
                                        (update map-form :child-form-ids pop) 0]) [:facts :reason])))))
            ;; New Core-only cases do not claim adoption of the separately
            ;; owned unmetered resolver. Retain its existing composition cases.
            (when (some #{value} upstream-cases)
              (is (true? (bootstrap/sh06-fixed-vector-pattern? value)))
              (is (= (count (:bindings (invoke 'sh07-match-pattern-descriptor [input pattern 0])))
                     (count (bootstrap/sh06-unique-match-binding-paths value [])))))))
      (let [form (make! (into {} (map (fn [n] [(keyword (str "edge" n)) n]) (range 256))))
            result (invoke 'sh07-match-pattern-descriptor [(request) form 0])]
        (is (= 512 (count (:child-form-ids form))))
        (is (= :accepted (:status result)))
        (is (= 257 (count (:records result))))
        (is (= [] (:bindings result))))
      (doseq [[value reason] [['{symbol x} :non-keyword-map-pattern-key-deferred]
                              ['{:nested {:when true :id x}} :guard-pattern-deferred]
                              ['{:a x :b x} :duplicate-match-binding]
                              ['{:id (Ok x)} :unsupported-vector-pattern-leaf]
                              ['{:id [x & y]} :variable-width-vector-pattern-deferred]]]
        (let [form (make! value)
              result (invoke 'sh07-match-pattern-descriptor [(request) form 0])]
          (is (= :rejected (:status result)))
          (is (= (if (= reason :duplicate-match-binding) "C6-VERIFY" "C6-LOWERING-GAP") (:rule result)))
          (is (= reason (get-in result [:facts :reason]))))))))

(deftest sh07-b11-keyword-map-authentic-composition
  (let [artifact (file-artifact "accepted" "recursive-map-patterns" ".gravity")
        boundary (:gravity-core-boundary artifact)
        input (request artifact)
        canonical (core artifact)
        node (first (filter #(= :match (:core-form %)) (:nodes canonical)))
        records (:match-pattern-records canonical)
        maps (filter #(= :map (:pattern-kind %)) records)
        forms (exactly-once-index (:forms input) :form-id)
        mapping (execute-core-function "recursive-map-patterns" 'sh07-resolve-source-core-map-preimage
                                       [(:preimage (last (:digest-requests boundary))) (pop (:resolved-digests boundary))])
        rows (get-in mapping [:value :records])
        row-index (exactly-once-index rows #(get-in % [:source :form-id]))
        references (set (map :reference-syntax-id (:resolution-table input)))
        definitions (set (map :definition-syntax-id (:binding-table input)))]
    (is (= :passed (:status (bootstrap/sh07-core-artifact-verification artifact))))
    (is (= :accepted (:status mapping)))
    (is (= :development-unadmitted (get-in boundary [:plan-binding :revision-status])))
    (is (false? (get-in boundary [:plan-binding :self-hosted?])))
    (is (contains? (set (get-in artifact [:execution-boundary :pending-lowering-families]))
                   :non-keyword-map-list-set-record-constructor-patterns))
    (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
    (is (false? (get-in artifact [:execution-boundary :self-hosted?])))
    (is (= 4 (count maps)))
    (is (= #{0 1 3 12} (set (map #(count (:pattern-value %)) maps))))
    (is (= #{:map :vector :literal :binding :wildcard} (set (map :pattern-kind records))))
    (is (= ['ready '_] (mapv :pattern-value (filter #(= :literal (:pattern-kind %)) records))))
    (is (= [(first (:children node))] (mapv :core-node-id (get-in node [:evaluation :order]))))
    (is (= :pending-sh08 (get-in node [:attributes :exhaustiveness]) (get-in node [:attributes :result-type-join])))
    (is (every? #(= :conditionally-evaluate-selected-branch (:branch-evaluation %)) (:match-branch-records canonical)))
    (doseq [record maps]
      (let [form (get forms (:pattern-form-id record))
            ids (:child-form-ids form)
            children (filterv #(= (:ordinal record) (:parent-ordinal %)) records)]
        (is (= (:value form) (:pattern-value record)))
        (is (nil? (:vector-width record)))
        (is (= (count (:value form)) (count children)))
        (is (= (set (range 1 (count ids) 2)) (set (map #(peek (:path %)) children))))
        (doseq [coordinate (range 0 (count ids) 2)]
          (let [key-form (get forms (nth ids coordinate))
                value-form (get forms (nth ids (inc coordinate)))
                child (first (filter #(= (:form-id value-form) (:pattern-form-id %)) children))
                row (get row-index (:form-id key-form))]
            (is (= :keyword (:kind key-form)))
            (is (= (get (:value form) (:value key-form)) (:value value-form)))
            (is (= (conj (:path record) (inc coordinate)) (:path child)))
            (is (= :consumed (:disposition row)))
            (is (= :match-pattern (:reason row)))
            (is (= [] (:emitted-node-ids row)))
            (is (= [(:node-id node)] (:anchor-node-ids row)))
            (is (not (contains? references (:syntax-id key-form))))
            (is (not (contains? definitions (:syntax-id key-form))))))))
    (doseq [record records :when (= :binding (:pattern-kind record))]
      (is (= 1 (count (:pattern-binding-use-syntax-ids record))))
      (is (= (:pattern-binding-use-syntax-ids record)
             (mapv :syntax-id (filter #(= (:pattern-binding-id record) (:binding-id %)) (:reference-uses canonical))))))))

(deftest sh07-b11-keyword-map-forgeries-fail-closed
  (let [artifact (file-artifact "accepted" "recursive-map-patterns" ".gravity")
        input (request artifact)
        canonical (core artifact)
        records (:match-pattern-records canonical)
        root (first (filter #(= :map (:pattern-kind %)) records))
        form (first (filter #(= (:pattern-form-id root) (:form-id %)) (:forms input)))
        ids (:child-form-ids form)
        binding (first (filter #(and (= (:ordinal root) (:parent-ordinal %)) (= :binding (:pattern-kind %))) records))
        wrong "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        alter-form (fn [f] (update input :forms #(mapv (fn [item] (if (= (:form-id form) (:form-id item)) (f item) item)) %)))
        mutations [(alter-form #(assoc % :child-form-ids (assoc ids 1 (nth ids 3) 3 (nth ids 1))))
                   (alter-form #(update % :child-form-ids pop))
                   (update input :binding-table #(mapv (fn [item] (if (= (:pattern-binding-id binding) (:binding-id item))
                                                                 (assoc item :scope-id wrong) item)) %))]]
    (doseq [mutant mutations]
      (let [result (diagnostic-result #(bootstrap/sh07-core-from-authenticated-request (:sh06-resolution-artifact artifact) mutant))
            diagnostic (diagnostic-data result)]
        (is (nil? (:raw-host-error result)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :authenticated-sh06-request-membership-mismatch (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))
    (let [mutant (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact
                                    :match-pattern-records (:ordinal binding) :pattern-binding-id] wrong)
          failed (verification-failures mutant artifact)]
      (is (contains? failed :canonical-core-replays?))
      (is (contains? failed :authoritative-products-replay?)))
    (let [mutant (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact
                                    :match-pattern-records (:ordinal root) :pattern-value] 0)
          result (diagnostic-result #(verification-failures mutant artifact))]
      (is (nil? (:raw-host-error result)))
      (is (contains? (:value result) :canonical-core-replays?)))
    (let [coordinate (peek (:path binding))
          key-form (first (filter #(= (nth ids (dec coordinate)) (:form-id %)) (:forms input)))
          value (assoc (:pattern-value root) (:value key-form) 'substituted)
          node-index (first (keep-indexed #(when (= (:core-node-id root) (:node-id %2)) %1) (:nodes canonical)))
          mutant-core (-> canonical
                          (assoc-in [:match-pattern-records (:ordinal root) :pattern-value] value)
                          (assoc-in [:match-pattern-records (:ordinal binding) :pattern-value] 'substituted)
                          (assoc-in [:match-branch-records (:clause-ordinal root) :pattern-value] value)
                          (assoc-in [:nodes node-index :attributes :branch-clauses (:clause-ordinal root) :pattern-value] value))
          mutant (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact] mutant-core)
          node (get-in mutant-core [:nodes node-index])
          failed (verification-failures mutant artifact)]
      (is (bootstrap/sh07-b11-match-group-coherent? (exactly-once-index (:nodes mutant-core) :node-id)
                                                   (:reference-uses mutant-core) node (:match-branch-records mutant-core)
                                                   (first (:match-decision-skeletons mutant-core)) (:match-pattern-records mutant-core)))
      (is (contains? failed :canonical-core-replays?))
      (is (contains? failed :authoritative-products-replay?)))
    (let [boundary (:gravity-core-boundary artifact)
          d (:digest-requests boundary) z (:resolved-digests boundary)
          rc (:value (execute-core-function "recursive-map-patterns" 'sh07-resolve-core-template
                                           [(get-in boundary [:raw-template-result :core-template]) z]))
          rows (get-in (last d) [:preimage :records])]
      (doseq [altered [(vec (remove #(= (first ids) (get-in % [:source :form-id])) rows))
                       (mapv #(if (= (first ids) (get-in % [:source :form-id])) (assoc % :reason :core-node-source) %) rows)]]
        (is (= :rejected (:status (execute-core-function "recursive-map-patterns" 'sh07-verify-source-core-map
                                                       [input rc (assoc-in d [(dec (count d)) :preimage :records] altered) z]))))))))

(deftest sh07-b11-keyword-map-source-rejections
  (doseq [basename ["map-non-keyword-key" "duplicate-map-bindings" "map-unsupported-leaf"
                    "map-branch-binding-escape" "map-sibling-binding-escape" "guard-pattern-deferred"]]
    (let [path (fixture-path "rejected" basename ".gravity")
          oracle (fixture-oracle basename ".gravity")
          result (diagnostic-result #(bootstrap/sh07-core-file-artifact path))
          data (:exception-data result)
          diagnostic (if (= "C5-UNRESOLVED" (:expected-rule oracle)) (or (:diagnostic data) data)
                         (diagnostic-data result))]
      (is (nil? (:raw-host-error result)))
      (is (= (:expected-rule oracle) (or (:rule diagnostic) (:id diagnostic))))
      (is (= (:expected-stage oracle) (:stage diagnostic)))
      (when (:expected-reason oracle)
        (is (= (:expected-reason oracle) (get-in diagnostic [:facts :reason])))
        (is (= (:expected-remediation oracle) (:remediation diagnostic)))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b11-keyword-map-promotions-and-vector-regression
  (doseq [basename ["keyword-map-patterns" "nested-map-in-vector" "map-tail-recur" "nested-vector-patterns"]]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          canonical (core artifact)]
      (is (= :accepted (:status artifact)))
      (is (= :passed (get-in artifact [:gravity-core-boundary :resolved-verification :status])))
      (is (seq (:match-pattern-records canonical)))
      (if (= basename "nested-vector-patterns")
        (is (= #{:vector :binding :literal :wildcard} (set (map :pattern-kind (:match-pattern-records canonical)))))
        (is (some #(= :map (:pattern-kind %)) (:match-pattern-records canonical))))
      (when (= basename "map-tail-recur")
        (is (= 1 (count (:recur-transfers canonical))))))))

(deftest sh07-b11-fixtures-are-paired-byte-identical-and-bounded-in-scope
  (doseq [family ["accepted" "rejected"]]
    (let [expected
          (if (= family "accepted")
            accepted-fixtures
            rejected-fixtures)]
      (is (= expected
             (fixture-basenames family ".gravity")
             (fixture-basenames family ".qst")))
      (doseq [basename expected]
        (is (= (seq (source-bytes
                     (fixture-path family basename ".gravity")))
               (seq (source-bytes
                     (fixture-path family basename ".qst"))))))))
  (doseq [required-name
          ["leaf" "nested" "branch-local" "scrutinee-once" "tail-recur"]]
    (is (some #(str/includes? % required-name) accepted-fixtures)))
  (doseq [required-name ["list" "map" "set" "rest"]]
    (is (some #(str/includes? % required-name) rejected-fixtures)))
  (is (= :duplicate-match-binding
         (:expected-reason (fixture-oracle "duplicate-map-bindings" ".gravity"))))
  (is (not-any? #(str/includes? % "variable-width") accepted-fixtures)))

(deftest sh07-b11-gravity-contract-names-l7
  (let [forms
        (source-forms "bootstrap/gravity/src/gravity/checked_core.gravity")
        contract-form
        (first
         (filter
          #(and (seq? %)
                (= 'def (first %))
                (= 'sh07-core-contract (second %)))
          forms))
        contract-documents
        (set (:governing-contracts (nth contract-form 2)))]
    (is (seq contract-form))
    (is (contains? contract-documents "L7"))))

(deftest sh07-b11-direct-and-public-routing-use-v12
  (doseq [extension extensions]
    (let [basename "vector-leaf-patterns"
          direct (direct-artifact "accepted" basename extension)
          public (file-artifact "accepted" basename extension)]
      (testing (str basename extension)
        (is (= :accepted (:status direct) (:status public)))
        (is (= (:artifact-id direct) (:artifact-id public)))
        (is (= (identity-input direct) (identity-input public)))
        (is (= (:match-pattern-records (core direct))
               (:match-pattern-records (core public))))
        (is (= 15 (:schema-version (request direct))
               (:schema-version (request public))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request direct))
               (:scope (request public))))
        (is (= "SH-07-B47" (:task direct) (:task public)))
        (is (= :c6-gravity-core-lowering-b47
               (get-in direct [:pass :name])
               (get-in public [:pass :name])))
        (is (= :gravity/sh07-to-c6-core-products-v16
               (get-in direct
                       [:gravity-core-boundary :adapter-contract])
               (get-in public
                       [:gravity-core-boundary :adapter-contract])))))))

(deftest sh07-b11-preserves-b10-scalar-pattern-products
  (doseq [extension extensions]
    (let [artifact
          (b10-file-artifact "literal-clauses-wildcard" extension)
          core-artifact (core artifact)]
      (testing extension
        (is (= :accepted (:status artifact)))
        (is (= 15 (:schema-version (request artifact))))
        (is (seq (:match-branch-records core-artifact)))
        (is (seq (:match-decision-skeletons core-artifact)))
        (is (= (count (:match-branch-records core-artifact))
               (count (:match-pattern-records core-artifact))))
        (is (= #{:literal :wildcard}
               (set (map :pattern-kind
                         (:match-branch-records core-artifact)))
               (set (map :pattern-kind
                         (:match-pattern-records core-artifact)))))
        (is (every? #(and (= 0 (:depth %))
                          (= [] (:path %))
                          (nil? (:parent-ordinal %)))
                    (:match-pattern-records core-artifact)))))))

(deftest sh07-b11-pattern-records-are-preorder-bounded-and-parent-linked
  (doseq [basename accepted-fixtures
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:match-pattern-records core-artifact)
          record-by-ordinal
          (exactly-once-index records :ordinal)]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (is (= (set (keys (match-groups artifact)))
               (set (keys (pattern-groups artifact)))))
        (doseq [[[match-id clause-ordinal] clause-records]
                (pattern-clause-groups artifact)]
          (let [ordered (vec (sort-by :ordinal clause-records))
                root-record (first ordered)
                root-ordinal (:ordinal root-record)]
            (is (= (vec (range (count ordered)))
                   (mapv :local-ordinal ordered)))
            (is (= #{match-id} (set (map :core-node-id ordered))))
            (is (= #{clause-ordinal}
                   (set (map :clause-ordinal ordered))))
            (is (= root-ordinal (:root-ordinal root-record)))
            (is (= 0 (:root-local-ordinal root-record)
                   (:local-ordinal root-record)
                   (:depth root-record)))
            (is (= [] (:path root-record)))
            (is (nil? (:parent-ordinal root-record)))
            (is (nil? (:parent-local-ordinal root-record)))
            (doseq [record ordered]
              (is (= match-pattern-record-keys
                     (set (keys record))))
              (is (= root-ordinal (:root-ordinal record)))
              (is (= (:local-ordinal record)
                     (- (:ordinal record) root-ordinal)))
              (is (= (:depth record) (count (:path record))))
              (is (<= 0 (:depth record) 64))
              (is (every? #(and (integer? %) (not (neg? %)))
                          (:path record)))
              (if-some [parent-ordinal (:parent-ordinal record)]
                (let [parent (get record-by-ordinal parent-ordinal)]
                  (is (contains? #{:vector :map} (:pattern-kind parent)))
                  (is (= (:local-ordinal parent)
                         (:parent-local-ordinal record)))
                  (is (= (inc (:depth parent)) (:depth record)))
                  (is (= (:path parent) (pop (:path record))))
                  (is (if (= :map (:pattern-kind parent))
                        (and (odd? (peek (:path record)))
                             (< (peek (:path record)) (* 2 (count (:pattern-value parent)))))
                        (< (peek (:path record)) (:vector-width parent)))))
                (is (= record root-record)))
              (if (= :vector (:pattern-kind record))
                (let [children
                      (filterv
                       #(= (:ordinal record) (:parent-ordinal %))
                       ordered)]
                  (is (nil? (:pattern-value record)))
                  (is (<= 0 (:vector-width record) 256))
                  (is (= (:vector-width record) (count children))))
                (is (nil? (:vector-width record)))))))))))

(deftest sh07-b11-nested-vector-records-preserve-source-order-and-kinds
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "nested-vector-patterns" extension)
          records (:match-pattern-records (core artifact))
          vector-records (filterv #(= :vector (:pattern-kind %)) records)
          nested-records (filterv #(pos? (:depth %)) records)]
      (testing extension
        (is (< 1 (count vector-records)))
        (is (seq nested-records))
        (is (some #(= 2 (:depth %)) records))
        (is (= #{:binding :literal :vector :wildcard}
               (set (map :pattern-kind records))))
        (doseq [[[_ clause-ordinal] clause-records]
                (pattern-clause-groups artifact)]
          (let [ordered (vec (sort-by :ordinal clause-records))]
            (is (= (if (< clause-ordinal 2)
                     [[] [0] [0 0] [0 1] [1] [1 0] [1 1]]
                     [[]])
                   (mapv :path ordered)))))))))

(deftest sh07-b11-vector-bindings-are-unique-branch-local-and-use-linked
  (doseq [basename ["nested-vector-patterns"
                    "vector-branch-local-binding"]
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          references (:reference-uses core-artifact)
          binding-records
          (filterv #(= :binding (:pattern-kind %))
                   (:match-pattern-records core-artifact))]
      (testing (str basename extension)
        (is (seq binding-records))
        (doseq [[_ clause-records] (pattern-clause-groups artifact)]
          (let [bindings
                (filterv #(= :binding (:pattern-kind %))
                         clause-records)]
            (is (= (count bindings)
                   (count (set (map :pattern-value bindings)))))
            (is (= (count bindings)
                   (count (set (map :pattern-binding-id bindings)))))))
        (doseq [record binding-records]
          (is (symbol? (:pattern-value record)))
          (is (not= '_ (:pattern-value record)))
          (is (string? (:pattern-binding-id record)))
          (is (string? (:pattern-binding-scope-id record)))
          (doseq [use-id (:pattern-binding-use-syntax-ids record)]
            (is (= 1
                   (count
                    (filter
                     #(and (= use-id (:syntax-id %))
                           (= (:pattern-binding-id record)
                              (:binding-id %)))
                     references))))))))))

(deftest sh07-b11-scrutinee-is-once-and-vector-branches-are-conditional
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "vector-scrutinee-once" extension)
          core-artifact (core artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          transfers (:error-transfers core-artifact)]
      (testing extension
        (is (= 2 (count transfers)))
        (doseq [[match-id records] (match-groups artifact)]
          (let [ordered (vec (sort-by :clause-ordinal records))
                match-node (get nodes match-id)
                scrutinee-id (first (:children match-node))
                branch-transfers
                (filterv #(= :match-branch
                             (get-in % [:evaluation-region :kind]))
                         transfers)]
            (is (= 1 (count (filter #{scrutinee-id}
                                    (:children match-node)))))
            (is (= #{scrutinee-id}
                   (set (map :scrutinee-core-node-id ordered))))
            (is (= #{:evaluate-scrutinee-once}
                   (set (map :scrutinee-evaluation ordered))))
            (is (= #{:conditionally-evaluate-selected-branch}
                   (set (map :branch-evaluation ordered))))
            (is (= 1 (count branch-transfers)))
            (is (= #{(:conditional-region (first ordered))}
                   (set (map :evaluation-region
                             branch-transfers))))))))))

(deftest sh07-b11-vector-branch-preserves-tail-recur-target
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "vector-tail-recur" extension)
          core-artifact (core artifact)
          targets (exactly-once-index (:recur-targets core-artifact) :target-id)
          transfers (:recur-transfers core-artifact)]
      (testing extension
        (is (= 1 (count transfers)))
        (let [transfer (first transfers)
              target (get targets (:target-id transfer))]
          (is (= :loop (:target-kind transfer)
                 (:target-kind target)))
          (is (true? (:tail-position transfer)))
          (is (= :nearest-lexical-recur-target
                 (:transfer-policy transfer)))
          (is (= :pending-sh08 (:type-compatibility transfer))))))))

(deftest sh07-b11-identities-are-deterministic-path-neutral-and-provenanced
  (let [fixture
        (fixture-path "accepted" "nested-vector-patterns" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b11-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/vector.gravity")
        right-path (.resolve temp-root "right/vector.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left ((required-var 'sh07-core-file-artifact) (str left-path))
            repeated
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right ((required-var 'sh07-core-file-artifact) (str right-path))]
        (is (= :accepted (:status left) (:status right)))
        (is (= left repeated))
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= (:match-pattern-records (core left))
               (:match-pattern-records (core right))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in (core left)
                       [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in (core right)
                       [:provenance :actual-source-path])))
        (is (not= (get-in left [:provenance :source-path])
                  (get-in right [:provenance :source-path]))))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b11-pattern-products-bind-to-the-authenticated-sh06-lineage
  (doseq [basename accepted-fixtures
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          lineage (:lineage (request artifact))]
      (doseq [record (:match-pattern-records (core artifact))]
        (is (= (:authenticated-sh06-artifact-id lineage)
               (:authenticated-sh06-artifact-id record)))
        (is (= (:sh06-semantic-projection-id lineage)
               (:sh06-semantic-projection-id record)))))))

(deftest sh07-b11-pattern-product-alterations-fail-replay
  (let [artifact
        (file-artifact "accepted" "nested-vector-patterns" ".gravity")
        records (:match-pattern-records (core artifact))
        child-index
        (first
         (keep-indexed
          (fn [index record]
            (when (:parent-ordinal record) index))
          records))
        alterations
        {"record removal"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records]
          pop)
         "record order"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records]
          (vec (reverse records)))
         "path"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records child-index :path]
          [255])
         "parent"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records child-index :parent-ordinal]
          nil)
         "width"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :match-pattern-records 0 :vector-width]
          257)}]
    (is (integer? child-index))
    (doseq [[label altered] alterations]
      (testing label
        (let [failed (verification-failures altered artifact)]
          (is (not= artifact altered))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :match-pattern-records-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var 'sh07-core-artifact-verification)
                   altered)))))))))

(deftest sh07-b11-pattern-resolvers-enforce-record-shape-and-coordinates
  (let [basename "nested-vector-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        vector-record (first (filter #(= :vector (:pattern-kind %)) records))
        cases
        {"unexpected field"
         [(assoc vector-record :unexpected true)
          :match-pattern-record-shape]
         "negative ordinal"
         [(assoc vector-record :ordinal -1)
          :match-pattern-ordinal]
         "negative depth"
         [(assoc vector-record
                 :depth -1
                 :path [])
          :match-pattern-ordinal-or-depth]
         "negative width"
         [(assoc vector-record :vector-width -1)
          :match-pattern-value]}]
    (is (map? vector-record))
    (doseq [[label [record reason]] cases]
      (testing label
        (is (= {:status :rejected :reason reason}
               (execute-core-function
                basename
                'sh07-resolve-match-pattern-record
                [record resolved-digests])))))
    (is (= {:status :rejected
            :reason :match-pattern-vector-required}
           (execute-core-function
            basename
            'sh07-resolve-match-pattern-vector
            [:not-a-vector resolved-digests])))))

(deftest sh07-b11-pattern-resolver-rejects-invalid-parent-graphs
  (let [basename "nested-vector-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        child-index
        (first
         (keep-indexed
          (fn [index record]
            (when (:parent-ordinal record) index))
          records))
        parentless
        (assoc-in records [child-index :parent-ordinal] nil)
        outside-width
        (assoc-in records [child-index :path]
                  [(get-in records
                           [(:parent-ordinal (get records child-index))
                            :vector-width])])]
    (is (integer? child-index))
    (is (= :accepted
           (:status
            (execute-core-function
             basename
             'sh07-resolve-match-pattern-vector
             [records resolved-digests]))))
    (doseq [altered [parentless outside-width]]
      (is (= {:status :rejected
              :reason :match-pattern-parent-graph}
             (execute-core-function
              basename
              'sh07-resolve-match-pattern-vector
              [altered resolved-digests]))))))

(deftest sh07-b11-pattern-graph-completeness-rejects-structural-alterations
  (let [basename "vector-leaf-patterns"
        artifact (file-artifact "accepted" basename ".gravity")
        raw-records
        (get-in artifact
                [:gravity-core-boundary :raw-template-result
                 :core-template :match-pattern-records])
        resolved-digests
        (get-in artifact [:gravity-core-boundary :resolved-digests])
        vector-root
        (first
         (filter
          #(and (= :vector (:pattern-kind %))
                (= 2 (:vector-width %))
                (nil? (:parent-ordinal %)))
          raw-records))
        direct-children
        (filterv #(= (:ordinal vector-root) (:parent-ordinal %))
                 raw-records)
        first-child (first direct-children)
        second-child (second direct-children)
        inflated-width
        (assoc-in raw-records
                  [(:ordinal vector-root) :vector-width]
                  3)
        missing-child-path
        [(assoc vector-root
                :ordinal 0
                :local-ordinal 0
                :root-ordinal 0)
         (assoc first-child
                :ordinal 1
                :local-ordinal 1
                :parent-ordinal 0
                :root-ordinal 0)]
        duplicate-child-path
        (assoc-in raw-records
                  [(:ordinal second-child) :path]
                  (:path first-child))
        duplicate-root
        (assoc raw-records
               (:ordinal second-child)
               (assoc second-child
                      :local-ordinal 0
                      :parent-local-ordinal nil
                      :parent-ordinal nil
                      :root-ordinal (:ordinal second-child)
                      :depth 0
                      :path []))
        resolver-cases
        {"inflated vector width" inflated-width
         "missing child path" missing-child-path
         "duplicate child path" duplicate-child-path
         "duplicate root in one match clause" duplicate-root}
        canonical (core artifact)
        canonical-records (:match-pattern-records canonical)
        canonical-root
        (first
         (filter
          #(and (= :vector (:pattern-kind %))
                (= 2 (:vector-width %))
                (nil? (:parent-ordinal %)))
          canonical-records))
        canonical-children
        (filterv
         #(= (:ordinal canonical-root) (:parent-ordinal %))
         canonical-records)
        canonical-first-child (first canonical-children)
        canonical-second-child (second canonical-children)
        coherence-cases
        {"inflated vector width"
         (assoc-in canonical-records
                   [(:ordinal canonical-root) :vector-width]
                   3)
         "missing child path"
         (vec (remove #(= (:ordinal canonical-second-child)
                          (:ordinal %))
                      canonical-records))
         "duplicate child path"
         (assoc-in canonical-records
                   [(:ordinal canonical-second-child) :path]
                   (:path canonical-first-child))
         "duplicate root in one match clause"
         (assoc canonical-records
                (:ordinal canonical-second-child)
                (assoc canonical-second-child
                       :local-ordinal 0
                       :parent-local-ordinal nil
                       :parent-ordinal nil
                       :root-ordinal (:ordinal canonical-second-child)
                       :depth 0
                       :path []))}]
    (is (map? vector-root))
    (is (= 2 (count direct-children)))
    (doseq [[label altered] resolver-cases]
      (testing (str "Gravity resolver: " label)
        (is (= {:status :rejected
                :reason :match-pattern-incomplete-graph}
               (execute-core-function
                basename
                'sh07-resolve-match-pattern-vector
                [altered resolved-digests])))))
    (doseq [[label altered-records] coherence-cases]
      (testing (str "Clojure coherence: " label)
        (is (false?
             ((required-var 'sh07-core-match-products-coherent?)
              (assoc canonical
                     :match-pattern-records altered-records))))))))

(deftest sh07-b11-public-proof-is-bounded-and-honest
  (doseq [basename accepted-fixtures]
    (let [artifact (file-artifact "accepted" basename ".gravity")
          proof (:capability-based-proof artifact)
          boundary (:gravity-core-boundary artifact)
          canonical (:canonical-core-artifact boundary)
          bounds (get-in boundary [:raw-template-result :bounds])
          pending
          (set (get-in artifact
                       [:execution-boundary
                        :pending-lowering-families]))]
      (testing basename
        (is (= :accepted (:status artifact)))
        (is (= :SH-07 (:slice artifact)))
        (is (contains? (set (:document-set artifact)) "L7"))
        (is (= 1024 (:maximum-match-pattern-records bounds)))
        (is (= 64 (:maximum-match-pattern-depth bounds)))
        (is (= 256 (:maximum-match-pattern-width bounds)))
        (is (<= (count (:match-pattern-records canonical)) 1024))
        (is (= :gravity/sh07-to-c6-core-products-v16
               (:adapter-contract boundary)))
        (is (= :gravity/sh07-core-capability-proof
               (:artifact proof)))
        (is (= :complete (:status proof)))
        (is (= [] (:failed-checks proof)))
        (is (true? (:match-pattern-records-replay? proof)))
        (is (= :passed
               (get-in boundary [:template-verification :status])))
        (is (= :passed
               (get-in boundary [:resolved-verification :status])))
        (is (not (contains? pending :fixed-width-vector-patterns)))
        (is (contains? pending :variable-width-vector-patterns))
        (is (contains? pending :non-keyword-map-list-set-record-constructor-patterns))
        (is (contains? pending :duplicate-pattern-binding-policy))
        (is (contains? pending :guard-patterns))
        (is (= [:types :effects :ownership :safety]
               (:pending-fact-families canonical)))))))

(deftest sh07-b11-rejections-are-structured-and-oracle-bound
  (let [observed-reasons (atom #{})]
    (doseq [basename (set/difference rejected-fixtures map-upstream-rejections)
            extension extensions]
      (testing (str basename extension)
        (let [source-path (fixture-path "rejected" basename extension)
              declared (fixture-oracle basename extension)
              result
              (diagnostic-result
               #((required-var 'sh07-core-file-artifact) source-path))
              diagnostic (diagnostic-data result)]
          (swap! observed-reasons conj (:expected-reason declared))
          (is (map? declared))
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
          (is (= (:expected-rule declared) (:rule diagnostic)))
          (is (= (:expected-stage declared) (:stage diagnostic)))
          (is (= (:expected-severity declared) (:severity diagnostic)))
          (is (= (:expected-reason declared)
                 (get-in diagnostic [:facts :reason])))
          (is (= (:expected-remediation declared)
                 (:remediation diagnostic)))
          (is (= source-path (get-in diagnostic [:source-span :source])))
          (is (true? (get-in diagnostic [:facts :fail-closed]))))))
    (is (= #{:composite-pattern-deferred
             :guard-pattern-deferred
             :unsupported-vector-pattern-leaf
             :variable-width-vector-pattern-deferred
             :vector-pattern-width-bound
             :non-keyword-map-pattern-key-deferred :duplicate-match-binding}
           @observed-reasons))))
