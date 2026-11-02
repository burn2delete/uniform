# Tasks: Legality implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- legality-implementation-tasks:start -->

- [ ] `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` Complete profile restrictions and generated-form checks

Status: Open

Evidence baseline: [Profile contract](../../bootstrap/clojure/src/gravity/profile_validation/contract.clj)

Closing evidence: Allowed profile matrix and hidden-allocation/reflection/effect rejection before lowering

<!-- legality-foundation-profile-contract:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/profile-contract-declaration: Publish each claimed profile's permitted, rejected, checked and delegated behavior with backend/runtime/provider dependencies and required evidence; enforce the profile contract after expansion and before affected lowering. Refusals: Reject implicit hosted services, hidden effects or allocation, unavailable checks and a claimed profile relying solely on target/runtime refusal; effects and capability grants remain separately typed contracts. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.
<!-- legality-foundation-profile-contract:end -->


<!-- legality-canonical-safety-root-profile:start -->

Apply the [root safety and unsafe-audit source criteria](plan.md#root-safety-and-unsafe-audit-source-criteria) within this existing task. Status remains Open. Restored finite criteria retain their frozen review scope; current composition and whole-source independent review remain pending.

Allocation interface/safety/safe1/profile-legality (Open). Role: legality compile-time safety/conformance checker. Input: expanded typed/effected source with profile, safety mode and dependency API policy. Required positive predicate: Evaluate safe-code claims against each active profile's available proofs, checks, providers and runtime machinery; reject unavailable machinery. Positive output witness: Per-profile available proof/check/provider/runtime decision and unavailable-machinery rejection. Concrete negative case: A constrained profile requests a runtime safety check or provider it cannot supply. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

SAFE1 profile-safety criterion (Open): Evaluate each safe-code claim after expansion under actual profile/target/provider and safety-mode facts, before affected lowering. Preserve the declared proof/check/provider/runtime support and profile-safety capability report. Core permits only portable safe semantics and rejects host-dependent checks; Hosted may use declared managed checks/exceptions/memory; Native combines ownership/lifetimes/regions/checks/atomics/provider allocation; Kernel/Firmware require explicit allocation, no hidden managed runtime and bounded declared failure; Hardware requires circuit/register/width/timing/reset facts; Distributed requires replay/idempotency/retry/workflow/external authority; AI requires model/tool authority, taint/prompt/provenance/nondeterminism policy. These are selected governing contracts, not profile-name allowlists. The same operation may be accepted in one profile and rejected or narrowed in another. Reject unavailable machinery, implicit authority/allocation and deferred backend-only legality; keep real physical, representability and runtime constraints distinct from removed compiler accounting.
<!-- legality-canonical-safety-root-profile:end -->


<!-- legality-canonical-memory-profile:start -->

Apply the [complete memory-safety source criteria](plan.md#complete-memory-safety-source-criteria) within this existing task. Status remains Open; finite review scope is frozen and current whole-source/composition review remains pending.

SAFE2-SAFE5 memory availability criterion (Open): Publish actual profile/target/provider support for managed heap, reference counting, ownership/borrows, regions, arenas, stack/static storage, linear resources, foreign heap, device memory and MMIO. Consume early provider/runtime/backend descriptors and typed/effect facts before operation grants and affected lowering; do not infer support from a family name or hidden managed fallback. Validate profile prohibitions on hidden/panicking allocation and required metadata, synchronization, failure, blocking, cleanup and lifetime mechanisms. Memory analysis consumes this decision for its original regime/provider predicates. Preserve concrete target object-size/alignment/storage-capacity/representability and declared deterministic timing/failure conditions. SAFE2/SAFE4 resource-budget wording never reinstates compiler work/input allowances: only an identified physical/provider/profile condition may justify a retained bound or no-failure proof. Reject unsupported machinery and unspecified physical/failure assumptions rather than inventing a project quota.
<!-- legality-canonical-memory-profile:end -->


<!-- legality-canonical-memory-model-profile:start -->

Apply the [complete source memory-model and provider criteria](plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10/L18 source memory profile criterion (Open): Check the actual regime/provider/type/effect/grant/source configuration before affected lowering. Portable Core uses abstract immutable values and portable facts without a particular runtime allocator. Hosted GC/RC/host buffers and FFI require declared managed boundaries; native ownership/borrows/regions/arenas/stack/static/foreign/pinned/device services require selected providers. Firmware/kernel require explicit allocation and concrete permitted storage/failure conditions with no ambient allocator or hidden hosted fallback; hardware uses register/wire/state/memory/buffer maps rather than heap assumptions. GPU retains host/device/shared/local/constant/global address-space and synchronization facts; AI/distributed persistent/vector/workflow/replay storage keeps schema and authority policy. Any constrained managed-memory exception needs its explicit governing profile boundary and checks, never inheritance from hosted behavior. Reject unavailable families, hidden GC/runtime dependencies, unsupported required metadata/failure mechanisms and widened provider policy. Preserve required target layout, physical capacity, alignment and timing facts without reinstating compiler work allowances.
<!-- legality-canonical-memory-model-profile:end -->


- [ ] `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis` Complete ownership, borrow, initialization and lifetime analysis

Status: Open

Evidence baseline: [Ownership contract](../../bootstrap/clojure/src/gravity/c9_ownership_checker/contract.clj)

Closing evidence: Real use-after-move, escape, invalid borrow and linear-resource fixtures with graphs
<!-- legality-canonical-ownership-analysis:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 ownership/lifetime/region/resource analysis criterion (Open): Derive ownership, borrowing, lifetime, region/arena generation, move/consume, escape and linear-resource facts from actual typed and effected Core, preserving persistent immutable sharing separately from explicit owned mutable memory. Enforce many immutable borrows or one exclusive mutable borrow for every proven alias range and lifetime; field/range splitting requires actual non-overlap proof, otherwise treat ranges as potentially aliasing. Preserve owner, borrow, reference, field, range and provider-scope graph nodes and immutable/mutable/projection/slice/move/consume/transfer/interior-mutability edges. Moving or consuming an owner during an active borrow, conflicting aliases and later use of moved/consumed storage reject; only actual persistent/copyable semantics or a returned valid owner/borrow permits reuse, never an implicit repair copy. Infer lexical, closure, stack, owned-heap, region, arena-generation, foreign-call/callback, provider, structured-task, device-buffer and generated-artifact intervals with start/end/owner/allowed-escape/invalidation facts. Reject escape through return, global/static storage, longer-lived closure, detached task, actor/workflow state, FFI/callback retention, tool/model request, generated artifact or region/arena storage when the destination outlives validity. Legal copy, serialization, explicit ownership transfer/promotion or a permitted audited unsafe boundary must establish the actual destination lifetime. Distinguish region lifetime, arena identity/generation, allocation/reset/release and provider contracts; inner regions may borrow outer values but cannot escape into outer storage, and reset invalidates prior-generation values. No hidden GC fallback is permitted. Track acquire/borrow/transfer/close/commit/rollback/unlock/cancel/release/poison/forget across every modeled normal/error/panic/cancellation/early-return path, plus provider-required timeout/task-failure/shutdown paths. Every path reaches exactly one contract-valid terminal state or transfers to a destination with cleanup obligations; reject leaks, duplicate terminal operations, wrong-provider release, reuse and transfer followed by local release. Emit actual ownership/borrow graphs, lifetime interval map, move/consume records, escape report, region/generation graphs, linear flow and transfer/check candidates with source/generated identities and unsafe audit references for lifetime extension, alias recovery or unproved manual flow. Preserve existing ownership-move, borrow-exclusivity, lifetime-escape, unsafe-lifetime and terminal-flow owner allocations. This task owns semantic state-transition analysis; downstream tasks consume its products.
<!-- legality-canonical-ownership-analysis:end -->



<!-- legality-canonical-memory-analysis:start -->

Apply the [complete memory-safety source criteria](plan.md#complete-memory-safety-source-criteria) within this existing task. Status remains Open; finite review scope is frozen and current whole-source/composition review remains pending.

Historical finite allocation context (all following criteria remain Open):

SAFE2 context: Role: legality compile-time safety/conformance checker. Input: typed memory operation with storage/range/initialization/lifetime/alias/provider facts.

SAFE3 context: Role: legality compile-time safety/conformance checker. Input: owned/borrowed arguments, call contracts, lifetimes and escape destinations.

SAFE4 context: Role: legality compile-time safety/conformance checker. Input: region/arena allocation/reset graph with provider, generation and task/foreign lifetimes.

SAFE5 context: Role: legality compile-time safety/conformance checker. Input: resource acquisition/borrow/transfer and all modeled normal/exceptional/cancellation paths.

Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite.

Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance.

Whole shared-facet entailment and implementation acceptance are not conferred.

Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions.

Each allocation below conjoins its document context with its positive/output/negative fields and all shared refusal/evidence/dependency/qualification fields. The source trace reconstructs the complete historical byte predicate; current whole-source composition remains pending.

Allocation interface/safety/safe2/access-validity (Open).
Required positive predicate: Classify memory operations and accept safe access only when storage, initialization, bounds, lifetime, aliasing, synchronization, allocator, profile and capability facts hold; otherwise check, reject or audit explicitly.
Positive output witness: Operation validity facts for storage, initialization, bounds, lifetime, alias, synchronization, allocator, profile and authority.
Concrete negative case: A load has valid bounds but expired storage, an uninitialized range, wrong allocator or incompatible ownership/synchronization.

Allocation interface/safety/safe2/aliasing (Open).
Required positive predicate: Permit immutable sharing, exclusive mutation, synchronized sharing and declared provider interior mutation only under their valid alias/access contracts.
Positive output witness: Alias ranges, access modes and declared synchronization/interior-mutation contract.
Concrete negative case: A mutable access overlaps a live immutable borrow or an unsynchronized shared alias.

Allocation interface/safety/safe2/allocation-failure (Open).
Required positive predicate: Require allocation sites to declare/inherit Result, error, panic, bounded no-failure proof or explicit unsafe failure behavior; enforce profile prohibitions on hidden/panicking allocation and semantic resource bounds.
Positive output witness: Allocation site/provider/failure and profile-legal fallback or refusal records; no project work allowance.
Concrete negative case: An allocation can fail but has no Result/error/panic/proof/unsafe failure contract, or allocates in a no-allocation profile.

Allocation interface/safety/safe2/bounds-layout (Open).
Required positive predicate: Check access ranges across collection, foreign, device and MMIO layouts; erase bounds checks only with valid range, length, schema or layout proof.
Positive output witness: Checked range/layout/alignment/ABI facts and the exact proof for any erased check.
Concrete negative case: A slice or MMIO/foreign/device access is outside its valid byte range or violates layout/alignment; a range proof refers to another length.

Allocation interface/safety/safe2/checks (Open).
Required positive predicate: Emit memory check records with profile-legal failure and declared allocation/blocking/host effects for dynamic bounds, null, initialization, lifetime, region, borrow, release, allocator and device checks.
Positive output witness: Emitted memory checks, ordering before access, declared failure/effects and source/provider context.
Concrete negative case: A dynamic lifetime/allocator/device check is recorded but omitted before access, or its failure blocks/allocates without declared effects.

Allocation interface/safety/safe2/initialization (Open).
Required positive predicate: Prevent reads of uninitialized ranges across partial, moved, conditional, foreign and device states; drop only initialized fields.
Positive output witness: Per-range definite-initialization facts and a drop map containing only initialized fields, including foreign/device states.
Concrete negative case: A branch leaves an aggregate field uninitialized or moved out, but a later read or cleanup treats it as initialized.

Allocation interface/safety/safe2/lifetime-escape (Open).
Required positive predicate: Reject storage/provider-scope escape and dangling references unless a profile-supported checked handle or explicit unsafe boundary supplies the missing lifetime guarantee.
Positive output witness: Lifetime/escape paths and static proof, supported handle check, explicit audit or rejection.
Concrete negative case: A stack, callback or provider-scoped reference escapes after its storage expires without a supported checked handle.

Allocation interface/safety/safe2/preservation (Open).
Required positive predicate: Preserve memory facts across transformations and target lowering, invalidating/recomputing affected facts; check elimination requires proof artifacts.
Positive output witness: Preserved or invalidated/recomputed memory facts and backend/check-elision evidence.
Concrete negative case: Inlining, memory reordering or target lowering keeps a stale lifetime/alignment proof or removes a volatile/atomic memory fact.

Allocation interface/safety/safe2/raw-boundaries (Open).
Required positive predicate: Confine raw pointers, unchecked casts/access, release, foreign pointers, device/MMIO and uninitialized-byte reinterpretation to audited unsafe islands or safe internal APIs.
Positive output witness: Raw operation audit and wrapper/precondition evidence, or safe-code rejection.
Concrete negative case: Safe code dereferences a raw foreign pointer or reinterprets uninitialized padding without an audited boundary.

Allocation interface/safety/safe2/regime-legality (Open).
Required positive predicate: Expose selected memory regime and provider in semantic records and reject unavailable memory families before backend lowering.
Positive output witness: Selected memory regime/provider and compile-time profile admission or refusal.
Concrete negative case: A firmware artifact implicitly falls back to a managed heap or uses a memory provider unavailable to the active profile.

Allocation interface/safety/safe3/call-contracts (Open).
Required positive predicate: Record copy, borrow, move, mutate and consume behavior in function/generic contracts and reject hidden polymorphic moves.
Positive output witness: Function/generic copy, borrow, move, mutate and consume facts bound to the actual argument identities.
Concrete negative case: A generic call silently moves or consumes an argument while its function contract advertises only borrowing/copying.

Allocation interface/safety/safe3/concurrent-transfer (Open).
Required positive predicate: Make task, actor and workflow ownership transfer explicit; permit sharing or borrowing only with valid synchronization and structured lifetime proof; reject detached local borrow capture.
Positive output witness: Task/actor/workflow transfer, parent-consumption and structured lifetime/synchronization proof.
Concrete negative case: A detached task captures a local borrow or the parent uses an owned value after moving it to an actor.

Allocation interface/safety/safe3/foreign-transfer (Open).
Required positive predicate: Require foreign and callback ownership declarations and allocator/release facts sufficient to prevent dangling pointers, double release and use after callback; missing declarations make boundary unsafe.
Positive output witness: Foreign/callback ownership, lifetime, allocator and release declarations and safe/unsafe boundary verdict.
Concrete negative case: A foreign callback retains a borrow after return or an owned foreign value has no allocator/release declaration.

Allocation interface/safety/safe3/lifetime-escape (Open).
Required positive predicate: Expose inferred lifetimes and escape facts, accepting a borrowed escape only if destination cannot outlive source; otherwise transfer, copy, serialize or use explicit unsafe boundary.
Positive output witness: Inferred lifetime graph, exact escape path and destination relation or explicit transfer/copy/serialization/audit.
Concrete negative case: A closure, returned value, global or generated artifact retains a borrow beyond its owner/provider lifetime.

Allocation interface/safety/safe3/runtime-borrows (Open).
Required positive predicate: Use dynamic borrow checks only with profile-permitted value/provider state, artifact records and declared failure; otherwise require static proof or reject.
Positive output witness: Profile-permitted borrow-state/provider metadata, defined failure and emitted check, otherwise static proof or refusal.
Concrete negative case: A profile forbids dynamic borrow metadata, but the implementation silently uses a runtime borrow-state check.

Allocation interface/safety/safe3/unsafe-recovery (Open).
Required positive predicate: Isolate manual lifetime extension, alias recovery and ownership recovery behind audited unsafe boundaries.
Positive output witness: Lifetime/alias/ownership recovery audit with invariant and permitted boundary, or rejection.
Concrete negative case: A safe function extends a borrow lifetime or recovers a mutable alias without an unsafe island.

Allocation interface/safety/safe4/arena-invalidation (Open).
Required positive predicate: On reset create a new arena generation and invalidate earlier values; prove absence of later use, emit supported generation checks or reject, while distinguishing region lifetime from arena identity/generation.
Positive output witness: Arena identity/generation/reset edges, invalidation and proof or supported generation checks.
Concrete negative case: An arena value from generation N is used after reset to N+1, or a region lifetime is mistaken for arena generation.

Allocation interface/safety/safe4/cleanup (Open).
Required positive predicate: Enforce declared region cleanup policy, at-most-once supported destructors, initialized-value/linear-resource cleanup, deterministic release where required and declared cleanup failure without access to invalidated values.
Positive output witness: Declared deterministic cleanup policy, initialized-value/linear-resource release and defined cleanup-failure behavior.
Concrete negative case: Reset drops an uninitialized field, invokes a destructor twice or lets cleanup access an already invalidated sibling value.

Allocation interface/safety/safe4/concurrent-reset (Open).
Required positive predicate: Allow cross-task region/arena use only through lifetime-valid transfer, structured completion, copy/serialization or synchronized thread-safe provider; reject reset with outstanding access absent synchronization proof.
Positive output witness: Region/task lifetime and provider synchronization facts controlling reset.
Concrete negative case: One task resets an arena while another retains outstanding access without a synchronizing no-access proof.

Allocation interface/safety/safe4/dynamic-checks (Open).
Required positive predicate: Emit dynamic generation, escape, borrow and reset checks only when the profile permits their metadata/failure behavior; reject unsupported metadata requirements.
Positive output witness: Generation/escape/borrow/reset check records with profile legality or rejection.
Concrete negative case: A region escape or reset-after-borrow check requires metadata/failure machinery forbidden by the profile.

Allocation interface/safety/safe4/foreign-borrows (Open).
Required positive predicate: Bound foreign borrows/callbacks to the region, require explicit copy/transfer for retention and prevent foreign reset/free except provider-declared operations; missing declarations make boundary unsafe.
Positive output witness: Call/callback borrow lifetime, explicit retention copy/transfer and provider reset/free authority.
Concrete negative case: Foreign code retains a scoped pointer or resets/frees an arena through an operation not exposed by its provider contract.

Allocation interface/safety/safe4/provider-validation (Open).
Required positive predicate: Validate region/arena provider profiles, targets, alignment, size, failure, reset, affinity, blocking, hidden allocation, cleanup and generation checks, retaining constrained-profile semantic resource bounds.
Positive output witness: Provider profile/target/alignment/physical-size/failure/reset/threading/blocking/allocation/cleanup declaration validation.
Concrete negative case: A region provider omits reset/failure/alignment/thread-affinity/cleanup behavior or hides allocation.

Allocation interface/safety/safe4/scoped-escape (Open).
Required positive predicate: Prevent region references from escaping to longer-lived return, global, closure, task, generated or foreign storage; allow explicit copy/serialization/transfer or proved nonretaining wrappers.
Positive output witness: Region lifetime/escape graph and valid copy/serialization/transfer or nonretaining-wrapper proof.
Concrete negative case: An inner-region reference is returned, stored globally or embedded in a longer-lived generated/foreign structure.

Allocation interface/safety/safe4/unsafe-regions (Open).
Required positive predicate: Confine unchecked region/arena APIs to audited unsafe islands or safe wrappers without hidden GC fallback.
Positive output witness: Region unsafe audit or checked wrapper and explicit no-hidden-fallback decision.
Concrete negative case: An unchecked arena API is exposed as safe or failed region analysis silently invokes a hidden GC fallback.

Allocation interface/safety/safe5/borrowing (Open).
Required positive predicate: Permit scoped linear-resource borrows only within owner lifetime and forbid closing, transferring or storing without function-contract authority.
Positive output witness: Scoped resource borrow and function-contract permissions with owner lifetime.
Concrete negative case: A borrowed resource is closed, transferred or stored beyond its owner despite a nonconsuming function contract.

Allocation interface/safety/safe5/generated-flow (Open).
Required positive predicate: Prevent macro/facet duplication of linear values and retain origin and linear flow for generated acquisition, close and structured cleanup.
Positive output witness: Generated linear-flow graph, acquisition/terminal records and generator/source origins.
Concrete negative case: A macro duplicates a linear resource or generated cleanup loses its source origin and exactly-once flow.

Allocation interface/safety/safe5/manual-audit (Open).
Required positive predicate: Require unsafe audit records for manual acquire/release handling when exact consumption cannot be proven; best-effort/finalizer/process-shutdown assumptions do not prove required deterministic cleanup.
Positive output witness: Exact-consumption proof or explicit manual-resource unsafe audit; deterministic cleanup requirements remain.
Concrete negative case: A manual acquire/release path is justified only by a best-effort finalizer or process shutdown.

Allocation interface/safety/safe5/provider-contract (Open).
Required positive predicate: Consume resource-provider declarations for acquire, exclusive terminal states, cleanup effects/failures, affinity, transfer, cancellation and poisoning; enforce declared recovery APIs for poisoned state.
Positive output witness: Acquire/terminal/poisoning/cleanup-effect/affinity/transfer/cancellation provider facts.
Concrete negative case: A provider omits exclusive terminal states or terminal failure/cancellation behavior, or poisoned state is reused without declared recovery.

Allocation interface/safety/safe5/structured-cleanup (Open).
Required positive predicate: Provide structured acquire/body/cleanup forms that preserve the resource's cleanup contract across normal return, errors, panic, cancellation and early exit.
Positive output witness: Acquire/body/cleanup lowering and terminal operations for normal/error/panic/cancellation/early-exit paths.
Concrete negative case: A with-open/lock/transaction lowering releases only on normal return and leaks on cancellation or early exit.

Allocation interface/safety/safe5/transfer (Open).
Required positive predicate: Consume the source on transfer, record destination cleanup ownership and enforce provider/profile/concurrency rules across tasks, actors, processes and error objects.
Positive output witness: Consumed source owner, destination owner/cleanup contract and provider/profile/concurrency transfer legality.
Concrete negative case: A sender closes a transferred resource or the receiving error/task type has no cleanup ownership.

SAFE2 access, initialization, layout and allocation criterion (Open): Derive real source-bound validity for allocation, initialization, move, borrow, load, store, slicing, indexing, pointer conversion, pinning, region enter/exit, arena reset, resource release, foreign allocation, device transfer, MMIO, atomic access and deallocation. Every access needs existing storage, initialized accessed range, in-bounds range, valid lifetime, permitted ownership/alias mode, required synchronization, correct allocator/release rules, legal profile regime and applicable raw/device/MMIO/FFI capability facts. Track partial and conditional initialization, moved-out fields, foreign initialization and host/device visibility; reads require range-specific proof/check and partial destruction releases only initialized fields. Bind array/vector/buffer/slice/string/typed-view/foreign/device/MMIO ranges to element size, alignment, length/capacity, valid byte range, offset, padding, relevant endianness and ABI. Static range/length/schema/layout facts can justify only their exact check conditions. Track stack/region/arena/borrow/foreign/device/pinned/callback/provider lifetimes and actual escape destinations. Alias contracts distinguish immutable sharing, exclusive mutation, synchronized/atomic sharing, declared interior mutation and ordered volatile access. Allocation sites declare or inherit Result, recoverable error, allowed panic, concrete no-failure proof or explicit unsafe failure, retaining provider identity and profile prohibitions. Emit actual typed-Core/MIR memory facts, allocation/release/range/layout/init/lifetime/escape maps and precise check/proof/audit requirements; bounds alone cannot prove a safe access. This task owns specialized facts and rule refusals plus its required consumer evidence, not an alternative whole-program safety classifier or Runtime implementation.

SAFE3 ownership, call and transfer criterion (Open): Preserve persistent immutable, owned mutable, immutable/mutable borrowed, linear, region, arena, foreign and provider-scoped distinctions. Reuse existing C9 state/borrow/range/lifetime derivation, requiring many immutable aliases or exclusive mutable access over each actual range and lifetime. Persistent structure sharing differs from transient/mutable borrowing; do not copy owned mutable values to repair move errors. Immutable borrows forbid concurrent mutable borrow and owner move/consume; mutable borrows forbid other access except a checked interior-mutability contract. Nested borrows are narrower/nonconflicting or statically ordered, and field/range splitting needs nonalias proof. Expose inferred or required explicit lifetime relations and all return/closure/task/actor/global/region/arena/foreign/tool/model/generated escapes. Generic/function contracts bind actual copy/borrow/move/mutate/consume behavior to arguments, rejecting hidden polymorphic moves. Task/actor/workflow transfer consumes the source; immutable copy/sharing, synchronized shared mutation and statically bounded structured borrowing remain distinct. Detached local borrow capture requires a valid outliving proof. Foreign declarations preserve borrowed-in/out, owned-in/out, retained, released-by and callback-borrow, with actual allocator/lifetime/release facts; missing declarations require an explicit unsafe boundary, never implicit FFI trust. Emit original ownership/borrow/lifetime/move/consume/escape/transfer facts and manual lifetime/alias/recovery audit requirements, retaining all original owner allocations.

SAFE4 region, arena, provider and cleanup criterion (Open): Preserve scoped region lifetime separately from arena identity/generation, allocation site and reset/release point. Region values may flow only to the same/shorter lifetime; arena reset starts a new generation and invalidates earlier values. Inner regions may borrow outer values, never the reverse retention, and nested invalidation runs innermost first. Reject return/global/longer closure/detached task/inner-to-outer/post-reset/foreign/generated escapes unless actual owned copy, serialization, independent persistent value, explicit longer-lived transfer/copy API or nonretaining safe-wrapper proof establishes legality. Providers declare profiles/targets/allocation effects/alignment, concrete bounded object capacity where applicable, failure/reset/release/thread affinity/blocking/hidden allocation/cleanup/generation-check availability and conformance. Keep concrete constrained storage/allocation conditions distinct from removed compiler accounting. Cleanup policy is explicitly none, initialized-value drop, registered reset callbacks, linear release before reset or rejection of resource-owning allocation. Supported destructors/callbacks run at most once, obey required deterministic release and declared failure, and cannot access invalidated region values except through the value being cleaned. Cross-task use requires lifetime-valid ownership, structured completion, copy/serialization or synchronized thread-safe provider; reset with outstanding access needs a no-access synchronization proof. Foreign borrow lasts only its declared call/callback interval; retention requires copy/transfer and reset/free requires an exposed provider operation. Emit real region/generation/allocation/escape/reset/cleanup/provider/grant facts and requirements for dynamic generation/escape/borrow/reset-after-borrow checks or audited unsafe wrappers. No hidden GC fallback is permitted.

SAFE5 linear flow, structured cleanup and provider criterion (Open): Track actual unacquired/owned/borrowed/transferred/closed/committed/rolled-back/cancelled/released/poisoned state and provider-specific exclusive terminal operations. An acquired linear resource reaches exactly one valid terminal state or destination cleanup ownership on every normal/error/panic/cancellation/early-return path, plus timeout/task-failure/shutdown paths when modeled by its contract. Files, sockets, locks, transactions, channels, GPU/device/foreign handles, workflow leases and one-shot tool sessions retain their declared linear discipline unless a provider proves a permitted weaker discipline. Structured acquire/body/cleanup lowering for open/lock/transaction and generated forms preserves every required exit path and source origin. Reject missing/branch-only/duplicate release, use after terminal state, wrong provider, post-transfer local release, duplicated owners or escaping captures. Error-object transfer requires an error type that owns cleanup; cross-thread/process transfer obeys actual profile/provider/concurrency contracts. Borrowed inspection/mutation/provider/foreign handles cannot close, transfer or retain beyond their declared function permission or owner lifetime. Resource providers declare acquire/terminal exclusivity/failure/blocking/allocation/affinity/transfer/cancellation/poisoning/audit/conformance; poisoned reuse requires its recovery API. Declared reliable hosted finally/finalizer mechanisms are conditional, and finalizers alone cannot establish deterministic cleanup or hide leaks behind process shutdown. Emit real linear/acquire/terminal/transfer/exceptional/cancellation/structured/generated flow and exact-consumption proof or manual unsafe-audit requirements.
<!-- legality-canonical-memory-analysis:end -->


<!-- legality-canonical-memory-model-memory-analysis:start -->

Apply the [complete source memory-model and provider criteria](plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

Decision criterion interface/language/memory/check-linear-resource-flow: Check exactly-once resource consumption or transfer and scoped acquisition/release; a privileged explicit forget path retains its declared policy boundary. Reject: Reject leaks, double consumption/release, invalid transfer and undeclared forget; preserve cleanup on every relevant normal/error/cancel path. Required outputs: Resource-flow and scope-cleanup graph, transfer/forget-policy evidence and leak/double-release refusal fixtures.

Decision criterion interface/language/memory/check-owner-and-borrow-lifetimes: Check ownership moves, shared versus mutable borrows and region lifetimes; permit escape only by valid longer-lived transfer or explicit unsafe responsibility. Reject: Reject stale/moved owners, conflicting mutable borrows, invalid region captures and unaudited unsafe escape. Required outputs: Ownership/borrow/region graph, legal-transfer and invalid-use/escape fixtures, source-bound safety diagnostics.

L10/L18 source memory validity criterion (Open): Derive actual initialized/uninitialized/partially initialized state, ownership/move/borrow access modes and lifetimes, region/arena/allocator identity, range/alignment/layout, sharing/race/synchronization and linear resource facts from typed/effected control and provider contracts. Constructors must establish full initialization before a value is treated as T, and safe reads require their accessed range initialized; partial destruction releases only initialized fields. Region escape is a compile-time error unless a valid longer-lived copy/move or explicit permitted unsafe responsibility establishes the source boundary. Require exactly-once consume/transfer, except a declared privileged explicit forget path whose authority/policy and unsafe obligations are actually checked; reject undeclared forget, leaks, double free/close and invalid transfer. Validate provider/checker-produced lifetime/alias/ownership/region/escape/init/bounds/device/MMIO facts under their actual source/operation/target/profile/grant and declared enforcement mechanism before using them. Replacement analysis cannot make a supplied fact or success flag authoritative. Emit real state/path/graph, initialization/range, linear/forget, allocation/effect and diagnostic products, preserving original producer refusals and actual downstream evidence requirements.
<!-- legality-canonical-memory-model-memory-analysis:end -->


- [ ] `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` Complete safe-code classification and taint/unsafe boundaries

Status: Open

Evidence baseline: [Safety contract](../../bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj)

Closing evidence: Every dangerous operation classified and unclassified operations rejected

<!-- legality-foundation-safe-api:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/safety-family-conformance: Exercise each claimed safety family on accepted safe programs, rejected illegal programs and explicit audited unsafe-island cases; retain the applicable proven-safe, runtime-checked, rejected or unsafe-island outcome, owning diagnostics and evidence. Refusals: Reject an unclassified dangerous operation, hidden unsafe path, stale proof/audit or missing actual runtime-check failure case; no fifth implicit outcome or undefined safe behavior is allowed. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/unsafe-island-safe-api-eligibility: Expose unsafe-backed functionality as a safe API only when the wrapper proves or checks every required precondition and cleanup obligation, with source-bound audit and actual boundary tests; preserve internal-only status as a separate permitted boundary. Refusals: Reject invalid state escaping into safe code, a missing wrapper test/audit, an unproved precondition or treating internal-only unsafe code as a safe exported API. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.
<!-- legality-foundation-safe-api:end -->
<!-- legality-canonical-ownership-checks:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 profile-legal runtime ownership check criterion (Open): Consume the actual ownership/lifetime/region/resource obligations and producer audit references; classify each affected dangerous operation under the existing four safety outcomes without treating a whole-fixture verdict as an operation proof. Dynamic borrow-state, region-generation, arena-generation, provider-scope and resource-terminal checks need explicit source-bound records, attached value/provider state, actual failure semantics and profile/provider support for metadata, allocation and failure. Emit a legal check before unsafe behavior or retain static proof, reject, or require an explicitly permitted audited unsafe island. Reject required dynamic checks when the profile forbids their metadata/allocation/failure behavior, and reject absent, stale or unbound check/audit evidence. Runtime/backend owners implement real checks and failures; a profile-name allowlist or assigned recorded/passed flag is not that evidence. Unsafe lifetime/alias/resource handling cannot silently make a safe exported wrapper; preserve the existing full safe-wrapper precondition, invariant, cleanup, audit and boundary-test obligations. Retain the original safety/safe1/runtime-checks owner allocation and actual C9-RUNTIME-CHECK/UNSAFE refusals; no new authority or fifth outcome is introduced.
<!-- legality-canonical-ownership-checks:end -->




<!-- legality-canonical-safety-inventory:start -->

Apply the [complete safety-pipeline source criteria](plan.md#complete-safety-pipeline-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C10 operation inventory criterion (Open): Derive the complete safety-sensitive operation inventory from actual typed/effected, profile/capability/ownership-checked Core before affected MIR construction. Include loads/stores/allocation/deallocation/pointer conversion; initialization and moved-field reads; indexing/slicing; overflow/division/shifting/casts/elementary approximations; borrow/move/consume/region/arena/resource operations; FFI and host boundaries; concurrency/atomics/locks/task captures/channels; capability use; taint sinks/sanitizers; generated unsafe or authority-bearing code; model/tool policy boundaries; and supply-chain/imported-certificate trust points. Bind each operation to its real Core node, typed/effect/ownership facts, capability context, profile/target and generated-origin chain. Consume specialized SAFE2-SAFE14 results at their owning interfaces rather than claiming all families from one synthetic inventory. Collect operations, attach facts, run specialized checks, produce outcomes/checks/obligations/audits, verify completeness and hand facts to MIR in C10 order. Preserve safety-operation inventory, outcome records, runtime-check list, proof-obligation list, proof/certificate references, unsafe manifest, taint/capability report, generated provenance and diagnostics. Reject missing operations/facts and duplicate, conflicting, unknown or absent classifications; equal sets of operation IDs do not prove exactly one outcome per operation. Every actual safety-sensitive operation must receive exactly one SAFE1 outcome, and a fixture-level accept/reject verdict cannot substitute for that operation result.
<!-- legality-canonical-safety-inventory:end -->


<!-- legality-canonical-safety-decisions:start -->

Apply the [complete safety-pipeline source criteria](plan.md#complete-safety-pipeline-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C10 safety decision and unsafe extraction criterion (Open): For each inventoried operation establish exactly one :proven-safe, :runtime-checked, :rejected or :unsafe-island outcome from actual specialized facts under the active profile, target, safety mode and package policy. Static proof must establish the claimed operation condition; otherwise emit a legal check before unsafe behavior, reject deterministically, or use an explicitly permitted audited unsafe boundary. Check records bind precise condition, source operation/span/origin, emitted location, profile/target, declared failure, introduced effects, performance class, evidence that the check guards that exact operation and invalidation conditions. Required unavailable checks or undefined failure reject. Emit policy-governed obligations for bounds, initialization, lifetime, aliasing, region escape, resource terminal state, race freedom, numeric safety, capability scope, taint sanitization, FFI ownership, macro safety and AI tool policy. Static analysis, solver, certificate, conformance fixture or unsafe audit discharges only the claim and scope permitted by policy, never by an assigned discharged flag. Unsafe records retain audit identity, owner, reason, operation, invariant, pre/postconditions, effects, capabilities, safe-wrapper boundary or explicit internal-only status, evidence, review/expiration/re-review policy, profile, target and source/generated origin. Reject forbidden modes/packages, missing metadata/review or invalid state escaping a safe wrapper; producer audit references alone confer no approval. Preserve taint facts to sinks, validate sanitizer contracts and capability scope, enforce foreign/numeric/tool/import trust conditions and diagnose generated unsafe or rejected forms at both the generated form and generator source. Sample IDs, labels, empty lists, fixture statuses and imported package names establish neither safety nor trust. Retain existing safety/safe1/outcomes, runtime-checks and safety-artifact plus compiler/c10/unsafe-islands and generated-safety-origins owners within this task.
<!-- legality-canonical-safety-decisions:end -->


<!-- legality-canonical-safety-root-classification:start -->

Apply the [root safety and unsafe-audit source criteria](plan.md#root-safety-and-unsafe-audit-source-criteria) within this existing task. Status remains Open. Restored finite criteria retain their frozen review scope; current composition and whole-source independent review remain pending.

Allocation interface/safety/safe1/mode-policy (Open). Role: legality compile-time safety/conformance checker. Input: expanded typed/effected source with profile, safety mode and dependency API policy. Required positive predicate: Enforce configured safety modes at namespace, package and build boundaries; weaker dependencies require accepted certificate or reviewed unsafe exported APIs, and experimental/audit-only claims cannot become stable safe-code claims. Positive output witness: Namespace/package/build mode decision and dependency API eligibility with certificate or wrapper evidence. Concrete negative case: A dependency compiled in experimental or audit-only mode is presented as a stable safe API without accepted certificates or reviewed exported wrappers. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe6/dependency-policy (Open). Role: legality compile-time safety/conformance checker. Input: source or generated unsafe operation, wrapper and package/profile review policy. Required positive predicate: Expose dependency unsafe counts/families/reviews/wrappers/profiles/authority/certificates and reject forbidden unsafe exports without accepted safe wrappers or certificates. Positive output witness: Dependency unsafe families/counts/reviews/wrappers/profiles/authority/certificates and caller-policy decision. Concrete negative case: A dependency exports unreviewed unsafe behavior into a package with forbidden unsafe policy and no accepted wrapper/certificate. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe6/generated-unsafe (Open). Role: legality compile-time safety/conformance checker. Input: source or generated unsafe operation, wrapper and package/profile review policy. Required positive predicate: Reject generated unsafe operations lacking island, authorizing source, generator/provenance, invariant/wrapper and effects/capabilities across macros, plugins, schemas, AI and build scripts. Positive output witness: Complete generated island, authorizing source, generator, wrapper, effect/capability and provenance records. Concrete negative case: A plugin, macro, schema, AI or build generator emits unsafe operations without authorizing source, invariant/wrapper or grants. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe6/isolation (Open). Role: legality compile-time safety/conformance checker. Input: source or generated unsafe operation, wrapper and package/profile review policy. Required positive predicate: Require explicit syntactic or generated-origin unsafe boundaries, classifying standard and profile-added unsafe operation families and attaching source/package/profile/target/dependency identity. Positive output witness: Unsafe operation classification, island identity and source/package/profile/target/dependency origin. Concrete negative case: A raw/authority/IR operation is hidden in generated code without an explicit unsafe-origin boundary. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe6/review-policy (Open). Role: legality compile-time safety/conformance checker. Input: source or generated unsafe operation, wrapper and package/profile review policy. Required positive predicate: Enforce forbidden/local/domain/certificate/audit-only policy, bind review identity/date/evidence/expiry to source version and island identity, invalidating affected reviews unless policy explicitly preserves them. Positive output witness: Policy-specific forbidden/local/domain/certificate/audit verdict, reviewer/date/source/island identity and invalidation. Concrete negative case: An expired or wrong-source island review is reused after its wrapper/layout changes, or audit-only is treated as safe acceptance. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

SAFE1/SAFE6 root-safety and unsafe-boundary criterion (Open): Preserve safe Gravity's no-undefined-behavior meaning across every specialized safety dimension, and classify each dangerous operation exactly once as :proven-safe, :runtime-checked, :rejected or :unsafe-island. Static facts must establish safety; actual emitted checks must guard the condition before violation with defined profile-legal failure, source/generated span, type/effect/capability context and hot-path performance classification. Consume the existing C10 inventory and exact check/proof/audit fields, rejecting missing facts and a fixture verdict used as operation proof. Enforce all seven modes at namespace/package/build boundaries: :safe and :safe-optimized forbid unsafe islands, and :safe-optimized permits check erasure only when proof survives; :audited-unsafe requires reviewed islands behind safe wrappers or explicit internal boundaries; :systems is profile-specific restricted audit, not unsafe by default; :trusted-runtime is an explicit internal trust boundary with conformance; :experimental cannot establish stable safe claims; :unsafe retains metadata without a safe-code guarantee. A weaker dependency needs accepted exported-API certificates or reviewed wrapper evidence, not its package name or audit-only label. Isolate all standard/profile-added unsafe families, including raw pointer arithmetic, unchecked load/store/cast/representation, manual allocation/release, unwrapped FFI and unchecked callbacks, MMIO/volatile, unchecked atomics/order, bounds suppression, type/capability escape, undeclared IR mutation, constrained reflection and model/tool authority bypass. Automatically attach actual namespace/package/source/profile/target/generator/dependency origins. Audit every island even when allowed, retaining island/operation/reason/owner/invariants/preconditions/postconditions/effects/capabilities/evidence/safe boundary/review state and required expiration/renewal plus external provider/dependency identity. A safe wrapper signature must prevent invariant violation, hide raw authority, enforce all preconditions, define failure and connect cleanup to ownership/linear rules; explicit internal-only status is distinct from a safe public API. Generated unsafe records bind the authorizing source, generator/macro/facet/schema/model/tool, invariant/wrapper and authority; diagnostics name both generated form and its source. Enforce forbidden/local-owner/domain-owner/certificate-required/audit-only review policies. For required or supplied reviews bind actual reviewer/date/source-version/island/evidence/expiry identity; affected source changes invalidate reviews unless policy explicitly preserves them. Audit-only does not gate the build and never proves safe eligibility. Preserve dependency counts/families/review states/exported wrappers/profiles/capabilities/certificates and caller-policy decisions; forbidden unsafe callers may consume only accepted safe wrappers/certificates, never hidden unsafe exports. Canonical syntax and artifact examples define required relationships, not default permission, accepted reviews or executed checks.
<!-- legality-canonical-safety-root-classification:end -->


<!-- legality-canonical-memory-classification:start -->

Apply the [complete memory-safety source criteria](plan.md#complete-memory-safety-source-criteria) within this existing task. Status remains Open; finite review scope is frozen and current whole-source/composition review remains pending.

SAFE2-SAFE5 check and unsafe-disposition criterion (Open): Consume actual memory/ownership/region/resource facts and rule obligations, original finite criteria and profile/provider availability. The existing C10 safety owner assigns the single final SAFE1 outcome; memory analysis remains the specialized producer and retains its required actual emission/preservation evidence. Dynamic bounds/null/initialization/lifetime-generation/region-escape/borrow-state/release-state/allocator/device-synchronization and region/arena/reset-after-borrow checks guard the exact operation before violation. Bind value/provider state, emitted record/location, source/generated identities, declared error/panic and any allocation/blocking/host effects; unavailable dynamic metadata or failure machinery requires the governing static proof or refusal. An explicit audited unsafe alternative remains available only where the specific source rule and profile/mode policy permit it; it does not waive SAFE3's normal-borrow or SAFE4's safe arena-use refusal. Confine unchecked pointer arithmetic/load/store/cast/representation, manual deallocation, foreign dereference, unwrapped MMIO, unsynchronized device access and uninitialized/padding reinterpretation to explicit permitted audited islands or safe wrappers. Manual lifetime/alias/ownership recovery, unchecked region APIs and unproved manual resource flow retain their source-bound invariants/review/wrapper requirements. A supplied valid/passed/complete flag or manifest is not a proof, guard execution or unsafe approval.
<!-- legality-canonical-memory-classification:end -->


<!-- legality-canonical-memory-model-wrapper-classification:start -->

Apply the [complete source memory-model and provider criteria](plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

Decision criterion interface/language/memory/check-raw-and-mmio-boundary: Admit raw/MMIO operations only through explicit unsafe responsibility or an audited proved/runtime-checked safe wrapper, retaining address-space/width/alignment/volatile/order/grant semantics. Reject: Reject unclassified danger, ungranted device authority, invalid register shape/order and raw operations masquerading as ordinary safe code. Required outputs: Safe-wrapper/unsafe audit and device-map records, proof/check preservation and valid/invalid raw/MMIO diagnostics.

Criterion safety/safe6/safe-wrappers: Admit unsafe internals through a safe public wrapper only when its signature and checked or proved invariants prevent callers violating every required unsafe precondition. Hide raw authority, enforce preconditions, specify failure behavior and connect cleanup to ownership/linear lifetime rules; retain the underlying island and operation in audit/package/release artifacts. Emit wrapper/island/precondition/proof-or-runtime-check/cleanup links under the actual profile/target and review policy. Exercise accepted wrappers and invalid-input, leaked-raw-authority, missing-precondition, unchecked-failure and lost-cleanup refusals with SAFE6-MISSING-BOUNDARY and applicable owning safety diagnostics. A wrapper label, certificate for another subject or reviewed unsafe implementation alone confers no safe API. Consume actual type/ownership/effect/grant facts and GOV9 review where required, as interface artifacts rather than whole-component completion.

L10/L18 raw, foreign and safe-wrapper criterion (Open): Classify every dangerous memory operation through the existing SAFE1/C10 owner. Unproved facts require a profile-legal actual check, rejection or an explicit permitted unsafe island; preserve each specialized rule's stronger static-proof/refusal requirement. Raw pointers, allocator internals, unchecked accesses, MMIO, device and foreign handles remain behind explicit unsafe responsibility or an audited safe wrapper. A safe MMIO wrapper must prove or check actual map, width, alignment, volatility/order, lifetime and capability scope, with the underlying unsafe operation still visible. Foreign contracts retain ownership direction, borrowed lifetime, nullability, initialization, error/exception, thread affinity, callback lifetime and allocator identity. Unexpressible foreign facts require an explicit unsafe boundary or checked wrapper; a binding claiming safe use must state its required ownership/lifetime/nullability/allocator facts. Retain wrapper signature/precondition/failure/cleanup/raw-authority and source-bound review requirements, invalidating changed map/backend/provider assumptions. Reject unclassified danger, ungranted access, invalid width/map/order, raw-safe masquerading and missing or wrong-subject proof/audit. No declared safe status, provider trust or wrapper label is its own proof or permission.
<!-- legality-canonical-memory-model-wrapper-classification:end -->


- [ ] `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution` Preserve obligations into MIR and runtime execution

Status: Open

Evidence baseline: [Safety pipeline](../../bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj)

Closing evidence: Surviving check/proof/source edges; missing-check and unjustified-elision rejection
<!-- legality-canonical-ownership-handoff:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 transfer and MIR preservation criterion (Open): Consume actual analysis products and compose explicit ownership transfers across function, task, actor, workflow, FFI, callback, tool and model-request boundaries, preserving source consumption, destination owner/lifetime/provider and cleanup obligations. The ownership task supplies state-transition legality; this task owns actual boundary composition and preservation under compiler/c9/ownership-transfer, not a second transfer analyzer. Generated code cannot duplicate linear resources or drop ownership facts. Attach relevant ownership/lifetime/region/linear facts and check/audit/source/generated edges to actual MIR allocation, load, store, call, spawn, send, receive, release and cleanup operations. Preserve current facts through safety, MIR, Opt, Lower, backend, runtime and diagnostic consumers; changed inputs invalidate affected assumptions/checks rather than silently inheriting them. Refuse missing or stale facts, illegal detached capture, callback retention, incompatible provider transfer, dropped cleanup or order and unsupported runtime checks before affected consumption/emission. Retain exact producer/source/operation/profile/target/provider/checker and consumer identities, positive/refused boundary fixtures and actual retained-check/cleanup execution where claimed. Typed-Core interfaces permit local fixtures; actual producer and runtime/target products are required for composition, not whole-component start gates.
<!-- legality-canonical-ownership-handoff:end -->



<!-- legality-canonical-safety-preservation:start -->

Apply the [complete safety-pipeline source criteria](plan.md#complete-safety-pipeline-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C10 safety proof lifecycle and consumer criterion (Open): Consume real operation outcomes/checks/obligations/proofs/audits and preserve their Core-to-MIR-to-Opt/Lower/backend/runtime/diagnostic identities and source/generated mappings. Runtime/backend owners emit and execute actual checks and declared failures; checks remain visible in MIR and backend artifacts unless valid later proof permits erasure. Consume subject-bound proof/certificate evidence under SAFE15 trust and claim policy; do not treat solver, fixture, manual-audit or provider labels as universal proofs. Opt may remove a check only with preserved or regenerated evidence for its exact condition, valid assumptions and required dominance through intervening transformations. Record erased check, proof/certificate, pass, operation, profile/target and invalidation; retain residual checks for uncovered conditions. Changed source, facts, control/data flow, layout, numeric mode, provider, target or backend assumptions require proof mapping preservation or affected rechecking/regeneration; restore the check or reject when preservation fails. Equivalent policy artifacts are required to discharge capability/effect, unsafe-audit, replay and :ai/human-review checks; speed never supplies authority. Refuse missing/reordered/detached checks, absent/uncheckable/stale evidence, lost taint/audit/origins and imported dependency safety claims without verified artifacts. Supply consumable safety products to existing certificate and conformance owners. This task retains compiler/c10/safety-proof-lifecycle and owns boundary preservation, while specialized analysis, proof checking, MIR construction, optimization and execution keep their existing owners.
<!-- legality-canonical-safety-preservation:end -->


<!-- legality-canonical-safety-root-preservation:start -->

Apply the [root safety and unsafe-audit source criteria](plan.md#root-safety-and-unsafe-audit-source-criteria) within this existing task. Status remains Open. Restored finite criteria retain their frozen review scope; current composition and whole-source independent review remain pending.

SAFE1/SAFE6 preservation and audit-consumer criterion (Open): Begin safety after ordinary generated-code expansion and continue through actual type/effect/capability/ownership/memory/profile checks into typed Core, MIR, optimization, lowering, linked dependencies and runtime/artifact consumers. Preserve each operation's classification, checks, proofs, unsafe audits, mode/profile/target, taint, source/generated and dependency identities. Changed facts require valid proof mapping or invalidation and affected rechecking; checks cannot be erased before exact surviving proof, and invalid evidence requires restoration or refusal under existing C10/SAFE15/PERF10 criteria. Keep stable machine-readable unsafe/review/wrapper/dependency records visible even when permitted, including in release/package policy, conformance, audit/documentation and language-server safety views. Supply all required safety classifications, check records, audits, proof references, diagnostics, profile-safety capability reports, generated provenance, check-erasure justifications and certificate inputs to their existing consumers. Reject lost metadata or hidden dependency unsafe behavior and revoked/wrong-source/expired evidence; this handoff owns composition, not a second analyzer, package publisher, unsafe reviewer or proof checker.
<!-- legality-canonical-safety-root-preservation:end -->


<!-- legality-canonical-memory-preservation:start -->

Apply the [complete memory-safety source criteria](plan.md#complete-memory-safety-source-criteria) within this existing task. Status remains Open; finite review scope is frozen and current whole-source/composition review remains pending.

SAFE2-SAFE5 memory consumer-preservation criterion (Open): Compose the existing analysis producer's actual allocation/release/range/layout/init/lifetime/borrow/region/generation/linear/transfer/cleanup/check/proof/audit/provider facts with C10, MIR, Opt, Lower/backend, Runtime and diagnostics. Actual consumers emit and execute guards, declared failure, cleanup and transfers where claimed; the original thirty producer criteria still require that evidence, not record-only substitutes. Inlining, loop changes, check elimination, escape/region inference, allocation sinking, vectorization, FFI lowering and device scheduling invalidate affected assumptions unless exact valid mappings survive; recompute/recheck or restore checks/refuse. Backends preserve actual alignment, bounds, lifetime, aliasing, volatile, atomic and allocator facts. Exact erased-check proof and residual checks follow SAFE15/PERF10, retaining source/generated and provider identities. Types/Effects/Auth/provider/profile/source/target/checker remain distinct actual inputs. This handoff owns boundary composition, never a second state analyzer, checker, backend, allocator or resource provider, and ordinary declared-interface fixtures need no whole-component or migration completion.
<!-- legality-canonical-memory-preservation:end -->


<!-- legality-canonical-memory-model-preservation:start -->

Apply the [complete source memory-model and provider criteria](plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10/L18 memory artifact preservation criterion (Open): Serialize actual regime/allocator/runtime/provider/version, ownership/borrow/lifetime/region/escape/init/alias/range/layout/linear/transfer/check/proof/audit and applicable device/MMIO/FFI facts into typed artifacts for their real MIR/Opt/Lower/Runtime/diagnostic consumers. Every actual transformation preserves its exact memory-safety premises or invalidates and recomputes/rechecks them, retaining a legal check or refusing unsupported use. Preserve source/generated identities, memory spaces, physical layouts/alignment, volatile/barrier/atomic order, selected allocator/releaser, safe-wrapper invariants and dependencies; target noalias/alignment/dereferenceable metadata needs current subject-bound proof. Layout/ABI/provider/backend/device-map changes cannot inherit stale proofs. Compiler and API annotations, actual linked manifests and runtime behavior must agree. Demonstrate actual accepted and refused consumer paths, proof invalidation and retained/elided checks under existing SAFE15/PERF10 obligations; this handoff neither implements the allocator nor accepts every profile/provider/backend.
<!-- legality-canonical-memory-model-preservation:end -->


- [ ] `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface` Close conformance for the declared profile surface

Status: Open

Evidence baseline: [Architecture](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)

Closing evidence: Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile
<!-- legality-canonical-ownership-conformance:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 ownership conformance and diagnostic criterion (Open): Exercise actual accepted many-immutable/exclusive-mutable borrows; rejected mutable alias, use after move/consume, move while borrowed and invalid lifetime escapes through return/closure/task/global/FFI/callback/workflow/tool/model/generated/storage paths; valid/invalid nested regions and arena reset invalidation; provider-specific normal/error/panic/cancellation/early-return linear terminal paths; explicit function/actor/task/FFI and other claimed transfers; supported runtime checks with actual failure and unavailable-profile refusal; and permitted audited manual lifetime/alias/resource cases. Preserve all required ownership/borrow/lifetime/move/escape/region/arena/linear/transfer/runtime-check/audit/diagnostic artifacts with actual source-bound producer and consumer results. Preserve all twelve C9 meanings: C9-USE-AFTER-MOVE, C9-USE-AFTER-CONSUME, C9-BORROW-ESCAPE, C9-MUT-ALIAS, C9-MOVE-WHILE-BORROWED, C9-REGION-ESCAPE, C9-ARENA-GENERATION, C9-LINEAR-LEAK, C9-LINEAR-DOUBLE, C9-TRANSFER, C9-RUNTIME-CHECK and C9-UNSAFE. Diagnostics bind value/owner/borrow/region/arena-generation/resource/control-path/source-span/generated-origin/profile/target/remediation context. Reject implicit owned-mutable copies, hidden GC fallback, finalizer-only deterministic cleanup, detached local borrows without lifetime proof and generated duplication/lost facts. Canonical sample ids, empty lists and accepted labels illustrate shape, not actual support. This existing task composes family fixtures and compiler/consumer evidence; Release retains its actual suite/admission role. Full L10/SAFE3/SAFE4/SAFE5/SAFE8, whole-profile, release, migration and self-hosting acceptance are not inferred from this C9 source proposal.
<!-- legality-canonical-ownership-conformance:end -->



<!-- legality-canonical-safety-conformance:start -->

Apply the [complete safety-pipeline source criteria](plan.md#complete-safety-pipeline-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C10 safety conformance and diagnostics criterion (Open): Demonstrate complete real operation inventory and exactly one operation outcome, accepted proven and checked cases, deterministic rejected cases, fully audited permitted unsafe cases and mode/package refusal. Inspect emitted exact-operation check/failure/effect/source records, subject-bound proofs/certificates, full audits, taint/capability reports, dual generated-form/generator diagnostics and actual consumer preservation/invalidation. Include duplicate/conflicting/missing outcome and unknown-operation controls, unavailable checks and undefined failure, invalid proof or audit/trust, dropped taint/authority/foreign/numeric facts and stale optimization evidence. Exercise retained-check and optimized modes, valid proof-backed elision, residual checks and rejected/retained-check cases when proof or backend assumptions fail. Preserve C10-NO-OUTCOME, C10-PROOF, C10-CHECK, C10-UNSAFE, C10-GENERATED, C10-TAINT, C10-CAPABILITY, C10-FFI, C10-NUMERIC and C10-OPTIMIZATION meanings. Diagnostics carry operation id, specialized SAFE rule, source span, generated-origin chain, profile, target, safety mode, missing fact, proof/check id when present and remediation; match stable IDs/spans rather than prose alone. Feed actual compiler/profile/target/fixture/expected/actual/diagnostic/artifact/proof/audit results to existing SAFE15 certificate and SAFE16/Release conformance consumers. All required SAFE families and profile/provider cases remain visible; a C10 coordinator source proposal or hosted document-coverage receipt does not accept whole SAFE1-SAFE16, PERF10, profile, runtime, release, self-hosting or migration support.
<!-- legality-canonical-safety-conformance:end -->


<!-- legality-canonical-safety-root-conformance:start -->

Apply the [root safety and unsafe-audit source criteria](plan.md#root-safety-and-unsafe-audit-source-criteria) within this existing task. Status remains Open. Restored finite criteria retain their frozen review scope; current composition and whole-source independent review remain pending.

SAFE1/SAFE6 conformance and diagnostic criterion (Open): Exercise every safety dimension with real proven/checked positives and missing-proof/check, forbidden-island, incomplete-audit, unavailable-profile, weaker-dependency and generated-origin refusals. Test all safety/review modes and actual policy exceptions, safe-wrapper signature/precondition/failure/cleanup/authority protection, generated macro/facet/plugin/schema/AI/build cases, complete dependency summaries, source-version review invalidation and audit emission for every island including allowed islands. Inspect actual checks with defined failure and optimization proofs/residual checks plus SAFE15/SAFE16-consumable artifacts. Preserve SAFE1-NO-OUTCOME, SAFE1-PROOF-MISSING, SAFE1-CHECK-MISSING, SAFE1-CHECK-ILLEGAL, SAFE1-UNSAFE-POLICY, SAFE1-UNSAFE-METADATA, SAFE1-GENERATED-PROVENANCE, SAFE1-OPTIMIZATION-PROOF and SAFE1-DEPENDENCY-MODE. Preserve SAFE6-UNSAFE-FORBIDDEN, SAFE6-MISSING-METADATA, SAFE6-MISSING-OWNER, SAFE6-MISSING-INVARIANT, SAFE6-MISSING-BOUNDARY, SAFE6-REVIEW-REQUIRED, SAFE6-GENERATED-UNSAFE, SAFE6-CAPABILITY, SAFE6-DEPENDENCY and SAFE6-CERTIFICATE. Diagnostics bind source/generated origins, active profile/mode, missing fact/evidence, specialized rule and remediation; unsafe diagnostics also bind island/owner/policy/operation family/effects/capabilities. Supply exact fixture/compiler/profile/target/provider/checker/expected/actual/artifact/diagnostic results to existing conformance/package/release consumers. Ordinary fixture construction needs declared interfaces; actual composition and independent acceptance need real products. Neither restored finite review nor full source mapping accepts specialized family implementations, whole profiles, release, migration or self-hosting.
<!-- legality-canonical-safety-root-conformance:end -->


<!-- legality-canonical-memory-conformance:start -->

Apply the [complete memory-safety source criteria](plan.md#complete-memory-safety-source-criteria) within this existing task. Status remains Open; finite review scope is frozen and current whole-source/composition review remains pending.

SAFE2-SAFE5 family conformance criterion (Open): Exercise every document's positive, negative, unsafe and supported/unsupported profile/provider cases with actual facts and emitted/executed check/failure/cleanup/transfer evidence where claimed. Include safe initialized in-bounds live access and independent missing-premise refusals; partial/moved/conditional/foreign/device initialization and initialized-only drop; all layout/allocator/lifetime/raw hazards; immutable/exclusive/nested/range-split borrowing and generic/call/FFI/task escape contracts; scoped/nested regions, arena reset, cleanup/provider/threading and foreign retention; structured/manual/error/task/actor/provider terminal flow, cancellation and generated duplication. Inspect all required memory/ownership/borrow/lifetime/escape/allocation/region/generation/reset/provider/cleanup/linear/terminal/structured/generated/check/proof/audit/backend artifacts. Preserve all 43 SAFE2/SAFE3/SAFE4/SAFE5 diagnostic meanings: SAFE2-UNINIT, SAFE2-BOUNDS, SAFE2-LIFETIME, SAFE2-ESCAPE, SAFE2-ALIAS, SAFE2-ALLOC-FAILURE, SAFE2-ALLOCATOR, SAFE2-USE-AFTER-RELEASE, SAFE2-DOUBLE-RELEASE, SAFE2-RAW, SAFE2-CHECK-ERASE, SAFE2-PROFILE; SAFE3-USE-AFTER-MOVE, SAFE3-USE-AFTER-CONSUME, SAFE3-BORROW-ESCAPE, SAFE3-MUT-ALIAS, SAFE3-MOVE-WHILE-BORROWED, SAFE3-CONSUME-WHILE-BORROWED, SAFE3-LIFETIME, SAFE3-TASK-CAPTURE, SAFE3-FFI-OWNERSHIP, SAFE3-RUNTIME-CHECK, SAFE3-UNSAFE-ALIAS; SAFE4-REGION-ESCAPE, SAFE4-ARENA-ESCAPE, SAFE4-POST-RESET, SAFE4-INNER-TO-OUTER, SAFE4-RETURN, SAFE4-TASK, SAFE4-FFI-RETAIN, SAFE4-CLEANUP, SAFE4-PROVIDER, SAFE4-RUNTIME-CHECK; SAFE5-LEAK, SAFE5-DOUBLE-CLOSE, SAFE5-USE-AFTER-CLOSE, SAFE5-BRANCH, SAFE5-TRANSFER, SAFE5-CAPTURE, SAFE5-WRONG-PROVIDER, SAFE5-CLEANUP-FAILURE, SAFE5-CANCEL and SAFE5-GENERATED and complete document-specific operation/value/owner/borrow/lifetime/region/arena/generation/resource/provider/acquire/terminal/control-path/source/generated/profile/missing-fact/remediation context. Match rule IDs and spans; wrong artifacts fail despite correct prose. Prove exact check-elision assumptions and refusal after invalidation. Existing Runtime/Backend owners supply actual behavior and Release owns the conformance harness and later independent admission; full L10/L18/SAFE7/SAFE8/provider/profile, release, migration and self-hosting acceptance are not inferred from finite source reviews or this current proposal.
<!-- legality-canonical-memory-conformance:end -->


<!-- legality-canonical-memory-model-conformance:start -->

Apply the [complete source memory-model and provider criteria](plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10/L18 source-family conformance criterion (Open): Exercise the complete source memory/provider surfaces and declared supported/unsupported profile combinations with actual compiler facts and required provider/API/backend results. Include safe initialized live bounds/borrow/region use; forbidden hidden/ambient allocation and host-runtime assumptions; invalid alias/lifetime/escape/init/linear/forget behavior; actual allocation/failure/release/leak/allocator matching; GC/RC/pinning/deterministic-cleanup distinctions; device/unified visibility and transfer/synchronization; MMIO map/width/alignment/volatile/order/authority; foreign ownership/null/error/affinity/callback boundaries; safe-wrapper proof/check/audit and changed-assumption refusal. Preserve all eight L10 meanings: L10-HIDDEN-ALLOC, L10-USE-AFTER-MOVE, L10-BORROW-ESCAPE, L10-UNINIT-READ, L10-BOUNDS, L10-RAW-SAFE, L10-MMIO-CAP, L10-LINEAR-RESOURCE; and all thirteen L18 meanings: L18-PROVIDER, L18-HIDDEN-ALLOC, L18-LIFETIME, L18-ESCAPE, L18-ALIAS, L18-UNINIT, L18-DOUBLE-RELEASE, L18-LEAK, L18-BOUNDS, L18-DEVICE-SYNC, L18-MMIO, L18-FFI-ALLOCATOR, L18-UNSAFE-AUDIT. Diagnostics and artifacts bind actual source/generated/profile/provider/memory-family/lifetime/region/grant and required-proof/check context. Consumer examples and backend checks preserve allocator and memory-space facts. Feed existing Release execution and independent admission, without treating source review, a record-only fixture or one target as full acceptance.
<!-- legality-canonical-memory-model-conformance:end -->


<!-- legality-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Legality workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare spec.md and plan.md with retained contract links and the canonical component task/acceptance anchors.
- [x] T002 [US2] Prepare tasks.md and evidence.md with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate the four owned artifacts, relative links/anchors, negative controls and existing document/roadmap checks; record results in evidence.md.
- [ ] T004 [US2] Independently review the exact candidate and record disposition in evidence.md or the linked PR.
- [ ] T005 [US2] Verify integration to main and record the merge receipt separately from local validation.

## Dependencies and outcomes

T003 follows artifact preparation; T004 follows successful validation; T005
follows independent acceptance. These five migration tasks are not a copy of
the [component implementation tasks](../../docs/component-roadmaps/legality.md#remaining-tasks-and-evidence).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
