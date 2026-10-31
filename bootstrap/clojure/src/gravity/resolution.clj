^{:line 87, :column 18}
(ns
 gravity.resolution
 "Gravity resolution source and plan runtime with explicit temporary seed operations.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {match-pattern-binding-paths
    {:arglists ([pattern path operations]),
     :operation-inputs [:sh06-match-pattern-binding-paths]},
    definition-records
    {:arglists ([module sh05-artifact operations]),
     :operation-inputs
     [:sh06-resolution-definition-kind
      :sh06-resolution-package
      :sh06-resolution-semantic-span]},
    candidate-for
    {:arglists ([overrides namespace name]), :operation-inputs []},
    current-plan-binding!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh06-resolution-binding-current!
      :sh06-resolution-boundary-fail!
      :sh06-resolution-build-binding!
      :sh06-resolution-current-source-key]},
    analysis-inputs
    {:arglists ([_module sh05-artifact overrides operations]),
     :operation-inputs
     [:reader-canonical-hash
      :sh06-fixed-vector-pattern?
      :sh06-let-loop-binding-paths
      :sh06-resolution-semantic-span
      :sh06-unique-match-binding-paths]},
    dependency-record
    {:arglists
     ([source-path module dependency overrides ordinal operations]),
     :operation-inputs [:reader-canonical-hash :source-span]},
    execute!
    {:arglists ([source-path binding function arguments operations]),
     :operation-inputs
     [:p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata
      :sh06-resolution-boundary-fail!]},
    binding-current!
    {:arglists ([source-path binding operations]),
     :operation-inputs
     [:sh06-resolution-boundary-fail!
      :sh06-resolution-current-source-key]},
    boundary-fail!
    {:arglists
     ([rule source-path missing-field observed facts operations]),
     :operation-inputs [:c5-resolution-fail! :source-span]},
    request-overrides {:arglists ([module]), :operation-inputs []},
    let-loop-binding-paths
    {:arglists ([pattern path]), :operation-inputs []},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input]},
    read-pinned-source!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh06-resolution-boundary-fail!
      :sh06-resolution-read-current-source!]},
    import-binding-records
    {:arglists ([_module imports overrides operations]),
     :operation-inputs
     [:reader-canonical-hash :sh06-resolution-package]},
    package-record {:arglists ([namespace]), :operation-inputs []},
    source-key
    {:arglists ([operations]),
     :operation-inputs
     [:c-backend-resolve-p15-s23-compiler-source-path
      :p15-s23-stage2-compiler-artifact-source-path
      :sh06-resolution-resolve-source-path
      :sha256-bytes-hex]},
    core-records
    {:arglists ([operations]),
     :operation-inputs [:input :sh06-resolution-core-record]},
    unique-match-binding-paths
    {:arglists ([pattern path operations]),
     :operation-inputs [:sh06-match-pattern-binding-paths]},
    definition-kind {:arglists ([form]), :operation-inputs []},
    source-revision-id
    {:arglists ([sh05-artifact operations]),
     :operation-inputs [:reader-canonical-hash]},
    explicit-candidate-records
    {:arglists ([module sh05-artifact overrides operations]),
     :operation-inputs
     [:reader-canonical-hash :sh06-resolution-package :source-span]},
    resolve-source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path]},
    semantic-span {:arglists ([span ordinal]), :operation-inputs []},
    read-current-source!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:sh06-resolution-boundary-fail!
      :sh06-resolution-resolve-source-path
      :sha256-bytes-hex]},
    dependency-edges
    {:arglists ([source-path module imports overrides operations]),
     :operation-inputs [:source-span]},
    envelope-id
    {:arglists ([sh05-artifact operations]),
     :operation-inputs [:reader-canonical-hash]},
    build-plan-binding!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :c-backend-canonical-value
      :c-backend-stage2-plan-emitter-source-rule!
      :p15-s23-stage2-compiler-artifact-plan
      :sh06-resolution-boundary-fail!
      :sh06-resolution-plan-identities
      :sh06-resolution-read-current-source!
      :sha256-hex]},
    import-records
    {:arglists ([source-path module overrides operations]),
     :operation-inputs [:sh06-resolution-dependency-record]},
    core-record
    {:arglists ([symbol kind ordinal operations]),
     :operation-inputs [:input :reader-canonical-hash]},
    fixed-vector-pattern?
    {:arglists ([pattern operations]),
     :operation-inputs [:sh06-fixed-vector-pattern?]}},
   :operation-inputs
   [:c-backend-canonical-value
    :c-backend-resolve-p15-s23-compiler-source-path
    :c-backend-stage2-plan-emitter-source-rule!
    :c5-resolution-fail!
    :input
    :p15-s23-c11-mir-digest
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :reader-canonical-hash
    :sh04-syntax-strip-host-metadata
    :sh06-fixed-vector-pattern?
    :sh06-let-loop-binding-paths
    :sh06-match-pattern-binding-paths
    :sh06-resolution-binding-current!
    :sh06-resolution-boundary-fail!
    :sh06-resolution-build-binding!
    :sh06-resolution-core-record
    :sh06-resolution-current-source-key
    :sh06-resolution-definition-kind
    :sh06-resolution-dependency-record
    :sh06-resolution-package
    :sh06-resolution-plan-identities
    :sh06-resolution-read-current-source!
    :sh06-resolution-resolve-source-path
    :sh06-resolution-semantic-span
    :sh06-unique-match-binding-paths
    :sha256-bytes-hex
    :sha256-hex
    :source-span],
   :dependency-direction
   {:requires [clojure.core], :forbids [gravity.bootstrap]},
   :seed-lifetime
   [:cache-atom
    :scoped-operation-binding
    :configuration
    :host-operations],
   :self-hosted? false,
   :governing-contracts [D1 D2 D3 D9 L3 C1 C3 C4 C5 C15 TEST2 L2 L7],
   :seed-inputs
   [:*sh06-resolution-operation-binding*
    :known-source-profiles
    :sh06-resolution-all-core-symbols
    :sh06-resolution-cached-binding
    :sh06-resolution-expected-source-byte-count
    :sh06-resolution-expected-source-content-hash
    :sh06-resolution-public-function-shapes
    :sh06-resolution-source-relative-path
    :sh06-resolution-special-symbols
    :sh06-resolution-type-symbols],
   :unsupported
   [:canonical-c5-admission
    :request-construction
    :artifact-assembly
    :verification
    :legacy-c5-engine
    :self-hosting
    :seed-retirement
    :migration
    :release
    :performance
    :pattern-typing
    :guard-legality
    :exhaustiveness
    :ownership-moves
    :match-lowering],
   :artifact-inputs
   [:current-source
    :compiler-and-emitter-snapshots
    :plan-records
    :resolution-requests
    :expanded-syntax
    :module-declarations
    :core-catalog
    :module-dependencies
    :candidate-bindings
    :patterns
    :shadow-policy],
   :namespace gravity.resolution,
   :diagnostic-ownership
   {:stage [:source-plan-boundary :host-containment],
    :catalog :existing-c5-catalog,
    :resolution-semantics :gravity-source},
   :artifact-outputs
   [:source-identities
    :development-plan-binding
    :function-identities
    :resolution-results
    :declaration-records
    :core-binding-records
    :package-records
    :upstream-references
    :dependency-records
    :import-binding-records
    :explicit-declaration-records
    :lexical-scopes
    :references
    :pattern-binding-paths
    :dependency-edges],
   :clojure-seed-boundary? true,
   :contract-boundary
   :gravity-resolution-normalization-and-plan-runtime})

(defn
 boundary-fail!
 [rule source-path missing-field observed facts operations]
 (let
  [c5-resolution-fail!
   (.valAt ^clojure.lang.ILookup operations :c5-resolution-fail!)
   source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)]
  (c5-resolution-fail!
   rule
   source-path
   {:source-span (source-span source-path 0),
    :symbol (:symbol facts),
    :syntax-id (:syntax-id facts),
    :namespace (:namespace facts),
    :profile (:profile facts),
    :target (:target facts),
    :candidate-bindings (:candidate-bindings facts),
    :dependency-edge (:dependency-edge facts)}
   {:severity :error,
    :profile (:profile facts),
    :missing-fields [missing-field],
    :facts (merge {:sh06-boundary :gravity-resolution-plan} facts),
    :observed observed})))

(defn
 resolve-source-path
 [operations]
 (let
  [input
   ^{:line 59, :column 41}
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-stage2-compiler-artifact-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-source-path)
   anchor
   (java.io.File. (p15-s23-stage2-compiler-artifact-source-path))
   start
   (if (.isDirectory anchor) anchor (.getParentFile anchor))]
  (or
   (loop
    [directory start]
    (when
     directory
     (let
      [candidate
       (java.io.File.
        directory
        (input :sh06-resolution-source-relative-path))]
      (if
       (.isFile candidate)
       (.getPath candidate)
       (recur (.getParentFile directory))))))
   (input :sh06-resolution-source-relative-path))))

(defn
 read-current-source!
 [request-source operations]
 (let
  [sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)
   sh06-resolution-resolve-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-resolve-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source-path
   (sh06-resolution-resolve-source-path)
   nio-path
   (.toPath (java.io.File. source-path))
   nofollow
   (into-array
    java.nio.file.LinkOption
    [java.nio.file.LinkOption/NOFOLLOW_LINKS])
   attributes
   (try
    (java.nio.file.Files/readAttributes
     nio-path
     java.nio.file.attribute.BasicFileAttributes
     nofollow)
    (catch
     Exception
     error
     (sh06-resolution-boundary-fail!
      "C5-UNRESOLVED"
      request-source
      :pinned-resolution-source-readable
      source-path
      {:cause-message (.getMessage error)})))
   bytes
   (try
    (java.nio.file.Files/readAllBytes nio-path)
    (catch
     Exception
     error
     (sh06-resolution-boundary-fail!
      "C5-UNRESOLVED"
      request-source
      :pinned-resolution-source-bytes
      source-path
      {:cause-message (.getMessage error)})))
   content-hash
   (str "sha256:" (sha256-bytes-hex bytes))]
  (when-not
   (and
    attributes
    (.isRegularFile attributes)
    (= (.size attributes) (alength bytes)))
   (sh06-resolution-boundary-fail!
    "C5-UNRESOLVED"
    request-source
    :exact-pinned-resolution-source
    {:source-path source-path,
     :source-byte-count (alength bytes),
     :source-content-hash content-hash}
    {}))
  {:source-path source-path,
   :source-text
   (String. bytes java.nio.charset.StandardCharsets/UTF_8),
   :source-byte-count (alength bytes),
   :source-content-hash content-hash}))

(defn
 read-pinned-source!
 [request-source operations]
 (let
  [input
   ^{:line 59, :column 41}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)
   sh06-resolution-read-current-source!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-read-current-source!)
   source
   (sh06-resolution-read-current-source! request-source)]
  (when-not
   (=
    [(input :sh06-resolution-expected-source-byte-count)
     (input :sh06-resolution-expected-source-content-hash)]
    ((juxt :source-byte-count :source-content-hash) source))
   (sh06-resolution-boundary-fail!
    "C5-UNRESOLVED"
    request-source
    :exact-pinned-resolution-source
    (dissoc source :source-text)
    {}))
  source))

(defn
 plan-identities
 [plan operations]
 (let
  [input
   ^{:line 59, :column 41}
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-c11-mir-digest
   (.valAt ^clojure.lang.ILookup operations :p15-s23-c11-mir-digest)
   p15-s23-stage2-compiler-artifact-semantic-input
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-semantic-input)
   functions
   (:functions plan)
   shapes
   (into
    (sorted-map)
    (map
     (fn
      [[name function]]
      [name (select-keys function [:arity :params])]))
    functions)]
  {:plan-semantic-hash
   (p15-s23-c11-mir-digest
    (p15-s23-stage2-compiler-artifact-semantic-input plan)),
   :functions-semantic-hash (p15-s23-c11-mir-digest functions),
   :function-count (count functions),
   :function-names-hash
   (p15-s23-c11-mir-digest (vec (keys functions))),
   :function-shapes-hash (p15-s23-c11-mir-digest shapes),
   :public-function-hashes
   (into
    (sorted-map)
    (map
     (fn [name] [name (p15-s23-c11-mir-digest (get functions name))]))
    (keys (input :sh06-resolution-public-function-shapes))),
   :public-function-shapes
   (select-keys
    shapes
    (keys (input :sh06-resolution-public-function-shapes)))}))

(defn
 build-plan-binding!
 [request-source operations]
 (let
  [input
   ^{:line 59, :column 41}
   (.valAt ^clojure.lang.ILookup operations :input)
   c-backend-canonical-value
   (.valAt ^clojure.lang.ILookup operations :c-backend-canonical-value)
   c-backend-stage2-plan-emitter-source-rule!
   (.valAt
    ^clojure.lang.ILookup operations
    :c-backend-stage2-plan-emitter-source-rule!)
   p15-s23-stage2-compiler-artifact-plan
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-plan)
   sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)
   sh06-resolution-plan-identities
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-plan-identities)
   sh06-resolution-read-current-source!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-read-current-source!)
   sha256-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-hex)
   source
   (sh06-resolution-read-current-source! request-source)
   emitter
   (:emitter
    (c-backend-stage2-plan-emitter-source-rule!
     (:source-path source)
     :jvm))
   raw-plan
   (p15-s23-stage2-compiler-artifact-plan
    emitter
    (:source-path source)
    (:source-text source))
   identities
   (sh06-resolution-plan-identities raw-plan)]
  (when-not
   (and
    (= :gravity/stage2-compiler-artifact-plan (:kind raw-plan))
    (true? (:compiler-artifact-plan? raw-plan))
    (= 'gravity.resolution (get-in raw-plan [:module :module]))
    (= :meta (get-in raw-plan [:module :profile]))
    (= :jvm (get-in raw-plan [:module :target]))
    (= #{} (get-in raw-plan [:module :effects]))
    (= #{} (get-in raw-plan [:module :capabilities]))
    (= :safe (get-in raw-plan [:module :safety]))
    (=
     (:source-content-hash source)
     (get-in raw-plan [:source :sha256]))
    (=
     (input :sh06-resolution-public-function-shapes)
     (:public-function-shapes identities)))
   (sh06-resolution-boundary-fail!
    "C5-UNRESOLVED"
    request-source
    :pinned-resolution-plan-and-functions
    identities
    {}))
  (let
   [authenticated-plan-base
    (assoc-in
     (dissoc raw-plan :plan-id)
     [:source :byte-count]
     (:source-byte-count source))
    plan
    (assoc
     authenticated-plan-base
     :plan-id
     (str
      "sha256:"
      (sha256-hex
       (pr-str (c-backend-canonical-value authenticated-plan-base)))))]
   (merge
    source
    identities
    {:executed-by :clojure-stage2-generic-rule-runner,
     :revision-status :development-unadmitted,
     :self-hosted? false,
     :status :complete,
     :artifact :gravity/sh06-current-resolution-plan-binding,
     :semantic-authority :gravity-source,
     :plan plan,
     :compiled-by :clojure-stage0-seed,
     :generic-bridge-residual? true}))))

(defn
 source-key
 [operations]
 (let
  [c-backend-resolve-p15-s23-compiler-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :c-backend-resolve-p15-s23-compiler-source-path)
   p15-s23-stage2-compiler-artifact-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-source-path)
   sh06-resolution-resolve-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-resolve-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source
   (sh06-resolution-resolve-source-path)
   compiler
   (java.io.File. (c-backend-resolve-p15-s23-compiler-source-path))
   emitter
   (java.io.File. (p15-s23-stage2-compiler-artifact-source-path))
   snapshot
   (fn
    [file]
    [(.getCanonicalPath ^java.io.File file)
     (str
      "sha256:"
      (sha256-bytes-hex
       (java.nio.file.Files/readAllBytes
        (.toPath ^java.io.File file))))])]
  {:source (snapshot (java.io.File. source)),
   :toolchain [(snapshot compiler) (snapshot emitter)]}))

(defn
 binding-current!
 [source-path binding operations]
 (let
  [sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)
   sh06-resolution-current-source-key
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-current-source-key)]
  (when-not
   (= (:input-key binding) (sh06-resolution-current-source-key))
   (sh06-resolution-boundary-fail!
    "C5-UNRESOLVED"
    source-path
    :resolution-source-or-toolchain-drift
    (dissoc binding :plan :source-text :reader-invocation)
    {}))
  binding))

(defn
 current-plan-binding!
 [request-source operations]
 (let
  [input
   ^{:line 59, :column 41}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh06-resolution-binding-current!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-binding-current!)
   sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)
   sh06-resolution-build-binding!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-build-binding!)
   sh06-resolution-current-source-key
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-current-source-key)]
  (if
   (input :*sh06-resolution-operation-binding*)
   (sh06-resolution-binding-current!
    request-source
    (input :*sh06-resolution-operation-binding*))
   (let [cache-lock (input :sh06-resolution-cached-binding)]
    (locking cache-lock
    (let
     [key
      (sh06-resolution-current-source-key)
      cached
      @(input :sh06-resolution-cached-binding)]
     (when
      (and
       cached
       (not= (:toolchain key) (:toolchain (:input-key cached))))
      (sh06-resolution-boundary-fail!
       "C5-UNRESOLVED"
       request-source
       :resolution-toolchain-drift
       {}
       {}))
     (if
      (= key (:input-key cached))
      cached
      (let
       [binding
        (assoc
         (sh06-resolution-build-binding! request-source)
         :input-key
         key)]
       (when-not
        (= (second (:source key)) (:source-content-hash binding))
        (sh06-resolution-boundary-fail!
         "C5-UNRESOLVED"
         request-source
         :resolution-source-drift
         {}
         {}))
       (sh06-resolution-binding-current! request-source binding)
       (reset! (input :sh06-resolution-cached-binding) binding)
       binding))))))))

(defn
 execute!
 [source-path binding function arguments operations]
 (let
  [p15-s23-stage2-runtime-execute-function
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-runtime-execute-function)
   sh04-syntax-strip-host-metadata
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-strip-host-metadata)
   sh06-resolution-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-boundary-fail!)]
  (try
   (sh04-syntax-strip-host-metadata
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh06-pinned-resolution-runner,
      :compiler-artifact-plan? true}
     (:plan binding)
     function
     (sh04-syntax-strip-host-metadata arguments)))
   (catch
    InterruptedException
    interrupted
    (.interrupt (Thread/currentThread))
    (throw interrupted))
   (catch
    StackOverflowError
    error
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED"
     source-path
     :bounded-resolution-host-stack
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    AssertionError
    error
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED"
     source-path
     :contained-resolution-assertion
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    LinkageError
    error
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED"
     source-path
     :contained-resolution-linkage
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    clojure.lang.ExceptionInfo
    error
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED"
     source-path
     :contained-resolution-runtime-diagnostic
     function
     {:contained-diagnostic (:id (ex-data error))}))
   (catch
    Exception
    error
    (sh06-resolution-boundary-fail!
     "C5-UNRESOLVED"
     source-path
     :contained-resolution-host-failure
     function
     {:contained-host-error (.getName (class error)),
      :cause-message (.getMessage error)})))))


(defn
 semantic-span
 [span ordinal]
 (merge
  {:ordinal ordinal}
  (select-keys
   (or span {})
   [:byte-start
    :byte-end
    :line-start
    :column-start
    :line-end
    :column-end
    :scalar-start
    :scalar-end])))

(defn
 source-revision-id
 [sh05-artifact operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
  (or
   (get-in
    sh05-artifact
    [:gravity-macro-boundary
     :authenticated-sh04-artifact
     :gravity-syntax-boundary
     :reader-source-revision
     :revision-id])
   (get-in
    sh05-artifact
    [:gravity-macro-boundary
     :authenticated-sh04-artifact
     :c2-reader-artifact
     :source-unit-record
     :source-id])
   (reader-canonical-hash
    {:domain :gravity/sh06-upstream-source-revision-v1,
     :syntax-stream-id (:expanded-syntax-stream-id sh05-artifact),
     :macro-trace-id (:macro-expansion-trace-id sh05-artifact)}))))

(defn
 envelope-id
 [sh05-artifact operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
  (or
   (get-in
    sh05-artifact
    [:gravity-macro-boundary
     :authenticated-envelope
     :semantic-envelope-id])
   (reader-canonical-hash
    {:domain :gravity/sh06-upstream-envelope-reference-v1,
     :artifact-id (:artifact-id sh05-artifact),
     :syntax-stream-id (:expanded-syntax-stream-id sh05-artifact),
     :macro-trace-id (:macro-expansion-trace-id sh05-artifact)}))))

(defn
 package-record
 [namespace]
 {:name (symbol (str namespace)), :version "workspace"})

(defn
 definition-kind
 [form]
 (let
  [operator
   (when (seq? form) (first form))
   value
   (when (and (seq? form) (> (count form) 2)) (nth form 2))]
  (case
   operator
   defconst
   :compile-time-constant
   defn
   :function
   defmacro
   :macro
   defschema
   :schema
   defprotocol
   :protocol
   def
   (if (and (seq? value) (= 'fn (first value))) :function :var)
   nil)))

(defn
 definition-records
 [module sh05-artifact operations]
 (let
  [sh06-resolution-definition-kind
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-definition-kind)
   sh06-resolution-package
   (.valAt ^clojure.lang.ILookup operations :sh06-resolution-package)
   sh06-resolution-semantic-span
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-semantic-span)
   exports
   (set (:exports module))
   module-namespace
   (:module module)
   artifact-id
   (:artifact-id sh05-artifact)]
  (->>
   (:expanded-syntax-stream sh05-artifact)
   (map-indexed
    (fn
     [ordinal syntax]
     (let
      [form
       (:form syntax)
       kind
       (sh06-resolution-definition-kind form)
       declared-name
       (when kind (second form))]
      (when
       (and kind (symbol? declared-name))
       (let
        [simple-name
         (clojure.core/symbol (name declared-name))
         declared-namespace
         (some-> (namespace declared-name) clojure.core/symbol)
         binding-namespace
         (or declared-namespace module-namespace)]
        {:capabilities (vec (sort (:capabilities module))),
         :package (sh06-resolution-package binding-namespace),
         :source-span (:span syntax),
         :name simple-name,
         :target-set [(:target module)],
         :type-ref
         (case
          kind
          :function
          :gravity.type/function
          :macro
          :gravity.syntax/macro
          :schema
          :gravity.type/schema
          :protocol
          :gravity.type/protocol
          :gravity.type/value),
         :definition-artifact-id artifact-id,
         :effects (vec (sort (:effects module))),
         :safety (:safety module),
         :kind kind,
         :semantic-span
         (sh06-resolution-semantic-span (:span syntax) ordinal),
         :definition-syntax-id (:syntax/id syntax),
         :profile-set [(:profile module)],
         :namespace binding-namespace,
         :visibility
         (if
          (or
           (contains? exports declared-name)
           (contains? exports simple-name))
          :public
          :private),
         :binding-class :namespace})))))
   (remove nil?)
   vec)))

(defn
 core-record
 [symbol kind ordinal operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
  {:capabilities (if (= symbol 'println) [:io/stdout] []),
   :package {:name 'gravity/core, :version "bootstrap-contract-v1"},
   :source-span {:source "gravity.core", :form-index ordinal},
   :name symbol,
   :target-set [:all],
   :type-ref
   (case
    kind
    :special-form
    :gravity.syntax/special-form
    :type
    :gravity.type/type
    :gravity.type/core-var),
   :definition-artifact-id
   (reader-canonical-hash
    {:domain :gravity/sh06-core-catalog-v1, :catalog-version 1}),
   :effects (if (= symbol 'println) [:io/write] []),
   :safety :safe,
   :kind kind,
   :semantic-span {:catalog :gravity/core, :ordinal ordinal},
   :definition-syntax-id
   (reader-canonical-hash
    {:domain :gravity/sh06-core-binding-syntax-v1,
     :name symbol,
     :kind kind}),
   :profile-set (vec (sort (input :known-source-profiles))),
   :namespace 'gravity.core,
   :visibility :public,
   :binding-class :core}))

(defn
 core-records
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   sh06-resolution-core-record
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-core-record)]
  (mapv
   (fn
    [ordinal symbol]
    (sh06-resolution-core-record
     symbol
     (cond
      (contains? (input :sh06-resolution-special-symbols) symbol)
      :special-form
      (contains? (input :sh06-resolution-type-symbols) symbol)
      :type
      :else
      :var)
     ordinal))
   (range)
   (sort (input :sh06-resolution-all-core-symbols)))))

(defn
 request-overrides
 [module]
 (let
  [candidate (get-in module [:metadata :compiler :sh06-request])]
  (if (map? candidate) candidate {})))

(defn
 candidate-for
 [overrides namespace name]
 (first
  (filter
   (fn*
    [p1__152#]
    (and
     (= namespace (:namespace p1__152#))
     (= name (:name p1__152#))))
   (:candidate-bindings overrides))))


(defn
 dependency-record
 [source-path module dependency overrides ordinal operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)
   namespace
   (:module dependency)
   alias
   (:alias dependency)
   referred
   (vec (:refer dependency))
   candidate
   (or
    (first
     (filter
      (fn* [p1__239#] (= namespace (:namespace p1__239#)))
      (:candidate-bindings overrides)))
    {})
   declared-target
   (or (:target candidate) (first (:target-set candidate)))
   targets
   (if declared-target [declared-target] [(:target module)])
   foreign?
   (= :import (:kind dependency))
   semantic-span
   {:dependency-ordinal ordinal, :namespace namespace, :alias alias}]
  {:capabilities
   (vec
    (sort (or (:capabilities candidate) (:capabilities dependency)))),
   :source-span (source-span source-path 0),
   :foreign-record-complete?
   (if
    foreign?
    (and
     (symbol? namespace)
     (symbol? alias)
     (some? (:boundary dependency)))
    true),
   :boundary
   (or
    (:boundary dependency)
    (when (= :core (:profile dependency)) :pure-core-api)),
   :targets targets,
   :alias alias,
   :effects (vec (sort (:effects dependency))),
   :safety (if foreign? :boundary-checked :safe),
   :kind (if foreign? :foreign :namespace),
   :semantic-span semantic-span,
   :namespace namespace,
   :visibility
   (or (:visibility candidate) (:visibility dependency) :public),
   :profile (or (:profile dependency) (:profile module)),
   :dependency-artifact-id
   (reader-canonical-hash
    {:domain :gravity/sh06-dependency-artifact-v1,
     :namespace namespace,
     :profile (:profile dependency),
     :targets targets,
     :boundary (:boundary dependency)}),
   :refer referred}))

(defn
 import-records
 [source-path module overrides operations]
 (let
  [sh06-resolution-dependency-record
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-resolution-dependency-record)]
  (mapv
   (fn
    [ordinal dependency]
    (sh06-resolution-dependency-record
     source-path
     module
     dependency
     overrides
     ordinal))
   (range)
   (concat (:requires module) (:imports module)))))

(defn
 import-binding-records
 [_module imports overrides operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh06-resolution-package
   (.valAt ^clojure.lang.ILookup operations :sh06-resolution-package)]
  (vec
   (mapcat
    (fn
     [ordinal dependency]
     (let
      [explicit
       (filter
        (fn*
         [p1__240#]
         (= (:namespace dependency) (:namespace p1__240#)))
        (:candidate-bindings overrides))
       names
       (distinct (concat (:refer dependency) (map :name explicit)))
       candidates-by-name
       (group-by :name explicit)]
      (map-indexed
       (fn
        [name-ordinal name]
        (let
         [candidate (first (get candidates-by-name name))]
         {:capabilities
          (or
           (some-> candidate :capabilities vec)
           (:capabilities dependency)),
          :package (sh06-resolution-package (:namespace dependency)),
          :source-span (:source-span dependency),
          :name name,
          :target-set
          (or
           (some-> candidate :target-set vec)
           (:targets dependency)),
          :type-ref
          (if
           (= :foreign (:kind dependency))
           :gravity.interop/foreign-value
           :gravity.type/imported-var),
          :definition-artifact-id (:dependency-artifact-id dependency),
          :effects (:effects dependency),
          :safety (:safety dependency),
          :kind (if (= :foreign (:kind dependency)) :foreign :var),
          :semantic-span
          {:dependency-ordinal ordinal,
           :binding-ordinal name-ordinal,
           :namespace (:namespace dependency),
           :name name},
          :definition-syntax-id
          (reader-canonical-hash
           {:domain :gravity/sh06-import-binding-syntax-v1,
            :dependency-id (:dependency-artifact-id dependency),
            :name name}),
          :profile-set [(:profile dependency)],
          :namespace (:namespace dependency),
          :visibility
          (or
           (:visibility candidate)
           (:visibility dependency)
           :public),
          :binding-class :import}))
       names)))
    (range)
    imports))))

(defn
 explicit-candidate-records
 [module sh05-artifact overrides operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh06-resolution-package
   (.valAt ^clojure.lang.ILookup operations :sh06-resolution-package)
   source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)
   existing-import-names
   (set (mapcat :refer (concat (:requires module) (:imports module))))]
  (->>
   (:candidate-bindings overrides)
   (remove
    (fn*
     [p1__241#]
     (contains? existing-import-names (:name p1__241#))))
   (map-indexed
    (fn
     [ordinal candidate]
     {:capabilities (vec (or (:capabilities candidate) [])),
      :package
      (sh06-resolution-package
       (or (:namespace candidate) (:module module))),
      :source-span (source-span (:source-path module) 0),
      :name (:name candidate),
      :target-set
      (vec (or (:target-set candidate) [(:target module)])),
      :type-ref :gravity.type/value,
      :definition-artifact-id
      (reader-canonical-hash
       {:domain :gravity/sh06-explicit-candidate-artifact-v1,
        :owner-artifact-id (:artifact-id sh05-artifact),
        :ordinal ordinal,
        :candidate candidate}),
      :effects (vec (or (:effects candidate) [])),
      :safety :safe,
      :kind (or (:kind candidate) :var),
      :semantic-span
      {:explicit-candidate ordinal,
       :name (:name candidate),
       :namespace (:namespace candidate)},
      :definition-syntax-id
      (reader-canonical-hash
       {:domain :gravity/sh06-explicit-candidate-v1,
        :ordinal ordinal,
        :candidate candidate}),
      :profile-set [(or (:profile candidate) (:profile module))],
      :namespace (or (:namespace candidate) (:module module)),
      :visibility (or (:visibility candidate) :public),
      :binding-class :namespace}))
   vec)))


(defn
 match-pattern-binding-paths
 [pattern path operations]
 (let
  [sh06-match-pattern-binding-paths
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-match-pattern-binding-paths)]
  (cond
   (and (symbol? pattern) (not= '_ pattern) (not= '& pattern))
   [{:name pattern, :path path}]
   (vector? pattern)
   (vec
    (mapcat
     (fn
      [index item]
      (sh06-match-pattern-binding-paths item (conj path index)))
     (range)
     pattern))
   (map? pattern)
   (vec
    (mapcat
     (fn
      [index [key child]]
      (if
       (= :when key)
       []
       (sh06-match-pattern-binding-paths
        child
        (conj path (inc (* 2 index))))))
     (range)
     pattern))
   :else
   [])))

(defn
 fixed-vector-pattern?
 [pattern operations]
 (let
  [sh06-fixed-vector-pattern?
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-fixed-vector-pattern?)]
  (cond
   (vector? pattern)
   (every? sh06-fixed-vector-pattern? pattern)
   (map? pattern)
   (and
    (not (contains? pattern :when))
    (every? keyword? (keys pattern))
    (every? sh06-fixed-vector-pattern? (vals pattern)))
   (symbol? pattern)
   (not= '& pattern)
   :else
   (or
    (nil? pattern)
    (true? pattern)
    (false? pattern)
    (number? pattern)
    (char? pattern)
    (string? pattern)
    (keyword? pattern)
    (and
     (seq? pattern)
     (= 2 (count pattern))
     (= 'quote (first pattern))
     (symbol? (second pattern)))))))

(defn
 unique-match-binding-paths
 [pattern path operations]
 (let
  [sh06-match-pattern-binding-paths
   (.valAt
    ^clojure.lang.ILookup operations
    :sh06-match-pattern-binding-paths)
   binding-paths
   (sh06-match-pattern-binding-paths pattern path)
   names
   (mapv :name binding-paths)]
  (when (= (count names) (count (set names))) binding-paths)))

(defn
 let-loop-binding-paths
 [pattern path]
 (if
  (vector? pattern)
  (:bindings
   (letfn
    [(walk
      [value current-path state]
      (cond
       (vector? value)
       (reduce-kv
        (fn
         [next-state index item]
         (walk item (conj current-path index) next-state))
        state
        value)
       (and
        (symbol? value)
        (not= '_ value)
        (not= '& value)
        (not (contains? (:seen state) value)))
       (->
        state
        (update :seen conj value)
        (update :bindings conj {:name value, :path current-path}))
       :else
       state))]
    (walk pattern path {:seen #{}, :bindings []})))
  (if (symbol? pattern) [{:name pattern, :path path}] [])))

(defn
 dependency-edges
 [source-path module imports overrides operations]
 (let
  [source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)
   base
   (mapv
    (fn
     [ordinal dependency]
     {:from (:module module),
      :to (:namespace dependency),
      :kind (:kind dependency),
      :boundary (:boundary dependency),
      :semantic-span {:dependency-edge ordinal},
      :source-span (:source-span dependency)})
    (range)
    imports)
   explicit
   (get-in overrides [:module-graph :edges])]
  (vec
   (distinct
    (concat
     base
     (map-indexed
      (fn
       [ordinal edge]
       (let
        [[from to] (when (vector? edge) edge)]
        {:from (or (:from edge) from),
         :to (or (:to edge) to),
         :kind (or (:kind edge) :namespace),
         :boundary (:boundary edge),
         :semantic-span {:explicit-edge ordinal},
         :source-span (source-span source-path 0)}))
      explicit))))))

(let
 [semantic-late-sh06-resolution-analysis-inputs-parameter-names
  (fn
   [parameters]
   (loop
    [remaining
     (seq (if (sequential? parameters) parameters []))
     names
     []]
    (if
     (empty? remaining)
     names
     (let
      [item (first remaining)]
      (cond
       (= item ':-)
       (recur (nnext remaining) names)
       (= item '&)
       (recur (next remaining) names)
       (symbol? item)
       (recur (next remaining) (conj names item))
       :else
       (recur (next remaining) names))))))
  semantic-late-sh06-resolution-analysis-inputs-make-traversal
  (fn
   [parameter-names add-reference! add-scope! operations]
   (let
    [sh06-fixed-vector-pattern?
     (.valAt
      ^clojure.lang.ILookup operations
      :sh06-fixed-vector-pattern?)
     sh06-let-loop-binding-paths
     (.valAt
      ^clojure.lang.ILookup operations
      :sh06-let-loop-binding-paths)
     sh06-unique-match-binding-paths
     (.valAt
      ^clojure.lang.ILookup operations
      :sh06-unique-match-binding-paths)]
    (clojure.core/letfn
     [(walk-types
       [parameters scope-chain syntax]
       (loop
        [remaining (seq parameters)]
        (when
         (seq remaining)
         (if
          (= ':- (first remaining))
          (do
           (let
            [type (second remaining)]
            (if
             (symbol? type)
             (walk-form type scope-chain syntax :type)
             (when
              (and
               (seq? type)
               (= 2 (count type))
               (every? symbol? type))
              (doseq
               [item type]
               (walk-form item scope-chain syntax :type)))))
           (recur (nnext remaining)))
          (recur (next remaining))))))
      (walk-function-body
       [body scope-chain syntax]
       (let
        [annotated? (= ':- (first body))]
        (when
         annotated?
         (let
          [type (second body)]
          (if
           (and (symbol? type) (not= ':- type))
           (walk-form type scope-chain syntax :type)
           (when
            (and (seq? type) (= 2 (count type)) (every? symbol? type))
            (doseq
             [item type]
             (walk-form item scope-chain syntax :type))))))
        (doseq
         [item (if annotated? (drop 2 body) body)]
         (walk-form item scope-chain syntax :expression))))
      (walk-form
       [value scope-chain syntax position]
       (cond
        (symbol? value)
        (add-reference!
         value
         scope-chain
         syntax
         position
         (:span syntax))
        (seq? value)
        (let
         [operator (first value)]
         (cond
          (= operator 'quote)
          (add-reference!
           'quote
           scope-chain
           syntax
           :operator
           (:span syntax))
          (contains? '#{syntax-quote} operator)
          (add-reference!
           operator
           scope-chain
           syntax
           :operator
           (:span syntax))
          (contains? '#{defconst def} operator)
          (do
           (add-reference!
            operator
            scope-chain
            syntax
            :operator
            (:span syntax))
           (doseq
            [item (drop 2 value)]
            (walk-form item scope-chain syntax :expression)))
          (= operator 'defn)
          (let
           [parameters
            (nth value 2 [])
            names
            (parameter-names parameters)
            scope-id
            (add-scope!
             (:syntax/id syntax)
             (first scope-chain)
             names
             (:span syntax))]
           (add-reference!
            'defn
            scope-chain
            syntax
            :operator
            (:span syntax))
           (walk-types parameters (cons scope-id scope-chain) syntax)
           (walk-function-body
            (drop 3 value)
            (cons scope-id scope-chain)
            syntax))
          (= operator 'fn)
          (let
           [named?
            (symbol? (second value))
            parameters
            (if named? (nth value 2 []) (second value))
            body
            (if named? (drop 3 value) (drop 2 value))
            names
            (cond->
             (parameter-names parameters)
             named?
             (conj (second value)))
            scope-id
            (add-scope!
             (:syntax/id syntax)
             (first scope-chain)
             names
             (:span syntax))]
           (add-reference!
            'fn
            scope-chain
            syntax
            :operator
            (:span syntax))
           (walk-types parameters (cons scope-id scope-chain) syntax)
           (walk-function-body
            body
            (cons scope-id scope-chain)
            syntax))
          (= operator 'match)
          (do
           (add-reference!
            'match
            scope-chain
            syntax
            :operator
            (:span syntax))
           (when-let
            [scrutinee (second value)]
            (walk-form scrutinee scope-chain syntax :expression))
           (doseq
            [[pattern branch] (partition 2 (drop 2 value))]
            (cond
             (and (symbol? pattern) (not= '_ pattern))
             (let
              [scope-id
               (add-scope!
                (:syntax/id syntax)
                (first scope-chain)
                [pattern]
                (:span syntax))]
              (walk-form
               branch
               (cons scope-id scope-chain)
               syntax
               :expression))
             (and
              (or (vector? pattern) (map? pattern))
              (sh06-fixed-vector-pattern?
               (if (map? pattern) (dissoc pattern :when) pattern)))
             (when-let
              [binding-paths
               (sh06-unique-match-binding-paths pattern [])]
              (if
               (empty? binding-paths)
               (do
                (when
                 (and (map? pattern) (contains? pattern :when))
                 (walk-form
                  (:when pattern)
                  scope-chain
                  syntax
                  :expression))
                (walk-form branch scope-chain syntax :expression))
               (let
                [scope-id
                 (add-scope!
                  (:syntax/id syntax)
                  (first scope-chain)
                  (mapv :name binding-paths)
                  (:span syntax))]
                (when
                 (and (map? pattern) (contains? pattern :when))
                 (walk-form
                  (:when pattern)
                  (cons scope-id scope-chain)
                  syntax
                  :expression))
                (walk-form
                 branch
                 (cons scope-id scope-chain)
                 syntax
                 :expression))))
             (or
              (= '_ pattern)
              (nil? pattern)
              (true? pattern)
              (false? pattern)
              (number? pattern)
              (char? pattern)
              (string? pattern)
              (keyword? pattern)
              (and
               (seq? pattern)
               (= 2 (count pattern))
               (= 'quote (first pattern))
               (symbol? (second pattern))))
             (walk-form branch scope-chain syntax :expression)
             :else
             nil)))
          (= operator 'try)
          (do
           (add-reference!
            'try
            scope-chain
            syntax
            :operator
            (:span syntax))
           (doseq
            [item (rest value)]
            (cond
             (and (seq? item) (= 'catch (first item)))
             (do
              (add-reference!
               'catch
               scope-chain
               syntax
               :operator
               (:span syntax))
              (when-let
               [error-type (second item)]
               (walk-form error-type scope-chain syntax :type))
              (let
               [binding-name (nth item 2 nil)]
               (when
                (symbol? binding-name)
                (let
                 [scope-id
                  (add-scope!
                   (:syntax/id syntax)
                   (first scope-chain)
                   [binding-name]
                   (:span syntax))
                  nested
                  (cons scope-id scope-chain)]
                 (doseq
                  [handler-form (drop 3 item)]
                  (walk-form
                   handler-form
                   nested
                   syntax
                   :expression))))))
             (and (seq? item) (= 'finally (first item)))
             (do
              (add-reference!
               'finally
               scope-chain
               syntax
               :operator
               (:span syntax))
              (doseq
               [cleanup-form (rest item)]
               (walk-form
                cleanup-form
                scope-chain
                syntax
                :expression)))
             :else
             (walk-form item scope-chain syntax :expression))))
          (contains? '#{let loop} operator)
          (let
           [binding-vector
            (second value)
            pairs
            (if
             (vector? binding-vector)
             (partition 2 binding-vector)
             [])
            _
            (add-reference!
             operator
             scope-chain
             syntax
             :operator
             (:span syntax))
            nested
            (loop
             [remaining pairs active-scope-chain scope-chain]
             (if
              (empty? remaining)
              active-scope-chain
              (let
               [[binding-pattern initializer]
                (first remaining)
                _
                (walk-form
                 initializer
                 active-scope-chain
                 syntax
                 :expression)
                binding-paths
                (sh06-let-loop-binding-paths binding-pattern [])
                next-scope-chain
                (if
                 (or
                  (symbol? binding-pattern)
                  (vector? binding-pattern))
                 (let
                  [scope-id
                   (add-scope!
                    (:syntax/id syntax)
                    (first active-scope-chain)
                    (mapv :name binding-paths)
                    (:span syntax))]
                  (cons scope-id active-scope-chain))
                 active-scope-chain)]
               (recur (next remaining) next-scope-chain))))]
           (doseq
            [item (drop 2 value)]
            (walk-form item nested syntax :expression)))
          :else
          (doseq
           [[index item] (map-indexed vector value)]
           (walk-form
            item
            scope-chain
            syntax
            (if (zero? index) :operator position)))))
        (map? value)
        (doseq
         [[key item] value]
         (walk-form key scope-chain syntax :expression)
         (walk-form item scope-chain syntax :expression))
        (coll? value)
        (doseq
         [item value]
         (walk-form item scope-chain syntax position))
        :else
        nil))]
     walk-form)))]
 (defn
  analysis-inputs
  [_module sh05-artifact overrides operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    sh06-resolution-semantic-span
    (.valAt
     ^clojure.lang.ILookup operations
     :sh06-resolution-semantic-span)
    scopes
    (atom [])
    references
    (atom [])
    next-scope
    (atom 0)
    next-reference
    (atom 0)
    shadow-forbidden
    (set (get-in overrides [:shadow-policy :forbidden]))
    add-reference!
    (fn
     [symbol scope-chain syntax position span]
     (when
      (symbol? symbol)
      (let
       [ordinal
        (swap! next-reference inc)
        namespace-part
        (namespace symbol)
        simple-name
        (clojure.core/symbol (name symbol))]
       (swap!
        references
        conj
        {:syntax-id
         (reader-canonical-hash
          {:domain :gravity/sh06-reference-syntax-v1,
           :owner-syntax-id (:syntax/id syntax),
           :ordinal ordinal,
           :symbol symbol,
           :position position}),
         :symbol symbol,
         :qualifier
         (when namespace-part (clojure.core/symbol namespace-part)),
         :name simple-name,
         :position position,
         :scope-chain (vec scope-chain),
         :semantic-span (sh06-resolution-semantic-span span ordinal),
         :source-span span}))))
    add-scope!
    (fn
     [owner-syntax-id parent-scope-id local-names span]
     (let
      [ordinal
       (swap! next-scope inc)
       scope-id
       (reader-canonical-hash
        {:domain :gravity/sh06-lexical-scope-v1,
         :owner-syntax-id owner-syntax-id,
         :ordinal ordinal,
         :parent-scope-id parent-scope-id})
       bindings
       (mapv
        (fn
         [binding-ordinal name]
         {:name name,
          :kind :local,
          :semantic-span
          {:scope-ordinal ordinal, :binding-ordinal binding-ordinal},
          :source-span span,
          :binding-syntax-id
          (reader-canonical-hash
           {:domain :gravity/sh06-local-binding-v1,
            :scope-id scope-id,
            :ordinal binding-ordinal,
            :name name}),
          :allow-shadow? (not (contains? shadow-forbidden name))})
        (range)
        local-names)]
      (swap!
       scopes
       conj
       {:scope-id scope-id,
        :parent-scope-id parent-scope-id,
        :owner-syntax-id owner-syntax-id,
        :bindings bindings})
      scope-id))
    walk-form
    (semantic-late-sh06-resolution-analysis-inputs-make-traversal
     semantic-late-sh06-resolution-analysis-inputs-parameter-names
     add-reference!
     add-scope!
     operations)]
   (doseq
    [syntax (:expanded-syntax-stream sh05-artifact)]
    (let
     [form (:form syntax)]
     (when-not
      (and (seq? form) (= 'ns (first form)))
      (walk-form form [] syntax :expression))))
   {:lexical-scopes @scopes, :references @references})))

(doseq
 [operation
  [#'analysis-inputs
   #'binding-current!
   #'boundary-fail!
   #'build-plan-binding!
   #'candidate-for
   #'core-record
   #'core-records
   #'current-plan-binding!
   #'definition-kind
   #'definition-records
   #'dependency-edges
   #'dependency-record
   #'envelope-id
   #'execute!
   #'explicit-candidate-records
   #'fixed-vector-pattern?
   #'import-binding-records
   #'import-records
   #'let-loop-binding-paths
   #'match-pattern-binding-paths
   #'package-record
   #'plan-identities
   #'read-current-source!
   #'read-pinned-source!
   #'request-overrides
   #'resolve-source-path
   #'semantic-span
   #'source-key
   #'source-revision-id
   #'unique-match-binding-paths]]
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
