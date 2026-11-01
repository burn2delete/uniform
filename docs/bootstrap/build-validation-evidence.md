# Build metadata validation boundary

## Governing contracts and scope

D1/D2/D3/D8/D9, PKG1/PKG2/PKG3/PKG6/PKG7/PKG10/PKG11, C1/C15/C16,
BOOT8 and TEST2 govern this increment. PKG2 requires declared build effects;
PKG7 requires controlled network inputs. Existing native Stage0 metadata checks
enforce those two conditions before instruction-plan execution.

Move only `validate-stage0-compiled-build-graph!` and
`validate-stage0-compiled-reproducible-build!` from component00537 into
`gravity.build/check-graph-effects!` and `check-offline-recipe!`. Preserve the
existing wrappers, arities, set/subset algorithm, offline-network equality,
diagnostic identifiers, complete diagnostic payload and evaluation order.
An explicit late diagnostic Var port keeps diagnostic construction with its
existing owner. The stage may depend on clojure.set; it must not import
Bootstrap, CLI, Release, backend or host invocation namespaces.

## Evidence obligations

The initial scope is based on actual main
b363c32b5569e4ea76df852abd142be7c067292e. Before implementation, capture native
positive and negative records and actual complete-source compilation or
instruction-plan execution. After extraction, compare complete values and
serialization, source provenance, deterministic identities, both full component
and surrounding behavior. Exercise real undeclared-effect PKG2001 and
uncontrolled-network PKG7003 failures, including late diagnostic-root behavior.

Run proportionate focused and affected tests, language boundary, ownership
registration, docs/roadmap/governance validation, contextual lint and independent
adversarial review. Each final source tuple requires its own CI and clean
current-main integration preflight. The initial scope commit conveyed no
implementation or acceptance credit.

## Implemented development slice

The six complete accepted/rejected source fixtures are recorded in
1a3bb12b111336905d1933c15a28d7d09dcc2ccd. Before and after controls compile
both accepted source spellings into actual Stage0 instruction plans and execute
them through the existing native Clojure instruction runner, producing
`build-metadata` plus a newline. Undeclared graph effects and enabled recipe
networking fail PKG2001 and PKG7003 during plan construction, before execution.
Metadata labels and graph/recipe ids are ordinary supplied test data.

All three complete and serialized controls (native data, Gravity source and QST
source) compare exactly. The actual source text/hash, complete plan, recomputed
plan id, output and full rejection message/data are captured. Stage0 identities
include actual paths: the Gravity plan is
`sha256:2b242e62806b75f3212417a37cca6b06aea52ce6106854d47c637c4e31ca9762`
and the QST plan is
`sha256:d21790b7d5a95490b349d9ff408361d82826c74e820c99304a83b8ac8eacd684`.
These are deterministic per input, not PKG7 path-neutral artifact identities.

The full original component00537 conserves except its two compatibility
wrappers, including its original lack of a final newline. Bootstrap adds only
one require. Both moved function bodies and parameter lists are exact except
the explicit late diagnostic Var port. All other native checks and diagnostic
construction remain unchanged. The existing sh-package-build / SH-23 inventory
owns the new stage and standalone test; primary catalog counts are unchanged.

Focused tests pass4/53 and coupled Runtime/Package/Standard Library/literal-helper
tests18/280. Host callback controls verify
late root reads and unchanged exception identity; callbacks issue no authority.
The first focused run4/53 had two incorrect expected line/column spans; the
actual unchanged span is source plus form-index. Its log is retained. The first
whole-component verifier caught an added final newline; restoring the original
byte makes the complete comparison pass. Neither correction changes semantics
or weakens comparisons.

Independent review rejects sourcebbd8df0b for a duplicate helper id, and lint
rejects a duplicate test Var name. Build now has the distinct stable helper id
`build-metadata-validation` and test Var
`build-metadata-helper-has-an-explicit-boundary`. The existing literal helper
id/test remain unchanged. A regression checks global helper-id uniqueness,
the Build index's actual source namespace and refusal of a duplicated id.
Fresh ownership38/1093 and language6/11 pass. The earlier37/1077 run had
overwritten the literal test Var and supplies no complete ownership coverage.
Initial lint receipts are retained outside source as nonpass evidence; final
lint and exact independent review are required on the corrected source.

Scripts are `/tmp/uniform-build-validation-{control,verify}.clj`, products are
`/tmp/uniform-build-validation-evidence/{before,after}-*`, and logs use
`/tmp/uniform-build-validation-*`. Exact source review, contextual lint, own CI
and current-main preflight remain integration requirements. No historical
authenticated Reader or release route is used.

## Actual CI main composition

After the registration correction, the normal composition uses actual main
0e01fc5937d5b224dd03e14751566f0e541b6529/tree
7bd8ec6c99a12fd3338edb3fce7a3e941064abef. Its six changed paths fromb363
are CI tools/docs, cached-shard runner/tests and the verification catalog.
All six incoming paths are preserved exactly. Compiler and Build sources,
complete fixture/control inputs and execution paths are unchanged. Updated-base
conservation and provenance must bind actual0e; previous-base CI or review
does not accept the new source tuple. Local controls execute the ordinary
native Stage0 compiler, without a cached-shard or authenticated Reader route.

## Compiler finalizer main composition

Own CI41267943957 passes all nine enabled jobs on source46af35ac/base0e,
with snapshot/prerelease skips. Its result remains historical after main moves
to09934c4a1a4d0a49970740307f1436018c74197b/tree
304ccdbc03da4dddb0f3164add7fe0d2380fa613. The three incoming changes refactor
Gravity emitter, C7 type-engine and effect result finalizers. They are preserved
exactly; native Stage0 compiler/runner and Build execution inputs are unchanged.
These three Gravity files are not executed by this slice's Stage0 controls.
Coupled tests continue to execute their declared Gravity subjects. Current099
requires fresh native/source controls, conservation, proportionate regressions,
review, own CI and clean preflight; no old-base green transfers.

## Unsupported boundaries

These are two checks over trusted native metadata, not complete PKG2/PKG7
validation. Existing fixture identities, complete/status/proof labels and
package metadata remain supplied compatibility data. They do not authenticate
source, grants, builders, lockfiles, signatures, SBOMs or reproducibility.
No package fetch, build action, shell, network access, cache reuse, release,
self-hosting, seed retirement or namespace-completion claim follows. Other
Build namespaces retain their current owners and authority boundaries.
