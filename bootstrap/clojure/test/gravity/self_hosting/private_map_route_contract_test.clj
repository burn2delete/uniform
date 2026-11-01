(ns gravity.self-hosting.private-map-route-contract-test
  "Reader-only private-map routing and reconstruction contracts, not execution.

  The emitter deliberately has a dedicated private route before shared queries.
  Do not make its shared query wrapper uniform merely to obtain source parity."
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [clojure.walk :as walk])
  (:import [java.io PushbackReader]))

(def ^:private owners
  [["bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity" "sh08-pmk-"]
   ["bootstrap/gravity/src/gravity/effects.gravity" "body-pmk-"]
   ["bootstrap/gravity/p15_s23/emitter.gravity" "p15-s23-pmk-"]])

(defn- read-forms [path]
  (binding [*read-eval* false]
    (with-open [reader (PushbackReader. (io/reader path))]
      (loop [forms []]
        (let [form (read {:eof ::eof} reader)]
          (if (= ::eof form) forms (recur (conj forms form))))))))

(defn- definition [forms name]
  (let [found (filter #(and (seq? %) (= 'defn (first %)) (= name (second %))) forms)]
    (when-not (= 1 (count found))
      (throw (ex-info "Expected exactly one definition" {:name name})))
    (first found)))

(def ^:private sources (delay (mapv #(read-forms (first %)) owners)))

(def ^:private private-route-guard
  '(if (if (map? context) (map? node) false)
     (if (if (p15-s23-sse-ids? [(get node :node-id)] 1)
           (p15-s23-typed-all [(map? (get context :calls)) (map? (get context :core))
                              (map? (get context :types)) (map? (get context :bindings))
                              (map? (get context :nodes))]) false)
       (if (p15-s23-pmk-entry? node (get (get context :calls) (get node :node-id))
             (get context :nodes) (get context :bindings) (get context :types)
             (get (get context :core) :module)) true
         (if (vector? (get node :children))
           (if (p15-s23-sse-ids? [(first (get node :children))] 1)
             (let [kind (get (get (get context :types) (first (get node :children))) :kind)]
               (if (keyword? kind) (= :gravity.type/applied-private-map-keys kind) false)) false) false)) false) false))

(defn- dedicated-private-first? [form]
  ;; Exact outer shape and first branch: never search for a matching branch
  ;; somewhere later, which would silently accept reordered dispatch.
  (let [body (nnext form) expression (second body)]
    (and (= 2 (count body)) (= '[context node] (first body))
         (seq? expression) (= 4 (count expression)) (= 'if (first expression))
         (= private-route-guard (second expression))
         (= '(p15-s23-typed-compile-private-map-keys context node)
            (nth expression 2)))))

(defn- replace-form [value before after]
  (walk/postwalk #(if (= before %) after %) value))

(deftest dedicated-private-route-precedes-other-dispatch
  (let [form (definition (last @sources) 'p15-s23-typed-compile-call)
        expression (last form)]
    (is (dedicated-private-first? form))
    (testing "predicate, target, arguments and precedence drift fail closed"
      (doseq [[before after]
              [[private-route-guard true]
               ['p15-s23-pmk-entry? 'p15-s23-mkeys-entry?]
               [:gravity.type/applied-private-map-keys :gravity.type/applied-map-keys]
               ['map? 'identity] ['keyword? 'identity] ['vector? 'identity]
               ['p15-s23-sse-ids? 'identity]
               ['p15-s23-typed-compile-private-map-keys 'p15-s23-typed-compile-map-keys]
               ['(p15-s23-typed-compile-private-map-keys context node)
                '(p15-s23-typed-compile-private-map-keys context context)]]]
        (let [changed (replace-form form before after)]
          (is (not= form changed) (str "mutation present: " before))
          (is (not (dedicated-private-first? changed)) (str before))))
      (doseq [changed [(concat (take 3 form) ['(if other-route? other-result false)])
                       (concat (take 3 form) [(list 'if 'other-route? 'other-result expression)])
                       (concat form [true]) (take 3 form)
                       (concat (take 3 form) [(cons 'when (rest expression))])
                       (concat (take 3 form) [(butlast expression)])]]
        (is (not (dedicated-private-first? changed)))))))

(defn- same-contracts? [values]
  (and (= 3 (count values)) (apply = values)))

(deftest actual-private-constraint-and-fact-bodies-match-completely
  (doseq [part ["constraint" "call-fact"]]
    (let [bodies (mapv (fn [forms [_ prefix]]
                         (nnext (definition forms (symbol (str prefix part)))))
                       @sources owners)]
      ;; Strip only defn and owner-specific name. No symbol, key or value
      ;; normalization; complete params, maps and expressions must be equal.
      (is (same-contracts? bodies) part)
      (doseq [owner (range 3)
              key (set (filter keyword? (tree-seq coll? seq (get bodies owner))))]
        (let [changed (replace-form (get bodies owner) key ::changed)]
          (is (not (same-contracts? (assoc bodies owner changed))) (str part " " key))))
      (is (not (same-contracts? (assoc bodies 1 (concat (get bodies 1) [true])))))
      (is (not (same-contracts? (assoc bodies 2 (replace-form (get bodies 2) 'get 'identity)))))))
  (let [forms (last @sources)
        evidence (definition forms 'p15-s23-pmk-evidence)
        compile-private (definition forms 'p15-s23-typed-compile-private-map-keys)]
    (doseq [expected ['(= [(p15-s23-pmk-constraint descriptor)] (get rows :items))
                      '(= [(p15-s23-pmk-call-fact descriptor (get core :module))] (get calls :items))]]
      (is (= 1 (count (filter #{expected} (tree-seq coll? seq evidence))))))
    (is (= 1 (count (filter #{'(p15-s23-pmk-evidence core (get context :ledger) (get context :call-facts)
                                node call edge (get context :nodes) (get context :bindings)
                                (get context :types) (get context :forms))}
                           (tree-seq coll? seq compile-private)))))))
