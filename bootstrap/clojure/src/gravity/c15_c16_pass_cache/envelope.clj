(ns gravity.c15-c16-pass-cache.envelope
  "Bounded cache envelopes and generic stage-operation adapters."
  (:require [gravity.pass-cache.envelope-codec :as codec]
            [gravity.pass-cache.receipt-validation :as receipt-validation]
            [gravity.c15-c16-pass-cache.validation :as validation]))

(def ^:private maximum-envelope-characters (* 8 1024 1024))

(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(defn- codec-options
  []
  {:artifact-tag :gravity/c15-c16-pass-cache-envelope
   :envelope-fields envelope-fields
   :maximum-envelope-characters maximum-envelope-characters
   :require-sha256! validation/require-sha256!
   :sha256-id? validation/sha256-id?
   :fail! validation/fail!
   :label "C15/C16"})

(defn encode!
  [stage artifact operations]
  (codec/encode! stage artifact operations (codec-options)))

(defn decode!
  [stage envelope validate operations]
  (codec/decode! stage envelope validate operations (codec-options)))

(def ^:private decoded-handoff-context-key
  :gravity.pass-cache/decoded-artifact-handoff)

(defn- decoded-handoff
  [context]
  (get (meta context) decoded-handoff-context-key))

(defn stage-cache-operations
  [context stage produce validate operations]
  {:produce! (fn [_] (encode! stage (produce) operations))
   :validate-output!
   (fn [envelope _ _]
     (let [artifact (decode! stage envelope validate operations)]
       ;; The handoff is invocation-local and is populated only after the
       ;; complete envelope identity and artifact validation above succeeds.
       (when-let [decoded-handoff (decoded-handoff context)]
         (swap! decoded-handoff assoc stage
                {:stage stage
                 :envelope envelope
                 :artifact artifact
                 :validator validate
                 :operations operations}))
       envelope))
   :artifact-id-of
   receipt-validation/envelope-artifact-id
   :validation-binding-id (get-in context [:validation-binding-ids stage])
   :verifier-reports (fn [& _] [])
   :evidence-records
   (fn [envelope _ _]
     (if (= :c15 stage)
       [{:evidence-id (get-in context [:diagnostic-stream-ids :c15])
         :kind :diagnostic-schema
         :status :accepted
         :artifact-id (:artifact-id envelope)
         :authority-level :none}]
       []))
   :validate-diagnostic-stream!
   (fn [stream-id receipt]
     (when-not (and (= stream-id
                       (get-in context [:diagnostic-stream-ids stage]))
                    (= stream-id (:diagnostic-stream-id receipt)))
       (validation/fail!
        "C16-DIAGNOSTIC"
        "C15/C16 receipt diagnostic stream binding is stale"
        {:pass stage})))
   :validate-verifier-report!
   (fn [& _]
     (validation/fail!
      "C18-EVIDENCE"
      "C15/C16 compatibility cache admits no verifier reports" {}))
   :validate-evidence-record!
   (fn [record receipt]
     (when-not
      (and (= :c15 stage)
           (= {:evidence-id (get-in context [:diagnostic-stream-ids :c15])
               :kind :diagnostic-schema
               :status :accepted
               :artifact-id (:output-artifact-id receipt)
               :authority-level :none}
              record))
       (validation/fail!
        "C18-EVIDENCE"
        "C15 replacement evidence differs from its current binding"
        {:pass stage})))})
