(ns gravity.c17-c18-pass-cache.upstream-boundary
  "Validated upstream evidence and C17 receipt boundaries."
  (:require [gravity.c17-c18-pass-cache.contract :as contract]
            [gravity.c17-c18-pass-cache.validation :as validation]))

(defn validate-upstream!
  [{:keys [evidence-root]} upstream-result]
  (let [expected
        #{:artifact :schema-version :c15-artifact :c16-artifact
          :c15-cache-evidence :c16-cache-evidence :c15-producer-receipt
          :c16-producer-receipt :evidence-dag :evidence-root-id :authority
          :release-authority? :proof-authority? :self-hosting-authority?}]
    (validation/exact-map! upstream-result expected :upstream-result)
    (let [dag (:evidence-dag upstream-result)
          observed-root (evidence-root dag)
          receipts (:receipts dag)
          contracts (:contracts dag)
          c16-artifact (:c16-artifact upstream-result)]
      (when-not
       (and (= :gravity/c15-c16-pass-cache-result (:artifact upstream-result))
            (= 1 (:schema-version upstream-result))
            (= :none (:authority upstream-result))
            (false? (:release-authority? upstream-result))
            (false? (:proof-authority? upstream-result))
            (false? (:self-hosting-authority? upstream-result))
            (= observed-root (:evidence-root-id upstream-result))
            (= 2 (count receipts))
            (= 2 (count contracts))
            (= 1 (count (:edges dag)))
            (= (:c15-producer-receipt upstream-result) (first receipts))
            (= (:c16-producer-receipt upstream-result) (second receipts))
            (= (:artifact-id c16-artifact)
               (:output-artifact-id (second receipts)))
            (= contract/c16-output-facts (:output-facts (second receipts)))
            (= :none (get-in dag [:authority :effective-level])))
        (validation/fail!
         "C18-EVIDENCE"
         "C17/C18 cache upstream evidence boundary is stale"
         {:evidence-root-id (:evidence-root-id upstream-result)})))
    upstream-result))

(defn validate-c17-receipt!
  [{:keys [validate-execution-receipt!]} context c17-receipt]
  (when-not (and (map? c17-receipt)
                 (= :gravity/pass-execution-receipt (:artifact c17-receipt))
                 (= 1 (:schema-version c17-receipt))
                 (= :c17-compiler-plugin (:stage c17-receipt))
                 (validation/sha256-id? (:receipt-id c17-receipt))
                 (validation/sha256-id? (:output-artifact-id c17-receipt))
                 (= contract/c17-output-facts (:output-facts c17-receipt))
                 (= :none (get-in c17-receipt
                                  [:authority :effective-level])))
    (validation/fail! "C18-EVIDENCE"
                      "C17 producer receipt boundary is malformed" {}))
  (validate-execution-receipt!
   c17-receipt contract/c17-pass-contract
   {:validate-diagnostic-stream!
    (fn [stream-id receipt]
      (when-not (and (= stream-id (get-in context
                                           [:diagnostic-stream-ids :c17]))
                     (= stream-id (:diagnostic-stream-id receipt)))
        (validation/fail! "C16-DIAGNOSTIC"
                          "C17 receipt diagnostic stream binding is stale" {})))
    :validate-verifier-report!
    (fn [& _]
      (validation/fail! "C18-EVIDENCE"
                        "C17 receipt admits no verifier report" {}))
    :validate-evidence-record!
    (fn [& _]
      (validation/fail! "C18-EVIDENCE"
                        "C17 receipt admits no evidence record" {}))})
  c17-receipt)
