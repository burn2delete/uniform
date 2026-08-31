(ns gravity.c16-incremental.validation
  (:require [clojure.set :as set]))

(defn fail!
  [{:keys [diagnostic-messages fail source-span]} id source-path subject extra]
  (fail id
        (get diagnostic-messages id "incremental compiler validation failed")
        (merge {:source-span (or (:source-span subject)
                                 (source-span source-path 0))
                :diagnostic-family :compiler-incremental
                :stage (or (:stage subject) :c16-incremental-compilation)
                :cache-key (:cache-key subject)
                :artifact-id (:artifact-id subject)
                :invalidating-input (:invalidating-input subject)
                :profile (:profile subject)
                :target (:target subject)
                :remediation "Regenerate incremental graph, cache keys, cache entries, invalidation traces, replay records, and revalidation reports before reuse."}
               extra)))

(defn validate-source-overrides!
  [{:keys [override-diagnostics diagnostic-ids incremental-fail!]}
   source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (let [[id subject-kind] (get override-diagnostics fail-kind)]
      (when (contains? (set diagnostic-ids) id)
        (incremental-fail! id source-path
                           {:stage subject-kind
                            :cache-key (str "c16-invalid-cache-" (name fail-kind))
                            :artifact-id (str "c16-cache-artifact-" (name fail-kind))
                            :invalidating-input fail-kind
                            :profile :hosted
                            :target :jvm}
                           {:missing-fields [fail-kind]})))))

(defn validate!
  [{:keys [cache-key-required-fields invalidation-causes diagnostic-ids
           perf-present? incremental-fail!]}
   source-path artifact]
  (let [required (set cache-key-required-fields)
        stage-keys (:stage-cache-keys artifact)
        entries (:cache-entry-manifest artifact)
        invalidations (set (map :invalidating-input
                                (:invalidation-trace artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:incremental-diagnostic-stream
                                       :diagnostics])))]
    (doseq [cache-key stage-keys]
      (let [present (set (keys cache-key))]
        (when-not (every? #(perf-present? (get cache-key %)) required)
          (incremental-fail! "C16-KEY" source-path cache-key
                             {:missing-fields (vec (remove present required))}))))
    (doseq [entry entries]
      (when-not (and (= :gravity/cache-entry (:artifact entry))
                     (:cache-key entry)
                     (:artifact-id entry)
                     (:producer entry)
                     (seq (:inputs entry))
                     (seq (:preserved-facts entry))
                     (seq (:invalidated-by entry))
                     (:diagnostics entry)
                     (:provenance entry)
                     (:revalidation entry))
        (incremental-fail! "C16-ENTRY" source-path entry
                           {:missing-fields [:cache-entry]})))
    (when-not (set/subset? (set invalidation-causes) invalidations)
      (incremental-fail! "C16-STALE" source-path
                         (first (:invalidation-trace artifact))
                         {:missing-fields [:invalidation-trace]}))
    (doseq [[id path missing]
            [["C16-PROOF"
              [:stale-proof-rejection-report :status]
              [:stale-proof-rejection]]
             ["C16-SPECULATIVE"
              [:speculative-reuse-record :publish-status]
              [:speculative-boundary]]
             ["C16-REPLAY"
              [:build-effect-replay-record :status]
              [:build-effect-replay]]
             ["C16-POLICY"
              [:policy-compatibility-report :status]
              [:policy-compatibility]]
             ["C16-DIAGNOSTIC"
              [:stale-diagnostic-rejection-report :status]
              [:diagnostic-revalidation]]
             ["C16-GRAPH"
              [:incremental-dependency-graph :status]
              [:incremental-graph]]]
            :let [expected (case id
                             "C16-SPECULATIVE" :blocked-from-release
                             "C16-REPLAY" :complete
                             "C16-POLICY" :compatible
                             "C16-GRAPH" :consistent
                             :rejected)]
            :when (not= expected (get-in artifact path))]
      (incremental-fail! id source-path (get-in artifact [(first path)])
                         {:missing-fields missing}))
    (when-not (= (set diagnostic-ids) diagnostics)
      (incremental-fail! "C16-GRAPH" source-path
                         (:incremental-diagnostic-stream artifact)
                         {:missing-fields [:incremental-diagnostics]})))
  :complete)
