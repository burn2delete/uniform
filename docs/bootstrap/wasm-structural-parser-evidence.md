# Native Wasm instruction and module parsing

## Governing contracts and source composition

D1/D2/D3/D6/D8/D9, C1/C11/C14/C15, B1/B4/B13/B14, TEST2 and BOOT8 govern
this extraction. The source cohort is
83e8245597df85077567ff621088a6ce278bd2a6, tree
b893f3ef9d7a7573c06b28ac2ffa14edd5c5078c. Its reviews and CI remain
separate. An unchanged import of verified published Main
9c34e295881ca1bf3ecbb9e4a8f12e0e8c1c7315, tree
cd77c47a63ec4a6135c67565df61eb2a50c2e2b0, produced local composition
7b193f5a9eebaa04c7f8d57a0d28979cca596e3d, tree
f263de6f68753e4315b875af570a6222faba0cb5. Its ordered parents are
[83e824559, 9c34e295]. Exactly 18 incoming paths equal Main's modes/blobs;
every other source path survives. No runtime/controller state, execution or
authority follows from this composition. The original 12-path source lease
remains active; incoming immutable
Main files did not require separate writer reservations.

## Stage ownership and artifacts

Move four existing algorithms from components 00756-003/004/005 into
gravity.backend.wasm: parse-instructions!, evaluate-decoded-ast,
flatten-decoded-ast and parse-module!. Preserve the native opcode and section
rules, stack/definite-initialization checks, recursive operation selection,
decoded evaluation, operation byte spans, first refusals and report data.
Explicit live owner operations retain failure construction, canonical integer
decoding, recursion, set intersection/map inversion and comparison-catalog
lookup. The stage imports/discovers no Bootstrap owner.

Inputs are caller-owned native byte vectors, offsets, stacks/locals and an
expected reconstruction packet. Outputs are decoded AST/stack/local records,
scalar results and the existing relative module report, or unchanged owner
refusals. A passed report compares supplied native data; it authenticates no
IR, source, callback or backend artifact and issues no certificate. Bootstrap
keeps source binding, backend input eligibility, authentication, publication,
Node/process/staging and all private execution routes.

## Remove the artificial byte refusal

Retire p15-s23-b4-wasm-max-module-bytes = 65536 and its three enforcing clauses
in parser 005, frozen-envelope 009 and integrity 010. Do not raise or rename the
quota. Preserve minimum header length, vector/octet legality, canonical ULEB32
and signed i32 encodings, section/body/index representability, hashes,
provenance and tamper detection. All other envelope/integrity code stays exact.
Host heap and process watchdogs remain separate from compiler accounting.

## Tests and integration evidence

The existing whole Wasm pipeline passes 6 tests / 151 assertions both before
and after the pure Main import. It compiles the current Gravity B4 helper
source through the ordinary fixed seed and emits real scalar core-module
bytes, with actual malformed encoding/opcode/join refusals. Extend this route
to the structural parser for straight and single-diamond modules; compare
immutable old and extracted algorithms on identical live inputs. Capture
complete bytes, expected packets, source/compiler hashes, reports, refusals,
byte coordinates and deterministic identities. The intended quota change is
separate from below-quota algorithm parity.

Add a valid emitted module above 65536 bytes and reject real header/section/
instruction/stack/result/byte-plan corruption. Preserve late/recursive Var
interposition and original owner exceptions. Add the complete Wasm namespace
to the existing contracts CI group with the current 2g/180-second runner;
retain every prior command, matrix, workflow and path decision. Selection
controls must detect omission/substitution and cover every current deftest.
Run focused, boundary/ownership, CI harness/format, contextual lint and document
checks, then independent semantic/adversarial review, own hosted CI/artifact
audit and exact current-Main coordinator integration. Maintain the early draft.

## Unsupported boundaries

The native dialect remains one scalar no-argument main function, straight
control flow or one structured conditional, i32 locals and the existing
comparison set, exact core sections, no imports/memory/tables/globals/start/data
or custom sections. This does not implement full B4, general Wasm validation,
WIT/WASI/components, target execution, verified B1 ingress, proof/source
authentication, release, migration, self-hosting or seed retirement. Private B4
and Node routes are not executed. The scope record itself earns no completion
or integration credit.

## Quota correction and original capture

The original scope source 77a1f353433e694d536e33a50b116b7aee409d1d emits
two small modules through the compiled Gravity B4 helpers and a 67417-byte
native module containing 7500 real constant/local operations. Its 27 complete
records contain two reports and 25 refusals, including the artificial refusal
of the large valid module. Capture SHA256:
fc5d583a0d1020ee1b79273fcd77b2c44ec687c7f6067098935bc977aab50c4d.

The constant and exactly three size predicates are now removed. Minimum
header/octet checks remain; the integrity function hashes every valid byte
vector, including oversized vectors. No other source binding, authentication,
hash or envelope rule changes. Before extraction, the whole Wasm namespace
passes 8 tests / 185 assertions, including genuine Gravity-emitted small
modules, malformed-module refusals and the 67417-byte module's complete
relative report and operation map. No private B4 or Node route is exercised.

The corrected clean baseline is
0f8a2e66e845ceb3dcde41f858ed543d644c024b, tree
b63fff2745c8330b469e174bcbea617d5e32793b. Its complete 27-record capture
contains three reports and 24 refusals; SHA256:
cc4159c4c22fc4a0ae902b7c7c9e18198ac4620f42a690098659b5efc070dd30.
Only the intentional large-module outcome changes from the original quota.

## Implemented extraction and initial verification

The four bodies now live in gravity.backend.wasm, with explicit per-use
function Vars and a late comparison-catalog Var. Bootstrap facades retain
their old arities. The stage exposes 23 public APIs, cold-loads with no aliases
and does not load Bootstrap. Its new reports remain native relative data.

The immutable corrected-baseline bodies and extracted bodies consume the same
live inputs in the independent-algorithm control. All 27 complete raw and
parsed outcomes match, including the large module; inputs remain unchanged.
The paired record SHA256 is
c8e50b68031b4d949fec151f7fd27c77c188b1ad61342fb5c82da815da4de8a1.
Conservation recovers all four prior bodies and the entire prior Wasm stage,
and preserves every other byte in the three parser components. One redundant
nested let is merged with its outer bindings to satisfy lint; reconstruction
restores its original scope and text. The constant and two private-envelope
components differ only at the three declared quota clauses and definition.
Their other hash/authentication/provenance code is unchanged; private routes
remain unexecuted.

The whole namespace passes 12 tests / 282 assertions. New controls cover
genuine compiled Gravity byte modules, large emission, header/section/opcode/
stack/initialization/result/byte-plan refusals, relative byte coordinates,
input retention, recursive Var interposition, callable selection before
argument lookup, late catalogs, returning failures and original exceptions.
The initial new returning-failure assertion indexed a list as a map; that
observation NONPASS is retained and corrected without changing production.
The initial source helper compared freshly generated anonymous-fn symbols;
its NONPASS is retained and corrected with exact single-form boundary checks.

CI harness controls pass 52 tests / 552 assertions, format and direct lint
pass, and language/ownership checks pass 48 tests / 1202 assertions. The
complete Wasm namespace is appended to the existing contracts group with
2g/180 seconds, all prior commands retained, and exact whole-namespace plus
omission/substitution controls. Contextual lint, final document validation,
independent implementation review, own hosted CI/artifact acceptance and
current-Main integration remain pending. PR1210's Macro watchdog NONPASS is
separate retained history; it grants no acceptance to this increment.
