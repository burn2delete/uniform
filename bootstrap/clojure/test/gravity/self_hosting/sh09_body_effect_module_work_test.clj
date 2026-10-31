(ns gravity.self-hosting.sh09-body-effect-module-work-test
  "Semantic producer-module traversal and child-result propagation."
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]))

(defn- effect [function arguments]
  (apply (var-get (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf))
         function arguments))

(defn- producer-ledger-fixtures []
  (mapv (fn [index]
          (let [key (mapv #(str "sha256:" (format "%064x" (+ 5000 (* 2 index) %))) [0 1])]
            {:producer-sh06-artifact-id (first key) :producer-sh07-artifact-id (second key)
             :module-key key :request {:producer-index index} :facts {:producer-index index}}))
        (range 3)))

(defn- scripted-producer-analysis [entries failure]
  (let [seam (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original (var-get seam) calls (atom [])]
    (with-redefs-fn
      {seam (fn [runtime plan callee args scope]
              (if (= 'body-analyze-module callee)
                (let [index (:producer-index (first args)) entry (nth entries index)]
                  (swap! calls conj index)
                  (if (= failure index)
                    {:status :rejected :reason :selected-producer-failure
                     :producer-module (:module-key entry) :producer-facts (:facts entry)}
                    {:status :accepted :graph {:producer-index index}}))
                (original runtime plan callee args scope)))}
      (fn [] {:result (effect 'body-analyze-producers [entries])
              :calls @calls}))))

(deftest producer-analysis-propagates-child-results
  (let [entries (producer-ledger-fixtures)
        complete (scripted-producer-analysis entries nil)]
    (is (= :accepted (get-in complete [:result :status])))
    (is (= [0 1 2] (:calls complete)))
    (is (= 3 (count (get-in complete [:result :bank]))))
    (doseq [failure (range 3)]
      (let [{:keys [result calls]} (scripted-producer-analysis entries failure)]
        (is (= :rejected (:status result)))
        (is (= :selected-producer-failure (:reason result)))
        (is (= (vec (range (inc failure))) calls))
        (is (= (:module-key (get entries failure)) (:producer-module result)))
        (is (identical? (:facts (get entries failure)) (:producer-facts result)))))))

(deftest producer-key-is-not-observed-before-child-rejection
  (let [entries (producer-ledger-fixtures) callbacks (atom 0) realizations (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "producer key hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "producer key equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "producer key printing"))))
        delayed (lazy-seq (swap! realizations inc) (list :must-not-realize))]
    (doseq [key [opaque delayed [opaque :id] [:id opaque] [:id delayed]]]
      (let [{:keys [result calls]}
            (scripted-producer-analysis [(assoc (first entries) :module-key key)] 0)]
        (is (= :selected-producer-failure (:reason result)))
        (is (= [0] calls))
        (is (identical? (:facts (first entries)) (:producer-facts result)))))
    (is (zero? @callbacks))
    (is (zero? @realizations))))

(defn- normalized-atomic-producer-entry [ordinal]
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        invoke (var-get (ns-resolve upstream 'invoke-c7))
        base ((var-get (ns-resolve upstream 'atomic-equality-input))
              [:gravity.type/string :gravity.type/string])
        sha (var-get (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'sha))
        ids (zipmap (keys (:nodes base)) (map sha (range (+ 7000 (* 16 ordinal)) (+ 7010 (* 16 ordinal)))))
        nodes (mapv (fn [node]
                      (-> node (update :node-id ids) (update :children #(mapv ids %))
                          (update-in [:evaluation :order] #(mapv (fn [entry] (update entry :core-node-id ids)) %))))
                    (get-in base [:core :nodes]))
        call (-> (:call base) (update :core-node-id ids) (update :operator-node-id ids)
                 (update :argument-node-ids #(mapv ids %)) (update :ordered-evaluation-node-ids #(mapv ids %)))
        edge (-> (:edge base) (update :call-core-node-id ids) (update :argument-core-node-ids #(mapv ids %))
                 (update :ordered-evaluation-node-ids #(mapv ids %)))
        core (merge (:core base)
                    {:nodes nodes :calls [call] :call-edges [edge] :root-core-node-ids [(ids :call)]
                     :definitions [] :lexical-bindings [] :match-branch-records [] :match-pattern-records []
                     :match-decision-skeletons [] :evaluation-order [] :recursion-components []
                     :error-transfers [] :error-handlers []})
        types (into {} (map (fn [[id type]] [(ids id) type]) (:types base)))
        derived (invoke 'sh08-ae-derive [core (last nodes) call edge (into {} (map (juxt :node-id identity) nodes))
                                      (:bindings base) types (:forms base)])
        descriptor (:descriptor derived)
        facts (merge {:module (:module core)
                      :type-table (assoc types (:operator-node-id call) descriptor (:core-node-id call) (:type derived))
                      :function-type-table [] :local-binding-facts []
                      :constraint-ledger [(invoke 'sh08-ae-constraint [descriptor])]
                      :call-type-facts [(invoke 'sh08-ae-call-fact [descriptor (:module core)])]}
                     (select-keys core [:function-records :call-edges :recursion-components :lexical-bindings]))
        preimage (assoc (select-keys core [:root-core-node-ids :definitions :function-records :call-edges :calls
                                          :evaluation-order :lexical-bindings :match-branch-records
                                          :match-decision-skeletons :match-pattern-records])
                        :nodes (effect 'body-throw-semantic-nodes [core]))
        key [(sha (+ 8000 (* 2 ordinal))) (sha (+ 8001 (* 2 ordinal)))]]
    (is (= :accepted (:status derived)))
    {:producer-sh06-artifact-id (first key) :producer-sh07-artifact-id (second key) :module-key key
     :request {:canonical-core-artifact (assoc core :identity-preimage preimage)
               :b47-context {:authenticated-core-request {:forms (vec (vals (:forms base)))
                                                         :binding-table (vec (vals (:bindings base)))}}}
     :facts facts}))

(deftest producer-worker-preserves-real-normalized-module-graphs-and-default-wrapper
  ;; Actual C7 derivation and Effects module analysis over normalized DATA;
  ;; these entries do not claim producer-envelope or source authentication.
  (let [entries (mapv normalized-atomic-producer-entry [0 1])
        accepted (effect 'body-analyze-producers [entries])
        repeated (effect 'body-analyze-producers [entries])]
    (is (= :accepted (:status accepted)))
    (is (= 2 (count (:bank accepted))))
    (is (= accepted repeated))
    (doseq [entry entries]
      (let [analyzed (effect 'body-analyze-module
                             [(:request entry) (:facts entry) {}])]
        (is (= :accepted (:status analyzed)))
        (is (= (:graph analyzed) (get-in accepted [:bank (:module-key entry) :graph])))
        (is (= (assoc entry :graph (:graph analyzed))
               (get-in accepted [:bank (:module-key entry)])))))))
