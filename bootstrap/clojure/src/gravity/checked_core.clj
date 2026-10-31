(ns
 gravity.checked-core
 "Current checked-core source and plan runtime with explicit temporary seed operations.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {current-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input :sh07-core-build-binding! :sh07-core-current-source-key],
     :admission :none},
    root-syntax-id
    {:arglists [[source-revision-id ordinal form operations]],
     :operation-inputs
     [:reader-canonical-hash :sh05-path-neutral-semantic-value],
     :admission :none},
    execute!
    {:arglists ([source-path function arguments operations]),
     :operation-inputs
     [:input
      :p15-s23-c6c10-carrier-validation
      :p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata],
     :admission :none},
    children
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    source-map-order
    {:arglists [[resolution-artifact syntaxes trees operations]],
     :operation-inputs
     [:sh07-core-projected-source-mismatch
      :sh07-core-source-occurrence-context],
     :admission :none},
    decimal-descriptor
    {:arglists
     [[source-path value evidence projection-form-id operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input],
     :admission :none},
    decimal-evidence
    {:arglists [[resolution-artifact operations]],
     :operation-inputs [:p15-s23-c6c10-literal-scalar-descriptor],
     :admission :none},
    source-key
    {:arglists ([operations]),
     :operation-inputs
     [:c-backend-resolve-p15-s23-compiler-source-path
      :p15-s23-stage2-compiler-artifact-source-path
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none},
    macro-trace
    {:arglists [[lineage sh05-step operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    value-kind
    {:arglists [[value _operations]],
     :operation-inputs [],
     :admission :none},
    build-form-tree
    {:arglists
     [[source-path
       source-revision-id
       decimal-evidence
       syntax
       trace
       operations]],
     :operation-inputs
     [:sh05-path-neutral-semantic-value
      :sh07-core-children
      :sh07-core-neutral-value
      :sh07-core-projected-form-id
      :sh07-core-projected-syntax-id
      :sh07-core-semantic-span
      :sh07-core-value-kind],
     :admission :none},
    source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path],
     :admission :none},
    semantic-macro-trace
    {:arglists [[trace operations]],
     :operation-inputs [:sh05-path-neutral-semantic-value],
     :admission :none},
    projected-syntax-id
    {:arglists
     [[source-revision-id root-syntax-id path kind trace operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    source-path-from-resolution
    {:arglists [[resolution-artifact _operations]],
     :operation-inputs [],
     :admission :none},
    decimal-evidence-complete!
    {:arglists [[source-path decimal-evidence _operations]],
     :operation-inputs [],
     :admission :none},
    parameter-binding-paths
    {:arglists [[parameters parameter-path _operations]],
     :operation-inputs [],
     :admission :none},
    neutral-value
    {:arglists
     [[source-path
       source-revision-id
       root-syntax-id
       path
       decimal-evidence
       value
       operations]
      [source-path
       source-revision-id
       root-syntax-id
       path
       decimal-evidence
       value
       source-occurrences
       operations]],
     :operation-inputs
     [:p15-s23-c6c10-literal-scalar-descriptor
      :sh07-core-decimal-descriptor
      :sh07-core-neutral-value
      :sh07-core-projected-form-id],
     :admission :none},
    projected-form-id
    {:arglists
     [[source-revision-id root-syntax-id path kind operations]],
     :operation-inputs [:reader-canonical-hash],
     :admission :none},
    semantic-span
    {:arglists [[span _operations]],
     :operation-inputs [],
     :admission :none},
    build-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input
      :c-backend-stage2-plan-emitter-source-rule!
      :p15-s23-stage2-compiler-artifact-plan
      :read-forms
      :sh07-core-plan-identities
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none}},
   :operation-inputs
   [:c-backend-resolve-p15-s23-compiler-source-path
    :c-backend-stage2-plan-emitter-source-rule!
    :input
    :p15-s23-c11-mir-digest
    :p15-s23-c6c10-carrier-validation
    :p15-s23-c6c10-literal-scalar-descriptor
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :read-forms
    :reader-canonical-hash
    :sh03-reader-host-character-value!
    :sh04-syntax-strip-host-metadata
    :sh05-path-neutral-semantic-value
    :sh07-core-build-binding!
    :sh07-core-children
    :sh07-core-current-source-key
    :sh07-core-decimal-descriptor
    :sh07-core-neutral-value
    :sh07-core-plan-identities
    :sh07-core-projected-form-id
    :sh07-core-projected-source-mismatch
    :sh07-core-projected-syntax-id
    :sh07-core-semantic-span
    :sh07-core-source-occurrence-context
    :sh07-core-source-path
    :sh07-core-source-path-from-resolution
    :sh07-core-value-kind
    :sha256-bytes-hex],
   :dependency-direction
   {:requires [clojure.core], :forbids [gravity.bootstrap]},
   :seed-lifetime
   [:cache-atom
    :cached-binding-deref
    :scoped-execution-binding
    :configuration
    :host-operations],
   :self-hosted? false,
   :private-implementation
   {source-occurrence-context
    {:arglists [[resolution-artifact syntaxes operations]],
     :operation-inputs
     [:sh03-reader-host-character-value!
      :sh07-core-children
      :sh07-core-source-path-from-resolution
      :sh07-core-value-kind],
     :admission :none},
    projected-source-mismatch
    {:arglists [[source target operations]],
     :operation-inputs [:sh07-core-value-kind],
     :admission :none}},
   :governing-contracts
   [D1 D2 D3 D9 L2 C1 C3 C4 C5 C6 C15 BOOT2 BOOT8 TEST2 L1 C2],
   :seed-inputs
   [:*sh07-core-execution-binding*
    :sh07-core-cached-binding
    :sh07-core-development-binding
    :sh07-core-public-function-shapes
    :sh07-core-source-relative-path],
   :unsupported
   [:canonical-c6-admission
    :lowering-assembly
    :artifact-verification
    :owner-publication
    :historical-migration
    :type-effect-safety-completion
    :native-launch
    :release
    :performance
    :self-hosting
    :seed-retirement],
   :artifact-inputs
   [:current-core-source
    :compiler-and-emitter-snapshots
    :plan-records
    :function-and-argument-carriers
    :owner-verified-c5-and-syntax-records
    :c2-form-occurrences
    :literal-decoding-records],
   :namespace gravity.checked-core,
   :diagnostic-ownership
   {:stage
    [:source-plan-contract
     :source-toolchain-drift
     :native-carrier-transport
     :host-containment
     :source-occurrence-bijections
     :decimal-c2-evidence
     :projected-parent-child-links],
    :catalog :existing-c6-catalog,
    :lowering-semantics :gravity-source},
   :artifact-outputs
   [:current-development-plan-binding
    :source-and-function-identities
    :gravity-function-results
    :projected-form-and-syntax-identities
    :form-trees
    :source-collection-order
    :macro-origin-traces
    :private-decimal-occurrence-state],
   :clojure-seed-boundary? true,
   :contract-boundary :checked-core-runtime-and-source-projection})

(defn
 source-path
 [operations]
 (let
  [input
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
        (input :sh07-core-source-relative-path))]
      (if
       (.isFile candidate)
       (.getPath candidate)
       (recur (.getParentFile directory))))))
   (input :sh07-core-source-relative-path))))

(defn
 plan-identities
 [plan operations]
 (let
  [input
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
    (keys (input :sh07-core-public-function-shapes))),
   :public-function-shapes
   (select-keys
    shapes
    (keys (input :sh07-core-public-function-shapes)))}))

(defn
 build-plan-binding!
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   c-backend-stage2-plan-emitter-source-rule!
   (.valAt
    ^clojure.lang.ILookup operations
    :c-backend-stage2-plan-emitter-source-rule!)
   p15-s23-stage2-compiler-artifact-plan
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-compiler-artifact-plan)
   read-forms
   (.valAt ^clojure.lang.ILookup operations :read-forms)
   sh07-core-plan-identities
   (.valAt ^clojure.lang.ILookup operations :sh07-core-plan-identities)
   sh07-core-source-path
   (.valAt ^clojure.lang.ILookup operations :sh07-core-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source-path
   (sh07-core-source-path)
   bytes
   (java.nio.file.Files/readAllBytes
    (.toPath (java.io.File. source-path)))
   source-text
   (String. bytes java.nio.charset.StandardCharsets/UTF_8)
   source-hash
   (str "sha256:" (sha256-bytes-hex bytes))
   emitter
   (:emitter
    (c-backend-stage2-plan-emitter-source-rule! source-path :jvm))
   plan
   (p15-s23-stage2-compiler-artifact-plan
    emitter
    source-path
    source-text)
   identities
   (sh07-core-plan-identities plan)]
  (when-not
   (and
    (= source-hash (get-in plan [:source :sha256]))
    (=
     (input :sh07-core-public-function-shapes)
     (:public-function-shapes identities))
    (=
     (set
      (concat
       '[sh07-core-contract sh07-core-diagnostic-catalog]
       (keys (input :sh07-core-public-function-shapes))))
     (set
      (some
       (fn*
        [p1__156#]
        (when (= :exports (first p1__156#)) (second p1__156#)))
       (drop 2 (first (read-forms source-path source-text))))))
    (= 'gravity.checked-core (get-in plan [:module :module]))
    (= :meta (get-in plan [:module :profile]))
    (= :jvm (get-in plan [:module :target]))
    (= #{} (get-in plan [:module :effects]))
    (= #{} (get-in plan [:module :capabilities]))
    (= :safe (get-in plan [:module :safety])))
   (throw
    (ex-info
     "SH-07 checked-core module contract changed"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :missing-fields [:sh07-core-module-contract],
      :observed
      (merge
       {:source-byte-count (alength bytes),
        :source-content-hash source-hash,
        :module (:module plan),
        :plan-source (:source plan)}
       identities)})))
  (when-not
   (=
    source-hash
    (str
     "sha256:"
     (sha256-bytes-hex
      (java.nio.file.Files/readAllBytes
       (.toPath (java.io.File. source-path))))))
   (throw
    (ex-info
     "SH-07 source changed during compilation"
     {:id "C6-VERIFY",
      :stage :core-lowering,
      :reason :checked-core-source-drift})))
  (merge
   {:source-content-hash source-hash,
    :executed-by :clojure-stage2-generic-rule-runner,
    :revision-status :development-unadmitted,
    :self-hosted? false,
    :source-path source-path,
    :source-byte-count (alength bytes),
    :artifact :gravity/sh07-current-core-plan-binding,
    :semantic-authority :gravity-source,
    :plan plan,
    :compiled-by :clojure-stage0-seed,
    :generic-bridge-residual? true}
   identities)))

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
   sh07-core-source-path
   (.valAt ^clojure.lang.ILookup operations :sh07-core-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source
   (sh07-core-source-path)
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
 current-plan-binding!
 "Compile the current source; reuse only a binding for unchanged input bytes."
 [operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   sh07-core-build-binding!
   (.valAt ^clojure.lang.ILookup operations :sh07-core-build-binding!)
   sh07-core-current-source-key
   (.valAt
    ^clojure.lang.ILookup operations
    :sh07-core-current-source-key)
   cache-lock
   (input :sh07-core-development-binding)]
  (locking
   cache-lock
   (let
    [key
     (sh07-core-current-source-key)
     cached
     @(input :sh07-core-development-binding)]
    (when
     (and
      cached
      (not= (:toolchain key) (get-in cached [:key :toolchain])))
     (throw
      (ex-info
       "SH-07 compiler dependencies changed; reload the toolchain"
       {:id "C6-VERIFY",
        :stage :core-lowering,
        :reason :checked-core-toolchain-drift})))
    (if
     (= key (:key cached))
     (:binding cached)
     (let
      [binding (sh07-core-build-binding!)]
      (when-not
       (= (second (:source key)) (:source-content-hash binding))
       (throw
        (ex-info
         "SH-07 source changed before binding publication"
         {:id "C6-VERIFY",
          :stage :core-lowering,
          :reason :checked-core-source-drift})))
      (let
       [after (sh07-core-current-source-key)]
       (when-not
        (= key after)
        (throw
         (ex-info
          "SH-07 inputs changed before binding publication"
          {:id "C6-VERIFY",
           :stage :core-lowering,
           :reason
           (if
            (= (:toolchain key) (:toolchain after))
            :checked-core-source-drift
            :checked-core-toolchain-drift)}))))
      (reset!
       (input :sh07-core-development-binding)
       {:key key, :binding binding})
      binding))))))

(defn
 execute!
 [source-path function arguments operations]
 (let
  [input
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-c6c10-carrier-validation
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-carrier-validation)
   p15-s23-stage2-runtime-execute-function
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-runtime-execute-function)
   sh04-syntax-strip-host-metadata
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-strip-host-metadata)]
  (try
   (p15-s23-c6c10-carrier-validation source-path arguments)
   (let
    [result
     (p15-s23-stage2-runtime-execute-function
      {:engine :gravity-sh07-pinned-core-runner,
       :compiler-artifact-plan? true}
      (:plan
       (or
        (input :*sh07-core-execution-binding*)
        @(input :sh07-core-cached-binding)))
      function
      (sh04-syntax-strip-host-metadata arguments))]
    (p15-s23-c6c10-carrier-validation source-path result)
    (sh04-syntax-strip-host-metadata result))
   (catch
    InterruptedException
    interrupted
    (.interrupt (Thread/currentThread))
    (throw interrupted))
   (catch
    StackOverflowError
    error
    (throw
     (ex-info
      "SH-07 core boundary rejected host stack exhaustion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    OutOfMemoryError
    error
    (throw
     (ex-info
      "SH-07 core boundary rejected host memory exhaustion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    AssertionError
    error
    (throw
     (ex-info
      "SH-07 core boundary contained an assertion"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch
    LinkageError
    error
    (throw
     (ex-info
      "SH-07 core boundary contained a linkage failure"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))})))
   (catch clojure.lang.ExceptionInfo error (throw error))
   (catch
    Exception
    error
    (throw
     (ex-info
      "SH-07 core boundary contained a host failure"
      {:id "C6-VERIFY",
       :stage :core-lowering,
       :source-path source-path,
       :contained-host-error (.getName (class error))}
      error))))))

(do
 (defn
  source-path-from-resolution
  [resolution-artifact _operations]
  (or
   (get-in resolution-artifact [:provenance :source-path])
   (get-in
    resolution-artifact
    [:gravity-resolution-boundary
     :authenticated-resolution-request
     :provenance
     :actual-source-path])
   "<sh07-core-input>"))
 (defn
  semantic-span
  [span _operations]
  (select-keys
   (or (:primary span) span {})
   [:byte-start
    :byte-end
    :line-start
    :column-start
    :line-end
    :column-end
    :scalar-start
    :scalar-end]))
 (defn
  value-kind
  [value _operations]
  (cond
   (nil? value)
   :nil
   (boolean? value)
   :boolean
   (integer? value)
   :integer
   (or
    (instance? java.math.BigDecimal value)
    (instance? Double value)
    (instance? Float value))
   :decimal
   (ratio? value)
   :ratio
   (char? value)
   :character
   (string? value)
   :string
   (keyword? value)
   :keyword
   (symbol? value)
   :symbol
   (vector? value)
   :vector
   (map? value)
   :map
   (set? value)
   :set
   (seq? value)
   :list
   :else
   :unsupported))
 (defn
  children
  [value _operations]
  (cond
   (map? value)
   (vec (mapcat identity value))
   (set? value)
   (vec (sort-by pr-str value))
   (or (vector? value) (seq? value))
   (vec value)
   :else
   []))
 (defn
  decimal-descriptor
  [source-path value evidence projection-form-id operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    decimal
    ^java.math.BigDecimal value
    scale
    (long (.scale decimal))
    unscaled
    (.unscaledValue decimal)]
   (when-not
    (and
     evidence
     (= :decimal (:kind evidence))
     (= value (:decoded evidence)))
    (throw
     (ex-info
      "Decimal literal is not exactly bound to C2 evidence"
      {:id "C6-ORIGIN",
       :stage :core-lowering,
       :source-path source-path,
       :reason :exact-decimal-c2-evidence})))
   {:kind :gravity/arbitrary-decimal-literal,
    :unscaled-value (.toString unscaled),
    :scale scale,
    :literal-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-literal-evidence-v1,
      :literal-id (:literal-id evidence),
      :form-id (:form-id evidence)}),
    :form-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-form-evidence-v1,
      :form-id (:form-id evidence),
      :literal-id (:literal-id evidence)}),
    :token-id
    (reader-canonical-hash
     {:domain :gravity/sh07-c2-decimal-token-evidence-v1,
      :token-id (:token-id evidence),
      :literal-id (:literal-id evidence),
      :form-id (:form-id evidence)}),
    :projection-form-id projection-form-id}))
 (defn
  decimal-evidence
  [resolution-artifact operations]
  (let
   [p15-s23-c6c10-literal-scalar-descriptor
    (.valAt
     ^clojure.lang.ILookup operations
     :p15-s23-c6c10-literal-scalar-descriptor)
    records
    (get-in
     resolution-artifact
     [:sh05-macro-artifact
      :gravity-macro-boundary
      :authenticated-sh04-artifact
      :c2-reader-artifact
      :literal-decoding-records])]
   {:records
    (reduce
     (fn
      [result record]
      (if
       (instance? java.math.BigDecimal (:decoded record))
       (update
        result
        (p15-s23-c6c10-literal-scalar-descriptor (:decoded record))
        (fnil conj [])
        record)
       result))
     {}
     records),
    :next (atom {}),
    :cache (atom {})}))
 (defn
  projected-form-id
  [source-revision-id root-syntax-id path kind operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
   (reader-canonical-hash
    {:domain :gravity/sh07-form-occurrence-v1,
     :source-revision-id source-revision-id,
     :root-syntax-id root-syntax-id,
     :path path,
     :kind kind})))
 (defn
  projected-syntax-id
  [source-revision-id root-syntax-id path kind trace operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)]
   (cond
    (empty? path)
    root-syntax-id
    (and trace (= path [2]))
    (:introduced-fn-syntax-id trace)
    :else
    (reader-canonical-hash
     {:domain :gravity/sh07-form-syntax-v1,
      :source-revision-id source-revision-id,
      :root-syntax-id root-syntax-id,
      :path path,
      :kind kind}))))
 (defn-
  projected-source-mismatch
  [source target operations]
  (let
   [sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)]
   (when
    source
    (let
     [value
      (:value source)
      expected-kind
      (if
       (= :character (:kind source))
       :character
       (sh07-core-value-kind value))]
     (cond
      (not= expected-kind (:kind target))
      :map-source-value-or-kind
      (and
       (not (coll? value))
       (not
        (if
         (instance? java.math.BigDecimal value)
         (and
          (=
           :gravity/arbitrary-decimal-literal
           (:kind (:value target)))
          (=
           (.scale ^java.math.BigDecimal value)
           (:scale (:value target)))
          (=
           (str (.unscaledValue ^java.math.BigDecimal value))
           (:unscaled-value (:value target))))
         (= value (:value target)))))
      :map-source-projected-literal
      :else
      nil)))))
 (defn
  neutral-value
  ([source-path
    source-revision-id
    root-syntax-id
    path
    decimal-evidence
    value
    operations]
   (let
    [sh07-core-neutral-value
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-neutral-value)]
    (sh07-core-neutral-value
     source-path
     source-revision-id
     root-syntax-id
     path
     decimal-evidence
     value
     nil)))
  ([source-path
    source-revision-id
    root-syntax-id
    path
    decimal-evidence
    value
    source-occurrences
    operations]
   (let
    [p15-s23-c6c10-literal-scalar-descriptor
     (.valAt
      ^clojure.lang.ILookup operations
      :p15-s23-c6c10-literal-scalar-descriptor)
     sh07-core-decimal-descriptor
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-decimal-descriptor)
     sh07-core-neutral-value
     (.valAt ^clojure.lang.ILookup operations :sh07-core-neutral-value)
     sh07-core-projected-form-id
     (.valAt
      ^clojure.lang.ILookup operations
      :sh07-core-projected-form-id)]
    (cond
     (= :character (get-in source-occurrences [:sources path :kind]))
     (if
      (instance? clojure.lang.IObj value)
      (with-meta value nil)
      value)
     (instance? java.math.BigDecimal value)
     (let
      [descriptor
       (p15-s23-c6c10-literal-scalar-descriptor value)
       cache-key
       [root-syntax-id path]
       cached
       (get @(:cache decimal-evidence) cache-key)]
      (or
       cached
       (let
        [ordinal
         (get @(:next decimal-evidence) descriptor 0)
         evidence
         (get-in decimal-evidence [:records descriptor ordinal])
         projection-form-id
         (sh07-core-projected-form-id
          source-revision-id
          root-syntax-id
          path
          :decimal)
         result
         (sh07-core-decimal-descriptor
          source-path
          value
          evidence
          projection-form-id)]
        (swap! (:next decimal-evidence) assoc descriptor (inc ordinal))
        (swap! (:cache decimal-evidence) assoc cache-key result)
        result)))
     (map? value)
     (into
      {}
      (map-indexed
       (fn
        [index [key child]]
        [(sh07-core-neutral-value
          source-path
          source-revision-id
          root-syntax-id
          (conj path (* 2 index))
          decimal-evidence
          key
          source-occurrences)
         (sh07-core-neutral-value
          source-path
          source-revision-id
          root-syntax-id
          (conj path (inc (* 2 index)))
          decimal-evidence
          child
          source-occurrences)]))
      value)
     (vector? value)
     (mapv
      (fn
       [index child]
       (sh07-core-neutral-value
        source-path
        source-revision-id
        root-syntax-id
        (conj path index)
        decimal-evidence
        child
        source-occurrences))
      (range)
      value)
     (set? value)
     (into
      #{}
      (map-indexed
       (fn
        [index child]
        (sh07-core-neutral-value
         source-path
         source-revision-id
         root-syntax-id
         (conj path index)
         decimal-evidence
         child
         source-occurrences)))
      (sort-by pr-str value))
     (seq? value)
     (apply
      list
      (map-indexed
       (fn
        [index child]
        (sh07-core-neutral-value
         source-path
         source-revision-id
         root-syntax-id
         (conj path index)
         decimal-evidence
         child
         source-occurrences))
       value))
     :else
     value))))
 (defn
  macro-trace
  [lineage sh05-step operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    input-id
    (:input-syntax-id sh05-step)
    output-id
    (:output-syntax-id sh05-step)
    fn-id
    (reader-canonical-hash
     {:domain :gravity/sh07-introduced-fn-syntax-v1,
      :input-syntax-id input-id,
      :output-def-syntax-id output-id})
    def-origin-id
    (reader-canonical-hash
     {:domain :gravity/sh07-def-generated-origin-v1,
      :input-syntax-id input-id,
      :output-syntax-id output-id})
    fn-origin-id
    (reader-canonical-hash
     {:domain :gravity/sh07-fn-generated-origin-v1,
      :input-syntax-id input-id,
      :fn-syntax-id fn-id})]
   {:macro-expansion-trace-id (:macro-expansion-trace-id lineage),
    :def-generated-origin-id def-origin-id,
    :sh05-artifact-id (:sh05-artifact-id lineage),
    :output-def-syntax-id output-id,
    :source-revision-id (:source-revision-id lineage),
    :macro 'defn,
    :fn-generated-origin-id fn-origin-id,
    :introduced-fn-syntax-id fn-id,
    :input-syntax-id input-id}))
 (defn
  root-syntax-id
  [source-revision-id ordinal form operations]
  (let
   [reader-canonical-hash
    (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
    sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)]
   (reader-canonical-hash
    {:domain :gravity/sh07-root-syntax-v1,
     :source-revision-id source-revision-id,
     :ordinal ordinal,
     :form (sh05-path-neutral-semantic-value form)})))
 (defn
  decimal-evidence-complete!
  [source-path decimal-evidence _operations]
  (doseq
   [[descriptor records] (:records decimal-evidence)]
   (let
    [consumed (get @(:next decimal-evidence) descriptor 0)]
    (when-not
     (= consumed (count records))
     (throw
      (ex-info
       "SH-07 decimal occurrence evidence is not bijective"
       {:id "C6-ORIGIN",
        :stage :core-lowering,
        :source-path source-path,
        :reason :decimal-c2-occurrence-bijection,
        :descriptor descriptor,
        :expected-occurrences (count records),
        :observed-occurrences consumed})))))
  :passed)
 (defn
  semantic-macro-trace
  [trace operations]
  (let
   [sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)]
   (mapv
    (fn
     [ordinal step]
     {:capabilities
      (sh05-path-neutral-semantic-value (:capabilities step)),
      :diagnostics
      (sh05-path-neutral-semantic-value (:diagnostics step)),
      :build-effects
      (sh05-path-neutral-semantic-value (:build-effects step)),
      :ordinal ordinal,
      :macro (:macro step),
      :artifact :gravity/sh07-preserved-macro-expansion-step,
      :target (:target step),
      :step (:step step),
      :profile (:profile step)})
    (range)
    trace)))
 (defn-
  source-occurrence-context
  [resolution-artifact syntaxes operations]
  (let
   [sh03-reader-host-character-value!
    (.valAt
     ^clojure.lang.ILookup operations
     :sh03-reader-host-character-value!)
    sh07-core-children
    (.valAt ^clojure.lang.ILookup operations :sh07-core-children)
    sh07-core-source-path-from-resolution
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-path-from-resolution)
    sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)
    source-path
    (sh07-core-source-path-from-resolution resolution-artifact)
    c2
    (get-in
     resolution-artifact
     [:sh05-macro-artifact
      :gravity-macro-boundary
      :authenticated-sh04-artifact
      :c2-reader-artifact])
    sources
    (:form-tree c2)
    source-index
    (into {} (map (juxt :form-id identity)) sources)
    literals
    (group-by :form-id (:literal-decoding-records c2))
    fail
    (fn
     [reason source]
     (throw
      (ex-info
       "SH-07 source occurrence projection is not bijective"
       {:id "C6-ORIGIN",
        :rule "C6-ORIGIN",
        :stage :core-lowering,
        :reason reason,
        :source-path source-path,
        :source-span (:span source),
        :source-form-id (:form-id source)})))]
   (when-not
    (and (vector? sources) (= (count sources) (count source-index)))
    (fail :map-source-form-id-bijection nil))
   (letfn
    [(children!
      [source]
      (let
       [ids (:children source)]
       (when-not
        (and
         (vector? ids)
         (= (count ids) (count (set ids)))
         (every?
          (fn*
           [p1__157#]
           (=
            (:form-id source)
            (:parent-form-id (get source-index p1__157#))))
          ids))
        (fail :map-source-child-link source))
       (mapv source-index ids)))
     (unwrap
      [source]
      (loop
       [source source seen #{}]
       (when
        (or (nil? source) (contains? seen (:form-id source)))
        (fail :map-source-occurrence-missing source))
       (if
        (= :metadata-wrapper (:kind source))
        (let
         [children (children! source)]
         (when-not
          (= 2 (count children))
          (fail :map-source-metadata-shape source))
         (recur (second children) (conj seen (:form-id source))))
        source)))
     (character!
      [source value]
      (let
       [records
        (get literals (:form-id source))
        record
        (first records)
        codepoint
        (if
         (char? value)
         (int value)
         (when (map? value) (:codepoint value)))
        expected
        (when
         (and
          (integer? codepoint)
          (<= 0 codepoint 1114111)
          (not (<= 55296 codepoint 57343)))
         (sh03-reader-host-character-value!
          source-path
          {:codepoint codepoint}))]
       (when-not
        (and
         (= [] (:children source))
         (= 1 (count records))
         (= :character (:kind record))
         (= value expected (:value source) (:decoded record))
         (= (:raw source) (:raw record))
         (= (:span source) (:span record)))
        (fail :character-c2-occurrence-mismatch source))))]
    (into
     {}
     (map
      (fn
       [syntax]
       (let
        [occurrences
         (atom {})
         orders
         (atom {})
         used
         (atom #{})
         remember!
         (fn
          [source path]
          (when
           (contains? @used (:form-id source))
           (fail :map-source-occurrence-duplicate source))
          (swap! used conj (:form-id source))
          (swap! occurrences assoc path source))]
        (letfn
         [(walk
           [source value path inert?]
           (let
            [source
             (unwrap source)
             kind
             (:kind source)
             children
             (children! source)]
            (remember! source path)
            (if
             (= :character kind)
             (character! source value)
             (do
              (when-not
               (or
                (= kind (sh07-core-value-kind value))
                (contains? #{:tagged-literal :abbreviation} kind))
               (fail :map-source-value-or-kind source))
              (when
               (and (not (coll? value)) (not= (:value source) value))
               (fail :map-source-projected-literal source))
              (cond
               (and
                (seq? value)
                (= 'quote (first value))
                (= 2 (count value)))
               (do
                (if
                 (= :abbreviation kind)
                 (when-not
                  (and
                   (= :quote (:abbrev source))
                   (= 1 (count children)))
                  (fail :map-source-quote-shape source))
                 (do
                  (when-not
                   (= 2 (count children))
                   (fail :map-source-quote-shape source))
                  (walk (first children) 'quote (conj path 0) inert?)))
                (walk
                 (last children)
                 (second value)
                 (conj path 1)
                 true))
               (map? value)
               (let
                [entries
                 (vec value)
                 pairs
                 (mapv vec (partition 2 children))
                 slots
                 (into
                  {}
                  (map-indexed (fn [index [key _]] [key index]))
                  entries)
                 matched
                 (mapv
                  (fn*
                   [p1__158#]
                   (get
                    slots
                    (:value (first p1__158#))
                    :gravity.bootstrap/missing))
                  pairs)]
                (when-not
                 (and
                  (even? (count children))
                  (if
                   inert?
                   true
                   (and
                    (= (count children) (* 2 (count entries)))
                    (not-any? #{:gravity.bootstrap/missing} matched)
                    (= (count matched) (count (set matched))))))
                 (fail :map-source-entry-bijection source))
                (swap! orders assoc path (when-not inert? matched))
                (doseq
                 [[index [key value]] (map-indexed vector entries)]
                 (let
                  [pair
                   (last
                    (filter
                     (fn*
                      [p1__159#]
                      (and
                       (= key (:value (first p1__159#)))
                       (= value (:value (second p1__159#)))))
                     pairs))]
                  (when-not
                   pair
                   (fail :map-source-entry-bijection source))
                  (walk
                   (first pair)
                   key
                   (conj path (* 2 index))
                   inert?)
                  (walk
                   (second pair)
                   value
                   (conj path (inc (* 2 index)))
                   inert?))))
               (set? value)
               (let
                [values
                 (sh07-core-children value)
                 slots
                 (zipmap values (range))
                 matched
                 (mapv
                  (fn*
                   [p1__160#]
                   (get
                    slots
                    (:value p1__160#)
                    :gravity.bootstrap/missing))
                  children)]
                (when-not
                 (and
                  (=
                   (count children)
                   (count values)
                   (count (set matched)))
                  (not-any? #{:gravity.bootstrap/missing} matched))
                 (fail :map-source-set-bijection source))
                (swap! orders assoc path (when-not inert? matched))
                (doseq
                 [[child index] (map vector children matched)]
                 (walk
                  child
                  (get values index)
                  (conj path index)
                  inert?)))
               (= :abbreviation kind)
               (do
                (when-not
                 (and
                  (= 1 (count children))
                  (= 2 (count value))
                  (seq? value)
                  (=
                   (first value)
                   (get
                    {:quote 'quote,
                     :syntax-quote 'syntax-quote,
                     :unquote 'unquote,
                     :splice-unquote 'splice-unquote,
                     :deref 'deref}
                    (:abbrev source))))
                 (fail :map-source-abbreviation-shape source))
                (walk
                 (first children)
                 (second value)
                 (conj path 1)
                 inert?))
               (or (vector? value) (seq? value))
               (do
                (when-not
                 (= (count children) (count value))
                 (fail :map-source-sequence-shape source))
                (doseq
                 [[index child raw]
                  (map vector (range) children value)]
                 (walk child raw (conj path index) inert?)))
               :else
               nil)))))]
         (let
          [source
           (unwrap
            (get source-index (get-in syntax [:source :form-id])))
           value
           (:form syntax)]
          (if
           (and
            (seq? (:value source))
            (= 'defn (first (:value source))))
           (let
            [children
             (children! source)
             fn-value
             (nth value 2 nil)
             trace
             (filter
              (fn*
               [p1__161#]
               (and
                (= 'defn (:macro p1__161#))
                (= (:syntax/id syntax) (:output-syntax-id p1__161#))))
              (get-in
               resolution-artifact
               [:sh05-macro-artifact :macro-expansion-trace]))]
            (when-not
             (and
              (= 1 (count trace))
              (seq? value)
              (= 'def (first value))
              (= 3 (count value))
              (seq? fn-value)
              (= 'fn (first fn-value))
              (= (dec (count children)) (count fn-value)))
             (fail :map-source-defn-transfer source))
            (remember! source [])
            (walk (get children 1) (second value) [1] false)
            (doseq
             [index (range 2 (count children))]
             (walk
              (get children index)
              (nth fn-value (dec index))
              [2 (dec index)]
              false)))
           (walk source value [] false))))
        [(:sh07/root-syntax-id syntax)
         {:sources @occurrences, :collection-orders @orders}]))
      syntaxes)))))
 (defn
  build-form-tree
  [source-path
   source-revision-id
   decimal-evidence
   syntax
   trace
   operations]
  (let
   [sh05-path-neutral-semantic-value
    (.valAt
     ^clojure.lang.ILookup operations
     :sh05-path-neutral-semantic-value)
    sh07-core-children
    (.valAt ^clojure.lang.ILookup operations :sh07-core-children)
    sh07-core-neutral-value
    (.valAt ^clojure.lang.ILookup operations :sh07-core-neutral-value)
    sh07-core-projected-form-id
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-form-id)
    sh07-core-projected-syntax-id
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-syntax-id)
    sh07-core-semantic-span
    (.valAt ^clojure.lang.ILookup operations :sh07-core-semantic-span)
    sh07-core-value-kind
    (.valAt ^clojure.lang.ILookup operations :sh07-core-value-kind)
    root-id
    (:sh07/root-syntax-id syntax)
    source-span
    (sh07-core-semantic-span (:span syntax))
    trace?
    (and trace (= root-id (:output-def-syntax-id trace)))
    records
    (atom [])]
   (letfn
    [(walk
      [value path parent-form-id]
      (let
       [source
        (get-in syntax [:sh07/source-occurrences :sources path])
        kind
        (if
         (= :character (:kind source))
         :character
         (sh07-core-value-kind value))
        syntax-id
        (sh07-core-projected-syntax-id
         source-revision-id
         root-id
         path
         kind
         (when trace? trace))
        form-id
        (sh07-core-projected-form-id
         source-revision-id
         root-id
         path
         kind)
        children
        (if (= :character kind) [] (sh07-core-children value))
        child-records
        (mapv
         (fn [index child] (walk child (conj path index) form-id))
         (range)
         children)
        generated-origin
        (cond
         (and trace? (empty? path))
         [{:origin-id (:def-generated-origin-id trace),
           :kind :macro-expansion,
           :from-syntax-id (:input-syntax-id trace),
           :role :introduced-def}]
         (and trace? (= path [2]))
         [{:origin-id (:fn-generated-origin-id trace),
           :kind :macro-expansion,
           :from-syntax-id (:input-syntax-id trace),
           :role :introduced-fn}]
         :else
         [])
        record
        {:source-span source-span,
         :origin-chain [],
         :value
         (sh07-core-neutral-value
          source-path
          source-revision-id
          root-id
          path
          decimal-evidence
          value
          (:sh07/source-occurrences syntax)),
         :syntax-id syntax-id,
         :parent-form-id parent-form-id,
         :kind kind,
         :form-id form-id,
         :scope-id nil,
         :metadata
         (sh05-path-neutral-semantic-value (or (meta value) {})),
         :child-form-ids (mapv :form-id child-records),
         :generated-origin generated-origin}]
       (swap! records conj record)
       record))]
    (let
     [root (walk (:form syntax) [] nil)]
     {:root root, :records @records}))))
 (defn
  source-map-order
  [resolution-artifact syntaxes trees operations]
  (let
   [sh07-core-projected-source-mismatch
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-projected-source-mismatch)
    sh07-core-source-occurrence-context
    (.valAt
     ^clojure.lang.ILookup operations
     :sh07-core-source-occurrence-context)
    contexts
    (sh07-core-source-occurrence-context resolution-artifact syntaxes)
    records
    (vec (mapcat :records trees))
    projected
    (into {} (map (juxt :form-id identity)) records)
    reordered
    (atom {})
    fail
    (fn
     [reason target]
     (throw
      (ex-info
       "SH-07 projected source occurrence does not match C2"
       {:id "C6-ORIGIN",
        :rule "C6-ORIGIN",
        :stage :core-lowering,
        :reason reason,
        :projected-form-id (:form-id target)})))]
   (when-not
    (and
     (= (count records) (count projected))
     (= (count syntaxes) (count trees)))
    (fail :map-source-form-id-bijection nil))
   (doseq
    [[syntax tree] (map vector syntaxes trees)]
    (let
     [context (get contexts (:sh07/root-syntax-id syntax))]
     (letfn
      [(walk
        [id path seen]
        (let
         [target
          (get projected id)
          source
          (get (:sources context) path)
          children
          (:child-form-ids target)]
         (when-not
          (and
           target
           (not (contains? seen id))
           (vector? children)
           (= (count children) (count (set children)))
           (every?
            (fn*
             [p1__162#]
             (= id (:parent-form-id (get projected p1__162#))))
            children))
          (fail :map-projected-child-link target))
         (when
          (= :character (:kind source))
          (when-not
           (and
            (= :character (:kind target))
            (= [] children)
            (= (:value source) (:value target)))
           (fail :character-c2-occurrence-mismatch target)))
         (when-let
          [reason (sh07-core-projected-source-mismatch source target)]
          (fail reason target))
         (when
          (contains? #{:set :map} (:kind target))
          (when-not
           (= (:kind target) (:kind source))
           (fail :map-source-value-or-kind target))
          (when-let
           [order (get (:collection-orders context) path)]
           (let
            [width (if (= :map (:kind target)) 2 1)]
            (when-not
             (= (count children) (* width (count order)))
             (fail :map-source-entry-bijection target))
            (swap!
             reordered
             assoc
             id
             (vec
              (mapcat
               (fn*
                [p1__163#]
                (subvec
                 children
                 (* width p1__163#)
                 (+ width (* width p1__163#))))
               order))))))
         (doseq
          [[index child] (map-indexed vector children)]
          (walk child (conj path index) (conj seen id)))))]
      (walk (get-in tree [:root :form-id]) [] #{}))))
   (mapv
    (fn
     [tree]
     (let
      [change
       (fn
        [record]
        (if-let
         [ids (get @reordered (:form-id record))]
         (assoc record :child-form-ids ids)
         record))]
      {:root (change (:root tree)),
       :records (mapv change (:records tree))}))
    trees)))
 (defn
  parameter-binding-paths
  [parameters parameter-path _operations]
  (loop
   [remaining
    (seq (if (sequential? parameters) parameters []))
    index
    0
    result
    []]
   (if
    (empty? remaining)
    result
    (let
     [item (first remaining)]
     (cond
      (= item ':-)
      (recur (nnext remaining) (+ index 2) result)
      (= item '&)
      (recur (next remaining) (inc index) result)
      (symbol? item)
      (recur
       (next remaining)
       (inc index)
       (conj result {:name item, :path (conj parameter-path index)}))
      :else
      (recur (next remaining) (inc index) result))))))
 (doseq
  [operation
   [#'build-plan-binding!
    #'current-plan-binding!
    #'execute!
    #'plan-identities
    #'source-key
    #'source-path
    #'build-form-tree
    #'children
    #'decimal-descriptor
    #'decimal-evidence
    #'decimal-evidence-complete!
    #'macro-trace
    #'neutral-value
    #'parameter-binding-paths
    #'projected-form-id
    #'projected-source-mismatch
    #'projected-syntax-id
    #'root-syntax-id
    #'semantic-macro-trace
    #'semantic-span
    #'source-map-order
    #'source-occurrence-context
    #'source-path-from-resolution
    #'value-kind]]
  (alter-meta!
   operation
   assoc
   :gravity/namespace-contract
   namespace-contract)))
