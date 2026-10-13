
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

(defn- sh06-authenticated-source-snapshot!
  ([source-path c2-artifact phase legacy?]
   (if legacy?
     {:canonical-path source-path :byte-count nil :bytes-hash
      (get-in c2-artifact [:source-unit-record :bytes-hash])}
     (let [source-unit (:source-unit-record c2-artifact)
        expected-path (:path source-unit)
        expected-hash (:bytes-hash source-unit)
        expected-count (or (:byte-count source-unit)
                           (get-in c2-artifact
                                   [:gravity-reader-boundary
                                    :authenticated-envelope-descriptor
                                    :source-revision :source-byte-count]))
        snapshot (try
                   (c2-pass-cache/bounded-source-snapshot! source-path)
                   (catch Exception error
                     (sh06-resolution-boundary-fail!
                      "C5-UNRESOLVED" source-path phase
                      {:source-path source-path
                       :error-class (some-> error class .getName)
                       :error-data (ex-data error)}
                      {:required [:bounded-no-follow-source-snapshot]})))]
    (when-not (and (= source-path expected-path)
                   (= (:canonical-path snapshot)
                      (.getCanonicalPath (java.io.File. expected-path)))
                   (= expected-hash (:bytes-hash snapshot))
                   (or (nil? expected-count)
                       (= (long expected-count) (long (:byte-count snapshot)))))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path phase
       {:source-path source-path :expected-path expected-path
        :observed-canonical-path (:canonical-path snapshot)
        :expected-bytes-hash expected-hash
        :observed-bytes-hash (:bytes-hash snapshot)
        :expected-byte-count expected-count
        :observed-byte-count (:byte-count snapshot)}
       {:required [:source-path-provenance :source-bytes-hash
                   :source-byte-count]}))
       snapshot)))
  ([source-path c2-artifact phase legacy? context]
   (let [source-unit (:source-unit-record c2-artifact)
         authority (:external-binding-authority context)
         preimage (:preimage authority)
         content (:content authority)
         descriptors (:source-descriptors preimage)
         descriptor (first (filter #(= source-path (:path %)) descriptors))
         ordinal (:module-ordinal authority)
         input (when (and (integer? ordinal) (<= 0 ordinal 6))
                 (nth (:module-source-inputs preimage) ordinal nil))
         expected-count
         (or (:byte-count source-unit)
             (get-in c2-artifact
                     [:gravity-reader-boundary
                      :authenticated-envelope-descriptor
                      :source-revision :source-byte-count]))]
     (when (nil? context)
       (sh06-resolution-c5-fail! source-path :missing-context {:phase phase}))
     (sh06-resolution-content-proof-tree!
      source-path content (:content-object-proof preimage))
     (when-not
      (and (not legacy?) descriptor input
           (= descriptor (:descriptor input))
           (= source-path (:path source-unit) (:path descriptor))
           (= (:raw-sha256 descriptor) (:bytes-hash source-unit))
           (= (:byte-count descriptor) expected-count))
       (sh06-resolution-c5-fail!
        source-path :contextual-source-snapshot
        {:phase phase :module-ordinal ordinal}))
     (sh06-resolution-source-proof-bytes!
      source-path content descriptor (:source-object-proof input))
     {:canonical-path source-path
      :byte-count (:byte-count descriptor)
      :bytes-hash (:raw-sha256 descriptor)
      :immutable-git-object? true})))

(defn- sh06-source-snapshot-stable!
  ([source-path before c2-artifact legacy?]
   (let [after (sh06-authenticated-source-snapshot!
                source-path c2-artifact :source-stability-after-c5 legacy?)]
    (when (and (not legacy?)
               (not= (select-keys before [:canonical-path :byte-count :bytes-hash])
                     (select-keys after [:canonical-path :byte-count :bytes-hash])))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED" source-path :source-mutated-during-c5
       {:before (select-keys before [:canonical-path :byte-count :bytes-hash])
        :after (select-keys after [:canonical-path :byte-count :bytes-hash])}
       {:required [:stable-source-before-and-after-construction]}))
     after))
  ([source-path before c2-artifact legacy? context]
   (let [after (sh06-authenticated-source-snapshot!
                source-path c2-artifact :source-stability-after-c5
                legacy? context)]
     (when-not (= before after)
       (sh06-resolution-c5-fail!
        source-path :contextual-source-mutated
        {:before before :after after}))
     after)))

(defn- sh06-resolution-source-artifact-candidate
  "Construct the authentic internal aggregate before its mandatory proof gate.

  This private value is available to bounded diagnostics and tests only.  It is
  not an accepted artifact and must never cross the public SH-06 boundary
  without sh06-resolution-capability-based-proof-for-construction."
  ([source-path sh05-artifact legacy?]
   (sh06-resolution-source-artifact-candidate
    source-path sh05-artifact legacy? ::legacy))
  ([source-path sh05-artifact legacy? context]
  (with-sh06-resolution-transport-bounds
   (let [contextual? (not= ::legacy context)
        _ (when (and contextual? (nil? context))
            (sh06-resolution-c5-fail! source-path :missing-context {}))
        _ (sh06-authenticated-sh05-parent!
            source-path sh05-artifact)
        c2-artifact (get-in sh05-artifact
                            [:gravity-macro-boundary
                             :authenticated-sh04-artifact
                             :c2-reader-artifact])
        source-before
        (if contextual?
          (sh06-authenticated-source-snapshot!
           source-path c2-artifact :source-authentication-before-c5
           legacy? context)
          (sh06-authenticated-source-snapshot!
           source-path c2-artifact :source-authentication-before-c5 legacy?))
        upstream-report (sh05-macro-artifact-verification sh05-artifact)
        request (if contextual?
                  (sh06-resolution-request source-path sh05-artifact context)
                  (sh06-resolution-request source-path sh05-artifact))
        binding (if contextual?
                  (sh06-resolution-context-plan-binding! source-path context)
                  (sh06-resolution-current-binding! source-path))
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
    (if contextual?
      (sh06-source-snapshot-stable!
       source-path source-before c2-artifact legacy? context)
      (sh06-source-snapshot-stable!
       source-path source-before c2-artifact legacy?))
    artifact-with-envelope))))
