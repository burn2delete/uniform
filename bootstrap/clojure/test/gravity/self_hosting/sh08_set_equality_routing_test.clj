(ns gravity.self-hosting.sh08-set-equality-routing-test
  "Current Gravity routing controls on existing normalized SH-08 inputs.
  These invoke actual C7 functions on supplied DATA, not source composition."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(defn- observe [original-route? invoke]
  ;; Disabling only the new negative guard delegates to the unchanged original
  ;; Gravity predicate, including its complete native-DATA admission. No host
  ;; replacement computes the predicate or any accepted semantic product.
  @support/c7-plan
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped
        counts (atom {:whole-product-preflights 0 :routing-preflights 0 :ancestor-visits 0})
        result (with-redefs-fn
                 {scoped
                  (fn [runtime plan callee args scope]
                    (if (and original-route? (= 'types-set-equality-routing-types? callee))
                      true
                      (do
                        (when (= 'types-set-equality-data? callee)
                          (case (count (first args))
                            6 (swap! counts update :whole-product-preflights inc)
                            2 (swap! counts update :routing-preflights inc)
                            nil))
                        (when (= 'sh08-lc-ancestor? callee)
                          (swap! counts update :ancestor-visits inc))
                        (original runtime plan callee args scope))))}
                 invoke)]
    (assoc @counts :result result)))

(defn- entry [input]
  (let [{:keys [node call nodes bindings types module]} input]
    (support/invoke-c7 'types-set-equality-entry? [node call nodes bindings types module])))

(defn- equality-step [input]
  (let [{:keys [core node call edge nodes bindings types forms]} input
        context {:ordering-core core :ordering-operators {:operator call}
                 :ordering-edges {:call edge} :lookup-nodes nodes
                 :ordering-bindings bindings :association-forms forms}]
    (support/invoke-c7 'sh08-ae-step [node types {} {} context])))

(deftest bare-set-routing-preserves-original-predicate-development-kernel
  (let [finite {:kind :gravity.type/set :shape :literal :element-types [:gravity.type/keyword]}]
    (doseq [[pair expected] [[[:gravity.type/set :gravity.type/set] true]
                             [[:gravity.type/set :gravity.type/never] true]
                             [[:gravity.type/never :gravity.type/set] true]
                             [[:gravity.type/never :gravity.type/never] false]
                             [[:gravity.type/set :gravity.type/unknown] false]
                             [[:gravity.type/set :gravity.type/string] false]
                             [[:gravity.type/string :gravity.type/set] false]
                             [[:gravity.type/set finite] false]
                             [[finite :gravity.type/set] false]
                             [[:gravity.type/string :gravity.type/string] false]
                             [[:gravity.type/integer :gravity.type/integer] false]
                             [[:gravity.type/keyword :gravity.type/bool] false]]]
      (let [input (support/atomic-equality-input pair)
            current (observe false #(entry input))
            original (observe true #(entry input))]
        (is (= expected (:result original) (:result current)))
        (when expected
          (is (= 1 (:whole-product-preflights current))))))
    (doseq [change [#(assoc % :call nil)
                    #(assoc-in % [:call :argument-node-ids] [:arg0])
                    #(assoc-in % [:call :argument-node-ids] [:arg0 :arg1 :extra])
                    #(assoc-in % [:call :operator-binding-id] :missing)
                    #(assoc-in % [:bindings :ordering-binding :name] 'identical?)
                    #(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)]]
      (let [input (change (support/atomic-equality-input [:gravity.type/set :gravity.type/set]))]
        (is (= false (:result (observe true #(entry input)))
                     (:result (observe false #(entry input)))))))))

(deftest ordinary-equality-avoids-whole-product-preflight-development-kernel
  (doseq [pair [[:gravity.type/string :gravity.type/string]
                [:gravity.type/integer :gravity.type/integer]
                [:gravity.type/keyword :gravity.type/bool]]]
    (let [input (support/atomic-equality-input pair)
          current (observe false #(equality-step input))
          original (observe true #(equality-step input))]
      (is (= :accepted (get-in current [:result :status])))
      (is (= (:result original) (:result current)))
      (is (zero? (:whole-product-preflights current)))
      (is (pos? (:routing-preflights current)))
      (is (pos? (:whole-product-preflights original)))
      (is (< (:ancestor-visits current) (:ancestor-visits original))))))

(deftest bare-set-routing-preserves-native-lookup-order-development-kernel
  (let [base (support/atomic-equality-input [:gravity.type/set :gravity.type/set])
        non-equality (assoc-in base [:bindings :ordering-binding :name] 'other)
        invalid-ratio (clojure.lang.Ratio. java.math.BigInteger/ONE java.math.BigInteger/ZERO)
        outcome (fn [input]
                  (try (entry input)
                       (catch Throwable error [:threw (.getName (class error))])))]
    (doseq [input [(assoc non-equality :types (sorted-map 0 :gravity.type/set))
                   (assoc-in non-equality [:call :argument-node-ids] [invalid-ratio :arg1])
                   (assoc non-equality :types [:gravity.type/set :gravity.type/set])
                   (assoc non-equality :types (list :gravity.type/set))
                   (assoc non-equality :types :not-a-table)
                   (-> non-equality
                       (assoc :call (sorted-map 0 :native))
                       (update :nodes with-meta {:inert true}))
                   (-> base
                       (assoc :types (sorted-map 0 :gravity.type/set))
                       (update :nodes with-meta {:inert true}))]]
      (is (= false (:result (observe true #(outcome input)))
                   (:result (observe false #(outcome input))))))
    ;; The entry predicate is only routing. Retain its existing answer even
    ;; for native lookup shapes rejected by the later full source checker.
    (doseq [[ids types] [[[0 1] [:gravity.type/set :gravity.type/set]]
                         [[[:left] '(:right)] {[:left] :gravity.type/set '(:right) :gravity.type/set}]
                         [[nil nil] {nil :gravity.type/set}]]]
      (let [input (-> base (assoc :types types) (assoc-in [:call :argument-node-ids] ids))]
        (is (= (:result (observe true #(outcome input)))
               (:result (observe false #(outcome input)))))))))

(deftest bare-set-routing-preserves-native-rejections-development-kernel
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "unexpected hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "unexpected equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "unexpected formatting"))))]
    (doseq [pair [[:gravity.type/set :gravity.type/set]
                  [:gravity.type/string :gravity.type/string]]
            [label change]
            [[:argument-id #(assoc-in % [:call :argument-node-ids] [:arg0 opaque])]
             [:operator-id #(assoc-in % [:call :operator-binding-id] opaque)]
             [:type-key #(assoc % :types (array-map opaque :gravity.type/set))]
             [:operand-type #(assoc-in % [:types :arg1] opaque)]
             [:late-type-data #(assoc-in % [:types :unrelated] (conj (vec (repeat 65 :valid)) opaque))]
             [:source #(assoc-in % [:nodes :arg0 :source :syntax-id] opaque)]
             [:late-source-data #(assoc-in % [:nodes :unrelated] (conj (vec (repeat 65 :valid)) opaque))]
             [:binding #(assoc-in % [:bindings :ordering-binding :profile-set] [:meta opaque])]
             [:module #(assoc-in % [:module :unrelated] opaque)]
             [:call-metadata #(update % :call with-meta {:inert true})]
             [:type-metadata #(update % :types with-meta {:inert true})]
             [:source-metadata #(update % :nodes with-meta {:inert true})]]]
      (testing (name label)
        (let [input (change (support/atomic-equality-input pair))]
          (is (= false (:result (observe true #(entry input)))
                       (:result (observe false #(entry input))))))))
    (is (zero? @callbacks))))

(deftest bare-set-routing-preserves-cyclic-data-rejection-development-kernel
  (let [cycle (list :head)
        header (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-native-carrier-header)
        original @header]
    ;; Inject only the raw native tail edge, as in the existing Set-equality
    ;; model control. No host carrier or reflective access policy is modified.
    (with-redefs-fn
      {header (fn [plan value]
                (if (identical? cycle value)
                  [:list-node false nil nil :head cycle]
                  (original plan value)))}
      #(doseq [pair [[:gravity.type/set :gravity.type/set]
                     [:gravity.type/string :gravity.type/string]]]
         (let [input (assoc-in (support/atomic-equality-input pair) [:types :unrelated] cycle)]
           (is (= false (:result (observe true (fn [] (entry input))))
                        (:result (observe false (fn [] (entry input)))))))))))

(def ^:private vector-stack-reference
  ;; Frozen original Gravity walker from 0de2f6a, with only its name changed.
  ;; It executes through the same current Gravity runtime and native helpers.
  '(defn types-set-equality-vector-stack-reference? [value]
     (loop [pending [[value []]]]
       (if (= 0 (count pending)) true
         (let [frame (get pending (- (count pending) 1)) value (get frame 0)
               ancestors (get frame 1) tail (subvec pending 0 (- (count pending) 1))
               header (bootstrap-native-carrier-header value) kind (get header 0)]
           (if (get header 1) false
             (if (contains? #{:nil :boolean :character :integer8 :integer16 :integer32 :integer64
                              :big-int :big-integer :ratio :big-decimal :float32 :float64
                              :string :symbol :keyword :empty-list} kind)
               (recur tail)
               (if (sh08-lc-ancestor? value ancestors) false
                 (let [next-ancestors (conj ancestors value)
                       items (if (contains? #{:vector :subvector} kind)
                               (if (sh08-lc-native-view? value) value nil)
                               (if (contains? #{:list-node} kind)
                                 (let [ingress (sh08-lc-native-arguments value)]
                                   (if (= :accepted (get ingress :status))
                                     (loop [current value out []]
                                       (let [part (bootstrap-native-carrier-header current)]
                                         (if (get part 1) nil
                                           (if (= :list-node (get part 0))
                                             (recur (get part 5) (conj out (get part 4))) out)))) nil))
                                 (if (if (= :map kind) true
                                       (if (= :tree-map kind) (= :default-comparator (get header 4)) false))
                                   (if (>= (get header 2) 0)
                                     (loop [entries (vec value) out []]
                                       (if (= 0 (count entries)) out
                                         (let [entry (first entries)]
                                           (recur (rest entries) (conj (conj out (get entry 0)) (get entry 1)))))) nil)
                                   (if (= :set kind)
                                     (if (>= (get header 2) 0) (vec value) nil) nil))))]
                   (if (= nil items) false
                     (recur (loop [remaining items out tail]
                              (if (= 0 (count remaining)) out
                                (recur (rest remaining) (conj out [(first remaining) next-ancestors])))))))))))))))

(def ^:private comparison-plan
  (delay
    (let [path (support/path "language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity")
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
      ;; An explicitly supplied test source: complete current C7 plus the
      ;; frozen reference. Its own source hash differs from the production plan.
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter "sh08-set-equality-routing-comparison.gravity"
       (str (slurp path) "\n" (pr-str vector-stack-reference) "\n")))))

(defn- observe-native-data [function value identities]
  (let [plan @comparison-plan
        header (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-native-carrier-header)
        builtin (ns-resolve 'gravity.runtime 'hosted-builtin)
        original-header @header original-builtin @builtin
        visits (atom []) copies (atom 0)
        result (with-redefs-fn
                 {header (fn [plan value]
                           (when-not (.containsKey ^java.util.IdentityHashMap identities value)
                             (.put ^java.util.IdentityHashMap identities value
                                   (.size ^java.util.IdentityHashMap identities)))
                           (swap! visits conj (.get ^java.util.IdentityHashMap identities value))
                           (original-header plan value))
                  builtin (fn [plan callee args operations]
                            (when (= 'subvec callee)
                              (swap! copies inc))
                            (original-builtin plan callee args operations))}
                 #(bootstrap/p15-s23-stage2-runtime-execute-function
                   {:engine :gravity-sh08-set-routing-comparison :compiler-artifact-plan? true}
                   plan function [value]))]
    {:result result :native-header-order @visits :pending-copies @copies}))

(deftest native-data-linked-stack-preserves-order-development-kernel
  (let [callbacks (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! callbacks inc) (throw (AssertionError. "unexpected hash")))
                 (equals [_] (swap! callbacks inc) (throw (AssertionError. "unexpected equality")))
                 (toString [] (swap! callbacks inc) (throw (AssertionError. "unexpected formatting"))))
        shared [:shared {:nested (list 'one 'two)}]
        compare (fn [value expected]
                  (let [identities (java.util.IdentityHashMap.)
                        original (observe-native-data 'types-set-equality-vector-stack-reference? value identities)
                        current (observe-native-data 'types-set-equality-data? value identities)]
                    (is (= expected (:result original) (:result current)))
                    (is (seq (:native-header-order original)))
                    (is (= (:native-header-order original) (:native-header-order current)))
                    (is (pos? (:pending-copies original)))
                    (is (zero? (:pending-copies current)))))]
    (doseq [value [nil [] '() [shared shared] (subvec [:unused shared :end] 1)
                   (array-map :vector [1 true :value] :list (list 'a 'b) :set #{:a :b})
                   (vec (repeat 129 :valid)) (nth (iterate vector :leaf) 129)]]
      (compare value true))
    (doseq [value [opaque (array-map opaque :inert) (conj (vec (repeat 129 :valid)) opaque)
                   (with-meta [1] {:inert true}) (cons 1 (with-meta (list 2) {:inert true}))]]
      (compare value false))
    (let [cycle (list :head)
          header (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-native-carrier-header)
          original @header]
      (with-redefs-fn
        {header (fn [plan value]
                  (if (identical? cycle value)
                    [:list-node false nil nil :head cycle]
                    (original plan value)))}
        #(compare cycle false)))
    (is (zero? @callbacks))))
