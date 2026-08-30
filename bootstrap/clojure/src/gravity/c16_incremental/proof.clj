(ns gravity.c16-incremental.proof
  (:require [clojure.set :as set]))

(defn capability-proof
  [{:keys [cache-key-required-fields invalidation-causes diagnostic-ids
           perf-present?]}
   artifact]
  {:c15-diagnostics-input-verified?
   (= :complete
      (get-in artifact [:c15-diagnostics-artifact
                        :capability-based-proof
                        :status]))
   :dependency-graph-consistent?
   (= :consistent (get-in artifact [:incremental-dependency-graph :status]))
   :stage-cache-keys-complete?
   (every? (fn [cache-key]
             (every? #(perf-present? (get cache-key %))
                     cache-key-required-fields))
           (:stage-cache-keys artifact))
   :cache-entries-retain-provenance?
   (every? #(and (= :gravity/cache-entry (:artifact %))
                 (:diagnostics %)
                 (:provenance %)
                 (:revalidation %))
           (:cache-entry-manifest artifact))
   :invalidations-cover-semantic-policy-proof-target?
   (set/subset? (set invalidation-causes)
                (set (map :invalidating-input
                          (:invalidation-trace artifact))))
   :stale-proof-rejected?
   (= :rejected (get-in artifact [:stale-proof-rejection-report :status]))
   :stale-diagnostics-rejected?
   (= :rejected (get-in artifact
                        [:stale-diagnostic-rejection-report :status]))
   :speculative-reuse-blocked-from-release?
   (= :blocked-from-release
      (get-in artifact [:speculative-reuse-record :publish-status]))
   :build-effect-replay-recorded?
   (= :complete (get-in artifact [:build-effect-replay-record :status]))
   :revalidation-passed?
   (= :passed (get-in artifact [:revalidation-report :status]))
   :release-rebuild-reproducible?
   (= :reproducible (get-in artifact [:release-rebuild-record :status]))
   :diagnostics-covered?
   (= (set diagnostic-ids)
      (set (map :diagnostic
                (get-in artifact
                        [:incremental-diagnostic-stream :diagnostics]))))
   :status :complete})
