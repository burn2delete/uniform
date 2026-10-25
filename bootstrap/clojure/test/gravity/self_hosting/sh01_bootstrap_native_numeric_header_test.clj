(ns gravity.self-hosting.sh01-bootstrap-native-numeric-header-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b])
  (:import (java.math BigDecimal BigInteger)))

;; BOOT2/STD5: native descriptors preserve the old Number contour without
;; granting DATA validity, equality, numeric conversion or exceptional policy.
(def ^:private operation 'bootstrap-native-carrier-header)
(def ^:private unsupported [:unsupported nil nil nil nil nil])
(def ^:private runtime {:engine :bootstrap-native-numeric-header-test})
(def ^:private plan
  {:kind :gravity/stage2-compiler-artifact-plan :compiler-artifact-plan? true
   :module {:profile :meta :target :jvm} :source {:path "native-numeric-header-test.gravity"}})
(defn- generic [active arguments]
  (b/p15-s23-stage2-runtime-invoke-builtin active operation arguments))
(defn- interpreted [active arguments]
  (b/p15-s23-stage2-runtime-execute-instruction runtime active {}
    {:op :builtin-call :function operation :args (mapv #(hash-map :op :literal :value %) arguments)}))
(defn- observe [value] (generic plan [value]))
(defn- outcome [thunk]
  (try {:value (thunk)} (catch clojure.lang.ExceptionInfo error {:error (ex-data error)})))
(defn- private-var [name] (ns-resolve 'gravity.bootstrap name))
(defn- forbidden [calls]
  (fn [] (swap! calls inc) (throw (AssertionError. "unpaid numeric-header callback"))))

(deftest native-numeric-header-byte-and-short-magnitudes
  (doseq [[value kind]
          (concat (map #(vector (Byte/valueOf (byte %)) :integer8) [0 1 -1 -128 127])
                  (map #(vector (Short/valueOf (short %)) :integer16) [0 1 -1 -32768 32767]))]
    (let [bits (.bitLength (.abs (BigInteger/valueOf (.longValue ^Number value))))
          expected [kind false nil bits nil nil]]
      (is (= expected (observe value)))
      (is (= expected (interpreted plan [value]))))))

(deftest native-numeric-header-float-width-does-not-classify-or-canonicalize
  (doseq [value [(Float/valueOf (float 0.0)) (Float/valueOf (float -0.0))
                 (Float/valueOf Float/MIN_VALUE) (Float/valueOf Float/MAX_VALUE)
                 (Float/valueOf (float (- Float/MAX_VALUE)))
                 (Float/valueOf Float/POSITIVE_INFINITY) (Float/valueOf Float/NEGATIVE_INFINITY)
                 (Float/valueOf (Float/intBitsToFloat (unchecked-int 0x7fc12345)))]]
    (let [before (Float/floatToRawIntBits (.floatValue ^Float value))]
      (is (= [:float32 false nil 32 nil nil] (observe value)))
      (is (= (observe value) (interpreted plan [value])))
      (is (= before (Float/floatToRawIntBits (.floatValue ^Float value))))))
  (doseq [value [(Double/valueOf 0.0) (Double/valueOf -0.0)
                 (Double/valueOf Double/MIN_VALUE) (Double/valueOf Double/MAX_VALUE)
                 (Double/valueOf (- Double/MAX_VALUE))
                 (Double/valueOf Double/POSITIVE_INFINITY) (Double/valueOf Double/NEGATIVE_INFINITY)
                 (Double/valueOf (Double/longBitsToDouble 0x7ff8123456789abc))]]
    (let [before (Double/doubleToRawLongBits (.doubleValue ^Double value))]
      (is (= [:float64 false nil 64 nil nil] (observe value)))
      (is (= (observe value) (interpreted plan [value])))
      (is (= before (Double/doubleToRawLongBits (.doubleValue ^Double value)))))))

(deftest native-numeric-header-decimal-magnitude-and-signed-scale
  (doseq [value [(BigDecimal/valueOf (long 0)) (BigDecimal/valueOf (long -7))
                 (BigDecimal/valueOf (long Long/MIN_VALUE)) (BigDecimal/valueOf (long Long/MAX_VALUE))
                 (BigDecimal. "1.2300") (BigDecimal. "-100.50") (BigDecimal. "0E+8")
                 (BigDecimal. BigInteger/ZERO Integer/MIN_VALUE)
                 (BigDecimal. BigInteger/ONE Integer/MAX_VALUE)
                 (BigDecimal. (.shiftLeft BigInteger/ONE 4096) -100000)
                 (BigDecimal. (.negate (.shiftLeft BigInteger/ONE 4096)) Integer/MIN_VALUE)]]
    (let [bits (.bitLength (.abs (.unscaledValue ^BigDecimal value)))
          scale (long (.scale ^BigDecimal value))
          expected [:big-decimal false nil bits scale nil]]
      (is (= expected (observe value)))
      (is (= expected (interpreted plan [value])))
      (is (identical? Long (class (nth (observe value) 4)))))))

(deftest native-numeric-header-refuses-foreign-receivers-before-methods
  (let [calls (atom 0) deny (forbidden calls)
        number (proxy [Number] []
                 (intValue [] (deny)) (longValue [] (deny))
                 (floatValue [] (deny)) (doubleValue [] (deny))
                 (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        decimal (proxy [BigDecimal] ["1"]
                  (unscaledValue [] (deny)) (scale [] (deny)) (precision [] (deny))
                  (intValue [] (deny)) (longValue [] (deny))
                  (floatValue [] (deny)) (doubleValue [] (deny))
                  (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        integer (proxy [BigInteger] ["1"]
                  (abs [] (deny)) (bitLength [] (deny)) (longValue [] (deny))
                  (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))
        opaque (proxy [Object] []
                 (toString [] (deny)) (hashCode [] (deny)) (equals [_] (deny)))]
    (doseq [value [number decimal integer opaque]]
      (is (= unsupported (observe value)))
      (is (= unsupported (interpreted plan [value]))))
    (is (zero? @calls))))

(deftest native-numeric-header-decimal-nested-guard-and-constructor-sanitization
  (let [calls (atom 0) setup-calls (atom 0) armed (atom false) deny (forbidden calls)
        foreign (proxy [BigInteger] ["18446744073709551617"]
                  (toByteArray [] (if @armed (deny)
                                    (do (swap! setup-calls inc) (proxy-super toByteArray))))
                  (bitLength [] (if @armed (deny) (proxy-super bitLength)))
                  (abs [] (if @armed (deny) (proxy-super abs)))
                  (longValue [] (if @armed (deny) (proxy-super longValue)))
                  (toString [] (if @armed (deny) (proxy-super toString))))
        ;; Actual OpenJDK public construction sanitizes the subclass before
        ;; observation. No opens, reflection or Unsafe forges a java.math field.
        value (BigDecimal. foreign 17)
        backing (.unscaledValue value)
        metric-var (private-var 'p15-s23-stage2-native-big-integer-magnitude-bits)]
    (is (identical? BigInteger (class backing)))
    (is (pos? @setup-calls))
    (reset! armed true)
    (is (= [:big-decimal false nil 65 17 nil] (observe value)))
    (is (= (observe value) (interpreted plan [value])))
    (is (nil? (@metric-var foreign)))
    (is (= unsupported (observe foreign)))
    (is (zero? @calls))
    ;; Test-only failure of the nested metric closes the native decimal branch;
    ;; this is not a claim that a foreign backing survived public construction.
    (with-redefs-fn {metric-var (fn [_] nil)}
      #(do (is (= unsupported (observe value)))
           (is (= unsupported (interpreted plan [value])))))))

(deftest native-numeric-header-retains-context-arity-and-no-rendering
  (let [seam (private-var 'p15-s23-stage2-runtime-str)
        renders (atom 0) deny (forbidden renders)
        values [(Byte/valueOf (byte -1)) (Short/valueOf (short -1))
                (Float/valueOf Float/NaN) (Double/valueOf Double/POSITIVE_INFINITY)
                (BigDecimal. BigInteger/ONE Integer/MIN_VALUE)]]
    (with-redefs-fn {seam (fn [_] (deny))}
      #(doseq [value values]
         (is (= (observe value) (interpreted plan [value])))
         (doseq [active [plan {} (assoc plan :compiler-artifact-plan? false)
                         (assoc plan :kind :gravity/stage2-instruction-plan)
                         (assoc-in plan [:module :profile] :hosted)
                         (assoc-in plan [:module :target] :wasm)]]
           (doseq [args [[] [value value]]]
             (let [direct (outcome (fn [] (generic active args)))]
               (is (= "L2-BUILTIN-ARITY" (get-in direct [:error :id])))
               (is (= direct (outcome (fn [] (interpreted active args)))))))
           (when-not (= plan active)
             (let [direct (outcome (fn [] (generic active [value])))]
               (is (= "L2-BUILTIN-ERROR" (get-in direct [:error :id])))
               (is (= direct (outcome (fn [] (interpreted active [value]))))))))))
    (is (zero? @renders))))

(deftest native-numeric-header-real-fixtures-execute-both-spellings
  (let [root (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh01_bootstrap_native_numeric_header_test.clj")))]
               (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))
        paths (mapv #(str (io/file root (str "bootstrap/clojure/fixtures/self-hosting/sh-01/modules/bootstrap-native-numeric-header." %)))
                    ["gravity" "qst"])
        values [(Byte/valueOf (byte 7)) (Short/valueOf (short -9))
                (Float/valueOf Float/NaN) (Double/valueOf Double/NEGATIVE_INFINITY)
                (BigDecimal. "7.00")]]
    (is (apply = (map slurp paths)))
    (doseq [source paths]
      (let [emitter (:emitter (b/c-backend-stage2-plan-emitter-source-rule! source :jvm))
            compiled (b/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))
            invoke #(b/p15-s23-stage2-runtime-execute-function runtime compiled %1 %2)]
        (doseq [value values]
          (is (= (observe value) (invoke 'observe [value])))
          (is (= (first (observe value)) (invoke 'kind-of [value]))))
        (is (= "L2-BUILTIN-ARITY" (get-in (outcome #(invoke 'wrong-arity [])) [:error :id])))
        (is (not (contains? (set (get-in emitter [:call-rules :builtin-functions])) operation)))))))
