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
      (is (= :owned-value (:metadata-location check)))
      (is (= :dynamic-borrow-state (:condition check)))
      (is (= :before-operation (:emitted-location check)))
      (is (= :bounded-constant-time (:performance-class check)))
      (is (= [] (:effects-introduced check)))
      (is (= :declared-error (get-in check [:failure :behavior])))
      (is (= :declared-error (:failure-behavior check)))
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
        altered-result-provenance
        (assoc accepted :provenance [{:substituted true}])
        altered-results
        [altered-facts altered-checks altered-result-provenance]]
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
