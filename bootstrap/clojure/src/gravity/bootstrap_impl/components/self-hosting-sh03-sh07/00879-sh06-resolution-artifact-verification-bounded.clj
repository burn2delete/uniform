
(defn- sh06-resolution-plan-authority-binding!
  [source-path context]
  (when-not
   (and (some? context)
        (sh06-resolution-exact-keys?
         context #{:external-binding-authority :resolution-plan-authority}))
    (sh06-resolution-c5-fail! source-path :missing-or-malformed-context {}))
  (let [authority (:resolution-plan-authority context)
        content (:content authority)
        descriptor (:source-descriptor authority)
        binding (:plan-binding authority)]
    (when-not
     (and
      (sh06-resolution-exact-keys?
       authority #{:schema :content :content-object-proof :source-descriptor
                   :source-object-proof :plan-binding :self-id-omits
                   :authority-id})
      (= :gravity/sh07-b51-sh06-resolution-plan-authority-v1
         (:schema authority))
      (= [[:authority-id]] (:self-id-omits authority))
      (sh06-resolution-sha256-id? (:authority-id authority))
      (= (:authority-id authority)
         (reader-canonical-hash
          [:gravity/sh07-b51-sh06-resolution-plan-authority-v1
           (dissoc authority :authority-id)]))
      (sh06-resolution-exact-keys?
       descriptor #{:ordinal :kind :module-ordinal :path :mode :blob-oid
                    :byte-count :raw-sha256})
      (= 12 (:ordinal descriptor))
      (= :sh06-resolution (:kind descriptor))
      (nil? (:module-ordinal descriptor))
      (= sh06-resolution-source-relative-path (:path descriptor))
      (= sh06-resolution-expected-source-byte-count (:byte-count descriptor))
      (= sh06-resolution-expected-source-content-hash
         (:raw-sha256 descriptor))
      (sh06-resolution-exact-keys?
       binding #{:source-path :source-byte-count :source-content-hash
                 :plan-semantic-hash :functions-semantic-hash :function-count
                 :function-names-hash :function-shapes-hash
                 :public-function-hashes :public-function-shapes :artifact
                 :status :semantic-authority :compiled-by :executed-by
                 :generic-bridge-residual? :self-hosted? :plan})
      (= sh06-resolution-source-relative-path (:source-path binding))
      (= sh06-resolution-expected-source-byte-count
         (:source-byte-count binding))
      (= sh06-resolution-expected-source-content-hash
         (:source-content-hash binding))
      (= :gravity/sh06-pinned-resolution-plan-binding (:artifact binding))
      (= :complete (:status binding))
      (= :gravity-source (:semantic-authority binding))
      (= :clojure-stage0-seed (:compiled-by binding))
      (= :clojure-stage2-generic-rule-runner (:executed-by binding))
      (true? (:generic-bridge-residual? binding))
      (false? (:self-hosted? binding)))
     (sh06-resolution-c5-fail! source-path :resolution-plan-authority-shape {}))
    (sh06-resolution-content-proof-tree!
     source-path content (:content-object-proof authority))
    (let [bytes (sh06-resolution-source-proof-bytes!
                 sh06-resolution-source-relative-path content descriptor
                 (:source-object-proof authority))
          plan (:plan binding)
          plan-base (dissoc plan :plan-id)
          expected-plan-id
          (c4-artifact-id (c-backend-canonical-value plan-base))
          identities (sh06-resolution-plan-identities plan)]
      (when-not
       (and (= sh06-resolution-expected-source-byte-count (count bytes))
            (= sh06-resolution-expected-source-content-hash
               (str "sha256:"
                    (sha256-bytes-hex
                     (sh06-resolution-byte-array bytes))))
            (= :gravity/stage2-compiler-artifact-plan (:kind plan))
            (true? (:compiler-artifact-plan? plan))
            (= 'gravity.resolution (get-in plan [:module :module]))
            (= :meta (get-in plan [:module :profile]))
            (= :jvm (get-in plan [:module :target]))
            (= #{} (get-in plan [:module :effects]))
            (= #{} (get-in plan [:module :capabilities]))
            (= :safe (get-in plan [:module :safety]))
            (= (:byte-count descriptor) (get-in plan [:source :byte-count]))
            (= sh06-resolution-expected-plan-id
               expected-plan-id (:plan-id plan))
            (= sh06-resolution-expected-plan-semantic-hash
               (:plan-semantic-hash identities)
               (:plan-semantic-hash binding))
            (= sh06-resolution-expected-functions-semantic-hash
               (:functions-semantic-hash identities)
               (:functions-semantic-hash binding))
            (= sh06-resolution-expected-function-count
               (:function-count identities) (:function-count binding))
            (= sh06-resolution-expected-function-names-hash
               (:function-names-hash identities)
               (:function-names-hash binding))
            (= sh06-resolution-expected-function-shapes-hash
               (:function-shapes-hash identities)
               (:function-shapes-hash binding))
            (= sh06-resolution-public-function-hashes
               (:public-function-hashes identities)
               (:public-function-hashes binding))
            (= sh06-resolution-public-function-shapes
               (:public-function-shapes identities)
               (:public-function-shapes binding)))
        (sh06-resolution-c5-fail!
         source-path :resolution-plan-authority-current {})))
    binding))

(defn sh06-resolution-context-plan-binding!
  [source-path context]
  (sh06-resolution-plan-authority-binding! source-path context))

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
        (when binding (dissoc binding :plan :source-text))
        replay
        (when (and binding (= :passed (:status sh05-report)))
          (try
            (let [request (if legacy?
                            (sh06-resolution-request source-path sh05-artifact)
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
        summary (sh06-resolution-envelope-summary artifact)
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
        {:bounded-artifact-carrier? true
         :bounded-semantic-components? true
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
         (= (:artifact-id artifact) (sh06-resolution-artifact-id artifact))
         :fresh-sh02-envelope-verified? envelope-ok?
         :exact-artifact-shape?
         (= sh06-resolution-artifact-keys (set (keys artifact)))
         :exact-boundary-shape?
         (= sh06-resolution-boundary-keys (set (keys boundary)))
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
         :component-transport-bounds-current?
         (= sh06-resolution-transport-bounds
            (get-in artifact
                    [:execution-boundary :component-transport-bounds]))
         :aggregate-artifact-bounds-current?
         (= sh06-resolution-artifact-bounds
            (get-in artifact
                    [:execution-boundary :aggregate-artifact-bounds]))
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
