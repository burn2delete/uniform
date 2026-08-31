

(defn sh07-core-file-proof-transaction
  "Build and independently audit one SH-07 module in a private proof epoch.

  The returned receipt contains counters and identity snapshots only.  Cached
  artifacts and reports are cleared before the function returns."
  [source-path]
  (when *sh07-proof-transaction-context*
    (throw
     (ex-info "Nested SH-07 proof transactions are not allowed"
              {:id "C6-VERIFY"
               :stage :sh07-proof-transaction
               :reason :nested-transaction})))
  (let [source-snapshot
        (sh07-proof-transaction-source-snapshot source-path)
        core-snapshot (sh07-proof-transaction-core-snapshot)
        context
        (atom
         {:open? true
          :owner-thread-id (sh07-proof-transaction-thread-id)
          :phase :construction
          :epoch 0
          :maximum-receipts 64
          :source-snapshot source-snapshot
          :checked-core-revision core-snapshot
          :receipts []
          :check-catalogs {}
          :executions {}
          :reuses {}
          :completed-phases []
          :cross-epoch-reuse-count 0
          :failed-report-executions 0
          :failed-report-reuse-count 0
          :construction-receipts-cleared? false
          :final-snapshot-rechecked? false
          :cleanup-complete? false})]
    (try
      (binding [*sh07-proof-transaction-context* context]
        (try
          (let [artifact (sh07-core-file-artifact source-path)
                _ (sh07-proof-transaction-transition!
                   context source-snapshot core-snapshot)
                verification (sh07-core-artifact-verification artifact)
                capability-proof
                (sh07-core-proof-from-verification-report verification)
                _
                (sh07-proof-transaction-final-snapshot-check!
                 context source-snapshot core-snapshot)
                _
                (swap! context assoc
                       :audited-artifact-id (:artifact-id artifact)
                       :verification-report-id
                       (reader-canonical-hash verification))
                transaction-status
                (if (and (= :passed (:status verification))
                         (= :complete (:status capability-proof)))
                  :passed :failed)
                receipt
                (sh07-proof-transaction-close!
                 context transaction-status)]
            {:artifact artifact
             :verification verification
             :capability-proof capability-proof
             :proof-transaction receipt})
          (finally
            (when (:open? @context)
              (let [receipt-count-before-cleanup (count (:receipts @context))
                    closed
                    (swap! context assoc
                           :open? false
                           :receipts []
                           :cleanup-complete? true)
                    cleanup
                    {:open? (:open? closed)
                     :cleanup-complete? (:cleanup-complete? closed)
                     :receipt-count-before-cleanup
                     receipt-count-before-cleanup
                     :retained-receipt-count (count (:receipts closed))
                     :owner-thread-id (:owner-thread-id closed)}]
                (when *sh07-proof-transaction-cleanup-observer*
                  (try
                    (*sh07-proof-transaction-cleanup-observer* cleanup)
                    (catch Throwable _ nil))))))))
      (catch InterruptedException interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted)))))