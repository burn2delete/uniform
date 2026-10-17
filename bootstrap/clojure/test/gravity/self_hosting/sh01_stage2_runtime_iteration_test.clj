(ns gravity.self-hosting.sh01-stage2-runtime-iteration-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.walk :as walk]
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
        forms (mapv #(assoc % :parent-form-id (case (:form-id %) :definition-form nil :function-form :definition-form
                                                :parameter-name :parameter-form :body-form (if throw? :throw-form :function-form)
                                                :function-form)) forms)
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
        core {:artifact-id :core :module core-module :nodes nodes :function-records [record] :root-core-node-ids [:definition]}
        reader {:source-unit-record {:path source-path :bytes-hash (str "sha256:" (bootstrap/sha256-hex source-text))}
                :token-stream [{:raw source-text}]}
        macro-artifact {:expanded-forms expanded :gravity-macro-boundary {:authenticated-sh04-artifact {:c2-reader-artifact reader}}}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:module core-module :forms forms :binding-table bindings :top-level-form-ids [:definition-form]}
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

(defn- typed-raw-literal-kernel-data [raw-value canonical-value kind type]
  (let [{:keys [module input observation]} (typed-lowering-kernel-data)
        core (:canonical-core-artifact (:request input))
        record (-> (first (:function-records core)) (dissoc :signature) (assoc :body-core-node-id :literal :parameter-binding-ids [] :fixed-arity 0))
        function (-> (first (:function-type-table (:typed input))) (dissoc :signature)
                     (assoc :body-core-node-id :literal :parameters [] :parameter-binding-ids [] :fixed-arity 0 :return type))
        raw-fn (list 'fn [] raw-value) raw-def (list 'def 'identity raw-fn)
        canonical-fn (list 'fn [] canonical-value) canonical-def (list 'def 'identity canonical-fn)
        source {:form-id :body-form :syntax-id :body :semantic-span {:byte-start 1 :byte-end 5}
                :origin-chain [] :generated-origin []}
        nodes [{:node-id :literal :core-form :literal :children [] :attributes {:literal-kind kind :value canonical-value} :source source}
               (-> (get (:nodes core) 1) (assoc :children [:literal] :resolved-binding-ids [] :attributes {:parameters []}))
               (last (:nodes core))]
        forms [{:form-id :definition-form :syntax-id :def :value canonical-def :parent-form-id nil :child-form-ids [:def-op :name-form :function-form]}
               {:form-id :function-form :syntax-id :fn :value canonical-fn :parent-form-id :definition-form :child-form-ids [:fn-op :parameter-form :body-form]}
               {:form-id :parameter-form :syntax-id :parameters :value [] :parent-form-id :function-form :child-form-ids []}
               {:form-id :body-form :syntax-id :body :kind kind :value canonical-value :parent-form-id :function-form}]
        core (assoc core :nodes nodes :function-records [record])
        typed (assoc (:typed input) :function-type-table [function] :type-table {:literal type :function function :definition function})
        report {:artifact :gravity/sh08-function-type-verification :status :passed :expected typed :candidate typed}
        text (str "(ns example.typed (:profile :meta) (:target :jvm) (:effects #{}) (:capabilities #{}) (:safety :safe))\n" (pr-str raw-def))
        hash (str "sha256:" (bootstrap/sha256-hex text)) module (assoc module :forms [raw-def])
        definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
        input (-> input (assoc :typed typed :verification report)
                  (assoc-in [:request :canonical-core-artifact] core)
                  (assoc-in [:request :b47-context :authenticated-core-request :forms] forms)
                  (assoc-in [:request :b47-context :authenticated-core-request :binding-table] [])
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :expanded-forms] [raw-def])
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
                             :authenticated-sh04-artifact :c2-reader-artifact :source-unit-record :bytes-hash] hash)
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
                             :authenticated-sh04-artifact :c2-reader-artifact :token-stream] [{:raw text}]))
        observation (-> observation (assoc :verification report) (assoc-in [:source :text] text) (assoc-in [:source :sha256] hash))]
    {:module module :input input :observation observation :definition (get definitions 'identity) :source source}))

(deftest current-gravity-raw-root-source-development-kernel
  ;; Actual current Gravity consumer algorithms over source-linked normalized
  ;; DATA. The separately supplied report is not an authentication claim.
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        descriptor {:kind :gravity/arbitrary-decimal-literal :unscaled-value "125" :scale 2
                    :literal-id :literal :form-id :original-form :token-id :token :projection-form-id :body-form}
        {:keys [module input observation definition source]} (typed-raw-literal-kernel-data (java.math.BigDecimal. "1.25") descriptor :decimal :gravity.type/decimal)
        checked (invoke 'p15-s23-typed-context module input observation) context (:value checked)
        compile #(invoke 'p15-s23-typed-compile-function emitter %1 %2 %3 observation)
        result (compile module definition input)]
    (is (= :complete (:status checked)))
    (is (= (first (:forms module)) (get-in context [:raw-roots :definition-form])))
    (is (true? (invoke 'p15-s23-typed-module-table? module context)))
    (is (= definition (get-in (invoke 'p15-s23-typed-definition context 'identity) [:value :definition])))
    (is (= :rejected (:status result)))
    (is (= :unelaborated-typed-numeric-literal (get-in result [:facts :reason])))
    (is (= :literal (get-in result [:facts :core-node-id])))
    (is (= (:semantic-span source) (get-in result [:facts :source-span])))
    (is (= (select-keys source [:syntax-id :origin-chain :generated-origin])
           (select-keys (:facts result) [:syntax-id :origin-chain :generated-origin])))
    (doseq [change [#(assoc-in % [:request :b47-context :authenticated-core-request :top-level-form-ids] [])
                    #(assoc-in % [:request :b47-context :authenticated-core-request :top-level-form-ids] [:wrong])
                    #(assoc-in % [:request :b47-context :authenticated-core-request :top-level-form-ids] [:definition-form :definition-form])
                    #(assoc-in % [:request :b47-context :authenticated-core-request :top-level-form-ids] '(:definition-form))
                    #(assoc-in % [:request :canonical-core-artifact :root-core-node-ids] [])
                    #(assoc-in % [:request :canonical-core-artifact :root-core-node-ids] [:function])
                    #(assoc-in % [:request :canonical-core-artifact :nodes 2 :source :form-id] :wrong)
                    #(assoc-in % [:request :b47-context :authenticated-core-request :forms 0 :parent-form-id] :wrong)
                    #(assoc-in % [:request :b47-context :authenticated-core-request :forms 1 :parent-form-id] nil)]]
      (is (= :rejected (:status (invoke 'p15-s23-typed-context module (change input) observation)))))
    (is (= :typed-module-function-table-mismatch
           (get-in (compile (assoc-in module [:function-table 'identity :body] [descriptor]) definition input) [:facts :reason])))
    (is (= :supplied-definition-does-not-match-typed-source
           (get-in (compile module (assoc definition :body [descriptor]) input) [:facts :reason])))
    (doseq [bad [(assoc-in context [:raw-roots :definition-form] '(def other (fn [] 1.25M)))
                 (assoc-in context [:raw-roots :definition-form] '(def identity (other [] 1.25M)))
                 (assoc-in context [:raw-roots :definition-form] '(def identity (fn [extra] 1.25M)))
                 (assoc-in context [:raw-roots :definition-form] '(def identity (fn [] :- String 1.25M)))
                 (assoc-in context [:records 'identity :signature :body-source-child-index] 3)
                 (assoc-in context [:forms :function-form :parent-form-id] :wrong)
                 (assoc-in context [:forms :body-form :parent-form-id] :wrong)]]
      (is (= :rejected (:status (invoke 'p15-s23-typed-definition bad 'identity)))))
    (is (= :typed-lowering-work-limit
           (get-in (invoke 'p15-s23-typed-context module
                     (assoc-in input [:request :b47-context :authenticated-core-request :top-level-form-ids] (vec (repeat 65537 :definition-form))) observation)
                   [:facts :reason])))
    (let [raw ['(def z (fn [] "z")) '(def a (fn [] "a"))]
          nodes {:n1 {:source {:form-id :r1}} :n2 {:source {:form-id :r2}}}
          forms {:r1 {:parent-form-id nil} :r2 {:parent-form-id nil}}]
      (is (= {:r1 (first raw) :r2 (second raw)} (invoke 'p15-s23-typed-raw-roots raw [:r1 :r2] [:n1 :n2] nodes forms)))
      (is (nil? (invoke 'p15-s23-typed-raw-roots raw [:r2 :r1] [:n1 :n2] nodes forms)))
      (is (nil? (invoke 'p15-s23-typed-raw-roots raw [:r1 :r1] [:n1 :n2] nodes forms))))
    (let [{:keys [module input observation definition]} (typed-raw-literal-kernel-data "ordinary" "ordinary" :string :gravity.type/string)
          result (invoke 'p15-s23-typed-compile-function emitter module definition input observation)]
      (is (= :complete (:status result)))
      (is (= [{:op :literal :value "ordinary"}] (get-in result [:value :instructions]))))
    (let [{:keys [module input observation]} (typed-lowering-kernel-data)
          base (:value (invoke 'p15-s23-typed-context module input observation))
          parameters '[problem :- String other :- String]
          raw-fn (list 'fn parameters :- 'String 'problem) raw-def (list 'def 'identity raw-fn)
          declaration (get-in base [:records 'identity :signature :parameters 0])
          signature (assoc (get-in base [:records 'identity :signature])
                           :parameters [declaration declaration] :parameter-source-child-indexes [0 3])
          record (assoc (get-in base [:records 'identity]) :signature signature :parameter-binding-ids [:parameter :other] :fixed-arity 2)
          function (assoc (get-in base [:functions 'identity]) :signature signature :parameter-binding-ids [:parameter :other]
                          :fixed-arity 2 :parameters [{:binding-id :parameter :type :gravity.type/string} {:binding-id :other :type :gravity.type/string}])
          context (-> base (assoc-in [:raw-roots :definition-form] raw-def)
                           (assoc-in [:records 'identity] record) (assoc-in [:functions 'identity] function)
                           (assoc-in [:types :function] function) (assoc-in [:types :definition] function)
                           (assoc-in [:nodes :function :attributes :signature] signature)
                           (assoc-in [:nodes :function :attributes :parameters] '[problem other])
                           (assoc-in [:nodes :function :resolved-binding-ids] [:parameter :other])
                           (assoc-in [:forms :definition-form :value] raw-def) (assoc-in [:forms :function-form :value] raw-fn)
                           (assoc-in [:forms :parameter-form :value] parameters)
                           (assoc-in [:forms :parameter-form :child-form-ids] [:parameter-name :parameter-marker :type-form :other-name :other-marker :type-form])
                           (assoc-in [:forms :other-name] {:syntax-id :other-name :value 'other :scope-id :function-scope})
                           (assoc-in [:bindings :other] {:binding-id :other :name 'other :kind :local :binding-class :lexical
                                                       :definition-syntax-id :other-name :scope-id :function-scope}))
          expected {:name 'identity :params '[problem other] :body ['problem] :definition-form :def-fn :arity 2 :body-form-count 1
                    :source-declarations {:parameters parameters :return '[:- String]}}
          joined (invoke 'p15-s23-typed-definition context 'identity)]
      (is (= :complete (:status joined)))
      (is (= expected (get-in joined [:value :definition])))
      (is (true? (invoke 'p15-s23-typed-module-table? (assoc module :function-table {'identity expected}) context)))
      (is (= [{:op :local :name 'problem}]
             (get-in (invoke 'p15-s23-typed-lower-function module expected context) [:value :instructions]))))))

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

(defn- typed-recursive-quote-kernel-context [base kind value type]
  ;; Root-only normalized projection records. Recursive C7 verification is a precondition.
  (let [context (typed-symbol-quote-kernel-context base)
        ids (if (contains? #{:list :vector :map :set} kind) [:data-child] [])
        attrs {:quoted-form-id :symbol-payload :quoted-syntax-id :symbol-payload :quoted-kind kind :quoted-value value}
        node (assoc-in (get-in context [:nodes :symbol-quote]) [:attributes] attrs)
        data (-> (get-in context [:ledger 0 :quoted-data])
                 (assoc :kind kind :value value :type type)
                 (assoc-in [:origin :child-form-ids] ids))
        premise {:core-node-id :symbol-quote :source (:source node) :attributes attrs :children []
                 :resolved-binding-ids [] :evaluation (:evaluation node)}
        row (-> (get-in context [:ledger 0])
                (assoc :rhs type :quoted-data data :dependencies {:quote premise :data data}
                       :solution {:type type :evaluation :no-evaluation}
                       :constraint-id-request {:domain :gravity/sh08-recursive-quoted-data-constraint-v1 :quote premise :data data}))]
    (-> context (assoc :ledger [row]) (assoc-in [:nodes :symbol-quote] node) (assoc-in [:types :symbol-quote] type)
        (assoc-in [:forms :quote-form :value] (list 'quote value))
        (update-in [:forms :symbol-payload] assoc :kind kind :value value :child-form-ids ids))))

(deftest current-gravity-recursive-quote-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))
        cases [[:nil nil :gravity.type/nil] [:boolean false :gravity.type/bool]
               [:integer 17 :gravity.type/integer] [:character \a :gravity.type/character]
               [:string "text" :gravity.type/string] [:keyword :data/key :gravity.type/keyword]
               [:symbol 'unresolved/data :gravity.type/symbol]
               [:list '(throw unresolved/data) {:kind :gravity.type/list}]
               [:vector '[nil true 17 \a "text" :key unresolved/data] {:kind :gravity.type/vector}]
               [:map '{unresolved/key [true {:nested ready}]} {:kind :gravity.type/map}]
               [:set '#{unresolved/data :key} {:kind :gravity.type/set}]
               [:list '() {:kind :gravity.type/list}] [:vector [] {:kind :gravity.type/vector}]
               [:map {} {:kind :gravity.type/map}] [:set #{} {:kind :gravity.type/set}]
               [:list '(quote nested) {:kind :gravity.type/list}]
               [:vector '[() [] {} #{}] {:kind :gravity.type/vector}]]]
    (doseq [[kind value type] cases]
      (let [context (typed-recursive-quote-kernel-context base kind value type)
            result (invoke 'p15-s23-typed-compile-node context :symbol-quote 4)]
        (is (= :complete (:status result)))
        (is (= :quote (get-in result [:value :op])))
        (is (identical? value (get-in result [:value :value])))
        (is (= 4 (:work-count result)))
        (is (= :typed-lowering-work-limit
               (get-in (invoke 'p15-s23-typed-compile-node context :symbol-quote 3) [:facts :reason])))))
    (let [context (typed-recursive-quote-kernel-context base :map '{data/key [ready]} {:kind :gravity.type/map})]
      (doseq [mutate [#(assoc % :ledger []) #(update % :ledger into (:ledger %))
                      #(assoc-in % [:ledger 0 :lhs] :other) #(assoc-in % [:ledger 0 :status] :pending)
                      #(assoc-in % [:ledger 0 :quoted-data :kind] :decimal)
                      #(assoc-in % [:ledger 0 :quoted-data :origin :data-path] [0])
                      #(assoc-in % [:ledger 0 :quoted-data :origin :quote-core-node-id] :other)
                      #(assoc-in % [:ledger 0 :quoted-data :origin :form-id] :other)
                      #(assoc-in % [:ledger 0 :quoted-data :source :syntax-id] :other)
                      #(assoc-in % [:ledger 0 :dependencies :quote :source :syntax-id] :other)
                      #(assoc-in % [:types :symbol-quote] :gravity.type/string)
                      #(assoc-in % [:nodes :symbol-quote :attributes :quoted-kind] :list)
                      #(assoc-in % [:nodes :symbol-quote :resolved-binding-ids] [:binding])
                      #(assoc-in % [:nodes :symbol-quote :children] [:executable])
                      #(assoc-in % [:forms :quote-op :value] 'unquote)
                      #(assoc-in % [:forms :symbol-payload :parent-form-id] :sibling)]]
        (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node (mutate context) :symbol-quote 4))))))
    ;; Opaque sentinels test cost only; they are not claimed as public accepted DATA.
    (let [equiv-calls (atom 0) hash-calls (atom 0) failure (AssertionError. "recursive quote projection inspected DATA")
          opaque (HostilePersistentCallee. equiv-calls hash-calls failure)
          type {:kind :gravity.type/vector :element-types opaque}
          context (typed-recursive-quote-kernel-context base :vector opaque type)
          result (invoke 'p15-s23-typed-compile-node context :symbol-quote 4)]
      (is (= :complete (:status result))) (is (identical? opaque (get-in result [:value :value])))
      (is (= 4 (:work-count result))) (is (= 0 @equiv-calls @hash-calls)))
    (let [forms (bootstrap/read-forms path (slurp path))
          helper (first (filter #(= 'p15-s23-typed-quote (second %)) forms))
          calls (set (keep #(when (seq? %) (first %)) (tree-seq coll? seq helper)))]
      (is (not-any? calls '[p15-s23-typed-quote p15-s23-typed-compile-children p15-s23-typed-source
                           vec list hash str pr-str map set into]))
      (is (some #{'(get data :value)} (tree-seq coll? seq helper))))))

(declare typed-catch-runtime-projection)

(deftest current-gravity-recursive-quote-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        expected '{quoted-nil nil quoted-boolean false quoted-integer 17 quoted-character \a
                   quoted-string "text" quoted-keyword :data/key quoted-symbol unresolved/data
                   quoted-list (throw unresolved/data) quoted-vector [nil true 17 \a "text" :key unresolved/data]
                   quoted-map {unresolved/key [true {:nested ready}]} quoted-set #{unresolved/data :key}
                   empty-data [() [] {} #{}] nested-quote (quote nested) let-forward {:payload [unresolved/data]}
                   same-module-forward {unresolved/key [true {:nested ready}]} main {unresolved/key [true {:nested ready}]}}
        cases
        (mapv
         (fn [extension]
           (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-quoted-data" extension)
                 path (.getPath (io/file root relative)) text (slurp path)
                 products (fresh relative) input (select-keys products [:request :typed :verification])
                 macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                 parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                 module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                 definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                 standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'quoted-map) input))
                 assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                 facts (filterv #(= :sh08-recursive-quoted-data-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                 results (into {} (map (fn [[name value]]
                                        (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                                       [{:op :function-call :function name :args []} {} "" (:functions assembled) []])]
                                          (is (= [value "" :normal] result)) [name (first result)])) expected))]
             (is (= :accepted (get-in input [:typed :status])))
             (is (= :passed (get-in input [:verification :status])))
             (is (= 16 (count definitions) (count (:functions assembled))))
             (is (= standalone (get-in assembled [:functions 'quoted-map])))
             (is (= #{:nil :boolean :integer :character :string :keyword :symbol :list :vector :map :set}
                    (set (map #(get-in % [:quoted-data :kind]) facts))))
             (is (= 14 (count facts) (get-in assembled [:instruction-summary :quote])))
             (is (not-any? #(= :throw (:core-form %)) (get-in input [:request :canonical-core-artifact :nodes])))
             (is (identical? (get results 'quoted-map) (get results 'same-module-forward)))
             (is (identical? (get results 'quoted-map) (get results 'main)))
             (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
             (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
             {:input input :module module :definitions definitions :typed (:typed products)}))
         [".gravity" ".qst"])
        {:keys [input module definitions]} (first cases)
        ledger (get-in input [:typed :constraint-ledger])
        index (first (keep-indexed #(when (and (= :sh08-recursive-quoted-data-v1 (:producer-rule %2)) (= :map (get-in %2 [:quoted-data :kind]))) %1) ledger))
        row (get ledger index) node-id (:lhs row) payload-id (get-in row [:quoted-data :origin :form-id])
        forms (get-in input [:request :b47-context :authenticated-core-request :forms])
        form-index (first (keep-indexed #(when (= payload-id (:form-id %2)) %1) forms))]
    (is (= (get-in cases [0 :typed :identity-input]) (get-in cases [1 :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :constraint-ledger index :quoted-data :children 0 :origin :data-path] [999])
                     (assoc-in input [:typed :constraint-ledger index :quoted-data :value] '{forged/data [:swapped]})
                     (assoc-in input [:typed :type-table node-id] :gravity.type/string)
                     (assoc-in input [:request :b47-context :authenticated-core-request :forms form-index :syntax-id] "forged-source-id")]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'quoted-map) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    ;; Existing C5/C6/C7 source-rejection evidence remains scoped to its owner revision.
    ;; This affected composition freshly exercises E's public verification boundary.
    (is (= 8 (count @verified)))))

(defn- typed-vector-kernel-context [base]
  (let [source (fn [id] {:form-id id :syntax-id id})
        children [:vector-left :vector-right]
        forms {:vector-form {:form-id :vector-form :syntax-id :vector-form :kind :vector
                             :child-form-ids [:vector-left-form :vector-right-form] :value [:not-evaluated]}
               :vector-left-form {:form-id :vector-left-form :syntax-id :vector-left-form :parent-form-id :vector-form :kind :boolean :value true}
               :vector-right-form {:form-id :vector-right-form :syntax-id :vector-right-form :parent-form-id :vector-form :kind :string :value "right"}}
        nodes {:vector {:node-id :vector :core-form :collection-literal :children children :resolved-binding-ids []
                        :source (source :vector-form) :attributes {:literal-kind :vector :element-count 2 :source-value [:not-evaluated]}
                        :evaluation {:kind :left-to-right :order [{:index 0 :core-node-id :vector-left} {:index 1 :core-node-id :vector-right}]}}
               :vector-left {:node-id :vector-left :core-form :literal :children [] :source (source :vector-left-form)
                             :attributes {:literal-kind :boolean :value true}}
               :vector-right {:node-id :vector-right :core-form :literal :children [] :source (source :vector-right-form)
                              :attributes {:literal-kind :string :value "right"}}}]
    (-> base (update :forms merge forms) (update :nodes merge nodes)
        (update :types merge {:vector {:kind :gravity.type/vector :shape :literal :element-types [:gravity.type/bool :gravity.type/string]}
                              :vector-left :gravity.type/bool :vector-right :gravity.type/string})
        (assoc :tail? true))))

(deftest current-gravity-evaluated-vector-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))
        context (typed-vector-kernel-context base)
        expected {:op :vector-literal :items [{:op :literal :value true} {:op :literal :value "right"}]}
        compile #(invoke 'p15-s23-typed-compile-node %1 :vector %2)]
    (is (= expected (:value (compile context 34))))
    (is (= 34 (:work-count (compile context 34))))
    (is (= :typed-lowering-work-limit (get-in (compile context 33) [:facts :reason])))
    (doseq [kind [:map :set]]
      (let [unsupported (-> context (assoc-in [:nodes :vector :attributes :literal-kind] kind)
                            (assoc-in [:forms :vector-form :kind] kind)
                            (assoc-in [:types :vector :kind] (if (= kind :map) :gravity.type/map :gravity.type/set)))]
        (let [unsupported (if (= kind :map)
                            (-> unsupported (update-in [:types :vector] dissoc :element-types)
                                (assoc-in [:types :vector :member-types] [:gravity.type/bool :gravity.type/string])) unsupported)]
          (is (= (if (= kind :map) :unsupported-typed-map-key :unsupported-typed-lowering-operation)
                 (get-in (compile unsupported 128) [:facts :reason]))))))
    (doseq [mutate [#(assoc-in % [:nodes :vector :attributes :literal-kind] :map)
                    #(assoc-in % [:nodes :vector :attributes :element-count] 1)
                    #(assoc-in % [:nodes :vector :children] [:vector-right :vector-left])
                    #(assoc-in % [:nodes :vector :resolved-binding-ids] [:binding])
                    #(assoc-in % [:nodes :vector :evaluation :order 0 :core-node-id] :vector-right)
                    #(assoc-in % [:forms :vector-form :child-form-ids] [:vector-right-form :vector-left-form])
                    #(assoc-in % [:forms :vector-left-form :parent-form-id] :other)
                    #(assoc-in % [:forms :vector-left-form :syntax-id] :other)
                    #(assoc-in % [:types :vector :element-types] [:gravity.type/string :gravity.type/bool])
                    #(assoc-in % [:types :vector :element-types] [:gravity.type/bool])
                    #(assoc-in % [:types :vector-left] :gravity.type/unknown)
                    #(assoc-in % [:types :vector] :gravity.type/never)
                    #(assoc-in % [:types :vector] :gravity.type/string)]]
      (is (= :rejected (:status (compile (mutate context) 128)))))
    (let [empty (-> context (assoc-in [:nodes :vector :children] [])
                    (assoc-in [:nodes :vector :attributes :element-count] 0)
                    (assoc-in [:nodes :vector :evaluation :order] [])
                    (assoc-in [:forms :vector-form :child-form-ids] [])
                    (assoc-in [:types :vector :element-types] []))]
      (is (= {:op :vector-literal :items []} (:value (compile empty 12))))
      (is (= 12 (:work-count (compile empty 12)))))
    (let [never (-> context (assoc-in [:types :vector] :gravity.type/never)
                    (assoc-in [:types :vector-left] :gravity.type/never)
                    (assoc-in [:nodes :vector-left :core-form] :throw)
                    (assoc-in [:nodes :vector-left :children] [:vector-right]))
          result (compile never 35)]
      (is (= :complete (:status result))) (is (= 35 (:work-count result)))
      (is (= :throw (get-in result [:value :items 0 :op])))
      (is (= :rejected (:status (compile (assoc-in never [:types :vector-right] :gravity.type/unknown) 128)))))
    (let [forms (bootstrap/read-forms path (slurp path))
          helper (first (filter #(= 'p15-s23-typed-compile-collection (second %)) forms))]
      (is (some #{'(p15-s23-typed-compile-children-mode context children (- budget charge) [] :none 0)} (tree-seq coll? seq helper)))
      (is (not-any? #{'(get attrs :source-value) '(get owner :value)} (tree-seq coll? seq helper))))))

(deftest current-gravity-evaluated-vector-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        lit (fn [value] {:op :literal :value value}) vector-ir (fn [items] {:op :vector-literal :items items})
        say (fn [label value] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} value]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        payload (String. "vector payload") data '{:op :throw :kind :gravity/recur :values [false]}]
    (is (= [[] "" :normal] (evaluate (vector-ir []))))
    (is (= [[payload false data] "left\nright\n" :normal]
           (evaluate (vector-ir [(say "left" (lit payload)) (say "right" (lit false)) {:op :quote :value data}]))))
    (is (= [[[1] []] "nested\n" :normal]
           (evaluate (vector-ir [(vector-ir [(say "nested" (lit 1))]) (vector-ir [])]))))
    (doseq [position (range 3)]
      (let [items (mapv #(if (= position %) {:op :throw :value (lit payload)} (say (str %) (lit %))) (range 3))
            result (evaluate (vector-ir items))]
        (is (= [payload (apply str (map #(str % "\n") (range position))) :thrown] result))
        (is (identical? payload (first result)))))
    (let [result (evaluate (vector-ir [(say "before" (lit 1)) {:op :unknown} (say "later" (lit 2))]))]
      (is (= ["before\n" :rejected] (subvec result 1)))
      (is (= "P15S23X002" (get-in result [0 :diagnostic]))))
    (doseq [bad [{:op :vector-literal} {:op :vector-literal :items nil} {:op :vector-literal :items '()}
                 {:op :vector-literal :items [42]} {:op :vector-literal :items [(say "never" (lit 1))] :extra true}
                 {:op :vector-literal :items (vec (repeat 65537 (lit 1)))}]]
      (is (= ["" :rejected] (subvec (evaluate bad) 1))))
    (let [result (evaluate {:op :loop :target "outer" :bindings []
                           :body [(vector-ir [{:op :recur :target "outer" :args []} (say "later" (lit 2))])]})]
      (is (= ["" :rejected] (subvec result 1))))
    (let [failure (AssertionError. "fatal vector item") value (proxy [Object] [] (toString [] (throw failure)))
          observed (seed-throw-test-capture #(evaluate (vector-ir [{:op :builtin-call :function 'str :args [(lit value)]}
                                                                  (say "later" (lit 1))])))]
      (is (identical? failure observed)))))

(defn- typed-map-kernel-context [base]
  (-> (typed-vector-kernel-context base)
      (assoc-in [:nodes :vector :attributes :literal-kind] :map)
      (assoc-in [:forms :vector-form :kind] :map)
      (assoc-in [:nodes :vector-left :attributes] {:literal-kind :keyword :value :key})
      (assoc-in [:forms :vector-left-form :kind] :keyword)
      (assoc-in [:forms :vector-left-form :value] :key)
      (assoc-in [:types :vector-left] :gravity.type/keyword)
      (assoc-in [:types :vector] {:kind :gravity.type/map :shape :literal
                                :member-types [:gravity.type/keyword :gravity.type/string]})))

(deftest current-gravity-evaluated-map-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        context (typed-map-kernel-context (:value (invoke 'p15-s23-typed-context module input observation)))
        compile #(invoke 'p15-s23-typed-compile-node %1 :vector %2)
        expected {:op :map-literal :entries [{:key {:op :literal :value :key} :value {:op :literal :value "right"}}]}]
    (is (= expected (:value (compile context 42))))
    (is (= 42 (:work-count (compile context 42))))
    (is (= :typed-lowering-work-limit (get-in (compile context 41) [:facts :reason])))
    (doseq [mutate [#(assoc-in % [:forms :vector-form :form-id] :wrong)
                    #(assoc-in % [:forms :vector-form :syntax-id] :wrong)
                    #(assoc-in % [:forms :vector-form :kind] :vector)
                    #(assoc-in % [:nodes :vector :children] [:vector-right :vector-left])
                    #(assoc-in % [:forms :vector-form :child-form-ids] [:vector-right-form :vector-left-form])
                    #(assoc-in % [:nodes :vector :attributes :element-count] 1)
                    #(assoc-in % [:nodes :vector :resolved-binding-ids] [:binding])
                    #(assoc-in % [:nodes :vector :evaluation :order] [])
                    #(assoc-in % [:forms :vector-right-form :parent-form-id] :wrong)
                    #(assoc-in % [:nodes :vector-right :source :form-id] :wrong)
                    #(assoc-in % [:nodes :vector-right :source :syntax-id] :wrong)
                    #(assoc-in % [:types :vector :member-types] [:gravity.type/string :gravity.type/keyword])
                    #(assoc-in % [:types :vector :member-types] [:gravity.type/keyword])
                    #(assoc-in % [:types :vector-right] :gravity.type/unknown)
                    #(assoc-in % [:types :vector] :gravity.type/never)]]
      (is (= :rejected (:status (compile (mutate context) 256)))))
    (doseq [mutate [#(assoc-in % [:nodes :vector-left :core-form] :call)
                    #(assoc-in % [:forms :vector-left-form :kind] :string)
                    #(assoc-in % [:forms :vector-left-form :value] "key")]]
      (is (= :unsupported-typed-map-key (get-in (compile (mutate context) 256) [:facts :reason]))))
    (let [equiv-calls (atom 0) hash-calls (atom 0)
          opaque (HostilePersistentCallee. equiv-calls hash-calls (AssertionError. "opaque key inspected"))
          changed (-> context (assoc-in [:forms :vector-left-form :value] opaque)
                      (assoc-in [:nodes :vector-left :attributes :value] opaque))]
      (is (= :unsupported-typed-map-key (get-in (compile changed 256) [:facts :reason])))
      (is (zero? @equiv-calls))
      (is (zero? @hash-calls)))
    (let [empty (-> context (assoc-in [:nodes :vector :children] [])
                    (assoc-in [:nodes :vector :attributes :element-count] 0)
                    (assoc-in [:nodes :vector :evaluation :order] [])
                    (assoc-in [:forms :vector-form :child-form-ids] [])
                    (assoc-in [:types :vector :member-types] []))]
      (is (= {:op :map-literal :entries []} (:value (compile empty 12))))
      (is (= 12 (:work-count (compile empty 12)))))
    (let [odd (-> context (assoc-in [:nodes :vector :children] [:vector-left])
                  (assoc-in [:forms :vector-form :child-form-ids] [:vector-left-form])
                  (assoc-in [:nodes :vector :attributes :element-count] 1)
                  (assoc-in [:nodes :vector :evaluation :order] [{:index 0 :core-node-id :vector-left}])
                  (assoc-in [:types :vector :member-types] [:gravity.type/keyword]))]
      (is (= :typed-map-source-or-type-mismatch (get-in (compile odd 256) [:facts :reason]))))
    (let [duplicate (-> context
                        (assoc-in [:nodes :vector :children] [:vector-left :vector-right :vector-left :vector-right])
                        (assoc-in [:forms :vector-form :child-form-ids] [:vector-left-form :vector-right-form :vector-left-form :vector-right-form])
                        (assoc-in [:nodes :vector :attributes :element-count] 4)
                        (assoc-in [:nodes :vector :evaluation :order]
                                  (mapv (fn [i child] {:index i :core-node-id child}) (range) [:vector-left :vector-right :vector-left :vector-right]))
                        (assoc-in [:types :vector :member-types] [:gravity.type/keyword :gravity.type/string :gravity.type/keyword :gravity.type/string]))]
      (is (= :unsupported-typed-map-key (get-in (compile duplicate 256) [:facts :reason]))))
    (let [never (-> context (assoc-in [:types :vector] :gravity.type/never)
                    (assoc-in [:types :vector-right] :gravity.type/never)
                    (assoc-in [:nodes :vector-right :core-form] :throw)
                    (assoc-in [:nodes :vector-right :children] [:vector-left]))
          result (compile never 256)]
      (is (= :complete (:status result)))
      (is (= :throw (get-in result [:value :entries 0 :value :op]))))))

(deftest current-gravity-evaluated-map-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        lit (fn [value] {:op :literal :value value})
        entry (fn [key value] {:key (lit key) :value value}) map-ir (fn [entries] {:op :map-literal :entries entries})
        say (fn [label value] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} value]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        payload (String. "map payload") data '{:op :throw :kind :gravity/recur :values [false]}]
    (is (= [{} "" :normal] (evaluate (map-ir []))))
    (let [result (evaluate (map-ir [(entry :left (say "left" (lit payload)))
                                    (entry :right (say "right" (lit false))) (entry :data {:op :quote :value data})]))]
      (is (= [{:left payload :right false :data data} "left\nright\n" :normal] result))
      (is (identical? payload (get (first result) :left))))
    (is (= [{:map {:value [true]} :vector [{}]} "" :normal]
           (evaluate (map-ir [(entry :map (map-ir [(entry :value {:op :vector-literal :items [(lit true)]})]))
                              (entry :vector {:op :vector-literal :items [(map-ir [])]})]))))
    (doseq [position (range 3)]
      (let [entries (mapv #(entry (get [:a :b :c] %) (if (= position %) {:op :throw :value (lit payload)} (say (str %) (lit %)))) (range 3))
            result (evaluate (map-ir entries))]
        (is (= [payload (apply str (map #(str % "\n") (range position))) :thrown] result))
        (is (identical? payload (first result)))))
    (let [result (evaluate (map-ir [(entry :a (say "before" (lit 1))) (entry :b {:op :unknown}) (entry :c (say "later" (lit 2)))]))]
      (is (= ["before\n" :rejected] (subvec result 1)))
      (is (= "P15S23X002" (get-in result [0 :diagnostic]))))
    (doseq [bad [{:op :map-literal} {:op :map-literal :entries nil} {:op :map-literal :entries '()}
                 (map-ir [42]) (map-ir [{:key (lit :a)}]) (map-ir [(assoc (entry :a (lit 1)) :extra true)])
                 (map-ir [{:key (assoc (lit :a) :extra true) :value (lit 1)}])
                 (map-ir [{:key {:op :quote :value :a} :value (lit 1)}]) (map-ir [(entry "a" (lit 1))])
                 (map-ir [(entry :a (say "never" (lit 1))) (entry :a (lit 2))])
                 (assoc (map-ir [(entry :a (say "never" (lit 1)))]) :extra true)
                 (map-ir (vec (repeat 65537 (entry :a (lit 1)))))]]
      (is (= ["" :rejected] (subvec (evaluate bad) 1))))
    (let [failure (AssertionError. "fatal map value") value (proxy [Object] [] (toString [] (throw failure)))
          observed (seed-throw-test-capture #(evaluate (map-ir [(entry :a {:op :builtin-call :function 'str :args [(lit value)]})
                                                               (entry :b (say "later" (lit 1)))])))]
      (is (identical? failure observed)))))

(defn- typed-keyword-lookup-kernel-context [base]
  (let [context (typed-map-kernel-context base)
        node {:node-id :lookup :core-form :keyword-map-lookup :children [:lookup-key :vector] :resolved-binding-ids []
              :source {:form-id :lookup-form :syntax-id :lookup-form}
              :attributes {:keyword-child-index 0 :map-child-index 1 :keyword :key :evaluation-order :keyword-then-map
                           :dispatch :keyword-map-lookup :missing-key-policy :nil :result-policy :map-value-or-nil :target-type-legality :pending-sh08}
              :evaluation {:kind :keyword-then-map :order [{:index 0 :core-node-id :lookup-key} {:index 1 :core-node-id :vector}]}}
        record {:core-node-id :lookup :keyword-node-id :lookup-key :map-node-id :vector :keyword :key
                :ordered-evaluation-node-ids [:lookup-key :vector] :evaluation-order :keyword-then-map
                :missing-key-policy :nil :result-policy :map-value-or-nil}]
    (-> context
        (assoc-in [:nodes :lookup] node)
        (assoc-in [:nodes :lookup-key] {:node-id :lookup-key :core-form :literal :children []
                                      :source {:form-id :lookup-key-form :syntax-id :lookup-key-form}
                                      :attributes {:literal-kind :keyword :value :key}})
        (assoc-in [:forms :lookup-form] {:form-id :lookup-form :syntax-id :lookup-form :kind :list :child-form-ids [:lookup-key-form :vector-form]})
        (assoc-in [:forms :lookup-key-form] {:form-id :lookup-key-form :syntax-id :lookup-key-form :kind :keyword :value :key :parent-form-id :lookup-form})
        (assoc-in [:forms :vector-form :parent-form-id] :lookup-form)
        (assoc-in [:types :lookup-key] :gravity.type/keyword)
        (assoc-in [:types :lookup] :gravity.type/string)
        (assoc-in [:core :keyword-lookups] [record]))))

(deftest current-gravity-keyword-map-lookup-lowering-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        {:keys [module input observation]} (typed-lowering-kernel-data)
        context (typed-keyword-lookup-kernel-context (:value (invoke 'p15-s23-typed-context module input observation)))
        work-context {:lookup-core (:core context) :lookup-nodes (:nodes context) :lookup-forms (:forms context) :lookup-bindings {}}
        node (get-in context [:nodes :lookup])
        step #(c7 'sh08-kl-step [%1 (:types context) {} {} %2 %3])
        result (step node work-context 65536)
        enriched (assoc context :ledger [(:constraint result)])
        compile #(invoke 'p15-s23-typed-compile-node %1 :lookup %2)]
    (is (= :accepted (:status result)))
    (is (= :gravity.type/string (:type result)))
    (is (= :present (get-in result [:constraint :solution :presence])))
    (is (= result (step node work-context (:work-count result))))
    (is (= :keyword-lookup-type-work-limit (:reason (step node work-context (dec (:work-count result))))))
    (is (= {:op :keyword-map-lookup :args [{:op :literal :value :key}
                                          {:op :map-literal :entries [{:key {:op :literal :value :key} :value {:op :literal :value "right"}}]}]}
           (:value (compile enriched 1000))))
    (is (= :typed-lowering-work-limit (get-in (compile enriched 43) [:facts :reason])))
    (doseq [changed [(assoc-in enriched [:ledger 0 :status] :pending)
                     (assoc-in enriched [:ledger 0 :solution :result-type] :gravity.type/nil)
                     (assoc-in enriched [:ledger 0 :solution :selected-coordinate] 99)
                     (assoc-in enriched [:ledger 0 :dependencies :selected-shape :type] :gravity.type/nil)
                     (assoc-in enriched [:ledger 0 :dependencies :operand-node-id] :other)
                     (assoc-in enriched [:forms :lookup-key-form :value] :other)
                     (assoc-in enriched [:nodes :lookup :evaluation :order] [])
                     (assoc-in enriched [:types :lookup] :gravity.type/nil)
                     (assoc enriched :ledger [])]]
      (is (= :rejected (:status (compile changed 1000)))))
    (let [bad-node (assoc-in node [:attributes :keyword] :forged)
          bad (step bad-node work-context 65536)]
      (is (= :keyword-lookup-record-mismatch (:reason bad)))
      (is (pos? (:work-count bad))))
    (doseq [changed [(assoc-in work-context [:lookup-forms :lookup-key-form :parent-form-id] :wrong)
                     (assoc-in work-context [:lookup-nodes :vector :core-form] :reference)
                     (assoc-in work-context [:lookup-nodes :vector-right :attributes :value] "forged")]]
      (let [failed (step node changed 65536)]
        (is (= :rejected (:status failed)))
        (is (pos? (:work-count failed)))))
    (let [missing-node (assoc-in node [:attributes :keyword] :missing)
          missing-context (-> work-context (assoc-in [:lookup-core :keyword-lookups 0 :keyword] :missing)
                              (assoc-in [:lookup-nodes :lookup-key :attributes :value] :missing)
                              (assoc-in [:lookup-forms :lookup-key-form :value] :missing))
          missing (step missing-node missing-context 65536)]
      (is (= :accepted (:status missing)))
      (is (= :gravity.type/nil (:type missing)))
      (is (= :absent (get-in missing [:constraint :solution :presence]))))
    (let [never (c7 'sh08-kl-step [node (assoc (:types context) :vector :gravity.type/never) {} {} work-context 65536])]
      (is (= :gravity.type/never (:type never)))
      (is (= :no-normal-result (get-in never [:constraint :solution :presence]))))
    (let [nodes (mapv (:nodes context) [:lookup-key :vector-left :vector-right :vector :lookup])
          infer #(c7 'sh08-ft-infer-acyclic-with-context [nodes (:nodes context) [] [] [] [] {} {} 2 (assoc work-context :loop-work-left %)])
          total (* 2 (:work-count result))]
      (is (= :converged (:convergence-status (infer total))))
      (is (= :gravity.type/string (get-in (infer total) [:type-table :lookup])))
      (is (= :nonconverged (:convergence-status (infer (dec total))))))
    (let [first (step node work-context (* 2 (:work-count result)))
          second (step node work-context (- (* 2 (:work-count result)) (:work-count first)))
          third (step node work-context (- (* 2 (:work-count result)) (:work-count first) (:work-count second)))]
      (is (= [:accepted :accepted :rejected] (mapv :status [first second third])))
      (is (= :keyword-lookup-type-work-limit (:reason third))))
    (require 'gravity.self-hosting.sh08-loop-recur-type-test)
    (let [[loop-core _] ((ns-resolve 'gravity.self-hosting.sh08-loop-recur-type-test 'loop-data))
          loop-nodes (mapv #(cond-> % (= :again-init (:node-id %)) (assoc :attributes {:literal-kind :boolean :value true})
                                     (= :value-init (:node-id %)) (assoc :attributes {:literal-kind :integer :value 7})
                                     (= :next-again (:node-id %)) (assoc :attributes {:literal-kind :boolean :value false})) (:nodes loop-core))
          nodes (into (mapv (:nodes context) [:lookup-key :vector-left :vector-right :vector :lookup]) loop-nodes)
          bindings {:again :gravity.type/bool :value :gravity.type/integer}
          pass #(c7 'sh08-ft-infer-pass-with-context [nodes [] [] [] [] {} bindings (assoc work-context :loop-core loop-core :loop-work-left %)])
          high (pass 65536)
          loop-only (c7 'sh08-ft-loop-type-step [loop-core (:type-table high) bindings 65536])
          narrow (pass (:work-count loop-only))]
      (is (= :accepted (:status loop-only)))
      (is (= (+ (:work-count result) (:work-count loop-only)) (:loop-work-count high)))
      (is (some #(= :loop-type-work-limit (:reason %)) (:diagnostics narrow))))))

(defn- returned-map-shape-kernel-data []
  (let [base (typed-keyword-lookup-kernel-context {}) map-type (get-in base [:types :vector])
        source (fn [form syntax] {:form-id form :syntax-id syntax})
        function-record (fn [name ordinal body parameters]
                          {:ordinal ordinal :function-core-node-id (keyword (str name "-fn"))
                           :function-form-id (keyword (str name "-fn-form")) :function-syntax-id (keyword (str name "-syntax"))
                           :body-core-node-id body :parameter-binding-ids parameters :fixed-arity (count parameters)
                           :definition-kind :named-top-level :definition-binding-id (keyword (str name "-binding"))
                           :definition-core-node-id (keyword (str name "-def")) :definition-name (symbol name)})
        definitions (mapv (fn [name] {:name (symbol name) :binding-id (keyword (str name "-binding"))
                                     :core-node-id (keyword (str name "-def")) :value-node-id (keyword (str name "-fn"))
                                     :syntax-id (keyword (str name "-def-syntax"))}) ["builder" "caller"])
        records {:builder-syntax (function-record "builder" 0 :vector [:builder-parameter])
                 :caller-syntax (function-record "caller" 1 :lookup [])}
        call {:core-node-id :builder-call :operator-node-id :builder-operator :operator-binding-id :builder-binding
              :argument-node-ids [:builder-argument] :ordered-evaluation-node-ids [:builder-operator :builder-argument]
              :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :builder-call :caller-function-syntax-id :caller-syntax :caller-function-core-node-id :caller-fn
              :callee-binding-id :builder-binding :callee-definition-syntax-id :builder-name-syntax
              :callee-function-syntax-id :builder-syntax :callee-function-core-node-id :builder-fn
              :argument-core-node-ids [:builder-argument] :ordered-evaluation-node-ids [:builder-operator :builder-argument]
              :evaluation-order :operator-then-arguments :classification :local-function}
        context (-> base
                    (assoc-in [:nodes :vector-right :core-form] :reference)
                    (assoc-in [:nodes :vector-right :attributes] {:binding-id :builder-parameter})
                    (assoc-in [:forms :vector-right-form :kind] :symbol)
                    (assoc-in [:forms :vector-right-form :value] 'value)
                    (assoc-in [:forms :vector-form :parent-form-id] :builder-fn-form)
                    (assoc-in [:nodes :lookup :children] [:lookup-key :builder-call])
                    (assoc-in [:nodes :lookup :evaluation :order 1 :core-node-id] :builder-call)
                    (assoc-in [:forms :lookup-form :child-form-ids] [:lookup-key-form :builder-call-form])
                    (assoc-in [:forms :lookup-form :parent-form-id] :caller-fn-form)
                    (assoc-in [:core :keyword-lookups 0 :map-node-id] :builder-call)
                    (assoc-in [:core :keyword-lookups 0 :ordered-evaluation-node-ids] [:lookup-key :builder-call])
                    (assoc-in [:nodes :builder-operator] {:node-id :builder-operator :core-form :reference :children []
                                                        :attributes {:binding-id :builder-binding :definition-syntax-id :builder-name-syntax :symbol 'builder}
                                                        :source (source :builder-operator-form :builder-operator-syntax)})
                    (assoc-in [:nodes :builder-argument] {:node-id :builder-argument :core-form :literal :children []
                                                        :attributes {:literal-kind :string :value "caller"}
                                                        :source (source :builder-argument-form :builder-argument-syntax)})
                    (assoc-in [:nodes :builder-call] {:node-id :builder-call :core-form :call :children [:builder-operator :builder-argument]
                                                    :resolved-binding-ids [:builder-binding] :source (source :builder-call-form :builder-call-syntax)
                                                    :evaluation {:kind :operator-then-arguments :owner-function-syntax-id :caller-syntax
                                                                 :order [{:index 0 :core-node-id :builder-operator} {:index 1 :core-node-id :builder-argument}]}})
                    (assoc-in [:forms :builder-call-form] {:form-id :builder-call-form :syntax-id :builder-call-syntax :kind :list
                                                         :parent-form-id :lookup-form :child-form-ids [:builder-operator-form :builder-argument-form]})
                    (assoc-in [:forms :builder-operator-form] {:form-id :builder-operator-form :syntax-id :builder-operator-syntax :kind :symbol :value 'builder :parent-form-id :builder-call-form})
                    (assoc-in [:forms :builder-argument-form] {:form-id :builder-argument-form :syntax-id :builder-argument-syntax :kind :string :value "caller" :parent-form-id :builder-call-form})
                    (assoc-in [:types :builder-call] map-type))
        context (reduce (fn [ctx name]
                          (let [record (get records (keyword (str name "-syntax"))) fn-id (:function-core-node-id record) def-id (:definition-core-node-id record)
                                fn-form (:function-form-id record) def-form (keyword (str name "-def-form")) name-form (keyword (str name "-name-form"))
                                name-syntax (keyword (str name "-name-syntax")) params (:parameter-binding-ids record) body (:body-core-node-id record)]
                            (-> ctx
                                (assoc-in [:nodes fn-id] {:node-id fn-id :core-form :fn :children [body] :resolved-binding-ids params
                                                         :attributes {:parameters (if (seq params) '[value] []) :fixed-arity (count params)}
                                                         :lowering-rule-version :sh07-b11-v12 :source (source fn-form (:function-syntax-id record))})
                                (assoc-in [:nodes def-id] {:node-id def-id :core-form :def :children [fn-id] :resolved-binding-ids [(:definition-binding-id record)]
                                                          :attributes {:name (symbol name) :name-syntax-id name-syntax} :source (source def-form (keyword (str name "-def-syntax")))})
                                (assoc-in [:forms fn-form] {:form-id fn-form :syntax-id (:function-syntax-id record) :kind :list :parent-form-id def-form
                                                           :child-form-ids [:fn-op :params (get-in ctx [:nodes body :source :form-id])]})
                                (assoc-in [:forms def-form] {:form-id def-form :syntax-id (keyword (str name "-def-syntax")) :kind :list :child-form-ids [:def-op name-form fn-form]})
                                (assoc-in [:forms name-form] {:form-id name-form :syntax-id name-syntax :kind :symbol :value (symbol name) :parent-form-id def-form})))) context ["builder" "caller"])
        bindings {:builder-binding {:binding-id :builder-binding :name 'builder :kind :function :binding-class :namespace :namespace 'example.returned
                                    :definition-syntax-id :builder-name-syntax}
                  :caller-binding {:binding-id :caller-binding :name 'caller :kind :function :binding-class :namespace :namespace 'example.returned
                                   :definition-syntax-id :caller-name-syntax}
                  :builder-parameter {:binding-id :builder-parameter :name 'value :binding-class :lexical}}
        products {:module {:namespace 'example.returned} :calls [call] :call-edges [edge] :definitions definitions :function-index records :recursion-components []}]
    {:context context :products products :bindings bindings :binding-types {:builder-parameter :gravity.type/string}
     :functions {:builder-syntax {:return map-type} :caller-syntax {:return :gravity.type/string}}}))

(deftest current-gravity-returned-map-shape-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [invoke (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        {:keys [context products bindings binding-types functions]} (returned-map-shape-kernel-data)
        prepared (invoke 'sh08-sm-call-context [products {} 65536])
        work-context {:lookup-core (:core context) :lookup-nodes (:nodes context) :lookup-forms (:forms context)
                      :lookup-bindings bindings :shape-call-context prepared}
        step #(invoke 'sh08-kl-step [(get-in context [:nodes :lookup]) (:types context) binding-types functions %1 %2])
        result (step work-context 65536)]
    (is (= (:call-edges products)
           (invoke 'sh08-ft-build-call-edges [(vec (vals (:nodes context))) (:definitions products) (vec (vals bindings))
                                             (:calls products) (vec (vals (:function-index products)))])))
    (is (not= (get-in products [:definitions 0 :syntax-id]) (get-in bindings [:builder-binding :definition-syntax-id])))
    (is (= :accepted (:status result)) (pr-str (select-keys result [:reason :expected :actual])))
    (is (= :gravity.type/string (:type result)))
    (is (= 1 (count (get-in result [:constraint :dependencies :operand-shape :call-links]))))
    (is (= :present (get-in result [:constraint :solution :presence])))
    (is (= :accepted (:status (step work-context (:work-count result)))))
    (is (= :keyword-lookup-type-work-limit (:reason (step work-context (dec (:work-count result))))))
    (doseq [changed [(assoc-in work-context [:shape-call-context :edges :builder-call :callee-function-core-node-id] :caller-fn)
                     (assoc-in work-context [:shape-call-context :edges :builder-call :callee-definition-syntax-id] :builder-def-syntax)
                     (assoc-in work-context [:shape-call-context :definitions 0 :value-node-id] :caller-fn)
                     (assoc-in work-context [:shape-call-context :definition-index :builder-binding :value-node-id] :caller-fn)
                     (assoc-in work-context [:shape-call-context :function-records :builder-syntax :body-core-node-id] :lookup)
                     (assoc-in work-context [:lookup-bindings :builder-binding :definition-syntax-id] :forged)
                     (assoc-in work-context [:lookup-nodes :builder-operator :attributes :symbol] 'forged)
                     (assoc-in work-context [:lookup-forms :builder-call-form :child-form-ids] [:builder-argument-form :builder-operator-form])
                     (assoc-in work-context [:lookup-forms :vector-form :parent-form-id] :caller-fn-form)]]
      (let [failed (step changed 65536)]
        (is (= :rejected (:status failed)))
        (is (pos? (:work-count failed)))))
    (let [same-types (-> work-context
                         (assoc-in [:lookup-nodes :vector-left :attributes :value] :other)
                         (assoc-in [:lookup-forms :vector-left-form :value] :other))
          missing (step same-types 65536)]
      (is (= :gravity.type/nil (:type missing)))
      (is (= :absent (get-in missing [:constraint :solution :presence]))))
    (let [changed (assoc-in work-context [:shape-call-context :edges :builder-call :classification] :nonlocal-or-nonfunction)
          failed (step changed 65536)]
      (is (= ["C7-ANNOTATION" :unsupported-nonlocal-return-shape] ((juxt :rule :reason) failed))))
    (let [changed (assoc-in work-context [:shape-call-context :recursion-components] [{:function-syntax-ids [:builder-syntax]}])
          failed (step changed 65536)]
      (is (= :unsupported-recursive-return-shape (:reason failed))))
    (let [duplicate (update products :calls conj (first (:calls products)))
          failed (invoke 'sh08-sm-call-context [duplicate {} 65536])]
      (is (= :duplicate-structural-call-record (:reason failed))))
    (let [initial (:work-count prepared)]
      (is (= (dissoc prepared :budget-left) (dissoc (invoke 'sh08-sm-call-context [products {} initial]) :budget-left)))
      (is (= :structural-call-index-work-limit (:reason (invoke 'sh08-sm-call-context [products {} (dec initial)])))))
    (let [types (dissoc (:types context) :vector :vector-left :vector-right)
          prior (:types context)
          data [(get-in context [:nodes :builder-call]) (:nodes context) types binding-types functions [] bindings (:forms context) #{} 0 {}
                (assoc prepared :prior-types prior)]
          shape (invoke 'sh08-sm-call-shape data)]
      (is (= :accepted (:status shape)))
      (is (= :map (:shape-kind shape)))
      (is (= :key (get-in shape [:parts 0 :key]))))
    (let [shape (invoke 'sh08-sm-shape-with-bindings [:builder-call (:nodes context) (:types context) binding-types functions [] bindings (:forms context)
                                                    #{} 129 {} prepared])]
      (is (= :structural-shape-depth-limit (:reason shape))))
    (let [changed (-> work-context
                      (assoc-in [:shape-call-context :function-records :builder-syntax :body-core-node-id] :vector-right)
                      (assoc-in [:lookup-nodes :builder-fn :children] [:vector-right])
                      (assoc-in [:lookup-forms :builder-fn-form :child-form-ids 2] :vector-right-form)
                      (assoc-in [:lookup-forms :vector-right-form :parent-form-id] :builder-fn-form))
          failed (invoke 'sh08-kl-step [(get-in context [:nodes :lookup])
                                        (assoc (:types context) :vector-right (get-in context [:types :vector]))
                                        {:builder-parameter (get-in context [:types :vector])} functions changed 65536])]
      (is (= ["C7-ANNOTATION" :unestablished-structural-return-shape] ((juxt :rule :reason) failed))))
    (let [nodes (mapv (:nodes context) [:lookup-key :builder-operator :builder-argument :builder-call :lookup :caller-fn :caller-def
                                       :vector-left :vector-right :vector :builder-fn :builder-def])
          skeletons (invoke 'sh08-ft-function-skeletons [(vec (vals (:function-index products))) (:module products)])
          inference (invoke 'sh08-ft-infer-acyclic-with-context
                            [nodes (:nodes context) [] (:definitions products) (:calls products) (:call-edges products) skeletons binding-types 8
                             (assoc work-context :shape-call-products products)])]
      (is (= :converged (:convergence-status inference)))
      (is (= :gravity.type/string (get-in inference [:type-table :lookup])))
      (is (some #(seq (get-in % [:dependencies :operand-shape :call-links])) (:constraints inference))))
    (let [no-demand (invoke 'sh08-ft-infer-pass-with-context [[] [] [] [] [] {} {} {:shape-call-products products}])]
      (is (zero? (:loop-work-count no-demand)))
      (is (nil? (get-in no-demand [:shape-call-context :calls]))))
    (let [clone-id #(keyword (str (name %) "-2"))
          source-ids [:builder-call :builder-operator :builder-argument]
          nodes (reduce (fn [out id]
                          (let [node (get out id) cloned (-> node (assoc :node-id (clone-id id))
                                                                            (update :source #(-> % (update :form-id clone-id) (update :syntax-id clone-id))))]
                            (assoc out (clone-id id)
                                   (if (= id :builder-call)
                                     (-> cloned (update :children #(mapv clone-id %))
                                         (update-in [:evaluation :order] #(mapv (fn [row] (update row :core-node-id clone-id)) %))) cloned))))
                        (:nodes context) source-ids)
          forms (reduce (fn [out id]
                          (let [form-id (get-in context [:nodes id :source :form-id]) form (get out form-id)]
                            (assoc out (clone-id form-id)
                                   (cond-> (-> form (update :form-id clone-id) (update :syntax-id clone-id))
                                     (= id :builder-call) (update :child-form-ids #(mapv clone-id %))
                                     (not= id :builder-call) (update :parent-form-id clone-id))))) (:forms context) source-ids)
          second-call (-> (first (:calls products)) (update :core-node-id clone-id) (update :operator-node-id clone-id)
                          (update :argument-node-ids #(mapv clone-id %)) (update :ordered-evaluation-node-ids #(mapv clone-id %)))
          second-edge (-> (first (:call-edges products)) (assoc :ordinal 1) (update :call-core-node-id clone-id)
                          (update :argument-core-node-ids #(mapv clone-id %)) (update :ordered-evaluation-node-ids #(mapv clone-id %)))
          products (-> products (update :calls conj second-call) (update :call-edges conj second-edge))
          prepared (invoke 'sh08-sm-call-context [products {} 65536])
          vector-node {:node-id :sibling-vector :core-form :collection-literal :children [:builder-call :builder-call-2]
                       :attributes {:literal-kind :vector :element-count 2} :source {:form-id :sibling-form :syntax-id :sibling-syntax}}
          nodes (assoc nodes :sibling-vector vector-node)
          forms (assoc forms :sibling-form {:form-id :sibling-form :syntax-id :sibling-syntax :kind :vector
                                            :child-form-ids [:builder-call-form :builder-call-form-2]})
          types (assoc (:types context) :builder-call-2 (get-in context [:types :vector]))
          join-work (get-in result [:constraint :dependencies :operand-shape :call-join-work])
          shape #(invoke 'sh08-sm-shape-with-bindings [:sibling-vector nodes types binding-types functions [] bindings %1 #{} 0 {}
                                                     (assoc prepared :budget-left %2)])
          narrow (shape forms join-work)
          failed-source (shape (assoc-in forms [:sibling-form :child-form-ids 1] :missing-form) (* 2 join-work))]
      (is (= :structural-call-join-work-limit (:reason narrow)))
      (is (= (* 2 join-work) (:call-join-work narrow)))
      (is (= :constructor-child-source-mismatch (:reason failed-source)))
      (is (= (* 2 join-work) (:call-join-work failed-source))))))

(deftest current-gravity-keyword-map-lookup-c8-development-kernel
  (let [c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        id #(str "sha256:" (bootstrap/sha256-hex (str %)))
        node (fn [n kind children] {:node-id (id n) :core-form kind :children (mapv id children)
                                   :source {:syntax-id (id n)}
                                   :evaluation {:kind :left-to-right :order (mapv (fn [i child] {:index i :core-node-id (id child)}) (range) children)}})
        key (node 1 :literal []) value (node 2 :literal []) thrown (node 3 :throw [2])
        lookup (-> (node 4 :keyword-map-lookup [1 3])
                   (assoc-in [:evaluation :kind] :keyword-then-map)
                   (assoc :attributes {:evaluation-order :keyword-then-map}))
        core {:nodes [key value thrown lookup] :function-records [] :call-edges []}
        facts {:module {:effects [:error/throw]} :function-type-table []
               :type-table {(id 1) :gravity.type/keyword (id 2) :gravity.type/string (id 3) :gravity.type/never (id 4) :gravity.type/never}}
        analyzed (c8 'body-derive-nodes core facts) graph {:nodes (:nodes analyzed) :functions {}}
        record (get-in graph [:nodes (id 4)])]
    (is (= :accepted (:status analyzed)))
    (is (= #{} (:direct record)))
    (is (= #{:error/throw} (:transitive record)))
    (is (= #{:gravity.type/string} (:escaping-thrown-errors record)))
    (is (= [(id 1) (id 3)] (:evaluated-children record)))
    (is (true? (c8 'body-order-valid? lookup)))
    (is (false? (c8 'body-order-valid? (update-in lookup [:evaluation :order] #(vec (reverse %))))))
    (is (false? (c8 'body-order-valid? (assoc-in lookup [:evaluation :kind] :left-to-right))))
    (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) graph 65536))))))

(deftest current-gravity-keyword-map-lookup-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        lit (fn [value] {:op :literal :value value}) lookup (fn [key operand] {:op :keyword-map-lookup :args [(lit key) operand]})
        say (fn [label value] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} value]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        payload (String. "lookup payload")]
    (doseq [[key data expected] [[:value {:value payload} payload] [:value {:value nil} nil] [:missing {:value payload} nil]]]
      (let [result (evaluate (lookup key (say "operand" (lit data))))]
        (is (= [expected "operand\n" :normal] result))
        (is (identical? expected (first result)))))
    (is (= [payload "" :normal] (evaluate (lookup :value (lookup :outer (lit {:outer {:value payload}}))))))
    (let [result (evaluate (lookup :missing {:op :map-literal :entries [{:key (lit :present) :value {:op :throw :value (lit payload)}}]}))]
      (is (= [payload "" :thrown] result))
      (is (identical? payload (first result))))
    (doseq [operand [(lit nil) (lit 42) (lit [])]]
      (let [result (evaluate (lookup :key (say "checked" operand)))]
        (is (= ["checked\n" :rejected] (subvec result 1)))
        (is (= :keyword-lookup-map-required (get-in result [0 :reason])))))
    (let [result (evaluate (lookup :key {:op :unknown}))]
      (is (= ["" :rejected] (subvec result 1)))
      (is (= "P15S23X002" (get-in result [0 :diagnostic]))))
    (doseq [bad [{:op :keyword-map-lookup} {:op :keyword-map-lookup :args []}
                 {:op :keyword-map-lookup :args [(lit :key)]} {:op :keyword-map-lookup :args [(lit :key) (lit {}) (lit {})]}
                 {:op :keyword-map-lookup :args [(lit "key") (say "never" (lit {}))]}
                 {:op :keyword-map-lookup :args [{:op :quote :value :key} (say "never" (lit {}))]}
                 (assoc (lookup :key (say "never" (lit {}))) :extra true)]]
      (is (= ["" :rejected] (subvec (evaluate bad) 1))))
    (let [failure (AssertionError. "fatal lookup map") value (proxy [Object] [] (toString [] (throw failure)))
          observed (seed-throw-test-capture #(evaluate (lookup :key {:op :builtin-call :function 'str :args [(lit value)]}))) ]
      (is (identical? failure observed)))))

(deftest current-gravity-evaluated-vector-c8-development-kernel
  ;; Independent existing C8 body algorithms over normalized DATA, not a legality grant.
  (let [c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        id #(str "sha256:" (bootstrap/sha256-hex (str %)))
        node (fn [n kind children] {:node-id (id n) :core-form kind :children (mapv id children)
                                   :source {:syntax-id (id n)}
                                   :evaluation {:kind :left-to-right :order (mapv (fn [i child] {:index i :core-node-id (id child)}) (range) children)}})
        literal (node 1 :literal []) thrown (node 2 :throw [1])
        vector-node (assoc (node 3 :collection-literal [2]) :attributes {:literal-kind :vector :element-count 1})
        core {:nodes [literal thrown vector-node] :function-records [] :call-edges []}
        facts {:module {:effects [:error/throw]} :function-type-table []
               :type-table {(id 1) :gravity.type/string (id 2) :gravity.type/never (id 3) :gravity.type/never}}
        analyzed (c8 'body-derive-nodes core facts) graph {:nodes (:nodes analyzed) :functions {}}
        record (get-in graph [:nodes (id 3)])]
    (is (= :accepted (:status analyzed)))
    (is (= #{} (:direct record)))
    (is (false? (get-in record [:allocation :normal-completion-possible?])))
    (is (= :normal-operand-completion (get-in record [:allocation :condition])))
    (is (= #{:error/throw} (:transitive record)))
    (is (= [(id 2)] (:evaluated-children record)))
    (is (true? (c8 'body-order-valid? vector-node)))
    (is (false? (c8 'body-order-valid? (assoc-in vector-node [:evaluation :order] []))))
    (is (= :accepted (:status (c8 'body-allowance-check core facts graph 65536))))
    (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) graph 65536))))))

(defn- evaluated-vector-c8-checks [input]
  (let [c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request (:request input) facts (get-in input [:typed :identity-input])
        core (:canonical-core-artifact request)
        vectors (filterv #(= :collection-literal (:core-form %)) (:nodes core))
        analyzed (c8 'body-analyze-module request facts {} 65536)]
    (is (= :accepted (:status analyzed)) (pr-str (select-keys analyzed [:reason :diagnostics])))
    (when (= :accepted (:status analyzed))
      (let [graph (:graph analyzed) nonempty (first (filter #(seq (:children %)) vectors))]
        (is (= 12 (count vectors)))
        (doseq [node vectors]
          (let [normal? (not-any? #(= :gravity.type/never (get-in facts [:type-table %])) (:children node))]
            (is (= (if normal? #{:memory/allocate} #{}) (get-in graph [:nodes (:node-id node) :direct])))
            (is (= normal? (get-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?]))))
          (is (= (:children node) (get-in graph [:nodes (:node-id node) :evaluated-children]))))
        (is (some #(contains? (get-in graph [:nodes (:node-id %) :transitive]) :error/throw) vectors))
        (is (false? (c8 'body-order-valid? (assoc-in nonempty [:evaluation :order] []))))
        (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) graph 65536))))))))

(deftest evaluated-vector-c8-consumer-development-kernel
  (let [leaf (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request {:canonical-core-artifact {:nodes []}} facts {:module {:effects [:error/throw]}}
        input {:request request :typed {:outer-artifact :not-c8-facts :identity-input facts}}
        calls (atom []) reports (atom [])]
    (with-redefs-fn {leaf (fn [function & args]
                           (swap! calls conj [function (vec args)])
                           {:status :rejected :reason :normalized-failure})
                    #'clojure.test/report (fn [event] (swap! reports conj event))}
      #(evaluated-vector-c8-checks input))
    (is (= 1 (count @calls)))
    (is (= 'body-analyze-module (ffirst @calls)))
    (is (identical? request (get-in @calls [0 1 0])))
    (is (identical? facts (get-in @calls [0 1 1])))
    (is (= [:fail] (mapv :type @reports)))))

(deftest current-gravity-evaluated-vector-c8-authenticated-composition
  ;; Affected successor to the failed C8 consumer in v1. Do not replay the
  ;; separate lowering/runtime/public-tamper work completed by that run.
  (let [fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-evaluated-vector.gravity")
        input (select-keys products [:request :typed :verification])]
    (is (= :accepted (get-in input [:typed :status])))
    (is (= :passed (get-in input [:verification :status])))
    (evaluated-vector-c8-checks input)))

(deftest current-gravity-evaluated-vector-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "vector source payload")
        cases
        (mapv
         (fn [extension]
           (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-evaluated-vector" extension)
                 path (.getPath (io/file root relative)) text (slurp path)
                 products (fresh relative) input (select-keys products [:request :typed :verification])
                 macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                 parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                 module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                 definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                 standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'packed) input))
                 assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                 vectors (filterv #(= :collection-literal (:core-form %)) (get-in input [:request :canonical-core-artifact :nodes]))
                 specifications [['empty-vector [] []] ['mixed-vector [] [nil true 17 \a "text" :key]]
                                 ['packed [true payload] [true payload]]
                                 ['nested-vector [] '[[] [true ["text"]] [unresolved/data (throw unresolved/data)]]]
                                 ['let-forward [payload] [true payload]] ['build [payload] [payload]]
                                 ['direct-forward [payload] [payload]] ['caught [payload] [payload]] ['main [] [true "payload"]]]
                 results (into {} (map (fn [[name args expected]]
                                        (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                                       [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                                          (is (= [expected "" :normal] result)) [name (first result)])) specifications))]
             (is (= :accepted (get-in input [:typed :status])))
             (is (= :passed (get-in input [:verification :status])))
             (is (= 9 (count (:functions assembled))))
             (is (= 12 (count vectors) (get-in assembled [:instruction-summary :vector-literal])))
             (is (= standalone (get-in assembled [:functions 'packed])))
             (is (= :pending-c8-and-profile-authority (get-in standalone [:typed-lowering-provenance :effect-legality])))
             (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
             (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
             (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
             (is (some #(= :gravity.type/never (get-in input [:typed :type-table (:node-id %)])) vectors))
             (doseq [[name position] [['packed 1] ['let-forward 1] ['build 0] ['direct-forward 0] ['caught 0]]]
               (is (identical? payload (get (get results name) position))))
             (evaluated-vector-c8-checks input)
             {:input input :module module :definitions definitions :typed (:typed products)}))
         [".gravity" ".qst"])
        {:keys [input module definitions]} (first cases)
        nodes (get-in input [:request :canonical-core-artifact :nodes])
        vector-index (first (keep-indexed #(when (and (= :collection-literal (:core-form %2)) (= 2 (count (:children %2)))
                                                     (= [:gravity.type/bool :gravity.type/string] (get-in input [:typed :type-table (:node-id %2) :element-types]))) %1) nodes))
        vector-node (get nodes vector-index) node-id (:node-id vector-node) child-id (first (:children vector-node))]
    (is (= (get-in cases [0 :typed :identity-input]) (get-in cases [1 :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :type-table node-id] :gravity.type/never)
                     (assoc-in input [:typed :type-table child-id] :gravity.type/unknown)
                     (assoc-in input [:request :canonical-core-artifact :nodes vector-index :children] (vec (reverse (:children vector-node))))]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'packed) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 7 (count @verified)))
    (let [products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-vector-result-type.gravity")
          typed (:typed products) failure (first (:diagnostics typed))]
      (is (= :rejected (:status typed)))
      (is (= ["C7-TYPE-MISMATCH" :function-result-type-mismatch] ((juxt :rule :reason) failure))))))

(defn- evaluated-map-c8-checks [input]
  ;; Existing C8 algorithms consume identity-input facts, independently of lowering.
  (let [c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request (:request input) facts (get-in input [:typed :identity-input])
        core (:canonical-core-artifact request)
        collections (filterv #(= :collection-literal (:core-form %)) (:nodes core))
        analyzed (c8 'body-analyze-module request facts {} 65536)]
    (is (= :accepted (:status analyzed)) (pr-str (select-keys analyzed [:reason :diagnostics])))
    (when (= :accepted (:status analyzed))
      (let [graph (:graph analyzed) nonempty (first (filter #(seq (:children %)) collections))]
        (is (= {:map 13 :vector 2} (frequencies (map #(get-in % [:attributes :literal-kind]) collections))))
        (doseq [node collections]
          (let [normal? (not-any? #(= :gravity.type/never (get-in facts [:type-table %])) (:children node))]
            (is (= (if normal? #{:memory/allocate} #{}) (get-in graph [:nodes (:node-id node) :direct])))
            (is (= normal? (get-in graph [:nodes (:node-id node) :allocation :normal-completion-possible?]))))
          (is (= (:children node) (get-in graph [:nodes (:node-id node) :evaluated-children]))))
        (is (some #(contains? (get-in graph [:nodes (:node-id %) :transitive]) :error/throw) collections))
        (is (false? (c8 'body-order-valid? (assoc-in nonempty [:evaluation :order] []))))
        (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) graph 65536))))))))

(deftest evaluated-map-c8-consumer-development-kernel
  (let [leaf (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request {:canonical-core-artifact {:nodes []}} facts {:module {:effects [:error/throw]}}
        input {:request request :typed {:outer-artifact :not-c8-facts :identity-input facts}}
        calls (atom []) reports (atom [])]
    (with-redefs-fn {leaf (fn [function & args] (swap! calls conj [function (vec args)])
                           {:status :rejected :reason :normalized-failure})
                    #'clojure.test/report (fn [event] (swap! reports conj event))}
      #(evaluated-map-c8-checks input))
    (is (= 1 (count @calls)))
    (is (= 'body-analyze-module (ffirst @calls)))
    (is (identical? request (get-in @calls [0 1 0])))
    (is (identical? facts (get-in @calls [0 1 1])))
    (is (= [:fail] (mapv :type @reports)))))

(deftest current-gravity-evaluated-map-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "map source payload")
        cases
        (mapv
         (fn [extension]
           (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-evaluated-map" extension)
                 path (.getPath (io/file root relative)) text (slurp path)
                 products (fresh relative) input (select-keys products [:request :typed :verification])
                 macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                 parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                 module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                 definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                 standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'packed) input))
                 assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                 specifications [['empty-map [] {}] ['mixed-map [] {:nil nil :flag true :number 17 :character \a :text "text" :keyword :key}]
                                 ['packed [true payload] {:flag true :value payload}]
                                 ['nested-map [] {:vector [{:inner "text"} {}] :map {:leaf [true]}}]
                                 ['let-forward [payload] {:value payload}] ['build [payload] {:value payload}]
                                 ['direct-forward [payload] {:value payload}] ['caught [payload] {:caught payload}]
                                 ['quoted-duplicate [] {:same 2}] ['main [] {:flag true :value "payload"}]]
                 evaluate (fn [name args] (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                           [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []]))
                 results (into {} (map (fn [[name args expected]] (let [result (evaluate name args)]
                                                                  (is (= [expected "" :normal] result)) [name (first result)])) specifications))]
             (is (= :accepted (get-in input [:typed :status])))
             (is (= :passed (get-in input [:verification :status])))
             (is (= 12 (count (:functions assembled))))
             (is (= 13 (get-in assembled [:instruction-summary :map-literal])))
             (is (= 2 (get-in assembled [:instruction-summary :vector-literal])))
             (is (= standalone (get-in assembled [:functions 'packed])))
             (is (= ["value-i" "" :thrown] (evaluate 'forward [])))
             (is (= ["value-a" "" :thrown] (evaluate 'backward [])))
             (is (= :pending-c8-and-profile-authority (get-in standalone [:typed-lowering-provenance :effect-legality])))
             (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
             (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
             (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
             (doseq [[name key] [['packed :value] ['let-forward :value] ['build :value] ['direct-forward :value] ['caught :caught]]]
               (is (identical? payload (get (get results name) key))))
             (evaluated-map-c8-checks input)
             (assoc (select-keys products [:artifact :c5]) :input input :module module :definitions definitions)))
         [".gravity" ".qst"])
        {:keys [input module definitions artifact c5]} (first cases)
        nodes (get-in input [:request :canonical-core-artifact :nodes])
        map-index (first (keep-indexed #(when (= [:gravity.type/keyword :gravity.type/bool :gravity.type/keyword :gravity.type/string]
                                                (get-in input [:typed :type-table (:node-id %2) :member-types])) %1) nodes))
        map-node (get nodes map-index) node-id (:node-id map-node) child-id (first (:children map-node))]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :type-table node-id] :gravity.type/never)
                     (assoc-in input [:typed :type-table child-id] :gravity.type/unknown)
                     (assoc-in input [:request :canonical-core-artifact :nodes map-index :children] (vec (reverse (:children map-node))))]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'packed) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 7 (count @verified)))
    (let [request (get-in artifact [:gravity-core-boundary :authenticated-core-request])
          index (first (keep-indexed #(when (= (get-in map-node [:source :form-id]) (:form-id %2)) %1) (:forms request)))
          children (get-in request [:forms index :child-form-ids])
          changed (assoc-in request [:forms index :child-form-ids] (vec (mapcat identity (reverse (partition 2 children)))))
          changed (assoc changed :projection-binding (bootstrap/reader-canonical-hash (bootstrap/sh07-core-projection-binding-input changed)))
          executed (atom 0)
          failure (with-redefs [bootstrap/sh07-core-run-structural-request-for-test
                               (fn [_] (swap! executed inc) (throw (AssertionError. "tampered source order reached core execution")))]
                    (diagnostic #(bootstrap/sh07-core-run-request-for-test c5 changed)))]
      (is (= 4 (count children)))
      (is (= "C6-VERIFY" (:rule failure)))
      (is (= :authenticated-sh06-request-membership-mismatch (get-in failure [:facts :reason])))
      (is (zero? @executed)))
    (let [products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-map-result-type.gravity")
          typed (:typed products) failure (first (:diagnostics typed))]
      (is (= :rejected (:status typed)))
      (is (= ["C7-TYPE-MISMATCH" :function-result-type-mismatch] ((juxt :rule :reason) failure))))))

(defn- keyword-lookup-c8-checks [input]
  (let [c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request (:request input) facts (get-in input [:typed :identity-input]) core (:canonical-core-artifact request)
        lookups (filterv #(= :keyword-map-lookup (:core-form %)) (:nodes core))
        analyzed (c8 'body-analyze-module request facts {} 65536)]
    (is (= :accepted (:status analyzed)) (pr-str (select-keys analyzed [:reason :diagnostics])))
    (when (= :accepted (:status analyzed))
      (let [graph (:graph analyzed) never (first (filter #(= :gravity.type/never (get-in facts [:type-table (:node-id %)])) lookups))]
        (is (= 10 (count lookups)))
        (doseq [node lookups]
          (is (= #{} (get-in graph [:nodes (:node-id node) :direct])))
          (is (= (:children node) (get-in graph [:nodes (:node-id node) :evaluated-children])))
          (is (= (get-in facts [:type-table (:node-id node)]) (get-in graph [:nodes (:node-id node) :type]))))
        (is (= #{:error/throw} (get-in graph [:nodes (:node-id never) :transitive])))
        (is (= #{:gravity.type/string} (get-in graph [:nodes (:node-id never) :escaping-thrown-errors])))
        (is (false? (c8 'body-order-valid? (assoc-in never [:evaluation :order] []))))
        (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) graph 65536))))))))

(deftest keyword-lookup-c8-consumer-development-kernel
  (let [leaf (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        request {:canonical-core-artifact {:nodes []}} facts {:module {:effects [:error/throw]}}
        input {:request request :typed {:outer-artifact :not-c8-facts :identity-input facts}}
        calls (atom []) reports (atom [])]
    (with-redefs-fn {leaf (fn [function & args] (swap! calls conj [function (vec args)])
                           {:status :rejected :reason :normalized-failure})
                    #'clojure.test/report (fn [event] (swap! reports conj event))}
      #(keyword-lookup-c8-checks input))
    (is (= 1 (count @calls)))
    (is (= 'body-analyze-module (ffirst @calls)))
    (is (identical? request (get-in @calls [0 1 0])))
    (is (identical? facts (get-in @calls [0 1 1])))
    (is (= [:fail] (mapv :type @reports)))))

(deftest current-gravity-keyword-map-lookup-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "lookup source payload")
        cases
        (mapv
         (fn [extension]
           (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-keyword-map-lookup" extension)
                 path (.getPath (io/file root relative)) text (slurp path)
                 products (fresh relative) input (select-keys products [:request :typed :verification])
                 macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                 parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                 module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                 definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                 standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'let-lookup) input))
                 assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                 rows (filterv #(= :sh08-keyword-map-lookup-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                 specifications [['constructor-lookup [payload] payload] ['let-lookup [payload] payload]
                                 ['nested-lookup [] "nested"] ['missing [] nil] ['present-nil [] nil]
                                 ['quoted-duplicate [] 2] ['caught [payload] payload] ['quoted-alias [] "quoted"] ['main [] "payload"]]
                 results (into {} (map (fn [[name args expected]]
                                        (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                                       [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                                          (is (= [expected "" :normal] result)) [name (first result)])) specifications))]
             (is (= :accepted (get-in input [:typed :status])))
             (is (= :passed (get-in input [:verification :status])))
             (is (= 9 (count (:functions assembled))))
             (is (= 10 (count rows) (get-in assembled [:instruction-summary :keyword-map-lookup])))
             (is (= 7 (get-in assembled [:instruction-summary :map-literal])))
             (is (= 2 (get-in assembled [:instruction-summary :quote])))
             (is (= {:present 8 :absent 1 :no-normal-result 1} (frequencies (map #(get-in % [:solution :presence]) rows))))
             (is (some #(and (= :present (get-in % [:solution :presence])) (= :gravity.type/nil (:rhs %))) rows))
             (is (some #(and (= :absent (get-in % [:solution :presence])) (= :gravity.type/nil (:rhs %))) rows))
             (is (= standalone (get-in assembled [:functions 'let-lookup])))
             (is (= :pending-c8-and-profile-authority (get-in standalone [:typed-lowering-provenance :effect-legality])))
             (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
             (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
             (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
             (doseq [name ['constructor-lookup 'let-lookup 'caught]] (is (identical? payload (get results name))))
             (keyword-lookup-c8-checks input)
             {:input input :module module :definitions definitions}))
         [".gravity" ".qst"])
        {:keys [input module definitions]} (first cases)
        ledger (get-in input [:typed :constraint-ledger])
        row-index (first (keep-indexed #(when (and (= :sh08-keyword-map-lookup-v1 (:producer-rule %2)) (= :gravity.type/string (:rhs %2))) %1) ledger))
        row (get ledger row-index) node-id (:lhs row)
        nodes (get-in input [:request :canonical-core-artifact :nodes])
        node-index (first (keep-indexed #(when (= node-id (:node-id %2)) %1) nodes)) node (get nodes node-index)
        forms (get-in input [:request :b47-context :authenticated-core-request :forms])
        form-index (first (keep-indexed #(when (= (get-in node [:source :form-id]) (:form-id %2)) %1) forms))]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :constraint-ledger row-index :dependencies :selected-shape :type] :gravity.type/nil)
                     (assoc-in input [:typed :type-table node-id] :gravity.type/never)
                     (update-in input [:typed :constraint-ledger] #(vec (concat (subvec % 0 row-index) (subvec % (inc row-index)))))
                     (assoc-in input [:request :canonical-core-artifact :nodes node-index :children] (vec (reverse (:children node))))
                     (assoc-in input [:request :canonical-core-artifact :nodes node-index :attributes :keyword] :forged)
                     (assoc-in input [:request :b47-context :authenticated-core-request :forms form-index :syntax-id] :forged)]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'let-lookup) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 10 (count @verified)))
    (doseq [[basename expected] [["local-runtime-keyword-map-scalar" ["C7-ANNOTATION" :keyword-lookup-map-shape-required]]
                               ["local-runtime-keyword-map-nil-result" ["C7-TYPE-MISMATCH" :function-result-type-mismatch]]]]
      (let [products (fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" basename ".gravity"))
            typed (:typed products) failure (first (:diagnostics typed))]
        (is (= :rejected (:status typed)))
        (is (= expected ((juxt :rule :reason) failure)))))))

(defn- typed-boolean-match-kernel-data [base]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [leaf (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        {:keys [core products forms bindings remainder match-id scrutinee-id bodies]} ((ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'boolean-match-input))
        source (fn [form] {:form-id (:form-id form) :syntax-id (:syntax-id form) :semantic-span (:source-span form)
                          :origin-chain (:origin-chain form) :generated-origin (:generated-origin form)})
        operator (get-in forms [0 :child-form-ids 0]) flag-binding (get-in core [:nodes 0 :attributes :binding-id])
        body-forms (mapv :branch-form-id (:match-branch-records core))
        forms (into {} (map (juxt :form-id identity)) forms)
        forms (-> forms
                  (assoc operator {:form-id operator :syntax-id operator :kind :symbol :value 'match :parent-form-id match-id :child-form-ids []})
                  (assoc scrutinee-id {:form-id scrutinee-id :syntax-id scrutinee-id :kind :symbol :value 'flag :parent-form-id match-id
                                      :source-span {:byte-start 0 :byte-end 1} :origin-chain [] :generated-origin nil})
                  (assoc-in [match-id :kind] :list)
                  (assoc-in [match-id :source-span] {:byte-start 0 :byte-end 1})
                  (assoc-in [match-id :origin-chain] [])
                  (assoc-in [(get-in core [:match-branch-records 0 :pattern-form-id]) :parent-form-id] match-id)
                  (assoc-in [(get-in core [:match-branch-records 1 :pattern-form-id]) :parent-form-id] match-id)
                  (assoc (body-forms 0) {:form-id (body-forms 0) :syntax-id (body-forms 0) :kind :boolean :value true :parent-form-id match-id
                                    :source-span {:byte-start 0 :byte-end 1} :origin-chain [] :generated-origin nil})
                  (assoc (body-forms 1) {:form-id (body-forms 1) :syntax-id (body-forms 1) :kind :symbol :value 'remainder :parent-form-id match-id
                                    :scope-id (get-in core [:match-branch-records 1 :pattern-binding-scope-id])
                                    :source-span {:byte-start 0 :byte-end 1} :origin-chain [] :generated-origin nil}))
        core (-> core
                 (assoc-in [:nodes 1 :attributes :value] true)
                 (assoc-in [:nodes 3 :attributes :evaluation-order] :scrutinee-then-source-ordered-pattern-candidates)
                 (assoc-in [:nodes 3 :evaluation :kind] :scrutinee-then-source-ordered-pattern-candidates)
                 (assoc-in [:nodes 3 :evaluation :order] [{:index 0 :core-node-id scrutinee-id}]))
        core (update core :nodes #(mapv (fn [node] (assoc node :source (source (get forms (get-in node [:source :form-id]))))) %))
        products (assoc products :node-table (into {} (map (juxt :node-id identity)) (:nodes core)))
        group (first (:boolean-matches (leaf 'sh08-bm-products [core products (vec (vals forms))])))
        types {scrutinee-id :gravity.type/bool (bodies 0) :gravity.type/bool (bodies 1) :gravity.type/bool match-id :gravity.type/bool}
        rows (:constraints (leaf 'sh08-bm-check [group types {} {:profile :meta :target :jvm}]))
        context (-> base (update :forms merge forms) (update :nodes merge (:node-table products))
                    (update :types merge types) (update :bindings merge (:binding-index products))
                    (update :core merge core) (assoc :ledger rows :locals {flag-binding 'flag} :parameter-types bindings :tail? true))]
    {:context context :match-id match-id :scrutinee-id scrutinee-id :bodies bodies :remainder remainder :group group :leaf leaf}))

(deftest current-gravity-boolean-match-call-errors-development-kernel
  ;; Normalized compiler DATA exercises the owning analyses. Source admission
  ;; is covered separately by the paired composition.
  (require 'gravity.self-hosting.sh08-loop-recur-type-test)
  (let [helper #(ns-resolve 'gravity.self-hosting.sh08-loop-recur-type-test %)
        c7 (fn [f & args] ((helper 'c7-invoke) f (vec args)))
        c8 (fn [f & args] (apply (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf) f args))]
    (doseq [position [:scrutinee :branch]]
      (let [{:keys [core types functions direct bindings id fid]} ((helper 'local-throw-kernel-data))
            scrutinee? (= position :scrutinee)
            make (fn [n kind children type]
                   (-> (first (:nodes core))
                       (assoc :node-id (id n) :core-form kind :children (mapv id children)
                              :attributes (if (= kind :literal) {:literal-kind type :value (if (= type :boolean) false "branch")} {}))
                       (assoc :source {:form-id (id (+ 10000 n)) :syntax-id (id (+ 20000 n))
                                       :semantic-span {:start-byte n :end-byte (inc n)} :origin-chain [] :generated-origin nil})
                       (assoc :evaluation {:kind :left-to-right :owner-function-syntax-id (fid 2)
                                           :order (mapv (fn [i n] {:index i :core-node-id (id n)}) (range) children)})))
            children (if scrutinee? [4 23 24] [20 4 24])
            match-node (-> (make 22 :match children nil)
                           (assoc :attributes {:branch-count 2 :branch-child-indexes [1 2] :guard-child-indexes []})
                           (assoc-in [:evaluation :order] [{:index 0 :core-node-id (id (first children))}]))
            extra [(make 20 :literal [] :boolean) (make 21 :literal [] :boolean)
                   (make 23 :literal [] :string) (make 25 :literal [] :boolean)
                   (make 24 :throw [25] nil) match-node]
            conditional (-> (make 26 :if [20 2 21] nil)
                            (assoc-in [:evaluation :owner-function-syntax-id] (fid 1))
                            (assoc-in [:evaluation :order] [{:index 0 :core-node-id (id 20)}]))
            extra (if scrutinee? (conj extra conditional) extra)
            nodes (mapv (fn [n]
                          (cond (= (:node-id n) (id 202)) (assoc n :children [(id 22)])
                                (and scrutinee? (= (:node-id n) (id 19)))
                                (-> n (assoc :children [(id 18) (id 26)])
                                    (assoc-in [:evaluation :order] [{:index 0 :core-node-id (id 18)} {:index 1 :core-node-id (id 26)}]))
                                :else n)) (:nodes core))
            core (-> core (assoc :nodes (into nodes extra))
                     (update :function-records #(mapv (fn [f] (if (= (:function-syntax-id f) (fid 2)) (assoc f :body-core-node-id (id 22)) f)) %))
                     (update :error-transfers conj {:core-node-id (id 24) :value-core-node-id (id 25)})
                     (assoc :match-branch-records (mapv (fn [ordinal body] {:core-node-id (id 22) :clause-ordinal ordinal
                              :branch-child-index (inc ordinal) :branch-core-node-id (id body) :scrutinee-core-node-id (id (first children))}) (range) (rest children))
                            :match-decision-skeletons [{:core-node-id (id 22) :scrutinee-core-node-id (id (first children))
                                                       :branch-core-node-ids (mapv id (rest children))}]))
            types (merge types {(id 20) :gravity.type/bool (id 21) :gravity.type/bool (id 23) :gravity.type/string
                                (id 25) :gravity.type/bool (id 24) :gravity.type/never (id 22) :gravity.type/string}
                         (when scrutinee? {(id 4) :gravity.type/bool (id 19) :gravity.type/bool (id 26) :gravity.type/bool}))
            functions (cond-> (assoc-in functions [(fid 2) :body-core-node-id] (id 22))
                        scrutinee? (assoc-in [(fid 1) :return] :gravity.type/bool))
            direct (assoc-in direct [:throws (fid 2)] [:gravity.type/bool])
            result (c7 'sh08-tc-products core types functions bindings direct 65536)
            facts {:type-table types :constraint-ledger (:constraints result)
                   :function-type-table (c7 'sh08-th-functions (vec (vals functions)) (:throws result))}
            scope (c8 'body-tc-scope core (:throws direct) 65536)
            joined (assoc scope :direct-throws (:throws direct) :owners (:owners (c8 'body-throw-owners core 65536)))
            derived (c8 'body-derive-nodes core facts) graph (:nodes derived)
            binding-index (into {} (map (juxt :binding-id identity) bindings))
            verify #(c8 'body-tc-verify core %1 binding-index %2 joined 65536)]
        (is (= :accepted (:status result)) (pr-str result))
        (is (= [:gravity.type/string :gravity.type/bool] (get-in result [:throws (fid 2)]) (get-in result [:throws (fid 3)])))
        (is (= :accepted (:status derived)) (pr-str derived))
        (is (= :accepted (:status (verify facts graph))) (pr-str (verify facts graph)))
        (is (= [:gravity.type/string :gravity.type/bool] (get-in graph [(id 22) :escaping-thrown-error-types])))
        (is (= [{:index 0 :core-node-id (id (first children))}] (get-in graph [(id 22) :ordering :order])))
        (is (= 2 (count (get-in graph [(id 22) :conditional-coverage]))))
        (is (= #{:gravity.type/bool} (get-in graph [(id 24) :direct-thrown-errors])))
        (is (true? (c8 'body-graph-equations-valid? core facts {} graph)))
        (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc facts :module {:effects []}) {:nodes graph :functions {}} 65536))))
        (doseq [bad [(assoc facts :constraint-ledger [])
                     (assoc-in facts [:constraint-ledger 0 :dependencies :callee-source-body-id] :forged)]]
          (is (= :rejected (:status (verify bad graph)))))
        (let [bad (assoc-in graph [(id 22) :escaping-thrown-error-types] [])]
          (is (false? (c8 'body-graph-equations-valid? core facts {} bad)))
          (is (= :rejected (:status (verify facts bad)))))
        (doseq [bad [(update core :nodes #(mapv (fn [n] (if (= (:node-id n) (id 22)) (assoc-in n [:source :syntax-id] :forged) n)) %))
                     (update core :nodes #(mapv (fn [n] (if (= (:node-id n) (id 22)) (assoc-in n [:evaluation :order] []) n)) %))]]
          (is (false? (c8 'body-graph-equations-valid? bad facts {} graph))))
        (is (= :rejected (:status (c8 'body-derive-nodes (assoc-in core [:match-branch-records 0 :branch-core-node-id] :forged) facts))))
        (is (= "C7-GENERIC" (get-in (c7 'sh08-tc-products core types functions bindings direct 1) [:diagnostics 0 :rule])))
        (is (= :rejected (:status (c8 'body-tc-verify core facts binding-index graph joined 1))))
        (doseq [bad [(update core :nodes #(mapv (fn [n] (if (= (:node-id n) (id 22)) (assoc n :core-form :unknown) n)) %))
                     (assoc core :recursion-components [{:function-syntax-ids [(fid 2)]}])]]
          (is (= :rejected (:status (c7 'sh08-tc-products bad types functions bindings direct 65536))))
          (is (= :rejected (:status (c8 'body-tc-verify bad facts binding-index graph joined 65536)))))))))

(defn- rebuild-boolean-match-kernel-data [data context]
  (let [leaf (:leaf data) core (:core context)
        products {:status :accepted :node-table (:nodes context) :binding-index (:bindings context)}
        joined (leaf 'sh08-bm-products [core products (vec (vals (:forms context)))])
        checked (leaf 'sh08-bm-check [(first (:boolean-matches joined)) (:types context) {} {:profile :meta :target :jvm}])]
    (assoc data :context (assoc context :ledger (:constraints checked)) :producer-status (:status joined) :producer-diagnostic (:diagnostic checked))))

(defn- boolean-match-variant-kernel-data [data variant]
  (let [{:keys [context match-id scrutinee-id bodies remainder]} data
        original-branches (get-in context [:core :match-branch-records])
        original-patterns (get-in context [:core :match-pattern-records])
        fields [:clause-ordinal :branch-child-index :pattern-kind :pattern-form-id :pattern-syntax-id :pattern-value
                :branch-form-id :branch-syntax-id :pattern-binding-id :pattern-binding-scope-id :pattern-binding-ids]
        false-first? (= :false-first variant) wildcard? (= :wildcard variant)
        region {:kind :match-branch :match-syntax-id (get-in context [:nodes match-id :source :syntax-id]) :clause-ordinal 0}
        branch (-> (get original-branches 1) (assoc :ordinal 0 :clause-ordinal 0 :clause-count 1 :branch-child-index 1 :conditional-region region))
        pattern (-> (get original-patterns 1) (assoc :ordinal 0 :root-ordinal 0 :clause-ordinal 0))
        clear-binding (fn [row kind value]
                        (assoc row :pattern-kind kind :pattern-value value :pattern-binding-id nil :pattern-binding-scope-id nil
                                   :pattern-binding-use-syntax-ids []))
        branches (if false-first?
                   [(assoc (get original-branches 0) :pattern-value false)
                    (assoc (clear-binding (get original-branches 1) :literal true) :pattern-binding-ids [])]
                   [(if wildcard? (assoc (clear-binding branch :wildcard '_) :pattern-binding-ids []) branch)])
        patterns (if false-first?
                   [(assoc (get original-patterns 0) :pattern-value false) (clear-binding (get original-patterns 1) :literal true)]
                   [(if wildcard? (clear-binding pattern :wildcard '_) pattern)])
        selected-bodies (if false-first? bodies [(bodies 1)])
        body (get-in context [:nodes (bodies 1)]) body-form-id (get-in body [:source :form-id])
        pattern-form-id (:pattern-form-id (get original-branches 1))
        body (cond-> body (not false-first?) (assoc-in [:evaluation :region] region)
                     (or false-first? wildcard?) (assoc :core-form :literal :children [] :attributes {:literal-kind :boolean :value false}))
        node (-> (get-in context [:nodes match-id]) (assoc :children (into [scrutinee-id] selected-bodies)
                                                         :resolved-binding-ids (if (or false-first? wildcard?) [] [remainder]))
                 (assoc-in [:attributes :branch-count] (count branches))
                 (assoc-in [:attributes :branch-child-indexes] (vec (range 1 (inc (count branches)))))
                 (assoc-in [:attributes :branch-clauses] (mapv #(select-keys % fields) branches)))
        owner-id (get-in node [:source :form-id]) original-owner-ids (get-in context [:forms owner-id :child-form-ids])
        context (-> context (assoc-in [:nodes match-id] node) (assoc-in [:nodes (bodies 1)] body)
                    (assoc-in [:core :match-branch-records] branches) (assoc-in [:core :match-pattern-records] patterns)
                    (assoc-in [:core :match-decision-skeletons 0 :branch-core-node-ids] selected-bodies))
        context (if false-first?
                  (assoc-in context [:forms (get-in original-branches [0 :pattern-form-id]) :value] false)
                  (assoc-in context [:forms owner-id :child-form-ids] (into (subvec original-owner-ids 0 2) [pattern-form-id body-form-id])))
        context (if (or false-first? wildcard?)
                  (-> context (assoc-in [:forms pattern-form-id :kind] (if wildcard? :symbol :boolean))
                      (assoc-in [:forms pattern-form-id :value] (if wildcard? '_ true))
                      (assoc-in [:forms pattern-form-id :scope-id] nil)
                      (assoc-in [:forms body-form-id :kind] :boolean) (assoc-in [:forms body-form-id :value] false)) context)
        context (assoc-in context [:core :nodes] (mapv (:nodes context) (into [scrutinee-id] (conj selected-bodies match-id))))]
    (rebuild-boolean-match-kernel-data data context)))

(deftest current-gravity-boolean-match-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))
        {:keys [context match-id bodies remainder]} (typed-boolean-match-kernel-data base)
        compile #(invoke 'p15-s23-typed-compile-node %1 match-id %2)
        result (compile context 65536)
        expected {:op :if :test {:op :local :name 'flag} :then {:op :literal :value true}
                  :else {:op :let :bindings [{:name 'remainder :expr {:op :literal :value false}}] :body [{:op :local :name 'remainder}]}}]
    (is (= :complete (:status result)) (pr-str result))
    (is (= expected (:value result)))
    (is (= result (compile context (:work-count result))))
    (is (= :typed-lowering-work-limit (get-in (compile context (dec (:work-count result))) [:facts :reason])))
    (doseq [changed [(assoc context :ledger []) (update context :ledger #(vec (reverse %)))
                     (update context :ledger conj (first (:ledger context)))
                     (assoc-in context [:ledger 0 :constraint-id-request :match-core-node-id] (bodies 0))
                     (assoc-in context [:ledger 0 :remaining-before] [false true])
                     (assoc-in context [:ledger 0 :matched-values] [false])
                     (assoc-in context [:ledger 0 :profile] :hosted)
                     (assoc-in context [:nodes match-id :attributes :guard-rule-version] :guard)
                     (assoc-in context [:ledger 1 :pattern-binding-scope-id] :forged)
                     (assoc-in context [:ledger 1 :pattern-binding-id] :forged)
                     (assoc-in context [:ledger 1 :branch-source :syntax-id] :forged)
                     (assoc-in context [:ledger 1 :constraint-id-request :branch :pattern-binding-use-syntax-ids] [])
                     (assoc-in context [:nodes (bodies 1) :attributes :binding-id] :forged)
                     (assoc-in context [:bindings remainder :scope-id] :forged)
                     (assoc-in context [:core :match-decision-skeletons 0 :branch-core-node-ids] (vec (reverse bodies)))]]
      (is (= :rejected (:status (compile changed 65536)))))
    (let [branch (get-in context [:core :match-branch-records 1]) pattern (get-in context [:core :match-pattern-records 1])
          row (get-in context [:ledger 1]) skeleton (get-in context [:core :match-decision-skeletons 0])
          node (get-in context [:nodes match-id])
          fixed (+ 20 (count (get-in context [:core :nodes])))
          result (invoke 'p15-s23-typed-boolean-match-branch context node branch pattern row skeleton 1 [false] fixed)]
      (is (= :typed-lowering-work-limit (get-in result [:facts :reason]))))
    (let [cyclic (assoc-in context [:nodes (bodies 1) :children] [(bodies 1)])
          result (invoke 'p15-s23-typed-match-uses cyclic (bodies 1) remainder 100)]
      (is (= :cyclic-typed-match-body (get-in result [:facts :reason]))))))

(deftest current-gravity-boolean-match-routes-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        data (typed-boolean-match-kernel-data (:value (invoke 'p15-s23-typed-context module input observation)))
        variants (into {} (map (fn [kind] [kind (boolean-match-variant-kernel-data data kind)])) [:false-first :binder :wildcard])
        runtime-plan (:plan (typed-catch-runtime-projection))
        evaluate (fn [instruction flag] (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction
                                                                                     [instruction {'flag flag 'remainder :outer} ""]))
        lowered (into {} (map (fn [[kind {:keys [context producer-status producer-diagnostic]}]]
                               (is (= :accepted producer-status) (pr-str producer-diagnostic))
                               (let [result (invoke 'p15-s23-typed-compile-node context (:match-id data) 65536)]
                                 (is (= :complete (:status result)) (pr-str result)) [kind (:value result)]))) variants)
        lit (fn [value] {:op :literal :value value})
        say (fn [instruction] {:op :do :body [{:op :println :args [(lit "test")] :effect :io/write :capability :io/stdout} instruction]})
        payload (String. "match transfer")]
    (is (= {:op :if :test {:op :local :name 'flag} :then (lit false) :else (lit true)} (:false-first lowered)))
    (is (= {:op :let :bindings [{:name 'remainder :expr {:op :local :name 'flag}}] :body [{:op :local :name 'remainder}]} (:binder lowered)))
    (is (= {:op :do :body [{:op :local :name 'flag} (lit false)]} (:wildcard lowered)))
    (is (= [false "" :normal] (evaluate (:false-first lowered) true)))
    (is (= [true "" :normal] (evaluate (:false-first lowered) false)))
    (is (= [false "" :normal] (evaluate (:binder lowered) false)))
    (is (= [true "" :normal] (evaluate (:binder lowered) true)))
    (doseq [[kind instruction] lowered]
      (let [marked (case kind :false-first (update instruction :test say)
                              :binder (update-in instruction [:bindings 0 :expr] say)
                              :wildcard (update-in instruction [:body 0] say))]
        (is (= "test\n" (second (evaluate marked true)))))
      (let [throwing (case kind :false-first (assoc instruction :test {:op :throw :value (lit payload)})
                                :binder (assoc-in instruction [:bindings 0 :expr] {:op :throw :value (lit payload)})
                                :wildcard (assoc-in instruction [:body 0] {:op :throw :value (lit payload)}))
            result (evaluate throwing true)]
        (is (= [payload "" :thrown] result))
        (is (identical? payload (first result)))))
    (is (= [:outer "" :normal] (evaluate {:op :do :body [(:binder lowered) {:op :local :name 'remainder}]} true)))))

(defn- typed-structural-match-kernel-data
  ([base] (typed-structural-match-kernel-data base :unguarded))
  ([base variant]
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [helper #(ns-resolve 'gravity.self-hosting.sh08-function-call-type-test %)
        invoke (fn [f args] ((helper 'invoke-c7) f args))
        {:keys [core products forms nodes bindings lexical binding-types item match-id sha]}
        (case variant :false ((helper 'literal-guard-input) false true)
                      :true ((helper 'literal-guard-input) true false)
                      :never ((helper 'throwing-guard-input))
                      ((helper 'structural-match-products-input)))
        forms (-> forms (assoc-in [(sha 109) :value] 'packet) (assoc-in [(sha 109) :parent-form-id] match-id)
                  (assoc-in [(sha 501) :value] 'fallback)
                  (assoc (sha 503) {:form-id (sha 503) :syntax-id (sha 503) :kind :symbol :value 'match
                                    :parent-form-id match-id :child-form-ids []}))
        forms (reduce (fn [out node] (let [id (get-in node [:source :form-id])]
                                       (if (get-in out [id :source-span]) out
                                           (assoc-in out [id :source-span] (get-in node [:source :semantic-span]))))) forms (:nodes core))
        forms (into {} (map (fn [[id form]] [id (assoc form :origin-chain [] :generated-origin nil)]) forms))
        nodes (into {} (map (fn [node] [(:node-id node) (assoc node :source {:form-id (get-in node [:source :form-id])
                                 :syntax-id (get-in node [:source :syntax-id]) :semantic-span (get-in forms [(get-in node [:source :form-id]) :source-span])
                                 :origin-chain [] :generated-origin nil})])) (:nodes core))
        core (assoc core :nodes (mapv #(get nodes (:node-id %)) (:nodes core)) :module (get-in base [:core :module]))
        products (assoc products :node-table nodes)
        joined (invoke 'sh08-bm-products [core products (vec (vals forms))])
        inference (invoke 'sh08-ft-infer-acyclic-with-context
                          [(:nodes core) nodes lexical [] [] [] {} binding-types 50 {:boolean-matches (:boolean-matches joined)}])
        types (:type-table inference)
        checked (invoke 'sh08-bm-checks [(:boolean-matches joined) types {} {:profile :meta :target :jvm}])
        context (-> base (update :forms merge forms) (update :nodes merge nodes) (update :types merge types)
                    (update :bindings merge bindings) (update :core merge core)
                    (assoc :ledger (:constraints checked) :locals {(sha 2000) 'packet (sha 1002) 'fallback}
                           :parameter-types {(sha 2000) (get types (sha 109)) (sha 1002) :gravity.type/i64} :tail? true))]
    {:context context :match-id match-id :sha sha :item item :checked checked :joined joined})))

(deftest current-gravity-structural-match-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        {:keys [context match-id sha item checked joined]} (typed-structural-match-kernel-data (:value (invoke 'p15-s23-typed-context module input observation)))
        compile #(invoke 'p15-s23-typed-compile-node %1 match-id %2)
        result (compile context 65536)
        expected {:op :structural-match :scrutinee {:op :local :name 'packet}
                  :clauses [{:pattern {:kind :map :entries [{:key :payload :pattern {:kind :vector :items
                                     [{:kind :literal :family :boolean :value true}
                                      {:kind :map :entries [{:key :value :pattern {:kind :binding :name 'item}}]}]}}]}
                             :body {:op :local :name 'item}}
                            {:pattern {:kind :wildcard} :body {:op :local :name 'fallback}}]}]
    (is (= :accepted (:status joined)) (pr-str joined))
    (is (= [] (:diagnostics checked)) (pr-str checked))
    (is (= :complete (:status result)) (pr-str result))
    (is (= expected (:value result)))
    (when (= :complete (:status result))
      (is (= result (compile context (:work-count result))))
      (is (= :typed-lowering-work-limit (get-in (compile context (dec (:work-count result))) [:facts :reason]))))
    (is (= {:structural-match 1 :local 3} (:value (invoke 'p15-s23-summary-instruction expected {}))))
    (doseq [changed [(assoc context :ledger []) (update context :ledger #(vec (reverse %)))
                     (update context :ledger conj (first (:ledger context)))
                     (assoc-in context [:ledger 0 :constraint-id-request :pattern-tree :children 0 :coordinate] 0)
                     (assoc-in context [:ledger 0 :extractions 0 :scope-id] :forged)
                     (assoc-in context [:ledger 0 :solution :binding-extractions 0 :constructor-path] [])
                     (assoc-in context [:ledger 0 :branch-source :syntax-id] :forged)
                     (assoc-in context [:bindings item :scope-id] :forged)
                     (assoc-in context [:nodes match-id :attributes :guard-rule-version] :guard)
                     (assoc-in context [:core :match-pattern-records 4 :path] [])
                     (assoc-in context [:forms (sha 606) :value] 'other)
                     (assoc-in context [:nodes (sha 500) :attributes :binding-id] :other)]]
      (is (= :rejected (:status (compile changed 65536)))))))

(deftest current-gravity-structural-integer-pattern-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [invoke (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        tree {:record {:pattern-kind :literal :pattern-value 7} :form {:kind :integer}}
        shape {:shape-kind :scalar :type :gravity.type/integer :domain {:values [7 8]}}
        result (invoke 'sh08-sm-split [shape tree])]
    (is (= :accepted (:status result)))
    (is (= [7] (get-in result [:selected 0 :domain :values])))
    (is (= [8] (get-in result [:residual 0 :domain :values])))
    (is (= :unsupported-numeric-pattern-elaboration (:reason (invoke 'sh08-sm-split [(assoc shape :type :gravity.type/i64) tree]))))))

(deftest current-gravity-structural-match-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        evaluate #(bootstrap/p15-s23-stage2-runtime-execute-function runtime plan 'p15-s23-runtime-evaluate-closed-instruction [% {'item :outer} ""])
        lit (fn [value] {:op :literal :value value})
        atom-pattern (fn [family value] {:kind :literal :family family :value value})
        binder {:kind :binding :name 'item} wildcard {:kind :wildcard}
        pattern {:kind :map :entries [{:key :payload :pattern {:kind :vector :items [(atom-pattern :boolean true)
                           {:kind :map :entries [{:key :value :pattern binder}]}]}}]}
        instruction (fn [value pattern] {:op :structural-match :scrutinee (lit value)
                                         :clauses [{:pattern pattern :body {:op :local :name 'item}}
                                                   {:pattern wildcard :body (lit :fallback)}]})
        payload (String. "structural payload")
        accepted (instruction {:extra :retained :payload [true {:value payload}]} pattern)]
    (is (= [payload "" :normal] (evaluate accepted)))
    (is (identical? payload (first (evaluate accepted))))
    (doseq [value [{:payload [false {:value payload}]} {:payload [true {:value payload} :extra]} {:payload []}
                   {:payload [true {}]} {:missing [true {:value payload}]} [] nil]]
      (is (= [:fallback "" :normal] (evaluate (instruction value pattern)))))
    (let [nil-pattern {:kind :map :entries [{:key :present :pattern (atom-pattern :nil nil)}]}]
      (is (= [:outer "" :normal] (evaluate (instruction {:present nil} nil-pattern))))
      (is (= [:fallback "" :normal] (evaluate (instruction {} nil-pattern)))))
    (doseq [[family value other] [[:boolean true 1] [:string "same" :same] [:keyword :same "same"]
                                  [:symbol 'same "same"] [:character \a "a"] [:integer 7 "7"] [:nil nil false]]]
      (is (= [:outer "" :normal] (evaluate (instruction value (atom-pattern family value)))))
      (is (= [:fallback "" :normal] (evaluate (instruction other (atom-pattern family value))))))
    (let [partial {:kind :vector :items [binder (atom-pattern :boolean true)]}]
      (is (= [:fallback "" :normal] (evaluate (instruction [payload false] partial))))
      (is (= [:outer "" :normal] (evaluate {:op :structural-match :scrutinee (lit [payload false])
                                             :clauses [{:pattern partial :body (lit :unreachable)}
                                                       {:pattern wildcard :body {:op :local :name 'item}}]})))
      (is (= [:outer "" :normal] (evaluate {:op :do :body [accepted {:op :local :name 'item}]}))))
    (let [marker {:op :println :args [(lit "once")] :effect :io/write :capability :io/stdout}
          marked (update accepted :scrutinee #(hash-map :op :do :body [marker %]))]
      (is (= [payload "once\n" :normal] (evaluate marked)))
      (let [result (evaluate (assoc marked :scrutinee {:op :do :body [marker {:op :throw :value (lit payload)}]}))]
        (is (= [payload "once\n" :thrown] result)) (is (identical? payload (first result))))
      (is (= "" (second (evaluate (assoc-in marked [:clauses 0 :pattern] {:kind :unknown}))))))
    (let [equiv-calls (atom 0) hash-calls (atom 0) failure (AssertionError. "opaque comparison")
          opaque (HostilePersistentCallee. equiv-calls hash-calls failure)]
      (doseq [[family value] [[:nil nil] [:boolean true] [:string "s"] [:keyword :key] [:symbol 's] [:character \a] [:integer 7]]]
        (is (= [:fallback "" :normal] (evaluate (instruction opaque (atom-pattern family value))))))
      (is (= [0 0] [@equiv-calls @hash-calls])))
    (let [again {:op :recur :target "selected" :args [(lit payload)]}
          selected (assoc accepted :clauses [{:pattern wildcard :body again}])
          execute #(bootstrap/p15-s23-stage2-runtime-execute-function runtime plan 'p15-s23-runtime-evaluate-with-targets
                    [% {} "" {} [] [{:id "selected" :names ['value]}]])
          transfer (execute selected)]
      (is (= [[payload] "" :recur "selected"] transfer))
      (is (identical? payload (get-in transfer [0 0])))
      (is (= :rejected (get (execute (assoc selected :scrutinee again)) 2))))
    (let [failure (AssertionError. "original fatal") environment (ThrowingAssociative. failure)
          observed (seed-throw-test-capture #(bootstrap/p15-s23-stage2-runtime-execute-function runtime plan
                     'p15-s23-runtime-evaluate-closed-instruction [(instruction payload binder) environment ""]))]
      (is (identical? failure observed)))
    (let [rejected (evaluate (assoc accepted :scrutinee {:op :unsupported}))]
      (is (= ["" :rejected] (subvec rejected 1)))
      (is (= "P15S23X002" (get-in rejected [0 :diagnostic]))))
    (doseq [bad [(assoc accepted :extra true) (assoc accepted :clauses [])
                 (assoc-in accepted [:clauses 0 :pattern :entries 0 :key] "payload")
                 (assoc-in accepted [:clauses 0 :pattern] {:kind :literal :family :decimal :value {:unscaled-value "7"}})
                 (assoc-in accepted [:clauses 0 :pattern] {:kind :vector :items [binder binder]})
                 (assoc-in accepted [:clauses 0 :pattern] {:kind :map :entries [{:key :a :pattern wildcard} {:key :a :pattern wildcard}]})
                 (assoc-in accepted [:clauses 0 :pattern] {:kind :wildcard :extra true})
                 (assoc-in accepted [:clauses 0 :pattern] (nth (iterate #(hash-map :kind :vector :items [%]) wildcard) 130))
                 (assoc-in accepted [:clauses 0 :pattern] {:kind :vector :items (vec (repeat 65536 wildcard))})
                 (assoc-in accepted [:clauses 0 :body] nil)]]
      (let [result (evaluate bad)]
        (is (= :rejected (get result 2))) (is (= "P15S23X002" (get-in result [0 :diagnostic])))))
    (let [no-match (assoc accepted :clauses [{:pattern (atom-pattern :boolean true) :body (lit :bad)}])]
      (is (= :structural-match-no-selected-clause (get-in (evaluate no-match) [0 :reason]))))))

(deftest current-gravity-root-map-guard-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke #(bootstrap/p15-s23-stage2-runtime-execute-function runtime plan %1 %2)
        lit (fn [value] {:op :literal :value value}) local {:op :local :name 'item}
        say (fn [label body] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} body]})
        payload (String. "guard payload") outer (String. "outer binding")
        pattern {:kind :map :entries [{:key :value :pattern {:kind :binding :name 'item}}]}
        match (fn [guard] {:op :structural-match :scrutinee (say "source" (lit {:value payload}))
                           :clauses [{:pattern pattern :guard guard :body (say "body" local)}
                                     {:pattern {:kind :wildcard} :body (say "fallback" local)}]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction [% {'item outer} ""])]
    (doseq [[flag expected output] [[true payload "source\nguard\nbody\n"] [false outer "source\nguard\nfallback\n"]]]
      (let [result (evaluate (match (say "guard" (lit flag))))]
        (is (= [expected output :normal] result)) (is (identical? expected (first result)))))
    (let [throwing (match (say "guard" {:op :throw :value local})) result (evaluate throwing)]
      (is (= [payload "source\nguard\n" :thrown] result)) (is (identical? payload (first result))))
    (let [skipped (assoc (match {:op :throw :value local}) :scrutinee (say "source" (lit {})))]
      (is (= [outer "source\nfallback\n" :normal] (evaluate skipped))))
    (doseq [flag [true false]]
      (let [guard {:op :if :test (lit flag) :then {:op :throw :value local} :else (say "allowed" (lit true))}
            result (evaluate (match guard))]
        (is (= (if flag [payload "source\n" :thrown] [payload "source\nallowed\nbody\n" :normal]) result))
        (is (identical? payload (first result)))))
    (let [caught {:op :try :protected {:op :throw :value local}
                  :handlers [{:type :gravity.type/string :name 'caught :body (say "caught" (lit false))}]}]
      (is (= [outer "source\ncaught\nfallback\n" :normal] (evaluate (match caught))))
      (let [result (evaluate (match (assoc-in caught [:handlers 0 :body] {:op :throw :value {:op :local :name 'caught}})))]
        (is (= [payload "source\n" :thrown] result)) (is (identical? payload (first result)))))
    (doseq [value [nil 0 1 "truthy" :truthy (Object.)]]
      (let [result (evaluate (match (say "guard" (lit value))))]
        (is (= ["source\nguard\n" :rejected] (subvec result 1)))
        (is (= :non-boolean-structural-guard (get-in result [0 :reason])))))
    (let [again {:op :recur :target "outer" :args [(lit payload)]}
          active #(invoke 'p15-s23-runtime-evaluate-with-targets [% {'item outer} "" {} [] [{:id "outer" :names ['value]}]])
          selected (assoc-in (match (lit true)) [:clauses 0 :body] again)]
      (is (= [[payload] "source\n" :recur "outer"] (active selected)))
      (is (= :rejected (get (active (match again)) 2)))
      (let [nested {:op :loop :target "guard-loop" :bindings [{:name 'again :expr (lit true)}]
                    :body [{:op :if :test {:op :local :name 'again}
                            :then {:op :recur :target "guard-loop" :args [(lit false)]} :else (lit true)}]}]
        (is (= [payload "source\nbody\n" :normal] (active (match nested))))))
    (let [failure (AssertionError. "guard original fatal") environment (ThrowingAssociative. failure)
          instruction {:op :structural-match :scrutinee (lit {}) :clauses [{:pattern {:kind :wildcard}
                         :guard {:op :let :bindings [{:name 'temporary :expr (lit true)}] :body [(lit true)]} :body (lit :unreachable)}]}
          observed (seed-throw-test-capture #(invoke 'p15-s23-runtime-evaluate-closed-instruction [instruction environment ""]))]
      (is (identical? failure observed)))
    (let [result (evaluate (match {:op :unsupported}))]
      (is (= ["source\n" :rejected] (subvec result 1))) (is (= "P15S23X002" (get-in result [0 :diagnostic]))))
    (doseq [guard [nil 42 []]]
      (is (= ["" :rejected] (subvec (evaluate (match guard)) 1))))))

(deftest current-gravity-root-map-guard-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation))]
    (let [scrutinee {:node-id :inner-scrutinee :core-form :literal :children []}
          guard {:node-id :inner-guard :core-form :reference :children [] :attributes {:binding-id :outer-binder} :source {:syntax-id :guard-use}}
          body {:node-id :inner-body :core-form :reference :children [] :attributes {:binding-id :outer-binder} :source {:syntax-id :body-use}}
          nested {:node-id :inner-match :core-form :match :children [:inner-scrutinee :inner-body :inner-guard]}
          sequence [scrutinee guard body nested]
          context (assoc base :nodes (into {} (map (juxt :node-id identity) sequence)) :core {:nodes sequence})
          uses (invoke 'p15-s23-typed-match-uses context :inner-match :outer-binder 65536)]
      (is (= [:guard-use :body-use] (:uses uses)))
      (is (= uses (invoke 'p15-s23-typed-match-uses context :inner-match :outer-binder (:work-count uses))))
      (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-match-uses context :inner-match :outer-binder (dec (:work-count uses))) [:facts :reason]))))
    (doseq [variant [:false :true]]
      (let [{:keys [context match-id sha joined checked]} (typed-structural-match-kernel-data base variant)
            result (invoke 'p15-s23-typed-compile-node context match-id 65536)
            guard (get-in context [:core :match-branch-records 0 :guard])]
        (is (= :accepted (:status joined)) (pr-str joined))
        (is (= [] (:diagnostics checked)) (pr-str checked))
        (is (= :complete (:status result)) (pr-str result))
        (is (= {:op :literal :value (= variant :true)} (get-in result [:value :clauses 0 :guard])))
        (is (= {:op :local :name 'item} (get-in result [:value :clauses 0 :body])))
        (when (= :complete (:status result))
          (is (= result (invoke 'p15-s23-typed-compile-node context match-id (:work-count result))))
          (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context match-id (dec (:work-count result))) [:facts :reason]))))
        (doseq [changed [(assoc-in context [:ledger 0 :guard :type] :gravity.type/string)
                         (assoc-in context [:ledger 0 :guard :truth] :unknown)
                         (assoc-in context [:ledger 0 :solution :effective-result-type] :gravity.type/bool)
                         (assoc-in context [:core :match-branch-records 0 :guard :child-index] 1)
                         (assoc-in context [:forms (sha 701) :value] :other)
                         (assoc-in context [:core :match-pattern-records 0 :guard :source-child-index] 1)]]
          (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node changed match-id 65536)))))
        (is (= (if (= variant :true) {:structural-match 1 :local 2 :literal 1} {:structural-match 1 :local 3 :literal 1})
               (:value (invoke 'p15-s23-summary-instruction (:value result) {}))))))))

(deftest current-gravity-root-map-guard-fixtures-development-kernel
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))]
    (doseq [[disposition basename functions] [["accepted" "local-runtime-root-map-guards" 14]
                                             ["accepted" "function-root-map-guard-false-result" 1]
                                             ["rejected" "function-root-map-guard-result" 1]
                                             ["rejected" "function-root-map-guard-throw" 1]
                                             ["rejected" "local-runtime-guard-false-call" 2]
                                             ["rejected" "local-runtime-guard-never-call" 2]
                                             ["rejected" "local-runtime-guard-false-unresolved" 1]
                                             ["rejected" "local-runtime-guard-never-unresolved" 1]]]
      (let [base (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename)
            path (.getPath (io/file root (str base ".gravity"))) text (slurp path)]
        (is (= text (slurp (io/file root (str base ".qst")))))
        (is (= functions (count (filter #(and (seq? %) (= 'defn (first %))) (bootstrap/read-forms path text)))))))))

(deftest current-gravity-root-map-guard-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                              (fn [runtime plan function args]
                                                (let [result (original runtime plan function args)]
                                                  (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                   (thunk))]
                      (is (= 1 (- (count @verified) before))) result))
        module-data (fn [input path]
                      (let [artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                            parsed (get-in artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                            module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms artifact))
                            definitions (bootstrap/stage0-function-table module)]
                        {:module (assoc module :function-table definitions) :definitions definitions}))
        payload (String. "source guard payload") fallback (String. "outer fallback") problem (String. "source guard failure")
        specifications [['choose [true payload fallback] payload :normal] ['choose [false payload fallback] fallback :normal]
                        ['false-result [payload fallback] fallback :normal] ['never-total [problem] problem :thrown]
                        ['never-subset [true problem fallback] problem :thrown] ['never-subset [false problem fallback] fallback :normal]
                        ['inline-if [true true payload fallback] payload :thrown]
                        ['inline-if [false true payload fallback] payload :normal] ['inline-if [false false payload fallback] fallback :normal]
                        ['caught [true payload fallback] payload :normal] ['caught [false payload fallback] fallback :normal]
                        ['nested [true] true :normal] ['nested [false] false :normal]
                        ['nested-loop [payload fallback] payload :normal]
                        ['selected-recur [true true payload fallback] payload :normal]
                        ['selected-recur [true false payload fallback] fallback :normal]
                        ['selected-recur [false true payload fallback] payload :normal]
                        ['forward [true payload] payload :normal] ['forward [false payload] payload :thrown]
                        ['called [true payload fallback] payload :normal] ['called [false payload fallback] fallback :normal]
                        ['main [] "payload" :normal]]
        cases (mapv
               (fn [extension]
                 (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-root-map-guards" extension)
                       path (.getPath (io/file root relative)) text (slurp path)
                       products (fresh relative) input (select-keys products [:request :typed :verification])
                       {:keys [module definitions]} (module-data input path)
                       typed-check (is (= :accepted (get-in input [:typed :status])) (pr-str (take 3 (get-in input [:typed :diagnostics]))))
                       verification-check (is (= :passed (get-in input [:verification :status])))
                       ready (when-not (and typed-check verification-check)
                               (throw (ex-info "Root-map guard source products rejected before lowering"
                                               {:typed-status (get-in input [:typed :status]) :diagnostics (vec (take 3 (get-in input [:typed :diagnostics])))})))
                       standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'choose) input))
                       assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                       rows (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                       guarded (filterv :guard rows) core (get-in input [:request :canonical-core-artifact])
                       facts (get-in input [:typed :identity-input])
                       functions (into {} (map (juxt :definition-name identity) (:function-type-table facts)))
                       effects (c8 'body-analyze-module (:request input) facts {} 65536)]
                   (is (= 14 (count (:functions assembled))))
                   (is (= 21 (count rows)))
                   (is (= 10 (count guarded)))
                   (is (= {:unknown 7 :false 1 :never 2} (frequencies (map #(get-in % [:guard :truth]) guarded))))
                   (is (= {:structural-match 11 :map-literal 11 :if 3 :loop 1 :recur 2 :try 1 :throw 5}
                          (select-keys (:instruction-summary assembled) [:structural-match :map-literal :if :loop :recur :try :throw])))
                   (is (= standalone (get-in assembled [:functions 'choose])))
                   (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
                   (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
                   (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
                   (is (= :gravity.type/string (get-in functions ['forward :return]) (get-in functions ['later :return])))
                   (is (nil? (get-in functions ['forward :signature :return])))
                   (is (nil? (get-in functions ['later :signature :return])))
                   (is (= :gravity.type/never (get-in facts [:type-table (get-in functions ['never-total :body-core-node-id])])) )
                   (let [filtered (filterv #(= :not-executed (get-in % [:solution :body-completion])) guarded)]
                     (is (= 3 (count filtered)))
                     (is (every? #(= :gravity.type/bool (:rhs %) (get-in % [:solution :result-type])) filtered))
                     (is (every? #(= :gravity.type/never (get-in % [:solution :effective-result-type])) filtered)))
                   (doseq [[name args expected status] specifications]
                     (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                            [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                       (is (= [expected "" status] result) [extension name result])
                       (when (or (identical? expected payload) (identical? expected fallback) (identical? expected problem))
                         (is (identical? expected (first result))))))
                   (is (= :accepted (:status effects)) (pr-str (select-keys effects [:reason :diagnostics])))
                   (when (= :accepted (:status effects))
                     (doseq [row guarded]
                       (let [id (get-in row [:guard :core-node-id]) match-id (get-in row [:constraint-id-request :match-core-node-id])]
                         (is (some #(= id (:node-id %)) (get-in effects [:graph :nodes match-id :conditional-coverage])))
                         (is (some #(= id %) (get-in effects [:graph :nodes match-id :dependencies])))))
                     (doseq [name ['never-total 'never-subset 'inline-if 'forward]]
                       (is (= [:gravity.type/string] (get-in effects [:graph :functions (get-in functions [name :function-id]) :throws]))))
                     (let [caught-row (first (filter #(= (get-in functions ['caught :body-core-node-id])
                                                         (get-in % [:constraint-id-request :match-core-node-id])) guarded))
                           guard-id (get-in caught-row [:guard :core-node-id])]
                       (is (= [] (get-in effects [:graph :nodes guard-id :escaping-thrown-error-types])))
                       (is (= [:gravity.type/string] (get-in effects [:graph :nodes guard-id :covered-source-errors 0 :matched-error-types])))
                       (is (= [] (get-in effects [:graph :functions (get-in functions ['caught :function-id]) :throws]))))
                     (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) (:graph effects) 65536)))))
                   {:input input :module module :definitions definitions :rows rows :guarded guarded})) [".gravity" ".qst"])
        {:keys [input module definitions guarded]} (first cases)
        ledger (get-in input [:typed :constraint-ledger]) row (first guarded)
        row-index (first (keep-indexed #(when (= (:constraint-id-request row) (:constraint-id-request %2)) %1) ledger))
        guard (assoc (:guard row) :type :gravity.type/string)
        changed-type (-> row (assoc :guard guard) (assoc-in [:dependencies :guard] guard) (assoc-in [:constraint-id-request :guard] guard))
        false-guard (assoc (:guard row) :truth :false)
        changed-truth (-> row (assoc :guard false-guard :matched-shapes [])
                          (assoc-in [:dependencies :guard] false-guard) (assoc-in [:constraint-id-request :guard] false-guard)
                          (assoc-in [:solution :guard-truth] :false) (assoc-in [:solution :guard-body-shapes] [])
                          (assoc-in [:solution :selected-shapes] []) (assoc-in [:solution :effective-result-type] :gravity.type/never)
                          (assoc-in [:solution :body-completion] :not-executed))]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed-row [changed-type changed-truth
                        (assoc-in row [:solution :effective-result-type] :gravity.type/bool)
                        (assoc-in row [:guard :conditional-region :clause-ordinal] 1)]]
      (let [changed (assoc-in input [:typed :constraint-ledger row-index] changed-row)
            failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'choose) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (let [relative "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-root-map-guard-false-result.gravity"
          products (fresh relative) input (select-keys products [:request :typed :verification])
          ready (is (= :accepted (get-in input [:typed :status])) (pr-str (get-in input [:typed :diagnostics])))
          _ (when-not ready (throw (ex-info "False guard source regression rejected" {})))
          {:keys [module definitions]} (module-data input (.getPath (io/file root relative)))
          row (first (filter :guard (get-in input [:typed :constraint-ledger])))
          compiled (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'filtered) input))
          result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                           [{:op :function-call :function 'filtered :args (literal-instructions [1 2])} {} "" {'filtered compiled} []])]
      (is (= :passed (get-in input [:verification :status])))
      (is (= [:gravity.type/string :gravity.type/never] [(:rhs row) (get-in row [:solution :effective-result-type])]))
      (is (= [2 "" :normal] result)))
    (is (= 9 (count @verified)))
    (doseq [[basename rule reason] [["function-root-map-guard-result" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch]
                                    ["function-root-map-guard-throw" "L7-UNREACHABLE" :unreachable-structural-match-clause]
                                    ["function-root-map-guard-partial" "C7-ANNOTATION" :unsupported-partial-structural-match]
                                    ["function-root-map-guard-string" "L7-GUARD-TYPE" :guard-result-not-boolean]
                                    ["local-runtime-guard-false-call" "C7-TYPE-MISMATCH" :call-argument-type-mismatch]
                                    ["local-runtime-guard-never-call" "C7-TYPE-MISMATCH" :call-argument-type-mismatch]]]
      (let [products (fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" basename ".gravity"))
            failure (first (get-in products [:typed :diagnostics]))]
        (is (= :rejected (get-in products [:typed :status])))
        (is (= [rule reason] ((juxt :rule :reason) failure)) basename)))
    (doseq [basename ["local-runtime-guard-false-unresolved" "local-runtime-guard-never-unresolved" "function-root-map-guard-scope-escape"]]
      (is (= "C5-UNRESOLVED" (:id (diagnostic #(fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/" basename ".gravity")))))))
    (let [failure (diagnostic #(fresh "bootstrap/clojure/fixtures/self-hosting/sh-07-b10/rejected/guard-tail-recur.gravity"))]
      (is (= "C6-VERIFY" (:rule failure)))
      (is (= :recur-tail-position-required (get-in failure [:facts :reason]))))))

(deftest current-gravity-structural-match-fixtures-development-kernel
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))]
    (doseq [[disposition suffix functions] [["accepted" "" 14] ["rejected" "-pattern" 1]
                                           ["rejected" "-absent" 1] ["rejected" "-width" 1]
                                           ["rejected" "-branch-type" 1] ["rejected" "-binder-escape" 1]]]
      (let [base (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/local-runtime-structural-match" suffix)
            path (.getPath (io/file root (str base ".gravity"))) text (slurp path)]
        (is (= text (slurp (io/file root (str base ".qst")))))
        (is (= functions (count (filter #(and (seq? %) (= 'defn (first %))) (bootstrap/read-forms path text)))))))))

(deftest current-gravity-structural-match-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "structural source payload") fallback (String. "original fallback") problem (String. "original callee failure")
        specifications [['choose [true payload fallback] payload :normal] ['choose [false payload fallback] fallback :normal]
                        ['multi [true payload] payload :normal] ['multi [false payload] payload :normal]
                        ['rollback [true payload fallback] payload :normal] ['rollback [false payload fallback] fallback :normal]
                        ['nested [true payload] payload :normal] ['nested [false payload] payload :normal]
                        ['present-nil [] "present" :normal] ['absent-map [] "absent" :normal] ['quoted [] "quoted" :normal]
                        ['literals [] "literals" :normal]
                        ['checked [false true payload problem fallback] payload :normal]
                        ['checked [false false payload problem fallback] fallback :normal]
                        ['checked [true true payload problem fallback] problem :thrown]
                        ['checked-wrapper [false true payload problem fallback] payload :normal]
                        ['checked-wrapper [true true payload problem fallback] problem :thrown]
                        ['selected-recur [true payload] payload :normal] ['selected-recur [false payload] payload :normal]
                        ['main [] "payload" :normal]]
        cases (mapv
               (fn [extension]
                 (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-structural-match" extension)
                       path (.getPath (io/file root relative)) text (slurp path)
                       products (fresh relative) input (select-keys products [:request :typed :verification])
                       macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                       parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                       module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                       definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                       typed-check (is (= :accepted (get-in input [:typed :status])) (pr-str (take 3 (get-in input [:typed :diagnostics]))))
                       verification-check (is (= :passed (get-in input [:verification :status])))
                       ready (when-not (and typed-check verification-check)
                               (throw (ex-info "Structural-match source products rejected before lowering"
                                               {:typed-status (get-in input [:typed :status]) :diagnostics (vec (take 3 (get-in input [:typed :diagnostics])))})))
                       standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'choose) input))
                       assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                       rows (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                       core (get-in input [:request :canonical-core-artifact]) facts (get-in input [:typed :identity-input])
                       effects (c8 'body-analyze-module (:request input) facts {} 65536)
                       matches (filterv #(= :match (:core-form %)) (:nodes core))]
                   (is (= 14 (count (:functions assembled))))
                   (is (= 17 (count rows)))
                   (is (= 11 (count matches) (get-in assembled [:instruction-summary :structural-match])))
                   (is (= {:map-literal 7 :vector-literal 6 :if 2 :quote 2 :recur 1}
                          (select-keys (:instruction-summary assembled) [:map-literal :vector-literal :if :quote :recur])))
                   (is (= standalone (get-in assembled [:functions 'choose])))
                   (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
                   (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
                   (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
                   (doseq [[name args expected status] specifications]
                     (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                            [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                       (is (= [expected "" status] result) [extension name result])
                       (when (or (identical? expected payload) (identical? expected fallback) (identical? expected problem))
                         (is (identical? expected (first result))))))
                   (is (= :accepted (:status effects)) (pr-str (select-keys effects [:reason :diagnostics])))
                   (when (= :accepted (:status effects))
                     (doseq [node matches]
                       (is (= [(first (:children node))] (get-in effects [:graph :nodes (:node-id node) :evaluated-children])))
                       (is (= (dec (count (:children node))) (count (get-in effects [:graph :nodes (:node-id node) :conditional-coverage])))))
                     (let [functions (into {} (map (juxt :definition-name identity)) (:function-type-table facts))]
                       (doseq [name ['gate 'checked 'checked-wrapper]]
                         (is (= [:gravity.type/string] (get-in effects [:graph :functions (get-in functions [name :function-id]) :throws])))))
                     (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) (:graph effects) 65536)))))
                   {:input input :module module :definitions definitions :rows rows})) [".gravity" ".qst"])
        {:keys [input module definitions rows]} (first cases)
        row (first (filter #(seq (:extractions %)) rows)) ledger (get-in input [:typed :constraint-ledger])
        row-index (first (keep-indexed #(when (= (:constraint-id-request row) (:constraint-id-request %2)) %1) ledger))
        changed-extractions (assoc-in (:extractions row) [0 :constructor-path] [])
        changed-tree (assoc-in (get-in row [:constraint-id-request :pattern-tree]) [:children 0 :coordinate] 0)
        changed-scope (assoc-in (get-in row [:constraint-id-request :pattern-tree])
                               [:children 0 :tree :children 1 :tree :children 0 :tree :record :pattern-binding-scope-id] :forged)]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    ;; These coherent carrier mutations must be rejected by fresh public C7;
    ;; lowering never treats an extraction path as a runtime indexing shortcut.
    (doseq [changed-row [(-> row (assoc :extractions changed-extractions)
                            (assoc-in [:dependencies :extractions] changed-extractions)
                            (assoc-in [:solution :binding-extractions] changed-extractions))
                        (-> row (assoc-in [:constraint-id-request :pattern-tree] changed-tree) (assoc-in [:dependencies :pattern-tree] changed-tree))
                        (-> row (assoc-in [:constraint-id-request :pattern-tree] changed-scope) (assoc-in [:dependencies :pattern-tree] changed-scope))
                        (assoc row :remaining-before [] :exhaustive-after? false)]]
      (let [changed (assoc-in input [:typed :constraint-ledger row-index] changed-row)
            failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'choose) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 8 (count @verified)))
    (doseq [[suffix rule reason] [["pattern" "L7-PATTERN-TYPE" :structural-pattern-type-mismatch]
                                 ["absent" "L7-UNREACHABLE" :unreachable-structural-match-clause]
                                 ["width" "L7-UNREACHABLE" :unreachable-structural-match-clause]
                                 ["branch-type" "C7-TYPE-MISMATCH" :structural-match-branch-type-mismatch]]]
      (let [products (fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-structural-match-" suffix ".gravity"))
            failure (first (get-in products [:typed :diagnostics]))]
        (is (= :rejected (get-in products [:typed :status])))
        (is (= [rule reason] ((juxt :rule :reason) failure)))))
    (is (= "C5-UNRESOLVED" (:id (diagnostic #(fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-structural-match-binder-escape.gravity")))))))

(deftest current-gravity-boolean-match-fixtures-development-kernel
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))]
    (doseq [[disposition basename functions] [["accepted" "local-runtime-boolean-match" 15]
                                             ["rejected" "local-runtime-boolean-match-pattern" 1]
                                             ["rejected" "local-runtime-boolean-match-unreachable" 1]
                                             ["rejected" "local-runtime-boolean-match-branch-type" 1]
                                             ["rejected" "local-runtime-boolean-match-binder-escape" 1]]]
      (let [path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename ".gravity")))
            paired (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename ".qst")))
            text (slurp path)]
        (is (= text (slurp paired)))
        (is (= functions (count (filter #(and (seq? %) (= 'defn (first %))) (bootstrap/read-forms path text)))))))))

(deftest current-gravity-boolean-match-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "match payload") problem (String. "scrutinee failure")
        specifications [['true-first [true payload] payload :normal] ['true-first [false payload] "false" :normal]
                        ['false-first [false payload] payload :normal] ['false-first [true payload] "true" :normal]
                        ['residual-shadow [true payload] payload :normal] ['residual-shadow [false payload] "bound" :normal]
                        ['whole-shadow [true] true :normal] ['whole-shadow [false] false :normal]
                        ['whole-binder-string [false payload] payload :normal]
                        ['whole-wildcard [false problem payload] payload :normal] ['whole-wildcard [true problem payload] problem :thrown]
                        ['wildcard-wrapper [false problem payload] payload :normal] ['wildcard-wrapper [true problem payload] problem :thrown]
                        ['probe [false problem] false :normal] ['probe [true problem] problem :thrown]
                        ['throwing-wildcard [false problem payload] payload :thrown] ['throwing-wildcard [true problem payload] problem :thrown]
                        ['nested [true payload] payload :normal] ['nested [false payload] "after-false" :normal]
                        ['packet-match [true payload] payload :normal] ['packet-match [false payload] "packet-false" :normal]
                        ['selected-recur [true payload] payload :normal] ['selected-recur [false payload] payload :normal]
                        ['main [] "payload" :normal]]
        cases (mapv
               (fn [extension]
                 (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-boolean-match" extension)
                       path (.getPath (io/file root relative)) text (slurp path)
                       products (fresh relative) input (select-keys products [:request :typed :verification])
                       macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                       parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                       module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                       definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                       typed-check (is (= :accepted (get-in input [:typed :status])) (pr-str (take 2 (get-in input [:typed :diagnostics]))))
                       verification-check (is (= :passed (get-in input [:verification :status])))
                       ready (when-not (and typed-check verification-check)
                               (throw (ex-info "Bool-match source products rejected before typed lowering"
                                               {:typed-status (get-in input [:typed :status]) :diagnostics (vec (take 2 (get-in input [:typed :diagnostics])))})))
                       standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'true-first) input))
                       assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                       rows (filterv #(= :sh08-boolean-match-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                       core (get-in input [:request :canonical-core-artifact]) facts (get-in input [:typed :identity-input])
                       effects (c8 'body-analyze-module (:request input) facts {} 65536)
                       matches (filterv #(= :match (:core-form %)) (:nodes core))]
                   (is (= 15 (count (:functions assembled))))
                   (is (= 17 (count rows)))
                   (is (= 11 (count matches)))
                   ;; Two wildcard routes plus the existing source-let body do.
                   (is (= {:if 10 :let 5 :do 3 :recur 1 :keyword-map-lookup 1 :map-literal 1}
                          (select-keys (:instruction-summary assembled) [:if :let :do :recur :keyword-map-lookup :map-literal])))
                   (is (nil? (get-in assembled [:instruction-summary :match])))
                   (is (= standalone (get-in assembled [:functions 'true-first])))
                   (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
                   (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
                   (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
                   (doseq [[name args expected status] specifications]
                     (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                            [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                       (is (= [expected "" status] result) [extension name args result])
                       (when (or (identical? expected payload) (identical? expected problem))
                         (is (identical? expected (first result))))))
                   (is (= :accepted (:status effects)) (pr-str (select-keys effects [:reason :diagnostics])))
                   (when (= :accepted (:status effects))
                     (doseq [node matches]
                       (is (= [(first (:children node))] (get-in effects [:graph :nodes (:node-id node) :evaluated-children])))
                       (is (= (dec (count (:children node))) (count (get-in effects [:graph :nodes (:node-id node) :conditional-coverage])))))
                     (let [functions (into {} (map (juxt :definition-name identity)) (:function-type-table facts))]
                       (doseq [name ['probe 'whole-wildcard 'wildcard-wrapper 'throwing-wildcard]]
                         (is (= [:gravity.type/string] (get-in effects [:graph :functions (get-in functions [name :function-id]) :throws])))))
                     (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) (:graph effects) 65536)))))
                   {:input input :module module :definitions definitions :rows rows})) [".gravity" ".qst"])
        {:keys [input module definitions rows]} (first cases)
        row (first rows) ledger (get-in input [:typed :constraint-ledger])
        row-index (first (keep-indexed #(when (= (:constraint-id-request row) (:constraint-id-request %2)) %1) ledger))]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :constraint-ledger row-index :remaining-before] [])
                     (assoc-in input [:typed :constraint-ledger row-index :constraint-id-request :match-core-node-id] :forged)
                     (assoc-in input [:typed :constraint-ledger row-index :branch-source :syntax-id] :forged)]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'true-first) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 7 (count @verified)))
    (doseq [[basename rule reason] [["pattern" "L7-PATTERN-TYPE" :boolean-pattern-type-mismatch]
                                  ["unreachable" "L7-UNREACHABLE" :unreachable-boolean-match-clause]
                                  ["branch-type" "C7-TYPE-MISMATCH" :boolean-match-branch-type-mismatch]]]
      (let [products (fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-boolean-match-" basename ".gravity"))
            failure (first (get-in products [:typed :diagnostics]))]
        (is (= :rejected (get-in products [:typed :status])))
        (is (= [rule reason] ((juxt :rule :reason) failure)))))
    (is (= "C5-UNRESOLVED" (:id (diagnostic #(fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-boolean-match-binder-escape.gravity")))))))

(deftest current-gravity-returned-map-fixtures-development-kernel
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))]
    (doseq [[disposition basename functions] [["accepted" "local-runtime-returned-map-shape" 16]
                                             ["rejected" "local-runtime-returned-map-bool-result" 2]
                                             ["accepted" "function-structural-match-call-return" 2]]]
      (let [path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename ".gravity")))
            paired (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename ".qst")))
            text (slurp path)]
        (is (= text (slurp paired)))
        (is (= functions (count (filter #(and (seq? %) (= 'defn (first %))) (bootstrap/read-forms path text)))))))))

(deftest current-gravity-returned-map-shape-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          value (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                             (fn [runtime plan function args]
                                               (let [result (original runtime plan function args)]
                                                 (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                  (thunk))]
                      (is (= 1 (- (count @verified) before))) value))
        payload (String. "returned map payload")
        cases
        (mapv
         (fn [extension]
           (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-returned-map-shape" extension)
                 path (.getPath (io/file root relative)) text (slurp path)
                 products (fresh relative) input (select-keys products [:request :typed :verification])
                 macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                 parsed (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                 module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms macro-artifact))
                 definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
                 typed-check (is (= :accepted (get-in input [:typed :status]))
                                 (pr-str (take 2 (get-in input [:typed :diagnostics]))))
                 verification-check (is (= :passed (get-in input [:verification :status])))
                 ready (when-not (and typed-check verification-check)
                         (throw (ex-info "Returned-map source products were rejected before typed lowering"
                                         {:typed-status (get-in input [:typed :status]) :verification-status (get-in input [:verification :status])
                                          :diagnostics (vec (take 2 (get-in input [:typed :diagnostics])))})))
                 standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'lookup) input))
                 assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                 rows (filterv #(= :sh08-keyword-map-lookup-v1 (:producer-rule %)) (get-in input [:typed :constraint-ledger]))
                 function-facts (into {} (map (juxt :definition-name identity)) (get-in input [:typed :function-type-table]))
                 specifications [['lookup [payload] payload :normal] ['forward [payload] {:value payload} :normal]
                                 ['build [payload] {:value payload} :normal] ['nested [] "nested" :normal]
                                 ['nested-build [] {:outer {:value "nested"}} :normal] ['missing [] nil :normal]
                                 ['other-build [payload] {:other payload} :normal] ['present-nil [] nil :normal] ['nil-build [] {:value nil} :normal]
                                 ['quoted-lookup [] "quoted" :normal] ['quoted-builder [] {:value "quoted"} :normal]
                                 ['let-body-lookup [payload] payload :normal] ['let-builder [payload] {:value payload} :normal]
                                 ['caught [payload] payload :normal] ['throw-builder [payload] payload :thrown] ['main [] "payload" :normal]]
                 results (into {} (map (fn [[name args expected status]]
                                        (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                                       [{:op :function-call :function name :args (literal-instructions args)} {} "" (:functions assembled) []])]
                                          (is (= [expected "" status] result)) [name (first result)])) specifications))
                 core (get-in input [:request :canonical-core-artifact]) facts (get-in input [:typed :identity-input])
                 effects (c8 'body-analyze-module (:request input) facts {} 65536)
                 lookups (filterv #(= :keyword-map-lookup (:core-form %)) (:nodes core))]
             (is (= 16 (count (:functions assembled))))
             (is (= 8 (count rows) (get-in assembled [:instruction-summary :keyword-map-lookup])))
             (is (= 6 (get-in assembled [:instruction-summary :map-literal])))
             (is (= 1 (get-in assembled [:instruction-summary :quote])))
             (is (= {:present 6 :absent 1 :no-normal-result 1} (frequencies (map #(get-in % [:solution :presence]) rows))))
             (is (= (get-in function-facts ['build :return]) (get-in function-facts ['other-build :return])))
             (is (= 1 (get-in function-facts ['build :fixed-arity]) (get-in function-facts ['other-build :fixed-arity])))
             (is (some #(= 2 (count (get-in % [:dependencies :operand-shape :call-links]))) rows))
             (is (every? #(if (= :no-normal-result (get-in % [:solution :presence])) true
                             (seq (get-in % [:dependencies :operand-shape :call-links]))) rows))
             (is (= standalone (get-in assembled [:functions 'lookup])))
             (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
             (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in standalone [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))
             (is (= {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))} (get-in standalone [:typed-lowering-provenance :source])))
             (doseq [name ['lookup 'let-body-lookup 'caught 'throw-builder]] (is (identical? payload (get results name))))
             (doseq [[name key] [['forward :value] ['build :value] ['other-build :other] ['let-builder :value]]]
               (is (identical? payload (get (get results name) key))))
             (is (= :accepted (:status effects)) (pr-str (select-keys effects [:reason :diagnostics])))
             (when (= :accepted (:status effects))
               (is (= 8 (count lookups)))
               (doseq [node lookups]
                 (is (= (:children node) (get-in effects [:graph :nodes (:node-id node) :evaluated-children])))
                 (is (= #{} (get-in effects [:graph :nodes (:node-id node) :direct]))))
               (let [never (first (filter #(= :gravity.type/never (get-in facts [:type-table (:node-id %)])) lookups))]
                 (is (= #{:gravity.type/string} (get-in effects [:graph :nodes (:node-id never) :escaping-thrown-errors]))))
               (is (= "C8-UNDECLARED" (:rule (c8 'body-allowance-check core (assoc-in facts [:module :effects] []) (:graph effects) 65536)))))
             {:input input :module module :definitions definitions :rows rows :function-facts function-facts}))
         [".gravity" ".qst"])
        {:keys [input module definitions rows function-facts]} (first cases)
        ledger (get-in input [:typed :constraint-ledger])
        row (first (filter #(seq (get-in % [:dependencies :operand-shape :call-links])) rows))
        row-index (first (keep-indexed #(when (= (:lhs row) (:lhs %2)) %1) ledger))
        constructor-id (get-in row [:dependencies :operand-shape :constructor-node-id])
        nodes (get-in input [:request :canonical-core-artifact :nodes])
        constructor (first (filter #(= constructor-id (:node-id %)) nodes))
        key-id (first (:children constructor))
        key-index (first (keep-indexed #(when (= key-id (:node-id %2)) %1) nodes))]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed [(assoc-in input [:typed :constraint-ledger row-index :dependencies :operand-shape :call-links] [])
                     (assoc-in input [:typed :constraint-ledger row-index :dependencies :operand-shape :call-links 0 :edge :callee-function-syntax-id]
                               (get-in function-facts ['other-build :function-id]))
                     (assoc-in input [:request :canonical-core-artifact :nodes key-index :attributes :value] :forged)]]
      (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'lookup) changed))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 7 (count @verified)))
    (let [products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-returned-map-bool-result.gravity")
          failure (first (get-in products [:typed :diagnostics]))]
      (is (= :rejected (get-in products [:typed :status])))
      (is (= ["C7-TYPE-MISMATCH" :function-result-type-mismatch] ((juxt :rule :reason) failure))))
    ;; This shared shape consumer is compiler-only. No match runtime is emitted.
    (let [products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/function-structural-match-call-return.gravity")
          typed (:typed products) match-rows (filterv #(= :sh08-structural-match-v1 (:producer-rule %)) (:constraint-ledger typed))]
      (is (= :accepted (:status typed)))
      (is (= :passed (get-in products [:verification :status])))
      (is (= 1 (count match-rows)))
      (is (= :gravity.type/i64 (:rhs (first match-rows))))
      (is (seq (get-in match-rows [0 :constructor-shape :call-links]))))
    (let [products (fresh "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/function-structural-match-return-shape.gravity")
          failure (first (get-in products [:typed :diagnostics]))
          core (get-in products [:request :canonical-core-artifact])
          pattern (first (filter #(and (zero? (:depth %)) (= 1 (:clause-ordinal %))) (:match-pattern-records core)))
          form (first (filter #(= (:pattern-form-id pattern) (:form-id %)) (get-in products [:request :b47-context :authenticated-core-request :forms]))) ]
      (is (= :rejected (get-in products [:typed :status])))
      (is (= ["L7-UNREACHABLE" :unreachable-structural-match-clause] ((juxt :rule :reason) failure)))
      (is (= [(:syntax-id form) (:source-span form) (:origin-chain form) (:generated-origin form)]
             ((juxt :syntax-id :source-span :origin-chain :generated-origin) failure))))))

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
                      #(assoc-in % [:ledger 0 :dependencies :quote :source :syntax-id] :swapped)
                      #(assoc-in % [:ledger 0 :solution :type] :gravity.type/string)
                      #(assoc-in % [:nodes :symbol-quote :children] [:reference])
                      #(assoc-in % [:forms :symbol-payload :parent-form-id] :sibling)
                      #(assoc-in % [:forms :quote-form :child-form-ids] 42)]]
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
          node {:node-id :unsupported :core-form :set! :source source :binding-context {:scope :original}}
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
             (get-in (invoke 'p15-s23-typed-compile-node (assoc-in context [:nodes :sequence :core-form] :set!) :sequence 65536) [:facts :reason]))))))

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
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        runtime-path (.getPath (io/file root "bootstrap/gravity/src/gravity/runtime.gravity"))
        compatibility-path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity"))
        compatibility-source {:path compatibility-path :sha256 (str "sha256:" (bootstrap/sha256-hex (slurp compatibility-path)))}
        runtime-text (slurp runtime-path) runtime-forms (bootstrap/read-forms runtime-path runtime-text)
        names '#{p15-s23-runtime-evaluate-arguments p15-s23-runtime-println-output p15-s23-runtime-evaluate-bindings
                 p15-s23-runtime-evaluate-arguments-in-context p15-s23-runtime-evaluate-bindings-in-context p15-s23-runtime-evaluate-sequence-in-context
                 p15-s23-runtime-select-handler-in-context p15-s23-runtime-evaluate-in-context p15-s23-runtime-call-target-valid? p15-s23-runtime-evaluate-call
                 p15-s23-runtime-function-valid? p15-s23-runtime-activate-function
                 p15-s23-runtime-evaluate-with-targets p15-s23-runtime-loop-valid? p15-s23-runtime-recur-valid? p15-s23-runtime-evaluate-loop
                 p15-s23-runtime-typed-handlers-valid? p15-s23-runtime-atomic-error-match? p15-s23-runtime-select-typed-handler
                 p15-s23-runtime-evaluate-sequence p15-s23-runtime-evaluate-closed-instruction p15-s23-runtime-execute-closed-plan
                 p15-s23-runtime-pattern-atom? p15-s23-runtime-structural-match-valid?
                 p15-s23-runtime-match-pattern p15-s23-runtime-evaluate-structural-match}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (names (second %))) runtime-forms)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        runtime-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter runtime-path runtime-text)
        execute (fn [instruction env]
                  (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan
                    'p15-s23-runtime-evaluate-closed-instruction [instruction env ""]))
        verifier 'sh08-verify-function-type-result original bootstrap/p15-s23-stage2-runtime-execute-function]
    (is (= 26 (count selected)))
    (is (= selected (vec (rest runtime-forms))))
    (is (= (str "sha256:" (bootstrap/sha256-hex runtime-text)) (get-in runtime-plan [:source :sha256])))
    (is (not= (:path compatibility-source) runtime-path))
    (is (= (:sha256 compatibility-source) (str "sha256:" (bootstrap/sha256-hex (slurp compatibility-path)))))
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
        path (.getPath (io/file root "bootstrap/gravity/src/gravity/runtime.gravity")) text (slurp path)
        compatibility-path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity"))
        forms (bootstrap/read-forms path text)
        arities {'p15-s23-runtime-evaluate-arguments 4 'p15-s23-runtime-println-output 2
                 'p15-s23-runtime-evaluate-arguments-in-context 7 'p15-s23-runtime-evaluate-bindings-in-context 6 'p15-s23-runtime-evaluate-sequence-in-context 6
                 'p15-s23-runtime-select-handler-in-context 6 'p15-s23-runtime-evaluate-in-context 5 'p15-s23-runtime-call-target-valid? 3 'p15-s23-runtime-evaluate-call 6
                 'p15-s23-runtime-function-valid? 2 'p15-s23-runtime-activate-function 5
                 'p15-s23-runtime-evaluate-with-targets 6 'p15-s23-runtime-loop-valid? 2 'p15-s23-runtime-recur-valid? 2 'p15-s23-runtime-evaluate-loop 6
                 'p15-s23-runtime-evaluate-bindings 3 'p15-s23-runtime-evaluate-sequence 3
                 'p15-s23-runtime-evaluate-closed-instruction 3 'p15-s23-runtime-execute-closed-plan 1
                 'p15-s23-runtime-typed-handlers-valid? 1 'p15-s23-runtime-atomic-error-match? 2 'p15-s23-runtime-select-typed-handler 3
                 'p15-s23-runtime-pattern-atom? 2 'p15-s23-runtime-structural-match-valid? 1
                 'p15-s23-runtime-match-pattern 3 'p15-s23-runtime-evaluate-structural-match 6}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? arities (second %))) forms)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path text)
        builtins (set (get-in (bootstrap/p15-s23-stage2-compiler-artifact-augmented-emitter emitter) [:call-rules :builtin-functions]))
        calls (keep #(when (and (seq? %) (symbol? (first %))) (first %)) (tree-seq coll? seq selected))]
    (is (= 26 (count selected) (count (set (map second selected)))))
    (is (= arities (into {} (map #(vector (second %) (count (nth % 2))) selected))))
    (is (= selected (vec (rest forms))))
    (is (every? #(or (contains? arities %) (contains? builtins %) (contains? '#{defn if let loop recur quote} %)) calls))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in plan [:source :sha256])))
    (is (= 'gravity.runtime (get-in plan [:module :module])))
    {:plan plan :implementation-source {:path path :sha256 (str "sha256:" (bootstrap/sha256-hex text))}
     :parent-source {:path compatibility-path :sha256 (str "sha256:" (bootstrap/sha256-hex (slurp compatibility-path)))}}))

(defn- typed-loop-kernel-data [module]
  ;; Source-linked normalized DATA. The test invokes the current C7 ledger
  ;; producer for these premises; none of this is an authentication receipt.
  (let [forms {:fn-form {:syntax-id "function-target" :scope-id :namespace}
               :loop-form {:syntax-id "loop-target" :kind :list :scope-id :function-scope :parent-form-id :fn-form :child-form-ids [:loop-op :binding-vector :branch-form]}
               :loop-op {:kind :symbol :value 'loop :parent-form-id :loop-form :child-form-ids []}
               :binding-vector {:kind :vector :scope-id nil :parent-form-id :loop-form :child-form-ids [:again-name :again-init-form :value-name :value-init-form]}
               :again-name {:kind :symbol :value 'again :scope-id :again-scope :parent-form-id :binding-vector :child-form-ids []}
               :value-name {:kind :symbol :value 'value :scope-id :value-scope :parent-form-id :binding-vector :child-form-ids []}
               :again-init-form {:kind :boolean :value true :scope-id :function-scope :parent-form-id :binding-vector}
               :value-init-form {:kind :integer :value 7 :scope-id :again-scope :parent-form-id :binding-vector}
               :branch-form {:kind :list :scope-id :value-scope :parent-form-id :loop-form :child-form-ids [:if-op :condition-form :transfer-form :result-form]}
               :condition-form {:kind :symbol :value 'again :scope-id :value-scope :parent-form-id :branch-form}
               :transfer-form {:kind :list :scope-id :value-scope :parent-form-id :branch-form :child-form-ids [:recur-op :next-again-form :next-value-form]}
               :recur-op {:kind :symbol :value 'recur :scope-id :value-scope :parent-form-id :transfer-form :child-form-ids []}
               :next-again-form {:kind :boolean :value false :scope-id :value-scope :parent-form-id :transfer-form}
               :next-value-form {:kind :symbol :value 'value :scope-id :value-scope :parent-form-id :transfer-form}
               :result-form {:kind :symbol :value 'value :scope-id :value-scope :parent-form-id :branch-form}}
        forms (into {} (map (fn [[id form]] [id (merge {:form-id id :syntax-id id :origin-chain [] :generated-origin []} form)]) forms))
        source (fn [id] (let [form (get forms id)] {:form-id id :syntax-id (:syntax-id form) :semantic-span (:source-span form)
                                                   :origin-chain (:origin-chain form) :generated-origin (:generated-origin form)}))
        node (fn [id form children form-id attrs] {:node-id id :core-form form :children children :source (source form-id)
                                                  :binding-context (get-in forms [form-id :scope-id]) :attributes attrs :resolved-binding-ids []})
        ordered (fn [node kind] (assoc node :evaluation {:kind kind :owner-function-syntax-id "function-target"
                                                        :order (mapv (fn [i id] {:index i :core-node-id id}) (range) (:children node))}))
        loop-attrs {:target-id "loop-target" :binding-vector-form-id :binding-vector :binding-vector-syntax-id :binding-vector
                    :binding-count 2 :body-count 1 :initializer-child-indexes [0 1] :body-child-indexes [2]
                    :evaluation-order :initializers-then-body-left-to-right :result-policy {:kind :last-body :child-index 2}}
        nodes {:fn-node (node :fn-node :fn [:loop-node] :fn-form {:recur-target-id "function-target"})
               :again-init (node :again-init :literal [] :again-init-form {:literal-kind :boolean :value true})
               :value-init (node :value-init :literal [] :value-init-form {:literal-kind :integer :value 7})
               :condition (node :condition :reference [] :condition-form {:binding-id :again})
               :next-again (node :next-again :literal [] :next-again-form {:literal-kind :boolean :value false})
               :next-value (node :next-value :reference [] :next-value-form {:binding-id :value})
               :result (node :result :reference [] :result-form {:binding-id :value})
               :branch (node :branch :if [:condition :transfer :result] :branch-form {})
               :transfer (assoc (ordered (node :transfer :recur [:next-again :next-value] :transfer-form
                                          {:target-id "loop-target" :target-kind :loop :arity 2 :semantic-rule "L2-RECUR-TARGET" :type-compatibility :pending-sh08})
                                        :arguments-left-to-right-then-transfer) :resolved-binding-ids [:again :value])
               :loop-node (assoc (ordered (node :loop-node :loop [:again-init :value-init :branch] :loop-form loop-attrs)
                                         :initializers-then-body-left-to-right) :resolved-binding-ids [:again :value])}
        parent {:target-id "function-target" :target-kind :function :owner-core-node-id :fn-node :owner-form-id :fn-form
                :owner-syntax-id "function-target" :arity 0 :binding-ids [] :body-scope-id :function-scope :parent-target-id nil :type-compatibility :pending-sh08}
        target {:target-id "loop-target" :target-kind :loop :owner-core-node-id :loop-node :owner-form-id :loop-form
                :owner-syntax-id "loop-target" :arity 2 :binding-ids [:again :value] :body-scope-id :value-scope :parent-target-id "function-target" :type-compatibility :pending-sh08}
        records (mapv (fn [id ordinal init prior scope init-scope]
                        {:loop-core-node-id :loop-node :ordinal ordinal :name (symbol (name id)) :binding-id id
                         :definition-form-id (keyword (str (name id) "-name")) :definition-syntax-id (keyword (str (name id) "-name")) :binding-scope-id scope
                         :initializer-form-id (keyword (str (name id) "-init-form")) :initializer-syntax-id (keyword (str (name id) "-init-form"))
                         :initializer-scope-id init-scope :initializer-node-id init :visible-prior-binding-ids prior :mutability :immutable})
                      [:again :value] [0 1] [:again-init :value-init] [[] [:again]] [:again-scope :value-scope] [:function-scope :again-scope])
        transfer {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer-form :target-id "loop-target" :target-kind :loop
                  :argument-node-ids [:next-again :next-value] :arity 2 :tail-position true :evaluation-order :arguments-left-to-right
                  :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        types {:again-init :gravity.type/bool :value-init :gravity.type/integer :condition :gravity.type/bool :next-again :gravity.type/bool
               :next-value :gravity.type/integer :result :gravity.type/integer :transfer :gravity.type/never :branch :gravity.type/integer :loop-node :gravity.type/integer}
        premises (conj (mapv (fn [record type] {:kind :loop-slot-type :binding-id (:binding-id record) :initializer-node-id (:initializer-node-id record)
                                               :type type :source-loop-binding record :source-target target :status :solved}) records [:gravity.type/bool :gravity.type/integer])
                        {:kind :loop-recur-type :source-transfer transfer :source-target target :argument-types [:gravity.type/bool :gravity.type/integer]
                         :slot-types [:gravity.type/bool :gravity.type/integer] :result-type :gravity.type/never :normal-transfer? true :status :solved})]
    {:premises premises
     :context {:core {:module module} :nodes nodes :forms forms :types types :loop-bindings records
               :loop-binding-index (into {} (map (juxt :binding-id identity) records)) :targets {"function-target" parent "loop-target" target}
               :target-owners {:fn-node parent :loop-node target} :transfers {:transfer transfer}
               :bindings (into {} (map (fn [record] [(:binding-id record) {:binding-id (:binding-id record) :name (:name record) :kind :local
                                                                        :binding-class :lexical :scope-id (:binding-scope-id record) :definition-syntax-id (:definition-syntax-id record)}]) records))
               :current-function {:function-core-node-id :fn-node :body-core-node-id :loop-node :fixed-arity 0 :parameter-binding-ids []}
               :nearest-target parent :tail? true :locals {} :parameter-types {}}}))

(deftest current-gravity-guard-local-loop-parent-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [f & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan f (vec args)))
        c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        module {:namespace 'example.guard-loop :profile :meta :target :jvm :effects [] :capabilities []}
        {:keys [context premises]} (typed-loop-kernel-data module)
        region {:kind :match-guard :match-syntax-id "guard-match" :clause-ordinal 0}
        source {:rule-version :sh07-root-map-guard-v1 :source-child-index 3 :form-id :loop-form :syntax-id "loop-target"}
        guard (assoc source :node-id :loop-node :child-index 2 :conditional-region region
                            :evaluation :once-after-structural-success :on-pattern-failure :next-candidate
                            :on-true :branch-body :on-false :next-candidate :non-boolean-type-effect-legality :pending-later-legality)
        match-node {:node-id :guard-match :core-form :match :children [:scrutinee :body :loop-node]
                    :source {:syntax-id "guard-match"} :binding-context :function-scope}
        context (-> context
                    (assoc-in [:nodes :fn-node :children] [:guard-match])
                    (assoc-in [:current-function :body-core-node-id] :guard-match)
                    (assoc-in [:nodes :guard-match] match-node)
                    (assoc-in [:nodes :loop-node :evaluation :region] region)
                    (assoc-in [:forms :loop-form :parent-form-id] :pattern-form)
                    (assoc-in [:nodes :value-init :attributes] {:literal-kind :boolean :value true})
                    (assoc-in [:forms :value-init-form :kind] :boolean)
                    (assoc-in [:forms :value-init-form :value] true)
                    (update :types merge (zipmap [:value-init :next-value :result :branch :loop-node] (repeat :gravity.type/bool))))
        premises (mapv (fn [premise]
                         (if (= :loop-slot-type (:kind premise))
                           (assoc premise :type :gravity.type/bool)
                           (assoc premise :argument-types [:gravity.type/bool :gravity.type/bool]
                                          :slot-types [:gravity.type/bool :gravity.type/bool]))) premises)
        rows (c7 'sh08-ft-loop-constraints [premises (:nodes context) module])
        context (assoc context :loop-rows (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows)))
        branch {:clause-ordinal 0 :guard guard :pattern-binding-ids []}
        evidence (:guard (c7 'sh08-gd-result [{:nodes (:nodes context) :node-sequence (vec (vals (:nodes context)))} branch (:types context) true]))
        row (c7 'sh08-gd-constraint [{:guard evidence :selected [:case] :structural-selected [:case] :remaining-after [:case]}
                                    {:rhs :gravity.type/bool :matched-shapes [:case] :remaining-after [:case]
                                     :constraint-id-request {:pattern-tree {:record {:guard source}}}
                                     :dependencies {} :solution {} :invalidation []}])
        compiled (invoke 'p15-s23-typed-structural-guard context match-node branch row 65536)]
    ;; C6 keeps the function on its lexical target stack while clearing tail
    ;; eligibility. The guard's own loop authenticates that nonnil parent.
    (is (= "function-target" (get-in context [:targets "loop-target" :parent-target-id])))
    (is (= :complete (:status compiled)) (pr-str compiled))
    (is (= :loop (get-in compiled [:value :op])))
    (is (= "loop-target" (get-in compiled [:value :body 0 :then :target])))
    (when (= :complete (:status compiled))
      (is (= compiled (invoke 'p15-s23-typed-structural-guard context match-node branch row (:work-count compiled))))
      (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-structural-guard context match-node branch row (dec (:work-count compiled))) [:facts :reason]))))
    (is (= :rejected (:status (invoke 'p15-s23-typed-structural-guard (assoc context :nearest-target nil) match-node branch row 65536))))
    (is (= :rejected (:status (invoke 'p15-s23-typed-structural-guard
                                    (assoc-in context [:targets "loop-target" :parent-target-id] nil) match-node branch row 65536))))
    (let [body-context (assoc context :nearest-target (get-in context [:targets "loop-target"]) :tail? false
                                     :locals {:again 'again :value 'value} :parameter-types {:again :gravity.type/bool :value :gravity.type/bool})]
      (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node body-context :transfer 65536)))))
    (when (= :complete (:status compiled))
      (let [runtime-plan (:plan (typed-catch-runtime-projection))
            instruction {:op :structural-match :scrutinee {:op :literal :value {}}
                         :clauses [{:pattern {:kind :wildcard} :guard (:value compiled) :body {:op :literal :value :selected}}]}
            result (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction [instruction {} ""])]
        (is (= [:selected "" :normal] result))))))

(deftest current-gravity-guard-recur-diagnostic-envelope-development-kernel
  (let [plan (:plan (bootstrap/sh07-core-current-binding!))
        invoke #(bootstrap/p15-s23-stage2-runtime-execute-function
                 {:engine :gravity-guard-recur-envelope-development} plan %1 %2)
        request {:module {:namespace 'example.guard :profile :meta :target :jvm}
                 :provenance {:actual-source-path "normalized-guard-recur.gravity"}}
        form {:form-id :guard-recur :syntax-id :guard-recur :kind :list :value '(recur value)
              :child-form-ids [:recur-operator :value] :origin-chain [] :generated-origin []}
        state (assoc (invoke 'sh07-empty-state []) :tail-target-id nil
                     :recur-target-stack [{:target-id "outer" :target-kind :function :arity 1 :binding-ids [:value]}])
        rejected (invoke 'sh07-lower-recur [request state form])
        template (invoke 'sh07-rejected-result [request (:rule rejected) (:form rejected) (:facts rejected)])
        failure (diagnostic #((module-private 'sh07-core-raise-diagnostic!) "normalized-guard-recur.gravity" template))]
    (is (= :rejected (:status rejected)))
    (is (= :recur-tail-position-required (get-in rejected [:facts :reason])))
    (is (= :gravity/sh07-core-diagnostic (:artifact failure)))
    (is (= "C6-VERIFY" (:rule failure)))
    (is (nil? (:id failure)))
    (is (= :recur-tail-position-required (get-in failure [:facts :reason])))
    (is (= (:diagnostics template) [(assoc failure :diagnostic-id-request (get-in template [:diagnostics 0 :diagnostic-id-request]))]))))

(defn- typed-function-recur-kernel-data [throw?]
  (let [{:keys [module input observation]} (typed-lowering-kernel-data)
        parameters '[flag :- Bool value :- String]
        next-value (if throw? '(throw value) 'value)
        body (list 'if 'flag (list 'recur false next-value) 'value)
        fn-value (list 'fn parameters :- 'String body) def-value (list 'def 'identity fn-value)
        declaration (fn [id binding type name] {:type-binding-id binding :type-id type :type-symbol name :type-form-id id :type-syntax-id id})
        signature {:body-source-child-index 4 :parameter-source-child-indexes [0 3]
                   :parameters [(declaration :flag-type :bool :gravity.type/bool 'Bool) (declaration :value-type :string :gravity.type/string 'String)]
                   :return (declaration :return-type :string :gravity.type/string 'String)}
        form (fn [id kind value parent children scope]
               {:form-id id :syntax-id id :kind kind :value value :parent-form-id parent :child-form-ids children
                :scope-id scope :source-span {:byte-start 10 :byte-end 20} :origin-chain [] :generated-origin []})
        forms [(form :definition-form :list def-value nil [:def-op :name-form :function-form] :namespace)
               (form :function-form :list fn-value :definition-form [:fn-op :parameter-form :return-marker :return-type :branch-form] :namespace)
               (form :parameter-form :vector parameters :function-form [:flag-name :flag-marker :flag-type :value-name :value-marker :value-type] nil)
               (form :flag-name :symbol 'flag :parameter-form [] :function-scope)
               (form :value-name :symbol 'value :parameter-form [] :function-scope)
               (form :flag-type :symbol 'Bool :parameter-form [] :namespace)
               (form :value-type :symbol 'String :parameter-form [] :namespace)
               (form :return-type :symbol 'String :function-form [] :namespace)
               (form :branch-form :list body :function-form [:if-op :condition-form :transfer-form :result-form] :function-scope)
               (form :condition-form :symbol 'flag :branch-form [] :function-scope)
               (form :transfer-form :list (list 'recur false next-value) :branch-form [:recur-op :next-flag-form :next-value-form] :function-scope)
               (form :recur-op :symbol 'recur :transfer-form [] :function-scope)
               (form :next-flag-form :boolean false :transfer-form [] :function-scope)
               (form :next-value-form (if throw? :list :symbol) next-value :transfer-form (if throw? [:throw-op :payload-form] []) :function-scope)
               (form :result-form :symbol 'value :branch-form [] :function-scope)]
        forms (cond-> forms throw? (into [(form :throw-op :symbol 'throw :next-value-form [] :function-scope)
                                         (form :payload-form :symbol 'value :next-value-form [] :function-scope)]))
        index (into {} (map (juxt :form-id identity) forms))
        source (fn [id] (let [f (get index id)] {:form-id id :syntax-id (:syntax-id f) :semantic-span (:source-span f)
                                               :origin-chain [] :generated-origin []}))
        node (fn [id kind children f attrs resolved eval-kind]
               {:node-id id :core-form kind :children children :source (source f) :binding-context (get-in index [f :scope-id])
                :attributes attrs :resolved-binding-ids resolved
                :evaluation {:kind eval-kind :owner-function-syntax-id :function-form
                             :order (mapv (fn [i child] {:index i :core-node-id child}) (range) (if (= :if kind) [(first children)] (if (= :fn kind) [] children)))}})
        nodes [(node :condition :reference [] :condition-form {:binding-id :flag} [:flag] :value)
               (node :next-flag :literal [] :next-flag-form {:literal-kind :boolean :value false} [] :value)]
        nodes (cond-> nodes throw? (conj (node :payload :reference [] :payload-form {:binding-id :value} [:value] :value)))
        nodes (into nodes [(node :next-value (if throw? :throw :reference) (if throw? [:payload] []) :next-value-form (if throw? {} {:binding-id :value}) (if throw? [] [:value]) (if throw? :value-then-transfer :value))
                           (node :transfer :recur [:next-flag :next-value] :transfer-form
                             {:target-id "function-target" :target-kind :function :arity 2 :semantic-rule "L2-RECUR-TARGET" :type-compatibility :pending-sh08}
                             [:flag :value] :arguments-left-to-right-then-transfer)
                           (node :result :reference [] :result-form {:binding-id :value} [:value] :value)
                           (node :branch :if [:condition :transfer :result] :branch-form {} [] :conditional-branch)
                           (node :function :fn [:branch] :function-form {:parameters '[flag value] :signature signature :recur-target-id "function-target"} [:flag :value] :function-creation)
                           (node :definition :def [:function] :definition-form {} [] :value)])
        bindings [{:binding-id :flag :name 'flag :kind :local :binding-class :lexical :definition-syntax-id :flag-name :scope-id :function-scope}
                  {:binding-id :value :name 'value :kind :local :binding-class :lexical :definition-syntax-id :value-name :scope-id :function-scope}
                  {:binding-id :bool :name 'Bool :kind :type :binding-class :core :namespace 'gravity.core}
                  {:binding-id :string :name 'String :kind :type :binding-class :core :namespace 'gravity.core}]
        record (assoc (first (get-in input [:request :canonical-core-artifact :function-records]))
                 :function-syntax-id :function-form :body-core-node-id :branch :parameter-binding-ids [:flag :value] :fixed-arity 2 :signature signature)
        function (assoc (first (get-in input [:typed :function-type-table])) :function-id :function-form
                   :body-core-node-id :branch :parameter-binding-ids [:flag :value] :fixed-arity 2 :signature signature
                   :thrown-error-effects (if throw? [:gravity.type/string] [])
                   :parameters [{:binding-id :flag :type :gravity.type/bool} {:binding-id :value :type :gravity.type/string}])
        target {:target-id "function-target" :target-kind :function :owner-core-node-id :function :owner-form-id :function-form :owner-syntax-id :function-form
                :arity 2 :binding-ids [:flag :value] :body-scope-id :function-scope :parent-target-id nil :type-compatibility :pending-sh08}
        transfer {:recur-core-node-id :transfer :recur-form-id :transfer-form :recur-syntax-id :transfer-form
                  :target-id "function-target" :target-kind :function :argument-node-ids [:next-flag :next-value] :arity 2
                  :tail-position true :evaluation-order :arguments-left-to-right :transfer-policy :nearest-lexical-recur-target :type-compatibility :pending-sh08}
        core (assoc (get-in input [:request :canonical-core-artifact]) :nodes nodes :function-records [record]
                    :binding-table bindings :loop-bindings [] :recur-targets [target] :recur-transfers [transfer])
        types (cond-> {:condition :gravity.type/bool :next-flag :gravity.type/bool :next-value (if throw? :gravity.type/never :gravity.type/string)
                       :transfer :gravity.type/never :result :gravity.type/string :branch :gravity.type/string :function function :definition function}
                throw? (assoc :payload :gravity.type/string))
        typed (assoc (:typed input) :function-type-table [function] :type-table types)
        report {:artifact :gravity/sh08-function-type-verification :status :passed :expected typed :candidate typed}
        text (str "(ns example.typed (:profile :meta) (:target :jvm) (:effects #{}) (:capabilities #{}) (:safety :safe))\n" (pr-str def-value))
        hash (str "sha256:" (bootstrap/sha256-hex text)) module (assoc module :forms [def-value])
        table (bootstrap/stage0-function-table module) module (assoc module :function-table table)
        input (-> input (assoc :typed typed :verification report)
                  (assoc-in [:request :canonical-core-artifact] core)
                  (assoc-in [:request :b47-context :authenticated-core-request :forms] forms)
                  (assoc-in [:request :b47-context :authenticated-core-request :binding-table] bindings)
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :expanded-forms] [def-value])
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
                             :authenticated-sh04-artifact :c2-reader-artifact :source-unit-record :bytes-hash] hash)
                  (assoc-in [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
                             :authenticated-sh04-artifact :c2-reader-artifact :token-stream] [{:raw text}]))
        observation (-> observation (assoc :verification report) (assoc-in [:source :text] text) (assoc-in [:source :sha256] hash))]
    {:module module :input input :observation observation :definition (get table 'identity) :target target :transfer transfer}))

(deftest current-gravity-function-recur-lowering-development-kernel
  ;; Actual C7 target/slot/transfer validation and ledger production feed the
  ;; current Gravity emitter/runtime. Normalized DATA is not authentication.
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [c7 (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test 'invoke-c7)
        path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        runtime-plan (:plan (typed-catch-runtime-projection))
        prepare (fn [throw?]
                  (let [{:keys [input observation] :as data} (typed-function-recur-kernel-data throw?)
                        core (get-in input [:request :canonical-core-artifact]) types (get-in input [:typed :type-table])
                        checked (c7 'sh08-ft-loop-type-step [core types {} 65536])
                        rows (c7 'sh08-ft-loop-constraints [(:constraints checked) (into {} (map (juxt :node-id identity) (:nodes core))) (:module core)])
                        typed (assoc (:typed input) :constraint-ledger rows)
                        report {:artifact :gravity/sh08-function-type-verification :status :passed :expected typed :candidate typed}]
                    (is (= :accepted (:status checked)) (pr-str (:issues checked)))
                    (is (= [:gravity.type/bool :gravity.type/string] (:slot-types (last (:constraints checked)))))
                    (is (= (not throw?) (:normal-transfer? (last (:constraints checked)))))
                    (assoc data :checked checked :input (assoc input :typed typed :verification report) :observation (assoc observation :verification report))))
        {:keys [module input observation definition target]} (prepare false)
        core (get-in input [:request :canonical-core-artifact])
        checked (invoke 'p15-s23-typed-context module input observation) context (:value checked)
        joined (invoke 'p15-s23-typed-definition context 'identity)
        active (assoc context :current-function (get-in joined [:value :record]) :locals (get-in joined [:value :locals])
                             :parameter-types (get-in joined [:value :parameter-types]) :nearest-target target :tail? true)
        compile #(invoke 'p15-s23-typed-compile-node % :transfer 65536)
        result (invoke 'p15-s23-typed-compile-function emitter module definition input observation)
        compiled (:value result) payload (String. "done")]
    (let [form (first (filter #(and (seq? %) (= 'defn (first %)) (= 'p15-s23-typed-function-slots (second %))) (bootstrap/read-forms path (slurp path))))
          calls (set (keep #(when (and (seq? %) (symbol? (first %))) (first %)) (tree-seq coll? seq form)))]
      (is (not (contains? calls 'p15-s23-typed-definition))
          "per-transfer slot checks must not repeat the complete source-body/header join"))
    (is (= :complete (:status checked) (:status joined) (:status result)))
    (is (= "function-target" (:recur-target compiled)))
    (is (= {:op :recur :target "function-target" :args [{:op :literal :value false} {:op :local :name 'value}]}
           (:value (compile active))))
    (let [result (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-activate-function
                   [compiled [true payload] "" {'identity compiled} ['identity]])]
      (is (= [payload "" :normal] result)) (is (identical? payload (first result))))
    (doseq [change [#(assoc % :tail? false) #(assoc % :nearest-target nil)
                    #(assoc-in % [:targets "function-target" :owner-core-node-id] :wrong)
                    #(assoc-in % [:targets "function-target" :body-scope-id] :wrong)
                    #(assoc-in % [:targets "function-target" :parent-target-id] "outer")
                    #(assoc-in % [:targets "function-target" :binding-ids] [:value :flag])
                    #(assoc-in % [:transfers :transfer :target-kind] :loop)
                    #(assoc-in % [:transfers :transfer :target-id] "foreign")
                    #(assoc-in % [:transfers :transfer :argument-node-ids] [:next-value :next-flag])
                    #(assoc-in % [:transfers :transfer :tail-position] false)
                    #(assoc-in % [:nodes :transfer :attributes :target-kind] :loop)
                    #(assoc-in % [:forms :next-flag-form :parent-form-id] :sibling)
                    #(assoc-in % [:nodes :next-value :source :syntax-id] :foreign)
                    #(assoc-in % [:nodes :transfer :evaluation :order] [{:index 0 :core-node-id :next-value} {:index 1 :core-node-id :next-flag}])
                    #(assoc-in % [:parameter-types :flag] :gravity.type/string)
                    #(assoc-in % [:functions 'identity :parameters 0 :binding-id] :value)
                    #(update-in % [:records 'identity :signature] dissoc :parameters)
                    #(assoc-in % [:loop-rows [:loop-recur-type :transfer] :solution :type] :gravity.type/string)
                    #(assoc % :loop-rows {})]]
      (is (= :rejected (:status (compile (change active))))))
    (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node active :transfer 1))))
    (let [cost (:work-count (compile active))]
      (is (= :complete (:status (invoke 'p15-s23-typed-compile-node active :transfer cost))))
      (is (= :typed-lowering-work-limit
             (get-in (invoke 'p15-s23-typed-compile-node active :transfer (dec cost)) [:facts :reason]))))
    (doseq [bad [(assoc-in core [:recur-transfers 0 :target-id] "foreign")
                 (assoc-in core [:recur-transfers 0 :tail-position] false)
                 (assoc-in core [:recur-transfers 0 :argument-node-ids] [:next-value :next-flag])]]
      (is (= :rejected (:status (c7 'sh08-ft-loop-type-step [bad (get-in input [:typed :type-table]) {} 65536])))))
    (is (= "C7-TYPE-MISMATCH"
           (get-in (c7 'sh08-ft-loop-type-step [core (assoc (get-in input [:typed :type-table]) :next-flag :gravity.type/string) {} 65536]) [:issues 0 :rule])))
    (is (= :rejected (:status (invoke 'p15-s23-typed-context module (update-in input [:typed :constraint-ledger] #(into % %)) observation))))
    (let [{:keys [module input observation definition]} (prepare true)
          result (invoke 'p15-s23-typed-compile-function emitter module definition input observation)
          execution (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-activate-function
                      [(:value result) [true payload] "" {} ['identity]])]
      (is (= :complete (:status result)))
      (is (= [payload "" :thrown] execution))
      (is (identical? payload (first execution))))))

(deftest current-gravity-loop-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        c7-path (.getPath (io/file (.getParentFile (.getParentFile (io/file path))) "src/gravity/compiler/c7_type_checker_engine.gravity"))
        c7-plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter c7-path (slurp c7-path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        module {:namespace 'example.loop :profile :meta :target :jvm :effects [] :capabilities []}
        {:keys [context premises]} (typed-loop-kernel-data module)
        produce (fn [context premises] (bootstrap/p15-s23-stage2-runtime-execute-function runtime c7-plan 'sh08-ft-loop-constraints [premises (:nodes context) module]))
        row-index (fn [rows] (into {} (map (fn [row] [[(get-in row [:dependencies :loop-premise :kind]) (:lhs row)] row]) rows)))
        rows (produce context premises) context (assoc context :loop-rows (row-index rows))
        compile #(invoke 'p15-s23-typed-compile-node % :loop-node 65536)
        result (compile context) runtime-plan (:plan (typed-catch-runtime-projection))]
    (is (= 3 (count rows)))
    (is (= :complete (:status result)) (pr-str result))
    (is (= {:op :loop :target "loop-target" :bindings [{:name 'again :expr {:op :literal :value true}} {:name 'value :expr {:op :literal :value 7}}]
            :body [{:op :if :test {:op :local :name 'again} :then {:op :recur :target "loop-target" :args [{:op :literal :value false} {:op :local :name 'value}]}
                    :else {:op :local :name 'value}}]} (:value result)))
    (is (= [7 "" :normal] (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction [(:value result) {} ""])))
    (is (= {:loop 1 :literal 3 :if 1 :local 3 :recur 1} (:value (invoke 'p15-s23-summary-instruction (:value result) {}))))
    (doseq [mutate [#(assoc-in % [:nodes :loop-node :children] 42) #(assoc-in % [:forms :binding-vector :child-form-ids] 42)
                    #(assoc-in % [:targets "loop-target" :parent-target-id] nil) #(assoc-in % [:nearest-target :owner-core-node-id] :wrong)
                    #(assoc-in % [:targets "loop-target" :binding-ids] [:value :again]) #(update % :loop-bindings pop)
                    #(assoc-in % [:loop-bindings 1 :visible-prior-binding-ids] []) #(assoc-in % [:bindings :value :scope-id] :wrong)
                    #(assoc-in % [:nodes :value-init :binding-context] :wrong) #(assoc-in % [:forms :branch-form :scope-id] :wrong)
                    #(assoc-in % [:nodes :transfer :attributes :target-id] "function-target")
                    #(assoc-in % [:transfers :transfer :tail-position] false)
                    #(assoc-in % [:transfers :transfer :argument-node-ids] [:next-value :next-again])
                    #(assoc-in % [:types :next-value] :gravity.type/i64)
                    #(assoc-in % [:loop-rows [:loop-slot-type :value] :dependencies :loop-premise :type] :gravity.type/i64)
                    #(assoc-in % [:loop-rows [:loop-recur-type :transfer] :solution :normal-transfer?] false)
                    #(update % :loop-rows dissoc [:loop-slot-type :value]) #(update % :loop-rows dissoc [:loop-recur-type :transfer])]]
      (let [rejected (compile (mutate context))] (is (= :rejected (:status rejected))) (is (= "P15S23Q002" (:diagnostic rejected)))))
    (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :loop-node 1) [:facts :reason])))
    (is (= (:loop-rows context) (invoke 'p15-s23-typed-loop-row-index rows)))
    (is (nil? (invoke 'p15-s23-typed-loop-row-index (conj rows (first rows)))))
    (let [body-context (assoc context :nearest-target (get-in context [:targets "loop-target"]) :locals {:again 'again :value 'value}
                                      :parameter-types {:again :gravity.type/bool :value :gravity.type/integer})]
      (is (= :rejected (:status (invoke 'p15-s23-typed-compile-node (assoc body-context :tail? false) :transfer 65536)))))
    (let [payload (String. "problem") source {:form-id :payload-form :syntax-id :payload-form :semantic-span nil :origin-chain [] :generated-origin []}
          changed (-> context (assoc-in [:nodes :next-again :core-form] :throw) (assoc-in [:nodes :next-again :children] [:payload])
                      (assoc-in [:nodes :next-again :attributes] {})
                      (assoc-in [:nodes :payload] {:node-id :payload :core-form :literal :children [] :binding-context :value-scope :source source
                                                  :attributes {:literal-kind :string :value payload}})
                      (assoc-in [:forms :payload-form] {:form-id :payload-form :syntax-id :payload-form :scope-id :value-scope :value payload :origin-chain [] :generated-origin []})
                      (assoc-in [:types :payload] :gravity.type/string) (assoc-in [:types :next-again] :gravity.type/never))
          changed-premises (-> premises (assoc-in [2 :argument-types] [:gravity.type/never :gravity.type/integer]) (assoc-in [2 :normal-transfer?] false))
          changed (assoc changed :loop-rows (row-index (produce changed changed-premises)))
          result (compile changed)
          instruction {:op :try :protected (:value result) :handlers [{:type :gravity.type/string :name 'error :body {:op :local :name 'error}}]}
          observed (bootstrap/p15-s23-stage2-runtime-execute-function runtime runtime-plan 'p15-s23-runtime-evaluate-closed-instruction [instruction {} ""])]
      (is (= :complete (:status result)) (pr-str result))
      (is (false? (get-in changed [:loop-rows [:loop-recur-type :transfer] :solution :normal-transfer?])))
      (is (identical? payload (first observed))) (is (= ["" :normal] (subvec observed 1))))
    (let [decimal {:kind :gravity/decimal-occurrence :source "1.25"}
          changed (-> context (assoc-in [:nodes :value-init :attributes] {:literal-kind :decimal :value decimal})
                      (assoc-in [:forms :value-init-form :value] decimal)
                      (update :types merge (zipmap [:value-init :next-value :result :branch :loop-node] (repeat :gravity.type/decimal))))
          changed-premises (-> premises (assoc-in [1 :type] :gravity.type/decimal)
                               (assoc-in [2 :argument-types] [:gravity.type/bool :gravity.type/decimal])
                               (assoc-in [2 :slot-types] [:gravity.type/bool :gravity.type/decimal]))
          changed (assoc changed :loop-rows (row-index (produce changed changed-premises))) result (compile changed)]
      (is (= :rejected (:status result)))
      (is (= :unelaborated-typed-numeric-literal (get-in result [:facts :reason]))))))

(deftest current-gravity-function-recur-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        {:keys [plan parent-source]} (typed-catch-runtime-projection)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        original bootstrap/p15-s23-stage2-runtime-execute-function verifications (atom 0)
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) transports (atom 0) original-transport (var-get transport)
        operation (fn [thunk]
                    (let [before @verifications
                          result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                               (fn [runtime plan function args]
                                                 (when (= 'sh08-verify-function-type-result function) (swap! verifications inc))
                                                 (original runtime plan function args))] (thunk))]
                      (is (= 1 (- @verifications before))) result))
        execute (fn [function arguments functions name]
                  (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                    #(original runtime plan 'p15-s23-runtime-activate-function [function arguments "" functions [name]])))
        inventory [["accepted/function-recur-types" "c9068acec85998d8dcba1b22e165b66ca9e73f5fae04f17229611ed6ad1d63f9" :again]
                   ["accepted/local-runtime-function-recur" "68357ca3d5b6983b06494dc8d56255b24550ae93628639da989fad40d2996e07" :main]
                   ["rejected/function-recur-slot-type" "e2fddda03f292efcef69bbade18a802260194514c015c00054203f76247b6d7e" :rejected]]]
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp (:path parent-source)))) (:sha256 parent-source)))
    ;; Construct each of the six genuine C5/C6/C7 inputs exactly once. Reuse
    ;; its immutable products for public operations; no historical proof replay.
    (doseq [extension [".gravity" ".qst"] [name hash kind] inventory]
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" name extension)
            source-path (.getPath (io/file root relative)) _ (is (= hash (bootstrap/sha256-hex (slurp source-path))))
            p (fresh relative) input (select-keys p [:request :typed :verification]) typed (:typed input)
            core (get-in input [:request :canonical-core-artifact])
            macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
            reader (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
            module (assoc (bootstrap/parse-module source-path (:parsed-semantic-values reader)) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)]
        (is (= :passed (get-in input [:verification :status])))
        (if (= kind :rejected)
          (do (is (= :rejected (:status typed)))
              (is (= ["C7-TYPE-MISMATCH" :function-recur-slot-type-mismatch]
                     ((juxt :rule :reason) (first (:diagnostics typed)))))
              (is (= [:gravity.type/string :gravity.type/bool]
                     ((juxt :expected-type :actual-type) (first (:diagnostics typed)))))
              (is (= "P15S23Q002" (:id (operation #(diagnostic (fn []
                        (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'again) input))))))))
          (do
            (is (= :accepted (:status typed)) (pr-str (map #(select-keys % [:rule :reason :expected-type :actual-type]) (:diagnostics typed))))
            (when-not (= :accepted (:status typed)) (throw (ex-info "Function recur input was not accepted by current C7" {:source-path source-path :status (:status typed)})))
            (let [assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                  functions (:functions assembled)
                  targets (into {} (map (juxt :owner-core-node-id identity) (filter #(= :function (:target-kind %)) (:recur-targets core))))
                  representative (if (= kind :again) 'again 'swap)
                  standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions representative) input))]
              (is (= (count (:recur-transfers core)) (get-in assembled [:instruction-summary :recur])))
              (is (= standalone (get functions representative)))
              (doseq [record (:function-records core)]
                (let [function-name (:definition-name record)
                      compiled (get functions function-name)
                      target (get targets (:function-core-node-id record))]
                  (is (= (:target-id target) (:recur-target compiled)))
                  (is (= (:parameter-binding-ids record) (:binding-ids target)))
                  (is (= (:fixed-arity record) (:arity target) (:arity compiled) (count (:params compiled))))
                  (is (= :pending-c8-and-profile-authority (get-in compiled [:typed-lowering-provenance :effect-legality])))
                  (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))))
              (if (= kind :again)
                (let [payload (String. "done") transfer (first (:recur-transfers core))
                      row (first (filter #(= (:recur-core-node-id transfer) (:lhs %)) (:constraint-ledger typed)))
                      result (execute (get functions 'again) [true payload] functions 'again)]
                  (is (= :function (:target-kind transfer)))
                  (is (= [:gravity.type/bool :gravity.type/string] (get-in row [:solution :slot-types])))
                  (is (= :gravity.type/never (get-in typed [:type-table (:recur-core-node-id transfer)])))
                  (is (= [payload "" :normal] result)) (is (identical? payload (first result)))
                  (is (= ["done" "" :normal] (execute (get functions 'run) [] functions 'run))))
                (let [left (String. "left") right (String. "right")
                      cases [['swap [true left right] left] ['shadow [true left] "shadow"]
                             ['locals [true left] left] ['nested [true right] right] ['zero [] "zero"]
                             ['catch-abort [left right] left] ['catch-after [left] left]]]
                  (doseq [[function-name arguments expected] cases]
                    (let [result (execute (get functions function-name) arguments functions function-name)]
                      (is (= [expected "" :normal] result))
                      (when (or (identical? expected left) (identical? expected right)) (is (identical? expected (first result))))))
                  (doseq [[function-name arguments] [['abort [true left right]] ['after [true left]]]]
                    (let [result (execute (get functions function-name) arguments functions function-name)]
                      (is (= [left "" :thrown] result)) (is (identical? left (first result)))))
                  (let [abort-record (first (filter #(= 'abort (:definition-name %)) (:function-records core)))
                        target (get targets (:function-core-node-id abort-record))
                        transfer (first (filter #(= (:target-id target) (:target-id %)) (:recur-transfers core)))
                        row (first (filter #(= (:recur-core-node-id transfer) (:lhs %)) (:constraint-ledger typed)))]
                    (is (= false (get-in row [:solution :normal-transfer?])))
                    (is (= [:gravity.type/never :gravity.type/never :gravity.type/string] (get-in row [:solution :argument-types]))))
                  (let [record (first (filter #(= 'zero (:definition-name %)) (:function-records core)))
                        target (get targets (:function-core-node-id record))
                        transfer (first (filter #(= (:target-id target) (:target-id %)) (:recur-transfers core)))
                        row (first (filter #(= (:recur-core-node-id transfer) (:lhs %)) (:constraint-ledger typed)))]
                    (is (= 0 (:arity target) (:arity transfer)))
                    (is (= [] (get-in row [:solution :slot-types]) (get-in row [:solution :argument-types]))))
                  (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                    #(is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main :stdout ""
                             :entrypoint-result "left" :status :complete :clojure-seed-boundary? true :self-hosted? false}
                            (original runtime plan 'p15-s23-runtime-execute-closed-plan [assembled]))))
                  (doseq [changed [(assoc-in input [:request :canonical-core-artifact :recur-transfers 0 :tail-position] false)
                                   (assoc-in input [:request :canonical-core-artifact :recur-targets 0 :owner-core-node-id] :wrong)
                                   (update-in input [:typed :constraint-ledger] #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))]]
                    (is (= "P15S23Q002" (:id (operation #(diagnostic (fn []
                              (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'main) changed)))))))))))))))
    (is (= 16 @verifications))
    (is (= 0 @transports))))

(deftest function-recur-composition-work-development-kernel
  ;; Read-only source controls establish the harness schedule, not acceptance
  ;; of a new compiler candidate or a measured timing improvement.
  (let [forms (binding [*read-eval* false]
                (with-open [reader (java.io.PushbackReader. (io/reader (io/resource "gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj")))]
                  (loop [out []]
                    (let [form (read {:eof ::eof} reader)]
                      (if (= ::eof form) out (recur (conj out form)))))))
        calls (fn [form] (filter seq? (tree-seq coll? seq form)))
        lookup (fn [bindings name] (some (fn [[key value]] (when (= key name) value)) (partition 2 bindings)))
        composition (first (filter #(and (seq? %) (= 'deftest (first %)) (= 'current-gravity-function-recur-authenticated-composition (second %))) forms))
        outer-bindings (second (nth composition 2))
        references (filter #(and (= 'ns-resolve (first %)) (= (list 'quote 'gravity.self-hosting.sh09-body-effect-test) (second %))) (calls forms))
        record-loop (first (filter #(and (= 'doseq (first %)) (= '[record (:function-records core)] (second %))) (calls composition)))
        record-bindings (second (nth record-loop 2))
        accepted-let (first (filter #(and (= 'let (first %)) (some #{'standalone} (take-nth 2 (second %)))) (calls composition)))
        accepted-bindings (second accepted-let)
        hostile-loop (first (filter #(and (= 'doseq (first %)) (= 'changed (first (second %)))) (calls composition)))
        inventory (lookup outer-bindings 'inventory)
        input-loop (first (filter #(and (= 'doseq (first %)) (= 'extension (first (second %)))) (calls composition)))
        extensions (second (second input-loop))
        accepted (count (remove #(= :rejected (nth % 2)) inventory))
        rejected (- (count inventory) accepted)
        hostile-count (count (second (second hostile-loop)))
        main-count (count (filter #(= :main (nth % 2)) inventory))]
    (is (= 6 (count references)))
    (is (every? #(= (list 'quote 'fresh-type-products) (nth % 2)) references))
    (is (= '(get functions function-name) (lookup record-bindings 'compiled)))
    (is (not-any? #(= 'bootstrap/p15-s23-stage2-compile-function (first %)) (calls record-loop)))
    (is (= 5 (count (filter #(= 'is (first %)) (calls record-loop)))))
    (is (every? (set (tree-seq coll? seq record-loop))
                [:recur-target :parameter-binding-ids :arity :params :typed-lowering-provenance
                 :effect-legality :compiler-sources :lowering :source :sha256 :pending-c8-and-profile-authority]))
    (is (= '(if (= kind :again) (quote again) (quote swap)) (lookup accepted-bindings 'representative)))
    (is (= 'operation (first (lookup accepted-bindings 'standalone))))
    (is (= 1 (count (filter #(= 'bootstrap/p15-s23-stage2-compile-function (first %)) (calls (lookup accepted-bindings 'standalone))))))
    (is (some #{'(is (= standalone (get functions representative)))} (calls accepted-let)))
    (is (= 6 (* (count extensions) (count inventory))))
    (is (= 3 hostile-count))
    (is (= 16 (* (count extensions) (+ (* 2 accepted) rejected (* hostile-count main-count)))))
    (is (some #{(read-string "(is (= 16 @verifications))")} (calls composition)))))

(defn- current-gravity-loop-composition [selection]
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        {:keys [plan parent-source]} (typed-catch-runtime-projection)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter-text (slurp emitter-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        original bootstrap/p15-s23-stage2-runtime-execute-function verifications (atom 0)
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) transports (atom 0) original-transport (var-get transport)
        operation (fn [thunk] (let [before @verifications
                                    result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                                         (fn [runtime plan function args]
                                                           (when (= 'sh08-verify-function-type-result function) (swap! verifications inc))
                                                           (original runtime plan function args))] (thunk))]
                                (is (= 1 (- @verifications before))) result))
        execute (fn [instruction env functions name]
                  (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                    #(original runtime plan 'p15-s23-runtime-evaluate-in-context [instruction env "" functions [name]])))
        inventory [["accepted/loop-recur-types" "21695e7ab85adc9dd0fa5565cba22969be303a09477443b96184f52168eb5785" :seven nil]
                   ["accepted/local-runtime-loop" "878d2a6411c920f6a772eb7fe12b48d6d5cc151a3c95618c327dae61f3f7aedc" :main nil]
                   ["accepted/local-runtime-loop-decimal" "40c00f6f691473a158ef9c1238f03ecc972e4a4a2eab7743799bcd31f13721a3" :decimal nil]
                   ["rejected/loop-recur-slot-type" "6fe048118129ec6d1ccd9445b47992fbe484d4ef43a9d5833206ac5f707fb388" :rejected :loop-recur-slot-type-mismatch]
                   ["rejected/loop-recur-return-type" "fea5be4854d28b15b75fa155e29e465d7f4b1d160d55e26ebd9155b6ce7fecdc" :rejected :function-result-type-mismatch]
                   ["rejected/local-runtime-loop-i64-return" "7308b2b66f185d0ae159862935053beb569913d26d8ce0a69c2082501263fda4" :rejected :function-result-type-mismatch]]]
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp (:path parent-source)))) (:sha256 parent-source)))
    ;; Each of these twelve real input constructions is independent and occurs
    ;; once. Its immutable products are reused by every operation in that case.
    (doseq [extension [".gravity" ".qst"] [name hash kind reason] inventory
            :when (case selection
                    :all true
                    :remaining (not (and (= extension ".gravity") (contains? #{:seven :main} kind)))
                    :source-join (or (= kind :decimal) (and (= extension ".qst") (contains? #{:main :rejected} kind))))]
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" name extension)
            source-path (.getPath (io/file root relative)) _ (is (= hash (bootstrap/sha256-hex (slurp source-path))))
            p (fresh relative) input (select-keys p [:request :typed :verification]) typed (:typed input) core (get-in input [:request :canonical-core-artifact])
            macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
            reader (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact])
            module (assoc (bootstrap/parse-module source-path (:parsed-semantic-values reader)) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
            compile-function (fn [name] (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions name) input)))]
        (is (= :passed (get-in input [:verification :status])))
        (if (= kind :rejected)
          (do (is (= :rejected (:status typed)))
              (is (= "C7-TYPE-MISMATCH" (get-in typed [:diagnostics 0 :rule])))
              (is (= reason (get-in typed [:diagnostics 0 :reason])))
              (is (= :type-checking (get-in typed [:diagnostics 0 :stage])))
              (when (= name "rejected/local-runtime-loop-i64-return")
                (is (= :gravity.type/i64 (get-in typed [:diagnostics 0 :expected-type])))
                (is (= :gravity.type/integer (get-in typed [:diagnostics 0 :actual-type]))))
              (is (= "P15S23Q002" (:id (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'run) input))))))))
          (do
            (is (= :accepted (:status typed)) (pr-str (map #(select-keys % [:rule :reason :expected-type :actual-type]) (:diagnostics typed))))
            (when-not (= :accepted (:status typed)) (throw (ex-info "Expected loop input did not reach typed acceptance" {:source-path source-path :status (:status typed)})))
            (if (= kind :decimal)
              (let [record (first (:loop-bindings core)) node (first (filter #(= (:initializer-node-id record) (:node-id %)) (:nodes core)))
                    failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'decimal-loop) input))))]
                (is (= :gravity.type/decimal (get-in typed [:type-table (:node-id node)])))
                (is (= "P15S23Q002" (:id failure))) (is (= :unelaborated-typed-numeric-literal (:reason failure)))
                (is (= (:node-id node) (:core-node-id failure)))
                (is (= (select-keys (:source node) [:syntax-id :origin-chain :generated-origin]) (select-keys failure [:syntax-id :origin-chain :generated-origin])))
                (is (= (get-in node [:source :semantic-span]) (:source-span failure))))
              (let [integer-nodes (filter #(and (= :literal (:core-form %)) (= :integer (get-in % [:attributes :literal-kind]))) (:nodes core))
                    values (mapv #(get-in % [:attributes :value]) integer-nodes)
                    source-integers (filter integer? (tree-seq coll? seq (:parsed-semantic-values reader)))
                    _ (is (every? integer? values))
                    _ (is (= (if (= kind :seven) #{7} #{0 -7 31}) (set values) (set source-integers)))
                    _ (is (= (if (= kind :seven) [7] [0 -7 31]) values (vec source-integers)))
                    _ (is (every? #(= :gravity.type/integer (get-in typed [:type-table (:node-id %)])) integer-nodes))
                    assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                    functions (:functions assembled)]
                (is (= (count (filter #(= :loop (:core-form %)) (:nodes core))) (get-in assembled [:instruction-summary :loop])))
                (is (= (count (:recur-transfers core)) (get-in assembled [:instruction-summary :recur])))
                (doseq [function-name (keys definitions)]
                  (let [compiled (compile-function function-name)]
                    (is (= compiled (get functions function-name)))
                    (is (= :pending-c8-and-profile-authority (get-in compiled [:typed-lowering-provenance :effect-legality])))
                    (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))))
                (if (= kind :seven)
                  (do (is (= :gravity.type/integer (get-in functions ['run :type-input :return])))
                      (is (= [7 "" :normal] (execute (first (get-in functions ['run :instructions])) {} functions 'run))))
                  (let [left (String. "left") right (String. "right")
                        cases [['swap {'left left 'right right} left] ['sequential {'problem left} left]
                               ['nested {'left left 'right right} right] ['zero {} "zero"]
                               ['throw-init {'problem left} left] ['throw-arg {'problem left} left] ['throw-body {'problem left} left]]]
                    (doseq [[function-name env expected] cases]
                      (let [result (execute (first (get-in functions [function-name :instructions])) env functions function-name)]
                        (is (= [expected "" :normal] result))
                        (when (or (identical? expected left) (identical? expected right)) (is (identical? expected (first result))))))
                    (is (= [0 -7 31] (mapv #(first (execute % {} functions 'integers)) (get-in functions ['integers :instructions 0 :body]))))
                    (is (= [0 -7 31] (mapv #(get-in % [:bindings 1 :expr :value]) (get-in functions ['integers :instructions 0 :body]))))
                    (is (= :gravity.type/integer (get-in functions ['integer-call :type-input :return])))
                    (is (= [31 "" :normal] (execute (first (get-in functions ['integer-call :instructions])) {} functions 'integer-call)))
                    (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                      #(let [record (original runtime plan 'p15-s23-runtime-execute-closed-plan [assembled])]
                         (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main :stdout ""
                                 :entrypoint-result "left" :status :complete :clojure-seed-boundary? true :self-hosted? false} record))))
                    (doseq [changed [(assoc-in input [:request :canonical-core-artifact :recur-transfers 0 :tail-position] false)
                                     (assoc-in input [:request :canonical-core-artifact :recur-targets 0 :owner-core-node-id] :wrong)
                                     (update-in input [:typed :constraint-ledger] #(filterv (fn [row] (not= :sh08-loop-target-types-v1 (:producer-rule row))) %))]]
                      (is (= "P15S23Q002" (:id (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'main) changed))))))))))))))))
    (is (= 0 @transports))))

(deftest current-gravity-loop-authenticated-composition
  (current-gravity-loop-composition :all))

(deftest current-gravity-loop-remaining-authenticated-composition
  ;; V2 already executed the unchanged seven/main .gravity inputs successfully.
  ;; Freshly construct only the decimal and negative .gravity inputs, and all
  ;; six .qst inputs. No retained products or failed aggregate becomes authority.
  (current-gravity-loop-composition :remaining))

(deftest current-gravity-loop-source-join-authenticated-composition
  ;; The raw-root definition join changed every accepted function. Recheck the
  ;; ten-function qst main once, both previously failing decimal paths, and the
  ;; last three qst negatives whose completion was unobserved at interruption.
  (current-gravity-loop-composition :source-join))

(deftest current-gravity-function-recur-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        lit (fn [value] {:op :literal :value value}) local (fn [name] {:op :local :name name})
        branch (fn [test then else] {:op :if :test test :then then :else else})
        again (fn [target args] {:op :recur :target target :args args})
        call (fn [name args] {:op :function-call :function name :args args})
        escape (fn [value] {:op :throw :value value})
        say (fn [text value] {:op :do :body [{:op :println :args [(lit text)] :effect :io/write :capability :io/stdout} value]})
        function (fn [id params body] {:recur-target id :arity (count params) :params params :instructions [body]})
        activate #(invoke 'p15-s23-runtime-activate-function %1 %2 "" {} ['main])
        left (String. "left") right (String. "right")
        swap (function "swap" '[flag left right]
               (branch (local 'flag) (again "swap" [(lit false) (say "right" (local 'right)) (say "left" (local 'left))]) (local 'right)))
        result (activate swap [true left right])]
    (is (= [left "right\nleft\n" :normal] result))
    (is (identical? left (first result)))
    (let [names (mapv #(symbol (str "flag" %)) (range 32))
          shift (function "shift" names
                  (branch (local (first names)) (again "shift" (conj (mapv local (subvec names 1)) (lit false))) (lit :done)))
          result (invoke 'p15-s23-runtime-activate-function shift (vec (concat (repeat 31 true) [false])) "" {} (vec (repeat 256 'active)))]
      (is (= [:done "" :normal] result)))
    (let [shadow (String. "shadow")
          f (function "shadow" '[flag value]
              (branch (local 'flag) {:op :let :bindings [{:name 'value :expr (lit shadow)}]
                                     :body [(again "shadow" [(lit false) (local 'value)])]} (local 'value)))
          result (activate f [true left])]
      (is (= [shadow "" :normal] result)) (is (identical? shadow (first result))))
    (let [f (function "fresh" '[flag value]
              (branch (local 'flag) {:op :let :bindings [{:name 'leaked :expr (lit :prior)}]
                                     :body [(again "fresh" [(lit false) (local 'value)])]} (local 'leaked)))
          result (activate f [true left])]
      (is (= ["" :rejected] (subvec result 1))))
    (let [inner {:op :loop :target "inner" :bindings [{:name 'flag :expr (lit true)}]
                 :body [(branch (local 'flag) (again "inner" [(lit false)]) (local 'value))]}
          f (function "outer" '[flag value]
              (branch (local 'flag) {:op :let :bindings [{:name 'saved :expr inner}]
                                     :body [(again "outer" [(lit false) (local 'saved)])]} (local 'value)))
          result (activate f [true right])]
      (is (= [right "" :normal] result)) (is (identical? right (first result))))
    (doseq [payload [nil false {:op :recur :target "data"} [[left] "" :recur "data"] (ex-info "inert" {})]]
      (let [f (function "data" '[flag value] (branch (local 'flag) (again "data" [(lit false) (local 'value)]) (local 'value)))
            result (activate f [true payload])]
        (is (= ["" :normal] (subvec result 1))) (is (identical? payload (first result)))))
    (let [f (function "abort" '[flag value] (again "abort" [(escape (local 'value)) (say "unreachable" (lit false))]))
          result (activate f [true left])]
      (is (= [left "" :thrown] result)) (is (identical? left (first result))))
    (let [f (function "abort-later" '[flag value] (again "abort-later" [(say "first" (lit false)) (escape (local 'value))]))
          result (activate f [true left])]
      (is (= [left "first\n" :thrown] result)) (is (identical? left (first result))))
    (let [f (function "after" '[flag value] (branch (local 'flag) (again "after" [(say "restart" (lit false)) (local 'value)]) (escape (local 'value))))
          result (activate f [true left])]
      (is (= [left "restart\n" :thrown] result)) (is (identical? left (first result))))
    (doseq [body [(again "foreign" [(say "unreachable" (lit false)) (local 'value)])
                  (again "guard" [(say "unreachable" (lit false))])
                  {:op :do :body [(again "guard" [(say "unreachable" (lit false)) (local 'value)]) (lit :unreachable)]}
                  (branch (again "guard" [(say "unreachable" (lit false)) (local 'value)]) (lit :bad) (lit :bad))
                  {:op :try :protected (again "guard" [(say "unreachable" (lit false)) (local 'value)])
                   :handlers [{:type :gravity.type/string :name 'error :body (lit :bad)}]}
                  {:op :try :protected (escape (local 'value))
                   :handlers [{:type :gravity.type/string :name 'error :body (again "guard" [(say "unreachable" (lit false)) (local 'error)])}]}
                  {:op :loop :target "nearest" :bindings [] :body [(again "guard" [(say "unreachable" (lit false)) (local 'value)])]}]]
      (let [result (activate (function "guard" '[flag value] body) [true left])]
        (is (= ["" :rejected] (subvec result 1))) (is (= "P15S23X002" (:diagnostic (first result))))))
    (let [callee (function "callee" [] (again "caller" [(say "unreachable" (lit false)) (lit left)]))
          caller (function "caller" '[flag value] (call 'callee []))
          result (invoke 'p15-s23-runtime-activate-function caller [true left] "" {'callee callee} ['caller])]
      (is (= ["" :rejected] (subvec result 1))))
    (let [f (function "cycle" '[flag value] (branch (local 'flag) (again "cycle" [(lit false) (local 'value)])
                                                  (call 'cycle [(say "unreachable" (lit false)) (local 'value)])))
          result (invoke 'p15-s23-runtime-evaluate-in-context (call 'cycle [(lit true) (lit left)]) {} "" {'cycle f} ['main])]
      (is (= ["" :rejected] (subvec result 1))))
    (doseq [bad [(assoc swap :recur-target nil) (assoc swap :recur-target 42) (assoc swap :recur-target "")
                 (assoc swap :recur-target (apply str (repeat 257 "x"))) (assoc swap :params nil) (assoc swap :arity nil)
                 (assoc swap :params '[flag left left]) (assoc swap :instructions [42])]]
      (let [result (invoke 'p15-s23-runtime-evaluate-in-context
                     (call 'bad [(say "unreachable" (lit true)) (lit left) (lit right)]) {} "" {'bad bad} ['main])]
        (is (= ["" :rejected] (subvec result 1)))))
    (doseq [bad [{:params nil :instructions [(say "unreachable" (lit nil))]}
                 {:arity nil :instructions [(say "unreachable" (lit nil))]}
                 {:recur-target nil :instructions [(say "unreachable" (lit nil))]}]]
      (let [result (invoke 'p15-s23-runtime-execute-closed-plan {:entrypoint 'main :functions {'main bad}})]
        (is (= :rejected (:status result))) (is (= "" (:stdout result)))))
    (let [f (function "zero" [] (lit :entry))
          result (invoke 'p15-s23-runtime-execute-closed-plan {:entrypoint 'main :functions {'main f}})]
      (is (= :complete (:status result))) (is (= :entry (:entrypoint-result result))))
    (let [fatal (AssertionError. "fatal recur argument") value (proxy [Object] [] (toString [] (throw fatal)))
          f (function "fatal" '[flag value] (again "fatal" [(lit false) {:op :builtin-call :function 'str :args [(lit value)]}]))
          observed (seed-throw-test-capture #(activate f [true left]))]
      (is (identical? fatal observed)))))

(deftest current-gravity-loop-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        literal (fn [value] {:op :literal :value value}) local (fn [name] {:op :local :name name})
        binding (fn [name expr] {:name name :expr expr})
        loop-ir (fn [target bindings body] {:op :loop :target target :bindings bindings :body body})
        recur-ir (fn [target args] {:op :recur :target target :args args})
        branch (fn [test then else] {:op :if :test test :then then :else else})
        escape (fn [value] {:op :throw :value (literal value)})
        say (fn [label value] {:op :do :body [{:op :println :args [(literal label)] :effect :io/write :capability :io/stdout} value]})
        recover (fn [value] {:op :try :protected value :handlers [{:type :gravity.type/string :name 'error :body (local 'error)}]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        slots [(binding 'again (literal true)) (binding 'left (literal :left)) (binding 'right (literal :right))]
        swap-loop (loop-ir "swap" slots [(branch (local 'again) (recur-ir "swap" [(literal false) (local 'right) (local 'left)]) (local 'right))])
        payload (String. "original")]
    (is (= [:left "" :normal] (evaluate swap-loop)))
    (is (= [7 "init\nstep\nfinal\n" :normal]
           (evaluate (loop-ir "once" [(binding 'again (literal true)) (binding 'value (say "init" (literal 0)))]
                      [(branch (local 'again) (recur-ir "once" [(literal false) (say "step" (literal 7))]) (say "final" (local 'value)))]))))
    (is (= ["first" "" :normal]
           (evaluate (loop-ir "shadow" [(binding 'x (literal "first")) (binding 'saved (local 'x)) (binding 'x (literal "last"))] [(local 'saved)]))))
    (is (= [:zero "" :normal] (evaluate (loop-ir "zero" [] [(literal :zero)]))))
    (is (= [:inner "" :normal] (evaluate (loop-ir "outer" [] [(loop-ir "inner" [] [(literal :inner)])]))))
    (let [names (mapv #(symbol (str "flag" %)) (range 32))
          bindings (mapv #(binding %1 (literal %2)) names (concat (repeat 31 true) [false]))
          arguments (conj (mapv local (subvec names 1)) (literal false))]
      (is (= [:done "" :normal]
             (evaluate (loop-ir "shift" bindings [(branch (local (first names)) (recur-ir "shift" arguments) (literal :done))])))))
    (doseq [value [nil false {:op :recur :target "data"} [[1] "" :recur "data"] (ex-info "inert" {})]]
      (let [result (evaluate (loop-ir "data" [(binding 'value (literal value))] [(local 'value)]))]
        (is (identical? value (first result))) (is (= ["" :normal] (subvec result 1))))
      (let [result (evaluate (loop-ir "data" [] [(escape value)]))]
        (is (identical? value (first result))) (is (= ["" :thrown] (subvec result 1)))))
    (doseq [instruction [(loop-ir "init" [(binding 'x (escape payload)) (binding 'y (say "unreachable" (literal nil)))] [(literal nil)])
                         (loop-ir "body" [] [(escape payload)])
                         (loop-ir "args" [(binding 'x (literal true)) (binding 'y (literal false))]
                           [(recur-ir "args" [(escape payload) (say "unreachable" (literal false))])])]]
      (is (= [payload "" :normal] (evaluate (recover instruction)))))
    (doseq [instruction [(loop-ir "bad" 42 [(literal nil)]) (loop-ir "bad" [] 42) (loop-ir "bad" [] [])
                         (loop-ir {} [] [(literal nil)]) (loop-ir "bad" [42] [(literal nil)])
                         (loop-ir "bad" [(binding 42 (say "unreachable" (literal nil)))] [(literal nil)])
                         (loop-ir "bad" [(assoc (binding 'x (say "unreachable" (literal nil))) :extra true)] [(literal nil)])]]
      (let [result (evaluate instruction)]
        (is (= ["" :rejected] (subvec result 1))) (is (= "P15S23X002" (:diagnostic (first result))))))
    (doseq [body [(recur-ir "wrong" [(say "unreachable" (literal nil))]) (recur-ir "guard" [])
                  {:op :do :body [(recur-ir "guard" [(literal false)]) (literal :unreachable)]}
                  (branch (recur-ir "guard" [(literal false)]) (literal :unreachable) (literal :unreachable))
                  (recover (recur-ir "guard" [(literal false)]))
                  (loop-ir "inner" [] [(recur-ir "guard" [(say "unreachable" (literal false))])])]]
      (let [result (evaluate (loop-ir "guard" [(binding 'x (literal true))] [body]))]
        (is (= ["" :rejected] (subvec result 1))) (is (= "P15S23X002" (:diagnostic (first result))))))
    (let [result (evaluate (recur-ir "none" [(say "unreachable" (literal false))]))]
      (is (= ["" :rejected] (subvec result 1))))
    (let [instruction (loop-ir "caller" [] [{:op :function-call :function 'escape :args []}])
          functions {'escape {:arity 0 :params [] :instructions [(recur-ir "caller" [])]}}
          result (invoke 'p15-s23-runtime-evaluate-in-context instruction {} "" functions ['main])]
      (is (= ["" :rejected] (subvec result 1))))
    (let [fatal (AssertionError. "fatal primitive") value (proxy [Object] [] (toString [] (throw fatal)))
          result (seed-throw-test-capture #(evaluate (recover (loop-ir "fatal" [] [{:op :builtin-call :function 'str :args [(literal value)]}]))))]
      (is (identical? fatal result)))))

(defn- typed-direct-call-kernel-context [base]
  (let [source (fn [id] {:form-id id :syntax-id id :semantic-span nil :origin-chain nil :generated-origin nil})
        record (assoc (get-in base [:records 'identity]) :definition-binding-id :target-binding)
        call {:core-node-id :call :operator-node-id :operator :operator-binding-id :target-binding :argument-node-ids [:argument]
              :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :result-policy :call-result}
        edge {:ordinal 0 :call-core-node-id :call :caller-function-syntax-id :caller-fn :caller-function-core-node-id :caller-core
              :callee-binding-id :target-binding :callee-definition-syntax-id :target-name :callee-function-syntax-id :fn :callee-function-core-node-id :function
              :argument-core-node-ids [:argument] :ordered-evaluation-node-ids [:operator :argument] :evaluation-order :operator-then-arguments :classification :local-function}
        module (get-in base [:core :module])
        fact {:artifact :gravity/c7-call-type-fact
              :fact-id-request {:domain :gravity/sh08-call-type-fact-v1 :call-core-node-id :call :callee-function-syntax-id :fn
                                :argument-types [:gravity.type/string] :result-type :gravity.type/string}
              :call-core-node-id :call :caller-function-syntax-id :caller-fn :callee-function-syntax-id :fn
              :argument-types [:gravity.type/string] :result-type :gravity.type/string :evaluation-order :operator-then-arguments
              :source-span nil :syntax-id :call-form :origin-chain nil :generated-origin nil
              :profile (:profile module) :target (:target module) :effects (:effects module) :capabilities (:capabilities module) :status :inferred}
        row {:constraint-id-request {:domain :gravity/sh08-function-call-constraint-v1 :call-core-node-id :call :callee-function-syntax-id :fn}
             :kind :equality :lhs :fn :rhs [{:parameter :parameter :argument :gravity.type/string :chosen :gravity.type/string}]
             :origin nil :profile :meta :target :jvm :status :solved :diagnostics []}]
    (-> base (assoc :locals {:caller-parameter 'problem} :parameter-types {:caller-parameter :gravity.type/string}
                    :current-function {:function-syntax-id :caller-fn :function-core-node-id :caller-core}
                    :calls {:call call} :edges {:call edge} :call-facts {:call fact} :call-order {:call 0} :recursion-components [] :ledger [row])
        (assoc-in [:core :function-records] [record]) (assoc-in [:records 'identity] record)
        (assoc-in [:bindings :target-binding] {:binding-id :target-binding :name 'identity :definition-syntax-id :target-name
                                              :binding-class :namespace :kind :function :namespace (:namespace module)})
        (update :nodes merge {:operator {:node-id :operator :core-form :reference :children [] :resolved-binding-ids [:target-binding]
                                         :attributes {:binding-id :target-binding :symbol 'identity :position :operator :binding-class :namespace :definition-syntax-id :target-name}
                                         :source (source :operator-form)}
                              :argument {:node-id :argument :core-form :reference :children [] :resolved-binding-ids [:caller-parameter]
                                         :attributes {:binding-id :caller-parameter} :source (source :argument-form)}
                              :call {:node-id :call :core-form :call :children [:operator :argument] :resolved-binding-ids [:target-binding]
                                     :attributes {:operator-child-index 0 :argument-count 1 :argument-child-indexes [1] :evaluation-order :operator-then-arguments :dispatch :resolved-symbol-call}
                                     :evaluation {:kind :operator-then-arguments :owner-function-syntax-id :caller-fn
                                                  :order [{:index 0 :core-node-id :operator} {:index 1 :core-node-id :argument}]} :source (source :call-form)}})
        (update :types merge {:operator (get-in base [:functions 'identity]) :argument :gravity.type/string :call :gravity.type/string})
        (update :forms merge {:name-form {:form-id :name-form :syntax-id :target-name :value 'identity :parent-form-id :definition-form}
                              :call-form {:form-id :call-form :syntax-id :call-form :kind :list :child-form-ids [:operator-form :argument-form]}
                              :operator-form {:form-id :operator-form :syntax-id :operator-form :kind :symbol :value 'identity :parent-form-id :call-form}
                              :argument-form {:form-id :argument-form :syntax-id :argument-form :kind :symbol :value 'problem :parent-form-id :call-form}}))))

(deftest current-gravity-integral-ordering-fixtures-development-kernel
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))]
    (doseq [[disposition basename count-functions]
            (into [["accepted" "local-runtime-integral-ordering" 10]]
                  (map #(vector "rejected" (str "integral-ordering-" %) 1)
                       ["arity" "mixed" "nonintegral" "unknown" "escape" "lexical" "late-invalid"]))]
      (let [base (str "bootstrap/clojure/fixtures/self-hosting/sh-08/" disposition "/" basename)
            path (.getPath (io/file root (str base ".gravity"))) text (slurp path)]
        (is (= text (slurp (io/file root (str base ".qst")))))
        (is (= count-functions (count (filter #(and (seq? %) (= 'defn (first %))) (bootstrap/read-forms path text)))))))))

(deftest current-gravity-integral-ordering-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        c8 (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'leaf)
        runtime-plan (:plan (typed-catch-runtime-projection))
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        original bootstrap/p15-s23-stage2-runtime-execute-function verified (atom [])
        operation (fn [thunk]
                    (let [before (count @verified)
                          result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                              (fn [runtime plan function args]
                                                (let [result (original runtime plan function args)]
                                                  (when (= function 'sh08-verify-function-type-result) (swap! verified conj result)) result))]
                                   (thunk))]
                      (is (= 1 (- (count @verified) before))) result))
        left (String. "first numeric operand") right (String. "second numeric operand")
        specifications [['less [Long/MIN_VALUE Long/MAX_VALUE] true] ['less [1 1] false]
                        ['less-equal [0 18446744073709551615N] true] ['less-equal [18446744073709551615N 18446744073709551615N] true]
                        ['less-equal [18446744073709551615N 0] false]
                        ['greater [127 -128] true] ['greater [-128 127] false] ['greater [0 0] false]
                        ['greater-equal [-2147483648 2147483647] false] ['greater-equal [2147483647 -2147483648] true]
                        ['greater-equal [-2147483648 -2147483648] true]
                        ['bounded [Long/MIN_VALUE Long/MAX_VALUE] "below"] ['bounded [2 1] "at-or-above"] ['bounded [1 1] "at-or-above"]
                        ['caught-first [left right] left] ['caught-right [1 right] right]
                        ['lexical-shadowed [left right] left] ['main [] true]]
        cases (mapv
               (fn [extension]
                 (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-integral-ordering" extension)
                       path (.getPath (io/file root relative)) products (fresh relative)
                       input (select-keys products [:request :typed :verification])
                       ready (is (= :accepted (get-in input [:typed :status])) (pr-str (take 3 (get-in input [:typed :diagnostics]))))
                       _ (when-not ready (throw (ex-info "Integral ordering products rejected before lowering" {:diagnostics (get-in input [:typed :diagnostics])})))
                       artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
                       parsed (get-in artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
                       raw-module (assoc (bootstrap/parse-module path parsed) :forms (:expanded-forms artifact))
                       definitions (bootstrap/stage0-function-table raw-module) module (assoc raw-module :function-table definitions)
                       standalone (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'bounded) input))
                       assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
                       core (get-in input [:request :canonical-core-artifact]) facts (get-in input [:typed :identity-input])
                       numeric (filterv #(= :direct-integral-ordering (:dispatch %)) (:call-type-facts facts))
                       descriptors (mapv :applied-integral-ordering numeric)
                       effects (c8 'body-analyze-module (:request input) facts {} 65536)]
                   (is (= :passed (get-in input [:verification :status])))
                   (is (= 10 (count (:function-records core)) (count (:function-type-table facts)) (count (:functions assembled))))
                   (is (= 9 (count (:calls core)) (count (:call-edges core)) (count (:call-type-facts facts))))
                   (is (= 8 (count numeric) (count (filter #(= :sh08-integral-ordering-v1 (:producer-rule %)) (:constraint-ledger facts)))))
                   (is (= {:normal 6 :not-invoked 2} (frequencies (map :application descriptors))))
                   (is (= #{:gravity.type/integer :gravity.type/i64 :gravity.type/u64 :gravity.type/i8 :gravity.type/i32 nil}
                          (set (map #(first (:parameter-types %)) descriptors))))
                   (is (= standalone (get-in assembled [:functions 'bounded])))
                   (is (= 8 (get-in assembled [:instruction-summary :builtin-call])))
                   (is (= 1 (get-in assembled [:instruction-summary :function-call])))
                   (is (= 1 (get-in assembled [:instruction-summary :structural-match])))
                   (is (= :pending-c8-and-profile-authority (get-in assembled [:effect-summary :legality])))
                   (doseq [descriptor descriptors]
                     (is (= 7 (count (:source-call descriptor))))
                     (is (= 12 (count (:source-edge descriptor))))
                     (is (= :nonlocal-or-nonfunction (get-in descriptor [:source-edge :classification])))
                     (is (nil? (get-in descriptor [:source-edge :callee-function-syntax-id]))))
                   (doseq [[name arguments expected] specifications]
                     (let [result (original runtime runtime-plan 'p15-s23-runtime-evaluate-in-context
                                            [{:op :function-call :function name :args (literal-instructions arguments)} {} "" (:functions assembled) []])]
                       (is (= [expected "" :normal] result) (str extension " " name " " arguments))
                       (when (or (identical? expected left) (identical? expected right)) (is (identical? expected (first result))))))
                   (is (= :accepted (:status effects)) (pr-str (select-keys effects [:reason :diagnostics])))
                   (when (= :accepted (:status effects))
                     ;; Direct numeric effects are empty. Constructor allocation
                     ;; inference is a separate pending slice; source declares it.
                     (is (contains? (set (get-in facts [:module :effects])) :memory/allocate))
                     (doseq [fact numeric]
                       (let [id (:call-core-node-id fact)]
                         (is (= #{} (get-in effects [:graph :nodes id :direct])))
                         (is (nil? (get-in effects [:graph :nodes id :call-body-dependency])))))
                     (doseq [name ['caught-first 'caught-right]]
                       (let [function (first (filter #(= name (:definition-name %)) (:function-type-table facts)))]
                         (is (= [] (get-in effects [:graph :functions (:function-id function) :throws]))))))
                   {:input input :module module :definitions definitions :descriptors descriptors})) [".gravity" ".qst"])
        {:keys [input module definitions descriptors]} (first cases) descriptor (first descriptors)]
    (is (= (get-in cases [0 :input :typed :identity-input]) (get-in cases [1 :input :typed :identity-input])))
    (doseq [changed [(assoc descriptor :numeric-mode :wrapping)
                     (assoc descriptor :operation '> :intrinsic :gravity.math.core/integral-greater-than)]]
      (let [typed (walk/postwalk #(if (= % descriptor) changed %) (:typed input))
            failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'bounded)
                                                                                            (assoc input :typed typed)))))]
        (is (= "P15S23Q002" (:id failure)))
        (is (= :rejected (:status (last @verified))))))
    (is (= 6 (count @verified)))
    (doseq [[suffix reason] [["arity" :integral-ordering-arity-mismatch] ["mixed" :integral-ordering-operand-type-mismatch]
                             ["nonintegral" :integral-ordering-operand-type-mismatch] ["unknown" :integral-ordering-operand-type-mismatch]
                             ["late-invalid" :integral-ordering-operand-type-mismatch] ["lexical" :unsupported-nonlocal-call]
                             ["escape" :unsupported-or-unresolved-core-node-type]]]
      (let [products (fresh (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/integral-ordering-" suffix ".gravity"))]
        (is (= :rejected (get-in products [:typed :status])) suffix)
        (is (= reason (get-in products [:typed :diagnostics 0 :reason])) suffix)))))

(deftest current-gravity-integral-ordering-lowering-development-kernel
  (require 'gravity.self-hosting.sh08-function-call-type-test)
  (let [upstream 'gravity.self-hosting.sh08-function-call-type-test input-fn (ns-resolve upstream 'integral-ordering-input)
        c7 (ns-resolve upstream 'invoke-c7)
        path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        input (input-fn '< [:gravity.type/i64 :gravity.type/i64])
        {:keys [core node call edge nodes bindings types module forms]} input
        descriptor (c7 'sh08-io-descriptor [core node call edge nodes bindings types module])
        context {:core core :nodes nodes :forms forms :bindings bindings :types (assoc types :operator descriptor :call :gravity.type/bool)
                 :calls {:call call} :edges {:call edge} :call-order {:call 0}
                 :call-facts {:call (c7 'sh08-io-call-fact [descriptor module])} :ledger [(c7 'sh08-io-constraint [descriptor])]
                 :locals {:arg0 'left :arg1 'right} :parameter-types types}
        compile #(invoke 'p15-s23-typed-compile-node % :call 65536)
        result (compile context)]
    (is (= :complete (:status result)) (pr-str result))
    (is (= {:op :builtin-call :function '< :args [{:op :local :name 'left} {:op :local :name 'right}]} (:value result)))
    (doseq [changed [(assoc descriptor :operation '> :intrinsic :gravity.math.core/integral-greater-than)
                     (assoc descriptor :semantic-owner 'gravity.compare)
                     (assoc descriptor :profile :hosted) (assoc descriptor :target :wasm)
                     (assoc descriptor :numeric-mode :wrapping) (assoc descriptor :safety :unsafe-island)
                     (assoc descriptor :safety-rule :target-default)
                     (assoc descriptor :parameter-types [:gravity.type/u64 :gravity.type/u64])
                     (assoc descriptor :result-type :gravity.type/i64)
                     (assoc-in descriptor [:source-binding :binding-class] :import)
                     (assoc-in descriptor [:source-binding :package :version] "forged")
                     (assoc-in descriptor [:source-call :argument-node-ids] [:arg1 :arg0])
                     (assoc-in descriptor [:source :syntax-id] :forged)
                     (assoc descriptor :extra true) (dissoc descriptor :invalidation)]]
      (let [candidate (-> context (assoc-in [:types :operator] changed)
                          (assoc :call-facts {:call (c7 'sh08-io-call-fact [changed module])}
                                 :ledger [(c7 'sh08-io-constraint [changed])]))]
        (is (= :rejected (:status (compile candidate))) (pr-str changed))))
    (doseq [mutate [#(assoc-in % [:types :operator :numeric-mode] :wrapping)
                    #(assoc-in % [:types :operator :safety-rule] :backend-default)
                    #(assoc-in % [:types :operator :parameter-types] [:gravity.type/u64 :gravity.type/u64])
                    #(assoc-in % [:types :operator :result-type] :gravity.type/i64)
                    #(assoc-in % [:types :operator :source-binding :package :version] "forged")
                    #(assoc-in % [:types :operator :extra] true)
                    #(assoc-in % [:bindings :ordering-binding :binding-class] :import)
                    #(assoc-in % [:edges :call :callee-function-syntax-id] :fake)
                    #(assoc-in % [:edges :call :extra] true)
                    #(assoc-in % [:calls :call :extra] true)
                    #(assoc-in % [:forms :arg1 :parent-form-id] :sibling)
                    #(assoc-in % [:nodes :call :evaluation :order] [])
                    #(assoc-in % [:call-facts :call :effects] [:io/write])
                    #(assoc % :ledger []) #(update % :ledger into (:ledger %))
                    #(assoc-in % [:ledger 0 :solution :numeric-mode] :wrapping)]]
      (is (= :rejected (:status (compile (mutate context))))))
    (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :call 1) [:facts :reason])))))

(deftest current-gravity-integral-ordering-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        evaluate #(bootstrap/p15-s23-stage2-runtime-execute-function runtime plan 'p15-s23-runtime-evaluate-closed-instruction [% {} ""])
        literal (fn [value] {:op :literal :value value})
        pairs [[-128 127] [-32768 32767] [-2147483648 2147483647] [Long/MIN_VALUE Long/MAX_VALUE]
               [0 255] [0 65535] [0 4294967295] [0 18446744073709551615N]
               [-184467440737095516160N 184467440737095516160N] [-1 0] [0 0] [1 -1]]]
    (doseq [[symbol comparison] [['< <] ['<= <=] ['> >] ['>= >=]] [left right] pairs]
      (is (= [(comparison left right) "" :normal]
             (evaluate {:op :builtin-call :function symbol :args [(literal left) (literal right)]}))))
    (doseq [args [[{:op :throw :value (literal "first")} {:op :throw :value (literal "second")}]
                  [{:op :throw :value (literal "first")} (literal 1)]]]
      (is (= ["first" "" :thrown] (evaluate {:op :builtin-call :function '< :args args}))))
    (is (= ["second" "" :thrown]
           (evaluate {:op :builtin-call :function '< :args [(literal 1) {:op :throw :value (literal "second")}]})))))

(deftest current-gravity-direct-call-lowering-development-kernel
  (let [path (bootstrap/p15-s23-stage2-compiler-artifact-source-path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        {:keys [module input observation]} (typed-lowering-kernel-data)
        base (:value (invoke 'p15-s23-typed-context module input observation)) context (typed-direct-call-kernel-context base)
        compile #(invoke 'p15-s23-typed-compile-node % :call 65536)]
    (is (= {:op :function-call :function 'identity :args [{:op :local :name 'problem}]} (:value (compile context))))
    (is (= :unsupported-or-mismatched-typed-direct-call
           (get-in (compile (assoc-in context [:edges :call :classification] :nonlocal-or-nonfunction)) [:facts :reason])))
    (is (= {:op :function-call :function 'identity :args [{:op :local :name 'problem}]}
           (:value (compile (-> context (assoc-in [:forms :operator-form :value] 'example.typed/identity)
                                        (assoc-in [:nodes :operator :attributes :symbol] 'example.typed/identity))))))
    (doseq [mutate [#(assoc-in % [:nodes :call :children] 42) #(assoc-in % [:forms :call-form :child-form-ids] 42)
                    #(assoc-in % [:calls :call :argument-node-ids] 42)
                    #(assoc-in % [:calls :call :operator-binding-id] :caller-parameter)
                    #(assoc-in % [:edges :call :classification] :nonlocal-or-nonfunction)
                    #(assoc-in % [:edges :call :callee-definition-syntax-id] :def)
                    #(assoc-in % [:edges :call :callee-function-syntax-id] :caller-fn)
                    #(assoc-in % [:edges :call :caller-function-core-node-id] :wrong)
                    #(assoc-in % [:call-facts :call :argument-types] [:gravity.type/bool])
                    #(assoc-in % [:call-facts :call :result-type] :gravity.type/never)
                    #(assoc-in % [:bindings :target-binding :namespace] 'foreign)
                    #(assoc-in % [:bindings :target-binding :binding-class] :lexical)
                    #(assoc-in % [:forms :operator-form :value] 'relay)
                    #(-> % (assoc-in [:forms :operator-form :value] 'foreign/identity) (assoc-in [:nodes :operator :attributes :symbol] 'foreign/identity))
                    #(assoc-in % [:nodes :operator :attributes :definition-syntax-id] :def)
                    #(assoc-in % [:forms :argument-form :parent-form-id] :sibling)
                    #(assoc-in % [:nodes :call :evaluation :order] [{:index 0 :core-node-id :argument} {:index 1 :core-node-id :operator}])
                    #(assoc-in % [:types :call] :gravity.type/bool)
                    #(assoc % :ledger []) #(update % :ledger into (:ledger %))
                    #(assoc-in % [:ledger 0 :rhs 0 :chosen] :gravity.type/bool)
                    #(assoc % :recursion-components [{:kind :self-recursive}])]]
      (let [result (compile (mutate context))]
        (is (= :rejected (:status result))) (is (= "P15S23Q002" (:diagnostic result)))))
    (is (= :typed-lowering-work-limit (get-in (invoke 'p15-s23-typed-compile-node context :call 1) [:facts :reason])))
    (let [huge (-> context (assoc-in [:nodes :call :children] (vec (repeat 65537 :argument)))
                          (assoc-in [:forms :call-form :child-form-ids] (vec (repeat 65537 :argument-form))))]
      (is (= :typed-lowering-work-limit (get-in (compile huge) [:facts :reason]))))
    (doseq [[path value] [[[:request :canonical-core-artifact :calls] 42]
                          [[:request :canonical-core-artifact :call-edges] 42]
                          [[:request :canonical-core-artifact :recursion-components] 42]
                          [[:typed :call-type-facts] 42]
                          [[:request :canonical-core-artifact :calls] [{:core-node-id :extra} {:core-node-id :extra}]]
                          [[:typed :call-type-facts] [{:call-core-node-id :extra}]]]]
      (is (= :rejected (:status (invoke 'p15-s23-typed-context module (assoc-in input path value) observation)))))
    (let [context (-> context (assoc-in [:nodes :argument :core-form] :throw) (assoc-in [:nodes :argument :attributes] {})
                      (assoc-in [:nodes :argument :children] [:caller-value])
                      (assoc-in [:nodes :caller-value] {:node-id :caller-value :core-form :reference :children [] :attributes {:binding-id :caller-parameter}})
                      (assoc-in [:types :caller-value] :gravity.type/string) (assoc-in [:types :argument] :gravity.type/never) (assoc-in [:types :call] :gravity.type/never)
                      (assoc-in [:call-facts :call :argument-types] [:gravity.type/never]) (assoc-in [:call-facts :call :result-type] :gravity.type/never)
                      (assoc-in [:call-facts :call :fact-id-request :argument-types] [:gravity.type/never])
                      (assoc-in [:call-facts :call :fact-id-request :result-type] :gravity.type/never)
                      (assoc-in [:ledger 0 :rhs 0 :argument] :gravity.type/never))]
      (is (= {:op :function-call :function 'identity :args [{:op :throw :value {:op :local :name 'problem}}]} (:value (compile context)))))))

(deftest current-gravity-direct-call-runtime-development-kernel
  (let [plan (:plan (typed-catch-runtime-projection))
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime plan function (vec args)))
        literal (fn [value] {:op :literal :value value}) local (fn [name] {:op :local :name name})
        call (fn [name args] {:op :function-call :function name :args args})
        escape (fn [value] {:op :throw :value value})
        say (fn [label value] {:op :do :body [{:op :println :args [(literal label)] :effect :io/write :capability :io/stdout} value]})
        recover (fn [value] {:op :try :protected value :handlers [{:type :gravity.type/string :name 'error :body (local 'error)}]})
        functions {'identity {:arity 1 :params '[problem] :instructions [(local 'problem)]}
                   'relay {:arity 1 :params '[clash] :instructions [(call 'identity [(local 'clash)])]}
                   'pair {:arity 2 :params '[left right] :instructions [(say "callee" (local 'right))]}
                   'boom {:arity 1 :params '[problem] :instructions [(escape (local 'problem))]}
                   'rethrow {:arity 1 :params '[problem] :instructions [{:op :try :protected (escape (local 'problem))
                                                                        :handlers [{:type :gravity.type/string :name 'error :body (escape (local 'error))}]}]}}
        evaluate (fn [instruction] (invoke 'p15-s23-runtime-evaluate-in-context instruction {'clash :caller-only} "" functions ['main]))
        payload (String. "original")]
    (doseq [name '[identity relay]]
      (let [result (evaluate (call name [(literal payload)]))]
        (is (identical? payload (first result))) (is (= ["" :normal] (subvec result 1)))))
    (doseq [value [nil false {:op :throw :value payload} [payload "" :thrown] (ex-info "inert" {})]]
      (doseq [[name control] [['identity :normal] ['boom :thrown]]]
        (let [result (evaluate (call name [(literal value)]))]
          (is (identical? value (first result))) (is (= ["" control] (subvec result 1))))))
    (is (= [payload "first\nsecond\ncallee\n" :normal]
           (evaluate (call 'pair [(say "first" (literal :first)) (say "second" (literal payload))]))))
    (is (= [payload "first\n" :thrown]
           (evaluate (call 'pair [(say "first" (escape (literal payload))) (say "unreachable" (literal :second))]))))
    (doseq [name '[boom rethrow]]
      (let [result (evaluate (recover (call name [(literal payload)])))]
        (is (identical? payload (first result))) (is (= ["" :normal] (subvec result 1)))))
    (let [result (invoke 'p15-s23-runtime-evaluate-in-context (call 'identity [(literal payload)]) {'clash :caller-only} ""
                        (assoc-in functions ['identity :instructions] [(local 'clash)]) ['main])]
      (is (= ["" :rejected] (subvec result 1))))
    (doseq [[target active table args]
            [['missing ['main] functions [(say "unreachable" (literal payload))]]
             ['identity ['main] (assoc-in functions ['identity :params] 42) [(say "unreachable" (literal payload))]]
             ['identity ['main] (assoc-in functions ['identity :params] '[x x]) [(say "unreachable" (literal payload))]]
             ['identity ['main] (assoc-in functions ['identity :params] [42]) [(say "unreachable" (literal payload))]]
             ['identity ['main] (assoc-in functions ['identity :arity] 2) [(say "unreachable" (literal payload))]]
             ['identity ['main] (assoc-in functions ['identity :instructions] [42]) [(say "unreachable" (literal payload))]]
             ['identity ['identity] functions [(say "unreachable" (literal payload))]]
             ['identity (vec (repeat 256 'frame)) functions [(say "unreachable" (literal payload))]]
             ['identity ['main] functions []]
             ['identity ['main] functions 42]]]
      (let [result (invoke 'p15-s23-runtime-evaluate-in-context (call target args) {} "" table active)]
        (is (= ["" :rejected] (subvec result 1))) (is (= "P15S23X002" (:diagnostic (first result))))))
    (is (= :rejected (get (invoke 'p15-s23-runtime-evaluate-closed-instruction (call 'identity [(say "unreachable" (literal payload))]) {} "") 2)))
    (let [table (assoc functions 'cycle {:arity 0 :params [] :instructions [(call 'cycle [])]})
          result (invoke 'p15-s23-runtime-evaluate-in-context (call 'cycle []) {} "" table ['main])]
      (is (= ["" :rejected] (subvec result 1))))
    (let [table (assoc functions 'main {:arity 0 :params [] :instructions [(call 'relay [(literal payload)])]})
          result (invoke 'p15-s23-runtime-execute-closed-plan {:entrypoint 'main :functions table})]
      (is (= :complete (:status result))) (is (identical? payload (:entrypoint-result result)))
      (is (= #{:artifact :entrypoint :stdout :entrypoint-result :status :clojure-seed-boundary? :self-hosted?} (set (keys result)))))
    (let [fatal (AssertionError. "fatal primitive") value (proxy [Object] [] (toString [] (throw fatal)))
          observed (seed-throw-test-capture #(evaluate (call 'identity [{:op :builtin-call :function 'str :args [(literal value)]}])))]
      (is (identical? fatal observed)))))

(deftest current-gravity-direct-call-authenticated-composition
  (let [root (loop [directory (.getParentFile (io/file (module-fixture "seed-library" ".gravity")))]
               (if (.isFile (io/file directory "deps.edn")) directory (recur (.getParentFile directory))))
        {:keys [plan parent-source]} (typed-catch-runtime-projection)
        emitter-path (bootstrap/p15-s23-stage2-compiler-artifact-source-path)
        emitter-text (slurp emitter-path) emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! emitter-path :jvm))
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
        original bootstrap/p15-s23-stage2-runtime-execute-function
        transport (module-private 'p15-s23-stage2-runtime-source-throw!) transports (atom 0) original-transport (var-get transport)
        verifications (atom 0)
        operation (fn [thunk]
                    (let [before @verifications
                          result (with-redefs [bootstrap/p15-s23-stage2-runtime-execute-function
                                               (fn [runtime plan function args]
                                                 (when (= 'sh08-verify-function-type-result function) (swap! verifications inc))
                                                 (original runtime plan function args))] (thunk))]
                      (is (= 1 (- @verifications before))) result))]
    (is (= (str "sha256:" (bootstrap/sha256-hex (slurp (:path parent-source)))) (:sha256 parent-source)))
    (doseq [extension [".gravity" ".qst"]]
      (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/local-runtime-direct-call" extension)
            source-path (.getPath (io/file root relative)) p (fresh relative) input (select-keys p [:request :typed :verification])
            macro-artifact (get-in input [:request :b47-context :authenticated-wrapper :sh06-resolution-artifact :sh05-macro-artifact])
            original-forms (get-in macro-artifact [:gravity-macro-boundary :authenticated-sh04-artifact :c2-reader-artifact :parsed-semantic-values])
            module (assoc (bootstrap/parse-module source-path original-forms) :forms (:expanded-forms macro-artifact))
            definitions (bootstrap/stage0-function-table module) module (assoc module :function-table definitions)
            assembled (operation #(bootstrap/p15-s23-stage2-assemble-plan-products emitter module (vec (sort-by key definitions)) input))
            functions (:functions assembled) payload (String. "original")
            execute (fn [name env]
                      (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
                        #(original runtime plan 'p15-s23-runtime-evaluate-in-context
                           [(first (get-in functions [name :instructions])) env "" functions [name]])))]
        (is (= :accepted (get-in input [:typed :status]))) (is (= :passed (get-in input [:verification :status])))
        (is (= 13 (count functions)))
        (is (= (count (get-in input [:request :canonical-core-artifact :calls])) (get-in assembled [:instruction-summary :function-call])))
        (is (= 'identity (get-in functions ['qualified :instructions 0 :function])))
        (doseq [name (keys definitions)]
          (let [compiled (operation #(bootstrap/p15-s23-stage2-compile-function emitter module (get definitions name) input))]
            (is (= compiled (get functions name)))
            (is (= :pending-c8-and-profile-authority (get-in compiled [:typed-lowering-provenance :effect-legality])))
            (is (= (str "sha256:" (bootstrap/sha256-hex emitter-text)) (get-in compiled [:typed-lowering-provenance :compiler-sources :lowering :source :sha256])))))
        (doseq [[name env control expected]
                [['identity {'problem payload} :normal payload] ['relay {'clash payload 'problem :caller-only} :normal payload]
                 ['aliased {'problem payload 'clash :caller-only} :normal payload] ['qualified {'problem payload} :normal payload]
                 ['thrower {'problem payload} :thrown payload] ['recover {'problem payload} :normal payload]
                 ['rethrow {'problem payload} :thrown payload] ['outer {'problem payload} :normal payload]
                 ['never-arg {'problem payload} :normal payload] ['first-arg {'problem payload} :normal payload]
                 ['bool-callee {'value payload} :thrown false]]]
          (let [result (execute name env)] (is (identical? expected (first result))) (is (= ["" control] (subvec result 1)))))
        (with-redefs-fn {transport (fn [& args] (swap! transports inc) (apply original-transport args))}
          #(let [result (original runtime plan 'p15-s23-runtime-execute-closed-plan [assembled])]
             (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main :stdout ""
                     :entrypoint-result "recovered" :status :complete :clojure-seed-boundary? true :self-hosted? false} result))))
        (doseq [changed [(assoc-in input [:typed :call-type-facts 0 :result-type] :gravity.type/bool)
                         (assoc-in input [:request :canonical-core-artifact :calls 0 :operator-binding-id] :wrong)
                         (assoc-in input [:request :canonical-core-artifact :call-edges 0 :callee-function-syntax-id] :wrong)]]
          (let [failure (operation #(diagnostic (fn [] (bootstrap/p15-s23-stage2-compile-function emitter module (get definitions 'main) changed))))]
            (is (= "P15S23Q002" (:id failure))))))
      (let [source-path (.getPath (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-call-unknown" extension)))]
        (is (= "C5-UNRESOLVED" (:id (diagnostic #(bootstrap/compiler-c5-resolution-file-artifact source-path))))))
      (doseq [[suffix rule reason] [["arity" "C7-TYPE-MISMATCH" :call-arity-mismatch]
                                    ["type" "C7-TYPE-MISMATCH" :call-argument-type-mismatch]
                                    ["nonfunction" "C7-ANNOTATION" :unsupported-nonlocal-call]]]
        (let [relative (str "bootstrap/clojure/fixtures/self-hosting/sh-08/rejected/local-runtime-call-" suffix extension)
              p (fresh relative) typed (:typed p)]
          (is (= :rejected (:status typed)))
          (is (= rule (get-in typed [:diagnostics 0 :rule]))) (is (= reason (get-in typed [:diagnostics 0 :reason])))
          (is (= :type-checking (get-in typed [:diagnostics 0 :stage])))
          (is (= :passed (get-in p [:verification :status]))))))
    (is (= 0 @transports))))

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
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
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
        fresh (ns-resolve 'gravity.self-hosting.sh09-body-effect-test 'fresh-type-products)
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
            (let [compiled (compile-function 'call-catch)]
              (is (= :function-call (get-in compiled [:instructions 0 :protected :op])))
              (is (= 'partial (get-in compiled [:instructions 0 :protected :function])))))
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
        source-path (.getPath (io/file root "bootstrap/gravity/src/gravity/runtime.gravity"))
        compatibility-path (.getPath (io/file root "bootstrap/gravity/p15_s23/runtime.gravity"))
        compatibility-source {:path compatibility-path :sha256 (str "sha256:" (bootstrap/sha256-hex (slurp compatibility-path)))}
        text (slurp source-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! fixture-path :jvm))
        parent-forms (bootstrap/read-forms source-path text)
        arities {'p15-s23-runtime-evaluate-arguments 4 'p15-s23-runtime-println-output 2
                 'p15-s23-runtime-evaluate-arguments-in-context 7 'p15-s23-runtime-evaluate-bindings-in-context 6 'p15-s23-runtime-evaluate-sequence-in-context 6
                 'p15-s23-runtime-select-handler-in-context 6 'p15-s23-runtime-evaluate-in-context 5 'p15-s23-runtime-call-target-valid? 3 'p15-s23-runtime-evaluate-call 6
                 'p15-s23-runtime-function-valid? 2 'p15-s23-runtime-activate-function 5
                 'p15-s23-runtime-evaluate-with-targets 6 'p15-s23-runtime-loop-valid? 2 'p15-s23-runtime-recur-valid? 2 'p15-s23-runtime-evaluate-loop 6
                 'p15-s23-runtime-typed-handlers-valid? 1 'p15-s23-runtime-atomic-error-match? 2 'p15-s23-runtime-select-typed-handler 3
                 'p15-s23-runtime-pattern-atom? 2 'p15-s23-runtime-structural-match-valid? 1
                 'p15-s23-runtime-match-pattern 3 'p15-s23-runtime-evaluate-structural-match 6
                 'p15-s23-runtime-evaluate-bindings 3 'p15-s23-runtime-evaluate-sequence 3
                 'p15-s23-runtime-evaluate-closed-instruction 3 'p15-s23-runtime-execute-closed-plan 1}
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? arities (second %))) parent-forms)
        compiled (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source-path text)
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
    ;; Compile the entire implementation file and retain the separate compatibility source identity.
    (is (= text (slurp source-path)))
    (is (= 26 (count selected) (count (set (map second selected)))))
    (is (= arities (into {} (map #(vector (second %) (count (nth % 2))) selected))))
    (is (= selected (vec (rest parent-forms))))
    (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in compiled [:source :sha256])))
    (is (not= (:path compatibility-source) source-path))
    (is (= (:sha256 compatibility-source) (str "sha256:" (bootstrap/sha256-hex (slurp compatibility-path)))))
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

(deftest compiler-module-whole-runtime-data-development-kernel
  (let [path (str/replace (module-fixture "seed-library" ".gravity")
                          "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                          "gravity/p15_s23/runtime.gravity")
        text (slurp path) forms (bootstrap/read-forms path text)
        definitions (filterv #(= 'def (first %)) forms)
        compatibility-functions (filterv #(= 'defn (first %)) forms)
        implementation-path (str/replace path "gravity/p15_s23/runtime.gravity" "gravity/src/gravity/runtime.gravity")
        implementation-text (slurp implementation-path)
        implementation-forms (bootstrap/read-forms implementation-path implementation-text)
        implementation-functions (vec (rest implementation-forms))
        functions (vec (concat (take 4 compatibility-functions) implementation-functions (drop 8 compatibility-functions)))
        preserved-functions (fn [forms] (filterv #(not (contains? '#{p15-s23-runtime-evaluate-with-targets
                                           p15-s23-runtime-pattern-atom? p15-s23-runtime-structural-match-valid?
                                           p15-s23-runtime-match-pattern p15-s23-runtime-evaluate-structural-match} (second %))) forms))
        underlying (fn [value] (if (and (seq? value) (= 'quote (first value))) (second value) value))
        data (mapv #(vector (second %) (underlying (nth % 2))) definitions)
        digest #(bootstrap/sha256-hex (pr-str (bootstrap/c-backend-canonical-value %)))
        arities '{main 0 p15-s23-runtime-activate-function 5 p15-s23-runtime-atomic-error-match? 2
                  p15-s23-runtime-b51-machine-action-shape? 1 p15-s23-runtime-b51-machine-frame-shape? 1
                  p15-s23-runtime-b51-machine-state 4 p15-s23-runtime-call-target-valid? 3
                  p15-s23-runtime-concat 2 p15-s23-runtime-evaluate-arguments 4
                  p15-s23-runtime-evaluate-arguments-in-context 7 p15-s23-runtime-evaluate-bindings 3
                  p15-s23-runtime-evaluate-bindings-in-context 6 p15-s23-runtime-evaluate-call 6
                  p15-s23-runtime-evaluate-closed-instruction 3 p15-s23-runtime-evaluate-in-context 5
                  p15-s23-runtime-evaluate-loop 6 p15-s23-runtime-evaluate-sequence 3
                  p15-s23-runtime-evaluate-sequence-in-context 6 p15-s23-runtime-evaluate-with-targets 6
                  p15-s23-runtime-execute-closed-plan 1 p15-s23-runtime-format-value 1
                  p15-s23-runtime-function-valid? 2 p15-s23-runtime-loop-valid? 2
                  p15-s23-runtime-println-output 2 p15-s23-runtime-println-two 2 p15-s23-runtime-println-value 1
                  p15-s23-runtime-recur-valid? 2 p15-s23-runtime-select-handler-in-context 6
                  p15-s23-runtime-select-typed-handler 3 p15-s23-runtime-typed-handlers-valid? 1
                  p15-s23-runtime-pattern-atom? 2 p15-s23-runtime-structural-match-valid? 1
                  p15-s23-runtime-match-pattern 3 p15-s23-runtime-evaluate-structural-match 6}
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        literals-var (module-private 'p15-s23-stage2-module-literals)
        original-literals @literals-var observed (atom [])
        compiled (with-redefs-fn {literals-var (fn [forms]
                                               (let [result (original-literals forms)]
                                                 (swap! observed conj result) result))}
                   #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter implementation-path implementation-text))
        invoke (fn [function & args]
                 (bootstrap/p15-s23-stage2-runtime-execute-function runtime compiled function (vec args)))
        lit (fn [value] {:op :literal :value value}) local (fn [name] {:op :local :name name})
        branch (fn [test then else] {:op :if :test test :then then :else else})
        again (fn [target args] {:op :recur :target target :args args})
        escape (fn [value] {:op :throw :value value})
        say (fn [label value] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} value]})
        evaluate #(invoke 'p15-s23-runtime-evaluate-closed-instruction % {} "")
        execute #(invoke 'p15-s23-runtime-execute-closed-plan
                         {:entrypoint 'main :functions {'main {:arity 0 :params [] :instructions [%]}}})
        payload (String. "original runtime payload")]
    ;; C/D's complete-body conservation result is historical. F intentionally
    ;; changes evaluate-with-targets; L adds four private structural helpers.
    ;; These historical digests cover the same other 21/29 original bodies.
    ;; Catalog DATA and current complete source identities remain checked.
    (is (= 23 (count data)))
    (is (= "68697377c7914c2442f2c6ae916984ddfebfb3fd77aa4b3ba3b385ea2db1bfe0" (digest data)))
    (is (= 29 (count (preserved-functions functions))))
    (is (= "680b2af4243eacd44211249930dd521728fc0cf625957817c617599519831984" (digest (preserved-functions functions))))
    (is (= 34 (count functions)))
    (is (= 12 (count compatibility-functions)))
    (is (= 26 (count implementation-functions) (count (:functions compiled))))
    (is (= 21 (count (preserved-functions implementation-functions))))
    (is (= "d78ce93eb159ce90c31d23c29d588744b336c46301d21b593dc06240ac2ab8c1" (digest (preserved-functions implementation-functions))))
    (is (= arities (into {} (map #(vector (second %) (count (nth % 2))) functions))))
    (is (= (select-keys arities (map second implementation-functions))
           (into {} (map (fn [[name function]] [name (:arity function)]) (:functions compiled)))))
    (is (= 18 (count (filter #(seq? (nth % 2)) definitions))))
    (is (= '#{p15-s23-reference-runtime-service-manifest p15-s23-reference-managed-runtime-adapter
              p15-s23-reference-memory-provider-manifest p15-s23-reference-runtime-failure-policy
              p15-s23-runtime-b51-machine-contract}
           (set (map second (filter #(map? (nth % 2)) definitions)))))
    (is (= (into (sorted-map) data) (original-literals forms)))
    (is (= [{}] @observed))
    (is (= (str "sha256:" (bootstrap/sha256-hex implementation-text)) (get-in compiled [:source :sha256])))
    (is (not= (bootstrap/sha256-hex text) (bootstrap/sha256-hex implementation-text)))
    (is (= 'gravity.runtime (get-in compiled [:module :module])))
    (is (= [:hosted :jvm] [(get-in compiled [:module :profile]) (get-in compiled [:module :target])]))
    (is (not (contains? compiled :literal-data)))
    (is (= {:artifact :gravity/p15-s23-runtime-closed-plan-execution-record :entrypoint 'main
            :stdout "normal\n" :entrypoint-result 7 :status :complete :clojure-seed-boundary? true :self-hosted? false}
           (execute (say "normal" (lit 7)))))
    (let [loop-instruction {:op :loop :target "once"
                            :bindings [{:name 'again :expr (lit true)} {:name 'value :expr (say "init" (lit 0))}]
                            :body [(branch (local 'again)
                                     (again "once" [(lit false) (say "step" (lit 7))])
                                     (say "final" (local 'value)))]}]
      (is (= [7 "init\nstep\nfinal\n" :normal] (evaluate loop-instruction))))
    (let [right (String. "right")
          function {:recur-target "swap" :arity 3 :params '[flag left right]
                    :instructions [(branch (local 'flag)
                                     (again "swap" [(lit false) (say "right" (local 'right)) (say "left" (local 'left))])
                                     (local 'right))]}
          result (invoke 'p15-s23-runtime-activate-function function [true payload right] "" {} ['main])]
      (is (= [payload "right\nleft\n" :normal] result))
      (is (identical? payload (first result))))
    (let [thrown (execute {:op :do :body [(say "before" (lit nil)) (escape (lit payload)) (say "later" (lit nil))]})
          handler (fn [body] {:type :gravity.type/string :name 'error :body body})
          caught (evaluate {:op :try :protected (escape (lit payload)) :handlers [(handler (local 'error))]})
          rethrown (evaluate {:op :try :protected (escape (lit payload)) :handlers [(handler (escape (local 'error)))]})
          rejected (execute {:op :do :body [(say "before" (lit nil)) {:op :unknown} (say "later" (lit nil))]})]
      (is (= :thrown (:status thrown))) (is (identical? payload (:thrown-value thrown)))
      (is (= [payload "" :normal] caught)) (is (identical? payload (first caught)))
      (is (= [payload "" :thrown] rethrown)) (is (identical? payload (first rethrown)))
      (is (= :rejected (:status rejected)))
      (is (= "P15S23X002" (get-in rejected [:diagnostic :diagnostic])))
      (is (= "before\n" (:stdout thrown) (:stdout rejected))))
    (let [data '{:forms [(launch unknown) (quote untouched)]}
          result (evaluate {:op :quote :value data})]
      (is (= [data "" :normal] result)))
    (let [unquoted (str/replace-first text "(def p15-s23-reference-runtime-contract\n  '{"
                                    "(def p15-s23-reference-runtime-contract\n  {")]
      (is (not= text unquoted))
      (is (= :unsupported-literal-data
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path unquoted)) [:facts :reason]))))))

(deftest compiler-module-runtime-extraction-authenticated-composition
  (let [caller-path (str/replace (module-fixture "seed-library" ".gravity")
                                 "clojure/fixtures/self-hosting/sh-01/modules/seed-library.gravity"
                                 "gravity/p15_s23/runtime.gravity")
        leaf-path (str/replace caller-path "gravity/p15_s23/runtime.gravity" "gravity/src/gravity/runtime.gravity")
        caller-text (slurp caller-path) leaf-text (slurp leaf-path)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
        leaf (bootstrap/sh06-resolution-file-artifact leaf-path)
        c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [leaf]}
        caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
        dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter leaf-path leaf-text)
        inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
        bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path caller-text inputs)
        root-entry (first (filter #(= (:artifact-id caller) (:module-id %)) (:modules bundle)))
        leaf-entry (first (filter #(= (:artifact-id leaf) (:module-id %)) (:modules bundle)))
        forwarders '{p15-s23-runtime-execute-closed-plan [plan]
                     p15-s23-runtime-evaluate-closed-instruction [instruction environment output]
                     p15-s23-runtime-evaluate-in-context [instruction environment output functions path]
                     p15-s23-runtime-activate-function [function values output functions path]}
        invoke (fn [function & args] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle function (vec args)))
        lit (fn [value] {:op :literal :value value}) local (fn [name] {:op :local :name name})
        branch (fn [test then else] {:op :if :test test :then then :else else})
        again (fn [target args] {:op :recur :target target :args args})
        escape (fn [value] {:op :throw :value value})
        say (fn [label value] {:op :do :body [{:op :println :args [(lit label)] :effect :io/write :capability :io/stdout} value]})
        payload (String. "original extracted runtime payload")
        swap {:recur-target "swap" :arity 3 :params '[flag left right]
              :instructions [(branch (local 'flag)
                               (again "swap" [(lit false) (local 'right) (local 'left)]) (local 'right))]}
        loop-instruction {:op :loop :target "once"
                          :bindings [{:name 'again :expr (lit true)} {:name 'value :expr (say "init" (lit 0))}]
                          :body [(branch (local 'again) (again "once" [(lit false) (say "step" (lit 7))])
                                   (say "final" (local 'value)))]}]
    (is (= :accepted (:status leaf) (:status caller)))
    (is (= [12 22] [(count (get-in root-entry [:plan :functions])) (count (get-in leaf-entry [:plan :functions]))]))
    (doseq [[entry text namespace] [[root-entry caller-text 'gravity.bootstrap.p15-s23.runtime] [leaf-entry leaf-text 'gravity.runtime]]]
      (is (= (str "sha256:" (bootstrap/sha256-hex text)) (get-in entry [:plan :source :sha256])))
      (is (= namespace (get-in entry [:plan :module :module])))
      (is (= [:hosted :jvm] [(get-in entry [:plan :module :profile]) (get-in entry [:plan :module :target])]))
      (is (= namespace (get-in entry [:resolution-artifact :gravity-resolution-boundary :authenticated-resolution-request :module :namespace]))))
    (is (= (set (keys forwarders)) (set (map :name (:exports leaf-entry)))))
    (is (= 18 (count (filter #(and (= :namespace (:binding-class %)) (= :function (:kind %)) (= :private (:visibility %)))
                               (get-in leaf [:binding-table :bindings])))))
    (is (= [] (get-in leaf [:gravity-resolution-boundary :authenticated-resolution-request :imports])))
    (is (= 4 (count (:routes bundle))))
    (doseq [route (:routes bundle)]
      (is (= (:artifact-id caller) (:caller-module-id route)))
      (is (= (:artifact-id leaf) (:callee-module-id route) (get-in route [:origin :ordinary-module-artifact-id])))
      (is (= (:caller-function route) (:callee-function route)))
      (is (= (count (get forwarders (:caller-function route))) (:arity route))))
    (doseq [form (filter #(and (= 'defn (first %)) (contains? forwarders (second %)))
                         (bootstrap/read-forms caller-path caller-text))]
      (let [name (second form) params (get forwarders name)]
        (is (= (list 'defn name params (apply list (symbol "runtime" (str name)) params)) form))))
    (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
    (is (= :not-checked (get-in bundle [:boundary :effects])))
    (is (= "gravity-stage2-runtime" (invoke 'main)))
    (let [record (invoke 'p15-s23-runtime-execute-closed-plan
                         {:entrypoint 'main :functions {'main {:arity 0 :params [] :instructions [loop-instruction]}}})]
      (is (= :complete (:status record)))
      (is (= 7 (:entrypoint-result record)))
      (is (= "init\nstep\nfinal\n" (:stdout record))))
    (doseq [[handler-body control] [[(local 'error) :normal] [(escape (local 'error)) :thrown]]]
      (let [result (invoke 'p15-s23-runtime-evaluate-closed-instruction
                           {:op :try :protected (escape (lit payload))
                            :handlers [{:type :gravity.type/string :name 'error :body handler-body}]} {} "")]
        (is (= [payload "" control] result)) (is (identical? payload (first result)))))
    (let [result (invoke 'p15-s23-runtime-evaluate-in-context
                         {:op :function-call :function 'swap :args [(lit true) (lit payload) (lit :right)]}
                         {} "" {'swap swap} ['main])]
      (is (= [payload "" :normal] result)) (is (identical? payload (first result))))
    (let [result (invoke 'p15-s23-runtime-activate-function swap [true payload :right] "" {} ['main])]
      (is (= [payload "" :normal] result)) (is (identical? payload (first result))))
    (doseq [[name rule] [["p15-s23-runtime-evaluate-with-targets" "C5-PRIVATE"] ["missing-runtime-function" "C5-UNRESOLVED"]]]
      (is (= rule (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                                    caller-path (str/replace caller-text "runtime/p15-s23-runtime-execute-closed-plan" (str "runtime/" name)) c5-inputs))))))
    (is (= :source-emitter-or-dependency-set
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path (str caller-text " ") inputs)) [:facts :reason])))
    (let [changed (assoc-in dependency [:functions 'p15-s23-runtime-activate-function :arity] 4)
          forged (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
      (is (= :independent-raw-plan-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path caller-text (assoc inputs :dependency-plans [forged]))) [:facts :reason]))))
    (let [forged (assoc-in bundle [:routes 0 :origin :ordinary-module-artifact-id] (:artifact-id caller))
          identity (assoc (:identity-input forged) :routes (:routes forged))
          forged (assoc forged :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'main [])) [:facts :reason]))))))

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

(deftest compiler-module-profile-source-development-kernel
  ;; Normalized source records and mocked front doors; not C5 authentication evidence.
  (let [expanded '(def identity (fn [value] value))
        syntax {:syntax/id :definition :form expanded}
        make-artifact
        (fn [profile target]
          {:kind :gravity/sh06-resolution-artifact :status :accepted :artifact-id :module
           :provenance {:source-path "<profile-kernel>"}
           :gravity-resolution-boundary
           {:owner :gravity-source
            :authenticated-resolution-request
            {:artifact :gravity/sh06-ordinary-module-resolution-request :schema-version 2
             :module {:namespace 'example.library :package 'example :profile profile :target target
                      :exports '[identity] :c4-artifact-id :c4}}}
           :sh05-macro-artifact
           {:expanded-syntax-stream [syntax]
            :gravity-macro-boundary {:authenticated-sh04-artifact
                                    {:c2-reader-artifact {:parsed-semantic-values
                                                         '[(ns example.library) (defn identity [value] value)]}}}}
           :binding-table {:bindings [{:binding-class :namespace :name 'identity :kind :function
                                       :namespace 'example.library :package 'example :visibility :public
                                       :profile-set [profile] :target-set [target]
                                       :definition-syntax-id :definition :definition-artifact-id :c4}]}})
        verified (atom []) reads (atom 0)
        overrides {(module-private 'sh06-resolution-artifact-verification)
                   (fn [artifact] (swap! verified conj artifact) {:status :passed})
                   (module-private 'c2-reader-artifact-source-text)
                   (fn [& _] (swap! reads inc) "normalized")
                   (module-private 'macro-source-artifact)
                   (fn [& _] {:module {:module 'example.library} :expanded-forms [expanded]})}
        source #(with-redefs-fn overrides (fn [] (bootstrap/p15-s23-stage2-module-source %)))
        reason #(get-in (diagnostic (fn [] (source %))) [:facts :reason])]
    (doseq [profile [:meta :hosted]]
      (let [artifact (make-artifact profile :jvm) result (source artifact)]
        (is (= profile (get-in result [:module :profile])))
        (is (identical? artifact (:artifact result)))
        (is (= '[identity] (vec (keys (:functions result)))))))
    (doseq [[profile target] [[:core :jvm] [:hosted :wasm] [:meta :js]]]
      (is (= :source-or-expansion-mismatch (reason (make-artifact profile target)))))
    (doseq [[key value] [[:namespace 'example.forged] [:package 'other] [:profile-set [:meta]]
                         [:target-set [:wasm]] [:visibility :private] [:definition-artifact-id :forged]]]
      (is (= :declaration-binding-mismatch
             (reason (assoc-in (make-artifact :hosted :jvm) [:binding-table :bindings 0 key] value)))))
    (is (= :declaration-syntax
           (reason (assoc-in (make-artifact :hosted :jvm)
                             [:binding-table :bindings 0 :definition-syntax-id] :missing))))
    (is (= 12 (count @verified) @reads))
    (reset! reads 0)
    (with-redefs-fn (assoc overrides (module-private 'sh06-resolution-artifact-verification)
                         (fn [_] {:status :failed}))
      #(is (= :verified-ordinary-c5-required
              (get-in (diagnostic (fn [] (bootstrap/p15-s23-stage2-module-source
                                         (make-artifact :hosted :jvm)))) [:facts :reason]))))
    (is (zero? @reads))))

(deftest compiler-module-profile-origin-development-kernel
  ;; Exercise the origin join itself with normalized DATA, independently of C5 construction.
  (let [make-case
        (fn [profile target]
          (let [original {:binding-id :original :binding-class :namespace :name 'identity :kind :function
                          :namespace 'example.library :package 'example :visibility :public
                          :profile-set [profile] :target-set [target] :type-ref :type
                          :effects [] :capabilities [] :safety :safe
                          :definition-artifact-id :c4 :definition-syntax-id :syntax}
                callee {:module-id :library
                        :module {:namespace 'example.library :package 'example :exports '[identity]
                                 :profile profile :target target}
                        :artifact {:binding-table {:bindings [original]}
                                   :gravity-resolution-boundary {:resolved-analysis {:artifact-id :analysis}}}}
                imported (assoc original :binding-class :import :definition-artifact-id :library
                                :semantic-span
                                {:ordinary-module-artifact-id :library :origin-binding-id :original
                                 :origin-analysis-id :analysis :origin-definition-artifact-id :c4
                                 :origin-definition-syntax-id :syntax
                                 :origin-definition (select-keys original [:name :kind :namespace :package :visibility
                                                                          :profile-set :target-set :type-ref :effects
                                                                          :capabilities :safety])})]
            [{:module {:profile profile :target target}} imported callee original]))
        origin (module-private 'p15-s23-stage2-module-origin!)
        [caller imported callee _] (make-case :hosted :jvm)
        reason (fn [c b m] (get-in (diagnostic #(origin c b m)) [:facts :reason]))]
    (doseq [profile [:meta :hosted]]
      (let [[c b m original] (make-case profile :jvm)]
        (is (identical? original (origin c b m)))))
    (doseq [[profile target] [[:core :jvm] [:hosted :wasm] [:meta :js]]]
      (let [[c b m] (make-case profile target)]
        (is (= :imported-function-origin (reason c b m)))))
    (doseq [[path value] [[[:profile-set] [:meta]] [[:target-set] [:wasm]]
                          [[:namespace] 'example.forged] [[:package] 'other]
                          [[:visibility] :private] [[:kind] :var]
                          [[:semantic-span :ordinary-module-artifact-id] :wrong]
                          [[:semantic-span :origin-analysis-id] :wrong]
                          [[:semantic-span :origin-definition-artifact-id] :wrong]
                          [[:semantic-span :origin-definition-syntax-id] :wrong]
                          [[:semantic-span :origin-definition :capabilities] [:io]]]]
      (is (= :imported-function-origin (reason caller (assoc-in imported path value) callee))))
    (doseq [changed [(assoc-in callee [:module :exports] [])
                     (assoc-in callee [:module :namespace] 'example.forged)
                     (assoc-in callee [:module :package] 'other)
                     (assoc-in callee [:artifact :binding-table :bindings 0 :visibility] :private)]]
      (is (= :imported-function-origin (reason caller imported changed))))
    (is (= :original-binding (reason caller (assoc-in imported [:semantic-span :origin-binding-id] :missing) callee)))
    (is (= :imported-function-origin (reason (assoc-in caller [:module :profile] :meta) imported callee)))))

(deftest compiler-module-hosted-authenticated-paired-composition
  ;; Finite ordinary C5 construction; public execution reconstructs every bundle afresh.
  (let [cases
        (mapv
         (fn [[profile extension]]
           (let [prefix (if (= :hosted profile) "hosted-seed" "seed")
                 library-path (module-fixture (str prefix "-library") extension)
                 caller-path (module-fixture (str prefix "-consumer") extension)
                 text (slurp caller-path)
                 emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! caller-path :jvm))
                 library (bootstrap/sh06-resolution-file-artifact library-path)
                 c5-inputs {:artifact :gravity/sh06-module-inputs :schema-version 1 :module-artifacts [library]}
                 caller (bootstrap/compiler-c5-resolution-file-artifact caller-path c5-inputs)
                 dependency (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter library-path (slurp library-path))
                 inputs {:caller-resolution-artifact caller :dependency-plans [dependency]}
                 bundle (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter caller-path text inputs)
                 value (String. "module identity")]
             (is (= :accepted (:status library) (:status caller)))
             (is (= [profile profile]
                    (mapv #(get-in % [:gravity-resolution-boundary :authenticated-resolution-request :module :profile])
                          [library caller])))
             (is (= :development-unadmitted (get-in bundle [:boundary :revision-status])))
             (is (= :not-checked (get-in bundle [:boundary :effects])))
             (is (= 2 (count (:modules bundle))))
             (is (= [profile profile] (mapv #(get-in % [:plan :module :profile]) (:modules bundle))))
             (is (= [profile profile] (mapv #(get-in % [:module :profile]) (get-in bundle [:identity-input :modules]))))
             (is (every? #(every? (fn [binding] (= [profile] (:profile-set binding))) (:exports %)) (:modules bundle)))
             (is (= (:artifact-id library) (get-in bundle [:routes 0 :callee-module-id])))
             (if (= :hosted profile)
               (do
                 (is (identical? value (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [value])))
                 (is (= 41 (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [41])))
                 (is (= [:caller value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'identity [value])))
                 (is (= :private (:visibility (first (filter #(= 'relay (:name %))
                                                           (get-in caller [:binding-table :bindings])))))))
               (is (= [:callee value] (bootstrap/p15-s23-stage2-runtime-execute-function runtime bundle 'relay [value]))))
             {:profile profile :bundle bundle :caller-path caller-path :text text :emitter emitter
              :library library :caller caller :inputs inputs :c5-inputs c5-inputs :dependency dependency}))
         [[:hosted ".gravity"] [:hosted ".qst"] [:meta ".gravity"]])
        {:keys [bundle caller-path text emitter inputs c5-inputs dependency]} (first cases)]
    (is (= (get-in cases [0 :bundle :identity-input]) (get-in cases [1 :bundle :identity-input])))
    (doseq [[name rule] [["hidden" "C5-PRIVATE"] ["missing" "C5-UNRESOLVED"]]]
      (is (= rule (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                                    caller-path (str/replace text "library/identity" (str "library/" name)) c5-inputs))))))
    (doseq [{:keys [profile caller-path text c5-inputs]} [(first cases) (nth cases 2)]]
      (let [other (if (= profile :hosted) ":meta" ":hosted")]
        (is (= "C5-CROSS-PROFILE"
               (:id (diagnostic #(bootstrap/compiler-c5-resolution-source-artifact
                                  caller-path (str/replace text (str ":profile " profile) (str ":profile " other)) c5-inputs)))))))
    (is (= :source-emitter-or-dependency-set
           (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                 emitter caller-path (str text " ") inputs)) [:facts :reason])))
    (let [changed (assoc-in dependency [:functions 'identity :instructions] [{:op :literal :value :forged}])
          forged (assoc changed :plan-id (str "sha256:" (bootstrap/sha256-hex
                                                         (pr-str (bootstrap/c-backend-canonical-value (dissoc changed :plan-id))))))]
      (is (= :independent-raw-plan-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-compiler-artifact-plan
                                   emitter caller-path text (assoc inputs :dependency-plans [forged]))) [:facts :reason]))))
    (let [forged (assoc-in bundle [:routes 0 :callee-module-id] (:root-module-id bundle))
          identity (assoc (:identity-input forged) :routes (:routes forged))
          forged (assoc forged :identity-input identity :bundle-id (bootstrap/reader-canonical-hash identity))]
      (is (= :module-bundle-reconstruction-mismatch
             (get-in (diagnostic #(bootstrap/p15-s23-stage2-runtime-execute-function runtime forged 'relay [41])) [:facts :reason]))))))

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
