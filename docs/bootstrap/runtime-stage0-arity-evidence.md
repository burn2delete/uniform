# Native Stage0 call arity

## Scope and governing contracts

The complete existing `fail-call-arity!`, `assert-min-arity!`,
`assert-exact-arity!`, `assert-between-arity!` and `assert-even-arity!` bodies
move from foundation component `00546` into `gravity.runtime`. All five old
Bootstrap signatures remain adapters. Every preceding validation/header/
function-table body in that component is unchanged.

D0, D1, D2, D3, D6, D8, D9, L2, C1, C6, C15, R1, R4, R11, R12,
BOOT8, TEST2 and TEST3 govern this increment. L2 requires evaluation-order and
failure preservation. C6 legality remains upstream; these existing native
execution guards do not establish checked-Core admission. C15/R12 constrain
diagnostic provenance without making the legacy failure payload a complete
canonical diagnostic. Signature arity is semantic legality, not compiler cost
accounting or an input quota.

The frozen source parent is
`10efdb1328db3b749e0376c2b45a121ecfe89f44`, tree
`59370742f5ee36b9637a73aa33578b182065be89`. Published Main
`b722e29a0096381fbf476ca04771fe6567657f69` is already an ancestor.
The parent's own hosted CI remains under independent audit at scope capture.
This successor imports no new Main paths and inherits no source or CI
acceptance. Its separate seven-path claim follows the parent's normal writer
release; an immutable predecessor awaiting CI does not reserve these paths.

## Public API and artifacts

The implemented APIs are:

```clojure
(fail-native-stage0-call-arity! fail-var id module callee args expected)
(assert-native-stage0-min-arity! fail-call-arity-var module callee args n)
(assert-native-stage0-exact-arity! fail-call-arity-var module callee args n)
(assert-native-stage0-between-arity! fail-call-arity-var module callee args min-n max-n)
(assert-native-stage0-even-arity! fail-call-arity-var module callee args)
```

Each consumes one explicit late owner Var, existing native module/callee/
argument data and the existing arity parameters. The module's `:source-path`
carrier differs from the separate Stage2 helper family's `[:source :path]`
plan carrier. Existing Stage2 APIs remain unchanged.

Outputs are the original nil/owner return or unchanged owner/host exception.
The failure helper preserves its id-specific message, source-path identity,
callee, expected arity, actual argument count and remediation. It creates no
standalone artifact. The enclosing ordinary public source route emits actual
plans and compiler reports, retains deterministic plan/report identity and
source hashes/paths, executes accepted plans, and records genuine arity
refusals. No source map, origin, proof or runtime manifest is invented.

## Ownership, ordering and dependency direction

Runtime owns these five arity predicates and local failure facts/decisions.
Bootstrap retains adapter wiring and generic failure construction/transport.
The adapter supplies `#'fail!` to the failure helper and
`#'fail-call-arity!` to the four predicate helpers. Runtime imports no
Bootstrap namespace and discovers no owner Var dynamically. Code dependencies
are Bootstrap adapters to Runtime to `clojure.core`.

Preserve count/guard evaluation before selecting an arity failure callback.
Select the failure function root before evaluating its call arguments,
including source-path lookup and actual-count construction. Preserve returning
failure compatibility, exception identity, valid-call nil returns, the unknown
id fallback message and all minimum/exact/between/even semantics. Do not add
new input validation, eager callback capture, error remapping or shape gates.

The existing builtin dispatcher and compiled-function executor continue to
call the original Bootstrap adapter Vars. Argument evaluation, effect/policy
validation, source/plan admission and reserved/compiler-native dispatch keep
their existing owners. Moving a runtime guard does not defer compile-time
profile/effect/capability/ownership/safety legality to execution.

## Tests and evidence

Paired accepted and rejected sources use unheld public
`macro-source-artifact(path, text)`, its genuine module/expanded forms,
`stage0-compiled-core-plan` and the public executor. The route compiles real
plans and demonstrates genuine function/builtin arity refusals after emission.
Complete macro/module/plan/report observations, source facts, identities,
stdout, helper calls and failures are identical before and after extraction.
Absent paired fixture files are labeled caller-supplied text, with no
file-authentication claim. The fourteen observations contain fourteen real plans, six successful
stdout executions and eight runtime refusals, reaching all five arity helpers.
Both source spellings cover core-app examples/accepted fixtures, function and
builtin arity fixtures, all ordinary builtin arms, odd assoc key/value arity
and division by zero. The host-error control retains the existing outer
`L2-BUILTIN-ERROR` remap; it is not new arity-rejection credit.

Baseline receipt SHA256
`6f1a513de50aa07c3531648db0da08236bd79b7fd7717a20a0f9e4710bd4f4b2`
and implementation observation SHA256
`650bae71a250d58ebcf0713eee0a2ca5db82d97a8de0aa1602d69b6224defbc6`
both record the actual scope HEAD
`3d9600cd7d1bb3cb836420271da9e9c092cf211e`, with separate producer file
hashes. They do not claim a later clean commit was observed. Their
authentication, Reader/pin and private-factory nonclaims apply only to this
recorded public observation route.

The existing whole `gravity.runtime.stage0-execution-test` command is extended
with five API/Var contracts, all valid/invalid arity rules, diagnostic/source
identity, opaque argument-count lookup, late root selection, returning failure
behavior and adapter interposition. All 19 earlier test bodies remain unchanged
except the documented 43-to-48 inventory literal. Only the five new names are
added to the exact inventory in `gravity.runtime.pipeline-test`; its old entries
and generic non-authority assertions remain. The six new tests declare managed
runtime, temporary Clojure host, hosted profile, JVM target and the native arity
operations under test. They do not claim canonical runtime conformance.
The whole Stage0 namespace passes 25 tests and 387 assertions; the selected
public-inventory test passes 1 test and 344 assertions. Language-boundary and
ownership checks pass 48 tests and 1202 assertions. Runtime and both changed
test files pass clj-kondo with zero errors/warnings; `git diff --check` passes.
Four selected Stage2 arity/unary/adapter regressions pass 4 tests and 92
assertions. Cold loading exposes all 48 public APIs with matching declarations
and argument lists, no namespace aliases, and no Bootstrap or Diagnostics
namespace loaded. Structural document validation passes 240 documents and 19
phase indexes; the full-language roadmap structural validator also passes.
Those structural checks do not establish semantic or roadmap completion.
Existing whole-namespace CI commands cover the additions; no workflow, command,
ownership, catalog or load-order edit is required.

The extraction retains every earlier Runtime function body, Stage2 helper and
surrounding `00546` source. The 69 existing CI command definitions and 29
contract commands remain unchanged. The derived changed-file lint command must
include the newly changed `00546` path in its actual source inventory; its
expanded argv is not claimed identical to the predecessor's inventory.
Complete current-Main comparison lint remains required.
Independent semantic, adversarial, conservation,
provider and own-CI review must bind the exact clean source and base before
integration eligibility. Do not rerun the excluded broader local Runtime
pipeline incident from the parent workstream or transfer its evidence here.

Independent static boundary review SHA256
`efdccac643a2c4ed3084111d27cf227ed11656d442a60693ddb28b05baa57f69`
identifies the five real helper bodies, current builtin/function-executor
callers, inventory consumers and existing whole-namespace test commands. It
is discovery evidence, not source acceptance.

## Current status and unsupported boundaries

The five complete bodies and compatibility adapters are implemented in the
current working tree, taking the public API inventory from 43 to 48. The
original-body receipt has SHA256
`5c5daf9f03cc264c4518d0dbe6265d3dc84caa9ee00e2a64565cfe139b61d9bf`.
A later whitespace-only receipt
`2e8aebce87194e42b3a38e9defece2a33886be57c2d3a387a64604bf69bd3eea`
records removal of the wrapper file's final blank line without rewriting the
original extraction record. Clean source publication, final independent
reviews, own hosted CI/provider audit and Main integration remain pending.

The preceding builtin/executor evidence preserves its historical API counts
and helper-ownership statements at those captures. This arity extraction
supersedes those statements for current helper-body ownership; their original
observations and checks do not become current arity acceptance.

This temporary native boundary does not implement canonical Core/MIR admission,
complete compile-time call checking, canonical L2/C15/runtime semantics,
typed host delegation, runtime-family/provider selection, source or plan
authentication, build-read/Reader/file authority, backend execution, portable
errors, complete source maps/generated origins, redaction/replay, release,
migration, self-hosting or seed retirement. Held Reader, authenticated/private
builders and source pins remain outside the permitted observation route.
Gravity/Uniform remains the destination implementation language.

Exact current-base preflight and Main integration remain with the designated
integrator. This manual Source has no Main, Ready, controller, service or
recovery role.
