

(defn- sh06-resolution-artifact-verification*
  [artifact construction?]
  (with-sh06-resolution-transport-bounds
   (let [aggregate-validation
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
       (sh06-resolution-artifact-verification-bounded* artifact construction?)
       {:artifact :gravity/sh06-resolution-artifact-verification
        :status :failed
        :checks preflight-checks
        :failed-checks preflight-failed
        :source-path "<sh06-resolution-artifact>"
        :carrier-validation aggregate-validation
        :preflight-observations preflight-observations
        :component-validations component-validations
        :gravity-verification nil
        :upstream-verification nil}))))

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
  [artifact construction?]
  (try
    (sh06-resolution-artifact-verification* artifact construction?)
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
      (sh06-resolution-contained-verification-failure error))))

(defn sh06-resolution-artifact-verification
  [artifact]
  (sh07-proof-transaction-report
   :sh06 :final
   {:verifier-root
    (var-get #'sh06-resolution-artifact-verification-contained)
    :report-schema-version 1
    :check-catalog-domain :gravity/sh06-final-verification-checks
    :source-content-hash sh06-resolution-expected-source-content-hash
    :plan-semantic-hash sh06-resolution-expected-plan-semantic-hash
    :functions-semantic-hash
    sh06-resolution-expected-functions-semantic-hash}
   artifact
   #(sh06-resolution-artifact-verification-contained artifact false)))

(defn- sh06-resolution-capability-based-proof-for-construction
  [artifact]
  (let [report
        (sh06-resolution-artifact-verification-contained artifact true)]
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
             (:carrier-validation report)))))

(defn sh06-resolution-capability-based-proof
  [artifact]
  (let [report (sh06-resolution-artifact-verification artifact)]
    (assoc (:checks report)
           :artifact :gravity/sh06-resolution-capability-proof
           :status (if (= :passed (:status report)) :complete :failed)
           :failed-checks (:failed-checks report))))

(defn- sh06-authenticated-sh05-parent!
  "Validate the complete SH-05 owner artifact before SH-06 consumes it.

  An SH-05 id by itself is not a sufficient parent edge.  This gate replays the
  owner verifier, checks the owner id and accepted/proof statuses, verifies the
  nested C3 owner, and joins source/path provenance across both artifacts.
  "
  [source-path sh05-artifact]
  (let [boundary (:gravity-macro-boundary sh05-artifact)
        c3-artifact (:authenticated-sh04-artifact boundary)
        c2-artifact (:c2-reader-artifact c3-artifact)
        source-unit (:source-unit-record c2-artifact)
        verification
        (try
          (sh05-macro-artifact-verification sh05-artifact)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Exception _ {:status :failed}))
        owner-id
        (try
          (sh05-macro-artifact-id sh05-artifact)
          (catch Exception _ nil))
        c3-id
        (try
          (c3-artifact-id c3-artifact)
          (catch Exception _ nil))
        c3-verified?
        (try
          (c3-syntax-validate! source-path c3-artifact)
          true
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Exception _ false))
        passed?
        (and (map? sh05-artifact)
             (= :gravity/sh05-macro-expansion-artifact
                (:kind sh05-artifact))
             (= :accepted (:status sh05-artifact))
             (= :passed (:status verification))
             (= :complete
                (get-in sh05-artifact [:capability-based-proof :status]))
             (= (:artifact-id sh05-artifact) owner-id)
             (map? boundary)
             (= :SH-05 (:slice boundary))
             (= :gravity-source (:owner boundary))
             (= :accepted
                (get-in boundary [:authenticated-envelope :status]))
             (= source-path (get-in sh05-artifact [:provenance :source-path]))
             (= source-path (get-in boundary
                                    [:authenticated-envelope-descriptor
                                     :actual-path-provenance :source-path]))
             (map? c3-artifact)
             (= :gravity/stage0-c3-syntax-object-artifact (:kind c3-artifact))
             (= (:artifact-id c3-artifact) c3-id)
             c3-verified?
             (= :accepted (get-in c3-artifact
                                  [:gravity-syntax-boundary
                                   :authenticated-envelope :status]))
             (= source-path (get-in c3-artifact
                                    [:c2-reader-artifact :source-unit-record
                                     :path]))
             (= source-path (get-in c3-artifact
                                    [:gravity-syntax-boundary
                                     :authenticated-envelope-descriptor
                                     :actual-path-provenance :source-path]))
             (= source-path (:path source-unit))
             (= (:source-id source-unit)
                (get-in sh05-artifact [:provenance :source-id]))
             (= (:artifact-id c3-artifact)
                (get-in sh05-artifact [:sh04-syntax-artifact :artifact-id])))]
    (when-not passed?
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :authenticated-sh05-macro-parent
       {:source-path source-path
        :observed-source-path (get-in sh05-artifact [:provenance :source-path])
        :artifact-kind (:kind sh05-artifact)
        :artifact-status (:status sh05-artifact)
        :artifact-id (:artifact-id sh05-artifact)
        :recomputed-artifact-id owner-id
        :verification-status (:status verification)
        :c3-artifact-id (:artifact-id c3-artifact)
        :recomputed-c3-artifact-id c3-id
        :c3-verified? c3-verified?}
       {:required [:complete-sh05-artifact-identity
                   :passed-sh05-verifier
                   :complete-sh05-capability-proof
                   :authenticated-c3-parent
                   :source-path-provenance]}))
    sh05-artifact))
