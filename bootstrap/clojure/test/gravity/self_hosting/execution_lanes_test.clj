(ns gravity.self-hosting.execution-lanes-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [gravity.self-hosting.execution-lanes :as lanes]
            [gravity.self-hosting.sh01-host-resource-broker :as broker]
            [gravity.self-hosting.sh07-authoritative-runner :as authoritative]
            [gravity.self-hosting.unified-execution-plan :as plan]))

(def ^:private test-commit (apply str (repeat 40 "a")))
(def ^:private test-tree (apply str (repeat 40 "b")))

(def ^:private coordination-root (atom nil))

(defn- delete-tree! [root]
  (when (and root
             (java.nio.file.Files/exists
              root (make-array java.nio.file.LinkOption 0)))
    (with-open [paths (java.nio.file.Files/walk
                       root
                       (make-array java.nio.file.FileVisitOption 0))]
      (.forEach
       (.sorted paths (java.util.Comparator/reverseOrder))
       (reify java.util.function.Consumer
         (accept [_ path]
           (java.nio.file.Files/deleteIfExists path)))))))

(use-fixtures
  :once
  (fn [tests]
    (let [root (java.nio.file.Files/createTempDirectory
                "uep-broker-"
                (make-array java.nio.file.attribute.FileAttribute 0))]
      (java.nio.file.Files/setPosixFilePermissions
       root
       (java.util.Set/of
        java.nio.file.attribute.PosixFilePermission/OWNER_READ
        java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
        java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE))
      (reset! coordination-root (str root))
      (try (tests)
           (finally
             (reset! coordination-root nil)
             (delete-tree! root))))))

(use-fixtures
  :each
  (fn [tests]
    (binding [lanes/*repository-identity*
              (constantly {:candidate-commit test-commit
                           :candidate-tree test-tree
                           :clean? true})]
      (try
        (tests)
        (finally
          (lanes/reset-warm-sessions!))))))

(defn- fake-warm-start
  [starts responses]
  (fn [{:keys [candidate-commit candidate-tree] :as request}]
    (swap! starts conj request)
    (let [records (java.util.concurrent.ArrayBlockingQueue. 16)
          calls (atom 0)
          closed? (atom false)
          response-for (or responses (constantly :passed))]
      (.offer records
              (str "WARM_DEV "
                   (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                            :candidate-commit candidate-commit
                            :candidate-tree candidate-tree
                            :authority :non-authoritative})))
      {:read-line! (fn [] (.poll records 2 java.util.concurrent.TimeUnit/SECONDS))
       :write-line! (fn [line]
                      (let [request (edn/read-string line)
                            outcome (response-for (swap! calls inc))
                            response {:schema :gravity/exact-tree-warm-worker-response-v1
                                      :authority :non-authoritative
                                      :authoritative? false
                                      :request-id (:request-id request)
                                      :candidate-commit candidate-commit
                                      :candidate-tree candidate-tree
                                      :outcome outcome
                                      :exit-code (if (= :passed outcome) 0 1)
                                      :elapsed-ns 1
                                      :request-count @calls
                                      :stdout ""
                                      :stderr ""
                                      :output-truncated? false}]
                        (.offer records (str "WARM_DEV " (pr-str response)))))
       :process-alive? (fn [] (not @closed?))
       :close! (fn [] (reset! closed? true))})))

(defn- fast-plan []
  (plan/plan {:route-id "sh07-b11-contract" :lane :fast}))

(defn- fast-plan-with-timeout [timeout-ms]
  (plan/plan {:route-id "sh07-b11-contract"
              :lane :fast
              :resource-policy {:resource-class :normal
                                :timeout-ms timeout-ms
                                :max-output-bytes 262144}}))

(defn- fresh-plan []
  (plan/plan {:route-id "sh07-authoritative" :lane :fresh}))

(defn- clean-process-result
  ([stdout]
   (clean-process-result stdout {}))
  ([stdout options]
   (merge {:exit-code 0
           :stdout stdout
           :stderr ""
           :timed-out? false
           :stdout-truncated? false
           :stderr-truncated? false
           :cleanup {:complete? true}}
          options)))

(defn- fast-output []
  "UEP_FAST_RESULT {:schema :gravity/unified-fast-result-v1, :status :passed, :summary {:test 2, :pass 29, :fail 0, :error 0}}")

(defn- fresh-output []
  "{:artifact :gravity/sh07-authoritative-proof-run, :schema-version 3, :status :passed, :coverage-census-policy :source-bound-derived, :authority-scope :individual-source-bound-derived, :counts-precommitted? false, :aggregate-authoritative? false, :fresh-process-required? true, :persistent-iteration-cache-used? false, :modules [{:module \"c6-core\", :status :accepted, :contract-checks {:ok true}, :failed-checks []}]}")

(defn- receipt-envelope [modules]
  {:artifact :gravity/sh07-authoritative-proof-run
   :schema-version 3
   :status :passed
   :coverage-census-policy :source-bound-derived
   :authority-scope :individual-source-bound-derived
   :counts-precommitted? false
   :independent-count-oracle? false
   :aggregate-authoritative? false
   :unsupported-claims [:exact-authentic-coverage :aggregate :release]
   :attestation-required? true
   :fresh-process-required? true
   :persistent-iteration-cache-used? false
   :proof-receipt-reuse-used? false
   :proof-receipt-reuse-count 0
   :modules modules
   :elapsed-ms 0})

(defn- sha [character]
  (str "sha256:" (apply str (repeat 64 character))))

(defn- valid-revision [source-path source-binding]
  {:source-byte-count (:source-byte-count source-binding)
   :source-content-hash (:source-bytes-sha256 source-binding)
   :plan-semantic-hash (sha "1")
   :functions-semantic-hash (sha "2")
   :function-count 1
   :function-names-hash (sha "3")
   :function-shapes-hash (sha "4")
   :public-function-hashes {}
   :public-function-shapes {}
   :observed-source-path source-path})

(defn- contract-valid-c6-receipt []
  (let [{:keys [source-path source-binding]}
        (get (authoritative/module-source-contracts) "c6-core")
        source-hash (:source-bytes-sha256 source-binding)
        artifact-id (sha "a")
        artifact {:artifact-id artifact-id
                  :sh06-resolution-artifact {:status :accepted}
                  :task "SH-07-B47"
                  :gravity-core-boundary {:target-source-reread? false}}
        request {:module {:namespace 'gravity.compiler.c6-core-lowering-engine
                          :source-revision-id source-hash}
                 :lineage {:source-revision-id source-hash}
                 :fragment-manifest []
                 :top-level-form-ids []
                 :forms []
                 :binding-table []
                 :resolution-table []
                 :schema-version 15
                 :scope :sh07-b15-keyword-map-lookup}
        core {:fragment-coverage {:covered-root-form-ids []
                                  :covered-form-ids []}
              :nodes [] :definitions [] :calls [] :reference-uses []
              :keyword-lookups []}
        census (authoritative/authoritative-coverage-census
                "c6-core" artifact request core
                {:byte-count (:source-byte-count source-binding)
                 :sha256 source-hash}
                {:byte-count (:source-byte-count source-binding)
                 :sha256 source-hash}
                :source-bound-derived)
        check-keys
        @(ns-resolve 'gravity.self-hosting.sh07-authoritative-runner
                     'authoritative-contract-check-keys)
        revision (valid-revision
                  "bootstrap/gravity/src/gravity/checked_core.gravity"
                  source-binding)
        module-result
        {:module "c6-core"
         :source-path source-path
         :source-byte-count (:source-byte-count source-binding)
         :source-bytes-sha256 source-hash
         :source-revision-id source-hash
         :status :accepted
         :artifact-id artifact-id
         :coverage-census census
         :schema-version 15
         :fragment-count 0 :form-count 0 :resolution-count 0
         :keyword-lookup-count 0 :function-record-count 0
         :call-edge-count 0 :recursion-component-count 0
         :verification-status :passed
         :capability-proof-status :complete
         :proof-transaction
         {:artifact :gravity/sh07-proof-transaction-receipt
          :schema-version 1 :status :passed :thread-confined? true
          :owner-thread-id 1
          :phase-order [:construction :independent-audit]
          :phases
          [{:phase :construction :epoch 0
            :verification-executions {} :verification-reuses {}
            :receipt-count 0}
           {:phase :independent-audit :epoch 1
            :verification-executions {:sh05 1 :sh06 1 :sh07 1}
            :verification-reuses {} :receipt-count 0}]
          :source-snapshot
          {:source-path source-path
           :source-byte-count (:source-byte-count source-binding)
           :source-content-hash source-hash}
          :checked-core-revision
          (assoc revision
                 :sh05-macro-revision (valid-revision "sh05.gravity" source-binding)
                 :sh06-resolution-revision (valid-revision "sh06.gravity" source-binding))
          :check-catalog-bindings
          {[:sh05 :construction] (sha "5")
           [:sh05 :final] (sha "6")
           [:sh06 :final] (sha "7")
           [:sh07 :final] (sha "8")}
          :maximum-receipts 64
          :artifact-id artifact-id
          :verification-report-id (sha "9")
          :construction-receipts-cleared? true
          :final-snapshot-rechecked? true
          :cross-epoch-reuse-count 0 :cross-epoch-reuse? false
          :failed-report-executions 0
          :failed-report-reuse-count 0 :failed-report-reuse? false
          :cleanup-complete? true :retained-receipt-count 0}
         :failed-checks []
         :contract-checks (zipmap check-keys (repeat true))
         :elapsed-ms 0}]
    (receipt-envelope [module-result])))

(defn- production-fresh-export [plan-value]
  ((deref (ns-resolve 'gravity.self-hosting.execution-lanes
                      'export-closure-default))
   plan-value))

(defn- private-lane-fn [symbol]
  (deref (ns-resolve 'gravity.self-hosting.execution-lanes symbol)))

(defn- git-ignored? [path]
  (let [process (.start (ProcessBuilder. ^java.util.List
                                         ["git" "check-ignore" "--quiet"
                                          (str path)]))]
    (zero? (.waitFor process))))

(deftest fast-adapter-acquires-and-releases-broker-lease
  (let [acquired (atom [])
        released (atom [])
        starts (atom [])
        snapshots (atom 0)
        start-fn (fake-warm-start starts nil)
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*broker-acquire!*
                  (fn [options resource-class]
                    (swap! acquired conj [options resource-class])
                    (broker/acquire! options resource-class))
                  lanes/*broker-release!*
                  (fn [lease]
                    (swap! released conj lease)
                    (broker/release! lease))
                  lanes/*warm-session-start!* start-fn
                  lanes/*closure-snapshot*
                  (fn [_]
                    (swap! snapshots inc)
                    {:identity :stable})]
          [(lanes/run-fast! (fast-plan))
           (lanes/run-fast! (fast-plan))])]
    (is (= :passed (:status (first result))))
    (is (= :passed (:status (second result))))
    (is (= 1 (count @acquired)))
    (is (zero? (count @released)))
    (is (= 4 @snapshots))
    (is (= 1 (count @starts)))
    (is (= "clojure" (get-in @starts [0 :command 0])))
    (is (= test-commit (get-in @starts [0 :candidate-commit])))
    (is (= test-tree (get-in @starts [0 :candidate-tree])))
    (is (= :miss (:cache-decision (first result))))))

(deftest fast-adapter-blocks-without-coordination-root
  (let [result
        (binding [lanes/*coordination-root* nil
                  lanes/*broker-acquire!* (fn [& _] (throw (ex-info "called" {})))]
          (lanes/run-fast! (fast-plan)))]
    (is (= :blocked (:status result)))
    (is (= "UEP-RESOURCE-LEASE-REQUIRED"
           (get-in result [:diagnostics 0 :id])))))

(deftest fast-adapter-converts-worker-rejection
  (let [starts (atom [])
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*warm-session-start!* (fake-warm-start starts
                                                              (constantly :failed))
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (lanes/run-fast! (fast-plan)))]
    (is (= :failed (:status result)))
    (is (= "UEP-WARM-TEST-FAILED"
           (some->> (:diagnostics result)
                    (map :id)
                    (some #{"UEP-WARM-TEST-FAILED"}))))
    (is (= 1 (count @starts)))))

(deftest fast-adapter-evicts-and-releases-after-worker-failure
  (let [released (atom 0)
        starts (atom [])
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*broker-release!*
                  (fn [lease]
                    (broker/release! lease)
                    (swap! released inc))
                  lanes/*warm-session-start!*
                  (fake-warm-start starts (constantly :failed))
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (lanes/run-fast! (fast-plan)))]
    (is (= :failed (:status result)))
    (is (= 1 (count @starts)))
    (Thread/sleep 100)
    (is (= 1 @released))))

(deftest fast-adapter-rejects-malformed-worker-response
  (let [released (atom 0)
        starts (atom [])
        start-fn
        (fn [{:keys [candidate-commit candidate-tree] :as request}]
          (swap! starts conj request)
          (let [records (java.util.concurrent.ArrayBlockingQueue. 4)
                closed? (atom false)]
            (.offer records
                    (str "WARM_DEV "
                         (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                                  :candidate-commit candidate-commit
                                  :candidate-tree candidate-tree
                                  :authority :non-authoritative})))
            {:read-line! (fn []
                           (.poll records 2 java.util.concurrent.TimeUnit/SECONDS))
             :write-line! (fn [_] (.offer records "WARM_DEV {:malformed true}"))
             :process-alive? (fn [] (not @closed?))
             :close! (fn [] (reset! closed? true))}))
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*broker-release!*
                  (fn [lease]
                    (broker/release! lease)
                    (swap! released inc))
                  lanes/*warm-session-start!* start-fn
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (lanes/run-fast! (fast-plan)))]
    (is (= :failed (:status result)))
    (is (some #{"UEP-WARM-RESPONSE"}
              (map :id (:diagnostics result))))
    (is (= 1 (count @starts)))
    (Thread/sleep 100)
    (is (= 1 @released))))

(deftest fast-adapter-times-out-and-releases-worker
  (let [released (atom 0)
        starts (atom [])
        start-fn
        (fn [{:keys [candidate-commit candidate-tree] :as request}]
          (swap! starts conj request)
          (let [records (java.util.concurrent.ArrayBlockingQueue. 4)
                reads (atom 0)
                closed? (atom false)]
            (.offer records
                    (str "WARM_DEV "
                         (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                                  :candidate-commit candidate-commit
                                  :candidate-tree candidate-tree
                                  :authority :non-authoritative})))
            {:read-line! (fn []
                           (if (= 1 (swap! reads inc))
                             (.poll records 2 java.util.concurrent.TimeUnit/SECONDS)
                             (do
                               (try (Thread/sleep 1000)
                                    (catch InterruptedException _ nil))
                               nil)))
             :write-line! (fn [_] true)
             :process-alive? (fn [] (not @closed?))
             :close! (fn [] (reset! closed? true))}))
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*broker-release!*
                  (fn [lease]
                    (broker/release! lease)
                    (swap! released inc))
                  lanes/*warm-session-start!* start-fn
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (lanes/run-fast! (fast-plan-with-timeout 25)))]
    (is (= :failed (:status result)))
    (is (some #{"UEP-WARM-TIMEOUT"}
              (map :id (:diagnostics result))))
    (is (= 1 (count @starts)))
    (Thread/sleep 100)
    (is (= 1 @released))))

(deftest fresh-adapter-uses-isolated-command-environment-and-write-once-seam
  (let [requests (atom [])
        writes (atom [])
        cleaned (atom 0)
        exported-root (atom nil)
        result
        (binding [lanes/*fresh-exporter*
                  (fn [plan-value]
                    (let [export (production-fresh-export plan-value)
                          cleanup (:cleanup export)]
                      (reset! exported-root
                              (.toRealPath ^java.nio.file.Path (:directory export)
                                           (make-array java.nio.file.LinkOption 0)))
                      (assoc export :cleanup #(do (cleanup)
                                                  (swap! cleaned inc)))))
                  lanes/*process-runner*
                  (fn [request]
                    (swap! requests conj request)
                    (clean-process-result (pr-str (contract-valid-c6-receipt))))
                  lanes/*closure-snapshot* (constantly {:identity :stable})
                  lanes/*fresh-receipt-writer*
                  (fn [directory p child]
                    (swap! writes conj [directory (:plan-id p) child])
                    {:write-once? true})
                  lanes/*coordination-root* @coordination-root]
          (lanes/run-fresh! (fresh-plan)))]
    (is (= :passed (:status result)))
    (is (= test-commit (get-in result [:artifacts 0 :candidate-commit])))
    (is (= test-tree (get-in result [:artifacts 0 :candidate-tree])))
    (is (re-matches #"sha256:[0-9a-f]{64}"
                    (get-in result [:artifacts 0 :export-closure-id])))
    (is (= 1 (count @requests)))
    (is (= 1 (count @writes)))
    (is (= 1 @cleaned))
    (let [{:keys [command directory environment]} (first @requests)]
      (is (= @exported-root directory))
      (is (= "c6-core" (last command)))
      (is (= "forbidden" (get environment "GRAVITY_UNIFIED_CACHE")))
      (is (= "1" (get environment "GRAVITY_UNIFIED_NO_RESUME")))
      (is (not-any? #{"sh07-authoritative"} command)))))

(deftest fresh-adapter-binds-exact-repository-and-export-identities
  (let [identities (atom [{:candidate-commit test-commit
                           :candidate-tree test-tree
                           :clean? true}
                          {:candidate-commit test-commit
                           :candidate-tree (apply str (repeat 40 "c"))
                           :clean? true}])
        result
        (binding [lanes/*repository-identity*
                  (fn [_]
                    (let [identity (first @identities)]
                      (swap! identities next)
                      identity))
                  lanes/*fresh-exporter* production-fresh-export
                  lanes/*process-runner*
                  (fn [_]
                    (clean-process-result (pr-str (contract-valid-c6-receipt))))
                  lanes/*closure-snapshot* (constantly {:identity :stable})
                  lanes/*fresh-receipt-writer* (fn [& _] {:write-once? true})
                  lanes/*coordination-root* @coordination-root]
          (lanes/run-fresh! (fresh-plan)))]
    (is (= :failed (:status result)))
    (is (= "UEP-FRESH-REPOSITORY-MUTATED"
           (get-in result [:diagnostics 0 :id])))))

(deftest fresh-admission-rejects-dirty-identity-before-export
  (let [exported? (atom false)
        processed? (atom false)
        written? (atom false)
        result
        (binding [lanes/*repository-identity*
                  (constantly {:candidate-commit test-commit
                               :candidate-tree test-tree
                               :clean? false})
                  lanes/*fresh-exporter* (fn [_] (reset! exported? true))
                  lanes/*process-runner* (fn [_] (reset! processed? true))
                  lanes/*fresh-receipt-writer* (fn [& _] (reset! written? true))
                  lanes/*coordination-root* @coordination-root]
          (lanes/run-fresh! (fresh-plan)))]
    (is (= :failed (:status result)))
    (is (= "UEP-WARM-REPOSITORY-IDENTITY"
           (get-in result [:diagnostics 0 :id])))
    (is (not-any? true? [@exported? @processed? @written?]))))

(deftest fresh-resource-admission-and-release-fail-closed
  (testing "missing coordination root touches no broker or process"
    (let [called? (atom false)
          result (binding [lanes/*coordination-root* nil
                           lanes/*broker-acquire!* (fn [& _]
                                                    (reset! called? true))
                           lanes/*process-runner* (fn [_]
                                                   (reset! called? true))]
                   (lanes/run-fresh! (fresh-plan)))]
      (is (= :blocked (:status result)))
      (is (= :forbidden (:cache-decision result)))
      (is (false? @called?))))
  (testing "forged lease never reaches export"
    (let [exported? (atom false)
          result (binding [lanes/*coordination-root* @coordination-root
                           lanes/*broker-acquire!* (fn [& _] {:forged true})
                           lanes/*fresh-exporter* (fn [_]
                                                   (reset! exported? true))]
                   (lanes/run-fresh! (fresh-plan)))]
      (is (= :failed (:status result)))
      (is (= "UEP-RESOURCE-LEASE-INVALID"
             (get-in result [:diagnostics 0 :id])))
      (is (false? @exported?))))
  (testing "release failure cannot preserve a passing result"
    (let [result
          (binding [lanes/*coordination-root* @coordination-root
                    lanes/*fresh-exporter* production-fresh-export
                    lanes/*process-runner*
                    (fn [_]
                      (clean-process-result
                       (pr-str (contract-valid-c6-receipt))))
                    lanes/*closure-snapshot* (constantly {:identity :stable})
                    lanes/*fresh-receipt-writer* (fn [& _]
                                                   {:write-once? true})
                    lanes/*broker-release!*
                    (fn [lease]
                      (broker/release! lease)
                      (throw (ex-info "release failed" {})))]
            (lanes/run-fresh! (fresh-plan)))]
      (is (= :failed (:status result)))
      (is (= "UEP-RESOURCE-RELEASE"
             (get-in result [:diagnostics 0 :id]))))))

(deftest bounded-warm-line-reader-rejects-before-unbounded-allocation
  (let [read-line! (private-lane-fn 'read-bounded-line!)
        input (java.io.InputStreamReader.
               (java.io.ByteArrayInputStream.
                (.getBytes "123456\n" java.nio.charset.StandardCharsets/UTF_8))
               java.nio.charset.StandardCharsets/UTF_8)]
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"exceeds its bound"
                          (read-line! input 5)))
    (let [maximum (var-get
                   (ns-resolve 'gravity.self-hosting.execution-lanes
                               'warm-maximum-response-line-length))
          noisy (apply str (repeat (* 256 1024) \newline))
          line (str "WARM_DEV " (pr-str {:stdout noisy :stderr noisy}))
          reader (java.io.InputStreamReader.
                  (java.io.ByteArrayInputStream.
                   (.getBytes (str line "\n")
                              java.nio.charset.StandardCharsets/UTF_8))
                  java.nio.charset.StandardCharsets/UTF_8)]
      (is (<= (count line) maximum))
      (is (= line (read-line! reader maximum))))))

(deftest dead-warm-worker-is-not-reused
  (let [starts (atom 0)
        start-fn
        (fn [{:keys [candidate-commit candidate-tree]}]
          (swap! starts inc)
          (let [records (java.util.concurrent.ArrayBlockingQueue. 4)
                alive? (atom true)
                reads (atom 0)]
            (.offer records
                    (str "WARM_DEV "
                         (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                                  :candidate-commit candidate-commit
                                  :candidate-tree candidate-tree
                                  :authority :non-authoritative})))
            {:read-line! (fn []
                           (let [value (.poll records 2
                                              java.util.concurrent.TimeUnit/SECONDS)]
                             (when (= 2 (swap! reads inc))
                               (reset! alive? false))
                             value))
             :write-line!
             (fn [line]
               (let [request (edn/read-string line)]
                 (.offer records
                         (str "WARM_DEV "
                              (pr-str
                               {:schema :gravity/exact-tree-warm-worker-response-v1
                                :authority :non-authoritative
                                :authoritative? false
                                :request-id (:request-id request)
                                :candidate-commit candidate-commit
                                :candidate-tree candidate-tree
                                :outcome :passed :exit-code 0 :elapsed-ns 1
                                :request-count 1 :stdout "" :stderr ""
                                :output-truncated? false})))))
             :process-alive? #(boolean @alive?)
             :close! #(reset! alive? false)}))
        results
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!* start-fn
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          [(lanes/run-fast! (fast-plan))
           (lanes/run-fast! (fast-plan))])]
    (is (= [:passed :passed] (mapv :status results)))
    (is (= 2 @starts))))

(deftest warm-session-does-not-share-across-distinct-request-deadlines
  (let [calls (atom 0)
        starts (atom [])
        start-fn (fake-warm-start
                  starts
                  (fn [_]
                    (when (= 2 (swap! calls inc)) (Thread/sleep 100))
                    :passed))
        results
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!* start-fn
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          [(lanes/run-fast! (fast-plan-with-timeout 25))
           (lanes/run-fast! (fast-plan-with-timeout 250))])]
    (is (= [:passed :passed] (mapv :status results)))
    (is (= 2 (count @starts)))))

(deftest repeated-fresh-receipts-are-append-only-and-replayable
  (let [writer (private-lane-fn 'write-once-default)
        plan-value (fresh-plan)
        result {:status :passed
                :artifacts
                [{:artifact :gravity/sh07-authoritative-child-reference
                  :child-receipt-id (sha "d")
                  :candidate-commit test-commit
                  :candidate-tree test-tree
                  :export-closure-id (sha "e")
                  :module-source-bindings [{:module "c6-core"}]}]}
        first-receipt (writer nil plan-value result)
        second-receipt (writer nil plan-value result)]
    (try
      (is (not= (:path first-receipt) (:path second-receipt)))
      (is (= (:receipt-id first-receipt) (:receipt-id second-receipt)))
      (is (every? true? (map :write-once?
                             [first-receipt second-receipt])))
      (is (every? git-ignored? (map :path [first-receipt second-receipt])))
      (finally
        (doseq [receipt [first-receipt second-receipt]]
          (java.nio.file.Files/deleteIfExists
          (java.nio.file.Paths/get ^String (:path receipt)
                                    (make-array String 0))))))))

(deftest fresh-lane-is-independently-replayable-with-distinct-exports
  (let [executed-roots (atom [])
        receipt-roots (atom [])
        run-once
        (fn []
          (binding [lanes/*coordination-root* @coordination-root
                    lanes/*fresh-exporter* production-fresh-export
                    lanes/*process-runner*
                    (fn [{:keys [directory]}]
                      (swap! executed-roots conj (str directory))
                      (clean-process-result
                       (pr-str (contract-valid-c6-receipt))))
                    lanes/*closure-snapshot* (constantly {:identity :stable})
                    lanes/*fresh-receipt-writer*
                    (fn [directory _ _]
                      (swap! receipt-roots conj (str directory))
                      {:write-once? true})]
            (lanes/run-fresh! (fresh-plan))))
        results [(run-once) (run-once)]]
    (is (= [:passed :passed] (mapv :status results)))
    (is (every? true? (map :fresh-replayable? results)))
    (is (= 2 (count (distinct @executed-roots))))
    (is (= @executed-roots @receipt-roots))))

(deftest fresh-export-rejects-canonical-parent-alias-of-source
  (let [source (.toRealPath
                (.toPath (java.io.File. (System/getProperty "user.dir")))
                (make-array java.nio.file.LinkOption 0))
        root (java.nio.file.Files/createTempDirectory
              "uep-alias-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        link (.resolve root "parent-alias")
        aliased-source (.resolve link (str (.getFileName source)))
        validate! (private-lane-fn 'validate-isolated-export!)]
    (try
      (java.nio.file.Files/createSymbolicLink
       link (.getParent source)
       (make-array java.nio.file.attribute.FileAttribute 0))
      (let [error (try
                    (validate! aliased-source (fresh-plan) {})
                    nil
                    (catch clojure.lang.ExceptionInfo value value))]
        (is (= "UEP-FRESH-EXPORT-ISOLATION" (:id (ex-data error)))))
      (finally
        (java.nio.file.Files/deleteIfExists link)
        (java.nio.file.Files/deleteIfExists root)))))

(deftest post-child-export-snapshot-enforces-byte-bound-before-hashing
  (let [root (java.nio.file.Files/createTempDirectory
              "uep-export-bound-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        file (.resolve root "oversized")
        snapshot (private-lane-fn 'export-tree-snapshot)]
    (try
      (with-open [channel
                  (java.nio.file.Files/newByteChannel
                   file
                   (into-array java.nio.file.OpenOption
                               [java.nio.file.StandardOpenOption/CREATE_NEW
                                java.nio.file.StandardOpenOption/WRITE]))]
        (.position channel (* 64 1024 1024))
        (.write channel (java.nio.ByteBuffer/wrap (byte-array [1]))))
      (let [error (try (snapshot root) nil
                       (catch clojure.lang.ExceptionInfo value value))]
        (is (= "UEP-FRESH-EXPORT-BOUND" (:id (ex-data error)))))
      (finally
        (delete-tree! root)))))

(deftest coordination-root-is-part-of-warm-session-identity
  (let [second-root (java.nio.file.Files/createTempDirectory
                     "uep-broker-second-"
                     (make-array java.nio.file.attribute.FileAttribute 0))
        starts (atom [])
        start-fn (fake-warm-start starts nil)]
    (try
      (java.nio.file.Files/setPosixFilePermissions
       second-root
       (java.util.Set/of
        java.nio.file.attribute.PosixFilePermission/OWNER_READ
        java.nio.file.attribute.PosixFilePermission/OWNER_WRITE
        java.nio.file.attribute.PosixFilePermission/OWNER_EXECUTE))
      (binding [lanes/*warm-session-start!* start-fn
                lanes/*closure-snapshot* (constantly {:identity :stable})]
        (is (= :passed
               (:status (binding [lanes/*coordination-root* @coordination-root]
                          (lanes/run-fast! (fast-plan))))))
        (is (= :passed
               (:status (binding [lanes/*coordination-root* (str second-root)]
                          (lanes/run-fast! (fast-plan)))))))
      (is (= 2 (count @starts)))
      (finally
        (lanes/reset-warm-sessions!)
        (delete-tree! second-root)))))

(deftest canonical-authoritative-validator-rejects-untrusted-receipts
  (let [fabricated-module {:module "c6-core"}]
   (doseq [[label requested result source-root]
          [["nil export root" "c6-core"
            (receipt-envelope [fabricated-module]) nil]
           ["unknown module" "not-a-module"
            (receipt-envelope [fabricated-module]) "/private/tmp"]
           ["extra top-level key" "c6-core"
            (assoc (receipt-envelope [fabricated-module]) :extra true)
            "/private/tmp"]
           ["extra selected module" "c6-core"
            (receipt-envelope [fabricated-module fabricated-module])
            (System/getProperty "user.dir")]
           ["fabricated selected module" "c6-core"
            (receipt-envelope [fabricated-module])
            (System/getProperty "user.dir")]]]
    (let [error (try
                  (authoritative/validate-authoritative-run-result!
                   requested result source-root)
                  nil
                  (catch clojure.lang.ExceptionInfo value value))]
      (is (some? error) label)
      (is (= "SH07-AUTHORITATIVE-RECEIPT" (:id (ex-data error))) label)))))

(deftest canonical-authoritative-validator-cross-links-nested-receipt
  (let [root (System/getProperty "user.dir")
        valid (contract-valid-c6-receipt)
        invalid
        [(assoc-in valid [:modules 0 :proof-transaction] {})
         (assoc-in valid [:modules 0 :artifact-id] (sha "b"))
         (assoc-in valid [:modules 0 :coverage-census :source-revision-id]
                   (sha "c"))]]
    (is (= valid
           (authoritative/validate-authoritative-run-result!
            "c6-core" valid root)))
    (doseq [receipt invalid]
      (is (thrown? clojure.lang.ExceptionInfo
                   (authoritative/validate-authoritative-run-result!
                    "c6-core" receipt root))))
    (is (thrown? clojure.lang.ExceptionInfo
                 (authoritative/validate-authoritative-run-result!
                  "all" valid root)))))

(deftest exported-authoritative-source-is-bound-to-contract-bytes
  (let [root (java.nio.file.Files/createTempDirectory
              "uep-source-binding-"
              (make-array java.nio.file.attribute.FileAttribute 0))
        contract (get (authoritative/module-source-contracts) "c6-core")
        relative (:source-path contract)
        source (.toPath (java.io.File. relative))
        target (.resolve root relative)
        binding-fn (deref
                    (ns-resolve
                     'gravity.self-hosting.sh07-authoritative-runner
                     'exported-source-binding!))]
    (try
      (java.nio.file.Files/createDirectories
       (.getParent target)
       (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/copy
       source target
       (make-array java.nio.file.CopyOption 0))
      (is (= (:source-binding contract) (binding-fn (str root) relative)))
      (java.nio.file.Files/write
       target
       (.getBytes "mutated" java.nio.charset.StandardCharsets/UTF_8)
       (into-array java.nio.file.OpenOption
                   [java.nio.file.StandardOpenOption/APPEND]))
      (is (not= (:source-binding contract) (binding-fn (str root) relative)))
      (finally
        (with-open [paths (java.nio.file.Files/walk
                           root
                           (make-array java.nio.file.FileVisitOption 0))]
          (.forEach
           (.sorted paths (java.util.Comparator/reverseOrder))
           (reify java.util.function.Consumer
             (accept [_ path]
               (java.nio.file.Files/deleteIfExists path)))))))))

(deftest fresh-adapter-rejects-malformed-output-cache-reuse-and-mutation
  (let [writes (atom 0)
        exporter production-fresh-export]
    (testing "malformed child output fails closed"
      (let [result
            (binding [lanes/*fresh-exporter* exporter
                      lanes/*process-runner* (fn [_] (clean-process-result "not-edn"))
                      lanes/*closure-snapshot* (constantly {:identity :stable})
                      lanes/*fresh-receipt-writer* (fn [& _] (swap! writes inc))
                      lanes/*coordination-root* @coordination-root]
              (lanes/run-fresh! (fresh-plan)))]
        (is (= :failed (:status result)))
        (is (some #{"UEP-LANE-MALFORMED-OUTPUT"}
                  (map :id (:diagnostics result))))))
    (testing "source mutation fails closed"
      (let [states (atom [{:identity :before} {:identity :after}])
            result
            (binding [lanes/*fresh-exporter* exporter
                      lanes/*process-runner* (fn [_] (clean-process-result (fresh-output)))
                      lanes/*closure-snapshot* (fn [_] (let [value (first @states)]
                                                         (swap! states next)
                                                         value))
                      lanes/*fresh-receipt-writer* (fn [& _] (swap! writes inc))
                      lanes/*coordination-root* @coordination-root]
              (lanes/run-fresh! (fresh-plan)))]
        (is (= :failed (:status result)))
        (is (true? (:mutation-detected? result)))))
    (testing "cache reuse cannot be reported as fresh"
      (let [result
            (binding [lanes/*fresh-exporter* exporter
                      lanes/*process-runner* (fn [_]
                                               (clean-process-result
                                                (fresh-output)
                                                {:cache-decision :hit}))
                      lanes/*closure-snapshot* (constantly {:identity :stable})
                      lanes/*fresh-receipt-writer* (fn [& _] (swap! writes inc))
                      lanes/*coordination-root* @coordination-root]
              (lanes/run-fresh! (fresh-plan)))]
        ;; The adapter owns the fresh decision regardless of a malicious child
        ;; field; the receipt validator still requires :forbidden.
        (is (= :forbidden (:cache-decision result)))))
    (is (zero? @writes)
        "no invalid or mutated fresh attempt may consume a durable receipt identity")))

(deftest default-process-supervisor-bounds-timeout-and-cleans-tree
  (let [runner (deref (var gravity.self-hosting.execution-lanes/run-process-default))
        result (runner {:command ["sh" "-c" "sleep 2"]
                        :directory (.toPath (java.io.File. "."))
                        :environment {"PATH" (System/getenv "PATH")}
                        :timeout-ms 25
                        :max-output-bytes 128})]
    (is (= 124 (:exit-code result)))
    (is (true? (:timed-out? result)))
    (is (true? (get-in result [:cleanup :complete?])))))
