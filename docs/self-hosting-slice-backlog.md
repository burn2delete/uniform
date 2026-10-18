# Self-Hosting Slice Backlog

Status: active coordination plan; no completion status in this file overrides
the governing phase roadmaps or evidence artifacts.

## Purpose

This backlog turns Gravity's self-hosting critical path into a finite,
dependency-ordered set of implementation slices. It is a coordination
projection of `D2`, `BOOT1`, `BOOT3`, `BOOT5`, `BOOT7`, `BOOT8`, `TEST13`, and
the Phase 15 and Phase 18 implementation roadmaps. It is not an additional
normative source document in the 240-document inventory.

## Master Coordinator Goal

Iterate on Gravity through the 30 smallest independently provable vertical
slices in this plan until the language is fully self-hosted, feature-complete
against its documented contracts, and able to compile to every documented
target with accepted and rejected behavior, stable diagnostics, reproducible
artifacts, and seedless public proof.

The first bounded goal is genuine seed retirement at `SH-29`: a public Gravity
compiler must compile its own compiler, runtime subset, standard-library
subset, and package/build path without the Clojure seed in the release
boundary. After `SH-29`, repeat the applicable target gates as a countable
target-expansion matrix until every documented target passes. The first honest
self-hosting claim therefore does not overclaim that every target is already
implemented.

Use one master coordinator with a fixed, reusable pipeline of writer, auditor,
and next-slice planner agents. Maintain a single writer for shared or
monolithic files, prepare the next eligible slice while the current slice is
being validated, and run each expensive native or public proof only once per
stable candidate.

Gradually replace the monolithic bootstrap implementation with contract-aligned,
stage-owned namespaces:

- `gravity.reader`
- `gravity.syntax`
- `gravity.macro`
- `gravity.resolution`
- `gravity.checked-core`
- `gravity.types`
- `gravity.effects`
- `gravity.ownership`
- `gravity.safety`
- `gravity.mir`
- `gravity.optimization`
- `gravity.lowering`
- `gravity.diagnostics`
- `gravity.backend.c`
- `gravity.backend.llvm`
- `gravity.backend.wasm`
- `gravity.backend.jvm`
- `gravity.backend.js`
- `gravity.backend.mlir`
- `gravity.backend.gpu`
- `gravity.backend.hdl`
- `gravity.backend.workflow`
- `gravity.backend.query`
- `gravity.backend.mobile`
- `gravity.runtime`
- `gravity.standard-library`
- `gravity.package`
- `gravity.build`
- `gravity.tooling`
- `gravity.cli`
- `gravity.bootstrap`
- `gravity.release`

Assign each namespace an explicit contract boundary, public API, artifact
inputs and outputs, diagnostic ownership, tests, and dependency direction.
Keep compatibility entrypoints thin while implementation authority moves into
these namespaces. Permit parallel writers only when namespace ownership,
files, tests, and worktrees are genuinely disjoint. Serialize changes to the
shared CLI, diagnostic catalogs, integration manifests, coverage accounting,
completion attestations, and release boundaries.

Every integrated slice must identify its governing contracts, compile or emit
something real, reject something real, preserve provenance and deterministic
identity, pass proportionate regression and adversarial review, state its
unsupported boundaries honestly, and finish as a clean verified commit.

Do not claim target support, feature completeness, self-hosting, seed
retirement, or release readiness before the corresponding documented evidence
gates pass.

## Implementation progress is separate from migration

Use the three-boundary workflow in `docs/development-verification.md` for
ordinary SH-07 work: edit the Gravity owning pass, add accepted/rejected
fixtures, run focused semantic tests, and validate affected composition.
Report the behavior and checks as implementation progress even while the
slice remains partial. The uniform completion gate below is an admission and
completion gate, not an obligation to run every authority proof before each
local improvement. Stale authoritative evidence stays stale until an explicit
stable-module migration decision re-establishes it. Preparation documents,
review counts and scheduler changes are not executable progress.

### Qualified current-namespace var objects: development increment

The owning `gravity.checked-core` pass now lowers `(var demo.value/answer)`
and fully qualified same-namespace function targets to var-object Core nodes.
L2's reference-without-dereference/execution behavior and L3's qualified
namespace resolution are preserved, including lexical shadowing, upstream
binding identities, actual resolution order and `:var-target` source mapping.
Alias-qualified, foreign-namespace and lexical var targets remain rejected;
profile legality remains `:pending-sh09`. This does not extend runtime var
semantics, cross-module initialization, type/profile legality or target support.

Paired accepted/rejected fixtures and tests live in the existing SH-07-B5
fixture directory and `sh07_var_reference_test.clj`. The direct
`sh07-b5-qualified-var-development-kernel` executes the Gravity lowerer and
validators on explicitly non-authoritative normalized inputs. Separate tests
exercise real SH-06-to-SH-07 construction, source mapping, request membership
and tampered-product replay. The default commands are in
`docs/development-verification.md`.

Validation: seven focused kernel/cache/revision-guard tests passed 72
assertions; real qualified-var composition passed 26 assertions; authenticated
order/spelling/binding-id and replay mutations passed 18 assertions. The kernel
executed in 1.230 seconds after 6.970 seconds of namespace loading; the positive
composition executed in 127.284 seconds. These are development observations,
not benchmark or proof claims. The initial whole-B5 run was interrupted at ten
minutes without a test report and remains unverified. The full suite and full
fixture matrix were not rerun.

The ordinary adapter executes current source with actual byte/plan identities,
labels it `:development-unadmitted`, and rejects source/toolchain drift rather
than mislabeling an artifact. Historical nine-field migration pins are unchanged
and are enforced at explicit proof entrypoints and on the exact transaction
snapshot. Fresh stable-revision provenance/equivalence/bootstrap verification
and authoritative admission remain outstanding. SH-07 remains partial; no
completion, self-hosting, seed-retirement or release count changes follow.

### Qualified current-namespace mutation targets: development increment

`gravity.checked-core` now lowers `(set! demo.value/target value)` for
authenticated top-level vars/functions in the current namespace. It preserves
the actual SH-06 resolution order and binding identity, does not evaluate the
target, evaluates the RHS once before the write, and records the unit result.
The mutation target is consumed without emitting a target-expression node;
the RHS retains its own resolution and source-map emission, including when a
lexical binding shadows the qualified target's unqualified name.

The existing B6 fixture `qualified-set-target` is promoted to accepted behavior.
New foreign and alias-qualified fixtures retain `C6-LOWERING-GAP`; lexical,
unsupported-location and forged binding joins remain rejected. No public API,
schema, effect registry or migration fingerprint changes are introduced.
SH-06 resolution feeds the existing C6 Core/mutation/mapping products; later
type, effect/profile/capability, ownership and safety legality facts remain
pending. This is Core lowering support, not runtime mutation permission.

Five selected B6 tests passed 231 assertions: the direct Gravity kernel,
authentic qualified products/mapping, request and product tampering,
foreign/alias diagnostics, and the existing unqualified substitution regression.
The direct kernel executed in 1.199 seconds after 6.961 seconds of namespace
loading. Changed test-only mapping/diagnostic oracles were rechecked; passed
unchanged negative and forgery tests were not repeated. These are development
checks, not a full B6 namespace pass or authoritative migration evidence.
SH-07 remains partial and development-unadmitted. The owning tests are in
`sh07_mutation_test.clj`; use its exact
`sh07-b6-qualified-set-development-kernel` through the existing development
runner for the inner loop.

### Quoted-symbol match literals: development increment

`gravity.checked-core` now lowers exact quoted-symbol patterns such as
`'ready`, `'event/ready` and `'_`, both at the root and inside supported fixed
vectors. Quoted underscore is a literal, not a wildcard. The normalized symbol
is preserved in literal pattern records and root branch/node records; existing
vector values and source-child paths remain unchanged. Quote operators and
payloads retain consumed `:match-pattern` mapping rows anchored to the actual
match node, without becoming executable references, calls or bindings.

The existing SH-06 traversal and SH-07 projection walk valid quoted-pattern
branches in their enclosing scope. Actual vector binding symbols still create
branch-local bindings. Malformed quotes and quoted composites remain rejected
by C6; an unresolved executable branch is rejected by C5. Forged payloads,
substituted pattern products and removed/misclassified quote mapping rows are
covered by negative checks. No public API, product schema or migration pin
changes are introduced.

Use `sh07_match_lowering_test.clj` with the existing development runner:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-quoted-symbol-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-quoted-symbol-authentic-composition --exact sh07-b10-quoted-symbol-forgeries-fail-authentication-and-mapping --exact sh07-b10-quoted-symbol-rejections
```

The four selected kernel/composition/negative tests passed 308 assertions. The
cold kernel executed in 1.337 seconds after 7.487 seconds of namespace loading;
the grouped checks executed in 314.985 seconds after 6.990 seconds of loading.
These are development observations, not performance or proof claims. Earlier
test-harness failures and an interrupted group do not count as passing checks.
The full B10 namespace and authoritative migration were not run. Exhaustiveness,
type narrowing, later legality, runtime matching and SH-07 completion remain
outstanding; this revision is development-unadmitted.

### Recursive keyword-keyed map patterns: development increment

`gravity.checked-core` now lowers keyword-keyed map-shape patterns recursively
with existing fixed vectors, scalar/quoted-symbol literals, wildcards and unique
branch bindings. This includes empty maps, maps in vectors, vectors in maps,
multiple bindings and branch-tail `recur`. The two formerly rejected map cases
are promoted to accepted fixtures in the existing SH-07-B11 family.

Map records retain the actual map value and nil `:vector-width`; only value
patterns create child records. Paths use odd flattened source-child coordinates
(`2i+1`), while key data at `2i` has consumed mapping rows joined to its exact
owning map. These coordinates follow authenticated semantic map traversal, not
a textual insertion-order promise. Map keys do not create references, calls or
bindings. Existing limits are unchanged: 256 entries may have 512 source
children; 257 entries reject. Closed producer, resolver, parent/completeness,
source-map and host-coherence checks include the new `:map` variant without
changing schema 16's record keys or migration pins.

Six focused tests passed 487 assertions: the compiled-Gravity kernel, fixture
pair census, authentic recursive products/mapping, coherent and malformed
forgeries, promoted cases/tail-recur/vector regression, and six source
rejections. Unsupported keys, guards, other composites and duplicate bindings remain
rejected; escaped bindings fail in C5. Malformed map values fail verification
without a host exception. The source-composition group executed in 704.105
seconds after 7.300 seconds of namespace loading; these observations are not
benchmark or proof claims. An interrupted pre-fix group is not passing evidence.

For the inner loop, select `sh07-b11-keyword-map-development-kernel` in
`gravity.self-hosting.sh07-vector-pattern-test` through `-M:dev-test`. Separate
exact source tests are named `sh07-b11-keyword-map-authentic-composition`,
`sh07-b11-keyword-map-forgeries-fail-closed`,
`sh07-b11-keyword-map-promotions-and-vector-regression` and
`sh07-b11-keyword-map-source-rejections`.

This is lowering support, not a runtime extra-key policy, exhaustiveness or
type-narrowing proof. Later legality, runtime matching, authoritative migration
and SH-07 completion remain outstanding. The revision is development-unadmitted;
no target, self-hosting, seed-retirement or release claims follow.

### Irrefutable root match clauses: development increment

`gravity.checked-core` rejects a clause after a successfully lowered unguarded
root wildcard or binding pattern. The structured diagnostic is `C6-VERIFY`
with semantic rule `L7-UNREACHABLE` and reason
`:match-clause-after-irrefutable-pattern`. It identifies the offending pattern
and earlier blocking clause by their actual form/syntax IDs, ordinal, kind and
available semantic source span. Existing projected descendants share their
enclosing root span; this increment does not invent more precise leaf spans.

A terminal root catchall remains supported. Nested map/vector bindings are
conditional patterns, and quoted underscore is a literal, not a wildcard.
The sibling-binding fixture now uses conditional vector/map patterns and
checks exactly two distinct branch bindings, scopes and reference joins.
Clause, node, branch-record and hosted coherence checks reject forged
nonterminal catchalls without changing product keys, schema 16 or migration pins.

Reader/form-shape and C5 authentication/resolution still precede C6. Within C6,
outer shape and scrutinee errors, then errors in the earlier clause's pattern
or body, retain priority. Only after that clause succeeds does its catchall
block the later clause before later C6 pattern/body analysis. An unresolved
name in a dead-looking branch still fails C5 rather than being silently dropped.

Use the exact `sh07-b10-irrefutable-clause-development-kernel` selector in
`gravity.self-hosting.sh07-match-lowering-test` for the inner loop. The separate
`sh07-b10-irrefutable-authentic-composition-and-regression`,
`sh07-b10-irrefutable-forgeries-fail-closed` and
`sh07-b10-irrefutable-source-diagnostics-and-priority` selectors exercise the
affected authenticated composition; the existing sibling-scope test covers the
changed fixture. Full literal/structural subsumption, guards, exhaustiveness,
type narrowing, runtime matching and authoritative migration remain outstanding.
This revision is development-unadmitted, not SH-07 or self-hosting completion.

Six selected tests passed 288 assertions: kernel and paired-fixture census
(145), authentic terminal/nested-pattern composition (30), coherent forgeries
(6), six source diagnostic/priority cases (77), and exact sibling binding/scope
joins (30). The kernel executed in 1.195 seconds after 6.992 seconds of namespace
loading; the composition group executed in 775.704 seconds after 7.231 seconds
of loading. These are development observations, not benchmark or proof claims.
Earlier test syntax errors and the unsupported-operation kernel failure are
not passing evidence. The full B10 namespace and migration proofs were not run.

### Root keyword-map guards: development increment

`gravity.checked-core` lowers a root keyword-map pattern's `:when` value as an
executable expression. Structural pattern bindings are visible in both guard
and body. C5 resolves guard references before body references in that same
branch scope, without changing the original flattened map child coordinates.
The `:when` key remains consumed pattern structure; its value and executable
descendants receive normal emitted source mappings.

The Core records describe scrutinee-once evaluation, structural failure skipping
guard/body, guard-once evaluation after structural success, Boolean true selecting
the body, and Boolean false continuing to the next candidate. Guard children are
explicitly indexed after body children, not added to unconditional evaluation.
Guard expressions are not tail positions; branch bodies retain their enclosing
tail target. Calls, mutation, throws, nested conditionals and typed handlers keep
their operation and conditional-region facts. This preserves facts for later
checking; it does not grant effect, profile, capability or safety legality.

Guarded products use the closed nested `:sh07-root-map-guard-v1` rule. Node
attributes contain source IDs/indexes, while actual guard node references are
explicitly resolved in branch records. Unguarded shapes, top-level schema 16,
public APIs and historical migration pins are unchanged. Source joins use the
actual authenticated child-form vector, not an assumed map iteration order.
The existing 256-entry map limit includes `:when`: 255 structural entries plus
the directive fit; 256 plus the directive do not.

Nested structural-map guards remain unsupported; ordinary nested match
expressions in a guard or body remain supported. The existing deferred-guard
fixtures now exercise that nested structural boundary. False, nil and other
guard expressions are retained at this lowering boundary; retention does not
establish typing legality. L7 now requires normally returning guards to produce
Bool: true selects the body, false tries the next clause, and no implicit
truthiness conversion is allowed. Non-returning guards preserve their control
transfer. The `L7-GUARD-TYPE` check and later type/effect legality remain
implementation work; the existing C6 fixture retaining nil is not evidence
that nil is a legal typed guard.
Current C7 match typing, runtime matching, exhaustiveness and authoritative
migration remain incomplete. No SH-07, target or self-hosting completion follows.

Use `sh07-b10-root-map-guard-development-kernel` in the existing
`gravity.self-hosting.sh07-match-lowering-test` namespace for the inner loop.
The separate `sh07-b10-root-map-guard-authentic-composition` and
`sh07-b10-root-map-guard-source-rejections` selectors cover affected source
composition and real rejected programs. They use the existing development runner;
no new lane, wrapper, authority receipt or admission prerequisite is introduced.

Three guard-specific tests passed 453 assertions: the compiled-Gravity kernel
(337), authentic composition (84), and four rejected source programs (32).
The source cases reject unresolved guard references and escaped bindings in C5,
and non-tail guard `recur` and nested structural guards in C6, with structured
diagnostics rather than raw host exceptions. Two legacy map/catchall kernels
also passed 310 assertions. Authentic composition executed in 139.673 seconds;
this is a development observation, not a benchmark or proof claim. The earlier
75-assertion composition result predates the conditional/handler guard-parent
correction; the final 84-assertion run covers that correction and new source
cases. An unsupported-operation kernel failure was corrected before these
passing checks. The selected source runs used `.gravity`; paired `.qst` text
equality was checked without claiming a second full composition run. Full
namespaces, runtime matching and authoritative migration were not run.

### Atomic function annotations: development increment

The ordinary C5 -> C6 -> C7 path now carries fixed-arity, single-body function
annotations such as `(defn identity-i64 [x :- I64] :- I64 x)` and checks typed
calls between such functions. C5 resolves annotation names in the type/protocol
namespace before expression-binding precedence; a lexical parameter named I64
does not capture the I64 type annotation. C6 derives concrete type IDs from the
authenticated core type binding, not its metatype or an unverified spelling.

The bounded declaration family is I8/I16/I32/I64, U8/U16/U32/U64, F32/F64,
Bool, String, Symbol, Keyword and Ratio. It does not introduce implicit numeric
coercion: a generic integer literal is not an I64 value merely because a
parameter or result is declared I64. Composite types, protocols as parameter
types, generics, dynamic boundaries and casts remain outside this increment.

Owning code is `gravity.resolution`, `gravity.checked-core` and
`gravity.compiler.c7-type-checker-engine`. The existing Clojure traversal and
projection entrypoints retain the real source coordinates and binding IDs.
Annotation markers and type leaves are consumed declaration syntax, not
parameters or executable expressions. Annotated functions carry a closed
optional `:sh07-atomic-function-signature-v1` signature and a versioned
declaration mapping; unannotated attribute shapes remain unchanged.

C7 seeds declared parameter and return types before fixed-point inference and
preserves them through existing recursive/higher-order completion. Declarations
appear in canonical typed function products, equality constraints and the
annotated typed identity domain. Unknown/var-only type names belong to C5,
malformed or unsupported annotation forms to C6, and incompatible argument or
result types to C7. Later effect, profile, capability, ownership and safety
legality is not implied by type acceptance.

Use the existing `sh07-b47-function-call-recursion-test` lowering kernel and
`sh08-function-call-type-test` inference kernel for the inner loop. The
`sh08-atomic-signature-authenticated-source-composition` selector exercises the
affected reader/macro/C5/C6/C7 composition and result verification on the paired
I64 identity/forwarding, Bool call and type-name-shadowing source fixture.
Its paired run passed 38 assertions. Three source-negative tests passed 73
assertions on eight real programs: unknown and var-only type names, dangling,
duplicate and composite annotation forms, a String result/argument against I64,
and a generic integer argument against I64. Their `.qst` text equality was
checked; this does not claim a second full negative composition run. Two
existing normalized acyclic-call and recursive fixed-point regressions passed
52 assertions. Final direct C6/C7 kernels passed 46/41 assertions, including
legacy malformed-header diagnostic priority, mixed annotated/unannotated
parameters and stale declaration/mapping rejection. C5/source-tool identity
kernels passed 17 assertions and the repository-language boundary passed five.
These five groups total 11 tests and 272 assertions. Earlier failed development
attempts are not counted as passing evidence.

The affected C8 pure-function adapter derives the annotated identity domain from
the actual declaration-bearing function records, preserving higher-order and
legacy precedence and exact identity/typed-core equality. Its compiled-Gravity
adapter kernel and existing pure-function regression passed 47 assertions;
the same run repeated the five-assertion language-boundary gate. These are
normalized adapter inputs, not another authentic C7 source proof. Declaration
and constraint preservation, stale-domain substitution, stripped declarations,
malformed carriers and result tampering are covered. The narrower effectful
compiler-read adapter remains unsupported for annotated functions. Independent
semantic review accepted this bounded increment; none of these development
checks is migration evidence.

Current-source C5 execution records actual source/plan identities under one
operation-local binding, rejects source/toolchain drift and remains
development-unadmitted. Private cache keys are not transported as semantic
artifact fields. Complete historical SOURCE/migration pins remain enforced at
their explicit boundary. The existing seed compiles one module function table;
general imported stage-function composition remains a separate implementation
task, not a reason to add a facade-only `gravity.types` namespace. No native,
target-completion, self-hosting or seed-retirement claim is made here.

### Boolean match typing: development increment

The ordinary `:meta/:jvm` C7 function path now types unguarded matches over an
independently established Bool scrutinee, using the actual C6 branch, pattern
and decision skeleton records. Supported root patterns are `true`, `false`, `_` and a
lexical binding. For example:

```clojure
(defn select-value [b :- Bool x :- I64 y :- I64] :- I64
  (match b true x false y))
(defn retain-bool [b :- Bool] :- Bool
  (match b true true remainder remainder))
```

Source-ordered residual domains start at `[true false]`. A literal consumes its
case; a wildcard or binding consumes the remaining cases. A branch binding has
Bool type, its actual lexical scope, and the residual value domain (for example
`[false]` for `remainder` above). Pattern shape does not supply a circular Bool
premise for an otherwise unknown scrutinee. Every reachable branch is typed;
the supported result join requires equal established types and preserves
declared function-result checks. No implicit numeric conversion or union is
introduced.

`L7-PATTERN-TYPE` diagnoses a pattern incompatible with a known Bool scrutinee;
`L7-UNREACHABLE` diagnoses an empty residual branch. At the direct C7 boundary,
a missing case in `:formal` without supported partiality evidence is
`L7-NONEXHAUSTIVE`. The current source C6 module boundary accepts only
`:meta/:jvm`: the real formal fixture stops there with `C6-VERIFY` before C7.
It does not establish formal-profile source composition. Outside mandatory
total contexts, partial matching remains explicitly unsupported with
`C7-ANNOTATION`, not an invented nil result or a universal language totality
rule. Unknown scrutinees and unsupported undeclared heterogeneous joins also
request annotations; a branch conflicting with an actual declared result is
`C7-TYPE-MISMATCH`. Upstream scope and irrefutable-pattern diagnostics remain
owned by C5/C6. Malformed C6-product joins are `C7-VERIFY`.

Owning implementation is `gravity.compiler.c7-type-checker-engine`, with the
existing `sh08-function-call-type-test` development selectors. C7 compares the
raw match families with C6's actual canonical preimage and checks source
classification, child order, global ordinals, pattern coordinates, binding
scope and branch-contained uses. Coverage, narrowing, branch-result premises,
origins, dependencies and invalidation inputs are retained in the existing
hashed constraint ledger and type table. Public APIs, outer artifact keys and
existing typed identity domains are unchanged. The pure C8 adapter preserves
the complete typed core and ledger; no effect policy is expanded.

The `sh08-boolean-match-development-kernel` passed 47 assertions; atomic
annotation and existing call regressions passed another 62. A normalized C8
ledger identity/verifier oracle passed eight, for 117 in the final cheap group.
The authentic `sh08-boolean-match-authenticated-source-composition` passed 32 assertions on
both source extensions, including I64 selection, residual Bool binding,
wildcard coverage, forwarding, fresh C7 verification and C8 preservation with
rewrite rejection. Its execution took 189.949 seconds, a development
observation rather than a benchmark. Subsequent ordinal/use-site hardening and
the undeclared-join diagnostic correction are covered by the final cheap
kernel; independent review confirmed the accepted fixture paths unchanged.
The ledger tag was then corrected to the existing `:equality` constraint kind,
retaining the versioned match producer and all premises. The paired source run
predates that one-field payload change: its typed artifact identities are not
the final identities. The final cheap group covers emitted equality facts and
generic C8 preservation/identity/rewrite checks; it is not a new source proof.
Earlier failed draft checks are not counted as passing evidence.

The first real rejection run passed 41 assertions on declared-result mismatch,
wrong pattern type, duplicate and subsumed clauses, then errored on that
upstream formal-profile boundary. It also passed the existing recursive
fixed-point regression (31) and language-boundary gate (5): 77 passed assertions
and one error, not an all-passing group. The remaining source cases were not
executed by that run. The separate boundary-closure selector then passed 24
assertions on the remaining partial/unknown cases, C5 scope escape and the
formal fixture's explicit C6 rejection. The formal fixture was re-evaluated
under that corrected expectation; the first four cases and accepted pair were
not repeated. Source-negative `.qst` text equality was checked without claiming
a second full negative composition. Broader C6 profile support requires its
own executable composition work; this increment does not weaken its boundary.

Guards, structured/union pattern typing, general subtyping and joins,
partiality-proof ingestion, recursive/higher-order match composition,
ownership/effect completion and runtime/backend equivalence remain outside
this Boolean-only increment. The structural increment below supplies
constructor-derived key/element evidence for map/vector bindings; it does not
itself type guards. The guard increment below adds the bounded Boolean case.
These development results are not authoritative migration or release evidence.

### Constructor-derived structural match typing: development increment

The C7 function path now derives recursive fixed-vector and keyword-map shapes
from actual C6 collection constructors and immutable `let` initializer links.
Map patterns select a subset of keys: extra constructor keys are retained,
not rejected. A present key whose value is nil is distinct from an absent key.
For example:

```clojure
(defn choose [flag :- Bool value :- I64 fallback :- I64] :- I64
  (let [packet {:extra :retained :payload [flag {:value value}]}]
    (match packet
      {:payload [true {:value item}]} item
      _ fallback)))
```

`item` receives the established I64 type of the actual value child, its C5
binding/scope identity, and separate pattern and constructor paths. Source
syntax stored in `:source-value` is not evaluated or used as field-value
evidence. Key literals, value-node types, constructor child order, lexical
initializer positions and binding uses supply the facts. Existing established
scalar-call types are retained; pattern shape does not manufacture them.

Ordered residual shapes preserve earlier failed tests. In particular, patterns
`{:a true :b true}`, `{:a true}`, `{:b true}`, `_` over two independent Bool
fields select TT, TF, FT and FF respectively, without overlapping or dropping
cases. Binding/wildcard leaves and supported same-type scalar literal tests
compose with nested maps/vectors. The result join still requires equal
established branch types. A declared return expectation reaches a nested match
only through validated final `let` result links, not through initializers or
arbitrary expressions.

The owner remains `gravity.compiler.c7-type-checker-engine`; focused selectors
remain in `gravity.self-hosting.sh08-function-call-type-test`:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-structural-match-shape-development-kernel \
  --exact sh08-structural-match-inference-development-kernel
```

`sh08-structural-match-c8-ledger-composition` is a separate, costly public C8
bundle check, not an inner-loop kernel.

Run `sh08-structural-match-authenticated-source-composition` when changing the
C5/C6-to-C7 composition or C8 handoff. It uses the existing accepted
`function-structural-matches` `.gravity`/`.qst` pair, including forwarding and
present-nil cases. The public C7 interface and outer artifact fields are
unchanged. Versioned structural premises, extraction facts, residual shapes,
source identities, dependencies and invalidation conditions use the existing
hashed type table and equality-constraint ledger. C8 preserves that complete
ledger; no C8 effect policy or production adapter is expanded.

The accepted pair passed 48 assertions, including fresh C7 verification and C8
shape/key/extraction rewrite rejection, in 191.877 seconds of test execution.
That is a development cost observation, not a benchmark or migration proof.
The source run precedes numeric-pattern diagnostic tightening and the root-vector
join correction described below; its map-root Bool tests, I64 binding extraction
and nil test do not take those corrected cases. Focused kernels cover those
later changes. This is not a source run of the final compiler bytes; the
expensive accepted pair is not repeated solely to refresh its receipt.

The first real rejection/regression group completed with 77 passed assertions
and one failure, not an all-passing result. Field-type mismatch, missing key,
declared result mismatch, unsupported function-return shapes and computed
keys, C5 scope escape, the existing recursive fixed-point regression and the
language-boundary gate were exercised. The vector-width case exposed a C7
join that compared the branch's original source vector with the vector
pattern record's intentionally nil value field; it reported `C7-VERIFY`
instead of `L7-UNREACHABLE`. That failure is retained rather than relabeled.
The corrected join compares the branch value with its authenticated source
form while retaining the vector record's nil/width/child checks. The isolated
`sh08-structural-match-source-vector-width-rejection` then passed six assertions;
the six already-correct source cases and accepted pair were not repeated.
The final current cheap group passed six tests and 173 assertions: shape (23),
inference and record joins (30), C8 preservation (11), Boolean matches (47),
atomic annotations (41), and legacy function chains (21). It includes the
root-vector join, explicit unsupported numeric elaboration, and direct
meta/formal partiality diagnostics. The formal kernel is not formal-profile
source composition.

Computed map keys, collection-shape propagation through function returns or
pattern-bound collection aliases, mutable aliases, generic or external shape
validation, numeric literal elaboration,
guards, general unions/subtyping, partiality proofs, recursive/higher-order
match composition, ownership/effect completion and runtime/backend equivalence
remain unsupported here. Numeric elaboration being unimplemented is not a
claim that an unsuffixed numeric pattern is fundamentally incompatible with a
fixed-width numeric value. Occurrence-specific decimal source descriptors are
not compared as numeric values. Existing shape/depth/residual/evidence bounds
produce explicit implementation-limit diagnostics, not new language legality
rules or silently increased limits. Source composition remains `:meta/:jvm`;
formal-source support still stops at the unchanged C6 boundary. Authoritative
migration, full SH-08, target support and release status do not advance from
these development checks.

### Boolean root-map guard typing: development increment

C7 now types normally returning guards on supported constructor-derived root
map patterns under L7's Boolean rule. For example:

```clojure
(defn permit [flag :- Bool] :- Bool flag)
(defn choose [flag :- Bool value :- I64 fallback :- I64] :- I64
  (match {:ok flag :value value}
    {:ok ok :value item :when (permit ok)} item
    {:value again :when false} again
    _ fallback))
```

The guard result must be established Bool. There is no `if`-style truthiness:
normally returning nil, integer, String and unknown values produce
`L7-GUARD-TYPE` at the guard. Bindings are available for the guard and its own
body, not sibling clauses or outer code. C7 authenticates C6's distinct
source-only, clause and branch guard projections, actual `:when` source slot,
guard node, conditional region and call membership. Match children contain
the scrutinee, all branch bodies, then guard nodes; result inference joins
only the validated body indexes, so Bool guards do not contaminate I64 results.

Structural failure skips the guard and body in the control-flow evidence.
The guard is recorded once after structural success, with true selecting the
body and false continuing. A Bool-returning call is not proof that the guard
is true: possible false fallthrough retains the incoming cases and requires
fallback coverage or another supported totality argument. A literal true can
consume the structurally matched cases; literal false has no selected body
cases and retains fallthrough. Its body is still type-checked, including
declared-result checks. This does not add a dead-code typing exception.

The existing equality ledger retains the guard premise, source, type, truth,
structural/body/fallthrough shapes, actual binding identities, ordinary call
records and evaluation regions. Guard inputs appear in the owning constraint's
dependencies, solution and invalidation fields. Preserved effect/capability
declarations remain declared inputs, not inferred empty-effect proofs. All
ordinary call facts survive the C8 handoff. No C8 effect policy or production
adapter, public C7 API, outer artifact schema, or authority pin is changed.

The owner remains `gravity.compiler.c7-type-checker-engine`. Use the existing
function-call test namespace for fast guard feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-root-map-guard-development-kernel \
  --exact sh08-root-map-guard-literal-development-kernel
```

Use `sh08-root-map-guard-c8-composition` separately when its C8 handoff is
affected; it constructs the public C8 bundle.

The source-ready group passed seven tests and 198 assertions, including
structural, Boolean, atomic-signature and legacy-call regressions. The actual
`sh08-root-map-guard-authenticated-source-composition` then passed 94 assertions
on the `function-root-map-guards` `.gravity`/`.qst` pair. It covers two guards
in one three-body match, I64/Bool separation, false continuation, a binding
used in both guard and body, literal-true total coverage, fresh C7 verification,
and complete C8 ledger/call preservation with rewrite rejection. Its 185.299
seconds of test execution is a development observation, not a benchmark.
Earlier failed draft checks are not acceptance evidence.

The real rejection/regression group passed four tests and 93 assertions:
normally returning nil/integer/String/unknown guards (32), guarded partiality,
false-guard body mismatch, unsupported throw and C5 scope escape (25), the
existing recursive fixed-point regression (31), and the language boundary (5).
Guard, body and match diagnostics are checked against actual core/syntax IDs,
spans and origins. Negative `.qst` fixtures were checked for equal source text,
not counted as a second full negative composition. The compiler remained
unchanged across the final 198-assertion kernels, accepted pair and real
rejections. Use `sh08-root-map-guard-source-type-rejections` and
`sh08-root-map-guard-source-boundary-rejections` for affected source-diagnostic
composition; do not repeat unrelated source construction for a local helper edit.

Nested structural guards, shape propagation through pattern-bound collection
aliases, non-returning/throw/try guard composition, general effect and ownership
legality, recursive/higher-order guard inference, runtime/backend equivalence
and authoritative migration remain outside this increment. Unmodeled guard
control transfer is explicitly unsupported, not assigned a fictitious Bool or
Never type. Direct formal partiality kernels do not extend C6's unchanged
`:meta/:jvm` source boundary. No full SH-08, self-hosting, target, seed-retirement
or release claim follows from these development results.

### Pattern-bound collection aliases: development increment

The C7 implementation extends constructor-derived map/vector evidence through
pattern bindings and authenticated immutable `let` links. An extracted
collection can be the scrutinee of another match in its own guard or body:

```clojure
(defn extract [flag :- Bool value :- I64] :- I64
  (match {:payload [flag {:value value}]}
    {:payload packet}
    (match packet
      [true {:value item}] item
      [false {:value item}] item)))
```

The implementation retains actual binding IDs, owner match/clause, lexical
scope, source uses, constructor IDs and extraction paths. It carries all whole
selected cases, not a first/last case or independently widened fields.
Reconstructing a collection from several extracted values joins their case
origins: shared origins must agree; independent origins form a bounded product.
The focused correlation oracle retains exactly TF/FT residual cases, excludes
impossible TT/FF combinations, and checks a four-case independent product.

Guard typing uses structural extraction premises before guard truth is known.
Literal-false guard bodies remain type-checked, including their nested aliases.
Shape state is derived afresh within each invocation and participates in the
existing inference fixed point. The canonical ledger, dependency and
invalidation facts retain the ancestry and correlated-construction premises;
public request fields and typed identity domains are unchanged. Size/product
overflow produces an explicit implementation-limit diagnostic at the alias
use, replacing prior evidence instead of silently retaining stale facts.

Use the existing function-call test namespace, without an authority refresh:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-pattern-alias-development-kernel \
  --exact sh08-pattern-alias-correlations-development-kernel \
  --exact sh08-pattern-alias-limit-development-kernel
```

`sh08-pattern-alias-c8-composition` is the separate public C8 bundle check.

The final focused group passed 11 tests and 241 assertions, including selected
structural, Boolean, guard, signature, call-chain and C8 regressions. The
initial real source pair passed its C7 checks but failed four C8 assertions:
the fixture had no ordinary call, while the existing C8 function-call adapter
requires nonempty call facts. This remains failed evidence. The corrected
fixture adds a real typed forwarding call; the C8 production adapter and its
acceptance policy are not weakened. That changed `.gravity`/`.qst` pair passed
124 assertions through actual C5/C6 products, fresh C7 verification and C8
preservation. It covers vector/map extraction, repeated immutable forwarding,
guard/body use, literal-false body typing, whole residual cases and correlated
reconstruction, distinct same-spelled binding IDs, and rejection of forged or
dropped ancestry. Test execution took 257.615 seconds; this is a development
observation, not a benchmark or migration proof. The changed source rejection
group then passed 37 assertions for incompatible nested patterns, absent keys,
wrong vector widths, declared-result mismatches, literal-false guard body
mismatches and escaped bindings. Diagnostics retain actual nested-match or
pattern syntax, spans and origins; negative `.qst` files were checked for equal
source text, not counted as a second full negative composition. The existing
recursive fixed-point regression (31 assertions) and language boundary (5)
passed in the same three-test, 73-assertion run. Compiler and test bytes were
unchanged across the final 241-assertion kernels, corrected accepted pair and
source rejections. Use `sh08-pattern-alias-authenticated-source-composition`
and `sh08-pattern-alias-source-rejections` only when that source composition
or its diagnostics are affected; the fast kernels remain the edit loop.

Computed, external, function-return and mutable collection shapes without
constructor evidence remain unsupported. General union/subtyping, recursive
or higher-order alias inference, effect/ownership legality, runtime/backend
equivalence and authoritative migration are not claimed. The source boundary
remains `:meta/:jvm`; no full SH-08, self-hosting, seed-retirement or release
status follows from this development increment.

### Recursive quoted data: development increment

C7's existing function path now derives types for L1/L2's inert quoted data:
nil, Bool, normalized exact integer, character, String, Keyword, Symbol, list,
vector, map and set, including
empty containers and recursively nested supported data. A quote inside an
outer quote remains an ordinary data list. For example:

```clojure
(defn token [] :- Symbol 'unresolved-data-name)
(defn inert-data [] '(throw unresolved-data-name))
(defn extract-token [] :- Symbol
  (match '[true {:value ready}]
    [flag {:value result}] result))
```

Payload symbols do not resolve, and payload lists do not become calls, bindings
or thrown-error effects. The checker authenticates the actual quote owner,
operator, two source children, payload parent/form/syntax/kind/value joins,
recursive child order and cardinality, and zero executable children/bindings
with `:no-evaluation`. A source-form collision with an executable node is
rejected. Quoted list types use the existing collection representation style:
`{:kind :gravity.type/list :element-types [...] :shape :literal}`. Types retain
heterogeneous members rather than widening them or inserting coercions.

Quoted map/vector shapes carry an explicit DATA-origin variant: the real quote
Core node plus actual payload form/syntax IDs and data paths. Payload form IDs
are not fabricated executable Core IDs. Extraction, immutable forwarding,
pattern aliases and correlated reconstruction retain these origins in the
existing canonical ledger, dependencies and invalidation. Quoted Boolean
guards retain their true/false meaning; other normally returning data must
still satisfy the Bool guard rule. Public C7 inputs and identity domains and
the C8 production adapter are unchanged.

Traversal has a shared payload-node-visit budget. Successful and failed
traversals charge consumed visits, including parent nodes and prior siblings;
preflight/collision failures retain the completed traversal count. This is not
a count of all VM operations, repeated shape queries or allocated bytes. A
small-budget oracle checks that a late three-node failure exhausts budget
three, budget four permits the next scalar, and collision rejection still
charges the three visits. Bounds produce explicit implementation-limit
diagnostics rather than silently accepting missing facts.

Use the existing focused runner and namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-quoted-data-recursive-development-kernel \
  --exact sh08-quoted-data-authentication-development-kernel \
  --exact sh08-quoted-data-work-budget-development-kernel \
  --exact sh08-quoted-data-structural-development-kernel \
  --exact sh08-quoted-data-guard-development-kernel
```

Run `sh08-quoted-data-c8-composition` separately for affected C8 integration;
it is not a cheap quoted-data kernel.

The stable focused group passed 17 tests and 399 assertions, including selected
atomic, structural, alias, guard, Boolean and call-chain regressions. Independent
static review closed the failed-traversal accounting finding. The actual
`function-recursive-quoted-data` `.gravity`/`.qst` pair then passed 144 assertions:
nine functions, ten quote nodes, all supported data families and empty
containers, inert throw/unresolved-name data, real DATA-origin links, Symbol
extraction, immutable/pattern aliases, quoted-false guard and nonkeyword map
data typing. A real forwarding call exercises fresh C7 verification and C8
preservation, with request/fact tampering rejected. Test execution took 224.901
seconds; this is a development observation, not a benchmark.

The changed real rejection group passed 42 assertions: Symbol/String and
generic-integer/I64 result mismatches, quoted-list guard typing, unsupported
decimal representation, nonkeyword structural map key, malformed quote arity
in C6 and unquoted unresolved name in C5. Diagnostics retain the actual quote,
payload or guard syntax, spans and origins. Rejected `.qst` files were checked
for equal paired text, not counted as another source composition. Ratio,
deferred-integer and tagged representations and quoted String/nil guards have
focused-kernel coverage, not additional real rejection runs. The existing
recursive fixed-point regression (31) and language boundary (5) passed in the
same three-test, 78-assertion invocation. Compiler/test bytes were unchanged
across the final kernels, accepted pair and rejection group. Use
`sh08-quoted-data-authenticated-source-composition` and
`sh08-quoted-data-source-rejections` for affected source composition, not as
the default edit loop. None of these checks confers authoritative migration
evidence.

Quoted integers retain generic integer type, not implicit I64 conversion.
Decimal/ratio/deferred-integer and tagged representations remain explicit
implementation limitations. Structural map matching still requires keyword
keys; other supported keys may occur in typed quoted map data. General scalar
root matching, numeric pattern elaboration, list patterns, union/subtyping,
error/import semantics, runtime/backend equivalence and authoritative migration
remain outside this increment. Source composition remains `:meta/:jvm`; no
full SH-08, self-hosting, target, seed-retirement or release claim follows.

### Canonical recursive quoted-DATA lowering: development implementation

The current Gravity emitter now consumes C7's existing recursive quoted-DATA
producer for all eleven supported kinds, rather than only Symbol. Nil, Bool,
represented exact integer, character, String, Keyword, Symbol, list, vector,
map and set lower to the existing `:quote` instruction. Empty and mixed nested
containers, qualified symbols, symbol map keys and nested quote lists remain
DATA. There is no unquote, payload name resolution or execution of a quoted
call/throw. The runtime representation and quote operation are unchanged.

The public typed operation still freshly verifies the same request and
candidate through current C7 before lowering. Its context joins the verified
inputs; the lowerer selects one exact producer/quote ID and checks shallow
owner, operator, payload, source, kind and root-origin coordinates. Recursive
shape, value, type and source authentication remain owned by C7. The projection
returns the recorded value directly instead of duplicating that checker.
Its logical charge remains four with the same cumulative cap. The dispatcher
also avoids hashing a composite type merely to reject nil/unknown types.
Neither fact is a total VM-work, memory or performance claim.

The focused batch passed five tests/264 assertions: all eleven kinds and empty
containers, shallow join rejection, budget three versus four, an opaque
no-recursive-inspection sentinel, and existing catch, raw-source, typed-input
and public orchestration checks. These tests consume normalized verified-context
records, not authentic source proof. The final test-only change removes five
unchanged source-rejection rebuilds from the then-unrun affected selector;
the focused bodies and compiler/fixture bytes are unchanged, so no focused
replay is attributed to that selector change.

The new complete `local-runtime-quoted-data` paired fixture contains sixteen
lowerable functions, including all supported quote roots, inert throw and
nested quote lists, let forwarding and direct same-module forwarding. The
affected check constructs each C5/C6/C7 input once, uses representative public
compile plus whole assembly, and executes the complete runtime leaf. It also
tests fresh public rejection of recursive child-origin, value, type and source
forgeries. Existing C5/C6/C7 malformed quote, unresolved-name, annotation and
unsupported-numeric rejection evidence remains scoped to its unchanged owning
revision rather than being rebuilt for this consumer edit.

The sole affected composition passed one test/78 assertions on the final
candidate, including thirty-two guest function executions and eight fresh
public C7 verification entries. Test execution took 275.677 seconds plus 7.269
seconds of namespace loading at the unchanged 2 GiB heap. Compiler, fixture,
test, C7 and runtime bytes were unchanged through successful process exit.
The eight entries are observed verifier calls, not a count of all internal
construction, traversal or allocation. Focused and composition workloads differ;
these timings establish no speedup or phase dominance.

Evaluated collection constructors, unquote, numeric elaboration, general match
execution, full effect/profile legality, target support, authoritative migration,
self-hosting and seed retirement remain outstanding. The direct focused and
affected commands are in `docs/development-verification.md`.

### Canonical evaluated vectors: development implementation

The existing Gravity emitter lowers C6 vector-constructor children with C7's
known positional types into `:vector-literal` instructions. The actual
`gravity.runtime` evaluator now supports those instructions by reusing its
ordered argument traversal. Empty, mixed and nested vectors, typed Bool/String
parameters, quoted DATA elements, let forwarding and direct same-module returns
are supported within the compiler-internal `:meta/:jvm` development route.
Source collection values are not substituted for evaluated children.

Items evaluate once from left to right with operand recur targets cleared.
The first throw or rejection preserves its original payload and accumulated
output and prevents later item execution. Quoted control-looking records are
ordinary DATA, not transfers. The new descriptor is closed, bounded to 65,536
immediate instruction maps, and checked shallowly before item execution. Fatal
host failures remain fatal. Lowering reserves its join work before traversal
and cumulatively charges child compilation within the unchanged cap; these are
logical work units, not a total VM or heap guarantee.

The final focused batch passed six tests/212 assertions: vector lowering and
runtime behavior, independent existing C8 equations, recursive quote regression,
runtime catalog/unchanged-body conservation and public orchestration. The first
six-test/210-assertion pass preceded a diagnostic correction: valid but
unsupported maps/sets now retain `:unsupported-typed-lowering-operation` rather
than being mislabeled malformed vectors. Two new controls check that boundary.
Unknown/incoherent type, Never, source/count/order and descriptor cases reject;
runtime controls cover transfers in each position, skipped output and fatal
failure identity.

Only the existing evaluator body changed in the runtime leaf; its 22 functions,
arities and four exports remain. Current conservation checks preserve all 23
catalog DATA values and the unchanged 21 leaf/29 reconstructed original
function bodies, explicitly excluding that intentionally changed owner. The
earlier complete C/D body-conservation result remains historical evidence for
its own revision, not a refreshed whole-runtime claim.

The paired `local-runtime-evaluated-vector` fixture contains nine functions and
twelve vector nodes. Its affected composition reuses each original input for
public compile/whole assembly, runtime checks and independent actual C8 body
analysis. A new String-versus-vector result fixture exercises the owning C7
diagnostic. Existing public typed inputs, fresh C7 verification and source
provenance remain unchanged; no old quote/module proof matrix is refreshed.

The first affected run failed overall: 71 assertions passed, 54 failed and
none errored. The new C8 test consumer passed the outer typed artifact instead
of its required `:identity-input` facts, then asserted against a missing graph.
The failed result remains failed. Independent count/dataflow reconciliation
attributes all 54 failures to those 27 C8 checks per extension, leaving the 71
non-C8 source/lowering/runtime/provenance/tamper/rejection assertions completed
and passed. The first raw output chunk was truncated; this attribution uses
the exact failed test, deterministic missing-graph checks and terminal counts,
not a claim that all stdout was retained. Production code was unchanged by the
subsequent test-only correction. A focused two-test/13-assertion check pins the
correct facts object and prevents missing-graph cascades. The corrected C8-only
source check then passed one test/31 assertions on `.gravity` alone, including
full body coverage, constructor/operand effects, order rejection and missing
declaration rejection. It took 126.439 seconds plus 7.397 seconds of loading at
2 GiB; the failed aggregate took 328.618 seconds plus 7.528 seconds of loading.
These are different workloads, not a speedup comparison. Compiler and fixture
bytes were unchanged across both runs; only the C8 test calls and guard changed.
The completed lowering/runtime/public verification work was not repeated. The
corrected combined selector was not rerun or relabeled passed; selective
validation and its limits remain explicit.

P3 permits compiler-owned internal allocation; an empty direct constructor
effect set does not imply zero physical allocation or grant runtime authority.
Operand effects, ordered body coverage and declaration rejection are retained.
Full C8/profile/provider legality is still explicitly pending and is required
before MIR/admission claims. Evaluated maps have a separate bounded increment
below. Evaluated sets, general collection operations,
hosted/constrained representations, target support, authoritative migration,
self-hosting and seed retirement remain outstanding. Commands and boundaries
are in `docs/development-verification.md`.

### Map source-order projection: development implementation

The temporary SH-07 request projection previously enumerated host maps even
though C2 retained textual entry order. The current correction joins retained
C2 key/value occurrences to the existing projected records and permutes only
evaluated map parents' child links. Existing form/syntax IDs, bindings,
reference paths, scopes, decimal coordinates and other record fields remain
unchanged. Known `defn` expansion uses its retained source origin and verified
header/body transfer; missing or ambiguous evaluated occurrences fail with
`C6-ORIGIN` rather than silently falling back to host order.

Quoted map DATA is inert and retains prior materialization, including repeated
keys already collapsed by the reader adapter. This is not a new language-wide
duplicate-map policy. Evaluated duplicate-key occurrence loss remains an
unsupported provenance case, not a newly declared language error. Metadata,
quote abbreviations, nested maps and set member coordinates have focused
coverage. Redundant whole-container equality and hashing of entry values were
removed; no new host-projection work counter or performance claim is made.

The final focused invocation passed two tests/207 assertions, including an
original-C2-shaped repeated-key control with four source children and two
projected children for both prefix and explicit list quotes. Independent
scoped review accepted the final code, focused result and real-source
composition oracle. The preceding revision passed two tests/204 assertions;
the final delta adds abbreviation checks and the list-quote case.
The earlier negative-control runs failed because they targeted a
metadata-only map rather than the actual syntax root; they remain failed
development runs. Real paired source composition then passed one test/55
assertions: both nine-key functions preserve opposite textual orders from C2
through request links to ordered Core children and their associated throw
operands, even though both host maps enumerate keys alike. Original quoted
duplicate entries remain inert materialized DATA. A rehashed pair-order
mutation fails fresh expected-request membership before Core execution.
The run used 2 GiB; the selected test took 197.354 seconds, outer execution
197.356 seconds, and namespace loading 6.854 seconds. These are development
observations, not a performance comparison.
The compiler, test and fixture bytes stayed unchanged across the final focused
and affected composition runs. No evaluated-map runtime,
authoritative migration, target, self-hosting or release claim follows.

### Canonical evaluated maps: development implementation

The current emitter/runtime increment adds evaluated maps with distinct
literal Keyword keys through canonical typed lowering. Empty and nested maps,
map/vector nesting, typed parameter values, immutable let aliases, direct
same-module returns and typed catch are the bounded fixture scope. Ordered C6
children and flat C7 member types drive lowering; no source map is substituted
for evaluated children. The shared collection helper retains vector checks and
their logical charge. Maps reserve `12 + 14 * child-count` logical join units
before cumulative child compilation; this is not a total VM-work claim.

Runtime consumes closed ordered entry instructions, validates their immediate
shape and literal unique Keyword keys before evaluation, then evaluates each
key and value once. The first throw/rejection preserves payload identity and
accumulated output and prevents later execution. Quoted maps remain inert DATA.
Computed, non-Keyword and duplicate evaluated keys remain implementation
limitations, not language-wide prohibitions. The legacy untyped host-map
compiler still sorts keys; this increment's source-order claim belongs to the
canonical typed route, not arbitrary instruction producers.

The final focused group passed seven tests/161 assertions: map lowering and
runtime behavior, vector regressions, runtime DATA/unchanged-body conservation,
independent C8 equations and correct C8 consumer facts. Keyword-first validation
rejects an opaque non-Keyword key without hashing or equality calls. The first
map-focused run failed two assertions because its synthetic Never case used a
reference without a local activation context; only that test input changed.
That failed run remains failed. The single affected paired-source composition
then passed one test/151 assertions, including 24 guest executions across twelve
functions per source spelling. Normal, nested and forwarded values, typed
catch, exact parameter identity, inert duplicate-key quotes and opposite
nine-key first-throw orders all passed. Public compile/assembly freshly entered
C7 verification seven times; type/order forgeries reject, and a rehashed C6
order mutation fails membership before Core execution. Independent existing
C8 body checks retain ordered operands, thrown effects and declaration
rejection. A new declared-String/map-result source fixture fails with the
owning C7 diagnostic. Each positive input was reused for these assertions;
old quote, vector and migration proof matrices were not replayed.

At 2 GiB, the selected composition took 391.939 seconds, outer execution
391.942 seconds, and namespace loading 7.857 seconds. The final focused group
took 1.015 seconds of outer execution plus 7.259 seconds of loading. These are
different workloads, not a speedup comparison or phase-cost attribution.
Compiler, test and fixture bytes remained fixed across the final focused and
composition runs. Public APIs, C7 and C8 production
code, migration pins and explicit pending effect/profile legality are unchanged.
No target, authoritative migration, self-hosting or release claim follows.

### Literal Keyword map lookup: development implementation

The current C7/emitter/effects/runtime increment connects SH-07's existing
one-operand `(:key map-expression)` Core form to executable development IR.
Constructor and verified quoted-map shapes, immutable let aliases and nested
lookup extraction supply member evidence. Present members retain their actual
type; established absence yields Nil, distinct from a present nil member.
A Never operand remains non-returning. Missing shape evidence is rejected
explicitly rather than replaced with Dynamic. Arbitrary map parameters,
call-returned shapes, generic `get`, defaults and non-Keyword keys remain
unsupported implementation boundaries, not new language prohibitions.

The solved C7 row records the source, canonical lookup record, operand and
selected shape, selected coordinate, presence and invalidation dependencies.
Lowering consumes that row behind fresh same-input C7 verification, preserving
ordered key/operand instructions. The runtime evaluates the entire operand
once, even when the key is absent, and preserves first transfer, payload
identity and output. C8 retains operand effects and escaping thrown types;
lookup itself does not erase an undeclared throw.

The final focused group passed seven tests/255 assertions, including C7 and
lowering joins, present/absent/Never results, runtime order and control, C8
equations/consumer facts, map and recursive-quote regressions, and runtime DATA
conservation. Failed shape attempts retain outer logical charges; exact and
one-less controls exercise two inference rounds and shared lookup/loop work.
These charges do not count recursive helper visits or prove total CPU cost;
the existing shape bounds remain separate. At 2 GiB this group took 1.977
seconds of outer execution plus 7.557 seconds of namespace loading. An earlier
runtime-focused test failed to compile because of a misplaced test parenthesis;
the corrected test's success does not relabel that failed invocation.

The existing SH-07 arity regression separately passed one test/78 assertions
across three rejected cases in both source spellings. It reconstructs complete
source frontdoors rather than using normalized Core inputs: selected execution
took 251.631 seconds, outer execution 251.633 seconds, plus 6.801 seconds of
loading. That cost is not part of the focused-kernel timing above; this unchanged
upstream matrix is not a requirement for each lookup edit.

The single affected paired-source composition passed one test/158 assertions.
Each positive source product was reused for public single-function lowering,
whole assembly, nine guest executions and independent C8 body checks: eighteen
executions across the two source spellings. Each assembly contains nine
functions, ten lookup instructions, seven evaluated maps and two quoted values.
The solved rows distinguish eight present lookups, one established absence and
one non-returning operand. Constructor/let/nested/quoted extraction, present
nil, missing key, inert duplicate-key quote materialization and catch around a
throwing operand all passed, including original payload identity.

Four positive public operations and six type/shape/ledger/key/operand/source
tamper cases freshly entered public C7 verification ten times. Both source
spellings produced equal typed identity inputs. The two new source negatives
reject a scalar operand with `C7-ANNOTATION` /
`:keyword-lookup-map-shape-required`, and a declared String result receiving an
absent-key Nil with `C7-TYPE-MISMATCH` /
`:function-result-type-mismatch`. C8 receives the actual typed identity input,
retains lookup operand order and thrown types, and rejects an undeclared throw;
this does not turn pending full profile legality into an accepted claim.

At 2 GiB the selected composition took 415.574 seconds, outer execution
415.575 seconds, and namespace loading 7.695 seconds. Compiler, test and fixture
bytes were unchanged across final focused and composition runs. The result's
158 assertions include six whole-runtime projection checks omitted from an
earlier static count forecast; no test changed to obtain that count. This is
affected composition evidence, not a speedup comparison or attribution of cost
to individual proof-construction phases. No old map/vector/quote migration
matrix was replayed. Focused success and scoped independent acceptance do not
confer migration, complete effect/profile legality, target, self-hosting or
release authority. Existing authoritative pins remain unchanged and are not
evidence for the new code.

### Local call-return shapes: development implementation

The C7 shape path now follows actual same-module acyclic calls into their
callee bodies. Constructor, quoted and immutable-let shape evidence can cross
a forwarding call and feed the existing Keyword lookup and structural-match
consumers. Call, edge, operator binding, definition, function/body, source and
type joins remain explicit in retained ancestry and invalidation conditions.
Same-arity functions with equal member-type vectors do not establish equal
map keys. Caller argument values do not specialize the callee's shape.

The corrected normalized group passed six tests/155 assertions: new call-shape and
fixture checks, the existing lookup lowerer, and structural inference,
pattern-alias and quoted-shape regressions. Controls include caller-before-callee
inference, body/edge/binding/source mutations, absent keys with unchanged member
types, unknown parameter-carried shapes, duplicate records, depth, cumulative
sibling/failure work and unused-index avoidance. New logical index/join and
retained-evidence charges share the inference budget; separately bounded seed
and helper reconstruction is not an aggregate CPU-visit claim. Earlier cheap
runs exposed a source delimiter error and an unsupported seed `symbol` builtin;
both failed invocations remain failed. The correction did not extend the seed.

At 2 GiB the corrected group took 1.732 seconds of outer test execution, with 7.802
seconds of runtime-test namespace loading and 0.325 seconds for the additional
type-test namespace. These normalized results do not establish source
composition. The first affected source run failed on its first input with
`P15S23Q002` / `:malformed-typed-input-families` before guest execution. Its six
passing assertions checked only whole-runtime projection; they are not
call-return evidence. The test attempted lowering before reporting the
upstream typed status. One narrow diagnostic recovered C7's source/type-join
rejection. Static producer/validator comparison then found a sufficient cause:
the canonical edge carries a definition's name-syntax ID, while a dormant
validator compared it with the whole definition's syntax ID. The new normalized
test had mirrored that incorrect expectation. The corrected validator retains
both source joins, and the test now derives its edge from the owning producer,
uses distinct IDs and rejects the old wrong edge. The composition reports typed
acceptance and verification before lowering. A separate affected existing edge
test passed one test/five assertions after its normalized source record was
corrected; this is not a combined seven-test run. The failed composition remains
failed. The narrow diagnostic printed only the first two of eight diagnostics;
the static mismatch was not a measured sole-predicate attribution.

The corrected candidate's single composition passed one test/149 assertions.
Two real source spellings each supplied one fresh product reused for public
function lowering, complete sixteen-function assembly, sixteen top-level guest
executions and C8 body analysis. Seven public operations freshly entered C7
verification, including three ancestry/key tamper rejections. The functions
exercise forward calls before their definitions, nested/quoted/let-returned
maps, equal return types with different literal keys, present nil, established
absence, catches and original payload identity. No emitter or runtime API
change was needed.

Three additional fresh compiler inputs reject an extracted String declared as
Bool with `C7-TYPE-MISMATCH`, accept an exhaustive constructor-derived structural
match, and reject the old wildcard fallback with `L7-UNREACHABLE`. The last check
preserves the second clause's exact syntax/span/origin coordinates. Only the
`.gravity` structural-match input was type-checked; its `.qst` pair was checked
for byte parity. Structural-match execution remains unsupported. At 2 GiB the
corrected selected composition took 551.919 seconds, outer execution 551.921
seconds and namespace loading 7.385 seconds. These costs are separate from
focused feedback and do not identify individual construction phases or prove
a speedup. Corrected production/test/fixture bytes stayed fixed during this
composition; no unchanged old lookup/map/vector/quote proof matrix was replayed.

No runtime match,
imported/recursive return-shape, unknown map-parameter, unresolved alternative
shape, complete profile-legality, target, migration or self-hosting claim
follows. Runtime and emitter APIs and authoritative pins are unchanged.

### Finite Boolean match execution: development implementation

The existing Gravity emitter consumes C7's solved finite Boolean match rows
and lowers root Boolean literals, wildcard and lexical binding patterns into
the existing `if`, `do` and `let` instructions. Rows join by match identity and
clause ordinal, not their branch-body `lhs`. Source, branch, root-pattern,
skeleton, binding scope, type and selected/residual-domain joins remain
explicit. The scrutinee is evaluated once. A residual binder is initialized
with its verified singleton Boolean inside its own branch, preserving an
outer same-named value in a sibling. No generated name, runtime match opcode,
new seed builtin or public API is introduced.

The affected throwing-local-call composition exposed two existing supported-
form restrictions: C7's call-error graph and the independent verifier in
`gravity/effects.gravity` omitted `match`. Both now accept it using their
existing conservative child-error unions, source order, call-body equations,
match-skeleton checks and budgets. This retains source-error potential from
all branches, not selected-path precision. Existing unsupported-operation,
recursion-component, import and higher-order boundaries are not relaxed.

The pre-composition focused group passed four tests/106 assertions: lowering joins
and work limits, false-first/whole-binder/wildcard routes, active C7 call-error
analysis with independent C8 reconstruction, and five fixture-pair parity
checks. Controls cover original throw payload identity, single evaluation,
non-leaking bindings, changed ledger/source/order/call-body evidence, missing
rows, unknown operations, recursion components and insufficient budgets. An
initial binder-use work-limit failure was being relabeled as a generic
binding mismatch; it now retains the original failure, with an intermediate-
budget control that reaches the traversal. A command using unsupported
`--result-file` failed before executing tests; the corrected command uses the
existing `--timing-receipt` option. That failure is not relabeled as a pass.

At 2 GiB, that focused test execution took 2.238 seconds, separate from
7.505 seconds of namespace loading. Independent scoped pre-run review accepted
the exact candidate. Its single real-source composition completed one test/184
assertions: 182 passed, two failed, zero errors. Both failures were the same
instruction-census oracle: two wildcard matches emit `do`, but the test had
omitted the existing source-let body wrapper in the nested function. The
observed count was three, not two. The original aggregate remains FAILED.

The completed semantic blocks exercised 48 top-level guest executions across
the two source extensions, including returned-map lookup into a match,
shadowing, nested binders, branch-tail recur, original thrown-payload identity,
and an ordinary caller through a match to a normally returning Bool helper
that may throw. Both independent C8 analyses passed, retaining conditional
coverage and conservative thrown errors and rejecting an undeclared allowance.
Seven public operations freshly entered C7 verification, including three
row/source mutations that rejected. Three additional `.gravity`
inputs rejected incompatible patterns, unreachable clauses and branch-result
mismatch; a fourth rejected binder escape in C5. Rejected `.qst` pairs were
checked for byte parity, not separately type-checked.

Only the test's expected `do` count and an explanatory comment changed after
that result. The four K kernels plus the existing exact let-lowering kernel
then passed a separate five-test/169-assertion run. Its outer execution took
2.486 seconds, separate from 7.521 seconds of namespace loading. These overlap
the earlier controls and are not added as unique coverage. Independent review
accepted the bounded semantic evidence and exact oracle correction; the
corrected full selector was not replayed or reported as a new 184/184 pass.
No production or fixture bytes changed after the composition. At 2 GiB its
selected execution took 664.653 seconds, outer execution 664.655 seconds and
namespace loading 8.267 seconds. These timings do not attribute construction
phases or establish a speedup. No unchanged upstream matrix was repeated.

Structural-pattern and guard execution, partial Boolean lowering, complete
profile legality, target lowering, authoritative migration and self-hosting
remain outstanding. This finite implementation limit does not impose a
universal L7 totality rule. Normally returning guards retain L7's Bool-only
policy. No historical authority pin is refreshed by this development work.

### Constructor-linked structural match execution: development increment

The Gravity emitter now projects C7's existing unguarded structural pattern
trees into one closed internal `:structural-match` instruction, evaluated by
the stage-owned Gravity runtime. Fixed vectors require exact width; literal-
Keyword subset maps permit extra keys and distinguish absent keys from present
nil. Nested wildcard, lexical binding and represented primitive leaves preserve
source order. Family checks precede literal equality. Represented Integer
matching does not authorize coercion to fixed-width numeric types.

C7 continues to own constructor shapes, partitioning, extraction facts and
exhaustiveness. The emitter joins the consumed tree, source coordinates,
clauses, binding scopes, narrowed types and uses instead of duplicating that
solver. Runtime bindings from a failed partial pattern are discarded before
the next clause. The scrutinee executes once without an outer recur target;
the selected body retains its own tail target and original control payload.
The runtime's four public exports remain unchanged. Descriptor bounds are
local shape bounds, not a global evaluator CPU or termination proof.

The final normalized group passed twelve tests/291 assertions: structural
lowering, runtime behavior, represented Integer matching, fixture parity,
unchanged-runtime conservation, existing Boolean lowering/routes, three C7
structural/quoted-shape kernels and two C8 operation/coverage kernels. Controls
include exact lowering work limits, malformed source/scope/coordinate evidence,
absent-versus-nil and width, same-name binder rollback, opaque values with zero
equality/hash calls, validation before output, recur boundaries and original
fatal-error identity. At 2 GiB the outer test execution took 2.015 seconds,
separate from 7.644 seconds of runtime-test namespace loading and 0.482 seconds
for the additional type-test namespace. Overlapping earlier focused runs are
not added as unique coverage.

Earlier invocations exposed a matcher delimiter error, an unsupported seed
`merge` call, normalized fixture origin mismatch and a test delimiter error.
Those failed invocations remain failed. The corrections preserve the strict
production source join and use a private map-extension loop without extending
the seed. Scoped independent review accepted the frozen candidate for one
affected real-source composition. That original run passed one test/177
assertions without a retry or input changes.

Each real source spelling supplied one fresh product reused for public
function lowering, complete fourteen-function assembly, twenty top-level guest
executions and actual C8 body analysis. The forty executions cover returned
and quoted constructors, nested shadowing, multiple binders, partial-binding
rollback, called throws with original String identity, and source-verified
selected-body function recur. Each source contains eleven matches and
seventeen structural rows. Both C8 analyses preserve conditional coverage and
conservative throw propagation, and reject an undeclared allowance.

Eight tracked emitter operations freshly entered public C7 verification:
four positive compile/assembly operations and four coherent mutations. The
mutations change extraction paths across their aliases, pattern coordinates,
binding scopes or remaining-domain evidence and reject with `P15S23Q002`.
That count excludes the source-product constructor's upstream verification.
Four additional `.gravity` inputs reject incompatible patterns with
`L7-PATTERN-TYPE`, known absent-key and wrong-width clauses with
`L7-UNREACHABLE`, and reachable result mismatch with `C7-TYPE-MISMATCH`.
A fifth rejects binder escape with `C5-UNRESOLVED`. Their rejected `.qst`
pairs were checked for byte parity, not separately type-checked.

At 2 GiB the selected composition took 737.988 seconds, outer test execution
737.990 seconds and namespace loading 7.584 seconds. All fifteen compiler,
runtime, test and fixture files stayed unchanged across the final focused and
composition runs. These different workloads do not establish a speedup or
identify which construction phase dominates. No unchanged Boolean, lookup,
map, vector or quote proof matrix was replayed.

Guards, additional L7 pattern families, complete profile legality, documented
target lowering, authoritative migration, self-hosting and release remain
outstanding. Existing authority pins are unchanged and do not certify this
development code.

### Root-map guard completion and execution: development increment

The current guard increment retains L7's Bool-only normal result policy and
preserves non-returning control. C7 no longer rejects every guard containing a
throw, try or recur descendant. Existing type, target and effect checks still
decide whether those expressions are legal. Every body retains its actual type
and legality checks; a body that cannot execute after a false or Never guard
has separate effective-completion evidence instead of a fabricated body type.
A Never guard removes its structurally matched cases by transferring control,
not by selecting its body or falling through.

The emitter consumes those facts through the existing closed structural-match
instruction. Runtime checks normal results are Boolean, evaluates guards once
after structural success, restores outer bindings on false while preserving
output, and propagates original non-normal control. Guards cannot recur to an
outer target; selected bodies retain their original targets, and a guard-local
loop retains its own target. C8 independently accepts authenticated Bool or
Never guards and retains conservative source-error potential.

Review caught two valid-program regressions in the draft. An Unknown first
branch was being mistaken for an empty result accumulator, allowing a later
Never branch to fix an inferred return prematurely. The correction uses the
existing alternatives join over all effective branch types; owning first-pass
and acyclic caller-before-callee checks preserve Unknown until String resolves.
Nested guards also exposed a difference between grouped Core child layout and
actual guard-before-body lowering order. Binder-use collection now determines
reachability, precharges its original-node scan and emits uses in authenticated
node order. Exact-budget, one-less and shared Boolean/structural lowering
controls passed. Neither finding required another parser or authority wrapper.

The final corrected normalized group passed fifteen tests/685 assertions: C7 completion,
quoted guards and forward inference; C8 guard checks; emitter/runtime controls;
fixture parity; unchanged-runtime conservation; and existing normalized C6 guard,
Boolean lowering, structural lowering and local-loop ancestry regressions.
At 2 GiB outer test execution took 4.781 seconds, with 7.733 seconds of runtime-
test namespace loading, 0.270 seconds for the C6 test namespace and 0.332 seconds
for the type-test namespace. Earlier thirteen-test/612-assertion,
ten-test/235-assertion and four-test/112-assertion groups
overlap these controls and are not added as unique coverage. No unchanged
four-source SH-07 rejection matrix was repeated.

Failed drafts remain failed: a test delimiter error prevented the first
producer batch from executing; a later normalized typed-try fixture retained
stale handler-use and ancestor source values; and the first consumer batch had
two errors from an unsupported seed `dissoc` and a runtime delimiter error.
The corrections keep the production handler source join strict, use an explicit
field projection rather than expanding the seed, and preserve those outcomes.
A broader batch also failed one of 235 assertions because its normalized C8
graph supplied ordered error values without the required escaping-error set.
The fixture now carries both representations, as actual derived nodes do;
the production checker was unchanged. That 234-pass/one-failure run is not
relabeled as the later ten-test/235-assertion pass.

Paired source fixtures cover inline throw, conditional throw, typed catch,
false and Never completion, nested binder use, loop/recur and forward inference.
The first real-source composition failed after nine passing assertions with
one uncaught lowering error, `typed-loop-source-or-target-mismatch`, while
assembling the guard-local loop case. Its one-test/ten-assertion result remains
failed; later runtime, effect and negative-source checks in that test did not
complete. Selected execution took 185.289 seconds, with 7.879 seconds of test
namespace loading. The source/target join requires correction and affected
validation; this is not a reason to remove the legal local-loop case or relax
outer-target recur rejection.

The correction preserves the nearest lexical target while compiling a guard
with tail eligibility disabled, matching C6. All existing loop source, target,
scope and ledger equalities remain intact. Its affected normalized group passed
four tests/145 assertions, including sixteen new guard-local-loop checks,
existing loop lowering and guard lowering/runtime checks. Execution took 2.502
seconds plus 7.659 seconds of namespace loading. An initial version of the new
fixture omitted its branch clause ordinal and failed three of 136 assertions
at the guard-evidence join, before loop compilation; the fixture was corrected,
not the production join. These overlapping focused results are not added to
the earlier group as unique coverage.

The corrected candidate's real-source run completed 222 assertions with 221
passes and one failure. Its only failed assertion read the final C6 diagnostic
from `:id`, although the owning C6 diagnostic and rejection bridge retain
`:rule`; the following exact `:recur-tail-position-required` assertion passed.
The aggregate remains failed, not a 222-assertion pass. Selected execution took
1080.451 seconds, plus 8.031 seconds of namespace loading, at the unchanged
2 GiB heap. Compiler and fixture bytes were unchanged throughout that run.

Its passing assertions cover both source extensions, 45 guest executions,
preserved payload identity, false/Never effective completion, forward String
inference, nested binder use, guard-local loop and selected-body recur. Two
actual C8 analyses retained guard dependencies and source errors, discharged
the typed catch and rejected an undeclared module allowance. Nine tracked
emitter C7 verification entries include four coherent type/truth/completion/
region forgeries; this count excludes upstream constructor verification.
Six C7 negatives and three earlier C5 negatives also passed, with thirteen
fresh source inputs in total. Rejected `.qst` pairs have fixture parity checks,
not separately repeated source construction. No seed throw-transport count
was instrumented or claimed.

The test now reads the owning C6 `:rule` field and retains the exact reason
assertion. A separate five-test/432-assertion focused run passed, including
seven direct checks through the current C6 recur lowerer, rendered rejection
and existing exception bridge. It also covers the normalized C6 guard kernel,
guard-local parent and guard lowering/runtime controls. Execution took 3.185
seconds plus 7.838 and 0.272 seconds of namespace loading. Its first invocation
stopped at a test-source delimiter error before assertions; that outcome is
preserved. No compiler or fixture changed for this correction, and the
18-minute source composition was not replayed or relabeled as passed.

Do not replay unchanged source construction merely to turn a test-only field
correction into a fresh aggregate pass. This development evidence does
not establish full L7, profile/provider legality, documented target support,
authoritative migration, self-hosting or release completion. Historical
authority pins remain unchanged.

### Direct integral ordering: development increment

The comparison increment adds direct binary `<`, `<=`, `>` and `>=`
over two operands of one established integral type. Integer remains distinct
from the eight fixed-width signed/unsigned types; no implicit conversion,
floating ordering or first-class operator value is supplied. The finite
proof-required safety rule derives Bool on normal invocation and Never when
an operand cannot return, while still checking other operands.

C7 reconstructs an occurrence-specific intrinsic descriptor, call fact and
numeric-mode constraint from the actual existing core binding. C8 and lowering
check the finite rule independently without inventing function/body identities
or changing C6's call and edge shapes. The semantic owner is `gravity.math`;
the existing core catalog is compatibility resolution, not a fabricated math
module artifact. Runtime code is unchanged. Inference reuses per-pass indexes
instead of rebuilding a node index inside every scope edge; this is not a
measured speedup or a total CPU-accounting claim.

The focused candidate passed seventeen tests/720 assertions, including the
four numeric kernels, paired fixture checks and affected guard, structural,
Boolean, direct-call and body-effect regressions. Execution took 2.968 seconds
plus 7.868 and 0.343 seconds of namespace loading at 2 GiB. The first group
remains failed: 383 assertions passed and three new runtime test expectations
used `:throw` instead of the existing `:thrown` control tag. Only those test
oracles were corrected; the production runtime was not changed. Overlapping
groups are not summed as unique coverage.

A subsequent dispatch-preservation correction restricts intrinsic lowering to
nonlocal calls whose operator occurrence has the applied-ordering descriptor.
Other calls keep their original direct-call rejection reason. Four affected
lowering kernels passed 140 assertions on that correction (2.282 seconds
execution plus 7.707 seconds loading at 2 GiB); this is separate evidence, not
a claim that the earlier 720-assertion group was rerun on changed bytes.

The first affected real-source composition failed before numeric checking:
six assertions passed, then C5 rejected the fixture's `gravity.core/>` variable
spelling with `C5-ALIAS` / `:unknown-namespace-alias`. It took 38.007 seconds
execution plus 8.047 seconds loading at 2 GiB. This establishes no paired
numeric, guest execution, C8 or negative-source coverage. Qualified core
variable resolution is not implemented by this slice; normalized descriptor
tests do not establish that source admission.

The fixture-only correction uses unqualified `>` and an ordinary String
parameter named `>` passed to a local function. This tests lexical value
shadowing without pretending it is a qualified core call or an intrinsic.
The final fixture/type kernels passed two tests/308 assertions in 0.742 seconds
plus 7.486 and 0.343 seconds loading at 2 GiB. Production code did not change
for this fixture correction.

The corrected paired source composition passed one test/202 assertions in
915.956 seconds execution plus 8.315 seconds loading at 2 GiB. It constructed
two fresh positive products, executed 36 guest cases, analyzed two C8 body
graphs and tracked six public-emitter fresh C7 verification entries: four
positive standalone/assembly calls and two coherent descriptor forgeries.
Seven actual rejected `.gravity` fixtures checked arity, mixed/nonintegral/
unknown types, invalid operands after Never, lexical operator calls and
escaped operator values; their `.qst` counterparts have focused pair parity,
not separate actual construction. Each positive retains ten functions, nine
calls and eight numeric descriptors (six normally invoked, two not invoked).
It covers all four operations, signed/unsigned boundaries, exact Integer
literals, lexical core-name shadowing, a computed guard and one/both Never
operands under typed catches. The runtime remained unchanged. The failed
first composition remains failed; no unchanged expensive matrix was replayed.

Source fixtures declare `:memory/allocate`; empty direct numeric effects do
not claim wrapper purity or close constructor allocation inference. Full
numeric typing, standard-library, profile/provider legality, target support,
authoritative migration, self-hosting and release remain outstanding.

### Collection-constructor allocation: development increment

At `02c1077c`, the body-effect pass's `body-node-record` derives a direct effect
for a normally transferring throw, but no allocation effect for a collection
literal. The development runtime constructs maps and vectors when evaluating
their instructions; quoted DATA instead returns its retained value. An empty
inferred collection effect set therefore does not establish allocation-free
behavior. L6 already registers `:memory/allocate`, and STD3 requires visible
allocation behavior for constructors and updates.

The effect owner now derives conditional `:memory/allocate` for evaluated
vector/map/set constructors. A Never operand suppresses only the constructor's
own allocation; operand effects and errors remain. Quoted DATA stays inert,
function bodies stay latent, and calls propagate their body effects. A static
allocation record identifies normal operand completion and constructor-input
extent in map entries or vector/set elements, not observed heap bytes or the
runtime cardinality of a set after deduplication. Existing graph
equation checking and whole-result reconstruction bind that record. Constructor
kind/count checks and the logical work budget remain enforced.

The focused batch passed ten tests/214 assertions in 0.564 seconds execution
plus 9.092 seconds namespace loading at 2 GiB. The first batch remains failed
(199 passed, one failed assertion and one error): its fixture check read an
existing duplicate-key quoted DATA form through the host reader, and its
normalized function-allowance case lacked the owning function record. Only
test setup changed: inspect the namespace header for allowance parity and
include the function record. The production rule was not relaxed.

The narrow accepted/rejected paired source composition passed one test/93
assertions in 356.126 seconds execution plus 7.655 seconds namespace loading
at 2 GiB. It reused four fresh source/type products: the accepted body-effects
pair and the undeclared-constructor pair. Each positive graph records two
maps, one vector and one set; `choose` and `assemble` infer allocation while
the other four functions remain empty and namespace definition effects remain
empty. Both undeclared constructors pass C7 and reject at C8 with
`C8-UNDECLARED` / `:function-effect-outside-source-allowance` for
`:memory/allocate`. Ten whole-result verifications cover two accepted results,
six coherent erased/forged graph rejections and two valid rejected results.

This closes the finite constructor inference/source-declaration boundary. It
uses the existing effect owner, declaration check and fresh whole-result
verification without rebuilding the unchanged public C8 module bundle.
Existing fixture declarations now acknowledge constructor allocation; their
prior identities and results remain historical, not relabeled current
evidence. C7, emitter and runtime code are unchanged. Persistent association
can follow this correction. Full allocation-strategy, capability, ownership
and deployment/provider acceptance remain separate outstanding gates; a
source allowance is not an allocator grant.

### Persistent map association: development increment

The current-source candidate implements the bounded STD3 operation
`(assoc established-map :literal-key value)` across the C7 engine, effect
owner, typed emitter and stage-owned runtime. It adds a missing Keyword key or
replaces its value without mutating the input. Established Bool parameters,
String results and nested map values are not restricted to literal values.
Derived association ancestry composes with immutable aliases, local returns,
later association, Keyword lookup and supported structural matching. The
descriptor records a derived map, not a fabricated literal-constructor origin.
Normal completion allocates; a Never operand suppresses only invocation and
own allocation, not operand effects or legality checks. Evaluation order is
operator, map, key, value, once each until control transfers.

C7 owns the deep type and shape derivation. C8 reconstructs the finite
source/binding/order/completion premises that determine its effects, preserves
operand dependencies and checks graph equations. It does not duplicate C7's
shape solver. The current public C8 report-envelope gate does not itself
execute fresh C7 verification; the affected source test explicitly constructs
current C7 products and the public lowerer verifies them freshly. No upstream
authentication upgrade is claimed for C8.

The pre-composition focused batch passed ten tests/579 assertions in 2.135
seconds of execution plus 8.541 seconds of namespace loading at 2 GiB. It covers the new
type/effect/lowering/runtime kernels, paired fixture parity and allocation,
ordering and ordinary-call regressions. Earlier failed batches remain failed:
two synthetic error-evidence setup assertions; one test-load helper-scope
error before assertions; and three producer mutations that changed unused
convenience fields instead of the owning call/edge products. Corrections did
not weaken production semantics. Independent review also caught new C8
count-before-shape guards; the old shape guard now dominates indexing and the
new source-vector guard precedes counting, with controlled-negative checks.
C7 charges association work before its source/caller scan.

The affected source composition completed one test/303 assertions: 299 passed,
four failed and none errored. It remains a failed aggregate. Execution took
934.952 seconds plus 7.968 seconds of namespace loading at 2 GiB. Both fresh
positive source products completed public lowering, 30 guest invocations and
C8 checking. Thirteen functions and fourteen calls retain ten association
descriptors: eight normal and two not invoked. Seven public operations freshly
invoke C7: four positive lowering checks and three descriptor-evidence mutation
rejections. Two positive whole-C8 verifications and three coherently resealed
effect-graph rejections completed, as did all seven rejected `.gravity` sources.
Their `.qst` counterparts are byte-identical fixture checks, not separately
executed negative source coverage.

All four failed assertions were two incorrect effect expectations repeated for
the two source extensions. Typed catches clear escaping error types, but
`:inferred` retains the performed `:error/throw` label while handler/profile/
capability discharge is pending. The correct inferred sets are
`#{:memory/allocate :error/throw}` for `caught-value` and `#{:error/throw}` for
`caught-input`; empty residual `:throws` is a separate fact. Only those test
expectations were corrected, not production behavior. The expensive source
composition was not replayed to obtain a green aggregate.

A separate test-only follow-up passed three tests/99 assertions in 1.479
seconds of execution plus 7.738 seconds of namespace loading at 2 GiB. It adds
seven current-C8 checks of conservative performed effects versus covered and
escaping typed errors, and reruns the association effect kernel and fixture
parity. Production and fixture bytes are unchanged. This repairs the oracle;
it does not relabel the original 303-assertion aggregate as passed.

The public mutation tests prove genuine C7 rejects before lowering. Although
repeated descriptor aliases are changed consistently, the altered input/value
shapes do not regenerate every finite transition/result projection. These are
not isolated proofs of deep-ancestry authentication independent of finite
consistency checks. Negative source tests establish rejection and owning rule
families, not a precise per-fixture diagnostic-reason oracle for every earlier
inference path.

The direct commands are in `docs/development-verification.md`.
Dynamic/general keys, variadic association,
unestablished map parameters, transients, complete collection-library support,
full allocator/profile/provider/ownership legality, target completion,
authoritative migration, self-hosting and seedless release remain outstanding.

### Collection count: development increment

The current-source implementation adds `(count collection)` for established
map and vector types through the same C7 engine, effect owner, typed emitter
and stage-owned runtime. Normal completion returns Integer; a Never operand
does not invoke the query. The operand is evaluated once and the runtime
counts its actual top-level members. The query adds no collection-allocation
effect, while operand allocation, errors and control transfer remain visible.

This operation consumes the established collection type, not the narrower
structural-pattern shape domain. Legal quoted maps with String keys or nested
values outside that domain remain countable. There is no recursive shape
proof, static-cardinality field, implicit I64 conversion or new generic Map
annotation. Existing source/binding/call/edge/type joins remain required.
C8 checks its finite effect premises rather than duplicating C7 inference;
the public lowerer retains fresh C7 verification at its existing boundary.

The pre-composition focused batch passed eleven tests/798 assertions in 2.522
seconds of execution plus 8.029 seconds of namespace loading at 2 GiB. A later
three-site correction guards source child-vector shape before counting it in
C7, C8 and lowering; its affected checks passed three tests/289 assertions in
1.398 seconds plus 8.169 seconds of loading. These overlapping batches are
not combined into a new coverage total. A separate paired-fixture/header
check passed one test/20 assertions; it does not parse or execute the source.
Two earlier test-reader delimiter failures occurred before assertions and
remain failed attempts, not semantic results. An earlier producer-only batch
exercised the superseded deep-shape draft and is not final-candidate evidence.

The single affected source composition passed one test/400 assertions with no
failures or errors. Selected execution took 961.418 seconds plus 7.919 seconds
of namespace loading at 2 GiB. Both fresh positive source products completed
public lowering, 38 guest invocations and C8 checking. Fourteen functions and
seventeen calls retain eleven count descriptors: ten normal and one not
invoked. Seven public operations freshly invoke C7: four positive lowering
checks and three descriptor-evidence mutation rejections. Two positive
whole-C8 verifications and three coherently resealed graph rejections passed.

One mutation rebuilds the existing association descriptor's finite projection
after changing its input-body source reference. The private finite validator
accepts that projection, then the genuine public C7 verifier rejects the stale
ancestry before lowering. This isolates more than P's earlier finite-consistency
mutants; it is not a comprehensive verifier-correctness proof.

All nine rejected `.gravity` sources passed their exact-reason checks: arity,
nil, String, scalar, unknown, set, list, wrong result annotation and escaping
the core binding. Their `.qst` counterparts have byte-identical fixture checks,
not separately executed negative source coverage. Quoted duplicate-key DATA
counts its materialized map, and quoted String/compound-key maps remain
countable without being structural patterns. Caught operand errors retain
performed allocation/throw effects while their escaping typed errors clear.
No production, test or fixture repair occurred during the composition; all
27 source-path hashes remained unchanged. Independent review accepted the
scoped implementation and affected integration. Direct edit-loop and
composition commands are in `docs/development-verification.md`.

Strings, nil conventions, sets/lists, unknown operands, additional collection
families, general annotations, full profile/provider/ownership legality,
authoritative migration, target support, self-hosting and seedless release
remain outstanding. Focused implementation feedback does not admit a new
compiler revision or refresh historical authority evidence.

### Proven vector indexing: development increment

The current-source implementation adds two-argument `(nth vector index)` through the
existing C7 engine, effect owner, typed emitter and stage-owned runtime.
Normal selection requires an established positional vector type and an Integer
source literal within its extent. The selected type is the actual member type,
including heterogeneous vectors and Nil members. A Nil value is not missing
type evidence or an out-of-bounds fallback. Never operands retain control
transfer without an executed access, selected value or bounds proof; other
non-Never operands still require their supported type and index facts.

Ordinary scalar selection does not require every member to be a structural
pattern. A later lookup or pattern consumer projects only the selected deeper
ancestry, including nested evaluated and quoted vector selections. Evaluated
vectors still check all immediate child source, order, extent and type joins.
Quoted selection consumes the existing validated quoted DATA before requesting
the terminal selected shape. Immutable aliases and local returns preserve the
existing binding and call lineage. Lookup-produced vectors retain the earlier
lookup owner's shape boundary; this is not general projection for every
collection producer.

Operands execute once, left to right, until transfer. Selection preserves the
actual member identity and adds no collection allocation; operand allocation
and errors remain visible. The finite bounds record does not authorize check
elision: the runtime retains vector, integer and range guards before access.
Malformed development IR rejection is not a source dynamic-bounds failure API.

The final focused projection/type/fixture batch passed seven tests/234
assertions in 1.091 seconds of execution, plus 8.677 seconds of namespace
loading, at 2 GiB. It includes selected and unselected source/order/type
mutations, nested evaluated and quoted selections past unrelated String-key
maps, and paired-fixture parsing. Two affected return-shape and lookup
regressions passed 84 assertions in 1.980 seconds plus 7.747 seconds of loading.
Earlier overlapping focused batches are not summed into unique coverage.

Earlier producer/projection delimiter errors and unsupported internal
`dissoc` failed before semantic assertions. The latter was replaced with the
already supported private-context update, not a seed extension. A combined
focused batch also retained two failed test expectations: when a mocked index
has Never type, its unused literal value is not C8 evidence. Named controls
identified those cases; the test-only correction preserves normal-index and
source-join rejection. Those failed aggregates remain failed history.

The single affected source composition passed one test/630 assertions with no
failures or errors. Selected execution took 1,262.029 seconds plus 8.129 seconds
of namespace loading at 2 GiB. Both positive source extensions completed
typing, lowering, execution and effect checks: 18 functions and 19 calls retain
17 nth descriptors (14 normal, three not invoked) and two local calls.
All 36 guest invocations passed, including first/last and heterogeneous members,
Nil, immutable aliases, local returns, nested evaluated/quoted selection,
pattern consumption, caught operand errors, shadowing and the entrypoint.

Seven public operations freshly invoke C7: four positive standalone/assembled
lowering checks and three coherently altered descriptor/ancestry rejections.
The stale actual callee-body-source mutation still satisfies the shallow nth
validator before genuine public C7 rejects it. This is a specific ancestry
control, not a comprehensive verifier-correctness proof. Two accepted whole-C8
verifications and three coherently resealed effect-graph rejections passed.
All eleven negative `.gravity` fixtures passed their exact rule/reason checks;
the `.qst` negatives have byte-identical parity checks, not separately executed
negative-source coverage. No compiler, test or fixture edits occurred during
the run; all 31 source-path hashes remained unchanged.

The direct kernels and separate composition command are documented in
`docs/development-verification.md`. Dynamic index failure semantics, general
collection types, profile/provider/ownership legality, authoritative migration,
target support, self-hosting and seedless release remain outstanding.

### Bounded counter successor: development increment

The current-source C7 engine, effect owner, typed emitter and stage-owned
runtime implement a finite proof-required Integer `inc`. The supported loop
starts its counter at zero, compares it strictly against runtime `count` of
the same established immutable vector, and advances that same slot by one on
every incoming backedge. The original loop target, slot, initializer, guard,
count operand, vector origin and complete transfer census are checked. The
existing vector child limit bounds its extent to 1024.

The invariant `0 <= i <= N` and dominating true-edge fact `i < N` establish
`0 < i + 1 <= N`. An empty vector has an unsatisfiable increment edge and a
normal zero-iteration exit; it does not produce a fictitious executed increment
or Never result. Immutable vector aliases and multiple coherent backedges are
covered. The result remains Integer, with a separate numeric-mode/range record,
not an implicit I64 or arbitrary-precision arithmetic choice.

The successor adds no allocation or error effect, but preserves operand effects
and control transfer. Count remains at its original evaluation site. No
folding, hoisting, check elision or general termination claim follows. The
runtime retains integer, current-slot and range checks before addition.
Defensive rejection of malformed development IR is not a source overflow API.

The combined focused batch passed seven tests/301 assertions in 2.067 seconds
of selected execution plus 8.157 seconds of namespace loading at 2 GiB. Its
fixture parsing preceded removal of unsupported Integer result annotations;
the corrected paired fixtures subsequently passed 22 parsing assertions.
The final strict-guard/fixture batch passed two tests/29 assertions, and four
affected loop/control regressions passed 186 assertions in 2.137 seconds plus
9.061 seconds of loading. Overlapping batches are not summed as unique coverage.

Earlier focused failures remain recorded: unsupported private `vals` and
`merge` calls were replaced with existing bounded operations, delimiter errors
were corrected, normalized vector-origin fixture joins were completed, and
budget exhaustion now retains its owning diagnostic. No seed extension was
introduced. Scalar, nil, map and string path controls motivated guarded path
consumers rather than host exceptions; the finite consumers do not replace
fresh public C7 reconstruction of the complete candidate.

The first affected source-composition attempt stopped before source processing:
the selector resolved the SH08 test helper before requiring its namespace.
Its result remains one test, zero passes and one setup error, not semantic
validation. After a one-line test-only dependency correction, the cold
namespace/fixture check passed one test/22 assertions. It did not execute the
composition body containing the new require; that dependency path received
targeted static review.

The changed selector's composition completed one test/280 assertions with
278 passes, two failures and no errors. Selected execution took 1,052.035
seconds plus 9.456 seconds of namespace loading at 2 GiB. Both failures were
the paired call-census expectation: ordinary local call facts omit `:dispatch`;
that label exists only on a different higher-order branch. The intrinsic counts
matched, but the expected local dispatch tag did not. The pre-run review missed
this schema distinction too. The failed aggregate remains failed history; no
production change is justified by this test-oracle error.

Both positive extensions completed typing, lowering and execution: four
functions, eleven calls (four successors, three counts, three comparisons and
one local call), three loop bindings, seven recur targets and four transfers.
All twelve guest invocations passed with results zero, one and three, including
the immutable alias and both backedges. Four positive public C7 operations and
three coherently altered candidate rejections passed. The unused source-echo
mutation still satisfies the finite successor predicate before fresh public C7
rejects the full candidate; this is not an isolated callee-body ancestry test
or a comprehensive verifier-correctness proof.

Two accepted whole-C8 verifications and three coherently resealed graph
rejections passed. All ten negative `.gravity` fixtures passed exact rule/reason
checks; paired `.qst` negatives have byte parity, not separately executed
negative-source coverage. The mode-named source fixture lacks the required
range proof; it is not an explicit wrapping-mode syntax test. Descriptor and
development-IR mutations separately cover unsupported represented modes.
All 29 compiler, test and fixture hashes remained unchanged during the run.
The subsequent test-only correction expects the existing nil dispatch and
explicitly joins the local call's node, caller and callee to its canonical
edge/function table. The existing direct-call lowering and paired-fixture
checks passed two tests/78 assertions in 0.629 seconds plus 8.424 seconds of
loading. Those checks do not execute the corrected composition body or its new
local-join assertions. The expensive source composition was not replayed solely
to repair this oracle; no result for the corrected aggregate is claimed.

The direct kernels and separate composition command are documented in
`docs/development-verification.md`. General arithmetic, arbitrary starts and
steps, unproved extents, fixed-width checked errors, BigInt, profile/provider
legality, authoritative migration, target support, self-hosting and seedless
release remain outstanding. This finite domain is not a language-level ban on
other documented numeric behavior.

### Guarded loop vector indexing: development increment

The current C7 engine, effect owner and typed emitter extend established
vector `nth` from literal indices to the original Integer slot of the accepted
bounded counter loop. The vector is nonempty with one established homogeneous
element type. Access must occur beneath the strict count guard's true edge,
using the same immutable vector origin and loop slot. Immutable aliases are
allowed; another vector of the same type and extent is not the same origin.

This rule consumes the existing counter invariant rather than running a
second range solver. Access requires `0 <= i < N`; the successor's weaker
upper bound `i + 1 <= N` does not justify indexing with `(inc i)`. The call
retains its original operator/vector/index evaluation order, returns the
established homogeneous element type, and adds no allocation or error effect.
Operand effects and non-returning control transfer remain visible. The
existing runtime and its vector, integer and bounds guards are unchanged;
no check elision or general termination claim follows.

The cold inference kernel begins without completed successor or nth
descriptors, covers both recur/throw branch orders and preserves current facts
over prior-round facts. Separate origin/scope tests retain a valid counter
invariant while rejecting a different same-extent vector and a nested target.
Deep selected-shape reconstruction is explicitly unsupported for this variable
index; ordinary element typing does not grant constructor-coordinate ancestry.
The existing literal-index and Never-operand rules remain separate.

Focused feedback includes a two-test/77-assertion producer/cold-inference pass,
a separate one-test/21-assertion origin/scope pass, and the corrected effect
kernel's one-test/75-assertion pass. An eight-test/330-assertion regression
batch covers the producer, cold inference, origin/scope, deep-shape fence,
effects, literal nth and bounded-successor lowering, and paired fixture
parsing. A final two-test/98-assertion batch covers changed lowering and
fixture parsing. These overlapping batches are not summed as unique coverage.

Earlier failures remain history: source/test delimiter errors stopped loading;
an effect fixture omitted the existing escaping-error set required by the
record contract; one command used the wrong working directory and stopped
before tests. Lowering's initial 78-assertion run had three real failures:
forged contextual callee edges were accepted even though its core-derived
proof was valid. Lowering now compares contextual call and edge records with
the descriptor's authenticated source records. The changed kernel passed all
78 assertions; the earlier 75-pass/3-fail aggregate remains failed.

The paired source fixtures contain a Bool-vector scan through an immutable
alias, two unit-step backedges, a String throw, catch and normal entrypoint.
Nine rejected fixture pairs cover different origin, wrong slot, derived index,
heterogeneous or empty element typing, nested target, outside-guard access,
false-edge access and an unknown index. Parsing establishes only syntax and
pair parity.

The single frozen source composition completed one test/166 assertions:
150 passes, 16 failures and no errors. Selected execution took 1,001.585 seconds
plus 8.006 seconds of namespace loading at 2 GiB. All 28 compiler, runtime,
test and fixture hashes remained unchanged. The aggregate remains failed.
Both positive extensions completed typing, lowering and guest execution:
three functions, seven calls, one loop, four recur targets, two transfers and
five builtin instructions. Ten guest invocations passed, including two actual
String throw identities per extension, catch recovery to 99 and normal scans
to three. Seven fresh public C7 operations covered four positive operations
and three altered candidates. The unused source-value echo hostile passes the
finite helper but fails full public reconstruction; it is not a comprehensive
verifier-correctness or isolated selected-ancestry proof. The nine negative
`.gravity` fixtures rejected within their explicit allowed diagnostic pairs;
the run does not identify which overlapping pair matched each case. The `.qst`
negative fixtures have byte parity, not separately executed rejection coverage.

Each positive extension had one C8 rejection followed by seven missing-graph
assertion failures. C8's throwing-call verifier excludes loop/recur from its
scoped operation whitelist, although the preceding derivation already checks
their original transfers, effect fixed point and final graph equations. This
is a sufficient static rejection condition; the shared diagnostic reason
alone does not uniquely identify the observed internal callsite. C8's verifier
accepting a well-formed rejected result, and hostiles rejected from an absent
graph, do not establish accepted effect graphs or successful graph-forgery
coverage. The narrow C8 compatibility correction adds only checked loop/recur
to that whitelist, preserving the existing joins and equations. The extended
local throwing-call kernel exercises module-node derivation and the actual
throwing-call verifier, including a called loop's String throw, catch residual,
performed throw label, exact/one-less work bounds and forged targets,
transfers, rows and summaries. Its corrected revision passed one test/44
assertions; the six-test/388-assertion affected regression batch also passed.
The batches overlap. Earlier mock identifier formatting, missing body scope,
and dependent missing-graph/work-bound failures remain recorded; they did not
justify further production changes.

A single-source C8 follow-up against the unchanged `.gravity` positive passed
one test/24 assertions in 114.787 seconds plus 6.935 seconds of namespace
loading at 2 GiB. All 29 compiler, runtime, test and fixture hashes remained
unchanged. It requires an accepted nonempty graph before whole-result
verification and the three coherent graph hostiles. The accepted graph retains
the two-transfer loop fixed point, pure nth dependencies without an allocation,
the scan's escaping String error and the catch's empty residual while keeping
the performed allocation/throw labels. The unchanged paired
source/type/lowering/runtime/public-C7 and negative-source matrix was not
replayed to make the failed aggregate green. This successful affected consumer
result complements, but does not relabel, the original failed run.

Use the focused commands in `docs/development-verification.md`. Empty vectors
without an element type, heterogeneous joins, arbitrary dynamic indices,
deep selected-shape propagation and dynamic bounds-failure APIs remain separate
work, not language bans. Profile/provider/ownership legality, authoritative
migration, target support, self-hosting and seedless release remain outstanding.

### Root Keyword match typing: development implementation

The existing C7 and typed-emitter owners now extend scalar matching to an
established Keyword scrutinee. Parameters, validated immutable aliases and
ordinary local-call results retain their current type. Literal clauses remove
values from an open domain; a fallback binder and its immutable aliases keep
those exclusions. Known literal and quoted domains stay exact in the focused
shape checks. Wildcards consume the remaining domain without a binding.
Open partial Keyword matches reject with an implementation annotation
requirement, not a language-wide totality rule. A known literal caller does not
specialize away a declared Keyword parameter's open domain.

The cold inference check starts without type/proof tables: existing loop and
guarded-index facts establish the Keyword result, and binder seeding preserves
it. A normally returning String branch is not treated as Never. The private
root-scalar demand does not establish a type or authenticate a whole callee;
it avoids demanding deep constructor ancestry for an already established root
scalar. Existing literal-index projection and variable-index deep-shape fences
remain intact. Lowering joins the current Keyword type and existing ordered
source, clause, scope and extraction records, then emits the existing
`structural-match` instruction. Runtime and C8 production code are unchanged.

The combined focused batch passed 13 tests/332 assertions; a separate final
lowering/fixture batch passed two tests/37 assertions. These overlap. The
fallback test initially had a load-time delimiter error, then passed its 15
assertions after correction. Paired fixture parsing is not execution evidence.

The one frozen source composition completed 244 assertions: 243 passed, one
failed and none errored. Test-var execution took 1,044.249 seconds plus 8.248
seconds of namespace loading at 2 GiB; all 31 observed input hashes remained
unchanged. The aggregate remains failed. Both positive extensions passed seven
function types, eight call facts, five Keyword matches and eleven clause rows.
Thirty-four guest outcomes cover parameter dispatch, local-call return typing,
ordinary if truthiness, exact literal aliases, function recur, guarded vector
scans, unmatched Keyword throws, catch recovery and normal entrypoints.
Seven fresh public C7 operations cover four positives and three altered
candidates. A stale source echo remains finite-helper-valid but fails public
reconstruction; fictional exclusions and changed extraction types also fail.
Actual extraction guards and accepted/nonempty effect graphs make those
checks nonvacuous. Both C8 whole-result verifications and three coherently
resealed graph hostiles passed.

All ten negative `.gravity` fixtures rejected. The single failure omitted the
observed outside-guard diagnostic from its allowed set: the unestablished
binder's throw payload is unknown, so the `C7-ANNOTATION` diagnostic
`:typed-throw-payload-type-required` precedes later match/index diagnostics.
The test-only expectation now includes it; no compiler change or expensive
unchanged replay is warranted. The existing direct throw kernel now checks the
exact unknown-payload rule and reason and passed 23 assertions. The combined
post-correction batch passed three tests/60 assertions, including unchanged
lowering and fixture checks. Only two test files changed after the original
composition; these checks do not turn its failed aggregate into a pass.
Other allowed diagnostic memberships are not
uniquely observed diagnostic traces. Negative `.qst` coverage is pair parity
only. The source escape fixture rejects first-class `nth`; match-binder escape
has normalized ownership checks, not an authentic source rejection claim.

Use the owning commands in `docs/development-verification.md`. Open partial
Keyword matching, additional scalar families, union joins and deep
selected-shape propagation remain separate implementation work. Quoted
Keyword domain propagation has focused coverage, but quoted-match source
composition is not exercised by this fixture. This is scoped executable
development support, not authoritative migration, profile/provider or
ownership admission, target support, self-hosting or seedless release.

### Persistent vector append: development implementation

The existing C7, C8, typed-emitter and stage-owned runtime owners implement
fixed-arity `(conj vector value)` under STD3. Established ordered element types
are extended by one value type, preserving the original vector and prefix.
Operands evaluate once in order. Never prevents the append's own allocation,
but does not excuse invalid source or operand types. Normal results retain the
1024-element bound. Count and literal-index projection consume the resulting
type; projection preserves append/source ancestry rather than inventing a
constructor. The guide gives the direct development and composition commands.

The focused affected batch passed 23 tests/1,180 assertions. Independent review
found a real C8 cumulative-accounting defect: append work was deducted locally
but omitted from the returned total. The fix retains that work through module
and producer accumulation; exact-budget and one-unit-short controls pass.
Projection controls cover prefix, appended, chained and nested coordinates.
These normalized checks are not authoritative source admission.

The single frozen source composition ended with 299 passing assertions and one
error, not a passing aggregate. Test-var execution took 980.465 seconds after
7.905 seconds of loading at 2 GiB. Both positive extensions passed seven
function types, eighteen call facts, twenty guest outcomes in total, eight
fresh C7 operations, two accepted/nonempty C8 whole-result checks and four
resealed graph hostiles. Seven negative `.gravity` fixtures rejected before
the error. Negative `.qst` coverage is paired parity only.

The oversized source fixture reached C6's fragment bound first:
`C6-VERIFY/:fragment-root-form-bound`, maximum 1024 forms, observed 1036.
This is an authentic upstream rejection, not a C7 append-extent rejection.
The 1024-to-1025 C7 boundary has normalized kernel coverage. The subsequent
Never/wrong-family and missing-allocation-allowance cases were initially unrun.
A separate two-source check passed all six assertions in 159.817 seconds after
7.882 seconds of loading at 2 GiB: the former rejects at C7 despite the other
operand's Never type; the latter accepts C7 and its verifier but rejects at C8
with the function-first undeclared-allocation diagnostic.

The test-only extent handler now recognizes exactly the original fail-closed
C6 bound and rethrows unexpected diagnostics. Eight synthetic handler controls
plus fixture and lowering checks passed two tests/84 assertions. These overlap
earlier focused checks and do not convert the original failed aggregate into
a pass. No positive composition or oversized source was replayed solely to
refresh an aggregate label. Only the test file changed after that run; compiler
and paired fixture bytes were unchanged for the separate remaining checks.

Whole-shape consumers retain the one-established-vector-shape fence. Growing
vector recur types and appended-vector iteration origins remain separate
implementation frontiers, not language bans. Collection/provider/profile and
ownership completion, target support, authoritative migration, self-hosting
and seedless release remain outstanding.

### Atomic value equality: development implementation

The existing C7, C8, typed-emitter and stage-owned runtime owners implement
direct binary `=` for independently established Nil, Bool, Character, String,
Symbol and Keyword operands under STD2/STD4. Same-family values compare by
value; different supported families compare false. String comparison preserves
the exact scalar sequence without normalization, case folding or locale rules.
Symbol and Keyword comparisons retain their complete namespace and name.
Normal results are Bool. Operands evaluate once in order; Never transfers
without invoking equality or excusing the other operand's legality. Equality
adds no allocation, capability or error effect; operand effects remain visible.

Supplementary Characters retain their original C2 occurrence kind through
C3/SH04 and C6 projection, quoted DATA, typing and lowering. The existing
four-field representation does not turn an identical ordinary source Map into
a Character. Occurrence-derived kind precedes recursion and identity creation;
the global host-value classifier is unchanged. C2 owns scalar decoding and
codepoint/text consistency. Downstream representation guards do not duplicate
that parser or establish a new authority channel.

The combined focused batch passed 23 tests/1,936 assertions, covering the new
type/effect/lowering/runtime behavior and prior quote, map, vector-append,
integer-comparison and throw regressions. A separate reader/fixture batch
passed two tests/46 assertions: both accepted extensions contain six childless
Character forms, five supplementary and one BMP. The final affected batch
passed four tests/188 assertions. These overlap earlier checks, rather than
forming an additive coverage total. A subsequent comment correction and
deletion of one tautological source-definition assertion required no replay.

The single unchanged-candidate source composition passed all 405 assertions.
Test-var execution took 923.743 seconds after 8.121 seconds of namespace loading
at 2 GiB. Both extensions passed nine function types, fifteen call facts and
thirteen equality applications (eleven normal, two Never). Twenty-eight guest
executions cover distinct equal-content Strings, NUL and supplementary text,
composed/decomposed inequality, atomic and cross-family values, Character
literal/quote/vector projection, ordinary Map preservation, parameter shadowing
and both operand-error orders with retained payload identity.

Nine fresh public C7 lowering operations include five altered-evidence cases.
The Character case changes source-identified quote evidence, not every equal
host-map payload; it establishes rejection of inconsistent Character evidence,
not an isolated codepoint/text theorem. Two accepted, nonempty C8 graphs pass
whole-result verification; four resealed graph mutations reject. All nine
negative `.gravity` fixtures reject arity, numeric/vector/map/function/unknown
operands, result annotation or invalid operands despite Never. Negative `.qst`
coverage is paired-source parity only. All 35 compiler/test/fixture inputs and
the Git base remained unchanged throughout the source run.

After that run, the routine LOC gate found the C6 assembly adapter at 253 lines.
Removing two leading blank lines and one blank separator brought it to 250;
all nonblank lines and their order are unchanged. The source result remains
attributed to the original pre-format bytes, not a relabelled proof of new
bytes. Four post-format Character/map checks passed all 314 assertions, and
independent review carries acceptance across this whitespace-only delta. No
expensive source composition was replayed for it.

Review also found two private-IR rejection-path defects: comparing an unknown
value before classifying it could invoke opaque equality, and building a key
set before checking key families could invoke opaque hashing. Existing
primitives now reject those payloads without callbacks; focused sentinel
controls pass. No source host-object support or seed primitive was added.

Use the owning commands in `docs/development-verification.md`. Numeric,
collection and function equality typing, variadic or first-class `=`, complete
standard-library/provider/profile support and other targets remain separate
work, not language prohibitions. Ordinary integer runtime compatibility is
preserved without claiming new numeric typing. Authoritative migration,
self-hosting and seedless release remain outstanding.

### Persistent List prepend: development implementation

STD3 now specifies binary List `conj` as prepend, with ordinary left-to-right
argument evaluation, original-value preservation and explicit allocation and
storage/lifetime obligations. It does not mandate universal physical sharing
or extend variadic/arbitrary-sequence behavior.

The C7 focused batch passed two tests / 271 assertions: 196 new List-prepend
checks and 75 existing vector-append regressions. They cover homogeneous,
positional and bare List result types, heterogeneous/Nil positional values,
Never, rejected member/source/binding mutations, computed-result depth/width,
exact and one-less work, malformed argument containers and large arity.
Early shape/budget checks now precede argument-type collection. The first two
batches stopped at syntax errors in the new test; a third reached an
unsupported `dissoc` in the new Gravity helper, with zero passing assertions
and two errors. The helper now carries its result type directly, avoiding the
unnecessary temporary field without expanding the seed builtin surface.
Those failures remain separate from the passing batch.

C8 reconstruction/allocation and typed lowering/runtime component checks now
pass. The stable combined focused batch passed 72 tests / 5,351 assertions,
including the existing traversal and vector-append regressions, malformed
children, exact/one-less budgets and paired fixture shape checks. Counts overlap
the earlier component batches; they are not additional independent coverage.
Earlier delimiter errors and a C8 call-join dispatch failure remain recorded as
failed attempts, not passing evidence.

The single affected source composition passed 1 test / 245 assertions at the
unchanged 2 GiB heap. Paired Gravity/QST sources exercise eight functions and
24 calls, four prepend descriptors (three normally invoked and one aborted),
32 guest execution cases, ordered reverse-and-join, old-input preservation and
original throw identity. It checks fresh public C7 lowering gates, independently
reconstructed C8 graphs and allocation facts, exact/one-less C8 work, hostile
mutations, eight C7 rejection families and the missing allocation allowance.
The selected test took 1,026.331 seconds after 8.643 seconds of namespace loading;
these timings are development observations, not benchmarks or speedup evidence.

Source homogeneous-member coverage is String; other supported atomic types
and finite/bare results have focused kernel coverage. Bare-List head knowledge,
homogeneous-to-bare compatibility and compound return annotations remain
separate frontiers. Profile-specific representation, provider and ownership
legality, other targets, authoritative migration, self-hosting and release
gates remain outstanding. This is accepted executable development behavior,
not completion of those broader contracts.

### Module-local literal initialization: development implementation

The typed emitter and `gravity.runtime` now execute supported module-local
literal definitions once in source root order before the selected entrypoint.
Each closed-plan execution creates a fresh module instance. Namespace reads
use its private binding-ID pool through calls and `recur`, independently of
lexical shadowing. Direct expression/function evaluation does not initialize
a module or acquire a pool from caller-supplied locals or function tables.
Plans without initialization retain their existing startup behavior.

The emitter joins the complete canonical root/definition census, including
unused initializers, with C5 bindings, source identities, types and C6
evaluation metadata. Function ordinals are not module-root ordinals. Unquoted
Vector and supported Map initializers remain evaluated constructors; quoted
DATA stays inert. C8 owns namespace initialization allocation and legality;
reads add no allocation. Runtime manifest checks do not replace source/type
verification, and reported constructor occurrences are validation data, not
allocation-event measurements.

The focused runtime/catch batch passed 2 tests / 142 assertions. The corrected
emitter kernel passed 1 / 55, including loader assertions that overlap other
checks. Its first attempt remains 37 passes / one failed residual-budget
assertion; the corrected check compares semantic products and explicitly
checks the remaining budget. Independent review also corrected scoped C6
branch-region handling and reserved known summary work before child lowering.

The sole source composition passed 1 test / 126 assertions (120 composition
checks plus six loader checks) at 2 GiB, taking 370.889 seconds of selected
execution after 9.561 seconds of namespace loading.
It preserves W5's exact literal definition and getter in the accepted G/Q
pair, verifies C8 before six closed executions, and rejects an unused call
initializer at the emitter and a missing allocation allowance at C8. It builds
four fresh source products and reuses accepted products for coherent type/effect
mutations. Negative
QST fixtures have byte-parity coverage, not additional source constructions.

Initializer calls, references/aliases, mutation, imported initialization and
other arbitrary initializer code remain unsupported. Full W5's current empty
effect declaration, Vector value equality, profile/provider/ownership and
other target gates, authoritative migration, self-hosting and release remain
outstanding. This is executable development progress, not completion credit
for those broader boundaries or a performance benchmark.

### Private call-entry Map presence: development implementation

This increment derives literal Keyword presence for an immutable parameter of
a closed private function. It preserves W5's unannotated
`w5-se-span-without-path` helper rather than adding a structural annotation.
Each normally invoking caller must independently establish the same key and
selected scalar type, retaining its own argument source, shape and coordinate.
Matching member-type vectors do not establish keys or a common deep shape.
At least one invoking entry is required; an all-aborting or unused function
cannot establish presence vacuously.

The existing C7 owner checks the complete binding-use, resolution, operator,
call and edge census, actual private visibility and absent export, fixed arity,
escape/mutation exclusions and parameter re-entry. C8 and the typed emitter
reconstruct the finite entry premises against genuine retained upstream shapes;
public C7 owns coherent deep-source verification. Helper cycles and function
parameter recur remain unsupported. A recursive caller can still supply an
independently proved literal argument, and nested-loop recur is not function
parameter re-entry. Existing defaulted and finite-constructor Map lookup paths
and runtime instructions are unchanged.

Independent review identified a cold-inference accounting defect before any
source composition: multiplying every module call by unrelated module data
at each of five lookups and each inference round could not fit the unchanged
65,536-unit budget. The correction retains fresh analysis, reserves global
scans first, then charges actual canonical incoming calls and measured
argument, child, binding-slot and component widths before detailed checks.
Failure/fallback retains spent work. No cache, new wrapper, larger budget or
smaller source helper is used. Known symbol text conversion remains bounded
by the existing canonical scalar contract, not a hard local CPU/allocation
precharge. Candidate entry count must match the actual census before gathering
supplied shapes.

The normalized cold regression passed 1 test / 19 assertions: four functions,
eight calls and five gets infer from Unknown skeletons in six rounds, using
46,030 cumulative units. The exact budget reproduces types and returns with
zero units left; one less rejects. Its first attempt remains failed with 13
passes and six failures: the fixture's node renamer also renamed the semantic
`:operator` position tag. Only that fixture field was corrected; the rejected
fixture's reported 27,720 units is not successful convergence evidence.

The accounting-focused batch passed 6 tests / 209 assertions. A later guard
delta passed 3 / 60, adding scalar and oversized candidate-entry rejection to
the C7, C8 and emitter kernels. These overlap earlier component results and
are not an additive green aggregate. They are development feedback, not source
composition, authoritative module migration, full W5 execution, target
completion, self-hosting or release evidence.

The first source composition remains failed: 1 test / six passes / one failure
and one error, after 108.525 seconds of selected-test execution at 2 GiB. The
first accepted-source candidate instead rejected all five lookups in C7 with
`:map-get-entry-families-required`. No accepted C7 source product, public assembly,
C8 result or guest observation completed; the QST and negative products were
not built. Static inspection of the owning C6 constructor and exact schema
identified the defect: canonical resolutions live under
`[:preserved-resolution :resolution-table]`, while the new consumers and their
normalized inputs incorrectly used the flat identity-preimage field. The
correction belongs to those consumers and test inputs, not a C6 schema change
or a fallback authority channel. The corrected-path focused batch passed six
tests / 108 assertions, including rejection of a flat lookalike table and the
same six-round cold inference with exact/one-below budgets.

The corrected-candidate source composition passed 1 test / 279 assertions
(273 composition checks plus six existing runtime-loader checks) in 322.282
seconds of selected execution, with 9.050 seconds of namespace loading at the
unchanged 2 GiB heap. It built the accepted Gravity/QST pair and one real
missing-key Gravity rejection. Four tracked public C7 gates cover the two
assemblies and two coherent entry forgeries, separately from upstream
verification. Six guest entry evaluations check all five returned fields and
path removal. Each accepted case retains five private-entry descriptors and
constraint rows over two genuine incoming spans with different key orders.
Whole C8 results, equation/ledger/graph hostiles, exact/one-below C8 work and
semantic G/Q equality passed. All sixteen selected inputs remained unchanged.

This supports the unchanged helper in the fixture's explicit allocation
context, not W5's complete module or its current effect declaration. The
negative QST file has parity coverage only. Public or escaping inputs,
unproved/unknown keys, general structural inference, recursive parameter
transfers, other target/profile/provider/ownership gates, authoritative
migration, self-hosting and release remain outstanding. These timings are
development observations, not benchmarks or speedup evidence; the failed V1
did less work and is not a comparison baseline.

### Proven-present Map lookup: development implementation

This increment adds `(get map :key)` when independent finite Map-shape facts
establish the literal Keyword's presence and selected stored type. STD3's
present-key rule preserves false/nil values, inert payloads, the original Map
and Map-then-key evaluation. It defines no absent-key result. C7 derives shape
from the existing owning analysis; C8 and lowering check the retained finite
selection and their own source/type/row premises. Existing three-argument
lookup remains unchanged. No new runtime operation or testing lane is added.

Focused checks currently have six distinct passing selectors / 809 assertions:
C7 present lookup 134, C8 present lookup 190, present lowering 154, and existing
defaulted lookup C7 183, C8 68 and lowering 80. The lowering count includes six
unchanged runtime-loader assertions; its five normal field extractions observe
String, Integer, Keyword, false and nil values. These are normalized inputs,
not accepted source modules.

The first batch remains failed with 794 passing assertions and 15 failures:
full inference had not registered two-argument lookup's shape-context demand.
The correction selects only the authenticated two-argument operation in the
existing demand loop. Its first targeted run stopped on a delimiter error
before semantic assertions; the corrected targeted C7 run passed 134. The
other five passing selectors were retained, not rerun for a green aggregate.

The paired-source composition passed 1 test / 416 assertions, including six
unchanged runtime-loader assertions, in 424.148 seconds at a 2 GiB heap. It
constructed exactly the accepted Gravity/QST pair and the existing empty-Map
missing-key rejection. The accepted products cover 15 functions, 15 calls,
12 present-key lookups (eight normal and four non-returning), one unchanged
defaulted lookup and 30 guest executions. Eight caught String identity checks
preserve real Map/key transfers. Local Map returns and immutable aliases retain
their independently derived selection facts; original Maps and quoted payloads
remain unchanged and inert.

Both whole C8 results passed. Exact/one-below work, ledger/effect mutations and
same-type wrong-field selection reject. Public C7 reconstructs deep source
shape; C8 checks selection against genuine retained input shape, not an
independent quote/type solver. The real missing-key source now rejects with
`C7-ANNOTATION` / `:map-get-presence-required`; its QST counterpart was not
separately built. All 15 selected inputs remained unchanged. This is executable
development support, not unknown-member or dynamic-key lookup, an absent-key
policy, whole W5 execution, authoritative migration, target completion,
self-hosting or release evidence.

### Bare Map parameters: development implementation

This slice adds explicitly annotated bare `Map` parameters and family-only
Map removal. L5 now specifies per-parameter forgetting of independently
established member refinements, retaining actual argument types, provenance and
compatibility evidence. This does not establish keys, members or a constructor
shape inside the callee. Exact bare-Map returns use the existing equality rule;
the parameter rule does not introduce a return restriction or change recur.

The existing checked-core, C7, effect and typed-emitter owners implement the
increment; the runtime is unchanged. Family-only removal retains receiver
node/source/type evidence but no input shape, member transition or result
shape. Normal invocation returns bare Map with possible allocation; operand
transfer remains Never and suppresses invocation. Count and membership require
no member facts. Current member-lookup, association and structural paths still
require their own established facts.

Independently reviewed development results are:

| Selected checks | Passing result | Scope |
| --- | --- | --- |
| Signature and argument compatibility | 5 tests / 467 assertions | Map declarations, source joins, mixed argument orders, type/work rejection and existing List regressions. |
| Compatibility consumers | 5 / 347 | Independent C8/emitter row reconstruction, actual types, malformed rows and cumulative budgets. |
| Family removal | C7/C8 2 / 138; lowering 1 / 62 | Normal/Never cases, no invented shape, allocation and source/row/work guards; separate targeted results. |
| Count and recur | C8 30; emitter 28; recur 5; old List call 36 assertions | Bare-Map count, unchanged exact recur compatibility and affected call regression. |
| Complete Map-call lowering | 1 / 58 | Real normalized definition/call checks, exact/one-below limits and successful-prefix/failing-child work carry. |
| Fixture and literal-key checks | 2 / 29 | Parity/ASCII/header census plus normalized computed-key rejection, not negative-source compilation. |

These are distinct component results, not a replayed passing aggregate. Failed
history remains: family 173 passes / 27 failures (three real bare-Map count
failures and 24 stale reference-order fixture failures); count/call/recur
148 / 1 (an early call guard omitted rejected work); targeted call 53 / 4
(inconsistent new two-parameter fixture metadata/scope). Corrections preserve
definition guards and successful IR, admit only bare Map to the count family,
and retain the actual argument-based call charge plus completed/failing work.

The affected source composition passed 1 test / 452 assertions, including six
unchanged runtime-loader assertions, in 518.096 seconds with a 2 GiB heap.
It constructed exactly four fresh inputs: the accepted Gravity/QST pair, a
non-Map argument rejection at C7 and an undeclared-allocation rejection at C8.
The accepted products cover 15 functions, 19 calls and 48 guest executions,
including mixed parameter compatibility, exact bare-Map returns, chained
removal, count, membership and caught operand transfer. Public C7 rejects
erased upstream types; C8 rejects missing compatibility evidence with genuine
types retained. Forged family shapes, ledgers and effect graphs also reject.

The five other rejected fixture pairs retain only their fixture or normalized
coverage, and the two selected rejected QST variants were not separately
compiled. All 28 selected implementation, test, fixture and contract inputs
remained unchanged. This establishes the stated development behavior, not
general member access on bare Map, W5 execution, authoritative migration,
release, target completeness or self-hosting.

### Persistent Map removal: development implementation

This slice implements binary `dissoc` with one literal Keyword key over an
independently established Map shape. STD3 specifies persistent removal and
ordinary operand evaluation without restricting the language to this initial
implementation frontier. The four existing owners are C7, C8, typed lowering
and the Gravity development runtime; no new seed operation or test lane is
needed. Normal present, absent and empty removal must all expose possible
allocation, while operand control transfer prevents invocation.

The pure C7 transition kernel passed 1 test / 124 assertions. It checks
present/absent/empty cases, removed-entry and survivor provenance, dense
derived coordinates, false/nil payload types, chained path-key removal DATA,
malformed shape inputs and exact/one-below logical work budgets. The first
run stopped on an unclosed delimiter before semantic assertions; that error
is retained separately from the corrected result. Static independent review
accepted the transformation under its established-shape precondition, not as
a type validator or public compiler result.

The source/derive kernel passed 1 test / 90 assertions. It joins the accepted
owning Map-shape result to the actual argument type before applying removal;
coherent nil/Unknown member echoes still reject. It checks original forms,
resolved operator/call/edge identity, present/absent/empty allocation and
storage policy, malformed arguments and exact-budget boundaries. Independent
static review accepted these source and work-accounting joins. Earlier test
preparation failures remain separate: a namespace scoping error before checks,
then 83 passing assertions and an empty-Map negative's setup index error. The
corrected setup does not weaken the nonempty malformed-member rejection.

The inference increment passed 1 test / 132 assertions. The descriptor is
wired into intrinsic recognition, ordered constraints, call facts and existing
shape consumers; independent static review accepted these C7 paths. The
normalized kernel checks cold present/absent/empty inference, descriptor,
allocation, storage and result-type mutations, exact/one-below work limits,
two-unit Unknown deferral and non-returning input/key cases. Shape consumers
re-enter the owning derivation rather than trusting a stored transition.
At this kernel checkpoint, fresh public C7 reconstruction, independent
downstream joins and actual runtime/source integration were still outstanding;
the distinct composition result below closes those development checks.

The independent C8 kernel passed 1 test / 121 assertions for normal
present/absent/empty removal, coherent descriptor/row/fact mutations,
allocation/storage graph mutations and exact/one-below context budgets. Its
first run remains 118 passing assertions and three errors: the production
context counted malformed source forms before checking their vector shape.
The corrected guard preserves the same rejection oracles; independent review
closed that finding. The Never node-record control proves only retained
operand effects/errors and suppression of direct allocation, not source-bound
removal validation or accepted whole-C8 noninvoked behavior. Genuine noninvoked
joins and whole-module budgets were deferred to the distinct composition.

Malformed input-work guards subsequently added nine checks each to C7 and C8;
their updated kernels passed 141 and 130 assertions. The combined four-test
batch nevertheless remains FAILED: 446 passed and eleven failed. Six lowering
oracles demanded an unperformed final child unit; three Never-case mutations
were unchanged empty effect vectors; two runtime checks omitted the new helper
from the exact source-function inventory. These were test corrections, not
production semantic fixes. Only the affected two selectors were rerun and
passed 186 assertions (147 lowering, 39 runtime). Failure-work checks now
retain prepaid work, completed children and the failing child's actual report,
not invented work. Runtime tests cover persistent removal, false/nil values,
inert payloads, operand order/transfer and invalid values/instructions. Raw IR
computed keys do not widen C7's literal-key source frontier. Retained focused
coverage is 671 assertions across six selectors, not an additive count over
their earlier revisions or a replayed green aggregate.

Twelve affected regression selectors passed 775 assertions, covering existing
association, quoted/structural shape, Keyword matching, get/contains/count and
lowering/runtime behavior, plus eighteen paired-fixture header checks. Root
source review removed an unsupported `Int` return annotation from the new
count example before any source run; the composition checks independently
inferred Integer instead. The final source selector covers
seventeen functions, twenty-one calls, fifteen removals (thirteen normal, two
not invoked), all affected Map consumers, original-value preservation and
genuine operand transfer through C7, C8 and runtime. It selects three fresh
products and reuses accepted products for guest and hostile cases. The
quoted-Map missing-allocation case isolates removal's own allocation policy;
other negative pairs have no fresh source-acceptance result yet.

Its first actual invocation passed six loader assertions, then errored before
any source product completed. The existing C2 default path attempted persistent
cache publication and hit its unchanged 512 MiB aggregate admission limit
(538,990,473 observed bytes). This establishes no Map-removal semantic result.
The bounded process correction routes the ordinary reader to its existing
fresh authenticated producer, leaving the explicit cache API, validation,
capacity policy and shared store untouched. Its two affected reader tests
passed 42 assertions; four unchanged strict-admission assertions passed
separately. The first focused batch remains 43 passes and three failed test
expectations (cold publication status and interrupt observation timing), not
a production failure or an additional passing aggregate. Independent review
accepted the corrected dispatch, parity and validation results.

The changed-dependency source composition then passed 1 test / 448 assertions
in 421.346 seconds of selected-test execution at a 2 GiB heap. It constructs
exactly three fresh products, makes four tracked public C7 verification calls
(two accepted assemblies and two coherent forgery rejections), and performs
34 guest executions. Genuine input/key control transfer, surviving source
coordinates, whole-C8 allocation and budgets, ordered removal-row census,
typed lowering and original-value preservation pass on the real source pair.
The quoted-Map absent-key case rejects undeclared allocation. All 28 reviewed
inputs stayed unchanged. These are development integration results, not
authoritative module migration or a generalized performance measurement.

Supported behavior is bounded to binary removal with a normal literal Keyword
key and an independently established Map shape, including present, absent and
empty Maps. Normal invocation retains possible allocation; operand transfer
prevents invocation. General Map parameters, arbitrary source keys, full W5
execution, documented-target completion, authoritative migration and
self-hosting remain outstanding. Other negative fixture pairs retain focused
kernel coverage and fixture parity, not fresh source-rejection credit.

### Homogeneous List canonical-if joins: development implementation

This slice supports an independently inferred common List element
type for unequal normal `if` branches, such as `(list part)` and `(list)` when
`part` has established String type. L5 now states the bounded rule: homogeneous
or uniformly typed positional branches can supply the same atomic element
type; an empty positional branch supplies none. Expected return declarations
cannot supply missing evidence. Equal-type and Never rules remain unchanged.

Implementation belongs to the existing C7 type owner, C8 effect checker and
typed emitter. It retains actual branch types and a source-bound
join-specific compatibility premise, not false equality. The shared normal
join used by match, try, loop and recur must not be widened. No runtime
conversion or new instruction is introduced.

The independent type-helper kernel passed 1 test / 817 assertions over the
15 currently supported atomic element types, both branch orders, empty and
homogeneous branches, incompatible or missing member evidence, malformed
shapes, and exact/one-below work budgets. The initial helper attempt stopped
before semantic assertions on unsupported bootstrap `dissoc`; it was replaced
with the existing projection helper, not a seed extension. The normalized
source-premise kernel also passed 40 assertions, including coherent branch
reordering, source/binding mutations and exact-budget checks. Its earlier
delimiter failure and 38-pass/two-failure test-oracle result are retained;
the corrected reorder negative keeps the condition first instead of making
the join ineligible before its source check. The inference kernel and existing
return-branch-independence regression passed 2 tests / 115 assertions (108 new
inference checks). Nominal and normal-return results, the final function return
and compatibility row agree; non-Bool conditions preserve L2 truthiness and a
Never condition prevents a normal result. An earlier 84-pass/four-failure
oracle compared retained caller budget metadata as well as semantic results;
the corrected comparison excludes only that scheduling context while keeping
types, rows, diagnostics and work counts. Independent review then found the
unknown-condition deferral failed to check its one-unit budget. The corrected
inference kernel passed 128 assertions, including zero/negative remaining
budget and cumulative exhaustion. That C7 finding is closed.

The updated source-owner kernel passed 46 assertions after adding namespace,
binding-kind and binding-class negatives. Independent C8 context reconstruction
passed 33 assertions covering ordered row census, missing/duplicate/orphaned
rows, changed types and source/binding premises, and exact/one-below budgets
for both the new join and legacy equal/Never cases. New downstream contexts
also check the cumulative one-unit cost of non-applicable joins.

The lowering kernel passed 142 assertions with branch values matching each
declared finite or homogeneous type, and condition types matching all twenty
Bool/nil/Keyword/String runtime cases. It emits the existing `if` instruction
and preserves the selected List value's identity. Its earlier 122-assertion
result is retained but did not use matching runtime values for every declared
branch family. The failure-work correction passed the updated 186-assertion
kernel: own rejection paths retain known prepaid work, and failures at each
of the three child positions retain earlier completed work. Preflight
reservation counts are not claims of actual source visits or CPU cost.

The affected 14-test regression batch remains FAILED: 824 assertions passed
and 12 failed in the existing guarded-vector cold-inference kernel. The new
deferral path had suppressed the old exact/Never branch seed while a condition
was still unknown, preventing loop inference from converging. The correction
retains only that legacy seed during deferral, not a new homogeneous join or
compatibility row. Targeted inference and vector/Keyword cold-loop checks then
passed 3 tests / 178 assertions (144 new-inference, 20 vector-cold and 14
Keyword-cold); the other thirteen passing selectors were not replayed merely
to replace the failed aggregate with a green one.

Independent component review accepted the three owners and closed the cold
regression. The actual source composition passed 1 test / 290 assertions,
constructing three fresh products: the accepted Gravity/QST pair and one real
bare-List/empty-List rejection lacking independent element evidence. The ten
accepted functions contain six new joins and two legacy equal/Never cases,
local caller flow and a separately checked return annotation. Accepted products
were reused for 42 guest executions, four public C7 verification gates,
independent whole-C8 graph checks, coherent hostile mutations and exact-budget
checks. The selected test took 370.244774041 seconds with a 2 GiB heap; this is
development timing, not a speedup or containment claim.

Independent actual-result review accepted the scoped executable behavior and
confirmed all eleven frozen inputs unchanged. The 290 assertions comprise
284 selector checks and six whole-loader checks. This does not relabel the
earlier failed aggregate. Match/try/loop/recur widening, other element types,
ownership and profile legality beyond this subset, other targets,
authoritative migration, self-hosting and release gates remain outstanding.

### Nil-bearing canonical-if joins: development implementation

L5 permits an independently established nil branch to join with a known atomic
type without changing either runtime value. The current C7, C8 and emitter
increment represents Bool, Character, String, Symbol and Keyword alternatives
as a closed nil-or type. It retains source-bound branch premises through local
inferred returns and calls. A non-nil return declaration cannot erase nil, and
nil-or-Bool does not satisfy L7's Bool-only guard rule. Ordinary `if` remains
truthy except for false and nil. Other control-flow joins are not widened.

Binary equality compares the original values. In particular, nil-or-Keyword
and nil-or-String can both contain nil; their differing non-nil alternatives
are not grounds for folding equality to false. Existing atomic equality and
List-join records retain their previous versions and contents. No runtime
instruction, annotation syntax, reader rule or seed primitive was added.
Independent review caught an unsupported compiler-seed `dissoc` call before
the first execution; premise construction now uses existing primitives.

The first focused batch passed 15 tests / 2,799 assertions: six new kernels
covering 806 assertions and nine affected regressions covering 1,993. It checks
closed type carriers, independent inference, source and binding mutations,
mixed List/nil row census, work limits, lowering and actual runtime equality.
The separate fixture-fidelity kernel passed 1 test / 10 assertions. The paired
positive source preserves W5's exact unannotated `w5-se-canonical-form [mode]`
helper; runtime cases retain all seven known modes plus an unknown mode. The
negative retains a real Keyword caller but declares a non-nil Keyword return
on the same body.

The first source composition attempt failed after its first accepted Gravity
product reached C7: 1 test / 7 passing assertions / 1 error. Public lowering
then rejected the existing whole-context work reservation before assembly;
the QST and negative products, runtime observations and C8 source checks did
not run. The 170.772-second failed attempt is retained. Its 17-function fixture
included eight redundant mode wrappers. The bounded follow-up simplifies those
test callers while preserving the exact W5 helper, inferred nil-bearing local
return, actual equality/error cases and all mode observations. It does not
increase the work limit or change production accounting.

The context reservation is distinct from JVM startup or scheduling overhead:
it charges whole-table cross-products before lowering. A bounded follow-up,
when a concrete larger-module integration requires it, should measure those
families and compare indexed validation with the current exact joins and
rejection behavior. Preserve source/provenance checks and cumulative work
limits; neither raising the ceiling nor redesigning verification-product
representation is part of this semantic increment.

The changed nine-function fixture passed the affected source composition:
1 test / 371 assertions, including six unchanged loader checks, in 386.096
seconds plus 8.815 seconds of namespace loading at a 2 GiB heap. It built the
accepted Gravity/QST pair and one representative negative Gravity product
once each. Accepted products were reused for 36 guest observations, four
tracked public C7 verification operations, whole-C8 reconstruction, exact and
one-below budgets, and coherent join/effect mutations. Upstream construction
also verifies its products; four is not a count of every C7 invocation.

The actual source has 25 calls, eight nil-bearing join rows and inferred
Keyword parameters for both unannotated helpers. W5's helper returns
nil-or-Keyword; the separate helper returns nil-or-String. Their local calls
retain these types through equality, including nil equals nil across those
two families. The declared-Keyword negative rejects with
`C7-TYPE-MISMATCH` / `function-result-type-mismatch`, actual nil-or-Keyword.
Bool, Character and Symbol alternatives have focused-kernel coverage here,
not additional source-composition coverage.
Throw/catch execution and inferred throw effects are retained, without a
claim that all caught effects escape. All 16 recorded inputs remained
unchanged. The earlier failed attempt is not relabeled as passing.

These development checks do not admit a module revision or establish complete
W5, numeric/general union support, other targets, self-hosting or release.

### Qualified core vars and availability: development implementation

This slice closes the C5 qualified-core frontier found before the List
count source run. L3 already permits fully qualified namespaces and STD2 makes
the permitted core surface available; no new language grant is required.
The owning `gravity.resolution` pass selects literal `gravity.core` vars
from supplied core records, preserving their binding identities and metadata.
Alias, source-definition and import route ownership remains prior to fallback;
an owned missing/private member must not silently borrow a core definition.
Type-position lookup remains separate and qualified macros/special forms are
not admitted by the new var route.

Inspection also found missing checks on supplied direct-core availability
metadata. The bounded correction validates the selected core
binding's profile/target fields after existing ambiguity and privacy checks,
with target exclusion before profile exclusion. Malformed fields reject;
only target `:all` is a wildcard. The shared check applies to existing core
vars, types and special forms without changing legal catalog identities.
It does not synthesize exports, grant capabilities or establish overload
purity. Focused exclusions and forged metadata accompany affected paired
source composition and a C5-only missing-member rejection. This work does not
establish general first-class builtins, unrestricted profile coverage or
authoritative migration.

The initial focused runs stopped before semantic assertions: an unclosed
candidate-selection form, then an unsupported bootstrap `merge` call. The
second correction uses existing `let`/`assoc` forms without extending the seed
or catalog. The corrected batch passed 3 tests / 400 assertions: 354 new
qualified-core/availability checks plus existing type-namespace and module
policy checks. It covers distinct reference IDs sharing the selected binding,
metadata preservation/forgeries, shadowing, owned-route misses, private and
duplicate precedence, malformed/excluded profile and target sets, and legal
type/special-form regressions. Resolution changes remain in the owning
resolver. A later position-guard refinement passed 363 assertions in the
owning kernel; the other 46 checks above are unchanged, not additional new
coverage.

The first paired source integration attempt is retained as failed: 1 test,
7 passing assertions, no assertion failures and one error. The first source
extension reached accepted C5/C6/C7 typing, then public plan assembly rejected
its typed-module definition census (`P15S23Q002`). Execution, the second
extension and the later rejection/forgery assertions did not run. This is not
an accepted source composition. Independent inspection found the inherited
lowering join compared canonical `List` directly with the authenticated source
spelling `gravity.core/List`. The owning emitter now accepts either the
canonical symbol or the exact qualified symbol while retaining binding kind,
namespace, canonical type, declaration and source-syntax checks. It does not
relax the module census or rewrite the fixture. Focused ordinary/qualified
String and List declarations, forged spellings/bindings, raw-source joins and
homogeneous List regressions passed 3 tests / 228 assertions (132 new checks).
The changed candidate passed its distinct source composition: 1 test / 119
assertions, including six loader assertions. Both fresh source extensions
retain one selected count binding and three distinct references, preserve
legal metadata, pass public C5 verification and C6/C7 typing, complete public
lowering and independently checked pure C8 effects, and execute ten guest
cases. A changed resolved binding is rejected by public C5 verification; the
missing-member source stops at C5 with `C5-UNRESOLVED`. Pair identities agree.
The selected composition took about 228 seconds with a 2 GiB heap; this is
development timing, not a speedup claim. The original failed attempt remains
failed. No unchanged expensive replay or migration credit follows.
Final fixture formatting removed only one redundant terminal newline byte in
each paired file; the separate 7-assertion parse/body check passed afterward.
The 119-assertion result remains attached to its original bytes, not silently
relabeled as a run on the formatting-only revision.

The prepared independent homogeneous List join for canonical `if` follows this
existing-contract gap. Its separate L5 clarification and join-specific
evidence remain unimplemented; AH is not reopened to add that behavior.

### Persistent List cardinality: development implementation

This slice extends existing unary `count` to established bare, positional
and homogeneous persistent Lists. STD3 specifies a non-negative integer result
equal to the number of stored elements, including zero, with ordinary operand
evaluation and inert stored payloads. This does not require a member-type,
nonempty or static-length proof. Never still aborts invocation; the query adds
no direct collection-construction effect while operand effects remain.

The same C7 count owner, independent C8 reconstruction, typed emitter and
runtime own the change. Existing Map/Vector behavior must survive. Malformed
argument containers and arity are checked before type collection; source,
binding, row and cumulative-work checks remain. Nil, Strings, Sets, arbitrary
sequences, first-class count and unknown operands are not admitted by this
slice. The first focused batch reached 251 passing assertions before a
sentinel exposed an inherited unknown-type set-membership check hashing the
entire positional type map. The three count validators now use shallow
nil/unknown comparisons instead; member metadata is not visited just to count.
A subsequent batch stopped at an extra closing delimiter introduced in that
edit. Both failed batches remain recorded. The corrected focused batch passed
5 tests / 448 assertions, including 137 new C7 List checks and existing
Map/Vector, C8, lowering and runtime checks. These overlap and are component
feedback, not source-composition coverage. The following consumer batch passed
8 tests / 748 assertions, including the new C7 checks, 105 independent C8
checks, 155 typed-lowering checks, 40 List-runtime assertions and existing
Map/Vector regressions. Exact/one-less work, malformed inputs, changed source,
forged descriptors/rows, operand effects/transfers, inert stored payloads and
unsupported receiver families are covered. Private opaque and large-metadata
controls do not expand upstream source/type bounds or host-object support.
Independent component review accepted this scope; migration support is not
claimed.

Pre-source review caught an existing resolution frontier in the proposed
fixture: C5's `gravity.core` qualification exception applies to type references,
not ordinary operators. The paired cardinality fixture uses unqualified
`count`; normalized qualified-header controls are conditional consumer checks,
not evidence that `gravity.core/count` resolves from source. Qualified core
operator resolution remains separately tracked language work. No C5 expansion
or expensive source run is hidden in this correction.

The stable combined focused batch passed 93 tests / 7,395 assertions across
the five existing namespaces. The same batch had already been launched again
after the paired spelling correction when the narrower fixture-only guidance
arrived; that second result also passed 93 / 7,395. They overlap completely
and are not additive coverage. No further regression rerun was needed for
that fixture-only edit.

The one affected source composition passed 1 test / 280 assertions in
318.138 selected-test seconds with a 2 GiB heap. Both accepted extensions have
seven functions, ten calls and five count facts: four normally invoked List
queries and one Never operand that aborts invocation. The 24 guest cases
exercise empty/nonempty bare Lists, homogeneous tails, AG's explicitly typed
List-returning producer, persistent inputs and original thrown-value identity.
The query adds no allocation effect; producer allocation and operand throws
survive independently reconstructed C8 graphs. Four fresh public C7 operations,
typed/effect forgeries, a resealed allocation-graph forgery, exact/one-less C8
work and the real String rejection were checked. Exactly three full fresh
source products were constructed; no actual source failure or retry occurred.
All eleven frozen code/test/fixture inputs and STD3 were unchanged through
the run. Negative QST coverage is pairing only; large runtime inputs and
private sentinel controls do not enlarge source bounds or admit host objects.

This is executable development support for persistent List cardinality. Other
collection families, general first-class builtins, qualified core-operator
source resolution, profile/provider/ownership coverage, other targets,
authoritative migration, self-hosting and seedless release retain their
separate gates. These timings are scheduling observations, not benchmarks or
speedup evidence.

### Homogeneous List returns: development implementation

This executable slice extends L5's explicit `(List T)` declaration to
function returns. The body type must be established independently; matching
homogeneous or finite member types, including the empty List, can satisfy the
declared return while retaining the actual body type and return-specific
compatibility evidence. An expected declaration does not supply missing
member facts, widen incompatible branches or authorize bare-List erasure.

Implementation is in the owning declaration, type, effect and
typed-lowering passes and the existing C5/C6 declaration-reference bridge.
The declaration/header batch passed 5 tests / 229 assertions, covering named
and unnamed functions, qualified type references, return-only and combined
annotations, source scopes/identities, malformed headers and forged mappings.
Its first run had 221 passing and eight failing assertions: the new test
matched form IDs against reference syntax IDs. Correcting that oracle retained
all four cases and production validation; the failed run remains separate.
The next C6/C7 batch passed 4 tests / 816 assertions, including 154 owning C6
producer/source-map checks and 536 new return checks across the fifteen
supported atomic member types. Empty, nonempty and 1,024-member bodies,
return-specific evidence, source/binding mutations and cumulative exact/one-less
work across functions and inference rounds are covered. A preceding batch
stopped after the 154 C6 assertions at a delimiter error in the new C7 updater;
that error and the later pass remain separate, overlapping results.
The consumer-context batch passed 5 tests / 1,150 assertions, including 163
checks in each independent C8 and lowering context. Twelve return functions
exercise source-record ordering despite a different function-map order;
missing, duplicated, reordered and foreign rows, changed premises and exact
work boundaries are rejected. Counts overlap the earlier C7 batch and existing
parameter/prepend regressions. The definition-join batch then passed 5 tests /
942 assertions, including complete normalized C8 function joins and typed
definition joins with altered source, declaration and proof metadata rejected.
A separate 5-test / 128-assertion branch batch checks equal finite List body
types, differing and unknown branch types, and existing Boolean, structural
match and root-map guard behavior. Declared homogeneous returns do not drive
branch inference; ordinary atomic return checks remain in force. These counts
overlap earlier contexts and are not whole-module source evidence. Actual
source composition was then exercised once. The stable combined focused batch passed
84 tests / 6,619 assertions across five existing namespaces, including the
declaration bridges, all new return kernels and affected atomic, branch,
collection, effect and runtime regressions. This is one overlapping combined
development result, not additional independent proof coverage.

The affected source run completed 377 assertions: 375 passed, two failed and
none errored, in 472.421 selected-test seconds with a 2 GiB heap. Both failures
were the same test-oracle error, once per accepted extension: the C5 uniqueness
assertion read C6's `:reference-syntax-id` field instead of C5's `:syntax-id`,
reporting 65 rows / one nil identity / 65 C6 rows / 65 distinct C6 identities.
The original aggregate remains failed. Its other checks reached both accepted
Gravity/QST programs, 28 runtime cases, four fresh public C7 operations,
independent C8 reconstruction and hostile rejection, the wrong-member C7 and
missing-allocation C8 cases, and a C5-only unresolved-type rejection. Four full
fresh source constructions and the C5-only case were performed; none is
automatically repeated to repair this assertion. The source/production inputs
were unchanged through the run. The test-only accessor correction and explicit
retained C5 schema/uniqueness/forged-ID checks passed a separate focused batch
of 3 tests / 128 assertions. The independent auditor accepted the reached
semantic/integration evidence with this qualification; no corrected 377/377
source pass or direct rerun of the repaired source uniqueness assertion is
claimed.

Empty and finite all-matching Lists, exact homogeneous Lists and existing
non-returning bottom behavior now support explicit homogeneous returns. The
six-function accepted program preserves actual body types, outward declared
types and return-specific premises through C7, C8 and executable lowering;
its composed main returns `"ba"`. Bare/unknown/wrong-member bodies, unresolved
type names and missing allocation allowance remain rejected. Differing finite
branch joins, nested generic declarations and general collection coercions
remain unsupported boundaries, not new language prohibitions. Authoritative
migration, profile/ownership/provider coverage, other targets, self-hosting
and seedless release still require their separate evidence gates.

### Homogeneous List traversal: development implementation

The L5 rule for `(List T)` parameters is implemented in the owning C6 signature
path, C7 types, C8 reconstruction and typed emitter. Initial parameter projection/header and C6 source
mapping checks passed 4 tests / 185 assertions, including qualified type
references, retained compound declarations, forged source identities and
unchanged return-annotation fences. An earlier run had 178 passing assertions
and one test error: it looked up a transient helper deliberately unmapped after
installation. The corrected test uses the retained projection function; no
production helper export or reload was introduced. The two batches overlap.

Homogeneous parameter/type checks passed 4 tests / 434 assertions, and initial
C8/emitter consumer checks passed 6 / 554. The new member scans reject malformed
scalar and nonvector type records before scanning; accepted validation, vector
shape and the reserved work budget dominate those scans. These batches include
existing bare-List and query regressions and are not additive totals.

C7's guarded-head batch passed 3 tests / 418 assertions, including 52 new head
checks. It reconstructs empty/nonempty knowledge from an authentic unary
`empty?` condition on the same immutable binding, under the nearest `if`.
Unrelated nearer conditions, other bindings, tails, Never conditions and erased
or forged proof fields cannot supply that knowledge. A dominant predicate-shape
check prevents scanning arbitrary condition arguments before rejection. Two
earlier batches failed because normalized test remapping also changed call and
operator discriminators; their failures are retained, not semantic passes.

The stable development batch passed 66 tests / 4,706 assertions, including
independent C8 guarded-head reconstruction, typed ordinary-call lowering,
emitter failure-work accounting and existing collection/runtime regressions.
Earlier component batches overlap this total. The initial 67-test batch had
one error in an added legacy macro-route selector: it reached the historical
emitter admission facade and rejected the changed semantic hash. That failure
is retained; the pins were not refreshed. The final development selection
excludes that migration-boundary route, not a failing semantic assertion.

The first actual source composition stopped before the first positive program
reached C7: six assertions passed and one test error reported C6's
`sh06-reference-projection-mismatch` for the `String` type reference. The
temporary C6 reference traversal had not mirrored the C5 compound parameter
projection. This is a transport defect to correct with a cheap parity
regression, not permission to relax the bijection check. The failed run is not
typed traversal or runtime evidence.

The corrected bridge passed three focused tests / 145 assertions, including
retained C5/C6 projection parity and unchanged atomic/return transport. The
subsequent changed-candidate composition passed one test / 257 assertions.
It built the paired seven-function source modules, retained three explicit
finite-to-homogeneous parameter premises and two independently reconstructed
nonempty head proofs, then executed 28 guest calls. Empty, singleton and
two-element Lists produced `""`, `"alpha"` and `"alphabeta"`; original Lists,
stored head objects and thrown objects were preserved. Six operation-local C7
verification calls include four coherent hostile inputs. C8 checked both full
graphs, rejected two graph mutations and exercised the owning analysis at
ample, exact and one-less work budgets. Nine C7 semantic negative families,
the C6 compound-return fence and the C8 missing-allocation rejection passed.
Negative `.qst` files have parity coverage, not additional source executions.

The selected composition took 1,211.036 seconds plus 8.853 seconds of loading
at 2 GiB; this is not a phase-cost or speedup measurement. Earlier focused
counts overlap and must not be added to this integration total. The first
failed composition remains recorded separately.

Actual source member coverage is String; type kernels cover the bounded
non-Nil atomic declaration set. The nearest-if subset is not a general path
solver. Nested type applications, homogeneous-to-bare adaptation, compound
return annotations, arbitrary collection operations and complete profile or
ownership legality remain outside this increment. Authoritative migration,
self-hosting and release status are unchanged.

### Typed shrinking-List traversal: development implementation

L5 now states the explicit bare `List` parameter boundary. The owning Gravity
signature path in C6 recognizes List declarations; C7, C8 and the typed emitter
keep a finite argument's actual type and reconstruct a distinct, versioned
parameter-compatibility premise. Unequal types are not labeled equal. An
unannotated parameter, unknown argument, unrelated sequence family, or erased
premise cannot silently obtain this permission. Schema, ownership, linearity,
region and profile obligations remain intact. Catch/error type maps and global
normal compatibility are unchanged.

The existing List-query family now includes unary `empty?`: established Lists
produce Bool, Never operands preserve their transfer. Query effects remain
separate from operand allocation/throws. Inside an annotated function, `rest`
preserves bare List and function-target recur keeps exact slot checks. Exact
bare-List result annotations are supported separately from finite-to-bare
result adaptation, which is not provided by the parameter rule.

The stable focused/regression batch passed 53 tests / 4,154 assertions. It
covers signature lowering, actual-type preservation, compatibility row and
binding/source mutations, erased-premise rejection, bounded work, independent
C8 reconstruction, typed ordinary-call lowering, runtime empty/nonempty Lists,
original transfers, and manual runtime traversal producing empty/singleton/
two-element markers. Earlier 385-, 594- and 292-assertion batches overlap this
batch and are not additive. The first C7 test compile failure and the second
batch's incorrect expectation of immediate rejection for an intermediate
Unknown are retained; the corrected test requires no compatibility premise for
Unknown and leaves final unresolved-source rejection to the composed check.

The paired source composition passed once: 1 test / 223 assertions. It compiles
and executes an annotated tail function and shrinking traversal, preserving
actual finite argument types through three compatibility rows and exact List
calls and recur slots. The pair covers 28 guest executions, two original thrown
payload identities, six selected tail-storage identities, six fresh public C7
verification gates, two accepted whole C8 graphs and two coherent graph
hostiles. Five real C7 rejected sources and one C8 allocation rejection preserve
the unsupported boundaries. The owning C8 analysis also passes at its observed
exact work budget and rejects one unit below it for the first fixture only.
The negative `.qst` files are parity fixtures, not additional executions.

Selected source-test execution took about 763 seconds; namespace loading took
about 8.3 seconds in the same 2 GiB JVM. These are non-authoritative development
timings, not a benchmark or evidence that scheduling dominates product
construction. The prior focused counts overlap and must not be added to the
source result. Selected persistent-List tail sharing is not a general ownership
or no-allocation guarantee; coherent whole-product mutations do not isolate a
deep source-causality check. The unknown-call rejection permits either of the
two documented unresolved-type diagnostics.

Homogeneous element annotations, guarded head typing, general generics,
imported/higher-order adaptation, full profile and provider legality,
authoritative migration, self-hosting and seed retirement remain outstanding.
Direct commands are in `docs/development-verification.md`.

### Persistent List head and suffix queries: development implementation

STD3 now defines empty and nonempty persistent List queries explicitly.
Owning Gravity C7, C8, typed-emitter and runtime changes implement unary
`first` and `rest`. Finite positional types establish the head (or Nil when
empty) and ordered suffix. Bare List knowledge is sufficient for `rest` to
return bare List; `first` still requires head-type evidence. No constructor or
quoted-value ancestry substitutes for typing, and no Dynamic type is invented.

The operand evaluates once; a transfer skips invocation without losing its
effects, output or original payload. Queries neither mutate the original List
nor execute stored payloads. The selected development PersistentList runtime
returns the stored head or existing immutable tail, possibly retaining input
storage. It adds no direct query allocation in this representation; operand
allocation and error effects remain. Generic sequence, ownership, profile and
backend guarantees do not follow.

The stable focused/regression batch passed 44 tests / 3,739 assertions. It
includes real empty/singleton/heterogeneous List results, tail identity, inert
payloads, false/nil, single evaluation, transfers, malformed runtime rejection,
bounded type work, source joins, effect retention and existing recur paths.
Earlier C7 test compilation and unsupported two-argument `subvec` calls failed;
the corrected Gravity owners use the seed's existing three-argument form.
The consumer test's earlier compile failure and ineffective Bool-to-Bool
mutation failure are retained as failures, not aggregate passes. Earlier
281- and 384-assertion checks overlap the final batch and are not additive.

The single new paired current-source composition passed 1 test / 331 assertions
with zero failures or errors. Each complete assembly contains eight functions
and sixteen calls: ten List queries (eight invoked, two skipped by transfers),
four List constructions and two ordinary local calls. Concrete caller/callee
joins cover payload/form and main/payload without a fabricated dispatch tag.
Twenty guest executions cover computed form payload extraction, empty and
singleton suffixes, inert nested data, false/nil, NUL/Unicode Strings and four
original transfer-payload identity checks.

Six fresh public C7 gates cover two whole assemblies and four coherent binding,
type, order and storage-policy hostiles. Two guarded, nonempty whole C8 results
pass verification; two resealed graph hostiles reject. Four actual `.gravity`
negatives cover arity, wrong collection family, unknown type and wrong return
annotation. A fifth passes C7 and its verifier before C8 rejects missing operand
allocation allowance with `C8-UNDECLARED` /
`function-effect-outside-source-allowance`. Negative `.qst` siblings are parity
checks only; the unknown-type oracle allows two diagnostic pairs, and whole-
result hostile substitutions do not isolate deep source causality.

All nineteen source/test/fixture hashes and STD3 stayed unchanged through the
sole run. Selected execution measured 663.008426708 seconds and namespace
loading 8.460406500 seconds with a 2 GiB heap. These are development scheduling
observations, not phase-cost or speedup evidence. No unchanged constructor,
String or Map source composition was replayed.

Bare-List `rest` is normalized type-boundary coverage, not a new annotated
source case. Bare-List head inspection, annotated List boundaries, shrinking-
List traversal, generic sequences, full profile/provider legality,
authoritative migration, self-hosting and seed retirement remain outstanding.
Direct commands are in `docs/development-verification.md`.

### Evaluated persistent List construction: development implementation

STD3's evaluated `list` constructor now has owning Gravity C7, C8, typed-emitter
and runtime paths. It accepts zero through 1,023 established, lowerable
first-order operands; the existing 1,024-child call limit includes the operator.
Known bare collection types do not require invented member facts. Supplied
positional type records receive bounded validation, with visited work retained
on rejection. The result uses the existing ordered List element-type carrier,
but records evaluated-construction ancestry rather than quoted-data authority.

Operands evaluate once, left-to-right. A transfer prevents construction and
later operand execution without excusing unestablished source types. The
current conservative constructor policy retains latent allocation and records
direct allocation only for normal invocation, including empty Lists. Placement,
provider, ownership and actual heap behavior are not established by that policy.
Returned Lists preserve nesting, duplicates, false, nil and inert payloads.

The first consumer run exposed a genuine StackOverflowError at the supported
1,023-operand runtime boundary: the existing argument collector recursively
called itself. Lowering passed 105 assertions; runtime reached 25 passes before
the error, and fail-fast skipped C8. That run is not an aggregate pass. An
initial List-only collector workaround passed a separate 268 assertions, but
was superseded by the smaller shared-helper loop correction. The final helper
keeps its signature, environment, function/path/target context, ordered output,
accumulated values and first non-normal control tuple; no helper, export,
arity reduction or host-stack increase was introduced.

Current shared-helper code passed 37 focused/regression tests / 2,935 assertions.
Two separate function-recur and loop runtime checks passed 127 assertions.
These executions are distinct; earlier focused counts overlap and are not
additive. C8's malformed scalar-forms guard was corrected and checked. Before
the source run, review also corrected an ordinary local-call dispatch oracle
and added concrete caller/callee joins. Only the cold selector changed after
the focused batch; a separate fixture/namespace smoke check passed ten
assertions without replaying the production kernels.

The single new paired source composition passed 1 test / 265 assertions with
no failures or errors. Each complete assembly contains eight functions and nine
calls: seven List constructions (five normally invoked, two not invoked), one
String concatenation and one ordinary local call. The concrete main/form
caller/callee joins and inferred List returns pass without new annotation
syntax or supplied result types. Twenty-four guest executions include actual
List-family checks, computed inert form DATA, nested collections, NUL/Unicode
Strings and four original transfer-payload identity checks.

Six fresh public C7 checks cover the two whole assemblies and four coherent
binding, type, order and allocation mutations. Two guarded, nonempty whole C8
results pass verification; two resealed graph hostiles reject. Three actual
rejected `.gravity` sources cover unknown operands, unknown operands after a
transfer and a mismatched return annotation. A fourth source passes C7 and
its verifier before rejecting in C8 with `C8-UNDECLARED` /
`function-effect-outside-source-allowance`. Negative `.qst` siblings receive
parity checks only, and the unknown-type tests permit two stated diagnostic
pairs rather than establishing one unique first diagnostic.

All seventeen source/test/fixture hashes and the owning STD3 document stayed
unchanged through the sole run. It measured 551.366308334 seconds in the
selected test and 8.216410500 seconds in namespace loading with a 2 GiB heap.
This is whole-composition development timing, not a phase-cost or speedup
comparison. Independent pre-source review accepted the stable candidate; no
unchanged String/Map/equality source proof was replayed. Direct commands and
supported boundaries are in `docs/development-verification.md`.

List queries have a separate development implementation above. Arbitrary
recursive traversal, generic sequences, List annotation grammar, imported List signatures, syntax-
object authority, full profile/provider/backend legality, self-hosting and seed
retirement remain separate frontiers. This development result does not admit a
stable module revision or confer authoritative migration credit.

### Binary String concatenation: development implementation

STD4's binary String overload of the compatibility spelling `str` now has
owning Gravity C7, C8 and typed-emitter paths. Both normally returning operands
must be established Strings. The result preserves their exact scalar sequences
without normalization, coercion or general formatting. Operands evaluate once,
left-to-right; a transfer aborts invocation but does not erase operand effects.

The applied overload retains latent `:memory/allocate` even when it is not
invoked. Direct allocation is conditional on normal invocation, including for
empty Strings. C8 independently derives this distinction and enforces source
allowances. The authenticated generic C5 binding's empty effect row remains
unchanged; it is not a claim that the applied overload is pure. Runtime
production code remains unchanged.

The final focused batch passed 29 tests / 2,203 assertions across the new
typing, effects, lowering, existing-runtime and fixture kernels and affected
Map lookup/membership, not, association, append, constructor-allocation,
Keyword-lookup and throw regressions. These counts overlap earlier focused
results and are not additive. Two earlier cheap namespace-load failures were
test-parenthesis defects, not semantic passes; their failed attempts remain
historical evidence.

The single affected source composition passed 1 test / 269 assertions, with no
failures or errors. The two complete paired assemblies each contain eight
functions and eleven calls: ten concatenations (seven normally invoked, three
not invoked) and one explicit-default Map lookup. Twenty-four guest executions
and six original String payload-identity checks cover nested calls, empty/NUL
and supplementary Unicode Strings, composed/decomposed distinctions, parameter
shadowing and transfer behavior. The eager `get` default retains its normal
concatenation allocation even on a Map hit.

Six fresh public C7 checks cover two whole assemblies and four coherent
binding, operand-type, order and latent/direct-allocation mutations. Two ready,
nonempty C8 results pass whole-result verification, and two resealed graph
mutations reject allocation/dependency erasure. Four actual rejected `.gravity`
fixtures cover arity, numeric/unknown operands and a result annotation. A fifth
source is accepted by C7 and its verifier, then rejects in C8 with
`C8-UNDECLARED` / `function-effect-outside-source-allowance`. Negative `.qst`
siblings receive parity checks only; the unknown-type case permits two stated
diagnostic pairs rather than claiming one unique first diagnostic.

All eighteen source/test/fixture hashes, the owning STD4 document and unchanged
runtime remained stable through the sole run. It measured 666.852492834 seconds
in the selected test and 8.240701291 seconds in namespace loading with a 2 GiB
heap. This is whole-composition development timing, not a phase-cost or speedup
comparison. Independent component and pre-source review accepted the stable
candidate. No authoritative module migration, target support, self-hosting or
seed-retirement claim follows. The unchanged Map and equality compositions
were not replayed.

Variadic and first-class invocation, other String/formatting overloads, numeric
or Character conversion, implemented library exports and complete profile,
provider and backend legality remain separate frontiers. Direct commands and
the affected composition selector are in `docs/development-verification.md`.

### Map lookup with an explicit default: development implementation

STD3 now states the three-argument Map lookup rule explicitly: evaluate Map,
key and default eagerly in that order, even on a hit; return the associated
value when present, including false or nil, and otherwise the default. A
transfer aborts invocation. The existing C7, C8, emitter and Gravity runtime
implement a finite typed route with dynamic String or Keyword keys.

For normal invocation, established bounded Map member types must give every
potentially matching key-family value the same established type as the default.
Empty Maps use the default type, and unrelated known scalar key families may
have different values. No constructor or static-presence proof is required.
A Never operand makes the call non-invoked/Never without a fictional normal
result join; known bare Maps are then allowed. Other returning operands remain
domain-checked, and supplied structured-type evidence retains its integrity
checks. No selected deep-shape authority follows from the result type.

The latest production and test bytes passed 26 focused/regression tests with
2,058 assertions, including the tightened type guard and preceding membership,
lookup, not, count, association, quote and throw paths. Before source execution,
a fixture-only correction avoided the existing Keyword-only evaluated-Map
literal frontier: the Map operand now allocates a supported Keyword Map before
returning quoted mixed-key Map data. Its final reader/parity check passed ten
assertions. Compiler and test bytes did not change; the earlier proposed pair
was never executed in the source composition, not a failed actual attempt.

The sole corrected paired composition passed all 214 assertions in 601.443
seconds after 8.167 seconds of namespace loading at 2 GiB. Both complete
ten-function assemblies retained ten calls: eight get applications (five
normal, three Never), one membership query and one ordinary local call.
Twenty-six guest executions and six original String payload identity checks
cover local Map returns and aliases, original-value preservation, dynamic keys,
empty Maps, mixed unrelated key families, false/nil presence and eagerly
throwing defaults on present keys.

Five public lowering operations each verified C7 freshly: two whole assemblies
and three coherent binding/type/order mutations. Two accepted, nonempty C8
results passed whole verification, and two resealed graph mutations rejected.
Real allocations in Map, key and default operands remain transitive effects;
get adds no allocation or effect. Four actual `.gravity` rejections cover arity,
an incompatible normal value/default join, Symbol keys and result annotation.
Negative `.qst` evidence is parity only; the broader malformed/unknown/Never
matrix is focused evidence rather than a claim of authentic source coverage
for every case. All seventeen source/test/fixture files, STD3 and the Git base
remained unchanged through execution. No retry or unchanged old proof occurred.

The smaller schedule is scoped integration coverage, not a compiler speedup
measurement. Two-argument lookup, general stored-key families, heterogeneous
normal results, first-class invocation, complete profile/provider or target
support, authoritative migration, self-hosting and seedless release remain
outstanding. See `docs/development-verification.md` for direct commands and the
separate boundaries; no admission or release status changes follow here.

### Map key membership: development implementation

The existing Gravity C7, C8, typed-emitter and runtime owners implement direct
Map `contains?` with dynamic String or Keyword keys under STD3/STD2. An
independently established Map type suffices; no literal constructor, static
presence fact or deep value-shape proof is required. Presence is independent
of the stored value: false and nil entries remain present. Key comparison is
by value, without coercion or Unicode normalization, and stored values remain
uninspected.

Map and key evaluate once, left-to-right, in non-tail positions. Never transfers
before lookup. The query adds no allocation, effect, error or capability;
operand effects remain visible. Runtime uses the existing closed binary
builtin-call representation and checks the Map and key domain before lookup.
No seed primitive, helper, public export, instruction family or test lane was
added. The runtime still has twenty-seven private functions and four exports.

Four focused kernels passed 344 assertions. The final affected regression
batch passed 22 tests/2,129 assertions, covering membership alongside existing
not, association, count, equality, quote, guard and throw behavior. Fixture
parity/header checks passed 24 assertions. These counts overlap and are not
an additive total or authenticated source evidence by themselves.

The sole paired source composition passed all 366 assertions in 1,198.132
seconds after 7.961 seconds of namespace loading at 2 GiB. Each extension
retained ten functions, thirteen calls and eight membership applications
(six normal, two Never), plus one not and four ordinary local calls with
canonical caller/callee/order joins. Forty-eight guest executions cover a
quoted registry returned by a local function, aliases preserving the original
Map, present false/nil and absent keys, dynamic equal String objects, Unicode,
NUL, case and normalization distinctions, Keyword namespaces, empty and
twelve-key Maps, explicit not composition and original String throw payloads.

Nine fresh public C7 lowering operations include five coherent altered
type/fact/constraint cases. Two accepted, nonempty C8 graphs pass whole-result
verification, and four resealed graph mutations reject. Actual evaluated Map
operands retain allocation, and both throwing-operand positions retain their
error effects without adding query allocation. Eleven negative `.gravity`
families reject arity, operand/key domain, unresolved types and result
annotations, including an invalid other operand when one operand is Never.
Negative `.qst` coverage is parity only; unresolved-type oracles permit two
existing diagnostic paths rather than claiming a uniquely observed reason.
All 31 compiler/test/fixture inputs and the Git base remained unchanged through
the run. No unchanged earlier expensive source proof was replayed.

Use `docs/development-verification.md` for the direct focused and affected
composition commands. Other key domains, vector/set membership, presence-based
type narrowing, first-class invocation, complete collections/profile support,
targets, authoritative migration, self-hosting and seedless release remain
outstanding. This is scoped executable development support, not admission of
a stable module revision or a performance claim.

### Unary truthiness complement: development implementation

The existing C7, C8 and typed-emitter owners implement direct unary `not`
under STD2/L2. False and nil yield true; other independently established,
normally returning values yield false. This does not inherit atomic equality's
six-family restriction. Existing unsupported operand source or lowering forms
remain separate frontiers, not new truthiness prohibitions.

The operand evaluates once in non-tail position. Lowering emits the existing
conditional with literal false/true branches, without a runtime, seed, public
API or instruction change. Never transfers before either branch. The operand's
allocation and error effects remain visible; `not` adds none. Bool-only L7
guards are unchanged: `(not (= tag :rejected))` supplies a Bool explicitly,
while a raw Keyword guard still rejects.

The producer kernel passed 165 assertions and the effect/lowering/existing-if
batch passed three tests/245 assertions. The combined affected batch passed
19 tests/1,539 assertions, including prior equality, Character, quoted-DATA,
guard and throw regressions. These overlap, rather than form an additive total.
Pre-run review corrected an authored oracle: ordinary local-call facts have no
`:dispatch`; the higher-order-call tag was inappropriate. Six explicit
canonical caller/callee/order/type joins now retain the local-call evidence.
The corrected namespace and unchanged lowering/runtime kernels passed two
tests/136 assertions before the sole source run.

The paired source composition passed all 464 assertions in 674.647 seconds
after 8.132 seconds of loading at 2 GiB. Each extension passed eight function
types, eighteen call facts and sixteen `not` applications (fifteen normal,
one Never), plus one atomic equality and one ordinary local call. Twenty guest
executions cover three map-guard outcomes, false/nil/zero/empty text and quoted
collections, evaluated vector/map operands, BMP and supplementary Characters,
an ordinary marker-shaped Map, retained String throw payloads and parameter
shadowing. Runtime code remained unchanged.

Nine fresh public C7 lowering operations include five altered type/fact/row
cases. Two accepted, nonempty C8 graphs pass whole-result verification; four
resealed graph mutations reject. A genuine constructed-vector operand retains
transitive allocation while `not` has no own allocation, and the throwing
operand retains its error effect. Five negative `.gravity` cases reject wrong
arity, unknown type, result annotation or raw non-Bool guards. The separate
non-tail recur case rejects with the exact C6 core-lowering diagnostic.
Negative `.qst` coverage is paired-source parity only; the unknown-type oracle
permits two existing diagnostic paths rather than claiming a unique observed
reason. All 20 compiler/test/fixture inputs and the Git base remained unchanged
throughout the source run. No expensive prior equality proof was replayed.

Use `docs/development-verification.md` for the focused and source-composition
commands. First-class `not`, other predicates, complete profile/provider and
standard-library support, target support, authoritative migration, self-hosting
and seedless release remain outstanding. This is scoped executable development
support, not admission of a stable module revision.

### Artifact-backed ordinary module imports: development increment

Under L3, C5 and C16, `gravity/resolution.gravity` now owns ordinary import
selection from explicit, verified callee artifacts. The normal C5 source/file
frontdoors use SH-06 without a metadata-selected stage0 fallback. Their
module-input argument is documented in `docs/development-verification.md`;
the default empty set is sufficient only when there are no external Gravity
requirements. Even an unused `:requires` needs its actual module artifact.
Namespace spelling, `:refer`, header fingerprints and metadata candidates do
not create an exported definition. Explicit foreign imports remain a separate
interop boundary.

Aliases, referred names and fully qualified names join actual local callee
definitions and exports. Private and missing names retain C5 diagnostics.
Repeated imports of one origin do not create false ambiguity; distinct module
origins remain distinct candidates. Target, capability and supported profile
compatibility come from the actual callee, not require-option overrides.
Verified transitive edges participate in cycle rejection. The ordinary input
rejects historical stage0 and strict SOURCE artifacts rather than stripping
their boundary or treating a stored status as verification.

Imported bindings retain actual callee artifact, analysis, binding and
definition-syntax origins separately from caller-side projected IDs. Raw callee
source spans are not nested into the semantic origin projection. Ordinary
artifacts retain their actual module inputs for fresh request reconstruction,
serialization and verification. Conservative invalidation includes callee
artifact IDs even when no name is used. No accepted-proof cache, resource-cap
increase, production fragment, load-order row or authority-pin refresh was
introduced. Clojure transports and verifies the inputs; Gravity selects and
checks the imports.

The existing strict SOURCE request remains v1, separate from the ordinary v2
request. Four B51 linker derivation sites and their forward declaration now
name the explicit base-request helper. A focused test checks actual base and
ordinary shapes, contextual delegation and the structural call sites. This is
adapter-routing evidence, not SOURCE construction, native evidence or admission.

The real `sh-06/modules/{values,consumer}.{gravity,qst}` fixtures define and
export `identity-i64`, preserve a private sibling, and lower an actual aliased
call to C6. The original paired run recorded 23 passing assertions and two
failures: it incorrectly expected a definition-artifact ID directly on C6
reference attributes, once per extension. Both extensions accepted C5/C6;
their artifact identities and explicit C7 unsupported-call checks passed.
The whole run remains failed. The corrected oracle follows the projected
binding into the authenticated C6 request table instead of changing C6's schema.

A separate real origin/negative/replay test passed 29 assertions: the projected
binding/resolution-use join, unused missing requirement through the default
frontdoor, private and missing names, forged export/binding/upstream artifacts,
serialization with fresh replay, and changed callee exports invalidating the
consumer. The unchanged expensive C7 construction was not repeated solely to
refresh the aggregate receipt. Paired test source remains co-canonical.

The initial focused policy/seed/type/API group passed four tests and 52
assertions. The later five-test, 377-assertion group adds genuine derived v2
cycles, distinct-origin ambiguity, strict routing and the normalized v1
rejection matrices. These groups overlap and are not summed as unique coverage.
Historical metadata-driven matrices are explicitly DATA policy tests, not
ordinary module artifacts or additional real module proofs. The direct module
kernel took about 0.26 seconds in the latter run; the selected group took
606.077 seconds after 6.946 seconds of namespace loading. These are development
timings, not benchmark or dominance claims. Full upstream-product construction
remains separate from scheduling and from the default kernel edit loop.

The initial affected B12 batch passed its eight actual-target assertions, but
the call-order fixture failed before assertions: it required `shared.core/combine`
while the new real library exported only `checksum`. C5 correctly reported a
missing module export. The paired library fixtures now define and export the
real three-argument `combine`; production policy was not weakened. The original
batch remains failed. The corrected B12 batch passed two tests and 12 assertions,
covering actual module targets in both extensions and operator-before-argument
order through C6. The affected module/foreign/compiler composition passed one
test and 10 assertions; the strict nil/context/private API checks passed two
tests and 12 assertions. The language boundary passed six tests and 11
assertions. IO and foreign declarations were resolved, not executed. Existing
optional B12 catalog selectors and dependency paths were updated for the
renamed tests, owning resolution source and actual shared-core fixture pair;
no new route, lane or authority record was needed.

Imported C7 signatures, C8 imported effects, runtime module
linking, module initialization, cross-profile facades, additional target
variants and package solving remain later work. This increment does not admit
an authoritative module revision or establish full SH-06/SH-07, self-hosting,
seed-retirement, target or release completion.

### Imported direct-call types: development implementation

The current L3/L5/C7 increment derives imported function types from actual
authenticated callee C7 requests. The owning pass is
`gravity/compiler/c7_type_checker_engine.gravity`; the request interface and
direct test commands are in `docs/development-verification.md`. An exported
callee is joined through the actual C5 module and original binding to its
projected C6 function, not selected by spelling or a copied caller signature.

The initial increment required explicit complete callee parameter/return
annotations; inferred exported signatures were unsupported by that increment,
not forbidden by L5. The producer-inference increment below extends this
boundary. Imported parameter and return types are immutable premises.
Argument/result types and arity are checked without specializing a producer
from caller input.
Operator references also receive their function type. Function signatures and
producer identities are retained in shared, deterministically ordered tables;
call facts and equality constraints refer to stable coordinates rather than
copying a complete producer identity per call. Existing finite input/output
bounds and fresh reconstruction remain in force.

Producer-declared effects and capabilities are preserved as declarations,
not relabeled inferred purity. Imported body effects, effect legality and
thrown-error analysis remain pending. The distinct type-only scope is rejected
by C8 with `:imported-body-effects-pending`, including when declarations are
empty. Runtime linking, higher-order imports, cross-profile typing, generic
specialization, complete error/effect semantics and authoritative migration
remain separate work.

The corrected direct C7 batch passed four tests and 102 assertions; the C8
boundary/local-adapter batch passed two tests and 21 assertions. The C7 groups
include overlapping regressions and are not added as unique coverage. An
earlier test-namespace read failed before tests and was corrected; it is not
passing evidence.

The first real source run reached accepted library/caller C5/C6 construction,
then C7 rejected `:module-type-input-carrier-limit`. The new aggregate check
traversed an entire retained producer request, including repeated core/wrapper
and coordinator preimages, rather than the existing per-producer boundary.
The owned JVM was interrupted after this known blocker instead of constructing
the remaining blocked variants. It exited 130 without a final timing receipt
or complete test counts. The diagnostic did not retain the specific failed
carrier counter; no exact size or node-count claim follows. The corrected
boundary checks the closed input container and actual retained callees, then
fully authenticates and types each distinct producer. It retains the existing
limits: 1,024 distinct producers, depth 128, and summed distinct canonical-core
visits of at most 1,048,576 nodes and 16,777,216 scalar units. Individual core
shape/product/signature limits and the imported output bound remain unchanged.
These counters do not claim to bound every VM operation, raw proof byte or CPU
second. Five direct tests and 118 assertions passed, including the earlier
regressions; a separate local-inference group passed three tests and 31
assertions. The bounds correction passed independent static review.

The corrected real-source composition passed one test and 72 assertions: both
extensions produced I64/Bool/String imported types, fresh replay passed,
signature tampering rejected, and separately reconstructed argument, arity and
declared-result mismatches rejected with source-linked diagnostics. C8 rejected
the authentic accepted result for pending imported body effects. The original
interrupted run remains separate, not relabeled as passing. Final focused
batches also passed local inference (3/31), higher-order regressions (2/27),
recursive regressions (2/33), the C8 boundary/local adapter (2/21), and the
language boundary (6/11). Together with the corrected direct batch (5/118) and
source composition (1/72), these are 21 test executions and 313 assertions,
not a count of unique semantic cases. JVMs were serialized at a 2 GiB heap.
This is development implementation evidence, not authoritative migration,
runtime linking, complete SH-07/SH-08/SH-09 or self-hosting completion.

### Producer-inferred imported scalar signatures: development implementation

The next L3/L5/C7 increment replaces annotation presence with complete types
derived freshly within the producer module. Function, body and syntax IDs,
ordered unique parameter IDs, fixed arity, inferred status and any present
annotations must match the actual producer facts. All existing original C5
export/origin and projected C6 joins remain in force. Caller observations do not
participate in producer inference.

The supported signature boundary is the existing literal/atomic scalar type
catalogs, including the actual nil type. Unknown types, host nil, callable types
and composite type maps reject rather than passing a shallow known-type check.
Composite and higher-order imported signatures remain implementation work, not
L5 restrictions. Request APIs, finite work bounds, immutable imported premises
and the mandatory C8 pending-body-effects rejection are unchanged.

The final direct batch passed five tests and 156 assertions, including existing
annotated imports, local-chain and work-boundary regressions. The earlier
four-test/140-assertion batch is overlapping evidence, not added coverage. The
language boundary passed six tests and 11 assertions. Independent static review
found no remaining blocker in the code, new fixtures or source test.

Real paired-source composition passed one test and 58 assertions. It derived
zero-argument Bool, annotated-parameter/inferred-return I64, producer-local
String identity and an unannotated Integer call chain through separate actual
modules. Fresh replay passed; producer/function/parameter/body tampering and
argument, arity and declared-result mismatches rejected. An actual unresolved
exported identity producer rejected even though its separate caller supplied
Bool. C8 rejected the authentic accepted result for pending imported body
effects. Both source extensions produced the same semantic identity.

The three final batches total 12 test executions and 225 assertions, not unique
semantic cases. Execution was serialized at a 2 GiB heap. The new real-source
test took 1,272.547 seconds of selected execution and 7.012 seconds of namespace
loading; the unchanged annotated-source composition was not replayed. This is
development implementation evidence, not authoritative migration, checked
runtime linking, complete effects/errors, self-hosting or release completion.

### Ordinary compiler-artifact module calls: development implementation

The L3/C5/BOOT2 enabling increment gives the temporary compiler-artifact seed
interpreter an explicit module-bundle input. Actual caller C5 source and imports
are joined to retained callee C5 public exports and independently compiled raw
plans. Modules retain separate function tables; exact source callsite routes
select the callee. Reconstructing source, exports, plans and the whole bundle
rejects self-rehashed substitutions rather than trusting claimed hashes.
The existing three-argument raw constructor stays acyclic; the four-argument
constructor and direct commands are documented in
`docs/development-verification.md`.

The initial executable subset is unannotated fixed-arity function-only modules
in `:meta/:jvm`, with direct qualified imported calls. Rest parameters,
duplicate parameters, annotation syntax and operator-shadowing cases that the
raw seed cannot lower faithfully reject. Actual C5 bindings determine whether
an operator means a core builtin, namespace function or local binding; matching
source forms alone is not an equivalence argument. General initializers,
imported values and imported calls nested under `let`, `loop`, functions or
collection expressions remain unsupported. These are implementation gaps, not
new language restrictions.

Four final focused tests passed 57 assertions, including argument evaluation
exactly once in order, route/export/function/arity rejection before arguments,
raw-entry rejection, inert quoted DATA, B51 opaque-scope forwarding and legacy
external authorization before arguments. Dispatch instrumentation is unit
regression evidence, not native admission. Raw C7 construction passed one test
and 41 assertions; C8's pending-imported-effects rejection passed one test and
seven assertions.

The single real composition passed one test and 38 assertions. Both source
extensions built two actual modules with different functions named `identity`;
qualified calls returned the callee result for scalar and nested DATA inputs,
while a direct root call returned the caller result. Actual private/missing
exports, stale source, self-rehashed plan/route substitutions, variadics and
builtin/local/namespace-function shadow mismatches rejected. Both extensions
produced the same semantic bundle identity. The language boundary passed six
tests and 11 assertions. These final batches total 13 test executions and 154
assertions; earlier overlapping kernel batches are not added. JVMs were
serialized at a 2 GiB heap. The real composition used 497.822 seconds of selected
execution after 7.184 seconds of namespace loading; unchanged C7 source typing
proofs and historical native admission proofs were not replayed.

The bundle explicitly records development-unadmitted status, unchecked effects
and the Clojure seed/interpreter boundary. It does not accept imported user-call
effects, bypass C8's type-only rejection, grant SOURCE/B51 authority, establish
target support or admit an authoritative module revision. Next, source-local
literal DATA declarations and mixed function/var exports must be supported to
retain the actual C8 engine's six exported catalogs before its existing API can
forward body analysis into `gravity.effects`. A second facade pretending to be
that namespace is not a substitute. Full effects/errors, checked runtime
linking, migration, self-hosting and seedless release remain outstanding.

### Hosted compiler-module construction: development increment

The existing module-development interface now accepts exactly `:meta/:jvm`
and `:hosted/:jvm` modules, with the caller and leaf dependency using the same
profile and target. The owning `00580-p15-s23-stage2-module-source.clj`
component derives declaration and imported-origin profile/target checks from
the authenticated module instead of assuming `:meta`. Ordinary C5 verification,
source, namespace, package, export and origin joins, fresh public bundle
reconstruction, and route validation before argument evaluation remain intact.
This removes an incidental development restriction; it does not implement a
new profile facade or change P13 compatibility rules.

The final focused batch passed eight tests and 195 assertions, including the
existing C5 policy kernel for target, capability and cross-profile diagnostics.
Normalized kernel inputs are not authentic module evidence. The single affected
real composition passed one test and 44 assertions: hosted caller/leaf pairs in
both source extensions and one existing meta/JVM pair. It preserves actual
profile facts, private root entry, same-name function routing, scalar values and
String object identity. Real private/missing imports and direct cross-profile
edges reject; stale source and rehashed plan/route substitutions also reject.
An earlier test-loading delimiter error has no semantic result and is not
counted as a passing batch.

Selected real execution took 476.823 seconds after 7.593 seconds of namespace
loading with a 2 GiB heap. This is an affected integration check, not a routine
edit prerequisite or an isolated measurement of verification phases. No old
full matrix, migration proof or authority-pin refresh was repeated. The module
bundle still records unchecked effects, development-unadmitted status and the
Clojure seed/interpreter boundary. Whole-runtime construction and extraction
into `gravity.runtime` remain next; checked imported-user legality, full hosted
runtime conformance, target support, authoritative migration and seed retirement
remain unproved.

### Whole-runtime source DATA: development increment

The actual `p15_s23/runtime.gravity` can now be constructed through the existing
raw compiler-artifact interface without selecting or rewriting its function
forms. Eighteen catalog maps now explicitly quote symbolic DATA, as required
by L2; the five already-literal maps and all 30 function forms are unchanged.
Pre-edit conservation checks preserve the 23 catalog values and exact function
names/arities. The compiler's construction-local literal table is checked
against those values; no catalog carrier or raw-plan field was added.

Two focused tests passed 50 assertions, including 29 in the new whole-runtime
check and 21 in the existing literal grammar check. One complete-file plan is
reused for nine public runtime invocations: main, normal completion, a real
loop backedge, a function-target swap/backedge preserving String identity,
throw/catch/rethrow, unknown-operation rejection and inert quoted DATA. Output
order and skipped later work remain observable. Removing the first catalog's
quote in an in-memory source variant rejects during initializer validation.
The new check took 0.142 seconds of selected execution; this is not a speedup
comparison with a different test or evidence of all 30 functions' conformance.

No C5/C7 products, old native proof or authoritative runtime binding were rebuilt.
The historical pinned adapter's source-byte and 22-definition fences remain
unchanged and stale for current source. Catalog representation compatibility
does not renew provider/grant claims, admit a runtime revision, prove whole
runtime conformance or retire the Clojure seed. The subsequent stage-owned
runtime increment below completes the call-closed `gravity.runtime` extraction
with thin compatibility entrypoints and real module composition.

### Stage-owned runtime evaluator: development implementation

The complete 22-function evaluator/activation closure now lives in
`bootstrap/gravity/src/gravity/runtime.gravity`, namespace `gravity.runtime`.
At that extraction checkpoint, all function forms and arities were preserved.
It exports only closed-plan
execution, closed-instruction evaluation, context evaluation and function
activation; the other 18 helpers remain private. The old runtime module keeps
23 unchanged catalog values, formatting/main and B51 helpers, plus four direct
qualified compatibility calls. Dependency direction is old root to leaf only.
The guide records the four APIs, DATA inputs/results, diagnostic ownership,
allocation/output boundary and direct test commands.

Existing SH01 and SH08 runtime loaders now compile the entire actual leaf file,
not selected or concatenated function forms. Focused checks passed 12 tests and
474 assertions, including throw/catch, loop and function backedges, original
payload identity, downstream handler behavior and module guards. A subsequent
comment-only correction removed obsolete pinned-entry instructions without
replaying those unchanged semantic checks.

The single genuine two-file composition passed one test and 53 assertions
against the final source. It constructs real ordinary C5 root/leaf artifacts
and an independent leaf plan, retains actual source/module/export/origin facts,
executes all four forwarders, and rejects private/missing imports, stale source,
rehashed independent plans and forged route origins. It took 1,429.233 seconds
of selected execution after 7.274 seconds of loading with a 2 GiB heap. That
total does not isolate construction, verification, hashing or execution cost;
the guide describes a bounded follow-up rather than another test wrapper.

This moves development implementation ownership, not authoritative stage
ownership. No host adapter, historical catalog principal, admission pin, stage
proof or TCB record was renewed. Source declarations are not provider grants;
output remains accumulated DATA. Full runtime/profile conformance, checked
imported-user legality, target ABI/lowering, authoritative migration,
self-hosting and seed retirement remain outstanding.

### Literal module DATA: development implementation

The L2/L3/C5/BOOT2 increment adds source-local literal DATA declarations to the
ordinary compiler-artifact module subset. Scalars, nested vector/map/set
initializers and exact quoted DATA are materialized before function bodies
compile. Unqualified local reads use existing literal instructions; parameters
and `let` bindings shadow module DATA. Quoted symbols, lists, nested quotes and
call-looking payloads remain inert. Actual C5 declarations retain their original
syntax, definition artifact, namespace/package, visibility and function/var
kind. Mixed exports preserve DATA vars rather than dropping them or converting
them into functions. DATA is not callable, and qualified/imported DATA reads,
arbitrary initializer calls, aliases, mutation and general initialization remain
unsupported, not forbidden by the language contract.

Early kernel/namespace compilation errors were corrected before source-backed
acceptance. The first real batch failed with `B1-METADATA` and
`:pinned-sh02-gravity-function-identity`, before any assertions; fail-fast left
the paired test unexecuted. A redundant literal inventory had been added to the
raw plan and existing function-semantic projection. The correction keeps the
table compile-local: actual source hashes and full C5/bundle reconstruction
bind unused declarations, while embedded literal instructions bind used values.
Unused DATA changes module identity without changing an otherwise identical
function projection. No authority pin or strict check was weakened or refreshed.
The failed result remains separate from the corrected passing evidence.

The corrected source batch passed two tests and 23 assertions. Paired modules
read different same-named catalogs, preserve nested DATA and lexical shadowing,
use private DATA locally, retain var exports, and reject non-callable DATA,
literal/export tampering and imported DATA reads. The actual existing C8 source
constructed a bundle retaining all six catalog exports and invoked its existing
quoted contract function without changing quoted-symbol output. This is C8
source compatibility, not evidence of effect analysis. Selected execution took
797.895 seconds for actual C8 and 328.845 seconds for the paired test, totaling
1,126.741 seconds after 6.736 seconds of namespace loading.

After that run, function-table validation was moved ahead of literal collection
to preserve the existing `L2-DUPLICATE-FUNCTION` diagnostic. This ordering-only
change leaves successful plans identical and was checked with focused tests,
not a replay of the expensive compositions. Six final tests passed 91 assertions,
including legacy `def`/`fn` construction, actual checked-core/C8 raw plans,
exact historical SH02 function/plan identities, used/unused DATA invalidation,
literal bounds, initializer and collision rejection, routing and dispatch
regressions. The language boundary passed six tests and 11 assertions. These
final batches total 14 test executions and 125 assertions; earlier overlapping
kernels are not added. JVMs were serialized with a 2 GiB heap.

No new loader resource, test lane, authority flag or raw-plan inventory was
introduced. The actual C8 namespace can now be the caller for the next
Gravity-owned body-analysis increment in `gravity.effects`; no parallel facade
was substituted. Effects, thrown-error completion, checked imported-user calls,
target support, authoritative migration, self-hosting and seedless release
remain outstanding.

### Local body-derived effects: development implementation

The C8/L6 increment moves body analysis into
`bootstrap/gravity/src/gravity/effects.gravity`. The actual existing
`gravity.compiler.c8-effect-checker-engine` exposes the three-argument
`sh09-build-authenticated-body-effect-result` and four-argument
`sh09-verify-authenticated-body-effect-result` as qualified forwarders to that
owner. Existing pure-result and compiler-read interfaces remain separate; no
caller purity flag, replacement C8 facade or accepted-proof cache is added.
The real compiler-artifact module bundle supplies the effects dependency and
retains all six existing C8 DATA catalog exports.

The local first-order subset consumes the actual C7 result, fresh verification
and existing binding containing the complete original request and canonical
core. It derives a complete node graph, function latent-effect table and
namespace summary across literals, collections, references, definitions,
`let`, `do`, `if`, inert quote, function construction, structural matches and
normally returning Bool guards. Acyclic local calls depend on their actual
callee body. Function creation does not execute its latent body. Conditional
bodies and guards contribute conservatively without becoming unconditional
evaluation children. Zero-call functions are included. Declarations remain
allowances: unused nonempty effect/capability declarations do not imply an
action or prevent a supported body from being inferred empty.

Graph coverage, types, source origins, function/body/call coordinates, local
bindings and ordering are joined to the retained core and typed identity.
The complete core preimage and derived graph participate in the new identity
meaning. This is not the older declaration-based pure adapter's identity scope;
the older identity path rejects the new body scope rather than claiming a
partial complete identity. Throws remain `[:pending-sh09]`.

The paired `.gravity`/`.qst` accepted fixture exercises local calls, collections,
inert quoted throw/unresolved DATA, conditionals and guarded matches; the
rejected pair diagnoses a non-Bool guard in C7. The actual composition passed
one test and 59 assertions with no failures or errors on the reviewed
`2ffa5262` effects-source snapshot. It freshly constructs C5/C6 and C7 products,
checks C7 and the current Gravity leaf, rejects coherent input/result mutations,
then constructs the actual C8/effects bundle and invokes the public C8
constructor and verifier for both source extensions. Selected execution took
2,496.702 seconds after 6.585 seconds of namespace loading with a 2 GiB heap.
This measures the combined workload, not phase dominance or a speedup.

After that run, four narrow corrections reject callable parameter/return type
shapes recursively, join the copied global ordering ledger to its identity
preimage, report the actual diagnostic function owner (or nil), and join call
profile/target facts to the callee. Successful fixture graphs and C8 forwarding
are unchanged. Five final body kernels passed 60 assertions, including ordinary
collection/list/quoted-DATA type positives and coherent negative substitutions.
The existing checked-core/SH02 identity kernel passed seven more assertions;
the language boundary passed six tests and 11 assertions. These final batches
and the explicitly attributed earlier composition total 13 test executions and
137 assertions. Earlier overlapping kernel runs are not added. The long run
did not execute the later guards; focused delta checks and static valid-path
review cover those corrections without repeating the expensive composition.

Transitive imported bodies, higher-order/callable value propagation, general recursion,
throw/try/control-transfer composition, general effect legality, provider and
grant validation, complete effect identities, targets, authoritative migration,
self-hosting and seedless release remain outstanding. Legacy imported type-only
input still rejects with `:imported-body-effects-pending`. Use the direct
`sh09-body-effect-test` kernels for semantic iteration and the affected
composition rules in `development-verification.md`; no new lane or wrapper is
needed.

### Direct imported body-derived effects: development implementation

The L3/L6/C7/C8 increment extends the same `gravity.effects` constructor and
fresh verifier to direct imported scalar functions. It consumes the caller's
actual C7 identity facts and complete original four-key request, including
`module-type-inputs`, through the existing binding. Each retained producer C5
artifact, original public export, projected definition, function and body must
join exactly. Producer facts are the actual flat C7 producer identity, not a
fabricated accepted typed-result/verification wrapper. The older compiler-read
interface still requires exactly three request keys, and legacy type-only C8
adapters still reject pending imported bodies.

The same algorithm derives every producer node and local callee body, including
private functions, once per distinct exact producer request within the
invocation. Producer graphs stay indexed by their C5/C6 module coordinates;
caller coverage includes exactly caller nodes. Foreign dependencies carry the
caller import binding, both producer artifact IDs, projected producer definition
binding, function and body. Matching raw node/function IDs in different modules
cannot replace this coordinate. Full producer core/type identities and derived
graphs enter the new effect identity. A call propagates the derived body summary,
not a declaration allowance. Aggregate limits remain 65,536 nodes and 256
functions before graph indexing; one 65,536-attempt deferred-node budget is
shared across producers and caller. These units are not an all-CPU guarantee.

Eight direct Gravity kernels passed 112 assertions for origin/export/body joins,
local compatibility, derived-versus-declared summaries, scoped graph separation,
malformed tables, aggregate limits and deferred work. Five boundary tests passed
48 assertions for unchanged public C8 forwarding, module routes, B51 separation,
historical literal semantic pins and repository language policy. One test-file
compilation error was corrected before actual composition; the later four-test,
56-assertion load/recheck overlaps the eight kernels and is not added again.

The existing `inferred-values`/`inferred-consumer` `.gravity`/`.qst` fixtures
then passed the actual paired composition on its first attempt: one test and
69 assertions, no failures or errors. It constructs real producer/caller C5/C6
products, freshly constructs and verifies caller C7, seals the full request,
and executes the current Gravity effects constructor and fresh verifier.
Complete graphs retain the `integer-chain` call to private `integer-base`,
four imported body dependencies, empty inferred effects and pending throws.
Malformed import tables, coherent origin/body/coordinate substitutions, missing
producer requests and stripped graph/identity/throw evidence reject. Selected
execution took 548.399 seconds after 6.931 seconds of namespace loading with a
2 GiB heap. The three nonoverlapping batches total 14 executions/229 assertions;
earlier overlapping kernels are not added.

Actual C8 source, exports, requires and public three-/four-argument forwarding
forms are unchanged. Their static checks and existing route regressions are
separate from this current-source upstream-to-leaf composition. The expensive
public C8 bundle was not rebuilt: previous route evidence is not a new bundle
identity proof. No new fixture, host implementation, test lane, wrapper,
authority flag, manifest resource or accepted-proof cache was introduced.
Transitive imports, callable values, general recursive propagation, completed throws,
effect/provider/grant legality, targets, authoritative migration, self-hosting
and seedless release remain outside this increment.

### Bounded recursive body effects: development implementation

The L2/L5/L6/C7/C8 increment extends `gravity.effects` to C7's existing one
named, capture-free, positive-fixed-arity self-recursive component. The existing
positional-parameter call, primitive literal base and external parameter evidence
must join the original component, core, function, definition, call facts and
source coordinates. Positive arity means parameter count. Ordinary `if` retains
L2 truthiness; the Bool-only match-guard policy is unchanged. The actual C7
recursive identity, typed-core fields and verification checks are preserved.
Direct imported producers use the same per-module analysis, with their original
flat recursive C7 facts and separate module coordinates.

The solver classifies the complete body, starts function summaries at the
empty set, and iterates within the shared 65,536 node-attempt budget. Only the
authenticated self-body dependency is cut internally during a round. The final
graph retains the backedge and conditional region, even beneath literal false,
and checks the complete graph equations. Fresh verification independently
reconstructs from bottom: an inflated fixed point that satisfies the equations
still rejects. Classification is budget-checked before traversal; iteration,
final reconstruction and equation visits retain the same caller/producer work
counter. This is not an all-CPU bound or a program-termination proof.

The final cheap batch passed 11 tests/155 assertions covering existing local
and imported behavior, recursive proof joins, retained edges, leastness,
ordinary truthy conditions, malformed call-fact rejection, exact work-limit
boundaries and unchanged public forwarding. Four separate route/language tests
passed 44 assertions. Pre-composition independent review found an incidental
Bool restriction on `if`, an unchecked call-fact lookup, and classification
before the remaining-budget check. All were corrected and checked before the
first real composition attempt. One earlier test-file parenthesis compilation
failure and overlapping cheap batches remain historical, not extra coverage.

The real paired composition then passed on its first attempt: one test,
99 assertions, no failures or errors. It constructs and freshly verifies actual
C5/C6/C7 products for the existing Integer and String recursive fixture pairs,
then constructs and freshly verifies current-source body effects. A new paired
caller imports both producers through the ordinary module-input interface.
The existing SH-07 mutual-recursion pair still receives C7's
`:mutual-recursion-unsupported` diagnostic. Coherent proof/call-fact changes,
nonleast effect graphs, erased throws and changed imported proofs reject.
Paired effect identities agree. Selected execution took 1367.254 seconds after
6.879 seconds of namespace loading with a 2 GiB heap; no phase-cost or speedup
claim follows. The nonoverlapping batches total 16 executions/298 assertions.

Production, test and fixture bytes stayed frozen throughout that run. C8's
requires, exports and public three-/four-argument forwarders remain unchanged;
the old expensive public-bundle test was not repeated or claimed as a proof of
the current bundle identity. No new compiler API, host implementation, lane,
wrapper, authority flag, manifest resource or accepted-proof cache was added.
This closes only the bounded recursive body-analysis subset. General or mutual
recursion support, callable recursion, termination, recursion-depth bounds,
tail-call optimization, thrown-error completion, full legality, target execution,
authoritative migration, self-hosting and seedless release remain outstanding.

### Capture-free callable body effects: development implementation

The L3/L5/L6/C7/C8 increment extends the same `gravity.effects` interface to
C7's existing named identity/apply-one proof. The exact identity definition,
apply definition, callable parameter, value argument, nested function type,
outer selection, inner invocation and original call facts must agree. Only
the proof-selected parameter may carry that callable type. The inner C6 call
retains its nonlocal classification and nil direct callee; a separate
proof-bound selected-body dependency identifies the invoked identity body.
Constructing or carrying a function value has no immediate body effect;
invoking it propagates the derived body summary, not declaration allowances.

Direct scalar imports retain the complete producer graph and original module
coordinates. The real producer fixture exports both identity and apply; the
new paired caller selects only identity. Thus the executable example covers
an unselected apply body, not a private apply fixture. Selected imported APIs
remain first-order. The same aggregate node/function limits and shared
65,536-attempt work counter apply. Throws and legality remain pending.

The final direct kernel batch passed 13 tests/190 assertions, and four separate
route/language checks passed 44 assertions. A later identical kernel recheck
after the diagnostic-only test correction overlaps that batch and is not extra
coverage. Pre-composition review added the selected-import first-order fence
and its coherent nested-callable control before any real composition ran.

The first actual composition completed all local callable and scalar-import
positive pairs, fresh verification, full node coverage and coherent proof,
type, edge, call-fact, selected-body, summary and throw mutations. It also
completed both nonfunction and capture rejection pairs. Its aggregate result
was FAILED: 76 passing assertions (66 positive-composition assertions and ten
negative-case assertions), two failing assertions and no errors.
Both failures were the test's hardcoded `C7-ANNOTATION` expectation for capture;
current C7 instead rejects it earlier with
`C7-TYPE-MISMATCH` / `:function-result-type-mismatch`. Nonfunction calls reject
with `C7-ANNOTATION` / `:unsupported-nonlocal-call`. Capture rejection here does
not establish a completed closure-legality diagnostic. The original failed
result is retained, not relabeled as a passing aggregate. Selected execution
took 1008.751 seconds after 7.035 seconds of namespace loading with a 2 GiB
heap; this is not a phase-cost or speedup measurement.

The correction changes only test expectations and factors the negative checks
to permit a focused capture-pair selector; production, C8 forwarding and fixture
bytes are unchanged. That follow-up passed one test/six assertions with exact
rule/reason, C7 rejection and body-analysis rejection for both extensions.
Selected execution took 173.914 seconds after 6.955 seconds of namespace loading
with the same 2 GiB heap. The already exercised positive and nonfunction cases
were not repeated for an oracle-only correction. The passing follow-up is
separate evidence, not a replacement passing result for the original aggregate;
overlapping assertions are not added into a new aggregate count. Independent
review checked the narrow oracle correction. No old public C8 bundle identity
is claimed for this revision.
Captures, multi-hop or arbitrary higher-order dispatch, imported callable APIs,
callable recursion, completed thrown effects, full effect/capability legality,
targets, authoritative migration, self-hosting and seedless release remain
outstanding.

### Private root compiler-kernel entry: development enabling increment

L3 permits current-module private resolution but forbids private imports as
stable APIs. The existing, explicitly unadmitted compiler-artifact development
interpreter now applies that distinction at its root entry. After complete
fresh bundle reconstruction and exact root-module selection, the requested
entry must be an unqualified symbol naming a map-valued function in that root
plan. It need not be exported. This matches the single-source development
interface without adding a flag, field, getter, source rewrite or test export.
It enables existing private compiler kernels to remain directly testable after
moving implementation into stage-owned namespaces.

Missing names, dependency-only names, qualified entries, non-symbols and DATA
declarations reject with `:root-function-member-required`. Function arity,
lexical scope, SOURCE/B51 separation and all cross-module public-export,
namespace, origin, binding, callsite and route checks remain in place. Linked
call checks still precede argument evaluation. The change is not private-import
support, checked user-program execution or a stable public ABI.

The stable focused batch passed six tests/109 assertions for normalized root
entry and lookup order, malformed entries, exact module membership, arity,
scope, pre-argument route rejection, B51 separation, seed shape, existing source
pins and repository language policy. The initial four-test/97-assertion batch
overlaps and is not extra coverage. Normalized checks are not authentic source
or migration evidence.

The new `private-root-consumer` `.gravity`/`.qst` pair then passed the affected
composition on its first attempt: one test/33 assertions, no failures or errors.
It constructs actual ordinary C5 caller/library products and an independent
library plan. The root's private relay invokes the library's public identity
function and returns the real nested value; the public root entry still works.
Each invocation delegates to the original fresh bundle validator. Exported DATA,
dependency-only and qualified entries, non-symbol entry and wrong arity reject.
Actual C5 still rejects importing the library's private function. Coherently
rehashed root-plan/bundle tampering and a changed-root bundle also reject.
Paired identities agree. Selected execution took 380.460 seconds after 7.045
seconds of namespace loading with a 2 GiB heap. This is total selected-test
timing, not a phase-cost or speedup claim. The two nonoverlapping batches total
seven executions/142 assertions.

Only the existing root-entry membership guard changes in production. Old seed
fixtures, public cross-module visibility and unchanged expensive compiler
proofs were not rewritten or replayed. Authoritative migration, target support,
compiler seed retirement, self-hosting and seedless release remain outstanding.

### Loop-target recur typing

The development increment implements loop-slot and normal-return kernels
in the existing Gravity C7 pass, which retains authentication, inference
orchestration and diagnostic ownership. The intended subset uses monomorphic
loop slots from sequential initializers, exact recur argument types, and `Never` for transfers
that do not return normally. C8 derives body-only backedges and bottom-initialized
effect equations; initializers must not be restarted by recur. Function-target
recur typing remains a separate next increment. Ordinary `if` truthiness is
unchanged.

Focused normalized checks, current-source fidelity, inference regressions and
existing effect regressions pass 25 tests/335 assertions. A separate
current-source C6 precedence check passes one test/seven
assertions. Its earlier four failures were an assertion-path mistake: the C6
reason is under `:facts`, not at the diagnostic top level. The failed receipt
is retained separately, not relabeled as passing. The slower source selector
is explicitly separate from the fast development-kernel selectors.

The real paired composition first failed during upstream product construction
at the unchanged 2 GiB heap. Direct ordinary C2 calls reproduced the allocation
failure without C5 or bundle construction. These failed results confer no loop,
runtime, migration or self-hosting credit. The detailed failed-attempt history
remains in the original command outputs, not as prerequisites for development.

The separately reviewed enabling commit `1e2e6f5c` removes discarded canonical
text, duplicate canonical-form construction, a discarded terminal request walk
and redundant private canonical history. It preserves public canonical text,
fixed UTF-8 hashing, fresh validation, complete products, diagnostics and both
canonical and host-container collision checks. Required digest uniqueness
already rejects equal canonical forms: equal forms have equal fixed encoding
bytes and deterministic hashes; no hash-injectivity assumption is needed.
Focused checks pass 15 tests/335 assertions, including full-result parity,
encoding/print settings, rejection precedence, collisions, repeat-call freshness
and primary/cleanup failures. An earlier test-observer mistake remains a
separate failed result. These overlapping helper results are not added to the
loop counts above.

A bounded recording of a failed C2 call showed fresh precomputed-product
verification overlapping the original products, not a scheduling or lock
failure. Allocation samples do not establish exact retained dominators. The
corrected ordinary C2 constructor subsequently returned on the then-current C7 draft
source at 2 GiB: 366.111 seconds inside the call, exit 0. This is constructor
completion, not a general memory bound, speedup, loop acceptance or migration.
The enabling fix does not cache accepted proofs, omit products, increase bounds
or introduce a new test lane. No repeated C2 confirmation is needed for unchanged
inputs. Product-representation redesign remains separate unless an actual
integration blocker requires it; ordinary semantic edits do not depend on this
investigation.

A later paired selector reached its thirty-minute operational limit
without completing. The exact owned JVM exited on TERM (exit 143); its timing
receipt was absent. No completed assertion counts, language rejection or
current execution phase can be inferred. Static inspection finds sixteen
public C7 calls and seventeen full bundle constructions on complete traversal,
but does not establish where the live attempt spent its time. A separate
ten-minute, 2 GiB phase observation also stopped incomplete (TERM, exit 143).
It captured early initial-construction events through its 64-event cap, but
no return boundary or final observer status. Later phase attribution and cost
ratios remain unresolved. No further timing-only retry, larger allowance or
validation-policy change follows.

Independent source inspection found a discarded SH05 verification result inside
C5 construction. Commit `a9c39502` removes only that unused call; consumed parent,
request and final-replay checks and source snapshots remain. Focused comparison
with the restored-call oracle passes three tests/27 assertions for complete
artifacts, serialization, diagnostics, fresh next-invocation checks, forged and
stale inputs, malformed context and interruption. This is a local simplification,
not measured speedup or loop acceptance. The existing single bundle-construction
selector was a separate affected integration check, not the full sixteen-call
loop selector or evidence of its semantic result. It was cancelled incomplete
before its limit when this optional extraction was deferred; it produced no
completed counts or timing receipt and is not a compiler rejection.

The optional, unaccepted `gravity.types` extraction is deferred. It made compiler
code compilation require the full multi-module C5 bundle path before this local
semantic change could be tested. Keeping the single kernel implementation in
the already stage-owned Gravity C7 namespace uses the existing single-source
interface instead. This is a real production-source change with local calls,
not import stripping, a raw-root bypass, duplicated kernels or a new test lane.
The accepted `gravity.effects` boundary and all user-artifact authentication,
fresh result verification and hostile cases remain required. Gradual namespace
extraction remains planned; it is not a per-increment admission prerequisite.

The first real composition after this simplification exposed a binding join:
the authenticated request retains bindings separately from canonical core,
whereas the normalized kernel fixture embedded them. A private C7 working view
now joins the already-authenticated vector for loop analysis only; published
core, request, identity and public schemas remain unchanged. Missing, malformed,
empty and forged-scope binding tables still reject without a fallback.

The corrected, unreduced paired composition passes one test/53 assertions. It
authenticates both `.gravity` and `.qst` source products through C5/C6 and current
C7 construction/fresh verification, preserves the complete request binding,
and constructs/freshly verifies the C8 effect leaf. Both exact source files
execute through the existing seed interface, return `7`, and observe the recur
transfer `[false 7]`. Wrong slot and declared return types reject with
`C7-TYPE-MISMATCH`; tampered type facts, constraints, bindings and effect graphs
reject. The earlier missing-binding failure remains a separate failed result.
The 25/335 focused group overlaps earlier kernel batches and must not be summed
with them. The paired check took about 489 seconds of selected execution at
2 GiB; this is an observation, not a speedup or memory-bound claim.

This supports the stated monomorphic loop-target subset, not function-target
recur, general coercions or joins, termination, tail-call optimization, native
target support, public C8 module-bundle acceptance, authoritative migration,
self-hosting or seedless release. Independent semantic/evidence review accepts
this bounded development slice. Documentation, roadmap, source-language and
Clojure physical-line gates pass; the local slice commit is separate from
authoritative admission and those outstanding claims.

### Function-target recur typing and effects

The next bounded C7/C8 increment supports fixed-arity function-target recur when
the original parameter declarations independently establish each slot type.
For example, an `again` function with `[flag :- Bool value :- String]`, returning
String, accepts `(if flag (recur false value) value)`. Slots retain declaration
order and exact types without caller specialization or implicit coercion.
Unknown undeclared slots remain `C7-ANNOTATION`, even when callers supply known
argument types. Wrong recur slots and declared result mismatches reject with
`C7-TYPE-MISMATCH`. Original C6 arity, tail position, binding and nearest-target
checks still apply. Function construction does not execute its body.

The existing C7 source classifies supported recur as `Never` and retains the
normal-return join. `gravity.effects` joins the original function signature,
parameter facts, body identity and ordered transfers. Function-only recur uses
the existing bounded body-backedge fixed point; the module need not contain a
`loop` node. Solver selection and graph metadata use the same private decision.
Ordinary acyclic code and named self-calls retain their prior paths. Malformed
control tables reject before host operations; nil/absent legacy acyclic inputs
retain their prior behavior.

Focused current-source checks pass 28 tests/392 assertions, including inference
from an initially absent transfer type, declared slot origin, function reset,
work limits, malformed controls, solver parity and effect regressions. The
unreduced paired composition passes one test/69 assertions: real C5/C6/C7
construction and fresh verification, complete request binding, C8 effect-leaf
construction/fresh verification, paired identities, hostile mutations, and all
three negative fixture families in both `.gravity` and `.qst` forms. Selected
execution took about 706 seconds at 2 GiB; no speedup or allocation bound is
claimed. Earlier overlapping kernel groups are not additional coverage.

The first paired attempt passed C7 but failed C8 because function-only recur
entered the acyclic solver. It remains a separate failed result (two passing
assertions, one failure and one error), with later assertions unexecuted. The
new dispatch regression executes the same private helper used after the module
joins; it does not itself replace those joins or the real paired check.

At this compiler-slice boundary, annotated-source seed execution remained open;
the separate increment below closes the stated fixture's execution without
stripping declarations or substituting an untyped example. General recursion,
termination, target legality, public C8 module-bundle acceptance, authoritative
migration, self-hosting and seedless release remain unclaimed.

### Annotated single-source seed definitions

The existing seed now lowers fixed-arity atomic parameter/return declarations
for `defn` and `def`/`fn`, retaining declarations in function records and deriving
logical parameter names and body forms. Macro-expanded returns retain their
actual `typed/return` declaration header; raw prefixes retain their raw form.
Original source bytes and SHA remain unchanged. Declarations affect both plan
and semantic identity independently of executable instructions.

The raw definition parser remains byte-identical. The shared function table
preserves complete plain-record parity for all seven existing C5/B51 module
sources, not merely an assumed seed-only caller boundary. Raw plan construction
reuses its existing source records for original-header validation; it does not
parse twice or add a field to shared macro artifacts. This catches nil/false
return annotations that legacy macro expansion otherwise consumes silently.
The direct emitter reuses its existing raw C2/C3 forms and remains hosted-only.

Focused checks pass 11 tests/179 assertions: valid raw and expanded signatures,
malformed and composite declarations, duplicate/variadic names, pre-execution
rejection, plain whole-plan parity, declaration-sensitive semantic input, shared
table parity, and function/loop transfer isolation. Earlier reader-syntax and
test-profile mistakes remain separate failed attempts; production was unchanged
by the test-only profile correction.

The exact previously compiler-checked function-recur fixture then passes one
test/14 assertions. Both unchanged `.gravity` and `.qst` files retain their
source SHA and declarations, return `"done"`, and observe exactly one transfer
`[false "done"]`. The completed 69-assertion compiler composition was not
replayed to renew a receipt. These are ordinary development results, not fresh
authoritative migration evidence.

Seed grammar recognition does not resolve types or replace C5/C6/C7/C8 legality.
Unknown type names, unsupported semantic forms and mismatched uses retain those
owners. Composite signatures, variadics, checked imported-user execution and
general typed-runtime conformance remain unsupported. No target legality,
termination, public proof, seed retirement or self-hosting claim follows.

### Direct typed throw and source effect allowances

The next bounded C7/C8 increment retains a concrete direct-throw payload type,
types the transfer expression as `Never`, and joins that result with a normal
branch without treating the thrown value as a normal return. Unknown payloads
require annotation; a mismatched normal result still rejects. The C7 ledger
uses the existing equality constraint kind, with original source, payload,
owner, ordering and invalidation premises. The original C6 transfer census is
preserved, including an outer transfer whose operand never returns.

C8 reconstructs the typed-transfer facts independently and derives the direct
`:error/throw` effect before checking original function and namespace source
allowances. Missing declarations produce `C8-UNDECLARED` with the offending
function and source node. Nested function errors stay latent at function
construction, quoted DATA remains inert, and unrelated calls preserve their
previous behavior. At that boundary, local calls involving the newly concrete
throwing owners remained unsupported; the direct-local increment below addresses
that gap. Selected imported concrete throw summaries remain fenced
by C8's existing pending-summary requirement, not a new C7 import claim.

Focused development checks pass 24 tests/425 assertions, covering the new
transfer/type/allowance kernels and existing loop, recur, import, recursion,
callable and body-effect regressions. These overlapping development runs are
not added together as independent coverage. The first actual composition run
failed at C8 canonical-core coverage after fresh C7 acceptance and verification:
one passing assertion, one failure and one error, with later cases unexecuted.
The mismatch was raw-versus-semantic authenticated IDs in throw records. The
correction follows C6's existing one-field identity projection, preserving raw
source authentication and every other field. A direct coverage-guard regression
uses distinct IDs and rejects wrong projection, authentication and other-field
changes. The failed result is retained separately from the corrected run.

The corrected actual composition passes one test/62 assertions over both
unchanged `.gravity` and `.qst` sources: fresh C5/C6/C7 construction and C7
verification, the current C8 leaf and fresh result verification, altered typed
facts and effect graphs, coherently rebound false typed inputs, and all three
real rejection families. It completed within the unchanged 2 GiB/30-minute
development bound. No public C8 compiler bundle was reconstructed. This is
accepted development composition evidence, not refreshed migration authority.

At this compiler-slice boundary, runtime throw execution remained open; the
following seed increment closes the exact fixture's explicit seed observation.
Handlers and cleanup, throwing-call propagation, general error/profile/provider
legality, authoritative migration, target completion, self-hosting and seed
retirement remain separate gates. Source
allowance checks do not establish the complete authority intersection. The
legacy normalized `:error/raise` registry is not silently treated as the source
`:error/throw` registration.

### Exact-source single-source seed throw

The existing raw three-argument plan constructor now lowers one-operand throw
through the temporary Clojure seed. Arity rejects before operand compilation;
execution evaluates one operand and escapes through a private intentional
transport. The payload remains DATA, including control-looking maps and host
Throwable objects supplied to the raw executor. They are not themselves thrown
or interpreted as control. A nested throw preserves the original inner
exception, with no second outer transfer. Existing host diagnostics and fatal
failures keep their prior propagation; ordinary builtin failures keep their
existing diagnostic mapping. This does not complete host-error normalization.

Summary traversal includes the operand. Throw-containing plans explicitly name
seed ownership and pending effect inference/unchecked legality, rather than
claiming Gravity-emitter execution or purity. Original source hashes,
declarations and full plan identity are retained; no-throw plans preserve exact
parity. The existing module-source and instruction-census boundaries reject
executable throw for the four-argument module route, while quoted DATA remains
inert. No new public API, namespace, test lane or authority path was added.

Focused seed checks pass 12 tests/202 assertions, including arity, malformed
instruction, ordering, early escape, nested transfer identity, function/loop
control, host errors, module fences and ordinary plan regressions. A separate
one-test/28-assertion observation executes both unchanged typed-throw fixture
extensions. The ordinary branch returns the identical original String payload;
the throwing branch escapes once with that payload and actual source hash.
Annotations and logical parameters are preserved. The accepted 62-assertion
compiler composition is reused only for those unchanged bytes, not replayed to
renew a receipt. These results do not establish Gravity-authored emitter/runtime
ownership, imported errors, handlers, resource cleanup, a public target exception
ABI, full profile legality, migration authority, self-hosting or seed retirement.

### Acyclic direct-local thrown-error propagation

The current development increment replaces the throwing-local-call fence with
bounded analysis of complete original C6 local edges and supported acyclic
bodies. C7 preserves direct throw records and derives ordered, distinct possible
error types through callee bodies. C8 independently reconstructs those equations
and compares the call premises and function summaries before source allowance
checks. Normal return types remain separate from thrown types. A supported pure
leaf can establish an empty error vector; unknown or pending is not empty.

The paired accepted fixture contains a pure String identity function, two relay
hops, a Bool-throwing callee and an argument that never returns. Conservative
source operand error potential is retained, but that argument prevents normal
callee invocation and excludes its Bool body error. Static call/source evidence
is not erased. Disconnected prior direct-throw owners keep their existing
analysis. Rejection fixtures cover argument/result mismatches, unknown slots and
missing source effect declarations.

The initial focused regression passes 27 tests/468 assertions, including
independent C8 replay, coherent summary tampering, source ordering, pure-leaf
and non-returning operand
cases, cumulative work limits, and existing loop/recur/callable/import controls.
The first affected source composition failed before seed execution: one passing
assertion, one failure and one error. A newly reused legacy edge validator
confused the callee name's syntax ID with the whole definition form's syntax ID;
the actual producer distinguishes them. The failed result is retained. Corrected
exact-edge reconstruction and original work-limit diagnostic precedence pass
three affected tests/57 assertions with distinct-coordinate and budget controls.
The corrected composition completed both source extensions and their same-byte
seed observations: 145 assertions passed, two failed and none errored. Both
failures were the new unknown-slot fixture expecting `C7-ANNOTATION` instead of
the existing `C7-GENERIC/:unresolved-function-type` diagnostic for an unused
unknown parameter. Positive construction, verifiers, coherent tampering,
normal return, escaping payload and early argument escape checks passed. The
aggregate run remains recorded as failed; correcting that test oracle does not
retroactively turn it into a passing run or require replaying its successful
blocks.

That actual composition used `effects.gravity` SHA-256
`4a2b686f680e4aaacf2791947e12bd48c4dc93a971331558b72ebe1d96f5a44b`.
A subsequent exact-output lookup factor uses the existing validated function
index rather than scanning every function for each node; its source SHA-256 is
`4906ff18c485e64535297201b90973da51b1804e8440bf420eacff99e6e34f0e`.
It retains original source-callee evidence separately from the selected
execution target. The final focused signature-status control passes, and
369 parity assertions cover local, non-returning, higher-order, noncall,
missing-ID and duplicate-table boundaries. This does not reattribute the earlier
composition to a different source revision. The first parity run's incomplete
positive test fixture is retained as a test-data failure; the repaired fixture
passes without weakening the production validator.

The final current-source focused regression passes 29 tests/852 assertions,
including the repaired oracle and lookup controls and existing loop/recur,
callable, imported-body and effect checks. These overlapping focused runs are
not added together as separate coverage. The unchanged successful expensive
composition and seed blocks were not replayed merely for a test-oracle edit or
the output-equivalent lookup factor.

Throwing recursive/recur components, imported and indirect throwing calls,
handlers, cleanup and complete error/profile/provider legality remain separate
work. No authoritative module migration, full target implementation, self-hosting
or seed-retirement claim follows from this development increment.

### Current Gravity emitter throw lowering

The Gravity-owned `p15_s23/emitter.gravity` now lowers exactly one `throw`
operand, preserving the owning child path and rejecting malformed arity before
operand compilation. Summaries traverse nested throw operands. Throw-containing
products mark effect inference pending, legality unchecked and authority
non-authoritative; ordinary products retain the existing println-only inference.
Original parsed function declarations and logical parameters are preserved.

The current-source focused regression passes nine tests/272 assertions,
including 62 assertions executing the actual changed Gravity emitter functions
through the existing raw developer interface. The unchanged accepted paired
throw source is lowered and assembled, then its returned instruction is observed
through the temporary seed executor: normal return and escaping payload/source
are preserved. Wrong arity, missing symbols, unknown callees, nested summaries,
inert quoted DATA and ordinary whole-product parity are checked. No old compiler
proof was replayed and no admission pin was refreshed.

Earlier focused results remain failures: the first run had two assertions read
source provenance from the wrong transport field; an adjacent legacy macro-route
test correctly rejected the stale admitted emitter identity; an added parity
oracle initially expected the seed's empty inference instead of the unchanged
Gravity println inference. Only test expectations and the development selection
were corrected. These results are not retroactively relabeled as passing.

Gravity-owned runtime error transfer, handlers, complete legality, target error
lowering and fresh authoritative migration remain outstanding. The emitter
development revision does not inherit its old admitted source identity or
establish full SH-07, self-hosting or seed retirement.

### Current Gravity runtime throw propagation

The local Gravity evaluator in `p15_s23/runtime.gravity` now propagates throw
control through arguments, binding initializers, sequences, conditionals and
top-level outcomes. Its private three-slot result separates normal values,
thrown payloads and rejected instructions. Payload maps, vectors, nil, false
and Throwable objects are DATA, not control records. A transfer preserves prior
output and skips later work. Unknown operations and builtins no longer fall
through to let or comparison evaluation. Ordinary success keeps its seven-key
record, formatting and metadata tolerance; malformed instructions are distinct
from language error values and host failures.

The focused current-function check passes 147 assertions. The final ordinary
regression passes ten tests/419 assertions, including that check, current emitter
lowering and existing raw seed/declaration controls. The same unchanged paired
throw fixture is lowered by the actual Gravity emitter, then evaluated by the
actual Gravity evaluator: false returns normally and true propagates the same
String payload. The seed's private throw transport is not invoked. Controls
include nested transfers, each argument/binding/sequence/branch position,
preserved prior output, inert control-looking DATA, invalid dispatch before child
work, legacy metadata accepted by existing validation, and original fatal host
failure identity. These overlapping runs are not separate coverage totals.

The first whole-runtime direct compilation failed on preexisting manifest
bare-symbol DATA outside that compiler interface's literal subset. Focused tests
instead select the exact six mutually dependent evaluator function forms from
the current source and freshly compile that projection. Names, arities, form
equality and dependency closure are checked. Projection and full parent source
hashes are distinct; this is not whole-module compilation or an admitted runtime
binding. A subsequent test-reader delimiter failure is retained separately.
Review caught and corrected an initial restriction that would have rejected
legacy instruction metadata; the existing ingress and comparison semantics were
not weakened to pass the tests.

No old packet validator, catalog identity or admission pin changes here. The
admitted packet subset still excludes throw. Handler execution, guest function
dispatch, loops/recur in this closed evaluator, resource unwinding, profile/target
error lowering, host normalization and authoritative migration remain open.
Whole-runtime manifest literal compatibility needs a bounded investigation at
its migration boundary, not a prerequisite for every semantic edit. Generic seed
interpretation and primitive execution remain; this is not seed retirement or
full SH-07/self-hosting completion.

### Atomic typed catch and residual source errors

C7 now resolves canonical atomic catch types from authenticated C6 bindings,
seeds each handler's lexical binder with that type, and joins protected/handler
normal results with the existing `Never` rules. Ordered handlers remove matching
protected source errors; a handler's own throws escape that try and can be
caught by an outer try, not a sibling clause. Acyclic direct-local calls carry
the residual error types. C8 independently checks the source joins, handler
premises and residual equations. Covered source errors do not grant effect
discharge: performed `:error/throw` and the source allowance remain required,
with profile/provider/capability authority explicitly pending.

The paired accepted fixture covers recovery, partial handling, nested catches,
rethrow, an unreachable handler, ordered repeated types and a local call under
a catch. Rejected pairs cover incompatible normal results, unsupported resolved
catch types, missing effect allowance, unresolved types and illegal binder use
in protected, sibling or outside scopes. Coherent mutations of type/binder,
clause order, source premises, residual summaries and effect graphs reject.

The broad focused regression passed 41 tests/1,186 assertions. A separate source
scope check passed six tests/80 assertions, including eight real C5 rejection
assertions. The affected authenticated paired composition passed one test/220
assertions with fresh C7 verification and C8 checking. These overlapping checks
are not additive coverage totals. An earlier new-helper reader-delimiter failure
remains a failed attempt. The final rejected-diagnostic provenance correction
passed the six-test/82-assertion direct handler batch, including ten original
source-location/origin assertions. It does not change accepted typed products
or justify repeating their expensive composition.

Optional plain-EDN retention exceeded its chosen serialization cap for both
accepted triplets (43,392,705 and 43,328,121 characters); no retained product file
was written. The optional capture is removed from the normal test rather than
growing caps, stripping proofs or replaying solely to obtain a file. A bounded
representation follow-up is described in `docs/development-verification.md`.

At that boundary, runtime catch execution, type-authoritative lowering inputs, complete Error
hierarchy/subtyping, target/profile error lowering, resource unwinding and
authoritative migration remain open. This increment does not establish full
SH-07/SH-08, self-hosting, release support or seed retirement. The next executable
boundary is an explicit current C7 typed input to the existing Gravity lowerer;
raw type spelling or an old admitted compiler binding is not type authority.

### Explicit current C7 input to Gravity lowering

The existing compile-function and assembly interfaces now accept an explicit
fourth argument containing the original request, typed result and verification
report. A small temporary Clojure component invokes the actual current Gravity
C7 verifier once per operation. The Gravity emitter requires an accepted typed
result and exact fresh report equality, then joins source bytes, expanded forms,
module metadata, declarations, function/body identities and parameter/type
bindings. A passed verification of a rejected type result is not acceptance.
The source snapshot is reconstructed from the same request's retained C2
tokens; target source files need not be reread or remain on disk.

The typed lowerer consumes canonical nodes rather than introducing another
source grammar. Its initial supported subset is fixed named top-level
single-body functions with represented literals, parameter references, `throw`,
`if` and `do`. Unelaborated numeric descriptors and unsupported operations reject
with structured diagnostics retaining available source and generated-origin
information. Current-source provenance includes actual compiler source/plan and
function/body identities; raw internal DATA arguments are not authority tokens.
Existing three-argument calls, the old pinned `00582` assembly envelope and
admission identities remain unchanged. Effect legality stays explicitly pending.

The focused batch passed four tests/283 assertions, including unchanged current
emitter/runtime throw controls. After the test-only namespace/body correction,
the two typed-input selectors passed 74 assertions. The sole affected paired
composition passed one test/65 assertions: it constructs real upstream products
once per extension, invokes fresh current C7 verification for each public typed
operation, compares function and module assembly, and executes current Gravity
instructions through the existing exact runtime projection. Both normal and
escaping String results preserve the original payload; seed throw transport is
not called. Changed source/module/declaration/body, stale reports and altered
type facts reject. These runs overlap and are not additive coverage totals.

Test-reader and emitter delimiter failures, and two omitted-namespace selector
failures, remain failures. The SH05 body-only expansion versus full namespace
parse mismatch was caught statically and corrected before the sole expensive
composition. No proof replay, optional product capture, old pin refresh or
authority-admission workflow was needed for these development corrections.

At this boundary, typed runtime catches and quoted-symbol lowering were the
next dependency-ready semantic work. General calls, bindings, loops, complete numeric representation,
Error subtyping, resource cleanup, profile/target legality and authoritative
migration remain outstanding for this typed route. The runtime test uses the
unchanged exact six-function projection, not whole-runtime compilation. This
is executable development progress, not full SH-07/SH-08, self-hosting, release
readiness or Clojure seed retirement.

### Canonical atomic catch lowering and runtime handling

The current Gravity emitter consumes solved C7 handler records and canonical
Bool, String, Symbol and Keyword types. It rejoins handler order, original
source clauses, binder identities/scopes/types and handler-body ownership.
Quoted Symbol payloads use the solved inert quoted-DATA record; source spelling
alone is not authority. Handler binders extend only their own body environment.

The Gravity runtime evaluates the protected instruction once and selects the
first matching atomic handler only for thrown control. Normal and rejected
control bypass handlers. A selected handler's throw bypasses sibling handlers
and can reach an outer try. False matches Bool; nil does not. Payloads retain
identity, shadowing leaves the outer environment intact, malformed handler
descriptors reject before protected execution, and fatal host failures retain
their original identity rather than becoming guest error values. Descriptor
validation checks body maps shallowly, not whole instruction graphs.

The final direct catch checks passed two tests/128 assertions. Earlier focused
regressions overlap and are not additive coverage. A test-only transport
instrumentation gap was found and corrected before the affected real
composition: all tiny atomic executions must be observed, not merely the
main entrypoint and older fixture cases. No expensive proof was replayed for
that correction. The sole affected composition passed one test/122 assertions
over four genuine upstream inputs, with fresh current C7 verification per
public operation. It exercised original nested/ordered/rethrow cases, all four
atomic payload types, quoted Symbol DATA and exact public main recovery. Seed
throw transport remained unused around every execution; the unsupported guest
call and changed typed ledger rejected. Test execution took about 491 seconds
plus seven seconds of namespace loading. This is a different workload from the
direct checks, not a speedup comparison or an additive coverage total.

At that boundary, general guest calls, symbol-let lowering, quoted containers, numeric-width
catch matching, Error hierarchy/subtyping, resource cleanup and complete
profile/target legality remain outside this subset. The runtime composition
uses nine exact original function definitions, not whole-runtime compilation.
Existing admitted packets and migration identities remain unchanged; this
does not establish authoritative migration, full SH-07/SH-08, self-hosting,
release support or seed retirement.

### Canonical immutable symbol-let lowering

The Gravity emitter now consumes C6 lexical-binding records and C7 inferred
local-binding facts. Each binding joins its source vector/name/initializer,
ordinal, original scope, prior-visible IDs, immutable binder and type fact.
Initializers lower under the outer and preceding bindings; a new binder is
introduced only afterward. Body lowering uses the completed local context
without mutating the outer one. Source operator, parent, exact record census
and body identities are checked as well as inferred types.

The existing resolver models repeated names in one vector as sequential
shadowing with distinct binding identities and scopes. Nested one-binding
runtime lets preserve that behavior while leaving the existing flat runtime
duplicate-label rejection unchanged. Empty binding vectors lower to body
sequence execution. The normal-result check respects a `Never` initializer,
rather than assuming the last body type proves a normal return.

The focused regression batch passed seven tests/470 assertions, followed by
the final let kernel at one test/60 assertions after an additional malformed
family check. These overlap and are not additive coverage. The initial draft
used unsupported `min`; its six-test run recorded 219 passes, seven failures
and four errors. The correction used explicit cardinality checks, not a new
host builtin or wider language allowance. Independent readiness review covered
the literal C6/C7 schemas and prepared real-source test before execution.

The first real composition failed during its first accepted assembly with
`typed-let-source-or-census-mismatch` (nine passed assertions and one error),
before runtime positives or the second extension. Static tracing independently
identified a sufficient rejection condition: the lowerer required the binding
vector container's scope to equal the owner scope, but the actual projection
leaves that container scope nil. C6 requires scopes for the owner, individual
initializers, binders and body, not the vector container. The normalized test
had fabricated that scope. The correction removes only this invented consumer
premise and uses nil in the normalized test, preserving all required scope
joins and the upstream language policy. The generic failure does not prove
it was the sole mismatch. A direct test of the saved projection helper bodies
also checks the real container/initializer/binder/body scope shape without
constructing an authority proof. Its first attempt could not resolve helper
Vars removed by normal bootstrap cleanup; that test error remains recorded.
The corrected focused batch passed eight tests/493 assertions, including 19
projection-schema and 63 let assertions. It does not replace real composition.

The corrected candidate's affected composition passed one test/132 assertions
in about 366 seconds of execution plus seven seconds of loading. Both source
extensions construct their accepted upstream input once and reuse it for all
nine functions, assembly and runtime checks. Actual self/forward-reference
rejections, nested one-binding census, original payload identity, shadowing and
outer restoration, multiple body forms, Never-initializer recovery, catch-local
aliases and exact public main results pass. Each public lowering operation
checks fresh current C7 verification; changed facts and lexical records reject,
and no guest execution uses the seed throw transport. This pass does not erase
the first failure or make overlapping focused counts additive.

Other call forms, closures, mutable locals, destructuring, quoted containers,
numeric coercion, complete effect/profile/target legality and authoritative
migration remain separate gates. Current-source lowering/execution does not
refresh old admitted compiler identities or establish self-hosting, release
support or seed retirement.

### Canonical direct same-module calls

The Gravity emitter now lowers fixed-arity direct calls between current-C7-
accepted named functions in one acyclic module. Canonical call/edge records,
operator binding, declaration/function/body identities, argument order, C7
constraints and call facts determine the target. Qualified references to the
same local binding emit the same target; no runtime string lookup is used.
Arguments with type `Never` make the call non-returning without changing the
callee's normal return declaration. Existing source and thrown-type facts are
retained; effect/profile authority stays pending.

The Gravity runtime carries the closed plan's function table and active call
path explicitly through one private evaluator/helper implementation. Legacy
entrypoints remain thin empty-context compatibility calls. Target shape,
parameter uniqueness, arity and active cycles reject before argument effects.
Arguments evaluate once, left-to-right, in the caller environment. Normal
values enter a fresh callee environment; thrown/rejected argument control
aborts the remaining arguments and invocation. Callee output and control return
unchanged. Fatal host failures are not converted into guest throw values.

The focused batch passed nine tests/592 assertions, including 55 direct-call
lowering and 63 runtime assertions alongside overlapping regressions. Earlier
stale unsupported-call oracle failures and a new-test reader EOF remain failed
attempts. Qualified-reference and declaration/binding identity checks were
reviewed before the real composition, not repaired by relaxing authority.

The sole affected paired composition passed one test/214 assertions in about
869 seconds of execution plus eight seconds of loading. Each accepted input is
constructed once per extension and reused for thirteen-function assembly,
public-function parity and runtime cases. Real chains, same-module qualified
references, let aliases, original payload identities, caller catches, callee
rethrow and first-throwing-argument abort pass. The actual C5 unknown-call and
C7 wrong-arity, wrong-type and nonfunction calls reject. Each public lowering
operation checks fresh current C7 verification; coherent input mutations reject
and the seed throw transport is never used by guest execution. These results
do not make overlapping focused counts additive or compare equivalent timing
workloads.

The current consumer's 256-entry active-path limit is not a language legality
rule or proof of host-stack, total-cost or memory fit. Target body checks are
shallow. The runtime test projection contains 16 exact original definitions,
not the whole runtime module. Existing cross-plan continuation machinery and
admitted identities are unchanged. Imports, recursion, callable parameters,
closures, variadics, full profile/provider legality, authoritative migration
and seed retirement remain open.

Qualified declarations are a separate existing C7 gap: macro/C5/C6 preserve the
original qualified declaration but C7 requires equality with the simple binding
name before typed acceptance. The new lowerer does not claim to fix this
upstream mismatch or turn it into a language-law prohibition. A future C7
identity-join correction needs its own accepted/rejected source cases and
affected validation; qualified references are already a distinct supported
case here.

### Canonical loop-target execution

The current development candidate passed the six-input affected composition:
one test/128 assertions, with a real multi-function `.qst` compile, assembly and
execution, both decimal diagnostic boundaries and three rejected `.qst` inputs.
The selected operations took 586.919 seconds; command elapsed time was 594.656 seconds.
The focused batch passed twelve tests/750 assertions. These are ordinary
development results, not authoritative migration or whole-language completion.
The failed and interrupted attempts below retain their original status.

The current Gravity lowerer and local runtime implement sequential loop
initialization, left-to-right recur arguments evaluated in the old environment,
and simultaneous slot replacement. Canonical C6 target/binding/transfer records
and C7 slot/constraint rows are consumed without changing the public lowering
or execution interfaces. The runtime uses a separate target-bearing control
position, not payload recognition; nested targets, non-tail and try barriers,
callee isolation, source errors and original host failures remain distinct.

The focused batch passed eleven tests/713 assertions, including 57 normalized
lowering and 64 runtime assertions. Current C7 constructs the normalized test
rows; this is not source authentication. The duplicate-index rejection oracle
was corrected to inspect the raw helper result, with a positive index check.
These counts overlap earlier batches and are not additive.

The first real composition failed: one test, 25 passed assertions, one failure
and one error. The unchanged `loop-recur-types.gravity` case completed; the
new `local-runtime-loop.gravity` input was rejected by C7 before lowering, and
the remaining ten inputs did not run. Its diagnostic was `C7-ANNOTATION`,
`:unsupported-throwing-body-operation`, with actual operation `:loop`.
Throws and handlers activate a source-error graph whose operation subset
excluded `loop` and `recur`; this does not establish that an ordinary loop call
alone triggers that graph. The failed run remains failed, not a passing loop
and handler composition.

The bounded C7 correction admits `loop` and `recur` to the existing lexical
source-error union. It adds no control backedge or thrown type for a transfer.
The preceding canonical target/slot/transfer validation, handler order, `Never`
behavior, work limits and fresh reconstruction are unchanged. Fourteen focused
tests/227 assertions pass, including handler-only activation, called-body
propagation and independent handler reconstruction negatives. Four affected
lowering/orchestration tests/170 assertions also pass. These batches overlap
earlier coverage and are not additive. Independent review accepted the changed
implementation for composition. That second run completed the unchanged loop
and new main `.gravity` fixtures, including loop/throw/catch execution, then
stopped on `local-runtime-loop-decimal.gravity`: `C7-VERIFY` reported
`:stale-b47-identity-preimage` before lowering. Its aggregate remains failed
(99 passed assertions, one failure, one error); paired `.qst` and negative
inputs were not reached. The mismatch was traced to the host exporter converting
only canonical decimal node values to `BigDecimal` after their descriptor-bearing
identity preimage was resolved. The exporter now retains the original exact
descriptor, including scale and C2 occurrence links. Its existing explicit
presentation helper remains available but is not canonical input. C7 equality
checks are unchanged. The focused actual-helper/C7 kernel passed 27 assertions;
two affected lowering/orchestration tests passed 67 assertions. These normalized
controls are not authenticated source evidence. Independent review accepted the
correction and the remaining-input selector, which constructs the other ten
inputs freshly without replaying the two successful nondecimal `.gravity`
cases. Those earlier successes retain their pre-correction snapshot attribution;
they are not final-current authoritative identities. That remaining-input run
was interrupted: its JVM is gone, its timing receipt is absent, and no final
aggregate or native exit status was recovered. Retained output contains eight
assertion failures, four for each decimal source extension. C7 accepts both;
lowering rejects earlier than expected because it compares the public raw
function body with C6's descriptor-bearing normalized body. Ordered partial
coverage is not a completed batch; completion of the last three negative `.qst`
inputs is unobserved. The implemented fix joins verified raw roots to C6 root
IDs in source order for definition/table fidelity while compiling only typed
nodes. Its focused batch passed twelve tests/750 assertions, including 37
source-join assertions for decimal fidelity, malformed roots and headers,
definition/table equality, work bounds and ordinary/annotated parity. Those
normalized checks are not source authentication. The affected real-input scope
was one multi-function positive, both decimal extensions and the three unfinished
negative `.qst` cases. This changed-candidate run passed all 128 assertions,
including precise numeric node/syntax/origin/span rejection, current C7
verification once per public operation, unchanged source/ledger tamper rejection
and zero seed throw transport. The original frozen source, test and fixture
inputs remained unchanged. No interrupted or failed aggregate is relabeled
passed, and no full twelve-input final-candidate matrix is claimed.
At that loop-only checkpoint, function-target runtime recur remained open.
Its twenty-definition evaluator projection was development-only. The following
increment adds function-target execution, not complete effect/profile legality,
whole-runtime compilation or authoritative migration.

### Canonical function-target recur execution

The current Gravity emitter and runtime now lower and execute fixed-arity
function recur using the original C6 function target, ordered parameter bindings,
explicit known C7 parameter slots and transfer ledger. Recur arguments run
left-to-right in the old environment. A matching backedge replaces all parameters
together in a fresh local environment, drops prior body locals, preserves output
and retains the same call path. Ordinary recursive calls retain their cycle
restriction. Loop targets, tail/try barriers and caller/callee isolation remain
distinct. Called-function metadata is validated before argument evaluation;
legacy zero-argument entry records may omit parameter/arity fields, but present
malformed fields are not defaulted away.

The full source/header join dominates function compilation once. Independent
review caught a new per-transfer repeat of that join whose source-sized work was
not covered by the precharge. The correction reuses existing active-function
records and C7 parameter facts, retaining target, declaration, ordered binding
and current-type checks without a cache, flag, public input or increased cap.
The final current-source regression passes fourteen tests/861 assertions,
including 48 function-target lowering and 63 runtime assertions. These cover
the exact transfer budget, one-less rejection, no-repeat source control and
parameter tampering. The smaller affected batches overlap this regression;
their counts are not additive. Earlier projection-census and test-syntax
failures remain failed observations.

The affected composition passed once: one test/276 assertions, six freshly
constructed source inputs, with all frozen code/test/fixture bytes unchanged.
Both extensions of the existing annotated function fixture return the original
String through a function restart and an ordinary guest call. Both extensions
of the new ten-function fixture exercise parameter swaps, shadowing, fresh
locals, nested loop/function targets, first-argument throw abort, throws after a
restart, caller catches preserving payload identity, a terminating zero-slot
case, and the unchanged seven-key main execution record. The existing rejected
slot-type pair reports the precise C7 String-versus-Bool mismatch and rejects
public lowering. Target, tail and ledger mutations reject; each public
operation verifies with current C7 once; no seed throw transport is used.
These mutations are not claimed as coherently resealed adversarial products.
The zero-slot case checks lowering, the empty ledger slots and normal entry;
its false branch does not execute a zero-arity backedge. Rejection of leaked
prior body locals is covered by the normalized runtime kernel, not by an
out-of-scope reference in a source fixture.

Selected execution took 660.745 seconds and the completed command took 668.622
seconds with a 2 GiB heap. This includes real construction and verification,
not just scheduling; it does not identify a dominant phase or establish a
speedup. The direct commands are in `docs/development-verification.md`.
The twenty-two original-source runtime functions are a development projection,
not a compiled or admitted whole runtime. Arbitrary recursive calls, closure
execution, full effect/profile/provider legality, termination and total-cost
proofs, authoritative migration, target completion, self-hosting and seedless
release remain outstanding. No unchanged expensive proof was replayed to repair
the cheap tests or documentation.

### Bounded test-construction cleanup

A subsequent test-only change removes duplicate work without changing compiler
or fixture bytes. All six SH01 type-only helper callers still construct genuine
fresh C5/C6/C7 products, but omit the unused SH09 binding seal. C8's existing
helper adds the unchanged seal and preserves its exact preimage and consumer
arguments. The function-recur selector retains every assembled-function and
runtime check, one standalone public compile per accepted input, the rejected
pair and all hostile cases. Its static public verification schedule falls from
36 to 16; each retained public operation still verifies freshly.

Helper flow checks pass two tests/27 assertions. Schedule, public-boundary and
existing recur checks pass four tests/135 assertions; these overlap the earlier
semantic regression and are not extra language coverage. The accepted actual
one-test/276-assertion composition remains attributed to the original test
revision in `55b7dca9`, not to a replay of the revised harness. There is no
measured speedup, new semantic feature or migration claim. The direct commands
and distinctions are in `docs/development-verification.md`.

## Current Baseline

The repository has a working Clojure stage0, Gravity-authored compiler source
models, bounded stage2/stage3 application paths, and authenticated operation
slices through selected C, LLVM, and Wasm paths. The current governing evidence
still records:

- `:full-language-compiler-self-hosted? false`;
- `:clojure-seed-retired? false`;
- `:clojure-seed-boundary? true`.

Consequently, proof records, source inventories, public `check` bridges, and a
stage3 application demonstration do not by themselves complete a slice below.
A slice completes only when its named behavior is executed by the claimed
Gravity implementation and passes its exit gate.

## Uniform Slice Gate

Every slice from `SH-03` onward must satisfy all applicable items below before
it changes to `complete`:

1. The governing documents and exact claimed subset are named.
2. The implementation is executable Gravity behavior, not only a source model,
   manifest, proof record, fixture classifier, or Clojure implementation.
3. At least one real accepted `.gravity` input and its co-canonical `.qst`
   input traverse the slice.
4. At least one real rejected input per rejection family produces a stable
   structured diagnostic with the actual source path and no raw host exception.
5. Source spans, syntax identity, preserved compiler facts, effects,
   capabilities, safety classifications, and provenance survive as required by
   the governing pass contracts.
6. Artifact identities are deterministic and checkout-path neutral while
   retaining actual-path provenance outside identity inputs.
7. Mutation, substitution, stale-input, and malformed-graph probes fail closed
   at authenticated boundaries.
8. Focused tests, relevant regression tests, document validation, roadmap
   validation, coverage self-tests, and `git diff --check` pass.
9. The evidence names the remaining trusted computing base and makes no broader
   profile, target, release, performance, safety, or self-hosting claim.

Target-emission slices additionally require execution or validation by an
independent target toolchain. Bootstrap slices additionally require canonical
stage comparison, reproducible rebuild, and compiler-lineage evidence.

### P15-S23 Darwin launcher prerequisite

The bounded host-authored Darwin launcher is tracked as a prerequisite for the
P15-S23 native/runtime boundary, not as completion of a self-hosting slice. Its
development-verification node is `stage0-p15-native-launcher-prerequisite` and
runs `clojure -J-Xmx1g -M:test --namespace gravity.p15-native-launcher-test`
with Xmx1g, a 600-second timeout, fresh/no-resume execution, and
`authority: none`. The parent verifier owns the canonical
`/private/tmp/gravity-sh07-heavy.lock` (`exclusive: true`, `capacity: 1`,
`lock_owner: runner`) and records the observed process-tree resource receipt.
The node binds the launcher source, exact test, five C fixtures, and
`docs/artifacts/phase-15/native-launcher/p15-s23-darwin-launcher-primitive.edn`,
plus only the `deps.edn` and self-hosting test-runner tool inputs required by
the command identity. Legacy Stage0 broad owners exclude these paths, so each
source, test, fixture, or artifact change routes to the focused node and the
cheap `stage0-orchestrator-unit` prerequisite without selecting the broad
Stage0 suite or an earlier/later proof candidate.

Coordinator evidence is exactly 8 tests/60 assertions in 5.983s with peak RSS
213,712,896B under Xmx1g and the canonical lock; independent review is
approved and the artifact status is `partial`. This evidence is bounded and
non-authoritative. It makes no public native-run, self-hosted, release, or
strict process-containment claim, and does not change the SH-19 or P15-S23
completion state.

### P15-S23 native runtime provider prerequisite

The bounded native runtime provider is tracked as a narrow internal
prerequisite, not as a completed self-hosting slice or general native-target
support. It has two parallel fixed profiles. The fast
`stage0-p15-native-runtime-provider-contract-prerequisite` uses the exact
`gravity.self-hosting.sh07-iteration-cache-runner` command with explicit source
and test paths, `--fail-fast`, ten ordered vars (artifact preflight first),
`--max-cache-entries 1`, Xmx1g, and a strict 180-second timeout. It owns the
four shared C/Gravity/test/artifact inputs and the exact 22 legacy fixtures
(26 primary inputs), with an expected 10-test/235-assertion profile.

The authenticated
`stage0-p15-native-runtime-provider-packet-binding-prerequisite` uses the same
runner contract with five ordered vars (packet-binding preflight first),
Xmx8g, and a strict 1,800-second timeout. It owns the four shared inputs, the
exact packet binder, and four new fixtures (9 primary inputs), with an expected
5-test/70-assertion profile. Both nodes are fresh/no-resume, automatic,
`authority: none`, and depend only on `stage0-orchestrator-unit`; deterministic
IDs place contract before packet binding for shared changes. Explicit
`--check` closes only the selected profile plus the orchestrator; `--all`
includes both.

Both use `/private/tmp/gravity-sh07-heavy.lock` with `lock_owner: runner`,
`exclusive: true`, `capacity: 1`, and the observed process-tree receipt. Their
exact shared tool closure is `deps.edn`, the iteration-cache runner, the
self-hosting runner, `bootstrap.clj`, the packet binder, and its six direct
Gravity dependencies. Shared inputs, the binder, and eager namespace-load
helpers select both profiles; old fixtures select only contract, and the four
authenticated fixtures select only packet binding. Existing bootstrap and
six-helper consumers retain their prior Stage0/Stage3/Stage4 routes in addition
to both profiles. Exact exclusions remove provider-specific fixture and binder
replay without replacing genuine helper ownership. No M0, proof, public,
self-hosted, release, seedless, or containment authority is implied by either
provider profile.

The manifest binds exactly `GRAVITY_P15_NATIVE_RUNTIME_REQUIRED=1`. Without the
marker, a direct namespace run retains its unsupported-platform/no-claim skip;
the focused verifier supplies the marker and fails if the ARM64 macOS
Clang/file toolchain is unavailable. Each profile independently performs a
bounded no-follow, strict-UTF8, single-form artifact read with identity/size
checks and current C/Gravity/test hashes; packet binding also validates its
adapter and four authenticated fixtures. The artifact's current source-only
census is 15 tests/305 assertions. The prior ae9f 13-test/303-assertion
receipt, timings, RSS, hashes, and attempt history are explicitly historical
and non-authoritative, not a current runtime measurement. This evidence does
not grant public native execution, self-hosting, release, seedless, or strict
OS-containment authority.

## Countable Backlog

Status meanings:

- `partial`: useful implementation evidence exists, but the exit gate is not
  met.
- `queued`: no current claim is made that the executable exit gate is met.
- `blocked`: its dependency gate is not yet complete.
- `complete`: all entry, exit, validation, and evidence gates passed. Only the
  coordinator may assign this state after reviewing the evidence bundle.

| ID | Slice | Depends on | Current state | Executable exit gate |
| --- | --- | --- | --- | --- |
| `SH-00` | Stabilize the current authenticated Wasm comparison work and restore a clean baseline | none | complete | The current bounded C11-C14-B1-B4 comparison path is committed, externally executed, adversarially reviewed, and the worktree is clean. |
| `SH-01` | Establish parallel-safe module and test ownership boundaries | `SH-00` | complete | Central routing, compiler modules, target modules, fixtures, tests, generated evidence, and coverage accounting have explicit single owners; new tests no longer require unrelated edits to the monolithic bootstrap test file. |
| `SH-02` | Generalize authenticated pass and artifact envelopes | `SH-00` | complete | One reusable contract verifies source revision, semantic inputs, preserved facts, artifact lineage, deterministic identity, graph bounds, and stale or substituted inputs across at least two different compiler stages. |
| `SH-03` | Complete the bootstrap reader and literal policy | `SH-01`, `SH-02` | complete | The Gravity reader, rather than a Clojure reread, handles the complete claimed bootstrap syntax and literal subset with Unicode, newline, delimiter, abbreviation, metadata, extension, and malformed-literal diagnostics. |
| `SH-04` | Complete syntax objects, hygiene, and origin chains | `SH-03` | complete | Gravity constructs and serializes syntax objects with stable identity, scopes, marks, metadata, source spans, generated origins, and adversarial graph validation. |
| `SH-05` | Implement the bootstrap macroexpander in Gravity | `SH-04` | complete | Gravity expands the macro subset required by all compiler sources, preserves hygiene and origins, rejects phase/profile/capability violations, and matches accepted stage0 diagnostics. |
| `SH-06` | Implement namespace and binding resolution in Gravity | `SH-05` | complete | Gravity resolves compiler namespaces, aliases, vars, lexical bindings, imports, visibility, cycles, and unresolved references for the complete bootstrap source set. |
| `SH-07` | Implement core-form semantics and lowering in Gravity | `SH-06` | partial | Every core form required to compile the compiler lowers to a canonical core artifact with stable evaluation order, arity, mutation, recursion, exception, and pattern diagnostics. |
| `SH-08` | Implement the bootstrap type checker in Gravity | `SH-07` | partial | Compiler sources receive genuine type facts for primitives, functions, collections, control flow, calls, records/unions, and required meta-programming values; ill-typed variants fail closed. |
| `SH-09` | Implement effect, capability, and profile checking in Gravity | `SH-08` | partial | The compiler's `:meta` and release-target paths reject undeclared effects, missing authority, ambient access, hidden services, and illegal profile/target assumptions before backend lowering. |
| `SH-10` | Implement ownership, lifetime, initialization, and memory checks in Gravity | `SH-08` | partial | The bootstrap subset has explicit ownership and memory facts, rejects invalid aliasing, use-after-move, uninitialized values, invalid escapes, and unsupported allocation regimes, and preserves facts into MIR. |
| `SH-11` | Implement the safety and numeric-safety classifier in Gravity | `SH-09`, `SH-10` | partial | Each dangerous operation reaches exactly one D8 outcome; bounds, overflow, division, casts, unsafe islands, and residual checks are represented in checked core and diagnostics. |
| `SH-12` | Implement MIR construction and verification in Gravity | `SH-11` | partial | Gravity builds verified C11 MIR for the whole bootstrap subset, including blocks, values, calls, branches, loops, errors, effects, ownership, safety facts, and source provenance; malformed MIR is rejected. The fixed Stage7 development lane checks the frozen 253,588-byte C11 source and defines a six-selector C10-to-MIR adapter batch (helper first, authenticated boundary last). The aggregate receipt at `target/validation/stage7-c11-post-native-3/receipt.json` passed non-authoritatively in 490866.529 ms under the canonical command-owned lock: source 3/62 (runner 298 ms, wrapper 6722.023 ms, peak 522,780,672 bytes), public 2/39 (runner 350265 ms, wrapper 359909.526 ms, peak 2,789,851,136 bytes), and the exact combined adapter 6/235 (runner 80730 ms, wrapper 86737.375 ms, peak 1,892,941,824 bytes). All exited 0 with no skipped selectors and `authority: fresh-command-pass-non-authoritative`. These measurements were fresh on the prior 6084-based composition; coordinator changes since then alter the exact Stage7 tool input `bootstrap/clojure/test/gravity/self_hosting_test_runner.clj`, so this is historical non-authoritative planning/performance evidence, not current admission evidence. It is not proof, reviewed attestation, scoped authority, or release evidence; no C11 proof candidate was rerun. The final exact seven-node rerun is pending coordinator C12/SH13 freeze. The earlier separate 1/53 helper and 5/182 suffix receipts are superseded planning evidence. |
| `SH-13` | Close functions, calls, recursion, and control flow through MIR | `SH-12` | partial | Multiple functions with typed parameters and results, direct and indirect calls required by the compiler, recursion, branches, loops, joins, and error exits execute through verified MIR. The fixed Stage8 development graph adds a 512 MiB C12 source-shape gate, one cache-affine six-selector C11-domain evidence batch at 8 GiB, and one 2 GiB public C12 selector. All are automatic non-authoritative development checks on the canonical capacity-one lock; there is no C12 proof node, and the exact combined graph still requires a fresh post-composition run. |
| `SH-14` | Close compiler-required data, layout, and allocation through MIR | `SH-12` | queued | The bootstrap subset can construct and access the strings, symbols, keywords, tuples, records, variants, maps, sets, vectors, and bounded mutable storage used by the compiler with explicit layout and allocation facts. |
| `SH-15` | Complete compiler diagnostics as Gravity data | `SH-13`, `SH-14` | partial | All bootstrap-stage rejection families originate from Gravity diagnostic construction and preserve stable ids, facts, spans, origin chains, severity, and remediation across stage comparison. |
| `SH-16` | Implement required MIR optimization passes in Gravity | `SH-15` | partial | Gravity executes the passes needed by the bootstrap compiler, verifies declared preserved/invalidated/regenerated facts, and never erases a check without surviving proof or a residual runtime check. The fixed evidence-only Stage9 graph runs the two C13 source-shape selectors at 512 MiB, then four cache-affine C12-to-C13 boundary selectors at 8 GiB. C13/SH16 changes do not force Stage8 production; both nodes are automatic non-authoritative checks with no proof, public, lowering, optimization-credit, self-hosting, or release claim. Historical direct-command measurements are 2/41 in 1.187 seconds at 111,738,880-byte peak RSS and 4/69 in 60.797 seconds at 1,602,322,432-byte peak RSS; they are not current wrapper receipts, and a composed receipt-bearing run remains pending. |
| `SH-17` | Complete the target-lowering and backend interface boundary | `SH-16` | partial | A verified, authenticated, target-independent MIR artifact is accepted by a reusable C14/B1 interface that selects a declared target, ABI, runtime, providers, and artifact policy without source reinterpretation. The fixed Stage10 development graph runs a 22-selector static admission batch, one automatic direct carrier-mutation discriminator, an independent one-selector SH25 inventory node, and a three-selector SH25/SH26 fixture-consumer branch. The costly packet-substitution matrix remains manual and follows direct mutation in one cache-affine JVM. All nodes are non-authoritative; historical W1 measurements guide resource bounds but do not establish current admission, proof, attestation, release, or generalized performance claims. |
| `SH-18` | Emit and run one complete native compiler target | `SH-17` | queued | A genuine `:native` program containing typed arguments, arithmetic, functions, collections, and explicit stdout lowers through C or LLVM to an independently verified and executed native artifact. |
| `SH-19` | Implement the minimal self-hosting runtime subset | `SH-11`, `SH-17` | queued | Startup, panic/error reporting, bounded allocation or explicit no-allocation paths, strings/bytes, file reads required by the compiler, stdout/stderr, and process exit are provided through declared effects and capabilities outside Clojure. |
| `SH-20` | Implement release-grade artifact emission and provenance | `SH-17` | partial | Gravity emits objects/executables, source maps, manifests, compiler identities, dependency graphs, safety summaries, and canonical provenance consumable by independent tools. |
| `SH-21` | Make the compiler source legal and executable under `:meta` | `SH-15`, `SH-17`, `SH-19` | queued | Every authoritative compiler module compiles under the declared `:meta` contract with no ambient filesystem, network, shell, registry, or undeclared host authority. |
| `SH-22` | Implement the bootstrap standard-library core | `SH-13`, `SH-14`, `SH-19` | queued | The Gravity implementations of collections, text, numeric helpers, errors, IO wrappers, artifact data, and compiler meta-programming helpers needed by the compiler pass conformance and safe-wrapper gates. |
| `SH-23` | Implement the bootstrap package and hermetic build subset | `SH-20`, `SH-21`, `SH-22` | queued | Gravity reads the project/lock inputs, resolves the fixed bootstrap dependency graph, enforces build effects and capabilities, and deterministically builds compiler components without Clojure orchestration. |
| `SH-24` | Implement the self-hosted compiler driver and CLI subset | `SH-18`, `SH-19`, `SH-20`, `SH-23` | queued | A Gravity executable owns source loading, pass orchestration, diagnostics, target selection, artifact writing, `check`, `compile`, and the compiler-internal verification commands needed for bootstrap. |
| `SH-25` | Compile every authoritative compiler component with Gravity | `SH-21`, `SH-22`, `SH-24` | queued | The Gravity compiler builds the reader, syntax, macro, analyzer, checked-core, MIR, optimizer, lowering, backend, diagnostic, runtime-subset, standard-library-subset, and package/build components from their Gravity sources. |
| `SH-26` | Rebuild the complete bootstrap compiler with the prior Gravity stage | `SH-25` | blocked | Stage N compiles the complete stage N+1 compiler and its required runtime/library/build components without invoking Clojure in the candidate compiler, runtime, or build path. |
| `SH-27` | Prove stage equivalence and deterministic fixed-point behavior | `SH-26` | blocked | Two clean rebuilds and the stage N/stage N+1 comparison pass the declared artifact, manifest, diagnostic, conformance, runtime-output, IR-modulo-id, and reviewed-delta modes. |
| `SH-28` | Close trusting-trust, provenance, and TCB evidence | `SH-27` | blocked | Diverse or independently anchored rebuild evidence, acyclic compiler lineage, environment/lock/build identities, SBOM, signatures, revocation checks, unsafe audit, and a TCB delta show the Clojure seed outside the release boundary. |
| `SH-29` | Emit and verify the public seedless Gravity release | `SH-28` | blocked | The public `gravity` binary builds and verifies the compiler, runtime subset, standard-library subset, package/build path, and release executable; full bootstrap conformance passes; final artifacts record `:full-language-compiler-self-hosted? true`, `:clojure-seed-retired? true`, and `:clojure-seed-boundary? false`. |

Count: 30 slices (`SH-00` through `SH-29`). The first self-hosting claim is
permitted only after all 30 are complete. A task may be implemented early, but
it cannot be credited before its dependencies and uniform gate pass.

## Dependency Waves and Parallel Ownership

The backlog permits parallel implementation while retaining one integration
owner:

| Wave | Coordinator-owned integration | Parallel lane A | Parallel lane B | Parallel lane C |
| --- | --- | --- | --- | --- |
| 0 | `SH-00` to `SH-02`; central bootstrap routing and coverage | Read-only adversarial review | Native toolchain fixture design | TCB and equivalence inventory |
| 1 | `SH-03`, `SH-04`; source/syntax integration | `SH-05`, `SH-06` after syntax contracts stabilize | `SH-08` type data model and fixtures | `SH-18` native ABI/toolchain harness without shared lowering edits |
| 2 | `SH-07`, `SH-12`; checked-core/MIR integration | `SH-08`, `SH-09` | `SH-10`, `SH-11` | `SH-19`, `SH-20` runtime/artifact leaf modules |
| 3 | `SH-15` to `SH-17`; pass and backend integration | `SH-13` functions/control | `SH-14` data/layout | `SH-18` native emission |
| 4 | `SH-21`, `SH-24`; compiler-driver integration | `SH-22` standard-library core | `SH-23` package/build subset | Stage comparison and mutation suites |
| 5 | `SH-25` to `SH-29`; bootstrap/release authority | Independent rebuild | Diverse verification | Provenance, SBOM, signing, and governance review |

The coordinator exclusively owns shared dispatch, generated release artifacts,
coverage accounting, completion attestations, and final status changes.
Parallel workers own leaf Gravity modules, new fixtures, and dedicated test
namespaces. A worker must not edit another lane's files or regenerate global
artifacts.

## Progress Accounting

Progress is reported as four numbers rather than a percentage inferred from
commit count:

```text
complete slices / 30
executable Gravity-owned slices / 30
slices with Clojure in their claimed execution boundary / 30
blocked slices / 30
```

The coordinator should also report the highest continuous completed prefix.
For example, completion of `SH-18` does not advance the prefix past `SH-07` if
`SH-08` remains incomplete.

Current reviewed accounting after `SH-06`:

```text
complete slices: 7 / 30
executable Gravity-owned slices: 6 / 30
slices with Clojure in their claimed execution boundary: 7 / 30
blocked slices: 4 / 30
highest continuous completed prefix: SH-00 through SH-06
```

`SH-02` credit is intentionally bounded to envelopes whose explicit reference
closure has at most 128 nodes, 128 edges, and shortest-discovery depth 64. The
Gravity module owns descriptor validation, semantic template construction, and
fresh template replay for both C13 and B1. Canonical encoding, SHA-256 request
resolution, identity equality enforcement, source reads, and final contextual
reconstruction remain in the declared Clojure stage0 boundary; `SH-02` does not
retire that seed or claim release-signature or verifier-correctness proof.

`SH-03` credit is bounded to the bootstrap reader and literal subset exercised
by its co-canonical fixture matrix and authoritative reader-source proof. The
Gravity reader owns Unicode-scalar decoding, newline and delimiter structure,
the ordered token stream, recursive form graph, bootstrap literals,
abbreviations, metadata, registered `inst` and `uuid` extensions, bounded
semantic-value reconstruction, and structured rejection. The public read, C2,
C3, P15, and check ingress consumes the authenticated Gravity result without a
second target-source read. The Clojure stage0 boundary still owns the initial
byte snapshot, compiler-plan execution, canonical digest resolution, the C2
compatibility adapter, central routing, and final artifact construction. The
compatibility facade is explicitly uncredited, and the remaining C2 boundaries
are the full-language literal surface, full-language abbreviation surface,
full-language extension registry, and host/seed retirement. This slice does not
claim overall C2 completion, compiler self-hosting, seed retirement, or release
readiness.

`SH-04` credit is bounded to the syntax-object, hygiene, metadata, source-span,
generated-origin, serialization, and graph-validation subset exercised by its
co-canonical fixture matrix. The Gravity syntax module constructs and verifies
the claimed products, preserves deferred ratio descriptors and exact SH-03/P15
reader lineage, and emits checkout-path-neutral syntax and artifact identities
while retaining actual paths in provenance. The SH-03 descriptor is freshly
verified through the reusable SH-02 envelope contract and independently bound
back to the current C2 semantic products; paired provenance alteration,
same-path cross-source replacement, stale-input, and malformed-graph probes
fail closed. The Clojure stage0 boundary still owns compiler-plan execution,
digest resolution, envelope binding, the C2/C3 compatibility adapter, and
central routing. This slice does not claim overall C3 completion, full compiler
self-hosting, seed retirement, package refresh, or release readiness.

`SH-05` credit is bounded to the compiler-required `defn` expansion subset
executed across the 36 authoritative compiler modules and their measured 1,276
top-level function definitions. The Gravity macro module owns expansion
recipes, hygiene marks, generated origins, build-effect and capability policy,
ordered expansion traces, and all ten structured C4 rejection families. Exact
stage0 body comparison, both source extensions, path-neutral identities,
literal-map preservation, replay and substitution checks, and complete
first/middle/last stream reconstruction passed. SH-04's implementation-local
syntax-stream ceiling is now 2,048 products, the exact capacity of its current
16-item chunks under the canonical width limit of 128; 2,049 is rejected before
downstream use, and aggregate stream carriers are checked separately. The
Clojure stage0 boundary still owns source reads, compiler-plan execution,
canonical digest resolution, envelope binding, compatibility routing, and final
artifact assembly. User-defined `defmacro`, the full macro surface, hierarchical
syntax-stream paging beyond 2,048 products, retained standalone carrier-audit
records, compiler self-hosting, seed retirement, packaged CLI refresh, and
release readiness remain outside this credit.

`SH-06` credit is bounded to the namespace and binding resolution subset
executed across the measured 41-path authoritative compiler-source cohort and the
co-canonical accepted/rejected fixture matrix. The Gravity
`gravity.resolution` module owns namespace, alias, import, var, lexical
binding, visibility, shadowing, dependency-cycle, profile, target, capability,
and foreign-boundary policy; deterministic binding and product identities; and
all ten structured C5 rejection families. Fresh authenticated SH-05 input,
exact Gravity replay, complete resolution-product reconstruction,
checkout-path-neutral identities with actual-path provenance, substitution and
mutation rejection, public C6 consumption, and bounded serialization passed.
The largest measured authentic artifact contained 19,445,399 carrier nodes at
depth 45 and maximum width 66,339 and serialized to 284,437,819 UTF-8 bytes.
Per-component transport limits are 33,554,432 nodes, depth 64, and width
131,072; whole-artifact limits are 67,108,864 nodes, depth 64, width 131,072,
and 1,073,741,824 serialized bytes. The Clojure stage0 boundary still owns initial
source reads, compiler-plan execution, canonical digest resolution,
authenticated-envelope binding, compatibility adaptation and central routing,
and final artifact assembly. That 41-module cohort was the measured bootstrap
source set at credit time; the current inventory contains 42 authoritative
modules after the additive B48 source proof. Neither count is a broader
full-language claim, and the compatibility C5 facade
receives no implementation-authority credit. This slice does not claim overall
C5 or C15 completion, full compiler self-hosting, Clojure seed retirement,
packaged CLI refresh, or release readiness.

`SH-07` remains partial. Its source-level authenticated evidence now covers all
42 authoritative compiler modules, including the complete bootstrap reader
source. For that reader source's earlier gate, Gravity consumes the authentic
SH-06 fragment, binding, and resolution products for 22,209 forms in 298
fragments, lowers 20
qualified definitions and 30 qualified calls, and preserves 24 quoted forms as
data rather than executable references or calls. The measured authenticated
request contains 1,446,007 carrier nodes at depth 25 and maximum width 22,209;
its exact UTF-8 scalar payload is 24,579,750 bytes. The generated core template
contains 9,172,831 nodes and 522,603,428 scalar bytes, and its generated digest
requests contain 6,695,903 nodes and 372,860,236 scalar bytes. Request ingress
is bounded at 8,388,608 nodes and 268,435,456 scalar bytes; template and
resolved-core output are independently bounded at 16,777,216 nodes and
1,073,741,824 scalar bytes; generated digest output is independently bounded at
16,777,216 nodes and 1,073,741,824 scalar bytes. All four boundaries also enforce
depth 256 and width 65,536. Request ingress is checked before lowering, and
each generated product is checked before downstream recursive hashing,
verification, or use. Stable path-neutral core identities, actual-path
provenance, fresh digest resolution, replay, and altered input, graph, binding,
resolution, template, digest, and resolved-product rejection are required by
the authoritative proof. The Gravity C6 core-lowering source is also covered
directly: its 9,109 source bytes authenticate into 7 fragments containing 348
forms, 289 bindings including 27 local bindings, and 13 resolutions, then lower
to 220 canonical core nodes with 7 definitions and no calls, references, or
keyword lookups. Its cache-free authoritative proof passed with 32,888 carrier
nodes at depth 8, width 348, and 498,205 UTF-8 scalar bytes; co-canonical
`.gravity` and `.qst` inputs retain equal semantic identities and distinct
actual-path provenance. This independent zero-lookup source does not broaden
the existing nine-module, 63-lookup B16 cohort. The Gravity L2 core-language
semantics source is covered by the same path: its 17,557 bytes authenticate
into 9 fragments containing 600 forms, 286 bindings including 24 local
bindings, and 15 resolutions, then lower to 465 canonical core nodes with 9
definitions and no calls, references, or keyword lookups. Its cache-free
authoritative proof passed with 44,353 carrier nodes at depth 8, width 600, and
706,674 UTF-8 scalar bytes; its co-canonical source-extension and provenance
gates also passed. The C4 compatibility source adds 4,053 authenticated source
bytes in 4 fragments containing 232 forms, 270 bindings including 8 local
bindings, and 70 resolutions. It lowers to 201 canonical core nodes with 4
definitions, 31 calls, 50 references, 13 conditionals, and 3 functions. Its 11
`get` operators are ordinary resolved symbol calls rather than keyword-headed
map lookups, so its canonical keyword-lookup product count is zero. The
cache-free authoritative proof passed with 29,163 request carrier nodes at
depth 17, width 270, and 427,463 UTF-8 scalar bytes. Co-canonical extension,
provenance, replay, and alteration gates passed, and the B16 cohort remains
unchanged. The C3 compatibility source adds 4,149 authenticated source bytes in
4 fragments containing 238 forms, 270 bindings including 8 local bindings, and
70 resolutions. It lowers to 207 canonical core nodes with 4 definitions, 31
calls, 50 references, 13 conditionals, and 3 functions. Its 11 `get` operators
are likewise ordinary resolved symbol calls with zero keyword-lookup products.
The cache-free proof passed with 29,439 request carrier nodes at depth 17,
width 270, and 432,437 UTF-8 scalar bytes. The source remains explicitly
compatibility-only: it grants no authentication credit, does not supply the
authoritative syntax result, and retains the `gravity.bootstrap.syntax` route.
The C5 compatibility source adds 4,154 authenticated bytes with the same
4-fragment, 232-form, 270-binding, 70-resolution structure as C4. It lowers to
201 canonical core nodes with 4 definitions, 31 calls, 50 references, 13
conditionals, and 3 functions. Its 11 `get` operators are ordinary resolved
calls with zero keyword-lookup products. Its cache-free proof passed with
29,163 request carrier nodes at depth 17, width 270, and 428,299 UTF-8 scalar
bytes. The source remains compatibility-only, denies authentication and
authoritative resolution credit, and retains the `gravity.resolution` route.
The C15 diagnostics source adds 6,580 authenticated bytes in 5 fragments
containing 218 forms, 279 bindings including 17 local bindings, and 9
resolutions. It lowers to 122 canonical core nodes with 5 definitions, 2
functions, and 2 quoted descriptor bodies, with no calls, references, or
keyword-lookup products. Its keyword-rich diagnostic maps therefore remain
quoted data rather than executable lookup operations. The cache-free proof
passed with 26,264 request carrier nodes at depth 8, width 279, and 382,555
UTF-8 scalar bytes. This proves source representation only: Clojure stage0
remains the declared seed/compiler/verifier boundary and no executable
diagnostic, rendering, redaction, stream-verification, or runtime-authority
credit is taken. The C16 incremental-compilation source adds 24,810
authenticated bytes in 17 fragments containing 833 forms, 331 bindings
including 69 local bindings, and 29 resolutions. It lowers to 547 canonical
core nodes with 17 definitions: 11 data contracts and 6 quoted function
bodies. Its keyword-rich descriptors produce no calls, references, or
keyword-lookup products. The cache-free proof passed with 57,611 request
carrier nodes at depth 8, width 833, and 945,725 UTF-8 scalar bytes. This
proves source-model representation only and grants no cache construction,
storage, invalidation, revalidation, proof reuse, filesystem/network, or
release-grade incremental authority. The C17 plugin/pass API source adds 25,254
authenticated bytes in 17 fragments containing 878 forms, 329 bindings
including 67 local bindings, and 27 resolutions. It lowers to 603 canonical
core nodes with 17 definitions: 12 data contracts and 5 quoted function bodies,
with no calls, references, or keyword-lookup products. The cache-free proof
passed with 59,392 request carrier nodes at depth 8, width 878, and 974,774
UTF-8 scalar bytes. This proves source-model representation only: production
plugin loading and pass execution remain disabled, central integration remains
pending, and no sandbox, trust, whitelist, domain, cache, filesystem/network,
or release authority is credited. The B16 cohort remains unchanged. The
SH-21 meta-compiler-legality source adds 28,374 authenticated bytes in 42
fragments containing 2,778 forms, 419 bindings, and 1,109 resolutions. It
lowers to 2,292 canonical core nodes with 42 definitions, 459 calls, 848
references, 151 conditionals, 26 local bindings, and 42 function forms. Its 161
`get` calls divide into 139 literal-keyword calls and 22 dynamic-key or index
calls; all are ordinary resolved calls, so the canonical keyword-lookup product
count remains zero and the B16 cohort remains frozen at nine modules and 63
lookups. The cache-free proof passed with 199,593 request carrier nodes at depth
27, width 2,778, and 3,301,719 UTF-8 scalar bytes. It also proves exact
forward/mutual-recursion binding identity, co-canonical extension parity,
path-neutral identity, actual-path provenance, replay, and altered-product
containment. This is bounded source execution evidence only: authenticated
SH-15 diagnostics, SH-17 lowering, SH-19 runtime services, whole authoritative
compiler execution under `:meta`, and seedless execution remain pending. The
C18 compiler-verification source adds 30,982 authenticated bytes in 22
fragments containing 1,148 forms, 394 bindings, and 42 resolutions. It lowers
to 640 canonical core nodes with 22 definitions: 12 data contracts and 10
quoted function bodies, with no calls, references, or keyword-lookup products.
The cache-free proof passed with 75,762 request carrier nodes at depth 8, width
1,148, and 1,266,488 UTF-8 scalar bytes. The proof pins all quoted artifact
schemas, their actual omissions from declared required fields, the absent
replay builder, the nine-entry diagnostic catalog, the source-owned verifier
checklist, co-canonical extension parity, path-neutral identity, actual-path
provenance, replay, and altered-product containment. This proves source-model
representation only: production verifier execution, schema enforcement,
certificate and evidence checking, translation validation and replay, release
decisions, and plugin/backend conformance execution remain pending. The B16
cohort remains unchanged. The recorded pre-promotion C7 type-checker revision
contributed 142,136 authenticated bytes in 142 fragments containing 12,759
forms and 5,221 resolutions. Its checked-core products contained 137 function
records, 2,069 call edges, ten recursion components, and no keyword-lookup
products. The recorded
cache-free proof passed every contract check in 2,411.35 seconds in its
then-current coordinator context, with artifact
identity
`sha256:fb44649cb56a696eb5132774ca4dd0100c9029e4c59bddc27791bbfb115a40f8`
and verification report
`sha256:7300876921f948fedbd2d9d723d1a4b7ec6827c4329777e167015db582937665`.
The bounded first-order function-typing integration then passed 24 tests and
244 assertions
in 723.332 seconds with 3.627 GB sampled peak resident memory. It consumes
the host-validated B47 report and digest boundary, reconstructs definitions,
fixed-arity functions, direct calls, local bindings, and recursion products in
Gravity, emits path-neutral type identities with retained provenance, and
rejects altered lineage and results. That revision proved a first-order
function, local, and call typing slice, not complete C7 or SH-08: higher-order
functions, complete
recursive annotation inference, records, unions, protocols, generics, casts,
dynamic/layout/schema/ownership legality, public routing, native Gravity digest
verification, and seed retirement remain pending. The later proof-traversal
deduplication changed a shared bootstrap fingerprint without changing this C7
source; a fresh checkpoint-v2 replay is therefore pending before completion or
release evidence, and the recorded v1 receipt is not resumed as current proof.
The stage-owned C7 source revision later proven at `5fe2013` is 176,551 bytes
with hash
`sha256:648c71d18f5b81649a8d6d755e1e73b5ba502d359e3a972e4a9341dff6dee975`.
It promotes exactly one capture-free named monomorphic fixed-arity function value
through one `apply-one` hop, derives the authoritative integer signature from B47
call, edge, binding, and evaluation-order products, preserves SH-09/SH-10 pending
obligations, and recomputes altered candidates through the public verifier. Its
dedicated current-runtime namespace passed 9 tests and 150 assertions; the
existing function-call namespace passed 24 tests and 245 assertions. This is
focused non-authoritative integration evidence, not a replacement for a fresh C7
module proof. General higher-order values, captures and lifetimes, aliases and
returns, multi-hop flow, polymorphism, overloads, multi-arity and variadic calls,
protocol or dynamic dispatch, reflection, and complete recursive inference remain
pending. A fresh source-bound proof for this revision then passed at exact proof
commit `5fe2013` in 3,351.068 seconds (55.85 minutes), with artifact identity
`sha256:689e48e4a588cbe5e39a1f9a8cd385eb9e6d9ef7de35742c16958c404c161e15`.
Its derived census has hash
`sha256:5579f5f73d7a53ad84317a098541ec00f1bf18d30f8075c7694763f03c29341e`,
163 fragments and root forms, 15,505 forms, 6,377 resolutions, 12,752 core
nodes, 163 definitions, 2,561 calls, 5,111 references, and no keyword-lookup
products. The reviewed attestation binds the exact source, proof contract, raw
stdout, artifact, and census while recording that counts were derived rather
than independently precommitted. It therefore does not establish exact
authentic coverage, aggregate or release authority, or seed retirement. Later
development-tool commits changed `deps.edn` after proof completion, so this is
accepted exact-context evidence for `5fe2013`, not current-context authority for
those later commits. The 55.85-minute result is the new observational baseline:
it is 38.7% slower than the 40.27-minute historical run while the source is
24.2% larger, so it does not support a speedup claim or a controlled comparison.
The current stage-owned C7 source is 210,220 bytes with hash
`sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`.
It retains the bounded one-hop higher-order and one-self-edge recursion
products, and now derives both from the authoritative primitive literal family
integer, bool, and string. The self-recursive case remains exactly one named,
capture-free, positive-fixed-arity component with one direct positional
parameter-forwarding self call, a direct primitive-literal sibling base from
the same family, and concrete external call evidence. Its fixed-point context
is node-order independent, recursive and external B47 edges remain distinct
and identity-bound, and recursive constraints are shared by the typed core,
top-level result, and semantic identity. The combined cheap gate passed 18
selected vars and 330 assertions. Separate authenticated boundary vars passed
the bool higher-order `.gravity` case with 41 assertions and the string
self-recursive `.gravity` case with 36 assertions, one cold carrier each; the
paired `.qst` files were checked only for byte parity. These are current-runtime
non-authoritative integration results. Zero-arity recursion, transformed or
literal recursive arguments, nonliteral bases, multiple recursive calls or
components, mutual recursion, recursive captures or higher-order values, and
polymorphic, multi-arity, variadic, or nonconvergent recursion remain rejected
or pending. The prior source-bound proof and census remain exact historical
evidence for `5fe2013`; they were not reused for this source. A fresh proof
candidate at commit `206e89f` completed with stable context and a checked
output contract in 3,998.709 seconds of wrapper time (3,993.553 seconds in the
proof). It produced artifact
`sha256:9ee396cb1f8d6403ce14061a9e9d9977829da25c7fc42c13d9a0804025006587`
and derived census
`sha256:6580b784bb46755231ad62ed3095ce5efe775f233d6f327098bb802fb6380393`:
192 fragments and roots, 18,554 forms, 1,528 bindings, 1,266 local bindings,
7,687 resolutions, 15,286 core nodes, 192 definitions, 3,082 calls, 6,185
references, 187 function records, 3,082 call edges, 14 recursion components,
and zero keyword-lookup products. Counts were derived rather than precommitted,
the result requires a separate attestation, and no trusted reviewed attestation
was created. It is therefore source-bound proof-candidate evidence only, not
exact-authentic-coverage, aggregate, release, or seed-retirement authority.
The historical C8 effect-checker source added 44,102 authenticated bytes in 40 fragments
containing 3,301 forms, 410 bindings, and 1,078 resolutions. It lowers to 2,788
canonical core nodes with 40 definitions: 34 executable SH-09 functions and
three quoted source-model functions. Its 178 `get` calls divide into 169
literal-key calls and nine dynamic-key calls; all are ordinary resolved calls
rather than keyword-headed lookup forms, so the canonical keyword-lookup
product count remains zero and B16 stays frozen. The cache-free authoritative
proof passed with exact source revision
`sha256:8be72ed8adbe830992ee990ba0cb23bb06ce7d29859360afa7d937f0833e0096`,
complete capability proof, and no failed checks. The bounded source recognizes
`:error/raise` as a declared language-level effect with no capability,
provider, grant, or resource subject. That receipt is historical evidence for
the 44,102-byte revision, not current evidence for the adapter revision.

The current stage-owned C8 source is 82,797 bytes with hash
`sha256:de3fb80e14336cadacf710a0b2fef33b19efab0728d5ca08e7a25c72df7afe16`.
It adds a bounded authenticated SH-08 adapter for exact schema-3 pure primitive
first-order and capture-free one-hop function-typed cores when all declared
call and latent effect sets are empty. It reconstructs the complete C7 identity
preimage rather than accepting a relabeled domain or shaped identifier. The
adapter preserves
`[:pending-sh09]` thrown effects, recomputes the existing C8 legality kernel,
keeps physical path provenance out of semantic identity, and rejects altered
upstream identities, facts, requests, or results. Four synthetic tests passed
64 assertions, and one authenticated `.gravity` function-typed boundary passed
27 assertions in 65.918 seconds with one cold carrier; its `.qst` twin was not
built. These are non-authoritative development results. A fresh source-bound
C8 proof candidate then completed at commit `871cd14` in 877.765 seconds of
checkpoint time (872.388 seconds in the proof), with stable context, empty
stderr, and a checked output contract. It produced artifact
`sha256:ed0a4eb33beafb7c0585cab276ab97aac3d6542872b8bad3e89e9df1930d46a0`
and derived census
`sha256:78ea0c864b4a59694e99470408e4aebbf5783c623827d29ea0f0d4ae9e49c902`:
61 fragments and roots, 5,973 forms, 512 bindings, 250 local bindings, 2,023
resolutions, 5,135 core nodes, 61 definitions, 852 calls and call edges, 1,607
references, 55 function records, one recursion component, and zero keyword
lookups. That proof binds the preceding 73,997-byte revision at commit
`f3729a5`; the current source retains ordered canonical type, effect, and
no-capability digest requests plus exact request/digest binding into a separate
path-neutral effected-core template. Its latest first-order and full-identity
discriminator passed two tests and 28 assertions. Digest computation remains
an external coordinator boundary. Counts were derived rather than precommitted
and no trusted reviewed
attestation was created, so the prior result is historical proof-candidate
evidence only. This does
not establish exact authentic coverage, aggregate or release authority, a
production effect checker, complete schema enforcement, effectful adaptation,
general effect inference, completed thrown effects, latent or transitive call
effects, handlers, namespace or module summaries, complete runtime-profile
policy, MIR preservation, C8-to-C9 adaptation, or SH-09 completion.
The C9 ownership-checker source adds 35,894 authenticated bytes in 31
fragments containing 2,320 forms, 370 bindings, and 741 resolutions. It lowers
to 1,964 canonical core nodes with 31 definitions: 21 executable SH-10
functions and three quoted source-model functions. Its 134 `get` calls divide
into 129 literal-key calls and five dynamic-key calls; all are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The cache-free
authoritative proof passed with exact source revision
`sha256:b4fdf1022eb6eb25d091f1c918332c7b1393b6850acf4bb6988d8b8dbb2269e0`,
complete capability proof, and no failed checks. The bounded executable surface
covers owned-mutable initialization, read, immutable and mutable borrow,
borrow end, move, consume, and one function-return escape policy with exact
ordered facts and structured rejection reasons. Request and event maps remain
shallow non-exact schemas; digest values are shape-checked only; whole-carrier
cycle, depth, width, and scalar preflight is absent; the 1,024-event uniqueness
and execution paths remain recursive; lifetime endpoints are generic numbers;
the capability proof identifier is preserved but not authenticated; and the
verifier is same-implementation recomputation. That result is historical
evidence for the 35,894-byte revision. The current 71,132-byte source at
`sha256:4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd`
preserves the owned-mutable kernel and adds an exact C8-to-C9 adapter for
identity-bound pure integer, boolean, and string values as persistent immutable
reads. It resolves each exact ownership fact before deriving and resolving the
whole ownership-core identity, rejects reordered, substituted, missing, or
colliding digest results, and keeps physical provenance outside semantic
identity. Coordinator digest resolution remains shape-checked and explicitly
non-authoritative. Four synthetic adapter tests passed 32 assertions, the
existing kernel passed 424 assertions, and the combined C9-to-C10 lane passed
five tests and 147 assertions in 69.470 seconds with one cold `.gravity`
carrier; its `.qst` twin was byte-parity evidence only. These are
non-authoritative development results. A historical source-bound-derived proof
candidate for a preceding source revision completed in 505.045 seconds of
checkpoint time (499.953 seconds in the proof), with stable context, empty
stderr, artifact
`sha256:56aa7b6cded727e47b7528a7b027b711b7fb911e8dd288df259d15282412b2de`,
and derived census
`sha256:b28f186ab5586620913748d21224937344cbacea22a178c391640a8c2bd61a45`:
44 fragments and roots, 3,274 forms, 426 bindings, 164 local bindings, 1,060
resolutions, 2,776 core nodes, 44 definitions, 423 calls and call edges, 855
references, 37 function records, five recursion components, and zero keyword
lookups. Counts were not precommitted, and no trusted reviewed attestation was
created, so the result is historical planning evidence only. The current
source and fixed wrapper identity invalidate it and require a fresh no-resume
run after integration and freeze; it does not establish exact authentic
coverage, aggregate or release authority, or automatic scoped authority.
Persistent aggregate copy
semantics, owned-mutable, effectful, and nonprimitive SH-09 adaptation, field
and range splitting, regions, arenas, linear resources, task, actor, and FFI
transfer, runtime borrow checks, unsafe-audit execution, trusted digest
resolution, MIR preservation, and SH-10 completion remain pending.
The historical C10 safety-analysis source added 68,327 authenticated bytes in
73 fragments containing 5,652 forms, 542 bindings, and 2,046 resolutions. Its calibrated
lowering contains 4,709 canonical core nodes with 73 definitions: 63
executable SH-11 functions and three quoted source-model functions. Its 337
`get` calls divide into 334 literal-key calls and three dynamic-key calls; all
are ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 380,720 carrier nodes at depth 38, maximum width 5,652,
and 6,314,034 exact UTF-8 scalar bytes; every unchanged B16 bound passes. The
bounded executable surface classifies index bounds, integer overflow, division,
numeric casts, and shifts into exactly one of `:proven-safe`,
`:runtime-checked`, `:rejected`, or `:unsafe-island`, with structured reason
surfaces and recomputing verification. Production safety-analysis authority,
complete contract and diagnostic schema enforcement, memory and lifetime
safety, regions, linear resources, FFI, concurrency, taint, generated-code
safety, floating-point and elementary-function safety, optimization
invalidation, authenticated SH-09/SH-10 convergence, MIR preservation, and
SH-11 completion remain pending. The current C10 source is 112,712 bytes at
`sha256:2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968`.
It adds a bounded adapter from the new identity-bound C9 ownership core. The
adapter admits exactly one persistent-immutable integer, boolean, or string
read and permits its private load classifier to return `:proven-safe` only
after sequential proof, result, and whole-core digest resolution. The generic
classifier still rejects load requests, and runtime-checked, unsafe,
nonpersistent, aggregate, and effectful load families remain pending. Its
current cross-stage development receipt is the five-test, 147-assertion lane
above; the final authenticated `.gravity` boundary took 61.220 seconds, used
one cold carrier, and did not build `.qst`. Fresh source-bound-derived proof
candidates for the three frozen sources completed at commit `e27757e` with
stable context, checked output contracts, exit zero, no timeout, and empty
stderr. C8 took 1,035.465 seconds and produced artifact
`sha256:e36f64d3fab9f8419d9f0e8d1b4b2f59f137b2ca07b7756dd9c0a7c92be6c3a4`
and census
`sha256:ecd335f292da034f0e283f79969d75002d459f41cd4f43f3ce10cee887510570`:
72 fragments and roots, 6,757 forms, 570 bindings, 308 local bindings, 2,312
resolutions, 5,792 core nodes, 72 definitions, 976 calls and call edges, 1,847
references, 66 function records, four recursion components, and zero keyword
lookups. C9 took 748.278 seconds and produced artifact
`sha256:ec15a730d9f264b7744e797d29e928adc9ca8953daf186a4f2e84634b6baa86d`
and census
`sha256:a6d95d6a7ddc63c3a446dafc946e102587b753f443cba7a004d3240dafe3d7c6`:
66 fragments and roots, 5,243 forms, 556 bindings, 294 local bindings, 1,797
resolutions, 4,409 core nodes, 66 definitions, 716 calls and call edges, 1,440
references, 59 function records, 13 recursion components, and zero keyword
lookups. C10 took 1,624.000 seconds and produced artifact
`sha256:de279d0e495212d259fd3f78e5575aa57c3a7559984df9acfdb71fd5f0488cb2`
and census
`sha256:d327c313e26d09bfbab9417d06ce4987767e7547941ec9afdeefdf643026c469`:
111 fragments and roots, 9,200 forms, 728 bindings, 466 local bindings, 3,306
resolutions, 7,728 core nodes, 111 definitions, 1,350 calls and call edges,
2,560 references, 104 function records, one recursion component, and zero
keyword lookups. Counts were derived rather than precommitted and all three
receipts require separate attestation. No trusted reviewed attestation,
exact-authentic coverage, aggregate authority, release authority, or
safety-certificate authority is claimed.
The C12 domain-IR source adds 61,946 authenticated bytes in 85 fragments
containing 5,711 forms, 569 bindings, and 2,073 resolutions. Its calibrated
lowering contains 4,591 canonical core nodes with 85 definitions: 72
executable SH-14 functions and four quoted source-model functions. Its 254
`get` calls divide into 209 literal-key calls and 45 dynamic-key or indexed
calls; all are ordinary resolved calls rather than keyword-headed lookup forms,
so the canonical keyword-lookup product count remains zero and B16 stays
frozen. The SH-06 request contains 383,280 carrier nodes at depth 23, maximum
width 5,711, and 6,372,295 exact UTF-8 scalar bytes; every unchanged B16 bound
passes. The bounded executable layout surface now enforces exact per-kind and
nested schemas, canonical bounded identifiers and payloads, iterative
structural preflight, bounded origin chains, portable ASCII symbol and keyword
sizes, supported profile/target pairs, path-neutral identity, and contained
candidate verification. Production domain-IR authority, complete C12 contract
and diagnostic schema enforcement, authenticated SH-12 MIR input,
target-specific layout, actual allocation, field-offset calculation, pointer
and lifetime layouts, and SH-14 completion remain pending.
The C13 MIR-optimization source adds 98,836 authenticated bytes in 114
fragments containing 8,513 forms, 740 bindings, and 3,140 resolutions. Its
calibrated lowering contains 7,036 canonical core nodes with 114 definitions:
100 executable SH-16 functions and five quoted source-model functions. Its 493
`get` calls divide into 445 literal-key calls and 48 dynamic-key or indexed
calls; all are ordinary resolved calls rather than keyword-headed lookup forms,
so the canonical keyword-lookup product count remains zero and B16 stays
frozen. The SH-06 request contains 562,128 carrier nodes at depth 31, maximum
width 8,513, and 9,490,546 exact UTF-8 scalar bytes; every unchanged B16 bound
passes. The cache-free acceptance, core, and rejected proof passed in 1,744.16
seconds with 4,345,921,536 maximum resident bytes and no swaps. The isolated
C13-specific replay proof passed in 1,557.99 seconds with 4,643,569,664 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
all contract checks in 2,588.82 seconds with artifact identity
`sha256:78f6074f338f98f3241ea08e2d2c8f427db0aa71d4d7bb6057f8a5168244fba9`,
6,019,416,064 maximum resident bytes, and no swaps. The bounded executable
surface now applies constant folding, branch
simplification, dead-code elimination, and proof-governed check retention or
elision as a genuine sequential pipeline with conservative invalidation,
signed-i64 containment, exact operation schemas, iterative structural
preflight, path-neutral identity, post-pass verification, and exact
recomputation. Authenticated SH-15 input, complete C11 MIR adaptation,
remaining proof-class replay, whole-function translation validation,
target-lowering proof preservation, a self-hosted certificate checker, and
SH-16 completion remain pending.
The C14 target-lowering source adds 168,303 authenticated bytes in 143
fragments containing 15,213 forms, 936 bindings, and 5,693 resolutions. Its
calibrated lowering contains 12,683 canonical core nodes with 143 definitions:
135 function definitions and eight data contracts. Three legacy function
bodies remain quoted compatibility surfaces; a fourth quoted body supplies the
active pinned C11 function-shape table. Its 1,131 `get` calls divide into 1,032
literal-key calls and 99 dynamic-key or indexed calls; all are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The SH-06
request contains 1,015,002 carrier nodes at depth 32, maximum width 15,213,
16,757,402 exact UTF-8 scalar payload bytes and a 67,007,600-byte conservative
executable scalar charge; every unchanged B16 bound
passes. The isolated authentic acceptance, core, altered-replay, and paired
rejection proof passed in 2,953.89 seconds with 5,054,251,008 maximum resident
bytes and no swaps. The independent cross-root `.gravity`/`.qst` parity proof
passed in 5,391.18 seconds with 5,778,669,568 maximum resident bytes and no
swaps. The final cache-free authoritative process passed every contract check
in 4,780.74 seconds with artifact identity
`sha256:0bfaf8070df705fd62b29cc57f760951ff9414751e7c320ec11df4daa95c2c2b`,
5,970,919,424 maximum resident bytes, and no swaps. The bounded executable
surface accepts LLVM, C, and Wasm requests,
constructs target-specific lowering records, preserves path-neutral identity
with actual-path provenance, and rejects malformed requests and altered
results through structured diagnostics and recomputing verification.
Authenticated SH-16 ingress, complete C11/MIR target coverage, proof
preservation across every supported target, production target-lowering
authority, the complete target matrix, and SH-17 completion remain pending.
The B1 backend-interface source adds 140,646 authenticated bytes in 70
fragments containing 10,980 forms, 530 bindings, and 3,692 resolutions. Its
calibrated lowering contains 9,440 canonical core nodes with 70 definitions:
64 executable functions and six data contracts. Its 1,077 `get` calls divide
into 1,069 literal-key calls and eight dynamic-key or indexed calls; all are
ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 721,001 carrier nodes at depth 29, maximum width 10,980,
11,727,269 exact UTF-8 scalar bytes, and a 46,892,996-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated census completed in
471.74 seconds with 4,039,589,888 maximum resident bytes and no swaps. Fresh
LLVM, C, and Wasm packet construction, verification, and altered-packet
rejection passed in 292.57 seconds with 3,904,782,336 maximum resident bytes
and no swaps. The authentic source, core, altered-replay, and paired rejection
proof passed with 2,439.17 CPU seconds, 5,763,072,000 maximum resident bytes,
and no swaps; its 37,764.43-second wall record includes host suspension. The
cross-root `.gravity`/`.qst` parity proof passed with 4,019.39 CPU seconds,
5,173,477,376 maximum resident bytes, and no swaps; its 30,169.85-second wall
record likewise includes host suspension. The final cache-free authoritative
process passed every contract check in 2,885.68 seconds with artifact identity
`sha256:b24e8ac18ddc88fdc887773b759ab8f36f79e103cb99e13e4f99ee4cb6c77a13`,
5,856,509,952 maximum resident bytes, and no swaps. The bounded executable
surface constructs and freshly verifies LLVM, C, and Wasm backend packets,
preserves source, MIR, optimization, lowering, proof, profile, target, ABI,
runtime, effect, capability, identity, and provenance boundaries, and rejects
missing or altered packet facts through structured diagnostics. Complete B1
MIR and domain-IR coverage, production backend execution, object or executable
emission, external target execution, backend conformance completion, public
Gravity routing, Clojure seed retirement, SH-07 completion, and SH-17
completion remain pending.
The B3 LLVM backend source adds 86,185 authenticated bytes in 83 fragments
containing 7,072 forms, 692 bindings, and 2,762 resolutions. Its calibrated
lowering contains 5,940 canonical core nodes with 83 definitions: 72 executable
functions and 11 data contracts. Its 372 `get` calls divide into 306 literal-key
calls and 66 dynamic-key or indexed calls; all are ordinary resolved calls
rather than keyword-headed lookup forms, so the canonical keyword-lookup
product count remains zero and B16 stays frozen. The SH-06 request contains
484,843 carrier nodes at depth 27, maximum width 7,072, 8,068,604 exact UTF-8
scalar bytes, and a 32,257,096-byte conservative scalar charge; every unchanged
B16 bound passes. The isolated exact census passed in 364.82 seconds with
3,775,266,816 maximum resident bytes and no swaps. Fresh bounded B3 packet and
final-artifact construction, contextual verification, independent lowering
reconstruction, and altered-MIR/lowering rejection passed in 682.65 seconds
with 3,973,185,536 maximum resident bytes and no swaps. The authentic source,
core, altered-replay, and paired rejection proof passed in 1,545.84 seconds
with 4,406,362,112 maximum resident bytes and no swaps. The independent
cross-root `.gravity`/`.qst` parity proof passed in 2,411.56 seconds with
4,782,587,904 maximum resident bytes and no swaps. The final cache-free
authoritative process passed every contract check in 2,189.43 seconds with
artifact identity
`sha256:2c512aaad76cebe430fc2083310ad9699890e314c67467cf899c6e3f0aede59d`,
4,721,672,192 maximum resident bytes, and no swaps. The bounded executable
surface validates a pinned ARM64 macOS LLVM subset, constructs deterministic
textual LLVM for supported scalar and control-flow MIR, preserves target and
source lineage, and rejects malformed envelopes, unsupported operations,
invalid data-flow or CFG structure, unsafe target assumptions, and altered
artifacts through structured diagnostics and contextual verification. The
subsequent authenticated internal gate consumes only verified C11 MIR, preserves
the C13/C14/B1/pass/fact/proof closure, emits an ARM64 Mach-O object and
executable with the pinned Apple Clang/linker, inspects the result with `file`
and `otool`, and executes the binary with the expected scalar exit code.
Standalone accepted `.gravity`/`.qst` fixtures and declared-target rejection
fixtures retain cross-root and extension evidence; target leakage and evidence
tamper reject before Clang, and hostile publication rejects before the final
artifact is visible. Complete B3 MIR and domain-IR coverage, a production or
public LLVM target, backend conformance completion, public Gravity routing,
Clojure seed retirement, SH-07 completion, SH-17 completion, and self-hosting
completion remain pending. The internal gate remains Clojure-seed-bound and
records no public, release, whole-backend, or self-hosting credit.
The B4 Wasm backend source adds 118,298 authenticated bytes in 72 fragments
containing 8,189 forms, 629 bindings, and 2,900 resolutions. Its calibrated
lowering contains 6,830 canonical core nodes with 72 definitions: 61 functions
and 11 data contracts. All 349 `if` forms have normative four-element shape
after the bounded source correction, and its 520 `get` calls divide into 471
literal-key calls and 49 dynamic-key or indexed calls. All are ordinary
resolved calls rather than keyword-headed lookup forms, so the canonical
keyword-lookup product count remains zero and B16 stays frozen. The SH-06
request contains 561,455 carrier nodes at depth 44, maximum width 8,189,
9,056,474 exact UTF-8 scalar bytes, and a 36,208,772-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated exact census passed in
420.52 seconds with 3,705,847,808 maximum resident bytes and no swaps. The
authentic source, core, altered-replay, and paired rejection proof passed in
1,731.80 seconds with 4,600,299,520 maximum resident bytes and no swaps. The
independent cross-root `.gravity`/`.qst` parity proof passed in 2,612.74
seconds with 4,819,746,816 maximum resident bytes and no swaps. Fresh bounded
B4 construction, contextual verification, and altered-MIR, lowering, and raw
Wasm rejection passed with 9 tests and 122 assertions; each alteration was
contained by its owning B1, B4, or B13 diagnostic boundary before effects. The
authenticated artifact identity is
`sha256:334fe22fbd4367dca63e53b768ff0ef1ed26ef8d8937f8b605471d3fe60da739`.
The bounded executable surface constructs and verifies deterministic Wasm32
core lowering for its supported compiler slice while preserving path-neutral
identity and actual-path provenance. Complete B4 MIR and domain-IR coverage,
production backend execution, component-model and canonical-ABI support,
complete import, export, memory, table, WASI, async, SIMD, atomic,
runtime-binding, artifact-emission, external target-matrix, public-routing,
conformance, release, and self-hosting authority remain pending.
The B2 C backend source adds 122,488 authenticated bytes in 129 fragments
containing 11,151 forms, 832 bindings, and 4,200 resolutions. Its calibrated
lowering contains 129 definitions: 120 functions and nine data contracts. All
441 `if` forms have normative four-element shape, and its 842 `get` calls
divide into 792 literal-key calls and 50 dynamic-key or indexed calls. They are
ordinary resolved calls rather than keyword-headed lookup forms, so the
canonical keyword-lookup product count remains zero and B16 stays frozen. The
SH-06 request contains 735,556 carrier nodes at depth 30, maximum width 11,151,
12,379,536 exact UTF-8 scalar bytes, and a 49,502,752-byte conservative scalar
charge; every unchanged B16 bound passes. The isolated exact census passed in
542.06 seconds with 3,733,536,768 maximum resident bytes and no swaps. The
authentic source, core, altered-replay, and paired rejection proof passed in
2,349.54 seconds with 4,543,021,056 maximum resident bytes and no swaps. The
independent cross-root `.gravity`/`.qst` parity proof passed in 3,801.31 seconds
with 4,851,482,624 maximum resident bytes and no swaps. The cache-free
authoritative process passed every contract check in 3,527.56 seconds with
artifact identity
`sha256:4a8e165324a492d57f6a3f7f5ce44ea291abd4ba2ebe424fd2206b6009406ed0`,
5,726,076,928 maximum resident bytes, and no swaps. Fresh Gate A validation,
construction, contextual verification, and altered-input rejection passed 11
tests and 110 assertions in 377.18 seconds with 3,735,666,688 maximum resident
bytes and no swaps. Fresh Gate B validation, construction, contextual
verification, real bounded hosted-C17 source, header, object, and executable
production, exact process records, exit-7 execution, cleanup evidence, and
altered-input rejection passed 11 tests and 231 assertions in 441.10 seconds
with 3,939,500,032 maximum resident bytes and no swaps. Semantic identities
remain path-neutral while actual checkout paths remain in provenance. Complete
B2 MIR and domain-IR coverage, production compiler authority, complete C17 and
target coverage, runtime and library integration, release-grade object and
executable emission, public routing, Clojure seed retirement, backend
conformance completion, SH-07 completion, and self-hosting completion remain
pending.
The cache-free proof-transaction checkpoint preserves the construction and
independent-audit boundary while reusing only thread-confined immutable
verification receipts. All 34 then-authoritative modules were accepted, all
verification and capability checks passed, and the process recorded 476
receipt reuses without a persistent cache. It completed in 31,928.08 seconds
with 7,959,691,264 maximum resident bytes and no swaps, reducing wall time by
about 31 percent from the preceding 46,167-second B38 process. Maximum resident
memory increased from 6,399,033,344 bytes, so this is a measured time-for-memory
trade rather than a memory improvement. The closed receipt binds artifact,
report, stage, mode, check-catalog, and exact verifier identities while
retaining no carrier values after cleanup.
The L1/C2 surface-reader source advances the authenticated compiler inventory
to 35 of 41 modules. It adds 36,920 authenticated bytes in 32 fragments
containing 2,892 forms and 1,019 resolutions; its source hash is
`sha256:cb416baa7330fd7db5507fcd5fc1d78d5c9e848feb9020552d4c21b9e1c17fe0`.
The isolated census passed 9 tests and 193 assertions in 183.00 seconds with
4,021,829,632 maximum resident bytes and no swaps. Authentic replay passed 9
tests and 194 assertions in 545.56 seconds with 4,021,174,272 maximum resident
bytes and no swaps, preserving the genuine reader products and rejecting an
altered token stream. Cross-root `.gravity`/`.qst` parity passed 9 tests and
191 assertions in 989.77 seconds with 4,023,549,952 maximum resident bytes and
no swaps; semantic identity stayed path-neutral while actual paths remained in
provenance. The final cache-free authoritative process passed every contract
check in 622.459 seconds with artifact identity
`sha256:f27d7569ad0a06988241864fcb49ca1fc0df5d39993b55397e96a58524f050f9`,
14 receipt reuses, 3,820,716,032 maximum resident bytes, and no swaps. Complete
reader semantics, public routing, Clojure seed retirement, SH-07 completion,
and self-hosting completion remain pending.
The SH-23 hermetic package-build source advances the authenticated compiler
inventory to 36 of 41 modules. It adds 39,872 authenticated bytes in 50
fragments containing 3,757 forms and 1,428 resolutions; its source hash is
`sha256:70649e49d7130cf33e1d11ac226452ea908fee6731e0e49ca8e003e14cbabe1e`.
The isolated census passed 10 tests and 105 assertions in 204.16 seconds with
4,076,879,872 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 106 assertions in 775.98 seconds with 4,549,574,656 maximum resident
bytes and no swaps, preserving the genuine package-build products and rejecting
altered core data. Cross-root `.gravity`/`.qst` parity passed 10 tests and 103
assertions in 1,286.10 seconds with 4,366,024,704 maximum resident bytes and no
swaps. The alternate-checkout proof preserved equal path-neutral identity while
retaining distinct physical paths in component records, artifact requests,
source maps, and provenance. The final cache-free authoritative process passed
every contract check in 776.594 seconds with artifact identity
`sha256:57811a49093fdc298005c42cebd9b42a089e4fa1b8fbb8009ee0080e3e4bac0c`,
14 receipt reuses, 4,653,350,912 maximum resident bytes, and no swaps. General
dependency solving, action execution, public routing, Clojure seed retirement,
SH-07 completion, SH-23 completion, and self-hosting completion remain pending.
The SH-22 bounded standard-library source advances the authenticated compiler
inventory to 37 of 41 modules. It adds 45,717 authenticated bytes in 64
fragments containing 4,896 forms and 1,944 resolutions; its source hash is
`sha256:6198d87e19ee86f72d51aa1253c2d438da6af8dea495f2dd5eb0e64ca3eff1e4`.
The isolated census passed 10 tests and 77 assertions in 255.76 seconds with
4,108,025,856 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 78 assertions in 811.91 seconds with 4,749,508,608 maximum resident
bytes and no swaps, preserving all 64 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 75 assertions in 1,684.35 seconds with 4,326,227,968 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,022.793 seconds with artifact identity
`sha256:27748ea15b172858d829a0f6761b3ad0245e3a088c118a033d9ba678b7a8fe61`,
14 receipt reuses, 4,632,182,784 maximum resident bytes, and no swaps. Complete
Unicode and normalization, authenticated runtime services, the complete
standard library, public routing, Clojure seed retirement, SH-07 completion,
SH-22 completion, and self-hosting completion remain pending.
The reusable authenticated-envelope source advances the authenticated compiler
inventory to 38 of 41 modules. It adds 59,495 authenticated bytes in 74
fragments containing 5,302 forms and 2,146 resolutions; its source hash is
`sha256:04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71`.
The isolated census passed 10 tests and 101 assertions in 318.15 seconds with
4,025,204,736 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 102 assertions in 965.93 seconds with 4,868,505,600 maximum resident
bytes and no swaps, preserving all 74 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 99 assertions in 1,840.12 seconds with 4,610,244,608 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,137.379 seconds with artifact identity
`sha256:b2c8ec3ad2dcb1b73f6707914788baa72dc301fb7d3a2ca69f2d2195625a28ee`,
14 receipt reuses, 4,585,766,912 maximum resident bytes, and no swaps. Release
signature authority, verifier-correctness proof, self-hosted envelope
execution, whole-language coverage, public routing, Clojure seed retirement,
SH-07 completion, and self-hosting completion remain pending.
The Gravity resolution source advances the authenticated compiler inventory to
39 of 41 modules. It adds 77,209 authenticated bytes in 97 fragments containing
8,102 forms and 3,278 resolutions; its source hash is
`sha256:001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56`.
The isolated census passed 10 tests and 105 assertions in 583.21 seconds with
4,082,614,272 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 106 assertions in 1,578.96 seconds with 5,026,824,192 maximum resident
bytes and no swaps, preserving all 97 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 103 assertions in 2,874.68 seconds with 5,472,419,840 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 1,745.662 seconds with artifact identity
`sha256:f58d88d1103ea2c72bf549ffdae0fac42272efc8dd5f7305ea85d269f68537fb`,
14 receipt reuses, 5,455,511,552 maximum resident bytes, and no swaps. Package
discovery, type and effect checking, self-hosted resolution execution,
whole-language coverage, public routing, Clojure seed retirement, SH-07
completion, and self-hosting completion remain pending.
The bounded C6-C10 checked-core pipeline source advances the authenticated
compiler inventory to 40 of 41 modules. It adds 141,562 authenticated bytes in
144 fragments containing 13,318 forms and 5,210 resolutions; its source hash is
`sha256:0299511c26c8b2a191309a4a4358528de397c88551ed98a2aed03d172067b2a5`.
The isolated census passed 10 tests and 91 assertions in 739.56 seconds with
4,070,670,336 maximum resident bytes and no swaps. Authentic replay passed 10
tests and 92 assertions in 2,648.54 seconds with 6,381,010,944 maximum resident
bytes and no swaps, preserving all 144 ordered source roots and definitions and
rejecting altered binding identity. Cross-root `.gravity`/`.qst` parity passed
10 tests and 89 assertions in 5,051.26 seconds with 5,924,700,160 maximum
resident bytes and no swaps. The final cache-free authoritative process passed
every contract check in 3,137.484 seconds with artifact identity
`sha256:aae6f68bd91a2c8b9f0907b20b3a2ca3e68dc64b8fbedca1ed2db8710ae8f9b5`,
14 receipt reuses, 6,959,955,968 maximum resident bytes, and no swaps. Calls,
complete collection and numeric forms, mutation and resource semantics,
runtime checks, unsafe islands, domain IR, backend lowering, whole-language
coverage, public routing, Clojure seed retirement, SH-07 completion, and
self-hosting completion remain pending.
The authoritative checked-core source advances the authenticated compiler
inventory to 41 of 41 modules. The measured B47 revision was 444,331 bytes with
source hash `sha256:fd5c61348a8470650bb314542ad0ee04ff84099ed42fca05cbe72d0c0fc3ebdb`,
308 fragments, 37,411 forms, 2,440 module bindings, and 15,247 resolutions. The
authentic B47 census accepted after 5,397.56 seconds with 7,758,774,272 maximum
resident bytes and no swaps. It emitted 31,428 core nodes, 308 definitions,
12,282 references, 6,547 calls, 305 function records, 6,547 call edges, six
recursion components, and 31,430 digest requests. The measured template carrier
contained 17,681,671 nodes and 1,035,974,980 scalar bytes; the generated-digest
carrier contained 12,572,396 nodes and 721,982,664 scalar bytes. Those results
support the bounded 33,554,432-node template ceiling and the 16,777,216-node
generated-digest ceiling, with the common 1,073,741,824 scalar-byte ceiling;
rejection telemetry remains diagnostic-only and does not weaken acceptance.
The subsequent semantics-preserving carrier preflight optimization advances
the current source revision to 444,325 bytes with hash
`sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092`;
its source, plan, and function identities are pinned, while a fresh cache-free
replay remains required before final B47 integration.
The B47 census proves bounded construction of exact function, direct-call, and
recursion products, but it is not the final cache-free authoritative replay or
cross-root parity gate for this revision. `SH-07` remains partial: general
higher-order calls beyond the bounded C7 one-hop consumer, closure capture,
complete core-form coverage, public routing, and seed
retirement remain pending. `SH-25` remains queued because its supplied component
descriptors are bounded planning inputs rather than authentic `SH-24` build
outputs.

The additive B48 call-arity module advances the authenticated source inventory
to 42 modules without changing the B47 v16 boundary task. Its 56,581-byte source
has hash
`sha256:d101855439ef185727608ee696aacbfb41aff025ee8bbe0c62122f45f7c112ed`.
The recorded module proof passed every contract check in 772.462 seconds in its
then-current coordinator context, lowering 4,849 forms in 52 fragments with
1,953 resolutions, 52 function records, 796
call edges, and no recursion components. It produced artifact identity
`sha256:381630793764afa6adc0d7d7b4419397fcd06d1030897dc2520d03d2503a2023`
and verification report
`sha256:f34777f5923c2296d839281727317fb53855ac8b5ab79a1afc134e0f4fd25b53`.
This proves bounded direct local fixed-arity legality, ordered argument lineage,
explicitly pending nonlocal calls, path-neutral identity, retained provenance,
and structured rejection. Variadic and multi-arity dispatch, closure capture,
general higher-order calls beyond the bounded C7 one-hop consumer, type/effect
legality, execution, public routing, and seed
retirement remain pending, so `SH-07` receives no completion credit. The later
shared proof-traversal deduplication requires a fresh checkpoint-v2 replay
before completion or release evidence; the recorded v1 receipt remains bounded
historical module evidence rather than current-context authority.

The additive bootstrap-hosted `sh07-core` command exposes the authenticated
SH-07 artifact through the strict public source boundary without repointing the
legacy `core` or compiler commands. Its dedicated executable gate passed 4
tests and 72 assertions for accepted `.gravity` and `.qst` inputs, path-neutral
identity, distinct actual-path provenance, deterministic repeats, unrelated
working directories, bootstrap-only and default stale-package bypasses,
structured C6 rejection, extension policy, and malformed UTF-8 containment.
The route still declares the Clojure source-loading, plan-execution, digest,
envelope, and final-assembly boundary; it does not provide package refresh,
seed retirement, complete public compiler routing, or `SH-07` completion.

The Clojure stage0 boundary still owns initial source reads, compiler-plan
execution, canonical digest resolution, authenticated-envelope assembly,
central routing, and final artifact construction. The historical admitted
compiler snapshots passed their source-level authenticated gates; subsequent
development revisions do not inherit that authority. The complete
core-form, mutation, recursion, exception, and pattern surface must pass the
remaining executable gates before `SH-07` can receive completion credit.

## Beyond First Self-Hosting

After `SH-29`, target expansion becomes a separate matrix. Each canonical
target must repeat the applicable `SH-17` through `SH-20` target gates plus
backend conformance, runtime/provider, packaging, and external execution gates.
The first seedless compiler therefore establishes the compiler authority needed
to implement every target; it does not claim that every target is already
implemented.

## Outputs and Artifacts

This plan expects each completed slice to produce:

- a slice manifest naming dependencies and governing documents;
- accepted and rejected fixtures;
- executable artifacts and stable diagnostics;
- focused, regression, and adversarial test results;
- authenticated pass/artifact records where applicable;
- a TCB boundary record;
- stage comparison evidence for bootstrap slices;
- an evidence-ledger entry linked from the owning phase roadmap.

## Conformance Criteria

- All 30 slices have unambiguous dependencies and executable exit gates.
- No slice receives completion credit for scaffold, source inventory, proof
  metadata, or public fixture routing alone.
- Parallel work has non-overlapping file ownership and a single integration
  owner for central routing and generated evidence.
- The count and continuous completed prefix can be reproduced from reviewed
  evidence bundles.
- `SH-29` cannot pass while any compiler, runtime, standard-library,
  package/build, verifier, or release path inside the public boundary depends on
  Clojure.
