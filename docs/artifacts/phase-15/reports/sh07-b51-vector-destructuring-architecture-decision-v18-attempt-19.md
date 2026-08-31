# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 19

Status: Draft bounded semantic-inverse correction for pre-freeze audit

Date: 2026-08-31

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 18. The
independent reviewer found its authenticated C2 semantic-key authority,
canonical scientific spelling algebra, exact 261-byte maximum, equality and
collision law, failure mapping, independent caller closure, Attempt-17
preservation, evidence structure, and nonclaims coherent. Attempt 18 was
rejected for one false universal operational claim: its 261-byte maximum
fixture cannot be normalized by the current C2 token path because C2
intentionally changes to normalization-deferred above 256 source scalars.

Attempt 19 preserves every coherent Attempt-18 rule. It replaces only the
universal current-C2 reread requirement with a separately authored bounded
semantic inverse over the canonical scientific spelling. Ordinary current-C2
reread remains mandatory through 256 bytes. Canonical spellings from 257
through 261 bytes must take C2's existing normalization-deferred branch while
the separate inverse reconstructs the exact normalized four-key C2 semantic
key. Neither branch may change the canonical bytes.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and terminal history

```text
authoritative main commit / terminal Attempt-18 commit
2777086d92aa87e297ce77e19be7ebdb834b4260

authoritative main tree
a9a33e2cd2ee77c7c008eac8ff78011a9c0cce58

integrated Attempt-17 main
57914082a4e6ef523778b4b24e4a2f93363b7fac

terminal Attempt-18 candidate
6d179d440386b9b4f6bd7078e167df2ddada2e94

terminal Attempt-18 candidate tree
016da13629588fff46692108c6d7461bf84bc5b4

terminal Attempt-18 report SHA-256
0b85308ea51b7cbdd75f03a039d9e7e57f7f75b2ee2213b1ff95caea7934c386
```

Attempts 14, 15, 16, and 18 remain terminal rejection history. Attempt 19 does
not reopen the resolved comparator/order, bound/cycle, unordered-failure, or
caller-equality blockers. It addresses only Attempt 18's exact reread blocker.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`,
`D9`, `L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`,
`bootstrap/clojure/src/gravity/bootstrap.clj`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Incorporated Attempt-18 authority

Attempt 19 incorporates Attempt 18 in full except every claim that current C2
must operationally normalize every canonical spelling through 261 bytes. The
sole value authority remains an authenticated exact normalized C2 semantic key:

```clojure
{:kind :decimal
 :sign sign
 :coefficient coefficient-codepoints
 :decimal-power decimal-power}
```

`sign` is exactly `-1` or `1`. Coefficient is a nonempty normalized ASCII digit
vector with neither a leading zero nor a trailing zero for nonzero values. Zero
is exactly sign `1`, coefficient `[48]`, and decimal power `0`. Decimal power is
an exact integer or this closed canonical record:

```clojure
{:artifact :gravity/signed-decimal-power
 :sign one-of-[:negative :positive]
 :digit-codepoints nonempty-normalized-nonzero-ASCII-decimal-digits}
```

Raw spelling, source exponent, scale, trailing zeroes, host decimal objects,
host formatting, locale, floating conversion, numeric comparison, digest,
source ordinal, object identity, and host hash remain non-authoritative.

The canonical spelling remains exactly:

```text
zero:       0E+0
nonzero:    sign-prefix first-digit fractional-suffix E exponent-sign exponent-digits

sign-prefix       = "-" when sign is -1, otherwise ""
fractional-suffix = "" when coefficient has one digit,
                    otherwise "." followed by the remaining digits
exponent-sign     = "+" when E is nonnegative, otherwise "-"
exponent-digits   = abs(E) in base ten with no leading zero
E                 = decimal-power + coefficient-count - 1
```

Uppercase `E`, an explicit exponent sign, and no suffix remain mandatory.
Canonical zero remains `0E+0`. The same exact key always produces the same
bytes, and the same canonical bytes always denote the same exact key.

Attempt-18 bounds remain exact. Under current C2's 256-scalar normalization
bound, fixed notation with 255 nonzero coefficient digits plus one decimal
point reaches the exact 261-byte canonical maximum. Exponent notation reaches
at most 260 bytes. Optional signs and punctuation consume source scalars and do
not increase those maxima. General Attempt-15 limits remain 4096 nodes, depth
96, width 512, scalar spelling 32768 UTF-8 bytes, and final output 262144 UTF-8
bytes.

## Corrected two-part round-trip law

Attempt 19 distinguishes semantic inversion from current-reader execution.

For every authenticated exact normalized key `K`, let `spell(K)` be the
Attempt-18 canonical scientific spelling and let `inverse(S)` be the bounded
semantic inverse defined below. The universal law is:

```text
inverse(spell(K)) = K
spell(inverse(S)) = S for every admitted canonical spelling S
```

This is an independently specified semantic law. It does not invoke C2 token
classification, current C2 numeric normalization, a host parser, or a host
decimal object.

Current C2 supplies a separate operational check:

```text
canonical spelling length <= 256
  current C2 returns an exact normalized semantic key equal to inverse(S)

canonical spelling length in [257, 261]
  current C2 accepts the decimal lexical shape and returns its exact existing
  normalization-deferred evidence under :reader-semantic-work-boundary
  inverse(S) independently reconstructs the exact normalized key
```

The deferred branch is not an equality failure and does not authorize a second
spelling. It proves only that current C2 deliberately declines that
normalization work. Neither the C2 branch, its raw/deferred descriptor, nor its
256-scalar threshold is an input to `spell(K)`.

No claim is made that current C2 operationally returns the exact normalized key
for a 257-to-261-byte token. Tests and reviews must reject that claim rather
than widening C2, shrinking the canonical domain, or weakening the 261-byte
fixture.

## Exact bounded semantic inverse

Each caller authors its own inverse over a vector of strict ASCII bytes. The
inverse accepts no more than 261 bytes and performs these exact steps:

1. reject empty input, non-ASCII bytes, or length above 261;
2. accept zero only as the complete four-byte sequence `0E+0` and return the
   exact canonical zero key;
3. otherwise consume optional `-`, then one coefficient digit from `1` through
   `9`;
4. consume either no fractional suffix or one `.` followed by one or more ASCII
   digits, requiring the final coefficient digit to be nonzero;
5. require exactly uppercase `E`, exactly one exponent sign, and one or more
   exponent digits with no leading zero unless the exponent is exactly zero;
6. concatenate the coefficient digits and let their count be `n`;
7. parse the signed scientific exponent as a sign plus normalized decimal digit
   vector, then compute `P = E - (n - 1)` by exact signed decimal-digit
   subtraction without fixed-width conversion;
8. encode `P` as integer `0` when zero, as the exact signed integer when its
   magnitude has at most 18 digits, or as the exact closed
   `:gravity/signed-decimal-power` record otherwise; and
9. return exactly the four-key normalized semantic key, then independently
   respell it and require byte equality with the input.

A leading `+` on the value is forbidden. A decimal point with zero fraction
digits is forbidden in canonical text. A multi-digit coefficient ending in
zero is forbidden. Lowercase `e`, absent exponent sign, exponent leading zero,
extra suffix, whitespace, Unicode digit, or trailing byte is forbidden.

The inverse uses at most four linear passes over at most 261 bytes: grammar
scan, coefficient construction, signed exponent subtraction/normalization, and
respelling comparison. Each pass has an explicit remaining-vector counter and
terminates after at most 261 observations. It never expands powers into zeroes,
converts an arbitrary exponent to a fixed-width host integer, recursively calls
the reader, allocates by exponent magnitude, or calls host decimal parsing or
formatting. The existing 261-byte domain is therefore a complete work bound;
no new public limit or C2 work allowance is added.

The 18-digit representation threshold is not host range authority. It repeats
C2's exact semantic-key representation rule so the reconstructed key is byte
equal, while Gravity exact integers remain semantic values. Nineteen or more
magnitude digits use the canonical signed-power record even if a particular
host could represent them.

## Independent caller algorithms

The B51 producer, independent source oracle, and Root 6 must each separately:

- authenticate the original exact C2 semantic key and its occurrence joins;
- construct canonical bytes from that key;
- run its own bounded inverse over those bytes;
- require inverse result byte-equal to the original four-key semantic key;
- apply the current-C2 operational check only in the correct length branch;
- enforce Attempt-15 scalar and output accounting; and
- apply its distinct Attempt-17 caller mapping.

For lengths through 256, each caller independently compares the current-C2
normalized result with both the original key and inverse result. For lengths
257 through 261, each caller independently requires exact lexical acceptance,
exact normalization-deferred evidence and reason, and exact inverse equality;
it must not compare the bounded/deferred C2 spelling key as though it were the
normalized four-key semantic value.

The callers may share only Attempt-17's admitted scalar predicates, UTF-8
validity/count, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash`. They may not share the inverse parser, signed-digit
subtraction, key constructor, respeller, C2-branch checker, decimal evidence
selector, printer, collection probe/fold/sort, or caller mapper.

The inverse is not a new runtime builtin, public function, C2 reader helper, or
general parser. Static transitive closure evidence must keep it confined to the
separately authored producer, oracle, and Root-6 evidence closures. Root 8 and
unrelated compiler/application/runtime code remain forbidden callers.

## Equality, collision, and failure behavior

Attempt-18 equality remains:

```text
A = B iff spell(A) = spell(B)
```

for admitted exact normalized decimal keys. The inverse proves the reverse
direction without current-reader authority. Equivalent raw fixed/scientific,
case, sign, point-placement, leading-zero, trailing-zero, and negative-zero
variants still normalize to one key and one spelling where current C2 produces
their authentic source key. Cross-kind numeric coercion remains forbidden.

Malformed source remains `C2-NUMERIC`. A malformed or noncanonical inverse
input, inverse/key mismatch, respelling mismatch, wrong current-C2 branch,
missing/ambiguous occurrence evidence, normalization-deferred source key,
host-only decimal, malformed signed power, or nonfinite injection fails exact
contained `:unsupported-decimal-readable-spelling`.

The contained reason adds no public tag, pending reason, diagnostic family,
schema, purpose, edge, or path. Root 1 maps it to the exact
`:template-boundary-rejected`/`:source-integrity-mismatch` envelope. Root 6 maps
it to the exact
`:independent-verifier-boundary-rejected`/`:source-integrity-mismatch`
envelope. The source oracle records `C6-VERIFY`. Within an unordered child
probe it becomes exact Attempt-16 `:unordered-decoration-failure` before caller
mapping. Host exceptions never escape.

Equal-text unequal-value collision handling, semantic duplicate handling, and
within-caller byte equality remain exact. R1, oracle, and R6 complete results
remain deliberately distinct across callers.

## Exact evidence and mutations

All coherent Attempt-18 evidence remains required. Corrected round-trip
evidence must additionally prove:

1. every canonical length from the shortest form through 256 uses current-C2
   exact normalization and equals the independent inverse;
2. representative lengths 257, 258, 259, 260, and the exact 261-byte maximum
   produce current-C2 lexical acceptance plus exact normalization-deferred
   evidence, while the independent inverse returns the original exact key;
3. the 255-digit fixed source maximum spells to 261 bytes, inverses exactly,
   respells identically, and is not falsely required to operationally normalize
   through current C2;
4. the maximum exponent-notation competitor is at most 260 bytes and follows
   the same length-branch law;
5. equivalent raw variants produce one original key and one canonical spelling
   without consulting raw bytes during spelling or inversion;
6. exponent addition in spelling and subtraction in inversion are mutual over
   positive, zero, negative, 18-digit, and 19-plus-digit powers;
7. producer, oracle, and Root 6 independently produce byte-stable results over
   at least three unordered carrier permutations; and
8. the 256/257 threshold changes only C2 evidence status, never canonical bytes,
   inverse result, order, equality, collision, digest, or caller envelope.

Mutation evidence must reject:

- universal current-C2 exact reread at length 257 through 261;
- widening C2's 256-scalar or 65536-work-unit bound;
- shrinking canonical decimal maximum to 256 or rejecting the authentic
  257-to-261-byte keys;
- treating the C2 deferred spelling key as the exact normalized four-key key;
- choosing canonical bytes from exact-versus-deferred C2 branch status;
- using current C2, host parsing, host formatting, raw spelling, scale, or
  exponent spelling inside the semantic inverse;
- lowercase `e`, missing/extra signs, leading exponent zero, coefficient
  leading/trailing zero, fixed/engineering output, suffixes, whitespace,
  Unicode digits, partial parse, or unconsumed bytes;
- wrong `P = E - (n - 1)` arithmetic, fixed-width overflow, wrong 18/19-digit
  representation branch, negative zero power, or noncanonical signed record;
- inverse without byte-identical respelling, respelling without original-key
  equality, or collision tie-breaking;
- shared inverse/normalizer/printer helpers across producer, oracle, and Root 6;
  and
- any drift in Attempt-17 caller mapping, probes/fold, bounds/cycles, G13
  topology, digest, ABI, tags, counts, pins, or nonclaims.

Independent evidence must include a digit-vector algebra proof or exhaustive
bounded property decomposition for both inverse laws. Differential checks may
compare current C2 only within its operational branch; above 256 they compare
exact deferment status plus the separately authored inverse. Host agreement is
not semantic proof.

## Preserved topology, bounds, and counts

All Attempt-17 and coherent Attempt-18 rules remain exact. Root 1 is the sole
pending detector. Roots 2, 4, 5, 6, and 7 remain success-or-boundary. Root 4
lacks raw authority; Root 5 first binds materialization to raw; Root 6 is
separately authored and success-only; unary Root 8 calls Root 1 exactly once and
cannot call the comparator, decimal printer, or semantic inverse.

Counts remain exactly 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
success purposes, 58 dependency edges, 94/174 controlled paths, 4 outcomes, 1
pending detector, 4 pending families, 8 reasons, 2 resource reasons, 2
unreachable mappings, and 1 failure-only purpose. Attempt 19 adds no root,
argument, builtin, tag, field, purpose, edge, path, pending reason, diagnostic
family, reader bound, or printer bound.

Reader-canonical digest identity and all hash inputs remain unchanged. Current
C2's maximum numeric semantic scalars remains 256 and maximum numeric semantic
work units remains 65536. Attempt-15's limits, Attempt-16's commutative opaque
unordered failure, Attempt-17's within-caller byte equality and cross-caller
distinction, and Attempt-18's exact 261-byte decimal maximum remain unchanged.

## Pins and implementation consequences

Attempt 19 authorizes no implementation or pin change. A later governed atomic
implementation may add the three independent semantic inverses only after this
exact candidate is frozen, independently accepted, made integration-eligible,
and reconciled to authoritative main.

No C2 grammar, semantic key, token path, 256-scalar bound, 65536-work bound,
public root, comparator ABI, G13 count, B47 source/pin, or whole-file pin may
change without separate governed authority. No producer-only, Root6-only,
oracle-only, inverse-only, fixture-only, or pin-only change may land.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-19`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its lifecycle
dependency is integrated Attempt 17. It preserves terminal Attempt 18 as exact
rejection history and directly addresses its sole blocker. It starts from
authoritative main `2777086d92aa87e297ce77e19be7ebdb834b4260`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration. It does not freeze, request review, accept, or
confer integration eligibility. The author does not self-review. The report
candidate owns only this file; draft registration owns only
`contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded
printer/comparator/probe substrate, and observer. It is not semantic authority.

The semantic inverse does not amend C2, accept normalization-deferred source
keys as exact keys, or become a general reader/parser. It does not define
general numeric formatting, float spelling, cross-kind numeric equality,
arbitrary-precision decimal arithmetic, or a public decimal library. This
report does not claim implementation, readable-printer self-hosting, aggregate
SH-07 completion, full language support, self-hosting, seed retirement,
release, performance, or pin acceptance. All Attempt-17/G13 and coherent
Attempt-18 nonclaims remain exact.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 19 starts from terminal Attempt-18 main and changes only the false
   universal operational C2 reread claim.
2. Every coherent Attempt-18 authority, spelling, 261-byte bound, equality,
   collision, failure, closure, evidence, Attempt-17, G13, and nonclaim rule is
   preserved.
3. The universal proof uses a separately specified bounded semantic inverse,
   not current C2, a host parser/formatter, raw spelling, scale, or exponent
   representation.
4. Current C2 exact normalization is required only through 256 bytes; 257
   through 261 require exact existing normalization-deferred evidence plus
   independent inverse equality, with no byte/order/equality change.
5. The inverse grammar, signed subtraction, 18/19-digit power representation,
   four-key construction, respelling equality, and four-pass 261-byte work
   bound are exact and fail closed.
6. Producer, source oracle, and Root 6 independently implement spelling,
   inverse, C2 branch checking, evidence joins, bounds, and caller mapping with
   no shared decimal helper or new builtin.
7. Evidence covers every threshold length, the 261 fixed maximum, exponent
   competitors, raw variants, power signs/representations, unordered carrier
   permutations, malformed inverse inputs, and wrong algorithms.
8. No C2 grammar, key, bound, work allowance, public ABI, builtin, root,
   purpose, edge, path, reason, diagnostic, digest, pin, implementation, test,
   fixture, roadmap, or unrelated canonical document changes in the report
   candidate.
9. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
   ownership, and exact range-diff checks pass.
10. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
