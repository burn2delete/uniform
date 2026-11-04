# Native Tooling artifact construction

## Boundary and contracts

D1/D2/D3/D6/D8/D9, T1-T13, C1/C15, BOOT8 and TEST2 govern this
increment. Its immutable predecessor is 7d4b7bb258fa85a6fcc3a6648ceb32bc4a8d1683,
tree bb74b99a49ac31f59d04c884b8cf0cd1985ac31f, retained in draft PR 920.
That unmerged source is not current main. Move only the artifact-construction
tails of the existing P13 experience and compiled-Tooling source callers into
gravity.tooling. Keep source reading, namespace/profile/safety checks, module
construction, compilation, execution and file IO in their existing callers.

The two added APIs are native-experience-artifact, taking late operations,
source path, native module and source overrides; and native-compiled-artifact,
taking late operations, source path/text, native module/plan and returned
output. They return the existing in-memory artifacts, records and identities
or the original callback/validation failure. Catalog, validation, coverage,
span and digest operations retain their original lookup and evaluation order.
Artifact validation must precede coverage and final experience identity.

The stage owns native artifact construction and propagates existing structured
diagnostics. It must not import Bootstrap, CLI, build or release. Callers
retain all input authentication, admission and publication. Existing forty
stage bodies remain unchanged. The owning tests are gravity.tooling.pipeline-test
and the existing Bootstrap P13 controls.

## Evidence and acceptance

The clean predecessor's complete native records are retained at raw SHA-256
1975a1be71b5e88c14548cab32ba02ef119046548d345760d5ef8ac1470ed140.
They include real paired .gravity/.qst experience artifacts and compiled Core
programs with source/plan/report identities, thirteen physical source refusals,
177 validation cases, 92 overrides and 552 diagnostic payloads. Both source
spellings compile and execute the existing Core program with its actual
stdout. The original and candidate records must remain byte/parsed identical.

Acceptance also requires exact preserved caller preludes, construction-tail
and wrapper conservation, forty unchanged prior stage bodies, late operation
and digest selection, refusal before later construction, original exception
identity, physical positive/negative fixtures, language/ownership and lint
checks, contract validators, independent adversarial review, own CI/artifact
audit and exact clean current-main integration preflight.

The working extraction passes 26 owning tests / 468 assertions. Both real
source spellings match the existing callers and recomputed identities.
Twenty-six physical refusal cases stop before the new construction handoffs.
Adversarial cases preserve validation-before-coverage/final-identity order,
late final identity lookup, ignored callback return behavior, report failure,
digest-before-serialization lookup and original exception identity. Pinned
stage/test lint reports zero errors or warnings. Exact source checks preserve
both constructor tails, both caller preludes, forty prior stage bodies and
every surrounding source byte. The extraction helper's initial unmatched
parenthesis stopped before mutation; its source and nonpass log are retained.
Clean committed capture, remaining integration checks, independent acceptance
and own exact-current-main CI/preflight remain outstanding.

The existing Bootstrap P13 controls also pass 3 / 156; language/ownership
gates pass 48 / 1190 with zero skipped namespaces. Document, roadmap and
workstream validators pass. These remain ordinary development observations
and confer no admission or retirement authority.

## Unsupported boundaries

The complete/proof/task labels and capability, review, redaction and validation
fields remain native compatibility data. Artifact construction does not
authenticate them or implement any canonical T1-T13 tool service. Original
truthiness, vacuity and ignored callback return values remain compatible.

No source/Core authentication, grant, proof/certificate/provider trust, native
target execution, performance, service publication, authoritative migration,
release, self-hosting or seed retirement is established. Held routes remain
untouched. PRs 892, 918 and 920 retain their separate evidence and scope, with
no duplicate integration credit. No Seedless, Simplify, shared CI or main
writer is part of this slice.
