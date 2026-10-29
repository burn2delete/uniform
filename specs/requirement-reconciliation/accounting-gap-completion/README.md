# Accounting historical-gap criterion completion

This is the explicitly reassigned accounting-policy (six IDs) and
accounting-removal (two IDs) slice of the canonical historical-gap partition.
The earlier phase-02 slice remains empty. No other family is assigned here.

The [proposal](proposal.edn) binds all eight historical IDs to full criteria
under existing tasks in the sole [ACC ledger](../../accounting-removal/tasks.md).
Each criterion has acceptance conditions, real rejection cases, required
outputs and dependencies. These are eight criteria, not eight new tasks:

| Historical ID | Existing task | Criterion |
| --- | --- | --- |
| policy/accounting/source-grounded-inventory | ACC-01 | accounting-source-inventory |
| policy/accounting/remove-artificial-refusals | ACC-12 | accounting-artificial-refusal-removal |
| policy/accounting/preserve-semantic-validation | ACC-12 | accounting-semantic-validation-preservation |
| policy/accounting/host-external-boundary-separation | ACC-12 | accounting-host-external-boundaries |
| policy/accounting/beyond-limit-and-late-invalid-evidence | ACC-12 | accounting-beyond-limit-late-invalid |
| policy/accounting/closure-evidence-record | ACC-12 | accounting-closure-evidence |
| policy/development/autonomy-and-conflict-coordination | ACC-12 | development-autonomy-conflict-coordination |
| policy/development/exact-candidate-admission | ACC-12 | development-exact-candidate-admission |

The source pins are the original AGENTS accounting policy and accounting-removal
spec, unchanged at their original hashes. Every original facet, occurrence,
raw span, repeated membership and historical obligation is retained. The
complete 577-ID history, existing 13 witnesses and three retirements remain
separate immutable inputs. No policy constraint becomes implementation credit.

The patch inserts one marked criterion section; removing it reproduces the
exact prior task file, including all task rows, checkboxes, restrictions and
historical suffixes. The task-source predecessor at the partition's source
`43598c5b87feb4d88928549bb20fc4089768718b` is byte-identical to this worktree's
input. A later changed source must be composed explicitly; a whole-file
overwrite is refused. ACC05's active amendment selection contains none of
these eight IDs, and its cross-family work is unchanged.

Forty field operations describe the proposed current coverage projection:
disposition, exact criterion source/span/hash, criterion text, existing task ID
and qualification. Each has collection/ID/field-path and exact predecessor
presence/value. They do not rewrite the original review receipt, canonical
source-dispositions, task-baseline or another owner's checkout. Unrelated
historical rows, semantic context and implementation acceptance remain equal.

Concrete mapping proposals: eight. Independently accepted mappings: zero.
Unresolved assigned IDs: none. New task count and task-count delta: zero.
ACC-01 and ACC-12 remain their existing unchecked, open/partial outcomes; their
full implementation evidence is still required. Whether the existing ACC tasks
need exposure in the component census belongs to the canonical owner, not a
new population or denominator here. Source review and adoption remain pending.

## Validation and adoption

Run the [Clojure checker](../../../tools/accounting_gap_completion.clj), supplying
the frozen coverage receipt as `AUDIT` and canonical partition as `PARTITION`:

```bash
clojure -J-Xmx512m -M tools/accounting_gap_completion.clj --check AUDIT PARTITION
```

The checker independently pins both inputs, the committed PR445 manifest and
the old/new task bytes. Its positive case and eleven negative controls cover
stale and duplicate task/projection application, forged criterion coverage,
wrong task/source identity, lost negative conditions/context, an unassigned
row and unsupported implementation credit. All twelve controls pass. Standalone
lint reports zero errors/warnings; language boundary passes 6 tests/11 assertions;
the 240-document/19-index and full-language roadmap validators and whitespace
check pass. No compiler, runtime, broker, source-authentication, host-policy,
integration or historical heavy acceptance path was executed.

The canonical owner can review each witness and its before/after projection,
compose the guarded task insertion against the actual later source and adopt
only independently accepted mappings. This does not close any ACC task or
authorize a held/denied action. No additional ledger, worker or reviewer exists.
