# Lowering: native C14 reconstruction and exact joins

## Boundary and contracts

D1, D2, D3, D6, D8, D9, C1, C11, C12, C13, C14, C15, B1, B2, B3, B4,
B13, B14, BOOT8 and TEST2 govern this increment. Its source base is actual
main f337751c6f0c56fa8a3e0a59d636b15ec068ba78, tree
6137edf7b5793ad055a180f26a6cacbb82cc2efb, including Diagnostics PR702.

Move the existing LLVM, C and Wasm C14 expected-record builders and exact
reconstruction gates into gravity.lowering. The six APIs consume explicit
optimized-MIR/policy/context data, expected and actual records, and strict
comparison/diagnostic operations. They emit the existing raw reconstruction
records or an exact-comparison refusal. Source/proof/fact associations, input
and request identities, dependencies, target data and diagnostic call order
are preserved; no upstream semantic fact is invalidated.

The new namespace imports only clojure.core. Three existing lexical Bootstrap
components forward record construction and supply six Var ports for comparison
and diagnostics. The canonical Gravity source builder, policy construction,
input admission, source invocation and private sealing stay with their existing
owners. The lexical authority holders and their guarded call sequence remain
unchanged. The current ownership record assigns SH-17 and the canonical C14
module to master-coordinator; the helper keeps that existing owner.

C14-MANIFEST identifies exact reconstruction/context-join failures. Strict
comparison still runs first and propagates C11-VERIFY; diagnostic construction
and publication remain with the existing C11/backend owners. Var ports retain
late interposition after the comparison callback.

## Development controls

At clean main43c69654, the complete current Gravity C14 source compiled into
an executable development plan with 168 functions. Its source SHA256 is
ccc1089fd7f180e6164bc19d3ab9887858b3e9a8f430e76c789e7ccb0b153506.
The LLVM, C and Wasm source builders each rejected nil optimized input and an
empty policy with C14-INPUT. These are current-source development execution
observations, not authenticated compiler composition or migration evidence.
The C14 source and its native reconstruction bodies are unchanged by incoming
Diagnostics mainf337751c6.

Twelve native data captures cover three target record builders and four
retained C13 equality cases. Each imported raw C13 text has a captured hash.
The added fixture handles use explicit fixture:unbound names, with no sealed
or authenticated identity. Incomplete policy evidence remains unavailable.
Legacy accepted/verified/semantic-authority fields in raw construction are
compatibility data; the stage cannot turn them into admission or authority.
These fixtures are not valid canonical C14 input packets.

Focused regression cases cover field and provenance transport, native wrapper
parity, strict record-drift rejection, wrong C11/C13 context, late diagnostic
Var reads, complete current-source compilation and rejection of malformed or
unbound inputs. Source/body conservation and captured data comparison are
checked separately from canonical input authentication.

## Unsupported boundaries

This slice does not implement canonical lower/verify APIs, C11/C13 or domain
IR admission, profile/target/ABI/runtime/provider/proof legality, source identity
binding, certificates, backend execution, migration, release, self-hosting or
seed retirement. Supplied comparison/diagnostic operations are trusted seed
dependencies, not proof. Existing input and source-verification gates remain
required before target publication. Raw native records and their status labels
do not authorize unchecked backend emission.

Focused checks, exact independent acceptance, committed contextual lint, the
candidate's own CI and clean current-main integration preflight are required
before integration. Namespace presence and this reconstruction increment do
not establish completion of the full Lowering namespace.

## Working-source regression results

The initial focused suite passes 6 tests / 100 assertions. Affected Lowering,
optimization and C/LLVM/Wasm data tests together pass 28 / 582. Ownership passes
31 / 975, language boundary 6 / 11 and docs 240 / 19. These are working-source
observations before the later committed candidate; they do not establish CI or
integration eligibility by themselves.

All twelve complete and serialized native captures match, and imported input
hashes verify. Frozen-parent replay conserves the three constructor bodies and
arities, three exact-join bodies, six Var ports and complete consumer/Bootstrap
text after the declared relocation. The complete current C14 source plan and
three source-refusal captures also match byte-for-byte across the change.
The fixtures remain explicitly unbound; the actual Gravity source rejects both
missing inputs and those fixture handles with C14-INPUT.
