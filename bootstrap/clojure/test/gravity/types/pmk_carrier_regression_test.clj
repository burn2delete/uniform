(ns gravity.types.pmk-carrier-regression-test
  "PROPOSED SOURCE ONLY. Not loaded or executed during preparation."
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh08-function-call-type-test]))

;; These are existing real Gravity plan adapters and normalized fixtures.
;; This tests the raw component boundary, not an authenticated Reader/Core join.
(defn- helper [name]
  (or (ns-resolve 'gravity.self-hosting.sh08-function-call-type-test name)
      (throw (ex-info "Missing existing SH08 helper" {:name name}))))

(def ^:private owners
  [["sh08" 'pr359-descriptor-c7]
   ["body" 'pr359-descriptor-body]
   ["p15-s23" 'pr359-descriptor-emit]])
;; C7 is unmetered after accounting removal; Effects and the emitter retain
;; their independent microbudget/work contracts.
(def ^:private unmetered-owners #{"sh08"})
(defn- unmetered-owner? [[prefix _]] (contains? unmetered-owners prefix))

(defn- invoke [[prefix adapter] suffix & args]
  (apply (helper adapter) (symbol (str prefix "-" suffix)) args))

(defn- invoke-pmk [owner suffix args]
  (apply invoke owner suffix
         (if (unmetered-owner? owner) args (conj (vec args) 65536))))

(defn- fixture [two-parameters?]
  ((helper (if two-parameters?
             'private-map-keys-two-argument-input 'private-map-keys-input))))

(defn- census [owner {:keys [core call nodes bindings forms]}]
  (invoke owner "pe-census" {:application :normal :source-call call}
          core nodes bindings forms))

(defn- parameter [owner c {:keys [core node nodes bindings forms]}]
  (invoke-pmk owner "pmk-parameter" [c core node nodes bindings forms]))

(defn- expected-work [c input]
  (+ 64 (* 12 (count (get-in c [:function-record :parameter-binding-ids])))
     (count (get-in input [:forms :helper-parameters :child-form-ids]))))

(defn- payload [input value]
  (assoc-in input [:forms :helper-parameters :value] value))

(defn- declaration-ids [input]
  (get-in input [:forms :helper-parameters :child-form-ids]))

(defn- poison [calls]
  ;; Instantiated only inside a future selected test. No instance was made
  ;; during preparation. Each callback records then throws, including count.
  (let [called (fn [method]
                 (swap! calls conj method)
                 (throw (ex-info "Opaque callback was invoked" {:method method})))]
    (proxy [Object clojure.lang.Counted clojure.lang.Seqable] []
      (count [] (called :count))
      (seq [] (called :seq))
      (equals [_] (called :equals))
      (hashCode [] (called :hashCode))
      (toString [] (called :toString)))))

(deftest pmk-carrier-real-owner-valid-vectors
  (doseq [owner owners two-parameters? [false true]]
    (testing (str (first owner) " annotated Map, optional unannotated second parameter")
      (let [input (fixture two-parameters?) c (census owner input)
            result (parameter owner c input)]
        (is (= :closed-private-parameter-entry (:kind c)))
        (is (= :accepted (:status result)))
        (if (unmetered-owner? owner)
          (is (not (contains? result :work-count)))
          (is (= (expected-work c input) (:work-count result))))
        (is (= (if two-parameters?
                 [{:binding-id :span-binding :type :gravity.type/map}
                  {:binding-id :other-binding :type :gravity.type/unknown}]
                 [{:binding-id :span-binding :type :gravity.type/map}])
               (get-in result [:callee :parameters])))))))

(deftest pmk-carrier-real-owner-fourteen-data-cases
  ;; The first DATA case is covered by both valid Vector variants above.
  ;; The remaining thirteen use actual fixture records and a real owner census.
  (doseq [owner owners]
    (let [input (fixture true) c (census owner input)
          ids (declaration-ids input) marker (get ids 1)
          good (get-in input [:forms :helper-parameters :value])
          calls (atom []) opaque (poison calls)
          cases
          [[:carrier-keyword (payload input :forged) :private-map-keys-source-mismatch]
           [:carrier-list (payload input (apply list good)) :private-map-keys-source-mismatch]
           [:carrier-map (payload input {'span 'value}) :private-map-keys-source-mismatch]
           [:carrier-set (payload input #{'span}) :private-map-keys-source-mismatch]
           [:width (payload input []) :private-map-keys-source-mismatch]
           [:late-name (payload input (assoc good 3 'forged)) :private-map-keys-source-mismatch]
           [:late-marker (payload input (assoc good 1 :wrong)) :private-map-keys-source-mismatch]
           [:late-type (payload input (assoc good 2 'Int)) :private-map-keys-source-mismatch]
           [:opaque-payload (payload input (assoc good 3 opaque)) :private-map-keys-source-mismatch]
           [:opaque-child (assoc-in input [:forms marker :value] opaque)
            :private-map-keys-entry-domain-required]
           [:missing-child (update input :forms dissoc (last ids)) :private-map-keys-source-mismatch]
           [:children-reordered (assoc-in input [:forms :helper-parameters :child-form-ids]
                                         (assoc ids 0 (last ids) 3 (first ids)))
            :private-map-keys-source-mismatch]
           [:metadata (assoc-in input [:forms :helper-parameters :parent-form-id] :forged)
            :private-map-keys-source-mismatch]]]
      (doseq [[id changed reason] cases]
        (testing (str (first owner) " " (name id))
          (reset! calls [])
          (let [result (parameter owner c changed)
                callbacks @calls]
            ;; Select scalar diagnostics only; never print an opaque result.
            (is (= (if (unmetered-owner? owner)
                     [:rejected reason]
                     [:rejected reason (expected-work c changed)])
                   ((if (unmetered-owner? owner)
                      (juxt :status :reason)
                      (juxt :status :reason :work-count)) result)))
            (when (unmetered-owner? owner)
              (is (not (contains? result :work-count))))
            (is (= [] callbacks))))))))

(deftest pmk-carrier-real-owner-native-guard-and-refusal-priority
  (doseq [owner owners]
    (let [input (fixture true) c (census owner input)
          calls (atom []) opaque (poison calls)
          good (get-in input [:forms :helper-parameters :value])
          marker (get (declaration-ids input) 1)]
      (testing "Non-Vector must be refused before Counted or Seqable callbacks"
        (let [changed (payload input opaque)
              result (parameter owner c changed) callbacks @calls]
          (is (= (if (unmetered-owner? owner)
                   [:rejected :private-map-keys-source-mismatch]
                   [:rejected :private-map-keys-source-mismatch (expected-work c changed)])
                 ((if (unmetered-owner? owner)
                    (juxt :status :reason)
                    (juxt :status :reason :work-count)) result)))
          (when (unmetered-owner? owner)
            (is (not (contains? result :work-count))))
          (is (= [] callbacks))))
      (testing "Existing declaration refusal precedes final correspondence"
        (let [changed (-> input
                          (assoc-in [:forms marker :value] :wrong)
                          (payload (assoc good 3 'forged)))
              result (parameter owner c changed)]
          (is (= (if (unmetered-owner? owner)
                   [:rejected :private-map-keys-entry-domain-required]
                   [:rejected :private-map-keys-entry-domain-required (expected-work c changed)])
                 ((if (unmetered-owner? owner)
                    (juxt :status :reason)
                    (juxt :status :reason :work-count)) result)))
          (when (unmetered-owner? owner)
            (is (not (contains? result :work-count)))))))))

(deftest pmk-carrier-real-owner-proof-consumer-rechecks-fresh-evidence
  (doseq [owner owners]
    (let [{:keys [core node call edge nodes bindings types forms] :as input} (fixture true)
          source (invoke-pmk owner "pmk-source" [core node call edge nodes bindings types (:module core)])
          descriptor (:descriptor source)
          good (invoke-pmk owner "pmk-proof" [descriptor core node nodes bindings types forms])
          changed (payload input (assoc (get-in input [:forms :helper-parameters :value]) 3 'forged))
          changed-forms (:forms changed)
          physical (invoke-pmk owner "pmk-physical" [core nodes bindings changed-forms])
          c (census owner changed)
          result (invoke-pmk owner "pmk-proof" [descriptor core node nodes bindings types changed-forms])]
      (if (unmetered-owner? owner)
        (do
          (is (= :accepted (:status source) (:status good) (:status physical)))
          (doseq [value [source good physical]]
            (is (not (contains? value :work-count))))
          (is (= [:rejected :private-map-keys-source-mismatch]
                 ((juxt :status :reason) result)))
          (is (not (contains? result :work-count))))
        (let [first-work (:work-count physical)
              receiver (get nodes (first (get-in descriptor [:source-call :argument-node-ids])))
              reserved (invoke owner "pe-reserve" core receiver nodes bindings (- 65536 first-work))]
          (is (= :accepted (:status source) (:status good) (:status physical) (:status reserved)))
          (is (= [:rejected :private-map-keys-source-mismatch
                  (+ first-work (:work-count reserved) (expected-work c changed))]
                 ((juxt :status :reason :work-count) result))))))))
