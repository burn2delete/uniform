(ns gravity.pass-cache.execution
  "Per-key lookup, producer execution, and publication orchestration."
  (:require [gravity.pass-cache.locking :refer :all]
            [gravity.pass-cache.lookup :refer :all]
            [gravity.pass-cache.path-policy :refer :all]
            [gravity.pass-cache.policy :refer :all]
            [gravity.pass-cache.publication :refer :all]
            [gravity.pass-cache.request-key :refer :all]
            [gravity.pass-cache.store-open :refer :all]
            [gravity.pass-execution :as pass-execution]))

(defn lookup-or-compute!
  "Reuse a fully revalidated hit, or execute one producer pass and publish it."
  [store key execution-request operations]
  (validate-store! store)
  (validate-key! key)
  ;; The semantic id deliberately excludes monotone authority levels and
  ;; nonsemantic provenance detail.  Execution must nevertheless use the exact
  ;; request from which the supplied key was projected, or a weaker/currently
  ;; different request could borrow authority or provenance carried by `key`.
  (when-not (= key (stage-cache-key execution-request))
    (fail! "C16-KEY" "execution request does not bind supplied cache key" {}))
  (with-key-lock
    store (key-id key)
    (fn []
      ;; The lock is held across lookup and producer execution.  Therefore two
      ;; cooperative writers execute at most one producer for one semantic key.
      (let [looked-up (lookup! store key operations)]
        (if (= :hit (:status looked-up))
          looked-up
          (let [execution (pass-execution/execute-pass!
                           execution-request (execution-operations operations))
                rejected? (= :rejected
                             (get-in looked-up [:cache-evidence :status]))]
            (if rejected?
              {:status :miss
               :key key
               :artifact (:artifact execution)
               :producer-receipt (:receipt execution)
               :cache-evidence {:artifact :gravity/pass-cache-evidence
                                :schema-version schema-version
                                :status :miss
                                :cache-key-id (key-id key)
                                :cache-publication :withheld
                                :rejected-entry-evidence
                                (:cache-evidence looked-up)
                                :producer-executed? true
                                :reader-executed? true
                                :artifact-reused? false
                                :local? true :speculative? true
                                :authoritative? false :release? false
                                :proof? false :equivalence? false
                                :self-hosting? false}}
              (let [stored (store-unlocked! store key (:artifact execution)
                                             (:receipt execution)
                                             operations true)]
                (assoc stored :cache-evidence
                       (assoc (:cache-evidence stored)
                              :producer-executed? true
                              :reader-executed? true))))))))))
