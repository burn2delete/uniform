# Stage1 Bootstrap Source Proof Report

Status: complete for the stage1 source bridge

This report records the first post-stage0 self-hosting reduction step. The
Clojure seed verifies Gravity-authored bootstrap source modules under
`bootstrap/gravity/src`; it does not execute those modules without Clojure
compiler logic yet.

## Capability

```bash
clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src > docs/artifacts/phase-15/bootstrap/stage1-bootstrap-source-proof.edn
```

The command emits a `:gravity/stage1-bootstrap-source-artifact` with:

- artifact id `sha256:7263046a97e26755f0b26a62d9eee2ef42e3a7dacb1c528e7d42ab7286346483`
- source set id `sha256:f97ce250a89ba1e6f9143073de86d078ac2b05bb6ffccf4f4ef73ab9bbb3f064`
- 3 Gravity-authored modules: `:reader`, `:syntax`, and `:diagnostics`
- 6 stable fail-closed diagnostics: `STAGE1001` through `STAGE1006`
- a seed boundary that records `:clojure-stage0` as still trusted

## Accepted Source

- `bootstrap/gravity/src/gravity/bootstrap/reader.gravity`
- `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity`
- `bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity`

Each module declares the `:meta` profile, no effects, no capabilities, Gravity
source ownership, Clojure seed lineage, the replacement objective, required
document coverage, and preservation of source spans, syntax identity,
diagnostic codes, and artifact provenance.

## Rejected Fixtures

- `stage1-bootstrap-missing-owner.gravity` -> `STAGE1001`
- `stage1-bootstrap-wrong-profile.gravity` -> `STAGE1002`
- `stage1-bootstrap-ambient-authority.gravity` -> `STAGE1003`
- `stage1-bootstrap-missing-lineage.gravity` -> `STAGE1004`
- `stage1-bootstrap-missing-preserved-fact.gravity` -> `STAGE1005`
- `stage1-bootstrap-incomplete-source-set.gravity` -> `STAGE1006`

## Validation Commands

```text
$ clojure -M -e '(require (quote [clojure.edn :as edn])) (let [a (edn/read-string (slurp "docs/artifacts/phase-15/bootstrap/stage1-bootstrap-source-proof.edn"))] (println (:kind a)) (println (:artifact-id a)) (println (:source-set-id a)) (println (count (:modules a))) (println (sort (set (map :component (:modules a))))) (println (count (get-in a [:stage1-diagnostic-stream :diagnostics]))) (println (:status (:capability-based-proof a))) (println (get-in a [:capability-based-proof :limitations :clojure-seed-retired?])))'
:gravity/stage1-bootstrap-source-artifact
sha256:7263046a97e26755f0b26a62d9eee2ef42e3a7dacb1c528e7d42ab7286346483
sha256:f97ce250a89ba1e6f9143073de86d078ac2b05bb6ffccf4f4ef73ab9bbb3f064
3
(:diagnostics :reader :syntax)
6
:complete
false

$ clojure -M:test
Ran 134 tests containing 8388 assertions.
0 failures, 0 errors.
```

## Conformance Argument

The stage1 bridge proves that the next bootstrap layer has real Gravity source
for the reader, syntax, and diagnostics components and that the Clojure seed can
read and verify that source against Phase 15, C2, C3, and C15 obligations. The
negative fixtures prove the bridge rejects missing ownership, wrong profiles,
ambient authority, missing lineage, missing preserved compiler facts, and
incomplete source sets.

## Residual Risks

This is not a completed self-hosted compiler. The proof explicitly records that
the Clojure seed is still trusted and not retired. The next capability gate is
to execute the Gravity-authored reader without Clojure compiler logic and
compare diagnostics, source spans, and artifacts against this bridge.

Follow-on evidence: `docs/artifacts/phase-15/reports/stage1-reader-execution-proof-report.md`
records a reader-table execution bridge. It still uses a Clojure-hosted
interpreter, so the next gate remains moving the reader algorithm itself into
executable Gravity source.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-algorithm-proof-report.md`
records a Gravity-authored reader entrypoint executed by the seed evaluator. It
still uses `:reader/read-with-table` as a host primitive.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-pipeline-proof-report.md`
records a Gravity-authored reader pipeline entrypoint executed by the seed
evaluator. It splits the old whole-reader primitive into token scanning and
form-building host primitives, but still does not retire those host primitives.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-character-pipeline-proof-report.md`
records a Gravity-authored character pipeline entrypoint executed by the seed
evaluator. It removes `:reader/scan-tokens` from that bridge while preserving
stage0 form parity.

Additional follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-classifier-pipeline-proof-report.md`
records a Gravity-authored token-classifier pipeline entrypoint executed by the
seed evaluator. It removes `:reader/tokens-from-characters` from the latest
bridge while keeping the token realizer and form builder explicit.

Follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-realizer-pipeline-proof-report.md`
records a Gravity-authored token-realizer pipeline entrypoint executed by the
seed evaluator. It removes `:reader/tokens-from-classifier` while keeping the
token realizer executor and form builder explicit.

Follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-token-automaton-pipeline-proof-report.md`
records a Gravity-authored token-automaton pipeline entrypoint executed by the
seed evaluator. It removes `:reader/realize-tokens` while keeping the token
automaton executor and form builder explicit.

Latest follow-on evidence:
`docs/artifacts/phase-15/reports/stage1-reader-form-builder-pipeline-proof-report.md`
records a Gravity-authored form-builder pipeline entrypoint executed by the
seed evaluator. It removes `:reader/forms-from-tokens` while keeping the token
automaton executor and form-builder executor explicit.
