# C6/C10 compiler support relocation

## Purpose

This increment separates two C6/C10 semantic compiler modules from the
temporary Clojure bootstrap. The modules remain executed through the existing
Clojure-hosted bridge, but their owned source path is now in the Uniform
language compiler project.

## Requirements

The relocation must preserve source bytes, module namespaces, exported
function names, diagnostics, and accepted/rejected behavior. Current source
selectors, component inventories, verification closure, CI stage selection,
and focused tests must resolve the new paths. The C6 call-arity module and
the SH-07 B51 producer/root files are outside this increment.

## Ownership and paths

The current semantic sources are:

```text
language/compiler/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity
language/compiler/src/gravity/compiler/c6_core_lowering_engine.gravity
```

Their namespaces and public function shapes are unchanged. Clojure remains
the execution host through the existing P15 source adapter and C6/C10 source
contract. No generic source registry was introduced because the existing
owner-specific path seams are sufficient for this move.

Current consumers updated by this increment include:

- the current C6/C10 ABI path and P15 source-relative-path adapter;
- the P15 rejected-candidate inventory and current compiler inventory;
- the current verification-catalog dependency closure;
- the checked-core-runtime stage selector and its unit assertions;
- current source-coverage, component-build, execution, and bootstrap tests;
- the SH-25 component catalog fixtures that carry current source paths.
- the current C6 rows in `docs/self-hosting-slice-ownership.edn` and
  `bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Dependencies and evidence boundaries

The increment is stacked on the checked-core relocation. It depends on the
existing Clojure stage0 seed, P15 bridge, and current CI selector contract.
The historical `current-compiler-program` and `flat-bool-compiler-program`
records in `bootstrap/clojure/src/gravity/c6_c10/source_contract.clj` retain
their original bootstrap path and recorded identity. Historical fields in
`sh07_proof_contract.edn`, workstream ledgers and terminal records, archived
reports, and other historical artifacts are not rewritten or renewed; only
the current C6 source rows in the proof contract are wired to the new path.
The moved lowering source also retains its pre-existing embedded fixture-path
string as part of the byte-preserved source; that string is not a filesystem
selector.

## Outputs and artifacts

The two source files are Git renames with unchanged bytes. The new current
path references are limited to the source contract, inventories, verification
catalog, CI selector, current tests, and current component fixtures listed
above. No release, migration, self-hosting, or seed-retirement evidence is
created by this relocation.

## Conformance and acceptance

The following checks are the focused evidence for this slice:

```text
git diff --check
clojure -M:dev-test --namespace gravity.self-hosting.sh07-c6-c10-checked-core-pipeline-source-coverage-test --fail-fast
clojure -M:dev-test --namespace gravity.self-hosting.sh07-core-lowering-source-coverage-test --fail-fast
clojure -M:dev-test --namespace gravity.self-hosting.sh07-c6-native-report-join-test --fail-fast
clojure -M:dev-test --namespace gravity.bootstrap-test --exact stage1-bootstrap-source-artifact-preserves-self-hosting-bridge --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

The C6/C10 source-coverage command runs against the relocated source but
currently fails its pre-existing static-shape and source-identity pins: the
source has 176 forms, 170 functions, and 185620 bytes while that test records
the older 145-form, 139-function, 141562-byte snapshot. This relocation does
not alter those historical pins. Full-suite, authoritative migration, and
release validation remain unrun and unclaimed here. The C6 core-lowering
coverage and native-report-join tests also reach the relocated source but stop
at the existing P15 stage2 emitter pin (`P15S23Q001`) because the current
`bootstrap/gravity/p15_s23/compiler.gravity` inventory path update changes its
observed source hash; dependent generated artifacts are intentionally not
regenerated in this source relocation. The focused stage1 bootstrap bridge
test is independently blocked by the pre-existing `STAGE1001` owner omission
in `bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity`.

This increment accepts the physical ownership move and current path wiring;
it does not claim a complete compiler, self-hosting, Clojure seed retirement,
or seedless release.
