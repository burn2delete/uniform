# Incremental projection rename tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

Status: integrated finite slice. [Verified implementation merge](evidence.md#verified-implementation-integration):
[PR397](https://github.com/burn2delete/uniform/pull/397), `8b0b2ed4`.

- [x] Rename the internal projection namespace and its complete live references.
- [x] Preserve metadata precedence and serialized stage identity in focused tests.
- [x] Record affected test, actual planner, language and documentation checks;
  distinguish passing focused tests from unchanged baseline catalog refusals.
- [x] Independently review exact implementation `003dbbe6`/tree `ea2d10f8`:
  no blocking findings; baseline limitations retained, no compiler replay claimed.
- [x] Reconcile normally with main `d6d10012`; preserve reviewed owned blobs,
  rerun focused leaf/catalog checks and all 12 actual planner requests. Record
  the repaired catalog paths separately from unchanged nested-helper refusals.
- [x] Review the final reconciliation/evidence delta: `d17cb087`/`b56e5a0d`,
  independently accepted with no blocking findings.
- [x] Coordinator verifies the final base and merges the accepted finite change:
  `8b0b2ed4`, with independently verified merge parents and reviewed tree parity.

The [remaining private helper group](../build-incremental-helper-names/tasks.md)
was subsequently integrated by PR399. The public facade/API and test selectors
remain unchanged compatibility boundaries. This is not full component or public
API naming completion; no global naming gate or duplicate roadmap is introduced.
