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

## Focused Development Runners

Use the reviewed Clojure aliases:

```bash
clojure -M:sh01-test
clojure -M:leaf-test --group foundation-reader
clojure -M:dev-test --catalog
clojure -M:project-structure-runner-unit
clojure -M:project-structure-test --help
clojure -M:stage3-verification --help
```

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
