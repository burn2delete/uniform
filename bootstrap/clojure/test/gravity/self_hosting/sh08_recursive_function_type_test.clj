(ns gravity.self-hosting.sh08-recursive-function-type-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh08-function-call-kernel-support :as kernel-support]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh08_recursive_function_type_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-08 recursive function type test source is absent"
                {:id "STD08-C7-RECURSIVE-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "STD08-C7-RECURSIVE-REPOSITORY-ROOT"}))
        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate
        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))
(def ^:private c7-relative-path
  "bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity")
(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-08")
(def ^:private c7-plan
  (delay
    (let [source-path (str (.resolve @root c7-relative-path))
          source-text (slurp source-path)
          emitter
          (:emitter
           (bootstrap/c-backend-stage2-plan-emitter-source-rule!
            source-path :jvm))]
      (bootstrap/p15-s23-stage2-compiler-artifact-plan
       emitter source-path source-text))))

(defn- invoke-c7 [function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh08-recursive-function-type
    :compiler-artifact-plan? true}
   @c7-plan function arguments))

(defn- node
  [node-id core-form children attributes owner]
  {:node-id node-id
   :core-form core-form
   :children children
   :attributes attributes
   :evaluation {:owner-function-syntax-id owner}
   :source {:syntax-id node-id
            :semantic-span {:line 1 :column 1 :length 1}
            :origin-chain []
            :generated-origin nil}})

(defn- recursive-carrier
  ([] (recursive-carrier {}))
  ([overrides]
   (let [function-syntax :recursive-function-syntax
         definition-syntax :recursive-definition-syntax
         function-node :recursive-function-node
         body-node :recursive-body-node
         condition-node :recursive-condition-node
         call-node :recursive-call-node
         operator-node :recursive-operator-node
         parameter-node :recursive-parameter-node
         base-node :recursive-base-node
         external-call-node :recursive-external-call-node
         external-operator-node :recursive-external-operator-node
         external-literal-node :recursive-external-literal-node
         definition-binding :recursive-definition-binding
         parameter-binding :recursive-parameter-binding
         nodes
         [(node function-node :fn [body-node] {} function-syntax)
          (node body-node :if [condition-node call-node base-node] {}
                function-syntax)
          (node condition-node :literal [] {:literal-kind :boolean}
                function-syntax)
          (node operator-node :reference []
                {:binding-id definition-binding} function-syntax)
          (node parameter-node :reference []
                {:binding-id parameter-binding} function-syntax)
          (node call-node :call [operator-node parameter-node] {}
                function-syntax)
          (node base-node :literal [] {:literal-kind :integer}
                function-syntax)
          (node external-operator-node :reference []
                {:binding-id definition-binding} nil)
          (node external-literal-node :literal [] {:literal-kind :integer} nil)
          (node external-call-node :call
                [external-operator-node external-literal-node] {} nil)]
         definitions
         [{:binding-id definition-binding
           :syntax-id definition-syntax
           :value-node-id function-node}]
         function-records
         [{:ordinal 0
           :function-core-node-id function-node
           :function-form-id :recursive-function-form
           :function-syntax-id function-syntax
           :body-core-node-id body-node
           :parameter-binding-ids [parameter-binding]
           :fixed-arity 1
           :definition-kind :named-top-level
           :definition-binding-id definition-binding
           :definition-core-node-id :recursive-definition-node
           :definition-name 'self-recursive-type}]
         calls
         [{:core-node-id call-node
           :operator-binding-id definition-binding
           :argument-node-ids [parameter-node]
           :ordered-evaluation-node-ids [operator-node parameter-node]
           :evaluation-order :operator-then-arguments}
          {:core-node-id external-call-node
           :operator-binding-id definition-binding
           :argument-node-ids [external-literal-node]
           :ordered-evaluation-node-ids
           [external-operator-node external-literal-node]
           :evaluation-order :operator-then-arguments}]
         edges
         [{:ordinal 0
           :call-core-node-id call-node
           :caller-function-syntax-id function-syntax
           :caller-function-core-node-id function-node
           :callee-binding-id definition-binding
           :callee-definition-syntax-id definition-syntax
           :callee-function-syntax-id function-syntax
           :callee-function-core-node-id function-node
           :argument-core-node-ids [parameter-node]
           :ordered-evaluation-node-ids [operator-node parameter-node]
           :evaluation-order :operator-then-arguments
           :classification :local-function}
          {:ordinal 1
           :call-core-node-id external-call-node
           :caller-function-syntax-id nil
           :caller-function-core-node-id nil
           :callee-binding-id definition-binding
           :callee-definition-syntax-id definition-syntax
           :callee-function-syntax-id function-syntax
           :callee-function-core-node-id function-node
           :argument-core-node-ids [external-literal-node]
           :ordered-evaluation-node-ids
           [external-operator-node external-literal-node]
           :evaluation-order :operator-then-arguments
           :classification :local-function}]
         bindings
         [{:binding-id definition-binding
           :definition-syntax-id definition-syntax}
          {:binding-id parameter-binding
           :definition-syntax-id definition-syntax}]
         component
         {:ordinal 0
          :kind :self-recursive
          :function-syntax-ids [function-syntax]
          :function-core-node-ids [function-node]
          :internal-call-edge-ordinals [0]}
         module {:effects #{}
                 :capabilities #{}
                 :profile :meta
                 :target :jvm}
         base {:nodes nodes
               :definitions definitions
               :function-records function-records
               :calls calls
               :edges edges
               :call-edges edges
               :recursion-components [component]
               :lexical-bindings []
               :bindings bindings
               :module module
               :artifact-id :recursive-artifact
               :provenance {:source-path "synthetic"}}]
     (loop [remaining overrides result base]
       (if (= (count remaining) 0)
         result
         (recur (rest remaining)
                (assoc-in result (first (first remaining))
                          (second (first remaining)))))))))

(defn- recursive-proof [carrier]
  (invoke-c7
   'sh08-ft-recursive-proof
   [(:nodes carrier) (:definitions carrier) (:bindings carrier)
    (:function-records carrier) (:calls carrier) (:edges carrier)
    (:recursion-components carrier) (:module carrier)]))

(defn- recursive-inference-nodes [carrier]
  ;; C6 emits child nodes before their consumers. Keep the normalized proof
  ;; carrier's stable indexes for hostile mutations, then use C6 order at the
  ;; inference boundary; the cold-if checker does not infer from a parent first.
  (let [nodes (into {} (map (juxt :node-id identity) (:nodes carrier)))]
    (vec (keep nodes [:recursive-condition-node :recursive-operator-node
                     :recursive-parameter-node :transformed-self-argument
                     :recursive-call-node :recursive-base-node
                     :recursive-body-node :recursive-function-node
                     :recursive-external-operator-node :recursive-external-literal-node
                     :recursive-external-call-node]))))

(def ^:private private-self-census-data kernel-support/private-self-census-data)

(deftest current-gravity-private-self-census-development-kernel
  (let [{:keys [core record bindings forms]} (private-self-census-data)
        census (fn [core bindings forms]
                 (invoke-c7 'sh08-is-private-census
                            [record core (into {} (map (juxt :node-id identity) (:nodes core))) bindings forms]))
        result (census core bindings forms)]
    (is (= :closed-private-self-use-census-v1 (:kind result)))
    (is (= record (:function-record result)))
    (is (= :private (get-in result [:definition-binding :visibility])))
    (is (= 3 (count (:calls result)) (count (:edges result))
           (count (:reference-uses result)) (count (:resolutions result)) (count (:reference-node-ids result))))
    (is (= [:span-binding] (mapv :binding-id (:parameter-bindings result))))
    (is (= [3] (get-in result [:component :internal-call-edge-ordinals])))
    (doseq [changed [(assoc-in core [:module :exports] ['without-path])
                     (assoc-in core [:module :exports] [(symbol (str (get-in core [:module :namespace]) "/without-path"))])
                     (update core :reference-uses pop)
                     (update-in core [:preserved-resolution :resolution-table] pop)
                     (update core :calls pop)
                     (update core :reference-uses conj (last (:reference-uses core)))
                     (assoc-in core [:recursion-components 0 :kind] :mutually-recursive)
                     (assoc-in core [:recursion-components 0 :internal-call-edge-ordinals] [1])
                     (update core :recursion-components conj (first (:recursion-components core)))
                     (assoc core :var-references [{:binding-id :helper-binding}])
                     (assoc core :mutations [{:target-binding-id :helper-binding}])
                     (assoc core :mutations [{:target-binding-id :span-binding}])
                     (assoc core :recur-transfers [{:target-id :missing}])
                     (assoc-in core [:call-edges 3 :caller-function-syntax-id] :caller-0)
                     (update core :nodes conj {:node-id :escaped :core-form :reference
                                               :attributes {:binding-id :helper-binding} :resolved-binding-ids [:helper-binding]})]]
      (is (nil? (census changed bindings forms))))
    (is (nil? (census core (assoc-in bindings [:helper-binding :visibility] :public) forms)))
    (is (nil? (census core (assoc-in bindings [:span-binding :mutability] :mutable) forms)))
    (is (nil? (census core bindings (assoc-in forms [:self-op :value] 'other))))
    (let [caller-control (assoc core :recur-targets [{:target-id :caller-target :owner-core-node-id :caller-0 :binding-ids []}]
                                    :recur-transfers [{:target-id :caller-target}])]
      (is (= result (census caller-control bindings forms))))))

(deftest current-gravity-private-self-cold-cut-development-kernel
  (let [{:keys [core record]} (private-self-census-data)
        owner (:function-syntax-id record)
        additions [(node :alias-read :reference [] {:binding-id :alias} nil)
                   (node :definition-read :reference [] {:binding-id :result-definition} nil)
                   (node :dependent-do :do [:alias-read] {} nil)
                   (node :genuine-throw :throw [:self-call] {} nil)
                   (node :genuine-recur :recur [:self-call] {} nil)
                   (node :wrapper-fn :fn [:entry-0] {} nil)
                   (node :wrapper-op :reference [] {:binding-id :wrapper-binding} nil)
                   (node :wrapper-argument :literal [] {:literal-kind :string} nil)
                   (node :wrapper-call :call [:wrapper-op :wrapper-argument] {} nil)
                   (node :independent-value :literal [] {:literal-kind :string} owner)
                   (node :independent-parameter :reference [] {:binding-id :span-binding} owner)]
        core (-> core
                 (update :nodes into additions)
                 (assoc :lexical-bindings [{:binding-id :alias :initializer-node-id :self-call}])
                 (update :definitions into [{:binding-id :result-definition :value-node-id :entry-1}
                                            {:binding-id :wrapper-binding :value-node-id :wrapper-fn}])
                 (update :function-records conj {:function-syntax-id :wrapper :function-core-node-id :wrapper-fn
                                                 :body-core-node-id :entry-0})
                 (update :calls conj {:core-node-id :wrapper-call :operator-node-id :wrapper-op
                                      :argument-node-ids [:wrapper-argument]})
                 (update :call-edges conj {:call-core-node-id :wrapper-call :callee-function-syntax-id :wrapper}))
        derive (fn [c mode]
                 (invoke-c7 'sh08-is-cold-cut
                            [c record (into {} (map (juxt :node-id identity) (:nodes c))) mode]))
        entries (derive core :entries)
        body (derive core :body)
        excluded #(set (:excluded-node-ids %))]
    (is (= :accepted (:status entries) (:status body)))
    (doseq [result [entries body]]
      (is (every? (excluded result) [:helper-fn :helper-def :self-op :self-call
                                     :entry-0 :entry-1 :entry-operator-0 :entry-operator-1
                                     :alias-read :definition-read :dependent-do :wrapper-call])))
    (doseq [result [entries body]]
      (is (not-any? (excluded result) [:input-0 :input-1 :wrapper-fn :wrapper-op
                                       :wrapper-argument :genuine-throw :genuine-recur])))
    (is (every? (excluded entries) [:self-arg :input :present-get :recursive-body
                                    :independent-value :independent-parameter]))
    (is (not-any? (excluded body) [:self-arg :input :present-get :independent-value :independent-parameter]))
    (is (contains? (excluded body) :recursive-body))
    (doseq [mode [:entries :body]
            :let [result (derive core mode)]]
      (is (= (excluded result) (excluded (derive (update core :nodes #(vec (reverse %))) mode)))))
    (is (= [:rejected :private-self-cold-mode-required]
           ((juxt :status :reason) (derive core :unknown))))))

(deftest current-gravity-private-self-cold-inference-development-kernel
  (let [base (recursive-carrier)
        sequence (->> (recursive-inference-nodes base)
                      (mapv (fn [n]
                              (cond
                                (= :recursive-external-literal-node (:node-id n))
                                (assoc n :core-form :reference :attributes {:binding-id :caller-parameter}
                                         :evaluation {:owner-function-syntax-id :caller})
                                (= :recursive-base-node (:node-id n))
                                (assoc n :attributes {:literal-kind :string})
                                :else n))))
        caller {:function-syntax-id :caller :function-core-node-id :caller-fn
                :body-core-node-id :recursive-external-call-node :parameter-binding-ids [:caller-parameter]
                :fixed-arity 1 :definition-binding-id :caller-binding}
        main-call {:core-node-id :main-call :operator-node-id :main-op :operator-binding-id :caller-binding
                   :argument-node-ids [:main-argument] :ordered-evaluation-node-ids [:main-op :main-argument]
                   :evaluation-order :operator-then-arguments :result-policy :call-result}
        core (-> base
                 (assoc :nodes (into sequence [(node :caller-fn :fn [:recursive-external-call-node] {} nil)
                                               (node :main-op :reference [] {:binding-id :caller-binding} nil)
                                               (node :main-argument :literal [] {:literal-kind :string} nil)
                                               (node :main-call :call [:main-op :main-argument] {} nil)]))
                 (update :function-records conj caller)
                 (update :definitions conj {:binding-id :caller-binding :value-node-id :caller-fn})
                 (update :bindings into [{:binding-id :caller-parameter} {:binding-id :caller-binding}])
                 (update :calls #(conj (mapv (fn [c] (assoc c :operator-node-id (first (:ordered-evaluation-node-ids c)))) %) main-call))
                 (assoc-in [:call-edges 1 :caller-function-syntax-id] :caller)
                 (assoc-in [:call-edges 1 :caller-function-core-node-id] :caller-fn)
                 (update :call-edges conj {:ordinal 2 :call-core-node-id :main-call :caller-function-syntax-id nil
                                          :callee-function-syntax-id :caller :callee-function-core-node-id :caller-fn
                                          :classification :local-function :argument-core-node-ids [:main-argument]}))
        record (first (:function-records core))
        derive (fn [c mode params]
                 (invoke-c7 'sh08-is-cold-infer
                            [c c record (into {} (map (juxt :node-id identity) (:nodes c))) (:bindings c) [] {} mode params]))
        entries (derive core :entries [])
        body (derive core :body [:gravity.type/string])
        types #(get-in % [:inference :type-table])]
    (is (= :accepted (:status entries) (:status body)))
    (is (= :gravity.type/string (get (types entries) :recursive-external-literal-node)))
    (is (= :gravity.type/string (get-in entries [:inference :binding-types :caller-parameter])))
    (is (= :gravity.type/string (get-in entries [:inference :function-types :caller :parameters 0 :type])))
    (is (= :gravity.type/unknown (get-in entries [:inference :function-types :caller :return])))
    (is (= [:main-call] (:source-argument-call-node-ids entries)))
    (is (not-any? #(contains? (types entries) %) [:main-call :recursive-call-node :recursive-body-node
                                                 :recursive-external-call-node :recursive-base-node :recursive-parameter-node]))
    (is (= :gravity.type/string (get (types body) :recursive-parameter-node)))
    (is (= :gravity.type/string (get (types body) :recursive-base-node)))
    (is (not (contains? (types body) :recursive-call-node)))
    (is (not (contains? (get-in body [:inference :function-types]) :recursive-function-syntax)))
    (let [changed (-> core
                      (assoc-in [:calls 2 :argument-node-ids] [:recursive-external-call-node])
                      (assoc-in [:calls 2 :ordered-evaluation-node-ids] [:main-op :recursive-external-call-node])
                      (update :nodes #(mapv (fn [n] (if (= :main-call (:node-id n))
                                                     (assoc n :children [:main-op :recursive-external-call-node]) n)) %)))
          result (derive changed :entries [:gravity.type/bool])]
      (is (= :accepted (:status result)))
      (is (= :gravity.type/unknown (get-in result [:inference :binding-types :caller-parameter])))
      (is (= :gravity.type/unknown (get (types result) :recursive-external-literal-node))))
    (doseq [[mode params result] [[:entries [] entries] [:body [:gravity.type/string] body]]]
      (let [exact (derive core mode params)]
        (is (= (select-keys result [:status :mode :cut :source-argument-call-node-ids])
               (select-keys exact [:status :mode :cut :source-argument-call-node-ids])))
        (is (= (select-keys (:inference result) [:type-table :binding-types :function-types :constraints :diagnostics
                                                :convergence-status])
               (select-keys (:inference exact) [:type-table :binding-types :function-types :constraints :diagnostics
                                               :convergence-status])))
        ))))

(deftest current-gravity-private-self-return-witness-development-kernel
  (let [nodes {:body (node :body :if [:guard :self-call :base] {} :self)
               :guard (node :guard :literal [] {:literal-kind :boolean} :self)
               :self-call (node :self-call :call [] {} :self)
               :base (node :base :reference [] {:binding-id :parameter} :self)
               :throw (node :throw :throw [:self-call] {} :self)
               :recur (node :recur :recur [:self-call] {} :self)}
        record {:body-core-node-id :body}
        types {:guard :gravity.type/bool :base :gravity.type/string
               :throw :gravity.type/never :recur :gravity.type/never}
        derive (fn [ns ts] (invoke-c7 'sh08-is-return-witness [record ns ts]))
        reason (fn [ns ts] (:reason (derive ns ts)))
        result (derive nodes types)]
    (is (= :accepted (:status result)))
    (is (= :gravity.type/string (:return-type result)))
    (is (= [:self-call] (:pending-return-node-ids result)))
    (is (= [:base] (mapv :node-id (:witnesses result))))
    (is (= [:body :base] (get-in result [:witnesses 0 :path])))
    (is (= :independent-normal-condition (get-in result [:witnesses 0 :control-records 0 :rule])))
    (is (= :private-self-independent-return-required (reason nodes (dissoc types :guard))))
    (is (= :private-self-independent-return-required (reason nodes (assoc types :guard :gravity.type/never))))
    (is (= :private-self-independent-return-required
           (reason (assoc-in nodes [:guard :attributes :value] true) types)))
    (let [selected (derive (assoc-in nodes [:guard :attributes :value] false) types)]
      (is (= :accepted (:status selected)))
      (is (= [] (:pending-return-node-ids selected)))
      (is (= [2] (get-in selected [:witnesses 0 :control-records 0 :selected-branch-indexes]))))
    (is (= :private-self-return-type-conflict (reason nodes (assoc types :self-call :gravity.type/bool))))
    (is (= :private-self-independent-return-required (reason nodes (dissoc types :base))))
    (is (= :private-self-independent-return-required
           (reason (assoc-in nodes [:body :children] [:guard :throw :recur]) types)))
    (doseq [kind [:do :let]]
      (let [sequence (-> nodes (assoc-in [:body :core-form] kind)
                              (assoc-in [:body :children] [:self-call :base]))]
        (is (= :private-self-independent-return-required (reason sequence types)))
        (is (= :private-self-independent-return-required (reason sequence (assoc types :self-call :gravity.type/never))))
        (is (= :accepted (:status (derive sequence (assoc types :self-call :gravity.type/string)))))))
    (doseq [type [:gravity.type/integer :gravity.type/list
                 {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]}]]
      (is (= :private-self-return-value-type-required (reason nodes (assoc types :base type)))))
    (is (= :private-self-return-source-required (reason (dissoc nodes :base) types)))
    (let [id #(if (zero? %) :body (keyword (str "return-path-" %)))
          path (conj (mapv id (range 130)) :tail)
          chain (into {:tail (node :tail :literal [] {:literal-kind :string} :self)}
                      (for [i (range 130)]
                        [(id i) (node (id i) :do [(if (= i 129) :tail (id (inc i)))] {} :self)]))
          result (derive chain {:tail :gravity.type/string})]
      (is (= :accepted (:status result)))
      (is (= path (get-in result [:witnesses 0 :path])))
      (is (= 130 (count (get-in result [:witnesses 0 :control-records]))))
      (is (= :private-self-return-source-required
             (reason (assoc-in chain [:tail :node-id] :wrong) {:tail :gravity.type/string}))))))

(deftest current-gravity-private-self-cold-quote-shape-development-kernel
  ;; Keep the quoted-data shape/authentication control without asserting any
  ;; caller-selected work or round allowance.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [quote-input ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'quoted-data-input)
                     (vec (repeat 24 "payload")))
        base (recursive-carrier)
        core (assoc base :nodes (conj (recursive-inference-nodes base) (:node quote-input)))
        record (first (:function-records core))
        derive (fn [forms]
                 (invoke-c7 'sh08-is-cold-infer
                            [core core record (into {} (map (juxt :node-id identity) (:nodes core)))
                             (:bindings core) forms {} :entries []]))
        full (derive (:source-forms quote-input))
        malformed (mapv #(if (= (:owner quote-input) (:form-id %)) (assoc % :kind :vector) %)
                        (:source-forms quote-input))
        rejected (derive malformed)]
    (is (= :accepted (:status full)))
    (is (= :gravity.type/vector
           (get-in full [:inference :type-table (get-in quote-input [:node :node-id]) :kind])))
    (is (= :private-self-quoted-data-required (:reason rejected)))
    (is (= [:malformed-quote-owner-payload-join]
           (mapv :reason (get-in rejected [:quoted-products :diagnostics]))))
    (is (not (contains? rejected :inference)))))

(deftest current-gravity-private-self-cold-declared-result-development-kernel
  (let [base (recursive-carrier)
        string :gravity.type/string
        wrapper {:function-syntax-id :wrapper :function-core-node-id :wrapper-fn
                 :body-core-node-id :wrapper-value :parameter-binding-ids [:wrapper-param]
                 :fixed-arity 1 :definition-binding-id :wrapper-binding
                 :signature {:parameters [{:type-id string}] :return {:type-id string}}}
        call (fn [id op argument]
               {:core-node-id id :operator-node-id op :operator-binding-id :wrapper-binding
                :argument-node-ids [argument] :ordered-evaluation-node-ids [op argument]
                :evaluation-order :operator-then-arguments})
        inner (call :inner :inner-op :missing)
        outer (call :outer :outer-op :inner)
        edge (fn [id argument ordinal]
               {:call-core-node-id id :callee-function-syntax-id :wrapper :callee-function-core-node-id :wrapper-fn
                :classification :local-function :argument-core-node-ids [argument] :ordinal ordinal})
        core (-> base
                 (assoc :nodes (into (recursive-inference-nodes base)
                                    [(node :wrapper-value :literal [] {:literal-kind :string} :wrapper)
                                     (node :wrapper-fn :fn [:wrapper-value] {} nil)
                                     (node :inner-op :reference [] {:binding-id :wrapper-binding} nil)
                                     (node :missing :reference [] {:binding-id :missing-binding} nil)
                                     (node :inner :call [:inner-op :missing] {} nil)
                                     (node :outer-op :reference [] {:binding-id :wrapper-binding} nil)
                                     (node :outer :call [:outer-op :inner] {} nil)
                                     (node :alias-read :reference [] {:binding-id :alias-binding} nil)]))
                 (update :function-records conj wrapper)
                 (update :definitions conj {:binding-id :wrapper-binding :value-node-id :wrapper-fn})
                 (assoc :lexical-bindings [{:binding-id :alias-binding :initializer-node-id :outer}])
                 (update :calls into [inner outer])
                 (update :call-edges into [(edge :inner :missing 2) (edge :outer :inner 3)]))
        record (first (:function-records core))
        derive (fn [c]
                 (invoke-c7 'sh08-is-cold-infer
                            [c c record (into {} (map (juxt :node-id identity) (:nodes c))) (:bindings c) [] {} :entries []]))
        result (derive core)
        dependent (derive (assoc-in core [:function-records 1 :body-core-node-id] :recursive-external-call-node))]
    (is (= :accepted (:status result)))
    (is (= string (get-in result [:inference :function-types :wrapper :return])))
    (doseq [id [:missing :inner :outer :alias-read]]
      (is (= :gravity.type/unknown (get-in result [:inference :type-table id]))))
    (is (= :accepted (:status dependent)))
    (is (= string (get-in dependent [:inference :function-types :wrapper :return])))
    (is (every? (set (get-in dependent [:cut :excluded-node-ids])) [:inner :outer :alias-read]))
    (is (not-any? #(contains? (get-in dependent [:inference :type-table]) %) [:inner :outer :alias-read]))))

(def ^:private private-self-string-proof-data kernel-support/private-self-string-proof-data)

(deftest current-gravity-private-self-proof-development-kernel
  (let [{:keys [core record bindings forms]} (private-self-string-proof-data)
        derive (fn [c]
                 (let [nodes (into {} (map (juxt :node-id identity) (:nodes c)))]
                   (invoke-c7 'sh08-is-proof
                              [{:nodes (:nodes c) :analysis-core c :analysis-source-forms forms :analysis-imports {}}
                               (assoc c :node-table nodes) bindings (:module c)])))
        result (derive core)]
    (is (= :accepted (:status result)))
    (is (= :sh08-private-self-value-v1 (:rule-version result)))
    (is (= [:gravity.type/string] (:parameter-types result)))
    (is (= :gravity.type/string (:inferred-return-type result)))
    (is (= 2 (get-in result [:parameter-value-premise :entry-values :normal-entry-count])))
    (is (= [:entry-0 :entry-1 :self-call] (:incoming-call-node-ids result)))
    (is (= [:independent-base] (mapv :node-id (get-in result [:return-witness :witnesses]))))
    (is (not (contains? record :signature)))
    (is (not (contains? (:function-record result) :signature)))
    (is (not (contains? result :base-type)))
    (is (not (contains? result :recursive-base-type)))
    (is (= :private-self-source-census-or-capture (:reason (derive (assoc-in core [:module :exports] ['without-path])))))
    (let [conflict (update core :nodes #(mapv (fn [n] (if (= :input-1 (:node-id n))
                                                       (assoc n :attributes {:literal-kind :boolean :value false}) n)) %))]
      (is (= :private-self-entry-type-conflict (:reason (derive conflict)))))
    (let [circular (update core :nodes #(mapv (fn [n] (if (= :recursive-body (:node-id n))
                                                       (assoc n :core-form :do :children [:self-call]) n)) %))]
      (is (= :private-self-independent-return-required (:reason (derive circular)))))
    (let [aborting (update core :nodes #(mapv (fn [n] (if (contains? #{:input-0 :input-1} (:node-id n))
                                                       (assoc n :core-form :throw :children []) n)) %))]
      ;; Acceptance and the independent entry/return controls above are the
      ;; semantic contract; no synthetic prior-attempt or exhaustion allowance
      ;; is part of the proof.
      (is (= :private-self-normal-entry-required (:reason (derive aborting)))))))

(deftest current-gravity-private-self-escaped-population-development-kernel
  (let [{:keys [core record bindings forms]} (private-self-census-data)
        extra (mapv #(keyword (str "escaped-self-" %)) (range 64))
        changed (-> core
                    (update :nodes into (mapv (fn [id] (node id :reference [] {:binding-id :helper-binding} nil)) extra))
                    (update :reference-uses into (mapv #(hash-map :core-node-id % :binding-id :helper-binding :position :expression) extra))
                    (update-in [:preserved-resolution :resolution-table] into
                               (mapv #(hash-map :reference-syntax-id % :binding-id :helper-binding :position :expression) extra)))]
    (is (= 4 (count (:calls changed))))
    (is (= 67 (count (:reference-uses changed))))
    (is (nil? (invoke-c7 'sh08-is-private-census
                         [record changed (into {} (map (juxt :node-id identity) (:nodes changed))) bindings forms])))))

(def ^:private private-self-homogeneous-head-data kernel-support/private-self-homogeneous-head-data)

(deftest current-gravity-private-self-homogeneous-head-development-kernel
  (let [{:keys [core bindings forms head type]} (private-self-homogeneous-head-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        proof (invoke-c7 'sh08-is-proof
                         [{:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
                          (assoc core :node-table nodes) bindings (:module core)])
        premise (:parameter-value-premise proof)
        working (assoc core :sh08-private-self-parameter-premise premise)
        record (first (:function-records core))
        cold (invoke-c7 'sh08-is-cold-infer [working core record nodes bindings forms {} :body [type]])
        table (get-in cold [:inference :type-table])
        call (:call head) n (nodes :call)
        edge (first (filter #(= :call (:call-core-node-id %)) (:call-edges core)))
        derive (fn [c ts]
                 (invoke-c7 'sh08-lq-head-proof [c n call nodes (into {} (map (juxt :binding-id identity) bindings)) ts]))
        result (derive working table)]
    (is (= :accepted (:status proof))
        (pr-str (assoc (select-keys proof [:status :reason])
                       :inference (select-keys (:inference proof) [:convergence-status :diagnostics]))))
    (is (= :accepted (:status cold)))
    (is (= [type] (:parameter-types proof)))
    (is (= type (:inferred-return-type proof)))
    (is (= :accepted (:status result)))
    (is (= :nonempty (get-in result [:proof :knowledge])))
    (is (= :gravity.type/string (get-in result [:proof :result-type])))
    (is (= :gravity.type/private-self-entry-parameter (get-in result [:proof :immutable-binding-record :kind])))
    (is (= premise (get-in result [:proof :immutable-binding-record :parameter-value-premise])))
    (is (= :list-head-branch-proof-required (:reason (derive core table))))
    (is (= :list-head-branch-proof-required (:reason (derive (assoc working :sh08-private-self-parameter-premise true) table))))
    (doseq [changed [(assoc-in working [:sh08-private-self-parameter-premise :entry-values :normal-entry-count] 0)
                     (assoc-in working [:sh08-private-self-parameter-premise :source-census :function-record :body-core-node-id] :other)
                     (assoc-in working [:sh08-private-self-parameter-premise :entry-values :parameter-types 0] :gravity.type/list)
                     (assoc working :mutations [{:target-binding-id :span-binding}])]]
      (is (= :list-head-branch-proof-required (:reason (derive changed table)))))
    (is (not= :accepted (:status (derive working (assoc table :predicate-receiver :gravity.type/list)))))
    (is (true? (invoke-c7 'sh08-lq-inferred-call-valid? [working n call edge nodes (into {} (map (juxt :binding-id identity) bindings)) table])))
    (is (false? (invoke-c7 'sh08-lq-inferred-call-valid? [core n call edge nodes (into {} (map (juxt :binding-id identity) bindings)) table])))))

(def ^:private indexed-private-self-data kernel-support/indexed-private-self-data)

(defn- indexed-private-cold-wrapper-data []
  ;; Canonical-shaped normalized inputs. Entry facts come through an unannotated
  ;; acyclic wrapper; the predicate result never supplies the parameter types.
  (let [{:keys [core record bindings forms]} (indexed-private-self-data)
        wrapper-id :caller-0 wrapper-params [:wrapper-values :wrapper-needle]
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain [] :generated-origin nil})
        make (fn [id kind children attrs owner]
               {:node-id id :core-form kind :children children :attributes attrs :source (source id)
                :resolved-binding-ids (if (= kind :reference) [(:binding-id attrs)] [])
                :evaluation {:kind :left-to-right :owner-function-syntax-id owner
                             :order (mapv #(hash-map :index %1 :core-node-id %2) (range) children)}})
        form (fn [id kind value parent children]
               {:form-id id :syntax-id id :source-span nil :origin-chain [] :generated-origin nil
                :kind kind :value value :parent-form-id parent :child-form-ids children})
        ref (fn [id bid name] (make id :reference [] {:symbol name :position :expression :binding-id bid
                                                    :binding-class :lexical :definition-syntax-id nil} wrapper-id))
        binding-map (into {} (map (juxt :binding-id identity) bindings))
        vp-binding (assoc (binding-map '=) :binding-id :vp-binding :name 'vector? :definition-syntax-id :vp-definition)
        wrapper-binding {:binding-id :wrapper-binding :name 'cold-wrapper :kind :function :binding-class :namespace
                         :namespace (get-in core [:module :namespace]) :visibility :private
                         :definition-syntax-id :wrapper-name :upstream-binding-id :wrapper-upstream}
        bindings (into bindings [vp-binding wrapper-binding
                                 {:binding-id :wrapper-values :name 'values :kind :local :binding-class :lexical}
                                 {:binding-id :wrapper-needle :name 'value :kind :local :binding-class :lexical}])
        old-nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        vp-op (assoc (make :wrapper-vp-op :reference [] {:symbol 'vector? :position :operator :binding-id :vp-binding
                                                       :binding-class :core :definition-syntax-id :vp-definition} wrapper-id)
                     :resolved-binding-ids [:vp-binding])
        vp-call {:core-node-id :wrapper-vp-call :operator-node-id :wrapper-vp-op :operator-binding-id :vp-binding
                 :argument-node-ids [:wrapper-vp-ref] :ordered-evaluation-node-ids [:wrapper-vp-op :wrapper-vp-ref]
                 :evaluation-order :operator-then-arguments :result-policy :call-result}
        vp-node (-> (make :wrapper-vp-call :call (:ordered-evaluation-node-ids vp-call)
                          {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1]
                           :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call} wrapper-id)
                    (assoc :resolved-binding-ids [:vp-binding]) (assoc-in [:evaluation :kind] :operator-then-arguments))
        wrapper-if (-> (make :wrapper-if :if [:wrapper-vp-call :entry-0 :wrapper-false]
                            (:attributes (old-nodes :recursive-body)) wrapper-id)
                       (assoc-in [:evaluation :kind] :condition-then-exclusive-branch)
                       (assoc-in [:evaluation :order] [{:index 0 :core-node-id :wrapper-vp-call}]))
        wrapper-record {:function-syntax-id wrapper-id :function-form-id wrapper-id :function-core-node-id wrapper-id
                        :body-core-node-id :wrapper-if :parameter-binding-ids wrapper-params :fixed-arity 2
                        :definition-kind :named-top-level :definition-binding-id :wrapper-binding
                        :definition-core-node-id :wrapper-def :definition-name 'cold-wrapper}
        wrapper-fn (-> (old-nodes :helper-fn) (assoc :node-id wrapper-id :source (source wrapper-id)
                                                  :children [:wrapper-if] :resolved-binding-ids wrapper-params))
        wrapper-def (-> (old-nodes :helper-def)
                        (assoc :node-id :wrapper-def :source (source :wrapper-def) :children [wrapper-id]
                               :attributes {:name 'cold-wrapper :name-syntax-id :wrapper-name :visibility :private}
                               :resolved-binding-ids [:wrapper-binding]))
        retained (filterv #(or (= (:function-syntax-id record) (get-in % [:evaluation :owner-function-syntax-id]))
                                (contains? #{:helper-fn :helper-def} (:node-id %))) (:nodes core))
        entry0 (mapv (fn [n] (case (:node-id n)
                              :input-0 (ref :input-0 :wrapper-values 'values)
                              :ix-needle-0 (ref :ix-needle-0 :wrapper-needle 'value)
                              n))
                     (filter #(and (= wrapper-id (get-in % [:evaluation :owner-function-syntax-id]))
                                   (not (str/starts-with? (name (:node-id %)) "ix-member-0-"))) (:nodes core)))
        entry1 (mapv (fn [n] (case (:node-id n)
                              :entry-operator-1 (-> n (assoc :resolved-binding-ids [:wrapper-binding])
                                                   (assoc-in [:attributes :symbol] 'cold-wrapper)
                                                   (assoc-in [:attributes :binding-id] :wrapper-binding)
                                                   (assoc-in [:attributes :definition-syntax-id] :wrapper-name))
                              :entry-1 (-> n (assoc :children [:entry-operator-1 :input-1 :ix-needle-1]
                                                    :resolved-binding-ids [:wrapper-binding])
                                           (assoc-in [:attributes :argument-count] 2)
                                           (assoc-in [:attributes :argument-child-indexes] [1 2])
                                           (assoc-in [:evaluation :order]
                                                     [{:index 0 :core-node-id :entry-operator-1} {:index 1 :core-node-id :input-1}
                                                      {:index 2 :core-node-id :ix-needle-1}]))
                              n))
                     (filter #(and (= :caller-1 (get-in % [:evaluation :owner-function-syntax-id]))
                                   (not= :ix-zero-1 (:node-id %))) (:nodes core)))
        nodes (vec (concat retained [vp-op (ref :wrapper-vp-ref :wrapper-values 'values) vp-node] entry0
                           [(make :wrapper-false :literal [] {:literal-kind :boolean :value false} wrapper-id)
                            wrapper-if wrapper-fn wrapper-def] entry1))
        calls (vec (concat (take 6 (:calls core)) [vp-call (nth (:calls core) 6)
                                                  (assoc (nth (:calls core) 7) :operator-binding-id :wrapper-binding
                                                         :argument-node-ids [:input-1 :ix-needle-1]
                                                         :ordered-evaluation-node-ids [:entry-operator-1 :input-1 :ix-needle-1])]))
        vp-edge {:ordinal 6 :call-core-node-id :wrapper-vp-call :caller-function-syntax-id wrapper-id
                 :caller-function-core-node-id wrapper-id :callee-binding-id :vp-binding :callee-definition-syntax-id :vp-definition
                 :callee-function-syntax-id nil :callee-function-core-node-id nil :argument-core-node-ids [:wrapper-vp-ref]
                 :ordered-evaluation-node-ids [:wrapper-vp-op :wrapper-vp-ref]
                 :evaluation-order :operator-then-arguments :classification :nonlocal-or-nonfunction}
        edges (vec (concat (take 6 (:call-edges core))
                           [vp-edge (assoc (nth (:call-edges core) 6) :ordinal 7)
                            (assoc (nth (:call-edges core) 7) :ordinal 8 :callee-binding-id :wrapper-binding
                                   :callee-definition-syntax-id :wrapper-name :callee-function-syntax-id wrapper-id
                                   :callee-function-core-node-id wrapper-id :argument-core-node-ids [:input-1 :ix-needle-1]
                                   :ordered-evaluation-node-ids [:entry-operator-1 :input-1 :ix-needle-1])]))
        core (-> core (assoc :nodes nodes :calls calls :call-edges edges)
                 (assoc-in [:function-records 1] wrapper-record)
                 (update :definitions conj {:binding-id :wrapper-binding :name 'cold-wrapper :core-node-id :wrapper-def
                                            :value-node-id wrapper-id :syntax-id :wrapper-def})
                 (assoc-in [:preserved-resolution :binding-table] bindings)
                 (update :reference-uses #(mapv (fn [r] (if (= :entry-operator-1 (:core-node-id r))
                                                       (assoc r :binding-id :wrapper-binding :symbol 'cold-wrapper :definition-syntax-id :wrapper-name) r)) %))
                 (update-in [:preserved-resolution :resolution-table]
                            #(mapv (fn [r] (if (= :entry-operator-1 (:reference-syntax-id r))
                                             (assoc r :binding-id :wrapper-binding :symbol 'cold-wrapper :upstream-binding-id :wrapper-upstream) r)) %))
                 (update :control-flow conj {:core-node-id :wrapper-if :kind :if :condition-node-id :wrapper-vp-call
                                            :branches [{:role :then :predicate :truthy :core-node-id :entry-0}
                                                       {:role :else :predicate :falsey :core-node-id :wrapper-false}]
                                            :branch-exclusivity :exactly-one :truthiness {:false-values [:nil :false] :other-values :true}
                                            :result-policy :selected-branch}))
        forms (into {} (map (juxt :form-id identity) forms))
        forms (apply dissoc forms (concat (map #(keyword (str "ix-member-0-" %)) (range 7)) [:ix-zero-1]))
        forms (merge forms {:input-0 (form :input-0 :symbol 'values :entry-0 [])
                            :ix-needle-0 (form :ix-needle-0 :symbol 'value :entry-0 [])
                            :wrapper-vp-op (form :wrapper-vp-op :symbol 'vector? :wrapper-vp-call [])
                            :wrapper-vp-ref (form :wrapper-vp-ref :symbol 'values :wrapper-vp-call [])
                            :wrapper-vp-call (form :wrapper-vp-call :list nil :wrapper-if [:wrapper-vp-op :wrapper-vp-ref])
                            :wrapper-false (form :wrapper-false :boolean false :wrapper-if [])
                            :wrapper-if (form :wrapper-if :list nil wrapper-id [:wrapper-if-op :wrapper-vp-call :entry-0 :wrapper-false])
                            :wrapper-if-op (form :wrapper-if-op :symbol 'if :wrapper-if [])
                            wrapper-id (form wrapper-id :list nil :wrapper-def [:wrapper-fn-op :wrapper-params :wrapper-if])
                            :wrapper-fn-op (form :wrapper-fn-op :symbol 'fn wrapper-id [])
                            :wrapper-params (form :wrapper-params :vector nil wrapper-id [])
                            :wrapper-def (form :wrapper-def :list nil nil [:wrapper-def-op :wrapper-name wrapper-id])
                            :wrapper-def-op (form :wrapper-def-op :symbol 'def :wrapper-def [])
                            :wrapper-name (form :wrapper-name :symbol 'cold-wrapper :wrapper-def [])})
        forms (-> forms (assoc-in [:entry-0 :parent-form-id] :wrapper-if)
                        (assoc-in [:entry-operator-1 :value] 'cold-wrapper)
                        (assoc-in [:entry-1 :child-form-ids] [:entry-operator-1 :input-1 :ix-needle-1]))]
    {:core core :record record :bindings bindings :forms (vec (vals forms))}))

(deftest current-gravity-indexed-private-cold-wrapper-development-kernel
  (let [{:keys [core record bindings forms]} (indexed-private-cold-wrapper-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        cold (invoke-c7 'sh08-is-cold-infer [core (assoc core :node-table nodes) record nodes bindings forms {} :entries []])
        types (get-in cold [:inference :type-table])]
    (is (= :accepted (:status cold)) (pr-str (select-keys cold [:status :reason :diagnostics])))
    (when-not (= :accepted (:status cold))
      (throw (ex-info "Unannotated cold wrapper prerequisite rejected" (select-keys cold [:status :reason]))))
    (is (= {:kind :gravity.type/vector :shape :literal :element-types (vec (repeat 7 :gravity.type/keyword))}
           (get types :input-0)))
    (is (= :gravity.type/keyword (get types :ix-needle-0)))
    (is (= :gravity.type/integer (get types :ix-zero-0)))
    (is (= :gravity.type/bool (get types :wrapper-vp-call)))
    (is (not (contains? (second (:function-records core)) :signature)))
    (let [proof (:recursive-proof
                 (invoke-c7 'sh08-ft-proof-results
                            [{:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
                             (assoc core :node-table nodes) bindings (:module core)]))]
      (is (= :accepted (:status proof)) (pr-str (select-keys proof [:status :reason])))
      (is (= :sh08-indexed-private-self-value-v1 (:rule-version proof)))
      (is (= 1 (get-in proof [:parameter-value-premise :entry-values :normal-entry-count]))))))

(deftest current-gravity-indexed-private-entry-source-development-kernel
  (let [{:keys [core record nodes bindings forms]} (indexed-private-self-data)
        binding-index (into {} (map (juxt :binding-id identity) bindings))
        form-index (into {} (map (juxt :form-id identity) forms))
        census (invoke-c7 'sh08-is-private-census [record core nodes binding-index form-index])
        cold (invoke-c7 'sh08-is-cold-infer [core core record nodes bindings forms {} :entries []])
        entries (invoke-c7 'sh08-ix-entry-values [census nodes (get-in cold [:inference :type-table]) form-index])
        derived (invoke-c7 'sh08-ix-source [core census entries nodes binding-index form-index])]
    (is (map? census))
    (is (= :accepted (:status cold)) (pr-str (:inference cold)))
    (is (= :accepted (:status entries)) (pr-str entries))
    (is (= 2 (:normal-entry-count entries)))
    (is (= :accepted (:status derived)) (pr-str (select-keys derived [:status :reason])))
    (is (= :ordinary-function-entry-induction (get-in derived [:proof :scope])))
    (is (= :gravity.type/bool (get-in derived [:return-witness :return-type])))
    (is (= [:ix-false] (mapv :node-id (get-in derived [:return-witness :witnesses]))))
    (doseq [changed [(assoc-in nodes [:ix-zero-0 :attributes :value] 1)
                     (assoc-in nodes [:ix-zero-1 :attributes :value] 1)]]
      (is (= :indexed-self-zero-source-entry-required
             (:reason (invoke-c7 'sh08-ix-entry-values [census changed (get-in cold [:inference :type-table]) form-index])))))
    (is (= :indexed-self-normal-entry-required
           (:reason (invoke-c7 'sh08-ix-entry-values
                              [(-> census (update :calls #(vec (take 1 %))) (update :edges #(vec (take 1 %))))
                              nodes {} form-index]))))))

(deftest current-gravity-indexed-private-proof-development-kernel
  (let [{:keys [core record nodes bindings forms]} (indexed-private-self-data)
        shape {:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
        products (assoc core :node-table nodes)
        derive #(invoke-c7 'sh08-ft-proof-results [shape products bindings (:module core)])
        proof (:recursive-proof (derive))
        proof-input (when (= :accepted (:status proof)) (invoke-c7 'sh08-ft-recursive-proof-input [proof]))
        rows (when (= :accepted (:status proof)) (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof]))]
    (is (= :accepted (:status proof)) (pr-str (select-keys proof [:status :reason])))
    (when (= :accepted (:status proof))
      (is (= :sh08-indexed-private-self-value-v1 (:rule-version proof)))
      (is (= :gravity.type/bool (:inferred-return-type proof)))
      (is (= :gravity/sh08-indexed-private-self-entry-premise-v1 (get-in proof [:parameter-value-premise :domain])))
      (is (= :ordinary-function-entry-induction (get-in proof [:parameter-value-premise :source-induction :scope])))
      (is (= [:ix-false] (mapv :node-id (get-in proof [:return-witness :witnesses]))))
      (is (false? (invoke-c7 'sh08-ds-type? [:gravity.type/integer])))
      (is (false? (invoke-c7 'sh08-ds-type? [(first (:parameter-types proof))])))
      (is (= :gravity/sh08-indexed-private-self-value-proof-v1
             (:domain proof-input)))
      (is (= :sh08-indexed-private-self-value-v1
             (:producer-rule (first rows)))))
    (let [binding-index (into {} (map (juxt :binding-id identity) bindings))
          form-index (into {} (map (juxt :form-id identity) forms))]
      (doseq [changed [(assoc-in core [:module :exports] ['without-path])
                       (assoc-in core [:var-references] [{:binding-id :helper-binding}])
                       (update core :reference-uses pop)]]
        (is (nil? (invoke-c7 'sh08-is-private-census
                             [record changed nodes binding-index form-index])))))))

(deftest current-gravity-indexed-private-rejection-development-kernel
  (let [{:keys [core bindings forms]} (indexed-private-self-data)
        prove (fn [c f]
                (let [nodes (into {} (map (juxt :node-id identity) (:nodes c)))]
                  (invoke-c7 'sh08-is-proof
                             [{:nodes (:nodes c) :analysis-core c :analysis-source-forms f :analysis-imports {}}
                              (assoc c :node-table nodes) bindings (:module c)])))
        change-node (fn [c id f] (update c :nodes #(mapv (fn [n] (if (= id (:node-id n)) (f n) n)) %)))
        change-form (fn [f id update] (mapv #(if (= id (:form-id %)) (update %) %) f))
        change-ref (fn [id bid symbol]
                     [(change-node core id #(-> % (assoc-in [:attributes :binding-id] bid)
                                                (assoc-in [:attributes :symbol] symbol)
                                                (assoc :resolved-binding-ids [bid])))
                      (change-form forms id #(assoc % :value symbol))])
        baseline (prove core forms)
        wrong-unit [(change-node core :ix-unit #(assoc-in % [:attributes :value] 2))
                    (change-form forms :ix-unit #(assoc % :value 2))]
        nonzero [(change-node core :ix-zero-1 #(assoc-in % [:attributes :value] 1))
                 (change-form forms :ix-zero-1 #(assoc % :value 1))]
        conflicting [(change-node core :ix-member-1-0 #(assoc % :attributes {:literal-kind :boolean :value false}))
                     (change-form forms :ix-member-1-0 #(assoc % :kind :boolean :value false))]]
    (is (= :accepted (:status baseline)) (pr-str (select-keys baseline [:status :reason])))
    (when-not (= :accepted (:status baseline))
      (throw (ex-info "Indexed rejection baseline prerequisite failed" (select-keys baseline [:status :reason]))))
    (doseq [[changed changed-forms] [(change-ref :ix-self-vector :needle-binding 'value)
                                     (change-ref :ix-self-value :span-binding 'values)
                                     (change-ref :ix-guard-index :needle-binding 'value)
                                     (change-ref :ix-count-vector :needle-binding 'value)
                                     wrong-unit nonzero conflicting
                                     [core (change-form forms :ix-unit #(assoc % :kind :keyword))]
                                     [(change-node core :ix-step #(assoc-in % [:evaluation :order] [])) forms]
                                     [(assoc-in core [:module :exports] ['without-path]) forms]
                                     [(assoc core :var-references [{:binding-id :helper-binding}]) forms]
                                     [(update core :reference-uses pop) forms]]]
      (let [result (prove changed changed-forms)]
        (is (= :rejected (:status result)) (pr-str (select-keys result [:status :reason])))))
    (is (= :indexed-self-source-induction-required (:reason (prove (first wrong-unit) (second wrong-unit)))))
    (is (= :indexed-self-zero-source-entry-required (:reason (prove (first nonzero) (second nonzero)))))
    ))

(def ^:private private-self-three-parameter-false-base-data kernel-support/private-self-three-parameter-false-base-data)

(deftest current-gravity-private-three-parameter-false-base-development-kernel
  (let [{:keys [core bindings forms]} (private-self-three-parameter-false-base-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        proof (:recursive-proof
               (invoke-c7 'sh08-ft-proof-results
                          [{:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
                           (assoc core :node-table nodes) bindings (:module core)]))
        projected (when (= :accepted (:status proof)) (invoke-c7 'sh08-ft-recursive-proof-input [proof]))]
    (is (= :accepted (:status proof)) (pr-str (select-keys proof [:status :reason])))
    (when-not (= :accepted (:status proof))
      (throw (ex-info "Prior three-parameter private prerequisite failed" (select-keys proof [:status :reason]))))
    (is (= :sh08-private-self-value-v1 (:rule-version proof)))
    (is (= [:gravity.type/bool :gravity.type/string :gravity.type/keyword] (:parameter-types proof)))
    (is (= :gravity.type/bool (:inferred-return-type proof)))
    (is (= :gravity/sh08-private-self-value-proof-v1 (:domain projected)))
    (is (= :gravity/sh08-private-self-entry-parameter-premise-v1 (get-in projected [:parameter-value-premise :domain])))
    (is (= [:independent-base] (mapv :node-id (get-in proof [:return-witness :witnesses]))))))

(deftest current-gravity-private-self-entry-values-development-kernel
  (let [list-type {:kind :gravity.type/list :shape :homogeneous :element-type :gravity.type/string}
        calls [{:core-node-id :self-call :argument-node-ids [:self-pending :self-text]}
               {:core-node-id :entry-a :argument-node-ids [:pending-a :text-a]}
               {:core-node-id :entry-b :argument-node-ids [:pending-b :text-b]}
               {:core-node-id :aborting :argument-node-ids [:transfer :text-c]}]
        edges (mapv (fn [ordinal call]
                      {:ordinal ordinal :call-core-node-id (:core-node-id call)
                       :caller-function-syntax-id (if (zero? ordinal) :self (keyword (str "caller-" ordinal)))
                       :callee-function-syntax-id :self :classification :local-function})
                    (range) calls)
        census {:function-record {:function-syntax-id :self :fixed-arity 2} :calls calls :edges edges}
        nodes (into {} (map (fn [call] [(:core-node-id call) {:source {:syntax-id (:core-node-id call)}}]) calls))
        types {:pending-a list-type :text-a :gravity.type/string :pending-b list-type :text-b :gravity.type/string
               :transfer :gravity.type/never :text-c :gravity.type/string
               :self-pending :gravity.type/unknown :self-text :gravity.type/unknown}
        derive #(invoke-c7 'sh08-is-entry-values [%1 nodes %2])
        reason (fn [c t] (:reason (derive c t)))
        result (derive census types)]
    (is (= :accepted (:status result)))
    (is (= [list-type :gravity.type/string] (:parameter-types result)))
    (is (= 2 (:normal-entry-count result)))
    (is (= [:entry-a :entry-b :aborting] (mapv :call-core-node-id (:entries result))))
    (is (= [true true false] (mapv :normal-invocation? (:entries result))))
    (is (every? :callee-result-is-not-invocation-guard (:entries result)))
    (is (= [:pending-a :text-a] (get-in result [:entries 0 :argument-node-ids])))
    (is (= (:source (nodes :entry-a)) (get-in result [:entries 0 :source])))
    (is (= result (derive census (assoc types :entry-a :gravity.type/never :entry-b :gravity.type/never))))
    (is (= result (derive census (assoc types :self-pending :gravity.type/bool :self-text :gravity.type/bool))))
    (is (= :private-self-entry-type-conflict
           (reason census (assoc types :pending-b (assoc list-type :element-type :gravity.type/keyword)))))
    (doseq [changed [(assoc types :pending-a :gravity.type/unknown)
                     (assoc types :pending-a :gravity.type/list)
                     (assoc types :pending-a {:kind :gravity.type/list :shape :literal :element-types [:gravity.type/string]})
                     (assoc types :text-b :gravity.type/integer)]]
      (is (= :private-self-independent-entry-types-required (reason census changed))))
    (doseq [indexes [[0] [0 3] []]]
      (is (= :private-self-normal-entry-required
             (reason (assoc census :calls (mapv calls indexes) :edges (mapv edges indexes)) types))))
    (doseq [changed [(assoc-in census [:calls 1 :argument-node-ids] [])
                     (assoc-in census [:edges 1 :callee-function-syntax-id] :other)
                     (assoc census :edges [])
                     (assoc census :calls 7)]]
      (is (= :rejected (:status (derive changed types)))))))

(deftest current-gravity-private-self-entry-diagnostic-source-development-kernel
  ;; Normalized source-rich DATA exercises the original owning algorithms; it
  ;; is not a fresh C5/C6 authentication or whole-source composition claim.
  (let [module {:namespace 'private.entry.diagnostics :profile :meta :target :jvm}
        calls [{:core-node-id :entry-a :argument-node-ids [:arg-a]}
               {:core-node-id :entry-b :argument-node-ids [:arg-b]}
               {:core-node-id :self-call :argument-node-ids [:self-arg]}]
        edges (mapv (fn [i call]
                      {:ordinal i :call-core-node-id (:core-node-id call)
                       :caller-function-syntax-id (if (= i 2) :self :main)
                       :callee-function-syntax-id :self :classification :local-function})
                    (range) calls)
        census {:function-record {:function-syntax-id :self :fixed-arity 1}
                :calls calls :edges edges}
        nodes (into {}
                    (map-indexed
                     (fn [i id]
                       [id (assoc (node id :call [(keyword (str "op-" i)) (keyword (str "arg-" i))] {} :main)
                                  :resolved-binding-ids [:helper-binding]
                                  :source {:form-id (keyword (str "form-" i))
                                           :syntax-id (keyword (str "syntax-" i))
                                           :semantic-span {:source "private-entry-diagnostics.gravity"
                                                           :line-start (+ 10 i) :column-start 3
                                                           :byte-start (* 20 i) :byte-end (+ 17 (* 20 i))}
                                           :origin-chain [{:input (keyword (str "input-" i))}]
                                           :generated-origin [{:generated-by (keyword (str "macro-" i))}]})])
                     [:entry-a :entry-b]))
        types {:arg-a :gravity.type/string :arg-b :gravity.type/string :self-arg :gravity.type/unknown}
        derive #(invoke-c7 'sh08-is-entry-values [census nodes %1])
        render #(invoke-c7 'sh08-ft-recursive-diagnostic [% module])
        accepted (derive types)
        source-fields [:core-node-id :syntax-id :source-span :binding-id :origin-chain :generated-origin]
        expectation :bounded-named-self-recursive-fixed-arity-positive-positional-literal-base]
    (is (= :accepted (:status accepted)))
    (is (= [:gravity.type/string] (:parameter-types accepted)))
    (is (= 2 (:normal-entry-count accepted)))
    (is (= [:entry-a :entry-b] (mapv :call-core-node-id (:entries accepted))))
    (is (not (contains? accepted :node)))
    (doseq [[changed reason id ordinal]
            [[(assoc types :arg-b :gravity.type/unknown) :private-self-independent-entry-types-required :entry-b 1]
             [(assoc types :arg-b :gravity.type/bool) :private-self-entry-type-conflict :entry-b 1]
             [(assoc types :arg-a :gravity.type/unknown :arg-b :gravity.type/unknown)
              :private-self-independent-entry-types-required :entry-a 0]
             [(assoc types :arg-a :gravity.type/never :arg-b :gravity.type/unknown)
              :private-self-independent-entry-types-required :entry-b 1]]]
      (let [result (derive changed) selected (nodes id) source (:source selected)
            diagnostic (render result)
            expected-source {:core-node-id id :syntax-id (:syntax-id source)
                             :source-span (:semantic-span source) :binding-id :helper-binding
                             :origin-chain (:origin-chain source) :generated-origin (:generated-origin source)}]
        (is (= [:rejected reason] [(:status result) (:reason result)]))
        (is (= id (get-in result [:entry :call-core-node-id])))
        (is (= (calls ordinal) (get-in result [:entry :source-call])))
        (is (= (edges ordinal) (get-in result [:entry :source-edge])))
        (is (= source (get-in result [:entry :source])))
        ;; These are the demonstrated missing fields on the unchanged base.
        (is (= selected (:node result)))
        (is (= expected-source (select-keys diagnostic source-fields)))
        (is (= (str "SH08-C7:C7-ANNOTATION:" id ":" reason) (:diagnostic-id diagnostic)))
        (is (= ["C7-ANNOTATION" reason expectation reason :meta :jvm]
               ((juxt :rule :reason :expected-type :actual-type :profile :target) diagnostic)))
        (is (= expected-source (select-keys (:diagnostic-id-request diagnostic) source-fields)))
        (is (= (invoke-c7 'sh08-ft-diagnostic ["C7-ANNOTATION" selected module expectation reason reason {}])
               diagnostic))))
    (let [transfer (derive (assoc types :arg-a :gravity.type/never))]
      (is (= :accepted (:status transfer)))
      (is (= 1 (:normal-entry-count transfer)))
      (is (= [false true] (mapv :normal-invocation? (:entries transfer)))))
    ;; An old source-less rejection still has its original formatter behavior.
    (let [legacy {:status :rejected :reason :recursive-argument-shape}]
      (is (= (invoke-c7 'sh08-ft-diagnostic
                       ["C7-ANNOTATION" nil module expectation :recursive-argument-shape :recursive-argument-shape {}])
             (render legacy))))))

(deftest current-gravity-private-self-value-projections-development-kernel
  (let [{:keys [core bindings forms]} (private-self-string-proof-data)
        nodes (into {} (map (juxt :node-id identity) (:nodes core)))
        proof (:recursive-proof (invoke-c7 'sh08-ft-proof-results
                                            [{:nodes (:nodes core) :analysis-core core :analysis-source-forms forms :analysis-imports {}}
                                             (assoc core :node-table nodes) bindings (:module core)]))
        record (first (:function-records core))
        skeleton (invoke-c7 'sh08-ft-function-type-skeleton [record (:module core)])
        completed (invoke-c7 'sh08-ft-recursive-complete-function [skeleton proof])
        seeds (invoke-c7 'sh08-ft-inference-context [true false proof {} {(:function-syntax-id record) completed}])
        projected (invoke-c7 'sh08-ft-recursive-proof-input [proof])
        rows (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])
        roles (invoke-c7 'sh08-ft-recursive-call-proof-facts
                         [proof (mapv #(hash-map :call-core-node-id %) (:incoming-call-node-ids proof))])]
    (is (= :accepted (:status proof)))
    (is (= :gravity/sh08-private-self-value-proof-v1 (:domain projected)))
    (is (= :closed-private-monomorphic-normal-entries-and-return-witness (:value-inference projected)))
    (is (= [:gravity.type/string] (mapv :type (:parameters completed))))
    (is (= :gravity.type/string (:return completed)))
    (is (= [:pending-sh09] (:thrown-error-effects completed)))
    (is (not (contains? completed :signature)))
    (is (not (contains? completed :declaration-rule-version)))
    (is (= :gravity.type/string (:span-binding seeds)))
    (is (= completed (:helper-binding seeds)))
    (is (= :gravity/sh08-private-self-value-constraint-v1 (get-in rows [0 :constraint-id-request :domain])))
    (is (= :sh08-private-self-value-v1 (get-in rows [0 :producer-rule])))
    (is (= :gravity.type/string (get-in rows [0 :rhs])))
    (is (= [:incoming-private-entry-call :incoming-private-entry-call :self-recursive-call] (mapv :role roles)))
    (is (= (:parameter-value-premise proof) (:parameter-value-premise projected)))
    (is (= (:return-witness proof) (:return-witness projected)))
    (is (not-any? #(contains? projected %) [:base-type :base-node-id :recursive-call-core-node-id :declared-return-type]))
    (is (some #{:independent-complete-normal-entry-value-inference}
              (invoke-c7 'sh08-ft-local-verification-checks [{:scope :private-inferred-monomorphic-named-self-value}])))))

(deftest current-gravity-declared-self-value-development-kernel
  (let [base (-> (recursive-carrier)
                (assoc-in [:function-records 0 :signature]
                          {:parameters [{:type-id :gravity.type/string}]
                           :return {:type-id :gravity.type/string}})
                (assoc-in [:nodes 6 :attributes :literal-kind] :string)
                (assoc-in [:nodes 8 :attributes :literal-kind] :string)
                (update :nodes conj
                        (node :transformed-self-argument :literal []
                              {:literal-kind :string} :recursive-function-syntax))
                (assoc-in [:nodes 5 :children]
                          [:recursive-operator-node :transformed-self-argument])
                (assoc-in [:calls 0 :argument-node-ids] [:transformed-self-argument])
                (assoc-in [:calls 0 :ordered-evaluation-node-ids]
                          [:recursive-operator-node :transformed-self-argument])
                (assoc-in [:call-edges 0 :argument-core-node-ids] [:transformed-self-argument])
                (assoc-in [:call-edges 0 :ordered-evaluation-node-ids]
                          [:recursive-operator-node :transformed-self-argument]))
        prove (fn [carrier]
                (invoke-c7 'sh08-ds-proof
                           [{:nodes (:nodes carrier)} carrier (:bindings carrier) (:module carrier)]))
        proof (prove base)
        record (first (:function-records base))
        skeleton (invoke-c7 'sh08-ft-function-type-skeleton [record (:module base)])
        completed (invoke-c7 'sh08-ft-recursive-complete-function [skeleton proof])
        infer (fn [carrier]
                (let [nodes (into {} (map (juxt :node-id identity) (:nodes carrier)))
                      ordered (recursive-inference-nodes carrier)]
                  (invoke-c7 'sh08-ft-infer-acyclic-with-context
                             [ordered nodes [] (:definitions carrier)
                              (:calls carrier) (:call-edges carrier)
                              {(:function-id skeleton) skeleton}
                              {:recursive-definition-binding skeleton
                               :recursive-parameter-binding :gravity.type/string}
                              proof])))
        inferred (infer base)
        facts (invoke-c7 'sh08-ft-call-facts
                         [(:calls base) (:call-edges base)
                          (into {} (map (juxt :node-id identity) (:nodes base)))
                          (:type-table inferred) (:function-types inferred) (:module base)])
        artifact (invoke-c7 'sh08-ft-recursive-result-fields
                            [{:status :accepted} proof facts])]
    (is (= :accepted (:status proof)))
    (is (= :sh08-declared-self-value-v1 (:rule-version proof)))
    (is (not (contains? proof :recursive-call-core-node-id)))
    (is (not (contains? proof :recursive-base-type)))
    (is (= skeleton completed))
    (is (= [:pending-sh09] (:thrown-error-effects completed)))
    (is (= #{} (:latent-effects completed)))
    (is (= [:recursive-call-node :recursive-external-call-node]
           (:incoming-call-node-ids proof)))
    (is (= (set (map :node-id (filter #(= :recursive-function-syntax
                                        (get-in % [:evaluation :owner-function-syntax-id]))
                                    (:nodes base))))
           (set (:owned-node-ids proof))))
    (is (= :converged (:convergence-status inferred)))
    (is (= [] (:diagnostics inferred)))
    (is (= :gravity.type/string (get-in inferred [:type-table :recursive-call-node])))
    (is (= :gravity.type/string (get-in inferred [:type-table :recursive-body-node])))
    (is (= :gravity/sh08-declared-self-value-proof-v1
           (get-in artifact [:recursive-proof :domain])))
    (is (= :pending-sh09-recursive-body
           (get-in artifact [:recursive-proof :effect-error-status])))
    (is (= [:self-recursive-call :incoming-declared-call]
           (mapv :role (:recursive-call-facts artifact))))
    (is (= facts (mapv #(dissoc % :role) (:recursive-call-facts artifact))))
    (is (= :sh08-declared-self-value-v1
           (:producer-rule (first (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])))))
    (is (= :gravity.type/string
           (invoke-c7 'sh08-ft-if-join-with-context
                      [[:condition :left :right]
                       {:left :gravity.type/never :right :gravity.type/string} proof])))
    (is (= :gravity.type/unknown
           (invoke-c7 'sh08-ft-if-join-with-context [[:condition :left :right] {} proof])))
    (doseq [changed [(assoc-in base [:function-records 0 :signature :return] nil)
                     (assoc-in base [:function-records 0 :signature :parameters 0] nil)
                     (assoc-in base [:function-records 0 :signature :return :type-id] :gravity.type/unknown)
                     (assoc-in base [:recursion-components 0 :kind] :mutually-recursive)
                     (assoc-in base [:call-edges 0 :ordinal] 9)
                     (assoc-in base [:calls 0 :argument-node-ids] [])
                     (assoc-in base [:nodes 4 :attributes :binding-id] :arbitrary-global)
                     (assoc-in base [:nodes 1 :core-form] :try)]]
      (is (= :rejected (:status (prove changed)))))
    (let [bad-argument (infer (assoc-in base [:nodes 10 :attributes :literal-kind] :boolean))
          bad-body (infer (assoc-in base [:nodes 6 :attributes :literal-kind] :boolean))
          bad-entry (infer (assoc-in base [:nodes 8 :attributes :literal-kind] :boolean))]
      (is (seq (:diagnostics bad-argument)))
      (is (= :gravity.type/unknown (get-in bad-body [:type-table :recursive-body-node])))
      (is (seq (:diagnostics bad-entry)))
      (is (= :gravity.type/bool (get-in bad-argument [:type-table :transformed-self-argument])))
      (is (= :gravity.type/bool (get-in bad-body [:type-table :recursive-base-node])))
      (is (= :gravity.type/bool (get-in bad-entry [:type-table :recursive-external-literal-node]))))))
(deftest sh08-recursive-source-reachability-and-structure
  (let [artifact-function
        (get-in @c7-plan [:functions 'sh08-ft-function-type-core-artifact-with-imports])
        recursive-function
        (get-in @c7-plan [:functions 'sh08-ft-recursive-proof])
        calls
        (filter
         #(and (map? %)
               (= :function-call (:op %)))
         (tree-seq coll? seq artifact-function))
        inference-call
        (first
         (filter
          #(= 'sh08-ft-infer-acyclic-with-context (:function %))
          calls))
        proof-results-call
        (first
         (filter
          #(= 'sh08-ft-proof-results (:function %))
          calls))
        context-proof-function
        (get-in @c7-plan [:functions 'sh08-ft-context-proof])
        context-proof-branch
        (first (:instructions context-proof-function))
        instructions (:instructions artifact-function)
        top-level (first instructions)
        invalid-lets
        (filter #(and (map? %)
                      (= :let (:op %))
                      (not= 1 (count (:body %))))
                (tree-seq coll? seq artifact-function))]
    (is (= 1 (count instructions)))
    (is (= :if (:op top-level)))
    (is (empty? invalid-lets))
    (is (map? recursive-function))
    (is (map? (get-in @c7-plan [:functions
                                'sh08-ft-proof-results])))
    (is (map? (get-in @c7-plan [:functions
                                'sh08-ft-recursive-complete-function])))
    (is (map? (get-in @c7-plan [:functions
                                'sh08-ft-recursive-call-proof-facts])))
    (is (map? inference-call))
    (is (map? proof-results-call))
    (is (some #(and (map? %) (= :function-call (:op %))
                    (= 'sh08-ft-context-proof (:function %)))
              (tree-seq coll? seq (get-in inference-call [:args 9]))))
    (is (= :if (:op context-proof-branch)))
    (is (= :local (get-in context-proof-branch [:then :op])))
    (is (= 'recursive-proof
           (get-in context-proof-branch [:then :name]))))
  (doseq [name ["sh08-ft-recursive-proof"
                "sh08-ft-recursive-pending"
                "sh08-ft-recursive-constraint-ledger"
                "sh08-ft-recursive-result-fields"]]
    (is (.contains
         (slurp (str (.resolve @root c7-relative-path))) name)
        name)))

(deftest sh08-recursive-fixture-pair-is-byte-identical
  (let [gravity
        (slurp (str (.resolve @root
                              (str fixture-root
                                   "/accepted/function-self-recursive-type.gravity"))))
        qst
        (slurp (str (.resolve @root
                              (str fixture-root
                                   "/accepted/function-self-recursive-type.qst"))))]
    (is (pos? (count gravity)))
    (is (= gravity qst))))

(deftest sh08-recursive-pure-positive-and-monotone-fixed-point
  (let [carrier (recursive-carrier)
        proof (recursive-proof carrier)
        skeleton
        (invoke-c7 'sh08-ft-function-type-skeleton
                   [(first (:function-records carrier)) (:module carrier)])
        complete
        (invoke-c7 'sh08-ft-recursive-complete-function
                   [skeleton proof])
        node-table (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
        inference
        (invoke-c7
         'sh08-ft-infer-acyclic-with-context
         [(recursive-inference-nodes carrier) node-table [] (:definitions carrier)
          (:calls carrier) (:edges carrier)
          {(:function-id complete) complete}
          {(get-in proof [:definition :binding-id]) complete}
          {}])
        permuted (assoc carrier :nodes (vec (reverse (:nodes carrier))))
        permuted-proof (recursive-proof permuted)
        permuted-skeleton
        (invoke-c7 'sh08-ft-function-type-skeleton
                   [(first (:function-records permuted)) (:module permuted)])
        permuted-complete
        (invoke-c7 'sh08-ft-recursive-complete-function
                   [permuted-skeleton permuted-proof])
        permuted-node-table
        (into {} (map (fn [n] [(:node-id n) n]) (:nodes permuted)))
        permuted-inference
        (invoke-c7
         'sh08-ft-infer-acyclic-with-context
         [(recursive-inference-nodes permuted) permuted-node-table [] (:definitions permuted)
          (:calls permuted) (:edges permuted)
          {(:function-id permuted-complete) permuted-complete}
          {(get-in permuted-proof [:definition :binding-id])
           permuted-complete}
          {}])
        canonical-call-facts
        (invoke-c7
         'sh08-ft-call-facts
         [(:calls carrier) (:edges carrier) node-table
          (:type-table inference)
          {(:function-id complete) complete}
          (:module carrier)])
        ledger
        (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])
        recursive-fields
        (invoke-c7 'sh08-ft-recursive-result-fields
                   [{:status :accepted} proof canonical-call-facts])]
    (is (= :accepted (:status proof)))
    (is (= :gravity.type/integer (:base-type proof)))
    (is (= [:gravity.type/integer] (:parameter-types proof)))
    (is (= :gravity.type/integer (get-in complete [:parameters 0 :type])))
    (is (= :gravity.type/integer (:return complete)))
    (is (= :pending-sh10 (:ownership-constraints complete)))
    (is (= [:pending-sh09] (:thrown-error-effects complete)))
    (is (= 1 (count ledger)))
    (is (= :recursive-fixed-point (get-in ledger [0 :kind])))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1
           (get-in recursive-fields [:recursive-proof :domain])))
    (is (= :direct-named-self-recursion
           (get-in recursive-fields [:recursive-call-facts 0 :dispatch])))
    (is (= :recursive-call-node
           (get-in recursive-fields [:recursive-call-facts 0 :syntax-id])))
    (is (= :meta
           (get-in recursive-fields [:recursive-call-facts 0 :profile])))
    (is (= :external-parameter-evidence
           (get-in recursive-fields [:recursive-call-facts 1 :role])))
    (is (= :recursive-external-call-node
           (get-in recursive-fields [:recursive-call-facts 1 :syntax-id])))
    (let [proof-input (invoke-c7 'sh08-ft-recursive-proof-input [proof])]
      (is (= (get (:edges carrier) 0)
             (:recursive-edge proof-input)))
      (is (= (get (:edges carrier) 1)
             (get-in proof-input [:parameter-evidence-call :edge])))
      (is (not= (:recursive-edge proof-input)
                (get-in proof-input [:parameter-evidence-call :edge])))
      (is (= :recursive-definition-binding
             (:recursive-definition-binding-id proof-input))))
    (is (not (contains?
              (set (invoke-c7 'sh08-ft-recursive-pending
                              [[:recursive-type-annotations :records]]))
              :recursive-type-annotations)))
    (is (= :gravity.type/unknown
           (invoke-c7 'sh08-ft-if-join-with-context
                      [[:unrelated-condition :unrelated-then
                        :unrelated-else]
                       {} proof])))
    (is (= :converged (:convergence-status inference)))
    (is (= [] (:diagnostics inference)))
    (is (= :gravity.type/integer
           (get (:type-table inference) :recursive-call-node)))
    (is (= :gravity.type/integer
           (get (:type-table inference) :recursive-body-node)))
    (is (= :gravity.type/integer
           (get-in (:binding-types inference)
                   [:recursive-parameter-binding])))
    (is (= :accepted (:status permuted-proof)))
    (is (= :converged (:convergence-status permuted-inference)))
    (is (= [] (:diagnostics permuted-inference)))
    (is (= :gravity.type/integer
           (get (:type-table permuted-inference) :recursive-body-node)))
    (is (= (:type-table inference) (:type-table permuted-inference)))))

(deftest sh08-recursive-pure-hostile-matrix
  (doseq [[label overrides expected]
          [["mutual-scc"
            [[[:recursion-components 0 :kind] :mutually-recursive]]
            :mutual-recursion-unsupported]
           ["multiple-components"
            [[[:recursion-components]
              [{:kind :self-recursive
                :function-syntax-ids [:recursive-function-syntax]
                :internal-call-edge-ordinals [0]}
               {:kind :self-recursive
                :function-syntax-ids [:other-function-syntax]
                :internal-call-edge-ordinals [0]}]]]
            :multiple-recursive-components]
           ["zero-arity"
            [[[:function-records 0 :fixed-arity] 0]]
            :recursive-fixed-arity-required]
           ["no-base"
            [[[:nodes 6 :core-form] :reference]
             [[:nodes 6 :attributes :binding-id] :recursive-parameter-binding]]
            :recursive-base-evidence-required]
           ["edge-lineage"
            [[[:edges 0 :callee-definition-syntax-id] :wrong-definition-syntax]]
            :recursive-call-edge-lineage]
           ["order-tamper"
            [[[:calls 0 :evaluation-order] :arguments-then-operator]]
            :recursive-call-edge-lineage]
           ["argument-count"
            [[[:calls 0 :argument-node-ids]
              [:recursive-parameter-node :recursive-parameter-node]]
             [[:calls 0 :ordered-evaluation-node-ids]
              [:recursive-operator-node :recursive-parameter-node
               :recursive-parameter-node]]
             [[:edges 0 :argument-core-node-ids]
              [:recursive-parameter-node :recursive-parameter-node]]
             [[:edges 0 :ordered-evaluation-node-ids]
              [:recursive-operator-node :recursive-parameter-node
               :recursive-parameter-node]]]
            :recursive-argument-shape]
           ["transformed-argument"
            [[[:nodes 4 :core-form] :literal]
             [[:nodes 4 :attributes :literal-kind] :integer]]
            :recursive-argument-shape]
           ["capture"
            [[[:nodes 2 :core-form] :reference]
             [[:nodes 2 :attributes :binding-id] :captured-binding]]
            :recursive-capture-unsupported]
           ["argument-binding"
            [[[:nodes 4 :attributes :binding-id] :captured-binding]]
            :recursive-argument-shape]
           ["result-conflict"
            [[[:nodes 6 :attributes :literal-kind] :string]]
            :recursive-result-type-conflict]]]
    (let [proof (recursive-proof (recursive-carrier overrides))]
      (is (= :rejected (:status proof)) label)
      (is (= expected (:reason proof)) label))))

(deftest sh08-recursive-unsupported-external-primitive-keeps-evidence
  (let [proof
        (recursive-proof
         (recursive-carrier
          [[[:nodes 8 :attributes :literal-kind] :decimal]]))
        offending-node (:node proof)]
    (is (= :rejected (:status proof)))
    (is (= :unsupported-recursive-primitive-type (:reason proof)))
    (is (= :recursive-external-literal-node (:node-id offending-node)))
    (is (= :recursive-external-literal-node
           (get-in offending-node [:source :syntax-id])))
    (is (= {:line 1 :column 1 :length 1}
           (get-in offending-node [:source :semantic-span])))
    (is (= [] (get-in offending-node [:source :origin-chain])))
    (is (nil? (get-in offending-node [:source :generated-origin])))))

(deftest sh08-recursive-primitive-family-diagonal-and-conflicts
  (let [primitive-kinds
        [[:integer :gravity.type/integer]
         [:boolean :gravity.type/bool]
         [:string :gravity.type/string]]]
    (doseq [[literal-kind expected-type] primitive-kinds]
      (let [carrier
            (recursive-carrier
             [[[:nodes 6 :attributes :literal-kind] literal-kind]
              [[:nodes 8 :attributes :literal-kind] literal-kind]])
            proof (recursive-proof carrier)
            skeleton
            (invoke-c7 'sh08-ft-function-type-skeleton
                       [(first (:function-records carrier)) (:module carrier)])
            complete
            (invoke-c7 'sh08-ft-recursive-complete-function
                       [skeleton proof])
            node-table
            (into {} (map (fn [n] [(:node-id n) n]) (:nodes carrier)))
            inference
            (invoke-c7
             'sh08-ft-infer-acyclic-with-context
             [(recursive-inference-nodes carrier) node-table [] (:definitions carrier)
              (:calls carrier) (:edges carrier)
              {(:function-id complete) complete}
              {(get-in proof [:definition :binding-id]) complete}
              {}])
            call-facts
            (invoke-c7
             'sh08-ft-call-facts
             [(:calls carrier) (:edges carrier) node-table
              (:type-table inference)
              {(:function-id complete) complete}
              (:module carrier)])
            proof-input (invoke-c7 'sh08-ft-recursive-proof-input [proof])
            ledger (invoke-c7 'sh08-ft-recursive-constraint-ledger [proof])]
        (is (= :accepted (:status proof)) (str literal-kind))
        (is (= expected-type (:base-type proof)) (str literal-kind))
        (is (= [expected-type] (:parameter-types proof)) (str literal-kind))
        (is (= expected-type (get-in complete [:parameters 0 :type])))
        (is (= expected-type (:return complete)))
        (is (= :pending-sh10 (:ownership-constraints complete)))
        (is (= [:pending-sh09] (:thrown-error-effects complete)))
        (is (= :converged (:convergence-status inference)))
        (is (= [] (:diagnostics inference)))
        (is (= expected-type (get (:type-table inference)
                                  :recursive-call-node)))
        (is (= expected-type (get (:type-table inference)
                                  :recursive-body-node)))
        (is (= expected-type (get-in (:binding-types inference)
                                     [:recursive-parameter-binding])))
        (is (= expected-type (get-in call-facts [0 :result-type])))
        (is (= expected-type (get-in call-facts [1 :result-type])))
        (is (= (get (:edges carrier) 0)
               (:recursive-edge proof-input)))
        (is (= (get (:edges carrier) 1)
               (get-in proof-input [:parameter-evidence-call :edge])))
        (is (= :recursive-fixed-point (get-in ledger [0 :kind])))
        (is (= :meta (:recursive-call-profile proof-input)))
        (is (= :meta (get-in proof-input
                              [:parameter-evidence-call :profile])))))
    (doseq [[base-kind base-type]
            primitive-kinds
            [evidence-kind evidence-type]
            primitive-kinds
            :when (not= base-kind evidence-kind)]
      (let [proof
            (recursive-proof
             (recursive-carrier
              [[[:nodes 6 :attributes :literal-kind] base-kind]
               [[:nodes 8 :attributes :literal-kind] evidence-kind]]))]
        (is (= :rejected (:status proof)))
        (is (= :recursive-result-type-conflict (:reason proof)))
        (is (= base-type (:expected proof)))
        (is (= evidence-type (:actual proof)))))))

(deftest sh08-recursive-authenticated-gravity-boundary
  ;; One .gravity carrier is the only expensive check in this namespace.  The
  ;; paired .qst file is covered by a separate release/parity lane.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact (deref (ns-resolve namespace 'fixture-artifact))
        function-request (deref (ns-resolve namespace 'function-request))
        artifact (fixture-artifact "accepted" "function-self-recursive-type"
                                   ".gravity")
        request (function-request artifact)
        result
        (invoke-c7 'sh08-function-type-core-artifact
                   [request])
        proof (:recursive-proof result)
        function-id (:function-syntax-id proof)
        function-entry
        (some #(when (= function-id (:function-id %)) %)
              (:function-type-table result))
        recursive-call-id (:recursive-call-core-node-id proof)
        evidence-call-id (:parameter-evidence-call-core-node-id proof)
        source-edges (get-in request [:canonical-core-artifact :call-edges])
        source-recursive-edge
        (some #(when (= recursive-call-id (:call-core-node-id %)) %) source-edges)
        source-evidence-edge
        (some #(when (= evidence-call-id (:call-core-node-id %)) %) source-edges)
        recursive-facts (:recursive-call-facts result)
        recursive-constraint-index
        (first
         (keep-indexed
          (fn [index constraint]
            (when (= :recursive-fixed-point (:kind constraint)) index))
          (:constraint-ledger result)))
        expected-pending
        [:mutual-recursion :unbounded-recursion :recursive-captures
         :recursive-higher-order :recursive-polymorphism
         :higher-order-functions :records :unions :protocols :generics
         :casts :dynamic-boundaries]
        altered-candidates
        [(assoc-in result [:recursive-proof :proof-bound] :altered-bound)
         (assoc-in result [:recursive-call-facts 0 :dispatch]
                   :altered-dispatch)
         (assoc-in result
                   [:constraint-ledger recursive-constraint-index :status]
                   :rejected)
         (assoc-in result [:pending 0] :altered-pending)
         (assoc result :scope :first-order-fixed-arity-functions-locals-calls)
         (assoc-in result [:typed-core :recursive-proof :base-type]
                   :gravity.type/string)
         (assoc-in result [:identity-input :recursive-proof :base-type]
                   :gravity.type/string)]]
    (is (= :accepted (:status result)))
    (is (= :bounded-named-self-recursive-positive-fixed-arity-positional-literal-base
           (:scope result)))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1 (:domain proof)))
    (is (= :gravity.type/integer (:base-type proof)))
    (is (= :gravity.type/integer (:return function-entry)))
    (is (= :gravity.type/integer
           (get-in function-entry [:parameters 0 :type])))
    (is (= :pending-sh10 (:ownership-constraints function-entry)))
    (is (= [:pending-sh09] (:thrown-error-effects function-entry)))
    (is (= :direct-named-self-recursion
           (get-in result [:recursive-call-facts 0 :dispatch])))
    (is (= [:self-recursive-call :external-parameter-evidence]
           (mapv :role recursive-facts)))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-recursive-edge])
           (:recursive-edge proof)
           (get-in recursive-facts [0 :b47-edge])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-evidence-edge])
           (get-in proof [:parameter-evidence-call :edge])
           (get-in recursive-facts [1 :external-edge])))
    (is (= (:recursive-call-source proof)
           (select-keys (first recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (select-keys (:parameter-evidence-call proof)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])
           (select-keys (second recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (not= (:recursive-edge proof)
              (get-in proof [:parameter-evidence-call :edge])))
    (is (= :operator-then-arguments
           (get-in proof [:recursive-edge :evaluation-order])))
    (is (= (:definition-binding-id proof)
           (:recursive-definition-binding-id proof)
           (get-in recursive-facts [0 :recursive-definition-binding-id])))
    (is (= :converged (get-in result [:convergence :status])))
    (is (= expected-pending (:pending result)
           (get-in result [:identity-input :pending])))
    (is (= (:constraint-ledger result)
           (get-in result [:typed-core :constraints])
           (get-in result [:identity-input :constraint-ledger])))
    (is (some #(= :recursive-fixed-point (:kind %))
              (:constraint-ledger result)))
    (is (integer? recursive-constraint-index))
    (is (= proof
           (get-in result [:typed-core :recursive-proof])
           (get-in result [:identity-input :recursive-proof])))
    (is (= recursive-facts
           (get-in result [:typed-core :recursive-call-facts])))
    (is (= :passed
           (:status
            (invoke-c7 'sh08-verify-function-type-result
                       [request result]))))
    (doseq [candidate altered-candidates]
      (is (= :rejected
             (:status
              (invoke-c7 'sh08-verify-function-type-result
                         [request candidate])))))))

(deftest sh08-recursive-authenticated-string-gravity-boundary
  ;; Independently selectable authenticated boundary for the String diagonal.
  ;; The paired .qst is byte-parity only and is not built here.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [namespace 'gravity.self-hosting.sh08-function-call-type-test
        fixture-artifact (deref (ns-resolve namespace 'fixture-artifact))
        function-request (deref (ns-resolve namespace 'function-request))
        artifact (fixture-artifact "accepted"
                                   "function-self-recursive-string-type"
                                   ".gravity")
        request (function-request artifact)
        result (invoke-c7 'sh08-function-type-core-artifact [request])
        proof (:recursive-proof result)
        function-id (:function-syntax-id proof)
        function-entry
        (some #(when (= function-id (:function-id %)) %)
              (:function-type-table result))
        recursive-call-id (:recursive-call-core-node-id proof)
        evidence-call-id (:parameter-evidence-call-core-node-id proof)
        source-edges (get-in request [:canonical-core-artifact :call-edges])
        source-recursive-edge
        (some #(when (= recursive-call-id (:call-core-node-id %)) %)
              source-edges)
        source-evidence-edge
        (some #(when (= evidence-call-id (:call-core-node-id %)) %)
              source-edges)
        recursive-facts (:recursive-call-facts result)
        recursive-constraint-index
        (first
         (keep-indexed
          (fn [index constraint]
            (when (= :recursive-fixed-point (:kind constraint)) index))
          (:constraint-ledger result)))
        pending
        [:mutual-recursion :unbounded-recursion :recursive-captures
         :recursive-higher-order :recursive-polymorphism
         :higher-order-functions :records :unions :protocols :generics
         :casts :dynamic-boundaries]
        verification
        (invoke-c7 'sh08-verify-function-type-result [request result])
        altered-candidates
        [(assoc-in result [:function-type-table 0 :return]
                   :gravity.type/integer)
         (assoc-in result [:recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:identity-input :recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:typed-core :recursive-proof :base-type]
                   :gravity.type/integer)
         (assoc-in result [:recursive-call-facts 0 :dispatch]
                   :altered-dispatch)
         (assoc-in result [:constraint-ledger recursive-constraint-index :status]
                   :rejected)]]
    (is (= :accepted (:status result)))
    (is (= :bounded-named-self-recursive-positive-fixed-arity-positional-literal-base
           (:scope result)))
    (is (= :gravity/sh08-authoritative-recursive-proof-v1 (:domain proof)))
    (is (= :gravity.type/string (:base-type proof)))
    (is (= :gravity.type/string (:base-type
                                 (get-in result [:identity-input
                                                 :recursive-proof]))))
    (is (= :gravity.type/string (:return function-entry)))
    (is (= :gravity.type/string
           (get-in function-entry [:parameters 0 :type])))
    (is (= :pending-sh10 (:ownership-constraints function-entry)))
    (is (= [:pending-sh09] (:thrown-error-effects function-entry)))
    (is (= :gravity.type/string
           (get (:type-table result) recursive-call-id)))
    (is (= :gravity.type/string
           (get (:type-table result) evidence-call-id)))
    (is (= [:self-recursive-call :external-parameter-evidence]
           (mapv :role recursive-facts)))
    (is (= :direct-named-self-recursion
           (get-in recursive-facts [0 :dispatch])))
    (is (= :external-parameter-evidence
           (get-in recursive-facts [1 :role])))
    (is (= :gravity.type/string
           (get-in recursive-facts [0 :result-type])))
    (is (= :gravity.type/string
           (get-in recursive-facts [1 :result-type])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-recursive-edge])
           (:recursive-edge proof)
           (get-in recursive-facts [0 :b47-edge])))
    (is (= (invoke-c7 'sh08-ft-higher-order-edge-projection
                      [source-evidence-edge])
           (get-in proof [:parameter-evidence-call :edge])
           (get-in recursive-facts [1 :external-edge])))
    (is (= (:recursive-call-source proof)
           (select-keys (first recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (select-keys (:parameter-evidence-call proof)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])
           (select-keys (second recursive-facts)
                        [:syntax-id :source-span :origin-chain
                         :generated-origin])))
    (is (= (:definition-binding-id proof)
           (:recursive-definition-binding-id proof)
           (get-in recursive-facts [0 :recursive-definition-binding-id])))
    (is (= :operator-then-arguments
           (get-in proof [:recursive-edge :evaluation-order])))
    (is (= (:constraint-ledger result)
           (get-in result [:typed-core :constraints])
           (get-in result [:identity-input :constraint-ledger])))
    (is (= pending (:pending result)
           (get-in result [:identity-input :pending])))
    (is (= proof
           (get-in result [:typed-core :recursive-proof])
           (get-in result [:identity-input :recursive-proof])))
    (is (= recursive-facts
           (get-in result [:typed-core :recursive-call-facts])))
    (is (= :converged (get-in result [:convergence :status])))
    (is (= (get-in request [:canonical-core-artifact :provenance :source-path])
           (get-in result [:provenance :source-path])))
    (is (contains? (:provenance result) :actual-source-path))
    (is (= :passed (:status verification)))
    (doseq [candidate altered-candidates]
      (is (= :rejected
             (:status
              (invoke-c7 'sh08-verify-function-type-result
                         [request candidate])))))))
