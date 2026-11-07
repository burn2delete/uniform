# Native Stage0 plan assembly

## Scope and governing contracts

Move the complete existing `stage0-compiled-core-plan` body from Bootstrap to
`gravity.lowering/assemble-native-stage0-plan`. Retain the original Bootstrap
name and three-argument API as an adapter. This slice also qualifies the
existing native compiler report's source-span preservation claim; it adds no
provenance fields to the instruction plan and does not change plan identity.

D0, D1, D2, D3, D6, D8, D9, L2, C1, C6, C14, C15, BOOT1, BOOT8 and TEST2
govern the boundary and its explicit limitations. Canonical C1/C6/C14 facts are
obligations, not authority supplied by a native record or callback result.

The frozen predecessor is `52d6138967b7392acd9a9c1613f13f5cb9140e8a`, tree
`3bf7816b4673fadad46f5308632b88f907b6a506`. The isolated branch begins at
normal published-Main composition `60ec9dbc032138236925caac25c010d4d97673d7`,
tree `26b445567ffae815e3a118083c2c36571721b9be`, whose parents are that
predecessor and published Main `de1eccf9a148cc199ff50f41014bc4732b715a1d`.
Exactly three published Advance paths were imported; all other predecessor
modes/blobs are conserved. No predecessor acceptance transfers to this slice.

## Public API, artifacts and dependency direction

The proposed API is:

```clojure
(assemble-native-stage0-plan operations source-path source-text module)
```

It consumes the caller's exact source path/text, native module and expanded
forms, and an explicit map of live owner Vars. It emits the existing
`:gravity/stage0-hosted-core-compiled-plan`: source path/hash, temporary compiler
metadata, selected module context, sorted function map and binding table,
instruction/effect summaries, entrypoint and deterministic plan ID.

The twenty owner ports cover function-table admission, fifteen policy calls,
function compilation, instruction summary, failure transport and SHA-256.
The fifteen calls keep their original fixed order and argument shapes: profile,
executable profile, effects, safety, performance, math, compiler, backend,
runtime, domain, schema, AI, package, tooling and conformance. The stage does
not iterate or sort a caller-supplied gate catalog. Each Var is selected at its
original use; serialization retains the same `clojure.core/pr-str` operation.

Bootstrap retains source intake, macro expansion, header/function-table
admission, every individual validator implementation, diagnostic construction
and publication, report projection, instruction execution and Runtime authority.
Bootstrap calls Lowering, which requires neither Bootstrap nor Runtime, CLI,
release or a backend. Supplied operations are trusted seed dependencies, not
authenticated policy grants.

The assembler owns the existing missing-main and main-arity structural
decisions, selecting `L3-UNKNOWN-ALIAS` and `L2-MAIN-ARITY` through the existing
failure Var. Header, policy, expression, function/builtin arity and execution
failures retain their existing owners, payloads and order.

## Preservation and report correction

Preserve the whole plan shape, hash preimage, map ordering, function-table
iteration, binding projection, limited println-based effect inference, opaque
values, callback return behavior, selected-callable timing and exact exceptions.
The two SHA calls remain late; hash selection precedes plan serialization.
The existing twenty-two Lowering implementations remain unchanged.

The native report in component `00562` currently claims that instruction-plan
emission preserves source spans, which the plan does not carry. Qualify that
emit-pass record and compiler report to name the actual path/text-hash/module
boundary and the unsupported instruction-span/origin/hygiene facts. Report IDs
change with the truthful metadata. Plan bytes, plan IDs, validator semantics,
diagnostic identities and execution remain unchanged. The report constructor
and its supplied status labels remain Bootstrap-owned and non-authoritative.

## Evidence and acceptance

Use the genuine public route: `macro-source-artifact`, its actual module and
expanded forms, `stage0-compiled-core-plan`, and the unchanged public executor.
Do not bind authenticated source products, replace Reader operations or invoke
private source factories. Record complete baseline/current plans and stdout
for both source extensions, and genuine missing-main and main-arity refusals.
Test report qualification separately from unchanged plan identity.

Read-only boundary discovery is recorded by SHA256
`1cc083aee5e5a21e719dc51a0f7dc457d5998b9bc2f9c715f4ec91ad194c420e`.
A real predecessor-source probe reached both entry guards after compiling a
function, refusing with the expected source paths and diagnostics; receipt
SHA256 `9e256b10dc9a5cc1e5c0ee5f73e728d28b939d1443e707d59e25809fe66af4d4`.
Those observations are scope evidence, not acceptance of an implementation.

Focused tests must cover policy ordering/refusal barriers, live port changes,
function and summary realization, hash/serialization timing, identities,
returning failure ports, exception identity, report nonclaims, real source
emission/execution and refusal. Preserve all earlier CI vectors and add one
whole-namespace command. Run affected regressions, the language boundary,
docs/roadmap checks and lint; obtain independent semantic/adversarial and exact
composition/provider/CI review before integration eligibility.

## Current status and unsupported boundaries

This is the early draft scope. No new API is implemented or accepted yet.
The original eleven-path lease governs authoring. This manually authored
external candidate has no Main, Ready, controller, service or recovery role;
the designated integrator retains current-base preflight and Main integration.

This is temporary Clojure-hosted native plan assembly, not canonical Core,
checked Core, MIR, optimized/domain IR, target lowering, backend emission or
execution. The plan does not carry canonical node identities, per-instruction
spans, generated origins, hygiene, complete type/effect/ownership/safety proof
facts or verifier results. Hashes provide the existing deterministic identity,
not source or callback authentication. Existing if/quote shape permissiveness,
map/set printer sorting, limited effect inference and builtin-vocabulary gaps
remain. No new source/plan admission, certificates, runtime services,
performance, migration, release, self-hosting or seed retirement is claimed.
Held Reader/authenticated/private builders/factories/pins remain unchanged and
uninvoked. Gravity/Uniform remains the destination implementation language.
