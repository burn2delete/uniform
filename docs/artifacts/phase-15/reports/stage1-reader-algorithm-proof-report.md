# Stage1 Reader Algorithm Proof Report

Status: complete for the stage1 reader algorithm bridge

This report records the third post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and compares the produced
forms against the current stage0 reader.

## Capability

```bash
clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-algorithm-proof.edn
```

The command emits a `:gravity/stage1-reader-algorithm-artifact` with:

- artifact id `sha256:55659ae6920bbd626e75932d61aa5ffe4250d337857238aac425e6112ed8f889`
- algorithm id `sha256:4612cf4a3521f086d15fa081f06ed0f85b4b56528b346b5e73bb7aa443068283`
- Gravity entrypoint `stage1-read-source`
- explicit host primitive boundary `:reader/read-with-table`
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 9 stable diagnostics across algorithm and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored entrypoint, not only
through direct Clojure table execution.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed algorithm diagnostics:

- missing Gravity reader entrypoint -> `STAGE1ALGO001`
- unsupported executable Gravity form -> `STAGE1ALGO002`
- unsupported host primitive -> `STAGE1ALGO003`
- stage0 form divergence -> `STAGE1ALGO004`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-algorithm-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:algorithm-id a)) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-algorithm-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :gravity-reader-algorithm-authored?])) (println (get-in a [:capability-based-proof :limitations :host-character-scanner?])))'
:gravity/stage1-reader-algorithm-artifact
sha256:55659ae6920bbd626e75932d61aa5ffe4250d337857238aac425e6112ed8f889
sha256:4612cf4a3521f086d15fa081f06ed0f85b4b56528b346b5e73bb7aa443068283
4
true
9
:complete
true
true

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader entrypoint is executable by
the seed evaluator and can drive the same accepted and rejected behavior as the
stage1 reader-table bridge. The artifact records the remaining trusted host
primitive boundary instead of hiding it.

## Residual Risks

This is not a self-hosted reader. The entrypoint is authored in Gravity, but the
seed evaluator and `:reader/read-with-table` character scanner are still
Clojure in this bridge. Follow-on evidence is recorded in
`docs/artifacts/phase-15/reports/stage1-reader-pipeline-proof-report.md`, which
splits that primitive into explicit token scanning and form-building host
primitives. The next capability gate after that pipeline bridge is to replace
those split host primitives with executable Gravity code while preserving
accepted forms, rejected diagnostics, source spans, token coverage, and stage0
parity.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-classifier-pipeline-proof-report.md`
records the current bridge, which makes the token classifier Gravity-authored
and keeps only source-character extraction, token realization, and form
building as explicit host primitives.

Latest follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records the current bridge, which makes the token realizer specification
Gravity-authored and keeps only source-character extraction, token realization
execution, and form building as explicit host primitives.
