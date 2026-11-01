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
           'hosted-str 'fail-builtin-error! 'hosted-get}
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
        declared (set (mapcat :operation-keys (vals (:public-api contract))))
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
