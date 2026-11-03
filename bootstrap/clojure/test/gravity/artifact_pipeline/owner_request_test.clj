(ns gravity.artifact-pipeline.owner-request-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is use-fixtures]]
            [gravity.artifact-pipeline.owner-request :as owner]
            [gravity.artifact-pipeline.parent-controller :as controller]))

(def watched-namespaces
  '#{gravity.bootstrap gravity.self-hosting.sh01-host-resource-broker
     gravity.self-hosting.sh01-parallel-test-runner gravity.development-test-runner})

(defn- namespace-names []
  (set (map ns-name (all-ns))))

(def operational-namespaces
  '[gravity.self-hosting.sh01-host-resource-broker
    gravity.self-hosting.sh01-parallel-test-runner])

(defn- with-preloaded-namespace [namespace f]
  (let [preexisting? (some? (find-ns namespace))
        namespace-object (or (find-ns namespace) (create-ns namespace))]
    (try
      (when-not preexisting?
        (intern namespace-object 'foreign-marker (fn [] :foreign)))
      (f)
      (finally
        (when-not preexisting?
          (remove-ns namespace))))))

(use-fixtures :each
  (fn [test]
    ;; Preload one watched namespace before the bookend. The fixture must
    ;; reject only new loads, not demand that an unrelated caller's namespace
    ;; was absent before this test.
    (when-not (find-ns 'gravity.bootstrap)
      (create-ns 'gravity.bootstrap))
    (let [before (namespace-names)]
      (try
        (test)
        (finally
          (let [after (namespace-names)
                newly-loaded (set/difference after before)]
            (is (empty? newly-loaded)
                (str "unexpected namespace loads: " (sort newly-loaded)))
            (is (= (select-keys (zipmap watched-namespaces (repeat true))
                                (filter watched-namespaces before))
                   (select-keys (zipmap watched-namespaces (repeat true))
                                (filter watched-namespaces after))))))))))

(defn fixture []
  (let [calls (atom []) lease (Object.) root-handle (Object.)
        token {:process :fake-process :terminal (atom nil) :wait-throwable (atom nil)}
        terminal {:status :complete :owned-descendants-complete? true :root-alive? false
                  :observed-pids [1] :tracked-complete? true :live-descendants-at-finalization 0
                  :survivor-pids [] :discovery-error nil :cleanup-error nil}
        request {:cwd "/fake/child" :argv controller/argv :outputs {:attempt "/fake/claim" :stdout "/fake/stdout"
                                                                    :stderr "/fake/stderr" :final "/fake/final"}
                 :guards [{:kind :broker :acquisition :broker :resource-class :exclusive :root "/fake/git-common/broker"}
                          {:kind :canonical :acquisition :non-broker :root owner/canonical-path}]}
        proposal {:request request :actual-child-command ["/fake/java" "-Xmx2g" "clojure.main"]}
        normal {:acquire (fn [& args] (swap! calls conj [:acquire args]) lease)
                :validate (fn [handle & args] (swap! calls conj [:validate args]) handle)
                :release (fn [handle] (is (identical? lease handle)) {:outcome :released :capacity 1N})
                :start (fn [& args] (swap! calls conj [:start args]) :fake-process)
                :retain (fn [process] (is (= :fake-process process)) token)
                :handles (fn [value] (is (identical? token value)) [root-handle])
                :finish (fn [value options]
                          (is (identical? token value))
                          (is (= 1500 (:timeout-ms options)))
                          terminal)
                :supervise (fn [& args]
                             (swap! calls conj [:supervise args])
                             {:status :passed :exit-code 0 :wait-interrupted? false
                              :output-truncated? false :capture-failed? false :cleanup-failed? false
                              :cleanup-root-alive? false :timeout-containment-unproven? false
                              :stdout "captured" :stderr "" :opaque lease})}]
    {:calls calls :lease lease :proposal proposal :ports (#'owner/assemble-ports proposal normal)
     :token token :terminal terminal :root-handle root-handle :normal normal}))

(deftest broker-ports-preserve-real-arity-root-class-and-raw-handle
  (let [{:keys [ports proposal lease calls]} (fixture) guard (get-in proposal [:request :guards 0])]
    (is (identical? lease ((:acquire ports) guard 123)))
    (is (identical? lease ((:validate ports) guard lease)))
    (is (= {:status :released :broker-receipt {:outcome :released :capacity 1N}} ((:release ports) guard lease)))
    (is (= [[:acquire (list {:coordination-root "/fake/git-common/broker" :timeout-ms 123} :exclusive)]
            [:validate (list "/fake/git-common/broker" :exclusive)]] @calls))))

(deftest canonical-dispatch-is-separate-and-retains-the-original-handle
  (let [{:keys [ports proposal calls]} (fixture) guard (get-in proposal [:request :guards 1]) handle (Object.)]
    (with-redefs [owner/canonical-acquire! (fn [] (swap! calls conj :canonical) handle)
                  owner/canonical-validate! (fn [value] (is (identical? handle value)) value)
                  owner/canonical-release! (fn [value] (is (identical? handle value)) {:status :released})]
      (is (identical? handle ((:acquire-guard ports) guard 100)))
      (is (identical? handle ((:validate ports) guard handle)))
      (is (= :released (:status ((:release ports) guard handle))))
      (is (= [:canonical] @calls))
      (is (thrown? clojure.lang.ExceptionInfo ((:acquire ports) guard 100)))
      (is (= [:canonical] @calls)))))

(deftest exact-start-arguments-and-source-observation-precede-launch
  (let [{:keys [ports proposal calls]} (fixture) observations (atom 0)]
    (with-redefs [owner/verify! (fn [value] (is (= proposal value)) (swap! observations inc))]
      (is (thrown? clojure.lang.ExceptionInfo ((:start ports) ["foreign"] "/fake/child" 100)))
      (is (thrown? clojure.lang.ExceptionInfo ((:start ports) controller/argv "/foreign" 100)))
      (is (empty? @calls)) (is (zero? @observations))
      (is (= :fake-process ((:start ports) controller/argv "/fake/child" 100)))
      (is (= 1 @observations))
      (is (= [[:start (list (:actual-child-command proposal) "/fake/child")]] @calls)))))

(deftest capture-option-mapping-and-publication-retain-raw-results
  (let [{:keys [ports calls lease token]} (fixture) writes (atom [])
        limits (assoc owner/proposed-limits :timeout-ms 20000)]
    (with-redefs [owner/verify! (constantly {})
                  owner/create-output! (fn [path bytes] (swap! writes conj [path (String. ^bytes bytes "UTF-8")]))]
      ((:start ports) controller/argv "/fake/child" 20000)
      (reset! calls [])
      (let [result ((:supervise ports) :fake-process limits)]
        (is (identical? lease (:opaque result)))
        (is (empty? @writes))
        (is (= [[:supervise (list :fake-process {:timeout-ms 8200 :max-capture-bytes 1048576
                                                 :max-capture-chars 1048576 :capture-wait-ms 5000}
                                                token)]] @calls))
        (is (= {:status :published} ((:publish ports) "/fake/final" {:status :failed})))
        (is (= [["/fake/stdout" "captured"] ["/fake/stderr" ""] ["/fake/final" "{:status :failed}\n"]] @writes))
        (is (thrown? clojure.lang.ExceptionInfo ((:supervise ports) :fake-process limits)))
        (is (thrown? clojure.lang.ExceptionInfo ((:publish ports) "/fake/final" {})))
        (is (= 3 (count @writes)))))))

(deftest insufficient-remaining-time-refuses-before-normal-supervision
  (let [{:keys [ports calls]} (fixture)]
    (with-redefs [owner/verify! (constantly {})]
      ((:start ports) controller/argv "/fake/child" 20000)
      (is (thrown? clojure.lang.ExceptionInfo ((:supervise ports) :fake-process {:timeout-ms 11800})))
      (is (= 1 (count @calls))))))

(deftest parent-acceptance-is-not-minted-by-proposal-data
  (let [{:keys [ports calls]} (fixture)
        error (try ((:admit ports) {:accepted true} {:accepted true} 100) nil
                   (catch clojure.lang.ExceptionInfo error error))]
    (is (= :own-profile-unaccepted (:reason (ex-data error))))
    (is (empty? @calls))))

(deftest finalizer-consumes-once-without-inventing-descendant-completeness
  (let [{:keys [proposal normal]} (fixture) calls (atom []) original (Exception. "original") cleanup (Object.)
        ports (#'owner/assemble-ports proposal (assoc normal :retain (fn [_] nil)))]
    (with-redefs [owner/verify! (constantly {})
                  owner/owned-finalizer (fn [& args] (swap! calls conj args) {:status :unknown})]
      ((:start ports) controller/argv "/fake/child" 20000)
      (is (= {:status :unknown} ((:finish ports) :fake-process {:cleanup cleanup} original {:cleanup-ms 1500})))
      (is (identical? cleanup (:cleanup (second (first @calls)))))
      (is (identical? original (nth (first @calls) 2)))
      (is (thrown? clojure.lang.ExceptionInfo ((:finish ports) :fake-process nil nil nil)))
      (is (= 1 (count @calls)))))
  (let [process (proxy [Process] [] (isAlive [] false)) cleanup (Object.)
        result (owner/owned-finalizer process {:cleanup cleanup} nil {})]
    (is (= :unknown (:status result)))
    (is (false? (:owned-descendants-complete? result)))
    (is (false? (:root-alive? result)))
    (is (identical? cleanup (:runner-cleanup result)))))

(deftest current-controller-broker-and-runner-source-abis-are-bound
  (is (= owner/controller-sha256
         (owner/file-hash "bootstrap/clojure/test/gravity/artifact_pipeline/parent_controller.clj")))
  (doseq [[resource digest] owner/normal-sources]
    (is (= digest (owner/file-hash (str "bootstrap/clojure/test/" resource)))))
  (is (= {:source-sha256 controller/runner-source-sha256
          :signatures controller/runner-signatures}
         (controller/verify-runner-contract!
          (slurp "bootstrap/clojure/test/gravity/self_hosting/sh01_parallel_test_runner.clj")
          controller/runner-signatures)))
  (is (= '([options resource-class]) (get owner/broker-signatures 'acquire!)))
  (is (= '([lease expected-root expected-class])
         (get owner/broker-signatures 'validate-live-lease!))))

(deftest retained-token-is-captured-before-release-and-finalizer-remains-unknown
  (let [{:keys [proposal normal token root-handle]} (fixture)
        finalizer-calls (atom 0)
        terminal {:status :complete :root-alive? false :tracked-complete? true}
        ports (#'owner/assemble-ports proposal
                (assoc normal :finish (fn [value _]
                                        (is (identical? token value))
                                        (swap! finalizer-calls inc)
                                        terminal)))]
    (with-redefs [owner/verify! (constantly {})]
      ((:start ports) controller/argv "/fake/child" 20000)
      (let [result ((:supervise ports) :fake-process (assoc owner/proposed-limits :timeout-ms 20000))
            finished ((:finish ports) :fake-process result nil {:cleanup-ms 1500})]
        (is (= [root-handle] (:retained-handles finished)))
        (is (= :unknown (:status finished)))
        (is (false? (:owned-descendants-complete? finished)))
        (is (= 1 @finalizer-calls))
        (is (thrown? clojure.lang.ExceptionInfo
                     ((:finish ports) :fake-process result nil {:cleanup-ms 1500})))))))

(deftest interrupted-wait-and-fatal-finalization-identity-are-not-rewritten
  (let [{:keys [proposal normal token]} (fixture)
        interrupted (InterruptedException. "wait interrupted")
        ports (#'owner/assemble-ports proposal
                (assoc normal :supervise
                       (fn [_ _ value]
                         (reset! (:wait-throwable value) interrupted)
                         {:status :error :exit-code 1 :wait-interrupted? true
                          :wait-error "runner text" :output-truncated? false
                          :capture-failed? false :cleanup-failed? false
                          :cleanup-root-alive? false :timeout-containment-unproven? false})))]
    (try
      (with-redefs [owner/verify! (constantly {})]
        ((:start ports) controller/argv "/fake/child" 20000)
        (let [result ((:supervise ports) :fake-process (assoc owner/proposed-limits :timeout-ms 20000))]
          (is (identical? interrupted (:wait-error result)))
          (is (identical? interrupted @(:wait-throwable token)))))
      (finally (Thread/interrupted))))
  (let [{:keys [proposal normal]} (fixture)
        fatal (LinkageError. "finalizer linkage failure")
        ports (#'owner/assemble-ports proposal
                (assoc normal :finish (fn [_ _] (throw fatal))))]
    (with-redefs [owner/verify! (constantly {})]
      ((:start ports) controller/argv "/fake/child" 20000)
      (let [result ((:supervise ports) :fake-process (assoc owner/proposed-limits :timeout-ms 20000))
            caught (try ((:finish ports) :fake-process result nil {:cleanup-ms 1500})
                        (catch Throwable error error))]
        (is (identical? fatal caught))
        (is (thrown? clojure.lang.ExceptionInfo
                     ((:finish ports) :fake-process result nil {:cleanup-ms 1500})))))))

(deftest cyclic-interrupt-causes-restore-the-thread-flag-through-capture
  (doseq [capture-key [:retain :handles]]
    (let [{:keys [proposal normal]} (fixture)
          outer (RuntimeException. "cyclic capture failure")
          interrupted (InterruptedException. "nested interruption")]
      (.initCause outer interrupted)
      (.initCause interrupted outer)
      (try
        (Thread/interrupted)
        (let [ports (#'owner/assemble-ports
                     proposal
                     (assoc normal capture-key
                            (fn [_] (throw outer))))]
          (with-redefs [owner/verify! (constantly {})]
            ((:start ports) controller/argv "/fake/child" 20000)
            (let [caught (try
                           ((:supervise ports) :fake-process
                            (assoc owner/proposed-limits :timeout-ms 20000))
                           nil
                           (catch Throwable error error))
                  interrupted? (Thread/interrupted)]
              (is (identical? outer caught))
              (is (true? interrupted?)))))
        (finally
          (Thread/interrupted))))))

(deftest preloaded-operational-namespaces-refuse-before-require
  (let [{:keys [proposal]} (fixture)]
    (doseq [namespace operational-namespaces]
      (with-preloaded-namespace
        namespace
        (fn []
          (let [require-calls (atom [])]
            (with-redefs [owner/verify! (constantly {})
                          clojure.core/require
                          (fn [& args]
                            (swap! require-calls conj args)
                            (throw (ex-info "foreign operational namespace was required"
                                            {:reason :foreign-require})))]
              (let [error (try
                            (owner/operation-ports proposal)
                            nil
                            (catch clojure.lang.ExceptionInfo error error))]
                (is (= :operational-namespace-preloaded (:reason (ex-data error))))
                (is (empty? @require-calls))))))))))

(deftest mutated-request-or-command-refuses-before-any-input-read-or-resolution
  (doseq [proposal [{:request {:id "changed"} :request-sha256 "foreign"}
                    {:request {} :request-sha256 (owner/sha256 (.getBytes "{}" "UTF-8"))
                     :actual-child-command ["foreign"] :command-sha256 "foreign"}]]
    (is (thrown? clojure.lang.ExceptionInfo (owner/operation-ports proposal)))))
