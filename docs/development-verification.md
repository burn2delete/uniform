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
`bootstrap/gravity/src/gravity/checked_core.gravity`. The Clojure adapter at
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj`
preserves transport and compatibility; it does not own language semantics.

Use the owning path for the behavior under test:

| Behavior | Owning implementation |
| --- | --- |
| Canonical source/Core shape and legality | `bootstrap/gravity/src/gravity/checked_core.gravity` |
| Namespace and reference binding | `bootstrap/gravity/src/gravity/resolution.gravity` |
| Reader literal transport | `bootstrap/clojure/src/gravity/c3_literal_projection.clj` and its SH-04 consumer |
| Function and expression types | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` |
| Body and latent effects | `bootstrap/gravity/src/gravity/effects.gravity` |
| Typed Core to executable development IR | `bootstrap/gravity/p15_s23/emitter.gravity` |
| Development IR evaluation | `bootstrap/gravity/src/gravity/runtime.gravity` |

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

## Focused runners and composition

Use the existing `:dev-test` runner with `--namespace` and `--exact` selectors.
Use a fresh JVM after compiler source changes. `--list` discovers selectors;
it does not execute tests. A namespace that combines cheap kernels with
authenticated composition should be selected by exact variable so ordinary
feedback does not build a large product unnecessarily.

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
