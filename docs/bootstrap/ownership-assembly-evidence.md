# Native Ownership artifact projection

## Boundary and contracts

D1/D2/D3/D6/D8/D9, L10, SAFE2-SAFE5, C1/C7-C11/C15, BOOT8 and
TEST2 govern this increment. Its immutable predecessor is
bd0ad4494f4dafd5e6bcce9ba58bcc54a2964965, tree
a2ba570b51758ebebd21b97626bc094132e777b5, retained in draft PR925.
That unmerged dependency is not current main.

The new native-artifact-projection API moves only the existing C9 assembly
tail after source intake, parsing, C8, graph/record, diagnostic and verifier
production. It accepts captured native values and a fresh five-key assembly
map: :validate!, :capability-proof, :artifact-id, :governing-document and
:diagnostic-ids. Missing or unknown operation keys are refused. No callback
type check, re-acquisition, deep copy, source reading or admission is added.
Opaque values and original host failures retain their identity and lookup
behavior. The source-path argument is metadata only.

The stage returns a :gravity/native-c9-artifact-projection envelope with
:admission :none and :authenticates-inputs? false. The legacy raw map is its
:artifact value. Existing source/file facade entrypoints keep their entire
prelude and only unwrap that value after projection; their guarded intake and
old output remain unchanged. The envelope never enters validation, proof
callbacks or digest input. Preserve exact order: construct original map,
validate it (including the internal proof call), project proof again, then
compute identity from the proof-enriched original map.

This reconciles ownership of native compatibility projection with the C9
source facade's unchanged intake, artifact publication and admission boundary.
Native complete/passed/proof labels and content identity remain data. The
stage requires only clojure.set, consumes explicit supplied values/operations
and invalidates no verified IR facts. All twenty-five prior functions and
four catalog values remain unchanged. Callback owners retain native validation
decisions and exception construction; the stage owns assembly ordering and
the closed assembly-operation shape refusal.

## Acceptance evidence

Use the original tail only as a structural native comparison, never as source
or C9 acceptance. New positive/refusal cases call the explicit native API.
Capture original/raw serialized tail parity and actual candidate identity;
test source/file sentinels, original callback/exception identity and order,
duplicate proof calls, envelope exclusion from hashing, missing-field refusal
before identity, closed operation keys and unchanged source/file prelude bytes.
Separately compile and execute existing paired Gravity normalized-transition
fixtures, retaining actual checkout provenance and recomputed identities.

Require focused/adversarial tests, cold C9 regression, language/source ownership
gates, lint, contract validators, independent exact acceptance, own CI/artifact
audit and clean current-main integration preflight. This declaration precedes
implementation and confers no acceptance or integration status.

The prior source-to-C9 C2-HASH refusal remains untouched and uncredited. Do not
invoke, retry, repin, transfer or route around it. The new native API accepts
unadmitted native views and cannot replace source/Core/C8 authentication.
Its callback map must not contain artifact-operations, *operations*, source,
C8, graph or catalog-acquisition callbacks. Existing intended interposition in
captured validation/proof/digest functions remains unchanged.

## Unsupported boundaries

No canonical C9/region/arena/lifetime/linear analysis, source/Core/C8
authentication, certificate/grant/proof/provider trust, runtime-check execution,
target execution/performance, publication, authoritative migration, release,
self-hosting or seed retirement is established. Fixed native examples,
truthiness/vacuity, ignored callback returns and proof/status labels remain
compatibility data. Meaningful exactly-one terminal semantics are unchanged.
Held routes remain untouched and previous slices retain separate evidence.
