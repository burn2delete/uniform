(ns gravity.self-hosting.sh13-mir-integer-add-bridge-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as producer]))

(def original-mir
  (delay
    (producer/invoke 'sh12-build-branch-join-mir (:core @producer/current-pair))))

(def reversed-mir
  (delay
    (producer/invoke 'sh12-build-branch-join-mir (:core @producer/reversed-pair))))

(def repeated-mirs
  (delay
    {:left (producer/invoke 'sh12-build-branch-join-mir (:core (get @producer/repeated-pairs :left)))
     :right (producer/invoke 'sh12-build-branch-join-mir (:core (get @producer/repeated-pairs :right)))}))

(defn run [core mir arguments]
  (producer/invoke 'sh13-run-branch-join-mir-with-arguments core mir arguments))

(defn verify [core mir arguments result]
  (producer/invoke 'sh13-verify-branch-join-execution-with-arguments core mir arguments result))

(deftest sh13-repeated-checked-i64-add-original-producer-bridge
  (doseq [[side index] [[:left 0] [:right 1]]]
    (let [core (:core (get @producer/repeated-pairs side)) mir (get @repeated-mirs side)
          ids (mapv :node-id (:core-nodes core)) function (get-in mir [:functions 'main]) entry (:entry function)
          operations (get-in function [:blocks entry :instructions]) check (get operations 2) token (:result check)
          arguments #(assoc [Long/MAX_VALUE Long/MIN_VALUE] index %)]
      (is (= :passed (:status (producer/invoke 'sh12-verify-branch-join-mir core mir))) (pr-str (:diagnostics mir)))
      (is (= (subvec ids 0 2) (:params function)))
      (is (= [:local :local :runtime-check :integer-add :function-boundary] (mapv :opcode operations)))
      (is (= [[(get ids index)] [(get ids index)]] (mapv :operands (subvec operations 0 2))))
      (is (= (subvec ids 2 4) (mapv :result (subvec operations 0 2))))
      (is (= 2 (count (set (mapv :result (subvec operations 0 2))))))
      (is (= (subvec ids 2 4) (:operands check)))
      (is (= [token (get ids 2) (get ids 3)] (:operands (get operations 3))))
      (doseq [[value expected] [[0 0] [1 2] [-1 -2] [4611686018427387903 9223372036854775806]
                                [-4611686018427387904 Long/MIN_VALUE]]]
        (let [args (arguments value) result (run core mir args) execution (:execution result)]
          (is (= :accepted (:status result)))
          (is (= :returned (:status execution)))
          (is (= expected (:value execution)))
          (is (= [(get ids 2) (get ids 3) (:op-id check) (get ids 4)] (vec (keep :operation (:trace execution)))))
          (is (true? (:token-produced? (nth (:trace execution) 2))))
          (is (= :passed (:status (verify core mir args result))))))
      (doseq [value [4611686018427387904 -4611686018427387905 Long/MAX_VALUE Long/MIN_VALUE]]
        (let [args (arguments value) result (run core mir args) execution (:execution result)]
          (is (= :rejected (:status result)))
          (is (= :raised (:status execution)))
          (is (= [:error/numeric :overflow] ((juxt :type :kind) (:error execution))))
          (is (= [value value] (get-in execution [:error :operands])))
          (is (= [:local :local :runtime-check] (mapv :opcode (:trace execution))))
          (is (false? (:token-produced? (last (:trace execution)))))
          (is (= :not-applicable (:token-value-id (last (:trace execution)))))
          (is (= :passed (:status (verify core mir args result))))))
      (doseq [args [(assoc (arguments 0) (- 1 index) true)
                    (assoc (arguments 0) (- 1 index) (inc (bigint Long/MAX_VALUE)))
                    (subvec (arguments 0) 0 1) (conj (arguments 0) 0) (assoc (arguments 0) index true)]]
        (let [result (run core mir args)]
          (is (= :rejected (:status result)))
          (is (= "C11-TYPE" (get-in result [:diagnostics 0 :diagnostic-id])))
          (is (nil? (:execution result))))))))

(deftest sh13-repeated-checked-i64-add-rejects-binding-occurrence-and-check-substitution
  (doseq [[side index] [[:left 0] [:right 1]]]
    (let [core (:core (get @producer/repeated-pairs side)) mir (get @repeated-mirs side)
          ids (mapv :node-id (:core-nodes core)) entry (get-in mir [:functions 'main :entry])
          path [:functions 'main :blocks entry :instructions] operations (get-in mir path)
          token (get-in operations [2 :result])]
      (doseq [candidate [(assoc-in mir (conj path 0 :operands) [(get ids (- 1 index))])
                         (assoc-in mir (conj path 1 :result) (get-in operations [0 :result]))
                         (assoc-in mir (conj path 2 :operands) [(get ids 2) (get ids 2)])
                         (assoc-in mir (conj path 3 :operands) [token (get ids 2) (get ids 2)])
                         (assoc-in mir path [(get operations 0) (get operations 1) (get operations 3)
                                            (get operations 2) (get operations 4)])]]
        (let [report (producer/invoke 'sh12-verify-branch-join-mir core candidate)
              result (run core candidate [1 2])]
          (is (= :rejected (:status report) (:status result)))
          (is (= (:diagnostics report) (:diagnostics result)))
          (is (nil? (:execution result))))))))

(deftest sh13-reversed-checked-i64-add-original-binding-order-kernel
  ;; Candidate-shape/mapping DATA only; this never accepts a checked Core.
  (let [path (str (.resolve @(producer/existing 'root) (producer/existing 'c11-source)))
        names '#{sh12-c6p-i64-add? sh12-c6p-i64-operand-parameter-order}
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (contains? names (second %))) forms)
        primitives '#{defn let if keyword? vector? get count = loop recur + conj}
        callees (for [form selected expression (tree-seq coll? seq form)
                      :when (and (seq? expression) (symbol? (first expression)))] (first expression))
        text (str "(ns gravity.test.reversed-add-binding-order (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        _ (assert (= names (set (map second selected))))
        _ (assert (and (= 2 (count selected)) (< (count text) 4096)))
        _ (assert (every? #(or (contains? names %) (contains? primitives %)) callees) (pr-str callees))
        emitter (:emitter (gravity.bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (gravity.bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "reversed-add-binding-order.gravity" text)
        core {:resolved-parameter {:kind :gravity/c6-c10-resolved-two-i64-parameters-v1}
              :core-nodes [{:source-operation :parameter :node-id :a} {:source-operation :parameter :node-id :b}
                           {:source-operation :local :operands [:a]} {:source-operation :local :operands [:b]}
                           {:source-operation :integer-add} {:source-operation :function}]}
        call #(gravity.bootstrap/p15-s23-stage2-runtime-execute-function
                {:engine :sh13-reversed-binding-order-data :compiler-artifact-plan? true}
                plan 'sh12-c6p-i64-operand-parameter-order [%])]
    (is (= [0 1] (call core)))
    (is (= [1 0] (call (-> core (assoc-in [:core-nodes 2 :operands] [:b]) (assoc-in [:core-nodes 3 :operands] [:a])))))
    (is (= [0 0] (call (assoc-in core [:core-nodes 3 :operands] [:a]))))
    (is (= [1 1] (call (assoc-in core [:core-nodes 2 :operands] [:b]))))
    (doseq [candidate [(assoc-in core [:core-nodes 2 :operands] [:unknown])
                       (assoc-in core [:core-nodes 1 :node-id] :a)
                       (assoc-in core [:core-nodes 2 :operands] [:a :b])]]
      (is (nil? (call candidate))))
    (is (= [0 1] (call (assoc-in core [:core-nodes 4 :source-operation] :integer-lt))))))

(deftest sh13-reversed-checked-i64-add-original-producer-bridge
  (let [core (:core @producer/reversed-pair) mir @reversed-mir ids (mapv :node-id (:core-nodes core))
        function (get-in mir [:functions 'main]) entry (:entry function)
        operations (get-in function [:blocks entry :instructions]) check (get operations 2) token (:result check)]
    (is (= :passed (:status (producer/invoke 'sh12-verify-branch-join-mir core mir))) (pr-str (:diagnostics mir)))
    (is (= (subvec ids 0 2) (:params function)))
    (is (= [:local :local :runtime-check :integer-add :function-boundary] (mapv :opcode operations)))
    (is (= [[(get ids 1)] [(get ids 0)]] (mapv :operands (subvec operations 0 2))))
    (is (= (subvec ids 2 4) (:operands check)))
    (is (= [token (get ids 2) (get ids 3)] (:operands (get operations 3))))
    (is (= [] (get-in check [:facts :capability-proof-ids]) (get-in check [:facts :proof-certificate-ids])))
    (doseq [[arguments expected] [[[2 40] 42] [[Long/MAX_VALUE Long/MIN_VALUE] -1]
                                  [[0 Long/MIN_VALUE] Long/MIN_VALUE] [[Long/MIN_VALUE 0] Long/MIN_VALUE]]]
      (let [result (run core mir arguments) execution (:execution result)]
        (is (= :accepted (:status result)))
        (is (= :returned (:status execution)))
        (is (= expected (:value execution)))
        (is (= [(get ids 2) (get ids 3) (:op-id check) (get ids 4)] (vec (keep :operation (:trace execution)))))
        (is (true? (:token-produced? (nth (:trace execution) 2))))
        (is (= :passed (:status (verify core mir arguments result))))))
    (doseq [arguments [[Long/MAX_VALUE 1] [Long/MIN_VALUE -1]]]
      (let [result (run core mir arguments) execution (:execution result)]
        (is (= :rejected (:status result)))
        (is (= :raised (:status execution)))
        (is (= [:error/numeric :overflow] ((juxt :type :kind) (:error execution))))
        (is (= (vec (reverse arguments)) (get-in execution [:error :operands])))
        (is (= [:local :local :runtime-check] (mapv :opcode (:trace execution))))
        (is (false? (:token-produced? (last (:trace execution)))))
        (is (= :not-applicable (:token-value-id (last (:trace execution)))))
        (is (= :passed (:status (verify core mir arguments result))))))))

(deftest sh13-reversed-checked-i64-add-rejects-binding-and-check-substitution
  (let [core (:core @producer/reversed-pair) mir @reversed-mir ids (mapv :node-id (:core-nodes core))
        entry (get-in mir [:functions 'main :entry]) path [:functions 'main :blocks entry :instructions]
        operations (get-in mir path) token (get-in operations [2 :result])
        changed-both (-> mir (assoc-in (conj path 0 :operands) [(get ids 0)])
                            (assoc-in (conj path 1 :operands) [(get ids 1)]))]
    (doseq [candidate [(assoc-in mir (conj path 0 :operands) [(get ids 0)]) changed-both
                       (assoc-in mir (conj path 2 :operands) [(get ids 3) (get ids 2)])
                       (assoc-in mir (conj path 3 :operands) [token (get ids 3) (get ids 2)])
                       (assoc-in mir path [(get operations 0) (get operations 1) (get operations 3)
                                          (get operations 2) (get operations 4)])]]
      (let [report (producer/invoke 'sh12-verify-branch-join-mir core candidate)
            result (run core candidate [Long/MAX_VALUE 1])]
        (is (= :rejected (:status report) (:status result)))
        (is (= (:diagnostics report) (:diagnostics result)))
        (is (nil? (:execution result)))))))

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

(deftest sh13-checked-i64-add-original-guard-endpoint-kernel
  ;; ONE unchanged current original definition, supplied scalar DATA only.
  (let [path (str (.resolve @(producer/existing 'root) (producer/existing 'c11-source)))
        forms (binding [*read-eval* false] (read-string (str "[" (slurp path) "]")))
        selected (filterv #(and (seq? %) (= 'defn (first %)) (= 'sh13-add-safe? (second %))) forms)
        text (str "(ns gravity.test.original-add-guard-kernel (:profile :meta) (:target :jvm)"
                  " (:effects #{}) (:capabilities #{}) (:safety :safe))\n"
                  (apply str (map #(str (pr-str %) "\n") selected)))
        callees (set (for [form selected expression (tree-seq coll? seq form)
                          :when (and (seq? expression) (symbol? (first expression)))] (first expression)))
        _ (assert (= 1 (count selected)))
        _ (assert (< (count text) 2048))
        _ (assert (every? '#{defn if > < <= >= = -} callees) (pr-str {:callees callees}))
        emitter (:emitter (gravity.bootstrap/c-backend-stage2-plan-emitter-source-rule! path :jvm))
        plan (gravity.bootstrap/p15-s23-stage2-compiler-artifact-plan emitter "original-add-guard-kernel.gravity" text)
        values [Long/MIN_VALUE (inc Long/MIN_VALUE) -2 -1 0 1 2 (dec Long/MAX_VALUE) Long/MAX_VALUE]]
    (doseq [left values right values]
      (let [sum (+' (bigint left) (bigint right))
            expected (<= (bigint Long/MIN_VALUE) sum (bigint Long/MAX_VALUE))
            actual (gravity.bootstrap/p15-s23-stage2-runtime-execute-function
                     {:engine :sh13-original-add-guard-data-kernel :compiler-artifact-plan? true}
                     plan 'sh13-add-safe? [left right])]
        (is (= expected actual) (str "left=" left " right=" right))))))
