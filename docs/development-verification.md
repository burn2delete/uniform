# Development Verification

This is the default workflow for ordinary compiler and tooling changes. It
keeps semantic feedback close to the owning implementation and reserves
expensive provenance and bootstrap work for an explicit migration decision.
The [reference archive](development-verification-reference.md) retains the
specialized commands, historical measurements and prior attempts that were
previously mixed into this guide.

## Purpose and evidence boundaries

Run commands from the repository root. Select the smallest test that exercises
the changed behavior, then expand validation only when the change crosses an
interface or affects an artifact consumed downstream.

| Boundary | Required work | Trigger |
| --- | --- | --- |
| Semantic implementation | Edit the owning pass; add accepted and rejected fixtures; run focused semantic tests and stable diagnostics. | Every semantic change. |
| Integration validation | Exercise affected upstream/downstream composition and proportionate regressions. | Input/output shape, identity, diagnostic, provenance or interface changes. |
| Authoritative migration | Fresh provenance, equivalence, bootstrap comparison, exact identities and independent acceptance. | Explicit admission of a stable module revision. |

Focused tests are development feedback. They do not establish release,
self-hosting, seed retirement or migration authority. Do not create a receipt,
cache, lane or routing record solely to run a focused test.

### Read-only self-host verification and explicit export

The current `gravity self-host verify` route is a read-only check: it reads and
authenticates the current P18-T06 proof, leaves tracked files and verifier
outputs unchanged, and fails closed with `P18T04007` when the evidence is
missing, stale or incomplete. It does not regenerate proof or diagnostic
artifacts. The explicit
`p18-t04-write-public-self-host-verify-command-artifacts` command is the
separate export path when regenerating those artifacts is intentional. This
describes current development behavior; it does not revise historical Phase 18
evidence or claim seed retirement, release or self-hosting completion.

### Explicit packaged JVM development entrypoint

The Phase 18 implementation roadmap and phase README are frozen historical
inputs for SH acceptance; they are not edited to describe this checkout's
development routing. The current packaged JVM development command is explicit:

```bash
clojure -M:packaged-gravity --version
clojure -M:packaged-gravity run examples/core-app.gravity
```

`bin/gravity` does not auto-select the current development packaged JAR at
`target/development-loop-batches/phase-18/jvm-cli/gravity-jvm-cli.jar`.
Its frozen wrapper can still select the historical
`target/phase-18/jvm-cli/gravity-jvm-cli.jar` when that file is present and
`GRAVITY_BOOTSTRAP_ONLY` is not `1`. Use `clojure -M:gravity --version` to
select the current source entrypoint explicitly.
The explicit `:packaged-gravity` alias launches the current development package and preserves the
accepted/rejected command checks, but it is bootstrap-hosted and is not a
claim that the final Phase 18 seedless release or self-hosted compiler is
complete. The byte-identical historical JAR and its Phase 18 evidence remain
at their archived paths; current package outputs use the development output
path.

## Remove project cost and budget mechanisms

Project cost models, work-unit budgets, producer/caller debits, prospective
reservations, cost proofs and budget-exhaustion refusals are not implementation
or admission requirements. Input node, depth, width, scalar, byte, token and
form quotas whose only purpose is refusing otherwise well-formed input are also
retired. Do not replace them with larger constants, renamed generic bounds,
unbounded sentinels or a new accounting policy.

Preserve independently meaningful validation: Unicode and UTF-8, types, legal
indices and arities, target-format representability, profiles, effects,
capabilities, ownership, authentic inputs, native class and callback rules,
metadata, source provenance and tamper detection. Host heap, process-watchdog,
capture, broker and platform restrictions remain separate safeguards.

Historical accounting observations retain their original scope in the
[reference archive](development-verification-reference.md#remove-project-cost-and-budget-mechanisms).
They are not current gates and do not justify replaying a denied or restricted
operation.

## Default SH-07 workflow: three boundaries

For SH-07, edit the owning Gravity implementation
`language/compiler/src/gravity/checked_core.gravity`. The Clojure adapter at
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj`
preserves transport and compatibility; it does not own language semantics.

Use the owning path for the behavior under test:

| Behavior | Owning implementation |
| --- | --- |
| Canonical source/Core shape and legality | `language/compiler/src/gravity/checked_core.gravity` |
| Namespace and reference binding | `language/compiler/src/gravity/resolution.gravity` |
| Reader literal transport | `bootstrap/clojure/src/gravity/c3_literal_projection.clj` and its SH-04 consumer |
| Function and expression types | `language/compiler/src/gravity/compiler/c7_type_checker_engine.gravity` |
| Body and latent effects | `language/compiler/src/gravity/effects.gravity` |
| Typed Core to executable development IR | `bootstrap/gravity/p15_s23/emitter.gravity` |
| Development IR evaluation | `language/runtime/src/gravity/runtime.gravity` |

Start with an exact kernel test:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-var-reference-test \
  --exact sh07-b5-qualified-var-development-kernel --fail-fast
```

Run affected source composition when the handoff changes. Keep `.gravity` and
`.qst` paired where extension handling, reader transport, canonical identity or
source-path projection is part of the change. Parser parity alone does not
establish typed, effect, lowering or runtime equivalence.

The full historical SH-07 selector matrix and its limits are in the
[reference archive](development-verification-reference.md#default-sh-07-workflow-three-boundaries).

## Current-reader development

The explicit current-reader route is for development and does not renew pinned
migration authority:

```bash
bin/gravity sh07-core /absolute/canonical/path/module.gravity --development-current-reader
bin/gravity sh07-core /absolute/canonical/path/module.qst --development-current-reader
```

The option must follow one target path. Unknown options, duplicate flags and
extra paths fail before target reading. The API accepts either its existing
one-argument call or `{}` / `{:reader :development-current}`; other maps fail
before target reading. The default route remains pinned and fail-closed on
reader drift. Both modes retain strict extension, UTF-8, source identity,
regular-file, ownership and path checks. The public decoder reads the target
once, and authenticated downstream passes consume the retained source.

## Current Stage2 module map-key development

The frozen `docs/self-hosting-slice-backlog.md` wording that lists Symbol keys
as unsupported describes its older checkpoint. Current temporary
module-development code has a narrow authenticated key increment: it accepts
exact native Symbol keys and ordinary call-shaped list-expression keys only
when authenticated C2 source/reference keys retain equality and form a
complete source permutation. Expression-key admission is structural; it does
not evaluate a key in Clojure or whitelist a named callee. Ordinary Stage2
call validation remains responsible for callee, arity, binding, effect,
capability and safety legality. Vectors, Maps, Sets, forbidden special forms,
and other unsupported key shapes remain rejected. The existing source,
reference, child-link, occurrence, native-class and order checks remain in
force. This does not widen the canonical typed-Gravity Map subset or establish
module admission, checked Effects, full C14, native execution, self-hosting,
migration or release support. Quoted Maps remain inert. The focused repair
covers the current C11 shape `{(get token :value-id) token}` through a small
current Reader/C5/Syntax route; the historical full SH17 result remains
terminal failed evidence until a separate complete rerun. This note describes
current development behavior; it does not update the frozen SH source input or
renew migration authority.

## Focused runners and composition

Use the existing `:dev-test` runner with `--namespace` and `--exact` selectors.
Use a fresh JVM after compiler source changes. `--list` discovers selectors;
it does not execute tests. A namespace that combines cheap kernels with
authenticated composition should be selected by exact variable so ordinary
feedback does not build a large product unnecessarily.

The project-scoped aliases make the language/bootstrap boundary explicit while
preserving those selectors and the existing fixtures:

| Alias | Initial assertion-based scope |
| --- | --- |
| `:language-test` | Reader, syntax, resolution and type-semantics tests. |
| `:compiler-composition-test` | Reader/core handoffs, effect checking and MIR composition tests. |
| `:bootstrap-validation-test` | W5 stage equivalence, compiler identity, trust/provenance, rebuild and subsystem-closure tests. |

The catalogs are an explicit initial subset, not a claim of full language,
compiler or bootstrap conformance. Assign a new test by the assertions it
checks: a file under the historical `self_hosting` path is not bootstrap
validation merely because of its path. Extend the named scope in
`tools/gravity/project_tests/scope.clj` when a test's owning
boundary is clear, and keep its focused positive and rejected cases visible.
The shared entrypoint is intentionally only a scope binding; the existing
development runner still applies `--exact`, `--regex`, `--prefix`, `--list` and
`--fail-fast` for all three aliases.

For example, these commands execute one language test, one compiler-composition
test, and one accepted and one rejected W5 assertion respectively:

```bash
clojure -J-Xmx2g -M:language-test \
  --namespace gravity.reader.normal-dispatch-test \
  --exact gravity.reader.normal-dispatch-test/actual-path-provenance-does-not-change-semantic-hashes \
  --fail-fast

clojure -J-Xmx2g -M:compiler-composition-test \
  --namespace gravity.reader.composition-test \
  --exact gravity.reader.composition-test/identity-view-conserves-companion-without-conferring-admission \
  --fail-fast

clojure -J-Xmx2g -M:bootstrap-validation-test \
  --namespace gravity.self-hosting.w5-stage-equivalence-verifier-test \
  --exact gravity.self-hosting.w5-stage-equivalence-verifier-test/w5-stage-equivalence-accepts-static-record-but-blocks-completion \
  --fail-fast

clojure -J-Xmx2g -M:bootstrap-validation-test \
  --namespace gravity.self-hosting.w5-stage-equivalence-verifier-test \
  --exact gravity.self-hosting.w5-stage-equivalence-verifier-test/w5-stage-equivalence-rejected-fixture-covers-every-mode-and-control \
  --fail-fast
```

Use `clojure -M:<alias> --list` to inspect an alias's initial namespace
catalog. These runs provide development feedback only; they do not establish
release, seed-retirement, self-hosting or migration authority.

The resource broker, bounded development runners and warm JVM are optional
host safeguards for genuinely heavy work. They are not semantic admission
requirements. They cannot select, skip, cache or promote a test result and do
not replace fresh verification at an authoritative boundary.

The detailed runner catalog, resource limits and historical timing records are
kept in the [reference archive](development-verification-reference.md).

## Validation commands

Run checks appropriate to the changed files:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
git diff --check
```

After adding, removing or replacing source or tooling, run the language-boundary
gate:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Run the full suite only for a broad integration or release decision. Workstream
governance and preflight remain read-only coordination checks; ordinary semantic
development does not require a fresh admission receipt or migration fingerprint.

## Compatibility anchors

These headings retain links used by historical evidence and component pages.
The detailed sections live in the reference archive.

### Character family classification

See [the archived selector and evidence](development-verification-reference.md#character-family-classification).

### Explicit Stage2 runtime execution attribution

See [the archived attribution procedure](development-verification-reference.md#explicit-stage2-runtime-execution-attribution).
