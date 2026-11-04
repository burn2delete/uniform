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

This initial scope record has no implementation or executed-test credit. These
temporary Clojure seed decoders authenticate neither raw/kind/provenance nor
supplied callbacks. They do not open source, validate complete identifier or
numeric grammar, define Gravity numeric families or Unicode character semantics,
replace canonical SH03, grant effects/capabilities, impose source/work quotas,
or establish complete Reader, self-hosting, migration, release or seed retirement.
Legacy host scalar/read-string behavior remains explicitly bounded seed behavior;
it is not imported as a normative Gravity language rule. Held operations and prior
authentic-source refusals remain unchanged.
