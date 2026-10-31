# Development Verification Reference Archive

This file preserves the detailed verification notes, historical measurements,
specialized runners and prior attempts that were formerly in the default
development guide. It is retained for provenance and targeted lookup. The
current edit-to-test workflow is in
[Development Verification](development-verification.md); historical material
does not add a prerequisite to ordinary development.

## Purpose

Repository verification uses the Clojure runners directly.

Run commands from the repository root.

## Remove project cost and budget mechanisms

The current implementation objective removes project cost models, logical work
units, caller/producer allowances, completed-work counters, debit/reservation
plumbing, cost proofs and budget-exhaustion refusals. Remove their enforcing
code, contract requirements and budget-only test assertions. Do not replace
them with larger constants, a new policy option, an unbounded sentinel, renamed
generic bounds or a requirement to finish accounting before implementation.

Capacity-oracle node, depth, width, scalar, source-byte, token and form maxima
that merely refuse otherwise well-formed inputs are included. A numeric check
remains only for an independently meaningful semantic or representability
condition, such as legal indices, arities, integer modes or target formats.
Host broker, heap, process-watchdog, capture and platform restrictions are
separate; paid account and external billing limits are not project changes.

Preserve complete semantic validation, authentication, source provenance,
native class/callback/metadata rules, fresh reconstruction, equality and tamper
detection independently of accounting. Effect and capability permission sets
are semantic authority, not work allowances; removing compiler fuel does not
make an undeclared effect or missing capability legal. Replace budget-only
assertions with applicable accepted/rejected semantic regressions, without
requiring a new cost certificate or claiming correctness from a deleted guard.

| Delete | Preserve independently | Owner |
| --- | --- | --- |
| C7 inference fuel, work-policy options, fee worksheets, prospective reservations, budget refusal and accounting-only fixtures | Type inference, legal effects, source/owner relationships, fresh verification and coherent tamper rejection | SOURCE |
| Effects/C8 producer/caller pools, debit propagation and cost-only refusal | Body-effect legality, authentic inputs and fresh result verification | Existing technical owners; C12-restricted work remains inaccessible |
| Reader/H6 capacity-oracle quotas and cost/fit prerequisites | Strict decoding, actual source identity, native carrier legality and authentication | Existing Reader/H6 limits owner |
| Runtime/library/optimizer cost counters, reservations and budget-only tests | Value semantics, MIR/fact consistency, profile/target legality and substitution rejection | Existing runtime, library and pass owners |
| Candidate/task/failure quotas and ordinary per-attempt/preparation permission stages | Conflicting-writer coordination, actual failures, independent acceptance and coordinator integration | Process/tooling owner and coordinator |

Historical metered methods, totals, limits, exact/one-short observations and
failed runs below retain their original scope. They are not current work
requirements or evidence that an unmetered successor passed. Old accounting
selectors and budget-only fixtures are retired requirements; choose the
semantic replacement checks from the owning removal, not a fuel boundary.
Do not rewrite a historical failure, retry or transfer C12's restricted action,
or route around the denied reader-oracle commit. Report any unreachable
removal by its exact mechanism and restriction.

## Default SH-07 workflow: three boundaries

The practical unit of progress is an executable semantic change, not another
lane, routing record, receipt, or authority proposal. These are distinct tasks:

| Boundary | Required work | Trigger |
| --- | --- | --- |
| Semantic implementation | Edit the owning pass; add accepted/rejected fixtures; run focused tests of semantics, preserved facts and stable diagnostics. | Every semantic change. |
| Integration validation | Test the affected upstream/downstream composition and proportionate regressions. | Behavior, input/output shape, identity, diagnostics or interfaces change. |
| Authoritative migration | Fresh provenance, equivalence, bootstrap stage comparison, exact identities and independent acceptance. | Explicit admission of a stable module revision, not every edit. |

D2 requires executable increments and honest milestone ordering; TEST2 requires
pass-level accepted/rejected cases and composition regressions. D9 requires
evidence appropriate to each claim, reproducible provenance and conservative
invalidation. BOOT3 requires fresh equivalence and stage comparison at
**authoritative module migration**, not before every local edit. Compile-time
legality, profile, safety, provenance and release obligations are unchanged.

Reservations, candidate tuples, admission reviews, receipt schemas and terminal
publication come from supplemental admission contracts. Generated test catalogs
and historical attempt-specific grants are tooling/coordination policies, not
language semantics or prerequisites for explicit local test selection. The
repository language boundary and actual host/platform protections remain
separate from proof authority. Project accounting and attempt quotas do not.

Keep paired source fixtures first-class. For extension-independent behavior
with unchanged reader, canonical identity and source-path projection, one real
source composition plus explicit paired byte/parser checks can be proportionate.
Record which spelling actually ran; parser parity does not establish fresh
paired typed/effect/lowering/runtime equivalence. Renew the affected paired
composition when extension handling, reader transport, canonical identity or
path projection changes, or a concrete cross-extension regression requires it.
A new primitive alone does not require rebuilding every product twice.

For SH-07, change `bootstrap/gravity/src/gravity/checked_core.gravity` (the
owning Gravity pass), not only the hosted C6 compatibility facade. The thin
adapter lives in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj`.
Tests are in `bootstrap/clojure/test/gravity/self_hosting/sh07_*_test.clj`;
paired `.gravity`/`.qst` fixtures are under
`bootstrap/clojure/fixtures/self-hosting/`.

Follow the owning boundary when the next change is downstream of SH-07:

| Behavior being changed | Implementation owner | Focused workflow below |
| --- | --- | --- |
| Canonical source/Core shape and legality | `bootstrap/gravity/src/gravity/checked_core.gravity` | Default SH-07 commands here. |
| Namespace/reference binding and core availability | `bootstrap/gravity/src/gravity/resolution.gravity` | SH06 adapter namespace with exact owning resolution selectors; affected source composition when selected bindings or diagnostics change. |
| Reader literal kind in syntax transport | `bootstrap/clojure/src/gravity/c3_literal_projection.clj` and the `00853-sh04-syntax-reader-binding.clj` consumer | Character occurrence transport checks below. |
| Retained source occurrences to the temporary C6 request | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00887-sh07-core-build-form-tree.clj` and its `00890` caller | `gravity.self-hosting.sh07-mutation-test`, exact `sh07-map-source-order-development-kernel`; affected real source composition before accepting a projection change. |
| Function and expression types | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | Loop/recur and the matching type-development section. |
| Body/latent effects and source allowances | `bootstrap/gravity/src/gravity/effects.gravity` | Constructor allocation effects and the matching body-effect kernels. |
| Typed Core to executable development IR | `bootstrap/gravity/p15_s23/emitter.gravity` | Explicit typed input, then the matching lowering section. |
| Development IR evaluation and control | `bootstrap/gravity/src/gravity/runtime.gravity` | Runtime namespace boundary, then the matching execution section. |

Change the owning Gravity implementation, not only a compatibility adapter or
fixture expectation. Select the relevant exact tests; this table is not a
requirement to run every upstream/downstream namespace for each edit.
The temporary request projection is an exception only for transport defects:
it must preserve source occurrences, bindings and evaluation order for the
owning Gravity pass. Fixing that bridge does not move language semantics or
implementation authority into it.

Check `git status --short`, preserve unrelated work, then use existing commands:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --list
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --exact sh07-b5-qualified-var-development-kernel
# Affected SH-06 -> SH-07 artifact composition (costlier, not the inner loop):
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --exact sh07-b5-qualified-current-namespace-preserves-resolution-and-mapping --exact sh07-b5-qualified-var-forged-order-spelling-and-id-fail-authentication
git diff --check
```

These commands target the current qualified-var slice: the direct kernel test
executes the owning Gravity lowerer and validators without constructing the
whole upstream proof product. Its small test inputs are not authoritative
SH-06 evidence. The separate artifact tests exercise authenticated composition.
Use the whole B5 namespace only when broader regression coverage is warranted.
Substitute the owning namespace and tests for a different slice. Prefer exact
semantic vars when a namespace also contains costly authenticated construction or source
coverage. `--list` does not execute tests, but namespace loading can cost time.
Explicit `:dev-test` selections use test resources on the classpath without a
manifest authority entry; unknown namespaces and unmatched selectors fail.
Use a fresh JVM after compiler source changes to avoid stale process-local plans.

Ordinary `compiler-c2-reader-file-artifact` construction executes the existing
authenticated reader directly. It does not require persistent cache
publication or a cache-store budget. Call the existing explicit
`compiler-c2-reader-file-artifact-cached` API when testing incremental reuse;
its current-input revalidation and authentication remain required. Cache
accounting cannot become an input-admission prerequisite. Do not clear shared
state or suppress unrelated compiler failures while removing accounting.

For explicit current-reader development, the source constructors' final
`reader-source-path` argument selects the actual repository reader source and
internally rebuilds its identities. Products remain development-unadmitted;
the default APIs still require the historical reader pins and fail closed on
drift. This does not renew migration authority. Run the existing intake check:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh03-reader-adapter-test --exact sh03-current-source-c3-intake-retains-the-original-reader-revision
```

The public SH-07 command also exposes this explicit development selection:

```bash
bin/gravity sh07-core /absolute/canonical/path/module.gravity --development-current-reader
bin/gravity sh07-core /absolute/canonical/path/module.qst --development-current-reader
```

The flag must follow the single target path. Unknown options, duplicate flags
and extra paths fail with `SH07-CLI-ARGS` before target reading. The public
`gravity.bootstrap/sh07-public-core-file-artifact` API accepts either its
existing one-argument call or a second options map: exactly `{}` or
`{:reader :development-current}`. Other options fail with `SH07-API-OPTIONS`
before target reading. The development route resolves the repository reader
internally; callers cannot supply a reader path or an admission override.
Omitting the flag, or passing `{}`, retains the historical pinned route and
its fail-closed drift diagnostics; there is no automatic fallback.

Both modes retain strict extension and UTF-8 checks. The public decoder reads
the target once. The current reader and macro boundaries still authenticate
the target through their existing before/after freshness snapshots, including
regular-file, ownership and path checks. Supply the canonical physical target
path: a lexical symlink alias such as `/tmp` on macOS can fail the existing C4
source-authentication comparison. After genuine authenticated C5 construction,
C6 consumes its retained source without rereading the target. The focused test
`development-public-api-retains-the-single-target-snapshot` removes its own
target at that boundary and requires an accepted Core product.

The explicit CLI flag or API option selects development mode. The result is
the original Core artifact, with its stored proof and provenance unchanged.
Inspect the actual reader revision already retained at
`[:sh06-resolution-artifact :sh05-macro-artifact :gravity-macro-boundary
:authenticated-sh04-artifact :c2-reader-artifact :sh03-reader-authentication
:reader-source-revision]`. No extra provenance marker or wrapper is added.
The unchanged fresh generic verifier must accept the genuine Core product and
reject a coherently rehashed Core substitution. That verification does not
renew reader migration authority. Core acceptance leaves C7-C10 pending and
does not establish SH-07 completion, migration, native execution or self-hosting. Use the exact
tests in `gravity.self-hosting.sh07-public-development-ingress-test` for the
closed options, paired source-policy and retained-input boundaries. Changes to
this public transport also require actual paired command products and rejected
Core input checks; parser-only parity is insufficient.

When debugging a shared parser or plan-loading error, the existing
`--fail-fast` option stops after the first failed test var, avoiding the same
load-error stack from every selected var. It does not stop within a test var,
and skipped tests are not passes. After fixing the error, run the affected
semantic kernels and proportionate regressions normally.

Selectors apply across all selected namespaces. A global regex can therefore
select more than the exact vars added to a mixed batch; use exact selections
when combining different-cost checks. In the SH08 function-test namespace,
`*-c8-composition` tests construct the public C8 module bundle even though their
inputs are normalized DATA. They are integration checks, not fast
`*-development-kernel` tests. Their bodies and coverage remain available;
renaming them does not require replaying an unchanged expensive proof.

### Explicit bounded closure and storage escapes (SH-10)

For normalized ownership events, change
`bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity`.
L10, SAFE3 and C9 govern this boundary. The existing `:escape-borrow` operation
accepts `:function-return`, `:closure-capture` and `:global-or-static-storage`
only with an active borrow and an explicit finite destination interval no
longer than the owner's interval. Outliving or inactive borrows reject;
unknown destinations and malformed or unbounded coordinates fail closed.

This is an explicit normalized-event interface, not source-level lifetime
inference or permission to assert a false finite bound for global storage.
The existing owned-mutable and runtime-borrow scenario validators preserve the
same destination restriction. Their acquisition rules remain distinct.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh10-bounded-escape-destination-test \
  --exact sh10-bounded-escape-fixtures-development-kernel \
  --exact sh10-bounded-escape-transitions-development-kernel \
  --exact sh10-bounded-escape-adapter-development-kernel --fail-fast
```

The paired fixtures are in `bootstrap/clojure/fixtures/self-hosting/sh-10/`,
under `accepted/` and `rejected/`, named `bounded-escape-destinations`.
The Gravity fixture requests and original C9 pass execute; QST receives byte
and reader parity checks only. Adapter tests use explicitly supplied records,
not a fresh C8 producer or authoritative proof. They check original request
construction, source/origin preservation and result-substitution rejection.
Use the existing ownership-transition tests for affected state-machine
regressions. C10's current read-only ingress does not consume these lifecycle
results; C10/MIR escape propagation and automatic lifetime inference remain
separate implementation work. No migration or milestone status changes follow.

### Exclusive owner writes (SH-10)

The same C9 owner checks normalized `:write` events for initialized, available
owned-mutable values. Direct owner writes require no active immutable or
mutable borrow. Ending one of several immutable borrows is insufficient.
Move, consume and initialization errors keep their existing precedence;
persistent immutable values remain read-only. Write events cannot name a
borrow or any move/escape destination. `:initialize` remains separate.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh10-owner-write-test \
  --exact sh10-owner-write-fixtures-development-kernel \
  --exact sh10-owner-write-transitions-development-kernel \
  --exact sh10-owner-write-shape-and-adapter-fences-development-kernel --fail-fast
```

Paired `owner-write` fixtures under the same SH-10 accepted/rejected directories
construct lifecycle requests; the original Gravity C9 implementation analyzes
them. This tests ownership legality and preserved diagnostics, not physical
storage mutation. Existing return/escape/borrow/move cases are proportionate
regressions. Both current pure-C8 adapter families still reject writes: a
normalized ownership fact does not manufacture mutation effects or authority.
Effectful source integration, C10/MIR propagation and migration remain separate.

### C7 inference index construction

For invocation-local source-join index construction, edit
`c7_type_checker_engine.gravity` and run
`sh08-private-entry-get-cold-inference-development-kernel` in
`gravity.self-hosting.sh08-function-call-type-test` using the direct development
runner. Include the structural-match, nonconvergence, monotone-return and
List-if inference controls when changing fixed-point preparation.

The four immutable lookup indexes are prepared once per admitted inference
invocation. Standalone calls
and verifier invocations prepare independently. Preserve original index inputs,
duplicate-last behavior, Map passthrough and every round-varying fact.
No caller-supplied prepared flag or cross-invocation acceptance cache is
justified by this optimization. The focused comparison checks complete outputs
and semantic rejections; it is not a source migration
proof or a wall-time benchmark.

### Function inference without a work allowance

Remove the caller-selected work-policy API, its default and local fuel
ceilings, effective-policy envelope, shared-pool plumbing and accounting-only
refusals/tests. The old `*-work-policy-*` and `*-cold-and-budget-*` accounting
selectors are not current acceptance gates. Keep any independently useful
type, import, call, effect, source-identity and fresh-verification cases in
ordinary semantic tests. Neither a caller nor a candidate supplies accounting
authority; inference and verification validate the actual semantic products.
Retain historical metered outcomes as history, with no migration or
self-hosting claim inferred from their accounting totals.

### Ordered-Map stored false/Nil: producer-consumer coverage

Run `sh22-ordered-map-lookup-preserves-stored-false-and-nil` in
`gravity.self-hosting.sh22-standard-library-test`. Real G/Q `:ordered-map-assoc`
results feed `:ordered-map-get`: stored false/Nil yield `Some` with an explicit
`:value`, while an absent key yields `None` without `:value`. Check insertion,
replacement/order, original-value preservation, replacement in a 256-entry map
and insertion of a 257th entry, complete malformed/duplicate validation before a hit and
fresh producer/consumer verification with result substitutions. The same
original-delegating lookup observer must record a valid lookup as well as zero
calls for rejected carriers. Existing collection/boundary and result-verifier
selectors are affected controls. This is test-only request-module coverage,
not a production repair, new semantics, compiled C6 client or admission claim.

### Bounded Keyword-key Map removal

STD2/STD3 govern `:ordered-map-dissoc-keyword` in the existing SH-22 request
API. Complete indexed pair/Keyword-key census and an actual
Keyword needle precede user-key equality; full uniqueness precedes removal.
Preserve remaining order, all stored values and the original Map. Empty or
missing keys produce a value-equivalent Map; false/Nil do not mean absence.

Run `sh22-keyword-map-removal-consumes-real-association-results` in
`gravity.self-hosting.sh22-standard-library-test`, plus the affected whole
namespace. Real assoc results feed removal and get; original-Map lookup and
fresh producer/consumer verification remain independent. Keep positive and
rejected controls for the same original-delegating observer. The opt-in `:meta`
request declares `:memory/allocate`, no capabilities and `:bounded-by-result`,
even when an absent-key result reports collection-value allocation `:none`.
This promises neither physical sharing nor global allocation freedom. Values
do not select removal; result verification still compares full results.
General keys, W5 metadata redaction/linkage, public admission and native
execution are not established.

### Checked i64 subtraction in the bootstrap library

STD1/STD5/D8 govern `:checked-sub-i64` in
`bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity`. The existing
experimental `:meta` request interface requires opt-in, no effects/capabilities
and `:none` allocation. It checks binary arity, integer family and i64 range
before overflow guards and arithmetic. Negative right operands use a guarded
result-minus-one decomposition, avoiding the seed builtin's negation of MIN.
`STD5001` retains integer type/range/overflow diagnostics; wrong arity retains
`STD1001`. The request/result schema and four exported entrypoints are unchanged.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-checked-i64-subtraction-executes-before-its-result-is-verified \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity \
  --exact sh22-policy-and-manifest-state-bounded-experimental-surface --fail-fast
```

These checks execute paired library fixtures and the original Gravity arithmetic
and verifier. They do not establish compiler linkage, native execution or the
complete SH-22 API closure. Historical SH07 source/digest pins are not updated;
their evidence does not authenticate the changed library source revision.

### Checked i64 multiplication in the bootstrap library

The same STD1/STD5/SAFE9/D8-owned library executes `:checked-mul-i64` through
the unchanged experimental `:meta` request interface. Binary arity, integer
family and i64 range checks precede zero handling and sign-specific quotient
thresholds. Divisors are nonzero and no threshold forms MIN/-1; multiplication
runs only after the in-range guard. Results retain `:checked-i64`, no effects or
capabilities and `:none` allocation. `STD5001` retains type/range/overflow failures
and `STD1001` retains wrong arity; the owner-local operation surface grows to 15.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-checked-i64-multiplication-guards-actual-seed-quotient-thresholds \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity \
  --exact sh22-policy-and-manifest-state-bounded-experimental-surface --fail-fast
```

The paired cases execute the actual seed quotient thresholds and Gravity product
in both operand orders, including MIN/zero, signed endpoints and the square
overflow boundary. They are not compiler linkage, native/provider, complete
library or migration evidence. Historical SH07 source/digest pins remain
unchanged and do not authenticate the revised library source.

### Checked nonnegative i64 quotient in the bootstrap library

STD5/SAFE9 govern `:checked-nonnegative-quot-i64` in the existing opt-in `:meta`
request API: dividend `0..MAX` and divisor `1..MAX`. Arity, both actual Integer
types and signed-i64 ranges, then the explicit domain checks precede `quot`.
The checked Result contains an exact integral quotient between zero and the
dividend; query effects/capabilities are empty and value allocation is `:none`.
`STD1001` retains arity, `STD5001` type/range and `STD5002` domain failures.

Run `sh22-checked-nonnegative-i64-quotient-checks-before-division` in
`gravity.self-hosting.sh22-standard-library-test`; the whole namespace is the
affected check for policy, manifest, paired fixtures and fresh recomputation.
Include zero/boundary inputs, rejected zero/negative divisors, precedence and
real checked-sub/mul results feeding quotient. The original-delegating observer
must see no `quot` on rejected inputs. This is request-only behavior motivated
by reader decimal normalization, not reader linkage, signed-negative rounding,
remainder/general-Integer division, runtime/native or admission evidence.

### Decimal digits through checked request Results

Run `sh22-decimal-digits-compose-checked-request-results` in the same SH-22
namespace, with the existing checked quotient, multiply, subtract and add
controls. The test-only driver consumes actual quotient-to-multiply-to-subtract-
to-add-48 Results for 9, 10, 11, 123 and MAX, using at most 19 iterations.
Zero returns `[48]` in the driver without arithmetic requests. Each later
operand comes from the prior Result; deterministic request order, input
preservation, fresh per-request verification and substitutions are checked.
Negative producer/domain results retain their existing rejection behavior.
This adds composition coverage, not operations or numeric semantics. The reader
helper was neither compiled nor invoked; reader linkage, signed rounding,
arbitrary-precision division, Unicode admission and C6 proof are not claimed.

### UTF-8 revalidation before stdout request construction

STD4/STD8 govern the existing `:io-stdout` consumer. After the original authority,
carrier and byte-bound checks, its recomputed scalar count must be an Integer
before comparison with the supplied count. Invalid UTF-8 returning nil cannot
match a supplied nil and produce a provider-request descriptor; the existing
`STD4001/:validated-text-facts-mismatch` rejection remains. Run
`sh22-stdout-revalidates-utf8-before-building-provider-request` with the adjacent
UTF-8, IO, rejection and verifier controls in the SH-22 namespace. Real validation
Results cover empty/ASCII and multibyte boundaries; the same observer sees valid
descriptor construction and none for the selected rejected sequences. Fresh
verification and substitutions remain required. No provider IO is executed,
and this guard does not establish universal malformed-byte or host-failure freedom.

The four special UTF-8 leads E0, ED, F0 and F4 now classify the second byte with
the existing Integer continuation-byte predicate before their narrower range
checks. Original valid ranges and third/fourth-byte check order remain. Run
`sh22-special-utf8-leads-classify-second-bytes-before-range-checks` with the
adjacent controls above. Actual validate-to-stdout Results cover valid endpoints;
selected non-Integer second bytes and out-of-range Integers reject with the
existing STD4001 diagnostics. Earlier authority and collection-bound failures
retain precedence. This is a bounded classifier repair, not full text/IO support,
provider execution or migration evidence.

### Keyword membership in bounded ordered Sets

STD2/STD3 govern `:ordered-set-contains-keyword` in the same SH-22 leaf. It
accepts the ordered-set carrier with actual Keyword members and an actual
Keyword needle. The full family census precedes member
equality; full uniqueness validation precedes a query hit. Qualified Keyword
value equality determines Bool membership independently of insertion order.
The query preserves its input and neither invokes payloads nor grants authority
from Keyword spelling. General Sets, mixed/non-Keyword families and host-provider
interop are not supported. `STD3001` retains the bound failure, `STD10003`
rejects malformed/duplicate/family inputs and `STD1001` rejects wrong arity.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-keyword-set-membership-executes-with-full-family-and-uniqueness-checks \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity \
  --exact sh22-policy-and-manifest-state-bounded-experimental-surface --fail-fast
```

The operation remains experimental `:meta` with explicit opt-in, no effects or
capabilities and no collection-value allocation. Result/envelope metadata is not
a native allocation-free claim. Opaque arguments admitted by the common carrier
reject at the Keyword family check before uniqueness/search; origin validation
is separate. This does not claim universal opaque-carrier rejection or absence
of host printing callbacks. No compiler linkage, general Set equality, native
execution, complete library or migration claim follows. Historical SH07 pins
remain unchanged and do not authenticate the revised library source.

### Bounded persistent Vector append and consumers

STD3's binary Vector append law governs `:vector-append` in the same SH-22
leaf. An input shorter than 256 elements produces its unchanged ordered prefix
and one inert appended value: 255 becomes 256; inputs of 256 or 257 reject
before construction (`STD3001`). Non-Vectors reject with `STD3002`; wrong arity
and allocation policy retain `STD1001`. Experimental `:meta` opt-in requires
`:memory/allocate`, no capabilities and `:bounded-by-result`, even for empty
input. The four exports and request/result envelope are unchanged.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-vector-append-executes-with-real-nth-and-assoc-consumers \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity \
  --exact sh22-policy-and-manifest-state-bounded-experimental-surface --fail-fast
```

The direct check executes append then real nth/assoc consumers and freshly
recomputes all results. It covers heterogeneous false/nil/nested payloads,
persistence, authority/substitution failures and original-delegating constructor
counts. Exact new dispatch/allocation discriminators reject nil before append;
the older general `sh22-member?` nil weakness is not repaired. Initial RED was
1/270 with 160 failures; historical GREEN 8/538 omitted this regression. Its
test-only RED had 63 total assertions (62 pass, one host stack error), with
later assertions in the sole selected var unrun. An unsupported-`or` successor
had one error assertion and seven selected vars unrun. Supported nested `if`
correction passed 8/546, including all 278 append assertions; failed/partial
epochs are not added coverage. No variadic/coercing behavior, physical sharing,
unbounded-profile, native allocation or host-callback-free guarantee follows.
Compiler linkage, full SH-22 and self-hosting remain pending; historical SH07
pins are unchanged and do not authenticate this changed source revision.

### Symbol membership after bounded ordered-Set insertion

STD2/STD3 govern `:ordered-set-contains-symbol` in the same SH-22 leaf. The
ordered carrier admits actual Symbols and a Symbol needle. Complete
member/needle family checks precede equality, and full
uniqueness validation precedes a hit. Qualified Symbol value equality determines
Bool membership independently of insertion order, without spelling coercion or
execution authority. Remove capacity-only `STD3001` refusals; `STD10003` rejects
malformed/duplicate/family inputs and `STD1001` rejects wrong arity.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-symbol-set-membership-consumes-real-insertion-results \
  --exact sh22-keyword-set-membership-executes-with-full-family-and-uniqueness-checks \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity --fail-fast
```

The check consumes real existing `:ordered-set-conj` Symbol results and freshly
recomputes insertion and query results. It covers qualified independently
constructed values, input preservation, 256/257, duplicate-after-hit, family,
authority, nil-operation and substitution failures without payload invocation.
Test-only RED against prior production was 1/304 (116 pass, 188 fail). The
complete GREEN batch at `f3990902` passed 10/1106, including all 304 new
assertions. They are separate epochs. Experimental `:meta` opt-in, no
effects/capabilities and `:none`
collection-value allocation remain explicit; envelope metadata is not native
allocation evidence. General Sets, mixed families, host-provider/callback safety,
compiler linkage, full SH-22 and self-hosting are not established. Historical
SH07 pins remain unchanged and do not authenticate this revised source.

### Integer membership after bounded ordered-Set insertion

STD2/STD3/STD5 govern `:ordered-set-contains-integer` in the existing SH-22
request API. Admit actual Integer members and an Integer needle;
this is family classification, not signed-i64 narrowing or arithmetic. The
complete indexed Vector census and member/needle checks precede any
equality, and complete uniqueness validation precedes the membership query.
Late invalid members or duplicates cannot hide behind an early hit. Preserve
the Set and actual malformed-input/arity diagnostic boundaries, without a
capacity-only refusal.

Run `sh22-integer-set-membership-consumes-real-insertion-results` in
`gravity.self-hosting.sh22-standard-library-test`; the affected whole namespace
is a proportionate 512 MiB check for policy, manifest, paired fixtures, routing
and recomputation. Include real `:ordered-set-conj` results, reader codepoint
and compiler/runtime arity values as DATA, 256/257, values beyond signed64,
wrong families, duplicate-after-hit, authority and result-substitution controls.
The experimental opt-in `:meta` surface has 20 operations; four public exports
and the envelope stay unchanged. Query effects/capabilities are empty and
collection-value allocation is `:none`. This request API does not establish
ordinary source Integer-Set lowering, whole-client linkage, Unicode ingress,
general mixed-family Sets, native allocation, migration or SH-22 completion.

### Bounded Keyword Set value equality

STD2/STD3 govern `:ordered-set-equal-keyword` in the same experimental SH-22
leaf, with checked signature
`(OrderedSet Keyword) (OrderedSet Keyword) -> (Result Bool CollectionError)`.
Both ordered carriers must contain actual Keywords. Complete Keyword-family
checks precede member equality; both
uniqueness validations precede a count mismatch or apparent matching prefix.
Equal valid Sets have equal counts and no unmatched member, independently of
stored order. The query preserves both inputs and does not invoke payloads.
`STD3001` retains bounds failures, `STD10003` rejects malformed/duplicate/family
inputs and `STD1001` rejects wrong arity. Explicit `:meta` opt-in, no effects or
capabilities and `:none` collection-value allocation remain required.

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh22-standard-library-test \
  --exact sh22-keyword-set-equality-consumes-real-insertion-results \
  --exact sh22-keyword-set-membership-executes-with-full-family-and-uniqueness-checks \
  --exact sh22-engine-and-co-canonical-fixtures-compile-as-gravity --fail-fast
```

The check consumes real existing Set insertion results and recomputes producer
and equality results. At executed candidate `42ba1831`, the complete batch
passed 11/1436, including all 322 new assertions. Prior-production RED remains
separate: 1/322, 150 pass, 172 fail, no errors. The four exports and request/
result envelope are unchanged; there is no general/mixed-family equality,
native allocation, host-callback safety, compiler linkage or SH-22 completion
claim. Historical SH07 pins do not authenticate this revised source.

String membership remains unimplemented: L1 requires Unicode-scalar Strings,
while STD4's `string?` grants no validation fact. The current SH-22 carrier only
bounds host String units; ordinary stage2 equality does not reject a lone UTF16
surrogate. Existing strict UTF-8 helpers prohibit SH-22/public runtime callers.
Checked String ingress or an independently established validated carrier is a
separate owning-interface dependency, not permission to weaken String semantics.

### C6 compiled-source aggregate binding maintenance

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh07-c6-compiled-source-binding-test \
  --exact sh07-c6-compiled-source-binding-retains-full-plan-identity --fail-fast
```

This original compile/validate check retains all three declared compiler-identity
fields in the full aggregate preimage, its 139-function manifest and strict
source/export pins. It rejects independently re-digested metadata mutations and
the historical three-field-dropped aggregate. Updating `00701` preserves the raw
stage0-seed boundary; it does not authenticate today's expression emitter.
Public C6 subject ingress separately loads that live emitter through `00581`,
whose retained source/semantic binding is not renewed by this development check.
There is no new C6 subject, C11 acceptance, migration or bootstrap evidence.

### MIR branch argument carriers

For normalized SH-13 branch transfers, edit `c11_mir_specification.gravity` and
run `sh13-branch-transfers-require-vector-carriers` in
`gravity.self-hosting.sh13-control-flow-test` with the direct development runner.
Both conditional arms require Vector argument carriers before count or indexed
access, even when execution selects the other arm. Preserve missing-target,
arity/type and dominance precedence; missing/Nil is not an empty Vector.
Use the existing invalid-MIR, recursion and runtime-bound checks for affected
regression feedback. This boundary check is not authenticated SH-12 ingress or
SH-13 completion evidence.

For module rejection before entry lookup, run
`sh13-rejects-malformed-modules-before-entry-lookup` in the same namespace.
Once module validation rejects, return its original diagnostic without the
executor's entry lookup or argument checks, not a host exception. Preserve
accepted execution and original verification recomputation.

For constant instruction arity, run
`sh13-constants-have-no-value-operands` in the same namespace. Normalized
`:const` instructions require an empty operand Vector; defined but unused
operands are not legal. Keep schema, profile, fact, effect and dominance
diagnostics ahead of this type check. Include the branch-carrier, module-entry,
invalid-MIR and recursion selectors for affected composition feedback. The
original G/Q constructors, executor and recomputation verifier exercise this
normalized boundary; they do not provide fresh authenticated SH-12 ingress.

For value-return diagnostic classification, run
`sh13-return-values-must-be-defined-before-type-checking` in the same namespace.
An unavailable normal return value is `C11-DOMINANCE`, as for the existing
structured exits; a defined value with the wrong type remains `C11-TYPE`.
Preserve earlier source-span and origin checks. Include constant-arity,
invalid-MIR, structured-exit and branch-execution selectors for affected
regression feedback; this does not admit a new upstream compiler revision.

For conditional-branch value classification, run
`sh13-conditional-values-must-be-defined-before-type-checking` in the same
namespace. Check that the condition is defined before checking its Bool type:
missing, Nil and later-block value IDs report `C11-DOMINANCE`; defined non-Bool
values remain `C11-TYPE`. Preserve earlier opcode/source/origin and operation
checks, then condition definition/type, then the original then-before-else
target checks. Include conditional execution, loop/indirect-call, return
dominance and branch-carrier selectors. This repairs a rejected-input
diagnostic, not accepted behavior, schemas, interpreter or authority status.

For calls to later normalized functions, run
`sh13-callee-parameter-carriers-are-validated-before-counting` in the same
namespace. Check the callee's parameter Vector before counting it; malformed
carriers reject with `C11-MODULE`, not a host exception. Preserve missing-callee,
dispatch and earlier operand/profile/fact/effect diagnostics, plus valid call
arity, type and execution behavior. Include invalid-MIR, recursion, constant
arity and return-dominance checks when changing this boundary.

For normalized Bool value boundaries, run
`sh13-normalized-bool-values-exclude-nil-and-nonbooleans` in the same
`gravity.self-hosting.sh13-control-flow-test` namespace. Both parameter values
and constant payloads must be actual `true` or `false`; Nil is not Bool and
must not reach execution as a false condition. Check constants in unselected
blocks too. Preserve earlier module, profile, fact, origin, effect and dominance
diagnostics. Include branch execution, runtime-bound and recomputation controls
when changing either predicate. This is typed MIR validation, not a change to
source-language truthiness or fresh upstream admission.

### MIR integer less-than-or-equal placement

Edit `c11_mir_specification.gravity` for the bounded SH-13 interpreter and
original SH-12 producer bridge. The normalized operation domain now includes
`:lte`: two already-defined signed-i64 operands produce Bool. The bridge maps
original `:integer-lte` to it with the explicit
`:integer-less-than-or-equal` projection role in a seven-row `[3,1,1]`
entry-comparison diamond with scalar literal arms. A literal Bool entry also
supports one three-operation `<=` arm, with widths `[1,3,1]` or `[1,1,3]`.
Both arms validate before execution. Two `<=` comparisons also compose in
nine-row `[3,3,1]` or `[3,1,3]` diamonds. Mixed Eq/Lt comparisons or truthiness
combined with a `<=` arm, and two computed `<=` arms, remain rejected; Eq/Lt arm support
is unchanged. Original source/fact, CFG/DFG, origin, type, effect and recomputation
checks remain. Remove depth/fuel quotas and their accounting assertions.
No public semantic keys or arities change.

Run `sh13-executes-integer-less-than-or-equal-entry-conditions` and
`sh13-executes-integer-less-than-or-equal-only-in-the-selected-arm` in
`gravity.self-hosting.sh13-branch-join-execution-test` and
`sh13-normalized-less-than-or-equal-checks-i64-operands` in
`gravity.self-hosting.sh13-control-flow-test`. Include the existing bridge,
normalized control-flow and original SH-12 producer controls when changing the
shared comparison validators. Nil and other non-i64 operands reject.

At `a20b282d`, five passing invocations cover 46 tests / 10,889 assertions,
including boundary checks. LOC separately passed 2,022 files, zero exceptions,
maximum 250 physical lines. The earlier bridge RED remains 220 passes and
180 failures in 1 / 400; the normalized selector was not part of that RED.
Preserve `/tmp/d-sh13-lte-*-v1-20261021` results. These supplied G/Q constructor
and original producer/verifier compositions are development evidence, not
fresh C6/C10 source adaptation, native execution, SH-13 completion or migration.
Future stable consumers must account for the additive opcode/projection domain;
this development merge does not renew historical admission evidence.

The selected-arm increment at `2b4406e8` reuses the existing opcode and role,
without changing public semantic keys or arities. Four historical passing
invocations cover the new arm, retained bridge,
normalized signature/Nil/bounds/overflow and boundary checks: 28 tests / 9,356
assertions. LOC separately passes 2,022 files, zero exceptions, maximum 250.
Retain `/tmp/d-sh13-lte-arm-*-v1-20261021`. The separate test-only RED contains
452 passes, 648 failures and 72 errors in 1 / 1,172; those errors inspect absent
projection roles after subset rejection, not failed comparison execution.
Literal witnesses exercise placement used by C11's guarded predicates, not
whole-helper execution, fresh source adaptation, native execution or admission.

For two-sided span guards, run `sh13-composes-two-integer-lte-span-guards` in
the same bridge namespace. At `d3653edc`, four passing invocations cover the
new composition 1/636, affected bridge 18/8,810, normalized controls 4/499 and
boundary 6/11: 29 tests / 9,956 assertions. LOC separately passes 2,022 files,
zero exceptions, maximum 250 lines. Preserve `/tmp/d-sh13-double-lte-*-v1-20261021`
and its separate test-only RED 276 passes / 360 failures in 1/636. Removing
36 now-obsolete composition-rejection assertions changes the old entry/arm
counts to 384/1,152; it does not relabel earlier runs. Independent literal
pairs model C11's `(if (<= 0 start) (<= start end) false)` placement, not shared
variable dataflow, member lookup, arbitrary Integer width or actual helper execution.

### MIR integer greater-than placement

Edit `c11_mir_specification.gravity` and run
`sh13-executes-the-bounded-integer-greater-than-family` in
`gravity.self-hosting.sh13-branch-join-execution-test`, plus
`sh13-normalized-greater-than-checks-i64-operands` in
`gravity.self-hosting.sh13-control-flow-test`. The normalized `:gt` operation
compares exactly two available signed-i64 operands and returns Bool. The
original `:integer-gt` bridge preserves the explicit `:integer-greater-than`
role and original source, facts, CFG/DFG, incoming values and operation IDs.
It supports seven-row entry comparisons with scalar literal arms, seven-row
literal-Bool entries with one GT arm, and nine-row GT-entry/GT-arm diamonds.
Both arms validate before execution. Mixed GT/Eq/Lt/LTE, truthiness, DO/let and
both-computed-arm GT layouts remain excluded. Remove depth/fuel quotas.
This adds an opcode/projection
domain, not public keys or arities; future migration must account for it.

At `342610b9`, four passing invocations cover new bridge 1/2,160, retained
bridge 19/9,446, normalized namespace 22/2,813 and boundary 6/11: 48 tests /
14,430 assertions. LOC separately passes 2,022 files, zero exceptions, maximum
250 lines. Retain `/tmp/d-sh13-gt-*-v1-20261021`, including the separate RED
900 passes / 1,260 failures in 1/2,160; normalized tests were not RED-selected.
The C11 capability helper supplies a concrete positive-length guard client,
but literal count witnesses do not prove count execution, capability authority,
whole-helper execution, fresh source/native composition or SH-13 completion.

### Scalar SSA forwarding in normalized MIR

For a pure scalar copy, edit `c11_mir_specification.gravity` and run
`sh13-forwards-defined-scalar-local-values` in
`gravity.self-hosting.sh13-control-flow-test`. The additive normalized opcode
`:local` forwards exactly one already-defined SSA operand of the same i64,
Bool or Keyword type. It consumes one existing interpreter step and retains
the original operation ID in the trace. Missing operands, arity/type mismatches,
duplicate results, effects and earlier source/fact failures still reject.
Include conditional execution, loop/indirect-call, return and conditional
dominance controls when changing this operation. A normalized value copy is
not proof of source lexical binding or fresh upstream adaptation.

### Execute an original SH-12 diamond

For the bounded original-MIR handoff, edit `c11_mir_specification.gravity` and
run the existing direct development runner against
`gravity.self-hosting.sh13-branch-join-execution-test`. Its eleven selectors cover
the interface, literal and computed conditions, original MIR tampering and
subset/result rejection:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh13-branch-join-execution-test --exact sh13-original-branch-join-interface-is-explicit --exact sh13-executes-original-verified-diamonds --exact sh13-rejects-original-mir-tampering-before-execution --exact sh13-execution-subset-and-result-substitution-fail-closed --exact sh13-executes-typed-integer-equality-conditions --exact sh13-executes-typed-integer-less-than-conditions --exact sh13-executes-integer-less-than-only-in-the-selected-arm --exact sh13-executes-integer-equality-only-in-the-selected-arm --exact sh13-composes-entry-and-selected-arm-comparisons --exact sh13-preserves-literal-keyword-return-values --exact sh13-executes-branch-local-scalar-let-with-original-identities --fail-fast
```

`sh13-run-branch-join-mir` takes the retained checked-Core and original MIR;
`sh13-verify-branch-join-execution` also takes the proposed execution result.
Each run calls the original SH-12 canonical-projection verifier before creating
private frames for the existing interpreter. A supplied verification report is
not an input. The normalized `sh13-run-module` interface remains unchanged.

Support is one zero-argument, four-block pure diamond. Its condition is either
a literal Bool, equality or strict less-than of two bounded signed-i64 literals;
its arms are homogeneous literal signed-i64, Bool or Keyword values. Validate
actual Keyword payloads with `keyword?`, not declared types or String spelling;
preserve namespaced Keyword values without coercion. Alternatively, a
literal or computed Bool condition may select between one equality or strict
less-than comparison of two signed-i64 literals and one literal Bool arm.
A join and function-result alias follow. Entry and one arm may both compute;
both arms cannot compute comparisons. Each comparison must have exactly two ordered
i64 operand IDs, a Bool result and a nonconstant payload marker. Original SH-12
verification alone does not establish that primitive signature.
Validate both arms before execution, including the unselected arm. Preserve
original CFG incoming values, IDs, fact references and source records; transfer
the selected arm value explicitly into the join. Only that arm executes.
The result's input IDs
and projection are dependency views, not complete content-hash authentication.
The computed cases explicitly extend the projection domain with seven or nine rows
and an `:integer-equality` or `:integer-less-than` role; existing keys and public
arities are unchanged. Keyword arms add only the projection runtime-type value
`:keyword`, not a new role or grammar. Conditions and computed-arm results
remain Bool-only. Other ordering comparisons and arithmetic are excluded.
Five-node results remain unchanged. Remove frame fuel and its accounting
assertions. An unselected arm does not execute; its complete original
source/fact projection and pre-execution validation still remain.

Include the existing SH-12 diamond and normalized SH-13 branch, recursion and
loop/indirect-call controls when changing this handoff. Calls, loops, effectful
nodes, runtime checks and broader MIR remain outside the new entrypoint. These
supplied checked-Core checks do not establish fresh C10/source ingress, generic
MIR acceptance, native execution or authoritative migration.

The separate eight-node lexical layout has a literal Bool condition, one
branch-local scalar `let` and one matching literal arm. The initializer,
binding, local reference and scope retain their original SSA IDs. Validate
actual Symbol names, resolved binding ID, one non-shadowing binding, plural
`:bindings` paths, operand order and source/fact records in both arms before
execution. The initializer executes once when selected; three forwarding
operations preserve the value, with scope forwarding its local-result operand.
Remove the execution-fuel requirement. This adds
three projection roles (`:lexical-binding`, `:local-reference`, `:lexical-scope`)
and an eight-row domain, not new public keys/arities. It excludes combined
entry comparisons, two lexical arms, parameters, captures and collections.

These witnesses use supplied SH-12 `:gravity.type/*` scalar facts and retained
lexical records. Actual C6/C10 emits a different `:gravity/*` type family and
truthiness nodes; a truthful type/truthiness adapter is still a separate source
integration dependency. A retained source-form ID is not a file/origin anchor.
Do not relabel supplied checked-Core execution as fresh source provenance.

For the separate bounded truthiness prerequisite, run
`sh13-normalizes-selected-scalar-truthiness` in the control-flow namespace and
`sh13-executes-original-scalar-truthiness-before-branching` in the branch-join
namespace. The pure normalized `:truthy` opcode requires one defined Bool,
i64, Keyword or explicit Nil operand and a Bool result. False and Nil yield
false; zero, negative integers and Keywords yield true. Strings and collections
remain unsupported here, not redefined as truthy.

The original producer bridge adds exactly the six-node `[2,1,1]` layout:
literal input and truthiness in entry, then two literal arms. Its new
`:truthiness` projection role retains original IDs, source/fact rows, full CFG
and selected trace. The SH-12 source-path
gate preserves the existing `if-path + :condition` prefix and additionally
accepts only source-op `:truthy` at the exact `if-path + :truthy` path. Bridge
input must be at `if-path + :test`; retained metadata must have fixed
`:false-values [nil false]` and canonical Bool result type. It is not a caller
truthiness policy. Check wrong operators, suffixed paths, contradictory
metadata and unselected-arm domains, plus the existing diamond and layouts.
This explicitly adds an upstream accepted path and normalized opcode/role;
it does not adapt actual C6 type families or renew pinned source authority.

For explicit Nil transport, run
`sh13-carries-explicit-nil-through-existing-scalar-transfers` in the control-flow
namespace and `sh13-executes-explicit-nil-carriers-without-fabricating-values`
in the branch-join namespace. This is a scalar-carrier extension, not an
input-only condition exception: existing matching local, parameter, branch,
backedge, allowed call, return, typed-exit and same-type equality paths carry
Nil. Existing effect and type checks still apply; arithmetic, ordering and
mixed-type equality do not become legal.

A normalized Nil constant must contain an explicit `:literal` key with Nil.
The original-producer bridge requires both an explicit payload `:value` and
an original `:literal` node whose attributes contain `:value nil`. Check all
nodes before execution, including unselected and lexical arms. A missing field
is not a Nil witness. Original `:implicit-nil` and `:quote` nodes remain outside
this bridge subset. Retain existing Bool/Nil separation, source/effect, schema,
argument-arity and result-substitution controls. This extends the declared Nil
value/projection domain, not public keys, arities, opcodes, roles or limits.

For a pure two-expression `do` in one branch arm, edit C11 and select
`sh13-executes-two-expression-do-only-in-the-selected-arm` plus
`sh13-do-arms-keep-the-literal-condition-placement-boundary` in the branch-join
namespace. For the existing scalar truthiness condition, also select
`sh13-composes-scalar-truthiness-with-two-expression-do`. The condition is a
literal Bool or a validated literal followed by the existing truthiness
operation. One arm contains two closed
scalar literals and their original `:sequence`/`:do` result; the other is a
matching literal. Validate ordered child IDs, `:body 0`/`:body 1` paths,
`:body-count 2`, last-value type and all source/fact records in both arms before
execution. Nil still requires explicit original value fields.

Both selected literals execute exactly once left-to-right, then the existing
SSA-forwarding interpreter returns the second value under the original sequence
ID. The new projection role is `:sequence-result`, not a new interpreter or
normalized opcode. Literal conditions use seven-row `[1,3,1]` or mirrored
layouts. Truthiness conditions use eight-row `[2,3,1]` or mirrored layouts.
The latter require the three-operation arm to end in `:sequence`, not a
comparison. Computed Eq/Lt entry plus `do` remains unsupported; existing
nine-row comparison layouts remain supported. Remove depth/fuel quotas.
Retain scalar/Nil/lexical, old layout, original verifier and result
substitution regressions. Empty/nested/effectful bodies, calls and fresh C6
adaptation are outside this slice.

### Normalized MIR constant-fold result types

For the fixed-i64 C13 fold domain, edit
`bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity`
and run the owning accepted/rejected regression:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh16-mir-optimization-test \
  --exact sh16-constant-folds-only-an-established-i64-result-domain --fail-fast
```

Bounded numeric operands alone do not establish a result type. Folding requires
the normalized operation's explicit `:type :i64`, in addition to existing
constant, arity, signed-range and overflow checks. Preserve earlier contract,
source, nondeterminism, identifier and effect diagnostics. Include the existing
sequential-pipeline, no-op/schema, recomputation and proof-governed check-elision
selectors for affected integration feedback.

Canonical opaque type IDs remain Keywords or Symbols, not Strings. Their
spelling does not establish i64 semantics. Ordinary explicitly constant
non-i64 additions fail the existing `C13-SAFETY` gate; other nonconstant or
audited unsafe-island operations may remain accepted unchanged without folding.
This is not a general type checker, fresh authenticated C11 ingress, native
execution, authoritative migration or SH-16 completion.

For the closed one-hop propagation route, run
`sh16-propagates-only-unique-pure-prefix-i64-constants-into-one-add` in the same
namespace. Each admitted segment starts with a pure, proven-safe, nondead const
prefix, followed by a pure, proven-safe, nondead
`:i64`/`:integer-exact` ADD with `:constant-operands false` and two ordered
operands. At least one references an established earlier one-payload i64
producer; the other is another such reference or a bounded i64 immediate.
Repeated references remain supported, and result definitions remain unique
across the whole input.
Fresh bounded indexing joins actual result IDs; the existing signed-i64
overflow rule still governs folding. Unknown, forward, ambiguous, wrongly
typed, unsafe, effectful or control-dependent joins do not authorize folding;
unsupported cases stay unchanged unless existing ingress rejects them.
Preserve original source/origin/result identity, input references, deterministic
decision/invalidation records and fresh recomputation. This normalized linear
subset is not general CFG/SSA propagation, fresh C11 input or native execution.

For named intermediate results, also run
`sh16-propagates-bounded-pure-named-add-chains-until-a-barrier`. The same forward
scan records each independently resolved, overflow-checked ADD value under its
original globally unique result ID and continues with the same two-operand,
i64/integer-exact, pure/proven-safe/nondead rules. A nonqualifying operation
stops new propagation, including otherwise resolvable suffix ADDs, except for
the closed pure-compute reset below; successful prefix folds remain. Existing
immediate-literal ADD folding elsewhere is
unchanged and does not automatically supply a new chain producer. The existing
operation-count quota is removed, as are its count/forward-row ceilings.
This is not a fixed point or a CPU-time claim. Original request references/flags,
source/origin/result identity, decision order, invalidation and fresh
recomputation remain intact; the same normalized-DATA evidence limits apply.

For an independent segment after opaque computation, run
`sh16-propagates-only-independent-constant-segments-after-pure-compute`.
An actual pure, proven-safe, nondead `:compute` remains byte-for-byte unchanged
in operation order and clears the producer environment before the same forward
scan continues. Only subsequent independently admitted constants and ADDs can
supply facts: neither pre-compute facts nor the compute result authorize a
fold. Effectful, unsafe or dead compute and all other existing barriers still
stop propagation. Preserve global result uniqueness, overflow checks, original
input/source/result identity, ordered decisions, post-fold DCE and fresh
verification. Remove count/forward-row quotas; local DCE preparation and
independent replay still perform their semantic work.
This does not evaluate or remove compute, add a pass/fixed point, or establish
general CFG/SSA propagation, global CSE, native execution or a speed claim.

For repeated references, run
`sh16-propagates-two-uses-of-one-established-i64-producer`. Both operand
occurrences are independently looked up, including two uses of one established
constant or folded intermediate; this is not algebraic multiplication or
general SSA propagation. Unknown, forward, ambiguous, wrongly typed, unsafe,
effectful, dead or overflowing producers still cannot authorize folding.
Keep original references/source/result identity, barrier behavior, post-fold
DCE, conservative invalidation and fresh result recomputation controls.
The existing signed-i64 negative overflow bound handles a right operand of
MIN with its equivalent zero threshold, avoiding hosted negation of MIN.
Immediate `[0, MIN]` and `[MAX, MIN]` fold to MIN and -1; negative-left plus
MIN is not foldable and ordinary explicit constant ingress still rejects it
with `C13-SAFETY`. No type, width, coercion, API or input-limit policy changes.

For mixed operands, run
`sh16-propagates-one-established-reference-and-one-i64-immediate`. The new
route requires exactly one actual producer join and one bounded i64 atom,
in either order, including a folded intermediate as producer. Canonical
identifier checks precede producer lookup; an integer cannot gain identifier
meaning from a supplied map. Two immediates with `:constant-operands false`
remain unchanged, while the existing all-reference route is preserved.
Keep both operand positions and original input flags/identity, signed-range
and MIN-right overflow rules, barriers, post-fold DCE and fresh recomputation.
Missing, ambiguous, forward, non-i64, impure, unsafe, dead and overflowing
joins do not authorize propagation. These remain normalized-DATA checks,
not fresh C6/C11 ingress, provider/native execution or performance evidence.

For post-fold local cleanup, run
`sh16-removes-only-unused-constants-created-by-fresh-folding` and
`sh16-retires-only-original-constants-consumed-by-fresh-reference-folds`.
The existing DCE pass freshly reconstructs the fold selection from the original
request and matches each fold-created constant's exact transformed operation
against its current input. Original constants are eligible only when their
canonical result IDs occur as actual references in ORIGINAL freshly selected
pure ADD folds, not as immediate payloads or supplied decision entries; their
current operations must exactly equal the original producer records.
Both routes require nondead, pure/proven-safe, one-payload bounded i64 consts,
globally unique original/current result definitions and no current operand
uses. Unrelated constants and used final results remain. A current user still
retains its producer even if that user is deleted in the same pass: there is
no fixed point or generic constant, compute or control deletion.

Existing explicit dead-proof selections keep their validated proof IDs; local
cleanup contributes no fabricated or nil proof ID. Ordered changed operations
and the existing `:direct-no-use-dead-rule` bounded replay record the local
evidence. Preserve runtime-proof invalidation, source/original-input identity,
fresh pipeline reconstruction and result-substitution rejection. Preparation
performs its operation traversals and original-operand kind checks without
operation/operand-position quotas. Independent pipeline/verifier replay
is additional; these are not two walks, a linear-CPU bound or a performance
claim.

For normalized C13 branch simplification, run
`sh16-simplifies-only-established-boolean-conditions` in the same namespace.
Only actual `true` and `false` conditions select their respective destination.
On the ordinary branch-validation path, Nil and other non-Bools reject with
`C13-CONTRACT` before transformation;
earlier source, nondeterminism, duplicate-operation and effect gates retain
priority. The private simplification predicate also refuses these values.
Keep source/origin, invalidation-ledger and recomputing-verifier controls.
This is the fixed normalized Bool interface, not a change to source-level
truthiness or a general rewrite of membership helpers.

For C13 transformation audit preservation, run
`sh16-preserves-unsafe-audit-identities-through-transformations` in the same
namespace. Constant folding and Bool branch simplification conditionally retain
the existing `:unsafe-audit-id` only for `:unsafe-island` operations. Safe output
maps remain unchanged, without a nil audit field. Input and post-pass schemas
are not loosened. Preserve exact audit IDs, source/origin, safety outcomes,
decision and invalidation records, plus fresh result recomputation controls.
The shared retained-runtime-check callsite does not admit audited unsafe checks:
its original public gate still requires `:runtime-checked`. These supplied
normalized IDs are not audit-provider evidence, authenticated C11/source
construction, native execution or renewed proof.

For the C13 constant-fold/DCE composition, run
`sh16-defers-proven-unused-adds-until-dead-code-elimination` in the same
namespace. An admitted pure, proven-unused ADD record remains unchanged,
including its original dead proof and identity, through constant folding and
branch simplification; only scheduled DCE removes it. Do not copy a dead proof
across a rewrite. Live ADD folding and original type, overflow, effect, proof
and admission checks remain unchanged, as do final/post-DCE dead-operation
rejections. This does not select dead-branch deletion or a new CFG proof rule.
Include the existing sequential/stale-proof, dead-result, no-op, audit, source,
schema and recomputing-verifier controls. These supplied normalized proof
records are not proof-provider evidence, fresh C11 input or native execution.

For C13 proof-usage records, run
`sh16-records-only-used-dce-and-runtime-proof-occurrences` in the same namespace.
DCE decisions name the validated dead proofs of actually selected operations;
runtime-check elision retains its existing selected proof IDs. Aggregate
translation-validation usage concatenates the freshly recomputed decisions in
pass/operation order, preserving repeated occurrences. No-change decisions
remain empty. Keep proof usage separate from invalidation: existing runtime
proof invalidation is unchanged, and DCE usage does not add dead IDs to it.
Test missing/corrupt proof rejection and decision/aggregate substitution through
the original recomputing verifier. This reports supplied normalized proof DATA,
not provider verification, fresh C11 input, native execution or admission.

For C13 RETURN control preservation, run
`sh16-unused-result-proofs-do-not-authorize-return-removal` in the same
namespace. The normalized no-use/effect-free proof has no control-flow
unreachability premise and cannot authorize deleting a RETURN; reject it with
`C13-PROOF`. Preserve live returns, pure const/ADD/compute DCE, earlier proof,
effect, safety and source diagnostics, and the existing runtime-check
`C13-SAFETY` fence. Branch/branch-if policy is not changed by this repair.
Use original optimization/recomputation controls over supplied normalized DATA;
this adds no CFG proof, provider evidence, fresh C11 input or native execution.

For the corresponding unconditional-branch boundary, run
`sh16-unused-result-proofs-do-not-authorize-unconditional-branch-removal`.
A `:branch` produced by the original Bool simplifier still transfers control;
an unused-result proof cannot authorize its deletion and rejects with
`C13-PROOF`. Preserve live branches at both destinations, ordinary Bool branch
simplification, pure-value DCE and earlier diagnostics. This extends the
control-preservation fence, not branch-if policy or CFG/unreachability inference;
the same supplied normalized DATA and recomputation limits apply.

For C13 rejected-operation attribution, run
`sh16-rejections-retain-the-first-failing-operation-source` in the same namespace.
The original ordered checks retain the first failing operation and reason.
Use that operation's source span and generated-origin chain only when BOTH
validate; otherwise retain the complete legacy request diagnostic without
skipping to a later operation. Aggregate checks keep priority and request-level
locations. Accepted output, rule/reason selection, schemas and bounds stay
unchanged. Recomputing verification reconstructs the rejection and rejects
substituted diagnostic locations. These are supplied normalized diagnostic
records, not fresh source construction, native execution or migration evidence.

### Closed actual-MIR-shaped comparison folding

For the development C13-to-C14 translation, run:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh17-c13-optimized-mir-carrier-test \
  --exact sh17-development-folds-one-actual-mir-shaped-integer-equality --fail-fast
```

This consumes supplied actual-MIR-shaped DATA, not a fresh authenticated C11
artifact. Select exactly one entry `:integer-eq` whose ordered operands join
earlier, independently typed, pure/proven-safe bounded-i64 literal producers.
Preserve repeated reference occurrences, original result/source/fact identities,
CFG and all unrelated operations. Unsupported cases retain the identity route.
The selected Bool constant retains its original `:integer-eq` source operation;
only the selected LLVM surface admits this derived constant, not shared C/Wasm
literal guards. Regenerate its value payload and remove exactly its two operand
DFG edges and producer-use occurrences, recording existing use-def invalidation.

A fixed four-row reversible delta retains original occurrence positions, not a
second MIR or source/fact table. Its measured carrier is 127 nodes including map
entries; the independent pure stage-owned reconstruction component is 183
physical lines with one load-order entry. Host reconstruction must round-trip
the complete original input and independently rederive the output. C14 also
restores and rederives the selected translation. Original C11 reports/pass and
preflight records describe ONLY the original input; never rewrite their counts
to validate output. The separate output hash excludes its own C13 record,
evidence and seal. Remove operation-count and capacity-only carrier quotas;
scans, copying, reconstruction and hashing still execute, not a speed
or free-sharing claim.

Retain direct identity/fallback, original-report, malformed-carrier, source/fact,
DFG/delta and result-substitution controls, plus the normalized SH16 suite,
direct SH17 hardening suite, boundary, load-order and LOC checks. Current public
C11 source pins and C13/C14 packet bindings are not renewed here. Fresh public
source/packet composition, native execution, milestone completion and migration
remain outside this development result.

For the existing B3 LLVM text-emission consumer, also run
`sh17-development-emits-c13-folded-bools-through-existing-b3-builder` in the
same namespace. Feed a complete supplied original MIR through current C13
folding and C14 restoration/rederivation, then pass that SAME output to the
original B3 builder. Its two constant-source gates now admit only the derived
`:constant`/`:integer-eq` case with exact Bool payload and zero operands, under
the existing envelope, purity and value-parity checks. Original comparison IR
and literal prefixes remain unchanged. Folded true/false emit `add i64 0, 1|0`
without comparison/zext instructions; no LLVM tool or native execution is run.
B3 retains its upstream verified-MIR premise: it does not authenticate or
reprove C13. Output stays `:constructed-unverified` with host content binding
pending; legacy C11 closure fields name input lineage/upstream obligations,
not fresh C11 verification of transformed output. Public packet/pin boundaries
remain unchanged and unrun. Run cheap checks serially through actual process
completion; these development checks establish no isolated timing benchmark.

For affected four-block composition coverage, run
`sh17-development-folded-predicate-preserves-integer-diamond-emission` in the
same namespace. The folded Bool drives the existing branch/Integer-phi path;
check true/false, repeated and signed-endpoint conditions, Integer arms within
the existing process-exit subset, exact two-edge removal and preserved CFG,
incoming values, source/type tables and terminator uses. Compare original and
folded emission and reject mutants at their actual C14/host/B3 owning checks.
This is test-only supplied-DATA coverage, not a new semantic repair, fresh
source/public packet, LLVM tool result or native execution.

For B1 LLVM status compatibility, run
`sh17-development-b1-llvm-accepts-matching-current-c14-optimization-statuses`
in the same namespace. The current pure `p15-s23-c14-expected-record` projection
produces supplied request DATA; B1 accepts only identity/fold-complete nested
statuses with exact outer agreement and unchanged optimization-report joins.
A test-only scoped observer delegates every call to the original interpreter:
valid statuses reach the original dependency check and retain its rejection of
unestablished current compiled lineage; invalid statuses/reports/targets reject
before that call. It never substitutes a successful result or an old-pin
positive. C/Wasm, source pins and public packet admission remain unchanged.
This is status compatibility, not fresh C11/output verification or public
source/backend readiness.

For pure host LLVM consumer parity, run
`sh17-development-host-llvm-consumes-c13-folded-bools` in the same namespace.
The original host rejection/render/evaluation helpers consume the current C13
fold through C14 checks and the SAME B3 output, in linear and Integer-diamond
cases. The host source gate admits only derived `:integer-eq` constants with
Bool type, an actual empty Vector, exact `:present?`/`:value` payload keys,
true presence and a Boolean value. Legacy literal/implicit-Nil branches and
earlier effects/capabilities/opcode diagnostics remain unchanged. Include the
existing B3 and diamond selectors for affected composition. This is pure
helper parity, not evidence that a public builder failed or that current
public source, preflight, pin or native admission has been renewed.

For external cross-target LLVM IR validation, set
`GRAVITY_SH18_CC=/usr/bin/clang` and run
`sh17-development-folded-bool-llvm-ir-passes-cross-target-clang` in the same
namespace. Four supplied-DATA/current C13-to-C14-to-B3 outputs (linear and
Integer-diamond true/false) go unchanged to Clang with
`-target x86_64-unknown-linux-gnu -x ir -Werror=override-module -S`.
A separate undefined-SSA mutation must complete with nonzero rejection. The
existing SH18 harness discovers the explicit Clang and runs all five module
calls serially: two identity probes allow five seconds and 1 MiB per stream;
each module call allows ten seconds and 8 KiB per stream, with existing
three-second process cleanup. Check IR before writing and assembly before
reading/hashing against 64 KiB; the output check is not a filesystem quota.
Retain exact argv, IR/assembly hashes, version, host target and requested target
as nonauthoritative observations. The invoked `/usr/bin/clang` SHA identifies
the Apple launcher only, not its effective Xcode engine or full toolchain
closure. Host arm64 Darwin and requested x86_64 Linux are distinct. Do not
rewrite emitted triples, link or execute generated programs, claim hard
aggregate wall/memory containment, or infer public packet/native admission,
fresh C11 output verification, migration or benchmark evidence from this check.

### Native execution failure records

The existing SH18 harness accepts execution only when its original process
record is `:completed` with exit zero. Timeout remains distinct; output-limit
and other noncompleted records reject without calling `zero?` on a missing
exit code. Preserve the complete process, executable hash, path and arguments.

Run `sh18-native-execution-preserves-output-limit-rejection` and
`sh18-native-execution-status-records-remain-distinct` in
`gravity.self-hosting.sh18-native-toolchain-harness-test`. The first uses the
original bounded compiler/executor; the second is explicitly synthetic status
mapping, not native execution. Keep existing process/output/cleanup limits and
coordinate the native work through the existing resource policy. An unrelated
normal-class development JVM is not itself a reason to require global JVM
quiescence. No new runner, lane or authoritative migration is required.

### Normalized target-lowering type consistency

For C14's declared return-type consistency boundary, edit
`c14_target_lowering_architecture.gravity` and run
`sh17-return-types-match-the-defined-returned-value` in
`gravity.self-hosting.sh17-target-lowering-hardening-test`. The terminator's
selected value must have the function's declared return type. Preserve prior
duplicate-definition and operand/return-defined checks. Include existing
three-target, normalized-schema, target-substitution and result-substitution
selectors when changing this boundary. These supplied normalized requests do
not establish fresh upstream verification or native backend execution.

For the existing normalized integer opcodes, also run
`sh17-integer-operations-check-their-declared-signatures`: `:integer-add`
requires two declared Integers and an Integer result; `:integer-eq` requires
two declared Integers and a Bool result. Check signatures after existing
duplicate-definition and operand-defined gates. This does not select integer
widths, overflow behavior, coercion or constant-value typing.

For the distinct internal C14 scalar-MIR eligibility gates, run
`sh17-local-forwarding-preserves-the-last-operand-scalar-type` in the same
namespace. `:local` and `:local-binding` must declare the canonical scalar type
of the actual LAST referenced operand. Existing nonempty arity is retained:
discarded earlier operands do not impose a unary rule or determine the result
type. Also run
`sh17-sequence-and-scope-preserve-the-last-operand-scalar-type` for the same
LAST-type join on `:sequence` and `:lexical-scope`, following the ordered
`do`/`let` body result. Preserve nonempty multioperand arity and earlier
artifact, verifier, source, effect and reference gates; leave function-boundary
handling unchanged.
The three existing C/LLVM/Wasm whole-surface gates and original evaluator are
checked with supplied MIR DATA, including Integer, Bool and Nil forwarding and
incompatible declarations. Adjacent public normalized SH-17 tests are separate;
this is not a fresh authenticated C11/C13 packet, native execution or migration.

### C11 node-local preflight diagnostics

For source-valid checked-Core nodes rejected by the existing single-diamond
MIR preflight, edit `c11_mir_specification.gravity` and run the owning regression:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh12-branch-join-mir-test \
  --exact sh12-node-local-preflight-diagnostics-preserve-source --fail-fast
```

Construction and verification must report the first offending node's operation,
span and generated origins, not the enclosing conditional. No verified CFG
exists on this rejection path, so its block is `:not-applicable`. Keep failures
with malformed or missing source metadata, and aggregate failures, on their
existing fallback. Accepted
MIR, public report fields, rejection precedence and resource limits are unchanged.
Use the existing diamond, malformed checked-Core and fact-loss selectors for
affected regression feedback. Supplied checked-Core tests exercise this ingress;
they do not establish fresh C10 composition, nested CFG support or migration.

For operation type or type-fact mismatches after those preflight gates, use
`sh12-operation-type-diagnostics-preserve-expected-source` in the same namespace.
The diagnostic must identify the first mismatch in canonical operation order
using freshly rebuilt expected MIR, never candidate-supplied source metadata.
Type-table, function-return and value-only failures retain their aggregate
fallback; earlier structural failures retain their original precedence. This
failure-only diagnostic path does not widen accepted MIR or its public schema.

For the corresponding effect or safety operation-fact failures, use
`sh12-operation-effect-safety-diagnostics-preserve-expected-source`. Preserve the
original aggregate checks before locating an operation, and the existing
type-before-effect-before-safety rejection order. Terminator/value-only errors
retain their fallback. Attribution must use only the original fact views and
fresh expected MIR; this check does not establish new safety proofs.

### Finite persistent List value equality

STD3 owns ordered List equality. The current JVM typed route supports binary
`=` for complete finite positional Lists, including empty and nested Lists,
with Nil, Bool, Character, String, Symbol and Keyword leaves. It compares stored
values at runtime, not type identity. Bare/homogeneous/unknown Lists, numeric
and callable leaves, other collection families, nil-or unions, nested Never
and corresponding nested-List/scalar pairs remain unsupported. Top-level Never
retains the existing non-invoked route; supported normal domains are still
validated completely. This subset does not restrict the eventual language law.

Owners are `c7_type_checker_engine.gravity` (`sh08-le-*`), `effects.gravity`
(`body-le-*`), `emitter.gravity` (`p15-s23-le-*` and typed lowering), and
`runtime.gravity` (`p15-s23-runtime-list-equality-work`). Start with these exact
selectors through the direct `-M:dev-test --namespace ... --exact ... --fail-fast`
interface, using `-J-Xmx2g`:

- `sh08-list-equality-development-kernel` in `gravity.self-hosting.sh08-function-call-type-test`.
- `current-gravity-list-equality-consumer-development-kernel` in `gravity.self-hosting.sh09-body-effect-test`.
- `current-gravity-list-equality-lowering-development-kernel` and `current-gravity-list-equality-runtime-development-kernel` in `gravity.self-hosting.sh01-stage2-runtime-iteration-test`.

Include the owning ID, transfer and old equality-family controls when
those paths change. Both complete domains and all corresponding pairs must
be checked before returning even false. Existing atomic, Vector and Set helpers
keep their independently justified semantic domains; remove work quotas.
The current runtime inventory is 56 functions; historical 55-function evidence
is not rewritten or treated as current.

For affected source composition, the same SH01 namespace contains
`current-gravity-list-equality-authenticated-composition`: one accepted Gravity
source and one typed Map-shaped-Character rejection, with QST parser parity
separate. Despite its historical naming convention, this is development
integration using fresh C5 and development C6-session/replay products, not fresh
public C6 verification or authoritative migration. Do not repeat it for unrelated
document or downstream-only edits. Private Character carrier tests retain their
existing representation premise; they are not a new Unicode validator.

### Finite persistent List value inequality

STD2's binary complement law extends to the supported STD3 List equality
domain above. `not=` preserves the same accepted/rejected frontier, ordinary
left-to-right operand evaluation and non-tail transfer rules. C7, C8 and typed
lowering use their existing ANE owners; each selected List-domain classification
uses its existing List equality validator. There is
no runtime change: lowering emits an owned conditional around one builtin `=`,
with false/true branches. It does not emit a runtime builtin `not=`.

Start with these direct focused selectors, using `-J-Xmx2g -M:dev-test` and
the namespace/`--exact`/`--fail-fast` interface:

- `sh08-list-inequality-development-kernel` in `gravity.self-hosting.sh08-function-call-type-test`.
- `current-gravity-list-inequality-consumer-development-kernel` in `gravity.self-hosting.sh09-body-effect-test`.
- `current-gravity-list-inequality-lowering-runtime-development-kernel` and `current-gravity-list-inequality-transfer-fence-development-kernel` in `gravity.self-hosting.sh01-stage2-runtime-iteration-test`.

Include the domain, ID, throw-consumer and old inequality-family selectors
when their owning paths change. C8's ANE context prepares type checks and
indexes; `body-ane-call-valid?` consumes the guarded source joins afterward.
Test rejection at that actual consumer rather than adding a repeated source
walk to preparation. Remove child-work accounting and work-unit defaults.
Callback observations apply to the selected
malformed fixtures, not arbitrary hostile DATA or provider CPU bounds.

For affected source composition, select
`current-gravity-list-inequality-authenticated-composition` in the SH01 namespace.
It exercises one fresh Gravity positive and one numeric-List typing negative;
QST parity is parser-only. Fresh C5 plus development C6-session/replay, C7, C8,
assembly and one closed entry provide development feedback, not fresh public
C6 verification or migration. Do not repeat this composition for unrelated
documentation or disjoint downstream changes without demonstrated invalidation.

### List family classification

STD3 owns unary `list?`. The current JVM typed development route accepts independently
established bare, positional and homogeneous List types and the existing closed
value-type families. It returns Bool for normal operands and preserves Never
transfer. Unknown, dynamic, callable and malformed type carriers remain outside
this route; the complete existing List-construction type validator still runs.
Classification supplies no member, length, nonempty or branch-narrowing facts.

The owners are the C7 type engine, `effects.gravity`, the P15 typed emitter and
the existing closed-plan dispatch in `src/gravity/runtime.gravity`. Start with:

```bash
clojure -J-Xmx2g -M:dev-test --fail-fast \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-predicate-development-kernel \
  --exact current-gravity-list-predicate-source-development-kernel \
  --exact current-gravity-list-predicate-lowering-runtime-development-kernel \
  --exact current-gravity-list-predicate-closed-runtime-development-kernel
```

Use the companion ID-guard and consumer selectors for source-join changes, and
the existing Set/Vector predicate and List construction/query controls for
affected regressions. Lowering emits the original unary builtin-call shape;
the operand runs once, non-tail, with effects and transfers preserved. Empty
and nonempty persistent Lists classify true; Cons and sequence-view compiler
DATA do not become List values merely because a trusted carrier admits them.
The runtime extension changes existing dispatch only, not its helper/export
count. Paired fixtures live under SH-08 as `local-runtime-list-predicate` and
`list-predicate-arity-two`. Parser parity and normalized tests do not establish
fresh source composition, authoritative migration or whole-runtime support.

For a stable change to this composition, run its affected source selector once:

```bash
clojure -J-Xmx2g -M:dev-test --fail-fast \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-predicate-authenticated-composition
```

It builds one accepted Gravity program and one arity typing negative through
fresh development sessions, C7 and the affected downstream path. Q remains
parser-only; public fresh C6 admission and a same-IR seed control are separate.
Keep failed attempts and reuse unaffected focused results after a narrow fix.
The named timing intervals are inclusive development observations, not isolated
benchmarks; the post-assembly interval also includes projection and runtime-plan
acquisition before execution.

### First stored value of a proven-nonempty Vector

STD3 defines `first` on a persistent nonempty Vector as its stored index-zero
value, including false or Nil. The current typed subset requires a complete,
finite positional Vector type with at least one element. It validates the
entire type, not just its head. Empty, bare and unknown-length Vector types
remain unsupported by this lowering; this is not a language-level decision
about the result of `first` on an empty Vector. No flow/count solver is added.

The owners are `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`,
`bootstrap/gravity/src/gravity/effects.gravity`,
`bootstrap/gravity/p15_s23/emitter.gravity` and
`bootstrap/gravity/src/gravity/runtime.gravity`. C7 establishes the result and
nonempty fact; C8 and lowering reconstruct its provenance independently.
Runtime evaluates the operand once, retains its guard and returns the stored
head without evaluating a stored payload. Existing List behavior is separate.
Run these focused checks for changes to that behavior:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-first-development-kernel \
  --exact sh08-vector-first-throw-consumer-development-kernel \
  --exact body-vector-first-development-kernel \
  --exact current-gravity-vector-first-lowering-development-kernel \
  --exact current-gravity-vector-first-runtime-development-kernel --fail-fast
```

When the typed/effect/lowering interface changes, use the affected source
composition `current-gravity-vector-first-authenticated-composition` in the
same SH01 namespace. Its companion
`current-gravity-vector-first-fixtures-development-kernel` checks the G/Q
fixtures cheaply; Q has parser coverage only. The source composition is a
development integration check, not current-closure admission. Do not replay
unaffected source compositions or require fresh migration proofs before an
ordinary edit. Stable authoritative admission remains a separate decision.

### Recognition of an already declared seed primitive

When ordinary C5 reports an unresolved primitive that the selected seed
already declares and implements, check the source request's declaration
catalog in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00870-sh06-resolution-special-symbols.clj`.
Repair a demonstrated declaration omission there; binding selection remains
owned by the Gravity resolver described below. Do not use ambient host lookup,
bulk-whitelist names or interpret recognition as typed/runtime/target support.

For the `list?` catalog repair, use the direct focused source check:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh06-list-predicate-binding-test --exact sh06-list-predicate-binding-authenticated-source --fail-fast
```

It constructs three tiny source attempts and invokes the original fresh C5
verifier: core and qualified binding/provenance, lexical shadowing and inert
quoted data, with unknown-lookalike and unimported host-namespace rejection.
It does not construct the whole runtime module or prove a typed List predicate.
Catalog records have sorted ordinals, so an insertion can also change later
core binding identities and affected C5 requests. Reassess affected evidence;
unchanged target bytes alone do not establish validity. Do not renew a corpus
or migration stamp merely to validate this development correction.

### Qualified core resolution and availability

Edit `bootstrap/gravity/src/gravity/resolution.gravity` for binding selection
and its C5 diagnostics, not the core catalog or a host namespace resolver.
Use the existing direct owning checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-qualified-core-var-development-kernel \
  --exact sh06-atomic-signature-type-namespace-development-kernel \
  --exact sh06-ordinary-module-policy-development-kernel
```

These execute the current owning Gravity resolver with normalized requests.
They check route ownership, selected identity, privacy/ambiguity precedence,
core profile/target eligibility and diagnostic facts. They are not fresh
source composition or authoritative migration. Preserve existing failure
facts unless the new availability check itself rejects; a private or duplicate
candidate must not disappear through eligibility filtering.

For the emitter's declaration/source-spelling join, use the existing
`gravity.self-hosting.sh01-stage2-runtime-iteration-test` namespace with exact
selector `current-gravity-qualified-type-declaration-development-kernel`.
It checks ordinary and qualified List/String declarations and rejects forged
spellings, bindings and source identities without rebuilding source products.

When resolution or that lowering join changes, run the affected composition
separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-qualified-core-authenticated-composition
```

This checks paired original sources through C5/C6/C7, public lowering, C8 and
execution, with missing-member rejection at C5. It is not an inner-loop
prerequisite, a reason to replay unchanged proof products, or authoritative
module migration. Preserve failed attempts and distinguish their completed
checks from assertions that never ran.

### Map source-order projection

The temporary SH-07 request builder must use retained C2 entry occurrences,
not host-map iteration, for evaluated map children. Its local correction keeps
existing form IDs, bindings, reference paths and decimal coordinates; only the
map parent's ordered child links change. Quoted map DATA retains its existing
materialized value and is not subjected to evaluated-map restrictions.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-map-source-order-development-kernel \
  --exact sh07-map-quoted-duplicate-source-development-kernel
```

These normalized checks cover correspondence and preservation, not actual
source composition or map execution. The affected composition must separately
check original textual order through C2, the request and Core IR, and reject a
reordered request against unchanged ancestry. No new migration proof or
host-projection accounting protocol is implied by this correction.

Run that affected composition separately, not in the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-map-source-order-authenticated-composition
```

The paired fixture checks opposite nine-key source orders, quoted duplicate-key
DATA compatibility and a coherently rehashed request-order rejection. Its
accepted result establishes Core IR order, not evaluated-map runtime support.

### Character occurrence transport checks

L1/C2 own Character decoding, including supplementary Unicode scalars. The
temporary C3/SH04 transport must retain the original authenticated occurrence's
kind and decoded value. A supplementary Character's existing four-field
representation is not a license to classify an ordinary source map as Character.
Keep the global host-value classifier unchanged.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-character-c3-occurrence-development-kernel \
  --exact sh07-character-source-occurrence-development-kernel \
  --exact sh07-map-source-order-development-kernel \
  --exact sh07-map-quoted-duplicate-source-development-kernel
```

These normalized kernels check occurrence/value joins, scalar representation,
ordinary-map distinction and malformed or retargeted records. C6 derives kind
before recursion from the original source occurrence, including quoted and
nested values; the map regressions preserve source order and quoted duplicate
materialization. The supplied integrity report is conditional test input, not
proof that C2 authenticated a
source. Actual source composition must separately preserve the same distinction
through C6 and the affected consumers, including quoted and nested occurrences.
Do not clone the Unicode parser, add a caller authority flag, or refresh
migration fingerprints to run this transport check.

### Ordinary module dependencies

L3/C5 module resolution consumes real callee artifacts. Namespace names,
`:refer` entries and `:compiler`/`:sh06` metadata do not establish that a
definition exists or is exported. Change the owning policy in
`bootstrap/gravity/src/gravity/resolution.gravity`; the existing SH-06 adapter
transports and verifies artifacts rather than selecting imports itself.

Use the ordinary constructor's module-input argument, not a new wrapper or
authority flag:

```clojure
(require '[gravity.bootstrap :as bootstrap])

(bootstrap/sh06-resolution-source-artifact caller-path caller-text
  {:artifact :gravity/sh06-module-inputs
   :schema-version 1
   :module-artifacts [callee-artifact]})
```

Build `callee-artifact` with the same ordinary constructor or file constructor.
The two-argument source constructor means an empty module-input set; every
external Gravity `:requires`, including an unused requirement, then needs an
explicit callee artifact. Missing inputs fail with `C5-UNRESOLVED`. There is no
ambient host-namespace lookup or export-all fallback. The existing strict
SOURCE-context interface is separate and is not a development shortcut.

Start with the existing direct policy checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-ordinary-module-policy-development-kernel \
  --exact sh06-ordinary-seed-build-does-not-call-public-frontdoors
```

Run the affected real module composition when import behavior or its artifact
interface changes. Local artifact verification checks the supplied semantic
inputs; it does not admit a module revision, refresh historical migration
proofs, or establish imported-call typing, execution or self-hosting support.

### Loop/recur type development

The existing owning pass is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
Its internal `sh08-ft-loop-type-step` consumes canonical C6 nodes, bindings,
targets and transfers plus current type/binding tables. Its semantic DATA
contains bindings, constraints and issues; remove visit fuel and work counts.
`sh08-ft-normal-return-type` joins ordered child types with a
nominal type. C7 retains input authentication, inference orchestration and
diagnostic rendering; calling an internal kernel does not grant acceptance.
The optional `gravity.types` extraction is deferred, not a prerequisite for
this semantic increment. There is one implementation, in the actual C7 source.

Run those current-source kernels directly for fast feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact normal-return-bottom-development-kernel \
  --exact loop-working-core-retains-authenticated-bindings-development-kernel \
  --exact loop-slot-transfer-development-kernel \
  --exact zero-slot-nested-nearest-development-kernel \
  --exact function-target-reset-is-not-forgery-development-kernel \
  --exact loop-effect-backedge-development-kernel
```

These tests use the existing single-source compiler interface on the exact C7
source, not a stripped source or a raw root projected from a module bundle.
The kernels use normalized records, not authoritative source evidence.
The production adapter joins the separately authenticated bindings into a
private loop-analysis view; it does not add fields to published canonical core.
`Never` means no normal return; unknown evidence is not `Never`. Loop slots
derive from sequential initializers, and recur arguments must match their
individual target slots without coercion. A never-returning argument does not
produce a mismatched transfer value. Ordinary `if` retains L2 truthiness, and
function construction does not execute its body. Function-target recur uses
the separately declared parameter slots described below; unresolved slots are
an annotation requirement, not malformed C6 by themselves.

`gravity.effects` owns the corresponding C8 body-effect equations. A recur
backedge returns to the target's body, not its initializers. Its focused check
joins the retained C7 slot/transfer premises to C6 control records, starts effect
summaries at bottom and checks the final equations without a logical work
budget. This is development feedback, not a termination or runtime-cost
proof.

After changing C7 inference or its namespace boundary, validate the affected
real C5/C6/C7/C8 composition and execute the same source bytes through the
existing seed interface:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact loop-recur-authenticated-paired-composition
```

The separate `loop-c6-controls-precede-types-current-source` selector exercises
the original source-level C6 rejection boundary. It is deliberately not named
as a development kernel: it constructs upstream products and is slower than
the normalized leaf checks. Run it when that composition boundary is affected,
not on every leaf edit. Leaf kernels alone do not establish real composition,
program termination, effect legality, migration or self-hosting completion.

### Function-target recur development

Use the same C7, `gravity.effects`, and loop-test owners. C7 takes each function
recur slot from the original parameter declaration, not from a caller's inferred
argument types. It checks original target, binding order, nearest-function reset
and transfer shape before deciding whether a type annotation is missing.
Supported recur transfers have type `Never`; the enclosing normal-return join
still determines the function result. C8 joins the original function signature,
parameter facts and body identity, then solves body backedges with the existing
bounded fixed-point equations. A function-only recur must not enter the ordinary
acyclic solver merely because the module has no `loop` node.

Use these exact selectors for local feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact function-recur-node-inference-development-kernel \
  --exact function-recur-slots-development-kernel \
  --exact function-recur-backedge-development-kernel \
  --exact function-target-reset-is-not-forgery-development-kernel
```

The backedge check calls the same private solver-dispatch helper used by module
analysis. It covers function-only recur, malformed control tables, and parity
with ordinary acyclic, loop and named-self-call paths. This is not a substitute
for the complete module joins. When that composition changes, run
`function-recur-authenticated-paired-composition` in the same namespace. It
checks actual accepted/rejected annotated `.gravity` and `.qst` source products,
fresh C7/C8 verifiers and tampered artifacts. Its result must be recorded, not
inferred from the kernel checks.

The compiler composition does not itself establish runtime execution. The
separate exact-source seed check below supplies that evidence for the bounded
fixture. General recursion, termination, target legality and authoritative
migration remain separate claims.

### Annotated single-source seed execution

The existing three-argument compiler-artifact plan interface lowers fixed-arity
atomic parameter and return annotations without changing the source text.
Definition records retain `:source-declarations` alongside logical parameter
names and executable body forms. Raw `def`/`fn` return prefixes and the existing
macro-produced `typed/return` header are retained in their actual forms; the
latter is not relabeled as literal unexpanded source. Full source hashes and
function records remain part of plan and semantic identity.

The owning seed code is `foundation/00546` (header normalization and validation),
`bootstrap-p15/00580` (existing source records and raw plan construction), and
`bootstrap-p15/00584` (direct-emitter validation and declaration comparison).
The shared raw definition parser remains unchanged. Original headers are
validated before body compilation, including invalid nil/false return tokens
that legacy macro expansion can erase. Plain records and plans retain their
previous shape; shared function-table callers are not assumed to be isolated.

Run the existing focused header and compatibility checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact seed-function-declaration-plan-parity-development-kernel \
  --exact seed-function-declaration-macro-route-development-kernel \
  --exact seed-shared-function-table-plain-parity-development-kernel \
  --exact seed-annotated-function-loop-isolation-development-kernel
```

For execution of the exact annotated function-recur fixture:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact function-recur-annotated-seed-execution
```

This seed lowering validates declaration grammar, not type legality. C5/C6/C7/C8
remain the semantic owners; a symbolic type token is not proof that its type
exists or that its use is legal. Composite annotations, variadics, duplicate
logical names, missing/repeated markers and empty bodies reject in this subset.
The four-argument module constructor retains its unannotated-definition policy;
the direct compiled-emitter path retains its hosted-profile requirement. No
annotation stripping, unchecked imported execution or new authority follows.

### Direct typed-throw development

The bounded source path checks a direct `throw` payload after local type
inference. A known payload type is retained as a thrown-error fact; the transfer
expression has type `Never`, so it does not become a normal branch result.
Unknown payloads require an annotation. The owning files are
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` and
`bootstrap/gravity/src/gravity/effects.gravity`; paired examples live under
`bootstrap/clojure/fixtures/self-hosting/sh-08/` as `direct-typed-throw` and the
three `direct-throw-*` rejected cases.

Use the existing direct kernels for edit feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact direct-typed-throw-source-and-type-development-kernel \
  --exact direct-typed-throw-effect-replay-development-kernel \
  --exact direct-typed-throw-nested-and-allowance-development-kernel
```

For an affected source/type/effect interface change, the existing
`direct-typed-throw-authenticated-paired-composition` selector constructs fresh
C5/C6/C7 products and checks the current C8 effect leaf over both extensions.
It checks payload/result typing, source effect allowances and tampered products;
it does not reconstruct the public C8 compiler bundle. Do not rerun this
unchanged composition merely for a later seed-execution observation or commit.

C8 independently joins the original payload-before-transfer record and checks
inferred effects against original source allowances. Missing `:error/throw`
declarations report `C8-UNDECLARED`; this is not the full package/provider/grant
or profile-lowering legality intersection. The normalized legacy C8 registry's
`:error/raise` label is not silently renamed or aliased.

Nested function ownership is separate, quoted DATA is inert, and an operand
that never returns preserves the outer source record without inventing a
`Never`-typed error. Unrelated calls keep their existing behavior. Direct local
call propagation has its own bounded increment below. Imported concrete throws
remain fenced by C8's existing foreign-summary check, not a new C7 import
guarantee. `try`,
handlers, cleanup, host-exception normalization, general runtime execution,
error legality and authoritative migration remain separate work.

### Direct-local thrown-error propagation

Use the same C7, `gravity.effects`, fixture directory and loop-test namespace.
The bounded acyclic component analysis joins original local call edges,
bindings, arguments and callee bodies. Normal return types and possible thrown
types are separate: a String-returning function can also throw String. A pure
leaf gets an empty thrown-type vector only after its supported body is checked;
pending or unknown facts are not treated as empty.

Start with these exact development checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact local-throw-call-closure-development-kernel \
  --exact local-throw-call-effect-equations-development-kernel \
  --exact local-throw-call-existing-type-guards-development-kernel \
  --exact local-throw-final-signature-status-development-kernel \
  --exact local-throw-source-body-index-parity-development-kernel
```

When the call/type/effect composition changes, use
`local-throw-propagation-authenticated-paired-composition` in that namespace.
It checks fresh C5/C6/C7 products, independent C8 body equations, altered
summaries and real rejection fixtures. It also executes the same accepted
source through the existing seed interface, without a second compiler proof.

Error vectors conservatively include source operand error potential in order;
this is not path-sensitive reachability analysis. A `Never` operator or argument
prevents normal invocation, so the callee body's errors are excluded while the
original static call edge and source records remain present. Disconnected
direct-throw owners retain their prior analysis. Throwing recursion, recur in
the affected component, imported or indirect throwing calls, handlers and
cleanup remain unsupported here. These checks do not admit a module revision
or establish Gravity-emitter/runtime ownership, complete legality or self-hosting.

### Atomic typed catch development

The owning compiler files are the existing C7 engine and `gravity.effects`.
Canonical C6 handler records supply the source/type/binder/scope/clause-order
boundary; C7 consumes authenticated resolved core rather than reconstructing C5
resolution through a new API. C8 independently checks its full request and the
typed handler premises. Keep these joins separate from runtime catch execution.

Start with the direct kernels in the existing loop/type test namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact atomic-typed-handler-partition-development-kernel \
  --exact atomic-typed-handler-source-development-kernel \
  --exact atomic-typed-handler-products-development-kernel \
  --exact atomic-typed-handler-body-development-kernel \
  --exact atomic-typed-handler-downstream-boundary-development-kernel \
  --exact atomic-typed-handler-diagnostic-provenance-development-kernel
```

`atomic-typed-handler-source-scope-integration` calls the ordinary public C5
constructor on real rejected fixtures. It is not an inner-loop kernel. Its
initial completed run spent about 169 seconds in that test, compared with about
one second across the five direct handler/downstream checks and seven seconds
of namespace loading. This observation identifies upstream product construction
separately from scheduling overhead; it is not a general C5 benchmark or a
speedup claim. The selector was renamed after that run without replaying its
unchanged body.

Handler candidate-transfer lists are source-potential censuses, not escaping
error summaries. Nested residuals must be derived bottom-up. Global handler
ordinals follow construction order; per-try clause ordinals follow source order.
A handler's own throw escapes that try rather than entering a sibling handler.

Covered and residual source error types do not by themselves establish legal
effect discharge. Preserve performed `:error/throw`, source allowances and
pending profile/provider permission. Actual C7/effects composition is required
when these typed facts or interfaces change; it is not a reason to replay old
unchanged public bundles or refresh admission pins.

For a changed handler boundary, run its affected composition explicitly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact atomic-typed-catch-authenticated-paired-composition --fail-fast
```

The initial completed composition passed one test/220 assertions in about
829 seconds of test execution, plus about eight seconds of startup/loading.
It constructs fresh accepted `.gravity` and `.qst` products, checks coherent
typed/effect mutations and semantic rejection cases. This cost belongs to the
affected product construction and checking, not a requirement before every
direct-kernel edit. A rejected-diagnostic source-location correction or removal
of optional test output does not require replaying the unchanged positive proof.

### Explicit typed input to current Gravity lowering

The existing `p15-s23-stage2-compile-function` and
`p15-s23-stage2-assemble-plan-products` interfaces have an ordinary four-argument
form: emitter, module, definition(s), and `{:request request :typed typed
:verification report}`. These are actual pass products, not an accepted flag.
The temporary Clojure orchestration component invokes current Gravity C7
verification once per operation. The Gravity emitter requires an accepted typed
result, the exact fresh expected/candidate result and matching supplied report,
then joins the original source snapshot, module, declarations, function/body
identities, parameter bindings and types.

The original source comes from the authenticated request's retained C2 tokens;
ordinary lowering does not require the source file to remain on disk. The
current C7 and emitter implementation sources are still read and compiled.
Raw internal kernel arguments are DATA, not cryptographic authority tokens.
Current-source products expose their actual compiler/source provenance and
pending effect legality. They do not enter the old pinned `00582` envelope.
Existing three-argument calls retain their old behavior and bindings.

Edit `bootstrap/gravity/p15_s23/emitter.gravity` for the typed canonical-core
lowerer. Its initial supported body subset is literals with an implemented
runtime representation, parameter references, `throw`, `if` and `do` in fixed
named top-level single-body functions. Unelaborated numeric descriptors and unsupported operations
reject; raw catch spelling is not type authority. The small `00581` Clojure
component only orchestrates existing compiler interfaces. It does not implement
type inference or a second source grammar.

Use the existing runtime-iteration test namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-lowering-input-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
# Affected source -> current C7 -> lowering -> Gravity evaluator composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-lowering-authenticated-composition --fail-fast
```

The first command uses normalized DATA to check joins, diagnostics and unchanged
legacy orchestration; it is not authenticated source evidence. The second is
the affected integration boundary. Build its actual source products once per
extension and reuse those immutable inputs across assertions. Neither command
admits a module revision or establishes profile/target legality, runtime catch
support or self-hosting.

The accepted focused batch passed four tests/283 assertions, including existing
emitter/runtime throw regressions. The test-only namespace/body correction then
passed the two typed-input selectors/74 assertions. The sole affected paired
composition passed one test/65 assertions in about 175 seconds of test execution
plus seven seconds of loading. These overlapping results are not additive and
do not compare equivalent workloads. SH05's `:expanded-forms` contains the body,
not the namespace declaration: parse the retained C2 original forms for the
module header, then associate the authenticated expanded body for function
analysis. Do not reconstruct upstream products merely to fix this test setup.

### Canonical atomic catch lowering and execution

The next increment uses the same emitter, runtime and test namespace. Catch
types come from canonical C7 bindings and solved handler premises, not source
spelling or host exception classes. The current subset is Bool, String, Symbol
and Keyword. Quoted DATA uses the solved C7 quote record; see the recursive
quoted-DATA boundary below. General calls are not catch-type evidence.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-catch-lowering-development-kernel \
  --exact current-gravity-typed-catch-runtime-development-kernel
# Run when the catch lowering/runtime composition changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-catch-authenticated-composition --fail-fast
```

The direct checks distinguish normal, thrown and rejected control, preserve
payload identity and evaluate the protected expression once. The first matching
handler runs; its own throw bypasses sibling handlers and may reach an outer
try. A malformed handler descriptor rejects before protected execution. Body
map validation is shallow, not a new whole-IR validity proof. Fatal host
failures are not guest error values.

The affected composition uses both existing atomic-catch fixture extensions
and the small local-runtime pair. It constructs each source product once and
checks fresh current C7 verification per public lowering operation. Runtime
execution at that checkpoint used an exact nine-function runtime projection
with original-source and dependency checks. The current selector now compiles
the whole actual `gravity.runtime` leaf; the historical 122-assertion result
below remains attributed to its original test revision, not a rerun of the
changed loader. Zero seed throw transport must be observed around every actual
execution, not inferred from an uninstrumented call. This boundary does not
admit a revision, refresh old pins, discharge effect/profile legality or prove
Error subtyping, resource cleanup, self-hosting or seed retirement.

The final direct batch passed two tests/128 assertions. The sole affected real
composition passed one test/122 assertions in about 491 seconds of execution
plus seven seconds of namespace loading. Its four upstream inputs were each
constructed once. These checks overlap; their counts are not additive and
their timings do not compare equivalent workloads. The instrumentation-only
correction preceded this run, rather than triggering a second proof build.

### Canonical symbol-let lowering

Use the same emitter and runtime-iteration test namespace for immutable symbol
bindings. The lowerer consumes the original C6 lexical records and C7 local
binding facts, including initializer identity, ordinal, scope, prior-visible
binding IDs and inferred type. Initializers see outer and preceding bindings,
not the binding currently being initialized or later bindings. Nested scopes
do not mutate the outer lowering context.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-let-lowering-development-kernel \
  --exact current-let-container-scope-projection-development-kernel
# Affected source -> typed lowering -> runtime composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-let-authenticated-composition --fail-fast
```

Repeated names in one source binding vector create successive lexical scopes
in the existing C5/C6 implementation. Preserve that behavior: nested one-binding
runtime lets avoid changing the flat instruction validator's duplicate-label
restriction. Runtime labels are not binding authority. An empty binding vector
still requires a body. A `Never` initializer prevents later initializers and
body execution; do not infer the let result from the last body type alone.
The binding-vector container itself has no assigned scope in the current
projection. Do not invent an equality between that nil value and the owner's
real scope. The projection helper check exercises this schema without building
an authenticated source product.

Run the affected real-source composition when this consumer changes, reusing
each constructed input across lowering and runtime assertions. Ordinary
self/forward-reference errors belong to C5. The unchanged C6 restrictions on
malformed bindings, missing bodies and unsupported destructuring are not a
reason to replay their historical expensive proof for each lowerer edit.
Other call forms, mutable locals, destructuring, numeric coercion, full legality
and authoritative migration remain separate work.

After correcting the unsupported container-scope premise, the focused batch
passed eight tests/493 assertions. The changed candidate's real composition
passed one test/132 assertions in about 366 seconds of execution plus seven
seconds of loading. It constructs each accepted input once per extension and
reuses it for nine-function assembly, lowering, runtime and mutation checks;
four C5 self/forward-reference rejections exercise the real owning boundary.
The first real composition remains failed (nine passed assertions, one error),
not relabeled by the later pass. Counts overlap and are not additive; timings
measure different workloads, not a speedup.

### Canonical recursive quoted-DATA lowering

Change `bootstrap/gravity/p15_s23/emitter.gravity` for this consumer. The
existing public typed operation first runs the current C7 verifier on the same
request and candidate, then joins that result to the lowering context. C7 owns
recursive quote shape, value, type, child order, source identity and work-bound
validation. Lowering selects the unique solved quote producer and checks its
scalar root, operator, payload, source and origin coordinates. It emits the
producer's exact value as existing `:quote` IR, without evaluating its contents.
It does not repeat the recursive checker or reinterpret quoted syntax.

Supported DATA is nil, Bool, represented exact integer, character, String,
Keyword, Symbol, list, vector, map and set, including empty and nested mixed
containers. Qualified symbols and symbol map keys remain inert; quoted
`(throw unresolved/data)` and `(quote nested)` are lists, not executed forms.
Immutable let forwarding and direct same-module returns preserve the value.
Numeric elaboration, unquote, evaluated set constructors, general match
execution, complete effect/profile legality and migration remain separate.
Evaluated vectors and maps have their own development boundaries below.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-recursive-quote-lowering-development-kernel \
  --exact current-gravity-typed-lowering-input-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
# Run separately when this source -> C7 -> lowering -> runtime boundary changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-recursive-quote-authenticated-composition
```

The direct quote kernel consumes normalized records with an explicit verified
context precondition; it is not another public authentication entrypoint. Its
four-unit logical projection charge does not count all VM work. The context
already reserves ledger lookup work, and the projection adds no recursive
comparison, hashing, serialization or DATA reconstruction. Opaque sentinel
controls test that boundary without claiming those sentinels are accepted
source DATA.

The affected composition uses the complete `local-runtime-quoted-data`
`.gravity`/`.qst` pair, not a selected subset of a module containing unsupported
bodies. Each original C5/C6/C7 product is constructed once and reused for a
representative public function compile, whole-module assembly and execution
through the actual runtime leaf. Deep payload, type and source forgeries are
rejected by fresh public C7 verification. Existing owning C5/C6/C7 malformed
quote, unresolved-name, annotation and numeric-representation rejection
evidence is not rebuilt merely because this lowerer changed.

### Canonical evaluated vectors

Edit the same Gravity emitter and `bootstrap/gravity/src/gravity/runtime.gravity`.
C6 already supplies ordered constructor children and source coordinates; C7
supplies positional element types and Never propagation. Lowering compiles
those verified children into existing `:vector-literal` IR. It does not return
the source form's `:source-value` as an evaluated result. Empty and nested
vectors, mixed represented values, typed parameters, quoted DATA elements,
immutable let forwarding and direct local calls use this path.

The runtime checks the closed `{:op :vector-literal :items [...]}` descriptor,
including the actual vector of instruction maps, before item
execution. This is shallow descriptor validation, not a whole nested-IR proof.
Items run once from left to right with operand recur targets cleared. Normal
values accumulate into a vector; the first thrown or rejected result keeps its
payload/output and skips later items. Control-looking quoted DATA remains a
normal element. Fatal host failures remain fatal, not guest throw values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-vector-lowering-development-kernel \
  --exact current-gravity-evaluated-vector-runtime-development-kernel \
  --exact current-gravity-evaluated-vector-c8-development-kernel
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-vector-authenticated-composition
```

Remove join-unit reservations and remaining-budget plumbing from lowering.
Missing/unknown types and
incoherent Never, source, order or count facts reject with `P15S23Q002`.
Malformed runtime descriptors reject with `P15S23X002`. Evaluated sets retain
`:unsupported-typed-lowering-operation`; maps have the separate bounded
development subset below. Unsupported language forms are not described as
malformed vectors.

This selects compiler-internal `:meta/:jvm` value construction. P3 permits
internal compiler allocation, and L10 makes its visibility regime-dependent;
neither empty declarations nor C7 acceptance grants arbitrary allocator or
runtime authority. Independent current C8 body checks retain element effects,
ordered coverage and declaration rejection. The existing internal
`body-analyze-module` consumes the original request and `typed.identity-input`
facts, not the outer typed artifact. These checks do not replace the missing
full C8-to-lowering/MIR legality boundary. The existing public typed input,
fresh C7 verification, source provenance and explicit pending effect/profile
legality status are unchanged. No MIR, deployable-program, hosted allocator,
target, authoritative migration or self-hosting acceptance follows.

### Canonical evaluated maps

Use the same emitter, `gravity.runtime`, and runtime-iteration test namespace.
The collection lowerer consumes G's ordered C6 children and C7's flat ordered
member types, sharing the vector source/type joins and child compiler. The
initial map subset requires distinct literal Keyword keys; computed,
non-Keyword and duplicate evaluated keys remain unsupported, not newly illegal
Gravity. Quoted maps remain inert DATA, including existing duplicate-key
materialization. Neither lowerer reads a source map as the evaluated result.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-map-lowering-development-kernel \
  --exact current-gravity-evaluated-map-runtime-development-kernel
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-map-authenticated-composition
```

The existing `:map-literal` IR contains an ordered vector of closed key/value
instruction pairs. Runtime shape checks precede evaluation. Keys and values
then run once in entry order, preserving the first transfer, payload and output
and skipping later entries. Lowering reserves `12 + 14 * child-count` logical
join units, including both children of each pair, before child compilation;
this is not a total execution or heap bound. Keep vector/conservation checks
when changing their shared helper, and run affected composition separately.
That composition reuses each paired source product for public lowering,
assembly, execution and independent C8 body checks with `typed.identity-input`
facts. It also checks source-order and typed-input forgeries and the new
String-versus-map result rejection, rather than replaying old whole proofs.

The source-order guarantee belongs to canonical four-argument typed lowering.
The runtime executes ordered instruction DATA; it does not certify how legacy
untyped host-map compilation selected that order. That legacy route's key
sorting is not repaired by adding map execution. Full effect/profile/provider
legality, general collection operations, target support and authoritative
migration remain separate. Real-source validation results belong in the
backlog, not inferred from these normalized checks.

### Literal Keyword map lookup

For `(:key map-expression)`, edit the existing C7 engine for inferred member
types, the typed emitter for solved-row projection, `gravity.effects` for
operand effects, and `gravity.runtime` for instruction execution. SH-07's
existing canonical lookup record owns the one-operand shape and evaluation
order; do not add a second dispatch interface.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-map-lookup-lowering-development-kernel \
  --exact current-gravity-keyword-map-lookup-runtime-development-kernel \
  --exact current-gravity-keyword-map-lookup-c8-development-kernel
# Affected paired-source composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-map-lookup-authenticated-composition --fail-fast
```

The initial development subset derives map shape from constructors, verified
quoted DATA and immutable let ancestry, including nested lookup extraction.
A present member retains its type; established absence yields Nil. Missing
shape evidence is an implementation boundary, not implicit Dynamic or a new
language prohibition. A Never operand remains non-returning. General `get`,
defaults, arbitrary key types and call-returned shape propagation are separate
increments.

The producer records source, ordered operands, reconstructed shape, selected
coordinate and presence in a solved constraint. Fresh public C7 verification
owns recursive reconstruction; lowering joins that verified row rather than
adding another recursive checker. Remove lookup/extracted-evidence logical
charges, inference counters and capacity-only depth/size/alternative quotas.

Runtime validates the closed two-argument instruction, evaluates the literal
key and whole map operand once in order, then performs checked lookup. A
missing key does not skip operand evaluation. Throws, rejection, payload
identity and accumulated output propagate unchanged. Effect analysis must
retain the operand's effects and escaping thrown types. Run the affected
paired-source composition separately once its candidate is stable; the
normalized kernels do not prove source composition, migration or self-hosting.

The older `sh07-b15-rejected-arities-use-the-normative-shape-diagnostic`
selector reconstructs six complete source frontdoors. It is a costlier
upstream regression, not an arity kernel or a prerequisite for every downstream
lookup edit. Select it when changing canonical lookup arity/shape behavior;
do not add it automatically to the focused lookup command above.

### Constructor shapes returned by local calls

Shape propagation belongs to the existing C7 engine, not runtime dispatch or a
caller-provided shape summary. Follow the actual same-module acyclic callee's
body using its established parameter types and authenticated call, edge,
binding, definition and function records. Retain that ancestry in the existing
lookup or structural-match constraint. Equal return-type vectors do not prove
equal map keys. Do not specialize a callee from caller argument values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-returned-map-shape-development-kernel \
  --exact current-gravity-keyword-map-lookup-lowering-development-kernel
```

Use these normalized checks while editing the shape path. Affected composition
must separately build a real map-returning function, forward its result and
look up the member through current typed lowering and runtime execution. Reuse
each source product within that test; do not rerun the unchanged lookup matrix
as a prerequisite. Structural-match compiler acceptance does not establish
runtime match lowering.

```bash
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-returned-map-shape-authenticated-composition --fail-fast
```

Current facts take precedence over prior-round facts within the same fresh
inference invocation, permitting caller-before-callee source order without a
cache. Remove call-index/join and retained-evidence accounting, inference fuel
and capacity-only seed/reconstruction quotas. Avoid constructing unused call-shape
indexes for unrelated type-only modules. Imported and recursive return shapes,
unknown map parameters and unresolved alternative return shapes remain
explicit implementation boundaries.

### Finite Boolean match lowering

Edit the existing Gravity emitter for execution of C7's solved finite Boolean
matches. Root `true`, `false`, wildcard and lexical binding patterns use the
existing `if`, `do` and `let` instructions; no runtime match operation or
generated temporary name is needed. The scrutinee is evaluated once, before
the selected body. Keep a residual binder inside its own branch so it cannot
shadow an outer same-named value used by a sibling branch.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-boolean-match-lowering-development-kernel \
  --exact current-gravity-boolean-match-routes-development-kernel \
  --exact current-gravity-boolean-match-call-errors-development-kernel \
  --exact current-gravity-boolean-match-fixtures-development-kernel
```

These normalized checks exercise the emitter and its evidence joins, not a
complete source pipeline. Select C7 rows by their match identity and clause
ordinal: their `lhs` identifies a branch body, not the match. Preserve source
order, narrowed binder scope, original failure diagnostics and branch tail
targets. Preserve binder-use traversal without a lowering work allowance.

Throwing local calls through a match also use C7's existing call-error graph and
the independent verifier in `gravity/effects.gravity`. Their supported-form
checks must agree with the existing conservative child-error union. This is
source-error potential, not a claim that every branch executes at runtime.
When changing interaction with source `let` or its instruction census, also
select `current-gravity-typed-let-lowering-development-kernel`; it checks the
existing let-body `do` wrapper directly. An unchanged lookup matrix is not a
prerequisite for editing match lowering.

Before accepting this increment, separately exercise real source through C7,
lowering and runtime, including both Boolean values, whole-domain binders and
wildcards, shadowing, throwing scrutinees and valid branch-tail recur. A focused
pass alone does not establish that composition. Partial Boolean matching,
structural pattern execution and guards remain separate implementation work;
this finite lowering limit is not a general L7 requirement that every match
be total. Normally returning guards retain L7's Bool-only policy.

```bash
# Affected source/type/effect/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-boolean-match-authenticated-composition --fail-fast
```

### Root Keyword match typing: development implementation

The current C7 extension reuses structural matching's existing scalar domain,
ordered partition, branch-local extraction and type-join machinery. Start with
the owning type kernel:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-match-development-kernel \
  --exact sh08-keyword-call-match-development-kernel \
  --exact sh08-keyword-match-fallback-and-source-development-kernel \
  --exact sh08-keyword-loop-match-cold-inference-development-kernel
```

An established Keyword parameter has an open domain. Literal clauses remove
their values from that domain; a fallback binder retains those exclusions.
An inner match on the binder or its immutable alias must not forget them.
Known literal/quote and validated alias domains remain exact, rather than
being widened to all Keywords. Open partial Keyword matches are currently
unsupported; this is not a language-wide totality rule or proof of closed-type
exhaustiveness. Existing Bool-only guard semantics are unchanged.

A private root-scalar demand may use an independently established Keyword
result without reconstructing a selected constructor coordinate. It does not
establish that type, authenticate a call, or grant deep selected-shape/callee
ancestry. The ordinary C7 call/source checks and guarded nth proof remain
required. Constructor, lookup, callee-body and literal-nth projection paths
retain their existing shape obligations.

The first kernel covers parameter inference, residual-binder and alias
preservation, exact literal domains, wrong-family patterns and source/binding
mutations. The fallback/source kernel adds wildcard coverage, duplicate-clause
rejection, escaped or wrong-scope binders and exact quoted/aliased domains.
The cold kernel starts with empty type/proof tables, derives the
existing loop and guarded-index facts, and then types the residual Keyword
binder. Binder seeding consumes the established Keyword result instead of
losing it through nominal call re-inference. A normally returning String branch
remains String and cannot acquire the loop's Never premise. No second range
solver or inference-order change is involved.

The call-shape kernel supplies an established Keyword result and checks its
shallow call/source joins and work boundary. It does not independently infer a
real callee's result or recover its constructor ancestry.

The existing emitter lowers scalar Keyword matches to the existing
`structural-match` instruction. Its focused check joins the current scrutinee
type, source, ordered rows and branch-local binder evidence:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-match-lowering-development-kernel
```

These kernels provide focused development feedback, not migration or
self-hosting evidence. The combined regression batch passed 13 tests and 332
assertions; the final lowering/fixture batch passed two tests and 37 assertions.
They overlap and are not summed as unique coverage. An earlier extra closing
delimiter stopped the fallback test's namespace load before assertions; the
corrected fallback kernel separately passed 15 assertions.

Run the affected source composition separately once the candidate is stable:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-match-authenticated-composition --fail-fast
```

Its paired fixture connects parameter and local-call scrutinees, exact literal
aliases, function recur, guarded vector indexing and Keyword throws. Rejected
source fixtures and fresh C7/C8 checks are separate from the normalized kernels.
The source `escape` fixture tests first-class `nth` escape, not a match binder
escaping its clause; binder-use and scope rejection currently have focused
ownership coverage. Parsing paired fixtures is not source-to-execution evidence.

The single frozen composition completed one test with 243 passing assertions,
one failure and no errors. Test-var execution took 1,044.249 seconds plus 8.248
seconds of namespace loading at 2 GiB. All 31 observed compiler, runtime, test
and fixture hashes remained unchanged. The original aggregate remains failed.
Both positive extensions passed actual local-call return typing, five matches
and eleven clause rows, function/loop recur, and seventeen guest outcomes each.
The scan returns three or throws the actual unmatched Keyword value; the catch
returns 99. Ordinary Keyword truthiness in `if` remains distinct from Bool-only
guards.

Seven mandatory fresh public C7 operations cover four positive operations and
three altered candidates. The stale nth source echo still passes its finite
helper but fails full reconstruction. Fictional exclusions and a changed
binder extraction are rejected by public reconstruction; these are not an
isolated partition-causality or comprehensive verifier-correctness proof.
Positive extraction and accepted/nonempty C8 graph guards prevent trivial
missing-value rejection credit. Both whole-effect verifications and the three
coherently resealed effect-graph hostiles passed.

All ten rejected `.gravity` fixtures rejected. The sole failed assertion was
the outside-guard diagnostic set: an unestablished match binder reaches the
earlier `C7-ANNOTATION :typed-throw-payload-type-required` prerequisite, rather
than one of the three anticipated later diagnostics. The expectation now
includes that observed pair; compiler behavior was not weakened. Other allowed
diagnostic memberships do not uniquely identify which alternative matched.
Rejected `.qst` files have byte parity, not separate execution coverage. The
unchanged expensive composition is not replayed merely to make its aggregate
green. Check unknown throw-payload diagnostic ownership directly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact direct-typed-throw-source-and-type-development-kernel
```

The existing unknown-payload assertion now checks the exact rule and reason.
It passed 23 assertions; together with the unchanged lowering and fixture
checks, the post-correction batch passed three tests/60 assertions. Only the
two test files changed after the composition; its original verdict is intact.

Open partial Keyword matches, other root scalar families, generalized unions
and deep selected-shape propagation remain separate implementation work.
Quoted Keyword domain propagation has focused coverage only; this source
fixture does not exercise quoted-match composition. This is scoped executable
development support, not authoritative migration or a target/release claim.

### Constructor-linked structural match execution

For unguarded structural matches, edit the existing emitter and stage-owned
`gravity/runtime.gravity`. C7 owns shape inference, partitioning, narrowing and
exhaustiveness. Lowering consumes its freshly verified pattern tree and joins
source coordinates, clause order, binding scopes, types and uses; it does not
solve the shape problem again.

Start with these existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-structural-match-lowering-development-kernel \
  --exact current-gravity-structural-match-runtime-development-kernel \
  --exact current-gravity-structural-integer-pattern-development-kernel \
  --exact compiler-module-whole-runtime-data-development-kernel
```

The bounded projection emits one closed internal `:structural-match`
instruction. Fixed vectors require exact width. Literal-Keyword map patterns
permit extra keys and distinguish an absent key from a present nil. Recursive
patterns support wildcard, lexical bindings and represented primitive leaves;
primitive-family checks precede equality. Represented Integer literals are not
an implicit coercion to I64 or other numeric types. Keep unsupported numeric
elaboration explicit rather than interpreting host numeric equality as policy.

The scrutinee executes once without an outer recur target. Only the selected
body executes, retaining its original tail target. Failed partial bindings
must not leak into later clauses; throws, rejected control and fatal errors
retain their original payload and output. Validate the whole closed descriptor
before evaluating its scrutinee. Remove descriptor depth/work quotas; complete
descriptor validity is required without a capacity-oracle refusal.

These normalized checks are development feedback, not source composition or
authoritative migration. A changed projection or runtime instruction also
requires the affected paired source/type/effect/lowering/runtime composition,
including genuine constructor-derived patterns and public C7 rejection of
coherently changed extraction evidence. Reuse each fresh source product within
that composition; do not repeat unchanged Boolean/map/vector proof matrices.
Select the existing Boolean lowering/routes kernels when changing shared
match dispatch. Guard execution has its own checks below; additional L7
pattern families remain separate implementation work, not language prohibitions.

```bash
# Affected real-source composition, outside the edit loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-structural-match-authenticated-composition --fail-fast
```

### Root-map guard completion and execution

L7 guards that return normally require an established Bool result. `true`
selects the body, `false` continues, and non-returning `Never` preserves its
control transfer. There is no truthiness conversion. Change C7 for completion,
narrowing and result facts; `gravity/effects.gravity` for independent effect
joins; and the existing emitter/runtime for execution.

Start with the owning producer checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact sh08-root-map-guard-development-kernel \
  --exact sh08-root-map-guard-literal-development-kernel \
  --exact sh08-root-map-guard-completion-development-kernel \
  --exact sh08-guard-match-forward-inference-development-kernel \
  --exact sh08-quoted-data-guard-development-kernel \
  --exact body-effect-guard-development-kernel

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-root-map-guard-lowering-development-kernel \
  --exact current-gravity-root-map-guard-runtime-development-kernel \
  --exact current-gravity-guard-local-loop-parent-development-kernel \
  --exact current-gravity-guard-recur-diagnostic-envelope-development-kernel
```

Keep every body's original type and legality checks. A body that cannot execute
after a false or Never guard contributes no normal match result; its separate
effective-completion evidence does not relabel that body as Never. An unresolved
potential result remains Unknown until inference resolves it, including when
a later branch never returns. Never guards consume their structurally matched
cases by transferring control; they do not select a body or fall through.

After structural success, evaluate the guard once with its pattern bindings in
non-tail position. Preserve lexical target ancestry for authenticating a local
loop; that ancestry does not permit a direct recur from the guard. False
restores the original bindings for later
clauses while preserving output. Only the selected body receives its original
tail target. Nested guards must preserve authenticated binding-use order, not
assume that the grouped Core child vector is an execution schedule. Conservative
source-error summaries remain separate from selected-path precision.

These are normalized development checks. Changes to guard facts or execution
also need one affected paired real-source composition through C7, lowering,
runtime and actual C8 body/allowance checks before acceptance. Preserve direct
guard-tail-recur and escaped-binding rejection. The unchanged
`sh07-b10-root-map-guard-source-rejections` selector constructs four complete
upstream source products; it is not a cheap prerequisite for every guard edit.
Use affected source negatives in the selected composition rather than replaying
that unchanged matrix. No new authority admission or migration pin is needed
for this development feedback.

```bash
# Affected paired source/type/effect/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-root-map-guard-authenticated-composition --fail-fast
```

### Constructor allocation effects

Change `bootstrap/gravity/src/gravity/effects.gravity` for evaluated collection
allocation inference, latent/caller propagation and source declarations. L6
already registers `:memory/allocate`; STD3 requires constructor allocation
behavior to be visible. Start with the current-source kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-constructor-allocation-development-kernel \
  --exact body-allocation-fixtures-development-kernel \
  --exact body-effect-operation-development-kernel \
  --exact body-effect-call-metadata-development-kernel \
  --exact current-gravity-evaluated-vector-c8-development-kernel \
  --exact current-gravity-keyword-map-lookup-c8-development-kernel
```

The constructor's own allocation is conditional on normal operand completion.
A Never operand prevents construction but does not erase operand effects or
errors. Quote remains inert DATA, not evaluated nested construction. Function
bodies retain latent effects; defining a function does not execute its body.
Existing declaration checking rejects inferred allocation outside allowances
with `C8-UNDECLARED`.

The effect node's allocation record describes static behavior. Extent counts
constructor input positions: map entries or vector/set elements. It is not
heap bytes, observed allocations or a set's runtime cardinality after equality
deduplication.
Placement, allocator-provider selection and ownership remain pending. Effect
inference and a source allowance do not grant allocator capabilities or prove
complete profile/provider legality.

For changes to this rule or its graph representation, run the affected source
composition separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-allocation-authenticated-paired-composition --fail-fast
```

It reuses fresh accepted/rejected source/type products and checks the actual
effect owner plus fresh whole-result reconstruction. It does not rebuild the
unchanged public C8 module bundle. Select that broader module composition only
when its forwarding/module interface changes; do not replay unchanged emitter
or runtime matrices for an effects-only correction.

C8 owns effect reconstruction, not a second implementation of C7's type and
shape solver. Its current public body-input gate checks a host-bound report
envelope; a passed report or matching digest does not itself execute fresh C7
verification. The paired tests above explicitly construct and verify current
C7 products before checking effects. Preserve that distinction: C8 must check
the source, ordered operands, completion facts and dependencies that determine
its effects, but deep type/shape authenticity remains the upstream boundary.
Do not add a fresh-proof wrapper to the local effect-edit loop or describe
effect-result reconstruction as independent certification of C7 correctness.

### Bare Map parameters: focused feedback

Change the existing `gravity.checked-core` signature owner, C7 engine, effect
checker and typed emitter. The new compatibility boundary retains each actual
argument type and its own declared-parameter premise, including mixed Map,
bare List, homogeneous List and scalar calls. It must not invent a structural
Map shape or erase other type obligations. Start with the existing runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-bare-map-parameter-development-kernel \
  --exact sh08-mixed-map-list-parameter-development-kernel
```

These are normalized source/compatibility checks, not accepted whole-module
products. Keep existing List row versions unchanged when no Map adaptation
occurs. Independent downstream reconstruction and real paired-source execution
remain separate integration work; no fresh authoritative admission is required
for these focused tests.

For the independent compatibility consumers, select the existing namespaces:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-mixed-map-list-parameter-development-kernel \
  --exact current-gravity-mixed-map-list-parameter-development-kernel
```

These checks reconstruct mixed parameter premises and whole compatibility rows,
not a fresh whole-source product. Full typed-call and source integration must
still exercise the real callee declarations, return types and execution.

For family-only removal, use these focused selectors:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-family-removal-development-kernel \
  --exact body-map-family-removal-development-kernel \
  --exact current-gravity-map-family-removal-lowering-development-kernel
```

These require bare-family operation evidence, reject invented member shapes and
check normal allocation versus operand transfer. They do not authenticate a
whole source module. Retain failed controls separately and rerun only selectors
affected by the diagnosed correction, not an unchanged passing aggregate.

Once the changed candidate is stable, run the affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-parameters-authenticated-composition
```

It selects four fresh inputs: the accepted Gravity/QST pair, a non-Map call
rejection and an undeclared-allocation rejection. Accepted products are reused
for compatibility, effect, lowering and runtime checks; other negative fixture
pairs do not acquire fresh source-rejection credit from header checks.
Public C7 reconstructs upstream type facts. C8 independently checks the
compatibility evidence it owns, not a duplicate general quote/type solver.
This composition is development integration, not authoritative migration.

### Persistent Map removal: focused feedback

The current `dissoc` increment starts in the existing C7 type owner. For its
pure known-shape transition, use the direct selector:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-map-removal-transition-development-kernel \
  --exact sh08-map-removal-source-development-kernel \
  --exact sh08-map-removal-inference-development-kernel
```

This checks present/absent/empty removal, dense result coordinates, retained
survivor source parts, false/nil payload types and logical work limits. Its
input is an already established Map shape; acceptance by this private copying
helper does not establish member-type validity or authenticate source input.
The source kernel separately joins the real owning shape reconstruction to
the actual argument type and checks original forms, resolved call/binding
records, allocation legality and actual source rejection. These kernels alone
do not establish a compiler-supported source call. The inference kernel checks cold normal and
Never cases, ordered constraints, descriptor mutations and Unknown deferral
without a work allowance. Fresh public C7 reconstruction, independent C8
reconstruction, lowering/runtime behavior and affected real
source composition are checked separately below. The
generic STD3 Map rule is broader than the first literal-Keyword/known-shape
implementation frontier. No admission proof is needed to run this kernel.

For the independent effect-consumer increment, use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-map-removal-development-kernel
```

It reconstructs removal, source/call/fact/constraint joins, allocation/storage
records and work limits for normal present/absent/empty cases. The node-record
Never control checks operand effects and suppression of direct allocation;
it does not establish source-bound or whole-C8 noninvoked-call acceptance.
Fresh genuine upstream inputs, full graph checks and lowering/runtime/source
integration remain distinct obligations. A copied C7 input shape is not
independently authenticated merely because its removal transition matches.

For the existing typed emitter and stage-owned runtime, select:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-removal-lowering-development-kernel \
  --exact current-gravity-map-removal-runtime-development-kernel
```

These check source-bound descriptor reconstruction, per-call rows, rejected
work carry, binary IR, eager operands, first control transfer, Map/Keyword
value guards and inert stored payloads. Raw IR computed-key cases exercise
the evaluator, not wider source-key support. Normalized caller fields are not
authentic function-tuple integration. Keep the failed mixed batch distinct
from its corrected targeted tests; do not rerun unaffected selectors merely
to obtain a green aggregate. Real paired source and whole-artifact checks are
still required before accepting the complete slice.

For the stable changed candidate, the separate source composition is:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-removal-authenticated-composition
```

It builds the accepted Gravity/QST pair and one quoted-Map missing-allocation
rejection, reusing accepted products for all consumers and mutations. Genuine
non-returning calls, surviving-source provenance and ordered
row census belong here, not to the normalized kernels' claims. Other rejected
fixture pairs are not fresh source-proof results merely because their headers
pass. No W5 execution, general Map parameters, arbitrary source keys, target
support or authoritative migration follows from this selection.

The first actual Map-removal composition stopped after six loader assertions
and one error, before any fresh source product completed: automatic C2 cache
publication exceeded the existing 512 MiB aggregate store limit. This is a
development-path dependency, not a Map-removal result. The narrowly changed
default reader uses its existing fresh producer; the explicit cache API and
strict admission failures remain unchanged. Validate that dispatch and parity
with the existing two hosted C2 cache selectors before a distinct composition
run. Preserve the failed result rather than relabeling it or clearing the store.
The corrected reader tests passed 42 assertions; strict admission passed four
separate assertions. The changed-dependency composition passed 448 assertions
in 421.346 seconds. Its support remains the literal-Keyword/known-Map-shape
frontier, not general Map parameters or authoritative module admission.

### Persistent map association

The current-source development path for `(assoc map :literal-key value)` uses
the same C7 engine, effect owner, typed emitter and `gravity.runtime` evaluator.
Change those owning files rather than adding a new lane or wrapper. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-association-development-kernel \
  --exact sh08-map-association-premises-development-kernel \
  --exact body-map-association-development-kernel \
  --exact body-map-association-catch-effects-development-kernel \
  --exact current-gravity-map-association-lowering-development-kernel \
  --exact current-gravity-map-association-runtime-development-kernel
```

The bounded operation adds or replaces one literal Keyword key in an
established map without changing its input. Values may have established types,
including Bool parameters and String results; they need not be literals.
Association results retain derived shape ancestry for later association,
lookup, supported local returns and structural matching. They are not
relabeled literal constructors. Operator, map, key and value retain once-only
source order; a control transfer prevents invocation. Allocation remains
conditional on normal operand completion, with operand effects preserved.
Typed catches can clear escaping error types without erasing the performed
`:error/throw` effect. Check those projections separately; effect-label
discharge remains pending profile, handler and capability authority.

General or dynamic keys, variadic association, unknown map parameters,
transients and complete collection-library support remain outside this slice.
Changes to the type/shape carrier, effect projection or executable instruction
require the affected real source/type/effect/lowering/runtime composition,
separately from these normalized kernels. Neither check admits a stable module
revision or establishes authoritative migration, target support or self-hosting.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-association-authenticated-composition
```

This selector shares each fresh paired source/type product across actual
lowering, guest execution and C8 checks. Deep association-ancestry mutations are
checked by the public lowerer's genuine C7 verification; effect-graph mutations
are checked by C8's whole-result reconstruction. The seven rejected `.gravity`
sources exercise type/key/arity/allocation boundaries; the cheap fixture check
establishes byte-identical `.qst` counterparts, not separately executed negative
source coverage.

### Collection count

The same C7 engine, effect owner, typed emitter and runtime own direct
`(count collection)` for currently established Map, Vector and persistent List types. Use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-collection-count-development-kernel \
  --exact body-collection-count-development-kernel \
  --exact current-gravity-collection-count-lowering-development-kernel \
  --exact current-gravity-collection-count-runtime-development-kernel
```

The query returns `Integer`, not an implicit I64, or Never when its operand
does not return normally. It evaluates the operand once and counts the actual
top-level elements or associations at runtime. It does not substitute a
compile-time member count and erase operand work. Its own query has no
collection-construction effect; operand allocation and error effects remain.

Do not route this query through structural-pattern analysis. A legal typed
quoted map with String keys, or nested values outside the pattern domain, can
still be counted. Count needs the established collection type, not a deep
shape proof or a static-cardinality artifact. Keep authentic call/source/type
joins and fresh verification at their existing boundaries. Unknown operands
still reject; this is not a new generic Map annotation or type-union feature.
String Unicode counting, nil conventions and other collection families remain
separate language/library work.

For changes to this query or its typed instruction, run the affected source
composition separately from the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-collection-count-authenticated-composition
```

It reuses the paired current source products for type/effect checks, public
lowering and execution. The source cases include quoted materialized maps,
aliases, local returns, association, guards and control transfer. The separate
rejected `.gravity` cases check operation and type diagnostics; cheap parity
checks cover their `.qst` counterparts. This is affected composition, not
authoritative migration or complete collection/profile/target support.

Persistent List cardinality follows STD3. Use the
same four owners and focused selectors, not a new lane or C5/C6 bridge. Bare,
positional and homogeneous List types establish the collection family; `count`
does not need member typing, nonemptiness or a static-length proof. Validate
closed argument containers and unary arity before collecting operand type
work. Keep existing Map/Vector evidence unchanged where possible and give
List evidence an accurate input-family description.

For the new List behavior, add these existing-namespace selectors to the same
direct command above:

```text
--exact sh08-list-count-development-kernel
--exact body-list-count-development-kernel
--exact current-gravity-list-count-lowering-development-kernel
--exact current-gravity-list-count-runtime-development-kernel
```

Private opaque-member and large-metadata controls check that this query does
not visit member data. They do not admit host objects or enlarge upstream
source/type bounds.

The affected List composition is separate from the earlier Map/Vector source
aggregate; do not rerun that unchanged aggregate for the List extension:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-count-authenticated-composition
```

The accepted Gravity/QST pair covers bare List parameters, homogeneous tails,
an explicitly typed List-returning producer and an aborting operand. Products
are constructed once per source and reused for runtime and hostile checks;
one String source supplies the representative semantic rejection. Qualified
core-operator source resolution remains a separate C5 frontier, so this pair
uses ordinary `count`. The normalized qualified-header control is not source
resolution evidence.

### Persistent vector append

For the fixed-arity `(conj vector value)` slice, edit the existing C7 engine,
`gravity/effects.gravity`, typed emitter and stage-owned `gravity/runtime.gravity`.
STD3 owns persistent collection semantics; the core spelling is a compatibility
resolution, not evidence of a complete standard-library module or provider.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-append-development-kernel \
  --exact sh08-vector-append-projection-development-kernel \
  --exact body-vector-append-development-kernel \
  --exact current-gravity-vector-append-lowering-development-kernel \
  --exact current-gravity-vector-append-runtime-development-kernel
```

An established vector's complete ordered element types are followed by the
appended value type, including heterogeneous values. Normal results retain the
actual ordered element types. Operator and operands evaluate once, left-to-right;
the original vector and prefix values are preserved. A Never operand prevents
the append's own allocation without excusing invalid source or operand types.
Operand effects and error transfer remain visible. Remove append debits,
module work pools and returned cumulative accounting totals.

`count` consumes the established result type. Literal `nth` projection retains
an explicit append application/source link: an appended coordinate selects the
new value; a prefix coordinate delegates to the original vector. It does not
relabel the append as a constructor or inspect unrelated nested prefix data.
Whole-shape consumers currently require one established vector shape.
Appended-vector S/T iteration and growing-vector recur types are not silently
admitted by this extension; their origin/type frontiers remain separate work.

Focused checks are development feedback. Changes to this slice also require
affected real paired source through current C7/C8 verification, lowering and
execution, including persistent input preservation, append-to-count/nth,
throwing operands and rejected family/arity/extent/allocation cases. This is
affected integration, not an authoritative module migration or a collection,
profile, target or self-hosting completion claim.

For changes to this composition, run the existing source selector separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-append-authenticated-composition --fail-fast
```

The cheap `current-gravity-vector-append-fixtures-development-kernel` checks
paired source parity, parsing and test-only diagnostic handling; it does not
establish semantic acceptance or rerun the oversized source.
The backlog records the original partial-error composition and its separate
remaining-negative result. The oversized literal reaches C6's fragment-form
bound before C7's vector extent check; the latter has normalized kernel coverage.

### Value equality development

STD2 owns value equality; STD4 requires exact text semantics. The current
binary `=` development slice covers Nil, Bool, Character, String, Symbol and
Keyword. Same-family operands compare values; different supported families
compare false. Strings compare the exact scalar sequence without normalization
or locale folding. The scalar path does not introduce numeric or function
equality typing, nor does it change ordinary integer runtime compatibility.

STD3 also defines ordered persistent Vector value equality. Its current
development path accepts independently established finite Vector types with
complete ordered element types, recursively containing Vectors and the
supported scalar or nil-bearing scalar alternatives. This is a value query,
not a requirement for literal-constructor ancestry. Counts, positions and
stored values matter; physical sharing and type identity do not decide the
answer. That legacy recursive version excludes numeric members; see
[Flat Generic Integer Vector comparison](#flat-generic-integer-vector-comparison)
for its separate flat extension and
[finite Keyword-Set leaves](#flat-vector-comparison-with-keyword-set-leaves)
for bounded Set composition. Bare/unknown Vector types and other collection
domains remain unsupported by this implementation, not forbidden by STD3.

The integral path follows STD5 and SAFE9: two independently established
operands of the same integral type compare their original integer values.
It records `:proof-required` and `:proven-safe` because comparison performs
no conversion or arithmetic and cannot overflow or round. The supported type
families are Integer, I8/I16/I32/I64 and U8/U16/U32/U64. Generic Integer does not
select a new machine-width or arbitrary-precision arithmetic policy. Mixed
types and floating/decimal/ratio equality remain separate implementation
frontiers. The flat Vector extension accepts Generic Integer only; fixed-width
and nested numeric Vector members remain outside it.

Use the owning type, effect, lowering and runtime kernels without rebuilding
upstream products:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-atomic-equality-development-kernel \
  --exact sh08-character-literal-and-quote-development-kernel \
  --exact body-atomic-equality-development-kernel \
  --exact current-gravity-atomic-equality-lowering-development-kernel \
  --exact current-gravity-atomic-equality-runtime-development-kernel --fail-fast
```

For a Vector equality change, select its four owning checks instead:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-equality-development-kernel \
  --exact body-vector-equality-development-kernel \
  --exact current-gravity-vector-equality-lowering-development-kernel \
  --exact current-gravity-vector-equality-development-kernel --fail-fast
```

For an integral equality change, use its corresponding owners:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-integral-equality-development-kernel \
  --exact body-integral-equality-development-kernel \
  --exact current-gravity-integral-equality-lowering-development-kernel \
  --exact current-gravity-integral-equality-runtime-development-kernel --fail-fast
```

Its fixed-width type cases are normalized compiler evidence. Runtime checks
exercise the existing represented-Integer primitive, including adjacent values
above `2^53`; a host `integer?` check does not admit arbitrary raw values into
fixed-width source parameters. One Never operand does not excuse the other
operand's type legality.

The Vector checks cover bounded type/value traversal and rejected-work accounting.
Both complete runtime operand domains are validated before a length or earlier
element mismatch can return false. The query adds no guest allocation; operand
constructors still retain their C8 allocation effects. This is not a claim
that the interpreter's internal traversal uses no host allocation.
Add the existing scalar, nil-bearing or module-initialization checks when a
change also affects their shared dispatch or runtime owner; do not rerun every
historical equality composition for an isolated Vector change.

Operands require independently established supported types and exact source,
binding and evaluation-order joins. Normal results are Bool; Never prevents
invocation without excusing the other operand's legality. Equality introduces
no direct allocation, capability or error effect; operand effects remain.
Character transport uses the occurrence checks above, not a map-tag shortcut
or a new Character annotation spelling.

These kernels are development feedback, including distinct equal-content
Strings, NUL and supplementary text, complete Symbol/Keyword values, once-only
operand order and error transfer. Unsupported private-IR payloads reject without
invoking opaque equality or hashing callbacks; this is not source host-object
support. The Character guard recognizes representation, while C2 owns the
codepoint/text decoding theorem.

Affected real source composition is a separate required check before accepting
the complete executable slice:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-atomic-equality-authenticated-composition --fail-fast
```

It exercises paired current source through type/effect verification, lowering
and execution, including supplementary Character occurrences and distinct
ordinary Map DATA. Keep negative mutations tied to actual Character source
identities rather than rewriting every equal host-map payload. The cheap
`current-gravity-atomic-equality-fixtures-development-kernel` checks the fixture
inputs before that run, not the complete semantic composition. No authoritative
migration or standard-library completion follows from these development checks.

For a stable changed Vector integration, use
`current-gravity-vector-value-equality-authenticated-composition` in that same
SH01 namespace instead. It preserves W5's original modes definition/getter,
compares actual stored values, verifies C8 before execution, and reuses accepted
products for hostile mutations. Run this affected composition once after the
owning checks and review; documentation edits do not require reconstructing
unchanged products. The fixture's explicit allocation allowance does not settle
the full W5 module's current empty effect declaration.

The corresponding integral selector is
`current-gravity-integral-value-equality-authenticated-composition`. It checks
real W5-style count and private ordinal comparisons, a typed left-operand
transfer that suppresses the right operand, and mixed I64/U64 source rejection.
The accepted I64/U64 function bodies are compiled and checked, not invoked with
raw host values. Handler type flow does not discharge profile/provider or
capability authority; the effect artifact retains that pending boundary. Keep
those claims separate from the executed generic-Integer results.

### Persistent List prepend: focused feedback

STD3 defines binary `(conj items value)` for persistent Lists as prepend,
leaving the original ordered values after the new head. The development slice
supports reverse-and-join with persistent List accumulators. C7 owns receiver-type dispatch and result typing; C8 owns
independent reconstruction and allocation; the typed emitter and runtime own
execution. Preserve the existing vector-append path rather than treating a
List as a Vector.

Start with the existing direct component selectors:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-prepend-development-kernel \
  --exact sh08-vector-append-development-kernel \
  --exact body-list-prepend-development-kernel \
  --exact current-gravity-list-prepend-lowering-development-kernel \
  --exact current-gravity-list-prepend-runtime-development-kernel \
  --exact current-gravity-list-prepend-fixtures-development-kernel
```

The new rule preserves a matching homogeneous member type, prepends to finite
positional types, and leaves a bare List bare. Unknown values do not invent
membership. Preserve operand/result validity without width/depth/work quotas;
an aborted invocation adds no prepend allocation.
These focused checks include normalized downstream and runtime kernels, not
fresh source composition or authoritative migration. When the source-to-typed
interface or affected composition changes, use the existing
`current-gravity-list-prepend-authenticated-composition` selector in the SH01
namespace once on the stable candidate. It checks the real reverse-and-join
source path and semantic rejection cases. Do not rebuild unchanged source
products to repeat a green aggregate; do not update admission fingerprints for
ordinary development.

### Persistent String-Set insertion: focused feedback

STD3's binary Set insertion preserves existing equal members and adds only an
absent value class. Change the owning C7, C8, typed emitter and runtime paths,
preserving the separate List-prepend and Vector-append behavior. The bounded
initial String slice required independently established finite all-String Set
input and a String value and returned only a bare Set. The inference-carrier
extension below retains the String member domain instead, without inventing
exact cardinality, literal ancestry or presence evidence.

Start with the direct kernels and affected List/Vector, Set-count and String
membership/equality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-set-insertion-development-kernel \
  --exact sh08-string-set-insertion-throw-consumer-development-kernel \
  --exact body-string-set-insertion-development-kernel \
  --exact current-gravity-string-set-insertion-lowering-development-kernel \
  --exact current-gravity-string-set-insertion-transfer-fence-development-kernel \
  --exact current-gravity-string-set-insertion-runtime-development-kernel
```

Check complete String domains and value-class uniqueness before hit/miss or
construction, including equal-but-distinct Strings, Unicode distinctions,
original-value preservation and semantically unsupported families.
Remove content/construction allowances and budget-only refusals. Invocation
declares possible
allocation and storage retention even when the member is already present.
Ordinary once-only operand evaluation and transfers remain observable.

Compiler checks retain source, binding, producer and type joins. Validate
closed scalar IDs before lookups and avoid hashing composite type records
before their semantic validation. C8's collection-family census is closed; emitter
failures retain owner, completed-prefix and failed-child identity. Current private
helper inventory is a code/closure fact, not an authority-admission prerequisite.

Use the paired fixture selector
`current-gravity-string-set-insertion-fixtures-development-kernel` for cheap
G/Q parser feedback. A stable affected composition must separately exercise
`count` of insertion results and a genuine rejected member family. A bare Set
still does not qualify for String insertion, membership or equality without
independently established member-domain evidence. The carrier below supplies
that domain for insertion and membership, but not finite equality or whole
B14 recursion. Focused checks are
development feedback, not current-closure migration or release evidence.

For that affected boundary, run only the existing
`current-gravity-string-set-insertion-authenticated-composition` selector in
the SH01 namespace on the stable revision. It checks empty, absent and present
insertion through actual C7/C8, lowering and one closed-plan entry, plus a
Character-member C7 negative. Its fresh C6 development session and same-catalog
replay are not independent public C6 admission. Q remains parser-only. Reuse
unchanged passing focused results; do not repeat source-product construction
merely to obtain another green aggregate.

### Finite Keyword-Set insertion: focused feedback

STD3's same Set `conj` law applies to independently established finite literal
Keyword Sets and a Keyword value: preserve present equal members and add only
absent values. Edit the existing C7, C8, typed emitter and runtime owners. The
result is a bare Set, not a homogeneous carrier or evidence of cardinality,
membership or recursive parameter specialization. `count` can consume it;
nested insertion still needs independently established member evidence.

Use the direct development runner from the repository root:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-keyword-set-insertion-development-kernel \
  --exact sh08-keyword-set-insertion-throw-consumer-development-kernel \
  --exact sh08-keyword-set-insertion-id-guard-development-kernel \
  --exact body-keyword-set-insertion-development-kernel \
  --exact body-keyword-set-insertion-id-guard-development-kernel \
  --exact current-gravity-keyword-set-insertion-lowering-development-kernel \
  --exact current-gravity-keyword-set-insertion-lowering-id-guard-development-kernel \
  --exact current-gravity-keyword-set-insertion-transfer-fence-development-kernel \
  --exact current-gravity-keyword-set-insertion-runtime-development-kernel \
  --exact current-gravity-keyword-set-insertion-fixtures-development-kernel \
  --fail-fast
```

Include existing String insertion, List prepend, Vector append and Keyword
membership regressions when editing shared dispatch. Preserve
the old empty-Set/Never String route and pure wrong-family diagnostics. An
unsupported mixed Set has no promised enumeration order: its public rejection
may name the selected String or Keyword domain; direct String-helper rejection
remains exact. Check full member validity and uniqueness before hit/miss,
membership and fresh reconstruction, opaque-ID fences, original
source/fact/ledger joins, once-only operands and both transfer positions.

For changed source-to-runtime composition, select only
`current-gravity-keyword-set-insertion-authenticated-composition` in the SH01
namespace on a stable revision. It checks one positive program through C7/C8,
assembly and one closed entry, plus one Character-Set typing rejection. This
uses fresh C5 and a fresh C6 development session with same-catalog replay,
not independent public C6 admission. Q fixtures are parser-only. Do not repeat
unchanged source construction to refresh a stamp. Logical work and selected
hostile-provider checks do not establish arbitrary provider CPU/heap bounds,
native execution, whole-compiler consumption or authoritative migration.

### Finite Symbol-Set insertion: focused feedback

STD3's same preserve-equal/add-absent `conj` law applies to independently finite
literal Symbol Sets (zero through 1,024 members) and a Symbol value. Change the
existing C7, effects, typed emitter and runtime insertion owners. Original
Symbol namespace/name equality applies; display units account work only.
The original v1 result was a bare Set. The inferred Symbol carrier increment
below supersedes that result policy with v2 member-domain evidence, not
cardinality or presence evidence.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-symbol-set-insertion-development-kernel \
  --exact sh08-symbol-set-insertion-throw-consumer-development-kernel \
  --exact sh08-symbol-set-insertion-id-guard-development-kernel \
  --exact body-symbol-set-insertion-development-kernel \
  --exact body-symbol-set-insertion-id-guard-development-kernel \
  --exact current-gravity-symbol-set-insertion-lowering-development-kernel \
  --exact current-gravity-symbol-set-insertion-lowering-id-guard-development-kernel \
  --exact current-gravity-symbol-set-insertion-transfer-fence-development-kernel \
  --exact current-gravity-symbol-set-insertion-runtime-development-kernel \
  --exact current-gravity-symbol-set-insertion-fixtures-development-kernel --fail-fast
```

Retain Keyword/String insertion, List/Vector updates and Symbol membership
regressions for shared dispatch changes. Guard IDs and validate family discovery,
independently reconstruct selected policy/source/ledger/allocation joins and
validate the complete actual domain, coherence and uniqueness before hit or miss. Normal invocation declares possible allocation even on a
hit. Preserve the scoped Never routes; selected callback checks do not promise
universally unchanged provider reads or arbitrary provider CPU containment.

For a changed composition, separately run the exact SH01 selector
`current-gravity-symbol-set-insertion-authenticated-composition`. It covers
empty, absent and present insertion consumed by `count`, plus a Character-Set
with Symbol value rejection. Fresh C6 development sessions/replay are not
fresh public C6 verification; QST is parser-only. That original composition
does not establish the newer carrier's repeated-insertion/storage boundary.
Symbol-Set equality and general loop joins remain separate work; the grounded
Symbol-Set loop subset below has its own checks.
The compiler's seen-Symbol scan is only a motivating subexpression, not an
executed whole helper. Reuse valid unchanged evidence without a stamp refresh.

### Inferred Symbol-Set carriers: focused feedback

The exact inferred carrier is
`{:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/symbol}`.
V2 insertion from an independently finite Symbol Set establishes it; subsequent
Symbol insertion preserves it. It supports existing `count`, ordinary predicates,
local bindings, inferred zero-argument returns and bounded List storage. It
does not establish extent, presence, annotation, private/recursive signature,
equality or finite-empty-to-carrier loop-join evidence by itself. The membership
consumer below is a separate checked increment. Runtime
insertion remains unchanged; edit the existing C7, effects and typed emitter
owners, not String-only private/signature predicates.

Use the existing development runner and namespaces above with these exact vars:
`sh08-symbol-set-carrier-development-kernel`,
`body-symbol-set-carrier-development-kernel`,
`current-gravity-symbol-set-carrier-development-kernel` and
`current-gravity-symbol-set-carrier-fixtures-development-kernel`. Retain affected
insertion, count, List, predicate, String/private and declared-self regressions.
Remove LC work charges, exact/one-below accounting and depth/width/size quotas.
Preserve Never and malformed/opaque-value rejection. V1 bare-Set records
must not stand in for v2 carrier facts.

When this composition changes, separately select the existing SH01 namespace's
`current-gravity-symbol-set-carrier-authenticated-composition`. It exercises
repeated insertion, `count`, return/local transport, List storage and `set?`/
`list?`, plus a wrong-needle carrier-membership typing rejection. Its original
Symbol-needle rejection was superseded by the consumer below; changing that
oracle does not rerun the historical source check. Fresh
C5 and development C6 replay, C7/C8 checks and one public C7 assembly/closed entry
are not fresh public C6 verification or migration. Q remains parser-only. Do
not repeat unchanged product construction merely to refresh evidence stamps.

### Inferred Symbol-Set membership: focused feedback

Binary `contains?` consumes the exact carrier above and a Symbol, yielding Bool
without an extent, presence/refinement fact, cardinality or loop-join proof.
Original namespace/name equality applies. Change the existing C7, effects and
typed emitter membership owners; runtime and private/signature predicates stay
unchanged. Remove carrier work formulas and one-short allowance tests; finite
literal inputs retain full member validation.
Preserve validated Never transfers, operand-prefix ordering and legacy-family
dispatch. Reject non-three-key receiver maps before walking keys or accessing
fields; keep malformed three-key/opaque and zero-callback controls too.

Use the existing development runner and three namespaces above with exact vars
`sh08-symbol-set-carrier-contains-development-kernel`,
`body-symbol-set-carrier-contains-development-kernel`,
`current-gravity-symbol-set-carrier-contains-development-kernel` and
`current-gravity-symbol-set-carrier-contains-fixtures-development-kernel`.
Retain finite Symbol and Keyword/Character/String membership, insertion, carrier
storage, source-ID and transfer checks; migrate quota-only assertions with their
enforcing source. Independently reconstruct v2
descriptor/constraint/fact/ledger joins; v1 records cannot claim carrier input.
Ordinary unselected producerless rows remain legal; malformed selected rows do
not. Reuse valid unchanged evidence, not stale policy identities.

For this changed source boundary, separately select
`current-gravity-symbol-set-carrier-contains-authenticated-composition` in SH01.
It checks seed/local/repeated-insertion transport, present/absent and qualified
Symbol queries, pure query effects with operand allocation, and an actual wrong
Keyword-needle typing rejection. Q is parser-only; development C6 replay is not
fresh public C6 verification. This does not execute the motivating compiler
helper or add private/recursive signatures, dynamic loop joins, Set equality,
native execution, authoritative migration or milestone completion.

### Grounded Symbol-Set loops: focused feedback

L5 permits an independently empty persistent Set loop slot to acquire a Symbol
member invariant from independently grounded normal re-entry. Preserve the
actual initializer type and all original transfers; do not equate the empty
initializer with its invariant. Whole-tuple Never transfers cannot ground it.
The implemented cold derivation covers v2 `conj` with an empty seed and a
literal/quoted Symbol through acyclic immutable aliases. Other Set families,
general joins, expected-type grounding and function/private signatures remain
outside this increment. Runtime and protected signature predicates are unchanged.

Edit the existing C7, effects and typed emitter owners. In
`gravity.self-hosting.sh08-loop-recur-type-test`, select
`symbol-set-loop-invariant-development-kernel` and
`symbol-set-loop-effect-invariant-development-kernel`. In the SH01 iteration
namespace, select `current-gravity-symbol-set-loop-invariant-development-kernel`
and `current-gravity-symbol-set-loop-invariant-fixtures-development-kernel`.
Keep affected carrier, insertion, membership and existing loop/function controls.
Validate cold grounding independently in C8/lowering, original target coverage,
normal empty/carrier compatibility, alias paths, exact/one-below work and guarded
quote identities. Pending inference publishes no invariant; existing exact-slot
checks can still reject an independently known incompatible re-entry.

The separate SH01 selector
`current-gravity-symbol-set-loop-invariant-authenticated-composition` exercises
one G alias-loop returning true and one G typing rejection; Q is parser-only.
Its original run passed the positive composition and rejected the negative, but
finished 67P/1F because the negative oracle expected an unestablished carrier
instead of the actual empty initializer type. Preserve that failed run. The
oracle correction is not a repeated passing source run. Reuse those unchanged
observations and validate pending fallback with the direct kernel, rather than
reconstructing the same expensive products to refresh a stamp. This development
session/replay is not fresh public C6 verification, native execution, whole-helper
support, authoritative migration or milestone completion.

### Evaluated empty Set loop initializers

The current JVM development route also grounds the same invariant from an
unquoted `#{}` initializer. C6 already emits an evaluated Set constructor and
C7 already gives it the literal empty-Set type. The C7, C8 and lowering cold
checks now independently join that node to its empty source value, exact
three-field attributes, zero child/resolved-binding vectors and left-to-right
empty execution order. Retain constructor allocation and Set IR; do not substitute
an inert quote or rewrite the initializer type. This adds no nonempty seed,
member refinement, dynamic guard rule, signature or runtime support.

In the SH08 loop namespace, run
`unquoted-empty-symbol-set-loop-development-kernel` and
`unquoted-empty-symbol-set-loop-effects-development-kernel`. In the SH01
iteration namespace, run
`current-gravity-unquoted-empty-symbol-set-loop-development-kernel` and
`current-gravity-unquoted-empty-symbol-set-loop-fixtures-development-kernel`.
Keep the affected quoted-loop, slot, backedge, function and Set-lowering controls.
The paired fixtures are `accepted/unquoted-empty-symbol-set-loop` and
`rejected/nonempty-keyword-symbol-set-loop` under SH08; Q is parser-only.

The separate changed-source composition is:

```bash
clojure -J-Xmx2g -M:dev-test --fail-fast \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-unquoted-empty-symbol-set-loop-authenticated-composition
```

It checks one G loop returning true and one nonempty Keyword-Set typing
rejection. This new source run does not relabel or repeat the earlier failed
quoted-source oracle run. Fresh development C6 sessions/replay remain separate
from public fresh C6 verification, native execution and migration evidence.

### Vector-first Symbol grounding for loop slots

The grounded Symbol-Set loop route also accepts a member obtained by `first`
from the original evaluated nonempty Vector constructor. Its stored head must
be independently a literal/quoted Symbol, including acyclic immutable aliases;
member aliases may lead to that call. Validate the complete positional Vector
type and every stored child, source identity and evaluation edge. Empty,
quoted, aliased or function-returned Vector receivers and nested-call head
grounding do not establish this witness. Preserve the actual empty Set
initializer, every normal transfer, whole-tuple Never behavior, constructor
allocation and the original Vector-first runtime guards.

Use the existing SH08 loop namespace's
`vector-first-symbol-loop-witness-development-kernel`, controls, effects,
reached-metadata, final-warm-descriptor, keyword-work and cold-solver selectors.
The corresponding SH01 iteration kernels cover typed lowering and paired
fixture parsing. In SH09, select
`body-private-evidence-keyword-work-development-kernel` and
`body-vector-first-symbol-loop-cumulative-work-development-kernel`, retaining
the affected private-entry/first/map checks and original loop/backedge controls.
`vector-first-empty-diagnostic-oracle-development-kernel` separately checks
the original annotation renderer and actual arity-mismatch control.

Reached source projections retain all consumed fields; unused C6 metadata is
not a premise. Compare the complete warm descriptor at the final boundary.
Preserve the useful removal of discarded Keyword formatting. Delete the C8
Map/Vector walk's scalar, depth, pending-child, text and row/equality accounting
and its work ceiling rather than using it to refuse semantically valid DATA.

The separate genuine SH01 selector is
`current-gravity-vector-first-symbol-loop-witness-authenticated-composition`.
Its retained `2677614a` invocation completed all positive execution and negative
typing observations with 80 passes and one wrong diagnostic-oracle assertion.
The empty-Vector annotation has a present, nil constraint ID, unlike a type
mismatch. The test-only correction and isolated 32-assertion kernel do not
relabel that failed receipt or replay its expensive source products. See the
backlog's qualified evidence record. G source execution and Q parser parity
remain distinct; development C6 session/replay is not public fresh C6 or
authoritative migration evidence.

### Inferred String-Set carriers: private value feedback

The closed inference carrier
`{:kind :gravity.type/set :shape :homogeneous :element-type :gravity.type/string}`
records only the String member domain. Independently typed String-Set insertion
can establish it; further insertion preserves it and membership can consume it.
It does not record an extent, presence fact, literal origin or comparison
descriptor. Standalone comparison uses the separate checked rule below.
It introduces no `(Set String)` annotation or declared-recursion expansion.

Keep C7, C8 and typed lowering's inference-only validators consistent. Normal
private entries must independently establish the complete parameter vector;
every use, escape, self-call and return witness retains its existing source,
visibility and cold-inference checks. A String argument or expected Bool
return cannot specialize an otherwise untyped empty Set. Do not widen the
declared-self predicates or the List-only parameter evidence used by Vector
append. Runtime membership and insertion still check complete actual domains
without size/work quotas; the carrier is not a substitute for semantic validation.

Start with the existing three owner kernels and paired parser check:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-set-carrier-private-values-development-kernel \
  --exact body-string-set-carrier-private-values-development-kernel \
  --exact current-gravity-string-set-carrier-private-values-development-kernel \
  --exact current-gravity-string-set-carrier-fixtures-development-kernel
```

Include affected insertion, membership, count and generic value consumers, plus
the existing private entry/witness/proof/census, declared-self, List and Vector
regressions. Richer result information must not break previously accepted
predicates or List construction. The bounded generic type walker validates the
exact carrier and visits its String leaf; it does not admit arbitrary homogeneous
collection schemas. Remove exact/one-short allowance checks; reject malformed
opaque records before relying on downstream descriptor equality.

On a stable candidate, separately test one real private self-call whose caller
seeds its Set using independently typed insertion, then queries membership.
Pair it with an untyped empty-entry rejection using the existing source selector:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-carrier-authenticated-composition
```

This constructs fresh development products for the changed composition; it is
not an authority stamp refresh. Reuse accepted unchanged focused results.
Normalized helper tests and Q
parser checks are not that source composition, current-closure admission or
whole-B14 execution. B14's empty entry, heterogeneous record ingress and indexed
recursion remain separate implementation boundaries.

### Standalone inferred String-Set comparison

The carrier above can participate in standalone binary `=` and `not=` after
both operand domains are independently established. At least one operand must
be the exact homogeneous String-Set carrier; the other may be the same carrier
or a fully checked finite all-String Set, including empty. Never retains its
ordinary non-invocation behavior; it does not excuse an invalid normal operand.
Bare, unknown, mixed-member and malformed types remain unsupported.

Change the existing C7, C8 and typed emitter owners. The separate carrier
rule retains unknown cardinality and extent, distinct carrier comparison
identities, complete source/fact/constraint joins and original `not=` ownership.
It never invents operand counts. Existing finite Set and Vector domain helpers
remain unchanged; this does not enable String-Set leaves in Vector comparison.
The existing runtime String-Set helper still validates complete actual member
domains and uniqueness; remove worst-case work accounting.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-set-carrier-equality-development-kernel \
  --exact sh08-string-set-carrier-inequality-development-kernel \
  --exact body-string-set-carrier-comparison-development-kernel \
  --exact current-gravity-string-set-carrier-comparison-lowering-development-kernel \
  --exact current-gravity-string-set-carrier-equality-fixtures-development-kernel --fail-fast
```

Select the matching source-ID, transfer-fence and mixed-ledger kernels when
changing those boundaries, plus affected existing finite/atomic/Vector tests.
Direct source-ID guards precede keyed lookups and reject opaque/composite IDs
without hashing, equality or printing callbacks. Generic emitter guards cover
Core lookup IDs only, not legitimate Symbol-keyed name tables or arbitrary
call arities. The C8 graph fixture must use its established SHA-shaped node
identities; Keyword IDs accepted by narrower helper tests are not graph inputs.

These are focused stage checks, not fresh source construction or authoritative
migration. Validate the changed real source composition separately: inferred
private entries and a self-call, finite module-root comparisons, `not=`, and a
genuinely bare Set entry rejection. Keep the module root out of the private
helper's captures. Paired parser checks alone do not establish paired typed
execution, whole-B14 support, another target or self-hosting.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-carrier-equality-authenticated-composition --fail-fast
```

This selects one G positive and one G typing negative. Fresh development C6
session construction and same-catalog replay are not public fresh C6 admission.
Do not repeat the composition for unchanged code, notes or historical labels.

### Private-entry diagnostic source preservation

For `:private-self-independent-entry-types-required` and
`:private-self-entry-type-conflict`, the owning C7 `sh08-is-entry-values`
rejection retains the actual indexed failing call node. The existing recursive
diagnostic renderer then preserves its Core/syntax IDs, span, binding and
source/generated origins. Do not reconstruct location from spelling, add a
generic formatter fallback or change accepted entry/proof records.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-private-self-entry-diagnostic-source-development-kernel \
  --exact current-gravity-private-self-entry-values-development-kernel \
  --exact current-gravity-private-self-proof-development-kernel --fail-fast
```

These direct original-helper/renderer checks use normalized source-bearing
inputs. Preserve first failing normal-entry selection, Never exclusions,
semantic reasons and legacy source-less formatting. Diagnostic identity
changes when its previously missing source fields become populated. Historical
nil-location results remain historical; this focused repair does not renew
their C5/C6 provenance or require an unchanged source composition to run again.

### Homogeneous List returns: declaration feedback

The explicit `(List T)` return slice is in development. L5 requires the body
type to be established independently and retained alongside return-specific
compatibility evidence. Start declaration and transport changes with these
existing selectors; their names also cover the earlier parameter boundary:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh06-list-parameter-type-projection-development-kernel \
  --exact sh07-b47-list-parameter-reference-projection-development-kernel \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact seed-function-declaration-plan-parity-development-kernel
```

Both the C5 walk and retained C6 mirror must visit the constructor and member
in type positions, preserving scope, occurrence identities and body coordinates.
The temporary header normalizer checks shape, not constructor spelling;
authenticated C6 binding resolution owns constructor legality. These checks do
not establish return typing, effect reconstruction, typed lowering or actual
source composition. Those remain separate required checks for the new slice.

For return compatibility itself, edit the C7 engine and use its direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-homogeneous-list-return-development-kernel \
  --exact sh08-homogeneous-list-parameter-development-kernel \
  --exact sh08-bare-list-parameter-development-kernel
```

The return rule retains the declared outward type separately from the actual
body type. Remove member/type debits across functions and inference rounds;
reject changed source/binding premises. Passing these kernels does
not replace independent C8 reconstruction, typed lowering or source composition.

For C8 or lowering return-context changes, use the matching existing kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-homogeneous-list-return-development-kernel \
  --exact body-homogeneous-list-return-function-joins-development-kernel \
  --exact current-gravity-homogeneous-list-return-consumer-development-kernel \
  --exact current-gravity-homogeneous-list-return-definition-development-kernel
```

These compare independently reconstructed return premises and strict ordered
row censuses, including function maps whose iteration order differs from source
order, and validate complete normalized definition joins. They are not
whole-module or fresh source acceptance. For changes to independent body
inference, also select
`sh08-homogeneous-list-return-branch-independence-development-kernel` and
`sh08-homogeneous-list-return-structural-independence-development-kernel` in
the SH08 namespace. Canonical `if` now supports the independently established
List join described below; other control-flow joins are not widened. The
return annotation must not supply a common type. The affected source
composition must also be reviewed before accepting the new return slice.

Run the affected source composition only for a stable changed candidate:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-homogeneous-list-return-authenticated-composition
```

It constructs the accepted Gravity/QST pair and representative C7/C8 negatives
once each, plus one C5-only rejection. Accepted products are reused across the
runtime cases and hostile checks. This is development integration evidence,
not authoritative migration. Preserve a failed run and diagnose its actual
scope: a test-accessor correction alone does not justify repeating unchanged
expensive source construction. Validate such a correction with the affected
cheap projection check and review the retained actual results.

### Homogeneous List canonical-if joins: focused feedback

For L5's independently established List branch join, edit the C7 type owner.
Do not widen the shared normal join used by other control-flow forms or use a
return annotation to invent a missing element type.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-list-if-join-type-development-kernel \
  --exact sh08-list-if-join-source-development-kernel \
  --exact sh08-list-if-join-inference-development-kernel
```

These normalized checks exercise independent element evidence, actual branch
types, source/binding correspondence and compatibility rows without inference
fuel. They cover both branch orders and preserve ordinary `if` truthiness,
distinct from Bool-only match guards. Passing them does not establish public
C7 artifact integration, independent C8 reconstruction, typed lowering or
source execution. Those checks are separate integration coverage; an existing
return declaration check is not a substitute for them.

For changes to the independent C8 context or typed lowering, select their
existing consumer kernels separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-list-if-join-development-kernel \
  --exact current-gravity-list-if-join-lowering-development-kernel
```

These check ordered compatibility-row census, source-bound reconstruction,
original failure behavior and the existing `if` instruction
with type-consistent guest values. They do not replace affected original-source
composition or establish module migration. Only rerun the relevant kernels
when their owning behavior changes.

For the stable changed candidate, run the affected original-source composition
separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-if-join-authenticated-composition
```

It constructs the accepted Gravity/QST pair and one representative C7 rejection,
then reuses accepted products for public lowering, independent C8 checks,
runtime cases and hostile mutations. A failed mixed regression batch remains
failed even when a targeted correction passes; review the retained passing
checks and rerun only affected regressions instead of rebuilding unchanged
source proofs to obtain a green aggregate. This is development integration,
not authoritative migration or target/self-hosting completion.

### Nil-bearing canonical-if joins: focused feedback

Edit the existing C7 type owner, C8 effect checker and typed emitter for L5's
independently established nil-or-atomic branch join. The current frontier is
Bool, Character, String, Symbol and Keyword. Preserve the actual nil/value,
local inferred return type and source-bound join premise; an expected non-nil
return declaration cannot remove nil. Different nil-bearing atomic families
can both contain nil, so equality compares their actual values rather than
folding the result from their non-nil types.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-nil-if-join-type-development-kernel \
  --exact sh08-nil-if-join-inference-development-kernel \
  --exact sh08-nil-atomic-equality-development-kernel \
  --exact body-nil-if-join-development-kernel \
  --exact body-nil-atomic-equality-development-kernel \
  --exact current-gravity-nil-if-and-equality-development-kernel
```

These normalized checks cover closed type validation, source-bound premises,
ordered mixed List/nil join rows, lowering and runtime value equality.
They are not cold source inference or authoritative migration. The separate
`current-gravity-nil-if-join-fixtures-development-kernel` checks that the paired
fixture preserves W5's actual unannotated `w5-se-canonical-form` helper.
For affected cold source composition, run separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-nil-if-join-authenticated-composition --fail-fast
```

It checks the unannotated helper's local callers and declared-non-nil rejection,
reusing the accepted pair and one negative product for consumer and runtime
observations. It is not a prerequisite for each subsequent unchanged edit.
Ordinary `if` truthiness and Bool-only match guards remain distinct:
nil-or-Bool is not a legal guard type. Numeric/general unions, new annotation syntax and widening
of match, try, loop or recur joins are outside this increment.

### Homogeneous List traversal: focused feedback

The `(List T)` parameter and guarded-head slice supports content-consuming
traversal such as joining a persistent List of Strings. Exercise
type-position transport, parameter headers, C6 declaration/source joins, C7
typing, independent C8 reconstruction and typed lowering directly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact sh06-list-parameter-type-projection-development-kernel \
  --exact sh07-b47-atomic-signature-lowering-development-kernel \
  --exact sh07-b47-list-parameter-reference-projection-development-kernel \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact sh08-homogeneous-list-parameter-development-kernel \
  --exact sh08-homogeneous-list-head-development-kernel \
  --exact body-homogeneous-list-parameter-development-kernel \
  --exact body-homogeneous-list-head-development-kernel \
  --exact current-gravity-homogeneous-list-call-lowering-development-kernel \
  --exact current-gravity-homogeneous-list-head-lowering-development-kernel
```

The temporary adapters preserve compound annotation source and type-position
references; they do not confer constructor or member-type authority. C6 owns
those checks. Parameter-reference transport has two sides:
`semantic_late/151848-sh06-resolution-inputs-traversal.clj` records C5 references
and `semantic_late/154235-sh07-core-resolution-fn.clj` projects them into the C6
request (both under `bootstrap/clojure/src/gravity/bootstrap_impl/`). Preserve
their reference order, type positions and source coordinates; do not relax the
request's bijection checks when adding an annotation shape.
Compound return annotations remain outside this implementation
increment. Empty Lists satisfy the parameter declaration, but `first` can have
the non-Nil member type only under an established nonempty fact. This increment
reconstructs that fact in the else branch of the nearest canonical
`if (empty? same-immutable-binding)`. A different binding, a tail, mutation or
an unrelated nearer condition cannot borrow it. `rest` preserves `(List T)`,
not nonemptiness; function recur retains exact slot types. Supported `T` is an
existing non-Nil atomic declaration, not nested applications or general
generics. Homogeneous-to-bare List adaptation remains unsupported.

Run the affected source-to-runtime composition once for a stable changed
candidate, rather than replaying old source proofs after every component edit:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-homogeneous-list-authenticated-composition --fail-fast
```

The paired accepted source joins empty, singleton and two-element String Lists
to `""`, `"alpha"` and `"alphabeta"`. The changed-candidate composition passed
257 assertions, including actual execution, retained head/throw identity,
specific C6/C7/C8 rejection cases and fresh affected verification. Selected
execution took 1,211.036 seconds plus 8.853 seconds of namespace loading at
2 GiB. This is a workload observation, not a phase profile or speedup claim.
The historical public emitter facade
may reject changed source against stale admission pins; do not refresh those
pins merely to make development feedback green.

### Typed shrinking-List traversal

L5's explicit bare `List` parameter boundary lets a function consume Lists
without retaining a particular positional length. Change the signature path in
`checked_core.gravity`, then C7, C8 and the typed emitter for call compatibility;
the existing runtime owns the List query and recur execution.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh07-b47-atomic-signature-lowering-development-kernel \
  --exact sh08-bare-list-parameter-development-kernel \
  --exact sh08-list-empty-development-kernel \
  --exact body-bare-list-parameter-development-kernel \
  --exact body-list-query-development-kernel \
  --exact current-gravity-bare-list-call-lowering-development-kernel \
  --exact current-gravity-list-empty-traversal-runtime-development-kernel
```

The actual finite argument type stays in the type table and call facts. A
separate, reconstructed compatibility premise binds the information-forgetting
step to the explicit parameter annotation, argument and call source. It is not
type equality, a runtime conversion or an unannotated inference shortcut.
Schema, ownership, linearity, region and profile obligations are not erased.
Imports, higher-order calls and general subtype/coercion rules are not extended
by this ordinary-local-call rule.

Inside the function, `empty?` returns Bool for an established List and `rest`
preserves its bare List type. Existing function-target `recur` still checks its
slot types exactly. These operations permit a real shrinking traversal without
guessing any element type. Exact bare-List return annotations are separate from
finite-to-bare return adaptation, which is not supplied by the parameter rule.
`first` on a bare List still needs head-type evidence; homogeneous element
annotations, general generics and path-sensitive head refinement remain later
work. Operand effects and original transfers remain visible.

Run the changed real-source traversal composition only after these kernels and
proportionate regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-traversal-authenticated-composition --fail-fast
```

The paired `local-runtime-list-traversal` fixture exercises an annotated tail
function, shrinking recur slots, finite-to-bare argument compatibility and
ordinary exact List calls. Manual runtime plans and normalized type premises do
not establish C5-to-C8 source composition or authoritative module migration.

### Persistent List head and suffix queries

STD3 owns unary `first` and `rest` on persistent Lists. Change C7, C8, the
typed emitter and the existing Gravity runtime; do not add a seed operation
or infer executable syntax authority from the stored values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-query-development-kernel \
  --exact body-list-query-development-kernel \
  --exact current-gravity-list-query-fixtures-development-kernel \
  --exact current-gravity-list-query-lowering-development-kernel \
  --exact current-gravity-list-query-runtime-development-kernel
```

Finite positional List types establish the first element's type, or Nil for
an empty List, and the exact ordered suffix. A known bare List is enough for
`rest` to return a bare List; it need not invent element facts. `first` on a
bare List remains a missing-head-type diagnostic, not an implicit Dynamic
operation or a language-wide ban. Queries use established type evidence,
not constructor, quote or local-return ancestry.

The operand evaluates once before querying. A transfer skips the query,
preserving its original payload and output. Stored payloads are not executed
and the original List is unchanged. The selected development runtime guards
actual Lists and returns the existing immutable tail, which may retain input
storage. Empty input gives nil for `first` and an empty List for `rest`.
The query adds no direct allocation in this representation; allocations and
errors from the operand remain visible. This is not a generic sequence,
profile, ownership, provider or backend claim.

After these kernels, exercise the changed current-source composition once
before accepting its integration:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-query-authenticated-composition --fail-fast
```

Do not replay unchanged constructor or
String compositions. Record actual source results separately from these
normalized kernel checks and from authoritative migration.

### Evaluated persistent List construction

STD3's `list` constructor builds immutable DATA, not executable forms or syntax
authority. Change C7, C8, the typed emitter and the existing Gravity runtime.
Use the direct development kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-construction-development-kernel \
  --exact body-list-construction-development-kernel \
  --exact current-gravity-list-construction-fixtures-development-kernel \
  --exact current-gravity-list-construction-lowering-development-kernel \
  --exact current-gravity-list-construction-runtime-development-kernel
```

The call boundary accepts established, lowerable first-order operands without
the child-count quota.
Known bare collection types need no invented member information. Supplied
positional type records are validated without depth/width/work quotas.
The result records operand types
in order, with evaluated-construction ancestry rather than quoted-data IDs.

Operands evaluate once, left-to-right. A transfer prevents construction and
later execution, without excusing unknown or illegal source operands. The
current conservative constructor policy records latent allocation and normal-
invocation direct allocation, even for an empty List; placement, provider and
ownership remain unresolved by that policy. Source allowances still apply.
The guest returns a real List, preserving nesting, duplicates, false and nil.
Its shared argument collector uses a loop rather than host-stack recursion.

Run the affected source composition separately after the kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-construction-authenticated-composition --fail-fast
```

The cold composition must infer List returns from real bodies and local calls,
not supplied result types or new annotation syntax. It checks inert form DATA
containing a computed String, genuine control transfers and representative
source rejections. Do not replay the unchanged String composition. List
queries have a separate focused workflow above; generic sequences,
syntax-object generation, arbitrary recursive
traversal, imported List signatures and full profile/provider/backend legality
remain separate frontiers.

### Binary String concatenation

STD4 owns the binary String overload of the compatibility spelling `str`.
Change C7, C8 and the typed emitter for its applied type, allocation and
lowering. The existing runtime's `str` route does not establish typing or
permission to format arbitrary values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-concat-development-kernel \
  --exact body-string-concat-development-kernel \
  --exact current-gravity-string-concat-lowering-development-kernel \
  --exact current-gravity-string-concat-existing-runtime-development-kernel
```

Both normally returning operands must have established String types. They
evaluate once, left-to-right; a transfer prevents invocation and preserves
operand effects. The applied overload retains latent `:memory/allocate`, while
direct allocation occurs only on normal invocation, including empty Strings.
C8 independently reconstructs these facts and checks the source allowance;
the generic compatibility binding's empty effect row is not overload purity.
Concatenation preserves exact scalar sequences without normalization, coercion
or general formatting. Variadic calls, other overloads, library exports and
provider/profile legality remain separate frontiers.

For the affected source composition, run separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-concat-authenticated-composition --fail-fast
```

It assembles each accepted extension once and checks nested calls, eager Map
fallback allocation, actual transfers and representative source rejections.
Public verification remains fresh for every selected operation. The negative
fixture siblings are parity checks, not duplicate source proofs. Do not replay
the unchanged Map lookup or earlier compositions as a prerequisite.

### Map lookup with an explicit default

STD3 defines `(get map key default)` as an eager ordinary call. A present value
is returned even when it is false or nil; only an absent key selects the
default. Map, key and default evaluate once in that order, including the
default on a hit. A transfer in any operand prevents invocation.

Edit the existing C7 engine, effect owner, typed emitter and Gravity runtime:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-get-development-kernel \
  --exact body-map-get-development-kernel \
  --exact current-gravity-map-get-lowering-development-kernel \
  --exact current-gravity-map-get-runtime-development-kernel --fail-fast
```

The finite normal-returning route requires an independently established Map
member-type vector, a dynamic String or Keyword key and an established default
type T. Every value potentially matching that key family must have exactly T;
unrelated known scalar key families may have other value types. Empty Maps use
T. This is a type join, not a literal-constructor or static-presence proof.
Unknown or malformed member keys cannot be discarded as unrelated.

With a Never operand, no lookup returns normally and no value/default join is
invented. A known bare Map is then sufficient, while the other normally
returning operands must still have legal, established domains. Supplied
structured-type evidence retains its bounds and integrity checks. The query
adds no allocation or effect; all operand allocation and errors remain visible,
including the eagerly evaluated default.

These normalized checks do not authenticate source products. Before accepting
the changed composition, separately exercise actual source, local Map returns,
eager defaults, meaningful rejections and fresh public reconstruction. The
affected source selector is:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-get-authenticated-composition --fail-fast
```

It assembles all functions once per accepted extension, retaining fresh public
verification, rather than also compiling the same positive function separately.
Representative real source rejections complement the complete focused type
matrix; this is not a request to replay the preceding membership composition.
The result type does not confer selected deep-shape ancestry. General stored-key
families, first-class calls,
full profile/provider support and authoritative migration remain separate
frontiers rather than new language prohibitions. The distinct proven-present
two-argument route below can select a type from a heterogeneous finite Map.

### Map lookup with established presence

For `(get map :key)`, change the existing C7 Map-lookup owner, effect checker
and typed emitter. Normal invocation needs independently established finite
Map shape, the actual literal Keyword, its selected coordinate and stored type;
a member-type family does not establish key presence. Use the direct kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-present-get-development-kernel \
  --exact body-map-present-get-development-kernel \
  --exact current-gravity-map-present-get-lowering-development-kernel
```

These are normalized development inputs, not fresh source products. Keep the
three-argument lookup's existing descriptor and behavior unchanged. Missing or
unproved presence is a typing rejection, not a fabricated nil/default result.
Stored false and nil remain values. A transferring operand needs no normally
returned value or presence proof, but other returning operands retain their
domain checks. Lower both operands non-tail and once in source order; only a
validated inert literal Keyword may be reordered in the existing lookup IR.
C8 checks its retained compatibility premises, not a duplicate source-shape
solver. Affected source composition remains necessary before accepting the
changed integration; authoritative migration is separate.

For that stable-candidate composition, select:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-present-get-authenticated-composition
```

It constructs the accepted Gravity/QST pair and one real missing-key rejection,
then reuses the accepted products for whole C8, public lowering and runtime
observations. Keep coherent deep source-shape forgeries at the public C7
boundary, and test C8's own selection premises with genuine input shape.
Do not construct another source product for each malformed proof field.

### Private call-entry Map key presence

For an immutable private function parameter, L5 permits entry-specific key
facts when every normally invoking incoming call independently establishes the
key and independently established selected stored type. The original development
subset supported scalar results; this was not a scalar-only L5 requirement.
Edit the same C7, effect and emitter owners, not a new
admission wrapper. Preserve each caller's distinct source, Map shape and key
coordinate; equal member types do not imply equal keys or a common deep shape.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-private-entry-get-development-kernel \
  --exact sh08-private-entry-get-entry-controls-development-kernel \
  --exact sh08-private-entry-get-nonparameter-fallback-development-kernel \
  --exact sh08-private-entry-get-cold-inference-development-kernel \
  --exact body-private-entry-get-development-kernel \
  --exact current-gravity-private-entry-get-development-kernel
```

These normalized checks are development feedback. The
complete binding-use/call census, visibility, nonvacuity, parameter re-entry
and work bounds are compiler obligations, not a prerequisite authority record
for editing the implementation. Public/escaping helpers, unproved keys,
function-parameter recursion and general deep structural inference remain
outside this increment. Recursive callers can supply independent literal
arguments; ordinary nested loops are not function re-entry.

For a stable changed integration, run the accepted Gravity/QST pair and one
real missing-key rejection through the existing source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-entry-span-authenticated-composition --fail-fast
```

It preserves W5's original unannotated span helper, reuses accepted products
for C8/lowering/runtime checks, and does not migrate W5 or admit a compiler
revision. Do not repeat unchanged upstream construction for every mutant or
subsequent documentation edit.

The finite-Vector result extension uses the existing exact positional Vector
type, including fully validated nested finite Map types. Every normal entry
must establish the same selected type; empty Vector is a valid `get` result
but does not satisfy `first`'s nonempty requirement. Bare, unknown-length,
malformed or incompletely established Vector results remain unsupported.
The extension does not establish inner Map keys from member types or create a
common deep parameter shape. Runtime lookup and public interfaces are unchanged.

Run the owning development kernels for changes to this extension:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-private-entry-vector-get-development-kernel \
  --exact sh08-private-entry-vector-get-rejection-development-kernel \
  --exact sh08-private-entry-vector-get-scoped-consumer-development-kernel \
  --exact sh08-finite-present-result-compatibility-development-kernel \
  --exact body-private-entry-vector-get-development-kernel \
  --exact body-vector-index-get-map-context-compatibility-development-kernel \
  --exact current-gravity-private-entry-vector-get-development-kernel --fail-fast
```

These check recursive validation before comparison, original failure behavior
and invocation-local reuse,
coherent forgeries and existing Map/Vector lookup routes. Internal semantic context
is freshly derived by the public C8 build and verification path, not an input
authority or persisted success cache. A private-leaf test must preserve that
precondition when mutating public facts.

For affected source integration, select
`current-gravity-private-entry-vector-get-authenticated-composition` in the
SH01 namespace; its `*-fixtures-development-kernel` companion provides cheap
G/Q parser feedback. The composition executes the current Gravity runtime.
The seed does not support emitted `:keyword-map-lookup`, so these entries have
no same-emitted-IR seed comparisons. Returned Map observations do not establish
Gravity Map equality. This is not whole-C17 execution or authoritative migration.

### Private entry: get, first, then an inner literal key

The C7, C8 and typed-emitter owners above also implement the narrow composed
query `(get (first (get package :signatures)) :algorithm)`, including two
immutable local bindings for the intermediate values. Each actual normal
private entry must independently establish the outer key, a nonempty finite
Vector, and the inner Map key. Member types alone do not prove key presence.
The selected leaf is Nil, Bool, Integer, Keyword, String or Symbol; Character,
arbitrary deep paths, dynamic Vector indices and public record ingress are not
established by this increment. A quoted scalar embedded inside evaluated
containers is also outside this route; Symbol controls use a wholly quoted
container, not that mixed spelling. Runtime instructions and public APIs are unchanged.

Use these direct kernels for the affected owners:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-private-first-entry-composed-development-kernel \
  --exact sh08-private-first-entry-provenance-development-kernel \
  --exact sh08-private-first-entry-local-products-development-kernel \
  --exact sh08-private-first-entry-local-all-arguments-development-kernel \
  --exact body-private-first-entry-composed-development-kernel \
  --exact body-private-first-entry-selector-guards-development-kernel \
  --exact body-private-first-entry-reference-values-development-kernel \
  --exact current-gravity-private-first-entry-session-wiring-development-kernel \
  --exact current-gravity-private-first-entry-composed-development-kernel --fail-fast
```

For changed source composition, use the SH01 namespace's
`current-gravity-private-first-entry-authenticated-composition`; its
`current-gravity-private-first-entry-fixtures-development-kernel` companion
checks paired G/Q bytes and parsing cheaply. The source case is deliberately
two functions and four calls, not the entire C17 helper. Q typed products and
seed execution of emitted `:keyword-map-lookup` remain outside this check.

The public C8 path reconstructs its internal context from current facts once.
It does not accept a supplied paid context as authority. Within that invocation,
checked parent references can be reused, but original parent source/type/census
checks, the demanded C6/C2 source spine and downstream carriers remain checked.
Quoted Map selection follows authenticated keys, not host enumeration order.
The new dependency projection is not a copy of the whole old parent artifact.
Remove representation-node/scalar work counts and their ceiling. Useful
performance fixes may remove repeated walks or discarded wrappers without
retaining an accounting charge. Source provenance retains C6's actual
generated-origin Vector, including an empty Vector for ordinary forms; it is
not erased or converted to a different representation. This is development
validation, not migration.

C7 may reuse constructed source DATA between rounds of one fresh inference
invocation. This is not reusable acceptance: the table starts empty, is not a
caller input or returned artifact, and does not cross builder or verifier calls.
Fresh parent derivation and checks of the current semantic type dependencies
still run. Public verification reconstructs its expected result independently.
Every dynamic value is recursively walked, and each of eight carriers
is independently checked. A cheap diagnostic over retained development inputs can
identify repeated construction before another source run, but reconstructed
tables and isolated constructor success do not establish source convergence.

Every incoming argument's current presence/value and consulted prior fallback
remain dependencies, including nonselected and noninvoked arguments. The private
reuse carrier need not also duplicate those same type trees in its entry-ID
rows. Likewise, removing C7's discarded temporary ancestry view does not remove
the demanded source projection. Neither correction establishes the larger
unchanged C17 helper's semantic composition; that work is tracked separately
in the backlog, not used as an ordinary-edit admission gate.

### Module-local literal initialization and reads

Edit the typed emitter and Gravity runtime for this boundary, not the seed's
literal-data index. A closed execution initializes supported module-local
literal definitions once, in source root order, before entry execution. Reads
use that instance's binding-ID pool through calls and `recur`; direct expression
evaluation does not implicitly initialize a module or accept a caller's pool.
Unquoted collection literals remain evaluated constructors, not quoted DATA.

Direct root Set initializers additionally support distinct homogeneous literal
Keywords or Strings, including an empty Set. The existing emitter closure and runtime census/type
join own this extension; nested evaluated Sets, mixed or computed members and other
member families remain outside it. Preflight validates exact IR, the full homogeneous
domain and uniqueness without constructing the guest Set. Remove logical
reservations while retaining the type-member check. All initializers and
function namespace reads must validate before any initialization occurs. Only
the existing evaluator then constructs each binding once into the instance
pool; repeated reads do not reconstruct it.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-literal-runtime-development-kernel \
  --exact current-gravity-module-literal-emitter-development-kernel
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-keyword-set-emitter-development-kernel \
  --exact current-gravity-module-keyword-set-runtime-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-string-set-emitter-development-kernel \
  --exact current-gravity-module-string-set-runtime-development-kernel --fail-fast
```

For a stable changed integration, select
`current-gravity-module-literal-modes-authenticated-composition` separately in
the same namespace. It preserves W5's literal definition and getter, checks C8
legality before execution, and reuses accepted products for malformed-input
checks. Initialization allocation belongs to the original constructor; reads
do not add allocation. Initializer calls, references/aliases, mutation,
imported initialization and other arbitrary initializer code remain outside
this literal-only implementation. A derived
constructor-occurrence count is not an allocation-event measurement. The
fixture's explicit allocation allowance does not migrate the full W5 module,
whose current empty effect declaration remains a separate integration issue.

For changed Keyword-Set initialization, select the paired parser and its tiny
accepted/lowering-rejected composition separately instead of replaying the
unchanged generic module source products:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-keyword-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-keyword-set-authenticated-composition --fail-fast
```

For the changed String family, use the corresponding tiny B14-definition
fixture, without replaying the unchanged Keyword source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-string-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-string-set-authenticated-composition --fail-fast
```

Constructor-function call tracing and validated constructor occurrences are
not physical allocation-event measurements. Q remains parser-only; this does
not establish whole-W5 legality, other targets or authoritative migration.
The String extension preserves values without formatting or normalization.
Remove the aggregate initializer allowance and prepaid member-type reads.
The type join validates shape and
cardinality; it relies on the census having validated the same immutable IR,
not on an independently authenticated caller-supplied type predicate.

### Map key membership

STD3's direct Map `contains?` query distinguishes key presence from value
truthiness. A present entry is present even when its value is false or nil.
This development slice accepts independently established Map types and dynamic
String or Keyword keys; it does not require a literal constructor, a known key
set or a deep value-shape proof.

Edit the existing C7 engine, effect owner, typed emitter and Gravity runtime.
Use the existing focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-contains-development-kernel \
  --exact body-map-contains-development-kernel \
  --exact current-gravity-map-contains-lowering-development-kernel \
  --exact current-gravity-map-contains-runtime-development-kernel --fail-fast
```

Map and key evaluate once, left-to-right, in non-tail positions. Never transfers
before the lookup. The query adds no allocation, effect or capability; operand
effects and errors remain visible. String and Keyword keys compare by value,
without coercion or Unicode normalization. Stored values remain uninspected.
Runtime instructions retain the existing closed binary builtin-call shape;
Map and key-domain checks precede lookup.

These normalized kernels provide development feedback, not authenticated source
evidence. Before accepting a changed composition, separately exercise real
paired source through current typing, effects, lowering and execution, with
fresh public reconstruction and meaningful altered-fact rejections:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-contains-authenticated-composition --fail-fast
```

Do not replay unchanged earlier source proofs for this query. Other key domains,
vector/set membership, presence-based type narrowing, first-class invocation,
full collection/profile support and authoritative migration remain separate
work, not language-level prohibitions inferred from this finite slice.

### Unary truthiness complement

STD2's unary `not` complements L2 truthiness: false and nil produce true; other
established normally returning values produce false. This is not equality's
six-family restriction or an implicit conversion in match guards. L7 guards
still require Bool; an explicit `not` supplies a Bool result.

Edit the existing C7 engine, effect owner and typed emitter. The existing
runtime conditional already implements the needed evaluation semantics:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-not-development-kernel \
  --exact body-not-development-kernel \
  --exact current-gravity-not-lowering-development-kernel \
  --exact current-gravity-not-existing-if-runtime-development-kernel --fail-fast
```

The operand is evaluated once, in non-tail position. Lowering uses the existing
conditional with literal false/true branches, not a new runtime instruction or
seed primitive. Never transfers before either branch. Operand allocation and
errors remain visible; `not` adds none. Empty text and collections, zero and
Characters are truthy. Their existing unsupported source or lowering boundaries
remain separate from `not`; they are not new truthiness prohibitions.

Keep the exact binding, unary call/source/order, operand type and Bool/Never
result joins and fresh type/effect reconstruction, without cumulative work limits.
These kernels are normalized development feedback. Real source composition,
including Bool match guards and rejected non-tail recur, remains a separate
integration check before accepting the executable slice:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-not-authenticated-composition --fail-fast
```

The paired source uses `(not (= tag :rejected))` in a Bool guard and retains
raw non-Bool guard rejection. For local-call oracles, use the canonical direct
same-module call guidance below. First-class `not`, other predicates, profile/provider
completion and authoritative migration are not established by these checks.

### Proven vector indexing

The same C7 engine, effect owner, typed emitter and runtime own the bounded
two-argument `(nth vector index)` implementation. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-nth-development-kernel \
  --exact sh08-vector-nth-selected-shape-development-kernel \
  --exact sh08-vector-nth-selected-coordinate-development-kernel \
  --exact body-vector-nth-development-kernel \
  --exact current-gravity-vector-nth-lowering-development-kernel \
  --exact current-gravity-vector-nth-runtime-development-kernel
```

Normal selection requires an established positional vector type and an Integer
literal satisfying `0 <= index < length`. C7 selects the corresponding actual
element type, including from heterogeneous vectors. A Nil element is a valid
value, not missing type evidence or an out-of-bounds fallback. Scalar selection
does not require structural-pattern analysis of unrelated members. A later
lookup or pattern consumer requests the selected value's deeper ancestry only
when it needs that evidence. Structural limits on the selected value do not
justify requiring unrelated vector members to be valid patterns. Preserve the
vector/index/source and binding/call joins while projecting the selected path.

Transferring operands produce Never without an executed bounds proof or a
selected value. Other non-Never operands still require their supported type
and index facts. Operands execute once, left to right, until control transfers;
the query preserves selected value identity and adds no collection allocation.
Operand allocation and error effects remain visible.

The bounds fact is not authorization to erase checks. The development runtime
retains vector, integer and range guards before access. Static invalid bounds
use the owning safety diagnostic; malformed development instructions use the
existing contained rejection. That transport rejection does not define a
valid-source dynamic-index failure API. Dynamic homogeneous indexing still
needs an implemented bounds-failure contract; heterogeneous dynamic indexing
also needs a result-typing rule. Neither host exceptions, nil fallback nor an
unimplemented Result path may silently fill those gaps.

Validate the affected real source-to-type/effect/lowering/runtime composition
when the bounds/type interface or emitted instruction changes. Keep that
costlier integration separate from the normalized edit-loop checks above and
from authoritative migration:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-nth-authenticated-composition \
  --timing-receipt /tmp/sh01-vector-nth-composition.edn
```

This finite operation does not establish full
collection, profile, provider, target or self-hosting support.

### Bounded counter successor

The same C7, effect, emitter and runtime owners implement the finite
proof-required Integer `inc` rule. Use the existing direct tests:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-bounded-successor-development-kernel \
  --exact sh08-bounded-successor-multiple-backedges-development-kernel \
  --exact sh08-bounded-successor-extent-bound-development-kernel \
  --exact sh08-bounded-successor-guard-relations-development-kernel \
  --exact body-bounded-successor-development-kernel \
  --exact current-gravity-bounded-successor-lowering-development-kernel \
  --exact current-gravity-bounded-successor-runtime-development-kernel \
  --exact current-gravity-bounded-successor-fixtures-development-kernel
```

The supported source relation is a zero-initialized immutable Integer counter,
a dominating strict `<` comparison against runtime `count` of the same
immutable vector, and a unit successor on every incoming backedge. C7 checks
the original target, slot, source, guard, count and complete transfer census.
The vector extent `N` is its actual established count, not an admission quota.
The invariant `0 <= i <= N` and true-edge fact `i < N` prove the successor
remains within the finite range. A zero-length vector has an unsatisfiable
increment edge and a normal zero-iteration exit; this does not manufacture a
Never result or an executed increment.

Integer typing remains separate from the numeric-mode and range records. It
does not silently select I64 or arbitrary-precision arithmetic. Count still
executes in the original condition; no folding, hoisting, termination claim or
check elision follows. The runtime retains integer, current-counter and range
guards before addition. Hostile development-IR rejection does not define a
source overflow-error API. The successor adds no allocation or error effect;
operand effects and control transfer remain visible.

Changes to this numeric/control relation or its emitted instruction require
the affected source-to-type/effect/lowering/runtime composition, separately
from these normalized kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-bounded-successor-authenticated-composition \
  --timing-receipt /tmp/sh01-bounded-successor-composition.edn
```

General arithmetic, arbitrary starts or steps,
unproved extents, fixed-width checked errors, BigInt support, target admission
and authoritative migration remain separate work. This rule is a supported
finite analysis domain, not a prohibition on other documented numeric forms.

### Guarded loop vector indexing

The C7 extension to proven vector indexing consumes the existing bounded
counter invariant. It does not run a second loop solver. Start with the
current producer checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-loop-nth-development-kernel \
  --exact sh08-vector-loop-nth-cold-inference-development-kernel \
  --exact sh08-vector-loop-nth-origin-scope-development-kernel \
  --exact sh08-vector-loop-nth-selected-shape-development-kernel
```

These normalized checks cover a nonempty homogeneous established vector,
its exact immutable origin, the original Integer loop slot, and access beneath
the strict count guard's true edge. The element result is its established
homogeneous type. The successor's result may equal the extent; that does not
make `(nth values (inc i))` safe. Access needs the separate `0 <= i < N` fact.

The cold-inference check starts without a completed successor descriptor and
tests both recur/throw branch orders and current facts overriding prior-round
facts. This is owning type-inference feedback, not a real-source composition
or authoritative proof. For effect reconstruction, lowering and paired fixture
syntax, use the affected consumer kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact local-throw-call-effect-equations-development-kernel \
  --exact body-vector-loop-nth-development-kernel \
  --exact current-gravity-vector-loop-nth-lowering-development-kernel \
  --exact current-gravity-vector-loop-nth-fixtures-development-kernel
```

These checks include exact source/call/edge joins, malformed bounds evidence
and operand effects; migrate their remaining work-limit assertions with the
enforcing source while retaining index and extent legality. The local
throwing-call kernel also exercises the actual loop-effect derivation and
throwing-call verifier together;
checked loop/recur nodes must not be excluded by a stale operation whitelist.
Fixture parsing alone does not establish rejection or execution.
The existing runtime evaluates index expressions and retains vector, integer
and bounds guards; no runtime change or check elision is implied here.

Changes to the producer/consumer relation need its affected composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-nth-authenticated-composition \
  --timing-receipt /tmp/sh01-vector-loop-nth-composition.edn
```

This is a separate, expensive source-to-runtime integration check, not the
default edit-to-test command above. Reuse its observed result for unchanged
source/test/fixture bytes; do not repeat it just to repair a reporting label or
obtain another green aggregate. Failed and partial results stay accurately
recorded. No authoritative migration or self-hosting claim follows from it.

The retained full composition completed its type/lowering/runtime checks but
failed C8's throwing-call compatibility boundary. After changing that consumer
alone, select its affected single-source test instead of repeating those
unchanged stages:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-vector-loop-nth-throw-control-authenticated-composition \
  --timing-receipt /tmp/sh09-vector-loop-nth-throw-control-composition.edn
```

It constructs one fresh unchanged `.gravity` input through the existing type
helper, then checks the current C8 graph, original loop/effect equations,
String propagation and catch residual, and coherently altered graph rejection.
This affected test passed 24 assertions after the whitelist correction; the
earlier failed aggregate remains failed. Its one-source C8 result complements
the unchanged source/type/lowering/runtime evidence rather than relabeling it.

Empty vectors without an element type, heterogeneous result joins, arbitrary
dynamic indices, deep selected-shape propagation and dynamic-bounds failure
APIs remain separate work. These implementation limits are not language bans.

### Guarded loop Vector get

The same existing C7, C8 and emitter owners have a distinct two-operand `get`
variant for the bounded loop domain above. It needs a nonempty homogeneous
Vector, the original Integer counter slot, the same immutable Vector origin as
the count guard, complete backedge evidence and access below the strict true
edge. It does not infer bounds from a member type or from an equal-shaped but
different Vector. An alias may preserve an origin; a new constructor does not.

Use the direct component checks while editing:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-loop-get-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-vector-loop-get-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-get-lowering-development-kernel --fail-fast
```

The source operation, compatibility binding, descriptor, call fact and ledger
row remain `get`-specific. Only after reconstructing those facts and the bounds
proof does lowering use the existing checked `nth` development instruction,
with Vector-then-index evaluation. Runtime guards remain; there is no check
elision or new runtime path. C8 checks the complete ordered `nth`/Vector-`get`
ledger census, including missing, duplicate and orphan rows. The local binary
lowering path preserves the original operand prefix and semantic failure.
Remove its logical reservation and outer zero-budget rejection.

Map lookup, explicit defaults and the prior Never/Unknown routing remain
separate. This subset does not grant general Vector `get`, literal-only access
outside this proof, arbitrary dynamic indices, heterogeneous results or an
out-of-range/absent-result policy. The paired `local-runtime-vector-loop-get`
fixtures and wrong-origin, heterogeneous, nonstrict and outside-loop negatives
belong to the affected source composition, not the edit/test inner loop.
Actual source outcomes are recorded in the backlog; normalized component
checks alone do not establish source execution or authoritative migration.

When this producer/consumer behavior changes, run the affected composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-get-authenticated-composition --fail-fast
```

This is not the default command for every edit. It checks the paired source,
fresh verification, typed lowering, effects and function-table execution.
Do not repeat an unchanged composition to repair a reporting label or refresh
migration status.

### Binary unit steps in bounded loops

For exact `(+ original-counter 1)` inside the bounded strict-loop domain, edit
the existing C7 engine, `gravity/effects.gravity`, P15 emitter and
`gravity/runtime.gravity`. Start with the owning component, not a corpus proof:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-bounded-unit-addition-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-bounded-unit-addition-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-bounded-unit-addition-lowering-development-kernel \
  --exact current-gravity-bounded-unit-addition-runtime-development-kernel --fail-fast
```

Use `sh08-bounded-unit-addition-cold-two-loop-development-kernel` for the cold
mixed-step/get/lexical-type dependency, and
`sh08-bounded-unit-access-current-lexical-development-kernel` for exact immutable
initializer propagation. Budget guards are retired requirements. Neither substitutes for source
composition. The cold source-prefix selector is a more expensive diagnostic
tool when real frontend shape matters; it is not the default inner loop.

The complete incoming census reconstructs each actual inc or binary unit step;
all-inc v1 and mixed v2 evidence are not interchangeable. Both binary operands
remain evaluated in order, runtime guards remain, and Never does not establish
a numeric invariant. This is not general addition or ordinary-call induction.
When these producer/consumer behaviors change, the existing affected integration
selector is `current-gravity-bounded-unit-addition-authenticated-composition`.
Its recorded result and limitations are in the backlog. Do not replay it for
unchanged source or to refresh authoritative migration status.

### Direct integral ordering

The C7 engine, `gravity/effects.gravity` and existing emitter own direct binary
`<`, `<=`, `>` and `>=` typing, effect reconstruction and lowering. The runtime
already evaluates these operations. Start with the current-source kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-integral-ordering-development-kernel \
  --exact body-integral-ordering-development-kernel \
  --exact current-gravity-integral-ordering-lowering-development-kernel \
  --exact current-gravity-integral-ordering-runtime-development-kernel \
  --exact current-gravity-integral-ordering-fixtures-development-kernel
```

This finite rule requires exactly two operands with the same established
integral type: Integer, I8/I16/I32/I64 or U8/U16/U32/U64. Integer literals are
not implicitly converted to fixed-width operands. Normally invoked operations
return Bool. Never operands preserve source-order control transfer; other
operands still require legality checks even when invocation cannot occur.

An applied intrinsic descriptor belongs to one authenticated operator
occurrence, not a fabricated function or generally callable binding type.
Keep the existing C6 call/edge shapes and function census. Fresh C7 verification
reconstructs the descriptor, numeric-mode constraint and call fact; effects
and lowering independently join the actual binding, source, order and types.
The proof-required safety rule is finite same-integral total ordering without
conversion, overflow or rounding. Local shadowed functions retain ordinary
dispatch. Numeric semantics belongs to `gravity.math`; the existing core
catalog remains compatibility resolution, not a newly implemented math import.
The actual source composition uses unqualified comparison operators. Current
C5 does not admit `gravity.core/>` as a qualified variable spelling; do not
infer that source support from normalized intrinsic-descriptor tests.

Run one affected paired source composition separately when those facts or
execution interfaces change:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integral-ordering-authenticated-composition --fail-fast
```

It combines ordering, a computed map guard, shadowing and typed catches. Reuse
each fresh product within that test; do not replay unchanged guard or match
proof matrices. Empty direct comparison effects do not establish allocation-
free wrapper execution: the fixture declares allocation, and constructor
allocation inference is a separate outstanding slice. Mixed numeric families,
floating/decimal ordering, conversions, escaped operators and complete
profile/provider, target, standard-library or migration acceptance are not
established by these development checks.

### Canonical direct same-module calls

Edit the existing Gravity emitter and runtime for fixed-arity, acyclic calls
between named functions in one actual module. The lowerer joins canonical C6
call/edge records, resolved binding and definition/function identities, ordered
arguments and C7 call facts/constraints. A qualified current-module spelling
can select the same resolved binding; spelling alone never selects executable
code. A `Never` argument makes the call non-returning without changing the
callee's declared normal return type.

When writing composition oracles, current ordinary C7 local-call facts omit
`:dispatch`. Check the actual canonical caller/callee edge and call-fact joins.
The separate `:direct-local-function` higher-order-call tag is not an ordinary
direct-call classification. This is an artifact-shape distinction, not a
missing executable behavior or a reason to reconstruct upstream proofs.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-direct-call-lowering-development-kernel \
  --exact current-gravity-direct-call-runtime-development-kernel
# Affected source -> current typed call lowering -> Gravity runtime:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-direct-call-authenticated-composition --fail-fast
```

The closed plan supplies its existing function table to the private Gravity
evaluator context. Legacy evaluator/helper entrypoints use an empty table;
public plan execution keeps its existing record shape. Validate the target,
fixed parameter vector and arity before argument effects. Evaluate arguments
once, left-to-right in the caller environment, then bind values in a fresh
callee environment. A thrown or rejected argument prevents later arguments and
invocation. Callee output and control return unchanged to the caller.

An active call path rejects semantically unsupported cycles before argument
evaluation. Remove the 256-entry path quota; this does not prove host-stack fit,
termination or memory use. Shallow target-body checks are not whole-plan validation.
Do not add host lookup, ambient callbacks or host exception translation to
implement these guest calls. Existing cross-plan continuation machinery is
separate and unchanged. Imports, callable parameters, closures, variadics and
recursive components remain later slices, as do complete profile/provider
legality, whole-runtime compilation and authoritative migration.
The supported declarations are those accepted by current C7. Qualified
references work, but qualified declaration names currently fail C7's
simple-binding-name versus definition-name equality before this lowerer.
That is an implementation gap to resolve in C7, not a new L2/L3 prohibition;
changing the call consumer alone would not close it.

The focused batch passed nine tests/592 assertions. The sole affected paired
composition passed one test/214 assertions in about 869 seconds of execution
plus eight seconds of loading. It builds each accepted input once per extension
for thirteen-function assembly and execution, then exercises the current C5
unknown-call and C7 arity, type and nonfunction-call boundaries. Current C7
verification runs once per public lowering operation; input tampering rejects
and every guest execution is observed for zero seed throw transport. The
sixteen-function runtime projection is not whole-module compilation. Counts
overlap and timings describe different workloads, not a speedup comparison.

### Canonical loop and function-target execution

Edit the existing Gravity emitter and runtime for canonical `loop`/`recur`
lowering and execution. Reuse C6's target, sequential binding and transfer
records and C7's slot types and constraint rows; do not add an admission input
or refresh historical migration pins for this work.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-lowering-development-kernel \
  --exact current-gravity-loop-runtime-development-kernel \
  --exact current-gravity-function-recur-lowering-development-kernel \
  --exact current-gravity-function-recur-runtime-development-kernel
# Affected source -> current typed lowering -> Gravity runtime composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-authenticated-composition --fail-fast
```

Initializers run once, sequentially. Recur arguments run left-to-right in the
old environment; only after they all complete are the slots replaced together.
A separate private control position carries the target ID, so ordinary or
thrown data that resembles a recur instruction stays inert. The matching loop
consumes its backedge. Non-tail positions, `try` regions and call boundaries
retain the existing C6 transfer restrictions; a nested loop establishes its
own target. Function-target recur uses the current function's declared parameter
slots, not types guessed from a caller. Its matching activation replaces all
parameters together in a fresh local environment and retains the same call
path. Function recur is a backedge, not another ordinary call. The separate
declared direct-self path below uses real ordinary activations; it does not
turn those calls into recur or provide tail-call optimization.

The function-target increment passed its six-input affected composition: one
test/276 assertions. Use its existing selector when validating that changed
boundary:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-function-recur-authenticated-composition --fail-fast
```

That selector constructs six source products: the existing annotated function
pair, the new executable function-recur pair, and a representative rejected
slot-type pair. It includes a terminating zero-parameter case, parameter swaps,
shadowing, nested loops, and throws before and after a restart. The selected run
took 660.745 seconds, including product construction and fresh public
verification. That is not a phase-cost or speedup measurement. Do not repeat
this unchanged composition, the old C6/C7 matrix, or migration proofs to debug
these development kernels or correct documentation.

That 276-assertion result belongs to commit `55b7dca9`, before a test-only
construction cleanup. Assembly already lowers every definition, so the revised
selector checks every assembled function and retains one representative
standalone compile per accepted input. It expects 16 fresh public C7
verifications instead of 36; every retained public call still verifies freshly.
The six SH01 type-only callers also omit the unused SH09 binding seal. The C8
helper still adds its original seal to the same fresh products. Production and
fixture bytes did not change. The old composition was not replayed merely to
renew evidence for this harness refactor, and no elapsed-time improvement or
new composition pass is claimed for the revised selector.

When changing that helper or test schedule, use the cheap preservation checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact fresh-type-products-construction-development-kernel \
  --exact fresh-products-seal-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact function-recur-composition-work-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
```

The helper checks use mocked control-flow DATA, not accepted compiler evidence.
The schedule check reads the actual test source. Neither is a substitute for
affected composition when production behavior or fixtures change.

One remaining cost candidate is compiler-code plan construction, distinct from
semantic product construction and verification. The existing `00581` typed
lowering operation constructs both current C7 and emitter plans before each
fresh public C7 verification. Thus the Map membership selector's nine public
operations imply eighteen code-plan constructions if that path completes.
`00580` parses and compiles those definitions and canonicalizes the plan for
its identity. This is a static call count, not an observed phase-time share.

At the next already-required changed composition or migration run, bounded
observations around code-plan construction and fresh verification can determine
whether that repetition is material. Do not launch a separate profiling run or
add another test lane to establish this. If measurements justify a change,
consider reuse of immutable executable code plans for identical captured
compiler inputs, with exact source/emitter/toolchain binding and invalidation.
Never reuse semantic products, accepted results or proof authority. Preserve
canonical plan bytes, public results, source-change reconstruction and drift
rejection, and one fresh verifier invocation for every public operation,
including hostile inputs. Product representation redesign remains separate
unless actual measurements identify it as the bottleneck; no speedup is
established by this follow-up proposal.

The focused checks exercise normalized producer-shaped data, including rows
constructed by the current C7 constraint producer. They are not authenticated
source evidence. The separate paired selector constructs each source input
once and reuses its products for assembly, lowering, execution and negative
checks. Run it when this composition changes, not to replay unchanged C6
tail/arity proofs or to repair a test-only assertion. Passing results are scoped
to the actual source and implementation revision tested.

Represented integer values remain generic Integer, not guessed I64 values.
Unelaborated decimal descriptors still reject at this consumer boundary;
that is not a language-level prohibition on decimal loops. Arbitrary recursive
calls, full effect/profile legality, whole-runtime compilation and authoritative
migration remain separate work. The current twenty-two-function runtime
projection and active-target bound do not establish
termination, total cost, memory bounds or host-stack fit.

If changing C7's source-error composition, edit
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` and run
the existing owning kernels before the affected integration check:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact loop-lexical-source-error-development-kernel \
  --exact loop-handler-source-error-development-kernel \
  --exact loop-called-source-error-development-kernel
```

These checks cover lexical error unions, handler-only activation and called
bodies. They do not add control backedges or establish termination. A failure
before lowering must be diagnosed at its owning boundary rather than hidden
by dropping the source case or weakening identity validation.

For the typed lowerer's source-definition join, use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-raw-root-source-development-kernel
```

Public definitions retain raw expanded source values; canonical C6 values may
instead contain exact occurrence-linked numeric descriptors. The lowerer pairs
all verified raw roots with C6 root IDs in source order, checks root coverage
and definition/header coordinates, and preserves full supplied-definition and
function-table equality. Only typed C6 nodes drive emission. The explicit host
decimal presentation helper is not canonical compiler input. Changing this
common join requires an affected real multi-function composition as well as
the focused malformed-root and numeric-diagnostic controls; it does not require
refreshing authoritative migration pins.

The preceding loop-only source-join candidate passed this bounded composition
(one test/128 assertions):

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-source-join-authenticated-composition --fail-fast
```

It selects one multi-function positive, both decimal boundary cases and three
type-rejection cases. Its 586.919-second selected time includes real product
construction and public verification, not merely scheduling. It is not a full
language, target, runtime-module or authoritative migration proof. Do not rerun
this expensive composition for unchanged code or documentation-only edits.

### Current Gravity emitter throw lowering

Edit `bootstrap/gravity/p15_s23/emitter.gravity` for the Gravity-owned
instruction lowerer. Its one-operand `throw` becomes `{:op :throw :value ...}`;
wrong arity rejects before operand compilation. Instruction summaries traverse
the operand. Products containing throws explicitly leave effect inference
pending and legality unchecked, rather than reporting an empty inferred effect.
Ordinary products and original declaration records retain their existing shape.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-emitter-throw-development-kernel
```

This check compiles and executes the actual current Gravity emitter source
through the existing raw three-argument developer interface. It lowers the
unchanged compiler-accepted paired throw fixture and observes normal return and
escaping payload through the temporary seed executor. Reuse the unchanged
fixture's upstream evidence; rerun affected compiler composition only when its
behavior or interface changes. Do not refresh old admission pins to run this
development check. Gravity-owned runtime error transfer, handlers, target error
lowering and authoritative migration remain separate gates.

### Current Gravity runtime throw execution

Edit `bootstrap/gravity/src/gravity/runtime.gravity` for the local closed
evaluator. The namespace is `gravity.runtime`; the older
`bootstrap/gravity/p15_s23/runtime.gravity` retains formatting, historical
catalog DATA, B51 machine helpers and four direct compatibility forwarders.
Its internal result is `[value output control]`, with `:normal`, `:thrown` and
`:rejected` kept separate from payload DATA. Argument, binding, sequence and
conditional evaluation propagate a transfer before doing later work. Ordinary
top-level success retains its existing seven-key record and output order;
escaping errors and rejected instructions do not publish normal completion.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-runtime-throw-development-kernel \
  --exact current-gravity-emitter-throw-development-kernel
```

The runtime check freshly compiles the entire current `gravity.runtime` file,
checks its 22 function names/arities and call closure, and retains its actual
source hash separately from the compatibility file's identity. It no longer
selects or concatenates function forms. No function is spliced into an old plan,
and no admission pin is refreshed.

The affected composition lowers the unchanged accepted paired throw fixture
with the current Gravity emitter and evaluates its returned instruction with
the current Gravity evaluator. The seed's private throw transport must not
implement guest error transfer. The generic seed interpreter and primitive
boundary remain. Old admitted packet validation still excludes throw; these
development checks do not widen it or establish a target exception ABI,
handlers, resource cleanup, complete profile legality or authoritative migration.

### Runtime namespace boundary

`gravity.runtime` owns evaluator traversal, guest activation, typed handler
selection and loop/function-target transfer. It has no module imports. The
compatibility module depends on this leaf, never the reverse. L2/L3 control and
module semantics, C5 origins/privacy, C15 diagnostics, P4/R4 hosted boundaries
and BOOT3's development/migration distinction govern this boundary.

The leaf exports only these existing entrypoints; its other 18 functions are
private:

| Entry point | Arguments | Result |
| --- | --- | --- |
| `p15-s23-runtime-execute-closed-plan` | plan | Normal, thrown or rejected execution record. |
| `p15-s23-runtime-evaluate-closed-instruction` | instruction, environment, output | Internal value/output/control tuple. |
| `p15-s23-runtime-evaluate-in-context` | instruction, environment, output, functions, path | Internal tuple with explicit guest function context. |
| `p15-s23-runtime-activate-function` | function, values, output, functions, path | Internal tuple with fresh activation locals. |

Inputs are development instruction/function DATA, not newly admitted typed
programs. Payloads remain DATA; internal `:recur` control additionally carries
its target identity. Normal public completion preserves the seven-key record.
The evaluator retains `P15S23X002` rejection behavior; module-development
reconstruction/routing failures remain `BOOT2006`. Existing diagnostics and
control representations do not become a target error ABI.

The leaf declares managed allocation. Output is accumulated as a string, not
written to a live stdout provider. Source declarations do not grant allocator,
provider or host authority. The generic Clojure interpreter and primitive
boundary remain. Focused checks compile the actual whole leaf directly;
compatibility source/export/import/routing changes require the separate actual
two-file composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-runtime-extraction-authenticated-composition
```

That composition uses real ordinary C5 root/leaf artifacts and an independent
leaf plan through the existing module interface. Do not merge function tables,
rewrite namespaces or inject its bundle into the stale pinned runtime adapter.
It is development integration, not checked imported-user legality, whole-runtime
conformance, authoritative migration or seed retirement.

The present module consumer does more than these foundational contracts
prescribe: `00580` calls full public C5 verification, including SH05 and envelope
verification, during every public bundle reconstruction. That is an existing
tool implementation choice, not a prerequisite imposed by BOOT3 or TEST2 on
every development invocation. Do not rerun it for unrelated semantic edits.
Verifier entry counts are not replay or allocation counts: existing permitted
proof-transaction object reuse may apply within an invocation.
If this cost blocks a needed integration decision, first distinguish initial
C5 construction, bundle construction, public reconstruction and execution;
then locate replay, hashing or equality cost at the owning boundary. Compare
source-derived semantic reconstruction in this same interface with sharing
immutable products within one invocation. Either approach must still reject
source/export/private/profile/origin/plan/route substitutions and preserve
traceable inputs, canonical identities and stale-authority status. Accepting
rehashed maps or `:passed` fields is not a substitute. No redesign, proof cache,
new runner or additional admission lane is selected here.

### Single-source seed throw execution

The temporary seed lowers one-operand `throw` in the existing three-argument
compiler-artifact plan interface. It evaluates the operand once and transfers
out without a normal value. The private seed transport retains the original
payload as DATA, actual plan source hash and module; it does not throw the
payload object itself or classify arbitrary host errors as source throws.
Nested throws escape before an outer transfer can occur. Ordinary control-key
maps remain DATA, and the existing host-error and recur boundaries remain in
force. Malformed source arity and missing operand instructions reject before
operand work.

The owning seed files are `bootstrap-p15/00575`, `00576`, `00590` and `00592`,
with operand summary traversal in `foundation/00555`. The existing `00580` raw
plan builder marks throw-containing plans as Clojure-seed lowering with pending
effect inference and unchecked legality; source declarations and plan identity
remain present. Unchanged no-throw plans retain their previous shape. The
existing module-source and module-bundle checks reject executable throw in the
four-argument module route, while preserving quoted throw DATA.

Run the focused transfer checks without constructing upstream proof products:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact seed-throw-lowering-and-arity-development-kernel \
  --exact seed-throw-transport-and-nesting-development-kernel \
  --exact seed-throw-evaluation-order-development-kernel \
  --exact seed-throw-host-error-and-module-fences-development-kernel
```

Use `direct-typed-throw-annotated-seed-execution` in the existing
`gravity.self-hosting.sh08-loop-recur-type-test` namespace to execute both
unchanged typed-throw fixtures. It reuses the accepted compiler composition for
those exact bytes, then observes both the ordinary branch and a real escaping
throw. It does not rerun the unchanged compiler proof. This is explicit seed
execution, not Gravity-authored emitter/runtime ownership, imported-error
execution, a public JVM exception ABI, deterministic resource cleanup, complete
profile legality or authoritative migration.

### Imported-call type development

The owning C7 implementation is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
Use its existing `sh08-function-type-core-artifact` and
`sh08-verify-function-type-result` interfaces. Extend the existing authenticated
caller request with actual callee C7 requests, not copied signatures or stored
verification results:

```clojure
(assoc caller-c7-request
  :module-type-inputs
  {:artifact :gravity/sh08-module-type-inputs
   :schema-version 1
   :module-requests [callee-c7-request]})
```

Each callee request must refer to the same real C5 module artifact already
supplied to the caller's resolution step. C7 freshly derives the selected
exported signature and joins its original binding to the projected C6 function.
Imported parameters are immutable premises, not variables specialized by the
caller. Missing typing inputs and incomplete signatures require a C7 annotation
diagnostic; they do not trigger ambient module discovery or dynamic fallback.
The scalar imported-call boundary accepts complete types inferred within the
producer as well as checked annotations. Any annotation that is present must
still agree with the derived signature. An unresolved producer cannot be
completed from caller arguments. The initial paired fixtures cover annotated
I64, Bool and String direct calls in the same `:meta/:jvm` profile/target; the
producer-inference increment is tracked separately in the slice backlog.
Only the existing literal/atomic scalar type catalogs are supported at this
boundary. Composite or function-valued imported signatures, cross-profile
typing, generic specialization and runtime module linking remain separate work,
not language-level prohibitions on those features.

Start with the existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-imported-function-types-development-kernel \
  --exact sh08-imported-inferred-signatures-development-kernel
```

The affected real compositions are
`sh08-imported-function-authenticated-source-composition` for the initial
annotated boundary and
`sh08-imported-inferred-signature-authenticated-source-composition` for
producer-local inference, in the same namespace. Run the one affected by the
change, not both merely to refresh an aggregate. They exercise the ordinary
C5 -> C6 -> C7 path, not authoritative module migration.
The imported result is type-only: declared effects/capabilities are preserved,
body-effect inference and thrown-error analysis remain pending. The legacy C8
type-only adapter rejects it with `:imported-body-effects-pending`; the separate
body-analysis interface below requires the complete original producer requests.
Type checking does not authorize target
lowering, linking, effect execution or runtime behavior.

### Scalar import catalog without Vector tails

`sh08-im-scalar-type?` recognizes the same 19 Keyword type IDs supplied by the
nine literal and fifteen atomic catalog occurrences. It now uses Keyword-family
classification followed by membership in an inert quoted finite Set, replacing
the two Vector-tail loops. Host nil, unknown, lookalike, composite and callable
types still return Bool false; the actual nil type remains accepted. This
does not expand the imported signature boundary or select Vector-rest behavior.

The focused 512 MiB selection in the SH08 function-call namespace is
`sh08-import-scalar-catalog-development-kernel`,
`sh08-import-scalar-catalog-no-vector-tail-development-kernel`,
`sh08-imported-function-types-development-kernel` and
`sh08-imported-inferred-signatures-development-kernel`. The original-delegating
observer covers both unary and generic dispatch: selected Keyword inputs use
one membership query and no Vector tail; non-Keywords do not enter membership.
Catalog mappings, complete producer signatures and identity/declaration rejects
are checked separately from that operation observation.

This repairs an actual compiler-source leaf dependency, not whole-helper source
typing. Its unannotated parameter and the surrounding import helpers retain
their independent typing requirements; `keyword?` grants no general flow
narrowing. These direct checks do not refresh C5/C6/source, public packet,
migration or performance evidence.

### Fixed match-family preimage checks

C7's private `sh08-ft-match-preimage-valid?` compares the complete
`:match-branch-records`, `:match-pattern-records` and
`:match-decision-skeletons` values in that order. Three explicit checks now
replace its fixed Vector-tail loop. Each reads preimage before core, rejects
at the first mismatch and preserves full equality, nil/missing behavior and
the unchanged three-argument API with its unused projection argument.
The parent validator still owns closed-preimage shape and lineage checks.

In the SH08 function-call namespace, select
`sh08-match-preimage-fixed-fields-development-kernel` with the existing Boolean
match, structural match-shape, semantic identity-normalization and malformed
request controls. The observer delegates specialized/generic `get` and unary
`rest` unchanged, checking exact read sequences and absence of Vector tails.
Private equality success on sparse DATA does not authenticate an artifact.
This leaf repair adds no normalization, partial comparison, schema, authority
or general typed-source support and requires no automatic C5/C6/source replay.

### Body-effect development

The body-analysis owner is `bootstrap/gravity/src/gravity/effects.gravity`.
The existing `gravity.compiler.c8-effect-checker-engine` module forwards its
`sh09-build-authenticated-body-effect-result [typed verification binding]` and
`sh09-verify-authenticated-body-effect-result [typed verification binding candidate]`
entrypoints to that owner. Its older pure-result and compiler-read interfaces
remain separate. A compiler-read allowance is not evidence that a body is pure.

Use the actual canonical core retained by the existing C7 request and binding,
not a supplied effect summary. The local first-order subset derives effects
from complete node coverage and acyclic local calls. Function creation retains
latent body effects without executing the body. Conditional branches and match
guards contribute conservatively to the effect union while their evaluation
regions remain conditional. Declared effects remain allowances; unused
nonempty declarations do not become inferred actions. Thrown-error facts remain
pending, and this analysis does not establish full effect legality.

Run the direct Gravity kernels first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-operation-development-kernel \
  --exact body-effect-coverage-development-kernel \
  --exact body-effect-guard-development-kernel \
  --exact body-effect-final-input-fences-development-kernel \
  --exact body-effect-call-metadata-development-kernel \
  --exact body-effect-import-origin-development-kernel \
  --exact body-effect-import-bounds-development-kernel \
  --exact body-effect-foreign-summary-development-kernel
```

These use normalized graph DATA for development feedback, not authenticated
upstream evidence. Changes to input joins or graph semantics also need affected
real C5/C6 -> C7 -> effects composition, using fresh C7 verification and the
complete original request in its binding. Do not reuse an accepted result as
that composition's input authority.

The current local-body composition also exercises the real public C8 bundle:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-authenticated-paired-composition --fail-fast
```

That test derives paired C5/C6 and C7 products, freshly verifies C7, and checks
the leaf before constructing the real C8 module bundle. It exercises the public
constructor and verifier without changing the bundle's fresh validation rules.
Repeat that public-bundle boundary when C8 forwarding/authentication code,
namespace dependencies, exports/arities, qualified callsite placement, or
bundle construction/routing changes. Private leaf semantics alone do not
require rebuilding this expensive boundary if affected real upstream-to-leaf
composition and unchanged forwarding/route checks cover the change. Source and
plan identities must still describe the current bytes: prior route evidence is
not proof of a new bundle identity. Keep post-run reject-only or diagnostic
corrections distinct from the source snapshot exercised by an earlier run.
Imported bodies, higher-order or general recursive propagation, unsupported operations,
throw/try composition, general provider/grant policy and authoritative effect
identities remain outside this local subset. The legacy imported-type-only
input still rejects with `:imported-body-effects-pending`. Record actual test
results separately from migration, target support and self-hosting status.

### Direct imported body analysis

The same `gravity.effects` body interface consumes the existing imported C7
result and fresh verification, with the original four-key request retained in
its binding. Each actual producer request must match the caller's retained C5
module input and C7 producer identity. Analysis follows the original public
export through its projected definition to the function and body; private
producer-local callees are analyzed within their own module, not exported as
new APIs. No declaration-only effect summary or synthetic accepted producer
result replaces the body.

Caller and producer graphs have separate module coordinates. A foreign body
dependency records both producer artifact IDs, the caller's imported binding,
the producer's projected definition binding, function and body. The original
C5 export identity remains in the validated origin join. Whole producer graphs enter the effect
identity; caller node coverage still covers only the caller. The existing
node/function counts must describe the actual graph; arbitrary aggregate
capacity quotas and deferred-work budgets are removed.
Legality and thrown-error facts remain pending. C7's original type-only facts
and pending labels are preserved as upstream evidence, not rewritten as if C7
performed effect analysis. The older compiler-read request validator continues
requiring exactly three keys.

Run affected real producer/consumer composition separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-imported-authenticated-paired-composition --fail-fast
```

This uses the existing paired inferred module fixtures, actual C5/C6 products,
fresh caller C7 construction/verification and the current effects leaf. It does
not build a new public C8 bundle or claim its current identity. Use
`body-effect-public-forwarding-source-boundary` and affected module-route
kernels for unchanged-forwarder checks; the public-bundle triggers above still
apply. Direct leaf producers and scalar imported signatures are the current
boundary. Transitive imports, callable values, general recursive propagation,
throw/try, provider/grant legality, target execution and authoritative migration
remain separate work.

### Declared ordinary direct-self calls

The current development increment accepts a fully declared, monomorphic,
fixed-arity named function with one direct self-call in a singleton component,
using the supported atomic or homogeneous-List parameter and return types.
Change the existing C7 engine, effects, emitter and runtime owners together
when this boundary changes. C7 uses the complete parameter and return types
as value assumptions, then checks the actual body and every incoming and
self-call argument. A declaration is not an inferred effect or error summary.
C8 reconstructs the source-bound proof and derives effects and escaping error
types from bottom; fresh verification rejects nonleast candidate graphs even
when their equations hold.

Use the existing focused checks, selecting only the changed component:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-declared-self-value-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-declared-self-effects-development-kernel \
  --exact current-gravity-declared-self-prefix-work-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact current-gravity-declared-self-caller-loop-effects-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-declared-self-lowering-development-kernel \
  --exact current-gravity-declared-self-runtime-development-kernel --fail-fast
# Only when changing the paired-source comparison:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-function-path-parity-development-kernel --fail-fast
# Affected source composition, not the ordinary edit/test inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-declared-self-authenticated-composition --fail-fast
```

The runtime retains target, arity and activation-path checks before argument
evaluation, once-only left-to-right operands and fresh activation locals. It
allows the current function's contiguous self-activation suffix, not an
indirect cycle through another function. Remove the arbitrary path-count quota;
supplied-path controls do not prove host-stack safety.
An acyclic caller may use its existing explicit function recur while calling
the self-recursive function. That combination uses one effect/error and loop
summary fixed point without a cumulative work budget.

The paired source selector exercises String List traversal with transformed
`rest` and `str` arguments, empty/one/two-element results, a deeper String throw,
argument transfer and a Bool caller recur. Rejected sources cover a wrong
self-argument, a wrong body return and missing source effect allowance. Its
actual validation result is recorded in the backlog, not implied by listing
the command here. Do not replay an unchanged expensive composition to edit
this guidance.

The private inferred extension below is separate from this declared path.
This increment excludes mutual or indirect recursion, arbitrary captures and
non-core named helpers inside the self body,
and try/loop/recur inside that body. These are current implementation frontiers,
not language-wide prohibitions. It proves neither termination nor TCO, fatal
host-error normalization, full profile/provider legality, target completeness,
authoritative migration or self-hosting.

### Private ordinary direct-self inference

Work on private recursive value inference in the existing C7 engine, effects
and emitter owners; the ordinary-call runtime does not need another recursion
mechanism. The narrow development case is one private, fixed-positive-arity
function with one direct self-call. Its complete normal incoming-call census
must independently establish one monomorphic parameter vector, and its actual
return positions must provide an independent, consistent return witness.
Supported value domains are the current atomic and homogeneous-List domains,
not generic Integer, positional Vector or an unconstrained recursive type.

Use the existing direct runner and select the changed component:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-private-self-value-projections-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-private-self-effects-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-self-lowering-development-kernel --fail-fast
# Affected source composition, not a prerequisite for every ordinary edit:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-self-authenticated-composition --fail-fast
```

The source selector uses the existing paired `private-self-values` fixtures
and rejected entry-conflict, return-conflict and missing-effect fixtures.
Listing it is not a claim that its current run passed; actual outcomes belong
in the backlog. Focused normalized kernels provide development feedback, not
source compilation or migration evidence.

Cold inference excludes recursive-result-dependent facts rather than treating
them as independent seeds. A Never callee result is not an invocation guard;
a definitely nonnormal operand prevents invocation. The derived parameter
premise supplies type and immutability, never nonemptiness: `first` still needs
the existing same-binding `empty?` branch proof. Effects and escaping errors
are derived separately from bottom. Calls remain ordinary activations, not
`recur` or TCO. Exported or escaping recursive functions, conflicting entries,
mutual recursion, general captures, imported recursive producers and expanded
recursive value domains remain outside this increment. No authoritative module
migration, full W5, target, self-hosting or release status follows from it.

### Private indexed ordinary recursion

Change the owning C7 pass for type/proof behavior, `effects.gravity` for C8,
the typed emitter for lowering and `runtime.gravity` for activation semantics.
Use paired `local-runtime-indexed-private-self` accepted fixtures and the six
`indexed-private-self-*` rejected pairs. They delimit the private zero-entry,
same-Vector/same-needle unit-step family; they do not make broader recursion
language-illegal. Existing public compiler interfaces remain unchanged.

Start with the affected direct kernel, adding adjacent kernels when its
interface or behavior changes:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-indexed-private-rejection-development-kernel --fail-fast
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-indexed-private-source-context-development-kernel --fail-fast
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-indexed-private-lowering-runtime-development-kernel --fail-fast
# Costly affected source composition, not the ordinary edit-to-test loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-indexed-private-authenticated-composition --fail-fast
```

The source selector constructs fresh products and exercises function-table
activations, not `execute-closed-plan`. Its recorded run took about 737 seconds;
do not replay it merely to change labels, collect metrics or restate unchanged
evidence. Runtime, producer or downstream composition changes determine when a
new affected integration run is useful. Focused checks are development feedback;
fresh provenance/equivalence/bootstrap evidence belongs to an explicit stable
revision admission decision. The backlog records actual outcomes and exclusions.

### Vector family classification

STD3 owns unary `vector?` semantics. The development implementation accepts
independently established closed operand types, evaluates the operand once,
preserves effects/transfers, and returns Bool on normal invocation. Never
aborts invocation; Unknown is unsupported, not a false classification. A Bool
result supplies no member, length, index or type-refinement facts.

Edit the owning pass, not an authority-admission adapter:

- C7: `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
- C8: `bootstrap/gravity/src/gravity/effects.gravity`.
- Typed lowering: `bootstrap/gravity/p15_s23/emitter.gravity`.
- Runtime: `bootstrap/gravity/src/gravity/runtime.gravity`.

Use the affected existing direct kernel first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-predicate-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-vector-predicate-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-predicate-lowering-runtime-development-kernel --fail-fast
# Affected source composition, separate from the normal inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-predicate-authenticated-composition --fail-fast
```

The paired source programs separately exercise the unchanged W5 indexed
helper/wrapper and the existing typed-catch path. The cold-wrapper kernel in
the SH08 recursive-function namespace checks independent caller-derived
Vector7/Keyword facts; it does not obtain them from `vector?`. Rejected fixture
pairs cover unknown operands and zero/two arity. QST byte/parser parity is not
negative source-compilation evidence. Actual results and exclusions belong in
the backlog. No fresh authority proof is a prerequisite for ordinary edits;
authoritative migration remains an explicit separate decision.

### Integer family classification

STD5 owns unary `integer?` semantics. The development frontier accepts
independently established closed operand types, including the existing Integer
and I8/I16/I32/I64/U8/U16/U32/U64 type facts. These normalized typing cases do
not establish native-width arithmetic. Unknown remains unsupported; Never
aborts invocation. Operand evaluation happens once in non-tail position and
preserves effects and transfers. A Bool result supplies no conversion, width,
range, arithmetic-mode, overflow or type-refinement proof.

Use the same C7, C8, emitter and runtime owners listed for `vector?`, and the
existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-integer-predicate-development-kernel \
  --exact sh08-integer-predicate-throw-consumer-development-kernel \
  --exact sh08-ordering-pending-call-entry-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-integer-predicate-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-predicate-lowering-runtime-development-kernel --fail-fast
# Affected source composition, separate from the normal inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-predicate-authenticated-composition --fail-fast
```

The throwing-call consumer check exercises the containing pass, not only the
predicate helper: resource reasons and measured work must survive semantic
rejection. Binding, source, descriptor, fact and ordered ledger checks remain
independent at their owning boundaries. Run an affected source composition when
those boundaries change; direct focused feedback does not first require fresh
authority proofs or a migration fingerprint update.
The pending-call entry check covers source-order independence in the existing
bounded inference pass. An unresolved ordering diagnostic remains present in
that round while later real calls contribute independent parameter facts;
final unresolved, incompatible or conflicting facts still reject. No predicate
result is used as an operand-type refinement.
The paired source programs separate the unchanged W5 nonnegative-integer helper
from existing typed-catch transfer behavior. They do not imply general W5,
native-width or authoritative migration support. Rejected QST fixture parity
is not negative source-compilation evidence. Record actual results and limits
in the backlog.

### Set conversion and cardinality

STD3 owns persistent Set conversion and cardinality. The current bounded
development constructor accepts a finite, independently established homogeneous
String or Keyword Vector on the JVM target and returns a bare Set type. It does not invent
cardinality, ordering or member facts. Normal construction declares possible
allocation even for an empty input; operand transfers prevent invocation and
preserve their effects. Set count is a separate outer-family query and is not
restricted to String members or allowed to evaluate stored payloads.

The owning files remain `c7_type_checker_engine.gravity`, `effects.gravity`,
`p15_s23/emitter.gravity` and `runtime.gravity`. Their focused checks use the
existing direct runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-set-conversion-development-kernel \
  --exact sh08-set-count-development-kernel \
  --exact sh08-set-conversion-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-set-conversion-source-development-kernel \
  --exact current-gravity-set-count-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-set-conversion-lowering-runtime-development-kernel \
  --exact current-gravity-set-count-lowering-runtime-development-kernel \
  --exact current-gravity-set-target-and-association-eligibility-development-kernel --fail-fast
```

The runtime validates every actual String member before hashing or insertion.
Remove the component allowance, member-count quota, content-work formula and
budget-only rejection. Preserve String value semantics and semantic duplicate
checks; unmetered correctness is not a native/realtime performance claim.

When changing the public typed/effected/lowered composition, exercise the
affected accepted and rejected source fixtures as well as these kernels.
Run the affected composition separately from the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-set-conversion-fixtures-development-kernel \
  --exact current-gravity-set-conversion-authenticated-composition --fail-fast
```

Preserve the distinction between normalized JVM/WASM eligibility checks and
actual target execution. Set equality, iteration, other conversion families,
whole-W5 execution and authoritative migration remain separate work; none is
established by these focused checks.

### Keyword Vector Set conversion

Use the same four conversion owners for the Keyword input extension. String,
empty and Never descriptors retain their existing route. Nonempty homogeneous
Keyword input has a distinct versioned construction policy; output is still
bare Set. Input extent is an allocation upper bound, not post-dedup cardinality
or a finite-member proof for Set equality/membership consumers.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-keyword-vector-set-conversion-development-kernel --exact sh08-keyword-vector-set-conversion-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-keyword-vector-set-conversion-source-development-kernel --exact current-gravity-keyword-vector-set-conversion-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-vector-set-conversion-lowering-development-kernel --exact current-gravity-keyword-vector-set-conversion-id-guards-development-kernel --exact current-gravity-keyword-vector-set-conversion-runtime-development-kernel
```

The complete actual Keyword Vector is validated before printing, comparison,
hashing or insertion. Remove logical allowances, member-count quotas and
printed-representation cost units. Gravity Keyword equality chooses
representatives; spelling is not equality and provider Set storage is not the
semantic deduplication proof. Preserve duplicate collapse, namespace/name
distinctions, original input, dominant source/producer ID guards, complete C8
semantic rows and normal/Never allocation. This is not a provider CPU or
realtime guarantee.

For the affected composition:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-vector-set-conversion-fixtures-development-kernel --exact current-gravity-keyword-vector-set-conversion-authenticated-composition --fail-fast
```

The fixture preserves the actual C16 semantic-fields declaration and binary
conj/conversion expression. Use the existing AP closed module-instance path
to initialize the declaration, not a nil-pool table wrapper or a replacement
local helper. Twenty-four input positions produce 23 distinct runtime members;
this observation does not refine the output type. One Gravity positive and one
mixed-family C7 negative run; QST is byte/parser only. Do not claim whole C16,
variadic conj, keys conversion, new Set consumer domains or migration support.
The retained source run described in the backlog remains failed at one false
harness expectation; its correction and separate cheap checks do not relabel
it as a green aggregate. Reuse unchanged completed observations where valid;
do not reconstruct expensive products solely to change a historical label.

### Binary nonnumeric atomic inequality

For binary `not=`, edit the C7 engine, `gravity.effects` and the typed emitter.
STD2 owns the complement-of-applicable-equality law. This nonnumeric increment
supports Nil, Bool, Character, String, Symbol, Keyword, their five existing
nil-or-atomic carriers, and Never. Both operand domains are checked even when
one transfers. Numeric, collection, callable, Unknown and malformed domains
remain outside this increment; the separate Integer extension is below. No
first-class or variadic operation is added.

Start with the owning focused checks, retaining adjacent `=`/nil-equality
regressions when changing their shared dispatch:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-atomic-inequality-development-kernel --exact sh08-atomic-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-atomic-inequality-source-development-kernel --exact current-gravity-atomic-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-id-guards-development-kernel --exact current-gravity-atomic-inequality-lowering-runtime-development-kernel --exact current-gravity-atomic-inequality-transfer-fence-development-kernel --fail-fast
```

C7/C8/emitter independently validate the source binding, two operands, versioned
descriptor, call/edge, constraints and complete ledger. Lowering owns an existing
equality test with false/true branches; it creates no source `=` facts. Evaluate
both operands once, left-to-right and non-tail, preserving effects and original
transfers. Compare actual nullable values: different nullable families can both
hold nil. Use the existing canonical Character transport, not a manually tagged
BMP record. Meter actual pre-child work separately from success-only suffix work.

For the affected source boundary, select the cheap fixture check before the
bounded completion composition:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-completion-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-completion-authenticated-composition --fail-fast
```

This executes five Gravity entries, two same-emitted-IR seed controls, genuine
C7/C8 guards and coherent rejection controls on the smaller completion input.
QST is byte/parser coverage only. The seed cannot interpret the existing typed
map-lookup or typed-catch instructions; do not widen it to satisfy a reference
outside its domain. The original sixteen-function source attempt remains a
failed partial observation, not a green aggregate. Preserve its passed and unrun
boundaries as recorded in the backlog; do not reconstruct that unchanged input
merely to change its historical label. Runtime and seed production files are
unchanged. This is development implementation, not migration, whole W5 execution,
other-target support, self-hosting or release evidence.

### Binary represented-Integer inequality

The same C7, effects and emitter owners extend binary `not=` to the existing
represented `Integer` equality domain. Select exactly Integer/Integer,
Integer/Never or Never/Integer; the old Never/Never atomic path is unchanged.
Do not broaden the atomic whitelist or infer fixed-width, cross-numeric,
Float/Decimal/Ratio or nullable-Integer support. Integer/Keyword still rejects.
The old numeric rejection fixture now uses `7.5`, because Integer `7` is no
longer an unsupported operand. This is an implementation frontier change,
not a change to STD2's complement law.

The versioned numeric record preserves existing Integer equality's original
values, no conversion/arithmetic/overflow/rounding, `:proof-required` mode and
`:proven-safe` rule. Numeric semantics remain owned by `gravity.math` and its
Layer0 owner; the actual source operation remains `gravity.core/not=`. All
three passes reconstruct the complete descriptor and constraint solution.
Lowering still owns its equality/false/true IR and creates no source `=` fact.
Runtime, seed, public APIs and instruction schemas are unchanged.

Start with the new owning kernels, alongside affected atomic-inequality and
integral-equality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-integer-inequality-development-kernel \
  --exact sh08-integer-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-integer-inequality-source-development-kernel \
  --exact current-gravity-integer-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-integer-inequality-transfer-fence-development-kernel --fail-fast
```

Select the affected real composition separately when these producer/consumer
facts or lowering behavior change:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-authenticated-composition --fail-fast
```

That selection uses one changed Gravity positive and one changed Gravity
negative. Count-produced Integers are concrete supported inputs; this is not
execution of a whole compiler consumer. Q sources provide byte/parser parity
only. Type-conforming runtime entries, actual pre-child work, preserved
operand effects and original transfers remain required. The seed reference
uses the same emitted IR and excludes typed catches it cannot interpret.
Focused results and this bounded composition do not establish another target,
authoritative migration, complete numeric support or self-hosting.

### Binary same-type fixed-integral inequality

The same three owners also support binary `not=` for I8, I16, I32, I64,
U8, U16, U32 and U64. Both normally returning operands must have the same
established type; either position may instead be Never. Cross-width,
signed/unsigned and generic-Integer/fixed pairs reject, as do Float, Decimal,
Ratio, nullable fixed types and collections. Existing Integer, nonnumeric and
Never/Never descriptors remain unchanged.

The fixed-family numeric record names the actual type and preserves the
existing equality policy: original values, no coercion or arithmetic,
`:proof-required` mode and `:proven-safe` safety. C7, effects and the emitter
independently reconstruct the complete record and its joins. Runtime, seed,
public APIs and IR schemas are unchanged. Start with these owning checks,
including affected Integer, atomic-inequality and integral-equality regressions
when their shared rules change:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-fixed-integral-inequality-development-kernel \
  --exact sh08-fixed-integral-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-fixed-integral-inequality-source-development-kernel \
  --exact current-gravity-fixed-integral-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-fixed-integral-inequality-transfer-fence-development-kernel --fail-fast
```

Runtime fixtures independently require Integer representation and the declared
signed/unsigned bounds before every normal or same-IR seed entry. This checks
conditional type-conforming execution, not implicit source Integer conversion,
host FFI admission or runtime width validation. Never transfers still preserve
their original identity, non-tail restrictions and left-to-right effects.

When those producer/consumer facts or lowering change, select the new paired
fixtures and affected source composition separately:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-authenticated-composition --fail-fast
```

This uses compiled helpers with explicit fixed-type signatures, not implicitly
converted source Integer literals. Paired Q sources provide byte/parser parity
only. Do not reconstruct earlier unchanged Integer or atomic source products
merely to refresh a green label.

### Binary finite persistent Vector inequality

For binary `not=` over established finite positional Vector types, change the
existing C7 engine, effects checker and typed emitter. Reuse their existing
Vector equality domain and owned `if (= left right) false true` IR; runtime,
seed, public interfaces and type grammar do not change. Each pass reconstructs
the complete versioned collections policy and the original `not=` constraint,
call fact and ledger. Do not manufacture a source `=` fact.

Both complete type domains are validated before an early value mismatch.
Supported recursive members are Vector, Nil, Bool, Character, String, Symbol,
Keyword and the five existing closed nullable nonnumeric atomic types. Empty
and differently shaped/length Vectors compare normally; static type equality
is not required. Remove width, depth and work quotas. The legacy version
excludes numeric members; the flat Generic
Integer and finite Keyword-Set leaf extensions below are separate.
Unknown/bare Vector types, other collection domains,
whole nullable Vectors and nested Never remain unsupported. Top-level Never
preserves non-tail control transfer when the other operand establishes this
Vector domain; Never/Never retains the existing atomic rule.

Start with these focused checks and affected equality/inequality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-inequality-development-kernel \
  --exact sh08-vector-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-vector-inequality-source-development-kernel \
  --exact current-gravity-vector-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-vector-inequality-transfer-fence-development-kernel --fail-fast
```

Remove Vector/atomic-inequality work-limit reasons and charged-work fields.
The private inferred-call check retains actual semantic subtype reasons.
Preserve original child failures without accounting or a scalar quota.

For changed producer/consumer facts or lowering, separately select the paired
fixture check and one affected source composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-authenticated-composition --fail-fast
```

This uses the W5 seven-Keyword value with a fresh producer and new inequality
expressions, not a copied or unchanged W5 inequality helper. Q fixtures provide
byte/parser parity only. Do not repeat unchanged Vector equality, numeric
inequality or whole-W5 source compositions to refresh a label. Focused results
are development feedback, not authoritative migration or self-hosting proof.

### Flat Generic Integer Vector comparison

STD3's corresponding-value Vector equality now has a bounded Generic Integer
extension, with STD2's `not=` complement. Edit the existing C7 engine, effects
checker, typed emitter and runtime Vector helpers. Reuse exact original-value
scalar Integer equality; do not add conversion, widening or arithmetic.

When either Vector contains an Integer, both must be flat. Every overlapping
Integer position must pair with Integer. Validate both complete domains and
all overlapping compatibility before returning a length/value mismatch. Keep
the legacy recursive nonnumeric value semantics and identity checks.
Nested numeric Vectors, fixed-width numeric leaves, Float/Decimal/ratio,
Nil-or-Integer and Unknown remain outside this increment. Remove capacity
quotas and logical visit units; this is not a provider CPU or heap guarantee.

Start with the existing direct focused tests, selecting the equality and
inequality transfer-fence checks when operand control flow changes:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-flat-integer-vector-equality-inequality-development-kernel \
  --exact sh08-flat-integer-vector-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-flat-integer-vector-equality-development-kernel \
  --exact current-gravity-flat-integer-vector-inequality-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-integer-vector-equality-lowering-development-kernel \
  --exact current-gravity-flat-integer-vector-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-flat-integer-vector-equality-runtime-development-kernel --fail-fast
```

Changed cross-pass behavior needs the affected tiny count-produced Vector
source composition, not an unchanged whole C6 validator:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-integer-vector-comparison-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-integer-vector-comparison-authenticated-composition --fail-fast
```

Mixed C6 rows are not covered merely by this Integer extension. The following
section separately covers finite Keyword-Set leaves, not arbitrary Sets.
Paired Q sources establish
parser parity only; focused tests and source composition are not authoritative
migration, current-closure admission or self-hosting evidence.

### Flat Vector comparison with Keyword-Set leaves

STD3's Vector equality composes the existing finite Keyword-Set equality;
STD2's `not=` remains its complement. Change the existing C7, C8, emitter and
runtime Vector owners, not a new comparison wrapper. Independently established
finite literal-shape all-Keyword Set types are supported as flat Vector leaves.
When either Vector contains a Set, both Vectors must be flat.
Corresponding Set positions require Set on both sides; corresponding Integer
positions require Generic Integer. Even an empty Set selects the new closed
policy. Preserve recursive nonnumeric and flat Integer no-Set versions and work.

At runtime, before any Vector length/value mismatch, validate all Set leaves on both sides
using full owned Set self-equality, including duplicate value classes. The
subsequent corresponding Set comparison invokes owned equality again. Each
call validates the actual values without an aggregate or per-leaf allowance.
Remove accounting-only nested quota failures; preserve actual malformed-domain
diagnostics and the containing C7 `not=` semantic checks. The Set-leaf variant retains the
existing Set equality JVM restriction.

Use the existing direct focused tests, with both transfer-fence kernels when
operand control flow changes:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-flat-keyword-set-vector-equality-inequality-development-kernel \
  --exact sh08-flat-keyword-set-vector-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-flat-keyword-set-vector-equality-development-kernel \
  --exact current-gravity-flat-keyword-set-vector-inequality-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-keyword-set-vector-equality-lowering-development-kernel \
  --exact current-gravity-flat-keyword-set-vector-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-flat-keyword-set-vector-equality-runtime-development-kernel --fail-fast
```

Changed composition needs a tiny accepted/rejected source check separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-keyword-set-vector-comparison-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-flat-keyword-set-vector-comparison-authenticated-composition --fail-fast
```

Do not replay unchanged Set construction or membership products. Bare, mixed,
String or numeric Set domains and nested Set-containing Vectors remain outside
this increment. No finite domain is inferred from `set(keys actual)` spelling;
whole List-compatibility/C6 helpers, C17, migration and self-hosting remain
separate. Existing immutable finite provider/cardinality-enumeration premises
apply; no arbitrary host-provider CPU or peak-memory containment is established.

### Binary finite Keyword-Set inequality

For binary `not=` over established finite literal-Keyword Set types, edit the
existing C7 engine, effects checker and typed emitter. Reuse their complete
Keyword-Set equality domain checks and owned `if (= left right) false true`
IR. Each pass reconstructs its own versioned inequality policy, original
source call, constraint solution, call fact and ledger. Do not manufacture a
source `=` fact. Runtime, seed, public interfaces and type grammar do not change.

Both complete domains are checked before cardinality or value comparison.
Empty, differently sized and reordered Sets compare normally; matching static
types are not required. Remove member-count and work-unit quotas.
Bare/homogeneous/Unknown or malformed Sets, whole nullable
Sets, non-Keyword members, nested collections and nested Never remain outside
this slice. A top-level Never preserves transfer when the other operand
establishes the supported Set domain; Never/Never keeps the atomic rule.

Start with focused semantic checks and affected equality/inequality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-set-inequality-development-kernel \
  --exact sh08-keyword-set-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-keyword-set-inequality-source-development-kernel \
  --exact current-gravity-keyword-set-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-keyword-set-inequality-transfer-fence-development-kernel --fail-fast
```

Preserve original child failures and the containing atomic-inequality diagnostic;
the private inferred-call check retains the Set-specific underlying reason.
Existing runtime equality validates both actual domains, member uniqueness
before an early result, without a comparison reservation.
This is not authority to rely on host Set storage or claim a provider CPU bound.

For changed producer/consumer facts or lowering, select the paired fixture
check and one affected source composition separately:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-authenticated-composition --fail-fast
```

The source fixture uses C17's capability-Set value with a fresh producer and
new inequality expressions. It does not activate or compile the whole C17
helper. Q fixtures provide byte/parser parity only. Do not replay unchanged
Keyword-Set equality or Vector inequality source compositions to refresh a
label. Development results do not establish migration or self-hosting.

### Nil value classification

For unary `(nil? value)`, edit the existing C7 engine, effects and typed emitter
owners. Runtime, seed, public APIs and IR schemas remain unchanged. Current
validated closed types select existing IR: known Nil/non-Nil evaluates the
operand then returns true/false; the existing nullable Bool, Character, String,
Symbol and Keyword families compare the evaluated operand with Nil. Never
retains the original transfer. Nullable Integer/collections, Unknown/Dynamic,
malformed types and unsupported callables are not added by this increment.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-nil-predicate-development-kernel --exact sh08-nil-predicate-id-guard-development-kernel --exact sh08-nil-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-nil-predicate-source-development-kernel --exact current-gravity-nil-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-nil-predicate-lowering-runtime-development-kernel --exact current-gravity-nil-predicate-transfer-fence-development-kernel
```

Keep the classification/source/type joins, independent C8 reconstruction and
complete semantic rows, original child failures, original throw and
non-tail recur fences. The synthetic nullable equality is typed lowering,
not a fabricated source equality call. No generated local, source rewrite or
raw runtime `nil?` builtin is introduced. This type-directed route is supported
for authenticated typed calls and type-conforming entries, not arbitrary
wrong-typed host/FFI arguments. It is not check-elision or migration evidence.

For affected source composition, run:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-nil-predicate-fixtures-development-kernel --exact current-gravity-nil-predicate-authenticated-composition --fail-fast
```

The fixture compares the copied `w5-se-canonical-form` body with its read-only
owner, then adds new known/unknown-mode queries on that nullable Keyword
producer. It does not claim an existing W5 nil-query helper or whole-W5 build.
One Gravity positive and one C7 arity rejection run; QST is byte/parser only.
An allocation-bearing operand checks source effect preservation; observable
I/O ordering is covered separately by normalized kernels, not this source
fixture. Seed comparisons interpret the same emitted IR, not raw-source parity.

### Boolean family classification

Unary `(boolean? value)` returns true for both `true` and `false`, not for
truthy values generally. Edit the existing C7 engine, effects, typed emitter
and Gravity runtime owners. Existing valid closed first-order operand types
are accepted; Never preserves control transfer. Unknown/Dynamic, malformed
carriers and unsupported callable types remain diagnosed. The type grammar,
unary IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-boolean-predicate-development-kernel --exact sh08-boolean-predicate-id-guard-development-kernel --exact sh08-boolean-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-boolean-predicate-source-development-kernel --exact current-gravity-boolean-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-lowering-runtime-development-kernel --exact current-gravity-boolean-predicate-shallow-runtime-development-kernel
```

Keep the once-only function-returned-false control: classification must return
true without dropping or repeating operand effects. String/Keyword spelling,
nil, represented numbers and collections do not become Bool values. A wrapper
Map is not unwrapped. Runtime classification is shallow, with no payload
traversal, hashing, equality, mutation or collection construction. Retain
dominant unary/ID guards, independent complete C8 semantic analysis,
transfers/original failures and proportionate
Keyword/Symbol-predicate regressions. These are component development checks,
not source integration or authoritative migration evidence.

For affected compiler-to-runtime composition, run the paired preflight and
the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-authenticated-composition
```

The preflight compares the copied C6 scalar-helper defn AST with its actual
owner. Only false/true callers supply that helper, giving one inferred Bool
parameter domain. Its existing nil comparison is `(= value nil)`, not a new
`nil?` primitive. Other families have separate direct-query functions. This is
not full C6 execution, polymorphic scalar validation or new parameter grammar.
One Gravity positive and one C7 arity rejection run; QST has byte/parser coverage
only. Same-emitted-IR seed comparisons are not independent source compilation.

### Symbol family classification

Unary `(symbol? value)` classifies the normally evaluated value, not source
identifier spelling. Edit the existing C7 engine, effects, typed emitter and
Gravity runtime owners. Valid closed first-order types are accepted; Symbol
values classify true, other families false and Never propagates. Unknown,
Dynamic, malformed carriers and unsupported callable types remain typing
failures. The existing type grammar, unary IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-symbol-predicate-development-kernel --exact sh08-symbol-predicate-id-guard-development-kernel --exact sh08-symbol-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-symbol-predicate-source-development-kernel --exact current-gravity-symbol-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-lowering-runtime-development-kernel --exact current-gravity-symbol-predicate-shallow-runtime-development-kernel
```

The query preserves unary evaluation, operand effects, transfers and original
failures. Runtime classification is shallow, with no payload traversal,
hashing, equality, mutation or collection construction. Qualified and
metadata-bearing Symbols remain values. A String or Keyword with similar
spelling stays non-Symbol; a syntax-wrapper Map is not unwrapped. Classification
grants no binding, call, macro or capability authority. Keep complete C8
semantic context and dominating unary shape/ID
guards, and proportionate Keyword/Set-predicate regressions. Focused tests are
component feedback, not source integration, frontend-module or migration proof.

For affected compiler-to-runtime composition, run the paired preflight and
the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-authenticated-composition
```

The existing Symbol return annotation is exercised through an ordinary
identity helper with a monomorphically inferred quoted-Symbol input. Other
families use separate functions, not fabricated polymorphic inference or new
parameter annotation syntax. A tagged wrapper is ordinary Map DATA, not proof
of accepted syntax-object schema. One Gravity positive and one C7 arity
rejection run; QST has byte/parser coverage only. Seed interpretation of the
same emitted IR is not independent source compilation or whole frontend
module coverage. Pure source operands do not independently observe I/O order.

### Set family classification

For unary `(set? value)`, edit the existing C7 engine, effects, typed emitter
and Gravity runtime owners. Existing valid closed first-order operand types
are accepted: bare or finite Set families classify true and other families
false. Unknown/Dynamic, malformed carriers and unsupported callable types
remain typing failures, not false classifications. Never propagates normally.
The existing type grammar, unary builtin IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-set-predicate-development-kernel --exact sh08-set-predicate-id-guard-development-kernel --exact sh08-set-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-set-predicate-source-development-kernel --exact current-gravity-set-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-lowering-runtime-development-kernel --exact current-gravity-set-predicate-shallow-runtime-development-kernel
```

These exercise the owning passes, including the complete C8 analyzer's
semantic context and missing/duplicate/orphan semantic rows, without a debit
or exact/one-less work test.
Malformed unary call shape must reject before any ID traversal. Runtime
classification itself is shallow: it does not count, sequence, hash, compare,
mutate or validate Set payloads. A throwing-provider control checks zero such
callbacks. This is not a claim that arbitrary foreign values bypass ordinary
input/evaluation boundaries. Operand effects, once-only evaluation, transfers
and original failures remain; the query adds no collection allocation.
Retain proportionate Map-predicate and Set-conversion regressions. Closed type
carrier validation is distinct from runtime payload inspection. Focused
component checks do not establish source integration or migration evidence.

For affected compiler-to-runtime composition, run the paired preflight and
then the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-authenticated-composition
```

The preflight checks paired bytes/parser shape and unchanged C9 aggregate-helper
body reuse. That helper is exercised monomorphically on the existing bare Set
result of String-Vector conversion; it is not a full C9 module build or generic
polymorphism test. Other finite Set/non-Set cases use separate functions. The
composition selects one Gravity positive and one Gravity C7 arity rejection,
not QST typed products. Same-emitted-IR interpreter comparisons do not establish
independent raw-source compiler parity. There is no new Set annotation grammar.

### Finite Keyword-Set membership

Edit the existing C7 engine, effects, typed emitter and Gravity runtime for
binary `(contains? items value)`. The development domain is an existing finite
literal-Keyword Set type and an independently established Keyword needle;
the normal result is Bool. This frontier is not a language-wide restriction.
The closed value-type grammar, existing Map behavior and Never transfers stay
unchanged. Querying adds no collection-allocation effect; constructors retain
their own allocation requirements.

Run the owning semantic kernels directly:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-keyword-set-contains-development-kernel --exact sh08-keyword-set-contains-dispatch-guard-development-kernel --exact sh08-keyword-set-contains-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact body-keyword-set-contains-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-lowering-development-kernel --exact current-gravity-keyword-set-contains-runtime-development-kernel
```

These check independent source/type/descriptor joins, actual child failures,
complete producer rows, once-only operand evaluation,
transfers and original failures. Runtime membership validates all actual Set
members and their semantic uniqueness before either hit or miss. Remove width
quotas, logical allowances and prepaid representation work. Membership uses
actual Gravity Keyword equality, not
printed identity, host Set lookup or hashing. This is not a provider CPU bound.
The query preserves original values and does not execute stored payloads.

Retain the existing Map-contains kernels and proportionate evaluated-Set and
Set-equality regressions when their shared consumers change. Focused kernels
are development feedback, not fresh source-composition or migration evidence.

When membership descriptors, effects, lowering or runtime composition change,
run the paired fixture preflight, then the affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-authenticated-composition
```

The preflight checks paired bytes and parser shape. The composition selects
one Gravity accepted source and one Gravity C7 rejection; it does not run QST
typed products. Seed interpretation of the same typed-emitted IR is a runtime
comparison, not independent raw-source compiler parity. Renew affected paired
composition when reader/extension/identity handling changes, not every edit.

### Finite Character-Set membership

The same four owning passes support a finite Character-Set value and a
Character needle, including quoted Set values. Evaluated literal construction
has separate owning checks below; Character-Set equality is not selected.
Start with the membership kernels:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-character-set-contains-development-kernel --exact sh08-character-set-contains-dispatch-guard-development-kernel --exact sh08-character-set-contains-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact body-character-set-contains-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-lowering-development-kernel --exact current-gravity-character-set-contains-runtime-development-kernel --exact current-gravity-character-set-contains-transfer-fence-development-kernel
```

Check independently reconstructed source/type/descriptor joins, complete
producer rows, Never transfers, malformed carriers, semantic duplicates and
actual value semantics. The runtime validates every member and the needle,
then checks semantic uniqueness
before returning either hit or miss. Host Set lookup, hashing and printing
are not membership authority. Existing Keyword membership remains unchanged.
The Character representation premise is the existing atomic Character IR;
C2 owns Unicode decoding and source validity. These checks do not establish
stronger validity for arbitrary host-created values or a provider CPU bound.

A quoted copy of C17's 16 hex Characters exercises the membership value, not
the unchanged C17 helper, whose evaluated Set construction is a separate
development slice. Keep that distinction when recording source results.
As with Keyword membership, affected source composition is a separate check,
not a prerequisite authority-admission workflow for ordinary focused edits.

For changes to membership's composed behavior, use the existing paired
fixture preflight and affected Gravity source selection:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-authenticated-composition
```

The source subject has seven functions and six membership queries, including
both caught transfer positions. QST receives byte/parser checks only; normal
seed controls interpret the same emitted IR, not an independent raw-source
compiler. Supplementary and hostile Character carriers remain focused IR
controls rather than claims about this ASCII hex source subject.

### Finite String-Set membership

STD3 owns binary `contains?` on an independently established finite Set of
Strings and a String needle, including empty Sets. Edit the existing C7, C8,
typed emitter and runtime membership owners; no new constructor, instruction
or export is required. Start with the focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-string-set-contains-development-kernel \
  --exact sh08-string-set-contains-throw-consumer-development-kernel \
  --exact sh08-string-set-contains-dispatch-guard-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-string-set-contains-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-contains-lowering-development-kernel \
  --exact current-gravity-string-set-contains-runtime-development-kernel \
  --exact current-gravity-string-set-contains-transfer-fence-development-kernel --fail-fast
```

Retain source/binding/type/fact/ordered-ledger reconstruction, exact String
descriptor identity, full work accounting and existing Keyword/Character
regressions. Set then needle evaluate once, non-tail; transfers abort invocation.
Every normally evaluated operand must retain its domain evidence even when the
other operand transfers. Bare, unknown or mixed Set types and wrong needle
families are unsupported, not ordinary false results.

Runtime validates all members and the needle and rejects duplicate value
classes before searching. String equality is exact
and case-sensitive, with no normalization or host hash/lookup authority.
Remove the content/comparison reservation, representation-unit charges and
width/work quotas.

This runtime path assumes authenticated typing and type-conforming immutable,
finite Set providers whose cardinality and enumeration agree. Detectable
malformed-provider controls do not authenticate arbitrary host Set impostors.
Dishonest or infinite host/FFI providers have no claimed CPU or peak-memory
containment; do not add an eager snapshot to imply that guarantee. Membership
uses no insertion or conversion, evaluates no stored payloads and establishes
no flow, cardinality or enumeration facts. It adds no source-level allocation
effect; its internal accumulator does not establish physical host
allocation-freedom. Allocation in a Set-valued operand remains visible under
the existing constructor rules.

For a changed upstream/downstream composition, run the paired parser check and
the tiny source selection separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-contains-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-contains-authenticated-composition --fail-fast
```

The source selection exercises a String-annotated helper containing the actual
extension-Set expression, with `.gravity`, `.qst` and `.txt` callers, plus a
numeric-Set/String-needle C7 rejection. QST has owning-C2 byte/parser checks
only. Fresh development C6 sessions and same-catalog replay are not fresh public
C6 authority verification. This is the compiler predicate subexpression, not
execution of an unchanged full provenance validator or admission of a revision.

### Finite Integer-Set membership

The existing C7/effects/emitter/runtime owners support a literal finite Set of
`:gravity.type/integer` and the same generic Integer needle. Reconstruct the
`:sh08-integer-set-contains-v1` descriptor, source, call fact and ledger in each
consumer. Literal Set construction uses the existing lowerer, runtime
constructor and IR validator. Preserve once-left-to-right operands, Never
fences, complete normal domains, original-value uniqueness and literal Set
allocation effects. Do not infer Integer width/range from `integer?`, narrow
to i64, or add arithmetic/rounding to exact membership.

Focused selectors are `sh08-integer-set-contains-development-kernel`,
`sh08-integer-set-contains-throw-consumer-development-kernel`,
`body-integer-set-contains-development-kernel`, and SH01
`current-gravity-integer-set-contains-{lowering,runtime,transfer-fence,fixtures}-development-kernel`
plus `current-gravity-integer-set-construction-development-kernel`.
Retain the affected prior Keyword/String/Character/Symbol and constructor
controls. Existing finite checks cover work equality/minus-one, large Integer
values, duplicates, opaque members, malformed constructors and coherent forged
source/type/policy/call/ledger joins. The current partitions pass 20 / 2,742
and 3 / 211 separately; boundary 6 / 11 and LOC also pass. Retain the earlier
domain RED (43 failures), first integration (eight failures), and narrow
affected selection (one legacy expectation failure) under their original
`/tmp/integer-set-membership-*` log/EDN prefixes. Do not recast them as green.

The affected `current-gravity-integer-set-contains-authenticated-composition`
passed one test / 144 assertions in one 2 GiB JVM while holding the canonical
heavy FileLock and existing SH01 memory-heavy lease. It uses the public
`gravity.development-test-runner/run-cli!` inside `broker/with-lease` and
releases locks before exiting with its real result. G positive typing/effects,
assembly-required public C7 verification and closed runtime pass; G Decimal
negative typing rejects. Negative C7 verification was invoked without observing
its report status. QST is parser-only, and development C6 replay is not public
fresh C6. Retain the full source prefix
`/tmp/integer-set-membership-source-lease-20261021`; do not refresh unchanged
source evidence solely for a stamp. Its postflight records two polling gaps
above the planned 30s (35.42s/37.38s), not plan-perfect operational monitoring.

Remove Integer runtime work units, comparison reservations and fixed work-cap
rejections. The slice adds no carrier, Integer Set
`conj`/equality, reader boundary or String/foreign admission. Whole helpers,
public fresh C6, native, migration, self-hosting and release remain separate.

### Proven nonnegative Integer quotient

SAFE9 and STD5 govern binary `quot` over generic Integer operands. The
existing C7 owner admits an original nonnegative Integer literal or an
independently reconstructed normal collection count as dividend, and an
original positive Integer literal as divisor. It records
`:sh08-nonnegative-integer-quotient-v1`, `:proof-required` and
`:proven-safe`. Reconstruct source, ordered calls/edges, count law, numeric
constraint and call fact in C8 and the existing typed emitter. Missing or
changed operator descriptor kinds cannot bypass the scoped C7 consumer:
selection uses the actual authenticated `quot` binding.

The nonnegative Euclidean law proves `0 <= q <= n` and excludes division by
zero and value overflow. It establishes no representation allocation freedom:
normal invocation has `[:memory/allocate]` and a possible Integer-result
allocation record. Preserve operand effects, left-to-right evaluation and
Never transfers, including the proof for every normal operand. No i64
conversion, negative signed rounding, unknown-parameter range, alias proof,
reader grounding or surrounding multiplication is admitted. Runtime checks
back the source proof; they do not replace it.

Use the four `sh08-nonnegative-integer-quotient-*-development-kernel`
selectors, `body-nonnegative-integer-quotient-development-kernel`, and the
four SH01 lowering/runtime/fixtures/transfer-fence selectors. At `0cfcea6d`,
the affected batch passed 26 tests / 3,163 assertions. Its prior 26 / 3,160
run retained one stale bare-Set count rejection expectation; all 13 count-owner
functions were unchanged, and a Decimal negative plus positive Set controls
corrected that oracle. Independent review then reproduced one scoped-kind
bypass in 1 / 21. At corrected `1fcfa05c`, the expanded selector passed
1 / 27 and four existing shared-dispatch controls passed 4 / 85. These are
separate revisions and invocations, not one final combined green batch.
Earlier literal, C8-routing and analyzer-oracle failures remain under their
original `/tmp/nonnegative-integer-quotient-*` receipts.

The single affected source selection
`current-gravity-nonnegative-integer-quotient-authenticated-composition`
passed 1 / 73 at `1fcfa05c` in one 2 GiB JVM. One G function with two
quotients and one count reached fresh C7/C8 verification, assembly-required
public C7 verification and one closed entry, producing `[12 2]` with empty
stdout. A zero-divisor G negative rejected with `STD5002`; its fresh C7
verification was asserted and printed passed. The secondary C7-ANNOTATION
diagnostic is retained. QST is parser-only; development C6 session replay is
not public fresh C6. Preserve the complete
`/tmp/nonnegative-integer-quotient-source-lease-scope-corrected-20261021`
prefix: original PTY 18640, natural exit zero, 13 unchanged inputs, released
lease, free lock snapshots and no Java observed at postcheck. Continuous
monitoring had a maximum observed gap of 16.442 seconds and was not hard OS
containment.
Boundary checks passed 6 / 11; LOC passed with 2,022 files and zero exceptions.
Runtime remains 56 functions and all nine protected carrier predicates are
unchanged. Whole helpers, native, migration, self-hosting and release remain
outside this evidence.

### SHA-prefix comparison composition

Existing production composes original ASCII String `vec`, proven `subvec`
and finite Character-Vector equality. Test-only coverage at `0ef4de19`
passes 13 / 1,701, including 331 assertions across four new selectors.
Correct/wrong prefixes return true/false; nested Never, eager order,
allocation and source/fact/ledger joins are covered. LOC passes 2,022 files,
zero exceptions, maximum 250. No compiler defect or production change follows.

After the disjoint C11/SH13 join, the source run at `cad84ea9` observed
`[true false]`, fresh C7/C8 and assembly-required public C7 verification,
and a correctly rejected Vector/Map negative with fresh C7 verification.
Its aggregate remains FAILED: 1 / 84, 77 passing and seven failing assertions.
The test selected the atomic diagnostic instead of the actual Vector diagnostic.
Only those two oracle tokens changed at `5e139891`. A separate 1 / 30
retained-diagnostic DATA check passes, binding the original log hash and checking
missing/forged counterexamples; it invokes no fresh compiler or source producer.
It does not turn the original source selector green. Raw history and resource
receipts remain under `/tmp/sha-prefix-comparison-*`; no producer replay occurred.
Parameter/alias grounding, String count semantics, complete SHA helpers and
authoritative/native/self-hosting/release support remain unestablished.

### Bounded Vector slice after authenticated String conversion

The selected `subvec` development subset is ternary and JVM/meta. Its normal
receiver is the independently reconstructed `vec` of an original finite ASCII
String literal. Original nonnegative generic Integer literals must establish
`0 <= start <= end <= receiver-count`; the result preserves the selected
Character positions in start-inclusive/end-exclusive order. STD3 records the
general persistent Vector law separately from this implementation subset.
Aliases, parameters and arbitrary Vector receivers are not established.

C7, C8 and typed lowering reconstruct the parent and nested child evidence,
remove child-proof work accounting, and require unique original calls,
edges, call facts and producer rows. Physical IDs, Symbols and nonnegative
Integer ordinals precede selected lookups/comparisons. Every normal operand
still proves itself when another is Never; Never suppresses cross-operand
bounds, subtraction, result extent/member construction and direct parent
allocation. Allocation already incurred by a normal child stays transitive.
Normal empty/full slices retain possible allocation and declared allowance.
Lowering emits the existing three-argument builtin call, with non-tail ordered
operands. Runtime extracts the actual third value and checks actual Vector
and Integer values, bounded range and original closed nested/literal shapes
before slicing once. Runtime remains 56 functions; no runtime helper, IR
key/op, context key, cap or host builtin was added.

The original test-only RED `c49456f9` ran 1 / 63 with 49 failures.
Initial C7 passed 1 / 63; one later test namespace compilation exited 2 before
tests, then C7 passed 2 / 213. Stronger own-row and transfer/child-priority
controls passed 3 / 237; initial C8 passed 1 / 237. Initial lowering ran
1 / 251 with two failures because an empty selected-types mutation left two
valid results unchanged; the corrected oracle passed 2 / 295. Scoped C7
passed 1 / 23. The first 50-selector batch stopped at 20 / 1,239, zero
failures and one error: host `array-map` construction called opaque equality
before Gravity received the fixture. Direct PersistentArrayMap construction
corrected that test. The coalesced implementation `52d6db10` passed
50 / 3,369, including all ten slice selectors and prior controls.
Boundary passed 6 / 11; LOC passed 2,022 files, zero exceptions, maximum 250.
Raw prefixes `/tmp/vector-slice-*` retain each original receipt and failure.

Boundary and LOC were briefly launched together with the first SOURCE 2 GiB
focused batch and D's 512 MiB RED, exceeding the standing two-Java limit.
The two default-heap starts were SOURCE's, not D's; all ended naturally.
Later cheap JVM work was serialized. Those ordinary passes do not establish
resource-protocol compliance for that overlap or source-proof credit.

Both independent implementation reviews accepted `52d6db10`.
The one history-preserving join `1f2cc242` has parents `52d6db10` and
accepted main `3af20721` (PRs 197/198). It changed only C11 Gravity and the
SH12/SH13 tests and preserved all 12 owned slice files. The actual source
route, entire host source tree, checked_core.gravity, resolution.gravity and
P15 compiler.gravity were unchanged across the join. C11's path is compiler
inventory data here; the selected C6 route uses SH07 checked-core build/verify,
and the C11-named digest helper is host canonical-value hashing.
No repeated 50-test stamp or blanket proof reuse was claimed.

The exact 185-line method at `f2490680`, tree `0527a021`, was listed once
with 512 MiB, original PTY 69944/natural zero, executing zero tests.
After both method/resource reviews and a concrete one-shot authorization,
original PTY 98034 ran once and exited naturally zero: 1 / 68 passed.
One positive G function contains one nested conversion and one slice and
returns the seven Characters in `sha256:` with empty stdout. Fresh C7/C8,
assembly-required public C7 verification and one closed-plan entry are checked.
The G end-72 negative has a 71-Character receiver and rejects
STD3002/:vector-slice-bounds-required with actual/expected types and
Core/syntax/binding/span provenance. Fresh negative C7 verification was asserted
and printed passed; the secondary annotation diagnostic remains. Negative
C8/runtime were not invoked; QST remains parser-only, with zero typed products.

Both final actual/resource reviews and the coordinator accepted this exact
scoped receipt; its one-shot boundary is closed without replay.

Preserve `/tmp/vector-slice-source-lease-20261021`: immutable inputs/command,
preflight/locks, original log/EDN, monitor and postflight. Separate dependency,
monitor-plan, launch-eligibility and namespace-only receipts retain the exact
joined tuple, 15 input pins plus 11 route pins and actual launch environment.
All remained unchanged. The executable-string SHA excludes the final newline;
the 1,463-byte command-file SHA includes it. Source used 2 GiB, the canonical
lock and existing memory-heavy broker (27 ms wait, released), a 600-second
operational stop, 8 MiB log ceiling and 4 KiB tails. Ten observations had maximum
gap 17.474 seconds; no signal, retry or cap growth occurred. Six free-lock and
empty selected-process postchecks are snapshots, not hard OS/RSS/descendant
guarantees. Selected time was 137.352 seconds, runner 137.354 seconds and
namespace loading 13.548 seconds; the 152.103-second monitor interval is not a
provider CPU benchmark. Public fresh C6, parameter grounding, general Vector
receivers, full SHA execution, native, authoritative migration, self-hosting
and release remain unestablished by this slice.

### ASCII literal String vector conversion

STD4 defines String `vec` as conversion to a Vector with one Character per
Unicode scalar in original order. The initial C7/C8/typed-emitter/runtime slice
supports only unary original finite ASCII String literals, including empty and all NUL-through-DEL
values, up to the existing 1,024-member bound. It does not split UTF-16 units,
normalize or coerce text. Valid non-ASCII, supplementary and malformed host
surrogate input all remain unsupported by this implementation; rejection does
not classify every unsupported String as invalid Unicode.

Authenticate the original String occurrence, resolved `vec` binding, ordered
call/edge, nonnegative Integer edge ordinal, source IDs, types, unique ledger
and call facts. Remove width-dependent work reservations;
validate every physical Character against the fixed 128-value domain before
constructing positional Character types or the result. The proof uses the
existing bootstrap first/rest interface, not an unproved `vec` call or new host
scalar primitive. This is not a provider CPU or heap guarantee.

Normal invocation, including empty conversion, retains possible Vector
allocation and storage/provider/ownership obligations. Never preserves the
operand transfer and latent allocation contract, with no direct result
allocation or normal extent. Operands remain once-only and non-tail. C8 shares
the existing source context; runtime keeps existing builtin-call IR and checks
the actual String plus original closed literal instruction before conversion.
Runtime remains at 56 functions; nine protected carrier predicates are unchanged.

Use the five `sh08-ascii-string-vector-*-development-kernel` selectors,
`body-ascii-string-vector-development-kernel` and SH01's lowering, runtime,
transfer-fence and fixtures selectors. The earlier affected batch passed
39 tests / 2,420 assertions at `88e67a9d`. Review then found an unguarded edge
ordinal: its RED was 3 / 231 with 12 failures. The grouped three-consumer guard
correction passed 14 / 579 at `e642fb32`; do not relabel the earlier batch as
post-correction evidence. Boundary passed 6 / 11 and LOC passed for 2,022 files,
zero exceptions, maximum 250 lines at the earlier implementation revision.

The separate source selector
`current-gravity-ascii-string-vector-authenticated-composition` passed
1 / 61 at `e22f9f88`: one G function/call returned the seven Characters in
`sha256:` with empty stdout after fresh C7/C8 and assembly-required public C7
verification. The valid non-ASCII G negative reported the unsupported-subset
C7-ANNOTATION with provenance; fresh negative C7 verification was asserted and
printed passed. Secondary C7-ANNOTATION remains. Negative C8/runtime were not
invoked; QST is parser-only and development C6 replay is not public fresh C6.

Preserve `/tmp/ascii-string-vector-source-lease-20261021`: original PTY
57427, natural exit zero, 15 unchanged inputs, matching command/env/CWD pins,
released lease, six free-lock snapshots and no Java observed at postcheck. Ten
monitored observations had maximum gap 16.370 seconds; bounds were operational,
without hard OS/RSS/descendant guarantees. The one history-preserving main join
`990ce8c8` retained all 12 producer/method byte sequences. The actual SH07 route,
checked_core, compiler/emitter and projection were unchanged; the joined private
C6 Bool-parameter producer is a separate path. No post-join source run or broad
proof-validity claim follows. Original BMP/ASCII REDs, pre-test syntax failures
and corrected oracles remain in their raw receipts.
String parameters, aliases, general sequence conversion, non-ASCII support,
subvec, complete SHA/compiler helpers, native, migration and release are unproven.

### Count-bounded quotient scaling

SAFE9/STD5 permit the existing owners to prove binary
`(* k (quot (count collection) d))` when `k` is an original nonnegative
Integer literal, `d` is an original positive Integer literal and `k <= d`.
Reconstruct the ordinary count and quotient source, bindings, types, ordered
calls/edges, unique facts and numeric/cardinality ledger rows. The Euclidean
premise is `d*q <= n`, giving `0 <= k*q <= d*q <= n` for the original count
`n`. A quotient-only bound `q <= n` is insufficient. Missing proof, including
`k > d`, rejects with `STD5001`; the quotient owns its zero-divisor rejection.

Retain `:sh08-count-quotient-scaling-v1`, `:proof-required`, `:proven-safe`
and possible Integer-result storage. Normal direct/latent effects include
`:memory/allocate`. Aborted invocations retain every normal operand proof,
record no normal result bound or direct result allocation, and preserve the
original transfer. No alias/parameter range, commuted route, general product,
fixed-width conversion, negative rounding or whole-helper support follows.
Remove Integer work-unit accounting. C8 shares the numeric source context and independently joins
both quotient and count facts/ledgers; it adds no acceptance flag or context key.

Use the four `sh08-count-quotient-scaling-*-development-kernel` selectors,
`body-count-quotient-scaling-development-kernel`, and SH01's four
lowering/runtime/fixtures/transfer-fence selectors. The coalesced affected
selection passed 39 tests / 4,074 assertions at `9b19fae5`; boundary passed
6 / 11 and LOC passed for 2,022 files, zero exceptions, maximum 250 lines.
Earlier RED, routing, metadata-callback, test-oracle and pre-test compilation
failures remain under their original `/tmp/count-quotient-scaling-*` receipts.
Do not replace those outcomes with the later green batch.

The runtime uses existing builtin-call IR. The original nested count/quotient
success and exact closed literal/coefficient/divisor instruction shape dominate
the product. Already evaluated values are checked; `n` is not retained,
reconstructed, compared afresh or reevaluated. This defense does not admit
unproved source. Physical Symbol checks reject malformed function metadata
before hashing, equality or argument work. Fixed selected source IDs are checked
before retaining proof data. Callback controls cover these selected fields,
without a universal hostile-DATA/provider containment claim. Runtime inventory
remains 56 functions and nine protected carrier predicates are unchanged.

After one main join preserved all 11 reviewed byte sequences, the new
`current-gravity-count-quotient-scaling-authenticated-composition` passed
1 / 68 at `e5117b9d`. One G function/three calls produced `4` from the
nine-element receiver with empty stdout. Fresh C7/C8 verification and
assembly-required public C7 verification passed. The unsupported-factor G
negative rejected with `STD5001`; fresh negative C7 verification was asserted
and printed passed, with a secondary C7-ANNOTATION retained. Negative C8/runtime
were not invoked. QST is parser-only; development C6 replay is not public fresh
C6. Preserve `/tmp/count-quotient-scaling-source-lease-20261021`: original
PTY 94074, natural exit zero, 14 unchanged inputs including the joined C6
projection, released lease, free lock snapshots and no Java observed at postcheck.
Its maximum monitored gap was 16.409 seconds; limits were operational, without
hard OS/RSS/descendant guarantees. Whole compiler/reader helpers, native,
authoritative migration, self-hosting and release remain outside this evidence.

### Finite Symbol-Set membership

STD3's binary `contains?` also accepts independently established finite literal
Symbol Sets (zero through 1,024 members) and a Symbol needle. STD2's original
Symbol namespace/name value law applies, not name resolution or display-string
equality. Edit the existing C7, effects, typed emitter and runtime membership
owners. Start with `sh08-symbol-set-contains-development-kernel` and the
throw-consumer, ID-guard and step-guard selectors in
`gravity.self-hosting.sh08-function-call-type-test`, using the direct
`-J-Xmx2g -M:dev-test --namespace ... --exact ... --fail-fast` command above.
Run the two `body-symbol-set-contains-*` selectors in
`gravity.self-hosting.sh09-body-effect-test` and the lowering, runtime,
transfer-fence and lowering-ID-guard selectors named
`current-gravity-symbol-set-contains-*` in the SH01 runtime namespace.

Preserve the exact policy/constraint/source joins, independent full selected-family
ledger/census validation, Never transfers and existing family diagnostic priority.
Guard opaque IDs before lookup. A known legacy-family needle must not trigger
the new Symbol receiver probe. Ordinary producerless local-call rows are unselected by the Symbol
policy, not invalid; a selected malformed Symbol row still rejects.

Runtime checks the complete actual Symbol domain, coherent cardinality and
enumeration and uniqueness before hit or miss. Original Symbol equality
answers the query; remove UTF-16 display-unit accounting.
No Set lookup/hash authority, coercion or new builtin is needed. Operands run
once left-to-right and non-tail. Query nodes are pure; allocation in an operand
or enclosing result remains visible. Preserve actual provider semantics and
remove the work allowance; this does not establish hostile-provider CPU safety.

For changed composition, separately select
`current-gravity-symbol-set-contains-fixtures-development-kernel` and
`current-gravity-symbol-set-contains-authenticated-composition`. The tiny
Gravity subject includes one local Symbol-producing call and six membership
queries, plus a Character-Set/Symbol typing rejection. QST is parser-only.
Development C6 sessions/replay do not invoke fresh public C6 verification.
That finite-membership increment alone does not establish insertion or inferred
carrier composition; use the owning development sections above for those later
increments. Set equality, recursive transport, native execution and authoritative
migration remain separate evidence boundaries.

### Evaluated literal-Keyword Sets

For the bounded JVM slice, edit the existing typed emitter and runtime owners.
C7 already describes finite literal Set member types, and C8 already accounts
for normal constructor allocation, including the empty Set. The lowerer emits
the existing `:set-literal`/`:items` instruction from authenticated actual
Keyword children, not the parent's cached `:source-value`. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-c8-development-kernel \
  --exact current-gravity-literal-keyword-set-lowering-development-kernel \
  --exact current-gravity-literal-keyword-set-runtime-development-kernel --fail-fast
```

Check source/child/type joins, malformed IDs before structural equality,
original child failures, complete Keyword domains and duplicate
rejection before host insertion. Set items must participate in instruction
width and module-read traversal. L1's duplicate-literal rejection remains in
force; this is not Vector-to-Set conversion's deduplication behavior.
Remove the component width quota, work allowance and comparison/content
budget-fit requirement.

Quoted Sets remain inert. Direct evaluated Keyword/String-Set module initializers
use the separate [module initialization boundary](#module-local-literal-initialization-and-reads).
Character literals have the separate extension below; other member
families or arbitrary effectful Set children, Keyword List
conversion, `keys` ordering and whole-W5 execution are separate frontiers.
Pure literal children cannot independently observe once-only evaluation order.
When assembly/lowering/runtime composition changes, validate genuine accepted
and representative rejected source through those affected boundaries; ordinary
kernel edits do not require a fresh corpus or authoritative migration first.

Run the paired fixtures/duplicate-reader check before the affected source
composition, as separate existing-runner selections:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-authenticated-composition --fail-fast
```

The composition selects one accepted G program and one upstream-accepted,
lowering-rejected mixed Character/String-member program. The original
String-only rejection is historical: homogeneous literal Strings now have the
separate construction extension below. Q gets byte/parser checks, plus the
separate C2 duplicate diagnostic, not typed-product parity. Reference execution
uses the seed interpreter on the same emitted IR, not independent raw-source
compilation. Listing these commands does not claim that they passed.

### Evaluated literal-Character Sets

Use the same emitter and runtime owners. Existing C7 finite member types and
C8 allocation accounting apply without a new production pass or interface.
The bounded extension accepts homogeneous Character literal children,
including existing supplementary Character carriers; empty construction keeps
the legacy path. Start with:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-lowering-development-kernel --exact current-gravity-literal-character-set-runtime-development-kernel --fail-fast
```

Preserve complete source occurrence, ordered child, parent and type joins;
the cached aggregate value is not authority. Compile each original child once,
non-tail, and retain failed-child work. The instruction remains the existing
literal-only `:set-literal`/`:items` shape. Mixed families, arbitrary expression
children and evaluated Character-Set module initializers remain unsupported here.

Validate all Character values before comparing or inserting any member.
Remove runtime reservation formulas, representation-unit charges and width/work
quotas. Compare
semantic Character classes when rejecting duplicates: distinct carrier Maps
can have the same codepoint. Do not silently deduplicate a Set literal,
substitute host Map equality or add a new Unicode decoder. Source validity
remains C2-owned under the existing atomic Character IR premise.

Retain existing Keyword construction, Character membership and C7/C8
literal/allocation checks as affected regressions. Construction requires
`:memory/allocate`; membership's pure query does not erase that effect.
Source composition, current-closure migration and release evidence remain
separate from these direct focused checks.

For changes to the composed constructor, select the existing preflight and
source checks separately:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-authenticated-composition --fail-fast
```

Preflight checks the unchanged C17 helper form, paired bytes/C2 values and
duplicate supplementary-source rejection. The affected Gravity composition
requires actual evaluated Set instructions and allocation, including
supplementary values; the mixed-member negative must pass upstream checks
before typed lowering rejects it. QST is not a typed-product comparison.
Seed controls use the same emitted IR, not an independent raw-source compiler.

### Evaluated literal-String Sets

Edit the existing typed emitter and runtime owners. C7's finite literal member
types and C8's constructor allocation apply unchanged. The bounded JVM slice
accepts homogeneous, distinct literal Strings through the existing
`:set-literal` instruction; empty construction retains its legacy path. Start
with the two direct checks and the adjacent Keyword/Character constructors:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-string-set-lowering-development-kernel \
  --exact current-gravity-literal-string-set-runtime-development-kernel \
  --exact current-gravity-literal-keyword-set-lowering-development-kernel \
  --exact current-gravity-literal-keyword-set-runtime-development-kernel \
  --exact current-gravity-literal-character-set-lowering-development-kernel \
  --exact current-gravity-literal-character-set-runtime-development-kernel --fail-fast
```

Validate the complete String domain before comparisons. Lowering checks the
actual source and attribute Strings for equality or member distinctness,
preserving all source/child/type/evaluation joins. Runtime checks Gravity
String uniqueness before host Set insertion. Remove source/content comparison
reservations, representation-unit charges and member/work quotas.
Equal-content distinct objects are duplicate literal values, while
same-hash distinct Strings remain distinct. Do not reuse Vector-to-Set
conversion's deduplication semantics.

This constructor boundary enables an evaluated value such as `#{".gravity" ".qst"}`;
membership has its own checks, and neither establishes whole compiler-consumer
execution. Direct String-Set definitions use the separate
[module initialization boundary](#module-local-literal-initialization-and-reads).
Mixed/computed members and nested evaluated Set initializers remain unsupported.
Quoted Sets are inert. Constructor allocation remains
explicit, and affected source composition is separate from normalized checks
and authoritative migration.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-string-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-string-set-authenticated-composition --fail-fast
```

Preflight checks paired owning-C2 values and duplicate-literal rejection. The
composition selects one accepted extension-Set/count program and the existing
mixed Character/String program, which must pass upstream checks before lowering
rejects it. QST has byte/parser checks only. Fresh development C6 sessions and
same-catalog replay are not fresh public C6 authority verification. The old
String-only negative is now an accepted fixture; its historical rejection is
not a current language restriction or a reason to replay the old large positive.

### Finite Keyword-Set value equality

STD3 owns Set value equality. The bounded development slice uses binary `=`
for two independently established finite Sets whose complete member domains
are Keywords, including empty Sets. Each operand keeps its own type evidence;
equal type vectors, constructor identity and enumeration order are not equality
premises. Bare Set types and unknown member domains remain separate
implementation frontiers, not language-wide prohibitions. The String family
uses the separate comparison extension below.

Change the existing C7, C8, typed emitter and runtime owners. Start with the
owning kernels and affected adjacent equality regressions, not fresh upstream
proof construction:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-keyword-set-equality-development-kernel \
  --exact sh08-keyword-set-equality-dispatch-guard-development-kernel \
  --exact sh08-keyword-set-equality-throw-consumer-development-kernel \
  --exact body-keyword-set-equality-development-kernel \
  --exact current-gravity-keyword-set-equality-lowering-development-kernel \
  --exact current-gravity-keyword-set-equality-runtime-development-kernel --fail-fast
```

The implementation must validate both complete typed and actual domains,
including distinct Keyword value classes within each actual operand, before
returning a cardinality or member mismatch. Runtime member matching uses the
existing guarded Keyword value-equality leaf, not host Set equality, hashing
or printed-spelling identity. Remove pairwise/content prepayment and component
allowances. JVM representation units do not define semantic equality.

Check paired fixture bytes/parser interpretation cheaply. When the
typed/effected/lowered composition changes, run the affected source selector
separately from the edit loop:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-equality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-equality-authenticated-composition --fail-fast
```

Use genuine quoted Sets and ordinary function returns rather than inventing an
accepted verification result. Ordinary operands retain left-to-right once-only
evaluation, effects and transfers; stored members remain inert and originals
unchanged. These checks do not establish evaluated Set-literal lowering,
Keyword List conversion, `keys` ordering, whole-W5 execution, other-target
support or authoritative migration. Record actual outcomes in the backlog;
listing a command is not evidence that it passed.

### Finite String-Set value comparison

STD3's Set equality and STD2's binary inequality complement govern this
extension. Change the existing C7, C8, emitter and runtime Set-comparison
owners. The bounded domain is two independently established finite literal
String-Set types, including empty Sets; bare Sets, mixed families and unknown
member domains do not acquire support from this rule. String equality is exact,
without normalization, locale, coercion or printed-spelling comparison.

Start with the existing direct owning checks, alongside the affected Keyword
Set and Keyword-Set Vector regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-set-equality-development-kernel \
  --exact sh08-string-set-equality-throw-consumer-development-kernel \
  --exact sh08-string-set-inequality-development-kernel \
  --exact sh08-string-set-inequality-throw-consumer-development-kernel \
  --exact body-string-set-equality-development-kernel \
  --exact current-gravity-string-set-inequality-source-development-kernel \
  --exact current-gravity-string-set-equality-lowering-development-kernel \
  --exact current-gravity-string-set-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-string-set-inequality-transfer-fence-development-kernel \
  --exact current-gravity-string-set-equality-transfer-fence-development-kernel \
  --exact current-gravity-string-set-equality-runtime-development-kernel --fail-fast
```

Validate both full member domains and within-operand value distinctness before
cardinality or membership mismatch. Remove content/pair reservations and
remaining-allowance accounting. Native String representation units do not
define semantic equality or guarantee provider CPU/heap safety. Host
Set equality, lookup and hashing do not decide the result. The hosting path
must itself support every operation used by these owning functions.

Keep Keyword and neutral-empty policy identities unchanged. Widening shared
Set helpers must not admit String-Set leaves in Vector equality or inequality:
those callers retain explicit Keyword-only fences. `not=` keeps original
source/type facts and the owned equality/branch complement, with ordinary
left-to-right, once-only, non-tail operand evaluation and transfer handling.
Actual affected source composition remains separate from these normalized
checks; no constructor/membership proof, whole B14 execution or authoritative
migration follows from this comparison slice. Listing commands is not a
passing result.

For changed composition, select the tiny paired definition witness and its
authenticated composition separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-comparison-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-set-comparison-authenticated-composition --fail-fast
```

The witness checks exact B14 definition text and its isolated parsed form,
not whole-B14 readability. Three quoted comparison Sets remain inert and
separate from the single evaluated initializer. Q receives parser checks only;
fresh development C6-session replay does not replace public fresh C6 admission.

### Map family classification

STD3 owns unary `map?`. The bounded JVM implementation classifies an
independently established Map as true and other supported closed value families
as false. Unknown remains unsupported, and Never prevents invocation. Normal
operand evaluation happens once in a non-tail position with effects preserved;
classification does not evaluate stored keys/values, allocate a collection or
grant key/member, cardinality, presence or ordering facts.

Edit the same C7, C8, typed emitter and runtime owners used for the adjacent
collection queries. Start with the existing direct focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-map-predicate-development-kernel \
  --exact sh08-map-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-map-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-predicate-lowering-runtime-development-kernel --fail-fast
```

These exercise owning Gravity implementations using normalized development
inputs, including source-bound reconstruction; they do not construct a fresh
source composition or admit a module revision. Changes to the public
typed/effected/lowered composition require its affected accepted and rejected
source checks separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-predicate-fixtures-development-kernel \
  --exact current-gravity-map-predicate-authenticated-composition --fail-fast
```

`keys`, collection conversion/equality and unchanged
W5 exact-key validation remain separate slices, not predicate-result facts.

### Keyword family classification

STD2's `gravity.core` owns unary `keyword?`. The bounded JVM implementation
accepts independently established closed operand types: actual Keywords return
true and other supported values return false, including the actual nil branch
of a nil-or-Keyword operand. Unknown and Dynamic remain unsupported, not
language-wide bans. Keyword-like String/Symbol spelling creates neither a
Keyword nor a binding, and a qualified Keyword confers no capability.

Edit the owning C7, C8, typed emitter and runtime files. Use the direct focused
checks before affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-predicate-development-kernel \
  --exact sh08-keyword-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-keyword-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-predicate-lowering-runtime-development-kernel --fail-fast
```

The query preserves once-only non-tail operand evaluation, effects and control
transfers; Never prevents invocation. It evaluates no stored payloads, adds no
collection allocation or own capability requirement, and grants no type-flow
refinement. Normalized reconstruction and JVM/WASM eligibility checks are not
actual target execution or fresh source products. Use a changed source
composition when the public typed/effected/lowered boundary changes:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-predicate-fixtures-development-kernel \
  --exact current-gravity-keyword-predicate-authenticated-composition --fail-fast
```

Compiling one predicate/projection subexpression is not execution of an unchanged W5
validator, current-closure migration or self-hosting completion.

### String family classification

STD4's `gravity.text` owns unary `string?` semantics behind the existing core
compatibility spelling. Edit C7, C8, typed lowering and runtime, then run:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-string-predicate-development-kernel \
  --exact sh08-string-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-string-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-predicate-lowering-runtime-development-kernel --fail-fast
```

Classification distinguishes actual Strings, including empty Strings, from
other supported independently established closed values. Nil-or-String operands
classify their actual value; Unknown and Dynamic remain implementation
frontiers. It preserves once-only non-tail operand evaluation, effects and
transfers without conversion, payload execution or type-flow refinement. It
does not independently validate Unicode or establish normalization, length or
indexing facts; existing String validity obligations remain at their owning
boundaries. It grants no new collection allocation, capability, export or
profile permission.
Text/view/Bytes or host-carrier convertibility does not establish String type.

Use changed source composition when the public typed/effected/lowered boundary
changes, and record its actual result separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-predicate-fixtures-development-kernel \
  --exact current-gravity-string-predicate-authenticated-composition --fail-fast
```

This selector constructs the two positive Gravity programs and one Gravity
arity rejection. Paired `.qst` fixtures receive byte/parser checks only; no
fresh paired typed/effect/lowering/result equivalence is claimed. Both positive
programs retain current C7/C8 verification and execution guards, and both
forgery checks remain. Compiling the W5 runtime record's String field checks
is not executing its unchanged validator, admitting
a compiler revision or completing self-hosting.

### Character family classification

STD4's `gravity.text` owns unary `char?` semantics behind the existing core
compatibility spelling. Character values classify true, including supplementary
characters. Strings, even one-character Strings, and ordinary Maps resembling a
Character carrier classify false. The authority is the independently established
Gravity operand type, not host representation or convertibility.

The implementation owners are C7's `sh08-chp-*` helpers, C8's `body-chp-*`
helpers and the emitter's `p15-s23-chp-*` helpers. Start with the focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-character-predicate-development-kernel \
  --exact sh08-character-predicate-throw-consumer-development-kernel \
  --exact sh08-character-predicate-id-guard-development-kernel \
  --exact sh08-character-predicate-step-id-guard-development-kernel \
  --exact sh08-character-predicate-unknown-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-character-predicate-source-development-kernel \
  --exact current-gravity-character-predicate-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-character-predicate-lowering-runtime-development-kernel \
  --exact current-gravity-character-predicate-inert-payload-development-kernel \
  --exact current-gravity-character-predicate-other-producer-id-guard-development-kernel --fail-fast
```

These checks exercise normalized typed/effected/lowered inputs and the existing
runtime, not fresh source compilation. Operand evaluation stays once-only and
non-tail; effects and transfers survive, and Never prevents invocation.
Nil-or-Character uses existing nil equality and conditional instructions, not a
new runtime predicate. Unknown, Dynamic and unsupported type families remain
implementation frontiers. No coercion, payload execution, type-flow refinement,
Unicode validation, normalization or text-boundary fact follows from this query.
Changes to the public typed/effected/lowered composition require the affected
source composition check separately; neither level admits a stable compiler
revision or establishes self-hosting completion.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-character-predicate-fixtures-development-kernel \
  --exact current-gravity-character-predicate-authenticated-composition --fail-fast
```

The source check selects one accepted `.gravity` program and one arity
rejection. Its fresh development C6 session and same-catalog replay are not a
fresh public C6 authority verification. The paired `.qst` files have byte/parser
checks only, not paired typed/effect/runtime equivalence. Keep actual outcomes
separate from the existence of this selector.

### Legacy bounded self-recursive body analysis

The same body interface also recognizes C7's existing named, capture-free,
positive-fixed-arity positional-call/literal-base proof. Positive arity means a
nonzero parameter count, not positive argument values. Ordinary `if` retains
L2 truthiness; the Bool-only match-guard policy is not an extra `if` restriction.
The original recursive
proof, call facts, component, positional arguments, literal base and external
parameter evidence must agree with the complete consumed core. A direct
imported producer uses the same per-module analysis and retains its own source
and artifact coordinates; it is not replaced by a synthetic local result.

Start with the direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-recursive-proof-development-kernel \
  --exact body-effect-recursive-fixed-point-development-kernel
```

The solver starts function summaries at the empty effect set, then derives a
least fixed point without a shared node-attempt budget. Only the
authenticated self-body dependency is cut internally while a round is solved.
The final graph retains that dependency, including a recursive branch under a
literal false condition, and its equations are checked against the complete
graph. Equation equality alone is insufficient: fresh candidate verification
reconstructs from bottom so an inflated, nonleast fixed point cannot pass.
Remove caller/producer work pools from classification, iteration, reconstruction
and equation checks; retain the actual least-fixed-point validation.

Historical test-only reconstruction counters on the unchanged tiny recursive input:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-recursive-fixed-point-development-kernel --fail-fast
```

Its original-delegating observer records two passes, each rebuilding two edge
rows and one function row, with 11 readiness attempts and zero false results.
Each pass repeats 13 invocation predicates, 11 support checks and 11 order checks;
general/import call validators are unvisited. Full observed/unobserved results
agreed at generous, exact 44 and rejecting 43 budgets in the historical metered
implementation. Remove those allowance assertions from the operative test; retain
full least-fixed-point and tamper checks. These old counters establish no
unmetered correctness or speedup. Production changes belong to the actual
`effects.gravity` owner and remain subject to its current platform restriction;
this historical measurement selects no implementation or alternate writer.

The affected real-source composition is separate from these normalized kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-recursive-authenticated-paired-composition --fail-fast
```

It uses the existing Integer and String recursive fixture pairs, a paired caller
importing both producers, and the existing real mutual-recursion rejection
fixture. Fresh C7 products and verification precede body analysis. Test results
belong in the slice backlog after the actual run; the command itself is not
evidence of a pass. The unchanged public-bundle triggers above still apply.
This bounded analysis does not prove program termination, recursion depth,
tail-call optimization, mutual recursion, callable recursion, completed thrown
effects, full effect legality, target execution or authoritative migration.

### Capture-free callable body analysis

The body interface recognizes C7's existing named identity/apply-one proof,
not arbitrary higher-order values or the older fixture bridges. The callable
parameter is allowed only at the exact proof-selected apply slot, with the
original nested function type. Other callable parameters, callable returns,
captures, multi-hop dispatch and higher-order recursion remain separate work.

The inner call keeps its original nonlocal C6 edge and nil direct callee. A
separate selected-body dependency joins the outer function-value argument,
callable parameter, inner operator/argument, selected identity function and
actual body. Carrying or constructing a function value does not execute it;
the inner invocation propagates its derived body effects. C7 declaration
allowances do not replace that derivation. Pending throws and legality remain
pending.

Run the direct checks first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-callable-proof-development-kernel \
  --exact body-effect-callable-dependency-development-kernel
```

The affected real composition uses
`body-effect-callable-authenticated-paired-composition` in the same namespace.
The separate `body-effect-callable-current-c7-rejections` selector covers the
real nonfunction and capture fixture pairs.
It consumes fresh C7 construction/verification and the original complete
binding. A direct scalar import can select the producer's identity export while
the complete producer graph also contains its validated apply body. The
selected imported function must still have a first-order signature; this is
not an imported callable API. Original producer coordinates and full private
body coverage remain required. The existing aggregate bounds, unchanged public
C8 forwarding checks and public-bundle rerun triggers apply. Actual results
are recorded separately in the slice backlog, not inferred from these commands.
Current C7 diagnostic precedence, rather than historical fixture comments,
defines the rejection oracle. An oracle-only correction does not require
repeating already completed positive composition when production and fixture
bytes are unchanged; retain the failed aggregate and run the affected negative
selector separately.

### Compiler-artifact module execution

The temporary seed interpreter has a separate, explicitly unadmitted module
bundle input for developing compiler code across real namespace boundaries.
This is not the checked C7/C8 user-program path. Keep the existing raw
three-argument plan constructor for a single source; it does not invoke the
checked frontdoors. For an ordinary caller with an explicit dependency, use:

```clojure
(bootstrap/p15-s23-stage2-compiler-artifact-plan
 emitter caller-path caller-text
 {:caller-resolution-artifact caller-c5
  :dependency-plans [independent-callee-plan]})
```

Build `caller-c5` with the ordinary module-input interface above. Build a leaf
plan independently with the raw three-argument constructor and the same
emitter. For an acyclic chain, retain each dependency's genuine ordinary C5
inputs and supply one plan for every nonroot module. A nonleaf plan can come
from that module's own bundle `:plan` entry; the root independently recompiles
and compares every supplied plan, so the projected entry is input DATA, not
accepted authority. Pass the resulting flat bundle to the existing
`p15-s23-stage2-runtime-execute-function` interface. Source reconstruction,
actual C5 public exports/origins, separate plans and exact callsite routes are
checked; a matching name or recomputed hash alone does not establish a link.
The public invocation reconstructs the bundle, and linked-call route and arity
checks precede argument evaluation. Local function names remain module-local.
The two supported module pairs are development construction/execution subsets,
not full profile conformance or host-capability grants. Ordinary C5 still owns
target, capability and cross-profile import diagnostics; this interface does
not turn a meta/hosted edge into an implicit facade.

Transitive calls use this same four-argument interface, not nested bundles or
a separate runner. The root's retained C5 inputs determine the complete
closure; missing, extra or conflicting inputs, and inputs that fail fresh
reconstruction, reject. Full-DATA
deduplication is not accepted-result caching: every unique module still enters
the original fresh source verifier, whose own dependency replay remains.
Per-module routes preserve lexical identity when different modules use the
same function name and callsite path. Remove module, nonroot-plan, aggregate
route and per-plan instruction capacity quotas.
Cycles still reject in ordinary C5. This does not add imported DATA reads,
general module initialization or checked user-call effects.

For this explicit development interface, the entry is an unqualified function
name in the freshly reconstructed root module. It may be private or public,
matching the existing single-source interpreter interface; it is not a lookup
through dependency modules. Missing names, qualified names, non-symbol entries
and literal DATA declarations are not callable entries. Function arity and the
lexical module scope still apply. Cross-module calls remain limited to actual
public exports and their validated routes, even when the root entry is private.
This permits focused compiler-kernel checks after extracting a stage-owned
namespace without exporting test-only functions or rewriting source. It does
not grant checked user-program execution or stable private-import access.

The subset is fixed-arity, unannotated `defn` modules in `:meta/:jvm` or
`:hosted/:jvm`, with caller and dependency using the same profile and target.
It supports direct qualified imported calls and source-local literal DATA declarations
of the form `(def name literal-data)`. Scalars, nested vectors/maps/sets and
exact quoted DATA are materialized before body compilation for unqualified
module-local reads; quoted symbols,
lists and call-looking payloads remain inert. Parameters and lexical bindings
take precedence over module DATA. Actual C5 var export descriptors are retained,
but exported DATA is not callable and imported DATA reads remain unsupported.
This is an immutable seed subset, not a language-wide restriction on `def`.
Arbitrary initializer calls, bare-symbol aliases, forward/dependency initializer
references, duplicate definitions, variadics and source/seed operator-shadowing
mismatches reject. Imported calls in symbol-only sequential `let` and `loop`
initializers and bodies are supported, including nested scopes, shadowing and multiple body
forms. Initializers see earlier bindings, not later ones; argument evaluation
remains once, left-to-right. Loop calls also retain their distinct paths through
conditions and `recur` arguments; nearest targets, tail position and arity still
come from the existing recur validator/runtime. Imported calls under nested
functions, maps or sets are not yet supported; function-body vector expressions
are supported. Forwarding nested DATA through a parameter is distinct from those
executable forms. Quoted DATA remains inert. General module initialization and
checked runtime linking are separate work, not language-level prohibitions.

For module-call path or sequential-let changes, start with the dedicated cheap
checks and then the affected real composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-lexical-call-test \
  --exact stage2-module-let-fixture-parity \
  --exact stage2-module-let-call-path-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-lexical-call-test \
  --exact stage2-module-let-authenticated-composition --fail-fast
```

The second command constructs genuine Gravity caller/callee C5 products and
executes the development module bundle. The paired `.qst` fixtures establish
byte/parser parity only, not a second authenticated execution. Private imports,
wrong arity and a coherently resealed route substitution reject. This temporary
Clojure-seed composition does not establish checked user-call effects, runtime
ownership, authoritative migration or self-hosting.

For loop-call paths and recur diagnostic changes, use:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-loop-call-test \
  --exact stage2-module-loop-fixture-parity \
  --exact stage2-module-loop-call-path-development-kernel \
  --exact stage2-module-loop-recur-diagnostics-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-loop-call-test \
  --exact stage2-module-loop-authenticated-composition --fail-fast
```

The normalized kernels cover paths, sequential scope and complete diagnostic
parity with the original body validator. On malformed loop bindings, the
walker delegates to that validator over the whole definition so an earlier
recur or throw error is not hidden by a later loop error. Source location comes
from the verified module, not a new authority channel. Valid compilation does
not add another validation pass. The separate composition exercises actual
outer and nested loop backedges, ordered effects and qualified imports; it is
not a termination proof or checked user-call effect analysis.

For closure reconstruction or transitive-route changes, use the separate
focused selection and one affected three-module composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-fixture-parity \
  --exact stage2-module-transitive-closure-development-kernel \
  --exact stage2-module-transitive-routing-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-authenticated-composition --fail-fast
```

The cheap kernels use normalized routing DATA and explicit substitutions;
they do not demonstrate original C5 verification. The composition constructs
genuine leaf, middle and root C5 artifacts, then exercises three-module execution,
source-bound exports, private-import rejection and a coherently resealed
transitive-route mutation before argument effects. Paired Q sources provide
byte/parser parity only. Run this composition when those boundaries change,
not for unrelated semantic edits or to refresh authoritative evidence.
For imported calls inside ordered vector expressions, use:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-vector-call-test \
  --exact stage2-module-vector-fixture-parity \
  --exact stage2-module-vector-call-path-development-kernel \
  --exact stage2-module-vector-recur-diagnostics-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-vector-call-test \
  --exact stage2-module-vector-authenticated-composition --fail-fast
```

Vector elements retain zero-based callsite paths and left-to-right evaluation,
including nested vectors, let initializers and loop bodies. Quotes remain
inert; vectors inside blocked function/set contexts do not escape those
fences. An enclosing Map requires the derived context described below.
This is function-body expression support, not executable top-level
`def` initialization. Declared allocation/IO metadata does not establish
checked effects or provider permissions; the seed boundary remains unadmitted.

Effects extraction still needs its exact remaining call-context coverage and a
concrete source/token-bound fit; transitive, loop and vector routing do not
close that dependency or the retained C9 source failure.

For evaluated Map literal order in the module-development constructor, edit
`00575-p15-s23-stage2-module-map-order.clj` and its existing recur, expression
compiler and `00580` reconstruction consumers. Start with:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-order-test \
  --exact stage2-module-map-order-fixture-parity \
  --exact stage2-module-map-order-development-kernel \
  --exact stage2-module-map-order-diagnostic-kernel --fail-fast
# Only when this source-order/reconstruction boundary changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-order-test \
  --exact stage2-module-map-order-authenticated-composition --fail-fast
```

L2 expression order is not a Map enumeration guarantee. After fresh original
C5 verification, the constructor joins retained C2 child occurrences to the
actual `defn` expansion and derives Map evaluation/diagnostic order separately
from host-map callsite coordinates. Public reconstruction repeats the join;
there is no new caller-supplied proof input. No-import dependencies are also
reconstructed with the current source-order context: incompatible legacy raw
plans reject.

The module subset supports distinct literal String or Keyword keys, including
mixed Maps, in evaluated function-body Maps. Computed, numeric, Symbol and
collection keys remain unsupported here, not forbidden by L2. This supersedes
the initial Keyword-only module restriction; it does not widen the separate
canonical typed-Gravity Map subset. Quoted Maps remain inert. Remove the
traversal depth and visit quotas. Raw three-argument/B51 behavior and
public bundle interfaces remain unchanged. This Map-order-only correction did
not enable imports inside Maps; the subsequent Map-value increment is below.
Neither increment establishes checked effects or Effects token-bound fit.

For qualified imported calls in supported Map values, change the same
`00575` Map-order component and existing `00580` module-source/module-bundle
owners. Use the dedicated focused checks first:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-call-test \
  --exact stage2-module-map-fixture-parity \
  --exact stage2-module-map-reference-development-kernel \
  --exact stage2-module-map-context-fences-development-kernel \
  --exact stage2-module-map-wrapped-arity-diagnostic-development-kernel --fail-fast
# Affected source resolution -> module construction -> runtime boundary:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-call-test \
  --exact stage2-module-map-authenticated-composition --fail-fast
```

Keep three coordinates distinct: original C5 host enumeration, seed-host
callsite paths, and C2 source evaluation order. Repeated callee names cannot be
joined by symbol counts or a source-ordered zip. For Map-bearing functions,
project the complete actual expanded owner syntax and match every original
reference, including unqualified references, by request ordinal, identity,
position and spans. Bridge literal keys to seed-host slots, then check imported
call origins and arity in source order using those original C5 references.
Original binding, scope, export, profile and target facts remain C5-owned.

Map values compose with existing let, loop, recur-argument and Vector paths.
Quote payloads remain inert. Set and nested-function fences remain inherited
both inside and outside Maps; a missing derived context does not enable the
new walker. Public construction derives it after fresh verification, never
from a caller's acceptance flag. Existing recur/throw pass precedence is not
replaced with a global cross-pass source-error ordering rule.

For literal String/Keyword key-domain changes, the owner remains `00575`;
the source/seed layout handoff belongs to existing `00580` module-source.
Use its dedicated paired-fixture, reference and rejection checks first:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-fixture-parity \
  --exact stage2-module-literal-map-key-reference-development-kernel \
  --exact stage2-module-literal-map-key-rejections-development-kernel --fail-fast
# After the authenticated-source/seed layout handoff changes:
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-seed-handoff-development-kernel --fail-fast
# Only for the affected source/composition boundary:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-authenticated-composition --fail-fast
```

Keep C2 scalar kinds and decoded values joined without coercion: String
`":tag"` and Keyword `:tag`
are different keys. Lost duplicate occurrences, including differently escaped
spellings decoding to the same String, reject the occurrence bijection rather
than silently borrowing host materialization. These supplied-record checks do
not replace genuine reader/source composition.

Equal Map values can have different C5 and seed host layouts. Select seed
definitions from the independent macro forms only after the complete equality
check against verified forms; retain original syntax/reference authority.
Derive C2-to-seed slots from that actual seed definition, rather than relaxing
the strict host-key join or assuming value equality fixes entry coordinates.

Full reference projection and traversal validate actual expansion/header
shapes rather than inventing C5 references. Remove the local work allowance
and arbitrary depth quota. Raw3/B51,
runtime and public interfaces are unchanged. No new C5 resolver, admission
lane or proof cache is introduced; use one changed composition, not an
unchanged source matrix, when this producer/consumer boundary changes.

Use the existing runtime test namespace, not an admission runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-root-private-entry-development-kernel \
  --exact compiler-module-route-development-kernel \
  --exact compiler-module-seed-shape-development-kernel \
  --exact compiler-module-b51-dispatch-development-kernel
# After literal declaration or local lookup changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-literal-data-development-kernel \
  --exact compiler-module-literal-data-existing-source-kernel
# Whole current runtime source: DATA conservation and real runtime execution:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-whole-runtime-data-development-kernel \
  --exact compiler-module-literal-data-development-kernel
# Run after module input, compilation or dispatch behavior changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-authenticated-source-and-runtime-composition
# Root-entry behavior, including private root functions and public dependencies:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-root-private-authenticated-paired-composition
# Affected literal DATA composition and actual C8 compatibility:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-literal-data-authenticated-composition \
  --exact compiler-module-literal-data-actual-c8-composition
# After the module profile/declaration/origin joins change:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-profile-source-development-kernel \
  --exact compiler-module-profile-origin-development-kernel
# Affected hosted caller/leaf composition, with one meta/JVM regression:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-hosted-authenticated-paired-composition
```

The direct kernels use normalized inputs and dispatch instrumentation; they
are not authentic source or admission evidence. The separate composition
constructs actual caller/callee C5 artifacts and independent plans. Bundle
records retain the temporary Clojure seed/interpreter boundary and unchecked
effects status. Neither path grants SOURCE/B51 authority, checked imported-call
legality, target support, migration acceptance or seed retirement.
The compile-time literal table is not another raw-plan field: actual source
identity binds unused declarations, while embedded literal instructions bind
values a function reads. Changing an unused catalog therefore changes module
identity without changing an otherwise identical function-semantic projection.
The whole-runtime check parses compatibility catalog DATA and compiles the
entire actual `gravity.runtime` leaf once, not the older selected-function
projection. The separate two-file composition validates the compatibility
module's actual bundle and routes. Explicit catalog quotes preserve symbolic
DATA without evaluating labels as variable references. Conservation checks are
development regression oracles, not refreshed authority pins. After evaluated
vector support intentionally changes one evaluator body, the current check
compares the unchanged 21 leaf/29 reconstructed original function bodies and
all catalog DATA; functional vector checks cover the changed owner. The earlier
whole-body conservation result stays attributed to its original revision. The
historical pinned runtime adapter remains separately stale and fail-closed;
repairing its byte counts, definition set or contract hashes is not a prerequisite
for this current-source check.
Do not refresh authority pins to account for redundant development metadata.
Run only the affected composition rather than replaying unchanged source proofs
to refresh an aggregate result.

Ordinary focused tests do not require fresh authority proofs, frozen migration
fingerprint refreshes, workstream receipts, broker setup or a new routing record.
Current-source execution must retain actual identities; it must not inherit a
stale frozen authority claim. Source, dependency, tool and policy changes still
conservatively invalidate prior authoritative evidence. Record implemented
behavior and focused results separately from migration and release status.

For example, `sh07-b47-c7-stage2-plan-identity-is-exact` in
`sh07_c7_type_source_coverage_test.clj` pins a historical C7 source revision,
247 functions and exact plan/function hashes. It is a migration identity check,
not the current C7 semantic loader. Do not refresh those pins or rebuild that
proof before a local type-rule edit. Changed source conservatively invalidates
its current-authority claim. Independent current-source checks in the same
file, such as control-form arities, remain applicable; this is not a blanket
exclusion of the test namespace. Current C7 semantic tests must execute the
actual dependency bundle and retain private-kernel regression coverage.
Ordinary record/match fixture checks compare that bundle's retained root source
path/text with the current file and inspect its actual function members. They
must not require refreshing a stale source-byte/hash golden merely to run a
semantic regression. Keep fixture parity, current-source fidelity and semantic
negative checks; retain exact historical fingerprints in their migration tests.

The stage2 interpreter's existing pure DATA predicates, collection operations,
integer quotient and vector slicing are ordinary execution facilities, not
plan-admission gates. Inspecting an argument with `map?` or `contains?` does
not authenticate the caller or its result. Both generic and unary dispatch keep
their arity, evaluation-order, normalization and contained-error behavior.
Historical pinned-plan checks, compiler-internal instruction provenance, SOURCE
admission and external-call authority remain separate checks at their owning
boundaries. No module-name spoof, caller authority flag or fresh migration is
needed to execute a current-source C7 development test. This supersedes old
attempt-specific expectations that raw pure-predicate execution itself proves
or requires M3 admission; their immutable evidence remains historical.

This section supersedes historical monitor/attempt instructions insofar as they
treated supplemental admission policies as prerequisites for ordinary semantic
development. Immutable attempt reports, terminal records and old proofs remain
history; they are not rewritten or reactivated. Workstream admission still
governs actual admission. Impact planners, caches, warm workers, profiling and
bounded resource runners below are optional tools, not the default inner loop.
Bound and serialize genuinely heavy work to protect the host, rather than
requiring lightweight tests to maintain heavy-run admission state first.

## Repository and explicit admission checks

### Product construction versus scheduling overhead

The ordinary C5 adapter shares identity chunks only within one constructor or
bounded-verifier invocation. Its existing public identity/summary helpers still
recompute freshly and both fresh C5 gates on the public C6 path remain. When editing these equations,
start with the bounded equation-only kernel, then select the affected real
consumer separately:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-product-identity-chunks-preserve-old-equations --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-invocation-local-chunks-preserve-fresh-verification --exact sh06-authenticated-resolution-is-the-c6-consumer-boundary --fail-fast
```

The first compares the old chunk/identity/summary equations at bounded chunk
edges. The second constructs a genuine tiny artifact and exercises fresh
verification, mutations and the public hosted-C6 consumer. It is not the
large checked-Core source subject. Preserve malformed/preflight, stale-envelope,
binding/order/span and source-read regressions when changing their owners.
The [evidence reconciliation](self-hosting-sh00-sh06-evidence-audit.md#bounded-invocation-local-projection-sharing)
records the actual selected results and their limits. This local factoring is
neither acceptance caching nor measured proof that chunk construction dominates
Core composition; do not rerun the unchanged large subject to refresh a stamp.

Some SH-07 tests build large authenticated products before reaching a semantic
assertion. The backlog records a 9,172,831-node reader-source template and a
later 17,681,671-node B47 template. Another lane or lock does not remove that
construction cost. The parked SOURCE draft separately exposed repeated scans
over 2,994,224 blob bytes: seventeen eight-unit scans alone would charge
407,214,464 metadata operations against its unchanged 67,108,864 cap. This is
a deficit in that draft, not a prerequisite for ordinary language development.

The quoted-pattern results documented in `7ccc15c3` separate this further: four
selected tests executed in 314.985 seconds after 6.990 seconds of namespace
loading. The accepted fixture artifact was already shared between test vars;
the current-source compiled plan is also reused. Static tracing identifies
repeated form-value traversal in `sh07-core-build-form-tree`, argument/result
metadata stripping in `sh07-core-execute!`, producer replay in template and
resolved verification, and canonicalization/hashing in digest resolution.
Repeated DATA fields can share objects: these call sites do not establish
allocation counts, runtime dominance or a speedup opportunity by themselves.

The imported-call type slice provides another concrete separation: its five
direct kernel tests executed in 0.893 seconds after 7.131 seconds of namespace
loading, while its one real-source composition test executed in 950.667 seconds
after 6.963 seconds of loading. The latter constructs paired modules and three
source mismatch variants and checks fresh composition and replay. These are
different workloads, not a speedup comparison or a measurement of which
construction phase dominates. Use direct kernels for iteration and this
affected composition for interface changes; adding scheduling machinery would
not remove its product-construction work.

The homogeneous-List composition later spent 1,211.036 seconds in its selected
test; the prepend slice's combined focused batch spent 13.700 seconds in test
execution. These are different workloads, not a speedup comparison. The prepend
source selector deliberately constructs eleven distinct sources, then reuses
the accepted products for runtime cases. `fresh-type-products` constructs and
verifies C5/C6/C7 products; `sh08-verify-function-type-result` reconstructs the
expected typed result. Coordinator and C8 binding preimages embed overlapping
request, artifact and verification DATA. Sharing immutable objects does not
remove repeated canonical serialization occurrences. The existing
`p15-s23-c11-mir-digest` also realizes canonical DATA and a complete `pr-str`
before hashing. These call sites identify candidates, not measured dominance
or permission to omit fresh verification.

Alongside the next necessary changed composition, a bounded cost investigation
can time/count those existing construction, verification and digest boundaries
without printing products. If canonical realization is material, investigate
only its transient representation/lifetime first, preserving exact bytes,
digests, ordering, diagnostics and hostile checks. No extra unchanged proof run,
accepted-result cache, new lane or wrapper is required by this investigation.

The new fixed-DATA case in the existing hosted C6 leaf namespace isolates one
representation mechanism without itself constructing compiler products:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.c6-core-lowering-test --fail-fast
```

Its four fixed, guarded DATA chains use the production flattener. For one,
two, four and eight nodes, nested node occurrences in the flat table are
1, 3, 10 and 36. Observed root-vector / flat-table UTF-8 bytes are 94 / 94,
192 / 285, 388 / 961 and 780 / 3,489. Full rows are checked before traversal
or printing, with conservative pre-print and final 256 KiB bounds. The whole
leaf namespace passed five tests / 114 assertions. This establishes small-case
printed amplification, not unique heap size, timing, actual Checked-Core
size, failure cause or a reason to replay the interrupted large subject.
The returned hosted C6 keeps a selected C5 view, not the full SH05/C3 ancestry;
do not attribute all upstream bytes to its whole-artifact hash. Any future
representation correction must inventory consumers and preserve required
identity, provenance, diagnostics and fresh verification, or explicitly version
and invalidate changed evidence. No representation redesign is selected here.

The existing tiny public-C6 consumer selector now also observes original phase
calls without printing products. At `0fdd0af1`, desugaring reuses one root-local
flattened Vector for its two trace projections; assembly, recursive flattening,
artifact representation, hashing and both fresh verification gates are unchanged.
An old two-traversal trace oracle checks full trace/artifact/ID parity, origins,
root order and original operation delegation. On the three-root, 27-node tiny
fixture, trace calls/returned row positions fall from 54/254 to 27/127;
assembly stays 27/127. These are occurrences, not unique allocations or bytes.
Construction is about 19ms inclusive of hashing and trace, while its two fresh
gates each take about five seconds; timings include observer overhead and are
not additive or a measured speedup. They do not explain the interrupted large
subject. Unit 5/115, tiny integration 1/59 and boundary 6/11 pass in three
invocations; LOC passes 2,022 files, zero exceptions, maximum 250 lines.
Retain `/tmp/sh06-single-flatten-trace-*-0fdd0af1-20261021` and the separate
`/tmp/sh06-single-flatten-trace-red-f0fadd5d-20261021` test-only RED of 24 passes
/ seven failures in 1/31. No unchanged large
proof was repeated and no acceptance cache or migration claim is introduced.

The typed-catch composition exposed a separate retention limitation. Rendering
its first original request, typed result and verification report as one EDN
string produced 43,392,705 characters. The retention attempt used the existing
16,777,216 scalar-unit bound as its serialized-text cap, so it wrote no file.
This is not a measurement of UTF-8 bytes or unique retained objects, nor proof
that the compiler's aggregate carrier bound was exceeded. The observed accepted
type/effect statuses and passed verifier report do not establish the aggregate
test verdict. Do not enlarge bounds, strip embedded proof DATA or rerun an
unchanged proof merely to recover that optional file. Before redesigning this
representation, inspect expected/candidate duplication, identity preimages,
source copies and the applicability of value versus serialized-output limits
using available data and construction code. Keep that bounded investigation
separate from semantic implementation and scheduling changes.

One such construction issue is already corrected in `7aa24451`: the six SH01
type-only callers use `fresh-type-products` in `sh09_body_effect_test.clj` to
construct genuine C5/C6/C7 inputs without the unused SH09 binding seal. The
existing `fresh-products` helper adds that unchanged seal for consumers that
need it. Focused helper and schedule checks cover the split; the earlier
authentic composition remains evidence for its original test revision, not a
replay of the revised harness. This removes statically unused construction,
not a measured runtime bottleneck or a claimed speedup. No new lane, cache or
authority shortcut is involved. Do not change a helper used by a live frozen
composition.

The private first-entry source test uses an existing, narrower development API:
one fresh `sh07-core-verification-session` per current source candidate, its
actual `:expected` artifact, and
`sh07-core-artifact-verification-against-expected` with the same session. Require
a ready session, a real expected artifact and a passed replay before typing.
This retains the original full check catalog but avoids constructing that Core
twice. It is development replay, not the original independent fresh C6 verifier
or admission evidence. Keep sessions invocation-local and never reuse them
across changed inputs or compiler revisions. The public authoritative verifier
and other source helpers are unchanged; source-run status is recorded in the
[private first-entry backlog](self-hosting-slice-backlog.md#private-first-entry-inner-key-lookup-development-increment).

Bounded follow-up: observe one existing authentic-composition test on one
stable revision. Separate upstream/projection, metadata stripping, Gravity
build/replay/resolve, canonicalization/hashing, and final fresh verification
only as needed for the concrete decision. Phase time, allocated/retained bytes,
DATA occurrences versus unique objects, and canonical byte totals are useful
where observable; collecting every metric is not a prerequisite to fixing a
directly observed construction failure. Use existing interfaces and tests, not
another lane or wrapper. Justify the changed representation or lifetime from
that evidence and preserve identities/equivalence and negative tests.
Do not substitute child hashes for embedded DATA, cache accepted proofs, or
change wire formats in this simplification. Heavy runs should answer a concrete
representation or migration question, not refresh historical receipts merely
because another development commit exists.

For example, the current loop-type composition failed at the 2 GiB limit before
semantic assertions. Narrowing to the ordinary C2 reader and observing the
printer's caller identified `p15-s23-c6c10-canonical-record` materializing its
complete canonical text during SH03 digest resolution. The digest caller only
needs a hash; the identity caller separately discards that text. This is
product-construction work, not scheduling overhead. A bounded correction may
avoid those unused strings while preserving the record API, exact canonical
form, UTF-8 bytes, hashes, limits and diagnostics. Require focused legacy/new
parity and the specifically failing source boundary at the unchanged heap;
do not infer a general allocation bound, speedup or semantic pass from the
identified callsite or a proposed streaming implementation.

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_workstream_governance.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M:test
git diff --check
```

Run checks appropriate to the changed files. Documentation changes require the
document validator; tooling changes require affected tool tests. The full suite
serves broad integration/release decisions, not every semantic edit, and does
not itself establish release, self-hosting, seed-retirement, performance or safety.

## Explicit full fresh integration candidate check

Focused composition tests provide integration-development feedback. To publish
the full fresh-candidate evidence described here, explicitly run the exact
committed candidate through this existing command. It is not a prerequisite
for local semantic or focused composition tests:

```bash
clojure -M tools/verify_integration_candidate.clj \
  --base-ref BASE_COMMIT \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

The runner first applies integration-mode worktree preflight to those exact
identities. It then checks out the exact committed tree through a temporary
Git index into a new directory,
uses `-Srepro -Sforce`, runs the existing full `clojure -M:test` suite without
narrowing it, and runs the document, roadmap, governance, and language-boundary
gates in that tree. It also runs `git diff --check` over the exact base-to-
candidate range in the preflight worktree. Existing repository target output
and incremental runner caches are not visible to the exported tree; the
temporary tree is not resumed.
Candidate `.gitattributes` files are rejected so checkout filters cannot make
the verified bytes depend on local Git configuration.

The receipt is written once to
`target/validation/integration-fresh-verification/CANDIDATE_COMMIT/receipt.edn`.
It records the candidate identities, fixed command list, exit codes, elapsed
times, fresh-tree policy, and explicit nonclaims. A prior receipt is never
overwritten or resumed. `C16-SPECULATIVE` rejects speculative, resumed, or
repository-local-cache evidence at this publishable boundary.

For CI, provide the reviewed base SHA, candidate SHA, and candidate tree SHA
from immutable job inputs and upload the receipt when the lane reaches receipt
publication, including a failed verification receipt. Usage, identity, or
receipt-publication errors can fail before a receipt exists.
CI must also impose its own job timeout; an externally killed verifier cannot
finish or publish a receipt.
Do not derive a publishable candidate identity from a mutable branch after the
job starts. For local admission preparation, commit first, keep the named
branch clean, and pass the same identities intended for the workstream ledger.

This lane supplies candidate-only integration evidence. It does not replace an
independent accepted review or grant release, self-hosting, seed-retirement,
safety, performance, Stage3, or SH-07 authority. When the owning contract
requires a Stage3 or SH-07 proof, run its existing fresh/no-resume command
separately and record that receipt in the governed evidence bundle.
`contracts/workstream-governance.json` still requires owned paths, governing
contracts, accepted and rejected fixtures, stable diagnostics, successful
checks, independent acceptance, residual boundaries, and explicit nonclaims
in addition to exact Git identities; `WG009` through `WG012` fail closed when
that admission evidence or authority boundary is incomplete.

The fresh lane also emits optional bounded observability. During the full suite
it gives the child test runner a temporary progress path, which records the
currently running namespace/test var, and the parent prints sparse heartbeats
with elapsed time, descendant RSS, and RSS high-water. These records are
diagnostic-only. Process sampling inspects at most 256 process identifiers and
records when that view is truncated; the host `ps` invocation is separately
time-bounded. Telemetry does not resume work, read repository caches, select
tests, change exit status, or add integration/release authority. The final
receipt keeps the existing identities, command list, status, and exit-code
semantics; the additive `:observability` field is safe to ignore when telemetry
is unavailable.

## Focused Development Runners

Use the reviewed Clojure aliases:

```bash
clojure -M:incremental-check
clojure -M:incremental-check --base-ref main
clojure -M:sh01-test
clojure -M:leaf-test --group foundation-reader
clojure -M:dev-test --catalog
clojure -M:project-structure-runner-unit
clojure -M:project-structure-test --help
clojure -M:stage3-verification --help
```

`clojure -M:incremental-check` passes the current tracked and untracked changed
paths to the existing SH-01 impact planner. `--base-ref REF` additionally
includes committed branch changes from `merge-base(REF, HEAD)..HEAD`, so a
clean candidate branch still produces a plan. Discovery records the resolved
base and merge-base plus committed, tracked-working, and untracked path sets.

The deterministic explanation keeps SH-01 ownership and downstream slice
closure as governance metadata while using the reviewed
`contracts/stage0-incremental-test-dependencies.edn` map for component-level
development invalidation. A reviewed component source selects its declared
leaf and compatibility tests. A present dedicated test-file change selects its
exact namespace. Deleted dedicated tests retain conservative slice fallback;
malformed dependency records and relevant unowned paths fail closed. JVM
groups in the reviewed map keep bootstrap-free tests isolated from tests that
load the bootstrap facade. The plan and execution report are explicitly
non-authoritative and cannot satisfy authoritative migration, release,
self-hosting, or seed-retirement gates.

The SH-01 impact runner batches only normal namespaces from the same slice in
warm JVMs, with a reviewed maximum batch size of eight. `:memory-heavy` work
retains its capacity-one fresh-process lane, and `:exclusive` work remains
fresh and sequential after the parallel lanes drain. Batch reports preserve
namespace fixture boundaries, deterministic fail-fast skips, bounded output,
and explicit non-authority.

### Retained SH-07 B8 regression wrapper

This older, route-specific wrapper runs the
two affected test vars in one bounded child JVM, allowing the existing
process-local fixture/artifact cache to be shared:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route b8-regression \
  --coordination-root "$GRAVITY_SH01_COORDINATION_ROOT"
```

The route is memory-heavy and therefore requires the reviewed SH-01 host
resource broker. Its child receipt reports the exact selected-var count and
the number of calls to the SH-07 artifact constructor; both values are
development observations only. Unknown routes, mixed namespace/route
selection, and extra options fail closed before launch. Admission also compares
the route owner with the live `SH-07` leaf owner and binds its path/namespace to
the current test dependency catalog; drifted, missing, malformed, or extra
owner metadata fails closed. These restrictions govern this wrapper, not
ordinary edits or direct `:dev-test` selections. Use the default three-boundary
workflow above for current development: focused checks first, affected
composition when behavior or interfaces change, and fresh authoritative
verification for an explicit stable-module migration. A complete SH-07
namespace run is a broad regression decision, not a prerequisite for every
semantic change. This wrapper and its cached products remain non-authoritative.

## Bounded P15 Profiling Receipt

The bounded pass-execution serialization observation uses its existing accepted
receipt fixture and one widened metadata fixture:

```bash
clojure -J-Xmx512m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M tools/profile_pass_execution_sort.clj
```

It uses the shared normal-resource broker, a 120-second process watchdog,
separate serialization accounting, and seven warm timing rounds per fixture.
Exact source, tree, file, harness, classpath, JVM, and heap identities accompany
its non-authoritative output. See the [comparison report](artifacts/phase-15/reports/pass-execution-canonical-sort-work-v1.md)
for two-process baseline/candidate receipts, variance, profiler limitations,
preservation tests, and the ranked remaining bottlenecks. This observation
does not establish full-suite or SH-07 performance or verification authority.

The fixed P15 stage2 profiling slice is a development-only observation over
`bootstrap/clojure/fixtures/accepted/core-app.gravity`. It returns an EDN
receipt with static stage2 instruction/function-call counts, finite static
function-frame depth, request-scoped proof-DAG hit/build accounting, phase
duration observations, and JVM thread-allocation observations when available:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote [gravity.self-hosting.p15-bounded-profile :as p])) (prn (p/run-profile))'
```

The receipt id covers only stable source/accounting fields; duration and
allocation remain observational. This slice neither changes nor reads the
development result cache or fresh-verification authority. It is not benchmark,
performance, allocation/stack-bound, stage-advancement, self-hosting, or
seed-retirement evidence. Its DAG counters cover only the request-scoped
P15 source-data node, and its frame-depth field is the static direct-call
depth from the fixture entrypoint, not a runtime stack measurement.

## Explicit SH-07 Cold-Build Phase Telemetry

The SH-07 cold-build telemetry slice is an opt-in diagnostic around one
`sh07-core-file-artifact` invocation.  It is intended for a long C6 source
build, not for the ordinary test suite.  Run it only when the requested source
path and its JVM have been reserved for this diagnostic:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cold-build-phase-telemetry --source PATH
```

The receipt aggregates a fixed set of SH-06, SH-07, and Stage2 plan phases and
the three declared SH-07 digest purposes plus a bounded `:other` bucket.  Each
digest row records call count, elapsed time, failed calls, and capped counts
for large preimage collections such as forms, nodes, bindings, resolutions,
and children.  A synchronous `:on-progress` callback can print sparse phase
and digest records while a long phase is running; callback failures are
swallowed and a maximum of 256 progress records is retained by default.
The collection bound is checked before realization, so an accidental infinite
or unexpectedly wide sequence cannot make the observer unbounded.

The profiler uses temporary process-local root-Var wrappers under a private
lock.  It does not alter the observed source, plan, digest, proof, diagnostic,
cache, or authoritative runner, and it does not memoize or reuse an artifact.
The `:cold-plan-binding-realized-before?` field indicates whether the pinned
SH-07 plan delay was already realized in that JVM; use a fresh JVM when a cold
binding observation is required.  Elapsed values and the source path are
host-variable observations.  The phase and digest accounting is diagnostic
only and grants no benchmark, performance, proof, integration, release,
self-hosting, seed-retirement, or cache authority.  No full or multi-hour
SH-07 run is part of the default telemetry tests; those tests exercise the
observer through bounded synthetic seams.

## Explicit Stage2 Emitter Phase Benchmark

The real Stage2 emitter benchmark is an explicit, bounded development
observation; default-suite coverage uses only lightweight seams. Run at most
three fresh emissions of the accepted hosted-core fixture with:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-plan-emitter-benchmark --iterations 1
```

Each sample retains the existing whole-emission elapsed time and optional
current-thread allocation observation. Deterministic accounting is limited to
the source/phase call counts and semantic receipt. The benchmark reports source
kinds `:authenticated-envelope`, `:syntax`, `:plan-emitter`, and `:other`;
`:other` includes the requested fixture and any unclassified source rather than
silently dropping it. Elapsed time, allocation availability/bytes, and Java and
Clojure runtime versions are host-variable observations. Safely observable
phases are macro parse/expand, function-table construction, function lowering,
instruction summary, canonicalization, and hashing. The emitted plan, semantic
receipt, cache scope, and fresh-emission behavior are unchanged; nested calls
are attributed once to their outermost observable phase.

The temporary `with-redefs` wrappers are protected by a private process-local
lock, so simultaneous benchmark requests in one JVM serialize rather than
overlap. This isolation applies only to explicit benchmark invocations; it does
not make the global Clojure Var replacement an authority boundary.

This is diagnostic profiling only. It supplies no benchmark baseline,
performance improvement/regression, allocation bound, fresh/no-cache
verification, integration, release, self-hosting, seed-retirement, or stage
advancement authority. The Clojure/JVM observer and the remaining Clojure seed
rule runner remain explicit residual boundaries.

## Explicit Stage2 Runtime Execution Attribution

The real runtime-execution profiler is an opt-in, single-iteration observation
over the accepted hosted-core fixture:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-runtime-execution-profile
```

It attributes the Stage2 runtime calls made during fresh plan emission and one
direct execution of that emitted plan. Deterministic accounting is limited to
the semantic receipt, sparse function/instruction/call-edge rows, row-sum
coverage, source identities, and a bounded plan-ID registry. The hot seams use
preallocated primitive counters and an identity-keyed plan registry: they do
not take clocks, read thread allocation, update atoms or persistent maps, or
bind dynamic function state per instruction. Compiler artifact plans omit
source paths, so the profiler registers at most eight plan identities at the
compiler-plan seam and distinguishes authenticated-envelope, syntax, reader,
plan-emitter, emitted-plan, runtime-artifact, and explicit `:other` execution.

Elapsed time, runtime versions, and sampled inclusive function costs are
host-variable. Allocation and elapsed samples are taken only once per 8,192
calls of a function; they are inclusive, sparse, and not a cost ranking or a
sum of the work. Exact instruction attribution is count-only. The output
permits one sample, 128 function rows per source (including an overflow row),
32 instruction operations, 255 named call-edge rows plus one exact overflow
row, and eight plan identities.
The receipt reports off-owner-thread events separately and excludes them from
the owner-thread counters. Counters and sampled sums saturate instead of
wrapping. `:counter-overflow?` makes row reconciliation explicitly incomplete;
the separately host-variable `:sample-overflow?` marks sampled-cost saturation.

Temporary root-Var wrappers are serialized by a private process-local lock.
The lock prevents two explicit profiler requests from overlapping in one JVM;
it does not make global Var replacement safe for unrelated concurrent work.
Do not run it alongside unrelated same-JVM execution. It grants no fresh/no-
cache, performance, allocation-bound, cost-ranking, integration, release,
self-hosting, or seed-retirement authority.

The profiler also exposes an opt-in targeted-cost view for the two currently
authenticated clusters: `:authenticated-envelope-digest-cluster` and
`:syntax-c3-lowercase-hex?`. Selection is predeclared and bounded (sample
stride at most 4096); primitive counters count every selected call while host
elapsed/allocation values are sampled only around selected spans. Targeted
rows remain inclusive and non-exclusive. A ranking is withheld unless both
clusters have nonzero deterministic counts and no sampled-cost saturation;
unsupported targets and unbounded strides are rejected.

`clojure -M:incremental-check` enables the thin development-loop wiring. In
the parent, before any child JVM can launch, it computes one conservative
SHA-256 identity from Git's content-addressed index plus the bytes and modes of
only dirty, deleted, and non-ignored untracked paths. Clean tracked content is
represented by its index mode, blob identity, stage, and repository-relative
path, so repeated snapshots do not reread unchanged files. Unsupported index
states, including unmerged entries, special tracked modes, assume-unchanged,
skip-worktree flags, and repositories configured to ignore file-mode changes,
fail closed to the complete content-and-index-hashing snapshot. The same
fallback applies when automatic CRLF conversion or any tracked-path Git
attribute could transform clean checkout bytes.
Repository symlinks fail closed because their followed targets are outside this
declared closure. That identity supplies the complete production, transitive,
fixture/contract, runner, and classpath closure in each closed cache request;
the request also
binds the test unit, command, external classpath file bytes, Java and command
executable bytes, runtime identities, policy, and bounded timeout. Ignored
generated state is not an input: a test declared deterministic by this
contract must not depend on it or on undeclared ambient environment state. An
injected development-loop context is a test seam; its repository root and
command identity must match the current execution or
`SH01-DEVELOPMENT-LOOP-CONTEXT` fails closed. The reviewed component dependency contract is
the only current source of deterministic cache eligibility. Slice-closure and
ad hoc dedicated tests have no such declaration and therefore execute
uncached. Authoritative,
freshness-required, performance, proof, and nondeterministic policies always
execute and are never stored.

Production parent probes deliberately remain ineligible: the parent JVM's
arguments do not identify a Clojure child's effective configuration. The
incremental runner instead passes an optional `--development-cache` request
to the existing `:test` child. The child JVM always launches, and the parent
holds its ordinary broker lease. Only the actual child can reuse its complete
selected test batch; it acquires no nested broker lease. Reusing a complete
batch preserves the fresh batch's namespace loading and fixture context.

The child binds its initial/current Clojure basis, configuration source bytes,
actual ordered classpath, JVM arguments, runtime/tool identities, environment
and system properties. Receipts contain hashes, not raw configuration values.
The supported JVM options are the CLI's default stack-trace option and
`-Xms`, `-Xmx`, `-Xss`, plus the identified Clojure basis file. Agent,
file-loading and other unknown JVM options, native loader environment changes,
unknown basis/main entrypoints and external classpath directories execute
fresh without storage. This is a precise supported-launch subset, not a claim
to identify arbitrary launcher or native-runtime configuration.

The parent supplies its complete prelaunch repository identity. A child with
different startup inputs fails with `SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE`;
already-loaded runner code cannot silently be attributed to a newer snapshot.
That admission identity and the runner-owned cache/report destinations are
not semantic cache keys. Selection, fail-fast behavior and timeout remain
keyed. Effective configuration and consumed inputs are revalidated before
execution, after execution and before admitting a hit.

`gravity.c11-mir-test` and `gravity.compiler-pass-manifest-test` use re-audited
explicit production closures, including their six and fifteen nested
implementation files respectively. `gravity.c2-artifact-identity-test` retains
its expanded input declaration for audit but requires fresh execution until
real expanded-closure producer evidence permits renewed cache admission.
The [Reader identity reuse acceptance record](verification-reuse.md)
distinguishes unrelated documentation/test bodies from consumed tests and
governing contracts. The runner never infers `require`
edges. Declared paths, roles, modes and bytes, runner/contracts, both supported
classpath orders, alternate `.cljc`/compiled shadows, data-reader resources,
internal class-file inventories and the test-catalog path set are bound.
Unrelated repository changes may reuse a result only when all those consumed
inputs and the effective runtime are unchanged. Runtime/configuration paths
remain bound, so cross-worktree production reuse is not claimed.

Bootstrap-dependent compatibility selections have no explicit narrowed closure.
Their obsolete declarations were removed after the
[compatibility loader audit](verification-reuse.md#compatibility-loader-closure-audit).
Those and other reviewed deterministic selections retain the complete-repository
key. This fallback does not prove that unrelated documentation is a semantic input.
Undeclared, authoritative, freshness-required, performance, proof and
nondeterministic work remains fresh. Failures and timeouts are never stored;
corrupt entries are preserved and rejected. Existing cross-process per-key
singleflight applies inside the child, not in place of launching it.

Reports distinguish `:child-cache-hit?`, `:tests-executed?`, the original
`:test-producer-provenance` and current `:test-applying-revision` and repository
snapshot. These survive parent batch expansion independently of the parent's
execution/cache receipts. A hit does not claim a saved JVM launch, newly run
assertions or fresh authority. A launcher that throws before returning is
still not counted as a successful launch.

The shared cache and broker roots are private mode-`0700` directories under
the repository's Git common directory at
`gravity-development-loop-v1/{cache,broker}`. Linked worktrees therefore use
the same cooperative same-user state without a daemon. The default incremental
child timeout is one hour. The direct `clojure -M:test` runner and fresh
integration verifier do not enable this wiring or observe its cache. The test
alias itself does not confer authority.

Fail-fast stops queue refill but does not cancel already-submitted workers that
are waiting for a cache key or broker lease; those workers remain bounded by
the same child/broker timeout and drain under the existing runner contract.
The shared roots, repository snapshot reads, and child-published batch report
remain a cooperative same-user boundary. They do not defend against hostile
same-user path replacement while a check is running. No daemon, shared compiler
blob, or authoritative result store is introduced by this slice.

## Host-Wide Development Resource Broker Foundation

The Clojure-only SH-01 host resource broker is available at
`gravity.self-hosting.sh01-host-resource-broker`. The incremental-check wiring
uses the canonical Git-common root described above. Other callers pass the
same trusted, existing, absolute host-local `:coordination-root` from every
participating worktree:

```clojure
(broker/with-lease
 {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1"
  :timeout-ms 3600000}
 :memory-heavy
 run-one-development-unit)
```

The coordination directory must be a non-symlink directory owned by the
current user with mode `0700`. The broker creates only direct child policy,
ticket, admission-lock, and slot-lock files. Reviewed host-wide capacities are
fixed at two `:normal` leases, one `:memory-heavy` lease, and one
`:exclusive` lease. An exclusive lease holds every class slot, so it cannot
overlap normal or memory-heavy work. Tickets provide deterministic FIFO
admission within each class; an earlier exclusive ticket blocks later class
admission. Independent classes before that barrier may proceed within their
own capacity.

Waiting is bounded by `:timeout-ms`. OS file locks release on process death,
and a queued ticket is reclaimed only after its lock proves that no process
still owns it. Malformed state and policy mismatches fail closed. Queue,
admission, timeout, stale-recovery, and release events use
`:gravity/sh01-host-resource-telemetry-v1` with `:authority
:non-authoritative`; they are operational observations, not test, proof,
benchmark, integration, release, self-hosting, or seed-retirement evidence.
An injected `:on-event` telemetry callback runs synchronously and must be
nonblocking; callback work is outside the broker's bounded lock-wait claim.
Each acquisition outcome also has a deterministic semantic receipt with schema
`:gravity/sh01-host-resource-non-authoritative-receipt-v1`. The closed receipt
fields are only `:schema`, `:resource-class`, `:capacity`, `:outcome`, and
`:diagnostic-id`; successful release returns the same receipt shape. Absolute
coordination roots, ticket names and sequences, queue positions and lengths,
wait times, and concurrency-dependent stale counts remain observational
telemetry and never enter the receipt. Neither form has evidence authority.
Lease release is exactly-once and thread-affine. Callers must not nest
acquisition; nested behavior is unspecified. Admission is interruptible and
restores the interrupted flag with
`SH01-BROKER-INTERRUPTED`; locks carry no poisoned state, so every acquisition
revalidates the fixed policy and direct-child shapes before admission.

The broker does not launch or execute tests, persist test results, or run a
daemon. The incremental runner holds its lease only across one child process
launch and completion; direct callers hold it around their thunk. A parent
process that dies after launching an uncontained descendant can release its OS
lock while that descendant survives; strict cross-session descendant
containment remains
an OS job/container boundary and is not claimed. The coordination root is a
trusted cooperative boundary: same-user deletion or
replacement of broker state files while leases are active is out of scope and
can bypass inode-based file locks.

Stable fail-closed diagnostics include `SH01-BROKER-ROOT-REQUIRED`,
`SH01-BROKER-ROOT-ABSOLUTE`, `SH01-BROKER-ROOT-INVALID`,
`SH01-BROKER-ROOT-OWNER`, `SH01-BROKER-ROOT-PERMISSIONS`,
`SH01-BROKER-POLICY-MISMATCH`, `SH01-BROKER-RESOURCE-CLASS`,
`SH01-BROKER-STATE-CORRUPT`, `SH01-BROKER-LOCK`,
`SH01-BROKER-TIMEOUT-OPTION`, `SH01-BROKER-TIMEOUT`,
`SH01-BROKER-INTERRUPTED`, `SH01-BROKER-STALE`, `SH01-BROKER-IO`, and
`SH01-BROKER-RELEASE`.

The namespace-lazy development runner can write an opt-in EDN timing receipt:

```bash
clojure -M:dev-test \
  --exact hosted-hello-runs \
  --timing-receipt target/validation/development-test-timing.edn
```

The `gravity/development-verification-timing-v1` receipt separates each
selected namespace's observable require interval from its fixture-wrapped test
execution, and records per-test-var elapsed time, outcome, assertion counts,
selection, and bounded JVM/OS metadata. The JVM-start-to-runner-load interval
is explicitly combined because the bootstrap and runner namespace portions are
not separately observable from inside the runner.

The receipt is non-authoritative scheduling input. It has no benchmark,
performance-regression, proof, conformance, release, self-hosting, or
seed-retirement authority, and it does not make results comparable across
different source, JVM, host, profile, target, or runtime conditions. Omitting
`--timing-receipt` preserves the existing command output and behavior.
This option belongs to `-M:dev-test`, not the compatibility `-M:test` runner.
Run from the repository root so the intended `deps.edn` selects that runner.
Receipts are published only after a selected test run returns normally;
namespace-load or fixture exceptions retain the runner's existing error path
and do not publish a partial receipt.

### Exact-tree warm development worker

Repeated exact development selections can reuse one already-started JVM:

```bash
clojure -M:warm-dev-worker \
  --expected-commit CANDIDATE_COMMIT \
  --expected-tree CANDIDATE_TREE \
  --max-requests 64
```

The worker accepts one closed EDN request per input line. Requests must repeat
the exact startup commit and tree and may use only explicit `--namespace` and
`--exact` development-test selections, with optional `--fail-fast`. It checks
the live commit, tree, and clean worktree before and after every request. Any
failure, dirty checkout, identity drift, malformed request, or request bound
terminates reuse. Output is returned in bounded `WARM_DEV` receipts.
The protocol does not impose an in-JVM test deadline; its supervisor must set
a wall-clock timeout and terminate the worker if a selected var does not
return. A timed-out worker must not be reused.

This worker is non-authoritative development feedback. It never supplies fresh
integration or SH-07 evidence, and it does not alter the cold exported-tree,
authoritative SH-07, release, self-hosting, or seed-retirement lanes.

Stage3 fixed batches run directly through the Clojure runner:

```bash
clojure -J-Xmx2g -M:stage3-verification --batch source-control-form-arity
```

Batch-specific heap requirements and accepted names are enforced by that
runner. Its receipts remain non-authoritative unless a governing contract
explicitly admits the exact result.

## SH-07 And Integration

For bounded cached development feedback, invoke the Clojure iteration runner
directly:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-iteration-cache-runner --help
```

Only the exact four-variable C10 source-coverage batch enables path-neutral
SH-07 carrier reuse. The selection must contain all four variables exactly
once; order is irrelevant. Namespace, subset, superset, duplicate, mixed, and
unknown selections bypass semantic reuse and keep the ordinary path-bound
lane. The runner reads each regular non-symlink source again, binds live
C2/C3/SH-05/SH-06/SH-07 and module-policy pins, admits a carrier only after one
ordinary successful build plus fresh verification, and reconstructs separate
per-path artifacts. Same-key work is single-flight, different keys may build
concurrently, and the process-local FIFO is bounded by four entries and 512
MiB of canonical carrier bytes. Failed, interrupted, stale, mutated, malformed,
or oversized values are not published and may be retried.

Carrier hits still execute the two exact bounded Gravity digest-resolver leaves
needed to rebind artifact and provenance identities. They do not construct a
fresh SH-07 template or cache a final artifact, diagnostic, or verification
report. Result fields `:semantic-cache-enabled?` and `:cache` are operational
telemetry only. Every result remains non-authoritative; a fresh authoritative
run is required only for an actual admission claim. Reuse grants no timing,
benchmark, proof, roadmap, release,
self-hosting, or seed-retirement authority.

The authentic accepted/rejected B8 carrier oracle is intentionally outside the
fast development lane because it performs ordinary full SH-07 construction.
Run that exact evidence only when admission review requires it:

```bash
GRAVITY_SH07_SEMANTIC_CACHE_AUTHENTIC=1 \
  clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-try-catch-test) (binding [clojure.test/*report-counters* (ref clojure.test/*initial-report-counters*)] (clojure.test/test-vars [#'gravity.self-hosting.sh07-try-catch-test/sh07-b8-semantic-carrier-authentic-acceptance-evidence]) (let [r @clojure.test/*report-counters*] (prn r) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1))))"
```

Without the environment variable, the added carrier evidence variable skips
the expensive construction while the ordinary B8 namespace behavior is
unchanged. The carrier and iteration-cache unit namespaces remain the bounded
default checks.

For a bounded exact SH-07 development selection, use the dedicated process
runner:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route c6-contract --timeout-ms 900000
```

The route catalog is closed.  `c6-coverage` selects the single authentic C6
coverage test and additionally requires the existing coordination-root option.
The terminal non-authoritative receipt is printed as EDN on stdout; pass
`--progress-file PATH` to retain bounded phase/heartbeat progress while the
child runs.  Timeout, malformed output, failed tests, or missing admission are
always non-passing, and no result is cached.  This helper does not replace the
authoritative SH-07 command or grant proof, performance, integration, release,
self-hosting, or seed-retirement authority.

For a fresh reviewed SH-07 transaction:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-authoritative-runner --fresh all
```

Workstream lifecycle and integration eligibility are governed by:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
```

Inspection is candidate-focused by default. It reports the current candidate
worktree without enumerating registered peers; use `--worktree-scan all` only
for an explicit, bounded repository-wide inventory.

The preflight is read-only. Integration mode requires the exact recorded base,
candidate commit, candidate tree, named branch, and clean worktree. A passing
development command never substitutes for independent acceptance or grants
authority beyond the workstream ledger.
