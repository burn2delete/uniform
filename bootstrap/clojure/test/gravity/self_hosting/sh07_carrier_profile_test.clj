(ns gravity.self-hosting.sh07-carrier-profile-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh07-carrier-profile :as profile]))

(defn- exception-data
  [f]
  (try
    (f)
    nil
    (catch clojure.lang.ExceptionInfo error
      (ex-data error))))

(defn- repeated-payload
  []
  {:kind :authenticated-upstream
   :forms
   (mapv
    (fn [ordinal]
      {:ordinal ordinal
       :name (str "form-" ordinal)
       :facts [:resolved :typed :bounded]})
    (range 64))})

(deftest carrier-profile-is-deterministic-across-map-construction-order
  (let [left
        (array-map
         :request {:schema 1 :payload [1 2 3]}
         :result {:status :accepted})
        right
        (array-map
         :result {:status :accepted}
         :request {:payload [1 2 3] :schema 1})
        left-profile (profile/profile-value left
                                            {:minimum-indexed-subtree-nodes 2})
        right-profile (profile/profile-value right
                                             {:minimum-indexed-subtree-nodes 2})]
    (is (= :gravity/sh07-carrier-profile-v1 (:schema left-profile)))
    (is (= :profiled (:status left-profile)))
    (is (= (:root-fingerprint left-profile)
           (:root-fingerprint right-profile)))
    (is (= (:measurements left-profile)
           (:measurements right-profile)))
    (is (= (mapv #(dissoc % :paths)
                 (:repeated-subtrees left-profile))
           (mapv #(dissoc % :paths)
                 (:repeated-subtrees right-profile))))
    (is (true? (:read-only? left-profile)))
    (is (false?
         (get-in
          left-profile
          [:hypothetical-reference-estimate
           :semantic-rewrite-authorized?])))
    (is (not=
         (:root-fingerprint
          (profile/profile-value {:a 1 :b 2}))
         (:root-fingerprint
          (profile/profile-value {:a 2 :b 1}))))))

(deftest carrier-profile-identifies-large-repeated-subtrees
  (let [request (repeated-payload)
        replay (repeated-payload)
        carrier
        {:request request
         :replay replay
         :result {:status :accepted :request request}}
        result
        (profile/profile-value
         carrier
         {:minimum-indexed-subtree-nodes 16
          :largest-subtree-count 8})
        repeated (:repeated-subtrees result)
        payload-record
        (first
         (filter
          #(= 3 (:occurrences %))
          repeated))]
    (is payload-record repeated)
    (is (< 100 (:nodes-per-copy payload-record)))
    (is (pos? (:hypothetical-node-savings payload-record)))
    (is (<= 3 (count (:top-level-sections result))))
    (is (= #{:request :replay :result}
           (set (map :section (:top-level-sections result)))))
    (is (pos?
         (get-in
          result
          [:hypothetical-reference-estimate
           :overlapping-node-savings-upper-bound])))
    (is (false?
         (get-in
          result
          [:hypothetical-reference-estimate
           :non-overlapping-plan-computed?])))
    (is (false?
         (get-in result
                 [:claims :authenticated-reference-integration?])))
    (is (false? (get-in result [:claims :artifact-authority?])))))

(deftest carrier-profile-measures-depth-width-and-node-kinds
  (let [carrier {:wide [1 2 3 4]
                 :deep [[[[:leaf]]]]}
        result (profile/profile-value carrier
                                      {:minimum-indexed-subtree-nodes 1})
        measurements (:measurements result)]
    (is (= 13 (:nodes measurements)))
    (is (= 6 (:aggregate-nodes measurements)))
    (is (= 5 (:maximum-depth measurements)))
    (is (= 4 (:maximum-width measurements)))
    (is (= 5 (get-in result [:node-kind-counts :vector])))
    (is (= 1 (get-in result [:node-kind-counts :map])))))

(deftest carrier-profile-bounds-enumeration-before-realization
  (let [realized (atom 0)
        infinite
        (map
         (fn [value]
           (swap! realized inc)
           value)
         (range))
        data
        (exception-data
         #(profile/profile-value
           infinite
           {:maximum-container-width 2}))]
    (is (= "SH07-CARRIER-PROFILE-WIDTH" (:id data)))
    (is (= 3 (:observed data)))
    (is (= 2 (:maximum data)))
    (is (<= @realized 3)))
  (let [result
        (profile/profile-value
         {:left 1 :right 2}
         {:maximum-container-width 2})]
    (is (= 2 (get-in result [:measurements :maximum-width])))
    (is (= "SH07-CARRIER-PROFILE-WIDTH"
           (:id
            (exception-data
             #(profile/profile-value
               {:left 1 :right 2}
               {:maximum-container-width 1})))))))

(deftest carrier-profile-rejects-noncanonical-map-keys
  (let [pattern (java.util.regex.Pattern/compile "same")
        data
        (exception-data
         #(profile/profile-value {pattern :value}))]
    (is (= "SH07-CARRIER-PROFILE-KEY" (:id data)))
    (is (= :unsupported-scalar-key (:reason data)))
    (is (= "java.util.regex.Pattern" (:key-class data)))))

(deftest aggregate-savings-are-explicitly-an-overlapping-upper-bound
  (let [payload [[1 2 3] [1 2 3]]
        result
        (profile/profile-value
         [payload payload]
         {:minimum-indexed-subtree-nodes 2})
        estimate (:hypothetical-reference-estimate result)]
    (is (= 13 (:overlapping-node-savings-upper-bound estimate)))
    (is (false? (:non-overlapping-plan-computed? estimate)))
    (is (not (contains? estimate :potential-node-savings)))))

(deftest carrier-profile-fails-closed-on-cycles-depth-and-width
  (let [cycle (java.util.ArrayList.)]
    (.add cycle cycle)
    (is (= "SH07-CARRIER-PROFILE-CYCLE"
           (:id
            (exception-data
             #(profile/profile-value cycle))))))
  (is (= "SH07-CARRIER-PROFILE-DEPTH"
         (:id
          (exception-data
           #(profile/profile-value
             [[[[[[:too-deep]]]]]]
             {:maximum-depth 3})))))
  (is (= "SH07-CARRIER-PROFILE-WIDTH"
         (:id
          (exception-data
           #(profile/profile-value
             [1 2 3]
             {:maximum-container-width 2})))))
  (is (= "SH07-CARRIER-PROFILE-NODES"
         (:id
          (exception-data
           #(profile/profile-value
             [1 2 3]
             {:maximum-nodes 3}))))))

(deftest duplicate-index-bound-is-explicit
  (let [carrier
        {:a (vec (range 20))
         :b (vec (range 20 40))
         :c (vec (range 40 60))}
        result
        (profile/profile-value
         carrier
         {:minimum-indexed-subtree-nodes 2
          :maximum-distinct-subtree-fingerprints 1})]
    (is (= 1 (get-in result [:duplicate-index :indexed-fingerprints])))
    (is (= 1
           (get-in
            result
            [:duplicate-index :maximum-distinct-fingerprints])))
    (is (true? (get-in result [:duplicate-index :truncated?])))
    (is (false? (get-in result [:duplicate-index :complete?])))
    (is (< 0
           (get-in
            result
            [:duplicate-index :omitted-subtree-occurrences])))
    (is (=
         (get-in result
                 [:duplicate-index :eligible-subtree-occurrences])
         (+
          (get-in result
                  [:duplicate-index :indexed-subtree-occurrences])
          (get-in result
                  [:duplicate-index :omitted-subtree-occurrences]))))))

(deftest profiler-options-fail-closed
  (doseq [[option value]
          [[:maximum-depth -1]
           [:maximum-container-width 1.5]
           [:minimum-indexed-subtree-nodes nil]
           [:unknown-option 1]]]
    (testing (str option)
      (let [data
            (exception-data
             #(profile/profile-value {} {option value}))]
        (is (= "SH07-CARRIER-PROFILE-OPTIONS" (:id data)))
        (is (= option (:option data)))
        (is (= value (:value data)))))))
