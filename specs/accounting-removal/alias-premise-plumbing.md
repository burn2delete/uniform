# Source-bound alias premise plumbing

## Purpose and governing requirements

This finite increment follows the reviewed
[alias seed decision](alias-seed-decision.md) at
`d3b66dd502032998a123edcf711b4f2c0d7c1dd0`. It implements retained source
provenance in C7. It does not implement alias normalization or close ACC07.

No contradictory normative clauses were found.
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
requires constraints with producers, dependencies and invalidation conditions.
[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
distinguishes declared boundaries from inferred facts. Its Bare Map Parameters
rule explicitly permits a checked positional Map to satisfy bare Map without
asserting equal type records. Consequently a conjunction of source obligations
must use each producer's compatibility relation; raw unequal records alone
cannot establish a contradiction.

## Actual source inputs and implementation

The owning implementation remains
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
`inference-seed-source-premises` reconstructs these contributions from the
same inputs and constructors used by the actual artifact entry point:

| Producer | Existing constructor | Retained source authority input | Required relation |
| --- | --- | --- | --- |
| Recursive/higher-order premise | `sh08-ft-inference-context` | Existing accepted recursive or higher-order proof and completed function skeletons | That producer's proof-specific relation |
| Imported function | `sh08-im-seeds` | Actual import records, including their producer/artifact identities | Imported function type |
| Declared parameter | `sh08-ft-declaration-seeds` | Function signature/declaration source records | Declared parameter compatibility |
| Handler binding | `sh08-eh-seed-bindings` consumes the already-derived handler seeds | Actual source handler groups and seed map | Source handler binding type |

The existing entry point still constructs and consumes the original seed map.
The new path retains each contribution independently, including contributions
overridden by a later `assoc`. No caller-supplied authority flag, seed policy
option or replacement seed input is introduced. The existing product/source,
signature, import and proof admission boundaries remain authoritative; direct
helper fixtures remain internal DATA and confer no authentication.

`inference-alias-components` uses the actual lexical records and matched def
producers. It records the members and edges of closed pure-reference components
in deterministic source order, with original equation/reference/definition
records and their available source metadata. It also retains every supported
incoming alias path, so an external tail's seed obligation is not lost merely
because the tail is outside the SCC. It refuses a component census
with duplicate/overlapping/parameter producers or duplicate node identities.
Visibility and intrinsic-operator exclusions survive. Constructors, opaque
calls and callbacks are not alias edges; loop/match transfer contexts are
unsupported. A constructor cycle remains owned by the existing detector.

Unlike that detector, this source census does not require a known normal type.
Unknown or Never observations may be retained as observations without becoming
a normal-type or grounding proof. Source topology is independent of the
previous round's provisional type values.

## Retained output and identity

For each component and its incoming alias paths, `retain-alias-source-premises`
retains every matching source seed contribution as `:source-seed-conjunction`,
with its binding, type, producer, relation, member/incoming role and source
records. It separately retains
`:provisional-observations`. No source contribution is selected as the winning
constraint, compared using an invented universal equality rule, or discarded.

The fields `:compatibility-decision` and `:normalization` remain
`:not-performed`. `:no-entry-seed-contribution` states only the absence of a
matching contribution from the real entry constructors; it is not a complete
independent grounding or source-initialization theorem. Otherwise the premise
requires producer-specific compatibility checking. Retaining a conjunction
does not mean it has been solved or accepted.

The actual artifact factory reconstructs this evidence after its original
inference/diagnostic path. Nonempty component evidence is retained under
`:convergence :alias-equivalence-premises` and the existing identity input and
artifact-id request. The typed-core schema and top-level accepted schema are
unchanged. Existing Effects identity reconstruction already preserves the
whole convergence value. No-component results remain unchanged. Rejected
results keep their existing diagnostics and status; no inferred type, seed,
round/work field or source authentication check is changed.

Fresh complete-result verification must reconstruct the evidence from the
actual inputs. It must reject missing, stale, duplicated or coherently forged
members, edges, source records, constraints or identity copies. The new held
helper tests check fresh evidence replacement and identity consistency; they
are not a claim that full authenticated tamper acceptance has run.

## Validation and next implementation gate

The new compiler-free SCC oracle uses mutual transitive reachability rather
than the source implementation's rooted path walk. It checks seed-independent
components, tails, separate components and duplicate/parameter/intrinsic/
loop/match/constructor/opaque-callback exclusions. The previously reviewed
oscillation/partial-grounding countermodel remains unchanged and is reused.
New held Gravity controls check actual producer reconstruction, retention of
all four overlapping contributions, Unknown/definition components, identity
copies, fresh replacement, unchanged diagnostics/types and the no-component
case. No Gravity or compiler execution is claimed by model or reader checks.

The next solver increment must consume the retained conjunction with the
existing producer-specific compatibility rules and independently grounded
source facts. It must establish a complete mandatory-constraint census before
invalidating provisional observations; stable compatible facts must survive,
incompatible mandatory obligations must conflict, and an ungrounded component
must remain unresolved. Unsupported relations must remain unresolved or
explicitly rejected, not silently selected, widened or unioned. Mandatory
declarations/imports/proof facts cannot be dropped during recomputation.

Actual semantic solver, source-authentication, coherent-tamper and affected
composition acceptance remain open. Round/work safeguards remain until genuine
convergence evidence justifies their removal. This source/model increment
claims no general convergence, accounting-removal completion, release or
self-hosting authority. Reconciliation artifacts and historical evidence are
unchanged.
