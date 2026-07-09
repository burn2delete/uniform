# Stage1 Reader Binary Pipeline Proof Report

Status: complete for the stage1 reader binary pipeline bridge

This report records the thirteenth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-reader-emitted-binary` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity` and runs its direct
reader stages for the `stage1-read-source-binary-pipeline` entrypoint. This
replaces the generic Clojure instruction executor for the latest reader bridge,
while keeping the Clojure binary runner, Clojure character-stream
implementation, and Clojure seed builtins explicit.

## Capability

```bash
clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-binary-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-binary-pipeline-artifact` with:

- artifact id `sha256:67cda07402a33ea0add45d091d50e2bb6fbe989ebff8ccb9def73f1ec7610106`
- binary pipeline id `sha256:0a3d82b4e16b107c984237377d4d7876b9f22337d3faa464ab08e645d4bca292`
- reader binary id `sha256:a1a3234940234ddcb6a638a6b666a7beefb55d7cdc64c967a85957ee18a96157`
- Gravity entrypoint `stage1-read-source-binary-pipeline`
- emitted binary engine `:gravity-reader-binary-v1`
- emitted-from link `:stage1-reader-compiled-program`
- direct binary stages `[:stage1-binary-create-character-stream :stage1-binary-execute-token-automaton :stage1-binary-execute-form-builder]`
- explicit host primitive boundary `[]`
- Gravity runtimes `[:stage1-reader-source-runtime]`
- Gravity executors `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- trusted boundary `{:clojure-runtime-interpreter? false, :clojure-instruction-executor? false, :clojure-binary-runner? true, :clojure-character-stream-implementation? true}`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across binary-pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored emitted reader binary.
The binary directly runs the Gravity-authored source runtime, token automaton
executor, and form-builder executor records. The emitted form records preserve
source spans and stage0 form parity.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed binary-pipeline diagnostics:

- missing Gravity reader binary-pipeline entrypoint -> `STAGE1BIN001`
- unsupported executable Gravity form -> `STAGE1BIN002`
- unsupported host primitive -> `STAGE1BIN003`
- invalid emitted reader binary -> `STAGE1BIN004`
- stage0 form divergence -> `STAGE1BIN005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-binary-pipeline-proof.edn")) p (:capability-based-proof a)] (println (:kind a)) (println (:artifact-id a)) (println (:binary-pipeline-id a)) (println (:reader-binary-id a)) (println (:gravity-entrypoint a)) (println (:host-primitives a)) (println (:gravity-runtimes a)) (println (:gravity-executors a)) (println (get-in a [:stage1-reader-emitted-binary :engine])) (println (get-in a [:stage1-reader-emitted-binary :emitted-from])) (println (mapv :op (get-in a [:stage1-reader-emitted-binary :direct-stages]))) (println (get-in a [:trusted-boundary :clojure-runtime-interpreter?])) (println (get-in a [:trusted-boundary :clojure-instruction-executor?])) (println (get-in a [:trusted-boundary :clojure-binary-runner?])) (println (get-in a [:trusted-boundary :clojure-character-stream-implementation?])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (:runtime-interpreter-replaced? p)) (println (:instruction-executor-replaced? p)) (println (:binary-runner-boundary-explicit? p)) (println (get-in p [:limitations :clojure-seed-retired?])) (println (get-in p [:limitations :next-required-capability])) (println (:status p)))'
:gravity/stage1-reader-binary-pipeline-artifact
sha256:67cda07402a33ea0add45d091d50e2bb6fbe989ebff8ccb9def73f1ec7610106
sha256:0a3d82b4e16b107c984237377d4d7876b9f22337d3faa464ab08e645d4bca292
sha256:a1a3234940234ddcb6a638a6b666a7beefb55d7cdc64c967a85957ee18a96157
stage1-read-source-binary-pipeline
[]
[:stage1-reader-source-runtime]
[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]
:gravity-reader-binary-v1
:stage1-reader-compiled-program
[:stage1-binary-create-character-stream :stage1-binary-execute-token-automaton :stage1-binary-execute-form-builder]
false
false
true
true
506
82
4
true
true
true
true
false
:replace-clojure-binary-runner-and-character-stream-with-self-hosted-reader-runtime
:complete

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that the latest reader capability can be driven by a
Gravity-authored emitted binary plan rather than by the Clojure generic
instruction executor over the compiled reader program. The artifact records
binary provenance, compiled-program linkage, direct-stage coverage, source
runtime coverage, executor coverage, character coverage, token coverage, form
parity, source spans, diagnostics, and trusted-boundary limitations.

## Residual Risks

This is not a self-hosted reader or self-hosted compiler. The emitted binary,
compiled program, source runtime, token classifier, token realizer, token
automaton, token-automaton executor, form-builder, and form-builder executor
are authored in Gravity, but the Clojure binary runner, Clojure
character-stream implementation, and Clojure seed builtins are still trusted
Clojure code. The next capability gate is to replace the Clojure binary runner
and character-stream implementation with a self-hosted reader runtime while
preserving accepted forms, rejected diagnostics, source spans, character
coverage, token-classifier coverage, token-realizer coverage, token-automaton
coverage, executor coverage, form-builder coverage, token coverage, artifact
provenance, and stage0 parity.
