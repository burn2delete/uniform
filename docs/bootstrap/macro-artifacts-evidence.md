# Macro artifact construction and verification

This increment moves the SH-05 artifact algorithms and their active verification
implementation into the macro stage. D1, D2, D3, D9, L4, L12, L16, SAFE12,
C1, C3, C4 and C15 govern the boundary. Existing Gravity macro source remains
the semantic authority; Clojure remains the explicit seed adapter.

The stage consumes authenticated C3 products, source snapshots, verified macro
runs and explicit seed operations. It emits expanded syntax, ordered traces,
source/provenance maps, deterministic identities, envelope descriptors and
verification/proof records. C4 owns macro artifact, lineage and trace failures;
upstream syntax and reader diagnostics keep their existing owners.

Artifact construction and verification must preserve actual source paths,
co-canonical extensions, metadata, hygiene, generated origins, parent identity,
ordered independent runs, exact native carrier rules and all existing rejection
points. The source/version constants, host operation configuration, envelope
services and process-local plan lifetime remain explicit seed inputs.

The existing 14 pinned-runtime APIs remain. New APIs have exact arglists and
operation inputs in namespace metadata:

| API group | Public API | Inputs -> outputs |
| --- | --- | --- |
| Requests | `merge-request`, `defn-form?`, `defmacro-form?`, `semantic-component-id`, `request` | Source syntax, module and reader identities -> macro request and component identities |
| Construction | `source-syntax-stream`, `expanded-form`, `trace-record`, `expanded-syntax-object`, `expanded-graph`, `expanded-graph-valid?`, `assemble` | Authenticated C3 parent and verified runs -> expanded syntax, trace, graph and SH-05 artifact |
| Identity | `path-neutral-value`, `identity-chunks`, `run-summary`, `artifact-payload`, `artifact-identity-input`, `artifact-id`, `stream-identity-input`, `stream-id`, `trace-identity-input`, `trace-id` | Semantic projections and ordered runs -> unchanged canonical preimages and digests |
| Source binding | `source-snapshot!`, `source-stable!`, `accept-syntax-parent!` | Requested path, bytes and C3 records -> checked source/parent identity or C4 rejection |
| Envelopes | `envelope-descriptor`, `envelope-summary` | Binding and artifact identities -> existing authenticated descriptor/summary records |
| Verification | `replay-report`, `verify` | Artifact, invocation and seed operations -> fresh Gravity replay and complete C4 check report |
| Compatibility | `c4-identity-input`, `c4-artifact-id`, `c4-artifact`, `compiler-source-path?`, `legacy-position?`, `source-eligible?`, `compile-source` | Existing eligibility and legacy adapters -> unchanged C4 dispatch/projection |

Pure structural projections take only their data inputs. Dependent algorithms
receive explicit seed operations. Source/file entrypoint glue and the three
SH-07 transaction/proof facades remain in bootstrap; their rule algorithms
consume the stage verifier's results. The verifier's four helpers are lexical.
Their old component definitions and cleanup entries are retired.

Receipt reuse retains the seed verifier root only when the stage implementation,
operation-map object and declared operation/configuration/cache Var roots are
canonical and have no thread bindings. Otherwise a fresh identity disables
reuse. The seed install runs after all macro callbacks are defined, preserving
the original source locator metadata. Qualification invokes no stage or
replaceable operation callback. No persistent stage cache is introduced.

Acceptance requires actual paired multi-expansion artifacts and real invalid
parent/output rejection, complete artifact identity comparison, focused
provenance/tamper regressions, proportionate integration checks, independent
adversarial review and exact clean candidate integration. The draft PR records
the final APIs, source tuple and executed evidence as implementation proceeds.

The paired `defn-multiple` fixtures emit two independent def/fn expansions and
artifact identity
`sha256:aa7c78fa3b2fbd8568f6b1baf92f224876d2452ae20e107b48614aba13039901`.
Complete artifacts compare equal before and after extraction. Receipt tests
cover canonical reuse, stage/seed/cache interposition, equal-valued thread
binding, invocation order and interruption. The cleanup unit isolates checked-core
prelude snapshots and exercises production lifetime unwinding; it performs no
checked-core admission.

CI run 41144468276 passed reader 16/209 and syntax 61/615, then hit the
combined bootstrap job's 15-minute deadline during macro regression. The
unchanged macro selection now runs in its own required matrix mode. Command
arguments, heaps, per-selection deadlines and the job deadline are preserved;
the cancelled run remains a nonpass.

This extraction grants no release, migration, self-hosting, seed-retirement or
performance authority. Legacy macro engine organization and remaining macro
accounting removal stay open until their own implementation and evidence land.
