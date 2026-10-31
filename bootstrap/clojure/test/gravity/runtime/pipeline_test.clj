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
  (is (= #{'execute-function-scoped 'execute-function 'artifact-invoke 'execute-plan}
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
