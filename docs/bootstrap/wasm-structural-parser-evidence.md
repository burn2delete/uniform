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
vector, including oversized vectors. No other source binding, authentication,+hash or envelope rule changes. Before extraction, the whole Wasm namespace
passes 8 tests / 185 assertions, including genuine Gravity-emitted small
modules, malformed-module refusals and the 67417-byte module's complete
relative report and operation map. No private B4 or Node route is exercised.
