(ns gravity.self-hosting.sh13-mir-integer-add-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as producer]))

(def original-mir
  (delay
    (producer/invoke 'sh12-build-branch-join-mir (:core @producer/current-pair))))

(defn run [core mir arguments]
  (producer/invoke 'sh13-run-branch-join-mir-with-arguments core mir arguments))

(defn verify [core mir arguments result]
  (producer/invoke 'sh13-verify-branch-join-execution-with-arguments core mir arguments result))

(deftest sh13-checked-i64-add-original-producer-bridge
  (let [core (:core @producer/current-pair) mir @original-mir
        nodes (:core-nodes core) ids (mapv :node-id nodes)
        function (get-in mir [:functions 'main]) entry (:entry function)
        operations (get-in function [:blocks entry :instructions])
        check (get operations 2) token (:result check)]
    (is (= :passed (:status (producer/invoke 'sh12-verify-branch-join-mir core mir))) (pr-str (:diagnostics mir)))
    (is (= :gravity/mir-module (:artifact mir)))
    (is (= 1 (count (:blocks function)) (count (:runtime-check-table mir))))
    (is (= 7 (count (get-in mir [:data-flow-graph :values]))
           (count (get-in mir [:data-flow-graph :definitions]))))
    (is (= 5 (count operations)))
    (is (= [:local :local :runtime-check :integer-add :function-boundary] (mapv :opcode operations)))
    (is (= (subvec ids 0 2) (:params function)))
    (is (= :gravity/i64 (:returns function)))
    (is (= #{:error/raise} (:latent-effects function)))
    (is (= [] (get-in check [:facts :capability-proof-ids])))
    (is (= [] (get-in check [:facts :proof-certificate-ids])))
    (is (= [] (get-in mir [:capability-table (get-in check [:facts :capability-fact-id]) :capability-proof-ids])))
    (is (= [token (get ids 2) (get ids 3)] (:operands (get operations 3))))
    (doseq [[arguments expected] [[[0 0] 0] [[Long/MAX_VALUE 0] Long/MAX_VALUE]
                                  [[Long/MIN_VALUE 0] Long/MIN_VALUE]
                                  [[0 Long/MIN_VALUE] Long/MIN_VALUE]
                                  [[Long/MAX_VALUE Long/MIN_VALUE] -1]
                                  [[Long/MIN_VALUE Long/MAX_VALUE] -1] [[-1 1] 0]]]
      (let [result (run core mir arguments) trace (get-in result [:execution :trace])]
        (is (= :accepted (:status result)) (pr-str (:diagnostics result)))
        (is (= :returned (get-in result [:execution :status])))
        (is (= expected (get-in result [:execution :value])))
        (is (= [(get ids 2) (get ids 3) (:op-id check) (get ids 4)] (vec (keep :operation trace))))
        (is (= :pass (:guard-result (nth trace 2))))
        (is (true? (:token-produced? (nth trace 2))))
        (is (= token (:token-value-id (nth trace 2))))
        (is (= :passed (:status (verify core mir arguments result))))
        (is (= :rejected (:status (verify core mir arguments (assoc-in result [:execution :value] 99)))))))
    (doseq [arguments [[Long/MAX_VALUE 1] [Long/MIN_VALUE -1]
                       [-1 Long/MIN_VALUE] [Long/MIN_VALUE Long/MIN_VALUE]]]
      (let [result (run core mir arguments) execution (:execution result)]
        (is (= :rejected (:status result)))
        (is (= :raised (:status execution)))
        (is (= [:error/numeric :overflow :checked 64 :signed]
               ((juxt :type :kind :numeric-mode :bit-width :signedness) (:error execution))))
        (is (= [:local :local :runtime-check] (mapv :opcode (:trace execution))))
        (is (= :fail (:guard-result (last (:trace execution)))))
        (is (false? (:token-produced? (last (:trace execution)))))
        (is (= :not-applicable (:token-value-id (last (:trace execution)))))
        (is (not-any? #(= (get ids 4) (:operation %)) (:trace execution)))
        (is (= :passed (:status (verify core mir arguments result))))))))

(deftest sh13-checked-i64-add-rejects-arguments-before-execution
  (let [core (:core @producer/current-pair) mir @original-mir]
    (doseq [arguments [nil {} '(1 2) [] [1] [1 2 3] [true 0] [0 false] [0 1.0]
                       [(inc (bigint Long/MAX_VALUE)) 0] [0 (dec (bigint Long/MIN_VALUE))]]]
      (let [result (run core mir arguments)]
        (is (= :rejected (:status result)))
        (is (= "C11-TYPE" (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (nil? (:execution result)))))))

(deftest sh13-checked-i64-add-rejects-original-mir-and-upstream-tampering
  (let [core (:core @producer/current-pair) mir @original-mir
        ids (mapv :node-id (:core-nodes core))
        entry (get-in mir [:functions 'main :entry])
        path [:functions 'main :blocks entry :instructions]
        operations (get-in mir path) check-id (get-in operations [2 :facts :runtime-check-id])
        token (get-in operations [2 :result])]
    (doseq [[bad-core bad-mir]
            [[(assoc-in core [:core-nodes 4 :attributes :numeric-mode] :wrapping) mir]
             [(assoc-in core [:core-nodes 4 :safety :outcome] :proven-safe) mir]
             [(assoc-in core [:effect-facts (get ids 5) :residual] #{}) mir]
             [(assoc-in core [:physical-provenance :binding-pins :builder-source-hash] "sha256:forged") mir]
             [(assoc-in core [:core-nodes 4 :source :span :file] "forged.gravity") mir]
             [core (assoc-in mir (conj path 3 :operands) [(get ids 2) (get ids 3)])]
             [core (assoc-in mir (conj path 3 :operands) [token (get ids 3) (get ids 2)])]
             [core (assoc-in mir path [(get operations 0) (get operations 1) (get operations 3)
                                      (get operations 2) (get operations 4)])]
             [core (update mir :runtime-check-table dissoc check-id)]
             [core (assoc-in mir [:runtime-check-table check-id :failure] :panic/numeric)]
             [core (update-in mir [:data-flow-graph :definitions] dissoc token)]
             [core (assoc-in mir [:data-flow-graph :definitions token :operation-id] (get ids 4))]
             [core (assoc-in mir [:type-table (get-in operations [3 :facts :type-fact-id]) :type] :gravity/bool)]]]
      (let [upstream (producer/invoke 'sh12-verify-branch-join-mir bad-core bad-mir)
            result (run bad-core bad-mir [Long/MAX_VALUE 1])]
        (is (= :rejected (:status upstream) (:status result)))
        (is (= (:diagnostics upstream) (:diagnostics result)))
        (is (nil? (:execution result)))))))
