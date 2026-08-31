(ns gravity.c15-c16-pass-cache.envelope
  "Bounded cache envelopes and generic stage-operation adapters."
  (:require [clojure.edn :as edn]
            [gravity.c15-c16-pass-cache.validation :as validation]))

(def ^:private maximum-envelope-characters (* 8 1024 1024))

(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(defn encode!
  [stage artifact operations]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        _ (validation/require-sha256! :artifact-id artifact-id)
        payload (pr-str artifact)]
    (when (> (count payload) maximum-envelope-characters)
      (validation/fail!
       "C16-ENTRY" "C15/C16 artifact envelope exceeds its local bound"
       {:stage stage
        :maximum-characters maximum-envelope-characters}))
    {:artifact :gravity/c15-c16-pass-cache-envelope
     :schema-version 1
     :stage stage
     :artifact-id artifact-id
     :payload-edn payload}))

(defn decode!
  [stage envelope validate operations]
  (when-not (and (map? envelope)
                 (= envelope-fields (set (keys envelope)))
                 (= :gravity/c15-c16-pass-cache-envelope
                    (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= stage (:stage envelope))
                 (validation/sha256-id? (:artifact-id envelope))
                 (string? (:payload-edn envelope))
                 (<= (count (:payload-edn envelope))
                     maximum-envelope-characters))
    (validation/fail! "C16-ENTRY" "C15/C16 cache envelope is malformed"
                      {:stage stage}))
  (let [artifact
        (try
          (edn/read-string
           {:readers {}
            :default
            (fn [tag _]
              (validation/fail!
               "C16-ENTRY" "C15/C16 cache envelope contains an unknown tag"
               {:stage stage :tag tag}))}
           (:payload-edn envelope))
          (catch clojure.lang.ExceptionInfo error
            (throw error))
          (catch Throwable error
            (validation/fail!
             "C16-ENTRY" "C15/C16 cache envelope EDN is malformed"
             {:stage stage
              :host-error (.getName (class error))})))
        observed ((:artifact-id-of operations) artifact)]
    (when-not (= observed (:artifact-id envelope))
      (validation/fail! "C16-STALE"
                        "C15/C16 envelope artifact identity is stale"
                        {:stage stage
                         :expected (:artifact-id envelope)
                         :observed observed}))
    (validate artifact)
    artifact))

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
