# Native Stage0 target-lowering metadata checks

## Scope and governing contracts

Move two complete C14 bodies to the existing `gravity.lowering` namespace:
`stage0-compiled-target-proof-present?` from foundation component 00529 and
`validate-stage0-compiled-target-lowering!` from backend component 00530.
Retain their original Bootstrap signatures as thin late-Var adapters. The
mixed C1/C11/C14/C18 aggregate remains Bootstrap orchestration; pass contracts,
MIR target independence and verification-risk policy keep their existing owners.

D0, D1, D2, D3, D6, D8, D9, C1, C14, C15, C18, BOOT8 and TEST2 govern
this increment. Root read the compiler phase README and C14/C18 directly.
Independent placement review SHA256
`5e5e35c296313d023477123c6007eaeeec7d5cb29e557e1c0f3430af8d60f66e`
recommends this exact two-body family and identifies the missing nonempty
accepted compiled-gate fixture as an evidence obligation.

The frozen source parent is `8d71224182ab85def45e6f3e73a16e66a05c5ab7`,
tree `b66d24284b9343f178f68ac0cf36cc110d924389`, with source freeze SHA256
`3cc1179f39ad826a6f3c64ddfda8c6e732319501640b041fd6b7833823b42afb`.
Its own CI audit remains pending and no acceptance transfers. This worktree
composes Main `2699ec09ef22a79576ec8c812ff6da0d7a08b6ea` as commit
`e07650061bf56f9bf35cf492d59d29b725a5f80a`, tree
`4fae390485401e27e20b7f659f19dd7490191094`. The three incoming Seedless
tooling files do not overlap this nine-path Source claim; the nine publication
SDK records are unchanged. No CI/workflow change is planned.

## Public API and artifact boundary

The intended additions are:

```clojure
(native-stage0-target-proof-present? present-var record)
(validate-native-stage0-target-lowering! operations module lowering)
```

The first operation receives the existing presence predicate Var. The second
receives explicit late `:fail!` and `:proof-present?` owner Vars. Inputs are
caller-owned module and target-lowering metadata, including source path,
profile, backend/target, input artifact id/kind, verification flag, target
metadata and proof/certificate presence markers. Outputs preserve the original
truthy presence result, nil, returning failure result or original exception.

These native checks create no target artifact or authority token. The enclosing
ordinary public macro/source-text route must emit a real Stage0 executable
plan/report and execute accepted source. Invalid input and missing proof
metadata must refuse real source before plan emission. Caller-supplied
`:verified?` and proof labels do not authenticate MIR, domain IR or proofs.

## Ownership and dependency direction

Lowering owns the two algorithms and decision facts for C14-INPUT and
C14-PROOF-METADATA. Bootstrap retains generic diagnostic construction,
throw transport, suite selection, mixed-stage orchestration, artifact identity
and publication. Preserve available source, input id, profile/backend, subject,
missing-field and remediation facts without inventing authentication data.

Dependency direction is Bootstrap adapters to Lowering to its existing Clojure
dependencies. Lowering already depends on `clojure.set`; preserve that import.
Do not add Bootstrap, Reader, Diagnostics, backend or host broker imports.

Preserve exact-true verification, metadata sequence semantics, proof-field
short circuit and original truthy return, backend fallback, opaque metadata
identity, lookup order/counts, callback selection before argument evaluation,
late rebinding, failure continuation and unchanged exception identity. Preserve
all 23 prior Lowering APIs and both legacy Bootstrap arglists.

## Tests and acceptance

Extend the existing whole `gravity.lowering.instructions-test` command with
focused native and genuine source controls. Update the four exact Lowering
public-API inventories for two additions; inspect broad test routes before
executing them. Preserve existing tests and CI command definitions.

Capture full before/after public-source observations for paired extensions,
nonempty accepted target-lowering metadata, both real C14 refusal fixtures,
real executable plans/reports, deterministic identities and source/failure
facts. The accepted metadata case must exercise proof presence, not merely an
absent suite. Keep C1/C11/C18 aggregate refusal behavior as affected regression
evidence without transferring those decisions to Lowering.

Run proportionate native regressions, language/ownership boundaries, cold
loading, changed-file lint, document validators and current-Main comparison.
Independent semantic/adversarial reviews must bind the clean source. Open an
early draft PR, maintain it and audit its own hosted CI/provider tuple.
Integration requires independent acceptance at the exact current base.

## Status and unsupported boundaries

Draft PR #1368 was opened at scope commit
`52f7b6c5191c19f213306f9385f559b7235a3e08`, tree
`42315115a5f952bbe500a5bc691f80ed6d5576be`, before moving source bodies.
Both bodies now live in Lowering behind the original two Bootstrap adapters.
All 23 prior Lowering APIs remain; four existing inventories now expect 25.
Original/moved body and adapter receipt SHA256 is
`2f8b81520aa9534982c3eb167ca6b187867303f179ecd881e3632d662ce881f5`.
Its test snapshots precede the added controls and record inventory-only edits.

Sixteen complete public-source observations match before and after extraction:
four genuine executable plans and compiler/runtime reports, four interpreted
and compiled execution agreements, and twelve pre-emission refusals. Both
source extensions cover the absent suite, nonempty accepted metadata exercising
both input kinds and all five proof-marker fields, and existing C1/C11/C14/C18
refusal fixtures. Full source/module/plan/report products, helper arguments,
results/classes, failure payloads and deterministic identities are retained.
Baseline SHA256 is
`a76fa8a918973560274bda3f2d0b691c14bc5d5520d3c0a90047e03d77eba589`;
implementation capture SHA256 is
`5d96c33a596f4a98bacda2e01997accbe0f581c5d17022e279f47c6575a0947c`.
Both bind actual scope HEAD; the implementation capture records dirty producer
hashes, not a future clean commit. Text is caller supplied under paired paths,
with no file-authentication or verified-IR/proof authority claim. Adjacent MIR
diagnostics retain their original top-level source span. The existing rejected
fixture supplies its own span label; it is preserved ahead of the paired
module-path fallback.

Initial baseline preparation used an unreadable numeric keyword suffix and
failed before source execution; the source fixture now uses `input-N` names.
A later baseline assertion incorrectly required the neighboring MIR diagnostic's
explicit fixture span label to equal the paired module path. Its log remains
history; the corrected capture compares full unchanged payloads and genuine
module paths, and still requires exact C14 diagnostic spans. The earlier prose
describing a span-shape difference was incorrect; MIR already emitted that
top-level field. An extraction helper spacing
assertion stopped before source writes; its original log is preserved.

Plan-assembly/native-backend regressions pass 25 tests/281 assertions. The
single native pipeline inventory passes 1/85 without forcing its delayed
private source factory. Language/ownership gates pass 48/1202. Cold loading
checks all 25 API declarations/arglists and only the existing `clojure.set`
alias, with no Bootstrap or Diagnostics loading.

The final focused instruction suite passes 21 tests/302 assertions, including
eight new C14 tests alongside all 13 prior tests. The added returning-refusal
control confirms that the later proof check observes its newly rebound Var and
receives the identical original record. Changed Lowering/test files lint with
zero errors/warnings. Canonical document validation passes 240 documents and
19 indexes with ASCII/no filler checks. The roadmap structural check passes;
its dated coverage figures are explicitly non-authoritative. Full current-Main
lint and clean source reviews/publication remain pending.

This increment does not implement authenticated verified-IR admission, proof
verification, backend emission, target/ABI/provider legality, canonical C14
conformance, complete source maps, release, migration, self-hosting or seed
retirement. It does not invoke held Reader/authentication/private source-factory
or pin routes. Clojure remains the temporary seed. Manual Source has no
Main/Ready/controller/service/recovery or GraphQL role; integration remains
with the designated integrator.
