(ns gravity.self-hosting.sh01-native-original-bool-compatibility-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-zero-arity-bool-diamond-test]
            [gravity.self-hosting.sh12-branch-join-mir-test]))

(defn- owner [namespace name]
  (var-get (ns-resolve namespace name)))

(defn- original-pair [condition extension]
  ((owner 'gravity.self-hosting.sh12-zero-arity-bool-diamond-test 'original-pair)
   condition extension))

(defn- gravity-verify [core mir]
  ((owner 'gravity.self-hosting.sh12-branch-join-mir-test 'invoke)
   (owner 'gravity.self-hosting.sh12-branch-join-mir-test 'c11-plan)
   'sh12-verify-branch-join-mir [core mir]))

(defn- native-verify [core mir]
  (bootstrap/p15-s23-c11-mir-validate-constructed!
   "<native-original-bool-compatibility>" core mir))

(defn- native-diagnostic [core mir]
  (try (native-verify core mir) nil
       (catch clojure.lang.ExceptionInfo error
         (select-keys (ex-data error) [:id :missing-fact :fallback-status]))))

(deftest native-verifier-accepts-original-false-qst-and-rejects-substitution
  ;; D1/C6/C11: actual current source construction and fresh C6 replay through
  ;; the existing producer. No pinned public admission or backend execution.
  (let [left (original-pair "true" "gravity")
        right (original-pair "false" "qst")
        snapshot [left right]]
    (doseq [[label {:keys [raw upstream core mir]}] [["true/.gravity" left] ["false/.qst" right]]]
      (testing label
        (is (= :accepted (:status raw)))
        (is (= :passed (:status upstream)))
        (is (= :gravity/mir-module (:artifact mir)))
        (is (= :passed (:status (gravity-verify core mir))))
        (is (= :pending (:verification-status mir)))
        (is (= :constructed-unverified (get-in mir [:pass-execution-record :status])))
        (is (= {:status :passed :block-count 4 :operation-count 6 :conditional-count 1}
               (select-keys (native-verify core mir)
                            [:status :block-count :operation-count :conditional-count])))))
    (is (not= (get-in left [:core :artifact-id]) (get-in right [:core :artifact-id])))
    (is (= true (get-in left [:core :core-nodes 0 :attributes :value])))
    (is (= false (get-in right [:core :core-nodes 0 :attributes :value])))
    (testing "real source products cannot be crossed in either direction"
      (doseq [[core mir] [[(:core left) (:mir right)] [(:core right) (:mir left)]]]
        (is (= {:id "C11-MODULE" :missing-fact :authenticated-c11-module-envelope
                :fallback-status :rejected}
               (native-diagnostic core mir)))
        (is (= :rejected (:status (gravity-verify core mir))))))
    (let [{:keys [core mir]} right
          entry (get-in mir [:functions 'main :entry])
          altered (assoc-in mir [:functions 'main :blocks entry :instructions 0 :type] :gravity/integer)]
      (testing "a real operation must retain its checked Core type"
        (is (= {:id "C11-TYPE" :missing-fact :resolvable-id-referenced-operation-facts
                :fallback-status :rejected}
               (native-diagnostic core altered))))
      (testing "retired quota fields remain outside the closed scope schema"
        (is (= {:id "C11-VERIFY" :missing-fact :bounded-c11-and-b1-scope
                :fallback-status :rejected}
               (native-diagnostic core (assoc-in mir [:scope :maximum-module-carrier-nodes] 65536))))))
    (is (= snapshot [left right]))))
