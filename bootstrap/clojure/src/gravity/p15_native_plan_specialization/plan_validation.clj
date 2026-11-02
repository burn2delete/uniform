(ns gravity.p15-native-plan-specialization.plan-validation
  (:require [gravity.bootstrap :as bootstrap]))

(defn instruction-children
  [source-path instruction unsupported-fail!]
  (when-not (map? instruction)
    (unsupported-fail! source-path
                       "native plan contains a malformed instruction"
                       {:observed-instruction instruction
                        :missing-fact :native-instruction-record}))
  (try
    (bootstrap/c-backend-instruction-children instruction)
    (catch Throwable error
      (unsupported-fail! source-path
                         "native plan instruction shape is not supported"
                         {:observed-op (:op instruction)
                          :missing-fact :native-instruction-children
                          :cause-message (.getMessage error)}))))

(defn plan-bounds!
  [source-path plan
   {:keys [maximum-instructions unsupported-fail! bounds-fail!
           scalar-value? scalar-bound! instruction-children]}]
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
    (loop [pending
           (vec (mapcat (fn [[_ function]]
                          (when-not (map? function)
                            (unsupported-fail!
                             source-path
                             "native plan function record is malformed"
                             {:missing-fact :native-plan-function-record}))
                          (let [instructions (:instructions function)]
                            (when-not (vector? instructions)
                              (unsupported-fail!
                               source-path
                               "native plan function instructions are malformed"
                               {:missing-fact :native-plan-instructions}))
                            instructions))
                        functions))
           count 0]
      (if-let [instruction (peek pending)]
        (let [pending (pop pending)
              next-count (inc count)]
          (when (> next-count maximum-instructions)
            (bounds-fail! source-path
                          "native plan exceeds the bounded instruction count"
                          {:maximum-instructions maximum-instructions
                           :observed-instructions next-count
                           :missing-fact :bounded-native-plan-instructions}))
          (doseq [[key value] instruction]
            (when (and (= key :value) (scalar-value? value))
              (scalar-bound! source-path value)))
          (let [children (instruction-children source-path instruction)]
            (doseq [child children]
              (when (and (map? child) (contains? child :value))
                (scalar-bound! source-path (:value child))))
            (recur (into pending (remove nil? children)) next-count)))
        {:instruction-count count}))))

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
