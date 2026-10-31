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

## Independent review and source-reader correction

The original `4c031d7349f7b00c0e7de196fc455bf95ac923a6` candidate received
changes requested, not acceptance. Independent receipt SHA256
`f4b863efea114b4c3933c99b541a5a6a2e92b50ec100c16ca028bcc19b86bd44`
records 47 checks and two P2 findings. Its source pins and original CI failure
remain historical evidence. This successor starts from that exact candidate.

F1: the resolver returned nil for core Vars and classes. A syntax quote of
`(map String)` in `never.syntax` silently qualified both names into that
namespace. The correction resolves core Var names and default class names
from the already initialized Clojure core/default-import tables. Explicit
`:refer-clojure` exclusions/only/renames, explicit library refers/renames,
imports and prior local declarations update an isolated data scope. Import
class names come from declarations as symbols; no Class.forName, class
initialization, library loading, source evaluation or namespace creation occurs.
Qualified symbols retain their source identities.

F2: prefix libspecs such as `(clojure [set :as s])` were ignored. The scope
collector now expands their prefixes as data, including nested prefixes and
standalone quoted `require` forms. It does not rewrite the returned source
forms. Aliases remain local to one input, and unknown aliases still refuse.

This reader does not emulate namespace execution. Unsupported use/refer-all,
standalone refer/alias/namespace-switch declarations and class-shadowing
conflicts refuse explicitly instead of inventing resolution. Existing
reader-evaluation and tag refusals remain. Ordinary definitions and declaration
data do not grant permission to run code.

The successor facade/private-usage suite passed 15 tests / 339 assertions,
including both review reproductions, declaration scopes, core/classes,
qualified symbols, prefix forms, no namespace creation, explicit unsupported
refusals and the unchanged private-call/dead-helper/current-error/mapping
controls. Static lint has zero errors/warnings and the boundary gate passed
6 / 11. Both actual runner byte pins, form counts, rows and fixture keywords
remain verified by inert source reads. The original failing and corrected
reproduction forms are retained together in the local source-check artifact.

The first successor test attempt refused an existing vector import group;
supporting both list and vector import declarations corrected that local
nonpass. It is retained separately from the passing final run. Everything
from `manifest!` through EOF remains byte-identical to the reviewed parent,
including archived selection, private-call analysis, provenance hashes and
warning mapping. No original CI rerun, independent rereview, publication,
actual Bootstrap/Reader/Gravity/CLI execution or session/Accounting change is
part of this successor. OWN routes the affected rereview after source freeze.

## F3: declaration heads follow source bindings

Independent review of `947797c3329260018e6becd707363598175124b0` resolved F1/F2
and requested one P2 correction, F3. Receipt SHA256
`90f2a3f75ff9a355636932438c13cf4954507116884c50784691172211abf0fa`
records 74 qualified checks. A local function named `require` was mistakenly
treated as a core declaration, inventing a foreign refer for a later syntax
quote. A local `import` call similarly invented a class identity. Neither call
was a declaration or authority to change scope.

This isolated successor changes only declaration-head classification. Bare
heads resolve through prior local names and explicit/core refers. Qualified
heads use source aliases or their explicit namespace. Only resolved core
declarations update reader scope; local and foreign-referred calls do not.
The compiler form `def` remains a declaration independently of the core Var
table, so variable-bound local calls are tracked too. Renamed core declarations,
explicit `clojure.core/require` and `clojure.core/import`, and an explicitly
declared core alias retain their data-scope behavior. No source is evaluated.

Focused controls pass 16 tests / 351 assertions. Added controls cover both
original F3 reproductions, variable-bound and foreign-referred heads, explicit
and renamed/aliased core declarations, and refusal of an alias invented by a
local call. The F1/F2 controls and baseline/current reset, private-call,
dead-helper, current-error and source-mapping controls remain passing.
Both actual runner byte pins and form counts remain verified by inert reads.
Static lint reports zero errors/warnings; the boundary gate passes 6 / 11.

The initial local attempt lacked a nil guard on unresolved heads. Its
correction then exposed the missing special-form `def` classification in one
existing refusal test. Those nonpasses are retained separately from the final
passing run. The source-check artifact retains the exact original F3 strings
and before/after returned forms. Core/import/prefix helpers and all code from
`manifest!` through EOF remain byte-conserved against the reviewed parent.
No warning allowance, source authentication change, original CI rerun,
publication, affected review, actual target execution or modification of the
separate frozen session/acceptance candidates is included. OWN routes review
of this exact successor.
