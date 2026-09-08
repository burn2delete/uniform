(def ^:private p15-s23-sh07-b51-runtime-machine-maximum-steps 8388608)
(def ^:private p15-s23-sh07-b51-runtime-machine-maximum-frames 4096)

(defn- p15-s23-sh07-b51-runtime-machine-fail! [plan reason]
  (fail! "L2-UNKNOWN-CORE-FORM"
         "bounded SH-07 B51 cross-plan runtime rejected execution"
         {:source-span {:source (get-in plan [:source :path])}
          :reason reason
          :result-committed? false
          :output-committed? false
          :remediation "Execute the authenticated bounded M3 plan bundle."}))

(defn- p15-s23-sh07-b51-runtime-machine-bind [names values]
  (if (= (count names) (count values))
    (into {} (map vector names values))
    nil))

(defn- p15-s23-sh07-b51-runtime-machine-sequence-action
  [plan env instructions frames]
  (if (empty? instructions)
    [[:value nil] frames]
    [[:eval plan env (first instructions)]
     (conj frames {:kind :sequence :plan plan :env env
                   :remaining (vec (rest instructions))})]))

(defn- p15-s23-sh07-b51-runtime-machine-call-action
  [plan function args frames public-invocation-scope]
  (let [definition (get-in plan [:functions function])
        params (:params definition)
        env (when definition
              (p15-s23-sh07-b51-runtime-machine-bind params args))]
    (when-not (and definition env)
      (if definition
        (p15-s23-stage2-runtime-fail-call-arity!
         "L2-FUNCTION-ARITY" plan function args (count params))
        (p15-s23-sh07-b51-runtime-machine-fail!
         plan :unknown-linked-function)))
    (if (and (= 'sh07-b51-build-template function)
             (= 1 (count params))
             (= 1 (count args))
             (seq (:instructions definition)))
      (let [invocation (Object.)
            lifecycle
            (java.util.concurrent.atomic.AtomicReference. {:state :pending})
            return-frame
            {:kind :function-return :plan plan
             :function function :params params
             :arguments-ref args :invocation-ref invocation
             :entry-lifecycle-ref lifecycle
             :prior-public-invocation-scope-ref public-invocation-scope}
            [action next-frames]
            (p15-s23-sh07-b51-runtime-machine-sequence-action
             plan env (:instructions definition) (conj frames return-frame))
            sequence-frame (peek next-frames)
            descriptor
            (p15-s23-sh07-b51-pending-entry-descriptor
             {:artifact :gravity/sh07-b51-pending-root1-entry-v3
              :schema-version 3
              :callee-plan-ref plan
              :callee-definition-ref definition
              :callee-function function
              :callee-params-ref params
              :arguments-ref args
              :environment-ref env
              :first-instruction-ref (first (:instructions definition))
              :callee-return-frame-ref return-frame
              :callee-sequence-frame-ref sequence-frame
              :invocation-ref invocation
              :lifecycle-ref lifecycle})]
        [(conj action descriptor) next-frames])
      (p15-s23-sh07-b51-runtime-machine-sequence-action
       plan env (:instructions definition)
       (conj frames {:kind :function-return :plan plan
                     :function function :params params})))))

(defn- p15-s23-sh07-b51-runtime-machine-args-action
  [plan env instructions frame frames]
  (if (empty? instructions)
    [[:value []] (conj frames (assoc frame :args-complete? true))]
    [[:eval plan env (first instructions)]
     (conj frames (assoc frame :kind :arguments
                         :target-kind (:kind frame)
                         :plan plan :env env :values []
                         :remaining (vec (rest instructions))))]))

(defn- p15-s23-sh07-b51-runtime-machine-invoke-builtin
  [runtime plan instruction values public-invocation-scope]
  (let [function (:function instruction)]
    (if (contains? instruction :compiler-internal-provenance)
      (p15-s23-sh07-runtime-invoke-compiler-internal-builtin
       runtime plan instruction values public-invocation-scope)
      (p15-s23-stage2-runtime-invoke-builtin
       runtime plan function values))))

(defn- p15-s23-sh07-b51-runtime-machine-start-collection
  [plan env items finish frames]
  (if (empty? items)
    [[:value (case finish :vector [] :set #{} nil)] frames]
    [[:eval plan env (first items)]
     (conj frames {:kind :collection :plan plan :env env
                   :finish finish :values []
                   :remaining (vec (rest items))})]))

(defn- p15-s23-sh07-b51-runtime-machine-eval
  [runtime plan env instruction frames public-invocation-scope]
  (let [op (:op instruction)]
    (case op
      :literal [[:value (:value instruction)] frames]
      :quote [[:value (:value instruction)] frames]
      :local
      (if (contains? env (:name instruction))
        [[:value (get env (:name instruction))] frames]
        (p15-s23-sh07-b51-runtime-machine-fail!
         plan :unknown-local))

      :vector-literal
      (p15-s23-sh07-b51-runtime-machine-start-collection
       plan env (:items instruction) :vector frames)
      :set-literal
      (p15-s23-sh07-b51-runtime-machine-start-collection
       plan env (:items instruction) :set frames)
      :map-literal
      (let [entries (:entries instruction)]
        (if (empty? entries)
          [[:value {}] frames]
          [[:eval plan env (:key (first entries))]
           (conj frames {:kind :map-key :plan plan :env env
                         :entry (first entries) :remaining (vec (rest entries))
                         :result {}})]))

      :do
      (p15-s23-sh07-b51-runtime-machine-sequence-action
       plan env (:body instruction) frames)
      :if
      [[:eval plan env (:test instruction)]
       (conj frames {:kind :if :plan plan :env env
                     :then (:then instruction) :else (:else instruction)})]
      :let
      (let [bindings (:bindings instruction)]
        (if (empty? bindings)
          (p15-s23-sh07-b51-runtime-machine-sequence-action
           plan env (:body instruction) frames)
          [[:eval plan env (:expr (first bindings))]
           (conj frames {:kind :let-binding :plan plan :env env
                         :binding (first bindings)
                         :remaining (vec (rest bindings))
                         :body (:body instruction)})]))
      :loop
      (let [bindings (:bindings instruction)]
        (if (empty? bindings)
          (p15-s23-sh07-b51-runtime-machine-sequence-action
           plan env (:body instruction)
           (conj frames {:kind :loop-return :plan plan :env env
                         :names [] :body (:body instruction)}))
          [[:eval plan env (:expr (first bindings))]
           (conj frames {:kind :loop-binding :plan plan :outer-env env
                         :env env :binding (first bindings)
                         :remaining (vec (rest bindings))
                         :names [] :values [] :body (:body instruction)})]))
      :recur
      (p15-s23-sh07-b51-runtime-machine-args-action
       plan env (:args instruction) {:kind :recur-arguments} frames)
      :builtin-call
      (p15-s23-sh07-b51-runtime-machine-args-action
       plan env (:args instruction)
       {:kind :builtin-arguments :instruction instruction} frames)
      :function-call
      (let [authority
            (when (contains? instruction :external-link)
              (p15-s23-sh07-b51-runtime-authorize-external-link
               runtime plan instruction))]
        (p15-s23-sh07-b51-runtime-machine-args-action
         plan env (:args instruction)
         {:kind :call-arguments :instruction instruction
          :caller-plan plan :authority authority} frames))
      :println
      (p15-s23-sh07-b51-runtime-machine-fail! plan :effectful-linked-plan)
      (p15-s23-sh07-b51-runtime-machine-fail! plan :unknown-instruction))))

(defn- p15-s23-sh07-b51-runtime-machine-continue
  [runtime value frame frames public-invocation-scope cleanup-owner]
  (let [recur? (p15-s23-stage2-runtime-recur-signal? value)
        result
        (case (:kind frame)
      :sequence
      (if (empty? (:remaining frame))
        [[:value value] frames]
        (if recur?
          (p15-s23-stage2-runtime-recur-fail!
           (:plan frame) nil 0 :non-tail-sequential-position)
          [[:eval (:plan frame) (:env frame) (first (:remaining frame))]
           (conj frames (assoc frame :remaining
                               (vec (rest (:remaining frame)))))]))

      :if
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-if-test)
        [[:eval (:plan frame) (:env frame)
          (if value (:then frame) (:else frame))] frames])

      :collection
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-collection)
        (let [values (conj (:values frame) value)]
          (if (empty? (:remaining frame))
            [[:value (case (:finish frame)
                       :vector values :set (set values))] frames]
            [[:eval (:plan frame) (:env frame) (first (:remaining frame))]
             (conj frames (assoc frame :values values
                                 :remaining (vec (rest (:remaining frame)))))])))

      :map-key
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-map-key)
        [[:eval (:plan frame) (:env frame) (:value (:entry frame))]
         (conj frames (assoc frame :kind :map-value :evaluated-key value))])
      :map-value
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-map-value)
        (let [result (assoc (:result frame) (:evaluated-key frame) value)]
          (if (empty? (:remaining frame))
            [[:value result] frames]
            (let [entry (first (:remaining frame))]
              [[:eval (:plan frame) (:env frame) (:key entry)]
               (conj frames {:kind :map-key :plan (:plan frame)
                             :env (:env frame) :entry entry
                             :remaining (vec (rest (:remaining frame)))
                             :result result})]))))

      :let-binding
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-let-binding)
        (let [env (assoc (:env frame) (get-in frame [:binding :name]) value)]
          (if (empty? (:remaining frame))
            (p15-s23-sh07-b51-runtime-machine-sequence-action
             (:plan frame) env (:body frame) frames)
            (let [binding (first (:remaining frame))]
              [[:eval (:plan frame) env (:expr binding)]
               (conj frames (assoc frame :env env :binding binding
                                   :remaining (vec (rest (:remaining frame)))))]))))

      :loop-binding
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-loop-binding)
        (let [env (assoc (:env frame) (get-in frame [:binding :name]) value)
              names (conj (:names frame) (get-in frame [:binding :name]))
              values (conj (:values frame) value)]
          (if (empty? (:remaining frame))
            (p15-s23-sh07-b51-runtime-machine-sequence-action
             (:plan frame) env (:body frame)
             (conj frames {:kind :loop-return :plan (:plan frame)
                           :outer-env (:outer-env frame) :names names
                           :body (:body frame)}))
            (let [binding (first (:remaining frame))]
              [[:eval (:plan frame) env (:expr binding)]
               (conj frames (assoc frame :env env :binding binding
                                   :names names :values values
                                   :remaining (vec (rest (:remaining frame)))))]))))

      :loop-return
      (if recur?
        (let [values (p15-s23-stage2-runtime-recur-values value)
              env (p15-s23-sh07-b51-runtime-machine-bind (:names frame) values)]
          (when-not env
            (p15-s23-stage2-runtime-recur-fail!
             (:plan frame) (count (:names frame)) (count values)
             :loop-arity-mismatch))
          (p15-s23-sh07-b51-runtime-machine-sequence-action
           (:plan frame) (merge (:outer-env frame) env) (:body frame)
           (conj frames frame)))
        [[:value value] frames])

      :function-return
      (if recur?
        (let [values (p15-s23-stage2-runtime-recur-values value)]
          (p15-s23-sh07-b51-runtime-machine-call-action
           (:plan frame) (:function frame) values frames
           public-invocation-scope))
        (if (contains? frame :entry-lifecycle-ref)
          [[:value value] frames
           (p15-s23-sh07-b51-runtime-machine-complete-pending-root1!
            runtime frame public-invocation-scope cleanup-owner)]
          [[:value value] frames]))

      :arguments
      (if recur?
        (p15-s23-stage2-runtime-recur-fail! (:plan frame) nil 0
                                             :recur-inside-call-argument)
        (let [values (conj (:values frame) value)]
          (if (empty? (:remaining frame))
            [[:value values]
             (conj frames (assoc frame :kind (:target-kind frame)
                                 :completed-values values))]
            [[:eval (:plan frame) (:env frame) (first (:remaining frame))]
             (conj frames (assoc frame :values values
                                 :remaining (vec (rest (:remaining frame)))))])))

      :builtin-arguments
      [[:value (p15-s23-sh07-b51-runtime-machine-invoke-builtin
                runtime (:plan frame) (:instruction frame) value
                public-invocation-scope)] frames]
      :recur-arguments [[:value (p15-s23-stage2-runtime-recur-signal value)] frames]
      :call-arguments
      (let [authority (:authority frame)
            callee-plan (or (:callee-plan authority) (:caller-plan frame))]
        (when authority
          (p15-s23-sh07-b51-runtime-record-external-call!
           runtime (:caller-plan frame) (:instruction frame) authority))
        (p15-s23-sh07-b51-runtime-machine-call-action
         callee-plan (get-in frame [:instruction :function]) value frames
         public-invocation-scope))
      (p15-s23-sh07-b51-runtime-machine-fail! (:plan frame) :unknown-frame))]
    (if (= 3 (count result))
      result
      [(nth result 0) (nth result 1) public-invocation-scope])))

(defn- p15-s23-sh07-b51-runtime-execute-external-machine
  [runtime caller-plan instruction authority args
   initial-public-invocation-scope]
  (p15-s23-sh07-b51-runtime-record-external-call!
   runtime caller-plan instruction authority)
  (let [callee-plan (:callee-plan authority)
        function (:function instruction)
        [initial-action initial-frames]
        (p15-s23-sh07-b51-runtime-machine-call-action
         callee-plan function args [] initial-public-invocation-scope)
        cleanup-owner (java.util.concurrent.atomic.AtomicReference. nil)]
    (try
      (loop [action initial-action
             frames initial-frames
             steps 0
             public-invocation-scope initial-public-invocation-scope]
        (when (or (>= steps p15-s23-sh07-b51-runtime-machine-maximum-steps)
                  (> (count frames)
                     p15-s23-sh07-b51-runtime-machine-maximum-frames))
          (p15-s23-sh07-b51-runtime-machine-fail!
           callee-plan :runtime-machine-bound))
        (case (first action)
          :eval
          (do
            (when-not (contains? #{4 5} (count action))
              (p15-s23-sh07-b51-runtime-machine-fail!
               callee-plan :runtime-machine-action))
            (let [[_ plan env current] action
                  next-scope
                  (p15-s23-sh07-b51-runtime-machine-enter-pending-root1!
                   runtime action frames public-invocation-scope cleanup-owner)
                  [next-action next-frames]
                  (p15-s23-sh07-b51-runtime-machine-eval
                   runtime plan env current frames next-scope)]
              (recur next-action next-frames (inc steps) next-scope)))
          :value
          (if (empty? frames)
            (second action)
            (let [[next-action next-frames next-scope]
                  (p15-s23-sh07-b51-runtime-machine-continue
                   runtime (second action) (peek frames) (pop frames)
                   public-invocation-scope cleanup-owner)]
              (recur next-action next-frames (inc steps) next-scope)))
          (p15-s23-sh07-b51-runtime-machine-fail!
           callee-plan :runtime-machine-action)))
      (finally
        (p15-s23-sh07-b51-runtime-machine-close-owned-entry!
         runtime cleanup-owner)))))
