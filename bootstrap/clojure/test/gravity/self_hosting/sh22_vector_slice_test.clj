(ns gravity.self-hosting.sh22-vector-slice-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; STD1/STD3: a bounded experimental request leaf, not new source subvec
;; support, compiler linkage, native execution, migration or SH-22 completion.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_vector_slice_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine
  (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-vector-slice." extension))
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
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- observe-copy [input]
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped copies (atom 0)
        result (with-redefs-fn
                 {scoped (fn [& args]
                           (when (= 'sh22-vector-slice-values (nth args 2)) (swap! copies inc))
                           (apply original args))}
                 #(handle input))]
    [result @copies]))
(def ^:private slice-facts
  {:collection-kind :vector :safety-outcome :proven-safe
   :allocation :bounded-by-result :persistent-value-semantics true
   :storage :copied-vector-spine :stored-values :shallow-shared
   :lifetime :persistent :borrows-input-storage false})

(deftest sh22-vector-slice-surface-and-paired-fixtures
  (is (= :gravity/stage2-compiler-artifact-plan (:kind @engine)))
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity")))
           (slurp (path (fixture-path family "qst"))))))
  (doseq [plan (vals @fixtures)] (is (= :gravity/stage2-compiler-artifact-plan (:kind plan))))
  (let [policy (call 'sh22-library-policy []) manifest (call 'sh22-module-manifest [])
        export (first (filter #(= :vector-slice (:name %)) (:exports manifest)))]
    (is (contains? (:operation-set policy) :vector-slice))
    (is (= (:operation-set policy) (set (map :name (:exports manifest)))))
    (is (= :experimental (:stability export)))
    (is (= #{:meta} (:profiles export)))
    (is (= #{:memory/allocate} (:effects export)))
    (is (= #{} (:capabilities export)))
    (is (= :bounded-by-result (:allocation export)))
    (is (= "(Vector a) Integer Integer -> (Result (Vector a) CollectionError)" (:type export)))))

(deftest sh22-vector-slice-executes-ordered-persistent-copies
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-vector-slice-request) members (vec (range 256))]
      (doseq [[values start end expected]
              [[[] 0 0 []] [[:reader false nil :syntax] 0 4 [:reader false nil :syntax]]
               [[:reader false nil :syntax] 0 2 [:reader false]]
               [[:reader false nil :syntax] 1 3 [false nil]]
               [[:reader false nil :syntax] 2 4 [nil :syntax]]
               [[:reader false nil :syntax] 2 2 []] [[:reader false nil :syntax] 4 4 []]
               [[{:value :inert} 'unresolved/name [1 2] #{:stored}] 0 3
                [{:value :inert} 'unresolved/name [1 2]]]
               [members 0 256 members] [members 255 256 [255]] [members 256 256 []]
               [[:reader false nil :syntax] 1N 3N [false nil]]]]
        (let [input (assoc base :arguments [values start end])
              [result copies] (observe-copy input)]
          (is (= :accepted (:status result)))
          (is (= expected (:value result)))
          (is (vector? (:value result)))
          (is (not (instance? clojure.lang.APersistentVector$SubVector (:value result))))
          (is (= 1 copies))
          (is (= slice-facts (:facts result)))
          (is (= [values start end] (:arguments input) (get-in result [:identity-input :arguments])))
          (is (verified? input result))))
      (let [calls (atom 0) payload (fn [] (swap! calls inc))
            input (assoc base :arguments [[payload {:stored payload} :tail] 0 2])
            result (handle input)]
        (is (= :accepted (:status result)))
        (is (= [payload {:stored payload}] (:value result)))
        (is (identical? payload (first (:value result))))
        (is (verified? input result))
        (is (zero? @calls))))))

(deftest sh22-vector-slice-composes-real-append-assoc-and-nth-results
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-vector-slice-request)
          append (request extension 'sh22-vector-slice-append-request)
          assoc-base (request extension 'sh22-vector-slice-assoc-request)
          nth-base (request extension 'sh22-vector-slice-nth-request)]
      (doseq [[prefix added coordinate replacement start end expected]
              [[[] :reader 0 :syntax 0 1 [:syntax]]
               [[:reader false] nil 1 nil 1 3 [nil nil]]
               [[:reader false nil] :syntax 3 :mir 1 4 [false nil :mir]]
               [(vec (range 255)) 255 255 :last 254 256 [254 :last]]]]
        (let [append-input (assoc append :arguments [prefix added]) appended (handle append-input)
              assoc-input (assoc assoc-base :arguments [(:value appended) coordinate replacement])
              associated (handle assoc-input)
              slice-input (assoc base :arguments [(:value associated) start end]) sliced (handle slice-input)]
          (is (= :accepted (:status appended) (:status associated) (:status sliced)))
          (is (= expected (:value sliced)))
          (is (= prefix (first (:arguments append-input))))
          (is (= (conj prefix added) (:value appended)))
          (doseq [[input result] [[append-input appended] [assoc-input associated] [slice-input sliced]]]
            (is (verified? input result)))
          (doseq [index (range (count expected))]
            (let [nth-input (assoc nth-base :arguments [(:value sliced) index]) selected (handle nth-input)]
              (is (= :accepted (:status selected)))
              (is (= (get expected index) (:value selected)))
              (is (verified? nth-input selected)))))))))

(deftest sh22-vector-slice-rejects-complete-invalid-inputs-before-copy
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-vector-slice-request) members (vec (range 257))]
      (doseq [[arguments rule reason]
              (concat
               [[[members :wrong :wrong] "STD3002" :slice-out-of-bounds]]
               (for [bad [nil [] {} #{1} '(1) :vector "vector"] :when (not (vector? bad))]
                 [[bad 0 0] "STD3002" :expected-vector])
               (for [[start end] [[-1 0] [0 -1] [2 1] [0 4] [4 4]
                                  [Long/MIN_VALUE 0] [0 Long/MAX_VALUE]
                                  [-9223372036854775809N 0] [0 9223372036854775808N]
                                  [nil 0] [0 nil] [false 0] [0 true] [0.0 1] [0 1.0]
                                  [1/2 1] [0 3/2] [:zero 1] [0 'end] ["0" 1] [0 []]]]
                 [[[:reader false nil] start end] "STD3002" :slice-out-of-bounds]))]
        (let [input (assoc base :arguments arguments) [result copies] (observe-copy input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (zero? copies))
          (is (verified? input result))))
      (doseq [[function rule reason]
              [['sh22-vector-slice-reversed-request "STD3002" :slice-out-of-bounds]
               ['sh22-vector-slice-wrong-index-request "STD3002" :slice-out-of-bounds]
               ['sh22-vector-slice-wrong-vector-request "STD3002" :expected-vector]]]
        (let [input (invoke (get @fixtures ["rejected" extension]) function [base])
              [result copies] (observe-copy input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (zero? copies))
          (is (verified? input result)))))))

(deftest sh22-vector-slice-authority-and-result-substitutions-fail-closed
  (doseq [extension ["gravity" "qst"]]
    (let [base (request extension 'sh22-vector-slice-request)]
      (doseq [[input rule reason]
              (concat (for [arguments [[] [[]] [[] 0] [[] 0 0 :extra]]]
                        [(assoc base :arguments arguments) "STD1001" :invalid-arity])
                      [[(assoc base :effects #{}) "STD1002" :undeclared-library-effect]
                       [(assoc base :effects #{:memory/allocate :io/write}) "STD1002" :undeclared-library-effect]
                       [(assoc base :capabilities #{:fs/read}) "STD1003" :unexpected-library-capability]
                       [(assoc base :allocation-policy :none) "STD1001" :allocation-policy-mismatch]
                       [(assoc base :experimental-opt-in false) "STD1001" :malformed-library-request]
                       [(assoc base :profile :core) "STD1001" :malformed-library-request]])]
        (let [[result copies] (observe-copy input)]
          (is (= :rejected (:status result)))
          (is (= rule (get-in result [:diagnostics 0 :rule])))
          (is (= reason (get-in result [:diagnostics 0 :reason])))
          (is (zero? copies))
          (is (verified? input result))))
      (let [result (handle base)]
        (doseq [changed [(assoc result :value [nil false])
                         (assoc result :operation :vector-nth)
                         (assoc-in result [:facts :storage] :retained-input)
                         (assoc-in result [:facts :allocation] :none)
                         (assoc-in result [:identity-input :arguments 1] 0)
                         (assoc-in result [:provenance :source-span :end] 65)]]
          (let [report (call 'sh22-verify-result [base changed])]
            (is (= :rejected (:status report)))
            (is (= :library-result-substitution (get-in report [:diagnostics 0 :reason])))))))))
