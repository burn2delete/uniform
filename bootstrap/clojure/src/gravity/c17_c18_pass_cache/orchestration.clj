(ns gravity.c17-c18-pass-cache.orchestration
  "C17/C18 cache execution and four-pass evidence composition."
  (:require [gravity.c17-c18-pass-cache.cache-operation :as cache-operation]
            [gravity.c17-c18-pass-cache.contract :as contract]
            [gravity.c17-c18-pass-cache.envelope :as envelope]
            [gravity.pass-execution :as pass-execution]))

(def ^:private default-stage-cache-operations
  cache-operation/stage-cache-operations)
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
  [{:keys [validate-upstream! validate-context! validate-operations!
           c17-stage-request c18-stage-request stage-cache-key
           cache-lookup-or-compute! stage-cache-operations decode-envelope!
           compose-evidence-dag evidence-root]}
   store upstream-result context operations]
  (let [upstream-result (validate-upstream! upstream-result)
        context (validate-context! context)
        operations (validate-operations! operations)
        decoded-handoff
        (when (and (identical? stage-cache-operations
                               default-stage-cache-operations)
                   (identical? decode-envelope! default-decode-envelope!))
          (atom {}))
        stage-context (context-with-decoded-handoff context decoded-handoff)
        c16-artifact (:c16-artifact upstream-result)
        c17-request (c17-stage-request context upstream-result)
        c17-key (stage-cache-key c17-request)
        c17-operations
        (stage-cache-operations
         stage-context :c17 #((:produce-c17! operations) c16-artifact)
         (:validate-c17! operations) operations)
        c17-result
        (cache-lookup-or-compute! store c17-key c17-request c17-operations)
        c17-artifact
        (decode-or-reuse! decoded-handoff decode-envelope! :c17
                          (:artifact c17-result)
                          (:validate-c17! operations) operations)
        c17-receipt (:producer-receipt c17-result)
        c18-request (c18-stage-request context c17-receipt)
        c18-key (stage-cache-key c18-request)
        c18-operations
        (stage-cache-operations
         stage-context :c18 #((:produce-c18! operations) c17-artifact)
         (:validate-c18! operations) operations)
        c18-result
        (cache-lookup-or-compute! store c18-key c18-request c18-operations)
        c18-artifact
        (decode-or-reuse! decoded-handoff decode-envelope! :c18
                          (:artifact c18-result)
                          (:validate-c18! operations) operations)
        upstream-dag (:evidence-dag upstream-result)
        receipts (into (vec (:receipts upstream-dag))
                       [c17-receipt (:producer-receipt c18-result)])
        contracts (into (vec (:contracts upstream-dag))
                        [contract/c17-pass-contract contract/c18-pass-contract])
        [evidence-dag evidence-root-id]
        (compose-evidence-dag-and-root compose-evidence-dag evidence-root
                                       receipts contracts)]
    {:artifact :gravity/c17-c18-pass-cache-result
     :schema-version 1
     :c17-artifact c17-artifact
     :c18-artifact c18-artifact
     :c17-cache-evidence (:cache-evidence c17-result)
     :c18-cache-evidence (:cache-evidence c18-result)
     :c17-producer-receipt c17-receipt
     :c18-producer-receipt (:producer-receipt c18-result)
     :evidence-dag evidence-dag
     :evidence-root-id evidence-root-id
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))
