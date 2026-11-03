# Current List-join width controls

This is a non-authoritative development receipt for the useful semantic slice
of PRs 611, 637, and 638. Its original development base was
`d59a7df90f91b578c1bb160a59ec1ef865abf25b` (tree
`c25b4efebc5fb76befb35edce88857012be236ad`). The historical PR identities and
path counts are recorded in the adjacent EDN receipt; their old source and
census snapshots are not silently treated as current authority.

## Preserved value

Current main already contains the two production predicate changes from PR611
and PR638. The current `effects.gravity` and `emitter.gravity` source blobs are
recorded in the EDN receipt, so this port does not replay or overwrite those
changes.

The new focused namespace carries the missing executable controls from the
three historical proposals:

- finite positional widths `0`, `1`, `1024`, `1025`, and `2049` remain accepted
  when all atomic members agree;
- Effects and emitter element reconstruction agree, including homogeneous-list
  identity and empty-list results;
- both sides of a wide `if` List join report the exact checked-member count;
- both sides of a wide `if` List join reach the current typed compile node and
  execute true and false values with preserved branch identity;
- changed atomic members, extra fields, non-vector member collections, nested
  List members, homogeneous records carrying `:element-types`, the late
  invalid member of a 2049-element list, and forged checked-member counts are
  rejected or not applicable with exact current reasons by the C7, Effects,
  emitter, context, and typed-compile consumers.
- The complete original late-invalid matrix is retained at both widths 1025
  and 2049 for keyword, unknown, nil, and nested List members. Standalone
  `:gravity.type/list`, missing homogeneous `:element-type`, malformed outer
  lists, and finite `[:gravity.type/unknown]` retain their exact atomic/list
  diagnostics.

The controls are in
`bootstrap/clojure/test/gravity/self_hosting/list_join_width_current_port_test.clj`
so the active SH09/SH01 test files owned by another workstream remain
unchanged. The historical PR census and Markdown files remain history-only
evidence; no stale source hash or work-budget assertion was refreshed.

## Check

```text
clojure -J-Xmx1g -M:dev-test --namespace gravity.self-hosting.list-join-width-current-port-test --fail-fast
3 tests, 188 assertions, 0 failures, 0 errors
```

The language-boundary gate passed 6 tests and 11 assertions. The document
validator passed 240 documents and 19 phase indexes; the full-language roadmap
structural validator also passed (non-authoritative dated snapshot).

The focused repair is checkpointed at `1c3534a3b427e34ef004eb148ad32d52d128854a`
(tree `6b3e4002f842ff41d41d6963dd37a6e660c3f7d2`). It changes only this test
namespace; the Effects and emitter source inputs remain byte-identical. The
receipt records that checkpoint before this metadata update.

Pinned `clj-kondo` 2025.10.23, invoked from
`/tmp/acc08-controls-01a183dd/kondo-2025.10.23/clj-kondo`, reported zero
warnings, errors, and infos for the changed namespace. An earlier unpinned
lookup recorded `clj-kondo` and `cljfmt` as unavailable; that observation is
historical only. `tools/ci` was unchanged, so no cljfmt check is required.

The current clean source composition checkpoint is
`40c08ae890e73e372e380066c4176b1caaacfc15`, tree
`993b8190288c512375021f60194a4c65e61d575f`, over main
`a5b6427cab15f40a8e1d61a6562a04b4a0f63efb`. The accepted test source
is unchanged by this composition; the Effects and emitter blobs still match.
The complete focused namespace passed again on that source checkpoint:

```text
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.list-join-width-current-port-test --fail-fast
3 tests, 188 assertions, 0 failures, 0 errors. Exit 0.
```

The log is `/tmp/uniform-pr611-current-main-a5b.log`. A later history-only
commit, `3df7b4f0cd00654071a8b0bc9bd64e866597ba3b`, retains all three
original PR heads with exactly the same tree. No historical parent tree is
replayed. The final integration identity is reported separately from this
source checkpoint and receipt update.

After the component-registry, cache-v1 removal, reference-runtime quota
removal, and module-lookup changes reached main, this namespace passed again
at clean source checkpoint `965672b6dd82829b72058b5a76e576b073321dfb`,
tree `a609846ade1b498e21403dd2b5668f22bbfee4de`, composed over
`0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`. The test and Gravity owner
sources are unchanged. The result remains 3 tests / 188 assertions / zero
failures or errors, exit 0, in `/tmp/uniform-pr611-current-main-0e.log`.
The earlier main composition remains historical evidence in the EDN receipt.

This is a bounded current-source helper test using normalized List-if inputs.
It does not establish an authoritative source migration, genuine C7/C8
admission, complete PR closure, release readiness, or self-hosting.
