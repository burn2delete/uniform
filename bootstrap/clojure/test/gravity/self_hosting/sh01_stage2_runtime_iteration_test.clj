(ns gravity.self-hosting.sh01-stage2-runtime-iteration-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh09-body-effect-test :as body-effects]))

(def ^:private runtime {:engine :stage2-runtime-iteration-test})
(def ^:private plan {:source {:path "stage2-runtime-iteration-test.gravity"}})
(def ^:private compiler-plan
  {:compiler-artifact-plan? true
   :kind :gravity/stage2-compiler-artifact-plan
   :module {:profile :meta}
   :compiler {:stage :p15-s23-stage2-expression-lowering}
   :source {:path "stage2-runtime-iteration-test.gravity"}})

(deftype ^:private ThrowingAssociative [failure]
  clojure.lang.Associative
  (containsKey [_ _] false)
  (entryAt [_ _] nil)
  (assoc [_ _ _] (throw failure))
  clojure.lang.ILookup
  (valAt [_ _] nil)
  (valAt [_ _ not-found] not-found)
  clojure.lang.IPersistentCollection
  (count [_] 0)
  (cons [_ _] (throw failure))
  (empty [_] {})
  (equiv [this other] (identical? this other))
  clojure.lang.Seqable
  (seq [_] nil))

(deftype ^:private HostilePersistentCallee [equiv-calls hash-calls failure]
  clojure.lang.IPersistentCollection
  (count [_] 0)
  (cons [_ _] (throw failure))
  (empty [_] (throw failure))
  (equiv [_ _]
    (swap! equiv-calls inc)
    (throw (AssertionError. "hostile persistent equiv invoked")))
  clojure.lang.Seqable
  (seq [_] (throw failure))
  Object
  (hashCode [_]
    (swap! hash-calls inc)
    (throw (AssertionError. "hostile persistent hash invoked"))))

(defn- literal-instructions
  [values]
  (mapv (fn [value] {:op :literal :value value}) values))

(defn- diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- interpreted-builtin
  [active-plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-instruction
   runtime active-plan {}
   {:op :builtin-call
    :function function
    :args (literal-instructions arguments)}))

(defn- module-private [name] (ns-resolve 'gravity.bootstrap name))
(defn- module-fixture [name extension]
  (loop [directory (.getParentFile (io/file (.toURI (io/resource "gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj"))))]
    (if (.isFile (io/file directory "deps.edn"))
      (.getPath (io/file directory (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/" name extension)))
      (recur (.getParentFile directory)))))

(defn- seed-throw-test-plan [forms]
  (let [path (module-fixture "seed-library" ".gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        source (str "(ns example.seed.throw (:profile :meta) (:target :jvm) (:effects #{:error/throw})) " forms)]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path source)))
(defn- seed-throw-test-capture [operation]
  (try (operation) nil (catch Throwable failure failure)))
(defn- seed-throw-test-transport? [failure]
  (and (instance? clojure.lang.ExceptionInfo failure)
       (identical? (var-get (module-private 'p15-s23-stage2-runtime-source-throw-token))
                   (:gravity.bootstrap/source-throw (ex-data failure)))))

(defn- typed-lowering-kernel-data
  ([] (typed-lowering-kernel-data false))
  ([throw?]
  ;; Normalized DATA checks the Gravity consumer joins, not C7 authentication.
  (let [source-path "in-memory-typed.gravity"
        ns-form '(ns example.typed (:profile :meta) (:target :jvm) (:effects #{}) (:capabilities #{}) (:safety :safe))
        body (if throw? '(throw problem) 'problem)
        body-id (if throw? :throw :reference) body-form-id (if throw? :throw-form :body-form)
        fn-form (list 'fn '[problem :- String] :- 'String body)
        definition-form (list 'def 'identity fn-form)
        expanded [definition-form] source-text (str/join "\n" (map pr-str (cons ns-form expanded)))
        declaration {:type-binding-id :string :type-id :gravity.type/string :type-symbol 'String :type-form-id :type-form :type-syntax-id :type}
        signature {:body-source-child-index 4 :parameter-source-child-indexes [0] :parameters [declaration] :return declaration}
        source (fn [form-id syntax-id] {:form-id form-id :syntax-id syntax-id})
        nodes [{:node-id :reference :core-form :reference :children [] :attributes {:binding-id :parameter} :source (source :body-form :body)}
               {:node-id :function :core-form :fn :children [body-id] :resolved-binding-ids [:parameter]
                :attributes {:parameters '[problem] :signature signature} :source (source :function-form :fn)}
               {:node-id :definition :core-form :def :children [:function] :source (source :definition-form :def)}]
        nodes (if throw? (conj nodes {:node-id :throw :core-form :throw :children [:reference] :source (source :throw-form :throw)}) nodes)
        forms [{:form-id :definition-form :syntax-id :def :value definition-form :child-form-ids [:def-op :name-form :function-form]}
               {:form-id :function-form :syntax-id :fn :value fn-form :child-form-ids [:fn-op :parameter-form :return-marker :type-form body-form-id]}
               {:form-id :parameter-form :syntax-id :parameters :value '[problem :- String] :child-form-ids [:parameter-name :parameter-marker :type-form]}
               {:form-id :parameter-name :syntax-id :parameter-name :value 'problem :scope-id :function-scope}
               {:form-id :type-form :syntax-id :type :value 'String}
               {:form-id :body-form :syntax-id :body :value 'problem}]
        forms (if throw? (conj forms {:form-id :throw-form :syntax-id :throw :value body :child-form-ids [:throw-operator :body-form]}) forms)
        bindings [{:binding-id :parameter :name 'problem :kind :local :binding-class :lexical
                   :definition-syntax-id :parameter-name :scope-id :function-scope}
                  {:binding-id :string :name 'String :kind :type :binding-class :core :namespace 'gravity.core}]
        module {:module 'example.typed :source-path source-path :forms expanded :profile :meta :target :jvm :safety :safe
                :effects #{} :capabilities #{} :exports []}
        core-module {:namespace 'example.typed :profile :meta :target :jvm :safety :safe :effects [] :capabilities [] :exports []}
        record {:definition-name 'identity :definition-kind :named-top-level :function-syntax-id :fn :function-core-node-id :function
                :definition-core-node-id :definition :body-core-node-id body-id :parameter-binding-ids [:parameter]
                :fixed-arity 1 :signature signature}
        function {:definition-name 'identity :function-id :fn :function-core-node-id :function :body-core-node-id body-id
                  :parameter-binding-ids [:parameter] :parameters [{:binding-id :parameter :type :gravity.type/string}]
                  :fixed-arity 1 :return :gravity.type/string :signature signature :thrown-error-effects (if throw? [:gravity.type/string] [])}
        core {:artifact-id :core :module core-module :nodes nodes :function-records [record]}
        reader {:source-unit-record {:path source-path :bytes-hash (str "sha256:" (bootstrap/sha256-hex source-text))}
                :token-stream [{:raw source-text}]}
        macro-artifact {:expanded-forms expanded :gravity-macro-boundary {:authenticated-sh04-artifact {:c2-reader-artifact reader}}}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:module core-module :forms forms :binding-table bindings}
                  :authenticated-wrapper {:sh06-resolution-artifact {:sh05-macro-artifact macro-artifact}}}}
        typed {:status :accepted :module core-module :sh07-shaped-artifact-id :core :function-type-table [function]
               :type-table (cond-> {:reference :gravity.type/string :function function :definition function} throw? (assoc :throw :gravity.type/never))}
        report {:artifact :gravity/sh08-function-type-verification :status :passed :expected typed :candidate typed}
        input {:request request :typed typed :verification report}
        observation {:verification report :source {:path source-path :text source-text :sha256 (get-in reader [:source-unit-record :bytes-hash])}
                     :compiler-sources {:c7 {:source {:sha256 :current-c7} :plan-id :c7-plan}
                                        :lowering {:source {:sha256 :current-lowering} :plan-id :lowering-plan}}}
        definition {:name 'identity :params '[problem] :body (list body) :definition-form :def-fn :arity 1 :body-form-count 1
                    :source-declarations {:parameters '[problem :- String] :return '[:- String]}}]
    {:module module :input input :observation observation :definition definition})))

(defn- typed-catch-kernel-context [context]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain nil :generated-origin nil})
        clause {:clause-ordinal 0 :handler-child-index 1 :catch-clause-form-id :catch :catch-clause-syntax-id :catch
                :error-type-form-id :catch-type :error-type-syntax-id :catch-type :error-type-binding-id :string
                :catch-binding-form-id :catch-name :catch-binding-syntax-id :catch-name :catch-binding-id :caught :catch-binding-scope-id :catch-scope}
        node {:node-id :try :core-form :try :children [:protected :handler] :source (source :try-form)
              :attributes {:handler-clauses [clause]}}
        record (assoc clause :core-node-id :try :form-id :try-form :protected-core-node-id :protected :handler-core-node-id :handler)
        premise {:version :sh08-atomic-typed-catch-v1 :source-node node :source-handlers [record]
                 :catch-types [:gravity.type/string] :normal-result-type :gravity.type/string}
        row {:producer-rule :sh08-atomic-typed-catch-v1 :status :solved :kind :equality
             :constraint-id-request {:domain :gravity/sh08-atomic-typed-catch-v1 :premise premise}
             :lhs {:core-node-id :try :projection :source-error-handler-result} :dependencies premise
             :solution {:normal-result-type :gravity.type/string}}]
    (-> context
        (assoc :locals {:parameter 'problem} :parameter-types {:parameter :gravity.type/string} :ledger [row] :error-handlers [record])
        (update :nodes merge {:try node :protected {:node-id :protected :core-form :throw :children [:reference] :source (source :protected-form)}
                              :handler {:node-id :handler :core-form :reference :children [] :attributes {:binding-id :caught} :source (source :handler-form)}})
        (update :types merge {:try :gravity.type/string :protected :gravity.type/never :handler :gravity.type/string})
        (assoc-in [:bindings :caught] {:binding-id :caught :name 'caught :kind :local :binding-class :lexical
                                      :definition-syntax-id :catch-name :scope-id :catch-scope})
        (update :forms merge {:try-form {:form-id :try-form :syntax-id :try-form :kind :list :child-form-ids [:try-op :protected-form :catch]}
                              :try-op {:form-id :try-op :value 'try}
                              :catch-op {:form-id :catch-op :value 'catch}
                              :protected-form {:form-id :protected-form :syntax-id :protected-form}
                              :catch {:form-id :catch :syntax-id :catch :parent-form-id :try-form :kind :list :value '(catch String caught caught)
                                      :child-form-ids [:catch-op :catch-type :catch-name :handler-form]}
                              :catch-type {:form-id :catch-type :syntax-id :catch-type :parent-form-id :catch :value 'String}
                              :catch-name {:form-id :catch-name :syntax-id :catch-name :parent-form-id :catch :value 'caught :scope-id :catch-scope}
                              :handler-form {:form-id :handler-form :syntax-id :handler-form :parent-form-id :catch :value 'caught}}))))

(defn- typed-symbol-quote-kernel-context [context]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain nil :generated-origin nil})
        attrs {:quoted-form-id :symbol-payload :quoted-syntax-id :symbol-payload :quoted-kind :symbol :quoted-value 'unresolved/data}
        node {:node-id :symbol-quote :core-form :quote :children [] :resolved-binding-ids [] :source (source :quote-form)
              :attributes attrs :evaluation {:kind :no-evaluation :order []}}
        data {:origin {:kind :quoted-data :quote-core-node-id :symbol-quote :form-id :symbol-payload :syntax-id :symbol-payload :data-path [] :child-form-ids []}
              :kind :symbol :value 'unresolved/data :type :gravity.type/symbol :source (source :symbol-payload) :children [] :shape-size 1}
        premise {:core-node-id :symbol-quote :source (:source node) :attributes attrs :children [] :resolved-binding-ids [] :evaluation (:evaluation node)}
        row {:producer-rule :sh08-recursive-quoted-data-v1 :status :solved :kind :equality :lhs :symbol-quote :rhs :gravity.type/symbol
             :constraint-id-request {:domain :gravity/sh08-recursive-quoted-data-constraint-v1 :quote premise :data data}
             :quoted-data data :dependencies {:quote premise :data data} :solution {:type :gravity.type/symbol :evaluation :no-evaluation}}]
    (-> context (assoc :ledger [row]) (assoc-in [:nodes :symbol-quote] node) (assoc-in [:types :symbol-quote] :gravity.type/symbol)
        (update :forms merge {:quote-form {:form-id :quote-form :syntax-id :quote-form :kind :list :child-form-ids [:quote-op :symbol-payload] :value '(quote unresolved/data)}
                              :quote-op {:form-id :quote-op :parent-form-id :quote-form :kind :symbol :value 'quote :child-form-ids []}
                              :symbol-payload {:form-id :symbol-payload :syntax-id :symbol-payload :parent-form-id :quote-form :kind :symbol :value 'unresolved/data :child-form-ids []}}))))

(deftest current-gravity-typed-catch-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))
        context (typed-catch-kernel-context base)
        instruction {:op :try :protected {:op :throw :value {:op :local :name 'problem}}
                     :handlers [{:type :gravity.type/string :name 'caught :body {:op :local :name 'caught}}]}]
    (is (= instruction (:value (invoke 'p15-s23-typed-compile-node context :try 65536))))
    (is (= {:try 1 :throw 1 :local 2} (:value (invoke 'p15-s23-summary-instruction instruction {}))))
    (is (= {:try 1 :throw 1 :local 4}
           (:value (invoke 'p15-s23-summary-instruction (update instruction :handlers #(vec (repeat 3 (first %)))) {}))))
    (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node context :handler 65536))))
    (doseq [mutate [#(assoc % :ledger []) #(update % :ledger into (:ledger %))
                    #(assoc-in % [:ledger 0 :status] :pending)
                    #(assoc-in % [:ledger 0 :dependencies :source-handlers] 42)
                    #(assoc-in % [:ledger 0 :dependencies :catch-types] 42)
                    #(assoc-in % [:nodes :try :children] 42)
                    #(assoc-in % [:nodes :try :attributes :handler-clauses] 42)
                    #(assoc-in % [:ledger 0 :dependencies :source-node :source :syntax-id] :swapped)
                    #(assoc-in % [:ledger 0 :dependencies :catch-types 0] :gravity.type/bool)
                    #(assoc-in % [:ledger 0 :dependencies :source-handlers 0 :catch-binding-id] :parameter)
                    #(assoc-in % [:nodes :try :attributes :handler-clauses 0 :clause-ordinal] 1)
                    #(assoc-in % [:bindings :caught :scope-id] :sibling)
                    #(assoc-in % [:bindings :string :namespace] 'foreign)
                    #(assoc-in % [:nodes :handler :source :form-id] :body-form)
                    #(assoc-in % [:forms :catch :child-form-ids] 42)
                    #(assoc-in % [:error-handlers 0 :handler-core-node-id] :reference)]]
      (let [rejected (invoke 'p15-s23-typed-compile-node (mutate context) :try 65536)]
        (is (= :rejected (:status rejected))) (is (= "P15S23Q002" (:diagnostic rejected)))))
    (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :try 1) [:facts :reason])))
    (let [context (typed-symbol-quote-kernel-context base)]
      (is (= {:op :quote :value 'unresolved/data} (:value (invoke 'p15-s23-typed-compile-node context :symbol-quote 65536))))
      (doseq [mutate [#(assoc % :ledger []) #(update % :ledger into (:ledger %))
                      #(assoc-in % [:ledger 0 :quoted-data :value] 'swapped)
                      #(assoc-in % [:ledger 0 :dependencies :quote :source :syntax-id] :swapped)
                      #(assoc-in % [:ledger 0 :solution :type] :gravity.type/string)
                      #(assoc-in % [:nodes :symbol-quote :children] [:reference])
                      #(assoc-in % [:forms :symbol-payload :parent-form-id] :sibling)
                      #(assoc-in % [:forms :quote-form :child-form-ids] 42)
                      #(assoc-in % [:forms :symbol-payload :child-form-ids] [:reference])]]
        (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node (mutate context) :symbol-quote 65536)))))
      (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :symbol-quote 1) [:facts :reason]))))))

(deftest current-gravity-typed-lowering-input-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation definition]} (typed-lowering-kernel-data)
        compile #(invoke 'p15-s23-typed-compile-function emitter %1 %2 %3 %4)
        result (compile module definition input observation)]
    (is (= :complete (:status result)) (pr-str result))
    (is (= [{:op :local :name 'problem}] (get-in result [:value :instructions])))
    (is (= definition (dissoc (:value result) :binding :instructions :type-input :typed-lowering-provenance)))
    (is (= :non-authoritative (get-in result [:value :typed-lowering-provenance :authority])))
    (is (= (:compiler-sources observation) (get-in result [:value :typed-lowering-provenance :compiler-sources])))
    (is (= :current-c6-typed-function-subset (get-in result [:value :typed-lowering-provenance :emission-scope])))
    (is (= (select-keys (first (get-in input [:typed :function-type-table])) [:function-core-node-id :body-core-node-id])
           (select-keys (get-in result [:value :type-input]) [:function-core-node-id :body-core-node-id])))
    (is (= :complete (:status (invoke 'p15-s23-typed-assemble-plan-products emitter module [['identity definition]] input observation))))
    (let [{:keys [module input observation definition]} (typed-lowering-kernel-data true)
          result (compile module definition input observation)]
      (is (= :complete (:status result)))
      (is (= [{:op :throw :value {:op :local :name 'problem}}] (get-in result [:value :instructions])))
      (is (= [:gravity.type/string] (get-in result [:value :type-input :thrown-error-effects]))))
    (doseq [[module definition input observation]
            [[42 definition input observation]
             [module definition 42 observation]
             [(assoc module :module 'other) definition input observation]
             [(assoc module :profile :hosted) definition input observation]
             [(assoc module :target :wasm) definition input observation]
             [(assoc module :source-path "other.gravity") definition input observation]
             [(assoc module :source-text "changed bytes") definition input observation]
             [(assoc module :effects 7) definition input observation]
             [(assoc module :forms []) definition input observation]
             [module (assoc definition :body '(false)) input observation]
             [module (assoc-in definition [:source-declarations :parameters] '[problem :- Bool]) input observation]
             [module definition (assoc input :extra true) observation]
             [module definition (assoc-in input [:request :canonical-core-artifact :nodes] 7) observation]
             [module definition (assoc-in input [:verification :status] :rejected) observation]
             [module definition input (assoc-in observation [:source :text] "changed bytes")]
             [module definition input (assoc-in observation [:verification :status] :rejected)]]]
      (let [rejected (compile module definition input observation)]
        (is (= :rejected (:status rejected))) (is (= "P15S23Q002" (:diagnostic rejected)))))
    (let [rejected-typed (assoc (:typed input) :status :rejected)
          passed-rejection {:artifact :gravity/sh08-function-type-verification :status :passed :expected rejected-typed :candidate rejected-typed}]
      (is (= :rejected (:status (compile module definition (assoc input :typed rejected-typed :verification passed-rejection)
                                                    (assoc observation :verification passed-rejection))))))
    (doseq [mutate [#(assoc-in % [:typed :type-table :function :return] :gravity.type/bool)
                    #(assoc-in % [:request :canonical-core-artifact :function-records 0 :body-core-node-id] :missing)
                    #(assoc-in % [:request :b47-context :authenticated-core-request :binding-table 0 :scope-id] :sibling)
                    #(assoc-in % [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact
                                 :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :token-stream 0 :raw] "changed")]]
      (let [changed (mutate input) report (assoc (:verification changed) :expected (:typed changed) :candidate (:typed changed))
            changed (assoc changed :verification report) observation (assoc observation :verification report)]
        (is (= :rejected (:status (compile module definition changed observation))))))
    (doseq [definitions [nil 7 [['identity definition] ['identity definition]] [] [['other definition]]
                        (vec (repeat 65537 ['identity definition]))]]
      (is (= :rejected (:status (invoke 'p15-s23-typed-assemble-plan-products emitter module definitions input observation)))))
    (let [source {:syntax-id :original-syntax :semantic-span {:source "original.gravity" :start 12 :end 19}
                  :origin-chain [:original] :generated-origin {:macro :original}}
          node {:node-id :unsupported :core-form :call :source source :binding-context {:scope :original}}
          context (:value (invoke 'p15-s23-typed-context module input observation))
          context (-> context (assoc-in [:nodes :unsupported] node) (assoc-in [:types :unsupported] :gravity.type/string))
          rejected (invoke 'p15-s23-typed-compile-node context :unsupported 65536)
          facts (:facts rejected)
          transported (diagnostic #(bootstrap/p15-s23-stage2-plan-emitter-fail! (:diagnostic rejected) path node facts))]
      (is (= :unsupported-typed-lowering-operation (:reason facts)))
      (is (= (assoc (select-keys source [:syntax-id :origin-chain :generated-origin])
                    :core-node-id :unsupported :source-span (:semantic-span source) :binding-context (:binding-context node))
             (select-keys facts [:core-node-id :syntax-id :source-span :origin-chain :generated-origin :binding-context])))
      (is (= facts (select-keys transported (keys facts))))
      (is (= "P15S23Q002" (:id transported))))
    (let [context (:value (invoke 'p15-s23-typed-context module input observation))
          decimal {:kind :gravity/decimal-occurrence :source "1.25"}
          context (-> context (assoc-in [:nodes :reference :core-form] :literal)
                              (assoc-in [:nodes :reference :attributes] {:literal-kind :decimal :value decimal})
                              (assoc-in [:forms :body-form :value] decimal)
                              (assoc-in [:types :reference] :gravity.type/decimal))
          rejected (invoke 'p15-s23-typed-compile-node context :reference 65536)]
      (is (= :rejected (:status rejected)))
      (is (= :unelaborated-typed-numeric-literal (get-in rejected [:facts :reason]))))
    (let [context (:value (invoke 'p15-s23-typed-context module input observation))
          context (-> context (assoc :locals {:parameter 'problem} :parameter-types {:parameter :gravity.type/string})
                      (assoc-in [:nodes :condition] {:node-id :condition :core-form :literal :children []
                                                    :attributes {:literal-kind :boolean :value true} :source {:form-id :condition-form}})
                      (assoc-in [:forms :condition-form] {:value true})
                      (assoc-in [:types :condition] :gravity.type/bool)
                      (assoc-in [:nodes :throw] {:node-id :throw :core-form :throw :children [:reference]})
                      (assoc-in [:types :throw] :gravity.type/never)
                      (assoc-in [:nodes :branch] {:node-id :branch :core-form :if :children [:condition :throw :reference]})
                      (assoc-in [:types :branch] :gravity.type/string)
                      (assoc-in [:nodes :sequence] {:node-id :sequence :core-form :do :children [:branch]})
                      (assoc-in [:types :sequence] :gravity.type/string))
          branch {:op :if :test {:op :literal :value true} :then {:op :throw :value {:op :local :name 'problem}}
                  :else {:op :local :name 'problem}}]
      (is (= branch (:value (invoke 'p15-s23-typed-compile-node context :branch 65536))))
      (is (= {:op :do :body [branch]} (:value (invoke 'p15-s23-typed-compile-node context :sequence 65536))))
      (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :sequence 1) [:facts :reason])))
      (is (= :unsupported-typed-lowering-operation
             (get-in (invoke 'p15-s23-typed-compile-node (assoc-in context [:nodes :sequence :core-form] :call) :sequence 65536) [:facts :reason]))))))

(deftest typed-lowering-public-orchestration-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        {:keys [module input definition]} (typed-lowering-kernel-data)
        original bootstrap/p15-s23-stage2-runtime-execute-function observed (atom [])
        operation (fn [call]
                    (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                  (fn [runtime plan function args]
                                    (when (= function 'sh08-verify-function-type-result)
                                      (swap! observed conj {:source (:source plan) :arguments args}))
                                    (original runtime plan function args))]
                      (diagnostic call)))]
    (let [failure (operation #(bootstrap/p15-s23-stage2-compile-function emitter module definition input))]
      (is (= "P15S23Q002" (:id failure)))
      (is (= :typed-input-source-or-verification-mismatch (:reason failure)))
      (is (= 1 (count @observed)))
      (is (= (str "sha256:" (bootstrap/sha256-hex
                              (slurp (io/file (.getParentFile (.getParentFile (io/file path)))
                                             "src/gravity/compiler/c7_type_checker_engine.gravity"))))
             (get-in @observed [0 :source :sha256])))
      (is (= [(:request input) (:typed input)] (:arguments (first @observed)))))
    (let [failure (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module [['identity definition]] input))]
      (is (= "P15S23Q002" (:id failure)))
      (is (= 2 (count @observed))))
    (let [calls (atom [])]
      (with-redefs [bootstrap/p15-s23-stage2-compiler-artifact-invoke
                    (fn [function args] (swap! calls conj [function args]) {:status :complete :value :legacy})]
        (is (= :legacy (bootstrap/p15-s23-stage2-compile-function emitter module definition)))
        (is (= :legacy (bootstrap/p15-s23-stage2-assemble-plan-products emitter module [['identity definition]]))))
      (is (= [['p15-s23-compile-function [emitter module definition]]
              ['p15-s23-assemble-plan-products [emitter module [['identity definition]]]]] @calls)))))

(deftest current-gravity-typed-lowering-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-products)
        runtime-path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity"))
        runtime-text (slurp runtime-path) runtime-forms (bootstrap/read-forms runtime-path runtime-text)
        names '#{p15-s23-runtime-evaluate-arguments p15-s23-runtime-println-output p15-s23-runtime-evaluate-bindings
                 p15-s23-runtime-typed-handlers-valid? p15-s23-runtime-atomic-error-match? p15-s23-runtime-select-typed-handler
                 p15-s23-runtime-evaluate-sequence p15-s23-runtime-evaluate-closed-instruction p15-s23-runtime-execute-closed-plan}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (names (second %))) runtime-forms)
        projection (str/join "\n" (map pr-str (cons (first runtime-forms) selected)))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        runtime-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter runtime-path projection)
        execute (fn [instruction env]
                  (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan
                    'p15-s23-runtime-evaluate-closed-instruction [instruction env ""]))
        verifier 'sh08-verify-function-type-result original bootstrap/p15-s23-stage2-runtime-execute-function]
    (is (= 9 (count selected)))
    (is (= (str "sha256:" (bootstrap/sha256-hex projection)) (get-in runtime-plan [:source :sha256])))
    (is (not= (bootstrap/sha256-hex runtime-text) (bootstrap/sha256-hex projection)))
    (doseq [extension [".gravity" ".qst"]]
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/direct-typed-throw" extension)
            source-path (.getPath (io/file root relative)) text (slurp source-path)
            p (fresh relative) input (select-keys p [:request :typed :verification])
            expanded (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :expanded-forms])
            original-forms (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact
                                         :gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
            module (assoc (bootstrap/parse-module source-path original-forms) :forms expanded)
            definitions (bootstrap/stage0-function-table module)
            module (assoc module :function-table definitions) definition (get definitions 'choose)
            calls (atom []) reads (atom []) read-source bootstrap/read-gravity-source-text
            operation (fn [thunk]
                        (let [before (count @calls)
                              value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                                   (fn [runtime plan function args]
                                                     (when (= verifier function) (swap! calls conj [(:source plan) args]))
                                                     (original runtime plan function args))
                                                 bootstrap/read-gravity-source-text
                                                   (fn [path] (swap! reads conj path) (read-source path))]
                                      (thunk))]
                          (is (= 1 (- (count @calls) before))) value))
            compiled (operation #(bootstrap/p15-s23-stage2-compile-function emitter module definition input))
            assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
            instruction (first (:instructions compiled)) payload (String. "problem")
            transport (module-private 'p15-s23-stage2-runtime-source-throw!) transport-count (atom 0) original-transport (var-get transport)]
        (is (= "755e5b876b8fde5f1be48b0dce2cea96c17da24d967b4d9e641c720a46617f23" (bootstrap/sha256-hex text)))
        (is (= :accepted (get-in input [:typed :status])))
        (is (= :passed (get-in input [:verification :status])))
        (is (= definition (dissoc compiled :binding :instructions :type-input :typed-lowering-provenance)))
        (is (= compiled (get-in assembled [:functions 'choose])))
        (is (= {:if 1 :local 3 :throw 1} (:instruction-summary assembled)))
        (is (= :non-authoritative (get-in compiled [:typed-lowering-provenance :authority])))
        (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text))
               (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
        (is (not= (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])
                  (var-get (module-private 'p15-s23-stage2-compiler-artifact-expected-source-content-hash))))
        (is (= {:path source-path :sha256 (str "sha256:" (bootstrap/sha256-hex text))}
               (get-in compiled [:typed-lowering-provenance :source])))
        (is (not-any? #{source-path} @reads))
        (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
        (with-redefs-fn {transport (fn [& args] (swap! transport-count inc) (apply original-transport args))}
          #(doseq [[flag control] [[false :normal] [true :thrown]]]
             (let [result (execute instruction {'flag flag 'problem payload})]
               (is (identical? payload (first result)))
               (is (= ["" control] (subvec result 1))))))
        (is (= 0 @transport-count))
        ;; All mutations reuse the original immutable upstream products. Each
        ;; public operation independently invokes current Gravity C7 once.
        (doseq [thunk [#(bootstrap/p15-s23-stage2-compile-function emitter (assoc module :module 'other) definition input)
                       #(bootstrap/p15-s23-stage2-compile-function emitter (assoc module :source-text "changed bytes") definition input)
                       #(bootstrap/p15-s23-stage2-compile-function emitter module (assoc definition :body '(false)) input)
                       #(bootstrap/p15-s23-stage2-compile-function emitter module (assoc-in definition [:source-declarations :return] '[:- Bool]) input)
                       #(bootstrap/p15-s23-stage2-compile-function emitter module definition (assoc-in input [:verification :status] :rejected))
                       #(bootstrap/p15-s23-stage2-compile-function emitter module definition (assoc-in input [:typed :type-table
                              (get-in input [:typed :function-type-table 0 :body-core-node-id])] :gravity.type/bool))]]
          (let [failure (operation #(diagnostic thunk))]
            (is (= "P15S23Q002" (:id failure)) (pr-str failure))))))))

(defn- typed-catch-runtime-projection []
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity")) text (slurp path)
        forms (bootstrap/read-forms path text)
        arities {'p15-s23-runtime-evaluate-arguments 4 'p15-s23-runtime-println-output 2
                 'p15-s23-runtime-evaluate-bindings 3 'p15-s23-runtime-evaluate-sequence 3
                 'p15-s23-runtime-evaluate-closed-instruction 3 'p15-s23-runtime-execute-closed-plan 1
                 'p15-s23-runtime-typed-handlers-valid? 1 'p15-s23-runtime-atomic-error-match? 2 'p15-s23-runtime-select-typed-handler 3}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? arities (second %))) forms)
        projection (str/join "\n" (map pr-str (cons (first forms) selected)))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path projection)
        builtins (set (get-in (bootstrap/p15-s23-stage2-compiler-artifact-augmented-emitter emitter) [:call-rules :builtin-functions]))
        calls (keep #(when (and (seq? %) (symbol? (first %))) (first %)) (tree-seq coll? seq selected))]
    (is (= 9 (count selected) (count (set (map second selected)))))
    (is (= arities (into {} (map #(vector (second %) (count (nth % 2))) selected))))
    (is (= selected (vec (rest (bootstrap/read-forms path projection)))))
    (is (every? #(or (contains? arities %) (contains? builtins %) (contains? '#{defn if let loop recur quote} %)) calls))
    (is (= (str "sha256:" (bootstrap/sha256-hex projection)) (get-in plan [:source :sha256])))
    (is (not= (bootstrap/sha256-hex text) (bootstrap/sha256-hex projection)))
    {:plan plan :parent-source {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))}}))

(defn- typed-let-kernel-context [base names]
  (let [n (count names) binding-ids (mapv #(keyword (str "let-binding-" %)) (range n))
        init-ids (mapv #(keyword (str "let-init-" %)) (range n))
        name-ids (mapv #(keyword (str "let-name-" %)) (range n))
        scopes (mapv #(keyword (str "let-scope-" %)) (range n))
        source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain nil :generated-origin nil})
        body-scope (if (zero? n) :let-outer (last scopes))
        attrs {:binding-vector-form-id :let-vector :binding-vector-syntax-id :let-vector :outer-scope-id :let-outer :body-scope-id body-scope
               :binding-count n :body-count 1 :initializer-child-indexes (vec (range n)) :body-child-indexes [n]
               :evaluation-order :initializers-then-body-left-to-right :result-policy {:kind :last-body :child-index n}}
        records (mapv (fn [i] {:let-core-node-id :let :ordinal i :name (get names i) :binding-id (get binding-ids i)
                               :definition-form-id (get name-ids i) :definition-syntax-id (get name-ids i) :binding-scope-id (get scopes i)
                               :initializer-form-id (get init-ids i) :initializer-syntax-id (get init-ids i)
                               :initializer-scope-id (if (zero? i) :let-outer (get scopes (dec i))) :initializer-node-id (get init-ids i)
                               :visible-prior-binding-ids (subvec binding-ids 0 i) :mutability :immutable}) (range n))
        module (get-in base [:core :module])
        facts (mapv (fn [record]
                      {:artifact :gravity/c7-local-binding-fact
                       :fact-id-request {:domain :gravity/sh08-local-binding-fact-v1 :binding-id (:binding-id record)
                                         :initializer-node-id (:initializer-node-id record) :type :gravity.type/string :profile (:profile module) :target (:target module)}
                       :binding-id (:binding-id record) :name (:name record) :type :gravity.type/string
                       :initializer-node-id (:initializer-node-id record) :let-core-node-id :let
                       :source-span nil :syntax-id (:initializer-syntax-id record) :origin-chain nil :generated-origin nil
                       :profile (:profile module) :target (:target module) :effects (:effects module) :capabilities (:capabilities module) :status :inferred}) records)
        initializers (into {} (map-indexed (fn [i id] [id {:node-id id :core-form :reference :children []
                                                         :attributes {:binding-id (if (zero? i) :parameter (get binding-ids (dec i)))} :source (source id)}]) init-ids))
        forms (into {} (mapcat (fn [record]
                                [[(:definition-form-id record) {:form-id (:definition-form-id record) :syntax-id (:definition-syntax-id record)
                                                                :kind :symbol :value (:name record) :child-form-ids [] :scope-id (:binding-scope-id record) :parent-form-id :let-vector}]
                                 [(:initializer-form-id record) {:form-id (:initializer-form-id record) :syntax-id (:initializer-syntax-id record)
                                                                 :scope-id (:initializer-scope-id record) :parent-form-id :let-vector}]]) records))]
    (-> base (assoc :locals {:parameter 'problem} :parameter-types {:parameter :gravity.type/string}
                    :lexical records :local-facts (into {} (map (juxt :binding-id identity) facts)))
        (update :bindings merge (into {} (map (fn [record] [(:binding-id record) {:binding-id (:binding-id record) :name (:name record) :kind :local
                                                                               :binding-class :lexical :definition-syntax-id (:definition-syntax-id record) :scope-id (:binding-scope-id record)}]) records)))
        (update :nodes merge initializers {:let {:node-id :let :core-form :let :children (conj init-ids :let-body) :resolved-binding-ids binding-ids
                                                :attributes attrs :source (source :let-form)}
                                          :let-body {:node-id :let-body :core-form :reference :children [] :source (source :let-body)
                                                     :attributes {:binding-id (if (zero? n) :parameter (last binding-ids))}}})
        (update :types merge (zipmap (conj init-ids :let-body :let) (repeat :gravity.type/string)))
        (update :forms merge forms {:let-form {:form-id :let-form :syntax-id :let-form :kind :list :scope-id :let-outer :child-form-ids [:let-op :let-vector :let-body]}
                                    :let-op {:form-id :let-op :kind :symbol :value 'let :child-form-ids [] :parent-form-id :let-form}
                                    :let-vector {:form-id :let-vector :syntax-id :let-vector :scope-id nil :parent-form-id :let-form :kind :vector
                                                 :child-form-ids (vec (mapcat vector name-ids init-ids))}
                                    :let-body {:form-id :let-body :syntax-id :let-body :scope-id body-scope :parent-form-id :let-form}}))))

(deftest current-gravity-typed-let-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))
        runtime-plan (:plan (typed-catch-runtime-projection))
        execute #(bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction [% {'problem "original"} ""])
        compile #(invoke 'p15-s23-typed-compile-node % :let 65536)
        context (typed-let-kernel-context base '[alias alias]) result (compile context) instruction (:value result)]
    (is (= :complete (:status result)) (pr-str result))
    (is (nil? (get-in context [:forms :let-vector :scope-id])))
    (is (= {:op :let :bindings [{:name 'alias :expr {:op :local :name 'problem}}]
            :body [{:op :let :bindings [{:name 'alias :expr {:op :local :name 'alias}}]
                    :body [{:op :do :body [{:op :local :name 'alias}]}]}]} instruction))
    (is (= ["original" "" :normal] (execute instruction)))
    (is (= {:parameter 'problem} (:locals context)))
    (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node context :let-body 65536))))
    (let [result (compile (typed-let-kernel-context base []))]
      (is (= {:op :do :body [{:op :local :name 'problem}]} (:value result)))
      (is (= ["original" "" :normal] (execute (:value result)))))
    (doseq [mutate [#(assoc-in % [:nodes :let :children] 42) #(assoc-in % [:nodes :let :attributes :binding-count] 42)
                    #(assoc-in % [:nodes :let :attributes :body-count] 0) #(assoc-in % [:forms :let-vector :child-form-ids] 42)
                    #(assoc-in % [:forms :let-vector :child-form-ids] [:let-name-0])
                    #(assoc-in % [:forms :let-op :value] 'loop) #(assoc-in % [:forms :let-op :parent-form-id] :sibling)
                    #(assoc-in % [:forms :let-form :kind] :vector)
                    #(assoc-in % [:nodes :let :attributes :outer-scope-id] :wrong)
                    #(assoc-in % [:lexical 0 :ordinal] 1) #(assoc-in % [:lexical 1 :visible-prior-binding-ids] [])
                    #(update % :lexical pop) #(update % :lexical conj (first (:lexical %)))
                    #(assoc-in % [:bindings :let-binding-0 :scope-id] :sibling)
                    #(assoc-in % [:forms :let-init-0 :scope-id] :let-scope-1)
                    #(assoc-in % [:forms :let-body :scope-id] :let-outer)
                    #(assoc-in % [:nodes :let-init-0 :attributes :binding-id] :let-binding-0)
                    #(assoc-in % [:nodes :let-init-0 :attributes :binding-id] :let-binding-1)
                    #(assoc-in % [:nodes :let :resolved-binding-ids] [:let-binding-1 :let-binding-0])
                    #(assoc-in % [:local-facts :let-binding-0 :type] :gravity.type/bool)
                    #(assoc-in % [:local-facts :let-binding-0 :fact-id-request :type] :gravity.type/bool)
                    #(assoc-in % [:types :let] :gravity.type/bool)]]
      (let [rejected (compile (mutate context))]
        (is (= :rejected (:status rejected))) (is (= "P15S23Q002" (:diagnostic rejected)))))
    (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :let 1) [:facts :reason])))
    (let [context (-> context (assoc-in [:nodes :let-init-0 :core-form] :throw)
                      (assoc-in [:nodes :let-init-0 :children] [:reference]) (assoc-in [:nodes :let-init-0 :attributes] {})
                      (assoc-in [:types :let-init-0] :gravity.type/never) (assoc-in [:types :let] :gravity.type/never)
                      (assoc-in [:local-facts :let-binding-0 :type] :gravity.type/never)
                      (assoc-in [:local-facts :let-binding-0 :fact-id-request :type] :gravity.type/never)
                      (assoc-in [:nodes :let-init-1 :core-form] :literal) (assoc-in [:nodes :let-init-1 :attributes] {:value "unreachable"})
                      (assoc-in [:forms :let-init-1 :value] "unreachable"))
          result (compile context)]
      (is (= :complete (:status result)) (pr-str result))
      (is (= ["original" "" :thrown] (execute (:value result)))))
    (doseq [family [:lexical-bindings]]
      (is (= :rejected (:status (invoke 'p15-s23-typed-context module (assoc-in input [:request :canonical-core-artifact family] 42) observation)))))
    (is (= :rejected (:status (invoke 'p15-s23-typed-context module (assoc-in input [:typed :local-binding-facts] 42) observation))))))

(deftest current-let-container-scope-projection-development-kernel
  ;; Invoke the actual projection traversal and final form projection on small
  ;; normalized DATA. This checks the producer schema, not authentication.
  (let [helper (fn [name]
                 ;; Installation captures these helpers and then unmaps their
                 ;; Vars. Evaluate their exact saved function bodies in the
                 ;; owning namespace without interning replacement Vars.
                 (let [suffix (if (= name "make-traversal") "traversal" name)
                       resource (str "gravity/bootstrap_impl/semantic_late/154235-sh07-core-resolution-" suffix ".clj")
                       form (read-string (slurp (io/resource resource)))]
                   (is (= 'clojure.core/defn (first form)))
                   (is (= (symbol (str "semantic-late-sh07-core-resolution-projection!-" name)) (second form)))
                   (binding [*ns* (the-ns 'gravity.bootstrap)] (eval (cons 'fn (nnext form))))))
        scopes (atom {}) declarations (atom {})
        projected-id (fn [_ path _] path)
        remember (fn [_ path _ active] (swap! scopes assoc path (first active)))
        consume (fn [_ _ _] {:scope-id :inner :sh07/projected-scope-id :inner :bindings [{:binding-syntax-id :upstream}]})
        walk ((helper "make-traversal") (helper "sequence") (helper "fn") (helper "match") (helper "try") (helper "bindings")
              projected-id remember consume (fn [& _]) scopes declarations "normalized.gravity")
        _ (walk {} '(let [value problem] value) [] [:outer] :expression)
        records (mapv (fn [[id syntax]] {:form-id id :syntax-id syntax})
                      [[:owner []] [:vector [1]] [:name [1 0]] [:initializer [1 1]] [:body [2]]])
        projected ((helper "projection") {:source-path "normalized.gravity" :source-revision-id :normalized
                                           :trees [{:records records}] :resolved-analysis {:binding-table [] :alias-table []}
                                           :root-by-upstream-id {} :declaration-syntax-by-upstream-id declarations
                                           :projected-scope-by-upstream (atom {}) :scope-by-syntax-id scopes :occurrences (atom [])})
        forms (into {} (map (juxt :form-id identity) (:forms projected)))]
    (is (nil? (get-in forms [:vector :scope-id])))
    (is (= :outer (get-in forms [:owner :scope-id]) (get-in forms [:initializer :scope-id])))
    (is (= :inner (get-in forms [:name :scope-id]) (get-in forms [:body :scope-id])))
    (is (= :inner (get @scopes [1 0])))
    (is (not (contains? @scopes [1])))))

(deftest current-gravity-typed-let-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        {:keys [plan parent-source]} (typed-catch-runtime-projection)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter-text (slurp emitter-path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-products)
        original bootstrap/p15-s23-stage2-runtime-execute-function
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) transports (atom 0) original-transport (var-get transport)
        execute (fn [function env]
                  (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                    #(original runtime plan 'p15-s23-runtime-evaluate-closed-instruction [(first (:instructions function)) env ""])))
        verifications (atom 0)]
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp (:path parent-source)))) (:sha256 parent-source)))
    (doseq [extension [".gravity" ".qst"]]
      (doseq [name ["local-runtime-let-self-reference" "local-runtime-let-forward-reference"]]
        (let [source-path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" name extension)))
              failure (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact source-path))]
          (is (= "C5-UNRESOLVED" (:id failure)) (pr-str failure))))
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-symbol-let" extension)
            source-path (.getPath (io/file root relative))
            p (fresh relative) input (select-keys p [:request :typed :verification])
            macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
            original-forms (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
            module (assoc (bootstrap/parse-module source-path original-forms) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
            operation (fn [thunk]
                        (let [before @verifications
                              result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                                   (fn [runtime plan function args]
                                                     (when (= 'sh08-verify-function-type-result function) (swap! verifications inc))
                                                     (original runtime plan function args))] (thunk))]
                          (is (= 1 (- @verifications before))) result))
            assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
            records (get-in input [:request :canonical-core-artifact :lexical-bindings])
            lets (filter #(and (map? %) (= :let (:op %))) (tree-seq coll? seq (:functions assembled)))
            payload (String. "original")]
        (is (= "5fc16cee6c8ac66ecf2adba725119e83409fe797c87970843b8177f843590a34" (bootstrap/sha256-hex (slurp source-path))))
        (is (= :accepted (get-in input [:typed :status]))) (is (= :passed (get-in input [:verification :status])))
        (is (= 9 (count (:functions assembled))))
        (is (= (count records) (count lets) (get-in assembled [:instruction-summary :let])))
        (is (every? #(= 1 (count (:bindings %))) lets))
        (doseq [name '[aliases empty-bindings repeated-name nested-initializer restore-outer multiple-body never-initializer catch-alias]]
          (let [compiled (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions name) input))
                result (execute compiled {'problem payload})]
            (is (= compiled (get-in assembled [:functions name])))
            (is (identical? payload (first result))) (is (= ["" :normal] (subvec result 1)))
            (is (= :pending-c8-and-profile-authority (get-in compiled [:typed-lowering-provenance :effect-legality])))
            (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text))
                   (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))))
        (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
          #(let [result (original runtime plan 'p15-s23-runtime-execute-closed-plan [assembled])]
             (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main :stdout ""
                     :entrypoint-result "recovered" :status :complete :clojure-seed-boundary? true :self-hosted? false} result))))
        (doseq [changed [(assoc-in input [:typed :local-binding-facts 0 :type] :gravity.type/bool)
                         (assoc-in input [:request :canonical-core-artifact :lexical-bindings 0 :visible-prior-binding-ids] [:wrong])]]
          (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'main) changed))))]
            (is (= "P15S23Q002" (:id failure)))))))
    (is (= 0 @transports))))

(deftest current-gravity-typed-catch-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        {:keys [plan parent-source]} (typed-catch-runtime-projection)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-products)
        original bootstrap/p15-s23-stage2-runtime-execute-function
        execute (fn [instruction env] (original runtime plan 'p15-s23-runtime-evaluate-closed-instruction [instruction env ""]))
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) transports (atom 0) original-transport (var-get transport)
        observed-verifications (atom 0)]
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp (:path parent-source)))) (:sha256 parent-source)))
    (doseq [extension [".gravity" ".qst"] name ["atomic-typed-catch" "local-runtime-atomic-catch"]]
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/" name extension)
            source-path (.getPath (io/file root relative))
            p (fresh relative) input (select-keys p [:request :typed :verification])
            macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
            original-forms (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
            module (assoc (bootstrap/parse-module source-path original-forms) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
            operation (fn [thunk]
                        (let [before @observed-verifications
                              value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                                   (fn [runtime plan function args]
                                                     (when (= 'sh08-verify-function-type-result function) (swap! observed-verifications inc))
                                                     (original runtime plan function args))]
                                      (thunk))]
                          (is (= 1 (- @observed-verifications before))) value))
            compile-function (fn [name] (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions name) input)))]
        (is (= (if (= name "atomic-typed-catch") "1a45d1b4dc314a9685d261d6db81de056358b24813c163039f1187091f5761d5"
                   "6f6de30256d77b0d583b57db828867366e86059d2dcc9a70fee74d1688333670") (bootstrap/sha256-hex (slurp source-path))))
        (is (= :accepted (get-in input [:typed :status]))) (is (= :passed (get-in input [:verification :status])))
        (if (= name "atomic-typed-catch")
          (let [functions (into {} (map #(vector % (compile-function %)) '[recover partial nested ordered rethrow bypass]))
                payload (String. "original")
                cases [['recover {'flag false 'problem payload} payload :normal]
                       ['recover {'flag true 'problem payload} payload :normal]
                       ['partial {'flag true 'problem payload} payload :normal]
                       ['partial {'flag false 'problem payload} false :thrown]
                       ['nested {'problem payload} payload :normal]
                       ['ordered {'flag true 'problem payload} payload :thrown]
                       ['ordered {'flag false 'problem payload} "bool" :normal]
                       ['rethrow {'problem payload} payload :thrown]
                       ['bypass {'problem payload} payload :normal]]]
            (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
              #(doseq [[name env expected control] cases]
                 (let [function (get functions name) result (execute (first (:instructions function)) env)]
                   (is (= [expected "" control] result))
                   (when (identical? expected payload) (is (identical? payload (first result))))
                   (is (= :pending-c8-and-profile-authority (get-in function [:typed-lowering-provenance :effect-legality]))))))
            (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'call-catch) input))))]
              (is (= "P15S23Q002" (:id failure))) (is (= :unsupported-typed-lowering-operation (:reason failure)))))
          (let [assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                functions (:functions assembled)]
            (is (= {:try 5 :throw 5 :local 5 :quote 1 :literal 4} (:instruction-summary assembled)))
            (doseq [[name expected] [['symbol-error 'unresolved/data] ['keyword-error :unresolved/data] ['bool-error false] ['string-error "original"]]]
              (let [function (compile-function name)]
                (is (= function (get functions name)))
                (is (= [expected "" :normal]
                       (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                         #(execute (first (:instructions function)) {}))))))
            (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
              #(let [result (original runtime plan 'p15-s23-runtime-execute-closed-plan [assembled])]
                 (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main :stdout ""
                         :entrypoint-result "recovered" :status :complete :clojure-seed-boundary? true :self-hosted? false} result))))
            (let [index (first (keep-indexed #(when (= :sh08-atomic-typed-catch-v1 (:producer-rule %2)) %1) (get-in input [:typed :constraint-ledger])))
                  changed (assoc-in input [:typed :constraint-ledger index :dependencies :catch-types] [:gravity.type/bool])
                  failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'main) changed))))]
              (is (= "P15S23Q002" (:id failure))))))))
    (is (= 0 @transports))))

(deftest current-gravity-typed-catch-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        literal (fn [value] {:op :literal :value value})
        throw-value (fn [value] {:op :throw :value (literal value)})
        handler (fn [type body] {:type type :name 'caught :body body})
        recover (fn [protected handlers] {:op :try :protected protected :handlers handlers})
        local {:op :local :name 'caught}
        say (fn [label value] {:op :do :body [{:op :println :args [(literal label)] :effect :io/write :capability :io/stdout} value]})
        eval #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        execute #(invoke 'p15-s23-runtime-execute-closed-plan {:entrypoint 'main :functions {'main {:instructions [%]}}})
        payload (String. "original")]
    (doseq [[type value] [[:gravity.type/string payload] [:gravity.type/bool false] [:gravity.type/bool true]
                         [:gravity.type/symbol 'unresolved/data] [:gravity.type/keyword :unresolved/data]]]
      (let [result (eval (recover (throw-value value) [(handler type local)]))]
        (is (identical? value (first result))) (is (= ["" :normal] (subvec result 1)))))
    (doseq [value [nil false {} [] {:op :throw :value payload} [:original "" :thrown] (ex-info "inert" {}) (AssertionError. "inert")]]
      (let [result (eval (recover (literal value) [(handler :gravity.type/string (throw-value :unreachable))]))]
        (is (identical? value (first result))) (is (= ["" :normal] (subvec result 1)))))
    (is (= [payload "protected\nhandler\n" :normal]
           (eval (recover (say "protected" (throw-value payload)) [(handler :gravity.type/string (say "handler" local))]))))
    (is (= [payload "protected\nfirst\n" :thrown]
           (eval (recover (say "protected" (throw-value payload))
                          [(handler :gravity.type/string (say "first" {:op :throw :value local}))
                           (handler :gravity.type/string (say "unreachable" (literal :bad)))]))))
    (is (= [payload "inner\nouter\n" :normal]
           (eval (recover (recover (throw-value payload) [(handler :gravity.type/string (say "inner" {:op :throw :value local}))])
                          [(handler :gravity.type/string (say "outer" local))]))))
    (doseq [value [nil {} [] {:op :throw :value payload} [:original "" :thrown] (ex-info "inert" {})]]
      (let [result (eval (recover (throw-value value) [(handler :gravity.type/bool (literal :bad)) (handler :gravity.type/string (literal :bad))]))]
        (is (identical? value (first result))) (is (= ["" :thrown] (subvec result 1)))))
    (let [rejected (eval (recover {:op :unsupported} [(handler :gravity.type/string (say "unreachable" local))]))]
      (is (= ["" :rejected] (subvec rejected 1))) (is (= "P15S23X002" (:diagnostic (first rejected)))))
    (let [rejected (eval (recover (throw-value payload) [(handler :gravity.type/string {:op :unsupported})
                                                         (handler :gravity.type/string (say "unreachable" local))]))]
      (is (= ["" :rejected] (subvec rejected 1))) (is (= "P15S23X002" (:diagnostic (first rejected)))))
    (let [environment {'caught :outer}
          result (invoke 'p15-s23-runtime-evaluate-closed-instruction (recover (throw-value payload) [(handler :gravity.type/string local)]) environment "")]
      (is (identical? payload (first result))) (is (= {'caught :outer} environment)))
    (doseq [handlers [nil 42 {} [] [42] [{:type :gravity.type/i32 :name 'caught :body local}]
                      [(handler :gravity.type/string local) {:type :gravity.type/string :name 42 :body local}]
                      [(assoc (handler :gravity.type/string local) :extra true)]
                      [(handler :gravity.type/string 42)] (vec (repeat 65537 (handler :gravity.type/string local)))]]
      (let [result (eval (recover (say "unreachable" (throw-value payload)) handlers))]
        (is (= ["" :rejected] (subvec result 1))) (is (= "P15S23X002" (:diagnostic (first result))))))
    (let [normal (execute (recover (throw-value payload) [(handler :gravity.type/string local)]))
          thrown (execute (recover (throw-value payload) [(handler :gravity.type/bool local)]))]
      (is (= :complete (:status normal))) (is (identical? payload (:entrypoint-result normal)))
      (is (= #{:artifact :entrypoint :stdout :clojure-seed-boundary? :self-hosted? :entrypoint-result :status} (set (keys normal))))
      (is (= :thrown (:status thrown))) (is (identical? payload (:thrown-value thrown))) (is (not (contains? thrown :entrypoint-result))))
    (let [failure (AssertionError. "fatal") environment (ThrowingAssociative. failure)
          observed (seed-throw-test-capture #(invoke 'p15-s23-runtime-evaluate-closed-instruction
                      (recover (throw-value payload) [(handler :gravity.type/string local)]) environment ""))]
      (is (identical? failure observed)))))

(deftest current-gravity-runtime-throw-development-kernel
  (let [fixture-path (module-fixture "seed-library" ".gravity")
        root (loop [directory (.getParentFile (io/file fixture-path))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        source-path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity"))
        text (slurp source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! fixture-path :jvm))
        parent-forms (bootstrap/read-forms source-path text)
        arities {'p15-s23-runtime-evaluate-arguments 4 'p15-s23-runtime-println-output 2
                 'p15-s23-runtime-typed-handlers-valid? 1 'p15-s23-runtime-atomic-error-match? 2 'p15-s23-runtime-select-typed-handler 3
                 'p15-s23-runtime-evaluate-bindings 3 'p15-s23-runtime-evaluate-sequence 3
                 'p15-s23-runtime-evaluate-closed-instruction 3 'p15-s23-runtime-execute-closed-plan 1}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? arities (second %))) parent-forms)
        projection (str/join "\n" (map pr-str (cons (first parent-forms) selected)))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path projection)
        invoke (fn [function & args]
                 (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled function (vec args)))
        literal (fn [value] {:op :literal :value value})
        say (fn [value] {:op :println :args [(literal value)] :effect :io/write :capability :io/stdout})
        marked (fn [label value] {:op :do :body [(say label) (literal value)]})
        escape (fn [value] {:op :throw :value (literal value)})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        execute #(invoke 'p15-s23-runtime-execute-closed-plan {:entrypoint 'main :functions {'main {:instructions [%]}}})
        payload (Object.) transport (module-private 'p15-s23-stage2-runtime-source-throw!)
        seed-transports (atom 0) original (var-get transport)]
    ;; The parent manifests contain bare-symbol DATA outside raw3's literal
    ;; subset. Compile exact original function forms as a fresh projection;
    ;; retain its own hash and the distinct complete parent-source provenance.
    (is (= text (slurp source-path)))
    (is (= 9 (count selected) (count (set (map second selected)))))
    (is (= arities (into {} (map #(vector (second %) (count (nth % 2))) selected))))
    (is (= selected (vec (rest (bootstrap/read-forms source-path projection)))))
    (is (= (str "sha256:" (bootstrap/sha256-hex projection)) (get-in compiled [:source :sha256])))
    (is (not= (bootstrap/sha256-hex text) (bootstrap/sha256-hex projection)))
    (let [builtins (set (get-in (bootstrap/p15-s23-stage2-compiler-artifact-augmented-emitter emitter) [:call-rules :builtin-functions]))
          calls (keep #(when (and (seq? %) (symbol? (first %))) (first %)) (tree-seq coll? seq selected))]
      (is (every? #(or (contains? arities %) (contains? builtins %) (contains? '#{defn if let loop recur quote} %)) calls)))
    (is (nil? (:scope compiled)))
    (with-redefs-fn {transport (fn [& args] (swap! seed-transports inc) (apply original args))}
      (fn []
        (doseq [value [nil false payload {:op :throw :kind :gravity/recur :values [false]}
                       [:payload "output" :thrown] {:status :rejected :diagnostic :data}
                       (ex-info "payload" {}) (AssertionError. "payload")]]
          (is (= [value "" :normal] (evaluate (literal value))))
          (is (= [value "" :normal] (evaluate {:op :quote :value value})))
          (let [result (evaluate (escape value))]
            (is (identical? value (first result)))
            (is (= ["" :thrown] (subvec result 1)))))
        (is (= [payload "once\n" :thrown]
               (evaluate {:op :throw :value {:op :throw :value (marked "once" payload)}})))
        (doseq [position (range 3)]
          (let [items (mapv #(if (= position %) (escape payload) (marked (str %) %)) (range 3))
                before (apply str (map #(str % "\n") (range position)))]
            (doseq [instruction [{:op :do :body items}
                                 {:op :println :args items :effect :io/write :capability :io/stdout}
                                 {:op :let :bindings (mapv (fn [n expr] {:name (symbol (str "x" n)) :expr expr}) (range) items)
                                  :body [(say "later")]}]]
              (is (= [payload before :thrown] (evaluate instruction))))))
        (doseq [position (range 2)]
          (let [items (mapv #(if (= position %) (escape payload) (marked "before" "value")) (range 2))]
            (is (= [payload (if (zero? position) "" "before\n") :thrown]
                   (evaluate {:op :builtin-call :function 'str :args items})))))
        (doseq [instruction [{:op :if :test (escape payload) :then (say "later") :else (say "later")}
                             {:op :if :test (literal true) :then (escape payload) :else (say "later")}
                             {:op :if :test (literal false) :then (say "later") :else (escape payload)}]]
          (is (= [payload "" :thrown] (evaluate instruction))))
        (doseq [test [nil false]]
          (is (= [:else "" :normal]
                 (evaluate {:op :if :test (literal test) :then (escape payload) :else (literal :else)}))))
        (doseq [bad [nil false {} {:op :mystery :bindings [{:name 'x :expr (say "later")}] :body [(say "later")]}
                     {:op :function-call :function 'unknown :args [(say "later")]}
                     {:op :builtin-call :function 'unknown :args [(say "later") (say "later")]}
                     {:op :builtin-call :function 'str :args [(say "later") (say "later") (say "later")]}
                     {:op :builtin-call :function '< :args [(say "later")]}
                     {:op :throw} {:op :throw :value nil} {:op :throw :value (say "later") :extra true}
                     {:op :if :test (say "later") :then nil :else (literal 0)}
                     {:op :let :bindings [{:name 'x :expr (say "later")} {:name 'x :expr (literal 0)}] :body []}
                     {:op :let :bindings [{:name 'x :expr (say "later")} {:name 'y :expr nil}] :body []}
                     {:op :local :name 'unknown}]]
          (let [result (evaluate bad)]
            (is (= ["" :rejected] (subvec result 1)))
            (is (= "P15S23X002" (:diagnostic (first result))))
            (is (= result (evaluate {:op :throw :value (if (map? bad) bad {})})))))
        (doseq [function '[= < <= > >=]]
          (is (= [(case function = false < true <= true > false >= false) "" :normal]
                 (evaluate {:op :builtin-call :function function :args [(literal 1) (literal 2)]})))
          (is (= :rejected (get (evaluate {:op :builtin-call :function function :args [(literal "bad") (literal 2)]}) 2))))
        (doseq [values [["a"] ["a" "b"] [nil false]]]
          (is (= [(apply str values) "" :normal]
                 (evaluate {:op :builtin-call :function 'str :args (mapv literal values)}))))
        (doseq [values [[] ["a"] ["a" "b"] ["\u03bb\u0000" false nil]]]
          (let [instruction {:op :println :args (mapv literal values) :effect :io/write :capability :io/stdout}
                expected (str (str/join " " (map str values)) "\n")]
            (is (= [nil expected :normal] (evaluate instruction)))
            (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main
                    :entrypoint-result nil :stdout expected :status :complete :clojure-seed-boundary? true :self-hosted? false}
                   (execute instruction)))))
        (is (= [2 "" :normal] (evaluate {:op :let :bindings [{:name 'x :expr (literal 2)}
                                                                        {:name 'y :expr {:op :local :name 'x}}]
                                           :body [{:op :local :name 'y}]})))
        (doseq [instruction [(assoc (literal 2) :source-span {:line 1})
                             {:op :let :source-span {:line 2}
                              :bindings [{:name 'x :expr (assoc (literal 2) :source-span {:line 3}) :source-span {:line 3}}]
                              :body [{:op :local :name 'x :source-span {:line 4}}]}]]
          (let [guest {:entrypoint 'main :functions {'main {:arity 0 :params [] :instructions [instruction]}}}]
            (is (nil? (diagnostic #(bootstrap/p15-s23-closed-runtime-plan-validation! fixture-path :jvm guest))))
            (is (= [2 "" :normal] (evaluate instruction)))))
        (let [thrown (execute {:op :do :body [(say "before") (escape payload) (say "later")]})
              rejected (execute {:op :do :body [(say "before") {:op :unknown} (say "later")]})]
          (is (= #{:artifact :entrypoint :stdout :status :clojure-seed-boundary? :self-hosted? :thrown-value} (set (keys thrown))))
          (is (= #{:artifact :entrypoint :stdout :status :clojure-seed-boundary? :self-hosted? :diagnostic} (set (keys rejected))))
          (is (= :rejected (:status rejected)))
          (is (= "P15S23X002" (get-in rejected [:diagnostic :diagnostic])))
          (is (= :thrown (:status thrown))) (is (identical? payload (:thrown-value thrown)))
          (is (= "before\n" (:stdout thrown) (:stdout rejected))))
        (let [failure (AssertionError. "host primitive failure")
              value (proxy [Object] [] (toString [] (throw failure)))
              observed (seed-throw-test-capture #(evaluate {:op :builtin-call :function 'str :args [(literal value)]}))]
          (is (identical? failure observed)))
        (doseq [extension [".gravity" ".qst"]]
          (let [emitter-path (.getPath (io/file root "bootstrap/gravity/p15_s23/emitter.gravity"))
                emitter-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter emitter-path (slurp emitter-path))
                path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/direct-typed-throw" extension)))
                source (slurp path) artifact (bootstrap/macro-source-artifact path source)
                module (assoc (:module artifact) :forms (:expanded-forms artifact))
                definitions (bootstrap/stage0-function-table module)
                result (bootstrap/p15-s23-stage2-runtime-execute-function runtime emitter-plan 'p15-s23-compile-function
                         [emitter (assoc module :function-table definitions) (get definitions 'choose)])
                instruction (get-in result [:value :instructions 0]) payload (String. "problem")]
            (is (= "755e5b876b8fde5f1be48b0dce2cea96c17da24d967b4d9e641c720a46617f23" (bootstrap/sha256-hex source)))
            (doseq [[flag control] [[false :normal] [true :thrown]]]
              (let [result (invoke 'p15-s23-runtime-evaluate-closed-instruction instruction {'flag flag 'problem payload} "")]
                (is (identical? payload (first result)))
                (is (= ["" control] (subvec result 1)))))))
        (is (= 0 @seed-transports))))))

(deftest current-gravity-emitter-throw-development-kernel
  (let [fixture-path (module-fixture "seed-library" ".gravity")
        root (loop [directory (.getParentFile (io/file fixture-path))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        emitter-path (.getPath (io/file root "bootstrap/gravity/p15_s23/emitter.gravity"))
        text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! fixture-path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter emitter-path text)
        invoke (fn [function & args]
                 (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled function (vec args)))
        module {:module 'example.gravity.emitter :profile :meta :target :jvm :effects #{:error/throw} :capabilities #{}}
        lower #(invoke 'p15-s23-compile-expr emitter module '#{problem flag} %)
        execute #(bootstrap/p15-s23-stage2-runtime-execute-instruction runtime
                   {:source {:path fixture-path :sha256 "guest-source"} :module module} %2 %1)]
    ;; This raw plan names the actual changed source. Configured metadata does
    ;; not admit it, and no old sealed plan is patched to run these functions.
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
    (is (nil? (:scope compiled)))
    (is (not= (get-in compiled [:source :sha256])
              (var-get (module-private 'p15-s23-stage2-compiler-artifact-expected-source-content-hash))))
    (is (= compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter emitter-path text)))
    (doseq [form ['(throw) '(throw unknown extra) '(throw (recur false) extra)]]
      (is (= {:status :rejected :diagnostic "L2-UNKNOWN-CORE-FORM"
              :facts {:operator 'throw :reason :throw-arity :expected-arity 1 :actual-arity (dec (count form))}}
             (lower form))))
    (is (= "L2-UNKNOWN-SYMBOL" (:diagnostic (lower '(throw missing)))))
    (is (= (get-in emitter [:call-rules :unknown-callee-diagnostic])
           (:diagnostic (lower '(throw (missing))))))
    (is (= {:status :complete :value {:op :throw :value {:op :local :name 'problem}}}
           (lower '(throw problem))))
    (doseq [form ['(throw problem) '(do (throw problem)) '(if flag (throw problem) problem)
                  '(throw (throw problem))]]
      (let [lowered (lower form) instruction (:value lowered) payload (Object.)
            failure (seed-throw-test-capture #(execute instruction {'flag true 'problem payload}))]
        (is (= :complete (:status lowered)))
        (is (seed-throw-test-transport? failure))
        (is (identical? payload (:payload (ex-data failure))))))
    (doseq [data ['(throw unknown) {:kind :gravity/recur :values [false] :op :throw}
                  {:gravity.bootstrap/source-throw :data :payload :data}]]
      (let [instruction (:value (lower (list 'quote data)))]
        (is (= data (execute instruction {})))
        (is (= {:quote 1} (:value (invoke 'p15-s23-summary-instruction instruction {}))))))
    (is (= {:throw 2 :local 1}
           (:value (invoke 'p15-s23-summary-instruction (:value (lower '(throw (throw problem)))) {}))))
    (let [instruction (:value (lower '(if flag (throw (do problem)) (throw (throw problem)))))
          summary (:value (invoke 'p15-s23-summary-instruction instruction {}))
          products {:functions {} :binding-table [] :instruction-summary summary :preserved :original}
          result (:value (invoke 'p15-s23-complete-plan-products module products nil))]
      (is (= {:if 1 :local 3 :throw 3 :do 1} summary))
      (is (= products (dissoc result :entrypoint :effect-summary)))
      (is (= {:declared #{:error/throw} :inferred :pending-gravity-emitter-effect-inference
              :legality :not-checked :authority :non-authoritative :source-operation-occurrences {:throw 3} :capabilities #{}}
             (:effect-summary result))))
    ;; Ordinary products retain their complete prior shape and println-only
    ;; summary behavior. These are comparisons, not effect-legality evidence.
    (doseq [summary [{} {:println 1 :literal 1}]]
      (let [products {:functions {'f {:params []}} :binding-table [] :instruction-summary summary}]
        (is (= (assoc products :entrypoint 'main
                      :effect-summary {:declared #{:error/throw} :inferred (if (:println summary) #{:io/write} #{}) :capabilities #{}})
               (:value (invoke 'p15-s23-complete-plan-products module products 'main))))))
    (doseq [body ["(do (println \"ordinary\") \"done\")"
                 "(if false [1 2] {:result '(throw unknown)})"]]
      (let [source (str "(ns example.gravity.plain (:profile :meta) (:target :jvm)) (defn main [] " body ")")
            artifact (bootstrap/macro-source-artifact fixture-path source)
            ordinary-module (assoc (:module artifact) :forms (:expanded-forms artifact))
            definitions (bootstrap/stage0-function-table ordinary-module)
            ordinary-module (assoc ordinary-module :function-table definitions)
            seed (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter fixture-path source)
            expected (-> (select-keys seed [:functions :instruction-summary :effect-summary :entrypoint])
                         (assoc :binding-table (mapv #(assoc (:binding %) :arity (:arity %)) (vals (:functions seed))))
                         (assoc-in [:effect-summary :inferred]
                                   (if (pos? (get-in seed [:instruction-summary :println] 0)) #{:io/write} #{})))]
        (is (= {:status :complete :value expected}
               (invoke 'p15-s23-assemble-plan-products emitter ordinary-module (vec definitions))))))
    (doseq [extension [".gravity" ".qst"]]
      (let [source-path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/direct-typed-throw" extension)))
            source-text (slurp source-path)
            macro-artifact (bootstrap/macro-source-artifact source-path source-text)
            guest-module (assoc (:module macro-artifact) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table guest-module)
            guest-module (assoc guest-module :function-table definitions)
            definition (get definitions 'choose)
            lowered (invoke 'p15-s23-compile-function emitter guest-module definition)
            function (:value lowered) instruction (first (:instructions function)) payload (String. "problem")
            guest-context {:source {:path source-path :sha256 (str "sha256:" (bootstrap/sha256-hex source-text))}
                           :module (select-keys guest-module [:module :profile :target :effects :capabilities])}
            run #(bootstrap/p15-s23-stage2-runtime-execute-instruction runtime guest-context {'flag % 'problem payload} instruction)
            failure (seed-throw-test-capture #(run true))
            assembled (invoke 'p15-s23-assemble-functions emitter guest-module (vec definitions) {} [] {})
            completed (invoke 'p15-s23-complete-plan-products guest-module (:value assembled) nil)]
        (is (= "755e5b876b8fde5f1be48b0dce2cea96c17da24d967b4d9e641c720a46617f23" (bootstrap/sha256-hex source-text)))
        (is (= :complete (:status lowered) (:status assembled) (:status completed)))
        (is (= definition (dissoc function :binding :instructions)))
        (is (= '[flag problem] (:params function)))
        (is (= (:source-declarations definition) (:source-declarations function)))
        (is (some? (:source-declarations function)))
        (is (identical? payload (run false)))
        (is (seed-throw-test-transport? failure))
        (is (identical? payload (:payload (ex-data failure))))
        (is (= (:source guest-context) (:source (ex-data failure))))
        (is (= function (get-in assembled [:value :functions 'choose])))
        (is (= {:if 1 :local 3 :throw 1} (get-in assembled [:value :instruction-summary])))
        (is (= :pending-gravity-emitter-effect-inference (get-in completed [:value :effect-summary :inferred])))))))

(deftest seed-throw-lowering-and-arity-development-kernel
  (let [compiled (seed-throw-test-plan "(defn f [problem] (throw problem))")
        boundary (module-private 'p15-s23-stage2-seed-throw-plan-boundary)
        plain (seed-throw-test-plan "(defn f [problem] problem)")
        legacy (with-redefs-fn {boundary identity} #(seed-throw-test-plan "(defn f [problem] problem)"))]
    (is (= {:op :throw :value {:op :local :name 'problem}} (get-in compiled [:functions 'f :instructions 0])))
    (is (= {:throw 1 :local 1} (:instruction-summary compiled)))
    (is (= :clojure-stage0-seed (get-in compiled [:compiler :owner])))
    (is (= :temporary-single-source-seed-throw (get-in compiled [:compiler :lowering-boundary])))
    (is (= :pending-seed-effect-inference (get-in compiled [:effect-summary :inferred])))
    (is (= :not-checked (get-in compiled [:effect-summary :legality])))
    (is (= {:throw 1} (get-in compiled [:effect-summary :source-operation-occurrences])))
    (is (= #{:error/throw} (set (get-in compiled [:effect-summary :declared]))))
    (is (= plain legacy))
    (is (= :gravity-source (get-in plain [:compiler :owner])))
    (is (= #{} (get-in plain [:effect-summary :inferred]))))
  (doseq [form ["(throw)" "(throw unknown extra)" "(throw (recur false) extra)"]]
    (let [compiled-forms (atom []) original bootstrap/p15-s23-stage2-seed-compile-expr
          data (with-redefs [bootstrap/p15-s23-stage2-seed-compile-expr
                             (fn [& args] (swap! compiled-forms conj (last args)) (apply original args))]
                 (diagnostic #(seed-throw-test-plan (str "(defn f [] " form ")"))))]
      (is (= "L2-THROW-ILLEGAL" (:id data)))
      (is (= :throw-arity (:reason data)))
      (is (= 1 (:expected-arity data)))
      (is (= [] @compiled-forms))))
  (is (= "L2-RECUR-TARGET" (:id (diagnostic #(seed-throw-test-plan "(defn f [x] (throw (recur x)))")))))
  (let [compiled (seed-throw-test-plan "(defn f [] '(throw unknown))")]
    (is (= '(throw unknown) (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f [])))
    (is (= {:quote 1} (:instruction-summary compiled))))
  (is (= {:throw 2 :literal 1}
         (bootstrap/stage0-instruction-summary {:op :throw :value {:op :throw :value {:op :literal :value :payload}}}))))

(deftest seed-throw-transport-and-nesting-development-kernel
  (let [compiled (seed-throw-test-plan "(defn f [problem] (throw problem))")
        payloads [nil false {:kind :gravity/recur :values [false] :op :throw}
                  {:gravity.bootstrap/source-throw (Object.) :payload :data}
                  (ex-info "payload data" {:id "PAYLOAD"}) (AssertionError. "payload data")]]
    (is (true? (:private (meta (module-private 'p15-s23-stage2-runtime-source-throw!)))))
    (doseq [payload payloads]
      (let [failure (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f [payload]))]
        (is (seed-throw-test-transport? failure))
        (is (identical? payload (:payload (ex-data failure))))
        (is (= (:source compiled) (:source (ex-data failure))))
        (is (= 'example.seed.throw (:module (ex-data failure))))
        (is (nil? (:id (ex-data failure))))
        (is (not (identical? payload failure)))
        (is (identical? payload (bootstrap/p15-s23-stage2-runtime-execute-instruction runtime plan {} {:op :literal :value payload}))))))
  (let [compiled (seed-throw-test-plan "(defn f [] (throw (throw \"inner\")))")
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) original (var-get transport) seen (atom []) emitted (atom nil)
        failure (with-redefs-fn {transport (fn [active-plan payload]
                                           (swap! seen conj payload)
                                           (try (original active-plan payload)
                                                (catch clojure.lang.ExceptionInfo error (reset! emitted error) (throw error))))}
                  #(seed-throw-test-capture (fn [] (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f []))))]
    (is (seed-throw-test-transport? failure))
    (is (identical? @emitted failure))
    (is (= ["inner"] @seen))
    (is (= "inner" (:payload (ex-data failure)))))
  (doseq [instruction [{:op :throw} {:op :throw :value nil} {:op :throw :value false}
                       {:op :throw :value {:op :literal :value :payload} :extra true}]]
    (let [evaluated (atom 0) execute (module-private 'p15-s23-stage2-runtime-execute-value-scoped)
          data (with-redefs-fn {execute (fn [& _] (swap! evaluated inc))}
                 #(diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-instruction runtime plan {} instruction))))]
      (is (= "L2-THROW-ILLEGAL" (:id data)))
      (is (= :malformed-throw-instruction (:reason data)))
      (is (zero? @evaluated))))
  (is (= "L2-RECUR-TARGET" (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-instruction
                                              runtime plan {} {:op :throw :value {:op :recur :args []}}))))))

(deftest seed-throw-evaluation-order-development-kernel
  (let [mark (fn [value] {:op :builtin-call :function 'str :args [{:op :literal :value value}]})
        before (mark "before") payload (mark "problem") after (mark "after") throw {:op :throw :value payload}
        original bootstrap/p15-s23-stage2-runtime-invoke-builtin]
    (doseq [instruction [{:op :do :body [before throw after]}
                         {:op :vector-literal :items [before throw after]}
                         {:op :set-literal :items [before throw after]}
                         {:op :builtin-call :function 'str :args [before throw after]}
                         {:op :let :bindings [{:name 'x :expr before} {:name 'y :expr throw} {:name 'z :expr after}]
                          :body [after]}
                         {:op :map-literal :entries [{:key before :value throw} {:key after :value after}]}]]
      (let [seen (atom [])
            failure (with-redefs [bootstrap/p15-s23-stage2-runtime-invoke-builtin
                                 (fn [active-runtime active-plan function arguments]
                                   (swap! seen conj (first arguments)) (original active-runtime active-plan function arguments))]
                      (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-instruction runtime plan {} instruction)))]
        (is (seed-throw-test-transport? failure))
        (is (= "problem" (:payload (ex-data failure))))
        (is (= ["before" "problem"] @seen)))))
  (let [compiled (seed-throw-test-plan "(defn f [flag problem] (if flag (recur false problem) (throw problem)))")
        transfers (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
        failure (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                             (fn [values] (swap! transfers conj values) (original values))]
                  (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f [true "problem"])))]
    (is (= [[false "problem"]] @transfers))
    (is (seed-throw-test-transport? failure)))
  (let [compiled (seed-throw-test-plan "(defn fail [problem] (throw problem)) (defn f [] (loop [again true] (if again (fail \"problem\") \"done\")))")
        failure (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f []))]
    (is (seed-throw-test-transport? failure))
    (is (= "problem" (:payload (ex-data failure))))))

(deftest seed-throw-host-error-and-module-fences-development-kernel
  (doseq [failure [(RuntimeException. "host runtime") (ex-info "host diagnostic" {:id "HOST"}) (AssertionError. "host fatal")]]
    (let [carrier (ThrowingAssociative. failure)
          instruction {:op :throw :value {:op :builtin-call :function 'assoc :args (literal-instructions [carrier :key :value])}}
          observed (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-instruction runtime plan {} instruction))]
      (is (not (seed-throw-test-transport? observed)))
      (if (instance? RuntimeException failure)
        (if (instance? clojure.lang.ExceptionInfo failure) (is (identical? failure observed))
          (is (= "L2-BUILTIN-ERROR" (:id (ex-data observed)))))
        (is (identical? failure observed)))))
  (doseq [instruction [{:op :throw :value {:op :literal :value :payload}}
                       {:op :vector-literal :items [{:op :throw :value {:op :literal :value :payload}}]}]]
    (is (= :single-source-throw-only
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-instructions {:functions {'f {:instructions [instruction]}}})) [:facts :reason]))))
  (is (= [{:op :literal :value :payload}]
         (bootstrap/p15-s23-stage2-module-instruction-children {:op :throw :value {:op :literal :value :payload}})))
  (is (= [{:op :quote :value '(throw unknown)}]
         (bootstrap/p15-s23-stage2-module-instructions {:functions {'f {:instructions [{:op :quote :value '(throw unknown)}]}}})))
  (let [source {:source-path "module.gravity" :functions {} :literals {}
                :request {:references [{:position :operator :symbol 'throw :syntax-id :throw}]}
                :artifact {:binding-table {:bindings [{:binding-id :core-throw :binding-class :core}]}
                           :gravity-resolution-boundary {:resolved-analysis {:resolution-table [{:syntax-id :throw :binding-id :core-throw}]}}}}]
    (is (= :single-source-throw-only
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check! source {})) [:facts :reason])))
    (is (nil? (bootstrap/p15-s23-stage2-module-shadowing-check! (assoc-in source [:request :references] []) {})))))

(deftest seed-function-declaration-headers-development-kernel
  (let [table #(bootstrap/stage0-function-table {:source-path "seed-header.gravity" :forms [%]})]
    (is (true? (:private (meta (module-private 'normalize-seed-function-header)))))
    (doseq [[form params body declarations]
            [['(defn f [x :- Bool y] :- String y) '[x y] '(y) {:parameters '[x :- Bool y] :return '[:- String]}]
             ['(def f (fn [x :- Bool] x)) '[x] '(x) {:parameters '[x :- Bool] :return nil}]
             ['(defn f [x] :- String x) '[x] '(x) {:parameters '[x] :return '[:- String]}]
             ['(def f (fn [x :- Bool y :- String] :- String y)) '[x y] '(y)
              {:parameters '[x :- Bool y :- String] :return '[:- String]}]
             ['(def f (fn [x] (typed/return (quote String) (do x)))) '[x] '(x)
              {:parameters '[x] :return '[typed/return (quote String)]}]]]
      (let [definition (get (table form) 'f)]
        (is (= params (:params definition)))
        (is (= body (:body definition)))
        (is (= (count params) (:arity definition)))
        (is (= 1 (:body-form-count definition)))
        (is (= declarations (:source-declarations definition)))))
    (let [raw (bootstrap/function-definition-form "seed-header.gravity" '(defn f [x :- FutureType] :- String x))
          lowered (get (table '(defn f [x :- FutureType] :- String x)) 'f)]
      (is (= '[x :- FutureType] (:params raw)))
      (is (= '(:- String x) (:body raw)))
      (is (= '[x] (:params lowered)))
      (is (= '[x :- FutureType] (get-in lowered [:source-declarations :parameters]))))
    (doseq [form ['(defn f [x y] y) '(def f (fn [x y] y))]]
      (let [raw (bootstrap/function-definition-form "seed-header.gravity" form)]
        (is (= (assoc raw :arity 2 :body-form-count 1) (get (table form) 'f)))
        (is (not (contains? (get (table form) 'f) :source-declarations)))))))

(deftest seed-function-declaration-rejections-development-kernel
  (let [table #(bootstrap/stage0-function-table {:source-path "seed-header.gravity" :forms [%]})]
    (doseq [form ['(defn f [x :-] x) '(defn f [:- Bool] 1)
                  '(defn f [x :- :- Bool] x) '(defn f [x :- Bool :- String] x)
                  '(defn f [x :- (Vector String)] x) '(defn f [x :- :Bool] x)
                  '(defn f [x :- 7] x) '(defn f [] :-) '(defn f [] :- String)
                  '(defn f [] :- [String] 1) '(defn f [] :- :String 1)
                  '(defn f [] :- String :- Bool 1)
                  '(defn f [x x] x) '(defn f [x :- Bool x] x)
                  '(defn f [x :- Bool x :- String] x)
                  '(defn f [& xs] xs) '(defn f [x & xs] x) '(defn f [x :- Bool & xs] x)
                  '(def f (fn [x :-] x)) '(def f (fn [] :- String))
                  '(def f (fn [x] (typed/return String (do x))))
                  '(def f (fn [x] (typed/return (quote String extra) (do x))))
                  '(def f (fn [x] (typed/return (quote String) (identity x))))
                  '(def f (fn [x] (typed/return (quote String) (do x) extra)))
                  '(def f (fn [x] (typed/return (quote String) (do x)) x))
                  '(def f (fn [x] (typed/return (quote nil) (do x))))
                  '(def f (fn [x] (typed/return (quote false) (do x))))]]
      (is (= "L2-FUNCTION-PARAMS" (:id (diagnostic #(table form)))) (pr-str form)))
    (is (= :duplicate-parameter-names
           (:reason (diagnostic #(table '(defn f [x :- Bool x] x))))))
    (is (= :variadic-parameters-unsupported
           (:reason (diagnostic #(table '(defn f [x :- Bool & xs] x))))))
    (is (= "L2-DUPLICATE-FUNCTION"
           (:id (diagnostic #(bootstrap/stage0-function-table
                               {:source-path "seed-header.gravity"
                                :forms ['(defn f [x :-] x) '(defn f [] 1)]})))))))

(deftest seed-function-declaration-plan-parity-development-kernel
  (let [path (module-fixture "seed-library" ".gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        compile-source #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path %)
        plain "(ns example.seed.plain (:profile :meta) (:target :jvm)) (defn f [x] x)"
        normalizer (module-private 'normalize-seed-function-header)
        current (compile-source plain)
        legacy (with-redefs-fn {normalizer (fn [_ definition] definition)} #(compile-source plain))
        bool-source "(ns example.seed.annotated (:profile :meta) (:target :jvm)) (defn f [x :- Bool] :- Bool x)"
        string-source (str/replace bool-source "Bool" "String")
        bool-plan (compile-source bool-source) string-plan (compile-source string-source)]
    (is (= legacy current))
    (is (= (dissoc (get-in bool-plan [:functions 'f]) :source-declarations)
           (dissoc (get-in string-plan [:functions 'f]) :source-declarations)))
    (is (= {:parameters '[x :- Bool] :return '[typed/return (quote Bool)]}
           (get-in bool-plan [:functions 'f :source-declarations])))
    (is (not= (get-in bool-plan [:functions 'f :source-declarations])
              (get-in string-plan [:functions 'f :source-declarations])))
    (is (not= (:plan-id bool-plan) (:plan-id string-plan)))
    (is (not= (:source bool-plan) (:source string-plan)))
    (is (not= (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input bool-plan)
              (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input string-plan)))
    (let [executed? (atom false)
          data (diagnostic #(let [compiled (compile-source
                                            "(ns example.seed.bad (:profile :meta) (:target :jvm)) (defn f [x :-] (println \"BODY\"))")]
                              (reset! executed? true)
                              (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'f [true])))]
      (is (= "L2-FUNCTION-PARAMS" (:id data)))
      (is (false? @executed?)))
    (is (false? (bootstrap/p15-s23-stage2-module-fixed-definition? '(defn f [x :- Bool] x))))))

(deftest seed-function-declaration-macro-route-development-kernel
  (let [path (module-fixture "seed-library" ".gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        prefix "(ns example.seed.headers (:profile :meta) (:target :jvm)) "
        compile-source #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (str prefix %))]
    (doseq [[form declarations]
            [["(defn f [x :- Bool] :- Bool x)" {:parameters '[x :- Bool] :return '[typed/return (quote Bool)]}]
             ["(defn f [x] :- String x)" {:parameters '[x] :return '[typed/return (quote String)]}]
             ["(def f (fn [x :- Bool] :- Bool x))" {:parameters '[x :- Bool] :return '[:- Bool]}]]]
      (let [function (get-in (compile-source form) [:functions 'f])]
        (is (= '[x] (:params function)))
        (is (= '(x) (:body function)))
        (is (= declarations (:source-declarations function)))))
    (doseq [form ["(defn f [] :- nil 1)" "(defn f [] :- false 1)" "(defn f [] :-)"
                  "(defn f [] :- [String] 1)" "(defn f [] :- (Vector String) 1)"
                  "(defn f [] :- String :- Bool 1)"]]
      (is (= "L2-FUNCTION-PARAMS" (:id (diagnostic #(compile-source form)))) form))
    (is (= "L2-DUPLICATE-FUNCTION"
           (:id (diagnostic #(compile-source "(defn f [x :-] x) (defn f [] 1)")))))
    (let [emitted (bootstrap/p15-s23-stage2-plan-emitter-compile-source
                   emitter path (str (str/replace prefix ":meta" ":hosted") "(defn main [] :- String \"done\")"))]
      (is (= {:parameters [] :return '[typed/return (quote String)]}
             (get-in emitted [:functions 'main :source-declarations]))))
    (doseq [form ["(defn main [] :- nil 1)" "(defn main [] :- false 1)"]]
      (is (= "L2-FUNCTION-PARAMS"
             (:id (diagnostic #(bootstrap/p15-s23-stage2-plan-emitter-compile-source
                                emitter path (str (str/replace prefix ":meta" ":hosted") form))))) form))))

(deftest seed-shared-function-table-plain-parity-development-kernel
  (let [specs (var-get (module-private 'sh06-resolution-b51-module-specs))
        normalizer (module-private 'normalize-seed-function-header)]
    (is (= 7 (count specs)))
    (doseq [spec specs]
      (let [path (:logical-source-path spec)
            module (bootstrap/parse-module path (bootstrap/read-forms path (slurp path)))
            current (bootstrap/stage0-function-table module)
            previous (with-redefs-fn {normalizer (fn [_ definition] definition)}
                       #(bootstrap/stage0-function-table module))]
        (is (= previous current) path)
        (is (every? #(not (contains? % :source-declarations)) (vals current)) path)))))

(deftest seed-annotated-function-loop-isolation-development-kernel
  (let [path (module-fixture "seed-library" ".gravity")
        source "(ns example.seed.control (:profile :meta) (:target :jvm)) (defn again [flag :- Bool value :- String] :- String (if flag (recur false value) (loop [local true] (if local (recur false) value)))) (defn run [] :- String (again true \"done\"))"
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path source)
        transfers (atom []) original bootstrap/p15-s23-stage2-runtime-recur-signal
        value (with-redefs [bootstrap/p15-s23-stage2-runtime-recur-signal
                           (fn [values] (swap! transfers conj values) (original values))]
                (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'run []))]
    (is (= "done" value))
    (is (= [[false "done"] [false]] @transfers))
    (is (= '[flag value] (get-in compiled [:functions 'again :params])))
    (is (= 2 (get-in compiled [:functions 'again :arity])))))

(deftest compiler-module-literal-data-development-kernel
  (let [path (module-fixture "seed-library" ".gravity")
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        source "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog {:nested [nil true 7 \"value\" #{:a}]}) (def secret :private) (defn shadow [catalog] catalog) (defn main [value] [catalog (let [catalog value] catalog) (shadow value) (quote catalog) secret])"
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path source)]
    (is (= {:nested [nil true 7 "value" #{:a}]} (get-in compiled [:functions 'main :instructions 0 :items 0 :value])))
    (is (= [{:nested [nil true 7 "value" #{:a}]} :argument :argument 'catalog :private]
           (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled 'main [:argument])))
    (is (= :literal (get-in compiled [:functions 'main :instructions 0 :items 0 :op])))
    (is (= '(data (quote untouched) (dangerous call))
           (bootstrap/p15-s23-stage2-module-literal! '(quote (data (quote untouched) (dangerous call))))))
    (is (= {:symbol 'catalog :forms '(call argument)}
           (bootstrap/p15-s23-stage2-module-literal! '{:symbol (quote catalog) :forms (quote (call argument))})))
    (doseq [body ["(def catalog other)" "(def catalog (identity 1))" "(def catalog (quote data extra))"
                  "(def catalog {:nested [other]})" "(def catalog other) (def other 1)"
                  "(def catalog 1) (def catalog 2)" "(def catalog 1) (defn catalog [x] x)"]]
      (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                           emitter path (str "(ns example.literal (:profile :meta) (:target :jvm)) " body)))))))
    (is (= :unsupported-literal-data
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (nth (iterate vector :leaf) 66))) [:facts :reason])))
    (is (= :unsupported-literal-data
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (apply str (repeat 65537 "x")))) [:facts :reason])))
    (is (= :literal-data-size-limit
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literal! (vec (repeat 65537 nil)))) [:facts :reason])))
    (is (= :duplicate-module-definition
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-literals '[(def value 1) (defn value [x] x)])) [:facts :reason])))
    (is (= "L2-DUPLICATE-FUNCTION"
           (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                              emitter path "(ns example.literal (:profile :meta) (:target :jvm)) (defn same [x] x) (defn same [x] x)")))))
    (is (= 42 (bootstrap/p15-s23-stage2-runtime-execute-function
               runtime (bootstrap/p15-s23-stage2-compiler-artifact-plan
                        emitter path "(ns example.literal (:profile :meta) (:target :jvm)) (def identity (fn [x] x))")
               'identity [42])))
    (let [compile (fn [literal used?]
                    (bootstrap/p15-s23-stage2-compiler-artifact-plan
                     emitter path (str "(ns example.literal (:profile :meta) (:target :jvm)) (def catalog " literal
                                       ") (defn main [x] " (if used? "catalog" "x") ")")))
          unused-a (compile 1 false) unused-b (compile 2 false)
          used-a (compile 1 true) used-b (compile 2 true)]
      (is (not= (:plan-id unused-a) (:plan-id unused-b)))
      (is (= (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input unused-a)
             (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input unused-b)))
      (is (not= (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input used-a)
                (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input used-b))))))

(deftest compiler-module-literal-data-existing-source-kernel
  (doseq [relative ["gravity/src/gravity/checked_core.gravity"]]
    (let [path (str/replace (module-fixture "seed-library" ".gravity")
                           "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity" relative)
          text (slurp path)
          emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
          compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text)]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind compiled)))
      (is (seq (:functions compiled)))
      (is (seq (bootstrap/p15-s23-stage2-module-literals (bootstrap/read-forms path text))))
      (is (not (contains? compiled :literal-data)))))
  (let [path (str/replace (module-fixture "seed-library" ".gravity")
                          "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                          bootstrap/p15-s23-sh02-source-relative-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))]
    (is (= bootstrap/p15-s23-sh02-expected-plan-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (bootstrap/p15-s23-stage2-compiler-artifact-semantic-input compiled))))
    (is (= bootstrap/p15-s23-sh02-expected-functions-semantic-hash
           (bootstrap/p15-s23-c11-mir-digest (:functions compiled))))
    (is (= bootstrap/p15-s23-sh02-expected-plan-id
           (str "sha256:" (bootstrap/sha256-hex
                            (pr-str (bootstrap/c-backend-canonical-value
                                     (assoc-in (dissoc compiled :plan-id) [:source :byte-count]
                                               bootstrap/p15-s23-sh02-source-byte-count)))))))))

(deftest compiler-module-route-development-kernel
  ;; Normalized routing DATA, not authenticated module input or admission.
  (let [instruction {:op :compiler-module-call :route-id :route :function 'library/identity
                     :caller-function 'relay :callsite-path [0]
                     :args (literal-instructions [:left :right])}
        caller-plan {:module {:module 'example.caller} :functions {'relay {:arity 2 :params '[x y] :instructions [instruction]}}}
        callee-plan {:module {:module 'example.library} :functions {'identity {:arity 2 :params '[x y] :instructions [{:op :local :name 'y}]}}}
        exported {:binding-id :export :name 'identity :kind :function :visibility :public :namespace 'example.library}
        caller {:module-id :caller :plan caller-plan}
        callee {:module-id :callee :plan callee-plan :exports [exported]
                :resolution-artifact {:gravity-resolution-boundary {:resolved-analysis {:module-contract {:namespace 'example.library}}}}}
        route {:route-id :route :caller-module-id :caller :caller-function 'relay :callsite-path [0]
               :symbol 'library/identity :callee-module-id :callee :callee-binding-id :export :callee-function 'identity :arity 2}
        bundle {:modules [caller callee] :routes [route]}
        scope {:kind :gravity/stage2-module-invocation :bundle bundle :module-id :caller :function 'relay}
        execute (module-private 'p15-s23-stage2-module-execute-call)
        execute-value (module-private 'p15-s23-stage2-runtime-execute-value-scoped)
        original @execute-value seen (atom [])
        run (fn [p instruction s]
              (with-redefs-fn {execute-value (fn [r p e arg reason s]
                                              (when (= reason :recur-inside-function-argument) (swap! seen conj (:value arg)))
                                              (original r p e arg reason s))}
                #(execute runtime p {} instruction s)))]
    (is (= :right (run caller-plan instruction scope)))
    (is (= [:left :right] @seen))
    (doseq [[p i s] [[caller-plan (assoc instruction :route-id :wrong) scope]
                     [caller-plan (assoc instruction :function 'identity) scope]
                     [caller-plan (assoc instruction :args []) scope]
                     [caller-plan instruction (assoc scope :module-id :callee)]
                     [caller-plan instruction (assoc scope :function 'identity)]
                     [callee-plan instruction scope]
                     [caller-plan instruction (assoc-in scope [:bundle :routes 0 :arity] 3)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :visibility] :private)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :kind] :var)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :namespace] 'example.wrong)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :exports 0 :semantic-span :ordinary-module-artifact-id] :caller)]
                     [caller-plan instruction (assoc-in scope [:bundle :routes 0 :callee-binding-id] :wrong)]
                     [caller-plan instruction (assoc-in scope [:bundle :modules 1 :plan :functions 'identity :arity] 1)]]]
      (reset! seen [])
      (is (= "BOOT2006" (:id (diagnostic #(run p i s)))))
      (is (= [] @seen)))
    (reset! seen [])
    (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-instruction
                                          (assoc runtime :module-invocation scope) caller-plan {} instruction)))))
    (is (= [] @seen))
    (is (= "BOOT2006" (:id (diagnostic #(run caller-plan instruction (Object.))))))
    (doseq [key [:sh07-b51-runtime-context-token :sh07-b51-linked-carrier-v4 :sh07-b51-execution-bundle]]
      (is (= :admission-runtime-forbidden
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function
                                   (assoc runtime key {}) {:kind :gravity/stage2-compiler-artifact-module-bundle} 'relay [])) [:facts :reason]))))))

(deftest compiler-module-root-private-entry-development-kernel
  ;; Normalized development DATA; full reconstruction is exercised separately.
  (let [validate (module-private 'p15-s23-stage2-module-validate-bundle!)
        entry-scope (module-private 'p15-s23-stage2-module-entry-scope!)
        original-entry @entry-scope
        private {:arity 1 :params '[value] :instructions [{:op :local :name 'value}]}
        public {:arity 1 :params '[value] :instructions [{:op :function-call :function 'private-entry
                                                       :args [{:op :local :name 'value}]}]}
        root-plan {:module {:module 'example.root}
                   :functions (sorted-map 'private-entry private 'public-entry public 'catalog :data)}
        dependency {:module-id :dependency :plan {:functions {'dependency-only private}} :exports []}
        root {:module-id :root :plan root-plan :exports [{:name 'public-entry :kind :function} {:name 'catalog :kind :var}]}
        bundle {:kind :gravity/stage2-compiler-artifact-module-bundle :root-module-id :root :modules [dependency root] :routes []}
        seen (atom [])
        run (fn [b function args]
              (reset! seen [])
              (with-redefs-fn
                {validate (fn [value] (swap! seen conj :validated) value)
                 entry-scope (fn [p f as scope]
                               (let [next (original-entry p f as scope)]
                                 (swap! seen conj [f (:module-id next) (:function next)]) next))}
                #(bootstrap/p15-s23-stage2-runtime-execute-function runtime b function args)))]
    (is (= :private (run bundle 'private-entry [:private])))
    (is (= [:validated ['private-entry :root 'private-entry]] @seen))
    (is (= :public (run bundle 'public-entry [:public])))
    (is (= [:validated ['public-entry :root 'public-entry] ['private-entry :root 'private-entry]] @seen))
    (doseq [function ['missing 'dependency-only 'catalog 'library/identity 'example.root/private-entry
                      nil :private-entry 7 "private-entry" {} [] true (Object.)]]
      (is (= :root-function-member-required (get-in (diagnostic #(run bundle function [])) [:facts :reason])))
      (is (= [:validated] @seen)))
    (is (= "L2-FUNCTION-ARITY" (:id (diagnostic #(run bundle 'private-entry [])))))
    (is (= :runtime-plan-or-function-member
           (get-in (diagnostic #(run (assoc-in bundle [:modules 1 :plan :functions 'private-entry :arity] 2)
                                   'private-entry [1])) [:facts :reason])))
    (is (= :runtime-module-member
           (get-in (diagnostic #(run (assoc bundle :root-module-id :missing) 'private-entry [1])) [:facts :reason])))
    (is (= :runtime-module-member
           (get-in (diagnostic #(run (update bundle :modules conj root) 'private-entry [1])) [:facts :reason])))
    (with-redefs-fn {validate (fn [_] (throw (ex-info "Reconstruction denied" {:id :reconstruction-denied})))}
      #(is (= :reconstruction-denied
              (:id (diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle :bad [])))))))))

(deftest compiler-module-b51-dispatch-development-kernel
  (let [guard (module-private 'p15-s23-sh07-b51-assert-scoped-function-entry!)
        scoped (module-private 'p15-s23-stage2-runtime-execute-function-scoped)
        opaque (Object.) seen (atom nil)
        raw {:functions {'identity {:params [] :instructions [{:op :literal :value :raw}]}}}]
    (is (= :raw (with-redefs-fn {guard (fn [_ _ _ _ scope] (reset! seen scope))}
                  #(scoped runtime raw 'identity [] opaque))))
    (is (identical? opaque @seen)))
  (let [authorize (module-private 'p15-s23-sh07-b51-runtime-authorize-external-link)
        values (module-private 'p15-s23-stage2-runtime-execute-values-scoped)
        evaluated (atom 0)
        denied (with-redefs-fn {authorize (fn [& _] (throw (ex-info "strict route denied" {:id "STRICT-ROUTE"})))
                               values (fn [& _] (swap! evaluated inc) [])}
                 #(diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-instruction
                                      runtime plan {} {:op :function-call :function 'identity :external-link {}
                                                       :args [{:op :literal :value :argument}]}))))]
    (is (= "STRICT-ROUTE" (:id denied)))
    (is (zero? @evaluated))))

(deftest compiler-module-seed-shape-development-kernel
  (is (= :unsupported-nested-module-call
         (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-call-sites
                               {:name 'relay :body '[(let [x 1] (library/identity x))]})) [:facts :reason])))
  (is (bootstrap/p15-s23-stage2-module-fixed-definition? '(defn identity [x] x)))
  (doseq [form ['(defn collect [& xs] xs) '(defn identity [x :- I64] x) '(defn identity [x x] x)
                '(defn identity [x] :- I64 x) '(def initialized (identity 1))]]
    (is (not (bootstrap/p15-s23-stage2-module-fixed-definition? form))))
  (doseq [kind [:namespace :local]]
    (let [source {:request {:references [{:syntax-id :use :symbol 'str :position :operator}]}
                  :artifact {:binding-table {:bindings [{:binding-id :local-str :binding-class kind}]}
                             :gravity-resolution-boundary {:resolved-analysis {:resolution-table [{:syntax-id :use :binding-id :local-str}]}}}}]
      (is (= :unsupported-seed-operator-shadowing
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check!
                                   source {:call-rules {:builtin-functions '[str]}})) [:facts :reason])))))
  (let [source {:module {:namespace 'example.shadow} :functions {'helper {}}
                :request {:references [{:syntax-id :use :symbol 'helper :position :operator}]}
                :artifact {:binding-table {:bindings [{:binding-id :parameter :binding-class :local}]}
                           :gravity-resolution-boundary {:resolved-analysis {:resolution-table [{:syntax-id :use :binding-id :parameter}]}}}}]
    (is (= :unsupported-seed-operator-shadowing
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check! source {})) [:facts :reason]))))
  (doseq [instruction [{:op :function-call :function 'identity :external-link {}}
                       {:op :builtin-call :function 'get :compiler-internal-provenance {}}
                       {:op :builtin-call :function 'sh07-b51-admitted-executable-carrier-validation?}
                       {:op :literal :source-authority {}} {:op :literal :special-role :producer}]]
    (is (= :admission-instruction-forbidden
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-instructions
                                 {:functions {'identity {:instructions [instruction]}}})) [:facts :reason]))))
  (is (= [{:op :quote :value {:external-link {:special-role :data}}}]
         (bootstrap/p15-s23-stage2-module-instructions
          {:functions {'identity {:instructions [{:op :quote :value {:external-link {:special-role :data}}}]}}})))
  (doseq [inputs [nil true {} {:caller-resolution-artifact {} :dependency-plans true}
                 {:caller-resolution-artifact {} :dependency-plans [] :authority true}]]
    (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan {} "module.gravity" "" inputs))))))
  (let [source "(ns example.seed (:profile :meta) (:target :jvm)) (defn identity [x] x)"
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! "module.gravity" :jvm))
        forbidden (fn [& _] (throw (ex-info "Raw seed reached a checked frontdoor" {})))
        raw (with-redefs [bootstrap/compiler-c4-macro-source-artifact forbidden
                          bootstrap/compiler-c5-resolution-source-artifact forbidden
                          bootstrap/compiler-c6-lowering-source-artifact forbidden]
              (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "module.gravity" source))]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind raw)))
    (is (= :clojure-stage0-seed (get-in raw [:compiler :compiled-by])))
    (is (= {:nested [1 {:data #{:x :y}}]}
           (bootstrap/p15-s23-stage2-runtime-execute-function runtime raw 'identity [{:nested [1 {:data #{:x :y}}]}])))))

(deftest compiler-module-root-private-authenticated-paired-composition
  (let [validate (module-private 'p15-s23-stage2-module-validate-bundle!)
        original-validate @validate
        cases (mapv
                (fn [extension]
                  (let [library-path (module-fixture "seed-library" extension)
                        caller-path (module-fixture "private-root-consumer" extension)
                        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                        library (bootstrap/compiler-c5-resolution-file-artifact library-path)
                        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                        caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
                        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                        root (body-effects/module-root-entry bundle)
                        validations (atom 0) value {:nested [1 #{:value}]}]
                    (is (= :accepted (:status library) (:status caller)))
                    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
                    (is (= :not-checked (get-in bundle [:boundary :effects])))
                    (is (false? (get-in bundle [:boundary :self-hosted?])))
                    (is (= #{['public-entry :function] ['catalog :var]} (set (map (juxt :name :kind) (:exports root)))))
                    (is (= :private (:visibility (first (filter #(= 'private-relay (:name %)) (get-in caller [:binding-table :bindings]))))))
                    (is (map? (get-in root [:plan :functions 'private-relay])))
                    (is (= 'private-relay (get-in bundle [:routes 0 :caller-function])))
                    (with-redefs-fn {validate (fn [candidate] (swap! validations inc) (original-validate candidate))}
                      #(do
                         (is (= [:callee value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'private-relay [value])))
                         (is (= [:callee value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'public-entry [value])))
                         (is (= :root-function-member-required
                                (get-in (diagnostic (fn [] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'catalog []))) [:facts :reason])))))
                    (is (= 3 @validations))
                    {:bundle bundle :caller-path caller-path :c5-inputs c5-inputs :emitter emitter}))
                [".gravity" ".qst"])
        {:keys [bundle caller-path c5-inputs emitter]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (doseq [function ['identity 'library/identity 'example.private-root-consumer/private-relay :private-relay]]
      (is (= :root-function-member-required
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle function [])) [:facts :reason]))))
    (is (= "L2-FUNCTION-ARITY"
           (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'private-relay [])))))
    (is (= "C5-PRIVATE"
           (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                               caller-path (str/replace (slurp caller-path) "library/identity" "library/hidden") c5-inputs)))))
    (let [entries (mapv (fn [entry]
                          (if (= (:root-module-id bundle) (:module-id entry))
                            (let [changed (assoc-in (:plan entry) [:functions 'private-relay :instructions] [{:op :literal :value :forged}])
                                  changed (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex
                                             (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
                              (assoc entry :plan changed)) entry)) (:modules bundle))
          identity (bootstrap/p15-s23-stage2-module-identity emitter (:root-module-id bundle) entries (:routes bundle))
          forged (assoc bundle :modules entries :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'private-relay [1])) [:facts :reason]))))
    (let [other-id (:module-id (first (remove #(= (:root-module-id bundle) (:module-id %)) (:modules bundle))))
          identity (bootstrap/p15-s23-stage2-module-identity emitter other-id (:modules bundle) (:routes bundle))
          forged (assoc bundle :root-module-id other-id :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= "BOOT2006" (:id (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'hidden [1]))))))))

(deftest compiler-module-literal-data-authenticated-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-path (module-fixture "literal-library" extension)
                 caller-path (module-fixture "literal-consumer" extension)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 library (bootstrap/compiler-c5-resolution-file-artifact library-path)
                 module-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path module-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                 root (first (filter #(= (:root-module-id bundle) (:module-id %)) (:modules bundle)))
                 value {:argument [42 #{:nested}]}]
             (is (= :accepted (:status library) (:status caller)))
             (is (= [{:module :caller} value value 'catalog :caller-private]
                    (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'identity [value])))
             (is (= [{:module :library :nested [nil true 7 "value" #{:a :b}]} :library-private value]
                    (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [value])))
             (is (= #{['catalog :var] ['identity :function] ['relay :function]}
                    (set (map (juxt :name :kind) (:exports root)))))
             (is (= :private (:visibility (first (filter #(= 'secret (:name %)) (get-in caller [:binding-table :bindings]))))))
             {:bundle bundle :caller-path caller-path :emitter emitter :inputs inputs :module-inputs module-inputs}))
         [".gravity" ".qst"])
        {:keys [bundle caller-path emitter inputs module-inputs]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (is (= :root-function-member-required
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'catalog [])) [:facts :reason])))
    (doseq [mutate [(fn [entry] (assoc-in entry [:plan :functions 'identity :instructions 0 :items 0 :value] :tampered))
                    (fn [entry] (update entry :exports #(filterv (fn [binding] (not= :var (:kind binding))) %)))]]
      (let [tampered (update bundle :modules (fn [entries] (mapv #(if (= (:root-module-id bundle) (:module-id %)) (mutate %) %) entries)))]
        (is (= :module-bundle-reconstruction-mismatch
               (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime tampered 'identity [1])) [:facts :reason])))))
    (let [text (str/replace (slurp caller-path) "(library/identity value)" "library/catalog")
          artifact (bootstrap/compiler-c5-resolution-source-artifact caller-path text module-inputs)]
      (is (= :accepted (:status artifact)))
      (is (= :qualified-direct-import-calls-only
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                   emitter caller-path text (assoc inputs :caller-resolution-artifact artifact))) [:facts :reason]))))))

(deftest compiler-module-literal-data-actual-c8-composition
  (let [path (str/replace (module-fixture "seed-library" ".gravity")
                          "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                          "gravity/src/gravity/compiler/c8_effect_checker_engine.gravity")
        bundle (body-effects/compile-c8-module-bundle path)
        entry (body-effects/module-root-entry bundle)
        artifact (:resolution-artifact entry)
        literals (bootstrap/p15-s23-stage2-module-literals (bootstrap/read-forms path (slurp path)))
        catalogs '#{c8-effect-graph-contract c8-effect-legality-contract c8-capability-proof-contract
                    c8-build-effect-contract c8-replay-ordering-contract c8-effect-diagnostic-catalog}]
    (is (= :accepted (:status artifact)))
    (is (= catalogs (set (keys literals))))
    (is (= catalogs (set (map :name (filter #(= :var (:kind %)) (:exports entry))))))
    (is (= (count (get-in artifact [:gravity-resolution-boundary :resolved-analysis :module-contract :exports]))
           (count (:exports entry))))
    (let [result (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'verify-c8-effect-checker [:input])]
      (is (= :stage1-source-owned (:status result)))
      (is (= 'typed-core-artifact (:typed-core-artifact result)))
      (is (= 'c8-effect-diagnostic-catalog (:diagnostics result))))))

(deftest compiler-module-authenticated-source-and-runtime-composition
  (let [cases
        (mapv
         (fn [extension]
           (let [library-path (module-fixture "seed-library" extension)
                 caller-path (module-fixture "seed-consumer" extension)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 library (bootstrap/sh06-resolution-file-artifact library-path)
                 c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (slurp caller-path) inputs)
                 nested {:payload [1 {:nested #{:a :b}}] :symbol 'data/identity :quoted '(external-link :special-role)}]
             (is (= :accepted (:status library) (:status caller)))
             (is (= :gravity/stage2-compiler-artifact-module-bundle (:kind bundle)))
             (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
             (is (= :not-checked (get-in bundle [:boundary :effects])))
             (is (= 2 (count (:modules bundle)) (count (set (map :module-id (:modules bundle))))))
             (is (= 1 (count (:routes bundle))))
             (is (= :stage2-local (get-in dependency [:functions 'identity :binding :visibility])))
             (is (= [:callee 41] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [41])))
             (is (= [:callee nested] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [nested])))
             (is (= [:caller nested] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'identity [nested])))
             (is (= (:artifact-id library) (get-in bundle [:routes 0 :callee-module-id])))
             (is (= :public (:visibility (first (:exports (first (filter #(= (:artifact-id library) (:module-id %)) (:modules bundle))))))))
             {:bundle bundle :caller-path caller-path :library library :emitter emitter :inputs inputs :c5-inputs c5-inputs :dependency dependency}))
         [".gravity" ".qst"])
        {:keys [bundle caller-path library emitter inputs c5-inputs dependency]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (doseq [[name rule] [["hidden" "C5-PRIVATE"] ["missing" "C5-UNRESOLVED"]]]
      (is (= rule (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                                    caller-path (str/replace (slurp caller-path) "library/identity" (str "library/" name)) c5-inputs))))))
    (is (= :source-emitter-or-dependency-set
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                 emitter caller-path (str (slurp caller-path) " ") inputs)) [:facts :reason])))
    (let [changed (assoc-in dependency [:functions 'identity :instructions] [{:op :literal :value :forged}])
          forged (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex
                                                         (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
      (is (= :independent-raw-plan-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                   emitter caller-path (slurp caller-path) (assoc inputs :dependency-plans [forged]))) [:facts :reason]))))
    (let [forged (assoc-in bundle [:routes 0 :callee-module-id] (:root-module-id bundle))
          identity (assoc (:identity-input forged) :routes (:routes forged))
          forged (assoc forged :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'relay [41])) [:facts :reason]))))
    (doseq [body ["(defn collect [& xs] xs)"
                  "(defn str [x] x) (defn identity [x] (str x))"
                  "(defn identity [str] (str 2))"
                  "(defn helper [x] 1) (defn identity [helper] (helper 2))"]]
      (let [text (str "(ns example.seed-rejected (:profile :meta) (:target :jvm) (:exports ["
                      (if (str/includes? body "collect") "collect" "identity") "])) " body)
            artifact (bootstrap/compiler-c5-resolution-source-artifact caller-path text)]
        (is (= :accepted (:status artifact)))
        (if (str/includes? body "collect")
          (is (= :function-only-unannotated-seed-subset
                 (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-source artifact)) [:facts :reason])))
          (is (= :unsupported-seed-operator-shadowing
                 (get-in (diagnostic #(bootstrap/p15-s23-stage2-module-shadowing-check!
                                       (bootstrap/p15-s23-stage2-module-source artifact) emitter)) [:facts :reason]))))))))

(deftest small-argument-carriers-preserve-values-and-order
  (doseq [values [[] [1] [1 2] [1 2 3] [1 2 3 4]]]
    (is (= values
           (bootstrap/p15-s23-stage2-runtime-execute-values
            runtime plan {} (literal-instructions values) :test)))))

(deftest vector-and-list-instruction-bodies-remain-equivalent
  (let [instructions (literal-instructions [1 2 3 4])]
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} instructions)))
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} (apply list instructions))))))

(deftest non-tail-recur-remains-rejected-on-both-traversal-paths
  (let [instructions
        [{:op :recur :args [{:op :literal :value 1}]}
         {:op :literal :value :unreachable}]]
    (doseq [body [instructions (apply list instructions)]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-execute-instructions
               runtime plan {} body))]
        (is (= "L2-RECUR-TARGET" (:id data)))
        (is (= :non-tail-sequential-position (:reason data)))))))

(deftest recur-carrier-is-distinct-from-ordinary-runtime-maps
  (is (true?
       (bootstrap/p15-s23-stage2-runtime-recur-signal?
        (bootstrap/p15-s23-stage2-runtime-recur-signal [1 2 3]))))
  (doseq [value [{:values [1 2 3]}
                 {:p15-s23/recur-token (Object.) :values [1 2 3]}
                 (sorted-map 'artifact :unrelated)]]
    (is (false?
         (bootstrap/p15-s23-stage2-runtime-recur-signal? value)))))

(deftest hot-get-builtin-preserves-two-and-three-argument-semantics
  (is (= 1
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{:key 1} :key])))
  (is (= :missing
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{} :key :missing])))
  (testing "arity diagnostics remain fail closed"
    (doseq [arguments [[{}] [{} :key :fallback :extra]]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'get arguments))]
        (is (= "L2-BUILTIN-ARITY" (:id data)))
        (is (= (count arguments) (:actual-arity data)))))))

(deftest interpreted-get-avoids-carrier-without-changing-observable-semantics
  (let [invoke
        (fn [arguments]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args (literal-instructions arguments)}))]
    (is (= 1 (invoke [{:key 1} :key])))
    (is (= :missing (invoke [{} :key :missing])))
    (is (nil? (invoke [{} :key])))
    (is (nil? (invoke [{:key nil} :key :missing])))
    (is (nil? (invoke [nil :key])))
    (is (= :missing (invoke [nil :key :missing])))
    (testing "wrong arities retain generic diagnostics"
      (doseq [arguments [[] [{}] [{} :key :missing :extra]]]
        (let [data (diagnostic #(invoke arguments))]
          (is (= "L2-BUILTIN-ARITY" (:id data)))
          (is (= (count arguments) (:actual-arity data))))))
    (testing "lookup failures retain generic builtin error mapping"
      (let [arguments [(sorted-map 1 :one) :key]
            data (diagnostic #(invoke arguments))
            generic-data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'get arguments))]
        (is (= "L2-BUILTIN-ERROR" (:id data)))
        (is (= 'get (:function data)))
        (is (= generic-data data))))))

(deftest map-literal-carriers-preserve-order-duplicates-and-recur-boundaries
  (let [entry
        (fn [key value]
          {:key {:op :literal :value key}
           :value {:op :literal :value value}})
        instruction
        (fn [entries]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :map-literal :entries entries}))]
    (is (= {:first 1 :second 2}
           (instruction [(entry :first 1) (entry :second 2)])))
    (is (= {:same :last}
           (instruction [(entry :same :first) (entry :same :last)])))
    (is (= {:first 1 :second 2}
           (instruction (apply list [(entry :first 1) (entry :second 2)]))))
    (testing "malformed list entries fail closed instead of ending traversal"
      (doseq [entries [(list nil (entry :second 2))
                       (list false (entry :second 2))
                       (list (entry :first 1) nil)
                       (list (entry :first 1) false)]]
        (let [data (diagnostic #(instruction entries))]
          (is (= "L2-UNKNOWN-CORE-FORM" (:id data)) entries))))
    (testing "key evaluation precedes value evaluation and preserves recur rejection"
      (let [data
            (diagnostic
             #(instruction
               [{:key {:op :recur :args [{:op :literal :value :key}]}
                 :value {:op :literal :value :value}}]))]
        (is (= "L2-RECUR-TARGET" (:id data)))
        (is (= :recur-inside-map-key (:reason data)))))))

(deftest interpreted-get-evaluates-arguments-left-to-right-exactly-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function 'get
         :args [{:op :literal :value {:key 1}}
                {:op :literal :value :key}
                {:op :literal :value :missing}]}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             (:value argument))}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (= 1 result))
    (is (= [[{:key 1} :recur-inside-builtin-argument]
            [:key :recur-inside-builtin-argument]
            [:missing :recur-inside-builtin-argument]]
           @seen))))

(deftest interpreted-get-preserves-recur-and-exceptioninfo-boundaries
  (let [recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args [{:op :recur :args [{:op :literal :value {}}]}
                   {:op :literal :value :key}]}))
        exceptional
        (reify clojure.lang.ILookup
          (valAt [_ _]
            (throw (ex-info "lookup failed" {:id "TEST-LOOKUP"})))
          (valAt [_ _ _]
            (throw (ex-info "lookup failed" {:id "TEST-LOOKUP"}))))
        exception-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function 'get
            :args (literal-instructions [exceptional :key])}))]
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data)))
    (is (= "TEST-LOOKUP" (:id exception-data)))))

(deftest unary-collection-instructions-preserve-values-and-arity-diagnostics
  (let [invoke
        (fn [function arguments]
          (bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function function
            :args (literal-instructions arguments)}))]
    (is (= 2 (invoke 'count [[1 2]])))
    (is (= 1 (invoke 'first [[1 2]])))
    (is (= 2 (invoke 'second [[1 2]])))
    (is (= (list 2) (invoke 'rest [[1 2]])))
    (testing "wrong arities retain the generic fail-closed path"
      (doseq [[function arguments]
              [['count []]
               ['first [[1] [2]]]
               ['second []]
               ['rest [[1] [2]]]]]
        (let [data (diagnostic #(invoke function arguments))]
          (is (= "L2-BUILTIN-ARITY" (:id data)))
          (is (= (count arguments) (:actual-arity data))))))))

(def ^:private direct-unary-cases
  [['count [1 2] 2 false]
   ['first [1 2] 1 false]
   ['second [1 2] 2 false]
   ['rest [1 2] (list 2) false]
   ['symbol? 'value true true]
   ['keyword? :value true true]
   ['char? \x true true]
   ['number? 1 true true]
   ['seq? (list 1) true true]
   ['list? (list 1) true true]
   ['vector? [1] true true]
   ['map? {:key 1} true true]
   ['set? #{1} true true]
   ['string? "value" true true]
   ['even? 2 true true]
   ['integer? 2 true true]
   ['boolean? false true true]
   ['keys (array-map :a 1 :b 2) (list :a :b) true]
   ['set [1 1 2] #{1 2} true]
   ['sort-by-pr-str [2 1] (list 1 2) true]
   ['vec (list 1 2) [1 2] true]])

(deftest direct-unary-allowlist-matches-generic-builtin-semantics
  (doseq [[function value expected compiler-only?] direct-unary-cases]
    (let [active-plan (if compiler-only? compiler-plan plan)
          generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           active-plan function [value])
          interpreted (interpreted-builtin active-plan function [value])]
      (is (= expected generic) [function :generic])
      (is (= generic interpreted) [function :interpreted]))))

(deftest direct-unary-evaluates-pure-data-arguments-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value-scoped)
        calls (atom [])
        data
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env instruction reason _public-scope]
             (swap! calls conj [instruction reason])
             (:value instruction))}
          #(interpreted-builtin plan 'map? [{:evaluated true}]))]
    (is (= [[{:op :literal :value {:evaluated true}}
             :recur-inside-builtin-argument]]
           @calls))
    (is (true? data))
    (is (false? (bootstrap/p15-s23-stage2-compiler-artifact-plan-context? plan))))
  (testing "argument diagnostics propagate before builtin execution"
    (let [data
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-execute-instruction
             runtime plan {}
             {:op :builtin-call
              :function 'map?
              :args [{:op :local :name 'missing}]}))]
      (is (= "L2-UNKNOWN-SYMBOL" (:id data))))))

(deftest malformed-nonsymbol-callee-cannot-hash-before-argument-evaluation
  (let [hash-calls (atom 0)
        malformed
        (proxy [Object] []
          (hashCode []
            (swap! hash-calls inc)
            (throw (AssertionError. "callee hash ran before argument"))))
        data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id data)))
    (is (zero? @hash-calls)))
  (testing "dispatch failures after successful evaluation are contained"
    (let [hash-calls (atom 0)
          malformed
          (proxy [Object] []
            (hashCode []
              (swap! hash-calls inc)
              (throw (RuntimeException. "malformed callee hash"))))
          data (diagnostic #(interpreted-builtin plan malformed [:value]))]
      (is (= "L2-BUILTIN-ERROR" (:id data)))
      (is (identical? malformed (:function data)))
      (is (pos? @hash-calls)))))

(deftest unary-nonallowlisted-builtins-retain-generic-semantics
  (doseq [[function value expected]
          [['+ 7 7]
           ['* 7 7]
           ['= 7 true]
           ['vector 7 [7]]
           ['list 7 (list 7)]
           ['pr-str :value ":value"]]]
    (is (= expected (interpreted-builtin plan function [value])) function)
    (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
            plan function [value])
           (interpreted-builtin plan function [value]))
        function)))

(deftest direct-unary-preserves-recur-and-exception-boundaries
  (let [recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime compiler-plan {}
           {:op :builtin-call
            :function 'map?
            :args [{:op :recur :args [{:op :literal :value {}}]}]}))
        exceptional
        (reify clojure.lang.Seqable
          (seq [_]
            (throw (ex-info "sequence failed" {:id "TEST-SEQUENCE"}))))
        direct-data (diagnostic #(interpreted-builtin plan 'first [exceptional]))
        generic-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan 'first [exceptional]))]
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data)))
    (is (= "TEST-SEQUENCE" (:id direct-data)))
    (is (= generic-data direct-data))))

(deftest direct-unary-wraps-host-errors-identically-to-generic-dispatch
  (doseq [[function value active-plan]
          [['count 1 plan]
           ['first 1 plan]
           ['second 1 plan]
           ['rest 1 plan]
           ['even? "not-an-integer" compiler-plan]
           ['keys 1 compiler-plan]
           ['set (Object.) compiler-plan]
           ['sort-by-pr-str (Object.) compiler-plan]
           ['vec (Object.) compiler-plan]]]
    (let [direct (diagnostic #(interpreted-builtin active-plan function [value]))
          generic
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
             active-plan function [value]))]
      (is (= "L2-BUILTIN-ERROR" (:id direct)) function)
      (is (= generic direct) function))))

(deftest direct-unary-wrong-arities-remain-on-generic-path
  (doseq [[function value _expected compiler-only?] direct-unary-cases
          arguments [[] [value value]]]
    (let [active-plan (if compiler-only? compiler-plan plan)
          data (diagnostic #(interpreted-builtin active-plan function arguments))]
      (is (= "L2-BUILTIN-ARITY" (:id data)) [function arguments])
      (is (= (count arguments) (:actual-arity data)) [function arguments]))))

(deftest direct-binary-equality-matches-generic-semantics
  (doseq [[left right]
          [[nil nil]
           [nil false]
           [1 1]
           [1 1N]
           [1 2]
           [:value :value]
           ['value 'value]
           [[1 2] (list 1 2)]
           [(with-meta [1 2] {:side :left})
            (with-meta [1 2] {:side :right})]
           [[1 {:key :value}] [1 {:key :value}]]
           [{:key [1 2]} {:key [1 2]}]
           [#{1 2} #{2 1}]]]
    (let [generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [left right])
          interpreted (interpreted-builtin plan '= [left right])]
      (is (= generic interpreted) [left right])))
  (let [same-object (Object.)]
    (is (true? (interpreted-builtin plan '= [same-object same-object]))))
  (testing "equality remains valid outside compiler-artifact context"
    (is (true? (interpreted-builtin plan '= [:same :same])))
    (is (true?
         (interpreted-builtin {:compiler-artifact-plan? true}
                              '= [:same :same]))))
  (testing "list argument carriers retain the same semantics"
    (is (true?
         (bootstrap/p15-s23-stage2-runtime-execute-instruction
          runtime plan {}
          {:op :builtin-call
           :function '=
           :args (apply list (literal-instructions [[1] [1]]))})))))

(deftest direct-binary-equality-evaluates-left-to-right-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function '=
         :args [{:op :literal :value :left}
                {:op :literal :value :right}]}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             :same)}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (true? result))
    (is (= [[:left :recur-inside-builtin-argument]
            [:right :recur-inside-builtin-argument]]
           @seen))))

(deftest direct-binary-equality-preserves-argument-and-recur-boundaries
  (let [missing-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :local :name 'missing-left}
                   {:op :local :name 'missing-right}]}))
        recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :recur :args [{:op :literal :value :left}]}
                   {:op :local :name 'missing-right}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id missing-data)))
    (is (= 'missing-left (:symbol missing-data)))
    (is (= "L2-RECUR-TARGET" (:id recur-data)))
    (is (= :recur-inside-builtin-argument (:reason recur-data))))
  (let [right-recur-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function '=
            :args [{:op :literal :value :left}
                   {:op :recur :args [{:op :literal :value :right}]}]}))]
    (is (= "L2-RECUR-TARGET" (:id right-recur-data)))
    (is (= :recur-inside-builtin-argument (:reason right-recur-data))))
  (testing "a first-argument error prevents second-argument evaluation"
    (let [execute-value
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-execute-value)
          seen (atom [])
          data
          (with-redefs-fn
            {execute-value
             (fn [_runtime _plan _env argument _reason]
               (swap! seen conj (:value argument))
               (if (= :left (:value argument))
                 (throw (ex-info "first failed" {:id "TEST-EQUALITY-FIRST"}))
                 :unreachable))}
            #(diagnostic
              (fn []
                (interpreted-builtin plan '= [:left :right]))))]
      (is (= "TEST-EQUALITY-FIRST" (:id data)))
      (is (= [:left] @seen)))))

(deftest direct-binary-equality-preserves-error-mapping
  (let [runtime-error (RuntimeException. "host equality failed")
        hostile
        (proxy [Object] []
          (equals [_] (throw runtime-error)))
        direct-data (diagnostic #(interpreted-builtin plan '= [hostile :right]))
        generic-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [hostile :right]))]
    (is (= "L2-BUILTIN-ERROR" (:id direct-data)))
    (is (= '= (:function direct-data)))
    (is (= generic-data direct-data)))
  (let [info-error (ex-info "host equality diagnostic" {:id "TEST-EQUALITY"})
        hostile
        (proxy [Object] []
          (equals [_] (throw info-error)))
        observed
        (try
          (interpreted-builtin plan '= [hostile :right])
          nil
          (catch clojure.lang.ExceptionInfo error error))
        generic-observed
        (try
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan '= [hostile :right])
          nil
          (catch clojure.lang.ExceptionInfo error error))]
    (is (identical? info-error observed))
    (is (identical? info-error generic-observed))
    (is (identical? generic-observed observed)))
  (let [fatal-error (AssertionError. "host equality fatal")
        hostile
        (proxy [Object] []
          (equals [_] (throw fatal-error)))
        observed
        (try
          (interpreted-builtin plan '= [hostile :right])
          nil
          (catch AssertionError error error))]
    (is (identical? fatal-error observed))))

(deftest direct-binary-equality-keeps-other-arities-and-callees-generic
  (is (true? (interpreted-builtin plan '= [7])))
  (is (true? (interpreted-builtin plan '= [7 7 7])))
  (is (false? (interpreted-builtin plan '= [7 7 8])))
  (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan '= [7 7 7 7])
         (interpreted-builtin plan '= [7 7 7 7])))
  (is (= (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan '= [7 7 7 8])
         (interpreted-builtin plan '= [7 7 7 8])))
  (is (= "L2-BUILTIN-ARITY"
         (:id (diagnostic #(interpreted-builtin plan '= [])))))
  (let [hash-calls (atom 0)
        malformed
        (proxy [Object] []
          (hashCode []
            (swap! hash-calls inc)
            (throw (RuntimeException. "malformed binary callee"))))
        argument-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}
                   {:op :literal :value :right}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id argument-data)))
    (is (zero? @hash-calls))
    (let [dispatch-data
          (diagnostic #(interpreted-builtin plan malformed [:left :right]))]
      (is (= "L2-BUILTIN-ERROR" (:id dispatch-data)))
      (is (identical? malformed (:function dispatch-data)))
      (is (pos? @hash-calls)))))

(deftest direct-three-argument-assoc-matches-generic-semantics
  (doseq [arguments
          [[nil :key :value]
           [{} :key :value]
           [{:existing 1} :key [1 2]]
           [[0 1 2] 1 :replacement]
           [(with-meta {:existing 1} {:source :fixture}) :key :value]]]
    (let [generic
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan 'assoc arguments)
          interpreted (interpreted-builtin plan 'assoc arguments)]
      (is (= generic interpreted) arguments)
      (is (= (meta generic) (meta interpreted)) arguments)))
  (testing "assoc has no compiler-artifact context requirement"
    (is (= {:key :value}
           (interpreted-builtin plan 'assoc [{} :key :value])))
    (is (= {:key :value}
           (interpreted-builtin compiler-plan 'assoc [{} :key :value]))))
  (testing "a distinct but equal assoc symbol selects the direct operation"
    (is (= {:key :value}
           (interpreted-builtin plan (symbol "assoc")
                                [{} :key :value]))))
  (testing "list argument carriers retain the same semantics"
    (is (= {:key :value}
           (bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {}
            {:op :builtin-call
             :function 'assoc
             :args (apply list
                          (literal-instructions [{} :key :value]))})))))

(deftest direct-three-argument-assoc-evaluates-left-to-right-once
  (let [execute-value
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-execute-value)
        seen (atom [])
        instruction
        {:op :builtin-call
         :function 'assoc
         :args (literal-instructions [:collection :key :value])}
        result
        (with-redefs-fn
          {execute-value
           (fn [_runtime _plan _env argument reason]
             (swap! seen conj [(:value argument) reason])
             (case (:value argument)
               :collection {}
               :key :key
               :value :value))}
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            runtime plan {} instruction))]
    (is (= {:key :value} result))
    (is (= [[:collection :recur-inside-builtin-argument]
            [:key :recur-inside-builtin-argument]
            [:value :recur-inside-builtin-argument]]
           @seen))))

(deftest direct-three-argument-assoc-preserves-argument-and-recur-boundaries
  (doseq [[position expected]
          [[0 [:collection]]
           [1 [:collection :key]]
           [2 [:collection :key :value]]]]
    (let [execute-value
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-execute-value)
          labels [:collection :key :value]
          seen (atom [])
          marker (nth labels position)
          observed
          (with-redefs-fn
            {execute-value
             (fn [_runtime _plan _env argument _reason]
               (let [label (:value argument)]
                 (swap! seen conj label)
                 (if (= marker label)
                   (throw (ex-info "argument failed"
                                   {:id "TEST-ASSOC-ARGUMENT"
                                    :position position}))
                   (case label :collection {} :key :key :value :value))))}
            #(diagnostic
              (fn []
                (interpreted-builtin plan 'assoc labels))))]
      (is (= "TEST-ASSOC-ARGUMENT" (:id observed)) position)
      (is (= position (:position observed)) position)
      (is (= expected @seen) position)))
  (doseq [position (range 3)]
    (let [arguments
          (assoc (vec (literal-instructions [{} :key :value]))
                 position
                 {:op :recur
                  :args [{:op :literal :value position}]})
          data
          (diagnostic
           #(bootstrap/p15-s23-stage2-runtime-execute-instruction
             runtime plan {}
             {:op :builtin-call :function 'assoc :args arguments}))]
      (is (= "L2-RECUR-TARGET" (:id data)) position)
      (is (= :recur-inside-builtin-argument (:reason data)) position))))

(deftest direct-three-argument-assoc-preserves-error-mapping
  (let [assoc-var (ns-resolve 'clojure.core 'assoc)
        runtime-error (RuntimeException. "host assoc failed")
        direct
        (with-redefs-fn
          {assoc-var (fn [& _] (throw runtime-error))}
          #(diagnostic
            (fn [] (interpreted-builtin plan 'assoc [{} :key :value]))))
        generic
        (with-redefs-fn
          {assoc-var (fn [& _] (throw runtime-error))}
          #(diagnostic
            (fn []
              (bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'assoc [{} :key :value]))))]
    (is (= "L2-BUILTIN-ERROR" (:id direct)))
    (is (= 'assoc (:function direct)))
    (is (= generic direct)))
  (doseq [error [(ex-info "assoc diagnostic" {:id "TEST-ASSOC"})
                 (AssertionError. "assoc fatal")]]
    (let [assoc-var (ns-resolve 'clojure.core 'assoc)
          observed
          (with-redefs-fn
            {assoc-var (fn [& _] (throw error))}
            #(try
               (interpreted-builtin plan 'assoc [{} :key :value])
               nil
               (catch Throwable thrown thrown)))]
      (is (identical? error observed))))
  (testing "custom Associative throwables retain exact identity"
    (doseq [error [(ex-info "custom associative diagnostic"
                            {:id "TEST-ASSOCIATIVE"})
                   (AssertionError. "custom associative fatal")]]
      (let [carrier (ThrowingAssociative. error)
            direct
            (try
              (interpreted-builtin plan 'assoc [carrier :key :value])
              nil
              (catch Throwable thrown thrown))
            generic
            (try
              (bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'assoc [carrier :key :value])
              nil
              (catch Throwable thrown thrown))]
        (is (identical? error direct))
        (is (identical? error generic))
        (is (identical? generic direct))))))

(deftest direct-three-argument-assoc-keeps-other-arities-and-callees-generic
  (is (= {:a 1 :b 2}
         (interpreted-builtin plan 'assoc [{} :a 1 :b 2])))
  (doseq [arguments [[] [{}] [{} :key]
                     [{} :key :value :extra]
                     [{} :a 1 :b 2 :dangling]]]
    (let [data (diagnostic #(interpreted-builtin plan 'assoc arguments))]
      (is (= "L2-BUILTIN-ARITY" (:id data)) arguments)
      (is (= (count arguments) (:actual-arity data)) arguments)))
  (let [arguments [7 :key :value]
        generic (diagnostic
                 #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
                   plan 'assoc arguments))
        direct (diagnostic #(interpreted-builtin plan 'assoc arguments))]
    (is (= "L2-BUILTIN-ERROR" (:id direct)))
    (is (= generic direct)))
  (let [hash-calls (atom 0)
        equals-calls (atom 0)
        malformed
        (proxy [Object] []
          (equals [_]
            (swap! equals-calls inc)
            (throw (RuntimeException. "malformed assoc equality")))
          (hashCode []
            (swap! hash-calls inc)
            (throw (RuntimeException. "malformed assoc callee"))))
        argument-data
        (diagnostic
         #(bootstrap/p15-s23-stage2-runtime-execute-instruction
           runtime plan {}
           {:op :builtin-call
            :function malformed
            :args [{:op :local :name 'missing}
                   {:op :literal :value :key}
                   {:op :literal :value :value}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (:id argument-data)))
    (is (zero? @hash-calls))
    (is (zero? @equals-calls))
    (let [dispatch-data
          (diagnostic
           #(interpreted-builtin malformed malformed [{} :key :value]))]
      (is (= "L2-BUILTIN-ERROR" (:id dispatch-data)))
      (is (identical? malformed (:function dispatch-data)))
      (is (pos? @hash-calls))))
  (testing "persistent hostile callees cannot run equiv or hash before args"
    (doseq [arity [1 2 3]]
      (let [equiv-calls (atom 0)
            hash-calls (atom 0)
            failure (AssertionError. "hostile persistent callee")
            malformed
            (HostilePersistentCallee. equiv-calls hash-calls failure)
            arguments
            (into [{:op :local :name 'missing}]
                  (repeat (dec arity) {:op :literal :value :unreachable}))
            observed
            (try
              (bootstrap/p15-s23-stage2-runtime-execute-instruction
               runtime plan {}
               {:op :builtin-call
                :function malformed
                :args arguments})
              nil
              (catch Throwable thrown thrown))]
        (is (instance? clojure.lang.ExceptionInfo observed) arity)
        (is (= "L2-UNKNOWN-SYMBOL" (:id (ex-data observed))) arity)
        (is (zero? @equiv-calls) arity)
        (is (zero? @hash-calls) arity)))))

(deftest runtime-artifact-str-remains-on-specialized-generic-path
  (let [artifact-invoke
        (ns-resolve 'gravity.bootstrap
                    'p15-s23-stage2-runtime-artifact-invoke)
        calls (atom [])
        artifact-runtime (assoc runtime :runtime-artifact-plan true)
        invoke
        (fn [arguments]
          (with-redefs-fn
            {artifact-invoke
             (fn [_runtime function values]
               (swap! calls conj [function values])
               :artifact-result)}
            #(bootstrap/p15-s23-stage2-runtime-execute-instruction
              artifact-runtime plan {}
              {:op :builtin-call
               :function 'str
               :args (literal-instructions arguments)})))]
    (is (= :artifact-result (invoke ["a"])))
    (is (= :artifact-result (invoke ["a" "b"])))
    (is (= 2 (count @calls)))
    (is (= [["a"] ["a" "b"]] (mapv second @calls))))
  (testing "unary artifact exceptions retain the pre-fast-path boundary"
    (let [artifact-invoke
          (ns-resolve 'gravity.bootstrap
                      'p15-s23-stage2-runtime-artifact-invoke)
          artifact-runtime (assoc runtime :runtime-artifact-plan true)
          invoke
          #(bootstrap/p15-s23-stage2-runtime-execute-instruction
            artifact-runtime plan {}
            {:op :builtin-call
             :function 'str
             :args (literal-instructions [:value])})
          runtime-error (RuntimeException. "artifact runtime failure")
          observed-runtime
          (with-redefs-fn
            {artifact-invoke (fn [& _] (throw runtime-error))}
            #(try (invoke)
                  nil
                  (catch RuntimeException error error)))
          info-error (ex-info "artifact diagnostic" {:id "TEST-ARTIFACT"})
          observed-info
          (with-redefs-fn
            {artifact-invoke (fn [& _] (throw info-error))}
            #(try (invoke)
                  nil
                  (catch clojure.lang.ExceptionInfo error error)))]
      (is (identical? runtime-error observed-runtime))
      (is (identical? info-error observed-info)))))

(deftest small-function-and-loop-binders-preserve-scope-and-recur
  (let [function-plan
        {:source {:path "stage2-runtime-iteration-test.gravity"}
         :functions
         {'sum-to
          {:params ['n 'sum]
           :instructions
           [{:op :if
             :test {:op :builtin-call
                    :function '>
                    :args [{:op :local :name 'n}
                           {:op :literal :value 0}]}
             :then {:op :recur
                    :args
                    [{:op :builtin-call
                      :function '-
                      :args [{:op :local :name 'n}
                             {:op :literal :value 1}]}
                     {:op :builtin-call
                      :function '+
                      :args [{:op :local :name 'sum}
                             {:op :local :name 'n}]}]}
             :else {:op :local :name 'sum}}]}
          'loop-scope
          {:params ['outer]
           :instructions
           [{:op :loop
             :bindings [{:name 'index
                         :expr {:op :literal :value 0}}]
             :body
             [{:op :if
               :test {:op :builtin-call
                      :function '<
                      :args [{:op :local :name 'index}
                             {:op :literal :value 2}]}
               :then {:op :recur
                      :args [{:op :builtin-call
                              :function '+
                              :args [{:op :local :name 'index}
                                     {:op :literal :value 1}]}]}
               :else {:op :vector-literal
                      :items [{:op :local :name 'outer}
                              {:op :local :name 'index}]}}]}]}
          'caller
          {:params ['value]
           :instructions
           [{:op :function-call
             :function 'sum-to
             :args [{:op :local :name 'value}
                    {:op :literal :value 0}]}]}}}]
    (is (= 15
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'sum-to [5 0])))
    (is (= [9 2]
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'loop-scope [9])))
    (is (= 6
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'caller [3])))))

(deftest common-builtin-arities-preserve-hosted-core-semantics
  (let [compiler-plan
        {:compiler-artifact-plan? true
         :kind :gravity/stage2-compiler-artifact-plan
         :module {:profile :meta}
         :compiler {:stage :p15-s23-stage2-expression-lowering}
         :source {:path "stage2-runtime-iteration-test.gravity"}}
        invoke
        (fn [callee arguments]
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan callee arguments))]
    (doseq [[callee arguments expected]
            [['+ [] 0]
             ['+ [7] 7]
             ['+ [7 5] 12]
             ['+ [7 5 3] 15]
             ['+ [7 5 3 2] 17]
             ['- [7] -7]
             ['- [7 5] 2]
             ['- [7 5 3] -1]
             ['* [] 1]
             ['* [7] 7]
             ['* [7 5] 35]
             ['/ [4] 1/4]
             ['/ [12 3] 4]
             ['/ [24 3 2] 4]
             ['= [1] true]
             ['= [1 1] true]
             ['= [1 1 2] false]
             ['< [1 2] true]
             ['< [1 2 3] true]
             ['> [3 2 1] true]
             ['<= [1 1 2] true]
             ['>= [2 2 1] true]
             ['str [] ""]
             ['str ["a"] "a"]
             ['str ["a" 1] "a1"]
             ['str ["a" 1 :b] "a1:b"]
             ['first [[1 2]] 1]
             ['second [[1 2]] 2]
             ['count [[1 2]] 2]]]
      (is (= expected (invoke callee arguments))
          [callee arguments]))
    (is (= (list 2) (invoke 'rest [[1 2]])))
    (doseq [[callee arguments expected]
            [['symbol? ['value] true]
             ['keyword? [:value] true]
             ['vector? [[1 2]] true]
             ['map? [{:key 1}] true]
             ['contains? [{:key 1} :key] true]
             ['even? [2] true]
             ['integer? [2] true]
             ['keys [{:key 1}] (list :key)]
             ['quot [7 2] 3]
             ['subvec [[1 2 3] 1 3] [2 3]]]]
      (is (= expected
             (bootstrap/p15-s23-stage2-runtime-invoke-builtin
              compiler-plan callee arguments))
          [callee arguments]))
    (doseq [[callee arguments]
            [['- []] ['/ []] ['= []] ['< [1]] ['> [1]]]]
      (is (= "L2-BUILTIN-ARITY"
             (:id (diagnostic #(invoke callee arguments))))))))
