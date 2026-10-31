# Normal extension carrier and replay decision

## Scope

Successor to immutable `e46fa2e15d5df4860c246f2d8f71a44a2c8d97d4` /
tree `ceb85ed77eef618de49f21b411284eb0c7e18a65`, for
`reader/finite-remaining-work/extension-policy-closure` (`FL-P01-T01`).
The independent source-contract/fixture-definition disposition is consumed
once: `/tmp/reader-binding-independent-disposition.json`, SHA256
`cb0ad00286978dbd0ffc514c4259f511a007570a547a4e55688798945ff7d161`.
It accepted definitions only and qualified the interface finding: the existing
policy argument can carry registrations. An extra positional API or separate
authentication protocol is not required. The original boundary report remains
historical; this decision refines its first-product requirement.

## Carrier decision

Use the existing normal `reader-policy` argument for data. Preserve the exact
legacy four-field standard case and fixed inst/uuid selection. For a future
custom-policy case, the smallest proposed additional field is
`:extension-policy`, containing the existing generic leaf policy fields
`:id`, `:profile`, `:registry`, `:enabled-tags`, `:build-effects` and
`:capabilities`. This is a proposed extension, currently rejected by the exact
policy schema, not a field accepted by today's Reader.

The selected policy's canonical identity must join the existing source-unit
extension-policy identities. The selection producer must derive enabled tag
codepoints from its validated registry and enabled set. Native carrier checks
precede map/protocol use. Only the private implementation table may resolve
handler names. Effects and capabilities come from that implementation and
must satisfy the existing subset checks. No callable, caller purity assertion,
authentication boolean or trust-bearing result hash enters the policy.

The binding must include the selected Reader implementation source identity
from the existing plan/envelope binding, and the private handler identity from
that implementation. A handler symbol alone does not authenticate executable
bytes. Changing implementation invalidates dependent invocation evidence.
No namespace guard or standard policy rule is weakened by this decision.

## Actual result boundary and minimum required producer/consumer

The concrete current source is
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`:

- `sh03-reader-policy-valid?` (line 1537 at the inspected base) requires the
  four-field schema. `sh03-registered-tag?` recognizes only inst/uuid.
- `sh03-extension-invocations-for-tag` produces form id, payload form id,
  span and raw source references. `sh03-reader-extension-invocation-record`
  selects fixed standard handler codepoints and labels occurrences invoked.
  It does not execute the host handler or contain its returned seed.
- `sh03-verify-reader-result` (line 5138) computes expected data by calling
  `sh03-read-source-unit source-unit source-bytes reader-policy`, then compares
  the complete result. It has no host provider/result replay input.

In `00813-sh03-reader-canonical-identity.clj`,
`sh03-reader-resolved-result!` passes the existing three producer arguments
and four verifier arguments, verifies the raw result, then resolves digests.
In `00815-sh03-reader-normalization-deferred.clj`, `sh03-reader-host-values!`
executes standard host handlers later. `dispatch-standard-tag` returns the
actual value and an unsealed observation. Current raw-result verification
therefore cannot authenticate that later returned value by replay equality.

The first missing result API is an implementation-owned producer and verifier
for the actual extension syntax-seed result at this boundary. Its minimum
data joins are policy identity, implementation identity, source content and
form/payload/span identity, actual input seed and returned seed, and invocation
identity. The existing result argument and invocation record family can carry
these data after an explicit schema extension; no extra callback argument is
needed. The consumer must reconstruct pure results from original bytes and
compare every join, rather than copying an unsealed observation into a trusted
record. Producer/verifier/source-option/normalizer changes must agree before
custom tags can become admitted. This is concrete remaining implementation
work, not a request for generic permission or whole Build completion.

## Replay decision before effects

Pure deterministic reconstruction may invoke the selected pure implementation
again with the same verified original-byte seed. Fresh verification must
derive its own evidence, compare returned seed and all identities, and never
reuse a prior invocation's acceptance as authority.

For `gravity.reader/read-input`, fail closed before the FIRST provider call
until an implementation-owned build result producer and verifier bind the
grant and recorded result. A valid original grant alone does not authorize a
fresh verification operation. Do not execute an original effect and only then
discover that its result cannot be verified.

L12 requires declared inputs and replayable compile-time traces; L15 requires
provider id/version, determinism/replay behavior and artifact schema, plus
grant principal/capability/provider/scope/phase/lifetime/audit policy. The
existing leaf's exact seven-field grant subset and trusted `:invoke` function
are not that recorded-result protocol. A source search of the Reader dispatch
directory and Gravity Reader found no consumer for recorded provider results.
This does not assert that no other project subsystem has such a product.

The minimum required effectful product must identify the provider and version,
authorized scope and input content, selected handler/policy/source, build
phase and invocation lifetime, audit/replay policy, actual returned seed and
its provenance. Its owning verifier must prove those joins and distinguish
original invocation from fresh verification without ambient access. Until
that producer/consumer exists, neither supplied hashes nor a serialized receipt
constitute authority. No replay grant or generic provider API is invented.

## Fixture/assertion definitions

[extension-replay-assertions.json](extension-replay-assertions.json) extends
the original five paired definitions with whole-population refusal and
original/fresh replay assertions. Four additional paired files cover a pure
tag followed by an unknown tag and a pure tag followed by an effectful tag.
Every refusal requires zero handler and provider calls across the population.
Cross-source, stale policy/implementation/handler/grant, missing replay and
ambient-provider substitutions are defined as pre-effect negatives. These
are proposed integration assertions; no current acceptance is claimed.

Returned-seed and payload-form mismatches have separate fresh-reconstruction
assertions: pure reconstruction may run to discover a mismatch; provider
calls must remain zero. The packet does not incorrectly claim all result
tampering is detectable without running any pure implementation.

## Concrete effectful alternatives

1. If an existing authenticated build-effect transcript producer is identified,
   bind its immutable invocation/result to explicit permission, provider/version,
   handler implementation, original source/input and audit/replay policy. Add a
   consumer in `sh03-verify-reader-result`'s reconstruction path that validates
   that actual product without invoking the provider again. Preserve the
   existing original-byte reconstruction and normal result comparison; a
   transcript must supply verified result data, never a bypass for those checks.
2. With the presently inspected Reader interfaces, keep effectful normal
   invocation and fresh verification explicitly unsupported and fail closed
   before effects. Implement the named grant/result producer and verifier
   before admitting `read-input`; no original effect is run speculatively.

Recommendation at this source is option 2. Option 1 is the intended admission
direction when its actual trusted producer/consumer exists. No contract found
here authorizes repeated provider effects merely to validate a product, or
trusts a caller-supplied transcript. No user decision between blanket replay
and invented receipt trust is necessary. This decision is bounded to the
Reader interfaces inspected, not a claim about all Build implementations.

## Frozen outcome and limits

This successor freezes the concrete carrier/result/replay decision and test
definitions. No production schema or execution path is changed: carrying a
registry alone would leave actual host results outside verifier coverage, and
enabling effects would have no justified replay producer/consumer. Pure normal
admission remains implementation work; granted normal admission remains
unaccepted pending the precise recorded-result interface above.

Source/data validation checks JSON shape, case and mutation inventory, pair
byte equality, ASCII, referenced inputs, source anchors and Git whitespace.
No Reader, handler, Bootstrap, compiler, runtime, provider, helper, lease or
probe runs. Executable Clojure validators and the language-boundary gate remain
unrun under that restriction. The original blocked goal and PR571 history,
original frozen supplement, production source, task and lifecycle records,
and Accounting-owned resource work remain unchanged. No publication, review
dispatch or integration is performed.

Governing contracts: [C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md),
[L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md),
[L15](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md),
and the existing Reader [spec](spec.md), [plan](plan.md) and [tasks](tasks.md).

## Observed data validation

On 2026-10-30, shell and `jq -e` data checks passed for six cases, eleven
pre-effect mutations, nine result joins, two fresh-result mutations, unique
case/mutation names, unrun status, no runtime schema or callable/auth/purity
fields, zero defined provider calls, and no granted-case acceptance.
`jq -r` source paths all exist. `cmp` passed for all seven inherited/new
source pairs. ASCII scans passed. Four in-memory JSON corruptions were refused
by their corresponding data assertions: executed status, one provider call,
granted acceptance, and a claim that fresh pure reconstruction never runs.
These checks test definition consistency, not implementation semantics.

`rg -Fq` confirmed the existing verifier reconstruction call, producer argument
vector and unsealed observation marker. `git diff --exit-code e46fa2e1 --
bootstrap/clojure/src bootstrap/gravity/src contracts
specs/reader-workflow/tasks.md` passed. Staged `git diff --cached --check`
passed before the local freeze.

Assertion packet SHA256:
`1285b77727b9e4127c3afaad5f07914993f7058cc907489ff51c4fb008dc188b`.
Inspected Gravity Reader SHA256:
`a74e3d1d3bc3d7627493586c78367844b6ba41980cb65b2bee1638fe3273928a`.
The other inspected source hashes remain those pinned in the predecessor.
