# Repository Tools

Repository tooling is Clojure. Gravity/Uniform is the successor language for
tooling admitted through the self-hosting contracts.

Current commands:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_full_language_roadmap.clj --self-test
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
clojure -M tools/verify_integration_candidate.clj --help
```

The document and roadmap validators are non-authoritative repository gates.
Workstream governance and preflight are read-only coordination tools. Passing
these checks does not establish compiler correctness, self-hosting, seed
retirement, or release eligibility.

The integration-candidate verifier applies exact-identity preflight, exports a
new candidate tree, runs the unchanged full suite and standard gates with a
fresh Clojure basis, and emits a candidate-bound receipt. It grants only
candidate integration evidence and does not replace lifecycle admission,
Stage3, SH-07, or release proof.

The source-language boundary is enforced by:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

It rejects newly introduced non-Lisp source and extensionless non-Lisp
launchers. Clojure remains the only temporary bootstrap and tooling language.
