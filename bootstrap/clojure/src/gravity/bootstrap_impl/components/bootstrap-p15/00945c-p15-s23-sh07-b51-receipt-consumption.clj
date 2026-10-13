(defn- p15-s23-sh07-b51-receipt-instruction-children [instruction]
  (case (:op instruction)
    :println (:args instruction)
    :do (:body instruction)
    :if [(:test instruction) (:then instruction) (:else instruction)]
    :let (concat (map :expr (:bindings instruction)) (:body instruction))
    :loop (concat (map :expr (:bindings instruction)) (:body instruction))
    :recur (:args instruction)
    :builtin-call (:args instruction)
    :function-call (:args instruction)
    :vector-literal (:items instruction)
    :set-literal (:items instruction)
    :map-literal (mapcat (fn [entry] [(:key entry) (:value entry)])
                         (:entries instruction))
    []))

(defn- p15-s23-sh07-b51-receipt-instruction-identical?
  [plan instruction]
  (let [definition
        (get-in plan [:functions (:compiler-internal-caller instruction)])]
    (loop [pending (vec (:instructions definition))]
      (if (empty? pending)
        false
        (let [candidate (peek pending)
              remaining (pop pending)]
          (if (identical? instruction candidate)
            true
            (recur
             (into remaining
                   (reverse
                    (filter map?
                            (p15-s23-sh07-b51-receipt-instruction-children
                             candidate)))))))))))

(defn- p15-s23-sh07-b51-assert-scoped-function-entry!
  [runtime plan callee args public-invocation-scope]
  (when public-invocation-scope
    (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
          issued (when context
                   (.get ^java.util.concurrent.atomic.AtomicReference
                         (:receipt-slot-ref context)))
          receipt (:receipt-ref issued)
          root (:selected-root-ref context)
          owner (:owner-ref issued)
          owner-state (when (p15-s23-sh07-b51-atomic-reference? owner)
                        (.get ^java.util.concurrent.atomic.AtomicReference owner))
          receipt-state-ref (:state-ref receipt)
          receipt-state
          (when (p15-s23-sh07-b51-atomic-reference? receipt-state-ref)
            (.get ^java.util.concurrent.atomic.AtomicReference
                  receipt-state-ref))
          selected-entry?
          (and (identical? plan (:selected-plan-ref context))
               (= callee (:function root)))]
      (when-not
       (and context
            (= p15-s23-sh07-b51-issued-payload-keys (set (keys issued)))
            (= :issued (:state issued))
            (identical? public-invocation-scope (:scope-ref issued))
            (identical? receipt (:receipt-ref issued))
            (if selected-entry?
              (and (contains? #{1 6} (:root-ordinal root))
                   (= p15-s23-sh07-b51-receipt-keys
                      (set (keys receipt)))
                   (= :gravity/sh07-b51-nested-validation-receipt-v3
                      (:artifact receipt))
                   (= 3 (:schema-version receipt))
                   (identical? args (:arguments-ref context))
                   (identical? args (:arguments-ref receipt))
                   (identical? plan (:plan-ref receipt))
                   (= callee (:root-function receipt))
                   (identical? (:top-level-invocation-ref context)
                               (:invocation-ref receipt))
                   (identical? (:top-level-owner-ref context)
                               owner)
                   (= p15-s23-sh07-b51-active-owner-keys
                      (set (keys owner-state)))
                   (= :active (:state owner-state))
                   (identical? issued (:issued-payload-ref owner-state))
                   (identical? public-invocation-scope
                               (:scope-ref owner-state))
                   (= {:state :fresh} receipt-state))
              true))
        (p15-s23-sh07-b51-receipt-fail!
         plan :unauthenticated-scoped-function-entry))))
  true)

(defn- p15-s23-sh07-b51-consume-admitted-carrier-validation!
  [runtime plan instruction carrier public-invocation-scope]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        issued (when context
                 (.get ^java.util.concurrent.atomic.AtomicReference
                       (:receipt-slot-ref context)))
        receipt (:receipt-ref issued)
        root (:root-ref receipt)
        root-spec (p15-s23-sh07-b51-root-receipt-spec root)
        caller (:compiler-internal-caller instruction)
        expected-role
        (case caller
          sh07-b51-root1-admitted-raw-carrier-shape? :root1-producer
          sh07-b51-r6-c30-request-proof? :root6-independent-verifier
          nil)
        expected-projection
        (case expected-role
          :root1-producer
          :gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v3
          :root6-independent-verifier
          :gravity/sh07-b51-attempt32-r6-nested-carrier-facts-v3
          nil)
        owner (:owner-ref issued)
        owner-state (when (p15-s23-sh07-b51-atomic-reference? owner)
                      (.get ^java.util.concurrent.atomic.AtomicReference owner))
        receipt-state-ref (:state-ref receipt)
        receipt-state (when (p15-s23-sh07-b51-atomic-reference?
                             receipt-state-ref)
                        (.get ^java.util.concurrent.atomic.AtomicReference
                              receipt-state-ref))
        authority (:authority-ref context)
        authority-products (:products authority)
        certificates (:compile-census-certificates (:carrier context))]
    (when-not
     (and context
          (= p15-s23-sh07-b51-issued-payload-keys (set (keys issued)))
          (= :issued (:state issued))
          (map? receipt)
          (= p15-s23-sh07-b51-receipt-keys (set (keys receipt)))
          (= :gravity/sh07-b51-nested-validation-receipt-v3
             (:artifact receipt))
          (= 3 (:schema-version receipt))
          (= expected-role (:role receipt) (:role root-spec))
          (= expected-projection
             (:fact-projection receipt) (:projection root-spec))
          (= (:root-ordinal root) (:root-ordinal receipt))
          (= (:function root) (:root-function receipt))
          (= (:arity root) (:root-arity receipt))
          (= (:semantic-root-hash (:carrier context))
             (:semantic-root-hash receipt))
          (= (get-in certificates [0 :certificate-id])
             (:certificate-a-id receipt))
          (= (get-in certificates [1 :certificate-id])
             (:certificate-b-id receipt))
          (identical? (:sh07-b51-runtime-context-token runtime)
                      (:token-ref receipt))
          (identical? runtime (:runtime-ref receipt))
          (identical? (Thread/currentThread) (:thread-ref receipt))
          (identical? (:raw-ref context) (:raw-ref receipt))
          (identical? (:carrier context) carrier)
          (identical? carrier (:carrier-ref receipt))
          (= #{:carrier :products} (set (keys authority)))
          (identical? (:carrier context) (:carrier authority))
          (identical? (:module-source-descriptors context)
                      (:module-source-descriptors authority-products))
          (identical? (:link-manifest context)
                      (:link-manifest authority-products))
          (identical? (:module-plans context)
                      (:module-plans authority-products))
          (identical? (:execution-bundle context)
                      (:execution-bundle authority-products))
          (identical? (:authority-ref context) (:authority-ref receipt))
          (identical? root
                      (nth (get-in context [:execution-bundle :public-roots])
                           (dec (:root-ordinal root))))
          (identical? (:plan-ref receipt)
                      (p15-s23-sh07-b51-root-plan context root))
          (vector? (:arguments-ref receipt))
          (= (:root-arity receipt) (count (:arguments-ref receipt)))
          (identical? (:raw-ref context)
                      (nth (:arguments-ref receipt) 0 nil))
          (identical? public-invocation-scope (:scope-ref issued))
          (identical? receipt (:receipt-ref issued))
          (identical? owner (:owner-ref issued))
          (= p15-s23-sh07-b51-active-owner-keys
             (set (keys owner-state)))
          (= :active (:state owner-state))
          (identical? issued (:issued-payload-ref owner-state))
          (identical? public-invocation-scope (:scope-ref owner-state))
          (some #(identical? plan %) (:module-plans context))
          (p15-s23-sh07-b51-receipt-instruction-identical?
           plan instruction)
          (= {:state :fresh} receipt-state))
      (p15-s23-sh07-b51-receipt-fail! plan :unauthenticated-receipt))
    (when-not (.compareAndSet
               ^java.util.concurrent.atomic.AtomicReference receipt-state-ref
               receipt-state {:state :consumed})
      (p15-s23-sh07-b51-receipt-fail! plan :consumed-receipt))
    true))
