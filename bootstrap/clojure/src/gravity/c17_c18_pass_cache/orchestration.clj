(ns gravity.c17-c18-pass-cache.orchestration
  "C17/C18 cache execution and four-pass evidence composition."
  (:require [gravity.c17-c18-pass-cache.contract :as contract]))

(defn lookup-or-compute!
  [{:keys [validate-upstream! validate-context! validate-operations!
           c17-stage-request c18-stage-request stage-cache-key
           cache-lookup-or-compute! stage-cache-operations decode-envelope!
           compose-evidence-dag evidence-root]}
   store upstream-result context operations]
  (let [upstream-result (validate-upstream! upstream-result)
        context (validate-context! context)
        operations (validate-operations! operations)
        c16-artifact (:c16-artifact upstream-result)
        c17-request (c17-stage-request context upstream-result)
        c17-key (stage-cache-key c17-request)
        c17-operations
        (stage-cache-operations
         context :c17 #((:produce-c17! operations) c16-artifact)
         (:validate-c17! operations) operations)
        c17-result
        (cache-lookup-or-compute! store c17-key c17-request c17-operations)
        c17-artifact
        (decode-envelope! :c17 (:artifact c17-result)
                          (:validate-c17! operations) operations)
        c17-receipt (:producer-receipt c17-result)
        c18-request (c18-stage-request context c17-receipt)
        c18-key (stage-cache-key c18-request)
        c18-operations
        (stage-cache-operations
         context :c18 #((:produce-c18! operations) c17-artifact)
         (:validate-c18! operations) operations)
        c18-result
        (cache-lookup-or-compute! store c18-key c18-request c18-operations)
        c18-artifact
        (decode-envelope! :c18 (:artifact c18-result)
                          (:validate-c18! operations) operations)
        upstream-dag (:evidence-dag upstream-result)
        receipts (into (vec (:receipts upstream-dag))
                       [c17-receipt (:producer-receipt c18-result)])
        contracts (into (vec (:contracts upstream-dag))
                        [contract/c17-pass-contract contract/c18-pass-contract])
        evidence-dag (compose-evidence-dag receipts contracts)]
    {:artifact :gravity/c17-c18-pass-cache-result
     :schema-version 1
     :c17-artifact c17-artifact
     :c18-artifact c18-artifact
     :c17-cache-evidence (:cache-evidence c17-result)
     :c18-cache-evidence (:cache-evidence c18-result)
     :c17-producer-receipt c17-receipt
     :c18-producer-receipt (:producer-receipt c18-result)
     :evidence-dag evidence-dag
     :evidence-root-id (evidence-root evidence-dag)
     :authority :none
     :release-authority? false
     :proof-authority? false
     :self-hosting-authority? false}))
