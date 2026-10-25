(ns gravity.self-hosting.sh07-current-loop-helper-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

(defn- repository-root []
  (loop [directory (.getParentFile
                    (io/file (io/resource "gravity/self_hosting/sh07_current_loop_helper_test.clj")))]
    (cond
      (nil? directory) (throw (ex-info "Repository root absent" {:id "SH07-HELPER-ROOT"}))
      (.isFile (io/file directory "deps.edn")) directory
      :else (recur (.getParentFile directory)))))

(defn- one [records predicate]
  (let [selected (filterv predicate records)]
    (when-not (= 1 (count selected))
      (throw (ex-info "Expected one source-bound record" {:count (count selected)})))
    (first selected)))

(defn- selected-helper [source]
  (let [start (.indexOf ^String source "(defn sh07-vector-contains? ")
        end (.indexOf ^String source "(defn sh07-all-unique? " start)]
    (when-not (and (<= 0 start) (< start end))
      (throw (ex-info "Original helper occurrence absent" {:id "SH07-HELPER-SOURCE"})))
    (str/trim (subs source start end))))

(deftest current-loop-helper-source-to-gravity-core-and-hostile-replay
  ;; Host extraction checks the unchanged occurrence. Only SOURCE4 authenticates
  ;; the separate fixture; this never authenticates the full production module.
  (let [root (repository-root)
        path #(str (io/file root %))
        owner-path (path "bootstrap/gravity/src/gravity/checked_core.gravity")
        reader-path (path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")
        fixture-prefix "bootstrap/clojure/fixtures/self-hosting/sh-07/compiler-vector-contains"
        source-path (path (str fixture-prefix ".gravity"))
        text (slurp source-path :encoding "UTF-8")
        helper (selected-helper (slurp owner-path :encoding "UTF-8"))
        original (binding [*read-eval* false] (read-string helper))]
    (is (<= (alength (.getBytes ^String text java.nio.charset.StandardCharsets/UTF_8)) 2048))
    (is (str/ends-with? text (str helper "\n")))
    (is (= text (slurp (path (str fixture-prefix ".qst")) :encoding "UTF-8")))
    (println {:loop-helper-phase :current-reader-c5-begin})
    (let [resolved (bootstrap/compiler-c5-resolution-source-artifact
                    source-path text
                    {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts []}
                    reader-path)
          _ (println {:loop-helper-phase :gravity-core-begin})
          artifact (bootstrap/sh07-core-from-resolution-artifact resolved)
          boundary (:gravity-core-boundary artifact)
          core (:canonical-core-artifact boundary)
          request (:authenticated-core-request boundary)
          expanded (:sh05-macro-artifact resolved)
          c3 (get-in expanded [:gravity-macro-boundary :authenticated-sh04-artifact])
          c2 (:c2-reader-artifact c3)
          nodes (into {} (map (juxt :node-id identity)) (:nodes core))
          forms (into {} (map (juxt :form-id identity)) (:forms request))
          bindings (into {} (map (juxt :binding-id identity)) (:binding-table request))
          mappings (into {} (map (juxt :core-node-id identity)) (:source-map core))
          function (one (:function-records core) #(= 'sh07-vector-contains? (:definition-name %)))
          loop-binding (one (:loop-bindings core) #(= 'remaining (:name %)))
          loop-target (one (:recur-targets core) #(= :loop (:target-kind %)))
          fn-target (one (:recur-targets core) #(= :function (:target-kind %)))
          transfer (one (:recur-transfers core) (constantly true))
          loop-node (nodes (:owner-core-node-id loop-target))
          recur-node (nodes (:recur-core-node-id transfer))
          initializer (nodes (:initializer-node-id loop-binding))
          call-name #(get-in nodes [(:operator-node-id %) :attributes :symbol])
          rest-call (one (:calls core) #(= 'rest (call-name %)))
          remaining-id (:binding-id loop-binding)]
      (is (= :accepted (:status resolved) (:status expanded) (:status artifact)))
      (is (= :gravity.checked-core (get-in artifact [:pass :owner])))
      (is (= :passed (get-in boundary [:template-verification :status])
             (get-in boundary [:resolved-verification :status])))
      (is (= [1 1 2 1] (mapv #(count (get core %))
                             [:function-records :loop-bindings :recur-targets :recur-transfers])))
      (is (= original (second (:parsed-semantic-values c2))))
      (is (= (list 'def 'sh07-vector-contains? (cons 'fn (drop 2 original)))
             (first (:expanded-forms expanded))))
      (is (= source-path (get-in resolved [:provenance :source-path])
             (get-in c2 [:source-unit-record :path])
             (get-in core [:provenance :actual-source-path])))
      (is (not= owner-path source-path))
      (is (= (str "sha256:" (bootstrap/sha256-hex text))
             (get-in c2 [:source-unit-record :bytes-hash])))
      (is (= (str "sha256:" (bootstrap/sha256-hex (slurp reader-path :encoding "UTF-8")))
             (get-in c2 [:sh03-reader-authentication :reader-source-revision :source-content-hash])))
      (is (= :private (get-in bindings [(:definition-binding-id function) :visibility])))
      (is (= '[values value] (mapv #(get-in bindings [% :name]) (:parameter-binding-ids function))))
      (is (= 2 (:fixed-arity function) (:arity fn-target)))
      (is (= (:function-core-node-id function) (:owner-core-node-id fn-target)))
      (is (= (:target-id fn-target) (:parent-target-id loop-target)))
      (is (= [:loop 1 [remaining-id]] ((juxt :target-kind :arity :binding-ids) loop-target)))
      (is (= (:loop-core-node-id loop-binding) (:owner-core-node-id loop-target)))
      (is (= 'values (get-in initializer [:attributes :symbol])))
      (is (= (first (:parameter-binding-ids function)) (get-in initializer [:attributes :binding-id])))
      (is (= (:body-core-node-id function) (:node-id loop-node)))
      (is (= 2 (count (:children loop-node))))
      (is (= (:node-id initializer) (first (:children loop-node))))
      (is (= :if (:core-form (nodes (last (:children loop-node))))))
      (is (= (:target-id loop-target) (:target-id transfer) (get-in recur-node [:attributes :target-id])))
      (is (= [:loop 1 true :nearest-lexical-recur-target :pending-sh08]
             ((juxt :target-kind :arity :tail-position :transfer-policy :type-compatibility) transfer)))
      (is (= [(:core-node-id rest-call)] (:argument-node-ids transfer) (:children recur-node)))
      (is (= 2 (count (filter #(= :if (:core-form %)) (:nodes core)))))
      (doseq [node (filter #(= :if (:core-form %)) (:nodes core))]
        (is (= 3 (count (:children node))))
        (is (= (vec (rest (:child-form-ids (forms (get-in node [:source :form-id])))))
               (mapv #(get-in nodes [% :source :form-id]) (:children node)))))
      (doseq [call (:calls core)]
        (let [ids (into [(:operator-node-id call)] (:argument-node-ids call))
              node (nodes (:core-node-id call))
              form (forms (get-in node [:source :form-id]))]
          (is (= [:core 'gravity.core (call-name call)]
                 ((juxt :binding-class :namespace :name) (bindings (:operator-binding-id call)))))
          (is (= ids (:children node) (:ordered-evaluation-node-ids call)
                 (mapv :core-node-id (get-in node [:evaluation :order]))))
          (is (= (:child-form-ids form) (mapv #(get-in nodes [% :source :form-id]) ids)))))
      (let [references (filterv #(= 'remaining (get-in % [:attributes :symbol])) (:nodes core))]
        (is (= 3 (count references) (count (set (map #(get-in % [:source :form-id]) references)))))
        (doseq [node references]
          (is (= remaining-id (get-in node [:attributes :binding-id])))))
      (doseq [node (:nodes core)]
        (let [source (:source node) form (forms (:form-id source)) mapping (mappings (:node-id node))
              fields [:syntax-id :form-id :origin-chain :generated-origin]]
          (is (and (map? form) (map? mapping)))
          (is (= (select-keys form fields) (select-keys source fields) (select-keys mapping fields)))
          (is (= (:source-span form) (:source-span mapping)))))
      (let [template (get-in boundary [:raw-template-result :core-template])
            target (one (:recur-targets template) #(= :loop (:target-kind %)))
            original-target (:target-id target)
            replacement (str "sha256:" (apply str (repeat 64 "f")))
            retarget #(if (= original-target (:target-id %)) (assoc % :target-id replacement) %)
            changed (-> template
                        (update :recur-targets #(mapv retarget %))
                        (update :recur-transfers #(mapv retarget %))
                        (update :nodes #(mapv (fn [node] (update node :attributes retarget)) %)))
            changed (update changed :identity-preimage merge
                            (select-keys changed [:nodes :recur-targets :recur-transfers]))
            _ (println {:loop-helper-phase :owning-hostile-template-replay-begin})
            rejected (bootstrap/sh07-core-execute!
                       source-path 'sh07-verify-core-template
                       [request changed (:digest-requests boundary)])]
        (is (not= original-target replacement))
        (is (= replacement (:target-id (one (:recur-targets changed) #(= :loop (:target-kind %))))))
        (is (= replacement (:target-id (first (:recur-transfers changed)))))
        (is (= (select-keys changed [:nodes :recur-targets :recur-transfers])
               (select-keys (:identity-preimage changed) [:nodes :recur-targets :recur-transfers])))
        (is (= [:rejected "C6-VERIFY" :template-replay] ((juxt :status :rule :reason) rejected)))
        (is (= request (:authenticated-core-request boundary)))
        (println {:loop-helper-phase :complete :hostile-rule (:rule rejected)}))
      (is (= {:C7 :pending :C8 :pending :C9 :pending :C10 :pending}
             (get-in artifact [:execution-boundary :downstream-fact-statuses])))
      (is (= text (slurp source-path :encoding "UTF-8"))))))
