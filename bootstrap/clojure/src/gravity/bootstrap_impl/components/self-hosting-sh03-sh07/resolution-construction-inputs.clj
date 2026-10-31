;; One lexical candidate-input seam, not a report cache. The two Macro owner
;; checks in the later construction proof and public verification stay fresh.
(defn- resolution-construction-eligible? []
  (and (some? (sh06-resolution-reader-invocation))
       (nil? (sh07-proof-transaction-context!))))

(declare ^:private resolution-construction-inputs
         ^:private resolution-construction-check!)

(defn- resolution-construction-fail! [source-path reason]
  (sh06-resolution-boundary-fail!
   "C5-UNRESOLVED" source-path reason {} {:reason reason}))

(defn- resolution-construction-binding-view [binding]
  ;; Include the actual plan and input key, not only advertised digest fields.
  (dissoc binding :source-text))

(defn- resolution-construction-envelope-source [source-path]
  ;; Observation only: this obtains no AE authority or private owner token.
  (let [anchor (.normalize (.toAbsolutePath
                           (.toPath (java.io.File. (p15-s23-c11-mir-resolve-source-path)))))
        relative (java.nio.file.Paths/get p15-s23-c11-mir-source-relative-path (make-array String 0))]
    (when-not (.endsWith anchor relative)
      (resolution-construction-fail! source-path :construction-envelope-location))
    (let [root (loop [path anchor n (.getNameCount relative)]
                 (if (zero? n) path (recur (.getParent path) (dec n))))
          location (.normalize (.resolve root p15-s23-sh02-source-relative-path))]
      (select-keys (c2-pass-cache/source-snapshot! (str location))
                   [:canonical-path :byte-count :bytes-hash]))))

(defn- resolution-construction-environment []
  {:thread (.getId (Thread/currentThread))
   :context (sh07-proof-transaction-context!)
   :invocation (sh06-resolution-reader-invocation)
   :operation-binding (resolution-construction-binding-view *sh06-resolution-operation-binding*)})

(defn- resolution-construction-dependencies [source-path invocation]
  (sh03-reader-authenticated-envelope-options source-path invocation)
  (let [dependencies
        {:reader-source (dissoc (sh03-reader-read-selected-source-bytes!
                                source-path (:reader-source-path invocation)) :bytes)
         :reader (resolution-construction-binding-view
                  (:binding (sh03-reader-verification-invocation! source-path invocation)))
         :syntax (resolution-construction-binding-view (sh04-syntax-current-binding! source-path))
         :macro (resolution-construction-binding-view (sh05-macro-current-binding! source-path))
         :resolver (resolution-construction-binding-view (sh06-resolution-current-binding! source-path))
         :resolver-inputs (sh06-resolution-current-source-key)
         :envelope-source (resolution-construction-envelope-source source-path)}]
    (when-not (= :passed (:status (p15-s23-trusted-carrier-validation dependencies :default-only)))
      (resolution-construction-fail! source-path :construction-dependency-carrier))
    dependencies))

(defn- resolution-construction-observe [source-path invocation]
  (try
    (resolution-construction-dependencies source-path invocation)
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    ;; Optional observations may be stricter than the owning reader (for
    ;; example secure snapshot link/permission policy). No new admission gate.
    (catch Exception _ ::unavailable)))

(defn- resolution-construction-roots []
  ;; Explicit short-seam drift boundary. No namespace scan or missing roots;
  ;; later actual owner verification remains the acceptance authority.
  (mapv (fn [v] [v (var-get v)])
        [#'gravity.resolution/boundary-fail! #'gravity.resolution/resolve-source-path
         #'gravity.resolution/read-current-source! #'gravity.resolution/read-pinned-source!
         #'gravity.resolution/plan-identities #'gravity.resolution/build-plan-binding!
         #'gravity.resolution/source-key #'gravity.resolution/binding-current!
         #'gravity.resolution/current-plan-binding! #'gravity.resolution/execute!
         #'resolution-stage-operations #'resolution-stage-defaults #'resolution-stage-input
         #'sh06-authenticated-sh05-parent! #'sh06-resolution-verify-sh05
         #'sh05-macro-artifact-verification #'sh05-macro-artifact-verification*
         #'sh07-proof-transaction-report #'sh07-proof-transaction-context!
         #'sh06-resolution-validate-c3! #'c3-syntax-validate! #'c3-artifact-id
         #'c3-syntax-capability-proof #'c3-syntax-stream-reader-products-authentic?
         #'c3-current-reader-revision-authentic? #'sh05-macro-artifact-id
         #'sh03-reader-authenticated-envelope-options #'sh03-reader-verification-invocation!
         #'sh03-reader-invocation-source! #'sh03-reader-current-binding!
         #'sh03-reader-read-selected-source-bytes! #'sh04-syntax-current-binding!
         #'sh04-syntax-read-pinned-source! #'sh05-macro-current-binding!
         #'sh05-macro-read-pinned-source! #'sh06-resolution-current-binding!
         #'sh06-resolution-binding-current! #'sh06-resolution-current-source-key
         #'sh06-resolution-reader-invocation #'p15-s23-c11-mir-resolve-source-path
         #'c2-pass-cache/source-snapshot! #'p15-s23-stage2-sh02-descriptor-envelope
         #'p15-s23-stage2-sh02-descriptor-envelope-verify!
         #'c-backend-stage2-plan-emitter-source-rule! #'p15-s23-trusted-carrier-validation
         #'reader-canonical-hash #'sh03-reader-canonical-digest #'p15-s23-c6c10-canonical-digest
         #'parse-module #'sh06-resolution-import-records #'sh06-resolution-explicit-candidate-records
         #'sh06-resolution-analysis-inputs #'sh06-resolution-source-revision-id
         #'sh06-resolution-package #'sh06-resolution-definition-records
         #'sh06-resolution-import-binding-records #'sh06-resolution-core-records
         #'sh06-resolution-dependency-edges #'sh06-resolution-envelope-id
         #'sh05-macro-gravity-verifier-report #'sh05-macro-sh02-descriptor
         #'sh05-macro-envelope-summary #'sh05-source-syntax-stream #'sh05-defn-form?
         #'sh05-expanded-syntax-object #'sh05-package-trace #'sh05-macro-trace-id
         #'sh05-expanded-syntax-stream-id #'sh05-expanded-graph-valid? #'sh05-expanded-graph
         #'sh04-syntax-reader-binding #'sh05-macro-version #'sh05-macro-expected-source-byte-count
         #'sh05-macro-artifact-keys #'sh05-macro-envelope-stage #'sh05-macro-sealed-artifact-kind
         #'sh05-macro-source-relative-path #'p15-s23-c11-mir-source-relative-path
         #'p15-s23-sh02-source-relative-path #'sh06-authenticated-source-snapshot!
         #'resolution-base-request-from-authenticated-parent #'resolution-construction-inputs
         #'resolution-construction-eligible? #'resolution-construction-fail!
         #'resolution-construction-binding-view #'resolution-construction-envelope-source
         #'resolution-construction-environment #'resolution-construction-dependencies
         #'resolution-construction-observe #'resolution-construction-roots
         #'resolution-construction-check!]))

(defn- resolution-construction-check!
  [source-path parent environment roots before]
  (when-not (and (every? (fn [[v value]] (identical? value (var-get v))) roots)
                 (= environment (resolution-construction-environment)))
    (resolution-construction-fail! source-path :construction-parent-dependency-drift))
  (when-not (= ::unavailable before)
    (let [after (resolution-construction-observe source-path (:invocation environment))
          ;; This rule is trusted only AFTER the unchanged full parent gate.
          rule (get-in parent [:gravity-macro-boundary :authenticated-envelope :source-rule])
          observed (:envelope-source before)]
      (when-not (and (= before after)
                     (= (:byte-count observed) (:source-byte-count rule))
                     (= (:bytes-hash observed) (:source-content-hash rule))
                     (every? (fn [[v value]] (identical? value (var-get v))) roots)
                     (= environment (resolution-construction-environment)))
        (resolution-construction-fail! source-path :construction-parent-dependency-drift)))))

(defn- resolution-construction-inputs [source-path parent legacy?]
  (when-not (= :passed (:status (p15-s23-trusted-carrier-validation parent :default-only)))
    ;; Never inspect an untrusted carrier while building this diagnostic.
    (resolution-construction-fail! source-path :authenticated-sh05-macro-parent))
  (let [environment (resolution-construction-environment)
        invocation (:invocation environment)
        _ (when-not (and invocation (nil? (:context environment)))
            (resolution-construction-fail! source-path :construction-parent-invocation))
        roots (resolution-construction-roots)
        before (resolution-construction-observe source-path invocation)
        ;; No inherited context is eligible, so this is a real owner execution.
        _ (sh06-authenticated-sh05-parent! source-path parent)
        source-before (sh06-authenticated-source-snapshot!
                       source-path (get-in parent [:gravity-macro-boundary
                                                   :authenticated-sh04-artifact :c2-reader-artifact])
                       :source-authentication-before-c5 legacy?)
        _ (resolution-construction-check! source-path parent environment roots before)
        base (if (= ::unavailable before)
               (sh06-resolution-base-request-data source-path parent {})
               (resolution-base-request-from-authenticated-parent source-path parent {}))]
    (resolution-construction-check! source-path parent environment roots before)
    {:source-before source-before :base base}))
