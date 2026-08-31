

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

(defn- sh06-resolution-source-artifact-candidate
  "Construct the authentic internal aggregate before its mandatory proof gate.

  This private value is available to bounded diagnostics and tests only.  It is
  not an accepted artifact and must never cross the public SH-06 boundary
  without sh06-resolution-capability-based-proof-for-construction."
  [source-path source-text]
  (with-sh06-resolution-transport-bounds
   (let [sh05-artifact (sh05-macro-source-artifact source-path source-text)
        upstream-report (sh05-macro-artifact-verification sh05-artifact)
        _ (when-not (= :passed (:status upstream-report))
            (sh06-resolution-boundary-fail!
             "C5-UNRESOLVED" source-path
             :fresh-authenticated-sh05-input upstream-report {}))
        request (sh06-resolution-request source-path sh05-artifact)
        binding (sh06-resolution-current-binding! source-path)
        run (sh06-resolution-run-request! source-path binding request)
        analysis (:resolved-analysis run)
        projections (sh06-resolution-product-projections analysis)
        plan-binding (dissoc binding :plan :source-text)
        boundary-base
        {:slice :SH-06
         :owner :gravity-source
         :adapter-contract sh06-resolution-adapter-contract
         :plan-binding plan-binding
         :authenticated-resolution-request request
         :raw-template-result (:raw-template-result run)
         :raw-analysis (:raw-analysis run)
         :resolved-analysis analysis
         :digest-requests (:digest-requests run)
         :resolved-digests (:resolved-digests run)
         :template-verification (:template-verification run)
         :resolved-verification (:resolved-verification run)
         :target-source-reread? false
         :clojure-adapter-residual? true
         :self-hosted? false}
        artifact-base
        (merge
         {:kind :gravity/sh06-resolution-artifact
          :status :accepted
          :slice :SH-06
          :task "SH-06"
          :document-set ["L3" "C5"]
          :governing-document c5-resolution-governing-document
          :artifact-id nil
          :sh05-macro-artifact sh05-artifact
          :gravity-resolution-boundary boundary-base
          :provenance
          {:source-path source-path
           :source-revision-id (get-in request
                                       [:module :source-revision-id])}
          :pass
          {:name :sh06-gravity-name-resolution
           :input :authenticated-sh05-expanded-syntax
           :output :authenticated-sh06-namespace-analysis
           :preserves [:source-spans :syntax-ids :macro-lineage
                       :profile :target :effects :capabilities]
           :rejects c5-resolution-diagnostic-ids}
          :execution-boundary
          {:resolution-authority :gravity
           :gravity-module 'gravity.resolution
           :plan-runner :clojure-stage0
           :digest-resolver :clojure-stage0
           :envelope-binder :clojure-stage0
           :compatibility-adapter :clojure-stage0
           :component-transport-bounds sh06-resolution-transport-bounds
           :aggregate-artifact-bounds sh06-resolution-artifact-bounds
           :self-hosted? false}
          :capability-based-proof nil
          :diagnostics []}
         projections)
        artifact-id (sh06-resolution-artifact-id artifact-base)
        artifact-with-id (assoc artifact-base :artifact-id artifact-id)
        summary (sh06-resolution-envelope-summary artifact-with-id)
        descriptor
        (sh06-resolution-sh02-descriptor source-path binding summary)
        envelope
        (p15-s23-stage2-sh02-descriptor-envelope
         sh06-resolution-envelope-stage
         sh06-resolution-sealed-artifact-kind descriptor source-path)
        _ (p15-s23-stage2-sh02-descriptor-envelope-verify!
           envelope sh06-resolution-envelope-stage
           sh06-resolution-sealed-artifact-kind descriptor source-path)
        artifact-with-envelope
        (-> artifact-with-id
            (assoc-in [:gravity-resolution-boundary
                       :authenticated-envelope-descriptor] descriptor)
            (assoc-in [:gravity-resolution-boundary
                       :authenticated-envelope] envelope))]
    artifact-with-envelope)))