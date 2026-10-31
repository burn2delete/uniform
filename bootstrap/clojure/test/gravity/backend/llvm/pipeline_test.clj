(ns gravity.backend.llvm.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [gravity.backend.llvm :as llvm]
            [gravity.bootstrap :as bootstrap]))

(defn- native [name & arguments]
  (apply (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b3-llvm-" name))) arguments))

(defn- operation [id block opcode operands type value]
  {:op-id id :block-id block :opcode opcode :source-operation (if (= opcode :constant) :literal opcode)
   :operands operands :type type :effects [] :capabilities []
   :constant-payload (if (= opcode :constant) {:present? true :value value} :not-applicable)
   :source {:span {:source "llvm-data.gravity" :start {:line 1 :column 1} :end {:line 1 :column 2}}
            :generated-origin [{:kind :fixture :origin-id (str "parent-" id)}]}
   :facts {:source-origin-id (str "source-" id) :type-fact-id (str "type-" id)
           :effect-fact-id (str "effect-" id) :safety-proof-id (str "proof-" id)}})

(defn- straight-operations []
  (vec (concat
        (map-indexed #(operation (str "c" %1) "entry" :constant []
                                 (cond (boolean? %2) :gravity/bool (nil? %2) :gravity/nil
                                       :else :gravity/integer) %2)
                     [0 7 -7 Long/MIN_VALUE Long/MAX_VALUE true false nil])
        (map-indexed #(operation (str "f" %1) "entry" %2 ["c1"] :gravity/integer nil)
                     [:local :local-binding :sequence :lexical-scope :function-boundary])
        (map-indexed #(operation (str "t" %1) "entry" :truthiness [%2] :gravity/bool nil)
                     ["c1" "c5" "c7"])
        (map-indexed #(operation (str "p" %1) "entry" %2 ["c1" "c2"] :gravity/bool nil)
                     [:integer-eq :integer-lt :integer-lte :integer-gt :integer-gte]))))

(defn- module-data [blocks join]
  (let [operations (mapcat :instructions (vals blocks))]
    {:module-id "fixture-module" :functions {'main {:entry "entry" :blocks blocks}}
     :source-core {:id "fixture-source-core"} :pass-execution-record {:record-id "fixture-pass"}
     :type-table (into {} (map #(vector (:op-id %) (:type %)) operations))
     :effect-table (into {} (map #(vector (:op-id %) []) operations))
     :safety-table (into {} (map #(vector (:op-id %) (:facts %)) operations))
     :source-map (into {} (map #(vector (:op-id %) (:source %)) operations))
     :control-flow-graph (if join {:join join} {})}))

(defn- fixtures []
  {"straight"
   (module-data {"entry" {:instructions (straight-operations)
                           :terminator {:kind :return :operands ["p4"] :successors []}}} nil)
   "diamond"
   (module-data
    {"entry" {:instructions [(operation "condition" "entry" :constant [] :gravity/bool true)]
               :successors ["then" "else"]
               :terminator {:kind :conditional-branch :operands ["condition"] :successors ["then" "else"]}}
     "then" {:instructions [(operation "left" "then" :constant [] :gravity/integer 7)]
              :terminator {:kind :branch :operands [] :successors ["join"]}}
     "else" {:instructions [(operation "right" "else" :constant [] :gravity/integer 9)]
              :terminator {:kind :branch :operands [] :successors ["join"]}}
     "join" {:instructions [(operation "result" "join" :conditional-join
                                        ["condition" "left" "right"] :gravity/integer nil)]
              :terminator {:kind :return :operands ["result"] :successors []}}}
    {:block-id "join" :incoming [{:predecessor "then"} {:predecessor "else"}]})})

(defn- capture-native [mir]
  (let [function (get-in mir [:functions 'main])
        order (native "block-order" mir function)
        operations (native "operation-sequence" function order)
        labels (native "block-labels" order)
        index (zipmap (map :op-id operations) (range))
        bad-op (operation "bad" "entry" :integer-eq ["missing" "c1"] :gravity/bool nil)]
    {:input mir :order order :operations operations :labels labels :index index
     :refusals (mapv #(native "operation-rejection" operations % labels)
                     [bad-op (assoc (first operations) :effects [:io])
                      (assoc (first operations) :capabilities [:io])
                      (assoc (first operations) :opcode :unsupported)
                      (operation "too-wide" "entry" :constant [] :gravity/integer
                                 (inc (bigint Long/MAX_VALUE)))])
     :accepted-reasons (mapv #(native "operation-rejection" operations % labels) operations)
     :two-argument-reasons (mapv #(native "operation-rejection" operations %) operations)
     :values (native "evaluate-operations" operations)
     :lines (mapv #(native "operation-line" mir % operations index labels) operations)
     :lowering (native "reconstructed-lowering" mir)}))

(def ^:private gravity-plan
  (delay
    (let [source (.getCanonicalPath (io/file "bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity"))
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :c))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source)))))

(defn- gravity-call [name arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-llvm-data-development :compiler-artifact-plan? true} @gravity-plan name arguments))

(deftest emitted-module-data-and-real-operation-refusals
  (doseq [[label mir] (fixtures)
          :let [{:keys [lowering refusals accepted-reasons two-argument-reasons]} (capture-native mir)]]
    (is (= :constructed-unverified (:status lowering)) label)
    (is (str/includes? (:llvm-ir lowering) "define ccc i32 @main()") label)
    (is (= :pending-host-content-binding (get-in lowering [:verified-input-closure :status])))
    (is (every? nil? accepted-reasons))
    (is (= accepted-reasons two-argument-reasons))
    (is (= [:integer-comparison-left-definition :program-effects-unsupported
            :program-capabilities-unsupported :unsupported-mir-opcode :bounded-scalar-constant-payload]
           refusals))
    (is (= [] (:ub-sensitive-flags lowering) (:emitted-metadata lowering) (:emitted-function-attributes lowering)))
    (is (= (:source-core mir) (get-in lowering [:verified-input-closure :source-core])))
    (is (= (count (:source-map mir)) (get-in lowering [:verified-input-closure :source-map-count])))
    (is (= "fixture-pass" (get-in lowering [:verified-input-closure :pass-execution-record-id])))))

(deftest current-gravity-instruction-and-module-record-parity
  (doseq [[label mir] (fixtures)
          :let [{:keys [operations order index labels lowering]} (capture-native mir)
                annotated (gravity-call 'b3-annotate-conditional-operations [operations mir])
                function (get-in mir [:functions 'main])
                emitted (gravity-call 'b3-build-emitted-lowering [mir function order annotated index labels])]]
    (is (= (:operation-records lowering) (:operation-records emitted)) label)
    (is (= (:block-records lowering) (:block-records emitted)) label)
    (is (= (:llvm-ir lowering) (:llvm-ir emitted)) label)
    (is (= (:semantic-result lowering) (:semantic-result emitted)) label)
    (is (= (:expected-exit-code lowering) (:expected-exit-code emitted)) label)
    (is (= :constructed-unverified (:status emitted)))
    (is (= (:verified-input-closure lowering) (:verified-input-closure emitted)))
    (is (= [] (:ub-sensitive-flags emitted) (:emitted-metadata emitted) (:emitted-function-attributes emitted)))))

(deftest compatibility-ports-preserve-recursion-and-late-value-reads
  (let [operation-rejection-var (ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-operation-rejection)
        labels-var (ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-block-labels)
        forward-var (ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-forward-opcodes)
        reference-var (ns-resolve 'gravity.bootstrap 'p15-s23-b3-llvm-value-reference)
        stage-rejection llvm/operation-rejection
        events (atom [])
        op (operation "custom" "entry" :custom-forward ["operand"] :gravity/integer nil)]
    (with-redefs-fn
      {operation-rejection-var (fn [& args] (swap! events conj [:recursive args]) :ported-refusal)
       labels-var (fn [order] (swap! events conj [:labels order]) {:ported true})}
      #(is (= :ported-refusal
              (stage-rejection operation-rejection-var labels-var nil nil nil nil nil nil [op] op))))
    (is (= [[:labels ["entry"]] [:recursive [[op] op {:ported true}]]] @events))
    (with-redefs-fn
      {forward-var #{}
       reference-var (fn [& _]
                       (alter-var-root forward-var (constantly #{:custom-forward}))
                       "%ported")}
      #(is (= "  %ported = add i64 0, %ported"
              (native "operation-line" {} op [op] {} {}))
           "Read the forwarded-opcode Var at the original point, after the value-reference callback"))))

(deftest reconstructed-module-calls-compatible-ports-in-order
  (let [mir (get (fixtures) "straight")
        names ["block-order" "operation-sequence" "block-labels" "value-reference"
               "operation-line" "evaluate-operations" "scalar-exit-code"]
        events (atom [])
        vars (mapv #(ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-b3-llvm-" %))) names)
        original (mapv var-get vars)
        replacements (zipmap vars (map (fn [name f]
                                         (fn [& arguments]
                                           (swap! events conj name)
                                           (apply f arguments))) names original))]
    (with-redefs-fn replacements #(is (= :constructed-unverified (:status (native "reconstructed-lowering" mir)))))
    (is (= ["block-order" "operation-sequence" "block-labels"] (subvec @events 0 3)))
    (is (= ["evaluate-operations" "scalar-exit-code"] (subvec @events (- (count @events) 2))))
    (is (= 21 (count (filter #{"operation-line"} @events))))))

(deftest source-facts-and-data-records-are-not-authority
  (let [contract (:gravity/namespace-contract (meta #'llvm/reconstructed-lowering))
        mir (get (fixtures) "straight")
        forged (assoc mir :source-core {:id "unverified-source-data"})
        lowering (native "reconstructed-lowering" forged)]
    (is (= 'gravity.backend.llvm (:namespace contract)))
    (is (false? (:authenticates-inputs? contract)))
    (is (false? (:issues-certificates? contract)))
    (is (every? #(= :none (:admission %)) (vals (:public-api contract))))
    (is (= {:id "unverified-source-data"} (get-in lowering [:verified-input-closure :source-core])))
    (is (= :constructed-unverified (:status lowering)))
    (doseq [field [:public? :release? :self-hosted? :whole-b3?]]
      (is (false? (get lowering field))))
    (is (= {} (native "block-labels" ["a" "b"])))
    (is (= [1 0 0 255] (mapv #(native "scalar-exit-code" %) [true false nil 255])))
    (is (false? (native "signed-i64?" (inc (bigint Long/MAX_VALUE)))))
    (is (false? (native "signed-i64?" (dec (bigint Long/MIN_VALUE)))))
    (is (true? (native "signed-i64?" Long/MIN_VALUE)))
    (is (true? (native "signed-i64?" Long/MAX_VALUE)))))

(deftest comparison-types-arity-and-dominance-are-concrete-legality
  (let [operations (straight-operations)
        comparison (last operations)
        labels {"entry" "entry"}]
    (doseq [[changed reason]
            [[(assoc comparison :operands ["c1"]) :integer-comparison-requires-two-operands]
             [(assoc comparison :source-operation :literal) :source-operation-opcode-parity]
             [(assoc comparison :constant-payload {}) :nonconstant-payload-must-be-not-applicable]
             [(assoc comparison :operands ["c5" "c1"]) :integer-comparison-left-operand-type]
             [(assoc comparison :type :gravity/integer) :integer-comparison-result-type]]]
      (is (= reason (native "operation-rejection" operations changed labels))))
    (let [future (operation "future" "entry" :constant [] :gravity/integer 1)
          changed (assoc comparison :operands ["c1" "future"])
          ops (conj operations future)]
      (is (= :integer-comparison-prior-definition-or-dominance
             (native "operation-rejection" ops changed labels))))
    (let [mir (get (fixtures) "diamond")
          function (get-in mir [:functions 'main])
          order (native "block-order" mir function)
          ops (native "operation-sequence" function order)
          labels (native "block-labels" order)
          cross-arm (operation "right" "else" :local ["left"] :gravity/integer nil)]
      (is (= :forwarded-operation-definition-or-dominance
             (native "operation-rejection" ops cross-arm labels))))))
