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
