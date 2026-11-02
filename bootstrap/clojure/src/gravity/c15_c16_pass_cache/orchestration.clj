(ns gravity.c15-c16-pass-cache.orchestration
  "Adjacent C15/C16 cache execution over explicitly injected operations.")

(defn lookup-or-compute!
  [{:keys [validate-context! validate-operations! c15-stage-request
           c16-stage-request stage-cache-key cache-lookup-or-compute!
           stage-cache-operations decode-envelope! compose-evidence-dag
           evidence-root c15-pass-contract c16-pass-contract]}
   store context operations]
  (let [context (validate-context! context)
        operations (validate-operations! operations)
        c15-request (c15-stage-request context)
        c15-key (stage-cache-key c15-request)
        c15-operations
        (stage-cache-operations context :c15 (:produce-c15! operations)
                                (:validate-c15! operations) operations)
        c15-result
        (cache-lookup-or-compute! store c15-key c15-request c15-operations)
        c15-artifact
        (decode-envelope! :c15 (:artifact c15-result)
                          (:validate-c15! operations) operations)
        c15-artifact-id ((:artifact-id-of operations) c15-artifact)
        c16-request (c16-stage-request context c15-artifact-id)
        c16-key (stage-cache-key c16-request)
        c16-operations
        (stage-cache-operations
         context :c16
         #((:produce-c16! operations) c15-artifact)
         (:validate-c16! operations) operations)
        c16-result
        (cache-lookup-or-compute! store c16-key c16-request c16-operations)
        receipts [(:producer-receipt c15-result)
                  (:producer-receipt c16-result)]
        contracts [c15-pass-contract c16-pass-contract]
        evidence-dag (compose-evidence-dag receipts contracts)]
    {:artifact :gravity/c15-c16-pass-cache-result
     :schema-version 1
     :c15-artifact c15-artifact
     :c16-artifact
     (decode-envelope! :c16 (:artifact c16-result)
                       (:validate-c16! operations) operations)
     :c15-cache-evidence (:cache-evidence c15-result)
     :c16-cache-evidence (:cache-evidence c16-result)
     :c15-producer-receipt (first receipts)
     :c16-producer-receipt (second receipts)
     :evidence-dag evidence-dag
     :evidence-root-id (evidence-root evidence-dag)
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))
