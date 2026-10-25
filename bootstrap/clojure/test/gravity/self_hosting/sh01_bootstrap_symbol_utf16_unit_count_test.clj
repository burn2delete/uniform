(ns gravity.self-hosting.sh01-bootstrap-symbol-utf16-unit-count-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b])
  (:import (java.lang.reflect InvocationHandler Proxy)))

;; BOOT2 temporary ABI, not a Gravity String/Unicode or public Core operation.
(def ^:private operation 'bootstrap-symbol-utf16-unit-count)
(def ^:private runtime {:engine :bootstrap-symbol-unit-test})
(def ^:private plan
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:profile :meta :target :jvm}
   :source {:path "bootstrap-symbol-unit-test.gravity"}})
(def ^:private root
  (delay (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh01_bootstrap_symbol_utf16_unit_count_test.clj")))]
           (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- fixture [extension]
  (str (io/file @root (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/bootstrap-symbol-units." extension))))
(defn- generic [active arguments]
  (b/p15-s23-stage2-runtime-invoke-builtin active operation arguments))
(defn- interpreted [active arguments]
  (b/p15-s23-stage2-runtime-execute-instruction
   runtime active {} {:op :builtin-call :function operation
                      :args (mapv #(hash-map :op :literal :value %) arguments)}))
(defn- outcome [thunk]
  (try {:value (thunk)} (catch clojure.lang.ExceptionInfo failure {:error (ex-data failure)})))

(deftest bootstrap-symbol-native-spelling-units-match-seed-rendering
  (doseq [[namespace name] [[nil "a"] ["ab" "c"] [nil ""] ["" ""] ["" "x"] ["ns" ""]
                            [nil "a/b"] ["n/s" "x/y"] [nil "\ud83d\ude00"]
                            ["\ud83d\ude00" "x"] [nil "\ud800"] [nil "\udc00"]
                            ["\ud800" "\udc00"] [nil nil] ["ns" nil] ["" nil]]]
    (let [value (clojure.lang.Symbol/intern namespace name)
          expected {:value (count (str value))}]
      (is (= expected (outcome #(generic plan [value]))))
      (is (= expected (outcome #(interpreted plan [value])))))))

(deftest bootstrap-symbol-abi-rejects-wrong-arity-type-and-context
  (doseq [active [plan {} (dissoc plan :compiler-artifact-plan?)
                  (assoc plan :compiler-artifact-plan? false)
                  (assoc plan :kind :gravity/stage2-instruction-plan)
                  (assoc-in plan [:module :profile] :hosted)
                  (assoc-in plan [:module :profile] :core)
                  (assoc-in plan [:module :target] :wasm)
                  (update plan :module dissoc :target)]]
    (doseq [arguments [[] ['a 'b]]]
      (let [direct (outcome #(generic active arguments)) lowered (outcome #(interpreted active arguments))]
        (is (= "L2-BUILTIN-ARITY" (get-in direct [:error :id])))
        (is (= direct lowered))))
    (when-not (= active plan)
      (let [direct (outcome #(generic active ['valid]))]
        (is (= "L2-BUILTIN-ERROR" (get-in direct [:error :id])))
        (is (= direct (outcome #(interpreted active ['valid])))))))
  (doseq [value [nil false 7 "symbol" :symbol [] {}]]
    (let [direct (outcome #(generic plan [value]))]
      (is (= "L2-BUILTIN-ERROR" (get-in direct [:error :id])))
      (is (= direct (outcome #(interpreted plan [value])))))))

(deftest bootstrap-symbol-abi-never-inspects-foreign-values-or-metadata
  (let [calls (atom 0)
        forbidden (fn [] (swap! calls inc) (throw (AssertionError. "operand callback")))
        foreign (proxy [Object clojure.lang.Named] []
                  (getName [] (forbidden)) (getNamespace [] (forbidden))
                  (toString [] (forbidden)) (hashCode [] (forbidden))
                  (equals [_] (forbidden)))
        metadata (Proxy/newProxyInstance
                  (.getClassLoader clojure.lang.IPersistentMap)
                  (into-array Class [clojure.lang.IPersistentMap])
                  (reify InvocationHandler (invoke [_ _ _ _] (forbidden))))
        value (.withMeta ^clojure.lang.Symbol (symbol "ns" "name") metadata)]
    (doseq [execute [generic interpreted]]
      (doseq [active [plan (assoc-in plan [:module :profile] :hosted)]]
        (is (= "L2-BUILTIN-ERROR" (get-in (outcome #(execute active [foreign])) [:error :id]))))
      (is (= {:value 7} (outcome #(execute plan [value])))))
    (is (zero? @calls))))

(deftest bootstrap-symbol-compiler-artifact-augmentation-is-usable-and-contained
  (is (= (slurp (fixture "gravity")) (slurp (fixture "qst"))))
  (doseq [extension ["gravity" "qst"]]
    (let [source (fixture extension)
          emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))
          compiled (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))
          invoke #(b/p15-s23-stage2-runtime-execute-function runtime compiled %1 %2)]
      (is (= :meta (get-in compiled [:module :profile])))
      (is (= :jvm (get-in compiled [:module :target])))
      (is (not (contains? (set (get-in emitter [:call-rules :builtin-functions])) operation)))
      (is (= "L2-UNKNOWN-CORE-FORM"
             (get-in (outcome #(b/p15-s23-stage2-seed-compile-expr
                               emitter {:source-path source} #{} (list operation (list 'quote 'a))))
                     [:error :id])))
      (is (= {:value 7} (outcome #(invoke 'observe ['ns/name]))))
      (let [result (atom nil) output (with-out-str (reset! result (invoke 'observe-once ['ns/name])))]
        (is (= 7 @result)) (is (= "once\n" output)))
      (doseq [[function args id] [['observe-zero [] "L2-BUILTIN-ARITY"]
                                 ['observe-two ['x] "L2-BUILTIN-ARITY"]
                                 ['observe-wrong [] "L2-BUILTIN-ERROR"]]]
        (is (= id (get-in (outcome #(invoke function args)) [:error :id])))))))

(deftest bootstrap-symbol-operand-evaluation-precedes-abi-check-once
  (let [execute (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-value-scoped)
        original @execute calls (atom 0)
        result (with-redefs-fn
                 {execute (fn [& args] (swap! calls inc) (apply original args))}
                 #(outcome (fn [] (interpreted {} ['a]))))]
    (is (= "L2-BUILTIN-ERROR" (get-in result [:error :id])))
    (is (= 1 @calls)))
  (let [result (outcome #(b/p15-s23-stage2-runtime-execute-instruction
                         runtime {} {} {:op :builtin-call :function operation
                                        :args [{:op :local :name 'missing}]}))]
    (is (= "L2-UNKNOWN-SYMBOL" (get-in result [:error :id])))))
