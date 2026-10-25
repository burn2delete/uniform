(ns gravity.self-hosting.sh22-operation-membership-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]))

;; STD3 membership distinguishes an absent nil from a stored nil. STD1's
;; declared operation set is closed before authority checks or dispatch.
(def ^:private root
  (delay
    (loop [file (io/file (.toURI (io/resource "gravity/self_hosting/sh22_operation_membership_test.clj")))]
      (if (.isFile (io/file file "deps.edn")) file (recur (.getParentFile file))))))
(defn- path [relative] (str (io/file @root relative)))
(defn- compile-plan [relative]
  (let [source (path relative)
        emitter (:emitter (bootstrap/c-backend-stage2-plan-emitter-source-rule! source :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan emitter source (slurp source))))
(def ^:private engine
  (delay (compile-plan "bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity")))
(defn- fixture-path [family extension]
  (str "bootstrap/clojure/fixtures/self-hosting/sh-22/" family "/coordinator-operation-membership." extension))
(def ^:private fixtures
  (delay (into {} (for [family ["accepted" "rejected"] extension ["gravity" "qst"]]
                   [[family extension] (compile-plan (fixture-path family extension))]))))
(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh22-standard-library-leaf :compiler-artifact-plan? true} plan function arguments))
(defn- call [function arguments] (invoke @engine function arguments))
(defn- handle [input] (call 'sh22-handle-request [input]))
(defn- verified? [input result] (= :passed (:status (call 'sh22-verify-result [input result]))))
(defn- base-request [extension]
  (invoke (get @fixtures ["accepted" extension]) 'sh22-operation-membership-request []))
(defn- observe-dispatch [input]
  (let [scoped (ns-resolve 'gravity.bootstrap 'p15-s23-stage2-runtime-execute-function-scoped)
        original @scoped calls (atom 0)
        result (with-redefs-fn
                 {scoped (fn [& args]
                           (when (= 'sh22-handle-valid-request (nth args 2)) (swap! calls inc))
                           (apply original args))}
                 #(handle input))]
    [result @calls]))

(deftest sh22-set-membership-distinguishes-absence-from-nil
  (doseq [[members value expected]
          [[#{} nil false] [#{:reader} nil false] [#{nil} nil true]
           [#{false} nil false] [#{nil false} false true] [#{} false false]
           [#{:reader :syntax} :reader true] [#{:reader} :syntax false]
           [#{\0 \a} \a true] [#{"STD1001"} "STD1001" true]
           [#{"STD1001"} "STD1002" false]]]
    (is (= expected (call 'sh22-member? [members value])))))

(deftest sh22-operation-membership-paired-producers-and-closed-dispatch
  (doseq [family ["accepted" "rejected"]]
    (is (= (slurp (path (fixture-path family "gravity")))
           (slurp (path (fixture-path family "qst"))))))
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension) original (:arguments base)]
      (doseq [[needle expected] [[:reader true] [:missing false]]]
        (let [input (assoc-in base [:arguments 1] needle) [result dispatches] (observe-dispatch input)]
          (is (= :accepted (:status result)))
          (is (= expected (:value result)))
          (is (= 1 dispatches))
          (is (= original (:arguments base)))
          (is (verified? input result))))
      (doseq [function ['sh22-nil-operation-request 'sh22-unknown-operation-request]]
        (let [input (invoke (get @fixtures ["rejected" extension]) function [base])
              [result dispatches] (observe-dispatch input)]
          (is (= :rejected (:status result)))
          (is (= "STD1001" (get-in result [:diagnostics 0 :rule])))
          (is (= :malformed-library-request (get-in result [:diagnostics 0 :reason])))
          (is (zero? dispatches))
          (is (false? (call 'sh22-request-common-valid? [input])))
          (is (verified? input result)))))))

(deftest sh22-undeclared-operations-reject-before-authority-and-forgery
  (doseq [extension ["gravity" "qst"]]
    (let [base (base-request extension)]
      (doseq [operation [nil :undeclared-operation 'ordered-set-contains-keyword
                        "ordered-set-contains-keyword" false 0 []]
              authority [{:effects #{} :capabilities #{} :allocation-policy :none}
                         {:effects #{:memory/allocate} :capabilities #{} :allocation-policy :none}
                         {:effects #{:io/write} :capabilities #{:io/stdout} :allocation-policy :provider-owned}]]
        (let [input (merge base authority {:operation operation}) [result dispatches] (observe-dispatch input)]
          (is (= :rejected (:status result)))
          (is (= "STD1001" (get-in result [:diagnostics 0 :rule])))
          (is (= :malformed-library-request (get-in result [:diagnostics 0 :reason])))
          (is (zero? dispatches))
          (is (verified? input result))))
      (let [valid (handle base)
            input (assoc base :operation nil :effects #{:memory/allocate})
            formerly-accepted (-> valid
                                  (assoc :operation nil)
                                  (assoc-in [:identity-input :operation] nil)
                                  (assoc-in [:identity-input :effects] #{:memory/allocate}))
            report (call 'sh22-verify-result [input formerly-accepted])]
        (is (= :rejected (:status report)))
        (is (= :library-result-substitution (get-in report [:diagnostics 0 :reason])))))))
