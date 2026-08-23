# Tooling Migration

The former Python tree was frozen, removal-only migration debt and has now been
removed. It had no compiler, evidence, aggregate, self-hosting,
seed-retirement, or release authority. New bootstrap and repository tooling is
Clojure; Gravity/Uniform is the only successor implementation language.

The enforceable inventory and content freeze is
`contracts/language-boundary.edn`:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The retired Python tooling and evidence contracts could not represent valid
component deletion without changing Python constants. Do not reconstruct
them. The language-boundary gate rejects new or modified Python and permits
only absence of a pinned legacy path.

## Current Clojure Tools

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_full_language_roadmap.clj --self-test
clojure -M tools/validate_repository_hygiene.clj
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
```

The document and roadmap validators are non-authoritative repository gates.
Repository hygiene rejects tracked Python interpreter cache output. Workstream
governance and preflight are read-only coordination tools; passing them does
not establish implementation correctness, self-hosting, seed retirement, or
release eligibility.

## Retired Surfaces

The reviewed document generators were retired because canonical documents are
enriched normative source, not generated output. Git history preserves the old
generators for audit and recovery; do not rerun or reconstruct them over
`docs/`.

The authority-candidate composer was also retired. It had no trusted admission
root and could only emit non-authoritative, release-blocked candidates. Future
evidence admission must be defined by current Clojure or Gravity governance.

Historical documents may name removed Python commands that were actually run.
Those references are provenance, not live commands or permission to restore
the Python layer.

## Python Boundary

No tracked Python files remain. The language-boundary gate retains the frozen
historical inventory so absence passes and any reintroduction or modification
fails. Historical documents and artifacts may still quote Python commands that
were actually run; they are provenance only.

See `docs/python-tooling-audit.md` and
`docs/tooling-language-migration.md` for the current inventory and migration
order.
