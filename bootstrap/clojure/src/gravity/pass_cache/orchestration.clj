(ns gravity.pass-cache.orchestration
  "Shared mechanics for adjacent pass-cache orchestration adapters.")

(def ^:private decoded-handoff-context-key
  :gravity.pass-cache/decoded-artifact-handoff)

(defn compose-evidence-dag-and-root
  [default-compose-evidence-dag default-evidence-root
   compose-evidence-dag evidence-root receipts contracts]
  (let [evidence-dag (compose-evidence-dag receipts contracts)]
    ;; The default composer has just validated and canonicalized this DAG; do
    ;; not add a spoofable validation marker to the public envelope.
    [evidence-dag
     (if (and (identical? compose-evidence-dag default-compose-evidence-dag)
              (identical? evidence-root default-evidence-root))
       (:evidence-root-id evidence-dag)
       (evidence-root evidence-dag))]))

(defn decode-or-reuse!
  [decoded-handoff decode-envelope! stage envelope validator operations]
  (if-let [entry (and decoded-handoff (get @decoded-handoff stage))]
    (if (and (= stage (:stage entry))
             (identical? envelope (:envelope entry))
             (identical? validator (:validator entry))
             (identical? operations (:operations entry)))
      (:artifact entry)
      (decode-envelope! stage envelope validator operations))
    (decode-envelope! stage envelope validator operations)))

(defn context-with-decoded-handoff
  [context decoded-handoff]
  (if decoded-handoff
    (with-meta context
      (assoc (meta context) decoded-handoff-context-key decoded-handoff))
    context))
