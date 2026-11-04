# Reader historical table-driven parser ownership

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this bounded
ownership move. Parent9b702ccfdb1ba808ceec9b823906fa4da473de0c, tree
8d2f66dcc234a9390ec168eb557f5b01e2b96d39, preserves the unmerged Reader
literal slice from draft PR1180. Its source acceptance does not confer wired
CI, integration, migration or release authority on this successor.

Move only stage1-reader-table-driven-records from component00368 into
gravity.reader/parse-compatibility-records. Keep the original three-argument
Bootstrap facade and the execution-diagnostic-stream suffix. Preserve the
historical P15 parser exactly; it is separate from the token/form pipeline
and canonical SH03 authentication. No manifests or source pins change.

## Public API and dependency direction

parse-compatibility-records accepts explicit source path, source text, table
and an operation map containing six late owner Vars: line-start-indices,
line-terminator-char?, stage1-reader-fail!, stage1-reader-span,
clojure.string/starts-with? and form-kind. Reader owns the scanning and
recursive parsing body. Bootstrap supplies its existing owners and calls
Reader. Reader depends only on clojure.core and supplied operations; it must
not discover Bootstrap roots, source files, tables, callbacks or authority.

## Artifact inputs, outputs, identity and diagnostics

Inputs are caller-supplied text/path/table. Output is the original ordered
vector of form records with native form, existing kind, full source span and
form-index. The same line, scalar-column, UTF-16 and UTF-8 arithmetic remains
owned by supplied span/location functions. No new identity preimage, envelope
or cache is introduced; no surviving facts are invalidated by this move.

The parser selects the same STAGE1READER001 through STAGE1READER005 refusal
branches and arguments. The supplied failure owner constructs/publishes the
diagnostic. Keep late lookup, source text/path, table behavior, index and call
order, returned-failure behavior, original native exception boundaries and
span/raw references. The existing execution-diagnostic-stream remains under
its original Bootstrap/evidence owner.

## Acceptance and tests

Capture original and candidate outcomes from retained real paired .gravity
and .qst paths, including nested lists/vectors/maps/sets, comments, mixed line
endings, strings and non-ASCII spans. Include actual unexpected delimiters,
unclosed forms/strings, unsupported dispatch and odd-map refusals, plus
honest observations of host parser limits. Compare complete records and
diagnostics byte-for-byte and parsed-equal with exact provenance.

Prove the body transformation modulo explicit ports, original facade arity,
unchanged diagnostic suffix/manifests and all fourteen preceding Reader API
bodies. Test callback order and late mutations, native exception identity,
source/table discovery absence and actual value/diagnostic behavior. Run
proportionate Reader regression, language/source ownership gates, contextual
lint, formatting/validators and independent semantic/adversarial review.
Maintain an early draft PR, clean verified commits, own exact wired CI and
immediate integration preflight before coordinator integration.

## Actual ownership and deferred CI

A genuine six-path implementing lease owns Reader, component00368, Reader
artifact/new parser tests, the namespace map and this evidence document.
The existing PR915 workstream still owns tools/ci/run.clj. Its CI path is
explicitly deferred until original owner release/actual absence and a valid
claim/handoff. The full intended slice includes one new namespace argument in
the existing Reader CI command. Do not adopt/release the foreign lease or
substitute another test route to evade ownership.

## Unsupported boundaries

This scope record has no implementation or executed-test credit. Historical
P15 behavior includes a restricted atom grammar, Long integer parsing, simple
string escape handling and legacy set/map behavior. Preserve these honestly
as seed limitations, never as normative Gravity semantics. This API does not
authenticate source/table/operations, implement full literal/identifier/tag/
abbreviation/metadata grammar, add trivia or canonical identities, normalize
host errors, execute Gravity-authored parsing, establish complete Reader,
or grant canonical SH03, migration, self-hosting, release or seed retirement.
Held operations and existing authentic-source refusals remain unchanged.
