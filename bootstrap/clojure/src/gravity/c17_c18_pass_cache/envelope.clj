(ns gravity.c17-c18-pass-cache.envelope
  "Bounded C17/C18 cache envelope encoding and decoding."
  (:require [clojure.edn :as edn]
            [gravity.c17-c18-pass-cache.validation :as validation]))

(def ^:private maximum-envelope-characters (* 8 1024 1024))

(def ^:private envelope-fields
  #{:artifact :schema-version :stage :artifact-id :payload-edn})

(defn encode!
  [stage artifact operations]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        _ (validation/require-sha256! :artifact-id artifact-id)
        payload (pr-str artifact)]
    (when (> (count payload) maximum-envelope-characters)
      (validation/fail! "C16-ENTRY"
                        "C17/C18 artifact envelope exceeds its local bound"
                        {:stage stage
                         :maximum-characters maximum-envelope-characters}))
    {:artifact :gravity/c17-c18-pass-cache-envelope
     :schema-version 1
     :stage stage
     :artifact-id artifact-id
     :payload-edn payload}))

(defn decode!
  [stage envelope validate operations]
  (when-not (and (map? envelope)
                 (= envelope-fields (set (keys envelope)))
                 (= :gravity/c17-c18-pass-cache-envelope (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= stage (:stage envelope))
                 (validation/sha256-id? (:artifact-id envelope))
                 (string? (:payload-edn envelope))
                 (<= (count (:payload-edn envelope))
                     maximum-envelope-characters))
    (validation/fail! "C16-ENTRY" "C17/C18 cache envelope is malformed"
                      {:stage stage}))
  (let [artifact
        (try
          (edn/read-string
           {:readers {}
            :default (fn [tag _]
                       (validation/fail!
                        "C16-ENTRY"
                        "C17/C18 cache envelope contains an unknown tag"
                        {:stage stage :tag tag}))}
           (:payload-edn envelope))
          (catch clojure.lang.ExceptionInfo error (throw error))
          (catch Throwable error
            (validation/fail! "C16-ENTRY"
                              "C17/C18 cache envelope EDN is malformed"
                              {:stage stage
                               :host-error (.getName (class error))})))
        observed ((:artifact-id-of operations) artifact)]
    (when-not (= observed (:artifact-id envelope))
      (validation/fail! "C16-STALE"
                        "C17/C18 envelope artifact identity is stale"
                        {:stage stage
                         :expected (:artifact-id envelope)
                         :observed observed}))
    (validate artifact)
    artifact))
