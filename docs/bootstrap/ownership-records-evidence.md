# Native Ownership lifecycle and resource records

## Boundary and contracts

D1/D2/D3/D6/D8/D9, L10, SAFE2-SAFE5, C1/C7-C11/C15, BOOT8 and
TEST2 govern this increment. Its immutable predecessor is
30d017cdf0c5b1a746e518b0d8bba39fbdc97ba9, tree
b95b300b49af2498e2ce50358469e745bcb8a038, retained in draft PR926.
That unmerged dependency is not current main. Move only eight existing
native record bodies from the C9 lifecycles and resources helpers into
gravity.ownership. Keep helper names/arities, facade interposition,
validation, diagnostics, artifact assembly and source intake unchanged.

The added APIs are native-lifetime-interval-map, native-escape-analysis-report,
native-region-lifetime-graph, native-arena-generation-graph,
native-linear-resource-flow-graph, native-transfer-records,
native-runtime-check-records and native-unsafe-audit-references.
Each takes explicit native module data and returns its original record or
the original host lookup failure. Preserve all labels, collection types,
ordering, profile classification, unsafe/review metadata and lookup order.

The stage owns native record construction and propagates host failures.
Existing C9 owners retain rule decisions, validation and diagnostic identity.
Runtime profile classification is supplied native data, not authorization to
insert or execute a check. No verified IR fact is consumed or invalidated.
The stage imports only clojure.set; the C9 helpers consume it, and it must
not import the facade, Bootstrap, CLI or release. All eleven earlier stage
function bodies remain unchanged. Owning tests are
gravity.ownership.pipeline-test and the separate cold C9 regression.

## Original observations and acceptance

The clean predecessor capture has raw SHA-256
6f03674e460355f2fb9afef1a43ee18fffb695cd69cabe822ef337d2ad3f4f3d.
Eleven explicit native module/profile views emit the eight record families;
the existing native verifier/validator accepts four views and refuses seven
with C9-RUNTIME-CHECK. Six altered-record views produce their original
structural diagnostic refusals, and eight opaque-module lookup failures
retain their original data. Upstream complete/proof labels in these views
are explicitly supplied test data, not authentic C8 or certificates.

Separately, four actual paired .gravity/.qst normalized-transition fixture
plans and the current Gravity ownership engine compile with recomputed
identities. Actual execution accepts eight requests and rejects twenty-eight
with their original rules. Full source paths/bytes/hashes, plans, requests,
results and declared fixture spans are retained. The new checkout's actual
paths are recorded; no earlier checkout's provenance or hash is substituted.

No source-to-C9 entrypoint is called. The prior C2-HASH pinned-Reader refusal
recorded by PR926 remains historical, uncredited and untouched. No retry,
repin, source-admission replacement, callback bypass of source intake or
transfer of that refused operation is part of this increment. Native record
validation and the existing normalized Gravity fixture boundary establish
no source/Core/C8 admission.

Acceptance requires exact eight-body/helper conservation, eleven unchanged
prior stage functions, original interposition and host lookup/exception
behavior, complete raw and parsed capture equality, focused positive/negative
and adversarial tests, language/ownership and lint gates, contract validators,
independent acceptance, own CI/artifact audit and exact clean current-main
integration preflight. Implementation and candidate evidence are outstanding
at this early draft checkpoint.

## Unsupported boundaries

Fixed examples, completion/proof labels, native truthiness/vacuity, runtime
profile flags, unsafe islands, safe-API labels and review-required fields
remain compatibility data. They do not implement canonical lifetime, escape,
region, arena, linear-resource, provider or runtime-check authorization.
The existing exactly-one terminal count is linear-state data, not a work
allowance, and remains unchanged.

The current Gravity normalized-transition engine retains its existing
semantic ownership and fixture boundary. No source/Core/C8 authentication,
grant/proof/provider trust, target execution, performance, publication,
authoritative migration, release, self-hosting or seed retirement is
established. Held routes remain untouched; Tooling925 retains its separate
source, reviews and CI without duplicate credit.
