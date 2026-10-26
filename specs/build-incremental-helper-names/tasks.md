# Incremental build helper tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Inventory and move the complete remaining four-helper private group.
- [x] Preserve public facade calls and compatibility identities.
- [x] Execute focused new regressions before changing helper namespaces.
- [x] Execute candidate leaf, adjacent, actual planner and policy/doc checks.
- [x] Obtain independent exact-implementation review: `431eaaad`/`4f782354`,
  no blocking findings; planner limitation retained, no compiler replay claimed.
- [ ] Coordinator verifies final base and integrates the reviewed group.

No automatic follow-on rename of the public facade or test selectors is included.
