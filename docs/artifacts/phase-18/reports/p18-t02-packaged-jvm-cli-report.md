# P18-T02 Packaged JVM CLI Report

Task: `P18-T02`
Status: complete for the bootstrap-hosted packaged JVM CLI milestone
Date: 2026-07-01

## Evidence

- Packaged jar:
  `target/phase-18/jvm-cli/gravity-jvm-cli.jar`
- Java launcher source:
  `bootstrap/clojure/java/gravity/cli/Main.java`
- Proof artifact:
  `docs/artifacts/phase-18/jvm-cli/p18-t02-packaged-jvm-cli-proof.edn`
- Package records:
  `docs/artifacts/phase-18/jvm-cli/p18-t02-package-manifest.edn`,
  `p18-t02-dependency-record.edn`, `p18-t02-artifact-manifest.edn`,
  `p18-t02-reproducible-build.edn`, `p18-t02-provenance.edn`,
  `p18-t02-sbom.edn`, and `p18-t02-signing-record.edn`
- Evidence command:
  `clojure -M:gravity p18-t02-write-packaged-jvm-cli-artifacts`

The proof artifact id is
`sha256:99499872bd04d794013b4eb1237521a9c0441aaf094d250161b18950ba1f185e`.

The packaged jar content hash is
`sha256:a16605a81fc81309f8c2c3370dc25f46689727dab16395047ff9f5a85ab0f907`.

## Command Proof

`bin/gravity --version` now reports `:phase "P18-T02"`,
`:packaged-jvm-cli? true`, `:bootstrap-hosted? true`, and
`:seedless-release? false`.

`bin/gravity run examples/core-app.gravity` prints:

```text
core-app
gravity:19:2
(:ok 19)
```

The P18-T02 proof records packaged-vs-Clojure parity for `check`, `run`,
`compile`, and `p18-t01-thin-cli-wrapper`, across `examples/hello.gravity` and
`examples/core-app.gravity`. The rejected app diagnostic is preserved through
the packaged path with `L2-BUILTIN-ARITY`.

`bin/gravity --assert-seedless-release` fails with stable diagnostic
`P18T02001`.

## Package Records

- Package manifest id:
  `sha256:3555e467d523b93224332222c149df631ef08ebcfa4c308339e084566dd428e5`
- Dependency record id:
  `sha256:c8292dfef6347c1e890b11217cffea611e60a1cccd682a1711d554575b3b67fc`
- Artifact manifest id:
  `sha256:25f24be60e193c643c61d17184d8664b41b632568568cd06c51f1baac5528f8b`
- Reproducible build record id:
  `sha256:dd138242981df567b503557ee005d8450d6b578e33a88bb8e27efbc9df7e7606`
- Provenance record id:
  `sha256:9467b58891c5c57fc2af284617ba045d29e97ea414a270c196eda2b086eadc1a`
- SBOM id:
  `sha256:674813d0379d8d71fdf6190b41f5ea6242e6146c922283218b6bfbb19c84428e`
- Development signing record id:
  `sha256:62b36cf5e830e819a0de9a1c7f697dc512d8164d6c4d1587fd25ec5fb3252e87`

## Rejected Evidence

The proof rejects package candidates with:

- `P18T02002` for missing package metadata
- `P18T02003` for command parity mismatch
- `P18T02004` for invalid target claims
- `P18T02005` for missing package provenance

## Validation

`clojure -M:test` passed 240 tests and 11537 assertions with 0 failures and 0
errors.

`tools/validate_gravity_docs.py` passed with:

```text
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders
```

## Limits

This completes only the packaged JVM CLI milestone. The package still records
`:bootstrap-hosted? true`, uses a Clojure/JVM runtime dependency boundary, and
does not satisfy the final seedless release, executable `compile -o`, release
boundary, cryptographic release signing, release governance, or Clojure seed
retirement gates.
