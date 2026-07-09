# Stage1 Reader Execution Proof Report

Status: complete for the stage1 reader-table execution bridge

This report records the second post-stage0 self-hosting reduction step. The
Clojure seed loads the `stage1-reader-table` authored in
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, uses that table to
read a Gravity fixture, and compares the resulting forms against the current
stage0 reader.

## Capability

```bash
clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-execution-proof.edn
```

The command emits a `:gravity/stage1-reader-execution-artifact` with:

- artifact id `sha256:bcf0e0e73e71060ae1f3b501e3a3cdebc689485997031275daa5ebffbaf3223b`
- reader table id `sha256:ec8d4b9f90f249f8a62202871f6a02e9e252f87edeb901d3153b5e7e900669e6`
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 6 stable reader-execution diagnostics
- explicit proof limitations: the Clojure host interpreter is still used, the
  reader algorithm is not yet authored in executable Gravity, and the Clojure
  seed is not retired

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture covers comments, an `ns` form, metadata maps, vectors,
sets, strings, integers, booleans, nil, symbols, and keywords inside the subset
declared by `stage1-reader-table`.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-execution-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:reader-table-id a)) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :limitations :clojure-host-interpreter?])) (println (get-in a [:capability-based-proof :limitations :gravity-reader-algorithm-authored?])))'
:gravity/stage1-reader-execution-artifact
sha256:bcf0e0e73e71060ae1f3b501e3a3cdebc689485997031275daa5ebffbaf3223b
sha256:ec8d4b9f90f249f8a62202871f6a02e9e252f87edeb901d3153b5e7e900669e6
4
true
6
:complete
true
false

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that Gravity-authored bootstrap source now drives observable
reader behavior through a table interpreted by the Clojure seed. The stage1
reader output matches stage0 reader forms on the accepted fixture, and malformed
inputs fail closed with stable diagnostics.

## Residual Risks

This is not a self-hosted reader. The table is authored in Gravity, but the
reader algorithm is still hosted by Clojure. The next capability gate is to move
the reader algorithm itself into executable Gravity source and prove the same
accepted fixture, rejected diagnostics, source spans, and artifact parity.

Follow-on evidence: `docs/artifacts/phase-15/reports/stage1-reader-algorithm-proof-report.md`
records the first Gravity-authored reader entrypoint executed by the seed
evaluator. It still keeps the host scanner explicit.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-pipeline-proof-report.md`
records the first Gravity-authored reader pipeline entrypoint executed by the
seed evaluator. It splits the old whole-reader host primitive into token
scanning and form-building host primitives while preserving stage0 form parity.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-classifier-pipeline-proof-report.md`
records the current bridge, which executes a Gravity-authored token-classifier
pipeline and removes `:reader/tokens-from-characters` from the latest bridge.

Latest follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records the current bridge, which executes a Gravity-authored token-realizer
pipeline and removes `:reader/tokens-from-classifier` from the latest bridge.
