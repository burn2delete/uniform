# Incremental projection rename tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Rename the internal projection namespace and its complete live references.
- [x] Preserve metadata precedence and serialized stage identity in focused tests.
- [x] Record affected test, actual planner, language and documentation checks;
  distinguish passing focused tests from unchanged baseline catalog refusals.
- [x] Independently review exact implementation `003dbbe6`/tree `ea2d10f8`:
  no blocking findings; baseline limitations retained, no compiler replay claimed.
- [x] Reconcile normally with main `d6d10012`; preserve reviewed owned blobs,
  rerun focused leaf/catalog checks and all 12 actual planner requests. Record
  the repaired catalog paths separately from unchanged nested-helper refusals.
- [ ] Review the final reconciliation/evidence delta.
- [ ] Coordinator verifies the final base and merges the accepted finite change.

The public facade and other legacy helpers remain explicit future debt; no
global naming gate or duplicate component roadmap is introduced.
