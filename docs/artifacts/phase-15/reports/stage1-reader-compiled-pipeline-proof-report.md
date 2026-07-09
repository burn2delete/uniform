# Stage1 Reader Compiled Pipeline Proof Report

Status: complete for the stage1 reader compiled pipeline bridge

This report records the twelfth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-reader-compiled-program` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity` and executes that
Gravity-authored instruction stream for the
`stage1-read-source-compiled-pipeline` entrypoint. This replaces the Clojure
runtime interpreter for the latest reader bridge, while keeping the Clojure
instruction executor, Clojure character-stream implementation, and Clojure seed
builtins explicit.

## Capability

```bash
clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-compiled-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-compiled-pipeline-artifact` with:

- artifact id `sha256:db1fb73650644a782cfa6b55854fa47de568f0feab94e24a8dad95811432eefb`
- compiled pipeline id `sha256:96fedf1626e1d750f6a3c684a1ba86ceb9c355220ea5f8e0e4a7a2b87fdd0e27`
- Gravity entrypoint `stage1-read-source-compiled-pipeline`
- compiled program engine `:gravity-reader-compiled-program-v1`
- compiled-from entrypoint `:stage1-read-source-runtime-pipeline`
- instruction operations `[:stage1-create-character-stream :stage1-execute-token-automaton :stage1-execute-form-builder]`
- explicit host primitive boundary `[]`
- Gravity runtimes `[:stage1-reader-source-runtime]`
- Gravity executors `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
- trusted boundary `{:clojure-runtime-interpreter? false, :clojure-instruction-executor? true, :clojure-character-stream-implementation? true}`
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across compiled-pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored compiled reader
program. The program creates the source-character stream through the
Gravity-authored source runtime record, executes the Gravity-authored token
automaton executor record, then executes the Gravity-authored form-builder
executor record. The emitted form records preserve source spans and stage0 form
parity.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed compiled-pipeline diagnostics:

- missing Gravity reader compiled-pipeline entrypoint -> `STAGE1COMP001`
- unsupported executable Gravity form -> `STAGE1COMP002`
- unsupported host primitive -> `STAGE1COMP003`
- invalid compiled reader program -> `STAGE1COMP004`
- stage0 form divergence -> `STAGE1COMP005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-compiled-pipeline-proof.edn")) p (:capability-based-proof a)] (println (:kind a)) (println (:artifact-id a)) (println (:compiled-pipeline-id a)) (println (:host-primitives a)) (println (:gravity-runtimes a)) (println (:gravity-executors a)) (println (get-in a [:stage1-reader-compiled-program :engine])) (println (mapv :op (get-in a [:stage1-reader-compiled-program :instructions]))) (println (get-in a [:trusted-boundary :clojure-runtime-interpreter?])) (println (get-in a [:trusted-boundary :clojure-instruction-executor?])) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (:runtime-interpreter-replaced? p)) (println (get-in p [:limitations :clojure-instruction-executor?])) (println (get-in p [:limitations :clojure-seed-retired?])) (println (:status p)))'
:gravity/stage1-reader-compiled-pipeline-artifact
sha256:db1fb73650644a782cfa6b55854fa47de568f0feab94e24a8dad95811432eefb
sha256:96fedf1626e1d750f6a3c684a1ba86ceb9c355220ea5f8e0e4a7a2b87fdd0e27
[]
[:stage1-reader-source-runtime]
[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]
:gravity-reader-compiled-program-v1
[:stage1-create-character-stream :stage1-execute-token-automaton :stage1-execute-form-builder]
false
true
506
82
4
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

This bridge proves that the latest reader capability can be driven by a
Gravity-authored compiled reader instruction stream rather than by the Clojure
runtime interpreter over the runtime-pipeline function body. The artifact
records compiled program coverage, source runtime coverage, executor coverage,
character coverage, token coverage, form parity, source spans, diagnostics, and
trusted-boundary limitations.

## Residual Risks

This is not a self-hosted reader or self-hosted compiler. The compiled program,
source runtime, token classifier, token realizer, token automaton,
token-automaton executor, form-builder, and form-builder executor are authored
in Gravity, but the Clojure instruction executor, Clojure character-stream
implementation, and Clojure seed builtins are still trusted Clojure code. The
next capability gate is to replace the Clojure instruction executor with a
Gravity-emitted reader binary while preserving accepted forms, rejected
diagnostics, source spans, character coverage, token-classifier coverage,
token-realizer coverage, token-automaton coverage, executor coverage,
form-builder coverage, token coverage, artifact provenance, and stage0 parity.
