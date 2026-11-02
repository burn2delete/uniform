# Finite acceptance dependencies

Inspection baseline: `176c4ee65bb3ceb545f51edfaa599902030a0cb7` (2026-10-26).
Outcome owner: dependency and completion-boundary maintainer. Existing component
writers retain their paths. This is the sole detailed FIN cleanup backlog;
[the existing component index](component-roadmaps/README.md#cleanup-completion-boundaries)
provides shared navigation. It does not reproduce component implementation status.

## Scope and completion

This cleanup removes ambiguous, circular or indefinitely extensible acceptance
definitions from the existing roadmap layers. The 240 normative contracts,
their stable IDs, all 31 retained Spec Kit packages, historical evidence,
outstanding decisions and accepted approvals remain. Mapping #422 gives 236
direct package links and four explicit GOV1/GOV6/GOV7/GOV8 proposals, not four
accepted replacements. Future explicitly authorized packages are additions,
not a reason to retire those contracts or rewrite the historical population.

FIN is complete only after FIN-01 through FIN-05 below are independently
reviewed, validated and integrated, with no unowned, infinite or circular
in-scope criterion. This is acceptance of the cleanup definitions and their
executable structural checks. It cannot close any unfinished compiler task,
other cleanup owner's backlog, final self-hosting gate or future language work.
The overall six-outcome cleanup remains open while any owner's declared outcome
is unfinished or blocked. A named role is accountable for resolving a missing
source owner; it is not proof of a source reservation or permission to overwrite.

## Source-grounded cycles

`tools/acceptance_dependencies.clj` reads all 240 canonical documents and records
their SHA256 digests. It reads the component registry and every registered
component roadmap; identifies an implementation ledger only inside its explicit
implementation markers; and excludes historical migration checkboxes outside
those regions. It extracts all 36 full-language tasks and 30 SH completion rows.
Its EDN output contains exact root-relative source identities, directed edges
and strongly connected components. It performs no compiler execution, evidence
authentication, task write or acceptance promotion.

Edges point from consumer to prerequisite. Reading the existing full-language
phase dependency declarations as final-task-completion prerequisites produces
these actual mutually reachable sets (not just pairs of cross references):

| Strongly connected set | Concrete circular requirement | Staged contract and accountable boundary |
| --- | --- | --- |
| FL-P04-T01, FL-P05-T02, FL-P06-T03 | Performance needs lowering; lowering needs certified math; math needs performance/lowering. | Compiler, math and performance maintainers publish explicit IR/certificate interfaces, implement their own operations, then execute certificate-preserving joint lowering. No proofless check elision or math claim passes on an interface. |
| FL-P07-T01, FL-P08-T02 | Backend acceptance requires the replaced runtime; runtime requires the backend. | Backend and runtime maintainers agree ABI/services, then independently validate emitted artifacts and actual runtime behavior on the same candidate. Host execution alone cannot establish seed retirement. |
| FL-P10-T01, FL-P12-T01 | Source schemas need packages; packages need source schemas. | Schema and Build maintainers implement declared schema/manifest interfaces separately, then exercise real hermetic builds, schema weakening rejection and provenance on the joint candidate. |
| FL-P11-T01, FL-P13-T01, FL-P13-T02, FL-P14-T01 | AI and tooling need the harness; the harness requires those feature phases. | Conformance maintainer builds executable selection/diagnostic interfaces first; AI and tooling maintainers supply actual cases. Full-population harness acceptance still requires executing them. |
| FL-P17-T01, FL-P18-T01 | Governance requires public release; release requires governance. | Governance maintainer supplies executable rejection rules; release maintainer produces the real candidate and evaluates them before final acceptance. No governance record is waived. |

The wildcard "all feature phases whose behavior is under test" expands to
phases 01 through 13 plus 16. Foundation is governance of this track; Bootstrap,
Governance and Release are separately gated final consumers. "All previous
full-language tasks" expands to every task in phases numerically before 18.
These expansions are explicit audit interpretation, tested by fixtures, not
inferred runtime dependency discovery. Unmentioned implementation dependencies
must still be inventoried by their actual component owner.

There is also a textual final-proof backedge: FL-P14-T02 requires the self-hosted
public binary, while Phase 15 names FL-P14-T02 as a prerequisite. The task-edge
extractor does not infer this edge from prose. The explicit correction in
[the full-language work order](full-language-implementation-gap-map.md#dependency-stages-and-finite-acceptance)
allows the executable runner implementation to be consumed by the rebuild;
both tasks retain their final proof on the actual rebuilt/public candidate.
It does not accept an unexecuted runner or exclude any required feature.

The 30 SH completion dependencies are acyclic. Their existing source closure,
six acceptance requirements and stopping rules remain in the
[milestone plan](self-hosting-milestone-completion-plan.md).
Component Markdown links are reported separately as reference edges. Reader to
Syntax and Syntax to Reader links, or Auth/Diagnostics references spanning
components, do not by themselves establish circular acceptance. The existing
component index already distinguishes declared interfaces from completed
releases; this correction preserves that distinction at the full-language layer.

## Executable boundary

Each task retains its existing ID and full scope. The staged graph has four
facets: `interface`, `implementation`, `composition`, `acceptance`. These are
evidence dependencies, not a new four-task successor chain. Construction uses
declared interfaces. Composition depends on every original prerequisite's real
implementation, not its document or final checkbox. Acceptance requires that
composition and the complete unchanged task-specific proof gates. The graph
does not encode or authenticate those proof receipts; they remain obligations
of the owning task and independently reviewed acceptance.

The checker verifies all referenced graph nodes exist, identifies true strongly
connected sets (including self edges), and demonstrates that every original
prerequisite remains an executable composition input. It rejects missing or
duplicate IDs, unknown dependency syntax, malformed implementation regions,
unknown prerequisites, SH cycles and a cyclic staged graph. It never treats
deleted unreferenced leaf tasks as closure: the real audit enforces the explicit
existing 36 task identities and SH-00 through SH-29, with malformed-row controls.
An intentional population amendment requires an owning-contract change and
independent review of the new identities. The checker never treats
zero staged cycles as proof that the components run or that all prose has been
formalized. Newly discovered semantic dependencies require explicit owning
contract updates; they cannot be hidden by a green structural report.

Dependency lists consume their whole declaration or table cell: IDs must match
exactly, use the original comma separators and, for full-language declarations,
retain their backticks and final period. `SH-XX`, `SH-003` and `FL-P06-T030`
are rejected rather than silently omitted or truncated. Unknown references,
duplicate references, empty/trailing cells and ambiguous separators reject.
Implementation ledger IDs are read literally inside their marked region;
the census does not require a fabricated component prefix or replace the
owning projector's exact identity validation. Existing Backends keys such as
`backend-llvm` therefore remain unchanged.

Run from the repository root under the existing normal resource brokers:

```bash
clojure -J-Xmx256m -M tools/acceptance_dependencies.clj --check
clojure -J-Xmx256m -M tools/acceptance_dependencies_test.clj
```

The first command prints the graph and source digests; it writes nothing.
The second runs pure graph/parser and real-source read-only controls, including
unknown/self/cyclic dependencies, executable edge conservation, historical
migration exclusion and attempted write/extra-argument rejection. Neither needs
the heavyweight lock, C12 execution or a compiler namespace.

## Finite task-authority migration population

The existing component registry is the finite population, not an endless series
of successor PRs. Each registered component's existing finite task sections must
have exactly one editable implementation authority; the other views are links
or deterministic projections. Compiler API's built-in pipeline, pass evidence
and plugin host; named Backend families; Developer Tools services; domain
projects; and Runtime/Standard Library selected-versus-wider outcomes remain
separate named sections. A migration must preserve every original row, stable
ID, status, exit criterion and historical record in those sections.

The task-authority migration owner owns this transfer and its existing
projectors. Completion is a readback of the fixed registry and every declared
section showing no remaining editable duplicate, missing row or stale
projection, with independent review and integrated evidence. Per-component
successors stop when that population is exhausted. Optional new component
scope requires an explicit decision and a changed registry, not a silent new
condition attached to this cleanup. This criterion does not mark any underlying
implementation row accepted. In-flight Backends and other existing migrations
remain that owner's work; no second writer is created here.

## Finite FIN backlog

| ID | Exact deliverable and dependencies | Yes/no acceptance evidence |
| --- | --- | --- |
| FIN-01 | Source census: canonical inventory/documents, component registry/roadmaps, explicitly marked Spec Kit implementation regions, full-language gap map and SH milestone index. No implementation dependency. | Exact source digests and source-derived graph populations; historical migration boxes excluded; unknown/duplicate/absent-node negative controls. All 240 contracts and 31 baseline packages remain retained, with the four explicit GOV proposals preserved. |
| FIN-02 | Actual SCC classification in this document and `tools/acceptance_dependencies.clj`; depends on FIN-01. | Five coarse full-language SCCs and the explicit runner/public-binary prose backedge explained; SH graph checked; component links labeled reference-only. No invented cycle or claim of exhaustive semantic dependency inference. |
| FIN-03 | Staged contracts in the existing full-language gap map and finite authority-migration population above; depends on FIN-02. | Every original task/status/proof gate and executable prerequisite retained; construction can proceed independently, composition needs actual producers, final proof cannot be satisfied by an interface or synthetic receipt; no new safety deferral. |
| FIN-04 | `tools/acceptance_dependencies_test.clj` plus shared cleanup navigation in the existing component index; depends on FIN-03 and each owner's one detailed contract link. | Positive/negative graph controls, exact source readback, docs/roadmap/language checks, one canonical navigation with ACC/RV/CO/OWN/AS/FIN IDs and no duplicate component status. No broad compiler validation credited. |
| FIN-05 | Independent review and normal integration of the completed FIN increment; depends on FIN-01 through FIN-04. | Exact base/head/tree, scoped passed/failed/unrun receipt, independent review disposition, current-main compatibility and remote/main readback. Keep unfinished work draft. Other owners' unfinished acceptance and restricted operations remain open. |

These records define finite deliverables and carry no inferred progress score.
Observed validation and exact review/integration receipts belong in the change's
public PR record. Historical failed syntax/launcher attempts remain failed;
only successful final-tree commands receive passing credit. No compiler suite,
current-source construction, C12 run, authoritative migration or public seedless
acceptance is part of this documentation/tooling increment.

## Future task construction and composition

The crosswalk captures the exact forty future task bodies from the four
existing marked regions in Domains, Release and Platform. Each keeps its sole
editable component task file, source pin, literal ID, checkbox and full closing
evidence. Capturing these records does not add SH criteria, route keys or
accepted evidence. A changed, missing or duplicated body invalidates this view.

The following declarations formalize the wider Domain tasks' explicit
`Dependencies consume` clauses, the Math/Schema producer and checker boundaries,
the Math parent's independent-verdict consumer, and the Platform Product records together
with [their existing composition plan](../specs/platform-workflow/plan.md#product-decision-and-implementation-order).
They are dependency declarations, not another task/status ledger. Full task IDs
are literal; `none` means this table declares no prerequisite at that boundary.
Each named producer supplies only the selected output described by the
consumer's existing full criteria. A broad parent checkbox is never a proxy
for that output's execution or acceptance.

Construction can use versioned declared producer interfaces. `Start results`
identifies the adopted product decisions needed for selected product work;
ordinary interface/fixture development can precede adoption. `Composition
results` requires real selected outputs before claiming that consumer result.
Start results are also composition inputs. Every selected result stays
execution-and-review-unassessed here, including outputs of checked historical
parents. This view authenticates no execution receipt. Its frontier is advisory
and grants no platform, provider, device or publication permission.

<!-- future-composition-dependencies:start -->
| Consumer | Start results | Composition results | Selected output contract |
| --- | --- | --- | --- |
| `domain-ir-boundaries` | none | `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `math`, `schema`, `workflow`, `ai`, `hardware`, `gpu`, `query`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `math/independent-proof-and-certificate-checking`, `plugins`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission` | Actual verified Core/MIR anchors and selected EFIR/schema/workflow/AI/HDL/GPU/query/FFI/package-graph schemas, payload semantics, verifier and transition products; independent Math proof/certificate checking supplies the concrete required accepted/refused optimization fixture, not universal domain proof authority. Plugin policy supplies actual registration/grant checks. Registered Lower exits and target eligibility/fallback consume already verified domain products; final conformance requires actual emission/refusal where claimed. Domain payload, optimization, registry, proof, backend and runtime owners stay distinct. Interface fixtures start without whole parents; no C12 execution hold is cleared. |
| `automation` | none | `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | Typed operation graph, granted roots/commands, dry-run and actual action audit are distinct outputs. |
| `blockchain` | none | `schema`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | Checked versioned state/ABI schemas and deterministic safe transition/authority facts; preserve selected chain rules and current accounting removal. |
| `crypto` | none | `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` | Secret/key authority, audited boundaries and protocol-specific checks; metadata does not prove cryptographic execution. |
| `formal` | none | `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes` | Surviving obligations and exact emitted subject/proof identities, followed by actual checker/integration results. |
| `native` | none | `backend-llvm`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` | Actual selected native artifact and preserved checks/anchors; performance claims require the consumer's real measurements. |
| `services` | none | `query`, `schema` | Executed typed queries and checked request/result/schema contracts with selected service authority and failures. |
| `simulation` | none | `math`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | Selected numeric/rounding contracts and actual runtime boundary behavior for deterministic simulation. |
| `storage` | none | `schema`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | Checked durable data contracts and actual ownership/FFI execution with recovery and failure cases. |
| `visual` | none | `workflow`, `schema`, `ui` | Checked visual/schema mappings and actual workflow/UI operations; visual graph construction alone is insufficient. |
| `math` | none | `math/independent-proof-and-certificate-checking` | Proof-dependent equality, normalization, transforms, provider use and conformance consume the exact independently checked claim/certificate/verdict; numeric-family declarations alone do not require a completed checker. |
| `math/eml-normalization-and-search` | none | `math`, `math/symbolic-rule-and-trace-production`, `math/independent-proof-and-certificate-checking` | Verified EFIR, numeric mode and guarded rule facts feed candidates/traces; promotion consumes the applicable exact accepted proof or bounded certificate and independent replay. |
| `math/certified-implementation-synthesis` | none | `math`, `math/interval-and-real-proof-production`, `math/independent-proof-and-certificate-checking` | Verified EFIR/mode/floating eligibility and separate real/roundoff ledgers feed executable candidates and certificates; independent exact-subject checking precedes accepted ranking/selection. |
| `math/interval-and-real-proof-production` | none | `math`, `math/independent-proof-and-certificate-checking` | Verified EFIR/mode feeds pending claims and exact domains/partitions/ledgers; allowed residual checks consume independent partition replay, preserving unresolved cells and failure behavior. |
| `math/symbolic-rule-and-trace-production` | none | `math`, `math/independent-proof-and-certificate-checking` | EFIR identity, numeric/proof policy and exact independent verdicts feed rule promotion, guarded rewrite and extraction; registration and candidate trace production precede accepted use. |
| `math/independent-proof-and-certificate-checking` | none | `math`, `math/eml-normalization-and-search`, `math/certified-implementation-synthesis`, `math/interval-and-real-proof-production`, `math/symbolic-rule-and-trace-production`, `pass-evidence`, `formal`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` | Independently check the selected actual EFIR/claim/candidate/certificate, EML/symbolic trace or partition with exact assumptions/trust/target/invalidation; these proof methods are applicable alternatives, not a demand for every method on every claim. |
| `schema/compatibility-and-migration` | none | `schema`, `schema/checked-consumer-projection`, `query`, `backend-query`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` | Actual old/new source and affected projection contracts feed semantic diffs and checked plans; actual granted migration/reload transitions supply result, state, rollback/loss, lifecycle and replay evidence. |
| `schema/checked-consumer-projection` | none | `schema`, `schema/compatibility-and-migration`, `pipeline`, `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `query`, `backend-query`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `pass-evidence`, `release/remaining-tasks-and-evidence/execute-formal-evidence-checks`, `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance` | Same-source generated API/database/ABI/config/artifact products consume applicable checked compatibility/migration and real compiler/handler/FFI/reload/evidence-consumer outputs. Layout, STD10 codec consumption, Runtime call/callback and actual release decisions remain distinct. |
| `platform-product/adopt-hardware-contract` | none | none | Reviewed ISA/alternative, device model, simulator and executable workloads; hardware architect remains unassigned. |
| `platform-product/adopt-kernel-contract` | none | none | Reviewed boot, memory, scheduling, IPC, service and driver interfaces; OS architect remains unassigned. |
| `platform-product/adopt-application-contract` | none | none | Named end-user workloads and accepted/rejected cases; product owner remains unassigned. |
| `platform-product/adopt-physical-boundary` | none | none | Reviewed simulation/FPGA/reference/fabrication scope and required external owners/provenance/availability; hardware/product owner remains unassigned. |
| `platform-product/implement-hardware` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-physical-boundary` | `hardware` | Uniform-authored selected design plus applicable structural/state/reset/timing/CDC/tag target conformance. |
| `platform-product/implement-firmware` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract`, `platform-product/adopt-physical-boundary` | `platform-product/implement-hardware`, `firmware` | Actual selected hardware and startup/boot/image contract plus applicable firmware target conformance. |
| `platform-product/implement-os` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract` | `kernel` | Uniform-authored selected services and applicable kernel target checks; versioned boot/driver interfaces permit independent development. |
| `platform-product/implement-drivers` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract` | `devices` | Uniform-authored drivers on the named substrate and applicable device/provider conformance; service ABI is a declared interface before stack integration. |
| `platform-product/implement-applications` | `platform-product/adopt-application-contract` | `platform-product/implement-os`, `platform-product/implement-drivers` | Actual selected OS/driver service outputs used by every adopted workload, with versioned interfaces available during construction. |
| `platform-product/integrate-execution` | none | `platform-product/implement-hardware`, `platform-product/implement-firmware`, `platform-product/implement-os`, `platform-product/implement-drivers`, `platform-product/implement-applications` | Same actual five-part stack boots and executes declared workloads with isolation, authority and device-failure evidence. |
| `platform-product/distribute-product` | none | `platform-product/integrate-execution`, `platform-product/adopt-physical-boundary` | Reproducible distribution of the actual integrated stack; physical deliverable/bring-up only where adopted. |
| `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | none | `pipeline`, `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` | Actual selected compiler/module L1-L19 fixture results, reader/syntax/Core goldens, types/effects and stable diagnostic/origin comparisons; no completed final compiler is a development prerequisite. |
| `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures` | none | `pipeline`, `plugins`, `pass-evidence`, `development-boundaries`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` | Actual stage/pass composition, generated re-entry, plugin denials, preserved/transformed facts and cache-invalidation results under the named implementation; development feedback is separate from migration admission. |
| `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures` | none | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `schema` | Actual claimed runtime-family, grant, memory/concurrency, replay/AI-ledger, FFI/cleanup and redaction interfaces/results; mock or no-runtime cases retain their declared scope and cannot prove an external service. |
| `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | none | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `backend-interface`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` | Allowed/checked/delegated/rejected compile-time profile facts and actual selected target/runtime/authority matrix, including multi-profile refusal; hosted execution never substitutes for a stricter profile. |
| `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures` | none | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `math/independent-proof-and-certificate-checking`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `ai` | Actual four-outcome safety checks, unsafe audits, proof-dependent elision, capability/taint/AI bypass and required supply-chain refusal results for the claimed subject; every applicable mechanism retains its own owner. |
| `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures` | none | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`, `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis`, `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures`, `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer` | Exact public API inventory, callable behavior/properties, profile/effect denials, safe wrappers, runnable documentation and compatibility results; use explicitly permitted proof evidence only where executable tests are not possible. |
| `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations` | none | `ai`, `workflow`, `schema`, `backend-workflow`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures`, `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer` | Actual named AI/workflow subject, dataset/metric, first-run/replay, grant/deny/expiry/revocation/payload-change, safety and release-policy results. Recorded/mock/live inputs stay distinct; live credentials, real external cost and retention policy remain applicable. |
| `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures` | none | `pipeline`, `pass-evidence`, `schema`, `math`, `workflow`, `ai`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `backend-interface`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` | Versioned generators/shrinkers, seed corpus and independent subject/property oracles feed actual generated accepted/rejected execution, minimized failures and replayable regressions; valid-input compiler quotas are not a resource-failure oracle. |
| `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures` | none | `pipeline`, `pass-evidence`, `backend-conformance`, `math`, `schema`, `workflow`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler` | Actual versioned oracle outputs and declared observables, modes, accepted divergence, diagnostics/artifacts and recorded nondeterminism. Consume the selected comparison operation/results, not its later authoritative admission decision. |
| `release/remaining-tasks-and-evidence/execute-formal-evidence-checks` | none | `formal`, `math/independent-proof-and-certificate-checking`, `pass-evidence`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | Actual declared checker invocation and exact claim/assumption/trusted-basis/input/before-after-IR verdicts, counterexamples and invalidation. This suite exercises existing checkers and supplies TEST11 results; it is not another proof authority. |
| `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | none | `pipeline`, `pass-evidence`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix`, `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer` | Actual artifact/environment/device measurements with variance/repeat policy and declared thresholds, after applicable semantic/safety checks. Preserve separate AI provider/tool/memory/review/replay costs; compiler work allowances confer no pass. |
| `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | none | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures`, `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations`, `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-formal-evidence-checks`, `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures`, `backend-conformance`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | Actual declared stage compiler/rebuild, required implemented-subset TEST1-TEST12 results, raw comparison, policy-applicable provenance/reproducibility, TCB and audit reports. Raw comparison and rebuild products precede intermediate admission; the runner does not require or confer prior authoritative admission or final publication. |
| `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations` | none | `pipeline`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`, `backend-interface`, `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution` | Actual BOOT2 subset/compiler artifact and exact-class compiler-artifact meta/JVM native observations, diagnostics, export exclusions and seed TCB. Retain applicable narrow receipts, unsupported/nil and no-callback/non-forcing behavior; sticky denial and every execution hold remain intact. |
| `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance` | none | `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures`, `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations`, `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-formal-evidence-checks`, `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures`, `backend-conformance`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `schema/checked-consumer-projection`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `evolution/change-review`, `evolution/experiment-lifecycle`, `evolution/library-admission`, `evolution/namespace-decision`, `evolution/registry-yank`, `evolution/rfc-review`, `evolution/security-response`, `evolution/security-review`, `evolution/stabilization-decision`, `evolution/target-support-admission` | Bind each exact claimed component/subset to actual conformance, source/support/matrix, checked schema/trust evidence and applicable governance decisions. Missing or stale producer results must refuse at the real consumer; reporting a claim does not implement its producers. |
| `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler` | none | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures`, `pass-evidence`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` | First compare two actual compiler outputs under applicable canonical hash/manifest/diagnostic/IR/runtime/conformance and conditional performance modes. Then independently admit or deny the exact intermediate module/stage using fresh subset conformance, accepted deltas, matrix/ownership, provenance, TCB and audit evidence. These are separate outputs of this existing authority. |
| `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` | none | `pipeline`, `backend-llvm`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`, `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`, `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` | Actual prior compiler invocation constructs and executes the declared next compiler/runtime/library/build closure. Intermediate compiler products precede final public binary/rebuild/verify claims; initial seed lineage remains distinct from exclusion of Clojure at all four final boundaries. |
| `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom` | none | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `schema/checked-consumer-projection` | Actual selected candidate, canonical provenance/SBOM/signatures under required custody/policy, locked recipes/environments and real reproducible or diverse rebuild results. An intermediate provenance bundle can precede final release; final distribution requires its complete declared closure. |
| `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate` | none | `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `schema/checked-consumer-projection`, `evolution/change-review`, `evolution/experiment-lifecycle`, `evolution/library-admission`, `evolution/namespace-decision`, `evolution/registry-yank`, `evolution/rfc-review`, `evolution/security-response`, `evolution/security-review`, `evolution/stabilization-decision`, `evolution/target-support-admission` | Independent exact-candidate release decision consumes actual claimed support/conformance, intermediate admission, public execution/seed exclusion, reproducibility/trust/schema gates and applicable governance evidence. Author self-checks cannot supply independent acceptance or publication authority. |
<!-- future-composition-dependencies:end -->

These declarations cover all forty captured future consumers, the existing Math
and domain-IR parents and five Release aggregates, with source/routing review
still pending.
The Release suite and seed rows identify selected subject/oracle inputs, followed
separately by stage comparison, intermediate admission and final release. No
declaration supplies execution, acceptance or complete prose entailment. A missing
consumer remains a reported dependency-review gap; no empty adjacency closes it.

The versioned boot and service interfaces resolve construction cycles without
removing the integrated stack's actual five producers. Four product adoption
tasks remain material decisions with unassigned accountable people. A selected
compiler release does not require every product surface universally; complete
project scope still retains those outcomes. Future-to-SH and full-language
finish-line joins, source conjunction review, independent acceptance and
integration remain pending. Existing full-language SCCs and final proof gates
are unchanged by this table.

### Math output dependencies and Schema staging

The checker derives forty-eight existing `math/*` criterion outputs directly
from the exact captured bodies of the Math parent and its five future Math
tasks. For each it preserves the full source-bound criterion, canonical MATH
identity, output declaration, literal `Dependencies:` clause and unchanged
external input contract. Every named Math dependency must resolve to exactly
one criterion in its existing task. Unknown, duplicated or malformed declarations
reject. These are output facts, not forty-eight new tasks or accepted features.
The projection connects cross-task selected results to their exact named output
inputs and reports both the detailed output graph and coarse task backedges.
It does not infer semantic coverage from those edges or clear pending review.

Verified EFIR and explicit numeric facts permit untrusted EML, approximation,
interval and symbolic candidates to be produced before any proof-dependent use.
The independent checker consumes exact recorded candidate/trace/partition inputs;
it cannot trust a producer's pass flag, assume the conclusion or rerun heuristic
search as proof replay. The parent's equality and transform gates consume the
applicable accepted verdict before normalization, extraction, selection or check
elision. A claim chooses a method allowed by D9/MATH5/MATH6/SAFE15; the dependency
declaration does not require all symbolic, interval, SMT and approximation
methods for each claim. The four literal checker criteria and its full MATH5/
MATH10 certificate contract remain one checker authority. The older parent
checker allocation stays qualified for current repartition review.

The detailed literal Math output graph exposes which input facts precede each
operation. Its acyclicity does not prove that every prose dependency has been
formalized: independent certificate use, general compiler facts, target/provider
execution, foundation/profile/safety clauses and full conformance remain explicit
task contracts and selected-output obligations. Current accounting removal still
supersedes historical compiler fuel, candidate/depth/iteration allowances and
quota-only refusals. Preserve numeric error, domain, branch, outward rounding,
target representability and actual host/provider constraints.

Schema's coarse projection/compatibility backedge also remains visible. S1
source validators and S2/S3/S7 contracts permit an initial typed projection
candidate without a migration verdict. Compatibility consumes that exact old/new
projection pair, then produces the checked diff and applicable migration or
reload plan. Actual generated consumers, migration execution or reload use the
matching checked policy/result and declared Runtime authority. S7 layout stays
with Lower, codecs with their STD10/Schema boundary and actual call/callback
checking with Runtime. S9 validates the exact evidence graph and required checker
results; its gate grants no publication authority. The complete inherited C6
consumer conjunction remains required, with its later source amendment and
current independent review qualification preserved.

### Release suite, comparison and admission boundaries

The thirteen existing Release records retain 77 literal source criteria. The
checker derives their exact task-relative UTF8 spans, source hashes, declaration
kind and owning task from the captured bodies. Each TEST clause keeps its full
concrete acceptance text; the four BOOT2/native clauses keep their complete
implementation/refusal/output contract. Their primary evidence-baseline links
remain navigation, not proof that every canonical source clause is covered.
Source predicates are not new tasks or accepted implementation units.

Suite construction uses declared stage, subject, fixture and oracle interfaces.
Composition requires actual results for the claimed implementation/subset,
including positive and negative execution, diagnostics, provenance, invalidation
and permitted reference behavior. A mock, recorded trace, live provider and
actual target are labeled by their scope. TEST7 permits explicit proof evidence
where execution is impossible; TEST8/TEST9 retain real external credential,
spending and retention policy. No synthetic compiler quota, fresh migration
admission or final public compiler is required merely to develop focused tests.
TEST6 remains with `backend-conformance`; Release consumes its actual results.

The apparent stage cycle has distinct producer results:

1. The existing actual-compiler task invokes the named prior compiler to build
   and execute the declared candidate. Its intermediate artifact and lineage
   can exist before its final public-binary/seed-exclusion outcome.
2. Suite owners run applicable TEST1-TEST12 cases using actual selected subject
   interfaces; the stage-comparison owner compares both real outputs and records
   accepted or refused differences under the declared modes. Raw comparison
   computation precedes authoritative admission.
3. TEST13 executes rebuild/conformance/diagnostic/trust checks and reports exact
   stage/compiler/support/TCB/audit and comparison inputs. Required signing is
   policy-specific; an intermediate provenance bundle is not final distribution.
4. The existing stage-comparison aggregate independently admits or denies the
   exact intermediate module/stage using those actual results and its versioned
   compatibility/ownership matrix. The runner cannot admit its own candidate.
5. Final distribution and the independent release-decision consumer require the
   complete claimed closure, actual public commands/rebuilds, all four Clojure
   exclusions, applicable governance/trust evidence and real final artifacts.

Coarse producer cycles remain in the report. A selected-result node names the
consumer-specific artifact above, not the entire producer's completion. In
particular TEST10/TEST13 consume comparison operation/results before the later
admission result; raw stage/provenance production does not wait for final
publication. All selected facts remain execution/review-unassessed. The unchanged
full-language/SH gates still require their complete applicable conjunctions;
these declarations alone do not connect or close the full project finish line.
Native observations retain exact-class/no-callback/export-exclusion behavior,
narrow historical receipts and sticky denial. No held execution or publication
operation is authorized or performed by this projection.

## Full-language and release finish-line joins

These are source-backed producer declarations within the existing dependency representation.
The original 36 full-language tasks, seven Phase18 tasks, terminal source gates
and SH21-SH29 retain their complete source scope, proof rules and recorded status.
Selected result nodes require real consumer-specific outputs, never completion
of an entire broad producer merely to start construction. Earlier checked audits
and hosted packaging remain historical scope; every current proof is unassessed.

<!-- finish-line-dependencies:start -->
| Consumer | Selected producers | Condition | Required result contract |
| --- | --- | --- | --- |
| `FL-P00-T00` | `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` | always | Existing bounded public-surface audit, actual command outcomes and unsupported/missing-command report at their recorded scope; the checked historical audit is not current language completion. |
| `FL-P00-T01` | `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`, `pass-evidence`, `schema/checked-consumer-projection` | always | Existing coverage-matrix/owner/scaffold classification and missing-owner controls over all 240 contracts; source links and validator success alone do not prove semantic coverage. |
| `FL-P00-T02` | `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `schema/checked-consumer-projection`, `development-boundaries` | always | Existing roadmap/coverage overclaim checks and actual accept/refuse evidence preserve audit scope and deny manifest-only product completion; no new development admission gate. |
| `FL-P01-T01` | `reader/finite-remaining-work/contract-coverage-reconciliation`, `reader/finite-remaining-work/decode-and-source-identity-closure`, `reader/finite-remaining-work/lexical-products-and-origins-closure`, `reader/finite-remaining-work/extension-policy-closure`, `reader/finite-remaining-work/authentication-and-quota-removal-closure`, `reader/finite-remaining-work/fresh-syntax-handoff`, `reader/finite-remaining-work/component-release-evidence`, `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` | always | Entire L1 source-unit/reader surface, both extensions and actual original paths, malformed/identity refusals, real public check and provenance; preserve valid narrow Reader receipts and current holds. |
| `FL-P01-T02` | `syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers`, `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs`, `resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers`, `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses`, `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order`, `pipeline` | always | Actual syntax/origins/hygiene, macros, namespaces/import/export, verified core and representative dispatch/pattern/error behavior through the real compiler. |
| `FL-P01-T03` | `types/current-evidence-and-remaining-work/types1-solve-core-constraints`, `types/current-evidence-and-remaining-work/types2-check-declared-abstractions`, `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit`, `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts`, `types/current-evidence-and-remaining-work/types5-integrate-typed-core`, `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects`, `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects`, `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering`, `effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation`, `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions`, `pipeline`, `plugins`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | always | Actual full L5-L19 typed/effect/memory/meta/facet/provider/interop surfaces and stable owning refusals without host semantic substitution or ambient authority. |
| `FL-P02-T01` | `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes` | always | Exactly four safe outcomes with preserved facts and actual accepted/rejected/runtime-check/audit evidence; no fifth implicit undefined outcome. |
| `FL-P02-T02` | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `pass-evidence`, `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins` | always | Actual ownership/borrow/region/lifetime/linear/unsafe/taint and safe-wrapper boundaries; static rejection remains compile time where required and permitted residual checks execute. |
| `FL-P03-T01` | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures` | always | Compile-time profile declarations, legality and compatibility across every required profile and target, with real allowed/rejected fixtures and artifact facts. |
| `FL-P03-T02` | `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers`, `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks`, `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes`, `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema` | always | Actual effect/grant/profile decisions survive package, tooling, lowering and release records without weakening profile semantics. |
| `FL-P04-T01` | `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts`, `opt/current-evidence-and-remaining-work/opt2-implement-structural-families`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate`, `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline`, `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins`, `pass-evidence`, `math/independent-proof-and-certificate-checking` | always | Actual semantic/proof-preserving optimization, specialization, layout, vector/realtime/PGO eligibility and check-elision results; runtime objectives and physical bounds do not restore compiler quotas. |
| `FL-P04-T02` | `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate` | always | Actual source/target/environment-bound benchmarks and correctness/safety gates feed release refusals for missing/stale/incompatible performance evidence. |
| `FL-P05-T01` | `math`, `types/current-evidence-and-remaining-work/types1-solve-core-constraints`, `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` | always | Actual numeric tower/mode/floating semantics, legal conversions, provider/runtime execution, both-extension fixtures and source-bound matrices/diagnostics. |
| `FL-P05-T02` | `math`, `math/eml-normalization-and-search`, `math/certified-implementation-synthesis`, `math/interval-and-real-proof-production`, `math/symbolic-rule-and-trace-production`, `math/independent-proof-and-certificate-checking`, `pass-evidence`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins` | always | Actual EFIR semantics, EML candidates/traces, synthesis, interval/symbolic artifacts, independent checking and conformance; proof-dependent consumers must use the applicable exact accepted verdict. |
| `FL-P06-T01` | `pipeline`, `reader/finite-remaining-work/fresh-syntax-handoff`, `syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers`, `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs`, `resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers`, `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses`, `types/current-evidence-and-remaining-work/types5-integrate-typed-core`, `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers`, `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `domain-ir-boundaries`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission`, `emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers` | always | Real canonical compiler and fused/unfused observable stage boundaries, with actual pass inputs/outputs/facts/origins/verifiers; preserve the explicit D1/C1 ordering qualification. |
| `FL-P06-T02` | `diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`, `diag/remaining-tasks-and-evidence/preserve-primaryrelatedgenerated-origins-and-stable-order`, `diag/remaining-tasks-and-evidence/close-textjsonredaction-and-explain-contracts`, `diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift`, `pipeline`, `plugins`, `pass-evidence`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation` | always | Actual stable diagnostics, incrementality/invalidation, plugin sandbox/pass APIs, translation/preservation checks and minimized regressions rather than generic unsupported-source results. |
| `FL-P06-T03` | `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir`, `mir/current-evidence-and-remaining-work/mir2-attach-fact-tables`, `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality`, `mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules`, `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption`, `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts`, `opt/current-evidence-and-remaining-work/opt2-implement-structural-families`, `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors`, `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate`, `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline`, `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `lower/current-evidence-and-remaining-work/lower3-select-explicit-providers`, `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission`, `domain-ir-boundaries`, `emit/current-evidence-and-remaining-work/emit4-establish-conformance` | always | Actual typed MIR/domain IR, optimization and target lowering with all semantic and proof metadata; emit executable artifacts for earlier feature families, preserving held C12 operations. |
| `FL-P07-T01` | `backend-interface`, `backend-emission`, `backend-conformance`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission` | always | Real verified backend inputs, runnable emissions, ABI/source/debug maps, manifests, provenance and conformance for the declared scope. |
| `FL-P07-T02` | `backend-interface`, `backend-c`, `backend-llvm`, `backend-wasm`, `backend-jvm`, `backend-js`, `backend-mlir`, `backend-gpu`, `backend-hdl`, `backend-workflow`, `backend-ai`, `backend-query`, `backend-mobile`, `backend-emission`, `backend-conformance` | always | Actual per-target implementations and conformance for claimed targets, staged coverage for every required family and explicit source-owned unsupported-target refusal; no target support from a manifest alone. |
| `FL-P08-T01` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `schema/checked-consumer-projection` | always | Actual selected no/minimal/managed/memory/concurrency/distributed/AI/REPL/FFI/authority/observation services, typed failures, replay, audit and redaction. |
| `FL-P08-T02` | `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | always | Gravity-authored runtime executes real product behavior with exact residual seed/host boundaries; a renamed Clojure instruction runner is not replacement. |
| `FL-P09-T01` | `math`, `schema`, `query`, `workflow`, `ai`, `gpu`, `ui`, `math/eml-normalization-and-search`, `math/certified-implementation-synthesis`, `math/interval-and-real-proof-production`, `math/symbolic-rule-and-trace-production`, `math/independent-proof-and-certificate-checking`, `schema/compatibility-and-migration`, `schema/checked-consumer-projection`, `automation`, `blockchain`, `crypto`, `formal`, `native`, `services`, `simulation`, `storage`, `visual`, `hardware`, `firmware`, `kernel`, `devices`, `domain-ir-boundaries`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission`, `emit/current-evidence-and-remaining-work/emit4-establish-conformance` | always | Actual documented domain/target registration, lowering, integration and accepted/rejected replacement-claim evidence. Generic target support does not implement selected Platform Product programs. |
| `FL-P10-T01` | `schema`, `schema/compatibility-and-migration`, `schema/checked-consumer-projection`, `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | always | Actual same-source schemas/validators/codecs/canonical bytes, API/database/ABI/config/artifact consumers and migration/reload evidence, including the full qualified C6 conjunction. |
| `FL-P11-T01` | `ai`, `workflow`, `schema/checked-consumer-projection`, `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations` | always | Actual AI declarations/provider/tool/memory/policy/review/eval and durable workflow behavior, recorded first-run/replay, grants/taint/secret refusals and release-bound subject identities. |
| `FL-P12-T01` | `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `schema/checked-consumer-projection`, `evolution/change-review`, `evolution/experiment-lifecycle`, `evolution/library-admission`, `evolution/namespace-decision`, `evolution/registry-yank`, `evolution/rfc-review`, `evolution/security-response`, `evolution/security-review`, `evolution/stabilization-decision`, `evolution/target-support-admission` | always | Actual project/lock/build/package/dependency/registry/signing/SBOM/target/provenance operations, hermetic effects and canonical identity; applicable governance decisions stay separate producers. |
| `FL-P13-T01` | `cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/connect-replformatlintdocinspectprofileauditairegistryexplain-services`, `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`, `pipeline`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` | always | Real public check/run/compile/run-compiled/test/self-host verification and both source extensions; selected executable services precede final full-language public-binary proof. |
| `FL-P13-T02` | `tools/finite-service-outcomes-and-remaining-work/format-formatter-maintainer`, `tools/finite-service-outcomes-and-remaining-work/lint-analysis-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/language-server-editor-maintainer`, `tools/finite-service-outcomes-and-remaining-work/debug-debugger-maintainer`, `tools/finite-service-outcomes-and-remaining-work/repl-interactive-runtime-tooling-maintainer`, `tools/finite-service-outcomes-and-remaining-work/ir-explorer-compiler-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/profile-performance-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/dev-server-development-session-maintainer`, `tools/finite-service-outcomes-and-remaining-work/registry-view-package-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/safety-explorer-safety-tool-maintainer`, `tools/finite-service-outcomes-and-remaining-work/ai-development-ai-tool-maintainer`, `pipeline`, `plugins`, `diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift` | always | Actual REPL/format/lint/LSP/debug/docs/server/registry/IR/profiler/safety/AI and UI services consume the same compiler facts and reject stale/unsafe/authority/secret/origin failures. |
| `FL-P14-T01` | `release/remaining-tasks-and-evidence/execute-agent-and-workflow-evaluations`, `release/remaining-tasks-and-evidence/execute-compiler-preservation-fixtures`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-formal-evidence-checks`, `release/remaining-tasks-and-evidence/execute-fuzz-and-property-fixtures`, `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures`, `release/remaining-tasks-and-evidence/execute-profile-compliance-fixtures`, `release/remaining-tasks-and-evidence/execute-runtime-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-safety-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `release/remaining-tasks-and-evidence/implement-declared-seed-compiler-and-native-observations`, `backend-conformance`, `release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`, `schema/checked-consumer-projection` | always | Actual complete source/fixture/diagnostic/artifact/oracle/proof-command matrix and harness execution; fail scaffold-only coverage and retain every applicable suite and native/seed restriction. |
| `FL-P14-T02` | `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution` | always | Actual Gravity-native runner is available for candidate construction; its full final suite proof is evaluated through the actual self-hosted public binary, not a subset bridge. |
| `FL-P15-T01` | `pipeline`, `pass-evidence`, `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` | always | Actual Gravity-authored compiler modules compile the next stage with meta legality, owned interfaces, source/generated provenance and module conformance; file presence alone does not prove migration. |
| `FL-P15-T02` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | always | Actual self-build/rebuild, full applicable conformance, accepted/rejected/IR/runtime/artifact comparisons and independently checked trust/provenance/TCB evidence on the real candidate. |
| `FL-P15-T03` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup`, `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects` | always | Actual exclusion of Clojure from compiler/runtime/library/package/build/public-executable behavior, with explicit external toolchain residuals and separate recovery seed; no retirement from flags alone. |
| `FL-P16-T01` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`, `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis`, `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters`, `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests`, `release/remaining-tasks-and-evidence/execute-library-conformance-fixtures`, `tools/finite-service-outcomes-and-remaining-work/docs-documentation-tool-maintainer` | always | Gravity-authored callable APIs, safe wrappers, all profile/module/compatibility/doc/fixture contracts and real compiler-client build results; bootstrap subset receipts do not establish all STD1-STD20. |
| `FL-P17-T01` | `evolution/change-review`, `evolution/experiment-lifecycle`, `evolution/library-admission`, `evolution/namespace-decision`, `evolution/registry-yank`, `evolution/rfc-review`, `evolution/security-response`, `evolution/security-review`, `evolution/stabilization-decision`, `evolution/target-support-admission`, `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate` | always | Actual applicable RFC/compatibility/security/target/experiment/deprecation/unsafe/package policy producers and release refusal behavior; governance rule execution precedes independent final release acceptance. |
| `FL-P18-T01` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`, `emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` | always | Real self-hosted public binary dispatches complete required language/compiler/runtime behavior with both extensions, actual paths and stable feature-specific diagnostics, without whitelist or hosted fallback. |
| `FL-P18-T02` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | always | Actual full designed-language public check/run/compile/emitted execution/test/self-host verify, complete conformance/provenance/SBOM/governance and all seed exclusions; this exceeds SH29's first supported seedless subset. |
| `P18-T00` | `reader/finite-remaining-work/fresh-syntax-handoff`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` | always | Both extensions remain first-class with actual path/extension provenance and accepted/rejected command parity; retain the recorded narrow proof. |
| `P18-T01` | `cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` | always | Recorded thin wrapper, bootstrap-hosted labeling, command forwarding and explicit recovery surface; historical wrapper completion is not final seedlessness. |
| `P18-T02` | `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`, `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` | always | Recorded packaged JVM CLI/parity and package/provenance/SBOM/signature evidence at its bootstrap-hosted scope, with seedless overclaim refusal. |
| `P18-T03` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers`, `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` | always | Actual stage3/self-hosted public artifact emission and source/debug/lineage evidence; Clojure packaging fails and final acceptance needs genuine P15 retirement. |
| `P18-T04` | `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `release/remaining-tasks-and-evidence/execute-language-conformance-fixtures` | always | Actual public commands/apps/executable results and stable refusals retain original subset bridge history. Final full runner and successful self-host verification require separate actual final evidence. |
| `P18-T05` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | always | Actual binary/compiler/runtime/release-compiler exclusion with library/build closure, TCB/provenance and separate recovery, preserving incomplete candidate and sticky refusal history. |
| `P18-T06` | `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` | always | At least two actual final rebuilds compare binary/provenance/SBOM/signing/command identities and complete applicable governance; required final custody/signing is not waived by an intermediate policy. |
| `P15-S23` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation` | always | All independently unresolved whole-compiler/self-build/seed-exclusion/application/conformance/governance/TCB/provenance capabilities close together on actual evidence; terminal flags alone are not a proof. |
| `final-phase-18-completion-gate` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services` | always | All seven existing task artifacts, actual final public command/app/negative/full-suite/self-host checks, source provenance, docs/tests and final P18-T06 proof after genuine P15 retirement. |
| `SH-21` | `pipeline`, `pass-evidence`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks`, `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers`, `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks` | always | Complete selected compiler modules run actual meta legality/generated re-entry/grant/audit checks; arbitrary plugins and all other profiles remain outside this stop. |
| `SH-22` | `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis`, `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis`, `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries`, `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`, `schema` | always | Actual complete transitive bootstrap API/client closure with canonical/text/numeric/resource/IO contracts and refusals; this is not all twenty library documents. |
| `SH-23` | `build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `build/remaining-tasks-and-evidence/implement-dependency-aware-caching-and-invalidation`, `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records`, `build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix` | always | Actual fixed compiler lock/graph/actions/bytes/provenance and controlled denials through Gravity-owned build, without Clojure orchestration; not a public registry or all plugins. |
| `SH-24` | `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/normalize-json-text-exit-codes-and-redaction`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints`, `pipeline`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` | always | Actual selected Gravity driver/compiler-bootstrap commands, structured status/diagnostics and both-extension provenance; not every IDE or tooling surface. |
| `SH-25` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `pipeline`, `pass-evidence`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects`, `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations`, `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` | always | Actual complete declared compiler/generated/runtime/library/build component compilation and execution; inventory DATA and fake output graphs cannot close component build. |
| `SH-26` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects` | always | Actual prior Gravity compiler invocation builds, links and executes the complete next stage, recording outcomes, conformance and lineage without Clojure in product paths; not yet equivalence. |
| `SH-27` | `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures`, `release/remaining-tasks-and-evidence/execute-performance-regression-fixtures` | always | Two actual clean compiler rebuilds, canonical outputs, diagnostics/conformance/runtime/IR comparisons and reviewed deltas. Only applicable real performance semantics survive removed compiler accounting. |
| `SH-28` | `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `evolution/security-review`, `evolution/target-support-admission` | always | Actual independent/diverse trust anchors, traversable lineage, required canonical signatures/SBOM/revocation, audits/TCB and exact seed exclusion, with residual OS/toolchain/provider trust explicit. |
| `SH-29` | `release/remaining-tasks-and-evidence/produce-actual-self-hosted-compilerruntime-execution`, `release/remaining-tasks-and-evidence/build-reproducible-signed-distributions-with-provenancesbom`, `release/remaining-tasks-and-evidence/obtain-independent-acceptance-for-the-exact-release-candidate`, `release/remaining-tasks-and-evidence/complete-stage-compatibilityequivalence-across-the-claimed-compiler`, `release/remaining-tasks-and-evidence/execute-staged-compiler-validation`, `cli/remaining-tasks-and-evidence/connect-checkruncompilebuildtestpackageverify-to-real-services`, `cli/remaining-tasks-and-evidence/package-and-verify-user-entrypoints` | always | First honest public seedless release for the complete declared bootstrap closure and target, with applicable Phase18 command/apps/trust evidence; preserve this stop and do not infer 240/240 language coverage. |
| `FL-P09-T01` | `platform-product/adopt-hardware-contract`, `platform-product/adopt-kernel-contract`, `platform-product/adopt-application-contract`, `platform-product/adopt-physical-boundary`, `platform-product/implement-hardware`, `platform-product/implement-firmware`, `platform-product/implement-os`, `platform-product/implement-drivers`, `platform-product/implement-applications`, `platform-product/integrate-execution`, `platform-product/distribute-product` | platform-product-adopted | Only the explicitly adopted Platform Product boundary requires its selected hardware/firmware/OS/driver/application/integration/distribution results. Unadopted material decisions remain visible, separate from generic target conformance and the first compiler release. |
<!-- finish-line-dependencies:end -->

All original full-language dependency edges survive at their existing stages.
Construction uses interfaces; composition requires real implementations and the
selected producer results; acceptance retains the original task proof and global
completion rule. P15-S23 additionally requires the three full-language bootstrap
outcomes; P18-T03/T05/T06 acceptance and the final Phase18 gate require genuine
P15 retirement. The final Phase18 gate retains all seven task proofs.

SH29 consumes only its declared first seedless closure and applicable Phase18
command/trust results. It does not wait for broader language completion. The
full-language final evidence task instead retains all other full-language
acceptances, the Phase18 final gate and the SH29 boundary proof. This combines
the project's required outcomes without moving the SH29 stop or accepting the
broader language from the first subset release. Raw public-binary/runner/rebuild
products can exist before those final proof decisions, avoiding final-checkbox
start cycles while retaining every actual required producer.

Platform Product results are conditional on its material adoption boundary,
currently unknown; the conservative edge remains visible and grants no eligibility.
All source conjunctions, source/task entailment, implementation/support evidence,
independent review and integration remain unassessed. Declaration completeness
does not establish any acceptance, source-document completion or seed retirement.

## SH acceptance to Spec Kit task projection

The [SH-family crosswalk](../specs/sh-family-crosswalk/spec.md) extends the same
read-only dependency representation. Its original inventory verifies SH-00
through SH-29, six criteria each: 180 acceptance-planning units. The separate
SH07-C01 through SH07-C18 sequence expands the same six SH-07 criteria. Neither
population adds implementation completion units. Component tasks retain their
literal IDs and sole editable marked Spec Kit ledgers.

The project-wide readiness successor binds task-file versions through
[its explicit source-version record](../specs/sh-family-crosswalk/implementation-readiness-source-version.edn).
At the Platform checkpoint it conserves 121 of the original 139 task bodies,
declares eighteen changed Open bodies, and registers the resumed ten-task
Governance authority. Governance is not another compiler pass or SH milestone.
The sixteen Domain, thirteen Release and eleven Platform Product future records
remain outside the 149-key current SH task projection. The future-task view
below captures those forty records separately; it does not change SH incidence.
Historical
ownership bytes retain their original pin; live drift is reported as unassessed.
This successor retains the original SH/SH07 routes and scoped Reader receipts,
but its new input fingerprint requires its own independent source/routing
review. Generation or passing structural controls cannot inherit an old review
or establish current whole-criterion execution.

`tools/sh_inventory.clj --write` extracts the original byte/line/hash-bound
criteria; `tools/sh_crosswalk.clj --write` derives routing and current task
records. `--check` refuses a stale projection. The acceptance checker consumes
that crosswalk and adds `:spec-kit-task-self-hosting` to its existing report.
The original report keys, FL36/SH30 edges and read-only CLI remain compatible.
The report is the canonical dependency projection for SH and the dashboard;
neither consumer edits a copied graph or task status. Typed edges preserve the
consumer-to-prerequisite direction, source identity, evidence facet and explicit
condition. Unknown conditions retain their conservative edge and cannot grant
eligibility. An explicitly false condition disables only its specific edge.

SH03-SH29 retain the plan's complete shared completion rule as an exact hashed
source span and explicit acceptance conjunction: actual owning execution,
reject/preservation/composition evidence, target-tool checks where claimed,
applicable BOOT/TEST13 provenance/support/TCB and independent stable-revision
acceptance. These are evidence facets, not additional tasks. SH00-SH02 retain
the original baseline/process exception. Reuse of unchanged evidence remains
allowed; this view introduces no unconditional development replay.

Selected acceptance facets refer to existing task IDs without requiring their
entire wider language or target scope to finish. A current SH facet remains
unassessed without exact applicable execution and independent acceptance. The
historical accepted SH-00 through SH-06 receipts stay retained at their original
commits and scopes. They are not silently relabeled as current closure, and an
open broad component task does not erase that historical completion. The actual
Reader fresh-Syntax task acceptance remains visible in its literal task record;
it does not close the full Reader or SH-03 population.

### Conditional and conflicting source boundaries

- Lower's actual MIR prerequisite survives. Opt is required only when the
  selected invocation requests optimization. The SH-16 to SH-17 edge remains
  a milestone completion dependency; it is not a universal development gate.
- Auth artifact identity work can begin against its explicit source/policy
  contract. Capability-grant composition additionally needs type/effect/profile
  facts. No effect record grants authority and no runtime repairs rejected code.
- D1's pipeline lists Core Gravity AST before Name Resolution. C1's stage data
  lists `resolve-names` before `lower-to-core`; C5 resolves syntax after expansion
  and before type/effect checks; C6 explicitly consumes expanded, resolved syntax
  and C5 binding artifacts. The operational C6 composition edge therefore runs
  Resolve to semantic Core lowering. D1's earlier Core representation boundary
  is retained as a declared interface, not inverted into a completed-Core
  prerequisite for Resolve. Whether both documents intend the identical AST
  construction event remains an explicit contract-ordering qualification for
  the architecture owner; this projection neither rewrites D1 nor asserts an
  invented fused pass. The exact four source files remain pinned in the crosswalk.
- SH07-C01 derives its population now. It has no SH-23/24/25 build-completion
  prerequisite. C12's C11 edge applies before final candidate selection; C16
  applicability runs alongside development; C17 consumes the actual C12-C16
  evidence; C18 requires C01-C17 for the claimed scope. Existing C12 platform
  restrictions remain with their current owner. Advisory eligibility grants no
  execution permission and never transfers a restricted operation.
- The selected native release remains Linux LLVM, independently verified and
  executed. Darwin harness support does not substitute for that release target.
  SH-26 invokes the actual prior compiler on the complete next-stage closure;
  SH-27 compares real clean rebuilds; SH-28 closes trust/TCB/provenance; SH-29
  delivers and verifies the public seedless binary. Record validators and boolean
  flags cannot satisfy those execution gates. Wider hardware/firmware/OS/device
  and deployment obligations retain their separate existing component outcomes.

### Frontier and validation limits

The frontier distinguishes declared-interface development, actual selected
producer/consumer composition and current acceptance. SH07-C01's source-bound
population reconciliation has no implementation prerequisite and is eligible
for that declared work. A task's explicit interface permits local development;
it is not evidence that its implementation executes. Current SH criteria and
SH-29 completion remain unassessed or blocked by unaccepted prerequisites.
Missing selected closure, condition values, source evidence and owner decisions
remain explicit; a zero-cycle result cannot resolve them.

The controls conserve every SH completion edge, all 18 sequence identities and
literal marked task IDs; reject unknown/duplicate/missing nodes, stale byte
spans, forged task checkmarks and malformed ranges; and demonstrate that an
unknown optional edge cannot produce eligibility. Full source/routing review
passed at `c6c80530`, with its receipt and input fingerprint in the crosswalk.
Current implementation and criterion acceptance remain unassessed. Changed
source/routes cannot inherit that review; regeneration checks stamped bytes
against their actual local Git commit before writing. The dashboard collector may
consume this projection only after accepted integration. It remains the sole
writer of its store and Site.

Unassessed source-inspection locators retain the exact inspected Git commit and
hash. The report's `:evidence-source-applicability` separately records whether
the live file matches, changed or is absent; none of those observations grants
current execution acceptance. Ordinary later compiler/test edits therefore do
not erase a valid historical inspection or masquerade as its original bytes.
Current task authorities, acceptance contracts and claims marked current remain
strict. Regeneration still verifies every stamped snapshot against its actual
Git object, including all explicit applying pins, before either output write.
