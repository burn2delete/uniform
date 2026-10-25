# Incremental operation rename tasks

Owner: semantic-name migration. Planning reference: C16.
Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Move the operation helper to `build/incremental/operations.clj` and update
  its complete facade import/call graph without changing behavior or public names.
- [x] Add nested-failure restoration and subsequent-call regressions through
  the unchanged public facade.
- [x] Run affected leaf/composition and repository-policy checks; record passing
  checks and the pristine-base-reproduced composition failure separately.
- [ ] Independently review the exact candidate and integrate through coordinator.

Only the declared internal namespace is migrated. The facade and other legacy
submodules remain explicit future debt; this is not a second component roadmap.
