# MLIR native diagnostic policy data

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B7, B13, B14, BOOT8 and TEST2 govern
this extraction. Four existing native functions move to gravity.backend.mlir:
source-overrides, missing-fact, mlir-operation and dialect. Inputs are trusted
native module metadata and diagnostic ids. Outputs are borrowed override values
and existing diagnostic context labels. Bootstrap consumes the stage; the
stage requires only clojure.core and imports no orchestrator or consumer.

The stage owns override precedence and native rule-context defaults. Existing
B7 wrappers retain their arities and interposition. Refusal decisions, failure
construction, catalog-stream construction, source admission and publication
remain with their existing owners. No callback, proof or input authority is
introduced. Whole-file conservation checks preserve the original lookup bodies
and all surrounding component and Bootstrap code except explicit delegation.

## Artifacts and checks

Focused tests emit and read back the actual native B7-MANIFEST diagnostic from
an empty malformed artifact, retaining its supplied source context. Native
catalog records retain supplied input identity, source context, ordering and
serialized determinism. These catalog rows are compatibility examples, not
ten observed semantic failures. Override-refusal controls retain each existing
rule, and data tests cover precedence, borrowed identity, defaults and late
stage/wrapper interposition. Complete before/after captures and source digests
must be checked from a frozen parent and candidate before acceptance.

The first full Gravity B7 compilation probe failed with L2-UNKNOWN-CORE-FORM
for the :required-fields keyword call. That nonpass remains evidence; no source
form, compiler mode or pin is changed to obtain acceptance. The initial native
baseline was not confirmed finished before editing, so it receives no baseline
credit. Fresh frozen-parent controls are required and recorded in the draft PR.

## Unsupported boundaries

This is native diagnostic data extraction, not a working MLIR emitter. Full
Gravity B7 source compilation, verified MIR/domain ingress, MLIR toolchain
verification, conversion legality, proof/metadata validation, LLVM/GPU handoff,
source authentication and public diagnostic redaction remain unsupported.
Legacy complete/passed/proof labels confer no authority. No native broker,
restricted factory, frozen Reader, package/archive/release builder or Attempt30
route executes. Migration, release, self-hosting and seed retirement are not
claimed. Independent review, own CI/artifacts and exact clean current-main
integration preflight remain required.
