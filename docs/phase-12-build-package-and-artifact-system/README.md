# Phase 12 - Build, Package and Artifact System

Defines project files, builds, packages, artifacts, provenance, and target matrices.

## Documents

- [165 PKG1 - Project File Specification](165-pkg1-project-file-specification.md)
- [166 PKG2 - Build System Architecture](166-pkg2-build-system-architecture.md)
- [167 PKG3 - Artifact Model Specification](167-pkg3-artifact-model-specification.md)
- [168 PKG4 - Package Manager Specification](168-pkg4-package-manager-specification.md)
- [169 PKG5 - Dependency Resolution Specification](169-pkg5-dependency-resolution-specification.md)
- [170 PKG6 - Capability and Permission Manifest Specification](170-pkg6-capability-and-permission-manifest-specification.md)
- [171 PKG7 - Reproducible Build Specification](171-pkg7-reproducible-build-specification.md)
- [172 PKG8 - Package Safety and Audit Metadata Specification](172-pkg8-package-safety-and-audit-metadata-specification.md)
- [173 PKG9 - Private Registry and Latent Package Space Design](173-pkg9-private-registry-and-latent-package-space-design.md)
- [174 PKG10 - Supply-Chain Security and Provenance Specification](174-pkg10-supply-chain-security-and-provenance-specification.md)
- [175 PKG11 - Cross-Compilation and Target Matrix Specification](175-pkg11-cross-compilation-and-target-matrix-specification.md)
- [176 PKG12 - Artifact Signing, Verification and SBOM Specification](176-pkg12-artifact-signing-verification-and-sbom-specification.md)

## Phase Authoring Contract

- Phase 12 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
