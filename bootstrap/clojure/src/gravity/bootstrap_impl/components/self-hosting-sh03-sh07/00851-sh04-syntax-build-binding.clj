(declare sh03-reader-authenticated-envelope-options
         sh04-syntax-selection!)

(defn-
 sh04-syntax-source-mode
 [request-source invocation]
 (if
  (and
   (map? invocation)
   (contains? invocation :syntax-source-selection))
  (do
   (sh04-syntax-selection! request-source invocation)
   :development-current)
  :pinned))

(defn-
 sh04-syntax-selection-path
 [invocation]
 (get-in invocation [:syntax-source-selection :source-path]))

(defn
 sh04-syntax-build-binding!
 ([request-source]
  (gravity.syntax/build-plan-binding!
   request-source
   (syntax-stage-runtime-operations)))
 ([request-source invocation]
  (sh03-reader-authenticated-envelope-options request-source invocation)
  (let [source-mode (sh04-syntax-source-mode request-source invocation)]
   (gravity.syntax/build-plan-binding!
    request-source
    (syntax-stage-runtime-operations)
    source-mode
    (sh04-syntax-selection-path invocation)))))

(def ^:private sh04-syntax-cached-binding
  (gravity.syntax/plan-binding-cache (syntax-stage-runtime-operations)))

(defn
 sh04-syntax-current-binding!
 ([request-source]
  (gravity.syntax/current-plan-binding!
   request-source
   sh04-syntax-cached-binding
   (syntax-stage-runtime-operations)))
 ([request-source invocation]
  (sh03-reader-authenticated-envelope-options request-source invocation)
  (let [source-mode (sh04-syntax-source-mode request-source invocation)]
   (gravity.syntax/current-plan-binding!
    request-source
    sh04-syntax-cached-binding
    (syntax-stage-runtime-operations)
    source-mode
    (sh04-syntax-selection-path invocation)))))
(defn
 sh04-syntax-execute!
 [source-path binding function arguments]
 (gravity.syntax/execute-plan!
  source-path
  binding
  function
  arguments
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-require-carrier!
 [source-path carrier value]
 (gravity.syntax/require-result-carrier!
  source-path
  carrier
  value
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-raise-result!
 [source-path result]
 (gravity.syntax/accept-result!
  source-path
  result
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-object-template!
 [source-path syntax-template resolved-digests]
 (gravity.syntax/resolve-object-template!
  source-path
  syntax-template
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-stream-template!
 [source-path stream-template resolved-digests]
 (gravity.syntax/resolve-stream-template!
  source-path
  stream-template
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-request-preimage!
 [source-path request resolved-digests]
 (gravity.syntax/resolve-request-preimage!
  source-path
  request
  resolved-digests
  (syntax-stage-runtime-operations)))
(defn
 sh04-syntax-resolve-stream-request-preimage!
 [source-path request resolved-digests]
 (gravity.syntax/resolve-stream-request-preimage!
  source-path
  request
  resolved-digests
  (syntax-stage-runtime-operations)))
