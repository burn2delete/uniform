# Inert source alias reading for the current lint context

## Purpose

This current-main port extends `ci.lint-context/source-forms!` and its private
Var usage pass with a non-evaluating, data-only reader scope. It recognizes
current `ns`/`require` aliases, core refers, imports, declaration heads,
prefix libspecs, and validated literal top-level `alias` declarations. The
scope is local to each input and follows source order.

## Requirements and dependencies

Reader evaluation remains disabled. Unknown aliases, tags, reader-evaluation
forms, unsupported scope declarations, class-shadowing conflicts, malformed
literal alias pairs, and foreign carriers refuse explicitly. No subject
namespace is created or required, no source is evaluated, and no Class.forName
or callback is used. The returned source forms, source locations, private-call
rows/columns, owner/caller hashes, warning mapping, archived selection, current
loader SHA, checker, C2 pass-cache, and resource/facade coverage remain under
their current owners.

The private-usage pass records a literal alias only after the same resolved
declaration-head operation has validated it. An alias does not invent a
`require` edge or suppress an unrelated unused-private warning. Current core
and C2 owner identities are preserved; this tooling port does not confer
Reader, Bootstrap, runtime, admission, publication, or CI authority.

## Outputs and artifacts

The implementation is in `tools/ci/lint_context.clj` and selected controls are
in `tools/ci/lint_facade_test.clj`. The current source checkpoint, loader
identity, focused command/log, and historical PR697 static nonpass are recorded
in `../accounting-removal/pr697-current-port.edn`.

## Conformance and acceptance limits

With the pinned `clj-kondo` 2025.10.23 directory on `PATH`, the facade/context
controls passed 25 tests/473 assertions with zero failures/errors and zero
blocking warnings/errors. The language-boundary gate passed 6 tests/11
assertions. These are finite source/data controls: they do not load Bootstrap,
execute a genuine Reader, run native code, dispatch hosted CI, or reclassify the
historical PR697 static failure. Independent acceptance of this exact
checkpoint and normal changed-head CI remain required.
