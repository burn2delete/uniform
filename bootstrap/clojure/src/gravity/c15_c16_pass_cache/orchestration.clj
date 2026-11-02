(ns gravity.c15-c16-pass-cache.orchestration
  "Adjacent C15/C16 cache execution over explicitly injected operations."
  (:require [gravity.c15-c16-pass-cache.envelope :as envelope]
            [gravity.pass-execution :as pass-execution]))

(def ^:private default-stage-cache-operations
  envelope/stage-cache-operations)
(def ^:private default-decode-envelope!
  envelope/decode!)
(def ^:private decoded-handoff-context-key
  :gravity.pass-cache/decoded-artifact-handoff)

(def ^:private default-compose-evidence-dag
  pass-execution/compose-evidence-dag)
(def ^:private default-evidence-root
  pass-execution/evidence-root)

(defn- compose-evidence-dag-and-root
  [compose-evidence-dag evidence-root receipts contracts]
  (let [evidence-dag (compose-evidence-dag receipts contracts)]
    ;; The default composer has just validated and canonicalized this DAG; do
    ;; not add a spoofable validation marker to the public envelope.
    [evidence-dag
     (if (and (identical? compose-evidence-dag default-compose-evidence-dag)
              (identical? evidence-root default-evidence-root))
       (:evidence-root-id evidence-dag)
       (evidence-root evidence-dag))]))

(defn- decode-or-reuse!
  [decoded-handoff decode-envelope! stage envelope validator operations]
  (if-let [entry (and decoded-handoff (get @decoded-handoff stage))]
    (if (and (= stage (:stage entry))
             (identical? envelope (:envelope entry))
             (identical? validator (:validator entry))
             (identical? operations (:operations entry)))
      (:artifact entry)
      (decode-envelope! stage envelope validator operations))
    (decode-envelope! stage envelope validator operations)))

(defn- context-with-decoded-handoff
  [context decoded-handoff]
  (if decoded-handoff
    (with-meta context
      (assoc (meta context) decoded-handoff-context-key decoded-handoff))
    context))

(defn lookup-or-compute!
  [{:keys [validate-context! validate-operations! c15-stage-request
           c16-stage-request stage-cache-key cache-lookup-or-compute!
           stage-cache-operations decode-envelope! compose-evidence-dag
           evidence-root c15-pass-contract c16-pass-contract]}
   store context operations]
  (let [context (validate-context! context)
        operations (validate-operations! operations)
        decoded-handoff
        (when (and (identical? stage-cache-operations
                               default-stage-cache-operations)
                   (identical? decode-envelope! default-decode-envelope!))
          (atom {}))
        stage-context (context-with-decoded-handoff context decoded-handoff)
        c15-request (c15-stage-request context)
        c15-key (stage-cache-key c15-request)
        c15-operations
        (stage-cache-operations stage-context :c15 (:produce-c15! operations)
                                (:validate-c15! operations) operations)
        c15-result
        (cache-lookup-or-compute! store c15-key c15-request c15-operations)
        c15-artifact
        (decode-or-reuse! decoded-handoff decode-envelope! :c15
                          (:artifact c15-result)
                          (:validate-c15! operations) operations)
        c15-artifact-id ((:artifact-id-of operations) c15-artifact)
        c16-request (c16-stage-request context c15-artifact-id)
        c16-key (stage-cache-key c16-request)
        c16-operations
        (stage-cache-operations
         stage-context :c16
         #((:produce-c16! operations) c15-artifact)
         (:validate-c16! operations) operations)
        c16-result
        (cache-lookup-or-compute! store c16-key c16-request c16-operations)
        c16-artifact
        (decode-or-reuse! decoded-handoff decode-envelope! :c16
                          (:artifact c16-result)
                          (:validate-c16! operations) operations)
        receipts [(:producer-receipt c15-result)
                  (:producer-receipt c16-result)]
        contracts [c15-pass-contract c16-pass-contract]
        [evidence-dag evidence-root-id]
        (compose-evidence-dag-and-root compose-evidence-dag evidence-root
                                       receipts contracts)]
    {:artifact :gravity/c15-c16-pass-cache-result
     :schema-version 1
     :c15-artifact c15-artifact
     :c16-artifact c16-artifact
     :c15-cache-evidence (:cache-evidence c15-result)
     :c16-cache-evidence (:cache-evidence c16-result)
     :c15-producer-receipt (first receipts)
     :c16-producer-receipt (second receipts)
     :evidence-dag evidence-dag
     :evidence-root-id evidence-root-id
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))
