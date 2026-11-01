# Bootstrap module literal boundary

## Contract and scope

This increment follows D1/D2/D3/D8/D9, L1/L2/L3, C1, BOOT1/BOOT2/BOOT8 and
TEST2. The current development-verification policy removes node, depth and
scalar quotas whose sole purpose is refusing otherwise valid input.

The proposed `gravity.bootstrap.module-literals/materialize!` owns the existing
temporary seed literal-data traversal and materialization. Its inputs are a
supplied diagnostic Var and an already parsed native form. Its output is
literal data, or the caller's unchanged BOOT2006 diagnostic for unsupported
literal shape. The helper imports no compiler stage. The Bootstrap wrapper
retains its existing one-argument API and diagnostic ownership.

Only the visited-node, nesting-depth and string-length quota checks are to be
removed. Unquoted symbols/calls, malformed quote shapes, unsupported literal
objects, declaration legality, duplicate declarations and authentic C5/source
admission remain governed by their existing checks. Quote returns inert data.
The helper does not confer source, Core, profile or execution authority.

## Evidence required for integration

Capture complete and serialized valid outputs and real refusals before moving
the helper. Compile complete ordinary Gravity and QST development modules,
execute their literal-returning functions, recompute plan identities and source
hashes, and reject genuinely malformed literal declarations. Verify unchanged
existing outputs and the intended newly accepted quota-only cases. Preserve
the full surrounding component and all admission guards. Run focused and
affected tests, language boundary, documentation/roadmap/governance, contextual
lint, independent exact-source reviews, own CI and a clean current-main
integration preflight.

## Unsupported boundaries

This remains a Clojure seed helper, not a Gravity-authored replacement or a
complete Bootstrap namespace migration. It accepts already parsed native data;
it is not a public Reader or an authenticated source decoder. Host heap and
stack limits remain actual host restrictions, not compiler accounting. No
general profile, effect, capability, ownership or safety legality follows from
literal construction. No new native/backend, compiler admission, migration,
self-hosting, release, seed retirement or namespace-completion claim follows.

This initial scope record carries no implementation or integration credit.
