# Typed diagnostic carrier enrichment

## Scope and governing contracts

This scope was committed before implementation and early draft PR #1292.
D1, D2, D3, D8,
D9, L5, L6, C1, C7, C8, C15, BOOT8 and TEST2 govern this slice. Starting
source is `0247e8f9e5741bb768341c567e9e97fa312426e9`, tree
`c5059112bf2f18854b20ba63085a351b8445343d`. Published main
`e24b2da4be8b79960462b8f61b7134b71b17f30a` is already an ancestor. Earlier
Runtime or Diagnostics acceptance does not transfer to this candidate.

## Shared Diagnostics boundary

The existing payload-enrichment and failure-call body moves from the five-argument
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

## Executed development evidence

Original baseline execution emits three real typed artifacts and rejects the
two actual type/effect fixtures through `typed-diagnostic!`. Five complete
source observations and three native callback observations are equal before
and after extraction. The receipt SHA256 values are
`1e156c5b3f2bbed18930e7258aaabaaacc9342d5ea4bbad127559b76835c13d5`
and `aa0c63678fc11ae92eb065939ede57dc2c0eaf94eb3119b7e305e6df920b7520`.
Implementation hashes are recorded separately and differ intentionally. The
typed artifacts are emitted with actual source path/text/hash and a development
observation digest; that digest is not a canonical typed-core or C15 identity.

The new complete carrier namespace passes seven tests and 101 assertions. It
checks producer-owned stage/rule contracts, physical span/remediation/fact
identity, caller overrides, failure-root selection before node/data lookup,
live four-to-five-argument compatibility dispatch, exact host exceptions and
real emitted artifacts/refusals. Source golden checks compare complete semantic
payloads independently of display wording; the external before/after receipts
also retain exact current messages and remediation values.

Independent scope review requested explicit callback result/exception transport
metadata and compatibility-path sentinel exceptions. Both were added within
the existing scope. Both compatibility arities preserve exact exception
identity; the final source capture and exact CI command include those changes.

The three affected native tests in `gravity.diagnostics-test` pass with 100
assertions (baseline 98). Types/Effects remain 20 tests and 263 assertions.
Cold Diagnostics loads 20 APIs without Bootstrap. The exact new 2g/180-second
CI command passes, and the CI harness passes 54 tests and 657 assertions.
Language/ownership checks pass 48 tests and 1202 assertions.

The first new ordering test supplied vectors in a lazy entry sequence; native
map merge correctly rejected those values. The test now uses actual MapEntry
values, with no production change. A cold helper initially inspected its
captured argument list with associative lookup; correcting the helper to use
sequence indexing confirmed physical identities. Original nonpass logs and
helper snapshots remain history. Neither issue altered source admission or
compiler behavior.

Conservation receipt SHA256
`7342f02f67c5ece6c6e749d44829838caa301889d557d27d3b4a389ec77bffb9`
records all 19 previous Diagnostics body texts, unchanged four-argument
compatibility dispatch, the exact 21 earlier CI command forms, 14 protected
paths and 26 byte-preserved generated files archived outside the worktree.
Final supplement SHA256
`13f8d75607b19a9cca5a60662de3e17e16e0672517cdef4577973c9467241359`
binds the final metadata/source capture and records 34 archived files in total;
all five source and three native observations still match.
The complete row-level namespace map now lists all 20 APIs and the new
carrier's inputs, outputs, diagnostic ownership and dependency nonclaims.
The new test namespace is formatted and the stage/new tests lint with zero
warnings. Document and roadmap structural validators pass.

Final immutable-commit review, current-main lint context, independent
semantic/adversarial/provider/CI acceptance and integration remain pending.
These local checks do not transfer earlier Runtime acceptance or confer
release, self-hosting or full Diagnostics/type/effect conformance.
