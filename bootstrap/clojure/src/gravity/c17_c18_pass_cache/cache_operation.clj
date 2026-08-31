(ns gravity.c17-c18-pass-cache.cache-operation
  "Generic cache-operation adapters for C17 and C18 artifacts."
  (:require [gravity.c17-c18-pass-cache.envelope :as envelope]
            [gravity.c17-c18-pass-cache.validation :as validation]))

(defn stage-cache-operations
  [context stage produce validate operations]
  {:produce! (fn [_] (envelope/encode! stage (produce) operations))
   :validate-output!
   (fn [encoded _ _]
     (envelope/decode! stage encoded validate operations)
     encoded)
   :artifact-id-of
   (fn [encoded]
     (validation/require-sha256! :artifact-id (:artifact-id encoded)))
   :validation-binding-id (get-in context [:validation-binding-ids stage])
   :verifier-reports (fn [& _] [])
   :evidence-records (fn [& _] [])
   :validate-diagnostic-stream!
   (fn [stream-id receipt]
     (when-not (and (= stream-id (get-in context
                                          [:diagnostic-stream-ids stage]))
                    (= stream-id (:diagnostic-stream-id receipt)))
       (validation/fail! "C16-DIAGNOSTIC"
                         "C17/C18 receipt diagnostic stream binding is stale"
                         {:pass stage})))
   :validate-verifier-report!
   (fn [& _]
     (validation/fail! "C18-EVIDENCE"
                       "C17/C18 cache admits no receipt verifier report" {}))
   :validate-evidence-record!
   (fn [& _]
     (validation/fail! "C18-EVIDENCE"
                       "C17/C18 cache admits no receipt evidence record" {}))})
