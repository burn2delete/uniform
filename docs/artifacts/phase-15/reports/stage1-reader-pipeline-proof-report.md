# Stage1 Reader Pipeline Proof Report

Status: complete for the stage1 reader pipeline bridge

This report records the fourth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records a split host
primitive boundary for token scanning and form building.

## Capability

```bash
clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-pipeline-artifact` with:

- artifact id `sha256:44657161a6f5eb352b86d6e31f815fa4117592ed75b143360b2508253f5cef78`
- pipeline id `sha256:8f1746f3f9c12d613c11cf4c45c65fc01dd10ed9222c542340ea4cf756b4d312`
- Gravity entrypoint `stage1-read-source-pipeline`
- explicit host primitive boundary `[:reader/scan-tokens :reader/forms-from-tokens]`
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored pipeline entrypoint.
The pipeline emits a token-stream artifact before form construction and records
the host primitives invoked by the seed evaluator. The form-building primitive
constructs forms from the token vector rather than invoking the older
whole-source reader primitive.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed pipeline diagnostics:

- missing Gravity reader pipeline entrypoint -> `STAGE1PIPE001`
- unsupported executable Gravity form -> `STAGE1PIPE002`
- unsupported host primitive -> `STAGE1PIPE003`
- invalid token stream -> `STAGE1PIPE004`
- stage0 form divergence -> `STAGE1PIPE005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-pipeline-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-pipeline-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :host-primitive-boundary-split?])) (println (get-in a [:capability-based-proof :whole-reader-host-primitive-removed?])) (println (get-in a [:capability-based-proof :limitations :host-tokenizer?])) (println (get-in a [:capability-based-proof :limitations :host-form-builder?])))'
:gravity/stage1-reader-pipeline-artifact
sha256:44657161a6f5eb352b86d6e31f815fa4117592ed75b143360b2508253f5cef78
sha256:8f1746f3f9c12d613c11cf4c45c65fc01dd10ed9222c542340ea4cf756b4d312
[:reader/scan-tokens :reader/forms-from-tokens]
82
4
true
10
:complete
true
true
true
true

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader pipeline entrypoint is
executable by the seed evaluator and can drive accepted and rejected behavior
without using the former whole-reader `:reader/read-with-table` host primitive.
The artifact records token coverage, form parity, source spans, diagnostics,
and the remaining trusted host boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint and pipeline shape are authored
in Gravity, but the seed evaluator, token scanner, and form builder are still
Clojure host primitives. The next capability gate is to replace
`:reader/scan-tokens` and `:reader/forms-from-tokens` with executable Gravity
code while preserving accepted forms, rejected diagnostics, source spans, token
coverage, artifact provenance, and stage0 parity.

Follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-character-pipeline-proof-report.md`
records a newer bridge that removes `:reader/scan-tokens` by splitting source
characters from token construction. That newer bridge still keeps the Clojure
seed, character stream, tokenizer, and form builder explicit.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-classifier-pipeline-proof-report.md`
records a newer bridge that removes `:reader/tokens-from-characters` from the
latest bridge by introducing a Gravity-authored token classifier. That bridge
still keeps the Clojure seed, character stream, token realizer, and form
builder explicit.

Latest follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records a newer bridge that removes `:reader/tokens-from-classifier` from the
latest bridge by introducing a Gravity-authored token realizer specification.
That bridge still keeps the Clojure seed, character stream, token realizer
executor, and form builder explicit.
