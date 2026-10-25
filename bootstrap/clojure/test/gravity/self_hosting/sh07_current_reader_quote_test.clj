(ns gravity.self-hosting.sh07-current-reader-quote-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-QUOTE-ROOT"})))))

(defn- one [rows predicate]
  (let [selected (filterv predicate rows)]
    (when-not (= 1 (count selected))
      (throw (ex-info "Expected one source-bound record" {:count (count selected)})))
    (first selected)))

(def observed-quote-product (atom nil))

(deftest original-reader-quoted-map-retains-symbol-data-without-executable-children
  ;; This helper is outside the raw SH03 prefix plan. The extracted fixture is
  ;; the authenticated subject, not the complete reader module or its runtime.
  (let [path #(str (io/file (root) %))
        owner (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        owner-text (slurp owner :encoding "UTF-8")
        start (.indexOf ^String owner-text "(defn read-source-form-records [source-bytes]\n")
        end (.indexOf ^String owner-text "(def stage1-reader-table\n" start)
        _ (is (and (<= 0 start) (< start end)))
        declaration (str/trim (subs owner-text start end))
        original (binding [*read-eval* false] (read-string declaration))
        owner-namespace (binding [*read-eval* false] (read-string owner-text))
        exports (some #(when (= :exports (first %)) (second %)) (drop 2 owner-namespace))
        payload (second (nth original 3))
        source-path (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-reader-quote.gravity")
        source (slurp source-path :encoding "UTF-8")
        negatives (atom [])]
    (is (str/ends-with? source (str declaration "\n")))
    (is (= source (slurp (path "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-reader-quote.qst") :encoding "UTF-8")))
    (is (some #{'read-source-form-records} exports))
    (is (str/includes? source "(:exports [read-source-form-records])"))
    (println {:reader-quote-phase :current-reader-c5}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact source-path source
                     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []} owner)
          _ (println {:reader-quote-phase :gravity-core})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact)
          core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
          function (one (:function-records core) #(= 'read-source-form-records (:definition-name %)))
          quote-node (nodes (:body-core-node-id function))
          quote-form (forms (get-in quote-node [:source :form-id]))
          quoted-form (forms (second (:child-form-ids quote-form)))
          definition (one (:definitions core) #(= 'read-source-form-records (:name %)))
          c2 (get-in resolved [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])]
      (is (= :accepted (:status resolved) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= original (second (:parsed-semantic-values c2))))
      (is (= source-path (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner source-path))
      (is (= (str "sha256:" (b/sha256-hex source)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (b/sha256-hex owner-text)) (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= [:public 'read-source-form-records] ((juxt :visibility :name) (bindings (:binding-id definition)))))
      (is (= 1 (:fixed-arity function) (count (:parameter-binding-ids function))))
      (is (= '[source-bytes] (mapv #(get-in bindings [% :name]) (:parameter-binding-ids function))))
      (is (= 3 (count (:nodes core))))
      (is (= #{:def :fn :quote} (set (map :core-form (:nodes core)))))
      (is (empty? (:reference-uses core)))
      (is (empty? (:calls core)))
      (is (= :quote (:core-form quote-node)))
      (is (= [] (:children quote-node) (get-in quote-node [:evaluation :order])))
      (is (= :no-evaluation (get-in quote-node [:evaluation :kind])))
      (is (= {:quoted-form-id (:form-id quoted-form) :quoted-syntax-id (:syntax-id quoted-form)
              :quoted-kind :map :quoted-value payload} (:attributes quote-node)))
      (is (= 'source-bytes (get-in quote-node [:attributes :quoted-value :input])))
      (is (symbol? (get-in quote-node [:attributes :quoted-value :input])))
      (is (= [:forms :source-spans :reader-diagnostics] (get-in quote-node [:attributes :quoted-value :emits])))
      (doseq [node (:nodes core)]
        (let [source (:source node) input (forms (:form-id source)) mapping (mappings (:node-id node))
              fields [:syntax-id :form-id :origin-chain :generated-origin]]
          (is (and (map? input) (map? mapping)))
          (is (= (select-keys input fields) (select-keys source fields) (select-keys mapping fields)))
          (is (= (:source-span input) (:source-span mapping)))
          (is (= [:types :effects :ownership :safety] (:pending-fact-families node)))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            target (one (:nodes template) #(= (:form-id quote-form) (get-in % [:source :form-id])))]
        (doseq [[kind value] [[:symbol-data (assoc payload :input 'other-source-bytes)]
                            [:quoted-vector-order (assoc payload :emits [:forms :reader-diagnostics :source-spans])]]]
          (let [replacement (assoc-in target [:attributes :quoted-value] value)
                changed (update template :nodes #(mapv (fn [node] (if (= (:node-id target) (:node-id node)) replacement node)) %))
                changed (assoc-in changed [:identity-preimage :nodes] (:nodes changed))
                rejected (b/sh07-core-execute! source-path 'sh07-verify-core-template [request changed (:digest-requests boundary)])]
            (swap! negatives conj {:mutation kind :result rejected})
            (is (not= payload value))
            (is (= [] (:children replacement) (get-in replacement [:evaluation :order])))
            (is (= (:nodes changed) (get-in changed [:identity-preimage :nodes])))
            (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected))))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending} (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (false? (get-in artifact [:execution-boundary :sh07-complete?])))
      (is (= source (slurp source-path :encoding "UTF-8")))
      (reset! observed-quote-product {:artifact artifact :negative-results @negatives}))))
