# Stage1 Reader Token Realizer Pipeline Proof Report

Status: complete for the stage1 reader token-realizer pipeline bridge

This report records the seventh post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-token-realizer-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records a
Gravity-authored token realizer specification between token classification and
token-stream construction.

## Capability

```bash
clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-token-realizer-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-token-realizer-pipeline-artifact`
with:

- artifact id `sha256:2c337ddfa6260586e3812cc20378ba83e460c740808b0d42098cb946e0730452`
- token-realizer pipeline id `sha256:aff56913c209ab951b76d978c2e55bc0e5f5ba8953787698aa4a3d260020003c`
- Gravity entrypoint `stage1-read-source-token-realizer-pipeline`
- explicit host primitive boundary `[:reader/source-characters :reader/realize-tokens :reader/forms-from-tokens]`
- no `:reader/tokens-from-classifier`, `:reader/tokens-from-characters`,
  `:reader/scan-tokens`, or `:reader/read-with-table` host primitive in this
  bridge
- Gravity-authored token classifier engine `:gravity-reader-token-classifier-v1`
- Gravity-authored token realizer engine `:gravity-reader-token-realizer-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across token-realizer-pipeline and malformed-input
  failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored token-realizer
pipeline entrypoint. The pipeline emits a character-stream artifact, applies
the Gravity-authored token classifier and token realizer specification, then
emits the token-stream artifact before form construction.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed token-realizer-pipeline diagnostics:

- missing Gravity reader token-realizer-pipeline entrypoint -> `STAGE1REAL001`
- unsupported executable Gravity form -> `STAGE1REAL002`
- unsupported host primitive -> `STAGE1REAL003`
- invalid token realizer or stream -> `STAGE1REAL004`
- stage0 form divergence -> `STAGE1REAL005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-token-realizer-pipeline-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:token-realizer-pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-token-classifier :engine])) (println (get-in a [:stage1-reader-token-realizer :engine])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-token-realizer-pipeline-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :tokens-from-classifier-host-primitive-removed?])) (println (get-in a [:capability-based-proof :tokens-from-characters-host-primitive-removed?])) (println (get-in a [:capability-based-proof :scan-tokens-host-primitive-removed?])) (println (get-in a [:capability-based-proof :whole-reader-host-primitive-removed?])) (println (get-in a [:capability-based-proof :limitations :host-character-stream?])) (println (get-in a [:capability-based-proof :limitations :host-token-realizer-executor?])) (println (get-in a [:capability-based-proof :limitations :host-form-builder?])))'
:gravity/stage1-reader-token-realizer-pipeline-artifact
sha256:2c337ddfa6260586e3812cc20378ba83e460c740808b0d42098cb946e0730452
sha256:aff56913c209ab951b76d978c2e55bc0e5f5ba8953787698aa4a3d260020003c
[:reader/source-characters :reader/realize-tokens :reader/forms-from-tokens]
:gravity-reader-token-classifier-v1
:gravity-reader-token-realizer-v1
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
true

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader token-realizer pipeline
entrypoint is executable by the seed evaluator and can drive accepted and
rejected behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, `:reader/tokens-from-characters`, or
`:reader/tokens-from-classifier` host primitives. The artifact records
character coverage, token-classifier coverage, token-realizer coverage, token
coverage, form parity, source spans, diagnostics, and the remaining trusted host
boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, token
classifier, and token realizer specification are authored in Gravity, but the
seed evaluator, character extraction, token realizer executor, and form builder
are still Clojure host primitives. The follow-on
`stage1-reader-token-automaton-pipeline` bridge replaces
`:reader/realize-tokens`; the current remaining gate is to replace the generic
token automaton executor and form-builder executor with executable Gravity
code while preserving accepted forms, rejected diagnostics, source spans,
character coverage, token-classifier coverage, token-realizer coverage,
token-automaton coverage, token coverage, artifact provenance, and stage0
parity.
