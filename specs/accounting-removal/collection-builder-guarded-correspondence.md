# Collection builder guarded-source correspondence proposal

Owner: ACC-01. This proposal covers four original source units without changing
production: three List-query builders at main
`70dcb2220701529dbb02bd2317a822eb3ac72fed`, and the C7 List-construction
builder in existing PR623 `c5e9fccdad293b34c290859cc3e4716870241dd6`.
The separate Effects/lowering construction proposal is not duplicated.

## Complete historical bodies and exact guards

All four complete historical bodies from PR502
`6a4f87f013d0076b81aba9e0667650e70d28e635` occur at reader path `[3 2 2]`
inside their source-version-specific native-map and validation guards. Argument
lists are unchanged, and both failure branches are nil. Complete function/body,
file, source commit/tree, original-unit, guard and governing-contract pins are
recorded in the companion EDN. No Gravity source terms are normalized.

The C7 constructor in PR623 permits exactly `{:status :accepted}` after its
native guard. At current main its one additional inner predicate is
`(<= (count args) 1023)`. Removing exactly that predicate from the current
function reconstructs PR623's complete function; that draft then contains the
complete historical body behind its guards. This is an existing implementation
route, so no duplicate constructor source change is added. It does not accept
PR623, its pipeline, wider inputs or its dependent PR636 controls.

The three current query builders have a distinct guard. After native-map
validation they allow either the exact accepted cell or exactly the keys
`#{:status :head-proof}` with accepted status. This second alternative is
preserved explicitly; it is not normalized to the constructor's one-field
cell. The wrapper does not certify the head-proof payload. Its validation,
use and effect remain governed by the unchanged historical body and its
dependencies, whose runtime behavior is not established here.

## Requested classification and exclusions

The proposed decision is conditional complete-body source incorporation under
pinned, unrebound source/native-guard bindings and L2 control semantics. Whole
old/new function behavior, validation-input contracts and behavior outside the
guarded path are not equated. Earlier predicate decisions supply only native
guard source pins; they are not extended to these four units.

Native prelude effects, callbacks, exceptions, termination, mutable bindings
and transitive/runtime behavior remain unproved. Other guard uses or numeric
conditions are not approved. Existing accounting and numeric conditions inside
historical bodies remain visible and unendorsed. No main or draft integration,
wide cross-stage support, head-proof validity, whole collection support or
PR502 closure follows.

## Source checks and remaining acceptance

The four complete source equations and argument lists, original memberships,
native guard pins and the exact current-to-PR623 constructor equation pass.
Thirty effective source corruptions refuse native/cell bypass, status-only
weakening, lost bodies, accepted failures, changed arities, lost head-proof
alternatives and unchecked head-proof status. No source helper, Bootstrap,
native provider, compiler or runtime executes.

L2, C7, C8, C14, D9 and current accounting-removal policy govern review.
Independent classification remains pending. The original inventory and stable
ACC IDs are unchanged; no release or self-hosting credit is claimed.
