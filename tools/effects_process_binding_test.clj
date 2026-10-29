(ns effects-process-binding-test
  "Pure process/clock controls. No production port is resolved or loaded."
  (:refer-clojure :exclude [run!])
  (:require [clojure.test :refer [deftest is run-tests]]
            [clojure.string :as str]
            [effects-acceptance :as a]))

(def source-identity
  {:candidate {:commit (apply str (repeat 40 "a")) :tree (apply str (repeat 40 "b"))}
   :main {:commit (apply str (repeat 40 "c")) :tree (apply str (repeat 40 "d"))}
   :dependency-trees (zipmap a/dependency-roots (repeat (apply str (repeat 40 "e"))))
   :resources (zipmap a/resource-paths (repeat (apply str (repeat 64 "f"))))})
(def selection
  {:schema :effects/aggregate-selection-v1 :authority :none :execution :unrun-held
   :identity source-identity :namespace a/test-namespace
   :selectors a/selectors :consumers a/consumers :command (a/command)})
(defn pin [path] {:path path :bytes 1 :sha256 (apply str (repeat 64 "a"))})
(def classpath
  (into (mapv (fn [p] {:path (str "/synthetic/checkout/" p)
                       :tree (get-in source-identity [:dependency-trees p])})
              ["tools" "bootstrap/clojure/test" "bootstrap/clojure/src"])
        (map pin ["/synthetic/clojure.jar" "/synthetic/core-spec.jar" "/synthetic/spec.jar"])))
(def frozen
  {:schema :effects/frozen-owner-operation-v1 :status :nonlaunching-operation-frozen
   :cwd "/synthetic/checkout" :source source-identity :selection selection
   :runtime {:files (mapv pin ["/synthetic/bin/java" "/synthetic/release" "/synthetic/modules" "/synthetic/libjvm"])}
   :classpath classpath :operation (pin "/synthetic/operation.clj")
   :identity-file (assoc (pin "/synthetic/identity.edn") :sha256 (a/request-hash source-identity))
   :broker {:class :memory-heavy :coordination-root "/synthetic/broker" :acquisition-timeout-ms 3600000}
   :command ["/synthetic/bin/java" "-Xmx2g" "-cp"
             (str/join java.io.File/pathSeparator (map :path classpath))
             "clojure.main" "/synthetic/operation.clj" "/synthetic/identity.edn" "/synthetic/broker"]})
(def port-source (slurp a/process-port-path))
(def request (a/validation-request frozen "/synthetic/checkout" port-source))
(def clean {:complete? true :root-alive? false :descendants-alive 0})
(def runner-result
  {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
   :selection {:namespaces [a/test-namespace] :fail-fast? false
               :test-vars (mapv #(str a/test-namespace "/" %) a/selectors)}
   :summary {:test 5 :pass 24 :fail 0 :error 0} :outcome :passed :exit-code 0})
(def payload {:request-hash (a/request-hash request)
              :before source-identity :after source-identity :runner runner-result})
(def output {:status :passed :exit-code 0
             :stdout-capture-status :complete :stderr-capture-status :complete
             :stdout (str a/result-prefix (pr-str payload) "\n")})
(defn harness []
  (let [time (atom 0) calls (atom []) restored (atom false)
        ports {:now #(deref time)
               :read-port-source (fn [_] port-source)
               :start (fn [argv cwd] (swap! calls conj [:start argv cwd]) :fake-process)
               :result (fn [p opts] (swap! calls conj [:result p opts]) output)
               :cleanup (fn [p] (swap! calls conj [:cleanup p]) clean)
               :publish (fn [_] (swap! calls conj [:publish]) true)
               :restore-interrupt (fn [_] (reset! restored true))}]
    {:time time :calls calls :restored restored :ports ports}))
(defn run! [h changes]
  (let [reader a/read-validation-result]
    (with-redefs [a/read-validation-result (fn [r] (swap! (:calls h) conj [:consume]) (reader r))]
      (a/observe-validation! request (a/request-hash request) (merge (:ports h) changes)))))
(defn consumed? [h] (some #{[:consume]} @(:calls h)))

(deftest only-the-original-batch-or-single-pmk-selector-is-admitted
  (let [plan (assoc selection :selectors a/pmk-selectors :command (a/command a/pmk-selectors))
        r (-> runner-result
              (assoc-in [:selection :test-vars] (mapv #(str a/test-namespace "/" %) a/pmk-selectors))
              (assoc-in [:summary :test] 1))]
    (is (= :passed (:outcome (a/bind-result plan source-identity source-identity r))))
    (is (= :selector (try (a/command [(first a/selectors)])
                         (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (= :selector (try (a/bind-result plan source-identity source-identity runner-result)
                         (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))))

(deftest new-pmk-requests-require-the-single-exclusive-lease-class
  (let [plan (assoc selection :selectors a/pmk-selectors :command (a/command a/pmk-selectors))
        pmk (assoc frozen :selection plan)
        exclusive (assoc-in pmk [:broker :class] :exclusive)]
    (is (= :exclusive (get-in (a/validation-request exclusive (:cwd frozen) port-source)
                              [:frozen :broker :class])))
    (is (= :frozen-command
           (try (a/validation-request pmk (:cwd frozen) port-source)
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (= :frozen-command
           (try (a/validation-request (assoc-in frozen [:broker :class] :exclusive) (:cwd frozen) port-source)
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))))

(def lease-source (slurp a/lease-port-path))
(defn leased-harness []
  (let [h (harness) token (Object.)
        r (a/lease-validation-request request lease-source)
        p (assoc payload :request-hash (a/request-hash r))]
    (assoc h :request r :token token
      :ports (assoc (:ports h) :result (fn [& _] (assoc output :stdout (str a/result-prefix (pr-str p)))))
      :broker-ports {:read-lease-source (fn [_] lease-source)
                     :acquire (fn [opts kind] (swap! (:calls h) conj [:acquire opts kind]) token)
                     :validate (fn [lease root kind]
                                 (swap! (:calls h) conj [:validate lease root kind]) lease)
                     :release (fn [lease] (swap! (:calls h) conj [:release lease]) {:outcome :released})})))
(defn run-leased! [h changes]
  (a/observe-under-lease! (:request h) (a/request-hash (:request h)) (:ports h)
                          (merge (:broker-ports h) changes)))

(deftest actual-parent-token-is-held-through-cleanup-and-publication
  (let [h (leased-harness) r (run-leased! h {}) calls @(:calls h)]
    (is (= :observed (:status r)))
    (is (= [:acquired :before-start :after-cleanup :before-publication :after-operation] (:lease-checks r)))
    (is (= :acquire (ffirst calls)))
    (is (= {:coordination-root "/synthetic/broker" :timeout-ms 3600000} (second (first calls))))
    (is (= [:release (:token h)] (last calls)))
    (is (every? #(identical? (:token h) (second %)) (filter #(#{:validate :release} (first %)) calls)))
    (is (= [:cleanup :validate :validate :publish :validate :release]
           (mapv first (take-last 6 calls))))
    (is (false? (:semantic-acceptance r)))
    (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker)))))

(deftest admission-and-token-failures-do-not-launch-or-retry
  (doseq [changes [{:acquire (fn [& _] (throw (ex-info "admission refused" {:id "refused"})))}
                   {:validate (fn [& _] (Object.))}]]
    (let [h (leased-harness) r (run-leased! h changes)]
      (is (= :refused (:status r)))
      (is (not-any? #(= :start (first %)) @(:calls h)))
      (is (<= (count (filter #(= :release (first %)) @(:calls h))) 1))))
  (let [h (leased-harness) n (atom 0)
        r (run-leased! h {:release (fn [_] (swap! n inc) (throw (Exception. "uncertain release")))})]
    (is (= :refused (:status r)))
    (is (= :unknown (get-in r [:release :status])))
    (is (= 1 @n))))

(deftest lease-source-drift-is-rejected-before-acquisition
  (let [h (leased-harness)]
    (is (= :lease-port-source
           (try (run-leased! h {:read-lease-source (fn [_] (str lease-source "\n; drift"))})
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (empty? @(:calls h))))
  (let [h (leased-harness) bad (assoc-in (:request h) [:lease-source :timeout-ms] 60000)]
    (is (= :missing-lease-port
           (try (a/observe-under-lease! bad (a/request-hash bad) (:ports h) (:broker-ports h))
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (empty? @(:calls h)))))

(deftest parent-release-follows-negative-and-result-owned-cleanup
  (doseq [changes [{:result (fn [& _] (assoc output :status :failed :exit-code 1))}
                   {:cleanup (fn [_] (throw (Exception. "cleanup failed")))}
                   {:result (fn [& _] (throw (InterruptedException.)))}]]
    (let [h (update (leased-harness) :ports merge changes) r (run-leased! h {})]
      (is (= :refused (:status r)))
      (is (= :released (get-in r [:release :status])))
      (is (= [:release (:token h)] (last @(:calls h))))))
  (let [h (leased-harness)
        h (update h :ports assoc :result (fn [& args] (assoc (apply (get-in h [:ports :result]) args) :cleanup clean)))
        r (run-leased! h {})]
    (is (= :observed (:status r)))
    (is (= [:acquired :before-start :before-publication :after-operation] (:lease-checks r)))
    (is (not-any? #(= :cleanup (first %)) @(:calls h)))))

(deftest exact-request-and-normal-port-specification
  (let [h (harness) r (run! h {})]
    (is (= :observed (:status r)))
    (is (= [:start (conj (:command frozen) (a/request-hash request)) "/synthetic/checkout"] (first @(:calls h))))
    (is (= 3600000 (get-in (second @(:calls h)) [2 :timeout-ms])))
    (is (= (a/sha256 port-source) (get-in request [:port-source :sha256])))
    (is (= 11800 (get-in request [:bounds :accounted-reclamation-base-ms])))
    (is (false? (:semantic-acceptance r)))
    (is (nil? (find-ns 'gravity.bootstrap)))
    (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner)))))

(deftest elapsed-start-and-late-result-never-reset-the-budget
  (let [h (harness)]
    (is (= :observed (:status (run! h {:start (fn [& _] (reset! (:time h) 1500) :fake-process)}))))
    (is (= 3598500 (get-in (first @(:calls h)) [2 :timeout-ms]))))
  (let [h (harness)]
    (is (= :timeout (:reason (run! h {:start (fn [& _] (reset! (:time h) 3600000) :fake-process)}))))
    (is (= [[:cleanup :fake-process]] @(:calls h))))
  (let [h (harness)]
    (is (= :timeout (:reason (run! h {:result (fn [& _] (reset! (:time h) 3600001) output)}))))
    (is (not (consumed? h)))))

(deftest failed-process-capture-or-containment-never-consumes-a-result
  (doseq [[r c expected]
          [[(assoc output :status :timeout :exit-code 124) clean :timeout]
           [(assoc output :status :failed :exit-code 9) clean :child-failure]
           [(assoc output :stdout-capture-status :error) clean :failed-capture]
           [(assoc output :stderr-capture-forced-close? true) clean :failed-capture]
           [(assoc output :stdout-capture-status :capture-timeout) clean :failed-capture]
           [(assoc output :cleanup-complete? false) clean :incomplete-cleanup]
           [output (assoc clean :complete? false) :incomplete-cleanup]
           [output (assoc clean :descendants-alive 1) :incomplete-cleanup]
           [(assoc output :timeout-containment-unproven? true) clean :incomplete-cleanup]]]
    (let [h (harness)]
      (is (= expected (:reason (run! h {:result (fn [& _] r) :cleanup (fn [_] c)}))))
      (is (not (consumed? h)))))
  (let [h (harness) n (atom 0)]
    (is (= :incomplete-cleanup (:reason (run! h {:cleanup (fn [_] (swap! n inc) (throw (Exception. "cleanup")))}))))
    (is (= 1 @n))
    (is (not (consumed? h)))))

(deftest interruption-missing-results-hash-and-publication-fail-closed
  (let [h (harness)]
    (is (= :interrupted (:reason (run! h {:result (fn [& _] (throw (InterruptedException.)))}))))
    (is (true? @(:restored h)))
    (is (not (consumed? h))))
  (doseq [[changes why]
          [[{:result (fn [& _] (assoc output :stdout "no sink"))} :missing-runner-result]
           [{:result (fn [& _] (assoc output :stdout (str a/result-prefix "{:request-hash \"wrong\"}")))} :request-hash]
           [{:publish (fn [_] false)} :publication-failed]
           [{:publish (fn [_] (throw (Exception. "publication")))} :publication-failed]]]
    (let [h (harness)] (is (= why (:reason (run! h changes))))))
  (let [h (harness)]
    (is (= :request-hash
           (try (a/observe-validation! request "wrong" (:ports h))
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (empty? @(:calls h)))))

(deftest returned-interruption-and-uncertain-publication-preserve-their-outcomes
  (let [h (harness)]
    (is (= :interrupted (:reason (run! h {:result (fn [& _] (assoc output :wait-interrupted? true))}))))
    (is (true? @(:restored h)))
    (is (not (consumed? h))))
  (let [h (harness) calls (atom 0)
        r (run! h {:publish (fn [_] (swap! calls inc) (throw (Exception. "uncertain")))})]
    (is (= :publication-failed (:reason r)))
    (is (= :unknown (:publication r)))
    (is (= 1 @calls)))
  (let [h (harness)]
    (is (= :incomplete-cleanup
           (:reason (run! h {:cleanup (fn [_] (throw (InterruptedException.)))}))))
    (is (true? @(:restored h)))))

(deftest frozen-command-classpath-cwd-and-bounds-reject-before-any-port
  (doseq [[bad why] [[(assoc-in request [:frozen :command 1] "-javaagent:unreviewed") :frozen-command]
                    [(assoc-in request [:frozen :command] (apply list (:command frozen))) :frozen-command]
                    [(assoc-in request [:frozen :classpath 0 :path] "/wrong/tools") :frozen-classpath]
                    [(assoc request :cwd "/different") :working-directory]
                    [(assoc-in request [:frozen :selection :execution] :passed) :plan]]]
    (let [h (harness)]
      (is (= why (try (a/observe-validation! bad (a/request-hash bad) (:ports h))
                      (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
      (is (empty? @(:calls h)))))
  (is (= :process-port-bounds
         (try (a/validation-request frozen "/synthetic/checkout"
                (str/replace port-source "process-cleanup-grace-ms 100" "process-cleanup-grace-ms 999"))
              (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
  (is (= :process-port-bounds
         (try (a/validation-request frozen "/synthetic/checkout"
                (str/replace port-source "(.onExit handle) 100" "(.onExit handle) 101"))
              (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))

(deftest empty-cleanup-and-wrapped-interruptions-are-not-retried-or-erased
  (doseq [outcome [nil false]]
    (let [h (harness) n (atom 0)
          r (run! h {:cleanup (fn [_] (swap! n inc) outcome)})]
      (is (= :incomplete-cleanup (:reason r)))
      (is (= 1 @n))
      (is (not (consumed? h)))))
  (let [h (harness)]
    (is (= :interrupted
           (:reason (run! h {:result (fn [& _] (throw (ex-info "wrapped" {} (InterruptedException.))))}))))
    (is (true? @(:restored h)))))

(deftest only-one-explicit-child-sink-envelope-is-consumed
  (let [payload {:request-hash (a/request-hash request)
                 :before source-identity :after source-identity :runner runner-result}
        line (str a/result-prefix (pr-str payload) "\n")]
    (is (= payload (a/read-validation-result {:stdout (str "Testing synthetic\n" line)})))
    (doseq [text ["0 failures" (str line line) (pr-str payload)]]
      (is (= :missing-runner-result
             (try (a/read-validation-result {:stdout text})
                  (catch clojure.lang.ExceptionInfo e (:reason (ex-data e)))))))))

(deftest stale-port-source-and-missing-timeout-cleanup-fail-closed
  (let [h (harness)]
    (is (= :process-port-source
           (try (run! h {:read-port-source (fn [_] (str port-source "\n; drift"))})
                (catch clojure.lang.ExceptionInfo e (:reason (ex-data e))))))
    (is (empty? @(:calls h))))
  (doseq [r [(assoc output :status :timeout :cleanup nil)
             (assoc output :wait-interrupted? true :cleanup nil)]]
    (let [h (harness)]
      (run! h {:result (fn [& _] r)})
      (is (= 1 (count (filter #{[:cleanup :fake-process]} @(:calls h)))))
      (is (not (consumed? h))))))

(let [r (run-tests 'effects-process-binding-test)]
  (prn {:real-process-ports-called 0 :Bootstrap-loaded (boolean (find-ns 'gravity.bootstrap))})
  (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))
