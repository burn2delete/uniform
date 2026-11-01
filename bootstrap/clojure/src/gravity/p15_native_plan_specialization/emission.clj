(ns gravity.p15-native-plan-specialization.emission
  (:require [gravity.bootstrap :as bootstrap]))

(defn validate-and-emit!
  [packet context
   {:keys [validate-plan-shape! compile-helper! helper-safety-proof
           helper-contract helper-function utf8-bytes
           unsupported-fail! helper-contract-fail! helper-rejected-fail!
           authentication-fail!]}]
  (let [source-path (:source-path context)
        plan (:plan packet)]
    (validate-plan-shape! source-path plan)
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
          safety (try
                   (helper-safety-proof plan)
                   (catch
                     StackOverflowError
                     error
                     (helper-contract-fail!
                       source-path
                       "Gravity C emitter helper safety proof exceeded the host stack"
                       {:missing-fact
                        :gravity-c-emitter-helper-safety-host-stack-failure
                        :boundary :helper-safety-proof
                        :cause-message (.getMessage error)})))
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
          expected-output (:reference-output packet)]
      (when-not (string? expected-output)
        (authentication-fail! source-path
                              "authenticated packet has no reference output"
                              {:missing-fact :authenticated-reference-output}))
      {:source source
       :source-bytes source-bytes
       :source-content-hash (str "sha256:" (bootstrap/sha256-hex source))
       :expected-output expected-output
       :helper helper
       :helper-safety safety
       :helper-result helper-result})))
