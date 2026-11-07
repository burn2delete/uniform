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
  (is (= 25 (count (ns-publics 'gravity.lowering))))
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

(defn- target-presence-port [value] (b/perf-present? value))
(defn- target-proof-port [record]
  (lowering/native-stage0-target-proof-present? #'target-presence-port record))
(defn- target-failure-port [id source artifact subject extra]
  (throw (ex-info "native target metadata refusal"
                  {:id id :source source :artifact artifact :subject subject :extra extra})))
(defn- target-ports [] {:fail! #'target-failure-port :proof-present? #'target-proof-port})
(def ^:private target-record
  {:input-kind :verified-mir :verified? true :input-artifact-id :native/input :backend :jvm})

(deftest target-metadata-apis-have-explicit-native-contracts
  (doseq [operation [#'lowering/native-stage0-target-proof-present?
                     #'lowering/validate-native-stage0-target-lowering!]]
    (let [contract (:gravity/namespace-contract (meta operation))
          entry (get-in contract [:public-api (:name (meta operation))])]
      (is (= (:arglists (meta operation)) (:arglists entry)))
      (is (= :stage0-native-target-lowering-metadata (:contract-boundary entry)))
      (is (= :none (:admission entry)))
      (is (false? (:authenticates-proofs? entry)))
      (is (false? (:authenticates-inputs? contract)))
      (is (false? (:issues-certificates? contract)))
      (is (every? (set (:governing-contracts contract)) '[C14 C15 C18 D9])))))

(deftest target-proof-presence-preserves-order-truthiness-and-return-identity
  (doseq [field [:proof-id :proof :certificate :certificate-id :translation-validation]
          value [false 0 "" :marker]]
    ;; These are presence markers, including false and empty text, not proofs.
    (is (true? (target-proof-port {field value}))))
  (doseq [value [nil [] {} #{} '()]]
    (is (false? (target-proof-port (zipmap [:proof-id :proof :certificate :certificate-id :translation-validation]
                                        (repeat value))))))
  (let [calls (atom []) marker (Object.)]
    (with-redefs [target-presence-port (fn [value]
                                       (swap! calls conj value)
                                       (when (= value :third) marker))]
      (is (identical? marker (target-proof-port {:proof-id :first :proof :second :certificate :third
                                                :certificate-id :unreached})))
      (is (= [:first :second :third] @calls))))
  (let [calls (atom [])]
    (with-redefs [target-presence-port (fn [value] (swap! calls conj value) nil)]
      (is (nil? (target-proof-port {})))
      (is (= [nil nil nil nil nil] @calls)))))

(deftest target-proof-selects-each-callee-before-looking-up-its-argument
  (let [events (atom []) marker (Object.)
        later (fn [value] (swap! events conj [:later value]) marker)
        original (fn [value] (swap! events conj [:original value]) nil)
        record (reify clojure.lang.ILookup
                 (valAt [_ key]
                   (swap! events conj [:lookup key])
                   (alter-var-root #'target-presence-port (constantly later))
                   key)
                 (valAt [_ key _]
                   (swap! events conj [:lookup-default key]) key))]
    (with-redefs [target-presence-port original]
      (is (identical? marker (target-proof-port record)))
      (is (= [[:lookup :proof-id] [:original :proof-id]
              [:lookup :proof] [:later :proof]] @events))))
  (doseq [sentinel [(Exception. "original predicate exception") (Error. "original predicate error")]]
    (with-redefs [target-presence-port (fn [_] (throw sentinel))]
      (is (identical? sentinel (try (target-proof-port {}) (catch Throwable error error)))))))

(deftest target-metadata-checks-preserve-flags-fallback-and-failure-facts
  (doseq [kind [:verified-mir :verified-domain-ir]]
    (is (nil? (lowering/validate-native-stage0-target-lowering!
               {:fail! nil :proof-present? nil} module (assoc target-record :input-kind kind)))))
  (doseq [[kind verified] [[:raw-source true] [:verified-mir nil] [:verified-mir false]
                          [:verified-domain-ir :truthy] [:verified-mir 1]]]
    (let [opaque (Object.) original (assoc target-record :input-kind kind :verified? verified
                                         :backend false :extra opaque :pass-id :overwritten
                                         :source-span {:source :overwritten})
          data (error-data #(lowering/validate-native-stage0-target-lowering! (target-ports) module original))]
      (is (= "C14-INPUT" (:id data)))
      (is (= (:source-path module) (:source data)))
      (is (= {:lowering-request {:profile :hosted :target {:backend :jvm}}
              :input :native/input} (:artifact data)))
      (is (= (assoc original :pass-id :stage0-compiled-target-lowering-gate
                              :source-span {:source (:source-path module)}) (:subject data)))
      (is (identical? opaque (get-in data [:subject :extra])))
      (is (= [:verified-mir-or-domain-ir] (get-in data [:extra :missing-fields])))))
  (let [original (assoc target-record :target-metadata [{:target-metadata :nuw}])
        data (error-data #(lowering/validate-native-stage0-target-lowering! (target-ports) module original))]
    (is (= "C14-PROOF-METADATA" (:id data)))
    (is (= [:proof-id] (get-in data [:extra :missing-fields])))
    (is (= (assoc original :pass-id :stage0-compiled-target-lowering-gate
                            :source-span {:source (:source-path module)}) (:subject data)))
    (is (nil? (lowering/validate-native-stage0-target-lowering! (target-ports) module
                (assoc original :proof false))))))

(deftest target-metadata-opaque-lookups-and-association-keep-original-order
  (let [events (atom []) opaque (Object.)
        lookup-module (reify clojure.lang.ILookup
                        (valAt [_ key] (swap! events conj [:module key]) (get module key))
                        (valAt [_ key default] (get module key default)))
        data (assoc target-record :backend nil :extra opaque)
        record (reify clojure.lang.Associative
                 (valAt [_ key] (swap! events conj [:lowering key]) (get data key))
                 (valAt [_ key default] (get data key default))
                 (containsKey [_ key] (contains? data key))
                 (entryAt [_ key] (find data key))
                 (assoc [_ key value] (swap! events conj [:assoc key value]) (assoc data key value))
                 (count [_] (count data))
                 (cons [_ value] (conj data value))
                 (empty [_] {})
                 (equiv [_ value] (= data value))
                 (seq [_] (seq data)))]
    (is (nil? (lowering/validate-native-stage0-target-lowering! (target-ports) lookup-module record)))
    (is (= [[:module :source-path] [:lowering :backend] [:module :target] [:module :profile]
            [:lowering :input-artifact-id] [:assoc :pass-id :stage0-compiled-target-lowering-gate]
            [:lowering :input-kind] [:lowering :verified?] [:lowering :target-metadata]] @events)))
  (let [events (atom [])
        data (assoc target-record :input-kind :raw-source)
        lookup-module (reify clojure.lang.ILookup
                        (valAt [_ key] (swap! events conj key) (get module key))
                        (valAt [_ key default] (get module key default)))]
    (is (= "C14-INPUT" (:id (error-data #(lowering/validate-native-stage0-target-lowering!
                                           (target-ports) lookup-module data)))))
    (is (= [:source-path :profile] @events))))

(deftest target-metadata-returning-failures-continue-and-original-throws-survive
  (let [calls (atom []) marker (Object.) invalid (assoc target-record :input-kind :raw-source
                                                     :target-metadata [:native-metadata])]
    (with-redefs [target-failure-port (fn [& args] (swap! calls conj (vec args)) marker)]
      (is (identical? marker (lowering/validate-native-stage0-target-lowering! (target-ports) module invalid)))
      (is (= ["C14-INPUT" "C14-PROOF-METADATA"] (mapv first @calls)))
      (reset! calls [])
      (is (nil? (lowering/validate-native-stage0-target-lowering! (target-ports) module
                  (dissoc invalid :target-metadata))))
      (is (= ["C14-INPUT"] (mapv first @calls)))))
  (let [calls (atom []) record (assoc target-record :input-kind :raw-source :target-metadata [:marker])]
    (with-redefs [target-failure-port (fn [& args]
                                       (swap! calls conj (first args))
                                       (alter-var-root #'target-proof-port
                                                       (constantly (fn [actual]
                                                                    (is (identical? record actual)) true)))
                                       :returned)
                  target-proof-port (fn [_] (throw (AssertionError. "stale proof predicate")))]
      (is (nil? (lowering/validate-native-stage0-target-lowering! (target-ports) module record)))
      (is (= ["C14-INPUT"] @calls))))
  (doseq [sentinel [(Exception. "original target exception") (Error. "original target error")]]
    (let [calls (atom [])]
      (with-redefs [target-failure-port (fn [& args] (swap! calls conj (first args)) (throw sentinel))
                    target-proof-port (fn [_] (throw (AssertionError. "proof check followed thrown refusal")))]
        (is (identical? sentinel
                        (try (lowering/validate-native-stage0-target-lowering! (target-ports) module
                               (assoc target-record :input-kind :raw-source :target-metadata [:marker]))
                             (catch Throwable error error))))
        (is (= ["C14-INPUT"] @calls))))))

(deftest target-metadata-bootstrap-adapters-retain-signatures-and-live-vars
  (is (= '([record]) (:arglists (meta #'b/stage0-compiled-target-proof-present?))))
  (is (= '([module lowering]) (:arglists (meta #'b/validate-stage0-compiled-target-lowering!))))
  (let [calls (atom []) marker (Object.)]
    (with-redefs [lowering/native-stage0-target-proof-present?
                  (fn [operation record] (swap! calls conj [operation record]) marker)
                  lowering/validate-native-stage0-target-lowering!
                  (fn [operations input record] (swap! calls conj [operations input record]) marker)]
      (is (identical? marker (b/stage0-compiled-target-proof-present? target-record)))
      (is (identical? marker (b/validate-stage0-compiled-target-lowering! module target-record)))
      (is (= [[#'b/perf-present? target-record]
              [{:fail! #'b/optimization-lowering-fail! :proof-present? #'b/stage0-compiled-target-proof-present?}
               module target-record]] @calls)))))

(deftest genuine-source-target-metadata-emits-and-refuses-before-instruction-lowering
  (doseq [extension ["gravity" "qst"]]
    (let [records (mapv (fn [index field]
                          (assoc target-record :input-kind (if (even? index) :verified-mir :verified-domain-ir)
                                 :input-artifact-id (keyword "native-presence" (str "input-" index))
                                 :target-metadata [{:target-metadata :nuw :operation :op/add}]
                                 field :native-presence-marker))
                        (range) [:proof-id :proof :certificate :certificate-id :translation-validation])
          text (str "(ns lowering.target-metadata (:profile :hosted) (:target :jvm)"
                    " (:effects #{:io/write}) (:capabilities #{:io/stdout}) (:safety :safe) (:metadata "
                    (pr-str {:compiler {:compiled-gate {:target-lowering records}}})
                    "))\n(defn main [] (println (+ 2 3)))\n")
          path (str "lowering-target-metadata." extension) calls (atom []) original b/validate-stage0-compiled-target-lowering!
          plan (with-redefs [b/validate-stage0-compiled-target-lowering!
                              (fn [input record] (swap! calls conj record) (original input record))]
                 (compile-source path text))]
      (is (= records @calls))
      (is (= plan (compile-source path text)))
      (is (= path (get-in plan [:source :path])))
      (is (= (str "sha256:" (digest/sha256-hex text)) (get-in plan [:source :sha256])))
      (is (= (str "sha256:" (digest/sha256-hex (pr-str (dissoc plan :plan-id)))) (:plan-id plan)))
      (is (= "5\n" (b/execute-stage0-compiled-plan plan))))
    (doseq [[suffix expected] [["pass-contract" "C1-PASS-CONTRACT"] ["evidence-drop" "C1-EVIDENCE-DROP"]
                               ["mir-target-leak" "C11-TARGET-LEAK"] ["lowering-input" "C14-INPUT"]
                               ["proof-metadata" "C14-PROOF-METADATA"] ["verification-evidence" "C18-EVIDENCE"]]]
      (let [path (str "compiler-refusal/" suffix "." extension)
            text (slurp (str "bootstrap/clojure/fixtures/rejected/core-app-compiler-" suffix ".gravity"))
            calls (atom 0) original b/compile-stage0-function
            data (with-redefs [b/compile-stage0-function (fn [& args] (swap! calls inc) (apply original args))]
                   (error-data #(compile-source path text)))]
        (is (= expected (:id data)))
        (when (contains? #{"C14-INPUT" "C14-PROOF-METADATA"} expected)
          (is (= path (get-in data [:source-span :source]))))
        (is (zero? @calls))))))
