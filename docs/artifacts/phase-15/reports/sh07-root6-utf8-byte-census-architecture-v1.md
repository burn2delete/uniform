# SH-07 Root-6 Exact UTF-8 Byte Census Architecture V1

Status: Draft exact scalar-observation seam for pre-freeze audit

Date: 2026-08-31

## Purpose

This architecture-only decision closes one bounded SH-07/Root-6 replay gap.
The current Gravity helper `sh07-utf8-byte-count` returns four times the host
string carrier length because the Stage2 subset lacks exact character-to-code
point observation. That conservative upper bound can reject legal carriers and
cannot independently replay exact aggregate scalar-byte census at the governed
256 MiB and 1 GiB carrier limits.

Integrated Attempt 15 already permits the B51 producer, independent source
oracle, and separately authored Root 6 to share one exact scalar UTF-8
validity/count predicate while requiring all collection traversal, aggregation,
decoration, sorting, and caller mapping to remain independent. This decision
defines that smallest shared seam. It observes one already-selected string and
threads a caller-supplied aggregate count under an authenticated maximum. It
does not enumerate a carrier, print a value, allocate encoded bytes, order
text, hash data, or change any existing bound.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline

```text
authoritative main commit
1be7a083aecbf24888766e950980e4f5875e23a4

authoritative main tree
294af4492a599d9fa1abe359aa07d0ddbe97c3c2

integrated G13 duplicate/recur architecture
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-13.md

integrated bounded collection architecture
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-15.md

integrated caller-scoped equality architecture
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-17.md

integrated decimal semantic-inverse architecture
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-19.md
```

The governing contracts are `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`,
`D9`, `L1`, `L2`, `C2`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`, `TEST11`,
`TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Preserved authority and bounds

Attempt 15's exact canonical-order printer limits remain:

```text
value-node occurrences                  4096
root-relative depth                       96
one collection width                     512
one scalar spelling UTF-8 bytes         32768
final canonical output UTF-8 bytes     262144
```

The SH-07 proof contract and current checked-core carrier bounds remain:

```text
module scalar bytes                    268435456
template scalar bytes                 1073741824
resolved-core scalar bytes            1073741824
generated-digest scalar bytes         1073741824
```

No maximum is widened, narrowed, reinterpreted, or made profile/backend
dependent. The caller remains responsible for selecting and authenticating the
active maximum. Passing a number to the seam does not confer carrier authority.

Attempt 17 remains exact: producer, oracle, and Root 6 compare complete bytes
only within each caller; their public envelopes remain distinct. Attempt 19's
decimal spelling/inverse remains independently authored. G13's eight roots,
Root-8 arity one, six-key envelopes, schema 18, 19 purposes, 58 edges, 94/174
paths, four outcomes, one pending detector, four pending families, eight
reasons, two resource reasons, two unreachable mappings, and one failure-only
purpose remain unchanged.

## Exact internal seam

The seam is:

```text
name             sh07-observe-utf8-byte-count
positional arity 3
arguments        [text observed-before maximum]
text             one exact Gravity string
observed-before  exact integer, 0 <= observed-before <= maximum
maximum          exact integer, 0 <= maximum <= 1073741824
effects          #{}
capabilities     #{}
```

Its compiler/runtime type is:

```clojure
[:fn [:string :nonnegative-exact-integer :nonnegative-exact-integer]
 [:record
  {:artifact :gravity/sh07-utf8-byte-observation-v1
   :status [:enum :accepted :rejected]
   :reason [:maybe-keyword]
   :maximum :nonnegative-exact-integer
   :observed-before :nonnegative-exact-integer
   :observed-after :nonnegative-exact-integer
   :prospective-after [:maybe-nonnegative-exact-integer]
   :utf16-units-scanned :nonnegative-exact-integer
   :unicode-scalars-scanned :nonnegative-exact-integer}]]
```

The result always has exactly those nine keys. Its variants are:

```clojure
accepted
{:artifact :gravity/sh07-utf8-byte-observation-v1
 :status :accepted
 :reason nil
 :maximum maximum
 :observed-before observed-before
 :observed-after exact-total
 :prospective-after exact-total
 :utf16-units-scanned exact-input-utf16-units
 :unicode-scalars-scanned exact-input-scalar-count}

limit rejection
{:artifact :gravity/sh07-utf8-byte-observation-v1
 :status :rejected
 :reason :utf8-byte-limit
 :maximum maximum
 :observed-before observed-before
 :observed-after committed-before-offender
 :prospective-after exact-count-including-offending-scalar
 :utf16-units-scanned utf16-index-before-offender
 :unicode-scalars-scanned scalar-count-before-offender}

Unicode rejection
{:artifact :gravity/sh07-utf8-byte-observation-v1
 :status :rejected
 :reason :invalid-unicode-scalar-sequence
 :maximum maximum
 :observed-before observed-before
 :observed-after committed-before-malformed-unit
 :prospective-after nil
 :utf16-units-scanned utf16-index-of-malformed-unit
 :unicode-scalars-scanned scalar-count-before-malformed-unit}
```

Wrong arity is contained `L2-BUILTIN-ARITY`. A non-string argument, noninteger
counter, negative counter, `observed-before > maximum`, or maximum above
1073741824 is contained `L2-BUILTIN-ERROR`. Invocation failures do not fabricate
an observation record and never escape as host exceptions.

## Why the result is not a raw count

A unary raw count cannot satisfy the aggregate replay boundary by itself. It
cannot distinguish malformed Unicode from a limit failure without a sentinel
or exception. It forces each caller to duplicate prospective addition and
overflow handling. It also cannot test an exact 1 GiB logical aggregate without
constructing a 1 GiB string or separately mutating caller state.

The arity-three observation threads the exact already-committed aggregate and
active maximum. Its rejected record identifies the first scalar that would
cross the bound without committing it. A tiny input with a near-limit
`observed-before` therefore proves exact high logical boundaries without a
heavyweight allocation. The record carries no source coordinate, semantic id,
carrier path, ordering key, digest, text, encoded bytes, or partial encoding.

## Strict Unicode and UTF-8 algorithm

The semantic input is a Gravity Unicode-scalar string. A UTF-16-backed runtime
must independently prove that its carrier is a valid encoding of that string.
The exact prospective scan starts at UTF-16 index zero, scalar count zero, and
`observed-after = observed-before`.

At each UTF-16 position:

1. code unit `0x0000..0x007f` denotes one scalar of width 1;
2. code unit `0x0080..0x07ff` denotes one scalar of width 2;
3. code unit `0x0800..0xd7ff` or `0xe000..0xffff` denotes one scalar of width 3;
4. high surrogate `0xd800..0xdbff` must be followed immediately by low
   surrogate `0xdc00..0xdfff`; the pair denotes exactly one scalar in
   `U+10000..U+10ffff`, has width 4, and advances two UTF-16 units; and
5. an unpaired high surrogate, high surrogate followed by non-low, isolated low
   surrogate, reversed pair, or incomplete final pair rejects exact
   `:invalid-unicode-scalar-sequence` before counting that unit.

For an admitted next scalar of width `w`, compute limit admission without
overflow:

```text
w <= maximum - observed-after
```

If true, commit `observed-after + w`, advance by one scalar and one or two
UTF-16 units, and continue. If false, do not commit. Return limit rejection
with exact `prospective-after = observed-after + w`. Because the contract
requires `observed-after <= maximum <= 1073741824` and `w <= 4`, that evidence
is at most 1073741828 and is representable by the required exact integer. No
fixed-width wraparound participates.

The empty string is accepted with unchanged counts. End of input returns the
accepted record. The scan is linear in UTF-16 units, uses constant traversal
state, and never constructs a codepoint vector, UTF-8 byte array, byte buffer,
substring, replacement string, normalized string, or encoded copy.

The algorithm is semantic, not JVM-specific. A runtime whose native string
representation is Unicode scalars applies the same scalar widths and reports
the equivalent UTF-16 unit census: one unit through `U+ffff`, two units above.

## Compiler and runtime contract

The compiler admits this as one internal pure capability-free primitive in the
SH-07 compiler-artifact context. Constant folding, Stage2 execution, seed
execution, and every runtime implementation must return the same nine-key
record for the same arguments. They may use indexed UTF-16/codepoint access but
must not call host `getBytes`, encoder APIs, replacement decoders, locale,
normalization, reflection, callback, FFI, I/O, clock, randomness, hashing,
printing, or generic serialization.

The primitive must be total over well-typed, range-valid arguments. Runtime
allocation is bounded by its constant-size result only. Backend integer
selection must represent every value through 1073741828 and retain checked
prospective comparison; narrowing, saturation, wrapping, floating conversion,
or platform-size integers are forbidden.

The compiler/runtime binding must record exact arity, type, purity,
capability-free status, admitted module/profile context, implementation
identity, and closure. A missing implementation, wrong arity, wrong result
keys, impossible count, or host exception is contained `L2-BUILTIN-ERROR`.

## Authorized caller closure

The exact authorized semantic callers are:

- SH-06/SH-07 authenticated carrier scalar-budget preflight for the governed
  module, template, resolved-core, and generated-digest carrier classes;
- the B51 producer's scalar and final-output accounting closure;
- the independently authored source oracle's scalar accounting closure; and
- Root 6's separately authored aggregate replay and scalar accounting closure.

Each caller must independently select scalar occurrences, derive or authenticate
the exact text presented to the seam, choose the active bound, preserve its own
traversal order, and thread only `observed-after` from an accepted result. The
seam cannot inspect a non-string value, coerce with `str`, choose a readable
spelling, enumerate a vector/list/map/set, detect a collection cycle, combine
sibling summaries, sort, select a failure, construct a public envelope, or
finalize a diagnostic.

Attempt 15's sharing allowance is scalar-only. Producer, oracle, and Root 6
remain independently authored for collection traversal, active-path state,
node/depth/width accounting, unordered probes, commutative failure fold,
decoration, sorting, collision handling, and caller mapping.

Root 8, other public roots outside their already authorized scalar-preflight
closure, unrelated compiler modules, application/runtime source, dynamic
resolve, apply, reflection, callback, FFI, generic printers, generic encoders,
generic sort, and generic compare are forbidden callers. The seam is not a
public standard-library UTF-8 API.

## Error containment and caller mapping

Carrier preflight maps exact `:utf8-byte-limit` to its existing
`:carrier-scalar-byte-bound` rejection. Attempt-15 scalar spelling accounting
maps it to contained `:scalar-byte-limit`; final-output aggregation retains
contained `:output-byte-limit`. Invalid Unicode and invocation/runtime contract
failures map to the caller's existing source-integrity boundary.

For Root 1 that boundary remains exact
`:template-boundary-rejected` with outer
`:source-integrity-mismatch`. For Root 6 it remains exact
`:independent-verifier-boundary-rejected` with outer
`:source-integrity-mismatch`. The source oracle records `C6-VERIFY`. A failure
inside an unordered child probe becomes Attempt-16's exact opaque
`:unordered-decoration-failure` before caller mapping.

The observation reasons do not add a B51 pending reason, public result tag,
diagnostic family, schema field, success purpose, dependency edge, controlled
path, or finalizer route. Root 8 receives no observation and no authority.

## Exact evidence obligations

Direct positive evidence must cover:

1. empty and ASCII strings, including NUL and `U+007f`;
2. exact width transitions `U+0080`, `U+07ff`, `U+0800`, `U+d7ff`, `U+e000`,
   and `U+ffff`;
3. supplementary boundaries `U+10000` and `U+10ffff` as one scalar, two UTF-16
   units, and four UTF-8 bytes;
4. mixed ASCII, BMP, and supplementary strings with exact bytes, UTF-16 units,
   scalar count, and nonzero `observed-before`;
5. empty input and each width at maximum exactly accepted;
6. ASCII from `maximum` rejected with exact `maximum + 1`, BMP and supplementary
   first-over prospective counts, and no offender commit;
7. maximum 268435456 and 1073741824 accepted exactly using a small final string
   plus a near-limit `observed-before`, followed by exact maximum+1 rejection;
8. chunk-composition evidence that sequential observations equal observation
   of concatenated valid text whenever neither path crosses the bound; and
9. producer, source oracle, Root 6, Stage2, seed, and runtime parity for the
   same direct vectors and caller-specific boundary mappings.

High-bound tests must not allocate proportional to 256 MiB or 1 GiB. They use
exact prior counts plus strings of at most four UTF-8 bytes, checked arithmetic,
and a reduced-bound exhaustive/property oracle. Allocation instrumentation and
static scans must prove no encoded byte array, byte buffer, substring, or
codepoint vector is created.

Negative evidence must cover isolated high and low surrogates, high followed by
BMP, reversed low/high, two highs, truncated final high, malformed after a
valid prefix, wrong argument types/ranges/arity, maximum 1073741825, and every
closed-record field mutation.

Mutation evidence must reject:

- the current `4 * carrier-length` conservative approximation;
- UTF-16 unit count, Unicode scalar count, UTF-32 width, CESU-8 six-byte
  supplementary encoding, or replacement-character recovery;
- treating a surrogate pair as two scalars or accepting any unpaired surrogate;
- allocating/calling a host encoder or trusting its replacement/error mode;
- unchecked `observed + width`, fixed-width wrap, saturation, floating count,
  platform-size count, or off-by-one `>=` rejection at the exact maximum;
- committing the offending scalar, reporting only maximum rather than exact
  prospective count, scanning after first limit failure, or losing malformed
  position/census evidence;
- resetting `observed-before`, authenticating a maximum inside the seam,
  confusing the 268435456 and 1073741824 bounds, or using the seam result as
  carrier admission without caller replay;
- value coercion, readable printing, ordering, hashing, digesting, sorting,
  coordinate selection, or public envelope construction in the seam;
- shared collection traversal/probe/fold/sort code between producer, oracle,
  and Root 6; and
- access from Root 8, application code, dynamic resolution, reflection, FFI,
  generic encoding, or other unauthorized closure members.

## Preserved order and digest separation

The observation record contains only counts and scan position. It never returns
UTF-8 bytes or a comparison key. `sh07-canonical-text-compare` remains the sole
ordering primitive in the governed B51 collection-order closure. The
observation seam cannot call it and its counts cannot order equal or unequal
strings.

`sh07-declared-digest-hash(input) = reader-canonical-hash(input)` remains the
sole governed B51 digest identity. Counts, UTF-16 positions, scalar counts, and
observation records never enter digest preimages, dependency ordering, ids, or
collision tie-breaking.

## Pins and implementation consequences

This report authorizes no implementation or pin change. A later governed
atomic implementation may replace the conservative helper and add the internal
primitive only after this exact candidate is frozen, independently accepted,
made integration-eligible, and reconciled to authoritative main.

No partial Root6-only, producer-only, seed-only, runtime-only, preflight-only,
test-only, or pin-only change may land. Existing B47 source/local pins, B51
source/Stage2/whole-file pins, proof contract bounds, root ABIs, public schemas,
and Attempt-15/17/19 identities remain unchanged without separate authority.

## Governance and lifecycle

This workstream id is `sh07-root6-utf8-byte-census-architecture-v1`. Its
invariant family is
`architecture/self-hosting-sh07-root6-utf8-byte-census-v1`. Its lifecycle
dependencies are integrated
`sh07-b51-vector-destructuring-architecture-v18-attempt-15`,
`sh07-b51-vector-destructuring-architecture-v18-attempt-17`, and
`sh07-b51-vector-destructuring-architecture-v18-attempt-19`. It starts from
authoritative main `1be7a083aecbf24888766e950980e4f5875e23a4`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration. It does not freeze, request review, accept, or
confer integration eligibility. The author does not self-review. The report
candidate owns only this file; draft registration owns only
`contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, primitive substrate, and
observer. It is not semantic authority.

This report does not define general Unicode normalization, grapheme counting,
string slicing, encoding output, text ordering, hashing, serialization, a
public UTF-8 library, or new carrier limits. It does not prove arbitrary
1 GiB allocation is practical; it proves exact logical counter admission under
the already governed bound without requiring evidence to allocate that many
bytes. It does not claim implementation, aggregate SH-07 completion,
self-hosting, seed retirement, release, performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer must confirm:

1. The decision realizes only Attempt-15's already permitted shared scalar
   UTF-8 validity/count predicate and leaves collection algorithms independent.
2. The exact seam is pure, capability-free, internal, arity three, string-only,
   range-bounded through 1073741824, and returns the exact nine-key record.
3. The UTF-16/codepoint scan accepts every valid scalar with width 1/2/3/4 and
   rejects every malformed surrogate shape without replacement or exception.
4. Prospective comparison commits exact-at-limit, rejects the first over-limit
   scalar before commit, reports exact prospective count through 1073741828,
   and cannot overflow.
5. The result record, rather than a raw count/sentinel, is justified by
   malformed-versus-limit containment, aggregate threading, and allocation-free
   high-bound evidence.
6. Compiler, Stage2, seed, and runtime implementations have exact parity and do
   not allocate encoded bytes, call host encoders, coerce values, print, sort,
   hash, reflect, or use ambient authority.
7. Authorized closure is exact; Root 8 and general application/runtime code are
   excluded; producer/oracle/Root6 collection traversal remains independently
   authored.
8. Evidence covers ASCII, BMP, supplementary, malformed, mixed, exact maximum,
   maximum+1, 268435456/1073741824 logical bounds, chunk composition, caller
   mapping, allocation, closure, and wrong-algorithm mutations.
9. The seam has no ordering or digest authority and changes no G13 root, ABI,
   tag, schema, count, purpose, edge, path, pending reason, diagnostic family,
   carrier bound, printer bound, or pin.
10. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, and exact range-diff checks pass.
11. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
