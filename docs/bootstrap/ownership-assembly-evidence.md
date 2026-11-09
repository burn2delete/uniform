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
This API authenticates neither callbacks nor input artifacts. Existing native
callback behavior remains with its owner and supplies no source authority.

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

The working projection passes 23 owning tests / 970 assertions. New cases
verify the explicit non-authority envelope, original map/input identity and
metadata retention, validation/internal-proof/projection-proof/identity order,
duplicate proof calls, the exact proof-enriched hash input, all eleven native
missing-field refusals before identity, missing/unknown assembly keys before
input observation, original callback exceptions, captured functions versus
late Var roots, and source/file sentinels. Opaque Associative failures precede
callback-type errors as before; no new callback type check is introduced.
The separate cold C9 regression passes 4 / 94 with Bootstrap absent. Exact
source checks conserve all prior functions/data, the source/file prelude,
wrapper arities and every legacy construction/validation/proof/identity form.

The clean structural baseline capture SHA-256 is
12df2362973bb5d9ccc58cd795026d2336e024f215cd3add7d6ff686d6e4cbc0.
It records four native constructions, seven native profile refusals, eleven
missing-field refusals, two original opaque failures and two unsupported-view
host refusals. It provides structural comparison only, not source or C9
acceptance. Separately the actual paired Gravity fixture plans and current
engine execute eight accepted and twenty-eight rejected normalized requests,
retaining actual checkout source paths/bytes/hashes and recomputed plan IDs.

The first baseline capture used an ILookup-only fixture where select-keys
requires an Associative view, so its expected custom-error assertion failed
before any evidence record was written. The initial helper/log/report remain
retained. The corrected fixture supplies that original interface; separate
unsupported native objects retain real host refusals. No implementation rule,
source pin, admission path or gate was weakened. Committed parity, remaining
gates, independent implementation acceptance, own CI and integration remain
outstanding.

The language/source ownership gates pass 48 / 1198 with zero skipped
namespaces; pinned stage/helper/contract/test lint has zero errors or warnings.
Document, roadmap and workstream validators pass. Committed parity/lint,
independent implementation acceptance, own CI and integration are still
separate requirements.

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
