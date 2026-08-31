

(defn- sh07-core-artifact-verification*
  [artifact]
  (let [source-path (or (get-in artifact [:provenance :source-path])
                        "<sh07-core-verification>")
        upstream (:sh06-resolution-artifact artifact)
        upstream-verification
        (try
          (sh06-resolution-artifact-verification upstream)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _
            {:artifact :gravity/sh06-resolution-artifact-verification
             :status :failed :failed-checks [:contained-failure]}))
        expected
        (try
          (sh07-core-from-resolution-artifact upstream)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _ nil))]
    (sh07-core-verification-report
     artifact expected upstream-verification)))

(defn sh07-core-artifact-verification
  [artifact]
  (sh07-proof-transaction-report
   :sh07 :final
   {:verifier-root
    (var-get #'sh07-core-artifact-verification*)
    :report-schema-version 1
    :check-catalog-domain :gravity/sh07-final-verification-checks
    :source-content-hash sh07-core-expected-source-content-hash
    :plan-semantic-hash sh07-core-expected-plan-semantic-hash
    :functions-semantic-hash
    sh07-core-expected-functions-semantic-hash}
   artifact
   #(sh07-core-artifact-verification* artifact)))

(defn sh07-core-capability-based-proof
  [artifact]
  (let [report (sh07-core-artifact-verification artifact)]
    (assoc (:checks report)
           :artifact :gravity/sh07-core-capability-proof
           :status (if (= :passed (:status report))
                     :complete :failed)
           :failed-checks (:failed-checks report))))

(defn- sh07-core-proof-from-verification-report
  [report]
  (assoc (:checks report)
         :artifact :gravity/sh07-core-capability-proof
         :status (if (= :passed (:status report)) :complete :failed)
         :failed-checks (:failed-checks report)))

(defn- sh07-proof-transaction-source-snapshot
  [source-path]
  (let [bytes (java.nio.file.Files/readAllBytes
               (.toPath (java.io.File. source-path)))]
    {:source-path source-path
     :source-byte-count (alength bytes)
     :source-content-hash
     (str "sha256:" (sha256-bytes-hex bytes))}))

(defn- sh07-proof-transaction-core-snapshot
  []
  (let [sh05-binding @sh05-macro-cached-binding
        sh05-source
        (sh05-macro-read-pinned-source! "<sh07-proof-transaction>")
        sh06-binding @sh06-resolution-cached-binding
        sh06-source
        (sh06-resolution-read-pinned-source! "<sh07-proof-transaction>")
        binding
        (select-keys
         @sh07-core-cached-binding
         [:source-byte-count :source-content-hash :plan-semantic-hash
          :functions-semantic-hash :function-count :function-names-hash
          :function-shapes-hash :public-function-hashes
          :public-function-shapes])
        source-path (sh07-core-source-path)
        bytes (java.nio.file.Files/readAllBytes
               (.toPath (java.io.File. source-path)))
        observed
        {:source-byte-count (alength bytes)
         :source-content-hash
         (str "sha256:" (sha256-bytes-hex bytes))}]
    (when-not (= observed
                 (select-keys binding
                              [:source-byte-count :source-content-hash]))
      (throw
       (ex-info "SH-07 checked-core source differs from the active binding"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :checked-core-source-binding-changed})))
    (when-not (= (select-keys sh05-binding
                             [:source-byte-count :source-content-hash])
                 (select-keys sh05-source
                              [:source-byte-count :source-content-hash]))
      (throw
       (ex-info "SH-05 macro source differs from the active binding"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :sh05-source-binding-changed})))
    (when-not (= (select-keys sh06-binding
                             [:source-byte-count :source-content-hash])
                 (select-keys sh06-source
                              [:source-byte-count :source-content-hash]))
      (throw
       (ex-info "SH-06 resolution source differs from the active binding"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :sh06-source-binding-changed})))
    (assoc binding
           :observed-source-path source-path
           :sh05-macro-revision
           (assoc
            (select-keys
             sh05-binding
             [:source-byte-count :source-content-hash :plan-semantic-hash
              :functions-semantic-hash :function-count
              :function-names-hash :function-shapes-hash
              :public-function-hashes :public-function-shapes])
            :observed-source-path (:source-path sh05-source))
           :sh06-resolution-revision
           (assoc
            (select-keys
             sh06-binding
             [:source-byte-count :source-content-hash :plan-semantic-hash
              :functions-semantic-hash :function-count
              :function-names-hash :function-shapes-hash
              :public-function-hashes :public-function-shapes])
            :observed-source-path (:source-path sh06-source)))))

(defn- sh07-proof-transaction-phase-summary
  [state]
  {:phase (:phase state)
   :epoch (:epoch state)
   :verification-executions (:executions state)
   :verification-reuses (:reuses state)
   :receipt-count (count (:receipts state))})

(defn- sh07-proof-transaction-transition!
  [context expected-source expected-core]
  (let [source-after
        (sh07-proof-transaction-source-snapshot
         (:source-path expected-source))
        core-after (sh07-proof-transaction-core-snapshot)]
    (when-not (= expected-source source-after)
      (throw
       (ex-info "SH-07 source changed between proof phases"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :source-snapshot-changed})))
    (when-not (= expected-core core-after)
      (throw
       (ex-info "SH-07 checked-core revision changed between proof phases"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :checked-core-revision-changed})))
    (swap!
     context
     (fn [state]
       (-> state
           (update :completed-phases conj
                   (sh07-proof-transaction-phase-summary state))
           (assoc :phase :independent-audit
                  :epoch (inc (:epoch state))
                  :receipts []
                  :executions {}
                  :reuses {}
                  :construction-receipts-cleared? true))))))

(defn- sh07-proof-transaction-final-snapshot-check!
  [context expected-source expected-core]
  (let [source-after
        (sh07-proof-transaction-source-snapshot
         (:source-path expected-source))
        core-after (sh07-proof-transaction-core-snapshot)]
    (when-not (= expected-source source-after)
      (throw
       (ex-info "SH-07 source changed during independent audit"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :source-snapshot-changed-during-audit})))
    (when-not (= expected-core core-after)
      (throw
       (ex-info "SH-07 checked-core revision changed during independent audit"
                {:id "C6-VERIFY"
                 :stage :sh07-proof-transaction
                 :reason :checked-core-revision-changed-during-audit})))
    (swap! context assoc :final-snapshot-rechecked? true)))

(defn- sh07-proof-transaction-close!
  [context status]
  (let [closed
        (swap!
         context
         (fn [state]
           (-> state
               (update :completed-phases conj
                       (sh07-proof-transaction-phase-summary state))
               (assoc :open? false
                      :receipts []
                      :cleanup-complete? true))))]
    {:artifact :gravity/sh07-proof-transaction-receipt
     :schema-version 1
     :status status
     :thread-confined? true
     :owner-thread-id (:owner-thread-id closed)
     :phase-order (mapv :phase (:completed-phases closed))
     :phases (:completed-phases closed)
     :source-snapshot (:source-snapshot closed)
     :checked-core-revision (:checked-core-revision closed)
     :check-catalog-bindings
     (into (sorted-map)
           (map (fn [[key catalog]]
                  [key (:check-catalog-hash catalog)]))
           (:check-catalogs closed))
     :maximum-receipts (:maximum-receipts closed)
     :artifact-id (:audited-artifact-id closed)
     :verification-report-id (:verification-report-id closed)
     :construction-receipts-cleared?
     (:construction-receipts-cleared? closed)
     :final-snapshot-rechecked? (:final-snapshot-rechecked? closed)
     :cross-epoch-reuse-count (:cross-epoch-reuse-count closed)
     :cross-epoch-reuse? (pos? (:cross-epoch-reuse-count closed))
     :failed-report-executions (:failed-report-executions closed)
     :failed-report-reuse-count (:failed-report-reuse-count closed)
     :failed-report-reuse? (pos? (:failed-report-reuse-count closed))
     :cleanup-complete? (:cleanup-complete? closed)
     :retained-receipt-count (count (:receipts closed))}))