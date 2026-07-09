# Stage1 Reader Form Builder Pipeline Proof Report

Status: complete for the stage1 reader form-builder pipeline bridge

This report records the ninth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-form-builder-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records a
Gravity-authored form-builder specification between the token stream and form
records.

## Capability

```bash
clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-form-builder-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-form-builder-pipeline-artifact`
with:

- artifact id `sha256:22222fc53b27462aaaa0e00b95e3e4e9b8222e3adea1305296b295ea8d03b7c3`
- form-builder pipeline id `sha256:3c808541c302db26d7feeee8c5ab652f9f0617b285c8fb4d4f3c6b1a47b6f48d`
- Gravity entrypoint `stage1-read-source-form-builder-pipeline`
- explicit host primitive boundary `[:reader/source-characters :reader/run-token-automaton :reader/build-forms]`
- no `:reader/forms-from-tokens`, `:reader/realize-tokens`,
  `:reader/tokens-from-classifier`, `:reader/tokens-from-characters`,
  `:reader/scan-tokens`, or `:reader/read-with-table` host primitive in this
  bridge
- Gravity-authored token classifier engine `:gravity-reader-token-classifier-v1`
- Gravity-authored token realizer engine `:gravity-reader-token-realizer-v1`
- Gravity-authored token automaton engine `:gravity-reader-token-automaton-v1`
- Gravity-authored form builder engine `:gravity-reader-form-builder-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across form-builder-pipeline and malformed-input
  failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored form-builder pipeline
entrypoint. The pipeline emits a character-stream artifact, applies the
Gravity-authored token classifier, token realizer, token automaton, and
form-builder specifications, then emits form records through the explicit
form-builder executor boundary.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed form-builder-pipeline diagnostics:

- missing Gravity reader form-builder-pipeline entrypoint -> `STAGE1FORM001`
- unsupported executable Gravity form -> `STAGE1FORM002`
- unsupported host primitive -> `STAGE1FORM003`
- invalid form builder or form stream -> `STAGE1FORM004`
- stage0 form divergence -> `STAGE1FORM005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-form-builder-pipeline-proof.edn")) p (:capability-based-proof a)] (println (:kind a)) (println (:artifact-id a)) (println (:form-builder-pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-token-classifier :engine])) (println (get-in a [:stage1-reader-token-realizer :engine])) (println (get-in a [:stage1-reader-token-automaton :engine])) (println (get-in a [:stage1-reader-form-builder :engine])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-form-builder-pipeline-diagnostic-stream :diagnostics]))) (println (:forms-from-tokens-host-primitive-removed? p)) (println (:realize-tokens-host-primitive-removed? p)) (println (:tokens-from-classifier-host-primitive-removed? p)) (println (:tokens-from-characters-host-primitive-removed? p)) (println (:scan-tokens-host-primitive-removed? p)) (println (:whole-reader-host-primitive-removed? p)) (println (get-in p [:limitations :clojure-seed-retired?])) (println (:status p)))'
:gravity/stage1-reader-form-builder-pipeline-artifact
sha256:22222fc53b27462aaaa0e00b95e3e4e9b8222e3adea1305296b295ea8d03b7c3
sha256:3c808541c302db26d7feeee8c5ab652f9f0617b285c8fb4d4f3c6b1a47b6f48d
[:reader/source-characters :reader/run-token-automaton :reader/build-forms]
:gravity-reader-token-classifier-v1
:gravity-reader-token-realizer-v1
:gravity-reader-token-automaton-v1
:gravity-reader-form-builder-v1
506
82
4
true
10
true
true
true
true
true
true
false
:complete

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader form-builder pipeline
entrypoint is executable by the seed evaluator and can drive accepted and
rejected behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, `:reader/tokens-from-characters`,
`:reader/tokens-from-classifier`, `:reader/realize-tokens`, or
`:reader/forms-from-tokens` host primitives. The artifact records character
coverage, token-classifier coverage, token-realizer coverage, token-automaton
coverage, form-builder coverage, token coverage, form parity, source spans,
diagnostics, and the remaining trusted host boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, token
classifier, token realizer specification, token automaton specification, and
form-builder specification are authored in Gravity, but the seed evaluator,
character extraction, generic token automaton executor, and form-builder
executor are still Clojure host primitives. The next capability gate is to
replace `:reader/run-token-automaton` and `:reader/build-forms` with executable
Gravity code while preserving accepted forms, rejected diagnostics, source
spans, character coverage, token-classifier coverage, token-realizer coverage,
token-automaton coverage, form-builder coverage, token coverage, artifact
provenance, and stage0 parity.
