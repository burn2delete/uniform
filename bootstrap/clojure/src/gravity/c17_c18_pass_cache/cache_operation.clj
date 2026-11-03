(ns gravity.c17-c18-pass-cache.cache-operation
  "Generic cache-operation adapters for C17 and C18 artifacts."
  (:require [gravity.c17-c18-pass-cache.envelope :as envelope]
            [gravity.pass-cache.receipt-validation :as receipt-validation]
            [gravity.c17-c18-pass-cache.validation :as validation]))

(def ^:private decoded-handoff-context-key
  :gravity.pass-cache/decoded-artifact-handoff)

(defn- decoded-handoff
  [context]
  (get (meta context) decoded-handoff-context-key))

(defn stage-cache-operations
  [context stage produce validate operations]
  {:produce! (fn [_] (envelope/encode! stage (produce) operations))
   :validate-output!
   (fn [encoded _ _]
     (let [artifact (envelope/decode! stage encoded validate operations)]
       ;; The handoff is invocation-local and is populated only after the
       ;; complete envelope identity and artifact validation above succeeds.
       (when-let [decoded-handoff (decoded-handoff context)]
         (swap! decoded-handoff assoc stage
                {:stage stage
                 :envelope encoded
                 :artifact artifact
                 :validator validate
                 :operations operations}))
       encoded))
   :artifact-id-of
   receipt-validation/envelope-artifact-id
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
