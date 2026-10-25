(ns gravity.self-hosting.sh01-bootstrap-native-carrier-header-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b])
  (:import (java.lang.reflect InvocationHandler Proxy)
           (java.math BigInteger)))

;; BOOT2: six descriptive slots, not DATA validation or an admission token.
(def ^:private operation 'bootstrap-native-carrier-header)
(def ^:private unsupported [:unsupported nil nil nil nil nil])
(def ^:private runtime {:engine :bootstrap-native-header-test})
(def ^:private plan
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:profile :meta :target :jvm} :source {:path "native-header-test.gravity"}})
(defn- generic [active arguments]
  (b/p15-s23-stage2-runtime-invoke-builtin active operation arguments))
(defn- interpreted [active arguments]
  (b/p15-s23-stage2-runtime-execute-instruction runtime active {}
    {:op :builtin-call :function operation :args (mapv #(hash-map :op :literal :value %) arguments)}))
(defn- outcome [thunk]
  (try {:value (thunk)} (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))
(defn- observe [value] (generic plan [value]))
(defn- private-var [name] (ns-resolve 'gravity.bootstrap name))
(defn- forbidden [calls]
  (fn [] (swap! calls inc) (throw (AssertionError. "unpaid native-header callback"))))
(defn- opaque-map [calls]
  (Proxy/newProxyInstance (.getClassLoader clojure.lang.IPersistentMap)
    (into-array Class [clojure.lang.IPersistentMap clojure.lang.IObj])
    (reify InvocationHandler (invoke [_ _ _ _] ((forbidden calls))))))

(deftest native-header-observes-required-native-families
  (doseq [[value expected]
          [[nil [:nil false nil nil nil nil]]
           [true [:boolean false nil nil nil nil]]
           [\a [:character false nil nil nil nil]]
           [(int 7) [:integer32 false nil 3 nil nil]]
           [(long 7) [:integer64 false nil 3 nil nil]]
           ["text" [:string false nil 4 nil nil]]
           ['ns/name [:symbol false nil 7 true nil]]
           [:ns/name [:keyword false nil 8 nil nil]]
           [(array-map :a 1) [:map false 1 nil nil nil]]
           [(hash-map :a 1) [:map false 1 nil nil nil]]
           [[1 2] [:vector false 2 nil nil nil]]
           ['() [:empty-list false 0 nil nil nil]]]]
    (is (= expected (observe value)))
    (is (= expected (interpreted plan [value]))))
  (let [value #{:a :b} header (observe value)]
    (is (= [:set false 2 nil] (subvec header 0 4)))
    (is (identical? clojure.lang.PersistentHashMap (class (nth header 4))))
    (is (nil? (nth header 5)))
    (is (= header (interpreted plan [value]))))
  (let [header (observe (sorted-map :a 1 :b 2))]
    (is (= [:tree-map false 2 nil :default-comparator nil] header))))

(deftest native-header-symbol-name-presence-distinguishes-null-from-empty
  (doseq [[namespace name units present?]
          [[nil nil 0 false] [nil "" 0 true] ["" nil 5 false] ["" "" 1 true]
           ["ns" nil 7 false] ["ns" "" 3 true] ["ns" "name" 7 true]
           ["unicode" "\ud83d\ude00" 10 true]]
          metadata? [false true]]
    (let [symbol (clojure.lang.Symbol/intern namespace name)
          value (if metadata? (with-meta symbol {:name-present? (not present?)}) symbol)
          expected [:symbol metadata? nil units present? nil]
          header (observe value)]
      (is (= expected header))
      (is (= expected (interpreted plan [value])))
      (is (boolean? (nth header 4)))
      (is (nil? (nth header 5)))
      (is (= units (b/p15-s23-stage2-runtime-invoke-builtin
                    plan 'bootstrap-symbol-utf16-unit-count [value]))))))

(deftest native-header-scalar-metrics-do-not-render-or-normalize
  (doseq [[value bits]
          [[(int 0) 0] [Integer/MIN_VALUE 32] [Integer/MAX_VALUE 31]
           [Long/MIN_VALUE 64] [Long/MAX_VALUE 63]
           [BigInteger/ZERO 0] [(.negate (.shiftLeft BigInteger/ONE 4096)) 4097]]]
    (is (= bits (nth (observe value) 3)))
    (is (= (observe value) (interpreted plan [value]))))
  (doseq [value [1/2 (clojure.lang.Ratio. (.negate (.shiftLeft BigInteger/ONE 512))
                                      (.shiftLeft BigInteger/ONE 257))]]
    (let [header (observe value)]
      (is (= :ratio (nth header 0)))
      (is (= (.bitLength (.abs ^BigInteger (.-numerator ^clojure.lang.Ratio value))) (nth header 3)))
      (is (= (.bitLength (.abs ^BigInteger (.-denominator ^clojure.lang.Ratio value))) (nth header 4)))
      (is (= header (interpreted plan [value])))))
  (let [renders (atom 0) seam (private-var 'p15-s23-stage2-runtime-str) original @seam]
    (with-redefs-fn
      {seam (fn [args] (swap! renders inc) (apply original [args]))}
      #(doseq [value ["\ud83d\ude00" "\ud800" 'ns/name :ns/name]]
         (is (= (observe value) (interpreted plan [value])))))
    (is (zero? @renders))))

(deftest native-header-does-not-inspect-foreign-values-or-native-metadata
  (let [calls (atom 0) deny (forbidden calls)
        foreign-map (opaque-map calls)
        foreign-number (proxy [Number] []
                         (intValue [] (deny)) (longValue [] (deny))
                         (floatValue [] (deny)) (doubleValue [] (deny))
                         (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        foreign (proxy [Object clojure.lang.Named] []
                  (getName [] (deny)) (getNamespace [] (deny))
                  (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        delayed (lazy-seq (deny) (list :unreachable))]
    (doseq [value [foreign-map foreign-number foreign delayed]]
      (is (= unsupported (observe value)))
      (is (= unsupported (interpreted plan [value]))))
    (doseq [value [(with-meta 'ns/name foreign-map)
                   (with-meta [1] foreign-map)
                   (with-meta (array-map :a 1) foreign-map)]]
      (let [header (observe value)]
        (is (true? (nth header 1)))
        (is (= 6 (count header)))
        (is (true? (every? #(not (identical? foreign-map %)) header)))))
    (is (zero? @calls))))

(deftest native-header-bigint-metrics-preserve-existing-options-domain
  (doseq [[value bits] [[0N 0] [7N 3] [-7N 3] [9223372036854775808N 64]
                        [-9223372036854775808N 64]
                        [(clojure.lang.BigInt/fromBigInteger
                          (.negate (.shiftLeft BigInteger/ONE 4096))) 4097]]]
    (is (= [:big-int false nil bits nil nil] (observe value)))
    (is (= (observe value) (interpreted plan [value]))))
  (let [calls (atom 0) armed (atom false) deny (forbidden calls)
        foreign (proxy [BigInteger] ["1"]
                  (bitLength [] (if @armed (deny) 65))
                  (abs [] (deny)) (longValue [] (deny)) (toString [] (deny)))
        value (clojure.lang.BigInt/fromBigInteger foreign)]
    (reset! armed true)
    (is (= unsupported (observe value)))
    (is (= unsupported (interpreted plan [value])))
    (is (zero? @calls))))

(deftest native-header-cursors-and-subvector-backing-remain-unforced
  (let [calls (atom 0) deny (forbidden calls)
        delayed (lazy-seq (deny) (list :unreachable))
        value (clojure.lang.Cons. :head delayed)
        header (observe value)
        backing (Proxy/newProxyInstance (.getClassLoader clojure.lang.IPersistentVector)
                  (into-array Class [clojure.lang.IPersistentVector clojure.lang.IObj])
                  (reify InvocationHandler (invoke [_ _ _ _] (deny))))
        view (clojure.lang.APersistentVector$SubVector. nil backing 2 7)
        subheader (observe view)]
    (is (= [:list-node false nil nil :head] (subvec header 0 5)))
    (is (identical? delayed (nth header 5)))
    (is (= unsupported (observe (nth header 5))))
    (is (= [:subvector false 5 nil] (subvec subheader 0 4)))
    (is (identical? backing (nth subheader 4)))
    (is (= 2 (nth subheader 5)))
    (is (= unsupported (observe (nth subheader 4))))
    (doseq [[input expected-head] [[value :head] [(list :eager) :eager]]]
      (let [result (interpreted plan [input])]
        (is (= :list-node (nth result 0)))
        (is (= expected-head (nth result 4)))))
    (let [bad (clojure.lang.APersistentVector$SubVector. nil backing Integer/MAX_VALUE Integer/MIN_VALUE)]
      (is (= -4294967295 (nth (observe bad) 2))))
    (is (zero? @calls))))

(deftest native-header-does-not-call-tree-comparators
  (let [calls (atom 0) armed (atom false)
        comparator (reify java.util.Comparator
                     (compare [_ left right]
                       (if @armed ((forbidden calls)) (compare left right))))
        value (sorted-map-by comparator :a 1 :b 2)]
    (reset! armed true)
    (is (= [:tree-map false 2 nil :foreign-comparator nil] (observe value)))
    (is (= (observe value) (interpreted plan [value])))
    (is (zero? @calls))))

(deftest native-header-hashset-field-is-closed-and-access-denial-is-sticky
  (let [field-var (private-var 'p15-s23-stage2-native-hashset-field-once)
        field (deref (deref (var-get field-var)))
        calls (atom 0) metadata (opaque-map calls)
        value (with-meta #{:probe} {:fixture true})]
    (is (true? (some? field)))
    (when field
      (is (= "impl" (.getName ^java.lang.reflect.Field field)))
      (is (identical? clojure.lang.APersistentSet (.getDeclaringClass ^java.lang.reflect.Field field)))
      (let [original (.get ^java.lang.reflect.Field field value)]
        (try
          (.set ^java.lang.reflect.Field field value metadata)
          (is (= unsupported (observe value)))
          (is (= unsupported (interpreted plan [value])))
          (finally (.set ^java.lang.reflect.Field field value original)))))
    (is (zero? @calls))
    (let [attempts (atom 0) unavailable (delay (swap! attempts inc) (atom nil))]
      (with-redefs-fn {field-var unavailable}
        #(dotimes [_ 2] (is (= unsupported (observe #{:a})))))
      (is (= 1 @attempts)))
    (let [denied (.getDeclaredField clojure.lang.APersistentSet "impl")
          access (atom denied)]
      ;; A separate, inaccessible Field simulates denial without changing JVM policy.
      (with-redefs-fn {field-var (delay access)}
        #(dotimes [_ 2] (is (= unsupported (observe #{:a})))))
      (is (nil? @access)))))

(deftest native-header-indirect-scalar-receivers-are-closed
  (let [calls (atom 0) deny (forbidden calls)
        foreign (proxy [BigInteger] ["1"]
                  (abs [] (deny)) (bitLength [] (deny)) (toString [] (deny)))
        ratio (clojure.lang.Ratio. foreign BigInteger/ONE)]
    (doseq [value [foreign ratio]]
      (is (= unsupported (observe value)))
      (is (= unsupported (interpreted plan [value]))))
    (is (zero? @calls)))
  (let [value (keyword (str "native-header-test-" (System/nanoTime)) "unique")
        field (.getField clojure.lang.Keyword "sym")
        allowed (.trySetAccessible field)]
    (is (true? allowed))
    (when allowed
      (let [original (.get field value)]
        (try
          (.set field value nil)
          (is (= unsupported (observe value)))
          (is (= unsupported (interpreted plan [value])))
          (finally (.set field value original)))))))

(deftest native-header-context-arity-and-operand-order-remain-closed
  (doseq [active [plan {} (assoc plan :compiler-artifact-plan? false)
                  (assoc plan :kind :gravity/stage2-instruction-plan)
                  (assoc-in plan [:module :profile] :hosted)
                  (assoc-in plan [:module :target] :wasm)
                  (update plan :module dissoc :target)]]
    (doseq [args [[] [1 2]]]
      (let [direct (outcome #(generic active args))]
        (is (= "L2-BUILTIN-ARITY" (get-in direct [:error :id])))
        (is (= direct (outcome #(interpreted active args))))))
    (when-not (= plan active)
      (let [direct (outcome #(generic active [1]))]
        (is (= "L2-BUILTIN-ERROR" (get-in direct [:error :id])))
        (is (= direct (outcome #(interpreted active [1])))))))
  (let [seam (private-var 'p15-s23-stage2-runtime-execute-value-scoped)
        original @seam calls (atom 0)]
    (with-redefs-fn {seam (fn [& args] (swap! calls inc) (apply original args))}
      #(is (= "L2-BUILTIN-ERROR" (get-in (outcome (fn [] (interpreted {} [1]))) [:error :id]))))
    (is (= 1 @calls)))
  (is (= "L2-UNKNOWN-SYMBOL"
         (get-in (outcome #(b/p15-s23-stage2-runtime-execute-instruction runtime {} {}
                           {:op :builtin-call :function operation :args [{:op :local :name 'missing}]}))
                 [:error :id]))))

(deftest native-header-compiler-artifact-fixtures-execute-both-spellings
  (let [root (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh01_bootstrap_native_carrier_header_test.clj")))]
               (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))
        paths (mapv #(str (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/bootstrap-native-header." %)))
                    ["gravity" "qst"])]
    (is (apply = (map slurp paths)))
    (doseq [source paths]
      (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))
            invoke #(b/p15-s23-stage2-runtime-execute-function runtime compiled %1 %2)]
        (is (= :integer64 (invoke 'kind-of [(long 7)])))
        (is (= [:integer64 false nil 3 nil nil] (invoke 'observe [(long 7)])))
        (is (= [:symbol false nil 7 true nil] (invoke 'observe ['ns/name])))
        (is (= [:symbol false nil 0 true nil] (invoke 'observe [(symbol "")])))
        (is (= [:symbol false nil 0 false nil]
               (invoke 'observe [(clojure.lang.Symbol/intern nil nil)])))
        (is (= "L2-BUILTIN-ARITY" (get-in (outcome #(invoke 'wrong-arity [])) [:error :id])))
        (is (not (contains? (set (get-in emitter [:call-rules :builtin-functions])) operation)))))))
