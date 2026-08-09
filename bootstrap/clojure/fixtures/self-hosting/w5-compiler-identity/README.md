# W5 Wave4 compiler-identity verifier

This fixture owns the bounded W5 Wave4 compiler-identity leaf.  The engine is
`bootstrap/gravity/src/gravity/self_hosting/w5_compiler_identity_verifier.gravity`.
It is a stage2 `:meta` program whose `(:target :jvm)` declaration is only the
Clojure stage2 compiler-plan/runtime harness.  The source validates records as
data; it does not invoke a compiler, verifier, linker, LLVM toolchain, JVM,
native artifact, Docker/container, filesystem, network, or lock/commit tool.

The accepted `incomplete-compiler-identity.gravity` and `.qst` files are
co-canonical byte-identical sources.  They inventory compiler, verifier, and
artifact-constructor source and executable identities; bind exact
stage1 to stage2 to stage3 lineage; and bind a deterministic recipe, hermetic
environment, frozen lock, LLVM/ELF/SysV toolchain, conformance record, and
path-bearing provenance.  Identity input is path-neutral while provenance
retains the actual checkout paths.  The identifiers are pending placeholders,
not generated final hashes.

The inventory roles and pending IDs are positionally frozen and must equal the
constructor and stage records. Stage inputs, outputs, compiler identities, and
lineage edges are cross-bound rather than independently shape-valid. Source
spans require nonnegative ordered byte offsets and positive line/column values;
source, executable, and artifact provenance retain `.gravity` or `.qst`,
`.elf`, and `.edn` respectively. The checkout root is derived from the exact
top accepted-fixture path. The top verifier and artifact-constructor paths,
every constructor source/executable/artifact path, and every stage
source/executable/artifact path must then equal that one root plus their exact
governed repository-relative paths. A valid suffix under `/attacker`, or a
coherent set of nested paths under a second root, is rejected. The actual
source extension is passed explicitly for each execution (`.gravity` or
`.qst`) and is cross-bound to constructor and stage provenance; no fixture
constructor silently defaults to `.gravity`.
A malformed top-level span receives a deterministic valid diagnostic fallback
span and cannot enter the accepted record.

Source-span identities are also context-bound. The top span equals the owning
provenance source id and exact fixture path, inventory spans equal their entry
source ids, and constructor/stage spans equal their owning source ids and path
suffixes. Coherent substitution of a record source id and its span source id
still fails closed.

The accepted record is structurally valid but deliberately incomplete.  It is
an executable positive record with candidate execution and independent review
pending.  The verifier returns `:accepted` with `:completion-status
:incomplete`, a blocked verifier gate, explicit residual seed harness, and
`:fail-closed? true`.  It never claims self-hosting, public authority, release
readiness, or seed retirement.  Global flags remain
`:clojure-seed-boundary? true`, `:self-hosted? false`, `:release? false`, and
`:public-authority? false`.

The candidate target is exactly LLVM on Linux x86_64 with ELF and the
`:sysv-amd64` ABI.  Unsupported targets are the ordered list
`[:darwin :darwin-arm64 :darwin-x86_64 :windows]`.  Each records
`:support :unsupported`, `invokes-clojure? false`, `links-jvm? false`, and
`fallback? false`; cross-target inference and Darwin fallback are false.

The rejected co-canonical pair exports one total mutator for each diagnostic
family: source inventory, constructor identity, executable identity, stage
record, stage lineage, recipe, environment, lock, toolchain, provenance,
target, cross-target inference, fallback, conformance, evidence class, and
authority.  Diagnostics retain a source span and path-bearing provenance, and
result verification recomputes from the request and rejects substitution with
`W5-CI-SUBSTITUTION`. Every rejected mutator accepts the explicit positive
request, so `.gravity` and `.qst` negatives retain their actual path and
extension while crosslink, suffix, and span hostiles remain directly testable.
The result verifier freezes exact top-level and nested schemas and compares the
entire recomputed result, including `:artifact`; artifact substitution and
top-level or nested extra keys are rejected.

Later, focused verification may be run by the owning integration task with:

```text
clojure -M:test --namespace gravity.self-hosting.w5-compiler-identity-verifier-test
```

That command is intentionally not run as part of this static-only slice.
