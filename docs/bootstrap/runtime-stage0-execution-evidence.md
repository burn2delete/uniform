# Native Stage0 instruction execution

## Scope and governing contracts

Move the complete existing native Stage0 instruction-sequence, instruction,
compiled-function and compiled-plan executor bodies from Bootstrap into
`gravity.runtime`. Keep the four original Bootstrap names and argument lists
as adapters. This is the existing temporary Clojure interpreter, not a backend
or an implementation of the canonical managed-runtime manifest.

D0, D1, D2, D3, D6, D8, D9, L2, L9, C1, C15, R1, R4, R11, R12, BOOT1,
BOOT8 and TEST2 govern this extraction and its explicit limits. L2 and L9
govern execution order and failure compatibility. R1/R4/R11/R12 identify the
runtime-service, host-delegation, authority and provenance requirements that
this native seed interface does not establish. Compile-time policy checks stay
upstream; moving execution does not replace them with runtime-only legality.

The frozen predecessor is commit
`51cfc9748ac9dd2ac519a9c4601922f167f2dc03`, tree
`1c9efae09c1804e9ae97388257cc629edc7f8012`. Published Main
`de1eccf9a148cc199ff50f41014bc4732b715a1d` is already an ancestor. No new
Main import is necessary at this scope capture. Predecessor reviews and CI do
not accept this new slice.

## Public API, artifacts and dependencies

The intended APIs are:

```clojure
(execute-native-stage0-instructions operations plan env instructions)
(execute-native-stage0-instruction operations plan env instruction)
(execute-native-stage0-function operations plan callee args)
(execute-native-stage0-plan operations plan)
```

Inputs are trusted native Stage0 plan/instruction data, environment bindings,
callee/arguments and explicit live owner Vars. Results are the original host
values, environment traversal, writer effects, captured stdout or unchanged
exceptions. No plan, source or callback authentication is added.

The operation boundary covers instruction and sequence recursion, function
execution, the existing effect validator, builtin invocation, failure transport,
call-arity failure construction and string joining. Select each callable at its
original use. Keep `println`, `str`, truthiness, collection constructors and
`with-out-str` on their existing shared Clojure operations. Do not capture all
ports eagerly, add a generic dispatcher or sort execution inputs.

Bootstrap calls Runtime; Runtime does not require Bootstrap, Lowering, CLI,
release or a backend. Bootstrap retains source intake, macro expansion,
instruction/plan compilation, all individual policy implementations, native
builtin dispatch, diagnostic construction/publication and artifact admission.
The moved stage owns instruction selection, traversal, local/function lookup,
entry/arity-check placement and existing diagnostic decisions. Failure payloads
and messages retain the original operation boundary and identity.

## Preservation and diagnostics

Preserve left-to-right argument and initializer evaluation, selected-branch
behavior, sequence return values, map-entry evaluation order, lazy set traversal,
duplicate-map-key behavior, function binding, callable-selection timing and
exception identity. For `println`, evaluate arguments before the existing
effect check and preserve writer capture. A returning failure callback retains
its original compatibility continuation; it does not admit an invalid plan.

Existing diagnostic decisions include `L2-UNKNOWN-SYMBOL`,
`L2-UNKNOWN-CORE-FORM`, `L2-FUNCTION-ARITY`, `L3-UNKNOWN-ALIAS` and
`L2-MAIN-ARITY`. Builtin arity and policy diagnostics retain their existing
owners. Source paths and plan identities remain unchanged; execution does not
invent instruction spans, generated origins or proof facts.

## Evidence and acceptance

Compile genuine `.gravity` and `.qst` sources through `macro-source-artifact`,
its actual module/expanded forms and `stage0-compiled-core-plan`, then execute
the emitted plans through the unchanged public Bootstrap entry. Record complete
before/after plans, reports, outputs and expected refusals. Genuine function and
builtin arity refusals must occur after plan emission. Retained compile-time
entry/header/policy refusals are regression controls, not new Runtime credit.

Controlled native tests must distinguish sequence, argument, collection,
binding, branch, effect-check and late-Var order, preserve opaque values and
exact failures, and document malformed-plan compatibility without source
admission. Preserve all earlier Runtime bodies and all twenty-six existing CI
vectors; append one whole focused namespace command. Run affected regressions,
the source-language boundary, structural docs/roadmap checks and lint. Obtain
independent semantic, adversarial, composition/provider and CI review against
the exact clean authored tuple.

## Current status and unsupported boundaries

This is the early draft scope. No new Runtime API is implemented or accepted
at this capture. The original nine-path lease governs authoring. Main
integration remains with the designated integrator after exact current-base
preflight and required checks; this manual external source has no Main, Ready,
controller, service or recovery role.

The native interpreter is not canonical Core/MIR instruction admission, typed
host delegation, runtime-family selection, provider grants, complete capability
enforcement, source-map/debug generation, redaction, replay, a portable error
ABI, native/backend execution or full R1/R4/R11/R12 conformance. Existing host
numeric/collection behavior, limited effect inference, missing canonical
provenance/proof facts and permissive malformed-plan behavior remain explicit.
No new performance, safety certificate, release, migration, self-hosting or
seed-retirement support is claimed. Held Reader/authenticated/private builders,
factories and source pins remain unchanged and uninvoked. Gravity/Uniform
remains the destination implementation language.
