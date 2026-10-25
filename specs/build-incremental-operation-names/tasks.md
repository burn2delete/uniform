# Incremental operation rename tasks

Owner: semantic-name migration. Planning reference: C16.
Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Move the operation helper to `build/incremental/operations.clj` and update
  its complete facade import/call graph without changing behavior or public names.
- [x] Add nested-failure restoration and subsequent-call regressions through
  the unchanged public facade.
- [x] Run affected leaf/composition and repository-policy checks; record passing
  checks and the pristine-base-reproduced composition failure separately.
- [x] Independent implementation review accepted exact commit
  `ab77968ec97a0d9032ea32b5f049c3ad52f2aab6`, tree
  `e7ca3f457d53e3adc01cd7cc88accbf4ab853a55`, with the documented baseline
  composition failure and planner limitations retained.
- [ ] Coordinator verifies the documentation-only successor's exact head,
  unchanged implementation and final-main compatibility before integration.
- [ ] Coordinator merges the accepted finite change to main.

Only the declared internal namespace is migrated. The facade and other legacy
submodules remain explicit future debt; this is not a second component roadmap.
