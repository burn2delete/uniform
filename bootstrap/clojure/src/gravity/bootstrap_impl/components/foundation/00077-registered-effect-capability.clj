

(defn registered-effect-capability
  [effect]
  (:capability (effect-registry-entry effect)))

(defn check-effect-handle-node
  [checker ctx node]
  (let [arg-nodes (:arguments node)]
    (when-not (>= (count arg-nodes) 3)
      (typed-diagnostic! "L6-HANDLER-TYPE"
                         "effect/handle requires an effect label, handler identity, and body"
                         node
                         "Use (effect/handle :effect :handler body)."))
    (let [label-fact (check-typed-node checker ctx (first arg-nodes))
          handler-fact (check-typed-node checker ctx (second arg-nodes))
          effect-label (:value label-fact)
          handler-id (:value handler-fact)
          handler-capability :test/fixture]
      (when-not (keyword? effect-label)
        (typed-diagnostic! "L6-HANDLER-TYPE"
                           "handler effect label must be a keyword"
                           node
                           "Use a registered effect keyword as the first effect/handle argument."))
      (check-effect-registry! node effect-label)
      (when-not (keyword? handler-id)
        (typed-diagnostic! "L6-HANDLER-TYPE"
                           "handler identity must be a keyword"
                           node
                           "Use a stable handler identity keyword."))
      (when (contains? #{:kernel :firmware :hardware} (:profile @ctx))
        (typed-diagnostic! "L6-HANDLER-PROFILE"
                           "active profile rejects this handler form"
                           node
                           "Use a profile-supported handler or lower to explicit effect operations."
                           {:effect effect-label
                            :profile (:profile @ctx)
                            :handler handler-id}))
      (when-not (contains? (:handler-grants @ctx) handler-capability)
        (typed-diagnostic! "L6-HANDLER-CAPABILITY"
                           "handler lacks fixture or interpretation capability"
                           node
                           "Grant the handler capability in metadata or remove the handler."
                           {:effect effect-label
                            :handler handler-id
                            :requested-capability handler-capability}))
      (let [covered-capability (registered-effect-capability effect-label)
            child (child-context ctx)]
        (swap! child assoc
               :handler-covered-effects #{effect-label}
               :handler-covered-capabilities (cond-> #{}
                                                covered-capability (conj covered-capability)))
        (let [body-facts (mapv #(check-typed-node checker child %) (drop 2 arg-nodes))
              body-effects (collect-fact-effects body-facts)
              body-capabilities (collect-fact-capabilities body-facts)]
          (when-not (contains? body-effects effect-label)
            (typed-diagnostic! "L6-HANDLER-COVERAGE"
                               "handler claims an effect label that no handled occurrence produces"
                               node
                               "Handle a body that actually performs the label, or remove the handler claim."
                               {:effect effect-label
                                :handler handler-id}))
          (check-effects-and-capabilities! checker ctx node #{} #{handler-capability})
          (let [escaping-effects (disj body-effects effect-label)
                escaping-capabilities (cond-> body-capabilities
                                        covered-capability (disj covered-capability))
                handled-record {:node-id (:node-id node)
                                :effect-label effect-label
                                :request-type "EffectRequest"
                                :response-type "EffectResponse"
                                :resume-mode :abort
                                :replay-mode :fixture
                                :handler-identity handler-id
                                :handler-residual-effects #{}
                                :required-handler-capabilities #{handler-capability}
                                :original-label-preserved true
                                :handled-node-ids (mapv :node-id
                                                        (filter #(contains? (:effects %) effect-label)
                                                                body-facts))}]
            (record-checker! checker :handled-effect-table handled-record)
            (record-checker! checker :handler-capability-and-profile-report
                             {:node-id (:node-id node)
                              :handler handler-id
                              :effect-label effect-label
                              :active-profile (:profile @ctx)
                              :required-capabilities #{handler-capability}
                              :status :accepted})
            (record-checker! checker :continuation-and-replay-safety-report
                             {:node-id (:node-id node)
                              :handler handler-id
                              :resume-mode :abort
                              :continuation-safety :proven-affine
                              :replay-safety :fixture-recorded})
            (typed-fact checker node (if (seq body-facts) (:type (last body-facts)) "Nil")
                        escaping-effects
                        (conj escaping-capabilities handler-capability)
                        {:children (vec (concat [label-fact handler-fact] body-facts))
                         :callee 'effect/handle
                         :handled-effects [handled-record]})))))))

(def dispatch-operator-modes
  {'dispatch/direct :direct
   'dispatch/dictionary :dictionary
   'dispatch/vtable :vtable
   'dispatch/dynamic :hosted-dynamic
   'dispatch/multimethod :multimethod
   'dispatch/host-normalized :host-interface
   'dispatch/tool :artifact-boundary})

(defn dispatch-arg-value
  [args index]
  (:value (nth args index nil)))

(defn record-dispatch-call!
  [checker node operator args effects capabilities]
  (when-let [mode (get dispatch-operator-modes operator)]
    (let [record {:node-id (:node-id node)
                  :call operator
                  :protocol (dispatch-arg-value args 0)
                  :receiver (dispatch-arg-value args 1)
                  :method (dispatch-arg-value args 2)
                  :dispatch-mode mode
                  :effects effects
                  :capabilities capabilities
                  :profile (:profile node)
                  :implementation (str (name (or (dispatch-arg-value args 1) :unknown))
                                       "/"
                                       (name (or (dispatch-arg-value args 2) :unknown)))
                  :fallback-artifact (when (contains? #{:hosted-dynamic :multimethod
                                                        :artifact-boundary}
                                                      mode)
                                       :recorded)}]
      (record-checker! checker :dispatch-mode-records record)
      (when (= :host-interface mode)
        (record-checker! checker :host-interop-dispatch-records
                         (assoc record
                                :nullability :normalized
                                :exceptions :mapped
                                :host-type-identity (:receiver record)))))))

(defn record-error-call!
  [checker node operator args effects capabilities]
  (case operator
    option/some
    (record-checker! checker :error-type-declarations
                     {:node-id (:node-id node)
                      :family :option
                      :constructor :Some
                      :type "Option[T]"})
    option/none
    (record-checker! checker :error-type-declarations
                     {:node-id (:node-id node)
                      :family :option
                      :constructor :None
                      :type "Option[T]"})
    result/ok
    (record-checker! checker :error-type-declarations
                     {:node-id (:node-id node)
                      :family :result
                      :constructor :Ok
                      :type "Result[T,E]"})
    result/err
    (record-checker! checker :error-type-declarations
                     {:node-id (:node-id node)
                      :family :result
                      :constructor :Err
                      :type "Result[T,E]"})
    panic
    (record-checker! checker :panic-lowering-records
                     {:node-id (:node-id node)
                      :profile (:profile node)
                      :lowering :hosted-panic-record
                      :effects effects
                      :provenance :source-span-preserved})
    safety/check
    (record-checker! checker :safety-check-failure-records
                     {:node-id (:node-id node)
                      :check (dispatch-arg-value args 0)
                      :failure-behavior :typed-record
                      :effects effects})
    host/error-normalized
    (record-checker! checker :host-error-normalization-records
                     {:node-id (:node-id node)
                      :host-error (dispatch-arg-value args 0)
                      :nullability :normalized
                      :exceptions :mapped
                      :type-contract :gravity-error})
    ffi/error-mapped
    (record-checker! checker :ffi-error-mapping-artifacts
                     {:node-id (:node-id node)
                      :convention (dispatch-arg-value args 0)
                      :errno :mapped
                      :nullability :mapped
                      :ownership-on-failure :preserved
                      :cleanup :recorded
                      :capabilities capabilities})
    workflow/failure-recorded
    (record-checker! checker :workflow-failure-records
                     {:node-id (:node-id node)
                      :step-id (dispatch-arg-value args 0)
                      :retry-policy :recorded
                      :compensation-policy :recorded
                      :replay-id :stage0-replay
                      :effects effects})
    ai/error-recorded
    (record-checker! checker :ai-tool-error-records
                     {:node-id (:node-id node)
                      :provider (dispatch-arg-value args 0)
                      :policy :recorded
                      :schema :recorded
                      :budget :recorded
                      :audit :recorded
                      :capabilities capabilities})
    nil))

(def memory-regime->family
  {:gc-backed :gc
   :ownership-backed :ownership
   :borrowing :borrow
   :region-memory :region
   :arena-memory :arena
   :stack-allocation :stack
   :static-allocation :static
   :raw-memory :raw
   :mmio :mmio
   :device-memory :gpu-device
   :host-managed :host-managed
   :initialization :initialization
   :bounds-checked :bounds-check})

(defn symbol-arg-name
  [args index]
  (let [arg (nth args index nil)]
    (when (= :symbol (:source-kind arg))
      (:name arg))))