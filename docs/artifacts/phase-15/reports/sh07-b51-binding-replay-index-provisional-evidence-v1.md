# SH-07 B51 Binding-Replay Index Provisional Evidence V1

Status: localization and architecture provenance only; no implementation,
performance, acceptance, or SH-07 completion authority

Date: 2026-09-07

## Baseline

The authoritative starting point is clean local main:

```text
commit 0a391faa3ea0cc299e60b476df0c3718d4b5af8c
subject Merge fresh authenticated artifact proof handoff
```

The integrated dependencies are:

```text
Attempt-32 architecture candidate 202d92d12fa6c4f6a74571eb4769587c02c0963c
Attempt-32 integration merge       8f7b90ac
V3 architecture candidate          a69e9be8664a054734ff029143ed519e323755dd
V3 integration merge               3650e57c
V3 lifecycle publication           f1c0c139
```

The unaccepted implementation checkpoint used for read-only localization is:

```text
branch codex/sh07-b51-attempt32-implementation-v1
commit 6974b0abb9ee27db57014a247fbb13718de66603
tree   1c0b09f2124fc0b674874d378dfdac15541239fd
V3 implementation commit c8dda98ae624c0f27a7cf5c5bf1e41852b8494de
```

That branch is draft evidence only. Its active record has no candidate commit,
clean-worktree assertion, review, accepted evidence, integration eligibility,
or completion authority.

## Observed failures

The deep successor suite reached Gravity admission but produced exactly two
fixed-limit failures relevant to this decision:

1. selected Root 8 making its required byte-identical public Root-1 call;
2. the declared external-binding fixture selecting Root 1.

Both returned `L2-UNKNOWN-CORE-FORM` with reason
`:runtime-machine-bound`, false result, and no successful output. These
observations are diagnostic; the transcript was not frozen as acceptance
evidence.

## Bounded localization

A selected Root-8 prefix stopped at 250,000 transitions with 111,313 `:eval`
and 138,687 `:value` transitions, maximum 29 frames, and no receipt
consumption yet. Execution remained in retained producer validation.

A direct authentic Root-8-to-Root-1 child prefix stopped at 500,000 transitions
with 223,804 `:eval` and 276,196 `:value` transitions, maximum 82 frames,
exactly one pending entry, exactly one receipt consumption, and no
receipt-created recurrence. Its tail was in
`sh07-b51-v16-binding-record-base-shape?`, SHA-256, and lowercase-hex work.

The V3 mechanism therefore behaved once-per-invocation in this prefix. The
observations do not prove completion or exclude another implementation defect.

## Exact source evidence

Read-only inspection of producer A and producer B at the draft checkpoint
establishes:

- `sh07-b51-index-request` builds `:forms/:by-secondary` from the exact request
  using syntax id;
- `sh07-b51-find-form-by-syntax` performs the existing indexed lookup;
- `sh07-b51-derived-no-form-failing-binding-ids` first rejects duplicate form
  and binding primary/secondary keys;
- inside its binding loop it nevertheless calls `sh07-b51-find-by` across the
  complete `request/:forms` vector for every binding;
- the loop then retains the full binding-record shape and digest checks; and
- all current callers construct the index locally from the same request.

The index substitution therefore removes an observed repeated scan without
inventing a new data structure or weakening duplicate handling. This is an
architecture hypothesis until a governed implementation rebuilds every
identity and produces the evidence required by the decision.

## Prohibited interpretations

This evidence does not authorize editing the draft implementation branch,
raising fuel, resetting or refunding counters, moving work to the host,
accepting supplied indexes, skipping digest validation, changing external
binding rules, or claiming a speedup. It does not establish that the successor
will complete within 8,388,608 transitions.

Overall SH-07 remains partial and four of the current eight completion stages
are complete.
