(ns gravity.types.set-equality-source-check-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.types.set-equality-source-check :as check]))

(defn- operations [events reject-stage]
  (let [descriptor {:kind :gravity.type/applied-checked-bare-set-equality
                    :argument-types [:gravity.type/set :gravity.type/set]}
        call {:core-node-id :call :operator-node-id :operator :argument-node-ids [:a :b]}
        edge {:call-core-node-id :call :ordinal 0 :caller-function-syntax-id :owner}
        nodes {:call {:node-id :call} :a {:attributes {:binding-id :left}} :b {:attributes {:binding-id :right}}}
        bindings [{:binding-id :left :name 'left} {:binding-id :right :name 'right}]
        core {:calls [call] :call-edges [edge] :function-records [{:function-syntax-id :owner}]}
        request {:canonical-core-artifact core :b47-context {:authenticated-core-request {:binding-table bindings}}}
        row {:lhs {:core-node-id :call :projection :checked-bare-set-equality}
             :producer-rule :sh08-checked-bare-set-equality-v1 :constraint-id-request {:domain :dse}}
        fact {:call-core-node-id :call :dispatch :direct-checked-bare-set-equality :fact-id-request {:domain :dse}}
        typed {:status :accepted :type-table {:operator descriptor}
               :constraint-ledger [row] :call-type-facts [fact]}
        evidence (fn [function & args]
                   (swap! events conj function)
                   (let [[_ ledger facts] args]
                     (if (= reject-stage :consumer-false-accept) {:status :accepted}
                       (do (is (= 3 (+ (count ledger) (count facts))))
                           {:status :rejected :reason :invalid-checked-bare-set-equality-source}))))]
    {:read-source (fn [path] (swap! events conj [:read path])
                    (if (and (= reject-stage :paired-bytes) (.endsWith ^String path ".qst")) "other" "same"))
     :read-forms (fn [path _] (swap! events conj [:parse path]) [:same])
     :source-request (fn [path text inputs]
                       (swap! events conj :source-request)
                       (is (= (first check/fixture-paths) path))
                       (is (= "same" text))
                       (is (= [] (:module-artifacts inputs)))
                       {:request request})
     :c7 (fn [function _]
           (swap! events conj function)
           (case function
             sh08-function-type-core-artifact (if (= reject-stage :c7-producer) {:status :rejected} typed)
             sh08-verify-function-type-result {:status :passed}))
     :body (fn [function & args]
             (case function
               body-ae-context (do (swap! events conj function)
                                  (if (= reject-stage :effects-context) {:status :rejected}
                                    {:status :accepted :context {:nodes nodes :edges {:call edge}}}))
               body-ae-call-valid? (do (swap! events conj function) true)
               (apply evidence function args)))
     :emit (fn [function & args]
             (if (= 'p15-s23-typed-compile-node function)
               (do (swap! events conj function)
                   (is (= :call (second args)))
                   (is (= {:left 'left :right 'right} (:locals (first args))))
                   {:status :complete :value {:op :builtin-call :function '=
                                             :args [{:op :local :name 'left} {:op :local :name 'right}]}})
               (apply evidence function args)))}))

(deftest prepared-selector-order-and-bounded-result
  (let [events (atom []) result (check/check! (operations events nil))]
    (is (= :passed (:status result)))
    (is (= 8 (:independent-review-regressions result)))
    (is (= :bytes-and-seed-parser-only (:companion-check result)))
    (is (false? (:subject-runtime-executed result)))
    (is (= [:source-request 'sh08-function-type-core-artifact 'sh08-verify-function-type-result
            'body-ae-context 'body-ae-call-valid? 'p15-s23-typed-compile-node]
           (vec (filter #(or (= :source-request %) (symbol? %)) (take 10 @events)))))
    (is (= 4 (count (filter #{'effects-set-equality-evidence} @events))))
    (is (= 4 (count (filter #{'emit-set-equality-evidence} @events))))))

(deftest preparation-controls-stop-before-dependent-consumers
  (doseq [[stage forbidden] [[:paired-bytes :source-request]
                            [:c7-producer 'sh08-verify-function-type-result]
                            [:effects-context 'p15-s23-typed-compile-node]]]
    (let [events (atom []) result (try (check/check! (operations events stage))
                                     (catch clojure.lang.ExceptionInfo error (ex-data error)))]
      (is (= stage (:stage result)))
      (is (not-any? #{forbidden} @events))))
  (let [result (try (check/check! (operations (atom []) :consumer-false-accept))
                    (catch clojure.lang.ExceptionInfo error (ex-data error)))]
    (is (= :duplicate-row (:stage result)))))
