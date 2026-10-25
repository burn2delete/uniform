(ns gravity.self-hosting.sh07-current-multi-slot-helper-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(defn- root []
  (loop [directory (.getParentFile (io/file (io/resource "gravity/self_hosting/sh07_current_multi_slot_helper_test.clj")))]
    (cond (nil? directory) (throw (ex-info "Repository absent" {:id "SH07-MULTI-SLOT-ROOT"}))
          (.isFile (io/file directory "deps.edn")) directory
          :else (recur (.getParentFile directory)))))
(defn- one [rows predicate]
  (let [matches (filterv predicate rows)]
    (when-not (= 1 (count matches)) (throw (ex-info "Unique source-bound row required" {:count (count matches)})))
    (first matches)))
(defn- original-helper [source]
  (let [start (.indexOf ^String source "(defn sh07-all-unique? ")
        end (.indexOf ^String source "(defn sh07-vector-subset? " start)]
    (when-not (and (<= 0 start) (< start end)) (throw (ex-info "Original occurrence absent" {:id "SH07-MULTI-SLOT-SOURCE"})))
    (str/trim (subs source start end))))

(deftest original-two-slot-helper-binds-initializers-and-simultaneous-transfer
  ;; C01/C02/C07, L2/C6: the fixture is an exact extracted occurrence. Only
  ;; its G path executes; Q checks text parity. Full-owner/C7/runtime stay open.
  (let [path #(str (io/file (root) %))
        owner (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        reader (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        fixture "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-all-unique"
        source (path (str fixture ".gravity")) text (slurp source :encoding "UTF-8")
        helper (original-helper (slurp owner :encoding "UTF-8"))
        original (binding [*read-eval* false] (read-string helper))]
    (is (str/ends-with? text (str helper "\n")))
    (is (= text (slurp (path (str fixture ".qst")) :encoding "UTF-8")))
    (is (< (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)) 2048))
    (println {:multi-slot-phase :current-reader-c5-begin}) (flush)
    (let [resolved (b/compiler-c5-resolution-source-artifact source text
                     {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []} reader)
          _ (println {:multi-slot-phase :gravity-core-begin})
          artifact (b/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact) core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          expanded (:sh05-macro-artifact resolved)
          c2 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          function (one (:function-records core) #(= 'sh07-all-unique? (:definition-name %)))
          slots (:loop-bindings core) slot-ids (mapv :binding-id slots)
          [remaining observed] slots
          target (one (:recur-targets core) #(= :loop (:target-kind %)))
          transfer (one (:recur-transfers core) (constantly true))
          recur-node (nodes (:recur-core-node-id transfer))
          loop-node (nodes (:owner-core-node-id target))
          call-name #(get-in nodes [(:operator-node-id %) :attributes :symbol])
          rest-call (one (:calls core) #(= 'rest (call-name %)))
          conj-call (one (:calls core) #(= 'conj (call-name %)))
          rest-arg (nodes (first (:argument-node-ids rest-call)))
          conj-args (mapv nodes (:argument-node-ids conj-call))]
      (is (= :accepted (:status resolved) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status]) (get-in boundary [:resolved-verification :status])))
      (is (= original (second (:parsed-semantic-values c2))))
      (is (= (list 'def 'sh07-all-unique? (cons 'fn (drop 2 original))) (first (:expanded-forms expanded))))
      (is (= source (get-in c2 [:source-unit-record :path]) (get-in core [:provenance :actual-source-path])))
      (is (not= owner source))
      (is (= (str "sha256:" (b/sha256-hex text)) (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (b/sha256-hex (slurp reader :encoding "UTF-8")))
             (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= :private (get-in bindings [(:definition-binding-id function) :visibility])))
      (is (= ['remaining 'observed] (mapv :name slots)))
      (is (= 2 (count slots) (count (set slot-ids)) (:arity target) (:arity transfer)))
      (is (= slot-ids (:binding-ids target)))
      (is (= (:target-id target) (:target-id transfer)))
      (is (= (:body-core-node-id function) (:node-id loop-node)))
      (is (= (mapv :initializer-node-id slots) (vec (take 2 (:children loop-node)))))
      (is (= (first (:parameter-binding-ids function))
             (get-in nodes [(:initializer-node-id remaining) :attributes :binding-id])))
      (let [empty-set (nodes (:initializer-node-id observed))]
        (is (= :collection-literal (:core-form empty-set)))
        (is (= #{} (get-in empty-set [:attributes :source-value])))
        (is (empty? (:children empty-set))))
      (is (= [(:core-node-id rest-call) (:core-node-id conj-call)] (:argument-node-ids transfer) (:children recur-node)))
      (is (= (:children recur-node) (mapv :core-node-id (get-in recur-node [:evaluation :order]))))
      (is (= [:arguments-left-to-right :nearest-lexical-recur-target true :pending-sh08]
             ((juxt :evaluation-order :transfer-policy :tail-position :type-compatibility) transfer)))
      (is (= (:binding-id remaining) (get-in rest-arg [:attributes :binding-id])))
      (is (= (:binding-id observed) (get-in (first conj-args) [:attributes :binding-id])))
      (let [first-call (one (:calls core) #(= (:node-id (second conj-args)) (:core-node-id %)))
            argument (nodes (first (:argument-node-ids first-call)))]
        (is (= 'first (call-name first-call)))
        (is (= (:binding-id remaining) (get-in argument [:attributes :binding-id]))))
      (doseq [slot slots]
        (is (= (:node-id loop-node) (:loop-core-node-id slot)))
        (is (= :immutable (:mutability slot)))
        (is (= (:initializer-form-id slot) (get-in nodes [(:initializer-node-id slot) :source :form-id]))))
      (doseq [call (:calls core)]
        (let [ordered (into [(:operator-node-id call)] (:argument-node-ids call))
              node (nodes (:core-node-id call)) form (forms (get-in node [:source :form-id]))]
          (is (= ordered (:children node) (:ordered-evaluation-node-ids call)))
          (is (= (:child-form-ids form) (mapv #(get-in nodes [% :source :form-id]) ordered)))))
      (doseq [node (:nodes core)]
        (let [form (forms (get-in node [:source :form-id]))]
          (is (= (select-keys form [:syntax-id :form-id :origin-chain :generated-origin])
                 (select-keys (:source node) [:syntax-id :form-id :origin-chain :generated-origin])))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            original-transfer (first (:recur-transfers template))
            recur-id (:recur-core-node-id original-transfer)
            reversed (vec (reverse (:argument-node-ids original-transfer)))
            changed (-> template
                        (update :recur-transfers #(mapv (fn [row] (assoc row :argument-node-ids reversed)) %))
                        (update :nodes #(mapv (fn [node]
                                               (if (= recur-id (:node-id node))
                                                 (-> node (assoc :children reversed)
                                                     (update-in [:evaluation :order]
                                                       (fn [rows] (mapv (fn [index row] (assoc row :index index))
                                                                         (range) (reverse rows))))) node)) %)))
            changed (update changed :identity-preimage merge (select-keys changed [:nodes :recur-transfers]))
            _ (println {:multi-slot-phase :fresh-hostile-template-replay-begin})
            rejected (b/sh07-core-execute! source 'sh07-verify-core-template
                                          [request changed (:digest-requests boundary)])]
        (is (not= reversed (:argument-node-ids original-transfer)))
        (is (= reversed (:argument-node-ids (first (:recur-transfers changed)))
               (:children (one (:nodes changed) #(= recur-id (:node-id %))))))
        (let [order (get-in (one (:nodes changed) #(= recur-id (:node-id %))) [:evaluation :order])]
          (is (= [0 1] (mapv :index order)))
          (is (= reversed (mapv :core-node-id order))))
        (is (= (select-keys changed [:nodes :recur-transfers])
               (select-keys (:identity-preimage changed) [:nodes :recur-transfers])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected))))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
             (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (= text (slurp source :encoding "UTF-8")))
      (println {:multi-slot-phase :complete}) (flush))))
