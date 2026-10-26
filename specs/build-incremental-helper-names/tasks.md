# Incremental build helper tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

Status: integrated finite slice. [Verified implementation merge](evidence.md#verified-implementation-integration):
[PR399](https://github.com/burn2delete/uniform/pull/399), `2bb0a6db`.

- [x] Inventory and move the complete remaining four-helper private group.
- [x] Preserve public facade calls and compatibility identities.
- [x] Execute focused new regressions before changing helper namespaces.
- [x] Execute candidate leaf, adjacent, actual planner and policy/doc checks.
- [x] Obtain independent exact-implementation review: `431eaaad`/`4f782354`,
  no blocking findings; planner limitation retained, no compiler replay claimed.
- [x] Coordinator verifies final base and integrates the reviewed group:
  `2bb0a6db`, after final `cc43455a`/`f5af10b4` delta acceptance; merge parents
  and reviewed tree parity independently verified.

Completion applies only to this private helper group, not the whole component
or public API migration. No automatic follow-on rename of the public facade or
test selectors is included.
