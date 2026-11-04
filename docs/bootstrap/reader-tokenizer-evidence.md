# Reader hosted tokenizer ownership

## Governing contracts and scope

D1/D2/D3/D8/D9, L1, C1/C2/C3/C15, BOOT8 and TEST2 govern this ownership
move. The immutable predecessor is cdba521c71529862a7fa3f0ecb0a44510df9e7aa,
tree 8d175602139e36b961d6a5451b7cb9970e29a68c, retained in draft PR1166.
It is unmerged source, not main or migration authority.

Move the actual lexical context, token realization, dispatch and token-loop
bodies from semantic_mid/004-006 into gravity.reader. Keep the public
stage1-reader-token-stream arities as a compatibility facade. Remove the three
retired resources from both load manifests; preserve the facade's loader
metadata and every unrelated resource and order. The parser in 008-013,
literal decoder, table source, source intake and authenticated SH03 path stay
with their existing owners.

## Public API and dependency direction

gravity.reader/tokenize-compatibility-source takes explicit source path, text,
table, reader options and nine late owner Vars: line starts, digest, line
terminator, span, failure, string decoder, token classifier, atom decoder and
character decoder. Internal tokenizer functions remain captured operations;
external owner Vars are invoked at their original expression positions.
No owner discovery, source read, authentication callback, fallback or callback
type guard belongs to this API. Supplied ports confer no authority.

The Reader depends only on clojure.core and explicit owners. Bootstrap calls
Reader; Reader cannot require Bootstrap or a downstream compiler namespace.

## Artifacts, diagnostics and preserved facts

Input is already-decoded text with an explicit historical reader table and
options. Output is the unchanged gravity/stage1-reader-token-stream map, with
source path, source-content digest, table digest, ordered tokens, count and
status. Preserve raw spelling, decoded values, token indexes/ids, trivia,
dispatch tags, abbreviation shape and all byte/scalar/UTF-16 source spans.
No new envelope or identity preimage is introduced, and no facts are invalidated.

Reader owns lexical branch selection and tokenizer sequencing. The supplied
failure owner constructs the original STAGE1READER003/004 diagnostics; literal
decoding/classification retains its own failures, including STAGE1READER007.
The existing C2 compatibility facade continues to contain/remap failures. No
delimiter/map/set/form legality is moved into tokenization.

## Acceptance and tests

Compare complete original and candidate outputs and failures over real source
fixtures, including .gravity/.qst paths and retained/omitted trivia. Preserve
actual paths, text, table, options, full native records and deterministic
identities; do not normalize provenance out of comparison. Malformed strings,
characters, numeric candidates and unsupported dispatch must retain real
refusals. Test late owner lookup, exception identity, callback sequencing,
literal deferral and cold Reader loading without Bootstrap. Verify exact body
conservation modulo explicit port calls and the small context/comment operation
signature changes needed to thread those ports.

Run owning and affected compatibility/artifact tests, source-language/ownership
gates, contextual lint, document/roadmap/governance validators and independent
regression/adversarial review. Maintain an early draft PR, a clean verified
source commit, own current CI and exact integration preflight. Each acceptance
record applies only to its actual source/base/tree.

## Unsupported boundaries

This scope record establishes no implementation or executed-test credit.
The tokenizer is temporary Clojure compatibility/seed code. It neither opens
source nor authenticates text, table, options, callbacks, tokens or C2 products.
It does not replace the canonical SH03 reader, parse forms, resolve names,
execute extensions, impose source/work quotas, or establish complete Reader,
self-hosting, migration, release or seed retirement. Existing held operations
and failed authentic-source controls remain untouched.

## Implemented hosted tokenizer

The sixteen original helper bodies now live privately in gravity.reader. Nine
explicit late Var ports replace their previous Bootstrap root calls. Only the
lexical context and comment-token signatures gain the operation map needed to
thread those ports. Fifteen internal function values remain captured in a
private operation map. The public API selects only its nine external keys;
callers cannot replace internal dispatch functions through additional entries.
It adds no callback class check, source read, authentication or new envelope.

The compatibility facade retains both arities and the original three-argument
default. The three moved resources are removed from both manifests; the facade
resource retains its source metadata. The existing public API contract test is
an affected owner and now names the eighth operation and expanded boundary.

The clean original capture uses twelve retained real files across .gravity and
.qst, both trivia settings, four successful streams and twenty lexical refusals.
Full records include original paths/text/table/options, token products and
diagnostics; SHA-256 is
082cd1267f86bfec381050f1dcde7906c3fa387bf0bf783dd874d1ff04000ae1.
These are hosted native products and refusals, not C2 source admission.

Initial focused Reader/tokenizer checks pass 51 tests/2153 assertions. They
cover paired source records/slices/identity, trivia, Unicode/CRLF coordinates,
literal deferral, malformed strings/characters/numbers/dispatch, late roots,
callback order, exception identity, no source discovery, internal operation
isolation and facade arities/defaults. An unmatched parenthesis in the initial
new test file was caught before execution and corrected; its source remains at
/tmp/uniform-reader-tokenizer-test-initial-syntax-nonpass.clj. Production source
was not changed for that correction.

Candidate capture, exact body/manifest conservation, committed regressions,
boundary gates, lint, validators, independent reviews, own CI and integration
remain separate outstanding checks. Receipts use /tmp/uniform-reader-tokenizer-*.
