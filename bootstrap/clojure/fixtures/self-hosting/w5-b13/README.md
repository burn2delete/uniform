# W5 B13 artifact-emission leaf

This fixture owns the executable Wave2 B13 artifact-emission slice.  The
Gravity emitter is a stage2 `:meta` leaf: it validates a supplied static
artifact record, emits inspectable output records, and never claims a release,
public, or self-hosted compiler authority.

The executable leaf and both fixture namespaces explicitly declare
`(:target :jvm)` because the existing stage2 compiler-plan parser and runtime
harness currently accept that source target.  This is only the compiler-plan
harness boundary.  It does not select the emitted artifact target: every
policy, request, and result candidate record must remain exact
`:llvm-x86_64-linux` with Linux `x86_64`, LLVM, ELF, and `:sysv-amd64` coherence.

The accepted and rejected requests are co-canonical byte-identical `.gravity`
and `.qst` files.  A valid request contains a common manifest, content-hash
record, artifact graph, source/debug map, compiler and dependency provenance,
safety/proof/certificate evidence, effect/capability summary, target/runtime/
ABI/layout summary, reproducibility record, conformance evidence, and a
development-only release gate.

This static leaf cannot hash emitted bytes or perform an independent rebuild.
It therefore checks only deterministic structural cross-links: supplied hash
records remain `:pending-unverified`, set
`:cryptographically-verified? false`, and cannot establish reproducibility.
Even the accepted structural request stays blocked and incomplete.  A coherent
producer-side hash substitution is rejected against the separately recorded
reproducibility input sequence; that structural rejection is not represented
as cryptographic verification.

The artifact target scope is exact: `:llvm-x86_64-linux` on Linux `x86_64`,
LLVM, ELF, `:sysv-amd64`, with no runtime provider.  Unsupported targets are
the ordered vector `[:darwin :darwin-arm64 :darwin-x86_64 :windows]`; each has
`:support :unsupported`, `:invokes-clojure? false`, `:links-jvm? false`, and
`:fallback? false`.  The emitter rejects target changes, cross-target
inference, and any Darwin or JVM fallback.  The JVM appears only in the explicit source namespace declaration
and later stage2 execution harness residual boundary; it is never an artifact
runtime provider or cross-target inference.

The manifest target metadata is exact and cross-bound to the separately
validated target/runtime/ABI summary.  Target features are exactly
`#{:x86_64 :sse2}`.  ABI layout is exactly `x86_64`, ELF,
`:sysv-amd64`, 64-bit little-endian, recorded as architecture, binary format,
calling convention, pointer width, and endianness.  Direct or coherent feature,
architecture, binary-format, calling-convention, or pointer-width drift is
rejected.

The artifact graph is a strict adjacent chain across the canonical D1
pipeline, including explicit MIR and domain-IR verification, from source forms
through package and provenance recording.
Its root is the real `:source-forms` node, while `:artifact-id` separately
binds the emitted artifact identity.  Exact node, edge, pass, and origin order
makes every node reachable and rejects missing, reordered, mislabeled,
or disconnected stages.  The source map carries the same exact ordered phase
set, a bounded source span and explicit origin at every location.

The manifest binds its conformance evidence id to the supplied conformance
record, but that record is only a structural cross-link.  Its status and
metadata-preservation status remain `:pending-unverified`, and
`:independently-verified?` remains false.  Caller-supplied pack names,
diagnostic names, or a `:complete` label cannot promote conformance.

Compiler, dependency, and top-level provenance use exact closed schemas.
Compiler identity freezes the record artifact, status, version, generator,
pass pipeline, and pass contract.  Dependency provenance freezes its record
artifact, status, generator, graph id, toolchain, runtime providers, and the
ordered unique inventory `gravity/compiler-core-v1` then
`gravity/llvm-lowering-contract-v1`.  That inventory is cross-bound through
the top provenance record, manifest provenance, dependency record, and
artifact graph, so even a coherent arbitrary replacement is rejected.  The
top provenance status is `:structural-complete`: this static check does not
promote it to independently or cryptographically complete provenance.  The
result therefore reports structural provenance and keeps
`:provenance-complete? false`.

Evidence cross-links use type-coherent values: manifest `:effects` and
`:capabilities` are the exact empty sets carried by the effect/capability
summary, while safety, proof, and certificate links use the evidence bundle
ID.  The bundle has a closed schema and exact safe mode, proven-safe outcome,
proof list, certificate list, and empty unsafe-audit inventory.  Its status is
only `:structural-complete`; the result keeps evidence completion false while
reporting structural completeness.  Missing, extra, mistyped, substituted, or
nonempty unsafe-audit contents are rejected.

The development release gate is also closed: its artifact, blocked status,
reason, ordered downstream blocks, diagnostic, authority, nonclaims, and
status are exact.  Missing fields, extra fields, and caller substitutions are
rejected before any accepted result is emitted.

The request's nested `:non-authority` record is mandatory and closed.  It
retains the Clojure seed boundary, denies self-hosting, release, public
authority, and full-language completion, and keeps authority exactly
`:non-authority`.  The same record is used by policy, request, release gate,
and result; missing, extra, or contradictory nested claims are rejected at
request-shape validation.

The rejected fixture includes direct hostile substitutions for each B13 family
and for coherent supplied hashes, phase/span/origin metadata, manifest/compiler
provenance, exact provenance keysets and literals, direct and coherent
dependency substitution, target-runtime, conformance, evidence, and artifact-graph
cross-links.  The canonical request constructors accept an actual
source path so `.gravity` and `.qst` provenance is audited separately.  Stable
diagnostics cover every B13 family:

- `B13-SCHEMA` - unsupported manifest schema;
- `B13-HASH` - mismatched content or manifest digest;
- `B13-PROVENANCE` - missing compiler/generator/pass/dependency provenance;
- `B13-SOURCEMAP` - lost source spans or generated-origin chain;
- `B13-EVIDENCE` - incomplete safety/proof/effect/capability evidence;
- `B13-TARGET` - incoherent target, ABI, layout, runtime, or provider record;
- `B13-CONFORMANCE` - missing policy conformance evidence;
- `B13-REPRODUCIBILITY` - missing deterministic build inputs;
- `B13-RELEASE` - attempted release-grade emission;
- `B13-GRAPH` - invalid artifact graph edge.

Identity fields contain only semantic/content IDs and are path-neutral.
Provenance and source-map records retain the actual source path for audit.
Every result keeps `:clojure-seed-boundary? true`,
`:self-hosted? false`, `:release? false`, `:public-authority? false`, and
`:authority :non-authority`.  The residual boundaries are the Clojure stage2
compiler plan and JVM stage2 runtime; LLVM toolchain execution, signing,
packaging, deployment, and seed retirement remain out of scope.
