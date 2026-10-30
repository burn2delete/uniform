(ns
 gravity.macro
 "Pinned Gravity macro plan execution under C4. Seed lifetime and host operations are explicit inputs.")

(def
 ^{:private true} namespace-contract
 '{:public-api
   {current-plan-binding!
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
    boundary-fail!
    {:arglists
     ([rule source-path missing-field observed facts operations]),
     :operation-inputs [:input :c4-macro-fail! :source-span]},
    resolve-digest-preimage!
    {:arglists ([source-path request resolved-digests operations]),
     :operation-inputs [:sh05-macro-boundary-fail!]},
    contain-operation!
    {:arglists ([source-path responsibility operation operations]),
     :operation-inputs [:input :sh05-macro-boundary-fail!]},
    raise-rejection!
    {:arglists ([source-path raw resolved-digests operations]),
     :operation-inputs
     [:c4-macro-fail! :sh05-macro-boundary-fail! :source-span]},
    plan-identities
    {:arglists ([plan operations]),
     :operation-inputs [:input :sh05-macro-compute-plan-identities]},
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
    resolve-digests!
    {:arglists ([source-path digest-requests operations]),
     :operation-inputs
     [:p15-s23-c6c10-canonical-digest
      :sh05-macro-boundary-fail!
      :sh05-resolve-request-preimage!]},
    compute-plan-identities
    {:arglists ([plan operations]),
     :operation-inputs
     [:input
      :p15-s23-c11-mir-digest
      :p15-s23-stage2-compiler-artifact-semantic-input]},
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
      :sha256-hex]}},
   :operation-inputs
   [:input
    :c-backend-canonical-value
    :c-backend-stage2-plan-emitter-source-rule!
    :c4-macro-fail!
    :p15-s23-c11-mir-digest
    :p15-s23-c6c10-canonical-digest
    :p15-s23-stage2-compiler-artifact-plan
    :p15-s23-stage2-compiler-artifact-semantic-input
    :p15-s23-stage2-compiler-artifact-source-path
    :p15-s23-stage2-runtime-execute-function
    :sh04-syntax-strip-host-metadata
    :sh05-contain-coordinator-operation!
    :sh05-macro-boundary-fail!
    :sh05-macro-compute-plan-identities
    :sh05-macro-execute!
    :sh05-macro-plan-identities
    :sh05-macro-raise-rejection!
    :sh05-macro-read-pinned-source!
    :sh05-macro-resolve-digests!
    :sh05-macro-resolve-source-path
    :sh05-resolve-expansion-template!
    :sh05-resolve-request-preimage!
    :sha256-bytes-hex
    :sha256-hex
    :source-span],
   :dependency-direction
   {:requires [clojure.core],
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
    :sh05-macro-cached-binding
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
    :sh05-macro-source-relative-path
    :sh05-macro-version],
   :unsupported
   [:macro-artifact-assembly
    :source-snapshots
    :compatibility-routing
    :legacy-macro-engine
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
    :explicit-seed-operations],
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
    :resolved-verification],
   :contract-boundary :pinned-gravity-macro-plan-execution})

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
    :p15-s23-stage2-compiler-artifact-source-path)]
  (let
   [anchor
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
    (input :sh05-macro-source-relative-path)))))

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
   (.valAt ^clojure.lang.ILookup operations :sha256-bytes-hex)]
  (let
   [source-path
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
    :source-content-hash content-hash})))

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
    :p15-s23-stage2-compiler-artifact-semantic-input)]
  (let
   [functions
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
     (keys (input :sh05-macro-public-function-shapes)))})))

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
    :sh05-macro-compute-plan-identities)]
  (let
   [[pinned-plan pinned-identities]
    @(input :sh05-macro-pinned-plan-identities)]
   (if
    (and pinned-plan (identical? pinned-plan plan))
    pinned-identities
    (sh05-macro-compute-plan-identities plan)))))

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
   (.valAt ^clojure.lang.ILookup operations :sha256-hex)]
  (let
   [source
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
        (pr-str
         (c-backend-canonical-value authenticated-plan-base)))))]
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
      :plan plan})))))

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
    :sh05-macro-read-pinned-source!)]
  (let
   [fresh
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
   binding)))

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
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-boundary-fail!)]
  (let
   [ordinal
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
     {:ordinal ordinal})))))

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
   (.valAt ^clojure.lang.ILookup operations :source-span)]
  (let
   [diagnostic
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
     :diagnostic-id (get diagnostic :diagnostic-id-request)}))))

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
   (.valAt
    ^clojure.lang.ILookup operations
    :sh05-macro-boundary-fail!)]
  (let
   [output-reference
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
      :provenance-reference
      (:provenance-binding-id expansion-template)}
     {:resolved-digest-count (count resolved-digests)}))
   (->
    (reduce
     (fn
      [template slot]
      (assoc-in template slot (first resolved-digests)))
     expansion-template
     output-reference-slots)
    (assoc :provenance-binding-id (second resolved-digests))))))

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
    :sh05-resolve-expansion-template!)]
  (let
   [raw
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
       [:artifact :schema-version :status :rule])})))))

(doseq
 [operation
  [#'boundary-fail!
   #'resolve-plan-source-path
   #'read-pinned-plan-source!
   #'compute-plan-identities
   #'plan-identities
   #'build-plan-binding!
   #'current-plan-binding!
   #'execute-plan!
   #'resolve-digest-preimage!
   #'resolve-digests!
   #'raise-rejection!
   #'contain-operation!
   #'resolve-expansion-template!
   #'expand]]
 (alter-meta!
  operation
  assoc
  :gravity/namespace-contract
  namespace-contract))
