(ns gravity.self-hosting.sh07-current-data-declaration-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-DATA-ROOT"})))))

(defn- one [rows predicate]
  (let [selected (filterv predicate rows)]
    (when-not (= 1 (count selected))
      (throw (ex-info "Expected one source-bound record" {:count (count selected)})))
    (first selected)))

(defn- substitute-node [template target replacement]
  (let [changed (update template :nodes
                        #(mapv (fn [node] (if (= (:node-id target) (:node-id node)) replacement node)) %))]
    (assoc-in changed [:identity-preimage :nodes] (:nodes changed))))

(def observed-data-product (atom nil))

(deftest original-top-level-contract-map-preserves-definition-literals-and-order
  ;; A source-region join for a C01 DATA declaration. This authenticates
  ;; the fixture's own path; it does not authenticate the complete owning module.
  (let [path #(str (io/file (root) %))
        owner (path "bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity")
        owner-text (slurp owner :encoding "UTF-8")
        start (.indexOf ^String owner-text "(def authenticated-envelope-contract\n")
        end (.indexOf ^String owner-text "\n\n(" start)
        _ (is (and (<= 0 start) (< start end)))
        declaration (str/trim (subs owner-text start end))
        original (binding [*read-eval* false] (read-string declaration))
        owner-namespace (binding [*read-eval* false] (read-string owner-text))
        exports (some #(when (= :exports (first %)) (second %)) (drop 2 owner-namespace))
        values (nth original 2)
        source-path (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-envelope-contract.gravity")
        source (slurp source-path :encoding "UTF-8")
        reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        negatives (atom [])]
    (is (str/ends-with? source (str declaration "\n")))
    (is (= source (slurp (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-envelope-contract.qst") :encoding "UTF-8")))
    (is (= :gravity/authenticated-envelope-contract (:artifact values)))
    (is (= 2 (:schema-version values)))
    (is (some vector? (vals values)))
    (is (some boolean? (vals values)))
    (is (some #{'authenticated-envelope-contract} exports))
    (is (str/includes? source "(:exports [authenticated-envelope-contract])"))
    (println {:data-declaration-phase :current-reader-c5}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact source-path source
                     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []} reader)
          _ (println {:data-declaration-phase :gravity-core})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact)
          core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          definition (one (:definitions core) #(= 'authenticated-envelope-contract (:name %)))
          def-node (nodes (:core-node-id definition))
          initializer (nodes (:value-node-id definition))
          leaves (mapv nodes (:children initializer))
          form (forms (get-in initializer [:source :form-id]))
          ordered (mapv #(get-in forms [% :value]) (:child-form-ids form))
          binding (bindings (:binding-id definition))
          c2 (get-in resolved [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])]
      (is (= :accepted (:status resolved) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= original (second (:parsed-semantic-values c2))))
      (is (= source-path (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner source-path))
      (is (= (str "sha256:" (b/sha256-hex source)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (b/sha256-hex (slurp reader :encoding "UTF-8")))
             (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= 1 (count (:definitions core))))
      (is (= (inc (count (tree-seq #(seq (:child-form-ids %))
                                  #(map forms (:child-form-ids %)) form)))
             (count (:nodes core))))
      (is (= [:def :initialize-once] [(:core-form def-node) (get-in def-node [:evaluation :kind])]))
      (is (= [(:value-node-id definition)] (:children def-node) (mapv :core-node-id (get-in def-node [:evaluation :order]))))
      (is (= 'authenticated-envelope-contract (:name binding) (get-in def-node [:attributes :name])))
      (is (= :public (:visibility binding) (get-in def-node [:attributes :visibility])))
      (is (= [(:binding-id definition)] (:resolved-binding-ids def-node)))
      (is (= [:collection-literal :left-to-right] [(:core-form initializer) (get-in initializer [:evaluation :kind])]))
      (is (= {:literal-kind :map :element-count (* 2 (count values)) :source-value values} (:attributes initializer)))
      (is (= ordered (mapv #(get-in % [:attributes (if (= :collection-literal (:core-form %)) :source-value :value)]) leaves)))
      (is (= (:child-form-ids form) (mapv #(get-in % [:source :form-id]) leaves)))
      (is (= (:children initializer) (mapv :core-node-id (get-in initializer [:evaluation :order]))))
      (is (= (vec (range (count ordered))) (mapv :index (get-in initializer [:evaluation :order]))))
      (doseq [[leaf expected] (map vector leaves ordered)]
        (let [input (forms (get-in leaf [:source :form-id]))]
          (is (= (:kind input) (get-in leaf [:attributes :literal-kind])))
          (is (= expected (:value input)
                 (get-in leaf [:attributes (if (= :collection-literal (:core-form leaf)) :source-value :value)])))
          (if (vector? expected)
            (do (is (= :collection-literal (:core-form leaf)))
                (is (= (:child-form-ids input) (mapv #(get-in nodes [% :source :form-id]) (:children leaf))))
                (is (= (:children leaf) (mapv :core-node-id (get-in leaf [:evaluation :order])))))
            (do (is (= :literal (:core-form leaf)))
                (is (= [] (:children leaf)))
                (is (= :value (get-in leaf [:evaluation :kind])))))))
      (doseq [node (:nodes core)]
        (let [source (:source node) input (forms (:form-id source)) mapping (mappings (:node-id node))
              fields [:syntax-id :form-id :origin-chain :generated-origin]]
          (is (and (map? input) (map? mapping)))
          (is (= (select-keys input fields) (select-keys source fields) (select-keys mapping fields)))
          (is (= (:source-span input) (:source-span mapping)))
          (is (= [:types :effects :ownership :safety] (:pending-fact-families node)))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            integer (one (:nodes template) #(= :integer (get-in % [:attributes :literal-kind])))
            changed (substitute-node template integer (update-in integer [:attributes :value] inc))
            result (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
        (swap! negatives conj {:mutation :integer-value :result result})
        (is (= (:nodes changed) (get-in changed [:identity-preimage :nodes])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) result))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            collection (one (:nodes template) #(= (:form-id form) (get-in % [:source :form-id])))
            children (:children collection)
            reordered (vec (concat (subvec children 2 4) (subvec children 0 2) (subvec children 4)))
            replacement (-> collection (assoc :children reordered)
                            (assoc-in [:evaluation :order] (mapv (fn [index id] {:index index :core-node-id id}) (range) reordered)))
            changed (substitute-node template collection replacement)
            changed (update changed :evaluation-order #(mapv (fn [row] (if (= (:core-node-id row) (:node-id collection))
                                                                        (assoc row :children reordered) row)) %))
            changed (assoc-in changed [:identity-preimage :evaluation-order] (:evaluation-order changed))
            result (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
        (swap! negatives conj {:mutation :constructor-entry-order :result result})
        (is (not= children reordered))
        (is (= (set children) (set reordered)))
        (is (= reordered (mapv :core-node-id (get-in replacement [:evaluation :order]))))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) result))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending} (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
      (is (= source (slurp source-path :encoding "UTF-8")))
      (reset! observed-data-product {:artifact artifact :negative-results @negatives}))))
