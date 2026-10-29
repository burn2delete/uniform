# Original Effects local-call composition controls

Owner: ACC-01. This test-only draft restores the two original Effects controls
from PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635` on main
`e047157d711652a5624012f0da7d6911ce086718`. C7, C8 and D9 require actual
semantic evidence and preserved authority distinctions. No production code or
existing test byte changes in this increment.

## Restored semantic obligations

Restore the complete `mixed-ordinary-local-call-effect-input` helper and both
original `body-list-set-local-call-composition-development-kernel` and
`body-map-keys-local-call-composition-development-kernel` controls. The helper
constructs an actual C7 ordinary local call alongside a List-to-Set or public
Map-key call, deriving ordinary constraints and facts through existing C7 APIs.
The tests invoke real Effects context and call validators through `leaf`.

Retain both row/fact orders and keyed facts; accepted ordinary row/fact omission
of optional family tags; malformed explicit tags; nil, duplicate, foreign,
missing-tag and coherent stale family evidence; stale bindings, source forms,
caller identity and evaluation order; empty and populated Map inputs; and opaque
key/tag zero-callback assertions. These are normalized Core/DATA controls, not
public authenticated source admission or DSE acceptance.

## Exact accounting-only adaptations

Remove exactly two assertions per control: exact payment at the returned work
counter and underpayment refusal. Remove the work counter from diagnostic field
selection. Drop the unused `ids` destructuring binding from both restored tests;
the shared helper still constructs and returns its original IDs. No other raw
historical test bytes change. Existing allowance arguments remain only where
the unchanged APIs require them; their removal is a separate open implementation
obligation. No accounting assertion or quota is introduced here.

The census records the exact removed blocks and each adaptation. It verifies
that the entire candidate file is the unchanged base plus only these three
explicitly adapted historical units. The complete helper is byte-identical to
history; every preexisting test byte remains unchanged.

## Source dependencies and evidence scope

`effects-local-composition-census.edn` pins 22 complete selected dependency
forms across C7 and Effects. Seventeen match history, including the List-to-Set
context/call validator, both evidence functions, their originals reconstructions
and all four mixed row/fact censuses. Five differences remain explicit:
`body-lset-header`, `body-lq-context`, `body-mkeys-header`, `sh08-lq-derive` and
`sh08-ft-call-constraints-with-context`. Both hashes are retained. Their semantic
or callback equivalence is not inferred from names or the other matching forms.
This is a selected dependency census, not a transitive call-graph proof.

## Validation and acceptance

- Source integrity: PASS, three historical units, four payment assertions
  retired, unchanged prior test bytes and production, 22 dependency pins.
- Boundary gate: PASS, 6 tests / 11 assertions.
- clj-kondo 2025.10.23 with repository config/cache disabled: zero errors,
  76 warnings and one info, matching base type/level/message multiplicities.
  Initial lint exposed the two restored unused bindings; only those newly
  copied bindings were removed, then the comparison passed.
- Structural docs, full-language roadmap and whitespace: PASS.

The local Clojure source checker reads with evaluation disabled and leaves
Bootstrap unloaded. An initial redundant AST-removal comparison failed because
it substituted a nil form for a deleted block; the corrected check validates the
complete explicitly adapted raw source and all remaining complete forms.
Neither preparation result is a genuine kernel execution result.

Both restored genuine controls are UNRUN. Independent source/result acceptance,
actual Effects composition, broader accounting removal and fresh integration
remain pending. The coordinator alone routes acceptance and integration. This
restoration does not extend PR533's PMK-only preparation, PR598's emitter
controls, DSE acceptance, authenticated source, release, self-hosting, any ACC
closure or PR502 closure.
