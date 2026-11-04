# Native Tooling catalog ownership

## Boundary and contracts

D1/D2/D3/D6/D8/D9, T1-T13, C1/C15, BOOT8 and TEST2 govern this
increment. Its immutable predecessor is 8e0ec29749605f38c12cba0a713efa31d9998a67,
tree 84c5c9af602190e78aad2fb55e9374755242ca56, retained in draft PR 921.
That unmerged source is not current main. Move the existing native P13
document, diagnostic, fixture, artifact-key and summary catalogs into
gravity.tooling. Keep compatibility Vars and their initialization order in
the existing Bootstrap fragment.

The stage will expose seven native catalog data Vars and two explicit-input
builders for the existing ordered diagnostic-id vector and override map.
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
and exact clean current-main integration preflight. Implementation and all
candidate evidence remain outstanding at this initial draft checkpoint.

## Unsupported boundaries

Native catalogs and complete/proof/task labels remain compatibility data.
Their diagnostic and contract associations do not authenticate canonical
T1-T13 services, source/Core, grants, proof certificates or providers.
No target execution, performance, service publication, authoritative migration,
release, self-hosting or seed retirement is established. Held routes remain
untouched. Predecessor PRs retain their separate evidence without duplicate
integration credit. No Seedless, Simplify, shared CI or main writer is part
of this slice.
