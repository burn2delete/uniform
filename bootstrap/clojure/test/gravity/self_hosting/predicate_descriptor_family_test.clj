(ns gravity.self-hosting.predicate-descriptor-family-test
  "Normalized actual-owner descriptor parity; no public admission authority."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-type-test]
            [gravity.self-hosting.sh09-body-effect-test]
            [gravity.self-hosting.sh01-stage2-runtime-iteration-test]))

(def families
  [["map" "mapp"] ["set" "setp"] ["list" "listp"]
   ["symbol" "symp"] ["boolean" "boolp"] ["character" "chp"]
   ["nil" "nilp"] ["keyword" "kp"] ["string" "strp"]
   ["integer" "ip"] ["vector" "vp"]])

(deftest complete-predicate-descriptors-cross-actual-owners
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test
        c7 (ns-resolve upstream 'invoke-c7)
        body (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime (var-get (ns-resolve 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'runtime))
        path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        lower (fn [function & args]
                (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        runtime-plan (:plan ((ns-resolve 'gravity.self-hosting.sh01-stage2-runtime-iteration-test 'typed-catch-runtime-projection)))
        late-invalid {:kind :gravity.type/vector :shape :literal
                      :element-types (assoc (vec (repeat 1025 :gravity.type/keyword)) 1024 :gravity.type/unknown)}]
    (is (= 11 (count families) (count (set families))))
    (doseq [[label family] families]
      (testing label
        (let [input-fn (ns-resolve upstream (symbol (str label "-predicate-input")))
              derive (ns-resolve upstream (symbol (str label "-predicate-derived")))
              {:keys [core node nodes forms bindings types module call edge] :as input} (input-fn [:gravity.type/string])
              result (derive input) descriptor (:descriptor result)
              validation (lower 'p15-s23-lc-validate-types [:gravity.type/string])
              body-validation (body 'body-lc-validate-types [:gravity.type/string])
              args [core node call edge nodes bindings types module]
              emitted (lower (symbol (str "p15-s23-" family "-finish"))
                             (apply lower (symbol (str "p15-s23-" family "-source")) (conj args validation)))
              effected (body (symbol (str "body-" family "-finish"))
                             (apply body (symbol (str "body-" family "-source")) (conj args body-validation)))
              fact #(c7 (symbol (str "sh08-" family "-call-fact")) [% module])
              ledger #(vector (c7 (symbol (str "sh08-" family "-constraint")) [%]))
              facts {:type-table (assoc types :operator descriptor :call (:type result))
                     :function-type-table [] :call-type-facts [(fact descriptor)] :constraint-ledger (ledger descriptor)}
              request {:canonical-core-artifact core
                       :b47-context {:authenticated-core-request {:forms (vec (vals forms))}}}
              context-fn (symbol (str "body-" family "-context"))
              context-key (keyword (str label "-predicate-context"))
              effect-context (body context-fn request facts bindings)
              checked (assoc facts context-key (:context effect-context))
              lower-context {:core core :nodes nodes :forms forms :bindings bindings
                             :types (:type-table facts) :calls {:call call} :edges {:call edge} :call-order {:call 0}
                             :call-facts {:call (fact descriptor)} :ledger (ledger descriptor)
                             :locals {:arg0 'value} :parameter-types types}
              compile #(lower 'p15-s23-typed-compile-node % :call)
              lowered (compile lower-context)]
          (is (= :accepted (:status result) (:status effect-context)))
          (is (not (contains? descriptor :operand-type-work)))
          (is (= descriptor emitted effected))
          (is (true? (body 'body-call-joins-with-foreign-valid? core checked bindings {})))
          (is (= :complete (:status lowered)))
          (when (= :complete (:status lowered))
            (is (= [(= label "string") "" :normal]
                   (bootstrap/p15-s23-stage2-runtime-execute-function
                    runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction
                    [(:value lowered) {'value "family-payload"} ""]))))
          (doseq [[key value] [[:operand-type-work 0] [:operand-type-work 1] [:operand-type-work nil] [::unknown-field true]]]
            (let [forged (assoc descriptor key value)
                  changed-facts (-> checked (assoc-in [:type-table :operator] forged)
                                    (assoc :call-type-facts [(fact forged)] :constraint-ledger (ledger forged)))
                  changed-lowering (-> lower-context (assoc-in [:types :operator] forged)
                                       (assoc :call-facts {:call (fact forged)} :ledger (ledger forged)))]
              (is (false? (body 'body-call-joins-with-foreign-valid? core changed-facts bindings {})))
              (is (= :rejected (:status (compile changed-lowering))))))
          (let [callbacks (atom 0)
                opaque (proxy [Object] []
                         (equals [_] (swap! callbacks inc) (throw (AssertionError. "opaque type compared")))
                         (hashCode [] (swap! callbacks inc) (throw (AssertionError. "opaque type hashed")))
                         (toString [] (swap! callbacks inc) (throw (AssertionError. "opaque type printed"))))]
            (doseq [bad [late-invalid opaque]]
              (let [changed-input (assoc-in input [:types :arg0] bad)
                    changed-facts (assoc-in facts [:type-table :arg0] bad)
                    changed-lowering (-> lower-context (assoc-in [:types :arg0] bad)
                                         (assoc-in [:parameter-types :arg0] bad))]
                (is (= :rejected (:status (derive changed-input))))
                (is (= :rejected (:status (body context-fn request changed-facts bindings))))
                (is (= :rejected (:status (compile changed-lowering))))))
            (is (zero? @callbacks))))))))
