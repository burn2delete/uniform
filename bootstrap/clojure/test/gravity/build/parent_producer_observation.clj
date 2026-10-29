(ns gravity.build.parent-producer-observation
  "Source-only RV2 parent adapter. Does not launch work or admit a capture."
  (:require [gravity.build.producer-evidence :as evidence]
            [gravity.self-hosting.sh01-parallel-test-runner :as runner]))

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

(defn- complete-stream? [result stream]
  (let [field #(get result (keyword (str (name stream) "-" %)))]
    (and (= :complete (field "capture-status"))
         (false? (field "truncated?"))
         (false? (field "capture-forced-close?"))
         (nil? (field "capture-error")))))

(defn serialize-capture
  "Connect retained parent observations to the existing fresh serializer.

  Copies the GLOBAL cleanup field, never tracked-handle completion. Current
  canonical observations leave that field nil and therefore refuse capture v1.
  Supplied/injected observations still confer no execution or review authority.
  Does not launch, wait, finalize again, read a report file or publish output."
  [contract-bytes report observation]
  (when-not (and (= :gravity/rv2-parent-process-observation-v1 (:schema observation))
                 (= :non-authoritative (:authority observation))
                 (map? (:process-result observation)))
    (throw (ex-info "RV2 capture requires a retained parent observation"
                    {:id "RV2-PARENT-OBSERVATION"})))
  (let [result (:process-result observation)
        terminal {:exit-code (:exit-code result)
                  :natural-exit? (and (= :passed (:status result))
                                      (false? (:wait-interrupted? result))
                                      (nil? (:wait-error result))
                                      (true? (:root-exited? result)))
                  :timed-out? (:timeout-containment-unproven? result)
                  :stdout-complete? (complete-stream? result :stdout)
                  :stderr-complete? (complete-stream? result :stderr)
                  :cleanup-complete? (:cleanup-complete? result)
                  :root-dead? (:root-exited? result)}]
    (evidence/serialize-observed-fresh-capture contract-bytes report terminal)))
