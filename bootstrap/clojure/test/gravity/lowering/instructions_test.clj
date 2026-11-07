(ns gravity.lowering.instructions-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]
            [gravity.digest :as digest]
            [gravity.lowering :as lowering]))

(declare expression-port collection-port let-port)
(def ^:private builtin-values #{'+ '> 'str 'pr-str 'get})
(defn- failure-port [id message facts] (throw (ex-info message (assoc facts :id id))))
(defn- ports []
  {:compile-expression #'expression-port :compile-collection #'collection-port
   :compile-let #'let-port :builtin-functions #'builtin-values :fail! #'failure-port})
(defn- expression-port [module locals form]
  (lowering/compile-native-expression (ports) module locals form))
(defn- collection-port [module locals form]
  (lowering/compile-native-collection #'expression-port module locals form))
(defn- let-port [module locals bindings body]
  (lowering/compile-native-let #'failure-port #'expression-port module locals bindings body))
(defn- summary-port [instruction]
  (lowering/native-instruction-summary #'summary-port instruction))
(def ^:private module {:source-path "native-lowering.gravity" :module 'native.lowering
                      :profile :hosted :target :jvm :effects #{:io/write}
                      :capabilities #{:io/stdout} :function-table {'helper {}}})
(defn- error-data [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(def ^:private source-header
  "(ns lowering.instructions (:profile :hosted) (:target :jvm) (:effects #{:io/write}) (:capabilities #{:io/stdout}) (:safety :safe))\n")
(defn- compile-source [path source]
  (is (nil? (var-get #'b/*authenticated-source-form-records*)))
  (let [expanded (b/macro-source-artifact path source)
        input (assoc (:module expanded) :forms (:expanded-forms expanded))]
    (b/stage0-compiled-core-plan path source input)))

(deftest complete-group-has-explicit-contracts
  (is (= 22 (count (ns-publics 'gravity.lowering))))
  (is (some #{'D0} (:governing-contracts (:gravity/namespace-contract (meta #'lowering/compile-native-expression)))))
  (doseq [operation [#'lowering/compile-native-expression #'lowering/compile-native-collection
                     #'lowering/compile-native-let #'lowering/compile-native-function
                     #'lowering/native-instruction-summary]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :stage0-native-instruction-lowering (:contract-boundary entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-certificates? contract))))))

(deftest expression-and-collection-branches-preserve-native-shapes
  (doseq [[form expected]
          [[nil {:op :literal :value nil}]
           [false {:op :literal :value false}]
           ['x {:op :local :name 'x}]
           ['(quote opaque) {:op :quote :value 'opaque}]
           ['(if x 1) {:op :if :test {:op :local :name 'x}
                       :then {:op :literal :value 1} :else {:op :literal :value nil}}]
           ['(helper x) {:op :function-call :function 'helper :args [{:op :local :name 'x}]}]
           ['(+ 1 2) {:op :builtin-call :function '+ :args [{:op :literal :value 1} {:op :literal :value 2}]}]
           [[2 1] {:op :vector-literal :items [{:op :literal :value 2} {:op :literal :value 1}]}]
           [#{2 1} {:op :set-literal :items [{:op :literal :value 1} {:op :literal :value 2}]}]
           [{:b 2 :a 1} {:op :map-literal :entries [{:key {:op :literal :value :a} :value {:op :literal :value 1}}
                                                  {:key {:op :literal :value :b} :value {:op :literal :value 2}}]}]]]
    (is (= expected (expression-port module #{'x} form)) (pr-str form)))
  (let [opaque (Object.) quoted (with-meta (list 'quote opaque) {:origin :retained})]
    (is (identical? opaque (:value (expression-port module #{} quoted))))
    (is (= {:op :literal :value opaque} (collection-port module #{} opaque)))))

(deftest lowering-refusals-keep-first-error-and-source
  (doseq [[form expected]
          [['absent "L2-UNKNOWN-SYMBOL"] ['(absent 1) "L2-UNKNOWN-CORE-FORM"]
           ['(absent missing) "L2-UNKNOWN-SYMBOL"]
           ['(host-reflect missing) "P4-HOST-REFLECTION"]
           ['(let [x] x) "L2-LET-BINDING"] ['(let [1 2] 3) "L2-LET-BINDING"]]]
    (let [data (error-data #(expression-port module #{} form))]
      (is (= expected (:id data)))
      (is (= (:source-path module) (get-in data [:source-span :source]))))))

(deftest let-initializers-see-prior-scope-and-retain-order
  (let [calls (atom [])]
    (with-redefs [expression-port (fn [_ scope form] (swap! calls conj [scope form]) {:value form})]
      (is (= {:op :let :bindings [{:name 'x :expr {:value 1}} {:name 'y :expr {:value 'x}}]
              :body [{:value 'y} {:value false}]}
             (let-port module #{'outer} '[x 1 y x] '[y false])))
      (is (= [[#{'outer} 1] [#{'outer 'x} 'x] [#{'outer 'x 'y} 'y]
              [#{'outer 'x 'y} false]] @calls))))
  (is (= "L2-UNKNOWN-SYMBOL" (:id (error-data #(expression-port module #{} '(let [x x] x)))))))

(deftest recursive-expression-vars-are-read-for-each-child
  (let [calls (atom []) old (fn [_ _ form]
                            (swap! calls conj [:old form])
                            (alter-var-root #'expression-port (constantly (fn [_ _ next-form]
                                                                          (swap! calls conj [:new next-form])
                                                                          {:new next-form})))
                            {:old form})]
    (with-redefs [expression-port old]
      (is (= {:op :vector-literal :items [{:old 1} {:new 2} {:new 3}]}
             (collection-port module #{} [1 2 3])))
      (is (= [[:old 1] [:new 2] [:new 3]] @calls))))
  (let [calls (atom [])]
    (with-redefs [expression-port (fn [_ _ form] (swap! calls conj form) {:value form})]
      (collection-port module #{} {:b 2 :a 1})
      (is (= [:a 1 :b 2] @calls))
      (reset! calls [])
      (collection-port module #{} #{3 1 2})
      (is (= [1 2 3] @calls)))))

(deftest builtin-catalog-is-read-after-argument-compilation
  (with-redefs [builtin-values #{}
                expression-port (fn [_ _ form]
                                  (alter-var-root #'builtin-values (constantly #{'late-builtin}))
                                  {:value form})]
    (is (= {:op :builtin-call :function 'late-builtin :args [{:value 1}]}
           (lowering/compile-native-expression (ports) module #{} '(late-builtin 1))))))

(deftest failure-function-is-selected-before-facts-and-exceptions-survive
  (let [sentinel (ex-info "original failure" {:opaque (Object.)}) calls (atom [])
        late (fn [& _] (swap! calls conj :late) :late)
        original (fn [& args] (swap! calls conj (vec args)) (throw sentinel))
        source-owner (reify clojure.lang.ILookup
                       (valAt [_ key]
                         (when (= :source-path key)
                           (alter-var-root #'failure-port (constantly late)) "selected.gravity"))
                       (valAt [_ key default]
                         (if (= :source-path key)
                           (do (alter-var-root #'failure-port (constantly late)) "selected.gravity")
                           default)))]
    (with-redefs [failure-port original]
      (is (identical? sentinel
                      (try (lowering/compile-native-expression (ports) source-owner #{} 'missing)
                           (catch clojure.lang.ExceptionInfo error error))))
      (is (= 1 (count @calls)))
      (is (identical? late (var-get #'failure-port)))
      (is (= "L2-UNKNOWN-SYMBOL" (ffirst @calls)))
      (is (= "selected.gravity" (get-in (first @calls) [2 :source-span :source])))))
  (let [sentinel (Exception. "argument failure")]
    (with-redefs [expression-port (fn [& _] (throw sentinel))]
      (is (identical? sentinel (try (collection-port module #{} [1]) (catch Exception error error)))))))

(deftest returning-failure-ports-preserve-existing-control-flow
  (let [calls (atom [])]
    (with-redefs [failure-port (fn [& args] (swap! calls conj args) :returned)]
      (is (= :returned (expression-port module #{} 'missing)))
      (is (= {:op :let :bindings [] :body []} (let-port module #{} '[unpaired] [])))
      (is (= ["L2-UNKNOWN-SYMBOL" "L2-LET-BINDING"] (mapv first @calls))))))

(deftest function-lowering-preserves-definition-metadata-and-bindings
  (let [opaque (Object.) definition {:name 'helper :params ['x] :body ['x 1] :arity 1 :extra opaque}
        result (lowering/compile-native-function #'expression-port (assoc module :exports ['helper]) definition)]
    (is (identical? opaque (:extra result)))
    (is (= (select-keys definition [:name :params :body :arity :extra])
           (select-keys result [:name :params :body :arity :extra])))
    (is (= [{:op :local :name 'x} {:op :literal :value 1}] (:instructions result)))
    (is (= {:name 'helper :kind :function :namespace 'native.lowering :profile :hosted
            :target :jvm :visibility :public :effects #{:io/write} :capabilities #{:io/stdout}}
           (:binding result)))
    (is (= :private (get-in (lowering/compile-native-function #'expression-port (assoc module :exports ['main]) definition)
                           [:binding :visibility])))
    (is (= :stage0-local (get-in (lowering/compile-native-function #'expression-port module definition)
                                [:binding :visibility])))))

(deftest summary-counts-all-opcodes-with-original-child-order
  (is (= {:builtin-call 2 :literal 2}
         (summary-port {:op :builtin-call :args [{:op :builtin-call :args [{:op :literal}]} {:op :literal}]})))
  (is (= {:loop 1 :literal 1 :throw 2 :recur 1 :local 1}
         (summary-port {:op :loop :bindings [{:expr {:op :literal}}]
                        :body [{:op :throw :value {:op :throw :value nil}}
                               {:op :recur :args [{:op :local}]}]})))
  (let [calls (atom [])]
    (with-redefs [summary-port (fn [instruction]
                                (swap! calls conj (:op instruction))
                                (alter-var-root #'summary-port (constantly (fn [_] {:new 1})))
                                {:old 1})]
      (is (= {:do 1 :old 2} (lowering/native-instruction-summary #'summary-port {:op :do :body [{:op :a} {:op :b}]})))
      (is (= [:a :b] @calls)))))

(deftest actual-source-plans-emit-execute-and-retain-identity
  (doseq [path ["examples/hello.gravity" "examples/hello.qst" "examples/core-app.gravity" "examples/core-app.qst"]]
    (testing path
      (let [text (slurp path :encoding "UTF-8") plan (compile-source path text)]
        (is (= plan (compile-source path text)))
        (is (= path (get-in plan [:source :path])))
        (is (= (str "sha256:" (digest/sha256-hex text)) (get-in plan [:source :sha256])))
        (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
        (is (= (if (.contains path "core-app") "core-app\ngravity:19:2\n(:ok 19)\n" "Hello Gravity\n")
               (b/execute-stage0-compiled-plan plan))))))
  (doseq [extension ["gravity" "qst"]]
    (let [path (str "lowering-collections." extension)
          source (str source-header "(defn helper [x] (+ x 2))\n"
                      "(defn main [] (let [x 3 y (helper x)] (do (println [x y]) (println {:b y :a x}) (println #{2 1}) (if (> y x) (println (quote ok)) (println false)))))\n")
          plan (compile-source path source)]
      (is (= #{:println :let :local :function-call :builtin-call :literal :vector-literal :map-literal :set-literal :do :if :quote}
             (set (keys (:instruction-summary plan)))))
      (is (= plan (compile-source path source)))
      (is (= "[3 5]\n{:a 3, :b 5}\n#{1 2}\nok\n" (b/execute-stage0-compiled-plan plan))))))

(deftest genuine-source-refusals-come-from-the-moved-expression-boundary
  (doseq [[body expected]
          [["(defn main [] absent)" "L2-UNKNOWN-SYMBOL"]
           ["(defn main [] (absent 1))" "L2-UNKNOWN-CORE-FORM"]
           ["(defn main [] (let [x] x))" "L2-LET-BINDING"]
           ["(defn main [] (let [1 2] 3))" "L2-LET-BINDING"]
           ["(defn main [] (host-reflect 1))" "P4-HOST-REFLECTION"]]]
    (let [calls (atom 0) original b/compile-stage0-expr path "lowering-refusal.gravity"
          data (with-redefs [b/compile-stage0-expr (fn [& args] (swap! calls inc) (apply original args))]
                 (error-data #(compile-source path (str source-header body "\n"))))]
      (is (= expected (:id data)))
      (is (= path (get-in data [:source-span :source])))
      (is (pos? @calls)))))

(deftest legacy-shape-and-vocabulary-gaps-remain-explicit
  ;; These retained native results are compatibility limitations, not L2
  ;; acceptance or evidence that the executor supports every catalog entry.
  (is (= {:op :if :test {:op :literal :value true}
          :then {:op :literal :value 1} :else {:op :literal :value 2}}
         (expression-port module #{} '(if true 1 2 ignored))))
  (is (= {:op :if :test {:op :literal :value nil}
          :then {:op :literal :value nil} :else {:op :literal :value nil}}
         (expression-port module #{} '(if))))
  (is (= {:op :quote :value 1} (expression-port module #{} '(quote 1 ignored))))
  (is (= {:op :quote :value nil} (expression-port module #{} '(quote))))
  (is (= "L2-UNKNOWN-CORE-FORM" (:id (error-data #(expression-port module #{} '(loop [] 1))))))
  (is (contains? b/stage0-builtin-functions 'build-read-declared-input))
  (is (= {:op :builtin-call :function 'build-read-declared-input :args [{:op :literal :value "input"}]}
         (b/compile-stage0-expr module #{} '(build-read-declared-input "input")))))
