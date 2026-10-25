(ns gravity.self-hosting.sh13-mir-integer-sub-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as add]
            [gravity.self-hosting.sh12-checked-i64-sub-producer-test :as producer]
            [gravity.self-hosting.sh13-mir-integer-add-bridge-test :as add-bridge]))

;; SAFE9/C11: reuse original constructors/verifiers, never patch accepted MIR.
(def original-mir
  (delay (add/invoke 'sh12-build-branch-join-mir (:core @producer/current-pair))))

(def ^:private returned-cases
  [[7 3 4] [3 7 -4] [0 0 0] [5 -3 8]
   [Long/MIN_VALUE 0 Long/MIN_VALUE] [Long/MAX_VALUE 0 Long/MAX_VALUE]
   [Long/MIN_VALUE Long/MIN_VALUE 0] [Long/MAX_VALUE Long/MAX_VALUE 0]
   [-1 Long/MIN_VALUE Long/MAX_VALUE] [0 Long/MAX_VALUE (- Long/MAX_VALUE)]
   [-1 Long/MAX_VALUE Long/MIN_VALUE] [Long/MIN_VALUE -1 (inc Long/MIN_VALUE)]
   [Long/MAX_VALUE 1 (dec Long/MAX_VALUE)]])

(def ^:private overflowing-cases
  [[Long/MIN_VALUE 1] [Long/MAX_VALUE -1] [0 Long/MIN_VALUE]
   [Long/MAX_VALUE Long/MIN_VALUE] [Long/MIN_VALUE Long/MAX_VALUE]])

(defn- original-execution-kernel []
  ;; Fixed original definitions; no whole C6/C11 plan or source ingress.
  (let [path (str (.resolve @(add/existing 'root) (add/existing 'c11-source)))
        names '#{sh13-i64? sh13-find-by-key sh13-function sh13-block sh13-bind-values
                 sh13-resolve-values sh13-execution-rejected sh13-continued-value
                 sh13-add-safe? sh13-sub-safe? sh13-mul-safe? sh13-execute-operation
                 sh13-execute-operations sh13-execute-terminator sh13-execute-block sh13-execute-function}
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? names (second %))) forms)
        primitives '#{defn if let loop recur get count = integer? <= >= < > + - * quot conj assoc}
        callees (for [form selected expression (tree-seq coll? seq form)
                      :when (and (seq? expression) (symbol? (first expression)))] (first expression))
        text (str "(ns gravity.test.original-sub-execution (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        _ (assert (= names (set (map second selected))))
        _ (assert (= 16 (count selected)))
        _ (assert (every? #(or (contains? names %) (contains? primitives %)) callees) (pr-str callees))
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "original-sub-execution.gravity" text)]
    (fn [name arguments]
      (bootstrap/p15-s23-stage2-runtime-execute-function
        {:engine :sh13-original-sub-execution-data :compiler-artifact-plan? true} plan name arguments))))

(deftest sh13-checked-i64-sub-original-arithmetic-endpoints-kernel
  ;; Actual original guard/operation on supplied scalar/normalized DATA only.
  (let [call (original-execution-kernel)
        values [Long/MIN_VALUE (inc Long/MIN_VALUE) -2 -1 0 1 2 (dec Long/MAX_VALUE) Long/MAX_VALUE]
        operation {:op-id :sub :opcode :sub :type :i64 :operands [:left :right]}
        trace [{:function :test :operation :sub :opcode :sub}]
        execute (fn [left right]
                  (try (call 'sh13-execute-operation
                         [{} {:fn-id :test} operation {:left left :right right} []])
                       (catch Exception error
                         {:exception (.getName (class error)) :message (.getMessage error)
                          :data (ex-data error)})))]
    (doseq [left values right values]
      (let [difference (-' (bigint left) (bigint right))
            expected (<= (bigint Long/MIN_VALUE) difference (bigint Long/MAX_VALUE))]
        (is (= expected (call 'sh13-sub-safe? [left right])) (str "guard " left " " right))))
    (doseq [[left right expected] returned-cases]
      (is (= {:status :continue :value expected :trace trace} (execute left right))
          (str "operation " left " " right)))
    (doseq [[left right] overflowing-cases]
      (is (= {:status :rejected :reason :integer-range :trace trace} (execute left right))
          (str "overflow " left " " right)))))

(deftest sh13-checked-i64-sub-original-producer-bridge
  (let [core (:core @producer/current-pair) mir @original-mir ids (mapv :node-id (:core-nodes core))
        function (get-in mir [:functions 'main]) entry (:entry function)
        operations (get-in function [:blocks entry :instructions]) check (get operations 2) token (:result check)]
    (is (= :passed (:status (add/invoke 'sh12-verify-branch-join-mir core mir))) (pr-str (:diagnostics mir)))
    (is (= :checked-i64-sub-v1 (get-in mir [:pass-contract :scope :family])))
    (is (= :c11-build-checked-i64-sub (get-in mir [:pass-execution-record :pass-id])))
    (is (= (subvec ids 0 2) (:params function)))
    (is (= [:local :local :runtime-check :integer-sub :function-boundary] (mapv :opcode operations)))
    (is (= [[(get ids 0)] [(get ids 1)]] (mapv :operands (subvec operations 0 2))))
    (is (= (subvec ids 2 4) (:operands check)))
    (is (= [token (get ids 2) (get ids 3)] (:operands (get operations 3))))
    (is (= [] (get-in check [:facts :capability-proof-ids]) (get-in check [:facts :proof-certificate-ids])))
    (is (= [5 7 1] [(count operations) (count (get-in mir [:data-flow-graph :values]))
                    (count (:runtime-check-table mir))]))
    (doseq [[left right expected] returned-cases]
      (let [arguments [left right] result (add-bridge/run core mir arguments) execution (:execution result)
            trace (:trace execution)]
        (is (= :accepted (:status result)))
        (is (= :returned (:status execution)))
        (is (= expected (:value execution)))
        (is (= [(get ids 2) (get ids 3) (:op-id check) (get ids 4)] (vec (keep :operation trace))))
        (is (= :pass (:guard-result (nth trace 2))))
        (is (true? (:token-produced? (nth trace 2))))
        (is (= token (:token-value-id (nth trace 2))))
        (is (= :passed (:status (add-bridge/verify core mir arguments result))))))
    (doseq [arguments overflowing-cases]
      (let [result (add-bridge/run core mir arguments) execution (:execution result) trace (:trace execution)]
        (is (= :rejected (:status result)))
        (is (= :raised (:status execution)))
        (is (= [:error/numeric :overflow :checked 64 :signed]
               ((juxt :type :kind :numeric-mode :bit-width :signedness) (:error execution))))
        (is (= arguments (get-in execution [:error :operands])))
        (is (= [:local :local :runtime-check] (mapv :opcode trace)))
        (is (false? (:token-produced? (last trace))))
        (is (not-any? #(= (get ids 4) (:operation %)) trace))
        (is (= :passed (:status (add-bridge/verify core mir arguments result))))))))

(deftest sh13-checked-i64-sub-rejects-arguments-before-execution
  (let [core (:core @producer/current-pair) mir @original-mir]
    (doseq [arguments [[true 1] [1 true] [(inc (bigint Long/MAX_VALUE)) 0]
                       [0 (dec (bigint Long/MIN_VALUE))] [] [0] [0 0 0] '(0 0)]]
      (let [result (add-bridge/run core mir arguments)]
        (is (= :rejected (:status result)))
        (is (= "C11-TYPE" (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (nil? (:execution result)))))))

(deftest sh13-checked-i64-sub-rejects-original-binding-check-and-opcode-substitution
  (let [core (:core @producer/current-pair) mir @original-mir ids (mapv :node-id (:core-nodes core))
        entry (get-in mir [:functions 'main :entry]) path [:functions 'main :blocks entry :instructions]
        operations (get-in mir path) token (get-in operations [2 :result])]
    (doseq [candidate [(assoc-in mir (conj path 0 :operands) [(get ids 1)])
                       (assoc-in mir (conj path 2 :operands) [(get ids 3) (get ids 2)])
                       (assoc-in mir (conj path 3 :operands) [token (get ids 3) (get ids 2)])
                       (assoc-in mir (conj path 3 :opcode) :integer-add)
                       (assoc-in mir (conj path 3 :operands) [(get ids 2) (get ids 3)])
                       (assoc-in mir path [(get operations 0) (get operations 1) (get operations 3)
                                          (get operations 2) (get operations 4)])]]
      (let [report (add/invoke 'sh12-verify-branch-join-mir core candidate)
            result (add-bridge/run core candidate [7 3])]
        (is (= :rejected (:status report) (:status result)))
        (is (= (:diagnostics report) (:diagnostics result)))
        (is (nil? (:execution result)))))))
