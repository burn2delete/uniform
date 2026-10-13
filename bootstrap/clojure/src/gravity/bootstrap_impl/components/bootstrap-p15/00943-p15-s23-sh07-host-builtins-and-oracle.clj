;; The load manifest places SH-07 before the late compiler-artifact components.
;; Define the private dynamic seam here so every later component resolves the
;; same Var.  Only the arity-two wrapper in 00945 ever installs a non-nil root.
(def ^:dynamic ^:private *p15-s23-sh07-b51-governed-source-session* nil)

(declare ^:private p15-s23-sh07-b51-governed-source-event!)
(declare ^:private p15-s23-sh07-b51-governed-source-entry)
(declare ^:private p15-s23-sh07-b51-active-module-ordinal)
(declare ^:private p15-s23-sh07-b51-compatibility-source-record)
(declare compiler-c2-reader-source-artifact)
(declare compiler-c3-syntax-source-artifact)
(declare ^:private sh03-reader-internal-product-authority)
(declare ^:private sh05-macro-artifact-from-c3-artifact*)
(declare ^:private sh06-resolution-source-artifact-candidate)
(declare ^:private sh06-resolution-finalize-candidate)
(declare sh07-core-authenticated-request)
(declare sh07-core-execute!)
(declare sh07-core-digest-requests)
(declare sh07-core-plan-identities)
(declare sh07-core-expected-source-byte-count)
(declare sh07-core-expected-source-content-hash)
(declare sh07-core-expected-plan-semantic-hash)
(declare sh07-core-expected-functions-semantic-hash)
(declare sh07-core-expected-function-count)
(declare sh07-core-expected-function-names-hash)
(declare sh07-core-expected-function-shapes-hash)
(declare sh07-core-public-function-hashes)
(declare sh07-core-public-function-shapes)
(declare p15-s23-stage2-compiler-artifact-plan)
(declare p15-s23-stage2-compiler-artifact-binding!)

(def p15-s23-sh07-declared-builtin-exact-arities
  {'sh07-canonical-text-compare 2
   'sh07-declared-digest-hash 1
   'sh07-observe-utf8-byte-count 3
   'sh07-b51-admitted-executable-carrier-validation? 1})

(def p15-s23-sh07-canonical-text-compare-direct-callers
  '#{sh07-b51-producer-insert-decoration
     sh07-b51-r6-decoration-insert
     p15-s23-sh07-canonical-decoration})

(def p15-s23-sh07-declared-digest-hash-direct-callers
  '#{sh07-b51-success-digest-replay-exact?
     sh07-b51-finalize-rejection
     sh07-b51-producer-decimal-admission
     sh07-b51-producer-decimal-projection-authentic?
     sh07-b51-r6-replay-digests
     sh07-b51-r6-decimal-projection-authentic?
     sh07-b51-r6-predecessor-closure-authentic?
     sh07-b51-r6-projection-binding
     sh07-b51-r6-projected-form-id
     sh07-b51-r6-projected-syntax-id
     sh07-b51-r6-project-decimal
     sh07-b51-r6-projected-macro-traces
     sh07-b51-r6-project-lineage
     sh07-b51-r6-replay-consume-scope
     sh07-b51-r6-replay-reference
     sh07-b51-r6-project-forms
     sh07-b51-r6-project-definition-syntax-id
     sh07-b51-r6-project-scope-catalog
     sh07-b51-r6-project-bindings
     sh07-b51-r6-v18-executable-authentic?})

(def p15-s23-sh07-observe-utf8-byte-count-direct-callers
  '#{sh07-b51-producer-observe-decoration-bytes
     sh07-b51-r6-observe-scalar-bytes
     p15-s23-sh07-o-append-scalar-fragment!
     p15-s23-sh07-combine-decorations
     p15-s23-sh07-o-c2-decoration})

(def p15-s23-sh07-admitted-carrier-validation-direct-callers
  '#{sh07-b51-root1-admitted-raw-carrier-shape?
     sh07-b51-r6-c30-request-proof?})

(def p15-s23-sh07-declared-builtin-contracts
  {'sh07-canonical-text-compare
   {:arity 2
    :input [:string :string]
    :result [:enum -1 0 1]
    :effects #{}
    :capabilities #{}
    :order :unsigned-lexicographic-strict-utf8
    :resource-authority :none
    :byte-census-authority :none
    :malformed-unicode-diagnostic "L2-BUILTIN-ERROR"
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :callers p15-s23-sh07-canonical-text-compare-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-declared-digest-hash
   {:arity 1
    :input [:reader-canonical-value]
    :result :sha256-digest-id
    :effects #{}
    :capabilities #{}
    :identity :reader-canonical-hash
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :callers p15-s23-sh07-declared-digest-hash-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-observe-utf8-byte-count
   {:arity 3
    :implementation :p15-s23-sh07-observe-utf8-byte-count-v1
    :input [:string :nonnegative-exact-integer
            :nonnegative-exact-integer]
    :result
    {:artifact :gravity/sh07-utf8-byte-observation-v1
     :keys #{:artifact :status :reason :maximum :observed-before
             :observed-after :prospective-after :utf16-units-scanned
             :unicode-scalars-scanned}
     :status [:enum :accepted :rejected]
     :reasons #{nil :utf8-byte-limit
                :invalid-unicode-scalar-sequence}}
    :effects #{}
    :capabilities #{}
    :maximum 1073741824
    :maximum-prospective-after 1073741828
    :algorithm :strict-unicode-scalar-utf8-prospective-census
    :encoded-allocation :forbidden
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :wrong-input-diagnostic "L2-BUILTIN-ERROR"
    :callers p15-s23-sh07-observe-utf8-byte-count-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}
   'sh07-b51-admitted-executable-carrier-validation?
   {:arity 1
    :implementation :p15-s23-sh07-b51-first-eval-validation-receipt-v3
    :input [:identity-bound-executable-carrier]
    :result [:literal true]
    :effects #{}
    :capabilities #{}
    :fact-projections
    #{:gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v3
      :gravity/sh07-b51-attempt32-r6-nested-carrier-facts-v3}
    :one-shot true
    :wrong-arity-diagnostic "L2-BUILTIN-ARITY"
    :wrong-input-diagnostic "L2-BUILTIN-ERROR"
    :callers p15-s23-sh07-admitted-carrier-validation-direct-callers
    :dynamic-resolution :forbidden
    :public-runtime-call :forbidden}})

(def ^:private p15-s23-sh07-checked-core-source-relative-path
  "bootstrap/gravity/src/gravity/checked_core.gravity")

(def ^:private p15-s23-sh07-compiler-source-authority-v1-keys
  #{:artifact :source-relative-path :source-sha256 :source-byte-count
    :module :function-source-hashes})

(def ^:private p15-s23-sh07-compiler-source-authority-v2-keys
  #{:artifact :manifest-id :module-source-id :source-sha256
    :function-source-hashes :module :logical-source-path})

(def ^:private p15-s23-sh07-instruction-provenance-v1-keys
  #{:artifact :source-relative-path :source-sha256 :function
    :function-source-hash :callee})

(def ^:private p15-s23-sh07-instruction-provenance-v2-keys
  #{:artifact :manifest-id :module-source-id :source-sha256 :function
    :function-source-hash :callee})

(declare p15-s23-stage2-compiler-artifact-source-path)
(declare p15-s23-sh07-b51-module-source-valid?)
(declare p15-s23-sh07-b51-manifest-id)

(defn- p15-s23-sh07-source-definition-hash
  [definition]
  (reader-canonical-hash
   (select-keys definition [:name :arity :params :body])))

(defn- p15-s23-sh07-governed-source-file
  [logical-source-path]
  (when (string? logical-source-path)
    (let [anchor (java.io.File.
                  (p15-s23-stage2-compiler-artifact-source-path))
          start (if (.isDirectory anchor) anchor (.getParentFile anchor))]
      (loop [directory start]
        (when directory
          (let [candidate
                (java.io.File. directory logical-source-path)]
            (if (.isFile candidate)
              (.getCanonicalFile candidate)
              (recur (.getParentFile directory)))))))))

(defn- p15-s23-sh07-source-authority-fail!
  [message source-path]
  (throw (ex-info message
                  {:id "L2-UNKNOWN-CORE-FORM"
                   :reason :unauthenticated-compiler-provenance
                   :source-path source-path})))

(defn- p15-s23-sh07-b51-source-input-authentic?
  [source-path supplied-bytes source-sha256 module descriptor manifest]
  (try
    (let [logical-source-path (:logical-source-path descriptor)]
      (and (= logical-source-path source-path)
           (p15-s23-sh07-b51-module-source-valid? descriptor)
           (map? manifest)
           (= (:manifest-id manifest)
              (p15-s23-sh07-b51-manifest-id manifest))
           (= (:module-source-id descriptor)
              (some #{(:module-source-id descriptor)}
                    (:module-source-ids manifest)))
           (= (:source-sha256 descriptor) source-sha256)
           (= (:source-byte-count descriptor) (alength supplied-bytes))
           (= (:module descriptor) (:module module))
           (= :meta (:profile module))
           (= :jvm (:target module))
           (= #{} (:effects module))
           (= #{} (:capabilities module))
           (= :safe (:safety module))))
    (catch InterruptedException interrupted
      (.interrupt (Thread/currentThread))
      (throw interrupted))
    (catch Throwable _ false)))

(defn- p15-s23-sh07-b51-current-module-source-authority
  [source-path source-text module function-table]
  (let [descriptor (:p15-s23-sh07-module-source-descriptor module)
        manifest (:p15-s23-sh07-link-manifest module)
        supplied-bytes (.getBytes ^String source-text
                                  java.nio.charset.StandardCharsets/UTF_8)
        source-sha256 (str "sha256:" (sha256-bytes-hex supplied-bytes))]
    (when-not
     (and descriptor
          (p15-s23-sh07-b51-source-input-authentic?
           source-path supplied-bytes source-sha256 module descriptor manifest))
      (p15-s23-sh07-source-authority-fail!
       "compiler-internal builtins require authenticated B51 source"
       source-path))
    {:artifact :gravity/sh07-compiler-source-authority-v2
     :manifest-id (:manifest-id manifest)
     :module-source-id (:module-source-id descriptor)
     :logical-source-path (:logical-source-path descriptor)
     :source-sha256 source-sha256
     :module (:module descriptor)
     :function-source-hashes
     (into (sorted-map)
           (map (fn [[name definition]]
                  [name (p15-s23-sh07-source-definition-hash definition)]))
           function-table)}))
