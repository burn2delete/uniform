# P18-T01 Thin CLI Wrapper Report

Task: `P18-T01`
Status: complete for the bootstrap-hosted thin wrapper milestone
Date: 2026-07-01

## Evidence

- Proof artifact:
  `docs/artifacts/phase-18/cli/p18-t01-thin-cli-wrapper-proof.edn`
- Public wrapper:
  `bin/gravity`
- Explicit bootstrap recovery wrapper:
  `bin/gravity-bootstrap`
- Clojure evidence command:
  `clojure -M:gravity p18-t01-thin-cli-wrapper`
- Automated validation:
  `clojure -M:test`

The proof artifact id is
`sha256:3b915e0f4f0b60aa3221ba7df50a33bc508069b613fbb0c1c589a30ae5e8133c`.

## Command Proof

`bin/gravity --version` reports:

```text
:bootstrap-hosted? true
:seedless-release? false
:delegates-to "clojure -M:gravity"
```

`bin/gravity help` lists `check`, `run`, `compile`, and artifact command
forwarding.

`bin/gravity check examples/hello.gravity` prints:

```text
gravity stage0 check passed: hello.main
```

`bin/gravity run examples/hello.gravity` prints:

```text
Hello Gravity
```

`bin/gravity compile examples/hello.gravity` emits a stage0 hosted artifact
with kind `:gravity/stage0-hosted-artifact`.

`bin/gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity`
fails through the wrapper with stable diagnostic `L2-BUILTIN-ARITY`.

`bin/gravity --assert-seedless-release` fails with stable diagnostic
`P18T01001`, proving the thin wrapper cannot be claimed as the final seedless
release artifact.

## Capability Result

The P18-T01 proof records:

- `:thin-wrapper-present? true`
- `:bootstrap-command-present? true`
- `:check-delegates-to-stage0? true`
- `:run-delegates-to-stage0? true`
- `:compile-delegates-to-stage0? true`
- `:diagnostic-preserved? true`
- `:seedless-overclaim-rejected? true`
- `:final-seedless-release? false`

## Validation

`clojure -M:test` passed 239 tests and 11487 assertions with 0 failures and 0
errors.

`tools/validate_gravity_docs.py` passed with:

```text
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders
```

## Limits

This completes only the P18-T01 bootstrap-hosted command shape. It does not
complete packaged JVM CLI work, self-hosted release artifact emission,
executable `compile -o` behavior, seedless release boundary proof,
reproducible release evidence, provenance, SBOM, signing records, or release
governance. Those remain Phase 18 downstream tasks.
