(ns gravity.self-hosting.execution-lanes-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [gravity.bootstrap :as bootstrap]
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

(defn- request-runner-result
  [request outcome]
  (let [namespace (second
                   (drop-while #(not= "--namespace" %)
                               (:args request)))
        selectors (->> (partition-all 2 (:args request))
                       (keep (fn [[option value]]
                               (when (= "--exact" option) value)))
                       sort
                       vec)
        passed? (= :passed outcome)]
    {:schema :gravity/development-test-result-v1
     :authority :non-authoritative
     :authoritative? false
     :selection {:namespaces [namespace]
                 :test-vars (mapv #(str namespace "/" %) selectors)
                 :fail-fast? true}
     :summary {:test (count selectors)
               :pass (if passed? (count selectors) 0)
               :fail (if passed? 0 1)
               :error 0}
     :outcome (if passed? :passed :failed)
     :exit-code (if passed? 0 1)}))

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
                            response {:schema :gravity/exact-tree-warm-worker-response-v2
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
                                      :output-truncated? false
                                      :runner-result
                                      (request-runner-result request outcome)}]
                        (.offer records (str "WARM_DEV " (pr-str response)))))
       :process-alive? (fn [] (not @closed?))
       :close! (fn [] (reset! closed? true))})))

(def ^:private cached-route-ids
  ["sh07-cached-accepted" "sh07-cached-contract" "sh07-cached-identity"
   "sh07-cached-rejected" "sh07-cached-verification"])

(defn- cached-plan
  [route-id]
  (plan/plan {:route-id route-id :lane :fast}))

(defn- cached-context
  []
  {:request-schema-version 15
   :adapter bootstrap/sh07-core-adapter-contract
   :checked-core-source-content-hash
   bootstrap/sh07-core-expected-source-content-hash
   :checked-core-plan-semantic-hash
   bootstrap/sh07-core-expected-plan-semantic-hash
   :java-runtime-version (System/getProperty "java.runtime.version")
   :clojure-version (clojure-version)})

(defn- cached-result
  [plan-value options]
  (let [shard (get-in plan-value [:executor :request :shard])
        test-vars (count (get-in plan-value [:selection :selectors]))
        options (merge {:test test-vars :pass test-vars :fail 0 :error 0
                        :cache {:sh06-hits 0 :sh06-misses 1
                                :core-hits 0 :core-misses 1
                                :verification-hits 0 :verification-misses 1}}
                       options)]
    {:test (:test options)
     :pass (:pass options)
     :fail (:fail options)
     :error (:error options)
     :artifact :gravity/sh07-cached-shard-result
     :shard shard
     :test-vars test-vars
     :cache-authoritative? false
     :fresh-authoritative-run-required? true
     :cache-context (cached-context)
     :elapsed-ms 7
     :cache (:cache options)}))

(defn- cached-response
  [plan-value raw options]
  (let [request-id (or (:request-id options) "cached")
        outcome (or (:outcome options) :passed)
        exit-code (or (:exit-code options) (if (= :passed outcome) 0 2))
        request (get-in plan-value [:executor :request])]
    {:schema :gravity/exact-tree-warm-worker-cached-response-v1
     :authority :non-authoritative
     :authoritative? false
     :request-id request-id
     :candidate-commit test-commit
     :candidate-tree test-tree
     :lane :fast
     :route-id (:route-id plan-value)
     :plan-id (:plan-id plan-value)
     :catalog-id (:catalog-id plan-value)
     :closure-identity (:closure-identity plan-value)
     :result-contract-id (:result-contract-id request)
     :shard (:shard request)
     :outcome outcome
     :exit-code exit-code
     :elapsed-ns 1
     :request-count 1
     :runner-result raw
     :stdout (or (:stdout options) "")
     :stderr (or (:stderr options) "")
     :output-truncated? (boolean (:output-truncated? options))}))

(defn- fake-cached-warm-start
  "A bounded duplex seam for cached-lane tests.

  It never invokes a runner or cache. Responses are synthesized from the
  request's declarative route and shard so tests exercise only dispatch,
  authentication, projection, and lifecycle behavior."
  [starts requests response-options]
  (fn [{:keys [candidate-commit candidate-tree] :as start-request}]
    (swap! starts conj start-request)
    (let [records (java.util.concurrent.ArrayBlockingQueue. 16)
          calls (atom 0)
          closed? (atom false)
          response-for (or response-options (fn [_ _] {}))]
      (.offer records
              (str "WARM_DEV "
                   (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                            :candidate-commit candidate-commit
                            :candidate-tree candidate-tree
                            :authority :non-authoritative})))
      {:read-line! (fn [] (.poll records 2 java.util.concurrent.TimeUnit/SECONDS))
       :write-line! (fn [line]
                      (let [request (edn/read-string line)
                            call (swap! calls inc)
                            options (let [value (response-for request call)]
                                      (if (map? value) value {:outcome value}))
                            cached? (= :run-cached-shard (:operation request))
                            p (when cached? (cached-plan (:route-id request)))
                            raw (cond
                                  (contains? options :runner-result)
                                  (:runner-result options)
                                  cached?
                                  (cached-result p (or (:raw-options options) {}))
                                  :else nil)
                            outcome (or (:outcome options)
                                         (if cached?
                                           (if (and raw (zero? (+ (:fail raw)
                                                                  (:error raw))))
                                             :passed
                                             :failed)
                                           :passed))
                            exit-code (or (:exit-code options)
                                          (case outcome
                                            :passed 0
                                            :failed (if raw 1 2)
                                            2))
                            response (if cached?
                                       {:schema :gravity/exact-tree-warm-worker-cached-response-v1
                                        :authority :non-authoritative
                                        :authoritative? false
                                        :request-id (:request-id request)
                                        :candidate-commit candidate-commit
                                        :candidate-tree candidate-tree
                                        :lane (:lane request)
                                        :route-id (:route-id request)
                                        :plan-id (:plan-id request)
                                        :catalog-id (:catalog-id request)
                                        :closure-identity (:closure-identity request)
                                        :result-contract-id (:result-contract-id request)
                                        :shard (:shard request)
                                        :outcome outcome :exit-code exit-code
                                        :elapsed-ns 1 :request-count call
                                        :runner-result raw
                                        :stdout (or (:stdout options) "")
                                        :stderr (or (:stderr options) "")
                                        :output-truncated? (boolean (:output-truncated? options))}
                                       {:schema :gravity/exact-tree-warm-worker-response-v2
                                        :authority :non-authoritative
                                        :authoritative? false
                                        :request-id (:request-id request)
                                        :candidate-commit candidate-commit
                                        :candidate-tree candidate-tree
                                        :outcome outcome :exit-code exit-code
                                        :elapsed-ns 1 :request-count call
                                        :stdout (or (:stdout options) "")
                                        :stderr (or (:stderr options) "")
                                        :output-truncated? (boolean (:output-truncated? options))
                                        :runner-result (request-runner-result request outcome)})]
                        (swap! requests conj request)
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

(def ^:private artifact-row-id
  "sh07-b15/keyword-lookup-zero-arity")

(defn- artifact-test-plan []
  (plan/plan {:route-id "sh07-conformance-table" :lane :fast}))

(defn- artifact-test-binding [plan-value]
  (plan/fixture-source-binding plan-value artifact-row-id :source))

(defn- artifact-projection-for
  [plan-value binding options]
  (let [stage-id (or (:selected-artifact-id options) (sha "a"))
        provenance {:actual-path
                    (str (.toRealPath
                          (.resolve ((private-lane-fn 'repository-root))
                                    ^String (:gravity-path binding))
                          (make-array java.nio.file.LinkOption 0)))
                    :project-root-path (str ((private-lane-fn 'repository-root)))
                    :project-relative-path (:gravity-path binding)
                    :byte-count (:fixture-bytes binding)
                    :content-hash (:fixture-sha256 binding)}
        projection (merge
                    {:schema :gravity/authenticated-artifact-fast-projection-v1
                     :binding-id (:binding-id binding)
                     :pipeline :gravity/authenticated-compiler-artifact-pipeline-v1
                     :result-id (sha "d")
                     :requested-stage :source
                     :selected-artifact-id stage-id
                     :stage-artifact-ids [[:source stage-id []]]
                     :source-content-hash (:fixture-sha256 binding)
                     :source-provenance-id nil
                     :source-provenance provenance
                     :source-relative-path (:gravity-path binding)
                     :cache-decision :miss
                     :execution-boundary {:mode :fast :isolation :same-jvm
                                          :isolated? false
                                          :acceptance-authority? false
                                          :fresh-authoritative-run-required? true}
                     :authority {:authoritative? false
                                 :ceiling :non-authoritative
                                 :development-only? true
                                 :nonclaims (:nonclaims plan-value)}
                     :mutation-detected? false
                     :provenance-checked? true
                     :source-authenticated? true
                     :closure-verified? true
                     :fresh-replayable? false
                     :diagnostics []}
                    (select-keys options [:selected-artifact-id :projection-id
                                          :stage-artifact-ids]))
        projection (assoc projection :source-provenance-id
                           (bootstrap/reader-canonical-hash
                            (:source-provenance projection)))]
    (assoc projection :projection-id
           (or (:projection-id options)
               (bootstrap/reader-canonical-hash
                (select-keys projection
                             [:schema :binding-id :pipeline :result-id :requested-stage
                              :selected-artifact-id :stage-artifact-ids :source-content-hash
                              :source-provenance-id :source-provenance :execution-boundary
                              :authority :mutation-detected? :provenance-checked?
                              :source-authenticated? :closure-verified? :fresh-replayable?
                              :diagnostics]))))))

(defn- fake-artifact-warm-start
  [starts requests response-options]
  (fn [{:keys [candidate-commit candidate-tree] :as start-request}]
    (swap! starts conj start-request)
    (let [records (java.util.concurrent.ArrayBlockingQueue. 16)
          calls (atom 0)
          closed? (atom false)]
      (.offer records
              (str "WARM_DEV "
                   (pr-str {:schema :gravity/exact-tree-warm-worker-ready-v1
                            :candidate-commit candidate-commit
                            :candidate-tree candidate-tree
                            :authority :non-authoritative})))
      {:read-line! (fn [] (.poll records 2 java.util.concurrent.TimeUnit/SECONDS))
       :write-line! (fn [line]
                      (let [request (edn/read-string line)
                            call (swap! calls inc)
                            p (artifact-test-plan)
                            b (artifact-test-binding p)
                            options (merge (if (fn? response-options)
                                             (response-options request call)
                                             response-options)
                                           {})
                            projection (artifact-projection-for p b options)
                            response (merge
                                      {:schema :gravity/exact-tree-warm-worker-artifact-response-v1
                                       :authority :non-authoritative
                                       :authoritative? false
                                       :request-id (:request-id request)
                                       :candidate-commit candidate-commit
                                       :candidate-tree candidate-tree
                                       :lane :fast
                                       :route-id (:route-id request)
                                       :plan-id (:plan-id request)
                                       :catalog-id (:catalog-id request)
                                       :closure-identity (:closure-identity request)
                                       :binding-id (:binding-id request)
                                       :row-id (:row-id request)
                                       :stage (:stage request)
                                       :outcome :passed :exit-code 0
                                       :elapsed-ns 1 :request-count call
                                       :projection projection
                                       :stdout "" :stderr ""
                                       :output-truncated? false}
                                      (select-keys options
                                                   [:outcome :exit-code :stdout :stderr
                                                    :output-truncated? :plan-id :catalog-id
                                                    :closure-identity :binding-id :row-id
                                                    :stage]))]
                        (swap! requests conj request)
                        (.offer records (str "WARM_DEV " (pr-str response)))))
       :process-alive? (fn [] (not @closed?))
       :close! (fn [] (reset! closed? true))})))

(defn- git-ignored? [path]
  (let [process (.start (ProcessBuilder. ^java.util.List
                                         ["git" "check-ignore" "--quiet"
                                          (str path)]))]
    (zero? (.waitFor process))))

(deftest run-fast-artifact-direct-transport-is-closed-and-non-authoritative
  (let [starts (atom [])
        requests (atom [])
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*repository-identity* (constantly
                                               {:candidate-commit test-commit
                                                :candidate-tree test-tree
                                                :clean? true})
                  lanes/*warm-session-start!*
                  (fake-artifact-warm-start starts requests {})
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (lanes/run-fast-artifact! (artifact-test-plan)
                                    artifact-row-id :source))]
    (is (= :passed (:status result)))
    (is (= 1 (count @starts)))
    (is (= 1 (count @requests)))
    (is (= :run-fast-artifact (:operation (first @requests))))
    (is (not (contains? (first @requests) :source-path)))
    (is (not (contains? (first @requests) :args)))
    (is (= #{:authoritative? :ceiling :development-only? :nonclaims}
           (set (keys (:authority result)))))
    (is (false? (get-in result [:authority :authoritative?])))
    (is (= :non-authoritative (get-in result [:authority :ceiling])))
    (is (= [:integration :proof :release :self-hosting :seed-retirement :safety :performance]
           (get-in result [:authority :nonclaims])))
    (is (= #{:mode :isolation :isolated? :acceptance-authority?
             :fresh-authoritative-run-required?}
           (set (keys (:execution-boundary result)))))
    (is (true? (get-in result [:execution-boundary :fresh-authoritative-run-required?])))
    (is (#{:hit :miss} (:cache-decision result)))
    (is (= :source (get-in result [:projection :requested-stage])))
    (is (true? (get-in result [:projection :provenance-checked?])))
    (let [reference (:artifact-reference result)]
      (is (= #{:schema :reference-id :operation :base-plan-id :binding-id
               :row-id :stage :projection-id :result-id :selected-artifact-id
               :source-content-hash :source-provenance-id
               :source-relative-path :source-provenance :cache-decision
               :execution-boundary :authority :status}
             (set (keys reference))))
      ;; Diagnostics remain one canonical stream on the execution result; the
      ;; compact reference carries only lineage/provenance identity.
      (is (not (contains? reference :diagnostics)))
      (is (= reference (lanes/verify-artifact-reference reference)))
      (doseq [[expected tampered]
              [["UEP-ARTIFACT-REFERENCE-ID"
                (assoc reference :reference-id (sha "d"))]
               ["UEP-ARTIFACT-REFERENCE"
                (let [value (assoc reference :row-id (apply str (repeat 257 "x")))]
                  (assoc value :reference-id
                         (lanes/artifact-reference-id
                          (dissoc value :reference-id))))]
               ["UEP-ARTIFACT-REFERENCE"
                (let [value (assoc reference :source-relative-path "../escape")]
                  (assoc value :reference-id
                         (lanes/artifact-reference-id
                          (dissoc value :reference-id))))]]]
        (is (= expected
               (try (lanes/verify-artifact-reference tampered)
                    nil
                    (catch clojure.lang.ExceptionInfo error
                      (:id (ex-data error))))))))))

(deftest run-fast-artifact-rejects-tampered-projection-and-identity
  (doseq [[label options expected]
           [["projection-id" {:projection-id (sha "z")} "UEP-WARM-ARTIFACT-PROJECTION"]
           ["stage-ancestry" {:stage-artifact-ids [[:source (sha "b") []]]}
            "UEP-WARM-ARTIFACT-PROJECTION"]
           ["plan-id" {:plan-id (sha "z")} "UEP-WARM-ARTIFACT-RESPONSE"]
           ["truncated" {:output-truncated? true} "UEP-WARM-ARTIFACT-RESPONSE"]
           ["failed-with-projection" {:outcome :failed :exit-code 2}
            "UEP-WARM-ARTIFACT-RESULT"]]]
    (testing label
      (let [starts (atom [])
            requests (atom [])
            result
            (binding [lanes/*coordination-root* @coordination-root
                      lanes/*repository-identity* (constantly
                                                   {:candidate-commit test-commit
                                                    :candidate-tree test-tree
                                                    :clean? true})
                      lanes/*warm-session-start!*
                      (fake-artifact-warm-start starts requests options)
                      lanes/*closure-snapshot* (constantly {:identity :stable})]
              (lanes/run-fast-artifact! (artifact-test-plan)
                                        artifact-row-id :source))]
        (lanes/reset-warm-sessions!)
        (is (= :failed (:status result)))
        (is (some #(= expected (:id %)) (:diagnostics result)))))))

(deftest run-fast-artifact-fails-closed-for-lane-cache-and-resource-policy
  (testing "fresh lane is not accepted by the fast artifact transport"
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"fast lane"
         (lanes/run-fast-artifact!
          (plan/plan {:route-id "sh07-authoritative" :lane :fresh})
          artifact-row-id :source))))
  (testing "cache-disabled plans are rejected"
    (let [p (artifact-test-plan)
          altered (assoc-in p [:cache-policy :mode] :disabled)
          identity-of (deref (ns-resolve 'gravity.self-hosting.unified-execution-plan
                                         'identity-of))
          altered (assoc altered :plan-id
                         (identity-of (dissoc altered :plan-id)))]
      (is (thrown-with-msg?
           clojure.lang.ExceptionInfo #"cache eligibility"
           (lanes/run-fast-artifact! altered artifact-row-id :source)))))
  (testing "invalid explicit resource context blocks before startup"
    (let [starts (atom [])
          p (artifact-test-plan)]
      (with-redefs [plan/*resource-lease* {:bounded-executor? false
                                           :timeout-enforced? false
                                           :deadline-ns 0}]
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-artifact-warm-start starts (atom []) {})]
          (let [result (lanes/run-fast-artifact! p artifact-row-id :source)]
            (is (= :blocked (:status result)))
            (is (= "UEP-RESOURCE-BOUNDED-CONTEXT-REQUIRED"
                   (-> result :diagnostics first :id)))
            (is (empty? @starts))))))))

(deftest run-fast-artifact-validates-row-and-stage-before-plan-or-binding
  (let [p (artifact-test-plan)]
    (doseq [[row stage expected]
            [["" :source "UEP-ARTIFACT-ROW"]
             [(apply str (repeat 257 "x")) :source "UEP-ARTIFACT-ROW"]
             [artifact-row-id :unknown "UEP-ARTIFACT-STAGE"]]]
      (testing expected
        (let [error (try (lanes/run-fast-artifact! p row stage)
                         nil
                         (catch clojure.lang.ExceptionInfo error error))]
          (is (= expected (:id (ex-data error)))))))))

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
    (is (= :miss (:cache-decision (first result))))
    (is (= {:test-count 2 :pass 2 :fail 0 :error 0}
           (select-keys (first result) [:test-count :pass :fail :error])))))

(deftest cached-routes-dispatch-from-declarative-plan-and-preserve-raw-result
  (let [starts (atom [])
        requests (atom [])
        results
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-cached-warm-start starts requests nil)
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          (mapv #(lanes/run-fast! (cached-plan %)) cached-route-ids))]
    (is (= (repeat 5 :passed) (map :status results)))
    (is (= 5 (count @starts)))
    (is (= cached-route-ids (mapv :route-id @requests)))
    (is (every? #(= #{:schema :request-id :operation :candidate-commit
                      :candidate-tree :lane :route-id :plan-id :catalog-id
                      :closure-identity :result-contract-id :shard}
                    (set (keys %)))
                @requests))
    (is (every? #(= {:schema :gravity/exact-tree-warm-worker-cached-request-v1
                     :operation :run-cached-shard
                     :lane :fast
                     :result-contract-id :gravity/sh07-cached-shard-result-v1}
                    (select-keys % [:schema :operation :lane
                                    :result-contract-id]))
                @requests))
    (is (= ["accepted" "contract" "identity" "rejected" "verification"]
           (mapv :shard @requests)))
    (is (every? #(= :miss (:cache-decision %)) results))
    (is (every? #(= 1 (count (:artifacts %)))
                (take 2 results)))
    (is (every? #(= #{:schema :result-contract-id :artifact :shard :context
                      :counters :elapsed-ms :cache-authoritative?
                      :fresh-authoritative-run-required?}
                    (set (keys (:cache-observation %))))
                results))
    (is (every? #(= (get-in % [:artifacts 0 :cache-context])
                    (get-in % [:cache-observation :context]))
                results))
    (is (every? #(= (get-in % [:artifacts 0 :cache])
                    (get-in % [:cache-observation :counters]))
                results))
    (is (every? #(= (get-in % [:artifacts 0 :artifact])
                    (get-in % [:cache-observation :artifact]))
                results))
    (is (every? #(= (get-in % [:artifacts 0 :shard])
                    (get-in % [:cache-observation :shard]))
                results))
    (is (every? #(= (get-in % [:artifacts 0 :elapsed-ms])
                    (get-in % [:cache-observation :elapsed-ms]))
                results))
    (is (every? #(= :gravity/sh07-cached-shard-result-v1
                    (get-in % [:cache-observation :result-contract-id]))
                results))
    (is (every? #(false? (get-in % [:cache-observation :cache-authoritative?]))
                results))
    (is (every? #(true? (get-in %
                              [:cache-observation
                               :fresh-authoritative-run-required?]))
                results))
    (is (= [6 2 2 2 4] (mapv :test-count results)))
    (is (= #{:test :pass :fail :error :artifact :shard :test-vars
             :cache-authoritative? :fresh-authoritative-run-required?
             :cache-context :elapsed-ms :cache}
           (set (keys (get-in (first results) [:artifacts 0])))))))

(deftest cached-hit-and-miss-decisions-project-authenticated-counters
  (let [starts (atom []) requests (atom [])
        results
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-cached-warm-start
                   starts requests
                   (fn [request _]
                     (if (= "accepted" (:shard request))
                       {:raw-options {:cache {:sh06-hits 1 :sh06-misses 0
                                               :core-hits 0 :core-misses 1
                                               :verification-hits 0
                                               :verification-misses 1}}}
                       {:raw-options {:cache {:sh06-hits 0 :sh06-misses 1
                                               :core-hits 0 :core-misses 1
                                               :verification-hits 0
                                               :verification-misses 1}}})))
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          [(lanes/run-fast! (cached-plan "sh07-cached-accepted"))
           (lanes/run-fast! (cached-plan "sh07-cached-contract"))])]
    (is (= [:hit :miss] (mapv :cache-decision results)))
    (is (= 2 (count @requests)))
    (is (= 1 (count (:artifacts (first results)))))
    (is (= {:test-count 6 :pass 6 :fail 0 :error 0}
           (select-keys (first results) [:test-count :pass :fail :error])))))

(deftest cached-response-and-raw-contracts-reject-identity-authority-and-bounds
  (let [p (cached-plan "sh07-cached-accepted")
        raw (cached-result p {})
        valid (cached-response p raw {})
        validate (private-lane-fn 'validate-warm-cached-response!)]
    (is (= valid (validate valid {:candidate-commit test-commit
                                  :candidate-tree test-tree}
                                "cached" 1 (:selection p) p)))
    (doseq [[field value expected-id]
            [[:route-id "sh07-cached-contract" "UEP-WARM-CACHED-RESPONSE"]
             [:plan-id (:catalog-id p) "UEP-WARM-CACHED-RESPONSE"]
             [:catalog-id (:plan-id p) "UEP-WARM-CACHED-RESPONSE"]
             [:closure-identity (:catalog-id p) "UEP-WARM-CACHED-RESPONSE"]
             [:result-contract-id :wrong "UEP-WARM-CACHED-RESPONSE"]
             [:shard "wrong" "UEP-WARM-CACHED-RESPONSE"]
             [:authoritative? true "UEP-WARM-CACHED-RESPONSE"]
             [:elapsed-ns 100000000000000000000000000N
              "UEP-WARM-CACHED-RESPONSE"]
             [:output-truncated? true "UEP-WARM-CACHED-RESPONSE"]]]
      (let [error (try
                    (validate (assoc valid field value)
                              {:candidate-commit test-commit
                               :candidate-tree test-tree}
                              "cached" 1 (:selection p) p)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= expected-id (:id (ex-data error))) (str "rejected " field))))
    (doseq [[field value expected-id]
            [[:cache-authoritative? true "UEP-WARM-CACHED-AUTHORITY"]
             [:fresh-authoritative-run-required? false "UEP-WARM-CACHED-AUTHORITY"]
             [:test-vars 1 "UEP-WARM-CACHED-COUNTER"]
             [:elapsed-ms 100000000000000000000000000N
              "UEP-WARM-CACHED-RESULT"]
             [:artifact :other "UEP-WARM-CACHED-RESULT"]]]
      (let [error (try
                    (validate (assoc valid :runner-result (assoc raw field value))
                              {:candidate-commit test-commit
                               :candidate-tree test-tree}
                              "cached" 1 (:selection p) p)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= expected-id (:id (ex-data error))) (str "rejected raw " field))))
    (let [error (try
                  (validate (assoc valid :runner-result
                                   (assoc raw :cache-context
                                          (assoc (cached-context)
                                                 :adapter :wrong)))
                            {:candidate-commit test-commit
                             :candidate-tree test-tree}
                            "cached" 1 (:selection p) p)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "UEP-WARM-CACHED-CONTEXT" (:id (ex-data error)))))))

(deftest cached-response-closed-shapes-and-counter-bounds-fail-closed
  (let [p (cached-plan "sh07-cached-contract")
        raw (cached-result p {})
        valid (cached-response p raw {})
        validate (private-lane-fn 'validate-warm-cached-response!)]
    (doseq [response [(assoc valid :extra true)
                      (dissoc valid :shard)
                      (assoc valid :candidate-commit test-tree)]]
      (is (thrown-with-msg? clojure.lang.ExceptionInfo
                            #"authenticated closed-shape"
                            (validate response
                                      {:candidate-commit test-commit
                                       :candidate-tree test-tree}
                                      "cached" 1 (:selection p) p))))
    (doseq [result [(assoc raw :extra true)
                    (dissoc raw :cache)
                    (assoc-in raw [:cache-context]
                              (assoc (cached-context)
                                     :checked-core-plan-semantic-hash "wrong"))]]
      (let [error (try
                    (validate (assoc valid :runner-result result)
                              {:candidate-commit test-commit
                               :candidate-tree test-tree}
                              "cached" 1 (:selection p) p)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (some? error))
        (is (re-matches #"UEP-WARM-CACHED-(RESULT|COUNTER|CACHE|CONTEXT)"
                        (:id (ex-data error))))))
    (doseq [field [:sh06-hits :sh06-misses :core-hits :core-misses
                   :verification-hits :verification-misses]
            value [-1 1000001 100000000000000000000000000N]]
      (let [error (try
                    (validate (assoc valid :runner-result
                                     (assoc-in raw [:cache field] value))
                              {:candidate-commit test-commit
                               :candidate-tree test-tree}
                              "cached" 1 (:selection p) p)
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "UEP-WARM-CACHED-CACHE" (:id (ex-data error)))
            (str "bounded cache counter " field " value " value))))
    (let [large (apply str (repeat (inc (* 256 1024)) "x"))
          error (try
                  (validate (assoc valid :stdout large)
                            {:candidate-commit test-commit
                             :candidate-tree test-tree}
                            "cached" 1 (:selection p) p)
                  nil
                  (catch clojure.lang.ExceptionInfo error error))]
      (is (= "UEP-WARM-CACHED-RESPONSE" (:id (ex-data error)))))))

(deftest cached-plan-control-failures-propagate-through-fast-admission
  (let [p (cached-plan "sh07-cached-accepted")
        canonical (private-lane-fn 'canonical-plan-for)]
    (testing "causally wrapped interruption restores status and rethrows"
      (try
        (with-redefs [clojure.core/requiring-resolve
                      (fn [_]
                        (throw (ex-info "wrapped" {}
                                        (InterruptedException. "stop"))))]
          (canonical p))
        (is false "expected interruption")
        (catch InterruptedException _
          (is (Thread/interrupted)))))
    (testing "causally wrapped linkage error is never converted"
      (try
        (with-redefs [clojure.core/requiring-resolve
                      (fn [_]
                        (throw (ex-info "wrapped" {}
                                        (LinkageError. "fatal"))))]
          (canonical p))
        (is false "expected linkage error")
        (catch LinkageError error
          (is (= "fatal" (.getMessage error))))))
    (testing "ordinary canonical reconstruction errors fail with a stable id"
      (let [error (try
                    (with-redefs [clojure.core/requiring-resolve
                                  (fn [_]
                                    (throw (IllegalStateException. "missing")))]
                      (canonical p))
                    nil
                    (catch clojure.lang.ExceptionInfo error error))]
        (is (= "UEP-WARM-CACHED-PLAN" (:id (ex-data error))))))
    (testing "run-fast keeps causal cancellation outside failed result"
      (try
        (with-redefs [gravity.self-hosting.execution-lanes/cached-plan-admitted?
                      (fn [_]
                        (throw (ex-info "wrapped" {}
                                        (InterruptedException. "stop"))))]
          (binding [lanes/*coordination-root* @coordination-root]
            (lanes/run-fast! p)))
        (is false "expected interruption")
        (catch InterruptedException _
          (is (Thread/interrupted)))))))

(deftest cached-route-policy-tightening-falls-back-to-generic-exact
  (let [starts (atom []) requests (atom [])
        disabled (plan/plan {:route-id "sh07-cached-accepted"
                             :lane :fast
                             :cache-policy {:mode :disabled}})
        tightened (plan/plan {:route-id "sh07-cached-contract"
                              :lane :fast
                              :resource-policy {:resource-class :memory-heavy
                                                :timeout-ms 1000
                                                :max-output-bytes 262144}})
        results
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-cached-warm-start starts requests nil)
                  lanes/*closure-snapshot* (constantly {:identity :stable})]
          [(lanes/run-fast! disabled) (lanes/run-fast! tightened)])]
    (is (= [:passed :passed] (mapv :status results)))
    (is (= [:run-exact :run-exact] (mapv :operation @requests)))
    (is (every? #(= :gravity/exact-tree-warm-worker-request-v1 (:schema %))
                @requests))
    (is (every? #(not (contains? % :lane)) @requests))
    (is (every? #(= :miss (:cache-decision %)) results))))

(deftest cached-closure-drift-evicts-session-and-releases-lease
  (let [released (atom 0)
        starts (atom [])
        snapshots (atom 0)
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-cached-warm-start starts (atom []) nil)
                  lanes/*broker-release!*
                  (fn [lease]
                    (broker/release! lease)
                    (swap! released inc))
                  lanes/*closure-snapshot*
                  (fn [_]
                    (if (= 1 (swap! snapshots inc))
                      {:identity :before}
                      {:identity :after}))]
          (lanes/run-fast! (cached-plan "sh07-cached-accepted")))]
    (is (= :failed (:status result)))
    (is (= "UEP-SNAPSHOT-MUTATED"
           (get-in result [:diagnostics 0 :id])))
    (is (= 1 (count @starts)))
    (Thread/sleep 100)
    (is (= 1 @released))))

(deftest cached-fast-never-enters-fresh-exporter
  (let [called? (atom false)
        result
        (binding [lanes/*coordination-root* @coordination-root
                  lanes/*warm-session-start!*
                  (fake-cached-warm-start (atom []) (atom []) nil)
                  lanes/*closure-snapshot* (constantly {:identity :stable})
                  lanes/*fresh-exporter*
                  (fn [_]
                    (reset! called? true)
                    (throw (AssertionError. "fresh lane must remain unreachable")))]
          (lanes/run-fast! (cached-plan "sh07-cached-accepted")))]
    (is (= :passed (:status result)))
    (is (false? @called?))))

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
    (is (= 1 (count @starts)))
    (is (= {:test-count 2 :pass 0 :fail 1 :error 0}
           (select-keys result [:test-count :pass :fail :error])))))

(deftest warm-preexecution-failure-keeps-closed-counters
  (let [plan-value (fast-plan)
        result ((private-lane-fn 'warm-request-result)
                plan-value (:selection plan-value)
                {:runner-result nil :outcome :failed :exit-code 2
                 :output-truncated? false}
                true)]
    (is (= {:test-count 2 :pass 0 :fail 0 :error 1}
           (select-keys result [:test-count :pass :fail :error])))
    (is (some #(= "UEP-WARM-TEST-FAILED" (:id %)) (:diagnostics result)))))

(deftest cached-preexecution-failure-carries-no-raw-or-observation
  (let [plan-value (cached-plan "sh07-cached-accepted")
        result ((private-lane-fn 'warm-request-result)
                plan-value (:selection plan-value)
                {:runner-result nil :outcome :failed :exit-code 2
                 :output-truncated? false}
                true true)]
    (is (= [] (:artifacts result)))
    (is (not (contains? result :cache-observation)))
    (is (= {:test-count 6 :pass 0 :fail 0 :error 1}
           (select-keys result [:test-count :pass :fail :error])))))

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

(deftest warm-response-v1-is-rejected
  (let [p (fast-plan)
        s (:selection p)
        response {:schema :gravity/exact-tree-warm-worker-response-v1
                  :authority :non-authoritative :authoritative? false
                  :request-id "one" :candidate-commit test-commit
                  :candidate-tree test-tree :outcome :failed :exit-code 2
                  :elapsed-ns 1 :request-count 1 :stdout "" :stderr ""
                  :output-truncated? false :runner-result nil}]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"authenticated closed-shape"
         ((private-lane-fn 'validate-warm-response!)
          response {:candidate-commit test-commit :candidate-tree test-tree}
          "one" 1 s)))))

(deftest warm-runner-rejects-complete-zero-failure-failures
  (let [p (fast-plan)
        s (:selection p)
        result {:schema :gravity/development-test-result-v1
                :authority :non-authoritative :authoritative? false
                :selection {:namespaces [(:namespace s)]
                            :test-vars (mapv #(str (:namespace s) "/" %) (:selectors s))
                            :fail-fast? true}
                :summary {:test (count (:selectors s)) :pass 0 :fail 0 :error 0}
                :outcome :failed :exit-code 1}]
    (is (thrown-with-msg?
         clojure.lang.ExceptionInfo #"exact closed-plan checks"
         ((private-lane-fn 'validate-warm-runner-result!) result s)))))

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
                               {:schema :gravity/exact-tree-warm-worker-response-v2
                                :authority :non-authoritative
                                :authoritative? false
                                :request-id (:request-id request)
                                :candidate-commit candidate-commit
                                :candidate-tree candidate-tree
                                :outcome :passed :exit-code 0 :elapsed-ns 1
                                :request-count 1 :stdout "" :stderr ""
                                :output-truncated? false
                                :runner-result
                                (request-runner-result request :passed)})))))
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
