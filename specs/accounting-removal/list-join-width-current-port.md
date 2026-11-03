# Current List-join width controls

This is a non-authoritative development receipt for the useful semantic slice
of PRs 611, 637, and 638. It is bound to current main
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
structural validator also passed (non-authoritative dated snapshot). Pinned
`clj-kondo` and `cljfmt` binaries were unavailable locally, so no static-lint
pass is claimed.

The focused repair is checkpointed at `9b8072f4977fc8f050e9dac1da1d50d5bd62d52d`
(tree `832dbc69611c2b34709adb09895c3eece6a8e283`). It changes only this test
namespace; the Effects and emitter source inputs remain byte-identical. The
receipt records that checkpoint before this metadata update.

This is a bounded current-source helper test using normalized List-if inputs.
It does not establish an authoritative source migration, genuine C7/C8
admission, complete PR closure, release readiness, or self-hosting.
