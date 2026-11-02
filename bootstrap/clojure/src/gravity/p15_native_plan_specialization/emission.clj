(ns gravity.p15-native-plan-specialization.emission
  (:require [gravity.bootstrap :as bootstrap]))

(defn validate-and-emit!
  [packet context
   {:keys [plan-bounds! compile-helper! helper-safety-proof
           helper-contract helper-function utf8-bytes
           max-generated-source-bytes max-reference-output-bytes
           unsupported-fail! helper-contract-fail! helper-rejected-fail!
           bounds-fail! authentication-fail!]}]
  (let [source-path (:source-path context)
        plan (:plan packet)
        bounds (plan-bounds! source-path plan)]
    (try
      (bootstrap/c-backend-validate-runtime-plan! source-path :c plan)
      (catch clojure.lang.ExceptionInfo error
        (unsupported-fail!
         source-path
         "authenticated plan is outside the runtime-derived C subset"
         {:cause-diagnostic (:id (ex-data error))
          :cause-facts (ex-data error)
          :missing-fact :public-c-backend-runtime-plan-validation}))
      (catch Throwable error
        (unsupported-fail!
         source-path
         "runtime-derived C plan validation failed"
         {:missing-fact :public-c-backend-runtime-plan-validation
          :cause-message (.getMessage error)})))
    (let [helper (compile-helper! source-path)
          safety (helper-safety-proof plan)
          helper-result
          (try
            (bootstrap/p15-s23-stage2-runtime-execute-function
             {:engine :gravity-native-plan-c-emitter-host-runner
              :compiler-artifact-plan? true}
             (:plan helper)
             helper-function
             [{:plan plan
               :safe-printable-ascii? (:safe? safety)
               :safety-facts (:facts safety)}])
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper invocation failed"
               {:missing-fact :gravity-c-emitter-runtime-invocation
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper invocation failed"
               {:missing-fact :gravity-c-emitter-runtime-invocation
                :cause-message (.getMessage error)})))
          _ (when-not (map? helper-result)
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper returned a malformed record"
               {:missing-fact :gravity-c-emitter-result-record
                :observed-result helper-result}))
          _ (when (= :rejected (:status helper-result))
              (helper-rejected-fail!
               source-path
               "authenticated plan is outside the Gravity C emitter subset"
               {:helper-diagnostic (:diagnostic helper-result)
                :helper-facts (:facts helper-result)
                :missing-fact
                (or (get-in helper-result [:facts :missing-fact])
                    :gravity-c-emitter-authenticated-subset)}))
          _ (when-not (and (= :complete (:status helper-result))
                           (= helper-contract (:contract helper-result))
                           (= :gravity-source (:implementation helper-result))
                           (string? (:source helper-result)))
              (helper-contract-fail!
               source-path
               "Gravity C emitter helper returned the wrong completion record"
               {:missing-fact :gravity-c-emitter-result-contract
                :expected-contract helper-contract
                :observed-result helper-result}))
          source (:source helper-result)
          source-bytes (utf8-bytes source)
          expected-output (:reference-output packet)
          expected-output-bytes (when (string? expected-output)
                                  (utf8-bytes expected-output))]
      (when (> (alength source-bytes) max-generated-source-bytes)
        (bounds-fail! source-path
                      "generated C source exceeds the bounded artifact size"
                      {:maximum-generated-source-bytes max-generated-source-bytes
                       :observed-generated-source-bytes (alength source-bytes)
                       :missing-fact :bounded-native-generated-source}))
      (when-not (string? expected-output)
        (authentication-fail! source-path
                              "authenticated packet has no reference output"
                              {:missing-fact :authenticated-reference-output}))
      (when (> (alength expected-output-bytes) max-reference-output-bytes)
        (bounds-fail! source-path
                      "authenticated reference output exceeds the bounded size"
                      {:maximum-reference-output-bytes max-reference-output-bytes
                       :observed-reference-output-bytes
                       (alength expected-output-bytes)
                       :missing-fact :bounded-native-reference-output}))
      {:bounds bounds
       :source source
       :source-bytes source-bytes
       :source-content-hash (str "sha256:" (bootstrap/sha256-hex source))
       :expected-output expected-output
       :helper helper
       :helper-safety safety
       :helper-result helper-result})))
