(ns gravity.types.private-map-metadata-test
  "Core-only metadata and entry-evidence models; native type/Map proof are seams."
  (:require [clojure.test :refer [deftest is]]
            [gravity.types.private-map-cycle-test :as source]))

(defn model [{:keys [prefix path]}]
  (let [space (create-ns (symbol (str "gravity.types.private-map-metadata-model-" prefix)))
        defs (source/definitions path) named #(symbol (str prefix "-" %))
        validations (atom 0) compatibility (atom [])]
    (binding [*ns* space]
      (refer 'clojure.core)
      (doseq [suffix ["all?" "typed-all"]] (intern space (named suffix) #(every? true? %)))
      (intern space (named "lc-validate-types")
              (fn [xs] (swap! validations inc) {:status (if (every? keyword? xs) :accepted :rejected)}))
      (intern space (named "mp-compatibility")
              (fn [& args] (swap! compatibility conj args)
                {:status :accepted :premise :model-premise}))
      (doseq [suffix (cond-> ["sse-ids?" "pmk-metadata-work" "pmk-unknown?"]
                      (not= prefix "sh08") (conj "pmk-entry-evidence"))]
        (eval (get defs (named suffix)))))
    {:space space :prefix prefix :validations validations :compatibility compatibility}))
(def models (delay (mapv model source/owners)))
(defn invoke [m suffix & args]
  (apply (var-get (ns-resolve (:space m) (symbol (str (:prefix m) "-" suffix)))) args))
(def accepted {:status :accepted})
(def rejected {:status :rejected :reason :private-map-keys-source-mismatch})
(defn nested [depth leaf] (loop [n depth out leaf] (if (zero? n) out (recur (dec n) [out]))))

(deftest unbounded-finite-first-order-data-validates-completely
  (doseq [m @models
          value [nil true false :k 's "text" 42 \a [] {} (vec (repeat 65537 :k))
                 (apply str (repeat 65537 \x)) (symbol (apply str (repeat 65537 \s)))
                 (keyword (apply str (repeat 65537 \k)))
                 (into {} (map #(vector % :value) (range 32769)))
                 (nested 129 :leaf) (nested 2048 :leaf)
                 {:nested [nil {:key [true 's \a]}]}]]
    (is (= accepted (invoke m "pmk-metadata-work" value))))
  (is (nil? (find-ns 'gravity.bootstrap))))

(deftest late-invalid-values-and-map-keys-refuse-without-callbacks
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks conj :hash) (throw (ex-info "opaque hash" {})))
                 (equals [_] (swap! callbacks conj :equals) (throw (ex-info "opaque equality" {})))
                 (toString [] (swap! callbacks conj :format) (throw (ex-info "opaque rendering" {}))))
        ;; LIFO traversal visits index zero last, and map keys after their values.
        wide (assoc (vec (repeat 65537 :k)) 0 opaque)
        key-table (sorted-map-by (fn [_ _] 0) opaque :value)]
    (doseq [m @models bad [opaque wide key-table {:key opaque} (nested 2048 opaque)
                           '(1 2) #{:k} 1/2 1.5 (atom :value)]]
      (is (= rejected (invoke m "pmk-metadata-work" bad)))
      (is (empty? @callbacks)))))

(defn fixture [extra normal?]
  (let [actual (if normal? :gravity.type/map {:kind :gravity.type/map})
        record {:function-syntax-id :owner :parameter-binding-ids [:p] :signature {}}
        fields {:source-payload extra :call-core-node-id :call}
        entry {:call-core-node-id :call :source-call {} :source-edge {} :entry (if normal? :normal :other)
               :argument-type actual :argument-types [actual] :compatibility :model-premise :call-input-facts fields}
        fact (assoc fields :artifact :gravity/c7-call-type-fact
                    :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id :call
                                      :callee-function-syntax-id :owner :argument-types [actual] :result-type :gravity.type/int}
                    :result-type :gravity.type/int :effects [] :capabilities [] :status :inferred)
        row {:constraint-id-request {:domain :gravity/sh08-function-call-constraint-v1
                                     :call-core-node-id :call :callee-function-syntax-id :owner}
             :lhs :owner :rhs [{:parameter :p :argument actual :chosen :gravity.type/map :compatibility :model-premise}]
             :kind :argument-compatibility :compatibility-rule-version :sh08-map-list-parameter-compatibility-v1
             :status :solved :diagnostics [] :profile :meta :target :jvm :origin []}]
    {:core {:module {:effects [] :capabilities [] :profile :meta :target :jvm}}
     :descriptor {:key-domain-proof {:entries [entry] :census {:function-record record} :parameter-index 0}}
     :row row :fact fact :nodes {:call {:source {:origin-chain []}}} :types {:call :gravity.type/int}}))
(defn evidence [m f]
  (invoke m "pmk-entry-evidence" (:core f) [(:row f)] [(:fact f)] (:descriptor f) (:nodes f) {} (:types f) 100))

(deftest actual-callers-preserve-reconstruction-and-separate-map-handoff
  (doseq [m (rest @models) :let [f (fixture (vec (repeat 65537 :payload)) true)]]
    (reset! (:validations m) 0)
    (reset! (:compatibility m) [])
    (is (= {:status :accepted :work-count 64} (evidence m f)))
    (is (= 1 @(:validations m)))
    (is (empty? @(:compatibility m)))
    (is (= {:status :accepted :work-count 64} (evidence m (fixture (nested 2048 :payload) true))))
    (doseq [path [[:fact :source-payload] [:fact :effects] [:fact :result-type]
                  [:row :origin] [:row :profile] [:row :rhs 0 :chosen]
                  [:row :constraint-id-request :callee-function-syntax-id] [:row :status]]]
      (is (= (assoc rejected :work-count 64) (evidence m (assoc-in f path :forged)))))
    (let [g (fixture :payload false)]
      (is (= {:status :accepted :work-count 64} (evidence m g)))
      (is (= 1 (count @(:compatibility m))))
      (is (= [(:core g) {} {}
              {:function-id :owner :signature {} :parameters [{:binding-id :p :type :gravity.type/map}]}
              0 {:kind :gravity.type/map} (:nodes g) {}]
             (vec (first @(:compatibility m)))) "Complete Map handoff uses the reviewed eight-argument ABI"))
    (let [opaque (Object.) before @(:validations m)]
      (is (= (assoc rejected :work-count 64) (evidence m (assoc-in f [:fact :source-payload] opaque))))
      (is (= before @(:validations m)) "Opaque metadata refuses before type or equality checks"))))
