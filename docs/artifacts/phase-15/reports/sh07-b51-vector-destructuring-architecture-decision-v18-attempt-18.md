# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 18

Status: Draft canonical decimal readable-spelling prerequisite for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision amends integrated Attempt 17 at one missing
prerequisite. Attempt 17 requires positive decimal ordering by exact canonical
C11 readable text. The current governed C11 bounded printer admits no decimal
scalar spelling, while C2 already authenticates an exact host-independent
decimal semantic key. Therefore the positive decimal evidence required by
Attempt 17 cannot be produced without inventing host formatting.

Attempt 14 correctly forbids raw exponent spelling, raw scale, retained trailing
zeroes, locale, host decimal representation, and ad hoc formatting as ordering
authority. Attempt 18 preserves that prohibition. It defines one minimal,
host-independent value-to-readable inverse of the accepted C2 decimal semantic
key, bounds it, and requires the B51 producer, independent source oracle, and
Root 6 to derive it independently. It changes no collection order, comparator,
failure fold, caller envelope, digest, root ABI, G13 topology, or public schema.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and history

```text
authoritative main commit / integrated Attempt-17 commit
57914082a4e6ef523778b4b24e4a2f93363b7fac

authoritative main tree
fca8c1232b1ab0d96902070f789196727f34890c

integrated Attempt-17 candidate
ddae98a7e6d56cb31f788c4fecb82377d0f16a70

integrated Attempt-17 candidate tree
7588d812fa371a93459be79fe4e286dadf6ef1c0

integrated Attempt-17 report SHA-256
367f1c62fdb7799ad276963e8c415cee68babc35cc9df622254ef0a81dc285d2
```

Attempts 14, 15, and 16 remain terminal rejection history. Their viable
comparator/order, bounds/cycles, and commutative unordered-failure decisions
were incorporated and corrected by independently accepted Attempt 17. Attempt
18 is not a retry of those rejected candidates and does not reopen their
resolved blockers.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`,
`D9`, `L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`,
`bootstrap/clojure/src/gravity/bootstrap.clj`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Incorporated Attempt-17 authority

Attempt 18 incorporates Attempt 17 in full except its unmet assumption that an
exact canonical C11 decimal spelling already exists. The pure binary
`sh07-canonical-text-compare` primitive remains strict unsigned UTF-8 text
comparison and gains no formatting or numeric authority.

B51 still orders sets by complete canonical member text and maps by complete
canonical key text then value text. Vectors and lists retain authenticated
left-to-right order. Bounded independent probes, active-path identity cycles,
shared-DAG recount, commutative Boolean-OR failure aggregation, opaque
`:unordered-decoration-failure`, current-collection versus child-failure
mapping, and caller-scoped byte equality remain exact.

The exact Attempt-15 bounds remain:

```text
maximum value-node occurrences                 4096
maximum root-relative depth                      96
maximum width of any collection                 512
maximum UTF-8 bytes of any scalar spelling     32768
maximum final canonical output UTF-8 bytes     262144
```

G13 digest identity remains
`sh07-declared-digest-hash(input) = reader-canonical-hash(input)`. Decimal
readable text orders values only; it never replaces a digest preimage or id.

## Sole decimal authority: the authenticated C2 semantic key

The only admitted input to canonical decimal spelling is an authenticated,
exactly normalized C2 semantic key with this exact closed shape:

```clojure
{:kind :decimal
 :sign sign
 :coefficient coefficient-codepoints
 :decimal-power decimal-power}
```

Its value is:

```text
sign * unsigned(coefficient) * 10^decimal-power
```

The key is already normalized by C2:

- `sign` is exactly `-1` or `1`;
- coefficient is a nonempty vector of ASCII decimal digit codepoints;
- coefficient has no leading zero except the one-digit zero coefficient;
- a nonzero coefficient has no trailing zero;
- zero is exactly sign `1`, coefficient `[48]`, decimal power `0`; and
- decimal power is either a bounded exact integer or C2's exact canonical
  signed-decimal-power record with normalized sign and digit codepoints.

The signed-record alternative has this exact closed shape:

```clojure
{:artifact :gravity/signed-decimal-power
 :sign one-of-[:negative :positive]
 :digit-codepoints nonempty-normalized-ASCII-decimal-digits}
```

Its digit vector has no leading zero, is not the zero magnitude, and carries no
host integer, raw spelling, width, radix, scale, or cached formatted text.

The printer must independently authenticate those facts and the semantic-value,
form, token, literal-decoding, and occurrence joins required by the caller. A
host decimal object, C6 projection descriptor, raw token, scale, unscaled host
integer, exponent spelling, or retained literal text is not sufficient.

The exact C2 key is semantic authority even when its descriptor records
`:semantic-validation :deferred` solely because of
`:host-independent-decimal-range`; that is a host materialization boundary, not
a semantic-normalization gap. Conversely, a host decimal value without its
unique authenticated exact C2 key is rejected at the existing source-integrity
boundary. The public C6 decimal descriptor may carry provenance, but it cannot
override or synthesize the C2 key.

## Canonical decimal readable spelling

For one admitted semantic key, let `C` be its coefficient digit string, `n` its
digit count, and `P` its signed decimal power. Define the scientific exponent
by exact signed decimal arithmetic:

```text
E = P + (n - 1)
```

The canonical ASCII spelling is:

```text
zero:       0E+0
nonzero:    sign-prefix first-digit fractional-suffix E exponent-sign exponent-digits

sign-prefix       = "-" when sign is -1, otherwise ""
fractional-suffix = "" when n is 1, otherwise "." followed by C[1..n)
exponent-sign     = "+" when E is nonnegative, otherwise "-"
exponent-digits   = the absolute value of E in base ten with no leading zero
```

The letter is uppercase ASCII `E`. An exponent sign is always present. There is
no suffix. The examples are normative:

```text
C2 key {:sign 1  :coefficient "0"   :decimal-power 0}  -> 0E+0
C2 key {:sign 1  :coefficient "12"  :decimal-power -1} -> 1.2E+0
C2 key {:sign -1 :coefficient "12"  :decimal-power -1} -> -1.2E+0
C2 key {:sign 1  :coefficient "1"   :decimal-power 2}  -> 1E+2
C2 key {:sign 1  :coefficient "123" :decimal-power -5} -> 1.23E-3
```

Quoted coefficient strings above abbreviate the exact digit-codepoint vector;
they are not an alternate record representation.

This spelling is a valid L1/C2 decimal token because it always contains an
exponent. Rereading it must produce the exact same normalized C2 semantic key.
That round trip is a required proof obligation, not an appeal to a host parser.
An implementation may emit fixed notation internally only if it proves byte
equality with the scientific spelling above; because the specified output is
scientific, a different byte spelling is not conforming.

## Why this is not raw or host formatting

Raw spellings such as `1.20`, `1.2E+0`, `12E-1`, and `0.012E+2` may denote the
same C2 semantic key. They must all yield `1.2E+0`. Negative zero spellings must
yield `0E+0`. No raw decimal point position, exponent letter/case, explicit
source sign, trailing zero, source scale, or token length survives.

No implementation may call host `toString`, `toPlainString`, `toEngineeringString`,
`pr-str`, generic format, locale format, numeric compare, float conversion, or
decimal normalization as authority. It must perform bounded operations over the
authenticated sign, coefficient digits, and signed decimal power. The host may
execute those specified digit operations as substrate but cannot choose bytes.

## Exact bounded normalization

Canonical spelling is computed within both C2 and Attempt-15 bounds:

1. authenticate the closed exact C2 semantic key and reject a
   `:reader-semantic-work-boundary` normalization-deferred key;
2. scan coefficient digits once, proving the normalized coefficient invariant;
3. add nonnegative `n - 1` to signed decimal power using checked decimal-digit
   arithmetic with no conversion to a fixed-width host integer;
4. compute the exact prospective ASCII byte count before allocation; and
5. emit once only when the complete scalar fits 32768 UTF-8 bytes.

The input coefficient and power each inherit C2's exact bounded numeric
normalization evidence. The spelling operation adds at most one pass over each
input digit vector plus one checked signed addition and one output pass. It may
not reparse raw source, expand powers into zeroes, repeatedly divide a host
decimal, or allocate proportional to the numeric magnitude of the exponent.

Every output byte is ASCII and therefore one UTF-8 byte. Exact scalar-byte
accounting includes optional value sign, coefficient digits, optional decimal
point, `E`, exponent sign, and exponent digits. A 32768-byte result is admitted;
a prospective 32769-byte result fails direct contained
`:scalar-byte-limit`. The final collection output remains independently bound
at 262144 bytes. Checked counter overflow is a contained boundary, never
wraparound.

For a source decimal whose exact key was normalized under C2's current
256-scalar bound, the exact maximum canonical spelling is 261 ASCII bytes. For
fixed notation, the source spends one scalar on the decimal point, leaving at
most 255 coefficient digits; placing the point after all 255 nonzero digits
produces a three-digit exponent and exactly `255 + 1 + 1 + 1 + 3 = 261`
output bytes. For exponent notation, the source also spends one scalar on `e`
or `E` and at least one on exponent digits; even one carry in signed exponent
addition cannot exceed 260 output bytes. Optional source/value signs, a decimal
point, or an exponent sign consume a source scalar for every corresponding
output scalar and cannot raise either maximum. Zero emits four bytes.

Therefore an authentic current-C2 decimal cannot itself reach the 32768 scalar
ceiling. The printer must still perform prospective accounting rather than
special-case acceptance. Attempt-15's exact 32768/+1 scalar fixtures remain
required for admitted scalar kinds capable of reaching them; decimal evidence
instead proves the tighter exact 261-byte maximum and rejects unauthenticated
oversized keys.

Attempt 18 adds no sixth printer limit. C2's existing 256 numeric semantic
scalar and 65536 work-unit bounds remain reader bounds. Attempt-15's node,
depth, width, scalar-byte, and output-byte limits remain ordering-printer
bounds.

## Malformed, deferred, and nonfinite cases

Malformed decimal syntax remains `C2-NUMERIC` and produces no spelling.

A C2 key marked `:reader-semantic-work-boundary`, a deferred numeric record, a
missing or non-unique semantic-value join, a malformed signed-decimal-power
record, or a key that violates normalization fails contained exact
`:unsupported-decimal-readable-spelling`. A syntactically valid decimal whose
exact semantic key exists but exceeds a host materialization range remains
eligible; host range is not semantic range.

Nonfinite floating values are not decimals in this contract. NaN, positive or
negative infinity, host float payloads, and target floating spellings cannot be
reclassified as a C2 decimal key. They retain their governing numeric boundary
or fail contained `:unsupported-decimal-readable-spelling` if injected into the
decimal path.

The new contained reason is not a B51 pending reason, public tag, diagnostic
family, schema field, purpose, dependency edge, or controlled path. At the
producer/Root-1 boundary it maps to the existing exact
`:template-boundary-rejected` envelope with outer
`:source-integrity-mismatch`. At Root 6 it maps to the existing exact
`:independent-verifier-boundary-rejected` envelope with the same outer reason.
The independent source oracle records `C6-VERIFY`. Inside an unordered child
probe it is erased to Attempt-16's exact `:unordered-decoration-failure` before
caller mapping.

## Equality and collision law

Decimal semantic equality for this spelling is exact equality of authenticated
normalized C2 decimal semantic keys. It is not host decimal object equality,
host scale-sensitive equality, numeric coercion across integer/ratio/float
kinds, or raw-token equality.

For admitted decimal keys `A` and `B`:

```text
A = B  iff canonical-decimal-readable(A) = canonical-decimal-readable(B)
```

The implementation and independent oracle must prove both directions over the
bounded key grammar. Same-value source variants therefore produce one spelling
and one semantic collection member/key. If an authenticated set or map presents
two distinct semantic occurrences whose normalized decimal keys are equal,
ordinary semantic duplicate handling applies before ordering; host carrier
identity cannot keep both.

If unequal authenticated semantic values of any admitted kind produce equal
complete canonical readable text, the existing equal-text unequal-value source
integrity collision fires. A decimal spelling always contains `E` and an
explicit exponent sign, preventing equality with canonical integer spelling.
No digest, source ordinal, raw spelling, scale, object identity, or host hash
may break a collision.

## Independent producer, oracle, and Root-6 evidence

The B51 producer, independent source oracle, and Root 6 must separately:

- authenticate the exact C2 semantic key and occurrence joins;
- normalize signed decimal power and compute `E`;
- construct the exact scientific spelling;
- enforce work and byte bounds;
- prove reread semantic-key round trip;
- classify unsupported/deferred input; and
- apply their already distinct Attempt-17 caller mapping.

They may share only the scalar predicates, exact UTF-8 validity/count,
`sh07-canonical-text-compare`, and `sh07-declared-digest-hash` already admitted
by Attempt 17. They may not share a decimal normalizer, decimal printer,
semantic-key-to-text helper, evidence selector, collection probe, fold, sort,
or caller mapper.

Within-caller byte equality remains mandatory: for each caller independently,
equivalent decimal raw spellings and map/set carrier permutations must yield
byte-identical complete results. Cross-caller complete-result equality remains
forbidden because R1, the oracle, and R6 retain distinct envelopes.

## Exact evidence and mutations

Positive evidence must cover at least:

1. zero, negative zero source variants, positive, and negative values;
2. one-digit and multi-digit coefficients;
3. positive, zero, and negative final scientific exponents;
4. raw fixed/scientific, uppercase/lowercase exponent, explicit-plus,
   leading-zero, trailing-zero, and equivalent point-placement variants;
5. the same semantic value from at least four raw variants yielding one exact
   spelling and exact C2-key reread;
6. values whose decimal-power signed record cannot fit the host fixed-width
   exponent range but whose canonical spelling is within 32768 bytes;
7. decimal members, map keys, map values, nested maps/sets, and key-then-value
   ties ordered by strict UTF-8 text rather than numeric value;
8. the exact 261-byte authentic C2-bounded decimal maximum accepted, every
   exponent-notation competitor proven at or below 260, and a supplied key that
   would spell to 262 bytes rejected as unauthenticated rather than misreported
   as an authentic scalar-limit case; and
9. producer, oracle, and Root-6 independent outputs stable within each caller
   across at least three carrier permutations.

Boundary evidence must cover malformed syntax, deferred semantic work,
non-unique/missing/wrong occurrence evidence, malformed coefficient, leading or
trailing coefficient zero, wrong zero sign/power, malformed signed power,
nonfinite injection, host-only decimal without C2 evidence, and scalar/output
at-limit plus one.

Mutation evidence must reject raw-token reuse, source exponent preservation,
source scale preservation, trailing-zero preservation, negative-zero
preservation, lowercase `e`, omitted exponent sign, omitted exponent, fixed
notation, engineering notation, host `toString`, host scale equality, numeric
ordering, numeric cross-kind coercion, UTF-16 byte counting, fixed-width
exponent conversion, power expansion, digest ordering, collision tie-breaking,
shared decimal printer/normalizer closure, and any wrong caller boundary.

The independent source oracle must include a small executable digit-vector
model and a property/certificate over the admitted bounded key grammar proving
normal form uniqueness and C2 reread inversion. Differential evidence must run
on at least two host decimal implementations or one host implementation plus
the digit-vector oracle, while comparing only governed outputs. Host agreement
alone is not proof.

## Preserved topology, closure, and counts

Attempt 17 remains exact: Root 1 is the sole pending detector; Roots 2, 4, 5,
6, and 7 remain success-or-boundary; Root 4 lacks raw authority; Root 5 first
binds materialization to raw; Root 6 is independently authored and success-only;
unary Root 8 calls Root 1 exactly once and cannot call the comparator or decimal
printer; duplicate/recur/resource priority and coordinates remain unchanged.

Counts remain exactly 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
success purposes, 58 dependency edges, 94/174 controlled paths, 4 outcomes, 1
pending detector, 4 pending families, 8 reasons, 2 resource reasons, 2
unreachable mappings, and 1 failure-only purpose. Decimal spelling adds no
public root, ABI argument, tag, result field, purpose, edge, path, pending
reason, or diagnostic family.

The comparator remains the only new runtime builtin from the Attempt-14-to-17
lineage. Attempt 18 adds no decimal builtin and no public/general printer API.
The bounded digit-vector algorithms live independently in the three caller
closures and their independent evidence. Root 8, unrelated compiler modules,
application/runtime source, dynamic resolution, apply, reflection, callback,
FFI, generic compare, and generic sort remain outside the closure.

## Pins and implementation consequences

Attempt 18 authorizes no implementation or pin change. A later governed atomic
implementation may replace the current decimal-readable rejection only after
this exact candidate is frozen, independently accepted, made
integration-eligible, and reconciled to authoritative main.

That implementation must not change C2 source grammar, C2 semantic-key
identity, reader-canonical digesting, public roots, the comparator ABI, G13
counts, or frozen B47 sources/pins without separate governed evidence. No
producer-only, Root6-only, oracle-only, decimal-host-format, fixture-only, or
pin-only change may land.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-18`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its lifecycle
dependency is integrated Attempt 17. It starts from authoritative main
`57914082a4e6ef523778b4b24e4a2f93363b7fac`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration. It does not freeze, request review, accept, or
confer integration eligibility. The author does not self-review. The report
candidate owns only this file; draft registration owns only
`contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded
printer/comparator/probe substrate, and observer. It is not semantic authority.

This report does not define general numeric formatting, float spelling,
cross-kind numeric equality, a public decimal library, arbitrary-precision
decimal arithmetic, C2 collection order, or a general C11 serializer. It does
not claim readable-printer self-hosting, implementation, aggregate SH-07
completion, full language support, self-hosting, seed retirement, release,
performance, or pin acceptance. All Attempt-17 and G13 nonclaims remain exact.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 18 starts from integrated Attempt 17 and changes only its missing
   canonical decimal readable-spelling prerequisite.
2. The sole authority is the exact authenticated normalized C2 semantic key;
   raw spelling, scale, trailing zeroes, host decimal representation, locale,
   formatting, and numeric comparison are forbidden.
3. The exact spelling is uppercase scientific decimal with mandatory exponent
   sign, no suffix, canonical zero, and exact signed decimal exponent addition.
4. Every admitted spelling rereads to the byte-equal C2 semantic key, and equal
   C2 keys produce byte-equal spelling in both directions.
5. Normalization is bounded by C2 work evidence and Attempt-15 scalar/output
   byte limits without power expansion, fixed-width exponent conversion, host
   parser/formatter authority, or counter overflow.
6. Malformed, deferred, nonfinite, missing-evidence, and malformed-key cases
   fail exact contained `:unsupported-decimal-readable-spelling`, with generic
   unordered-child aggregation and existing caller-specific boundaries.
7. Producer, oracle, and Root 6 independently authenticate, normalize, render,
   bound, round-trip, and map failures; they share no decimal helper.
8. Positive and mutation evidence covers equivalent raw variants, signs,
   exponent ranges, nested map/set ordering, key/value ties, collisions, the
   decimal-specific 261-byte maximum, preserved general 32768/+1 scalar and
   output limits, host-range independence, and wrong algorithms.
9. Attempt-17 caller-scoped equality, comparator, bounded probes, commutative
   failure fold, cycles/shared DAG, caller distinction, G13 topology, digest,
   ABI, tags, counts, pins, and nonclaims remain exact.
10. No C2 grammar/key, public ABI, builtin, root, purpose, edge, path, pending
    reason, diagnostic family, implementation, test, fixture, pin, roadmap, or
    unrelated canonical document changes in the report candidate.
11. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, and exact range-diff checks pass.
12. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
