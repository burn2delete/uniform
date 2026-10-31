(ns gravity.checked-core.pipeline-test
  (:require [clojure.test :refer [deftest is]]
            [gravity.bootstrap :as b]
            [gravity.checked-core :as core]))

(defn- seed-var [name]
  (or (ns-resolve 'gravity.bootstrap name)
      (throw (ex-info "Missing checked-core seed seam" {:name name}))))
(defn- operations [] ((seed-var 'checked-core-stage-operations)))
(defn- input [field] ((:input (operations)) field))
(defn- caught [f] (try (f) nil (catch clojure.lang.ExceptionInfo error error)))
(def ^:private source "<checked-core-runtime-unit>")

(deftest namespace-contract-and-operation-inventories-agree
  (let [publics (ns-publics 'gravity.checked-core)
        contract (:gravity/namespace-contract (meta #'core/execute!))]
    (is (= 6 (count publics)))
    (is (= (set (keys publics)) (set (keys (:public-api contract)))))
    (is (= (set (keys (operations))) (set (:operation-inputs contract))))
    (doseq [field (:seed-inputs contract)]
      (is (identical? @(seed-var (symbol (name field))) (input field))))
    (doseq [[name api] (:public-api contract)]
      (is (= (:arglists api) (:arglists (meta (publics name)))))
      (is (= :none (:admission api)))
      (is (= contract (:gravity/namespace-contract (meta (publics name))))))
    (is (true? (.isDynamic ^clojure.lang.Var (seed-var '*sh07-core-execution-binding*))))
    (is (not (contains? (ns-aliases 'gravity.checked-core) 'b)))
    (is (= '[gravity.bootstrap] (get-in contract [:dependency-direction :forbids])))))

(deftest actual-current-source-plan-and-execution-retain-development-status
  (let [ops (operations) binding (b/sh07-core-current-binding!)
        identities (core/plan-identities (:plan binding) ops)
        empty-state (core/execute! source 'sh07-empty-state [] ops)]
    (is (= :development-unadmitted (:revision-status binding)))
    (is (false? (:self-hosted? binding)))
    (is (= identities (select-keys binding (keys identities))))
    (is (= (:source-content-hash binding) (get-in binding [:plan :source :sha256])))
    (is (= [] (:nodes empty-state) (:var-references empty-state)))
    (is (= (core/source-key ops) ((seed-var 'sh07-core-current-source-key))))
    (is (= (b/sh07-core-source-path) (core/source-path ops)))))

(deftest module-and-source-contract-drift-reject-before-binding-publication
  ;; Real current source bytes with isolated compiler result ports. No admission.
  (let [binding (b/sh07-core-current-binding!)
        ops (assoc (operations)
                   :c-backend-stage2-plan-emitter-source-rule! (fn [& _] {:emitter :unit})
                   :sh07-core-plan-identities (fn [_] (select-keys binding [:public-function-shapes])))]
    (doseq [plan [(assoc-in (:plan binding) [:module :profile] :hosted)
                  (assoc-in (:plan binding) [:module :effects] #{:io/read})
                  (assoc-in (:plan binding) [:source :sha256] "sha256:changed")]]
      (let [error (caught #(core/build-plan-binding!
                           (assoc ops :p15-s23-stage2-compiler-artifact-plan (fn [& _] plan))))]
        (is (= "C6-VERIFY" (:id (ex-data error))))
        (is (= [:sh07-core-module-contract] (:missing-fields (ex-data error))))))))

(deftest native-carriers-reject-before-runtime-callbacks
  (let [calls (atom 0)
        opaque (proxy [Object] []
                 (hashCode [] (swap! calls inc) 7)
                 (equals [_] (swap! calls inc) false)
                 (toString [] (swap! calls inc) "opaque"))
        ops (assoc (operations) :p15-s23-stage2-runtime-execute-function (fn [& _] (swap! calls inc)))]
    (doseq [arguments [[opaque] {:opaque opaque}]]
      (let [error (caught #(core/execute! source 'unit arguments ops))]
        (is (= "C6-VERIFY" (:id (ex-data error))))))
    (is (zero? @calls))))

(deftest host-errors-and-interruption-retain-the-existing-boundary
  (let [ops (operations)
        fixture-ops (assoc ops :input (fn [field]
                                       (if (= field :*sh07-core-execution-binding*)
                                         {:plan :unit} ((:input ops) field))))]
    (doseq [error [(StackOverflowError. "unit") (OutOfMemoryError. "unit")
                   (AssertionError. "unit") (LinkageError. "unit") (Exception. "unit")]]
      (let [failure (caught #(core/execute! source 'unit []
                                            (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                                   (fn [& _] (throw error)))))]
        (is (= "C6-VERIFY" (:id (ex-data failure))))
        (is (= (.getName (class error)) (:contained-host-error (ex-data failure))))))
    (let [error (ex-info "unit" {:id "UNIT"})]
      (is (identical? error (caught #(core/execute! source 'unit []
                                                   (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                                          (fn [& _] (throw error))))))))
    (let [error (InterruptedException. "unit")
          observed (try
                     (core/execute! source 'unit []
                                    (assoc fixture-ops :p15-s23-stage2-runtime-execute-function
                                           (fn [& _] (throw error))))
                     (catch InterruptedException observed [observed (.isInterrupted (Thread/currentThread))])
                     (finally (Thread/interrupted)))]
      (is (identical? error (first observed)))
      (is (true? (second observed))))))

(def ^:private unit-artifact {:artifact-id "unit-core"})
(def ^:private unit-report {:status :passed :checks {:unit? true} :failed-checks []})
(defn- with-unit-verifier [f]
  ;; Real receipt qualification with an inert verifier; no Core admission claim.
  (with-redefs-fn {(seed-var 'sh07-core-artifact-verification*) (fn [& _] unit-report)} f))
(defn- with-context [f]
  (let [ctx (atom {:open? true :owner-thread-id (.getId (Thread/currentThread)) :phase :construction :epoch 0
                   :receipts [] :executions {} :reuses {} :check-catalogs {} :failed-report-executions 0})]
    (try (with-bindings {(seed-var '*sh07-proof-transaction-context*) ctx} (f ctx))
         (finally (swap! ctx assoc :open? false :receipts [])))))
(defn- verify-unit [] (b/sh07-core-artifact-verification unit-artifact))
(defn- assert-interposition-misses [interpose]
  (with-unit-verifier
    #(with-context
       (fn [ctx]
         (is (= unit-report (verify-unit)))
         (is (= unit-report (verify-unit)))
         (is (= 1 (get-in @ctx [:executions :sh07])))
         (is (= 1 (get-in @ctx [:reuses :sh07])))
         (interpose (fn [] (dotimes [_ 2] (is (= unit-report (verify-unit))))))
         (is (= 3 (get-in @ctx [:executions :sh07])))
         (is (= 1 (get-in @ctx [:reuses :sh07])))
         (is (= unit-report (verify-unit)))
         (is (= 2 (get-in @ctx [:reuses :sh07])))))))

(deftest every-stage-operation-and-compatibility-root-invalidates-receipts
  (let [vars (set (concat (vals (ns-publics 'gravity.checked-core)) (filter var? (vals (operations)))
                         (map seed-var '[checked-core-stage-operations sh07-core-source-path sh07-core-plan-identities
                                         sh07-core-build-binding! sh07-core-current-source-key sh07-core-current-binding!
                                         sh07-core-execute!])))]
    (doseq [v vars]
      (let [original @v]
        (assert-interposition-misses #(with-redefs-fn {v (fn [& args] (apply original args))} %))))))

(deftest configuration-cache-and-thread-binding-roots-invalidate-receipts
  (doseq [name '[checked-core-stage-defaults sh07-core-public-function-shapes sh07-core-source-relative-path
                sh07-core-development-binding sh07-core-cached-binding]]
    (let [v (seed-var name) original @v
          replacement (cond (map? original) (reduce-kv assoc {} original)
                            (string? original) (String. ^String original)
                            (instance? clojure.lang.Atom original) (atom @original)
                            :else (reify clojure.lang.IDeref (deref [_] @original)))]
      (is (not (identical? original replacement)))
      (assert-interposition-misses #(with-redefs-fn {v replacement} %))))
  (let [v #'core/execute! original @v dynamic? (.isDynamic v)]
    (try (.setDynamic v true)
         (assert-interposition-misses #(with-bindings {v original} (%)))
         (finally (.setDynamic v dynamic?)))))

(deftest scoped-execution-binding-is-an-explicit-value-epoch
  (let [v (seed-var '*sh07-core-execution-binding*)
        original-root (.getRawRoot ^clojure.lang.Var v)
        binding {:plan {:body :original} :source-content-hash "same" :plan-semantic-hash "same"}]
    (is (not (identical? binding (reduce-kv assoc {} binding))))
    (with-unit-verifier
      #(with-context
         (fn [ctx]
           (with-bindings {v binding}
             (is (= unit-report (verify-unit)))
             (is (= unit-report (verify-unit))))
           (with-bindings {v (reduce-kv assoc {} binding)} (is (= unit-report (verify-unit))))
           (is (= 1 (get-in @ctx [:executions :sh07])))
           (is (= 2 (get-in @ctx [:reuses :sh07])))
           (with-bindings {v (assoc-in binding [:plan :body] :changed)}
             (is (= unit-report (verify-unit))))
           (is (= 2 (get-in @ctx [:executions :sh07])))
           (with-bindings {v binding} (is (= unit-report (verify-unit))))
           (is (= 3 (get-in @ctx [:reuses :sh07]))))))
    (is (identical? original-root (.getRawRoot ^clojure.lang.Var v)))))

(deftest construction-roots-detect-the-new-runtime-inputs-before-owner-work
  (let [expected (set (concat (vals (ns-publics 'gravity.checked-core))
                              (filter var? (vals (operations)))
                              (map seed-var '[checked-core-stage-operations checked-core-stage-defaults checked-core-stage-input
                                              sh07-core-source-path sh07-core-plan-identities sh07-core-build-binding!
                                              sh07-core-current-source-key sh07-core-current-binding! sh07-core-execute!
                                              sh07-core-source-relative-path sh07-core-public-function-shapes
                                              sh07-core-development-binding sh07-core-cached-binding *sh07-core-execution-binding*])))
        downstream (atom 0)]
    ;; Isolated bookend only; the owning admission operations are not executed.
    (with-redefs-fn
      {(seed-var 'sh07-core-construction-phase) (constantly nil)
       (seed-var 'sh07-core-construction-catalog) (fn [& _] (swap! downstream inc) nil)}
      (fn []
        (let [roots ((seed-var 'sh07-core-construction-roots))
              facts {:source-path source :thread (.getId (Thread/currentThread)) :phase nil :roots roots}]
          (is (every? (set (map first roots)) expected))
          (doseq [v expected]
            (let [original @v
                  replacement (cond (map? original) (reduce-kv assoc {} original)
                                    (string? original) (String. ^String original)
                                    (instance? clojure.lang.Atom original) (atom @original)
                                    (instance? clojure.lang.IDeref original) (reify clojure.lang.IDeref (deref [_] @original))
                                    (nil? original) :unit-binding
                                    :else (fn [& args] (apply original args)))
                  error (caught #(with-redefs-fn {v replacement}
                                    (fn [] ((seed-var 'sh07-core-construction-check!) facts))))]
              (is (= :construction-dependency-drift (:reason (ex-data error))) (str (:name (meta v))))))
          (is (zero? @downstream)))))))
