(ns gravity.artifact-pipeline.parent-controller-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [gravity.artifact-pipeline.parent-controller :as controller]))

(def real-calls (atom 0))
(use-fixtures :each
  (fn [test]
    (with-redefs [controller/normal-process-bindings
                  (fn [] (swap! real-calls inc) (throw (Exception. "Real ports forbidden")))]
      (test)
      (is (zero? @real-calls))
      (doseq [name '[gravity.bootstrap gravity.self-hosting.sh01-host-resource-broker
                     gravity.self-hosting.sh01-parallel-test-runner]]
        (is (nil? (find-ns name)))))))

(defn fixture []
  (let [events (atom []) time (atom 0) leases (atom {})
        guards (mapv #(hash-map :kind % :root (str "/fake/" (name %)) :source-id (str "fixture/" (name %)))
                     [:broker :exclusive :canonical])
        participants [{:owner :fixture :roots (mapv :root guards)}]
        request {:id "fake-once" :source controller/source :selector controller/selector :argv controller/argv
                 :cwd "/fake/frozen-source" :runtime {:classpath ["fake-source"] :jars ["fake-jar"]
                                                      :executables ["fake-java"] :configuration "fake-config"}
                 :config "fake-source-bound-owner-config"
                 :port-identities (zipmap controller/port-names (repeat "fake-function-source"))
                 :outputs {:attempt "/fake/attempt" :stdout "/fake/stdout" :stderr "/fake/stderr" :final "/fake/final"}
                 :limits {:whole-ms 100 :capture-bytes 1000 :capture-chars 1000 :capture-wait-ms 5 :cleanup-ms 10}
                 :guards guards :coverage {:authority-source "fake-reviewed-config" :decision-id "fake-decision"
                                           :participants participants :covered-participants participants :ordered-guards guards}}
        observation (select-keys request controller/identity-fields)
        result {:status :passed :exit-code 0 :wait-interrupted? false :output-truncated? false :capture-failed? false
                :cleanup-failed? false :cleanup-root-alive? false :timeout-containment-unproven? false
                :stdout "actual fake capture" :stderr "" :stdout-bytes 19N :stderr-bytes 0}
        ports {:clock (fn [] @time)
               :admit (fn [& _] (swap! events conj :admit) true)
               :reserve (fn [& _] (swap! events conj :reserve) true)
               :acquire (fn [guard _] (let [lease (Object.)]
                                        (swap! events conj [:acquire (:kind guard)])
                                        (swap! leases assoc (:kind guard) lease) lease))
               :validate (fn [guard lease] (swap! events conj [:validate (:kind guard)]) lease)
               :release (fn [guard lease]
                          (is (identical? lease (get @leases (:kind guard))))
                          (swap! events conj [:release (:kind guard)])
                          {:status :released :evidence-id (str "fake-release/" (:kind guard))})
               :start (fn [argv cwd _]
                        (is (= controller/argv argv)) (is (= (:cwd request) cwd))
                        (swap! events conj :start) :fake-process)
               :supervise (fn [process _] (is (= :fake-process process)) (swap! events conj :supervise) result)
               :finish (fn [process _ _ bounds]
                         (is (= :fake-process process))
                         (is (= 100 (:deadline-ms bounds)))
                         (is (= 10 (:cleanup-ms bounds)))
                         (is (fn? (:clock bounds)))
                         (swap! events conj :finish)
                         {:status :complete :owned-descendants-complete? true :root-alive? false :evidence-id "fake-terminal"})
               :observe (fn [] (swap! events conj :observe) observation)
               :publish (fn [_ receipt] (swap! events conj [:publish (:status receipt)]) {:status :published})}]
    {:request request :observation observation :ports ports :events events :time time :result result}))

(defn execute [f & [changes request observation]]
  (controller/run! (controller/bind-controller (or request (:request f)) (merge (:ports f) changes))
                   (or observation (:observation f))))

(defn reason [result] (:reason (ex-data (:error result))))

(deftest identity-and-unbound-coverage-precede-every-port
  (let [f (fixture)]
    (is (thrown? clojure.lang.ExceptionInfo (execute f {} nil (assoc (:observation f) :cwd "/foreign"))))
    (is (empty? @(:events f)))
    (doseq [field [:config :coverage :runtime :guards :outputs]]
      (let [request (dissoc (:request f) field)]
        (is (= :refused (:status (execute f {} request (select-keys request controller/identity-fields)))))
        (is (empty? @(:events f)))))))

(deftest replacing-a-captured-runtime-port-refuses-before-effects
  (let [f (fixture) prepared (controller/bind-controller (:request f) (:ports f))]
    (is (thrown? clojure.lang.ExceptionInfo
                 (controller/run! (assoc-in prepared [:ports :start] (fn [& _] :foreign-process)) (:observation f))))
    (is (empty? @(:events f)))))

(deftest success-retains-exact-order-and-opaque-owner-values
  (let [f (fixture) owner-object (Object.)
        result (assoc (:result f) :opaque-owner owner-object)
        r (execute f {:supervise (fn [_ _] (swap! (:events f) conj :supervise) result)})]
    (is (= :passed (:status r)))
    (is (= [:admit :reserve [:acquire :broker] [:validate :broker] [:acquire :exclusive] [:validate :exclusive]
            [:acquire :canonical] [:validate :canonical] :start :supervise :finish :observe
            [:release :canonical] [:release :exclusive] [:release :broker] [:publish :passed]] @(:events f)))
    (is (identical? owner-object (get-in r [:raw-process-result :opaque-owner])))
    (is (not (contains? (get-in r [:receipt :process-result]) :opaque-owner)))
    (is (= 19N (get-in r [:receipt :process-result :stdout-bytes])))
    (is (instance? Long (get-in r [:receipt :process-result :stdout-bytes])))
    (is (instance? clojure.lang.BigInt (get-in r [:raw-process-result :stdout-bytes])))
    (is (= :published (:publication r)))))

(deftest acquisition-order-comes-from-the-explicit-coverage-decision
  (let [f (fixture) guards (vec (reverse (get-in f [:request :guards])))
        request (-> (:request f) (assoc :guards guards) (assoc-in [:coverage :ordered-guards] guards))
        observation (select-keys request controller/identity-fields)
        r (execute f {:observe (fn [] observation)} request observation)]
    (is (= :passed (:status r)))
    (is (= [[:acquire :canonical] [:acquire :exclusive] [:acquire :broker]]
           (filter #(and (vector? %) (= :acquire (first %))) @(:events f))))
    (is (= [[:release :broker] [:release :exclusive] [:release :canonical]]
           (filter #(and (vector? %) (= :release (first %))) @(:events f))))))

(deftest unordered-guard-collections-refuse-before-every-port
  (let [f (fixture) guards (get-in f [:request :guards])]
    (doseq [[actual declared] [[(set guards) (set guards)] [(set guards) guards] [guards (set guards)]]]
      (let [request (-> (:request f) (assoc :guards actual) (assoc-in [:coverage :ordered-guards] declared))
            r (execute f {} request (select-keys request controller/identity-fields))]
        (is (= :refused (:status r)))
        (is (some #{:source-bound-ordered-guards} (:missing r)))
        (is (empty? @(:events f)))))))

(deftest denied-admission-and-output-reservation-never-start-or-publish
  (doseq [port [:admit :reserve]]
    (let [f (fixture) r (execute f {port (fn [& _] (swap! (:events f) conj [:denied port]) false)})]
      (is (= :failed (:status r)))
      (is (not-any? #{:start :finish} @(:events f)))
      (is (not-any? #(and (vector? %) (= :publish (first %))) @(:events f))))))

(deftest partial-lock-failure-releases-original-leases-in-reverse-order
  (let [f (fixture) acquire (get-in f [:ports :acquire]) error (Exception. "lock failure")
        r (execute f {:acquire (fn [guard budget]
                                 (if (= :canonical (:kind guard)) (throw error) (acquire guard budget)))})]
    (is (identical? error (:error r)))
    (is (= [[:release :exclusive] [:release :broker]]
           (filter #(and (vector? %) (= :release (first %))) @(:events f))))
    (is (not-any? #{:start :finish} @(:events f)))))

(deftest startup-and-release-time-are-charged-to-the-whole-deadline
  (let [f (fixture) seen (atom nil)
        r (execute f {:start (fn [& _] (swap! (:time f) + 40) :fake-process)
                      :supervise (fn [_ opts] (reset! seen (:timeout-ms opts)) (:result f))})]
    (is (= 60 @seen)) (is (= :passed (:status r))))
  (doseq [port [:start :release]]
    (let [f (fixture) original (get-in f [:ports port])
          r (execute f {port (fn [& args] (swap! (:time f) + 101) (apply original args))})]
      (is (= :failed (:status r)))
      (is (= :whole-deadline (reason r)))
      (is (= 1 (count (filter #{:finish} @(:events f))))))))

(deftest nonzero-timeout-capture-and-owned-cleanup-fail-closed
  (doseq [bad [(assoc (:result (fixture)) :exit-code 1)
               (assoc (:result (fixture)) :status :timeout :timeout-containment-unproven? true)
               (assoc (:result (fixture)) :capture-failed? true)
               (dissoc (:result (fixture)) :cleanup-failed?)]]
    (let [f (fixture) r (execute f {:supervise (fn [& _] bad)})]
      (is (= :failed (:status r)))
      (is (= :nonpassing-process-or-cleanup (reason r)))))
  (doseq [terminal [nil {:status :unknown} {:status :complete :owned-descendants-complete? false :root-alive? false}]]
    (let [f (fixture) r (execute f {:finish (fn [& _] terminal)})]
      (is (= :failed (:status r)))
      (is (= :nonpassing-process-or-cleanup (reason r))))))

(deftest original-error-survives-cleanup-release-and-publication-errors
  (let [f (fixture) original (Exception. "supervision failed")
        r (execute f {:supervise (fn [& _] (throw original))
                      :finish (fn [& _] (throw (Exception. "cleanup unknown")))
                      :release (fn [& _] (throw (Exception. "release failed")))
                      :publish (fn [& _] (throw (Exception. "receipt failed")))})]
    (is (identical? original (:error r)))
    (is (= :failed (:status r)))
    (is (= :unknown (:publication r)))
    (is (= 3 (count (:raw-release-attempts r))))
    (is (every? #(and (:lease %) (= :unknown (:status %)) (:error %)) (:raw-release-attempts r)))
    (is (<= 5 (count (:secondary-errors r))))))

(deftest interruption-restores-the-flag-after-finalization
  (let [f (fixture)]
    (try
      (let [r (execute f {:supervise (fn [& _] (throw (InterruptedException. "interrupted")))})
            flag-at-return (.isInterrupted (Thread/currentThread))]
        (is (instance? InterruptedException (:error r)))
        (is flag-at-return)
        (is (= 1 (count (filter #{:finish} @(:events f))))))
      (finally (Thread/interrupted)))))

(deftest returned-runner-interruption-survives-a-finalizer-clearing-the-flag
  (let [f (fixture) original (InterruptedException. "normal runner wait interrupted")]
    (try
      (let [r (execute f {:supervise (fn [& _]
                                       (.interrupt (Thread/currentThread))
                                       (assoc (:result f) :status :error :wait-interrupted? true :wait-error original))
                          :finish (fn [& _] (Thread/interrupted)
                                    {:status :complete :owned-descendants-complete? true :root-alive? false})})
            flag-at-return (.isInterrupted (Thread/currentThread))]
        (is (= :failed (:status r)))
        (is (identical? original (:error r)))
        (is flag-at-return)
        (is (true? (get-in r [:receipt :process-result :wait-interrupted?]))))
      (finally (Thread/interrupted)))))

(deftest wrapped-normal-broker-interruption-keeps-original-error-and-flag
  (let [f (fixture) acquire (get-in f [:ports :acquire])
        original (ex-info "broker interrupted" {:id "SH01-BROKER-INTERRUPTED"} (InterruptedException. "wait"))]
    (try
      (let [r (execute f {:acquire (fn [guard budget]
                                     (if (= :exclusive (:kind guard)) (throw original) (acquire guard budget)))
                          :release (fn [& _] (Thread/interrupted) {:status :released})})
            flag-at-return (.isInterrupted (Thread/currentThread))]
        (is (identical? original (:error r)))
        (is flag-at-return)
        (is (not-any? #{:start} @(:events f))))
      (finally (Thread/interrupted)))))

(deftest one-finalizer-and-one-publication-no-retry-on-unknown-write
  (let [f (fixture) finishes (atom 0) publications (atom 0)
        r (execute f {:finish (fn [& _] (swap! finishes inc) {:status :unknown})
                      :publish (fn [& _] (swap! publications inc) nil)})]
    (is (= 1 @finishes)) (is (= 1 @publications))
    (is (= :failed (:status r)))
    (is (= :unknown (:publication r)))))

(deftest denied-or-invalid-guard-never-starts-and-releases-acquired-handles
  (doseq [changes [{:acquire (fn [& _] nil)} {:validate (fn [& _] :foreign-lease)}]]
    (let [f (fixture) r (execute f changes)]
      (is (= :failed (:status r)))
      (is (not-any? #{:start :finish} @(:events f)))
      (is (= (if (:validate changes) 1 0)
             (count (filter #(and (vector? %) (= :release (first %))) @(:events f))))))))

(deftest repeated-operation-cannot-publish-or-clean-up-a-second-time
  (let [f (fixture) reserved (atom false)
        ports {:reserve (fn [& _] (compare-and-set! reserved false true))}]
    (is (= :passed (:status (execute f ports))))
    (is (= :failed (:status (execute f ports))))
    (is (= 1 (count (filter #{:finish} @(:events f)))))
    (is (= 1 (count (filter #(and (vector? %) (= :publish (first %))) @(:events f)))))))

(deftest drift-release-nonfinalization-and-late-publication-cannot-pass
  (doseq [changes [{:observe (fn [] {})}
                   {:release (fn [& _] {:status :unknown})}
                   {:publish (fn [& _] (throw (Exception. "receipt codec failed")))}]]
    (let [f (fixture) r (execute f changes)] (is (= :failed (:status r)))))
  (let [f (fixture) writes (atom 0)
        r (execute f {:publish (fn [& _] (swap! writes inc) (swap! (:time f) + 101) {:status :published})})]
    (is (= :failed (:status r))) (is (= :whole-deadline (reason r)))
    (is (= 1 @writes)) (is (= :unknown (:publication r)))))
