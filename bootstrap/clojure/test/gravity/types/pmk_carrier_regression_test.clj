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

(defn- invoke [[prefix adapter] suffix & args]
  (apply (helper adapter) (symbol (str prefix "-" suffix)) args))

(defn- fixture [two-parameters?]
  ((helper (if two-parameters?
             'private-map-keys-two-argument-input 'private-map-keys-input))))

(defn- census [owner {:keys [core call nodes bindings forms]}]
  (invoke owner "pe-census" {:application :normal :source-call call}
          core nodes bindings forms))

(defn- parameter [owner c {:keys [core node nodes bindings forms]}]
  (invoke owner "pmk-parameter" c core node nodes bindings forms 65536))

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
        (is (= (expected-work c input) (:work-count result)))
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
            (is (= [:rejected reason (expected-work c changed)]
                   ((juxt :status :reason :work-count) result)))
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
          (is (= [:rejected :private-map-keys-source-mismatch (expected-work c changed)]
                 ((juxt :status :reason :work-count) result)))
          (is (= [] callbacks))))
      (testing "Existing declaration refusal precedes final correspondence"
        (let [changed (-> input
                          (assoc-in [:forms marker :value] :wrong)
                          (payload (assoc good 3 'forged)))
              result (parameter owner c changed)]
          (is (= [:rejected :private-map-keys-entry-domain-required (expected-work c changed)]
                 ((juxt :status :reason :work-count) result))))))))

(deftest pmk-carrier-real-owner-proof-consumer-retains-work-report
  (doseq [owner owners]
    (let [{:keys [core node call edge nodes bindings types forms] :as input} (fixture true)
          source (invoke owner "pmk-source" core node call edge nodes bindings types (:module core) 65536)
          descriptor (:descriptor source)
          good (invoke owner "pmk-proof" descriptor core node nodes bindings types forms 65536)
          changed (payload input (assoc (get-in input [:forms :helper-parameters :value]) 3 'forged))
          changed-forms (:forms changed)
          physical (invoke owner "pmk-physical" core nodes bindings changed-forms 65536)
          first-work (:work-count physical)
          receiver (get nodes (first (get-in descriptor [:source-call :argument-node-ids])))
          reserved (invoke owner "pe-reserve" core receiver nodes bindings (- 65536 first-work))
          c (census owner changed)
          result (invoke owner "pmk-proof" descriptor core node nodes bindings types changed-forms 65536)]
      (is (= :accepted (:status source) (:status good) (:status physical) (:status reserved)))
      (is (= [:rejected :private-map-keys-source-mismatch
              (+ first-work (:work-count reserved) (expected-work c changed))]
             ((juxt :status :reason :work-count) result))))))
