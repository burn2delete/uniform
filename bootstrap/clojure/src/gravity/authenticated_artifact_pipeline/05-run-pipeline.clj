
(defn run-pipeline
  "Build the requested earliest artifact and all of its authenticated parents.

  Accepts either a request map or a source path.  The map schema is closed:
  `{:source-path string :target-stage stage :mode :fast|:fresh}` with optional
  `:reader :development-current` and its owner-validated `:module-inputs`.
  Omitted Reader selection retains the historical route. The result
  keeps both envelopes and the original bootstrap payload available:
  `:artifact` is the selected envelope, `:payload` is its existing artifact,
  and `:stage-artifacts` is the ordered stage map.
  "
  ([request]
   ;; Entries built by this invocation remain staged until the final secure
   ;; source re-snapshot and bounded result identity both succeed.  A mutation
   ;; or construction failure therefore cannot publish a misleading fast hit.
   (binding [*pending-cache* (atom {})]
     (let [{:keys [source-path target-stage mode] :as request}
           (validate-request (if (string? request)
                               {:source-path request}
                               request))
         before (source-snapshot! source-path)
         actual-path (:canonical-path before)
         reader-operation (reader-operation! actual-path request)
         request (cond-> request reader-operation (assoc :reader-operation reader-operation))
         context (project-context actual-path)
         source-text (strict-source-text! actual-path (:bytes before))
         source-id (source-identity before context)
         provenance {:actual-path actual-path
                     :project-root-path (:project-root-path context)
                     :project-root-id (:project-root-id context)
                     :project-relative-path (:project-relative-path context)}
         target-index (get stage-index target-stage)
         artifacts
         (loop [index 0 previous []]
           (if (> index target-index)
             previous
             (let [stage (nth stages index)
                   built
                   (if (= :source stage)
                     (let [preimage (cache-key-preimage stage mode source-id [] request)
                           key-id (canonical-id! stage preimage)
                           key [key-id actual-path]
                           cached (when (and (= mode :fast)
                                             (contains? @stage-cache key))
                                    (get @stage-cache key))]
                       (if (and cached
                           (envelope-valid? stage mode key-id preimage cached
                                            source-id provenance [])
                           (revalidate-payload? stage actual-path
                                                 (:payload cached)))
                         (-> cached
                             (assoc :cache-hit? true
                                    :cache-policy
                                    (assoc (:cache-policy cached)
                                           :observed-hit? true))
                             (assoc :receipt
                                    (receipt stage mode (:artifact-id cached) []
                                             true key-id source-id provenance)))
                         (let [payload (source-payload actual-path before context)
                               built (envelope stage mode source-id provenance []
                                                payload false key-id)]
                           (when (= mode :fast)
                             (cache-put! key built))
                           built)))
                     (build-stage! stage mode source-id provenance actual-path
                                   source-text context previous request))]
               (recur (inc index) (conj previous built)))))
         _ (stable-source-after! actual-path before)
         _ (stable-reader-after! actual-path request)
         selected (last artifacts)
         stage-map (into (array-map) (map (juxt :stage identity) artifacts))
         result-base {:kind :gravity/authenticated-artifact-pipeline-result
                      :schema-version pipeline-schema-version
                      :pipeline pipeline-id
                      :requested-stage target-stage
                      :mode mode
                      :actual-path actual-path
                      :source-content-hash (:bytes-hash source-id)
                      :stage-artifacts stage-map
                      :artifact selected
                      :payload (:payload selected)
                      :authority {:authoritative? false
                                  :ceiling :development-only
                                  :nonclaims [:integration :proof :release
                                              :self-hosting :seed-retirement
                                              :safety :performance]}}
         ;; A result is an operational envelope over potentially very large
         ;; compiler payloads.  Bind it to the ordered authenticated stage
         ;; identities instead of recursively re-encoding those payloads;
         ;; the latter exceeds C16's closed node bound for real checked core.
         result-id
         (canonical-id!
          target-stage
          {:kind (:kind result-base)
           :schema-version pipeline-schema-version
           :pipeline pipeline-id
           :requested-stage target-stage
           ;; Semantic result identity is path/lane neutral.  Absolute path,
           ;; cache hit, and fresh/fast policy remain operational fields on the
           ;; result but do not create distinct compiler artifacts.
           :source-identity source-id
           :stage-artifact-ids
           (mapv (fn [stage]
                   [stage (get-in stage-map [stage :artifact-id])])
                 (keys stage-map))
           :selected-artifact-id (:artifact-id selected)
           :authority (:authority result-base)})
         result (assoc result-base
                       :result-id result-id
                       :execution-boundary
                       {:fresh-cache-bypassed? (= mode :fresh)
                        :isolation :same-jvm
                        :isolated? false
                        :acceptance-authority? false})
         ;; Commit only after the post-execution source snapshot and bounded
         ;; semantic result id have succeeded.  Fresh requests never stage or
         ;; commit entries.
         _ (when (= mode :fast)
             (commit-pending-cache! @*pending-cache*))]
       result))))

(defn artifact
  "Return the existing bootstrap payload selected by `run-pipeline`."
  [result]
  (or (:payload result)
      (fail! "ARTIFACT-RESULT" "pipeline result has no selected payload" {})))

(defn artifact-envelope
  "Return the authenticated envelope selected by `run-pipeline`."
  [result]
  (or (:artifact result)
      (fail! "ARTIFACT-RESULT" "pipeline result has no selected envelope" {})))

(defn stage-artifact
  "Return one stage envelope from a pipeline result, if it was requested."
  [result stage]
  (require-stage! stage)
  (or (get-in result [:stage-artifacts stage])
      (fail! "ARTIFACT-STAGE"
             "requested stage was not built by this pipeline result"
             {:stage stage
              :requested-stage (:requested-stage result)})))

(defn fresh-checked-core-artifact
  "Build and validate a fresh checked-core projection for SH-07 handoff."
  [source-path]
  (let [result (run-pipeline {:source-path source-path
                              :target-stage :checked-core :mode :fresh})
        caller (source-snapshot! source-path)
        selected (stage-artifact result :checked-core)
        stage-map (:stage-artifacts result)
        fresh? #(and (= :fresh (:mode %))
                     (every? false?
                             [(get-in % [:cache-policy :eligible?])
                              (get-in % [:cache-policy :observed-hit?])
                              (get-in % [:cache-policy :authoritative?])
                              (get-in % [:receipt :cache :eligible?])
                              (get-in % [:receipt :cache :hit?])
                              (get-in % [:receipt :cache :authoritative?])
                              (get-in % [:authority :authoritative?])]))]
    (when-not (and (= :gravity/authenticated-artifact-pipeline-result
                     (:kind result))
                   (= pipeline-schema-version (:schema-version result))
                   (= pipeline-id (:pipeline result))
                   (= :checked-core (:requested-stage result))
                   (= :fresh (:mode result))
                   (= stages (vec (keys stage-map)))
                   (= selected (:artifact result))
                   (and (some? (:payload-artifact-id selected)) (= [(:payload selected) (:payload-artifact-id selected)] [(:payload result) (get-in selected [:payload :artifact-id])]))
                   (= [(:canonical-path caller) (:bytes-hash caller)]
                      [(:actual-path result) (:source-content-hash result)]
                      [(get-in selected [:provenance :actual-path]) (get-in selected [:source-identity :bytes-hash])])
                   (= {:fresh-cache-bypassed? true :isolation :same-jvm
                       :isolated? false :acceptance-authority? false}
                      (:execution-boundary result))
                   (false? (get-in result [:authority :authoritative?]))
                   (= :development-only (get-in result [:authority :ceiling]))
                   (= [:integration :proof :release :self-hosting :seed-retirement :safety :performance] (get-in result [:authority :nonclaims]))
                   (every? fresh? (vals stage-map)))
      (fail! "ARTIFACT-RESULT"
             "fresh checked-core pipeline result failed validation"
             {:source-path source-path
              :requested-stage (:requested-stage result)
              :mode (:mode result)}))
    (:payload selected)))
