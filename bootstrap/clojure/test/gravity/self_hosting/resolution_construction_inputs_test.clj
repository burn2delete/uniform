(ns gravity.self-hosting.resolution-construction-inputs-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as b]))

(defn- implementation [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing resolution implementation" {:name name}))))
(defn- thrown [f] (try (f) nil (catch Throwable error error)))
(def ^:private invocation
  {:reader-source-path "bootstrap/gravity/src/gravity/bootstrap/reader.gravity"
   :authenticated-envelope-options {:source :development-current}})
(def ^:private parent
  {:gravity-macro-boundary
   {:authenticated-envelope {:source-rule {:source-byte-count 3 :source-content-hash "hash"}}}})
(defn- prepare [] ((implementation 'resolution-construction-inputs) "model.gravity" parent false))

(defn- with-model [f]
  ;; These explicit orchestration models do not fabricate compiler acceptance.
  ;; Genuine owner execution and exact product equivalence are checked separately.
  (let [events (atom [])
        state (atom {:dependencies {:reader :reader :syntax :syntax :macro :macro
                                    :resolver :resolver :resolver-inputs :toolchain
                                    :envelope-source {:byte-count 3 :bytes-hash "hash"}}
                     :environment {:thread 1 :context nil :invocation invocation
                                   :operation-binding {:plan :actual-plan}}
                     :after-parent identity :after-projection identity})
        overrides
        {'resolution-construction-environment (fn [] (:environment @state))
         'resolution-construction-dependencies
         (fn [& _]
           (if-let [error (:observation-error @state)] (throw error) (:dependencies @state)))
         'sh03-reader-authenticated-envelope-options (fn [& _] {:source :development-current})
         'sh06-authenticated-sh05-parent!
         (fn [_ value]
           (swap! events conj :parent)
           (when-let [error (:parent-error @state)] (throw error))
           ((:after-parent @state) state)
           value)
         'sh06-authenticated-source-snapshot!
         (fn [& _]
           (swap! events conj :target)
           (when-let [error (:target-error @state)] (throw error))
           {:source :checked})
         'resolution-base-request-from-authenticated-parent
         (fn [_ value overrides]
           (swap! events conj :projection)
           (is (identical? parent value))
           (is (= {} overrides))
           (when-let [error (:projection-error @state)] (throw error))
           ((:after-projection @state) state)
           {:request :derived})
         'sh06-resolution-base-request-data
         (fn [& _] (swap! events conj :fresh-request) {:request :derived})
         ;; Restore the constant even in the intentional root-drift test.
         'sh05-macro-version b/sh05-macro-version}]
    (with-redefs-fn (into {} (map (fn [[name value]] [(implementation name) value])) overrides)
      #(f state events))))

(deftest one-shot-projection-follows-real-parent-boundary
  (with-model
    (fn [_ events]
      (is (= {:source-before {:source :checked} :base {:request :derived}} (prepare)))
      (is (= [:parent :target :projection] @events))
      (is (= #{:source-before :base} (set (keys (prepare)))))
      (is (= 2 (count (filter #{:parent} @events)))))))

(deftest unavailable-optional-observation-keeps-original-fresh-path
  (with-model
    (fn [state events]
      (swap! state assoc :observation-error (ex-info "stricter source observation" {}))
      (is (= {:source-before {:source :checked} :base {:request :derived}} (prepare)))
      (is (= [:parent :target :fresh-request] @events)))))

(deftest invocation-source-observation-does-not-preempt-the-parent-gate
  (let [dependencies (implementation 'resolution-construction-dependencies)
        actual-dependencies @dependencies]
    (with-model
      (fn [state events]
        (with-redefs-fn
          {dependencies actual-dependencies
           (implementation 'sh03-reader-authenticated-envelope-options)
           (fn [& _] (throw (ex-info "source became unavailable" {:id "C2-HASH"})))}
          (fn []
            (is (= {:source-before {:source :checked} :base {:request :derived}} (prepare)))
            (is (= [:parent :target :fresh-request] @events))
            (let [error (ex-info "original parent diagnostic" {:id "C5-UNRESOLVED"})]
              (reset! events [])
              (swap! state assoc :parent-error error)
              (is (identical? error (thrown prepare)))
              (is (= [:parent] @events)))))))))

(deftest established-source-binding-and-environment-drift-reject
  (doseq [boundary [:after-parent :after-projection]
          field [:reader :syntax :macro :resolver :resolver-inputs :envelope-source]]
    (with-model
      (fn [state _]
        (swap! state assoc boundary #(swap! % assoc-in [:dependencies field] :changed))
        (let [error (ex-data (thrown prepare))]
          (is (= "C5-UNRESOLVED" (:id error)) [boundary field])
          (is (= [:construction-parent-dependency-drift] (:missing-fields error)) [boundary field])))))
  (doseq [field [:thread :context :invocation :operation-binding]]
    (with-model
      (fn [state events]
        (swap! state assoc :after-parent #(swap! % assoc-in [:environment field] :changed))
        (is (= "C5-UNRESOLVED" (:id (ex-data (thrown prepare)))) field)
        (is (= [:parent :target] @events) field)))))

(deftest authenticated-envelope-source-and-root-drift-reject
  (with-model
    (fn [state events]
      (swap! state assoc-in [:dependencies :envelope-source :bytes-hash] "foreign")
      (is (= "C5-UNRESOLVED" (:id (ex-data (thrown prepare)))))
      (is (= [:parent :target] @events))))
  (with-model
    (fn [state _]
      (swap! state assoc :after-projection
             (fn [_] (alter-var-root (implementation 'sh05-macro-version) (constantly ::changed))))
      (is (= [:construction-parent-dependency-drift]
             (:missing-fields (ex-data (thrown prepare))))))))

(deftest original-failure-order-and-interruption-are-preserved
  (doseq [[field expected] [[:parent-error [:parent]]
                           [:target-error [:parent :target]]
                           [:projection-error [:parent :target :projection]]]]
    (with-model
      (fn [state events]
        (let [error (ex-info "original owner failure" {:id (name field)})]
          (swap! state assoc field error)
          (when (= field :parent-error)
            (swap! state assoc :observation-error (ex-info "unavailable observation" {})))
          (is (identical? error (thrown prepare)))
          (is (= expected @events))))))
  (with-model
    (fn [state events]
      (let [error (InterruptedException. "interrupted source observation")]
        (swap! state assoc :observation-error error)
        (try
          (let [caught (thrown prepare)
                interrupted? (.isInterrupted (Thread/currentThread))]
            ;; Observe before the reporting/locking machinery can clear it.
            (Thread/interrupted)
            (is (identical? error caught))
            (is interrupted?)
            (is (empty? @events)))
          (finally (Thread/interrupted)))))))

(deftest native-carrier-guard-precedes-new-observation-or-field-access
  (let [calls (atom 0) observed? (atom false)
        opaque (proxy [Object] []
                 (hashCode [] (swap! calls inc) 7)
                 (equals [_] (swap! calls inc) false)
                 (toString [] (swap! calls inc) "opaque"))]
    (with-redefs-fn {(implementation 'resolution-construction-environment)
                    (fn [] (reset! observed? true))}
      (fn []
        (doseq [value [opaque (java.util.HashMap.) (with-meta {} {:callback opaque})
                       (lazy-seq (swap! calls inc) (list :lazy))]]
          (is (= [:authenticated-sh05-macro-parent]
                 (:missing-fields
                  (ex-data (thrown #((implementation 'resolution-construction-inputs)
                                     "carrier.gravity" value false)))))))))
    (is (zero? @calls))
    (is (false? @observed?))))

(deftest historical-and-outer-transaction-paths-do-not-optimize
  (let [eligible (implementation 'resolution-construction-eligible?)
        operation (implementation '*sh06-resolution-operation-binding*)
        context (implementation '*sh07-proof-transaction-context*)
        open (atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                    :phase :construction :epoch 0 :receipts [] :check-catalogs {}})]
    (with-bindings {operation {} context nil} (is (false? (eligible))))
    (with-bindings {operation {:reader-invocation invocation} context nil} (is (true? (eligible))))
    (with-bindings {operation {:reader-invocation invocation} context open}
      (is (false? (eligible)))
      (is (empty? (:receipts @open)))
      (swap! open assoc :open? false)
      (is (= :closed-transaction (:reason (ex-data (thrown eligible))))))))
