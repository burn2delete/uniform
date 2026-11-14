(ns gravity.self-hosting.sh08-normal-return-inference-kernel-test
  "L2/L5/C7 ordered evaluation retains unresolved normal-completion facts."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh08-function-call-kernel-support :as support]))

(defn- normal-return [form children nominal]
  (support/invoke-c7 'sh08-ft-normal-return-type [form children nominal]))

(deftest ordered-evaluation-defers-a-normal-result-with-an-unresolved-child
  ;; A later pass may establish that the child transfers. Inferring the last
  ;; value now can freeze an invalid branch join into a function return type.
  (doseq [form [:do :let :call :loop]
          missing [nil :gravity.type/unknown]
          children [[missing :gravity.type/string]
                    [:gravity.type/keyword missing :gravity.type/string]]]
    (testing (pr-str [form children])
      (is (= :gravity.type/unknown
             (normal-return form children :gravity.type/string)))))
  (doseq [form [:do :let :call :loop]]
    (is (= :gravity.type/string
           (normal-return form [:gravity.type/keyword :gravity.type/string]
                          :gravity.type/string)))))

(deftest established-nonreturning-evaluation-dominates-unresolved-children
  ;; Keep scanning after unknown: a later transfer still proves that no normal
  ;; result exists. The unresolved child's own type obligation remains separate.
  (doseq [form [:do :let :call :loop]
          children [[:gravity.type/unknown :gravity.type/never]
                    [:gravity.type/never :gravity.type/unknown]
                    [:gravity.type/string :gravity.type/unknown :gravity.type/never]]]
    (is (= :gravity.type/never
           (normal-return form children :gravity.type/string))))
  (doseq [children [[:gravity.type/unknown] [nil :gravity.type/string]]]
    (is (= :gravity.type/never
           (normal-return :throw children :gravity.type/never))))
  (is (= :gravity.type/unknown
         (normal-return :do [:gravity.type/string] :gravity.type/unknown))))

(deftest function-construction-and-branch-rules-retain-their-own-evaluation-policy
  (let [signature {:kind :gravity.type/function}]
    (is (= signature
           (normal-return :fn [:gravity.type/unknown] signature))))
  (is (= :gravity.type/keyword
         (normal-return :if [:gravity.type/bool :gravity.type/never :gravity.type/keyword]
                        :gravity.type/unknown)))
  (is (= :gravity.type/unknown
         (normal-return :if [:gravity.type/bool :gravity.type/unknown :gravity.type/keyword]
                        :gravity.type/keyword)))
  (is (= :gravity.type/never
         (normal-return :if [:gravity.type/never :gravity.type/string :gravity.type/keyword]
                        :gravity.type/unknown)))
  (is (= :gravity.type/keyword
         (normal-return :try [:gravity.type/never :gravity.type/keyword]
                        :gravity.type/unknown)))
  (is (= :gravity.type/unknown
         (normal-return :try [:gravity.type/unknown :gravity.type/keyword]
                        :gravity.type/keyword))))

(deftest ordinary-node-inference-waits-for-evaluated-call-arguments
  (let [call {:node-id :call :core-form :call :children [:operator :argument]}
        edge {:call-core-node-id :call :classification :local-function
              :callee-function-syntax-id :callee}
        signature {:return :gravity.type/string}
        infer (fn [argument]
                (support/invoke-c7 'sh08-ft-infer-node-with-context
                                  [call {:operator signature :argument argument}
                                   {} {:callee signature} [edge] {}]))]
    (is (= :gravity.type/unknown (infer :gravity.type/unknown)))
    (is (= :gravity.type/string (infer :gravity.type/integer)))
    (is (= :gravity.type/never (infer :gravity.type/never)))))
