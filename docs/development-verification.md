# Development Verification

## Purpose

Repository verification uses the Clojure runners directly.

Run commands from the repository root.

## Standard Gates

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_workstream_governance.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M:test
git diff --check
```

The full Clojure suite is necessary but does not by itself establish release,
self-hosting, seed-retirement, performance, or safety authority.

## Fresh Integration Candidate Lane

Focused and incremental runners are development feedback. Before a candidate
can supply publishable integration evidence, run the exact committed candidate
through the separate fresh lane:

```bash
clojure -M tools/verify_integration_candidate.clj \
  --base-ref BASE_COMMIT \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

The runner first applies integration-mode worktree preflight to those exact
identities. It then checks out the exact committed tree through a temporary
Git index into a new directory,
uses `-Srepro -Sforce`, runs the existing full `clojure -M:test` suite without
narrowing it, and runs the document, roadmap, governance, and language-boundary
gates in that tree. It also runs `git diff --check` over the exact base-to-
candidate range in the preflight worktree. Existing repository target output
and incremental runner caches are not visible to the exported tree; the
temporary tree is not resumed.
Candidate `.gitattributes` files are rejected so checkout filters cannot make
the verified bytes depend on local Git configuration.

The receipt is written once to
`target/validation/integration-fresh-verification/CANDIDATE_COMMIT/receipt.edn`.
It records the candidate identities, fixed command list, exit codes, elapsed
times, fresh-tree policy, and explicit nonclaims. A prior receipt is never
overwritten or resumed. `C16-SPECULATIVE` rejects speculative, resumed, or
repository-local-cache evidence at this publishable boundary.

For CI, provide the reviewed base SHA, candidate SHA, and candidate tree SHA
from immutable job inputs and upload the receipt when the lane reaches receipt
publication, including a failed verification receipt. Usage, identity, or
receipt-publication errors can fail before a receipt exists.
CI must also impose its own job timeout; an externally killed verifier cannot
finish or publish a receipt.
Do not derive a publishable candidate identity from a mutable branch after the
job starts. For local admission preparation, commit first, keep the named
branch clean, and pass the same identities intended for the workstream ledger.

This lane supplies candidate-only integration evidence. It does not replace an
independent accepted review or grant release, self-hosting, seed-retirement,
safety, performance, Stage3, or SH-07 authority. When the owning contract
requires a Stage3 or SH-07 proof, run its existing fresh/no-resume command
separately and record that receipt in the governed evidence bundle.
`contracts/workstream-governance.json` still requires owned paths, governing
contracts, accepted and rejected fixtures, stable diagnostics, successful
checks, independent acceptance, residual boundaries, and explicit nonclaims
in addition to exact Git identities; `WG009` through `WG012` fail closed when
that admission evidence or authority boundary is incomplete.

## Focused Development Runners

Use the reviewed Clojure aliases:

```bash
clojure -M:incremental-check
clojure -M:sh01-test
clojure -M:leaf-test --group foundation-reader
clojure -M:dev-test --catalog
clojure -M:project-structure-runner-unit
clojure -M:project-structure-test --help
clojure -M:stage3-verification --help
```

`clojure -M:incremental-check` passes the current tracked and untracked changed
paths to the existing SH-01 impact planner, prints the ownership and
dependency-expanded invalidation explanation, and runs only the selected test
namespaces through the bounded parallel runner. It succeeds without starting a
runner when all changed paths are unrelated, and fails closed when a relevant
path has no owner. Its plan and execution report are explicitly
non-authoritative and cannot satisfy the standard gates below or any release,
self-hosting, or seed-retirement gate.

The SH-01 impact runner batches only normal namespaces from the same slice in
warm JVMs, with a reviewed maximum batch size of eight. `:memory-heavy` work
retains its capacity-one fresh-process lane, and `:exclusive` work remains
fresh and sequential after the parallel lanes drain. Batch reports preserve
namespace fixture boundaries, deterministic fail-fast skips, bounded output,
and explicit non-authority.

The namespace-lazy development runner can write an opt-in EDN timing receipt:

```bash
clojure -M:dev-test \
  --exact hosted-hello-runs \
  --timing-receipt target/validation/development-test-timing.edn
```

The `gravity/development-verification-timing-v1` receipt separates each
selected namespace's observable require interval from its fixture-wrapped test
execution, and records per-test-var elapsed time, outcome, assertion counts,
selection, and bounded JVM/OS metadata. The JVM-start-to-runner-load interval
is explicitly combined because the bootstrap and runner namespace portions are
not separately observable from inside the runner.

The receipt is non-authoritative scheduling input. It has no benchmark,
performance-regression, proof, conformance, release, self-hosting, or
seed-retirement authority, and it does not make results comparable across
different source, JVM, host, profile, target, or runtime conditions. Omitting
`--timing-receipt` preserves the existing command output and behavior.
Receipts are published only after a selected test run returns normally;
namespace-load or fixture exceptions retain the runner's existing error path
and do not publish a partial receipt.

Stage3 fixed batches run directly through the Clojure runner:

```bash
clojure -J-Xmx2g -M:stage3-verification --batch source-control-form-arity
```

Batch-specific heap requirements and accepted names are enforced by that
runner. Its receipts remain non-authoritative unless a governing contract
explicitly admits the exact result.

## SH-07 And Integration

For bounded cached development feedback, invoke the Clojure iteration runner
directly:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-iteration-cache-runner --help
```

For a fresh reviewed SH-07 transaction:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-authoritative-runner --fresh all
```

Workstream lifecycle and integration eligibility are governed by:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
```

The preflight is read-only. Integration mode requires the exact recorded base,
candidate commit, candidate tree, named branch, and clean worktree. A passing
development command never substitutes for independent acceptance or grants
authority beyond the workstream ledger.
