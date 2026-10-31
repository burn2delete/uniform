# Platform

Working name: **Platform** (proposed). Semantic key: `platform`.
Owner role: platform architecture and target maintainers; current owner
unassigned by this proposal. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit platform ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).

## Purpose and scope

Track the full hardware, firmware, operating-system/kernel and device-extension
surface separately from the compiler/runtime release. Contracts live in the
[domain index](../phase-09-domain-specific-computing-coverage/README.md),
[backend index](../phase-07-backend-architecture/README.md) and
[platform library index](../phase-16-standard-library/README.md).
An executable compiler on an existing OS is not an implementation of a new OS,
and a hosted native adapter is not a hardware backend.

## Release outcome

For the explicitly adopted platform target matrix, real hardware artifacts,
firmware images, kernel modules and device interfaces satisfy their target
contracts with accepted/rejected fixtures and source-to-artifact provenance.
This is an extension outcome, open and not a prerequisite for Reader completion.

## Interfaces and dependencies

Inputs: verified MIR/domain IR, explicit memory/layout/interrupt/clock facts,
profile legality, device grants, target ABI and runtime-service manifest.
Outputs: target images/modules, linking/layout records, driver/provider APIs,
simulation or execution traces and deployment evidence. Requires Lower/Emit,
Legality/Auth and applicable Runtime/Stdlib outputs for integration. Target ABI,
simulation fixtures and architecture decisions may proceed in parallel.

## Finite projects and remaining tasks

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).

These are separate component projects, not an assertion that one generic target
covers all platforms. Baseline evidence includes
[Darwin native publication bindings](../../bootstrap/clojure/src/gravity/darwin_publication/native_runtime.clj),
which explicitly require a specific host/runtime and only establish that source
boundary. They do not establish any of the four outcomes above.

## Architecture decision gate

Before a target project claims implementation scope, the platform architecture
owner must record target/ABI, accepted artifact kind, simulator/device evidence
and required service set. Recommendation: choose one explicit reference target
per artifact family, keep platform adapters separate from language semantics,
and extend the matrix only with named conformance evidence. No particular
hardware or OS architecture is adopted here; that uncertainty remains distinct
from completed compiler development. This gate is a concrete target-selection
record, not an unbounded task to "finish architecture".

## Acceptance and integration

Yes/no for each adopted target: does the artifact actually simulate/execute? Are
layout/clock/interrupt contracts checked? Are forbidden services rejected? Are
unsafe islands and grants auditable? Is provenance retained? All must be yes.
Integration milestones: accepted target decision; Lower/Emit artifact; runtime
or no-runtime execution; standard-library adapters. Full platform completion
requires all adopted project outcomes, not native compiler availability alone.

## Full Uniform platform product (separate from domain conformance)

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/platform-workflow/tasks.md#platform-product-scope).
