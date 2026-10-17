(ns gravity.self-hosting.sh07-mutation-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource "gravity/self_hosting/sh07_mutation_test.clj")]
    (when-not resource
      (throw (ex-info "SH-07-B6 test source is not on the classpath"
                      {:id "SH07-B6-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH07-B6-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-07-b6")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  ["ordered-top-level-mutations"
   "function-var-mutation"
   "conditional-mutations"
   "nested-value-mutation"
   "qualified-set-target"])
(def ^:private shape-remediation
  "Provide a bounded, delimiter-linked SH-06 form graph with exact core-form shape.")
(def ^:private gap-remediation
  "Use only the declared bounded SH-07 core subset; defer unsupported lowering families to their owning slices.")
(def ^:private recur-remediation
  "Place recur in tail position inside the nearest loop or fixed-arity function target and pass exactly the target arity.")
(def ^:private rejected-oracles
  {"set-arity"
   {:rule "C6-CORE-SHAPE" :reason :set-arity
    :remediation shape-remediation}
   "non-symbol-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :non-symbol-set-target-deferred
    :remediation gap-remediation}
   "lexical-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :lexical-set-target-deferred
    :remediation gap-remediation}
   "external-qualified-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :qualified-set-target-deferred
    :remediation gap-remediation}
   "alias-qualified-set-target"
   {:rule "C6-LOWERING-GAP"
    :reason :qualified-set-target-deferred
    :remediation gap-remediation}
   "recur-in-set-value"
   {:rule "C6-VERIFY"
    :reason :recur-tail-position-required
    :semantic-rule "L2-RECUR-TARGET"
    :remediation recur-remediation}})
(def ^:private mutation-record-keys
  #{:ordinal :core-node-id :form-id :syntax-id
    :target-form-id :target-syntax-id :target-symbol
    :target-binding-id :target-binding-class
    :target-definition-kind :target-location
    :target-upstream-binding-id :target-namespace
    :target-definition-syntax-id
    :target-definition-artifact-id :resolution-order
    :value-core-node-id :target-evaluated-children
    :evaluated-children :evaluation-region
    :evaluation-owner-function-syntax-id
    :ordered-steps :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :ownership-legality :safety-classification})
(def ^:private mutation-attribute-keys
  #{:target-symbol :target-binding-id
    :target-binding-class :target-definition-kind
    :target-location :target-upstream-binding-id
    :target-namespace :target-definition-syntax-id
    :target-definition-artifact-id
    :target-form-id :target-syntax-id
    :resolution-order :target-evaluation
    :value-child-index :evaluation-order
    :result-policy :required-effect
    :authenticated-sh06-artifact-id
    :sh06-semantic-projection-id
    :type-legality :effect-registry-legality
    :effect-profile-capability-legality
    :ownership-legality :safety-classification})

(defn- path
  [relative]
  (str (.resolve @root relative)))

(defn- fixture-path
  [family basename extension]
  (path (str fixture-root "/" family "/" basename extension)))

(defn- fixture-basenames
  [family extension]
  (let [directory (io/file (path (str fixture-root "/" family)))]
    (->> (.listFiles directory)
         (filter #(.isFile %))
         (map #(.getName %))
         (filter #(str/ends-with? % extension))
         (map #(subs % 0 (- (count %) (count extension))))
         set)))

(defn- source-bytes
  [source-path]
  (java.nio.file.Files/readAllBytes (.toPath (io/file source-path))))

(defn- source-text
  [source-path]
  (String. (source-bytes source-path)
           java.nio.charset.StandardCharsets/UTF_8))

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
        "Required SH-07-B6 coordinator adapter is absent"
        {:id "SH07-B6-ADAPTER-ABSENT"
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

(defn- map-order-kernel [value]
  ;; Normalized retained C2 occurrences. Each map's source order deliberately
  ;; reverses host entry order, without changing values or projected paths.
  (let [sources (atom []) next-id (atom 0)
        build (fn build [value parent]
                (let [id (keyword (str "c2-map-order-" (swap! next-id inc)))
                      metadata (meta value) wrapper? (seq metadata)
                      abbreviation? (and (seq? value) (= 2 (count value)) (= 'quote (first value)))
                      kind (cond wrapper? :metadata-wrapper abbreviation? :abbreviation :else (bootstrap/sh07-core-value-kind value))
                      children (cond wrapper? [metadata (with-meta value nil)]
                                     abbreviation? [(second value)]
                                     (map? value) (vec (mapcat identity (reverse (vec value))))
                                     (set? value) (vec (reverse (bootstrap/sh07-core-children value)))
                                     :else (bootstrap/sh07-core-children value))
                      records (mapv #(build % id) children)
                      record (cond-> {:form-id id :parent-form-id parent :kind kind :value value
                                      :children (mapv :form-id records) :span {:source "<map-order-kernel>"}}
                               abbreviation? (assoc :abbrev :quote))]
                  (swap! sources conj record) record))
        root (build value nil)
        decimals (mapv (fn [form] {:kind :decimal :decoded (:value form) :form-id (:form-id form)
                                   :literal-id (:form-id form) :token-id (:form-id form)})
                       (filter #(instance? java.math.BigDecimal (:value %)) @sources))
        defn? (and (seq? value) (= 'defn (first value)))
        syntax {:syntax/id :upstream-map-root :sh07/root-syntax-id :projected-map-root
                :source {:form-id (:form-id root)} :span {} :form (bootstrap/sh05-expanded-form value)}
        resolution {:provenance {:source-path "<map-order-kernel>"}
                    :sh05-macro-artifact
                    {:gravity-macro-boundary {:authenticated-sh04-artifact
                                             {:c2-reader-artifact {:form-tree @sources :literal-decoding-records decimals}}}
                     :macro-expansion-trace (if defn? [{:macro 'defn :output-syntax-id :upstream-map-root}] [])}}
        evidence (bootstrap/sh07-core-decimal-evidence resolution)
        trace (when defn? (bootstrap/sh07-core-macro-trace {:source-revision-id :revision}
                              {:input-syntax-id :input-map-root :output-syntax-id :projected-map-root}))
        tree (bootstrap/sh07-core-build-form-tree "<map-order-kernel>" :revision evidence syntax trace)
        records (mapv #(assoc % :scope-id :preserved-scope :metadata (assoc (:metadata %) :binding-marker :preserved)) (:records tree))
        indexed (into {} (map (juxt :form-id identity)) records)]
    {:resolution resolution :syntaxes [syntax] :trees [{:root (get indexed (get-in tree [:root :form-id])) :records records}]}))

(deftest sh07-map-source-order-development-kernel
  (let [mixed (array-map :a 'value '[key] '(quote {ready "quoted"}) '(identity key) {:nested [true false]})
        values [(into {} (map #(vector (keyword (str "key" %)) (str "value" %)) (range 9)))
                mixed (list 'quote mixed) #{mixed {:other 'value}}
                (with-meta mixed {:doc "metadata map"})
                (list 'defn 'packet '[key value] mixed)
                (list 'defn 'typed '[key :- Symbol value :- String] ':- 'String (list 'quote mixed))
                (array-map :first (java.math.BigDecimal. "1.25") :second (java.math.BigDecimal. "1.25"))]]
    (doseq [value values]
      (let [{:keys [resolution syntaxes trees]} (map-order-kernel value)
            result (bootstrap/sh07-core-source-map-order resolution syntaxes trees)
            before (get-in trees [0 :records]) after (get-in result [0 :records])]
        (is (= (mapv :form-id before) (mapv :form-id after)))
        (is (= (mapv #(dissoc % :child-form-ids) before) (mapv #(dissoc % :child-form-ids) after)))
        (doseq [[original changed] (map vector before after)]
          (let [indexed (into {} (map (juxt :form-id identity)) before)
                inert? (loop [child original]
                         (when-let [parent (get indexed (:parent-form-id child))]
                           (or (and (seq? (:value parent)) (= 'quote (first (:value parent)))
                                    (= (:form-id child) (second (:child-form-ids parent))))
                               (recur parent))))]
          (is (= (if (and (= :map (:kind original)) (not inert?))
                   (vec (mapcat identity (reverse (partition 2 (:child-form-ids original)))))
                   (:child-form-ids original)) (:child-form-ids changed)))))
        (is (= result (bootstrap/sh07-core-source-map-order resolution syntaxes trees)))))
    (let [{:keys [resolution syntaxes trees]} (map-order-kernel mixed)
          sources (get-in resolution [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree])
          map-index (first (keep-indexed #(when (= (get-in syntaxes [0 :source :form-id]) (:form-id %2)) %1) sources))
          root-id (get-in trees [0 :root :form-id])
          projected-index (first (keep-indexed #(when (= root-id (:form-id %2)) %1) (get-in trees [0 :records])))
          source-path [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree]]
      (doseq [[label r s t] [[:duplicate-source (update-in resolution source-path conj (first sources)) syntaxes trees]
                             [:missing-child (assoc-in resolution (into source-path [map-index :children 0]) :missing) syntaxes trees]
                             [:duplicate-child (assoc-in resolution (into source-path [map-index :children]) [(:form-id (first sources)) (:form-id (first sources))]) syntaxes trees]
                             [:missing-origin resolution (assoc-in syntaxes [0 :source :form-id] :missing) trees]
                             [:missing-projected resolution syntaxes (assoc-in trees [0 :records projected-index :child-form-ids 0] :missing)]]]
        (let [failure (try (bootstrap/sh07-core-source-map-order r s t) nil
                          (catch clojure.lang.ExceptionInfo error (ex-data error)))]
          (is (= "C6-ORIGIN" (:id failure)) label)
          (is (= :core-lowering (:stage failure)) label))))
    (let [{:keys [resolution syntaxes trees]} (map-order-kernel '(defn packet [] {:a true :b false}))
          resolution (assoc-in resolution [:sh05-macro-artifact :macro-expansion-trace 0 :macro] 'unverified-macro)]
      (is (= :map-source-defn-transfer
             (:reason (try (bootstrap/sh07-core-source-map-order resolution syntaxes trees) nil
                           (catch clojure.lang.ExceptionInfo error (ex-data error)))))))))

(deftest sh07-map-quoted-duplicate-source-development-kernel
  (doseq [mode [:prefix-quote :list-quote :evaluated]]
    (let [quoted? (not= mode :evaluated)
          {:keys [resolution syntaxes trees]} (map-order-kernel (if quoted? (list 'quote {:a 2}) {:a 2}))
          source-path [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree]
          sources (get-in resolution source-path)
          index (first (keep-indexed #(when (= :map (:kind %2)) %1) sources))
          owner (get sources index) old-children (:children owner)
          key {:form-id :original-first-key :parent-form-id (:form-id owner) :kind :keyword :value :a :children []}
          value {:form-id :original-first-value :parent-form-id (:form-id owner) :kind :integer :value 1 :children []}
          original-sources (-> sources (assoc-in [index :children] (into [(:form-id key) (:form-id value)] old-children)) (conj key value))
          original-sources (if (= mode :list-quote)
                             (let [root-id (get-in syntaxes [0 :source :form-id])
                                   root-index (first (keep-indexed #(when (= root-id (:form-id %2)) %1) original-sources))
                                   operator {:form-id :original-quote-operator :parent-form-id root-id :kind :symbol :value 'quote :children []}]
                               (-> original-sources (assoc-in [root-index :kind] :list)
                                   (update-in [root-index :children] #(into [(:form-id operator)] %)) (conj operator))) original-sources)
          resolution (assoc-in resolution source-path original-sources)]
      (is (= 4 (count (get-in original-sources [index :children]))))
      (is (= {:a 2} (get-in original-sources [index :value])))
      (if quoted?
        (is (= trees (bootstrap/sh07-core-source-map-order resolution syntaxes trees)))
        (let [failure (try (bootstrap/sh07-core-source-map-order resolution syntaxes trees) nil
                           (catch clojure.lang.ExceptionInfo error (ex-data error)))]
          (is (= "C6-ORIGIN" (:id failure)))
          (is (= :map-source-entry-bijection (:reason failure))))))))

(deftest sh07-map-source-order-authenticated-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [source-path (path (str "bootstrap/clojure/fixtures/self-hosting/sh-07/map-entry-source-order" extension))
                 text (slurp source-path)
                 resolution (bootstrap/compiler-c5-resolution-file-artifact source-path)
                 artifact (bootstrap/sh07-core-from-resolution-artifact resolution)
                 verification (bootstrap/sh07-core-artifact-verification artifact)
                 request (get-in artifact [:gravity-core-boundary :authenticated-core-request])
                 core (get-in artifact [:gravity-core-boundary :canonical-core-artifact])
                 c2 (get-in resolution [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
                 source-forms (into {} (map (juxt :form-id identity)) (:form-tree c2))
                 source-roots (mapv source-forms (:top-level-form-ids c2))
                 projected (into {} (map (juxt :form-id identity)) (:forms request))
                 projected-roots (mapv projected (:top-level-form-ids request))
                 nodes (into {} (map (juxt :node-id identity)) (:nodes core))
                 orders
                 (into {}
                  (for [[name expected] [['forward [:a :b :c :d :e :f :g :h :i]]
                                        ['backward [:i :h :g :f :e :d :c :b :a]]]]
                    (let [source-root (first (filter #(= name (second (:value %))) source-roots))
                          source-map (get source-forms (get (:children source-root) 3))
                          original-keys (mapv #(get-in source-forms [% :value]) (take-nth 2 (:children source-map)))
                          projected-root (first (filter #(= name (get-in projected [(second (:child-form-ids %)) :value])) projected-roots))
                          function-form (get projected (get (:child-form-ids projected-root) 2))
                          projected-map (get projected (get (:child-form-ids function-form) 2))
                          projected-keys (mapv #(get-in projected [% :value]) (take-nth 2 (:child-form-ids projected-map)))
                          function (first (filter #(= name (:definition-name %)) (:function-records core)))
                          body (get nodes (:body-core-node-id function))
                          keys (mapv #(get-in nodes [% :attributes :value]) (take-nth 2 (:children body)))
                          values (mapv (fn [id] (get-in nodes [(first (:children (get nodes id))) :attributes :value]))
                                       (take-nth 2 (rest (:children body))))]
                      (is (= :map (:kind source-map) (:kind projected-map)))
                      (is (= expected original-keys projected-keys keys))
                      (is (= (mapv #(str "value-" (clojure.core/name %)) expected) values))
                      (is (= :collection-literal (:core-form body)))
                      (is (= :map (get-in body [:attributes :literal-kind])))
                      (is (= (:children body) (mapv :core-node-id (get-in body [:evaluation :order]))))
                      (is (= :left-to-right (get-in body [:evaluation :kind])))
                      [name {:source-map source-map :projected-map projected-map :keys keys}])))
                 quoted-source-root (first (filter #(= 'quoted-duplicates (second (:value %))) source-roots))
                 quoted-prefix (get source-forms (get (:children quoted-source-root) 2))
                 quoted-source-map (get source-forms (first (:children quoted-prefix)))
                 quoted-root (first (filter #(= 'quoted-duplicates (get-in projected [(second (:child-form-ids %)) :value])) projected-roots))
                 quote-form (get projected (get (:child-form-ids quoted-root) 2))
                 quoted-map (get projected (get (:child-form-ids quote-form) 1))
                 quote-node (first (filter #(= :quote (:core-form %)) (:nodes core)))]
             (is (= :accepted (:status resolution)))
             (is (= :passed (:status verification)))
             (is (= [:meta :jvm] [(get-in request [:module :profile]) (get-in request [:module :target])]))
             (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in c2 [:source-unit-record :bytes-hash])))
             (is (= (:artifact-id resolution) (get-in request [:lineage :authenticated-sh06-artifact-id])))
             (is (= (reverse (get-in orders ['forward :keys])) (get-in orders ['backward :keys])))
             (is (= (vec (keys (get-in orders ['forward :source-map :value])))
                    (vec (keys (get-in orders ['backward :source-map :value])))))
             (is (= 4 (count (:children quoted-source-map))))
             (is (= 2 (count (:child-form-ids quoted-map))))
             (is (= {:a 2} (:value quoted-source-map) (:value quoted-map) (get-in quote-node [:attributes :quoted-value])))
             (is (= [] (:children quote-node)))
             {:artifact artifact :resolution resolution :request request :orders orders}))
         [".gravity" ".qst"])
        {:keys [resolution request orders]} (first cases)
        id (get-in orders ['forward :projected-map :form-id])
        index (first (keep-indexed #(when (= id (:form-id %2)) %1) (:forms request)))
        children (get-in request [:forms index :child-form-ids])
        changed (assoc-in request [:forms index :child-form-ids] (vec (mapcat identity (reverse (partition 2 children)))))
        changed (assoc changed :projection-binding
                       (bootstrap/reader-canonical-hash (bootstrap/sh07-core-projection-binding-input changed)))
        executed (atom 0)
        failure (with-redefs [bootstrap/sh07-core-run-structural-request-for-test
                             (fn [_] (swap! executed inc) (throw (AssertionError. "tampered source order reached core execution")))]
                  (try (bootstrap/sh07-core-run-request-for-test resolution changed) nil
                       (catch clojure.lang.ExceptionInfo error (ex-data error))))]
    (is (= (get-in cases [0 :artifact :artifact-id]) (get-in cases [1 :artifact :artifact-id])))
    (is (= (set children) (set (get-in changed [:forms index :child-form-ids]))))
    (is (= "C6-VERIFY" (:rule failure)))
    (is (= :authenticated-sh06-request-membership-mismatch (get-in failure [:facts :reason])))
    (is (zero? @executed))))

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

(defn- source-artifact
  [source-path text]
  ((required-var 'sh07-core-source-artifact) source-path text))

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
    (get (second clause) :sh07-b6)))

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
       (ex-info "SH-07-B6 records are not uniquely identifiable"
                {:id "SH07-B6-AMBIGUOUS-INDEX"
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

(defn- update-request-binding
  [request binding-id update-function]
  (update request :binding-table
          (fn [bindings]
            (mapv (fn [binding]
                    (if (= binding-id (:binding-id binding))
                      (update-function binding)
                      binding))
                  bindings))))

(defn- update-request-resolution
  [request syntax-id update-function]
  (update request :resolution-table
          (fn [resolutions]
            (mapv (fn [resolution]
                    (if (= syntax-id (:reference-syntax-id resolution))
                      (update-function resolution)
                      resolution))
                  resolutions))))

(deftest sh07-b6-qualified-set-development-kernel
  ;; Normalized development fixtures exercise compiled Gravity leaves, not
  ;; authenticated SH-06 provenance. Real composition is checked separately.
  (let [plan (:plan (bootstrap/sh07-core-current-binding!))
        invoke (fn [function arguments]
                 (bootstrap/p15-s23-stage2-runtime-execute-function
                  {:engine :gravity-sh07-development-fixture :compiler-artifact-plan? true}
                  plan function arguments))
        sha (fn [n] (str "sha256:" (format "%064x" n)))
        target {:form-id (sha 1) :syntax-id (sha 2) :kind :symbol
                :value 'demo.value/target}
        value {:form-id (sha 3) :syntax-id (sha 4) :kind :integer :value 7
               :metadata {} :generated-origin [] :origin-chain []}
        form {:form-id (sha 5) :syntax-id (sha 6) :kind :list
              :child-form-ids [(sha 7) (:form-id target) (:form-id value)]
              :metadata {} :generated-origin [] :origin-chain []}
        binding {:binding-id (sha 8) :binding-class :namespace :kind :var
                 :namespace 'demo.value :upstream-binding-id (sha 9)
                 :definition-syntax-id (sha 10) :definition-artifact-id (sha 11)}
        resolution {:symbol (:value target) :position :expression
                    :binding-id (:binding-id binding)
                    :upstream-binding-id (:upstream-binding-id binding)
                    :resolution-order :fully-qualified-namespace-binding}
        request {:module {:namespace 'demo.value :profile :meta :target :jvm
                          :effects #{:state/write} :capabilities #{} :safety :safe}
                 :lineage {:authenticated-sh06-artifact-id (sha 12)
                           :sh06-semantic-projection-id (sha 13)}
                 :internal-indexes
                 {:forms {:by-id {(:form-id target) target (:form-id value) value}}
                  :bindings {:by-id {(:binding-id binding) binding}}
                  :resolutions {:by-reference {(:syntax-id target) resolution}}}}
        resolution-path [:internal-indexes :resolutions :by-reference (:syntax-id target)]
        binding-path [:internal-indexes :bindings :by-id (:binding-id binding)]
        lower #(invoke 'sh07-lower-set [% (invoke 'sh07-empty-state []) form])]
    (testing "development-unadmitted qualified and ordinary var/function targets"
      (doseq [kind [:var :function]
              order [:fully-qualified-namespace-binding :current-namespace-binding]]
        (let [target-symbol (if (= order :current-namespace-binding) 'target (:value target))
              input (-> request
                        (assoc-in (conj binding-path :kind) kind)
                        (assoc-in (conj resolution-path :resolution-order) order)
                        (assoc-in (conj resolution-path :symbol) target-symbol)
                        (assoc-in [:internal-indexes :forms :by-id (:form-id target) :value] target-symbol))
              result (lower input)
              nodes (get-in result [:state :nodes])
              node (last nodes)
              record (first (get-in result [:state :mutations]))
              rhs-id (sha 14)
              resolved-node (-> node (assoc :children [rhs-id])
                                (assoc-in [:evaluation :order] [{:index 0 :core-node-id rhs-id}]))
              resolved (invoke 'sh07-resolve-mutation-record [record [rhs-id (sha 15)]])]
          (is (= :accepted (:status result)))
          (is (= [:literal :set!] (mapv :core-form nodes)))
          (is (= 7 (get-in (first nodes) [:attributes :value])))
          (is (= 1 (count (get-in result [:state :mutations]))))
          (is (= order (:resolution-order record) (get-in node [:attributes :resolution-order])))
          (is (= kind (:target-definition-kind record)))
          (is (= [] (:target-evaluated-children record)))
          (is (= [(:value-core-node-id record)] (:children node) (:evaluated-children record)))
          (is (= [:evaluate-value :write-target :yield-unit] (:ordered-steps record)))
          (is (= :unit (:result-policy record)))
          (is (= :state/write (:required-effect record)))
          (is (= #{(:syntax-id target)} (get-in result [:state :used-reference-syntax-ids])))
          (is (= {:type-legality :pending-sh08 :effect-registry-legality :pending-sh09
                  :effect-profile-capability-legality :pending-sh09
                  :ownership-legality :pending-sh10 :safety-classification :pending-sh11}
                 (select-keys record [:type-legality :effect-registry-legality
                                      :effect-profile-capability-legality :ownership-legality
                                      :safety-classification])))
          (is (true? (invoke 'sh07-resolved-b6-node-valid? [resolved-node])))
          (is (= :accepted (:status resolved)))
          (is (= rhs-id (get-in resolved [:value :value-core-node-id])))
          (is (false? (invoke 'sh07-resolved-b6-node-valid?
                              [(assoc-in resolved-node [:attributes :resolution-order]
                                         :alias-qualified-required-binding)])))
          (is (= :rejected (:status (invoke 'sh07-resolve-mutation-record
                                           [(assoc record :resolution-order :alias-qualified-required-binding)
                                            [rhs-id (sha 15)]])))))))
    (testing "foreign, alias, lexical and forged joins retain exact rejections"
      (doseq [[input rule reason]
              [[(assoc-in request (conj binding-path :namespace) 'foreign.value)
                "C6-LOWERING-GAP" :qualified-set-target-deferred]
               [(assoc-in request (conj resolution-path :resolution-order) :alias-qualified-required-binding)
                "C6-LOWERING-GAP" :qualified-set-target-deferred]
               [(assoc-in request (conj binding-path :binding-class) :lexical)
                "C6-LOWERING-GAP" :lexical-set-target-deferred]
               [(assoc-in request (conj binding-path :kind) :type)
                "C6-LOWERING-GAP" :unsupported-set-location-kind]
               [(assoc-in request (conj resolution-path :symbol) 'target)
                "C6-VERIFY" :set-resolution-reference-mismatch]
               [(assoc-in request (conj resolution-path :binding-id) (sha 16))
                "C6-VERIFY" :set-resolution-binding-missing]
               [(assoc-in request (conj resolution-path :upstream-binding-id) (sha 16))
                "C6-VERIFY" :set-resolution-binding-identity-mismatch]
               [(assoc-in request (conj resolution-path :resolution-order) :lexical-binding)
                "C6-VERIFY" :set-same-namespace-binding-required]]]
        (let [result (lower input)]
          (is (= :rejected (:status result)))
          (is (= rule (:rule result)))
          (is (= reason (get-in result [:facts :reason]))))))))

(deftest sh07-b6-qualified-set-preserves-authenticated-products-and-mapping
  (let [artifact (file-artifact "accepted" "qualified-set-target" ".gravity")
        canonical (core artifact)
        records (:mutations canonical)
        nodes (exactly-once-index (:nodes canonical) :node-id)
        bindings (exactly-once-index (:binding-table (request artifact)) :binding-id)
        resolutions (exactly-once-index (:resolution-table (request artifact)) :reference-syntax-id)
        mapping-result
        (bootstrap/sh07-core-execute!
         (fixture-path "accepted" "qualified-set-target" ".gravity")
         'sh07-resolve-source-core-map-preimage
         [(:preimage (last (get-in artifact [:gravity-core-boundary :digest-requests])))
          (pop (get-in artifact [:gravity-core-boundary :resolved-digests]))])
        mapping (get-in mapping-result [:value :records])]
    (is (= [:var :function :var] (mapv :target-definition-kind records)))
    (is (= :accepted (:status mapping-result)))
    (is (= [0 1 2] (mapv :ordinal records)))
    (is (= :passed (:status (bootstrap/sh07-core-artifact-verification artifact))))
    (is (= :development-unadmitted (get-in artifact [:gravity-core-boundary :plan-binding :revision-status])))
    (is (false? (get-in artifact [:gravity-core-boundary :plan-binding :self-hosted?])))
    (is (= (source-text (fixture-path "accepted" "qualified-set-target" ".gravity"))
           (source-text (fixture-path "accepted" "qualified-set-target" ".qst"))))
    (doseq [record records]
      (let [node (get nodes (:core-node-id record))
            binding (get bindings (:target-binding-id record))
            resolution (get resolutions (:target-syntax-id record))
            row (first (filter #(= (:target-form-id record) (get-in % [:source :form-id])) mapping))]
        (is (= :fully-qualified-namespace-binding (:resolution-order record)
               (:resolution-order resolution) (get-in node [:attributes :resolution-order])))
        (is (= :namespace (:binding-class binding) (:target-binding-class record)))
        (is (= (:binding-id binding) (:binding-id resolution) (:target-binding-id record)))
        (is (= (:target-symbol record) (:symbol resolution)))
        (is (= (select-keys record mutation-attribute-keys)
               (dissoc (:attributes node) :target-evaluation :value-child-index :evaluation-order)))
        (is (= (:upstream-binding-id binding) (:upstream-binding-id resolution)
               (:target-upstream-binding-id record)))
        (is (= (get-in (request artifact) [:module :namespace])
               (:namespace binding) (:target-namespace record)))
        (is (= [] (:target-evaluated-children record)))
        (is (= [(:value-core-node-id record)] (:children node) (:evaluated-children record)))
        (is (= [:evaluate-value :write-target :yield-unit] (:ordered-steps record)))
        (is (= :unit (:result-policy record)))
        (is (= :state/write (:required-effect record)))
        (is (= :pending-sh09 (:effect-registry-legality record) (:effect-profile-capability-legality record)))
        (is (= :consumed (:disposition row)))
        (is (= :mutation-target (:reason row)))
        (is (= [] (:emitted-node-ids row)))))
    (let [shadow-write (last records)
          rhs (get nodes (:value-core-node-id shadow-write))
          rhs-resolution (get resolutions (get-in rhs [:source :syntax-id]))
          reference (first (filter #(= (:value-core-node-id shadow-write) (:core-node-id %))
                                   (:reference-uses canonical)))
          rhs-row (first (filter #(= (get-in rhs [:source :form-id]) (get-in % [:source :form-id])) mapping))]
      (is (= :reference (:core-form rhs)))
      (is (= :lexical (:binding-class (get bindings (:binding-id rhs-resolution)))))
      (is (not= (:binding-id rhs-resolution) (:target-binding-id shadow-write)))
      (is (= (:binding-id rhs-resolution) (:binding-id reference) (get-in rhs [:attributes :binding-id])))
      (is (= (:reference-syntax-id rhs-resolution) (:syntax-id reference) (get-in rhs [:source :syntax-id])))
      (is (= (:form-id reference) (get-in rhs [:source :form-id]) (get-in rhs-row [:source :form-id])))
      (is (= 1 (count (filter #(= (:node-id rhs) (:core-node-id %)) (:reference-uses canonical)))))
      (is (= :emitted (:disposition rhs-row)))
      (is (= [(:node-id rhs)] (:emitted-node-ids rhs-row))))))

(deftest sh07-b6-qualified-set-forgeries-fail-membership-and-replay
  (let [artifact (file-artifact "accepted" "qualified-set-target" ".gravity")
        authenticated (request artifact)
        record (first (:mutations (core artifact)))
        wrong-id "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"]
    (doseq [[field value] [[:resolution-order :current-namespace-binding]
                         [:resolution-order :alias-qualified-required-binding]
                         [:symbol 'target]
                         [:binding-id wrong-id]
                         [:upstream-binding-id wrong-id]]]
      (let [mutation (update-request-resolution authenticated (:target-syntax-id record) #(assoc % field value))
            result (diagnostic-result #(bootstrap/sh07-core-from-authenticated-request
                                        (:sh06-resolution-artifact artifact) mutation))
            diagnostic (diagnostic-data result)]
        (is (not= authenticated mutation))
        (is (nil? (:raw-host-error result)))
        (is (= "C6-VERIFY" (:rule diagnostic)))
        (is (= :authenticated-sh06-request-membership-mismatch (get-in diagnostic [:facts :reason])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))
    (doseq [[field value] [[:resolution-order :current-namespace-binding]
                         [:target-binding-id wrong-id]
                         [:value-core-node-id wrong-id]
                         [:target-evaluated-children [(:value-core-node-id record)]]]]
      (let [mutant (assoc-in artifact [:gravity-core-boundary :canonical-core-artifact :mutations 0 field] value)
            checks (bootstrap/sh07-core-verification-checks mutant artifact {:status :passed})]
        (is (false? (:canonical-core-replays? checks)))
        (is (false? (:mutations-replay? checks)))
        (is (false? (:authoritative-products-replay? checks)))))))

(deftest sh07-b6-qualified-set-external-and-alias-diagnostics
  (doseq [basename ["external-qualified-set-target" "alias-qualified-set-target"]]
    (let [source-path (fixture-path "rejected" basename ".gravity")
          result (diagnostic-result #(bootstrap/sh07-core-file-artifact source-path))
          diagnostic (diagnostic-data result)]
      (is (= (source-text source-path) (source-text (fixture-path "rejected" basename ".qst"))))
      (is (= :qualified-set-target-deferred (get-in (fixture-oracle basename ".gravity") [:expected-reason])))
      (is (nil? (:raw-host-error result)))
      (is (= "C6-LOWERING-GAP" (:rule diagnostic)))
      (is (= :core-lowering (:stage diagnostic)))
      (is (= :qualified-set-target-deferred (get-in diagnostic [:facts :reason])))
      (is (= gap-remediation (:remediation diagnostic)))
      (is (true? (get-in diagnostic [:facts :fail-closed]))))))

(deftest sh07-b6-fixtures-are-paired-complete-and-path-neutral
  (doseq [family ["accepted" "rejected"]
          extension extensions]
    (is (= (if (= family "accepted")
             (set accepted-basenames)
             (set (keys rejected-oracles)))
           (fixture-basenames family extension))))
  (doseq [family ["accepted" "rejected"]
          basename (if (= family "accepted")
                     accepted-basenames
                     (keys rejected-oracles))]
    (is (= (seq (source-bytes
                 (fixture-path family basename ".gravity")))
           (seq (source-bytes
                 (fixture-path family basename ".qst"))))))
  (doseq [basename accepted-basenames]
    (let [gravity (file-artifact "accepted" basename ".gravity")
          qst (file-artifact "accepted" basename ".qst")]
      (testing basename
        (is (= :accepted (:status gravity) (:status qst)))
        (is (= (:artifact-id gravity) (:artifact-id qst)))
        (is (= (identity-input gravity) (identity-input qst)))
        (is (= (:mutations (core gravity))
               (:mutations (core qst))))
        (is (= 15 (:schema-version (request gravity))
               (:schema-version (request qst))))
        (is (= :sh07-b15-keyword-map-lookup
               (:scope (request gravity))
               (:scope (request qst)))))))
  (let [fixture
        (fixture-path
         "accepted" "ordered-top-level-mutations" ".gravity")
        temp-root
        (java.nio.file.Files/createTempDirectory
         "gravity-sh07-b6-cross-root-"
         (make-array java.nio.file.attribute.FileAttribute 0))
        left-path (.resolve temp-root "left/mutation.gravity")
        right-path (.resolve temp-root "right/mutation.qst")]
    (try
      (doseq [target [left-path right-path]]
        (java.nio.file.Files/createDirectories
         (.getParent target)
         (make-array java.nio.file.attribute.FileAttribute 0))
        (java.nio.file.Files/write
         target
         (source-bytes fixture)
         (make-array java.nio.file.OpenOption 0)))
      (let [left
            ((required-var 'sh07-core-file-artifact) (str left-path))
            right
            ((required-var 'sh07-core-file-artifact) (str right-path))
            left-core (core left)
            right-core (core right)
            left-request (request left)
            right-request (request right)
            left-semantic-id
            (get-in left-request
                    [:lineage :sh06-semantic-projection-id])
            right-semantic-id
            (get-in right-request
                    [:lineage :sh06-semantic-projection-id])
            left-mutations (:mutations left-core)
            right-mutations (:mutations right-core)
            left-set-nodes
            (filterv #(= :set! (:core-form %)) (:nodes left-core))
            right-set-nodes
            (filterv #(= :set! (:core-form %)) (:nodes right-core))]
        (is (= (:artifact-id left) (:artifact-id right)))
        (is (= (identity-input left) (identity-input right)))
        (is (= left-semantic-id right-semantic-id))
        (is (=
             (get-in left-request
                     [:lineage :authenticated-sh06-artifact-id])
             (get-in right-request
                     [:lineage :authenticated-sh06-artifact-id])))
        (is (not= (str left-path) (str right-path)))
        (is (= (mapv :node-id (:nodes left-core))
               (mapv :node-id (:nodes right-core))))
        (is (= (str left-path)
               (get-in left [:provenance :source-path])
               (get-in left-core [:provenance :actual-source-path])))
        (is (= (str right-path)
               (get-in right [:provenance :source-path])
               (get-in right-core [:provenance :actual-source-path])))
        (is (= (count left-mutations) (count right-mutations)))
        (is (= (count left-set-nodes) (count right-set-nodes)))
        (is (every?
             #(= (get-in left-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (:authenticated-sh06-artifact-id %))
             left-mutations))
        (is (every?
             #(= (get-in right-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (:authenticated-sh06-artifact-id %))
             right-mutations))
        (is (every?
             #(= (get-in left-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (get-in % [:attributes
                            :authenticated-sh06-artifact-id]))
             left-set-nodes))
        (is (every?
             #(= (get-in right-request
                         [:lineage :authenticated-sh06-artifact-id])
                 (get-in % [:attributes
                            :authenticated-sh06-artifact-id]))
             right-set-nodes)))
      (finally
        (delete-tree! temp-root)))))

(deftest sh07-b6-mutations-bind-authenticated-target-and-evaluate-only-value
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          core-artifact (core artifact)
          records (:mutations core-artifact)
          nodes (exactly-once-index (:nodes core-artifact) :node-id)
          source-map
          (exactly-once-index (:source-map core-artifact)
                              :core-node-id)
          forms
          (exactly-once-index (:forms (request artifact)) :form-id)
          bindings
          (exactly-once-index (:binding-table (request artifact))
                              :binding-id)
          resolutions
          (exactly-once-index (:resolution-table (request artifact))
                              :reference-syntax-id)]
      (testing (str basename extension)
        (is (seq records))
        (is (= (vec (range (count records)))
               (mapv :ordinal records)))
        (doseq [record records]
          (let [node (get nodes (:core-node-id record))
                binding (get bindings (:target-binding-id record))
                resolution
                (get resolutions (:target-syntax-id record))
                target-form (get forms (:target-form-id record))
                value-node-id (:value-core-node-id record)
                source-record (get source-map (:core-node-id record))
                attributes (:attributes node)
                shared-identity-keys
                [:target-symbol :target-binding-id
                 :target-binding-class :target-definition-kind
                 :target-location :target-upstream-binding-id
                 :target-namespace :target-definition-syntax-id
                 :target-definition-artifact-id
                 :target-form-id :target-syntax-id
                 :resolution-order
                 :authenticated-sh06-artifact-id
                 :sh06-semantic-projection-id
                 :type-legality :effect-registry-legality
                 :effect-profile-capability-legality
                 :ownership-legality :safety-classification]]
            (is (= mutation-record-keys (set (keys record))))
            (is (= mutation-attribute-keys
                   (set (keys attributes))))
            (is (= (select-keys record shared-identity-keys)
                   (select-keys attributes shared-identity-keys)))
            (is (= :set! (:core-form node)))
            (is (= (:form-id record)
                   (get-in node [:source :form-id])
                   (:form-id source-record)))
            (is (= (:syntax-id record)
                   (get-in node [:source :syntax-id])
                   (:syntax-id source-record)))
            (is (= [value-node-id] (:children node)))
            (is (= [(:target-binding-id record)]
                   (:resolved-binding-ids node)))
            (is (= :value-then-write
                   (get-in node [:evaluation :kind])))
            (is (= [{:index 0 :core-node-id value-node-id}]
                   (get-in node [:evaluation :order])))
            (is (= (:evaluation-region record)
                   (get-in node [:evaluation :region])))
            (is (= (:evaluation-owner-function-syntax-id record)
                   (get-in node
                           [:evaluation
                            :owner-function-syntax-id])))
            (is (= [] (:target-evaluated-children record)))
            (is (= [value-node-id] (:evaluated-children record)))
            (is (= [:evaluate-value :write-target :yield-unit]
                   (:ordered-steps record)))
            (is (= :unit (:result-policy record)
                   (get-in node [:attributes :result-policy])))
            (is (= :state/write (:required-effect record)
                   (get-in node [:attributes :required-effect])))
            (is (= :namespace (:binding-class binding)
                   (:target-binding-class record)))
            (is (= (:target-symbol record)
                   (:symbol resolution)
                   (:value target-form)))
            (is (= (name (:target-symbol record)) (name (:name binding))))
            (is (= (:target-binding-id record)
                   (:binding-id binding)
                   (:binding-id resolution)))
            (is (= (:target-syntax-id record)
                   (:syntax-id target-form)
                   (:reference-syntax-id resolution)))
            (is (= (:target-form-id record)
                   (:form-id target-form)))
            (is (= (:target-namespace record)
                   (:namespace binding)
                   (get-in (request artifact)
                           [:module :namespace])))
            (is (= (:target-definition-syntax-id record)
                   (:definition-syntax-id binding)))
            (is (contains? #{:var :function}
                           (:target-definition-kind record)))
            (is (= (:kind binding)
                   (:target-definition-kind record)))
            (is (= :top-level-var (:target-location record)))
            (is (= (:upstream-binding-id binding)
                   (:upstream-binding-id resolution)
                   (:target-upstream-binding-id record)))
            (is (contains? #{:current-namespace-binding :fully-qualified-namespace-binding}
                           (:resolution-order resolution)))
            (is (= (:resolution-order resolution)
                   (:resolution-order record)))
            (is (= (:definition-artifact-id binding)
                   (:target-definition-artifact-id record)))
            (is (= (get-in (request artifact)
                           [:lineage
                            :authenticated-sh06-artifact-id])
                   (:authenticated-sh06-artifact-id record)))
            (is (= (get-in (request artifact)
                           [:lineage :sh06-semantic-projection-id])
                   (:sh06-semantic-projection-id record)))
            (is (= :pending-sh08 (:type-legality record)))
            (is (= :pending-sh09
                   (:effect-registry-legality record)
                   (:effect-profile-capability-legality record)))
            (is (= :pending-sh10 (:ownership-legality record)))
            (is (= :pending-sh11
                   (:safety-classification record)))))))))

(deftest sh07-b6-target-and-rhs-resolutions-are-distinct-and-consumed-once
  (doseq [extension extensions]
    (let [artifact
          (file-artifact
           "accepted" "ordered-top-level-mutations" extension)
          records (:mutations (core artifact))
          self-write
          (first
           (filter #(= 'first-target (:target-symbol %)) records))
          rhs-use
          (first
           (filter #(= 'first-target (:symbol %))
                   (:reference-uses (core artifact))))
          target-syntax-id (:target-syntax-id self-write)
          rhs-syntax-id (:syntax-id rhs-use)
          resolutions (:resolution-table (request artifact))]
      (is self-write)
      (is rhs-use)
      (is (not= target-syntax-id rhs-syntax-id))
      (is (= 1 (count (filter #(= target-syntax-id
                                  (:reference-syntax-id %))
                             resolutions))))
      (is (= 1 (count (filter #(= rhs-syntax-id
                                  (:reference-syntax-id %))
                             resolutions))))
      (is (= (:target-binding-id self-write)
             (:binding-id rhs-use))))))

(deftest sh07-b6-conditional-regions-and-function-var-kind-are-preserved
  (doseq [extension extensions]
    (let [conditional
          (file-artifact "accepted" "conditional-mutations" extension)
          records (:mutations (core conditional))
          regions (mapv :evaluation-region records)
          function-artifact
          (file-artifact "accepted" "function-var-mutation" extension)
          function-record
          (first (:mutations (core function-artifact)))]
      (is (= [:then :else] (mapv :role regions)))
      (is (every? #(= :conditional-branch (:kind %)) regions))
      (is (every? #(= :selected-exactly-once (:execution %))
                  regions))
      (is (= :function (:target-definition-kind function-record)))
      (is (= :top-level-var (:target-location function-record))))))

(deftest sh07-b6-nested-rhs-mutation-precedes-outer-write
  (doseq [extension extensions]
    (let [artifact
          (file-artifact "accepted" "nested-value-mutation" extension)
          records (:mutations (core artifact))
          nodes (exactly-once-index (:nodes (core artifact)) :node-id)
          source-map
          (exactly-once-index (:source-map (core artifact))
                              :core-node-id)
          inner (first records)
          outer (second records)
          inner-node (get nodes (:core-node-id inner))
          outer-node (get nodes (:core-node-id outer))]
      (is (= ['inner-target 'outer-target]
             (mapv :target-symbol records)))
      (is (= [0 1] (mapv :ordinal records)))
      (is (= (:core-node-id inner)
             (:value-core-node-id outer)))
      (is (= [(:core-node-id inner)] (:children outer-node)))
      (is (= [(:core-node-id inner)]
             (:evaluated-children outer)))
      (is (= [] (:target-evaluated-children outer)))
      (is (= [:evaluate-value :write-target :yield-unit]
             (:ordered-steps inner)
             (:ordered-steps outer)))
      (is (= :value-then-write
             (get-in inner-node [:evaluation :kind])
             (get-in outer-node [:evaluation :kind])))
      (doseq [record records]
        (let [node (get nodes (:core-node-id record))
              source (get source-map (:core-node-id record))]
          (is (= (:form-id record)
                 (get-in node [:source :form-id])
                 (:form-id source)))
          (is (= (:syntax-id record)
                 (get-in node [:source :syntax-id])
                 (:syntax-id source))))))))

(deftest sh07-b6-identity-normalizes-physical-sh06-ids-without-dropping-provenance
  (doseq [basename accepted-basenames
          extension extensions]
    (let [artifact (file-artifact "accepted" basename extension)
          actual (:mutations (core artifact))
          semantic (:mutations (identity-input artifact))
          actual-nodes
          (filterv #(= :set! (:core-form %))
                   (:nodes (core artifact)))
          semantic-nodes
          (filterv #(= :set! (:core-form %))
                   (:nodes (identity-input artifact)))]
      (is (= (count actual) (count semantic)))
      (doseq [[actual-record semantic-record]
              (map vector actual semantic)]
        (let [semantic-projection-id
              (:sh06-semantic-projection-id actual-record)]
          (is (= (:target-binding-id actual-record)
                 (:target-upstream-binding-id semantic-record)))
          (is (= semantic-projection-id
                 (:target-definition-artifact-id semantic-record)
                 (:authenticated-sh06-artifact-id semantic-record)))
          (is (= (:target-upstream-binding-id actual-record)
                 (get-in
                  artifact
                  [:gravity-core-boundary
                   :canonical-core-artifact :mutations
                   (:ordinal actual-record)
                   :target-upstream-binding-id])))))
      (is (= (count actual-nodes) (count semantic-nodes)))
      (doseq [[actual-node semantic-node]
              (map vector actual-nodes semantic-nodes)]
        (let [actual-attributes (:attributes actual-node)
              semantic-attributes (:attributes semantic-node)
              semantic-projection-id
              (:sh06-semantic-projection-id actual-attributes)]
          (is (= (:target-binding-id actual-attributes)
                 (:target-upstream-binding-id
                  semantic-attributes)))
          (is (= semantic-projection-id
                 (:target-definition-artifact-id
                  semantic-attributes)
                 (:authenticated-sh06-artifact-id
                  semantic-attributes))))))))

(deftest sh07-b6-authenticated-target-substitution-fails-before-lowering
  (let [artifact
        (file-artifact
         "accepted" "ordered-top-level-mutations" ".gravity")
        record (first (:mutations (core artifact)))
        authenticated (request artifact)
        wrong-id
        "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        mutations
        {"binding kind"
         (update-request-binding
          authenticated (:target-binding-id record)
          #(assoc % :kind :type))
         "binding upstream identity"
         (update-request-binding
          authenticated (:target-binding-id record)
          #(assoc % :upstream-binding-id wrong-id))
         "resolution order"
         (update-request-resolution
          authenticated (:target-syntax-id record)
          #(assoc % :resolution-order
                  :fully-qualified-namespace-binding))}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [result
              (diagnostic-result
               #((required-var
                  'sh07-core-from-authenticated-request)
                 (:sh06-resolution-artifact artifact)
                 mutation))
              diagnostic (diagnostic-data result)]
          (is (not= authenticated mutation))
          (is (nil? (:raw-host-error result)))
          (is (= :gravity/sh07-core-diagnostic
                 (:artifact diagnostic)))
          (is (= "C6-VERIFY" (:rule diagnostic)))
          (is (= :authenticated-sh06-request-membership-mismatch
                 (get-in diagnostic [:facts :reason])))
          (is (true?
               (get-in diagnostic [:facts :fail-closed]))))))))

(deftest sh07-b6-rejections-are-structured-and-oracle-bound
  (doseq [[basename oracle] rejected-oracles
          extension extensions]
    (testing (str basename extension)
      (let [source-path (fixture-path "rejected" basename extension)
            declared (fixture-oracle basename extension)
            result
            (diagnostic-result
             #((required-var 'sh07-core-file-artifact) source-path))
            diagnostic (diagnostic-data result)]
        (is (= {:expected-rule (:rule oracle)
                :expected-stage :core-lowering
                :expected-severity :error
                :expected-reason (:reason oracle)
                :expected-remediation (:remediation oracle)}
               declared))
        (is (nil? (:raw-host-error result)))
        (is (= :gravity/sh07-core-diagnostic (:artifact diagnostic)))
        (is (= (:rule oracle) (:rule diagnostic)))
        (is (= :core-lowering (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= (:reason oracle)
               (get-in diagnostic [:facts :reason])))
        (when (:semantic-rule oracle)
          (is (= (:semantic-rule oracle)
                 (get-in diagnostic
                         [:facts :semantic-rule]))))
        (is (= (:remediation oracle) (:remediation diagnostic)))
        (is (= source-path (get-in diagnostic [:source-span :source])))
        (is (true? (get-in diagnostic [:facts :fail-closed])))))))

(deftest sh07-b6-mutation-product-tampering-fails-replay
  (let [artifact
        (file-artifact
         "accepted" "ordered-top-level-mutations" ".gravity")
        value-node
        (get-in artifact
                [:gravity-core-boundary :canonical-core-artifact
                 :mutations 1 :value-core-node-id])
        mutations
        {"result policy"
         (update-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0]
          assoc :result-policy :target-value)
         "ordinal"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0 :ordinal]
          7)
         "value node"
         (assoc-in
          artifact
          [:gravity-core-boundary :canonical-core-artifact
           :mutations 0 :value-core-node-id]
          value-node)}]
    (doseq [[label mutation] mutations]
      (testing label
        (let [checks
              ((required-var 'sh07-core-verification-checks)
               mutation artifact {:status :passed})
              failed
              (set (for [[check passed?] checks
                         :when (not (true? passed?))]
                     check))]
          (is (not= artifact mutation))
          (is (contains? failed :canonical-core-replays?))
          (is (contains? failed :mutations-replay?))
          (is (contains? failed :authoritative-products-replay?))
          (is (= :failed
                 (:status
                  ((required-var
                    'sh07-core-artifact-verification)
                   mutation)))))))))
