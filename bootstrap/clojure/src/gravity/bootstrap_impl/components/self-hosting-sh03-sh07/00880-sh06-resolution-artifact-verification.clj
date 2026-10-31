(declare sh06-resolution-artifact-verification)

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
            base (sh06-resolution-base-request source-path sh05)
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

(defn-
 sh06-resolution-artifact-verification*
 ([artifact construction?]
  (sh06-resolution-artifact-verification*
   artifact
   construction?
   :gravity.bootstrap/legacy))
 ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (#'gravity.resolution/verify*
    artifact
    construction?
    context
    (resolution-stage-operations)))))

(defn
 sh06-resolution-contained-verification-failure
 [error]
 (gravity.resolution/verification-failure error))

(defn-
 sh06-resolution-artifact-verification-contained
 ([artifact construction?]
  (sh06-resolution-artifact-verification-contained
   artifact
   construction?
   :gravity.bootstrap/legacy))
 ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (#'gravity.resolution/verify-contained*
    artifact
    construction?
    context
    (resolution-stage-operations)))))

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
