(ns gravity.self-hosting.sh06-b51-vector-leaf-checkpoint-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root
  []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh06_b51_vector_leaf_checkpoint_test.clj")]
    (when-not resource
      (throw (ex-info "SH-06-B51-A test source is not on the classpath"
                      {:id "SH06-B51-A-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw (ex-info "Repository root could not be located"
                        {:id "SH06-B51-A-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-06/b51-vector-leaves")
(def ^:private extensions [".gravity" ".qst"])
(def ^:private accepted-basenames
  #{"nested-vector-let" "nested-vector-loop" "simple-symbol-parity"})
(def ^:private recovery-basenames
  #{"duplicate-vector-let" "duplicate-vector-loop"})
(def ^:private vector-oracles
  {"nested-vector-let"
   {:family "accepted"
    :pattern '[first [_ [second]] []]
    :names ['first 'second]
    :leaf-paths [[0] [1 1 0]]
    :frontier :let-destructuring-deferred}
   "nested-vector-loop"
   {:family "accepted"
    :pattern '[head [_ [tail]] []]
    :names ['head 'tail]
    :leaf-paths [[0] [1 1 0]]
    :frontier :loop-destructuring-deferred}
   "duplicate-vector-let"
   {:family "recovery"
    :pattern '[first [first _ & second] second]
    :names ['first 'second]
    :leaf-paths [[0] [1 3]]
    :duplicate-paths [[1 0] [2]]
    :frontier :let-destructuring-deferred}
   "duplicate-vector-loop"
   {:family "recovery"
    :pattern '[value [value _ & tail] tail]
    :names ['value 'tail]
    :leaf-paths [[0] [1 3]]
    :duplicate-paths [[1 0] [2]]
    :frontier :loop-destructuring-deferred}})

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

(defn- required-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw (ex-info "Required SH-06-B51-A adapter is absent"
                      {:id "SH06-B51-A-ADAPTER-ABSENT"
                       :symbol symbol}))))

(def ^:private artifacts (atom {}))

(defn- artifact
  [family basename extension]
  (let [key [family basename extension]]
    (or (get @artifacts key)
        (let [value
              ((required-var 'sh06-resolution-file-artifact)
               (fixture-path family basename extension))]
          (swap! artifacts assoc key value)
          value))))

(defn- raw-request
  [value]
  (get-in value
          [:gravity-resolution-boundary
           :authenticated-resolution-request]))

(defn- projected-request
  [value]
  ((required-var 'sh07-core-authenticated-request) value))

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

(defn- local-bindings
  [request names]
  (->> (:binding-table request)
       (filter #(and (= :lexical (:binding-class %))
                     (contains? (set names) (:name %))))
       vec))

(defn- raw-scope
  [request names]
  (let [matches
        (filterv
         #(= (vec names) (mapv :name (:bindings %)))
         (:lexical-scopes request))]
    (when (= 1 (count matches))
      (first matches))))

(defn- form-index
  [request]
  (into {} (map (juxt :form-id identity)) (:forms request)))

(defn- pattern-form
  [request pattern]
  (let [matches (filterv #(= pattern (:value %)) (:forms request))]
    (when (= 1 (count matches))
      (first matches))))

(defn- form-at-path
  [request root-form child-path]
  (let [index (form-index request)]
    (reduce (fn [form child-index]
              (get index (nth (:child-form-ids form) child-index)))
            root-form
            child-path)))

(defn- assert-checkpoint-artifact
  [family basename extension]
  (let [value (artifact family basename extension)
        request (raw-request value)
        projected (projected-request value)]
    (is (= :accepted (:status value)))
    (is (= 1 (:schema-version request)))
    (is (= 15 (:schema-version projected)))
    (is (= :passed
           (:status
            ((required-var 'sh06-resolution-artifact-verification)
             value))))
    (is (= :complete
           (:status
            ((required-var 'sh06-resolution-capability-based-proof)
             value))))
    [value request projected]))

(defn- assert-vector-projection
  [basename extension oracle]
  (let [[value request projected]
        (assert-checkpoint-artifact (:family oracle) basename extension)
        names (:names oracle)
        scope (raw-scope request names)
        bindings (local-bindings projected names)
        binding-by-name (into {} (map (juxt :name identity)) bindings)
        root-form (pattern-form projected (:pattern oracle))
        leaf-forms (mapv #(form-at-path projected root-form %)
                         (:leaf-paths oracle))
        duplicate-forms (mapv #(form-at-path projected root-form %)
                              (:duplicate-paths oracle))
        empty-scopes (filterv #(empty? (:bindings %))
                              (:lexical-scopes request))
        explicit-empty-forms
        (filterv #(and (= [] (:value %)) (:scope-id %))
                 (:forms projected))
        references (filterv #(contains? (set names) (:symbol %))
                            (:references request))
        projected-resolutions
        (filterv #(contains? (set names) (:symbol %))
                 (:resolution-table projected))
        diagnostic
        (diagnostic-data
         (diagnostic-result
          #((required-var 'sh07-core-run-request-for-test)
            value projected)))]
    (is scope)
    (is (= names (mapv :name (:bindings scope))))
    (is (= (count names) (count bindings)))
    (is (= (set names) (set (keys binding-by-name))))
    (is root-form)
    (is (:scope-id root-form))
    (is (= (mapv :syntax-id leaf-forms)
           (mapv #(get-in binding-by-name [% :definition-syntax-id])
                 names)))
    (is (every? #(= (:scope-id root-form)
                    (get-in binding-by-name [% :scope-id]))
                names))
    (is (not-any? (set (map :syntax-id duplicate-forms))
                  (map :definition-syntax-id bindings)))
    (when (some #(= [] %)
                (tree-seq coll? seq (:pattern oracle)))
      (is (seq empty-scopes))
      (is (seq explicit-empty-forms)))
    (is (seq references))
    (is (every? #(some #{(:scope-id scope)} (:scope-chain %)) references))
    (is (= (set (map :binding-id bindings))
           (set (map :binding-id projected-resolutions))))
    (is (= "C6-LOWERING-GAP" (:rule diagnostic)) diagnostic)
    (is (= (:frontier oracle) (get-in diagnostic [:facts :reason]))
        diagnostic)
    [value request projected]))

(deftest fixture-catalog-and-byte-parity
  (doseq [extension extensions]
    (is (= accepted-basenames
           (fixture-basenames "accepted" extension)))
    (is (= recovery-basenames
           (fixture-basenames "recovery" extension))))
  (doseq [[family basenames]
          [["accepted" accepted-basenames]
           ["recovery" recovery-basenames]]
          basename basenames]
    (is (java.util.Arrays/equals
         (source-bytes (fixture-path family basename ".gravity"))
         (source-bytes (fixture-path family basename ".qst"))))))

(deftest preorder-vector-leaf-enumerator-is-shared-and-recovering
  (is (= [{:name '_ :path [4]}]
         (bootstrap/sh06-let-loop-binding-paths '_ [4])))
  (is (= [{:name '& :path [4]}]
         (bootstrap/sh06-let-loop-binding-paths '& [4])))
  (is (= [] (bootstrap/sh06-let-loop-binding-paths [] [4])))
  (is (= [{:name 'first :path [4 0]}
          {:name 'second :path [4 1 3]}]
         (bootstrap/sh06-let-loop-binding-paths
          '[first [first _ & second] second]
          [4]))))

(deftest nested-vector-leaves-preserve-sequential-visibility
  (doseq [basename ["nested-vector-let" "nested-vector-loop"]
          extension extensions]
    (testing (str basename extension)
      (let [oracle (get vector-oracles basename)
            [_ request _]
            (assert-vector-projection basename extension oracle)
            names (:names oracle)
            vector-scope (raw-scope request names)
            empty-scope
            (some #(when (and (empty? (:bindings %))
                              (= (:scope-id vector-scope)
                                 (:parent-scope-id %)))
                     %)
                  (:lexical-scopes request))
            total-scope (raw-scope request ['total])
            references
            (filterv #(contains? (set names) (:symbol %))
                     (:references request))
            input-reference
            (some #(when (= 'input (:symbol %)) %) (:references request))
            empty-input-reference
            (some #(when (= 'empty-input (:symbol %)) %)
                  (:references request))]
        (is vector-scope)
        (is empty-scope)
        (is total-scope)
        (is (= (:scope-id empty-scope) (:parent-scope-id total-scope)))
        (is (not (some #{(:scope-id vector-scope)}
                       (:scope-chain input-reference))))
        (is (some #{(:scope-id vector-scope)}
                  (:scope-chain empty-input-reference)))
        (is (not (some #{(:scope-id empty-scope)}
                       (:scope-chain empty-input-reference))))
        (doseq [name names]
          (let [named (filterv #(= name (:symbol %)) references)]
            (is (= 2 (count named)))
            (is (= 1 (count (filter #(some #{(:scope-id total-scope)}
                                           (:scope-chain %))
                                    named))))))))))

(deftest duplicate-vector-leaves-retain-first-occurrence-for-recovery
  (doseq [basename ["duplicate-vector-let" "duplicate-vector-loop"]
          extension extensions]
    (testing (str basename extension)
      (assert-vector-projection basename extension
                                (get vector-oracles basename)))))

(deftest simple-symbol-let-bindings-remain-unchanged
  (doseq [extension extensions]
    (testing extension
      (let [[_ request projected]
            (assert-checkpoint-artifact
             "accepted" "simple-symbol-parity" extension)
            underscore-scope (raw-scope request ['_])
            ampersand-scope (raw-scope request ['&])
            local (local-bindings projected ['_ '&])]
        (is underscore-scope)
        (is ampersand-scope)
        (is (= (:scope-id underscore-scope)
               (:parent-scope-id ampersand-scope)))
        (is (= #{'_ '&} (set (map :name local))))
        (is (= 2 (count local)))))))
