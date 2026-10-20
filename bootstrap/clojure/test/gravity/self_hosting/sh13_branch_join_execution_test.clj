(ns gravity.self-hosting.sh13-branch-join-execution-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

;; Original Gravity producers/consumers execute through their existing seed
;; plans. The retained checked-Core records are supplied DATA, not fresh C10.
(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn- invoke [name & arguments]
  ((existing 'invoke) (existing 'c11-plan) name (vec arguments)))

(defn- checked [extension]
  ((existing 'checked-core) extension))

(defn- build [core]
  (invoke 'sh12-build-branch-join-mir core))

(defn- run [core mir]
  (invoke 'sh13-run-branch-join-mir core mir))

(defn- verify [core mir result]
  (invoke 'sh13-verify-branch-join-execution core mir result))

(defn- scalar-core [core condition type then-value else-value]
  (let [values [condition then-value else-value nil nil]
        types [:gravity.type/bool type type type type]]
    (reduce (fn [result index]
              (let [id (get-in core [:core-nodes index :node-id])]
                (-> result
                    (assoc-in [:core-nodes index :type] (types index))
                    (assoc-in [:core-nodes index :attributes :value]
                              (values index))
                    (assoc-in [:type-facts id :type] (types index))
                    (assoc-in [:type-facts id :type-id] (types index)))))
            core (range 5))))

(defn- coordinates [core mir]
  (let [function (get-in mir [:functions (:entrypoint core)])
        entry (:entry function)
        [then-id else-id] (get-in function [:blocks entry :successors])]
    {:function (:fn-id function) :entry entry :then then-id :else else-id
     :join (get-in mir [:control-flow-graph :join :block-id])}))

(deftest sh13-original-branch-join-interface-is-explicit
  (let [core (checked ".gravity") mir (build core) result (run core mir)]
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= :meta (:profile mir)))
    (is (= :jvm (:source-target mir)))
    (is (= :gravity/sh13-branch-join-execution (:artifact result)))
    (is (= 1 (:schema-version result)))
    (is (= #{:artifact :schema-version :status :input-bindings
             :upstream-verification :projection :execution :diagnostics
             :clojure-seed-boundary? :self-hosted? :nonclaims}
           (set (keys result))))
    (is (= {:checked-core-artifact-id (:artifact-id core)
            :mir-module-id (:module-id mir)} (:input-bindings result)))
    (is (= :passed (get-in result [:upstream-verification :status])))
    (is (= :accepted (:status result)))
    (is (true? (:clojure-seed-boundary? result)))
    (is (false? (:self-hosted? result)))
    (is (some #{:fresh-c10-ingress} (:nonclaims result)))
    (is (= :passed (:status (verify core mir result))))))

(deftest sh13-executes-original-verified-diamonds
  (doseq [extension [".gravity" ".qst"]
          [condition type left right expected]
          [[true :gravity.type/i64 11 22 11]
           [false :gravity.type/i64 11 22 22]
           [true :gravity.type/i64 -9223372036854775808
            9223372036854775807 -9223372036854775808]
           [false :gravity.type/i64 -9223372036854775808
            9223372036854775807 9223372036854775807]
           [true :gravity.type/bool false true false]
           [false :gravity.type/bool false true true]]]
    (testing [extension condition type]
      (let [core (scalar-core (checked extension) condition type left right)
            mir (build core) result (run core mir)
            {:keys [function entry then else join]} (coordinates core mir)
            rows (get-in result [:projection :operations])
            ids (mapv :node-id (:core-nodes core))
            selected (if condition then else)
            selected-id (ids (if condition 1 2))
            trace (get-in result [:execution :trace])]
        (is (= :accepted (:status result)))
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :returned (get-in result [:execution :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= 507 (get-in result [:execution :fuel])))
        (is (= [{:function function :event :enter :depth 0}
                {:function function :operation (ids 0) :opcode :const}
                {:function function :block entry :terminator :conditional-branch}
                {:function function :operation selected-id :opcode :const}
                {:function function :block selected :terminator :branch}
                {:function function :block join :terminator :return}] trace))
        (is (= ids (mapv :operation-id rows)))
        (is (= [:literal :literal :literal :edge-join :function-result-alias]
               (mapv :role rows)))
        (is (= (ids 3) (:alias-of (rows 4))))
        (is (= (ids 3) (get-in result [:projection :return-value-id])))
        (is (= (:control-flow-graph mir)
               (get-in result [:projection :control-flow-graph])))
        (doseq [index (range 5)]
          (let [node ((:core-nodes core) index)
                op (first (filter #(= (ids index) (:op-id %))
                                  (mapcat :instructions
                                          (vals (get-in mir [:functions
                                                            (:entrypoint core)
                                                            :blocks])))))]
            (is (= (:source node) (:source (rows index))))
            (is (= (:facts op) (:facts (rows index))))))
        (is (= :passed (:status (verify core mir result))))))))

(deftest sh13-rejects-original-mir-tampering-before-execution
  (let [core (checked ".gravity") mir (build core)
        {:keys [entry then join]} (coordinates core mir)
        fp [:functions (:entrypoint core)]
        ep (into fp [:blocks entry :instructions 0])
        mutants [(assoc-in mir [:control-flow-graph :join :incoming 0 :value]
                           "forged-value")
                 (assoc-in mir [:control-flow-graph :edges 0 :to] join)
                 (assoc-in mir (conj ep :constant-payload :value) false)
                 (assoc-in mir (conj ep :type) :gravity.type/i64)
                 (assoc-in mir (conj ep :effects) #{:io/write})
                 (assoc-in mir (into ep [:facts :safety-outcome-id]) "forged")
                 (assoc-in mir (into ep [:facts :ownership-fact-id]) "forged")
                 (assoc-in mir (into ep [:facts :capability-fact-id]) "forged")
                 (assoc-in mir (into ep [:source :span :start]) 99)
                 (assoc-in mir [:data-flow-graph :definitions "forged"] {})
                 (assoc-in mir (into fp [:blocks then :terminator :operands]) [])]]
    (doseq [mutant mutants]
      (let [original (invoke 'sh12-verify-branch-join-mir core mutant)
            result (run core mutant)]
        (is (= :rejected (:status original)))
        (is (= :rejected (:status result)))
        (is (= original (:upstream-verification result)))
        (is (= (:diagnostics original) (:diagnostics result)))
        (is (nil? (:projection result)))
        (is (nil? (:execution result)))
        (is (= :passed (:status (verify core mutant result))))))))

(deftest sh13-execution-subset-and-result-substitution-fail-closed
  ;; The NEW literal gate must reject arbitrary values without hashing or
  ;; calling equality on them. ArrayMap values/vectors do not hash this object
  ;; while constructing these direct-helper, supplied-DATA controls.
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality invoked" {})))
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash invoked" {}))))]
    (doseq [value [opaque [opaque] {:value opaque}]]
      (is (false? (invoke 'sh13-branch-join-literal-valid?
                          {:opcode :constant :source-operation :literal
                           :operands [] :type :gravity.type/bool
                           :constant-payload {:present? true :value value}}
                          :gravity.type/bool)))
      (is (empty? @callbacks))))
  (let [base (checked ".gravity")
        invalids [(scalar-core base true :gravity.type/i64 11 "unselected")
                  (scalar-core base false :gravity.type/i64 [] 22)
                  (scalar-core base true :gravity.type/i64 11 9223372036854775808N)
                  (scalar-core base false :gravity.type/bool nil true)
                  (assoc-in base [:core-nodes 1 :source-operation] :quote)]]
    (doseq [core invalids]
      (let [mir (build core) result (run core mir)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :rejected (:status result)))
        (is (= :bounded-execution-subset
               (get-in result [:diagnostics 0 :missing-fact])))
        (is (nil? (:execution result)))
        (is (nil? (:projection result)))
        (is (= :passed (:status (verify core mir result))))))
    (let [core ((existing 'invoke)
                (get (existing 'rejected-plans) ".gravity")
                'sh12-runtime-checked-core [base])
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (= :bounded-execution-subset
             (get-in result [:diagnostics 0 :missing-fact])))
      (is (nil? (:execution result)))
      (is (nil? (:projection result))))
    (let [mir (build base) result (run base mir)
          other (scalar-core base true :gravity.type/i64 11 23)
          other-mir (build other)]
      (doseq [candidate [(assoc result :status :rejected)
                         (assoc-in result [:execution :value] 22)
                         (assoc-in result [:projection :operations 2
                                          :constant-payload :value] 23)
                         (assoc-in result [:projection :operations 0 :source] {})]]
        (let [report (verify base mir candidate)]
          (is (= :rejected (:status report)))
          (is (= "C11-VERIFY" (get-in report [:diagnostics 0 :diagnostic-id])))
          (is (= :execution-result-substitution
                 (get-in report [:diagnostics 0 :missing-fact])))))
      ;; Same declared IDs and selected result do not authenticate new input.
      (is (= (:artifact-id base) (:artifact-id other)))
      (is (= :rejected (:status (verify other other-mir result)))))))

(defn- with-integer-comparison [extension base placement operation left right]
  (let [original (:core-nodes base)
        if-node (first (filter #(= :if (:source-operation %)) original))
        target-id ((:operands if-node) ({:condition 0 :then 1 :else 2} placement))
        target-index (.indexOf (mapv :node-id original) target-id)
        target (original target-index)
        start (get-in target [:source :span :start])
        make (fn [name args]
               ((existing 'invoke) (get (existing 'accepted-plans) extension)
                name args))
        ids [(str "sh13:" (name placement) ":left")
             (str "sh13:" (name placement) ":right")]
        nodes (mapv (fn [id value index]
                      (make 'sh12-branch-join-node
                            [id (conj (:path target) index) :literal []
                             :gravity.type/i64 value (+ start index)
                             (+ start index 1)]))
                    ids [left right] [0 1])
        comparison (make 'sh12-branch-join-node
                         [target-id (:path target) operation ids
                          :gravity.type/bool nil start
                          (get-in target [:source :span :end])])]
    (reduce (fn [core node]
              (let [id (:node-id node)]
                (-> core
                    (assoc-in [:type-facts id]
                              (make 'sh12-branch-join-type-fact
                                    [id :gravity.type/i64]))
                    (assoc-in [:effect-facts id]
                              (make 'sh12-branch-join-effect-fact [id]))
                    (assoc-in [:ownership-facts id]
                              (make 'sh12-branch-join-ownership-fact [id]))
                    (assoc-in [:capability-facts id]
                              (make 'sh12-branch-join-capability-fact [id]))
                    (assoc-in [:safety-facts id] (:safety node))
                    (assoc-in [:profile-facts id]
                              {:profile :meta :target :jvm}))))
            (-> base
                (assoc :core-nodes
                       (into (into (subvec original 0 target-index) nodes)
                             (cons comparison (subvec original (inc target-index)))))
                (assoc-in [:safety-facts target-id] (:safety comparison)))
            nodes)))

(defn- integer-condition-core [extension operation left right type]
  (with-integer-comparison
    extension
    (scalar-core (checked extension) true type
                 (if (= type :gravity.type/bool) false 11)
                 (if (= type :gravity.type/bool) true 22))
    :condition operation left right))

(defn- check-integer-condition [operation role cases]
  (doseq [extension [".gravity" ".qst"]]
    (doseq [[left right type condition expected] cases]
      (let [core (integer-condition-core extension operation left right type)
            mir (build core) result (run core mir)
            ids (mapv :node-id (:core-nodes core))
            rows (get-in result [:projection :operations])]
        (is (= :accepted (:status result)))
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= 505 (get-in result [:execution :fuel])))
        (is (= (conj (subvec ids 0 3) (ids (if condition 3 4)))
               (mapv :operation (filter :operation (get-in result [:execution :trace])))))
        (is (= ids (mapv :operation-id rows)))
        (is (= (mapv :source (:core-nodes core)) (mapv :source rows)))
        (is (= [:literal :literal role :literal :literal
                :edge-join :function-result-alias] (mapv :role rows)))
        (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
               (mapv #(get-in % [:facts :type-fact-id]) rows)))
        (is (= :passed (:status (verify core mir result))))))
    (let [core (integer-condition-core extension operation 0 1 :gravity.type/i64)
          id (get-in core [:core-nodes 0 :node-id])
          right-id (get-in core [:core-nodes 1 :node-id])]
      (doseq [bad [(assoc-in core [:core-nodes 0 :attributes :value] nil)
                  (assoc-in core [:core-nodes 1 :attributes :value] 9223372036854775808N)
                  (assoc-in core [:core-nodes 2 :operands] [id])
                  (assoc-in core [:core-nodes 2 :operands] [right-id id])
                  (-> core (assoc-in [:core-nodes 0 :type] :gravity.type/bool)
                      (assoc-in [:core-nodes 0 :attributes :value] true)
                      (assoc-in [:type-facts id :type] :gravity.type/bool)
                      (assoc-in [:type-facts id :type-id] :gravity.type/bool))
                  (integer-condition-core extension :integer-lte 0 1 :gravity.type/i64)
                  (integer-condition-core extension :integer-gt 0 1 :gravity.type/i64)
                  (integer-condition-core extension :integer-gte 0 1 :gravity.type/i64)]]
        (let [mir (build bad) result (run bad mir)]
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= :rejected (:status result)))
          (is (= :bounded-execution-subset
                 (get-in result [:diagnostics 0 :missing-fact])))
          (is (nil? (:execution result)))))
      (let [mir (build core) result (run core mir)
            entry (get-in mir [:control-flow-graph :entry])
            path [:functions (:entrypoint core) :blocks entry :instructions 2]]
        (doseq [bad [(assoc-in mir (conj path :operands) [id id])
                    (assoc-in mir (conj path :source-operation) :literal)
                    (assoc-in mir (conj path :constant-payload) {:present? true :value false})
                    (assoc-in mir (conj path :type) :gravity.type/i64)]]
          (let [upstream (invoke 'sh12-verify-branch-join-mir core bad)
                rejection (run core bad)]
            (is (= :rejected (:status rejection)))
            (is (= (:diagnostics upstream) (:diagnostics rejection)))
            (is (nil? (:execution rejection)))))
        (doseq [candidate [(assoc-in result [:execution :value] :forged)
                           (assoc-in result [:projection :operations 2 :role] :forged)]]
          (is (= :rejected (:status (verify core mir candidate)))))))))

(deftest sh13-executes-typed-integer-equality-conditions
  (check-integer-condition
   :integer-eq :integer-equality
   [[0 0 :gravity.type/i64 true 11] [0 1 :gravity.type/i64 false 22]
    [-9223372036854775808 -9223372036854775808 :gravity.type/i64 true 11]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 false 22]
    [0 0 :gravity.type/bool true false] [0 1 :gravity.type/bool false true]]))

(deftest sh13-executes-typed-integer-less-than-conditions
  (check-integer-condition
   :integer-lt :integer-less-than
   [[0 1 :gravity.type/i64 true 11] [1 0 :gravity.type/i64 false 22]
    [0 0 :gravity.type/i64 false 22] [-1 0 :gravity.type/i64 true 11]
    [0 -1 :gravity.type/i64 false 22]
    [-9223372036854775808 9223372036854775807 :gravity.type/i64 true 11]
    [9223372036854775807 -9223372036854775808 :gravity.type/i64 false 22]
    [-9223372036854775808 -9223372036854775808 :gravity.type/i64 false 22]
    [0 1 :gravity.type/bool true false] [1 0 :gravity.type/bool false true]]))

(deftest sh13-executes-integer-less-than-only-in-the-selected-arm
  (doseq [extension [".gravity" ".qst"] placement [:then :else]]
    (letfn [(base [condition]
              (scalar-core (checked extension) condition
                           :gravity.type/bool false true))
            (arm [condition left right operation]
              (with-integer-comparison
                extension (base condition) placement operation left right))]
      (doseq [condition [true false]
              [left right less?]
              [[0 1 true] [1 0 false] [1 1 false]
               [-9223372036854775808 9223372036854775807 true]
               [9223372036854775807 -9223372036854775808 false]]]
        (let [core (arm condition left right :integer-lt)
              mir (build core) result (run core mir)
              nodes (:core-nodes core) ids (mapv :node-id nodes)
              if-node (first (filter #(= :if (:source-operation %)) nodes))
              computed-id ((:operands if-node) (if (= placement :then) 1 2))
              comparison (first (filter #(= computed-id (:node-id %)) nodes))
              selected? (= placement (if condition :then :else))
              other-id ((:operands if-node) (if condition 1 2))
              operation-ids (if selected?
                              (into [(ids 0)] (conj (:operands comparison) computed-id))
                              [(ids 0) other-id])
              rows (get-in result [:projection :operations])
              coordinates (coordinates core mir)
              incoming (first (filter #(= (coordinates placement) (:predecessor %))
                                      (get-in mir [:control-flow-graph :join :incoming])))]
          (is (= :accepted (:status result)))
          (is (= :passed (get-in result [:upstream-verification :status])))
          (is (= (if selected? less? (not condition))
                 (get-in result [:execution :value])))
          (is (= (if selected? 505 507) (get-in result [:execution :fuel])))
          (is (= operation-ids
                 (mapv :operation (filter :operation (get-in result [:execution :trace])))))
          (is (= ids (mapv :operation-id rows)))
          (is (= 7 (count rows)))
          (is (= (mapv :source nodes) (mapv :source rows)))
          (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
                 (mapv #(get-in % [:facts :type-fact-id]) rows)))
          (is (= :integer-less-than (:role (rows (.indexOf ids computed-id)))))
          (is (= computed-id (:value incoming)))
          (is (= (:control-flow-graph mir)
                 (get-in result [:projection :control-flow-graph])))
          (is (= :passed (:status (verify core mir result))))))
      ;; All these invalid computations are in the UNSELECTED arm. The full
      ;; typed subset is checked before entering the interpreter at all.
      (let [condition (= placement :else)
            core (arm condition 0 1 :integer-lt)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            comparison (first (filter #(= :integer-lt (:source-operation %)) nodes))
            id (first (:operands comparison))
            left-index (.indexOf ids id)
            comparison-index (.indexOf ids (:node-id comparison))]
        (doseq [bad [(assoc-in core [:core-nodes left-index :attributes :value] nil)
                    (assoc-in core [:core-nodes left-index :attributes :value] 9223372036854775808N)
                    (assoc-in core [:core-nodes comparison-index :operands] [id])
                    (-> core (assoc-in [:core-nodes left-index :type] :gravity.type/bool)
                        (assoc-in [:core-nodes left-index :attributes :value] true)
                        (assoc-in [:type-facts id :type] :gravity.type/bool)
                        (assoc-in [:type-facts id :type-id] :gravity.type/bool))
                    (with-integer-comparison extension core :condition :integer-lt 0 1)
                    (with-integer-comparison extension core
                      (if (= placement :then) :else :then) :integer-lt 0 1)
                    (arm condition 0 1 :integer-eq)]]
          (let [mir (build bad) rejection (run bad mir)]
            (is (= :passed (get-in rejection [:upstream-verification :status])))
            (is (= :rejected (:status rejection)))
            (is (= :bounded-execution-subset
                   (get-in rejection [:diagnostics 0 :missing-fact])))
            (is (nil? (:execution rejection)))))
        (let [mir (build core) result (run core mir)
              block-id ((coordinates core mir) placement)
              path [:functions (:entrypoint core) :blocks block-id :instructions 2]]
          (doseq [bad [(assoc-in mir (conj path :operands) (vec (reverse (:operands comparison))))
                      (assoc-in mir (into path [:source :span :start]) 999)
                      (assoc-in mir [:control-flow-graph :join :incoming
                                     (if (= placement :then) 0 1) :value] id)]]
            (let [upstream (invoke 'sh12-verify-branch-join-mir core bad)
                  rejection (run core bad)]
              (is (= :rejected (:status rejection)))
              (is (= (:diagnostics upstream) (:diagnostics rejection)))
              (is (nil? (:execution rejection)))))
          (doseq [candidate [(assoc-in result [:execution :value] :forged)
                             (assoc-in result [:projection :operations 0 :role] :forged)]]
            (is (= :rejected (:status (verify core mir candidate))))))))))

(deftest sh13-preserves-literal-keyword-return-values
  ;; Actual c11-opcode outputs are supplied literal DATA here, not a lowering
  ;; or execution claim for the whole c11-opcode helper or a fresh C10 input.
  (let [constant (invoke 'c11-opcode {:source-operation :literal})
        less-than (invoke 'c11-opcode {:source-operation :integer-lt})]
    (is (= :constant constant))
    (is (= :integer-lt less-than))
    (doseq [extension [".gravity" ".qst"]
            [operation left right condition then-value else-value]
            [[nil nil nil true constant less-than]
             [nil nil nil false constant less-than]
             [nil nil nil true :left/value :right/value]
             [nil nil nil false :left/value :right/value]
             [:integer-eq 0 0 true :io/write :pure]
             [:integer-eq 0 1 false :io/write :pure]
             [:integer-lt -9223372036854775808 9223372036854775807
              true constant less-than]
             [:integer-lt 9223372036854775807 -9223372036854775808
              false constant less-than]]]
      (let [base (scalar-core (checked extension) condition
                              :gravity.type/keyword then-value else-value)
            core (if operation
                   (with-integer-comparison extension base :condition operation left right)
                   base)
            mir (build core) result (run core mir)
            nodes (:core-nodes core) ids (mapv :node-id nodes)
            rows (get-in result [:projection :operations])
            expected (if condition then-value else-value)
            types (mapv :type nodes)]
        (is (= :passed (get-in result [:upstream-verification :status])))
        (is (= :accepted (:status result)))
        (is (= :returned (get-in result [:execution :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= (namespace expected) (namespace (get-in result [:execution :value]))))
        (is (= (name expected) (name (get-in result [:execution :value]))))
        (is (= (if operation 505 507) (get-in result [:execution :fuel])))
        (is (= ids (mapv :operation-id rows)))
        (is (= types (mapv :source-type rows)))
        (is (= (mapv {:gravity.type/i64 :i64 :gravity.type/bool :bool
                      :gravity.type/keyword :keyword} types)
               (mapv :runtime-type rows)))
        (is (= (mapv :source nodes) (mapv :source rows)))
        (is (= (mapv #(get-in core [:type-facts % :fact-id]) ids)
               (mapv #(get-in % [:facts :type-fact-id]) rows)))
        (is (= (mapv #(get-in % [:attributes :value])
                     (filter #(= :literal (:source-operation %)) nodes))
               (mapv #(get-in % [:constant-payload :value])
                     (filter #(= :literal (:role %)) rows))))
        (is (= :passed (:status (verify core mir result)))))))
  ;; Both arms are validated, even when the bad typed Keyword is unselected.
  (doseq [extension [".gravity" ".qst"] condition [true false]
          value [":constant" 'constant nil true 7 [] {} '() #{}]]
    (let [core (scalar-core (checked extension) condition :gravity.type/keyword
                            (if condition :constant value)
                            (if condition value :integer-lt))
          mir (build core) result (run core mir)]
      (is (= :passed (get-in result [:upstream-verification :status])))
      (is (= :rejected (:status result)))
      (is (= :bounded-execution-subset
             (get-in result [:diagnostics 0 :missing-fact])))
      (is (nil? (:projection result)))
      (is (nil? (:execution result)))
      (is (= :passed (:status (verify core mir result))))))
  (let [core (scalar-core (checked ".gravity") true
                          :gravity.type/keyword :left/value :right/value)
        quoted (assoc-in core [:core-nodes 2 :source-operation] :quote)
        quoted-result (run quoted (build quoted))
        mir (build core) result (run core mir)
        then-id (:then (coordinates core mir))
        path [:functions (:entrypoint core) :blocks then-id :instructions 0]]
    (is (= :passed (get-in quoted-result [:upstream-verification :status])))
    (is (= :bounded-execution-subset
           (get-in quoted-result [:diagnostics 0 :missing-fact])))
    (is (nil? (:execution quoted-result)))
    (doseq [mutant [(assoc-in mir (into path [:constant-payload :value]) :right/value)
                    (assoc-in mir (into path [:facts :type-fact-id]) "forged")
                    (assoc-in mir (into path [:source :span :start]) 999)]]
      (let [original (invoke 'sh12-verify-branch-join-mir core mutant)
            rejection (run core mutant)]
        (is (= :rejected (:status original)))
        (is (= (:diagnostics original) (:diagnostics rejection)))
        (is (nil? (:execution rejection)))))
    (doseq [candidate [(assoc-in result [:execution :value] :right/value)
                       (assoc-in result [:projection :operations 1 :runtime-type] :bool)
                       (assoc-in result [:projection :operations 1 :role] :integer-less-than)]]
      (is (= :rejected (:status (verify core mir candidate))))))
  (let [callbacks (atom [])
        opaque (proxy [Object] []
                 (equals [_] (swap! callbacks conj :equals)
                   (throw (ex-info "opaque equality invoked" {})))
                 (hashCode [] (swap! callbacks conj :hash)
                   (throw (ex-info "opaque hash invoked" {}))))]
    (doseq [value [opaque [opaque] {:value opaque}]]
      (is (false? (invoke 'sh13-branch-join-literal-valid?
                          {:opcode :constant :source-operation :literal
                           :operands [] :type :gravity.type/keyword
                           :constant-payload {:present? true :value value}}
                          :gravity.type/keyword)))
      (is (empty? @callbacks))))
  (is (false? (invoke 'sh13-branch-join-entry-valid?
                      [{:opcode :constant :source-operation :literal :operands []
                        :type :gravity.type/keyword
                        :constant-payload {:present? true :value :condition}}])))
  (is (false? (invoke 'sh13-branch-join-arm-valid?
                      [{:opcode :constant} {:opcode :constant} {:opcode :integer-lt}]
                      :gravity.type/keyword))))
