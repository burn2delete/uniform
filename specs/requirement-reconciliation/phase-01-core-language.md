# Core language family proposal

Scope: `requirement-reconciliation/phase-01-core-language`, original L1-L19 only.
State: draft proposal; independent review and central adoption pending.
Owner: bounded core-language source-family reconciliation owner.

The [family artifact](phase-01-core-language.edn) is a guarded field patch,
with the complete preserved family occurrence/facet/requirement context. It is
not a replacement manifest or another task population. The canonical owner
alone composes it with other families and adjudicates overlap and acceptance.

## Inputs and conservation

The applying input is the preserved uncommitted PR445 manifest, SHA256
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
Its committed predecessor at `e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30`
has the different hash `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
These inputs are not interchangeable. The artifact preserves prior Language
decomposition, source-review receipts and historical evidence without replaying
the held Language/Foundation composition or granting it new authority.

PR554 input head `53ae6a403b20d15155bb5b04911fe7ac0092b921` pins the
original/current document and task inputs. Its input file SHA256 is
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.
The task/source base is `6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. Original source is
`a1b991be499d50579a8b4366976c519ae8949224`; all 19 original/current
document hash pairs match. Original byte offsets and the exact published
`source-occurrences` API at `c0dc5790197a1bbd3a8aa285e5458c272d877220`
remain authoritative for these source identities.

The artifact retains 2,783 occurrences (including layout), 2,069 facets and
161 existing requirement records. It preserves every nonblank disposition,
all cross-family ownership links and the full original requirement records.
No semantic IDs are added, merged or deleted. There are 28 guarded field edits;
25 refresh changed applying task pins and three refine the provider startup
interface. Every edit includes collection, ID, field path, presence and exact
before/after values. Stale, duplicate or incompatible applications refuse.

## Concrete task-interface correction

`language/provider/initialize-runtime-contract` already describes L15 startup
validation. Its five preserved facets include the Runtime Providers paragraph,
the six required initialization checks, and the missing-provider/fallback rule.
The previous Auth hook reference does not entail actual selected runtime startup.

The proposed `task-source-amendments` entry refines the existing managed/native
Runtime startup task, preserving its literal ID and Open status. It requires
provider existence, artifact versions, scoped grants, compiled contracts,
profile/target compatibility and enabled safety/audit policy before user code.
A missing provider must reject unless a declared fallback applies. Positive
fallback and negative mismatch fixtures become explicit closing evidence.
Auth selection/grant tasks remain dependencies and an additional interface.
Runtime maintainer retains execution ownership; no individual is invented.

The task file amendment includes exact before/after content and SHA256 pins.
After central application, regenerate the existing Runtime roadmap projection;
the supplied pure projection test checks the proposed source. This draft does
not alter the canonical Runtime task file, roadmap or main manifest.

## Unresolved and nonclaims

All 161 requirements remain unassessed for current implementation acceptance
and unresolved for central nonoverlap. The artifact lists every stable ID.
Thirty-six requirements retain explicit missing task interfaces. Existing
partial bindings retain their gap text; fresh task hashes do not prove full
semantic entailment. This proposal reviews only the concrete L15 managed/native
startup amendment, not every inherited task binding.

Eleven source facets retain their existing unresolved accounting-supersession
records, precise raw spans, proposed decisions and preserved neighboring
semantic conditions. Their IDs are listed in `unresolved-facet-ids`. They
receive no new scope exclusion or acceptance. No compiler quota or replacement
accounting mechanism is introduced. The draft does not confer release,
self-hosting, migration, implementation completion, denominator or ratio credit.

## Observed validation and delivery

- Family exact input check: all 19 source hashes, occurrences, raw facet hashes,
  owners, nonblank dispositions and applying task hashes/markers/IDs.
- Family controls: 3 tests, 191 assertions, zero failures/errors. They reject
  stale/duplicate edits, missing IDs, semantic/alias field changes, wrong source
  bytes/occurrences and wrong task hashes/markers/IDs. Unchanged requirement
  fields preserve rejection conditions and historical review/evidence records.
- Existing Runtime projector controls: 4 tests, 67 assertions, zero failures/errors.
  Proposed task parsing preserves five IDs and five Open statuses; the proposed
  source produces a valid roadmap projection.
- Language boundary: 6 tests, 11 assertions, zero failures/errors.
- Structural docs and full-language roadmap validators passed.
- No compiler, provider, runtime, broker, heavy historical lane or held builder
  ran. No independent review is claimed; central owner routes it.

Reproduce preparation/check with
`clojure -J-Xmx512m -M tools/core_language_reconciliation.clj --check PRESERVED_MANIFEST`
using the exact input hash above. The tool reads the preserved manifest and
immutable Git inputs; it never changes the canonical manifest or task source.
Its `--write` mode writes only this bounded family artifact.

The requested new branch was denied by the filesystem sandbox. That denied
operation was not retried, transferred or routed around. HEAD/tree remain the
exact frozen base; delivery is a content-hashed working patch, without a new
commit or PR. Canonical owner should review this immutable patch, compose the
guarded fields against its actual manifest, and apply the task amendment only
with the corresponding projection update and unchanged Open status.
