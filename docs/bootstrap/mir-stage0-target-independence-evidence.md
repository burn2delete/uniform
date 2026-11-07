# Native Stage0 MIR target-independence check

## Scope and governing contracts

Move the complete `validate-stage0-compiled-mir!` body from foundation
component 00529 to the existing `gravity.mir` namespace. Preserve the original
three-argument Bootstrap adapter and the mixed C1/C11/C14/C18 orchestration in
00531. The stage owns this C11 family decision; Bootstrap retains generic
diagnostic construction, transport, suite selection and pass ordering.

D0, D1, D2, D3, D6, D8, D9, C1, C11, C15, C18, BOOT8 and TEST2 govern
the increment. Root read C11 and its neighboring compiler contracts directly.
Independent placement review SHA256
`f51cc30e5101ad93a32082e6dd8ca10657b4599f2ea115e2b8489dfdf67d3d50`
recommends direct MIR ownership and identifies the missing accepted nonempty
compiled-gate source control. Existing canonical MIR fixture coverage cannot
substitute for exercising this particular metadata gate.

The frozen parent is `991e89b2aa6363acc7387b55f89b47090163850e`, tree
`b737aa4cd54bac75f1ccb61aefb99af3e2f47160`, source freeze SHA256
`65cac1da93f9ab2f8d40c6a69f7336bd8a68f8dcbce64dec5f41cb38ca881e01`.
Its own CI audit remains pending; no acceptance transfers. This worktree
composes Main `21f42b7d92d163be9ede20376c32808ec3b90efd` as commit
`80206f131a9f07d7fb4135bab46f1652adc20f59`, tree
`0ffd58683ca7867e2e2efc5cfca16e24e42504a6`. Main adds only CI selector
documentation/control tests, with no overlap in the six owned paths and no
publication SDK changes. Root read the full incoming diff. Loading the CI
control-test script still invokes its default suite; no such load is planned.
Independent Main context review SHA256 is
`2bdd43f4a809f961a98a51f7c6d593bfeb938dea758fbf2c35eee7f8b58f9f46`.

The six-path claim covers MIR, 00529, the existing MIR pipeline tests, this
record, the namespace map and a narrow correction to the preceding C14 evidence
prose. Its MIR refusal already has a top-level source span: the old failing
assertion wrongly required the explicit fixture label to equal the paired
module path. Correct that description while preserving captured payloads.

## Public API, inputs and outputs

The intended addition is:

```clojure
(validate-native-stage0-mir-operation! failure-var module _manifest mir-operation)
```

Inputs are caller-owned module/manifest/operation values and the existing late
five-argument MIR failure Var. The manifest is intentionally unused, matching
the old body. A non-target-specific family returns nil without reading module
fields or resolving the failure Var. A target-specific family passes the same
C11-TARGET-LEAK id, source path, native module context, original operation and
missing-field/remediation map to the selected failure operation. Return values
and exact thrown objects remain unchanged.

The operation creates no MIR module, certificate or target artifact. The
enclosing ordinary public macro/source-text route must emit and execute a real
Stage0 instruction plan/report for a nonempty accepted compiler gate, and
reject genuine target-specific source metadata before instruction lowering.
Its report must retain the existing lack of verified MIR emission. A supplied
family label is metadata, not evidence that an operation otherwise satisfies
the MIR schema or verifier.

## Ownership, diagnostics and dependencies

MIR owns the family comparison and C11-TARGET-LEAK decision facts. Bootstrap's
`mir-fail!` retains diagnostic message, source-span selection, context/fact
projection, remediation defaults and throwing/publication. In particular,
explicit subject spans precede nested spans and the source-path fallback.
Preserve available fields without inventing missing origins or authentication.

Dependencies remain Bootstrap to MIR to Clojure core, with no new imports.
Keep every prior MIR API and legacy Bootstrap arglist. Preserve the single
family lookup, branch laziness, failure selection before argument lookup,
source/profile/target lookup order, unused manifest, original operation and
opaque field identity, callback rebinding and exact return/exception behavior.

## Tests and acceptance

Extend the existing `gravity.mir.pipeline-test` whole-namespace CI command.
Locally select the new native/ordinary Stage0 tests and existing pure API
inventory explicitly; do not force the delayed private source factories in
other MIR tests. Inspect selected test bodies and namespace initialization.
No new CI command or workflow edit is planned.

Add meaningful generic/target-specific native controls, no-touch accepted
inputs, late callee selection, opaque lookup/record identity, returning and
throwing failure ports, legacy adapter interposition and explicit span versus
fallback diagnostics. Add durable ordinary source text with nonempty generic
MIR metadata for both extensions. Retain the existing target-leak fixture as
the real refusal; preserve C1/C14/C18 neighbors as affected regressions.
Compare full before/after source/module/plan/report products, callbacks,
diagnostic payloads, provenance and deterministic identities.

Run proportionate focused checks, cold API loading, language/ownership gates,
changed-file lint, document validators and full current-Main comparison.
Independent semantic/adversarial acceptance must bind the clean source; open
an early draft PR and maintain it through its own hosted CI/provider audit.
Integration requires independent acceptance at the exact current base.

## Status and unsupported boundaries

Draft PR #1369 was opened at scope commit
`2d4e8e5f2f29de8772e9f1773bb43339363b266d`, tree
`3130821f2801e68b2afda02e41d90bf9c78327a6`, before moving the body.
That two-document scope commit also corrects the preceding C14 span wording;
its code and captured diagnostic payloads are unchanged.

The complete predicate now lives in MIR behind the original Bootstrap adapter.
All 50 previous public APIs remain, with one addition and no dependency or
orchestration changes. Original/moved-body and adapter receipt SHA256 is
`f742277e70b51d9dc183c9b44f3af280ed3bfda03d8859182cf6e9814a846854`.
Its test snapshots record the initial inventory-only edit before new controls.

Twenty complete before/after public-source observations match: four genuine
instruction plans and compiler/runtime reports, four interpreted/compiled
execution agreements, and sixteen pre-emission refusals. Paired spellings cover
the absent suite, nonempty generic MIR metadata, the six existing compiler
refusal fixtures and two additional target-leak cases for fallback and nested
spans. Full macro/module/plan/report products, helper arguments and return/error
classes, exact diagnostics, provenance and deterministic identities survive.
Baseline SHA256 is
`c5196d2a2c994a0dc91372415f52d30e65ef9a09c1d775f086d90ea01f2a10be`;
implementation capture SHA256 is
`5f65139650eb9d52eff8b06b89f1c99730cf65efad3085f8bf896e651efd72b0`.
Both record actual scope HEAD; the latter binds dirty producer hashes, not a
future clean commit. Supplied paired text has no file-authentication claim.
The accepted report explicitly retains `:emitted? false` for MIR and requires
a later verified-MIR build; the emitted product here is the instruction plan.

Focused tests pass 8 tests/444 assertions: seven new native/ordinary Stage0
tests and the existing pure MIR API inventory. The exact selection is recorded;
eight named existing private-source-factory delays remain unforced before and
after that selection. This is a route-local observation, not proof of global
route absence. Full MIR pipeline/private-factory tests were not run locally.
Lowering instruction/plan regressions pass 34/478, language/ownership gates
48/1202, and cold loading validates all 51 API declarations with no aliases,
Bootstrap or Diagnostics imports. Changed MIR/test files lint with zero errors
or warnings. Canonical document validation passes 240 documents/19 indexes;
the roadmap structural check passes and its dated coverage figures remain
non-authoritative. Current-Main comparison, clean source review/publication
and own CI audit remain pending.

The slice establishes no full MIR verification, canonical diagnostic identity,
source/checked-Core authentication, profile/effect/ownership/safety legality,
runtime-check legality, general CFG support, target lowering, backend/runtime
authority, release, migration, self-hosting or seed retirement. Held Reader,
authentication, private source-factory and pin routes remain excluded.
Manual Source has no Main/Ready/controller/service/recovery or GraphQL role;
the designated integrator retains exact current-base integration.
