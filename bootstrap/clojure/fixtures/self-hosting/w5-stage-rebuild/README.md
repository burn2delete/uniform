# W5 Wave3 Stage-Rebuild Orchestrator

This fixture is the executable Gravity-owned W5 Wave3 leaf for an exact
stage1 to stage2 to stage3 rebuild record.  The engine is
`bootstrap/gravity/src/gravity/self_hosting/w5_stage_rebuild_orchestrator.gravity`.
It validates a compiler source inventory, compiler/verifier/artifact executable
identities, complete per-stage artifact sets, parent-child `compiled-by`
lineage, and the exact build recipe, environment, dependency lock, LLVM
toolchain, target, and conformance identifiers.

Stage keys are the canonical `:stage1`, `:stage2`, and `:stage3` values. The
stage1 artifact set is explicitly compiled by the seed compiler executable;
stage2 and stage3 artifact sets are compiled by their parent stage compiler
executables, never by their own output executable. The request carries exact
BOOT5 compatibility rows and BOOT7 equivalence input/report records. Both
remain pending until execution and independent evidence exist.

The three source inventory entries have the exact governed unit IDs
`:compiler-source-a`, `:compiler-source-b`, and `:compiler-source-c`, with
dependencies `[]`, `[:compiler-source-a]`, and
`[:compiler-source-a :compiler-source-b]`. Their content IDs are cross-bound to
the corresponding stage1, stage2, and stage3 compiler source IDs; the inventory
ID is cross-bound through the aggregate compiler and every stage record.
Coherently renaming the entry IDs and dependencies is rejected.

The pending BOOT7 comparison operands are exactly the stage2 and stage3
compiler executables and their corresponding stage2 and stage3 artifact sets,
as required by BOOT7. Substituting the seed compiler or stage1 artifact set in
both the input and report is rejected even though the local pair stays
internally coherent.

The accepted `.gravity` and `.qst` files are co-canonical.  They contain real
positive records for all three stages and all executable identities, while
actual candidate execution and independent evidence remain `:pending`.  The
orchestrator returns `:accepted` only for the bounded structural record;
completion remains `:incomplete`, the verifier gate is `:blocked`, and release,
self-hosting, public, and seed-retirement claims remain false.

Structural recomputation is explicitly marked `:structural-only`; raw-byte
and canonical-byte hash validation remain `:pending` and are never inferred
from identifier strings.

The candidate target is exact LLVM on Linux x86_64 with ELF and the
`:sysv-amd64` ABI.  The namespace `(:target :jvm)` is only the stage2 Clojure
compiler-plan harness.  Unsupported targets are the ordered set `[:darwin
:darwin-arm64 :darwin-x86_64 :windows]`; every one records
`invokes-clojure? false`, `links-jvm? false`, and `fallback? false`.  No
candidate record may fall back to Clojure, JVM, Darwin, or an inferred
cross-target.

The rejected pair provides executable one-to-one mutators for source catalog
and source identity, hostile spans and top/nested provenance, stage order and
lineage, nested keysets, executable identity, artifact set, build recipe,
  environment, lock, toolchain, target, conformance, BOOT5/BOOT7 matrix and
  equivalence records, evidence class, and no-fallback/authority policy.
  Artifact-parent, source-unit, and BOOT7 operand substitutions are direct
  hostiles. Malformed
  request spans use a deterministic valid diagnostic fallback. Each diagnostic
  has a stable `W5-SR-*` identifier, source span, path-bearing provenance, and
  remediation.

Semantic identity excludes checkout paths.  Actual source and artifact paths
appear only under provenance.  The accepted evidence class is an executable
record with pending execution/independent verification; source-ownership,
check-only, and replay records are not completion evidence.

This slice is static-only.  It does not execute a compiler, verifier, linker,
toolchain, native artifact, JVM fallback, or Docker/container command.  The
later focused test command is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-stage-rebuild-orchestrator-test
```
