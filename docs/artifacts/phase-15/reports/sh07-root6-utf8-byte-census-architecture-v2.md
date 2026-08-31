# SH-07 Root-6 Exact UTF-8 Byte Census Architecture V2

Status: Draft successor correcting authority history only

Date: 2026-08-31

## Purpose and exact delta

This architecture-only decision is the narrow successor to terminally rejected
`sh07-root6-utf8-byte-census-architecture-v1`. V1 was rejected for one false
authority statement: it described terminally rejected Attempt 15 as integrated.
The independent rejection found V1's technical arity-three observation,
Unicode scan, prospective counting, high-bound evidence, closure, containment,
and separation rules otherwise coherent.

V2 preserves every one of those coherent technical rules. It corrects only
their authority and lifecycle provenance:

- Attempt 15 is terminally rejected history, never an integrated baseline or
  lifecycle dependency;
- the viable scalar-sharing, bounds, cycle, collection-order, and failure-fold
  rules first explored in that rejected history are incorporated into the
  authoritative lineage through integrated Attempt 17; and
- V2's only lifecycle dependencies are integrated Attempt 17 and integrated
  Attempt 19.

No technical rule acquires authority merely because it appeared in Attempt 15.
Where this report describes a preserved sharing or bound rule, its authority is
integrated Attempt 17. Attempt 15 is cited only to make rejection history and
the V1 correction auditable.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and history

```text
authoritative main commit
5f4200be1bef02f0b69019d01fb3df239fea5c5a

authoritative main tree
2aae7c3cd9e085e8adcc9ee3b128e7ba7660ad53

terminally rejected V1 candidate
6d1656733d7c4327b829046dd17f8ab59071131d

terminally rejected V1 tree
8ea59abf3c41f53a4147f0e8dfa148b1e0d0b170

terminally rejected V1 report SHA-256
725318d6c8fdae4d3bdbc6bb6b9e0d73c9157ed6080fdb5cd599a3baa50840d9

terminal rejection history only
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-15.md

integrated caller-scoped equality, scalar-sharing, bound, and collection rules
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-17.md

integrated decimal semantic-inverse architecture
docs/artifacts/phase-15/reports/
sh07-b51-vector-destructuring-architecture-decision-v18-attempt-19.md
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`,
`D9`, `L1`, `L2`, `C2`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`, `TEST11`,
`TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Normative preservation of V1 technical clauses

V2 incorporates the technical contents of V1's following sections without
semantic change: `Preserved authority and bounds`, `Exact internal seam`, `Why
the result is not a raw count`, `Strict Unicode and UTF-8 algorithm`, `Compiler
and runtime contract`, `Authorized caller closure`, `Error containment and
caller mapping`, `Exact evidence obligations`, `Preserved order and digest
separation`, `Pins and implementation consequences`, `Nonclaims`, and the
technical independent-acceptance criteria. References in those clauses to an
Attempt-15 allowance or bound are corrected to mean the corresponding rule
incorporated through integrated Attempt 17. References to Attempt 15 as an
identity or historical experiment remain terminal-rejection history only.

For avoidance of doubt, the preserved contract is restated below at its exact
semantic boundary.

## Exact seam and closed result

The internal pure capability-free seam remains:

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

It always returns the same exact nine-key
`:gravity/sh07-utf8-byte-observation-v1` record with keys `:artifact`,
`:status`, `:reason`, `:maximum`, `:observed-before`, `:observed-after`,
`:prospective-after`, `:utf16-units-scanned`, and
`:unicode-scalars-scanned`. Its only record variants remain accepted,
`:utf8-byte-limit`, and `:invalid-unicode-scalar-sequence`, with the exact field
meanings and commit points specified by V1. Wrong arity remains contained
`L2-BUILTIN-ARITY`; wrong type or range and runtime contract failure remain
contained `L2-BUILTIN-ERROR`, never host exception or fabricated observation.

The scan remains an exact prospective UTF-16/codepoint scan: widths are 1 for
`U+0000..U+007f`, 2 for `U+0080..U+07ff`, 3 for valid BMP scalars outside the
surrogate range, and 4 for a valid high/low surrogate pair representing
`U+10000..U+10ffff`. Every unpaired, reversed, or truncated surrogate shape
rejects before commit. Admission remains the overflow-free comparison
`width <= maximum - observed-after`; exact-at-limit commits, while the first
over-limit scalar does not commit and reports the exact prospective count.
That count is bounded by 1073741828.

The implementation remains linear, constant-state, and allocation-free except
for the fixed result. It constructs no encoded byte array, byte buffer,
substring, codepoint vector, replacement text, normalized text, or encoded
copy, and calls no host encoder, replacement decoder, locale, normalization,
reflection, callback, FFI, I/O, clock, randomness, hash, printer, generic
serializer, generic sorter, or generic comparator.

## Preserved bounds and caller independence

Integrated Attempt 17 carries the unchanged canonical value limits: 4096 value
node occurrences, depth 96, width 512, scalar spelling 32768 UTF-8 bytes, and
final output 262144 UTF-8 bytes. The proof-contract carrier limits remain
268435456 module scalar bytes and 1073741824 template, resolved-core, and
generated-digest scalar bytes. V2 widens, narrows, and reinterprets none of
them. Callers authenticate the applicable maximum; the seam never does.

The exact caller closure remains limited to authenticated SH-06/SH-07 carrier
scalar-budget preflight, B51 producer scalar/final-output accounting, the
independently authored source oracle, and Root 6's separately authored
aggregate replay. Each caller independently selects scalar occurrences,
derives text, chooses its authenticated maximum, traverses, aggregates, and
maps failures. The seam neither enumerates collections nor shares collection
traversal, active-path state, node/depth/width accounting, unordered probes,
commutative failure fold, decoration, sorting, collision handling, or public
envelope construction.

Root 8, unauthorized public roots, application/runtime source, dynamic
resolution, apply, reflection, callback, FFI, and generic encoding/printing/
sorting/comparison remain forbidden. The seam is not a public UTF-8 library.

Caller mappings remain exact: carrier census maps `:utf8-byte-limit` to
`:carrier-scalar-byte-bound`; scalar spelling maps to `:scalar-byte-limit`;
final output maps to `:output-byte-limit`; malformed Unicode and invocation
failure map to the caller's source-integrity boundary. Root 1 retains
`:template-boundary-rejected` and Root 6 retains
`:independent-verifier-boundary-rejected`, both under
`:source-integrity-mismatch`. Unordered child failure remains the opaque
`:unordered-decoration-failure`. No B51 pending reason, tag, schema field,
purpose, edge, path, digest, coordinate, or Root-8 route is added.

## Preserved separation and evidence

The result contains counts and scan position only. It carries no text bytes,
comparison key, source coordinate, path, semantic id, digest, carrier authority,
or public-envelope authority. `sh07-canonical-text-compare` remains the sole
ordering primitive in its governed closure; the census cannot call it or order
values. The declared digest hash remains the sole digest identity; census data
never enters preimages, dependency ordering, ids, or collision tie-breaking.

All V1 direct, negative, mutation, caller-parity, allocation, closure, and
high-logical-bound evidence obligations remain mandatory. In particular:

- ASCII, BMP, supplementary, mixed, empty, and every malformed surrogate case;
- every UTF-8 width boundary, exact maximum, maximum+1, chunk composition, and
  nonzero prior count;
- exact 268435456 and 1073741824 logical limits using a near-limit prior count
  and no allocation proportional to those limits;
- parity across compiler folding, Stage2, seed, runtime, producer, oracle, and
  Root 6 with caller-specific containment;
- rejection of four-times-length, UTF-16 count, scalar count, UTF-32, CESU-8,
  replacement, wrap, saturation, narrowing, floating, off-by-one, offender
  commit, continuation after first failure, and unauthenticated bounds; and
- static and dynamic proof of exact caller closure, independent collection
  algorithms, constant allocation, closed records, and absence of ordering or
  digest authority.

Integrated Attempt 19 remains the decimal semantic-inverse prerequisite and is
unchanged. G13 roots, Root-8 arity, six-key envelopes, schemas, purpose/edge/
path counts, outcomes, pending detector/families/reasons, unreachable mappings,
and failure-only purpose remain unchanged.

## Governance and lifecycle

This workstream id is `sh07-root6-utf8-byte-census-architecture-v2`. Its
invariant family remains
`architecture/self-hosting-sh07-root6-utf8-byte-census-v1`. Its complete and
only lifecycle dependency vector is:

```text
sh07-b51-vector-destructuring-architecture-v18-attempt-17
sh07-b51-vector-destructuring-architecture-v18-attempt-19
```

Attempt 15 and V1 are terminal rejection history, not dependencies. This task
creates an immutable report-only candidate followed by a separate draft ledger
registration. It does not freeze, request review, accept, confer integration
eligibility, or authorize implementation or pins. The author does not
self-review.

## Nonclaims and independent review

The host remains reader, strict decoder, SH-06/B47 host, Stage2 executor,
runtime-check host, digest transport, primitive substrate, and observer, not
semantic authority. V2 does not define normalization, grapheme counting,
slicing, encoding output, ordering, hashing, serialization, a public UTF-8 API,
or a new bound. It claims no implementation, test, pin, aggregate SH-07
completion, self-hosting, seed retirement, release, or performance result.

An independent reviewer must verify that V2 changes only the rejected authority
and lifecycle wording; that Attempt 15 appears only as terminal history; that
the only dependencies are integrated Attempts 17 and 19; that every coherent
V1 technical seam, algorithm, bound, closure, containment, separation, and
evidence rule remains exact; and that documentation, roadmap, governance,
language-boundary, ASCII, EOF, ownership, identity, and exact-range checks pass.
The author must stop at draft registration.
