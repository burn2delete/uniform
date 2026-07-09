# Stage1 Reader Executor Pipeline Proof Report

Status: complete for the stage1 reader executor pipeline bridge

This report records the tenth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-executor-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records
Gravity-authored executor records for token-automaton execution and form
building.

## Capability

```bash
clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-executor-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-executor-pipeline-artifact` with:

- artifact id `sha256:b050a9667a920761a393979896563be807ad63cd8d2702dc1eb32720ae77b57c`
- executor pipeline id `sha256:d3c62b202ced50f9b7a160514ae07c5624a42dc2a047e8a8b07d5621fb70697d`
- Gravity entrypoint `stage1-read-source-executor-pipeline`
- explicit host primitive boundary `[:reader/source-characters]`
- Gravity executors `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- no `:reader/run-token-automaton`, `:reader/build-forms`,
  `:reader/forms-from-tokens`, `:reader/realize-tokens`,
  `:reader/tokens-from-classifier`, `:reader/tokens-from-characters`,
  `:reader/scan-tokens`, or `:reader/read-with-table` host primitive in this
  bridge
- Gravity-authored token classifier engine `:gravity-reader-token-classifier-v1`
- Gravity-authored token realizer engine `:gravity-reader-token-realizer-v1`
- Gravity-authored token automaton engine `:gravity-reader-token-automaton-v1`
- Gravity-authored form builder engine `:gravity-reader-form-builder-v1`
- Gravity-authored token automaton executor engine
  `:gravity-reader-token-automaton-executor-v1`
- Gravity-authored form builder executor engine
  `:gravity-reader-form-builder-executor-v1`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across executor-pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored executor pipeline
entrypoint. The pipeline emits a character-stream artifact, applies the
Gravity-authored token classifier, token realizer, token automaton, token
automaton executor, form builder, and form-builder executor records, then emits
form records with source spans and stage0 parity evidence.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed executor-pipeline diagnostics:

- missing Gravity reader executor-pipeline entrypoint -> `STAGE1EXEC001`
- unsupported executable Gravity form -> `STAGE1EXEC002`
- unsupported host primitive -> `STAGE1EXEC003`
- invalid executor or stream -> `STAGE1EXEC004`
- stage0 form divergence -> `STAGE1EXEC005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-executor-pipeline-proof.edn")) p (:capability-based-proof a)] (println (:kind a)) (println (:artifact-id a)) (println (:executor-pipeline-id a)) (println (:host-primitives a)) (println (:gravity-executors a)) (println (get-in a [:stage1-reader-token-automaton-executor :engine])) (println (get-in a [:stage1-reader-form-builder-executor :engine])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (:run-token-automaton-host-primitive-removed? p)) (println (:build-forms-host-primitive-removed? p)) (println (:forms-from-tokens-host-primitive-removed? p)) (println (:whole-reader-host-primitive-removed? p)) (println (get-in p [:limitations :clojure-seed-retired?])) (println (:status p)))'
:gravity/stage1-reader-executor-pipeline-artifact
sha256:b050a9667a920761a393979896563be807ad63cd8d2702dc1eb32720ae77b57c
sha256:d3c62b202ced50f9b7a160514ae07c5624a42dc2a047e8a8b07d5621fb70697d
[:reader/source-characters]
[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]
:gravity-reader-token-automaton-executor-v1
:gravity-reader-form-builder-executor-v1
506
82
4
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

This bridge proves that a Gravity-authored reader executor pipeline entrypoint
is executable by the seed evaluator and can drive accepted and rejected
behavior without using the older `:reader/read-with-table`,
`:reader/scan-tokens`, `:reader/tokens-from-characters`,
`:reader/tokens-from-classifier`, `:reader/realize-tokens`,
`:reader/forms-from-tokens`, `:reader/run-token-automaton`, or
`:reader/build-forms` host primitives. The artifact records character
coverage, token-classifier coverage, token-realizer coverage, token-automaton
coverage, executor coverage, form-builder coverage, token coverage, form
parity, source spans, diagnostics, and the remaining trusted host boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint, pipeline shape, token
classifier, token realizer specification, token automaton specification,
token-automaton executor record, form-builder specification, and form-builder
executor record are authored in Gravity, but the seed evaluator, character
extraction, and Clojure seed builtins are still trusted Clojure code. The next
capability gate is to replace the host character stream and seed evaluator with
a Gravity reader runtime while preserving accepted forms, rejected diagnostics,
source spans, character coverage, token-classifier coverage, token-realizer
coverage, token-automaton coverage, executor coverage, form-builder coverage,
token coverage, artifact provenance, and stage0 parity.
