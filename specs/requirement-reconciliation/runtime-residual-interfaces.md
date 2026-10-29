# Runtime historical-gap criterion patch

Status: eight finite actionable criterion mappings prepared; independent source
review and canonical adoption pending. Implementation remains Open/unassessed.
This is a patch over existing task authority, not another task ledger.

## Exact assignment and inputs

Canonical partition commit `bd37ab50c50d27094dce664b34831fcc43c3e9c3`, path
`specs/requirement-reconciliation/historical-gap-partition.edn`, SHA256
`f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a`
assigns precisely these eight IDs to phase 08. The partition retains the whole
577-record history, its 13 criterion witnesses and three retirements separately
from the 561 residuals. Coverage receipt SHA256:
`a7e82612a715713799be4db46cc3e911aff0cb5a2525d4555c8f8e6ded87101f`.

The exact later task predecessor is
`43598c5b87feb4d88928549bb20fc4089768718b`,
`specs/runtime-workflow/tasks.md` SHA256
`51b68ffda3eea2e859934d831db59385cd151baaac00a3c946b8d0087aae4de6`.
It includes the composed L15, P4 and SAFE amendments. The earlier family proposal
SHA256 `a6b2ee0fd88a46d20da8d70afa182d54c4ad6b69b86dbb7959adc1edb4214a3f`
supplies unchanged original/current documents, raw byte spans, occurrence IDs,
facets, aliases, review history and proposed normalized-owner routing.

## Per-ID completion evidence

Each row previously had disposition
`:no-same-id-open-row-and-no-full-coverage-witness`. The machine patch preserves
that complete before-record and proposes a source-qualified finite criterion,
acceptance/rejection outputs, dependencies and an existing stable task target.
The new criteria are implementation work, not claims that linked fixtures ran.

| Stable historical ID | Existing Runtime task | Concrete proposed acceptance boundary |
| --- | --- | --- |
| `runtime/debug-service-selection` | managed/native startup and cleanup | Debug/release service difference; provider/source/check traces, redaction and sink grants; reject unmarked debug services and undeclared external access |
| `runtime/family-composition` | explicit family selection | Allowed combination and incompatible profile/package/provider matrix, with typed service classification and hidden dependency rejection |
| `runtime/foreign-symbol-resolution` | memory/concurrency/typed FFI | Admitted symbol resolution and dynamic loading with provider identity, ABI/layout, grants and policy; reject mismatched symbols/providers/ABI and denied loading |
| `runtime/hot-reload-invalidation` | distributed/AI/interactive execution | Change-to-artifact invalidation, transcripts/snapshots and preserved/restarted state; reject stale artifacts and ungranted inspection |
| `runtime/managed-collection-semantics` | managed/native startup and cleanup | Host representation, nullability, equality/hash/serialization/mutability/identity/taint comparisons against Gravity contracts; reject observable divergence |
| `runtime/managed-dynamic-state` | managed/native startup and cleanup | Manifested dynamic/thread/async/namespace/session behavior; reject implicit host authority and lower-profile leakage |
| `runtime/managed-null-conversion` | managed/native startup and cleanup | Conditional Option/Result/checked/opaque conversion fixtures, exception/null mapping; reject unchecked nulls and untranslated failures |
| `runtime/manifest-consumption` | explicit family selection | Actual backend/package/observability/conformance/self-hosting manifest agreement with selected services/providers/startup/failure/enforcement/provenance; reject missing or contradictory records |

The patch conserves all 112 assigned facet memberships, their raw spans/hashes
and historical obligations. Every affected family is phase 08. Intersecting
freestanding-layout, check-proof-preservation and build-input-capture rows are
assigned to other owners and receive no edits here. Historical aliases/splits
are neither accepted nor retired by these mappings.

## Applying the finite patch

[runtime-residual-interfaces.edn](runtime-residual-interfaces.edn) contains eight
mapping records and four sequential edits of existing acceptance fields. Every
edit has collection, stable task ID, field path, before presence/value, after
value and exact applying-file SHA256. All pre-existing acceptance text is a
prefix of its successor. Task IDs, titles, baselines and Open statuses remain
identical; task-count delta is zero. A later source edit, repeat application or
different criterion predecessor must reject, including a central composition
that adds more task semantics. Reconcile against that successor instead of
overwriting it. No shared checkout, roadmap or SH pin is mutated here.

Prepared actionable mapping count: 8. Independently adopted mapping count: 0.
Unresolved interface-design IDs: none in this assigned slice. Independent
semantic acceptance/adoption remains pending for all eight IDs above. Runtime
implementation and the pre-existing 44 mixed-accounting facets remain open at
their original scope. No denominator or completion ratio follows from this patch.

## Reproduction and controls

Use the exact coverage receipt as the final argument:

```bash
clojure -J-Xmx512m -M tools/runtime_residual_interfaces.clj --check COVERAGE_RECEIPT
clojure -J-Xmx512m -M tools/runtime_residual_interfaces_test.clj COVERAGE_RECEIPT
```

Observed focused controls: two tests, 30 assertions, zero failures/errors.
They verify the exact eight-ID/facet population, preservation of the later
predecessor and all task statuses, valid parsing of amended task records, and
rejection of stale/duplicate application, missing or foreign assignments, forged
source hashes, headings substituted for criteria, missing rejection outputs,
forged coverage/adoption credit and deleted historical retirements. Changed-file
clj-kondo reports zero errors/warnings. Language boundary: six tests, 11
assertions, zero failures/errors. The deterministic patch check and whitespace
check pass. These are source-only controls; no compiler/runtime/provider job or
new review lane is claimed.

One early read-only inspection command printed its intended summary and then
reported an extra closing delimiter; it wrote no artifact. The corrected bounded
reads and controls above establish the reported evidence. Original failed
attempts, prior review results and accepted source-family history remain intact.

The canonical reconciliation owner consumes this immutable patch once, composes
later task changes, regenerates derived views and routes independent review.
