# Native MIR validation sequencing and report

## Governing contracts and ownership

D1/D2/D3/D8/D9, C1/C6-C11/C15, BOOT8 and TEST2 govern this extraction.
Parent source 2340d64dcc54758a9a2555252126c142d270afb4 has tree
11bf3807c91210f5debc65e04d10a4099779ea9e. Its review and CI remain
separate; they do not accept this successor.

Move only p15-s23-c11-mir-validate-constructed!* from component 00740 into
gravity.mir. The stage owns the native sequence: semantic carrier validation,
module envelope, fact tables, operation products, runtime checks, CFG, effect
placement, data flow, then report assembly. Bootstrap keeps its old facade,
diagnostic dynamic binding and containment wrapper, source/compiler binding,
Core policy and authentication/ingress code. The legacy bounded-value! callback
already validates semantic native carriers without quotas; preserve its live
operation and do not add accounting or input allowances.

## API, artifacts and dependencies

The new validate-constructed! API consumes native checked-Core/MIR data, source
path and eight explicit owner operations. It emits the same native verification
report or propagates the existing refusal/exception. Preserve per-use callable
selection before arguments, exact phase order and arguments, first refusal,
returning failures, borrowed runtime-check/data-flow records, counts, CFG
conditional value, source provenance and input identities. No source admission,
input authentication or verifier certificate follows from a passed native
report. The stage imports/discovers no Bootstrap owner.

## Tests, CI and acceptance

Use the existing paired SH12 normalized Gravity fixture and real Gravity C11
builder through ordinary fixed-seed plan compilation. The existing MIR test
products force only accepted fixture and C11 builder delays, not parameter,
private Core ingress or source-admission routes. Capture complete Core/MIR,
compiler/source text hashes, report and real tamper refusals. Compare immutable
old algorithm and extracted algorithm on identical live genuine inputs; do not
infer parity from lossy host serialization. Add phase/argument/interposition,
refusal/exception, borrowed identity and deterministic report controls.

Existing gravity.mir.pipeline-test passes 43 tests / 1158 assertions on the
source parent in 14.43 seconds. It is currently absent from hosted CI. Add one
fresh native MIR command to the existing checked-core-runtime group with the
existing 2g heap/180-second child boundary. Keep every prior command, mode,
workflow and stage-path selection unchanged. CI controls must prove complete
namespace/deftest selection and detect omission. This is required coverage for
the slice, not a new framework or authority gate.

Run focused/genuine composition, boundary/ownership, real harness controls,
contextual lint and validators, then independent semantic/adversarial review,
own exact hosted CI and clean current-base coordinator integration. Maintain
the early draft. The scope record itself earns no implementation credit.

## Unsupported boundaries

This does not authenticate checked Core, MIR, callbacks or sources; issue a
certificate; implement canonical C15 identity/publication; establish general
CFG, loop, match, exception or domain support; execute a backend; or establish
migration, self-hosting, release or seed retirement. Private C6/C11 policy,
default/frozen Reader, broker/factory, staging, package and release routes are
not exercised. Native validity labels remain data under the existing narrow
single-entry and single-conditional contracts.
