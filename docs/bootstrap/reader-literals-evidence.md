# Reader hosted literal ownership

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this bounded
ownership move. Predecessor3e4cd59d2927ddd1e711d152ba46ada5e8bced40, tree
20aca24b78b7564c21ff69f8c647c907e0addb68, is retained in unmerged draft
PR1175. That source does not grant main, migration or release authority.

Move only token classification, malformed numeric payload assembly, atom,
string and character decoding from component00363 into gravity.reader. Keep
all five Bootstrap public names/arities as explicit owner facades. Table source
loading/validation and location/span arithmetic remain unchanged in that resource.
No load-manifest or canonical source pin changes belong to this slice.

## Public API and dependency direction

The new hosted APIs are classify-compatibility-token, reject-compatibility-numeric!,
decode-compatibility-atom, decode-compatibility-string and
decode-compatibility-character. They consume explicit raw spelling, kind where
applicable, source path, span, token id and late owner Vars. Classification uses
the existing starts-with Var; numeric refusal uses the existing failure Var;
atom decoding receives starts-with, split and malformed-numeric Vars; string
and character decoding receive read-string and failure Vars.

Reader depends only on clojure.core and explicit supplied owners. Bootstrap
supplies its existing Vars and calls Reader, never the reverse. No root lookup,
callback type guard, source discovery, new envelope or default is added.

## Inputs, outputs, diagnostics and identity

Input is already-decoded literal spelling plus explicit provenance. Output is
the unchanged native scalar/keyword/symbol/string/character/ratio value, deferred
zero-denominator record or original owner refusal. Preserve integer radix/sign,
exact ratio components/spelling, decimal host behavior, escape processing,
UTF-16 character class, source/span/token/raw/cause/fact references and call order.
These outputs do not introduce an identity preimage; token/form/source identities
and all existing caller-owned hashing remain unchanged. No facts are invalidated.

Reader selects the existing lexical branches and assembles the original numeric
refusal data. The supplied failure owner retains STAGE1READER003/007 construction
and publication. The existing malformed-numeric Var retains late repeated calls
from NumberFormatException/ArithmeticException handling. String/character
read-string calls retain read-eval:false binding, original catches and character
class check. No numeric family, target/profile/effect or safety legality moves here.

## Acceptance and tests

Capture complete original/candidate literal outcomes and real paired token
streams, preserving exact paths/text/raw/spans/token ids/value classes/deferred
facts/diagnostics and deterministic identities. Exercise malformed numbers,
strings and characters; preserve owner exception identity and catch boundaries,
late Var lookup, returning failure behavior and read-eval binding restoration.
Existing token/form/artifact and compatibility tests must continue to pass.

Prove the five body transformations and facades, all previous Reader API bodies,
unchanged table/span portion, unchanged manifests and single CI namespace
insertion. Run language/source ownership, contextual lint, formatting, validators,
independent regression/adversarial review, own exact CI and immediate integration
preflight. Maintain an early draft PR and clean verified source commit.

## Unsupported boundaries

These temporary Clojure seed decoders authenticate neither raw/kind/provenance nor
supplied callbacks. They do not open source, validate complete identifier or
numeric grammar, define Gravity numeric families or Unicode character semantics,
replace canonical SH03, grant effects/capabilities, impose source/work quotas,
or establish complete Reader, self-hosting, migration, release or seed retirement.
Legacy host scalar/read-string behavior remains explicitly bounded seed behavior;
it is not imported as a normative Gravity language rule. Held operations and prior
authentic-source refusals remain unchanged.

## Actual ownership and original comparison

The original seven-path request reported ordinary busy ownership for
tools/ci/run.clj. Its actual owner is the PR915 current-source composition
workstream. No CI source was edited, no foreign lease was adopted/released and
no alternative test route is substituted. The initial scope-only document/PR
was published before the busy result was observed; that new document path had
no ownership overlap and conveys no implementation credit. The actual current
semantic authoring lease owns only the six disjoint Reader/component/test/map/
evidence paths. CI namespace wiring remains explicitly deferred pending the
original owner's known-end/actual absence and a valid claim/handoff.

Original native evidence captures fifty-six literal outcomes across fifty-eight
retained real .gravity/.qst paths: thirty-eight values and eighteen original
refusals, plus four complete token streams with both trivia modes. Source text,
raw spelling, kind, span, token id, exact value class, deferred data and complete
diagnostics are retained. Raw SHA-256 is
a65f15a6ac535fa0aaf0963480a4d189d9f555b3225e4dac8626e6d2727ba673.
This is native structural evidence, not source/callback/C2 authentication.

The six-path semantic implementation moves exactly five bodies through explicit
ports. Static comparison proves the previous nine Reader API bodies unchanged,
the original component table/span prefix and all five facades exact, both load
manifests byte-identical, and the CI file unchanged. The focused Reader artifact,
tokenizer, forms and literal tests pass seventy-three tests and 4,044 assertions.
Language-boundary tests pass six tests and eleven assertions with no skipped
namespace. Documentation, roadmap and workstream validators pass. Standalone
Reader/new-test lint reports zero warnings or errors. These local results do
not confer independent acceptance, full CI or integration authority.

The initial static observer incorrectly interpreted the quoted-name vector and
failed its own start-position assertion before comparing bodies. That helper
and nonpass are retained; correcting only the observer yields the conservation
result above. The earlier new-test syntax nonpass is also retained. Neither
failure was used to weaken production behavior or acceptance requirements.

## Committed comparison and regression

Semantic source c8213baf465e267772ab2af241c124e42644edcc, tree
8179b9ac1b339f35bcdfb1d19bb28d7237c67901, is a clean six-path commit.
Original, candidate and final captures are byte-identical and parsed-equal at
the SHA above. The existing Reader regression command augmented locally with
the new literal namespace passes eighty-eight tests and 4,247 assertions at
one GiB heap. This run does not substitute for the pending CI source insertion.
The source-ownership gate adds forty-two tests and 1,187 assertions with no
skipped namespace. Cold loading Reader exposes fourteen public functions and
does not load Bootstrap. Contextual comparison covers all thirty-nine present
Clojure/EDN paths against main c7f61484ced92850309030b69f50088cf54a8d89:
3,458 unchanged baseline warnings, zero blocking warnings/errors. Raw baseline
warnings remain visible; they are not reported as a warning-free context.

The initial contextual observer incorrectly expected thirty-eight paths,
omitting that component00363 is newly changed relative to main. Its corrected
count is thirty-nine. Its next clean-bookend check also correctly refused the
observer's own two generated discovery files in target/ci. Those files were
archived intact and the observer output path was directed to /tmp before the
successful comparison. Both nonpasses remain recorded. No source or gate was
weakened, no foreign output was removed, and no CI file was edited.

Independent semantic review accepts the exact six-path source with gates:
no material source defect, with independent adversarial review, full wired CI,
fresh integration preflight and coordinator integration still outstanding.

## Owner invocation timing correction

The original global Clojure call captures its current function root before
evaluating arguments. Invoking a supplied Var directly instead resolves the
root after arguments have run. A span callback or an exception's getMessage
can change the failure owner during argument evaluation. Earlier conservation
modulo port substitution did not establish this ordering property; prior
source acceptance does not waive the newly demonstrated failure.

On composed source5876f03f930378c7e3741c69efc6f4351134cc42, three new
regressions fail all five assertions: string/character/numeric cause-message
evaluation, tokenizer failure-span construction, and tagged-form error-message
construction select the later owner instead of the original captured owner.
The failing receipt is retained. Correct all sixty-three affected owner call
sites in hosted tokenization, form construction and literal decoding by
dereferencing the supplied Var in operator position at each call. This is not
an entry snapshot: later calls still see later owner roots. The previous seven
Reader APIs already used that original operator ordering and remain unchanged.

No callback restriction, type guard, default, source discovery, public API,
identity preimage, grammar rule, diagnostic mapping, source pin or manifest
change is introduced. This correction includes the affected predecessor
tokenizer/form bodies because their preservation invariant must hold before
integrating their shared ancestry. Full exact regression, native comparisons,
independent review and new source/base-bound CI remain required; earlier
passing tests/CI do not clear this finding.
The same three new regressions now pass all five assertions after the
operator-position correction; broader repair verification is still pending.

The original PR915 CI owner has since normally released its seven-path claim.
Its actual absence was observed before this workstream obtained a fresh own
one-file claim. Commit8d28222de7027a4d94ceb83482167570024c2222 adds the
literal namespace to the existing Reader CI command. That own one-file claim
was normally released after authoring/checking ended; no foreign lease/token
was adopted or released. The repair has a fresh five-path source lease.
Normal composition5876 preserves incoming mainf66233177d9fc3eaf29af30c7bfc3286bf4f53bc
and its two disjoint set/effects blobs. Current-base Reader88/4,247,
set/effects3/127 and boundary48/1,198 pass, but they precede the owner-order
repair. Its exact CI41907637750 remains historical, not repair acceptance.

## Repair verification

Repair source60087858765a1e9113416ba5b78423cb75aeb7bc, tree
4d0eac5a5760da9739e17dc660905d81327dd4e1, is a clean five-path commit.
The exact wired Reader CI command passes ninety-one tests and 4,252 assertions
under its original one GiB heap and 120-second process watchdog. Language/source
ownership gates pass forty-eight tests and 1,198 assertions with zero skipped
namespaces. The thirty-nine-path contextual lint comparison against mainf662
has 3,458 unchanged baseline warnings and zero blocking warnings/errors.
Docs, roadmap and governance validators pass; cold Reader still exposes
fourteen functions without Bootstrap.

Fresh native captures preserve all original literal outcomes and four complete
token streams at a65f15a6ac535fa0aaf0963480a4d189d9f555b3225e4dac8626e6d2727ba673.
The form-builder capture preserves fifty-two metadata-aware records, four
products, forty-eight syntax refusals and twenty native join refusals, byte
identical to its prior final candidate at
ad4c5c42a3b93f2eac63a9a81cc786237885974966822a3298325841b7a9ab14.
The earlier four declared composite-set failure repairs remain exactly those
recorded in the form-builder evidence; no additional baseline differences are
accepted. These are native structural comparisons, not source authentication.

Static conservation proves exactly sixty-three operator dereferences plus
the explicit owner-call-resolution contract field, with all earlier seven
Reader API bodies, Bootstrap facades, manifests and CI bytes unchanged.
Independent repair acceptance, new source/base-bound CI and immediate clean
integration preflight remain separate outstanding gates. The prior review
withdrawal and failing regressions remain evidence, never erased by this pass.
