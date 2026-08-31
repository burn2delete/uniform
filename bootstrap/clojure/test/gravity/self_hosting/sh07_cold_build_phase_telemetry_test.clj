(ns gravity.self-hosting.sh07-cold-build-phase-telemetry-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-cold-build-phase-telemetry
             :as telemetry]))

(defn- diagnostic
  [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest synthetic-profile-records-phases-and-digest-cardinality
  (let [events (atom [])
        receipt
        (telemetry/run-profile
         "synthetic.gravity"
         {:maximum-progress-events 16
          :on-progress #(swap! events conj %)
          :runner
          (fn [{:keys [observe-phase observe-digest]}]
            (observe-phase
             :sh07-authenticated-request
             #(observe-digest
               :sh07-core-node-id
               {:nodes (vec (range 3))
                :children [:left :right]
                :unrelated :ignored}
               (constantly {:artifact :synthetic :status :accepted}))))})]
    (is (= :gravity/sh07-cold-build-phase-telemetry-v1 (:schema receipt)))
    (is (= :non-authoritative (:authority receipt)))
    (is (false? (:authoritative? receipt)))
    (is (= :map (get-in receipt [:result-summary :value-kind])))
    (is (= 1
           (get-in receipt
                   [:phase-observations :sh07-authenticated-request
                    :call-count])))
    (is (= 1
           (get-in receipt
                   [:digest-observations :sh07-core-node-id :call-count])))
    (is (= 5
           (get-in receipt
                   [:digest-observations :sh07-core-node-id
                    :selected-entry-count])))
    (is (= 3
           (get-in receipt
                   [:digest-observations :sh07-core-node-id
                    :maximum-cardinality :nodes])))
    (is (= 3 (count @events)))
    (is (every? #(contains? % :ordinal) @events))
    (is (false? (:progress-truncated? receipt)))
    (is (false? (:persistent-cache-authority? receipt)))
    (is (= :not-performed (:semantic-result-comparison receipt)))))

(deftest progress-is-bounded-and-callback-failure-is-non-authoritative
  (let [events (atom [])
        receipt
        (telemetry/run-profile
         "synthetic.gravity"
         {:maximum-progress-events 2
          :on-progress (fn [event]
                         (swap! events conj event)
                         (throw (ex-info "synthetic observer failure" {})))
          :runner
          (fn [{:keys [observe-phase]}]
            (dotimes [_ 3]
              (observe-phase :sh07-authenticated-request (constantly :ok))))})]
    (is (= 4 (:progress-event-count receipt)))
    (is (true? (:progress-truncated? receipt)))
    (is (= 2 (count @events)))
    (is (= 2 (:progress-callback-error-count receipt)))
    (is (= 3
           (get-in receipt
                   [:phase-observations :sh07-authenticated-request
                    :call-count])))))

(deftest cardinality-bounds-infinite-sequences-before-realization
  (let [observations (atom [])
        receipt
        (telemetry/run-profile
         "synthetic.gravity"
         {:maximum-cardinality-per-key 2
          :on-progress #(swap! observations conj %)
          :runner
          (fn [{:keys [observe-digest]}]
            (observe-digest
             :sh07-core-artifact-id
             {:nodes (map identity (range))}
             (constantly :done)))})
        cardinality (->> @observations
                         (filter #(= :digest (:kind %)))
                         first
                         :cardinality)]
    (is (= 2 (get-in cardinality [:selected :nodes :count])))
    (is (true? (get-in cardinality [:selected :nodes :truncated?])))
    (is (true? (:truncated? cardinality)))
    (is (= 1
           (get-in receipt
                   [:digest-observations :sh07-core-artifact-id
                    :call-count])))))

(deftest failed-digest-is-reported-before-error-escapes
  (let [events (atom [])
        failure
        (try
          (telemetry/run-profile
           "synthetic.gravity"
           {:on-progress #(swap! events conj %)
            :runner
            (fn [{:keys [observe-digest]}]
              (observe-digest
               :unknown-purpose
               {:children [:one]}
               #(throw (ex-info "synthetic digest failure"
                                {:id "SYNTHETIC-DIGEST-FAILURE"}))))})
          nil
          (catch clojure.lang.ExceptionInfo error
            (ex-data error)))
        digest-event (last (filter #(= :digest (:kind %)) @events))]
    (is (= "SYNTHETIC-DIGEST-FAILURE" (:id failure)))
    (is (= :digest (:kind digest-event)))
    (is (= :other (:purpose digest-event)))
    (is (= :failed (:status digest-event)))
    (is (= 1 (get-in digest-event [:cardinality :selected :children :count])))))

(deftest options-and-required-seams-fail-closed
  (is (= "SH07-COLD-TELEMETRY-SOURCE"
         (:id (diagnostic #(telemetry/run-profile "")))))
  (is (= "SH07-COLD-TELEMETRY-OPTIONS"
         (:id (diagnostic #(telemetry/run-profile
                            "synthetic.gravity"
                            {:unknown-option true})))))
  (is (= "SH07-COLD-TELEMETRY-OPTIONS"
         (:id (diagnostic #(telemetry/run-profile
                            "synthetic.gravity"
                            {:maximum-cardinality-per-key 0})))))
  (is (= "SH07-COLD-TELEMETRY-OPTIONS"
         (:id (diagnostic #(telemetry/run-profile
                            "synthetic.gravity"
                            {:maximum-digest-purpose-rows 3})))))
  (is (= "SH07-COLD-TELEMETRY-OPTIONS"
         (:id (diagnostic #(telemetry/run-profile
                            "synthetic.gravity"
                            {:maximum-digest-purpose-rows nil})))))
  (is (= "SH07-COLD-TELEMETRY-OPTIONS"
         (:id (diagnostic #(telemetry/run-profile
                            "synthetic.gravity"
                            {:on-progress :not-callable})))))
  (let [wrapper-roots (ns-resolve
                       'gravity.self-hosting.sh07-cold-build-phase-telemetry
                       'wrapper-roots)
        new-state (ns-resolve
                   'gravity.self-hosting.sh07-cold-build-phase-telemetry
                   'new-state)
        state (@new-state {})
        roots (@wrapper-roots state)
        symbols ['sh07-core-build-binding!
                 'sh06-resolution-source-artifact
                 'sh07-core-authenticated-request
                 'sh07-core-run-structural-request-for-test
                 'sh07-core-digest-requests
                 'sh07-core-resolve-digest-preimage!]]
    (is (every? #(contains? roots (ns-resolve 'gravity.bootstrap %))
                symbols))))

(deftest synthetic-runner-result-is-not-replaced-by-telemetry
  (let [sentinel (Object.)
        seen (atom nil)
        receipt (telemetry/run-profile
                 "synthetic.gravity"
                 {:runner (fn [_]
                            (reset! seen sentinel)
                            sentinel)})]
    (is (identical? sentinel @seen))
    (is (= :scalar (get-in receipt [:result-summary :value-kind])))
    (is (= :gravity/sh07-cold-build-phase-telemetry-v1
           (:schema receipt)))))

(deftest bootstrap-seam-wrapper-attributes-an-invoked-core-operation
  (let [execute-var (ns-resolve 'gravity.bootstrap 'sh07-core-execute!)
        receipt
        (with-redefs-fn
         {execute-var (fn [_ _ _] {:status :accepted})}
         #(telemetry/run-profile
           "synthetic.gravity"
           {:runner
            (fn [_]
              (@execute-var "synthetic.gravity"
                            'sh07-build-core-template
                            []))}))]
    (is (= 1
           (get-in receipt
                   [:phase-observations :core-template-construction
                    :call-count])))
    (is (= :passed
           (get-in receipt
                   [:phase-observations :core-template-construction
                    :last-status])))
    (is (= :accepted (get-in receipt [:result-summary :status])))))
