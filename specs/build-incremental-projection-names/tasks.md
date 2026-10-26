# Incremental projection rename tasks

Spec: [spec.md](spec.md). Plan: [plan.md](plan.md). Evidence: [evidence.md](evidence.md).

- [x] Rename the internal projection namespace and its complete live references.
- [x] Preserve metadata precedence and serialized stage identity in focused tests.
- [x] Record affected test, actual planner, language and documentation checks;
  distinguish passing focused tests from unchanged baseline catalog refusals.
- [x] Independently review exact implementation `003dbbe6`/tree `ea2d10f8`:
  no blocking findings; baseline limitations retained, no compiler replay claimed.
- [ ] Coordinator verifies the final base and merges the accepted finite change.

The public facade and other legacy helpers remain explicit future debt; no
global naming gate or duplicate component roadmap is introduced.
