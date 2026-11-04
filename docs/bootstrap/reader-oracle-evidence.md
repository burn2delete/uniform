# Reader compatibility-oracle ownership

## Purpose and governing contracts

This bounded slice moves the existing Clojure compatibility-oracle construction
body from component00819 into gravity.reader. D1/D2/D3/D8/D9, L1, C1/C2/C3/C15,
BOOT8 and TEST2 govern stage boundaries, source identity, token/form provenance,
native host-error containment and development evidence.

## Boundary and dependencies

The native API accepts actual source path/text/options/project context
and explicit late owner Vars. It constructs the existing compatibility product
map using the existing source-unit, tokenizer, parser and record operations.
Source-unit and reader-table construction stay outside the protected region;
token/form construction and the original StackOverflowError and ExceptionInfo
containment retain their order and exception behavior. Bootstrap keeps both
compatibility arities and project-context discovery.

The canonical c2-reader-products implementation, SH03 resolution/adaptation,
product authentication, source/file intake and publication remain unchanged.
The oracle is a comparison implementation, not an alternative admission path.
No source pin or previously refused Source-to-C9 route is invoked or changed.
The destination imports neither Bootstrap nor a downstream stage.

## Inputs, outputs and diagnostics

Inputs are explicit source text, provenance, deterministic reader options,
project context and the existing callable Var owners. Output is the original
source-unit, token stream, form tree, root ids, parsed records and parsed values.
Identity and provenance are supplied by the same source/token/form owners.
The stage owns the existing native containment sequence; diagnostic policy and
transport remain with the supplied C2 failure owner. No facts are invalidated.

## Verification and acceptance

Before implementation, capture real accepted and malformed paired source
products through this compatibility oracle only. Preserve complete serialized
and parsed values, actual paths, source bytes/digests and deterministic ids.
Verify late Var lookup, callback/refusal ordering, original exception causes,
source-unit/table failures outside containment and opaque host failures.
Run focused Reader and relevant compatibility tests, language/ownership gates,
lint and contract validators; obtain independent source and adversarial review.
Keep a draft PR and clean verified commit. Current own CI/artifact acceptance
and exact-base integration preflight remain necessary before integration.

## Unsupported boundaries

The initial scope commit has no implementation or executed-test credit. This
bounded native extraction does not establish canonical C2 conformance, SH03 product authentication, cache
admission, proof authority, source grants, migration, release, self-hosting or
seed retirement. Historical failed or restricted operations remain untouched.

## Implemented source and initial checks

gravity.reader/compatibility-reader-products now owns the original four-argument
body through eight explicit late Vars. The destination has no imports or
Bootstrap resolution. The compatibility wrapper retains both arities and
project-context discovery; canonical c2-reader-products and projection-operation
construction remain unchanged. Diagnostic construction retains C2-HASH for
native stack containment and propagates the parser's original STAGE1READER002
for the actual malformed-delimiter fixture, with original cause and context.

Before extraction, the native oracle accepted both surface-syntax spellings
and refused both malformed-delimiter fixtures. Complete actual-path/text/hash,
context and product/diagnostic records have raw SHA-256
311c086ccd3b5e4cc3dfe64b2102b107b4011580ecc99f958733f5b6fa41e55b
at scope source1bafe77b79f54f26bcf62e908f58420122a5a1da. This is only native
comparison evidence, not source-product authentication. Initial focused tests
pass27 tests/336 assertions, including real products, stable/source-sensitive
identities, native delimiter errors, outer versus inner exceptions, exact cause
identity, late per-token roots and controlled host-stack containment.

Initial temporary capture syntax and an incorrect expected C2-DELIMITER id
failed before producing a record. Their scripts/logs/reports remain retained.
The corrected expectation follows the unchanged Stage1 diagnostic owner;
production behavior, source pins and admission gates were not altered.
Complete post-extraction comparison, affected regressions, lint, validators and
independent source/adversarial acceptance remain outstanding at this commit.

## Verified native comparison and regression checks

The complete four-record after-capture equals the original serialized bytes and
parsed values, retaining SHA-256311c086ccd3b5e4cc3dfe64b2102b107b4011580ecc99f958733f5b6fa41e55b.
The four prior gravity.reader function bodies, the canonical c2-reader-products
and projection-operation suffix, and the three-argument context-discovery
arity are exact. No authority/status field is added to the native product map.
The current source body was recorded at351b27627d6db744af5c3353e8b3f3a621a96434.

Focused Reader checks pass29 tests/339 assertions. Fresh separate JVMs pass
C2 source-identity9/146, product-projection6/43 and compatibility7/210: a total
of51 tests/738 assertions across the four successful runs. Added controls verify
failure-operator selection before span lookup and tripwire canonical entrypoints
without calling them. A separate cold process constructs native products through
supplied Vars with Bootstrap absent and five public stage operations.
Language6/11 and source-ownership42/1187 pass with zero skipped namespaces.
The three-path contextual lint passes with no blocking findings or unchanged
baseline warnings; raw context subprocess warnings remain visible. Documentation,
roadmap and governance validators pass. Temporary raw captures, checks and logs
use /tmp/uniform-reader-oracle-*; generated lint receipts are archived outside
the source checkout.

The initial combined compatibility/helper run failed the helper's cold-load
assertion because Bootstrap had already loaded. It remains a nonpass; the fresh
standalone helper runs supply their own results. A new timing test initially
left the real reader-table constructor under an interposed span callback, so
upstream table construction observed that callback before the intended stack
handler. Its failed source/log remain retained. The corrected test supplies
native source/table stubs to isolate the stated operator-order contract; genuine
product controls remain separate. No production behavior or predicate changed.

Independent implementation/adversarial review, final committed checks, own CI
and exact current-main composition/preflight remain separate integration gates.
