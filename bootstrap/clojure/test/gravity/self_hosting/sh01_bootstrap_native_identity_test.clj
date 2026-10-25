(ns gravity.self-hosting.sh01-bootstrap-native-identity-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]))

(def ^:private operation 'bootstrap-native-identical?)
(def ^:private runtime {:engine :bootstrap-native-identity-test})
(def ^:private plan
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:profile :meta :target :jvm} :source {:path "native-identity-test.gravity"}})
(defn- generic [active args]
  (b/p15-s23-stage2-runtime-invoke-builtin active operation args))
(defn- interpreted [active args]
  (b/p15-s23-stage2-runtime-execute-instruction runtime active {}
    {:op :builtin-call :function operation :args (mapv #(hash-map :op :literal :value %) args)}))
(defn- outcome [thunk]
  (try {:value (thunk)} (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))

(deftest native-identity-distinguishes-reference-aliases-from-equal-values
  (let [left (vector :equal) right (vector :equal)
        text (String. "equal") other-text (String. "equal")]
    (doseq [[a b expected] [[nil nil true] [nil false false] [false false true]
                             [left left true] [left right false]
                             [text text true] [text other-text false]]]
      (is (identical? expected (generic plan [a b])))
      (is (identical? expected (interpreted plan [a b]))))))

(deftest native-identity-does-not-inspect-operands
  (let [calls (atom 0)
        deny #(do (swap! calls inc) (throw (AssertionError. "native identity callback")))
        opaque (proxy [Object clojure.lang.IObj clojure.lang.Named] []
                 (hashCode [] (deny)) (equals [_] (deny)) (toString [] (deny))
                 (meta [] (deny)) (withMeta [_] (deny))
                 (getName [] (deny)) (getNamespace [] (deny)))
        delayed (lazy-seq (deny) (list :unreachable))
        physical (object-array [:self nil])
        cycle (clojure.lang.PersistentArrayMap. physical)]
    (aset physical 1 cycle)
    (doseq [value [opaque delayed cycle
                   (clojure.lang.PersistentArrayMap. nil)
                   (clojure.lang.PersistentArrayMap. (object-array [:unpaired]))]]
      (is (true? (generic plan [value value])))
      (is (true? (interpreted plan [value value])))
      (is (false? (generic plan [value nil])))
      (is (false? (interpreted plan [value nil]))))
    (is (zero? @calls))))

(deftest native-identity-requires-exact-arity-and-compiler-artifact-context
  (doseq [active [plan {} (assoc plan :compiler-artifact-plan? false)
                  (assoc plan :kind :gravity/stage2-instruction-plan)
                  (assoc-in plan [:module :profile] :hosted)
                  (assoc-in plan [:module :target] :wasm)
                  (update plan :module dissoc :target)]]
    (doseq [args [[] [nil] [nil nil nil]]]
      (let [direct (outcome #(generic active args))]
        (is (= "L2-BUILTIN-ARITY" (get-in direct [:error :id])))
        (is (= direct (outcome #(interpreted active args))))))
    (when-not (= active plan)
      (let [direct (outcome #(generic active [nil nil]))]
        (is (= "L2-BUILTIN-ERROR" (get-in direct [:error :id])))
        (is (= direct (outcome #(interpreted active [nil nil])))))))
  (let [seam (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-value-scoped)
        original @seam calls (atom 0)]
    (with-redefs-fn {seam (fn [& args] (swap! calls inc) (apply original args))}
      #(is (= "L2-BUILTIN-ERROR"
              (get-in (outcome (fn [] (interpreted {} [nil nil]))) [:error :id]))))
    (is (= 2 @calls)))
  (is (= "L2-UNKNOWN-SYMBOL"
         (get-in (outcome #(b/p15-s23-stage2-runtime-execute-instruction runtime {} {}
                           {:op :builtin-call :function operation
                            :args [{:op :local :name 'missing} {:op :literal :value nil}]}))
                 [:error :id]))))

(deftest native-identity-compiler-artifact-fixtures-execute-both-spellings
  (let [root (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh01_bootstrap_native_identity_test.clj")))]
               (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))
        paths (mapv #(str (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/bootstrap-native-identity." %)))
                    ["gravity" "qst"])]
    (is (apply = (map slurp paths)))
    (doseq [source paths]
      (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))
            invoke #(b/p15-s23-stage2-runtime-execute-function runtime compiled %1 %2)
            value (vector :alias) equal (vector :alias)]
        (is (true? (invoke 'same-object? [value value])))
        (is (false? (invoke 'same-object? [value equal])))
        (is (= "L2-BUILTIN-ARITY" (get-in (outcome #(invoke 'wrong-arity [])) [:error :id])))
        (is (not (contains? (set (get-in emitter [:call-rules :builtin-functions])) operation)))))))
