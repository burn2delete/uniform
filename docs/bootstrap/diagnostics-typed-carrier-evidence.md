# Typed diagnostic carrier enrichment

## Scope and governing contracts

This scope precedes implementation and its early draft PR. D1, D2, D3, D8,
D9, L5, L6, C1, C7, C8, C15, BOOT8 and TEST2 govern this slice. Starting
source is `0247e8f9e5741bb768341c567e9e97fa312426e9`, tree
`c5059112bf2f18854b20ba63085a351b8445343d`. Published main
`e24b2da4be8b79960462b8f61b7134b71b17f30a` is already an ancestor. Earlier
Runtime or Diagnostics acceptance does not transfer to this candidate.

## Proposed shared Diagnostics boundary

Move the existing payload-enrichment and failure-call body from the five-argument
Bootstrap `typed-diagnostic!` into:

```clojure
gravity.diagnostics/fail-at-node!
[fail-var analyzer-stage id message node remediation data]
```

The stage selects the caller-owned failure Var before evaluating the payload.
It merges the actual node source span, caller-selected analyzer stage and
remediation with caller data in the existing order. Caller data retains its
override precedence, including explicit nil/false values. The failure operation
receives the original id, message and enriched payload, and its exact return
value or exception propagates unchanged. No rule id is added or remapped.

Bootstrap retains the existing four-argument delegation through its live
five-argument compatibility Var, the `:typed-effected-core` stage label and
the actual `fail!` Var. Keeping that delegation in Bootstrap preserves existing
interposition without adding a redundant default-data arity to the stage API.
Types, effects and all other producers retain rule selection, semantic facts,
profile/effect/capability/ownership/safety decisions and admission.

Inputs are the trusted failure Var, producer-selected stage/id/message,
existing node span, remediation and optional caller facts. Output is the
failure operation's result, normally the existing Stage0 ExceptionInfo carrier.
No typed artifact, source span, generated origin, source hash or deterministic
identity is rewritten. This helper creates no canonical C15 diagnostic identity.
Dependencies remain core/string to Diagnostics to Bootstrap and C15 consumers;
Diagnostics must not require a producing stage, Bootstrap, CLI or release.

## Evidence and CI obligations

Capture original complete typed artifacts and source refusals before extraction.
Use the existing ordinary `typed-source-artifact`/`typed-file-artifact` route:
current host form records, macro expansion, core lowering and native typed
checks. This route does not invoke the held authenticated C2/C7/C8 composition
or a private source factory. Emit actual typed artifacts as EDN with source
path/text/hash and implementation context alongside them. Preserve complete
before/after artifact and diagnostic data, not just message/id equality.

Use the accepted typed-core fixture and paired real hello sources, plus the
existing rejected type-mismatch and undeclared-effect fixtures. Native controls
must preserve callee-before-node selection, source-span object identity,
default-field/data merge order, nil/false overrides, both compatibility arities,
return and exception identity, late failure roots, and host errors for unusual
node/data carriers. All 19 previous Diagnostics body texts remain unchanged.

Add one whole-namespace command for `gravity.diagnostics.carrier-test` under
the existing 2g/180-second host limits. Keep the 21 existing command vectors
unchanged. The new namespace contains only native carrier and ordinary Stage0
source tests; it does not execute the separate held private C15 binding tests
in `gravity.diagnostics-test`. Update that existing namespace's native contract
expectations for the added API and run its affected native tests explicitly.
No workflow or admission-policy edit is included.

Require focused Types/Effects regressions, cold Diagnostics load without
Bootstrap, language/ownership checks, document/roadmap validation, CI harness
and complete current-main lint comparison. Bind exact source/base/tree, owned
paths, conservation, independent semantic/adversarial review, provider and CI
evidence. Finish with a clean commit and maintained draft PR. Integration and
current-main preflight remain with the designated integrator.

## Unsupported boundaries

This is temporary trusted Stage0 carrier enrichment, not a new canonical C15
schema, authenticated diagnostic identity, rule catalog, redaction/publication
policy, renderer, source/plan admission or capability grant. It does not establish
full type/effect legality, canonical typed-core/C15 conformance, target execution,
release, self-hosting, migration or seed retirement. The producer's stage label
does not authenticate its data. Held Reader/C2 routes, private source factories,
backend source builders, pins and admission boundaries remain unchanged and
uninvoked. Prior acceptance and source metadata labels confer no new authority.
