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

(deftest manifest-projections-are-derived-and-complete
  (let [projection (plan/validate-manifest-projections!)
        accounting (:coverage projection)]
    (is (= :gravity/verification-manifest-projection-v1
           (:schema projection)))
    (is (= :non-authoritative (:authority projection)))
    (is (false? (:authoritative? projection)))
    (is (= (plan/manifest-id) (:manifest-id projection)))
    ;; The route projection is a generated view, not a second catalog owner.
    (is (= (:routes plan/catalog) (:routes projection)))
    (is (= (:routes plan/catalog)
           (plan/route-catalog plan/catalog)))
    (is (= ["sh07-b8-regression"]
           (get-in projection [:legacy-routes :impact-test-planner])))
    (is (= ["sh07-b13-accepted" "sh07-conformance-table"]
           (get-in projection [:legacy-routes :iteration-cache-runner])))
    (is (= ["sh07-b13-accepted" "sh07-cached-accepted"
            "sh07-cached-contract" "sh07-cached-identity"
            "sh07-cached-rejected" "sh07-cached-verification"
            "sh07-conformance-table"]
           (get-in projection [:legacy-routes :cached-shard-runner])))
    (is (= (set (mapcat :replacement-for (vals (:routes plan/catalog))))
           (set (keys (:legacy-routes projection)))))
    (is (= {:module-id "sh07-checked-core" :owner :gravity}
           (get-in projection [:module-ownership
                               "bootstrap/gravity/src/gravity/checked_core.gravity"])))
    (is (= (set (keys (:routes plan/catalog)))
           (set (keys (:dependency-closures projection)))))
    (is (= 21 (:route-count accounting)))
    (is (= 1 (:module-count accounting)))
    (is (= 2 (:suite-count accounting)))
    (is (= 109 (:selector-count accounting)))
    (is (= 108 (:exact-var-selector-count accounting)))
    (is (= 1 (:proof-contract-selector-count accounting)))
    (is (= 14 (:fixture-suite-count accounting)))
    (is (= 68 (:accepted-fixture-count accounting)))
    (is (= 79 (:rejected-fixture-count accounting)))
    (is (= 147 (:fixture-pair-count accounting)))))

(deftest manifest-projection-validation-rejects-stale-declarations
  (let [stale-accepted (assoc-in plan/catalog
                                 [:conformance :coverage :accepted-count]
                                 67)
        duplicate-route (update-in plan/catalog
                                   [:coverage :route-ids]
                                   conj "sh07-authoritative")
        duplicate-suite-route (update-in plan/catalog
                                         [:suites 0 :route-ids]
                                         conj "sh07-b8-regression")
        stale-accounting (assoc-in plan/catalog
                                   [:coverage :accounting :selector-count]
                                   39)]
    (is (= "SH07-CONFORMANCE-COVERAGE"
           (error-id #(plan/validate-manifest-projections! stale-accepted))))
    (is (= "UEP-CATALOG-COVERAGE"
           (error-id #(plan/validate-manifest-projections! duplicate-route))))
    (is (= "UEP-CATALOG-SUITE-ROUTES"
           (error-id #(plan/validate-manifest-projections! duplicate-suite-route))))
    (is (= "UEP-MANIFEST-COVERAGE-ACCOUNTING"
           (error-id #(plan/validate-manifest-projections! stale-accounting))))))

(deftest manifest-paths-and-coverage-shapes-fail-closed
  (doseq [unsafe ["C:bootstrap/gravity/src/gravity/checked_core.gravity"
                  "bootstrap/gravity/src/gravity/checked_core.gravity\u0000"
                  "bootstrap/gravity/src/gravity/checked_core.gravity\u0001"]]
    (let [route-ids (keys (:routes plan/catalog))
          changed (reduce (fn [catalog route-id]
                            (update-in catalog [:routes route-id
                                                :dependency-closure :paths]
                                       (fn [paths]
                                         (mapv #(if (= "bootstrap/gravity/src/gravity/checked_core.gravity" %)
                                                  unsafe
                                                  %)
                                               paths))))
                          (assoc-in plan/catalog
                                    [:modules "sh07-checked-core" :source-paths]
                                    [unsafe])
                          route-ids)]
      (is (= "UEP-CATALOG-MODULE-SOURCES"
             (error-id #(plan/validate-catalog changed))))))
  (doseq [route-ids [(set (get-in plan/catalog [:coverage :route-ids]))
                     (seq (get-in plan/catalog [:coverage :route-ids]))]]
    (is (= "UEP-CATALOG-COVERAGE"
           (error-id #(plan/validate-catalog
                       (assoc-in plan/catalog [:coverage :route-ids]
                                 route-ids))))))
  (is (= "UEP-CATALOG-COVERAGE"
         (error-id #(plan/validate-catalog
                     (update-in plan/catalog [:coverage :route-ids]
                                (comp vec reverse)))))))

(deftest canonical-conformance-validation-is-required
  (is (= "SH07-CONFORMANCE-BASENAME"
         (error-id #(plan/validate-catalog
                     (assoc-in plan/catalog
                               [:conformance :suites 0 :accepted 0 :basename]
                               "../not-reviewed"))))))

(deftest conformance-suites-bind-to-manifest-module-ownership
  (let [renamed-module (get-in plan/catalog [:modules "sh07-checked-core"])
        route-ids (keys (:routes plan/catalog))
        changed (reduce (fn [catalog route-id]
                          (-> catalog
                              (assoc-in [:routes route-id :module-id]
                                        "other-module")
                              (assoc-in [:routes route-id :dependency-closure
                                         :module-ids]
                                        ["other-module"])))
                        (-> plan/catalog
                            (update :modules dissoc "sh07-checked-core")
                            (assoc-in [:modules "other-module"]
                                      (assoc renamed-module :id "other-module")))
                        route-ids)]
    (is (= "UEP-CATALOG-CLOSURE-FIXTURE-OWNER"
           (error-id #(plan/validate-manifest-projections! changed))))))

(deftest route-closures-bind-every-module-and-fixture-suite-owner
  (let [other {:id "other-module" :owner :gravity
               :source-paths ["bootstrap/gravity/src/gravity/other.gravity"]
               :artifact-request :checked-core :ownership-scope :sh07
               :required-evidence [:source-identity]}
        with-other (assoc-in plan/catalog [:modules "other-module"] other)
        missing-source
        (update-in with-other
                   [:routes "sh07-b8-regression" :dependency-closure :module-ids]
                   conj "other-module")
        wrong-fixture-owner
        (-> with-other
            (assoc-in [:routes "sh07-conformance-table" :module-id]
                      "other-module")
            (assoc-in [:routes "sh07-conformance-table" :dependency-closure
                       :module-ids]
                      ["other-module"])
            (update-in [:routes "sh07-conformance-table" :dependency-closure
                        :paths]
                       conj "bootstrap/gravity/src/gravity/other.gravity"))]
    (is (= "UEP-CATALOG-ROUTE-CLOSURE"
           (error-id #(plan/validate-catalog missing-source))))
    (is (= "UEP-CATALOG-CLOSURE-FIXTURE-OWNER"
           (error-id #(plan/validate-catalog wrong-fixture-owner))))))
