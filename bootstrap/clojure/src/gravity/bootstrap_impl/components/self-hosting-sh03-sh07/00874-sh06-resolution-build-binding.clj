

(defn
 sh06-resolution-build-binding!
 [request-source]
 (gravity.resolution/build-plan-binding!
  request-source
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn-
 sh06-resolution-current-source-key
 []
 (gravity.resolution/source-key
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(def ^:private sh06-resolution-cached-binding (atom nil))
(def ^:private ^:dynamic *sh06-resolution-operation-binding* nil)

(defn-
 sh06-resolution-binding-current!
 [source-path binding]
 (gravity.resolution/binding-current!
  source-path
  binding
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-current-binding!
 [request-source]
 (gravity.resolution/current-plan-binding!
  request-source
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

(defn
 sh06-resolution-execute!
 [source-path binding function arguments]
 (gravity.resolution/execute!
  source-path
  binding
  function
  arguments
  ^{:line 117, :column 65}
  (resolution-stage-operations)))

;; Temporary value for external 00580/00897 two-argument callers. The SH06
;; carrier validator ignores that obsolete argument; this is not a quota.
(def sh06-resolution-artifact-bounds nil)

(defn- sh06-resolution-envelope-source-options [binding]
  (if (= :development-unadmitted (:revision-status binding))
    {:source :development-current}
    {}))

(declare ^:private sh03-reader-authenticated-envelope-options)

(defn- sh06-resolution-reader-invocation []
  (:reader-invocation *sh06-resolution-operation-binding*))

(defn- sh06-resolution-with-reader-invocation [source-path invocation operation]
  (when (some? invocation)
    (sh03-reader-authenticated-envelope-options source-path invocation))
  (let [current (sh06-resolution-current-binding! source-path)]
    (binding [*sh06-resolution-operation-binding* (assoc current :reader-invocation invocation)]
      (operation))))

(defn- sh06-resolution-verify-sh05 [artifact]
  (if-let [invocation (sh06-resolution-reader-invocation)]
    (sh05-macro-artifact-verification artifact invocation)
    (sh05-macro-artifact-verification artifact)))

(defn- sh06-resolution-validate-c3! [source-path artifact]
  (if-let [invocation (sh06-resolution-reader-invocation)]
    (c3-syntax-validate! source-path artifact invocation)
    (c3-syntax-validate! source-path artifact)))
