(ns gravity.self-hosting.p15-bounded-profile-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.self-hosting.p15-bounded-profile :as profile]))

(defn- accounting [receipt] (dissoc receipt :observations))

(def ^:private structural-plan
  {:plan-id "sha256:structural-p15-profile-plan"
   :entrypoint 'main
   :functions {'main {:instructions [{:op :function-call
                                      :function 'render
                                      :args [{:op :literal :value "ok"}]}]}
               'render {:instructions [{:op :local :name 'value}]}}})

(def ^:private structural-proof-dag
  {:observed-node-kind :source-data
   :source-record-identical? true
   :source-text-identical? true
   :context-artifact-call-count 2
   :context-artifact-hit-count 1
   :context-artifact-build-count 1
   :context-artifact-node-count 1})

(deftest bounded-profile-receipt-is-non-authoritative-and-accounted
  (let [receipt (profile/run-profile {:emit-plan (constantly structural-plan)
                                      :observe-proof-dag
                                      (constantly structural-proof-dag)})
        stage2 (:stage2 receipt)
        proof-dag (:proof-dag receipt)]
    (is (= :gravity/p15-bounded-profile-receipt-v1 (:schema receipt)))
    (is (= :non-authoritative (:authority receipt)))
    (is (true? (:deterministic-accounting? receipt)))
    (is (pos? (:instruction-count stage2)))
    (is (pos? (:function-count stage2)))
    (is (pos? (:function-call-count stage2)))
    (is (pos? (:max-frame-depth stage2)))
    (is (= (:context-artifact-build-count proof-dag)
           (:context-artifact-node-count proof-dag)))
    (is (pos? (:context-artifact-build-count proof-dag)))
    (is (pos? (:context-artifact-hit-count proof-dag)))
    (is (true? (:source-record-identical? proof-dag)))
    (is (true? (:source-text-identical? proof-dag)))
    (is (every? keyword? (:nonclaims receipt)))
    (is (= (:receipt-id receipt)
           (str "sha256:"
                (gravity.bootstrap/sha256-hex
                 (pr-str (dissoc (accounting receipt) :receipt-id))))))
    (doseq [[phase duration] (get-in receipt [:observations :phase-duration-ns])]
      (is (pos? duration) phase))
    (doseq [[phase allocation] (get-in receipt [:observations :allocation])]
      (is (boolean? (:allocation-telemetry-available? allocation)) phase)
      (when (:allocation-telemetry-available? allocation)
        (is (number? (:allocated-bytes allocation)) phase)))))
