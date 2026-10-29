# Alias import and handler source joins

## Purpose and requirement

This finite ACC-07 increment follows the reviewed scalar conjunction checks at
`0e9003819f75fbb5293f4332f6704a1733e5fae5`. It closes two retained-source join
gaps in `alias-seed-constraint-check`. Valid import/handler contributions still
do not establish an eligible alias-value grounding producer or normalization.

C7 requires producer-bound constraints and source identity. The actual import
and handler constructors already derive the records needed for these joins;
the successor consumes them rather than accepting a caller authority flag.
The owning implementation is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.

## Imported function join

`sh08-im-origin` produces the accepted import record after its existing
caller/producer origin and export checks; `sh08-im-seeds` copies its function
type. `alias-import-source-check` verifies the retained import key and binding,
its SH06 origin and SH07 identity links, and the unique definition/function
rows in the retained producer identity. It reuses
`sh08-im-complete-signature?` and the same exported function projection as
`sh08-im-origin`, then compares that projection with both the import's function
type and the contribution's type.

Missing, renamed, ambiguous, malformed or inconsistent identities/signatures
are rejected. A structurally matching retained record remains unsupported for
alias grounding. This join does not replace the original authenticated import
admission, export visibility, profile or producer verification boundary.

## Handler join

`sh08-eh-source-products` derives source handler groups, clause records,
checked error types and catch binding seeds. `sh08-eh-seed-bindings` consumes
that seed map. `alias-handler-source-check` locates exactly one clause for the
contribution's catch binding across the retained groups. It verifies the error
type, group/owner node, handler node and canonical source identifiers.

Missing or duplicate catch witnesses, wrong error types, wrong owners/handlers
and malformed source IDs are rejected. Valid joined catch records remain
unsupported as alias initializers; neither source ordering nor a matching
catch type grants value grounding, runtime reachability or effect authority.

## Outputs, controls and preservation

Both joins retain their source witness and original mandatory contribution.
The existing conjunction checker and diagnostic path consume their checked
status: a source mismatch is `C7-VERIFY`, ahead of a retained scalar conflict
when no pre-existing semantic/source/resource failure has precedence. All
obligations, provisional observations and scalar conflict evidence survive.

The new compiler-free relational model checks source joins independently of
the Gravity helper's selection loops. Positive matching records remain
unsupported; type/binding/producer/relation, duplicate, missing, inconsistent
producer/function and handler-owner forgeries refuse. Fixture IDs are internal
DATA, not authenticated source IDs. New held Gravity controls cover the actual
helpers and preserved diagnostic precedence. Prior models and source controls
are retained unchanged. Exact checks/review belong to the publication receipt.

No binding normalization, type selection, union, seed deletion, new policy
option or acceptance credit is introduced. Existing source/proof admission,
round/work and return-conflict safeguards remain. Actual Gravity, authentication,
tamper and composition execution is unrun/held; grounded alias normalization,
ACC-07 accounting removal, release and self-hosting remain open.
