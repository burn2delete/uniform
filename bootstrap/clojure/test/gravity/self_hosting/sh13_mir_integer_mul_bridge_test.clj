(ns gravity.self-hosting.sh13-mir-integer-mul-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as add]
            [gravity.self-hosting.sh12-checked-i64-mul-producer-test :as producer]
            [gravity.self-hosting.sh13-mir-integer-add-bridge-test :as add-bridge]
            [gravity.self-hosting.sh13-mir-integer-sub-bridge-test]))

;; SAFE9/C11: original source products and original MIR, never patched acceptance.
(def original-mir
  (delay (add/invoke 'sh12-build-branch-join-mir (:core @producer/current-pair))))

(def ^:private returned-cases
  [[7 6 42] [-7 6 -42] [7 -6 -42] [-7 -6 42]
   [Long/MIN_VALUE 0 0] [0 Long/MIN_VALUE 0]
   [Long/MIN_VALUE 1 Long/MIN_VALUE] [1 Long/MIN_VALUE Long/MIN_VALUE]
   [Long/MAX_VALUE 1 Long/MAX_VALUE] [-1 Long/MAX_VALUE (- Long/MAX_VALUE)]
   [3037000499 3037000499 9223372030926249001]
   [3037000499 3037000500 9223372033963249500]
   [3037000500 3037000499 9223372033963249500]
   [-3037000499 3037000499 -9223372030926249001]])

(def ^:private overflowing-cases
  [[Long/MIN_VALUE -1] [-1 Long/MIN_VALUE] [Long/MAX_VALUE 2] [2 Long/MAX_VALUE]
   [Long/MIN_VALUE 2] [2 Long/MIN_VALUE]
   [3037000500 3037000500] [-3037000500 3037000500]])

(deftest sh13-checked-i64-mul-original-arithmetic-endpoints-kernel
  ;; Exact unchanged original 16-definition guard/executor closure on DATA.
  (let [call ((var-get (ns-resolve 'gravity.self-hosting.sh13-mir-integer-sub-bridge-test 'original-execution-kernel)))
        values [Long/MIN_VALUE (inc Long/MIN_VALUE) -2 -1 0 1 2 (dec Long/MAX_VALUE) Long/MAX_VALUE]
        thresholds [-3037000500 -3037000499 3037000499 3037000500]
        pairs (concat (for [left values right values] [left right])
                      (for [left thresholds right thresholds] [left right]))
        operation {:op-id :mul :opcode :mul :type :i64 :operands [:left :right]}
        trace [{:function :test :operation :mul :opcode :mul}]]
    (assert (= 97 (count pairs) (count (set pairs))))
    (doseq [[left right] pairs]
      (let [product (*' (bigint left) (bigint right))
            safe? (<= (bigint Long/MIN_VALUE) product (bigint Long/MAX_VALUE))
            result (try (call 'sh13-execute-operation
                          [{} {:fn-id :test} operation {:left left :right right} 8 0 []])
                        (catch Exception error
                          {:exception (.getName (class error)) :message (.getMessage error)
                           :data (ex-data error)}))]
        (is (= safe? (call 'sh13-mul-safe? [left right])) (str "guard " left " " right))
        (is (= (if safe? {:status :continue :value product :fuel 7 :trace trace}
                  {:status :rejected :reason :integer-range :fuel 7 :trace trace}) result)
            (str "operation " left " " right))))))

(deftest sh13-checked-i64-mul-original-producer-bridge
  (let [core (:core @producer/current-pair) mir @original-mir ids (mapv :node-id (:core-nodes core))
        function (get-in mir [:functions 'main]) entry (:entry function)
        operations (get-in function [:blocks entry :instructions]) check (get operations 2) token (:result check)]
    (is (= :passed (:status (add/invoke 'sh12-verify-branch-join-mir core mir))) (pr-str (:diagnostics mir)))
    (is (= :checked-i64-mul-v1 (get-in mir [:pass-contract :scope :family])))
    (is (= :c11-build-checked-i64-mul (get-in mir [:pass-execution-record :pass-id])))
    (is (= (subvec ids 0 2) (:params function)))
    (is (= [:local :local :runtime-check :integer-mul :function-boundary] (mapv :opcode operations)))
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

(deftest sh13-checked-i64-mul-rejects-arguments-before-execution
  (let [core (:core @producer/current-pair) mir @original-mir]
    (doseq [arguments [[true 1] [1 true] [(inc (bigint Long/MAX_VALUE)) 0]
                       [0 (dec (bigint Long/MIN_VALUE))] [] [0] [0 0 0] '(0 0)]]
      (let [result (add-bridge/run core mir arguments)]
        (is (= :rejected (:status result)))
        (is (= "C11-TYPE" (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (nil? (:execution result)))))))

(deftest sh13-checked-i64-mul-rejects-original-binding-check-and-opcode-substitution
  (let [core (:core @producer/current-pair) mir @original-mir ids (mapv :node-id (:core-nodes core))
        entry (get-in mir [:functions 'main :entry]) path [:functions 'main :blocks entry :instructions]
        operations (get-in mir path) token (get-in operations [2 :result])]
    (doseq [candidate [(assoc-in mir (conj path 0 :operands) [(get ids 1)])
                       (assoc-in mir (conj path 2 :operands) [(get ids 3) (get ids 2)])
                       (assoc-in mir (conj path 3 :operands) [token (get ids 3) (get ids 2)])
                       (assoc-in mir (conj path 3 :opcode) :integer-add)
                       (assoc-in mir (conj path 3 :opcode) :integer-sub)
                       (assoc-in mir (conj path 3 :operands) [(get ids 2) (get ids 3)])
                       (assoc-in mir path [(get operations 0) (get operations 1) (get operations 3)
                                          (get operations 2) (get operations 4)])]]
      (let [report (add/invoke 'sh12-verify-branch-join-mir core candidate)
            result (add-bridge/run core candidate [7 3])]
        (is (= :rejected (:status report) (:status result)))
        (is (= (:diagnostics report) (:diagnostics result)))
        (is (nil? (:execution result)))))))
