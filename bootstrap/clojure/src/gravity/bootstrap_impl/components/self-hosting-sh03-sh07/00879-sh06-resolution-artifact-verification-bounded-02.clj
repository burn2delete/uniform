
(defn- sh06-resolution-artifact-verification-bounded*
  ([artifact construction?]
   (sh06-resolution-artifact-verification-bounded*
    artifact construction? ::legacy))
  ([artifact construction? context]
  (with-sh06-resolution-transport-bounds
   (let [legacy? (= ::legacy context)
        source-path (or (get-in artifact [:provenance :source-path])
                        "<sh06-resolution-artifact>")
        boundary (:gravity-resolution-boundary artifact)
        analysis (:resolved-analysis boundary)
        sh05-artifact (:sh05-macro-artifact artifact)
        sh05-report
        (try
          (sh05-macro-artifact-verification sh05-artifact)
          (catch Throwable _ {:status :failed}))
        binding
        (try
          (if legacy?
            (sh06-resolution-current-binding! source-path)
            (sh06-resolution-context-plan-binding! source-path context))
          (catch Throwable _ nil))
        expected-plan-binding
        (when binding (dissoc binding :plan :source-text :input-key))
        replay
        (when (and binding (= :passed (:status sh05-report)))
          (try
            (let [request (if legacy?
                            (sh06-resolution-module-request source-path sh05-artifact (:module-inputs boundary))
                            (sh06-resolution-request
                             source-path sh05-artifact context))
                  run (sh06-resolution-run-request!
                       source-path binding request)]
              {:status :passed :request request :run run})
            (catch InterruptedException interrupted
              (.interrupt (Thread/currentThread))
              (throw interrupted))
            (catch Throwable error
              {:status :failed
               :contained-host-error (.getName (class error))
               :contained-diagnostic
               (when (instance? clojure.lang.ExceptionInfo error)
                 (:id (ex-data error)))})))
        expected-request (:request replay)
        expected-run (:run replay)
        gravity-report
        (or (:resolved-verification expected-run) {:status :failed})
        projections (when (map? analysis)
                      (sh06-resolution-product-projections analysis))
        identity-chunks (sh06-resolution-product-identity-chunks artifact)
        summary (sh06-resolution-envelope-summary-with-chunks
                 artifact identity-chunks)
        expected-descriptor
        (when binding
          (sh06-resolution-sh02-descriptor source-path binding summary))
        envelope-ok?
        (and expected-descriptor
             (= expected-descriptor
                (:authenticated-envelope-descriptor boundary))
             (try
               (p15-s23-stage2-sh02-descriptor-envelope-verify!
                (:authenticated-envelope boundary)
                sh06-resolution-envelope-stage
                sh06-resolution-sealed-artifact-kind
                expected-descriptor source-path)
               true
               (catch Throwable _ false)))
        base-checks
        (cond->
        {:trusted-artifact-carrier? true
         :trusted-semantic-components? true
         :exact-kind? (= :gravity/sh06-resolution-artifact (:kind artifact))
         :accepted-status? (= :accepted (:status artifact))
         :document-set-current?
         (= ["L3" "C5"] (:document-set artifact))
         :governing-document-current?
         (= c5-resolution-governing-document
            (:governing-document artifact))
         :fresh-upstream-sh05-verification?
         (= :passed (:status sh05-report))
         :plan-binding-current?
         (and expected-plan-binding
              (= expected-plan-binding (:plan-binding boundary)))
         :fresh-gravity-request-replay?
         (= :passed (:status replay))
         :authenticated-resolution-request-current?
         (and expected-request
              (= expected-request
                 (:authenticated-resolution-request boundary)))
         :raw-template-result-current?
         (and expected-run
              (= (:raw-template-result expected-run)
                 (:raw-template-result boundary)))
         :raw-analysis-current?
         (and expected-run
              (= (:raw-analysis expected-run) (:raw-analysis boundary)))
         :resolved-analysis-current?
         (and expected-run
              (= (:resolved-analysis expected-run)
                 (:resolved-analysis boundary)))
         :digest-requests-current?
         (and expected-run
              (= (:digest-requests expected-run)
                 (:digest-requests boundary)))
         :resolved-digests-current?
         (and expected-run
              (= (:resolved-digests expected-run)
                 (:resolved-digests boundary)))
         :template-verification-current?
         (and expected-run
              (= (:template-verification expected-run)
                 (:template-verification boundary)))
         :resolved-verification-current?
         (and expected-run
              (= (:resolved-verification expected-run)
                 (:resolved-verification boundary)))
         :upstream-request-binding?
         (and (= (:artifact-id sh05-artifact)
                 (get-in boundary
                         [:authenticated-resolution-request :module
                          :c4-artifact-id]))
              (= (:expanded-syntax-stream-id sh05-artifact)
                 (get-in boundary
                         [:authenticated-resolution-request
                          :macro-expansion-binding :macro-result-id])))
         :fresh-gravity-verification?
         (= :passed (:status gravity-report))
         :namespace-analysis-current?
         (= (:namespace-analysis projections) (:namespace-analysis artifact))
         :binding-table-current?
         (= (:binding-table projections) (:binding-table artifact))
         :alias-table-current?
         (= (:alias-table projections) (:alias-table artifact))
         :import-export-current?
         (= (:import-export-table projections) (:import-export-table artifact))
         :lexical-scope-current?
         (= (:lexical-scope-graph projections)
            (:lexical-scope-graph artifact))
         :dependency-graph-current?
         (= (:dependency-graph projections) (:dependency-graph artifact))
         :cross-profile-current?
         (= (:cross-profile-edge-report projections)
            (:cross-profile-edge-report artifact))
         :invalidation-current?
         (= (:incremental-invalidation-keys projections)
            (:incremental-invalidation-keys artifact))
         :resolution-table-current?
         (= (:resolution-table projections) (:resolution-table artifact))
         :artifact-identity-current?
         (= (:artifact-id artifact)
            (reader-canonical-hash
             (sh06-resolution-artifact-identity-input-with-chunks
              artifact identity-chunks)))
         :fresh-sh02-envelope-verified? envelope-ok?
         :exact-artifact-shape?
         (= sh06-resolution-artifact-keys (set (keys artifact)))
         :exact-boundary-shape?
         (= (sh06-resolution-boundary-keys-for boundary) (set (keys boundary)))
         :fixed-boundary-contract?
         (= {:slice :SH-06
             :owner :gravity-source
             :adapter-contract sh06-resolution-adapter-contract
             :target-source-reread? false
             :clojure-adapter-residual? true
             :self-hosted? false}
            (select-keys
             boundary
             [:slice :owner :adapter-contract
              :target-source-reread? :clojure-adapter-residual?
              :self-hosted?]))
         :pass-contract-current?
         (= sh06-resolution-pass-contract (:pass artifact))
         :execution-boundary-current?
         (= sh06-resolution-execution-boundary-contract
            (:execution-boundary artifact))
         :provenance-current?
         (let [source-revision-id
               (get-in boundary
                       [:authenticated-resolution-request :module
                        :source-revision-id])]
           (and (= {:source-path source-path
                    :source-revision-id source-revision-id}
                   (:provenance artifact))
                (= source-path
                   (get-in boundary
                           [:authenticated-resolution-request
                            :provenance :actual-source-path]))))
         :accepted-diagnostics-empty?
         (empty? (:diagnostics artifact))}
          (not legacy?)
          (assoc
           :resolution-plan-authority-current?
           (boolean binding)
           :external-binding-authority-current?
           (boolean (and expected-request
                         (= (:authority-id
                             (:external-binding-authority context))
                            (reader-canonical-hash
                             [:gravity/sh07-b51-c5-external-binding-authority-v1
                              (dissoc (:external-binding-authority context)
                                      :authority-id)]))))))
        base-failed
        (->> base-checks (keep (fn [[key passed?]]
                                (when-not passed? key))) vec)
        expected-embedded-proof
        (assoc base-checks
               :embedded-capability-proof-current? true
               :artifact :gravity/sh06-resolution-capability-proof
               :status (if (empty? base-failed) :complete :failed)
               :failed-checks base-failed)
        embedded-proof-current?
        (or (and construction?
                 (nil? (:capability-based-proof artifact)))
            (= expected-embedded-proof
               (:capability-based-proof artifact)))
        checks
        (assoc base-checks
               :embedded-capability-proof-current?
               embedded-proof-current?)
        failed (->> checks (keep (fn [[key passed?]]
                                   (when-not passed? key))) vec)]
    {:artifact :gravity/sh06-resolution-artifact-verification
     :status (if (empty? failed) :passed :failed)
     :checks checks
     :failed-checks failed
     :source-path source-path
     :gravity-verification gravity-report
     :upstream-verification sh05-report}))))
