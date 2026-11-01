# Reader native source intake extraction

## Purpose and governing contracts

Move the existing native `read-gravity-source-text` body from Bootstrap
component00778 into `gravity.reader`. D1/D2/D3/D8/D9, L1, C1/C2/C3/C15,
BOOT1/BOOT8 and TEST2 govern source bytes, extension/encoding diagnostics,
provenance, stage boundaries and regression evidence.

## Declared boundary

The new stage API takes a path and three explicit late owner Vars: the existing
co-canonical extension predicate, path-policy refusal and captured-byte decoder.
It retains extension refusal before valid-path byte capture, the existing Java
file read, exact path transport and UTF-8 decoding. Bootstrap keeps the public
compatibility function, source-policy diagnostic construction and its invalid
extension hash capture. Existing stage decoding and artifact assembly remain
unchanged. No source-callable builtin, capability grant or alternate admission
path is introduced.

## Evidence obligations

Initial base isec15f95051e6e9e674974d43f5a14e6555cf176a. Capture real valid and
malformed files in both source spellings and actual invalid-extension and host
file failures. Preserve complete results/diagnostics, byte hashes/spans, paths,
extension provenance, exception identity and late dependency lookup order.
Emit genuine complete public current-reader Core artifacts in both spellings
before and after extraction and compare full values, serialization and
recomputed identities. Preserve the whole native component except this one
wrapper and the exact existing stage assembly/decoder bodies.

Run focused and affected tests, language/ownership gates, docs/roadmap/governance,
contextual lint and independent review. Maintain an early draft PR; own CI and
exact clean current-base integration preflight govern integration. This scope
commit carries no implementation credit or new authority.

## Implemented development evidence

The exact former file-read body now lives in `gravity.reader/read-source-text`.
Its three owner lookups are explicit late Var ports. Bootstrap's compatibility
arity remains `[path]`; its extension diagnostic/hash capture and all surrounding
component behavior remain unchanged. Existing decoder and artifact assembly
bodies remain exact. Metadata and the stage map distinguish native file capture
from supplied-byte decoding and assign the extension refusal to its existing
owner. No Bootstrap or CLI dependency is introduced into Reader.

Real native controls accept two files, reject malformed UTF-8 and invalid
extensions with their original complete diagnostics, and preserve actual missing
file/directory host errors. Both Reader artifacts preserve exact source hashes,
paths and extension provenance. Before/after complete public development-reader
Core controls accept both complete `compiler-exact-keys` subjects with25 nodes,
one API target read, source SHA
`8e63ea9e37ab40a8e66975b3e58dc8eb5b9b74aa339d2625d5bd67070802758d`
and recomputed Core id
`sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e`.
The complete390-function current C6 source is compiled; its generic-seed,
development-unadmitted and non-self-hosted boundaries remain explicit.

Focused Reader tests pass15/230, coupled CLI/Reader/Build/runtime/package/library
and literal tests44/667, selected public ingress4/50, ownership38/1093 and
language6/11. Isolated native tests cover extension refusal before decoding,
policy-before-byte-capture order, late decoder replacement, original dependency
exception identity and real missing-file failure before decoder invocation.
Those trusted host interpositions are separate from the genuine Core controls;
no fabricated authentication packet or guard/pin substitution is used.

Controls and verification use `/tmp/uniform-reader-source-intake-*.clj`.
Native products are under `/tmp/uniform-reader-source-intake-evidence/`; complete
Core products use `reader-source-{before,after}-*` in the existing CLI evidence
directory. Full data/text equality and whole-component/assembly/decoder
conservation remain exact checks. Contextual lint, independent exact review,
own CI and clean current-main preflight still govern integration. This is a
bounded native extraction, not Reader or Core completion.

## Leaf CI dependency main composition

Actual main09e48072fbb69131dbe3a7826b4dbbc4c9bb45f0/tree
c63848d2d311935d3b0764c64a2bb0e6675f70e6 changes exactly four CI workflow,
runner/test and documentation paths. The normal composition preserves all four
byte-for-byte. Reader code, its compatibility component, tests and all executed
compiler/runtime/fixture inputs remain exact36a6d6eb. The leaf selector uses a
full fallback for unknown, cross-cutting and closure-discovery changes; it adds
no source-Core, migration or release authority. Fresh current-base controls,
conservation, focused checks, independent exact review, own CI and clean
integration preflight remain required. Older source checks remain bound to
their actual conserved inputs; scope-only or older-base CI is not transferred.

## Verified Release main composition

The next normal composition uses verified Release PR741 main
b40ddaaca5aafcd1f955f9b3c73cb6dd965d34bf/tree
8bd22beb1ab157c6751393a4428078565fcab01f. It has no conflicts. Release code,
component, tests, evidence and registrations remain exact main. The shared
namespace map preserves Release's row and merges Reader's row. Reader's
source-intake body, ports, component wrapper and tests remain unchanged.

Fresh combined tests pass50/732, including real JAR acceptance and tampered
archive rejection. Native controls still accept two files, retain seven actual
refusals/host errors and emit two Reader artifacts. Complete current-reader Core
controls and exact comparisons use `release-main-*`. Ownership/language, docs,
contextual lint, independent exact review, own CI and clean current-base
preflight remain gates; previous-base green does not transfer.

## CI harness environment evidence

The current CI runner tests pass20/221 with the installed linter directory on
PATH. The initial invocation lacked that directory and recorded20/192 with two
missing-linter errors; its nonpass log remains at
`/tmp/uniform-leaf-main-ci-runner-missing-linter-nonpass.log`. Correcting the
command environment changes no repository source, guard, policy or watchdog.
Terminal harness receipts are archived outside the source checkout.

## Unsupported boundaries

This remains temporary native host file intake. Trusted Vars are dependencies,
not authentication or authority. No new filesystem grant, sandbox, symlink or
race guarantee, alternate encoding, replacement decoding, source quota, canonical
C2 authentication, complete Reader/Core conformance, migration, release,
self-hosting or seed retirement is claimed. Java filesystem, heap and process
restrictions remain physical host boundaries. Historical denied Reader/H6/native
and full-compatibility routes remain unused; admission and migration pins stay
unchanged.

## CLI launcher main composition

The normal composition uses actual main
da68cb09ca88710a4a57a14b9d14dc612a743c32/tree
1a46d2728d826f07d0f555fe9fa52e0ecbad1391. Its four incoming paths add
the lazy CLI launcher and its tests, select it for the :gravity alias, and
register launcher tests in the CI runner. All four remain byte-identical
to main. Stage algorithms, compatibility components, tests and executed
compiler/runtime/fixture inputs remain conserved from the Release-main
composition. No source admission or authority changes are introduced.

Fresh ownership/language checks pass45/1118, and fresh-JVM launcher
checks pass2/32. The actual new :gravity entrypoint emits complete Core
for both compiler-exact-keys spellings with the explicit
--development-current-reader flag. A real malformed UTF-8 file exits1,
emits L1-SOURCE-ENCODING on stderr and emits no artifact on stdout.
Current-base whole-component conservation uses launcher-main scripts;
existing complete native/Core comparisons remain tied to their unchanged
actual source inputs. Prior-base CI remains historical. Exact independent
acceptance, own CI and clean current-base preflight still gate integration.

The launcher main changes deps.edn, an actual Reader project-identity input.
A strict initial comparison against the older complete API artifact fails
because the project-root/source-unit identities and their dependent records
change. The nonpass is retained in uniform-launcher-main-artifact-verify.log;
no fields are removed or rewritten to obtain equality. New complete API
controls use the current manifest and compare against actual CLI output.
The source-byte hash and semantic Core id remain unchanged. Older full
artifact comparisons retain only their historical input authority.

## Shared compiler test support main composition

Actual main eb61e3823435b57c9d8e1bcb0f7fd136df6e4086/tree
88c309d8013d169813765c2e9c4ba2dfb08ba396 adds exactly five test-only
changes: shared normalized SH08 fixtures, their SH01/SH08/SH09 consumers,
and removal of internal membership-call-count assertions and non-authoritative
timing receipt equality. The normal conflict-free merge preserves all five
files exactly. Implementation, deps.edn, stage tests, fixture sources and
the source inputs of the current controls remain unchanged. No fixture
labels or timing data are promoted into compiler authority.

Current-manifest native intake and argument products retain two complete/text
pairs; the prior four-pair comparison remains historical. New genuine Core API
products and actual CLI products are compared without dropping identity fields.
Whole-component conservation is rebound to this exact base. Contextual lint
receipts bind da68 and its unchanged baseline source files: Reader3 paths/0
warnings/0 blockers; Safety7 paths/3486 unchanged warnings/0 blockers.
Terminal receipts are archived outside the source checkout. Exact source
review, own CI and clean current-main integration preflight remain required.

## Loader alias CI main composition

The next actual main is8a980ac27d9392c3e85eaead76646fc7f3f38810/tree
dc2ba33e28b557b618abe61de8b25e857962faa0. Exactly two CI runner/test
paths change: use, clojure.core aliases/renames and refer-clojure renames
now fail conservatively to the full leaf suite. Both paths remain exact main
through the normal conflict-free merge. No compiler, runtime, manifest or
control inputs change. Whole-component conservation and provenance are
rebound to this base; existing actual controls retain their unchanged inputs.

Both complete current-manifest API/Core products have passed, including
exact equality with the original CLI artifact data and raw text, with only
the CLI newline and outer API evidence wrapper excluded. An intermediate
parse/reprint text comparison changed nested map iteration order and failed;
its log remains uniform-launcher-main-current-artifact-verify.log. The original
raw comparison passes without removing or rewriting any artifact field.
Independent reviewers accepted the prior exact source and closed that
semantic-evidence condition. This composition still requires exact review,
own CI and clean current-main preflight; prior-base CI remains historical.

## Current accounting, custody and release-proof composition

Actual main 7ad0edc7abcc7ea7b8f536e8363bd66ad2329453/tree
5271f1984a28f9da39b24beb999ad2038fc4c210 includes PR749 accounting
removal, PR746 host custody and PR748 read-only release verification. The
[ordinary-development record](current-ordinary-composition-evidence.md)
distinguishes current semantics from immutable historical A1 evidence.
All incoming implementation and tests remain preserved; Safety's shared
registrations are rebuilt from exact main plus its frozen association,
including the new process helper and unchanged primary catalog formulas.
Stage algorithms and fixtures remain conserved. Fresh current-base controls,
catalog identities, ownership/language, contextual lint and independent review
remain required. Old-base CI supplies no current integration eligibility.

Fresh current-base coupled checks pass37/530, ownership/language46/1136.
All4 complete data and original serialized pairs, actual source hashes,
deterministic identities and whole-component conservation pass. Current Core
controls still accept both spellings with25 nodes/one target read/source8e63
and recomputed Core16ec. Contextual lint passes3 paths/0 warnings/0 blockers.
Terminal lint receipts are archived outside the source checkout. Source
admission, historical denied routes and migration pins remain unchanged.
