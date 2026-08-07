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
                              {:op :local :name 'index}]}}]}]}}}]
    (is (= 15
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'sum-to [5 0])))
    (is (= [9 2]
           (bootstrap/p15-s23-stage2-runtime-execute-function
            runtime function-plan 'loop-scope [9])))))
