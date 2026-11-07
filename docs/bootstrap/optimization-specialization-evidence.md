# Native specialization record validation

## Scope and governing contracts

Move the complete PERF3 family into existing `gravity.optimization`: three
policy values and seven functions currently split between foundation/00141
and 00142. Keep the original Bootstrap names/arities as aliases and live
adapters. Preserve the PERF2 source assembler preceding these forms and the
entire tail beginning with `specialization-source-artifact`, including its
following PERF4 policy/helper definitions. This draft opens before production
changes.

D0, D1, D2, D3, D6, D8, D9, C1, C11, C12, C13, C15, SAFE15,
PERF1, PERF3, PERF10, BOOT8 and TEST2 govern this temporary seed extraction.
PERF3 depends on L5/L6/L12, P1/P13 and SAFE15 facts. Native labels and supplied
records do not confer those contracts' semantic or proof authority.

## Public API, inputs and outputs

Add `native-specialization-required-record-fields`,
`native-specialization-required-nonempty-record-fields` and
`native-specialization-required-artifacts`, plus:

```clojure
(native-specialization-normalize-record record)
(native-specialization-missing-record-fields
  present-var required-fields-var nonempty-fields-var record)
(native-specialization-failure!
  failure-var id source-path manifest performance-claim record extra)
(native-specialization-missing-key-inputs record)
(native-specialization-required-cache-inputs record)
(validate-native-specialization-record!
  missing-fields-var missing-key-inputs-var required-cache-inputs-var
  present-var failure-var required-artifacts-var
  source-path manifest performance-claim record)
(native-specialization-capability-proof
  required-cache-inputs-var present-var manifest performance-claim records)
```

Inputs are caller-owned native manifests, performance claims, specialization
records and explicit late Vars. Policy APIs return their native vector/sets.
Normalization returns a record with original alias fallbacks; field/key scans
return ordered missing vectors; cache policy returns a required-input set.
Failure construction returns the supplied owner's result or exception.
Validation retains `:complete` if all invoked operations return, including
returning failure callbacks; any uncaught operation exception propagates.
The capability helper returns the original boolean envelope and relative
`:status :complete`. None of these outputs authenticates or admits an input.

Optimization owns PERF3 rule selection, messages, remediation and facts.
The supplied existing failure owner retains generic diagnostic construction,
publication and exception transport. Preserve source span/path, profile/target,
source function, key/guard, build effects, variant/proof ids, diagnostic family
and extra-fact override precedence. Preserve all original late policy/helper
Var selection and map/set evaluation behavior without AST reprinting.

Dependency direction remains Bootstrap -> Optimization -> Clojure core and
unaliased `clojure.set`. No reverse Bootstrap, Diagnostics, backend, reader,
CLI or release import is introduced. Source/profile/PERF1 composition,
record iteration, artifact/hash/provenance assembly and CLI/file wiring stay
with their existing owners. No new namespace, CI row or schema is needed.

## Real source evidence and verification

Capture the existing `specialization-partial-eval.gravity` fixture and all
nine rejected specialization fixtures through the ordinary source-artifact
route, paired with `.qst` paths. Preserve full macro/Core/typed/profile/module/
performance products, specialization outputs, original helper calls, diagnostic
facts and upstream hash links. Identity evidence is scoped to the same pinned
runtime, source, policy and operation sequence. Do not invent an artifact-id
field for a native output without one. Compiled Stage0 plans do not invoke
PERF3 and receive no evidence credit for this family.

Use the existing Bootstrap-free C13 leaf for policy, API/arglist, late-port,
ordered refusal, returning-failure, opaque exception and proof-envelope tests.
Use exact existing/new Bootstrap test Vars for genuine accepted/rejected
source artifacts and adapter interposition. Keep the private C15 source delay
unrealized and the SH17 carrier test namespace unloaded. The Optimization
pipeline test receives only its public inventory update locally.

Run proportionate plan regressions, cold loading, lint, language/ownership,
document/roadmap validation and a full changed-file Main comparison. Maintain
the early draft through a clean verified commit, independent source reviews,
source freeze and its own provider CI audit. Source acceptance, CI observations
and current-base integration are separate; prior results do not transfer.

## Unsupported boundaries

This family does not specialize MIR, execute partial evaluation or guards,
emit machine variants, persist/reuse a cache, validate a proof provider or
certificate, measure benchmarks, remove checks or execute a target/backend.
Presence of a key, source map, replay marker or proof id does not authenticate
its content. False and empty-string markers retain historical native presence
semantics. A partial record with no explicit false hermetic flag can pass the
native rule when its replay marker is present; that is not hermetic execution.

Record effect/capability subset relationships are reported by the capability
envelope rather than independently refused by this validator. Its
`:status :complete` can coexist with false preservation fields or vacuous empty
input. Native metadata does not become L6 authority or SAFE15 proof evidence.
The existing sequential diagnostic precedence is preserved; no new priority
or cross-runtime/cache identity guarantee is introduced.

Behavior keys, grants, replay records, profile legality and proof references
are semantic facts, not project accounting. No quota, work allowance or debit
mechanism is added. No held Reader/authentication/private source-factory/pin
route, source admission, release, migration, self-hosting or seed retirement
is part of local evidence.
