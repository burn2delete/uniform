(def ^:private p15-s23-sh07-b51-pending-entry-seal (Object.))

(deftype ^:private P15S23Sh07B51PendingEntryDescriptor [payload seal]
  clojure.lang.ILookup
  (valAt [_ key] (get payload key))
  (valAt [_ key not-found] (get payload key not-found))
  clojure.lang.Counted
  (count [_] (count payload))
  clojure.lang.Seqable
  (seq [_] (seq payload)))

(alter-meta! #'->P15S23Sh07B51PendingEntryDescriptor assoc :private true)

(defn- p15-s23-sh07-b51-pending-entry-descriptor [payload]
  (P15S23Sh07B51PendingEntryDescriptor.
   payload p15-s23-sh07-b51-pending-entry-seal))

(defn- p15-s23-sh07-b51-authentic-pending-entry-descriptor? [descriptor]
  (and (instance? P15S23Sh07B51PendingEntryDescriptor descriptor)
       (identical? p15-s23-sh07-b51-pending-entry-seal
                   (.-seal ^P15S23Sh07B51PendingEntryDescriptor descriptor))))

(defn- p15-s23-sh07-b51-close-authentic-pending-entry! [descriptor]
  (when (p15-s23-sh07-b51-authentic-pending-entry-descriptor? descriptor)
    (let [lifecycle (:lifecycle-ref descriptor)
          pending (when (p15-s23-sh07-b51-atomic-reference? lifecycle)
                    (.get ^java.util.concurrent.atomic.AtomicReference
                          lifecycle))]
      (when (= {:state :pending} pending)
        (.compareAndSet ^java.util.concurrent.atomic.AtomicReference lifecycle
                        pending {:state :closed}))))
  nil)

(defn- p15-s23-sh07-b51-pending-entry-shape?
  [descriptor action frames public-invocation-scope]
  (let [frame-count (count frames)
        sequence-frame (when (pos? frame-count) (peek frames))
        return-frame (when (<= 2 frame-count)
                       (nth frames (- frame-count 2)))]
    (and (p15-s23-sh07-b51-authentic-pending-entry-descriptor? descriptor)
         (= p15-s23-sh07-b51-pending-entry-keys
            (set (keys descriptor)))
         (= :gravity/sh07-b51-pending-root1-entry-v3
            (:artifact descriptor))
         (= 3 (:schema-version descriptor))
         (= 5 (count action))
         (= :eval (nth action 0 nil))
         (identical? (nth action 1 nil) (:callee-plan-ref descriptor))
         (identical? (nth action 2 nil) (:environment-ref descriptor))
         (identical? (nth action 3 nil) (:first-instruction-ref descriptor))
         (identical? sequence-frame
                     (:callee-sequence-frame-ref descriptor))
         (identical? return-frame
                     (:callee-return-frame-ref descriptor))
         (= #{:kind :plan :function :params :arguments-ref :invocation-ref
              :entry-lifecycle-ref :prior-public-invocation-scope-ref}
            (set (keys return-frame)))
         (= :function-return (:kind return-frame))
         (identical? (:plan return-frame) (:callee-plan-ref descriptor))
         (= (:function return-frame) (:callee-function descriptor))
         (identical? (:params return-frame)
                     (:callee-params-ref descriptor))
         (identical? (:arguments-ref return-frame)
                     (:arguments-ref descriptor))
         (identical? (:invocation-ref return-frame)
                     (:invocation-ref descriptor))
         (identical? (:entry-lifecycle-ref return-frame)
                     (:lifecycle-ref descriptor))
         (identical? (:prior-public-invocation-scope-ref return-frame)
                     public-invocation-scope)
         (= #{:kind :plan :env :remaining} (set (keys sequence-frame)))
         (= :sequence (:kind sequence-frame))
         (identical? (:plan sequence-frame) (:callee-plan-ref descriptor))
         (identical? (:env sequence-frame) (:environment-ref descriptor)))))

(defn- p15-s23-sh07-b51-runtime-machine-enter-pending-root1!
  [runtime action frames public-invocation-scope cleanup-owner]
  (if (= 4 (count action))
    public-invocation-scope
    (let [plan (nth action 1 nil)
          descriptor (nth action 4 nil)
          lifecycle (:lifecycle-ref descriptor)]
      (when-not (and (p15-s23-sh07-b51-pending-entry-shape?
                      descriptor action frames public-invocation-scope)
                     (p15-s23-sh07-b51-atomic-reference? lifecycle)
                     (p15-s23-sh07-b51-atomic-reference? cleanup-owner))
        (p15-s23-sh07-b51-close-authentic-pending-entry! descriptor)
        (p15-s23-sh07-b51-pending-entry-fail! plan))
      (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
            roots (get-in context [:execution-bundle :public-roots])
            root (when (and (vector? roots) (= 8 (count roots)))
                   (nth roots 0))
            local-plan (when root (p15-s23-sh07-b51-root-plan context root))
            definition (when local-plan
                         (get-in local-plan [:functions (:function root)]))
            arguments (:arguments-ref descriptor)
            pending (.get ^java.util.concurrent.atomic.AtomicReference
                          lifecycle)]
        (when-not
         (and context
              (= {:state :pending} pending)
              (= {:role :root1-producer
                  :projection
                  :gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v3}
                 (p15-s23-sh07-b51-root-receipt-spec root))
              (identical? plan local-plan)
              (identical? definition (:callee-definition-ref descriptor))
              (= (:function root) (:callee-function descriptor))
              (identical? (:params definition)
                          (:callee-params-ref descriptor))
              (identical? (first (:instructions definition))
                          (:first-instruction-ref descriptor))
              (= (vec (rest (:instructions definition)))
                 (:remaining (:callee-sequence-frame-ref descriptor)))
              (vector? arguments)
              (= 1 (count arguments))
              (identical? (:raw-ref context) (nth arguments 0))
              (identical? (:carrier context)
                          (:v18-executable-descriptor
                           (:raw-ref context))))
          (when (= {:state :pending} pending)
            (.compareAndSet
             ^java.util.concurrent.atomic.AtomicReference lifecycle
             pending {:state :closed}))
          (p15-s23-sh07-b51-pending-entry-fail! plan))
        (when-not (.compareAndSet
                   ^java.util.concurrent.atomic.AtomicReference lifecycle
                   pending {:state :entering})
          (p15-s23-sh07-b51-pending-entry-fail! plan))
        (let [scope (Object.)
              receipt
              (p15-s23-sh07-b51-receipt-value
               context root local-plan arguments
               (:invocation-ref descriptor) lifecycle)
              issued (when receipt
                       (p15-s23-sh07-b51-publish-receipt!
                        context receipt scope lifecycle))]
          (when-not issued
            (.set ^java.util.concurrent.atomic.AtomicReference lifecycle
                  {:state :closed})
            (p15-s23-sh07-b51-pending-entry-fail! plan))
          (let [entering (.get ^java.util.concurrent.atomic.AtomicReference
                               lifecycle)
                active {:state :active :issued-payload-ref issued
                        :scope-ref scope}]
            (when-not (and (= {:state :entering} entering)
                           (.compareAndSet
                            ^java.util.concurrent.atomic.AtomicReference lifecycle
                            entering active))
              (p15-s23-sh07-b51-close-issued-owner!
               context lifecycle issued)
              (p15-s23-sh07-b51-pending-entry-fail! plan))
            (let [owner-payload {:lifecycle-ref lifecycle
                                 :issued-payload-ref issued}]
              (when-not (.compareAndSet
                         ^java.util.concurrent.atomic.AtomicReference
                         cleanup-owner nil owner-payload)
                (p15-s23-sh07-b51-close-issued-owner!
                 context lifecycle issued)
                (p15-s23-sh07-b51-pending-entry-fail! plan))
              scope)))))))

(defn- p15-s23-sh07-b51-runtime-machine-complete-pending-root1!
  [runtime frame public-invocation-scope cleanup-owner]
  (let [lifecycle (:entry-lifecycle-ref frame)
        context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        active (when (p15-s23-sh07-b51-atomic-reference? lifecycle)
                 (.get ^java.util.concurrent.atomic.AtomicReference lifecycle))
        issued (:issued-payload-ref active)
        owner-payload
        (when (p15-s23-sh07-b51-atomic-reference? cleanup-owner)
          (.get ^java.util.concurrent.atomic.AtomicReference cleanup-owner))]
    (when-not (and context
                   (= p15-s23-sh07-b51-active-owner-keys
                      (set (keys active)))
                   (= :active (:state active))
                   (identical? public-invocation-scope (:scope-ref active))
                   (identical? lifecycle (:owner-ref issued))
                   (= #{:lifecycle-ref :issued-payload-ref}
                      (set (keys owner-payload)))
                   (identical? lifecycle (:lifecycle-ref owner-payload))
                   (identical? issued (:issued-payload-ref owner-payload))
                   (p15-s23-sh07-b51-close-issued-owner!
                    context lifecycle issued))
      (p15-s23-sh07-b51-pending-entry-fail! (:plan frame)))
    (.compareAndSet ^java.util.concurrent.atomic.AtomicReference cleanup-owner
                    owner-payload nil)
    (:prior-public-invocation-scope-ref frame)))

(defn- p15-s23-sh07-b51-runtime-machine-close-owned-entry!
  [runtime cleanup-owner]
  (try
    (when (p15-s23-sh07-b51-atomic-reference? cleanup-owner)
      (when-let [owner-payload
                 (.get ^java.util.concurrent.atomic.AtomicReference
                       cleanup-owner)]
        (when (= #{:lifecycle-ref :issued-payload-ref}
                 (set (keys owner-payload)))
          (when-let [context
                     (p15-s23-sh07-b51-admitted-runtime-context runtime)]
            (p15-s23-sh07-b51-close-issued-owner!
             context (:lifecycle-ref owner-payload)
             (:issued-payload-ref owner-payload)))
          (.compareAndSet
           ^java.util.concurrent.atomic.AtomicReference cleanup-owner
           owner-payload nil))))
    (catch Throwable _ nil))
  nil)
