# Syntax diagnostic owner-call ordering

## Governing contracts and observed defect

D1/D2/D3/D8/D9, C1/C3/C15 and TEST2 govern this corrective increment. Parent
f1f2e1073967b1c8ec717564b87b77e6cc9e618c, tree
a16926a454fab62fef63ecf3292bcc02e46f3272, is unmerged Reader draft PR1182.
No parent CI or acceptance transfers to this new source.

The Syntax implementation blob f4323dff191228274103ea76bfcc2425ddbd357e
is identical in parent and actual mainf66233177d9fc3eaf29af30c7bfc3286bf4f53bc.
This is a pre-existing main defect, not a regression introduced by the Reader
cohort. The original global function call captures its function before argument
evaluation. Calling a local Var as an IFn dereferences its root after arguments.
If a source-span callback changes the failure owner, the extracted constructor
selects a different owner.

Independent and Root read-only probes compare the exact original
sh04-syntax-boundary-fail! from6c7df70327e8ccfe138bd21ffa0db89a11cee99e:
original selects the old failure owner, current Var ports select the new owner,
and function-valued ports select the old owner. Full arguments remain equal.
Original component blob c185ea9f96889eae794fe2b51a058acf51f3bca6 and both
probe helpers/logs are retained. No compiler, syntax assembly, authentication,
source selection or plan execution was invoked by those probes.

## API and ownership scope

Correct only five existing operations: boundary-fail!, registered-literal-fail!,
accept-result!, require-result-carrier! and execute-plan!. They cover pure C3
diagnostic construction, native result/carrier transport and host-failure
containment. Preserve public names/arities, operation maps and result shapes.
Resolve a supplied Var to its current callable value at each original callable
use before evaluating arguments. Preserve existing function-valued and other
callable ports; this adds no admission/type guard or new callback restriction.
Do not resolve once at entry: later calls must retain late rebinding.

The diagnostic operations retain C3-ID, C3-FACT-STALE and C3-SHAPE decisions,
source/producer/form/fact fields and supplied-owner publication/exception identity.
The runtime bridge retains argument cleaning, execution/result cleaning order,
existing contained host classes and interruption propagation. Its execution
port is a trusted dependency, not permission to run a held source route.

Syntax remains stage-owned and consumes explicit operations. Bootstrap remains
the source/admission/orchestration owner. No Bootstrap discovery/import, source
IO, authenticated reader association, source selection, pins, cache publication,
implementation hash/export admission or sealed-input rule changes belong here.

## Artifact inputs, outputs and identity

Inputs are explicit diagnostic facts, native result/carrier values or runtime
arguments plus trusted operation ports. Outputs remain the original native
carrier/result, structured C3 refusal and host containment data. Preserve full
data, object references, source path/span, diagnostic identity, operation order
and exception identity. No identity preimage, envelope, source provenance field
or downstream semantic fact changes. New implementation provenance is recorded
separately; no stale bootstrap/migration authority is renewed.

## Acceptance and tests

Retain failing Var-port regressions and independent old/current controls; cover
both Vars and ordinary function values, nested callbacks, later-call rebinding,
span short-circuiting, facts precedence, returning failure owners and exact
exceptions. Exercise accepted native result/carrier transport and real C3
refusals for invalid native carriers/rejected results. Capture full diagnostic/
native data and provenance on real paired source paths, keeping diagnostic
emission distinct from authenticated syntax compilation.

Prove all unrelated Syntax functions, Bootstrap facade/source/admission files,
manifests and pins unchanged. Add the new focused namespace to the existing
Syntax CI command. Run focused regressions, applicable current-source integration
checks, language/source ownership, contextual lint, formatting/validators,
independent semantic/adversarial review, exact source/base hosted CI and
immediate clean integration preflight. Maintain this early draft PR.

## Actual ownership and nonclaims

An actual five-path writer owns syntax.clj, its focused owner-call tests,
this evidence, the namespace map and tools/ci/run.clj. The separate SH03 author
reported its prior writer released; the actual claim succeeded. Read-only
comparison with unmerged1177/source8e79 shows its reader-authentication/integrity
functions are outside these five operations. No unmerged authentication code
was adopted or executed; its acceptance does not grant this slice authority.
Coordinate any later actual writer or incompatible invariant change.

This initial scope record has no implementation/test/CI credit. This correction
does not complete Syntax migration, prove every other port use, establish
canonical C2/C3 authentication, execute Gravity source, renew source pins or
grant self-hosting, seed retirement, release or whole-language conformance.
Held SH03/SH04, private compiler/authentication and retained dirty-patch routes
remain untouched. Runtime test execution ports are explicit unit controls,
never evidence of a genuine authenticated compiler execution.
