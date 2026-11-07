# Native Stage0 call arity

## Scope and governing contracts

Move the complete existing `fail-call-arity!`, `assert-min-arity!`,
`assert-exact-arity!`, `assert-between-arity!` and `assert-even-arity!` bodies
from foundation component `00546` into `gravity.runtime`. Keep all five old
Bootstrap signatures as adapters. Leave every preceding validation/header/
function-table body in that component unchanged.

D0, D1, D2, D3, D6, D8, D9, L2, C1, C6, C15, R1, R4, R11, R12,
BOOT8 and TEST2 govern this increment. L2 requires evaluation-order and
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

The proposed APIs are:

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

## Tests and evidence plan

Capture paired accepted and rejected sources through unheld public
`macro-source-artifact(path, text)`, its genuine module/expanded forms,
`stage0-compiled-core-plan` and the public executor. Compile real plans and
demonstrate genuine function/builtin arity refusals after emission. Compare
complete macro/module/plan/report observations, source facts, identities,
stdout, helper calls and failures before and after extraction. Absent paired
fixture files must be labeled caller-supplied text, with no file-authentication
claim.

Extend the existing whole `gravity.runtime.stage0-execution-test` command
with five API/Var contracts, all valid/invalid arity rules, diagnostic/source
identity, opaque argument-count lookup, late root selection, returning failure
behavior and adapter interposition. Preserve all 19 earlier tests except the
documented 43-to-48 inventory literal. Add only the five new names to the
exact inventory in `gravity.runtime.pipeline-test`; preserve its old entries
and generic non-authority assertions. Reuse existing whole-namespace CI
commands; no workflow, command, ownership, catalog or load-order edit is
required.

Conserve every earlier Runtime function body, the Stage2 helpers, surrounding
`00546` source and all existing 69 CI vectors/29 contract commands. Run focused
regressions, the language boundary, document/roadmap validators and complete
current-Main comparison lint. Independent semantic, adversarial, conservation,
provider and own-CI review must bind the exact clean source and base before
integration eligibility. Do not rerun the excluded broader local Runtime
pipeline incident from the parent workstream or transfer its evidence here.

Independent static boundary review SHA256
`efdccac643a2c4ed3084111d27cf227ed11656d442a60693ddb28b05baa57f69`
identifies the five real helper bodies, current builtin/function-executor
callers, inventory consumers and existing whole-namespace test commands. It
is discovery evidence, not source acceptance.

## Current status and unsupported boundaries

This is the early scope draft; the five new Runtime APIs are not implemented
at this capture. The current public count remains 43. The draft will be
maintained as actual implementation, tests and reviews complete.

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
