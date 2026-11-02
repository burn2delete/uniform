(ns gravity.c15-c16-pass-cache.envelope
  "Bounded cache envelopes and generic stage-operation adapters."
  (:require [gravity.pass-cache.envelope-codec :as codec]
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

(defn stage-cache-operations
  [context stage produce validate operations]
  {:produce! (fn [_] (encode! stage (produce) operations))
   :validate-output!
   (fn [envelope _ _]
     (decode! stage envelope validate operations)
     envelope)
   :artifact-id-of
   (fn [envelope]
     (validation/require-sha256! :artifact-id (:artifact-id envelope)))
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
