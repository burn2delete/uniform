(ns gravity.self-hosting.sh16-checked-i64-sub-fold-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; C13/D6/D8/D9: current-source normalized DATA, not authenticated C11 input,
;; native execution, proof-provider verification or module migration evidence.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource
                                "gravity/self_hosting/sh16_checked_i64_sub_fold_test.clj")))]
      (cond
        (nil? file) (throw (ex-info "Missing repository root" {}))
        (.isFile (io/file file "deps.edn")) file
        :else (recur (.getParentFile file))))))

(defn- source-path [relative] (str (io/file @root relative)))

(defn- compile-plan [relative]
  (let [path (source-path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule!
                           path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter path (slurp path))))

(def ^:private engine
  (delay (compile-plan
          "bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity")))

(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-16/" family
       "/coordinator-checked-i64-sub-fold" extension))

(def ^:private fixtures
  (into {} (for [family ["accepted" "rejected"] extension [".gravity" ".qst"]]
             [[family extension] (delay (compile-plan (fixture-path family extension)))])))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh16-checked-sub-development :compiler-artifact-plan? true}
   @plan function arguments))

(defn- request [extension left right]
  (invoke (get fixtures ["accepted" extension]) 'checked-sub-request [left right]))

(defn- optimize [input] (invoke engine 'sh16-optimize [input]))
(defn- verify [input result] (invoke engine 'sh16-verify [input result]))

(defn- check-rejection [input rule]
  (let [result (optimize input)]
    (is (= :rejected (:status result)))
    (is (= rule (get-in result [:diagnostics 0 :rule])))
    (is (nil? (:optimized-operations result)))
    (is (= :passed (:status (verify input result))))))

(deftest sh16-subtraction-fixtures-use-both-source-spellings
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (source-path (fixture-path family ".gravity")))
           (slurp (source-path (fixture-path family ".qst"))))))
  (is (= (request ".gravity" 7 3) (request ".qst" 7 3)))
  (doseq [extension [".gravity" ".qst"]]
    (check-rejection
     (invoke (get fixtures ["rejected" extension]) 'overflowing-sub-request [])
     "C13-SAFETY")))

(deftest sh16-subtraction-folds-representable-endpoints-and-preserves-facts
  ;; The actual Gravity constructor feeds the original optimizer, subsequent
  ;; pass consumers and independent public recomputation verifier.
  (doseq [extension [".gravity" ".qst"]
          [left right expected]
          [[7 3 4] [3 7 -4] [0 0 0] [5 -3 8]
           [Long/MIN_VALUE 0 Long/MIN_VALUE]
           [Long/MAX_VALUE 0 Long/MAX_VALUE]
           [Long/MIN_VALUE Long/MIN_VALUE 0]
           [Long/MAX_VALUE Long/MAX_VALUE 0]
           [-1 Long/MIN_VALUE Long/MAX_VALUE]
           [0 Long/MAX_VALUE (- Long/MAX_VALUE)]
           [-1 Long/MAX_VALUE Long/MIN_VALUE]
           [Long/MIN_VALUE -1 (inc Long/MIN_VALUE)]
           [Long/MAX_VALUE 1 (dec Long/MAX_VALUE)]]]
    (let [input (request extension left right)
          result (optimize input)
          operation (first (:operations input))
          folded (first (:optimized-operations result))
          ledger (first (:invalidation-ledger result))]
      (is (= :accepted (:status result)))
      (is (= :const (:opcode folded)))
      (is (= [expected] (:operands folded)))
      (is (= (dissoc operation :opcode :operands :constant-operands :numeric-mode)
             (dissoc folded :opcode :operands)))
      (is (= [:op/sub] (get-in result [:decision-log 0 :changed-operations])))
      (is (= [:data-flow-cache] (:analysis-invalidated ledger)))
      (is (= [:data-flow-facts :range-facts :safety-facts] (:facts-invalidated ledger)))
      (is (= [:proof/bounds] (:proofs-invalidated ledger)))
      (is (= [:post-pass-verifier-report] (:facts-regenerated ledger)))
      (is (= [:check/bounds] (get-in result [:residual-check-report :retained-runtime-checks])))
      (is (empty? (:check-elision-records result)))
      (is (= :passed (get-in result [:post-pass-verifier :status])))
      (is (= :accepted (get-in result [:translation-validation :result])))
      (is (= :passed (:status (verify input result)))))))

(deftest sh16-subtraction-rejects-overflow-and-invalid-fold-premises
  (let [base (request ".gravity" 7 3)]
    (doseq [operands [[Long/MIN_VALUE 1] [Long/MAX_VALUE -1]
                      [0 Long/MIN_VALUE] [Long/MAX_VALUE Long/MIN_VALUE]
                      [Long/MIN_VALUE Long/MAX_VALUE] [1] [1 2 3]
                      [true 1] [nil 1] [:value/unknown 1]]]
      (check-rejection (assoc-in base [:operations 0 :operands] operands) "C13-SAFETY"))
    (doseq [operands [[(inc (bigint Long/MAX_VALUE)) 0]
                      [0 (dec (bigint Long/MIN_VALUE))]]]
      (let [input (assoc-in base [:operations 0 :operands] operands)
            result (optimize input)]
        (is (= :rejected (:status result)))
        (is (= "C13-CONTRACT" (get-in result [:diagnostics 0 :rule])))
        (is (= :request-structural-bound (get-in result [:diagnostics 0 :reason])))
        (is (= :rejected (:status (verify input result))))))
    (doseq [type [:bool :u64 :foreign/i64 'i64]]
      (check-rejection (assoc-in base [:operations 0 :type] type) "C13-SAFETY"))
    (doseq [mode [nil :wrapping :fast-approx]]
      (check-rejection (assoc-in base [:operations 0 :numeric-mode] mode) "C13-SAFETY"))
    (check-rejection (update-in base [:operations 0] dissoc :numeric-mode) "C13-SAFETY")
    (check-rejection (assoc-in base [:operations 0 :constant-operands] nil) "C13-CONTRACT")
    (check-rejection (assoc-in base [:operations 0 :effects] #{:io/write}) "C13-EFFECT")
    (check-rejection (assoc-in base [:operations 0 :origin-chain] []) "C13-CONTRACT")))

(deftest sh16-subtraction-retains-ineligible-operations-and-checks
  (let [base (request ".gravity" 7 3)
        seed (first (:operations base))]
    (doseq [operation [(assoc seed :constant-operands false)
                       (assoc seed :constant-operands false :operands [:value/x 3])
                       (assoc seed :constant-operands false :operands [Long/MIN_VALUE 1])
                       (assoc seed :safety-outcome :runtime-checked)
                       (assoc seed :effects #{:io/write} :ordering :sequence)
                       (assoc seed :safety-outcome :unsafe-island :unsafe-audit-id :audit/sub)
                       (assoc seed :type :bool :safety-outcome :unsafe-island :unsafe-audit-id :audit/sub)]]
      (let [input (assoc-in base [:operations 0] operation)
            result (optimize input)]
        (is (= :accepted (:status result)))
        (is (= operation (first (:optimized-operations result))))
        (is (false? (get-in result [:decision-log 0 :changed?])))
        (is (= :passed (:status (verify input result))))))
    ;; Subtraction does not silently extend the established ADD propagation route.
    (let [producer (-> seed (dissoc :numeric-mode :constant-operands)
                       (assoc :op-id :op/input :opcode :const :result :value/x :operands [7]))
          operation (assoc seed :constant-operands false :operands [:value/x 3])
          input (assoc base :operations [producer operation (last (:operations base))])
          result (optimize input)]
      (is (= :accepted (:status result)))
      (is (= (:operations input) (:optimized-operations result)))
      (is (= :passed (:status (verify input result))))))
  (let [base (request ".gravity" 7 3)
        dead (assoc (first (:operations base)) :dead true
                    :dead-proof {:proof-id :proof/dead :operation-id :op/sub
                                 :no-uses true :effect-free true :verified true})
        ret (assoc (last (:operations base)) :operands [0])
        input (assoc base :operations [dead ret])
        result (optimize input)]
    (is (= :accepted (:status result)))
    (is (false? (get-in result [:decision-log 0 :changed?])))
    (is (= [:op/sub] (get-in result [:decision-log 2 :changed-operations])))
    (is (= [:proof/dead] (get-in result [:decision-log 2 :proofs-used])))
    (is (= [ret] (:optimized-operations result)))
    (is (= :passed (:status (verify input result))))))

(deftest sh16-subtraction-recomputation-rejects-substituted-results-and-evidence
  (let [input (request ".gravity" 7 3)
        result (optimize input)]
    (doseq [mutant [(assoc-in result [:optimized-operations 0 :operands] [-4])
                    (assoc-in result [:optimized-operations 0 :source-span :end] 9)
                    (assoc-in result [:optimized-operations 0 :safety-outcome] :runtime-checked)
                    (assoc-in result [:decision-log 0 :changed-operations] [])
                    (assoc-in result [:invalidation-ledger 0 :proofs-invalidated] [])
                    (update result :optimized-operations #(vec (remove (fn [op] (= :op/check (:op-id op))) %)))]]
      (is (= :rejected (:status (verify input mutant)))))
    (is (= :rejected (:status (verify (assoc-in input [:operations 0 :operands] [3 7]) result))))
    (is (= :rejected (:status (verify (assoc-in input [:operations 0 :opcode] :add) result))))))
