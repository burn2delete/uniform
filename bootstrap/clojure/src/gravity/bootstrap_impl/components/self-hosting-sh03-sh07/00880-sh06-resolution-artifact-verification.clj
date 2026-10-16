
(defn sh06-resolution-context-root
  [context]
  (let [source-path
        (or (get-in context [:external-binding-authority :source-path])
            "<sh06-resolution-context>")
        external (:external-binding-authority context)
        plan (:resolution-plan-authority context)]
    (when-not
     (and (some? context)
          (sh06-resolution-exact-keys?
           context #{:external-binding-authority :resolution-plan-authority})
          (map? external) (map? plan)
          (= (:content external) (:content plan))
          (= (:authority-id external)
             (reader-canonical-hash
              [:gravity/sh07-b51-c5-external-binding-authority-v1
               (dissoc external :authority-id)]))
          (= (:authority-id plan)
             (reader-canonical-hash
              [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
               (dissoc plan :authority-id)])))
      (sh06-resolution-c5-fail!
       source-path :context-root-authority {}))
    (sh06-resolution-context-plan-binding! source-path context)
    (reader-canonical-hash
     [:gravity/sh07-b51-sh06-context-root-v1 context])))

(defn sh06-resolution-context-validation
  [artifact context]
  (let [source-path (or (get-in artifact [:provenance :source-path])
                        "<sh06-resolution-artifact>")]
    (try
      (when (nil? context)
        (sh06-resolution-c5-fail! source-path :missing-context {}))
      (let [sh05 (:sh05-macro-artifact artifact)
            base (sh06-resolution-request source-path sh05)
            expected
            (sh06-resolution-c5-authority-request!
             source-path sh05 base context)
            binding (sh06-resolution-context-plan-binding!
                     source-path context)
            actual (get-in artifact
                           [:gravity-resolution-boundary
                            :authenticated-resolution-request])
            root (sh06-resolution-context-root context)]
        (when-not (= expected actual)
          (sh06-resolution-c5-fail!
           source-path :artifact-request-context-mismatch {}))
        {:status :passed
         :context-root root
         :resolution-plan-authority-id
         (get-in context [:resolution-plan-authority :authority-id])
         :external-binding-authority-id
         (get-in context [:external-binding-authority :authority-id])
         :plan-binding binding})
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
      (catch Throwable error
        {:status :failed
         :source-path source-path
         :contained-host-error (.getName (class error))
         :contained-diagnostic
         (when (instance? clojure.lang.ExceptionInfo error)
           (:id (ex-data error)))}))))

(defn- sh06-resolution-artifact-verification*
  ([artifact construction?]
   (sh06-resolution-artifact-verification* artifact construction? ::legacy))
  ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (let [legacy? (= ::legacy context)
         aggregate-validation
         (sh06-resolution-carrier-validation
          artifact sh06-resolution-artifact-bounds)
         carrier-ok? (= :passed (:status aggregate-validation))
         boundary (when (and carrier-ok? (map? artifact))
                    (:gravity-resolution-boundary artifact))
         shape-checks
         {:bounded-artifact-carrier? carrier-ok?
          :top-level-artifact-map? (and carrier-ok? (map? artifact))
          :exact-artifact-shape?
          (and carrier-ok? (map? artifact)
               (= sh06-resolution-artifact-keys (set (keys artifact))))
          :resolution-boundary-map? (map? boundary)
          :exact-boundary-shape?
          (and (map? boundary)
               (= sh06-resolution-boundary-keys (set (keys boundary))))}
         shape-failed
         (->> shape-checks
              (keep (fn [[key passed?]] (when-not passed? key)))
              vec)
         component-validations
         (when (empty? shape-failed)
           (sh06-resolution-component-validations artifact))
         components-ok?
         (and component-validations
              (every? #(= :passed (:status %))
                      (vals component-validations)))
         preflight-checks
         (assoc shape-checks
                :bounded-semantic-components? (boolean components-ok?))
         preflight-failed
         (->> preflight-checks
              (keep (fn [[key passed?]] (when-not passed? key)))
              vec)
         preflight-observations
         (when (and construction?
                    (seq preflight-failed)
                    (= :maximum-carrier-nodes
                       (:reason aggregate-validation)))
           (merge
            {:normal-aggregate-bounds sh06-resolution-artifact-bounds
             :diagnostic-measurement-bounds
             sh06-resolution-diagnostic-measurement-bounds}
            (sh06-resolution-candidate-measurements artifact)))]
     (if (empty? preflight-failed)
       (if legacy?
         (sh06-resolution-artifact-verification-bounded*
          artifact construction?)
         (sh06-resolution-artifact-verification-bounded*
          artifact construction? context))
       {:artifact :gravity/sh06-resolution-artifact-verification
        :status :failed
        :checks preflight-checks
        :failed-checks preflight-failed
        :source-path "<sh06-resolution-artifact>"
        :carrier-validation aggregate-validation
        :preflight-observations preflight-observations
        :component-validations component-validations
        :gravity-verification nil
        :upstream-verification nil})))))

(defn sh06-resolution-contained-verification-failure
  [error]
  {:artifact :gravity/sh06-resolution-artifact-verification
   :status :failed
   :checks {:contained-host-resource-failure? false}
   :failed-checks [:contained-host-resource-failure?]
   :source-path "<sh06-resolution-artifact>"
   :contained-host-error (.getName (class error))
   :gravity-verification nil
   :upstream-verification nil})

(defn- sh06-resolution-artifact-verification-contained
  ([artifact construction?]
   (sh06-resolution-artifact-verification-contained
    artifact construction? ::legacy))
  ([artifact construction? context]
  (try
    (if (= ::legacy context)
      (sh06-resolution-artifact-verification* artifact construction?)
      (sh06-resolution-artifact-verification* artifact construction? context))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch StackOverflowError error
      (sh06-resolution-contained-verification-failure error))
    (catch OutOfMemoryError error
      (sh06-resolution-contained-verification-failure error))
    (catch AssertionError error
      (sh06-resolution-contained-verification-failure error))
    (catch LinkageError error
      (sh06-resolution-contained-verification-failure error))
    (catch Exception error
      (sh06-resolution-contained-verification-failure error)))))

(defn sh06-resolution-artifact-verification
  ([artifact]
   (let [source-path (or (get-in artifact [:provenance :source-path]) "<sh06-verification>")
         plan-binding (sh06-resolution-current-binding! source-path)]
    (binding [*sh06-resolution-operation-binding* plan-binding]
     (let [report
   (sh07-proof-transaction-report
    :sh06 :final
    {:verifier-root
     (var-get #'sh06-resolution-artifact-verification-contained)
     :report-schema-version 1
     :check-catalog-domain :gravity/sh06-final-verification-checks
     :source-content-hash (:source-content-hash plan-binding)
     :plan-semantic-hash (:plan-semantic-hash plan-binding)
     :functions-semantic-hash
     (:functions-semantic-hash plan-binding)}
    artifact
    #(sh06-resolution-artifact-verification-contained artifact false))]
      (sh06-resolution-binding-current! source-path plan-binding)
      report))))
  ([artifact context]
   (let [validation (sh06-resolution-context-validation artifact context)]
     (if-not (= :passed (:status validation))
       (assoc (sh06-resolution-contained-verification-failure
               (ex-info "SH-06 context validation failed"
                        {:id "C5-UNRESOLVED"}))
              :context-validation validation)
       (sh07-proof-transaction-report
        :sh06 :final
        {:verifier-root
         (var-get #'sh06-resolution-artifact-verification-contained)
         :report-schema-version 1
         :check-catalog-domain :gravity/sh06-final-verification-checks
         :source-content-hash sh06-resolution-expected-source-content-hash
         :plan-semantic-hash sh06-resolution-expected-plan-semantic-hash
         :functions-semantic-hash
         sh06-resolution-expected-functions-semantic-hash
         :sh06-context-root (:context-root validation)
         :resolution-plan-authority-id
         (:resolution-plan-authority-id validation)
         :external-binding-authority-id
         (:external-binding-authority-id validation)}
        artifact
        #(sh06-resolution-artifact-verification-contained
          artifact false context))))))

(defn- sh06-resolution-capability-based-proof-for-construction
  ([artifact]
   (sh06-resolution-capability-based-proof-for-construction
    artifact ::legacy))
  ([artifact context]
  (let [report
        (if (= ::legacy context)
          (sh06-resolution-artifact-verification-contained artifact true)
          (sh06-resolution-artifact-verification-contained
           artifact true context))]
    (cond->
     (assoc (:checks report)
            :artifact :gravity/sh06-resolution-capability-proof
            :status (if (= :passed (:status report)) :complete :failed)
            :failed-checks (:failed-checks report))
      (and (= :failed (:status report))
           (:preflight-observations report))
      (assoc :preflight-observations
             (:preflight-observations report))
      (and (= :failed (:status report))
           (:carrier-validation report))
      (assoc :carrier-validation
             (:carrier-validation report))))))

(defn sh06-resolution-capability-based-proof
  [artifact]
  (let [report (sh06-resolution-artifact-verification artifact)]
    (assoc (:checks report)
           :artifact :gravity/sh06-resolution-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report))))
