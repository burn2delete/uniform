# Stage0 Effects C8 contract regression correction

## Problem and governing contracts

The Effects extraction added four Stage0 APIs to the existing thirty public
Vars, but an older C8 leaf test still asserts a legacy-only inventory, one
input boundary for every checking API, and the previous consumer list.
Correct that test contract while preserving every earlier API assertion.
No producer implementation or public API change is indicated.

D1, D2, D3, D9, L6, L15, C8, C15, BOOT8 and TEST2 govern the correction.
The existing [Effects policy boundary](effects-stage0-policy-evidence.md)
remains the implementation contract. This is regression coverage for that
boundary, not a new language capability or canonical C8 conformance claim.

## Failed candidate and exact new source base

The failed candidate remains source
`9a0e5f4cbbc63564abc43ca740c71e709822ee60`, tree
`7635003a1d63d9384dacd032023f0ab93d86d62b`, provider
`e32f97635ba4ca94e3cc6e7c02f90b836214811c`, tested Main
`4d0ab35a4d085eea3de3c8a2af0372578b62fdec`, CI `42356394110/1`.
Its audit is terminal failure/rejected: three portable C8 assertions fail,
and a separate resolution job encounters Maven Central HTTP 429 before its
test. The new Effects command passes 13 tests and 137 assertions, but the
whole candidate is not accepted. No failed job is rerun or reclassified.

Independent failure triage SHA256
`a34e94fcbe26c6ba9f037da8c1176bcde91c1d4a177d9cefe0b745400dda28da`
identifies the three stale test assumptions and recommends retaining all
thirty prior API checks plus explicit checks for the four new APIs.
The documented owned leaf runner reproduces exactly 23 tests, 577 passes,
three failures and zero errors; log SHA256
`391c28d451a288aa9ce560b7381fe69dd301bb66f19ad239b916f60d9f77d323`.

The isolated corrective source starts at normal own-branch composition
`fb49ab8d8c0c8c64fec37f3ff2bc2f15d2a5fc06`, tree
`aad67714ef10c6ac9308a539f8cee9c341bdb975`, with ordered parents
`[9a0e5f4cbbc63564abc43ca740c71e709822ee60,
13304e2a6ddc80fa0df2994071cf2479f042f16a]`. The two CI files combine the
published Main selector additions with all twenty-eight existing source CI
vectors. They are combined files, not copies of Main's complete file blobs.
Canonical merge-tree equality, exact published insertions, and conservation
of every other path mode/blob are checked before source authoring.

The original three-path corrective lease owns only the C8 test, this evidence
document and the existing Effects evidence document. The Parser work remains
separate and cannot gain integration credit around this failure. No old
lease, frozen source, rejected run, or authority record is reset.

## Public contracts and intended correction

The original thirty API names remain an exact explicit legacy set. The four
additional names are exactly:

- `stage0-uses-println?`
- `infer-stage0-effects`
- `required-stage0-capabilities`
- `validate-stage0-module-effects!`

Assert the complete public/contract inventory equals the union of these two
explicit families. Keep the existing legacy arity, Var metadata, admission,
data/checks classification, native-premise input-boundary, non-authentication,
non-proof and non-grant checks. Do not substitute a count or subset assertion.

For each new API, assert its exact argument list, operation inputs,
`:stage0-native-effect-policy-kernel` boundary, input carrier, callback-value
selection, and data/checks flags. The first two consume caller-owned Stage0
forms, the capability projection consumes effect/catalog data, and the guard
consumes an expanded Stage0 module. The guard retains supplied failure
transport. All four remain non-admitting, non-authenticating and
non-authorizing.

Retain the exact prior consumer order and append only
`gravity.module-analysis.effects`. Effects continues to require only
`clojure.set`; requiring the C8 leaf must still leave Bootstrap unloaded.
The dependency direction is Bootstrap/module-analysis to Effects, never
Effects to Bootstrap. Diagnostic ownership, callback-value semantics,
provenance and deterministic identities do not change in this correction.

## Verification and evidence limits

Run the complete documented C8 leaf and the existing Stage0 Effects namespace
in separate processes. The latter emits and executes actual source plans and
rejects genuine modules, distinguishing earlier P4 source controls from
direct L6/L3 Effect-stage refusals. Compare the producer blobs and protected
source/plan/report identities, retain all earlier CI command vectors, run the
boundary/ownership and structural docs/roadmap checks, and test the imported
CI-selection controls. No new CI command or workflow change is required.

Independent source/adversarial and Main-composition reviews must bind the clean
corrective commit/tree. Its own provider and hosted run must pass the exact
current-base gates; the old failed run and source reviews cannot supply that
acceptance. Maven HTTP 429 remains separate infrastructure evidence if it
recurs. Do not change dependency versions, retry the rejected run, or weaken
selection/coverage to avoid it.

The failed-run report SHA256 is
`e27d515ffe8780825a0edfb142eebc5a019b2a7b1672068bead30d06d3820bbd`;
summary SHA256 is
`87ed6be4b499124d0a0f8289775437b6f1e99f38a9c7461c7de917163fd6b18a`.
All original sealed files remain unchanged. The immutable audit supplement
corrects only the copied Main-import metadata; it does not alter rejection.

## Current status and nonclaims

The test correction is implemented. The complete C8 leaf passes 23 tests and
579 assertions, and the separate real-source Effects namespace passes 13
tests and 137 assertions. The new guard's exact metadata-map assertion
replaces its earlier pair of generic checks; all thirty legacy API
assertions remain. No producer code changed. Boundary/ownership checks pass
48 tests and 1,202 assertions; the CI harness, including the imported selector
controls, passes 56 tests and 963 assertions. Structural docs and roadmap
checks pass. Fourteen complete real-source observations also match the
original baseline, using identical source-path/text inputs: eight plans emit,
six execute, and plans, reports, IDs, stdout, facts and refusals are unchanged.
The two direct Effect-stage refusals and earlier P4 controls remain distinct.
Current observation receipt SHA256 is
`ca0a157a6979cd1af2a53e13b4376f570c498ddaba89095b8d2be89f344eab57`.
It records composition HEAD `fb49ab8d8c0c8c64fec37f3ff2bc2f15d2a5fc06`
and actual producer hashes; it does not claim a later authored commit identity.
Complete current-Main lint, independent exact-commit review and
fresh provider/CI evidence remain pending; these local
results do not accept the failed predecessor or confer integration authority.

Published Main `13304e2a6ddc80fa0df2994071cf2479f042f16a`
is bound by SDK manifest SHA256
`0c9ba16b9ee0c8217f5eb02b2f2477714371708f9c08eeef128970c37758190f`.
All nine Simplify SDK files are unchanged from the prior published Main.
This code-context fact grants no new role or source/CI authority.

The original Stage0 Effects unsupported boundaries remain intact: no complete
or canonical effect inference, typed Core/MIR admission, capability proof or
provider grant, source authentication, C15 publication, runtime network
service, backend, performance, release, migration, self-hosting or seed
retirement authority. Held Reader/authenticated/private builders/factories
and pins remain unchanged and uninvoked. Main integration remains with the
designated integrator after independent acceptance and exact current-base
preflight. This manual Source has no Main, Ready, controller, service or
recovery role.
