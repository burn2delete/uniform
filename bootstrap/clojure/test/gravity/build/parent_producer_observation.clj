(ns gravity.build.parent-producer-observation
  "Source-only RV2 parent adapter. Does not launch work or admit a capture."
  (:require [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

(defn observe!
  "Consume one newly launched Process through the canonical ownership API.

  The caller owns launch, exact loader/options/output binding and admission.
  process-result waits, captures and finalizes the same raw token, including
  its exceptional path. Never reconstruct ownership from PIDs or finalize a
  second time here. The returned raw token is local state, not EDN evidence.
  Selected-workload descendant reachability remains explicitly unresolved."
  [process options]
  (let [owned (runner/retain-owned-process! process)
        result (runner/process-result process options owned)]
    {:schema :gravity/rv2-parent-process-observation-v1
     :authority :non-authoritative
     :owned-process owned
     :retained-handles (runner/retained-process-handles owned)
     :process-result result
     :root-exited? (:root-exited? result)
     :tracked-owned-processes-terminal? (:tracked-owned-processes-terminal? result)
     :cleanup-complete? (:cleanup-complete? result)
     :descendant-coverage (:descendant-coverage result)
     :selected-workload-reachability :unresolved
     :capture-v1-ready? false
     :cache-admission? false}))
