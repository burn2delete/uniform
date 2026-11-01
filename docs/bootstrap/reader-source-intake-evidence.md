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

## Unsupported boundaries

This remains temporary native host file intake. Trusted Vars are dependencies,
not authentication or authority. No new filesystem grant, sandbox, symlink or
race guarantee, alternate encoding, replacement decoding, source quota, canonical
C2 authentication, complete Reader/Core conformance, migration, release,
self-hosting or seed retirement is claimed. Java filesystem, heap and process
restrictions remain physical host boundaries. Historical denied Reader/H6/native
and full-compatibility routes remain unused; admission and migration pins stay
unchanged.
