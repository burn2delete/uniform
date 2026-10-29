(ns gravity.pmk.controller-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer [deftest is]]
            [gravity.pmk.controller :as controller]
            [gravity.pmk.first-request-test :as fixture]
            [gravity.pmk.first-request :as first]))

(def process-pass {:status :passed :exit-code 0 :terminal? true :root-alive? false
                   :cleanup-complete? true :cleanup-failed? false :capture-failed? false
                   :timeout-containment-unproven? false :output-truncated? false
                   :wait-error nil :wait-interrupted? false
                   :stdout-capture-status :complete :stderr-capture-status :complete
                   :stdout-capture-error nil :stderr-capture-error nil
                   :stdout-capture-forced-close? false :stderr-capture-forced-close? false})
(defn machine-result [request]
  {:schema :gravity/development-test-result-v1 :authority :non-authoritative :authoritative? false
   :selection {:namespaces ["gravity.self-hosting.sh08-function-call-type-test"]
               :test-vars (:selection request) :fail-fast? false}
   :summary {:test 1 :pass 30 :fail 0 :error 0} :outcome :passed :exit-code 0})

(defn model []
  (let [request (fixture/request)
        ;; One synthetic broker and two file guards exercise three-step cleanup
        ;; without claiming that nested real broker acquisitions are supported.
        guards (mapv #(hash-map :id % :kind (if (= % :common) :broker :canonical)
                               :root (str "/synthetic/" (name %))
                               :resource-class (if (= % :common) :normal :exclusive)
                               :policy-sha256 fixture/h) [:common :host :canonical])
        binding (-> (fixture/binding-input request)
                    (assoc :guards guards)
                    (assoc :coverage {:authority-sha256 fixture/h :required-guards (mapv :id guards)
                                      :acquisition-order (mapv :id guards)
                                      :participants [{:id :synthetic :guards (mapv :id guards)}]}))
        binding (assoc-in binding [:controller :launcher]
                          {:argv (controller/child-command request binding) :cwd (:cwd request)})
        binding (assoc-in binding [:controller :ports :claim] (controller/claim-identity binding))
        events (atom []) now (atom 0) counter (atom 0) claimed (atom false)
        event #(swap! events conj %)
        ports {:clock! #(deref now)
               :bookend! (fn [_] (event :bookend) (controller/expected-bookend request binding))
               :claim! #'controller/claim-operation!
               :acquire! (fn [g _] (event [:acquire (:id g)]) (:id g))
               :validate! (fn [g _] (event [:validate (:id g)]))
               :release! (fn [g _] (event [:release (:id g)]) {:outcome :released})
               :start! (fn [cmd cwd] (event [:start cmd cwd]) :synthetic-child)
               :supervise! (fn [_ opts] (event [:supervise opts]) process-pass)
               :capture! (fn [_ _] (event :capture) (machine-result request))
               :terminate! (fn [_] (event :terminate) {:complete? false :status :unknown})
               :publish! (fn [r] (event [:publish (:status r)]))
               :interrupt! (fn [] (event :interrupt))}]
    {:request request :binding binding :verify (fixture/fake-verifier counter)
     :claim-fn (fn [_] (event :claim)
                 (if (compare-and-set! claimed false true) (controller/claim-record request)
                   (throw (ex-info "already claimed" {:reason :operation-claimed}))))
     :ports ports :events events :clock now :verifier-count counter}))

(defn run-model [m]
  ;; Explicitly replace the declared entrypoint root only in these core controls;
  ;; normal execution never replaces it or injects a different claim function.
  (with-redefs [controller/claim-operation! (:claim-fn m)]
    (controller/run! (:request m) (:binding m) (:binding m) (:verify m) (:ports m))))
(defn event-count [m event] (count (filter #{event} @(:events m))))

(deftest order-identity-and-single-publication
  (let [m (model) r (run-model m) events @(:events m)]
    (is (= :passed (:status r)))
    (is (= [:common :host :canonical] (mapv second (filter #(and (vector? %) (= :acquire (first %))) events))))
    (is (= [:canonical :host :common] (mapv :guard (:releases r))))
    (is (= [:publish :passed] (last events)))
    (is (zero? (event-count m :terminate)))
    (is (= r (edn/read-string (pr-str r))))
    (let [cmd (controller/child-command (:request m) (:binding m))]
      (is (= ["/synthetic/java" "-Xmx2g" "-XX:-OmitStackTraceInFastThrow" "-cp" "/synthetic/src" "clojure.main" "-e"] (subvec cmd 0 7)))
      (is (.contains ^String (last cmd) "gravity.development-test-runner/run-cli!"))
      (is (.contains ^String (last cmd) (str first/selector)))
      (is (not (.contains ^String (last cmd) "test-vars")))))
  (let [m (model) bad (assoc-in m [:binding :guards] [])]
    (is (= :guard-binding (fixture/reason #(run-model bad))))
    (is (empty? @(:events m)))
    (is (zero? @(:verifier-count m))))
  (let [m (model) bad (assoc-in m [:binding :controller :launcher :cwd] "/wrong")]
    (is (= :launcher (fixture/reason #(run-model bad))))
    (is (empty? @(:events m))))
  (let [m (model) bad (assoc-in m [:binding :guards 0 :kind] :guessed)]
    (is (= :guard-kind (fixture/reason #(run-model bad))))
    (is (empty? @(:events m))))
  (let [m (model) bad (assoc-in m [:ports :claim!] (fn [_] (controller/claim-record (:request m))))]
    (is (= :claim-port-binding (fixture/reason #(run-model bad))))
    (is (empty? @(:events m))))
  (let [m (model) bad (assoc-in m [:binding :controller :ports :claim] fixture/h)]
    (is (= :claim-port-binding (fixture/reason #(run-model bad))))
    (is (empty? @(:events m))))
  (let [m (model) bad (assoc-in m [:binding :guards 1 :kind] :broker)]
    (is (= :nested-broker-unsupported (fixture/reason #(run-model bad))))
    (is (empty? @(:events m)))))

(deftest bookends-bind-the-tuple-and-claims-are-one-shot
  (doseq [wrong [nil {} {:wrong-source true}]]
    (let [m (model)]
      (is (= :source-bookend
             (fixture/reason #(run-model (assoc-in m [:ports :bookend!] (fn [_] wrong))))))
      (is (zero? (event-count m :claim)))
      (is (not-any? #(and (vector? %) (#{:acquire :start :publish} (first %))) @(:events m)))))
  (let [m (model) original (get-in m [:ports :bookend!])]
    (is (= :source-bookend
           (fixture/reason #(run-model (assoc-in m [:ports :bookend!]
                                                (fn [b] (assoc (original b) :inputs-sha256 "foreign")))))))
    (is (zero? (event-count m :claim))))
  (let [m (model)]
    (is (= :passed (:status (run-model m))))
    (is (= :operation-claimed (fixture/reason #(run-model m))))
    (is (= 1 (count (filter #(and (vector? %) (= :start (first %))) @(:events m)))))
    (is (= 1 (count (filter #(and (vector? %) (= :publish (first %))) @(:events m)))))))

(deftest acquisition-validation-and-release-failures
  (doseq [at [:acquire! :validate!]]
    (let [m (model) original (get-in m [:ports at])
          r (run-model (assoc-in m [:ports at]
                                (fn [g x] (if (= :host (:id g))
                                            (throw (ex-info "denied" {:reason :denied}))
                                            (original g x)))))]
      (is (= :failed (:status r)))
      (is (= :denied (get-in r [:error :reason])))
      (is (= (if (= at :acquire!) [:common] [:host :common]) (mapv :guard (:releases r))))
      (is (not-any? #(and (vector? %) (= :start (first %))) @(:events m)))))
  (let [m (model) original (get-in m [:ports :release!])
        r (run-model (assoc-in m [:ports :release!]
                              (fn [g lease] (if (= :host (:id g))
                                              (throw (ex-info "release" {:reason :release-error}))
                                              (original g lease)))))]
    (is (= :failed (:status r)))
    (is (= :release-error (get-in r [:error :reason])))
    (is (= [:canonical :host :common] (mapv :guard (:releases r))))
    (is (= [:publish :failed] (last @(:events m)))))
  (let [m (model) r (run-model (assoc-in m [:ports :release!] (fn [& _] {:outcome :uncertain})))]
    (is (= :release-incomplete (get-in r [:error :reason])))
    (is (= 3 (count (:releases r))))))

(deftest deadline-startup-capture-and-process-fail-closed
  (doseq [process [(assoc process-pass :exit-code 7 :status :failed)
                   (assoc process-pass :status :timeout :cleanup-attempted? true :terminal? false
                          :cleanup-complete? false :timeout-containment-unproven? true)
                   (assoc process-pass :capture-failed? true)
                   (assoc process-pass :capture-failed? true :wait-error (Exception. "native wait"))
                   (assoc process-pass :wait-error (Exception. "hidden wait failure"))
                   (assoc process-pass :wait-interrupted? true)
                   (assoc process-pass :stdout-capture-status :error)
                   (assoc process-pass :stderr-capture-error "failed decode")
                   (assoc process-pass :stdout-capture-forced-close? true)
                   (assoc process-pass :stderr-capture-forced-close? true)
                   (assoc process-pass :output-truncated? true)
                   (assoc process-pass :cleanup-complete? false)]]
    (let [m (model) r (run-model (assoc-in m [:ports :supervise!] (fn [& _] process)))]
      (is (= :failed (:status r)))
      (is (= :process-nonpass (get-in r [:error :reason])))
      (is (= r (edn/read-string (pr-str r))))
      (is (zero? (event-count m :capture)))
      (is (zero? (event-count m :terminate)))))
  (let [m (model) original (get-in m [:ports :start!])
        r (run-model (assoc-in m [:ports :start!]
                              (fn [& args] (reset! (:clock m) 120000) (apply original args))))]
    (is (= :whole-deadline (get-in r [:error :reason])))
    (is (= 1 (event-count m :terminate)))
    (is (= :unknown (get-in r [:cleanup :status]))))
  (let [m (model) original (get-in m [:ports :release!])
        r (run-model (assoc-in m [:ports :release!]
                              (fn [& args] (reset! (:clock m) 120000) (apply original args))))]
    (is (= :whole-deadline (get-in r [:error :reason])))
    (is (= 3 (count (:releases r)))))
  (doseq [capture [(assoc-in (machine-result (:request (model))) [:summary :pass] 0)
                   (assoc-in (machine-result (:request (model))) [:selection :test-vars] [])]]
    (let [m (model) r (run-model (assoc-in m [:ports :capture!] (fn [& _] capture)))]
      (is (= :failed (:status r))))))

(deftest interruption-source-drift-and-original-error-preservation
  (let [m (model) r (run-model (assoc-in m [:ports :supervise!]
                                       (fn [& _] (throw (InterruptedException. "stop")))))]
    (is (= :failed (:status r)))
    (is (= 1 (event-count m :interrupt)))
    (is (= 1 (event-count m :terminate)))
    (is (= 3 (count (:releases r)))))
  (let [m (model) count (atom 0)
        original (get-in m [:ports :bookend!])
        r (run-model (assoc-in m [:ports :bookend!] (fn [b] (if (= 1 (swap! count inc)) (original b) nil))))]
    (is (= :source-drift (get-in r [:error :reason]))))
  (let [m (model) r (run-model (-> m
                                 (assoc-in [:ports :supervise!] (fn [& _] (throw (InterruptedException. "original stop"))))
                                 (assoc-in [:ports :interrupt!] (fn [] (throw (Exception. "interrupt hook failed"))))))]
    (is (= "original stop" (get-in r [:error :message])))
    (is (= "interrupt hook failed" (get-in r [:interrupt-errors 0 :message])))
    (is (= 3 (count (:releases r))))
    (is (= [:publish :failed] (last @(:events m)))))
  (let [m (model) original (ex-info "first" {:reason :first-error}) published (atom 0)
        m (-> m (assoc-in [:ports :supervise!] (fn [& _] (throw original)))
              (assoc-in [:ports :release!] (fn [& _] (throw (Exception. "release"))))
              (assoc-in [:ports :publish!] (fn [_] (swap! published inc) (throw (Exception. "publish")))))]
    (try (run-model m) (is false)
         (catch clojure.lang.ExceptionInfo e
           (is (identical? original (.getCause e)))
           (is (= :first-error (get-in (ex-data e) [:original-error :reason])))))
    (is (= 1 @published))
    (is (= 1 (event-count m :terminate))))
  (let [m (model)]
    (is (= :normal-hooks (fixture/reason #(controller/normal-process-ports (assoc (:ports m) :extra identity)))))
    (is (empty? @(:events m)))
    (let [normal (controller/normal-process-ports (select-keys (:ports m) [:bookend! :capture! :publish!]))]
      (is (= controller/port-keys (set (keys normal))))
      (is (every? ifn? (vals normal)))
      (is (empty? @(:events m)))
      (is (nil? (find-ns 'gravity.bootstrap)))
      (is (nil? (find-ns 'gravity.self-hosting.sh01-host-resource-broker)))
      (is (nil? (find-ns 'gravity.self-hosting.sh01-parallel-test-runner))))))
