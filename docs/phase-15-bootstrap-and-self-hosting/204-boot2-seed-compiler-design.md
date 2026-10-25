# BOOT2 - Seed Compiler Design

Sequence: 204
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines the seed compiler. The seed compiler exists to implement a
minimal, auditable subset of Gravity well enough to compile early Gravity
compiler modules. It must be small, explicit about exclusions, deterministic,
and tested against the language subset it claims.

The seed compiler must not grow into an alternate language definition.

## Implemented Subset

The seed compiler implements:

- reader for the bootstrap subset;
- syntax objects with source spans;
- core special forms;
- minimal namespace resolution;
- minimal macro support required for compiler source;
- type and effect checking subset;
- MIR subset;
- diagnostics;
- one or more bootstrap backends;
- artifact manifest emission.

Exclusions are explicit, such as full AI runtime, GPU lowering, advanced
macro facilities, formal proofs, full package registry support, and optional
optimization passes.

## Native Symbol spelling-unit ABI

The temporary JVM seed exposes unary `bootstrap-symbol-utf16-unit-count`
only through compiler-artifact builtin augmentation. Runtime admission requires
the compiler-artifact plan kind and flag, with `:module` declaring profile
`:meta` and target `:jvm`. These fields declare this bootstrap ABI; they are
not source authentication, C5 binding evidence, or a public Core/library export.

The operand must have exactly native `clojure.lang.Symbol` class. The result
counts UTF16 spelling units from native `getName` and `getNamespace` String
lengths without rendering, interning, hashing, metadata inspection, UTF8
conversion, or user `Named` callbacks. A non-null namespace adds its length
plus one slash even when empty. Native null names contribute zero with a null
namespace, or four literal `null` units with a present namespace. Empty names,
supplementary pairs and unpaired surrogates retain their native unit counts.
The sum uses integer arithmetic wide enough for two maximal String lengths
and a separator. This is not Unicode validation or a scalar-count contract.

Ordinary L2 operand evaluation happens once before ABI checks. Wrong arity
reports `L2-BUILTIN-ARITY`; a wrong operand class or plan context reports
`L2-BUILTIN-ERROR` without rendering the operand. The ordinary builtin set,
Core and C5 catalogs, authenticated-source admissions and target backends do
not gain this operation. Its declaration and host implementation remain seed
TCB bytes: changes invalidate affected retained fingerprints and require fresh
evidence for any later migration claim. Existing pins are not renewed by this
development implementation or its conformance tests.

## Native carrier header ABI

Unary `bootstrap-native-carrier-header` uses the same compiler-artifact
`:meta`/`:jvm` plan restriction. It returns exactly
`[kind metadata-present? extent metric child0 child1]`. The default is
`[:unsupported nil nil nil nil nil]`, obtained without operand methods.
Unknown metrics are nil, never zero. This fixed observation is neither
validated DATA, language legality, an admission token, nor source provenance.
Wrong arity reports `L2-BUILTIN-ARITY`; wrong plan context reports
`L2-BUILTIN-ERROR` without rendering the operand.

Every operand and nested native method receiver must pass an exact class
check first. The current contour covers nil, Boolean, Character, Byte, Short,
Integer, Long, String, Symbol, Keyword, native persistent array/hash/tree maps, hash
sets, vectors, List nodes/empty Lists and Cons/SubVector observations. It also
describes native BigInt, BigInteger, Ratio, BigDecimal, Float and Double;
other numeric classes remain unsupported observations, not language exclusions.
Metadata is only a presence Boolean after a native class check. It is not
walked, returned, or implicitly rejected. String/Symbol/Keyword metrics count
native UTF16 spelling units without rendering; Keyword's native Symbol
receiver is checked separately. Symbol child0 is a fresh native name-present
Boolean: false for a null name, true for an empty or other non-null name.
Child1 remains nil. This fixed native getter and null test expose no name or
namespace String, reject no Symbol DATA, and grant no comparison legality;
the existing UTF16 metric and metadata observation are unchanged. Reserving
two observations for the name read and derived Boolean gives the Symbol
branch 75 logical cells: seven returned cells, 30 common plan-check cells,
and 38 branch cells (30 nested Symbol-observer plan-check cells, six numeric
observations and those two new observations). The unchanged Keyword branch
uses 37 branch cells. Callers must reserve the applicable bound before invoking
an unknown-kind observation; there is no constant-folding or cache discount.
Integer metrics are absolute-magnitude bits.
BigInt reads its native compact long or checks its exact native BigInteger
backing before observing magnitude; it does not coerce through Number methods.
Ratio supplies separate numerator/denominator bits, requiring exact native
BigInteger fields before numeric observation. No GCD, printing, precision,
normalization or numeric equality is performed. Positive-magnitude bitLength
avoids the negative bitLength scan; its bounded temporary native headers must
be included in the caller's explicit accounting.

Byte and Short use kinds `:integer8` and `:integer16` with the same magnitude-bit
metric as Integer/Long. Float and Double use `:float32` and `:float64`, with
fixed carrier-width metrics 32 and 64 and nil child slots. These require no
operand methods and describe finite values, signed zero, NaN and infinities
alike; they grant no exceptional-value, equality or conversion policy.
BigDecimal uses `[:big-decimal false nil unscaled-bits signed-scale nil]`.
Its exact native receiver's `unscaledValue` result must pass the existing exact
BigInteger guard before magnitude observation. Scale is a signed, widened
native field observation, including both int extrema, not precision or a
normalization request. Unknown nested backing yields unsupported.

On the audited OpenJDK 26 path, compact BigDecimal unscaled observation may
allocate one native BigInteger and an at-most-two-word magnitude array; negative
absolute magnitude may add one native BigInteger sharing the array. A
conservative logical inventory reserves two seven-cell BigInteger headers,
three array cells, one magnitude metric and two scale-read/widen cells: 20
branch temporaries. Byte/Short reserve at most five numeric observation cells,
and Float/Double one width cell. Together with seven returned cells and the
30 common plan-check path/cursor cells, 57 is a conservative bound for these
new numeric branches. This is not a heap-byte or interpreter-work estimate.
No cached-box or constant-folding discount applies; callers must separately
reserve the applicable fee before invocation and validate before comparison.

Container extents are native cached counts or checked physical lengths, not
content validation. For exactly native PersistentArrayMap, the seed resolves
only the fixed `clojure.lang.PersistentArrayMap.array` field through the same
ordinary, read-only, sticky-denial access boundary described below. A null
array, odd physical length or unavailable access yields the six-slot
unsupported header before metadata or count observation. A non-null even
array supplies length divided by two, without count, entry materialization,
element reads, hashing, equality or duplicate-key validation. This check also
precedes extent observation for an ArrayMap backing a native HashSet; a
dangling tail is never skipped. Empty and other even arrays remain supported.
The array reference is neither returned nor copied. TreeMap
reports only default-versus-foreign comparator identity in child0 and never
invokes it. List nodes return raw head/tail references, without forcing Cons
tails or counting their spines. SubVector returns its raw backing in child0,
start in child1 and the long end-minus-start extent; no backing method is
called. Consumers must close these references, bounds and comparator policy
before traversal, hashing or equality.

HashSet count delegates into a hidden map. The seed therefore resolves only
the fixed `clojure.lang.APersistentSet.impl` field, using ordinary JVM access
checks once. No caller-selected class/field, module opening, Unsafe operation,
access-denial retry or security-manager bypass is allowed. Unavailable access
or an unrecognized backing class yields unsupported, without delegated count.
An admitted native backing is returned in child0 after its extent guard.
Together with the fixed ArrayMap field, these are explicit bootstrap TCB
field-access boundaries, not general reflection authority or a proof that
stored members are safe. Their one-time Delay/Atom/Field access state caches
only fixed JVM infrastructure, including denial, never DATA or DATA validity.
Cold field resolution and first-access JVM accessor initialization are part of
that audited host mechanism, not a heap-byte or interpreter-work bound.

The ArrayMap path reserves 16 logical temporary cells: three access-state and
Field references; the array reference and null result; length, parity and
quotient observations with three numeric widening/result cells; the native
class, extent-presence and extent-forwarding observations; and metadata
reference and presence. A HashSet path adds eight cells for its access-state,
Field and backing references, backing class, extent guard and forwarding.
Thus these container paths fit seven returned cells plus 30 common plan-check
cells plus 24 branch cells, or 61 logical cells. This is below the existing
75-cell unknown-kind bound set by Symbol; 75 remains required before an
unknown-kind call. These observations are charged even when JVM boxes, fixed
access state or results are cached. Fixed host reflection infrastructure is
not a fresh DATA carrier; this logical inventory grants no bound on its JVM
heap allocation, initialization latency or runtime work.

Each selected result branch constructs only its returned header, with no
unconditional fallback tuple. One Vector and six slots cost seven logical
output cells regardless of any actual compiler constant folding. Callers
must reserve its construction and all bounded temporary observations before
invocation, separately from every subsequent DATA visit and native comparison.
Native metrics do not waive full child validation or existing schema/profile
checks. Conservative fee changes require an explicit owning accounting change.
The operation stays outside ordinary Core/C5 catalogs and authenticated-source
admission. Its helper and load-order entry invalidate affected seed fingerprints;
development tests do not renew migration, self-hosting or release evidence.

## Requirements

- The seed compiler MUST declare implemented and excluded documents.
- Seed diagnostics MUST be stable enough for conformance fixtures.
- Seed artifacts MUST include compiler identity and source hashes.
- Seed behavior MUST be tested by the bootstrap language subset suite.
- Unsupported profiles or forms MUST be rejected, not accepted silently.
- Host dependencies MUST be recorded.
- Seed runtime assumptions MUST be documented.
- The seed compiler MUST emit enough metadata for later stage comparison.

## Semantic Dependencies

- `L1` through `L6` define the minimum source and semantic subset.
- `C1`, `C2`, `C5`, `C6`, `C7`, `C8`, `C11`, and `C15` define seed compiler phases.
- `B2` or `B3` define likely initial backend support.
- `TEST1`, `TEST2`, and `TEST6` define seed conformance.
- `BOOT5` defines stage compatibility.

## Outputs and Artifacts

The seed compiler emits:

- seed compiler binary or executable artifact;
- seed compiler manifest;
- supported subset report;
- diagnostic report;
- bootstrap backend artifact;
- conformance report;
- provenance record.

## Example

```clojure
(seed-compiler gravityc-seed
  {:implements [:reader :core-forms :resolver :types :effects :mir :c-backend]
   :excludes [:full-macro-system :ai :gpu :distributed-runtime :formal-profile]
   :tests [:language-subset :compiler-subset :c-backend-subset]})
```

## Rejection Rules

- Reject seed features not listed in the supported subset.
- Reject unsupported profiles accepted as warnings.
- Reject seed artifacts without provenance.
- Reject seed diagnostics with unstable or missing codes.
- Reject host dependencies not recorded in the seed manifest.
- Reject seed behavior that contradicts upstream language specs.

## Diagnostics

- `BOOT2001` reports undeclared seed feature.
- `BOOT2002` reports unsupported profile accepted.
- `BOOT2003` reports missing seed provenance.
- `BOOT2004` reports unstable seed diagnostic.
- `BOOT2005` reports untracked host dependency.
- `BOOT2006` reports seed/spec semantic mismatch.

## Conformance Criteria

- Seed compiler passes the declared language subset suite.
- Unsupported forms and profiles are rejected with stable diagnostics.
- Seed artifacts include source, compiler, backend, environment, and dependency hashes.
- Host dependencies are listed and reproducible.
- Seed output can be compared with stage1 output.
- The seed manifest states exactly what remains outside Gravity ownership.
