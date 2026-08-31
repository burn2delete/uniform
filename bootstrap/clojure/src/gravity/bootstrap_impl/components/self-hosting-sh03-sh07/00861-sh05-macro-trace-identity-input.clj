

(defn sh05-macro-trace-identity-input
  [trace]
  {:domain :gravity/sh05-macro-expansion-trace-v2
   :item-count (count trace)
   :item-chunks
   (sh05-ordered-identity-chunks
    trace
    (fn [ordinal step]
      {:ordinal ordinal
       :input-syntax-id (:input-syntax-id step)
       :output-syntax-id (:output-syntax-id step)
       :macro (:macro step)
       :macro-version (:macro-version step)
       :trace-replay-id (:trace-replay-id step)}))})

(defn sh05-macro-trace-id
  [source-path trace]
  (p15-s23-c6c10-canonical-digest
   source-path (sh05-macro-trace-identity-input trace)))

(defn sh05-macro-envelope-summary
  [artifact]
  {:slice :SH-05
   :status (:status artifact)
   :artifact-id (:artifact-id artifact)
   :semantic-payload-id (:artifact-id artifact)
   :expanded-syntax-stream-id (:expanded-syntax-stream-id artifact)
   :macro-expansion-trace-id (:macro-expansion-trace-id artifact)
   :expanded-defn-count (:expanded-defn-count artifact)
   :sh04-artifact-id (get-in artifact [:sh04-syntax-artifact :artifact-id])
   :ordered-run-count
   (count (get-in artifact [:gravity-macro-boundary :expansion-runs]))
   :ordered-run-identity-chunks
   (sh05-ordered-identity-chunks
    (get-in artifact [:gravity-macro-boundary :expansion-runs])
    (fn [ordinal run]
      {:ordinal ordinal
       :artifact-id (get-in run [:resolved-expansion :artifact-id])
       :input-syntax-id
       (get-in run [:resolved-expansion :input-syntax-id])
       :output-syntax-id
       (get-in run [:resolved-expansion :output-syntax-id])
       :macro-version
       (get-in run [:resolved-expansion :macro-version])
       :trace-replay-id
       (get-in run [:resolved-expansion
                    :macro-expansion-trace 0 :trace-replay-id])}))})

(defn sh05-macro-gravity-verifier-report
  [source-path boundary]
  (let [binding (sh05-macro-current-binding! source-path)
        reports
        (mapv
         (fn [run]
           (let [raw (:raw-template-result run)
                 resolved (:resolved-expansion run)
                 requests (:digest-requests run)
                 digests (:resolved-digests run)]
             {:template
              (sh05-macro-execute!
               source-path binding 'sh05-verify-macro-template
               [(:expansion-template raw) requests])
              :resolved
              (sh05-macro-execute!
               source-path binding 'sh05-verify-macro-resolved
               [resolved requests digests])}))
         (:expansion-runs boundary))
        template-passed?
        (every? #(= :passed (get-in % [:template :status])) reports)
        resolved-passed?
        (every? #(= :passed (get-in % [:resolved :status])) reports)]
    {:template {:status (if template-passed? :passed :failed)
                :verified-run-count (count reports)}
     :resolved {:status (if resolved-passed? :passed :failed)
                :verified-run-count (count reports)}
     :runs reports}))