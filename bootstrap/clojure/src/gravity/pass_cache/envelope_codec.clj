(ns gravity.pass-cache.envelope-codec
  "Shared EDN codec for the adjacent C15-C18 cache envelope adapters.

  The adapters retain their own validation policy, envelope tag, and local
  character bound.  This namespace only owns the duplicated envelope shape
  and EDN transport mechanics; it is not a general serialization framework."
  (:require [clojure.edn :as edn]))

(defn encode!
  [stage artifact operations
   {:keys [artifact-tag maximum-envelope-characters require-sha256! fail!
           label]}]
  (let [artifact-id ((:artifact-id-of operations) artifact)
        _ (require-sha256! :artifact-id artifact-id)
        payload (pr-str artifact)]
    (when (> (count payload) maximum-envelope-characters)
      (fail! "C16-ENTRY"
             (str label " artifact envelope exceeds its local bound")
             {:stage stage
              :maximum-characters maximum-envelope-characters}))
    {:artifact artifact-tag
     :schema-version 1
     :stage stage
     :artifact-id artifact-id
     :payload-edn payload}))

(defn decode!
  [stage envelope validate operations
   {:keys [artifact-tag envelope-fields maximum-envelope-characters sha256-id?
           fail! label]}]
  (when-not (and (map? envelope)
                 (= envelope-fields (set (keys envelope)))
                 (= artifact-tag (:artifact envelope))
                 (= 1 (:schema-version envelope))
                 (= stage (:stage envelope))
                 (sha256-id? (:artifact-id envelope))
                 (string? (:payload-edn envelope))
                 (<= (count (:payload-edn envelope))
                     maximum-envelope-characters))
    (fail! "C16-ENTRY" (str label " cache envelope is malformed")
           {:stage stage}))
  (let [artifact
        (try
          (edn/read-string
           {:readers {}
            :default
            (fn [tag _]
              (fail! "C16-ENTRY"
                     (str label " cache envelope contains an unknown tag")
                     {:stage stage :tag tag}))}
           (:payload-edn envelope))
          (catch clojure.lang.ExceptionInfo error
            (throw error))
          (catch Throwable error
            (fail! "C16-ENTRY"
                   (str label " cache envelope EDN is malformed")
                   {:stage stage
                    :host-error (.getName (class error))})))
        observed ((:artifact-id-of operations) artifact)]
    (when-not (= observed (:artifact-id envelope))
      (fail! "C16-STALE"
             (str label " envelope artifact identity is stale")
             {:stage stage
              :expected (:artifact-id envelope)
              :observed observed}))
    (validate artifact)
    artifact))
