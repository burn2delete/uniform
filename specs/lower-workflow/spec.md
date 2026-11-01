# Target lowering implementation specification

Implementation scope added under the 2026-11-01 request. Existing target,
runtime and backend owners retain their scope. Historical workflow transfer
below establishes no target support or executable lowering acceptance.

## Behavior and interfaces

Implement [the Lower outcome](../../docs/component-roadmaps/lower.md#release-outcome)
under [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
and [B1](../../docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md).
Consume actual verified MIR or permitted verified anchored domain IR, explicit
profile/target/ABI, runtime/provider contracts, effects/capabilities and current
safety/proof/source/dependency inputs. Produce backend-ready operations,
eligibility and unsupported-feature records, ABI/layout/runtime decisions,
proof-to-target metadata maps, source/generated maps and diagnostics for Emit
and the selected backend adapter.

The existing staged Linux LLVM compiler candidate remains the selected release
path. Hosted development artifacts and other required backend/profile families
retain separate outcomes. Lowering cannot legalize rejected source or rely on
target undefined behavior, implicit services or wider authority. Opt may supply
optimized MIR, but valid verified unoptimized MIR remains a legitimate input.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`lower/current-evidence-and-remaining-work/`; current criteria, evidence,
source-family refinements and task status remain there.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `lower1-validate-lowering-inputs` | Verify the actual input and current fact/proof dependencies, target/profile/features, representable operations, services and domain verifier results; emit explainable eligibility. | Reject unverified/stale IR, incompatible profiles/features and unsupported operations before emission. Include actual source/op/anchor, missing requirement and legal alternatives in diagnostics. |
| `lower2-derive-abi-and-layout` | Derive actual calling/export, aggregate/closure, error/panic, resource/FFI, stack/heap/GC, device/address-space and debug/unwind representations from explicit target rules. | Reject unrepresentable exports, layout/alignment/width/address-space conflicts, lost pointer provenance and illegal cleanup or unwind strategy. Bind exact ABI/layout manifests and real selected-target fixtures. |
| `lower3-select-explicit-providers` | Select actual allocation/check/error/atomic/math/FFI/IO/domain/debug services matching profile, target, effects, grants and safety; record explicit no-service cases and legal fallbacks. | Reject missing or hidden hosted services, undeclared runtime dependencies and fallbacks that widen effects/authority or weaken the semantic contract. Test actual selected helper/provider linkage with its runtime owner. |
| `lower4-preserve-proofs-and-origins` | Revalidate current assumptions for target width/alignment/memory-order/numeric/exception changes, and map each emitted target assumption to applicable Gravity evidence. Retain source/generated, safety, audit and dependency edges. | Reject unjustified no-overflow/no-alias/dereferenceability/fast-math and invalidated elision. Conservatively omit optional metadata, retain a legal check or reject when assumptions cannot survive; a metadata claim is not its proof. |
| `lower5-integrate-target-emission` | Feed actual selected Linux LLVM backend-ready input through Emit and the real adapter with accepted programs, unsupported-feature negatives and preserved manifests. | Reject changed-input, missing-service and invalid-proof cases at the owning boundary. Verify actual resulting behavior and artifacts with Emit/Runtime; hosted compatibility cannot establish the selected native release or every target. |

LLVM composition also retains B3's pinned triple/data layout/CPU/features,
relocation/code model, ABI, sanitizer/unwind/runtime selection and actual LLVM
verification before/after configured passes. Safe arithmetic, pointers, atomics,
volatile/MMIO and numeric modes cannot acquire poison/undefined behavior from
target attributes or tool defaults. Backends own their concrete mappings; Auth
owns grants and trust, and runtime owners supply actual service implementations.

## Starting work and completion

Declared MIR/backend/runtime interfaces and verified fixtures permit local
lowering before whole-component release. Real composition uses actual selected
producer, verifier, adapter and runtime services. Completion requires the
declared supported-operation/target matrix, real ABI/provider/proof mapping,
accepted/rejected lowering and actual Emit consumption. Preserve C12 and all
other recorded restrictions and writer reservations. Independent acceptance,
integration and seedless signed product release remain separate.

## Historical task-authority-transfer specification

# Feature specification: Lower implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: compiler/backend maintainer.
Input revision: `b394258a06950bcc69d57cae99f9ff6b582d0ad8`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the five existing Lower implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/lower.md) retains its selected
release target, producer/admission requirements, accounting, compatibility and
acceptance wording with a direct task-ledger link. Retain direct navigation to its
[release outcome](../../docs/component-roadmaps/lower.md#release-outcome) and
[remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work).
The [C14 contract](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
remains normative. The reviewed [additional interfaces](plan.md#additional-contract-interfaces)
retain their exact mapped text and authority boundaries.

## User scenarios and acceptance

### US1 - Find one editable implementation task

Preserve all five component/heading/title semantic IDs, lower/1 through lower/5
display labels, exact evidence/status and concrete exit criteria in the sole
task ledger. The roadmap links directly to those records. There is no second
editable implementation ledger or
positional renumbering.

### US2 - Separate workflow delivery from implementation acceptance

Keep the complete historical migration tasks file frozen outside the parsed
implementation region. Its checkboxes do not indicate compiler completion. All
five implementation outcomes remain Open and unchecked. Checking a task requires
explicit Accepted; status and independently reviewed immutable owner evidence.
The ledger records status separately from semantic receipt validity. Workflow
artifacts, metadata and assertion counts do not establish producer, lowering,
emission or runtime acceptance.

## Scope and compatibility

Own the Lower roadmap and four workflow artifacts. Preserve complete task history,
links and all roadmap wording outside additive
task navigation. Preserve the entire reviewed Additional contract interfaces
appendix byte-for-byte. Shared helpers, mapping artifacts, other packages,
compiler/runtime implementation/tests and normative contracts remain unchanged.

## Evidence and outcome ownership

The compiler/backend maintainer retains semantic ownership. Preserve the selected
staged Linux LLVM release candidate and distinguish hosted development. MIR
acceptance remains required; Opt is optional when optimization is not requested.
Runtime/provider schemas remain negotiated interfaces, not a circular requirement
for completed releases. No target change, proof discharge or producer acceptance
is granted by this transfer. See [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Hosted eligibility metadata is not executable target coverage or valid proof for
each attribute. Wider backend families retain their separate ownership. This
migration establishes no compiler/runtime acceptance, component release,
self-hosting, seed retirement or whole-platform support.

## Implementation naming review

`lower-workflow` and its component roadmap name the subsystem and behavior. Follow the
[semantic naming rule](../../.specify/semantic-names.md); compatibility IDs,
historical planning references and compiler source names remain unchanged.
