# Gravity

Gravity/Uniform is a self-hosting, homoiconic language platform with one
semantic model and multiple compilation profiles. The repository contains the
normative design contracts, the current Clojure stage0 bootstrap, and the
incremental Gravity implementation.

The implementation boundary is Lisp-only: Clojure is the temporary bootstrap,
seed and tooling language, while Gravity/Uniform is the destination language.
New Python source, bytecode and executable shebangs are prohibited. See
[the tooling-language policy](docs/tooling-language-migration.md).

## Start here

- [Documentation set](docs/README.md) - phase map and source-of-truth rules.
- [Development verification](docs/development-verification.md) - the focused
  edit-to-test workflow.
- [Stage-owned namespaces](docs/bootstrap/stage-owned-namespaces.md) - current
  extraction boundaries and implementation ownership.
- [Implementation roadmap](docs/implementation-roadmap.md) - milestone order
  and capability gates.
- [Roadmap capability audit](docs/roadmap-capability-audit.md) - current
  executable reachability and release limitations.
- [Repository reference archive](README-reference-archive.md) - historical
  command inventories and detailed validation notes retained from the former
  long-form README.

The 240 documents in `docs/` remain the semantic authority. Phase README files
provide navigation; they do not replace the documents they link. The current
product release surface is described by [Phase 18](docs/phase-18-binary-distribution-and-seedless-release/README.md),
which remains separate from the 240-document inventory.

## Run the hosted bootstrap

Gravity source files are co-canonical as `.gravity` and `.qst`; both extensions
remain first-class.

```bash
clojure -M:gravity run examples/hello.gravity
clojure -M:gravity run examples/hello.qst
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
```

The public launcher exposes the accepted bootstrap-hosted subset:

```bash
gravity run examples/core-app.gravity
gravity test
gravity self-host verify
```

`gravity test` covers the current public subset. `gravity self-host verify` is
fail-closed while final self-hosting and seed retirement remain incomplete; an
exit status of `P18T04007` records that limitation rather than a successful
self-hosting claim.

Inspect a hosted artifact when its evidence is relevant:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

The complete stage, profile, compiler, backend, runtime, domain and release
command inventory is retained in the [reference archive](README-reference-archive.md).

## Normal compiler development

Start with [Development Verification](docs/development-verification.md). The
normal loop is:

1. Edit the owning pass or source module.
2. Add or update accepted and rejected fixtures with stable diagnostics.
3. Run the focused semantic tests for the changed behavior.
4. Run affected upstream/downstream composition tests when an interface,
   identity, diagnostic or artifact shape changes.
5. Perform fresh provenance, equivalence and bootstrap checks only when
   explicitly admitting a stable module migration.

For a selected test variable, use the development runner directly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-var-reference-test \
  --exact sh07-b5-qualified-var-development-kernel
git diff --check
```

Focused tests are development feedback. They do not require an admission
receipt, fresh migration proof or routing record. Semantic legality, profiles,
effects, capabilities, ownership, safety, provenance and target
representability remain required compiler behavior. Project cost models,
work-unit budgets, task quotas and budget-only refusals are retired; host heap,
process, capture and platform restrictions remain independent safeguards.

For SH-07, the semantic owner is
`bootstrap/gravity/src/gravity/checked_core.gravity`; the Clojure adapter is a
transport boundary only. Follow the owning-path table and paired-source rules
in [Development Verification](docs/development-verification.md).

## Validation

Documentation and roadmap changes use:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
git diff --check
```

After changing source or tooling, run the language-boundary gate:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Workstream governance and integration preflight are read-only admission tools,
not prerequisites for every local semantic edit. Use them for a governed
candidate or an explicit integration decision:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref origin/main
```

The retired document generator and enrichment pass are provenance only. Do not
reconstruct them over reviewed documents. Historical measurements, failed
attempts and specialized runner notes remain in the [README archive](README-reference-archive.md)
and [development-verification reference](docs/development-verification-reference.md);
they do not become standing instructions merely because they are retained.
