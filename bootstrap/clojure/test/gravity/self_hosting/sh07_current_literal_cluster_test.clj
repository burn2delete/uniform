(ns gravity.self-hosting.sh07-current-literal-cluster-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.self-hosting.sh07-current-set-order-test]))

(defn- helper [name]
  (ns-resolve 'gravity.self-hosting.sh07-current-set-order-test name))

(defn- path [relative]
  (.getCanonicalPath (io/file ((helper 'root)) relative)))

(def observed-literal-cluster (atom nil))

(deftest original-literal-cluster-preserves-character-occurrences-callees-and-loop-target
  (let [owner (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        owner-text (slurp owner :encoding "UTF-8")
        start (.indexOf ^String owner-text "(defn sh07-literal-kind? [kind]\n")
        end (.indexOf ^String owner-text "(defn sh07-exact-decimal-descriptor? " start)
        _ (is (and (<= 0 start) (< start end)))
        cluster (str/trim (subs owner-text start end))
        originals (binding [*read-eval* false] (vec (rest (read-string (str "(do\n" cluster "\n)")))))
        names (mapv second originals)
        source-path (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-literal-cluster.gravity")
        source (slurp source-path :encoding "UTF-8")]
    (is (str/ends-with? source (str cluster "\n")))
    (is (= source (slurp (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-literal-cluster.qst") :encoding "UTF-8")))
    (is (= '[sh07-literal-kind? sh07-decimal-digit? sh07-all-decimal-digits? sh07-canonical-signed-integer-string?] names))
    (println {:literal-cluster-phase :current-source-to-core}) (flush)
    (let [artifact (b/sh07-public-core-file-artifact source-path {:reader :development-current})
          resolved (:sh06-resolution-artifact artifact)
          boundary (:gravity-core-boundary artifact) core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          maps (into {} (map (juxt :core-node-id identity)) (:source-map core))
          functions (into {} (map (juxt :definition-name identity)) (:function-records core))
          definitions (into {} (map (juxt :name identity)) (:definitions core))
          c2 (get-in resolved [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          c2-index (into {} (map (juxt :form-id identity)) (:form-tree c2))
          decoded (filterv #(= :character (:kind %)) (:literal-decoding-records c2))
          decoded-index (into {} (map (juxt :form-id identity)) decoded)
          syntaxes (->> (get-in resolved [:sh05-macro-artifact :expanded-syntax-stream])
                        (remove #(and (seq? (:form %)) (= 'ns (first (:form %)))))
                        (map-indexed (fn [index syntax]
                                       (assoc syntax :sh07/root-syntax-id
                                              (b/sh07-core-root-syntax-id (get-in request [:lineage :source-revision-id]) index (:form syntax))))) vec)
          contexts ((ns-resolve 'gravity.bootstrap 'sh07-core-source-occurrence-context) resolved syntaxes)
          character-sources (vec (for [[root context] contexts [position input] (:sources context)
                                      :when (= :character (:kind input))]
                                  {:source input :projected (b/sh07-core-projected-form-id
                                                            (get-in request [:lineage :source-revision-id]) root position :character)}))
          by-form (into {} (map (fn [node] [(get-in node [:source :form-id]) node])) (:nodes core))
          characters (filterv #(= :character (get-in % [:attributes :literal-kind])) (:nodes core))]
      (is (= :accepted (:status artifact) (:status resolved)))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= originals (vec (rest (:parsed-semantic-values c2)))))
      (is (= source-path (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner source-path))
      (is (= (str "sha256:" (b/sha256-hex source)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (set names) (set (keys functions)) (set (keys definitions))))
      (doseq [original originals
              :let [name (second original) function (functions name) definition (definitions name)]]
        (is (= 1 (:fixed-arity function)))
        (is (= (nth original 2) (mapv #(get-in bindings [% :name]) (:parameter-binding-ids function))))
        (is (= :private (get-in bindings [(:definition-binding-id function) :visibility])))
        (is (= (:binding-id definition) (:definition-binding-id function))))
      (is (= 13 (count decoded) (count character-sources) (count characters)))
      (is (= (set (map :form-id decoded)) (set (map #(get-in % [:source :form-id]) character-sources))))
      (is (= 13 (count (set (map :projected character-sources)))))
      (doseq [{:keys [source projected]} character-sources
              :let [record (decoded-index (:form-id source)) node (by-form projected)]]
        (is (= :literal (:core-form node)))
        (is (= :character (get-in node [:attributes :literal-kind]) (:kind record)))
        (is (= (:value source) (:decoded record) (get-in node [:attributes :value])))
        (is (= (:raw source) (:raw record)))
        (is (= (:span source) (:span record))))
      (doseq [name '[sh07-literal-kind? sh07-decimal-digit?]
              :let [function (functions name)
                    call ((helper 'one) (:calls core) #(= (:body-core-node-id function) (:core-node-id %)))
                    constructor (nodes (first (:argument-node-ids call)))
                    input ((helper 'source-body) c2-index ((helper 'source-function) (:form-tree c2) name))
                    input-set (c2-index (second (:children input)))
                    expected (mapv #(get-in c2-index [% :value]) (:children input-set))]]
        (is (= :set (get-in constructor [:attributes :literal-kind])))
        (is (= expected (mapv #(get-in nodes [% :attributes :value]) (:children constructor))))
        (is (= (:children constructor) (mapv :core-node-id (get-in constructor [:evaluation :order]))))
        (is (= :left-to-right (get-in constructor [:evaluation :kind]))))
      (let [local-names #{'sh07-decimal-digit? 'sh07-all-decimal-digits?}
            calls (filterv #(contains? local-names (get-in nodes [(:operator-node-id %) :attributes :symbol])) (:calls core))]
        (is (= {'sh07-decimal-digit? 1 'sh07-all-decimal-digits? 2}
               (frequencies (map #(get-in nodes [(:operator-node-id %) :attributes :symbol]) calls))))
        (doseq [call calls :let [name (get-in nodes [(:operator-node-id call) :attributes :symbol])]]
          (is (= (:definition-binding-id (functions name)) (:operator-binding-id call)))
          (is (= 1 (count (:argument-node-ids call))))
          (is (= (into [(:operator-node-id call)] (:argument-node-ids call)) (:ordered-evaluation-node-ids call)))))
      (let [binding ((helper 'one) (:loop-bindings core) (constantly true))
            loop-node (nodes (:loop-core-node-id binding))
            transfer ((helper 'one) (:recur-transfers core) (constantly true))
            target ((helper 'one) (:recur-targets core) #(= (:target-id transfer) (:target-id %)))
            recur-node (nodes (:recur-core-node-id transfer))
            parameter (first (:parameter-binding-ids (functions 'sh07-all-decimal-digits?)))
            parent ((helper 'one) (:recur-targets core) #(= (:parent-target-id target) (:target-id %)))]
        (is (= 'remaining (:name binding)))
        (is (= parameter (get-in nodes [(:initializer-node-id binding) :attributes :binding-id])))
        (is (= (:target-id target) (get-in loop-node [:attributes :target-id]) (get-in recur-node [:attributes :target-id])))
        (is (= :loop (:target-kind target) (:target-kind transfer)))
        (is (= [(:binding-id binding)] (:binding-ids target) (:resolved-binding-ids recur-node)))
        (is (= (:value-node-id (definitions 'sh07-all-decimal-digits?)) (:owner-core-node-id parent)))
        (is (= :pending-sh08 (:type-compatibility transfer)))
        (is (true? (:tail-position transfer)))
        (is (= (:children recur-node) (:argument-node-ids transfer)))
        (is (= :nearest-lexical-recur-target (:transfer-policy transfer))))
      (doseq [node (:nodes core)
              :let [source (:source node) form (forms (:form-id source)) mapping (maps (:node-id node))
                    fields [:syntax-id :form-id :origin-chain :generated-origin]]]
        (is (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields)))
        (is (= (:source-span form) (:source-span mapping)))
        (is (= [:types :effects :ownership :safety] (:pending-fact-families node))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            original-char ((helper 'one) (:nodes template) #(and (= :character (get-in % [:attributes :literal-kind]))
                                                                               (= (char 45) (get-in % [:attributes :value]))))
            replacement (assoc-in original-char [:attributes :value] (char 43))
            changed (update template :nodes #(mapv (fn [node] (if (= (:node-id original-char) (:node-id node)) replacement node)) %))
            changed (assoc-in changed [:identity-preimage :nodes] (:nodes changed))
            rejected (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
        (is (= (:nodes changed) (get-in changed [:identity-preimage :nodes])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected)))
        (reset! observed-literal-cluster {:artifact artifact :rejection rejected :character-occurrences character-sources}))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending} (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (false? (get-in artifact [:execution-boundary :sh07-complete?]))))))
