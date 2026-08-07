(ns gravity.self-hosting.sh01-stage2-runtime-iteration-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private runtime {:engine :stage2-runtime-iteration-test})
(def ^:private plan {:source {:path "stage2-runtime-iteration-test.gravity"}})

(defn- literal-instructions
  [values]
  (mapv (fn [value] {:op :literal :value value}) values))

(defn- diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest small-argument-carriers-preserve-values-and-order
  (doseq [values [[] [1] [1 2] [1 2 3] [1 2 3 4]]]
    (is (= values
           (bootstrap/p15-s23-stage2-runtime-execute-values
            runtime plan {} (literal-instructions values) :test)))))

(deftest vector-and-list-instruction-bodies-remain-equivalent
  (let [instructions (literal-instructions [1 2 3 4])]
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} instructions)))
    (is (= 4
           (bootstrap/p15-s23-stage2-runtime-execute-instructions
            runtime plan {} (apply list instructions))))))

(deftest non-tail-recur-remains-rejected-on-both-traversal-paths
  (let [instructions
        [{:op :recur :args [{:op :literal :value 1}]}
         {:op :literal :value :unreachable}]]
    (doseq [body [instructions (apply list instructions)]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-execute-instructions
               runtime plan {} body))]
        (is (= "L2-RECUR-TARGET" (:id data)))
        (is (= :non-tail-sequential-position (:reason data)))))))

(deftest recur-carrier-is-distinct-from-ordinary-runtime-maps
  (is (true?
       (bootstrap/p15-s23-stage2-runtime-recur-signal?
        (bootstrap/p15-s23-stage2-runtime-recur-signal [1 2 3]))))
  (doseq [value [{:values [1 2 3]}
                 {:p15-s23/recur-token (Object.) :values [1 2 3]}
                 (sorted-map 'artifact :unrelated)]]
    (is (false?
         (bootstrap/p15-s23-stage2-runtime-recur-signal? value)))))

(deftest hot-get-builtin-preserves-two-and-three-argument-semantics
  (is (= 1
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{:key 1} :key])))
  (is (= :missing
         (bootstrap/p15-s23-stage2-runtime-invoke-builtin
          plan 'get [{} :key :missing])))
  (testing "arity diagnostics remain fail closed"
    (doseq [arguments [[{}] [{} :key :fallback :extra]]]
      (let [data
            (diagnostic
             #(bootstrap/p15-s23-stage2-runtime-invoke-builtin
               plan 'get arguments))]
        (is (= "L2-BUILTIN-ARITY" (:id data)))
        (is (= (count arguments) (:actual-arity data)))))))

(deftest small-function-and-loop-binders-preserve-scope-and-recur
  (let [function-plan
        {:source {:path "stage2-runtime-iteration-test.gravity"}
         :functions
         {'sum-to
          {:params ['n 'sum]
           :instructions
           [{:op :if
             :test {:op :builtin-call
                    :function '>
                    :args [{:op :local :name 'n}
                           {:op :literal :value 0}]}
             :then {:op :recur
                    :args
                    [{:op :builtin-call
                      :function '-
                      :args [{:op :local :name 'n}
                             {:op :literal :value 1}]}
                     {:op :builtin-call
                      :function '+
                      :args [{:op :local :name 'sum}
                             {:op :local :name 'n}]}]}
             :else {:op :local :name 'sum}}]}
          'loop-scope
          {:params ['outer]
           :instructions
           [{:op :loop
             :bindings [{:name 'index
                         :expr {:op :literal :value 0}}]
             :body
             [{:op :if
               :test {:op :builtin-call
                      :function '<
                      :args [{:op :local :name 'index}
                             {:op :literal :value 2}]}
               :then {:op :recur
                      :args [{:op :builtin-call
                              :function '+
                              :args [{:op :local :name 'index}
                                     {:op :literal :value 1}]}]}
               :else {:op :vector-literal
                      :items [{:op :local :name 'outer}
                              {:op :local :name 'index}]}}]}]}
          'caller
          {:params ['value]
           :instructions
           [{:op :function-call
             :function 'sum-to
             :args [{:op :local :name 'value}
                    {:op :literal :value 0}]}]}}}]
    (is (= 15
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'sum-to [5 0])))
    (is (= [9 2]
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'loop-scope [9])))
    (is (= 6
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'caller [3])))))

(deftest common-builtin-arities-preserve-hosted-core-semantics
  (let [compiler-plan
        {:compiler-artifact-plan? true
         :kind :gravity/stage2-compiler-artifact-plan
         :module {:profile :meta}
         :compiler {:stage :p15-s23-stage2-expression-lowering}
         :source {:path "stage2-runtime-iteration-test.gravity"}}
        invoke
        (fn [callee arguments]
          (bootstrap/p15-s23-stage2-runtime-invoke-builtin
           plan callee arguments))]
    (doseq [[callee arguments expected]
            [['+ [] 0]
             ['+ [7] 7]
             ['+ [7 5] 12]
             ['+ [7 5 3] 15]
             ['+ [7 5 3 2] 17]
             ['- [7] -7]
             ['- [7 5] 2]
             ['- [7 5 3] -1]
             ['* [] 1]
             ['* [7] 7]
             ['* [7 5] 35]
             ['/ [4] 1/4]
             ['/ [12 3] 4]
             ['/ [24 3 2] 4]
             ['= [1] true]
             ['= [1 1] true]
             ['= [1 1 2] false]
             ['< [1 2] true]
             ['< [1 2 3] true]
             ['> [3 2 1] true]
             ['<= [1 1 2] true]
             ['>= [2 2 1] true]
             ['str [] ""]
             ['str ["a"] "a"]
             ['str ["a" 1] "a1"]
             ['str ["a" 1 :b] "a1:b"]
             ['first [[1 2]] 1]
             ['second [[1 2]] 2]
             ['count [[1 2]] 2]]]
      (is (= expected (invoke callee arguments))
          [callee arguments]))
    (is (= (list 2) (invoke 'rest [[1 2]])))
    (doseq [[callee arguments expected]
            [['symbol? ['value] true]
             ['keyword? [:value] true]
             ['vector? [[1 2]] true]
             ['map? [{:key 1}] true]
             ['contains? [{:key 1} :key] true]
             ['even? [2] true]
             ['integer? [2] true]
             ['keys [{:key 1}] (list :key)]
             ['quot [7 2] 3]
             ['subvec [[1 2 3] 1 3] [2 3]]]]
      (is (= expected
             (bootstrap/p15-s23-stage2-runtime-invoke-builtin
              compiler-plan callee arguments))
          [callee arguments]))
    (doseq [[callee arguments]
            [['- []] ['/ []] ['= []] ['< [1]] ['> [1]]]]
      (is (= "L2-BUILTIN-ARITY"
             (:id (diagnostic #(invoke callee arguments))))))))
