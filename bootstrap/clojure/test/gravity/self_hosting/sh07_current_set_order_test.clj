(ns gravity.self-hosting.sh07-current-set-order-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-SET-ROOT"})))))

(defn- one [rows predicate]
  (let [selected (filterv predicate rows)]
    (when-not (= 1 (count selected))
      (throw (ex-info "Expected one source-bound record" {:count (count selected)})))
    (first selected)))

(def observed-set-product (atom nil))

(defn- source-function [records name]
  (one records #(and (seq? (:value %)) (= 'defn (first (:value %))) (= name (second (:value %))))))

(defn- source-body [index function]
  (index (nth (:children function) 3)))

(defn- check-source-order! [source-index nodes source node]
  (let [kind (:kind source) value (:value source)]
    (cond
      (and (seq? value) (= 'quote (first value)))
      (do (is (= :quote (:core-form node)))
          (is (= [] (:children node) (get-in node [:evaluation :order])))
          (is (= :no-evaluation (get-in node [:evaluation :kind])))
          (is (= (second value) (get-in node [:attributes :quoted-value]))))

      (contains? #{:map :vector :set :list} kind)
      (do (is (= (if (= :list kind) :call :collection-literal) (:core-form node)))
          (when-not (= :list kind)
            (is (= kind (get-in node [:attributes :literal-kind])))
            (is (= value (get-in node [:attributes :source-value]))))
          (is (= (count (:children source)) (count (:children node))))
          (is (= (:children node) (mapv :core-node-id (get-in node [:evaluation :order]))))
          (doseq [[input child] (map vector (:children source) (:children node))]
            (check-source-order! source-index nodes (source-index input) (nodes child))))

      (= :symbol kind)
      (do (is (= :reference (:core-form node)))
          (is (= value (get-in node [:attributes :symbol]))))

      :else
      (do (is (= :literal (:core-form node)))
          (is (= kind (get-in node [:attributes :literal-kind])))
          (is (= value (get-in node [:attributes :value])))))))

(defn- failure [operation]
  (try (operation) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))

(deftest original-set-constructor-retains-c2-source-child-order
  (let [path #(str (io/file (root) %))
        owner (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        owner-text (slurp owner :encoding "UTF-8")
        start (.indexOf ^String owner-text "(defn sh07-collection-kind? [kind]\n")
        end (.indexOf ^String owner-text "(defn sh07-even-count? " start)
        _ (is (and (<= 0 start) (< start end)))
        declaration (str/trim (subs owner-text start end))
        original (binding [*read-eval* false] (read-string declaration))
        owner-namespace (binding [*read-eval* false] (read-string owner-text))
        exports (some #(when (= :exports (first %)) (second %)) (drop 2 owner-namespace))
        source-path (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-collection-kind.gravity")
        source (slurp source-path :encoding "UTF-8")]
    (is (str/ends-with? source (str declaration "\n")))
    (is (= source (slurp (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-collection-kind.qst") :encoding "UTF-8")))
    (is (not (some #{'sh07-collection-kind?} exports)))
    (println {:set-order-phase :current-reader-c5}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact source-path source
                     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                     (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"))
          _ (println {:set-order-phase :gravity-core})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact)
          core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          c2 (get-in resolved [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          c2-forms (into {} (map (juxt :form-id identity)) (:form-tree c2))
          source-root (source-function (:form-tree c2) 'sh07-collection-kind?)
          source-call (source-body c2-forms source-root)
          source-set (c2-forms (second (:children source-call)))
          source-order (mapv #(get-in c2-forms [% :value]) (:children source-set))
          function (one (:function-records core) #(= 'sh07-collection-kind? (:definition-name %)))
          call (one (:calls core) #(= (:body-core-node-id function) (:core-node-id %)))
          constructor (nodes (first (:argument-node-ids call)))
          core-order (mapv #(get-in nodes [% :attributes :value]) (:children constructor))
          projected (forms (get-in constructor [:source :form-id]))
          projected-order (mapv #(get-in forms [% :value]) (:child-form-ids projected))
          negatives (atom [])]
      (is (= :accepted (:status resolved) (:status artifact)))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= original (one (:parsed-semantic-values c2) #(and (seq? %) (= 'sh07-collection-kind? (second %))))))
      (is (= source-path (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (= [:vector :map :set] source-order))
      (is (= source-order projected-order) "C6 request must retain C2's evaluated Set element order.")
      (is (= source-order core-order) "Core constructor must retain C2's evaluated Set element order.")
      (is (= #{:vector :map :set} (set core-order) (get-in constructor [:attributes :source-value])))
      (is (= :left-to-right (get-in constructor [:evaluation :kind])))
      (is (= (:children constructor) (mapv :core-node-id (get-in constructor [:evaluation :order]))))
      (is (= [0 1 2] (mapv :index (get-in constructor [:evaluation :order]))))
      (is (= [(:node-id constructor) (second (:argument-node-ids call))] (:argument-node-ids call)))
      (is (= (first (:parameter-binding-ids function))
             (get-in nodes [(second (:argument-node-ids call)) :attributes :binding-id])))
      (is (= :private (get-in bindings [(:definition-binding-id function) :visibility])))
      (is (= :contains? (keyword (str (get-in nodes [(:operator-node-id call) :attributes :symbol])))))
      (is (= [:kind] (mapv #(keyword (str (get-in bindings [% :name]))) (:parameter-binding-ids function))))
      (doseq [name '[sh07-collection-kind? set-call-order nested-set-order]]
        (let [input (source-function (:form-tree c2) name)
              function (one (:function-records core) #(= name (:definition-name %)))]
          (check-source-order! c2-forms nodes (source-body c2-forms input) (nodes (:body-core-node-id function)))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            function (one (:function-records core) #(= 'set-call-order (:definition-name %)))
            collection (nodes (:body-core-node-id function))
            target (one (:nodes template) #(= (get-in collection [:source :form-id]) (get-in % [:source :form-id])))
            children (:children target) changed-order (vec (concat (rest children) [(first children)]))
            replacement (-> target (assoc :children changed-order)
                            (assoc-in [:evaluation :order] (mapv (fn [index id] {:index index :core-node-id id}) (range) changed-order)))
            changed (update template :nodes #(mapv (fn [node] (if (= (:node-id target) (:node-id node)) replacement node)) %))
            changed (update changed :evaluation-order #(mapv (fn [row] (if (= (:node-id target) (:core-node-id row))
                                                                       (assoc row :children changed-order) row)) %))
            changed (update changed :identity-preimage assoc :nodes (:nodes changed) :evaluation-order (:evaluation-order changed))
            rejected (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
        (swap! negatives conj {:mutation :evaluated-set-call-order :result rejected})
        (is (= :collection-literal (:core-form collection)))
        (is (not= children changed-order))
        (is (= (set children) (set changed-order)))
        (is (= changed-order (mapv :core-node-id (get-in replacement [:evaluation :order]))))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected))))
      (let [context! (ns-resolve 'gravity.bootstrap 'sh07-core-source-occurrence-context)
            syntaxes (->> (get-in resolved [:sh05-macro-artifact :expanded-syntax-stream])
                          (remove #(and (seq? (:form %)) (= 'ns (first (:form %)))))
                          (map-indexed (fn [index syntax]
                                         (assoc syntax :sh07/root-syntax-id
                                                (b/sh07-core-root-syntax-id (get-in request [:lineage :source-revision-id]) index (:form syntax))))) vec)
            base-path [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :form-tree]
            bad-child (first (:children source-set))
            change-value (fn [records] (mapv #(if (= bad-child (:form-id %)) (assoc % :value :not-in-the-set) %) records))
            malformed (update-in resolved base-path change-value)
            duplicate (update-in malformed base-path
                                 #(mapv (fn [record] (if (= (:form-id source-set) (:form-id record))
                                                      (assoc record :children [bad-child bad-child (last (:children source-set))]) record)) %))]
        ;; These are explicitly malformed private-adapter inputs, not fresh C5 products.
        (is (= ["C6-ORIGIN" :map-source-set-bijection]
               ((juxt :id :reason) (failure #(context! malformed syntaxes)))))
        (is (= ["C6-ORIGIN" :map-source-child-link]
               ((juxt :id :reason) (failure #(context! duplicate syntaxes))))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending} (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
      (reset! observed-set-product {:artifact artifact :source-set source-set
                                   :source-children (mapv c2-forms (:children source-set))
                                   :negative-results @negatives
                                   :source-order source-order :projected-order projected-order :core-order core-order}))))
