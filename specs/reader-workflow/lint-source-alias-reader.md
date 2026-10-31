# Alias-aware inert lint source reading

The PR697 original static failure remains a nonpass: run `41204203482`, job
`124545858508`, artifact `13043481814`, source
`7c9f3489d1e0bc2fe77e8410f0f58b8e4765686b`. Its changed-file lint source reader
failed first on baseline `::test/once-fixtures`. The original harness controls
passed 11 / 108. No rerun, workflow dispatch or runner keyword rewrite is part
of this correction.

The input disposition is `PR697-successor-static-baseline-disposition.edn`,
SHA256 `71fa2b7ba8b290a142b609947f334c34f164c0f0cd62d9e78e8bf4d0963d16dc`.
This candidate starts at main `0be1c7d4ff8f4a0bc1acba68c9799e6f461be2e7`.
Its original `tools/ci/lint_context.clj` is byte-identical to PR697's source,
SHA256 `8a1f5145b2abca26100095b47116efcd6622768c6b8e5215b0cc1e497b9ed7fa`.
Accounting's archived baseline selection correction is therefore retained.

## Source interface

`ci.lint-context/source-forms!` keeps its existing path/bytes API. A local
`LispReader$Resolver` obtains the current namespace and aliases from completed
top-level `ns` and `require` declarations read as data. Both `:as` and
`:as-alias` are recognized for keyword resolution. Each input starts with an
empty alias map; an `ns` declaration resets that map. A reader without an `ns`
declaration retains the existing calling namespace default. No subject
namespace is created, required, evaluated or initialized, and tool aliases are
not imported. Unknown aliases fail instead of being guessed. Reader evaluation
is disabled; arbitrary tags and the built-in inst/uuid readers are refused.

The baseline and current paths use this same reader independently. Nothing at
or after `manifest!` changes: source assembly, archived selection, private Var
usage, executable-position/scope analysis, caller/definition hashes, warning
mapping and current error blocking remain byte-conserved. Source is still
passed to the linter with original text and locations.

## Validation and limits

The full facade/private-usage controls passed 13 tests / 301 assertions. New
controls use coherent baseline/current programs with different aliases and
owner namespaces, resolve real private call warnings with source hashes, and
retain dead-helper warnings. They check declaration changes, native aliases,
auto-qualified keywords/maps, unknown and ambient aliases, tags and blocked
reader evaluation without side effects. Existing case constants, quoted calls,
shadowed comments, destructuring defaults and complete lint-pipeline negative
controls are unchanged and pass.

The actual main runner source (38 forms, hash
`654d730243d5b2116b10c805865a9be71af5e91cd2548f15ef08efdb8c2c7604`)
and PR697 source (49 forms, hash
`c744ff76e728c77b94e72cbd9592782900c9dc2d9d1403ba6207cdbc34efaffd`)
were read only as data. Lines 518/520 and 635/637 resolve to the actual
`:clojure.test/once-fixtures` and `:clojure.test/each-fixtures` keywords.
Neither the runner namespace nor Bootstrap was loaded.

Local CI tooling controls passed 11 / 106 on this base (not the later PR697
11 / 108 scope). Language-boundary controls passed 6 / 11; static lint has zero
errors/warnings. The first local facade attempt found an unmatched delimiter in
the new reader; that nonpass is retained in the local log. After correction,
the complete 13 / 301 run passed. No warning exemption or baseline allowance
was added. The original uploaded CI failure is not replaced by these checks.

This is a finite tooling source candidate for OWN to route for independent
review. No actual Reader, Bootstrap, Gravity, provider, CLI, selector, lease,
6343 operation, review dispatch, publication, CI rerun or integration occurred.
The separate normal-entry acceptance definitions remain frozen and unrun.
