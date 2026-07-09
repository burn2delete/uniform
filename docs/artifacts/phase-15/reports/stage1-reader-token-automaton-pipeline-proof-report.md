# Stage1 Reader Token Automaton Pipeline Proof Report

Status: complete for the stage1 reader token-automaton pipeline bridge

This report records the eighth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-token-automaton-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records a
Gravity-authored token automaton between token realization metadata and
token-stream construction.

## Capability

```bash
clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-token-automaton-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-token-automaton-pipeline-artifact`
with:

- artifact id `sha256:74824ebd28764bf36a86fd4001b98a7bdfc0470599bfe0112b4811e8fb96c189`
- token-automaton pipeline id `sha256:632cfcd1362f0ef2d6762a0b692b8d662630c7db08a0ca37aaf8c1ecabbc4da0`
- Gravity entrypoint `stage1-read-source-token-automaton-pipeline`
- explicit host primitive boundary `[:reader/source-characters :reader/run-token-automaton :reader/forms-from-tokens]`
- no `:reader/realize-tokens`, `:reader/tokens-from-classifier`,
  `:reader/tokens-from-characters`, `:reader/scan-tokens`, or
  `:reader/read-with-table` host primitive in this bridge
- Gravity-authored token classifier engine `:gravity-reader-token-classifier-v1`
- Gravity-authored token realizer engine `:gravity-reader-token-realizer-v1`
- Gravity-authored token automaton engine `:gravity-reader-token-automaton-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across token-automaton-pipeline and malformed-input
  failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored token-automaton
pipeline entrypoint. The pipeline emits a character-stream artifact, applies
the Gravity-authored token classifier, token realizer, and token automaton, then
emits the token-stream artifact before form construction.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed token-automaton-pipeline diagnostics:

- missing Gravity reader token-automaton-pipeline entrypoint -> `STAGE1AUTO001`
- unsupported executable Gravity form -> `STAGE1AUTO002`
- unsupported host primitive -> `STAGE1AUTO003`
- invalid token automaton or stream -> `STAGE1AUTO004`
- stage0 form divergence -> `STAGE1AUTO005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-token-automaton-pipeline-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:token-automaton-pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-token-classifier :engine])) (println (get-in a [:stage1-reader-token-realizer :engine])) (println (get-in a [:stage1-reader-token-automaton :engine])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-token-automaton-pipeline-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :realize-tokens-host-primitive-removed?])) (println (get-in a [:capability-based-proof :tokens-from-classifier-host-primitive-removed?])) (println (get-in a [:capability-based-proof :tokens-from-characters-host-primitive-removed?])) (println (get-in a [:capability-based-proof :scan-tokens-host-primitive-removed?])) (println (get-in a [:capability-based-proof :whole-reader-host-primitive-removed?])) (println (get-in a [:capability-based-proof :limitations :host-character-stream?])) (println (get-in a [:capability-based-proof :limitations :host-token-automaton-executor?])) (println (get-in a [:capability-based-proof :limitations :host-form-builder?])))'
:gravity/stage1-reader-token-automaton-pipeline-artifact
sha256:74824ebd28764bf36a86fd4001b98a7bdfc0470599bfe0112b4811e8fb96c189
sha256:632cfcd1362f0ef2d6762a0b692b8d662630c7db08a0ca37aaf8c1ecabbc4da0
[:reader/source-characters :reader/run-token-automaton :reader/forms-from-tokens]
:gravity-reader-token-classifier-v1
:gravity-reader-token-realizer-v1
:gravity-reader-token-automaton-v1
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
true

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader token-automaton pipeline
entrypoint is executable by the seed evaluator and can drive accepted and
rejected behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, `:reader/tokens-from-characters`,
`:reader/tokens-from-classifier`, or `:reader/realize-tokens` host primitives.
The artifact records character coverage, token-classifier coverage,
token-realizer coverage, token-automaton coverage, token coverage, form parity,
source spans, diagnostics, and the remaining trusted host boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, token
classifier, token realizer specification, and token automaton specification are
authored in Gravity, but the seed evaluator, character extraction, generic
token automaton executor, and form builder are still Clojure host primitives.
The follow-on `stage1-reader-form-builder-pipeline` bridge replaces
`:reader/forms-from-tokens`; the current remaining gate is to replace the
generic token automaton executor and form-builder executor with executable
Gravity code while preserving accepted forms, rejected diagnostics, source
spans, character coverage, token-classifier coverage, token-realizer coverage,
token-automaton coverage, form-builder coverage, token coverage, artifact
provenance, and stage0 parity.
