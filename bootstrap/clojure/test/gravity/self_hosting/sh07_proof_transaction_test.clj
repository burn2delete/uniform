(ns gravity.self-hosting.sh07-proof-transaction-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]))

(def ^:private diagnostics-path
  "bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity")

(defn- required-private-var
  [symbol]
  (or (ns-resolve 'gravity.bootstrap symbol)
      (throw
       (ex-info "Required SH-07 proof transaction implementation is absent"
                {:id "SH07-PROOF-TRANSACTION-IMPLEMENTATION"
                 :symbol symbol}))))

(def ^:private transaction-result
  (delay (bootstrap/sh07-core-file-proof-transaction diagnostics-path)))

(def ^:private legacy-artifact
  (delay (bootstrap/sh07-core-file-artifact diagnostics-path)))

(deftest private-proof-transaction-preserves-authoritative-products
  (let [{:keys [artifact verification capability-proof proof-transaction]}
        @transaction-result
        legacy @legacy-artifact
        phases (:phases proof-transaction)]
    (testing "transactional construction is semantically identical"
      (is (= legacy artifact))
      (is (= (:artifact-id legacy) (:artifact-id artifact)))
      (is (= (bootstrap/sh07-core-artifact-identity-input legacy)
             (bootstrap/sh07-core-artifact-identity-input artifact)))
      (is (= :passed (:status verification)))
      (is (empty? (:failed-checks verification)))
      (is (= :complete (:status capability-proof)))
      (is (empty? (:failed-checks capability-proof))))
    (testing "construction and independent audit are distinct epochs"
      (is (= [:construction :independent-audit]
             (:phase-order proof-transaction)))
      (is (= [0 1] (mapv :epoch phases)))
      (is (= 3
             (reduce + 0
                     (map #(get-in % [:verification-executions :sh05] 0)
                          phases))))
      (is (pos?
           (reduce + 0
                   (map #(get-in % [:verification-reuses :sh05] 0)
                        phases))))
      (is (pos?
           (reduce + 0
                   (map #(get-in % [:verification-reuses :sh06] 0)
                        phases)))))
    (testing "the returned receipt retains no cached carrier"
      (is (= :passed (:status proof-transaction)))
      (is (true? (:thread-confined? proof-transaction)))
      (is (false? (:cross-epoch-reuse? proof-transaction)))
      (is (false? (:failed-report-reuse? proof-transaction)))
      (is (zero? (:failed-report-reuse-count proof-transaction)))
      (is (zero? (:failed-report-executions proof-transaction)))
      (is (true? (:construction-receipts-cleared? proof-transaction)))
      (is (true? (:final-snapshot-rechecked? proof-transaction)))
      (is (map? (get-in proof-transaction
                        [:checked-core-revision :sh05-macro-revision])))
      (is (map? (get-in proof-transaction
                        [:checked-core-revision
                         :sh06-resolution-revision])))
      (is (= (:artifact-id artifact)
             (:artifact-id proof-transaction)))
      (is (= (bootstrap/reader-canonical-hash verification)
             (:verification-report-id proof-transaction)))
      (is (true? (:cleanup-complete? proof-transaction)))
      (is (zero? (:retained-receipt-count proof-transaction)))
      (is (not-any? #(contains? proof-transaction %)
                    [:artifact-value :verification-report :receipts])))))

(deftest altered-artifacts-are-independently-rejected
  (let [artifact (:artifact @transaction-result)
        altered
        (assoc-in artifact
                  [:gravity-core-boundary :canonical-core-artifact
                   :fragment-coverage :form-count]
                  999999)
        report (bootstrap/sh07-core-artifact-verification altered)]
    (is (= (:artifact-id artifact) (:artifact-id altered)))
    (is (= :failed (:status report)))
    (is (seq (:failed-checks report)))))

(deftest transactions-reject-nesting-and-thread-transfer
  (let [original (var-get #'bootstrap/sh07-core-file-artifact)
        first-call? (atom true)
        nested-data
        (try
          (with-redefs [bootstrap/sh07-core-file-artifact
                        (fn [path]
                          (if (compare-and-set! first-call? true false)
                            (bootstrap/sh07-core-file-proof-transaction path)
                            (original path)))]
            (bootstrap/sh07-core-file-proof-transaction diagnostics-path))
          nil
          (catch clojure.lang.ExceptionInfo exception
            (ex-data exception)))
        context-var
        (required-private-var '*sh07-proof-transaction-context*)
        owner-id (.getId (Thread/currentThread))
        context
        (atom {:open? true :owner-thread-id owner-id :phase :construction
               :epoch 0 :maximum-receipts 1 :receipts []
               :executions {} :reuses {} :check-catalogs {}})
        cross-thread-data
        (with-bindings {context-var context}
          @(future
             (try
               (bootstrap/sh05-macro-artifact-verification {})
               nil
               (catch clojure.lang.ExceptionInfo exception
                 (ex-data exception)))))]
    (is (= :nested-transaction (:reason nested-data)))
    (is (= :cross-thread-access (:reason cross-thread-data)))
    (is (empty? (:receipts @context)))
    (let [closed (atom (assoc @context :open? false))
          closed-data
          (with-bindings {context-var closed}
            (try
              (bootstrap/sh05-macro-artifact-verification {})
              nil
              (catch clojure.lang.ExceptionInfo exception
                (ex-data exception))))]
      (is (= :closed-transaction (:reason closed-data))))))

(deftest snapshot-changes-abort-before-independent-audit
  (let [transition-var
        (required-private-var 'sh07-proof-transaction-transition!)
        final-check-var
        (required-private-var
         'sh07-proof-transaction-final-snapshot-check!)
        source-var
        (required-private-var 'sh07-proof-transaction-source-snapshot)
        core-var
        (required-private-var 'sh07-proof-transaction-core-snapshot)
        transition (var-get transition-var)
        final-check (var-get final-check-var)
        expected-source
        {:source-path diagnostics-path
         :source-byte-count 1
         :source-content-hash "sha256:source"}
        expected-core {:source-content-hash "sha256:core"}
        context
        #(atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                :phase :construction :epoch 0 :maximum-receipts 1
                :receipts [] :executions {} :reuses {}
                :completed-phases []})
        failure-for
        (fn [operation source-result core-result]
          (try
            (with-redefs-fn
              {source-var (fn [_] source-result)
               core-var (fn [] core-result)}
              #(operation (context) expected-source expected-core))
            nil
            (catch clojure.lang.ExceptionInfo exception
              (ex-data exception))))]
    (is (= :source-snapshot-changed
           (:reason
            (failure-for transition
                         (assoc expected-source :source-byte-count 2)
                         expected-core))))
    (is (= :checked-core-revision-changed
           (:reason
            (failure-for transition expected-source
                         (assoc expected-core :function-count 1)))))
    (is (= :source-snapshot-changed-during-audit
           (:reason
            (failure-for final-check
                         (assoc expected-source :source-byte-count 2)
                         expected-core))))
    (is (= :checked-core-revision-changed-during-audit
           (:reason
            (failure-for final-check expected-source
                         (assoc expected-core :function-count 1)))))))

(deftest receipt-invalidation-and-failed-report-rules-are-enforced
  (let [context-var
        (required-private-var '*sh07-proof-transaction-context*)
        report-var
        (required-private-var 'sh07-proof-transaction-report)
        report-fn (var-get report-var)
        make-context
        #(atom {:open? true :owner-thread-id (.getId (Thread/currentThread))
                :phase :construction :epoch 0 :maximum-receipts 8
                :receipts [] :executions {} :reuses {}
                :check-catalogs {} :failed-report-executions 0})
        immutable-artifact {:artifact-id "sha256:immutable" :value [1 2 3]}
        passed {:artifact :test/report :status :passed
                :checks {:complete? true} :failed-checks []}
        failed {:artifact :test/report :status :failed
                :checks {:complete? false} :failed-checks [:complete]}
        verify-count (atom 0)
        epoch-context (make-context)]
    (testing "verifier epoch changes force a fresh execution"
      (with-bindings {context-var epoch-context}
        (report-fn :sh07 :final {:root 1} immutable-artifact
                   #(do (swap! verify-count inc) passed))
        (report-fn :sh07 :final {:root 2} immutable-artifact
                   #(do (swap! verify-count inc) passed))
        (report-fn :sh07 :final {:root 2} immutable-artifact
                   #(do (swap! verify-count inc) passed)))
      (is (= 2 @verify-count))
      (is (= 1 (get-in @epoch-context [:reuses :sh07])))
      (is (= 2 (count (:receipts @epoch-context)))))
    (testing "exact verifier roots and check catalogs invalidate receipts"
      (let [context (make-context)
            artifact {:artifact-id "sha256:root" :value [1]}
            calls-a (atom 0)
            calls-b (atom 0)
            verifier-a #(do (swap! calls-a inc) passed)
            verifier-b #(do (swap! calls-b inc) passed)
            epoch-a {:verifier-root verifier-a
                     :report-schema-version 1
                     :check-catalog-domain :test/catalog}
            epoch-b (assoc epoch-a :verifier-root verifier-b)]
        (with-bindings {context-var context}
          (report-fn :sh07 :final epoch-a artifact verifier-a)
          (report-fn :sh07 :final epoch-a artifact verifier-a)
          (report-fn :sh07 :final epoch-b artifact verifier-b)
          (swap! context assoc-in
                 [:receipts 1 :check-catalog-hash]
                 "sha256:altered")
          (report-fn :sh07 :final epoch-b artifact verifier-b))
        (is (= 1 @calls-a))
        (is (= 2 @calls-b))))
    (testing "failed reports and mutable or lazy carriers are never retained"
      (doseq [[artifact result]
              [[immutable-artifact failed]
               [{:artifact-id "sha256:mutable" :value (atom 1)} passed]
               [{:artifact-id "sha256:atomic-number"
                 :value (java.util.concurrent.atomic.AtomicInteger. 1)}
                passed]
               [{:artifact-id "sha256:lazy" :value (lazy-seq [1])}
                passed]]]
        (let [context (make-context)
              executions (atom 0)]
          (with-bindings {context-var context}
            (dotimes [_ 2]
              (report-fn :sh07 :final {:root :stable} artifact
                         #(do (swap! executions inc) result))))
          (is (= 2 @executions))
          (is (empty? (:receipts @context))))))))

(deftest cleanup-runs-for-exceptions-and-interruptions
  (let [observer-var
        (required-private-var '*sh07-proof-transaction-cleanup-observer*)
        report-var
        (required-private-var 'sh07-proof-transaction-report)
        report-fn (var-get report-var)
        original (var-get #'bootstrap/sh07-core-file-artifact)
        exercise
        (fn [failure]
          (let [observed (atom nil)]
            (try
              (with-bindings {observer-var #(reset! observed %)}
                (with-redefs [bootstrap/sh07-core-file-artifact
                              (fn [_]
                                (report-fn
                                 :sh07 :final
                                 {:verifier-root original
                                  :report-schema-version 1
                                  :check-catalog-domain :test/cleanup}
                                 {:artifact-id "sha256:cleanup" :value [1]}
                                 (fn []
                                   {:artifact :test/report :status :passed
                                    :checks {:complete? true}
                                    :failed-checks []}))
                                (throw failure))]
                  (bootstrap/sh07-core-file-proof-transaction
                   diagnostics-path)))
              (catch Throwable _ nil))
            @observed))
        exception-cleanup (exercise (ex-info "expected" {:reason :test}))
        interrupted (InterruptedException. "expected")
        interruption-cleanup (exercise interrupted)]
    (is (.isInterrupted (Thread/currentThread)))
    (Thread/interrupted)
    (is (false? (:open? exception-cleanup)))
    (is (true? (:cleanup-complete? exception-cleanup)))
    (is (pos? (:receipt-count-before-cleanup exception-cleanup)))
    (is (zero? (:retained-receipt-count exception-cleanup)))
    (is (false? (:open? interruption-cleanup)))
    (is (true? (:cleanup-complete? interruption-cleanup)))
    (is (pos? (:receipt-count-before-cleanup interruption-cleanup)))
    (is (zero? (:retained-receipt-count interruption-cleanup)))
    (is (not (.isInterrupted (Thread/currentThread))))
    (is (fn? original))))

(deftest public-verifiers-remain-cache-free-outside-a-transaction
  (let [sh05-var
        (required-private-var 'sh05-macro-artifact-verification*)
        sh06-var
        (required-private-var
         'sh06-resolution-artifact-verification-contained)
        sh07-var
        (required-private-var 'sh07-core-artifact-verification*)
        calls (atom {:sh05 0 :sh06 0 :sh07 0})
        report {:artifact :gravity/sh07-core-artifact-verification
                :status :passed :checks {:complete? true}
                :failed-checks []}]
    (with-redefs-fn
      {sh05-var (fn [_ _] (swap! calls update :sh05 inc) report)
       sh06-var (fn [_ _] (swap! calls update :sh06 inc) report)
       sh07-var (fn [_] (swap! calls update :sh07 inc) report)}
      #(do
         (bootstrap/sh05-macro-artifact-verification {:artifact-id "one"})
         (bootstrap/sh05-macro-artifact-verification {:artifact-id "one"})
         (bootstrap/sh06-resolution-artifact-verification
          {:artifact-id "one"})
         (bootstrap/sh06-resolution-artifact-verification
          {:artifact-id "one"})
         (bootstrap/sh07-core-artifact-verification {:artifact-id "one"})
         (bootstrap/sh07-core-artifact-verification {:artifact-id "one"})))
    (is (= {:sh05 2 :sh06 2 :sh07 2} @calls))
    (is (false? (thread-bound?
                 (required-private-var
                  '*sh07-proof-transaction-context*))))))

(deftest public-verifier-redefinitions-never-reuse-an-old-root
  (let [context-var
        (required-private-var '*sh07-proof-transaction-context*)
        internal-var
        (required-private-var 'sh07-core-artifact-verification*)
        artifact {:artifact-id "sha256:root-redefinition" :value [1]}
        context
        (atom {:open? true
               :owner-thread-id (.getId (Thread/currentThread))
               :phase :independent-audit :epoch 1
               :maximum-receipts 8 :receipts []
               :executions {} :reuses {} :check-catalogs {}
               :failed-report-executions 0})
        calls (atom {:first 0 :second 0})
        report {:artifact :gravity/sh07-core-artifact-verification
                :status :passed :checks {:complete? true}
                :failed-checks []}]
    (with-bindings {context-var context}
      (with-redefs-fn
        {internal-var (fn [_] (swap! calls update :first inc) report)}
        #(do
           (bootstrap/sh07-core-artifact-verification artifact)
           (bootstrap/sh07-core-artifact-verification artifact)))
      (with-redefs-fn
        {internal-var (fn [_] (swap! calls update :second inc) report)}
        #(bootstrap/sh07-core-artifact-verification artifact)))
    (is (= {:first 1 :second 1} @calls))))
