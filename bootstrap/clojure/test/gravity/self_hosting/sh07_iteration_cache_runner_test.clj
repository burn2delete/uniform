(ns gravity.self-hosting.sh07-iteration-cache-runner-test
  (:require [clojure.test :refer [deftest is testing]]
            [gravity.bootstrap :as bootstrap]
            [gravity.self-hosting.sh07-iteration-cache-runner :as runner]
            [gravity.self-hosting.sh07-semantic-carrier :as semantic-carrier])
  (:import [java.util.concurrent CountDownLatch TimeUnit]))

(def ^:private semantic-counter-keys
  [:semantic-hits :semantic-misses :semantic-joined :semantic-published
   :semantic-evictions :semantic-failed-producers :semantic-rejected-hits])

(defn- semantic-counters []
  (atom (zipmap semantic-counter-keys (repeat 0))))

(defn- await-condition
  [predicate timeout-ms]
  (let [deadline (+ (System/nanoTime) (* timeout-ms 1000000))]
    (loop []
      (if (predicate)
        true
        (if (< (System/nanoTime) deadline)
          (do (Thread/yield) (recur))
          false)))))

(defn- semantic-test-cache
  [maximum-entries maximum-bytes counters]
  (#'runner/bounded-semantic-cache maximum-entries maximum-bytes counters))

(defn- semantic-test-value
  ([key] (semantic-test-value key 1))
  ([key weight] {:key key :weight weight}))

(defn- b8-fixture-var
  [name]
  (require 'gravity.self-hosting.sh07-try-catch-test)
  (or (ns-resolve 'gravity.self-hosting.sh07-try-catch-test name)
      (throw (ex-info "Required SH-07-B8 fixture helper is absent"
                      {:name name}))))

(deftest semantic-cache-same-key-joins-and-different-key-progresses
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 100 counters)
        active (atom 0)
        maximum-active (atom 0)
        calls (atom {})
        a-started (CountDownLatch. 1)
        b-started (CountDownLatch. 1)
        release-a (CountDownLatch. 1)]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (letfn [(operation [key]
                (swap! calls update key (fnil inc 0))
                (let [now (swap! active inc)]
                  (swap! maximum-active max now)
                  (try
                    (if (= :a key)
                      (do
                        (.countDown a-started)
                        (when-not (.await release-a 2 TimeUnit/SECONDS)
                          (throw (ex-info "semantic producer timed out" {}))))
                      (.countDown b-started))
                    (semantic-test-value key)
                    (finally
                      (swap! active dec)))))]
        (let [producer (future (cache :a (constantly true) #(operation :a)))]
          (try
            (is (.await a-started 1 TimeUnit/SECONDS))
            (let [waiters (mapv (fn [_]
                                  (future (cache :a (constantly true)
                                                 #(operation :a))))
                                (range 3))]
              (is (await-condition #(= 3 (:semantic-joined @counters)) 2000))
              (let [different-key
                    (future (cache :b (constantly true) #(operation :b)))]
                (is (.await b-started 1 TimeUnit/SECONDS))
                (.countDown release-a)
                (is (not= ::timeout (deref producer 3000 ::timeout)))
                (is (not= ::timeout (deref different-key 3000 ::timeout)))
                (doseq [waiter waiters]
                  (is (not= ::timeout (deref waiter 3000 ::timeout))))
                (is (= {:a 1 :b 1} @calls))
                (is (>= @maximum-active 2))
                (is (= 2 (:semantic-misses @counters)))
                (is (= 3 (:semantic-joined @counters)))
                (is (= 2 (:semantic-published @counters)))))
            (finally
              (.countDown release-a)
              (deref producer 3000 nil))))))))

(deftest sh07-b8-fixture-contract-parity-is-bounded
  ;; This is deliberately a fixture-level guard.  The ordinary SH-07
  ;; artifact/status and diagnostic parity clause requires a bounded frozen
  ;; artifact source; invoking `file-artifact` here would redo the full
  ;; lowering and turn this focused test into the long SH-07 run it protects.
  ;; Keep the distinction explicit rather than treating source inventory as
  ;; ordinary artifact evidence.
  (let [fixture-path (b8-fixture-var 'fixture-path)
        source-bytes (b8-fixture-var 'source-bytes)
        pair (fn [family basename]
               (mapv (fn [extension]
                       (let [path (fixture-path family basename extension)
                             bytes (source-bytes path)]
                         {:extension extension
                          :path path
                          :source (String. ^bytes bytes
                                            java.nio.charset.StandardCharsets/UTF_8)
                          :bytes bytes}))
                     [".gravity" ".qst"]))
        accepted (pair "accepted" "top-level-typed-catch")
        rejected (pair "rejected" "missing-catch")
        same-bytes? (fn [items]
                      (= (seq (:bytes (first items)))
                         (seq (:bytes (second items)))))]
    (testing "accepted B8 source pair declares the accepted shape"
      (is (same-bytes? accepted))
      (is (every? #(= :accepted (:fixture-status %))
                  (map #(assoc % :fixture-status :accepted) accepted)))
      (is (every? #(.contains ^String (:source %) "(catch Exception error")
                  accepted))
      (is (every? #(.contains ^String (:source %) ":slice :SH-07-B8")
                  accepted)))
    (testing "rejected B8 source pair carries the same diagnostic oracle"
      (is (same-bytes? rejected))
      (is (every? #(= :rejected (:fixture-status %))
                  (map #(assoc % :fixture-status :rejected) rejected)))
      (is (every? #(.contains ^String (:source %) ":expected-rule \"C6-CORE-SHAPE\"")
                  rejected))
      (is (every? #(.contains ^String (:source %) ":expected-reason :try-catch-required")
                  rejected))
      (is (every? #(.contains ^String (:source %) ":expected-severity :error")
                  rejected))
      (is (every? #(not (.contains ^String (:source %) "(catch ") )
                  rejected)))))

(deftest semantic-cache-producer-failure-clears-flight-and-allows-retry
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 100 counters)
        attempts (atom 0)
        started (CountDownLatch. 1)
        release (CountDownLatch. 1)
        capture (fn [operation]
                  (try
                    {:value (operation)}
                    (catch clojure.lang.ExceptionInfo error
                      {:error (ex-data error)})))]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (let [operation
            (fn []
              (if (= 1 (swap! attempts inc))
                (do
                  (.countDown started)
                  (when-not (.await release 2 TimeUnit/SECONDS)
                    (throw (ex-info "semantic producer timed out" {})))
                  (throw (ex-info "synthetic producer failure"
                                  {:id "SEMANTIC-PRODUCER-FAILED"})))
                (semantic-test-value :failed)))
            producer (future (capture #(cache :failed (constantly true)
                                              operation)))]
        (try
          (is (.await started 1 TimeUnit/SECONDS))
          (let [waiter (future (capture #(cache :failed (constantly true)
                                                operation)))]
            (is (await-condition #(= 1 (:semantic-joined @counters)) 2000))
            (.countDown release)
            (let [producer-result (deref producer 3000 ::timeout)
                  waiter-result (deref waiter 3000 ::timeout)
                  expected {:artifact :gravity/sh07-semantic-cache-diagnostic
                            :rule "SH07-SEMANTIC-CACHE"
                            :severity :error :stage :development-cache
                            :reason :producer-failed :source-path nil
                            :fail-closed true
                            :fresh-authoritative-run-required? true}]
              (is (= expected (:error producer-result)))
              (is (= expected (:error waiter-result)))
              (is (= 1 (:semantic-failed-producers @counters)))
              (is (= 0 (:semantic-published @counters)))
              (is (= 1 (:semantic-misses @counters)))
              (is (= 1 (:semantic-joined @counters)))
              (is (= (semantic-test-value :failed)
                     (cache :failed (constantly true) operation)))
              (is (= 2 (:semantic-misses @counters)))
              (is (= 1 (:semantic-published @counters)))))
          (finally
            (.countDown release)
            (deref producer 3000 nil)))))))

(deftest semantic-cache-producer-interruption-clears-flight-and-allows-retry
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 100 counters)
        started (CountDownLatch. 1)
        release (CountDownLatch. 1)
        producer-result (promise)
        producer
        (Thread.
         #(deliver producer-result
                   (try
                     {:value
                      (cache :interrupted (constantly true)
                             (fn []
                               (.countDown started)
                               (.await release 10 TimeUnit/SECONDS)
                               (semantic-test-value :interrupted)))}
                     (catch clojure.lang.ExceptionInfo error
                       {:error (ex-data error)}))))]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (.start producer)
      (try
        (is (.await started 1 TimeUnit/SECONDS))
        (let [waiter
              (future
                (try
                  (cache :interrupted (constantly true)
                         #(semantic-test-value :interrupted))
                  nil
                  (catch clojure.lang.ExceptionInfo error
                    (ex-data error))))]
          (is (await-condition #(= 1 (:semantic-joined @counters)) 2000))
          (.interrupt producer)
          (.join producer 3000)
          (let [expected {:artifact :gravity/sh07-semantic-cache-diagnostic
                          :rule "SH07-SEMANTIC-CACHE"
                          :severity :error :stage :development-cache
                          :reason :producer-interrupted :source-path nil
                          :fail-closed true
                          :fresh-authoritative-run-required? true}]
            (is (= expected (:error @producer-result)))
            (is (= expected (deref waiter 3000 ::timeout)))
            (is (.isInterrupted producer))
            (is (= 1 (:semantic-failed-producers @counters)))
            (is (= 0 (:semantic-published @counters)))
            (is (= (semantic-test-value :interrupted)
                   (cache :interrupted (constantly true)
                          #(semantic-test-value :interrupted))))
            (is (= 2 (:semantic-misses @counters)))
            (is (= 1 (:semantic-published @counters)))))
        (finally
          (.countDown release)
          (.interrupt producer)
          (.join producer 3000))))))

(deftest semantic-cache-reentry-fails-and-retry-is-fresh
  (let [counters (semantic-counters)
        cache-holder (atom nil)
        attempts (atom 0)]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (let [cache (semantic-test-cache 4 100 counters)]
        (reset! cache-holder cache)
        (let [failure
              (try
                (cache :reentrant (constantly true)
                       (fn []
                         (swap! attempts inc)
                         (@cache-holder :reentrant (constantly true)
                                        #(semantic-test-value :reentrant))))
                nil
                (catch clojure.lang.ExceptionInfo error
                  (ex-data error)))]
          (is (= :reentrant-request (:reason failure)))
          (is (= 1 (:semantic-failed-producers @counters)))
          (is (= 0 (:semantic-published @counters)))
          (is (= (semantic-test-value :reentrant)
                 (cache :reentrant (constantly true)
                        #(do (swap! attempts inc)
                             (semantic-test-value :reentrant)))))
          (is (= 2 @attempts))
          (is (= 2 (:semantic-misses @counters)))
          (is (= 1 (:semantic-published @counters))))))))

(deftest semantic-cache-uses-fifo-entry-eviction-without-hit-promotion
  (let [counters (semantic-counters)
        cache (semantic-test-cache 2 100 counters)
        calls (atom {})]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (letfn [(load [key]
                (cache key (constantly true)
                       #(do (swap! calls update key (fnil inc 0))
                            (semantic-test-value key))))]
        (load :a)
        (load :b)
        (load :a)
        (load :c)
        (load :b)
        (load :a)
        (is (= {:a 2 :b 1 :c 1} @calls))
        (is (= 4 (:semantic-misses @counters)))
        (is (= 2 (:semantic-hits @counters)))
        (is (= 4 (:semantic-published @counters)))
        (is (= 2 (:semantic-evictions @counters)))))))

(deftest semantic-cache-evicts-by-canonical-byte-bound
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 5 counters)
        calls (atom {})]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (letfn [(load [key weight]
                (cache key (constantly true)
                       #(do (swap! calls update key (fnil inc 0))
                            (semantic-test-value key weight))))]
        (load :a 3)
        (load :b 2)
        (load :c 4)
        (load :c 4)
        (load :a 3)
        (is (= {:a 2 :b 1 :c 1} @calls))
        (is (= 4 (:semantic-misses @counters)))
        (is (= 1 (:semantic-hits @counters)))
        (is (= 4 (:semantic-published @counters)))
        (is (= 3 (:semantic-evictions @counters)))))))

(deftest semantic-cache-rejects-oversized-values-without-publication
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 3 counters)
        attempts (atom 0)]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (let [failure
            (try
              (cache :oversized (constantly true)
                     #(if (= 1 (swap! attempts inc))
                        (semantic-test-value :oversized 4)
                        (semantic-test-value :oversized 3)))
              nil
              (catch clojure.lang.ExceptionInfo error
                (ex-data error)))]
        (is (= :carrier-too-large (:reason failure)))
        (is (= 1 (:semantic-failed-producers @counters)))
        (is (= 0 (:semantic-published @counters)))
        (is (= 1 (:semantic-misses @counters)))
        (is (= (semantic-test-value :oversized 3)
               (cache :oversized (constantly true)
                      #(if (= 1 (swap! attempts inc))
                         (semantic-test-value :oversized 4)
                         (semantic-test-value :oversized 3)))))
        (is (= 2 (:semantic-misses @counters)))
        (is (= 1 (:semantic-published @counters)))))))

(deftest semantic-cache-rejects-invalid-entry-and-reruns-producer
  (let [counters (semantic-counters)
        cache (semantic-test-cache 4 100 counters)
        valid? (atom true)
        calls (atom 0)
        validate (fn [_] @valid?)]
    (with-redefs [semantic-carrier/carrier-byte-count
                  (fn [value] (:weight value))]
      (let [load #(cache :invalid validate
                         (fn []
                           (swap! calls inc)
                           (semantic-test-value :invalid)))]
        (is (= (semantic-test-value :invalid) (load)))
        (reset! valid? false)
        (is (= (semantic-test-value :invalid) (load)))
        (reset! valid? true)
        (is (= (semantic-test-value :invalid) (load)))
        (is (= 2 @calls))
        (is (= 1 (:semantic-hits @counters)))
        (is (= 2 (:semantic-misses @counters)))
        (is (= 2 (:semantic-published @counters)))
        (is (= 1 (:semantic-rejected-hits @counters)))))))

(def ^:private exact-c10-semantic-test-vars
  '[gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-has-exact-authentic-coverage
    gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-calls-lookups-and-quotes-are-exact
    gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-is-deterministic-path-neutral-and-provenanced
    gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-replay-and-high-value-alterations-are-contained])

(defn- synthetic-semantic-request [resolution]
  (let [path (get-in resolution [:provenance :source-path])
        canonical (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot)]
    {:artifact :gravity/sh07-authenticated-sh06-core-request
     :schema-version 15
     :lineage {:source-revision-id (:sha256 canonical)
               :sh06-semantic-projection-id "projection"
               :authenticated-sh06-artifact-id (:artifact-id resolution)}
        :module {:namespace 'gravity.compiler.c10-safety-analysis-pipeline
                 :source-revision-id (:sha256 canonical)
                 :profile :meta :target :jvm
              :safety :safe :effects [] :capabilities [] :exports []}
     :provenance {:actual-source-path path}
     :scope :sh07-b15-keyword-map-lookup}))

(defn- synthetic-semantic-artifact [resolution]
  (let [request (synthetic-semantic-request resolution)
        path (get-in request [:provenance :actual-source-path])
        sh06-id (:artifact-id resolution)
        identity-preimage {:lineage {:source-revision-id
                                     (get-in request [:lineage :source-revision-id])
                                     :sh06-semantic-projection-id "projection"
                                     :authenticated-sh06-artifact-id sh06-id}}
        provenance-preimage {:actual-source-path path}
        requests [{:ordinal 0 :purpose :sh07-core-artifact-id
                   :preimage identity-preimage}
                  {:ordinal 1 :purpose :sh07-core-provenance-binding-id
                   :preimage provenance-preimage}]
        digest (fn [purpose preimage]
                 (let [resolved-preimage
                       (if (= :sh07-core-artifact-id purpose)
                         (bootstrap/sh07-core-semantic-identity-preimage
                          preimage)
                         preimage)]
                   (bootstrap/reader-canonical-hash
                    {:domain :gravity/sh07-declared-digest-v1
                     :purpose purpose :preimage resolved-preimage})))
        resolved (mapv #(digest (:purpose %) (:preimage %)) requests)
        core {:artifact-id (first resolved)
              :identity-preimage identity-preimage
              :provenance {:actual-source-path path}
              :provenance-binding-preimage provenance-preimage
              :provenance-binding-id (second resolved)
              :lineage (:lineage request)
              :empty-vector [] :empty-map {} :empty-set #{}}
        run {:raw-template-result
             {:artifact :gravity/sh07-core-template-result
              :schema-version 16 :status :accepted
              :core-template {:provenance {:actual-source-path path}
                              :provenance-binding-preimage provenance-preimage
                              :lineage (:lineage request)}
              :diagnostics [] :bounds {} :containment {}
              :execution-boundary {}
              :digest-requests requests}
             :canonical-core-artifact core :digest-requests requests
             :resolved-digests resolved
             :template-verification
             {:artifact :gravity/sh07-core-template-verification
              :schema-version 16 :status :passed :rule nil :reason nil :details {}}
             :resolved-verification
             {:artifact :gravity/sh07-core-resolved-verification
              :schema-version 16 :status :passed :rule nil}}]
    {:kind :gravity/sh07-core-artifact :status :accepted
     :artifact-id (:artifact-id core) :sh06-resolution-artifact resolution
     :gravity-core-boundary run :provenance {:source-path path}}))

(deftest semantic-reuse-requires-the-exact-c10-selection
  (let [enabled?
        (fn [selection]
          (:semantic-cache-enabled?
           (runner/with-iteration-cache
            {:maximum-entries 1 :selection selection}
            (constantly :ok))))]
    (is (true? (enabled? {:mode :test-vars
                          :units exact-c10-semantic-test-vars})))
    (is (true? (enabled? {:mode :test-vars
                          :units (vec (reverse exact-c10-semantic-test-vars))})))
    (is (false? (enabled? {:mode :test-vars
                           :units (subvec exact-c10-semantic-test-vars 0 3)})))
    (is (false? (enabled? {:mode :test-vars
                           :units (conj exact-c10-semantic-test-vars
                                        (first exact-c10-semantic-test-vars))})))
    (is (false? (enabled? {:mode :test-vars
                           :units (conj exact-c10-semantic-test-vars
                                        'gravity.self-hosting.sh07-c10-safety-source-coverage-test/unknown)})))
    (is (false? (enabled? {:mode :test-vars
                           :units [(first exact-c10-semantic-test-vars)
                                   'gravity.self-hosting.sh07-iteration-cache-runner-test/semantic-reuse-requires-the-exact-c10-selection]})))
    (is (false? (enabled? {:mode :test-vars :units []})))
    (is (false? (enabled? {:mode :namespaces
                           :units ['gravity.self-hosting.sh07-c10-safety-source-coverage-test]})))))

(deftest exact-c10-route-rebinds-one-successful-carrier-across-paths
  (let [left-root (java.nio.file.Files/createTempDirectory
                   "sh07-semantic-runner-left-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        right-root (java.nio.file.Files/createTempDirectory
                    "sh07-semantic-runner-right-"
                    (make-array java.nio.file.attribute.FileAttribute 0))
        left (.resolve left-root "left.gravity")
        right (.resolve right-root "right.gravity")
        source (String.
                ^bytes (:bytes (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot))
                java.nio.charset.StandardCharsets/UTF_8)
        core-builds (atom 0)
        verification-reports (atom 0)
        hit-forbidden-calls (atom {})
        phase (atom :admission)
        plan-leaves (atom [])
        plan-binding-var
        (ns-resolve 'gravity.self-hosting.sh07-iteration-cache-runner
                    'current-sh07-plan-binding)]
    (try
      (doseq [path [left right]]
        (java.nio.file.Files/write
         path (.getBytes source java.nio.charset.StandardCharsets/UTF_8)
         (make-array java.nio.file.OpenOption 0)))
      (with-redefs-fn
        {plan-binding-var (constantly {:synthetic true})}
        #(with-redefs
          [bootstrap/sh06-resolution-source-artifact
           (fn [path _source]
             {:kind :gravity/sh06-resolution-artifact :status :accepted
              :artifact-id "sh06"
              :provenance
              {:source-path path
               :source-revision-id
               (:sha256 (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot))}
              :gravity-resolution-boundary
              {:plan-binding {:unrelated-sh06-pin true}
               :authenticated-resolution-request
               {:module
                {:source-revision-id
                 (:sha256 (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot))}}
               :resolved-analysis {:semantic-projection-id "projection"}}})
           bootstrap/sh06-resolution-artifact-verification
           (fn [resolution]
             {:artifact :gravity/sh06-resolution-artifact-verification
              :status :passed :checks {:synthetic true} :failed-checks []
              :source-path (get-in resolution [:provenance :source-path])})
           bootstrap/sh07-core-authenticated-request synthetic-semantic-request
           bootstrap/sh07-core-from-resolution-artifact
           (fn [resolution]
             (swap! core-builds inc)
             (when (= :hit @phase)
               (swap! hit-forbidden-calls update :from-resolution (fnil inc 0)))
             (synthetic-semantic-artifact resolution))
           bootstrap/sh07-core-execute!
           (fn [_ function arguments]
             (when (= :hit @phase)
               (swap! plan-leaves conj function))
             {:status :accepted :value (first arguments)})
           bootstrap/sh07-core-verification-checks
           (fn [_ _ _] {:synthetic-check true})
           bootstrap/sh07-core-proof-from-checks
           (fn [_] {:artifact :gravity/sh07-core-capability-proof
                    :status :complete :failed-checks []})
           bootstrap/sh07-core-artifact-verification
           (fn [artifact]
             (swap! verification-reports inc)
             (when (= :hit @phase)
               (swap! hit-forbidden-calls update :artifact-verification (fnil inc 0)))
             (bootstrap/sh07-core-from-resolution-artifact
             (:sh06-resolution-artifact artifact))
             {:artifact :gravity/sh07-core-artifact-verification
              :status :passed :checks {:synthetic-check true}
              :failed-checks []
              :source-path (get-in artifact [:provenance :source-path])})
           bootstrap/sh07-core-run-request-for-test
           (fn [& _]
             (swap! hit-forbidden-calls update :run-request (fnil inc 0))
             (throw (ex-info "forbidden SH-07 run request" {})))
           bootstrap/sh07-core-run-structural-request-for-test
           (fn [& _]
             (swap! hit-forbidden-calls update :run-structural-request (fnil inc 0))
             (throw (ex-info "forbidden SH-07 structural request" {})))
           bootstrap/sh07-core-from-authenticated-request
           (fn [& _]
             (swap! hit-forbidden-calls update :from-authenticated-request (fnil inc 0))
             (throw (ex-info "forbidden SH-07 authenticated request" {})))
           bootstrap/sh07-core-verification-session
           (fn [& _]
             (swap! hit-forbidden-calls update :verification-session (fnil inc 0))
             (throw (ex-info "forbidden SH-07 verification session" {})))]
          (let [result
                (runner/with-iteration-cache
                 {:maximum-entries 2
                  :selection {:mode :test-vars
                              :units exact-c10-semantic-test-vars}}
                 (fn []
                   (mapv
                    (fn [[index path]]
                      (when (= 1 index)
                        (reset! phase :hit)
                        (reset! plan-leaves []))
                      (let [resolution
                            (bootstrap/sh06-resolution-source-artifact
                             (str path) source)
                            artifact
                            (bootstrap/sh07-core-from-resolution-artifact resolution)
                            report
                            (bootstrap/sh07-core-artifact-verification artifact)]
                        {:path (get-in artifact [:provenance :source-path])
                         :artifact-id (:artifact-id artifact)
                         :artifact artifact
                         :report-status (:status report)}))
                    (map-indexed vector [left right]))))]
            (is (= 2 @core-builds) (:cache result))
            (is (= 1 @verification-reports) (:cache result))
            (is (= ['sh07-resolve-identity-preimage
                    'sh07-resolve-provenance-preimage]
                   @plan-leaves))
            (is (= {} @hit-forbidden-calls))
            (is (= 1 (get-in result [:cache :semantic-misses])))
            (is (= 1 (get-in result [:cache :semantic-hits])))
            (is (= [(str left) (str right)]
                   (mapv :path (:value result))))
            (is (apply = (map :artifact-id (:value result))))
            (is (#'gravity.self-hosting.sh07-iteration-cache-runner/aggregate-identities-disjoint?
                 (get-in result [:value 0 :artifact])
                 (get-in result [:value 1 :artifact])))
            (is (every? #{:passed} (map :report-status (:value result)))))))
      (finally
        (java.nio.file.Files/deleteIfExists left)
        (java.nio.file.Files/deleteIfExists right)
        (java.nio.file.Files/deleteIfExists left-root)
        (java.nio.file.Files/deleteIfExists right-root)))))

(deftest exact-c10-selection-bypasses-semantic-cache-for-altered-bytes
  (let [path (java.nio.file.Files/createTempFile
              "sh07-altered-c10-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))
        current (String.
                 ^bytes (:bytes (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot))
                 java.nio.charset.StandardCharsets/UTF_8)
        altered (str (if (= \space (first current)) \; \space)
                     (subs current 1))
        core-builds (atom 0)]
    (try
      (java.nio.file.Files/write
       path (.getBytes altered java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (with-redefs
        [bootstrap/sh06-resolution-source-artifact
         (fn [source-path _]
           {:artifact-id "altered-sh06" :provenance {:source-path source-path}
            :gravity-resolution-boundary
            {:resolved-analysis {:semantic-projection-id "altered"}}})
         bootstrap/sh07-core-from-resolution-artifact
         (fn [resolution]
           (swap! core-builds inc)
           {:artifact-id "ordinary" :sh06-resolution-artifact resolution})
         bootstrap/sh07-core-artifact-verification
         (fn [_] {:artifact :gravity/sh07-core-artifact-verification
                  :status :passed :failed-checks []})]
        (let [result
              (runner/with-iteration-cache
               {:maximum-entries 2
                :selection {:mode :test-vars
                            :units exact-c10-semantic-test-vars}}
               (fn []
                 (dotimes [_ 2]
                   (let [resolution
                         (bootstrap/sh06-resolution-source-artifact
                          (str path) altered)]
                     (bootstrap/sh07-core-artifact-verification
                      (bootstrap/sh07-core-from-resolution-artifact resolution))))))]
          (is (= 1 @core-builds))
          (is (= 0 (get-in result [:cache :semantic-misses])))
          (is (= 0 (get-in result [:cache :semantic-hits])))
          (is (= 0 (get-in result [:cache :semantic-published])))))
      (finally
        (java.nio.file.Files/deleteIfExists path)))))

(deftest exact-c10-selection-captures-canonical-source-once
  (let [captures (atom 0)
        original @#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot]
    (with-redefs-fn
      {#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot
       (fn [] (swap! captures inc) (original))}
      #(runner/with-iteration-cache
        {:maximum-entries 1
         :selection {:mode :test-vars :units exact-c10-semantic-test-vars}}
        (constantly :ok)))
    (is (= 1 @captures))))

(deftest exact-c10-selection-bypasses-each-live-binding-mismatch
  (let [canonical (#'gravity.self-hosting.sh07-iteration-cache-runner/current-c10-source-snapshot)
        source (String. ^bytes (:bytes canonical)
                        java.nio.charset.StandardCharsets/UTF_8)
        path (java.nio.file.Files/createTempFile
              "sh07-c10-pin-" ".gravity"
              (make-array java.nio.file.attribute.FileAttribute 0))
        base-resolution
        {:kind :gravity/sh06-resolution-artifact :status :accepted
         :artifact-id "sh06"
         :provenance {:source-path (str path)
                      :source-revision-id (:sha256 canonical)}
         :gravity-resolution-boundary
         {:plan-binding {:unrelated-sh06-pin true}
          :authenticated-resolution-request
          {:module {:source-revision-id (:sha256 canonical)}}
          :resolved-analysis {:semantic-projection-id "projection"}}}
        base-request (synthetic-semantic-request base-resolution)
        cases
        [[:resolution-revision
          (assoc-in base-resolution [:provenance :source-revision-id] "sha256:bad")
          base-request]
         [:resolution-request-revision
          (assoc-in base-resolution
                    [:gravity-resolution-boundary :authenticated-resolution-request
                     :module :source-revision-id]
                    "sha256:bad")
          base-request]
         [:request-revision base-resolution
          (assoc-in base-request [:lineage :source-revision-id] "sha256:bad")]
         [:module base-resolution
          (assoc-in base-request [:module :namespace] 'demo.core)]
         [:projection base-resolution
          (assoc-in base-request [:lineage :sh06-semantic-projection-id] "other")]]]
    (try
      (java.nio.file.Files/write path (:bytes canonical)
                                 (make-array java.nio.file.OpenOption 0))
      (doseq [[label resolution request] cases]
        (with-redefs
          [bootstrap/sh06-resolution-source-artifact (fn [_ _] resolution)
           bootstrap/sh07-core-authenticated-request (fn [_] request)
           bootstrap/sh07-core-from-resolution-artifact
           (fn [current]
             {:artifact-id "ordinary" :sh06-resolution-artifact current})
           bootstrap/sh07-core-artifact-verification
           (fn [_] {:artifact :gravity/sh07-core-artifact-verification
                    :status :passed :failed-checks []})]
          (let [result
                (runner/with-iteration-cache
                 {:maximum-entries 1
                  :selection {:mode :test-vars :units exact-c10-semantic-test-vars}}
                 #(let [current (bootstrap/sh06-resolution-source-artifact
                                 (str path) source)]
                    (bootstrap/sh07-core-artifact-verification
                     (bootstrap/sh07-core-from-resolution-artifact current))))]
            (is (= [0 0 0]
                   (mapv #(get-in result [:cache %])
                         [:semantic-misses :semantic-hits :semantic-published]))
                (str label)))))
      (finally
        (java.nio.file.Files/deleteIfExists path)))))

(deftest semantic-source-snapshot-rejects-missing-symlink-and-nul
  (let [directory (java.nio.file.Files/createTempDirectory
                   "sh07-semantic-source-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        target (.resolve directory "target.gravity")
        link (.resolve directory "link.gravity")
        missing (.resolve directory "missing.gravity")
        resolution {:gravity-resolution-boundary
                    {:resolved-analysis {:semantic-projection-id "projection"}}}
        snapshot #'gravity.self-hosting.sh07-iteration-cache-runner/strict-source-snapshot
        reason (fn [path source]
                 (:reason
                  (ex-data
                   (try
                     (snapshot (str path) source resolution)
                     (catch clojure.lang.ExceptionInfo error error)))))]
    (try
      (java.nio.file.Files/write
       target (.getBytes "ok" java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (java.nio.file.Files/createSymbolicLink
       link (.getFileName target)
       (make-array java.nio.file.attribute.FileAttribute 0))
      (is (= :source-missing (reason missing "")))
      (is (= :source-symlink (reason link "ok")))
      (is (= :source-missing
             (reason (str "invalid" (char 0) ".gravity") "ok")))
      (java.nio.file.Files/write
       target (byte-array [(byte 0)])
       (make-array java.nio.file.OpenOption 0))
      (is (= :invalid-utf8 (reason target (str (char 0)))))
      (finally
        (java.nio.file.Files/deleteIfExists link)
        (java.nio.file.Files/deleteIfExists target)
        (java.nio.file.Files/deleteIfExists directory)))))

(deftest semantic-source-snapshot-rejects-not-regular-invalid-extension-and-mutation
  (let [directory (java.nio.file.Files/createTempDirectory
                   "sh07-semantic-source-rules-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        regular (.resolve directory "regular.gravity")
        directory-path (.resolve directory "nested.gravity")
        text-path (.resolve directory "text.txt")
        resolution {:gravity-resolution-boundary
                    {:resolved-analysis {:semantic-projection-id "projection"}}}
        snapshot #'gravity.self-hosting.sh07-iteration-cache-runner/strict-source-snapshot
        reason (fn [path source]
                 (:reason
                  (ex-data
                   (try
                     (snapshot (str path) source resolution)
                     (catch clojure.lang.ExceptionInfo error error)))))]
    (try
      (java.nio.file.Files/write
       regular (.getBytes "ok" java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (java.nio.file.Files/createDirectory
       directory-path (make-array java.nio.file.attribute.FileAttribute 0))
      (java.nio.file.Files/write
       text-path (.getBytes "ok" java.nio.charset.StandardCharsets/UTF_8)
       (make-array java.nio.file.OpenOption 0))
      (is (= :source-not-regular (reason directory-path "ok")))
      (is (= :invalid-extension (reason text-path "ok")))
      (is (= :source-mutated (reason regular "changed")))
      (finally
        (java.nio.file.Files/deleteIfExists regular)
        (java.nio.file.Files/deleteIfExists text-path)
        (java.nio.file.Files/deleteIfExists directory-path)
        (java.nio.file.Files/deleteIfExists directory)))))

(deftest semantic-source-snapshot-rejects-malformed-utf8-and-oversize
  (let [directory (java.nio.file.Files/createTempDirectory
                   "sh07-semantic-source-bounds-"
                   (make-array java.nio.file.attribute.FileAttribute 0))
        malformed (.resolve directory "malformed.gravity")
        oversized (.resolve directory "oversized.gravity")
        resolution {:gravity-resolution-boundary
                    {:resolved-analysis {:semantic-projection-id "projection"}}}
        snapshot #'gravity.self-hosting.sh07-iteration-cache-runner/strict-source-snapshot
        reason (fn [path source]
                 (:reason
                  (ex-data
                   (try
                     (snapshot (str path) source resolution)
                     (catch clojure.lang.ExceptionInfo error error)))))]
    (try
      (java.nio.file.Files/write
       malformed (byte-array [(unchecked-byte 0xC3) (unchecked-byte 0x28)])
       (make-array java.nio.file.OpenOption 0))
      (is (= :invalid-utf8 (reason malformed "x")))
      (java.nio.file.Files/write
       oversized (byte-array (inc (* 16 1024 1024)))
       (make-array java.nio.file.OpenOption 0))
      (is (= :source-too-large (reason oversized "x")))
      (finally
        (java.nio.file.Files/deleteIfExists malformed)
        (java.nio.file.Files/deleteIfExists oversized)
        (java.nio.file.Files/deleteIfExists directory)))))

(deftest iteration-cache-reuses-equal-source-and-identical-artifacts
  (let [sh06-calls (atom 0)
        core-calls (atom 0)
        verification-calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! sh06-calls inc)
         {:artifact-id (str path ":" source)
          :provenance {:source-path path}
          :gravity-resolution-boundary
          {:resolved-analysis {:semantic-projection-id source}}})
       bootstrap/sh07-core-from-resolution-artifact
       (fn [resolution]
         (swap! core-calls inc)
         {:artifact-id (:artifact-id resolution)})
       bootstrap/sh07-core-artifact-verification
       (fn [artifact]
         (swap! verification-calls inc)
         {:verified (:artifact-id artifact)})]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 2}
             (fn []
               (let [left (bootstrap/sh06-resolution-source-artifact "a" "x")
                     right (bootstrap/sh06-resolution-source-artifact "a" "x")
                     core-left
                     (bootstrap/sh07-core-from-resolution-artifact left)
                     core-right
                     (bootstrap/sh07-core-from-resolution-artifact right)]
                 [(bootstrap/sh07-core-artifact-verification core-left)
                  (bootstrap/sh07-core-artifact-verification core-right)])))]
        (is (= 1 @sh06-calls @core-calls @verification-calls))
        (is (= {:sh06-hits 1 :sh06-misses 1
                :core-hits 1 :core-misses 1
                :verification-hits 1 :verification-misses 1}
               (select-keys (:cache result)
                            [:sh06-hits :sh06-misses :core-hits :core-misses
                             :verification-hits :verification-misses])))
        (is (= :non-authoritative (:authority result)))
        (is (false? (:authoritative? result)))
        (is (false? (:cache-authoritative? result)))
        (is (true? (:fresh-authoritative-run-required? result)))))))

(deftest iteration-cache-is-content-keyed-and-bounded
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               [(bootstrap/sh06-resolution-source-artifact "a" "one")
                (bootstrap/sh06-resolution-source-artifact "a" "two")
                (bootstrap/sh06-resolution-source-artifact "a" "one")]))]
        (is (= 3 @calls))
        (is (= 3 (get-in result [:cache :sh06-misses])))
        (is (zero? (get-in result [:cache :sh06-hits])))))))

(deftest iteration-cache-serializes-concurrent-misses
  (let [calls (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         (Thread/sleep 25)
         [path source])]
      (let [result
            (runner/with-iteration-cache
             {:maximum-entries 1}
             (fn []
               (mapv deref
                     (repeatedly
                      8
                      #(future
                         (bootstrap/sh06-resolution-source-artifact
                          "same" "content"))))))]
        (is (= 1 @calls))
        (is (= 1 (get-in result [:cache :sh06-misses])))
        (is (= 7 (get-in result [:cache :sh06-hits])))
        (is (apply = (:value result)))))))

(deftest iteration-cache-allows-different-keys-and-retries-failures
  (let [active (atom 0)
        maximum-active (atom 0)
        attempts (atom 0)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! attempts inc)
         (let [now (swap! active inc)]
           (swap! maximum-active max now)
           (try
             (Thread/sleep 40)
             (when (= source :failed)
               (throw (ex-info "synthetic failure" {:id "FAILED"})))
             [path source]
             (finally (swap! active dec)))))]
      (let [parallel
            (runner/with-iteration-cache
             {:maximum-entries 2}
             #(mapv deref
                    [(future
                       (bootstrap/sh06-resolution-source-artifact "a" :one))
                     (future
                       (bootstrap/sh06-resolution-source-artifact "b" :two))]))]
        (is (= 2 @maximum-active))
        (is (= 2 (get-in parallel [:cache :sh06-misses]))))
      (dotimes [_ 2]
        (let [data
              (try
                (runner/with-iteration-cache
                 {:maximum-entries 1}
                 #(bootstrap/sh06-resolution-source-artifact "a" :failed))
                nil
                (catch clojure.lang.ExceptionInfo error (ex-data error)))]
          (is (= "FAILED" (:id data)))))
      (is (= 4 @attempts)))))

(deftest iteration-cache-reentrant-same-key-fails-without-deadlock
  (let [delegate (atom nil)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (@delegate path source))]
      (let [data
            (try
              (runner/with-iteration-cache
               {:maximum-entries 1}
               (fn []
                 (reset! delegate bootstrap/sh06-resolution-source-artifact)
                 (bootstrap/sh06-resolution-source-artifact "same" "bytes")))
              nil
              (catch clojure.lang.ExceptionInfo error (ex-data error)))]
        (is (= "SH07-ITERATION-CACHE-REENTRANT" (:id data)))))))

(deftest iteration-cache-reports-per-namespace-time-and-cache-deltas
  (let [calls (atom 0)
        tested-namespaces (atom [])
        output (java.io.StringWriter.)]
    (with-redefs
      [bootstrap/sh06-resolution-source-artifact
       (fn [path source]
         (swap! calls inc)
         [path source])
       clojure.test/test-ns
       (fn [namespace]
         (swap! tested-namespaces conj namespace)
         (case namespace
           example.one
           (let [nested (Thread.
                         #(clojure.test/test-ns 'unrelated.namespace))]
             (.start nested)
             (.join nested))

           unrelated.namespace
           (bootstrap/sh06-resolution-source-artifact "same" "content")

           example.two
           (bootstrap/sh06-resolution-source-artifact "same" "content"))
         {:test 0 :pass 0 :fail 0 :error 0})]
      (let [result
            (binding [*out* output]
              (runner/with-iteration-cache
               {:maximum-entries 1}
               #(#'runner/run-tests-with-telemetry
                  '[example.one example.two] false)))
            namespace-results (get-in result [:value :namespace-results])]
        (is (= 1 @calls))
        (is (= '[example.one unrelated.namespace example.two]
               @tested-namespaces))
        (is (= '[example.one example.two]
               (mapv :namespace namespace-results)))
        (is (every? #(and (integer? (:elapsed-ms %))
                          (not (neg? (:elapsed-ms %))))
                    namespace-results))
        (is (= 1 (get-in namespace-results [0 :cache :sh06-misses])))
        (is (= 1 (get-in namespace-results [1 :cache :sh06-hits])))
        (is (every? #(= :non-authoritative (:authority %))
                    namespace-results))
        (is (= (:cache result)
               (apply merge-with + (map :cache namespace-results))))
        (is (= 2
               (count
                (re-seq #":gravity/sh07-iteration-namespace-result"
                        (str output)))))))))

(deftest iteration-cache-fail-fast-skips-downstream-namespaces
  (let [test-namespace-var
        (ns-resolve
         'gravity.self-hosting.sh07-iteration-cache-runner
         'test-namespace)
        tested-namespaces (atom [])
        output (java.io.StringWriter.)]
    (with-redefs-fn
      {test-namespace-var
       (fn [namespace _fail-fast?]
         (swap! tested-namespaces conj namespace)
         {:summary
          (if (= 'example.failing namespace)
            {:test 1 :pass 0 :fail 1 :error 0}
            {:test 1 :pass 1 :fail 0 :error 0})
          :skipped-test-vars []})}
      (fn []
        (let [result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (#'runner/run-tests-with-telemetry
                 '[example.failing example.skipped] true))]
        (is (= '[example.failing] @tested-namespaces))
        (is (= '[example.failing]
               (mapv :namespace (:namespace-results result))))
        (is (true? (:stopped-early? result)))
        (is (= '[example.skipped] (:skipped-namespaces result)))
        (is (= [] (:skipped-test-vars result)))
        (is (= {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
               (:test-result result)))
        (is (= 1
               (count
                (re-seq #":gravity/sh07-iteration-namespace-result"
                        (str output)))))
        (reset! tested-namespaces [])
        (let [full-result
              (binding [*out* (java.io.StringWriter.)
                        clojure.test/*test-out* (java.io.StringWriter.)]
                (#'runner/run-tests-with-telemetry
                 '[example.failing example.skipped] false))]
          (is (= '[example.failing example.skipped] @tested-namespaces))
          (is (false? (:stopped-early? full-result)))
          (is (= [] (:skipped-namespaces full-result)))
          (is (= {:test 2 :pass 1 :fail 1 :error 0 :type :summary}
                 (:test-result full-result)))))))))

(deftest iteration-cache-fail-fast-stops-inside-ordinary-namespace
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fail-fast-test
        namespace-object (create-ns namespace-symbol)
        once-calls (atom 0)
        each-calls (atom 0)
        executed (atom [])
        output (java.io.StringWriter.)]
    (try
      (let [test-vars
            (mapv
             (fn [var-name]
               (let [test-var (intern namespace-object var-name)]
                 (alter-meta! test-var assoc :test (fn []))
                 test-var))
             '[alpha-test beta-test gamma-test])
            ordered-vars
            (vec
             (filter (comp :test meta)
                     (vals (ns-interns namespace-object))))
            failing-var (first ordered-vars)
            passing-vars (subvec ordered-vars 1)
            qualified
            (fn [test-var]
              (symbol (str namespace-symbol)
                      (str (:name (meta test-var)))))]
        (alter-meta!
         failing-var assoc :test
         #(do
            (swap! executed conj (:name (meta failing-var)))
            (clojure.test/do-report
             {:type :fail
              :message "synthetic fail-fast failure"
              :expected true
              :actual false})))
        (doseq [test-var passing-vars]
          (alter-meta!
           test-var assoc :test
           #(do
              (swap! executed conj (:name (meta test-var)))
              (clojure.test/do-report
               {:type :pass :expected true :actual true}))))
        (alter-meta!
         namespace-object assoc
         :clojure.test/once-fixtures
         [(fn [operation] (swap! once-calls inc) (operation))]
         :clojure.test/each-fixtures
         [(fn [operation] (swap! each-calls inc) (operation))])
        (let [result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (#'runner/run-tests-with-telemetry
                 [namespace-symbol] true))]
          (is (= [(:name (meta failing-var))] @executed))
          (is (= 1 @once-calls @each-calls))
          (is (= (mapv qualified passing-vars)
                 (:skipped-test-vars result)))
          (is (= (:skipped-test-vars result)
                 (get-in result [:namespace-results 0
                                 :skipped-test-vars])))
          (is (true? (:stopped-early? result)))
          (is (= {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
                 (:test-result result))))
        (reset! executed [])
        (reset! once-calls 0)
        (reset! each-calls 0)
        (let [result
              (binding [*out* (java.io.StringWriter.)
                        clojure.test/*test-out* (java.io.StringWriter.)]
                (#'runner/run-tests-with-telemetry
                 [namespace-symbol] false))]
          (is (= (set (map (comp :name meta) test-vars))
                 (set @executed)))
          (is (= 1 @once-calls))
          (is (= 3 @each-calls))
          (is (false? (:stopped-early? result)))
          (is (= [] (:skipped-test-vars result)))
          (is (= {:test 3 :pass 2 :fail 1 :error 0 :type :summary}
                 (:test-result result)))))
      (finally
        (remove-ns namespace-symbol)))))

(deftest iteration-cache-fail-fast-preserves-dynamic-fixture-selection
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fixture-selection-test
        namespace-object (create-ns namespace-symbol)
        added-var (intern namespace-object 'added-by-once-fixture)
        removed-var (intern namespace-object 'removed-by-once-fixture)
        empty-namespace-symbol
        'gravity.self-hosting.synthetic-empty-fixture-test
        empty-namespace (create-ns empty-namespace-symbol)
        executed (atom [])
        each-calls (atom 0)
        empty-once-calls (atom 0)]
    (try
      (alter-meta!
       removed-var assoc :test
       #(swap! executed conj 'removed-by-once-fixture))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [operation]
          (alter-meta!
           added-var assoc :test
           #(do
              (swap! executed conj 'added-by-once-fixture)
              (clojure.test/do-report
               {:type :pass :expected true :actual true})))
          (alter-meta! removed-var dissoc :test)
          (operation))]
       :clojure.test/each-fixtures
       [(fn [operation] (swap! each-calls inc) (operation))])
      (alter-meta!
       empty-namespace assoc
       :clojure.test/once-fixtures
       [(fn [operation] (swap! empty-once-calls inc) (operation))])
      (let [selection-result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/fail-fast-test-ns namespace-symbol))
            empty-result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/fail-fast-test-ns empty-namespace-symbol))]
        (is (= '[added-by-once-fixture] @executed))
        (is (= 1 @each-calls))
        (is (= {:test 1 :pass 1 :fail 0 :error 0}
               (:summary selection-result)))
        (is (zero? @empty-once-calls))
        (is (= {:test 0 :pass 0 :fail 0 :error 0}
               (:summary empty-result))))
      (finally
        (remove-ns namespace-symbol)
        (remove-ns empty-namespace-symbol)))))

(deftest iteration-cache-retains-telemetry-when-fixture-throws
  (let [namespace-symbol 'gravity.self-hosting.synthetic-fixture-error-test
        namespace-object (create-ns namespace-symbol)
        test-var (intern namespace-object 'never-runs-test)]
    (try
      (alter-meta!
       test-var assoc :test
       #(clojure.test/do-report
         {:type :pass :expected true :actual true}))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [_operation]
          (throw (ex-info "fixture failed" {:id "FIXTURE-FAILED"})))])
      (doseq [fail-fast? [false true]]
        (let [output (java.io.StringWriter.)
              data
              (binding [*out* output
                        clojure.test/*test-out* output]
                (try
                  (#'runner/run-tests-with-telemetry
                   [namespace-symbol] fail-fast?)
                  nil
                  (catch clojure.lang.ExceptionInfo error
                    (ex-data error))))
              rendered (str output)]
          (is (= "FIXTURE-FAILED" (:id data)))
          (is (re-find #":gravity/sh07-iteration-namespace-result"
                       rendered))
          (is (re-find #":completed\? false" rendered))
          (is (re-find #":authority :non-authoritative" rendered))))
      (finally
        (remove-ns namespace-symbol)))))

(deftest iteration-cache-fail-fast-preserves-test-ns-hook-boundary
  (let [hook-namespace-symbol
        'gravity.self-hosting.synthetic-test-ns-hook-test
        later-namespace-symbol
        'gravity.self-hosting.synthetic-after-test-ns-hook-test
        hook-namespace (create-ns hook-namespace-symbol)
        later-namespace (create-ns later-namespace-symbol)
        hook-calls (atom 0)
        later-calls (atom 0)]
    (try
      (intern
       hook-namespace 'test-ns-hook
       #(do
          (swap! hook-calls inc)
          (clojure.test/do-report
           {:type :fail
            :message "synthetic hook failure"
            :expected true
            :actual false})))
      (let [later-var (intern later-namespace 'later-test)]
        (alter-meta!
         later-var assoc :test
         #(do
            (swap! later-calls inc)
            (clojure.test/do-report
             {:type :pass :expected true :actual true}))))
      (let [result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (#'runner/run-tests-with-telemetry
               [hook-namespace-symbol later-namespace-symbol] true))]
        (is (= 1 @hook-calls))
        (is (zero? @later-calls))
        (is (true? (:stopped-early? result)))
        (is (= [later-namespace-symbol] (:skipped-namespaces result)))
        (is (= [] (:skipped-test-vars result)))
        (is (= {:test 0 :pass 0 :fail 1 :error 0 :type :summary}
               (:test-result result))))
      (finally
        (remove-ns hook-namespace-symbol)
        (remove-ns later-namespace-symbol)))))

(deftest iteration-cache-runs-one-owned-test-var-with-honest-authority
  (require 'gravity.cli-test)
  (let [selection
        (runner/parse-arguments
         ["--test-var"
          "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"
          "--max-cache-entries" "1"])
        namespace-object (find-ns 'gravity.cli-test)
        original-namespace-meta (meta namespace-object)
        once-calls (atom 0)
        each-calls (atom 0)
        report-events (atom [])
        original-report clojure.test/report
        output (java.io.StringWriter.)
        result
        (try
          (alter-meta!
           namespace-object assoc
           :clojure.test/once-fixtures
           [(fn [operation] (swap! once-calls inc) (operation))]
           :clojure.test/each-fixtures
           [(fn [operation] (swap! each-calls inc) (operation))])
          (with-redefs
            [clojure.test/report
             (fn [event]
               (swap! report-events conj (:type event))
               (original-report event))]
            (binding [*out* output
                      clojure.test/*test-out* output]
              (runner/run-selection selection)))
          (finally
            (reset-meta! namespace-object original-namespace-meta)))]
    (is (true? (:ok? result)))
    (is (= :gravity/sh07-iteration-cache-run (:artifact result)))
    (is (= 1 (get-in result [:test-result :test])))
    (is (= 40 (get-in result [:test-result :pass])))
    (is (zero? (get-in result [:test-result :fail])))
    (is (= :non-authoritative (:authority result)))
    (is (false? (:authoritative? result)))
    (is (true? (:fresh-authoritative-run-required? result)))
    (is (= :non-authoritative
           (get-in result [:test-var-result :authority])))
    (is (re-find #":gravity/sh07-iteration-test-var-result" (str output)))
    (is (= 1 @once-calls @each-calls))
    (is (= :begin-test-ns (first @report-events)))
    (is (= [:end-test-ns :summary] (vec (take-last 2 @report-events)))))
  (is (= "SH07-ITERATION-CACHE-TEST-VAR"
         (:id
          (ex-data
           (try
             (runner/run-selection
              (runner/parse-arguments
               ["--test-var" "gravity.cli-test/not-a-test"]))
             (catch clojure.lang.ExceptionInfo error error)))))))

(deftest iteration-cache-test-var-rejects-refers-and-reports-failures
  (require 'gravity.cli-test 'gravity.diagnostics-test)
  (let [cli-namespace (find-ns 'gravity.cli-test)]
    (binding [*ns* cli-namespace]
      (refer 'gravity.diagnostics-test
             :only '[base-exception-info-carrier-is-extracted-with-bootstrap-parity]
             :rename
             '{base-exception-info-carrier-is-extracted-with-bootstrap-parity
               borrowed-cross-owner-test}))
    (try
      (is (= "SH07-ITERATION-CACHE-TEST-VAR"
             (:id
              (ex-data
               (try
                 (runner/run-selection
                  (runner/parse-arguments
                   ["--test-var"
                    "gravity.cli-test/borrowed-cross-owner-test"]))
                 (catch clojure.lang.ExceptionInfo error error))))))
      (finally
        (ns-unmap cli-namespace 'borrowed-cross-owner-test)))
    (let [failing-var (intern cli-namespace 'synthetic-failing-test)]
      (alter-meta!
       failing-var assoc :test
       #(clojure.test/do-report
         {:type :fail
          :message "synthetic failure"
          :expected true
          :actual false}))
      (try
        (let [output (java.io.StringWriter.)
              result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (runner/run-selection
                 (runner/parse-arguments
                  ["--test-var"
                   "gravity.cli-test/synthetic-failing-test"])))]
          (is (false? (:ok? result)))
          (is (= 1 (get-in result [:test-result :fail])))
          (is (zero? (get-in result [:test-result :error])))
          (is (= :non-authoritative (:authority result))))
        (finally
          (ns-unmap cli-namespace 'synthetic-failing-test))))))

(deftest iteration-cache-batches-exact-vars-with-one-bounded-cache
  (require 'gravity.cli-test)
  (let [namespace-object (find-ns 'gravity.cli-test)
        original-namespace-meta (meta namespace-object)
        first-symbol 'synthetic-batch-first-test
        second-symbol 'synthetic-batch-second-test
        first-var (intern namespace-object first-symbol)
        second-var (intern namespace-object second-symbol)
        calls (atom 0)
        once-calls (atom 0)
        each-calls (atom 0)
        operation
        #(bootstrap/sh06-resolution-source-artifact "same" "content")]
    (try
      (alter-meta!
       first-var assoc :test
       #(do (operation)
            (clojure.test/do-report
             {:type :pass :expected true :actual true})))
      (alter-meta!
       second-var assoc :test
       #(do (operation)
            (clojure.test/do-report
             {:type :pass :expected true :actual true})))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [operation] (swap! once-calls inc) (operation))]
       :clojure.test/each-fixtures
       [(fn [operation] (swap! each-calls inc) (operation))])
      (with-redefs
        [bootstrap/sh06-resolution-source-artifact
         (fn [path source]
           (swap! calls inc)
           [path source])]
        (let [selection
              (runner/parse-arguments
               ["--test-var" (str "gravity.cli-test/" first-symbol)
                "--test-var" (str "gravity.cli-test/" second-symbol)
                "--max-cache-entries" "1"])
              output (java.io.StringWriter.)
              result
              (binding [*out* output
                        clojure.test/*test-out* output]
                (runner/run-selection selection))]
          (is (true? (:ok? result)))
          (is (= :exact-test-vars (:selection-mode result)))
          (is (= [(symbol "gravity.cli-test" (str first-symbol))
                  (symbol "gravity.cli-test" (str second-symbol))]
                 (:test-vars result)))
          (is (= [0 1]
                 (mapv :selection-index (:test-var-results result))))
          (is (= 1 @calls))
          (is (= 2 @once-calls @each-calls))
          (is (= {:sh06-hits 1 :sh06-misses 1}
                 (select-keys (:cache result)
                              [:sh06-hits :sh06-misses])))
          (is (= [{:sh06-hits 0 :sh06-misses 1}
                  {:sh06-hits 1 :sh06-misses 0}]
                 (mapv #(select-keys (:cache %)
                                     [:sh06-hits :sh06-misses])
                       (:test-var-results result))))
          (is (= {:test 2 :pass 2 :fail 0 :error 0 :type :summary}
                 (:test-result result)))
          (is (every? :completed? (:test-var-results result)))
          (is (every? #(= :non-authoritative (:authority %))
                      (:test-var-results result)))
          (is (= 2
                 (count
                  (re-seq #":gravity/sh07-iteration-test-var-result"
                          (str output)))))))
      (finally
        (reset-meta! namespace-object original-namespace-meta)
        (ns-unmap namespace-object first-symbol)
        (ns-unmap namespace-object second-symbol)))))

(deftest iteration-cache-test-var-batch-fail-fast-skips-ordered-tail
  (require 'gravity.cli-test)
  (let [namespace-object (find-ns 'gravity.cli-test)
        symbols '[synthetic-batch-failing-test
                  synthetic-batch-skipped-test]
        test-vars (mapv #(intern namespace-object %) symbols)
        executed (atom [])]
    (try
      (alter-meta!
       (first test-vars) assoc :test
       #(do
          (swap! executed conj (first symbols))
          (clojure.test/do-report
           {:type :fail :message "batch failure"
            :expected true :actual false})))
      (alter-meta!
       (second test-vars) assoc :test
       #(do
          (swap! executed conj (second symbols))
          (clojure.test/do-report
           {:type :pass :expected true :actual true})))
      (let [qualified (mapv #(symbol "gravity.cli-test" (str %)) symbols)
            result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (runner/run-selection
               (runner/parse-arguments
                ["--fail-fast"
                 "--test-var" (str (first qualified))
                 "--test-var" (str (second qualified))])))]
        (is (= [(first symbols)] @executed))
        (is (false? (:ok? result)))
        (is (true? (:stopped-early? result)))
        (is (= [(second qualified)] (:skipped-test-vars result)))
        (is (= [(first qualified)]
               (mapv :test-var (:test-var-results result))))
        (is (= {:test 1 :pass 0 :fail 1 :error 0 :type :summary}
               (:test-result result)))
        (reset! executed [])
        (let [full-result
              (binding [*out* (java.io.StringWriter.)
                        clojure.test/*test-out* (java.io.StringWriter.)]
                (runner/run-selection
                 (runner/parse-arguments
                  ["--test-var" (str (first qualified))
                   "--test-var" (str (second qualified))])))]
          (is (= symbols @executed))
          (is (false? (:stopped-early? full-result)))
          (is (= [] (:skipped-test-vars full-result)))
          (is (= {:test 2 :pass 1 :fail 1 :error 0 :type :summary}
                 (:test-result full-result)))))
      (finally
        (doseq [symbol symbols]
          (ns-unmap namespace-object symbol))))))

(deftest iteration-cache-test-var-batch-preserves-cross-namespace-order
  (require 'gravity.cli-test 'gravity.diagnostics-test)
  (let [cli-namespace (find-ns 'gravity.cli-test)
        diagnostics-namespace (find-ns 'gravity.diagnostics-test)
        cli-symbol 'synthetic-cross-namespace-cli-test
        diagnostics-symbol 'synthetic-cross-namespace-diagnostics-test
        cli-var (intern cli-namespace cli-symbol)
        diagnostics-var (intern diagnostics-namespace diagnostics-symbol)
        cli-qualified (symbol "gravity.cli-test" (str cli-symbol))
        diagnostics-qualified
        (symbol "gravity.diagnostics-test" (str diagnostics-symbol))
        executed (atom [])]
    (try
      (doseq [[test-var marker]
              [[cli-var cli-qualified]
               [diagnostics-var diagnostics-qualified]]]
        (alter-meta!
         test-var assoc :test
         #(do
            (swap! executed conj marker)
            (clojure.test/do-report
             {:type :pass :expected true :actual true}))))
      (let [result
            (binding [*out* (java.io.StringWriter.)
                      clojure.test/*test-out* (java.io.StringWriter.)]
              (runner/run-selection
               (runner/parse-arguments
                ["--test-var" (str diagnostics-qualified)
                 "--test-var" (str cli-qualified)])))]
        (is (= [diagnostics-qualified cli-qualified] @executed))
        (is (= '[gravity.diagnostics-test gravity.cli-test]
               (:namespaces result)))
        (is (= [diagnostics-qualified cli-qualified]
               (mapv :test-var (:test-var-results result)))))
      (reset! executed [])
      (let [data
            (try
              (runner/run-test-vars
               {:test-vars
                [cli-qualified
                 'gravity.cli-test/absent-batch-test]
                :maximum-entries 1
                :fail-fast? false})
              nil
              (catch clojure.lang.ExceptionInfo error
                (ex-data error)))]
        (is (= "SH07-ITERATION-CACHE-TEST-VAR" (:id data)))
        (is (= [] @executed)))
      (finally
        (ns-unmap cli-namespace cli-symbol)
        (ns-unmap diagnostics-namespace diagnostics-symbol)))))

(deftest iteration-cache-test-var-batch-retains-fixture-exception-telemetry
  (require 'gravity.cli-test)
  (let [namespace-object (find-ns 'gravity.cli-test)
        original-namespace-meta (meta namespace-object)
        symbols '[synthetic-batch-fixture-error-test
                  synthetic-batch-after-fixture-error-test]
        test-vars (mapv #(intern namespace-object %) symbols)
        qualified (mapv #(symbol "gravity.cli-test" (str %)) symbols)]
    (try
      (doseq [test-var test-vars]
        (alter-meta!
         test-var assoc :test
         #(clojure.test/do-report
           {:type :pass :expected true :actual true})))
      (alter-meta!
       namespace-object assoc
       :clojure.test/once-fixtures
       [(fn [_operation]
          (throw
           (ex-info "batch fixture failed"
                    {:id "BATCH-FIXTURE-FAILED"})))])
      (let [output (java.io.StringWriter.)
            data
            (binding [*out* output
                      clojure.test/*test-out* output]
              (try
                (runner/run-selection
                 (runner/parse-arguments
                  ["--fail-fast"
                   "--test-var" (str (first qualified))
                   "--test-var" (str (second qualified))]))
                nil
                (catch clojure.lang.ExceptionInfo error
                  (ex-data error))))
            rendered (str output)]
        (is (= "BATCH-FIXTURE-FAILED" (:id data)))
        (is (= 1
               (count
                (re-seq #":gravity/sh07-iteration-test-var-result"
                        rendered))))
        (is (re-find #":completed\? false" rendered))
        (is (re-find
             #":skipped-test-vars \[gravity\.cli-test/synthetic-batch-after-fixture-error-test\]"
             rendered))
        (is (re-find #":authority :non-authoritative" rendered)))
      (finally
        (reset-meta! namespace-object original-namespace-meta)
        (doseq [symbol symbols]
          (ns-unmap namespace-object symbol))))))

(deftest argument-selection-is-explicit-and-owned
  (testing "repeatable namespaces and cache bound"
    (is (= {:namespaces '[gravity.diagnostics-test gravity.cli-test]
            :maximum-entries 2
            :fail-fast? false
            :test-var nil}
           (runner/parse-arguments
            ["--namespace" "gravity.diagnostics-test"
             "--max-cache-entries" "2"
             "--namespace" "gravity.cli-test"]))))
  (testing "fail-fast is explicit and order independent"
    (is (= true
           (:fail-fast?
            (runner/parse-arguments
             ["--fail-fast"
              "--namespace" "gravity.cli-test"])))))
  (testing "owned namespace-qualified test vars preserve input order"
    (is (= 'gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity
           (:test-var
            (runner/parse-arguments
             ["--test-var"
              "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"]))))
    (is (= "SH07-ITERATION-CACHE-SELECTION-CONFLICT"
           (:id
            (ex-data
             (try
               (runner/parse-arguments
               ["--namespace" "gravity.cli-test"
                 "--test-var"
                 "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"])
               (catch clojure.lang.ExceptionInfo error error))))))
    (let [left
          'gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity
          right
          'gravity.cli-test/cli-namespace-contract-is-narrow-and-acyclic
          selection
          (runner/parse-arguments
           ["--fail-fast"
            "--test-var" (str left)
            "--test-var" (str right)])]
      (is (nil? (:test-var selection)))
      (is (= [left right] (:test-vars selection)))
      (is (true? (:fail-fast? selection))))
    (doseq [[arguments expected-id]
            [[["--test-var" "unqualified-test"]
              "SH07-ITERATION-CACHE-TEST-VAR"]
             [["--test-var"
               "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"
               "--test-var"
               "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"]
              "SH07-ITERATION-CACHE-DUPLICATE-TEST-VAR"]
             [["--fail-fast"
               "--test-var"
               "gravity.cli-test/presentation-values-are-extracted-with-bootstrap-parity"]
              "SH07-ITERATION-CACHE-FAIL-FAST-SELECTION"]]]
      (is (= expected-id
             (:id
              (ex-data
               (try
                 (runner/parse-arguments arguments)
                 (catch clojure.lang.ExceptionInfo error error))))))))
  (testing "zero work and invalid bounds fail closed"
    (is (= "SH07-ITERATION-CACHE-SELECTION"
           (:id (ex-data (try (runner/parse-arguments [])
                              (catch clojure.lang.ExceptionInfo error
                                error))))))
    (is (= "SH07-ITERATION-CACHE-ARGUMENT"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--max-cache-entries" "0"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "unowned namespaces are rejected by the coordinator"
    (is (= "SH01-TEST-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                   ["--namespace" "gravity.not-owned-test"])
                   (catch clojure.lang.ExceptionInfo error error)))))))
  (testing "duplicate namespace work is rejected"
    (is (= "SH07-ITERATION-CACHE-DUPLICATE-NAMESPACE"
           (:id (ex-data
                 (try
                   (runner/parse-arguments
                    ["--namespace" "gravity.cli-test"
                     "--namespace" "gravity.cli-test"])
                   (catch clojure.lang.ExceptionInfo error error))))))))
