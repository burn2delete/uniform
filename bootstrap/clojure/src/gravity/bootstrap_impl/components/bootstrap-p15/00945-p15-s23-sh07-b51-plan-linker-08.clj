
(defn- p15-s23-sh07-b51-runtime-authorize-external-link*
  [runtime plan instruction]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        descriptors (:module-source-descriptors context)
        manifest (:link-manifest context)
        plans (:module-plans context)
        bundle (:execution-bundle context)
        link (:external-link instruction)
        edge (when (and (map? link) (integer? (:edge-ordinal link))
                        (<= 0 (:edge-ordinal link))
                        (vector? (:edges manifest))
                        (< (:edge-ordinal link) (count (:edges manifest))))
               (nth (:edges manifest) (:edge-ordinal link) nil))
        route (when (and edge (vector? (:routes bundle))
                         (< (:ordinal edge) (count (:routes bundle))))
                (nth (:routes bundle) (:ordinal edge) nil))
        caller-plan (when route
                      (first (filter #(= (:caller-plan-id route) (:plan-id %))
                                     plans)))
        callee-plan (when route
                      (first (filter #(= (:callee-plan-id route) (:plan-id %))
                                     plans)))
        caller-definition
        (when edge (get-in plan [:functions (:caller-function edge)]))
        occurrences
        (when caller-definition
          (filter #(= instruction %)
                  (tree-seq coll? seq (:instructions caller-definition))))]
    (when-not
     (and context
          (= #{:op :function :args :external-link} (set (keys instruction)))
          (= :function-call (:op instruction))
          (= #{:artifact :manifest-id :edge-ordinal} (set (keys link)))
          (= :gravity/sh07-b51-external-call-v1 (:artifact link))
          (= (:manifest-id manifest) (:manifest-id link))
          edge route
          (= plan caller-plan)
          (= (:plan-id plan) (:caller-plan-id route))
          (= (:sh07-b51-module-source-id plan)
             (:caller-module-source-id edge))
          (= (:function instruction) (:callee-function edge))
          (= (count (:args instruction)) (:arity edge))
          (= 1 (count occurrences))
          (= (:plan-id callee-plan) (:callee-plan-id route))
          (= (:sh07-b51-module-source-id callee-plan)
             (:callee-module-source-id edge))
          (= (:callee-function edge) (:name (get-in callee-plan
                                                     [:functions
                                                      (:callee-function edge)])))
          (= (:arity edge) (:arity (get-in callee-plan
                                           [:functions
                                            (:callee-function edge)]))))
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))
    {:callee-plan callee-plan :edge edge :route route}))

(defn p15-s23-sh07-b51-runtime-authorize-external-link
  [runtime plan instruction]
  (try
    (p15-s23-sh07-b51-runtime-authorize-external-link*
     runtime plan instruction)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch clojure.lang.ExceptionInfo error
      (if (= :unauthenticated-b51-external-link (:reason (ex-data error)))
        (throw error)
        (p15-s23-sh07-b51-runtime-link-fail! plan instruction)))
    (catch Throwable _
      (p15-s23-sh07-b51-runtime-link-fail! plan instruction))))

(defn- p15-s23-sh07-b51-runtime-record-external-call!
  [runtime caller-plan instruction authority]
  (let [context (p15-s23-sh07-b51-admitted-runtime-context runtime)
        trace (:sh07-b51-external-call-trace runtime)
        callee-plan (:callee-plan authority)]
    (when-not (and context
                   (identical? trace (:external-call-trace context))
                   (instance? clojure.lang.IAtom trace)
                   (< (count @trace) p15-s23-sh07-b51-maximum-external-calls))
      (p15-s23-sh07-b51-runtime-link-fail! caller-plan instruction))
    (swap! trace conj
           {:bundle-id (get-in runtime
                               [:sh07-b51-execution-bundle :bundle-id])
            :edge-ordinal (get-in instruction [:external-link :edge-ordinal])
            :caller-plan-id (:plan-id caller-plan)
            :callee-plan-id (:plan-id callee-plan)
            :function (:function instruction)})))
