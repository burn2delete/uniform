(ns gravity.types.set-equality-test
  "Source-bound Clojure model controls only. No Gravity producer or runtime plan.
  C7/C8/STD2/STD3 acceptance still requires the genuine compiler composition."
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap]))

(def owners
  [{:path "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity"
    :prefix "types-set-equality" :legacy "sh08" :all 'sh08-ft-all-true?
    :source-helper 'sh08-ma-form-source}
   {:path "bootstrap/gravity/src/gravity/effects.gravity"
    :prefix "effects-set-equality" :legacy "body" :all 'body-all?
    :source-helper 'body-ma-form-node}
   {:path "bootstrap/gravity/p15_s23/emitter.gravity"
    :prefix "emit-set-equality" :legacy "p15-s23" :all 'p15-s23-typed-all
    :source-helper 'p15-s23-typed-source}])

(defn- source-forms [path]
  (with-open [reader (java.io.PushbackReader. (io/reader path))]
    (loop [out []]
      (let [form (read {:eof ::end} reader)]
        (if (= ::end form) out (recur (conj out form)))))))

(defn- definitions [path]
  (into {} (for [form (source-forms path)
                 :when (and (seq? form) (contains? '#{defn defn-} (first form)))]
             [(second form) form])))

(defn- bootstrap-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing native primitive" {:name name}))))

(defn- load-model [{:keys [path prefix legacy all source-helper]}]
  (let [space (create-ns (gensym "set-equality-model-"))
        definitions (definitions path)
        helpers (into #{all source-helper}
                      (map #(symbol (str legacy "-" %)))
                      ["lc-ancestor?" "lc-native-view?" "lc-native-arguments"
                       "bka-binding?" "bka-ids?" "bka-optional-id?" "sse-ids?"])
        helpers (cond-> helpers (= legacy "sh08") (conj 'sh08-sm-failure))
        helpers (into helpers
                      (map #(symbol (str legacy "-" %)))
                      (if (= legacy "sh08")
                        ["bka-entry?" "bka-derive" "bka-inferred-call" "kse-constraint" "kse-call-fact"]
                        ["bka-entry?" "bka-evidence" "bka-call-valid?" "bka-forms?"
                         "kse-constraint" "kse-call-fact"]))
        selected (into {} (filter (fn [[name _]]
                                    (or (str/starts-with? (str name) (str prefix "-"))
                                        (contains? helpers name)))) definitions)
        ;; This synthetic plan-shaped value admits only read-only native header
        ;; primitives. It is not an authenticated or executable compiler plan.
        primitive-context {:compiler-artifact-plan? true :kind :gravity/stage2-compiler-artifact-plan
                           :module {:profile :meta :target :jvm}}]
    (binding [*ns* space]
      (clojure.core/refer 'clojure.core)
      (intern space 'bootstrap-native-identical? identical?)
      (intern space 'bootstrap-native-carrier-header
              #((bootstrap-var 'p15-s23-stage2-native-carrier-header) primitive-context %))
      ;; Unselected fallback functions stay unbound and fail if accidentally
      ;; reached. Their declarations allow the selected dispatchers to compile.
      (doseq [name (keys definitions)] (intern space name))
      ;; Compilation's child boundary is injected. No typed child is executed.
      (doseq [name '[p15-s23-typed-compile-children p15-s23-compiler-complete?
                     p15-s23-compiler-result p15-s23-typed-node-error]]
        (intern space name (fn [& _] (throw (ex-info "Uninjected child boundary" {:name name})))))
      (doseq [[_ form] selected] (eval form)))
    {:space space :prefix prefix :definitions selected}))

(def models (delay (mapv load-model owners)))

(defn- invoke [{:keys [space prefix]} suffix & args]
  (apply (ns-resolve space (symbol (str prefix "-" suffix))) args))

(def fixture
  (delay
    ;; Reuse the inherited normalized input constructor without loading its
    ;; producer namespace or executing any Gravity source plan.
    (let [form (get (definitions "bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj")
                    'integral-ordering-input)]
      (binding [*ns* (the-ns 'gravity.types.set-equality-test)]
        (eval (list 'fn (nth form 2) (last form)))))))

(defn- input
  ([] (input [:gravity.type/set :gravity.type/set]))
  ([types] (@fixture '= types)))

(defn- header [model {:keys [core node call edge nodes bindings types]}]
  (invoke model "header" core node call edge nodes bindings types (:module core)))

(defn- product [model]
  (let [input (input) descriptor (:descriptor (header model input))
        types (assoc (:types input) :operator descriptor :call (:result-type descriptor))
        row (invoke model "constraint" descriptor)
        fact (invoke model "call-fact" descriptor (:module input))]
    (assoc input :types types :descriptor descriptor :ledger [row] :facts [fact])))

(defn- evidence [model {:keys [core ledger facts node call edge nodes bindings types]}]
  ;; C7 produces the descriptor/row/fact. Its real downstream census belongs
  ;; to Effects, so compose that consumer with C7's model product here.
  (let [consumer (if (= "types-set-equality" (:prefix model)) (second @models) model)]
    (invoke consumer "evidence" core ledger facts node call edge nodes bindings types)))

(deftest complete-three-owner-descriptors-and-transfer-model
  (doseq [pair [[:gravity.type/set :gravity.type/set]
                [:gravity.type/set :gravity.type/never]
                [:gravity.type/never :gravity.type/set]]]
    (let [results (mapv #(header % (input pair)) @models)
          descriptors (mapv :descriptor results)]
      (is (every? #(= :accepted (:status %)) results))
      (is (apply = descriptors))
      (is (= (if (some #{:gravity.type/never} pair) :gravity.type/never :gravity.type/bool)
             (:result-type (first descriptors))))
      (is (= [nil nil] (:operand-counts (first descriptors))))
      (is (= [] (:direct-effects (first descriptors))))
      (is (not-any? #(re-find #"work|budget|allowance|maximum" (str %))
                    (keys (first descriptors))))))
  (doseq [model @models]
    (is (= :accepted (:status (evidence model (product model)))))
    (doseq [pair [[:gravity.type/set :gravity.type/unknown]
                  [:gravity.type/set :gravity.type/string]
                  [:gravity.type/never :gravity.type/unknown]
                  [:gravity.type/set {:kind :gravity.type/set :shape :literal :element-types []}]]]
      (is (= :checked-bare-set-equality-operand-type-required (:reason (header model (input pair))))))))

(deftest original-source-authority-and-late-invalid-model
  (doseq [model @models]
    (let [base (product model)]
      (doseq [change [#(assoc-in % [:bindings :ordering-binding :binding-class] :namespace)
                      #(assoc-in % [:bindings :ordering-binding :effects] [:io/write])
                      #(assoc-in % [:bindings :ordering-binding :capabilities] [:fs/read])
                      #(assoc-in % [:bindings :ordering-binding :package :version] "forged")
                      #(assoc-in % [:core :module :target] :native)
                      #(assoc-in % [:core :module :profile] :kernel)
                      #(assoc-in % [:call :extra] true)
                      #(assoc-in % [:edge :ordered-evaluation-node-ids] [:operator :arg1 :arg0])
                      #(assoc-in % [:node :evaluation :order] [])
                      #(assoc-in % [:node :evaluation :owner-function-syntax-id] :missing)
                      #(assoc-in % [:nodes :arg1 :source :syntax-id] :substituted)
                      #(assoc-in % [:types :call] :gravity.type/integer)
                      #(update-in % [:core :calls] conj (:call base))
                      #(update-in % [:core :call-edges] conj (:edge base))]]
        (is (= :rejected (:status (evidence model (change base))))))
      (is (true? (invoke model "forms?" (:node base) (:call base) (:nodes base) (:forms base))))
      (is (false? (invoke model "forms?" (:node base) (:call base) (:nodes base)
                          (assoc-in (:forms base) [:arg1 :parent-form-id] :stale))))
      (let [records (mapv #(hash-map :function-syntax-id (keyword (str "owner" %))
                                    :function-core-node-id (keyword (str "body" %))) (range 1025))]
        (is (= :accepted (:status (header model (assoc-in base [:core :function-records] records)))))
        (is (= :rejected (:status (header model (assoc-in base [:core :function-records]
                                                        (conj records {:function-syntax-id nil}))))))))))

(deftest coherent-forgery-and-mixed-ledger-model
  (doseq [model @models]
    (let [base (product model) row (first (:ledger base)) fact (first (:facts base))
          ordinary-row {:lhs {:core-node-id :ordinary} :kind :equality}
          ordinary-fact {:call-core-node-id :ordinary :artifact :gravity/c7-call-type-fact}]
      (doseq [rows [[ordinary-row row] [row ordinary-row]]
              facts [[ordinary-fact fact] [fact ordinary-fact]
                     (array-map :ordinary ordinary-fact :call fact)]]
        (is (= :accepted (:status (evidence model (assoc base :ledger rows :facts facts))))))
      (doseq [change [#(update % :ledger conj row) #(update % :facts conj fact)
                      #(update % :ledger conj (assoc-in row [:lhs :core-node-id] :orphan))
                      #(update % :facts conj (assoc fact :call-core-node-id :orphan))
                      #(assoc % :facts {:wrong fact})
                      #(assoc % :ledger [(dissoc row :producer-rule)])
                      #(assoc % :facts [(dissoc fact :dispatch)])
                      #(assoc % :ledger [ordinary-row])
                      #(assoc % :facts [ordinary-fact])
                      #(assoc % :ledger [(assoc row :producer-rule nil)])
                      #(assoc % :facts [(assoc fact :dispatch false)])]]
        (is (= :rejected (:status (evidence model (change base))))))
      (is (= :rejected (:status (evidence model (update base :ledger conj (dissoc row :producer-rule))))))
      (is (= :rejected (:status (evidence model (update base :facts conj (dissoc fact :dispatch))))))
      (doseq [change [#(assoc % :operand-counts [0 0]) #(assoc % :direct-effects [:io/write])
                      #(assoc % :runtime-provider :foreign) #(assoc % :family-data-work 1)
                      #(assoc % :member-refinement :gravity.type/keyword)
                      #(assoc-in % [:runtime-policy :logical-work-limit] 65536)]]
        (let [forged (change (:descriptor base))]
          (is (= :rejected (:status (evidence model
                                     (-> base (assoc-in [:types :operator] forged)
                                         (assoc :ledger [(invoke model "constraint" forged)]
                                                :facts [(invoke model "call-fact" forged (:module base))])))))))))))

(deftest native-foreign-values-reject-with-zero-callbacks-model
  (let [callbacks (atom 0)
        foreign (proxy [Object] []
                  (hashCode [] (swap! callbacks inc) (throw (AssertionError. "hash")))
                  (equals [_] (swap! callbacks inc) (throw (AssertionError. "equality")))
                  (toString [] (swap! callbacks inc) (throw (AssertionError. "spelling"))))]
    (doseq [model @models]
      (doseq [value [foreign [1 2 foreign] (array-map foreign :inert)
                     (vec (concat (repeat 1025 :valid) [foreign]))]]
        (is (false? (invoke model "data?" value))))
      (let [base (product model)]
        (doseq [path [[:call :operator-binding-id] [:types :arg1] [:types :operator :value-origin]
                      [:bindings :ordering-binding :profile-set] [:ledger 0 :producer-rule]
                      [:facts 0 :dispatch] [:forms :arg1 :value]]]
          (let [changed (assoc-in base path foreign)]
            (if (= :forms (first path))
              (is (false? (invoke model "forms?" (:node changed) (:call changed) (:nodes changed) (:forms changed))))
              (is (= :rejected (:status (evidence model changed)))))))))
    (is (zero? @callbacks))))

(deftest source-dispatch-and-injected-lowering-model
  (doseq [[owner model] (map vector owners @models)]
    (let [base (product model) space (:space model)
          legacy (fn [suffix & args]
                   (apply (ns-resolve space (symbol (str (:legacy owner) "-" suffix))) args))
          {:keys [core node call edge nodes bindings types descriptor ledger facts forms]} base]
      (is (true? (legacy "bka-entry?" node call nodes bindings types (:module core))))
      (is (= (first ledger) (legacy "kse-constraint" descriptor)))
      (is (= (first facts) (legacy "kse-call-fact" descriptor (:module core))))
      (if (= "sh08" (:legacy owner))
        (do (is (= :accepted (:status (legacy "bka-derive" core node call edge nodes bindings types forms))))
            (is (= :accepted (:status (legacy "bka-inferred-call" core node call edge nodes bindings types)))))
        (do (is (= :accepted (:status (legacy "bka-evidence" core ledger facts node call edge nodes bindings types 1))))
            (when (= "body" (:legacy owner))
              (let [body-facts {:constraint-ledger ledger :call-type-facts facts :type-table types
                                :equality-context {:forms forms}}]
                (is (true? (legacy "bka-call-valid?" core body-facts bindings node call edge nodes)))
                (is (false? (legacy "bka-call-valid?" core (assoc-in body-facts [:equality-context :forms :arg1 :parent-form-id] :stale)
                                   bindings node call edge nodes)))))))))
  (let [model (last @models) {:keys [core node call edge nodes bindings types ledger facts forms]} (product model)
        context {:core core :calls {:call call} :edges {:call edge} :nodes nodes :bindings bindings
                 :types types :ledger ledger :call-facts facts :forms forms :call-order {:call 0}}
        calls (atom []) space (:space model)
        child-var (ns-resolve space 'p15-s23-typed-compile-children)
        error-var (ns-resolve space 'p15-s23-typed-node-error)
        redefinitions {child-var (fn [context ids remaining out]
                                  (swap! calls conj [(:tail? context) ids remaining out])
                                  {:status :complete :value [:left-ir :right-ir] :work-count 7})
                       (ns-resolve space 'p15-s23-compiler-complete?) #(= :complete (:status %))
                       (ns-resolve space 'p15-s23-compiler-result) #(hash-map :status :complete :value %)
                       error-var (fn [reason _] {:status :rejected :reason reason})}]
    (with-redefs-fn redefinitions
      #(do (is (= {:status :complete :value {:op :builtin-call :function '= :args [:left-ir :right-ir]}
                    :work-count 7}
                   (invoke model "compile" context node 19)))
           (is (= [[false [:arg0 :arg1] 19 []]] @calls))
           (reset! calls [])
           (is (= :rejected (:status (invoke model "compile" (assoc-in context [:forms :arg1 :parent-form-id] :stale) node 19))))
           (is (empty? @calls))))))

(deftest complete-native-data-and-metadata-model
  (doseq [model @models]
    (let [shared [1 :two "three"]]
      (is (true? (invoke model "data?" [shared shared])))
      (is (true? (invoke model "data?" (vec (repeat 1025 :valid)))))
      (is (true? (invoke model "data?" (nth (iterate vector :leaf) 129))))
      (is (false? (invoke model "data?" (with-meta [1] {:inert true}))))
      (is (false? (invoke model "data?" (cons 1 (with-meta (list 2) {:inert true}))))))
    (let [root (list :head) header-var (ns-resolve (:space model) 'bootstrap-native-carrier-header)
          original @header-var]
      ;; Inject only the raw tail edge. No host carrier is mutated and no
      ;; reflective access policy is changed to manufacture a cyclic value.
      (with-redefs-fn {header-var #(if (identical? root %) [:list-node false nil nil :head root] (original %))}
        #(is (false? (invoke model "data?" root)))))))
