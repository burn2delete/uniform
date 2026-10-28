(ns gravity.self-hosting.private-collection-chain-test
  (:require [clojure.test :refer [deftest is testing]]))

;; Compiler plans are constructed only by explicitly selected semantic tests.
(def owners
  {:c7 ["sh08" "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"]
   :effects ["body" "bootstrap/gravity/src/gravity/effects.gravity"]
   :emitter ["p15-s23" "bootstrap/gravity/p15_s23/emitter.gravity"]})

(defn- source [id]
  {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})

(defn chain-input [length]
  (let [ref-id #(keyword (str "r" %))
        binding-id #(keyword (str "b" %))
        owner-id #(keyword (str "l" %))
        definition-id #(keyword (str "d" %))
        scope-id #(keyword (str "s" %))
        records
        (mapv (fn [i]
                (let [next-id (if (= (inc i) length) :z (ref-id (inc i)))]
                  {:let-core-node-id (owner-id i) :ordinal 0 :name (symbol (str "x" i))
                   :binding-id (binding-id i) :definition-form-id (definition-id i)
                   :definition-syntax-id (definition-id i) :binding-scope-id (scope-id i)
                   :initializer-form-id next-id :initializer-syntax-id next-id
                   :initializer-scope-id (scope-id i) :initializer-node-id next-id
                   :visible-prior-binding-ids [] :mutability :immutable})) (range length))
        nodes
        (reduce (fn [out [i record]]
                  (let [id (ref-id i) bid (binding-id i)]
                    (assoc out
                      id {:node-id id :core-form :reference :children []
                          :resolved-binding-ids [bid]
                          :attributes {:binding-id bid :binding-class :lexical :symbol (:name record)}
                          :source (source id) :evaluation {:owner-function-syntax-id :f}}
                      (owner-id i) {:node-id (owner-id i) :core-form :let
                                    :children [(:initializer-node-id record) id]
                                    :resolved-binding-ids [bid]})))
                {:z {:node-id :z :core-form :call :source (source :z)
                     :evaluation {:owner-function-syntax-id :f}}}
                (map-indexed vector records))
        bindings (into {} (map (fn [record]
                                [(:binding-id record)
                                 {:binding-id (:binding-id record) :binding-class :lexical
                                  :mutability :immutable :definition-syntax-id (:definition-syntax-id record)
                                  :name (:name record) :scope-id (:binding-scope-id record)}]) records))]
    {:start (ref-id 0) :core {:lexical-bindings records :mutations []}
     :nodes nodes :bindings bindings
     :links (mapv (fn [i record]
                    {:reference-core-node-id (ref-id i) :reference-source (source (ref-id i))
                     :binding-record record}) (range length) records)}))

(def plans (atom {}))

(defn- invoke [owner function arguments]
  (let [[prefix relative] (get owners owner)
        plan (or (get @plans owner)
                 (let [path (str (System/getProperty "user.dir") "/" relative)
                       rule ((requiring-resolve 'gravity.bootstrap/c-backend-stage2-plan-emitter-source-rule!) path :jvm)
                       built ((requiring-resolve 'gravity.bootstrap/p15-s23-stage2-compiler-artifact-plan)
                              (:emitter rule) path (slurp path))]
                   (swap! plans assoc owner built)
                   built))]
    ((requiring-resolve 'gravity.bootstrap/p15-s23-stage2-runtime-execute-function)
     {:engine :private-collection-chain-development :compiler-artifact-plan? true}
     plan (symbol (str prefix "-" function)) arguments)))

(defn- unroll [owner {:keys [start core nodes bindings]}]
  ;; Both old and proposed helpers accept an explicit supplied allowance.
  ;; Keep it sufficient to distinguish the chain ceiling from the separate
  ;; unresolved work pool; no production default or caller is changed.
  (invoke owner "pc-unroll" [start core nodes bindings 131072]))

(defn original-observations [owner]
  ;; Run only as an explicitly selected old-source observation, never as a
  ;; registered candidate test. The wrapper must retain exact source identity.
  (mapv (fn [n]
          (let [input (chain-input n) result (unroll owner input)]
            {:length n :status (:status result) :reason (:reason result)
             :checked-links (count (:links result))
             :descriptor-domain (invoke owner "pc-links-domain?" [(:links input)])}))
        [127 128 129 130]))

(defn- check-owner [owner]
  (doseq [n [127 128 129 130]]
    (testing (str owner " chain " n)
      (let [input (chain-input n) result (unroll owner input)]
        (is (= :accepted (:status result)) (pr-str (select-keys result [:status :reason :work-count])))
        (is (= :z (:node-id result)))
        (is (= (:links input) (:links result)))
        (is (true? (invoke owner "pc-links-domain?" [(:links input)]))))))
  (let [input (chain-input 130)
        reconstructed (unroll owner input)
        expected {:presence-proof {:kind :private-first-entry-key-presence
                                   :get-call-core-node-id :get :first-call-core-node-id :first
                                   :receiver-links (:links reconstructed) :vector-links []
                                   :entries [] :selected-type :gravity.type/keyword}}
        descriptor-check #(invoke owner "pc-descriptor-work" [% expected 131072])
        last-index 129
        last-record (get-in input [:core :lexical-bindings last-index])
        bid (:binding-id last-record)
        changed
        {:late-mutable (assoc-in input [:core :lexical-bindings last-index :mutability] :mutable)
         :late-mutation (assoc-in input [:core :mutations] [{:target-binding-id bid}])
         :late-definition (assoc-in input [:bindings bid :definition-syntax-id] :different)
         :late-source (assoc-in input [:nodes :z :source :syntax-id] :different)
         :cycle (-> input
                    (assoc-in [:core :lexical-bindings last-index :initializer-node-id] (:start input))
                    (assoc-in [:core :lexical-bindings last-index :initializer-form-id] (:start input))
                    (assoc-in [:core :lexical-bindings last-index :initializer-syntax-id] (:start input))
                    (assoc-in [:nodes (:let-core-node-id last-record) :children 0] (:start input)))}]
    ;; This is the real descriptor equality consumer with expected links freshly
    ;; reconstructed from the current core. Complete typed products and source
    ;; forms have separate composition checks; this normalized proof is scoped
    ;; to the long-link domain and equality boundary.
    (is (= :accepted (:status reconstructed)))
    (is (= 130 (count (get-in expected [:presence-proof :receiver-links]))))
    (is (= :accepted (:status (descriptor-check expected))))
    (doseq [bad [(update-in expected [:presence-proof :receiver-links] pop)
                 (update-in expected [:presence-proof :receiver-links] #(vec (reverse %)))
                 (update-in expected [:presence-proof :receiver-links] #(conj % (last %)))
                 (assoc-in expected [:presence-proof :receiver-links last-index :reference-source :syntax-id] :forged)
                 (assoc-in expected [:presence-proof :receiver-links last-index :binding-record :mutability] :mutable)
                 (assoc-in expected [:presence-proof :unknown] true)]]
      (let [result (descriptor-check bad)]
        (is (= :rejected (:status result)))
        (is (= :map-get-source-mismatch (:reason result)))))
    (doseq [[label bad] changed]
      (testing (name label)
        (let [result (unroll owner bad)]
          (is (= :rejected (:status result)))
          (is (= :map-get-source-mismatch (:reason result))))))
    (doseq [bad [(assoc-in (:links input) [last-index :binding-record :mutability] :mutable)
                 (assoc-in (:links input) [last-index :binding-record :binding-id] nil)
                 (assoc-in (:links input) [last-index :reference-source :syntax-id] nil)
                 (assoc-in (:links input) [last-index :unknown] true)]]
      (is (false? (invoke owner "pc-links-domain?" [bad]))))
    (let [callbacks (atom 0)
          opaque (proxy [Object] []
                   (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque ID hashed")))
                   (equals [_other] (swap! callbacks inc) (throw (AssertionError. "opaque ID compared")))
                   (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque ID printed"))))
          reference-id (get-in input [:links last-index :reference-core-node-id])
          bad-input (assoc-in input [:nodes reference-id :attributes :binding-id] opaque)
          result (unroll owner bad-input)
          bad-links (assoc-in (:links input) [last-index :binding-record :binding-id] opaque)
          bad-descriptor (assoc-in expected [:presence-proof :receiver-links last-index :reference-core-node-id] opaque)]
      (is (= :rejected (:status result)))
      (is (= :map-get-source-mismatch (:reason result)))
      (is (false? (invoke owner "pc-links-domain?" [bad-links])))
      (is (= :rejected (:status (descriptor-check bad-descriptor))))
      (is (zero? @callbacks)))))

(deftest pc-chain-c7 (check-owner :c7))
(deftest pc-chain-effects (check-owner :effects))
(deftest pc-chain-emitter (check-owner :emitter))
