(ns gravity.types.private-map-parameter-test
  "Core-only parameter source and first proof consumer; broader admission is seamed."
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.types.private-map-cycle-test :as source]))

(defn model [{:keys [prefix path]}]
  (let [space (create-ns (symbol (str "gravity.types.private-map-parameter-model-" prefix)))
        defs (source/definitions path) named #(symbol (str prefix "-" %))
        census (atom nil) graphs (atom []) reservations (atom [])]
    (binding [*ns* space]
      (refer 'clojure.core)
      (doseq [sym (distinct (filter #(and (symbol? %) (str/starts-with? (str %) (str prefix "-")))
                                    (tree-seq coll? seq (get defs (named "pmk-proof")))))]
        (intern space sym (fn [& _] (throw (ex-info "Unreached entry-proof seam" {:name sym})))))
      (doseq [suffix ["ft-all-true?" "all?" "typed-all"]] (intern space (named suffix) #(every? true? %)))
      (intern space (named "pmk-physical") (fn [& _] {:status :accepted}))
      (intern space (named "pe-reserve") (fn [& args] (swap! reservations conj (last args)) {:status :accepted :work-count 7}))
      (intern space (named "pe-census") (fn [& _] @census))
      (intern space (named "pmk-acyclic") (fn [_ owner] (swap! graphs conj owner) {:status :accepted}))
      (doseq [suffix ["sse-ids?" "pmk-id-vector?" "pmk-record-ids?" "pmk-fields?"
                      (case prefix "sh08" "ma-form-source" "body" "ma-form-node" "typed-source")
                      "pmk-parameter" "pmk-proof"]]
        (eval (get defs (named suffix)))))
    {:space space :prefix prefix :census census :graphs graphs :reservations reservations}))
(def models (delay (mapv model source/owners)))
(defn invoke [m suffix & args]
  (apply (var-get (ns-resolve (:space m) (symbol (str (:prefix m) "-" suffix)))) args))
(defn id [kind i] (keyword (str kind i)))
(defn fixture [n annotated?]
  (let [indices (range n) declared? #(or (zero? %) annotated?)
        params (mapv #(id "binding" %) indices)
        coordinates (mapv #(if annotated? (* 3 %) (if (zero? %) 0 (+ 2 %))) indices)
        declarations (mapv (fn [i]
                             (when (declared? i)
                               {:source-child-index (inc (get coordinates i))
                                :marker-form-id (id "marker" i) :marker-syntax-id (id "marker-syntax" i)
                                :type-form-id (id "type" i) :type-syntax-id (id "type-syntax" i)
                                :type-symbol (if (zero? i) 'Map 'Int) :type-binding-id (id "type-binding" i)
                                :type-id (if (zero? i) :gravity.type/map :gravity.type/int)})) indices)
        signature {:rule-version :sh07-atomic-function-signature-v1
                   :mapping-rule-version :sh07-atomic-function-declaration-map-v1
                   :parameters declarations :return nil :parameter-source-child-indexes coordinates :body-source-child-index 2}
        names (vec (mapcat #(cond-> [(id "name" %)] (declared? %) (conj (id "marker" %) (id "type" %))) indices))
        values (vec (mapcat #(cond-> [(symbol (str "p" %))] (declared? %) (conj :- (if (zero? %) 'Map 'Int))) indices))
        fn-form {:form-id :fn-form :syntax-id :owner :kind :list :child-form-ids [:operator :parameters :body-form]
                 :source-span {:start 0 :end 1} :origin-chain [] :generated-origin nil}
        forms (reduce (fn [out i]
                        (cond-> (assoc out (id "name" i) {:kind :symbol :value (symbol (str "p" i))
                                                          :syntax-id (id "name-syntax" i) :parent-form-id :parameters})
                          (declared? i) (assoc (id "marker" i) {:kind :keyword :value :- :syntax-id (id "marker-syntax" i) :parent-form-id :parameters}
                                              (id "type" i) {:kind :symbol :value (if (zero? i) 'Map 'Int)
                                                             :syntax-id (id "type-syntax" i) :parent-form-id :parameters})))
                      {:fn-form fn-form :operator {:kind :symbol :value 'fn :parent-form-id :fn-form}
                       :parameters {:form-id :parameters :syntax-id :parameter-syntax :kind :vector
                                    :parent-form-id :fn-form :child-form-ids names :value values}} indices)
        bindings (reduce (fn [out i]
                           (cond-> (assoc out (get params i) {:name (symbol (str "p" i)) :definition-syntax-id (id "name-syntax" i) :mutability :immutable})
                             (declared? i) (assoc (id "type-binding" i) {:name (if (zero? i) 'Map 'Int)
                                                                       :binding-class :core :kind :type :namespace 'gravity.core}))) {} indices)
        record {:parameter-binding-ids params :signature signature :function-core-node-id :fn-node
                :function-form-id :fn-form :function-syntax-id :owner :body-core-node-id :body :fixed-arity n}
        node {:attributes {:parameter-form-id :parameters :parameter-syntax-id :parameter-syntax :recur-target-id :owner
                           :parameters (mapv #(symbol (str "p" %)) indices) :signature signature}
              :lowering-rule-version :sh07-atomic-function-signature-v1
              :source {:form-id :fn-form :syntax-id :owner :semantic-span {:start 0 :end 1} :origin-chain [] :generated-origin nil}}]
    {:census {:function-record record :parameter-binding (get bindings (first params)) :parameter-index 0 :calls []}
     :core {} :node {} :nodes {:fn-node node :body {:source {:form-id :body-form}}} :bindings bindings :forms forms}))
(def wide (delay (fixture 6000 true)))
(defn cell [m f] (apply invoke m "pmk-parameter" (map f [:census :core :node :nodes :bindings :forms])))

(deftest wide-parameter-source-preserves-complete-output-without-accounting
  (doseq [m @models f [(fixture 3 false) (fixture 3 true) @wide]
          :let [r (cell m f) record (get-in f [:census :function-record]) n (:fixed-arity record)]]
    (is (= :accepted (:status r)))
    (is (not (contains? r :work-count)))
    (is (= {:function-id :owner :signature (:signature record)
            :parameters (mapv (fn [i] {:binding-id (id "binding" i)
                                      :type (or (get-in record [:signature :parameters i :type-id]) :gravity.type/unknown)}) (range n))}
           (:callee r)))
    (is (= {:function-node (get-in f [:nodes :fn-node]) :function-form (get-in f [:forms :fn-form])
            :parameter-form (get-in f [:forms :parameters]) :declaration (get-in record [:signature :parameters 0])
            :parameter-index 0 :parameter-binding (get-in f [:census :parameter-binding])} (:source r))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest signature-binding-source-and-late-declaration-tampering-still-refuse
  (let [f (fixture 3 true)
        paths [[:census :function-record :signature :rule-version]
               [:census :function-record :signature :mapping-rule-version]
               [:census :function-record :signature :return]
               [:census :function-record :signature :body-source-child-index]
               [:census :function-record :fixed-arity]
               [:census :function-record :signature :parameter-source-child-indexes 2]
               [:census :function-record :signature :parameters 2 :source-child-index]
               [:census :function-record :signature :parameters 2 :type-form-id]
               [:nodes :fn-node :attributes :recur-target-id]
               [:nodes :fn-node :attributes :parameter-syntax-id]
               [:nodes :fn-node :attributes :signature :return]
               [:nodes :fn-node :lowering-rule-version] [:nodes :fn-node :source :origin-chain]
               [:nodes :body :source :form-id] [:census :parameter-binding :mutability]
               [:forms :operator :value] [:forms :parameters :kind]
               [:forms :parameters :child-form-ids]
               [:forms :name2 :kind] [:forms :name2 :value] [:forms :name2 :syntax-id] [:forms :name2 :parent-form-id]
               [:forms :marker2 :value] [:forms :marker2 :syntax-id] [:forms :type2 :syntax-id]
               [:forms :type2 :parent-form-id] [:bindings :type-binding2 :binding-class]
               [:bindings :type-binding2 :kind] [:bindings :type-binding2 :namespace]
               [:census :function-record :signature :parameters 0 :type-id]]]
    (doseq [m @models path paths]
      (let [r (cell m (assoc-in f path :forged))]
        (is (= :rejected (:status r)))
        (is (contains? #{:private-map-keys-source-mismatch :private-map-keys-entry-domain-required} (:reason r)))
        (is (not (contains? r :work-count)))))
    (doseq [m @models bad [(assoc-in @wide [:forms :name5999 :syntax-id] :forged)
                           (assoc-in @wide [:forms :marker5999 :value] :wrong)
                           (assoc-in f [:forms :parameters :value] [:forged])
                           (update-in f [:census :function-record :signature] assoc :extra true)
                           (update-in f [:census :function-record :signature :parameters 2] assoc :extra true)
                           (assoc-in f [:census :function-record :signature :parameters 0] nil)]]
      (is (= :rejected (:status (cell m bad)))))))

(deftest opaque-declaration-ids-and-domain-keys-refuse-before-dispatch
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque rendering" {}))))
        f (fixture 3 true)]
    (doseq [m @models
            bad [(assoc-in f [:census :function-record :signature :parameters 2 :type-binding-id] opaque)
                 (assoc-in f [:census :function-record :signature :parameters 2 :type-symbol] opaque)
                 (assoc-in f [:census :function-record :signature :parameters 2 :marker-syntax-id] opaque)
                 (assoc-in f [:census :function-record :signature]
                           (into (sorted-map-by (fn [a b] (if (identical? a opaque) -1 (if (identical? b opaque) 1 (compare a b)))))
                                 (cons [opaque true] (dissoc (get-in f [:census :function-record :signature]) :return))))]]
      (is (= :rejected (:status (cell m bad))))
      (is (empty? @callbacks)))))

(deftest actual-first-consumer-retains-only-separate-reservation-report
  (doseq [m @models :let [f (fixture 3 true)
                         proof #(invoke m "pmk-proof" {} (:core %) (:node %) (:nodes %) (:bindings %) {} (:forms %) 20)]]
    (reset! (:census m) (:census f))
    (reset! (:reservations m) []) (reset! (:graphs m) [])
    (is (= {:status :rejected :reason :private-map-keys-entry-domain-required :work-count 7} (proof f)))
    (is (= [20] @(:reservations m)))
    (is (= [:owner] @(:graphs m)))
    (let [bad (assoc-in f [:forms :operator :value] 'other)]
      (reset! (:census m) (:census bad))
      (is (= {:status :rejected :reason :private-map-keys-source-mismatch :work-count 7} (proof bad)))
      (is (= [:owner] @(:graphs m)) "Parameter refusal precedes graph and entry proof"))
    (is (nil? (find-ns 'gravity.bootstrap)))))
