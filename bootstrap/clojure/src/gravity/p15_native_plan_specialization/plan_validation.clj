(ns gravity.p15-native-plan-specialization.plan-validation
  (:require [gravity.bootstrap :as bootstrap]))

;; The plan shape is semantic admission, not a work allowance.  The public C
;; backend remains the source of truth for instruction legality; this check
;; keeps the specialization's narrower one-entrypoint contract explicit before
;; that validator runs.
(defn validate-plan-shape!
  [source-path plan unsupported-fail!]
  (let [functions (:functions plan)]
    (when-not (map? functions)
      (unsupported-fail! source-path
                         "native plan has no function map"
                         {:missing-fact :native-plan-functions}))
    (let [entrypoint (:entrypoint plan)
          entry-function (when (map? functions)
                           (get functions entrypoint))]
      (when-not (and (symbol? entrypoint)
                     (= 1 (count functions))
                     (map? entry-function)
                     (zero? (:arity entry-function))
                     (vector? (:instructions entry-function)))
        (unsupported-fail!
         source-path
         "native plan requires one zero-arity entrypoint"
         {:observed-entrypoint entrypoint
          :observed-function-count (count functions)
          :missing-fact :single-zero-arity-native-entrypoint})))
    (doseq [[_ function] functions]
      (when-not (map? function)
        (unsupported-fail!
         source-path
         "native plan function record is malformed"
         {:missing-fact :native-plan-function-record}))
      (when-not (vector? (:instructions function))
        (unsupported-fail!
         source-path
         "native plan function instructions are malformed"
         {:missing-fact :native-plan-instructions})))
    nil))

(defn authenticate!
  [packet context authentication-fail!]
  ;; Packet-local hashes are not authority; authenticate before inspecting the
  ;; packet plan or envelope.
  (let [source-path (when (map? context) (:source-path context))
        authenticated?
        (try
          (bootstrap/p15-s23-closed-runtime-packet-authentic?
           packet context)
          (catch Throwable _ false))]
    (when-not authenticated?
      (authentication-fail!
       source-path
       "stage2 runtime packet is not authentic for the trusted context"
       {:missing-fact :authenticated-stage2-runtime-packet-and-context
        :authenticator
        'gravity.bootstrap/p15-s23-closed-runtime-packet-authentic?
        :authenticator-arity 2}))
    (when-not (and (map? context)
                   (= :c (:requested-target context)))
      (authentication-fail!
       source-path
       "native plan specialization requires the :c requested target"
       {:observed-target (when (map? context) (:requested-target context))
        :missing-fact :native-c-target-context}))
    packet))
