# Sharded Workstream Ledger

## Purpose

The canonical coordination ledger is a compact manifest at
`contracts/workstream-ledger.json`. Workstream content is stored independently
so a lifecycle update touches one small file instead of a large aggregate JSON
document. The representation preserves the v1 decoded aggregate and all
governance decisions; it changes storage and publication granularity only.

## Layout

Terminal records are immutable and content-addressed:

```text
contracts/workstream-records/terminal/<workstream-id>-<sha256>.json
```

Active candidates are reservations, one file per candidate:

```text
contracts/workstream-active/<workstream-id>.json
```

The manifest index is sorted by stable workstream id. Each entry records the
relative path, SHA-256, lifecycle state, invariant family, dependencies,
terminal flag, and source ordinal. The ordinal reconstructs the historical v1
ordering without making directory enumeration authoritative.

Active files are not reusable result caches. They reserve an invariant family
for the candidate lifecycle states (`draft`, `frozen`, `review-pending`,
`accepted`, and `integration-eligible`). A candidate becomes available only
when its active reservation is closed by a terminal lifecycle record. The
validator still checks dependency floors, cycles, failure-stop architecture
decisions, review independence, evidence, and authority ceilings over the
decoded aggregate.

## Integrity and migration

Every referenced file is a regular non-symlink UTF-8 JSON file. Terminal names
must contain their exact content hash; active names must match their candidate
id. The validator rejects traversal, absolute, home-relative, backslash, stale,
missing, duplicate, or unreferenced paths. Hash mismatches, aggregate digest
mismatches, and migration divergence use `WG013`.

The manifest records the SHA-256 of the canonical decoded `workstreams` vector
and a migration proof (`source_schema_version: 1`, `decoded_record_count: 114`,
`parity: exact`). Re-running the migration utility is deterministic:

```bash
clojure -M tools/migrate_workstream_ledger.clj \
  --source contracts/workstream-ledger-v1.json \
  --manifest contracts/workstream-ledger.json
```

The v1 aggregate is retained as a migration source and audit reference, but
normal governance validation reads the compact v2 manifest and its shards. The
migration proof is a frozen snapshot: later terminal publications advance the
manifest aggregate digest without rewriting the v1 source or its proof.

## Validation

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M:test --namespace gravity.self-hosting.sh01-workstream-ledger-sharding-test
```

Lifecycle publication should append or replace one candidate shard and close
one active reservation. It should not rewrite the full manifest or replay all
historical terminal records. A manifest update is required only when the
candidate index, aggregate digest, or counts change.

The guarded publisher performs that transition after validating the complete
decoded aggregate. It writes a new terminal shard, atomically replaces the
compact manifest, and then removes the active reservation:

```bash
clojure -M tools/publish_workstream_terminal_record.clj \
  --candidate contracts/workstream-active/<workstream-id>.json \
  --dry-run
```

Remove `--dry-run` only after the candidate record has its final terminal state
and independent review/evidence. A failed publication leaves the old
reservation in place; the validator rejects any incomplete publication with
`WG013` rather than inferring a successful close.

## Nonclaims

Sharding does not grant integration authority, implementation correctness,
release authority, self-hosting authority, seed-retirement authority, or
semantic cache reuse. Terminal content addressing detects mutation; it does
not replace independent review or exact integration preflight.
