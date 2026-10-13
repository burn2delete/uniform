(def ^:private p15-s23-sh07-b51-receipt-keys
  #{:artifact :schema-version :fact-projection :role :root-ordinal
    :root-function :root-arity :semantic-root-hash :certificate-a-id
    :certificate-b-id :token-ref :runtime-ref :invocation-ref :thread-ref
    :raw-ref :carrier-ref :authority-ref :root-ref :plan-ref :arguments-ref
    :state-ref})

(def ^:private p15-s23-sh07-b51-pending-entry-keys
  #{:artifact :schema-version :callee-plan-ref :callee-definition-ref
    :callee-function :callee-params-ref :arguments-ref :environment-ref
    :first-instruction-ref :callee-return-frame-ref
    :callee-sequence-frame-ref :invocation-ref :lifecycle-ref})
(def ^:private p15-s23-sh07-b51-issued-payload-keys
  #{:state :receipt-ref :scope-ref :owner-ref})

(def ^:private p15-s23-sh07-b51-active-owner-keys
  #{:state :issued-payload-ref :scope-ref})

(defn- p15-s23-sh07-b51-receipt-fail! [plan reason]
  (fail! "L2-BUILTIN-ERROR"
         "compiler-internal SH-07 validation receipt rejected its invocation"
         {:source-span {:source (get-in plan [:source :path])}
          :function 'sh07-b51-admitted-executable-carrier-validation?
          :reason reason
          :result-committed? false
          :output-committed? false
          :remediation
          "Execute the exact authenticated receipt occurrence within its live public-root invocation."}))

(defn- p15-s23-sh07-b51-pending-entry-fail! [plan]
  (fail! "L2-UNKNOWN-CORE-FORM"
         "bounded SH-07 B51 pending Root-1 entry was not authenticated"
         {:source-span {:source (get-in plan [:source :path])}
          :reason :unauthenticated-pending-root1-entry
          :result-committed? false
          :output-committed? false
          :remediation
          "Enter the exact public Root-1 definition through the admitted first-eval boundary."}))

(defn- p15-s23-sh07-b51-atomic-reference? [value]
  (instance? java.util.concurrent.atomic.AtomicReference value))

(defn- p15-s23-sh07-b51-root-receipt-spec [root]
  (case (:root-ordinal root)
    1 (when (and (= 'sh07-b51-build-template (:function root))
                 (= 1 (:arity root)))
        {:role :root1-producer
         :projection
         :gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v3})
    6 (when (and (= 'sh07-b51-build-independent-verifier-binding
                    (:function root))
                 (= 6 (:arity root)))
        {:role :root6-independent-verifier
         :projection
         :gravity/sh07-b51-attempt32-r6-nested-carrier-facts-v3})
    nil))

(defn- p15-s23-sh07-b51-root-plan [context root]
  (first (filter #(= (:plan-id root) (:plan-id %))
                 (:module-plans context))))

(defn- p15-s23-sh07-b51-receipt-value
  [context root plan arguments invocation owner]
  (let [carrier (:carrier context)
        certificates (:compile-census-certificates carrier)
        spec (p15-s23-sh07-b51-root-receipt-spec root)]
    (when (and spec
               (vector? certificates)
               (= 2 (count certificates)))
      {:artifact :gravity/sh07-b51-nested-validation-receipt-v3
       :schema-version 3
       :fact-projection (:projection spec)
       :role (:role spec)
       :root-ordinal (:root-ordinal root)
       :root-function (:function root)
       :root-arity (:arity root)
       :semantic-root-hash (:semantic-root-hash carrier)
       :certificate-a-id (get-in certificates [0 :certificate-id])
       :certificate-b-id (get-in certificates [1 :certificate-id])
       :token-ref (.get ^java.lang.ref.WeakReference (:token-ref context))
       :runtime-ref (.get ^java.lang.ref.WeakReference (:runtime-ref context))
       :invocation-ref invocation
       :thread-ref (:thread-ref context)
       :raw-ref (:raw-ref context)
       :carrier-ref carrier
       :authority-ref (:authority-ref context)
       :root-ref root
       :plan-ref plan
       :arguments-ref arguments
       :state-ref
       (java.util.concurrent.atomic.AtomicReference. {:state :fresh})})))

(defn- p15-s23-sh07-b51-invalidate-receipt! [receipt]
  (try
    (when-let [state-ref (:state-ref receipt)]
      (when (p15-s23-sh07-b51-atomic-reference? state-ref)
        (loop [attempt 0]
          (when (< attempt 3)
            (let [current (.get ^java.util.concurrent.atomic.AtomicReference
                                state-ref)]
              (when-not (= :invalid (:state current))
                (if (.compareAndSet
                     ^java.util.concurrent.atomic.AtomicReference state-ref
                     current {:state :invalid})
                  nil
                  (recur (inc attempt)))))))))
    (catch Throwable _ nil)))

(defn- p15-s23-sh07-b51-close-issued-owner!
  [context owner issued-payload]
  (try
    (when (and context
               (p15-s23-sh07-b51-atomic-reference? owner)
               (= p15-s23-sh07-b51-issued-payload-keys
                  (set (keys issued-payload)))
               (= :issued (:state issued-payload))
               (identical? owner (:owner-ref issued-payload)))
      (let [slot (:receipt-slot-ref context)
            receipt (:receipt-ref issued-payload)
            owner-state
            (.get ^java.util.concurrent.atomic.AtomicReference owner)
            active-owner?
            (and (= p15-s23-sh07-b51-active-owner-keys
                    (set (keys owner-state)))
                 (= :active (:state owner-state))
                 (identical? issued-payload (:issued-payload-ref owner-state)))
            entering-owner? (= {:state :entering} owner-state)
            current (when (p15-s23-sh07-b51-atomic-reference? slot)
                      (.get ^java.util.concurrent.atomic.AtomicReference slot))
            retired-owned?
            (when (or active-owner? entering-owner?)
              (if (identical? issued-payload current)
                (or (.compareAndSet
                     ^java.util.concurrent.atomic.AtomicReference slot
                     current {:state :retired})
                    (and active-owner?
                         (= {:state :retired}
                            (.get ^java.util.concurrent.atomic.AtomicReference slot))))
                (and active-owner? (= {:state :retired} current))))]
        (when retired-owned?
          (p15-s23-sh07-b51-invalidate-receipt! receipt)
          (.compareAndSet ^java.util.concurrent.atomic.AtomicReference owner
                          owner-state {:state :closed})
          true)))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-publish-receipt!
  [context receipt scope owner]
  (let [slot (:receipt-slot-ref context)
        available (.get ^java.util.concurrent.atomic.AtomicReference slot)
        issued {:state :issued :receipt-ref receipt
                :scope-ref scope :owner-ref owner}]
    (when (and (= {:state :available} available)
               (.compareAndSet ^java.util.concurrent.atomic.AtomicReference slot
                               available issued))
      issued)))

(defn- p15-s23-sh07-b51-issue-top-level-validation-receipt!
  [context]
  (let [root (:selected-root-ref context)
        spec (p15-s23-sh07-b51-root-receipt-spec root)
        owner (:top-level-owner-ref context)]
    (if spec
      (let [plan (:selected-plan-ref context)
            arguments (:arguments-ref context)
            invocation (:top-level-invocation-ref context)
            scope (Object.)
            receipt
            (p15-s23-sh07-b51-receipt-value
             context root plan arguments invocation owner)
            issued
            (when (and receipt
                       (identical? plan
                                   (p15-s23-sh07-b51-root-plan context root))
                       (identical? (:raw-ref context) (nth arguments 0 nil))
                       (identical? (:carrier context)
                                   (:v18-executable-descriptor
                                    (:raw-ref context))))
              (p15-s23-sh07-b51-publish-receipt!
               context receipt scope owner))
            entering (.get ^java.util.concurrent.atomic.AtomicReference owner)
            active {:state :active :issued-payload-ref issued :scope-ref scope}]
        (when-not (and issued
                       (= {:state :entering} entering)
                       (.compareAndSet
                        ^java.util.concurrent.atomic.AtomicReference owner
                        entering active))
          (when issued
            (p15-s23-sh07-b51-close-issued-owner!
             context owner issued))
          (throw (ex-info "top-level SH-07 receipt mint failed"
                          {:id "L2-UNKNOWN-CORE-FORM"
                           :reason :unauthenticated-b51-receipt-mint}))))
      (let [entering (.get ^java.util.concurrent.atomic.AtomicReference owner)]
        (when-not (and (= {:state :entering} entering)
                       (.compareAndSet
                        ^java.util.concurrent.atomic.AtomicReference owner
                        entering {:state :active}))
          (throw (ex-info "top-level SH-07 lifecycle activation failed"
                          {:id "L2-UNKNOWN-CORE-FORM"
                           :reason :unauthenticated-b51-receipt-mint})))))
    context))
(defn- p15-s23-sh07-b51-top-level-public-invocation-scope
  [runtime]
  (when-let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)]
    (when (p15-s23-sh07-b51-root-receipt-spec
           (:selected-root-ref context))
      (let [issued (.get ^java.util.concurrent.atomic.AtomicReference
                         (:receipt-slot-ref context))
            owner (:top-level-owner-ref context)
            owner-state (.get ^java.util.concurrent.atomic.AtomicReference
                              owner)]
        (when (and (= p15-s23-sh07-b51-issued-payload-keys
                      (set (keys issued)))
                   (= :issued (:state issued))
                   (identical? owner (:owner-ref issued))
                   (= p15-s23-sh07-b51-active-owner-keys
                      (set (keys owner-state)))
                   (= :active (:state owner-state))
                   (identical? issued (:issued-payload-ref owner-state))
                   (identical? (:scope-ref issued) (:scope-ref owner-state)))
          (:scope-ref issued))))))
(defn- p15-s23-sh07-b51-retire-admitted-runtime-context! [context]
  (try
    (when context
      (let [slot (:receipt-slot-ref context)
            current (.get ^java.util.concurrent.atomic.AtomicReference slot)]
        (if (= :issued (:state current))
          (p15-s23-sh07-b51-close-issued-owner!
           context (:owner-ref current) current)
          (when (= {:state :available} current)
            (.compareAndSet ^java.util.concurrent.atomic.AtomicReference slot
                            current {:state :retired})))
        (let [owner (:top-level-owner-ref context)
              owner-state (.get ^java.util.concurrent.atomic.AtomicReference
                                owner)]
          (when-not (= :closed (:state owner-state))
            (.compareAndSet ^java.util.concurrent.atomic.AtomicReference owner
                            owner-state {:state :closed})))))
    (catch Throwable _ nil))
  nil)
(defn- p15-s23-sh07-b51-unregister-admitted-runtime-context! [runtime]
  (try
    (when-let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)]
      (p15-s23-sh07-b51-retire-admitted-runtime-context! context)
      (p15-s23-sh07-b51-remove-runtime-context!
       (:sh07-b51-runtime-context-token runtime) context))
    (catch Throwable _ nil))
  nil)
