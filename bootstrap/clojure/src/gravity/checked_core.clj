(ns
 gravity.checked-core
 "Current checked-core source and plan runtime with explicit temporary seed operations.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {build-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input
      :c-backend-stage2-plan-emitter-source-rule!
      :p15-s23-stage2-compiler-artifact-plan
      :read-forms
      :sh07-core-plan-identities
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none},
    current-plan-binding!
    {:arglists ([operations]),
     :operation-inputs
     [:input :sh07-core-build-binding! :sh07-core-current-source-key],
     :admission :none},
    execute!
    {:arglists ([source-path function arguments operations]),
     :operation-inputs
     [:input
      :p15-s23-c6c10-carrier-validation
      :p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata],
     :admission :none},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input],
     :admission :none},
    source-key
    {:arglists ([operations]),
     :operation-inputs
     [:c-backend-resolve-p15-s23-compiler-source-path
      :p15-s23-stage2-compiler-artifact-source-path
      :sh07-core-source-path
      :sha256-bytes-hex],
     :admission :none},
    source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path],
     :admission :none}},
   :operation-inputs
   [:input
    :c-backend-resolve-p15-s23-compiler-source-path
    :c-backend-stage2-plan-emitter-source-rule!
    :p15-s23-c11-mir-digest
    :p15-s23-c6c10-carrier-validation
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :read-forms
    :sh04-syntax-strip-host-metadata
    :sh07-core-build-binding!
    :sh07-core-current-source-key
    :sh07-core-plan-identities
    :sh07-core-source-path
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
   :governing-contracts
   [D1 D2 D3 D9 L2 C1 C3 C4 C5 C6 C15 BOOT2 BOOT8 TEST2],
   :seed-inputs
   [:*sh07-core-execution-binding*
    :sh07-core-cached-binding
    :sh07-core-development-binding
    :sh07-core-public-function-shapes
    :sh07-core-source-relative-path],
   :unsupported
   [:canonical-c6-admission
    :core-request-projection
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
    :function-and-argument-carriers],
   :namespace gravity.checked-core,
   :diagnostic-ownership
   {:stage
    [:source-plan-contract
     :source-toolchain-drift
     :native-carrier-transport
     :host-containment],
    :catalog :existing-c6-catalog,
    :lowering-semantics :gravity-source},
   :artifact-outputs
   [:current-development-plan-binding
    :source-and-function-identities
    :gravity-function-results],
   :clojure-seed-boundary? true,
   :contract-boundary :checked-core-current-source-and-plan-runtime})

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

(doseq
 [operation
  [#'build-plan-binding!
   #'current-plan-binding!
   #'execute!
   #'plan-identities
   #'source-key
   #'source-path]]
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
