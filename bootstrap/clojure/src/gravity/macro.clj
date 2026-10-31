(ns
 gravity.macro
 "Gravity macro artifacts and pinned execution; explicit seed operations carry host boundaries."
 (:require
  [clojure.set :as set]
  [clojure.java.io :as io]
  [clojure.string :as str]
  [gravity.c2-pass-cache :as c2-pass-cache]
  [gravity.macro-run-storage :as macro-run-storage]))

(def
 ^{:private true} namespace-contract
 '{:public-api
   {source-eligible?
    {:arglists ([source-path module forms operations]),
     :operation-inputs
     [:sh05-authoritative-compiler-source-path?
      :sh05-form-contains-legacy-macro-position?]},
    assemble
    {:arglists
     ([source-path c3-artifact legacy? invocation operations]),
     :operation-inputs
     [:input
      :c3-syntax-validate!
      :p15-s23-stage2-sh02-descriptor-envelope
      :p15-s23-stage2-sh02-descriptor-envelope-verify!
      :parse-module
      :sh03-reader-authenticated-envelope-options
      :sh05-authenticated-c3-parent!
      :sh05-authenticated-source-snapshot!
      :sh05-deep-merge
      :sh05-default-macro-request
      :sh05-defn-form?
      :sh05-expanded-graph
      :sh05-expanded-syntax-object
      :sh05-expanded-syntax-stream-id
      :sh05-macro-artifact-id
      :sh05-macro-artifact-verification
      :sh05-macro-boundary-fail!
      :sh05-macro-capability-based-proof-for-construction
      :sh05-macro-current-binding!
      :sh05-macro-envelope-summary
      :sh05-macro-sh02-descriptor
      :sh05-macro-trace-id
      :sh05-package-trace
      :sh05-run-macro-request!
      :sh05-source-snapshot-stable!
      :sh05-source-syntax-stream]},
    path-neutral-value
    {:arglists ([value operations]),
     :operation-inputs [:sh05-path-neutral-semantic-value]},
    source-snapshot!
    {:arglists ([source-path c2-artifact phase legacy? operations]),
     :operation-inputs [:sh05-macro-boundary-fail!]},
    merge-request
    {:arglists ([left right operations]),
     :operation-inputs [:sh05-deep-merge]},
    current-plan-binding!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh05-macro-boundary-fail!
      :sh05-macro-plan-identities
      :sh05-macro-read-pinned-source!]},
    resolve-expansion-template!
    {:arglists
     ([source-path expansion-template resolved-digests operations]),
     :operation-inputs [:sh05-macro-boundary-fail!]},
    expand
    {:arglists ([source-path binding request operations]),
     :operation-inputs
     [:sh05-contain-coordinator-operation!
      :sh05-macro-boundary-fail!
      :sh05-macro-execute!
      :sh05-macro-raise-rejection!
      :sh05-macro-resolve-digests!
      :sh05-resolve-expansion-template!]},
    expanded-form
    {:arglists ([form operations]),
     :operation-inputs [:sh05-defn-form?]},
    artifact-payload
    {:arglists ([artifact operations]),
     :operation-inputs
     [:sh05-macro-run-semantic-summary
      :sh05-ordered-identity-chunks
      :sh05-path-neutral-semantic-value]},
    compiler-source-path?
    {:arglists ([source-path operations]),
     :operation-inputs [:sh05-macro-resolve-source-path]},
    identity-chunks
    {:arglists ([values projector operations]),
     :operation-inputs [:input]},
    boundary-fail!
    {:arglists
     ([rule source-path missing-field observed facts operations]),
     :operation-inputs [:input :c4-macro-fail! :source-span]},
    trace-identity-input
    {:arglists ([trace operations]),
     :operation-inputs [:sh05-ordered-identity-chunks]},
    c4-artifact-id
    {:arglists ([artifact operations]),
     :operation-inputs
     [:reader-canonical-hash :sh05-c4-compatibility-identity-input]},
    defmacro-form? {:arglists ([form]), :operation-inputs []},
    defn-form? {:arglists ([form]), :operation-inputs []},
    request
    {:arglists
     ([source-path c3-artifact module syntax form operations]),
     :operation-inputs
     [:input :reader-canonical-hash :sh05-semantic-component-id]},
    source-stable!
    {:arglists ([source-path before c2-artifact legacy? operations]),
     :operation-inputs
     [:sh05-authenticated-source-snapshot!
      :sh05-macro-boundary-fail!]},
    resolve-digest-preimage!
    {:arglists ([source-path request resolved-digests operations]),
     :operation-inputs [:sh05-macro-boundary-fail!]},
    contain-operation!
    {:arglists ([source-path responsibility operation operations]),
     :operation-inputs [:input :sh05-macro-boundary-fail!]},
    expanded-syntax-object
    {:arglists ([syntax form run operations]),
     :operation-inputs [:sh05-expanded-form]},
    expanded-graph-valid? {:arglists ([graph]), :operation-inputs []},
    raise-rejection!
    {:arglists ([source-path raw resolved-digests operations]),
     :operation-inputs
     [:c4-macro-fail! :sh05-macro-boundary-fail! :source-span]},
    run-summary {:arglists ([run]), :operation-inputs []},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs [:input :sh05-macro-compute-plan-identities]},
    envelope-summary
    {:arglists ([artifact operations]),
     :operation-inputs [:sh05-ordered-identity-chunks]},
    artifact-id
    {:arglists ([artifact operations]),
     :operation-inputs
     [:p15-s23-c6c10-canonical-digest
      :sh05-macro-artifact-identity-input]},
    semantic-component-id
    {:arglists ([call-site-syntax-id role value operations]),
     :operation-inputs
     [:reader-canonical-hash :sh04-syntax-strip-host-metadata]},
    source-syntax-stream
    {:arglists ([c3-artifact]), :operation-inputs []},
    execute-plan!
    {:arglists ([source-path binding function arguments operations]),
     :operation-inputs
     [:p15-s23-stage2-runtime-execute-function
      :sh04-syntax-strip-host-metadata
      :sh05-macro-boundary-fail!]},
    read-pinned-plan-source!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :sh05-macro-boundary-fail!
      :sh05-macro-resolve-source-path
      :sha256-bytes-hex]},
    resolve-plan-source-path
    {:arglists ([operations]),
     :operation-inputs
     [:input :p15-s23-stage2-compiler-artifact-source-path]},
    artifact-identity-input
    {:arglists ([artifact operations]),
     :operation-inputs [:sh05-macro-artifact-semantic-payload]},
    expanded-graph
    {:arglists ([expanded-stream trace]), :operation-inputs []},
    legacy-position?
    {:arglists ([form operations]),
     :operation-inputs [:sh05-form-contains-legacy-macro-position?]},
    stream-id
    {:arglists ([source-path expanded-stream operations]),
     :operation-inputs
     [:p15-s23-c6c10-canonical-digest
      :sh05-expanded-syntax-stream-identity-input]},
    stream-identity-input
    {:arglists ([expanded-stream operations]),
     :operation-inputs [:sh05-ordered-identity-chunks]},
    resolve-digests!
    {:arglists ([source-path digest-requests operations]),
     :operation-inputs
     [:p15-s23-c6c10-canonical-digest
      :sh05-macro-boundary-fail!
      :sh05-resolve-request-preimage!]},
    envelope-descriptor
    {:arglists ([source-path binding summary operations]),
     :operation-inputs [:input :p15-s23-c6c10-canonical-digest]},
    trace-record
    {:arglists ([run profile target]), :operation-inputs []},
    trace-id
    {:arglists ([source-path trace operations]),
     :operation-inputs
     [:p15-s23-c6c10-canonical-digest
      :sh05-macro-trace-identity-input]},
    compute-plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input]},
    compile-source
    {:arglists ([source-path source-text operations]),
     :operation-inputs
     [:compiler-c2-reader-source-artifact
      :compiler-c4-stage0-legacy-source-artifact
      :parse-module
      :sh05-bounded-authoritative-source?
      :sh05-c4-compatibility-artifact
      :sh05-macro-source-artifact]},
    c4-artifact
    {:arglists ([_source-path sh05 operations]),
     :operation-inputs [:input :sh05-c4-compatibility-artifact-id]},
    replay-report
    {:arglists ([source-path boundary operations]),
     :operation-inputs
     [:sh05-macro-current-binding! :sh05-macro-execute!]},
    c4-identity-input {:arglists ([artifact]), :operation-inputs []},
    accept-syntax-parent!
    {:arglists ([source-path c3-artifact operations]),
     :operation-inputs [:c3-artifact-id :sh05-macro-boundary-fail!]},
    build-plan-binding!
    {:arglists ([request-source operations]),
     :operation-inputs
     [:input
      :c-backend-canonical-value
      :c-backend-stage2-plan-emitter-source-rule!
      :p15-s23-stage2-compiler-artifact-plan
      :sh05-macro-boundary-fail!
      :sh05-macro-compute-plan-identities
      :sh05-macro-read-pinned-source!
      :sha256-hex]},
    verify
    {:arglists
     ^{:line 64, :column 29}
     ([artifact allow-missing-proof? invocation operations]),
     :operation-inputs
     [:input
      :c3-artifact-id
      :c3-syntax-capability-proof
      :c3-syntax-stream-reader-products-authentic?
      :p15-s23-stage2-sh02-descriptor-envelope-verify!
      :parse-module
      :sh03-reader-authenticated-envelope-options
      :sh05-defn-form?
      :sh05-expanded-graph
      :sh05-expanded-graph-valid?
      :sh05-expanded-syntax-object
      :sh05-expanded-syntax-stream-id
      :sh05-macro-artifact-id
      :sh05-macro-current-binding!
      :sh05-macro-envelope-summary
      :sh05-macro-gravity-verifier-report
      :sh05-macro-sh02-descriptor
      :sh05-macro-trace-id
      :sh05-package-trace
      :sh05-source-syntax-stream]}},
   :operation-inputs
   [:c-backend-canonical-value
    :c-backend-stage2-plan-emitter-source-rule!
    :c3-artifact-id
    :c3-syntax-capability-proof
    :c3-syntax-stream-reader-products-authentic?
    :c3-syntax-validate!
    :c4-macro-fail!
    :compiler-c2-reader-source-artifact
    :compiler-c4-stage0-legacy-source-artifact
    :input
    :p15-s23-c11-mir-digest
    :p15-s23-c6c10-canonical-digest
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :p15-s23-stage2-sh02-descriptor-envelope
    :p15-s23-stage2-sh02-descriptor-envelope-verify!
    :parse-module
    :reader-canonical-hash
    :sh03-reader-authenticated-envelope-options
    :sh04-syntax-strip-host-metadata
    :sh05-authenticated-c3-parent!
    :sh05-authenticated-source-snapshot!
    :sh05-authoritative-compiler-source-path?
    :sh05-bounded-authoritative-source?
    :sh05-c4-compatibility-artifact
    :sh05-c4-compatibility-artifact-id
    :sh05-c4-compatibility-identity-input
    :sh05-contain-coordinator-operation!
    :sh05-deep-merge
    :sh05-default-macro-request
    :sh05-defn-form?
    :sh05-expanded-form
    :sh05-expanded-graph
    :sh05-expanded-graph-valid?
    :sh05-expanded-syntax-object
    :sh05-expanded-syntax-stream-id
    :sh05-expanded-syntax-stream-identity-input
    :sh05-form-contains-legacy-macro-position?
    :sh05-macro-artifact-id
    :sh05-macro-artifact-identity-input
    :sh05-macro-artifact-semantic-payload
    :sh05-macro-artifact-verification
    :sh05-macro-boundary-fail!
    :sh05-macro-capability-based-proof-for-construction
    :sh05-macro-compute-plan-identities
    :sh05-macro-current-binding!
    :sh05-macro-envelope-summary
    :sh05-macro-execute!
    :sh05-macro-gravity-verifier-report
    :sh05-macro-plan-identities
    :sh05-macro-raise-rejection!
    :sh05-macro-read-pinned-source!
    :sh05-macro-resolve-digests!
    :sh05-macro-resolve-source-path
    :sh05-macro-run-semantic-summary
    :sh05-macro-sh02-descriptor
    :sh05-macro-source-artifact
    :sh05-macro-trace-id
    :sh05-macro-trace-identity-input
    :sh05-ordered-identity-chunks
    :sh05-package-trace
    :sh05-path-neutral-semantic-value
    :sh05-resolve-expansion-template!
    :sh05-resolve-request-preimage!
    :sh05-run-macro-request!
    :sh05-semantic-component-id
    :sh05-source-snapshot-stable!
    :sh05-source-syntax-stream
    :sha256-bytes-hex
    :sha256-hex
    :source-span],
   :dependency-direction
   {:requires
    [clojure.core
     clojure.java.io
     clojure.set
     clojure.string
     gravity.c2-pass-cache
     gravity.macro-run-storage],
    :forbidden [gravity.bootstrap gravity.cli gravity.release],
    :host-operations :explicit-seed-ports},
   :governing-contracts
   ["D1"
    "D2"
    "D3"
    "D9"
    "L4"
    "L12"
    "L16"
    "SAFE12"
    "C1"
    "C3"
    "C4"
    "C15"],
   :seed-inputs
   [:c4-macro-diagnostic-ids
    :c4-macro-governing-document
    :sh05-identity-chunk-width
    :sh05-macro-adapter-contract
    :sh05-macro-artifact-keys
    :sh05-macro-cached-binding
    :sh05-macro-envelope-stage
    :sh05-macro-expected-function-count
    :sh05-macro-expected-function-names-hash
    :sh05-macro-expected-function-shapes-hash
    :sh05-macro-expected-functions-semantic-hash
    :sh05-macro-expected-plan-semantic-hash
    :sh05-macro-expected-source-byte-count
    :sh05-macro-expected-source-content-hash
    :sh05-macro-pinned-plan-identities
    :sh05-macro-public-function-hashes
    :sh05-macro-public-function-shapes
    :sh05-macro-sealed-artifact-kind
    :sh05-macro-source-relative-path
    :sh05-macro-version],
   :unsupported
   [:legacy-macro-engine
    :full-macro-accounting-removal
    :release
    :migration
    :self-hosting
    :seed-retirement],
   :artifact-inputs
   [:pinned-gravity-source
    :compiler-plan
    :syntax-request
    :ordered-digest-requests
    :explicit-seed-operations
    :authenticated-c3-artifact
    :current-source-snapshot
    :expanded-syntax
    :macro-artifact],
   :namespace gravity.macro,
   :diagnostic-ownership
   {:stage :macro-expansion,
    :document "C4",
    :rules ["C4-TRACE" "C4-RETURN" "C4-DEPTH"],
    :interruption :preserved,
    :host-errors :contained},
   :artifact-outputs
   [:plan-binding
    :resolved-expansion
    :ordered-digests
    :template-verification
    :resolved-verification
    :macro-artifact
    :expanded-syntax
    :trace
    :envelope-descriptor
    :verification-report],
   :contract-boundary :gravity-macro-artifacts-and-pinned-execution})

(defn
 boundary-fail!
 [rule source-path missing-field observed facts operations]
 (let
  [input
   ^{:line 61, :column 54}
   (.valAt ^clojure.lang.ILookup operations :input)
   c4-macro-fail!
   (.valAt ^clojure.lang.ILookup operations :c4-macro-fail!)
   source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)]
  (c4-macro-fail!
   rule
   source-path
   {:source-span (source-span source-path 0),
    :macro 'gravity.core/defn,
    :macro-version (input :sh05-macro-version),
    :profile :meta,
    :target :jvm,
    :build-effects [],
    :capabilities [],
    :hygiene {:marks [], :lexical-scopes []}}
   {:severity :error,
    :missing-fields [missing-field],
    :facts (merge {:sh05-boundary :gravity-macro-plan} facts),
    :observed observed})))

(defn
 resolve-plan-source-path
 [operations]
 (let
  [input
   ^{:line 61, :column 54}
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
        (input :sh05-macro-source-relative-path))]
      (if
       (.isFile candidate)
       (.getPath candidate)
       (recur (.getParentFile directory))))))
   (input :sh05-macro-source-relative-path))))

(defn
 read-pinned-plan-source!
 [request-source operations]
 (let
  [input
   ^{:line 61, :column 54}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-macro-resolve-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-resolve-source-path)
   sha256-bytes-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)
   source-path
   (sh05-macro-resolve-source-path)
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
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      request-source
      :pinned-macro-source-readable
      source-path
      {:cause-message (.getMessage error)})))
   _
   (when-not
    (and
     attributes
     (.isRegularFile attributes)
     (=
      (long (input :sh05-macro-expected-source-byte-count))
      (.size attributes)))
    (sh05-macro-boundary-fail!
     "C4-TRACE"
     request-source
     :exact-pinned-macro-source
     source-path
     {:expected-byte-count
      (input :sh05-macro-expected-source-byte-count),
      :observed-byte-count (when attributes (.size attributes))}))
   bytes
   (try
    (java.nio.file.Files/readAllBytes nio-path)
    (catch
     Exception
     error
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      request-source
      :pinned-macro-source-bytes
      source-path
      {:cause-message (.getMessage error)})))
   content-hash
   (str "sha256:" (sha256-bytes-hex bytes))]
  (when-not
   (and
    (= (input :sh05-macro-expected-source-byte-count) (alength bytes))
    (= (input :sh05-macro-expected-source-content-hash) content-hash))
   (sh05-macro-boundary-fail!
    "C4-TRACE"
    request-source
    :pinned-macro-source-identity
    source-path
    {:observed-content-hash content-hash,
     :observed-byte-count (alength bytes)}))
  {:source-path source-path,
   :source-text
   (String. bytes java.nio.charset.StandardCharsets/UTF_8),
   :source-byte-count (alength bytes),
   :source-content-hash content-hash}))

(defn
 compute-plan-identities
 [plan operations]
 (let
  [input
   ^{:line 61, :column 54}
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
    (keys (input :sh05-macro-public-function-hashes))),
   :public-function-shapes
   (select-keys
    shapes
    (keys (input :sh05-macro-public-function-shapes)))}))

(defn
 plan-identities
 [plan operations]
 (let
  [input
   ^{:line 61, :column 54}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh05-macro-compute-plan-identities
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-compute-plan-identities)
   [pinned-plan pinned-identities]
   @(input :sh05-macro-pinned-plan-identities)]
  (if
   (and pinned-plan (identical? pinned-plan plan))
   pinned-identities
   (sh05-macro-compute-plan-identities plan))))

(defn
 build-plan-binding!
 [request-source operations]
 (let
  [input
   ^{:line 61, :column 54}
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
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-macro-compute-plan-identities
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-compute-plan-identities)
   sh05-macro-read-pinned-source!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-read-pinned-source!)
   sha256-hex
   (.valAt ^clojure.lang.ILookup operations :sha256-hex)
   source
   (sh05-macro-read-pinned-source! request-source)
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
   (sh05-macro-compute-plan-identities raw-plan)]
  (when-not
   (and
    (= :gravity/stage2-compiler-artifact-plan (:kind raw-plan))
    (true? (:compiler-artifact-plan? raw-plan))
    (= 'gravity.macro (get-in raw-plan [:module :module]))
    (= :meta (get-in raw-plan [:module :profile]))
    (= :jvm (get-in raw-plan [:module :target]))
    (= #{} (get-in raw-plan [:module :effects]))
    (= #{} (get-in raw-plan [:module :capabilities]))
    (= :safe (get-in raw-plan [:module :safety]))
    (=
     (input :sh05-macro-expected-plan-semantic-hash)
     (:plan-semantic-hash identities))
    (=
     (input :sh05-macro-expected-functions-semantic-hash)
     (:functions-semantic-hash identities))
    (=
     (input :sh05-macro-expected-function-count)
     (:function-count identities))
    (=
     (input :sh05-macro-expected-function-names-hash)
     (:function-names-hash identities))
    (=
     (input :sh05-macro-expected-function-shapes-hash)
     (:function-shapes-hash identities))
    (=
     (input :sh05-macro-public-function-hashes)
     (:public-function-hashes identities))
    (=
     (input :sh05-macro-public-function-shapes)
     (:public-function-shapes identities)))
   (sh05-macro-boundary-fail!
    "C4-TRACE"
    request-source
    :pinned-macro-plan-and-functions
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
   (reset!
    (input :sh05-macro-pinned-plan-identities)
    [plan identities])
   (merge
    source
    identities
    {:artifact :gravity/sh05-pinned-macro-plan-binding,
     :status :complete,
     :semantic-authority :gravity-source,
     :compiled-by :clojure-stage0-seed,
     :executed-by :clojure-stage2-generic-rule-runner,
     :generic-bridge-residual? true,
     :self-hosted? false,
     :plan plan}))))

(defn
 current-plan-binding!
 [request-source operations]
 (let
  [input
   ^{:line 61, :column 54}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-macro-plan-identities
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-plan-identities)
   sh05-macro-read-pinned-source!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-read-pinned-source!)
   fresh
   (sh05-macro-read-pinned-source! request-source)
   binding
   @(input :sh05-macro-cached-binding)
   identities
   (sh05-macro-plan-identities (:plan binding))]
  (when-not
   (and
    (= (:source-byte-count fresh) (:source-byte-count binding))
    (= (:source-content-hash fresh) (:source-content-hash binding))
    (=
     (select-keys
      binding
      [:plan-semantic-hash
       :functions-semantic-hash
       :function-count
       :function-names-hash
       :function-shapes-hash
       :public-function-hashes
       :public-function-shapes])
     identities))
   (sh05-macro-boundary-fail!
    "C4-TRACE"
    request-source
    :fresh-macro-source-and-plan-binding
    binding
    {}))
  binding))

(defn
 execute-plan!
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
   sh05-macro-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-boundary-fail!)]
  (try
   (sh04-syntax-strip-host-metadata
    (p15-s23-stage2-runtime-execute-function
     {:engine :gravity-sh05-pinned-macro-runner,
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
    (sh05-macro-boundary-fail!
     "C4-DEPTH"
     source-path
     :bounded-macro-host-stack
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    AssertionError
    error
    (sh05-macro-boundary-fail!
     "C4-RETURN"
     source-path
     :contained-macro-assertion
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    LinkageError
    error
    (sh05-macro-boundary-fail!
     "C4-RETURN"
     source-path
     :contained-macro-linkage
     function
     {:contained-host-error (.getName (class error))}))
   (catch
    clojure.lang.ExceptionInfo
    error
    (sh05-macro-boundary-fail!
     "C4-RETURN"
     source-path
     :contained-macro-runtime-diagnostic
     function
     {:contained-diagnostic (:id (ex-data error))}))
   (catch
    Exception
    error
    (sh05-macro-boundary-fail!
     "C4-RETURN"
     source-path
     :contained-macro-host-failure
     function
     {:contained-host-error (.getName (class error)),
      :cause-message (.getMessage error)})))))

(defn
 resolve-digest-preimage!
 [source-path request resolved-digests operations]
 (let
  [sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   ordinal
   (:ordinal request)
   purpose
   (:purpose request)
   preimage
   (:preimage request)]
  (case
   purpose
   :sh05-expanded-syntax-id
   (do
    (when-not
     (and (= 0 ordinal) (empty? resolved-digests))
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      :exact-expanded-syntax-request
      request
      {:resolved-digest-count (count resolved-digests)}))
    preimage)
   :sh05-expansion-provenance-binding-id
   (do
    (when-not
     (and
      (= 1 ordinal)
      (= 1 (count resolved-digests))
      (= {:digest-ref 0} (:semantic-artifact-id preimage)))
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      :declared-provenance-artifact-reference
      (:semantic-artifact-id preimage)
      {:resolved-digest-count (count resolved-digests)}))
    (assoc preimage :semantic-artifact-id (first resolved-digests)))
   :sh05-macro-diagnostic-id
   (do
    (when-not
     (and (= 0 ordinal) (empty? resolved-digests))
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      :exact-macro-diagnostic-request
      request
      {:resolved-digest-count (count resolved-digests)}))
    preimage)
   (sh05-macro-boundary-fail!
    "C4-TRACE"
    source-path
    :known-macro-digest-request-purpose
    purpose
    {:ordinal ordinal}))))

(defn
 resolve-digests!
 [source-path digest-requests operations]
 (let
  [p15-s23-c6c10-canonical-digest
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-canonical-digest)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-resolve-request-preimage!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-resolve-request-preimage!)]
  (reduce
   (fn
    [resolved request]
    (let
     [ordinal
      (:ordinal request)
      preimage
      (sh05-resolve-request-preimage! source-path request resolved)]
     (when-not
      (= ordinal (count resolved))
      (sh05-macro-boundary-fail!
       "C4-TRACE"
       source-path
       :ordered-macro-digest-requests
       request
       {:resolved-count (count resolved)}))
     (conj
      resolved
      (p15-s23-c6c10-canonical-digest source-path preimage))))
   []
   digest-requests)))

(defn
 raise-rejection!
 [source-path raw resolved-digests operations]
 (let
  [c4-macro-fail!
   (.valAt ^clojure.lang.ILookup operations :c4-macro-fail!)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   source-span
   (.valAt ^clojure.lang.ILookup operations :source-span)
   diagnostic
   (let
    [template (first (:diagnostics raw))]
    (when-not
     (and
      (= 1 (count resolved-digests))
      (= {:digest-ref 0} (:diagnostic-id-request template)))
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      :declared-diagnostic-id-reference
      (:diagnostic-id-request template)
      {:resolved-digest-count (count resolved-digests)}))
    (assoc template :diagnostic-id-request (first resolved-digests)))
   rule
   (:rule diagnostic)]
  (c4-macro-fail!
   rule
   source-path
   {:source-span (source-span source-path 0),
    :macro (:macro-name diagnostic),
    :macro-version (:macro-version diagnostic),
    :profile (:profile diagnostic),
    :target (:target diagnostic),
    :build-effects (:build-effects diagnostic),
    :capabilities (:capabilities diagnostic),
    :hygiene (:hygiene-context diagnostic)}
   {:severity (:severity diagnostic),
    :diagnostic-family :c4-macro-expansion,
    :stage :macro-expansion,
    :facts (:facts diagnostic),
    :remediation (:remediation diagnostic),
    :gravity-diagnostic diagnostic,
    :diagnostic-id (get diagnostic :diagnostic-id-request)})))

(defn
 contain-operation!
 [source-path responsibility operation operations]
 (let
  [input
   ^{:line 61, :column 54}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh05-macro-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-boundary-fail!)]
  (try
   (operation)
   (catch
    InterruptedException
    interrupted
    (.interrupt (Thread/currentThread))
    (throw interrupted))
   (catch
    StackOverflowError
    error
    (sh05-macro-boundary-fail!
     "C4-DEPTH"
     source-path
     responsibility
     nil
     {:contained-host-error (.getName (class error))}))
   (catch
    clojure.lang.ExceptionInfo
    error
    (if
     (contains?
      (set (input :c4-macro-diagnostic-ids))
      (:id (ex-data error)))
     (throw error)
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      responsibility
      nil
      {:contained-diagnostic (:id (ex-data error))})))
   (catch
    Throwable
    error
    (sh05-macro-boundary-fail!
     "C4-RETURN"
     source-path
     responsibility
     nil
     {:contained-host-error (.getName (class error)),
      :cause-message (.getMessage error)})))))

(defn
 resolve-expansion-template!
 [source-path expansion-template resolved-digests operations]
 (let
  [sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   output-reference
   {:digest-ref 0}
   provenance-reference
   {:digest-ref 1}
   output-reference-slots
   [[:artifact-id]
    [:output-syntax-id]
    [:macro-expansion-trace 0 :output-syntax-ids 0]
    [:metadata-ledger 0 :output-syntax-id]
    [:generated-origin-source-map 0 :output-syntax-id]]]
  (when-not
   (and
    (= 2 (count resolved-digests))
    (every?
     (fn*
      [p1__45134#]
      (= output-reference (get-in expansion-template p1__45134#)))
     output-reference-slots)
    (=
     provenance-reference
     (:provenance-binding-id expansion-template)))
   (sh05-macro-boundary-fail!
    "C4-TRACE"
    source-path
    :declared-macro-output-reference-slots
    {:output-references
     (mapv
      (fn* [p1__45135#] (get-in expansion-template p1__45135#))
      output-reference-slots),
     :provenance-reference (:provenance-binding-id expansion-template)}
    {:resolved-digest-count (count resolved-digests)}))
  (->
   (reduce
    (fn
     [template slot]
     (assoc-in template slot (first resolved-digests)))
    expansion-template
    output-reference-slots)
   (assoc :provenance-binding-id (second resolved-digests)))))

(defn
 expand
 [source-path binding request operations]
 (let
  [sh05-contain-coordinator-operation!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-contain-coordinator-operation!)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-macro-execute!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-execute!)
   sh05-macro-raise-rejection!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-raise-rejection!)
   sh05-macro-resolve-digests!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-resolve-digests!)
   sh05-resolve-expansion-template!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-resolve-expansion-template!)
   raw
   (sh05-macro-execute!
    source-path
    binding
    'sh05-expand-macro-template
    [request])
   digest-requests
   (:digest-requests raw)
   resolved-digests
   (sh05-contain-coordinator-operation!
    source-path
    :bounded-ordered-macro-digest-resolution
    (fn*
     []
     (sh05-macro-resolve-digests! source-path digest-requests)))]
  (if
   (= :rejected (:status raw))
   (sh05-contain-coordinator-operation!
    source-path
    :structured-macro-rejection-packaging
    (fn*
     []
     (sh05-macro-raise-rejection! source-path raw resolved-digests)))
   (let
    [template-verification
     (sh05-macro-execute!
      source-path
      binding
      'sh05-verify-macro-template
      [(:expansion-template raw) digest-requests])
     resolved-expansion
     (sh05-contain-coordinator-operation!
      source-path
      :bounded-macro-reference-resolution
      (fn*
       []
       (sh05-resolve-expansion-template!
        source-path
        (:expansion-template raw)
        resolved-digests)))
     resolved-verification
     (sh05-macro-execute!
      source-path
      binding
      'sh05-verify-macro-resolved
      [resolved-expansion digest-requests resolved-digests])]
    (when-not
     (and
      (= :passed (:status template-verification))
      (= :passed (:status resolved-verification)))
     (sh05-macro-boundary-fail!
      "C4-TRACE"
      source-path
      :fresh-gravity-macro-verification
      {:template template-verification,
       :resolved resolved-verification}
      {}))
    {:raw-template-result
     (select-keys
      raw
      [:artifact :schema-version :status :expansion-template]),
     :digest-requests digest-requests,
     :resolved-digests resolved-digests,
     :resolved-expansion resolved-expansion,
     :template-verification
     (select-keys
      template-verification
      [:artifact :schema-version :status :rule]),
     :resolved-verification
     (select-keys
      resolved-verification
      [:artifact :schema-version :status :rule])}))))

(defn
 merge-request
 [left right operations]
 (let
  [sh05-deep-merge
   (.valAt ^clojure.lang.ILookup operations :sh05-deep-merge)]
  (merge-with
   (fn
    [left-value right-value]
    (if
     (and (map? left-value) (map? right-value))
     (sh05-deep-merge left-value right-value)
     right-value))
   left
   right)))

(defn defn-form? [form] (and (seq? form) (= 'defn (first form))))

(defn
 defmacro-form?
 [form]
 (and (seq? form) (= 'defmacro (first form))))

(defn
 semantic-component-id
 [call-site-syntax-id role value operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh04-syntax-strip-host-metadata
   (.valAt
    ^clojure.lang.ILookup operations
    :sh04-syntax-strip-host-metadata)]
  (reader-canonical-hash
   {:domain :gravity/sh05-input-syntax-component-v1,
    :call-site-syntax-id call-site-syntax-id,
    :role role,
    :semantic-value (sh04-syntax-strip-host-metadata value)})))

(defn
 request
 [source-path c3-artifact module syntax form operations]
 (let
  [input
   ^{:line 78, :column 42}
   (.valAt ^clojure.lang.ILookup operations :input)
   reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh05-semantic-component-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-semantic-component-id)
   [_ name parameters & body]
   form
   call-site-syntax-id
   (:syntax/id syntax)
   reader-binding
   (get-in
    c3-artifact
    [:gravity-syntax-boundary :reader-semantic-binding])
   reader-source-revision
   (get-in
    c3-artifact
    [:gravity-syntax-boundary :reader-source-revision])
   macro-environment-id
   (reader-canonical-hash
    {:domain :gravity/sh05-bootstrap-macro-environment-v1,
     :macros
     [{:name 'defn,
       :version (input :sh05-macro-version),
       :phase :macro-expansion,
       :build-effects [],
       :capabilities []}]})
   base-policy
   {:granted-build-effects [],
    :generated-profile (:profile module),
    :expansion-depth 0,
    :requested-capabilities [],
    :granted-capabilities [],
    :declared-build-effects [],
    :hidden-captures [],
    :generated-unsafe? false,
    :capture-policy :explicit-only,
    :output-node-count (+ 4 (count body)),
    :requested-build-effects [],
    :allowed-profiles [(:profile module)],
    :unsafe-declared? false,
    :macro-return-kind :syntax}
   build-policy-id
   (reader-canonical-hash
    {:domain :gravity/sh05-build-policy-v1, :policy base-policy})
   trace-replay-id
   (reader-canonical-hash
    {:domain :gravity/sh05-trace-replay-v1,
     :call-site-syntax-id call-site-syntax-id,
     :macro-environment-id macro-environment-id,
     :build-policy-id build-policy-id})
   semantic-span
   (let
    [span
     (select-keys
      (:span syntax)
      [:file
       :byte-start
       :byte-end
       :scalar-start
       :scalar-end
       :line-start
       :column-start
       :line-end
       :column-end])]
    (assoc span :source (:file span)))]
  {:call
   {:hygiene (select-keys (:hygiene syntax) [:marks :lexical-scopes]),
    :macro-version (input :sh05-macro-version),
    :call-site-syntax-id call-site-syntax-id,
    :origin-chain (vec (:origin syntax)),
    :name-syntax-id
    (sh05-semantic-component-id call-site-syntax-id :name name),
    :definition-span
    {:source (input :sh05-macro-source-relative-path),
     :byte-start 0,
     :byte-end (input :sh05-macro-expected-source-byte-count)},
    :semantic-call-span semantic-span,
    :body-syntax-ids
    (mapv
     (fn*
      [p1__174# p2__175#]
      (sh05-semantic-component-id
       call-site-syntax-id
       [:body p1__174#]
       p2__175#))
     (range)
     body),
    :parameter-vector-syntax-id
    (sh05-semantic-component-id
     call-site-syntax-id
     :parameters
     parameters),
    :call-site-span (:span syntax),
    :macro-name 'defn,
    :definition-syntax-id
    (reader-canonical-hash
     {:domain :gravity/sh05-defn-macro-definition-v1,
      :macro-version (input :sh05-macro-version)}),
    :metadata (or (:metadata syntax) {})},
   :reader-source-revision reader-source-revision,
   :schema-version 1,
   :reader-binding reader-binding,
   :policy
   (assoc base-policy :recorded-trace-replay-id trace-replay-id),
   :source-revision-id (:revision-id reader-source-revision),
   :context
   {:phase :macro-expansion,
    :profile (:profile module),
    :target (:target module),
    :macro-environment-id macro-environment-id,
    :build-policy-id build-policy-id,
    :trace-replay-id trace-replay-id},
   :provenance {:actual-source-path source-path},
   :reader-semantic-binding-id (:semantic-binding-id reader-binding),
   :artifact :gravity/sh05-authenticated-c3-macro-request}))

(defn
 source-syntax-stream
 [c3-artifact]
 (filterv
  (fn*
   [p1__176#]
   (not= :generated-form (get-in p1__176# [:form :kind])))
  (:syntax-object-stream c3-artifact)))

(defn
 expanded-form
 [form operations]
 (let
  [sh05-defn-form?
   (.valAt ^clojure.lang.ILookup operations :sh05-defn-form?)]
  (if
   (sh05-defn-form? form)
   (let
    [[_ name parameters & body] form]
    (list 'def name (apply list 'fn parameters body)))
   form)))

(defn
 trace-record
 [run profile target]
 (let
  [expansion
   (:resolved-expansion run)
   trace
   (first (:macro-expansion-trace expansion))
   origin
   (get-in expansion [:generated-origin-source-map 0 :origin])]
  {:capabilities (:capabilities trace),
   :hygiene (:hygiene trace),
   :macro-version (:macro-version trace),
   :diagnostics [],
   :build-effects (:build-effects trace),
   :definition-span (:definition-span trace),
   :macro 'defn,
   :call-site-span (:call-site-span trace),
   :artifact :gravity/macro-expansion-step,
   :target target,
   :trace-replay-id (:trace-replay-id trace),
   :step (:step trace),
   :input-syntax-id (first (:input-syntax-ids trace)),
   :profile profile,
   :generated-origin [origin],
   :output-syntax-id (first (:output-syntax-ids trace))}))

(defn
 expanded-syntax-object
 [syntax form run operations]
 (let
  [sh05-expanded-form
   (.valAt ^clojure.lang.ILookup operations :sh05-expanded-form)]
  (if
   run
   (let
    [expansion
     (:resolved-expansion run)
     output-id
     (:output-syntax-id expansion)
     origin
     (get-in expansion [:generated-origin-source-map 0 :origin])]
    (->
     syntax
     (assoc
      :artifact
      :gravity/expanded-syntax-object
      :form
      (sh05-expanded-form form)
      :syntax/id
      output-id
      :expanded-syntax-id
      output-id
      :phase
      :macro-expanded)
     (update :origin (fn* [p1__177#] (conj (vec p1__177#) origin)))))
   (assoc
    syntax
    :artifact
    :gravity/expanded-syntax-object
    :form
    form
    :expanded-syntax-id
    (:syntax/id syntax)
    :phase
    :macro-expanded))))

(defn
 expanded-graph
 [expanded-stream trace]
 (let
  [nodes
   (mapv
    (fn
     [syntax]
     {:id (:expanded-syntax-id syntax),
      :kind :expanded-syntax,
      :form (:form syntax)})
    expanded-stream)
   node-ids
   (set (map :id nodes))
   edges
   (mapv
    (fn
     [step]
     {:from (:input-syntax-id step), :to (:output-syntax-id step)})
    trace)]
  {:artifact :gravity/sh05-expanded-syntax-graph,
   :nodes nodes,
   :edges edges,
   :node-ids node-ids,
   :status :complete}))

(defn
 expanded-graph-valid?
 [graph]
 (let
  [node-ids (set (map :id (:nodes graph)))]
  (and
   (= node-ids (:node-ids graph))
   (every?
    (fn
     [edge]
     (and
      (contains? node-ids (:to edge))
      (or (contains? node-ids (:from edge)) (string? (:from edge)))))
    (:edges graph)))))

(defn
 envelope-descriptor
 [source-path binding summary operations]
 (let
  [input
   ^{:line 78, :column 42}
   (.valAt ^clojure.lang.ILookup operations :input)
   p15-s23-c6c10-canonical-digest
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-canonical-digest)
   projection-name
   :macro-product-identities
   fact-name
   :macro-product-binding
   identity-name
   :macro-result
   identity-domain
   :gravity/sh05-macro-result-identity-v2
   evidence-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:domain :gravity/sh05-macro-envelope-evidence-v2,
     :summary summary,
     :plan-semantic-hash (:plan-semantic-hash binding)})
   identity-preimage
   {:summary summary}
   observed-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:domain identity-domain, :semantic-input identity-preimage})
   fact-value
   {:family fact-name, :entries [summary]}
   artifact-id
   (p15-s23-c6c10-canonical-digest
    source-path
    {:macro-result (:artifact-id summary)})]
  {:proof-composite
   {:proof-records [{:proof-id evidence-id, :status :checked}],
    :proof-certificate-table {evidence-id {:status :checked}},
    :proof-summary {:required 1, :checked 1},
    :proof-usage [{:proof-id evidence-id, :used-by :macro-products}]},
   :effect-capability-relation
   {:effect-facts {:declared #{}, :observed #{}},
    :capability-facts {:required #{}, :granted #{}},
    :capability-proof-facts {:proof-ids [evidence-id]},
    :effect-order [],
    :provider-selections [],
    :grant-scopes []},
   :semantic-projections
   [{:name projection-name,
     :role :complete-macro-product-identities,
     :entry-count (count summary),
     :value summary}],
   :stage (input :sh05-macro-envelope-stage),
   :lineage
   [{:stage :sh05-macro,
     :artifact-kind :gravity/sh05-macro-expansion-artifact,
     :semantic-id (:artifact-id summary),
     :artifact-id artifact-id,
     :verification-id evidence-id,
     :relation :produced-from-gravity-macro}],
   :identity-subjects
   [{:name identity-name,
     :domain identity-domain,
     :preimage identity-preimage,
     :observed-id observed-id}],
   :projection-contract
   {:contract-kind :gravity/sh05-macro-product-envelope-contract,
    :contract-version 1,
    :profile :meta,
    :target :jvm,
    :required-semantic-projections [projection-name],
    :required-fact-families [fact-name],
    :required-identity-subjects [identity-name]},
   :schema-version 2,
   :source-revision
   {:source-content-hash (:source-content-hash binding),
    :builder-function 'sh05-expand-macro-template,
    :builder-semantic-hash
    (get
     (input :sh05-macro-public-function-hashes)
     'sh05-expand-macro-template),
    :source-byte-count (:source-byte-count binding),
    :functions-semantic-hash (:functions-semantic-hash binding),
    :source-language :gravity,
    :function-shapes (input :sh05-macro-public-function-shapes),
    :owner :sh05-macro,
    :logical-source-path (input :sh05-macro-source-relative-path),
    :plan-semantic-hash (:plan-semantic-hash binding)},
   :actual-path-provenance
   {:source-path source-path,
    :workspace-root (System/getProperty "user.dir"),
    :invocation-root (System/getProperty "user.dir")},
   :preservation
   {:requires [fact-name],
    :preserves [fact-name],
    :invalidates [],
    :regenerates [],
    :residual-checks
    [:identity-subject-equality :digest-graph-reachability]},
   :fact-transitions
   [{:name fact-name,
     :disposition :preserved,
     :input fact-value,
     :output fact-value,
     :input-count (count fact-value),
     :output-count (count fact-value),
     :evidence-ids [evidence-id]}],
   :reference-closure
   {:root-id "sh05-macro-result",
    :node-ids ["sh05-macro-result"],
    :proof-reference-ids [evidence-id],
    :observed-maximum-depth 0,
    :observed-node-count 1,
    :observed-edge-count 0,
    :runtime-check-reference-ids [],
    :fact-reference-ids [evidence-id],
    :edges [],
    :origin-reference-ids []},
   :artifact :gravity/private-authenticated-envelope-descriptor,
   :artifact-kind (input :sh05-macro-sealed-artifact-kind)}))

(defn
 path-neutral-value
 [value operations]
 (let
  [sh05-path-neutral-semantic-value
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-path-neutral-semantic-value)]
  (cond
   (map? value)
   (let
    [clean
     (into
      {}
      (keep
       (fn
        [[key item]]
        (when-not
         (contains?
          #{:workspace-root :invocation-root :actual-source-path}
          key)
         [key (sh05-path-neutral-semantic-value item)])))
      value)]
    (if
     (and
      (contains? clean :byte-start)
      (or (contains? clean :source) (contains? clean :file)))
     (let
      [semantic-source (or (:file clean) (:source clean))]
      (cond->
       clean
       (contains? clean :source)
       (assoc :source semantic-source)
       (contains? clean :file)
       (assoc :file semantic-source)))
     clean))
   (vector? value)
   (mapv sh05-path-neutral-semantic-value value)
   (set? value)
   (into #{} (map sh05-path-neutral-semantic-value) value)
   (seq? value)
   (apply list (map sh05-path-neutral-semantic-value value))
   :else
   value)))

(defn
 identity-chunks
 [values projector operations]
 (let
  [input
   ^{:line 78, :column 42}
   (.valAt ^clojure.lang.ILookup operations :input)]
  (->>
   values
   (map-indexed vector)
   (partition-all (input :sh05-identity-chunk-width))
   (map-indexed
    (fn
     [chunk-index entries]
     {:chunk-index chunk-index,
      :start-ordinal (first (first entries)),
      :item-count (count entries),
      :items
      (mapv
       (fn [[ordinal value]] (projector ordinal value))
       entries)}))
   vec)))

(defn
 run-summary
 [run]
 (let
  [raw
   (:raw-template-result run)
   resolved
   (:resolved-expansion run)
   trace
   (first (:macro-expansion-trace resolved))]
  {:raw-result
   {:artifact (:artifact raw),
    :schema-version (:schema-version raw),
    :status (:status raw)},
   :digest-contract
   (mapv
    (fn* [p1__178#] (select-keys p1__178# [:ordinal :purpose]))
    (:digest-requests run)),
   :semantic-resolved-digest (first (:resolved-digests run)),
   :resolved-expansion
   (select-keys
    resolved
    [:artifact
     :schema-version
     :artifact-id
     :status
     :macro
     :macro-version
     :input-syntax-id
     :output-syntax-id
     :semantic-binding
     :profile
     :target
     :phase
     :bounds]),
   :trace-replay-id (:trace-replay-id trace),
   :template-verification (:template-verification run),
   :resolved-verification (:resolved-verification run)}))

(defn
 artifact-payload
 [artifact operations]
 (let
  [sh05-macro-run-semantic-summary
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-run-semantic-summary)
   sh05-ordered-identity-chunks
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-ordered-identity-chunks)
   sh05-path-neutral-semantic-value
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-path-neutral-semantic-value)
   boundary
   (:gravity-macro-boundary artifact)]
  (sh05-path-neutral-semantic-value
   {:plan-binding
    (select-keys
     (:plan-binding boundary)
     [:artifact
      :status
      :semantic-authority
      :compiled-by
      :executed-by
      :generic-bridge-residual?
      :self-hosted?
      :source-language
      :source-byte-count
      :source-content-hash
      :plan-semantic-hash
      :functions-semantic-hash
      :function-count
      :function-names-hash
      :function-shapes-hash
      :public-function-hashes
      :public-function-shapes]),
    :expanded-defn-count (:expanded-defn-count artifact),
    :macro-expansion-trace-id (:macro-expansion-trace-id artifact),
    :diagnostics (:diagnostics artifact),
    :expanded-syntax-stream-id (:expanded-syntax-stream-id artifact),
    :task (:task artifact),
    :expansion-run-count (count (:expansion-runs boundary)),
    :boundary-contract
    (select-keys
     boundary
     [:slice
      :owner
      :adapter-contract
      :target-source-reread?
      :clojure-adapter-residual?
      :self-hosted?]),
    :document-set (:document-set artifact),
    :governing-document (:governing-document artifact),
    :execution-boundary (:execution-boundary artifact),
    :macro-environment (:macro-environment artifact),
    :status (:status artifact),
    :slice (:slice artifact),
    :kind (:kind artifact),
    :expansion-run-chunks
    (sh05-ordered-identity-chunks
     (:expansion-runs boundary)
     (fn
      [ordinal run]
      {:ordinal ordinal,
       :summary (sh05-macro-run-semantic-summary run)})),
    :sh04-syntax-artifact (:sh04-syntax-artifact artifact),
    :pass (:pass artifact),
    :domain :gravity/sh05-macro-source-artifact-v2})))

(defn
 artifact-identity-input
 [artifact operations]
 (let
  [sh05-macro-artifact-semantic-payload
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-artifact-semantic-payload)]
  (sh05-macro-artifact-semantic-payload artifact)))

(defn
 artifact-id
 [artifact operations]
 (let
  [p15-s23-c6c10-canonical-digest
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-canonical-digest)
   sh05-macro-artifact-identity-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-artifact-identity-input)]
  (p15-s23-c6c10-canonical-digest
   "<sh05-macro-artifact>"
   (sh05-macro-artifact-identity-input artifact))))

(defn
 stream-identity-input
 [expanded-stream operations]
 (let
  [sh05-ordered-identity-chunks
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-ordered-identity-chunks)]
  {:domain :gravity/sh05-expanded-syntax-stream-v2,
   :item-count (count expanded-stream),
   :item-chunks
   (sh05-ordered-identity-chunks
    expanded-stream
    (fn
     [ordinal syntax]
     {:ordinal ordinal,
      :expanded-syntax-id (:expanded-syntax-id syntax)}))}))

(defn
 stream-id
 [source-path expanded-stream operations]
 (let
  [p15-s23-c6c10-canonical-digest
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-canonical-digest)
   sh05-expanded-syntax-stream-identity-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-expanded-syntax-stream-identity-input)]
  (p15-s23-c6c10-canonical-digest
   source-path
   (sh05-expanded-syntax-stream-identity-input expanded-stream))))

(defn
 trace-identity-input
 [trace operations]
 (let
  [sh05-ordered-identity-chunks
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-ordered-identity-chunks)]
  {:domain :gravity/sh05-macro-expansion-trace-v2,
   :item-count (count trace),
   :item-chunks
   (sh05-ordered-identity-chunks
    trace
    (fn
     [ordinal step]
     {:ordinal ordinal,
      :input-syntax-id (:input-syntax-id step),
      :output-syntax-id (:output-syntax-id step),
      :macro (:macro step),
      :macro-version (:macro-version step),
      :trace-replay-id (:trace-replay-id step)}))}))

(defn
 trace-id
 [source-path trace operations]
 (let
  [p15-s23-c6c10-canonical-digest
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-c6c10-canonical-digest)
   sh05-macro-trace-identity-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-trace-identity-input)]
  (p15-s23-c6c10-canonical-digest
   source-path
   (sh05-macro-trace-identity-input trace))))

(defn
 envelope-summary
 [artifact operations]
 (let
  [sh05-ordered-identity-chunks
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-ordered-identity-chunks)]
  {:expanded-defn-count (:expanded-defn-count artifact),
   :macro-expansion-trace-id (:macro-expansion-trace-id artifact),
   :expanded-syntax-stream-id (:expanded-syntax-stream-id artifact),
   :ordered-run-identity-chunks
   (sh05-ordered-identity-chunks
    (get-in artifact [:gravity-macro-boundary :expansion-runs])
    (fn
     [ordinal run]
     {:ordinal ordinal,
      :artifact-id (get-in run [:resolved-expansion :artifact-id]),
      :input-syntax-id
      (get-in run [:resolved-expansion :input-syntax-id]),
      :output-syntax-id
      (get-in run [:resolved-expansion :output-syntax-id]),
      :macro-version (get-in run [:resolved-expansion :macro-version]),
      :trace-replay-id
      (get-in
       run
       [:resolved-expansion
        :macro-expansion-trace
        0
        :trace-replay-id])})),
   :artifact-id (:artifact-id artifact),
   :sh04-artifact-id
   (get-in artifact [:sh04-syntax-artifact :artifact-id]),
   :status (:status artifact),
   :slice :SH-05,
   :ordered-run-count
   (count (get-in artifact [:gravity-macro-boundary :expansion-runs])),
   :semantic-payload-id (:artifact-id artifact)}))

(defn
 replay-report
 [source-path boundary operations]
 (let
  [sh05-macro-current-binding!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-current-binding!)
   sh05-macro-execute!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-execute!)
   binding
   (sh05-macro-current-binding! source-path)
   reports
   (mapv
    (fn
     [run]
     (let
      [raw
       (:raw-template-result run)
       resolved
       (:resolved-expansion run)
       requests
       (:digest-requests run)
       digests
       (:resolved-digests run)]
      {:template
       (sh05-macro-execute!
        source-path
        binding
        'sh05-verify-macro-template
        [(:expansion-template raw) requests]),
       :resolved
       (sh05-macro-execute!
        source-path
        binding
        'sh05-verify-macro-resolved
        [resolved requests digests])}))
    (:expansion-runs boundary))
   template-passed?
   (every?
    (fn* [p1__179#] (= :passed (get-in p1__179# [:template :status])))
    reports)
   resolved-passed?
   (every?
    (fn* [p1__180#] (= :passed (get-in p1__180# [:resolved :status])))
    reports)]
  {:template
   {:status (if template-passed? :passed :failed),
    :verified-run-count (count reports)},
   :resolved
   {:status (if resolved-passed? :passed :failed),
    :verified-run-count (count reports)},
   :runs reports}))

(defn
 source-snapshot!
 [source-path c2-artifact phase legacy? operations]
 (let
  [sh05-macro-boundary-fail!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-boundary-fail!)]
  (if
   legacy?
   {:canonical-path source-path,
    :byte-count nil,
    :bytes-hash (get-in c2-artifact [:source-unit-record :bytes-hash])}
   (let
    [source-unit
     (:source-unit-record c2-artifact)
     expected-path
     (:path source-unit)
     expected-hash
     (:bytes-hash source-unit)
     expected-count
     (or
      (:byte-count source-unit)
      (get-in
       c2-artifact
       [:gravity-reader-boundary
        :authenticated-envelope-descriptor
        :source-revision
        :source-byte-count]))
     snapshot
     (try
      (c2-pass-cache/bounded-source-snapshot! source-path)
      (catch
       Exception
       error
       (sh05-macro-boundary-fail!
        "C4-RETURN"
        source-path
        phase
        {:source-path source-path,
         :error-class (some-> error class .getName),
         :error-data (ex-data error)}
        {:required [:bounded-no-follow-source-snapshot]})))]
    (when-not
     (and
      (= source-path expected-path)
      (=
       (:canonical-path snapshot)
       (.getCanonicalPath (java.io.File. expected-path)))
      (= expected-hash (:bytes-hash snapshot))
      (or
       (nil? expected-count)
       (= (long expected-count) (long (:byte-count snapshot)))))
     (sh05-macro-boundary-fail!
      "C4-RETURN"
      source-path
      phase
      {:source-path source-path,
       :expected-path expected-path,
       :observed-canonical-path (:canonical-path snapshot),
       :expected-bytes-hash expected-hash,
       :observed-bytes-hash (:bytes-hash snapshot),
       :expected-byte-count expected-count,
       :observed-byte-count (:byte-count snapshot)}
      {:required
       [:source-path-provenance
        :source-bytes-hash
        :source-byte-count]}))
    snapshot))))

(defn
 source-stable!
 [source-path before c2-artifact legacy? operations]
 (let
  [sh05-authenticated-source-snapshot!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-authenticated-source-snapshot!)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   after
   (sh05-authenticated-source-snapshot!
    source-path
    c2-artifact
    :source-stability-after-c4
    legacy?)]
  (if
   legacy?
   after
   (do
    (when-not
     (=
      (select-keys before [:canonical-path :byte-count :bytes-hash])
      (select-keys after [:canonical-path :byte-count :bytes-hash]))
     (sh05-macro-boundary-fail!
      "C4-RETURN"
      source-path
      :source-mutated-during-c4
      {:before
       (select-keys before [:canonical-path :byte-count :bytes-hash]),
       :after
       (select-keys after [:canonical-path :byte-count :bytes-hash])}
      {:required [:stable-source-before-and-after-construction]}))
    after))))

(defn
 accept-syntax-parent!
 [source-path c3-artifact operations]
 (let
  [c3-artifact-id
   (.valAt ^clojure.lang.ILookup operations :c3-artifact-id)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   c2-artifact
   (:c2-reader-artifact c3-artifact)
   source-unit
   (:source-unit-record c2-artifact)
   report
   (:syntax-verification-report c3-artifact)
   proof
   (:capability-based-proof c3-artifact)
   results
   (:c3-syntax-results c3-artifact)
   observed-path
   (:path source-unit)
   artifact-id
   (try (c3-artifact-id c3-artifact) (catch Exception _ nil))
   passed?
   (and
    (map? c3-artifact)
    (= :gravity/stage0-c3-syntax-object-artifact (:kind c3-artifact))
    (= :passed (:status report))
    (= :complete (:status proof))
    (= :complete (:status results))
    (= (:artifact-id c3-artifact) artifact-id)
    (= source-path observed-path)
    (map? source-unit)
    (string? (:source-id source-unit))
    (string? (:bytes-hash source-unit))
    (=
     (:bytes-hash source-unit)
     (get-in source-unit [:identity-inputs :bytes-hash]))
    (=
     (:project-relative-path source-unit)
     (get-in source-unit [:identity-inputs :project-relative-path]))
    (=
     (:project-root source-unit)
     (get-in source-unit [:identity-inputs :project-root-id]))
    (=
     source-path
     (get-in
      c3-artifact
      [:gravity-syntax-boundary
       :authenticated-envelope-descriptor
       :actual-path-provenance
       :source-path]))
    (=
     source-path
     (get-in
      c3-artifact
      [:gravity-syntax-boundary
       :reader-authentication-provenance
       :actual-sh03-envelope-descriptor
       :actual-path-provenance
       :source-path]))
    (=
     :accepted
     (get-in
      c3-artifact
      [:gravity-syntax-boundary :authenticated-envelope :status]))
    (=
     :passed
     (get-in
      c3-artifact
      [:gravity-syntax-boundary
       :resolved-stream-verification
       :status])))]
  (when-not
   passed?
   (sh05-macro-boundary-fail!
    "C4-RETURN"
    source-path
    :authenticated-c3-syntax-parent
    {:source-path source-path,
     :observed-path observed-path,
     :artifact-kind (:kind c3-artifact),
     :artifact-id (:artifact-id c3-artifact),
     :recomputed-artifact-id artifact-id,
     :syntax-verification-status (:status report),
     :capability-proof-status (:status proof),
     :c3-results-status (:status results)}
    {:required
     [:complete-c3-artifact-identity
      :passed-c3-verifier
      :complete-c3-capability-proof
      :source-unit-path-provenance]}))
  c3-artifact))

(defn
 assemble
 [source-path c3-artifact legacy? invocation operations]
 (let
  [input
   ^{:line 78, :column 42}
   (.valAt ^clojure.lang.ILookup operations :input)
   c3-syntax-validate!
   (.valAt ^clojure.lang.ILookup operations :c3-syntax-validate!)
   p15-s23-stage2-sh02-descriptor-envelope
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-sh02-descriptor-envelope)
   p15-s23-stage2-sh02-descriptor-envelope-verify!
   (.valAt
    ^clojure.lang.ILookup operations
    :p15-s23-stage2-sh02-descriptor-envelope-verify!)
   parse-module
   (.valAt ^clojure.lang.ILookup operations :parse-module)
   sh03-reader-authenticated-envelope-options
   (.valAt
    ^clojure.lang.ILookup operations
    :sh03-reader-authenticated-envelope-options)
   sh05-authenticated-c3-parent!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-authenticated-c3-parent!)
   sh05-authenticated-source-snapshot!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-authenticated-source-snapshot!)
   sh05-deep-merge
   (.valAt ^clojure.lang.ILookup operations :sh05-deep-merge)
   sh05-default-macro-request
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-default-macro-request)
   sh05-defn-form?
   (.valAt ^clojure.lang.ILookup operations :sh05-defn-form?)
   sh05-expanded-graph
   (.valAt ^clojure.lang.ILookup operations :sh05-expanded-graph)
   sh05-expanded-syntax-object
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-expanded-syntax-object)
   sh05-expanded-syntax-stream-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-expanded-syntax-stream-id)
   sh05-macro-artifact-id
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-artifact-id)
   sh05-macro-artifact-verification
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-artifact-verification)
   sh05-macro-boundary-fail!
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-boundary-fail!)
   sh05-macro-capability-based-proof-for-construction
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-capability-based-proof-for-construction)
   sh05-macro-current-binding!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-current-binding!)
   sh05-macro-envelope-summary
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-envelope-summary)
   sh05-macro-sh02-descriptor
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-sh02-descriptor)
   sh05-macro-trace-id
   (.valAt ^clojure.lang.ILookup operations :sh05-macro-trace-id)
   sh05-package-trace
   (.valAt ^clojure.lang.ILookup operations :sh05-package-trace)
   sh05-run-macro-request!
   (.valAt ^clojure.lang.ILookup operations :sh05-run-macro-request!)
   sh05-source-snapshot-stable!
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-source-snapshot-stable!)
   sh05-source-syntax-stream
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-source-syntax-stream)]
  (sh05-authenticated-c3-parent! source-path c3-artifact)
  (let
   [ae-options
    (when
     invocation
     (sh03-reader-authenticated-envelope-options
      source-path
      invocation))
    c2-artifact
    (:c2-reader-artifact c3-artifact)
    source-before
    (sh05-authenticated-source-snapshot!
     source-path
     c2-artifact
     :source-authentication-before-c4
     legacy?)
    _
    (c3-syntax-validate! source-path c3-artifact invocation)
    forms
    (:parsed-semantic-values c2-artifact)
    module
    (parse-module source-path forms)
    binding
    (sh05-macro-current-binding! source-path)
    source-syntax
    (sh05-source-syntax-stream c3-artifact)
    _
    (when-not
     (= (count forms) (count source-syntax))
     (sh05-macro-boundary-fail!
      "C4-RETURN"
      source-path
      :complete-sh04-source-syntax-lockstep
      {:forms (count forms), :syntaxes (count source-syntax)}
      {}))
    compiler-metadata
    (get-in module [:metadata :compiler])
    request-overrides
    (if
     (and
      (map? compiler-metadata)
      (contains? compiler-metadata :sh05-request))
     (:sh05-request compiler-metadata)
     {})
    _
    (when-not
     (map? request-overrides)
     (sh05-macro-boundary-fail!
      "C4-RETURN"
      source-path
      :map-shaped-macro-request-overrides
      request-overrides
      {:observed-type (some-> request-overrides class .getName)}))
    pairs
    (mapv
     (fn
      [form syntax]
      (if
       (sh05-defn-form? form)
       (let
        [request
         (sh05-deep-merge
          (sh05-default-macro-request
           source-path
           c3-artifact
           module
           syntax
           form)
          request-overrides)]
        {:form form,
         :syntax syntax,
         :run (sh05-run-macro-request! source-path binding request)})
       {:form form, :syntax syntax, :run nil}))
     forms
     source-syntax)
    runs
    (mapv :run (filter :run pairs))
    expanded-stream
    (mapv
     (fn
      [{:keys [syntax form run]}]
      (sh05-expanded-syntax-object syntax form run))
     pairs)
    expanded-forms
    (mapv :form (subvec expanded-stream 1))
    trace
    (mapv
     (fn*
      [p1__181#]
      (sh05-package-trace p1__181# (:profile module) (:target module)))
     runs)
    stream-id
    (sh05-expanded-syntax-stream-id source-path expanded-stream)
    trace-id
    (sh05-macro-trace-id source-path trace)
    graph
    (sh05-expanded-graph expanded-stream trace)
    sh04-view
    {:artifact :gravity/sh04-syntax-object-artifact,
     :artifact-id (:artifact-id c3-artifact),
     :status :accepted,
     :syntax-stream-id
     (get-in
      c3-artifact
      [:gravity-syntax-boundary :resolved-syntax-result :artifact-id])}
    plan-binding
    (dissoc binding :plan :source-text)
    boundary-base
    {:plan-binding plan-binding,
     :raw-template-result (:raw-template-result (first runs)),
     :clojure-adapter-residual? true,
     :resolved-expansion (:resolved-expansion (first runs)),
     :digest-requests (:digest-requests (first runs)),
     :resolved-digests (:resolved-digests (first runs)),
     :self-hosted? false,
     :expansion-runs runs,
     :slice :SH-05,
     :authenticated-sh04-artifact c3-artifact,
     :adapter-contract (input :sh05-macro-adapter-contract),
     :target-source-reread? false,
     :owner :gravity-source}
    artifact-base
    {:expanded-defn-count (count runs),
     :capability-based-proof nil,
     :macro-expansion-trace-id trace-id,
     :diagnostics [],
     :expanded-syntax-stream-id stream-id,
     :expanded-forms expanded-forms,
     :task "SH-05",
     :artifact-id nil,
     :expanded-syntax-stream expanded-stream,
     :expanded-syntax-graph graph,
     :document-set ["L4" "C4"],
     :governing-document (input :c4-macro-governing-document),
     :execution-boundary
     {:macro-authority :gravity,
      :gravity-module 'gravity.macro,
      :plan-runner :clojure-stage0,
      :digest-resolver :clojure-stage0,
      :envelope-binder :clojure-stage0,
      :c4-stage0-adapter :compatibility-only,
      :self-hosted? false},
     :macro-environment
     {:artifact :gravity/sh05-macro-environment,
      :macros
      [{:name 'defn,
        :version (input :sh05-macro-version),
        :phase :macro-expansion}],
      :status :complete},
     :status :accepted,
     :slice :SH-05,
     :kind :gravity/sh05-macro-expansion-artifact,
     :generated-origin-source-map
     (mapv
      (fn*
       [p1__182#]
       (select-keys p1__182# [:output-syntax-id :generated-origin]))
      trace),
     :gravity-macro-boundary boundary-base,
     :provenance
     {:source-path source-path,
      :source-id
      (get-in c2-artifact [:source-unit-record :source-id])},
     :sh04-syntax-artifact sh04-view,
     :pass
     {:name :sh05-gravity-macro-expansion,
      :input :authenticated-sh04-syntax,
      :output :authenticated-sh05-expanded-syntax,
      :preserves
      [:syntax-identity :metadata :hygiene :origins :reader-lineage],
      :rejects (input :c4-macro-diagnostic-ids)},
     :macro-expansion-trace trace}
    artifact-id
    (sh05-macro-artifact-id artifact-base)
    summary
    (sh05-macro-envelope-summary
     (assoc artifact-base :artifact-id artifact-id))
    descriptor
    (sh05-macro-sh02-descriptor source-path binding summary)
    envelope
    (apply
     p15-s23-stage2-sh02-descriptor-envelope
     (cond->
      [(input :sh05-macro-envelope-stage)
       (input :sh05-macro-sealed-artifact-kind)
       descriptor
       source-path]
      ae-options
      (conj ae-options)))
    _
    (apply
     p15-s23-stage2-sh02-descriptor-envelope-verify!
     (cond->
      [envelope
       (input :sh05-macro-envelope-stage)
       (input :sh05-macro-sealed-artifact-kind)
       descriptor
       source-path]
      ae-options
      (conj ae-options)))
    artifact-with-envelope
    (->
     artifact-base
     (assoc :artifact-id artifact-id)
     (assoc-in
      [:gravity-macro-boundary :authenticated-envelope-descriptor]
      descriptor)
     (assoc-in
      [:gravity-macro-boundary :authenticated-envelope]
      envelope))
    proof
    (sh05-macro-capability-based-proof-for-construction
     artifact-with-envelope
     invocation)
    artifact
    (assoc artifact-with-envelope :capability-based-proof proof)
    final-report
    (sh05-macro-artifact-verification artifact invocation)]
   (when-not
    (= :passed (:status final-report))
    (sh05-macro-boundary-fail!
     "C4-TRACE"
     source-path
     :final-authenticated-macro-artifact
     (:failed-checks final-report)
     {}))
   (sh05-source-snapshot-stable!
    source-path
    source-before
    c2-artifact
    legacy?)
   artifact)))

(defn
 c4-identity-input
 [artifact]
 {:build-effect-log (:build-effect-log artifact),
  :capability-based-proof (:capability-based-proof artifact),
  :macro-expansion-trace-id
  (get-in
   artifact
   [:sh05-macro-expansion-artifact :macro-expansion-trace-id]),
  :hygiene-capture-records (:hygiene-capture-records artifact),
  :diagnostics (:diagnostics artifact),
  :expanded-syntax-stream-id
  (get-in
   artifact
   [:sh05-macro-expansion-artifact :expanded-syntax-stream-id]),
  :task (:task artifact),
  :sh05-artifact-id
  (get-in artifact [:sh05-macro-expansion-artifact :artifact-id]),
  :document-set (:document-set artifact),
  :macro-safety-declarations (:macro-safety-declarations artifact),
  :governing-document (:governing-document artifact),
  :execution-boundary (:execution-boundary artifact),
  :macro-expansion-input (:macro-expansion-input artifact),
  :macro-environment (:macro-environment artifact),
  :kind (:kind artifact),
  :domain :gravity/sh05-c4-compatibility-artifact-v1})

(defn
 c4-artifact-id
 [artifact operations]
 (let
  [reader-canonical-hash
   (.valAt ^clojure.lang.ILookup operations :reader-canonical-hash)
   sh05-c4-compatibility-identity-input
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-c4-compatibility-identity-input)]
  (reader-canonical-hash
   (sh05-c4-compatibility-identity-input artifact))))

(defn
 c4-artifact
 [_source-path sh05 operations]
 (let
  [input
   ^{:line 78, :column 42}
   (.valAt ^clojure.lang.ILookup operations :input)
   sh05-c4-compatibility-artifact-id
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-c4-compatibility-artifact-id)
   artifact-base
   {:build-effect-log
    {:artifact :gravity/macro-build-effect-log,
     :records [],
     :status :complete},
    :capability-based-proof (:capability-based-proof sh05),
    :hygiene-capture-records [],
    :diagnostics [],
    :c3-syntax-object-artifact (:sh04-syntax-artifact sh05),
    :task "P06-D083",
    :expanded-syntax-stream (:expanded-syntax-stream sh05),
    :document-set ["C4"],
    :macro-safety-declarations
    {:artifact :gravity/macro-safety-declaration-records,
     :records [],
     :status :complete},
    :governing-document (input :c4-macro-governing-document),
    :execution-boundary
    {:macro-authority :gravity,
     :c4-stage0-adapter :compatibility-only,
     :target-source-reread? false,
     :self-hosting-credit? false},
    :macro-expansion-input
    {:artifact :gravity/macro-expansion-input,
     :syntax-root
     (get-in sh05 [:sh04-syntax-artifact :syntax-stream-id]),
     :profile :meta,
     :target :jvm,
     :hermetic true},
    :macro-environment (:macro-environment sh05),
    :kind :gravity/stage0-c4-macro-expansion-artifact,
    :generated-origin-source-map (:generated-origin-source-map sh05),
    :macro-expansion-trace (:macro-expansion-trace sh05),
    :sh05-macro-expansion-artifact sh05}
   artifact-id
   (sh05-c4-compatibility-artifact-id artifact-base)]
  (assoc artifact-base :artifact-id artifact-id)))

(defn
 compiler-source-path?
 [source-path operations]
 (let
  [sh05-macro-resolve-source-path
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-resolve-source-path)
   anchor
   (io/file (sh05-macro-resolve-source-path))
   repository-root
   (loop
    [directory (.getParentFile anchor)]
    (cond
     (nil? directory)
     nil
     (.isFile (io/file directory "deps.edn"))
     directory
     :else
     (recur (.getParentFile directory))))
   root
   (.toPath
    (.getCanonicalFile
     (io/file
      (or repository-root (io/file "."))
      "bootstrap/gravity/src")))
   candidate
   (.toPath (.getCanonicalFile (io/file source-path)))]
  (.startsWith candidate root)))

(defn
 legacy-position?
 [form operations]
 (let
  [sh05-form-contains-legacy-macro-position?
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-form-contains-legacy-macro-position?)]
  (cond
   (seq? form)
   (or
    (contains? '#{-> when defmacro} (first form))
    (some sh05-form-contains-legacy-macro-position? form))
   (map? form)
   (some
    sh05-form-contains-legacy-macro-position?
    (mapcat identity form))
   (coll? form)
   (some sh05-form-contains-legacy-macro-position? form)
   :else
   false)))

(defn
 source-eligible?
 [source-path module forms operations]
 (let
  [sh05-authoritative-compiler-source-path?
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-authoritative-compiler-source-path?)
   sh05-form-contains-legacy-macro-position?
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-form-contains-legacy-macro-position?)]
  (and
   (sh05-authoritative-compiler-source-path? source-path)
   (empty? (:requires module))
   (empty? (:imports module))
   (every?
    (fn
     [form]
     (and (seq? form) (contains? '#{defn ns def} (first form))))
    forms)
   (not-any? sh05-form-contains-legacy-macro-position? forms))))

(defn
 compile-source
 [source-path source-text operations]
 (let
  [compiler-c2-reader-source-artifact
   (.valAt
    ^clojure.lang.ILookup operations
    :compiler-c2-reader-source-artifact)
   compiler-c4-stage0-legacy-source-artifact
   (.valAt
    ^clojure.lang.ILookup operations
    :compiler-c4-stage0-legacy-source-artifact)
   parse-module
   (.valAt ^clojure.lang.ILookup operations :parse-module)
   sh05-bounded-authoritative-source?
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-bounded-authoritative-source?)
   sh05-c4-compatibility-artifact
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-c4-compatibility-artifact)
   sh05-macro-source-artifact
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-source-artifact)
   c2
   (compiler-c2-reader-source-artifact source-path source-text)
   forms
   (:parsed-semantic-values c2)
   module
   (parse-module source-path forms)]
  (if
   (sh05-bounded-authoritative-source? source-path module forms)
   (sh05-c4-compatibility-artifact
    source-path
    (sh05-macro-source-artifact source-path source-text))
   (compiler-c4-stage0-legacy-source-artifact
    source-path
    source-text))))

(defn
 verify
 [artifact allow-missing-proof? invocation operations]
 (letfn
  [(semantic-late-sh05-macro-artifact-verification*-carrier
    [state]
    (let
     [input
      ^{:line 48, :column 42}
      (.valAt ^clojure.lang.ILookup operations :input)
      c3-artifact-id
      (.valAt ^clojure.lang.ILookup operations :c3-artifact-id)
      c3-syntax-capability-proof
      (.valAt
       ^clojure.lang.ILookup operations
       :c3-syntax-capability-proof)
      c3-syntax-stream-reader-products-authentic?
      (.valAt
       ^clojure.lang.ILookup operations
       :c3-syntax-stream-reader-products-authentic?)
      p15-s23-stage2-sh02-descriptor-envelope-verify!
      (.valAt
       ^clojure.lang.ILookup operations
       :p15-s23-stage2-sh02-descriptor-envelope-verify!)
      sh03-reader-authenticated-envelope-options
      (.valAt
       ^clojure.lang.ILookup operations
       :sh03-reader-authenticated-envelope-options)
      sh05-macro-current-binding!
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-macro-current-binding!)
      sh05-macro-envelope-summary
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-macro-envelope-summary)
      sh05-macro-gravity-verifier-report
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-macro-gravity-verifier-report)
      sh05-macro-sh02-descriptor
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-macro-sh02-descriptor)
      {:keys [artifact  invocation]}
      state
      source-path
      (get-in artifact [:provenance :source-path])
      boundary
      (:gravity-macro-boundary artifact)
      c3-artifact
      (:authenticated-sh04-artifact boundary)
      c3-boundary
      (:gravity-syntax-boundary c3-artifact)
      ae-options
      (when
       invocation
       (sh03-reader-authenticated-envelope-options
        source-path
        invocation))
      gravity-verifiers
      (try
       (sh05-macro-gravity-verifier-report source-path boundary)
       (catch
        InterruptedException
        interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
       (catch
        Throwable
        _
        {:template {:status :failed}, :resolved {:status :failed}}))
      output-envelope-ok?
      (try
       (=
        :passed
        (apply
         p15-s23-stage2-sh02-descriptor-envelope-verify!
         (cond->
          [(:authenticated-envelope boundary)
           (input :sh05-macro-envelope-stage)
           (input :sh05-macro-sealed-artifact-kind)
           (:authenticated-envelope-descriptor boundary)
           source-path]
          ae-options
          (conj ae-options))))
       (catch
        InterruptedException
        interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
       (catch Throwable _ false))
      binding
      (sh05-macro-current-binding! source-path)
      embedded-descriptor
      (:authenticated-envelope-descriptor boundary)
      expected-descriptor
      (try
       (sh05-macro-sh02-descriptor
        source-path
        binding
        (sh05-macro-envelope-summary artifact))
       (catch
        InterruptedException
        interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
       (catch Throwable _ nil))
      descriptor-current?
      (= expected-descriptor embedded-descriptor)
      sh03-current?
      (try
       (true?
        (c3-syntax-stream-reader-products-authentic?
         (:syntax-object-stream c3-artifact)
         (:c2-reader-artifact c3-artifact)
         c3-boundary
         invocation))
       (catch
        InterruptedException
        interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
       (catch Throwable _ false))
      sh04-current?
      (try
       (and
        (= (:artifact-id c3-artifact) (c3-artifact-id c3-artifact))
        (=
         :complete
         (:status
          (c3-syntax-capability-proof c3-artifact invocation))))
       (catch
        InterruptedException
        interrupted
        (.interrupt (Thread/currentThread))
        (throw interrupted))
       (catch Throwable _ false))
      template-passed?
      (= :passed (get-in gravity-verifiers [:template :status]))
      resolved-passed?
      (= :passed (get-in gravity-verifiers [:resolved :status]))]
     (clojure.core/assoc
      state
      :source-path
      source-path
      :boundary
      boundary
      :c3-artifact
      c3-artifact
      :c3-boundary
      c3-boundary
      :gravity-verifiers
      gravity-verifiers
      :output-envelope-ok?
      output-envelope-ok?
      :binding
      binding
      :embedded-descriptor
      embedded-descriptor
      :expected-descriptor
      expected-descriptor
      :descriptor-current?
      descriptor-current?
      :sh03-current?
      sh03-current?
      :sh04-current?
      sh04-current?
      :template-passed?
      template-passed?
      :resolved-passed?
      resolved-passed?)))
   (semantic-late-sh05-macro-artifact-verification*-expansion-replay
    [state]
    (let
     [input
      ^{:line 48, :column 42}
      (.valAt ^clojure.lang.ILookup operations :input)
      parse-module
      (.valAt ^clojure.lang.ILookup operations :parse-module)
      sh05-defn-form?
      (.valAt ^clojure.lang.ILookup operations :sh05-defn-form?)
      sh05-expanded-syntax-object
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-expanded-syntax-object)
      sh05-expanded-syntax-stream-id
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-expanded-syntax-stream-id)
      sh05-macro-trace-id
      (.valAt ^clojure.lang.ILookup operations :sh05-macro-trace-id)
      sh05-package-trace
      (.valAt ^clojure.lang.ILookup operations :sh05-package-trace)
      sh05-source-syntax-stream
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-source-syntax-stream)
      {:keys
       [artifact
        source-path
        boundary
        c3-artifact
        binding
        template-passed?
        resolved-passed?]}
      state
      runs
      (:expansion-runs boundary)
      input-forms
      (get-in
       c3-artifact
       [:c2-reader-artifact :parsed-semantic-values])
      input-syntax
      (sh05-source-syntax-stream c3-artifact)
      input-module
      (parse-module source-path input-forms)
      expected-expanded-stream
      (loop
       [forms
        input-forms
        syntaxes
        input-syntax
        remaining-runs
        runs
        result
        []]
       (if-let
        [form (first forms)]
        (let
         [syntax
          (first syntaxes)
          run
          (when (sh05-defn-form? form) (first remaining-runs))]
         (recur
          (rest forms)
          (rest syntaxes)
          (if run (rest remaining-runs) remaining-runs)
          (conj result (sh05-expanded-syntax-object syntax form run))))
        (when (and (empty? syntaxes) (empty? remaining-runs)) result)))
      expected-trace
      (mapv
       (fn*
        [p1__1275#]
        (sh05-package-trace
         p1__1275#
         (:profile input-module)
         (:target input-module)))
       runs)
      first-run
      (first runs)
      first-run-shortcuts-current?
      (if
       first-run
       (=
        (select-keys
         boundary
         [:raw-template-result
          :resolved-expansion
          :digest-requests
          :resolved-digests])
        (select-keys
         first-run
         [:raw-template-result
          :resolved-expansion
          :digest-requests
          :resolved-digests]))
       (every?
        nil?
        ((juxt
          :raw-template-result
          :resolved-expansion
          :digest-requests
          :resolved-digests)
         boundary)))
      run-count-current?
      (= (:expanded-defn-count artifact) (count runs))
      run-storage-exact?
      (macro-run-storage/exact? runs)
      plan-binding-current?
      (=
       (select-keys
        (:plan-binding boundary)
        [:source-byte-count
         :source-content-hash
         :plan-semantic-hash
         :functions-semantic-hash
         :function-count
         :function-names-hash
         :function-shapes-hash
         :public-function-hashes
         :public-function-shapes])
       (select-keys
        binding
        [:source-byte-count
         :source-content-hash
         :plan-semantic-hash
         :functions-semantic-hash
         :function-count
         :function-names-hash
         :function-shapes-hash
         :public-function-hashes
         :public-function-shapes]))
      pinned-version?
      (every?
       (fn*
        [p1__1276#]
        (=
         (input :sh05-macro-version)
         (get-in p1__1276# [:resolved-expansion :macro-version])))
       runs)
      grants-current?
      (every?
       (fn
        [run]
        (let
         [log (get-in run [:resolved-expansion :build-effect-log])]
         (and
          (set/subset? (set (:requested log)) (set (:declared log)))
          (set/subset? (set (:declared log)) (set (:granted log))))))
       runs)
      trace-current?
      (and
       template-passed?
       resolved-passed?
       (= expected-trace (:macro-expansion-trace artifact))
       (=
        (:macro-expansion-trace-id artifact)
        (sh05-macro-trace-id
         source-path
         (:macro-expansion-trace artifact))))
      output-current?
      (and
       template-passed?
       resolved-passed?
       (= expected-expanded-stream (:expanded-syntax-stream artifact))
       (= (:expanded-defn-count artifact) (count runs))
       (=
        (:expanded-forms artifact)
        (mapv :form (subvec (:expanded-syntax-stream artifact) 1)))
       (=
        (:expanded-syntax-stream-id artifact)
        (sh05-expanded-syntax-stream-id
         source-path
         (:expanded-syntax-stream artifact))))
      generated-origins-current?
      (=
       (:generated-origin-source-map artifact)
       (mapv
        (fn*
         [p1__1277#]
         (select-keys p1__1277# [:output-syntax-id :generated-origin]))
        (:macro-expansion-trace artifact)))
      input-lineage-current?
      (=
       (get-in artifact [:sh04-syntax-artifact :artifact-id])
       (:artifact-id c3-artifact))]
     (clojure.core/assoc
      state
      :runs
      runs
      :input-forms
      input-forms
      :input-syntax
      input-syntax
      :input-module
      input-module
      :expected-expanded-stream
      expected-expanded-stream
      :expected-trace
      expected-trace
      :first-run
      first-run
      :first-run-shortcuts-current?
      first-run-shortcuts-current?
      :run-count-current?
      run-count-current?
      :run-storage-exact?
      run-storage-exact?
      :plan-binding-current?
      plan-binding-current?
      :pinned-version?
      pinned-version?
      :grants-current?
      grants-current?
      :trace-current?
      trace-current?
      :output-current?
      output-current?
      :generated-origins-current?
      generated-origins-current?
      :input-lineage-current?
      input-lineage-current?)))
   (semantic-late-sh05-macro-artifact-verification*-provenance
    [state]
    (let
     [input
      ^{:line 48, :column 42}
      (.valAt ^clojure.lang.ILookup operations :input)
      sh05-expanded-graph
      (.valAt ^clojure.lang.ILookup operations :sh05-expanded-graph)
      sh05-expanded-graph-valid?
      (.valAt
       ^clojure.lang.ILookup operations
       :sh05-expanded-graph-valid?)
      sh05-macro-artifact-id
      (.valAt ^clojure.lang.ILookup operations :sh05-macro-artifact-id)
      {:keys
       [artifact
        source-path
        c3-artifact
        c3-boundary
        template-passed?
        resolved-passed?
        runs
        ]}
      state
      c3-syntax-by-id
      (into
       {}
       (map
        (juxt :syntax/id identity)
        (:syntax-object-stream c3-artifact)))
      expected-reader-binding
      (:reader-semantic-binding c3-boundary)
      expected-reader-revision
      (:reader-source-revision c3-boundary)
      expected-definition-span
      {:source (input :sh05-macro-source-relative-path),
       :byte-start 0,
       :byte-end (input :sh05-macro-expected-source-byte-count)}
      run-provenance-current?
      (every?
       (fn
        [run]
        (let
         [raw-expansion
          (get-in run [:raw-template-result :expansion-template])
          resolved
          (:resolved-expansion run)
          input-id
          (:input-syntax-id resolved)
          input-syntax
          (get c3-syntax-by-id input-id)
          raw-provenance
          (:provenance raw-expansion)
          resolved-provenance
          (:provenance resolved)
          digest-provenance
          (get-in run [:digest-requests 1 :preimage :provenance])]
         (and
          input-syntax
          (=
           source-path
           (:actual-source-path raw-provenance)
           (:actual-source-path resolved-provenance)
           (:actual-source-path digest-provenance))
          (=
           (:span input-syntax)
           (:call-site-span raw-provenance)
           (:call-site-span resolved-provenance)
           (:call-site-span digest-provenance))
          (=
           (:origin input-syntax)
           (:input-origin-chain raw-provenance)
           (:input-origin-chain resolved-provenance)
           (:input-origin-chain digest-provenance))
          (=
           expected-definition-span
           (:definition-span raw-provenance)
           (:definition-span resolved-provenance)
           (:definition-span digest-provenance))
          (=
           expected-reader-binding
           (:reader-binding raw-expansion)
           (:reader-binding resolved)
           (:reader-binding raw-provenance)
           (:reader-binding resolved-provenance)
           (:reader-binding digest-provenance))
          (=
           expected-reader-revision
           (:reader-source-revision raw-expansion)
           (:reader-source-revision resolved)
           (:reader-source-revision raw-provenance)
           (:reader-source-revision resolved-provenance)
           (:reader-source-revision digest-provenance)))))
       runs)
      provenance-current?
      (and
       (= source-path (get-in artifact [:provenance :source-path]))
       (=
        (get-in
         c3-artifact
         [:c2-reader-artifact :source-unit-record :source-id])
        (get-in artifact [:provenance :source-id])))
      graph-valid?
      (sh05-expanded-graph-valid? (:expanded-syntax-graph artifact))
      graph-current?
      (=
       (:expanded-syntax-graph artifact)
       (sh05-expanded-graph
        (:expanded-syntax-stream artifact)
        (:macro-expansion-trace artifact)))
      exact-shape?
      (and
       (= (input :sh05-macro-artifact-keys) (set (keys artifact)))
       template-passed?
       resolved-passed?)
      artifact-id-current?
      (= (:artifact-id artifact) (sh05-macro-artifact-id artifact))]
     (clojure.core/assoc
      state
      :c3-syntax-by-id
      c3-syntax-by-id
      :expected-reader-binding
      expected-reader-binding
      :expected-reader-revision
      expected-reader-revision
      :expected-definition-span
      expected-definition-span
      :run-provenance-current?
      run-provenance-current?
      :provenance-current?
      provenance-current?
      :graph-valid?
      graph-valid?
      :graph-current?
      graph-current?
      :exact-shape?
      exact-shape?
      :artifact-id-current?
      artifact-id-current?)))
   (semantic-late-sh05-macro-artifact-verification*-proof
    [state]
    (let
     [{:keys
       [artifact
        allow-missing-proof?
        output-envelope-ok?
        descriptor-current?
        sh03-current?
        sh04-current?
        first-run-shortcuts-current?
        run-count-current?
        run-storage-exact?
        plan-binding-current?
        pinned-version?
        grants-current?
        trace-current?
        output-current?
        generated-origins-current?
        input-lineage-current?
        run-provenance-current?
        provenance-current?
        graph-valid?
        graph-current?
        exact-shape?
        artifact-id-current?]}
      state
      base-checks
      {:expanded-graph-current? graph-current?,
       :macro-version-current? pinned-version?,
       :actual-path-provenance-current? provenance-current?,
       :trace-replay-current? trace-current?,
       :exact-artifact-shape? exact-shape?,
       :macro-run-storage-exact? run-storage-exact?,
       :sh04-syntax-lineage-current? sh04-current?,
       :expanded-graph-valid? graph-valid?,
       :generated-origins-current? generated-origins-current?,
       :sh03-reader-lineage-current? sh03-current?,
       :macro-output-current? output-current?,
       :artifact-id-current? artifact-id-current?,
       :macro-plan-binding-current? plan-binding-current?,
       :build-grants-current? grants-current?,
       :first-run-compatibility-view-current?
       first-run-shortcuts-current?,
       :sh02-envelope-descriptor-current? descriptor-current?,
       :macro-run-provenance-current? run-provenance-current?,
       :macro-run-count-current? run-count-current?,
       :fresh-sh02-envelope-verified? output-envelope-ok?,
       :input-lineage-current? input-lineage-current?}
      base-failed-checks
      (vec
       (keep
        (fn
         [[check-name passed?]]
         (when-not
          passed?
          (keyword
           (str/replace (clojure.core/name check-name) #"\?$" ""))))
        base-checks))
      expected-embedded-proof
      (assoc
       base-checks
       :embedded-capability-proof-current?
       true
       :artifact
       :gravity/sh05-macro-capability-proof
       :status
       (if (empty? base-failed-checks) :complete :failed)
       :failed-checks
       base-failed-checks)
      embedded-proof-current?
      (or
       (and
        allow-missing-proof?
        (nil? (:capability-based-proof artifact)))
       (= expected-embedded-proof (:capability-based-proof artifact)))
      checks
      (assoc
       base-checks
       :embedded-capability-proof-current?
       embedded-proof-current?)
      failed-checks
      (vec
       (keep
        (fn
         [[check-name passed?]]
         (when-not
          passed?
          (keyword
           (str/replace (clojure.core/name check-name) #"\?$" ""))))
        checks))]
     (clojure.core/assoc
      state
      :base-checks
      base-checks
      :base-failed-checks
      base-failed-checks
      :expected-embedded-proof
      expected-embedded-proof
      :embedded-proof-current?
      embedded-proof-current?
      :checks
      checks
      :failed-checks
      failed-checks)))]
  (clojure.core/let
   [state-0
    {:artifact artifact,
     :allow-missing-proof? allow-missing-proof?,
     :invocation invocation}
    state-1
    (semantic-late-sh05-macro-artifact-verification*-carrier state-0)
    state-2
    (semantic-late-sh05-macro-artifact-verification*-expansion-replay
     state-1)
    state-3
    (semantic-late-sh05-macro-artifact-verification*-provenance
     state-2)
    state-4
    (semantic-late-sh05-macro-artifact-verification*-proof state-3)
    {:keys
      [
       gravity-verifiers
       checks
       failed-checks]}
     state-4]
    {:artifact :gravity/sh05-macro-artifact-verification,
     :status (if (empty? failed-checks) :passed :failed),
     :checks checks,
     :failed-checks failed-checks,
     :gravity-verifiers gravity-verifiers})))

(doseq
 [operation
  [#'source-eligible?
   #'assemble
   #'path-neutral-value
   #'source-snapshot!
   #'merge-request
   #'current-plan-binding!
   #'resolve-expansion-template!
   #'expand
   #'expanded-form
   #'artifact-payload
   #'compiler-source-path?
   #'identity-chunks
   #'boundary-fail!
   #'trace-identity-input
   #'c4-artifact-id
   #'defmacro-form?
   #'defn-form?
   #'request
   #'source-stable!
   #'resolve-digest-preimage!
   #'contain-operation!
   #'expanded-syntax-object
   #'expanded-graph-valid?
   #'raise-rejection!
   #'run-summary
   #'plan-identities
   #'envelope-summary
   #'artifact-id
   #'semantic-component-id
   #'source-syntax-stream
   #'execute-plan!
   #'read-pinned-plan-source!
   #'resolve-plan-source-path
   #'artifact-identity-input
   #'expanded-graph
   #'legacy-position?
   #'stream-id
   #'stream-identity-input
   #'resolve-digests!
   #'envelope-descriptor
   #'trace-record
   #'trace-id
   #'compute-plan-identities
   #'compile-source
   #'c4-artifact
   #'replay-report
   #'c4-identity-input
   #'accept-syntax-parent!
   #'build-plan-binding!
   #'verify]]
 ^{:line 73, :column 10}
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
