# Gravity

Gravity/Uniform is a homoiconic language platform with one semantic model and
multiple compilation profiles. The Clojure/JVM bootstrap runs hosted programs
and compiles a checked scalar subset to JavaScript that runs in Node 20. The
compiler self-build and seedless release are still open. See the
[implementation roadmap](docs/current-roadmap.md) for runnable examples,
expected diagnostics, and the remaining self-build requirements.

The implementation boundary is Lisp-only: Clojure is the temporary bootstrap,
seed and tooling language, while Gravity/Uniform is the destination language.
New Python source, bytecode and executable shebangs are prohibited. See
[the tooling-language policy](docs/tooling-language-migration.md).

## Start here

- [Documentation set](docs/README.md) - phase map and source-of-truth rules.
- [Project structure](docs/project-structure.md) - language and bootstrap-replacement ownership.
- [Uniform language project](language/README.md) - permanent language/compiler scope.
- [Bootstrap replacement project](bootstrap/README.md) - seed and trust-reduction scope.
- [Development verification](docs/development-verification.md) - the focused
  edit-to-test workflow.
- [Stage-owned namespaces](docs/bootstrap/stage-owned-namespaces.md) - current
  extraction boundaries and implementation ownership.
- [Repository reference archive](README-reference-archive.md) - historical
  command inventories and detailed validation notes retained from the former
  long-form README.

The 240 documents in `docs/` remain the semantic authority. Phase README files
provide navigation; they do not replace the documents they link. The current
product release surface is described by [Phase 18](docs/phase-18-binary-distribution-and-seedless-release/README.md),
which remains separate from the 240-document inventory.

The checkout contains two coordinated delivery projects. Uniform language
modules own semantics regardless of which host executes them. Bootstrap
replacement owns the temporary Clojure seed, rebuild/equivalence work, and
seed-retirement evidence. See [the project structure map](docs/project-structure.md).

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

`gravity test` covers the current public subset. `gravity self-host verify`
exits 1 with diagnostic `P18T04007` while the Clojure seed boundary remains
active.

Inspect a hosted artifact when its evidence is relevant:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

The complete stage, profile, compiler, backend, runtime, domain and release
command inventory is retained in the [reference archive](README-reference-archive.md).

## Compile a checked JavaScript module

The public compiler supports hosted/safe scalar modules with I64 and Bool,
checked I64 addition, eager local bindings, Boolean branches, fixed-arity
nonrecursive direct calls, exports and private helpers. The compiler runs on
the Clojure/JVM seed; the emitted module runs independently with Node 20.

The tested toolchain is OpenJDK 21.0.13, Clojure CLI 1.12.0.1530,
Clojure 1.12.0 and Node v20.20.2. From the repository root:

```bash
clojure -P -M:gravity
clojure -J-Xmx2g -M:gravity --version
bin/gravity compile examples/checked-js/acceptance.gravity \
  --target js -o target/acceptance-js
node --input-type=module -e '
  import {score} from "./target/acceptance-js/program.mjs";
  console.log(score(true, 3n, 7n).toString());
  console.log(score(false, 7n, 3n).toString());
  console.log(score(true, 9007199254740993n, 9007199254740994n).toString());
'
```

The expected stdout is `10`, `7`, then `18014398509481987`, one value per line.
Use a fresh output directory: compilation emits `program.mjs`, `program.d.ts`,
`program.mjs.map`, `package.json`, `manifest.edn` and `provenance.edn` together.
`--target js-ts` selects the same producer; the canonical source declaration
remains `:js-ts`. The explicit source launcher is
`clojure -J-Xmx2g -M:gravity compile` with the same arguments.
Use `clojure -M:gravity help` for the current command forms.

The CLI reads policy from the actual source. It requires no API expression,
policy map or intermediate IR. Node syntax validation occurs during
compilation; importing and calling exports demonstrates runtime behavior.
Explicit `--lowering runtime-derived` retains the legacy route and its genuine
results or refusals. Other lowering requests receive structured refusals.

See [Checked scalar JavaScript modules](docs/compiler-checked-js-modules.md)
for source launch, both aliases, ABI errors, and independent execution. The
guide gives focused backend and public smoke tests and the full acceptance
command. [Verification](docs/compiler-checked-js-modules.md#verification)

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
`language/compiler/src/gravity/checked_core.gravity`; the Clojure adapter is a
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
