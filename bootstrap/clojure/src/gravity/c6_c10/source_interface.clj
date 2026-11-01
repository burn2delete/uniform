(ns gravity.c6-c10.source-interface
  "Explicit current C6 source-contract dispatch, not Core admission."
  (:require [gravity.source-interface-header :as shared]))

(def legacy-version 1)

(defn interface-version!
  "Select the built-in current interface using the shared native header parser.
   Missing/foreign selectors do not fall back to historical v1. No source,
   compiler, report or user callback is consulted by this operation."
  [header]
  (try
    (shared/source-interface-version! header :gravity/c6-c10)
    (catch clojure.lang.ExceptionInfo error
      (let [data (ex-data error)]
        (if (and (= "C11-VERIFY" (:id data))
                 (= :versioned-c11-interface (:missing-fact data)))
          (throw (ex-info "Invalid C6 source interface header"
                          {:id "C6-VERIFY" :missing-fact :versioned-c6-source-interface
                           :reason (:reason data)}))
          (throw error))))))
