(ns gravity.self-hosting.sh12-checked-mir-finalization-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh12-checked-i64-add-producer-test :as add]
            [gravity.self-hosting.sh12-checked-i64-sub-producer-test :as sub]
            [gravity.self-hosting.sh13-branch-join-execution-test]))

;; C11/D9: pending projection is not acceptance, verification or publication.
(defn- supplied-mir [pass-id]
  {:source-core :supplied-core
   :pass-contract {:pass-id pass-id :scope {:fixture :supplied-data}}
   :functions {'main {:blocks {:entry {:instructions
                                       [{:op-id :local :operands [:input] :facts {:type :i64} :verifier-status :passed}
                                        {:op-id :check :operands [:local] :facts {:check :overflow} :verifier-status :passed}]
                                      :terminator {:kind :return :operands [:local] :verifier-status :passed}}}}}
   :type-table {:local :i64} :safety-table {:check :runtime-checked}
   :runtime-check-table {:check {:failure :error/numeric}}
   :source-map {:local {:span :supplied-span}}
   :verification-status :passed
   :pass-execution-record {:status :verified-by-independent-seed :record-id :supplied-report}
   :b1-preflight {:status :verified-mir-candidate-for-b1 :backend-credit? true}
   :semantic-replay-parity false :live-invocation-claim? false :self-hosted? false})

(defn- pending-markers [mir]
  [(:verification-status mir)
   (get-in mir [:pass-execution-record :pass-contract-hash])
   (get-in mir [:pass-execution-record :output-content-id])
   (get-in mir [:pass-execution-record :verifier-report-id])
   (get-in mir [:pass-execution-record :verifier-report-hash])
   (get-in mir [:pass-execution-record :status])
   (get-in mir [:pass-execution-record :record-id])
   (get-in mir [:b1-preflight :status])
   (get-in mir [:b1-preflight :backend-credit?])])

(def ^:private expected-pending
  [:pending :pending-source-binding :pending-independent-verifier
   :pending-independent-verifier :pending-independent-verifier
   :constructed-unverified :pending-independent-verifier :pending-c11-verifier false])

(def ^:private protected-fields
  [:source-core :pass-contract :type-table :effect-table :ownership-table
   :capability-table :safety-table :runtime-check-table :proof-certificate-table
   :source-map :control-flow-graph :data-flow-graph :effect-order-graph
   :semantic-replay-parity :live-invocation-claim? :self-hosted?])

(deftest sh12-pending-view-preserves-checked-pass-identities-development-kernel
  ;; Supplied DATA exercises the real existing Clojure projection directly.
  (doseq [pass-id [:c11-build-checked-i64-add :c11-build-checked-i64-sub]]
    (let [input (supplied-mir pass-id) result (bootstrap/p15-s23-c11-mir-pending-view input)
          path [:functions 'main :blocks :entry]]
      (is (= pass-id (get-in result [:pass-execution-record :pass-id])))
      (is (identical? (:pass-contract input) (:pass-contract result)))
      (is (= expected-pending (pending-markers result)))
      (is (= (select-keys input protected-fields) (select-keys result protected-fields)))
      (is (= (mapv #(dissoc % :verifier-status) (get-in input (conj path :instructions)))
             (mapv #(dissoc % :verifier-status) (get-in result (conj path :instructions)))))
      (is (= [:pending :pending] (mapv :verifier-status (get-in result (conj path :instructions)))))
      (is (= :pending (get-in result (conj path :terminator :verifier-status))))
      (is (= result (bootstrap/p15-s23-c11-mir-pending-view result))))))

(deftest sh12-pending-view-retains-generic-family-legacy-parity-development-kernel
  ;; The exact old generic result, including its fixed pending pass identity.
  (doseq [contract [{:pass-id :c11-build-mir-bounded-slice :scope {:fixture :supplied-data}} nil {}]]
    (let [input (assoc (supplied-mir :c11-build-mir-bounded-slice) :pass-contract contract)
          expected (-> input
                       (assoc-in [:functions 'main :blocks :entry :instructions 0 :verifier-status] :pending)
                       (assoc-in [:functions 'main :blocks :entry :instructions 1 :verifier-status] :pending)
                       (assoc-in [:functions 'main :blocks :entry :terminator :verifier-status] :pending)
                       (assoc :verification-status :pending
                         :pass-execution-record
                         {:artifact :gravity/build-mir-pass-execution-record
                          :pass-id :c11-build-mir-bounded-slice :pass-contract-hash :pending-source-binding
                          :input-artifact-id :supplied-core :output-content-id :pending-independent-verifier
                          :verifier-report-id :pending-independent-verifier :verifier-report-hash :pending-independent-verifier
                          :diagnostics [] :status :constructed-unverified :record-id :pending-independent-verifier}
                         :b1-preflight {:input-kind :gravity/mir :requires-verifier-status :passed
                                        :status :pending-c11-verifier :backend-credit? false}))
          result (bootstrap/p15-s23-c11-mir-pending-view input)]
      (is (= expected result))
      (is (= result (bootstrap/p15-s23-c11-mir-pending-view result))))))

(deftest sh12-pending-view-does-not-promote-supplied-acceptance-development-kernel
  ;; Malformed/foreign records stay untrusted DATA; this helper is no validator.
  (let [base (supplied-mir :c11-build-checked-i64-sub)]
    (doseq [input [(assoc base :pass-contract :foreign)
                   (assoc base :pass-contract [:foreign])
                   (assoc base :pass-contract {:pass-id :foreign})
                   (assoc base :pass-contract {:pass-id :c11-build-checked-i64-sub :scope {:family :foreign}})
                   (dissoc base :pass-contract)
                   (assoc base :verification-status :foreign)
                   (assoc base :pass-execution-record {:status :passed :verifier-report-id :forged
                                                      :verifier-report-hash :forged :output-content-id :forged})
                   (assoc base :verification-report {:status :passed :semantic-replay-parity :passed
                                                    :independent-acceptance true})
                   (assoc base :b1-preflight {:status :passed :backend-credit? true})
                   (assoc base :source-rule {:verification-status :passed :self-hosted? true})]]
      (let [result (bootstrap/p15-s23-c11-mir-pending-view input)]
        (is (= expected-pending (pending-markers result)))
        (is (= (:verification-report input) (:verification-report result)))
        (is (= [false false false]
               ((juxt :semantic-replay-parity :live-invocation-claim? :self-hosted?) result)))))))

(def ^:private original-mirs
  (delay
    (let [root @(add/existing 'root)
          generic ((var-get (ns-resolve 'gravity.self-hosting.sh13-branch-join-execution-test 'current-parameter-pair))
                   (str (.resolve root "bootstrap/clojure/fixtures/self-hosting/sh-12/branch-join/accepted/i64_parameter_source.gravity"))
                   nil (str (.resolve root "bootstrap/gravity/src/gravity/bootstrap/reader.gravity")))
          add-core (:core @add/current-pair) sub-core (:core @sub/current-pair)]
      {:add {:core add-core :mir (add/invoke 'sh12-build-branch-join-mir add-core)}
       :sub {:core sub-core :mir (add/invoke 'sh12-build-branch-join-mir sub-core)}
       :generic generic})))

(deftest sh12-pending-view-roundtrips-original-add-sub-and-generic-mir
  ;; Expensive fresh current source composition: not selected by cheap kernels.
  (doseq [[family pass-id] [[:add :c11-build-checked-i64-add] [:sub :c11-build-checked-i64-sub]
                           [:generic :c11-build-mir-bounded-slice]]]
    (let [{:keys [core mir]} (get @original-mirs family)
          pending (bootstrap/p15-s23-c11-mir-pending-view mir)]
      (is (= :passed (:status (add/invoke 'sh12-verify-branch-join-mir core mir))))
      (is (= mir pending))
      (is (= pass-id (get-in pending [:pass-execution-record :pass-id])))
      (is (= (:pass-contract mir) (:pass-contract pending)))
      (is (= (select-keys mir protected-fields) (select-keys pending protected-fields)))
      (is (= expected-pending (pending-markers pending)))
      (is (= pending (bootstrap/p15-s23-c11-mir-pending-view pending)))
      (is (= :passed (:status (add/invoke 'sh12-verify-branch-join-mir core pending)))))))

(deftest sh12-pending-view-original-contract-substitution-remains-rejected
  (doseq [[_ {:keys [core mir]}] @original-mirs
          candidate [(assoc-in mir [:pass-contract :pass-id] :foreign)
                     (assoc-in mir [:pass-contract :scope :family] :foreign)]]
    (let [pending (bootstrap/p15-s23-c11-mir-pending-view candidate)
          report (add/invoke 'sh12-verify-branch-join-mir core pending)]
      (is (= :rejected (:status report)))
      (is (= "C11-MODULE" (get-in report [:diagnostics 0 :diagnostic-id])))
      (is (= expected-pending (pending-markers pending))))))
