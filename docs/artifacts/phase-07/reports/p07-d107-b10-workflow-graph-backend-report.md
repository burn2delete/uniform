# P07-D107 B10 Workflow Graph Backend Proof Report

Date: 2026-06-29
Task: `P07-D107`
Status: complete (stage0 B10 workflow graph backend document coverage)

## Governing Document Read

- `docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b10-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-d107-b10-workflow-graph-backend-proof.edn`

The `backend-b10-workflow-document` command emits
`:gravity/stage0-b10-workflow-graph-backend-document-artifact` from the current
P07-T04 specialized lowering artifact. It records B10 workflow IR handoff,
durable workflow graph output, step schemas, event-log schemas, replay policy
and replay fixtures, idempotency records, retry/timeout/cancellation/
compensation records, external capability manifests, model/tool provider
manifests, human-review policy graphs, policy graphs, taint validation, audit
provenance, source/debug maps, differential replay records, B10 diagnostics,
document-specific results, and capability-based proof.

## Validation

```text
clojure -M:gravity backend-b10-workflow-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-b10-workflow-graph-backend-document-artifact,
 :task "P07-D107",
 :artifact-id "sha256:ac08c37a5a2af599ef264f82d3c1484950f8c791d80fa8fd0734633c13603491",
 :document-set ["B10"],
 :diagnostics 10,
 :rejected-designs 5,
 :conformance-criteria 9,
 :workflow-graph-structural true,
 :replay-fixture-structural true,
 :differential-replay true,
 :external-runtime :not-available-in-current-environment,
 :proof :complete}
```

Workflow graph hash:

```text
sha256:c8d26673a70bde6253aa22c929689ae6412677cab2df4a815036b107f1753b9f
```

Replay fixture hash:

```text
sha256:eaebc8693f7b86d8cb6fd9efdf321a7b3c1f616b8b7c9603ca62a51d071fdd83
```

```text
clojure -M -e <extract B10 workflow graph, replay fixture, and policy graph>
{:dir "/tmp/gravity-p07-b10-workflow",
 :files ("gravity_stage0_policy.edn" "gravity_stage0_workflow.edn" "gravity_stage0_workflow_replay.edn"),
 :workflow-graph-structural true,
 :replay-fixture-structural true,
 :differential-replay true,
 :external-runtime :not-available-in-current-environment}
```

```text
sed -n '1,40p' /tmp/gravity-p07-b10-workflow/gravity_stage0_workflow.edn
{:workflow :gravity-stage0-workflow
 :runtime :durable-workflow
 :nodes [{:id :start :kind :deterministic-computation}
         {:id :call-model :kind :model-call :provider :stage0-model-provider}
         {:id :call-tool :kind :tool-call :provider :stage0-tool-provider}
         {:id :approve-output :kind :human-review-gate :capability :ai/human-review}
         {:id :write-ticket :kind :external-service-call :effect :network/request}
         {:id :compensate-ticket :kind :compensation-handler}
         {:id :done :kind :deterministic-computation}]
 :edges [{:from :start :to :call-model :kind :data}
         {:from :call-model :to :call-tool :kind :tool-input}
         {:from :call-tool :to :approve-output :kind :taint-validation}
         {:from :approve-output :to :write-ticket :kind :human-reviewed-write}
         {:from :write-ticket :to :done :kind :success}
         {:from :write-ticket :to :compensate-ticket :kind :compensation}]
 :replay-barriers [:call-model :call-tool :write-ticket]
 :status :complete}
```

```text
sed -n '1,35p' /tmp/gravity-p07-b10-workflow/gravity_stage0_workflow_replay.edn
{:workflow-input-digest "sha256:workflow-input-stage0"
 :events [{:event :started :step :start :cycle 0}
          {:event :model-output-recorded :step :call-model :digest "sha256:model-output-stage0"}
          {:event :tool-output-recorded :step :call-tool :digest "sha256:tool-output-stage0"}
          {:event :human-reviewed :step :approve-output :decision :approved}
          {:event :external-write-idempotent :step :write-ticket :idempotency-key "workflow-input-hash"}]
 :replay-mode :event-log
 :side-effects-reissued false
 :status :complete}
```

```text
sed -n '1,20p' /tmp/gravity-p07-b10-workflow/gravity_stage0_policy.edn
{:budget {:model-tokens 2048, :tool-calls 1, :external-writes 1}, :rate-policy {:call-model :bounded, :call-tool :bounded}, :provider-policy {:call-model :stage0-model-provider, :call-tool :stage0-tool-provider}, :human-review-required [:write-ticket], :status :complete}
```

```text
gravity-workflow-replay --version
zsh:1: command not found: gravity-workflow-replay
```

The workflow graph and replay artifacts are structurally validated by the
Clojure proof and recorded for external durable workflow runtime replay when an
external replay runtime is available.

```text
clojure -M:test
Ran 86 tests containing 5043 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend proof EDN parse>
{:parsed 16,
 :tasks [:P07-D098 :P07-D099 :P07-D100 :P07-D101 :P07-D102 :P07-D103 :P07-D104 :P07-D105 :P07-D106 :P07-D107 :P07-T01 :P07-T02 :P07-T03 :P07-T04 :P07-T05 :P07-T06],
 :statuses [:complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete]}
```

```text
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

```text
git diff --check
passed
```

## Rejected Diagnostics

The rejected fixture suite covers all B10 workflow graph backend diagnostic IDs:

- `B10-SCHEMA`
- `B10-REPLAY`
- `B10-IDEMPOTENCY`
- `B10-RETRY`
- `B10-COMPENSATION`
- `B10-CAPABILITY`
- `B10-POLICY`
- `B10-TAINT`
- `B10-GRAPH`
- `B10-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-d107-b10-workflow-graph-backend-proof.edn`

## Remaining Limits

This completes `P07-D107` for deterministic Clojure stage0 coverage of the B10
workflow graph backend design contract. The emitted workflow manifest includes
workflow graph artifacts, step schemas, event-log schemas, replay policy and
replay fixtures, idempotency maps, retry/timeout/cancellation/compensation
records, external capability manifests, model/tool provider manifests,
human-review and policy graphs, taint validation, audit provenance,
source/debug maps, differential replay records, and stable B10 diagnostics.
The current environment does not provide `gravity-workflow-replay`, so this
does not claim external durable workflow runtime replay, scheduler execution,
deployment, provider execution, or full Phase 07 completion.
