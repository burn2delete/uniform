
(defn validate-child!
  "Strictly parse and validate a joined child after its exit event is recorded."
  [joined codec]
  (let [role (:role joined)
        stderr-bytes (:stderr-bytes joined)]
    (when-not (and (zero? (:exit-code joined))
                   (zero? (alength ^bytes stderr-bytes)))
      (reject! :child-exit
               {:role role :exit (:exit-code joined)
                :stderr-byte-count (alength ^bytes stderr-bytes)}))
    (let [response
          ((:strict-read-wire codec) :response (:stdout-bytes joined))
          transport (:transport response)
          result (:result response)
          receipt (:source-read-receipt result)
          descriptors (:source-descriptors receipt)
          events (:events receipt)
          production (:production result)
          sentinel (:sentinel result)
          counters (:counters response)
          tcb (:tcb response)
          without-self-id
          (update response :transport dissoc :response-preimage-id)
          response-id
          ((:H codec)
           [:gravity/sh07-b51-response-preimage-id-v1 without-self-id])
          _ (validate-child-eligibility! joined codec tcb)]
      (when-not
       (and (exact-keys? response response-keys)
            (= :gravity/sh07-b51-checker-child-response-v1
               (:schema response))
            (= role (:role response))
            (exact-keys? transport response-transport-keys)
            (= (:argv-id joined) (:argv-id transport))
            (= (:request-id joined) (:request-id transport))
            (= response-id (:response-preimage-id transport))
            (= [[:transport :response-preimage-id]]
               (:self-id-omits transport))
            (exact-keys? result result-keys)
            (= :accepted (get-in result [:checker-output :status]))
            (every? #(exact-keys? % result-record-keys)
                    [production sentinel])
            (= [role role] [(:role production) (:role sentinel)])
            (= [:accepted :accepted]
               [(:status production) (:status sentinel)])
            (= [true false] [(:authority production) (:authority sentinel)])
            (= [:gravity/sh07-b51-checker-production-v1
                :gravity/sh07-b51-checker-sentinel-v1]
               [(:schema production) (:schema sentinel)])
            (= (:artifact-id production)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-artifact-id-v1
                 (dissoc production :artifact-id :output-id)]))
            (= (:output-id production)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-output-id-v1
                 (dissoc production :output-id)]))
            (= (:artifact-id sentinel)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-artifact-id-v1
                 (dissoc sentinel :artifact-id :output-id)]))
            (= (:output-id sentinel)
               ((:H codec)
                [:gravity/sh07-b51-checker-result-output-id-v1
                 (dissoc sentinel :output-id)]))
            (exact-keys? receipt source-receipt-keys)
            (= role (:role receipt))
            (= (:content (:request joined))
               (select-keys receipt [:commit :tree]))
            (vector? descriptors) (= 13 (count descriptors))
            (every? #(and (exact-keys? % source-descriptor-keys)
                          (= "100644" (:mode %))
                          (git-id? (:blob-oid %))
                          (sha256-id? (:raw-sha256 %)))
                    descriptors)
            (= (vec (range 13)) (mapv :ordinal descriptors))
            (= (:source-descriptors-root receipt)
               ((:H codec)
                [:gravity/sh07-b51-source-descriptors-root-v1 descriptors]))
            (vector? events) (= 40 (count events))
            (every? #(and (exact-keys? % source-event-keys)
                          (sha256-id? (:source-raw-sha256 %))
                          (sha256-id? (:product-id %))
                          (= (get-in descriptors
                                     [(:source-ordinal %) :raw-sha256])
                             (:source-raw-sha256 %)))
                    events)
            (= (vec (range 40)) (mapv :ordinal events))
            (= (:event-root receipt)
               ((:H codec)
                [:gravity/sh07-b51-source-event-root-v1 events]))
            (exact-keys? (:counters receipt) source-counter-keys)
            (= {:acquire-count 13 :decode-count 13
                :event-count 40 :finalize-count 1}
               (:counters receipt))
            (true? (:finalized receipt))
            (exact-keys? counters response-counter-keys)
            (exact-keys? (:work counters) work-counter-keys)
            (= {:vector [3 3 3 2 2] :total 13} (:work counters))
            (= (:counters receipt) (:source-session counters))
            (exact-keys? (:process counters) child-process-counter-keys)
            (= {:child-count 1 :start-count 1 :exit-count 1
                :overlap-count 0}
               (:process counters))
            (exact-keys? (:ipc counters) ipc-counter-keys)
            (= {:request-count 1 :response-count 1
                :request-max-bytes 4194304
                :response-max-bytes 1073741824
                :stderr-max-bytes 1048576 :deadline-ms 3600000}
               (:ipc counters))
            (exact-keys? tcb tcb-keys)
            (= role (:role tcb))
            (= (:manifest-id tcb)
               ((:H codec)
                [:gravity/sh07-b51-checker-child-tcb-manifest-v1
                 (dissoc tcb :manifest-id)]))
            (empty? (:diagnostics response)))
        (reject! :child-response {:role role}))
      (assoc joined
             :response response
             :response-preimage-id response-id
             :response-byte-count (alength ^bytes (:stdout-bytes joined))
             :response-raw-sha256 ((:R codec) (:stdout-bytes joined))))))

(defn- append-event
  [events H event role coordinates]
  (let [ordinal (count events)
        [expected-event expected-role] (nth coordinator-event-plan ordinal nil)]
    (when-not (= [expected-event expected-role] [event role])
      (reject! :event-order
               {:ordinal ordinal
                :expected [expected-event expected-role]
                :actual [event role]}))
    (let [base
          (merge
           {:ordinal ordinal :event event :role role
            :process-handle-id nil :argv-id nil :request-id nil
            :response-preimage-id nil :source-session-id nil
            :prior-event-root (:event-id (peek events))}
           coordinates)
          row (assoc base :event-id
                     (H [:gravity/sh07-b51-coordinator-event-id-v1 base]))]
      (when-not (= event-keys (set (keys row)))
        (reject! :event-shape {:ordinal ordinal}))
      (conj events row))))

(defn coordinate!
  "Run the append-only exact A -> B -> M state machine.

  OPERATIONS is a closed process seam used by focused tests and production.
  Since `:build-child` for B is not called until A validation returns, B argv
  and its ProcessBuilder cannot exist before ordinal 4.  The M session is not
  invoked until B validation returns at ordinal 8."
  [operations H]
  (let [events0
        (append-event [] H :m-process-start :m
                      {:process-handle-id
                       (:m-process-handle-id operations)})
        a-built ((:build-child operations) :producer-census)
        events1 (append-event events0 H :a-builder :a
                              {:argv-id (:argv-id a-built)})
        a-started ((:start-child operations) a-built)
        events2 (append-event events1 H :a-start :a
                              {:process-handle-id
                               (get-in a-started
                                       [:process-start :process-handle-id])})
        a-joined ((:join-child operations) a-started)
        _ (when-not (zero? (:exit-code a-joined))
            (reject! :a-exit {:exit-code (:exit-code a-joined)}))
        events3 (append-event events2 H :a-exit-observed :a
                              {:process-handle-id
                               (get-in a-joined
                                       [:process-start :process-handle-id])})
        a-valid ((:validate-child operations) a-joined)
        events4
        (append-event events3 H :a-response-validated :a
                      {:request-id (:request-id a-valid)
                       :response-preimage-id
                       (:response-preimage-id a-valid)})
        ;; Nothing related to B is evaluated above this binding.
        b-built ((:build-child operations) :independent-verifier-census)
        events5 (append-event events4 H :b-builder :b
                              {:argv-id (:argv-id b-built)})
        b-started ((:start-child operations) b-built)
        events6 (append-event events5 H :b-start :b
                              {:process-handle-id
                               (get-in b-started
                                       [:process-start :process-handle-id])})
        b-joined ((:join-child operations) b-started)
        _ (when-not (zero? (:exit-code b-joined))
            (reject! :b-exit {:exit-code (:exit-code b-joined)}))
        events7 (append-event events6 H :b-exit-observed :b
                              {:process-handle-id
                               (get-in b-joined
                                       [:process-start :process-handle-id])})
        b-valid ((:validate-child operations) b-joined)
        events8
        (append-event events7 H :b-response-validated :b
                      {:request-id (:request-id b-valid)
                       :response-preimage-id
                       (:response-preimage-id b-valid)})
        ;; The synchronous arity-two wrapper opens and finalizes internally.
        ;; Its invocation still occurs strictly after ordinal-8 validation.
        m-session ((:m-session operations))
        session-id (:source-session-id m-session)
        events9 (append-event events8 H :m-session-start :m
                              {:source-session-id session-id})
        events10 (append-event events9 H :m-session-finalize :m
                               {:source-session-id session-id})]
    {:a a-valid
     :b b-valid
     :m-session m-session
     :events events10
     :event-root
     (H [:gravity/sh07-b51-coordinator-event-root-v1 events10])}))

(defn- source-session-id [H receipt]
  (H [:gravity/sh07-b51-governed-source-session-id-v1
      {:role (:role receipt)
       :commit (:commit receipt)
       :tree (:tree receipt)
       :source-descriptors-root (:source-descriptors-root receipt)
       :event-root (:event-root receipt)
       :counters (:counters receipt)}]))
