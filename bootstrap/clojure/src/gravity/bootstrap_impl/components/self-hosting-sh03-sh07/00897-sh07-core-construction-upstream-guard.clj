;; A construction-local proof fact. No caller can supply this through a public
;; API; no report survives the invocation or crosses an independent audit.
(defn- sh07-core-construction-binding-view [binding]
  (select-keys binding [:artifact :status :source-path :source-byte-count
                       :source-content-hash :plan-semantic-hash :functions-semantic-hash
                       :function-count :function-names-hash :function-shapes-hash
                       :public-function-hashes :public-function-shapes :input-key :plan]))

(declare ^:private sh07-core-build-verified-artifact ^:private sh07-core-artifact-base)

(defn- sh07-core-construction-fail! [source-path reason]
  (throw (ex-info "SH-07 construction dependencies changed"
                  {:id "C6-VERIFY" :stage :core-lowering :source-path source-path
                   :reason reason})))

(defn- sh07-core-construction-carrier! [artifact]
  (let [aggregate (p15-s23-trusted-carrier-validation artifact :default-only)]
    (when-not (= :passed (:status aggregate))
      (sh07-core-construction-fail! "<sh07-core-input>" :trusted-artifact-carrier))
    ;; The aggregate trusted-carrier guard dominates every component lookup.
    (let [components (into (sorted-map)
                      (map (fn [[component path]]
                             [component (p15-s23-trusted-carrier-validation
                                         (get-in artifact path) :default-only)]))
                      sh06-resolution-component-paths)]
      (when-not (every? #(= :passed (:status %)) (vals components))
        (sh07-core-construction-fail! "<sh07-core-input>" :trusted-semantic-components))
      {:aggregate aggregate :components components})))

(defn- sh07-core-construction-envelope-source
  ([source-path] (sh07-core-construction-envelope-source nil source-path nil))
  ([artifact source-path invocation]
  (if invocation
    ;; The public owner operation freshly snapshots and executes the selected
    ;; source. Its source rule is a dependency bookend, never ingress authority.
    (:source-rule
     (p15-s23-stage2-sh02-descriptor-envelope
      sh06-resolution-envelope-stage sh06-resolution-sealed-artifact-kind
      (get-in artifact [:gravity-resolution-boundary :authenticated-envelope-descriptor])
      source-path (sh03-reader-authenticated-envelope-options source-path invocation)))
  ;; Reuse the owner's component-safe, bounded, pinned source reader. This
  ;; obtains no envelope authority and never exposes its lexical token.
  (let [anchor (.normalize (.toAbsolutePath (.toPath (java.io.File. (p15-s23-c11-mir-resolve-source-path)))))
        relative (java.nio.file.Paths/get p15-s23-c11-mir-source-relative-path (make-array String 0))
        root (loop [p anchor n (.getNameCount relative)]
               (if (zero? n) p (recur (.getParent p) (dec n))))
        location (.normalize (.resolve root p15-s23-sh02-source-relative-path))]
    (when-not (.endsWith anchor relative)
      (sh07-core-construction-fail! source-path :envelope-source-location))
    (dissoc (p15-s23-sh02-source-snapshot! source-path root location) :source-bytes)))))

(defn- sh07-core-construction-dependencies
  ([source-path] (sh07-core-construction-dependencies nil source-path nil))
  ([artifact source-path invocation]
  {:core (sh07-core-current-source-key)
   :resolver (sh06-resolution-current-source-key)
   :macro (select-keys (sh05-macro-read-pinned-source! source-path)
                       [:source-path :source-byte-count :source-content-hash])
   :syntax (select-keys (sh04-syntax-read-pinned-source! source-path)
                        [:source-path :source-byte-count :source-content-hash])
   :envelope (sh07-core-construction-envelope-source artifact source-path invocation)}))

(defn- sh07-core-construction-bindings [source-path]
  (let [resolution (sh06-resolution-current-binding! source-path)]
    (sh06-resolution-binding-current! source-path resolution)
    {:resolver (sh07-core-construction-binding-view resolution)
     :macro (sh07-core-construction-binding-view (sh05-macro-current-binding! source-path))
     :syntax (sh07-core-construction-binding-view (sh04-syntax-current-binding! source-path))
     :core (sh07-core-construction-binding-view @sh07-core-cached-binding)}))

(defn-
 sh07-core-construction-roots
 []
 (into
  (mapv
   ;; Track operation roots, including under the builder's local plan binding.
   (fn [v] [v (.getRawRoot ^clojure.lang.Var v)])
   (concat
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
 #'*sh07-core-execution-binding*
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
 #'sh07-core-governing-document]
    (filter var? (vals checked-core-stage-defaults))))
  (mapv
   (fn
    [name]
    (let [v (ns-resolve 'gravity.bootstrap name)] [v (var-get v)]))
   '[sh06-resolution-artifact-verification
     sh06-resolution-artifact-verification-contained
     sh06-resolution-artifact-verification*
     sh06-resolution-artifact-verification-bounded*
     sh05-macro-artifact-verification
     sh05-macro-artifact-verification*
     sh07-proof-transaction-report
     c3-syntax-capability-proof
     c3-syntax-stream-reader-products-authentic?
     c3-current-reader-revision-authentic?
     sh04-syntax-reader-binding
     p15-s23-stage2-sh02-descriptor-envelope-verify!
     p15-s23-stage2-sh02-descriptor-envelope
     sh03-reader-authenticated-envelope-options
     p15-s23-sh02-source-snapshot!
     sh07-core-current-source-key
     sh06-resolution-current-source-key
     sh05-macro-read-pinned-source!
     sh04-syntax-read-pinned-source!
     sh07-core-current-binding!
     sh06-resolution-current-binding!
     sh06-resolution-binding-current!
     sh05-macro-current-binding!
     sh04-syntax-current-binding!
     sh07-core-construction-dependencies
     sh07-core-construction-bindings
     sh07-core-construction-envelope-source
     sh07-core-construction-carrier!
     sh07-core-construction-reader
     sh07-core-construction-catalog
     sh07-core-construction-envelope!
     sh07-core-construction-check!
     sh07-core-run-with-construction
     sh07-core-build-verified-artifact
     sh07-core-request-from-verified-upstream
     sh07-core-run-structural-request-for-test
     sh07-core-execute!
     sh07-core-verification-checks
     sh07-core-proof-from-checks])))

(defn- sh07-core-construction-phase []
  (when-let [context (sh07-proof-transaction-context!)]
    {:context context
     :phase (select-keys @context [:owner-thread-id :open? :phase :epoch])}))

(defn- sh07-core-construction-catalog [report]
  (let [checks (set (keys (:checks report)))]
    {:check-catalog checks
     :check-catalog-hash
     (reader-canonical-hash {:schema-version 1 :domain :gravity/sh06-final-verification-checks
                            :checks (vec (sort checks))})}))

(defn- sh07-core-construction-reader
  ([artifact] (sh07-core-construction-reader artifact nil))
  ([artifact invocation]
  ;; The existing C3 predicate authenticates this retained selected revision
  ;; and binding, not a newly loaded default reader or the target source.
  (let [c3 (get-in artifact [:sh05-macro-artifact :gravity-macro-boundary :authenticated-sh04-artifact])]
    (when-not (apply c3-current-reader-revision-authentic?
                    (cond-> [(:c2-reader-artifact c3) (:gravity-syntax-boundary c3)]
                      invocation (conj invocation)))
      (sh07-core-construction-fail! (sh07-core-source-path-from-resolution artifact)
                                   :construction-selected-reader-drift))
    {:revision (get-in c3 [:c2-reader-artifact :sh03-reader-authentication :reader-source-revision])
     :boundary (select-keys (:gravity-syntax-boundary c3)
                            [:reader-source-revision :reader-semantic-binding])})))

(defn- sh07-core-construction-envelope!
  ([artifact source-path] (sh07-core-construction-envelope! artifact source-path nil))
  ([artifact source-path invocation]
  ;; Preserve the authority owner's operation as well as its source bookend.
  ;; The descriptor was authenticated by this invocation's full SH-06 report.
  (let [boundary (:gravity-resolution-boundary artifact)
        options (when invocation (sh03-reader-authenticated-envelope-options source-path invocation))]
    (apply p15-s23-stage2-sh02-descriptor-envelope-verify!
     (cond-> [(:authenticated-envelope boundary) sh06-resolution-envelope-stage sh06-resolution-sealed-artifact-kind
              (:authenticated-envelope-descriptor boundary) source-path]
       options (conj options))))))

(defn- sh07-core-construction-check! [facts]
  (let [{:keys [artifact report source-path dependencies bindings roots phase carrier reader catalog thread invocation]} facts]
    (when-not (and (= thread (.getId (Thread/currentThread)))
                   (= phase (sh07-core-construction-phase))
                   (every? (fn [[v root]] (identical? root (.getRawRoot ^clojure.lang.Var v))) roots)
                   (= catalog (sh07-core-construction-catalog report))
                   (= carrier (sh07-core-construction-carrier! artifact))
                   (= reader (sh07-core-construction-reader artifact invocation))
                   (= dependencies (sh07-core-construction-dependencies artifact source-path invocation))
                   (= bindings (sh07-core-construction-bindings source-path)))
      (sh07-core-construction-fail! source-path :construction-dependency-drift))
    (when-let [context (:context phase)]
      (when-not (= catalog
                   (get-in @context [:check-catalogs [:sh06 :final]]))
        (sh07-core-construction-fail! source-path :construction-check-catalog-drift)))
    (sh07-core-construction-envelope! artifact source-path invocation)))

(defn- sh07-core-with-fresh-construction
  ([artifact build] (sh07-core-with-fresh-construction artifact build nil))
  ([artifact build invocation]
  (try
   (let [carrier (sh07-core-construction-carrier! artifact)
        source-path (sh07-core-source-path-from-resolution artifact)
        _ (when invocation (sh03-reader-authenticated-envelope-options source-path invocation))
        dependencies (sh07-core-construction-dependencies artifact source-path invocation)
        bindings (sh07-core-construction-bindings source-path)
        roots (sh07-core-construction-roots)
        phase (sh07-core-construction-phase)
        report (if invocation
                 (sh06-resolution-artifact-verification artifact ::legacy invocation)
                 (sh06-resolution-artifact-verification artifact))]
    (if-not (= :passed (:status report))
      ;; The caller keeps its existing failed-upstream diagnostic boundary.
      (build {:artifact artifact :report report :invocation invocation})
      (let [facts {:artifact artifact :report report :source-path source-path
                   :dependencies dependencies :roots roots :phase phase :carrier carrier
                   :reader (sh07-core-construction-reader artifact invocation)
                   :invocation invocation
                   :bindings bindings
                   :catalog (sh07-core-construction-catalog report)
                   :thread (.getId (Thread/currentThread))}
            _ (sh07-core-construction-check! facts)
            request (sh07-core-request-from-verified-upstream artifact report)
            facts (assoc facts :request request)
            result (build facts)]
        (sh07-core-construction-check! facts)
        result)))
   (catch InterruptedException interrupted
     (.interrupt (Thread/currentThread))
     (throw interrupted)))))

(defn- sh07-core-run-with-construction [artifact request facts]
  (when-not (= :passed (get-in facts [:report :status]))
    (sh07-core-request-diagnostic! request
      {:reason :authenticated-sh06-artifact-required
       :failed-checks (get-in facts [:report :failed-checks])}))
  (when-not (and (identical? artifact (:artifact facts)) (= request (:request facts)))
    (sh07-core-request-diagnostic! request {:reason :authenticated-sh06-request-membership-mismatch}))
  (sh07-core-construction-check! facts)
  (sh07-core-run-structural-request-for-test request))
