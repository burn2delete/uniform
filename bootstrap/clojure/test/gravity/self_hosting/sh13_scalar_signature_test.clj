(ns gravity.self-hosting.sh13-scalar-signature-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.sh13-control-flow-test]))

(defn- existing [name]
  (var-get (ns-resolve 'gravity.self-hosting.sh13-control-flow-test name)))

(defn- invoke [function & arguments]
  ((existing 'invoke-engine) function (vec arguments)))

(defn- with-unused-parameter [base type]
  ;; Existing normalized MIR constructor, not authenticated source/Core.
  ;; :error-main is uncalled by :choose, and returns its original :reason.
  (-> base
      (assoc-in [:functions 5 :params] [:reason :unused])
      (assoc-in [:functions 5 :param-types] [:keyword type])
      (assoc-in [:functions 5 :blocks 0 :params] [:reason :unused])
      (assoc-in [:functions 5 :blocks 0 :param-types] [:keyword type])))

(defn- check-rejection [subject category]
  (let [result ((existing 'run) subject [true])
        expected (invoke 'sh13-diagnostic subject category)]
    (is (= category (invoke 'sh13-module-error subject)))
    (is (= :rejected (:status result)))
    (is (= [expected] (:diagnostics result)))
    (is (nil? (:execution result)))
    (is (= :passed (:status (invoke 'sh13-verify-execution subject [true] result))))))

(deftest sh13-unused-parameter-types-are-verified-before-execution
  ;; C11 requires complete typed MIR; SH13's executable scalar domain is
  ;; i64/bool/keyword/nil even when a parameter or function is not used.
  (doseq [plan [(existing 'accepted-gravity-plan) (existing 'accepted-qst-plan)]]
    (let [base ((existing 'module) plan 'sh13-branch-module)
          snapshot base]
      (doseq [type [:i64 :bool :keyword :nil]]
        (let [valid (with-unused-parameter base type)]
          (is (nil? (invoke 'sh13-module-error valid)))
          (doseq [[condition value] [[true 42] [false 7]]]
            (let [result ((existing 'run) valid [condition])]
              (is (= :accepted (:status result)))
              (is (= value (get-in result [:execution :value])))
              (is (not-any? #(= :error-main (:function %)) (get-in result [:execution :trace])))
              (is (= :passed (:status (invoke 'sh13-verify-execution valid [condition] result))))))))
      (doseq [type [nil :unit :string :dynamic :gravity/i64 :gravity.type/i64 {} []]]
        (let [invalid (with-unused-parameter base type)
              accepted ((existing 'run) base [true])]
          (check-rejection invalid :type)
          (is (= :rejected (:status (invoke 'sh13-verify-execution invalid [true] accepted))))))
      (is (= snapshot base)))))

(deftest sh13-nonreturning-functions-still-require-scalar-result-types
  (doseq [plan [(existing 'accepted-gravity-plan) (existing 'accepted-qst-plan)]]
    (let [base ((existing 'module) plan 'sh13-branch-module)
          branch ((existing 'invoke) plan 'sh13-branch [:exit/entry [:reason] 158 164])
          looped (assoc-in base [:functions 5 :blocks 0 :terminator] branch)]
      ;; An uncalled bounded loop has no return use to validate its header.
      ;; No loop executes here; the existing :choose fixture remains the entry.
      (doseq [type [:i64 :bool :keyword :nil]]
        (let [subject (assoc-in looped [:functions 5 :return-type] type)
              result ((existing 'run) subject [true])]
          (is (nil? (invoke 'sh13-module-error subject)))
          (is (= :accepted (:status result)))
          (is (= 42 (get-in result [:execution :value])))))
      (doseq [type [:unit :string :dynamic :gravity/i64 :gravity.type/i64 {} []]]
        (check-rejection (assoc-in looped [:functions 5 :return-type] type) :type))
      ;; Existing missing-header classification remains a structural failure.
      (check-rejection (assoc-in looped [:functions 5 :return-type] nil) :module))))

(deftest sh13-signature-check-preserves-existing-diagnostic-precedence
  (let [base ((existing 'module) (existing 'accepted-gravity-plan) 'sh13-branch-module)
        invalid (with-unused-parameter base :dynamic)
        operation [:functions 0 :blocks 1 :operations 0]]
    (doseq [[subject category]
            [[(assoc invalid :artifact :wrong) :module]
             [(assoc-in invalid [:functions 5 :param-types] nil) :module]
             [(assoc-in invalid [:functions 0 :blocks 1 :param-types] nil) :block]
             [(assoc-in invalid (conj operation :operands) [:missing]) :dominance]
             [(assoc-in invalid (conj operation :source-span) nil) :origin]
             [(assoc-in invalid (conj operation :effects) #{:io/write}) :effect]
             [(assoc-in invalid (into operation [:facts :type]) :wrong) :verify]]]
      (check-rejection subject category))))
