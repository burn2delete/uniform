(ns gravity.p15-native-plan-specialization.helper-compilation
  (:require [clojure.string :as str]
            [gravity.bootstrap :as bootstrap]))

(defn compile-gravity-c-emitter-helper!
  [request-source
   {:keys [source-loader helper-contract-fail! helper-source-relative
           max-helper-source-bytes helper-source-content-hash helper-function
           helper-function-shape helper-contract helper-function-semantic-hash
           helper-contract-hash]}]
  (let [snapshot
        (try
          (source-loader request-source)
          (catch clojure.lang.ExceptionInfo error
            (throw error))
          (catch Throwable error
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source loader failed"
             {:missing-fact :gravity-c-emitter-source-loader
              :cause-message (.getMessage error)})))
        _ (when-not (and (map? snapshot)
                         (= #{:source-path :source-byte-count
                              :source-content-hash :source-text}
                            (set (keys snapshot)))
                         (string? (:source-path snapshot))
                         (string? (:source-text snapshot))
                         (integer? (:source-byte-count snapshot))
                         (string? (:source-content-hash snapshot)))
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source snapshot is malformed"
             {:missing-fact :gravity-c-emitter-source-snapshot
              :observed-snapshot snapshot}))
        _ (when-not (str/ends-with? (:source-path snapshot)
                                    helper-source-relative)
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source path is not the tracked helper"
             {:missing-fact :gravity-c-emitter-source-path
              :expected-relative-path helper-source-relative
              :observed-source-path (:source-path snapshot)}))
        _ (when (> (:source-byte-count snapshot) max-helper-source-bytes)
            (helper-contract-fail!
             request-source
             "Gravity C emitter helper source snapshot exceeds its bound"
             {:maximum-helper-source-bytes max-helper-source-bytes
              :observed-helper-source-bytes (:source-byte-count snapshot)
              :missing-fact :bounded-helper-source-snapshot}))
        actual-source-content-hash
        (str "sha256:" (bootstrap/sha256-hex (:source-text snapshot)))]
    (when-not (= actual-source-content-hash
                 (:source-content-hash snapshot)
                 helper-source-content-hash)
      (helper-contract-fail!
       request-source
       "Gravity C emitter helper source content hash is not pinned"
       {:expected-source-content-hash helper-source-content-hash
        :observed-source-content-hash actual-source-content-hash
        :snapshot-source-content-hash (:source-content-hash snapshot)
        :missing-fact :pinned-gravity-c-emitter-source-content-hash}))
    (let [emitter-rule
          (try
            (bootstrap/c-backend-stage2-plan-emitter-source-rule!
             request-source :jvm)
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               request-source
               "Pinned Gravity plan-emitter rule could not be loaded"
               {:missing-fact :pinned-stage2-plan-emitter-rule
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               request-source
               "Pinned Gravity plan-emitter rule could not be loaded"
               {:missing-fact :pinned-stage2-plan-emitter-rule
                :cause-message (.getMessage error)})))
          helper-plan
          (try
            (bootstrap/p15-s23-stage2-plan-emitter-compile-source
             (:emitter emitter-rule)
             (:source-path snapshot)
             (:source-text snapshot))
            (catch clojure.lang.ExceptionInfo error
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source did not compile"
               {:missing-fact :gravity-c-emitter-source-compilation
                :cause-diagnostic (:id (ex-data error))
                :cause-facts (ex-data error)}))
            (catch Throwable error
              (helper-contract-fail!
               request-source
               "Gravity C emitter helper source did not compile"
               {:missing-fact :gravity-c-emitter-source-compilation
                :cause-message (.getMessage error)})))
          definition (get-in helper-plan [:functions helper-function])
          observed-shape (when (map? definition)
                           {:function helper-function
                            :arity (:arity definition)
                            :params (:params definition)})]
      (when-not (and (map? helper-plan)
                     (= :gravity/stage2-hosted-core-compiled-plan
                        (:kind helper-plan))
                     (= :hosted (get-in helper-plan [:module :profile]))
                     (= :jvm (get-in helper-plan [:module :target]))
                     (= 'main (:entrypoint helper-plan))
                     (map? definition)
                     (= helper-function-shape observed-shape))
        (helper-contract-fail!
         request-source
         "Gravity C emitter helper export shape is not exact"
         {:missing-fact :gravity-c-emitter-export-shape
          :expected-function-shape helper-function-shape
          :observed-function-shape observed-shape
          :observed-plan-kind (:kind helper-plan)
          :observed-plan-entrypoint (:entrypoint helper-plan)}))
      {:snapshot snapshot
       :emitter-rule emitter-rule
       :plan helper-plan
       :function definition
       :function-semantic-hash (helper-function-semantic-hash definition)
       :contract-hash (helper-contract-hash
                       (:source-content-hash snapshot))})))
