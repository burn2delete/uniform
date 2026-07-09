# Stage1 Reader Token Classifier Pipeline Proof Report

Status: complete for the stage1 reader token-classifier pipeline bridge

This report records the sixth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-token-classifier-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records a
Gravity-authored token classifier between source-character extraction and token
realization.

## Capability

```bash
clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-token-classifier-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-token-classifier-pipeline-artifact`
with:

- artifact id `sha256:f6f66393a40bdf19fe3cab497347fca1e33875e49beee2a868954de6f8ed2ef9`
- token-classifier pipeline id `sha256:beb095efaa5aae99a82c1483d8c326fe4a1abf3e3e0d4932b29eb711f8e8ee1d`
- Gravity entrypoint `stage1-read-source-token-classifier-pipeline`
- explicit host primitive boundary `[:reader/source-characters :reader/tokens-from-classifier :reader/forms-from-tokens]`
- no `:reader/tokens-from-characters`, `:reader/scan-tokens`, or
  `:reader/read-with-table` host primitive in this bridge
- Gravity-authored token classifier engine `:gravity-reader-token-classifier-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across token-classifier-pipeline and malformed-input
  failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored token-classifier
pipeline entrypoint. The pipeline emits a character-stream artifact, applies
the Gravity-authored token classifier, then emits the token-stream artifact
before form construction.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed token-classifier-pipeline diagnostics:

- missing Gravity reader token-classifier-pipeline entrypoint -> `STAGE1CLASS001`
- unsupported executable Gravity form -> `STAGE1CLASS002`
- unsupported host primitive -> `STAGE1CLASS003`
- invalid token classifier or stream -> `STAGE1CLASS004`
- stage0 form divergence -> `STAGE1CLASS005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-token-classifier-pipeline-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:token-classifier-pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-token-classifier-pipeline-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :tokens-from-characters-host-primitive-removed?])) (println (get-in a [:capability-based-proof :scan-tokens-host-primitive-removed?])) (println (get-in a [:capability-based-proof :whole-reader-host-primitive-removed?])) (println (get-in a [:capability-based-proof :limitations :host-character-stream?])) (println (get-in a [:capability-based-proof :limitations :host-token-realizer?])) (println (get-in a [:capability-based-proof :limitations :host-form-builder?])))'
:gravity/stage1-reader-token-classifier-pipeline-artifact
sha256:f6f66393a40bdf19fe3cab497347fca1e33875e49beee2a868954de6f8ed2ef9
sha256:beb095efaa5aae99a82c1483d8c326fe4a1abf3e3e0d4932b29eb711f8e8ee1d
[:reader/source-characters :reader/tokens-from-classifier :reader/forms-from-tokens]
506
82
4
true
10
:complete
true
true
true
true
true
true

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader token-classifier pipeline
entrypoint is executable by the seed evaluator and can drive accepted and
rejected behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, or `:reader/tokens-from-characters` host primitives. The
artifact records character coverage, token-classifier coverage, token coverage,
form parity, source spans, diagnostics, and the remaining trusted host
boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, and token
classifier are authored in Gravity, but the seed evaluator, character
extraction, token realizer, and form builder are still Clojure host primitives.
The next capability gate is to replace `:reader/tokens-from-classifier` and
`:reader/forms-from-tokens` with executable Gravity code while preserving
accepted forms, rejected diagnostics, source spans, character coverage, token
coverage, token-classifier coverage, artifact provenance, and stage0 parity.

Follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records a newer bridge that removes `:reader/tokens-from-classifier` from the
latest bridge by introducing a Gravity-authored token realizer specification.
The next gate after that bridge is replacing the token realizer executor and
form builder while preserving the same accepted/rejected behavior.
