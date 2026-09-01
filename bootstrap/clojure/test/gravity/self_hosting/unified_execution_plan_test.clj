(ns gravity.self-hosting.unified-execution-plan-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.unified-execution-plan :as plan]))

(defn- error-id [f]
  (try (f) nil (catch clojure.lang.ExceptionInfo e (:id (ex-data e)))))

(defn- passing-result []
  {:status :passed :test-count 1 :pass 1 :fail 0 :error 0
   :diagnostics [] :evidence [:selected-tests :dependency-closure :diagnostics :receipt]
   :mutation-detected? false :fresh-replayable? true
   :provenance-checked? true :source-authenticated? true
   :closure-verified? true :cache-decision :miss})

(deftest catalog-is-closed-and-compatibility-scoped
  (is (= :gravity/verification-catalog-v1 (:schema plan/catalog)))
  (is (= false (:complete? plan/catalog)))
  (is (= :compatibility-phase (get-in plan/catalog [:coverage :completeness])))
  (is (= [:exclusive :memory-heavy :normal]
         (sort (keys (:resource-classes plan/catalog)))))
  (is (every? #(false? (:replaces? %)) (vals (:routes plan/catalog))))
  (is (= (set (keys (:routes plan/catalog)))
         (set (get-in plan/catalog [:coverage :route-ids])))))

(deftest plans-are-deterministic-and-declarative
  (let [request {:route-id "sh07-b8-regression" :lane :fast}
        first-plan (plan/plan request)
        second-plan (plan/plan request)]
    (is (= first-plan second-plan))
    (is (= :fast (:lane first-plan)))
    (is (= :non-authoritative (:authority first-plan)))
    (is (false? (:authoritative? first-plan)))
    (is (= :eligible (get-in first-plan [:cache-policy :mode])))
    (is (= :warm-bounded-process (:execution-mode first-plan)))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:plan-id first-plan)))
    (is (= true (get-in first-plan [:dependency-closure :complete?])))
    (is (= :pre-and-post-execution (get-in first-plan [:guards :source-authentication])))))

(deftest fresh-plan-is-acceptance-capable-but-not-proof
  (let [fresh (plan/plan {:route-id "sh07-authoritative" :lane :fresh})]
    (is (= :acceptance-capable (:authority fresh)))
    (is (false? (:authoritative? fresh)))
    (is (= :forbidden (get-in fresh [:cache-policy :mode])))
    (is (= :fresh-isolated-process (:execution-mode fresh)))
    (is (= true (:fresh-authoritative-gate-required? fresh)))
    (is (= :acceptance-only (:authority-ceiling fresh)))
    (is (= "UEP-REQUEST-CACHE"
           (error-id #(plan/plan {:route-id "sh07-authoritative"
                                  :lane :fresh
                                  :cache-policy {:mode :eligible}}))))))

(deftest unknown-and-tampered-inputs-fail-closed
  (is (= "UEP-ROUTE-UNKNOWN"
         (error-id #(plan/plan {:route-id "unknown" :lane :fast}))))
  (is (= "UEP-REQUEST-KEYS"
         (error-id #(plan/plan {:route-id "sh07-b8-regression" :lane :fast
                                :extra true}))))
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-PLAN-ID"
           (error-id #(plan/validate-plan (assoc p :lane :fresh)))))))

(deftest catalog-rejects-unbounded-executors-and-incomplete-cache-keys
  (let [fast (assoc-in plan/catalog [:routes "sh07-b8-regression" :executor :bounded?] false)
        fresh (assoc-in plan/catalog [:routes "sh07-authoritative" :executor :bounded?] false)
        cache (assoc-in plan/catalog [:routes "sh07-b8-regression" :cache-policy :key-inputs]
                        [:route-id])]
    (is (= "UEP-CATALOG-ROUTE-FAST-POLICY"
           (error-id #(plan/validate-catalog fast))))
    (is (= "UEP-CATALOG-ROUTE-FRESH-POLICY"
           (error-id #(plan/validate-catalog fresh))))
    (is (= "UEP-CATALOG-ROUTE-CACHE"
           (error-id #(plan/validate-catalog cache))))))

(deftest plan-rejects-nonpositive-recomputed-resource-bounds
  (let [identity-of (deref (var gravity.self-hosting.unified-execution-plan/identity-of))
        p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (doseq [k [:timeout-ms :max-output-bytes :max-test-vars]]
      (let [altered (assoc-in p [:resource-policy k] 0)
            altered (assoc altered :plan-id (identity-of (dissoc altered :plan-id)))]
        (is (= "UEP-PLAN-RESOURCE"
               (error-id #(plan/validate-plan altered))) k)))))

(deftest receipt-generation-preserves-integrity-and-authority-ceiling
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        receipt (plan/execute-plan p (fn [_] (passing-result)))]
    (is (= :gravity/unified-execution-receipt-v1 (:schema receipt)))
    (is (= :passed (:status receipt)))
    (is (= false (:authoritative? receipt)))
    (is (= :non-authoritative (:authority receipt)))
    (is (= :miss (get-in receipt [:cache :decision])))
    (is (= true (get-in receipt [:integrity :source-authenticated?])))
    (is (re-matches #"sha256:[0-9a-f]{64}" (:receipt-id receipt)))))

(deftest malformed-result-and-mutation-never-pass
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-RESULT-STATUS"
           (error-id #(plan/execute-plan p (fn [_] {:status :unknown})))))
    (is (= "UEP-RESULT-MUTATION"
           (error-id #(plan/execute-plan p
                                         (fn [_]
                                           (assoc (passing-result)
                                                  :mutation-detected? true))))))))

(deftest passing-result-must-list-required-evidence
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})]
    (is (= "UEP-RESULT-EVIDENCE"
           (error-id #(plan/execute-plan p
                                         (fn [_]
                                           (assoc (passing-result)
                                                  :evidence [:selected-tests]))))))))

(deftest recomputed-plan-id-cannot-authorize-tampered-closure
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        altered (update-in p [:dependency-closure :paths] conj "unreviewed.txt")
        identity-of (deref (var gravity.self-hosting.unified-execution-plan/identity-of))
        altered (assoc altered :plan-id (identity-of (dissoc altered :plan-id)))]
    (is (= "UEP-PLAN-CLOSURE"
           (error-id #(plan/validate-plan altered))))))

(deftest interrupted-execution-is-rethrown-and-flag-restored
  (let [p (plan/plan {:route-id "sh07-b8-regression" :lane :fast})
        interrupted? (try
                       (plan/execute-plan p
                                          (fn [_] (throw (InterruptedException. "stop"))))
                       false
                       (catch InterruptedException _ true)
                       (finally (Thread/interrupted)))]
    (is interrupted?)))

(deftest current-jvm-adapter-requires-explicit-resource-context
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (plan/execute-plan p)]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (false? (get-in receipt [:integrity :source-authenticated?])))))

(deftest current-jvm-adapter-rejects-unproven-bounds
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (binding [plan/*resource-lease* {:bounded-executor? true
                                                   :timeout-enforced? false
                                                   :deadline-ns Long/MAX_VALUE}]
                  (plan/execute-plan p))]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (not (true? (get-in receipt [:integrity :closure-verified?]))))))

(deftest current-jvm-adapter-rejects-arbitrary-lease
  (let [p (plan/plan {:route-id "sh07-b11-contract" :lane :fast})
        receipt (binding [plan/*resource-lease* {:lease-id "unreviewed"}]
                  (plan/execute-plan p))]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))))

(deftest closure-snapshot-enforces-entry-bound-before-retention
  (let [root (java.nio.file.Files/createTempDirectory "uep-snapshot-" (make-array java.nio.file.attribute.FileAttribute 0))
        path (.resolve root "oversize")
        snapshot (deref (var gravity.self-hosting.unified-execution-plan/closure-snapshot))
        max-bytes (var gravity.self-hosting.unified-execution-plan/max-snapshot-file-bytes)
        root-var (var gravity.self-hosting.unified-execution-plan/snapshot-root)]
    (spit (.toFile path) "0123456789")
    (try
      (is (= "UEP-SNAPSHOT-BOUND"
             (error-id #(with-redefs-fn {root-var (fn [] root)
                                         max-bytes 4}
                          (fn [] (snapshot {:dependency-closure {:paths ["oversize"]}}))))))
      (finally (java.nio.file.Files/deleteIfExists path)
               (java.nio.file.Files/deleteIfExists root)))))

(deftest fresh-executor-requires-explicit-admission-before-isolated-run
  (let [p (plan/plan {:route-id "sh07-authoritative" :lane :fresh})
        receipt (plan/execute-plan p)]
    (is (= :blocked (:status receipt)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (get-in receipt [:diagnostics 0 :id])))
    (is (= :forbidden (get-in receipt [:cache :decision])))
    (is (false? (:authoritative? receipt)))))

(deftest compatibility-routes-expose-existing-selector-shapes
  (testing "all catalog routes are deterministic and non-empty"
    (doseq [route-id (plan/route-names)]
      (let [route (plan/route route-id)]
        (is (= route-id (:id route)))
        (is (seq (get-in route [:selection :selectors])))
        (is (seq (:required-evidence route)))))))
