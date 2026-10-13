
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
