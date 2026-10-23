(ns gravity.self-hosting.sh22-integer-set-equality-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; STD2/STD3/STD5: this is the bounded experimental request leaf, not source
;; Integer-Set lowering, native execution, migration or SH-22 completion.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_integer_set_equality_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))

(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine
  (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family
       "/coordinator-integer-set-equality." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true}
   plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- request [extension function]
  (invoke (get @fixtures ["accepted" extension]) function []))
(defn- ordered [values] {:artifact :gravity/ordered-set :values values})
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result]
  (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- observe [input]
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped counts (atom {:census 0 :unique 0 :compare 0})
        result (with-redefs-fn
                 {scoped (fn [& args]
                           (when-let [kind ({'sh22-integer-vector? :census
                                             'sh22-vector-unique? :unique
                                             'sh22-integer-set-equal-values? :compare} (nth args 2))]
                             (swap! counts update kind inc))
                           (apply original args))}
                 #(handle input))]
    [result @counts]))

(deftest sh22-integer-set-equality-surface-and-paired-fixtures
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine)))
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity")))
           (slurp (path (fixture-path family "qst"))))))
  (doseq [plan (vals @fixtures)]
    (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (let [policy (call 'sh22-library-policy [])
        manifest (call 'sh22-module-manifest [])
        export (first (filter #(= :ordered-set-equal-integer (:name %)) (:exports manifest)))]
    (is (contains? (:operation-set policy) :ordered-set-equal-integer))
    (is (= 24 (count (:operation-set policy)) (count (:exports manifest))))
    (is (= 256 (:maximum-collection-elements policy)))
    (is (= :experimental (:stability export)))
    (is (= #{:meta} (:profiles export)))
    (is (= #{} (:effects export) (:capabilities export)))
    (is (= :none (:allocation export)))
    (is (= "(OrderedSet Integer) (OrderedSet Integer) -> (Result Bool CollectionError)" (:type export)))))

(deftest sh22-integer-set-equality-executes-value-semantics
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-integer-set-equality-request)
          members (vec (range 256))]
      (doseq [[left right expected]
              [[[] [] true] [[] [0] false] [[0] [] false] [[0] [0] true]
               [[-1 0 1] [1 -1 0] true] [[-1 0 1] [-1 0 2] false]
               [[-1] [1] false] [[1 2] [1 2 3] false]
               [[Long/MIN_VALUE 0 Long/MAX_VALUE] [Long/MAX_VALUE Long/MIN_VALUE 0] true]
               [[Long/MIN_VALUE] [Long/MAX_VALUE] false]
               [[9223372036854775808N -9223372036854775809N]
                [-9223372036854775809N 9223372036854775808N] true]
               [[1N] [1] true] [[9223372036854775808N] [Long/MIN_VALUE] false]
               [members (vec (reverse members)) true]
               [members (assoc members 255 256) false]]]
        (let [input (assoc base :arguments [(ordered left) (ordered right)])
              before (:arguments input) result (handle input)]
          (is (= :accepted (:status result)))
          (is (= expected (:value result)))
          (is (= {:collection-kind :ordered-set :member-family :integer
                  :equality :integer-set-value :safety-outcome :proven-safe
                  :allocation :none :persistent-value-semantics true} (:facts result)))
          (is (= before (:arguments input) (get-in result [:identity-input :arguments])))
          (is (verified? input result)))))))

(deftest sh22-integer-set-equality-consumes-real-insertion-results
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-integer-set-equality-request)
          producer (request extension 'sh22-integer-set-insertion-request)]
      (doseq [[members value right expected]
              [[[] 0 [0] true] [[0] -1 [-1 0] true]
               [[-1 0] 0 [0 -1] true] [[1] 2 [1 3] false]
               [[Long/MIN_VALUE] Long/MAX_VALUE [Long/MAX_VALUE Long/MIN_VALUE] true]
               [[0] 9223372036854775808N [9223372036854775808N 0] true]]]
        (let [insertion (assoc producer :arguments [(ordered members) value])
              inserted (handle insertion)
              query (assoc base :arguments [(:value inserted) (ordered right)])
              result (handle query)]
          (is (= :accepted (:status inserted) (:status result)))
          (is (= expected (:value result)))
          (is (= (ordered members) (first (:arguments insertion))))
          (is (verified? insertion inserted))
          (is (verified? query result)))))))

(deftest sh22-integer-set-equality-rejects-before-comparison
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-integer-set-equality-request)
          members (vec (range 256))]
      (doseq [[left right reason census unique?]
              (concat
               [[(conj members 256) [1] :collection-bound-exceeded 0 false]
                [[1] (conj members 256) :collection-bound-exceeded 0 false]
                [[1 1] [1] :expected-ordered-set 2 true]
                [[1] [1 2 1] :expected-ordered-set 2 true]
                [[] [1 1] :expected-ordered-set 2 true]
                [[1 1N] [1] :expected-ordered-set 2 true]]
               (for [invalid [nil false 1.0 1/2 :one 'one "1" \1 [] {} #{1} '(1)]
                     side [:left :right]]
                 (if (= :left side)
                   [[1 invalid] [1] :expected-integer-ordered-set 1 false]
                   [[1] [1 invalid] :expected-integer-ordered-set 2 false])))]
        (let [input (assoc base :arguments [(ordered left) (ordered right)])
              [result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= (if (= reason :collection-bound-exceeded) "STD3001" "STD10003")
                 (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= census (:census counts)))
          (is (= unique? (pos? (:unique counts))))
          (is (zero? (:compare counts)))
          (is (verified? input result))))
      (doseq [bad [nil [] #{1} {:artifact :other :values [1]}
                   {:artifact :gravity/ordered-set :values '(1)}
                   {:artifact :gravity/ordered-set :values [1] :extra true}]
              arguments [[bad (ordered [])] [(ordered []) bad]]]
        (let [[result counts] (observe (assoc base :arguments arguments))]
          (is (= :rejected (:status result)))
          (is (= :expected-ordered-set (get-in result [:diagnostics 0 :reason])))
          (is (= {:census 0 :unique 0 :compare 0} counts))))
      (doseq [[function reason] [['sh22-integer-set-duplicate-request :expected-ordered-set]
                                ['sh22-integer-set-mixed-request :expected-integer-ordered-set]
                                ['sh22-integer-set-malformed-request :expected-ordered-set]]]
        (let [input (invoke (get @fixtures ["rejected" extension]) function [base]) result (handle input)]
          (is (= :rejected (:status result)))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (verified? input result))))
      (let [calls (atom 0) opaque (fn [] (swap! calls inc))]
        (doseq [arguments [[(ordered [opaque]) (ordered [])]
                          [(ordered [1]) (ordered [1 opaque])]]]
          (let [[result counts] (observe (assoc base :arguments arguments))]
            (is (= :rejected (:status result)))
            (is (zero? (:unique counts)))
            (is (zero? (:compare counts)))
            (is (zero? @calls))))))))

(deftest sh22-integer-set-equality-authority-and-substitution-fail-closed
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-integer-set-equality-request)]
      (doseq [[input rule reason]
              (concat (for [arguments [[] [(ordered [])] [(ordered []) (ordered []) :extra]]]
                        [(assoc base :arguments arguments) "STD1001" :invalid-arity])
                      [[(assoc base :effects #{:memory/allocate}) "STD1002" :undeclared-library-effect]
                       [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                       [(assoc base :allocation-policy :bounded-by-result) "STD1001" :allocation-policy-mismatch]
                       [(assoc base :profile :hosted) "STD1001" :malformed-library-request]
                       [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]])]
        (let [[result counts] (observe input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (= {:census 0 :unique 0 :compare 0} counts))
          (is (verified? input result))))
      (let [result (handle base)]
        (doseq [changed [(assoc result :value false)
                         (assoc result :operation :ordered-set-equal-keyword)
                         (assoc-in result [:facts :member-family] :keyword)
                         (assoc-in result [:identity-input :arguments 1 :values] [99])
                         (assoc-in result [:provenance :implementation] :substitute)]]
          (let [report (call 'sh22-verify-result [base changed])]
            (is (= :rejected (:status report)))
            (is (= :library-result-substitution (get-in report [:diagnostics 0 :reason])))))))))
