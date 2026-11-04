# Reader hosted form-builder ownership

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this bounded
ownership move. Its immutable predecessor is
b3a0376451e76a86cd82c53c42456678f3cb7373, tree
49ddbdc3d3cb706d36aef38ef0948e882e4221b9, retained in unmerged draft PR1171.
That source supplies no main, migration or release authority.

Move the actual recursive form construction in semantic_mid/008-013 into
gravity.reader: native token joins, form ids, collection/delimiter handling,
literal set duplicate checks, metadata, abbreviations, tagged literals, parent
links, root records and parsed values. Preserve stage1-reader-products-from-token-stream
as the public compatibility facade. Remove only the six retired resources from
both manifests, retaining the facade's loader metadata and unrelated order.

## Public API and dependency direction

gravity.reader/build-compatibility-forms takes explicit path, source text,
table, token stream and ten late owner Vars: digest, pipeline failure, line
starts, token index, span, canonical hash, atom decoder, failure, tagged-value
decoder and form kind. Native context retains these ports privately; no port
or context is emitted. No owner discovery, source read, callback type check,
fallback or authentication operation belongs to this API.

Bootstrap supplies existing owner Vars and calls Reader. Reader depends only on
clojure.core and explicit operations, never Bootstrap or a downstream stage.
Literal decoding and diagnostic construction remain supplied owners. The
existing Reader CI command must include the new owning test namespace.

## Artifact and diagnostic ownership

Input is already-decoded source and an explicit native token stream/table.
Output is the unchanged map of records, flat form tree, root form ids and parsed
values. Preserve source/token/form identities, source/trivia hashes, raw slices,
metadata, collection values, parent/child links, source spans, abbreviation
expansions and generated-origin links. No new identity preimage or envelope is
introduced and no facts are invalidated.

Reader owns existing native join predicates, form construction and ordered
diagnostic selection. The supplied pipeline failure owner retains STAGE1PIPE004;
the supplied reader failure owner retains STAGE1READER001/002/004/005 and
C2-SET/METADATA/ABBREV payload construction. Related duplicate locations and
form/token/raw/expected/actual facts survive. The C2 compatibility facade keeps
its exception containment/remapping and publication. Unavailable semantic facts
are not invented by this ownership move.

## Acceptance and tests

Capture complete original and candidate outputs/refusals over retained real
.gravity/.qst files, both trivia modes and all moved syntax branches. Preserve
actual text/paths/table/options/tokens, parsed metadata, raw slices and diagnostic
data in comparisons. Reject mismatched/unclosed delimiters, odd maps, duplicate
literal sets, malformed metadata, unattached abbreviations/tags and bad registered
tag payloads. Separately compare existing kind/path/source/table/count join
refusals without claiming that those predicates authenticate inputs.

Test late Var lookup and callback/exception ordering, lazy token indexing,
metadata/generated-origin and parent links, semantic deferral, no source
discovery, facade arity and cold Reader loading. Prove conservation of all
sixteen helpers modulo explicit ports/private context, every previous Reader
API, both manifests and the single added CI namespace argument.

Run owning/affected regressions, language/source ownership gates, contextual
lint, formatting and validators, independent regression/adversarial review,
own exact CI and immediate integration preflight. Maintain an early draft PR
and a clean verified commit. Each receipt is bounded to its actual source/base/tree.

## Unsupported boundaries

This scope record has no implementation or executed-test credit. This is
temporary Clojure compatibility/seed parsing. Native kind/path/hash/count joins
are consistency checks, not source/table/token/callback/C2 authentication.
The API does not open source, replace canonical SH03, grant reader extension
effects, decide namespace/type/effect/profile legality, impose input/work quotas,
or establish complete Reader, self-hosting, migration, release or seed retirement.
Existing source pins, denied operations and held routes remain unchanged.

## Original parser failure and bounded correction

The first native baseline at scopeb6cd5855d2366c21ab0369dd0b78502498276b48
raised IllegalStateException on the duplicate composite set case. Its recursive
literal-set predicate attempted to invoke an unbound Bootstrap helper. This is
an observed native implementation failure, not an authentication or permission
refusal. Source/log/report remain retained; no source pin or held route changed.

A separate observer captures that exact known host failure instead of claiming
it was a diagnostic. Across twenty-six retained real .gravity/.qst paths and
both trivia modes, the original has four parsed products, forty-four syntax
refusals and four unbound-helper failures; twenty native join refusals are
recorded separately. Full metadata is serialized. The raw capture SHA-256 is
aad631a7cf2bd3fbe64faa1c4cf231a3eb245292da0f393d5300f5828089583d.
The observer's initial printed summary incorrectly counted all forty-eight
negative outcomes as syntax refusals; the corrected read-only receipt derives
forty-four refusals/four host failures from the unchanged capture, without
repeating parser execution.

The stage-owned predicate must remain bound for recursive composite keys and
produce the C2-required C2-SET refusal with the duplicate and first-literal
locations; well-formed composite sets must build normally. This recursive-helper
binding is the only declared behavior correction. In the retained capture it
affects four paired/trivia variants of the duplicate composite case. All other forty-eight records and all
twenty native join refusals must retain exact output/metadata/diagnostic parity.
The comparison cannot report complete old/new raw-byte equality across this
explicit diagnostic repair.

The move also removes one unused tokens binding from the private root-context
destructuring. The context still contains the original token vector; all actual
token access and outputs remain unchanged. This avoids introducing a moved
unused-binding warning and changes no public input lookup.

## Implemented hosted form builder

The sixteen recursive helpers now live privately in gravity.reader, retaining
their recursive Var bindings. Ten explicit owner Vars are threaded through a
private reader-operations context field; that context/port data is never emitted.
The facade retains its four-argument signature and supplies only those Vars.
All eight earlier Reader API bodies remain unchanged. Both manifests remove
only the six moved resources, and the existing Reader CI command names the
new owning forms-test namespace.

Initial owning Reader checks pass63 tests/3877 assertions. Coverage includes
paired real records/raw slices/parent links, metadata and unsafe/review flags,
generated origins, tagged values, deferred ratios, valid and duplicate composite
sets, syntax and native-join refusals, lazy indexing, late roots, callback order,
returning failure callbacks, exception identity, tagged-decoder containment,
coherent native forgery non-authority, source-discovery tripwires and facade arity.

Retained test-only nonpasses include malformed parentheses before successful
test execution and a callback expectation that omitted nested digest calls made
by the unchanged canonical-hash owner. Only the test syntax/expectation changed;
production ordering was preserved. Candidate artifacts, conservation, committed
regressions, boundary/lint/format/validator checks, independent review, own CI
and integration remain outstanding.
