

(defn- sh07-core-artifact-verification*
  ([artifact] (sh07-core-artifact-verification* artifact nil))
  ([artifact invocation]
  (let [source-path (or (get-in artifact [:provenance :source-path])
                        "<sh07-core-verification>")
        upstream (:sh06-resolution-artifact artifact)
        upstream-verification
        (try
          (if invocation
            (sh06-resolution-artifact-verification upstream ::legacy invocation)
            (sh06-resolution-artifact-verification upstream))
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _
            {:artifact :gravity/sh06-resolution-artifact-verification
             :status :failed :failed-checks [:contained-failure]}))
        expected
        (try
          (sh07-core-from-resolution-artifact upstream invocation)
          (catch InterruptedException interrupted
            (.interrupt (Thread/currentThread))
            (throw interrupted))
          (catch Throwable _ nil))]
    (sh07-core-verification-report
     artifact expected upstream-verification))))

(let
 [canonical-core-operations
  checked-core-stage-defaults
  canonical-core-binding-root
  (.getRawRoot #'*sh07-core-execution-binding*)
  canonical-core-roots
  (into
   {}
   (map (fn [v] [v (.getRawRoot ^clojure.lang.Var v)]))
   (concat
    (filter var? (vals canonical-core-operations))
    [#'gravity.checked-core/build-plan-binding!
 #'gravity.checked-core/current-plan-binding!
 #'gravity.checked-core/execute!
 #'gravity.checked-core/plan-identities
 #'gravity.checked-core/source-key
 #'gravity.checked-core/source-path
 #'sh07-core-build-binding!
 #'sh07-core-current-binding!
 #'sh07-core-current-source-key
 #'sh07-core-execute!
 #'sh07-core-plan-identities
 #'sh07-core-source-path
 #'checked-core-stage-operations
 #'checked-core-stage-defaults
 #'checked-core-stage-input
 #'sh07-core-cached-binding
 #'sh07-core-development-binding
 #'sh07-core-public-function-shapes
 #'sh07-core-source-relative-path
 #'gravity.checked-core/build-form-tree
 #'gravity.checked-core/children
 #'gravity.checked-core/decimal-descriptor
 #'gravity.checked-core/decimal-evidence
 #'gravity.checked-core/decimal-evidence-complete!
 #'gravity.checked-core/macro-trace
 #'gravity.checked-core/neutral-value
 #'gravity.checked-core/parameter-binding-paths
 #'gravity.checked-core/projected-form-id
 #'gravity.checked-core/projected-source-mismatch
 #'gravity.checked-core/projected-syntax-id
 #'gravity.checked-core/root-syntax-id
 #'gravity.checked-core/semantic-macro-trace
 #'gravity.checked-core/semantic-span
 #'gravity.checked-core/source-map-order
 #'gravity.checked-core/source-occurrence-context
 #'gravity.checked-core/source-path-from-resolution
 #'gravity.checked-core/value-kind
 #'sh07-core-build-form-tree
 #'sh07-core-children
 #'sh07-core-decimal-descriptor
 #'sh07-core-decimal-evidence
 #'sh07-core-decimal-evidence-complete!
 #'sh07-core-macro-trace
 #'sh07-core-neutral-value
 #'sh07-core-parameter-binding-paths
 #'sh07-core-projected-form-id
 #'sh07-core-projected-source-mismatch
 #'sh07-core-projected-syntax-id
 #'sh07-core-root-syntax-id
 #'sh07-core-semantic-macro-trace
 #'sh07-core-semantic-span
 #'sh07-core-source-map-order
 #'sh07-core-source-occurrence-context
 #'sh07-core-source-path-from-resolution
 #'sh07-core-value-kind
 #'gravity.checked-core/alias-name
 #'gravity.checked-core/binding-order
 #'gravity.checked-core/fragment-coverage
 #'gravity.checked-core/fragment-manifest
 #'gravity.checked-core/fragment-record
 #'gravity.checked-core/module-assembly-manifest
 #'gravity.checked-core/lineage
 #'gravity.checked-core/lineage-with-semantic-trace
 #'gravity.checked-core/projection-binding-input
 #'sh07-b13-alias-name
 #'sh07-b13-binding-order
 #'sh07-b13-fragment-coverage
 #'sh07-b13-fragment-manifest
 #'sh07-b13-fragment-record
 #'sh07-b13-module-assembly-manifest
 #'sh07-core-lineage
 #'sh07-core-lineage-with-semantic-trace
 #'sh07-core-projection-binding-input
 #'gravity.checked-core/authenticated-request
 #'gravity.checked-core/digest-reference-ordinal
 #'gravity.checked-core/request-from-verified-upstream
 #'gravity.checked-core/resolve-reference!
 #'gravity.checked-core/resolve-reference-vector!
 #'gravity.checked-core/semantic-binding
 #'gravity.checked-core/semantic-projection-id
 #'gravity.checked-core/semantic-resolution
 #'gravity.checked-core/semantic-var-reference
 #'sh07-core-authenticated-request
 #'sh07-core-digest-reference-ordinal
 #'sh07-core-request-from-verified-upstream
 #'sh07-core-resolve-reference!
 #'sh07-core-resolve-reference-vector!
 #'sh07-core-semantic-binding
 #'sh07-core-semantic-projection-id
 #'sh07-core-semantic-resolution
 #'sh07-core-semantic-var-reference
 #'gravity.checked-core/digest-requests
 #'gravity.checked-core/resolve-digest-preimage!
 #'gravity.checked-core/resolve-result
 #'gravity.checked-core/semantic-error-handler
 #'gravity.checked-core/semantic-error-transfer
 #'gravity.checked-core/semantic-identity-preimage
 #'gravity.checked-core/semantic-mutation
 #'gravity.checked-core/semantic-node
 #'sh07-core-digest-requests
 #'sh07-core-resolve-digest-preimage!
 #'sh07-core-resolve-result
 #'sh07-core-semantic-error-handler
 #'sh07-core-semantic-error-transfer
 #'sh07-core-semantic-identity-preimage
 #'sh07-core-semantic-mutation
 #'sh07-core-semantic-node
 #'gravity.checked-core/canonical-artifact
 #'gravity.checked-core/public-decimal-presentation
 #'gravity.checked-core/raise-diagnostic!
 #'gravity.checked-core/request-diagnostic!
 #'sh07-core-canonical-artifact
 #'sh07-core-public-decimal-presentation
 #'sh07-core-raise-diagnostic!
 #'sh07-core-request-diagnostic!
 #'gravity.checked-core/request-preflight!
 #'gravity.checked-core/run-structural-request-for-test
 #'gravity.checked-core/run-request-for-test
 #'sh07-core-request-preflight!
 #'sh07-core-run-structural-request-for-test
 #'sh07-core-run-request-for-test
 #'checked-core-with-execution-binding
 #'gravity.checked-core/error-transfers-coherent?
 #'gravity.checked-core/projection-diagnostic!
 #'sh07-core-error-transfers-coherent?
 #'sh07-core-projection-diagnostic!
 #'gravity.checked-core/descendant-node-ids
 #'gravity.checked-core/handler-group-coherent?
 #'gravity.checked-core/error-handlers-coherent?
 #'sh07-core-descendant-node-ids
 #'sh07-b9-handler-group-coherent?
 #'sh07-core-error-handlers-coherent?
 #'sh07-b9-handler-descriptor-keys
 #'gravity.checked-core/match-branch-tree
 #'gravity.checked-core/match-products-coherent?
 #'gravity.checked-core/match-guard-source
 #'gravity.checked-core/match-guard-valid?
 #'gravity.checked-core/match-pattern-graph-complete?
 #'gravity.checked-core/match-pattern-record-coherent?
 #'gravity.checked-core/match-clause
 #'gravity.checked-core/match-guard-source-valid?
 #'gravity.checked-core/match-group-coherent?
 #'sh07-b11-branch-tree
 #'sh07-core-match-products-coherent?
 #'sh07-b11-guard-source
 #'sh07-b11-guard-valid?
 #'sh07-b11-match-pattern-graph-complete?
 #'sh07-b11-match-pattern-record-coherent?
 #'sh07-b11-clause
 #'sh07-b11-guard-source-valid?
 #'sh07-b11-match-group-coherent?
 #'sh07-b11-match-clause-keys
 #'sh07-b11-match-pattern-record-keys
 #'gravity.checked-core/verification-checks
 #'gravity.checked-core/proof-from-checks
 #'gravity.checked-core/exact-comparison-value
 #'gravity.checked-core/artifact-identity-input
 #'gravity.checked-core/verification-report
 #'sh07-core-verification-checks
 #'sh07-core-proof-from-checks
 #'sh07-core-exact-comparison-value
 #'sh07-core-artifact-identity-input
 #'sh07-core-verification-report
 #'gravity.checked-core/artifact-base
 #'sh07-core-artifact-base
 #'sh07-core-adapter-contract
 #'sh07-core-governing-document]))]
 (defn
  sh07-core-artifact-verification
  ([artifact] (sh07-core-artifact-verification artifact nil))
  ([artifact invocation]
   (when
    invocation
    (sh03-reader-authenticated-envelope-options
     (get-in artifact [:provenance :source-path])
     invocation))
   (let
    [binding @sh07-core-cached-binding]
    (sh07-proof-transaction-report
     :sh07
     :final
     {:verifier-root
      (let
       [entries
        (.iterator (.entrySet ^java.util.Map canonical-core-roots))]
       (if
        (and
         (identical?
          (.getRawRoot #'checked-core-stage-defaults)
          canonical-core-operations)
         (identical?
          (.getRawRoot #'*sh07-core-execution-binding*)
          canonical-core-binding-root)
         (loop
          []
          (if
           (.hasNext entries)
           (let
            [entry
             (.next entries)
             v
             (.getKey ^java.util.Map$Entry entry)]
            (if
             (and
              (identical?
               (.getValue ^java.util.Map$Entry entry)
               (.getRawRoot ^clojure.lang.Var v))
              (nil? (.getThreadBinding ^clojure.lang.Var v)))
             (recur)
             false))
           true)))
        (var-get #'sh07-core-artifact-verification*)
        (Object.))),
      :report-schema-version 1,
      :invocation invocation,
      :check-catalog-domain :gravity/sh07-final-verification-checks,
      :source-content-hash (:source-content-hash binding),
      :plan-semantic-hash (:plan-semantic-hash binding),
      :functions-semantic-hash (:functions-semantic-hash binding),
      :checked-core-operation-binding *sh07-core-execution-binding*}
     artifact
     (fn*
      []
      (sh07-core-artifact-verification* artifact invocation)))))))

(defn sh07-core-capability-based-proof
  ([artifact] (sh07-core-capability-based-proof artifact nil))
  ([artifact invocation]
  (let [report (sh07-core-artifact-verification artifact invocation)]
    (assoc (:checks report)
           :artifact :gravity/sh07-core-capability-proof
           :status (if (= :passed (:status report))
                     :complete :failed)
           :failed-checks (:failed-checks report)))))

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
        (sh07-core-assert-pinned-revision!
        (select-keys
         @sh07-core-cached-binding
         [:source-byte-count :source-content-hash :plan-semantic-hash
          :functions-semantic-hash :function-count :function-names-hash
          :function-shapes-hash :public-function-hashes
          :public-function-shapes]))
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
