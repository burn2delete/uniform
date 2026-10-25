(ns gravity.self-hosting.sh07-current-map-guard-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (io/file (System/getProperty "user.dir"))]
    (cond (.isFile (io/file directory "deps.edn")) directory
          (.getParentFile directory) (recur (.getParentFile directory))
          :else (throw (ex-info "Repository absent" {:id "SH07-MAP-GUARD-ROOT"})))))

(defn- one [rows predicate]
  (let [selected (filterv predicate rows)]
    (when-not (= 1 (count selected))
      (throw (ex-info "Expected one source-bound record" {:count (count selected)})))
    (first selected)))

(deftest current-map-guard-source-to-core-and-branch-substitution-replay
  ;; Original text is an extraction check; SOURCE4 authenticates this fixture.
  ;; No C7 annotation narrows the original non-Map false branch.
  (let [path #(str (io/file (root) %))
        owner-path (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        prefix "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-exact-keys"
        source-path (path (str prefix ".gravity"))
        text (slurp source-path :encoding "UTF-8")
        source (slurp owner-path :encoding "UTF-8")
        start (.indexOf ^String source "(defn sh07-exact-keys? ")
        end (.indexOf ^String source "(defn sh07-vector-contains? " start)
        _ (is (and (<= 0 start) (< start end)))
        helper (str/trim (subs source start end))
        original (binding [*read-eval* false] (read-string helper))]
    (is (str/ends-with? text (str helper "\n")))
    (is (= text (slurp (path (str prefix ".qst")) :encoding "UTF-8")))
    (println {:map-guard-phase :current-reader-c5}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact
                    source-path text
                    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                    reader-path)
          _ (println {:map-guard-phase :gravity-core})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact)
          core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          expanded (:sh05-macro-artifact resolved)
          c2 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
          function (one (:function-records core) #(= 'sh07-exact-keys? (:definition-name %)))
          call-name #(get-in nodes [(:operator-node-id %) :attributes :symbol])
          call-at (fn [id] (one (:calls core) #(= id (:core-node-id %))))
          outer (nodes (:body-core-node-id function))
          [map-id inner-id outer-false-id] (:children outer)
          inner (nodes inner-id)
          [count-equal-id key-equal-id inner-false-id] (:children inner)
          map-call (call-at map-id)
          count-equal (call-at count-equal-id)
          key-equal (call-at key-equal-id)
          [value-id expected-id] (:parameter-binding-ids function)
          reference-id #(get-in nodes [% :attributes :binding-id])]
      (is (= :accepted (:status resolved) (:status expanded) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status])
             (get-in boundary [:resolved-verification :status])))
      (is (= original (second (:parsed-semantic-values c2))))
      (is (= (list 'def 'sh07-exact-keys? (cons 'fn (drop 2 original))) (first (:expanded-forms expanded))))
      (is (= source-path (get-in resolved [:provenance :source-path])
             (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner-path source-path))
      (is (= (str "sha256:" (b/sha256-hex text)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (b/sha256-hex (slurp reader-path :encoding "UTF-8")))
             (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= [1 2 7] (mapv #(count (get core %)) [:function-records :control-flow :calls])))
      (is (= :private (get-in bindings [(:definition-binding-id function) :visibility])))
      (is (= '[value expected] (mapv #(get-in bindings [% :name]) (:parameter-binding-ids function))))
      (is (= 2 (:fixed-arity function)))
      (is (= '{map? 1 = 2 count 2 set 1 keys 1} (frequencies (map call-name (:calls core)))))
      (is (= '[map? = =] (mapv call-name [map-call count-equal key-equal])))
      (is (= [value-id] (mapv reference-id (:argument-node-ids map-call))))
      (let [counts (mapv call-at (:argument-node-ids count-equal))
            set-call (call-at (first (:argument-node-ids key-equal)))
            keys-call (call-at (first (:argument-node-ids set-call)))]
        (is (= '[count count] (mapv call-name counts)))
        (is (= [value-id expected-id] (mapv #(reference-id (first (:argument-node-ids %))) counts)))
        (is (= '[set keys] (mapv call-name [set-call keys-call])))
        (is (= [value-id] (mapv reference-id (:argument-node-ids keys-call))))
        (is (= expected-id (reference-id (second (:argument-node-ids key-equal))))))
      (doseq [node [outer inner]]
        (let [control (one (:control-flow core) #(= (:node-id node) (:core-node-id %)))]
          (is (= :if (:core-form node)))
          (is (= {:condition-child-index 0 :then-child-index 1 :else-child-index 2
                  :condition-evaluation :always-first :branch-evaluation :exactly-one
                  :truthiness {:false-values [:nil :false] :other-values :true}} (:attributes node)))
          (is (= (:children node) (into [(:condition-node-id control)] (map :core-node-id (:branches control)))))
          (is (= [:then :else] (mapv :role (:branches control))))
          (is (= :exactly-one (:branch-exclusivity control)))
          (is (= (vec (rest (:child-form-ids (forms (get-in node [:source :form-id])))))
                 (mapv #(get-in nodes [% :source :form-id]) (:children node))))))
      (is (= [false false] (mapv #(get-in nodes [% :attributes :value]) [outer-false-id inner-false-id])))
      (doseq [call (:calls core)]
        (let [node (nodes (:core-node-id call)) ids (into [(:operator-node-id call)] (:argument-node-ids call))]
          (is (= [:core 'gravity.core (call-name call)]
                 ((juxt :binding-class :namespace :name) (bindings (:operator-binding-id call)))))
          (is (= ids (:children node) (:ordered-evaluation-node-ids call)
                 (mapv :core-node-id (get-in node [:evaluation :order]))))))
      (doseq [node (:nodes core)]
        (let [source (:source node) form (forms (:form-id source)) mapping (mappings (:node-id node))
              fields [:syntax-id :form-id :origin-chain :generated-origin]]
          (is (and (map? form) (map? mapping)))
          (is (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields)))
          (is (= (:source-span form) (:source-span mapping)))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            target (one (:nodes template) #(= (get-in outer [:source :form-id]) (get-in % [:source :form-id])))
            [condition then else] (:children target)
            changed (update template :nodes #(mapv (fn [node] (if (= (:node-id target) (:node-id node))
                                                                (assoc node :children [condition else then]) node)) %))
            changed (update changed :identity-preimage assoc :nodes (:nodes changed))
            _ (println {:map-guard-phase :hostile-branch-replay})
            rejected (b/sh07-core-execute! source-path 'sh07-verify-core-template
                                           [request changed (:digest-requests boundary)])]
        (is (not= then else))
        (is (= (:nodes changed) (get-in changed [:identity-preimage :nodes])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
             (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (= text (slurp source-path :encoding "UTF-8"))))))
