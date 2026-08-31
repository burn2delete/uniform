# SH-07 Semantic Run-Product Carrier Prerequisite (v2)

## Status and authority

This report is a closed implementation prerequisite for one process-local,
non-authoritative development optimization. It authorizes no cache code. It is
not an SH-07 proof, fresh authoritative verification, performance claim,
release claim, self-hosting claim, roadmap credit, or seed-retirement claim.
The public and authoritative entrypoints remain unchanged.

## Empirical basis

An authentic B8 `top-level-typed-catch.gravity` build was compared with a
structural replay whose only changed authenticated-request field was
`[:provenance :actual-source-path]`. Both runs produced SH-07 artifact id
`sha256:9833aee0bd75425b57eca0651483a4d8fcff5433710ceefefcc667c67b36e008`.
The existing path-neutral projection made the core template equal, but the
complete run products, canonical cores, and resolved digest vectors differed.
`sh07-core-artifact-verification` rebuilds the full expected artifact;
`sh07-core-artifact-verification-against-expected` trusts a path-bound expected
artifact. Neither is an independent supplied-product verifier.

## Canonical path-slot vocabulary

The only neutral sentinel is the keyword
`:gravity/sh07-semantic-carrier-path-slot-v2`. The following eleven vectors are
the complete slot-address vocabulary, in this order:

~~~clojure
[[:raw-template-result :core-template :provenance]
 [:raw-template-result :core-template :provenance-binding-preimage
  :actual-source-path]
 [:raw-template-result :core-template :lineage
  :authenticated-sh06-artifact-id]
 [:raw-template-result :digest-requests :artifact-id-ordinal :preimage
  :lineage :authenticated-sh06-artifact-id]
 [:raw-template-result :digest-requests :provenance-ordinal :preimage
  :actual-source-path]
 [:digest-requests :artifact-id-ordinal :preimage :lineage
  :authenticated-sh06-artifact-id]
 [:digest-requests :provenance-ordinal :preimage :actual-source-path]
 [:resolved-digests :provenance-ordinal]
 [:canonical-core-artifact :provenance]
 [:canonical-core-artifact :provenance-binding-preimage
  :actual-source-path]
 [:canonical-core-artifact :provenance-binding-id]]
~~~

`:artifact-id-ordinal` and `:provenance-ordinal` are symbolic address tokens,
not stored values. They resolve uniquely by `:purpose` to
`:sh07-core-artifact-id` and `:sh07-core-provenance-binding-id`. Construction
fails unless the run-level and raw-template digest vectors are exactly equal;
both copies are neutralized and later rebound together. A carrier never stores
a physical path, a symlink target, a path hash, or a path-derived diagnostic.

## Closed carrier schema

The carrier is an immutable, fully realized Clojure value with exactly these
top-level keys. `closed-map` below means that extra keys are rejected.

~~~clojure
{:artifact :gravity/sh07-semantic-run-product-carrier-v2
 :schema-version 2
 :status :accepted
 :authority :non-authoritative
 :semantic-key
 {:source-byte-count non-negative-integer
  :source-bytes-sha256 "sha256:<64 lowercase hex>"
  :source-extension one-of #{".gravity" ".qst"}
  :source-kind :gravity/source
  :sh06-semantic-projection-id canonical-id
  :pins pins-map}
 :path-slot-addresses the-eleven-vectors-above
 :shape-manifest canonical-shape-manifest
 :static-envelope static-envelope-map
 :run-product
 {:raw-template-result closed-value
  :canonical-core-artifact closed-value
  :digest-requests vector
  :resolved-digests vector
  :template-verification closed-value
  :resolved-verification closed-value}
 :integrity-hash "sha256:<64 lowercase hex>"
 :canonical-byte-count positive-integer
 :nonclaims
 {:authoritative? false
  :fresh-authoritative-run-required? true
  :cross-process? false
  :cross-stage? false
  :final-artifact-cached? false
  :verification-report-cached? false}}
~~~

`closed-value` permits nil, booleans, integers, strings, keywords, symbols,
vectors, sets, and maps composed only of those types. It rejects lazy
sequences, records, Java objects, functions, metadata, atoms, refs, agents,
volatiles, futures, promises, delays, throwables, and any mutable value. Every
sequence is realized before publication. The carrier contains neither an
SH-06/SH-07 complete artifact, verification report, diagnostic, physical path,
symlink target, nor path hash.

`canonical-shape-manifest` closes every nested map and collection without a
hand-maintained duplicate of the large SH-07 product schema. It is the sorted
vector of `[coordinate descriptor]` pairs produced by a complete iterative
walk of the neutral run product. A coordinate begins at `[]`; a map child adds
`[:map-key canonical-key-bytes]`, a vector child adds `[:vector-index n]`, and
a set child adds `[:set-member canonical-member-bytes]`. Map and set traversal
is sorted by canonical UTF-8 bytes. A descriptor is exactly one of
`[:nil]`, `[:boolean]`, `[:integer]`, `[:string utf8-byte-count]`, `[:keyword]`,
`[:symbol]`, `[:map key-count sorted-canonical-key-bytes]`,
`[:vector element-count]`, or `[:set member-count sorted-canonical-member-bytes]`.
Duplicate canonical member/key encodings, unsupported values, metadata, or a
census above the live SH-07 carrier limits fail construction. The manifest
must exactly equal a fresh recomputation over the stored neutral run product;
therefore every nested allowed key set, collection length, scalar type, and
string byte bound is closed. It is included in carrier integrity.

The integrity preimage is the complete carrier with `:integrity-hash` removed.
Its canonical UTF-8 encoding uses the repository canonical reader encoding and
domain `:gravity/sh07-semantic-run-product-carrier-v2`. Construction initially
sets `:canonical-byte-count` to zero, encodes, replaces the field with the
observed encoded length, and repeats until the value is stable; it then hashes
those exact final bytes. Validation requires the stored count to equal the
encoded length and hashes the same bytes. There is no generic path-removal
projection.

## Live pin and static-envelope schemas

`pins-map` is a closed map. Values are read from the live Vars at construction
and again at lookup; names below are part of the contract, not examples.

~~~clojure
{:request-schema-version 15
 :sh03-c2-reader
 [sh03-reader-expected-source-byte-count
  sh03-reader-expected-source-content-hash
  sh03-reader-expected-plan-semantic-hash
  sh03-reader-expected-functions-semantic-hash
  sh03-reader-expected-function-count
  sh03-reader-expected-function-names-hash
  sh03-reader-expected-function-shapes-hash
  sh03-reader-expected-entrypoint-semantic-hash
  sh03-reader-expected-verifier-semantic-hash
  sh03-reader-expected-builtin-functions-hash]
 :sh04-c3-syntax
 [sh04-syntax-expected-source-byte-count
  sh04-syntax-expected-source-content-hash
  sh04-syntax-expected-plan-semantic-hash
  sh04-syntax-expected-functions-semantic-hash
  sh04-syntax-expected-function-count
  sh04-syntax-expected-function-names-hash
  sh04-syntax-expected-function-shapes-hash
  sh04-syntax-public-function-hashes
  sh04-syntax-public-function-shapes
  sh04-syntax-adapter-contract]
 :sh05-macro
 [sh05-macro-expected-source-byte-count
  sh05-macro-expected-source-content-hash
  sh05-macro-expected-plan-semantic-hash
  sh05-macro-expected-functions-semantic-hash
  sh05-macro-expected-function-count
  sh05-macro-expected-function-names-hash
  sh05-macro-expected-function-shapes-hash
  sh05-macro-public-function-hashes
  sh05-macro-public-function-shapes
  sh05-macro-adapter-contract]
 :sh06-resolution
 [sh06-resolution-expected-source-byte-count
  sh06-resolution-expected-source-content-hash
  sh06-resolution-expected-plan-semantic-hash
  sh06-resolution-expected-functions-semantic-hash
  sh06-resolution-expected-function-count
  sh06-resolution-expected-function-names-hash
  sh06-resolution-expected-function-shapes-hash
  sh06-resolution-public-function-hashes
  sh06-resolution-public-function-shapes
  sh06-resolution-adapter-contract
  sh06-resolution-pass-contract
  sh06-resolution-execution-boundary-contract]
 :sh07-core
 [sh07-core-expected-source-byte-count
  sh07-core-expected-source-content-hash
  sh07-core-expected-plan-semantic-hash
  sh07-core-expected-functions-semantic-hash
  sh07-core-expected-function-count
  sh07-core-expected-function-names-hash
  sh07-core-expected-function-shapes-hash
  sh07-core-public-function-hashes
  sh07-core-public-function-shapes
  sh07-core-adapter-contract]
 :runtime {:java-runtime-version string :clojure-version string}}
~~~

An absent Var, nil pin, changed value, or extra/missing pin fails lookup before
carrier access. `static-envelope-map` is a closed map with these keys:

~~~clojure
{:scope :sh07-b15-keyword-map-lookup
 :adapter :gravity/sh07-to-c6-core-products-v16
 :task "SH-07-B47"
 :module-policy
 {:namespace symbol
  :profile keyword
  :target keyword
  :safety keyword
  :effects sorted-vector-of-keywords
  :capabilities sorted-vector-of-keywords
  :exports canonical-value}
 :source-revision-id canonical-id
 :sh06-semantic-projection-id canonical-id
 :authenticated-request-path-neutral complete-neutral-request}
~~~

`complete-neutral-request` is the entire freshly reconstructed authenticated
request with exactly `[:provenance :actual-source-path]` and
`[:lineage :authenticated-sh06-artifact-id]` replaced by the canonical path
sentinel. No key is omitted and no other value is changed. The other envelope
fields are redundant routing assertions extracted from that request; all must
equal their corresponding request values. Profile, target, safety, effects,
and capabilities are never inferred or defaulted.

## Constructor operation

`sh07-semantic-carrier-from-fresh-run` accepts exactly four arguments: the
bounded source snapshot, a freshly verified SH-06 artifact, its exact
authenticated request, and the ordinary successful SH-07 run product. It
returns either the closed carrier or throws `ExceptionInfo` with the diagnostic
schema below.

Construction performs, in order: validate the source snapshot; require SH-06
verification `:passed`; reconstruct the request with
`sh07-core-authenticated-request` and require exact equality; require all live
pins; require both SH-07 verification products `:passed`; require the six-key
run-product schema; resolve the two digest ordinals uniquely by `:purpose`;
require run/raw digest-vector equality; replace exactly the eleven slots; scan
the whole result for forbidden values and physical paths; compute census,
canonical bytes, and integrity; and publish only after all checks pass.

The constructor is called only after the ordinary full SH-07 artifact and
`sh07-core-artifact-verification` have succeeded. That fresh report is an
argument to admission policy but is never retained. A rejected, thrown,
interrupted, cancelled, malformed, or failed construction publishes nothing.

## Per-path binder operation

`sh07-bind-semantic-carrier` accepts the carrier, a requested source path, the
bounded source snapshot, a freshly built SH-06 artifact, its passed
verification, and the freshly reconstructed authenticated request. It returns:

~~~clojure
{:artifact :gravity/sh07-semantic-carrier-bind-result-v2
 :schema-version 2
 :status :accepted
 :authority :non-authoritative
 :source-path exact-requested-path-string
 :sh07-artifact complete-ordinary-sh07-artifact
 :supplied-verification supplied-verifier-result
 :fresh-authoritative-run-required? true}
~~~

The binder revalidates carrier integrity and live pins, rechecks the source
snapshot and SH-06 verification, reconstructs and exactly compares the request,
compares the static envelope and semantic key, deep-copies the run product,
and binds exactly the eleven slots. It recomputes both digest purposes with the
ordinary canonical digest function, updates both digest-vector copies, and
reconstructs the same final artifact envelope used by
`sh07-core-from-authenticated-request`. Every non-slot value must remain equal
to the integrity preimage. The returned artifact and every nested aggregate
must be a new object; no aggregate may be `identical?` to an aggregate returned
for another path or to one stored in the carrier.

On a hit the binder must not call `sh07-core-run-request-for-test`,
`sh07-core-run-structural-request-for-test`, `sh07-core-from-authenticated-request`,
`sh07-core-from-resolution-artifact`, `sh07-core-artifact-verification`, or
`sh07-core-verification-session`. It retains neither the bound artifact nor its
verification result.

## Independent supplied-product verifier

`sh07-core-verify-supplied-run-product` accepts the exact request, the complete
candidate final artifact containing the bound run product, passed current
SH-06 verification, current live pins, and carrier integrity preimage. Its
closed result is:

~~~clojure
{:artifact :gravity/sh07-supplied-run-product-verification-v2
 :schema-version 2
 :status one-of #{:passed :failed}
 :authority :non-authoritative
 :checks sorted-map-of-keyword-to-boolean
 :failed-checks sorted-vector-of-keywords
 :source-path exact-requested-path-string
 :fresh-authoritative-run-required? true}
~~~

The fixed check catalog is `[:carrier-integrity :closed-schema :live-pins
:request-membership :upstream-verification :source-envelope :static-envelope
:path-slot-completeness :digest-purpose-uniqueness :digest-rebinding
:non-path-equality :template-status :resolved-status :artifact-identity
:provenance-identity :lineage :no-alias]`. Their exact predicates are:

* `:carrier-integrity`: the fixed-point byte count and domain-separated hash
  recomputed from the carrier equal its stored values.
* `:closed-schema`: top-level/run-product key sets are exactly those printed in
  this contract and a recomputed coordinate/type/keyset manifest exactly equals
  `:shape-manifest`.
* `:live-pins`: a newly read closed pins map exactly equals the stored pins map.
* `:request-membership`: SH-06 verification is passed and
  `(sh07-core-authenticated-request current-sh06)` is exactly the supplied
  request.
* `:upstream-verification`: the report artifact kind is the ordinary SH-06
  verification kind, status is `:passed`, `:failed-checks` is empty, and its
  artifact identity equals the current SH-06 artifact identity.
* `:source-envelope`: pre/post file attributes and exact bytes satisfy the
  source rules below, and byte count, SHA-256, extension, path, and SH-06 source
  identity all equal the current request/key.
* `:static-envelope`: neutralizing exactly the two request fields produces the
  stored complete request, and every redundant routing assertion equals its
  source field.
* `:path-slot-completeness`: every one of the eleven concrete coordinates
  exists with the required type, both symbolic ordinals resolve uniquely, and
  replacing exactly those values in the bound product yields the stored
  neutral product and no remaining physical path or current SH-06 artifact id.
* `:digest-purpose-uniqueness`: run/raw vectors are exactly equal, ordinals are
  contiguous from zero, and exactly one request has each required purpose.
* `:digest-rebinding`: for every ordinal, the ordinary resolver applied to its
  exact bound preimage equals the resolved digest at that ordinal; run/raw
  vectors and all digest references resolve to the same values. For the two
  path-sensitive purposes this predicate executes exactly
  `sh07-resolve-identity-preimage` and `sh07-resolve-provenance-preimage`
  through the pinned SH-07 plan, then applies
  `sh07-core-semantic-identity-preimage` where the ordinary resolver does and
  hashes `{:domain :gravity/sh07-declared-digest-v1 :purpose purpose
  :preimage resolved-preimage}` with `reader-canonical-hash`. Other ordinals
  use the ordinary non-plan pure preimage resolution rules. No resolved digest
  supplied by the carrier is trusted as the expected value.
* `:non-path-equality`: after the exact eleven replacements, canonical bytes of
  the whole bound product equal canonical bytes of the carrier run product.
* `:template-status`: raw result is the ordinary template-result artifact with
  schema 16/status accepted/empty diagnostics, template verification is passed,
  and both expose the same request/product/digest identities.
* `:resolved-status`: resolved verification is passed with empty failed checks,
  and its resolved product and digest identities equal the bound product.
* `:artifact-identity`: the unique `:sh07-core-artifact-id` request preimage is
  exactly canonical-core `:identity-preimage`, its recomputed digest equals the
  corresponding resolved digest, and that digest equals canonical-core and
  final-envelope `:artifact-id`.
* `:provenance-identity`: the unique provenance request preimage is exactly
  canonical-core `:provenance-binding-preimage`, contains the exact current
  path, and its recomputed digest equals both the resolved ordinal and
  `:provenance-binding-id`.
* `:lineage`: request, raw template, canonical core, and final envelope have
  exactly the current source revision, SH-06 semantic projection, and current
  SH-06 artifact identities at their schema-defined coordinates; no carrier
  sentinel remains in a bound value.
* `:no-alias`: an identity-set walk finds no aggregate object shared with the
  carrier run product or with any prior result retained for this cache entry.

The verifier independently checks each predicate and fails if any catalog
entry is absent or non-boolean. It may call pure schema, census, canonical
encoding, digest, lineage, provenance, and identity-preimage helpers. Its only
permitted Gravity-plan executions are the two named path-sensitive resolver
functions for the `:digest-rebinding` predicate; call count and function symbol
are asserted by tests. It must not call any of the six full-build/session
functions forbidden to the binder, execute any other Gravity function, resolve
a fresh template, compare with a cached artifact/report, or trust an already
passed check. Passing this verifier is required before the binder returns
accepted.

## Source, diagnostic, and resource rules

One source snapshot is read through `java.nio.file.Files` with no-follow link
attributes. Before and after the bounded read, the path must exist, be regular,
not be a symbolic link, and have equal file key, size, and last-modified time.
The read rejects more than 16 MiB, invalid UTF-8, NUL, a missing extension, or
an extension outside `.gravity` and `.qst`. The SHA-256 is over exact bytes.
Mutation between snapshot, SH-06 construction, lookup, and bind is detected by
a final attribute/hash recheck. No `slurp` or canonical-path resolution is
permitted in this lane.

Every failure throws `ExceptionInfo` whose `ex-data` is exactly:

~~~clojure
{:artifact :gravity/sh07-semantic-cache-diagnostic
 :rule "SH07-SEMANTIC-CACHE"
 :severity :error
 :stage :development-cache
 :reason one-of-closed-reasons
 :source-path exact-requested-path-or-nil
 :fail-closed true
 :fresh-authoritative-run-required? true}
~~~

The closed reasons are `:invalid-selection`, `:source-missing`,
`:source-not-regular`, `:source-symlink`, `:source-too-large`, `:invalid-utf8`,
`:invalid-extension`, `:source-mutated`, `:sh06-verification-failed`,
`:request-membership-mismatch`, `:stale-pin`, `:carrier-schema`,
`:carrier-too-large`, `:carrier-integrity`, `:path-slot-mismatch`,
`:digest-purpose-mismatch`, `:supplied-verification-failed`,
`:reentrant-request`, `:producer-interrupted`, and `:producer-failed`.
Diagnostics and exception messages never enter a key or cache value.

Each carrier is at most 256 MiB canonical bytes. The cache has an explicit
positive `maximum-entries` no greater than 4 and `maximum-canonical-bytes` no
greater than 512 MiB. Admission rejects a single oversized carrier. Accounting
uses canonical byte counts, not JVM heap estimates.

## Cache and concurrency state machine

The process-local cache is a fixed-capacity FIFO, not LRU. Its key is the
closed semantic-key plus static envelope; it contains no path. State is one
atom holding `{:order vector :entries map :bytes integer :flights map}`. A
single monitor protects the following atomic transitions:

1. A hit validates current pins and integrity before incrementing `:hits`.
2. The first miss installs one promise-like flight and becomes producer.
3. Same-key waiters register outside carrier state and wait interruptibly;
   different keys may produce concurrently.
4. The producer performs work outside the monitor. Reentry by the producer for
   the same key fails `:reentrant-request` instead of waiting on itself.
5. Only a successful constructed carrier is published. FIFO eviction removes
   oldest keys until both limits hold; replacement does not move a key.
6. Failure, cancellation, or interruption removes the flight, delivers a
   failure to all waiters, and stores no carrier. Interrupted threads restore
   their interrupt flag. A later request is a fresh miss and may retry.

Counters are monotonic and distinguish hits, producer misses, joined waiters,
published entries, evictions, failed producers, and rejected hits. They are
observability only and have no authority.

## Exact C10 development routing

Reuse is enabled only when selection mode is exactly `:test-vars` and the
selection vector, with no duplicate, missing, or extra value, equals this set:

~~~clojure
#{gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-source-has-exact-authentic-coverage
  gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-calls-lookups-and-quotes-are-exact
  gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-is-deterministic-path-neutral-and-provenanced
  gravity.self-hosting.sh07-c10-safety-source-coverage-test/sh07-b31-c10-replay-and-high-value-alterations-are-contained}
~~~

The current C10 source bytes are snapshotted and hashed live; no committed
fixture-content hash is trusted. Namespace selection, subsets, supersets,
duplicates, mixed variables, unknown variables, altered bytes, or any pin
mismatch bypass reuse entirely and execute the existing ordinary runner. The
fresh authoritative runner and release paths never construct or consult this
cache. A bypass is not a cache miss and publishes nothing.

## Required acceptance evidence

The implementation is admissible only with focused positive and negative tests
that prove all clauses above, including: same bytes at two regular paths in two
temporary roots; equal semantic identity but distinct exact provenance; no
aggregate aliasing; ordinary artifact/status/diagnostic parity for accepted and
rejected B8 fixtures; every slot alteration and one non-slot alteration; each
live pin family; profile, target, effect, capability, and safety changes;
missing/symlink/mutation/oversize/UTF-8 cases; exact C10 selection and every
bypass class; successful-only admission; producer failure, cancellation,
interruption, retry, reentry, same-key single-flight, different-key progress,
FIFO order, entry and byte eviction; stable diagnostic shapes; and assertions
that all forbidden full-build functions have zero calls on a hit and that the
only plan calls are the two exact resolver symbols above.

The evidence must also show that admission performs one ordinary full SH-07
build and fresh verification, while a subsequent second-path hit performs no
SH-07 template construction or lowering and only the two bounded resolver
leaves plus the independent supplied-product verifier. The result
must state `:authority :non-authoritative` and
`:fresh-authoritative-run-required? true`. No timing result, cache hit, or
supplied verification advances SH-07 roadmap authority.
