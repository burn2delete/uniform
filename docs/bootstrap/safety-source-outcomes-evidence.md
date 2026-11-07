# SAFE1 source-call validation and records

## Scope and governing contracts

This scope precedes implementation and its early draft PR. D1, D2, D3, D8,
D9, SAFE1, SAFE6, SAFE15, C1, C7, C8, C10, C15, BOOT8 and TEST2 govern the
slice. Starting source is `4277f6440102a138af73920e4463c6ca1304b011`, tree
`ec7d0f31e73ed58cd6d2df444d125608b28ca22b`. Published main
`e24b2da4be8b79960462b8f61b7134b71b17f30a` is already an ancestor. The
independent static boundary review SHA256 is
`4546f7b9ce21847789a89b50fad88ce614472b8acec9418d0a74c16f28af2bd3`.
Earlier source, provider or CI acceptance does not transfer to this candidate.

## Safety boundary and APIs

Move the complete SAFE1 typed-call validation/recording group from components
00086 and 00087 into `gravity.safety`:

```clojure
emit-native-outcome-diagnostic!
[typed-diagnostic-var id message node remediation data]

check-native-outcome!
[legal-outcomes-var diagnostic-var node operation outcome evidence]

record-native-outcome-call!
[checker ctx node operator args effects capabilities return-type spec operations]
```

The diagnostic helper owns the existing SAFE1 rule/profile/span enrichment,
selecting the supplied typed diagnostic Var before node/data lookup. The guard
owns only legal outcome-tag membership and nil proof-reference refusal, with
the original independent-guard order. The recorder owns all seven existing
branches: classification, runtime check, unsafe audit, generated provenance,
optimization erasure data, dependency mode and conformance data. It keeps
the original field-presence/policy decisions, rule IDs, messages, remediation,
base/branch record shapes and record-write order.

The recorder receives the original checker and dereferenceable context, not an
eager context snapshot. Each profile/target/safety lookup remains at its original
position, including the later unsafe-policy read after argument extraction.
This preserves nil-kind behavior, repeated context reads and callback changes.
It receives five trusted late Var ports: `:arg-value`, `:check-outcome!`,
`:diagnostic!`, `:record!` and `:legal-outcomes`. Each callee or data root is
selected at its original expression position. Generated/conformance branches
select their record sink before extracting payload arguments. The original
base call record, rather than the sink's result, remains the recorder return.

Bootstrap retains the existing compatibility names/arities, legal-outcome and
required-family catalogs, typed traversal, argument representation, checker
sink, summary fixture and source/file artifact projectors. The early profile
check and special generated-origin diagnostics remain with their current
06106 owner. Shared typed carrier construction remains Diagnostics-owned.
Safety requires no Bootstrap or Diagnostics import; callers supply the ports.

Inputs are trusted node/span/generated-origin data, operator/arguments,
profile/target/safety context, effects/capabilities/return type, call spec and
late operations. Outputs are the original base record or nil, seven checker
record families, or unchanged owner results/exceptions from diagnostics.
Preserve all source/node identity, ordering and payload object identity; add
no identity encoding and invalidate no semantic facts.

## Evidence obligations

Before extraction, capture complete ordinary Stage0 Safety artifacts from the
existing accepted `safety-outcomes.gravity` and paired real hello sources.
Use `safety-source-artifact`/`safety-file-artifact` through the original host
form reader, macro/core lowering and typed checker. Emit complete EDN records
with actual source path/text/hash and implementation context. A development
observation digest is not a canonical Safety or certificate identity.

The accepted Safety fixture exercises all four outcomes and all seven branches.
Use all nine existing `typed-safe1-*` refusal fixtures for no outcome, missing
proof, missing/illegal check, unsafe policy/metadata, generated provenance,
optimization proof and dependency mode. Seven exercise moved decisions; the
illegal-check and special generated-origin refusals retain their early owner.
Compare complete before/after source artifacts and diagnostic payloads.

Native controls must cover live legal/diagnostic/argument/sink Var selection,
the no-kind fast path, all seven branches, repeated context reads, late unsafe
policy, two nonthrowing diagnostic guards, sink result versus base return,
nil/false/default semantics, exact exceptions, and physical span/origin/fact
identity. Preserve all 12 earlier Safety bodies and all unrelated forms in
both components, including interop recording and the conformance summary.

Add one complete `gravity.safety.source-outcomes-test` CI command at the existing
2g/180-second host limits; preserve all 22 earlier command vectors. Update the
affected existing Safety contract test, run focused Types/Effects regressions,
language/ownership, cold loading, document/roadmap, CI harness and complete
current-main lint checks. Bind exact source/base/tree, owned paths, immutable
semantic/adversarial/provider/CI acceptance and final integration preflight.
The designated integrator retains integration authority.

## Unsupported boundaries

These are trusted temporary Stage0 source-record decisions and writes, not
canonical C10 outcome authority, a complete operation inventory, source/Core
admission or proof/provider authentication. Supplied evidence, proof-reference,
audit-status, review-policy, certificate, conformance and proof-preserved labels
remain unverified data. A non-nil reference is not proof verification.

No runtime check executes and no optimization erases a check here. This slice
does not establish unsafe approval, full SAFE1/SAFE6/SAFE15 or C10 conformance,
capability grants, package policy, MIR/backend preservation, performance,
release, migration, self-hosting or seed retirement. Existing C10/checked-core
verification remains the canonical owner. Held C2/Reader/authenticated routes,
private factories, backend builders, pins and admission boundaries remain
unchanged and uninvoked.
