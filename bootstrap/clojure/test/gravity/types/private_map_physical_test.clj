(ns gravity.types.private-map-physical-test
  "Core-only PMK physical-source models; no compiler or native admission."
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [gravity.types.private-map-cycle-test :as source]))

(defn model [{:keys [prefix path]}]
  (let [space (create-ns (symbol (str "gravity.types.private-map-physical-model-" prefix)))
        defs (source/definitions path) named #(symbol (str prefix "-" %))
        reservations (atom [])]
    (binding [*ns* space]
      (refer 'clojure.core)
      ;; Only the real physical validator and first proof/header handoff run.
      ;; All later proof stages are explicit seams and remain unproven.
      (doseq [sym (distinct (filter #(and (symbol? %) (str/starts-with? (str %) (str prefix "-")))
                                    (tree-seq coll? seq [(get defs (named "pmk-proof"))
                                                         (get defs (named "pmk-header"))])))]
        (intern space sym (fn [& _] (throw (ex-info "Unreached proof seam" {:name sym})))))
      (doseq [suffix ["ft-all-true?" "all?" "typed-all"]]
        (intern space (named suffix) #(every? true? %)))
      (intern space (named "pe-reserve")
              (fn [& args] (swap! reservations conj (last args))
                {:status :rejected :reason :map-get-work-limit :work-count 7}))
      (intern space (named "pe-census") (fn [& _] nil))
      (intern space (named "pmk-source")
              (fn [& _] {:status :accepted :descriptor {:source-call {:argument-node-ids [:node]}}}))
      (doseq [suffix ["sse-ids?" "pmk-id-vector?" "pmk-record-ids?" "pmk-physical" "pmk-proof" "pmk-header"]]
        (eval (get defs (named suffix)))))
    {:space space :prefix prefix :reservations reservations}))
(def models (delay (mapv model source/owners)))
(defn invoke [m suffix & args]
  (apply (var-get (ns-resolve (:space m) (symbol (str (:prefix m) "-" suffix)))) args))

(def families
  [[:nodes [:node-id] [] [:children :resolved-binding-ids]]
   [:definitions [:binding-id :core-node-id :value-node-id] [:syntax-id] []]
   [:function-records [:function-syntax-id :function-core-node-id]
    [:body-core-node-id :definition-binding-id :definition-core-node-id :function-form-id] [:parameter-binding-ids]]
   [:reference-uses [:core-node-id :form-id :syntax-id :binding-id] [:definition-syntax-id] []]
   [[:preserved-resolution :resolution-table] [:reference-syntax-id :binding-id] [:upstream-binding-id] []]
   [:calls [:core-node-id :operator-node-id :operator-binding-id] [] [:argument-node-ids :ordered-evaluation-node-ids]]
   [:call-edges [:call-core-node-id :callee-binding-id]
    [:caller-function-syntax-id :caller-function-core-node-id :callee-definition-syntax-id :callee-function-syntax-id :callee-function-core-node-id]
    [:argument-core-node-ids :ordered-evaluation-node-ids]]
   [:var-references [:binding-id] [] []] [:mutations [:target-binding-id] [] []]
   [:recur-targets [:target-id :owner-core-node-id] [] [:binding-ids]]
   [:recur-transfers [:target-id] [] []]
   [:recursion-components [] [] [:function-syntax-ids]]])
(defn family-path [key] (if (vector? key) key [key]))
(defn fixture []
  (let [node {:node-id :node :core-form :literal :children [:child] :resolved-binding-ids [:binding]
              :source {:form-id :form :syntax-id :syntax}
              :attributes {:binding-id :binding :definition-syntax-id :definition :symbol 'x :binding-class :local}
              :evaluation {:owner-function-syntax-id :owner :order [{:core-node-id :child :index 0}]}}
        core (reduce (fn [c [key required optional vectors]]
                       (assoc-in c (family-path key)
                                 [(merge (zipmap (concat required optional) (repeat :id))
                                         (zipmap vectors (repeat [:id])))]))
                     {:module {:namespace 'example :exports ['x] :effects [:alloc] :capabilities [:memory]}}
                     families)]
    {:core (assoc core :nodes [node]) :nodes {:node node}
     :bindings {:binding {:binding-id :binding :definition-syntax-id :definition :upstream-binding-id :upstream
                          :scope-id :scope :name 'x :namespace 'example :binding-class :local
                          :kind :var :visibility :private :mutability :immutable}}
     :forms {:form {:form-id :form :syntax-id :syntax :parent-form-id :parent
                    :child-form-ids [:child] :kind :symbol :value 'x}}}))
(defn cell [m f] (apply invoke m "pmk-physical" (map f [:core :nodes :bindings :forms])))
(def accepted {:status :accepted})
(def rejected {:status :rejected :reason :private-map-keys-source-mismatch})

(deftest physical-widths-have-no-allowance-or-report
  (let [f (fixture) wide (vec (repeat 65537 :id))]
    (doseq [m @models]
      (is (= accepted (cell m f)))
      (doseq [[key _ _ vectors] families
              :let [path (into [:core] (family-path key)) row (first (get-in f path))]]
        (is (= accepted (cell m (assoc-in f path (vec (repeat 8193 row))))))
        (doseq [field vectors]
          (is (= accepted (cell m (assoc-in f (into path [0 field]) wide))))))
      (is (= accepted (cell m (assoc-in f [:core :nodes 0 :evaluation :order]
                                      (vec (repeat 65537 {:core-node-id :child :index 0}))))))
      (is (= accepted (cell m (assoc-in f [:forms :form :child-form-ids] wide)))))
    (is (nil? (find-ns 'gravity.bootstrap)))))

(deftest every-family-and-member-still-validates-without-opaque-dispatch
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque rendering" {}))))
        f (fixture)]
    (doseq [m @models [key required optional vectors] families
            :let [path (into [:core] (family-path key)) row (first (get-in f path))
                  rows (conj (vec (repeat 8192 row)) row) late (into path [8192])]]
      (doseq [bad [nil opaque [opaque]]]
        (is (= rejected (cell m (assoc-in f path bad))))
        (is (empty? @callbacks)))
      (doseq [field (concat required optional)]
        (is (= rejected (cell m (-> f (assoc-in path rows) (assoc-in (conj late field) opaque)))))
        (is (empty? @callbacks)))
      (doseq [field vectors bad [opaque (conj (vec (repeat 65536 :id)) opaque)]]
        (is (= rejected (cell m (assoc-in f (into path [0 field]) bad))))
        (is (empty? @callbacks))))
    (doseq [m @models
            bad (concat
                 (for [path [[:core :nodes 0 :source :syntax-id]
                             [:core :nodes 0 :attributes :symbol]
                             [:core :nodes 0 :evaluation :order 0 :index]
                             [:forms :form :value] [:bindings :binding :name]]]
                   (assoc-in f path opaque))
                 [(assoc f :forms (sorted-map-by (fn [_ _] 0) opaque (get-in f [:forms :form])))
                  (assoc f :bindings (sorted-map-by (fn [_ _] 0) opaque (get-in f [:bindings :binding])))])]
      (is (= rejected (cell m bad)))
      (is (empty? @callbacks)))))

(deftest metadata-forms-bindings-and-source-ids-remain-validated
  (let [f (fixture)
        corrupt (concat
                 (for [key [:nodes :bindings :forms]] (assoc f key []))
                 (for [path [[:core :module :namespace] [:core :module :exports]
                             [:core :module :effects] [:core :module :capabilities]
                             [:core :nodes 0 :source] [:core :nodes 0 :attributes]
                             [:core :nodes 0 :evaluation] [:core :nodes 0 :evaluation :order]
                             [:forms :form :child-form-ids]]]
                   (assoc-in f path :invalid))
                 (for [path [[:core :module :exports] [:core :module :effects] [:core :module :capabilities]]]
                   (update-in f path conj 42))
                 (for [path [[:core :nodes 0 :core-form] [:core :nodes 0 :source :form-id]
                             [:core :nodes 0 :source :syntax-id] [:core :nodes 0 :attributes :binding-id]
                             [:core :nodes 0 :attributes :definition-syntax-id] [:core :nodes 0 :attributes :symbol]
                             [:core :nodes 0 :attributes :binding-class] [:core :nodes 0 :evaluation :owner-function-syntax-id]
                             [:core :nodes 0 :evaluation :order 0 :core-node-id] [:core :nodes 0 :evaluation :order 0 :index]
                             [:forms :form :form-id] [:forms :form :syntax-id] [:forms :form :parent-form-id]
                             [:forms :form :kind] [:forms :form :value]
                             [:bindings :binding :binding-id] [:bindings :binding :definition-syntax-id]
                             [:bindings :binding :upstream-binding-id] [:bindings :binding :scope-id]
                             [:bindings :binding :name] [:bindings :binding :namespace]
                             [:bindings :binding :binding-class] [:bindings :binding :kind]
                             [:bindings :binding :visibility] [:bindings :binding :mutability]]]
                   (assoc-in f path true))
                 [(assoc f :forms {42 (get-in f [:forms :form])})
                  (assoc f :bindings {42 (get-in f [:bindings :binding])})
                  (assoc-in f [:forms :form :child-form-ids] [:child true])
                  (assoc-in f [:forms :form] {:form-id :form :syntax-id :syntax :child-form-ids [] :kind :keyword :value 'x})])]
    (doseq [m @models bad corrupt] (is (= rejected (cell m bad))))
    (doseq [m @models]
      (is (= accepted (cell m (assoc-in f [:forms :form :kind] :literal))))
      (is (= accepted (cell m (assoc-in f [:forms :form :value] 'other))))
      (is (= accepted (cell m (assoc-in f [:forms :form :syntax-id] (apply str (repeat 71 \a))))))
      (is (= rejected (cell m (assoc-in f [:forms :form :syntax-id] (apply str (repeat 70 \a))))))
      (is (= accepted (cell m (assoc-in f [:core :function-records 0 :parameter-binding-ids] nil)))))))

(deftest first-consumer-preserves-separate-reservation-and-result-charges
  (doseq [m @models :let [f (fixture) header {:source-call {:argument-node-ids [:node]}}
                         proof #(invoke m "pmk-proof" header (:core %) nil
                                        (:nodes %) (:bindings %) {} (:forms %) 20)]]
    (reset! (:reservations m) [])
    (is (= {:status :rejected :reason :private-map-keys-work-limit :work-count 7} (proof f)))
    (is (= [20] @(:reservations m)))
    (is (= rejected (proof (assoc-in f [:core :module :namespace] 42))))
    (is (= [20] @(:reservations m)) "Physical refusal never enters the reservation seam")
    (with-redefs-fn {(ns-resolve (:space m) (symbol (str (:prefix m) "-pe-reserve")))
                    (fn [& args]
                      (swap! (:reservations m) conj (last args))
                      {:status :accepted :work-count 7})}
      #(is (= {:status :rejected :reason :private-map-keys-entry-domain-required :work-count 7}
              (proof f))))
    (is (= [20 20] @(:reservations m)) "Later refusal reports only the separate reservation")
    (reset! (:reservations m) [20])
    (let [args (concat (map f [:core]) [nil nil nil (:nodes f) (:bindings f) {} (:forms f) 20])]
      (is (= {:status :rejected :reason :private-map-keys-work-limit :work-count 10}
             (apply invoke m "pmk-header" args)))
      (is (= [20 17] @(:reservations m)))
      (is (= (assoc rejected :work-count 3)
             (apply invoke m "pmk-header" (assoc (vec args) 0 (assoc-in (:core f) [:module :namespace] 42)))))
      (is (= [20 17] @(:reservations m))))))
