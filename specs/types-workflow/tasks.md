# Tasks: Types implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler types maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Implementation tasks

This is the sole editable Types implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `types/1` through `types/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- types-implementation-tasks:start -->

- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints` types/1: Solve core constraints

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement literal, binding, function/application, branch, collection and error-result constraints with actionable mismatch diagnostics.


<!-- types-source-constraints:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

C7/L5 core-inference and local-evidence criterion (Open): Consume actual resolved C5 bindings and C6 core/evaluation-order records after expansion/resolution. Give every core expression an inferred type, checked declared type, explicitly profile-accepted Dynamic boundary or rejected typing diagnostic; inference is local, deterministic and evidence-producing, never an unconditional solved/Dynamic label. Derive types from actual literals, declarations, bindings, parameters, patterns, constructors and expected-type checking without inventing missing premises. Represent bottom/never, nil/unit, Boolean, numeric families, symbols/keywords, text/views, persistent collections, tuples, structs/records, enums/unions, functions/protocols, generic/existential/effect, memory/resource/initialization/taint, compile-time/const-generic, schema, meta syntax/IR and artifact-reference families in a stable artifact schema; do not invent advanced annotation syntax from examples. Constraint records cover equality, subtype, protocol implementation, generic bounds, numeric family/width, region/lifetime, ownership/borrow, linear consumption, initialization, schema refinement and thrown-error propagation, each with source node, producer rule, dependencies, solution and invalidation. Bare List/Map parameters forget only parameter refinements: retain independently established actual argument types/provenance and per-parameter boundary compatibility, including mixed List/Map/scalar calls; reject unrelated families, implicit Dynamic, invented key/member/extent facts, changed return/recur rules and erased schema/ownership/linearity/regions/effects/profile obligations. For a homogeneous (List T) parameter or return, resolve List and atomic T in type positions, check all independently established positional members or a matching homogeneous carrier, permit empty Lists and nonreturning bodies under their existing rules, and establish body types before checking declared returns. Reject normally returning bare/unknown/mismatched member types and expected-return-driven inference. Preserve (List T) through rest; first of empty remains nil, and excluding nil requires nonemptiness for this exact value and control-flow path, invalidated by relevant rebinding/mutation or taking a tail. Canonical if List joins retain both actual branch types and a join-specific compatibility premise: independently matching homogeneous/positional carriers require at least one independent T witness; empty positional supplies none, homogeneous supplies its declared T even if empty, two exact empty branches remain exact, and existing exact/Never rules take precedence. Canonical if nil-bearing joins require independently established nil and one exact non-nil atomic T, retaining a closed nil-or-T artifact through inferred local returns/calls and actual value equality. Unknown/conflicting alternatives reject; nil is not an Option wrapper or host null, nil-or-Bool is not established Bool for an L7 guard, and declared non-nil returns cannot discard nil. Neither join rule expands match/try/loop/recur joins or introduces general union/coercion/subtyping. Private immutable bare-Map member inference requires actual closed visibility/exports and every resolved use, entry and parameter re-entry, at least one independent normal witness and the same literal key with compatible selected type at every normal entry, preserving each argument source/shape/selection coordinates. Whole-key-domain inference for keys is separate: every normal entry must establish a complete finite Keyword-key Map and validate all stored values, including ignored values; different shapes/value types and an empty normal Map are legal. A Never argument in any coordinate makes the entire call non-invoking; unknown/circular/unaccounted or all-non-invoking evidence proves neither rule. Escapes, public/external entries, mutation and recursive parameter transfers need complete separate evidence; nested-loop transfers are not function-parameter re-entry. Retain entry-specific evidence without a common constructor/deep shape, reconstruct it at every consuming compiler stage, and invalidate on function/parameter/keys use, callers/uses/visibility/exports, arguments/types/compatibility/bindings/mutation/transfers. The parameter stays bare Map; keys yields the existing homogeneous Keyword List including empty, with no key identity/presence/extent/cardinality/nonempty/flow/specialization inference. Ground a Symbol-Set loop invariant only from independently empty persistent-Set initialization, complete original-target re-entry coverage, at least one independent normally invoking Symbol-Set witness and every other normal entry empty or that same carrier. Derive grounding without assuming the invariant; any Never operand makes that whole transfer non-invoking. Retain initializer and every actual re-entry type with source-bound compatibility, separating cold grounding from later invariant-dependent reads; initializer and invariant types are not falsely equal. Empty-only/self-preserving loops retain exact types. Reject unknown/circular/conflicting/non-invoking grounding, lost targets/entries, wrong-value nonemptiness and hidden parameter/branch-rule extension. Required outputs: actual type environment and constraint ledger, independently derived branch/entry/grounding evidence, stable solutions/diagnostics, accepted/refused literal/call/function/collection/branch/match/error and all named boundary/entry/loop fixtures. Existing L2 target/arity/tailness/evaluation and other type/effect/ownership/profile obligations remain; supported atomic and grounding derivations are stated explicitly, with no work/cost allowance introduced. Common annotations use :-; F32/F64 and fixed/machine-size integer representations retain numeric-mode/profile legality rather than host defaults. Joining an already established nil-or-T type with nil, T or the identical nil-or-T retains the same two alternatives under the existing exact/Never precedence. Historical L5 bounded-validation/work fragments are superseded project accounting predicates; they remain recorded source history and add no current allowance or quota.
<!-- types-source-constraints:end -->


<!-- types-source-private-key-domain:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

Decision criterion interface/language/types/derive-private-keyword-map-domain: Derive the whole Keyword key domain of an immutable bare Map parameter only from a complete finite census of private normal entries; preserve the existing homogeneous Keyword List result for keys. Reject: Reject partial/open/public/mutable or otherwise invalid census assumptions, non-Keyword entries and widening inferred keys from mere observed subsets. Required outputs: Private-entry census and key-domain inference evidence, homogeneous List type result and incomplete/invalid-census refusal fixtures.
<!-- types-source-private-key-domain:end -->


<!-- types-pattern-source-narrowing:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 pattern typing and narrowing criterion (Open): Check every actual pattern against its independently established scrutinee type, then type each branch in its actual branch-local narrowed environment. Cover all fourteen L7 pattern families subject to their real profile/runtime/ownership conditions; keep unsupported required families visible as remaining work. Preserve literal, tuple/vector, List, Map, record/struct, union/enum/type/schema/resource bindings and actual Result T/E extraction. Reject incompatible patterns as L7-PATTERN-TYPE and retain actual branch types. Canonical-if-only List/nil joins do not widen match joins. Required evidence: real narrowing tables and accepted/refused family fixtures, not expected-type-driven facts or a pattern catalog.
<!-- types-pattern-source-narrowing:end -->


<!-- types-pattern-source-coverage:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 closed-match coverage criterion (Open): Derive actual exhaustive and unreachable-case results from the current scrutinee domain, constructors, clauses and guards. Require coverage for closed enums/unions, formal and opted-in safety-critical matching and compiler analysis where missing cases are unsound. An explicit default can supply coverage where allowed, retaining applicable future-compatibility warnings/policy diagnostics; it cannot suppress required formal/safety proof. Open/dynamic/host/untrusted shapes require real validation/check premises before closed coverage. Emit L7-NONEXHAUSTIVE and L7-UNREACHABLE with actual missing/unreachable cases. Required evidence: total/default/guarded positives and missing, hidden, stale-domain or unjustified-closedness negatives.
<!-- types-pattern-source-coverage:end -->


<!-- types-pattern-source-guard:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 Boolean guard criterion (Open): Establish Bool for each normally returning guard from actual checked expression and branch-binding facts, or emit L7-GUARD-TYPE. True selects its body and false continues; nil, numeric, text, nil-or-Bool, unknown or Dynamic results are not silently Boolean. A throw or other non-returning guard retains its control transfer without manufacturing a Bool value. Preserve actual guard type, source and binding dependencies. Required evidence: accepted true/false/non-returning cases and rejected non-Boolean/unestablished results, distinct from L2 if truthiness and without authorizing illegal effects.
<!-- types-pattern-source-guard:end -->


<!-- types-error-source-typing:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 actual throw/handler typing criterion (Open): Check real thrown payload types, typed catch bindings and protected/handler normal results against resolved source declarations, preserving nonreturning evaluation and original transfer identities. Actual function/call artifacts expose thrown-error type/effect requirements; distinguish conservative source-error potential from admitted or executed effects. Result/Option pattern coverage uses actual declared constructor domains and L7 evidence, never a canonical-if-only join extension or host truthiness. Reject untyped/unknown/mismatched thrown values, invalid handler bindings/results and missing required handling/propagation. Required evidence: independent payload/result/handler/call constraints, typed/never and accepted/refused source/generated cases with current provenance. Source coverage alone does not discharge profile, handler, effect or capability legality.
<!-- types-error-source-typing:end -->

- [ ] `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` types/2: Check declared abstractions

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Validate generics, protocols/dispatch, records and schema-derived types against their declarations and resolved identities.


<!-- types-source-abstractions:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

C7/L5 declared-abstraction and function-contract criterion (Open): Emit function signatures with actual parameter/return types, latent effects, relevant capability requirements, ownership/borrow and profile restrictions and thrown-error effects. Record actual generic instantiations and protocol method/type facts for MIR/Lower; direct, dictionary, vtable, hosted-dynamic and rejected dispatch choices remain tentative until profile validation and effect checking accept required runtime behavior. Reject failed generic bounds, absent/ambiguous protocol implementations, erased latent/thrown effects, missing ownership/profile facts and dispatch claimed legal merely from a type. Retain ownership/resource type facts for Owned, Borrow, BorrowMut, Region, Ptr, RawPtr, MMIO, Uninit, Init and Linear with actual L10/SAFE2/SAFE3/C9/C10 consumers. Reject uninitialized reads, safe raw-pointer dereference, illegal linear duplication/drop, escaping borrows and MMIO use without profile/capability support; type facts do not replace deeper lifetime, ownership or safety checking. Preserve expanded source-schema identity and validation boundaries for generated types and validators: nullability, optionality, defaults, bounds, refinements, taint and all applicable source constraints survive generated GraphQL/OpenAPI/database/binary-ABI/config/artifact/AI-output contracts and remain linked to the source schema. A generated contract is not accepted because its shape resembles the schema. Required outputs: versioned function signature/generic-instantiation/protocol-dispatch tables, schema type/layout links, memory/resource facts, accepted/refused records/unions/protocols/generics/schema/ownership/linear fixtures and actual downstream handoff records. S1/schema generation supplies its authentic manifest, declared/resolved identities supply constraints, and Effects/Auth/profile/ownership/safety own their independent legality outcomes. Preserve the existing schema static-projection, typed-error, protocol and private-key-domain criteria unchanged.
<!-- types-source-abstractions:end -->


<!-- types-source-schema-static:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

Criterion `types/schema-static-projection/decision-interface` (Open): Input: The exact S1 expanded typed/versioned schema manifest and its source identity/refinement/constraint facts. Outputs: Static type projection artifact with schema identity; Accepted and rejected source-to-type parity fixtures. Accept: Generate static records/unions/optional/reference/recursive/refined types from the expanded schema, preserve resolved names, schema identity and constraints, and compare declared and generated type legality against the same manifest. Record target representation limitations explicitly. Refuse: Type projection from a mismatched/stale schema identity; Dropped nullability/refinement/variant constraint or unrepresentable schema family without explicit policy/rejection; Schema normalization or validator generation substituted for the static type artifact. Dependencies consume actual artifacts: Schema task supplies the expanded manifest; Types resolved declarations/constraints supply legality; downstream validators and bindings consume the projected type facts. Diagnostics: S1-PROJECTION and applicable Types mismatch diagnostics retain schema/version/span/target and remediation. These are required future products/results; no execution or implementation acceptance is claimed.
<!-- types-source-schema-static:end -->


<!-- types-source-typed-outcome:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

Decision criterion interface/language/errors/represent-typed-outcome: Represent Option absence, Result recoverable error, denial, panic, replay and policy failure as their declared distinct typed contracts, retaining applicable origins and audit policy. Reject: Reject conflating capability denial with nil/absence, replay failure with a local exception or AI failure with an unrecorded free-text error. Required outputs: Typed outcome/interface schema and exhaustiveness cases, source/audit records and cross-kind rejection fixtures.
<!-- types-source-typed-outcome:end -->


<!-- types-source-dispatch:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

Decision criterion interface/language/protocol/select-checked-dispatch: Resolve a declared profile/target-legal implementation set and dispatch mode with typed method/effect contracts; retain host-boundary metadata when delegated. Reject: Reject missing/ambiguous dispatch, ungranted reflection/open-world assumptions and method/type/effect/profile mismatches. Required outputs: Protocol/implementation/dispatch and target metadata, legal selection and missing/ambiguous/host-boundary refusal fixtures.
<!-- types-source-dispatch:end -->


<!-- types-pattern-source-trusted:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 trusted-shape boundary criterion (Open): Require actual schema-validation or typed-boundary-check evidence and authentic source identity before external/dynamic/host data is treated as a trusted closed pattern shape. Network/filesystem/database/AI/tool values must cross the real required boundary; AI variants and required keys are checked against their declared schema. Matching itself, a schema-shaped record, a narrowed annotation or a nonempty validation link does not validate a value. Preserve dataflow and surviving checks to selected runtime consumers; Schema/Runtime own actual validators/check execution. Required evidence: accepted validated values and rejected unvalidated, weakened, wrong-source, stale or coherently substituted shape/constructor/key assumptions.
<!-- types-pattern-source-trusted:end -->


<!-- types-protocol-source-methods:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 protocol method/extension criterion (Open): Produce actual typed protocol contracts retaining method name, receiver type position, parameter and return types, latent effects, relevant capability requirements, profile restrictions, permitted defaults and documentation/stability metadata. Check every concrete implementation against that signature without widening effects beyond callers' visible contract; additional capability requirements remain in method types and dispatch artifacts. Resolve actual protocol/type/method identities rather than accepting a protocol-shaped record. Required evidence: real protocol/implementation/signature tables, legal definition/extension/default cases and rejected incompatible receiver/parameter/return/effect/capability/profile cases with L8-PROTOCOL-METHOD and L8-METHOD-EFFECT. Defaults remain conditional on their governing allowance, not universally enabled.
<!-- types-protocol-source-methods:end -->


<!-- types-protocol-source-selection:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 implementation-set/dispatch criterion (Open): Derive actual applicable implementation candidates and selected mode from resolved declarations, independently established type facts, profile, target and governing optimization facts. Preserve selected implementation identity, receiver and method contracts and source references. Reject missing implementation and multiple matches lacking a declared priority rule with L8-DISPATCH-MISSING/AMBIGUOUS; do not invent a global overlap/priority/coherence policy. Typed selection is tentative until actual Effects and profile checks admit required runtime behavior, with separate Auth grants and actual provider facts. No safe implementation set means rejection or a checked runtime-provider route with explicit effects/capabilities, never implicit host dispatch.
<!-- types-protocol-source-selection:end -->


<!-- types-protocol-source-modes:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 dispatch-mode criterion (Open): Keep direct static, monomorphized generic, dictionary, vtable/interface, multimethod, hosted dynamic, explicitly profile-permitted reflective and schema/workflow/AI/tool artifact-boundary dispatch as distinct source-supported modes. Record the actual chosen mode and its implementation/type/effect/capability/profile/target dependencies; the sample direct close/FileHandle record supplies no implementation, grant or universal default. Cover actual legal and refused cases for every applicable mode; unsupported required modes remain visible implementation work and cannot be completed by a refusal or hosted example.
<!-- types-protocol-source-modes:end -->


<!-- types-protocol-source-multimethod:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 declared multimethod criterion (Open): Where multimethod-style dispatch is supported, use the actual declared dispatch function and case/implementation identities. Open multimethods are hosted/dynamic unless a profile-specific table is actually generated with known coverage; closed multimethods may lower to actual matching or jump tables preserving source behavior. Retain dispatch-function effects, actual table/case coverage and missing/ambiguous-selection diagnostics. The illustrated event kinds are examples, not the complete language registry, and optional support does not silently make open dispatch portable or mandate an invented priority rule.
<!-- types-protocol-source-multimethod:end -->


<!-- types-error-source-outcomes:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 distinct error-family criterion (Open): Represent actual declared absence Option, recoverable Result, typed thrown error, panic/unrecoverable failure, assertion/contract failure, safety-check failure, capability denial, FFI error, workflow retry/compensation failure, AI/model/tool/policy/evaluation failure and compiler diagnostic as distinct contracts. Preserve actual Some/None and Ok/Err payload types and source/audit identities across portable constrained profiles. Do not turn capability denial into nil/absence, workflow replay failure into an unrelated local exception or AI failure into unrecorded text. Source nil remains its own L2 value; it is not an implicit Option or host-null conversion. Required evidence: genuine error/outcome declarations, constructor/match type cases and positive/refused cross-family conversions without inventing new error syntax or a host exception hierarchy.
<!-- types-error-source-outcomes:end -->


<!-- types-library-source-typed:start -->

Apply the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 actual public library type-contract criterion (Open): Check actual public function/type/macro/protocol/constructor declarations and selected calls against independently derived argument/result/generic/schema facts and genuine resolved export identities. Preserve declared effect, capability, allocation, failure and profile contracts as inputs without treating them as completed effect/safety admission or callable implementation. Source schemas and intrinsic signatures are authentic; invalid/generated forms follow normal checks. Reject declaration/implementation/call mismatch, unsupported overloads, invented shape or host-default types, retaining actual source/export/version identity and typed accepted/refused cases. Library publishes contracts and implements APIs; Types supplies real type/compatibility facts, while other owners retain effects, grants, safety and target judgments.
<!-- types-library-source-typed:end -->

- [ ] `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` types/3: Make dynamic boundaries explicit

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require checked casts and representability/numeric conversions where specified; reject implicit unsafe narrowing and unsupported profile-dependent dynamic behavior.


<!-- types-source-boundaries:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

C7/L5 explicit-dynamic and conversion criterion (Open): Dynamic values/operations have explicit source-bound records with input/result type, kind, profile, required runtime checks, effects, relevant capabilities and diagnostics, retained through Effects, MIR, runtime metadata and diagnostics. Cover dynamic dispatch, field lookup, casts, type inspection and host reflection; hosted/REPL acceptance is explicit, constrained profiles reject forbidden behavior or demand their required facts and annotations. Core types exclude host object identity; native boundaries require layout/ownership/FFI/resources, systems profiles require actual size/layout/initialization/allocation/MMIO/interrupt/raw-memory facts, GPU boundaries require their profile's size/layout/allocation facts, AI surfaces require typed prompt/tool/memory/output/policy schemas, and formal code rejects unmodeled dynamic behavior/partiality and needs proof-friendly types. A type or target does not grant effects/capabilities or relax profile legality. Subtyping/coercion must be explicit or profile-defined; no silently inserted systems numeric widening, pointer/host casts or schema coercions. Classify exact widening, checked narrowing, explicit rounding, saturating, wrapping, schema-validation, host-nullable-to-Option/Result, FFI-representation and unsafe-reinterpretation conversions. Casts, narrowing/nullability/FFI/raw-pointer/host boundaries are checked or explicitly unsafe; reinterpretation requires an unsafe island and audit metadata. Type checking emits records; safety analysis assigns required proof/check/rejection/unsafe audit, preserving the four D8 outcomes. Required outputs: dynamic-boundary and cast/conversion artifacts, actual profile/annotation/representability and boundary reports, positive hosted checked cases and negative forbidden Dynamic, hidden narrowing, unchecked cast, host-null-to-non-null, missing lifetime/ownership/encoding/layout or untracked unsafe cases. Explicit loss/failure stays Result or declared boundary error and Option absence remains distinct. Reject missing checks/metadata, fabricated grants and reliance on host behavior as Gravity type semantics; retain actual C7/L5 diagnostics for annotation, dynamic, cast, nullability and layout failures.
<!-- types-source-boundaries:end -->


<!-- types-source-foreign-value:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

Decision criterion interface/language/interop/convert-declared-foreign-value: Perform the explicitly directional Gravity/foreign type conversion under declared ownership/nullability/encoding/layout semantics; loss is explicit and failure returns Result or the declared boundary error. Reject: Reject hidden lossy conversion, invalid null-to-non-null mapping and foreign values admitted without required type/lifetime facts; declared Option absence remains legal. Required outputs: Directional type map and conversion/round-trip results, declared loss/failure records and invalid-value/ownership refusals.
<!-- types-source-foreign-value:end -->


<!-- types-protocol-source-dynamic:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 explicit dynamic/foreign-boundary criterion (Open): Represent actual dynamic/reflective/host dispatch with method/receiver/type identity, selected runtime dependencies, source profile, nullability, exception/failure behavior, ownership, required runtime checks, effects and capabilities. Preserve a declared host boundary and actual normalization route before values/errors enter safe core semantics; a host null or exception cannot become an unmodeled core value/throw. Types records the concrete boundary; Runtime performs normalization and resource behavior, Effects checks actual effects and Auth grants separate authority. Reject missing type/null/exception/lifetime/runtime facts and forbidden dynamic/reflective behavior with L8-HOST-DISPATCH or L8-DYNAMIC-FORBIDDEN; metadata alone is not executed normalization.
<!-- types-protocol-source-dynamic:end -->


<!-- types-error-source-ffi:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 complete FFI error-boundary criterion (Open): Check and preserve actual declared error-return convention, errno/equivalent state, exception behavior, nullability, ownership transfer on failure, resource cleanup, panic/abort behavior and typed Gravity error mapping. Declared Option absence remains legal, while real errors and denials cannot become absence. Host nulls/exceptions cross into safe/non-null types only through the actual required checked/typed boundary. Types owns concrete mapping/type/dataflow facts; Runtime performs actual conversion/normalization and failure cleanup. Reject missing/incompatible/stale mappings or untranslated failures with L9-HOST-ERROR/FFI-ERROR and governing interop diagnostics. Required evidence: source-bound complete error-map/host contract and accepted/refused nullable/typed-error/failure-resource cases without enabling held native/provider/callback execution.
<!-- types-error-source-ffi:end -->

- [ ] `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts` types/4: Preserve and invalidate facts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Serialize type solutions and layout requirements with source/profile/ownership/effect references; invalidate on changed nodes, bindings or contracts.


<!-- types-source-artifacts:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

C7/L5 current typed-artifact and invalidation criterion (Open): Serialize actual typed core, type environment, constraint ledger, function signature table, generic instantiations, protocol dispatch types, dynamic-boundary and cast/conversion records, layout/schema links, ownership/resource facts and type diagnostics/conformance fixtures. Bind the module to its actual core-input hash; nodes, locals and functions retain their real identities, type/mutability/ownership, latent/thrown effects, capabilities and current constraint/boundary/cast references. The sample artifact's filesystem effect, FileError, module/type/hash values illustrate fields and do not supply facts or grants. Preserve source node/syntax/span/generated-origin chains, resolved binding, profile/target, schema/version and all relevant declared/actual type, effect, ownership/resource and representation evidence. Constraints retain producer rule, dependencies, solution and invalidation; changed nodes, source, resolved declarations/bindings, callers/entries/uses/visibility/exports/argument or transfer premises, schema constraints, profile/target or downstream contracts invalidate affected solutions/boundaries/layout/dispatch and consuming artifacts, rather than silently reusing a solved flag. Required outputs: stable serialized schemas and provenance, current/invalidated fact maps naming actual consumers/rechecks, round-trip and changed-input refusal fixtures. New type forms require artifact-schema updates and fixtures before stable-document use; type changes identify affected Effects, memory safety, profiles, MIR, backends, schemas, AI, stdlib and conformance consumers. This is contract change control, not an extra ordinary-edit permission gate. Reject stale/mismatched/missing source or schema identities, erased actual argument/body types or compatibility premises, dropped origins/latent effects and unsupported type forms concealed by generic metadata.
<!-- types-source-artifacts:end -->


<!-- types-protocol-source-artifacts:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 complete dispatch-artifact criterion (Open): Serialize actual protocol and implementation tables, method signatures, selected dispatch-mode records, applicable multimethod tables, interface-lowering and host-interop records, diagnostics and conformance fixtures with current producer/source/generated/binding/type/effect/capability/profile/target/ABI/schema/provider identities. Preserve all seven L8 diagnostic meanings and actual source failure subjects rather than a requested diagnostic catalog. Later interface/runtime/conformance products retain their actual producing owners; typed serialization cannot fabricate them. Changed protocols, implementations, dispatch functions/cases/priority declarations, types, effects, grants, profiles, target layouts, host contracts or specialization proofs invalidate affected selection and consumers. Required evidence: real round-trip/current-identity checks and missing/stale/tampered/coherently substituted refusal fixtures; a complete flag or nonempty link is not acceptance.
<!-- types-protocol-source-artifacts:end -->


<!-- types-error-source-artifacts:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 complete error-artifact criterion (Open): Preserve actual error declarations, function thrown-error/effect records, panic lowering, safety-check failure, FFI mapping, workflow failure, AI/tool failure and diagnostic fixtures with current source/generated/binding/type/effect/grant/profile/target/runtime/provider/schema/check/replay/policy identities. Each later runtime/target/workflow/AI artifact retains its actual producer; typed serialization cannot fabricate it. Keep all seven L9 diagnostic meanings and real failure subjects, not a requested-code catalog. Changed payload/handler/call/control/type/effect/mapping/profile/service/schema/retry/compensation/policy/source inputs invalidate affected facts and consumers. Required evidence: actual stable round-trip/currentness checks and missing/stale/tampered/coherently substituted refusal fixtures, with no complete-container or nonempty-link acceptance.
<!-- types-error-source-artifacts:end -->

- [ ] `types/current-evidence-and-remaining-work/types5-integrate-typed-core` types/5: Integrate typed core

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed Effects and MIR prerequisites from actual solutions, with accepted inferred/declared cases and rejected unsatisfied, stale and illegal-conversion cases.


<!-- types-source-verification:start -->

Apply the [complete Types source criteria](plan.md#complete-types-source-criteria) within this existing Open task. Preserve original criteria and exact historical review scopes. This restores already-published planning text; the original C7 publication operation, implementation reservations and all execution restrictions remain unchanged.

C7/L5 typed-core verifier and consumer criterion (Open): Verify after actual expansion/name resolution/Core production and before Effects can claim legality that each node has exactly one current type or diagnostic, all constraints are solved or rejected, function latent effects are present, all nontrivial conversions have cast records, Dynamic boundaries are profile-marked, schema-derived types reference source schemas, required layout facts exist and source/generated origins survive. Malformed typed-core artifacts refuse with C7-VERIFY and prevent downstream legality claims. Typed core feeds actual Effects and ownership/safety/profile/capability validation; it is not target-lowering authority until those required checks pass. MIR/lowering fixtures must preserve the checked type facts and generic/dispatch/schema/boundary contracts; actual later checks consume current artifacts, not fabricated summaries or completed-component prerequisites. Required outputs: positive typed-core emission for literals, calls, functions, records, unions, matches, protocols, generics, dynamic/schema/ownership/linear surfaces; deterministic constraint/annotation diagnostics; accepted/rejected boundary, cast/unsafe, layout/schema, stale-artifact and verifier-tamper fixtures; actual producer/verifier/consumer records and source/generated-origin diagnostic goldens. Preserve all ten C7 meanings (TYPE-MISMATCH, ANNOTATION, DYNAMIC, CAST, NULLABILITY, GENERIC, PROTOCOL, LAYOUT, SCHEMA, VERIFY) and all eight L5 meanings (TYPE-MISMATCH, ANNOTATION-REQUIRED, DYNAMIC-FORBIDDEN, CAST-UNSAFE, UNINIT-READ, LINEAR-DUP, SCHEMA-WEAKEN, LATENT-EFFECT-MISSING). C7 diagnostics retain core node, syntax ID, source span, expected/actual types, active profile, target, relevant binding, generated-origin chain and remediation. These clauses require future real results and artifacts; source propositions, structural checks and existing partial Dynamic inference do not prove type-system/component, compiler-stage, migration, release or final-count completion. Earlier independent reviews retain exact subjects/scopes and do not accept these new appends; no compiler/helper/runtime/provider/broker/controller/probe execution is authorized or reported here.
<!-- types-source-verification:end -->


<!-- types-protocol-source-composition:start -->

Apply the [complete protocol and dispatch source criteria](plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 actual checked-dispatch composition criterion (Open): Compose actual source protocol definitions/extensions, typed method/implementation selection, visible effects and separate grants, profile decisions, selected interface/runtime artifacts and verified downstream consumers. Owner fixtures cover definition/extension, direct/dictionary/vtable/hosted dynamic and rejected reflective dispatch, call-site implementation/upper-bound effects, kernel/firmware/hardware open-dispatch refusals, actual host null/exception normalization, proved specialization with preserved diagnostics/fallback and artifact mode/implementation identity. Keep all required mode/interface/profile surfaces visible; representative fixtures or table presence cannot accept complete L8. Required evidence: actual positive/refusal program/artifact/diagnostic/behavior comparisons and current source/generated provenance; owner fixture construction precedes independent final conformance and no universal optimizer or native runtime is a prerequisite for valid unspecialized portable dispatch.
<!-- types-protocol-source-composition:end -->

<!-- types-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Types workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
