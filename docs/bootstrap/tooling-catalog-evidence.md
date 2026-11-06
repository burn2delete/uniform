# Native Tooling catalog ownership

## Boundary and contracts

D1/D2/D3/D6/D8/D9, T1-T13, C1/C15, BOOT8 and TEST2 govern this
increment. Its immutable predecessor is 8e0ec29749605f38c12cba0a713efa31d9998a67,
tree 84c5c9af602190e78aad2fb55e9374755242ca56, retained in draft PR 921.
That unmerged source is not current main. Move the existing native P13
document, diagnostic, fixture, artifact-key and summary catalogs into
gravity.tooling. Keep compatibility Vars and their initialization order in
the existing Bootstrap fragment.

The stage exposes native-documents, native-governing-documents,
native-diagnostics-by-document, native-rejected-diagnostics,
native-rejected-fixture-names, native-artifact-keys and
native-document-summaries as catalog data Vars. Its native-diagnostic-ids
builder takes the diagnostic catalog and document order and returns the
existing ordered distinct diagnostic-id vector. native-override-diagnostics
takes diagnostic ids and returns the existing lower-case keyword override map.
Bootstrap retains its catalog Vars; derived definitions must consume their
current preceding Bootstrap values at the original initialization positions.
Existing runtime consumers continue to select those Vars through their late
operations. Catalog values, traversal order, case conversion, collection
types and diagnostic identities must remain unchanged.

The stage owns native catalog data and diagnostic associations. Callers retain
source intake, compilation, execution, authentication, admission and
publication. The stage must not import Bootstrap, CLI, build or release.
The forty-two existing stage function bodies remain unchanged. Owning tests
are gravity.tooling.pipeline-test and the existing Bootstrap P13 controls.

## Evidence and acceptance

The original, records and assembly captures are raw-byte and parsed-value
identical at SHA-256
1975a1be71b5e88c14548cab32ba02ef119046548d345760d5ef8ac1470ed140.
They include all nine catalog values, real paired .gravity/.qst artifacts,
compiled Core programs and their stdout, source/plan/report identities,
thirteen physical source refusals, 177 validation cases, 92 overrides and
552 diagnostic payloads. Require the same complete candidate capture.

Acceptance requires exact catalog-expression and surrounding-source
conservation, preserved compatibility initialization and late Var behavior,
forty-two unchanged prior stage bodies, independent positive/negative
catalog and actual artifact tests, language/ownership and lint checks,
contract validators, independent adversarial review, own CI/artifact audit
and exact clean current-main integration preflight. Clean committed
candidate capture, committed lint, independent acceptance, own CI and
integration evidence remain outstanding.

The working extraction passes 28 owning tests / 525 assertions. Tests retain
actual paired programs and physical refusals, diagnostic collection order,
duplicate removal and case-collision behavior, exact catalog alias identity,
and compatibility snapshots when the stage Var root changes. Pinned stage/test
lint reports zero errors or warnings. Exact source checks preserve all nine
catalog expressions, original compatibility initialization positions,
forty-two earlier stage functions and every surrounding source byte.
Language/ownership gates pass 48 / 1190 with zero skipped namespaces;
document, roadmap and workstream validators pass.
The existing Bootstrap P13 controls also pass 3 / 156.

The extraction helper first attempted to update a list as an associative
collection. It failed before repository mutation; the original helper and
nonpass log remain retained. The corrected helper constructs the same exact
definition form and compares all planned forms before writing any source.

## Unsupported boundaries

Native catalogs and complete/proof/task labels remain compatibility data.
Their diagnostic and contract associations do not authenticate canonical
T1-T13 services, source/Core, grants, proof certificates or providers.
No target execution, performance, service publication, authoritative migration,
release, self-hosting or seed retirement is established. Held routes remain
untouched. Predecessor PRs retain their separate evidence without duplicate
integration credit. No Seedless, Simplify, shared CI or main writer is part
of this slice.
