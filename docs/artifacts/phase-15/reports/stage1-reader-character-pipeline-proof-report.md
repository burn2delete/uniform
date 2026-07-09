# Stage1 Reader Character Pipeline Proof Report

Status: complete for the stage1 reader character pipeline bridge

This report records the fifth post-stage0 self-hosting reduction step. The
Clojure seed now loads `stage1-read-source-character-pipeline` from
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, executes that
Gravity-authored entrypoint through a seed evaluator, and records character,
token, and form construction stages as separate host primitives.

## Capability

```bash
clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity > docs/artifacts/phase-15/bootstrap/stage1-reader-character-pipeline-proof.edn
```

The command emits a `:gravity/stage1-reader-character-pipeline-artifact` with:

- artifact id `sha256:8b43cf24bd480151bbd677bca5e5088360bd1d755313b6d2f96e767040b0bf82`
- character pipeline id `sha256:4ed8a53b93b733210daa9c0b827522c88cf9ae923589a2e4b2567d2125e6b107`
- Gravity entrypoint `stage1-read-source-character-pipeline`
- explicit host primitive boundary `[:reader/source-characters :reader/tokens-from-characters :reader/forms-from-tokens]`
- no `:reader/scan-tokens` host primitive in this bridge
- 506 character records with source spans
- 82 token records with source spans
- 4 top-level forms read from the accepted fixture
- form parity with the stage0 reader
- 10 stable diagnostics across character-pipeline and malformed-input failures

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`

The accepted fixture is read through the Gravity-authored character pipeline
entrypoint. The pipeline emits a character-stream artifact before tokenization,
then emits the existing token-stream artifact before form construction.

## Rejected Fixtures

- `stage1-reader-unexpected-close.gravity` -> `STAGE1READER001`
- `stage1-reader-unclosed-list.gravity` -> `STAGE1READER002`
- `stage1-reader-unclosed-string.gravity` -> `STAGE1READER003`
- `stage1-reader-unsupported-dispatch.gravity` -> `STAGE1READER004`
- `stage1-reader-odd-map.gravity` -> `STAGE1READER005`

The artifact also records fail-closed character-pipeline diagnostics:

- missing Gravity reader character-pipeline entrypoint -> `STAGE1CHAR001`
- unsupported executable Gravity form -> `STAGE1CHAR002`
- unsupported host primitive -> `STAGE1CHAR003`
- invalid character or token stream -> `STAGE1CHAR004`
- stage0 form divergence -> `STAGE1CHAR005`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-reader-character-pipeline-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:character-pipeline-id a)) (println (:host-primitives a)) (println (get-in a [:stage1-reader-character-stream :character-count])) (println (get-in a [:stage1-reader-token-stream :token-count])) (println (count (:stage1-reader-records a))) (println (get-in a [:stage0-comparison :forms-equal?])) (println (count (get-in a [:stage1-reader-character-pipeline-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :scan-tokens-host-primitive-removed?])) (println (get-in a [:capability-based-proof :limitations :host-character-stream?])) (println (get-in a [:capability-based-proof :limitations :host-tokenizer?])) (println (get-in a [:capability-based-proof :limitations :host-form-builder?])))'
:gravity/stage1-reader-character-pipeline-artifact
sha256:8b43cf24bd480151bbd677bca5e5088360bd1d755313b6d2f96e767040b0bf82
sha256:4ed8a53b93b733210daa9c0b827522c88cf9ae923589a2e4b2567d2125e6b107
[:reader/source-characters :reader/tokens-from-characters :reader/forms-from-tokens]
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

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

This bridge proves that a Gravity-authored reader character pipeline entrypoint
is executable by the seed evaluator and can drive accepted and rejected behavior
without using the older `:reader/read-with-table` or `:reader/scan-tokens` host
primitives. The artifact records character coverage, token coverage, form
parity, source spans, diagnostics, and the remaining trusted host boundary.

## Residual Risks

This is not a self-hosted reader. The entrypoint and pipeline shape are authored
in Gravity, but the seed evaluator, character extraction, tokenizer, and form
builder are still Clojure host primitives. The next capability gate is to
replace `:reader/tokens-from-characters` and `:reader/forms-from-tokens` with
executable Gravity code while preserving accepted forms, rejected diagnostics,
source spans, character coverage, token coverage, artifact provenance, and
stage0 parity.

Follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-classifier-pipeline-proof-report.md`
records a newer bridge that removes `:reader/tokens-from-characters` from the
latest bridge by introducing a Gravity-authored token classifier. The next gate
after that bridge is replacing the token realizer and form builder while
preserving the same accepted/rejected behavior.

Latest follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records a newer bridge that removes `:reader/tokens-from-classifier` from the
latest bridge by introducing a Gravity-authored token realizer specification.
The next gate after that bridge is replacing the token realizer executor and
form builder while preserving the same accepted/rejected behavior.
