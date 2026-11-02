# PR597 C6 reader policy current-main assessment

## Identity and scope

The original PR597 base is `f0be2a55cc348fd1c9ce8eca377b2427541643b7`
(tree `5f7ed31ee0c2a342b8ee053a3dcaa9f6ef0db2ce`). Its exact head is
`6a5977cd34d8db438226424fe38618ebe9989498` (tree
`9de8eeaa08757e5a8e8d03d866b36df69a7e9574`). The authored delta is eight
paths: four C6 component files, two reports, and two tooling tests. The
current composition base is `f43e77992fe86b6dfb55df3516b27cfcdea7fa40`
(tree `a0e433f9f185b651f6f5ebc54340a085a5cd6721`).

This review preserves useful behavior without replaying the stale source
binding implementation from the old PR branch.

## Whole-path accounting

- `00702-p15-s23-c6c10-host-fail.clj`: PR597 adds an explicit source-contract
  require. Current main already loads that namespace before the P15 component
  stream through the existing bootstrap component loader; no semantic change
  is needed.
- `00719-p15-s23-c6c10-record-compiled-binding-metric.clj`: the PR version
  removes current-source support and restores only pinned reads. Replaying it
  would regress the current `:development-current` C6 owner. It is rejected
  as stale implementation, while the historical source-binding behavior is
  retained in current main.
- `00720-p15-s23-c6c10-compiled-binding-cache-key.clj`: the PR version has
  the same regression: it removes current-source cache identity and current
  binding validation. Current main keeps the reviewed versioned/current split.
  No old implementation is replayed.
- `00723-p15-s23-c6c10-seal-digest-request-result.clj`: the PR's 267 lines of
  Reader policy, envelope-join, native-header, metadata projection, and
  expression-owner helpers are already present on current main in the split
  owner files `00720b`, `00720c`, and `00720d`, and are consumed by
  `00724a`'s v2 fresh-construction path. No duplicate helper body is added.
- `docs/artifacts/phase-15/reports/c6-versioned-source-binding.md` and the
  PR's 238-line `tools/c6_source_binding_test.clj`: these are the PR594
  source-binding increment and are already represented by current main's
  source-contract/cache/current-owner implementation and controls. They are
  not replayed.
- `docs/artifacts/phase-15/reports/c6-reader-policy-projection.md` and
  `tools/c6_reader_policy_test.clj`: the policy-control value was not present
  at the current base. The test is ported below against the current split
  owner files, with a current-source evidence report.

## Current owner behavior verified

The current split implementation retains the useful joins required by C6,
C11, D9, and the C6/C11 source contracts:

- `00720b` authenticates current C6/compiler inputs before and after the
  Reader-owner binding, checks selected path, fixed Reader identities,
  required exports, emitter identity, and declared plan labels, and explicitly
  records that Reader execution and envelope state were not observed.
- `00720c` joins C2 artifact, C3 boundary provenance, Reader revision/result,
  integrity, source identity, envelope identity, and provenance identity. It
  verifies the full unmodified envelope before projecting a receipt, requires
  `:development-unadmitted`, and crosses only the fixed projection into the
  strict comparator-free Core carrier.
- `00720d` extracts only native keyword-header pairs, rejects foreign/header
  classes and duplicate keys without callbacks, permits default-comparator
  owner metadata, rejects metadata-bearing/custom-comparator values, and
  records separate content identities for Reader and expression-owner data.
- `00724a` calls the current Reader owner only for public C6 source version 2.
  Public C6 version 3 remains rejected by the existing interface boundary;
  this port does not invent a v3 Reader route or claim v3 product evidence.

## Ported controls and evidence

`tools/c6_reader_policy_test.clj` now evaluates the current `00720b/c/d`
owners rather than the old 00723 location. It retains six mechanism tests
covering:

- current source-input bookends and explicit nonclaims;
- source/path/export/emitter drift refusal;
- public v2 routing and default arity behavior;
- Reader and expression projection schema/content identities;
- C2/C3/result/integrity/source/envelope/provenance joins and admission;
- native header, metadata, comparator, and zero-foreign-callback refusals.

The control run passed:

```text
clojure -J-Xmx256m -M tools/c6_reader_policy_test.clj
relative policy: 4 tests / 54 assertions / 0 failures / 0 errors
reader policy: 6 tests / 39 assertions / 0 failures / 0 errors
```

The relative-policy harness required a five-line compatibility repair in
`tools/c11_relative_policy_test.clj`: current `00727` refers to the actual
`gravity.mir` namespace and current `00740` resolves the split expression
projection by symbol. The added alias/declarations only make isolated
mechanism evaluation resolve current owners; they do not change production
semantics or source authority.

`git diff --check` passes. The test uses actual native carrier/header parsing
and canonical digest code, but synthetic Reader/emitter/envelope owners. It
does not authenticate source bytes, execute the Reader, run Bootstrap, or
prove C3/C11 admission.

## Decision and nonclaims

Accept the narrow current-main port as test/report conservation for PR597.
Do not replay the original `00719`/`00720` implementation, because it would
remove current-source authentication and cache identity already required by
current main. The production helper semantics are credited to the current
main `#552` composition; this candidate adds only the missing current-owner
mechanism controls and evidence.

This increment claims no fresh source authority, C6/C11 migration, v3 public
construction, C13/C14/B1 completion, native-C execution, release, self-hosting,
seed retirement, or canonical admission. Independent review must bind the
final candidate commit/tree and clean worktree before integration.
