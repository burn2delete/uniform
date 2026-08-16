# SH-23 Hermetic Package/Build Fixtures

These co-canonical fixtures exercise the fixed, offline bootstrap dependency
graph and deterministic component plan owned by the SH-23 leaf.

- `accepted/hermetic-builds.gravity` and `.qst` provide the same valid build
  under two physical checkout roots.
- `rejected/invalid-hermetic-builds.gravity` and `.qst` isolate project,
  lockfile, authority, environment, target, lineage, library, and graph
  failures.

The fixtures do not claim network resolution, general semantic-version
solving, arbitrary plugins, shell execution, a complete compiler driver, or
seedless build execution.

The leaf accepts only the explicit portable-MIR bootstrap target. It validates
the supplied SH-21 legality and SH-22 library record shapes and binds them into
the build identity; dependency-record authentication remains a coordinator
integration gate.
