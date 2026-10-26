(ns gravity.self-hosting.mir-scalar-admission-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh12-branch-join-mir-test name)))

(defn- invoke [name & arguments]
  ((existing 'invoke) (existing 'c11-plan) name (vec arguments)))

(deftest scalar-length-admission-preserves-carrier-and-identity-contracts
  ;; Finite supplied DATA, not fresh authenticated source ingress. Larger
  ;; scalar admission does not establish arbitrary heap or CPU safety.
  (let [text (apply str (repeat 65537 "x"))
        scalars [text (symbol text) (keyword text)]
        small {:field ["x" 'x :x]}
        nested {:field scalars}
        preflight #(invoke 'sh12-authenticated-c10-mir-carrier-preflight %)]
    (doseq [scalar scalars]
      (is (> (count (str scalar)) 65536))
      (is (true? (invoke 'sh12-c6p-scalars-valid? scalar)))
      (is (= (preflight "x") (preflight scalar))))
    (is (true? (invoke 'sh12-c6p-scalars-valid? nested)))
    (is (= {:status :accepted :nodes 6 :enqueued 6 :nodes-exact? true}
           (preflight nested)))
    (is (= (preflight small) (preflight nested)))
    (doseq [value [1.5 1/2 1M]]
      (is (false? (invoke 'sh12-c6p-scalars-valid? value)))
      (is (false? (invoke 'sh12-c6p-scalars-valid? {:field [text value]}))))
    (is (true? (invoke 'sh12-c10-mir-sha256-id? (str "sha256:" (apply str (repeat 64 "a"))))))
    (doseq [identity [text (str "sha256:" text) (symbol text) 42]]
      (is (false? (invoke 'sh12-c10-mir-sha256-id? identity))))
    (is (= :c10-mir-carrier-width-bound
           (:reason (preflight (vec (repeat 1025 text))))))
    (let [calls (atom 0)
          poison (lazy-seq (swap! calls inc) (throw (ex-info "must not realize" {})))]
      (is (= :c10-mir-carrier-seq-unsupported (:reason (preflight {:field poison}))))
      (is (zero? @calls)))))

(deftest scalar-kind-acceptance-does-not-authorize-invalid-mir-indices-or-types
  ;; Existing supplied checked-Core fixture and real Gravity builder/verifier;
  ;; no C6 constructor, C10 authentication, C12 or identity repinning.
  (let [core ((existing 'checked-core) ".gravity")
        mir (invoke 'sh12-build-branch-join-mir core)
        function-id (:entrypoint core)
        entry (get-in mir [:functions function-id :entry])
        instruction [:functions function-id :blocks entry :instructions 0]
        edge-index (first (keep-indexed #(when (contains? %2 :operand-index) %1)
                                         (get-in mir [:data-flow-graph :edges])))
        long-text (apply str (repeat 65537 "x"))]
    (is (= :passed (:status (invoke 'sh12-verify-branch-join-mir core mir))))
    (is (integer? edge-index))
    (doseq [candidate [(assoc-in mir [:data-flow-graph :edges edge-index :operand-index] -1)
                       (assoc-in mir [:data-flow-graph :edges edge-index :operand-index] long-text)
                       (assoc-in mir (conj instruction :type) :invalid-type)
                       (assoc mir :module-id long-text)
                       (assoc mir :source-core long-text)]]
      (is (true? (invoke 'sh12-c6p-scalars-valid? candidate)))
      (is (= :rejected (:status (invoke 'sh12-verify-branch-join-mir core candidate)))))))
