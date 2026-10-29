# Atomic handler obligations in alias conjunctions

## Purpose and governing requirements

This ACC-07 successor to `58c1f486` checks compatibility for source-joined atomic
catch-binding contributions. C7 requires deterministic constraints with source
producers; L5 distinguishes checked restrictions from independent inferred
facts; L9 specifies typed catch clauses. No conflicting clauses were found.

The canonical backlog remains `tasks.md` ACC-07: remove inference accounting
while preserving fixed-point, cycle/conflict and fresh-verification semantics.
This finite compatibility correction does not close that task.

## Source rule and implementation

`sh08-eh-source-products` derives each catch type with
`sh08-ft-atomic-type-id` from its resolved core type binding and rejects an
unknown atomic catch type. It retains that type on the checked clause and in
the catch-binding seed map. `alias-handler-source-check` already rejoins the
contribution to a unique clause, error type, owner and source identifiers.

For that same fifteen-type atomic family, the joined restriction now has
`:checked-scalar-handler` status. `check-alias-conjunction` consumes both this
status and checked scalar parameter declarations through the existing
`sh08-ft-first-type-mismatch` consumer. All supported restrictions participate;
an unsupported import/proof/collection contribution cannot hide a scalar
conflict. The existing scalar-declaration status/reason names remain stable
for this extended family of scalar restrictions.

Coherent records outside the actual atomic catch catalog remain unsupported.
Matching records do not establish authenticated admission by themselves.
The existing handler source constructor and fresh factory verification remain
the authority for canonical source inputs.

## Outputs and conformance controls

The checked premise retains every original contribution, source witness and
provisional observation. Equal restrictions are compatible; differing atomic
restrictions conflict in either order. Invalid source has precedence over that
conflict, while its evidence remains retained. Existing resource, return and
source failures retain their precedence. The binding/type map is unchanged.

The compiler-free oracle enumerates simultaneous atomic solutions rather than
copying the production adjacent-pair walk. Controls cover the full atomic
catalog, handler/parameter conjunctions, differing seeds, incoming roles,
unsupported interleaving, forged relations/types and coherent non-catalog
records. Added Gravity controls remain unrun/held, including actual mismatch
and diagnostic checks. Prior import/source-identity controls are preserved;
only the affected held positive handler status assertion changes.

## Normalization boundary

The unique-producer census admits closed immutable lexical/def reference
equations and incoming tails; parameters and catch/import binders do not
authorize a second initializer for those bindings. Thus this correction checks
restrictions without admitting a value anchor. A compatible conjunction still
has `:grounding :not-established` and `:normalization :unresolved`. Choosing a
declaration, catch type or previous observation as its value would bypass that
source requirement. No normative conflict or new source rule is inferred.

Cycle/strict-growth detection, round/work safeguards, original inference,
source authentication, identity reconstruction and complete verification are
unchanged. Exact model/check and independent review receipts accompany the
publication. Actual Gravity/compiler/runtime/authentication/tamper/composition
execution remains unrun/held; normalization, ACC-07 acceptance, self-hosting
and release remain open.
