# Repository Tools

Repository tooling is Clojure. Gravity/Uniform is the successor language for
tooling admitted through the self-hosting contracts.

Current commands:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_full_language_roadmap.clj --self-test
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/validate_architecture_authority.clj --report REPORT
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
# Add --worktree-scan all (or --full-inventory) for an explicit peer inventory.
clojure -M tools/verify_integration_candidate.clj --help
```

The document and roadmap validators are non-authoritative repository gates.
Workstream governance and preflight are read-only coordination tools. Passing
these checks does not establish compiler correctness, self-hosting, seed
retirement, or release eligibility.

Architecture reports have a closed, strict JSON authority block fenced as
`json gravity-architecture-authority-v1`. The pre-freeze command runs the
governance contract and ledger checks, then verifies exact report identity,
integrated dependency states, terminal-history references, and the
integration-only authority ceiling. Use `--legacy` only to inspect retained
reports written before the block was introduced; legacy prose grants no
authority and is checked for false claims such as describing a rejected
Attempt 15 as integrated.

The integration-candidate verifier applies exact-identity preflight, exports a
new candidate tree, runs the unchanged full suite and standard gates with a
fresh Clojure basis, and emits a candidate-bound receipt. It grants only
candidate integration evidence and does not replace lifecycle admission,
Stage3, SH-07, or release proof.

The fresh verifier's full-suite process has bounded diagnostic observability:
the child runner reports its latest namespace/test-var progress in the
temporary export, while the parent prints sparse elapsed/RSS/high-water
heartbeats and records an additive `:observability` receipt section. Process
sampling is capped at 256 identifiers, records truncation, and time-bounds the
host RSS command. This is best-effort, non-authoritative telemetry. It cannot
resume a run, use repository-local caches, select or skip tests, or alter the
existing command, status, exit-code, identity, or authority semantics.

The source-language boundary is enforced by:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

It rejects newly introduced non-Lisp source and extensionless non-Lisp
launchers. Clojure remains the only temporary bootstrap and tooling language.
