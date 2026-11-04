# Reader compatibility-oracle ownership

## Purpose and governing contracts

This bounded slice moves the existing Clojure compatibility-oracle construction
body from component00819 into gravity.reader. D1/D2/D3/D8/D9, L1, C1/C2/C3/C15,
BOOT8 and TEST2 govern stage boundaries, source identity, token/form provenance,
native host-error containment and development evidence.

## Boundary and dependencies

The proposed native API accepts actual source path/text/options/project context
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
