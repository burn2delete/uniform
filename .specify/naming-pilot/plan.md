# Plan and sampled legacy audit

Owner: Spec Kit adoption delegate. Input: main `c21068a6`.
Canonical architecture: [compiler phase](../../docs/phase-06-compiler-architecture/README.md).
The proposed names below describe behavior; they are not reservations or completed
renames. Coordinate actual owners through Deus/process bridge before touching them.

| Observed legacy surface | Semantic direction | Required transaction |
| --- | --- | --- |
| `bootstrap/clojure/src/gravity/c2_reader_diagnostics.clj` and its child namespaces | `reader/diagnostics.clj`, `gravity.reader.diagnostics` | Move facade and child require graph; update bootstrap caller, reader diagnostics test and bootstrap compatibility tests; retain diagnostic IDs and payload behavior |
| `bootstrap/clojure/src/gravity/c16_incremental.clj` and child namespaces | `build/incremental.clj`, `gravity.build.incremental` | Update facade/subnamespace requires, bootstrap caller, compatibility test and adjacent cache tests; inspect persisted schema/receipt identity before changing any serialized fields |
| `bootstrap/clojure/src/gravity/c15_c16_pass_cache.clj` and child namespaces | `build/pass_cache.clj`, `gravity.build.pass-cache` | Review existing generic pass-cache boundary to avoid conflating responsibilities; migrate request/contract/orchestration imports and cache tests together; preserve cache format/version compatibility or invalidate explicitly |
| `components/self-hosting-sh03-sh07/00887-sh07-core-build-form-tree.clj` and `sh07-core-build-form-tree` | Core source-tree builder and `build-form-tree` behavior | Loader/load-order entry, module assembly consumer, mutation and recursion tests must move coherently; coordinate active Core owner; do not disturb fragment identity/proof records |
| `bootstrap/clojure/test/gravity/self_hosting/sh07_current_public_closure_integration_test.clj` namespace | Core public-source integration test | Update namespace/path, runner selectors and documented commands together; retain historical PR356 references as history |

This was a bounded audit: files and namespace declarations were read directly;
`rg -l` located require/caller occurrences in `bootstrap/clojure/src` and `test`.
For example reader diagnostics is referenced by `gravity/bootstrap.clj`, its leaf
test and `bootstrap_compatibility/c2_test.clj`; incremental compilation is referenced
by bootstrap, compatibility and adjacent cache tests. Core builder references were
found in `bootstrap_impl/load_order.edn`, module assembly, mutation and recursion
tests. This is not an exhaustive dependency closure or compatibility attestation.

## Adoption implementation

Update only `.specify` guidance and the generated Codex skills. Use the existing
pinned official v1.2.3 CLI in external scratch to regenerate the local preset's
skills; import no new executable helper. Keep old validation snapshots intact.
Validate documents, generated/template consistency and this change's new-name
diff. Record actual results separately in evidence.md.

## Cloud handoff

The next owner reviews this draft and chooses one independent legacy transaction
with the relevant implementation owner. Re-scan callers on the then-current base;
check APIs, namespace aliases, manifests, source hashes, test selectors and docs.
Run focused semantic/compatibility checks for that rename, then affected integration
checks. Do not treat this sampled plan as implementation acceptance or permission
to rename active work. Parent decides integration and any shared-doc link.
