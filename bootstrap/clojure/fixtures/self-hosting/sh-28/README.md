# SH-28 Trust and Provenance Leaf

This leaf implements a bounded Gravity-authored validator for bootstrap trust
evidence governed by `D9`, `PKG7`, `PKG10`, `PKG12`, `BOOT6`, and `BOOT8`.
It validates acyclic traversable compiler ancestry, controlled build inputs,
exact source and dependency graphs, lock contents, build recipes and builders,
diverse-build descriptors, trust-anchor and bootstrap-input descriptors,
structured SBOM/signature/policy/attestation records, subject-linked revocation
decisions, unsafe-audit references, and a derived explicit TCB delta. The SBOM
binds canonical capability, generated-source, binary-blob, dependency, safety,
manifest, and provenance inventories to the release artifact. The signature
binds that manifest, SBOM, safety summary, build recipe, content, provenance
attestation, signing policy, and declared root metadata. Unsafe-audit indexes
bind unique audit and evidence identifiers with an explicit verification state.
The SBOM's capability, generated-source, and binary-blob sets must exactly equal
a separately declared canonical release inventory. That inventory is bound to
the root artifact, content, source graph, manifest, and root build capability
inventory identifier; individually valid additions, omissions, and
substitutions in the SBOM are rejected.

The TCB prior, candidate, added, removed, retained, and release-exclusion sets
are derived from the request's declared bootstrap, lineage, runtime, build,
toolchain, and recovery inventories. They are not accepted as independent
labels. Every diverse-build compiler is bound to the compiler declared by the
root release-lineage node.

Every untrusted request and candidate result is structurally preflighted before
recursive validation or equality. Noncanonical sequences and host values are
rejected without sequence realization; node, depth, width, and scalar bounds
apply to canonical map/vector/set carriers.

The accepted `.gravity` and `.qst` fixtures are byte-identical. Physical
checkout, toolchain, artifact, runtime, recovery, rebuild, and trust-anchor
locations are retained in provenance but excluded from semantic identity.

This is early executable SH-28 evidence, not SH-28 completion. The leaf treats
the SH-27 equivalence report as supplied but unverified and treats independent
trust anchors as descriptor-only. External signature, SBOM, revocation-source,
and trust-anchor verification, public routing, seedless execution, and final
seed retirement remain pending. The result therefore keeps
`:release-trust-closed? false`, `:clojure-seed-retired? false`, and
`:clojure-seed-boundary? true`.
