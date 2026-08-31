(ns gravity.self-hosting.sh10-runtime-borrow-conflict-family-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh10-initialization-move-family-test]))

(defn- repository-root []
  (let [resource
        (io/resource
         "gravity/self_hosting/sh10_runtime_borrow_conflict_family_test.clj")]
    (when-not resource
      (throw
       (ex-info "SH-10 runtime borrow test is not on the classpath"
                {:id "SH10-RUNTIME-BORROW-TEST-SOURCE"})))
    (loop [candidate (.getParent (.toPath (io/file (.toURI resource))))]
      (cond
        (nil? candidate)
        (throw
         (ex-info "Repository root could not be located"
                  {:id "SH10-RUNTIME-BORROW-ROOT"}))

        (.isFile (.toFile (.resolve candidate "deps.edn")))
        candidate

        :else
        (recur (.getParent candidate))))))

(def ^:private root (delay (repository-root)))

(defn- path [relative]
  (str (.resolve @root relative)))

(defn- compile-plan [relative]
  (let [source-path (path relative)
        source-text (slurp source-path)
        emitter
        (:emitter
         (bootstrap/c-backend-stage2-plan-emitter-source-rule!
          source-path :jvm))]
    (bootstrap/p15-s23-stage2-compiler-artifact-plan
     emitter source-path source-text)))

(def ^:private fixture-root
  "bootstrap/clojure/fixtures/self-hosting/sh-10/runtime-borrow-conflict")

(defn- fixture-path [disposition basename extension]
  (str fixture-root "/" disposition "/" basename extension))

(def ^:private accepted-gravity-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "runtime-borrow-conflict" ".gravity"))))

(def ^:private accepted-qst-plan
  (delay
   (compile-plan
    (fixture-path "accepted" "runtime-borrow-conflict" ".qst"))))

(def ^:private rejected-gravity-plan
  (delay
   (compile-plan
    (fixture-path
     "rejected" "invalid-runtime-borrow-conflict" ".gravity"))))

(def ^:private rejected-qst-plan
  (delay
   (compile-plan
    (fixture-path
     "rejected" "invalid-runtime-borrow-conflict" ".qst"))))

(defn- init-var [name]
  (or
   (ns-resolve
    'gravity.self-hosting.sh10-initialization-move-family-test name)
   (throw
    (ex-info "Required SH-10 initialization helper is unavailable"
             {:id "SH10-RUNTIME-BORROW-INIT-HELPER" :name name}))))

(defn- init-value [name]
  (var-get (init-var name)))

(defn- invoke [plan function arguments]
  (bootstrap/p15-s23-stage2-runtime-execute-function
   {:engine :gravity-sh10-runtime-borrow-conflict-family
    :compiler-artifact-plan? true}
   @plan function arguments))

(defn- invoke-c9 [function arguments]
  ((init-value 'invoke-c9) function arguments))

(defn- prepared []
  @(init-value 'prepared))

(defn- core-node-id []
  ((init-value 'core-node-id)))

(defn- scenario [plan function]
  (invoke plan function [(core-node-id)]))

(defn- analyze-with [bound verification scenario]
  (invoke-c9
   'sh10-build-runtime-mutable-borrow-analysis
   [bound verification scenario]))

(defn- analyze [scenario]
  (analyze-with
   (:bound (prepared)) (:verification (prepared)) scenario))

(defn- verify-analysis [scenario candidate]
  (invoke-c9
   'sh10-verify-runtime-mutable-borrow-analysis
   [(:bound (prepared)) (:verification (prepared)) scenario candidate]))

(def ^:private accepted-function
  'sh10-runtime-mutable-borrow-end-reborrow-scenario)

(def ^:private rejected-cases
  {'sh10-overlapping-mutable-borrows-scenario
   {:rule "C9-MUT-ALIAS"
    :reason :multiple-active-mutable-borrows
    :operation :borrow-mutable
    :facts [:borrow-mutable]}
   'sh10-immutable-then-mutable-borrow-scenario
   {:rule "C9-MUT-ALIAS"
    :reason :mutable-borrow-with-active-immutable-aliases
    :operation :borrow-mutable
    :facts [:borrow-immutable]}
   'sh10-mutable-then-immutable-borrow-scenario
   {:rule "C9-MUT-ALIAS"
    :reason :immutable-borrow-during-active-mutable-borrow
    :operation :borrow-immutable
    :facts [:borrow-mutable]}
   'sh10-move-while-mutably-borrowed-scenario
   {:rule "C9-MOVE-WHILE-BORROWED"
    :reason :move-during-active-borrow
    :operation :move
    :facts [:borrow-mutable]}
   'sh10-consume-while-mutably-borrowed-scenario
   {:rule "C9-UNSAFE"
    :reason :consume-during-active-borrow
    :operation :consume
    :facts [:borrow-mutable]}
   'sh10-stale-mutable-borrow-end-scenario
   {:rule "C9-UNSAFE"
    :reason :end-of-inactive-borrow
    :operation :end-borrow
    :facts [:borrow-mutable :end-borrow :borrow-mutable]}
   'sh10-mutable-borrow-identity-reuse-scenario
   {:rule "C9-UNSAFE"
    :reason :borrow-identity-reuse
    :operation :borrow-mutable
    :facts [:borrow-mutable :end-borrow]}
   'sh10-cross-kind-borrow-identity-reuse-scenario
   {:rule "C9-UNSAFE"
    :reason :borrow-identity-reuse
    :operation :borrow-mutable
    :facts [:borrow-immutable :end-borrow]}
   'sh10-active-mutable-borrow-at-exit-scenario
   {:rule "C9-BORROW-ESCAPE"
    :reason :active-borrow-at-scope-exit
    :operation :scope-exit
    :facts [:borrow-mutable]}
   'sh10-active-immutable-borrow-at-exit-scenario
   {:rule "C9-BORROW-ESCAPE"
    :reason :active-borrow-at-scope-exit
    :operation :scope-exit
    :facts [:borrow-immutable]}
   'sh10-negative-owner-lifetime-scenario
   {:rule "C9-UNSAFE"
    :reason :malformed-runtime-mutable-borrow-scenario
    :operation nil
    :facts []}
   'sh10-fractional-owner-lifetime-scenario
   {:rule "C9-UNSAFE"
    :reason :malformed-runtime-mutable-borrow-scenario
    :operation nil
    :facts []}
   'sh10-mutable-borrow-escape-scenario
   {:rule "C9-BORROW-ESCAPE"
    :reason :borrow-outlives-owner
    :operation :escape-borrow
    :facts [:borrow-mutable]}})

(deftest sh10-runtime-borrow-source-api-and-fixtures-are-bounded
  (let [policy (invoke-c9 'sh10-runtime-mutable-borrow-policy [])]
    (is (= :gravity/sh10-runtime-mutable-borrow-policy
           (:artifact policy)))
    (is (= :runtime-checked (:runtime-check-outcome policy)))
    (is (= :dynamic-borrow-state (:runtime-check-kind policy)))
    (is (= :meta-jvm-dynamic-borrow-state
           (:runtime-check-provider policy)))
    (is (= :declared-error (:runtime-failure-behavior policy)))
    (is (= :forbidden (:runtime-panic-behavior policy)))
    (is (= :finite-integral-bounded-i32
           (:lifetime-numeric-mode policy)))
    (is (= 2147483647 (:lifetime-coordinate-maximum policy)))
    (is (= :meta
           (get-in policy [:runtime-provider-contract :profile])))
    (is (= #{}
           (get-in policy [:runtime-provider-contract :effects])))
    (is (= #{:initialize :read :borrow-immutable :borrow-mutable
             :end-borrow :move :consume :escape-borrow}
           (:accepted-events policy)))
    (is (some #{:general-runtime-borrow-check-execution}
              (:pending policy)))
    (is (= (slurp
            (path
             (fixture-path
              "accepted" "runtime-borrow-conflict" ".gravity")))
           (slurp
            (path
             (fixture-path
              "accepted" "runtime-borrow-conflict" ".qst")))))
    (is (= (slurp
            (path
             (fixture-path
              "rejected" "invalid-runtime-borrow-conflict" ".gravity")))
           (slurp
            (path
             (fixture-path
              "rejected" "invalid-runtime-borrow-conflict" ".qst")))))
    (doseq [plan [accepted-gravity-plan accepted-qst-plan
                  rejected-gravity-plan rejected-qst-plan]]
      (is (= :gravity/stage2-compiler-artifact-plan (:kind @plan))))))

(deftest sh10-runtime-borrow-accepts-borrow-end-and-fresh-reborrow
  (let [gravity-scenario (scenario accepted-gravity-plan accepted-function)
        qst-scenario (scenario accepted-qst-plan accepted-function)
        gravity-result (analyze gravity-scenario)
        qst-result (analyze qst-scenario)
        checks (:runtime-check-records gravity-result)
        effect-request
        (first (get-in (prepared)
                       [:bound :effected-core :effect-requests]))]
    (is (= gravity-scenario qst-scenario))
    (is (= gravity-result qst-result))
    (is (= :accepted (:status gravity-result)))
    (is (= :runtime-checked (:safety-outcome gravity-result)))
    (is (= :authenticated-sh09-runtime-checked-mutable-borrow-conflict
           (:scope gravity-result)))
    (is (= [:initialize :borrow-mutable :end-borrow
            :borrow-mutable :end-borrow :read]
           (mapv :operation (:ownership-facts gravity-result))))
    (is (= [:mutable-first :mutable-first
            :mutable-second :mutable-second]
           (mapv :borrow-id checks)))
    (is (= [:borrow-mutable :end-borrow
            :borrow-mutable :end-borrow]
           (mapv :operation checks)))
    (is (= [0 1 2 3] (mapv :sequence checks)))
    (is (= checks (:runtime-checks gravity-result)))
    (is (= checks (:residual-checks gravity-result)))
    (doseq [check checks]
      (is (= :gravity/c9-runtime-check-record (:artifact check)))
      (is (= :dynamic-borrow-state (:kind check)))
      (is (= :runtime-checked (:safety-outcome check)))
      (is (= :insufficient-dynamic-alias-state
             (:static-proof-status check)))
      (is (= :meta-jvm-dynamic-borrow-state (:provider check)))
      (is (= :required-at-lowering (:provider-binding-status check)))
      (is (= (:runtime-provider-contract
              (invoke-c9 'sh10-runtime-mutable-borrow-policy []))
             (:provider-contract check)))
      (is (= :owned-value (:metadata-location check)))
      (is (= :dynamic-borrow-state (:condition check)))
      (is (= :before-operation (:emitted-location check)))
      (is (= :bounded-constant-time (:performance-class check)))
      (is (= [] (:effects-introduced check)))
      (is (= :declared-error (get-in check [:failure :behavior])))
      (is (= :forbidden (get-in check [:failure :panic-behavior])))
      (is (= :declared-error (:failure-behavior check)))
      (is (= :forbidden (:panic-behavior check)))
      (is (= :finite-integral-bounded-i32
             (:lifetime-numeric-mode check)))
      (is (= #{}
             (get-in check [:c8-residual-effect-evidence
                            :residual-effects])))
      (is (= :meta (:profile check)))
      (is (= :jvm (:target check)))
      (is (= (:source-span effect-request) (:source-span check)))
      (is (= (:origin-chain effect-request) (:origin-chain check))))
    (is (= :available
           (get-in gravity-result [:ownership-result :state :availability])))
    (is (nil?
         (get-in gravity-result
                 [:ownership-result :state :mutable-borrow-id])))
    (is (= :passed
           (:status (verify-analysis gravity-scenario gravity-result))))))

(deftest sh10-runtime-borrow-residual-checks-immutable-acquisition-and-end
  (let [gravity-scenario
        (scenario accepted-gravity-plan
                  'sh10-runtime-immutable-borrow-end-scenario)
        qst-scenario
        (scenario accepted-qst-plan
                  'sh10-runtime-immutable-borrow-end-scenario)
        result (analyze gravity-scenario)
        checks (:runtime-check-records result)]
    (is (= gravity-scenario qst-scenario))
    (is (= result (analyze qst-scenario)))
    (is (= :accepted (:status result)))
    (is (= :runtime-checked (:safety-outcome result)))
    (is (= [:initialize :borrow-immutable :end-borrow :read]
           (mapv :operation (:ownership-facts result))))
    (is (= [:borrow-immutable :end-borrow]
           (mapv :operation checks)))
    (is (= [:no-active-mutable-or-external-exclusive-alias
            :matching-active-immutable-borrow]
           (mapv :required-state checks)))
    (is (= [0 1] (mapv :sequence checks)))
    (is (= :passed (:status (verify-analysis gravity-scenario result))))))

(deftest sh10-runtime-borrow-rejects-static-conflicts
  (doseq [[function expected] rejected-cases]
    (testing (str function)
      (let [gravity-scenario (scenario rejected-gravity-plan function)
            qst-scenario (scenario rejected-qst-plan function)
            gravity-result (analyze gravity-scenario)
            qst-result (analyze qst-scenario)
            diagnostic (first (:diagnostics gravity-result))]
        (is (= gravity-scenario qst-scenario))
        (is (= gravity-result qst-result))
        (is (= :rejected (:status gravity-result)))
        (is (= :rejected (:safety-outcome gravity-result)))
        (is (= (:rule expected) (:diagnostic-id diagnostic)))
        (is (= (:rule expected) (:rule diagnostic)))
        (is (= (:reason expected) (:reason diagnostic)))
        (is (= (:operation expected) (:operation diagnostic)))
        (is (= (:facts expected)
               (mapv :operation (:ownership-facts gravity-result))))
        (is (= :ownership-checking (:stage diagnostic)))
        (is (= :error (:severity diagnostic)))
        (is (= :meta (:profile diagnostic)))
        (is (= :jvm (:target diagnostic)))
        (is (map? (:source-span diagnostic)))
        (is (vector? (:generated-origin-chain diagnostic)))
        (is (= :passed
               (:status
                (verify-analysis gravity-scenario gravity-result))))))))

(deftest sh10-runtime-borrow-rejects-identity-fact-and-provenance-substitution
  (let [scenario (scenario accepted-gravity-plan accepted-function)
        accepted (analyze scenario)
        bound (:bound (prepared))
        verification (:verification (prepared))
        wrong-node (assoc scenario :core-node-id :substituted-node)
        wrong-node-result (analyze wrong-node)
        wrong-fact
        (assoc-in
         bound [:fact-identities (core-node-id) :type-fact-id]
         "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff")
        wrong-fact-result (analyze-with wrong-fact verification scenario)
        wrong-identity
        (assoc bound :identity-input {:substituted true})
        wrong-identity-result (analyze-with wrong-identity verification scenario)
        wrong-provenance
        (assoc bound :provenance [{:substituted true}])
        wrong-provenance-result
        (analyze-with wrong-provenance verification scenario)
        coordinated-provenance-bound
        (assoc bound :provenance
               {:actual-source-path "substituted.gravity"
                :provenance-binding-id
                "sha256:ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"})
        coordinated-provenance-verification
        (assoc verification
               :expected coordinated-provenance-bound
               :candidate coordinated-provenance-bound)
        coordinated-provenance-result
        (analyze-with coordinated-provenance-bound
                      coordinated-provenance-verification scenario)
        coordinated-effect-bound
        (assoc-in bound [:effected-core :effect-requests 0 :effect]
                  :compiler/write-ir)
        coordinated-effect-verification
        (assoc verification
               :expected coordinated-effect-bound
               :candidate coordinated-effect-bound)
        coordinated-effect-result
        (analyze-with coordinated-effect-bound
                      coordinated-effect-verification scenario)
        coordinated-unsafe-bound
        (assoc-in bound [:effected-core :module :safety] :unsafe)
        coordinated-unsafe-verification
        (assoc verification
               :expected coordinated-unsafe-bound
               :candidate coordinated-unsafe-bound)
        coordinated-unsafe-result
        (analyze-with coordinated-unsafe-bound
                      coordinated-unsafe-verification scenario)
        duplicate-evidence-bound
        (let [effected (:effected-core bound)]
          (assoc
           bound :effected-core
           (assoc
            (assoc effected
                   :effect-requests
                   (conj (:effect-requests effected)
                         (first (:effect-requests effected))))
            :effect-legality-results
            (conj (:effect-legality-results effected)
                  (first (:effect-legality-results effected))))))
        duplicate-evidence-verification
        (assoc verification
               :expected duplicate-evidence-bound
               :candidate duplicate-evidence-bound)
        duplicate-evidence-result
        (analyze-with duplicate-evidence-bound
                      duplicate-evidence-verification scenario)
        unsupported-bound
        (assoc-in bound [:effected-core :module :target] :no-runtime)
        unsupported-verification
        (assoc verification
               :expected unsupported-bound
               :candidate unsupported-bound)
        unsupported-result
        (analyze-with
         unsupported-bound unsupported-verification scenario)
        altered-facts (assoc accepted :ownership-facts [])
        altered-checks (assoc accepted :runtime-check-records [])
        altered-provider
        (assoc-in accepted [:runtime-check-records 0 :provider]
                  :substituted-provider)
        altered-panic
        (assoc-in accepted [:runtime-check-records 0 :panic-behavior]
                  :panic)
        altered-result-provenance
        (assoc accepted :provenance [{:substituted true}])
        altered-results
        [altered-facts altered-checks altered-provider altered-panic
         altered-result-provenance]]
    (is (= :rejected (:status wrong-node-result)))
    (is (= "C9-UNSAFE"
           (get-in wrong-node-result [:diagnostics 0 :diagnostic-id])))
    (is (= :scenario-does-not-bind-an-authenticated-sh09-fact
           (get-in wrong-node-result [:diagnostics 0 :reason])))
    (doseq [result
            [wrong-fact-result wrong-identity-result wrong-provenance-result]]
      (is (= :rejected (:status result)))
      (is (= :rejected (:safety-outcome result)))
      (is (= "C9-UNSAFE"
             (get-in result [:diagnostics 0 :diagnostic-id])))
      (is (= :untrusted-or-malformed-sh09-effected-core
             (get-in result [:diagnostics 0 :reason]))))
    (doseq [result [coordinated-provenance-result
                    coordinated-effect-result
                    coordinated-unsafe-result]]
      (is (= :rejected (:status result)))
      (is (= "C9-RUNTIME-CHECK"
             (get-in result [:diagnostics 0 :diagnostic-id])))
      (is (= :unauthenticated-runtime-effect-capability-provider-contract
             (get-in result [:diagnostics 0 :reason]))))
    (is (= :rejected (:status duplicate-evidence-result)))
    (is (= "C9-UNSAFE"
           (get-in duplicate-evidence-result [:diagnostics 0 :diagnostic-id])))
    (is (= :scenario-does-not-bind-an-authenticated-sh09-fact
           (get-in duplicate-evidence-result [:diagnostics 0 :reason])))
    (is (= :rejected (:status unsupported-result)))
    (is (= "C9-RUNTIME-CHECK"
           (get-in unsupported-result [:diagnostics 0 :diagnostic-id])))
    (is (= :runtime-borrow-check-unavailable-in-profile
           (get-in unsupported-result [:diagnostics 0 :reason])))
    (doseq [candidate altered-results]
      (let [verification-result (verify-analysis scenario candidate)]
        (is (= :rejected (:status verification-result)))
        (is (= "C9-UNSAFE"
               (get-in verification-result
                       [:diagnostics 0 :diagnostic-id])))
        (is (= :runtime-mutable-borrow-analysis-substitution
               (get-in verification-result [:diagnostics 0 :reason])))))))

(deftest sh10-runtime-borrow-rejects-nonfinite-and-out-of-range-lifetimes
  (let [base (scenario accepted-gravity-plan accepted-function)
        invalid-owner-values
        [Double/NaN Double/POSITIVE_INFINITY Double/NEGATIVE_INFINITY
         -1 3/2 2147483648]
        escape-base
        (scenario rejected-gravity-plan
                  'sh10-mutable-borrow-escape-scenario)]
    (doseq [value invalid-owner-values]
      (let [result (analyze (assoc base :owner-lifetime-end value))]
        (is (= :rejected (:status result)))
        (is (= "C9-UNSAFE"
               (get-in result [:diagnostics 0 :diagnostic-id])))
        (is (= :malformed-runtime-mutable-borrow-scenario
               (get-in result [:diagnostics 0 :reason])))))
    (doseq [value invalid-owner-values]
      (let [candidate
            (assoc-in escape-base
                      [:events 1 :destination-lifetime-end] value)
            result (analyze candidate)]
        (is (= :rejected (:status result)))
        (is (= :malformed-runtime-mutable-borrow-scenario
               (get-in result [:diagnostics 0 :reason])))))))

(deftest sh10-runtime-borrow-narrows-fully-coordinated-provenance-claim
  (let [scenario (scenario accepted-gravity-plan accepted-function)
        bound (:bound (prepared))
        verification (:verification (prepared))
        substituted
        {:actual-source-path "substituted.gravity"
         :provenance-binding-id
         "sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"}
        coordinated-bound
        (assoc
         (assoc bound :provenance substituted)
         :effected-core
         (assoc (:effected-core bound) :provenance substituted))
        coordinated-verification
        (assoc verification
               :expected coordinated-bound
               :candidate coordinated-bound)
        result
        (analyze-with coordinated-bound coordinated-verification scenario)]
    (is (= :accepted (:status result)))
    (is (= :cross-carrier-consistency-only
           (:provenance-authentication result)))
    (is (some #{:independent-provenance-issuer} (:nonclaims result)))
    (is (some #{:full-coordinated-provenance-substitution-resistance}
              (:nonclaims result)))
    (is (= substituted (:provenance result)))))
