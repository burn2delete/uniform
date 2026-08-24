(ns gravity.self-hosting.sh01-host-resource-broker-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is testing]]
            [gravity.self-hosting.sh01-host-resource-broker :as broker])
  (:import (java.nio.charset StandardCharsets)
           (java.nio.file Files LinkOption OpenOption Path StandardOpenOption)
           (java.nio.file.attribute FileAttribute PosixFilePermissions)
           (java.util.concurrent TimeUnit)))

(def ^:private no-follow-links
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(defn- private-root
  []
  (Files/createTempDirectory
   "gravity-sh01-broker-test-"
   (into-array
    FileAttribute
    [(PosixFilePermissions/asFileAttribute
      (PosixFilePermissions/fromString "rwx------"))])))

(defn- delete-tree!
  [^Path root]
  (when (Files/exists root no-follow-links)
    (with-open [stream (Files/walk
                        root (make-array java.nio.file.FileVisitOption 0))]
      (doseq [file (reverse (sort-by #(.getNameCount ^Path %)
                                    (iterator-seq (.iterator stream))))]
        (Files/deleteIfExists file)))))

(defn- write-edn!
  [^Path path value]
  (Files/write path
               (.getBytes (str (pr-str value) "\n")
                          StandardCharsets/UTF_8)
               (into-array OpenOption
                           [StandardOpenOption/CREATE
                            StandardOpenOption/TRUNCATE_EXISTING
                            StandardOpenOption/WRITE])))

(defn- read-edn
  [^Path path]
  (when (Files/isRegularFile path no-follow-links)
    (try
      (edn/read-string
       (String. (Files/readAllBytes path) StandardCharsets/UTF_8))
      (catch Throwable _ nil))))

(defn- wait-for-result
  [^Path result-file predicate timeout-ms]
  (let [deadline (+ (System/nanoTime)
                    (.toNanos TimeUnit/MILLISECONDS timeout-ms))]
    (loop []
      (let [result (read-edn result-file)]
        (cond
          (predicate result) result
          (not (pos? (- deadline (System/nanoTime))))
          (throw (ex-info "Timed out waiting for broker helper"
                          {:result-file (str result-file)
                           :last-result result}))
          :else (do (Thread/sleep 10) (recur)))))))

(defn- helper-command
  [root resource-class result-file release-file timeout-ms]
  [(str (Path/of (System/getProperty "java.home")
                 (into-array String ["bin" "java"])))
   "-Xmx256m"
   "-cp" (System/getProperty "java.class.path")
   "clojure.main" "-m"
   "gravity.self-hosting.sh01-host-resource-broker-test"
   "--helper" (str root) (name resource-class) (str result-file)
   (str release-file) (str timeout-ms)])

(defn- start-helper!
  [root resource-class result-file release-file timeout-ms]
  (-> (ProcessBuilder. ^java.util.List
                       (helper-command root resource-class result-file
                                       release-file timeout-ms))
      (.redirectErrorStream true)
      (.redirectOutput (java.io.File. (str result-file ".log")))
      (.start)))

(defn- stop-helper!
  [^Process process]
  (when (.isAlive process)
    (.destroyForcibly process))
  (.waitFor process 5 TimeUnit/SECONDS)
  process)

(defn- release-helper!
  [^Path release-file]
  (Files/createFile release-file (make-array FileAttribute 0)))

(defn- helper-main
  [[root class-name result-file release-file timeout-ms]]
  (let [root (Path/of root (make-array String 0))
        result-file (Path/of result-file (make-array String 0))
        release-file (Path/of release-file (make-array String 0))
        resource-class (keyword class-name)
        started (System/nanoTime)
        events (atom [])]
    (try
      (let [lease
            (broker/acquire!
             {:coordination-root root
              :timeout-ms (parse-long timeout-ms)
              :on-event
              (fn [event]
                (swap! events conj event)
                (when (= :queued (:outcome event))
                  (write-edn! result-file
                              {:status :queued
                               :resource-class resource-class
                               :event event})))}
             resource-class)]
        (write-edn! result-file
                    {:status :acquired
                     :resource-class resource-class
                     :receipt (:receipt lease)
                     :telemetry (:telemetry lease)
                     :events @events})
        (let [deadline (+ (System/nanoTime)
                          (.toNanos TimeUnit/SECONDS 15))]
          (loop []
            (when-not (Files/exists release-file no-follow-links)
              (when-not (pos? (- deadline (System/nanoTime)))
                (throw (ex-info "helper release deadline expired"
                                {:id "SH01-BROKER-HELPER-TIMEOUT"})))
              (Thread/sleep 10)
              (recur))))
        (let [receipt (broker/release! lease)]
          (write-edn! result-file
                      {:status :released
                       :resource-class resource-class
                       :receipt receipt
                       :events @events}))
        0)
      (catch Throwable throwable
        (write-edn! result-file
                    {:status :error
                     :resource-class resource-class
                     :diagnostic-id (:id (ex-data throwable))
                     :receipt (:receipt (ex-data throwable))
                     :message (.getMessage throwable)
                     :elapsed-ms
                     (long (/ (- (System/nanoTime) started) 1000000.0))
                     :events @events})
        1))))

(defn -main
  [& arguments]
  (if (= "--helper" (first arguments))
    (System/exit (helper-main (rest arguments)))
    (System/exit 2)))

(deftest explicit-root-policy-and-stable-diagnostics
  (let [root (private-root)]
    (try
      (let [lease (broker/acquire! {:coordination-root root :timeout-ms 1000}
                                   :normal)]
        (try
          (is (= {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
                  :resource-class :normal
                  :capacity 2
                  :outcome :admitted
                  :diagnostic-id nil}
                 (:receipt lease)))
          (is (= :gravity/sh01-host-resource-telemetry-v1
                 (get-in lease [:telemetry :schema])))
          (finally (broker/release! lease))))
      (testing "an unknown class and an implicit root fail closed"
        (is (= "SH01-BROKER-RESOURCE-CLASS"
               (:id (ex-data
                     (try
                       (broker/acquire! {:coordination-root root} :unknown)
                       (catch clojure.lang.ExceptionInfo error error))))))
        (is (= "SH01-BROKER-ROOT-REQUIRED"
               (:id (ex-data
                     (try
                       (broker/acquire! {:timeout-ms 10} :normal)
                       (catch clojure.lang.ExceptionInfo error error)))))))
      (testing "a relative root is rejected"
        (is (= "SH01-BROKER-ROOT-ABSOLUTE"
               (:id (ex-data
                     (try
                       (broker/acquire! {:coordination-root "relative"
                                         :timeout-ms 10}
                                        :normal)
                       (catch clojure.lang.ExceptionInfo error error)))))))
      (testing "the reviewed policy must be exactly one EDN form"
        (Files/write (.resolve root "policy.edn")
                     (.getBytes "{:trailing true}\n" StandardCharsets/UTF_8)
                     (into-array OpenOption [StandardOpenOption/APPEND]))
        (is (= "SH01-BROKER-POLICY-MISMATCH"
               (:id (ex-data
                     (try
                       (broker/acquire! {:coordination-root root
                                         :timeout-ms 1000}
                                        :normal)
                       (catch clojure.lang.ExceptionInfo error error)))))))
      (testing "a mismatched reviewed policy is rejected"
        (write-edn! (.resolve root "policy.edn") {:capacities {:normal 99}})
        (is (= "SH01-BROKER-POLICY-MISMATCH"
               (:id (ex-data
                     (try
                       (broker/acquire! {:coordination-root root
                                         :timeout-ms 1000}
                                        :normal)
                       (catch clojure.lang.ExceptionInfo error error)))))))
      (finally
        (delete-tree! root)))))

(deftest unsafe-root-state-and-double-release-fail-closed
  (let [container (private-root)
        target (.resolve container "target")
        link (.resolve container "link")]
    (try
      (Files/createDirectory
       target
       (into-array
        FileAttribute
        [(PosixFilePermissions/asFileAttribute
          (PosixFilePermissions/fromString "rwx------"))]))
      (Files/createSymbolicLink link target (make-array FileAttribute 0))
      (is (= "SH01-BROKER-ROOT-INVALID"
             (:id (ex-data
                   (try
                     (broker/acquire! {:coordination-root link
                                       :timeout-ms 1000}
                                      :normal)
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/setAttribute target "unix:mode" (int 960) no-follow-links)
      (is (= "SH01-BROKER-ROOT-PERMISSIONS"
             (:id (ex-data
                   (try
                     (broker/acquire! {:coordination-root target
                                       :timeout-ms 1000}
                                      :normal)
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/setAttribute target "unix:mode" (int 448) no-follow-links)
      (Files/createDirectory
       (.resolve target "slot-normal-00.lock")
       (make-array FileAttribute 0))
      (is (= "SH01-BROKER-STATE-CORRUPT"
             (:id (ex-data
                   (try
                     (broker/acquire! {:coordination-root target
                                       :timeout-ms 1000}
                                      :normal)
                     (catch clojure.lang.ExceptionInfo error error))))))
      (Files/delete (.resolve target "slot-normal-00.lock"))
      (let [lease (broker/acquire! {:coordination-root target
                                    :timeout-ms 1000}
                                   :normal)
            wrong-thread
            (deref
             (future
               (try
                 (broker/release! lease)
                 (catch clojure.lang.ExceptionInfo error error)))
             1000 ::timeout)]
        (is (= "SH01-BROKER-RELEASE" (:id (ex-data wrong-thread))))
        (is (= :released (:outcome (broker/release! lease))))
        (is (= "SH01-BROKER-RELEASE"
               (:id (ex-data
                     (try
                       (broker/release! lease)
                       (catch clojure.lang.ExceptionInfo error error)))))))
      (finally
        (delete-tree! container)))))

(deftest admission-cleanup-failure-releases-acquired-capacity
  (let [root (private-root)
        close-ticket-var
        (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker
                    'close-ticket!)
        close-ticket @close-ticket-var]
    (try
      (let [error
            (with-redefs-fn
              {close-ticket-var
               (fn [ticket delete?]
                 (close-ticket ticket false)
                 (when delete?
                   (throw
                    (ex-info "injected ticket deletion failure"
                             {:id "SH01-BROKER-INJECTED-TICKET"}))))}
              #(try
                 (broker/acquire! {:coordination-root root :timeout-ms 1000}
                                  :memory-heavy)
                 nil
                 (catch clojure.lang.ExceptionInfo thrown thrown)))]
        (is (= "SH01-BROKER-INJECTED-TICKET" (:id (ex-data error)))))
      ;; Memory-heavy capacity is one. A leaked slot would make this recovery
      ;; admission time out after the injected post-slot ticket failure.
      (let [lease (broker/acquire! {:coordination-root root :timeout-ms 1000}
                                   :memory-heavy)]
        (is (= :admitted (get-in lease [:receipt :outcome])))
        (is (= :released (:outcome (broker/release! lease)))))
      (finally
        (delete-tree! root)))))

(deftest partial-release-failure-restores-a-retryable-linear-handle
  (let [root (private-root)
        release-slots-var
        (ns-resolve 'gravity.self-hosting.sh01-host-resource-broker
                    'release-slots!)
        release-slots @release-slots-var]
    (try
      (let [lease (broker/acquire! {:coordination-root root :timeout-ms 1000}
                                   :memory-heavy)]
        (try
          (let [error
                (with-redefs-fn
                  {release-slots-var
                   (fn [slots]
                     (release-slots slots)
                     (throw
                      (ex-info "injected post-release failure"
                               {:id "SH01-BROKER-INJECTED-RELEASE"})))}
                  #(try
                     (broker/release! lease)
                     nil
                     (catch clojure.lang.ExceptionInfo thrown thrown)))]
            (is (= "SH01-BROKER-INJECTED-RELEASE" (:id (ex-data error))))
            (is (false? @(:released? lease)))
            (is (= :released (:outcome (broker/release! lease))))
            (is (true? @(:released? lease))))
          (finally
            (when (false? @(:released? lease))
              (broker/release! lease)))))
      (finally
        (delete-tree! root)))))

(deftest interrupted-waiter-cleans-its-ticket-and-preserves-interrupt-status
  (let [root (private-root)]
    (try
      (let [holder (broker/acquire! {:coordination-root root :timeout-ms 1000}
                                    :memory-heavy)
            queued (promise)
            result (promise)
            waiter
            (Thread.
             (fn []
               (try
                 (broker/acquire!
                  {:coordination-root root
                   :timeout-ms 5000
                   :on-event #(when (= :queued (:outcome %))
                                (deliver queued true))}
                  :memory-heavy)
                 (deliver result {:unexpected :admitted})
                 (catch clojure.lang.ExceptionInfo error
                   (deliver result
                            {:data (ex-data error)
                             :interrupted?
                             (.isInterrupted (Thread/currentThread))}))))) ]
        (try
          (.start waiter)
          (is (= true (deref queued 2000 :timeout)))
          (.interrupt waiter)
          (.join waiter 2000)
          (is (not (.isAlive waiter)))
          (let [{:keys [data interrupted?]} (deref result 1000 :timeout)]
            (is (= "SH01-BROKER-INTERRUPTED" (:id data)))
            (is (= "SH01-BROKER-INTERRUPTED"
                   (get-in data [:receipt :diagnostic-id])))
            (is interrupted?))
          (finally
            (broker/release! holder)))
        ;; The interrupted ticket is stale and must not block the next lease.
        (let [recovery (broker/acquire! {:coordination-root root
                                        :timeout-ms 1000}
                                       :memory-heavy)]
          (is (= :released (:outcome (broker/release! recovery))))))
      (finally
        (delete-tree! root)))))

(deftest multi-process-memory-capacity-is-one-and-recovers-after-crash
  (let [root (private-root)
        a-result (.resolve root "a.edn")
        a-release (.resolve root "a.release")
        b-result (.resolve root "b.edn")
        b-release (.resolve root "b.release")
        c-result (.resolve root "c.edn")
        c-release (.resolve root "c.release")
        queued-result (.resolve root "queued.edn")
        queued-release (.resolve root "queued.release")
        a (start-helper! root :memory-heavy a-result a-release 5000)]
    (try
      (let [a-observed (wait-for-result a-result #(= :acquired (:status %)) 8000)]
        (is (= :non-authoritative
               (get-in a-observed [:telemetry :authority])))
        (is (false? (get-in a-observed [:telemetry :authoritative?]))))
      (let [b (start-helper! root :memory-heavy b-result b-release 250)]
        (try
          (is (= "SH01-BROKER-TIMEOUT"
                 (:diagnostic-id
                  (wait-for-result b-result #(= :error (:status %)) 8000))))
          (let [timeout-result (read-edn b-result)]
            (is (< (:elapsed-ms timeout-result) 2000))
            (is (= {:schema :gravity/sh01-host-resource-non-authoritative-receipt-v1
                    :resource-class :memory-heavy
                    :capacity 1
                    :outcome :rejected
                    :diagnostic-id "SH01-BROKER-TIMEOUT"}
                   (:receipt timeout-result))))
          (is (.waitFor b 5 TimeUnit/SECONDS))
          (finally (stop-helper! b))))
      (let [queued (start-helper! root :memory-heavy queued-result
                                  queued-release 5000)]
        (wait-for-result queued-result #(= :queued (:status %)) 8000)
        (stop-helper! queued))
      ;; Hard process death releases the active capacity slot; the next
      ;; admission also proves and reclaims the abandoned queued ticket.
      (stop-helper! a)
      (let [c (start-helper! root :memory-heavy c-result c-release 3000)]
        (try
          (let [observed
                (wait-for-result c-result #(= :acquired (:status %)) 8000)]
            (is (= 1 (get-in observed [:telemetry :capacity])))
            (is (pos? (get-in observed
                              [:telemetry :recovered-stale-tickets])))
            (is (some #(= :stale-recovered (:outcome %))
                      (:events observed))))
          (release-helper! c-release)
          (is (= :released
                 (:status
                  (wait-for-result c-result #(= :released (:status %)) 5000))))
          (finally (stop-helper! c))))
      (finally
        (stop-helper! a)
        (delete-tree! root)))))

(deftest queued-admission-revalidates-policy-before-granting-a-slot
  (let [root (private-root)
        holder-result (.resolve root "holder.edn")
        holder-release (.resolve root "holder.release")
        waiter-result (.resolve root "waiter.edn")
        waiter-release (.resolve root "waiter.release")
        holder (start-helper! root :memory-heavy holder-result holder-release 5000)]
    (try
      (wait-for-result holder-result #(= :acquired (:status %)) 8000)
      (let [waiter (start-helper! root :memory-heavy waiter-result
                                  waiter-release 5000)]
        (try
          (wait-for-result waiter-result #(= :queued (:status %)) 8000)
          (write-edn! (.resolve root "policy.edn")
                      {:schema :gravity/sh01-host-resource-policy-v1
                       :capacities {:normal 99 :memory-heavy 1 :exclusive 1}})
          (release-helper! holder-release)
          (let [observed
                (wait-for-result waiter-result #(= :error (:status %)) 8000)]
            (is (= "SH01-BROKER-POLICY-MISMATCH"
                   (:diagnostic-id observed)))
            (is (= "SH01-BROKER-POLICY-MISMATCH"
                   (get-in observed [:receipt :diagnostic-id]))))
          (finally (stop-helper! waiter))))
      (finally
        (when-not (Files/exists holder-release no-follow-links)
          (release-helper! holder-release))
        (stop-helper! holder)
        (delete-tree! root)))))

(deftest semantic-receipts-are-deterministic-and-observation-free
  (let [left-root (private-root)
        right-root (private-root)]
    (try
      (let [left (broker/acquire! {:coordination-root left-root
                                   :timeout-ms 1000}
                                  :normal)
            right (broker/acquire! {:coordination-root right-root
                                    :timeout-ms 1000}
                                   :normal)]
        (try
          (is (= (:receipt left) (:receipt right)))
          (is (= #{:schema :resource-class :capacity :outcome :diagnostic-id}
                 (set (keys (:receipt left)))))
          (is (= (:coordination-root (:telemetry left)) (str left-root)))
          (is (= (:coordination-root (:telemetry right)) (str right-root)))
          (is (not= (:coordination-root (:telemetry left))
                    (:coordination-root (:telemetry right))))
          (finally
            (broker/release! left)
            (broker/release! right))))
      (finally
        (delete-tree! left-root)
        (delete-tree! right-root)))))

(deftest multi-process-normal-capacity-is-two-and-independent-of-heavy
  (let [root (private-root)
        paths (into {}
                    (for [id ["a" "b" "c" "heavy"]]
                      [id {:result (.resolve root (str id ".edn"))
                           :release (.resolve root (str id ".release"))}]))
        start (fn [id class timeout]
                (let [{:keys [result release]} (get paths id)]
                  (start-helper! root class result release timeout)))
        a (start "a" :normal 5000)
        b (start "b" :normal 5000)]
    (try
      (is (= :acquired
             (:status (wait-for-result (get-in paths ["a" :result])
                                       #(= :acquired (:status %)) 8000))))
      (is (= :acquired
             (:status (wait-for-result (get-in paths ["b" :result])
                                       #(= :acquired (:status %)) 8000))))
      (let [c (start "c" :normal 250)
            heavy (start "heavy" :memory-heavy 3000)]
        (try
          (is (= "SH01-BROKER-TIMEOUT"
                 (:diagnostic-id
                  (wait-for-result (get-in paths ["c" :result])
                                   #(= :error (:status %)) 8000))))
          (let [heavy-observed
                (wait-for-result (get-in paths ["heavy" :result])
                                 #(= :acquired (:status %)) 8000)]
            (is (= 2 (get-in
                      (read-edn (get-in paths ["a" :result]))
                      [:telemetry :capacity])))
            (is (= :acquired (:status heavy-observed))))
          (release-helper! (get-in paths ["heavy" :release]))
          (finally
            (stop-helper! c)
            (stop-helper! heavy))))
      (finally
        (doseq [id ["a" "b"]]
          (when-not (Files/exists (get-in paths [id :release]) no-follow-links)
            (release-helper! (get-in paths [id :release]))))
        (stop-helper! a)
        (stop-helper! b)
        (delete-tree! root)))))

(deftest exclusive-lease-excludes-all-host-resource-classes
  (let [root (private-root)
        exclusive-result (.resolve root "exclusive.edn")
        exclusive-release (.resolve root "exclusive.release")
        exclusive (start-helper! root :exclusive exclusive-result
                                 exclusive-release 5000)]
    (try
      (is (= :acquired
             (:status (wait-for-result exclusive-result
                                       #(= :acquired (:status %)) 8000))))
      (doseq [resource-class [:normal :memory-heavy]]
        (let [stem (name resource-class)
              result (.resolve root (str stem ".edn"))
              release (.resolve root (str stem ".release"))
              process (start-helper! root resource-class result release 250)]
          (try
            (is (= "SH01-BROKER-TIMEOUT"
                   (:diagnostic-id
                    (wait-for-result result #(= :error (:status %)) 8000))))
            (finally (stop-helper! process)))))
      (finally
        (when-not (Files/exists exclusive-release no-follow-links)
          (release-helper! exclusive-release))
        (stop-helper! exclusive)
        (delete-tree! root)))))

(deftest queued-admission-is-deterministic-within-a-resource-class
  (let [root (private-root)
        paths (into {}
                    (for [id ["holder" "first" "second"]]
                      [id {:result (.resolve root (str id ".edn"))
                           :release (.resolve root (str id ".release"))}]))
        start (fn [id]
                (start-helper! root :memory-heavy
                               (get-in paths [id :result])
                               (get-in paths [id :release]) 10000))
        holder (start "holder")]
    (try
      (wait-for-result (get-in paths ["holder" :result])
                       #(= :acquired (:status %)) 8000)
      (let [first-process (start "first")]
        (try
          (wait-for-result (get-in paths ["first" :result])
                           #(= :queued (:status %)) 8000)
          (let [second-process (start "second")]
            (try
              (wait-for-result (get-in paths ["second" :result])
                               #(= :queued (:status %)) 8000)
              (release-helper! (get-in paths ["holder" :release]))
              (is (= :acquired
                     (:status
                      (wait-for-result (get-in paths ["first" :result])
                                       #(= :acquired (:status %)) 8000))))
              (is (= :queued
                     (:status (read-edn (get-in paths ["second" :result]))))
                  "the later class-local ticket cannot bypass the first")
              (release-helper! (get-in paths ["first" :release]))
              (is (= :acquired
                     (:status
                      (wait-for-result (get-in paths ["second" :result])
                                       #(= :acquired (:status %)) 8000))))
              (release-helper! (get-in paths ["second" :release]))
              (finally (stop-helper! second-process))))
          (finally (stop-helper! first-process))))
      (finally
        (doseq [id ["holder" "first" "second"]]
          (let [release (get-in paths [id :release])]
            (when-not (Files/exists release no-follow-links)
              (release-helper! release))))
        (stop-helper! holder)
        (delete-tree! root)))))

(deftest active-normal-and-heavy-work-block-exclusive-admission
  (let [root (private-root)
        normal-result (.resolve root "normal-holder.edn")
        normal-release (.resolve root "normal-holder.release")
        heavy-result (.resolve root "heavy-holder.edn")
        heavy-release (.resolve root "heavy-holder.release")
        exclusive-result (.resolve root "exclusive-waiter.edn")
        exclusive-release (.resolve root "exclusive-waiter.release")
        normal (start-helper! root :normal normal-result normal-release 5000)
        heavy (start-helper! root :memory-heavy heavy-result heavy-release 5000)]
    (try
      (wait-for-result normal-result #(= :acquired (:status %)) 8000)
      (wait-for-result heavy-result #(= :acquired (:status %)) 8000)
      (let [exclusive (start-helper! root :exclusive exclusive-result
                                     exclusive-release 250)]
        (try
          (is (= "SH01-BROKER-TIMEOUT"
                 (:diagnostic-id
                  (wait-for-result exclusive-result
                                   #(= :error (:status %)) 8000))))
          (finally (stop-helper! exclusive))))
      (finally
        (doseq [release [normal-release heavy-release]]
          (when-not (Files/exists release no-follow-links)
            (release-helper! release)))
        (stop-helper! normal)
        (stop-helper! heavy)
        (delete-tree! root)))))
