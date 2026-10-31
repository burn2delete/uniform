^{:line 87, :column 18}
(ns
 gravity.resolution
 "Gravity resolution source and plan runtime with explicit temporary seed operations.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {binding-current!
    {:arglists ([source-path binding operations]),
     :operation-inputs
     [:sh06-resolution-boundary-fail!
      :sh06-resolution-current-source-key]},
    boundary-fail!
    {:arglists
     ([rule source-path missing-field observed facts operations]),
     :operation-inputs [:c5-resolution-fail! :source-span]},
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
    current-plan-binding!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh06-resolution-binding-current!
      :sh06-resolution-boundary-fail!
      :sh06-resolution-build-binding!
      :sh06-resolution-current-source-key]},
    execute!
    {:arglists ([source-path binding function arguments operations]),
     :operation-inputs
     [:p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata
      :sh06-resolution-boundary-fail!]},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input]},
    read-current-source!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:sh06-resolution-boundary-fail!
      :sh06-resolution-resolve-source-path
      :sha256-bytes-hex]},
    read-pinned-source!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh06-resolution-boundary-fail!
      :sh06-resolution-read-current-source!]},
    resolve-source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path]},
    source-key
    {:arglists ([operations]),
     :operation-inputs
     [:c-backend-resolve-p15-s23-compiler-source-path
      :p15-s23-stage2-compiler-artifact-source-path
      :sh06-resolution-resolve-source-path
      :sha256-bytes-hex]}},
   :operation-inputs
   [:input
    :c-backend-canonical-value
    :c-backend-resolve-p15-s23-compiler-source-path
    :c-backend-stage2-plan-emitter-source-rule!
    :c5-resolution-fail!
    :p15-s23-c11-mir-digest
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :sh04-syntax-strip-host-metadata
    :sh06-resolution-binding-current!
    :sh06-resolution-boundary-fail!
    :sh06-resolution-build-binding!
    :sh06-resolution-current-source-key
    :sh06-resolution-plan-identities
    :sh06-resolution-read-current-source!
    :sh06-resolution-resolve-source-path
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
   :governing-contracts [D1 D2 D3 D9 L3 C1 C3 C4 C5 C15 TEST2],
   :seed-inputs
   [:*sh06-resolution-operation-binding*
    :sh06-resolution-cached-binding
    :sh06-resolution-expected-source-byte-count
    :sh06-resolution-expected-source-content-hash
    :sh06-resolution-public-function-shapes
    :sh06-resolution-source-relative-path],
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
    :performance],
   :artifact-inputs
   [:current-source
    :compiler-and-emitter-snapshots
    :plan-records
    :resolution-requests],
   :namespace gravity.resolution,
   :diagnostic-ownership
   {:stage [:source-plan-boundary :host-containment],
    :catalog :existing-c5-catalog,
    :resolution-semantics :gravity-source},
   :artifact-outputs
   [:source-identities
    :development-plan-binding
    :function-identities
    :resolution-results],
   :clojure-seed-boundary? true,
   :contract-boundary :gravity-resolution-source-and-plan-runtime})

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

(doseq
 [operation
  [#'boundary-fail!
   #'resolve-source-path
   #'read-current-source!
   #'read-pinned-source!
   #'plan-identities
   #'build-plan-binding!
   #'source-key
   #'binding-current!
   #'current-plan-binding!
   #'execute!]]
 ^{:line 90, :column 30}
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
