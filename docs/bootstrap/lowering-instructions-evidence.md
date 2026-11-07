# Stage0 instruction lowering

## Scope and governing contracts

This slice moves the existing temporary Stage0 expression, collection, let,
function and instruction-summary bodies from Bootstrap into `gravity.lowering`.
It starts at frozen source `6bd0265992ad294d05fbf6611b1cf2315a4e5c14`, tree
`ad45ae152ad95f7fb6e322f1389cac339d207e39`, containing published Main
`9f201518f400b118a7cf60533d6cebb94f5bcd68`.

D0, D1, D2, D3, D6, D8 and D9 govern the extraction. C1 governs explicit pass
boundaries and preservation, L2 and C6 govern evaluation order and lowering,
and C14 identifies the verified input and evidence obligations that this
temporary native instruction format does not satisfy. C15, BOOT1, BOOT8 and
TEST2 govern diagnostic compatibility, seed organization and evidence.

This is the existing Clojure-hosted instruction compiler. It does not turn
macro-expanded native data into authenticated Core, MIR or backend input.
Gravity/Uniform remains the destination implementation language.

## Boundary and dependency direction

The intended new APIs are `compile-native-expression`,
`compile-native-collection`, `compile-native-let`, `compile-native-function`
and `native-instruction-summary`. They consume caller-owned native module,
local scope, form or function data, or an instruction, plus explicit live
operation Vars where the existing implementation reads Bootstrap owners.
They produce the existing instruction maps, function binding/instruction map
or opcode-frequency map. They perform no source IO, authentication, execution,
target selection, provider selection or certificate construction.

Bootstrap keeps its original function names and arities as adapters. It retains
function-header parsing and validation, function-table formation, source and
module intake, all profile/effect/safety/package/tooling preconditions,
whole-plan assembly and plan identity, and instruction execution. The stage
does not import Bootstrap, CLI or release, discover owners with `resolve`, or
interpret supplied callbacks as proof authority.

The stage owns the existing expression unknown-symbol/unknown-form and
host-reflection decisions and let-vector/name checks. Bootstrap retains
diagnostic construction and publication, with the original `L2-UNKNOWN-SYMBOL`,
`L2-UNKNOWN-CORE-FORM`, `P4-HOST-REFLECTION` and `L2-LET-BINDING` identifiers,
payloads, message text, call order and exception behavior. Main-entry and
function-header refusals remain with the caller.

## Preservation and artifact evidence

The extraction must preserve all original branches, map/set sorting, lexical
scope progression, initializer-before-binding semantics, eager and lazy
realization points, callable selection, late recursive/helper/catalog/failure
Vars, source paths, metadata and opaque values. It adds no new lowering rules,
numeric semantics or admission conditions. No unrelated Runtime or plan
assembly body is in scope.

Before moving bodies, record complete actual source observations through
`macro-source-artifact`, its actual module and expanded forms, and
`stage0-compiled-core-plan`. Execute accepted plans with the existing Clojure
interpreter. Compare complete before/after plans, source digests, deterministic
plan IDs, stdout and real source refusals. Exercise both `.gravity` and `.qst`.
Do not replace Reader operations, bind authenticated products, invoke private
source factories or claim a request map alone is compilation.

Focused regression tests must cover the complete five-body group, positive
instruction shapes, genuine malformed/unsupported source, recursive operation
interposition, ordering, exception identity and the unchanged existing native
Lowering API. Add one whole-namespace native CI command and preserve every
existing command. Run the language-boundary gate and proportionate affected
tests, document/roadmap checks and lint. Independent source/adversarial review
and the exact provider/CI audit are required before integration eligibility.

## Current status and unsupported boundaries

This is the early draft scope. Implementation, source equivalence, local
checks, independent acceptance, provider evidence and CI are not yet complete.
The original ten-path writer lease governs implementation. This manually
authored external source has no Main, Ready, controller, service or recovery
role; final current-base preflight and integration remain with the designated
integrator.

Native instruction maps do not contain canonical node identities, complete
per-instruction spans, type/effect/ownership/safety proofs or verifier results.
Existing plan-level paths, source hashes and plan IDs are the preservation
boundary, not proof of full C1/C6/C14 conformance. This slice claims no canonical
Core or MIR admission, target emission, native execution, new unsafe support,
performance, migration, release, self-hosting or seed retirement. Held Reader,
authenticated source/Core builders, private factories and source pins remain
unchanged and uninvoked.
