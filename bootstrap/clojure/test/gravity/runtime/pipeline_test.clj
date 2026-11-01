(ns gravity.runtime.pipeline-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.runtime :as runtime]))

(defn- compile-source [relative]
  (let [path (.getCanonicalPath (io/file relative))
        text (slurp path :encoding "UTF-8")
        emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! path :jvm))]
    {:path path :plan (b/p15-s23-stage2-compiler-artifact-plan emitter path text)}))
(def ^:private hello-plans (delay (mapv #(compile-source (str "examples/hello" %)) [".gravity" ".qst"])))
(def ^:private runtime-plan (delay (compile-source "bootstrap/gravity/src/gravity/runtime.gravity")))
(def ^:private context {:engine :stage-owned-runtime-pipeline-test})
(defn- diagnostic [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo error (ex-data error))))
(defn- observed-error [f]
  (try (f) nil (catch Throwable error error)))

(deftest namespace-contract-is-explicit-and-does-not-admit-plans
  (is (empty? (ns-aliases 'gravity.runtime)))
  (is (= #{'execute-function-scoped 'execute-function 'artifact-invoke 'execute-plan
           'hosted-add 'hosted-subtract 'hosted-multiply 'hosted-divide
           'hosted-equal 'hosted-less-than 'hosted-greater-than
           'hosted-less-than-or-equal 'hosted-greater-than-or-equal
           'hosted-str 'fail-builtin-error! 'hosted-get 'hosted-builtin
           'bind-values 'execute-value-scoped 'execute-values-scoped
           'execute-map-entries-scoped 'execute-instructions-scoped 'execute-instruction-scoped
           'executor-rule-record 'fail-call-arity! 'assert-min-arity! 'assert-exact-arity!
           'assert-between-arity! 'assert-even-arity! 'hosted-unary-builtin
           'map-entry 'fail-recur! 'nontail-value!
           'execute-generic-builtin-call-scoped 'execute-call-scoped}
         (set (keys (ns-publics 'gravity.runtime)))))
  (doseq [[name operation] (ns-publics 'gravity.runtime)]
    (let [contract (:gravity/namespace-contract (meta operation))]
      (is (= 'gravity.runtime (:namespace contract)))
      (is (= (:arglists (meta operation)) (get-in contract [:public-api name :arglists])))
      (is (= :none (get-in contract [:public-api name :admission])))
      (is (false? (:authenticates-plans? contract)))
      (is (false? (:grants-authority? contract)))
      (is (true? (:trusted-operation-map? contract)))
      (is (some #{:public-plan-authentication} (:unsupported contract)))))
  (let [contract (:gravity/namespace-contract (meta #'runtime/execute-plan))
        declared (set (mapcat :operation-keys (vals (select-keys (:public-api contract) '[execute-function-scoped execute-function artifact-invoke execute-plan]))))
        operations ((var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-stage-operations)))]
    (is (= declared (set (keys operations))))
    (is (every? var? (vals operations)))
    (is (false? (get-in contract [:public-api 'execute-plan :adds-function-entry-scope-guard?])))))

(deftest paired-real-programs-retain-output-plan-identity-and-diagnostics
  (is (= (get-in (first @hello-plans) [:plan :plan-id])
         (get-in (second @hello-plans) [:plan :plan-id])))
  (doseq [{:keys [path plan]} @hello-plans]
    (is (.isFile (io/file path)))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (let [record (b/p15-s23-stage2-runtime-execute-plan context plan)]
      (is (= "Hello Gravity\n" (:stdout record)))
      (is (nil? (:entrypoint-result record)))
      (is (= (:plan-id plan) (:plan-id record)))
      (is (= (:instruction-summary plan) (:instruction-summary record)))
      (is (= (:effect-summary plan) (:effect-summary record)))
      (is (= :complete (:status record))))
    (is (= "Hello Gravity\n"
           (with-out-str (b/p15-s23-stage2-runtime-execute-function context plan (:entrypoint plan) []))))
    (is (= "L2-UNKNOWN-CORE-FORM"
           (:id (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan 'not-defined [])))))
    (is (= "L2-FUNCTION-ARITY"
           (:id (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan (:entrypoint plan) [:extra])))))
    (is (= "L3-UNKNOWN-ALIAS"
           (:id (diagnostic #(b/p15-s23-stage2-runtime-execute-plan context (dissoc plan :entrypoint))))))
    (is (= "L2-MAIN-ARITY"
           (:id (diagnostic #(b/p15-s23-stage2-runtime-execute-plan
                               context (assoc-in plan [:functions (:entrypoint plan) :params] ['unexpected])))))))
  (let [ctx (assoc context :runtime-artifact-plan (:plan @runtime-plan)
                          :runtime-artifact-source-path (:path @runtime-plan))]
    (is (= "prefix A B\n"
           (b/p15-s23-stage2-runtime-artifact-invoke ctx 'p15-s23-runtime-println-output [["A" "B"] "prefix "])))
    (doseq [changed [context ctx]]
      (is (= "P15S23X002"
             (:id (diagnostic #(b/p15-s23-stage2-runtime-artifact-invoke
                                 changed 'p15-s23-runtime-println-output []))))))))

(defn- scope-operations [events selected registered? module?]
  ;; Mocked control flow only. These functions are not native scope holders or
  ;; authority proofs, and their markers never enter a genuine admission path.
  {:native-scope! (fn [scope] (swap! events conj [:native scope]))
   :module-entry-scope! (fn [_ _ _ scope] (swap! events conj [:module scope]) selected)
   :registered-scope? (fn [scope] (swap! events conj [:registered scope]) registered?)
   :assert-function-entry! (fn [_ _ _ _ scope] (swap! events conj [:reader-entry scope]))
   :module-scope? (fn [scope] (swap! events conj [:module-scope scope]) module?)
   :assert-scoped-function-entry! (fn [_ _ _ _ scope] (swap! events conj [:scoped-entry scope]))
   :fail! (fn [id message data] (throw (ex-info message (assoc data :id id))))
   :fail-call-arity! (fn [& _] (throw (ex-info "unexpected arity" {})))
   :bind-values (fn [_ params values] (swap! events conj [:bind params values]) (zipmap params values))
   :execute-instructions-scoped (fn [_ _ env instructions scope]
                                  (swap! events conj [:execute env instructions scope]) :finished)
   :recur-signal? (constantly false) :recur-values identity
   :recur-fail! (fn [& _] (throw (ex-info "unexpected recurrence" {})))})

(deftest native-scope-check-and-entry-authorization-precede-execution
  (let [plan {:functions {'f {:params ['x] :instructions [:body]}}}]
    (doseq [[registered? module? expected]
            [[true false [[:native :incoming] [:module :incoming] [:registered :selected] [:reader-entry :selected]]]
             [false true [[:native :incoming] [:module :incoming] [:registered :selected]
                          [:module-scope :selected] [:scoped-entry nil]]]
             [false false [[:native :incoming] [:module :incoming] [:registered :selected]
                           [:module-scope :selected] [:scoped-entry :selected]]]]]
      (let [events (atom []) ops (scope-operations events :selected registered? module?)]
        (is (= :finished (runtime/execute-function-scoped context plan 'f [9] :incoming ops)))
        (is (= expected (subvec @events 0 (count expected))))
        (is (= [:bind ['x] [9]] (nth @events (count expected))))
        (is (= [:execute {'x 9} [:body] :selected] (last @events))))))
  (let [lookups (atom 0) events (atom []) denied (ex-info "native denial" {:id :native-denied})
        hostile (reify clojure.lang.ILookup
                  (valAt [_ _] (swap! lookups inc) nil)
                  (valAt [_ _ fallback] (swap! lookups inc) fallback))
        ops (assoc (scope-operations events :selected false false) :native-scope! (fn [_] (throw denied)))]
    (is (identical? denied (observed-error #(runtime/execute-function-scoped context hostile 'f [] :incoming ops))))
    (is (zero? @lookups))
    (is (empty? @events)))
  (let [events (atom []) denied (ex-info "entry denial" {:id :entry-denied})
        ops (assoc (scope-operations events :selected true false)
                   :assert-function-entry! (fn [& _] (throw denied)))]
    (is (identical? denied (observed-error #(runtime/execute-function-scoped context {} 'f [] :incoming ops))))
    (is (= [[:native :incoming] [:module :incoming] [:registered :selected]] @events))))

(deftest module-dispatch-and-artifact-invocation-preserve-operation-ports
  (let [seen (atom []) ops {:module-execute-function (fn [& args] (swap! seen conj [:module args]) :module-result)
                           :execute-function-scoped (fn [& args] (swap! seen conj [:scoped args]) :scoped-result)}
        bundle {:kind :gravity/stage2-compiler-artifact-module-bundle}]
    (is (= :module-result (runtime/execute-function context bundle 'f [1] ops)))
    (is (= :scoped-result (runtime/execute-function context {} 'g [2] ops)))
    (is (= [:module (list context bundle 'f [1])] (first @seen)))
    (is (= [:scoped (list context {} 'g [2] nil)] (second @seen))))
  (let [plan {:functions {'format-value {:arity 2}}} seen (atom nil)
        ctx (assoc context :runtime-artifact-plan plan :runtime-artifact-source-path "actual.gravity")]
    (with-redefs [b/p15-s23-stage2-runtime-execute-function
                  (fn [& args] (reset! seen (vec args)) :interposed)]
      (is (= :interposed (b/p15-s23-stage2-runtime-artifact-invoke ctx 'format-value [1 2])))
      (is (= [(dissoc ctx :runtime-artifact-plan) plan 'format-value [1 2]] @seen)))))

(deftest compatibility-wrapper-retains-the-private-scoped-var
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        seen (atom nil)]
    (with-redefs-fn {scoped (fn [& args] (reset! seen (vec args)) :interposed-scope)}
      #(is (= :interposed-scope (b/p15-s23-stage2-runtime-execute-function context {} 'f [1]))))
    (is (= [context {} 'f [1] nil] @seen))))

(def ^:private arithmetic-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-arithmetic/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(def ^:private arithmetic-results
  [['add-zero 0] ['add-unary 7] ['add-binary 9] ['add-ternary 6] ['add-many 15]
   ['subtract-unary -7] ['subtract-binary 5] ['subtract-ternary 4] ['subtract-many 10]
   ['multiply-zero 1] ['multiply-unary 7] ['multiply-binary 20] ['multiply-ternary 24] ['multiply-many 120]
   ['divide-unary 1/2] ['divide-binary 9/2] ['divide-ternary 4] ['divide-many 1]])

(deftest paired-real-arithmetic-plans-preserve-values-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @arithmetic-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected] arithmetic-results]
        (is (= expected (b/p15-s23-stage2-runtime-execute-function context plan callee []))))
      (doseq [[callee id builtin]
              [['divide-zero "L2-BUILTIN-ERROR" '/] ['unary-zero "L2-BUILTIN-ERROR" '/]
               ['overflow-add "L2-BUILTIN-ERROR" '+] ['overflow-subtract "L2-BUILTIN-ERROR" '-]
               ['overflow-multiply "L2-BUILTIN-ERROR" '*] ['wrong-operand "L2-BUILTIN-ERROR" '+]
               ['subtract-no-arguments "L2-BUILTIN-ARITY" '-] ['divide-no-arguments "L2-BUILTIN-ARITY" '/]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= id (:id error)))
          (is (= builtin (:function error)))
          ;; The legacy dispatcher has no diagnostic source path at this seam.
          ;; Actual source paths remain in the compiled-plan/control envelope.
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @arithmetic-programs))))))

(deftest hosted-arithmetic-retains-native-values-classes-and-failure-kinds
  (doseq [[operation host inputs]
          [[runtime/hosted-add + [[] [4] [1 2] [1 2 3] [1 2 3 4 5] [1M 2M] [1N 2N]]]
           [runtime/hosted-subtract - [[4] [9 4] [9 2 3] [20 1 2 3 4] [1/2 1/4]]]
           [runtime/hosted-multiply * [[] [4] [2 3] [2 3 4] [1 2 3 4 5] [1M 2M]]]
           [runtime/hosted-divide / [[2] [9 2] [24 3 2] [120 2 3 4 5]]]]
          args inputs]
    (let [expected (apply host args) actual (operation args)]
      (is (= expected actual))
      (is (= (class expected) (class actual)))))
  (doseq [[operation host args]
          [[runtime/hosted-add + [Long/MAX_VALUE 1]]
           [runtime/hosted-subtract - []] [runtime/hosted-multiply * [Long/MAX_VALUE 2]]
           [runtime/hosted-divide / []] [runtime/hosted-divide / [1 0]]
           [runtime/hosted-add + ["wrong" 1]]]]
    (let [expected (observed-error #(apply host args)) actual (observed-error #(operation args))]
      (is (some? expected))
      (is (= (class expected) (class actual)))
      (is (= (ex-message expected) (ex-message actual))))))

(deftest private-arithmetic-wrappers-retain-late-stage-interposition
  ;; Trusted callback markers only, never source or authority evidence.
  (doseq [name '[add subtract multiply divide]]
    (let [compatibility (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-stage2-runtime-" name)))
          stage (ns-resolve 'gravity.runtime (symbol (str "hosted-" name)))
          args [2 4] calls (atom []) sentinel (Object.)]
      (is (true? (:private (meta compatibility))))
      (is (= '([args]) (:arglists (meta compatibility))))
      (with-redefs-fn {stage (fn [received] (swap! calls conj received) sentinel)}
        #(is (identical? sentinel ((deref compatibility) args))))
      (is (= 1 (count @calls)))
      (is (identical? args (first @calls))))))

(deftest arithmetic-caller-arity-guards-run-before-the-stage-helper
  (let [plan (:plan (first (filter #(= "rejected" (:disposition %)) @arithmetic-programs)))]
    (doseq [[callee stage] [['subtract-no-arguments #'runtime/hosted-subtract]
                           ['divide-no-arguments #'runtime/hosted-divide]]]
      (let [calls (atom 0)]
        (with-redefs-fn {stage (fn [_] (swap! calls inc) :unexpected)}
          #(is (= "L2-BUILTIN-ARITY"
                  (:id (diagnostic (fn [] (b/p15-s23-stage2-runtime-execute-function context plan callee [])))))))
        (is (zero? @calls))))))

(def ^:private comparison-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-comparisons/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(def ^:private comparison-results
  '[[equal-unary true] [equal-binary true] [equal-binary-false false]
    [equal-ternary true] [equal-ternary-false false] [equal-many true]
    [equal-many-false false] [equal-string true]
    [less-binary true] [less-binary-false false] [less-ternary true] [less-many-false false]
    [greater-binary true] [greater-binary-false false] [greater-ternary true] [greater-many-false false]
    [less-equal-binary true] [less-equal-binary-false false] [less-equal-ternary true]
    [less-equal-many-false false] [less-equal-equality true]
    [greater-equal-binary true] [greater-equal-binary-false false] [greater-equal-ternary true]
    [greater-equal-many-false false] [greater-equal-equality true]])

(deftest paired-real-comparison-plans-retain-booleans-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @comparison-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected] comparison-results]
        (let [result (b/p15-s23-stage2-runtime-execute-function context plan callee [])]
          (is (= expected result))
          (is (= java.lang.Boolean (class result)))))
      (doseq [[callee id builtin]
              '[[equal-no-arguments "L2-BUILTIN-ARITY" =]
                [less-unary "L2-BUILTIN-ARITY" <] [greater-unary "L2-BUILTIN-ARITY" >]
                [less-equal-unary "L2-BUILTIN-ARITY" <=] [greater-equal-unary "L2-BUILTIN-ARITY" >=]
                [less-wrong-operand "L2-BUILTIN-ERROR" <] [greater-wrong-operand "L2-BUILTIN-ERROR" >]
                [less-equal-wrong-operand "L2-BUILTIN-ERROR" <=] [greater-equal-wrong-operand "L2-BUILTIN-ERROR" >=]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= id (:id error)))
          (is (= builtin (:function error)))
          ;; Retain the legacy source gap; actual source remains in the plan envelope.
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @comparison-programs))))))

(deftest hosted-comparisons-preserve-native-results-and-exceptions
  (doseq [[operation host inputs]
          [[runtime/hosted-equal = [[7] [7 7] [7 8] [7 7 7] [7 7 8] [7 7 7 7] ["same" "same"] [nil nil]]]
           [runtime/hosted-less-than < [[1] [1 2] [2 1] [1 2 3] [1 2 3 2] [Long/MIN_VALUE 0 Long/MAX_VALUE]]]
           [runtime/hosted-greater-than > [[1] [2 1] [1 2] [3 2 1] [4 3 2 3] [Long/MAX_VALUE 0 Long/MIN_VALUE]]]
           [runtime/hosted-less-than-or-equal <= [[1] [1 2] [2 1] [1 1 2] [1 2 3 2] [2 2]]]
           [runtime/hosted-greater-than-or-equal >= [[1] [2 1] [1 2] [2 2 1] [4 3 2 3] [2 2]]]]
          args inputs]
    (let [expected (apply host args) actual (operation args)]
      (is (= expected actual))
      (is (= (class expected) (class actual)))))
  (doseq [[operation host] [[runtime/hosted-equal =] [runtime/hosted-less-than <]
                          [runtime/hosted-greater-than >] [runtime/hosted-less-than-or-equal <=]
                          [runtime/hosted-greater-than-or-equal >=]]]
    (let [expected (observed-error #(apply host [])) actual (observed-error #(operation []))]
      (is (some? expected))
      (is (= (class expected) (class actual)))
      (is (= (ex-message expected) (ex-message actual))))))

(deftest private-comparison-wrappers-retain-late-stage-interposition
  ;; Trusted callback markers only, separate from actual source/plan evidence.
  (doseq [name '[equal less-than greater-than less-than-or-equal greater-than-or-equal]]
    (let [compatibility (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-stage2-runtime-" name)))
          stage (ns-resolve 'gravity.runtime (symbol (str "hosted-" name)))
          args [2 4] calls (atom []) sentinel (Object.) denied (ex-info "comparison callback" {:id :comparison-callback})]
      (is (true? (:private (meta compatibility))))
      (is (= '([args]) (:arglists (meta compatibility))))
      (with-redefs-fn {stage (fn [received] (swap! calls conj received) sentinel)}
        #(is (identical? sentinel ((deref compatibility) args))))
      (is (= 1 (count @calls)))
      (is (identical? args (first @calls)))
      (with-redefs-fn {stage (fn [_] (throw denied))}
        #(is (identical? denied (observed-error (fn [] ((deref compatibility) args)))))))))

(deftest comparison-caller-arity-guards-run-before-the-stage-helper
  (let [plan (:plan (first (filter #(= "rejected" (:disposition %)) @comparison-programs)))]
    (doseq [[callee stage] [['equal-no-arguments #'runtime/hosted-equal]
                           ['less-unary #'runtime/hosted-less-than]
                           ['greater-unary #'runtime/hosted-greater-than]
                           ['less-equal-unary #'runtime/hosted-less-than-or-equal]
                           ['greater-equal-unary #'runtime/hosted-greater-than-or-equal]]]
      (let [calls (atom 0)]
        (with-redefs-fn {stage (fn [_] (swap! calls inc) :unexpected)}
          #(is (= "L2-BUILTIN-ARITY"
                  (:id (diagnostic (fn [] (b/p15-s23-stage2-runtime-execute-function context plan callee [])))))))
        (is (zero? @calls))))))

(def ^:private value-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-values/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(deftest paired-real-values-retain-results-failures-and-identities
  (doseq [{:keys [path plan disposition]} @value-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected]
              '[[string-empty ""] [string-nil ""] [string-number "7"] [string-binary "v=7"]
                [string-ternary "a1b"] [string-many "afalse7z"] [string-ratio "1/2"]
                [lookup-map 7] [lookup-missing nil] [lookup-default 9]
                [lookup-nil-value nil] [lookup-false-value false]
                [lookup-vector 9] [lookup-vector-default -1]
                [lookup-nil nil] [lookup-nil-default :missing] [lookup-vector-value [1 2]]]]
        (let [value (b/p15-s23-stage2-runtime-execute-function context plan callee [])]
          (is (= expected value))
          (is (= (class expected) (class value)))))
      (doseq [[callee builtin] '[[divide-zero /] [wrong-operand +] [lookup-eager-default /]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= "L2-BUILTIN-ERROR" (:id error)))
          (is (= builtin (:function error)))
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @value-programs))))))

(deftest hosted-string-and-lookup-preserve-values-and-reference-identity
  (doseq [args [[] [nil] [7] ["v=" 7] ["a" 1 "b"] ["a" nil false 7 "z"] [1/2] [[:a 7] {:x nil}]]]
    (is (= (apply str args) (runtime/hosted-str args))))
  ;; Trusted host contexts/objects only; these do not admit source or authority.
  (let [value (with-meta [1 2] {:origin :retained}) fallback (Object.) calls (atom 0)
        failure (clojure.lang.Var/create (fn [& _] (swap! calls inc) :unexpected))]
    (is (identical? value (runtime/hosted-get failure {} {:x value} :x)))
    (is (identical? fallback (runtime/hosted-get failure {} {} :missing fallback)))
    (is (nil? (runtime/hosted-get failure {} {:x nil} :x fallback)))
    (is (false? (runtime/hosted-get failure {} {:x false} :x fallback)))
    (is (identical? fallback (runtime/hosted-get failure {} nil :x fallback)))
    (is (zero? @calls))))

(deftest lookup-retains-exception-info-and-late-host-error-operation
  (doseq [default? [false true]]
    (let [calls (atom []) info (ex-info "lookup info" {:id :existing-lookup-info})
          failure (clojure.lang.Var/create (fn [& args] (swap! calls conj args) :unexpected))
          collection (reify clojure.lang.ILookup
                       (valAt [_ _] (throw info))
                       (valAt [_ _ _] (throw info)))
          invoke #(if default? (runtime/hosted-get failure {} collection :key :fallback)
                               (runtime/hosted-get failure {} collection :key))]
      (is (identical? info (observed-error invoke)))
      (is (empty? @calls)))
    (let [events (atom []) error (ArithmeticException. "host lookup") plan {:source {:path "unit.gravity"}}
          failure (clojure.lang.Var/create (fn [& _] (swap! events conj :old-error) :old))
          changed (fn [& args] (swap! events conj [:new-error args]) :translated)
          lookup (fn [] (swap! events conj :lookup) (alter-var-root failure (constantly changed)) (throw error))
          collection (reify clojure.lang.ILookup
                       (valAt [_ _] (lookup))
                       (valAt [_ _ _] (lookup)))
          result (if default? (runtime/hosted-get failure plan collection :key :fallback)
                              (runtime/hosted-get failure plan collection :key))]
      (is (= :translated result))
      (is (= :lookup (first @events)))
      (is (= :new-error (first (second @events))))
      (is (= [plan 'get error] (vec (second (second @events))))))))

(deftest builtin-error-operation-is-selected-before-message-construction
  (let [events (atom []) failure (clojure.lang.Var/create nil)
        original (fn [& args] (swap! events conj [:original args]) :emitted)
        changed (fn [& _] (swap! events conj :changed) :unexpected)
        error (proxy [Exception] ["original"]
                (getMessage [] (swap! events conj :message)
                  (alter-var-root failure (constantly changed)) "host-message"))]
    (alter-var-root failure (constantly original))
    (is (= :emitted (runtime/fail-builtin-error! failure {:source {:path "unit.gravity"}} 'get error)))
    (is (= :message (first @events)))
    (is (= :original (first (second @events))))
    (is (= ["L2-BUILTIN-ERROR" "stage2 runtime builtin call failed"
            {:source-span {:source "unit.gravity"} :function 'get :cause-message "host-message"
             :remediation "Keep builtin inputs inside the checked stage2 hosted-core runtime subset."}]
           (vec (second (second @events)))))))

(deftest private-value-wrappers-preserve-signatures-and-late-stage-ports
  (let [string-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-str)
        error-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-fail-builtin-error!)
        get-var (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-invoke-get)
        args ["a" 7] plan {} error (Exception. "unit") seen (atom []) marker (Object.)]
    (doseq [operation [string-var error-var get-var]] (is (true? (:private (meta operation)))))
    (is (= '([args]) (:arglists (meta string-var))))
    (is (= '([plan callee ex]) (:arglists (meta error-var))))
    (is (= '([plan collection key] [plan collection key not-found]) (:arglists (meta get-var))))
    (with-redefs [runtime/hosted-str (fn [received] (swap! seen conj received) marker)]
      (is (identical? marker ((deref string-var) args))))
    (is (identical? args (first @seen)))
    (reset! seen [])
    (with-redefs [runtime/fail-builtin-error! (fn [& received] (swap! seen conj (vec received)) marker)]
      (is (identical? marker ((deref error-var) plan 'get error))))
    (is (= [#'b/fail! plan 'get error] (first @seen)))
    (reset! seen [])
    (with-redefs [runtime/hosted-get (fn [& received] (swap! seen conj (vec received)) marker)]
      (is (identical? marker ((deref get-var) plan {:x 1} :x)))
      (is (identical? marker ((deref get-var) plan {} :x :fallback))))
    (is (= [[error-var plan {:x 1} :x] [error-var plan {} :x :fallback]] @seen))))

(def ^:private dispatch-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-dispatch/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(deftest paired-builtin-dispatch-programs-preserve-values-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @dispatch-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected] '[[add 6] [subtract 5] [multiply 6] [divide 4] [equal true] [less true] [greater true] [less-equal true] [greater-equal true] [string ":a7"] [readable "[1 2]"] [mapping {:a 1, :b 2}] [vector-value [1 2]] [list-value (1 2)] [conjoin [1 2 3]] [associate {:a 1, :b 2, :c 3}] [lookup 1] [lookup-default 7] [head 1] [next-value 2] [tail (2 3)] [length 2] [symbol-value true] [keyword-value true] [character true] [number-value true] [sequence true] [list-kind true] [vector-kind true] [map-kind true] [set-kind true] [string-kind true] [contains true] [even-value true] [integer-value true] [boolean-value true] [map-keys (:a)] [set-value #{1 2}] [sorted (1 2 3)] [vectorized [1 2]] [quotient 3] [subvector [2 3]]]]
        (let [value (b/p15-s23-stage2-runtime-execute-function context plan callee [])]
          (is (= expected value) (str callee))
          (is (= (case callee mapping clojure.lang.PersistentHashMap length Integer (class expected))
                 (class value)) (str callee))))
      (doseq [[callee builtin id] '[[subtract-arity - "L2-BUILTIN-ARITY"] [divide-arity / "L2-BUILTIN-ARITY"] [equal-arity = "L2-BUILTIN-ARITY"] [less-arity < "L2-BUILTIN-ARITY"] [greater-arity > "L2-BUILTIN-ARITY"] [less-equal-arity <= "L2-BUILTIN-ARITY"] [greater-equal-arity >= "L2-BUILTIN-ARITY"] [map-arity hash-map "L2-BUILTIN-ARITY"] [conj-arity conj "L2-BUILTIN-ARITY"] [assoc-short assoc "L2-BUILTIN-ARITY"] [assoc-pairs assoc "L2-BUILTIN-ARITY"] [get-short get "L2-BUILTIN-ARITY"] [first-short first "L2-BUILTIN-ARITY"] [quot-short quot "L2-BUILTIN-ARITY"] [subvec-short subvec "L2-BUILTIN-ARITY"] [divide-zero / "L2-BUILTIN-ERROR"] [bad-assoc assoc "L2-BUILTIN-ERROR"] [bad-conj conj "L2-BUILTIN-ERROR"] [bad-quot quot "L2-BUILTIN-ERROR"] [bad-subvec subvec "L2-BUILTIN-ERROR"]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= id (:id error)) (str callee))
          (is (= builtin (:function error)))
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @dispatch-programs))))))

(defn- builtin-operations []
  (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-builtin-operations)))

(deftest generic-dispatch-covers-every-ordinary-arm-with-trusted-host-values
  ;; Direct host units cover arms bypassed by the existing unary/get fast paths.
  ;; These values/operations do not establish source admission or authority.
  (let [operations (builtin-operations) plan {:source {:path "unit.gravity"}}
        contract (:gravity/namespace-contract (meta #'runtime/hosted-builtin))]
    (is (= (set (get-in contract [:public-api 'hosted-builtin :operation-keys])) (set (keys operations))))
    (is (every? var? (vals operations)))
    (is (= :bootstrap (get-in contract [:public-api 'hosted-builtin :reserved-operation-owner])))
    (doseq [[callee args expected]
            '[[+ [1 2 3] 6] [- [8 3] 5] [* [2 3] 6] [/ [8 2] 4]
              [= [2 2] true] [< [1 2] true] [> [2 1] true] [<= [2 2] true] [>= [2 2] true]
              [str [:a 7] ":a7"] [pr-str [[1 2]] "[1 2]"]
              [hash-map [:a 1 :b 2] {:a 1 :b 2}] [vector [1 2] [1 2]] [list [1 2] (1 2)]
              [conj [[1] 2 3] [1 2 3]] [assoc [{:a 1} :b 2 :c 3] {:a 1 :b 2 :c 3}]
              [get [{:a 1} :a] 1] [get [{} :a 7] 7]
              [first [[1 2]] 1] [second [[1 2]] 2] [rest [[1 2 3]] (2 3)] [count [[1 2]] 2]
              [symbol? [a] true] [keyword? [:a] true] [char? [\a] true] [number? [1] true]
              [seq? [(1)] true] [list? [(1)] true] [vector? [[1]] true] [map? [{}] true]
              [set? [#{1}] true] [string? ["a"] true] [contains? [{:a nil} :a] true]
              [even? [2] true] [integer? [2] true] [boolean? [false] true]
              [keys [{:a 1}] (:a)] [set [[1 1 2]] #{1 2}] [sort-by-pr-str [[2 1 3]] (1 2 3)]
              [vec [(1 2)] [1 2]] [quot [7 2] 3] [subvec [[1 2 3] 1 3] [2 3]]]]
      (doseq [value [(runtime/hosted-builtin plan callee args operations)
                     (b/p15-s23-stage2-runtime-invoke-builtin plan callee args)]]
        (is (= expected value) (str callee))
        (is (= (case callee hash-map clojure.lang.PersistentHashMap count Integer (class expected))
               (class value)) (str callee))))))

(deftest ordinary-dispatch-selects-only-needed-ports-and-retains-late-vars
  (let [events (atom [])
        addition (clojure.lang.Var/create (fn [args] (swap! events conj [:add args]) :result))
        only-add (reify clojure.lang.ILookup
                   (valAt [_ key] (swap! events conj [:lookup key])
                     (if (= :add key) addition (throw (AssertionError. "unrelated lookup")))))]
    (is (= :result (runtime/hosted-builtin {} '+ [1 2] only-add)))
    (is (= [[:lookup :add] [:add [1 2]]] @events)))
  (let [events (atom []) subtraction (clojure.lang.Var/create (fn [& _] :old))
        guard (clojure.lang.Var/create
                (fn [plan callee args minimum]
                  (swap! events conj [:arity plan callee args minimum])
                  (alter-var-root subtraction (constantly (fn [args] (swap! events conj [:subtract args]) :new)))))
        operations {:assert-min-arity! guard :subtract subtraction}]
    (is (= :new (runtime/hosted-builtin {:unit true} '- [7] operations)))
    (is (= [[:arity {:unit true} '- [7] 1] [:subtract [7]]] @events)))
  (let [failure (ex-info "unit refusal" {:id :unit-arity}) reads (atom 0)
        args (reify clojure.lang.Indexed
               (nth [_ _] (swap! reads inc) :unexpected)
               (nth [_ _ _] (swap! reads inc) :unexpected))
        guard (clojure.lang.Var/create (fn [& _] (throw failure)))]
    (is (identical? failure
                   (observed-error #(runtime/hosted-builtin {} 'get args {:assert-between-arity! guard}))))
    (is (zero? @reads))))

(deftest reserved-callees-have-no-stage-operation-or-authority-port
  (let [lookups (atom 0)
        operations (reify clojure.lang.ILookup
                     (valAt [_ _] (swap! lookups inc) (throw (AssertionError. "reserved port"))))]
    (doseq [callee '[build-read-declared-input hosted-native-data-preflight bootstrap-native-identical?
                    bootstrap-native-carrier-header bootstrap-symbol-utf16-unit-count unknown-builtin]]
      (is (instance? IllegalArgumentException
                     (observed-error #(runtime/hosted-builtin {} callee [] operations)))))
    (is (zero? @lookups))))

(deftest unknown-ordinary-callee-retains-bootstrap-host-error-translation
  (let [error (diagnostic #(b/p15-s23-stage2-runtime-invoke-builtin
                            {:source {:path "unit.gravity"}} 'unknown-builtin []))]
    (is (= "L2-BUILTIN-ERROR" (:id error)))
    (is (= 'unknown-builtin (:function error)))
    (is (= "No matching clause: unknown-builtin" (:cause-message error)))
    (is (= {:source "unit.gravity"} (:source-span error)))))

(deftest bootstrap-builtin-wrapper-retains-signatures-late-stage-port-and-catches
  (is (= '([plan callee args] [runtime plan callee args]
           [runtime plan callee args instruction public-invocation-scope])
         (:arglists (meta #'b/p15-s23-stage2-runtime-invoke-builtin))))
  (let [calls (atom []) plan {} args [1 2] marker (Object.)
        info (ex-info "unit stage exception" {:id :unit-stage})]
    (with-redefs [runtime/hosted-builtin (fn [& received] (swap! calls conj (vec received)) marker)]
      (is (identical? marker (b/p15-s23-stage2-runtime-invoke-builtin plan '+ args)))
      (is (identical? marker (b/p15-s23-stage2-runtime-invoke-builtin context plan '+ args)))
      (is (identical? marker (b/p15-s23-stage2-runtime-invoke-builtin context plan '+ args :instruction nil))))
    (is (= 3 (count @calls)))
    (doseq [call @calls]
      (is (= [plan '+ args (builtin-operations)] call))
      (is (identical? args (nth call 2))))
    (with-redefs [runtime/hosted-builtin (fn [& _] (throw info))]
      (is (identical? info (observed-error #(b/p15-s23-stage2-runtime-invoke-builtin plan '+ args)))))
    (with-redefs [runtime/hosted-builtin (fn [& _] (throw (ArithmeticException. "unit host failure")))]
      (let [error (diagnostic #(b/p15-s23-stage2-runtime-invoke-builtin plan '+ args))]
        (is (= "L2-BUILTIN-ERROR" (:id error)))
        (is (= '+ (:function error)))
        (is (= "unit host failure" (:cause-message error)))))))

(def ^:private evaluation-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-evaluation/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(deftest paired-evaluation-carrier-programs-preserve-values-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @evaluation-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected] '[[vector0 []] [vector1 [7]] [vector2 [1 2]] [vector3 [1 2 3]] [vector5 [1 2 3 4 5]] [set-values #{1 3 2}] [map0 {}] [map1 {:a 7}] [map-collision {:a 1}] [nested-map {:a [1 2 3 4], :b {:c 7}}] [call0 0] [call1 7] [call2 3] [call3 6] [call4 10] [call5 15] [do0 nil] [do1 7] [do4 4] [sequential-bindings [1 2 3 4]] [loop-sum 10] [loop-four 10] [function-recur 6]]]
        (is (= expected (b/p15-s23-stage2-runtime-execute-function context plan callee [])) (str callee)))
      (doseq [[callee builtin id] '[[vector-first + "L2-BUILTIN-ERROR"] [vector-third / "L2-BUILTIN-ERROR"] [map-key-first / "L2-BUILTIN-ERROR"] [map-value-first / "L2-BUILTIN-ERROR"] [sequence-first + "L2-BUILTIN-ERROR"] [binding-first + "L2-BUILTIN-ERROR"] [argument-first + "L2-BUILTIN-ERROR"] [loop-initializer / "L2-BUILTIN-ERROR"]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= id (:id error)) (str callee))
          (is (= builtin (:function error)))
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @evaluation-programs))))))

(defn- evaluation-operations []
  (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-evaluation-operations)))

(deftest evaluation-contract-ports-and-small-binders-retain-native-values
  (let [contract (:gravity/namespace-contract (meta #'runtime/bind-values))
        names '[bind-values execute-value-scoped execute-values-scoped execute-map-entries-scoped execute-instructions-scoped]
        operations (evaluation-operations)]
    (is (= (set (mapcat #(get-in contract [:public-api % :operation-keys]) names)) (set (keys operations))))
    (is (every? var? (vals operations))))
  (let [base (with-meta {:retained :value} {:origin :unit})]
    (is (identical? base (runtime/bind-values base [] [])))
    (doseq [n (range 1 6)]
      (let [names (mapv #(keyword (str "x" %)) (range n)) values (vec (range n))
            expected (merge base (zipmap names values)) actual (runtime/bind-values base names values)]
        (is (= expected actual))
        (is (= (meta expected) (meta actual)))))
    (is (= {:x 2} (runtime/bind-values {} [:x :x] [1 2])))))

(deftest value-evaluation-selects-nontail-operation-before-instruction-evaluation
  ;; Anonymous operations/scopes are host control-flow units, not admission.
  (let [events (atom []) scope (Object.)
        nontail (clojure.lang.Var/create (fn [& args] (swap! events conj [:original args]) :retained))
        instruction (clojure.lang.Var/create
                      (fn [& args]
                        (swap! events conj [:instruction args])
                        (alter-var-root nontail (constantly (fn [& _] :changed))) :value))]
    (is (= :retained (runtime/execute-value-scoped :runtime :plan :env :instruction :reason scope
                     {:nontail-value! nontail :execute-instruction-scoped instruction})))
    (is (= :instruction (ffirst @events)))
    (is (= [:runtime :plan :env :instruction scope] (vec (second (first @events)))))
    (is (= [:original '(:plan :value :reason)] (second @events)))))

(deftest argument-traversal-retains-small-and-generic-paths-and-late-operations
  (let [lookups (atom 0)
        none (reify clojure.lang.ILookup (valAt [_ _] (swap! lookups inc) nil))]
    (is (= [] (runtime/execute-values-scoped nil nil nil [] :reason nil none)))
    (is (zero? @lookups)))
  (doseq [n (range 1 6) list? [false true]]
    (let [events (atom []) scope (Object.) callback (clojure.lang.Var/create nil)
          later (fn [& args] (swap! events conj [:later args]) (nth args 3))
          first-call (fn [& args] (swap! events conj [:first args])
                       (alter-var-root callback (constantly later)) (nth args 3))
          values (vec (range n)) carrier (if list? (apply list values) values)]
      (alter-var-root callback (constantly first-call))
      (is (= values (runtime/execute-values-scoped :runtime :plan :env carrier :reason scope
                    {:execute-value-scoped callback})))
      (is (= (vec (cons :first (repeat (dec n) :later))) (mapv first @events)))
      (is (= values (mapv #(nth (second %) 3) @events)))
      (doseq [[_ args] @events]
        (is (= [:runtime :plan :env] (vec (take 3 args))))
        (is (= :reason (nth args 4)))
        (is (identical? scope (nth args 5)))))))

(deftest map-carriers-retain-plan-entry-order-duplicate-keys-and-scope
  (doseq [list? [false true]]
    (let [events (atom []) scope (Object.)
          values {:k1 :a :v1 nil :k2 :a :v2 false}
          invoke (clojure.lang.Var/create
                   (fn [runtime plan env instruction received-scope]
                     (is (= [:runtime :plan :env] [runtime plan env]))
                     (is (identical? scope received-scope))
                     (swap! events conj instruction) (get values instruction)))
          ops (assoc (evaluation-operations) :execute-instruction-scoped invoke)
          entries [{:key :k1 :value :v1} {:key :k2 :value :v2}]
          carrier (if list? (apply list entries) entries)]
      (is (= {:a false} (runtime/execute-map-entries-scoped :runtime :plan :env carrier scope ops)))
      (is (= [:k1 :v1 :k2 :v2] @events))))
  (let [info (ex-info "unit map key" {:id :unit-key}) events (atom [])
        invoke (clojure.lang.Var/create (fn [& args] (swap! events conj (nth args 3)) (throw info)))
        ops (assoc (evaluation-operations) :execute-instruction-scoped invoke)]
    (is (identical? info (observed-error #(runtime/execute-map-entries-scoped nil nil nil
                                          [{:key :key :value :value}] nil ops))))
    (is (= [:key] @events))))

(deftest instruction-carriers-retain-recursion-failure-and-legacy-list-termination
  (doseq [list? [false true]]
    (let [events (atom []) signal (b/p15-s23-stage2-runtime-recur-signal [7]) scope (Object.)
          invoke (clojure.lang.Var/create
                   (fn [_ _ _ instruction received]
                     (is (identical? scope received)) (swap! events conj instruction)
                     (if (= :recur instruction) signal instruction)))
          ops (assoc (evaluation-operations) :execute-instruction-scoped invoke)
          carrier (fn [v] (if list? (apply list v) v))]
      (is (identical? signal (runtime/execute-instructions-scoped nil {} nil (carrier [:recur]) scope ops)))
      (reset! events [])
      (let [error (diagnostic #(runtime/execute-instructions-scoped nil {} nil (carrier [:recur :unreachable]) scope ops))]
        (is (= "L2-RECUR-TARGET" (:id error)))
        (is (= :non-tail-sequential-position (:reason error)))
        (is (= 1 (:actual-arity error))))
      (is (= [:recur] @events))))
  ;; Existing direct list callers stop at false/nil. Vector traversal retains
  ;; their callback behavior. Canonical admitted instruction streams are separate.
  (doseq [head [nil false] list? [false true]]
    (let [events (atom []) invoke (clojure.lang.Var/create
                                   (fn [_ _ _ instruction _] (swap! events conj instruction) instruction))
          body [head :later] carrier (if list? (apply list body) body)
          result (runtime/execute-instructions-scoped nil {} nil carrier nil
                   (assoc (evaluation-operations) :execute-instruction-scoped invoke))]
      (is (= (if list? nil :later) result))
      (is (= (if list? [] body) @events)))))

(deftest private-evaluation-wrappers-retain-signatures-and-late-stage-ports
  (is (= (:doc (meta (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-map-entries-scoped)))
         (:doc (meta #'runtime/execute-map-entries-scoped))))
  (doseq [[legacy stage args]
          [['p15-s23-stage2-runtime-bind-values #'runtime/bind-values '[base names values]]
           ['p15-s23-stage2-runtime-execute-value-scoped #'runtime/execute-value-scoped '[runtime plan env instruction reason public-invocation-scope]]
           ['p15-s23-stage2-runtime-execute-values-scoped #'runtime/execute-values-scoped '[runtime plan env instructions reason public-invocation-scope]]
           ['p15-s23-stage2-runtime-execute-map-entries-scoped #'runtime/execute-map-entries-scoped '[runtime plan env entries public-invocation-scope]]
           ['p15-s23-stage2-runtime-execute-instructions-scoped #'runtime/execute-instructions-scoped '[runtime plan env instructions public-invocation-scope]]]]
    (let [compatibility (ns-resolve 'gravity.bootstrap legacy) received (atom nil) marker (Object.)
          values (mapv (fn [_] (Object.)) args)]
      (is (true? (:private (meta compatibility))))
      (is (= (list args) (:arglists (meta compatibility))))
      (with-redefs-fn {stage (fn [& actual] (reset! received (vec actual)) marker)}
        #(is (identical? marker (apply (deref compatibility) values))))
      (is (= (if (= legacy 'p15-s23-stage2-runtime-bind-values)
                values (conj values (evaluation-operations))) @received)))))

(def ^:private instruction-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-instructions/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(deftest paired-instruction-programs-preserve-values-stdout-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @instruction-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected output] '[[literal 7 ""] [quoted (1 2) ""] [local-value 7 ""] [vector-value [1 2 3] ""] [set-value #{1 2} ""] [map-value {:a 1, :b 2} ""] [sequence-value 3 ""] [if-true 7 ""] [if-false 9 ""] [if-nil 9 ""] [if-zero 1 ""] [let-order [1 3] ""] [loop-value 6 ""] [function-value 8 ""] [get-two 7 ""] [get-three 9 ""] [count-value 2 ""] [str-one "7" ""] [unary-value 7 ""] [equal-two true ""] [assoc-three {:a 7} ""] [generic-two 5 ""] [generic-three 6 ""] [generic-zero "" ""] [print-zero nil "\n"] [print-one nil "7\n"] [print-two nil "a 7\n"] [print-three nil "a 7 b\n"] [sequence-output 7 "a\nb\n"] [argument-output [1 2] "a\nb\n"]]]
        (let [value (atom nil) stdout (with-out-str (reset! value (b/p15-s23-stage2-runtime-execute-function context plan callee [])))]
          (is (= expected @value) (str callee)) (is (= output stdout) (str callee))))
      (doseq [[callee builtin id output] '[[if-test / "L2-BUILTIN-ERROR" ""] [let-initializer + "L2-BUILTIN-ERROR" ""] [loop-initializer / "L2-BUILTIN-ERROR" ""] [bad-count count "L2-BUILTIN-ERROR" ""] [eager-get / "L2-BUILTIN-ERROR" ""] [bad-assoc assoc "L2-BUILTIN-ERROR" ""] [call-argument / "L2-BUILTIN-ERROR" ""] [vector-output / "L2-BUILTIN-ERROR" "before\n"] [print-argument / "L2-BUILTIN-ERROR" ""] [builtin-arity < "L2-BUILTIN-ARITY" ""]]]
        (let [error (atom nil) stdout (with-out-str (reset! error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))))]
          (is (= id (:id @error)) (str callee))
          (is (= builtin (:function @error)))
          (is (= {:source nil} (:source-span @error)))
          (is (= output stdout)))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @instruction-programs)))))))

(defn- instruction-operations []
  (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-instruction-operations)))

(deftest instruction-contract-and-data-ports-are-explicit
  (let [contract (:gravity/namespace-contract (meta #'runtime/execute-instruction-scoped))
        api (get-in contract [:public-api 'execute-instruction-scoped]) operations (instruction-operations)]
    (is (= (set (:operation-keys api)) (set (keys operations))))
    (is (= #{:artifact-println-function :artifact-println-two-function :artifact-function} (set (:data-operation-keys api))))
    (is (every? var? (vals operations)))
    (is (= :existing-callers (:instruction-admission-owner api)))
    (is (= :existing-seed-operations (:call-authority-owner api)))))

(deftest literal-quote-local-and-shape-refusals-retain-identity-and-facts
  (let [value (Object.) lookups (atom 0)
        none (reify clojure.lang.ILookup (valAt [_ _] (swap! lookups inc) nil))]
    (doseq [op [:literal :quote]]
      (is (identical? value (runtime/execute-instruction-scoped nil nil nil {:op op :value value} nil none))))
    (is (zero? @lookups)))
  (let [plan {:source {:path "unit.gravity"}} ops (instruction-operations)]
    (doseq [value [nil false]]
      (is (= value (runtime/execute-instruction-scoped nil plan {:x value} {:op :local :name :x} nil ops))))
    (doseq [[instruction id field value]
            [[{:op :local :name :missing} "L2-UNKNOWN-SYMBOL" :symbol :missing]
             [{:op :unknown} "L2-UNKNOWN-CORE-FORM" :operator :unknown]
             [{:op :throw :value nil} "L2-THROW-ILLEGAL" :reason :malformed-throw-instruction]
             [{:op :throw :value {:op :literal :value 1} :extra true} "L2-THROW-ILLEGAL" :reason :malformed-throw-instruction]]]
      (let [error (diagnostic #(runtime/execute-instruction-scoped nil plan {} instruction nil ops))]
        (is (= id (:id error))) (is (= value (get error field)))
        (is (= {:source "unit.gravity"} (:source-span error)))))))

(deftest source-throw-retains-actual-bootstrap-token-and-payload-identity
  ;; Direct trusted-host control evidence only, not source/plan admission or a
  ;; serializable target error ABI. No opaque token is rewritten for equality.
  (let [payload (Object.) plan {:source {:path "unit.gravity"} :module {:module 'unit}}
        error (observed-error #(runtime/execute-instruction-scoped nil plan {}
                                {:op :throw :value {:op :literal :value payload}} nil (instruction-operations)))]
    (is (instance? clojure.lang.ExceptionInfo error))
    (is (identical? payload (:payload (ex-data error))))
    (is (identical? (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-source-throw-token))
                   (:gravity.bootstrap/source-throw (ex-data error))))
    (is (= (:source plan) (:source (ex-data error))))
    (is (= 'unit (:module (ex-data error)))))
  (let [events (atom []) throw-operation (clojure.lang.Var/create (fn [& args] (swap! events conj [:original args]) :original))
        evaluate (clojure.lang.Var/create (fn [& _] (alter-var-root throw-operation (constantly (fn [& _] :changed))) :payload))]
    (is (= :original (runtime/execute-instruction-scoped nil :plan nil
                       {:op :throw :value {:op :literal :value 7}} nil
                       {:source-throw! throw-operation :execute-value-scoped evaluate})))
    (is (= [[:original '(:plan :payload)]] @events))))

(deftest if-and-let-forward-scopes-and-evaluate-only-ordered-required-instructions
  (doseq [condition [true false nil 0]]
    (let [events (atom []) scope (Object.)
          execute (clojure.lang.Var/create
                    (fn [_ _ _ instruction received]
                      (is (identical? scope received)) (swap! events conj instruction)
                      (case instruction :test condition :then :yes :else :no)))
          ops (assoc (instruction-operations) :execute-instruction-scoped execute)
          value (runtime/execute-instruction-scoped nil {} {} {:op :if :test :test :then :then :else :else} scope ops)]
      (is (= (if condition :yes :no) value))
      (is (= [:test (if condition :then :else)] @events))))
  (let [events (atom []) scope (Object.)
        execute (clojure.lang.Var/create
                  (fn [_ _ env instruction received]
                    (is (identical? scope received)) (swap! events conj [instruction env])
                    (case instruction :a 1 :b (inc (:a env)))))
        body (clojure.lang.Var/create (fn [_ _ env _ received] (is (identical? scope received)) env))
        result (runtime/execute-instruction-scoped nil {} {:retained 7}
                 {:op :let :bindings [{:name :a :expr :a} {:name :b :expr :b}] :body [:body]} scope
                 (assoc (instruction-operations) :execute-instruction-scoped execute :execute-instructions-scoped body))]
    (is (= {:retained 7 :a 1 :b 2} result))
    (is (= [[:a {:retained 7}] [:b {:retained 7 :a 1}]] @events))))

(deftest loop-recurrence-arity-refuses-before-rebinding
  (let [signal (b/p15-s23-stage2-runtime-recur-signal [1 2]) binds (atom 0)
        body (clojure.lang.Var/create (fn [& _] signal))
        bind (clojure.lang.Var/create (fn [& _] (swap! binds inc) :unexpected))
        error (diagnostic #(runtime/execute-instruction-scoped nil {} {}
                             {:op :loop :bindings [{:name :x :expr {:op :literal :value 1}}] :body []} nil
                             (assoc (instruction-operations) :execute-instructions-scoped body :bind-values bind)))]
    (is (= "L2-RECUR-TARGET" (:id error)))
    (is (= :loop-arity-mismatch (:reason error)))
    (is (= 1 (:target-arity error))) (is (= 2 (:actual-arity error)))
    (is (zero? @binds))))

(deftest println-evaluates-arguments-before-legality-and-does-not-publish-after-denial
  (let [events (atom []) denied (ex-info "unit effect denial" {:id :unit-denied})
        values (clojure.lang.Var/create (fn [& _] (swap! events conj :arguments) [7]))
        deny (clojure.lang.Var/create (fn [_] (swap! events conj :denied) (throw denied)))
        error (atom nil)
        stdout (with-out-str (reset! error (observed-error #(runtime/execute-instruction-scoped nil {} {}
                           {:op :println :args []} nil
                           (assoc (instruction-operations) :execute-values-scoped values :validate-module-effects! deny)))))]
    (is (identical? denied @error)) (is (= "" stdout))
    (is (= [:arguments :denied] @events))))

(deftest unary-artifact-exceptions-retain-their-distinct-boundary
  ;; A rejecting invocation port tests routing only; it never fakes successful
  ;; artifact authorization or supplies a source/admission proof.
  (let [error (ex-info "unit artifact denial" {:id :unit-artifact}) seen (atom []) host-errors (atom 0)
        invoke (clojure.lang.Var/create (fn [& args] (swap! seen conj args) (throw error)))
        evaluate (clojure.lang.Var/create (fn [& _] "value"))
        host-error (clojure.lang.Var/create (fn [& _] (swap! host-errors inc) :unexpected))
        format-name (clojure.lang.Var/create 'unit-format)
        observed (observed-error #(runtime/execute-instruction-scoped {:runtime-artifact-plan :unit-selector} {} {}
                       {:op :builtin-call :function 'str :args [{:op :literal :value "value"}]} nil
                       (assoc (instruction-operations) :execute-value-scoped evaluate :artifact-invoke invoke
                              :artifact-function format-name :fail-builtin-error! host-error)))]
    (is (identical? error observed)) (is (zero? @host-errors))
    (is (= 'unit-format (second (first @seen))))
    (is (= ["value"] (nth (first @seen) 2)))))

(deftest compiler-and-module-operation-denials-retain-arguments-and-scopes
  (let [events (atom []) scope (Object.) denied (ex-info "unit compiler denial" {:id :unit-denied})
        instruction {:op :builtin-call :function 'unit-compiler :args [{:op :literal :value 7}] :compiler-internal-caller nil}
        evaluate (clojure.lang.Var/create (fn [& _] (swap! events conj :argument) 7))
        deny (clojure.lang.Var/create (fn [& args] (swap! events conj args) (throw denied)))
        error (observed-error #(runtime/execute-instruction-scoped :runtime :plan :env instruction scope
                  (assoc (instruction-operations) :execute-value-scoped evaluate :invoke-compiler-internal-builtin deny)))]
    (is (identical? denied error)) (is (= :argument (first @events)))
    (is (= [:runtime :plan instruction [7] scope] (vec (second @events)))))
  (doseq [[op key] [[:function-call :execute-call-scoped] [:compiler-module-call :module-execute-call]]]
    (let [seen (atom nil) scope (Object.) instruction {:op op} denied (ex-info "unit call denial" {:id :unit-denied})
          deny (clojure.lang.Var/create (fn [& args] (reset! seen args) (throw denied)))]
      (is (identical? denied (observed-error #(runtime/execute-instruction-scoped :runtime :plan :env instruction scope
                                       (assoc (instruction-operations) key deny)))))
      (is (= [:runtime :plan :env instruction scope] (vec @seen))))))

(deftest instruction-compatibility-wrappers-preserve-arities-and-late-stage-port
  (let [private (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-instruction-scoped)
        seen (atom []) marker (Object.) values (vec (repeatedly 5 #(Object.)))]
    (is (true? (:private (meta private))))
    (is (= '([runtime plan env instruction public-invocation-scope]) (:arglists (meta private))))
    (is (= '([runtime plan env instruction]) (:arglists (meta #'b/p15-s23-stage2-runtime-execute-instruction))))
    (with-redefs [runtime/execute-instruction-scoped (fn [& args] (swap! seen conj (vec args)) marker)]
      (is (identical? marker (apply (deref private) values)))
      (is (identical? marker (apply b/p15-s23-stage2-runtime-execute-instruction (subvec values 0 4)))))
    (is (= (conj values (instruction-operations)) (first @seen)))
    (is (= (conj (subvec values 0 4) nil (instruction-operations)) (second @seen)))))

(def ^:private builtin-policy-programs
  (delay (mapv (fn [[extension disposition]]
                 (assoc (compile-source (str "bootstrap/clojure/fixtures/runtime-builtin-policy/" disposition extension))
                        :extension extension :disposition disposition))
               (for [extension [".gravity" ".qst"] disposition ["accepted" "rejected"]]
                 [extension disposition]))))

(deftest paired-builtin-policy-programs-preserve-values-refusals-and-identities
  (doseq [{:keys [path plan disposition]} @builtin-policy-programs]
    (is (= (str "sha256:" (b/sha256-hex (slurp path :encoding "UTF-8"))) (get-in plan [:source :sha256])))
    (is (= (:plan-id plan)
           (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
    (if (= "accepted" disposition)
      (doseq [[callee expected] '[[first-value 7] [second-value 9] [rest-value (2 3)] [rest-empty ()] [symbol-kind true] [keyword-kind true] [char-kind true] [number-kind true] [seq-kind true] [list-kind true] [vector-kind true] [map-kind true] [set-kind true] [string-kind true] [even-value true] [integer-kind true] [boolean-kind true] [keys-value (:a)] [set-value #{1 2}] [sorted (1 2 3)] [vectorized [1 2]] [generic-add 7] [generic-subtract -7] [generic-string "7"] [empty-map {}] [valid-lookup 7]]]
        (is (= expected (b/p15-s23-stage2-runtime-execute-function context plan callee [])) (str callee)))
      (doseq [[callee builtin id] '[[bad-first first "L2-BUILTIN-ERROR"] [bad-even even? "L2-BUILTIN-ERROR"] [bad-vector vec "L2-BUILTIN-ERROR"] [min-arity - "L2-BUILTIN-ARITY"] [exact-arity first "L2-BUILTIN-ARITY"] [between-short get "L2-BUILTIN-ARITY"] [between-long get "L2-BUILTIN-ARITY"] [even-arity hash-map "L2-BUILTIN-ARITY"] [pairs-arity assoc "L2-BUILTIN-ARITY"]]]
        (let [error (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan callee []))]
          (is (= id (:id error)) (str callee)) (is (= builtin (:function error)))
          (is (= {:source nil} (:source-span error)))))))
  (doseq [disposition ["accepted" "rejected"]]
    (is (apply = (map #(get-in % [:plan :plan-id])
                     (filter #(= disposition (:disposition %)) @builtin-policy-programs))))))

(defn- runtime-rule-operations []
  (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-rule-operations)))
(defn- runtime-unary-operations []
  (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-unary-operations)))
(def ^:private compiler-runtime-rule
  (delay (let [path (.getCanonicalPath (io/file "bootstrap/gravity/p15_s23/compiler.gravity"))
               data (b/p15-s23-compiler-source-form-record path)]
           {:path path :source data
            :definition (b/p15-s23-compiler-def-value path (:forms data) 'p15-s23-stage2-runtime-executor)})))

(deftest actual-compiler-source-rule-record-is-data-and-altered-rules-fail
  (let [{:keys [path source definition]} @compiler-runtime-rule ops (runtime-rule-operations)
        contract (:gravity/namespace-contract (meta #'runtime/executor-rule-record))
        record (runtime/executor-rule-record definition ops)]
    (is (.isFile (io/file path))) (is (seq (:source-text source)))
    (is (= (b/p15-s23-stage2-runtime-executor-rule-record definition) record))
    (is (= :complete (:status record)))
    (is (false? (get-in contract [:public-api 'executor-rule-record :completion-label-admits?])))
    (is (= (set (get-in contract [:public-api 'executor-rule-record :operation-keys])) (set (keys ops))))
    (is (every? var? (vals ops)))
    (doseq [changed [(update definition :instruction-rules dissoc :local)
                     (update-in definition [:builtin-rules :functions] #(vec (remove #{'+} %)))
                     (assoc-in definition [:builtin-rules :compiler-internal-builtin-arities] {})
                     (assoc-in definition [:builtin-rules :compiler-internal-builtin-contracts] {})
                     (assoc definition :engine :other) (assoc definition :input :other) (assoc definition :output :other)]]
      (let [result (runtime/executor-rule-record changed ops)]
        (is (= :failed (:status result))) (is (false? (:rule-set-complete? result)))
        (is (= (b/p15-s23-stage2-runtime-executor-rule-record changed) result))))))

(deftest rule-data-operations-are-selected-after-runtime-input-observation
  ;; Mutate only an anonymous unit Var, never a pin, guard or authority owner.
  (let [definition (:definition @compiler-runtime-rule)
        exact (clojure.lang.Var/create {})
        runtime-data (reify clojure.lang.ILookup
                       (valAt [_ key]
                         (when (= :instruction-rules key)
                           (alter-var-root exact (constantly (get-in definition [:builtin-rules :compiler-internal-builtin-arities]))))
                         (get definition key))
                       (valAt [_ key fallback] (get definition key fallback)))
        record (runtime/executor-rule-record runtime-data (assoc (runtime-rule-operations) :declared-builtin-exact-arities exact))]
    (is (true? (:declared-builtin-arities-current? record)))
    (is (= :complete (:status record)))))

(deftest all-arity-guards-preserve-signature-legality-and-returned-refusals
  (doseq [count-args (range 6)
          [guard parameters valid? expected]
          [[runtime/assert-min-arity! [2] (<= 2 count-args) "at least 2"]
           [runtime/assert-exact-arity! [2] (= 2 count-args) 2]
           [runtime/assert-between-arity! [2 3] (<= 2 count-args 3) "2 to 3"]
           [runtime/assert-even-arity! [] (even? count-args) "an even number of arguments"]]]
    (let [calls (atom []) marker (Object.) plan {:source {:path "unit.gravity"}} args (vec (range count-args))
          failure (clojure.lang.Var/create (fn [& values] (swap! calls conj (vec values)) marker))
          result (apply guard failure plan 'unit args parameters)]
      (if valid?
        (do (is (nil? result)) (is (empty? @calls)))
        (do (is (identical? marker result))
            (is (= [["L2-BUILTIN-ARITY" plan 'unit args expected]] @calls)))))))

(deftest arity-failure-var-selection-retains-original-evaluation-order
  (let [events (atom []) failure (clojure.lang.Var/create (fn [& _] :old))
        args (reify clojure.lang.Counted
               (count [_] (alter-var-root failure (constantly (fn [& values] (swap! events conj values) :new))) 0))]
    (is (= :new (runtime/assert-min-arity! failure {} 'unit args 1)))
    (is (= "L2-BUILTIN-ARITY" (ffirst @events))))
  (let [events (atom []) failure (clojure.lang.Var/create (fn [& values] (swap! events conj values) :original))
        args (reify clojure.lang.Counted (count [_] (alter-var-root failure (constantly (fn [& _] :changed))) 3))
        result (runtime/fail-call-arity! failure "L2-FUNCTION-ARITY" {:source {:path "unit.gravity"}} 'unit args 2)]
    (is (= :original result))
    (let [[id message data] (first @events)]
      (is (= "L2-FUNCTION-ARITY" id))
      (is (= "stage2 runtime function call has the wrong arity" message))
      (is (= {:source-span {:source "unit.gravity"} :function 'unit :expected-arity 2 :actual-arity 3
              :remediation "Call the function with the arity supported by the stage2 hosted-core runtime subset."} data)))))

(deftest unary-dispatch-retains-core-only-arms-fallback-and-exception-boundaries
  (let [ops (runtime-unary-operations) contract (:gravity/namespace-contract (meta #'runtime/hosted-unary-builtin))]
    (is (= (set (get-in contract [:public-api 'hosted-unary-builtin :operation-keys])) (set (keys ops))))
    (is (every? var? (vals ops))))
  (let [reads (atom 0) none (reify clojure.lang.ILookup (valAt [_ _] (swap! reads inc) nil))]
    (is (= 7 (runtime/hosted-unary-builtin nil nil 'first [7] none))) (is (zero? @reads)))
  (let [seen (atom []) marker (Object.)
        invoke (clojure.lang.Var/create (fn [& args] (swap! seen conj args) marker))]
    (is (identical? marker (runtime/hosted-unary-builtin :runtime :plan '+ 7 {:invoke-builtin invoke})))
    (is (= '(:runtime :plan + [7]) (first @seen))))
  (let [info (ex-info "unit unary" {:id :unit-unary}) errors (atom 0)
        rest (clojure.lang.Var/create (fn [_] (throw info)))
        failure (clojure.lang.Var/create (fn [& _] (swap! errors inc) :unexpected))]
    (is (identical? info (observed-error #(runtime/hosted-unary-builtin nil nil 'rest []
                                          {:normalized-rest rest :fail-builtin-error! failure}))))
    (is (zero? @errors)))
  (let [events (atom []) failure (clojure.lang.Var/create (fn [& _] :old)) host-error (Exception. "unit host")
        rest (clojure.lang.Var/create
               (fn [_] (alter-var-root failure (constantly (fn [& values] (swap! events conj values) :new)))
                 (throw host-error)))]
    (is (= :new (runtime/hosted-unary-builtin nil :plan 'rest [] {:normalized-rest rest :fail-builtin-error! failure})))
    (is (= [:plan 'rest host-error] (vec (first @events))))))

(deftest builtin-policy-compatibility-wrappers-retain-signatures-and-late-ports
  (doseq [[legacy stage args prefix suffix]
          [['p15-s23-stage2-runtime-executor-rule-record #'runtime/executor-rule-record '[runtime] nil (runtime-rule-operations)]
           ['p15-s23-stage2-runtime-fail-call-arity! #'runtime/fail-call-arity! '[id plan callee args expected] #'b/fail! nil]
           ['p15-s23-stage2-runtime-assert-min-arity! #'runtime/assert-min-arity! '[plan callee args n] #'b/p15-s23-stage2-runtime-fail-call-arity! nil]
           ['p15-s23-stage2-runtime-assert-exact-arity! #'runtime/assert-exact-arity! '[plan callee args n] #'b/p15-s23-stage2-runtime-fail-call-arity! nil]
           ['p15-s23-stage2-runtime-assert-between-arity! #'runtime/assert-between-arity! '[plan callee args min-n max-n] #'b/p15-s23-stage2-runtime-fail-call-arity! nil]
           ['p15-s23-stage2-runtime-assert-even-arity! #'runtime/assert-even-arity! '[plan callee args] #'b/p15-s23-stage2-runtime-fail-call-arity! nil]
           ['p15-s23-stage2-runtime-invoke-unary-or-generic-builtin #'runtime/hosted-unary-builtin '[runtime plan function value] nil (runtime-unary-operations)]]]
    (let [compatibility (ns-resolve 'gravity.bootstrap legacy) values (mapv (fn [_] (Object.)) args)
          seen (atom nil) marker (Object.)]
      (is (= (list args) (:arglists (meta compatibility))))
      (is (= (= legacy 'p15-s23-stage2-runtime-invoke-unary-or-generic-builtin) (true? (:private (meta compatibility)))))
      (with-redefs-fn {stage (fn [& actual] (reset! seen (vec actual)) marker)}
        #(is (identical? marker (apply (deref compatibility) values))))
      (is (= (cond-> (if prefix (into [prefix] values) values) suffix (conj suffix)) @seen)))))

(def ^:private control-positive
 '[[generic-empty ""] [generic-many 10] [generic-vector [1 2 3 4]]
   [ordinary-call [7 8]] [nil-call [nil false]] [call-order [7 8]]
   [generic-order [1 2 3]] [function-recur 15] [loop-recur 10]
   [nested-call [6 3]] [control-data {:a nil :b false :c 7}] [loop-nil nil]])
(def ^:private control-output
 '{call-order "left\nright\n" generic-order "a\nb\nc\n"})
(def ^:private control-negative
 '[[builtin-argument / "L2-BUILTIN-ERROR" ""]
   [function-argument / "L2-BUILTIN-ERROR" ""]
   [function-order / "L2-BUILTIN-ERROR" "before\n"]
   [generic-order / "L2-BUILTIN-ERROR" "before\n"]
   [bad-operand + "L2-BUILTIN-ERROR" ""]
   [bad-arity pair "L2-FUNCTION-ARITY" ""]])

(deftest paired-control-source-retains-values-refusals-and-evaluation-order
 (doseq [extension [".gravity" ".qst"]]
  (let [{:keys [plan]} (compile-source (str "bootstrap/clojure/fixtures/runtime-control/accepted" extension))]
   (is (= (:plan-id plan) (str "sha256:" (b/sha256-hex (pr-str (b/c-backend-canonical-value (dissoc plan :plan-id)))))))
   (doseq [[function expected] control-positive]
    (let [value (atom nil)
          output (with-out-str (reset! value (b/p15-s23-stage2-runtime-execute-function context plan function [])))]
     (is (= expected @value) (str function extension))
     (is (= (get control-output function "") output) (str function extension)))))
  (let [{:keys [plan]} (compile-source (str "bootstrap/clojure/fixtures/runtime-control/rejected" extension))]
   (doseq [[function builtin id expected-output] control-negative]
    (let [failure (atom nil)
          output (with-out-str (reset! failure (diagnostic #(b/p15-s23-stage2-runtime-execute-function context plan function []))))]
     (is (= id (:id @failure)) (str function extension))
     (is (= builtin (:function @failure)) (str function extension))
     (is (= expected-output output) (str function extension)))))))

(deftest map-entry-preserves-native-equality-nil-values-and-sorted-comparator-isolation
 (let [comparisons (atom [])
       values (sorted-map-by (fn [left right] (swap! comparisons conj [left right]) (compare left right)) "a" nil "b" false)]
  (reset! comparisons [])
  (is (nil? (runtime/map-entry values :control)))
  (is (= ["a" nil] (runtime/map-entry values "a")))
  (is (= ["b" false] (runtime/map-entry values "b")))
  (is (empty? @comparisons)))
 (doseq [values [{:a nil :b false} (hash-map :a nil :b false)]]
  (is (= [:a nil] (runtime/map-entry values :a)))
  (is (= [:b false] (runtime/map-entry values :b)))
  (is (nil? (runtime/map-entry values :missing))))
 (doseq [value [nil false 7 [:a nil] '(:a nil)]]
  (is (nil? (runtime/map-entry value :a))))
 (let [wrapper (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-map-entry)]
  (is (= [:a nil] (wrapper {:a nil} :a)))
  (is (:private (meta wrapper)))))

(deftest recurrence-facts-and-nontail-check-retain-failure-order-and-original-value
 (let [plan {:source {:path "original.gravity"}} received (atom nil)
       fail (clojure.lang.Var/create (fn [& args] (reset! received args) :refused))]
  (is (= :refused (runtime/fail-recur! fail plan 2 3 :wrong-target)))
  (is (= ["L2-RECUR-TARGET" "recur has no compatible loop or function target"
          {:source-span {:source "original.gravity"} :target-arity 2 :actual-arity 3 :reason :wrong-target
           :remediation "Use recur only in tail position inside a compatible loop or function with matching arity."}]
         @received))
  (let [wrapper-result (atom nil)]
   (with-redefs-fn {#'b/fail! (fn [& args] (reset! wrapper-result args))}
     #(b/p15-s23-stage2-runtime-recur-fail! plan 2 3 :wrong-target))
   (is (= @received @wrapper-result))))
 (let [value (Object.) predicate (clojure.lang.Var/create (constantly false))]
  (is (identical? value (runtime/nontail-value! {} value :reason {:recur-signal? predicate}))))
 (let [signal (b/p15-s23-stage2-runtime-recur-signal [nil false])
       data (diagnostic #(b/p15-s23-stage2-runtime-nontail-value! {:source {:path "actual.qst"}} signal :nontail))]
  (is (= "L2-RECUR-TARGET" (:id data)))
  (is (= :nontail (:reason data)))
  (is (= 2 (:actual-arity data)))
  (is (nil? (:target-arity data))))
 (let [events (atom []) signal (Object.) fail (clojure.lang.Var/create (fn [& _] (swap! events conj :old)))
       values (clojure.lang.Var/create
               (fn [_] (swap! events conj :values)
                 (alter-var-root fail (constantly (fn [& _] (swap! events conj :too-late)))) [1]))
       predicate (clojure.lang.Var/create
                  (fn [value] (is (identical? signal value)) (swap! events conj :predicate)
                    (alter-var-root fail (constantly (fn [& _] (swap! events conj :selected)))) true))]
  (is (identical? signal (runtime/nontail-value! {} signal :reason {:recur-signal? predicate :recur-values values :recur-fail! fail})))
  (is (= [:predicate :values :selected] @events))))

(defn- generic-call-operations []
 ((fn [name] (var-get (ns-resolve 'gravity.bootstrap name))) 'p15-s23-runtime-generic-call-operations))
(defn- ordinary-call-operations []
 (var-get (ns-resolve 'gravity.bootstrap 'p15-s23-runtime-call-operations)))

(deftest call-operation-inventories-and-compatibility-signatures-remain-explicit
 (doseq [[name operations expected]
         [['execute-generic-builtin-call-scoped (generic-call-operations)
           '([runtime plan env instruction function public-invocation-scope])]
          ['execute-call-scoped (ordinary-call-operations)
           '([runtime plan env instruction public-invocation-scope])]]]
  (let [stage-var (ns-resolve 'gravity.runtime name)
        wrapper (ns-resolve 'gravity.bootstrap (symbol (str "p15-s23-stage2-runtime-" name)))
        contract (:gravity/namespace-contract (meta stage-var))]
   (is (= (set (get-in contract [:public-api name :operation-keys])) (set (keys operations))))
   (is (every? var? (vals operations)))
   (is (= expected (:arglists (meta wrapper))))
   (is (:private (meta wrapper))))))

(deftest generic-call-evaluates-before-late-dispatch-and-preserves-refusal-identity
 ;; These anonymous Vars test sequencing only; they never enter admission.
 (let [events (atom []) scope (Object.) args [1 2 3]
       invoke (clojure.lang.Var/create (fn [& _] :obsolete))
       evaluate (clojure.lang.Var/create
                  (fn [& received] (swap! events conj [:evaluate received])
                    (alter-var-root invoke (constantly (fn [& received] (swap! events conj [:invoke received]) :value))) args))
       instruction {:args [:a :b :c]}]
  (is (= :value (runtime/execute-generic-builtin-call-scoped :runtime :plan :env instruction '+ scope
                  {:execute-values-scoped evaluate :invoke-builtin invoke})))
  (is (= [[:evaluate [:runtime :plan :env [:a :b :c] :recur-inside-builtin-argument scope]]
          [:invoke [:runtime :plan '+ args instruction scope]]] @events)))
 (doseq [count [1 2]]
  (let [formatter (clojure.lang.Var/create 'obsolete)
        evaluate (clojure.lang.Var/create (fn [& _] (alter-var-root formatter (constantly 'current-formatter)) (vec (range count))))
        seen (atom nil) invoke (clojure.lang.Var/create (fn [& args] (reset! seen args) :formatted))]
   (is (= :formatted (runtime/execute-generic-builtin-call-scoped {:runtime-artifact-plan :trusted-data} :plan :env {:args []} 'str nil
                     {:execute-values-scoped evaluate :artifact-invoke invoke :artifact-function formatter :artifact-concat-function formatter})))
   (is (= [{:runtime-artifact-plan :trusted-data} 'current-formatter (vec (range count))] @seen))))
 (let [error (ex-info "denied compiler call" {}) events (atom [])
       evaluate (clojure.lang.Var/create (fn [& _] (swap! events conj :arguments) [7]))
       deny (clojure.lang.Var/create (fn [& _] (swap! events conj :denied) (throw error)))]
  (is (identical? error (observed-error #(runtime/execute-generic-builtin-call-scoped nil {} nil
                                         {:args [] :compiler-internal-caller nil} '+ nil
                                         {:execute-values-scoped evaluate :invoke-compiler-internal-builtin deny}))))
  (is (= [:arguments :denied] @events)))
 (let [error (IllegalArgumentException. "argument failed") evaluate (clojure.lang.Var/create (fn [& _] (throw error)))]
  (is (identical? error (observed-error #(runtime/execute-generic-builtin-call-scoped nil {} nil {:args []} '+ nil {:execute-values-scoped evaluate}))))))

(deftest ordinary-call-authorizes-before-arguments-and-preserves-scope-and-failure
 ;; Authority ports deny in host units; real source controls use live owners.
 (let [scope (Object.) events (atom []) error (ex-info "external denied" {})
       deny (clojure.lang.Var/create (fn [& args] (swap! events conj [:deny args]) (throw error)))]
  (is (identical? error (observed-error #(runtime/execute-call-scoped :runtime :plan :env {:function 'f :external-link nil} scope
                                         {:authorize-external-link deny}))))
  (is (= [[:deny [:runtime :plan {:function 'f :external-link nil}]]] @events)))
 (doseq [external? [false true]]
  (let [scope (Object.) events (atom []) error (ex-info "argument failure" {})
        authorize (clojure.lang.Var/create (fn [& _] (swap! events conj :authorize) nil))
        evaluate (clojure.lang.Var/create (fn [& _] (swap! events conj :arguments) (throw error)))
        instruction (cond-> {:function 'f :args [:a]} external? (assoc :external-link nil))]
   (is (identical? error (observed-error #(runtime/execute-call-scoped nil {} nil instruction scope
                                          {:authorize-external-link authorize :execute-values-scoped evaluate}))))
   (is (= (if external? [:authorize :arguments] [:arguments]) @events))))
 (let [scope (Object.) seen (atom nil) invoke (clojure.lang.Var/create (fn [& _] :obsolete))
       evaluate (clojure.lang.Var/create
                  (fn [& args] (is (= [:runtime :plan :env [:a] :recur-inside-function-argument scope] args))
                    (alter-var-root invoke (constantly (fn [& args] (reset! seen args) :called))) [7]))]
  (is (= :called (runtime/execute-call-scoped :runtime :plan :env {:function 'f :args [:a]} scope
                   {:execute-values-scoped evaluate :execute-function-scoped invoke})))
  (is (= [:runtime :plan 'f [7] scope] @seen))))

(deftest generic-formatter-refusals-and-call-exceptions-remain-outside-host-translation
 (doseq [count [0 3]]
  (let [seen (atom nil) args (vec (range count))
        evaluate (clojure.lang.Var/create (fn [& _] args))
        failure (ex-info "formatter arity" {})
        deny (clojure.lang.Var/create (fn [& data] (reset! seen data) (throw failure)))]
   (is (identical? failure
        (observed-error #(runtime/execute-generic-builtin-call-scoped
                           {:runtime-artifact-plan :trusted-data} :plan :env {:args []} 'str nil
                           {:execute-values-scoped evaluate :fail-call-arity! deny}))))
   (is (= ["L2-BUILTIN-ARITY" :plan 'str args "1 or 2"] @seen))))
 (doseq [failure [(ex-info "original" {}) (IllegalArgumentException. "original")]]
  (let [evaluate (clojure.lang.Var/create (fn [& _] [7]))
        deny (clojure.lang.Var/create (fn [& _] (throw failure)))]
   (is (identical? failure
        (observed-error #(runtime/execute-generic-builtin-call-scoped
                           {:runtime-artifact-plan :trusted-data} {} nil {:args []} 'str nil
                           {:execute-values-scoped evaluate :artifact-function (clojure.lang.Var/create 'formatter)
                            :artifact-invoke deny}))))
   (is (identical? failure
        (observed-error #(runtime/execute-call-scoped nil {} nil {:function 'f :args []} nil
                           {:execute-values-scoped evaluate :execute-function-scoped deny})))))))
