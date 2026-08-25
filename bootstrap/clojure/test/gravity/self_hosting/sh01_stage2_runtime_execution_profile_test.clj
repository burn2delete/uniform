(ns gravity.self-hosting.sh01-stage2-runtime-execution-profile-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh01-stage2-runtime-execution-profile
             :as profile]))

(def ^:private synthetic-plan
  {:kind :gravity/stage2-hosted-core-compiled-plan
   :plan-id "sha256:synthetic-stage2-runtime-profile"
   :entrypoint 'main
   :source {:path "synthetic.gravity" :sha256 "sha256:synthetic-source"}
   :module {:effects #{} :capabilities #{}}
   :functions {'main {:params [] :instructions [{:op :function-call
                                                 :function 'helper :args []}]}
               'helper {:params [] :instructions [{:op :literal :value :ok}]}}
   :instruction-summary {:function-call 1 :literal 1}
   :effect-summary {}})

(defn- diagnostic [operation]
  (try
    (operation)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(deftest fixed-profile-records-bounded-deterministic-attribution
  (let [receipt
        (profile/run-profile
         {:emit-plan
          (fn []
            ;; Exercise the temporary runtime wrappers during the fresh-emission
            ;; scope without changing the synthetic plan or observable output.
            (bootstrap/p15-s23-stage2-runtime-execute-function
             {:engine :synthetic} synthetic-plan 'helper [])
            synthetic-plan)
          :execute-plan
          (fn [plan]
            (bootstrap/p15-s23-stage2-runtime-execute-plan
             {:engine :synthetic} plan))})]
    (is (= :gravity/sh01-stage2-runtime-execution-profile (:artifact receipt)))
    (is (= :non-authoritative (:authority receipt)))
    (is (false? (:authoritative? receipt)))
    (is (true? (:fresh-plan-emission? receipt)))
    (is (false? (:persistent-cache-authority? receipt)))
    (is (= "sha256:synthetic-stage2-runtime-profile"
           (get-in receipt [:sample :semantic-receipt :plan-id])))
    (is (= #{{:scope :fresh-plan-emission :source :stage2-emitted-plan
              :function "helper" :call-count 1}
             {:scope :emitted-plan-execution :source :stage2-emitted-plan
              :function "helper" :call-count 1}}
           (set (get-in receipt [:sample :function-rows]))))
    (is (contains? (set (get-in receipt [:sample :instruction-rows]))
                   {:scope :emitted-plan-execution :source :stage2-emitted-plan
                    :function "helper" :instruction-op :literal :call-count 1}))
    (is (= {:function {:row-call-count 2 :counter-call-count 2 :overflow? false :complete? true}
            :instruction {:row-call-count 3 :counter-call-count 3 :overflow? false :complete? true}
            :call-edge {:row-call-count 0 :counter-call-count 0 :overflow? false :complete? true}}
           (get-in receipt [:sample :row-sum-coverage])))
    (is (= :gravity/stage2-hosted-core-compiled-plan
           (get-in receipt [:sample :source-identities
                            :stage2-emitted-plan :plan-kind])))
    (is (= [:semantic-receipt :function-rows :instruction-rows :call-edge-rows
            :row-sum-coverage :source-identities :plan-source-registry
            :counter-overflow?
            :off-owner-thread-event-count]
           (:deterministic-accounting receipt)))
    (is (false? (get-in receipt [:sample :counter-overflow?])))
    (is (every? keyword? (:nonclaims receipt)))))

(deftest profile-rejects-unbounded-or-mismatched-requests
  (is (= "SH01-STAGE2-RUNTIME-PROFILE-COUNT"
         (:id (diagnostic #(profile/run-profile {:iterations 2})))) )
  (is (= "SH01-STAGE2-RUNTIME-PROFILE-USAGE"
         (:id (diagnostic #(profile/parse-arguments ["--iterations" "1"])))) )
  (is (= "SH01-STAGE2-RUNTIME-PROFILE-EQUIVALENCE"
         (:id
          (diagnostic
           #(profile/run-profile
             {:emit-plan (constantly synthetic-plan)
              :execute-plan (fn [_]
                              {:stdout "different" :entrypoint-result :ok})}))))))

(deftest profile-rejects-forged-result-and-depth-overflow
  (is (= "SH01-STAGE2-RUNTIME-PROFILE-EQUIVALENCE"
         (:id (diagnostic #(profile/run-profile
                            {:emit-plan (constantly synthetic-plan)
                             :execute-plan (fn [plan]
                                             (assoc (bootstrap/p15-s23-stage2-runtime-execute-plan
                                                     {:engine :synthetic} plan)
                                                    :entrypoint-result :forged))})))))
  (let [depth 129
        names (mapv #(symbol (str "f" %)) (range depth))
        plan {:kind :gravity/stage2-hosted-core-compiled-plan
              :plan-id "sha256:synthetic-depth-profile" :entrypoint 'main
              :source {:path "synthetic-depth.gravity"} :module {:effects #{} :capabilities #{}}
              :functions (into {}
                               (map-indexed
                                (fn [index name]
                                  [name {:params []
                                         :instructions [(if (= index (dec depth))
                                                          {:op :literal :value :ok}
                                                          {:op :function-call :function (nth names (inc index)) :args []})]}])
                                names))}]
    (is (= {:id "SH01-STAGE2-RUNTIME-PROFILE-DEPTH"
            :maximum-function-depth 128 :observed-function-depth 129}
           (diagnostic #(profile/run-profile
                          {:emit-plan (fn []
                                        (bootstrap/p15-s23-stage2-runtime-execute-function
                                         {:engine :synthetic} plan (first names) [])
                                        plan)}))))))

(deftest profile-reports-counter-saturation
  (let [receipt (profile/run-profile
                 {:emit-plan (constantly synthetic-plan)
                  :execute-plan #(bootstrap/p15-s23-stage2-runtime-execute-plan {:engine :synthetic} %)
                  :initialize-state #(java.util.Arrays/fill ^longs (:function-calls %) Long/MAX_VALUE)})]
    (is (true? (get-in receipt [:sample :counter-overflow?])))
    (is (false? (get-in receipt [:sample :row-sum-coverage :function :complete?])))
    (is (true? (get-in receipt [:sample :row-sum-coverage :function :overflow?])))))

(deftest profile-separates-sampled-cost-saturation
  (let [helper-offset 1537
        receipt (profile/run-profile
                 {:emit-plan (constantly synthetic-plan)
                  :execute-plan #(bootstrap/p15-s23-stage2-runtime-execute-plan {:engine :synthetic} %)
                  :initialize-state
                  (fn [state]
                    (aset-long ^longs (:function-calls state) helper-offset 8191)
                    (aset-long ^longs (:sample-count state) helper-offset Long/MAX_VALUE))})]
    (is (true? (get-in receipt [:sample :sample-overflow?])))
    (is (false? (get-in receipt [:sample :counter-overflow?])))
    (is (true? (get-in receipt [:sample :row-sum-coverage :function :complete?])))))
